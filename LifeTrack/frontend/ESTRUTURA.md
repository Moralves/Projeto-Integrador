# 📁 Estrutura de Pastas do Frontend

## Organização do Projeto

O frontend está organizado de forma clara para separar as funcionalidades de **Admin** e **Usuário comum**:

```
src/
├── pages/              # Páginas completas da aplicação
│   ├── admin/          # Páginas exclusivas para administradores
│   │   ├── AdminDashboard.jsx
│   │   └── AdminDashboard.css
│   └── user/           # Páginas para usuários comuns
│       ├── UserDashboard.jsx
│       └── UserDashboard.css
│
├── components/         # Componentes reutilizáveis
│   └── auth/           # Componentes de autenticação
│       ├── Login.jsx
│       └── Login.css
│
├── services/           # Serviços de API
│   ├── authService.js
│   └── usuarioService.js
│
├── App.jsx            # Componente principal (roteamento)
├── App.css
├── main.jsx           # Ponto de entrada
└── index.css          # Estilos globais
```

## 📋 Descrição das Pastas

### `pages/admin/`
**Páginas exclusivas para administradores:**
- `AdminDashboard.jsx` - Tela principal de gerenciamento de usuários
- Funcionalidades: Criar, editar, deletar, ativar/desativar usuários

### `pages/user/`
**Páginas para usuários comuns:**
- `UserDashboard.jsx` - Tela principal do usuário comum
- Aqui serão implementadas as funcionalidades de atendimento

### `components/auth/`
**Componentes de autenticação (compartilhados):**
- `Login.jsx` - Componente de login usado por todos

### `services/`
**Serviços de comunicação com a API:**
- `authService.js` - Autenticação e gerenciamento de tokens
- `usuarioService.js` - CRUD de usuários (requer permissão ADMIN)

## 🔄 Fluxo de Navegação

1. **Usuário não autenticado:**
   - Vê `components/auth/Login.jsx`

2. **Usuário autenticado como ADMIN:**
   - Vê `pages/admin/AdminDashboard.jsx`
   - Tem acesso ao gerenciamento de usuários

3. **Usuário autenticado como USER:**
   - Vê `pages/user/UserDashboard.jsx`
   - Tem acesso às funcionalidades de atendimento

## ➕ Adicionando Novas Funcionalidades

### Para Admin:
1. Crie novos componentes em `pages/admin/`
2. Exemplo: `pages/admin/Relatorios.jsx`

### Para Usuário:
1. Crie novos componentes em `pages/user/`
2. Exemplo: `pages/user/Atendimentos.jsx`

### Componentes Compartilhados:
1. Crie em `components/shared/` (se necessário)
2. Exemplo: `components/shared/Modal.jsx`

## 📝 Convenções

- **Páginas**: Componentes completos que representam uma tela inteira
- **Componentes**: Pequenos componentes reutilizáveis
- **Services**: Lógica de comunicação com API
- **CSS**: Um arquivo CSS por componente, mesmo nome do JSX

