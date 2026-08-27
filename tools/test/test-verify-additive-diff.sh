#!/usr/bin/env bash
# Fixture-based test for verify-additive-diff.sh. Builds a throwaway git repo,
# tags a baseline, then asserts the guard's verdict for additive vs non-additive edits.
set -euo pipefail
SCRIPT="$(cd "$(dirname "$0")/.." && pwd)/verify-additive-diff.sh"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
cd "$TMP"; git init -q -b main; git config user.email t@t; git config user.name t

mkdir -p src
printf 'line one\nline two\n' > src/A.kt
printf 'old = 1\n' > src/B.kt
git add -A; git commit -qm base; git tag v0.0.0

pass=0; fail=0
check() { if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }

# (a) pure append to an existing file, and a brand-new file -> append-only PASS (exit 0)
printf 'line three\n' >> src/A.kt
printf 'brand new\n' > src/C.kt
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 0 "additive append + new file should pass with NO path args (all-files default)"

# (b) a REWRITE of a pre-existing line in a file NOT in the old hardcoded list -> FAIL (exit 1)
git checkout -q -- .; git clean -fdq
sed -i 's/old = 1/old = 2/' src/B.kt   # B.kt was never in the 6-file default
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 1 "rewrite of a non-listed pre-existing file must be caught by the all-files default"

echo "PASS=$pass FAIL=$fail"; [ "$fail" -eq 0 ]
