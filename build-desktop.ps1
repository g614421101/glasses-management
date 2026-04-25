# build-desktop.ps1
# Glasses Management System - Desktop Packaging Script (H2)

# Step 1: Build Frontend
Write-Host "[1/5] Building Frontend..." -ForegroundColor Cyan
cd glasses-management-frontend
npm run build
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Frontend build failed." -ForegroundColor Red; exit }
cd ..

# Step 2: Copy static resources to Backend
Write-Host "[2/5] Copying frontend resources to Spring Boot static dir..." -ForegroundColor Cyan
Remove-Item 'glasses-management-backend-h2\src\main\resources\static\*' -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path 'glasses-management-backend-h2\src\main\resources\static' -ErrorAction SilentlyContinue
Copy-Item -Path 'glasses-management-frontend\dist\*' -Destination 'glasses-management-backend-h2\src\main\resources\static' -Recurse -Force

# Step 3: Build Spring Boot JAR
Write-Host "[3/5] Building Spring Boot Backend (Maven)..." -ForegroundColor Cyan
cd glasses-management-backend-h2
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Backend build failed." -ForegroundColor Red; exit }
cd ..

# Step 4: Build bundled Java runtime for Electron
Write-Host "[4/5] Preparing bundled Java runtime..." -ForegroundColor Cyan
$runtimeDir = 'glasses-management-electron\runtime\jre'
$electronDistDir = 'glasses-management-electron\dist'
Remove-Item $runtimeDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item $electronDistDir -Recurse -Force -ErrorAction SilentlyContinue

$jlinkCandidates = @()
if ($env:JAVA_HOME) {
    $jlinkCandidates += (Join-Path $env:JAVA_HOME 'bin\jlink.exe')
}
$jlinkCommand = Get-Command jlink.exe -ErrorAction SilentlyContinue
if ($jlinkCommand) {
    $jlinkCandidates += $jlinkCommand.Source
}
$jlinkCandidates += 'C:\Software\Java\jdk-21\bin\jlink.exe'

$jlink = $jlinkCandidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
if (-not $jlink) {
    Write-Host "Error: jlink.exe not found. Install JDK 21 or set JAVA_HOME." -ForegroundColor Red
    exit
}

& $jlink --add-modules ALL-MODULE-PATH --strip-debug --no-man-pages --no-header-files --compress=2 --output $runtimeDir
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Java runtime build failed." -ForegroundColor Red; exit }

# Step 5: Build Electron App
Write-Host "[5/5] Building Electron Desktop App..." -ForegroundColor Cyan
cd glasses-management-electron

Write-Host "Checking Node dependencies..." -ForegroundColor Yellow
npm install
if ($LASTEXITCODE -ne 0) { Write-Host "Error: npm install failed." -ForegroundColor Red; exit }

Write-Host "Starting electron-builder (this may take a minute)..." -ForegroundColor Cyan

# Set mirrors for Electron and electron-builder to avoid GitHub connection resets in China
$env:ELECTRON_MIRROR="https://npmmirror.com/mirrors/electron/"
$env:ELECTRON_BUILDER_BINARIES_MIRROR="https://npmmirror.com/mirrors/electron-builder-binaries/"

npm run build
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Electron build failed." -ForegroundColor Red; exit }

cd ..
Write-Host ""
Write-Host "Success! Build complete." -ForegroundColor Green
Write-Host "The installer (.exe) is located in: glasses-management-electron\dist\" -ForegroundColor Green
