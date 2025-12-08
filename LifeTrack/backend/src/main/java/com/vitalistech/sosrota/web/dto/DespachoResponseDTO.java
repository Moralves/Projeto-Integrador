package com.vitalistech.sosrota.web.dto;

import com.vitalistech.sosrota.dominio.modelo.Atendimento;

/**
 * DTO para resposta do despacho, contendo o atendimento criado
 * e as informações do timer em tempo real.
 */
public class DespachoResponseDTO {
    
    private Atendimento atendimento;
    private TimerOcorrenciaDTO timer;

    public DespachoResponseDTO() {
    }

    public DespachoResponseDTO(Atendimento atendimento, TimerOcorrenciaDTO timer) {
        this.atendimento = atendimento;
        this.timer = timer;
    }

    public Atendimento getAtendimento() {
        return atendimento;
    }

    public void setAtendimento(Atendimento atendimento) {
        this.atendimento = atendimento;
    }

    public TimerOcorrenciaDTO getTimer() {
        return timer;
    }

    public void setTimer(TimerOcorrenciaDTO timer) {
        this.timer = timer;
    }
}


