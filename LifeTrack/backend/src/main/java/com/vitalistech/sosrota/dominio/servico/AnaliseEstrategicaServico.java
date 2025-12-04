package com.vitalistech.sosrota.dominio.servico;

import com.vitalistech.sosrota.dominio.modelo.*;
import com.vitalistech.sosrota.dominio.repositorio.*;
import com.vitalistech.sosrota.util.AlgoritmoDijkstra;
import com.vitalistech.sosrota.util.ResultadoRota;
import com.vitalistech.sosrota.web.dto.BairroSugeridoDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável por análises estratégicas para posicionamento de ambulâncias.
 */
@Service
public class AnaliseEstrategicaServico {

    private final BairroRepositorio bairroRepositorio;
    private final OcorrenciaRepositorio ocorrenciaRepositorio;
    private final AmbulanciaRepositorio ambulanciaRepositorio;
    private final RuaConexaoRepositorio ruaConexaoRepositorio;

    public AnaliseEstrategicaServico(BairroRepositorio bairroRepositorio,
                                     OcorrenciaRepositorio ocorrenciaRepositorio,
                                     AmbulanciaRepositorio ambulanciaRepositorio,
                                     RuaConexaoRepositorio ruaConexaoRepositorio) {
        this.bairroRepositorio = bairroRepositorio;
        this.ocorrenciaRepositorio = ocorrenciaRepositorio;
        this.ambulanciaRepositorio = ambulanciaRepositorio;
        this.ruaConexaoRepositorio = ruaConexaoRepositorio;
    }

    /**
     * Retorna lista de bairros sugeridos para posicionamento de novas ambulâncias.
     * A análise considera:
     * - Número de ocorrências no bairro
     * - Tempo médio de resposta para outros bairros com ocorrências
     * - Número de ambulâncias já existentes no bairro
     */
    public List<BairroSugeridoDTO> obterBairrosSugeridos() {
        List<Bairro> todosBairros = bairroRepositorio.findAll();
        List<Ocorrencia> todasOcorrencias = ocorrenciaRepositorio.findAll();
        List<Ambulancia> todasAmbulancias = ambulanciaRepositorio.findAll();
        List<RuaConexao> todasConexoes = ruaConexaoRepositorio.findAll();

        // Agrupar ocorrências por bairro
        List<Bairro> bairrosComOcorrencias = todasOcorrencias.stream()
                .map(Ocorrencia::getBairroLocal)
                .distinct()
                .collect(Collectors.toList());

        // Agrupar ambulâncias por bairro base
        List<BairroSugeridoDTO> sugestoes = new ArrayList<>();

        for (Bairro bairro : todosBairros) {
            // Contar ocorrências neste bairro
            int ocorrenciasNoBairro = (int) todasOcorrencias.stream()
                    .filter(o -> o.getBairroLocal().getId().equals(bairro.getId()))
                    .count();

            // Contar ambulâncias existentes neste bairro
            int ambulanciasExistentes = (int) todasAmbulancias.stream()
                    .filter(a -> a.isAtiva() && a.getBairroBase().getId().equals(bairro.getId()))
                    .count();

            // Calcular tempo médio de resposta para outros bairros com ocorrências
            double tempoMedioResposta = calcularTempoMedioResposta(bairro, bairrosComOcorrencias, todasConexoes);

            // Calcular quantos bairros são alcançáveis a partir deste bairro (alcance de conexões)
            int bairrosAlcancaveis = calcularBairrosAlcancaveis(bairro, todosBairros, todasConexoes);

            // Gerar justificativa
            String justificativa = gerarJustificativa(ocorrenciasNoBairro, tempoMedioResposta, ambulanciasExistentes, bairrosAlcancaveis, todosBairros.size());

            BairroSugeridoDTO dto = new BairroSugeridoDTO(
                    bairro.getId(),
                    bairro.getNome(),
                    justificativa,
                    ocorrenciasNoBairro,
                    tempoMedioResposta,
                    ambulanciasExistentes,
                    bairrosAlcancaveis
            );

            sugestoes.add(dto);
        }

        // Ordenar por prioridade: maior alcance de conexões, mais ocorrências, menor tempo médio, menos ambulâncias existentes
        return sugestoes.stream()
                .sorted(Comparator
                        .comparingInt((BairroSugeridoDTO dto) -> dto.getBairrosAlcancaveis()).reversed()
                        .thenComparingInt((BairroSugeridoDTO dto) -> dto.getOcorrenciasNoBairro()).reversed()
                        .thenComparingDouble(BairroSugeridoDTO::getTempoMedioResposta)
                        .thenComparingInt((BairroSugeridoDTO dto) -> dto.getAmbulanciasExistentes()))
                .collect(Collectors.toList());
    }

