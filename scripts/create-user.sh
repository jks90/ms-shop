#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:6004/ms-store}"
SIGNUP_URL="${BASE_URL%/}/api/v1/user/signup"

SIGNUP_USERNAME="${SIGNUP_USERNAME:-${APP_USERNAME:-user_$(date +%s)}}"
EMAIL="${EMAIL:-${SIGNUP_USERNAME}@example.com}"
PASSWORD="${PASSWORD:-Passw0rd!}"

cat <<EOF
== CREATE USER ==
URL      : ${SIGNUP_URL}
USERNAME : ${SIGNUP_USERNAME}
EMAIL    : ${EMAIL}
EOF

response=$(curl -sS -X POST "${SIGNUP_URL}" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${SIGNUP_USERNAME}\",\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\"}" \
  -w "\nHTTP_STATUS:%{http_code}")

http_status=$(echo "${response}" | sed -n 's/^HTTP_STATUS://p')
body=$(echo "${response}" | sed '/^HTTP_STATUS:/d')

echo ""
echo "HTTP ${http_status}"
echo "${body}"

echo ""
echo "Tip: si tienes confirmación de email activa, confirma la cuenta antes de login."
echo "Nota: usa SIGNUP_USERNAME o APP_USERNAME (no USERNAME en zsh)."
