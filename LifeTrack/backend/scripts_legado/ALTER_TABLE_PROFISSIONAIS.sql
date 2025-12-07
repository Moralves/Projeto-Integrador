-- Script para adicionar campos de turno e status na tabela profissionais
-- Execute este script no banco de dados

-- Adicionar coluna turno
ALTER TABLE profissionais 
ADD COLUMN IF NOT EXISTS turno VARCHAR(20) NOT NULL DEFAULT 'MANHA' 
CHECK (turno IN ('MANHA', 'TARDE', 'NOITE'));

-- Adicionar coluna status
ALTER TABLE profissionais 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL' 
CHECK (status IN ('DISPONIVEL', 'EM_ATENDIMENTO', 'EM_FOLGA', 'INATIVO'));

-- Comentários para documentação
COMMENT ON COLUMN profissionais.turno IS 'Turno de trabalho do profissional: MANHA, TARDE ou NOITE';
COMMENT ON COLUMN profissionais.status IS 'Status do profissional: DISPONIVEL, EM_ATENDIMENTO, EM_FOLGA ou INATIVO';



