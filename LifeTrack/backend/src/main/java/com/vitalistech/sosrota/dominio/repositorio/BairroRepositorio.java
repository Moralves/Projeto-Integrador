package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BairroRepositorio extends JpaRepository<Bairro, Long> {

    Bairro findByNome(String nome);
}
