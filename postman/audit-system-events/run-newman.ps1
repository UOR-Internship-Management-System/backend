$ErrorActionPreference = "Stop"
$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$environmentFile = Join-Path $scriptRoot "audit-system-events.local.postman_environment.json"

if (-not (Test-Path -LiteralPath $environmentFile)) {
    throw "Create audit-system-events.local.postman_environment.json from the template and supply local credentials."
}

npx --yes newman@6.2.2 run `
    (Join-Path $scriptRoot "audit-system-events.postman_collection.json") `
    --environment $environmentFile `
    --bail
