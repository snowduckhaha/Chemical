<#
.SYNOPSIS
    Verify if a plain-text password matches a BCrypt-12 hash.
.EXAMPLE
    .\scripts\Test-BcryptPassword.ps1 -Password "rost*2026@" -Hash '$2a$12$uIbVHT6L3xo.P1L3EDpDa.XRk9bvJ8G5iuXrjiDl0wk/qFSuTqohm'
#>
[CmdletBinding()]
param(
    [string]$Password,
    [string]$Hash
)

$ErrorActionPreference = 'Stop'

if (-not $Password) {
    $Password = Read-Host 'Enter plain password'
}
if (-not $Hash) {
    $Hash = Read-Host 'Enter BCrypt hash'
}

$m2Repo = Join-Path $HOME '.m2\repository'
$cryptoJar = Get-ChildItem (Join-Path $m2Repo 'org\springframework\security\spring-security-crypto') -Recurse -Filter 'spring-security-crypto-*.jar' |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $cryptoJar) {
    throw 'spring-security-crypto not found. Run mvn test in backend directory first.'
}

$springJclJar = Get-ChildItem (Join-Path $m2Repo 'org\springframework\spring-jcl') -Recurse -Filter 'spring-jcl-*.jar' |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $springJclJar) {
    throw 'spring-jcl not found. Run mvn test in backend directory first.'
}

$javaClasspath = "$cryptoJar;$springJclJar"

$tempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("bcrypt-verify-" + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $tempDir | Out-Null

$javaSource = @'
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptVerify {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String password = br.readLine();
        String hash = br.readLine();
        if (password == null || hash == null) {
            System.out.println("ERROR: missing input");
            System.exit(1);
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        boolean matches = encoder.matches(password, hash);
        System.out.println("MATCH:" + matches);
    }
}
'@

[System.IO.File]::WriteAllText(
    (Join-Path $tempDir 'BcryptVerify.java'),
    $javaSource,
    [System.Text.UTF8Encoding]::new($false)
)

try {
    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = 'java'
    $psi.Arguments = "--class-path `"$javaClasspath`" `"$tempDir\BcryptVerify.java`""
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true

    $proc = [System.Diagnostics.Process]::new()
    $proc.StartInfo = $psi
    $null = $proc.Start()

    $proc.StandardInput.WriteLine($Password)
    $proc.StandardInput.WriteLine($Hash)
    $proc.StandardInput.Close()

    $output = $proc.StandardOutput.ReadToEnd().Trim()
    $errors = $proc.StandardError.ReadToEnd().Trim()
    $proc.WaitForExit()

    if ($proc.ExitCode -ne 0) {
        Write-Host "Error: $errors" -ForegroundColor Red
        exit 1
    }

    if ($output -match 'MATCH:(true|false)') {
        $matched = $Matches[1] -eq 'true'
        if ($matched) {
            Write-Host "[OK] Password matches!" -ForegroundColor Green
        }
        else {
            Write-Host "[FAIL] Password does not match!" -ForegroundColor Red
        }
    }
    else {
        Write-Host "Unknown output: $output" -ForegroundColor Yellow
    }
}
finally {
    Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
}
