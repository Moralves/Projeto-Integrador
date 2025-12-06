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
        
        HistoricoOcorrencia historico = new HistoricoOcorrencia();
        historico.setOcorrencia(ocorrencia);
        historico.setUsuario(usuario);
        historico.setAcao(acao);
        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(statusNovo);
        historico.setDescricaoAcao(descricaoAcao);
        historico.setDataHora(LocalDateTime.now());

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

        return historicoRepositorio.save(historico);
    }

    /**
     * Registra a abertura de uma ocorrência.
     * Método auxiliar específico para abertura.
     */
    @Transactional
    public HistoricoOcorrencia registrarAbertura(Ocorrencia ocorrencia, Usuario usuario) {
        String descricao = String.format(
            "Ocorrência aberta: %s - Gravidade: %s - Local: %s",
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
        String descricao = String.format(
            "Ocorrência despachada. %s",
            detalhes != null ? detalhes : ""
        );
        
        return registrarAcao(
            ocorrencia,
            usuario,
            AcaoHistorico.DESPACHO,
            StatusOcorrencia.ABERTA,
            StatusOcorrencia.DESPACHADA,
            descricao
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

