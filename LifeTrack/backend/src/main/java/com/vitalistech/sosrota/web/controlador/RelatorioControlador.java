package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Atendimento;
import com.vitalistech.sosrota.dominio.modelo.Ocorrencia;
import com.vitalistech.sosrota.dominio.modelo.StatusOcorrencia;
import com.vitalistech.sosrota.dominio.repositorio.AtendimentoRepositorio;
import com.vitalistech.sosrota.dominio.repositorio.OcorrenciaRepositorio;
import com.vitalistech.sosrota.web.dto.RelatorioOcorrenciaDTO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
            // Buscar atendimento relacionado à ocorrência (para qualquer status que tenha atendimento)
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
                
                // Calcular tempo total de forma FIXA quando retornou à base
                if (ocorrencia.getDataHoraAbertura() != null) {
                    LocalDateTime agora = LocalDateTime.now();
                    long tempoTotalSegundos = 0;
                    boolean retornouBase = atendimento.getDataHoraRetorno() != null;
                    boolean chegouLocal = atendimento.getDataHoraChegada() != null;
                    boolean foiDespachada = atendimento.getDataHoraDespacho() != null;
                    boolean foiConcluida = ocorrencia.getStatus() == StatusOcorrencia.CONCLUIDA;
                    
                    if (retornouBase && atendimento.getDataHoraRetorno() != null) {
                        // Se retornou: tempo total = abertura até retorno (tempo completo e FIXO - não aumenta mais)
                        tempoTotalSegundos = java.time.Duration.between(
                            ocorrencia.getDataHoraAbertura(), atendimento.getDataHoraRetorno()).getSeconds();
                    } else if (chegouLocal && atendimento.getDataHoraChegada() != null) {
                        // Se chegou mas ainda não retornou: tempo desde abertura até chegada + tempo decorrido de retorno
                        long tempoAteChegadaSegundos = java.time.Duration.between(
                            ocorrencia.getDataHoraAbertura(), atendimento.getDataHoraChegada()).getSeconds();
                        long tempoRetornoSegundos = 0;
                        if (foiConcluida) {
                            // Se está concluída mas ainda não retornou, calcular tempo decorrido desde chegada
                            tempoRetornoSegundos = java.time.Duration.between(
                                atendimento.getDataHoraChegada(), agora).getSeconds();
                        }
                        tempoTotalSegundos = tempoAteChegadaSegundos + tempoRetornoSegundos;
                    } else if (foiDespachada && atendimento.getDataHoraDespacho() != null) {
                        // Se ainda não chegou mas foi despachada: tempo desde abertura até agora (estimativa)
                        tempoTotalSegundos = java.time.Duration.between(ocorrencia.getDataHoraAbertura(), agora).getSeconds();
                    } else {
                        // Se ainda não foi despachada: apenas tempo desde abertura
                        tempoTotalSegundos = java.time.Duration.between(ocorrencia.getDataHoraAbertura(), agora).getSeconds();
                    }
                    
                    dto.setTempoTotalDecorridoMinutos(tempoTotalSegundos / 60);
                    dto.setTempoTotalFormatado(formatarTempo(tempoTotalSegundos));
                }
            } else if (ocorrencia.getDataHoraAbertura() != null) {
                // Se não há atendimento mas há data de abertura, calcular tempo desde abertura
                LocalDateTime agora = LocalDateTime.now();
                long tempoTotalSegundos = java.time.Duration.between(ocorrencia.getDataHoraAbertura(), agora).getSeconds();
                dto.setTempoTotalDecorridoMinutos(tempoTotalSegundos / 60);
                dto.setTempoTotalFormatado(formatarTempo(tempoTotalSegundos));
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * Formata tempo em segundos para uma string legível (ex: "15m 30s" ou "1h 25m").
     */
    private String formatarTempo(long segundos) {
        boolean negativo = segundos < 0;
        segundos = Math.abs(segundos);
        
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long seg = segundos % 60;
        
        StringBuilder sb = new StringBuilder();
        if (negativo) sb.append("-");
        
        if (horas > 0) {
            sb.append(horas).append("h");
            if (minutos > 0) {
                sb.append(" ").append(minutos).append("m");
            }
        } else if (minutos > 0) {
            sb.append(minutos).append("m");
            if (seg > 0) {
                sb.append(" ").append(seg).append("s");
            }
        } else {
            sb.append(seg).append("s");
        }
        
        return sb.toString();
    }
}

