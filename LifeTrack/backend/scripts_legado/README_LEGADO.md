# 📦 Scripts SQL Legado

Esta pasta contém scripts SQL que foram **consolidados no script mestre** `00_SETUP_COMPLETO_BANCO_DADOS.sql`.

## ⚠️ Não Use Estes Scripts Individualmente

Todos estes scripts foram integrados no script mestre. Use apenas:
- **`00_SETUP_COMPLETO_BANCO_DADOS.sql`** (na pasta raiz)

## 📋 Scripts Nesta Pasta

Estes scripts são mantidos apenas para **referência histórica**:

- `CORRIGIR_COLUNA_DATA_OCORRENCIA.sql` → Integrado no script mestre (Seção 2)
- `ADICIONAR_CAMPOS_SLA_OCORRENCIA.sql` → Integrado no script mestre (Seção 5)
- `ADICIONAR_CAMPO_TEMPO_EXCEDIDO.sql` → Integrado no script mestre (Seção 5)
- `CREATE_TABLE_HISTORICO_OCORRENCIA.sql` → Integrado no script mestre (Seção 4)
- `CREATE_TABLE_ATENDIMENTO_ROTA_CONEXAO.sql` → Integrado no script mestre (Seção 4)
- `MIGRACAO_AUDITORIA.sql` → Integrado no script mestre (Seção 3)
- `ALTER_TABLE_PROFISSIONAIS.sql` → Integrado no script mestre (Seção 6.5)
- `ALTER_TABLE_USUARIOS.sql` → Integrado no script mestre (Seção 6.5)

## ✅ Use Apenas

1. `00_SETUP_COMPLETO_BANCO_DADOS.sql` - Script mestre (pasta raiz)
2. `schema.sql` - Schema base (src/main/resources/)
3. `CRIAR_USUARIO_ADMIN.sql` - Criar usuário admin (pasta raiz)
4. `VERIFICAR_ESTRUTURA_BANCO.sql` - Verificação (pasta raiz)

---

**Data de consolidação:** Dezembro 2025

