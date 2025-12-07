-- Script SQL para corrigir a inconsistência da coluna de data na tabela ocorrencias
-- Execute este script no DBeaver ou pgAdmin no banco pi_2025_2

-- Verificar e corrigir a estrutura da tabela ocorrencias
-- O modelo Java espera a coluna 'data_hora_abertura'

-- Se a coluna data_registro existe mas data_hora_abertura não existe, renomear
DO $$
BEGIN
    -- Se data_registro existe e data_hora_abertura não existe
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'data_registro'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'data_hora_abertura'
    ) THEN
        ALTER TABLE ocorrencias RENAME COLUMN data_registro TO data_hora_abertura;
        RAISE NOTICE 'Coluna data_registro renomeada para data_hora_abertura';
    END IF;
    
    -- Se ambas existem, usar data_hora_abertura e remover data_registro
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'data_registro'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'data_hora_abertura'
    ) THEN
        -- Copiar dados de data_registro para data_hora_abertura se necessário
        UPDATE ocorrencias 
        SET data_hora_abertura = COALESCE(data_hora_abertura, data_registro, CURRENT_TIMESTAMP)
        WHERE data_hora_abertura IS NULL;
        
        -- Remover data_registro
        ALTER TABLE ocorrencias DROP COLUMN data_registro;
        RAISE NOTICE 'Coluna data_registro removida, usando data_hora_abertura';
    END IF;
END $$;

-- Garantir que data_hora_abertura existe e está configurada corretamente
ALTER TABLE ocorrencias 
    ADD COLUMN IF NOT EXISTS data_hora_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Se a coluna já existe, apenas garantir as constraints
DO $$
BEGIN
    ALTER TABLE ocorrencias 
        ALTER COLUMN data_hora_abertura SET DEFAULT CURRENT_TIMESTAMP;
    
    -- Remover NOT NULL temporariamente se necessário, depois adicionar de volta
    BEGIN
        ALTER TABLE ocorrencias 
            ALTER COLUMN data_hora_abertura SET NOT NULL;
    EXCEPTION WHEN OTHERS THEN
        -- Se já está NOT NULL, não faz nada
        NULL;
    END;
END $$;

-- Verificar estrutura final
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'ocorrencias'
AND column_name IN ('data_registro', 'data_hora_abertura')
ORDER BY column_name;
