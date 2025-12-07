-- ============================================================================
-- SCRIPT ÚNICO E COMPLETO DE CONFIGURAÇÃO DO BANCO DE DADOS
-- Sistema: LifeTrack - SOS Rota
-- Executado automaticamente pelo Spring Boot na inicialização
-- ============================================================================

-- ============================================================================
-- SEÇÃO 1: CRIAÇÃO DAS TABELAS BASE
-- ============================================================================

-- Tabela de bairros (vértices do grafo)
CREATE TABLE IF NOT EXISTS bairros (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela de ruas/conexões (arestas do grafo)
CREATE TABLE IF NOT EXISTS ruas_conexoes (
    id BIGSERIAL PRIMARY KEY,
    id_bairro_origem BIGINT NOT NULL,
    id_bairro_destino BIGINT NOT NULL,
    distancia_km DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (id_bairro_origem) REFERENCES bairros(id),
    FOREIGN KEY (id_bairro_destino) REFERENCES bairros(id),
    UNIQUE(id_bairro_origem, id_bairro_destino)
);

-- Tabela de ambulâncias
CREATE TABLE IF NOT EXISTS ambulancias (
    id BIGSERIAL PRIMARY KEY,
    placa VARCHAR(10) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('BASICA', 'UTI')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('DISPONIVEL', 'EM_ATENDIMENTO', 'EM_MANUTENCAO', 'INATIVA')),
    id_bairro_base BIGINT NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_bairro_base) REFERENCES bairros(id)
);

-- Tabela de profissionais
CREATE TABLE IF NOT EXISTS profissionais (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    funcao VARCHAR(20) NOT NULL CHECK (funcao IN ('MEDICO', 'ENFERMEIRO', 'CONDUTOR')),
    contato VARCHAR(100),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabela de equipes
CREATE TABLE IF NOT EXISTS equipes (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    id_ambulancia BIGINT NOT NULL UNIQUE,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_ambulancia) REFERENCES ambulancias(id)
);

-- Tabela de relacionamento equipe-profissional
CREATE TABLE IF NOT EXISTS equipe_profissional (
    id BIGSERIAL PRIMARY KEY,
    id_equipe BIGINT NOT NULL,
    id_profissional BIGINT NOT NULL,
    FOREIGN KEY (id_equipe) REFERENCES equipes(id) ON DELETE CASCADE,
    FOREIGN KEY (id_profissional) REFERENCES profissionais(id),
    UNIQUE(id_equipe, id_profissional)
);

-- Tabela de usuários (deve vir antes de ocorrencias por causa da FK)
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    nome VARCHAR(255),
    email VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabela de ocorrências (sem constraint de status por enquanto - será corrigida depois)
CREATE TABLE IF NOT EXISTS ocorrencias (
    id BIGSERIAL PRIMARY KEY,
    descricao TEXT NOT NULL,
    tipo_ocorrencia VARCHAR(255) NOT NULL,
    gravidade VARCHAR(20) NOT NULL CHECK (gravidade IN ('BAIXA', 'MEDIA', 'ALTA', 'CRITICA')),
    status VARCHAR(20) NOT NULL,
    id_bairro_origem BIGINT NOT NULL,
    id_bairro_destino BIGINT,
    id_equipe_atribuida BIGINT,
    data_hora_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacoes VARCHAR(1000),
    id_usuario_registro BIGINT,
    FOREIGN KEY (id_bairro_origem) REFERENCES bairros(id),
    FOREIGN KEY (id_bairro_destino) REFERENCES bairros(id),
    FOREIGN KEY (id_equipe_atribuida) REFERENCES equipes(id),
    FOREIGN KEY (id_usuario_registro) REFERENCES usuarios(id)
);

-- Tabela de atendimentos
CREATE TABLE IF NOT EXISTS atendimentos (
    id BIGSERIAL PRIMARY KEY,
    id_ocorrencia BIGINT NOT NULL,
    id_ambulancia BIGINT NOT NULL,
    id_equipe BIGINT NOT NULL,
    data_hora_despacho TIMESTAMP,
    data_hora_chegada TIMESTAMP,
    distancia_km DECIMAL(10, 2),
    id_usuario_despacho BIGINT,
    FOREIGN KEY (id_ocorrencia) REFERENCES ocorrencias(id),
    FOREIGN KEY (id_ambulancia) REFERENCES ambulancias(id),
    FOREIGN KEY (id_equipe) REFERENCES equipes(id),
    FOREIGN KEY (id_usuario_despacho) REFERENCES usuarios(id)
);

