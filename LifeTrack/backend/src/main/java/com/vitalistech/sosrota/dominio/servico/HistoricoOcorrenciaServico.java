package com.vitalistech.sosrota.dominio.servico;

import com.vitalistech.sosrota.dominio.modelo.*;
import com.vitalistech.sosrota.dominio.repositorio.HistoricoOcorrenciaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Serviço responsável por registrar e consultar o histórico de ocorrências.
 */
@Service
public class HistoricoOcorrenciaServico {

    private final HistoricoOcorrenciaRepositorio historicoRepositorio;

    public HistoricoOcorrenciaServico(HistoricoOcorrenciaRepositorio historicoRepositorio) {
        this.historicoRepositorio = historicoRepositorio;
    }

    /**
     * Registra uma ação no histórico de ocorrências.
     * Captura um snapshot completo das informações da ocorrência e do usuário.
     * 
     * @param ocorrencia A ocorrência relacionada
     * @param usuario O usuário que realizou a ação
     * @param acao O tipo de ação realizada
     * @param statusAnterior O status anterior da ocorrência (pode ser null)
     * @param statusNovo O novo status da ocorrência
     * @param descricaoAcao Descrição detalhada da ação (opcional)
     */
    @Transactional
    public HistoricoOcorrencia registrarAcao(Ocorrencia ocorrencia,
                                             Usuario usuario,
                                             AcaoHistorico acao,
                                             StatusOcorrencia statusAnterior,
                                             StatusOcorrencia statusNovo,
                                             String descricaoAcao) {
        return registrarAcao(ocorrencia, usuario, acao, statusAnterior, statusNovo, descricaoAcao, null, null);
    }

    /**
     * Registra uma ação no histórico de ocorrências com informações da ambulância.
     * Captura um snapshot completo das informações da ocorrência, do usuário e da ambulância.
     * 
     * @param ocorrencia A ocorrência relacionada
     * @param usuario O usuário que realizou a ação
     * @param acao O tipo de ação realizada
     * @param statusAnterior O status anterior da ocorrência (pode ser null)
     * @param statusNovo O novo status da ocorrência
     * @param descricaoAcao Descrição detalhada da ação (opcional)
     * @param placaAmbulancia Placa da ambulância envolvida (opcional)
     * @param acaoAmbulancia Ação que a ambulância está executando (ex: "Indo até o local", "Retornando para base")
     */
    @Transactional
    public HistoricoOcorrencia registrarAcao(Ocorrencia ocorrencia,
                                             Usuario usuario,
                                             AcaoHistorico acao,
                                             StatusOcorrencia statusAnterior,
                                             StatusOcorrencia statusNovo,
                                             String descricaoAcao,
                                             String placaAmbulancia,
                                             String acaoAmbulancia) {
        return registrarAcaoComDataHora(ocorrencia, usuario, acao, statusAnterior, statusNovo, descricaoAcao, placaAmbulancia, acaoAmbulancia, LocalDateTime.now());
    }

