package com.vitalistech.sosrota.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para criação de usuário.
 */
public class CriarUsuarioDTO {

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "A senha é obrigatória")
    @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", message = "A senha deve ter pelo menos 8 caracteres, contendo letras e números")
    private String password;

    private String nome;

    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @jakarta.validation.constraints.Pattern(regexp = "^\\(\\d{2}\\) \\d{4,5}-\\d{4}$", message = "O telefone deve estar no formato (XX) XXXXX-XXXX ou (XX) XXXX-XXXX")
    private String telefone;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}