-- ============================================================================
-- SEÇÃO 2: CORREÇÃO DA CONSTRAINT DE STATUS (CRÍTICO!)
-- ============================================================================

-- Remover TODAS as constraints CHECK relacionadas ao campo status de forma FORÇADA
DO $$
DECLARE
    constraint_record RECORD;
BEGIN
    -- Primeiro, tentar remover pela busca no pg_constraint
    FOR constraint_record IN 
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'ocorrencias'::regclass
        AND contype = 'c'
        AND (
            conname LIKE '%status%' 
            OR pg_get_constraintdef(oid) LIKE '%status%'
        )
    LOOP
        BEGIN
            EXECUTE 'ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS ' || quote_ident(constraint_record.conname) || ' CASCADE';
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END LOOP;
    
    -- Remover constraint específica (múltiplas tentativas para garantir)
    BEGIN
        ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS ocorrencias_status_check CASCADE;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;
    
    -- Tentar remover sem o nome exato (caso tenha nome diferente)
    BEGIN
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
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;
END $$;

-- SEMPRE recriar a constraint correta (sem verificação - sempre remove e recria)
DO $$
BEGIN
    -- Primeiro, remover a constraint se existir (sem erro se não existir)
    ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS ocorrencias_status_check CASCADE;
EXCEPTION WHEN OTHERS THEN
    NULL;
END $$;

-- Criar a constraint correta em um bloco separado
DO $$
BEGIN
    ALTER TABLE ocorrencias 
        ADD CONSTRAINT ocorrencias_status_check 
        CHECK (status IN ('ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA'));
EXCEPTION WHEN duplicate_object THEN
    -- Se ainda existe, remover e recriar
    ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS ocorrencias_status_check CASCADE;
    ALTER TABLE ocorrencias 
        ADD CONSTRAINT ocorrencias_status_check 
        CHECK (status IN ('ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA'));
EXCEPTION WHEN OTHERS THEN
    NULL;
END $$;

-- ============================================================================
-- SEÇÃO 3: CORREÇÃO DE ESTRUTURA - COLUNAS DE BAIRRO
-- ============================================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'id_bairro_local'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'id_bairro_origem'
    ) THEN
        ALTER TABLE ocorrencias RENAME COLUMN id_bairro_local TO id_bairro_origem;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'id_bairro_local'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'id_bairro_origem'
    ) THEN
        UPDATE ocorrencias 
        SET id_bairro_origem = COALESCE(id_bairro_origem, id_bairro_local)
        WHERE id_bairro_origem IS NULL;
        
        BEGIN
            ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS fk_ocorrencia_bairro_local;
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
        
        ALTER TABLE ocorrencias DROP COLUMN IF EXISTS id_bairro_local;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'id_bairro_origem'
    ) THEN
        ALTER TABLE ocorrencias 
            ADD COLUMN id_bairro_origem BIGINT;
        
        IF EXISTS (SELECT 1 FROM ocorrencias) THEN
            UPDATE ocorrencias 
            SET id_bairro_origem = (SELECT id FROM bairros LIMIT 1)
            WHERE id_bairro_origem IS NULL;
        END IF;
        
        ALTER TABLE ocorrencias 
            ALTER COLUMN id_bairro_origem SET NOT NULL;
        
        BEGIN
            ALTER TABLE ocorrencias 
                ADD CONSTRAINT fk_ocorrencia_bairro_origem 
                FOREIGN KEY (id_bairro_origem) REFERENCES bairros(id);
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'id_bairro_origem'
        AND is_nullable = 'YES'
    ) THEN
        IF EXISTS (SELECT 1 FROM ocorrencias WHERE id_bairro_origem IS NULL) THEN
            UPDATE ocorrencias 
            SET id_bairro_origem = (SELECT id FROM bairros LIMIT 1)
            WHERE id_bairro_origem IS NULL;
        END IF;
        
        ALTER TABLE ocorrencias 
            ALTER COLUMN id_bairro_origem SET NOT NULL;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'ocorrencias' 
        AND constraint_name = 'fk_ocorrencia_bairro_origem'
    ) THEN
        BEGIN
            ALTER TABLE ocorrencias 
                ADD CONSTRAINT fk_ocorrencia_bairro_origem 
                FOREIGN KEY (id_bairro_origem) REFERENCES bairros(id);
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END IF;
END $$;

