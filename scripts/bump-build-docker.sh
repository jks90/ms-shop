#!/usr/bin/env bash
set -euo pipefail

# =====================================================================
# bump-build-docker.sh
# - Incrementa versión del pom.xml (patch/minor/major)
# - Compila el proyecto
# - Construye imagen Docker con la nueva versión
# =====================================================================
# Uso:
#   ./scripts/bump-build-docker.sh
#   BUMP_TYPE=minor ./scripts/bump-build-docker.sh
#   BUMP_TYPE=major KEEP_SNAPSHOT=false ./scripts/bump-build-docker.sh
# =====================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
POM_FILE="${PROJECT_DIR}/pom.xml"
POM_BAK="${PROJECT_DIR}/pom.xml.bak.bump"

BUMP_TYPE="${BUMP_TYPE:-patch}"          # patch | minor | major
KEEP_SNAPSHOT="${KEEP_SNAPSHOT:-true}"   # true | false
DOCKER_REPO="${DOCKER_REPO:-juankanh/ms-shop}"
MVN_CMD="${MVN_CMD:-mvn}"

log() {
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

if [[ ! -f "${POM_FILE}" ]]; then
  log "ERROR: no se encontró pom.xml en ${PROJECT_DIR}"
  exit 1
fi

if [[ "${BUMP_TYPE}" != "patch" && "${BUMP_TYPE}" != "minor" && "${BUMP_TYPE}" != "major" ]]; then
  log "ERROR: BUMP_TYPE inválido: ${BUMP_TYPE} (usa patch|minor|major)"
  exit 1
fi

extract_project_version() {
  # Extrae SOLO la versión del proyecto:
  # la primera etiqueta <version> después de </parent>
  awk '
    /<parent>/ { in_parent=1 }
    in_parent && /<\/parent>/ { in_parent=0; after_parent=1; next }
    after_parent && /<version>[^<]+<\/version>/ {
      match($0, /<version>[^<]+<\/version>/)
      if (RSTART > 0) {
        v = substr($0, RSTART + 9, RLENGTH - 19)
        print v
        exit
      }
    }
  ' "${POM_FILE}"
}

increment_version() {
  local version="$1"
  local major minor patch

  IFS='.' read -r major minor patch <<< "${version}"
  major="${major:-0}"
  minor="${minor:-0}"
  patch="${patch:-0}"

  case "${BUMP_TYPE}" in
    patch)
      patch=$((patch + 1))
      ;;
    minor)
      minor=$((minor + 1))
      patch=0
      ;;
    major)
      major=$((major + 1))
      minor=0
      patch=0
      ;;
  esac

  echo "${major}.${minor}.${patch}"
}

replace_project_version() {
  local new_version="$1"

  awk -v new="${new_version}" '
    {
      if ($0 ~ /<parent>/) in_parent=1
      if (in_parent && $0 ~ /<\/parent>/) { in_parent=0; after_parent=1; print; next }

      if (after_parent && !done && $0 ~ /<version>[^<]+<\/version>/) {
        sub(/<version>[^<]+<\/version>/, "<version>" new "</version>")
        done=1
      }

      print
    }
  ' "${POM_FILE}" > "${POM_FILE}.tmp"

  mv "${POM_FILE}.tmp" "${POM_FILE}"
}

restore_pom_on_error() {
  local exit_code=$?
  if [[ -f "${POM_BAK}" ]]; then
    mv "${POM_BAK}" "${POM_FILE}"
    log "Se restauró pom.xml por error en el proceso."
  fi
  exit ${exit_code}
}

log "== MS-SHOP BUMP + BUILD + DOCKER =="
cd "${PROJECT_DIR}"

current_version="$(extract_project_version)"
if [[ -z "${current_version}" ]]; then
  log "ERROR: no se pudo extraer la versión del proyecto desde pom.xml"
  exit 1
fi

base_version="${current_version%-SNAPSHOT}"
next_base_version="$(increment_version "${base_version}")"

if [[ "${KEEP_SNAPSHOT}" == "true" ]]; then
  new_version="${next_base_version}-SNAPSHOT"
else
  new_version="${next_base_version}"
fi

log "Versión actual : ${current_version}"
log "Nueva versión  : ${new_version}"

cp "${POM_FILE}" "${POM_BAK}"
trap restore_pom_on_error ERR

replace_project_version "${new_version}"
log "pom.xml actualizado"

log "Compilando proyecto..."
"${MVN_CMD}" clean package -DskipTests

docker_tag="${new_version}"
log "Comando Docker (debug): docker build --progress=plain -t ${DOCKER_REPO}:${docker_tag} ."
docker build --progress=plain -t "${DOCKER_REPO}:${docker_tag}" .

rm -f "${POM_BAK}"
trap - ERR

log "OK ✅ Imagen creada: ${DOCKER_REPO}:${docker_tag}"
log "Debug command: docker build -t ${DOCKER_REPO}:${docker_tag} ."