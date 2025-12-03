# 📦 Guia de Instalação - LifeTrack

Este guia irá te ajudar a configurar e executar o projeto LifeTrack em um computador novo, passo a passo.

---

## 📋 Pré-requisitos

Antes de começar, verifique se você tem instalado:

### 1. Java 17 ou superior

**Como verificar:**
```powershell
java -version
```

**Se não tiver, instale:**
- Baixe em: https://www.oracle.com/java/technologies/downloads/#java17
- Ou use OpenJDK: https://adoptium.net/
- Após instalar, verifique novamente com `java -version`

### 2. Node.js 18 ou superior

**Como verificar:**
```powershell
node -version
npm -version
```

**Se não tiver, instale:**
- Baixe em: https://nodejs.org/
- Instale a versão LTS (Long Term Support)
- Após instalar, verifique com `node -version` e `npm -version`

### 3. PostgreSQL

**Como verificar:**
- Procure por "PostgreSQL" no menu iniciar
- Ou execute no terminal: `psql --version`

**Se não tiver, instale:**
- Baixe em: https://www.postgresql.org/download/windows/
- Durante a instalação, **anote a senha do usuário postgres** (você vai precisar!)
- Deixe a porta padrão (5432)

### 4. DBeaver (Opcional, mas recomendado)

**Instale:**
- Baixe em: https://dbeaver.io/download/
- Facilita o gerenciamento do banco de dados

---

## 🚀 Instalação Passo a Passo

### Passo 1: Clonar/Baixar o Projeto

Se você tem o projeto em um repositório Git:
```powershell
git clone URL_DO_REPOSITORIO
cd LifeTrack
```

Se você tem o projeto em uma pasta ZIP:
1. Extraia o arquivo ZIP
2. Abra o PowerShell na pasta extraída
3. Navegue até a pasta `LifeTrack`

### Passo 2: Configurar o Banco de Dados

#### 2.1. Criar o Banco de Dados

**Opção A: Usando DBeaver (Recomendado)**

1. Abra o DBeaver
2. Crie uma nova conexão PostgreSQL:
   - Clique em "Nova Conexão" (ícone de plug)
   - Selecione "PostgreSQL"
   - Preencha:
     - **Host:** `localhost`
     - **Porta:** `5432`
     - **Database:** `postgres` (banco padrão)
     - **Usuário:** `postgres`
     - **Senha:** (a senha que você definiu na instalação)
