package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Ambulancia;
import com.vitalistech.sosrota.dominio.modelo.StatusAmbulancia;
import com.vitalistech.sosrota.dominio.repositorio.AmbulanciaRepositorio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de gestão de ambulâncias.
 */
@RestController
@RequestMapping("/api/ambulancias")
@CrossOrigin(origins = "*")
public class AmbulanciaControlador {

    private final AmbulanciaRepositorio ambulanciaRepositorio;

    public AmbulanciaControlador(AmbulanciaRepositorio ambulanciaRepositorio) {
        this.ambulanciaRepositorio = ambulanciaRepositorio;
    }

    @GetMapping
    public List<Ambulancia> listar() {
        return ambulanciaRepositorio.findAll();
    }

    @PostMapping
    public ResponseEntity<Ambulancia> cadastrar(@RequestBody @Valid Ambulancia ambulancia) {
        ambulancia.setStatus(StatusAmbulancia.DISPONIVEL);
        ambulancia.setAtiva(true);
        return ResponseEntity.ok(ambulanciaRepositorio.save(ambulancia));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ambulancia> atualizar(@PathVariable Long id,
                                                @RequestBody @Valid Ambulancia dados) {
        Ambulancia a = ambulanciaRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ambulância não encontrada"));
        a.setPlaca(dados.getPlaca());
        a.setTipo(dados.getTipo());
        a.setBairroBase(dados.getBairroBase());
        return ResponseEntity.ok(ambulanciaRepositorio.save(a));
    }

    @PutMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        Ambulancia a = ambulanciaRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ambulância não encontrada"));
        a.setAtiva(true);
        a.setStatus(StatusAmbulancia.DISPONIVEL);
        ambulanciaRepositorio.save(a);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        Ambulancia a = ambulanciaRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ambulância não encontrada"));
        a.setAtiva(false);
        a.setStatus(StatusAmbulancia.INATIVA);
        ambulanciaRepositorio.save(a);
        return ResponseEntity.ok().build();
    }
}
