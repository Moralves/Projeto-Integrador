# Script PowerShell para executar o backend Spring Boot
# Este script usa o Maven Wrapper (mvnw) que não requer instalação do Maven

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  LifeTrack - Backend Spring Boot" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se Java está instalado
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "ERRO: Java não encontrado!" -ForegroundColor Red
    Write-Host "Por favor, instale o Java 11 ou superior." -ForegroundColor Yellow
    exit 1
}

# Configurar JAVA_HOME automaticamente se não estiver definido
if (-not $env:JAVA_HOME) {
    $javaPath = (Get-Command java).Source
    $javaHome = Split-Path (Split-Path $javaPath)
    $env:JAVA_HOME = $javaHome
    Write-Host "JAVA_HOME configurado automaticamente: $javaHome" -ForegroundColor Yellow
}

Write-Host "Java encontrado:" -ForegroundColor Green
java -version
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray
Write-Host ""

# Verificar se o arquivo mvnw.cmd existe
if (-not (Test-Path "mvnw.cmd")) {
    Write-Host "ERRO: Maven Wrapper não encontrado!" -ForegroundColor Red
    Write-Host "O arquivo mvnw.cmd deve estar na pasta backend." -ForegroundColor Yellow
    exit 1
}

Write-Host "Iniciando build e execução do backend..." -ForegroundColor Yellow
Write-Host ""

# Executar Maven Wrapper
.\mvnw.cmd clean install

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERRO no build!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Build concluído com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "Iniciando aplicação Spring Boot..." -ForegroundColor Yellow
Write-Host ""

.\mvnw.cmd spring-boot:run

