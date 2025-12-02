package com.example.app.service;

import com.example.app.dto.CreateUsuarioRequest;
import com.example.app.dto.UsuarioResponse;
import com.example.app.model.Usuario;
import com.example.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public UsuarioResponse criarUsuario(CreateUsuarioRequest request) {
        // Verificar se username já existe
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username já está em uso");
        }
        
        // Verificar se email já existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já está em uso");
        }
        
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setAtivo(true);
        
        // Definir roles (se não especificado, padrão é USER)
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            usuario.setRoles(request.getRoles());
        } else {
            usuario.getRoles().add("USER");
        }
        
        Usuario saved = usuarioRepository.save(usuario);
        return toResponse(saved);
    }
    
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return toResponse(usuario);
    }
    
    public UsuarioResponse atualizarUsuario(Long id, CreateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verificar se username mudou e se já existe
        if (!usuario.getUsername().equals(request.getUsername()) && 
            usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username já está em uso");
        }
        
        // Verificar se email mudou e se já existe
        if (!usuario.getEmail().equals(request.getEmail()) && 
            usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já está em uso");
        }
        
        usuario.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            usuario.setRoles(request.getRoles());
        }
        
        Usuario saved = usuarioRepository.save(usuario);
        return toResponse(saved);
    }
    
    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }
    
    public UsuarioResponse ativarDesativarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        usuario.setAtivo(!usuario.getAtivo());
        Usuario saved = usuarioRepository.save(usuario);
        return toResponse(saved);
    }
    
    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getAtivo(),
            usuario.getRoles()
        );
    }
}

