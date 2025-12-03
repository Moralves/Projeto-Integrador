package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Ambulancia;
import com.vitalistech.sosrota.dominio.modelo.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EquipeRepositorio extends JpaRepository<Equipe, Long> {

    Optional<Equipe> findByAmbulanciaAndAtivaTrue(Ambulancia ambulancia);

    @Query("SELECT COUNT(ep) FROM EquipeProfissional ep WHERE ep.profissional.id = :idProfissional AND ep.equipe.ativa = true")
    long profissionalEmEquipeAtiva(Long idProfissional);

    @Query("SELECT e FROM Equipe e WHERE e.ativa = true AND e.ambulancia.status = com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia.DISPONIVEL")
    List<Equipe> equipesDisponiveis();
}
