package com.vitalistech.sosrota.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração responsável por fornecer um bean de PasswordEncoder
 * para criptografar senhas com BCrypt.
 */
@Configuration
public class ConfiguracaoSenha {

    @Bean
    public PasswordEncoder codificadorSenha() {
        return new BCryptPasswordEncoder();
    }
}
