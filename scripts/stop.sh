#!/usr/bin/env bash
#
# Derruba o stack do Podman. Use --purge para apagar o volume tambem.
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

PURGE=0
for arg in "$@"; do
  case "$arg" in
    --purge) PURGE=1 ;;
    -h|--help) sed -n '2,5p' "$0"; exit 0 ;;
    *) echo "argumento desconhecido: $arg" >&2; exit 2 ;;
  esac
done

if [[ $PURGE -eq 1 ]]; then
  echo ">> Derrubando containers e APAGANDO volume salalivre-mysql-data..."
  "${COMPOSE[@]}" down -v
else
  echo ">> Derrubando containers (volume preservado)..."
  "${COMPOSE[@]}" down
fi
