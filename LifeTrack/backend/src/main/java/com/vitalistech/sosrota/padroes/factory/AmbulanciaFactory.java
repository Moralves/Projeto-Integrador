package com.vitalistech.sosrota.padroes.factory;

import com.vitalistech.sosrota.dominio.modelo.Ambulancia;
import com.vitalistech.sosrota.dominio.modelo.Bairro;

/**
 * Padrão Factory Method: Define uma interface para criar um objeto, 
 * mas deixa as subclasses decidirem qual classe instanciar.
 */
public interface AmbulanciaFactory {
    Ambulancia criarAmbulancia(String placa, Bairro bairroBase);
}
