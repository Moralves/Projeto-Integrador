# 🚀 Setup Automático do Banco de Dados

## ✅ Sistema Totalmente Automatizado

O banco de dados é configurado **AUTOMATICAMENTE** quando você inicia o backend. **NÃO é necessário executar scripts SQL manualmente!**

---

## 📋 Como Funciona

### 1. Execução Automática

Quando você executa o backend Spring Boot, o sistema:

1. ✅ Conecta ao banco de dados PostgreSQL
2. ✅ Executa automaticamente o script `src/main/resources/db/migration/setup.sql`
3. ✅ Cria todas as tabelas, relacionamentos, índices e constraints
4. ✅ Cria o usuário administrador padrão
5. ✅ Configura toda a estrutura necessária

### 2. Arquivo do Script

O script principal está em:
```
src/main/resources/db/migration/setup.sql
```

Este script é **completo e atualizado** com toda a estrutura do banco de dados.

### 3. Código Responsável

**Classe**: `com.vitalistech.sosrota.config.InicializadorBancoDados`

Esta classe executa o script automaticamente na inicialização do Spring Boot usando `ResourceDatabasePopulator`.

---

## 🎯 O que o Script Configura

### Tabelas Criadas (11 tabelas):
- ✅ `bairros` - Vértices do grafo de rotas
- ✅ `ruas_conexoes` - Arestas do grafo (conexões entre bairros)
- ✅ `ambulancias` - Cadastro de ambulâncias
- ✅ `profissionais` - Profissionais de saúde (com turno, status, contato obrigatório)
- ✅ `equipes` - Equipes de atendimento
- ✅ `equipes_profissionais` - Relacionamento equipe-profissional
- ✅ `usuarios` - Usuários do sistema (com telefone)
- ✅ `ocorrencias` - Ocorrências de emergência (com campos de SLA)
- ✅ `atendimentos` - Atendimentos realizados (com data_hora_retorno)
- ✅ `historico_ocorrencias` - Auditoria de ações (com placa_ambulancia e acao_ambulancia)
- ✅ `atendimento_rota_conexao` - Rotas calculadas pelo Dijkstra

### Recursos Incluídos:
- ✅ Todas as Foreign Keys (relacionamentos)
- ✅ Todas as Constraints CHECK (valores permitidos)
- ✅ Todos os índices para performance (30+ índices)
- ✅ Usuário administrador padrão (login: `admin`, senha: `admin`)
- ✅ Comentários de documentação

---

## 📝 Logs de Execução

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

## ⚙️ Configuração do Banco

### application.properties

Certifique-se de que o `application.properties` está configurado corretamente:

```properties
# Configuração do Banco de Dados PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/pi_2025_2
spring.datasource.username=postgres
spring.datasource.password=5432

# Configuração JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Configuração de inicialização do banco
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=false
```

### Criar o Banco de Dados

**IMPORTANTE**: Antes de iniciar o backend, crie o banco de dados:

```sql
CREATE DATABASE pi_2025_2;
```

Você pode fazer isso no DBeaver, pgAdmin ou via linha de comando:

```bash
psql -U postgres -c "CREATE DATABASE pi_2025_2;"
```

---

## 🔄 Script Idempotente

O script é **idempotente**, ou seja:
- ✅ Pode ser executado múltiplas vezes sem erro
- ✅ Não duplica estruturas existentes
- ✅ Atualiza apenas o que é necessário
- ✅ Seguro para executar em bancos já existentes

---

## 🛠️ Solução de Problemas

### Problema: Script não executa

**Verifique:**
1. O arquivo `setup.sql` existe em `src/main/resources/db/migration/`
2. O banco de dados `pi_2025_2` foi criado
3. As credenciais no `application.properties` estão corretas
4. O PostgreSQL está rodando

### Problema: Erros ao executar

**Solução:**
- O script continua mesmo com erros (idempotente)
- Verifique os logs do Spring Boot para detalhes
- Se necessário, execute o script manualmente no DBeaver/pgAdmin

### Problema: Tabelas não são criadas

**Solução:**
1. Verifique se o `InicializadorBancoDados` está sendo executado (veja os logs)
2. Verifique se há erros de conexão com o banco
3. Verifique se o Hibernate não está interferindo (`spring.jpa.hibernate.ddl-auto=update`)

---

## 📚 Scripts Removidos (Legados)

Todos os scripts legados foram removidos e unificados no `setup.sql`:

- ❌ `scripts_legado/` - Pasta removida
- ❌ `ADICIONAR_TELEFONE_USUARIO.sql` - Removido
- ❌ `ATUALIZAR_CONTATO_PROFISSIONAL_OBRIGATORIO.sql` - Removido
- ❌ `CORRIGIR_STATUS_URGENTE.sql` - Removido
- ❌ `ATUALIZAR_SENHA_ADMIN.sql` - Removido
- ❌ `schema.sql` - Removido (não era usado automaticamente)

**Tudo está agora unificado no `setup.sql` que executa automaticamente!**

---

## 🎉 Resultado Final

Com este sistema:
- ✅ **Zero configuração manual** - Tudo é automático
- ✅ **Fácil para novos desenvolvedores** - Apenas iniciar o backend
- ✅ **Sem scripts legados confusos** - Tudo unificado
- ✅ **Sempre atualizado** - Um único script fonte da verdade

---

## 📞 Suporte

Se tiver problemas:
1. Verifique os logs do Spring Boot
2. Verifique a conexão com o PostgreSQL
3. Verifique se o banco `pi_2025_2` existe
4. Execute o script manualmente se necessário (último recurso)



