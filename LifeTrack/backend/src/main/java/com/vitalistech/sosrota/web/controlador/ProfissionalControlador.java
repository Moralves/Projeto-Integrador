package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Profissional;
import com.vitalistech.sosrota.dominio.modelo.StatusProfissional;
import com.vitalistech.sosrota.dominio.modelo.Turno;
import com.vitalistech.sosrota.dominio.repositorio.EquipeRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.ProfissionalRepositorio;
import com.vitalistech.sosrota.web.dto.EditarProfissionalDTO;
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
    private final EquipeRepositorio equipeRepositorio;

    public ProfissionalControlador(ProfissionalRepositorio profissionalRepositorio,
                                   EquipeRepositorio equipeRepositorio) {
        this.profissionalRepositorio = profissionalRepositorio;
        this.equipeRepositorio = equipeRepositorio;
    }

    @GetMapping
    public List<Profissional> listar(
            @RequestParam(required = false) Turno turno,
            @RequestParam(required = false) StatusProfissional status) {
        
        if (turno != null && status != null) {
            return profissionalRepositorio.findByTurnoAndStatusAndAtivoTrue(turno, status);
        } else if (status != null) {
            return profissionalRepositorio.findByStatusAndAtivoTrueOrderByNome(status);
        } else {
            return profissionalRepositorio.findAll();
        }
    }

    @GetMapping("/disponiveis")
    public List<Profissional> listarDisponiveis(@RequestParam(required = false) Turno turno) {
        if (turno != null) {
            return profissionalRepositorio.findByTurnoAndStatusAndAtivoTrue(turno, StatusProfissional.DISPONIVEL);
        } else {
            return profissionalRepositorio.findByStatusAndAtivoTrueOrderByNome(StatusProfissional.DISPONIVEL);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profissional> buscarPorId(@PathVariable Long id) {
        Profissional p = profissionalRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));
        return ResponseEntity.ok(p);
    }

    @PostMapping
    public ResponseEntity<Profissional> cadastrar(@RequestBody @Valid Profissional p) {
        p.setAtivo(true);
        if (p.getStatus() == null) {
            p.setStatus(StatusProfissional.DISPONIVEL);
        }
        return ResponseEntity.ok(profissionalRepositorio.save(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profissional> editar(@PathVariable Long id, 
                                             @RequestBody @Valid EditarProfissionalDTO dto) {
        Profissional p = profissionalRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        // Validar se pode alterar status
        if (dto.getStatus() == StatusProfissional.DISPONIVEL && 
            p.getStatus() == StatusProfissional.EM_ATENDIMENTO) {
            // Verificar se está em equipe ativa
            if (equipeRepositorio.profissionalEmEquipeAtiva(id) > 0) {
                throw new IllegalStateException(
                    "Profissional está em uma equipe ativa. Finalize o atendimento antes de alterar o status.");
            }
        }

        p.setNome(dto.getNome());
        p.setFuncao(dto.getFuncao());
        p.setContato(dto.getContato());
        p.setTurno(dto.getTurno());
        p.setStatus(dto.getStatus());
        p.setAtivo(dto.getAtivo());

        return ResponseEntity.ok(profissionalRepositorio.save(p));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Profissional> alterarStatus(@PathVariable Long id,
                                                     @RequestParam StatusProfissional status) {
        Profissional p = profissionalRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        // Validar transição de status
        if (status == StatusProfissional.DISPONIVEL && 
            p.getStatus() == StatusProfissional.EM_ATENDIMENTO) {
            if (equipeRepositorio.profissionalEmEquipeAtiva(id) > 0) {
                throw new IllegalStateException(
                    "Profissional está em uma equipe ativa. Finalize o atendimento antes de alterar o status.");
            }
        }

        p.setStatus(status);
        return ResponseEntity.ok(profissionalRepositorio.save(p));
    }

    @PutMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        Profissional p = profissionalRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));
        
        if (p.getStatus() == StatusProfissional.EM_ATENDIMENTO) {
            throw new IllegalStateException("Não é possível desativar profissional em atendimento.");
        }
        
        p.setAtivo(false);
        profissionalRepositorio.save(p);
        return ResponseEntity.ok().build();
    }
}
