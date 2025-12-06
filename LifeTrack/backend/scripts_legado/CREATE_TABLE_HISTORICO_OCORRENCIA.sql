-- Script SQL para criar a tabela de histórico de ocorrências
-- Execute este script no DBeaver ou pgAdmin no banco pi_2025_2

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

-- Índices para melhor performance nas consultas
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_id_ocorrencia ON historico_ocorrencias(id_ocorrencia);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_id_usuario ON historico_ocorrencias(id_usuario);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_data_hora ON historico_ocorrencias(data_hora DESC);
CREATE INDEX IF NOT EXISTS idx_historico_ocorrencia_acao ON historico_ocorrencias(acao);

-- Comentários para documentação
COMMENT ON TABLE historico_ocorrencias IS 'Registra o histórico completo de todas as ações realizadas em ocorrências';
COMMENT ON COLUMN historico_ocorrencias.acao IS 'Tipo de ação realizada: ABERTURA, DESPACHO, ALTERACAO_STATUS, CANCELAMENTO, CONCLUSAO';
COMMENT ON COLUMN historico_ocorrencias.descricao_acao IS 'Descrição detalhada da ação realizada';
COMMENT ON COLUMN historico_ocorrencias.tipo_ocorrencia IS 'Tipo de ocorrência no momento da ação (snapshot)';
COMMENT ON COLUMN historico_ocorrencias.gravidade IS 'Gravidade da ocorrência no momento da ação (snapshot)';
COMMENT ON COLUMN historico_ocorrencias.bairro_origem_nome IS 'Nome do bairro de origem no momento da ação (snapshot)';

