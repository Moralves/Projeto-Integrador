package com.vitalistech.sosrota.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Componente que executa automaticamente o script de setup do banco de dados
 * na inicialização da aplicação Spring Boot.
 * 
 * O script é executado de forma idempotente e pode ser executado múltiplas vezes.
 */
@Configuration
public class InicializadorBancoDados {

    private static final Logger logger = LoggerFactory.getLogger(InicializadorBancoDados.class);

    /**
     * Executa o script de setup do banco de dados na inicialização da aplicação.
     * Este método é executado automaticamente uma vez quando a aplicação inicia.
     * A ordem -1 garante que seja executado antes de outros CommandLineRunners.
     */
    @Bean
    @Order(-1)
    public CommandLineRunner inicializarBancoDados(DataSource dataSource) {
        return args -> {
            try {
                logger.info("========================================");
                logger.info("INICIANDO CONFIGURAÇÃO DO BANCO DE DADOS");
                logger.info("========================================");
                
                // Carregar o script SQL
                Resource resource = new ClassPathResource("db/migration/setup.sql");
                
                if (!resource.exists()) {
                    logger.warn("Script de setup não encontrado em: db/migration/setup.sql");
                    logger.warn("O banco de dados pode não estar completamente configurado.");
                    return;
                }

                logger.info("Executando script de setup do banco de dados...");
                
                // Executar o script usando ResourceDatabasePopulator
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.setScripts(resource);
                populator.setSeparator(";"); // Separador padrão para comandos SQL
                populator.setContinueOnError(true); // Continua mesmo se houver erros (idempotente)
                
                // Executar o script
                populator.execute(dataSource);
                
                logger.info("✓ Script de setup executado com sucesso!");
                logger.info("========================================");
                logger.info("Banco de dados configurado e pronto para uso");
                logger.info("========================================");
                
            } catch (Exception e) {
                logger.error("Erro ao executar script de setup do banco de dados", e);
                // Não lança exceção para não impedir a inicialização da aplicação
                // O script é idempotente, então pode ser executado manualmente se necessário
                logger.warn("A aplicação continuará iniciando, mas o banco pode não estar completamente configurado.");
            }
        };
    }
}

