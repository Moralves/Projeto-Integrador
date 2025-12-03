package com.vitalistech.sosrota.util;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.RuaConexao;

import java.util.*;

/**
 * Implementação do algoritmo de Dijkstra para calcular a menor rota
 * entre dois bairros utilizando as conexões viárias cadastradas.
 */
public class AlgoritmoDijkstra {

    /**
     * Calcula a menor rota entre dois bairros.
     *
     * @param bairroOrigem   bairro de partida
     * @param bairroDestino  bairro de chegada
     * @param todasConexoes  lista de todas as conexões (arestas)
     * @return ResultadoRota contendo distância total e caminho; se não houver caminho, distanciaKm = POSITIVE_INFINITY
     */
    public static ResultadoRota calcularRota(Bairro bairroOrigem,
                                             Bairro bairroDestino,
                                             List<RuaConexao> todasConexoes) {

        Map<Long, List<RuaConexao>> adj = new HashMap<>();
        Map<Long, Bairro> mapaBairros = new HashMap<>();

        for (RuaConexao c : todasConexoes) {
            mapaBairros.put(c.getBairroOrigem().getId(), c.getBairroOrigem());
            mapaBairros.put(c.getBairroDestino().getId(), c.getBairroDestino());

            adj.computeIfAbsent(c.getBairroOrigem().getId(), k -> new ArrayList<>()).add(c);

            RuaConexao reversa = new RuaConexao();
            reversa.setBairroOrigem(c.getBairroDestino());
            reversa.setBairroDestino(c.getBairroOrigem());
            reversa.setDistanciaKm(c.getDistanciaKm());
            adj.computeIfAbsent(reversa.getBairroOrigem().getId(), k -> new ArrayList<>()).add(reversa);
        }

        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Long> anterior = new HashMap<>();
        Set<Long> visitados = new HashSet<>();

        for (Long id : mapaBairros.keySet()) {
            dist.put(id, Double.POSITIVE_INFINITY);
        }

        dist.put(bairroOrigem.getId(), 0.0);

        PriorityQueue<long[]> fila = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        fila.offer(new long[]{bairroOrigem.getId(), 0L});

        while (!fila.isEmpty()) {
            long[] atual = fila.poll();
            Long idAtual = atual[0];

            if (visitados.contains(idAtual)) continue;
            visitados.add(idAtual);

            if (idAtual.equals(bairroDestino.getId())) break;

            List<RuaConexao> vizinhos = adj.getOrDefault(idAtual, List.of());
            for (RuaConexao c : vizinhos) {
                Long idVizinho = c.getBairroDestino().getId();
                if (visitados.contains(idVizinho)) continue;

                double novaDist = dist.get(idAtual) + c.getDistanciaKm();
                if (novaDist < dist.getOrDefault(idVizinho, Double.POSITIVE_INFINITY)) {
                    dist.put(idVizinho, novaDist);
                    anterior.put(idVizinho, idAtual);
                    fila.offer(new long[]{idVizinho, (long) novaDist});
                }
            }
        }

        double distanciaFinal = dist.getOrDefault(bairroDestino.getId(), Double.POSITIVE_INFINITY);
        if (Double.isInfinite(distanciaFinal)) {
            return new ResultadoRota(Double.POSITIVE_INFINITY, List.of());
        }

        List<Bairro> caminho = new ArrayList<>();
        Long atual = bairroDestino.getId();
        while (atual != null) {
            Bairro b = mapaBairros.get(atual);
            if (b != null) {
                caminho.add(b);
            }
            atual = anterior.get(atual);
        }
        Collections.reverse(caminho);

        return new ResultadoRota(distanciaFinal, caminho);
    }
}
