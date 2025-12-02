-- Script SQL para criar o banco de dados e tabelas do LifeTrack
-- Execute este script no MySQL após criar o banco de dados

CREATE DATABASE IF NOT EXISTS lifetrack;
USE lifetrack;

-- Tabela de usuários
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabela de roles dos usuários
CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (usuario_id, role),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- IMPORTANTE: Para inserir usuários, você precisa gerar o hash BCrypt da senha primeiro.
-- Use a classe PasswordGenerator.java para gerar os hashes.
-- 
-- Exemplo de inserção (substitua o hash pela senha gerada):
-- INSERT INTO usuarios (username, password, nome, email, ativo) 
-- VALUES ('admin', '$2a$10$SEU_HASH_AQUI', 'Administrador', 'admin@lifetrack.com', TRUE);
--
-- INSERT INTO usuario_roles (usuario_id, role) 
-- VALUES ((SELECT id FROM usuarios WHERE username = 'admin'), 'ADMIN');
--
-- Ou use o endpoint de registro (quando implementado) ou crie um DataLoader no Spring Boot.

