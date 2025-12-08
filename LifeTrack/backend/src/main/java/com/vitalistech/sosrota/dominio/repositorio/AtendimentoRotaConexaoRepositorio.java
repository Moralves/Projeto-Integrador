package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Atendimento;
import com.vitalistech.sosrota.dominio.modelo.AtendimentoRotaConexao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtendimentoRotaConexaoRepositorio extends JpaRepository<AtendimentoRotaConexao, Long> {
    
    /**
     * Busca todas as conexões de rua utilizadas no caminho de um atendimento,
     * ordenadas pela ordem sequencial.
     */
    List<AtendimentoRotaConexao> findByAtendimentoOrderByOrdem(Atendimento atendimento);
    
    /**
     * Remove todas as conexões de um atendimento.
     */
    void deleteByAtendimento(Atendimento atendimento);
}







