-- ============================================================================
-- SCRIPT SQL UNIFICADO DE INSTALAÇÃO COMPLETA DO BANCO DE DADOS
-- Sistema: LifeTrack - SOS Rota
-- Descrição: Este script cria toda a estrutura do banco de dados do zero
-- Data: 2025
-- ============================================================================
-- 
-- INSTRUÇÕES DE USO:
-- 1. Execute este script no PostgreSQL (DBeaver, pgAdmin, psql, etc.)
-- 2. Certifique-se de estar conectado ao banco de dados desejado
-- 3. Se o banco não existir, crie-o primeiro: CREATE DATABASE PI_2025_2;
-- 4. Este script é idempotente (pode ser executado múltiplas vezes sem erro)
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
    contato VARCHAR(100) NOT NULL,
    turno VARCHAR(20) NOT NULL DEFAULT 'MANHA' CHECK (turno IN ('MANHA', 'TARDE', 'NOITE')),
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL' CHECK (status IN ('DISPONIVEL', 'EM_ATENDIMENTO', 'EM_FOLGA', 'INATIVO')),
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
CREATE TABLE IF NOT EXISTS equipes_profissionais (
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
    telefone VARCHAR(20),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabela de ocorrências
CREATE TABLE IF NOT EXISTS ocorrencias (
    id BIGSERIAL PRIMARY KEY,
    descricao TEXT NOT NULL,
    tipo_ocorrencia VARCHAR(255) NOT NULL,
    gravidade VARCHAR(20) NOT NULL CHECK (gravidade IN ('BAIXA', 'MEDIA', 'ALTA', 'CRITICA')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ABERTA', 'DESPACHADA', 'EM_ATENDIMENTO', 'CONCLUIDA', 'CANCELADA')),
    id_bairro_origem BIGINT NOT NULL,
    id_bairro_destino BIGINT,
    id_equipe_atribuida BIGINT,
    data_hora_abertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_hora_fechamento TIMESTAMP,
    tempo_atendimento_minutos INTEGER,
    sla_minutos INTEGER,
    sla_cumprido BOOLEAN DEFAULT FALSE,
    tempo_excedido_minutos INTEGER,
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
    data_hora_retorno TIMESTAMP,
    distancia_km DECIMAL(10, 2),
    id_usuario_despacho BIGINT,
    FOREIGN KEY (id_ocorrencia) REFERENCES ocorrencias(id),
    FOREIGN KEY (id_ambulancia) REFERENCES ambulancias(id),
    FOREIGN KEY (id_equipe) REFERENCES equipes(id),
    FOREIGN KEY (id_usuario_despacho) REFERENCES usuarios(id)
);

-- Tabela de histórico de ocorrências (auditoria)
CREATE TABLE IF NOT EXISTS historico_ocorrencias (
    id BIGSERIAL PRIMARY KEY,
    id_ocorrencia BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    acao VARCHAR(50) NOT NULL CHECK (acao IN ('ABERTURA', 'DESPACHO', 'CHEGADA', 'ALTERACAO_STATUS', 'CANCELAMENTO', 'CONCLUSAO')),
    status_anterior VARCHAR(20),
    status_novo VARCHAR(20) NOT NULL,
    descricao_acao TEXT,
    data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Informações da ocorrência no momento da ação (para histórico completo)
    tipo_ocorrencia VARCHAR(255),
    gravidade VARCHAR(20),
    bairro_origem_nome VARCHAR(255),
    observacoes VARCHAR(1000),
    -- Informações do usuário que realizou a ação
    usuario_nome VARCHAR(255),
    usuario_login VARCHAR(100),
    usuario_perfil VARCHAR(50),
    -- Informações da ambulância no momento da ação
    placa_ambulancia VARCHAR(20),
    acao_ambulancia VARCHAR(100),
    FOREIGN KEY (id_ocorrencia) REFERENCES ocorrencias(id) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

-- Tabela de rota de conexão do atendimento (caminho calculado pelo Dijkstra)
CREATE TABLE IF NOT EXISTS atendimento_rota_conexao (
    id BIGSERIAL PRIMARY KEY,
    id_atendimento BIGINT NOT NULL,
    id_rua_conexao BIGINT NOT NULL,
    ordem INTEGER NOT NULL,
    FOREIGN KEY (id_atendimento) REFERENCES atendimentos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_rua_conexao) REFERENCES ruas_conexoes(id),
    UNIQUE(id_atendimento, id_rua_conexao, ordem)
);

-- ============================================================================
-- SEÇÃO 2: ÍNDICES PARA PERFORMANCE
-- ============================================================================

-- Índices para bairros e conexões
CREATE INDEX IF NOT EXISTS idx_ruas_conexoes_origem ON ruas_conexoes(id_bairro_origem);
CREATE INDEX IF NOT EXISTS idx_ruas_conexoes_destino ON ruas_conexoes(id_bairro_destino);

-- Índices para ambulâncias
CREATE INDEX IF NOT EXISTS idx_ambulancias_bairro ON ambulancias(id_bairro_base);
CREATE INDEX IF NOT EXISTS idx_ambulancias_status ON ambulancias(status);
CREATE INDEX IF NOT EXISTS idx_ambulancias_tipo ON ambulancias(tipo);

-- Índices para profissionais
CREATE INDEX IF NOT EXISTS idx_profissionais_funcao ON profissionais(funcao);
CREATE INDEX IF NOT EXISTS idx_profissionais_status ON profissionais(status);
CREATE INDEX IF NOT EXISTS idx_profissionais_ativo ON profissionais(ativo);

-- Índices para equipes
CREATE INDEX IF NOT EXISTS idx_equipes_ambulancia ON equipes(id_ambulancia);
CREATE INDEX IF NOT EXISTS idx_equipes_ativa ON equipes(ativa);

-- Índices para equipes_profissionais
CREATE INDEX IF NOT EXISTS idx_equipes_profissionais_equipe ON equipes_profissionais(id_equipe);
CREATE INDEX IF NOT EXISTS idx_equipes_profissionais_profissional ON equipes_profissionais(id_profissional);

-- Índices para usuários
CREATE INDEX IF NOT EXISTS idx_usuarios_login ON usuarios(login);
CREATE INDEX IF NOT EXISTS idx_usuarios_perfil ON usuarios(perfil);
CREATE INDEX IF NOT EXISTS idx_usuarios_ativo ON usuarios(ativo);

-- Índices para ocorrências
CREATE INDEX IF NOT EXISTS idx_ocorrencias_bairro_origem ON ocorrencias(id_bairro_origem);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_bairro_destino ON ocorrencias(id_bairro_destino);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_status ON ocorrencias(status);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_gravidade ON ocorrencias(gravidade);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_equipe ON ocorrencias(id_equipe_atribuida);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_usuario_registro ON ocorrencias(id_usuario_registro);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_data_abertura ON ocorrencias(data_hora_abertura);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_data_fechamento ON ocorrencias(data_hora_fechamento);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_sla_cumprido ON ocorrencias(sla_cumprido);

-- Índices para atendimentos
CREATE INDEX IF NOT EXISTS idx_atendimentos_ocorrencia ON atendimentos(id_ocorrencia);
CREATE INDEX IF NOT EXISTS idx_atendimentos_ambulancia ON atendimentos(id_ambulancia);
CREATE INDEX IF NOT EXISTS idx_atendimentos_equipe ON atendimentos(id_equipe);
CREATE INDEX IF NOT EXISTS idx_atendimentos_usuario_despacho ON atendimentos(id_usuario_despacho);
CREATE INDEX IF NOT EXISTS idx_atendimentos_data_despacho ON atendimentos(data_hora_despacho);
CREATE INDEX IF NOT EXISTS idx_atendimentos_data_chegada ON atendimentos(data_hora_chegada);
CREATE INDEX IF NOT EXISTS idx_atendimentos_data_retorno ON atendimentos(data_hora_retorno);

-- Índices para histórico de ocorrências
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_id_ocorrencia ON historico_ocorrencias(id_ocorrencia);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_id_usuario ON historico_ocorrencias(id_usuario);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_data_hora ON historico_ocorrencias(data_hora DESC);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_acao ON historico_ocorrencias(acao);

-- Índices para atendimento_rota_conexao
CREATE INDEX IF NOT EXISTS idx_atendimento_rota_conexao_atendimento ON atendimento_rota_conexao(id_atendimento);
CREATE INDEX IF NOT EXISTS idx_atendimento_rota_conexao_ordem ON atendimento_rota_conexao(id_atendimento, ordem);
CREATE INDEX IF NOT EXISTS idx_atendimento_rota_conexao_rua ON atendimento_rota_conexao(id_rua_conexao);

-- ============================================================================
-- SEÇÃO 3: DADOS INICIAIS (OPCIONAL)
-- ============================================================================

-- Criar usuário administrador padrão
-- Senha padrão: admin (hash bcrypt)
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

-- ============================================================================
-- SEÇÃO 4: COMENTÁRIOS E DOCUMENTAÇÃO
-- ============================================================================

COMMENT ON TABLE bairros IS 'Tabela de bairros (vértices do grafo de rotas)';
COMMENT ON TABLE ruas_conexoes IS 'Tabela de conexões entre bairros (arestas do grafo)';
COMMENT ON TABLE ambulancias IS 'Tabela de ambulâncias cadastradas no sistema';
COMMENT ON TABLE profissionais IS 'Tabela de profissionais de saúde (médicos, enfermeiros, condutores)';
COMMENT ON TABLE equipes IS 'Tabela de equipes de atendimento (associadas a ambulâncias)';
COMMENT ON TABLE equipes_profissionais IS 'Tabela de relacionamento entre equipes e profissionais';
COMMENT ON TABLE usuarios IS 'Tabela de usuários do sistema (autenticação e autorização)';
COMMENT ON TABLE ocorrencias IS 'Tabela de ocorrências de emergência registradas';
COMMENT ON TABLE atendimentos IS 'Tabela de atendimentos realizados pelas ambulâncias';
COMMENT ON TABLE historico_ocorrencias IS 'Tabela de histórico/auditoria de ações em ocorrências';
COMMENT ON TABLE atendimento_rota_conexao IS 'Tabela que armazena o caminho calculado pelo algoritmo Dijkstra para cada atendimento';

COMMENT ON COLUMN usuarios.telefone IS 'Telefone de contato do usuário';
COMMENT ON COLUMN profissionais.contato IS 'Telefone de contato do profissional (obrigatório)';
COMMENT ON COLUMN profissionais.turno IS 'Turno de trabalho do profissional (MANHA, TARDE, NOITE)';
COMMENT ON COLUMN profissionais.status IS 'Status atual do profissional (DISPONIVEL, EM_ATENDIMENTO, EM_FOLGA, INATIVO)';
COMMENT ON COLUMN ocorrencias.sla_minutos IS 'Tempo máximo em minutos para atendimento (SLA)';
COMMENT ON COLUMN ocorrencias.sla_cumprido IS 'Indica se o SLA foi cumprido';
COMMENT ON COLUMN ocorrencias.tempo_atendimento_minutos IS 'Tempo real de atendimento em minutos';
COMMENT ON COLUMN ocorrencias.tempo_excedido_minutos IS 'Tempo excedido além do SLA em minutos';

-- ============================================================================
-- FIM DO SCRIPT DE INSTALAÇÃO
-- ============================================================================
-- 
-- VERIFICAÇÃO:
-- Execute as seguintes queries para verificar se tudo foi criado corretamente:
-- 
-- SELECT table_name FROM information_schema.tables 
-- WHERE table_schema = 'public' 
-- ORDER BY table_name;
-- 
-- SELECT COUNT(*) as total_tabelas FROM information_schema.tables 
-- WHERE table_schema = 'public';
-- 
-- SELECT indexname FROM pg_indexes 
-- WHERE schemaname = 'public' 
-- ORDER BY indexname;
-- ============================================================================

