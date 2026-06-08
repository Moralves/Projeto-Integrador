# LifeTrack Frontend (Angular)

Este é o novo projeto frontend para a aplicação LifeTrack, migrado para Angular (Standalone Components + CSS Nativo).

## Comandos Úteis

- **Servidor de Desenvolvimento**: Execute `npm run start` ou `ng serve` para rodar localmente. Acesse `http://localhost:4200/`.
- **Build de Produção**: Execute `npm run build` ou `ng build`.
- **Gerar Componentes**: `ng generate component path/to/component-name`

## Estrutura do Projeto

- `/src/app/core`: Serviços isolados, interceptors e modelos de dados (TypeScript).
- `/src/app/shared`: Componentes visuais compartilhados em todo o projeto.
- `/src/app/features`: Módulos ou páginas específicas separadas por contexto (ex: `admin`, `operador`, `auth`).
- `/src/app/layout`: Componentes de estrutura principal das páginas (ex: Navbars e Cascas das rotas).
