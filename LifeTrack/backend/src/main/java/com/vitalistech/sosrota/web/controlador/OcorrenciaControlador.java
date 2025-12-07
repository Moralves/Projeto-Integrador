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
import com.vitalistech.sosrota.web.dto.DespachoResponseDTO;
import com.vitalistech.sosrota.web.dto.RegistrarOcorrenciaDTO;
import com.vitalistech.sosrota.web.dto.TimerOcorrenciaDTO;
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
    public ResponseEntity<?> despachar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            Usuario usuarioDespacho = null;
            if (userId != null) {
                usuarioDespacho = usuarioRepositorio.findById(userId)
                        .orElse(null);
            }
            
            Atendimento atendimento = ocorrenciaServico.despacharOcorrencia(id, usuarioDespacho);
            
            // Obter informações do timer após o despacho
            TimerOcorrenciaDTO timer = ocorrenciaServico.obterInformacoesTimer(id);
            
            // Retornar resposta combinada com atendimento e timer
            DespachoResponseDTO response = new DespachoResponseDTO(atendimento, timer);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
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

    @GetMapping("/{id}/timer")
    public ResponseEntity<?> obterTimer(@PathVariable Long id) {
        try {
            TimerOcorrenciaDTO timer = ocorrenciaServico.obterInformacoesTimer(id);
            return ResponseEntity.ok(timer);
        } catch (IllegalArgumentException e) {
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
            
            // Novo método: registra chegada e muda para EM_ATENDIMENTO (não fecha automaticamente)
            Ocorrencia ocorrencia = ocorrenciaServico.registrarChegada(idAtendimento, usuarioChegada);
            return ResponseEntity.ok(ocorrencia);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping("/atendimentos/{idAtendimento}/chegada-e-fechar")
    public ResponseEntity<?> registrarChegadaEFechar(
            @PathVariable Long idAtendimento,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            Usuario usuarioChegada = null;
            if (userId != null) {
                usuarioChegada = usuarioRepositorio.findById(userId)
                        .orElse(null);
            }
            
            // Método antigo mantido para compatibilidade: registra chegada e fecha automaticamente
            Ocorrencia ocorrencia = ocorrenciaServico.registrarChegadaEFechar(idAtendimento, usuarioChegada);
            return ResponseEntity.ok(ocorrencia);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping("/atendimentos/{idAtendimento}/retorno")
    public ResponseEntity<?> registrarRetorno(
            @PathVariable Long idAtendimento,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            Usuario usuarioRetorno = null;
            if (userId != null) {
                usuarioRetorno = usuarioRepositorio.findById(userId)
                        .orElse(null);
            }
            
            Atendimento atendimento = ocorrenciaServico.registrarRetorno(idAtendimento, usuarioRetorno);
            return ResponseEntity.ok(atendimento);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }
}
