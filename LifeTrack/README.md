# 🚑 LifeTrack - Sistema de Gestão de Emergências

Sistema full-stack para gestão e despacho de atendimentos de emergência, desenvolvido com **React (Vite)** e **Spring Boot**.

---

## 📑 Índice

- [Início Rápido](#-início-rápido)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração](#-configuração)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Arquitetura](#-arquitetura)
- [Guia de Navegação no Código](#-guia-de-navegação-no-código)
- [Fluxos Principais](#-fluxos-principais)
- [API Endpoints](#-api-endpoints)
- [Desenvolvimento](#-desenvolvimento)
- [Como Adicionar Funcionalidades](#-como-adicionar-funcionalidades)
- [Troubleshooting](#-troubleshooting)

---

## ⚡ Início Rápido

### 1. Configurar Banco de Dados

```sql
-- Criar banco
CREATE DATABASE pi_2025_2;

-- Executar schema
-- Abra: backend/src/main/resources/schema.sql no DBeaver e execute

-- Criar usuário admin (veja seção "Criar Usuário Admin" abaixo)
```

### 2. Configurar Backend

Edite `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pi_2025_2
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA_AQUI
```

### 3. Executar Aplicação

**Backend:**
```powershell
cd LifeTrack\backend
java -jar target\sos-rota-0.0.1-SNAPSHOT.jar
```

**Frontend (outro terminal):**
```powershell
cd LifeTrack\frontend
npm run dev
```

### 4. Acessar

- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8081/api

---

## 📋 Pré-requisitos

- **Node.js 18+** e npm
- **Java 17+** (LTS)
- **PostgreSQL** rodando localmente
- **DBeaver** (opcional, para gerenciar o banco)

**Nota:** O projeto usa Maven Wrapper, então não é necessário ter Maven instalado.

---

## 🗄️ Configuração

### Configuração do Banco de Dados

1. **Criar o Banco:**
   ```sql
   CREATE DATABASE pi_2025_2;
   ```

2. **Executar Schema:**
   - Abra `backend/src/main/resources/schema.sql` no DBeaver
   - Execute o script completo (`Ctrl+Enter`)

3. **Adicionar Campos na Tabela de Usuários** (se necessário):
   ```sql
   ALTER TABLE usuarios 
   ADD COLUMN IF NOT EXISTS nome VARCHAR(255),
   ADD COLUMN IF NOT EXISTS email VARCHAR(255),
   ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;
   ```

### Criar Usuário Admin

**Método 1: Gerar Hash e Inserir Manualmente (Recomendado)**

1. Inicie o backend
2. Acesse: `http://localhost:8081/api/util/hash?senha=admin123`
3. Copie o hash retornado
4. Execute no DBeaver:
   ```sql
   INSERT INTO usuarios (login, senha_hash, perfil, nome, email, ativo)
   VALUES (
       'admin',
       'HASH_COPIADO_AQUI',
       'ADMIN',
       'Administrador',
       'admin@sistema.local',
       true
   );
   ```

**Método 2: Usar Script SQL**

Veja o arquivo `backend/CRIAR_USUARIO_ADMIN.sql` (você precisará gerar o hash primeiro).

**Credenciais Padrão:**
- Login: `admin`
- Senha: `admin123` (ou a senha que você definiu)

---

## 📁 Estrutura do Projeto

```
LifeTrack/
│
├── backend/                          # Backend Spring Boot
│   ├── src/main/
│   │   ├── java/com/vitalistech/sosrota/
│   │   │   ├── SosRotaApplication.java      # Classe principal
│   │   │   │
│   │   │   ├── config/                      # Configurações
│   │   │   │   ├── ConfiguracaoSeguranca.java
│   │   │   │   ├── ConfiguracaoSenha.java
│   │   │   │   └── CarregamentoDadosIniciaisModel.java
│   │   │   │
│   │   │   ├── dominio/                     # Camada de Domínio
│   │   │   │   ├── modelo/                  # Entidades JPA
│   │   │   │   │   ├── Usuario.java
│   │   │   │   │   ├── Ambulancia.java
│   │   │   │   │   ├── Profissional.java
│   │   │   │   │   ├── Equipe.java
│   │   │   │   │   ├── Ocorrencia.java
│   │   │   │   │   └── ...
│   │   │   │   │
│   │   │   │   ├── repositorio/             # Repositórios JPA
│   │   │   │   │   ├── UsuarioRepositorio.java
│   │   │   │   │   ├── AmbulanciaRepositorio.java
│   │   │   │   │   └── ...
│   │   │   │   │
│   │   │   │   └── servico/                 # Lógica de Negócio
│   │   │   │       ├── EquipeServico.java
│   │   │   │       └── OcorrenciaServico.java
│   │   │   │
│   │   │   ├── web/                         # Camada Web
│   │   │   │   ├── controlador/             # Controllers REST
│   │   │   │   │   ├── AuthControlador.java
│   │   │   │   │   ├── UsuarioControlador.java
│   │   │   │   │   ├── AmbulanciaControlador.java
│   │   │   │   │   └── ...
│   │   │   │   │
│   │   │   │   └── dto/                     # Data Transfer Objects
│   │   │   │       ├── LoginDTO.java
│   │   │   │       ├── CriarUsuarioDTO.java
│   │   │   │       └── ...
│   │   │   │
│   │   │   └── util/                        # Utilitários
│   │   │       ├── AlgoritmoDijkstra.java
│   │   │       └── ResultadoRota.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties      # Configurações
│   │       ├── schema.sql                  # Script SQL
│   │       └── data/                       # Dados iniciais
│   │           ├── bairros.csv
│   │           └── ruas_conexoes.csv
│   │
│   ├── pom.xml                             # Dependências Maven
│   └── mvnw.cmd                            # Maven Wrapper
│
├── frontend/                               # Frontend React + Vite
│   ├── src/
│   │   ├── App.jsx                         # Componente raiz
│   │   ├── main.jsx                        # Entry point
│   │   │
│   │   ├── pages/                          # Páginas
│   │   │   ├── Login.jsx                   # Página de login
│   │   │   └── admin/                      # Páginas admin
│   │   │       ├── AdminLayout.jsx         # Layout principal
│   │   │       └── sections/               # Seções do painel
│   │   │           ├── GerenciarUsuarios.jsx
│   │   │           ├── GerenciarAmbulancias.jsx
│   │   │           ├── GerenciarFuncionarios.jsx
│   │   │           └── GerenciarEquipes.jsx
│   │   │
│   │   ├── services/                       # Serviços de API
│   │   │   ├── authService.js              # Autenticação
│   │   │   ├── usuarioService.js
│   │   │   ├── ambulanciaService.js
│   │   │   ├── profissionalService.js
│   │   │   └── equipeService.js
│   │   │
│   │   └── assets/                         # Assets estáticos
│   │
│   ├── package.json                        # Dependências npm
│   └── vite.config.js                     # Config Vite
│
└── README.md                              # Este arquivo
```

---

## 🏗️ Arquitetura

### Backend (Spring Boot)

O backend segue a arquitetura em camadas:

```
┌─────────────────────────────────────┐
│   Web Layer (Controllers)           │  ← Recebe requisições HTTP
│   - AuthControlador                 │
│   - UsuarioControlador              │
│   - ...                             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Service Layer (Lógica de Negócio) │  ← Regras de negócio
│   - EquipeServico                   │
│   - OcorrenciaServico               │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Domain Layer                       │
│   ├── Model (Entidades JPA)         │  ← Modelos de dados
│   ├── Repository (JPA Repositories) │  ← Acesso a dados
│   └── Service (Business Logic)      │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Database (PostgreSQL)              │  ← Persistência
└─────────────────────────────────────┘
```

**Padrões Utilizados:**
- **Repository Pattern:** Abstração do acesso a dados
- **DTO Pattern:** Transferência de dados entre camadas
- **Service Layer:** Lógica de negócio isolada

### Frontend (React)

O frontend segue uma arquitetura baseada em componentes:

```
┌─────────────────────────────────────┐
│   App.jsx                           │  ← Roteamento e autenticação
└──────────────┬──────────────────────┘
               │
    ┌──────────┴──────────┐
    │                     │
┌───▼──────┐      ┌───────▼──────┐
│  Login   │      │ AdminLayout  │
└──────────┘      └───────┬──────┘
                          │
              ┌───────────┼───────────┐
              │           │           │
      ┌───────▼──┐  ┌─────▼────┐  ┌──▼──────┐
      │ Usuários │  │Ambulâncias│  │Equipes  │
      └──────────┘  └───────────┘  └─────────┘
              │
      ┌───────▼──────┐
      │  Services    │  ← Comunicação com API
      └──────────────┘
```

**Padrões Utilizados:**
- **Component-Based:** Componentes reutilizáveis
- **Service Layer:** Abstração de chamadas à API
- **State Management:** useState/useEffect para estado local

---

## 🧭 Guia de Navegação no Código

### Como Encontrar Código Relacionado

#### 1. **Autenticação e Login**

**Frontend:**
- `frontend/src/pages/Login.jsx` - Tela de login
- `frontend/src/services/authService.js` - Serviço de autenticação
- `frontend/src/App.jsx` - Controle de autenticação

**Backend:**
- `backend/src/main/java/.../web/controlador/AuthControlador.java` - Endpoint de login
- `backend/src/main/java/.../web/dto/LoginDTO.java` - DTO de requisição
- `backend/src/main/java/.../web/dto/LoginResponseDTO.java` - DTO de resposta
- `backend/src/main/java/.../config/ConfiguracaoSeguranca.java` - Configuração de segurança

#### 2. **Gerenciamento de Usuários**

**Frontend:**
- `frontend/src/pages/admin/sections/GerenciarUsuarios.jsx` - Interface
- `frontend/src/services/usuarioService.js` - Chamadas à API

**Backend:**
- `backend/src/main/java/.../web/controlador/UsuarioControlador.java` - Endpoints REST
- `backend/src/main/java/.../dominio/modelo/Usuario.java` - Modelo de dados
- `backend/src/main/java/.../dominio/repositorio/UsuarioRepositorio.java` - Acesso a dados
- `backend/src/main/java/.../web/dto/CriarUsuarioDTO.java` - DTO de criação
- `backend/src/main/java/.../web/dto/UsuarioDTO.java` - DTO de resposta

#### 3. **Gerenciamento de Ambulâncias**

**Frontend:**
- `frontend/src/pages/admin/sections/GerenciarAmbulancias.jsx`
- `frontend/src/services/ambulanciaService.js`

**Backend:**
- `backend/src/main/java/.../web/controlador/AmbulanciaControlador.java`
- `backend/src/main/java/.../dominio/modelo/Ambulancia.java`
- `backend/src/main/java/.../dominio/repositorio/AmbulanciaRepositorio.java`

#### 4. **Adicionar Nova Funcionalidade**

Siga este padrão:

1. **Backend:**
   - Criar Model em `dominio/modelo/`
   - Criar Repository em `dominio/repositorio/`
   - Criar DTOs em `web/dto/`
   - Criar Controller em `web/controlador/`
   - Adicionar tabela no `schema.sql`

2. **Frontend:**
   - Criar Service em `services/`
   - Criar Component em `pages/admin/sections/`
   - Adicionar rota no `AdminLayout.jsx`

### Convenções de Nomenclatura

**Backend:**
- Classes: `PascalCase` (ex: `UsuarioControlador`)
- Métodos: `camelCase` (ex: `listarUsuarios`)
- Arquivos: Mesmo nome da classe

**Frontend:**
- Componentes: `PascalCase` (ex: `GerenciarUsuarios`)
- Arquivos: Mesmo nome do componente
- Services: `camelCase` (ex: `usuarioService.js`)

---

## 🔄 Fluxos Principais

### Fluxo de Autenticação

```
1. Usuário acessa /login
   └─> Login.jsx renderiza

2. Usuário preenche credenciais e submete
   └─> authService.login(login, senha)
       └─> POST /api/auth/login
           └─> AuthControlador.login()
               ├─> Busca usuário no banco
               ├─> Valida senha (BCrypt)
               └─> Retorna LoginResponseDTO

3. Frontend recebe resposta
   ├─> Salva no localStorage
   └─> Redireciona para AdminLayout
```

### Fluxo de Criação de Usuário

```
1. Admin clica em "Novo Usuário"
   └─> GerenciarUsuarios.jsx abre modal

2. Admin preenche formulário e submete
   └─> usuarioService.criarUsuario(dados)
       └─> POST /api/usuarios
           └─> UsuarioControlador.criar()
               ├─> Valida dados (DTO)
               ├─> Criptografa senha (BCrypt)
               ├─> Define perfil como "USER"
               ├─> Salva no banco
               └─> Retorna UsuarioDTO

3. Frontend atualiza lista
   └─> Recarrega usuários
```

### Fluxo de Listagem

```
1. Componente monta
   └─> useEffect() executa
       └─> service.listar()
           └─> GET /api/entidade
               └─> Controller.listar()
                   └─> Repository.findAll()
                       └─> Retorna List<Entidade>

2. Frontend atualiza estado
   └─> Renderiza lista
```

---

## 🔌 API Endpoints

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/login` | Autenticar usuário |

**Request:**
```json
{
  "login": "admin",
  "senha": "admin123"
}
```

**Response:**
```json
{
  "id": 1,
  "login": "admin",
  "nome": "Administrador",
  "email": "admin@sistema.local",
  "perfil": "ADMIN",
  "ativo": true,
  "token": "mock-token"
}
```

### Usuários

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/usuarios` | Listar todos |
| POST | `/api/usuarios` | Criar novo (sempre USER) |
| PUT | `/api/usuarios/{id}` | Atualizar |
| DELETE | `/api/usuarios/{id}` | Deletar |
| PUT | `/api/usuarios/{id}/toggle-status` | Ativar/Desativar |

### Ambulâncias

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/ambulancias` | Listar todas |
| POST | `/api/ambulancias` | Criar nova |
| GET | `/api/ambulancias/{id}` | Buscar por ID |
| PUT | `/api/ambulancias/{id}` | Atualizar |
| DELETE | `/api/ambulancias/{id}` | Deletar |

### Profissionais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/profissionais` | Listar todos |
| POST | `/api/profissionais` | Criar novo |
| PUT | `/api/profissionais/{id}/desativar` | Desativar |

### Equipes

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/equipes` | Listar todas |
| POST | `/api/equipes` | Criar nova |
| GET | `/api/equipes/disponiveis` | Listar disponíveis |

### Ocorrências

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/ocorrencias` | Listar todas |
| POST | `/api/ocorrencias` | Criar nova |
| POST | `/api/ocorrencias/{id}/despachar` | Despachar equipe |

### Utilitários

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/util/hash?senha=xxx` | Gerar hash BCrypt |

---

## 🛠️ Desenvolvimento

### Comandos Backend

```powershell
cd LifeTrack\backend

# Compilar
.\mvnw.cmd clean install -DskipTests

# Executar JAR
java -jar target\sos-rota-0.0.1-SNAPSHOT.jar

# Executar diretamente
.\mvnw.cmd spring-boot:run

# Testes
.\mvnw.cmd test
```

### Comandos Frontend

```powershell
cd LifeTrack\frontend

# Desenvolvimento
npm run dev

# Build produção
npm run build

# Preview build
npm run preview
```

### Hot Reload

- **Frontend:** Automático com Vite
- **Backend:** Reinicie o servidor após mudanças

---

## ➕ Como Adicionar Funcionalidades

### Exemplo: Adicionar "Gerenciar Veículos"

#### 1. Backend

**a) Criar Model:**
```java
// backend/src/main/java/.../dominio/modelo/Veiculo.java
@Entity
@Table(name = "veiculos")
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String placa;
    private String modelo;
    // ... outros campos
}
```

**b) Criar Repository:**
```java
// backend/src/main/java/.../dominio/repositorio/VeiculoRepositorio.java
public interface VeiculoRepositorio extends JpaRepository<Veiculo, Long> {
}
```

**c) Criar DTOs:**
```java
// backend/src/main/java/.../web/dto/CriarVeiculoDTO.java
public class CriarVeiculoDTO {
    private String placa;
    private String modelo;
    // ... getters/setters
}
```

**d) Criar Controller:**
```java
// backend/src/main/java/.../web/controlador/VeiculoControlador.java
@RestController
@RequestMapping("/api/veiculos")
@CrossOrigin(origins = "*")
public class VeiculoControlador {
    // Implementar CRUD
}
```

**e) Adicionar Tabela no Schema:**
```sql
-- backend/src/main/resources/schema.sql
CREATE TABLE IF NOT EXISTS veiculos (
    id BIGSERIAL PRIMARY KEY,
    placa VARCHAR(10) NOT NULL,
    modelo VARCHAR(100),
    -- ... outros campos
);
```

#### 2. Frontend

**a) Criar Service:**
```javascript
// frontend/src/services/veiculoService.js
const API_URL = 'http://localhost:8081/api';

export const veiculoService = {
  async listar() {
    // Implementar
  },
  async criar(veiculo) {
    // Implementar
  },
  // ... outros métodos
};
```

**b) Criar Component:**
```javascript
// frontend/src/pages/admin/sections/GerenciarVeiculos.jsx
import { useState, useEffect } from 'react';
import { veiculoService } from '../../../services/veiculoService';
import '../AdminDashboard.css';

function GerenciarVeiculos() {
  // Implementar componente
}
```

**c) Adicionar no AdminLayout:**
```javascript
// frontend/src/pages/admin/AdminLayout.jsx
import GerenciarVeiculos from './sections/GerenciarVeiculos';

// Adicionar no menuItems:
{ id: 'veiculos', label: 'Veículos', icon: '🚗' }

// Adicionar no renderContent:
case 'veiculos':
  return <GerenciarVeiculos />;
```

---

## 🐛 Troubleshooting

### Erro 500 no Login

**Causa:** Usuário não existe ou senha hash inválida.

**Solução:**
1. Verifique se o usuário existe: `SELECT * FROM usuarios WHERE login = 'admin';`
2. Gere hash correto: `http://localhost:8081/api/util/hash?senha=admin123`
3. Atualize no banco: `UPDATE usuarios SET senha_hash = 'HASH_AQUI' WHERE login = 'admin';`

### Port 8081 Already in Use

**Solução:**
Altere em `application.properties`: `server.port=8082`

### Cannot Connect to Database

**Verificações:**
1. PostgreSQL está rodando?
2. Senha correta no `application.properties`?
3. Banco `pi_2025_2` existe?
4. Nome do banco está em minúsculas?

### Maven não encontrado

**Solução:**
Use o Maven Wrapper: `.\mvnw.cmd clean install`

### Frontend não conecta com Backend

**Verificações:**
1. Backend está rodando em `http://localhost:8081`?
2. CORS está habilitado? (já está configurado)
3. URL no service está correta?

---

## 📝 Ordem de Cadastro Recomendada

1. **Bairros** (via DBeaver ou API)
2. **Usuário Admin** (via SQL ou endpoint util)
3. **Usuários** (via Painel Admin)
4. **Ambulâncias** (via Painel Admin)
5. **Profissionais** (via Painel Admin)
6. **Equipes** (via Painel Admin - associa ambulância + profissionais)

---

## 🔐 Segurança

### Autenticação

- Senhas são criptografadas com **BCrypt**
- Hash é gerado automaticamente ao criar usuários
- Admin só pode criar usuários com perfil "USER"

### CORS

- Configurado para aceitar requisições de `http://localhost:5173`
- Em produção, ajuste em `ConfiguracaoSeguranca.java`

---

## 📚 Recursos Adicionais

### Scripts SQL Úteis

- `backend/CRIAR_USUARIO_ADMIN.sql` - Criar usuário admin
- `backend/ATUALIZAR_SENHA_ADMIN.sql` - Atualizar senha
- `backend/ALTER_TABLE_USUARIOS.sql` - Adicionar campos

### Documentação

- **Spring Boot:** https://spring.io/projects/spring-boot
- **React:** https://react.dev
- **Vite:** https://vitejs.dev

---

**Desenvolvido com ❤️ para gestão eficiente de emergências**
