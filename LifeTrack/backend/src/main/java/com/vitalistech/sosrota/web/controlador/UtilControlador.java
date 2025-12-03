package com.vitalistech.sosrota.web.controlador;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de utilidades (temporário - para gerar hash de senha).
 * Pode ser removido após criar o usuário admin.
 */
@RestController
@RequestMapping("/api/util")
@CrossOrigin(origins = "*")
public class UtilControlador {

    private final PasswordEncoder passwordEncoder;

    public UtilControlador(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Endpoint temporário para gerar hash de senha.
     * Exemplo: GET /api/util/hash?senha=admin123
     */
    @GetMapping("/hash")
    public String gerarHash(@RequestParam String senha) {
        return passwordEncoder.encode(senha);
    }
}

