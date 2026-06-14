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

## Como rodar pela primeira vez

### 0. Antes de tudo

Para o projeto subir de primeira, estes itens precisam existir na máquina:

- Java 17 instalado.
- Node.js 20 ou superior instalado.
- npm disponível no terminal.
- PostgreSQL em execução.

### 1. Prepare o banco antes do backend

O backend **não configura o PostgreSQL sozinho**. Ele tenta conectar nestes dados por padrão:

- Host: `localhost`
- Porta: `5432`
- Banco: `postgres`
- Usuário: `postgres`
- Senha: `5432`

Essas informações estão em [backend/src/main/resources/application.properties](backend/src/main/resources/application.properties).

Se o seu PostgreSQL usar outro usuário, senha, porta ou nome de banco, ajuste esse arquivo antes de iniciar a API.

Se você quiser usar a configuração padrão do projeto, faça o PostgreSQL aceitar exatamente esses dados primeiro.

### 2. Suba o backend

Depois que o banco estiver acessível, execute:

```powershell
cd LifeTrack\backend
.\mvnw.cmd clean spring-boot:run
```

Se preferir validar a compilação antes de subir:

```powershell
cd LifeTrack\backend
.\mvnw.cmd clean test
```

A API deve ficar disponível em `http://localhost:8081`.

### 3. Suba o frontend

Em outro terminal:

```powershell
cd LifeTrack\frontend
npm install
npm start
```

A aplicação deve ficar disponível em `http://localhost:4200`.

### 4. Ordem recomendada para não travar

1. Inicie o PostgreSQL.
2. Confira ou ajuste as credenciais em `backend/src/main/resources/application.properties`.
3. Inicie o backend.
4. Inicie o frontend.
5. Acesse a aplicação no navegador.

### 5. Se for a primeira vez no seu computador

Se o banco ainda não estiver pronto, a forma mais simples é:

1. Instalar o PostgreSQL.
2. Garantir que o serviço esteja rodando.
3. Criar ou ajustar o usuário e a senha para bater com o arquivo de configuração.
4. Só então execute o comando abaixo dentro de `backend`:

```powershell
.\mvnw.cmd clean spring-boot:run
```

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

## Padrões de Projeto (Requisito Acadêmico)

Conforme as exigências da disciplina, o projeto aplica estrategicamente os seguintes padrões de projeto no backend:

1. **Singleton (`LoggerAuditoria`)**: Garante um ponto único e global de acesso para logs críticos de auditoria do sistema.
2. **Factory Method (`AmbulanciaFactory`)**: Desacopla a instanciação das ambulâncias do controlador, usando fábricas específicas para `AmbulanciaBasica` e `AmbulanciaUTI`.
3. **Template Method (`GeradorRelatorioTemplate`)**: Define a estrutura fixa (algoritmo) para a extração de relatórios, permitindo que a subclasse `GeradorRelatorioOcorrencia` personalize os dados específicos.
4. **Adapter (`CalculadorDistanciaPort` / `AlgoritmoDijkstraAdapter`)**: Adapta o utilitário `AlgoritmoDijkstra` para uma porta (interface), permitindo injetar e substituir a lógica de cálculo de distância (ex: Google Maps) no futuro sem alterar as regras de negócio.
5. **Iterator (`FrotaAmbulancias` / `AmbulanciaDisponivelIterator`)**: Permite iterar pela coleção de ambulâncias de forma inteligente, retornando nativamente apenas as que estão com status `DISPONIVEL` e `Ativa`.
6. **Decorator (`CalculadorSla` / `CalculadorSlaTransitoDecorator`)**: Permite adicionar penalidades ou bonificações no cálculo do SLA (ex: tempo de trânsito) de forma dinâmica, abraçando o Princípio Aberto/Fechado (Open/Closed).

Todas as implementações se encontram dentro do pacote estruturado `com.vitalistech.sosrota.padroes`.

## Linguagens Formais, Autômatos e Compiladores (Requisito Acadêmico)

