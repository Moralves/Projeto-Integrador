package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Usuario;
import com.vitalistech.sosrota.dominio.repositorio.UsuarioRepositorio;
import com.vitalistech.sosrota.web.dto.LoginDTO;
import com.vitalistech.sosrota.web.dto.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de autenticação.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthControlador {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public AuthControlador(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO loginDTO) {
        try {
            // Buscar usuário pelo login
            Usuario usuario = usuarioRepositorio.findByLogin(loginDTO.getLogin())
                    .orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(401).body("Login ou senha inválidos");
            }

            // Verificar se o usuário está ativo
            if (!usuario.isAtivo()) {
                return ResponseEntity.status(403).body("Usuário inativo");
            }

            // Verificar se a senha hash existe
            if (usuario.getSenhaHash() == null || usuario.getSenhaHash().isEmpty()) {
                return ResponseEntity.status(500).body("Erro: senha não configurada para este usuário");
            }

            // Verificar senha
            if (!passwordEncoder.matches(loginDTO.getSenha(), usuario.getSenhaHash())) {
                return ResponseEntity.status(401).body("Login ou senha inválidos");
            }

            // Criar resposta de login
            LoginResponseDTO response = new LoginResponseDTO(
                    usuario.getId(),
                    usuario.getLogin(),
                    usuario.getNome() != null ? usuario.getNome() : usuario.getLogin(),
                    usuario.getEmail() != null ? usuario.getEmail() : usuario.getLogin() + "@sistema.local",
                    usuario.getPerfil(),
                    usuario.isAtivo()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Para debug
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }
}

