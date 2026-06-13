# LifeTrack

Guia oficial para executar e entender o sistema. Se você só quiser subir o projeto, siga a seção **Como rodar**.

## O que é o projeto

O LifeTrack é um sistema para gestão, triagem e despacho de ocorrências de emergência, com backend em Spring Boot e frontend em Angular.

## Estrutura

```text
LifeTrack/
├── backend/      # API Spring Boot + acesso ao banco
├── frontend/     # Aplicação Angular
├── docs/         # Documentação técnica e regras de negócio
└── GUIA_DE_COMMITS.md
```

## Pré-requisitos

- Java 17 instalado.
- Node.js 20 ou superior instalado.
- npm disponível no terminal.
- PostgreSQL em execução na máquina local.
- VS Code ou outro editor para abrir o monorepo.

## Configuração do banco

O backend está configurado para conectar em `localhost:5432`, usando o banco `postgres`, usuário `postgres` e senha `5432` conforme [backend/src/main/resources/application.properties](backend/src/main/resources/application.properties).

Se o seu PostgreSQL estiver com outros dados, ajuste esse arquivo antes de iniciar o backend.

O banco do projeto é criado automaticamente pelo Hibernate com `ddl-auto=update`, e o script de inicialização em `db/migration` é executado na inicialização.

## Como rodar

### 1. Backend

```powershell
cd LifeTrack\backend
.\mvnw.cmd clean spring-boot:run
```

Se preferir validar a compilação antes de subir:

```powershell
cd LifeTrack\backend
.\mvnw.cmd clean test
```

API disponível em `http://localhost:8081`.

### 2. Frontend

```powershell
cd LifeTrack\frontend
npm install
npm start
```

Aplicação disponível em `http://localhost:4200`.

### 3. Ordem recomendada

1. Suba o PostgreSQL.
2. Inicie o backend.
3. Inicie o frontend.
4. Acesse a aplicação no navegador.

## Credenciais iniciais

O sistema possui usuário administrador inicial:

- Login: `admin`
- Senha: `admin123`

## Validação rápida

Use estes sinais para confirmar que tudo subiu corretamente:

- Backend responde em `http://localhost:8081`.
- Frontend responde em `http://localhost:4200`.
- O login inicial autentica normalmente.

## Problemas comuns

- Se o backend falhar ao conectar, verifique PostgreSQL, credenciais e porta no `application.properties`.
- Se o frontend não abrir, confirme a instalação do Node.js e execute `npm install` dentro de `frontend`.
- Se a porta estiver ocupada, finalize o processo anterior ou ajuste a porta antes de subir o serviço.

## Referências da área

- [backend/README.md](backend/README.md)
- [frontend/README.md](frontend/README.md)
- [GUIA_DE_COMMITS.md](GUIA_DE_COMMITS.md)
