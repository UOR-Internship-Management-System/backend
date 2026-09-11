#!/usr/bin/env sh
set -eu
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ENV_FILE="$SCRIPT_DIR/audit-system-events.local.postman_environment.json"

if [ ! -f "$ENV_FILE" ]; then
  echo "Create audit-system-events.local.postman_environment.json from the template and supply local credentials." >&2
  exit 1
fi

npx --yes newman@6.2.2 run \
  "$SCRIPT_DIR/audit-system-events.postman_collection.json" \
  --environment "$ENV_FILE" \
  --bail
