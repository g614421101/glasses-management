# sync-frontend.ps1
# Build the Vue frontend and copy the generated assets into backend static dirs.

param(
    [ValidateSet('All', 'MySQL', 'H2')]
    [string]$Backend = 'All',

    [ValidateSet('Vue', 'React')]
    [string]$Frontend = 'Vue',

    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'

# Only show interactive menu when running standalone (not called by other scripts)
if (-not $MyInvocation.BoundParameters.ContainsKey('Frontend')) {
    Write-Host "=== Frontend Sync ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Select frontend:"
    Write-Host "  [1] Vue   (default)"
    Write-Host "  [2] React"
    Write-Host ""
    $choice = Read-Host "Enter choice (1 or 2, press Enter for default)"
    $Frontend = if ($choice -eq '2') { 'React' } else { 'Vue' }
    Write-Host "Using $Frontend frontend." -ForegroundColor Green
    Write-Host ""
}

$rootDir = $PSScriptRoot
$frontendDir = if ($Frontend -eq 'Vue') { Join-Path $rootDir 'glasses-management-frontend-vue' } else { Join-Path $rootDir 'glasses-management-frontend-react' }
$distDir = Join-Path $frontendDir 'dist'

function Assert-Success {
    param([string]$Message)
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: $Message" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

function Copy-Frontend {
    param(
        [string]$Name,
        [string]$BackendDir
    )

    $resourceStaticDir = Join-Path $BackendDir 'src\main\resources\static'
    $classesDir = Join-Path $BackendDir 'target\classes'
    $classesStaticDir = Join-Path $classesDir 'static'

    Write-Host "Copying frontend resources to $Name backend static dir..." -ForegroundColor Cyan
    Remove-Item (Join-Path $resourceStaticDir '*') -Recurse -Force -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force -Path $resourceStaticDir | Out-Null
    Copy-Item -Path (Join-Path $distDir '*') -Destination $resourceStaticDir -Recurse -Force

    if (Test-Path $classesDir) {
        Write-Host "Copying frontend resources to $Name target classes static dir..." -ForegroundColor Cyan
        Remove-Item (Join-Path $classesStaticDir '*') -Recurse -Force -ErrorAction SilentlyContinue
        New-Item -ItemType Directory -Force -Path $classesStaticDir | Out-Null
        Copy-Item -Path (Join-Path $distDir '*') -Destination $classesStaticDir -Recurse -Force
    }
}

if (-not $SkipBuild) {
    Write-Host "[1/2] Building $Frontend Frontend..." -ForegroundColor Cyan
    Push-Location $frontendDir
    if (-not (Test-Path 'node_modules')) {
        Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
        npm install
        Assert-Success 'Frontend dependency install failed.'
    }
    npm run build
    Assert-Success 'Frontend build failed.'
    Pop-Location
} else {
    Write-Host "[1/2] Skipping frontend build..." -ForegroundColor Yellow
}

if (-not (Test-Path (Join-Path $distDir 'index.html'))) {
    Write-Host "Error: Frontend dist not found. Run without -SkipBuild first." -ForegroundColor Red
    exit 1
}

Write-Host "[2/2] Syncing frontend resources..." -ForegroundColor Cyan

if ($Backend -eq 'All' -or $Backend -eq 'MySQL') {
    Copy-Frontend -Name 'MySQL' -BackendDir (Join-Path $rootDir 'glasses-management-backend')
}

if ($Backend -eq 'All' -or $Backend -eq 'H2') {
    Copy-Frontend -Name 'H2' -BackendDir (Join-Path $rootDir 'glasses-management-backend-h2')
}

Write-Host ''
Write-Host "Success! Frontend resources synced. Backend: $Backend" -ForegroundColor Green
