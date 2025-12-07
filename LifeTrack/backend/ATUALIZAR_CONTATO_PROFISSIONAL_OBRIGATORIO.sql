-- ============================================================================
-- SCRIPT PARA TORNAR O CAMPO CONTATO OBRIGATÓRIO NA TABELA PROFISSIONAIS
-- ============================================================================
-- Este script atualiza a coluna contato na tabela profissionais para ser NOT NULL
-- ATENÇÃO: Execute este script apenas se todos os profissionais já tiverem contato
-- Caso contrário, primeiro atualize os registros sem contato
-- ============================================================================

-- Verificar se há profissionais sem contato
-- SELECT id, nome, contato FROM profissionais WHERE contato IS NULL OR contato = '';

-- Se houver registros sem contato, atualize-os primeiro:
-- UPDATE profissionais SET contato = '(00) 00000-0000' WHERE contato IS NULL OR contato = '';

-- Tornar o campo contato obrigatório
ALTER TABLE profissionais 
    ALTER COLUMN contato SET NOT NULL;

-- Comentário para documentação
COMMENT ON COLUMN profissionais.contato IS 'Telefone de contato do profissional (obrigatório)';

-- ============================================================================
-- FIM DO SCRIPT
-- ============================================================================

