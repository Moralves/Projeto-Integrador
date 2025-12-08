# 🔧 Solução para Limpar Ocorrências

## 📋 Problema

Ao tentar executar o script `LIMPAR_OCORRENCIAS.sql`, pode ocorrer erro devido a constraints de foreign keys (chaves estrangeiras) que impedem a deleção.

## ✅ Soluções Disponíveis

### **Opção 1: Script Padrão (LIMPAR_OCORRENCIAS.sql)**

Este script tenta deletar na ordem correta, respeitando as foreign keys:

1. Reseta status de ambulâncias e profissionais
2. Deleta rotas (`atendimento_rota_conexao`)
3. Deleta histórico (`historico_ocorrencias`)
4. Deleta atendimentos (`atendimentos`)
5. Deleta ocorrências (`ocorrencias`)

**Como usar:**
```sql
-- Execute no pgAdmin, DBeaver ou psql
\i LifeTrack/backend/LIMPAR_OCORRENCIAS.sql
```

**Se der erro**, use a Opção 2.

---

### **Opção 2: Script Alternativo (LIMPAR_OCORRENCIAS_ALTERNATIVO.sql) - RECOMENDADO**

Este script usa `TRUNCATE CASCADE` que é mais robusto e resolve automaticamente as dependências:

```sql
-- Execute no pgAdmin, DBeaver ou psql
\i LifeTrack/backend/LIMPAR_OCORRENCIAS_ALTERNATIVO.sql
```

**Vantagens:**
- ✅ Mais rápido
- ✅ Resolve automaticamente as dependências (CASCADE)
- ✅ Menos propenso a erros
- ✅ Reseta sequências automaticamente

---

### **Opção 3: Limpeza Manual (se ambos falharem)**

Se ambos os scripts falharem, execute estas queries na ordem:

```sql
BEGIN;

-- 1. Resetar status
UPDATE ambulancias SET status = 'DISPONIVEL' WHERE status = 'EM_ATENDIMENTO';
UPDATE profissionais SET status = 'DISPONIVEL' WHERE status = 'EM_ATENDIMENTO';

-- 2. Limpar na ordem correta
TRUNCATE TABLE atendimento_rota_conexao CASCADE;
TRUNCATE TABLE historico_ocorrencias CASCADE;
TRUNCATE TABLE atendimentos CASCADE;
TRUNCATE TABLE ocorrencias CASCADE;

-- 3. Resetar sequências
ALTER SEQUENCE IF EXISTS ocorrencias_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS atendimentos_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS historico_ocorrencias_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS atendimento_rota_conexao_id_seq RESTART WITH 1;

COMMIT;
```

---

## 🔍 Verificação

Após executar qualquer script, verifique se funcionou:

```sql
-- Todas devem retornar 0
SELECT COUNT(*) as ocorrencias FROM ocorrencias;
SELECT COUNT(*) as atendimentos FROM atendimentos;
SELECT COUNT(*) as historico FROM historico_ocorrencias;
SELECT COUNT(*) as rotas FROM atendimento_rota_conexao;

-- Estas devem mostrar todas as ambulâncias e profissionais como DISPONIVEL
SELECT COUNT(*) as amb_disponiveis FROM ambulancias WHERE status = 'DISPONIVEL';
SELECT COUNT(*) as prof_disponiveis FROM profissionais WHERE status = 'DISPONIVEL';
```

---

## 🚨 Mensagens de Erro Comuns

### **Erro: "violates foreign key constraint"**

**Solução:** Use o script alternativo (`LIMPAR_OCORRENCIAS_ALTERNATIVO.sql`) que usa `TRUNCATE CASCADE`.

### **Erro: "cannot truncate a table referenced in a foreign key constraint"**

**Solução:** Execute na ordem correta ou use a Opção 3 (limpeza manual).

### **Erro: "permission denied"**

**Solução:** Certifique-se de estar conectado com um usuário que tenha permissões de DELETE/TRUNCATE nas tabelas.

---

## 📝 O que é Mantido

Após a limpeza, **TODOS** estes dados são mantidos:
- ✅ Equipes
- ✅ Profissionais
- ✅ Ambulâncias
- ✅ Bairros
- ✅ Conexões de ruas
- ✅ Usuários
- ✅ Todas as outras configurações

**Apenas são removidos:**
- ❌ Ocorrências
- ❌ Atendimentos
- ❌ Histórico de ocorrências
- ❌ Rotas calculadas

---

## 🎯 Recomendação

**Use sempre o `LIMPAR_OCORRENCIAS_ALTERNATIVO.sql`** - é mais rápido e confiável!


