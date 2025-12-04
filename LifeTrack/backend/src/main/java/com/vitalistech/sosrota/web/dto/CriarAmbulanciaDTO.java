package com.vitalistech.sosrota.web.dto;

import com.vitalistech.sosrota.dominio.modelo.TipoAmbulancia;
import jakarta.validation.constraints.NotNull;

public class CriarAmbulanciaDTO {

    @NotNull
    private String placa;

    @NotNull
    private TipoAmbulancia tipo;

    @NotNull
    private Long idBairroBase;

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

    public Long getIdBairroBase() {
        return idBairroBase;
    }

    public void setIdBairroBase(Long idBairroBase) {
        this.idBairroBase = idBairroBase;
    }
}