    /**
     * Registra uma ação no histórico de ocorrências com informações da ambulância e data/hora customizada.
     * Captura um snapshot completo das informações da ocorrência, do usuário e da ambulância.
     * 
     * @param ocorrencia A ocorrência relacionada
     * @param usuario O usuário que realizou a ação
     * @param acao O tipo de ação realizada
     * @param statusAnterior O status anterior da ocorrência (pode ser null)
     * @param statusNovo O novo status da ocorrência
     * @param descricaoAcao Descrição detalhada da ação (opcional)
     * @param placaAmbulancia Placa da ambulância envolvida (opcional)
     * @param acaoAmbulancia Ação que a ambulância está executando (ex: "Indo até o local", "Retornando para base")
     * @param dataHora Data/hora customizada para o registro
     */
    @Transactional
    public HistoricoOcorrencia registrarAcaoComDataHora(Ocorrencia ocorrencia,
                                                        Usuario usuario,
                                                        AcaoHistorico acao,
                                                        StatusOcorrencia statusAnterior,
                                                        StatusOcorrencia statusNovo,
                                                        String descricaoAcao,
                                                        String placaAmbulancia,
                                                        String acaoAmbulancia,
                                                        LocalDateTime dataHora) {
        
        HistoricoOcorrencia historico = new HistoricoOcorrencia();
        historico.setOcorrencia(ocorrencia);
        historico.setUsuario(usuario);
        historico.setAcao(acao);
        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(statusNovo);
        historico.setDescricaoAcao(descricaoAcao);
        historico.setDataHora(dataHora);

        // Snapshot das informações da ocorrência
        historico.setTipoOcorrencia(ocorrencia.getTipoOcorrencia());
        historico.setGravidade(ocorrencia.getGravidade());
        historico.setBairroOrigemNome(ocorrencia.getBairroLocal() != null 
                ? ocorrencia.getBairroLocal().getNome() 
                : null);
        historico.setObservacoes(ocorrencia.getObservacoes());

        // Snapshot das informações do usuário
        historico.setUsuarioNome(usuario.getNome() != null ? usuario.getNome() : usuario.getLogin());
        historico.setUsuarioLogin(usuario.getLogin());
        historico.setUsuarioPerfil(usuario.getPerfil() != null ? usuario.getPerfil() : "USER");

        // Informações da ambulância
        historico.setPlacaAmbulancia(placaAmbulancia);
        historico.setAcaoAmbulancia(acaoAmbulancia);

        return historicoRepositorio.save(historico);
    }

    /**
     * Registra a abertura de uma ocorrência.
     * Método auxiliar específico para abertura.
     */
    @Transactional
    public HistoricoOcorrencia registrarAbertura(Ocorrencia ocorrencia, Usuario usuario) {
        String descricao = String.format(
            "Tipo: %s - Ocorrência aberta. Gravidade: %s - Local: %s",
            ocorrencia.getTipoOcorrencia(),
            ocorrencia.getGravidade(),
            ocorrencia.getBairroLocal() != null ? ocorrencia.getBairroLocal().getNome() : "Não informado"
        );
        
        return registrarAcao(
            ocorrencia,
            usuario,
            AcaoHistorico.ABERTURA,
            null, // Não há status anterior na abertura
            StatusOcorrencia.ABERTA,
            descricao
        );
    }

    /**
     * Registra o despacho de uma ocorrência.
     * Método auxiliar específico para despacho.
     */
    @Transactional
    public HistoricoOcorrencia registrarDespacho(Ocorrencia ocorrencia, Usuario usuario, String detalhes) {
        return registrarDespacho(ocorrencia, usuario, detalhes, null);
    }

    /**
     * Registra o despacho de uma ocorrência com informações da ambulância.
     * Método auxiliar específico para despacho.
     */
    @Transactional
    public HistoricoOcorrencia registrarDespacho(Ocorrencia ocorrencia, Usuario usuario, String detalhes, String placaAmbulancia) {
        String descricao = String.format(
            "Tipo: %s - Ocorrência despachada. %s",
            ocorrencia.getTipoOcorrencia(),
            detalhes != null ? detalhes : ""
        );
        
        return registrarAcao(
            ocorrencia,
            usuario,
            AcaoHistorico.DESPACHO,
            StatusOcorrencia.ABERTA,
            StatusOcorrencia.DESPACHADA,
            descricao,
            placaAmbulancia,
            "Indo até o local"
        );
    }

    /**
     * Busca todo o histórico de uma ocorrência específica.
     */
    public java.util.List<HistoricoOcorrencia> buscarHistoricoPorOcorrencia(Long ocorrenciaId) {
        return historicoRepositorio.findByOcorrenciaIdOrderByDataHoraDesc(ocorrenciaId);
    }

    /**
     * Busca todo o histórico de ações de um usuário específico.
     */
    public java.util.List<HistoricoOcorrencia> buscarHistoricoPorUsuario(Long usuarioId) {
        return historicoRepositorio.findByUsuarioIdOrderByDataHoraDesc(usuarioId);
    }

    /**
     * Busca histórico com filtros opcionais.
     * Se ambos os parâmetros forem null, retorna todo o histórico.
     */
    public java.util.List<HistoricoOcorrencia> buscarHistoricoComFiltros(Long usuarioId, Long ocorrenciaId) {
        return historicoRepositorio.buscarComFiltros(usuarioId, ocorrenciaId);
    }
}

