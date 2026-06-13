-- ============================================================================
-- SCRIPT DE CORREÇÃO: DUPLICAÇÃO DE PROFISSIONAIS
-- ============================================================================
-- Objetivo: Adicionar constraints e validações para prevenir duplicação de dados
-- Data: 2026-06-12
-- Status: CRÍTICO - Execução obrigatória
-- ============================================================================

-- ============================================================================
-- SEÇÃO 1: CORRIGIR NOME DA TABELA equipe_profissional
-- ============================================================================
-- A tabela foi criada com nome diferente, precisa ser padronizada
DO $$
BEGIN
    -- Renomear tabela se existir com nome errado
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_name = 'equipes_profissionais'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_name = 'equipe_profissional'
    ) THEN
        ALTER TABLE equipes_profissionais RENAME TO equipe_profissional;
    END IF;
END $$;

-- ============================================================================
-- SEÇÃO 2: GARANTIR CONSTRAINT UNIQUE EM equipe_profissional
-- ============================================================================
-- Impede que o mesmo profissional apareça duas vezes na mesma equipe
DO $$
BEGIN
    -- Remover constraint anterior se existir (com nomes variados)
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'equipe_profissional' 
        AND constraint_type = 'UNIQUE'
        AND constraint_name LIKE '%equipe%profissional%'
    ) THEN
        BEGIN
            ALTER TABLE equipe_profissional 
                DROP CONSTRAINT IF EXISTS uq_equipe_profissional CASCADE;
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END IF;

    -- Verificar se há dados duplicados ANTES de adicionar constraint
    IF EXISTS (
        SELECT id_equipe, id_profissional, COUNT(*) 
        FROM equipe_profissional 
        WHERE id_equipe IS NOT NULL AND id_profissional IS NOT NULL
        GROUP BY id_equipe, id_profissional 
        HAVING COUNT(*) > 1
    ) THEN
        -- Se houver duplicatas, remover as linhas posteriores (manter a primeira)
        DELETE FROM equipe_profissional 
        WHERE id NOT IN (
            SELECT MIN(id) 
            FROM equipe_profissional 
            WHERE id_equipe IS NOT NULL AND id_profissional IS NOT NULL
            GROUP BY id_equipe, id_profissional
        );
    END IF;

    -- Adicionar constraint UNIQUE
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'equipe_profissional' 
        AND constraint_type = 'UNIQUE'
        AND constraint_name = 'uq_equipe_profissional'
    ) THEN
        BEGIN
            ALTER TABLE equipe_profissional 
                ADD CONSTRAINT uq_equipe_profissional 
                UNIQUE (id_equipe, id_profissional);
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END IF;
END $$;

-- ============================================================================
-- SEÇÃO 3: GARANTIR CONSTRAINT UNIQUE NO CONTATO DOS PROFISSIONAIS
-- ============================================================================
-- Impede que dois profissionais tenham o mesmo telefone
DO $$
BEGIN
    -- Verificar se há duplicatas de contato
    IF EXISTS (
        SELECT contato, COUNT(*) 
        FROM profissionais 
        WHERE contato IS NOT NULL AND contato != ''
        GROUP BY contato 
        HAVING COUNT(*) > 1
    ) THEN
        -- Log das duplicatas encontradas (para diagnóstico)
        RAISE WARNING 'Duplicatas de contato encontradas em profissionais. Verifique os logs.';
        
        -- Marcar como inativas as cópias posteriores da mesma pessoa (manter a primeira ativa)
        WITH duplicatas AS (
            SELECT 
                id, 
                contato, 
                nome,
                ROW_NUMBER() OVER (PARTITION BY contato ORDER BY id ASC) as rn
            FROM profissionais
            WHERE contato IS NOT NULL AND contato != ''
        )
        UPDATE profissionais 
        SET ativo = false 
        FROM duplicatas 
        WHERE profissionais.id = duplicatas.id 
        AND duplicatas.rn > 1;
    END IF;

    -- Adicionar constraint UNIQUE no contato
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'profissionais' 
        AND constraint_type = 'UNIQUE'
        AND constraint_name = 'uq_profissional_contato'
    ) THEN
        BEGIN
            ALTER TABLE profissionais 
                ADD CONSTRAINT uq_profissional_contato 
                UNIQUE (contato);
        EXCEPTION WHEN duplicate_key_value_violates_unique_constraint THEN
            -- Se falhar por causa de duplicatas, elas já foram marcadas como inativas acima
            NULL;
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END IF;
END $$;

-- ============================================================================
-- SEÇÃO 4: CRIAR ÍNDICES PARA PERFORMANCE
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_profissional_contato ON profissionais(contato);
CREATE INDEX IF NOT EXISTS idx_profissional_ativo ON profissionais(ativo);
CREATE INDEX IF NOT EXISTS idx_profissional_status ON profissionais(status);
CREATE INDEX IF NOT EXISTS idx_profissional_turno ON profissionais(turno);

-- ============================================================================
-- SEÇÃO 5: ADICIONAR COLUNAS DE AUDITORIA (opcional, para rastreabilidade)
-- ============================================================================
DO $$
BEGIN
    -- Adicionar coluna de data de criação se não existir
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'profissionais' 
        AND column_name = 'data_criacao'
    ) THEN
        ALTER TABLE profissionais 
            ADD COLUMN data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;

    -- Adicionar coluna de última atualização se não existir
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'profissionais' 
        AND column_name = 'data_atualizacao'
    ) THEN
        ALTER TABLE profissionais 
            ADD COLUMN data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- ============================================================================
-- SEÇÃO 6: VERIFICAÇÃO FINAL
-- ============================================================================
-- Script de diagnóstico para verificar se há inconsistências
DO $$
DECLARE
    count_profissionais INTEGER;
    count_equipe_profissional INTEGER;
    count_duplicatas_contato INTEGER;
BEGIN
    -- Contar profissionais
    SELECT COUNT(*) INTO count_profissionais FROM profissionais;
    RAISE NOTICE 'Total de profissionais: %', count_profissionais;

    -- Contar relacionamentos equipe-profissional
    SELECT COUNT(*) INTO count_equipe_profissional FROM equipe_profissional;
    RAISE NOTICE 'Total de relacionamentos equipe-profissional: %', count_equipe_profissional;

    -- Procurar por possíveis duplicatas de contato
    SELECT COUNT(*) INTO count_duplicatas_contato 
    FROM (
        SELECT contato, COUNT(*) 
        FROM profissionais 
        WHERE contato IS NOT NULL AND contato != '' AND ativo = true
        GROUP BY contato 
        HAVING COUNT(*) > 1
    ) subquery;
    
    IF count_duplicatas_contato > 0 THEN
        RAISE WARNING 'ATENÇÃO: % contatos ainda aparecem duplicados em profissionais ativos!', count_duplicatas_contato;
    ELSE
        RAISE NOTICE 'Verificação OK: Nenhum contato duplicado em profissionais ativos.';
    END IF;
END $$;

-- ============================================================================
-- FIM DO SCRIPT
-- ============================================================================
COMMENT ON TABLE equipe_profissional IS 'Relacionamento entre equipes e profissionais - com constraint UNIQUE para evitar duplicação';
COMMENT ON COLUMN profissionais.contato IS 'Telefone/contato do profissional - ÚNICO para evitar duplicação';
