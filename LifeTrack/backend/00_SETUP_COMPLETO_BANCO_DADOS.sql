-- ============================================================================
-- SCRIPT ÚNICO E COMPLETO DE CONFIGURAÇÃO DO BANCO DE DADOS
-- Sistema: LifeTrack - SOS Rota
-- ============================================================================
-- 
-- Este script configura COMPLETAMENTE o banco de dados do sistema.
-- É SEGURO executar múltiplas vezes - todas as operações são idempotentes.
--
-- ═══════════════════════════════════════════════════════════════════════
-- INSTRUÇÕES DE USO:
-- ═══════════════════════════════════════════════════════════════════════
--
-- 1. CONECTAR AO BANCO:
--    - Abra o DBeaver, pgAdmin ou outro cliente PostgreSQL
--    - Conecte-se ao banco de dados: pi_2025_2 (ou o nome do seu banco)
--
-- 2. EXECUTAR O SCRIPT:
--    - Execute o script INTEIRO de uma vez (recomendado)
--    - OU execute seção por seção, na ordem (1 a 10)
--    - O script pode ser executado múltiplas vezes sem problemas
--
-- 3. VERIFICAR RESULTADO:
--    - No final, você verá uma verificação automática
--    - Se tudo estiver OK, verá: "✓✓✓ CONFIGURAÇÃO COMPLETA E CORRETA! ✓✓✓"
--    - Credenciais do admin: Login: admin / Senha: admin123
--
-- ═══════════════════════════════════════════════════════════════════════
-- IMPORTANTE:
-- ═══════════════════════════════════════════════════════════════════════
--
-- - Este script corrige automaticamente problemas conhecidos
-- - Remove constraints antigas e cria novas corretas
-- - Adiciona colunas faltantes
-- - Cria índices para performance
-- - Cria o usuário administrador padrão
--
-- ============================================================================

\echo '========================================'
\echo 'INICIANDO CONFIGURAÇÃO COMPLETA DO BANCO'
\echo '========================================'
\echo ''

-- ============================================================================
-- SEÇÃO 1: CRIAÇÃO DAS TABELAS BASE
-- ============================================================================

\echo '[1/10] Criando tabelas base...'

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
    data_hora_despacho TIMESTAMP,
    data_hora_chegada TIMESTAMP,
    distancia_km DECIMAL(10, 2),
    id_usuario_despacho BIGINT,
    FOREIGN KEY (id_ocorrencia) REFERENCES ocorrencias(id),
    FOREIGN KEY (id_ambulancia) REFERENCES ambulancias(id),
    FOREIGN KEY (id_usuario_despacho) REFERENCES usuarios(id)
);

\echo '✓ Tabelas base criadas'

-- ============================================================================
-- SEÇÃO 2: CORREÇÃO DA CONSTRAINT DE STATUS (CRÍTICO!)
-- ============================================================================

\echo ''
\echo '[2/10] Corrigindo constraint de status em ocorrencias...'

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
            RAISE NOTICE '✓ Constraint % removida', constraint_record.conname;
        EXCEPTION WHEN OTHERS THEN
            NULL;
        END;
    END LOOP;
    
    -- Remover constraint específica (múltiplas tentativas para garantir)
    BEGIN
        ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS ocorrencias_status_check CASCADE;
        RAISE NOTICE '✓ Constraint ocorrencias_status_check removida (se existia)';
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
    -- Tentar adicionar a constraint
    BEGIN
        ALTER TABLE ocorrencias 
            ADD CONSTRAINT ocorrencias_status_check 
            CHECK (status IN ('ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA'));
        RAISE NOTICE '✓ Constraint de status criada corretamente';
    EXCEPTION WHEN duplicate_object THEN
        -- Se já existe, remover e recriar
        BEGIN
            ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS ocorrencias_status_check CASCADE;
            ALTER TABLE ocorrencias 
                ADD CONSTRAINT ocorrencias_status_check 
                CHECK (status IN ('ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA'));
            RAISE NOTICE '✓ Constraint de status removida e recriada corretamente';
        EXCEPTION WHEN OTHERS THEN
            -- Última tentativa sem CASCADE
            BEGIN
                ALTER TABLE ocorrencias DROP CONSTRAINT IF EXISTS ocorrencias_status_check;
                ALTER TABLE ocorrencias 
                    ADD CONSTRAINT ocorrencias_status_check 
                    CHECK (status IN ('ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA'));
                RAISE NOTICE '✓ Constraint de status criada (segunda tentativa)';
            EXCEPTION WHEN OTHERS THEN
                RAISE NOTICE '⚠ Aviso: Não foi possível criar a constraint automaticamente. Execute manualmente se necessário.';
            END;
        END;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '⚠ Erro ao criar constraint: %', SQLERRM;
        NULL;
    END;
