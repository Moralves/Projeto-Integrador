# 🚀 LifeTrack SOS Rota - Guia de Instalação

## ⚡ Instalação Rápida (3 passos)

### 1️⃣ Criar Banco de Dados
```sql
CREATE DATABASE pi_2025_2;
```

### 2️⃣ Executar Schema Base
```sql
-- Execute: src/main/resources/schema.sql
```

### 3️⃣ Executar Script Mestre (TUDO EM UM)
```sql
-- Execute: 00_SETUP_COMPLETO_BANCO_DADOS.sql
-- Este script configura TUDO automaticamente!
```

### 4️⃣ Criar Usuário Admin (Opcional)
```sql
-- Execute: CRIAR_USUARIO_ADMIN.sql
-- Login: admin | Senha: admin123
```

**Pronto!** 🎉 O sistema está configurado.

---

## 📚 Documentação Completa

- **`INSTALACAO_RAPIDA.md`** - Guia rápido para equipe
- **`SCRIPTS_SQL.md`** - Resumo dos scripts essenciais
- **`ESTRUTURA_SCRIPTS.md`** - Estrutura organizada dos scripts
- **`README_SCRIPTS_SQL.md`** - Documentação completa (referência)
- **`VERIFICAR_ESTRUTURA_BANCO.sql`** - Script para verificar se tudo está OK

---

## ✅ Verificação Rápida

Execute para verificar se tudo está correto:

```sql
-- Execute: VERIFICAR_ESTRUTURA_BANCO.sql
```

---

## 🆘 Problemas?

1. **Erro ao executar?**
   - Verifique se executou o `schema.sql` primeiro
   - Execute o script mestre novamente (é seguro!)

2. **Estrutura não criada?**
   - Consulte `README_SCRIPTS_SQL.md` para detalhes

3. **Dúvidas?**
   - Todos os scripts são seguros e podem ser executados múltiplas vezes
   - Use o script de verificação para diagnosticar

---

## 📋 Scripts Disponíveis

### ✅ Scripts Ativos (Use Estes)

| Script | Descrição | Quando Usar |
|--------|-----------|-------------|
| `00_SETUP_COMPLETO_BANCO_DADOS.sql` | ⭐ **Script Mestre** - Configura tudo | **SEMPRE USE ESTE** |
| `schema.sql` | Cria tabelas base | Primeira instalação |
| `CRIAR_USUARIO_ADMIN.sql` | Cria usuário admin | Após setup completo |
| `VERIFICAR_ESTRUTURA_BANCO.sql` | Verifica estrutura | Para diagnóstico |
| `ATUALIZAR_SENHA_ADMIN.sql` | Atualiza senha admin | Quando necessário |
| `RESOLVER_ERRO_BAIRRO.sql` | ⚠️ Correção rápida erro id_bairro_local | Se houver erro de bairro |

### 📦 Scripts Legado (Não Use Mais)

**⚠️ Scripts antigos foram movidos para `scripts_legado/`** - não use mais, estão integrados no script mestre!

Consulte `ESTRUTURA_SCRIPTS.md` para ver a organização completa.

---

**Última atualização:** Dezembro 2025

