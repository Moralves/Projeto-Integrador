# Changelog — LifeTrack

Todas as mudanças notáveis deste projeto serão documentadas neste arquivo.

Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).
Versionamento baseado em [Semantic Versioning](https://semver.org/lang/pt-BR/) — MAJOR.MINOR.PATCH.

---

## [Unreleased]

Funcionalidades planejadas para a próxima release:
- Integração com notificações em tempo real (WebSocket)
- Dashboard de métricas operacionais em tempo real

---

## [1.2.0] — 2026-06-14 — Baseline BL1: Requisitos Acadêmicos Integrados

### Adicionado
- **Padrões de Projeto:** Implementação dos 6 padrões exigidos pela disciplina no pacote `padroes/`:
  - Singleton (`LoggerAuditoria`)
  - Factory Method (`AmbulanciaFactory`, `AmbulanciaBasicaFactory`, `AmbulanciaUTIFactory`)
  - Template Method (`GeradorRelatorioTemplate`, `GeradorRelatorioOcorrencia`)
  - Adapter (`CalculadorDistanciaPort`, `AlgoritmoDijkstraAdapter`)
  - Iterator (`FrotaAmbulancias`, `AmbulanciaDisponivelIterator`)
  - Decorator (`CalculadorSla`, `CalculadorSlaPadrao`, `CalculadorSlaTransitoDecorator`)
- **Linguagens Formais:** Validações via `@Pattern` (Regex) nos DTOs para placa, telefone e senha
- **Compilador Simulado:** `AnalisadorLexico` + `AnalisadorSintatico` com GLC para consulta avançada
- **Endpoint consulta avançada:** `GET /api/relatorios/consulta-avancada?query=...`
- **Arquitetura AWS:** Documentação completa em `docs/arquitetura_aws.txt`
- **Cálculo de custos AWS:** Estimativa por serviço em `docs/calculo_custos_aws.txt`
- **CI Pipeline:** `.github/workflows/ci.yml` com build do backend e frontend
- **Documentação GCS:** Catálogo de ICs, Baselines, Matriz de Rastreabilidade e RFC formal

### Modificado
- `OcorrenciaServico` refatorado para usar Adapter e Iterator
- `AnaliseEstrategicaServico` refatorado para usar Adapter
- `AmbulanciaControlador` refatorado para usar Factory
- `RelatorioControlador` refatorado para usar Template Method + novo endpoint
- `CriarAmbulanciaDTO` com validação Regex de placa
- `CriarUsuarioDTO` com validação Regex de telefone e senha
- `README.md` atualizado com todas as seções acadêmicas

---

## [1.1.0] — 2026-06-10 — Migração Angular e Correções de Backend

### Adicionado
- Frontend migrado para Angular (SPA moderna)
- Roteamento completo com Guards de autenticação (Admin / Operador)
- Componentes: `registrar-ocorrencia`, `gerenciar-equipes`, `gerenciar-funcionarios`

### Corrigido
- Duplicação de profissionais ao adicionar a equipes (`EquipeServico`)
- Conflito `*ngIf/*ngFor` no template `gerenciar-equipes.component.html`
- Tipagem estrita no `gerenciar-funcionarios.component.ts`

### Removido
- Diretório legado `frontend-react-legacy/`

---

## [1.0.0] — 2026-06-03 — Baseline BL0: Sistema Funcional Inicial

### Adicionado
- Backend Spring Boot com arquitetura Controller → Service → Repository
- Entidades principais: `Ocorrencia`, `Ambulancia`, `Equipe`, `Profissional`, `Bairro`, `RuaConexao`
- Algoritmo de Dijkstra para cálculo de rota mínima entre bairros
- Sistema de despacho automático de ambulâncias
- Autenticação JWT com perfis `ADMIN` e `OPERADOR`
- Relatórios de ocorrências com histórico detalhado
- Banco de dados PostgreSQL com Flyway para migrações
- API REST documentada com endpoints de ambulâncias, ocorrências, equipes e profissionais

---

[Unreleased]: https://github.com/Moralves/Projeto-Integrador/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/Moralves/Projeto-Integrador/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/Moralves/Projeto-Integrador/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/Moralves/Projeto-Integrador/releases/tag/v1.0.0
