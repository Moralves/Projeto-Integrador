package com.vitalistech.sosrota.padroes.iterator;

import com.vitalistech.sosrota.dominio.modelo.Ambulancia;

import java.util.Iterator;
import java.util.List;

/**
 * Padrão Iterator: Representa a coleção (Aggregate) que será iterada.
 */
public class FrotaAmbulancias implements Iterable<Ambulancia> {

    private List<Ambulancia> ambulancias;

    public FrotaAmbulancias(List<Ambulancia> ambulancias) {
        this.ambulancias = ambulancias;
    }

    @Override
    public Iterator<Ambulancia> iterator() {
        // Retorna o iterador especializado
        return new AmbulanciaDisponivelIterator(this.ambulancias);
    }
}
