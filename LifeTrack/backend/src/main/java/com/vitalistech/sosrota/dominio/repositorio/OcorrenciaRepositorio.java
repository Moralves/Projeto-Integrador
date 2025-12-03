package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.Gravidade;
import com.vitalistech.sosrota.dominio.modelo.Ocorrencia;
import com.vitalistech.sosrota.dominio.modelo.StatusOcorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcorrenciaRepositorio extends JpaRepository<Ocorrencia, Long> {

    List<Ocorrencia> findByStatus(StatusOcorrencia status);

    List<Ocorrencia> findByGravidade(Gravidade gravidade);

    List<Ocorrencia> findByBairroLocal(Bairro bairro);
}
