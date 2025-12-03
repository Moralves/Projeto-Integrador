package com.vitalistech.sosrota.dominio.modelo;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Equipe de atendimento associada a uma ambulância.
 */
@Entity
@Table(name = "equipes")
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao;

    @OneToOne
    @JoinColumn(name = "id_ambulancia", nullable = false, unique = true)
    private Ambulancia ambulancia;

    @Column(nullable = false)
    private boolean ativa = true;

    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EquipeProfissional> profissionais = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Ambulancia getAmbulancia() {
        return ambulancia;
    }

    public void setAmbulancia(Ambulancia ambulancia) {
        this.ambulancia = ambulancia;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public Set<EquipeProfissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(Set<EquipeProfissional> profissionais) {
        this.profissionais = profissionais;
    }
}
