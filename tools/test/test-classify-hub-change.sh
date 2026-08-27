#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"; SCRIPT="$DIR/classify-hub-change.sh"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
cd "$TMP"; git init -q -b main; git config user.email t@t; git config user.name t
# Minimal fixture: a source file, a stand-in .api, and copies of the guard scripts.
mkdir -p tools/explorer src/main api
cp "$DIR/verify-additive-diff.sh" "$DIR/verify-api-additive.sh" "$DIR/verify-additive-surface.sh" tools/ 2>/dev/null || true
printf 'val x = 1\n' > src/main/A.kt
printf 'public fun a(): Unit\n' > api/hub.api
git add -A; git commit -qm base; git tag v0.0.0
export API_FILE="api/hub.api"

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }

# lane 1: new file + new api line
printf 'val y = 2\n' > src/main/B.kt; printf 'public fun b(): Unit\n' >> api/hub.api
set +e; "$SCRIPT" --baseline v0.0.0 >/dev/null 2>&1; check "$?" 0 "pure additive => lane 1 exit 0"; set -e

# lane 3: remove an api line
git checkout -q -- .; git clean -fdq
sed -i 's/public fun a(): Unit//' api/hub.api
set +e; "$SCRIPT" --baseline v0.0.0 >/dev/null 2>&1; check "$?" 3 "api removal => lane 3 exit 3"; set -e

# lane 3 under --mode curation is permitted (exit 0)
set +e; "$SCRIPT" --baseline v0.0.0 --mode curation >/dev/null 2>&1; check "$?" 0 "lane 3 permitted under curation"; set -e

# sub-guard error: missing API_FILE -> classifier fails closed (exit 1)
git checkout -q -- .; git clean -fdq
rm api/hub.api
set +e; "$SCRIPT" --baseline v0.0.0 >/dev/null 2>&1; check "$?" 1 "missing API_FILE => classifier fails closed exit 1"; set -e

echo "PASS=$pass FAIL=$fail"; [ "$fail" -eq 0 ]
