package com.vitalistech.sosrota.dominio.modelo;

import jakarta.persistence.*;

/**
 * Tabela de junção entre Equipe e Profissional.
 */
@Entity
@Table(name = "equipes_profissionais")
public class EquipeProfissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_equipe")
    private Equipe equipe;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_profissional")
    private Profissional profissional;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }
}