-- ============================================================================
-- SEÇÃO 4: CORREÇÃO DE ESTRUTURA - COLUNA DATA
-- ============================================================================

DO $$
BEGIN
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
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'data_registro'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ocorrencias' 
        AND column_name = 'data_hora_abertura'
    ) THEN
        UPDATE ocorrencias 
        SET data_hora_abertura = COALESCE(data_hora_abertura, data_registro, CURRENT_TIMESTAMP)
        WHERE data_hora_abertura IS NULL;
        
        ALTER TABLE ocorrencias DROP COLUMN IF EXISTS data_registro;
    END IF;
END $$;

ALTER TABLE ocorrencias 
    ADD COLUMN IF NOT EXISTS data_hora_abertura TIMESTAMP;

DO $$
BEGIN
    ALTER TABLE ocorrencias 
        ALTER COLUMN data_hora_abertura SET DEFAULT CURRENT_TIMESTAMP;
    
    UPDATE ocorrencias 
    SET data_hora_abertura = CURRENT_TIMESTAMP 
    WHERE data_hora_abertura IS NULL;
    
    BEGIN
        ALTER TABLE ocorrencias 
            ALTER COLUMN data_hora_abertura SET NOT NULL;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;
END $$;

-- ============================================================================
-- SEÇÃO 5: CAMPOS DE AUDITORIA
-- ============================================================================

ALTER TABLE ocorrencias 
    ADD COLUMN IF NOT EXISTS id_usuario_registro BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_ocorrencia_usuario_registro'
    ) THEN
        BEGIN
            ALTER TABLE ocorrencias 
                ADD CONSTRAINT fk_ocorrencia_usuario_registro 
                FOREIGN KEY (id_usuario_registro) REFERENCES usuarios(id);
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END IF;
END $$;

ALTER TABLE atendimentos 
    ADD COLUMN IF NOT EXISTS id_usuario_despacho BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_atendimento_usuario_despacho'
    ) THEN
        BEGIN
            ALTER TABLE atendimentos 
                ADD CONSTRAINT fk_atendimento_usuario_despacho 
                FOREIGN KEY (id_usuario_despacho) REFERENCES usuarios(id);
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END IF;
END $$;

-- Adicionar coluna id_equipe em atendimentos (se não existir)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'atendimentos' 
        AND column_name = 'id_equipe'
    ) THEN
        ALTER TABLE atendimentos 
            ADD COLUMN id_equipe BIGINT;
        
        -- Preencher com a equipe da ambulância se houver atendimentos existentes
        UPDATE atendimentos a
        SET id_equipe = (
            SELECT e.id
            FROM equipes e
            WHERE e.id_ambulancia = a.id_ambulancia
            AND e.ativa = true
            LIMIT 1
        )
        WHERE a.id_equipe IS NULL;
        
        -- Tornar NOT NULL após preencher
        BEGIN
            ALTER TABLE atendimentos 
                ALTER COLUMN id_equipe SET NOT NULL;
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
        
        -- Adicionar constraint FK
        BEGIN
            ALTER TABLE atendimentos 
                ADD CONSTRAINT fk_atendimento_equipe 
                FOREIGN KEY (id_equipe) REFERENCES equipes(id);
        EXCEPTION WHEN duplicate_object THEN
            NULL;
        END;
    ELSE
        -- Garantir que é NOT NULL
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'atendimentos' 
            AND column_name = 'id_equipe'
            AND is_nullable = 'YES'
        ) THEN
            UPDATE atendimentos a
            SET id_equipe = (
                SELECT e.id
                FROM equipes e
                WHERE e.id_ambulancia = a.id_ambulancia
                AND e.ativa = true
                LIMIT 1
            )
            WHERE a.id_equipe IS NULL;
            
            BEGIN
                ALTER TABLE atendimentos 
                    ALTER COLUMN id_equipe SET NOT NULL;
            EXCEPTION WHEN OTHERS THEN
                NULL;
            END;
        END IF;
        
        -- Garantir que a constraint FK existe
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.table_constraints 
            WHERE table_name = 'atendimentos' 
            AND constraint_name = 'fk_atendimento_equipe'
        ) THEN
            BEGIN
                ALTER TABLE atendimentos 
                    ADD CONSTRAINT fk_atendimento_equipe 
                    FOREIGN KEY (id_equipe) REFERENCES equipes(id);
            EXCEPTION WHEN OTHERS THEN
                NULL;
            END;
        END IF;
    END IF;
