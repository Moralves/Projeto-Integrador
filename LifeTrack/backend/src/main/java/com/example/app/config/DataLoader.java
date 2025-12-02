package com.example.app.config;

import com.example.app.model.Usuario;
import com.example.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * DataLoader para criar usuários iniciais no banco de dados.
 * Este componente é executado automaticamente quando a aplicação inicia.
 * 
 * Para desabilitar, comente a anotação @Component ou remova este arquivo.
 */
@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Verificar se já existe usuário admin
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Senha padrão: admin123
            admin.setNome("Administrador");
            admin.setEmail("admin@lifetrack.com");
            admin.setAtivo(true);
            
            Set<String> roles = new HashSet<>();
            roles.add("ADMIN");
            admin.setRoles(roles);
            
            usuarioRepository.save(admin);
            System.out.println("Usuário admin criado com sucesso! (username: admin, senha: admin123)");
        }
        
        // Verificar se já existe usuário atendente
        if (usuarioRepository.findByUsername("atendente").isEmpty()) {
            Usuario atendente = new Usuario();
            atendente.setUsername("atendente");
            atendente.setPassword(passwordEncoder.encode("atendente123")); // Senha padrão: atendente123
            atendente.setNome("Atendente Teste");
            atendente.setEmail("atendente@lifetrack.com");
            atendente.setAtivo(true);
            
            Set<String> roles = new HashSet<>();
            roles.add("USER");
            atendente.setRoles(roles);
            
            usuarioRepository.save(atendente);
            System.out.println("Usuário atendente criado com sucesso! (username: atendente, senha: atendente123)");
        }
    }
}

