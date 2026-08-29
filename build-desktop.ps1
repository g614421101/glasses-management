# build-desktop.ps1
# Glasses Management System - Desktop Packaging Script (H2 / SQLite / MySQL)

param(
    [ValidateSet('H2', 'SQLite', 'MySQL')]
    [string]$Backend = '',

    [ValidateSet('Vue', 'React')]
    [string]$Frontend = ''
)

$ErrorActionPreference = 'Stop'

Write-Host "=== Glasses Management Desktop Builder ===" -ForegroundColor Cyan
Write-Host ""

if (-not $Backend) {
    Write-Host "Select backend to package:"
    Write-Host "  [1] H2     (default)"
    Write-Host "  [2] SQLite"
    Write-Host "  [3] MySQL"
    Write-Host ""
    $choice = Read-Host "Enter choice (1/2/3, press Enter for default)"
    $Backend = if ($choice -eq '2') { 'SQLite' } elseif ($choice -eq '3') { 'MySQL' } else { 'H2' }
    Write-Host "Using $Backend backend." -ForegroundColor Green
    Write-Host ""
}

if (-not $Frontend) {
    Write-Host "Select frontend to package:"
    Write-Host "  [1] Vue   (default)"
    Write-Host "  [2] React"
    Write-Host ""
    $choice = Read-Host "Enter choice (1 or 2, press Enter for default)"
    $Frontend = if ($choice -eq '2') { 'React' } else { 'Vue' }
    Write-Host "Using $Frontend frontend." -ForegroundColor Green
    Write-Host ""
}

# 所选后端对应的模块目录（MySQL 版模块目录名无后缀）
$backendDirName = if ($Backend -eq 'H2') { 'glasses-management-backend-h2' }
    elseif ($Backend -eq 'SQLite') { 'glasses-management-backend-sqlite' }
    else { 'glasses-management-backend' }
$backendDir = Join-Path $PSScriptRoot $backendDirName

if ($Backend -eq 'MySQL') {
    Write-Host "[hint] MySQL 版需连接外部 MySQL 数据库：首次启动前需配置数据源（打包时携带 ${backendDirName}\application-local.yml，或安装后在 %APPDATA%\视光管理系统\application-local.yml 配置）。" -ForegroundColor Yellow
    Write-Host ""
}

# Step 1: Build and sync Frontend
Write-Host "[1/4] Building and syncing $Frontend Frontend to $Backend backend..." -ForegroundColor Cyan
& "$PSScriptRoot\sync-frontend.ps1" -Backend $Backend -Frontend $Frontend
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Frontend sync failed." -ForegroundColor Red; exit }

# Step 1.5: Prepare packaged config (统一管理员策略：不再强制预置 application-local.yml)
# 本地存在 application-local.yml 时原样带入安装包（私有分发场景）；
# 不存在时生成仅含注释的占位文件 —— H2/SQLite 首次启动在登录页引导完成管理员初始化；
# MySQL 无默认数据源，占位文件附数据源模板，需在首次启动前完成配置。
$localConfig = Join-Path $backendDir 'application-local.yml'
$packagingConfigDir = Join-Path $PSScriptRoot 'glasses-management-electron\packaging-config'
$packagedConfig = Join-Path $packagingConfigDir 'application-local.yml'
New-Item -ItemType Directory -Force -Path $packagingConfigDir | Out-Null
if (Test-Path $localConfig) {
    Copy-Item -LiteralPath $localConfig -Destination $packagedConfig -Force
    Write-Host "[config] 已携带本地 ${backendDirName}\application-local.yml 进入安装包（含其中预置的邀请码/管理员密码/数据源，请确认仅用于私有分发）。" -ForegroundColor Yellow
} elseif ($Backend -eq 'MySQL') {
    @(
        '# 本安装包未预置本地配置（MySQL 版）：',
        '# MySQL 版首次启动前必须配置数据源，否则后端无法启动。',
        '# 将下方模板填写完整后，保存为以下任一位置（userData 下的配置优先生效）：',
        '#   - %APPDATA%\视光管理系统\application-local.yml（推荐）',
        '#   - 安装目录 resources\backend\application-local.yml',
        '#',
        '# app:',
        '#   invite-code: 你的邀请码',
        '# spring:',
        '#   datasource:',
        '#     url: jdbc:mysql://<主机>:<端口>/<数据库名>?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai',
        '#     username: <数据库用户名>',
        '#     password: <数据库密码>'
    ) | Set-Content -Path $packagedConfig -Encoding UTF8
    Write-Host "[config] 未找到 application-local.yml，MySQL 版安装包需在首次启动前配置数据源（模板见 %APPDATA%\视光管理系统\application-local.yml）。" -ForegroundColor Green
} else {
    @(
        '# 本安装包未预置本地配置：',
        '# 首次启动时登录页会引导完成管理员初始化（需邀请码），管理员密码在初始化时现场设置。'
    ) | Set-Content -Path $packagedConfig -Encoding UTF8
    Write-Host "[config] 未找到 application-local.yml，安装包将采用首次启动引导初始化。" -ForegroundColor Green
}

