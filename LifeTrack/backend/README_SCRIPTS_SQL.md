# 📋 Guia de Scripts SQL - LifeTrack SOS Rota

Este documento descreve todos os scripts SQL do projeto e como utilizá-los de forma segura e profissional.

## 🎯 Script Principal (RECOMENDADO)

### `00_SETUP_COMPLETO_BANCO_DADOS.sql`
**⭐ USE ESTE SCRIPT PARA CONFIGURAÇÃO COMPLETA**

Este é o script mestre que executa todas as configurações necessárias na ordem correta. É **seguro executar múltiplas vezes** - todas as operações são idempotentes.

**Como usar:**
1. Conecte-se ao banco de dados PostgreSQL (banco `pi_2025_2`)
2. Execute o script completo
3. O script verificará e criará/atualizará tudo automaticamente

**O que este script faz:**
- ✅ Verifica estrutura base do banco
- ✅ Corrige coluna de data (data_registro → data_hora_abertura)
- ✅ Adiciona campos de auditoria (usuários em ocorrências/atendimentos)
- ✅ Cria tabela de histórico de ocorrências
- ✅ Cria tabela de rota de conexão (Dijkstra)
- ✅ Adiciona campos de SLA e tempo de atendimento
- ✅ Cria índices para performance
- ✅ Verifica estrutura final

---

## 📁 Scripts Essenciais

### Scripts de Estrutura Base

#### `schema.sql`
**Localização:** `src/main/resources/schema.sql`

Script principal que cria todas as tabelas base do sistema:
- bairros, rua_conexoes
- ambulancias, profissionais, equipes
- ocorrencias, atendimentos
- usuarios

**Quando usar:** Primeira instalação do sistema (criação inicial do banco)

---

### Scripts de Usuários

#### `CRIAR_USUARIO_ADMIN.sql`
Cria o usuário administrador padrão do sistema.

**Usuário criado:**
- Login: `admin`
- Senha: `admin123` (hash BCrypt)

**Quando usar:** Após executar o script mestre, para ter acesso ao sistema.

---

#### `ATUALIZAR_SENHA_ADMIN.sql`
Atualiza a senha do usuário administrador.

**Quando usar:** Quando precisar alterar a senha do admin.

---

## 📦 Scripts Legado (Não Use Mais)

**⚠️ ATENÇÃO:** Os scripts abaixo foram **movidos para `scripts_legado/`** pois foram **consolidados no script mestre**.

**Não execute estes scripts individualmente!** Eles já estão integrados no `00_SETUP_COMPLETO_BANCO_DADOS.sql`.

- ~~`CORRIGIR_COLUNA_DATA_OCORRENCIA.sql`~~ → Integrado (Seção 2)
- ~~`MIGRACAO_AUDITORIA.sql`~~ → Integrado (Seção 3)
- ~~`CREATE_TABLE_HISTORICO_OCORRENCIA.sql`~~ → Integrado (Seção 4)
- ~~`CREATE_TABLE_ATENDIMENTO_ROTA_CONEXAO.sql`~~ → Integrado (Seção 4)
- ~~`ADICIONAR_CAMPOS_SLA_OCORRENCIA.sql`~~ → Integrado (Seção 5)
- ~~`ADICIONAR_CAMPO_TEMPO_EXCEDIDO.sql`~~ → Integrado (Seção 5)

**Consulte `scripts_legado/README_LEGADO.md` para referência histórica.**

---

## 🚀 Fluxo de Instalação Recomendado

### Para Nova Instalação

1. **Criar banco de dados:**
   ```sql
   CREATE DATABASE pi_2025_2;
   ```

2. **Executar schema base:**
   ```sql
   -- Execute: src/main/resources/schema.sql
   ```

3. **Executar script mestre:**
   ```sql
   -- Execute: 00_SETUP_COMPLETO_BANCO_DADOS.sql
   ```

4. **Criar usuário admin:**
   ```sql
   -- Execute: CRIAR_USUARIO_ADMIN.sql
   ```

5. **Iniciar aplicação:**
   ```bash
   # A aplicação Spring Boot carregará os dados iniciais automaticamente
   ```

### Para Atualização de Banco Existente

1. **Executar script mestre:**
   ```sql
   -- Execute: 00_SETUP_COMPLETO_BANCO_DADOS.sql
   -- Este script é seguro e não quebrará dados existentes
   ```

2. **Verificar se tudo está OK:**
   - O script exibirá um relatório final
   - Verifique se todas as estruturas foram criadas

---

## ⚠️ Importante

### Segurança dos Scripts

- ✅ Todos os scripts usam `IF NOT EXISTS` e `IF EXISTS` para evitar erros
- ✅ Scripts são idempotentes (podem ser executados múltiplas vezes)
- ✅ Não há `DROP TABLE` ou operações destrutivas
- ✅ Constraints são adicionadas apenas se não existirem

### Backup Recomendado

Antes de executar scripts em produção:
```sql
-- Fazer backup do banco
pg_dump -U postgres -d pi_2025_2 > backup_antes_migracao.sql
```

### Ordem de Execução

**NÃO é necessário executar scripts individuais se você usar o script mestre!**

O script mestre (`00_SETUP_COMPLETO_BANCO_DADOS.sql`) já inclui tudo na ordem correta.

---

## 📊 Estrutura Final Esperada

Após executar o script mestre, você terá:

### Tabelas Principais
- ✅ usuarios
- ✅ bairros
- ✅ rua_conexoes
- ✅ ambulancias
- ✅ profissionais
- ✅ equipes
- ✅ ocorrencias (com campos de SLA)
- ✅ atendimentos
- ✅ historico_ocorrencias
- ✅ atendimento_rota_conexao

### Campos de SLA em `ocorrencias`
- ✅ data_hora_abertura
- ✅ data_hora_fechamento
- ✅ tempo_atendimento_minutos
- ✅ sla_minutos
- ✅ sla_cumprido
- ✅ tempo_excedido_minutos

### Índices Criados
- ✅ Índices de performance em todas as tabelas principais
- ✅ Índices para consultas de relatórios
- ✅ Índices para histórico e auditoria

---

## 🆘 Troubleshooting

### Erro: "Tabela não encontrada"
**Solução:** Execute primeiro o `schema.sql` para criar as tabelas base.

### Erro: "Constraint já existe"
**Solução:** Normal, o script verifica antes de criar. Pode ignorar.

### Erro: "Coluna já existe"
**Solução:** Normal, o script usa `IF NOT EXISTS`. Pode ignorar.

### Verificar se tudo está OK
```sql
-- Verificar estrutura da tabela ocorrencias
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'ocorrencias'
ORDER BY ordinal_position;

-- Verificar se histórico existe
SELECT COUNT(*) FROM historico_ocorrencias;
```

---

## 📝 Notas para Desenvolvedores

- Todos os scripts seguem padrões PostgreSQL
- Comentários explicam o propósito de cada seção
- Scripts são versionados junto com o código
- Mudanças estruturais devem ser documentadas aqui

---

## 🔄 Versionamento

- **v1.0** - Estrutura base inicial
- **v1.1** - Adição de histórico de ocorrências
- **v1.2** - Adição de campos de SLA e tempo de atendimento
- **v1.3** - Script mestre unificado

---

**Última atualização:** Dezembro 2025

