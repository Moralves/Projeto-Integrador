package com.vitalistech.sosrota.web.dto;

/**
 * DTO para resposta de login.
 */
public class LoginResponseDTO {

    private Long id;
    private String login;
    private String nome;
    private String email;
    private String perfil;
    private boolean ativo;
    private String token; // Para futura implementação com JWT

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(Long id, String login, String nome, String email, String perfil, boolean ativo) {
        this.id = id;
        this.login = login;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.ativo = ativo;
        this.token = "mock-token"; // Por enquanto, token mockado
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

