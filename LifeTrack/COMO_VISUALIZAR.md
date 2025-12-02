# 🖥️ Como Visualizar as Telas do Sistema

## Passo a Passo

### 1️⃣ Executar o Backend (Terminal 1)

Abra o PowerShell e execute:

```powershell
cd LifeTrack\backend

# Configurar JAVA_HOME (se necessário)
$env:JAVA_HOME = (Split-Path (Split-Path (Get-Command java).Source))

# Executar o backend
.\mvnw.cmd spring-boot:run
```

**Aguarde até ver:**
```
Started Application in X.XXX seconds
```

O backend estará rodando em: `http://localhost:8080`

---

### 2️⃣ Executar o Frontend (Terminal 2)

Abra um **NOVO** PowerShell e execute:

```powershell
cd LifeTrack\frontend
npm run dev
```

**Aguarde até ver:**
```
  VITE v7.x.x  ready in XXX ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

O frontend estará rodando em: `http://localhost:5173`

---

### 3️⃣ Acessar no Navegador

1. Abra seu navegador (Chrome, Firefox, Edge, etc.)
2. Acesse: **http://localhost:5173**

---

## 🎭 Visualizando as Diferentes Telas

### Tela de Login (Inicial)
- **Quando aparece:** Ao acessar pela primeira vez ou após fazer logout
- **O que você vê:** Tela de login com campos de usuário e senha

### Tela de Admin (AdminDashboard)
**Para ver esta tela:**
1. Faça login com:
   - **Usuário:** `admin`
   - **Senha:** `admin123`
2. Você verá:
   - Header com seu nome e badge "Admin"
   - Tabela de gerenciamento de usuários
   - Botão "+ Novo Usuário"
   - Ações: Editar, Ativar/Desativar, Deletar

**Funcionalidades disponíveis:**
- ✅ Listar todos os usuários
- ✅ Criar novo usuário
- ✅ Editar usuário existente
- ✅ Ativar/Desativar usuário
- ✅ Deletar usuário

### Tela de Usuário Comum (UserDashboard)
**Para ver esta tela:**
1. Faça login com:
   - **Usuário:** `atendente`
   - **Senha:** `atendente123`
2. Você verá:
   - Header com seu nome (sem badge Admin)
   - Mensagem de boas-vindas
   - Placeholder para futuras funcionalidades

---

## 🔄 Testando as Telas

### Teste 1: Login como Admin
1. Acesse `http://localhost:5173`
2. Digite: `admin` / `admin123`
3. Clique em "Entrar"
4. **Resultado:** Você verá a tela de **AdminDashboard**

### Teste 2: Criar um Novo Usuário
1. Na tela de Admin, clique em **"+ Novo Usuário"**
2. Preencha os dados:
   - Usuário: `teste`
   - Senha: `teste123`
   - Nome: `Usuário Teste`
   - Email: `teste@exemplo.com`
   - Marque "Usuário" nas permissões
3. Clique em **"Criar"**
4. **Resultado:** Novo usuário aparece na tabela

### Teste 3: Login como Usuário Comum
1. Clique em **"Sair"** no header
2. Faça login com: `atendente` / `atendente123`
3. **Resultado:** Você verá a tela de **UserDashboard**

### Teste 4: Editar Usuário (Admin)
1. Faça login como admin
2. Na tabela, clique em **"Editar"** em qualquer usuário
3. Altere o nome
4. Clique em **"Atualizar"**
5. **Resultado:** Dados atualizados na tabela

---

## 🐛 Problemas Comuns

### Frontend não abre
- Verifique se o terminal está na pasta `frontend`
- Execute `npm install` se for a primeira vez
- Verifique se a porta 5173 está livre

### Backend não inicia
- Verifique se o Java está instalado: `java -version`
- Configure JAVA_HOME se necessário
- Verifique se a porta 8080 está livre

### Erro de conexão com API
- Certifique-se de que o backend está rodando
- Verifique se o backend está na porta 8080
- Veja o console do navegador (F12) para erros

### Não consigo fazer login
- Verifique se o backend está rodando
- Use as credenciais corretas:
  - Admin: `admin` / `admin123`
  - Atendente: `atendente` / `atendente123`

---

## 📸 Estrutura das Telas

```
┌─────────────────────────────────────┐
│  LifeTrack - Sistema de Atendimento │
│  Olá, [Nome] [Admin] [Sair]        │
├─────────────────────────────────────┤
│                                     │
│  [Conteúdo da Tela]                 │
│  - AdminDashboard (se admin)        │
│  - UserDashboard (se usuário)       │
│                                     │
└─────────────────────────────────────┘
```

---

## ✅ Checklist Rápido

- [ ] Backend rodando em `http://localhost:8080`
- [ ] Frontend rodando em `http://localhost:5173`
- [ ] Navegador aberto na URL correta
- [ ] Login funcionando
- [ ] Telas aparecendo corretamente

---

**Dica:** Mantenha ambos os terminais abertos enquanto desenvolve. O Vite recarrega automaticamente quando você salva alterações no frontend!

