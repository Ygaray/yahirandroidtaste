#!/usr/bin/env bash
set -euo pipefail
SCRIPT="$(cd "$(dirname "$0")/.." && pwd)/verify-api-additive.sh"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
cd "$TMP"; git init -q -b main; git config user.email t@t; git config user.name t
API="api/hub.api"; mkdir -p api
printf 'public fun a(): Unit\npublic fun b(): Unit\n' > "$API"
git add -A; git commit -qm base; git tag v0.0.0
export API_FILE="$API"

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }

# (a) add a new public symbol -> append-only, exit 0
printf 'public fun c(): Unit\n' >> "$API"
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 0 "added API symbol is append-only"

# (b) remove/rename a public symbol -> lane-3, exit 3
git checkout -q -- .; git clean -fdq
sed -i 's/public fun b(): Unit/public fun bb(): Unit/' "$API"   # rename b -> bb (a break)
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 3 "renamed API symbol is a lane-3 break"

echo "PASS=$pass FAIL=$fail"; [ "$fail" -eq 0 ]
