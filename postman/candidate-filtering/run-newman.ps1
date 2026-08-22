[CmdletBinding()]
param(
    [string]$EnvironmentPath = "$PSScriptRoot\candidate-filtering.local.postman_environment.json",
    [string]$CollectionPath = "$PSScriptRoot\candidate-filtering.postman_collection.json",
    [string]$ReportDirectory = "$PSScriptRoot\reports"
)

$ErrorActionPreference = 'Stop'
if (-not (Get-Command newman -ErrorAction SilentlyContinue)) {
    throw 'Newman is not installed or not on PATH. Install the pinned version with: npm install --global newman@6.2.2'
}
if (-not (Test-Path -LiteralPath $CollectionPath)) { throw "Collection not found: $CollectionPath" }
if (-not (Test-Path -LiteralPath $EnvironmentPath)) { throw "Local environment not found: $EnvironmentPath" }

New-Item -ItemType Directory -Force -Path $ReportDirectory | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$jsonReport = Join-Path $ReportDirectory "candidate-filtering-$timestamp.json"
$junitReport = Join-Path $ReportDirectory "candidate-filtering-$timestamp.xml"

& newman run $CollectionPath `
    --environment $EnvironmentPath `
    --reporters "cli,json,junit" `
    --reporter-json-export $jsonReport `
    --reporter-junit-export $junitReport `
    --delay-request 100 `
    --timeout-request 30000 `
    --timeout-script 5000

if ($LASTEXITCODE -ne 0) { throw "BMD-010 Newman acceptance failed with exit code $LASTEXITCODE." }
Write-Host "BMD-010 acceptance passed. JSON: $jsonReport"
Write-Host "BMD-010 acceptance passed. JUnit: $junitReport"
