-- ============================================================================
-- SCRIPT SIMPLES PARA LIMPAR OCORRÊNCIAS - EXECUTE TUDO DE UMA VEZ
-- ============================================================================
-- Versão ultra-simplificada, sem blocos DO, funciona em qualquer cliente SQL
-- ============================================================================

-- 1. Resetar status (remove bloqueios)
UPDATE ambulancias SET status = 'DISPONIVEL' WHERE status = 'EM_ATENDIMENTO';
UPDATE profissionais SET status = 'DISPONIVEL' WHERE status = 'EM_ATENDIMENTO';

-- 2. Limpar dados (na ordem correta)
DELETE FROM atendimento_rota_conexao;
DELETE FROM historico_ocorrencias;
DELETE FROM atendimentos;
DELETE FROM ocorrencias;

-- 3. Resetar sequências (versão simples sem blocos DO)
ALTER SEQUENCE IF EXISTS ocorrencias_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS atendimentos_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS historico_ocorrencias_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS atendimento_rota_conexao_id_seq RESTART WITH 1;


