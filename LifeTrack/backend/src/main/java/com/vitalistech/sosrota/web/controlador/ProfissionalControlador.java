package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Profissional;
import com.vitalistech.sosrota.dominio.repositorio.ProfissionalRepositorio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de gestão de profissionais.
 */
@RestController
@RequestMapping("/api/profissionais")
@CrossOrigin(origins = "*")
public class ProfissionalControlador {

    private final ProfissionalRepositorio profissionalRepositorio;

    public ProfissionalControlador(ProfissionalRepositorio profissionalRepositorio) {
        this.profissionalRepositorio = profissionalRepositorio;
    }

    @GetMapping
    public List<Profissional> listar() {
        return profissionalRepositorio.findAll();
    }

    @PostMapping
    public ResponseEntity<Profissional> cadastrar(@RequestBody @Valid Profissional p) {
        p.setAtivo(true);
        return ResponseEntity.ok(profissionalRepositorio.save(p));
    }

    @PutMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        Profissional p = profissionalRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));
        p.setAtivo(false);
        profissionalRepositorio.save(p);
        return ResponseEntity.ok().build();
    }
}
