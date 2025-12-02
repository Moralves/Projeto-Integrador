# 🚀 Como Executar o Backend

## Método Mais Fácil (Recomendado)

Execute o script PowerShell:

```powershell
cd LifeTrack\backend
.\executar.ps1
```

## Método Manual

```powershell
cd LifeTrack\backend

# Compilar o projeto
.\mvnw.cmd clean install

# Executar a aplicação
.\mvnw.cmd spring-boot:run
```

## ⚠️ Importante

1. **Configure o banco de dados MySQL** antes de executar:
   - Crie o banco: `CREATE DATABASE lifetrack;`
   - Ajuste as credenciais em `src/main/resources/application.properties`

2. **Primeira execução**: O Maven Wrapper vai baixar o Maven automaticamente (pode demorar alguns minutos).

3. **A aplicação estará disponível em**: `http://localhost:8080`

## 🔍 Verificar se Funcionou

Após iniciar, você verá mensagens como:
```
Started Application in X.XXX seconds
```

E os usuários iniciais serão criados automaticamente:
- `admin` / `admin123`
- `atendente` / `atendente123`

