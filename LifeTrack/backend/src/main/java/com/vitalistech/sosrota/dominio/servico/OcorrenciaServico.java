package com.vitalistech.sosrota.dominio.servico;

import com.vitalistech.sosrota.dominio.modelo.*;
import com.vitalistech.sosrota.dominio.repositorio.*;
import com.vitalistech.sosrota.dominio.modelo.AtendimentoRotaConexao;
import com.vitalistech.sosrota.util.AlgoritmoDijkstra;
import com.vitalistech.sosrota.util.ResultadoRota;
import com.vitalistech.sosrota.web.dto.AmbulanciaSugeridaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável por registrar ocorrências e despachar ambulâncias.
 */
@Service
public class OcorrenciaServico {

    private final OcorrenciaRepositorio ocorrenciaRepositorio;
    private final AmbulanciaRepositorio ambulanciaRepositorio;
    private final AtendimentoRepositorio atendimentoRepositorio;
    private final RuaConexaoRepositorio ruaConexaoRepositorio;
    private final EquipeRepositorio equipeRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ProfissionalRepositorio profissionalRepositorio;
    private final AtendimentoRotaConexaoRepositorio atendimentoRotaConexaoRepositorio;

    public OcorrenciaServico(OcorrenciaRepositorio ocorrenciaRepositorio,
                             AmbulanciaRepositorio ambulanciaRepositorio,
                             AtendimentoRepositorio atendimentoRepositorio,
                             RuaConexaoRepositorio ruaConexaoRepositorio,
                             EquipeRepositorio equipeRepositorio,
                             UsuarioRepositorio usuarioRepositorio,
                             ProfissionalRepositorio profissionalRepositorio,
                             AtendimentoRotaConexaoRepositorio atendimentoRotaConexaoRepositorio) {
        this.ocorrenciaRepositorio = ocorrenciaRepositorio;
        this.ambulanciaRepositorio = ambulanciaRepositorio;
        this.atendimentoRepositorio = atendimentoRepositorio;
        this.ruaConexaoRepositorio = ruaConexaoRepositorio;
        this.equipeRepositorio = equipeRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.profissionalRepositorio = profissionalRepositorio;
        this.atendimentoRotaConexaoRepositorio = atendimentoRotaConexaoRepositorio;
    }

    public Ocorrencia registrarOcorrencia(Bairro bairroLocal,
                                          String tipoOcorrencia,
                                          Gravidade gravidade,
                                          String observacoes,
                                          Usuario usuarioRegistro) {

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setBairroLocal(bairroLocal);
        ocorrencia.setTipoOcorrencia(tipoOcorrencia);
        ocorrencia.setGravidade(gravidade);
        ocorrencia.setObservacoes(observacoes);
        // Preencher descrição (campo obrigatório no banco)
        // Usar tipoOcorrencia como descrição, ou combinar tipo + observações
        String descricao = tipoOcorrencia;
        if (observacoes != null && !observacoes.trim().isEmpty()) {
            descricao += " - " + observacoes;
        }
        ocorrencia.setDescricao(descricao);
        ocorrencia.setDataHoraAbertura(LocalDateTime.now());
        ocorrencia.setStatusOcorrencia(StatusOcorrencia.ABERTA);
        ocorrencia.setUsuarioRegistro(usuarioRegistro);

        return ocorrenciaRepositorio.save(ocorrencia);
    }

