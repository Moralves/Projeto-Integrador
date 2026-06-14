package com.vitalistech.sosrota.padroes.decorator;

import com.vitalistech.sosrota.dominio.modelo.Gravidade;

/**
 * Padrão Decorator: Componente Base. Define a interface para objetos
 * que podem ter responsabilidades adicionadas dinamicamente.
 */
public interface CalculadorSla {
    int calcularSla(Gravidade gravidade);
}
