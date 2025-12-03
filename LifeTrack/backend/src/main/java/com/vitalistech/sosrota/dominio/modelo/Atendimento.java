package com.vitalistech.sosrota.dominio.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Registro do atendimento realizado para uma ocorrência.
 */
@Entity
@Table(name = "atendimentos")
public class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_ocorrencia")
    private Ocorrencia ocorrencia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_ambulancia")
    private Ambulancia ambulancia;

    private LocalDateTime dataHoraDespacho;

    private LocalDateTime dataHoraChegada;

    private Double distanciaKm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(Ocorrencia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    public Ambulancia getAmbulancia() {
        return ambulancia;
    }

    public void setAmbulancia(Ambulancia ambulancia) {
        this.ambulancia = ambulancia;
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

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }
}
