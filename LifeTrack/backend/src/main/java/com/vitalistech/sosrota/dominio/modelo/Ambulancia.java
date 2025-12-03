package com.vitalistech.sosrota.dominio.modelo;

import jakarta.persistence.*;

/**
 * Representa uma ambulância cadastrada no sistema.
 */
@Entity
@Table(name = "ambulancias")
public class Ambulancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAmbulancia tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAmbulancia status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_bairro_base")
    private Bairro bairroBase;

    @Column(nullable = false)
    private boolean ativa = true;

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

    public TipoAmbulancia getTipo() {
        return tipo;
    }

    public void setTipo(TipoAmbulancia tipo) {
        this.tipo = tipo;
    }

    public StatusAmbulancia getStatus() {
        return status;
    }

    public void setStatus(StatusAmbulancia status) {
        this.status = status;
    }

    public Bairro getBairroBase() {
        return bairroBase;
    }

    public void setBairroBase(Bairro bairroBase) {
        this.bairroBase = bairroBase;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}