Para contemplar as avaliações teóricas e práticas desta disciplina, o projeto foi incrementado com duas funcionalidades robustas:

1. **Validação de Entradas com Regex (Autômatos Finitos)**
   - O projeto utiliza Expressões Regulares (`@Pattern` do Jakarta Validation) nos DTOs que recebem os dados do front-end.
   - Foram validados os campos: **Placa da Ambulância**, **Telefone do Usuário** e **Senha Forte**.
   - Toda string enviada é processada nativamente pela máquina de estados finitos do motor Regex do Java. Para ver o desenho lógico dos Autômatos e a tabela de expressões exigidas para a entrega, consulte o documento `docs/validacoes_automatos.txt`.

2. **Consulta Avançada (Gramática e Compilador Simulado)**
   - Criamos um micro-compilador no backend para processar queries estruturadas do administrador (Exemplo: `parametro.tipo="BASICA" AND parametro.status="DISPONIVEL"`).
   - Isso foi alcançado modelando um **Analisador Léxico (Scanner)** e um **Analisador Sintático (Parser)** utilizando a técnica de Descida Recursiva Preditiva (*Top-Down*).
   - O endpoint para teste desse compilador se encontra em `GET /api/relatorios/consulta-avancada`.
   - Para entender a Gramática Livre de Contexto projetada para essa linguagem e mais detalhes sobre as transições, consulte o documento `docs/gramatica_consulta.txt`.

## Sistemas Distribuídos e Computação em Nuvem — Arquitetura AWS (Requisito Acadêmico)

O sistema LifeTrack foi projetado para ser implantado na **AWS (Amazon Web Services)**, seguindo os pilares do **AWS Well-Architected Framework**: segurança, confiabilidade, eficiência e otimização de custos.

### Arquitetura em 3 Camadas

```
 Internet
    │ HTTPS
    ▼
 CloudFront (CDN) ─── S3 (Frontend Angular)
    │
    ▼ Requisições de API
 Application Load Balancer (ALB)
    │
    ▼ VPC Privada
 ECS Fargate (Backend Spring Boot — Java 17)
    │
    ▼ Sub-rede Privada
 Amazon RDS PostgreSQL (banco de dados gerenciado)
```

### Serviços Escolhidos e Justificativas

| Serviço | Finalidade |
|---|---|
| **ECS Fargate** | Hospedagem do backend Spring Boot em containers serverless, sem gerenciar EC2 |
| **Amazon RDS (PostgreSQL)** | Banco de dados gerenciado com backups automáticos e failover configurável |
| **Application Load Balancer** | Roteamento HTTPS e distribuição de carga entre as réplicas do backend |
| **Amazon S3** | Hospedagem do build estático do frontend Angular |
| **Amazon CloudFront** | CDN global para o frontend — baixa latência e HTTPS automático |
| **Amazon ECR** | Registro privado das imagens Docker do backend, integrado ao ECS |
| **AWS Secrets Manager** | Gerenciamento seguro de credenciais (senha do banco, JWT secret) |
| **Amazon CloudWatch** | Monitoramento de logs, métricas de CPU/memória e alarmes |
| **Amazon VPC** | Rede virtual isolada — o banco de dados nunca fica exposto à internet |

### Estimativa de Custos (Região: sa-east-1 — São Paulo)

| Cenário | Custo Mensal (USD) | Custo Mensal (BRL ~R$5,20) |
|---|---|---|
| **On-Demand (conservador)** | ~$111,46 | ~R$ 579,59 |
| **Otimizado** (Reserved + VPC Endpoint) | ~$67,37 | ~R$ 350,32 |

O cálculo completo serviço a serviço, com premissas de uso, oportunidades de economia e referências da AWS Pricing Calculator, está documentado em `docs/calculo_custos_aws.txt`.

O diagrama textual completo da arquitetura e a justificativa técnica de cada serviço estão em `docs/arquitetura_aws.txt`.
