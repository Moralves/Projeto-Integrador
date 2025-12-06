# 📋 Scripts SQL - LifeTrack SOS Rota

## 🎯 Scripts Essenciais (Use Estes)

### 1. `00_SETUP_COMPLETO_BANCO_DADOS.sql` ⭐ **PRINCIPAL**
**Script mestre que configura tudo automaticamente.**

Execute este script após criar o banco e executar o `schema.sql`.

**O que faz:**
- ✅ Corrige estrutura de colunas
- ✅ Adiciona campos de auditoria
- ✅ Cria tabelas auxiliares (histórico, rota)
- ✅ Adiciona campos de SLA
- ✅ Cria índices de performance
- ✅ Verifica estrutura final

**É seguro executar múltiplas vezes!**

---

### 2. `schema.sql`
**Localização:** `src/main/resources/schema.sql`

Cria todas as tabelas base do sistema.

**Execute primeiro** antes do script mestre.

---

### 3. `CRIAR_USUARIO_ADMIN.sql`
Cria o usuário administrador padrão.

**Credenciais:**
- Login: `admin`
- Senha: `admin123`

Execute após o script mestre.

---

### 4. `VERIFICAR_ESTRUTURA_BANCO.sql`
Script de diagnóstico e verificação.

Use para verificar se tudo está configurado corretamente.

---

### 5. `ATUALIZAR_SENHA_ADMIN.sql`
Atualiza a senha do usuário administrador.

Use quando precisar alterar a senha do admin.

---

### 6. `RESOLVER_ERRO_BAIRRO.sql` ⚠️
**Script de correção rápida para erro de coluna `id_bairro_local`.**

Use se estiver recebendo erro:
```
Erro: o valor nulo na coluna "id_bairro_local" da relação "ocorrencias"
```

Este script corrige a inconsistência entre `id_bairro_local` e `id_bairro_origem`.

**Nota:** O script mestre (`00_SETUP_COMPLETO_BANCO_DADOS.sql`) já inclui esta correção. Use este script apenas para correção rápida.

---

## 📁 Scripts Legado

Scripts antigos foram movidos para `scripts_legado/` pois foram **consolidados no script mestre**.

**Não use scripts da pasta `scripts_legado/`** - eles já estão integrados no script mestre.

---

## 🚀 Fluxo de Instalação

```sql
-- 1. Criar banco
CREATE DATABASE pi_2025_2;

-- 2. Executar schema base
-- Execute: src/main/resources/schema.sql

-- 3. Executar script mestre (TUDO EM UM)
-- Execute: 00_SETUP_COMPLETO_BANCO_DADOS.sql

-- 4. Criar usuário admin (opcional)
-- Execute: CRIAR_USUARIO_ADMIN.sql

-- 5. Verificar (opcional)
-- Execute: VERIFICAR_ESTRUTURA_BANCO.sql
```

---

## 📚 Documentação

- **`LEIA-ME_PRIMEIRO.md`** - Guia rápido
- **`INSTALACAO_RAPIDA.md`** - Passo a passo para equipe
- **`README_SCRIPTS_SQL.md`** - Documentação completa (referência)

---

## ⚠️ Importante

- **Use apenas o script mestre** para configuração completa
- Scripts individuais foram consolidados
- Todos os scripts são seguros e idempotentes
- Sempre faça backup antes de executar em produção

---

**Última atualização:** Dezembro 2025

