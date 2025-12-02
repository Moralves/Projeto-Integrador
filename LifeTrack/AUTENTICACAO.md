# Sistema de Autenticação - LifeTrack

Este documento descreve como configurar e usar o sistema de autenticação do LifeTrack.

## 📋 Estrutura Implementada

### Backend (Spring Boot)
- ✅ Modelo `Usuario` com JPA
- ✅ Repository para acesso ao banco
- ✅ Service de autenticação com JWT
- ✅ Controller de autenticação (`/api/auth/login`)
- ✅ Spring Security configurado
- ✅ Filtro JWT para proteger rotas
- ✅ DataLoader para criar usuários iniciais

### Frontend (React)
- ✅ Componente de Login
- ✅ Service de autenticação
- ✅ Gerenciamento de token no localStorage
- ✅ Proteção de rotas

## 🚀 Configuração Inicial

### 1. Configurar Banco de Dados MySQL

1. Crie o banco de dados:
```sql
CREATE DATABASE lifetrack;
```

2. Configure as credenciais no `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lifetrack
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

3. Execute o script SQL (opcional, se preferir criar manualmente):
```bash
mysql -u seu_usuario -p lifetrack < backend/src/main/resources/schema.sql
```

### 2. Usuários Iniciais

O sistema cria automaticamente dois usuários na primeira execução:

- **Admin:**
  - Username: `admin`
  - Senha: `admin123`
  - Role: `ADMIN`

- **Atendente:**
  - Username: `atendente`
  - Senha: `atendente123`
  - Role: `USER`

> **⚠️ IMPORTANTE:** Altere essas senhas em produção!

### 3. Executar o Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

O backend estará disponível em `http://localhost:8080`

### 4. Executar o Frontend

```bash
cd frontend
npm install
npm run dev
```

O frontend estará disponível em `http://localhost:5173`

## 🔐 Como Funciona

### Fluxo de Autenticação

1. Usuário faz login no frontend
2. Frontend envia credenciais para `/api/auth/login`
3. Backend valida credenciais e retorna JWT token
4. Frontend armazena token no localStorage
5. Próximas requisições incluem token no header `Authorization: Bearer <token>`
6. Backend valida token em cada requisição protegida

### Endpoints

#### POST `/api/auth/login`
Autentica um usuário e retorna um token JWT.

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response (sucesso):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "nome": "Administrador",
  "email": "admin@lifetrack.com"
}
```

**Response (erro):**
```json
"Erro no login: Usuário não encontrado"
```

### Rotas Protegidas

Todas as rotas, exceto `/api/auth/**`, requerem autenticação. Para acessar rotas protegidas, inclua o header:

```
Authorization: Bearer <seu_token>
```

## 🛠️ Criar Novos Usuários

### Opção 1: Via DataLoader (desenvolvimento)
Edite `DataLoader.java` e adicione novos usuários.

### Opção 2: Via SQL
1. Gere o hash da senha usando `PasswordGenerator.java`:
```bash
cd backend/src/main/java/com/example/app/util
javac PasswordGenerator.java
java PasswordGenerator
```

2. Insira no banco:
```sql
INSERT INTO usuarios (username, password, nome, email, ativo) 
VALUES ('novo_usuario', '$2a$10$HASH_GERADO', 'Nome Completo', 'email@exemplo.com', TRUE);

INSERT INTO usuario_roles (usuario_id, role) 
VALUES ((SELECT id FROM usuarios WHERE username = 'novo_usuario'), 'USER');
```

### Opção 3: Criar Endpoint de Registro (recomendado)
Implemente um endpoint `/api/auth/register` para criar novos usuários.

## 🔧 Configurações

### JWT
As configurações de JWT estão em `application.properties`:
```properties
jwt.secret=LifeTrackSecretKeyForJWTTokenGeneration2025
jwt.expiration=86400000  # 24 horas em milissegundos
```

> **⚠️ IMPORTANTE:** Altere o `jwt.secret` em produção para um valor seguro e aleatório!

### CORS
O CORS está configurado para aceitar requisições de qualquer origem (`@CrossOrigin(origins = "*")`). Em produção, restrinja para o domínio do frontend.

## 📝 Próximos Passos

- [ ] Implementar endpoint de registro de usuários
- [ ] Implementar refresh token
- [ ] Adicionar recuperação de senha
- [ ] Implementar logout no backend (blacklist de tokens)
- [ ] Adicionar validação de força de senha
- [ ] Implementar rate limiting para login
- [ ] Adicionar logs de auditoria de autenticação

## 🐛 Troubleshooting

### Erro: "Usuário não encontrado"
- Verifique se o usuário existe no banco de dados
- Confirme que o DataLoader foi executado

### Erro: "Senha inválida"
- Verifique se a senha está correta
- Confirme que a senha no banco está em hash BCrypt

### Erro de conexão com banco
- Verifique se o MySQL está rodando
- Confirme credenciais em `application.properties`
- Verifique se o banco `lifetrack` existe

### Token inválido
- Verifique se o token não expirou (24h por padrão)
- Confirme que está enviando no formato: `Bearer <token>`
- Verifique se o `jwt.secret` é o mesmo usado para gerar o token