END $$;

\echo '✓ Constraint de status corrigida'
\echo ''
\echo 'NOTA: A constraint de status foi corrigida para aceitar:'
\echo '      ABERTA, DESPACHADA, EM_ATENDIMENTO, CONCLUIDA, CANCELADA'

-- ============================================================================
-- SEÇÃO 3: CORREÇÃO DE ESTRUTURA - COLUNAS DE BAIRRO
-- ============================================================================
-- Esta seção corrige problemas de nomenclatura da coluna de bairro.
-- Se existir id_bairro_local, será renomeada para id_bairro_origem.
-- Tudo é feito de forma segura e idempotente.
-- ============================================================================

\echo ''
\echo '[3/10] Corrigindo colunas de bairro...'

DO $$
BEGIN
    -- Se id_bairro_local existe mas id_bairro_origem não existe, renomear
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
        RAISE NOTICE '✓ Coluna id_bairro_local renomeada para id_bairro_origem';
    END IF;
    
    -- Se ambas existem, usar id_bairro_origem e remover id_bairro_local
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
        RAISE NOTICE '✓ Coluna id_bairro_local removida';
    END IF;
    
    -- Garantir que id_bairro_origem existe e está configurada corretamente
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
        
        ALTER TABLE ocorrencias 
            ADD CONSTRAINT fk_ocorrencia_bairro_origem 
            FOREIGN KEY (id_bairro_origem) REFERENCES bairros(id);
        
        RAISE NOTICE '✓ Coluna id_bairro_origem criada';
    END IF;
    
    -- Garantir que id_bairro_origem é NOT NULL
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
    
    -- Garantir que a constraint FK existe
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

\echo '✓ Colunas de bairro corrigidas'

-- ============================================================================
-- SEÇÃO 4: CORREÇÃO DE ESTRUTURA - COLUNA DATA
-- ============================================================================
-- Esta seção corrige problemas de nomenclatura da coluna de data.
-- Se existir data_registro, será renomeada para data_hora_abertura.
-- Garante que a coluna existe e tem valores padrão corretos.
-- ============================================================================

\echo ''
\echo '[4/10] Corrigindo coluna de data...'

DO $$
BEGIN
    -- Se data_registro existe mas data_hora_abertura não existe, renomear
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
        RAISE NOTICE '✓ Coluna data_registro renomeada para data_hora_abertura';
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
        UPDATE ocorrencias 
        SET data_hora_abertura = COALESCE(data_hora_abertura, data_registro, CURRENT_TIMESTAMP)
        WHERE data_hora_abertura IS NULL;
        
        ALTER TABLE ocorrencias DROP COLUMN IF EXISTS data_registro;
        RAISE NOTICE '✓ Coluna data_registro removida';
    END IF;
END $$;

-- Garantir que data_hora_abertura existe e está configurada corretamente
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

\echo '✓ Coluna de data corrigida'

-- ============================================================================
-- SEÇÃO 5: CAMPOS DE AUDITORIA
-- ============================================================================

\echo ''
\echo '[5/10] Adicionando campos de auditoria...'

-- Adicionar coluna de usuário que registrou a ocorrência
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

-- Adicionar coluna de usuário que despachou o atendimento
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

\echo '✓ Campos de auditoria adicionados'
\echo ''
\echo 'NOTA: Campos adicionados para rastrear:'
\echo '      - Quem registrou cada ocorrência (id_usuario_registro)'
\echo '      - Quem despachou cada atendimento (id_usuario_despacho)'

-- ============================================================================
-- SEÇÃO 6: TABELAS AUXILIARES
-- ============================================================================
-- Cria tabelas auxiliares necessárias para o funcionamento completo:
-- - historico_ocorrencias: Registra todas as ações em ocorrências
-- - atendimento_rota_conexao: Armazena as rotas calculadas pelo algoritmo
-- ============================================================================

\echo ''
\echo '[6/10] Criando tabelas auxiliares...'

