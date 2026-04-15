#!/usr/bin/env bash
set -euo pipefail

# -----------------------------------------------------------------------------
# MS-SHOP public endpoints smoke test
# - Public catalog
# - OpenAPI docs
# - Optional public auth (login)
# -----------------------------------------------------------------------------

BASE_URL="${BASE_URL:-http://localhost:6004/ms-store}"
RUN_AUTH="${RUN_AUTH:-true}"

_TS="$(date +%s)"
APP_USERNAME="${APP_USERNAME:-user_1776171001}"
EMAIL="${EMAIL:-testuser@mail.com}"
PASSWORD="${PASSWORD:-12345678}"

RED='\033[0;31m'
GREEN='\033[0;32m'

YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log()   { echo -e "${BLUE}[INFO]${NC} $*"; }
ok()    { echo -e "${GREEN}[OK]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
fail()  { echo -e "${RED}[FAIL]${NC} $*"; }

if ! command -v curl >/dev/null 2>&1; then
  fail "curl no está instalado"
  exit 1
fi

HAS_JQ=true
if ! command -v jq >/dev/null 2>&1; then
  HAS_JQ=false
  warn "jq no está instalado; extracción automática limitada"
fi

TMP_BODY="$(mktemp)"
cleanup() {
  rm -f "$TMP_BODY"
}
trap cleanup EXIT

TOTAL=0
FAILED=0

print_body() {
  if [[ "$HAS_JQ" == "true" ]]; then
    jq . "$TMP_BODY" 2>/dev/null || cat "$TMP_BODY"
  else
    cat "$TMP_BODY"
  fi
}

extract_json() {
  local query="$1"
  if [[ "$HAS_JQ" == "true" ]]; then
    jq -r "$query // empty" "$TMP_BODY"
  else
    echo ""
  fi
}

matches_expected() {
  local code="$1"
  local expected_csv="$2"
  IFS=',' read -r -a arr <<< "$expected_csv"
  for x in "${arr[@]}"; do
    if [[ "$code" == "$x" ]]; then
      return 0
    fi
  done
  return 1
}

request() {
  local name="$1"
  local method="$2"
  local url="$3"
  local expected_csv="${4:-200}"
  local data="${5:-}"

  TOTAL=$((TOTAL + 1))
  log "$name -> $method $url"

  local http_code
  if [[ -n "$data" ]]; then
    http_code=$(curl -sS -o "$TMP_BODY" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" \
      --data "$data")
  else
    http_code=$(curl -sS -o "$TMP_BODY" -w "%{http_code}" -X "$method" "$url")
  fi

  echo "HTTP $http_code"
  print_body

  if matches_expected "$http_code" "$expected_csv"; then
    ok "$name completado"
    return 0
  fi

  FAILED=$((FAILED + 1))
  warn "$name devolvió HTTP $http_code (esperado: $expected_csv)"
  return 1
}

log "BASE_URL=$BASE_URL"

# 1) Swagger / OpenAPI públicos
request "Swagger UI" "GET" "$BASE_URL/swagger-ui/index.html" "200,302" || true
request "OpenAPI docs" "GET" "$BASE_URL/api-docs" "200" || true

# 2) Catálogo público
request "Public categories" "GET" "$BASE_URL/api/v1/public/categories" "200" || true

request "Public products" "GET" "$BASE_URL/api/v1/public/products?page=0&size=5&activeOnly=true" "200" || true
PRODUCT_ID="$(extract_json '.data[0].id')"
if [[ -n "$PRODUCT_ID" ]]; then
  request "Public product detail" "GET" "$BASE_URL/api/v1/public/products/$PRODUCT_ID" "200" || true
else
  warn "No hay productId en la lista pública; se omite detail"
fi

# 3) Auth pública (opcional: solo login)
if [[ "$RUN_AUTH" == "true" ]]; then
  LOGIN_PAYLOAD=$(cat <<JSON
{
  "username": "$APP_USERNAME",
  "email": "$EMAIL",
  "password": "$PASSWORD"
}
JSON
)
  request "Public login" "POST" "$BASE_URL/api/v1/user/login" "200,401" "$LOGIN_PAYLOAD" || true

  token="$(extract_json '.token')"
  if [[ -n "$token" ]]; then
    ok "Token obtenido"
  else
    warn "Sin token (usuario inexistente o cuenta sin confirmar)"
  fi
else
  warn "RUN_AUTH=false; se omite login"
fi

echo ""
log "Resumen: total=$TOTAL failed=$FAILED"
if [[ "$FAILED" -eq 0 ]]; then
  ok "Smoke test público finalizado sin fallos"
else
  warn "Smoke test público finalizado con fallos"
fi
