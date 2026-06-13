-- ============================================================================
-- SCRIPT DE LIMPEZA: REMOVER DUPLICATAS EXISTENTES DE PROFISSIONAIS
-- ============================================================================
-- Objetivo: Remover registros duplicados de profissionais criados antes da correção
-- ATENÇÃO: Execute ANTES de aplicar as constraints no arquivo de migração
-- Data: 2026-06-12
-- Status: OPCIONAL - Apenas se houver duplicatas existentes
-- ============================================================================

-- ============================================================================
-- SEÇÃO 1: DIAGNÓSTICO - IDENTIFICAR DUPLICATAS
-- ============================================================================
-- Use estas queries para identificar e documentar duplicatas ANTES de remover

-- Query 1: Encontrar profissionais com mesmo contato
SELECT 
    contato,
    COUNT(*) as total_duplicatas,
    array_agg(id ORDER BY id) as ids,
    array_agg(nome ORDER BY id) as nomes,
    array_agg(ativo ORDER BY id) as ativos,
    array_agg(data_criacao ORDER BY id) as datas
FROM profissionais
WHERE contato IS NOT NULL AND contato != ''
GROUP BY contato
HAVING COUNT(*) > 1
ORDER BY total_duplicatas DESC;

-- Query 2: Encontrar equipes com mesmo profissional duplicado
SELECT 
    id_equipe,
    id_profissional,
    COUNT(*) as total_duplicatas,
    array_agg(id) as ids_equipe_profissional
FROM equipe_profissional
GROUP BY id_equipe, id_profissional
HAVING COUNT(*) > 1
ORDER BY total_duplicatas DESC;

-- ============================================================================
-- SEÇÃO 2: REMOVER DUPLICATAS DE CONTATO EM PROFISSIONAIS
-- ============================================================================
-- Remove cópias posteriores, mantendo a primeira criada

-- Criar CTE para identificar quais registros manter
WITH duplicatas_contato AS (
    SELECT 
        id,
        contato,
        nome,
        ROW_NUMBER() OVER (PARTITION BY contato ORDER BY data_criacao ASC NULLS LAST, id ASC) as rn
    FROM profissionais
    WHERE contato IS NOT NULL 
    AND contato != ''
)
-- Deletar apenas as cópias posteriores (rn > 1)
DELETE FROM profissionais
WHERE id IN (
    SELECT id FROM duplicatas_contato WHERE rn > 1
);

-- Log de quantos foram deletados
DO $$
DECLARE
    registros_deletados INTEGER;
BEGIN
    GET DIAGNOSTICS registros_deletados = ROW_COUNT;
    RAISE NOTICE 'Profissionais duplicados removidos (baseado em contato): %', registros_deletados;
END $$;

-- ============================================================================
-- SEÇÃO 3: REMOVER DUPLICATAS EM EQUIPE_PROFISSIONAL
-- ============================================================================
-- Remove cópias posteriores da mesma relação equipe-profissional

WITH duplicatas_equipe_prof AS (
    SELECT 
        id,
        id_equipe,
        id_profissional,
        ROW_NUMBER() OVER (PARTITION BY id_equipe, id_profissional ORDER BY id ASC) as rn
    FROM equipe_profissional
)
DELETE FROM equipe_profissional
WHERE id IN (
    SELECT id FROM duplicatas_equipe_prof WHERE rn > 1
);

-- Log de quantos foram deletados
DO $$
DECLARE
    registros_deletados INTEGER;
BEGIN
    GET DIAGNOSTICS registros_deletados = ROW_COUNT;
    RAISE NOTICE 'Relacionamentos equipe-profissional duplicados removidos: %', registros_deletados;
END $$;

-- ============================================================================
-- SEÇÃO 4: VERIFICAÇÃO FINAL
-- ============================================================================
-- Verificar se todas as duplicatas foram removidas

-- Query 3: Verificar se ainda há duplicatas de contato
SELECT 
    contato,
    COUNT(*) as total
FROM profissionais
WHERE contato IS NOT NULL AND contato != ''
GROUP BY contato
HAVING COUNT(*) > 1;

-- Query 4: Verificar se ainda há duplicatas em equipe_profissional
SELECT 
    id_equipe,
    id_profissional,
    COUNT(*) as total
FROM equipe_profissional
GROUP BY id_equipe, id_profissional
HAVING COUNT(*) > 1;

-- ============================================================================
-- FIM DO SCRIPT
-- ============================================================================
RAISE NOTICE 'Script de limpeza executado com sucesso!';
RAISE NOTICE 'Próximo passo: Aplicar as migrations do arquivo V001_FIX_DUPLICACAO_PROFISSIONAIS.sql';
