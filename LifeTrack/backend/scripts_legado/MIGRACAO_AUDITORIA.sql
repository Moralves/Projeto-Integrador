-- Script de migração para adicionar campos de auditoria
-- Execute este script no DBeaver ou pgAdmin no banco pi_2025_2

-- Adicionar coluna de usuário que registrou a ocorrência
ALTER TABLE ocorrencias 
ADD COLUMN IF NOT EXISTS id_usuario_registro BIGINT,
ADD CONSTRAINT fk_ocorrencia_usuario_registro 
    FOREIGN KEY (id_usuario_registro) REFERENCES usuarios(id);

-- Adicionar coluna de usuário que despachou o atendimento
ALTER TABLE atendimentos 
ADD COLUMN IF NOT EXISTS id_usuario_despacho BIGINT,
ADD CONSTRAINT fk_atendimento_usuario_despacho 
    FOREIGN KEY (id_usuario_despacho) REFERENCES usuarios(id);

-- Criar índices para melhor performance nas consultas de relatórios
CREATE INDEX IF NOT EXISTS idx_ocorrencias_usuario_registro ON ocorrencias(id_usuario_registro);
CREATE INDEX IF NOT EXISTS idx_atendimentos_usuario_despacho ON atendimentos(id_usuario_despacho);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_data_abertura ON ocorrencias(data_hora_abertura);
CREATE INDEX IF NOT EXISTS idx_atendimentos_data_despacho ON atendimentos(data_hora_despacho);

