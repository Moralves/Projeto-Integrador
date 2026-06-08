# Plano de Migração Frontend: React (Vite) para Angular (Latest)

Este documento detalha o plano estratégico e estruturado para migrar o projeto frontend atual (baseado em React 19 + Vite) para a versão mais recente do Angular. A arquitetura foi desenhada para uma transição suave, permitindo validação em cada etapa e mantendo a paridade de funcionalidades.

A migração foi dividida em um roteiro orientado a commits para garantir um histórico limpo, rollback facilitado e revisões de código eficientes.

## Estrutura Atual (React) vs Destino (Angular)
- **Componentes**: Funcionais (JSX) → Classes TypeScript (`@Component`).
- **Estado**: `useState`/`useEffect` → Propriedades de classe, Signals (Angular 16+) ou RxJS (`BehaviorSubject`).
- **Roteamento**: React Router (implícito no `App.jsx` ou layouts) → Angular Router (`app.routes.ts`).
- **Serviços**: Funções isoladas exportadas (`.js`) → Classes Injetáveis (`@Injectable`).
- **Estilização**: Arquivos `.css` soltos → Estilos encapsulados por componente e `styles.css` global.

---

## Estratégia de Commits e Execução

### Commit 1: Setup Inicial do Projeto Angular [CONCLUÍDO]
**Objetivo**: Inicializar o ecossistema Angular.
- Gerar o novo projeto usando Angular CLI configurado com CSS Puro e Standalone.
- Transferir assets do projeto antigo para o novo.
- Copiar e adaptar os estilos globais (`index.css` e `App.css`) para o `styles.css` global do Angular.

### Commit 2: Core e Modelagem de Dados (TypeScript) [CONCLUÍDO]
**Objetivo**: Tipar os dados que transitam na aplicação.
- Criar interfaces TypeScript para as entidades baseadas nos serviços atuais (`Usuario`, `Ocorrencia`, `Ambulancia`, etc).
- Configurar o `provideHttpClient` global no `app.config.ts`.

### Commit 3: Migração da Camada de Serviços [EM ANDAMENTO]
**Objetivo**: Portar as chamadas de API para Angular Services.
- Configurado o Interceptor de Autenticação (`auth.interceptor.ts`).
- Migrados: `AuthService`, `OcorrenciaService`.
- A migrar: `Ambulancia`, `Equipe`, `Profissional`, `Usuario`, etc.

### Commit 4: Componentes Compartilhados (Shared)
**Objetivo**: Migrar componentes visuais reutilizáveis.
- Migrar `SLATimer.jsx` → `sla-timer.component`.
- Migrar `HistoricoOcorrencia.jsx` → `historico-ocorrencia.component`.
- Migrar `AutocompleteSelect.jsx` → `autocomplete-select.component`.

### Commit 5: Autenticação, Layouts e Guards
**Objetivo**: Estruturar a fundação visual (cascas) e a segurança de rotas.
- Migrar `Login.jsx` → `login.component`.
- Migrar `AdminLayout.jsx` e `OperatorLayout.jsx` com `<router-outlet>`.
- Criar Route Guards para rotas admin e operador.

### Commit 6: Área do Operador (Features)
**Objetivo**: Migrar as telas específicas do perfil de Operador.
- Migrar `RegistrarOcorrencia`, `ListarOcorrencias`, `SugerirAmbulancias`.

### Commit 7: Área do Administrador (Features)
**Objetivo**: Migrar as telas gerenciais e os CRUDs completos.
- Migrar painéis de gerência (`Ambulancias`, `Equipes`, `Funcionarios`, `Usuarios`, `Relatorios`).

### Commit 8: Roteamento Principal e Limpeza
**Objetivo**: Conectar tudo, estabelecer a navegação final e remover o legado.
- Configurar `app.routes.ts` com rotas protegidas.
- Excluir o projeto React legado.
