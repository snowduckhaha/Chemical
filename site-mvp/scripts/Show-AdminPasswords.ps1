<#
.SYNOPSIS
    Query admin_user table and show known password mappings.
.EXAMPLE
    .\scripts\Show-AdminPasswords.ps1
.EXAMPLE
    .\scripts\Show-AdminPasswords.ps1 -Username admin
#>
[CmdletBinding()]
param(
    [string]$Username
)

$ErrorActionPreference = 'Stop'

# Known password mappings (update when passwords are changed)
$knownPasswords = @{
    'admin'    = 'rost*2026@'
    'operator' = 'user*2026#'
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Admin Account Password Reference" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($Username) {
    $users = @($Username)
}
else {
    $users = @('admin', 'operator')
}

foreach ($user in $users) {
    $known = $knownPasswords[$user]
    if ($known) {
        Write-Host "  Username: $user" -ForegroundColor White
        Write-Host "  Password: $known" -ForegroundColor Green
        Write-Host ""
    }
    else {
        Write-Host "  Username: $user" -ForegroundColor White
        Write-Host "  Password: [Unknown - not in mapping]" -ForegroundColor Yellow
        Write-Host ""
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  To verify a password against a hash:" -ForegroundColor Gray
Write-Host "  .\scripts\Test-BcryptPassword.ps1 -Password 'xxx' -Hash 'yyy'" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
