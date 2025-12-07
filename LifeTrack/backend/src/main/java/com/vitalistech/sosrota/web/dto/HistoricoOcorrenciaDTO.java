package com.vitalistech.sosrota.web.dto;

import java.time.LocalDateTime;

/**
 * DTO para exibição do histórico de ocorrências.
 */
public class HistoricoOcorrenciaDTO {
    private Long id;
    private Long ocorrenciaId;
    private Long usuarioId;
    private String acao;
    private String statusAnterior;
    private String statusNovo;
    private String descricaoAcao;
    private LocalDateTime dataHora;
    
    // Informações da ocorrência (snapshot)
    private String tipoOcorrencia;
    private String gravidade;
    private String bairroOrigemNome;
    private String observacoes;
    
    // Informações do usuário (snapshot)
    private String usuarioNome;
    private String usuarioLogin;
    private String usuarioPerfil;
    
    // Informações da ambulância
    private String placaAmbulancia;
    private String acaoAmbulancia;

    public HistoricoOcorrenciaDTO() {
    }

    public HistoricoOcorrenciaDTO(Long id, Long ocorrenciaId, Long usuarioId, String acao,
                                  String statusAnterior, String statusNovo, String descricaoAcao,
                                  LocalDateTime dataHora, String tipoOcorrencia, String gravidade,
                                  String bairroOrigemNome, String observacoes, String usuarioNome,
                                  String usuarioLogin, String usuarioPerfil) {
        this.id = id;
        this.ocorrenciaId = ocorrenciaId;
        this.usuarioId = usuarioId;
        this.acao = acao;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.descricaoAcao = descricaoAcao;
        this.dataHora = dataHora;
        this.tipoOcorrencia = tipoOcorrencia;
        this.gravidade = gravidade;
        this.bairroOrigemNome = bairroOrigemNome;
        this.observacoes = observacoes;
        this.usuarioNome = usuarioNome;
        this.usuarioLogin = usuarioLogin;
        this.usuarioPerfil = usuarioPerfil;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOcorrenciaId() {
        return ocorrenciaId;
    }

    public void setOcorrenciaId(Long ocorrenciaId) {
        this.ocorrenciaId = ocorrenciaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getStatusAnterior() {
        return statusAnterior;
    }

    public void setStatusAnterior(String statusAnterior) {
        this.statusAnterior = statusAnterior;
    }

    public String getStatusNovo() {
        return statusNovo;
    }

    public void setStatusNovo(String statusNovo) {
        this.statusNovo = statusNovo;
    }

    public String getDescricaoAcao() {
        return descricaoAcao;
    }

    public void setDescricaoAcao(String descricaoAcao) {
        this.descricaoAcao = descricaoAcao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getTipoOcorrencia() {
        return tipoOcorrencia;
    }

    public void setTipoOcorrencia(String tipoOcorrencia) {
        this.tipoOcorrencia = tipoOcorrencia;
    }

    public String getGravidade() {
        return gravidade;
    }

    public void setGravidade(String gravidade) {
        this.gravidade = gravidade;
    }

    public String getBairroOrigemNome() {
        return bairroOrigemNome;
    }

    public void setBairroOrigemNome(String bairroOrigemNome) {
        this.bairroOrigemNome = bairroOrigemNome;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public String getUsuarioLogin() {
        return usuarioLogin;
    }

    public void setUsuarioLogin(String usuarioLogin) {
        this.usuarioLogin = usuarioLogin;
    }

    public String getUsuarioPerfil() {
        return usuarioPerfil;
    }

    public void setUsuarioPerfil(String usuarioPerfil) {
        this.usuarioPerfil = usuarioPerfil;
    }

    public String getPlacaAmbulancia() {
        return placaAmbulancia;
    }

    public void setPlacaAmbulancia(String placaAmbulancia) {
        this.placaAmbulancia = placaAmbulancia;
    }

    public String getAcaoAmbulancia() {
        return acaoAmbulancia;
    }

    public void setAcaoAmbulancia(String acaoAmbulancia) {
        this.acaoAmbulancia = acaoAmbulancia;
    }
}

