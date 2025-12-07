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

    private final OcorrenciaRepositorio ocorrenciaRepositorio;
    private final AtendimentoRepositorio atendimentoRepositorio;

    public RelatorioControlador(OcorrenciaRepositorio ocorrenciaRepositorio,
                                AtendimentoRepositorio atendimentoRepositorio) {
        this.ocorrenciaRepositorio = ocorrenciaRepositorio;
        this.atendimentoRepositorio = atendimentoRepositorio;
    }

    @GetMapping("/ocorrencias")
    public List<RelatorioOcorrenciaDTO> relatorioOcorrencias() {
        List<Ocorrencia> ocorrencias = ocorrenciaRepositorio.findAll();
        
        return ocorrencias.stream().map(ocorrencia -> {
            RelatorioOcorrenciaDTO dto = new RelatorioOcorrenciaDTO();
            dto.setId(ocorrencia.getId());
            dto.setDataHoraAbertura(ocorrencia.getDataHoraAbertura());
            dto.setTipoOcorrencia(ocorrencia.getTipoOcorrencia());
            dto.setGravidade(ocorrencia.getGravidade() != null ? ocorrencia.getGravidade().name() : null);
            dto.setStatus(ocorrencia.getStatus() != null ? ocorrencia.getStatus().name() : null);
            dto.setBairroNome(ocorrencia.getBairroLocal() != null ? ocorrencia.getBairroLocal().getNome() : null);
            dto.setObservacoes(ocorrencia.getObservacoes());
            
            // Informações de SLA e tempo de atendimento
            dto.setDataHoraFechamento(ocorrencia.getDataHoraFechamento());
            dto.setTempoAtendimentoMinutos(ocorrencia.getTempoAtendimentoMinutos());
            dto.setSlaMinutos(ocorrencia.getSlaMinutos());
            dto.setSlaCumprido(ocorrencia.getSlaCumprido());
            dto.setTempoExcedidoMinutos(ocorrencia.getTempoExcedidoMinutos());
            
            // Informações do usuário que registrou
            if (ocorrencia.getUsuarioRegistro() != null) {
                dto.setUsuarioRegistroNome(ocorrencia.getUsuarioRegistro().getNome() != null 
                    ? ocorrencia.getUsuarioRegistro().getNome() 
                    : ocorrencia.getUsuarioRegistro().getLogin());
                dto.setUsuarioRegistroLogin(ocorrencia.getUsuarioRegistro().getLogin());
            }
            
            // Informações do atendimento (se houver)
            if (ocorrencia.getStatus() != null && ocorrencia.getStatus().name().equals("DESPACHADA")) {
                // Buscar atendimento relacionado à ocorrência
                List<Atendimento> atendimentos = atendimentoRepositorio.findAll().stream()
                    .filter(a -> a.getOcorrencia() != null && a.getOcorrencia().getId().equals(ocorrencia.getId()))
                    .collect(Collectors.toList());
                
                if (!atendimentos.isEmpty()) {
                    Atendimento atendimento = atendimentos.get(0);
                    dto.setDataHoraDespacho(atendimento.getDataHoraDespacho());
                    dto.setAmbulanciaPlaca(atendimento.getAmbulancia() != null 
                        ? atendimento.getAmbulancia().getPlaca() 
                        : null);
                    dto.setDistanciaKm(atendimento.getDistanciaKm());
                    
                    if (atendimento.getUsuarioDespacho() != null) {
                        dto.setUsuarioDespachoNome(atendimento.getUsuarioDespacho().getNome() != null 
                            ? atendimento.getUsuarioDespacho().getNome() 
                            : atendimento.getUsuarioDespacho().getLogin());
                        dto.setUsuarioDespachoLogin(atendimento.getUsuarioDespacho().getLogin());
                    }
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}