END $$;

-- ============================================================================
-- SEÇÃO 6: TABELAS AUXILIARES
-- ============================================================================

CREATE TABLE IF NOT EXISTS historico_ocorrencias (
    id BIGSERIAL PRIMARY KEY,
    id_ocorrencia BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    acao VARCHAR(50) NOT NULL CHECK (acao IN ('ABERTURA', 'DESPACHO', 'CHEGADA', 'ALTERACAO_STATUS', 'CANCELAMENTO', 'CONCLUSAO')),
    status_anterior VARCHAR(20),
    status_novo VARCHAR(20) NOT NULL,
    descricao_acao TEXT,
    data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_ocorrencia VARCHAR(255),
    gravidade VARCHAR(20),
    bairro_origem_nome VARCHAR(255),
    observacoes VARCHAR(1000),
    usuario_nome VARCHAR(255),
    usuario_login VARCHAR(100),
    usuario_perfil VARCHAR(50),
    FOREIGN KEY (id_ocorrencia) REFERENCES ocorrencias(id) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS atendimento_rota_conexao (
    id BIGSERIAL PRIMARY KEY,
    id_atendimento BIGINT NOT NULL,
    id_rua_conexao BIGINT NOT NULL,
    ordem INTEGER NOT NULL,
    FOREIGN KEY (id_atendimento) REFERENCES atendimentos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_rua_conexao) REFERENCES rua_conexoes(id),
    UNIQUE(id_atendimento, id_rua_conexao, ordem)
);

-- ============================================================================
-- SEÇÃO 7: CAMPOS DE SLA E TEMPO DE ATENDIMENTO
-- ============================================================================

ALTER TABLE ocorrencias 
    ADD COLUMN IF NOT EXISTS data_hora_fechamento TIMESTAMP,
    ADD COLUMN IF NOT EXISTS tempo_atendimento_minutos INTEGER,
    ADD COLUMN IF NOT EXISTS sla_minutos INTEGER,
    ADD COLUMN IF NOT EXISTS sla_cumprido BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS tempo_excedido_minutos INTEGER;

-- ============================================================================
-- SEÇÃO 8: CAMPOS ADICIONAIS EM PROFISSIONAIS
-- ============================================================================

ALTER TABLE profissionais 
    ADD COLUMN IF NOT EXISTS turno VARCHAR(20) DEFAULT 'MANHA';

ALTER TABLE profissionais 
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'DISPONIVEL';

DO $$
BEGIN
    BEGIN
        ALTER TABLE profissionais DROP CONSTRAINT IF EXISTS profissionais_turno_check;
        ALTER TABLE profissionais DROP CONSTRAINT IF EXISTS profissionais_status_check;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;
    
    BEGIN
        ALTER TABLE profissionais 
            ADD CONSTRAINT profissionais_turno_check 
            CHECK (turno IN ('MANHA', 'TARDE', 'NOITE'));
    EXCEPTION WHEN duplicate_object THEN
        NULL;
    END;
    
    BEGIN
        ALTER TABLE profissionais 
            ADD CONSTRAINT profissionais_status_check 
            CHECK (status IN ('DISPONIVEL', 'EM_ATENDIMENTO', 'EM_FOLGA', 'INATIVO'));
    EXCEPTION WHEN duplicate_object THEN
        NULL;
    END;
    
    UPDATE profissionais SET turno = 'MANHA' WHERE turno IS NULL;
    UPDATE profissionais SET status = 'DISPONIVEL' WHERE status IS NULL;
    
    BEGIN
        ALTER TABLE profissionais ALTER COLUMN turno SET NOT NULL;
        ALTER TABLE profissionais ALTER COLUMN status SET NOT NULL;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;
    
    ALTER TABLE profissionais ALTER COLUMN turno SET DEFAULT 'MANHA';
    ALTER TABLE profissionais ALTER COLUMN status SET DEFAULT 'DISPONIVEL';
