# Como Executar o Backend sem Instalar Maven

## ✅ Solução Rápida: Maven Wrapper (Já Configurado!)

O projeto já inclui o **Maven Wrapper**, que permite executar o Maven sem instalação!

### Opção 1: Usar o Script PowerShell (Mais Fácil)

Execute o script `executar.ps1`:

```powershell
cd LifeTrack\backend
.\executar.ps1
```

Este script vai:
1. Verificar se o Java está instalado
2. Baixar o Maven automaticamente (na primeira vez)
3. Compilar o projeto
4. Executar a aplicação

### Opção 2: Usar Maven Wrapper Manualmente

```powershell
cd LifeTrack\backend

# Primeira vez: baixar dependências e compilar
.\mvnw.cmd clean install

# Executar a aplicação
.\mvnw.cmd spring-boot:run
```

### Opção 3: Instalar Maven Globalmente (Opcional)

Se preferir usar `mvn` diretamente:

1. **Baixar o Maven:**
   - Acesse: https://maven.apache.org/download.cgi
   - Baixe: `apache-maven-3.9.x-bin.zip`

2. **Extrair e Configurar:**
   - Extraia para: `C:\Program Files\Apache\maven`
   - Adicione ao PATH: `C:\Program Files\Apache\maven\bin`
   - Como adicionar ao PATH:
     - `Win + R` → `sysdm.cpl` → "Avançado" → "Variáveis de Ambiente"
     - Edite a variável `Path` e adicione o caminho acima

3. **Verificar:**
   ```powershell
   mvn --version
   ```

### Opção 4: Usar IDE (IntelliJ IDEA / Eclipse)

IDEs como IntelliJ IDEA ou Eclipse têm Maven integrado:
- Abra o projeto na IDE
- Execute a classe `Application.java` diretamente
- Ou use os botões de build/run da IDE

## 🔧 Troubleshooting

### Erro: "JAVA_HOME não encontrado"
Configure a variável de ambiente `JAVA_HOME`:
```powershell
# Verificar onde está o Java
where java

# Configurar JAVA_HOME (ajuste o caminho)
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-21', 'User')
```

### Erro: "mvnw.cmd não encontrado"
Certifique-se de estar na pasta `LifeTrack\backend` ao executar o comando.

### Erro de permissão no PowerShell
Execute:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

