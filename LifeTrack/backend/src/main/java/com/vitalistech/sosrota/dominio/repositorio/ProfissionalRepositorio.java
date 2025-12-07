package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Profissional;
import com.vitalistech.sosrota.dominio.modelo.StatusProfissional;
import com.vitalistech.sosrota.dominio.modelo.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfissionalRepositorio extends JpaRepository<Profissional, Long> {
    
    /**
     * Busca profissionais disponíveis (não em atendimento e ativos).
     */
    List<Profissional> findByStatusAndAtivoTrue(StatusProfissional status);
    
    /**
     * Busca profissionais por turno e status.
     */
    List<Profissional> findByTurnoAndStatusAndAtivoTrue(Turno turno, StatusProfissional status);
    
    /**
     * Busca profissionais ativos disponíveis.
     */
    List<Profissional> findByStatusAndAtivoTrueOrderByNome(StatusProfissional status);
}
