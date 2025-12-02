# 🚀 Como Rodar o Sistema LifeTrack

## Pré-requisitos

- Java 11+ instalado
- Node.js 18+ e npm instalados
- MySQL instalado e rodando (ou configure H2 para desenvolvimento)

## 1. Configurar o Banco de Dados

### Opção A: MySQL (Recomendado para produção)

1. Crie o banco de dados:
```sql
CREATE DATABASE lifetrack;
```

2. Configure as credenciais em `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lifetrack
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

### Opção B: H2 (Para desenvolvimento rápido)

Se quiser testar sem MySQL, altere o `application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:lifetrack
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

E adicione no `pom.xml` (se não estiver):
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

## 2. Executar o Backend

```powershell
cd LifeTrack\backend

# Configurar JAVA_HOME (se necessário)
$env:JAVA_HOME = (Split-Path (Split-Path (Get-Command java).Source))

# Executar
.\mvnw.cmd spring-boot:run
```

O backend estará disponível em: `http://localhost:8080`

**Usuários criados automaticamente:**
- **Admin:** `admin` / `admin123`
- **Atendente:** `atendente` / `atendente123`

## 3. Executar o Frontend

Abra um **novo terminal**:

```powershell
cd LifeTrack\frontend
npm install
npm run dev
```

O frontend estará disponível em: `http://localhost:5173`

## 4. Testar o Sistema

1. Acesse `http://localhost:5173`
2. Faça login com:
   - Username: `admin`
   - Senha: `admin123`
3. Você verá a tela de **Gerenciamento de Usuários**
4. Clique em **"+ Novo Usuário"** para criar usuários

## 📋 Funcionalidades Disponíveis

### Tela de Admin
- ✅ Listar todos os usuários
- ✅ Criar novo usuário
- ✅ Editar usuário existente
- ✅ Ativar/Desativar usuário
- ✅ Deletar usuário
- ✅ Definir permissões (USER/ADMIN)

### Endpoints da API

- `POST /api/auth/login` - Autenticação
- `GET /api/usuarios` - Listar usuários (requer ADMIN)
- `POST /api/usuarios` - Criar usuário (requer ADMIN)
- `PUT /api/usuarios/{id}` - Atualizar usuário (requer ADMIN)
- `DELETE /api/usuarios/{id}` - Deletar usuário (requer ADMIN)
- `PATCH /api/usuarios/{id}/toggle-status` - Ativar/Desativar (requer ADMIN)

## 🐛 Troubleshooting

### Erro: "JAVA_HOME not found"
Execute antes de rodar o backend:
```powershell
$env:JAVA_HOME = (Split-Path (Split-Path (Get-Command java).Source))
```

### Erro de conexão com banco
- Verifique se o MySQL está rodando
- Confirme as credenciais em `application.properties`
- Certifique-se de que o banco `lifetrack` existe

### Erro CORS no frontend
O backend já está configurado com `@CrossOrigin(origins = "*")`. Se ainda houver problemas, verifique se o backend está rodando na porta 8080.

### Token inválido
- Faça logout e login novamente
- Verifique se o token não expirou (24h por padrão)

## 📝 Próximos Passos

- [ ] Criar tela de atendimento para usuários não-admin
- [ ] Implementar funcionalidades de atendimento
- [ ] Adicionar validações mais robustas
- [ ] Implementar recuperação de senha
- [ ] Adicionar logs de auditoria

