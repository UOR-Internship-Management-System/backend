#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ENVIRONMENT_PATH=${1:-"$SCRIPT_DIR/company-internship.local.postman_environment.json"}
COLLECTION_PATH="$SCRIPT_DIR/company-internship.postman_collection.json"
REPORT_DIRECTORY="$SCRIPT_DIR/reports"

if ! command -v newman >/dev/null 2>&1; then
  echo "Newman is not installed or not on PATH. Install: npm install --global newman@6.2.2" >&2
  exit 2
fi
if [ ! -f "$COLLECTION_PATH" ]; then
  echo "Postman collection not found: $COLLECTION_PATH" >&2
  exit 2
fi
if [ ! -f "$ENVIRONMENT_PATH" ]; then
  echo "Local Postman environment not found: $ENVIRONMENT_PATH" >&2
  exit 2
fi

mkdir -p "$REPORT_DIRECTORY"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
JSON_REPORT="$REPORT_DIRECTORY/company-internship-$TIMESTAMP.json"
JUNIT_REPORT="$REPORT_DIRECTORY/company-internship-$TIMESTAMP.xml"

newman run "$COLLECTION_PATH" \
  --environment "$ENVIRONMENT_PATH" \
  --reporters cli,json,junit \
  --reporter-json-export "$JSON_REPORT" \
  --reporter-junit-export "$JUNIT_REPORT" \
  --timeout-request 15000 \
  --timeout-script 5000

echo "Acceptance passed. JSON: $JSON_REPORT"
echo "Acceptance passed. JUnit: $JUNIT_REPORT"
