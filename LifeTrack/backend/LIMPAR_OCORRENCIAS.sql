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

-- 1. Deletar rotas de atendimento (conexões calculadas pelo Dijkstra)
--    Isso será deletado automaticamente quando deletarmos atendimentos,
--    mas vamos fazer explicitamente para garantir
DELETE FROM atendimento_rota_conexao;

-- 2. Resetar status das ambulâncias que estão EM_ATENDIMENTO para DISPONIVEL
UPDATE ambulancias 
SET status = 'DISPONIVEL' 
WHERE status = 'EM_ATENDIMENTO';

-- 3. Resetar status dos profissionais que estão EM_ATENDIMENTO para DISPONIVEL
UPDATE profissionais 
SET status = 'DISPONIVEL' 
WHERE status = 'EM_ATENDIMENTO';

-- 4. Deletar todos os atendimentos
--    Isso deve ser feito antes de deletar ocorrências devido à FK
DELETE FROM atendimentos;

-- 5. Deletar todas as ocorrências
--    O histórico será deletado automaticamente devido ao ON DELETE CASCADE
DELETE FROM ocorrencias;

-- 6. Verificar se há histórico órfão (não deveria ter, mas por segurança)
DELETE FROM historico_ocorrencias 
WHERE id_ocorrencia NOT IN (SELECT id FROM ocorrencias);

-- 7. Resetar sequências (opcional, mas recomendado para manter IDs limpos)
--    Isso fará com que novas ocorrências comecem do ID 1
ALTER SEQUENCE IF EXISTS ocorrencias_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS atendimentos_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS historico_ocorrencias_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS atendimento_rota_conexao_id_seq RESTART WITH 1;

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

