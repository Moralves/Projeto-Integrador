package com.vitalistech.sosrota.web.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DTO para retornar dados de usuário ao frontend.
 */
public class UsuarioDTO {

    private Long id;
    private String username;
    private String nome;
    private String email;
    private String telefone;
    private List<String> roles;
    private boolean ativo;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Long id, String username, String nome, String email, String perfil, boolean ativo) {
        this(id, username, nome, email, null, perfil, ativo);
    }

    public UsuarioDTO(Long id, String username, String nome, String email, String telefone, String perfil, boolean ativo) {
        this.id = id;
        this.username = username;
        this.nome = nome != null ? nome : username;
        this.email = email != null ? email : username + "@sistema.local";
        this.telefone = telefone;
        // Converter perfil (string) para lista de roles
        if (perfil != null && !perfil.isEmpty()) {
            this.roles = Arrays.asList(perfil.split(","));
        } else {
            this.roles = new ArrayList<>();
        }
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}

