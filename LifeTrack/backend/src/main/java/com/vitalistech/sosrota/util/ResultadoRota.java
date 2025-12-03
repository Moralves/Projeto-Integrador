package com.vitalistech.sosrota.util;

import com.vitalistech.sosrota.dominio.modelo.Bairro;

import java.util.List;

/**
 * Representa o resultado do cálculo de rota:
 * distância total em km e lista ordenada de bairros.
 */
public class ResultadoRota {

    private double distanciaKm;
    private List<Bairro> caminho;

    public ResultadoRota(double distanciaKm, List<Bairro> caminho) {
        this.distanciaKm = distanciaKm;
        this.caminho = caminho;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public List<Bairro> getCaminho() {
        return caminho;
    }

    public void setCaminho(List<Bairro> caminho) {
        this.caminho = caminho;
    }
}
