-- ============================================================================
-- CORREÇÃO URGENTE DA CONSTRAINT DE STATUS
-- Execute este script AGORA para corrigir o erro imediatamente
-- ============================================================================

-- Remover TODAS as constraints de status (forçado)
DO $$
DECLARE
    constraint_record RECORD;
BEGIN
    -- Buscar e remover todas as constraints CHECK de status
    FOR constraint_record IN 
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'ocorrencias'::regclass
        AND contype = 'c'
        AND pg_get_constraintdef(oid) LIKE '%status%'
    LOOP
        BEGIN
            EXECUTE 'ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS ' || quote_ident(constraint_record.conname) || ' CASCADE';
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END LOOP;
END $$;

-- Remover constraint específica (caso tenha nome fixo)
ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS ocorrencias_status_check CASCADE;

-- Criar a constraint CORRETA
ALTER TABLE ocorrencias 
    ADD CONSTRAINT ocorrencias_status_check 
    CHECK (status IN ('ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA'));

-- Verificar se foi criada corretamente
SELECT 
    conname AS constraint_name,
    pg_get_constraintdef(oid) AS constraint_definition
FROM pg_constraint
WHERE conrelid = 'ocorrencias'::regclass
AND conname = 'ocorrencias_status_check';

