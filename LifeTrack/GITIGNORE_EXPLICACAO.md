# 📋 Explicação do .gitignore

Este documento explica as escolhas feitas no arquivo `.gitignore` do projeto LifeTrack e as vantagens de cada configuração.

---

## 🎯 Objetivo do .gitignore

O arquivo `.gitignore` instrui o Git a **ignorar** certos arquivos e diretórios, evitando que sejam versionados no repositório. Isso é essencial para manter o repositório limpo, seguro e eficiente.

---

## 📦 Seções do .gitignore

### 1. Java + Maven (Backend)

#### O que é ignorado:
- `target/` - Diretório de compilação do Maven
- `*.class` - Arquivos compilados Java
- `*.jar`, `*.war`, `*.ear` - Arquivos empacotados
- Arquivos de configuração do Maven (`.mvn/`, `pom.xml.*`)

#### Por quê?
✅ **Vantagens:**
- **Tamanho do repositório**: Arquivos compilados são grandes e desnecessários no Git
- **Regeneráveis**: Podem ser recriados a qualquer momento com `mvn clean install`
- **Específicos do ambiente**: Cada desenvolvedor compila localmente
- **Evita conflitos**: Não há conflitos de merge em arquivos binários

❌ **Sem isso:**
- Repositório ficaria gigantesco (centenas de MB)
- Conflitos constantes em arquivos `.class`
- Commits desnecessários a cada compilação

---

### 2. Spring Boot

#### O que é ignorado:
- `application-local.properties` - Configurações locais
- `*.log` - Arquivos de log
- `.apt_generated/` - Arquivos gerados automaticamente

#### Por quê?
✅ **Vantagens:**
- **Segurança**: Arquivos locais podem conter senhas e credenciais
- **Limpeza**: Logs são temporários e específicos de cada execução
- **Flexibilidade**: Cada desenvolvedor tem suas próprias configurações locais

❌ **Sem isso:**
- Risco de expor senhas de banco de dados no Git
- Logs com informações sensíveis versionados
- Configurações locais sobrescrevendo as do time

> **Nota**: O arquivo `application.properties` principal **NÃO** está no `.gitignore` porque contém configurações padrão que devem ser compartilhadas.

---

### 3. Node.js / Vite (Frontend)

#### O que é ignorado:
- `node_modules/` - Dependências do npm
- `dist/`, `dist-ssr/` - Builds de produção
- `*.log` - Logs do npm/yarn
- `.npm`, `.eslintcache` - Caches

#### Por quê?
✅ **Vantagens:**
- **Tamanho**: `node_modules/` pode ter centenas de MB
- **Regenerável**: Dependências são instaladas via `npm install`
- **Performance**: Git fica mais rápido sem milhares de arquivos
- **Consistência**: Todos usam as mesmas versões definidas no `package.json`

❌ **Sem isso:**
- Repositório com vários GB de tamanho
- Commits lentos e pesados
- Conflitos em arquivos de dependências
- Diferenças entre versões de pacotes instalados

---

### 4. IDEs e Editores

#### O que é ignorado:
- `.vscode/` - Configurações do Visual Studio Code
- `.idea/` - Configurações do IntelliJ IDEA
- `.classpath`, `.project` - Configurações do Eclipse
- `*.iml`, `*.ipr` - Arquivos do IntelliJ

#### Por quê?
✅ **Vantagens:**
- **Preferências pessoais**: Cada desenvolvedor tem suas configurações
- **Evita conflitos**: Configurações de IDE mudam frequentemente
- **Flexibilidade**: Time pode usar diferentes IDEs

❌ **Sem isso:**
- Conflitos constantes em arquivos de configuração
- Configurações pessoais sobrescrevendo as do time
- Commits desnecessários a cada ajuste de IDE

> **Exceção**: Alguns arquivos úteis são mantidos (como `extensions.json` do VSCode) para recomendações ao time.

---

### 5. Windows

#### O que é ignorado:
- `Thumbs.db` - Cache de miniaturas do Windows
- `Desktop.ini` - Configurações de pastas
- `$RECYCLE.BIN/` - Lixeira
- `*.lnk` - Atalhos

