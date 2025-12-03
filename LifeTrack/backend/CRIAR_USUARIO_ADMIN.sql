-- Script para criar um usuário administrador inicial
-- Execute este script no DBeaver após criar a tabela usuarios

-- IMPORTANTE: Para gerar o hash da senha, você tem duas opções:
--
-- OPÇÃO 1: Usar o endpoint temporário (recomendado)
-- 1. Inicie o backend
-- 2. Acesse: http://localhost:8081/api/util/hash?senha=admin123
-- 3. Copie o hash retornado
-- 4. Use o hash no INSERT abaixo
--
-- OPÇÃO 2: Usar um hash pré-gerado (pode não funcionar se o salt for diferente)
-- O hash abaixo foi gerado para a senha "admin123"
-- Se não funcionar, use a OPÇÃO 1

-- Hash para senha "admin123" (gerado com BCrypt)
-- ATENÇÃO: Este hash pode variar. Use a OPÇÃO 1 para garantir que funcione.

INSERT INTO usuarios (login, senha_hash, perfil, nome, email, ativo)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'Administrador',
    'admin@sistema.local',
    true
)
ON CONFLICT (login) DO NOTHING;

-- Credenciais padrão:
-- Login: admin
-- Senha: admin123
--
-- Após fazer login, você pode criar outros usuários pelo painel admin.

