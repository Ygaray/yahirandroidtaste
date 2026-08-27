#!/usr/bin/env bash
# classify-hub-change.sh — combine the additive guards into one lane verdict.
# lane 1 (exit 0): source append-only AND api append-only  -> inert-additive (parallel fast path)
# lane 2 (exit 2): api append-only BUT a pre-existing source line changed -> behavior change
# lane 3 (exit 3): an api line removed/renamed -> API break
# --mode curation: a lane-2/3 result is permitted (exit 0) but still reported.
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"; cd "$(git rev-parse --show-toplevel)"
BASE=""; MODE="additive"; JSON=0
while [ "$#" -gt 0 ]; do case "$1" in
  --baseline) BASE="$2"; shift 2;;
  --mode) MODE="$2"; shift 2;;
  --json) JSON=1; shift;;
  *) echo "unknown arg: $1" >&2; exit 1;;
esac; done
[ -n "$BASE" ] || { echo "Usage: $0 --baseline <tag> [--mode additive|curation] [--json]" >&2; exit 1; }

set +e
bash "$DIR/verify-additive-diff.sh" "$BASE" >/dev/null 2>&1; src_rc=$?
bash "$DIR/verify-api-additive.sh" "$BASE" >/dev/null 2>&1; api_rc=$?
set -e

if   [ "$api_rc" -eq 3 ]; then lane=3
elif [ "$src_rc" -ne 0 ]; then lane=2
else lane=1
fi

if [ "$JSON" -eq 1 ]; then echo "{\"lane\":$lane,\"mode\":\"$MODE\",\"baseline\":\"$BASE\"}"
else echo "LANE $lane (mode=$MODE, baseline=$BASE)"; fi

# Curation deliberately does non-additive work under the gate -> always exit 0.
[ "$MODE" = "curation" ] && exit 0

# Map lane to exit code: lane 1 -> 0, lane 2 -> 2, lane 3 -> 3
case "$lane" in
  1) exit 0;;
  2) exit 2;;
  3) exit 3;;
  *) exit 1;;
esac