    /**
     * Calcula o tempo médio de resposta (em minutos) de um bairro para todos os outros bairros com ocorrências.
     * Usa Dijkstra para calcular rotas e assume velocidade de 60 km/h.
     */
    private double calcularTempoMedioResposta(Bairro bairroBase, List<Bairro> bairrosDestino, List<RuaConexao> todasConexoes) {
        if (bairrosDestino.isEmpty()) {
            return 0.0;
        }

        double somaTempos = 0.0;
        int rotasValidas = 0;

        for (Bairro destino : bairrosDestino) {
            if (bairroBase.getId().equals(destino.getId())) {
                continue; // Pular se for o mesmo bairro
            }

            ResultadoRota rota = AlgoritmoDijkstra.calcularRota(bairroBase, destino, todasConexoes);
            
            if (!Double.isInfinite(rota.getDistanciaKm())) {
                // Tempo em minutos = (distância em km / 60 km/h) * 60 minutos/hora
                double tempoMinutos = (rota.getDistanciaKm() / 60.0) * 60.0;
                somaTempos += tempoMinutos;
                rotasValidas++;
            }
        }

        return rotasValidas > 0 ? somaTempos / rotasValidas : 0.0;
    }

    /**
     * Calcula quantos bairros são alcançáveis a partir de um bairro base usando Dijkstra.
     * Isso representa o "alcance" de conexões do bairro na rede viária.
     */
    private int calcularBairrosAlcancaveis(Bairro bairroBase, List<Bairro> todosBairros, List<RuaConexao> todasConexoes) {
        int alcancaveis = 0;

        for (Bairro destino : todosBairros) {
            if (bairroBase.getId().equals(destino.getId())) {
                continue; // Pular o próprio bairro
            }

            ResultadoRota rota = AlgoritmoDijkstra.calcularRota(bairroBase, destino, todasConexoes);
            
            if (!Double.isInfinite(rota.getDistanciaKm())) {
                alcancaveis++;
            }
        }

        return alcancaveis;
    }

    /**
     * Gera uma justificativa textual baseada nos dados do bairro, incluindo análise do Dijkstra.
     */
    private String gerarJustificativa(int ocorrenciasNoBairro, double tempoMedioResposta, int ambulanciasExistentes, 
                                     int bairrosAlcancaveis, int totalBairros) {
        List<String> razoes = new ArrayList<>();

        // Priorizar informação sobre alcance de conexões
        if (bairrosAlcancaveis > 0) {
            double percentualAlcance = (bairrosAlcancaveis * 100.0) / (totalBairros - 1);
            if (percentualAlcance >= 80) {
                razoes.add("excelente alcance: " + bairrosAlcancaveis + " de " + (totalBairros - 1) + 
                          " bairros alcançáveis via Dijkstra (" + String.format("%.0f", percentualAlcance) + 
                          "% de cobertura da rede viária)");
            } else if (percentualAlcance >= 60) {
                razoes.add("bom alcance: " + bairrosAlcancaveis + " de " + (totalBairros - 1) + 
                          " bairros alcançáveis através das conexões calculadas pelo Dijkstra (" + 
                          String.format("%.0f", percentualAlcance) + "% de cobertura)");
            } else {
                razoes.add("alcance de " + bairrosAlcancaveis + " bairros via conexões Dijkstra (" + 
                          String.format("%.0f", percentualAlcance) + "% de cobertura)");
            }
        }

        if (ocorrenciasNoBairro > 0) {
            razoes.add(ocorrenciasNoBairro + " ocorrência(s) registrada(s) no bairro");
        }

        if (tempoMedioResposta > 0) {
            if (tempoMedioResposta < 15) {
                razoes.add("excelente tempo médio de resposta calculado pelo Dijkstra: " + 
                          String.format("%.1f", tempoMedioResposta) + " min");
            } else if (tempoMedioResposta < 20) {
                razoes.add("bom tempo médio de resposta via Dijkstra: " + 
                          String.format("%.1f", tempoMedioResposta) + " min");
            } else {
                razoes.add("tempo médio de resposta: " + String.format("%.1f", tempoMedioResposta) + 
                          " min (calculado pelo algoritmo Dijkstra)");
            }
        }

        if (ambulanciasExistentes == 0) {
            razoes.add("sem ambulâncias no momento - posicionamento estratégico recomendado");
        } else if (ambulanciasExistentes == 1) {
            razoes.add("apenas 1 ambulância existente - pode necessitar reforço");
        } else {
            razoes.add(ambulanciasExistentes + " ambulância(s) já posicionadas");
        }

        if (razoes.isEmpty()) {
            return "Bairro estratégico para expansão da cobertura - análise baseada em Dijkstra mostra potencial de melhoria";
        }

        return "Análise Dijkstra: " + String.join("; ", razoes);
    }
}

