[CmdletBinding()]
param(
    [string]$EnvironmentPath = "$PSScriptRoot\company-internship.local.postman_environment.json",
    [string]$CollectionPath = "$PSScriptRoot\company-internship.postman_collection.json",
    [string]$ReportDirectory = "$PSScriptRoot\reports"
)

$ErrorActionPreference = 'Stop'

if (-not (Get-Command newman -ErrorAction SilentlyContinue)) {
    throw 'Newman is not installed or not on PATH. Install the pinned acceptance version with: npm install --global newman@6.2.2'
}
if (-not (Test-Path -LiteralPath $CollectionPath)) {
    throw "Postman collection not found: $CollectionPath"
}
if (-not (Test-Path -LiteralPath $EnvironmentPath)) {
    throw "Local Postman environment not found: $EnvironmentPath. Copy the committed template and fill local credentials/Skill UUIDs."
}

New-Item -ItemType Directory -Force -Path $ReportDirectory | Out-Null
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$jsonReport = Join-Path $ReportDirectory "company-internship-$timestamp.json"
$junitReport = Join-Path $ReportDirectory "company-internship-$timestamp.xml"

Write-Host "Running Company/Internship acceptance against environment: $EnvironmentPath"
& newman run $CollectionPath `
    --environment $EnvironmentPath `
    --reporters "cli,json,junit" `
    --reporter-json-export $jsonReport `
    --reporter-junit-export $junitReport `
    --timeout-request 15000 `
    --timeout-script 5000

if ($LASTEXITCODE -ne 0) {
    throw "Newman acceptance failed with exit code $LASTEXITCODE. Inspect $jsonReport and $junitReport."
}

Write-Host "Acceptance passed. JSON: $jsonReport"
Write-Host "Acceptance passed. JUnit: $junitReport"
