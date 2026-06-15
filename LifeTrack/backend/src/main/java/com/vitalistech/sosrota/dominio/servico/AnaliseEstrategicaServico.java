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
    private final com.vitalistech.sosrota.padroes.adapter.CalculadorDistanciaPort calculadorDistanciaPort;

    public AnaliseEstrategicaServico(BairroRepositorio bairroRepositorio,
                                     OcorrenciaRepositorio ocorrenciaRepositorio,
                                     AmbulanciaRepositorio ambulanciaRepositorio,
                                     RuaConexaoRepositorio ruaConexaoRepositorio,
                                     com.vitalistech.sosrota.padroes.adapter.CalculadorDistanciaPort calculadorDistanciaPort) {
        this.bairroRepositorio = bairroRepositorio;
        this.ocorrenciaRepositorio = ocorrenciaRepositorio;
        this.ambulanciaRepositorio = ambulanciaRepositorio;
        this.ruaConexaoRepositorio = ruaConexaoRepositorio;
        this.calculadorDistanciaPort = calculadorDistanciaPort;
    }

    /**
     * Retorna lista de bairros sugeridos para posicionamento de novas ambulâncias.
     * A análise considera:
     * - Número de ocorrências no bairro (por tipo: UTI vs Básica)
     * - Tempo médio de resposta para outros bairros com ocorrências
     * - Número de ambulâncias já existentes no bairro
     * - Distância mínima para outras ambulâncias (evita aglomeração)
     * - Tipo de ambulância sendo cadastrada (UTI vs Básica)
     * 
     * @param tipoAmbulancia Tipo de ambulância sendo cadastrada (opcional, null para análise geral)
     */
    public List<BairroSugeridoDTO> obterBairrosSugeridos(TipoAmbulancia tipoAmbulancia) {
        List<Bairro> todosBairros = bairroRepositorio.findAll();
        List<Ocorrencia> todasOcorrencias = ocorrenciaRepositorio.findAll();
        List<Ambulancia> todasAmbulancias = ambulanciaRepositorio.findAll()
                .stream()
                .filter(a -> a.isAtiva())
                .collect(Collectors.toList());
        List<RuaConexao> todasConexoes = ruaConexaoRepositorio.findAll();

        // Filtrar ocorrências por tipo necessário
        final List<Ocorrencia> ocorrenciasRelevantes;
        if (tipoAmbulancia != null) {
            if (tipoAmbulancia == TipoAmbulancia.UTI) {
                // UTI atende apenas ocorrências de gravidade ALTA
                ocorrenciasRelevantes = todasOcorrencias.stream()
                        .filter(o -> o.getGravidade() == Gravidade.ALTA)
                        .collect(Collectors.toList());
            } else {
                // Básica atende ocorrências de gravidade MÉDIA e BAIXA
                ocorrenciasRelevantes = todasOcorrencias.stream()
                        .filter(o -> o.getGravidade() == Gravidade.MEDIA || o.getGravidade() == Gravidade.BAIXA)
                        .collect(Collectors.toList());
            }
        } else {
            ocorrenciasRelevantes = todasOcorrencias;
        }

        // Agrupar ocorrências por bairro
        List<Bairro> bairrosComOcorrencias = ocorrenciasRelevantes.stream()
                .map(Ocorrencia::getBairroLocal)
                .distinct()
                .collect(Collectors.toList());

        List<BairroSugeridoDTO> sugestoes = new ArrayList<>();

        for (Bairro bairro : todosBairros) {
            // Contar ocorrências relevantes neste bairro
            int ocorrenciasNoBairro = (int) ocorrenciasRelevantes.stream()
                    .filter(o -> o.getBairroLocal().getId().equals(bairro.getId()))
                    .count();

            // Contar ambulâncias existentes neste bairro (por tipo se especificado)
            int ambulanciasExistentes = (int) todasAmbulancias.stream()
                    .filter(a -> a.getBairroBase().getId().equals(bairro.getId()))
                    .filter(a -> tipoAmbulancia == null || a.getTipo() == tipoAmbulancia)
                    .count();

            // Contar ambulâncias do mesmo tipo no bairro
            int ambulanciasMesmoTipo = (int) todasAmbulancias.stream()
                    .filter(a -> a.getBairroBase().getId().equals(bairro.getId()))
                    .filter(a -> tipoAmbulancia != null && a.getTipo() == tipoAmbulancia)
                    .count();

            // Calcular distância mínima para outras ambulâncias (evitar aglomeração)
            double distanciaMinimaProxima = calcularDistanciaMinimaProxima(bairro, todasAmbulancias, todasConexoes, tipoAmbulancia);

            // Calcular tempo médio de resposta para outros bairros com ocorrências relevantes
            double tempoMedioResposta = calcularTempoMedioResposta(bairro, bairrosComOcorrencias, todasConexoes);

            // Calcular quantas conexões diretas este bairro possui
            int conexoesDiretas = calcularConexoesDiretas(bairro, todasConexoes);

            // Calcular score de prioridade considerando todos os fatores
            double scorePrioridade = calcularScorePrioridade(
                    ocorrenciasNoBairro,
                    ambulanciasExistentes,
                    ambulanciasMesmoTipo,
                    distanciaMinimaProxima,
                    conexoesDiretas,
                    tempoMedioResposta,
                    tipoAmbulancia
            );

            // Gerar justificativa
            String justificativa = gerarJustificativa(
                    ocorrenciasNoBairro,
                    tempoMedioResposta,
                    ambulanciasExistentes,
                    ambulanciasMesmoTipo,
                    distanciaMinimaProxima,
                    conexoesDiretas,
                    todosBairros.size(),
                    tipoAmbulancia
            );

            BairroSugeridoDTO dto = new BairroSugeridoDTO(
                    bairro.getId(),
                    bairro.getNome(),
                    justificativa,
                    ocorrenciasNoBairro,
                    tempoMedioResposta,
                    ambulanciasExistentes,
                    conexoesDiretas
            );

            sugestoes.add(dto);
        }

        // Ordenar por score de prioridade (maior primeiro)
        return sugestoes.stream()
                .sorted(Comparator
                        .comparingDouble((BairroSugeridoDTO dto) -> {
                            // Recalcular score para ordenação
                            Bairro bairro = todosBairros.stream()
                                    .filter(b -> b.getId().equals(dto.getId()))
                                    .findFirst()
                                    .orElse(null);
                            if (bairro == null) return 0.0;
                            
                            int ocorrencias = (int) ocorrenciasRelevantes.stream()
                                    .filter(o -> o.getBairroLocal().getId().equals(bairro.getId()))
                                    .count();
                            
                            int ambExistentes = (int) todasAmbulancias.stream()
                                    .filter(a -> a.getBairroBase().getId().equals(bairro.getId()))
                                    .filter(a -> tipoAmbulancia == null || a.getTipo() == tipoAmbulancia)
                                    .count();
                            
                            int ambMesmoTipo = (int) todasAmbulancias.stream()
                                    .filter(a -> a.getBairroBase().getId().equals(bairro.getId()))
                                    .filter(a -> tipoAmbulancia != null && a.getTipo() == tipoAmbulancia)
                                    .count();
                            
                            double distMin = calcularDistanciaMinimaProxima(bairro, todasAmbulancias, todasConexoes, tipoAmbulancia);
                            int conexoes = calcularConexoesDiretas(bairro, todasConexoes);
                            double tempo = dto.getTempoMedioResposta();
                            
                            return calcularScorePrioridade(ocorrencias, ambExistentes, ambMesmoTipo, distMin, conexoes, tempo, tipoAmbulancia);
                        }).reversed()
                        .thenComparingInt((BairroSugeridoDTO dto) -> dto.getBairrosAlcancaveis()).reversed()
                        .thenComparingInt((BairroSugeridoDTO dto) -> dto.getOcorrenciasNoBairro()).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Sobrecarga para compatibilidade - análise geral sem tipo específico
     */
    public List<BairroSugeridoDTO> obterBairrosSugeridos() {
        return obterBairrosSugeridos(null);
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

            // Padrão Adapter
            ResultadoRota rota = calculadorDistanciaPort.calcularRota(bairroBase, destino, todasConexoes);
            
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
     * Calcula quantas conexões diretas um bairro possui.
     * Uma conexão direta é quando o bairro aparece como origem ou destino em uma conexão de rua.
     * Como o grafo é bidirecional, cada conexão conta apenas uma vez.
     */
    private int calcularConexoesDiretas(Bairro bairro, List<RuaConexao> todasConexoes) {
        int conexoes = 0;
        Long idBairro = bairro.getId();
        
        for (RuaConexao conexao : todasConexoes) {
            // Contar se o bairro é origem ou destino da conexão
            if (conexao.getBairroOrigem().getId().equals(idBairro) || 
                conexao.getBairroDestino().getId().equals(idBairro)) {
                conexoes++;
            }
        }
        
        return conexoes;
    }

    /**
     * Calcula a distância mínima para a ambulância mais próxima.
     * Usado para evitar aglomeração de ambulâncias no mesmo bairro ou muito próximas.
     * 
     * @param bairro Bairro sendo analisado
     * @param todasAmbulancias Lista de todas as ambulâncias ativas
     * @param todasConexoes Lista de todas as conexões viárias
     * @param tipoAmbulancia Tipo de ambulância sendo cadastrada (null para considerar todas)
     * @return Distância mínima em km, ou Double.POSITIVE_INFINITY se não houver ambulâncias próximas
     */
    private double calcularDistanciaMinimaProxima(Bairro bairro, List<Ambulancia> todasAmbulancias, 
                                                  List<RuaConexao> todasConexoes, TipoAmbulancia tipoAmbulancia) {
        double distanciaMinima = Double.POSITIVE_INFINITY;
        
        for (Ambulancia ambulancia : todasAmbulancias) {
            // Filtrar por tipo se especificado
            if (tipoAmbulancia != null && ambulancia.getTipo() != tipoAmbulancia) {
                continue;
            }
            
            Bairro bairroAmbulancia = ambulancia.getBairroBase();
            
            // Se for o mesmo bairro, distância é 0
            if (bairro.getId().equals(bairroAmbulancia.getId())) {
                return 0.0;
            }
            
            // Calcular distância usando Dijkstra
            // Padrão Adapter
            ResultadoRota rota = calculadorDistanciaPort.calcularRota(bairro, bairroAmbulancia, todasConexoes);
            
            if (!Double.isInfinite(rota.getDistanciaKm()) && rota.getDistanciaKm() < distanciaMinima) {
                distanciaMinima = rota.getDistanciaKm();
            }
        }
        
        return distanciaMinima;
    }

    /**
     * Calcula um score de prioridade para o bairro.
     * Score maior = maior prioridade.
     * 
     * Fatores considerados:
     * - Ocorrências relevantes (peso alto)
     * - Distância mínima para outras ambulâncias (evitar aglomeração)
     * - Conexões diretas (conectividade)
     * - Tempo médio de resposta (menor é melhor)
     * - Ambulâncias existentes (menos é melhor)
     */
    private double calcularScorePrioridade(int ocorrenciasNoBairro, int ambulanciasExistentes, 
                                          int ambulanciasMesmoTipo, double distanciaMinimaProxima,
                                          int conexoesDiretas, double tempoMedioResposta,
                                          TipoAmbulancia tipoAmbulancia) {
        double score = 0.0;
        
        // 1. Ocorrências relevantes (peso: 50 pontos por ocorrência)
        score += ocorrenciasNoBairro * 50.0;
        
        // 2. Distância mínima para outras ambulâncias (evitar aglomeração)
        // Penalizar se muito próximo (< 2km), bonificar se bem distribuído (> 5km)
        if (distanciaMinimaProxima < 2.0) {
            score -= 100.0; // Penalidade alta por aglomeração
        } else if (distanciaMinimaProxima > 5.0) {
            score += 30.0; // Bonificação por boa distribuição
        } else if (distanciaMinimaProxima == Double.POSITIVE_INFINITY) {
            // Sem ambulâncias próximas - ideal para distribuição
            score += 40.0;
        }
        
        // 3. Ambulâncias do mesmo tipo no bairro (penalizar aglomeração do mesmo tipo)
        if (ambulanciasMesmoTipo > 0) {
            score -= ambulanciasMesmoTipo * 30.0; // Penalidade por ter ambulâncias do mesmo tipo
        }
        
        // 4. Ambulâncias existentes no bairro (menos é melhor)
        if (ambulanciasExistentes == 0) {
            score += 20.0; // Bonificação por bairro sem cobertura
        } else {
            score -= ambulanciasExistentes * 10.0; // Penalidade por já ter ambulâncias
        }
        
        // 5. Conexões diretas (conectividade)
        score += conexoesDiretas * 5.0;
        
        // 6. Tempo médio de resposta (menor é melhor)
        if (tempoMedioResposta > 0) {
            if (tempoMedioResposta < 15) {
                score += 15.0; // Excelente tempo
            } else if (tempoMedioResposta < 20) {
                score += 10.0; // Bom tempo
            } else {
                score -= 5.0; // Tempo pode ser melhorado
            }
        }
        
        return score;
    }

    /**
     * Gera uma justificativa textual baseada nos dados do bairro, incluindo análise do Dijkstra.
     * Focado em informações diretas: critérios e estratégia.
     */
    private String gerarJustificativa(int ocorrenciasNoBairro, double tempoMedioResposta, int ambulanciasExistentes,
                                     int ambulanciasMesmoTipo, double distanciaMinimaProxima, int conexoesDiretas, 
                                     int totalBairros, TipoAmbulancia tipoAmbulancia) {
        List<String> criterios = new ArrayList<>();
        
        if (ambulanciasExistentes == 0) {
            criterios.add("Sem cobertura");
        } else {
            if (tipoAmbulancia != null && ambulanciasMesmoTipo > 0) {
                criterios.add("Aglomeração do tipo " + tipoAmbulancia.name());
            } else {
                criterios.add("Já possui cobertura");
            }
        }
        
        if (distanciaMinimaProxima < 2.0) {
            criterios.add("Risco de aglomeração");
        } else if (distanciaMinimaProxima > 5.0 && distanciaMinimaProxima != Double.POSITIVE_INFINITY) {
            criterios.add("Boa distribuição");
        } else if (distanciaMinimaProxima == Double.POSITIVE_INFINITY) {
            criterios.add("Distribuição ideal");
        }
        
        if (tempoMedioResposta > 0 && tempoMedioResposta < 15) {
            criterios.add("Tempo resposta rápido");
        }

        String txtEstrategia = "Expansão de malha e melhoria de atendimento regional";
        if (conexoesDiretas >= 5) {
            txtEstrategia = "Alta conectividade. Posicionamento central para acesso rápido a múltiplos bairros";
        } else if (conexoesDiretas <= 2 && conexoesDiretas > 0) {
            txtEstrategia = "Ponto crítico na rede. Evita gargalos e isolamento da área";
        } else if (ambulanciasExistentes == 0) {
            txtEstrategia = "Preenchimento de lacuna de cobertura e distribuição eficiente";
        }

        String txtCriterios = criterios.isEmpty() ? "Melhoria de rede" : String.join(" • ", criterios);
        return String.format("📊 Critérios: %s | 💡 Estratégia: %s", txtCriterios, txtEstrategia);
    }
}

