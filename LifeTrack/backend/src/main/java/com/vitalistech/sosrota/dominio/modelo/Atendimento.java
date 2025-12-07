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

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_equipe")
    private Equipe equipe;

    private LocalDateTime dataHoraDespacho;

    private LocalDateTime dataHoraChegada;

    private LocalDateTime dataHoraRetorno;

    private Double distanciaKm;

    @ManyToOne
    @JoinColumn(name = "id_usuario_despacho")
    private Usuario usuarioDespacho;

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

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
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

    public LocalDateTime getDataHoraRetorno() {
        return dataHoraRetorno;
    }

    public void setDataHoraRetorno(LocalDateTime dataHoraRetorno) {
        this.dataHoraRetorno = dataHoraRetorno;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Usuario getUsuarioDespacho() {
        return usuarioDespacho;
    }

    public void setUsuarioDespacho(Usuario usuarioDespacho) {
        this.usuarioDespacho = usuarioDespacho;
    }
}
