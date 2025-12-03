-- Script para atualizar a senha do usuário admin
-- 
-- PASSO 1: Gere o hash da senha acessando:
-- http://localhost:8081/api/util/hash?senha=admin123
--
-- PASSO 2: Copie o hash retornado
--
-- PASSO 3: Execute este SQL substituindo HASH_AQUI pelo hash copiado:

UPDATE usuarios 
SET senha_hash = 'HASH_AQUI'
WHERE login = 'admin';

-- Exemplo (NÃO USE ESTE HASH, gere o seu próprio):
-- UPDATE usuarios 
-- SET senha_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
-- WHERE login = 'admin';

