package com.example.app.service;

import com.example.app.dto.LoginRequest;
import com.example.app.dto.LoginResponse;
import com.example.app.model.Usuario;
import com.example.app.repository.UsuarioRepository;
import com.example.app.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(loginRequest.getUsername());
        
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (!usuario.getAtivo()) {
            throw new RuntimeException("Usuário inativo");
        }
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }
        
        String token = jwtUtil.generateToken(usuario.getUsername());
        
        return new LoginResponse(
            token,
            usuario.getId(),
            usuario.getUsername(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getRoles()
        );
    }
}

