#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:6004/ms-store}"
LOGIN_URL="${BASE_URL%/}/api/v1/user/login"

# Puedes usar LOGIN_USERNAME / APP_USERNAME o EMAIL.
LOGIN_USERNAME="${LOGIN_USERNAME:-${APP_USERNAME:-}}"
EMAIL="${EMAIL:-}"
PASSWORD="${PASSWORD:-Passw0rd!}"

if [[ -z "${LOGIN_USERNAME}" && -z "${EMAIL}" ]]; then
  echo "ERROR: debes informar LOGIN_USERNAME/APP_USERNAME o EMAIL"
  echo "Ejemplo: LOGIN_USERNAME=admin PASSWORD=123456 ./scripts/login-user.sh"
  exit 1
fi

cat <<EOF
== LOGIN USER ==
URL      : ${LOGIN_URL}
USERNAME : ${LOGIN_USERNAME:-<empty>}
EMAIL    : ${EMAIL:-<empty>}
EOF

payload=$(cat <<JSON
{
  "username": "${LOGIN_USERNAME}",
  "email": "${EMAIL}",
  "password": "${PASSWORD}"
}
JSON
)

response=$(curl -sS -X POST "${LOGIN_URL}" \
  -H "Content-Type: application/json" \
  -d "${payload}" \
  -w "\nHTTP_STATUS:%{http_code}")

http_status=$(echo "${response}" | sed -n 's/^HTTP_STATUS://p')
body=$(echo "${response}" | sed '/^HTTP_STATUS:/d')

echo ""
echo "HTTP ${http_status}"
echo "${body}"

# Si está jq, extrae token automáticamente
if command -v jq >/dev/null 2>&1; then
  token=$(echo "${body}" | jq -r '.token // empty' 2>/dev/null || true)
  if [[ -n "${token}" ]]; then
    echo ""
    echo "TOKEN=${token}"
    echo "AUTH_HEADER=Authorization: Bearer ${token}"
  fi
fi
