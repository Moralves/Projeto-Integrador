package com.vitalistech.sosrota.padroes.decorator;

import com.vitalistech.sosrota.dominio.modelo.Gravidade;

/**
 * Padrão Decorator: Decorator Base. Mantém uma referência para um objeto 
 * Componente e define uma interface que conforma com a interface Componente.
 */
public abstract class CalculadorSlaDecorator implements CalculadorSla {

    protected CalculadorSla calculadorBase;

    public CalculadorSlaDecorator(CalculadorSla calculadorBase) {
        this.calculadorBase = calculadorBase;
    }

    @Override
    public int calcularSla(Gravidade gravidade) {
        return calculadorBase.calcularSla(gravidade);
    }
}