3. Teste a conexão e salve
4. Clique com botão direito na conexão → **SQL Editor** (ou pressione `Alt+\`)
5. Execute este comando:
   ```sql
   CREATE DATABASE pi_2025_2;
   ```
6. Clique com botão direito na conexão → **Refresh** (F5)
7. Expanda "Bancos de dados" e você verá `pi_2025_2`

**Opção B: Usando psql (Linha de Comando)**

1. Abra o PowerShell
2. Navegue até a pasta bin do PostgreSQL (geralmente):
   ```powershell
   cd "C:\Program Files\PostgreSQL\17\bin"
   ```
   (Ajuste o número da versão conforme sua instalação)
3. Execute:
   ```powershell
   .\psql.exe -U postgres
   ```
4. Digite a senha quando solicitado
5. Execute:
   ```sql
   CREATE DATABASE pi_2025_2;
   ```
6. Saia: `\q`

#### 2.2. Executar o Schema SQL

1. No DBeaver, conecte-se ao banco `pi_2025_2`:
   - Clique com botão direito na conexão
   - Selecione "Editar Conexão"
   - Altere o campo "Database" para `pi_2025_2`
   - Salve e conecte

2. Abra um SQL Editor (`Alt+\` ou botão direito → SQL Editor)

3. Abra o arquivo: `LifeTrack\backend\src\main\resources\schema.sql`

4. Execute o script completo:
   - Pressione `Ctrl+Enter` ou clique em "Executar SQL"
   - Aguarde a mensagem de sucesso

5. Verifique se as tabelas foram criadas:
   ```sql
   SELECT table_name FROM information_schema.tables 
   WHERE table_schema = 'public';
   ```
   Você deve ver tabelas como: `usuarios`, `ambulancias`, `profissionais`, etc.

#### 2.3. Criar Usuário Administrador

**Método 1: Gerar Hash e Inserir (Recomendado)**

1. Primeiro, você precisa iniciar o backend (veja Passo 3)
2. Após o backend iniciar, acesse no navegador:
   ```
   http://localhost:8081/api/util/hash?senha=admin123
   ```
3. Você verá um hash longo, algo como:
   ```
   $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
   ```
4. **Copie esse hash completo**
5. No DBeaver, execute:
   ```sql
   INSERT INTO usuarios (login, senha_hash, perfil, nome, email, ativo)
   VALUES (
       'admin',
       'COLE_O_HASH_AQUI',
       'ADMIN',
       'Administrador',
       'admin@sistema.local',
       true
   );
   ```
   (Substitua `COLE_O_HASH_AQUI` pelo hash que você copiou)

**Método 2: Usar Script SQL Direto**

Se você já tem um hash BCrypt válido, pode usar o arquivo:
- `LifeTrack\backend\CRIAR_USUARIO_ADMIN.sql`
- (Mas você ainda precisará gerar o hash primeiro)

### Passo 3: Configurar o Backend

#### 3.1. Configurar Conexão com o Banco

1. Abra o arquivo: `LifeTrack\backend\src\main\resources\application.properties`

2. Edite as seguintes linhas:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/pi_2025_2
   spring.datasource.username=postgres
   spring.datasource.password=SUA_SENHA_POSTGRES_AQUI
   ```
   
   **Importante:**
   - Substitua `SUA_SENHA_POSTGRES_AQUI` pela senha do PostgreSQL que você definiu na instalação
   - O nome do banco deve ser `pi_2025_2` (minúsculas)
   - A porta padrão é `5432`

3. Verifique se estas linhas existem:
   ```properties
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   server.port=8081
   ```

4. Salve o arquivo

#### 3.2. Compilar o Backend

1. Abra o PowerShell
2. Navegue até a pasta do backend:
   ```powershell
   cd LifeTrack\backend
   ```
3. Compile o projeto:
   ```powershell
   .\mvnw.cmd clean install -DskipTests
   ```
   
   **Aguarde a compilação terminar.** Isso pode levar alguns minutos na primeira vez.
   
   **Se der erro:**
   - Verifique se o Java está instalado: `java -version`
   - Verifique se está na pasta correta: `cd LifeTrack\backend`
   - Se o `mvnw.cmd` não existir, você pode precisar baixar o Maven ou usar o JAR já compilado

4. Se tudo der certo, você verá: `BUILD SUCCESS`

#### 3.3. Executar o Backend

**Opção A: Executar JAR (Mais Rápido)**

```powershell
java -jar target\sos-rota-0.0.1-SNAPSHOT.jar
```

**Opção B: Executar com Maven**

```powershell
.\mvnw.cmd spring-boot:run
```

**Verifique se funcionou:**
- Você verá mensagens como: `Tomcat started on port(s): 8081`
- Acesse no navegador: `http://localhost:8081/api/ambulancias`
- Se retornar `[]` ou JSON → ✅ Funcionando!

**Se der erro:**
- Verifique se o PostgreSQL está rodando
- Verifique a senha no `application.properties`
- Verifique se o banco `pi_2025_2` existe

### Passo 4: Configurar o Frontend

#### 4.1. Instalar Dependências

1. Abra um **novo terminal** (deixe o backend rodando)
2. Navegue até a pasta do frontend:
   ```powershell
   cd LifeTrack\frontend
   ```
3. Instale as dependências:
   ```powershell
   npm install
   ```
   
   **Aguarde a instalação terminar.** Isso pode levar alguns minutos na primeira vez.

#### 4.2. Executar o Frontend

```powershell
npm run dev
```

**Verifique se funcionou:**
- Você verá: `Local: http://localhost:5173`
- Acesse no navegador: `http://localhost:5173`
- Se aparecer a tela de login → ✅ Funcionando!

### Passo 5: Fazer Login

1. Acesse: `http://localhost:5173`
2. Você verá a tela de login
3. Use as credenciais:
   - **Login:** `admin`
   - **Senha:** `admin123` (ou a senha que você definiu)
4. Clique em "Entrar"
5. Se tudo estiver correto, você será redirecionado para o painel admin

**Se der erro de login:**
- Verifique se o usuário admin foi criado no banco (Passo 2.3)
- Verifique se o hash da senha está correto
- Verifique os logs do backend para mais detalhes

---

## ✅ Verificação Final

### Checklist

- [ ] Java instalado e funcionando (`java -version`)
- [ ] Node.js instalado e funcionando (`node -version`)
- [ ] PostgreSQL instalado e rodando
- [ ] Banco `pi_2025_2` criado
- [ ] Schema SQL executado (tabelas criadas)
- [ ] Usuário admin criado no banco
- [ ] `application.properties` configurado com senha correta
- [ ] Backend compilado (`BUILD SUCCESS`)
- [ ] Backend rodando (porta 8081)
- [ ] Frontend com dependências instaladas (`npm install`)
- [ ] Frontend rodando (porta 5173)
- [ ] Login funcionando

### Testar Endpoints

Você pode testar se a API está funcionando acessando:

- `http://localhost:8081/api/ambulancias` → Deve retornar `[]` ou JSON
- `http://localhost:8081/api/usuarios` → Deve retornar `[]` ou JSON
- `http://localhost:8081/api/profissionais` → Deve retornar `[]` ou JSON

---

## 🐛 Problemas Comuns

### "Port 8081 already in use"

**Solução:**
1. Encontre o processo usando a porta:
   ```powershell
   netstat -ano | findstr :8081
   ```
2. Encerre o processo ou altere a porta em `application.properties`:
   ```properties
   server.port=8082
   ```
3. Lembre-se de atualizar a URL no frontend também!

### "Cannot connect to database"

**Verificações:**
1. PostgreSQL está rodando?
   - Verifique nos serviços do Windows
   - Ou tente conectar no DBeaver
2. Senha está correta no `application.properties`?
3. Banco `pi_2025_2` existe?
   ```sql
   SELECT datname FROM pg_database WHERE datname = 'pi_2025_2';
   ```
4. Nome do banco está em minúsculas? (`pi_2025_2`, não `PI_2025_2`)

### "Maven não encontrado"

**Solução:**
O projeto usa Maven Wrapper, então não precisa ter Maven instalado!

Se o `mvnw.cmd` não existir:
1. Execute diretamente o JAR (se já estiver compilado):
   ```powershell
   java -jar target\sos-rota-0.0.1-SNAPSHOT.jar
   ```
2. Ou baixe o Maven Wrapper novamente do repositório

### "npm install falha"

**Soluções:**
1. Limpe o cache:
   ```powershell
   npm cache clean --force
   ```
2. Delete a pasta `node_modules` e `package-lock.json`:
   ```powershell
   Remove-Item -Recurse -Force node_modules
   Remove-Item package-lock.json
   ```
3. Tente novamente:
   ```powershell
   npm install
   ```

### "Erro 500 no login"

**Causa:** Usuário não existe ou hash de senha inválido.

**Solução:**
1. Verifique se o usuário existe:
   ```sql
   SELECT * FROM usuarios WHERE login = 'admin';
   ```
2. Se não existir, crie seguindo o Passo 2.3
3. Se existir mas não funcionar, gere um novo hash e atualize:
   ```sql
   UPDATE usuarios 
   SET senha_hash = 'NOVO_HASH_AQUI'
   WHERE login = 'admin';
   ```

### "Frontend não conecta com Backend"

**Verificações:**
1. Backend está rodando? Acesse `http://localhost:8081/api/ambulancias`
2. URL no service está correta? Verifique `frontend/src/services/*.js`
3. CORS está habilitado? (Já está configurado no backend)

---

## 📞 Precisa de Ajuda?

1. **Verifique os logs:**
   - Backend: Veja as mensagens no terminal onde está rodando
   - Frontend: Abra o Console do navegador (F12)

2. **Verifique a documentação:**
   - Leia o `README.md` principal para mais detalhes
   - Veja a seção "Troubleshooting" no README

3. **Verifique a configuração:**
   - Banco de dados está acessível?
   - Portas não estão em conflito?
   - Todas as dependências instaladas?

---

## 🎉 Pronto!

Se você chegou até aqui e tudo está funcionando, parabéns! 🎊

Agora você pode:
- Fazer login no sistema
- Criar usuários, ambulâncias, profissionais e equipes
- Explorar todas as funcionalidades do painel admin

**Boa sorte com o desenvolvimento!** 🚀

