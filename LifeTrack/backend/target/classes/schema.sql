-- Script SQL para criar o banco de dados PostgreSQL
-- Execute este script no DBeaver ou pgAdmin

-- Criar banco de dados (execute no banco 'postgres' primeiro)
-- CREATE DATABASE PI_2025_2;

-- Conectar ao banco PI_2025_2 e executar o restante:

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

-- Tabela de usuários (para futura autenticação)
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    nome VARCHAR(255),
    email VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabela de histórico de ocorrências
-- Registra todas as ações relacionadas a ocorrências para auditoria
CREATE TABLE IF NOT EXISTS historico_ocorrencias (
    id BIGSERIAL PRIMARY KEY,
    id_ocorrencia BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    acao VARCHAR(50) NOT NULL CHECK (acao IN ('ABERTURA', 'DESPACHO', 'ALTERACAO_STATUS', 'CANCELAMENTO', 'CONCLUSAO')),
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
    FOREIGN KEY (id_ocorrencia) REFERENCES ocorrencias(id) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_ambulancias_bairro ON ambulancias(id_bairro_base);
CREATE INDEX IF NOT EXISTS idx_equipes_ambulancia ON equipes(id_ambulancia);
CREATE INDEX IF NOT EXISTS idx_equipe_profissional_equipe ON equipe_profissional(id_equipe);
CREATE INDEX IF NOT EXISTS idx_equipe_profissional_profissional ON equipe_profissional(id_profissional);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_bairro_origem ON ocorrencias(id_bairro_origem);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_equipe ON ocorrencias(id_equipe_atribuida);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_id_ocorrencia ON historico_ocorrencias(id_ocorrencia);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_id_usuario ON historico_ocorrencias(id_usuario);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_data_hora ON historico_ocorrencias(data_hora DESC);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_acao ON historico_ocorrencias(acao);

-- Dados iniciais de exemplo (opcional)
-- INSERT INTO bairros (nome) VALUES 
--     ('Centro'),
--     ('Jardim das Flores'),
--     ('Vila Nova'),
--     ('Bairro Industrial')
-- ON CONFLICT (nome) DO NOTHING;

