package com.vitalistech.sosrota.dominio.modelo;

import jakarta.persistence.*;

/**
 * Representa uma ligação viária entre dois bairros com uma distância em km.
 */
@Entity

@Table(name = "ruas_conexoes")
public class RuaConexao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_bairro_origem")
    private Bairro bairroOrigem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_bairro_destino")
    private Bairro bairroDestino;

    @Column(nullable = false)
    private Double distanciaKm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bairro getBairroOrigem() {
        return bairroOrigem;
    }

    public void setBairroOrigem(Bairro bairroOrigem) {
        this.bairroOrigem = bairroOrigem;
    }

    public Bairro getBairroDestino() {
        return bairroDestino;
    }

    public void setBairroDestino(Bairro bairroDestino) {
        this.bairroDestino = bairroDestino;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(Double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }
}
