-- Script SQL para adicionar campos de SLA e tempo de atendimento na tabela ocorrencias
-- Execute este script no DBeaver ou pgAdmin no banco pi_2025_2

-- Adicionar campos para controle de SLA e tempo de atendimento
ALTER TABLE ocorrencias 
    ADD COLUMN IF NOT EXISTS data_hora_fechamento TIMESTAMP,
    ADD COLUMN IF NOT EXISTS tempo_atendimento_minutos INTEGER,
    ADD COLUMN IF NOT EXISTS sla_minutos INTEGER,
    ADD COLUMN IF NOT EXISTS sla_cumprido BOOLEAN DEFAULT FALSE;

-- Comentários para documentação
COMMENT ON COLUMN ocorrencias.data_hora_fechamento IS 'Data e hora em que a ocorrência foi concluída';
COMMENT ON COLUMN ocorrencias.tempo_atendimento_minutos IS 'Tempo total de atendimento em minutos (da abertura até o fechamento)';
COMMENT ON COLUMN ocorrencias.sla_minutos IS 'SLA esperado em minutos baseado na gravidade (ALTA=8, MEDIA=15, BAIXA=30)';
COMMENT ON COLUMN ocorrencias.sla_cumprido IS 'Indica se o SLA foi cumprido (tempo_atendimento <= sla_minutos)';

-- Criar índice para consultas de relatórios por SLA
CREATE INDEX IF NOT EXISTS idx_ocorrencias_sla_cumprido ON ocorrencias(sla_cumprido);
CREATE INDEX IF NOT EXISTS idx_ocorrencias_data_fechamento ON ocorrencias(data_hora_fechamento);

