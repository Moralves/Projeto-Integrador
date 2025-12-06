package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Atendimento;
import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.Ocorrencia;
import com.vitalistech.sosrota.dominio.modelo.Usuario;
import com.vitalistech.sosrota.dominio.repositorio.BairroRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.OcorrenciaRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.UsuarioRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.AtendimentoRepositorio;
import com.vitalistech.sosrota.dominio.servico.OcorrenciaServico;
import com.vitalistech.sosrota.web.dto.AmbulanciaSugeridaDTO;
import com.vitalistech.sosrota.web.dto.RegistrarOcorrenciaDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints para registrar e despachar ocorrências.
 */
@RestController
@RequestMapping("/api/ocorrencias")
@CrossOrigin(origins = "*")
public class OcorrenciaControlador {

    private final OcorrenciaServico ocorrenciaServico;
    private final BairroRepositorio bairroRepositorio;
    private final OcorrenciaRepositorio ocorrenciaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final AtendimentoRepositorio atendimentoRepositorio;

    public OcorrenciaControlador(OcorrenciaServico ocorrenciaServico,
                                 BairroRepositorio bairroRepositorio,
                                 OcorrenciaRepositorio ocorrenciaRepositorio,
                                 UsuarioRepositorio usuarioRepositorio,
                                 AtendimentoRepositorio atendimentoRepositorio) {
        this.ocorrenciaServico = ocorrenciaServico;
        this.bairroRepositorio = bairroRepositorio;
        this.ocorrenciaRepositorio = ocorrenciaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.atendimentoRepositorio = atendimentoRepositorio;
    }

    @GetMapping
    public List<Ocorrencia> listar() {
        return ocorrenciaRepositorio.findAll();
    }

    @PostMapping
    public ResponseEntity<?> registrar(
            @RequestBody @Valid RegistrarOcorrenciaDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (dto.getIdBairroLocal() == null) {
                return ResponseEntity.badRequest().body("ID do bairro é obrigatório");
            }

            Bairro bairroLocal = bairroRepositorio.findById(dto.getIdBairroLocal())
                    .orElseThrow(() -> new IllegalArgumentException("Bairro não encontrado"));

            if (dto.getGravidade() == null) {
                return ResponseEntity.badRequest().body("Gravidade é obrigatória");
            }

            Usuario usuarioRegistro = null;
            if (userId != null) {
                usuarioRegistro = usuarioRepositorio.findById(userId)
                        .orElse(null);
            }

            Ocorrencia ocorrencia = ocorrenciaServico.registrarOcorrencia(
                    bairroLocal,
                    dto.getTipoOcorrencia(),
                    dto.getGravidade(),
                    dto.getObservacoes(),
                    usuarioRegistro
            );

            return ResponseEntity.ok(ocorrencia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/ambulancias-sugeridas")
    public ResponseEntity<List<AmbulanciaSugeridaDTO>> sugerirAmbulancias(@PathVariable Long id) {
        List<AmbulanciaSugeridaDTO> sugestoes = ocorrenciaServico.sugerirAmbulancias(id);
        return ResponseEntity.ok(sugestoes);
    }

    @PostMapping("/{id}/despachar")
    public ResponseEntity<Atendimento> despachar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        Usuario usuarioDespacho = null;
        if (userId != null) {
            usuarioDespacho = usuarioRepositorio.findById(userId)
                    .orElse(null);
        }
        
        Atendimento atendimento = ocorrenciaServico.despacharOcorrencia(id, usuarioDespacho);
        return ResponseEntity.ok(atendimento);
    }

    @PostMapping("/{id}/concluir")
    public ResponseEntity<?> concluir(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            Usuario usuarioConclusao = null;
            if (userId != null) {
                usuarioConclusao = usuarioRepositorio.findById(userId)
                        .orElse(null);
            }
            
            Ocorrencia ocorrencia = ocorrenciaServico.concluirOcorrencia(id, usuarioConclusao);
            return ResponseEntity.ok(ocorrencia);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping("/atendimentos/{idAtendimento}/chegada")
    public ResponseEntity<?> registrarChegada(
            @PathVariable Long idAtendimento,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            Usuario usuarioChegada = null;
            if (userId != null) {
                usuarioChegada = usuarioRepositorio.findById(userId)
                        .orElse(null);
            }
            
            Ocorrencia ocorrencia = ocorrenciaServico.registrarChegadaEFechar(idAtendimento, usuarioChegada);
            return ResponseEntity.ok(ocorrencia);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }
}
