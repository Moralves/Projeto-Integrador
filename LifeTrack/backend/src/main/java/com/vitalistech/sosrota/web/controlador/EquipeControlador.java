package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Equipe;
import com.vitalistech.sosrota.dominio.repositorio.EquipeRepositorio;
import com.vitalistech.sosrota.dominio.servico.EquipeServico;
import com.vitalistech.sosrota.web.dto.CriarEquipeDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de gestão de equipes.
 */
@RestController
@RequestMapping("/api/equipes")
@CrossOrigin(origins = "*")
public class EquipeControlador {

    private final EquipeRepositorio equipeRepositorio;
    private final EquipeServico equipeServico;

    public EquipeControlador(EquipeRepositorio equipeRepositorio, EquipeServico equipeServico) {
        this.equipeRepositorio = equipeRepositorio;
        this.equipeServico = equipeServico;
    }

    @GetMapping
    public List<Equipe> listar() {
        return equipeRepositorio.findAll();
    }

    @PostMapping
    public ResponseEntity<Equipe> criar(@RequestBody @Valid CriarEquipeDTO dto) {
        Equipe equipe = equipeServico.criarEquipe(
                dto.getIdAmbulancia(),
                dto.getDescricao(),
                dto.getIdsProfissionais()
        );
        return ResponseEntity.ok(equipe);
    }

    @GetMapping("/disponiveis")
    public List<Equipe> equipesDisponiveis() {
        return equipeRepositorio.equipesDisponiveis();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipe> atualizar(@PathVariable Long id, @RequestBody @Valid CriarEquipeDTO dto) {
        Equipe equipe = equipeServico.atualizarEquipe(
                id,
                dto.getDescricao(),
                dto.getIdsProfissionais()
        );
        return ResponseEntity.ok(equipe);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Boolean> verificarStatus(@PathVariable Long id) {
        boolean emAtendimento = equipeServico.equipeEmAtendimento(id);
        return ResponseEntity.ok(emAtendimento);
    }

    @DeleteMapping("/por-ambulancia/{idAmbulancia}")
    public ResponseEntity<Void> removerEquipePorAmbulancia(@PathVariable Long idAmbulancia) {
        equipeServico.removerEquipePorAmbulancia(idAmbulancia);
        return ResponseEntity.ok().build();
    }
}
