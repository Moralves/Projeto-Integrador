package com.example.app.dto;

import java.util.Set;

public class UsuarioResponse {
    private Long id;
    private String username;
    private String nome;
    private String email;
    private Boolean ativo;
    private Set<String> roles;
    
    public UsuarioResponse() {
    }
    
    public UsuarioResponse(Long id, String username, String nome, String email, Boolean ativo, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.nome = nome;
        this.email = email;
        this.ativo = ativo;
        this.roles = roles;
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
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}

