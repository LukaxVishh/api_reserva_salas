#!/usr/bin/env bash
#
# Abre um shell mysql no container salalivre-mysql.
#
set -euo pipefail
exec podman exec -it salalivre-mysql mysql -uroot -prootroot salalivre
