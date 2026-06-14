package com.vitalistech.sosrota.padroes.adapter;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.RuaConexao;
import com.vitalistech.sosrota.util.ResultadoRota;

import java.util.List;

/**
 * Padrão Adapter: Define a interface esperada (Port) pelo cliente do sistema 
 * para cálculo de distância.
 */
public interface CalculadorDistanciaPort {
    ResultadoRota calcularRota(Bairro origem, Bairro destino, List<RuaConexao> conexoes);
}
