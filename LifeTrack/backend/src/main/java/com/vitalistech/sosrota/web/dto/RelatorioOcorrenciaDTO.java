package com.vitalistech.sosrota.web.dto;

import java.time.LocalDateTime;

/**
 * DTO para relatório de ocorrências com informações de auditoria.
 */
public class RelatorioOcorrenciaDTO {
    private Long id;
    private LocalDateTime dataHoraAbertura;
    private String tipoOcorrencia;
    private String gravidade;
    private String status;
    private String bairroNome;
    private String observacoes;
    private String usuarioRegistroNome;
    private String usuarioRegistroLogin;
    private String usuarioDespachoNome;
    private String usuarioDespachoLogin;
    private LocalDateTime dataHoraDespacho;
    private String ambulanciaPlaca;
    private Double distanciaKm;
    private LocalDateTime dataHoraFechamento;
    private Integer tempoAtendimentoMinutos;
    private Integer slaMinutos;
    private Boolean slaCumprido;
    private Integer tempoExcedidoMinutos;

    public RelatorioOcorrenciaDTO() {
    }

    public RelatorioOcorrenciaDTO(Long id, LocalDateTime dataHoraAbertura, String tipoOcorrencia,
                                  String gravidade, String status, String bairroNome, String observacoes,
                                  String usuarioRegistroNome, String usuarioRegistroLogin,
                                  String usuarioDespachoNome, String usuarioDespachoLogin,
                                  LocalDateTime dataHoraDespacho, String ambulanciaPlaca, Double distanciaKm,
                                  LocalDateTime dataHoraFechamento, Integer tempoAtendimentoMinutos,
                                  Integer slaMinutos, Boolean slaCumprido, Integer tempoExcedidoMinutos) {
        this.id = id;
        this.dataHoraAbertura = dataHoraAbertura;
        this.tipoOcorrencia = tipoOcorrencia;
        this.gravidade = gravidade;
        this.status = status;
        this.bairroNome = bairroNome;
        this.observacoes = observacoes;
        this.usuarioRegistroNome = usuarioRegistroNome;
        this.usuarioRegistroLogin = usuarioRegistroLogin;
        this.usuarioDespachoNome = usuarioDespachoNome;
        this.usuarioDespachoLogin = usuarioDespachoLogin;
        this.dataHoraDespacho = dataHoraDespacho;
        this.ambulanciaPlaca = ambulanciaPlaca;
        this.distanciaKm = distanciaKm;
        this.dataHoraFechamento = dataHoraFechamento;
        this.tempoAtendimentoMinutos = tempoAtendimentoMinutos;
        this.slaMinutos = slaMinutos;
        this.slaCumprido = slaCumprido;
        this.tempoExcedidoMinutos = tempoExcedidoMinutos;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDataHoraAbertura() { return dataHoraAbertura; }
    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) { this.dataHoraAbertura = dataHoraAbertura; }

    public String getTipoOcorrencia() { return tipoOcorrencia; }
    public void setTipoOcorrencia(String tipoOcorrencia) { this.tipoOcorrencia = tipoOcorrencia; }

    public String getGravidade() { return gravidade; }
    public void setGravidade(String gravidade) { this.gravidade = gravidade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBairroNome() { return bairroNome; }
    public void setBairroNome(String bairroNome) { this.bairroNome = bairroNome; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getUsuarioRegistroNome() { return usuarioRegistroNome; }
    public void setUsuarioRegistroNome(String usuarioRegistroNome) { this.usuarioRegistroNome = usuarioRegistroNome; }

    public String getUsuarioRegistroLogin() { return usuarioRegistroLogin; }
    public void setUsuarioRegistroLogin(String usuarioRegistroLogin) { this.usuarioRegistroLogin = usuarioRegistroLogin; }

    public String getUsuarioDespachoNome() { return usuarioDespachoNome; }
    public void setUsuarioDespachoNome(String usuarioDespachoNome) { this.usuarioDespachoNome = usuarioDespachoNome; }

    public String getUsuarioDespachoLogin() { return usuarioDespachoLogin; }
    public void setUsuarioDespachoLogin(String usuarioDespachoLogin) { this.usuarioDespachoLogin = usuarioDespachoLogin; }

    public LocalDateTime getDataHoraDespacho() { return dataHoraDespacho; }
    public void setDataHoraDespacho(LocalDateTime dataHoraDespacho) { this.dataHoraDespacho = dataHoraDespacho; }

    public String getAmbulanciaPlaca() { return ambulanciaPlaca; }
    public void setAmbulanciaPlaca(String ambulanciaPlaca) { this.ambulanciaPlaca = ambulanciaPlaca; }

    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }

    public LocalDateTime getDataHoraFechamento() { return dataHoraFechamento; }
    public void setDataHoraFechamento(LocalDateTime dataHoraFechamento) { this.dataHoraFechamento = dataHoraFechamento; }

    public Integer getTempoAtendimentoMinutos() { return tempoAtendimentoMinutos; }
    public void setTempoAtendimentoMinutos(Integer tempoAtendimentoMinutos) { this.tempoAtendimentoMinutos = tempoAtendimentoMinutos; }

    public Integer getSlaMinutos() { return slaMinutos; }
    public void setSlaMinutos(Integer slaMinutos) { this.slaMinutos = slaMinutos; }

    public Boolean getSlaCumprido() { return slaCumprido; }
    public void setSlaCumprido(Boolean slaCumprido) { this.slaCumprido = slaCumprido; }

    public Integer getTempoExcedidoMinutos() { return tempoExcedidoMinutos; }
    public void setTempoExcedidoMinutos(Integer tempoExcedidoMinutos) { this.tempoExcedidoMinutos = tempoExcedidoMinutos; }
}

