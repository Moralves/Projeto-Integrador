package com.example.app.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitário para gerar hash de senhas BCrypt.
 * Execute este método main para gerar hashes de senhas.
 * 
 * Exemplo de uso:
 * - Execute: java PasswordGenerator
 * - Digite a senha quando solicitado
 * - Copie o hash gerado e use no banco de dados ou código
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Exemplos de senhas comuns
        System.out.println("=== Hashes BCrypt Gerados ===");
        System.out.println("Senha: admin123");
        System.out.println("Hash: " + encoder.encode("admin123"));
        System.out.println();
        
        System.out.println("Senha: atendente123");
        System.out.println("Hash: " + encoder.encode("atendente123"));
        System.out.println();
        
        System.out.println("Senha: senha123");
        System.out.println("Hash: " + encoder.encode("senha123"));
        System.out.println();
        
        // Para gerar hash de uma senha customizada, descomente:
        // Scanner scanner = new Scanner(System.in);
        // System.out.print("Digite a senha para gerar hash: ");
        // String senha = scanner.nextLine();
        // System.out.println("Hash: " + encoder.encode(senha));
    }
}

