#!/usr/bin/env bash
# =====================================================================
# start-ms-shop.sh  —  Arranca ms-shop con las variables del .env
# =====================================================================
# Uso: ./start-ms-shop.sh
# El .env debe estar en el mismo directorio que este script.
# =====================================================================

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "❌ No se encontró .env en $SCRIPT_DIR"
  echo "   Copia .env.example o edita el .env con tus credenciales."
  exit 1
fi

# Cargar variables
set -o allexport
# shellcheck disable=SC1090
source "$ENV_FILE"
set +o allexport

# ── Localizar Java ───────────────────────────────────────────
JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/java}"
if [[ -z "$JAVA_BIN" || ! -x "$JAVA_BIN" ]]; then
  # Intentar SDKMAN por defecto
  SDKMAN_JAVA="$HOME/.sdkman/candidates/java/current/bin/java"
  SDKMAN_JAVA_AMZ="$HOME/.sdkman/candidates/java/21.0.9-amzn/bin/java"
  if [[ -x "$SDKMAN_JAVA" ]];     then JAVA_BIN="$SDKMAN_JAVA"
  elif [[ -x "$SDKMAN_JAVA_AMZ" ]]; then JAVA_BIN="$SDKMAN_JAVA_AMZ"
  else JAVA_BIN=$(command -v java 2>/dev/null || true)
  fi
fi
if [[ -z "$JAVA_BIN" || ! -x "$JAVA_BIN" ]]; then
  echo "❌ No se encontró Java. Instálalo o ajusta JAVA_HOME."
  exit 1
fi
echo "    Java : $JAVA_BIN"

JAR=$(find "$SCRIPT_DIR/target" -name "*.jar" ! -name "*sources*" ! -name "*original*" 2>/dev/null | tail -1)

if [[ -z "$JAR" ]]; then
  echo "⚙️  JAR no encontrado. Compilando con Maven..."
  cd "$SCRIPT_DIR"
  mvn clean package -DskipTests -q
  JAR=$(find "$SCRIPT_DIR/target" -name "*.jar" ! -name "*sources*" ! -name "*original*" | tail -1)
fi

echo "🚀  Arrancando ms-shop..."
echo "    JAR  : $JAR"
echo "    BD   : $HOST/$NAMEBD"
echo "    Puerto: ${SERVER_PORT:-8080}${CONTEXT_PATH:-}"
echo ""

"$JAVA_BIN" \
  -DHOST="${HOST}" \
  -DNAMEBD="${NAMEBD}" \
  -DUSER="${USER}" \
  -DPASS="${PASS}" \
  -DJWT_SECRET="${JWT_SECRET}" \
  -DSTRIPE_SECRET_KEY="${STRIPE_SECRET_KEY}" \
  -DSTRIPE_WEBHOOK_SECRET="${STRIPE_WEBHOOK_SECRET}" \
  -Dserver.port="${SERVER_PORT:-8080}" \
  -Dserver.servlet.context-path="${CONTEXT_PATH:-/ms-store}" \
  -jar "$JAR"
