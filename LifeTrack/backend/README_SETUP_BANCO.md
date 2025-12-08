# 🗄️ Setup Automático do Banco de Dados

## ✅ Como Funciona

O banco de dados é configurado **automaticamente** quando você inicia o backend. **Não é necessário executar scripts SQL manualmente!**

---

## 🚀 Execução Automática

### Arquivo do Script
```
src/main/resources/db/migration/setup.sql
```

### Como Funciona

1. Ao iniciar o backend Spring Boot, o script é executado automaticamente
2. Todas as tabelas, constraints e estruturas são criadas/corrigidas
3. O processo é idempotente (pode ser executado múltiplas vezes)

### Logs

Ao iniciar o backend, você verá:

```
========================================
INICIANDO CONFIGURAÇÃO DO BANCO DE DADOS
========================================
Executando script de setup do banco de dados...
✓ Script de setup executado com sucesso!
========================================
Banco de dados configurado e pronto para uso
========================================
```

---

## 📋 O que o Script Configura

- ✅ Tabelas base (bairros, ambulancias, profissionais, equipes, usuarios, ocorrencias, atendimentos)
- ✅ Constraints corretas (status, tipos, etc.)
- ✅ Colunas adicionais (SLA, auditoria, histórico)
- ✅ Índices para performance
- ✅ Correções automáticas de estrutura

---

## 🔧 Código Responsável

**Classe**: `com.vitalistech.sosrota.config.InicializadorBancoDados`

Esta classe executa o script automaticamente na inicialização do Spring Boot usando `ResourceDatabasePopulator`.

---

## ⚙️ Execução Manual (Opcional)

Se precisar executar manualmente (debug):

1. Conecte-se ao banco (DBeaver, pgAdmin, etc.)
2. Abra: `src/main/resources/db/migration/setup.sql`
3. Execute o script

---

## 📝 Manutenção

### Para Modificar

1. Edite: `src/main/resources/db/migration/setup.sql`
2. Reinicie o backend
3. O script será executado automaticamente

### Para Desabilitar

Comente o bean `inicializarBancoDados` em `InicializadorBancoDados.java`.

---

## ⚠️ Importante

- ✅ O script é **seguro** e **idempotente**
- ✅ **Não apaga dados** existentes
- ✅ **Não delete** o arquivo `setup.sql`
- ✅ Você **não precisa fazer nada** - é automático!

---

## 🎯 Resumo

**Apenas inicie o backend e o banco será configurado automaticamente!** 🚀



