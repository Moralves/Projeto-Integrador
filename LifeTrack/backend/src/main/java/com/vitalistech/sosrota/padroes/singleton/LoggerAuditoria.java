package com.vitalistech.sosrota.padroes.singleton;

import java.time.LocalDateTime;

/**
 * Padrão Singleton: Garante que apenas uma instância de LoggerAuditoria 
 * seja criada no sistema, fornecendo um ponto de acesso global para ela.
 * Utilizado para manter um registro centralizado de auditoria.
 */
public class LoggerAuditoria {

    // Instância única estática
    private static LoggerAuditoria instancia;

    // Construtor privado para evitar instanciação externa
    private LoggerAuditoria() {
        // Inicialização de recursos (ex: abrir arquivo, conectar ao serviço de log)
        System.out.println("LoggerAuditoria inicializado.");
    }

    // Ponto de acesso global e sincronizado para evitar problemas com threads
    public static synchronized LoggerAuditoria getInstancia() {
        if (instancia == null) {
            instancia = new LoggerAuditoria();
        }
        return instancia;
    }

    public void log(String acao, String usuario) {
        String mensagem = String.format("[%s] Ação: %s | Usuário: %s", 
                LocalDateTime.now(), acao, usuario);
        System.out.println("[AUDITORIA] " + mensagem);
        // Em um cenário real, aqui gravaríamos num arquivo ou banco
    }
}
