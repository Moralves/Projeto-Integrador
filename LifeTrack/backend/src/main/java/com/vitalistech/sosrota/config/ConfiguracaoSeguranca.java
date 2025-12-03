package com.vitalistech.sosrota.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança HTTP.
 * Para simplificar, todas as rotas estão liberadas.
 * Você pode evoluir depois para autenticação real com JWT ou HTTP Basic.
 */
@Configuration
public class ConfiguracaoSeguranca {

    @Bean
    public SecurityFilterChain filtroSeguranca(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