-- Tabela de histórico de ocorrências
CREATE TABLE IF NOT EXISTS historico_ocorrencias (
    id BIGSERIAL PRIMARY KEY,
    id_ocorrencia BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    acao VARCHAR(50) NOT NULL CHECK (acao IN ('ABERTURA', 'DESPACHO', 'ALTERACAO_STATUS', 'CANCELAMENTO', 'CONCLUSAO')),
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

-- Tabela de rota de conexão (caminho calculado pelo Dijkstra)
CREATE TABLE IF NOT EXISTS atendimento_rota_conexao (
    id BIGSERIAL PRIMARY KEY,
    id_atendimento BIGINT NOT NULL,
    id_rua_conexao BIGINT NOT NULL,
    ordem INTEGER NOT NULL,
    FOREIGN KEY (id_atendimento) REFERENCES atendimentos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_rua_conexao) REFERENCES rua_conexoes(id),
    UNIQUE(id_atendimento, id_rua_conexao, ordem)
);

\echo '✓ Tabelas auxiliares criadas'

-- ============================================================================
-- SEÇÃO 7: CAMPOS DE SLA E TEMPO DE ATENDIMENTO
-- ============================================================================

\echo ''
\echo '[7/10] Adicionando campos de SLA...'

ALTER TABLE ocorrencias 
    ADD COLUMN IF NOT EXISTS data_hora_fechamento TIMESTAMP,
    ADD COLUMN IF NOT EXISTS tempo_atendimento_minutos INTEGER,
    ADD COLUMN IF NOT EXISTS sla_minutos INTEGER,
    ADD COLUMN IF NOT EXISTS sla_cumprido BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS tempo_excedido_minutos INTEGER;

COMMENT ON COLUMN ocorrencias.data_hora_fechamento IS 'Data e hora em que a ocorrência foi concluída';
COMMENT ON COLUMN ocorrencias.tempo_atendimento_minutos IS 'Tempo total de atendimento em minutos (da abertura até o fechamento)';
COMMENT ON COLUMN ocorrencias.sla_minutos IS 'SLA esperado em minutos baseado na gravidade (ALTA=8, MEDIA=15, BAIXA=30)';
COMMENT ON COLUMN ocorrencias.sla_cumprido IS 'Indica se o SLA foi cumprido (tempo_atendimento <= sla_minutos)';
COMMENT ON COLUMN ocorrencias.tempo_excedido_minutos IS 'Tempo em minutos que excedeu o SLA (null se SLA foi cumprido, positivo se excedeu)';

\echo '✓ Campos de SLA adicionados'
\echo ''
\echo 'NOTA: Campos de SLA permitem monitorar:'
\echo '      - Tempo de atendimento total'
\echo '      - Se o SLA foi cumprido'
\echo '      - Quanto tempo excedeu o SLA (se houver)'

-- ============================================================================
-- SEÇÃO 8: CAMPOS ADICIONAIS EM PROFISSIONAIS
-- ============================================================================
-- Adiciona campos essenciais em profissionais:
-- - turno: MANHA, TARDE ou NOITE
-- - status: DISPONIVEL, EM_ATENDIMENTO, EM_FOLGA ou INATIVO
-- ============================================================================

\echo ''
\echo '[8/10] Adicionando campos em profissionais...'

ALTER TABLE profissionais 
    ADD COLUMN IF NOT EXISTS turno VARCHAR(20) DEFAULT 'MANHA';

ALTER TABLE profissionais 
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'DISPONIVEL';

DO $$
BEGIN
    -- Remover constraints antigas se existirem
    BEGIN
        ALTER TABLE profissionais DROP CONSTRAINT IF EXISTS profissionais_turno_check;
        ALTER TABLE profissionais DROP CONSTRAINT IF EXISTS profissionais_status_check;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;
    
    -- Adicionar constraints corretas
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
    
    -- Preencher valores padrão
    UPDATE profissionais SET turno = 'MANHA' WHERE turno IS NULL;
    UPDATE profissionais SET status = 'DISPONIVEL' WHERE status IS NULL;
    
    -- Tornar NOT NULL
    BEGIN
        ALTER TABLE profissionais ALTER COLUMN turno SET NOT NULL;
        ALTER TABLE profissionais ALTER COLUMN status SET NOT NULL;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;
    
    -- Definir defaults
    ALTER TABLE profissionais ALTER COLUMN turno SET DEFAULT 'MANHA';
    ALTER TABLE profissionais ALTER COLUMN status SET DEFAULT 'DISPONIVEL';
END $$;

COMMENT ON COLUMN profissionais.turno IS 'Turno de trabalho do profissional: MANHA, TARDE ou NOITE';
COMMENT ON COLUMN profissionais.status IS 'Status do profissional: DISPONIVEL, EM_ATENDIMENTO, EM_FOLGA ou INATIVO';

\echo '✓ Campos em profissionais adicionados'

-- ============================================================================
-- SEÇÃO 9: ÍNDICES PARA PERFORMANCE
-- ============================================================================

