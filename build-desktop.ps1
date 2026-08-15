# build-desktop.ps1
# Glasses Management System - Desktop Packaging Script (H2)

$ErrorActionPreference = 'Stop'

Write-Host "=== Glasses Management Desktop Builder ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Select frontend to package:"
Write-Host "  [1] Vue   (default)"
Write-Host "  [2] React"
Write-Host ""
$choice = Read-Host "Enter choice (1 or 2, press Enter for default)"
$Frontend = if ($choice -eq '2') { 'React' } else { 'Vue' }
Write-Host "Using $Frontend frontend." -ForegroundColor Green
Write-Host ""

# Step 1: Build and sync Frontend
Write-Host "[1/4] Building and syncing $Frontend Frontend..." -ForegroundColor Cyan
& "$PSScriptRoot\sync-frontend.ps1" -Backend H2 -Frontend $Frontend
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Frontend sync failed." -ForegroundColor Red; exit }

# Step 1.5: Prepare packaged config (统一管理员策略：不再强制预置 application-local.yml)
# 本地存在 application-local.yml 时原样带入安装包（私有分发场景）；
# 不存在时生成仅含注释的占位文件 —— 首次启动在登录页引导完成管理员初始化。
$h2LocalConfig = Join-Path $PSScriptRoot 'glasses-management-backend-h2\application-local.yml'
$packagingConfigDir = Join-Path $PSScriptRoot 'glasses-management-electron\packaging-config'
$packagedConfig = Join-Path $packagingConfigDir 'application-local.yml'
New-Item -ItemType Directory -Force -Path $packagingConfigDir | Out-Null
if (Test-Path $h2LocalConfig) {
    Copy-Item -LiteralPath $h2LocalConfig -Destination $packagedConfig -Force
    Write-Host "[config] 已携带本地 glasses-management-backend-h2\application-local.yml 进入安装包（含其中预置的邀请码/管理员密码，请确认仅用于私有分发）。" -ForegroundColor Yellow
} else {
    @(
        '# 本安装包未预置本地配置：',
        '# 首次启动时登录页会引导完成管理员初始化（需邀请码），管理员密码在初始化时现场设置。'
    ) | Set-Content -Path $packagedConfig -Encoding UTF8
    Write-Host "[config] 未找到 application-local.yml，安装包将采用首次启动引导初始化。" -ForegroundColor Green
}

# Step 2: Build Spring Boot JAR
Write-Host "[2/4] Building Spring Boot Backend (Maven)..." -ForegroundColor Cyan
cd "$PSScriptRoot\glasses-management-backend-h2"
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Backend build failed." -ForegroundColor Red; exit }
cd $PSScriptRoot

# Step 3: Build bundled Java runtime for Electron
Write-Host "[3/4] Preparing bundled Java runtime..." -ForegroundColor Cyan
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
    Write-Host "Error: jlink.exe not found. Install JDK 21 or set JAVA_HOME." -ForegroundColor 1Red
    exit
}

& $jlink --add-modules ALL-MODULE-PATH --strip-debug --no-man-pages --no-header-files --compress=2 --output $runtimeDir
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Java runtime build failed." -ForegroundColor Red; exit }

# Step 4: Build Electron App
Write-Host "[4/4] Building Electron Desktop App..." -ForegroundColor Cyan
cd "$PSScriptRoot\glasses-management-electron"

Write-Host "Checking Node dependencies..." -ForegroundColor Yellow
npm install
if ($LASTEXITCODE -ne 0) { Write-Host "Error: npm install failed." -ForegroundColor Red; exit }

Write-Host "Starting electron-builder (this may take a minute)..." -ForegroundColor Cyan

# Set mirrors for Electron and electron-builder to avoid GitHub connection resets in China
$env:ELECTRON_MIRROR="https://npmmirror.com/mirrors/electron/"
$env:ELECTRON_BUILDER_BINARIES_MIRROR="https://npmmirror.com/mirrors/electron-builder-binaries/"

npm run build
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Electron build failed." -ForegroundColor Red; exit }

cd $PSScriptRoot
Write-Host ""
Write-Host "Success! Build complete." -ForegroundColor Green
Write-Host "The installer (.exe) is located in: glasses-management-electron\dist\" -ForegroundColor Green
