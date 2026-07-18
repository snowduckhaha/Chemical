$ErrorActionPreference = 'Continue'

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host '[1/3] Stopping backend process(es)...'
$backendJava = Get-CimInstance Win32_Process | Where-Object {
    $_.Name -match '^java(\\.exe)?$' -and
    $_.CommandLine -like '*site-mvp\\backend*' -and
    $_.CommandLine -like '*spring-boot:run*'
}
foreach ($p in $backendJava) {
    Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
}

Write-Host '[2/3] Stopping frontend process(es)...'
$frontendNode = Get-CimInstance Win32_Process | Where-Object {
    $_.Name -match '^node(\\.exe)?$' -and
    $_.CommandLine -like '*site-mvp\\frontend*' -and
    $_.CommandLine -like '*vite*'
}
foreach ($p in $frontendNode) {
    Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
}

Write-Host '[3/3] Stopping MySQL container...'
docker compose -f "$root\docker-compose.mysql.yml" down

Write-Host 'All local services are stopped.'
