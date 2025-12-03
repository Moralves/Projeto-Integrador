package com.vitalistech.sosrota.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO para criação de equipe.
 */
public class CriarEquipeDTO {

    @NotNull
    private Long idAmbulancia;

    @NotNull
    private String descricao;

    @NotNull
    private List<Long> idsProfissionais;

    public Long getIdAmbulancia() {
        return idAmbulancia;
    }

    public void setIdAmbulancia(Long idAmbulancia) {
        this.idAmbulancia = idAmbulancia;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<Long> getIdsProfissionais() {
        return idsProfissionais;
    }

    public void setIdsProfissionais(List<Long> idsProfissionais) {
        this.idsProfissionais = idsProfissionais;
    }
}
