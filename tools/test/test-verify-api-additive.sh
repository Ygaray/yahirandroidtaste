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

# (c) missing current .api file -> usage error, exit 1 (not 3)
git checkout -q -- .; git clean -fdq
rm "$API"
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 1 "missing current .api file is usage error (exit 1, not 3)"

# (d) baseline predates the .api file -> DEGRADE to exit 0 (not fail-closed 1); source-only still applies
git checkout -q -- .; git clean -fdq
git rm -q "$API"; git commit -qm "remove api file (pre-api baseline)"; git tag v-preapi
mkdir -p api; printf 'public fun a(): Unit\n' > "$API"   # current file exists + readable
set +e; "$SCRIPT" v-preapi >/dev/null 2>&1; rc=$?; set -e
check "$rc" 0 "baseline lacking the .api file degrades to exit 0 (not fail-closed 1)"

# (e) KNOWN RESIDUAL RISK (GOV-03 Pitfall 2 / RESEARCH.md Open Question 1): verify-api-additive.sh
# shares verify-additive-diff.sh's PRE-FIX architecture (stale, cumulative baseline-tag-vs-current
# comparison, not a per-commit staged delta) -- currently dormant only because v1.10.0 predates
# api.txt. D-01 scopes this phase's fix literally to src/main; this case proves live -- not just
# asserts -- that the identical bug shape is latent here, tracked as a residual risk for Phase 5's
# first tag-cut that includes api.txt, rather than fixed this phase.
# `git reset --hard` (not `git checkout -- .`): case (d)'s last commit removed the ONLY tracked
# file, leaving an empty tree — `git checkout -- .` errors on an empty index ("pathspec '.' did
# not match any file(s) known to git"); `git reset --hard` handles the empty-tree case cleanly.
git reset -q --hard; git clean -fdq
mkdir -p api; printf 'public fun a(): Unit\npublic fun b(): Unit\n' > "$API"   # restore v0.0.0's original 2-fn baseline content
# Simulate a legitimate, already-landed API break (mirrors case (b)): rename b -> bb, land it as
# already-committed history -- no new tag, the baseline stays v0.0.0, exactly as it would in
# reality until the NEXT tag is cut.
sed -i 's/public fun b(): Unit/public fun bb(): Unit/' "$API"
git add -A; git commit -qm "declared API break (rename b -> bb, already landed)"
# Simulate a LATER, completely unrelated commit that never touches api.txt at all -- no further
# edit to $API_FILE.
printf 'unrelated\n' > unrelated.txt; git add -A; git commit -qm "unrelated later commit"
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 3 "KNOWN RESIDUAL RISK (GOV-03 Pitfall 2, tracked not fixed this phase): once a baseline tag includes api.txt, an unrelated later commit after an earlier declared API-break stays permanently lane-3-flagged — same architecture bug DS-05 had; verify-api-additive.sh is currently dormant only because v1.10.0 predates api.txt. Revisit before/at Phase 5's first tag-cut that includes api.txt."

echo "PASS=$pass FAIL=$fail"; [ "$fail" -eq 0 ]