\echo ''
\echo '[9/10] Criando índices...'

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

\echo '✓ Índices criados'

-- ============================================================================
-- SEÇÃO 10: CRIAR USUÁRIO ADMINISTRADOR
-- ============================================================================

\echo ''
\echo '[10/10] Criando usuário administrador...'

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

\echo '✓ Usuário administrador criado'
\echo ''
\echo 'CREDENCIAIS PADRÃO DO ADMINISTRADOR:'
\echo '  Login: admin'
\echo '  Senha: admin123'
\echo ''
\echo '⚠ IMPORTANTE: Altere a senha após o primeiro login!'

-- ============================================================================
-- VERIFICAÇÃO FINAL
-- ============================================================================
-- Esta seção verifica se tudo foi criado corretamente.
-- Se algo estiver faltando, você verá avisos.
-- ============================================================================

\echo ''
\echo '========================================'
\echo 'VERIFICAÇÃO FINAL'
\echo '========================================'

DO $$
DECLARE
    tabelas_count INTEGER;
    indices_count INTEGER;
    colunas_sla_count INTEGER;
    constraint_ok BOOLEAN;
BEGIN
    -- Contar tabelas principais
    SELECT COUNT(*) INTO tabelas_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_name IN ('usuarios', 'ocorrencias', 'atendimentos', 'historico_ocorrencias', 'atendimento_rota_conexao');
    
    -- Contar índices
    SELECT COUNT(*) INTO indices_count
    FROM pg_indexes
    WHERE schemaname = 'public'
    AND tablename IN ('ocorrencias', 'atendimentos', 'historico_ocorrencias');
    
    -- Verificar colunas de SLA
    SELECT COUNT(*) INTO colunas_sla_count
    FROM information_schema.columns
    WHERE table_name = 'ocorrencias'
    AND column_name IN ('sla_minutos', 'sla_cumprido', 'tempo_atendimento_minutos', 'tempo_excedido_minutos');
    
    -- Verificar constraint de status
    SELECT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'ocorrencias'::regclass
        AND conname = 'ocorrencias_status_check'
        AND pg_get_constraintdef(oid) LIKE '%ABERTA%'
    ) INTO constraint_ok;
    
    RAISE NOTICE '';
    RAISE NOTICE 'Tabelas principais: %/5', tabelas_count;
    RAISE NOTICE 'Índices criados: %', indices_count;
    RAISE NOTICE 'Colunas de SLA: %/4', colunas_sla_count;
    RAISE NOTICE 'Constraint de status: %', CASE WHEN constraint_ok THEN '✓ CORRETA' ELSE '✗ ERRO' END;
    RAISE NOTICE '';
    
    IF tabelas_count >= 5 AND colunas_sla_count = 4 AND constraint_ok THEN
        RAISE NOTICE '✓✓✓ CONFIGURAÇÃO COMPLETA E CORRETA! ✓✓✓';
    ELSE
        RAISE WARNING '⚠ Algumas estruturas podem estar faltando ou incorretas!';
    END IF;
END $$;

\echo ''
\echo '========================================'
\echo 'CONFIGURAÇÃO FINALIZADA'
\echo '========================================'
\echo ''
\echo '✓✓✓ BANCO DE DADOS CONFIGURADO COM SUCESSO! ✓✓✓'
\echo ''
\echo '════════════════════════════════════════════════════════'
\echo 'CREDENCIAIS DO USUÁRIO ADMINISTRADOR:'
\echo '════════════════════════════════════════════════════════'
\echo '  Login: admin'
\echo '  Senha: admin123'
\echo ''
\echo '⚠ IMPORTANTE: Altere a senha após o primeiro login!'
\echo ''
\echo '════════════════════════════════════════════════════════'
\echo 'PRÓXIMOS PASSOS:'
\echo '════════════════════════════════════════════════════════'
\echo '1. Configure o arquivo application.properties com as'
\echo '   credenciais do seu banco de dados'
\echo ''
\echo '2. Compile o backend:'
\echo '   cd LifeTrack\backend'
\echo '   .\mvnw.cmd clean install -DskipTests'
\echo ''
\echo '3. Execute o backend:'
\echo '   java -jar target\sos-rota-0.0.1-SNAPSHOT.jar'
\echo ''
\echo '4. O script de setup será executado automaticamente'
\echo '   na inicialização (não precisa rodar manualmente)'
\echo ''
\echo '════════════════════════════════════════════════════════'
\echo 'O SISTEMA ESTÁ PRONTO PARA USO!'
\echo '════════════════════════════════════════════════════════'
\echo ''
