package com.vitalistech.sosrota.padroes.template;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Padrão Template Method: Define o esqueleto de um algoritmo em uma operação,
 * delegando alguns passos para as subclasses. Permite que subclasses redefinam 
 * certos passos de um algoritmo sem alterar a estrutura do mesmo.
 * 
 * @param <T> Tipo do dado original (Ex: Entidade do banco de dados)
 * @param <DTO> Tipo do dado de saída do relatório (Ex: DTO de visualização)
 */
public abstract class GeradorRelatorioTemplate<T, DTO> {

    // Método Template - final para não ser sobrescrito
    public final List<DTO> gerarRelatorio() {
        List<T> dados = buscarDados();
        return dados.stream()
                .map(this::processarDado)
                .collect(Collectors.toList());
    }

    // Passos a serem implementados pelas subclasses concretas
    protected abstract List<T> buscarDados();
    
    protected abstract DTO processarDado(T dado);
}
