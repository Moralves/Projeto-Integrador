# 🧹 Script de Limpeza de Ocorrências

## 📋 Descrição

Este script SQL (`LIMPAR_OCORRENCIAS.sql`) remove **todas as ocorrências e atendimentos** do banco de dados, mas **mantém todos os outros dados**:

✅ **Mantém:**
- Equipes
- Profissionais
- Ambulâncias
- Bairros
- Conexões de ruas (`ruas_conexoes`)
- Usuários
- Todos os outros dados do sistema

❌ **Remove:**
- Todas as ocorrências
- Todos os atendimentos
- Todo o histórico de ocorrências
- Todas as rotas calculadas (atendimento_rota_conexao)

## ⚠️ ATENÇÃO

**Esta operação é IRREVERSÍVEL!** 

Certifique-se de que realmente deseja limpar todos os dados de ocorrências antes de executar.

## 🚀 Como Usar

### Opção 1: Via DBeaver / pgAdmin

1. Abra o DBeaver ou pgAdmin
2. Conecte-se ao banco de dados `pi_2025_2` (ou seu banco)
3. Abra o arquivo `LIMPAR_OCORRENCIAS.sql`
4. Execute o script completo (Ctrl+Enter ou botão Execute)
5. Verifique os resultados usando as queries de verificação no final do script

### Opção 2: Via Terminal (psql)

```bash
# Conecte-se ao banco
psql -U seu_usuario -d pi_2025_2

# Execute o script
\i LifeTrack/backend/LIMPAR_OCORRENCIAS.sql
```

### Opção 3: Via linha de comando direto

```bash
psql -U seu_usuario -d pi_2025_2 -f LifeTrack/backend/LIMPAR_OCORRENCIAS.sql
```

## 📊 O que o Script Faz

1. **Deleta rotas de atendimento** (`atendimento_rota_conexao`)
2. **Reseta status das ambulâncias** de `EM_ATENDIMENTO` para `DISPONIVEL`
3. **Reseta status dos profissionais** de `EM_ATENDIMENTO` para `DISPONIVEL`
4. **Deleta todos os atendimentos**
5. **Deleta todas as ocorrências** (o histórico é deletado automaticamente por CASCADE)
6. **Limpa histórico órfão** (por segurança)
7. **Reseta sequências** para que novas ocorrências comecem do ID 1

## ✅ Verificação Pós-Limpeza

Após executar o script, execute as queries de verificação no final do arquivo para confirmar:

- ✅ Total de ocorrências: **0**
- ✅ Total de atendimentos: **0**
- ✅ Total de histórico: **0**
- ✅ Total de rotas: **0**
- ✅ Ambulâncias disponíveis: **Todas as ativas**
- ✅ Profissionais disponíveis: **Todos os ativos**
- ✅ Total de equipes: **Mantido** (não deve ser 0)

## 🔄 Após a Limpeza

Após executar o script:

1. **Todas as equipes estarão disponíveis** para novos atendimentos
2. **Todas as ambulâncias estarão disponíveis** (`DISPONIVEL`)
3. **Todos os profissionais estarão disponíveis** (`DISPONIVEL`)
4. **Novas ocorrências começarão do ID 1**

## 📝 Exemplo de Uso

```sql
-- Antes da limpeza
SELECT COUNT(*) FROM ocorrencias;  -- Ex: 50 ocorrências
SELECT COUNT(*) FROM atendimentos;  -- Ex: 45 atendimentos

-- Execute o script LIMPAR_OCORRENCIAS.sql

-- Após a limpeza
SELECT COUNT(*) FROM ocorrencias;  -- Resultado: 0
SELECT COUNT(*) FROM atendimentos; -- Resultado: 0
SELECT COUNT(*) FROM equipes;      -- Resultado: Mantido (ex: 5 equipes)
```

## 🛡️ Segurança

O script usa uma **transação** (`BEGIN`/`COMMIT`) para garantir que:
- Se algo der errado, todas as mudanças são revertidas
- A operação é atômica (tudo ou nada)

## 📍 Localização do Arquivo

```
LifeTrack/backend/LIMPAR_OCORRENCIAS.sql
```

---

**Pronto para testar novos chamados com as equipes existentes!** 🚀


