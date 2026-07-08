#!/usr/bin/env sh
set -eu

# Validate that both OpenAPI YAML files exist and are synchronised.

DOCS_FILE="docs/api/CV_Management_API_OpenAPI_v1.0.yaml"
RESOURCES_FILE="src/main/resources/openapi/CV_Management_API_OpenAPI_v1.0.yaml"

if [ ! -f "$DOCS_FILE" ]; then
  echo "ERROR: $DOCS_FILE does not exist."
  exit 1
fi

if [ ! -f "$RESOURCES_FILE" ]; then
  echo "ERROR: $RESOURCES_FILE does not exist."
  exit 1
fi

if ! diff -q "$DOCS_FILE" "$RESOURCES_FILE" > /dev/null 2>&1; then
  echo "ERROR: OpenAPI files are out of sync."
  echo "  $DOCS_FILE"
  echo "  $RESOURCES_FILE"
  echo "Run: cp $DOCS_FILE $RESOURCES_FILE"
  exit 1
fi

echo "OK: OpenAPI files exist and are synchronised."
