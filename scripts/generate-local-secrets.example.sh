#!/usr/bin/env sh
set -eu

# Generate a sample .env file with randomised secrets for local development.
# This script does NOT create production credentials.

ENV_FILE=".env"

if [ -f "$ENV_FILE" ]; then
  echo "WARNING: $ENV_FILE already exists. Refusing to overwrite."
  echo "Delete it manually if you want to regenerate."
  exit 1
fi

JWT_SECRET=$(openssl rand -base64 48 2>/dev/null || head -c 48 /dev/urandom | base64)

cat > "$ENV_FILE" <<EOF
CV_DB_NAME=cv_management
CV_DB_USERNAME=cv_user
CV_DB_PASSWORD=cv_local_password
CV_DB_PORT=5432

SPRING_PROFILES_ACTIVE=local
JWT_SECRET=${JWT_SECRET}
JWT_ISSUER=cv-management-local
JWT_ACCESS_TOKEN_TTL=PT30M
JWT_REFRESH_TOKEN_TTL=P7D

FRONTEND_ORIGIN=http://localhost:5173
EOF

echo "Generated $ENV_FILE with a random JWT_SECRET."
echo "Do NOT commit this file to version control."
