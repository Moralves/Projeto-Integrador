-- Tabela para armazenar o caminho calculado pelo Dijkstra para cada atendimento
-- Armazena as conexões de rua (ruas_conexoes) utilizadas no caminho da ambulância até a ocorrência

CREATE TABLE IF NOT EXISTS atendimento_rota_conexao (
    id BIGSERIAL PRIMARY KEY,
    id_atendimento BIGINT NOT NULL,
    id_rua_conexao BIGINT NOT NULL,
    ordem INTEGER NOT NULL,
    FOREIGN KEY (id_atendimento) REFERENCES atendimentos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_rua_conexao) REFERENCES ruas_conexoes(id),
    UNIQUE(id_atendimento, id_rua_conexao, ordem)
);

-- Índices para melhor performance nas consultas
CREATE INDEX IF NOT EXISTS idx_atendimento_rota_conexao_atendimento 
    ON atendimento_rota_conexao(id_atendimento);

CREATE INDEX IF NOT EXISTS idx_atendimento_rota_conexao_ordem 
    ON atendimento_rota_conexao(id_atendimento, ordem);




