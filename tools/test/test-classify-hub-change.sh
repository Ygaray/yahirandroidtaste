#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"; SCRIPT="$DIR/classify-hub-change.sh"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
cd "$TMP"; git init -q -b main; git config user.email t@t; git config user.name t
# Minimal fixture: a source file, a stand-in .api (inert: API-surface checks are owned by Metalava
# apiCheck and tools/verify-binary-abi.sh, D-03), and copies of the guard scripts.
mkdir -p tools/explorer src/main api
cp "$DIR/verify-additive-diff.sh" "$DIR/verify-additive-surface.sh" tools/ 2>/dev/null || true
printf 'val x = 1\n' > src/main/A.kt
printf 'public fun a(): Unit\n' > api/hub.api
git add -A; git commit -qm base; git tag v0.0.0

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }

# (1) lane 1: new file + new api line
printf 'val y = 2\n' > src/main/B.kt; printf 'public fun b(): Unit\n' >> api/hub.api; git add -A
set +e; "$SCRIPT" --baseline v0.0.0 >/dev/null 2>&1; check "$?" 0 "pure additive => lane 1 exit 0"; set -e

# (2) removing an api line is NOT a signal for the classifier any more (exit 0)
git reset -q --hard HEAD
sed -i 's/public fun a(): Unit//' api/hub.api; git add -A
set +e; "$SCRIPT" --baseline v0.0.0 >/dev/null 2>&1; check "$?" 0 "api-line removal alone is inert => exit 0"; set -e

# (3) lane 2: rewrite an EXISTING src/main line (staged: the guard diffs index vs HEAD)
git reset -q --hard HEAD
printf 'val x = 999\n' > src/main/A.kt; git add -A
set +e; "$SCRIPT" --baseline v0.0.0 >/dev/null 2>&1; check "$?" 2 "source rewrite => lane 2 exit 2"; set -e

# (4) lane 2 under --mode curation is permitted (exit 0) but still reported
set +e; OUT="$("$SCRIPT" --baseline v0.0.0 --mode curation 2>&1)"; check "$?" 0 "lane 2 permitted under curation"; set -e
case "$OUT" in LANE\ 2*) pass=$((pass+1));; *) echo "FAIL: curation still reports lane 2 (got '$OUT')"; fail=$((fail+1));; esac

# (5) fail-closed: a missing sub-guard exits 127, which is neither 0 nor 1 -> classifier exit 1.
# The classifier resolves its guards relative to its own directory, so run a COPY from the fixture
# after deleting the fixture's verify-additive-diff.sh (invoking the real one would not exercise it).
cp "$DIR/classify-hub-change.sh" tools/
rm tools/verify-additive-diff.sh
set +e; bash tools/classify-hub-change.sh --baseline v0.0.0 >/dev/null 2>&1; check "$?" 1 "missing sub-guard => classifier fails closed exit 1"; set -e

echo "PASS=$pass FAIL=$fail"; [ "$fail" -eq 0 ]
