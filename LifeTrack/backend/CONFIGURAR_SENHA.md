# 🔐 Como Configurar a Senha do PostgreSQL

## Problema
O erro mostra: `FATAL: autenticação do tipo senha falhou para o usuário "postgres"`

Isso significa que a senha no `application.properties` está incorreta.

## Soluções

### Opção 1: Descobrir a Senha Atual

Se você já tem uma senha configurada, tente:

1. **No DBeaver:**
   - Abra o DBeaver
   - Tente conectar ao PostgreSQL
   - Se conseguir, a senha que você usa lá é a correta

2. **No pgAdmin:**
   - Abra o pgAdmin
   - Verifique as credenciais salvas

### Opção 2: Redefinir a Senha do PostgreSQL

Se você não lembra a senha, pode redefinir:

#### No Windows (via psql):

1. Abra o PowerShell como **Administrador**

2. Navegue até a pasta bin do PostgreSQL (geralmente):
   ```powershell
   cd "C:\Program Files\PostgreSQL\17\bin"
   ```
   (Ajuste o número da versão conforme sua instalação)

3. Conecte-se como superusuário:
   ```powershell
   .\psql.exe -U postgres
   ```
   (Pode pedir senha - tente deixar vazio ou pressionar Enter)

4. Se conseguir conectar, altere a senha:
   ```sql
   ALTER USER postgres WITH PASSWORD 'sua_nova_senha_aqui';
   ```

5. Saia:
   ```sql
   \q
   ```

#### Alternativa: Editar pg_hba.conf

1. Localize o arquivo `pg_hba.conf` (geralmente em):
   ```
   C:\Program Files\PostgreSQL\17\data\pg_hba.conf
   ```

2. Abra como Administrador e altere a linha:
   ```
   # De:
   host    all             all             127.0.0.1/32            md5
   
   # Para (temporariamente):
   host    all             all             127.0.0.1/32            trust
   ```

3. Reinicie o serviço PostgreSQL:
   ```powershell
   Restart-Service postgresql-x64-17
   ```

4. Conecte sem senha e altere:
   ```powershell
   cd "C:\Program Files\PostgreSQL\17\bin"
   .\psql.exe -U postgres
   ```
   ```sql
   ALTER USER postgres WITH PASSWORD 'sua_nova_senha_aqui';
   \q
   ```

5. **IMPORTANTE:** Volte o `pg_hba.conf` para `md5` e reinicie o serviço novamente.

### Opção 3: Usar Senha Padrão Comum

Se você instalou recentemente, tente estas senhas comuns:
- `postgres`
- `admin`
- `123456`
- (deixar vazio)

## Depois de Descobrir/Definir a Senha

1. Edite `src/main/resources/application.properties`:
   ```properties
   spring.datasource.password=SUA_SENHA_AQUI
   ```

2. Recompile o projeto:
   ```powershell
   .\mvnw.cmd clean install -DskipTests
   ```

3. Execute novamente:
   ```powershell
   java -jar target\sos-rota-0.0.1-SNAPSHOT.jar
   ```

## Verificar se Funcionou

Se o backend iniciar sem erros e mostrar:
```
Tomcat started on port(s): 8080 (http)
```

✅ **Sucesso!** O backend está rodando!

