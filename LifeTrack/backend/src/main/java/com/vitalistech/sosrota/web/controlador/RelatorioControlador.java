package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Atendimento;
import com.vitalistech.sosrota.dominio.modelo.Ocorrencia;
import com.vitalistech.sosrota.dominio.repositorio.AtendimentoRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.OcorrenciaRepositorio;
import com.vitalistech.sosrota.web.dto.RelatorioOcorrenciaDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints para relatórios administrativos.
 */
@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
public class RelatorioControlador {

    private final com.vitalistech.sosrota.padroes.template.GeradorRelatorioOcorrencia geradorRelatorioOcorrencia;

    public RelatorioControlador(com.vitalistech.sosrota.padroes.template.GeradorRelatorioOcorrencia geradorRelatorioOcorrencia) {
        this.geradorRelatorioOcorrencia = geradorRelatorioOcorrencia;
    }

    @GetMapping("/ocorrencias")
    public List<RelatorioOcorrenciaDTO> relatorioOcorrencias() {
        // Padrão Template Method: Delega a execução para o gerador que implementa o template
        return geradorRelatorioOcorrencia.gerarRelatorio();
    }
}

