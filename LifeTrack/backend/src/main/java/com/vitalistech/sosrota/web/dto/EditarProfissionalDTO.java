package com.vitalistech.sosrota.web.dto;

import com.vitalistech.sosrota.dominio.modelo.FuncaoProfissional;
import com.vitalistech.sosrota.dominio.modelo.StatusProfissional;
import com.vitalistech.sosrota.dominio.modelo.Turno;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para edição de profissional.
 */
public class EditarProfissionalDTO {

    @NotNull
    private String nome;

    @NotNull
    private FuncaoProfissional funcao;

    private String contato;

    @NotNull
    private Turno turno;

    @NotNull
    private StatusProfissional status;

    @NotNull
    private Boolean ativo;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public FuncaoProfissional getFuncao() {
        return funcao;
    }

    public void setFuncao(FuncaoProfissional funcao) {
        this.funcao = funcao;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public StatusProfissional getStatus() {
        return status;
    }

    public void setStatus(StatusProfissional status) {
        this.status = status;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}





