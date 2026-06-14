package com.vitalistech.sosrota.padroes.adapter;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.RuaConexao;
import com.vitalistech.sosrota.util.AlgoritmoDijkstra;
import com.vitalistech.sosrota.util.ResultadoRota;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Padrão Adapter: Adapta a classe utilitária (estática) existente AlgoritmoDijkstra
 * para a interface CalculadorDistanciaPort, permitindo a futura injeção de outras 
 * implementações (ex: API do Google Maps).
 */
@Component
public class AlgoritmoDijkstraAdapter implements CalculadorDistanciaPort {

    @Override
    public ResultadoRota calcularRota(Bairro origem, Bairro destino, List<RuaConexao> conexoes) {
        // Delega a chamada para a implementação subjacente (Adaptee)
        return AlgoritmoDijkstra.calcularRota(origem, destino, conexoes);
    }
}
