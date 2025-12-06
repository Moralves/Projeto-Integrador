package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.HistoricoOcorrencia;
import com.vitalistech.sosrota.dominio.servico.HistoricoOcorrenciaServico;
import com.vitalistech.sosrota.web.dto.HistoricoOcorrenciaDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para consulta do histórico de ocorrências.
 * Permite que usuários e administradores consultem o histórico de ações realizadas.
 */
@RestController
@RequestMapping("/api/historico-ocorrencias")
@CrossOrigin(origins = "*")
public class HistoricoOcorrenciaControlador {

    private final HistoricoOcorrenciaServico historicoOcorrenciaServico;

    public HistoricoOcorrenciaControlador(HistoricoOcorrenciaServico historicoOcorrenciaServico) {
        this.historicoOcorrenciaServico = historicoOcorrenciaServico;
    }

    /**
     * Busca o histórico completo de uma ocorrência específica.
     * Disponível para usuários e administradores.
     * 
     * GET /api/historico-ocorrencias/ocorrencia/{idOcorrencia}
     */
    @GetMapping("/ocorrencia/{idOcorrencia}")
    public ResponseEntity<List<HistoricoOcorrenciaDTO>> buscarPorOcorrencia(@PathVariable Long idOcorrencia) {
        try {
            List<HistoricoOcorrencia> historicos = historicoOcorrenciaServico.buscarHistoricoPorOcorrencia(idOcorrencia);
            List<HistoricoOcorrenciaDTO> dtos = historicos.stream()
                    .map(this::converterParaDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Busca o histórico de ações realizadas por um usuário específico.
     * Disponível para o próprio usuário e para administradores.
     * 
     * GET /api/historico-ocorrencias/usuario/{idUsuario}
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<HistoricoOcorrenciaDTO>> buscarPorUsuario(@PathVariable Long idUsuario) {
        try {
            List<HistoricoOcorrencia> historicos = historicoOcorrenciaServico.buscarHistoricoPorUsuario(idUsuario);
            List<HistoricoOcorrenciaDTO> dtos = historicos.stream()
                    .map(this::converterParaDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Busca histórico com filtros opcionais.
     * Se nenhum filtro for fornecido, retorna todo o histórico (apenas para admin).
     * 
     * GET /api/historico-ocorrencias?usuarioId={id}&ocorrenciaId={id}
     */
    @GetMapping
    public ResponseEntity<List<HistoricoOcorrenciaDTO>> buscarComFiltros(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long ocorrenciaId) {
        try {
            List<HistoricoOcorrencia> historicos = historicoOcorrenciaServico.buscarHistoricoComFiltros(usuarioId, ocorrenciaId);
            List<HistoricoOcorrenciaDTO> dtos = historicos.stream()
                    .map(this::converterParaDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Busca o histórico do usuário logado.
     * 
     * GET /api/historico-ocorrencias/meu-historico
     */
    @GetMapping("/meu-historico")
    public ResponseEntity<List<HistoricoOcorrenciaDTO>> buscarMeuHistorico(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().body(null);
            }
            List<HistoricoOcorrencia> historicos = historicoOcorrenciaServico.buscarHistoricoPorUsuario(userId);
            List<HistoricoOcorrenciaDTO> dtos = historicos.stream()
                    .map(this::converterParaDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Converte um HistoricoOcorrencia para DTO.
     */
    private HistoricoOcorrenciaDTO converterParaDTO(HistoricoOcorrencia historico) {
        HistoricoOcorrenciaDTO dto = new HistoricoOcorrenciaDTO();
        dto.setId(historico.getId());
        dto.setOcorrenciaId(historico.getOcorrencia() != null ? historico.getOcorrencia().getId() : null);
        dto.setUsuarioId(historico.getUsuario() != null ? historico.getUsuario().getId() : null);
        dto.setAcao(historico.getAcao() != null ? historico.getAcao().name() : null);
        dto.setStatusAnterior(historico.getStatusAnterior() != null ? historico.getStatusAnterior().name() : null);
        dto.setStatusNovo(historico.getStatusNovo() != null ? historico.getStatusNovo().name() : null);
        dto.setDescricaoAcao(historico.getDescricaoAcao());
        dto.setDataHora(historico.getDataHora());
        dto.setTipoOcorrencia(historico.getTipoOcorrencia());
        dto.setGravidade(historico.getGravidade() != null ? historico.getGravidade().name() : null);
        dto.setBairroOrigemNome(historico.getBairroOrigemNome());
        dto.setObservacoes(historico.getObservacoes());
        dto.setUsuarioNome(historico.getUsuarioNome());
        dto.setUsuarioLogin(historico.getUsuarioLogin());
        dto.setUsuarioPerfil(historico.getUsuarioPerfil());
        return dto;
    }
}

