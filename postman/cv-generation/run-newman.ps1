[CmdletBinding()]
param(
    [string]$EnvironmentPath = "$PSScriptRoot\cv-generation.local.postman_environment.json",
    [string]$CollectionPath = "$PSScriptRoot\cv-generation.postman_collection.json",
    [string]$ReportDirectory = "$PSScriptRoot\reports"
)
$ErrorActionPreference = 'Stop'
if (-not (Get-Command newman -ErrorAction SilentlyContinue)) {
    throw 'Newman is not installed or not on PATH. Install the pinned acceptance version with: npm install --global newman@6.2.2'
}
if (-not (Test-Path -LiteralPath $EnvironmentPath)) { throw "Local environment not found: $EnvironmentPath" }
New-Item -ItemType Directory -Force -Path $ReportDirectory | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$jsonReport = Join-Path $ReportDirectory "cv-generation-$timestamp.json"
$junitReport = Join-Path $ReportDirectory "cv-generation-$timestamp.xml"
& newman run $CollectionPath --environment $EnvironmentPath --reporters "cli,json,junit" --reporter-json-export $jsonReport --reporter-junit-export $junitReport --timeout-request 30000 --timeout-script 5000
if ($LASTEXITCODE -ne 0) { throw "BMD-007 Newman acceptance failed with exit code $LASTEXITCODE." }
Write-Host "BMD-007 acceptance passed. JSON: $jsonReport"
Write-Host "BMD-007 acceptance passed. JUnit: $junitReport"
