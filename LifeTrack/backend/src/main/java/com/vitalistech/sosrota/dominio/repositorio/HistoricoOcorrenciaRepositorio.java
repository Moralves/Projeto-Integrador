package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.HistoricoOcorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para operações de banco de dados relacionadas ao histórico de ocorrências.
 */
@Repository
public interface HistoricoOcorrenciaRepositorio extends JpaRepository<HistoricoOcorrencia, Long> {

    /**
     * Busca todo o histórico de uma ocorrência específica, ordenado por data/hora (mais recente primeiro).
     */
    List<HistoricoOcorrencia> findByOcorrenciaIdOrderByDataHoraDesc(Long ocorrenciaId);

    /**
     * Busca todo o histórico de ações realizadas por um usuário específico, ordenado por data/hora (mais recente primeiro).
     */
    List<HistoricoOcorrencia> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId);

    /**
     * Busca histórico de ocorrências com filtros opcionais.
     * Permite buscar por usuário, ocorrência ou ambos.
     */
    @Query("SELECT h FROM HistoricoOcorrencia h WHERE " +
           "(:usuarioId IS NULL OR h.usuario.id = :usuarioId) AND " +
           "(:ocorrenciaId IS NULL OR h.ocorrencia.id = :ocorrenciaId) " +
           "ORDER BY h.dataHora DESC")
    List<HistoricoOcorrencia> buscarComFiltros(@Param("usuarioId") Long usuarioId, 
                                                @Param("ocorrenciaId") Long ocorrenciaId);

    /**
     * Conta quantos registros de histórico existem para uma ocorrência.
     */
    long countByOcorrenciaId(Long ocorrenciaId);

    /**
     * Conta quantos registros de histórico existem para um usuário.
     */
    long countByUsuarioId(Long usuarioId);
}

