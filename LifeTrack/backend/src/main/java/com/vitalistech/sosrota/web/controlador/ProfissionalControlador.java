package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Equipe;
import com.vitalistech.sosrota.dominio.modelo.Profissional;
import com.vitalistech.sosrota.dominio.modelo.StatusProfissional;
import com.vitalistech.sosrota.dominio.modelo.Turno;
import com.vitalistech.sosrota.dominio.repositorio.EquipeRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.ProfissionalRepositorio;
import com.vitalistech.sosrota.web.dto.EditarProfissionalDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<Map<String, Object>> listar(
            @RequestParam(required = false) Turno turno,
            @RequestParam(required = false) StatusProfissional status) {
        
        List<Profissional> profissionais;
        if (turno != null && status != null) {
            profissionais = profissionalRepositorio.findByTurnoAndStatusAndAtivoTrue(turno, status);
        } else if (status != null) {
            profissionais = profissionalRepositorio.findByStatusAndAtivoTrueOrderByNome(status);
        } else {
            profissionais = profissionalRepositorio.findAll();
        }
        
        // Enriquecer com informações de ambulância
        return profissionais.stream().map(prof -> {
            Map<String, Object> profissionalMap = new HashMap<>();
            profissionalMap.put("id", prof.getId());
            profissionalMap.put("nome", prof.getNome());
            profissionalMap.put("funcao", prof.getFuncao() != null ? prof.getFuncao().name() : null);
            profissionalMap.put("contato", prof.getContato());
            profissionalMap.put("turno", prof.getTurno() != null ? prof.getTurno().name() : null);
            profissionalMap.put("status", prof.getStatus() != null ? prof.getStatus().name() : null);
            profissionalMap.put("ativo", prof.isAtivo());
            
            // Buscar equipe e ambulância vinculada
            Optional<Equipe> equipeOpt = equipeRepositorio.findEquipeAtivaPorProfissional(prof.getId());
            if (equipeOpt.isPresent()) {
                Equipe equipe = equipeOpt.get();
                profissionalMap.put("idEquipe", equipe.getId());
                profissionalMap.put("descricaoEquipe", equipe.getDescricao());
                if (equipe.getAmbulancia() != null) {
                    profissionalMap.put("idAmbulancia", equipe.getAmbulancia().getId());
                    profissionalMap.put("placaAmbulancia", equipe.getAmbulancia().getPlaca());
                    profissionalMap.put("tipoAmbulancia", equipe.getAmbulancia().getTipo() != null ? equipe.getAmbulancia().getTipo().name() : null);
                }
            }
            
            return profissionalMap;
        }).collect(Collectors.toList());
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
    public ResponseEntity<Map<String, Object>> buscarPorId(@PathVariable Long id) {
        Profissional p = profissionalRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));
        
        Map<String, Object> profissionalMap = new HashMap<>();
        profissionalMap.put("id", p.getId());
        profissionalMap.put("nome", p.getNome());
        profissionalMap.put("funcao", p.getFuncao() != null ? p.getFuncao().name() : null);
        profissionalMap.put("contato", p.getContato());
        profissionalMap.put("turno", p.getTurno() != null ? p.getTurno().name() : null);
        profissionalMap.put("status", p.getStatus() != null ? p.getStatus().name() : null);
        profissionalMap.put("ativo", p.isAtivo());
        
        // Buscar equipe e ambulância vinculada
        Optional<Equipe> equipeOpt = equipeRepositorio.findEquipeAtivaPorProfissional(p.getId());
        if (equipeOpt.isPresent()) {
            Equipe equipe = equipeOpt.get();
            profissionalMap.put("idEquipe", equipe.getId());
            profissionalMap.put("descricaoEquipe", equipe.getDescricao());
            if (equipe.getAmbulancia() != null) {
                profissionalMap.put("idAmbulancia", equipe.getAmbulancia().getId());
                profissionalMap.put("placaAmbulancia", equipe.getAmbulancia().getPlaca());
                profissionalMap.put("tipoAmbulancia", equipe.getAmbulancia().getTipo() != null ? equipe.getAmbulancia().getTipo().name() : null);
            }
        }
        
        return ResponseEntity.ok(profissionalMap);
    }

    @PostMapping
    public ResponseEntity<Profissional> cadastrar(@RequestBody @Valid Profissional p) {
        // Normalizar contato: remover formatação e espaços
        if (p.getContato() != null && !p.getContato().trim().isEmpty()) {
            String contatoLimpo = p.getContato().trim().replaceAll("[^0-9]", "");
            
            // Verificar se já existe profissional com o mesmo contato (telefone)
            profissionalRepositorio.findAll().stream()
                .filter(prof -> {
                    String contatoExistente = prof.getContato() != null ? prof.getContato().replaceAll("[^0-9]", "") : "";
                    return contatoExistente.equals(contatoLimpo);
                })
                .findFirst()
                .ifPresent(profExistente -> {
                    throw new IllegalStateException(
                        "Já existe um funcionário cadastrado com o telefone/contato: " + p.getContato());
                });
            
            p.setContato(contatoLimpo);
        }
        
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

        // Não permitir editar funcionário que está em atendimento
        if (p.getStatus() == StatusProfissional.EM_ATENDIMENTO) {
            throw new IllegalStateException(
                "Não é possível editar funcionário que está em atendimento. Finalize o atendimento antes de editar.");
        }

        // Validar se pode alterar status
        if (dto.getStatus() == StatusProfissional.DISPONIVEL && 
            p.getStatus() == StatusProfissional.EM_ATENDIMENTO) {
            // Verificar se está em equipe ativa
            if (equipeRepositorio.profissionalEmEquipeAtiva(id) > 0) {
                throw new IllegalStateException(
                    "Profissional está em uma equipe ativa. Finalize o atendimento antes de alterar o status.");
            }
        }

        // Normalizar contato: remover formatação e espaços
        if (dto.getContato() != null && !dto.getContato().trim().isEmpty()) {
            String contatoLimpo = dto.getContato().trim().replaceAll("[^0-9]", "");
            
            // Verificar se já existe outro profissional com o mesmo contato (telefone)
            profissionalRepositorio.findAll().stream()
                .filter(prof -> !prof.getId().equals(id)) // Excluir o próprio profissional
                .filter(prof -> {
                    String contatoExistente = prof.getContato() != null ? prof.getContato().replaceAll("[^0-9]", "") : "";
                    return contatoExistente.equals(contatoLimpo);
                })
                .findFirst()
                .ifPresent(profExistente -> {
                    throw new IllegalStateException(
                        "Já existe outro funcionário cadastrado com o telefone/contato: " + dto.getContato());
                });
            
            p.setContato(contatoLimpo);
        }

        p.setNome(dto.getNome());
        p.setFuncao(dto.getFuncao());
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

        // Não permitir alterar status de profissional que está em atendimento
        if (p.getStatus() == StatusProfissional.EM_ATENDIMENTO) {
            throw new IllegalStateException(
                "Não é possível alterar status de profissional que está em atendimento. Finalize o atendimento antes.");
        }

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
