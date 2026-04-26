# glasses-management-backend-h2/build-package.ps1
# Build the H2 Spring Boot native Windows installer.

$ErrorActionPreference = 'Stop'

$backendDir = $PSScriptRoot
$rootDir = Split-Path -Parent $backendDir
$frontendDir = Join-Path $rootDir 'glasses-management-frontend'
$staticDir = Join-Path $backendDir 'src\main\resources\static'
$tempDir = Join-Path $backendDir 'jpackage-temp'
$distDir = Join-Path $backendDir 'dist-install'
$jarName = 'glasses-management-backend-h2-0.0.1-SNAPSHOT.jar'
$jarPath = Join-Path $backendDir "target\$jarName"

function Assert-Success {
    param([string]$Message)
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: $Message" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

function Resolve-Jpackage {
    $candidates = @()
    if ($env:JAVA_HOME) {
        $candidates += (Join-Path $env:JAVA_HOME 'bin\jpackage.exe')
    }

    $command = Get-Command jpackage.exe -ErrorAction SilentlyContinue
    if ($command) {
        $candidates += $command.Source
    }

    $candidates += 'C:\Software\Java\jdk-21\bin\jpackage.exe'

    return $candidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
}

Write-Host "[1/5] Building Frontend..." -ForegroundColor Cyan
Push-Location $frontendDir
if (-not (Test-Path 'node_modules')) {
    Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
    npm install
    Assert-Success 'Frontend dependency install failed.'
}
npm run build
Assert-Success 'Frontend build failed.'
Pop-Location

Write-Host "[2/5] Copying frontend resources to H2 backend static dir..." -ForegroundColor Cyan
Remove-Item (Join-Path $staticDir '*') -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $staticDir | Out-Null
Copy-Item -Path (Join-Path $frontendDir 'dist\*') -Destination $staticDir -Recurse -Force

Write-Host "[3/5] Building H2 Spring Boot Backend (Maven)..." -ForegroundColor Cyan
Push-Location $backendDir
mvn clean package -DskipTests
Assert-Success 'Backend build failed.'
Pop-Location

if (-not (Test-Path $jarPath)) {
    Write-Host "Error: Built jar not found: $jarPath" -ForegroundColor Red
    exit 1
}

Write-Host "[4/5] Preparing jpackage input..." -ForegroundColor Cyan
Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item $distDir -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
Copy-Item -LiteralPath $jarPath -Destination $tempDir -Force

Write-Host "[5/5] Building H2 native installer..." -ForegroundColor Cyan
$jpackage = Resolve-Jpackage
if (-not $jpackage) {
    Write-Host 'Error: jpackage.exe not found. Install JDK 21 or set JAVA_HOME.' -ForegroundColor Red
    exit 1
}

$wixPath = 'C:\Program Files (x86)\WiX Toolset v3.14\bin'
if (Test-Path $wixPath) {
    $env:Path = "$wixPath;$env:Path"
}

Push-Location $backendDir
& $jpackage '@jpackage.cfg'
Assert-Success 'jpackage build failed.'
Pop-Location

Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue

Write-Host ''
Write-Host 'Success! H2 native installer build complete.' -ForegroundColor Green
Write-Host 'The installer (.exe) is located in: glasses-management-backend-h2\dist-install\' -ForegroundColor Green
