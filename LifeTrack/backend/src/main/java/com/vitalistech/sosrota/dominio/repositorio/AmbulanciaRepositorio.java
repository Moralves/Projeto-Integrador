package com.vitalistech.sosrota.dominio.repositorio;

import com.vitalistech.sosrota.dominio.modelo.Ambulancia;
import com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia;
import com.vitalistech.sosrota.dominio.modelo.TipoAmbulancia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmbulanciaRepositorio extends JpaRepository<Ambulancia, Long> {

    List<Ambulancia> findByTipoAndStatusAndAtivaTrue(TipoAmbulancia tipo, StatusAmbulancia status);

    List<Ambulancia> findByStatusAndAtivaTrue(StatusAmbulancia status);
}
