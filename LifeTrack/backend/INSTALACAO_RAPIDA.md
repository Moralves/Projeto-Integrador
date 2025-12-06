# ⚡ Instalação Rápida - LifeTrack SOS Rota

## 🎯 Para Equipe - Setup Rápido

### Passo 1: Criar Banco de Dados
```sql
CREATE DATABASE pi_2025_2;
```

### Passo 2: Executar Schema Base
```sql
-- Execute o arquivo: src/main/resources/schema.sql
-- Isso cria todas as tabelas principais
```

### Passo 3: Executar Script Mestre (TUDO EM UM)
```sql
-- Execute o arquivo: 00_SETUP_COMPLETO_BANCO_DADOS.sql
-- Este script configura TUDO automaticamente:
-- ✅ Correções de estrutura
-- ✅ Campos de auditoria
-- ✅ Tabelas auxiliares
-- ✅ Campos de SLA
-- ✅ Índices de performance
```

### Passo 4: Criar Usuário Admin
```sql
-- Execute o arquivo: CRIAR_USUARIO_ADMIN.sql
-- Login: admin
-- Senha: admin123
```

### Passo 5: Iniciar Aplicação
```bash
# A aplicação Spring Boot está pronta para uso!
```

---

## ✅ Verificação Rápida

Execute para verificar se tudo está OK:

```sql
-- Verificar tabelas principais
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'usuarios', 'ocorrencias', 'atendimentos', 
    'historico_ocorrencias', 'atendimento_rota_conexao'
);

-- Verificar campos de SLA
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'ocorrencias' 
AND column_name IN (
    'sla_minutos', 'sla_cumprido', 
    'tempo_atendimento_minutos', 'tempo_excedido_minutos'
);
```

**Resultado esperado:** 5 tabelas e 4 colunas de SLA.

---

## 🆘 Problemas?

1. **Erro ao executar script mestre?**
   - Verifique se executou o `schema.sql` primeiro
   - Verifique se está conectado ao banco correto

2. **Estrutura não criada?**
   - Execute o script mestre novamente (é seguro!)
   - Verifique os logs do PostgreSQL

3. **Dúvidas?**
   - Consulte `README_SCRIPTS_SQL.md` para documentação completa

---

**Tempo estimado de instalação:** 2-3 minutos

