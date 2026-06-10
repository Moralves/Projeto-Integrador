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

Para rodar a aplicação completa, é necessário iniciar ambos os serviços (Backend e Frontend).

### 1. Executando o Backend (Spring Boot)
Certifique-se de ter o **Java (JDK 17+)** e o **Maven** instalados.

1. Navegue até o diretório do backend:
   ```bash
   cd backend
   ```
2. Instale as dependências e inicie a aplicação:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   *Alternativamente, no Windows, utilize o script fornecido:* `.\rodar.ps1`
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

## 🛡️ Padrões e Melhores Práticas Adotadas

Visando a manutenibilidade, testabilidade e legibilidade do código, o LifeTrack implementa:
- **Design Patterns**: Injeção de Dependências (DI), Singleton (Services), Observable/Observer (RxJS).
- **Clean Code & SOLID**: Separação rigorosa entre regras de negócio (Services no Angular / Backend) e regras de apresentação (Components/Controllers).
- **Segurança Integrada**: Autenticação e Autorização robustas via Interceptors no Frontend e Filtros no Backend. Rotas protegidas (Route Guards) baseadas em perfis de acesso estritos (Admin vs Operador).
- **Tipagem Estrita**: Uso avançado de TypeScript no frontend para mitigar erros em tempo de execução, garantindo contratos sólidos na comunicação com a API.
