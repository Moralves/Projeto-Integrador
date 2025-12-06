package com.vitalistech.sosrota.web.dto;

/**
 * DTO para representar uma ambulância sugerida para uma ocorrência.
 */
public class AmbulanciaSugeridaDTO {

    private Long id;
    private String placa;
    private String tipo;
    private String bairroBase;
    private Double distanciaKm;
    private Integer tempoEstimadoMinutos;
    private Boolean dentroSLA;
    private Boolean equipeCompleta;
    private String statusEquipe;
    private Integer slaMinutos;

    public AmbulanciaSugeridaDTO() {
    }

    public AmbulanciaSugeridaDTO(Long id, String placa, String tipo, String bairroBase,
                                 Double distanciaKm, Integer tempoEstimadoMinutos,
                                 Boolean dentroSLA, Boolean equipeCompleta,
                                 String statusEquipe, Integer slaMinutos) {
        this.id = id;
        this.placa = placa;
        this.tipo = tipo;
        this.bairroBase = bairroBase;
        this.distanciaKm = distanciaKm;
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
        this.dentroSLA = dentroSLA;
        this.equipeCompleta = equipeCompleta;
        this.statusEquipe = statusEquipe;
        this.slaMinutos = slaMinutos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getBairroBase() {
        return bairroBase;
    }

    public void setBairroBase(String bairroBase) {
        this.bairroBase = bairroBase;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Integer getTempoEstimadoMinutos() {
        return tempoEstimadoMinutos;
    }

    public void setTempoEstimadoMinutos(Integer tempoEstimadoMinutos) {
        this.tempoEstimadoMinutos = tempoEstimadoMinutos;
    }

    public Boolean getDentroSLA() {
        return dentroSLA;
    }

    public void setDentroSLA(Boolean dentroSLA) {
        this.dentroSLA = dentroSLA;
    }

    public Boolean getEquipeCompleta() {
        return equipeCompleta;
    }

    public void setEquipeCompleta(Boolean equipeCompleta) {
        this.equipeCompleta = equipeCompleta;
    }

    public String getStatusEquipe() {
        return statusEquipe;
    }

    public void setStatusEquipe(String statusEquipe) {
        this.statusEquipe = statusEquipe;
    }

    public Integer getSlaMinutos() {
        return slaMinutos;
    }

    public void setSlaMinutos(Integer slaMinutos) {
        this.slaMinutos = slaMinutos;
    }
}