    /**
     * Despacha uma ambulância para a ocorrência considerando regras de SLA e equipe.
     */
    @Transactional
    public Atendimento despacharOcorrencia(Long idOcorrencia, Usuario usuarioDespacho) {

        Ocorrencia ocorrencia = ocorrenciaRepositorio.findById(idOcorrencia)
                .orElseThrow(() -> new IllegalArgumentException("Ocorrência não encontrada"));

        if (ocorrencia.getStatus() != StatusOcorrencia.ABERTA) {
            throw new IllegalStateException("Somente ocorrências ABERTAS podem ser despachadas");
        }

        TipoAmbulancia tipoNecessario;
        int slaMinutos;

        if (ocorrencia.getGravidade() == Gravidade.ALTA) {
            tipoNecessario = TipoAmbulancia.UTI;
            slaMinutos = 8;
        } else if (ocorrencia.getGravidade() == Gravidade.MEDIA) {
            tipoNecessario = TipoAmbulancia.BASICA;
            slaMinutos = 15;
        } else {
            tipoNecessario = TipoAmbulancia.BASICA;
            slaMinutos = 30;
        }

        List<Ambulancia> ambulanciasDisponiveis =
                ambulanciaRepositorio.findByStatusAndAtivaTrue(StatusAmbulancia.DISPONIVEL);

        Ambulancia melhorAmbulancia = null;
        double menorDistancia = Double.POSITIVE_INFINITY;
        ResultadoRota melhorRota = null;

        List<RuaConexao> todasConexoes = ruaConexaoRepositorio.findAll();

        for (Ambulancia a : ambulanciasDisponiveis) {

            if (!tipoCompativel(a.getTipo(), tipoNecessario)) {
                continue;
            }

            if (!ambulanciaPossuiEquipeCompleta(a)) {
                continue;
            }

            ResultadoRota rota = AlgoritmoDijkstra.calcularRota(
                    a.getBairroBase(),
                    ocorrencia.getBairroLocal(),
                    todasConexoes
            );

            double distKm = rota.getDistanciaKm();
            if (Double.isInfinite(distKm)) continue;

            // Calcular tempo estimado (velocidade de 60 km/h conforme documento)
            int tempoEstimadoMinutos = (int) Math.ceil((distKm / 60.0) * 60);

            // Verificar se está dentro do SLA e é a mais próxima
            if (tempoEstimadoMinutos <= slaMinutos && distKm < menorDistancia) {
                menorDistancia = distKm;
                melhorAmbulancia = a;
                melhorRota = rota;
            }
        }

        if (melhorAmbulancia == null) {
            throw new IllegalStateException("Nenhuma ambulância apta encontrada dentro do SLA.");
        }

        melhorAmbulancia.setStatus(StatusAmbulancia.EM_ATENDIMENTO);
        ambulanciaRepositorio.save(melhorAmbulancia);

        // Marcar todos os profissionais da equipe como EM_ATENDIMENTO
        Equipe equipe = equipeRepositorio.findByAmbulanciaAndAtivaTrue(melhorAmbulancia).orElse(null);
        if (equipe != null) {
            for (EquipeProfissional ep : equipe.getProfissionais()) {
                Profissional profissional = ep.getProfissional();
                profissional.setStatus(StatusProfissional.EM_ATENDIMENTO);
                profissionalRepositorio.save(profissional);
            }
        }

        ocorrencia.setStatusOcorrencia(StatusOcorrencia.DESPACHADA);
        ocorrenciaRepositorio.save(ocorrencia);

        Atendimento atendimento = new Atendimento();
        atendimento.setOcorrencia(ocorrencia);
        atendimento.setAmbulancia(melhorAmbulancia);
        atendimento.setDataHoraDespacho(LocalDateTime.now());
        atendimento.setDistanciaKm(menorDistancia);
        atendimento.setUsuarioDespacho(usuarioDespacho);

        atendimento = atendimentoRepositorio.save(atendimento);

        // Salvar o caminho calculado pelo Dijkstra (conexões de rua utilizadas)
        if (melhorRota != null && melhorRota.getCaminho() != null && melhorRota.getCaminho().size() > 1) {
            salvarCaminhoCalculado(atendimento, melhorRota.getCaminho(), todasConexoes);
        }

        return atendimento;
    }

    /**
     * Verifica se o tipo de ambulância é compatível com o tipo necessário.
     * REGRA IMPORTANTE: Ocorrências BAIXAS nunca podem ser atendidas por UTI,
     * pois a qualquer momento pode surgir uma chamada mais importante.
     * 
     * @param tipoAmbulancia Tipo da ambulância disponível
     * @param tipoNecessario Tipo necessário baseado na gravidade
     * @return true se compatível, false caso contrário
     */
    private boolean tipoCompativel(TipoAmbulancia tipoAmbulancia, TipoAmbulancia tipoNecessario) {
        // Se precisa de UTI, só aceita UTI
        if (tipoNecessario == TipoAmbulancia.UTI) {
            return tipoAmbulancia == TipoAmbulancia.UTI;
        }
        
        // Se precisa de BÁSICA (BAIXA ou MÉDIA), só aceita BÁSICA
        // NUNCA aceita UTI para não desperdiçar recurso crítico
        return tipoAmbulancia == TipoAmbulancia.BASICA;
    }

