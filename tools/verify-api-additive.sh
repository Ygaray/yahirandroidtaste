#!/usr/bin/env bash
# verify-api-additive.sh — the .api append-only lane signal.
# Exit 0: current .api is an append-only superset of the baseline .api (lane 1/2 for API).
# Exit 3: a public-API line was removed/renamed/re-signatured (lane-3 API break).
# Exit 1: usage/resolution error.
set -euo pipefail
REPO_ROOT="$(git rev-parse --show-toplevel)"; cd "$REPO_ROOT"
API_FILE="${API_FILE:?set API_FILE to the committed public-API file path (see tools/README-api-guard.md)}"
[ "$#" -ge 1 ] || { echo "Usage: $0 <baseline-ref>" >&2; exit 1; }
BASE="$1"
git show "$BASE:$API_FILE" >/dev/null 2>&1 || { echo "API-ADDITIVE FAIL: cannot read $API_FILE at $BASE" >&2; exit 1; }

# Every line present in the baseline .api must still be present now. A missing line = a removed
# or renamed public symbol. (Ordering-independent: compare as sets, like DS-05 / DS-04.)
missing="$(comm -23 <(git show "$BASE:$API_FILE" | sort -u) <(sort -u "$API_FILE") || true)"
if [ -n "$missing" ]; then
  printf '%s\n' "$missing" | while IFS= read -r l; do
    echo "API-ADDITIVE FAIL (lane 3): public API line removed/renamed since $BASE: $l" >&2
  done
  exit 3
fi
added="$(comm -13 <(git show "$BASE:$API_FILE" | sort -u) <(sort -u "$API_FILE") | grep -c . || true)"
echo "API-ADDITIVE PASS: +$added new public symbol line(s), 0 removed (append-only)"
exit 0
