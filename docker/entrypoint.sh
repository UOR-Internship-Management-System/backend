#!/bin/sh
set -e

mkdir -p /app/data/cv /app/data/academic-ledger /app/data/exports
chown -R appuser:appgroup /app/data

exec gosu appuser java -jar app.jar
