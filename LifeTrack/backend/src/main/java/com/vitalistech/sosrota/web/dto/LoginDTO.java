package com.vitalistech.sosrota.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login.
 */
public class LoginDTO {

    @NotBlank(message = "Login é obrigatório")
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}