    /**
     * Sugere ambulâncias aptas para uma ocorrência, calculando rotas com Dijkstra.
     * Retorna lista ordenada por distância, incluindo apenas ambulâncias que atendem aos critérios.
     */
    public List<AmbulanciaSugeridaDTO> sugerirAmbulancias(Long idOcorrencia) {
        Ocorrencia ocorrencia = ocorrenciaRepositorio.findById(idOcorrencia)
                .orElseThrow(() -> new IllegalArgumentException("Ocorrência não encontrada"));

        if (ocorrencia.getStatus() != StatusOcorrencia.ABERTA) {
            throw new IllegalStateException("Somente ocorrências ABERTAS podem ter ambulâncias sugeridas");
        }

        // Calcular SLA baseado na gravidade
        TipoAmbulancia tipoNecessario;
        int slaMinutos;

        if (ocorrencia.getGravidade() == Gravidade.ALTA) {
            tipoNecessario = TipoAmbulancia.UTI;
            slaMinutos = 8;
        } else if (ocorrencia.getGravidade() == Gravidade.MEDIA) {
            tipoNecessario = TipoAmbulancia.BASICA;
            slaMinutos = 15;
        } else {
            tipoNecessario = TipoAmbulancia.BASICA;
            slaMinutos = 30;
        }

        // Buscar ambulâncias disponíveis
        List<Ambulancia> ambulanciasDisponiveis =
                ambulanciaRepositorio.findByStatusAndAtivaTrue(StatusAmbulancia.DISPONIVEL);

        // Buscar todas as conexões para o Dijkstra
        List<RuaConexao> todasConexoes = ruaConexaoRepositorio.findAll();

        List<AmbulanciaSugeridaDTO> sugestoes = new ArrayList<>();

        // Para cada ambulância disponível, calcular rota e verificar critérios
        for (Ambulancia ambulancia : ambulanciasDisponiveis) {
            // Verificar tipo compatível
            if (!tipoCompativel(ambulancia.getTipo(), tipoNecessario)) {
                continue;
            }

            // Verificar equipe completa
            boolean equipeCompleta = ambulanciaPossuiEquipeCompleta(ambulancia);
            String statusEquipe = equipeCompleta ? "Completa" : "Incompleta";

            if (!equipeCompleta) {
                continue; // Só sugerir ambulâncias com equipe completa
            }

            // Calcular rota usando Dijkstra
            ResultadoRota rota = AlgoritmoDijkstra.calcularRota(
                    ambulancia.getBairroBase(),
                    ocorrencia.getBairroLocal(),
                    todasConexoes
            );

            double distanciaKm = rota.getDistanciaKm();
            if (Double.isInfinite(distanciaKm)) {
                continue; // Sem caminho disponível
            }

            // Calcular tempo estimado (velocidade de 60 km/h conforme documento)
            // Tempo em horas = distanciaKm / 60, então tempo em minutos = (distanciaKm / 60) * 60
            int tempoEstimadoMinutos = (int) Math.ceil((distanciaKm / 60.0) * 60);

            // Verificar se está dentro do SLA
            boolean dentroSLA = tempoEstimadoMinutos <= slaMinutos;

            // Criar DTO com informações da ambulância sugerida
            AmbulanciaSugeridaDTO dto = new AmbulanciaSugeridaDTO(
                    ambulancia.getId(),
                    ambulancia.getPlaca(),
                    ambulancia.getTipo().name(),
                    ambulancia.getBairroBase().getNome(),
                    Math.round(distanciaKm * 100.0) / 100.0, // Arredondar para 2 casas decimais
                    tempoEstimadoMinutos,
                    dentroSLA,
                    equipeCompleta,
                    statusEquipe,
                    slaMinutos
            );

            sugestoes.add(dto);
        }

        // Ordenar por distância (menor primeiro)
        return sugestoes.stream()
                .sorted(Comparator.comparingDouble(AmbulanciaSugeridaDTO::getDistanciaKm))
                .collect(Collectors.toList());
    }

    private boolean ambulanciaPossuiEquipeCompleta(Ambulancia ambulancia) {
        Equipe equipe = equipeRepositorio.findByAmbulanciaAndAtivaTrue(ambulancia).orElse(null);
        if (equipe == null) return false;

        boolean condutor = false;
        boolean enfermeiro = false;
        boolean medico = false;

        for (EquipeProfissional ep : equipe.getProfissionais()) {
            FuncaoProfissional f = ep.getProfissional().getFuncao();
            if (f == FuncaoProfissional.CONDUTOR) condutor = true;
            if (f == FuncaoProfissional.ENFERMEIRO) enfermeiro = true;
            if (f == FuncaoProfissional.MEDICO) medico = true;
        }

        if (ambulancia.getTipo() == TipoAmbulancia.UTI) {
            return condutor && enfermeiro && medico;
        } else {
            return condutor && enfermeiro;
        }
    }

    /**
     * Salva o caminho calculado pelo Dijkstra na tabela atendimento_rota_conexao.
     * Converte a sequência de bairros em conexões de rua utilizadas.
     */
    private void salvarCaminhoCalculado(Atendimento atendimento, 
                                        List<Bairro> caminho, 
                                        List<RuaConexao> todasConexoes) {
        // Criar um mapa para busca rápida de conexões
        java.util.Map<String, RuaConexao> mapaConexoes = new java.util.HashMap<>();
        for (RuaConexao conexao : todasConexoes) {
            // Chave bidirecional: origem-destino e destino-origem
            String chave1 = conexao.getBairroOrigem().getId() + "-" + conexao.getBairroDestino().getId();
            String chave2 = conexao.getBairroDestino().getId() + "-" + conexao.getBairroOrigem().getId();
            mapaConexoes.put(chave1, conexao);
            mapaConexoes.put(chave2, conexao);
        }

        // Converter caminho de bairros em conexões de rua
        int ordem = 1;
        for (int i = 0; i < caminho.size() - 1; i++) {
            Bairro origem = caminho.get(i);
            Bairro destino = caminho.get(i + 1);
            
            String chave = origem.getId() + "-" + destino.getId();
            RuaConexao conexao = mapaConexoes.get(chave);
            
            if (conexao != null) {
                AtendimentoRotaConexao rotaConexao = new AtendimentoRotaConexao();
                rotaConexao.setAtendimento(atendimento);
                rotaConexao.setRuaConexao(conexao);
                rotaConexao.setOrdem(ordem);
                atendimentoRotaConexaoRepositorio.save(rotaConexao);
                ordem++;
            }
        }
    }
}
