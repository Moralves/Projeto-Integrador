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
     * Despacha uma ambulância para a ocorrência. Escolhe a ambulância mais próxima
     * disponível com equipe completa, independente do SLA. O SLA será verificado
     * posteriormente no relatório.
     * 
     * Regras:
     * - Ambulância deve estar DISPONIVEL (só faz um atendimento por vez)
     * - Equipe deve estar completa (só faz um atendimento por vez)
     * - Profissionais só podem estar em uma equipe por vez (já validado na criação da equipe)
     * - Escolhe a ambulância mais próxima, mesmo que o SLA esteja em risco
     */
    @Transactional
    public Atendimento despacharOcorrencia(Long idOcorrencia, Usuario usuarioDespacho) {

        Ocorrencia ocorrencia = ocorrenciaRepositorio.findById(idOcorrencia)
                .orElseThrow(() -> new IllegalArgumentException("Ocorrência não encontrada"));

        if (ocorrencia.getStatus() != StatusOcorrencia.ABERTA) {
            throw new IllegalStateException("Somente ocorrências ABERTAS podem ser despachadas");
        }

        // Determinar tipo de ambulância necessário baseado na gravidade
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

        // Buscar apenas ambulâncias DISPONIVEIS (garante que só faz um atendimento por vez)
        List<Ambulancia> ambulanciasDisponiveis =
                ambulanciaRepositorio.findByStatusAndAtivaTrue(StatusAmbulancia.DISPONIVEL);

        Ambulancia melhorAmbulancia = null;
        double menorDistancia = Double.POSITIVE_INFINITY;
        ResultadoRota melhorRota = null;

        List<RuaConexao> todasConexoes = ruaConexaoRepositorio.findAll();

        // Encontrar a ambulância mais próxima que atende aos critérios
        for (Ambulancia a : ambulanciasDisponiveis) {

            // Verificar tipo compatível
            if (!tipoCompativel(a.getTipo(), tipoNecessario)) {
                continue;
            }

            // Verificar equipe completa (garante que equipe só faz um atendimento por vez)
            if (!ambulanciaPossuiEquipeCompleta(a)) {
                continue;
            }

            // Calcular rota usando Dijkstra
            ResultadoRota rota = AlgoritmoDijkstra.calcularRota(
                    a.getBairroBase(),
                    ocorrencia.getBairroLocal(),
                    todasConexoes
            );

            double distKm = rota.getDistanciaKm();
            if (Double.isInfinite(distKm)) continue;

            // Escolher a ambulância mais próxima (SEM verificar SLA)
            if (distKm < menorDistancia) {
                menorDistancia = distKm;
                melhorAmbulancia = a;
                melhorRota = rota;
            }
        }

        if (melhorAmbulancia == null) {
            throw new IllegalStateException("Nenhuma ambulância apta encontrada. Verifique se há ambulâncias disponíveis com equipe completa do tipo necessário.");
        }

        // Marcar ambulância como EM_ATENDIMENTO (só pode fazer um atendimento por vez)
        melhorAmbulancia.setStatus(StatusAmbulancia.EM_ATENDIMENTO);
        ambulanciaRepositorio.save(melhorAmbulancia);

        // Buscar a equipe associada à ambulância (obrigatória para atendimento)
        Equipe equipe = equipeRepositorio.findByAmbulanciaAndAtivaTrue(melhorAmbulancia)
                .orElseThrow(() -> new IllegalStateException("Ambulância não possui equipe ativa associada. É necessário criar uma equipe antes de despachar."));

        // Marcar todos os profissionais da equipe como EM_ATENDIMENTO
        // Isso garante que equipe e profissionais só fazem um atendimento por vez
        for (EquipeProfissional ep : equipe.getProfissionais()) {
            Profissional profissional = ep.getProfissional();
            profissional.setStatus(StatusProfissional.EM_ATENDIMENTO);
            profissionalRepositorio.save(profissional);
        }

        // Salvar SLA na ocorrência para verificação posterior no relatório (se ainda não foi definido)
        if (ocorrencia.getSlaMinutos() == null) {
            ocorrencia.setSlaMinutos(slaMinutos);
        }
        
        ocorrencia.setStatusOcorrencia(StatusOcorrencia.DESPACHADA);
        ocorrenciaRepositorio.save(ocorrencia);

        Atendimento atendimento = new Atendimento();
        atendimento.setOcorrencia(ocorrencia);
        atendimento.setAmbulancia(melhorAmbulancia);
        atendimento.setEquipe(equipe); // Equipe é obrigatória - garante rastreabilidade
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
                historicoOcorrenciaServico.registrarDespacho(ocorrencia, usuarioDespacho, detalhesDespacho, melhorAmbulancia.getPlaca());
            } catch (Exception e) {
                // Log do erro mas não interrompe o fluxo
                System.err.println("Erro ao registrar histórico de despacho: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Salvar o caminho calculado pelo Dijkstra (conexões de rua utilizadas)
        if (melhorRota != null && melhorRota.getCaminho() != null && melhorRota.getCaminho().size() > 1) {
            try {
                salvarCaminhoCalculado(atendimento, melhorRota.getCaminho(), todasConexoes);
            } catch (Exception e) {
                // Log do erro mas não interrompe o fluxo
                System.err.println("Erro ao salvar caminho calculado: " + e.getMessage());
                e.printStackTrace();
            }
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
     * Cancela uma ocorrência. Só pode ser cancelada se ainda estiver ABERTA (não despachada).
     * 
     * @param idOcorrencia ID da ocorrência a ser cancelada
     * @param usuarioCancelamento Usuário que está cancelando a ocorrência
     * @return Ocorrência cancelada
     */
    @Transactional
    public Ocorrencia cancelarOcorrencia(Long idOcorrencia, Usuario usuarioCancelamento) {
        Ocorrencia ocorrencia = ocorrenciaRepositorio.findById(idOcorrencia)
                .orElseThrow(() -> new IllegalArgumentException("Ocorrência não encontrada"));

        if (ocorrencia.getStatus() == StatusOcorrencia.CANCELADA) {
            throw new IllegalStateException("Ocorrência já está cancelada");
        }

        if (ocorrencia.getStatus() == StatusOcorrencia.CONCLUIDA) {
            throw new IllegalStateException("Ocorrência concluída não pode ser cancelada");
        }

        if (ocorrencia.getStatus() != StatusOcorrencia.ABERTA) {
            throw new IllegalStateException("Apenas ocorrências ABERTAS podem ser canceladas. Status atual: " + ocorrencia.getStatus());
        }

        // Capturar status anterior
        StatusOcorrencia statusAnterior = ocorrencia.getStatus();
        
        // Cancelar ocorrência
        ocorrencia.setStatusOcorrencia(StatusOcorrencia.CANCELADA);
        ocorrencia.setDataHoraFechamento(LocalDateTime.now());
        ocorrencia = ocorrenciaRepositorio.save(ocorrencia);

        // Registrar no histórico
        try {
            String descricao = String.format(
                "Tipo: %s - Ocorrência cancelada. Gravidade: %s - Local: %s",
                ocorrencia.getTipoOcorrencia(),
                ocorrencia.getGravidade(),
                ocorrencia.getBairroLocal() != null ? ocorrencia.getBairroLocal().getNome() : "Não informado"
            );
            
            historicoOcorrenciaServico.registrarAcao(
                ocorrencia,
                usuarioCancelamento,
                AcaoHistorico.CANCELAMENTO,
                statusAnterior,
                StatusOcorrencia.CANCELADA,
                descricao
            );
        } catch (Exception e) {
            System.err.println("Erro ao registrar histórico de cancelamento: " + e.getMessage());
            e.printStackTrace();
        }

        return ocorrencia;
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
                // Buscar atendimento para obter informações da ambulância
                Atendimento atendimento = atendimentoRepositorio.findByOcorrenciaId(idOcorrencia);
                String placaAmbulancia = atendimento != null && atendimento.getAmbulancia() != null 
                    ? atendimento.getAmbulancia().getPlaca() 
                    : null;
                
                String descricao = String.format(
                    "Tipo: %s - Ocorrência concluída. %s Tempo de atendimento: %d minutos. SLA: %d minutos. %s",
                    ocorrencia.getTipoOcorrencia(),
                    placaAmbulancia != null ? String.format("Ambulância: %s. ", placaAmbulancia) : "",
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
                    descricao,
                    placaAmbulancia,
                    placaAmbulancia != null ? "Retornando para base" : null
                );
            } catch (Exception e) {
                System.err.println("Erro ao registrar histórico de conclusão: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ocorrencia;
    }

    /**
     * Registra a chegada da ambulância ao local e finaliza automaticamente a OS.
     * Após a finalização, inicia a contagem do tempo de retorno da ambulância para a base.
     * O tempo de retorno é igual ao tempo de deslocamento (despacho até chegada).
     * 
     * @param idAtendimento ID do atendimento
     * @param usuarioChegada Usuário que está registrando a chegada (pode ser null)
     * @return Ocorrência finalizada com dados de SLA calculados
     */
    @Transactional
    public Ocorrencia registrarChegada(Long idAtendimento, Usuario usuarioChegada) {
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
            throw new IllegalStateException("Ocorrência cancelada não pode ter chegada registrada");
        }

        // Capturar status anterior
        StatusOcorrencia statusAnterior = ocorrencia.getStatus();

        // Registrar chegada
        LocalDateTime agora = LocalDateTime.now();
        atendimento.setDataHoraChegada(agora);
        atendimentoRepositorio.save(atendimento);

        // Finalizar a OS automaticamente quando a ambulância chega
        ocorrencia.setDataHoraFechamento(agora);
        ocorrencia.setStatusOcorrencia(StatusOcorrencia.CONCLUIDA);
        
        // Calcular tempo desde abertura até chegada (tempo até despacho + tempo desde despacho até chegada)
        LocalDateTime dataAbertura = ocorrencia.getDataHoraAbertura();
        if (dataAbertura != null) {
            long minutos = java.time.Duration.between(dataAbertura, agora).toMinutes();
            ocorrencia.setTempoAtendimentoMinutos((int) minutos);
        }

        // Garantir que SLA está definido
        if (ocorrencia.getSlaMinutos() == null) {
            ocorrencia.setSlaMinutos(calcularSlaPorGravidade(ocorrencia.getGravidade()));
        }

        // Calcular tempo de deslocamento (desde despacho até chegada)
        long tempoDeslocamentoMinutos = 0;
        if (atendimento.getDataHoraDespacho() != null) {
            long tempoDeslocamentoSegundos = java.time.Duration.between(
                atendimento.getDataHoraDespacho(), agora).getSeconds();
            tempoDeslocamentoMinutos = tempoDeslocamentoSegundos / 60;
        }

        // Calcular SLA: apenas tempo desde abertura até chegada (tempo até despacho + tempo de deslocamento)
        // O tempo de retorno NÃO é considerado no SLA
        if (ocorrencia.getSlaMinutos() != null && dataAbertura != null) {
            // Tempo total desde abertura até chegada = tempo até despacho + tempo de deslocamento
            long tempoAteChegadaMinutos = ocorrencia.getTempoAtendimentoMinutos() != null 
                ? ocorrencia.getTempoAtendimentoMinutos() 
                : 0;
            
            // Verificar se SLA foi cumprido (apenas tempo até chegada, sem retorno)
            if (tempoAteChegadaMinutos <= ocorrencia.getSlaMinutos()) {
                ocorrencia.setSlaCumprido(true);
                ocorrencia.setTempoExcedidoMinutos(null);
            } else {
                ocorrencia.setSlaCumprido(false);
                ocorrencia.setTempoExcedidoMinutos((int) (tempoAteChegadaMinutos - ocorrencia.getSlaMinutos()));
            }
        } else {
            ocorrencia.setSlaCumprido(false);
            ocorrencia.setTempoExcedidoMinutos(null);
        }
        
        ocorrencia = ocorrenciaRepositorio.save(ocorrencia);

        // Registrar no histórico
        if (usuarioChegada != null) {
            try {
                // Usar tempoDeslocamentoMinutos já calculado acima
                
                String placaAmbulancia = atendimento.getAmbulancia() != null ? atendimento.getAmbulancia().getPlaca() : null;
                
                String descricao;
                if (ocorrencia.getSlaCumprido() != null && ocorrencia.getSlaCumprido()) {
                    descricao = String.format(
                        "Tipo: %s - Ambulância %s chegou ao local. OS finalizada automaticamente. " +
                        "Tempo de deslocamento (Dijkstra): %d minutos. " +
                        "Tempo total desde abertura: %d minutos. SLA: %d minutos. SLA CUMPRIDO.",
                        ocorrencia.getTipoOcorrencia(),
                        placaAmbulancia != null ? placaAmbulancia : "N/A",
                        tempoDeslocamentoMinutos,
                        ocorrencia.getTempoAtendimentoMinutos() != null ? ocorrencia.getTempoAtendimentoMinutos() : 0,
                        ocorrencia.getSlaMinutos() != null ? ocorrencia.getSlaMinutos() : 0
                    );
                } else {
                    descricao = String.format(
                        "Tipo: %s - Ambulância %s chegou ao local. OS finalizada automaticamente. " +
                        "Tempo de deslocamento (Dijkstra): %d minutos. " +
                        "Tempo total desde abertura: %d minutos. SLA: %d minutos. SLA NÃO CUMPRIDO. " +
                        "Tempo excedido: %d minutos.",
                        ocorrencia.getTipoOcorrencia(),
                        placaAmbulancia != null ? placaAmbulancia : "N/A",
                        tempoDeslocamentoMinutos,
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
                    descricao,
                    placaAmbulancia,
                    "Retornando para base"
                );
            } catch (Exception e) {
                System.err.println("Erro ao registrar histórico de chegada: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ocorrencia;
    }

    /**
     * Obtém informações detalhadas do timer em tempo real para uma ocorrência.
     * Calcula todos os tempos decorridos, tempo restante do SLA e status atual.
     * 
     * @param idOcorrencia ID da ocorrência
     * @return DTO com informações completas do timer
     */
    public com.vitalistech.sosrota.web.dto.TimerOcorrenciaDTO obterInformacoesTimer(Long idOcorrencia) {
        Ocorrencia ocorrencia = ocorrenciaRepositorio.findById(idOcorrencia)
                .orElseThrow(() -> new IllegalArgumentException("Ocorrência não encontrada"));

        com.vitalistech.sosrota.web.dto.TimerOcorrenciaDTO dto = new com.vitalistech.sosrota.web.dto.TimerOcorrenciaDTO();
        dto.setIdOcorrencia(ocorrencia.getId());
        dto.setStatus(ocorrencia.getStatus() != null ? ocorrencia.getStatus().name() : null);
        dto.setDataHoraAbertura(ocorrencia.getDataHoraAbertura());
        
        // Buscar atendimento associado
        Atendimento atendimento = atendimentoRepositorio.findByOcorrenciaId(idOcorrencia);
        if (atendimento != null) {
            dto.setIdAtendimento(atendimento.getId());
            dto.setDataHoraDespacho(atendimento.getDataHoraDespacho());
            dto.setDataHoraChegada(atendimento.getDataHoraChegada());
            dto.setDataHoraRetorno(atendimento.getDataHoraRetorno());
            dto.setPlacaAmbulancia(atendimento.getAmbulancia() != null ? atendimento.getAmbulancia().getPlaca() : null);
            dto.setDistanciaKm(atendimento.getDistanciaKm());
        }
        
        dto.setDataHoraFechamento(ocorrencia.getDataHoraFechamento());
        dto.setSlaMinutos(ocorrencia.getSlaMinutos());
        
        LocalDateTime agora = LocalDateTime.now();
        
        // Status das etapas - definir ANTES de usar
        dto.setFoiDespachada(atendimento != null && atendimento.getDataHoraDespacho() != null);
        dto.setChegouLocal(atendimento != null && atendimento.getDataHoraChegada() != null);
        dto.setFoiConcluida(ocorrencia.getStatus() == StatusOcorrencia.CONCLUIDA);
        // IMPORTANTE: Verificar retornouBase ANTES de calcular tempos
        dto.setRetornouBase(atendimento != null && atendimento.getDataHoraRetorno() != null);
        
        // Calcular tempo total: abertura + tempo até chegada + tempo de retorno (se concluída e retornou)
        // O tempo total inclui:
        // - Se ainda não chegou: tempo desde abertura até agora (estimativa)
        // - Se chegou mas não retornou: tempo desde abertura até chegada + tempo decorrido de retorno
        // - Se retornou: tempo desde abertura até retorno (tempo total completo e FIXO - não aumenta mais)
        if (ocorrencia.getDataHoraAbertura() != null) {
            long tempoTotalSegundos = 0;
            Boolean chegouLocal = dto.getChegouLocal();
            Boolean foiDespachada = dto.getFoiDespachada();
            Boolean retornouBase = dto.getRetornouBase();
            
            if (Boolean.TRUE.equals(retornouBase) && atendimento != null && atendimento.getDataHoraRetorno() != null) {
                // Se retornou: tempo total = abertura até retorno (tempo completo incluindo retorno)
                // IMPORTANTE: Usar dataHoraRetorno FIXO, não "agora", para que o tempo não continue aumentando
                tempoTotalSegundos = java.time.Duration.between(
                    ocorrencia.getDataHoraAbertura(), atendimento.getDataHoraRetorno()).getSeconds();
            } else if (Boolean.TRUE.equals(chegouLocal) && atendimento != null && atendimento.getDataHoraChegada() != null) {
                // Se chegou mas ainda não retornou: tempo desde abertura até chegada + tempo decorrido de retorno
                long tempoAteChegadaSegundos = java.time.Duration.between(
                    ocorrencia.getDataHoraAbertura(), atendimento.getDataHoraChegada()).getSeconds();
                long tempoRetornoSegundos = 0;
                if (Boolean.TRUE.equals(dto.getFoiConcluida())) {
                    // Se está concluída mas ainda não retornou, calcular tempo decorrido desde chegada
                    tempoRetornoSegundos = java.time.Duration.between(
                        atendimento.getDataHoraChegada(), agora).getSeconds();
                }
                tempoTotalSegundos = tempoAteChegadaSegundos + tempoRetornoSegundos;
            } else if (Boolean.TRUE.equals(foiDespachada) && atendimento != null && atendimento.getDataHoraDespacho() != null) {
                // Se ainda não chegou mas foi despachada: tempo desde abertura até agora (estimativa)
                tempoTotalSegundos = java.time.Duration.between(ocorrencia.getDataHoraAbertura(), agora).getSeconds();
            } else {
                // Se ainda não foi despachada: apenas tempo desde abertura
                tempoTotalSegundos = java.time.Duration.between(ocorrencia.getDataHoraAbertura(), agora).getSeconds();
            }
            dto.setTempoTotalDecorridoMinutos(tempoTotalSegundos / 60);
            dto.setTempoTotalFormatado(formatarTempo(tempoTotalSegundos));
        }
        
        // Calcular tempo desde o despacho
        if (atendimento != null && Boolean.TRUE.equals(dto.getFoiDespachada()) && atendimento.getDataHoraDespacho() != null) {
            long tempoDespachoSegundos = java.time.Duration.between(atendimento.getDataHoraDespacho(), agora).getSeconds();
            dto.setTempoDespachoMinutos(tempoDespachoSegundos / 60);
        }
        
        // Calcular tempo estimado de chegada baseado na distância (60 km/h)
        // Tempo estimado = (distância em km / 60 km/h) * 60 minutos
        Long tempoAteChegadaMinutosCalculado = null;
        Long tempoRestanteAteChegadaMinutos = null;
        
        if (atendimento != null && atendimento.getDistanciaKm() != null && atendimento.getDistanciaKm() > 0) {
            // Calcular tempo estimado de chegada baseado na distância (velocidade 60 km/h)
            double distanciaKm = atendimento.getDistanciaKm();
            long tempoEstimadoChegadaMinutos = (long) Math.ceil((distanciaKm / 60.0) * 60);
            dto.setTempoAteChegadaMinutos(tempoEstimadoChegadaMinutos);
            
            Boolean chegouLocal = dto.getChegouLocal();
            Boolean foiDespachada = dto.getFoiDespachada();
            
            if (Boolean.TRUE.equals(chegouLocal) && atendimento.getDataHoraDespacho() != null && atendimento.getDataHoraChegada() != null) {
                // Se já chegou: calcular tempo real de deslocamento
                long tempoAteChegadaSegundos = java.time.Duration.between(
                    atendimento.getDataHoraDespacho(), atendimento.getDataHoraChegada()).getSeconds();
                tempoAteChegadaMinutosCalculado = tempoAteChegadaSegundos / 60;
                dto.setTempoAteChegadaMinutos(tempoAteChegadaMinutosCalculado);
                tempoRestanteAteChegadaMinutos = 0L; // Já chegou
                dto.setTempoRetornoMinutos(tempoAteChegadaMinutosCalculado);
            } else if (Boolean.TRUE.equals(foiDespachada) && atendimento.getDataHoraDespacho() != null) {
                // Se ainda não chegou: calcular tempo restante até chegada (DECRESCENTE)
                long tempoDesdeDespachoSegundos = java.time.Duration.between(atendimento.getDataHoraDespacho(), agora).getSeconds();
                long tempoDesdeDespachoMinutos = tempoDesdeDespachoSegundos / 60;
                
                // Tempo restante = tempo estimado - tempo decorrido (DECRESCENTE)
                tempoRestanteAteChegadaMinutos = Math.max(0, tempoEstimadoChegadaMinutos - tempoDesdeDespachoMinutos);
                dto.setTempoRestanteAteChegadaMinutos(tempoRestanteAteChegadaMinutos);
                
                // Se o tempo restante chegou a 0, finalizar automaticamente a OS
                if (tempoRestanteAteChegadaMinutos <= 0 && atendimento.getDataHoraChegada() == null) {
                    // Finalizar automaticamente quando tempo restante chega a 0
                    try {
                        registrarChegada(atendimento.getId(), null);
                        // Recarregar dados após finalização
                        atendimento = atendimentoRepositorio.findById(atendimento.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Atendimento não encontrado"));
                        ocorrencia = ocorrenciaRepositorio.findById(ocorrencia.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Ocorrência não encontrada"));
                        // Atualizar status no DTO após finalização automática
                        dto.setChegouLocal(true);
                        dto.setFoiConcluida(true);
                        dto.setStatus(StatusOcorrencia.CONCLUIDA.name());
                        dto.setDataHoraChegada(atendimento.getDataHoraChegada());
                        dto.setDataHoraFechamento(ocorrencia.getDataHoraFechamento());
                        
                        // Recalcular tempo de chegada com os dados atualizados
                        if (atendimento.getDataHoraDespacho() != null && atendimento.getDataHoraChegada() != null) {
                            long tempoAteChegadaSegundos = java.time.Duration.between(
                                atendimento.getDataHoraDespacho(), atendimento.getDataHoraChegada()).getSeconds();
                            long tempoAteChegadaMinutos = tempoAteChegadaSegundos / 60;
                            dto.setTempoAteChegadaMinutos(tempoAteChegadaMinutos);
                            dto.setTempoRestanteAteChegadaMinutos(0L);
                            // Tempo de retorno = mesmo tempo da ida
                            dto.setTempoRetornoMinutos(tempoAteChegadaMinutos);
                        }
                    } catch (Exception e) {
                        // Log do erro mas continua o processamento
                        System.err.println("Erro ao finalizar automaticamente: " + e.getMessage());
                    }
                }
                
                // Tempo de retorno estimado = tempo estimado de chegada
                dto.setTempoRetornoMinutos(tempoEstimadoChegadaMinutos);
            }
        } else if (atendimento != null && Boolean.TRUE.equals(dto.getChegouLocal()) && atendimento.getDataHoraDespacho() != null && atendimento.getDataHoraChegada() != null) {
            // Se chegou mas não tem distância salva, calcular do tempo real
            long tempoAteChegadaSegundos = java.time.Duration.between(
                atendimento.getDataHoraDespacho(), atendimento.getDataHoraChegada()).getSeconds();
            tempoAteChegadaMinutosCalculado = tempoAteChegadaSegundos / 60;
            dto.setTempoAteChegadaMinutos(tempoAteChegadaMinutosCalculado);
            tempoRestanteAteChegadaMinutos = 0L;
            dto.setTempoRetornoMinutos(tempoAteChegadaMinutosCalculado);
        }
        
        // Calcular tempo após chegada
        if (atendimento != null && Boolean.TRUE.equals(dto.getChegouLocal()) && atendimento.getDataHoraChegada() != null && !Boolean.TRUE.equals(dto.getFoiConcluida())) {
            long tempoAposChegadaSegundos = java.time.Duration.between(atendimento.getDataHoraChegada(), agora).getSeconds();
            dto.setTempoAposChegadaMinutos(tempoAposChegadaSegundos / 60);
        }
        
        // Calcular tempo de retorno
        if (atendimento != null && atendimento.getDataHoraChegada() != null) {
            if (atendimento.getDataHoraRetorno() != null) {
                // Retorno já registrado
                long tempoRetornoSegundos = java.time.Duration.between(
                    atendimento.getDataHoraChegada(), atendimento.getDataHoraRetorno()).getSeconds();
                dto.setTempoRetornoDecorridoMinutos(tempoRetornoSegundos / 60);
                dto.setRetornouBase(true);
                dto.setDataHoraRetorno(atendimento.getDataHoraRetorno());
            } else if (Boolean.TRUE.equals(dto.getFoiConcluida())) {
                // OS finalizada mas ainda não retornou - calcular tempo decorrido desde chegada
                long tempoRetornoSegundos = java.time.Duration.between(
                    atendimento.getDataHoraChegada(), agora).getSeconds();
                dto.setTempoRetornoDecorridoMinutos(tempoRetornoSegundos / 60);
                dto.setRetornouBase(false);
                
                // Tempo de retorno estimado = tempo de deslocamento (ida) - mesmo tempo usado para ida
                // Se não tiver tempo calculado, usar tempo estimado baseado na distância
                if (tempoAteChegadaMinutosCalculado != null) {
                    dto.setTempoRetornoMinutos(tempoAteChegadaMinutosCalculado);
                } else if (atendimento.getDistanciaKm() != null && atendimento.getDistanciaKm() > 0) {
                    // Calcular tempo estimado de retorno baseado na distância (60 km/h)
                    double distanciaKm = atendimento.getDistanciaKm();
                    long tempoEstimadoRetornoMinutos = (long) Math.ceil((distanciaKm / 60.0) * 60);
                    dto.setTempoRetornoMinutos(tempoEstimadoRetornoMinutos);
                }
                
                // Calcular tempo restante de retorno (DECRESCENTE) - mesmo tempo que foi usado para ida
                if (dto.getTempoRetornoMinutos() != null && dto.getTempoRetornoDecorridoMinutos() != null) {
                    long tempoRestanteRetorno = Math.max(0, dto.getTempoRetornoMinutos() - dto.getTempoRetornoDecorridoMinutos());
                    
                    // Se o tempo restante de retorno chegou a 0, registrar retorno automaticamente
                    // Isso torna a ambulância/equipe disponível novamente
                    if (tempoRestanteRetorno <= 0 && atendimento.getDataHoraRetorno() == null) {
                        try {
                            // Registrar retorno (o histórico será criado mesmo sem usuário)
                            registrarRetorno(atendimento.getId(), null);
                            // Recarregar dados após retorno
                            atendimento = atendimentoRepositorio.findById(atendimento.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Atendimento não encontrado"));
                            dto.setRetornouBase(true);
                            dto.setDataHoraRetorno(atendimento.getDataHoraRetorno());
                            if (atendimento.getDataHoraRetorno() != null) {
                                long tempoRetornoSegundosFinal = java.time.Duration.between(
                                    atendimento.getDataHoraChegada(), atendimento.getDataHoraRetorno()).getSeconds();
                                dto.setTempoRetornoDecorridoMinutos(tempoRetornoSegundosFinal / 60);
                            }
                        } catch (Exception e) {
                            // Log do erro mas continua o processamento
                            System.err.println("Erro ao registrar retorno automaticamente: " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        // Calcular SLA: tempo desde abertura até chegada ao local
        // O SLA é contabilizado desde o momento da abertura e finalizado quando a ambulância chega ao local
        // O tempo de retorno NÃO é considerado no SLA
        if (ocorrencia.getSlaMinutos() != null && ocorrencia.getDataHoraAbertura() != null) {
            long tempoSlaDecorridoMinutos = 0;
            
            Boolean chegouLocal = dto.getChegouLocal();
            Boolean foiDespachada = dto.getFoiDespachada();
            
            if (Boolean.TRUE.equals(chegouLocal) && atendimento != null && atendimento.getDataHoraChegada() != null) {
                // Se já chegou: SLA = tempo desde abertura até chegada
                long tempoDesdeAberturaAteChegadaSegundos = java.time.Duration.between(
                    ocorrencia.getDataHoraAbertura(), atendimento.getDataHoraChegada()).getSeconds();
                tempoSlaDecorridoMinutos = tempoDesdeAberturaAteChegadaSegundos / 60;
            } else if (Boolean.TRUE.equals(foiDespachada) && atendimento != null && atendimento.getDataHoraDespacho() != null) {
                // Se ainda não chegou: calcular tempo estimado
                // Tempo desde abertura até despacho
                long tempoDesdeAberturaAteDespachoSegundos = java.time.Duration.between(
                    ocorrencia.getDataHoraAbertura(), atendimento.getDataHoraDespacho()).getSeconds();
                long tempoDesdeAberturaAteDespachoMinutos = tempoDesdeAberturaAteDespachoSegundos / 60;
                
                // Tempo desde despacho até agora (tempo de deslocamento em andamento)
                long tempoDeslocamentoDecorridoSegundos = java.time.Duration.between(
                    atendimento.getDataHoraDespacho(), agora).getSeconds();
                long tempoDeslocamentoDecorridoMinutos = tempoDeslocamentoDecorridoSegundos / 60;
                
                // SLA decorrido = tempo desde abertura até agora (estimativa até chegada)
                tempoSlaDecorridoMinutos = tempoDesdeAberturaAteDespachoMinutos + tempoDeslocamentoDecorridoMinutos;
            } else {
                // Se ainda não foi despachada: apenas tempo desde abertura
                long tempoDesdeAberturaSegundos = java.time.Duration.between(ocorrencia.getDataHoraAbertura(), agora).getSeconds();
                tempoSlaDecorridoMinutos = tempoDesdeAberturaSegundos / 60;
            }
            
            dto.setTempoSlaDecorridoMinutos(tempoSlaDecorridoMinutos);
            long tempoRestanteMinutos = ocorrencia.getSlaMinutos() - tempoSlaDecorridoMinutos;
            
            dto.setTempoRestanteMinutos(tempoRestanteMinutos);
            dto.setSlaExcedido(tempoRestanteMinutos < 0);
            dto.setSlaEmRisco(tempoRestanteMinutos >= 0 && tempoRestanteMinutos <= (ocorrencia.getSlaMinutos() * 0.25));
            
            // Formatar tempo restante (pode ser negativo)
            long tempoRestanteSegundos = (ocorrencia.getSlaMinutos() * 60) - (tempoSlaDecorridoMinutos * 60);
            dto.setTempoRestanteFormatado(formatarTempo(tempoRestanteSegundos));
        }
        
        return dto;
    }
    
    /**
     * Formata tempo em segundos para uma string legível (ex: "15m 30s" ou "1h 25m").
     */
    private String formatarTempo(long segundos) {
        boolean negativo = segundos < 0;
        segundos = Math.abs(segundos);
        
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long seg = segundos % 60;
        
        StringBuilder sb = new StringBuilder();
        if (negativo) sb.append("-");
        
        if (horas > 0) {
            sb.append(horas).append("h");
            if (minutos > 0) {
                sb.append(" ").append(minutos).append("m");
            }
        } else if (minutos > 0) {
            sb.append(minutos).append("m");
            if (seg > 0) {
                sb.append(" ").append(seg).append("s");
            }
        } else {
            sb.append(seg).append("s");
        }
        
        return sb.toString();
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
                String placaAmbulancia = atendimento.getAmbulancia() != null ? atendimento.getAmbulancia().getPlaca() : null;
                
                String descricao;
                if (ocorrencia.getSlaCumprido() != null && ocorrencia.getSlaCumprido()) {
                    descricao = String.format(
                        "Tipo: %s - Ambulância %s chegou ao local. Ocorrência fechada automaticamente. " +
                        "Tempo total desde abertura: %d minutos. SLA: %d minutos. SLA CUMPRIDO.",
                        ocorrencia.getTipoOcorrencia(),
                        placaAmbulancia != null ? placaAmbulancia : "N/A",
                        ocorrencia.getTempoAtendimentoMinutos() != null ? ocorrencia.getTempoAtendimentoMinutos() : 0,
                        ocorrencia.getSlaMinutos() != null ? ocorrencia.getSlaMinutos() : 0
                    );
                } else {
                    descricao = String.format(
                        "Tipo: %s - Ambulância %s chegou ao local. Ocorrência fechada automaticamente. " +
                        "Tempo total desde abertura: %d minutos. SLA: %d minutos. SLA NÃO CUMPRIDO. " +
                        "Tempo excedido: %d minutos.",
                        ocorrencia.getTipoOcorrencia(),
                        placaAmbulancia != null ? placaAmbulancia : "N/A",
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
                    descricao,
                    placaAmbulancia,
                    "Retornando para base"
                );
            } catch (Exception e) {
                System.err.println("Erro ao registrar histórico de chegada: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return ocorrencia;
    }

    /**
     * Registra o retorno da ambulância à base de origem.
     * A OS deve estar finalizada (CONCLUIDA) para registrar o retorno.
     * Quando a ambulância retorna, ela fica disponível novamente.
     * 
     * @param idAtendimento ID do atendimento
     * @param usuarioRetorno Usuário que está registrando o retorno (pode ser null)
     * @return Atendimento atualizado com dataHoraRetorno
     */
    @Transactional
    public Atendimento registrarRetorno(Long idAtendimento, Usuario usuarioRetorno) {
        Atendimento atendimento = atendimentoRepositorio.findById(idAtendimento)
                .orElseThrow(() -> new IllegalArgumentException("Atendimento não encontrado"));

        if (atendimento.getDataHoraRetorno() != null) {
            throw new IllegalStateException("Retorno já foi registrado para este atendimento");
        }

        Ocorrencia ocorrencia = atendimento.getOcorrencia();
        if (ocorrencia == null) {
            throw new IllegalStateException("Atendimento não possui ocorrência associada");
        }

        if (ocorrencia.getStatus() != StatusOcorrencia.CONCLUIDA) {
            throw new IllegalStateException("A OS deve estar finalizada (CONCLUIDA) para registrar o retorno");
        }

        if (atendimento.getDataHoraChegada() == null) {
            throw new IllegalStateException("A chegada deve ser registrada antes do retorno");
        }

        Ambulancia ambulancia = atendimento.getAmbulancia();
        if (ambulancia == null) {
            throw new IllegalStateException("Atendimento não possui ambulância associada");
        }

        // Registrar retorno
        LocalDateTime agora = LocalDateTime.now();
        atendimento.setDataHoraRetorno(agora);
        atendimentoRepositorio.save(atendimento);

        // Calcular tempo de retorno (desde chegada até retorno)
        long tempoRetornoSegundos = java.time.Duration.between(
            atendimento.getDataHoraChegada(), agora).getSeconds();
        long tempoRetornoMinutos = tempoRetornoSegundos / 60;

        // Marcar ambulância como DISPONIVEL novamente
        ambulancia.setStatus(StatusAmbulancia.DISPONIVEL);
        ambulanciaRepositorio.save(ambulancia);

        // Marcar todos os profissionais da equipe como DISPONIVEL novamente
        // Isso permite que a equipe seja incluída em novos chamados
        Equipe equipe = atendimento.getEquipe();
        if (equipe != null) {
            for (EquipeProfissional ep : equipe.getProfissionais()) {
                Profissional profissional = ep.getProfissional();
                profissional.setStatus(StatusProfissional.DISPONIVEL);
                profissionalRepositorio.save(profissional);
            }
        }

        // Registrar no histórico (mesmo se usuarioRetorno for null - retorno automático)
        try {
            // Calcular tempo total desde abertura até retorno
            long tempoTotalMinutos = 0;
            if (ocorrencia.getDataHoraAbertura() != null && atendimento.getDataHoraRetorno() != null) {
                long tempoTotalSegundos = java.time.Duration.between(
                    ocorrencia.getDataHoraAbertura(), atendimento.getDataHoraRetorno()).getSeconds();
                tempoTotalMinutos = tempoTotalSegundos / 60;
            }
            
            String descricao = String.format(
                "Tipo: %s - Ambulância %s retornou à base. " +
                "Tempo de retorno: %d minutos. Tempo total: %d minutos. " +
                "Ambulância e equipe disponíveis novamente para novos chamados.",
                ocorrencia.getTipoOcorrencia(),
                ambulancia.getPlaca(),
                tempoRetornoMinutos,
                tempoTotalMinutos
            );
            
            // Se não houver usuário, usar o usuário que registrou a ocorrência ou null
            Usuario usuarioParaHistorico = usuarioRetorno;
            if (usuarioParaHistorico == null && ocorrencia.getUsuarioRegistro() != null) {
                usuarioParaHistorico = ocorrencia.getUsuarioRegistro();
            }
            
            historicoOcorrenciaServico.registrarAcao(
                ocorrencia,
                usuarioParaHistorico,
                AcaoHistorico.ALTERACAO_STATUS,
                StatusOcorrencia.CONCLUIDA,
                StatusOcorrencia.CONCLUIDA,
                descricao,
                ambulancia.getPlaca(),
                "Retornou à base"
            );
        } catch (Exception e) {
            System.err.println("Erro ao registrar histórico de retorno: " + e.getMessage());
            e.printStackTrace();
        }

        return atendimento;
    }
}