END $$;

-- ============================================================================
-- SEÇÃO 9: ÍNDICES PARA PERFORMANCE
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_ambulancias_bairro ON ambulancias(id_bairro_base);
CREATE INDEX IF NOT EXISTS idx_equipes_ambulancia ON equipes(id_ambulancia);
CREATE INDEX IF NOT EXISTS idx_equipe_profissional_equipe ON equipe_profissional(id_equipe);
CREATE INDEX IF NOT EXISTS idx_equipe_profissional_profissional ON equipe_profissional(id_profissional);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_bairro_origem ON ocorrencias(id_bairro_origem);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_status ON ocorrencias(status);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_gravidade ON ocorrencias(gravidade);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_equipe ON ocorrencias(id_equipe_atribuida);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_usuario_registro ON ocorrencias(id_usuario_registro);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_data_abertura ON ocorrencias(data_hora_abertura);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_data_fechamento ON ocorrencias(data_hora_fechamento);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_sla_cumprido ON ocorrencias(sla_cumprido);
CREATE INDEX IF NOT EXISTS idx_atendimentos_ocorrencia ON atendimentos(id_ocorrencia);
CREATE INDEX IF NOT EXISTS idx_atendimentos_ambulancia ON atendimentos(id_ambulancia);
CREATE INDEX IF NOT EXISTS idx_atendimentos_usuario_despacho ON atendimentos(id_usuario_despacho);
CREATE INDEX IF NOT EXISTS idx_atendimentos_data_despacho ON atendimentos(data_hora_despacho);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_id_ocorrencia ON historico_ocorrencias(id_ocorrencia);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_id_usuario ON historico_ocorrencias(id_usuario);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_data_hora ON historico_ocorrencias(data_hora DESC);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_acao ON historico_ocorrencias(acao);
CREATE INDEX IF NOT EXISTS idx_atendimento_rota_conexao_atendimento ON atendimento_rota_conexao(id_atendimento);
CREATE INDEX IF NOT EXISTS idx_atendimento_rota_conexao_ordem ON atendimento_rota_conexao(id_atendimento, ordem);

-- ============================================================================
-- SEÇÃO 6.5: CORREÇÃO DA CONSTRAINT DE AÇÃO NO HISTÓRICO (ADICIONAR CHEGADA)
-- ============================================================================

DO $$
BEGIN
    -- Remover constraint antiga se existir
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'historico_ocorrencias' 
        AND constraint_name LIKE '%acao%'
    ) THEN
        -- Buscar nome exato da constraint
        FOR constraint_record IN 
            SELECT conname
            FROM pg_constraint
            WHERE conrelid = 'historico_ocorrencias'::regclass
            AND contype = 'c'
            AND pg_get_constraintdef(oid) LIKE '%acao%'
        LOOP
            EXECUTE 'ALTER TABLE historico_ocorrencias DROP CONSTRAINT IF EXISTS ' || quote_ident(constraint_record.conname) || ' CASCADE';
        END LOOP;
    END IF;
    
    -- Adicionar nova constraint com CHEGADA incluída
    ALTER TABLE historico_ocorrencias 
        ADD CONSTRAINT historico_ocorrencias_acao_check 
        CHECK (acao IN ('ABERTURA', 'DESPACHO', 'CHEGADA', 'ALTERACAO_STATUS', 'CANCELAMENTO', 'CONCLUSAO'));
EXCEPTION WHEN duplicate_object THEN
    NULL;
WHEN OTHERS THEN
    NULL;
END $$;

-- ============================================================================
-- SEÇÃO 10: CRIAR USUÁRIO ADMINISTRADOR
-- ============================================================================

INSERT INTO usuarios (login, senha_hash, perfil, nome, email, ativo)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'Administrador',
    'admin@sistema.local',
    true
)
ON CONFLICT (login) DO NOTHING;

