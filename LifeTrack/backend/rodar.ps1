# Script para executar o Backend Spring Boot
# Versão simplificada - executa o JAR compilado

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  LifeTrack Backend - Spring Boot" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Java
Write-Host "[1/3] Verificando Java..." -ForegroundColor Yellow
try {
    $javaOutput = java -version 2>&1
    $javaLine = $javaOutput | Select-Object -First 1
    Write-Host "    OK: $javaLine" -ForegroundColor Green
} catch {
    Write-Host "    ERRO: Java nao encontrado!" -ForegroundColor Red
    Write-Host "    Instale o Java 17+ (LTS)" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Verificar JAR
Write-Host "[2/3] Verificando JAR compilado..." -ForegroundColor Yellow
$jarPath = ".\target\sos-rota-0.0.1-SNAPSHOT.jar"
if (Test-Path $jarPath) {
    $jarSize = (Get-Item $jarPath).Length / 1MB
    Write-Host "    OK: JAR encontrado ($([math]::Round($jarSize, 2)) MB)" -ForegroundColor Green
} else {
    Write-Host "    ERRO: JAR nao encontrado!" -ForegroundColor Red
    Write-Host "    Execute: mvn clean install" -ForegroundColor Yellow
    Read-Host "Pressione Enter para sair"
    exit 1
}

# Executar
Write-Host "[3/3] Iniciando aplicacao..." -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Backend iniciando em http://localhost:8080" -ForegroundColor Green
Write-Host "  Pressione Ctrl+C para parar" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

java -jar $jarPath
