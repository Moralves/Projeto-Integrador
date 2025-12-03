# Script para testar conexão com PostgreSQL
# Ajuda a descobrir a senha correta

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Teste de Conexão PostgreSQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$host = "localhost"
$port = "5432"
$database = "PI_2025_2"
$username = "postgres"

Write-Host "Testando conexão..." -ForegroundColor Yellow
Write-Host "Host: $host" -ForegroundColor Gray
Write-Host "Porta: $port" -ForegroundColor Gray
Write-Host "Banco: $database" -ForegroundColor Gray
Write-Host "Usuário: $username" -ForegroundColor Gray
Write-Host ""

# Tentar encontrar psql
$psqlPath = $null
$possiblePaths = @(
    "C:\Program Files\PostgreSQL\17\bin\psql.exe",
    "C:\Program Files\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files\PostgreSQL\15\bin\psql.exe",
    "C:\Program Files\PostgreSQL\14\bin\psql.exe"
)

foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $psqlPath = $path
        break
    }
}

if (-not $psqlPath) {
    Write-Host "⚠️  psql não encontrado nos caminhos padrão." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Opções:" -ForegroundColor Yellow
    Write-Host "1. Use o DBeaver para testar a conexão" -ForegroundColor White
    Write-Host "2. Instale o PostgreSQL client tools" -ForegroundColor White
    Write-Host ""
    Write-Host "Ou edite manualmente o application.properties com a senha correta." -ForegroundColor Cyan
    exit 0
}

Write-Host "✅ psql encontrado: $psqlPath" -ForegroundColor Green
Write-Host ""

# Pedir senha
$senha = Read-Host "Digite a senha do PostgreSQL (ou deixe vazio para tentar sem senha)" -AsSecureString
$senhaPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($senha)
)

if ([string]::IsNullOrEmpty($senhaPlain)) {
    Write-Host "Tentando conectar sem senha..." -ForegroundColor Yellow
    $env:PGPASSWORD = ""
} else {
    $env:PGPASSWORD = $senhaPlain
}

# Testar conexão
Write-Host ""
Write-Host "Testando conexão..." -ForegroundColor Yellow

try {
    $result = & $psqlPath -h $host -p $port -U $username -d $database -c "SELECT version();" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ CONEXÃO BEM-SUCEDIDA!" -ForegroundColor Green
        Write-Host ""
        Write-Host "A senha que você digitou está correta!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Próximos passos:" -ForegroundColor Cyan
        Write-Host "1. Edite: src\main\resources\application.properties" -ForegroundColor White
        Write-Host "2. Altere a linha:" -ForegroundColor White
        Write-Host "   spring.datasource.password=SUA_SENHA_AQUI" -ForegroundColor Yellow
        Write-Host "   Para:" -ForegroundColor White
        Write-Host "   spring.datasource.password=$senhaPlain" -ForegroundColor Green
        Write-Host ""
        Write-Host "3. Recompile: .\mvnw.cmd clean install -DskipTests" -ForegroundColor Cyan
        Write-Host "4. Execute: java -jar target\sos-rota-0.0.1-SNAPSHOT.jar" -ForegroundColor Cyan
    } else {
        Write-Host ""
        Write-Host "❌ FALHA NA CONEXÃO" -ForegroundColor Red
        Write-Host ""
        Write-Host "Possíveis causas:" -ForegroundColor Yellow
        Write-Host "- Senha incorreta" -ForegroundColor White
        Write-Host "- Banco de dados '$database' não existe" -ForegroundColor White
        Write-Host "- PostgreSQL não está rodando" -ForegroundColor White
        Write-Host ""
        Write-Host "Tente novamente ou consulte CONFIGURAR_SENHA.md" -ForegroundColor Cyan
    }
} catch {
    Write-Host ""
    Write-Host "❌ Erro ao testar conexão: $_" -ForegroundColor Red
}

# Limpar senha da memória
$env:PGPASSWORD = ""
$senhaPlain = $null

