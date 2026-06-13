package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Profissional;
import com.vitalistech.sosrota.dominio.modelo.StatusProfissional;
import com.vitalistech.sosrota.dominio.modelo.Turno;
import com.vitalistech.sosrota.dominio.repositorio.EquipeRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.ProfissionalRepositorio;
import com.vitalistech.sosrota.web.dto.EditarProfissionalDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Endpoints de gestão de profissionais.
 * 
 * IMPORTANTE: Implementa validações rigorosas para evitar duplicação de profissionais
 * Todos os métodos que modificam dados usam @Transactional
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

    /**
     * Cadastra um novo profissional.
     * 
     * PROTEÇÕES CONTRA DUPLICAÇÃO:
     * - Valida se contato já existe
     * - Normaliza contato (remove formatação)
     * - Usa @Transactional para atomicidade
     * - Inicializa status e ativo com valores padrão
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Profissional> cadastrar(@RequestBody @Valid Profissional p) {
        // Normalizar contato: remover formatação
        if (p.getContato() != null && !p.getContato().trim().isEmpty()) {
            String contatoLimpo = normalizarContato(p.getContato());
            
            // PROTEÇÃO: Verificar se já existe profissional com o mesmo contato
            Optional<Profissional> profissionalExistente = profissionalRepositorio.findAll().stream()
                    .filter(prof -> {
                        String contatoExistente = prof.getContato() != null ? 
                            normalizarContato(prof.getContato()) : "";
                        return contatoExistente.equals(contatoLimpo);
                    })
                    .findFirst();

            if (profissionalExistente.isPresent()) {
                throw new IllegalStateException(
                    "Já existe um funcionário cadastrado com o contato: " + p.getContato() + 
                    " (ID: " + profissionalExistente.get().getId() + ")");
            }

            p.setContato(contatoLimpo);
        }

        // Inicializar valores padrão
        p.setAtivo(true);
        if (p.getStatus() == null) {
            p.setStatus(StatusProfissional.DISPONIVEL);
        }
        if (p.getTurno() == null) {
            p.setTurno(Turno.MANHA);
        }

        return ResponseEntity.ok(profissionalRepositorio.save(p));
    }

    /**
     * Edita um profissional existente.
     * 
     * PROTEÇÕES:
     * - Valida se está em atendimento
     * - Verifica duplicação de contato
     * - Usa @Transactional para atomicidade
     */
    @PutMapping("/{id}")
    @Transactional
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
            if (equipeRepositorio.profissionalEmEquipeAtiva(id) > 0) {
                throw new IllegalStateException(
                    "Profissional está em uma equipe ativa. Finalize o atendimento antes de alterar o status.");
            }
        }

        // Validar contato se foi alterado
        if (dto.getContato() != null && !dto.getContato().trim().isEmpty()) {
            String contatoLimpo = normalizarContato(dto.getContato());
            
            // Verificar se outro profissional já usa este contato
            Optional<Profissional> outroComMesmoContato = profissionalRepositorio.findAll().stream()
                    .filter(prof -> !prof.getId().equals(id) && 
                                   prof.getContato() != null && 
                                   normalizarContato(prof.getContato()).equals(contatoLimpo))
                    .findFirst();

            if (outroComMesmoContato.isPresent()) {
                throw new IllegalStateException(
                    "Já existe outro funcionário cadastrado com o contato: " + dto.getContato());
            }

            p.setContato(contatoLimpo);
        }

        p.setNome(dto.getNome());
        p.setFuncao(dto.getFuncao());
        p.setTurno(dto.getTurno());
        p.setStatus(dto.getStatus());
        p.setAtivo(dto.getAtivo());

        return ResponseEntity.ok(profissionalRepositorio.save(p));
    }

    /**
     * Altera o status de um profissional.
     */
    @PutMapping("/{id}/status")
    @Transactional
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

    /**
     * Desativa um profissional.
     */
    @PutMapping("/{id}/desativar")
    @Transactional
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

    /**
     * Normaliza o contato removendo caracteres especiais e espaços.
     * Exemplo: "(11) 99999-1111" -> "11999991111"
     * 
     * @param contato O contato a ser normalizado
     * @return Contato normalizado contendo apenas dígitos
     */
    private String normalizarContato(String contato) {
        return contato.replaceAll("[^0-9]", "").trim();
    }
}
