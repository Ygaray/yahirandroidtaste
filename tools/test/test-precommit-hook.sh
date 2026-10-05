#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
cd "$TMP"; git init -q -b main; git config user.email t@t; git config user.name t
mkdir -p tools/hooks src/main api
cp "$DIR"/verify-additive-diff.sh "$DIR"/verify-additive-surface.sh \
   "$DIR"/classify-hub-change.sh tools/
cp "$DIR"/hooks/pre-commit tools/hooks/pre-commit
printf 'val x = 1\n' > src/main/A.kt; printf 'public fun a(): Unit\n' > api/hub.api
git add -A; git commit -qm base; git tag v1.0.0
ln -sf ../../tools/hooks/pre-commit .git/hooks/pre-commit; chmod +x tools/hooks/pre-commit 2>/dev/null || true

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }

# (1) lane 1 additive commit -> allowed
printf 'public fun b(): Unit\n' >> api/hub.api; printf 'val y=2\n' > src/main/B.kt; git add -A
set +e; git commit -qm "additive"; check "$?" 0 "lane-1 commit allowed"; set -e
# Tag the "additive" state so later resets do not depend on how many earlier commits succeeded (IN-03).
git tag t-additive

# (2) removing an api line with no src/main change is no longer a block (Metalava apiCheck and
#     tools/verify-binary-abi.sh own the API surface, D-03) -> allowed, then reset back to the t-additive tag
sed -i 's/public fun a(): Unit//' api/hub.api; git add -A
set +e; git commit -qm "api line removal"; check "$?" 0 "api-line removal alone allowed"; set -e
git reset -q --hard t-additive

# (3) lane 2: rewrite an EXISTING source line -> blocked; override allows
printf 'val x = 999\n' > src/main/A.kt   # A.kt started as 'val x = 1' at tag v1.0.0 -> line rewrite = lane 2
git add -A
set +e; git commit -qm "behavior change"; check "$?" 1 "lane-2 commit blocked"; set -e
set +e; HUB_LANE_OVERRIDE=2 git commit -qm "declared behavior change"; check "$?" 0 "declared lane-2 allowed"; set -e

# (4) GOV-03 regression: the exact reproduced-bug shape. Right after a legitimate, override-landed
# src/main rewrite (the lane-2 commit above — poisoned history since the tag, mirroring the real
# 5b01532 HeatSwatch.kt reword), commit an UNRELATED, non-src/main file with NO HUB_LANE_OVERRIDE
# set. Pre-fix, the stale-tag-vs-working-tree comparison basis would inherit the earlier rewrite
# forever and wrongly block this; post-fix (D-01: staged-vs-HEAD), it must be unconditionally
# lane 1 because this commit's own staged delta touches nothing under src/main.
printf 'notes\n' > NOTES.md; git add -A
set +e; git commit -qm "unrelated docs-only change"; check "$?" 0 "post-lane-2 unrelated commit unblocked (GOV-03 fix)"; set -e

# (5) fail-closed: with the fixture's verify-additive-diff.sh gone and a src/main edit staged, the
# sub-guard exits 127, the classifier exits 1 and the hook blocks (exit 1).
# Reset to the t-additive tag (not a relative HEAD~N offset, which silently tests a different state if an
# earlier commit was blocked).
git reset -q --hard t-additive
rm tools/verify-additive-diff.sh
printf 'val x = 888\n' > src/main/A.kt; git add -A
set +e; git commit -qm "error state"; check "$?" 1 "classifier error => hook fails closed"; set -e

echo "PASS=$pass FAIL=$fail"; [ "$fail" -eq 0 ]
