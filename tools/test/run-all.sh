#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
rc=0
for t in "$DIR"/test-*.sh; do
  echo "== $t =="
  if bash "$t"; then echo "  ok"; else echo "  FAILED"; rc=1; fi
done
exit "$rc"
