#!/usr/bin/env bash
#
# Sobe o MySQL via Podman e a API.
#
# Uso:
#   ./scripts/start.sh             # sobe banco + API (ambos em container)
#   ./scripts/start.sh --db-only   # apenas o banco
#   ./scripts/start.sh --local     # banco em container, API local (mvn no host)
#
# Requisitos:
#   - podman (com 'podman compose' OU podman-compose)
#   - --local exige mvn + java 17 no PATH
#
set -euo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if podman compose version >/dev/null 2>&1; then
  COMPOSE=(podman compose)
elif command -v podman-compose >/dev/null 2>&1; then
  COMPOSE=(podman-compose)
else
  echo "!! Nao encontrei 'podman compose' nem 'podman-compose' no PATH." >&2
  exit 1
fi

MODE="full"
for arg in "$@"; do
  case "$arg" in
    --db-only) MODE="db-only" ;;
    --local)   MODE="local" ;;
    -h|--help) sed -n '2,13p' "$0"; exit 0 ;;
    *) echo "argumento desconhecido: $arg" >&2; exit 2 ;;
  esac
done

CONTAINER_DB="salalivre-mysql"

echo ">> [1/3] Subindo MySQL..."
"${COMPOSE[@]}" up -d mysql

echo ">> [2/3] Aguardando healthcheck do banco..."
status=""
for i in $(seq 1 60); do
  status="$(podman inspect -f '{{.State.Health.Status}}' "$CONTAINER_DB" 2>/dev/null || echo "missing")"
  if [[ "$status" == "healthy" ]]; then
    echo "   MySQL pronto (tentativa $i)."
    break
  fi
  sleep 2
done

if [[ "$status" != "healthy" ]]; then
  echo "!! MySQL nao ficou saudavel a tempo. Ultimas 50 linhas do log:" >&2
  podman logs --tail 50 "$CONTAINER_DB" || true
  exit 1
fi

case "$MODE" in
  db-only)
    echo ">> --db-only: parando aqui."
    ;;

  local)
    if ! command -v mvn >/dev/null 2>&1; then
      echo "!! mvn nao encontrado no PATH. Instale Java 17 + Maven ou use o modo padrao." >&2
      exit 1
    fi
    echo ">> [3/3] Iniciando API local com Maven (Ctrl+C para parar)..."
    exec mvn spring-boot:run
    ;;

  full)
    echo ">> [3/3] Iniciando API em container (Ctrl+C para parar a API; banco continua de pe)..."
    # 'up' em foreground apenas para o service api. Quando o usuario der Ctrl+C,
    # so o container da api e parado; o mysql continua rodando para inspecoes.
    exec "${COMPOSE[@]}" up --no-log-prefix api
    ;;
esac
