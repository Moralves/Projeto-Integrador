package com.vitalistech.sosrota.web.dto;

import java.time.LocalDateTime;

/**
 * DTO para informações do timer de uma ocorrência em tempo real.
 * Fornece todas as informações necessárias para exibir o progresso
 * e os tempos decorridos de forma profissional.
 */
public class TimerOcorrenciaDTO {

    private Long idOcorrencia;
    private String status;
    private LocalDateTime dataHoraAbertura;
    private LocalDateTime dataHoraDespacho;
    private LocalDateTime dataHoraChegada;
    private LocalDateTime dataHoraFechamento;
    private LocalDateTime dataHoraRetorno;
    
    // Tempos em minutos
    private Long tempoTotalDecorridoMinutos; // Abertura + tempo até chegada (tempo total do SLA)
    private Long tempoDespachoMinutos; // Desde o despacho até agora (se despachada)
    private Long tempoAteChegadaMinutos; // Tempo estimado até chegada (baseado na distância) - DECRESCENTE quando despachada
    private Long tempoRestanteAteChegadaMinutos; // Tempo restante até chegada (contagem regressiva) - DECRESCENTE
    private Long tempoAposChegadaMinutos; // Desde a chegada até agora (se chegou)
    private Long tempoRetornoMinutos; // Tempo de retorno (igual ao tempo de deslocamento até chegada)
    private Long tempoRetornoDecorridoMinutos; // Tempo decorrido desde chegada até retorno (se retornou)
    private Long tempoRestanteRetornoMinutos; // Tempo restante de retorno (DECRESCENTE)
    private Boolean retornouBase; // Se a ambulância já retornou à base
    
    // SLA
    private Integer slaMinutos;
    private Long tempoRestanteMinutos; // Tempo restante para cumprir SLA (pode ser negativo)
    private Boolean slaEmRisco; // Se o SLA está em risco (restam menos de 25% do tempo)
    private Boolean slaExcedido; // Se o SLA já foi excedido
    private Long tempoSlaDecorridoMinutos; // Tempo decorrido do SLA (tempo até chegada + tempo de retorno)
    
    // Status das etapas
    private Boolean foiDespachada;
    private Boolean chegouLocal;
    private Boolean foiConcluida;
    
    // Informações do atendimento
    private Long idAtendimento;
    private String placaAmbulancia;
    private Double distanciaKm;
    
    // Tempo formatado para exibição
    private String tempoTotalFormatado; // Ex: "15m 30s" ou "1h 25m"
    private String tempoRestanteFormatado; // Ex: "5m 15s" ou "-3m 20s" (negativo se excedido)

    public TimerOcorrenciaDTO() {
    }

    // Getters e Setters
    public Long getIdOcorrencia() {
        return idOcorrencia;
    }