# Step 2: Build Spring Boot JAR and stage it for Electron packaging
Write-Host "[2/4] Building $Backend Spring Boot Backend (Maven)..." -ForegroundColor Cyan
cd $backendDir
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Host "Error: Backend build failed." -ForegroundColor Red; exit }
cd $PSScriptRoot

# 把所选后端的 JAR 暂存到 electron\backend-jar（清空后仅保留一个 JAR，保证打包时确定性拷贝）
$stagingDir = Join-Path $PSScriptRoot 'glasses-management-electron\backend-jar'
$jarCandidates = @(Get-ChildItem -Path (Join-Path $backendDir 'target') -Filter '*.jar' |
    Where-Object { $_.Name -notlike '*.original' } |
    Sort-Object LastWriteTime -Descending)
if ($jarCandidates.Count -eq 0) {
    Write-Host "Error: Backend JAR not found in $backendDir\target." -ForegroundColor Red
    exit
}
if ($jarCandidates.Count -gt 1) {
    Write-Host "[jar] target 下存在多个 JAR，取最新的: $($jarCandidates[0].Name)" -ForegroundColor Yellow
}
Remove-Item $stagingDir -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $stagingDir | Out-Null
Copy-Item -LiteralPath $jarCandidates[0].FullName -Destination $stagingDir -Force
Write-Host "[jar] 已暂存 $($jarCandidates[0].Name) -> glasses-management-electron\backend-jar\" -ForegroundColor Green

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
    Write-Host "Error: jlink.exe not found. Install JDK 21 or set JAVA_HOME." -ForegroundColor Red
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

# 产物名带后端标识，避免 H2/SQLite/MySQL 安装包同名混淆
$electronPkg = Get-Content -Raw -Path (Join-Path $PSScriptRoot 'glasses-management-electron\package.json') | ConvertFrom-Json
$appVersion = $electronPkg.version
$defaultArtifact = Join-Path $PSScriptRoot "glasses-management-electron\dist\视光管理系统_${appVersion}.exe"
$finalArtifact = Join-Path $PSScriptRoot "glasses-management-electron\dist\视光管理系统_${Backend}_${appVersion}.exe"
if (Test-Path -LiteralPath $defaultArtifact) {
    Move-Item -LiteralPath $defaultArtifact -Destination $finalArtifact -Force
    # blockmap 是安装包的差分更新哈希文件，一并重命名保持一致
    $defaultBlockmap = "$defaultArtifact.blockmap"
    if (Test-Path -LiteralPath $defaultBlockmap) {
        Move-Item -LiteralPath $defaultBlockmap -Destination "$finalArtifact.blockmap" -Force
    }
} elseif (-not (Test-Path -LiteralPath $finalArtifact)) {
    Write-Host "[warn] 未找到预期安装包产物（视光管理系统_${appVersion}.exe），请检查 glasses-management-electron\dist 目录。" -ForegroundColor Yellow
}

cd $PSScriptRoot
Write-Host ""
Write-Host "Success! Build complete ($Backend backend)." -ForegroundColor Green
Write-Host "The installer (.exe) is located in: glasses-management-electron\dist\视光管理系统_${Backend}_${appVersion}.exe" -ForegroundColor Green
