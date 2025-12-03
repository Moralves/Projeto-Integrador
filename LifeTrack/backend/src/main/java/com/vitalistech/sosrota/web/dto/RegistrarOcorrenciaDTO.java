package com.vitalistech.sosrota.web.dto;

import com.vitalistech.sosrota.dominio.modelo.Gravidade;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para registro de uma nova ocorrência.
 */
public class RegistrarOcorrenciaDTO {

    @NotNull
    private Long idBairroLocal;

    @NotNull
    private String tipoOcorrencia;

    @NotNull
    private Gravidade gravidade;

    private String observacoes;

    public Long getIdBairroLocal() {
        return idBairroLocal;
    }

    public void setIdBairroLocal(Long idBairroLocal) {
        this.idBairroLocal = idBairroLocal;
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

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
