-- ============================================================================
-- SCRIPT PARA LIMPAR TODAS AS OCORRÊNCIAS E ATENDIMENTOS
-- ============================================================================
-- Este script remove todas as ocorrências, atendimentos e dados relacionados,
-- mas MANTÉM todos os outros dados (equipes, profissionais, ambulâncias, etc.)
--
-- ATENÇÃO: Esta operação é IRREVERSÍVEL!
-- Execute apenas se tiver certeza de que deseja limpar os dados de ocorrências.
-- ============================================================================

BEGIN;

-- 1. Resetar status das ambulâncias e profissionais ANTES de deletar (importante para evitar erros)
--    Isso remove o bloqueio de edição/desativação que implementamos
UPDATE ambulancias 
SET status = 'DISPONIVEL' 
WHERE status = 'EM_ATENDIMENTO';

UPDATE profissionais 
SET status = 'DISPONIVEL' 
WHERE status = 'EM_ATENDIMENTO';

-- 2. Deletar rotas de atendimento (conexões calculadas pelo Dijkstra)
--    Tem ON DELETE CASCADE, mas deletamos explicitamente para garantir ordem
DELETE FROM atendimento_rota_conexao;

-- 3. Deletar histórico de ocorrências
--    Tem ON DELETE CASCADE na FK de ocorrencias, mas deletamos explicitamente para garantir ordem
DELETE FROM historico_ocorrencias;

-- 4. Deletar todos os atendimentos
--    IMPORTANTE: Deve ser deletado ANTES das ocorrências porque NÃO tem ON DELETE CASCADE
--    Se der erro aqui, tente usar LIMPAR_OCORRENCIAS_ALTERNATIVO.sql que usa TRUNCATE CASCADE
DELETE FROM atendimentos;

-- 5. Deletar todas as ocorrências
--    Agora que atendimentos foram deletados, podemos deletar ocorrências
DELETE FROM ocorrencias;

-- 6. Resetar sequências (opcional, mas recomendado para manter IDs limpos)
--    Isso fará com que novas ocorrências comecem do ID 1
DO $$
BEGIN
    -- Resetar sequências apenas se existirem
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

-- ============================================================================
-- VERIFICAÇÃO PÓS-LIMPEZA
-- ============================================================================
-- Execute estas queries para verificar se a limpeza foi bem-sucedida:

-- Verificar se não há mais ocorrências
SELECT COUNT(*) as total_ocorrencias FROM ocorrencias;
-- Resultado esperado: 0

-- Verificar se não há mais atendimentos
SELECT COUNT(*) as total_atendimentos FROM atendimentos;
-- Resultado esperado: 0

-- Verificar se não há mais histórico
SELECT COUNT(*) as total_historico FROM historico_ocorrencias;
-- Resultado esperado: 0

-- Verificar se não há mais rotas
SELECT COUNT(*) as total_rotas FROM atendimento_rota_conexao;
-- Resultado esperado: 0

-- Verificar se as ambulâncias estão disponíveis
SELECT COUNT(*) as ambulancias_disponiveis 
FROM ambulancias 
WHERE status = 'DISPONIVEL';
-- Resultado esperado: Todas as ambulâncias ativas

-- Verificar se os profissionais estão disponíveis
SELECT COUNT(*) as profissionais_disponiveis 
FROM profissionais 
WHERE status = 'DISPONIVEL';
-- Resultado esperado: Todos os profissionais ativos

-- Verificar se as equipes foram mantidas
SELECT COUNT(*) as total_equipes FROM equipes;
-- Resultado esperado: Todas as equipes existentes (não deve ser 0)

-- ============================================================================
-- FIM DO SCRIPT
-- ============================================================================

