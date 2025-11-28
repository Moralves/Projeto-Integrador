# LifeTrack | React (Vite) + Spring Boot

LifeTrack é um workspace full-stack que combina um frontend em React (com Vite) e um backend em Spring Boot. Use este guia para preparar o ambiente, executar os serviços e manter o fluxo de desenvolvimento organizado.

## Estrutura do Projeto

```
LifeTrack
├── frontend/                   # Aplicação React + Vite
│   ├── package.json            # Scripts npm e dependências
│   ├── vite.config.js
│   ├── public/
│   │   └── vite.svg
│   └── src/
│       ├── main.jsx            # Ponto de entrada React
│       ├── App.jsx             # Componente raiz
│       ├── App.css | index.css # Estilos globais
│       └── assets/
├── backend/                    # Aplicação Spring Boot
│   ├── pom.xml                 # Configuração Maven
│   └── src/
│       ├── main/
│       │   ├── java/com/example/app/
│       │   │   ├── Application.java
│       │   │   ├── controller/ExampleController.java
│       │   │   ├── service/ExampleService.java
│       │   │   └── model/ExampleModel.java
│       │   └── resources/application.properties
│       └── test/java/com/example/app/ApplicationTests.java
└── README.md
```

## Pré-requisitos

- Node.js 18+ e npm (testado com Vite 7)
- Java 17+ (LTS) e Maven 3.9+
- IDE/Editor com suporte a React e Java (VS Code + IntelliJ/Eclipse recomendado)

## Configurando o Frontend (React + Vite)

1. Instale dependências:
   ```
   cd frontend
   npm install
   ```
2. Ambiente de desenvolvimento com hot reload:
   ```
   npm run dev
   ```
   O Vite expõe a aplicação em `http://localhost:5173` por padrão.
3. Build de produção:
   ```
   npm run build
   ```
4. Preview do build:
   ```
   npm run preview
   ```
5. Lint opcional (ESLint):
   ```
   npm run lint
   ```

## Configurando o Backend (Spring Boot)

1. Instale dependências e gere o artefato:
   ```
   cd backend
   mvn clean install
   ```
2. Execute a API:
   ```
   mvn spring-boot:run
   ```
   O serviço inicia em `http://localhost:8080`.

## Integração Frontend ↔ Backend

- Exponha endpoints REST no backend (`backend/src/main/java/com/example/app/controller/ExampleController.java`).
- Consuma-os no frontend criando clientes HTTP (por exemplo, `fetch` ou `axios`) na pasta `frontend/src/`.
- Durante o desenvolvimento, mantenha ambos os servidores rodando simultaneamente (Vite e Spring Boot).

## Fluxo de Desenvolvimento Recomendado

1. `npm run dev` para trabalhar a UI em tempo real.
2. `mvn spring-boot:run` para validar APIs.
3. Ajuste as dependências via `package.json` (frontend) ou `pom.xml` (backend).
4. Garanta que builds (`npm run build` e `mvn clean install`) passem antes de publicar.

## Endpoints e Contratos

- Consulte `ExampleController.java` para ver os endpoints padrão.
- Documente novos endpoints diretamente no controller ou em arquivos de especificação (Swagger/OpenAPI) para manter o contrato atualizado.

## Próximos Passos

- Personalize o README do frontend (`frontend/README.md`) se precisar detalhar fluxos específicos da UI.
- Adicione instruções de deploy (Docker, cloud, etc.) conforme o projeto evoluir.