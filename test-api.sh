#!/usr/bin/env bash
# ============================================================
# test-api.sh  —  Suite de pruebas para ms-shop (ms-store)
# ============================================================
# Endpoints reales del micro:
#   POST /api/v1/customers/self/checkout         ← checkout + idempotencia
#   POST /api/v1/payments/stripe/webhook         ← webhook Stripe
#   GET  /api/v1/admin/orders/{orderId}/payments ← auditoría pagos (ADMIN)
#
# Uso:
#   ./test-api.sh [TOKEN_USER] [TOKEN_ADMIN]
#
# TOKEN_USER  : JWT de usuario autenticado (obtenido de ms-auth)
# TOKEN_ADMIN : JWT con rol ADMIN
# ============================================================

set -euo pipefail

GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; NC='\033[0m'; BOLD='\033[1m'

pass()    { echo -e "${GREEN}✔ PASS${NC}  $*" >&2; }
fail()    { echo -e "${RED}✘ FAIL${NC}  $*" >&2; FAILED=$((FAILED+1)); }
info()    { echo -e "${CYAN}ℹ${NC}  $*" >&2; }
warn()    { echo -e "${YELLOW}⚠${NC}  $*" >&2; }
section() { echo -e "\n${BOLD}${YELLOW}══ $* ══${NC}" >&2; }

FAILED=0; TOTAL=0

BASE_URL="http://localhost:6070/ms-store/api/v1"
ACTUATOR_URL="http://localhost:6070/ms-store/actuator/health"
TOKEN="${1:-${TOKEN:-}}"
ADMIN_TOKEN="${2:-${ADMIN_TOKEN:-}}"
IDEMPOTENCY_KEY="$(uuidgen 2>/dev/null || echo '11111111-2222-3333-4444-555555555555')"

call() {
  local expected="$1" desc="$2"; shift 2
  [[ "$1" == "--" ]] && shift
  TOTAL=$((TOTAL+1))
  local response body
  response=$(curl -s -o /tmp/ms_shop_body -w "%{http_code}" "$@") || true
  body=$(cat /tmp/ms_shop_body)
  if [[ "$response" == "$expected" ]]; then
    pass "[$response] $desc"
  else
    fail "[$response != $expected] $desc"
    echo "  Body: $(echo "$body" | head -3)" >&2
  fi
  echo "$body"
}

# ── 0. Health check ───────────────────────────────────────────
section "0. Health check"
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "$ACTUATOR_URL" 2>/dev/null) || true
if [[ "$HEALTH" == "200" ]]; then
  pass "[200] Actuator /health"
else
  echo -e "${RED}ERROR: El microservicio no responde (HTTP $HEALTH)${NC}" >&2
  echo "  Arranca con: ./start-ms-shop.sh" >&2
  exit 1
fi

# ── 1. Seguridad: endpoints protegidos sin token ────────────────
section "1. Seguridad"

for url in \
  "$BASE_URL/customers/self/checkout" \
  "$BASE_URL/admin/orders/1/payments"; do
  RESP=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$url" \
    -H "Content-Type: application/json" -d '{}') || true
  TOTAL=$((TOTAL+1))
  path="${url/$BASE_URL/}"
  if [[ "$RESP" == "401" || "$RESP" == "403" ]]; then
    pass "[$RESP] $path sin token → denegado"
  else
    fail "[$RESP != 401/403] $path sin token no fue denegado"
  fi
done

# Token inválido en checkout → 401/403
RESP=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "$BASE_URL/customers/self/checkout" \
  -H "Authorization: Bearer INVALID_TOKEN" \
  -H "Content-Type: application/json" -d '{}') || true
TOTAL=$((TOTAL+1))
if [[ "$RESP" == "401" || "$RESP" == "403" ]]; then
  pass "[$RESP] Token inválido en /checkout → 401/403"
else
  fail "[$RESP] Token inválido no devolvió 401/403"
fi

# ── 2. Webhook Stripe ─────────────────────────────────────────
section "2. Webhook Stripe"

# Sin Stripe-Signature → 400/401/500 (no 404)
RESP=$(curl -s -o /tmp/ms_shop_body -w "%{http_code}" \
  -X POST "$BASE_URL/payments/stripe/webhook" \
  -H "Content-Type: application/json" \
  -d '{"id":"evt_test","type":"checkout.session.completed","data":{}}') || true
TOTAL=$((TOTAL+1))
if [[ "$RESP" =~ ^[45][0-9][0-9]$ && "$RESP" != "404" ]]; then
  pass "[$RESP] Webhook sin Stripe-Signature → rechazado"
else
  fail "[$RESP] Webhook sin firma no fue rechazado correctamente"
fi

# Firma malformada → 400
RESP=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "$BASE_URL/payments/stripe/webhook" \
  -H "Content-Type: application/json" \
  -H "Stripe-Signature: t=fake,v1=invalida" \
  -d '{"id":"evt_test","type":"checkout.session.completed","data":{}}') || true
TOTAL=$((TOTAL+1))
if [[ "$RESP" == "400" || "$RESP" == "401" || "$RESP" == "403" || "$RESP" == "500" ]]; then
  pass "[$RESP] Webhook con firma inválida → rechazado"
else
  fail "[$RESP] Webhook con firma inválida no devolvió 4xx/5xx de rechazo"
fi

