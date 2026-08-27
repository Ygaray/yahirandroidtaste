#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
cd "$TMP"; git init -q -b main; git config user.email t@t; git config user.name t
mkdir -p tools/hooks tools/explorer src api
cp "$DIR"/verify-additive-diff.sh "$DIR"/verify-api-additive.sh "$DIR"/verify-additive-surface.sh \
   "$DIR"/classify-hub-change.sh tools/ 2>/dev/null || true
cp "$DIR"/hooks/pre-commit tools/hooks/pre-commit 2>/dev/null || true
printf 'val x = 1\n' > src/A.kt; printf 'public fun a(): Unit\n' > api/hub.api
git add -A; git commit -qm base; git tag v1.0.0
export API_FILE="api/hub.api"
ln -sf ../../tools/hooks/pre-commit .git/hooks/pre-commit; chmod +x tools/hooks/pre-commit 2>/dev/null || true

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }

# lane 1 additive commit -> allowed
printf 'public fun b(): Unit\n' >> api/hub.api; printf 'val y=2\n' > src/B.kt; git add -A
set +e; git commit -qm "additive"; check "$?" 0 "lane-1 commit allowed"; set -e

# lane 3 (remove api line) -> blocked
sed -i 's/public fun a(): Unit//' api/hub.api; git add -A
set +e; git commit -qm "break"; check "$?" 1 "lane-3 commit blocked"; set -e

# lane 3 with declared override -> allowed
set +e; HUB_LANE_OVERRIDE=3 git commit -qm "declared break"; check "$?" 0 "declared lane-3 allowed"; set -e

# lane 2: rewrite an EXISTING source line (api unchanged) -> blocked; override allows
# Reset to the pre-lane-3 state so API is back to original
git reset --hard HEAD~1
git checkout -q -- .; git clean -fdq
printf 'val x = 999\n' > src/A.kt   # A.kt started as 'val x = 1' at tag v1.0.0 -> line rewrite = lane 2
git add -A
set +e; git commit -qm "behavior change"; check "$?" 1 "lane-2 commit blocked"; set -e
set +e; HUB_LANE_OVERRIDE=2 git commit -qm "declared behavior change"; check "$?" 0 "declared lane-2 allowed"; set -e

echo "PASS=$pass FAIL=$fail"; [ "$fail" -eq 0 ]
