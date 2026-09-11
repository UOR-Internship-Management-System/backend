#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_PATH="${1:-$DIR/cv-generation.local.postman_environment.json}"
COLLECTION="$DIR/cv-generation.postman_collection.json"
REPORT_DIR="$DIR/reports"
command -v newman >/dev/null || { echo 'Install pinned Newman: npm install --global newman@6.2.2' >&2; exit 1; }
[[ -f "$ENV_PATH" ]] || { echo "Missing local environment: $ENV_PATH" >&2; exit 1; }
mkdir -p "$REPORT_DIR"
TS="$(date +%Y%m%d-%H%M%S)"
newman run "$COLLECTION" --environment "$ENV_PATH" --reporters cli,json,junit \
  --reporter-json-export "$REPORT_DIR/cv-generation-$TS.json" \
  --reporter-junit-export "$REPORT_DIR/cv-generation-$TS.xml" \
  --timeout-request 30000 --timeout-script 5000
