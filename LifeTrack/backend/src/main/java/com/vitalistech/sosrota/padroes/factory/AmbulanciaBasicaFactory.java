package com.vitalistech.sosrota.padroes.factory;

import com.vitalistech.sosrota.dominio.modelo.Ambulancia;
import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia;
import com.vitalistech.sosrota.dominio.modelo.TipoAmbulancia;

/**
 * Padrão Factory Method: Fábrica concreta para criar Ambulâncias de Suporte Básico.
 */
public class AmbulanciaBasicaFactory implements AmbulanciaFactory {

    @Override
    public Ambulancia criarAmbulancia(String placa, Bairro bairroBase) {
        Ambulancia ambulancia = new Ambulancia();
        ambulancia.setPlaca(placa);
        ambulancia.setBairroBase(bairroBase);
        ambulancia.setTipo(TipoAmbulancia.BASICA);
        ambulancia.setStatus(StatusAmbulancia.DISPONIVEL);
        ambulancia.setAtiva(true);
        return ambulancia;
    }
}
