[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Za-z0-9._-]{3,128}$')]
    [string]$Username,

    [Parameter(Mandatory = $true)]
    [ValidateSet('ADMIN', 'OPERATOR')]
    [string]$Role,

    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\$2[aby]\$12\$.+')]
    [string]$PasswordHash
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$containerStatus = docker inspect --format '{{.State.Running}}' qidian-mysql 2>$null
if ($LASTEXITCODE -ne 0 -or $containerStatus.Trim() -ne 'true') {
    throw 'qidian-mysql is not running. Start the local services first.'
}

$sql = @"
INSERT INTO admin_user (username, password_hash, role_code, enabled, must_change_password)
VALUES ('$Username', '$PasswordHash', '$Role', 1, 1)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  role_code = VALUES(role_code),
  enabled = 1,
  must_change_password = 1;
"@

docker exec -e MYSQL_PWD=root123456 qidian-mysql mysql -uroot qidian_site -e $sql
if ($LASTEXITCODE -ne 0) {
    throw 'Account provisioning failed.'
}

Write-Host "Account '$Username' was created or updated with role $Role."