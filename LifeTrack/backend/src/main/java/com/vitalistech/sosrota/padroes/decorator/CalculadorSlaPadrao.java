package com.vitalistech.sosrota.padroes.decorator;

import com.vitalistech.sosrota.dominio.modelo.Gravidade;

/**
 * Padrão Decorator: Componente Concreto. Define o comportamento básico
 * ao qual novas responsabilidades podem ser adicionadas.
 */
public class CalculadorSlaPadrao implements CalculadorSla {

    @Override
    public int calcularSla(Gravidade gravidade) {
        if (gravidade == Gravidade.ALTA) {
            return 8;
        } else if (gravidade == Gravidade.MEDIA) {
            return 15;
        } else {
            return 30; // BAIXA
        }
    }
}
