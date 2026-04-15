#!/usr/bin/env bash
set -euo pipefail

if ! command -v mysql >/dev/null 2>&1; then
  echo "ERROR: mysql client no está instalado"
  exit 1
fi

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-store}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="${SCRIPT_DIR}/fix-user-details-schema.sql"

echo "Applying schema fix to ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"

if [[ -n "${DB_PASS}" ]]; then
  mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASS}" "${DB_NAME}" < "${SQL_FILE}"
else
  mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" "${DB_NAME}" < "${SQL_FILE}"
fi

echo "Done. user_details.id should now be AUTO_INCREMENT."
