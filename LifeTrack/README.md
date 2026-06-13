# LifeTrack 🚑

O **LifeTrack** é um sistema robusto e altamente escalável para gestão, triagem e despacho de ambulâncias em ocorrências de emergência. Projetado com foco em alta disponibilidade, resposta rápida (SLA) e UX eficiente para operadores e administradores, o sistema garante precisão no salvamento de vidas através da otimização logística.

---

## 🏗️ Arquitetura do Sistema

O projeto adota uma arquitetura orientada a serviços com separação clara de responsabilidades, utilizando um repositório centralizado (Monorepo) que abriga tanto o backend (API RESTful) quanto o frontend (SPA).

### Backend (API RESTful)
- **Tecnologia**: Java com Spring Boot.
- **Responsabilidades**: Regras de negócio, cálculo de distâncias (Dijkstra/Grafos para rotas otimizadas), gerenciamento de estado das ocorrências, controle de SLA e persistência de dados.
- **Banco de Dados**: Gerenciamento relacional com mapeamento ORM (JPA/Hibernate), garantindo integridade e consistência transacional.

### Frontend (Single Page Application)
- **Tecnologia**: Angular (Ecossistema Moderno).
- **Responsabilidades**: Consumo da API RESTful, gerenciamento de estado no lado do cliente, proteção de rotas (Guards), interfaces modulares componentizadas, e dashboards reativos.
- **Design System**: Estilização própria baseada em componentes reutilizáveis, garantindo consistência visual (Admin e Operador) com resposta imediata a eventos.

---

## 📂 Estrutura do Repositório

O monorepo está estruturado de forma a facilitar a manutenção, integração contínua e a escalabilidade da equipe de desenvolvimento:

```text
LifeTrack/
├── backend/                # Código-fonte da API em Java/Spring Boot
│   ├── src/main/java       # Lógica de domínio, Controllers, Services e Repositories
│   ├── src/main/resources  # Configurações (application.properties, scripts SQL)
│   └── pom.xml             # Dependências e ciclo de vida do Maven
├── frontend/               # Código-fonte da SPA em Angular
│   ├── src/app/core/       # Serviços, Guards, Interceptors e Modelos TypeScript
│   ├── src/app/layouts/    # Layouts base da aplicação (AdminLayout, OperatorLayout)
│   ├── src/app/pages/      # Páginas roteáveis agrupadas por domínio (Admin/Operador)
│   └── src/app/shared/     # Componentes e diretivas reutilizáveis
└── docs/                   # Documentação técnica e regras de negócio
```

---

## 🚀 Como Executar o Projeto Localmente

Para rodar a aplicação completa, é necessário configurar o banco de dados e iniciar ambos os serviços (Backend e Frontend).

### Pré-requisito: Configuração do Banco de Dados (PostgreSQL via DBeaver)

O sistema utiliza PostgreSQL. Siga os passos para provisionar o banco de dados localmente usando o DBeaver:

1. **Abra o DBeaver** e clique no ícone **"Nova Conexão"** (tomada no canto superior esquerdo).
2. Selecione **PostgreSQL** e clique em Avançar.
3. Preencha as credenciais padrão do ambiente local (conforme `application.properties`):
   - **Host:** `localhost`
   - **Porta:** `5432`
   - **Database:** `postgres` (banco default apenas para conectar a primeira vez)
   - **Username:** `postgres`
   - **Password:** `5432`
4. Clique em **Testar Conexão** para baixar os drivers (se solicitado) e garantir que o PostgreSQL está respondendo. Depois clique em **Concluir**.
5. No painel esquerdo, expanda a conexão recém-criada, clique com o botão direito em **Databases** > **Create New Database**.
6. No campo **Database Name**, digite exatamente: **`pi_2025_2`** e clique em OK.
7. *Pronto!* Não é necessário rodar scripts de criação de tabelas. O Hibernate (Spring Boot) está configurado com `ddl-auto=update` e criará toda a estrutura relacional automaticamente ao rodar o backend.

---

### 1. Executando o Backend (Spring Boot)
Certifique-se de ter o **Java (JDK 17+)** e o **Maven** instalados.

1. Navegue até o diretório do backend:
   ```bash
   cd backend
   ```
2. Instale as dependências e inicie a aplicação:
   ```cmd
   .\mvnw.cmd clean install -DskipTests
   .\mvnw.cmd spring-boot:run
   ```
   *Alternativamente, no Windows (PowerShell), caso ocorra erro de execução de script, utilize:*
   `powershell -ExecutionPolicy Bypass -File .\rodar.ps1`
3. A API estará disponível em `http://localhost:8081`.

### 2. Executando o Frontend (Angular)
Certifique-se de ter o **Node.js (v18+)** e o **Angular CLI** instalados.

1. Navegue até o diretório do frontend:
   ```bash
   cd frontend
   ```
2. Instale as dependências do projeto:
   ```bash
   npm install
   ```
3. Inicie o servidor de desenvolvimento:
   ```bash
   npm run start
   ```
   *Ou diretamente via Angular CLI:* `ng serve`
4. Acesse a aplicação no navegador através de `http://localhost:4200`.

---

### 3. Acesso ao Sistema e Criação de Usuários

O sistema já é inicializado com um usuário administrador padrão:
- **Login:** `admin`
- **Senha:** `admin123`

#### Como criar perfis Staff (Operadores/Usuários)
A criação de novos usuários com perfil padrão (`USER` / staff) é feita via API (podendo ser integrada pela interface). Apenas administradores não podem ser criados via endpoint por segurança.
- **Endpoint:** `POST http://localhost:8081/api/usuarios`
- **Corpo da requisição (JSON de Exemplo):**
  ```json
  {
    "username": "operador1",
    "password": "senhaSegura123",
    "nome": "João da Silva",
    "email": "joao@sistema.local",
    "telefone": "11999999999"
  }
  ```

---

## 🛡️ Padrões e Melhores Práticas Adotadas

Visando a manutenibilidade, testabilidade e legibilidade do código, o LifeTrack implementa:
- **Design Patterns**: Injeção de Dependências (DI), Singleton (Services), Observable/Observer (RxJS).
- **Clean Code & SOLID**: Separação rigorosa entre regras de negócio (Services no Angular / Backend) e regras de apresentação (Components/Controllers).
- **Segurança Integrada**: Autenticação e Autorização robustas via Interceptors no Frontend e Filtros no Backend. Rotas protegidas (Route Guards) baseadas em perfis de acesso estritos (Admin vs Operador).
- **Tipagem Estrita**: Uso avançado de TypeScript no frontend para mitigar erros em tempo de execução, garantindo contratos sólidos na comunicação com a API.
