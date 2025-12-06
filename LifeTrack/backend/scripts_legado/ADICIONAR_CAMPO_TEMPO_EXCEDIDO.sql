-- Script SQL para adicionar campo de tempo excedido do SLA
-- Execute este script no DBeaver ou pgAdmin no banco pi_2025_2

-- Adicionar campo para armazenar quanto tempo excedeu o SLA
ALTER TABLE ocorrencias 
    ADD COLUMN IF NOT EXISTS tempo_excedido_minutos INTEGER;

-- Comentário para documentação
COMMENT ON COLUMN ocorrencias.tempo_excedido_minutos IS 'Tempo em minutos que excedeu o SLA (null se SLA foi cumprido, 0 ou positivo se excedeu)';

