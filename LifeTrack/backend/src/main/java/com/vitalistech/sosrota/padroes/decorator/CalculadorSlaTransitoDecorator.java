package com.vitalistech.sosrota.padroes.decorator;

import com.vitalistech.sosrota.dominio.modelo.Gravidade;

/**
 * Padrão Decorator: Decorador Concreto. Adiciona responsabilidades ao componente.
 * Neste caso, adiciona um tempo extra no SLA simulando condições de trânsito intenso.
 */
public class CalculadorSlaTransitoDecorator extends CalculadorSlaDecorator {

    public CalculadorSlaTransitoDecorator(CalculadorSla calculadorBase) {
        super(calculadorBase);
    }

    @Override
    public int calcularSla(Gravidade gravidade) {
        int slaBase = super.calcularSla(gravidade);
        // Adiciona 5 minutos de tolerância por conta do trânsito
        return slaBase + 5;
    }
}