#### Por quê?
✅ **Vantagens:**
- **Arquivos do sistema**: Não são relevantes para o código
- **Regeneráveis**: Windows recria automaticamente
- **Limpeza**: Mantém o repositório focado no código

❌ **Sem isso:**
- Commits acidentais de arquivos do sistema
- Repositório poluído com arquivos irrelevantes

---

### 6. Arquivos Temporários e Backups

#### O que é ignorado:
- `*.bak`, `*.swp`, `*.tmp` - Backups e temporários
- `*~` - Arquivos de backup do editor
- `.env` - Variáveis de ambiente

#### Por quê?
✅ **Vantagens:**
- **Segurança**: Arquivos `.env` podem conter senhas
- **Limpeza**: Arquivos temporários não devem ser versionados
- **Organização**: Mantém apenas código-fonte relevante

---

## 🎁 Benefícios Gerais

### 1. **Repositório Mais Leve**
- Sem arquivos compilados, dependências e caches
- Clones mais rápidos
- Menos uso de banda e armazenamento

### 2. **Segurança**
- Senhas e credenciais não são versionadas
- Configurações locais não vazam para o repositório
- Reduz risco de exposição de dados sensíveis

### 3. **Performance**
- Git opera mais rápido com menos arquivos
- Commits e pushes mais rápidos
- Melhor experiência de desenvolvimento

### 4. **Organização**
- Repositório focado apenas no código-fonte
- Histórico limpo e relevante
- Facilita code review

### 5. **Flexibilidade**
- Cada desenvolvedor pode ter configurações locais
- Diferentes IDEs podem ser usados
- Ambientes de desenvolvimento personalizados

---

## 📝 Boas Práticas

### ✅ O que DEVE estar no Git:
- Código-fonte (`.java`, `.jsx`, `.js`)
- Arquivos de configuração padrão (`application.properties`, `package.json`)
- Scripts e documentação
- Arquivos de schema (`schema.sql`)
- Arquivos de dados iniciais (CSVs, etc.)

### ❌ O que NÃO DEVE estar no Git:
- Arquivos compilados (`.class`, `.jar`)
- Dependências (`node_modules/`, `target/`)
- Configurações locais (`.env`, `application-local.properties`)
- Arquivos de log
- Configurações de IDE pessoais
- Arquivos do sistema operacional

---

## 🔄 Fluxo de Trabalho

### Ao clonar o repositório:

1. **Backend:**
   ```powershell
   cd LifeTrack\backend
   .\mvnw.cmd clean install -DskipTests
   ```
   - Maven baixa dependências e compila o projeto
   - Pasta `target/` é criada localmente (não versionada)

2. **Frontend:**
   ```powershell
   cd LifeTrack\frontend
   npm install
   ```
   - npm instala dependências do `package.json`
   - Pasta `node_modules/` é criada localmente (não versionada)

### Ao fazer alterações:

1. Edite apenas arquivos de código-fonte
2. Compile/execute localmente
3. Commit apenas código-fonte e configurações padrão
4. Nunca commite arquivos gerados automaticamente

---

## 🚨 Atenção

### Arquivos Sensíveis

Se você **acidentalmente** commitou um arquivo com senhas:

1. **Remova do histórico:**
   ```bash
   git rm --cached arquivo-sensivel
   git commit -m "Remove arquivo sensível"
   ```

2. **Se já foi para o repositório remoto:**
   - Considere as credenciais como comprometidas
   - Altere todas as senhas expostas
   - Use `git filter-branch` ou ferramentas similares para limpar o histórico

### Verificar o que será ignorado

Para ver quais arquivos estão sendo ignorados:
```bash
git status --ignored
```

---

## 📚 Referências

- [Documentação oficial do Git - gitignore](https://git-scm.com/docs/gitignore)
- [GitHub - gitignore templates](https://github.com/github/gitignore)
- [Maven - What to ignore](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)

---

**Última atualização:** Dezembro 2024

