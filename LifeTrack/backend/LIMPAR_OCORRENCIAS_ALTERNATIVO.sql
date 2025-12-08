-- ============================================================================
-- SCRIPT ALTERNATIVO PARA LIMPAR TODAS AS OCORRÊNCIAS E ATENDIMENTOS
-- ============================================================================
-- Versão alternativa que desabilita temporariamente as constraints
-- Use este script se o LIMPAR_OCORRENCIAS.sql não funcionar
-- ============================================================================

BEGIN;

-- 1. Resetar status das ambulâncias e profissionais ANTES de deletar
UPDATE ambulancias 
SET status = 'DISPONIVEL' 
WHERE status = 'EM_ATENDIMENTO';

UPDATE profissionais 
SET status = 'DISPONIVEL' 
WHERE status = 'EM_ATENDIMENTO';

-- 2. Deletar usando TRUNCATE CASCADE (mais eficiente e garante ordem correta)
TRUNCATE TABLE atendimento_rota_conexao CASCADE;
TRUNCATE TABLE historico_ocorrencias CASCADE;
TRUNCATE TABLE atendimentos CASCADE;
TRUNCATE TABLE ocorrencias CASCADE;

-- 3. Resetar sequências
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'ocorrencias_id_seq') THEN
        ALTER SEQUENCE ocorrencias_id_seq RESTART WITH 1;
    END IF;
    
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'atendimentos_id_seq') THEN
        ALTER SEQUENCE atendimentos_id_seq RESTART WITH 1;
    END IF;
    
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'historico_ocorrencias_id_seq') THEN
        ALTER SEQUENCE historico_ocorrencias_id_seq RESTART WITH 1;
    END IF;
    
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'atendimento_rota_conexao_id_seq') THEN
        ALTER SEQUENCE atendimento_rota_conexao_id_seq RESTART WITH 1;
    END IF;
END $$;

COMMIT;

-- Verificações
SELECT COUNT(*) as total_ocorrencias FROM ocorrencias; -- Deve ser 0
SELECT COUNT(*) as total_atendimentos FROM atendimentos; -- Deve ser 0
SELECT COUNT(*) as total_historico FROM historico_ocorrencias; -- Deve ser 0
SELECT COUNT(*) as total_rotas FROM atendimento_rota_conexao; -- Deve ser 0

