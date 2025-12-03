package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalRepositorio extends JpaRepository<Profissional, Long> {
}
