package com.vitalistech.sosrota.web.controlador;

import com.vitalistech.sosrota.dominio.modelo.Usuario;
import com.vitalistech.sosrota.dominio.repositorio.UsuarioRepositorio;
import com.vitalistech.sosrota.web.dto.CriarUsuarioDTO;
import com.vitalistech.sosrota.web.dto.UsuarioDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints de gestão de usuários.
 */
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioControlador {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioControlador(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioRepositorio.findAll().stream()
                .map(u -> new UsuarioDTO(u.getId(), u.getLogin(), u.getNome(), u.getEmail(), u.getTelefone(), u.getPerfil(), u.isAtivo()))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> criar(@RequestBody @Valid CriarUsuarioDTO dto) {
        // Verificar se o login já existe
        if (usuarioRepositorio.findByLogin(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Login já está em uso");
        }

        // Normalizar telefone: remover formatação e espaços
        final String telefoneNormalizado;
        if (dto.getTelefone() != null && !dto.getTelefone().trim().isEmpty()) {
            telefoneNormalizado = dto.getTelefone().trim().replaceAll("[^0-9]", "");
            
            // Verificar se o telefone já existe (comparando apenas números)
            final String telefoneParaComparar = telefoneNormalizado;
            usuarioRepositorio.findAll().stream()
                .filter(u -> {
                    String telefoneExistente = u.getTelefone() != null ? u.getTelefone().replaceAll("[^0-9]", "") : "";
                    return telefoneExistente.equals(telefoneParaComparar);
                })
                .findFirst()
                .ifPresent(usuarioExistente -> {
                    throw new IllegalStateException(
                        "Já existe um usuário cadastrado com o telefone: " + dto.getTelefone());
                });
        } else {
            telefoneNormalizado = null;
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(dto.getUsername());
        
        // Criptografar senha
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setSenhaHash(passwordEncoder.encode(dto.getPassword()));
        } else {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        // Admin só pode criar usuários comuns (USER), nunca ADMIN
        usuario.setPerfil("USER");
        
        // Definir nome, email e telefone (salvar apenas números)
        usuario.setNome(dto.getNome() != null && !dto.getNome().isEmpty() ? dto.getNome() : dto.getUsername());
        usuario.setEmail(dto.getEmail() != null && !dto.getEmail().isEmpty() ? dto.getEmail() : dto.getUsername() + "@sistema.local");
        usuario.setTelefone(telefoneNormalizado);
        usuario.setAtivo(true);

        Usuario usuarioSalvo = usuarioRepositorio.save(usuario);
        UsuarioDTO dtoRetorno = new UsuarioDTO(
                usuarioSalvo.getId(), 
                usuarioSalvo.getLogin(), 
                usuarioSalvo.getNome(), 
                usuarioSalvo.getEmail(), 
                usuarioSalvo.getTelefone(), 
                usuarioSalvo.getPerfil(), 
                usuarioSalvo.isAtivo()
        );

        return ResponseEntity.ok(dtoRetorno);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable Long id, @RequestBody @Valid CriarUsuarioDTO dto) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Atualizar login se fornecido
        if (dto.getUsername() != null && !dto.getUsername().isEmpty()) {
            // Verificar se o novo login já existe em outro usuário
            usuarioRepositorio.findByLogin(dto.getUsername())
                    .ifPresent(u -> {
                        if (!u.getId().equals(id)) {
                            throw new IllegalArgumentException("Login já existe");
                        }
                    });
            usuario.setLogin(dto.getUsername());
        }

        // Atualizar senha se fornecida
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setSenhaHash(passwordEncoder.encode(dto.getPassword()));
        }

        // Admin não pode alterar perfil para ADMIN (sempre mantém como USER)
        // Não atualizamos o perfil aqui para garantir que não seja alterado para ADMIN

        // Normalizar telefone: remover formatação e espaços
        final String telefoneNormalizado;
        if (dto.getTelefone() != null && !dto.getTelefone().trim().isEmpty()) {
            telefoneNormalizado = dto.getTelefone().trim().replaceAll("[^0-9]", "");
            
            // Verificar se o telefone já existe em outro usuário (comparando apenas números)
            final String telefoneParaComparar = telefoneNormalizado;
            final Long idUsuarioAtual = id;
            usuarioRepositorio.findAll().stream()
                .filter(u -> !u.getId().equals(idUsuarioAtual)) // Excluir o próprio usuário
                .filter(u -> {
                    String telefoneExistente = u.getTelefone() != null ? u.getTelefone().replaceAll("[^0-9]", "") : "";
                    return telefoneExistente.equals(telefoneParaComparar);
                })
                .findFirst()
                .ifPresent(usuarioExistente -> {
                    throw new IllegalStateException(
                        "Já existe outro usuário cadastrado com o telefone: " + dto.getTelefone());
                });
            
            usuario.setTelefone(telefoneNormalizado);
        } else {
            telefoneNormalizado = null;
            usuario.setTelefone(null);
        }

        // Atualizar nome e email
        if (dto.getNome() != null && !dto.getNome().isEmpty()) {
            usuario.setNome(dto.getNome());
        }
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            usuario.setEmail(dto.getEmail());
        }

        Usuario usuarioSalvo = usuarioRepositorio.save(usuario);
        UsuarioDTO dtoRetorno = new UsuarioDTO(
                usuarioSalvo.getId(), 
                usuarioSalvo.getLogin(), 
                usuarioSalvo.getNome(), 
                usuarioSalvo.getEmail(), 
                usuarioSalvo.getTelefone(), 
                usuarioSalvo.getPerfil(), 
                usuarioSalvo.isAtivo()
        );

        return ResponseEntity.ok(dtoRetorno);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!usuarioRepositorio.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepositorio.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<UsuarioDTO> toggleStatus(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        
        // Alternar status ativo/inativo
        usuario.setAtivo(!usuario.isAtivo());
        Usuario usuarioSalvo = usuarioRepositorio.save(usuario);
        
        UsuarioDTO dto = new UsuarioDTO(
                usuarioSalvo.getId(), 
                usuarioSalvo.getLogin(), 
                usuarioSalvo.getNome(), 
                usuarioSalvo.getEmail(), 
                usuarioSalvo.getTelefone(), 
                usuarioSalvo.getPerfil(), 
                usuarioSalvo.isAtivo()
        );
        
        return ResponseEntity.ok(dto);
    }
}

