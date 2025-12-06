# 📁 Estrutura de Scripts SQL - LifeTrack SOS Rota

## ✅ Scripts Ativos (Use Estes)

### Na Pasta Raiz (`LifeTrack/backend/`)

| Script | Descrição | Quando Usar |
|--------|-----------|-------------|
| **`00_SETUP_COMPLETO_BANCO_DADOS.sql`** | ⭐ **Script Mestre** - Configura tudo | **SEMPRE USE ESTE** |
| `schema.sql` | Cria tabelas base | Primeira instalação |
| `CRIAR_USUARIO_ADMIN.sql` | Cria usuário admin | Após setup completo |
| `VERIFICAR_ESTRUTURA_BANCO.sql` | Verifica estrutura | Para diagnóstico |
| `ATUALIZAR_SENHA_ADMIN.sql` | Atualiza senha admin | Quando necessário |

**Localização do schema.sql:** `src/main/resources/schema.sql`

---

## 📦 Scripts Legado (Não Use Mais)

### Na Pasta `scripts_legado/`

**⚠️ ATENÇÃO:** Estes scripts foram **consolidados no script mestre** e não devem ser executados individualmente.

- `CORRIGIR_COLUNA_DATA_OCORRENCIA.sql` → Integrado (Seção 2)
- `MIGRACAO_AUDITORIA.sql` → Integrado (Seção 3)
- `CREATE_TABLE_HISTORICO_OCORRENCIA.sql` → Integrado (Seção 4)
- `CREATE_TABLE_ATENDIMENTO_ROTA_CONEXAO.sql` → Integrado (Seção 4)
- `ADICIONAR_CAMPOS_SLA_OCORRENCIA.sql` → Integrado (Seção 5)
- `ADICIONAR_CAMPO_TEMPO_EXCEDIDO.sql` → Integrado (Seção 5)

**Consulte `scripts_legado/README_LEGADO.md` para referência histórica.**

---

## 🚀 Fluxo de Instalação Simplificado

```sql
-- 1. Criar banco
CREATE DATABASE pi_2025_2;

-- 2. Executar schema base
-- Execute: src/main/resources/schema.sql

-- 3. Executar script mestre (TUDO EM UM)
-- Execute: 00_SETUP_COMPLETO_BANCO_DADOS.sql

-- 4. Criar usuário admin (opcional)
-- Execute: CRIAR_USUARIO_ADMIN.sql
```

**Pronto!** 🎉

---

## 📚 Documentação

- **`LEIA-ME_PRIMEIRO.md`** - Guia rápido de início
- **`INSTALACAO_RAPIDA.md`** - Passo a passo para equipe
- **`SCRIPTS_SQL.md`** - Resumo dos scripts essenciais
- **`README_SCRIPTS_SQL.md`** - Documentação completa (referência)

---

## ✨ Benefícios da Organização

✅ **Apenas 1 script principal** para configurar tudo  
✅ **Scripts legado organizados** em pasta separada  
✅ **Documentação clara** sobre o que usar  
✅ **Sem confusão** - scripts obsoletos não estão na raiz  
✅ **Fácil manutenção** - tudo centralizado  

---

**Última atualização:** Dezembro 2025

