package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AtendimentoRepositorio extends JpaRepository<Atendimento, Long> {

    @Query("SELECT a FROM Atendimento a WHERE a.dataHoraDespacho BETWEEN :inicio AND :fim")
    List<Atendimento> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT AVG(a.distanciaKm) FROM Atendimento a")
    Double mediaDistancia();
}
