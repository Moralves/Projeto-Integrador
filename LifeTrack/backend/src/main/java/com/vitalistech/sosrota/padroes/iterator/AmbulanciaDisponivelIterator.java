package com.vitalistech.sosrota.padroes.iterator;

import com.vitalistech.sosrota.dominio.modelo.Ambulancia;
import com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Padrão Iterator: Iterador concreto que percorre a frota de ambulâncias
 * e retorna apenas aquelas que estão com status DISPONIVEL e ativas.
 */
public class AmbulanciaDisponivelIterator implements Iterator<Ambulancia> {

    private List<Ambulancia> ambulancias;
    private int posicaoAtual = 0;

    public AmbulanciaDisponivelIterator(List<Ambulancia> ambulancias) {
        this.ambulancias = ambulancias;
    }

    @Override
    public boolean hasNext() {
        while (posicaoAtual < ambulancias.size()) {
            Ambulancia ambulancia = ambulancias.get(posicaoAtual);
            if (ambulancia.isAtiva() && ambulancia.getStatus() == StatusAmbulancia.DISPONIVEL) {
                return true;
            }
            posicaoAtual++;
        }
        return false;
    }

    @Override
    public Ambulancia next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Ambulancia ambulancia = ambulancias.get(posicaoAtual);
        posicaoAtual++;
        return ambulancia;
    }
}
