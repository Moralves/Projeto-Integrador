package com.vitalistech.sosrota.dominio.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Representa uma ocorrência de emergência.
 */
@Entity
@Table(name = "ocorrencias")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_bairro_local")
    private Bairro bairroLocal;

    @Column(nullable = false)
    private String tipoOcorrencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gravidade gravidade;

    @Column(nullable = false)
    private LocalDateTime dataHoraAbertura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOcorrencia status;

    @Column(length = 1000)
    private String observacoes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bairro getBairroLocal() {
        return bairroLocal;
    }

    public void setBairroLocal(Bairro bairroLocal) {
        this.bairroLocal = bairroLocal;
    }

    public String getTipoOcorrencia() {
        return tipoOcorrencia;
    }

    public void setTipoOcorrencia(String tipoOcorrencia) {
        this.tipoOcorrencia = tipoOcorrencia;
    }

    public Gravidade getGravidade() {
        return gravidade;
    }

    public void setGravidade(Gravidade gravidade) {
        this.gravidade = gravidade;
    }

    public LocalDateTime getDataHoraAbertura() {
        return dataHoraAbertura;
    }

    public void setDataHoraAbertura(LocalDateTime dataHoraAbertura) {
        this.dataHoraAbertura = dataHoraAbertura;
    }

    public StatusOcorrencia getStatus() {
        return status;
    }

    public void setStatusOcorrencia(StatusOcorrencia status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
