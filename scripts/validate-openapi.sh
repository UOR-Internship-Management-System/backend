#!/usr/bin/env sh
set -eu
PATH="/usr/bin:$PATH"

# Validate that the canonical OpenAPI v1.6.0 documentation/runtime copies stay synchronized.
DOCS_FILE="docs/api/CV_Management_API_OpenAPI_v1.6.0.yaml"
RESOURCES_FILE="src/main/resources/openapi/CV_Management_API_OpenAPI_v1.6.0.yaml"

if [ ! -f "$DOCS_FILE" ]; then
  echo "ERROR: $DOCS_FILE does not exist."
  exit 1
fi

if [ ! -f "$RESOURCES_FILE" ]; then
  echo "ERROR: $RESOURCES_FILE does not exist."
  exit 1
fi

if ! cmp -s "$DOCS_FILE" "$RESOURCES_FILE"; then
  echo "ERROR: OpenAPI files are out of sync."
  echo "  $DOCS_FILE"
  echo "  $RESOURCES_FILE"
  echo "Run: cp $DOCS_FILE $RESOURCES_FILE"
  exit 1
fi

if ! tr -d '\r' < "$DOCS_FILE" | grep -q '^openapi: 3\.1\.1$'; then
  echo "ERROR: canonical contract must use OpenAPI 3.1.1."
  exit 1
fi

if ! tr -d '\r' < "$DOCS_FILE" | grep -q '^  version: 1\.6\.0$'; then
  echo "ERROR: canonical contract info.version must be 1.6.0."
  exit 1
fi

echo "OK: OpenAPI v1.6.0 files exist and are synchronised."
