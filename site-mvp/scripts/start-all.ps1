$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host '[1/4] Starting MySQL container...'
docker compose -f "$root\docker-compose.mysql.yml" up -d

Write-Host '[2/4] Waiting for MySQL readiness...'
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    $health = (docker inspect --format "{{.State.Health.Status}}" qidian-mysql 2>$null).Trim()
    if ($LASTEXITCODE -eq 0 -and $health -eq 'healthy') {
        $ready = $true
        break
    }
    Start-Sleep -Seconds 2
}
if (-not $ready) {
    throw 'MySQL did not become ready in time.'
}

Write-Host '[3/4] Checking database schema and seed...'
& "$PSScriptRoot\db-check-and-sync.ps1"

Write-Host '[4/4] Starting backend and frontend...'
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root\\backend'; mvn spring-boot:run"
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root\\frontend'; npm run dev"

Write-Host ''
Write-Host 'All services are launching:'
Write-Host '- Frontend: http://localhost:5173/zh'
Write-Host '- Backend:  http://localhost:8080/api/v1/health'
Write-Host '- MySQL:    127.0.0.1:3306 (qidian_site)'