    public void setIdOcorrencia(Long idOcorrencia) {
        this.idOcorrencia = idOcorrencia;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public LocalDateTime getDataHoraDespacho() {
        return dataHoraDespacho;
    }

    public void setDataHoraDespacho(LocalDateTime dataHoraDespacho) {
        this.dataHoraDespacho = dataHoraDespacho;
    }

    public LocalDateTime getDataHoraChegada() {
        return dataHoraChegada;
    }

    public void setDataHoraChegada(LocalDateTime dataHoraChegada) {
        this.dataHoraChegada = dataHoraChegada;
    }

    public LocalDateTime getDataHoraFechamento() {
        return dataHoraFechamento;
    }

    public void setDataHoraFechamento(LocalDateTime dataHoraFechamento) {
        this.dataHoraFechamento = dataHoraFechamento;
    }

    public Long getTempoTotalDecorridoMinutos() {
        return tempoTotalDecorridoMinutos;
    }

    public void setTempoTotalDecorridoMinutos(Long tempoTotalDecorridoMinutos) {
        this.tempoTotalDecorridoMinutos = tempoTotalDecorridoMinutos;
    }

    public Long getTempoDespachoMinutos() {
        return tempoDespachoMinutos;
    }

    public void setTempoDespachoMinutos(Long tempoDespachoMinutos) {
        this.tempoDespachoMinutos = tempoDespachoMinutos;
    }

    public Long getTempoAteChegadaMinutos() {
        return tempoAteChegadaMinutos;
    }

    public void setTempoAteChegadaMinutos(Long tempoAteChegadaMinutos) {
        this.tempoAteChegadaMinutos = tempoAteChegadaMinutos;
    }

    public Long getTempoRestanteAteChegadaMinutos() {
        return tempoRestanteAteChegadaMinutos;
    }

    public void setTempoRestanteAteChegadaMinutos(Long tempoRestanteAteChegadaMinutos) {
        this.tempoRestanteAteChegadaMinutos = tempoRestanteAteChegadaMinutos;
    }

    public Long getTempoAposChegadaMinutos() {
        return tempoAposChegadaMinutos;
    }

    public void setTempoAposChegadaMinutos(Long tempoAposChegadaMinutos) {
        this.tempoAposChegadaMinutos = tempoAposChegadaMinutos;
    }

    public Integer getSlaMinutos() {
        return slaMinutos;
    }

    public void setSlaMinutos(Integer slaMinutos) {
        this.slaMinutos = slaMinutos;
    }

    public Long getTempoRestanteMinutos() {
        return tempoRestanteMinutos;
    }

    public void setTempoRestanteMinutos(Long tempoRestanteMinutos) {
        this.tempoRestanteMinutos = tempoRestanteMinutos;
    }

    public Boolean getSlaEmRisco() {
        return slaEmRisco;
    }

    public void setSlaEmRisco(Boolean slaEmRisco) {
        this.slaEmRisco = slaEmRisco;
    }

    public Boolean getSlaExcedido() {
        return slaExcedido;
    }

    public void setSlaExcedido(Boolean slaExcedido) {
        this.slaExcedido = slaExcedido;
    }

    public Boolean getFoiDespachada() {
        return foiDespachada;
    }

    public void setFoiDespachada(Boolean foiDespachada) {
        this.foiDespachada = foiDespachada;
    }

    public Boolean getChegouLocal() {
        return chegouLocal;
    }

    public void setChegouLocal(Boolean chegouLocal) {
        this.chegouLocal = chegouLocal;
    }

    public Boolean getFoiConcluida() {
        return foiConcluida;
    }

    public void setFoiConcluida(Boolean foiConcluida) {
        this.foiConcluida = foiConcluida;
    }

    public Long getIdAtendimento() {
        return idAtendimento;
    }

    public void setIdAtendimento(Long idAtendimento) {
        this.idAtendimento = idAtendimento;
    }

    public String getPlacaAmbulancia() {
        return placaAmbulancia;
    }

    public void setPlacaAmbulancia(String placaAmbulancia) {
        this.placaAmbulancia = placaAmbulancia;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public String getTempoTotalFormatado() {
        return tempoTotalFormatado;
    }

    public void setTempoTotalFormatado(String tempoTotalFormatado) {
        this.tempoTotalFormatado = tempoTotalFormatado;
    }

    public String getTempoRestanteFormatado() {
        return tempoRestanteFormatado;
    }

    public void setTempoRestanteFormatado(String tempoRestanteFormatado) {
        this.tempoRestanteFormatado = tempoRestanteFormatado;
    }

    public Long getTempoRetornoMinutos() {
        return tempoRetornoMinutos;
    }

    public void setTempoRetornoMinutos(Long tempoRetornoMinutos) {
        this.tempoRetornoMinutos = tempoRetornoMinutos;
    }

    public Long getTempoSlaDecorridoMinutos() {
        return tempoSlaDecorridoMinutos;
    }

    public void setTempoSlaDecorridoMinutos(Long tempoSlaDecorridoMinutos) {
        this.tempoSlaDecorridoMinutos = tempoSlaDecorridoMinutos;
    }

    public LocalDateTime getDataHoraRetorno() {
        return dataHoraRetorno;
    }

    public void setDataHoraRetorno(LocalDateTime dataHoraRetorno) {
        this.dataHoraRetorno = dataHoraRetorno;
    }

    public Long getTempoRetornoDecorridoMinutos() {
        return tempoRetornoDecorridoMinutos;
    }

    public void setTempoRetornoDecorridoMinutos(Long tempoRetornoDecorridoMinutos) {
        this.tempoRetornoDecorridoMinutos = tempoRetornoDecorridoMinutos;
    }

    public Boolean getRetornouBase() {
        return retornouBase;
    }

    public void setRetornouBase(Boolean retornouBase) {
        this.retornouBase = retornouBase;
    }

    public Long getTempoRestanteRetornoMinutos() {
        return tempoRestanteRetornoMinutos;
    }

    public void setTempoRestanteRetornoMinutos(Long tempoRestanteRetornoMinutos) {
        this.tempoRestanteRetornoMinutos = tempoRestanteRetornoMinutos;
    }
}