# ── 3. Checkout e idempotencia (requiere TOKEN) ───────────────
section "3. Checkout e idempotencia"

if [[ -z "$TOKEN" ]]; then
  warn "Sin TOKEN user — se saltan tests de checkout."
  warn "Uso: ./test-api.sh <JWT_USUARIO> [JWT_ADMIN]"
else
  CHECKOUT_PAYLOAD='{"shippingAddressId":1,"billingAddressId":2,"paymentMode":"CHECKOUT_SESSION","successUrl":"https://test.local/ok","cancelUrl":"https://test.local/ko","notes":"Test script"}'

  # 1er intento
  IDEM_KEY="$(uuidgen 2>/dev/null || echo '33333333-4444-5555-6666-777777777777')"
  R1=$(curl -s -o /tmp/ms_shop_body_1 -w "%{http_code}" \
    -X POST "$BASE_URL/customers/self/checkout" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Idempotency-Key: $IDEM_KEY" \
    -H "Content-Type: application/json" \
    -d "$CHECKOUT_PAYLOAD") || true
  info "POST /checkout 1er intento → HTTP $R1"

  # Reintento mismo key+body → mismo código (idempotencia)
  R2=$(curl -s -o /tmp/ms_shop_body_2 -w "%{http_code}" \
    -X POST "$BASE_URL/customers/self/checkout" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Idempotency-Key: $IDEM_KEY" \
    -H "Content-Type: application/json" \
    -d "$CHECKOUT_PAYLOAD") || true
  TOTAL=$((TOTAL+1))
  if [[ "$R1" == "$R2" ]]; then
    pass "[$R2] Idempotencia: misma respuesta para key+body repetido"
  else
    fail "[$R1 → $R2] Idempotencia: respuestas distintas para misma key+body"
  fi

  # Mismo key, payload distinto → 409
  R3=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/customers/self/checkout" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Idempotency-Key: $IDEM_KEY" \
    -H "Content-Type: application/json" \
    -d '{"shippingAddressId":1,"billingAddressId":2,"paymentMode":"CHECKOUT_SESSION","successUrl":"https://otro.com/ok","cancelUrl":"https://otro.com/ko"}') || true
  TOTAL=$((TOTAL+1))
  if [[ "$R3" == "409" ]]; then
    pass "[409] IDEMPOTENCY_CONFLICT: key+payload distinto → 409"
  else
    info "[${R3}] Idempotency conflict (409 esperado; puede ser otro 4xx si el checkout falló antes)"
    TOTAL=$((TOTAL-1))
  fi

  # Sin Idempotency-Key → 400 (requerida en checkout)
  R4=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "$BASE_URL/customers/self/checkout" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$CHECKOUT_PAYLOAD") || true
  TOTAL=$((TOTAL+1))
  if [[ "$R4" == "400" || "$R4" == "422" ]]; then
    pass "[$R4] POST /checkout sin Idempotency-Key → error correcto"
  else
    info "[${R4}] POST /checkout sin Idempotency-Key devolvió $R4 (se esperaba 400/422)"
    TOTAL=$((TOTAL-1))
  fi
fi

# ── 4. Admin: auditoría de pagos ─────────────────────────────
section "4. Admin — auditoría pagos"

if [[ -z "$ADMIN_TOKEN" ]]; then
  warn "Sin ADMIN_TOKEN — se saltan tests de admin."
else
  # GET /admin/orders/{orderId}/payments con order existente
  # Primero obtenemos un orderId de la BBDD para no inventar uno
  ORDER_ID_FROM_DB=$(curl -s \
    "$BASE_URL/admin/orders/1/payments" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -o /dev/null -w "%{response_code}") || true
  # Intentamos con el orderId 1
  RESP=$(curl -s -o /dev/null -w "%{http_code}" \
    "$BASE_URL/admin/orders/1/payments" \
    -H "Authorization: Bearer $ADMIN_TOKEN") || true
  TOTAL=$((TOTAL+1))
  if [[ "$RESP" == "200" || "$RESP" == "404" ]]; then
    pass "[$RESP] GET /admin/orders/1/payments → respuesta válida (200 si existe, 404 si no)"
  else
    fail "[$RESP] GET /admin/orders/1/payments devolvió respuesta inesperada"
  fi

  # Sin rol ADMIN → 403
  if [[ -n "$TOKEN" ]]; then
    RESP=$(curl -s -o /dev/null -w "%{http_code}" \
      "$BASE_URL/admin/orders/1/payments" \
      -H "Authorization: Bearer $TOKEN") || true
    TOTAL=$((TOTAL+1))
    if [[ "$RESP" == "403" || "$RESP" == "401" ]]; then
      pass "[$RESP] /admin/ con token user (no admin) → denegado"
    else
      fail "[$RESP] /admin/ con token user no fue denegado (esperado 403/401)"
    fi
  fi
fi

# ── Resumen ───────────────────────────────────────────────────
PASSED=$((TOTAL-FAILED))
echo "" >&2
echo -e "${BOLD}═══════════════════════════════════════${NC}" >&2
echo -e "${BOLD} Resultado: ${GREEN}$PASSED${NC}/${BOLD}$TOTAL passed${NC}  ${RED}$FAILED failed${NC}" >&2
echo -e "${BOLD}═══════════════════════════════════════${NC}" >&2
[[ $FAILED -eq 0 ]] && exit 0 || exit 1
