#!/usr/bin/env bash
# Fixture-based test for verify-additive-diff.sh. Builds a throwaway git repo,
# tags a baseline, then asserts the guard's verdict for additive vs non-additive edits.
set -euo pipefail
SCRIPT="$(cd "$(dirname "$0")/.." && pwd)/verify-additive-diff.sh"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
cd "$TMP"; git init -q -b main; git config user.email t@t; git config user.name t

mkdir -p src/main
printf 'line one\nline two\n' > src/main/A.kt
printf 'old = 1\n' > src/main/B.kt
printf 'doc line\n' > NOTES.md   # a tracked DOC file OUTSIDE src/ (not part of the consumable surface)
git add -A; git commit -qm base; git tag v0.0.0

pass=0; fail=0
check() { if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }

# (a) pure append to an existing file, and a brand-new file -> append-only PASS (exit 0)
# Staged (git add -A): under the new staged-vs-HEAD basis (D-01), an unstaged working-tree edit
# produces an empty `git diff --cached` and would trivially (and wrongly) pass — stage it to
# actually exercise the invariant.
printf 'line three\n' >> src/main/A.kt
printf 'brand new\n' > src/main/C.kt
git add -A
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 0 "additive append + new file should pass with NO path args (all-files default)"

# (b) a REWRITE of a pre-existing SOURCE line (any file under src/) -> FAIL (exit 1)
# `git reset --hard` (not just `git checkout -- .`): case (a) staged its edits via `git add -A`,
# so the INDEX — not just the working tree — must be reset back to HEAD, or the prior case's
# staged rewrite would still leak into this case's `git diff --cached`.
git reset -q --hard; git clean -fdq
sed -i 's/old = 1/old = 2/' src/main/B.kt
git add -A
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 1 "rewrite of a pre-existing source file under src/ must be caught by the src-scoped default"

# (c) a REWRITE of a DOC file OUTSIDE src/ -> NOT a lane-2 change; excluded from the src-scoped guard (exit 0)
git reset -q --hard; git clean -fdq
sed -i 's/doc line/doc line edited/' NOTES.md
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 0 "a doc-file rewrite outside src/ is NOT flagged (docs are not the consumable surface)"

# (d) CR-01 regression: a file created AFTER the baseline tag (commit N), then a REWRITE of one of
# its lines in a later commit (N+1) -> FAIL (exit 1). The default PATHS enumeration must be drawn
# from HEAD, not the baseline ref, or a post-tag file is invisible to the guard for the rest of
# that tag's lifetime (the bug fixed in this phase).
git reset -q --hard; git clean -fdq
printf 'val d = 1\n' > src/main/D.kt
git add -A; git commit -qm "add D.kt (post-tag)"
sed -i 's/val d = 1/val d = 999/' src/main/D.kt
git add -A
set +e; "$SCRIPT" v0.0.0 >/dev/null 2>&1; rc=$?; set -e
check "$rc" 1 "rewrite of a line in a file created after the baseline tag must be caught (CR-01 regression)"

# (e) WR-01 regression: removing a blank line (no replacement) from a pre-existing source file
# must be caught -- a bare '-'/'+' diff marker (no second character) must not be silently dropped
# by the extraction regexes, and a lone offender that normalizes to EMPTY must not be swallowed by
# a `$(...)`-captured string test (trailing-newline stripping).
git reset -q --hard; git clean -fdq
printf 'line one\n\nline three\n' > src/main/A.kt
git add -A; git commit -qm "A.kt gains a blank line"
printf 'line one\nline three\n' > src/main/A.kt
git add -A
set +e; "$SCRIPT" HEAD~1 src/main/A.kt >/dev/null 2>&1; rc=$?; set -e
check "$rc" 1 "removing a blank line with no replacement must be caught (WR-01 regression)"

echo "PASS=$pass FAIL=$fail"; [ "$fail" -eq 0 ]
