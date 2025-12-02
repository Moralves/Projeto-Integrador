package com.example.app.controller;

import com.example.app.dto.CreateUsuarioRequest;
import com.example.app.dto.UsuarioResponse;
import com.example.app.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarUsuario(@RequestBody CreateUsuarioRequest request) {
        try {
            UsuarioResponse response = usuarioService.criarUsuario(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            UsuarioResponse response = usuarioService.buscarPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @RequestBody CreateUsuarioRequest request) {
        try {
            UsuarioResponse response = usuarioService.atualizarUsuario(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuario(id);
            return ResponseEntity.ok().body("Usuário deletado com sucesso");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
    
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> ativarDesativarUsuario(@PathVariable Long id) {
        try {
            UsuarioResponse response = usuarioService.ativarDesativarUsuario(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
}

