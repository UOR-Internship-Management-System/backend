#!/usr/bin/env sh
set -eu
PATH="/usr/bin:$PATH"

# Validate that both OpenAPI YAML files exist and are synchronised.

DOCS_FILE="docs/api/CV_Management_API_OpenAPI_v1.1.yaml"
RESOURCES_FILE="src/main/resources/openapi/CV_Management_API_OpenAPI_v1.1.yaml"

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

echo "OK: OpenAPI files exist and are synchronised."
