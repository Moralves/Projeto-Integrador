package com.example.app.service;

import com.example.app.model.Usuario;
import com.example.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        
        return User.builder()
            .username(usuario.getUsername())
            .password(usuario.getPassword())
            .authorities(getAuthorities(usuario))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!usuario.getAtivo())
            .build();
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (usuario.getRoles() != null) {
            usuario.getRoles().forEach(role -> {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            });
        }
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authorities;
    }
}

