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
    private final ProfissionalRepositorio profissionalRepositorio;
    private final AtendimentoRotaConexaoRepositorio atendimentoRotaConexaoRepositorio;
    private final HistoricoOcorrenciaServico historicoOcorrenciaServico;

    public OcorrenciaServico(OcorrenciaRepositorio ocorrenciaRepositorio,
                             AmbulanciaRepositorio ambulanciaRepositorio,
                             AtendimentoRepositorio atendimentoRepositorio,
                             RuaConexaoRepositorio ruaConexaoRepositorio,
                             EquipeRepositorio equipeRepositorio,
                             ProfissionalRepositorio profissionalRepositorio,
                             AtendimentoRotaConexaoRepositorio atendimentoRotaConexaoRepositorio,
                             HistoricoOcorrenciaServico historicoOcorrenciaServico) {
        this.ocorrenciaRepositorio = ocorrenciaRepositorio;
        this.ambulanciaRepositorio = ambulanciaRepositorio;
        this.atendimentoRepositorio = atendimentoRepositorio;
        this.ruaConexaoRepositorio = ruaConexaoRepositorio;
        this.equipeRepositorio = equipeRepositorio;
        this.profissionalRepositorio = profissionalRepositorio;
        this.atendimentoRotaConexaoRepositorio = atendimentoRotaConexaoRepositorio;
        this.historicoOcorrenciaServico = historicoOcorrenciaServico;
    }

    @Transactional
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
        
        // Definir SLA baseado na gravidade
        ocorrencia.setSlaMinutos(calcularSlaPorGravidade(gravidade));

        ocorrencia = ocorrenciaRepositorio.save(ocorrencia);

        // Registrar no histórico se houver usuário
        if (usuarioRegistro != null) {
            try {
                historicoOcorrenciaServico.registrarAbertura(ocorrencia, usuarioRegistro);
            } catch (Exception e) {
                // Log do erro mas não interrompe o fluxo
                // O histórico é importante mas não deve impedir a criação da ocorrência
                System.err.println("Erro ao registrar histórico de abertura: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ocorrencia;
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

        // Registrar no histórico se houver usuário
        if (usuarioDespacho != null) {
            try {
                String detalhesDespacho = String.format(
                    "Ambulância: %s (%s) - Distância: %.2f km",
                    melhorAmbulancia.getPlaca(),
                    melhorAmbulancia.getTipo().name(),
                    menorDistancia
                );
                historicoOcorrenciaServico.registrarDespacho(ocorrencia, usuarioDespacho, detalhesDespacho);
            } catch (Exception e) {
                // Log do erro mas não interrompe o fluxo
                System.err.println("Erro ao registrar histórico de despacho: " + e.getMessage());
                e.printStackTrace();
            }
        }

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
        int slaMinutos = ocorrencia.getSlaMinutos() != null 
                ? ocorrencia.getSlaMinutos() 
                : calcularSlaPorGravidade(ocorrencia.getGravidade());

        if (ocorrencia.getGravidade() == Gravidade.ALTA) {
            tipoNecessario = TipoAmbulancia.UTI;
        } else {
            tipoNecessario = TipoAmbulancia.BASICA;
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

    /**
     * Calcula o SLA em minutos baseado na gravidade da ocorrência.
     * ALTA = 8 minutos, MÉDIA = 15 minutos, BAIXA = 30 minutos.
     */
    private int calcularSlaPorGravidade(Gravidade gravidade) {
        if (gravidade == Gravidade.ALTA) {
            return 8;
        } else if (gravidade == Gravidade.MEDIA) {
            return 15;
        } else {
            return 30; // BAIXA
        }
    }

    /**
     * Conclui uma ocorrência, calculando o tempo de atendimento e verificando se o SLA foi cumprido.
     * 
     * @param idOcorrencia ID da ocorrência a ser concluída
     * @param usuarioConclusao Usuário que está concluindo a ocorrência
     * @return Ocorrência concluída com dados de SLA preenchidos
     */
    @Transactional
    public Ocorrencia concluirOcorrencia(Long idOcorrencia, Usuario usuarioConclusao) {
        Ocorrencia ocorrencia = ocorrenciaRepositorio.findById(idOcorrencia)
                .orElseThrow(() -> new IllegalArgumentException("Ocorrência não encontrada"));

        if (ocorrencia.getStatus() == StatusOcorrencia.CONCLUIDA) {
            throw new IllegalStateException("Ocorrência já está concluída");
        }

        if (ocorrencia.getStatus() == StatusOcorrencia.CANCELADA) {
            throw new IllegalStateException("Ocorrência cancelada não pode ser concluída");
        }

        // Capturar status anterior antes de alterar
        StatusOcorrencia statusAnterior = ocorrencia.getStatus();
        
        LocalDateTime agora = LocalDateTime.now();
        ocorrencia.setDataHoraFechamento(agora);
        ocorrencia.setStatusOcorrencia(StatusOcorrencia.CONCLUIDA);

        // Calcular tempo de atendimento em minutos
        LocalDateTime dataAbertura = ocorrencia.getDataHoraAbertura();
        if (dataAbertura != null) {
            long minutos = java.time.Duration.between(dataAbertura, agora).toMinutes();
            ocorrencia.setTempoAtendimentoMinutos((int) minutos);
        }

        // Garantir que SLA está definido
        if (ocorrencia.getSlaMinutos() == null) {
            ocorrencia.setSlaMinutos(calcularSlaPorGravidade(ocorrencia.getGravidade()));
        }

        // Verificar se SLA foi cumprido
        if (ocorrencia.getTempoAtendimentoMinutos() != null && ocorrencia.getSlaMinutos() != null) {
            ocorrencia.setSlaCumprido(ocorrencia.getTempoAtendimentoMinutos() <= ocorrencia.getSlaMinutos());
        } else {
            ocorrencia.setSlaCumprido(false);
        }

        ocorrencia = ocorrenciaRepositorio.save(ocorrencia);

        // Registrar no histórico
        if (usuarioConclusao != null) {
            try {
                String descricao = String.format(
                    "Ocorrência concluída. Tempo de atendimento: %d minutos. SLA: %d minutos. %s",
                    ocorrencia.getTempoAtendimentoMinutos() != null ? ocorrencia.getTempoAtendimentoMinutos() : 0,
                    ocorrencia.getSlaMinutos() != null ? ocorrencia.getSlaMinutos() : 0,
                    ocorrencia.getSlaCumprido() != null && ocorrencia.getSlaCumprido() 
                        ? "SLA CUMPRIDO" 
                        : "SLA NÃO CUMPRIDO"
                );
                
                historicoOcorrenciaServico.registrarAcao(
                    ocorrencia,
                    usuarioConclusao,
                    AcaoHistorico.CONCLUSAO,
                    statusAnterior,
                    StatusOcorrencia.CONCLUIDA,
                    descricao
                );
            } catch (Exception e) {
                System.err.println("Erro ao registrar histórico de conclusão: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ocorrencia;
    }

    /**
     * Registra a chegada da ambulância ao local e fecha automaticamente a ocorrência.
     * Calcula tempo de atendimento, verifica SLA e registra quanto excedeu (se houver).
     * 
     * @param idAtendimento ID do atendimento
     * @param usuarioChegada Usuário que está registrando a chegada (pode ser null)
     * @return Ocorrência fechada com dados de SLA calculados
     */
    @Transactional
    public Ocorrencia registrarChegadaEFechar(Long idAtendimento, Usuario usuarioChegada) {
        Atendimento atendimento = atendimentoRepositorio.findById(idAtendimento)
                .orElseThrow(() -> new IllegalArgumentException("Atendimento não encontrado"));

        if (atendimento.getDataHoraChegada() != null) {
            throw new IllegalStateException("Chegada já foi registrada para este atendimento");
        }

        Ocorrencia ocorrencia = atendimento.getOcorrencia();
        if (ocorrencia == null) {
            throw new IllegalStateException("Atendimento não possui ocorrência associada");
        }

        if (ocorrencia.getStatus() == StatusOcorrencia.CONCLUIDA) {
            throw new IllegalStateException("Ocorrência já está concluída");
        }

        if (ocorrencia.getStatus() == StatusOcorrencia.CANCELADA) {
            throw new IllegalStateException("Ocorrência cancelada não pode ser fechada");
        }

        // Capturar status anterior
        StatusOcorrencia statusAnterior = ocorrencia.getStatus();

        // Registrar chegada
        LocalDateTime agora = LocalDateTime.now();
        atendimento.setDataHoraChegada(agora);
        atendimentoRepositorio.save(atendimento);

        // Fechar ocorrência
        ocorrencia.setDataHoraFechamento(agora);
        ocorrencia.setStatusOcorrencia(StatusOcorrencia.CONCLUIDA);

        // Calcular tempo de atendimento em minutos (da abertura até a chegada)
        LocalDateTime dataAbertura = ocorrencia.getDataHoraAbertura();
        if (dataAbertura != null) {
            long minutos = java.time.Duration.between(dataAbertura, agora).toMinutes();
            ocorrencia.setTempoAtendimentoMinutos((int) minutos);
        }

        // Garantir que SLA está definido
        if (ocorrencia.getSlaMinutos() == null) {
            ocorrencia.setSlaMinutos(calcularSlaPorGravidade(ocorrencia.getGravidade()));
        }

        // Verificar se SLA foi cumprido e calcular tempo excedido
        if (ocorrencia.getTempoAtendimentoMinutos() != null && ocorrencia.getSlaMinutos() != null) {
            int tempoAtendimento = ocorrencia.getTempoAtendimentoMinutos();
            int slaMinutos = ocorrencia.getSlaMinutos();
            
            if (tempoAtendimento <= slaMinutos) {
                // SLA cumprido
                ocorrencia.setSlaCumprido(true);
                ocorrencia.setTempoExcedidoMinutos(null); // null indica que não excedeu
            } else {
                // SLA não cumprido - calcular quanto excedeu
                ocorrencia.setSlaCumprido(false);
                ocorrencia.setTempoExcedidoMinutos(tempoAtendimento - slaMinutos);
            }
        } else {
            ocorrencia.setSlaCumprido(false);
            ocorrencia.setTempoExcedidoMinutos(null);
        }

        ocorrencia = ocorrenciaRepositorio.save(ocorrencia);

        // Registrar no histórico
        if (usuarioChegada != null) {
            try {
                String descricao;
                if (ocorrencia.getSlaCumprido() != null && ocorrencia.getSlaCumprido()) {
                    descricao = String.format(
                        "Ambulância chegou ao local. Ocorrência fechada automaticamente. " +
                        "Tempo de atendimento: %d minutos. SLA: %d minutos. SLA CUMPRIDO.",
                        ocorrencia.getTempoAtendimentoMinutos() != null ? ocorrencia.getTempoAtendimentoMinutos() : 0,
                        ocorrencia.getSlaMinutos() != null ? ocorrencia.getSlaMinutos() : 0
                    );
                } else {
                    descricao = String.format(
                        "Ambulância chegou ao local. Ocorrência fechada automaticamente. " +
                        "Tempo de atendimento: %d minutos. SLA: %d minutos. SLA NÃO CUMPRIDO. " +
                        "Tempo excedido: %d minutos.",
                        ocorrencia.getTempoAtendimentoMinutos() != null ? ocorrencia.getTempoAtendimentoMinutos() : 0,
                        ocorrencia.getSlaMinutos() != null ? ocorrencia.getSlaMinutos() : 0,
                        ocorrencia.getTempoExcedidoMinutos() != null ? ocorrencia.getTempoExcedidoMinutos() : 0
                    );
                }
                
                historicoOcorrenciaServico.registrarAcao(
                    ocorrencia,
                    usuarioChegada,
                    AcaoHistorico.CONCLUSAO,
                    statusAnterior,
                    StatusOcorrencia.CONCLUIDA,
                    descricao
                );
            } catch (Exception e) {
                System.err.println("Erro ao registrar histórico de chegada: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ocorrencia;
    }
}
