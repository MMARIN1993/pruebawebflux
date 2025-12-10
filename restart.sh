#!/usr/bin/env bash
set -euo pipefail

# Script de redeploy para la app prueba-webflux
# - Baja la stack con docker compose down
# - Construye la imagen Docker
# - Levanta la stack con docker compose up -d
# Uso:
#   ./restart.sh            -> redeploy
#   ./restart.sh logs       -> redeploy y seguir logs
#   ./restart.sh down       -> solo bajar
#   ./restart.sh up         -> solo subir (requiere imagen construida)

IMAGE_NAME="prueba-webflux:local"
SERVICE_NAME="prueba-webflux"
COMPOSE_FILE="docker-compose.yml"

function ensure_compose() {
  if [[ ! -f "$COMPOSE_FILE" ]]; then
    echo "[ERROR] No se encontró $COMPOSE_FILE en el directorio actual. Ejecuta este script en la raíz del proyecto." >&2
    exit 1
  fi
}

function down() {
  ensure_compose
  echo "=== Paso 1: bajar stack con docker compose ==="
  docker compose down || true
}

function build_image() {
  echo "=== Paso 2: construir imagen ==="
  docker build -t "$IMAGE_NAME" .
}

function up() {
  ensure_compose
  echo "=== Paso 3: levantar stack ==="
  docker compose up -d
}

function logs() {
  echo "=== Paso 4: logs del servicio ==="
  docker compose logs -f "$SERVICE_NAME"
}

case "${1:-redeploy}" in
  redeploy)
    down
    build_image
    up
    echo "=== Listo: la app debería estar arriba en http://localhost:8080 ==="
    ;;
  logs)
    down
    build_image
    up
    logs
    ;;
  down)
    down
    ;;
  up)
    up
    ;;
  *)
    echo "Uso: $0 [redeploy|logs|down|up]" >&2
    exit 2
    ;;

esac

