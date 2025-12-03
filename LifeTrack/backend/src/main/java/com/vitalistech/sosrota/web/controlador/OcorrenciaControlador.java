package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Atendimento;
import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.Ocorrencia;
import com.vitalistech.sosrota.dominio.modelo.Usuario;
import com.vitalistech.sosrota.dominio.repositorio.BairroRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.OcorrenciaRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.UsuarioRepositorio;
import com.vitalistech.sosrota.dominio.servico.OcorrenciaServico;
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

    public OcorrenciaControlador(OcorrenciaServico ocorrenciaServico,
                                 BairroRepositorio bairroRepositorio,
                                 OcorrenciaRepositorio ocorrenciaRepositorio,
                                 UsuarioRepositorio usuarioRepositorio) {
        this.ocorrenciaServico = ocorrenciaServico;
        this.bairroRepositorio = bairroRepositorio;
        this.ocorrenciaRepositorio = ocorrenciaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @GetMapping
    public List<Ocorrencia> listar() {
        return ocorrenciaRepositorio.findAll();
    }

    @PostMapping
    public ResponseEntity<Ocorrencia> registrar(
            @RequestBody @Valid RegistrarOcorrenciaDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        Bairro bairroLocal = bairroRepositorio.findById(dto.getIdBairroLocal())
                .orElseThrow(() -> new IllegalArgumentException("Bairro não encontrado"));

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
}
