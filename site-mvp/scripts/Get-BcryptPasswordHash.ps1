[CmdletBinding()]
param(
    [SecureString]$Password
)

$ErrorActionPreference = 'Stop'

if ($null -eq $Password) {
    $Password = Read-Host 'Enter password (input is hidden)' -AsSecureString
}

$cryptoJar = Get-ChildItem (Join-Path $HOME '.m2\repository\org\springframework\security\spring-security-crypto') -Recurse -Filter 'spring-security-crypto-*.jar' |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName

if ([string]::IsNullOrWhiteSpace($cryptoJar)) {
    throw 'spring-security-crypto dependency was not found. Run mvn test in the backend directory first.'
}

$springJclJar = Get-ChildItem (Join-Path $HOME '.m2\repository\org\springframework\spring-jcl') -Recurse -Filter 'spring-jcl-*.jar' |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName

if ([string]::IsNullOrWhiteSpace($springJclJar)) {
    throw 'spring-jcl dependency was not found. Run mvn test in the backend directory first.'
}

$javaClasspath = "$cryptoJar;$springJclJar"

$tempDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ("qidian-bcrypt-" + [Guid]::NewGuid().ToString('N'))
$sourceFile = Join-Path $tempDirectory 'BcryptHash.java'
New-Item -ItemType Directory -Path $tempDirectory | Out-Null

@'
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptHash {
    public static void main(String[] args) throws Exception {
        String password = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        System.out.print(new BCryptPasswordEncoder(12).encode(password));
    }
}
'@ | ForEach-Object {
    [System.IO.File]::WriteAllText($sourceFile, $_, [System.Text.UTF8Encoding]::new($false))
}

$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password)
try {
    $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    if ([string]::IsNullOrWhiteSpace($plainPassword)) {
        throw 'Password must not be empty.'
    }

    $processInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $processInfo.FileName = 'java'
    $processInfo.Arguments = "--class-path `"$javaClasspath`" `"$sourceFile`""
    $processInfo.UseShellExecute = $false
    $processInfo.RedirectStandardInput = $true
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $processInfo
    $null = $process.Start()
    $process.StandardInput.WriteLine($plainPassword)
    $process.StandardInput.Close()
    $hash = $process.StandardOutput.ReadToEnd().Trim()
    $errorOutput = $process.StandardError.ReadToEnd().Trim()
    $process.WaitForExit()

    if ($process.ExitCode -ne 0 -or -not $hash.StartsWith('$2')) {
        throw "BCrypt hash generation failed. $errorOutput"
    }

    Write-Output $hash
}
finally {
    if ($bstr -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
    Remove-Item -Path $tempDirectory -Recurse -Force -ErrorAction SilentlyContinue
}