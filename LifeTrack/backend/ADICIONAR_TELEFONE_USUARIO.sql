-- ============================================================================
-- SCRIPT PARA ADICIONAR CAMPO TELEFONE NA TABELA USUARIOS
-- ============================================================================
-- Este script adiciona a coluna telefone na tabela usuarios
-- Execute este script antes de usar a funcionalidade de criação de usuários
-- ============================================================================

ALTER TABLE usuarios 
    ADD COLUMN IF NOT EXISTS telefone VARCHAR(20);

-- Comentário para documentação
COMMENT ON COLUMN usuarios.telefone IS 'Telefone de contato do usuário (obrigatório na criação)';

-- ============================================================================
-- FIM DO SCRIPT
-- ============================================================================

