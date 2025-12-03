package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.RuaConexao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuaConexaoRepositorio extends JpaRepository<RuaConexao, Long> {

    List<RuaConexao> findByBairroOrigem(Bairro bairroOrigem);
}
