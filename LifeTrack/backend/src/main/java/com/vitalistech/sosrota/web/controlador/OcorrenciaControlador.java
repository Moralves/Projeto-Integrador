package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Atendimento;
import com.vitalistech.sosrota.dominio.modelo.Bairro;
import com.vitalistech.sosrota.dominio.modelo.Ocorrencia;
import com.vitalistech.sosrota.dominio.repositorio.BairroRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.OcorrenciaRepositorio;
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

    public OcorrenciaControlador(OcorrenciaServico ocorrenciaServico,
                                 BairroRepositorio bairroRepositorio,
                                 OcorrenciaRepositorio ocorrenciaRepositorio) {
        this.ocorrenciaServico = ocorrenciaServico;
        this.bairroRepositorio = bairroRepositorio;
        this.ocorrenciaRepositorio = ocorrenciaRepositorio;
    }

    @GetMapping
    public List<Ocorrencia> listar() {
        return ocorrenciaRepositorio.findAll();
    }

    @PostMapping
    public ResponseEntity<Ocorrencia> registrar(@RequestBody @Valid RegistrarOcorrenciaDTO dto) {
        Bairro bairroLocal = bairroRepositorio.findById(dto.getIdBairroLocal())
                .orElseThrow(() -> new IllegalArgumentException("Bairro não encontrado"));

        Ocorrencia ocorrencia = ocorrenciaServico.registrarOcorrencia(
                bairroLocal,
                dto.getTipoOcorrencia(),
                dto.getGravidade(),
                dto.getObservacoes()
        );

        return ResponseEntity.ok(ocorrencia);
    }

    @PostMapping("/{id}/despachar")
    public ResponseEntity<Atendimento> despachar(@PathVariable Long id) {
        Atendimento atendimento = ocorrenciaServico.despacharOcorrencia(id);
        return ResponseEntity.ok(atendimento);
    }
}
