#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENVIRONMENT_PATH="${1:-$SCRIPT_DIR/shortlist-management.local.postman_environment.json}"
REPORT_DIRECTORY="${2:-$SCRIPT_DIR/reports}"
command -v newman >/dev/null 2>&1 || { echo 'Install pinned Newman: npm install --global newman@6.2.2' >&2; exit 1; }
test -f "$ENVIRONMENT_PATH" || { echo "Local environment not found: $ENVIRONMENT_PATH" >&2; exit 1; }
mkdir -p "$REPORT_DIRECTORY"
timestamp="$(date +%Y%m%d-%H%M%S)"
newman run "$SCRIPT_DIR/shortlist-management.postman_collection.json" \
  --environment "$ENVIRONMENT_PATH" \
  --reporters cli,json,junit \
  --reporter-json-export "$REPORT_DIRECTORY/shortlist-management-$timestamp.json" \
  --reporter-junit-export "$REPORT_DIRECTORY/shortlist-management-$timestamp.xml" \
  --delay-request 250 --timeout-request 30000 --timeout-script 5000
