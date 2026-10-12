#!/usr/bin/env bash
# verify-api-source-additive.sh <baseline-tag> - source-additivity REVIEW of api.txt against a baseline tag (API-03, D-02 step 5).
#
# Why this exists: Metalava `apiCheck` compares the dump to the committed api.txt, and a hand review of
# `git diff v2.5.1 -- api.txt` is unreliable because a literal append-only text diff is impossible
# (sketch P2): appending a defaulted parameter REWRITES the old overload line, and adding or inserting
# defaulted fields before the two required lambdas of a model reorders its component functions.
# The question that matters to a consumer that recompiles is "does every call that compiled before
# still compile", and for this library that reduces to four mechanical rules, checked on the parsed
# signature files instead of the text:
#   R1  every public or protected class present in the baseline still exists
#   R2  every (class, kind, member name) triple present in the baseline still exists
#       (kinds: ctor, method, property, field; a constructor's name is the class simple name)
#   R3  every baseline enum constant still exists
#   R4  for every baseline method or constructor signature, at least one current signature of the same
#       class, kind and name has an IDENTICAL ordered list of REQUIRED (non-optional) parameter types.
#       Appending or inserting `optional` parameters therefore passes; adding a required parameter,
#       removing one, or changing a required parameter type fails.
# Additions (classes, members, overloads) are never a violation.
#
# Usage:   tools/verify-api-source-additive.sh <baseline-tag>        e.g. tools/verify-api-source-additive.sh v2.5.1
# Output:  line 1 is `SEAMS: none` (or the seams that are set); then either one `FAIL R<n>: ...` line per
#          violation plus a `API SOURCE-ADDITIVE FAILED ...` summary, or `API SOURCE-ADDITIVE OK baseline=<tag>
#          removed_classes=0 removed_members=0 enum_constants_intact=<n> required_param_changes=0 added_classes=<n> added_members=<n>`.
# Exit codes:
#   0  PASS    none of R1 to R4 is violated
#   1  FAIL    at least one rule is violated (every violation is printed)
#   2  usage or tooling error: no argument, malformed tag or a name that is not an existing git tag,
#      unreadable or empty api file, python3 missing
# Baseline: `git show <tag>:api.txt` (the tag must exist under refs/tags). Head: the working-tree `api.txt`.
# Fixture-only seams (a release cut MUST show `SEAMS: none`): BASELINE_API_FILE, HEAD_API_FILE - existing
# files that bypass git; the tag argument is then only a label.
# Run it in the cut gate next to `./gradlew apiCheck`; the permanent negative controls are in
# tools/test/test-verify-api-source-additive.sh (run by tools/test/run-all.sh).
set -euo pipefail

SEAMS=""
for v in BASELINE_API_FILE HEAD_API_FILE; do
  if [ -n "${!v:-}" ]; then SEAMS="$SEAMS $v"; fi
done
if [ -z "$SEAMS" ]; then echo "SEAMS: none"; else echo "SEAMS:$SEAMS"; fi

die() { # <exit-code> <message>
  echo "API-ADDITIVE ERROR: $2" >&2
  exit "$1"
}

[ "$#" -ge 1 ] || die 2 "usage: tools/verify-api-source-additive.sh <baseline-tag>"
TAG="$1"
printf '%s' "$TAG" | grep -Eq '^[A-Za-z0-9][A-Za-z0-9._/-]*$' || die 2 "malformed baseline tag '$TAG'"
command -v python3 >/dev/null || die 2 "python3 not found"

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(git rev-parse --show-toplevel 2>/dev/null || true)"
W="$(mktemp -d)"; trap 'rm -rf "$W"' EXIT

if [ -n "${BASELINE_API_FILE:-}" ]; then
  [ -f "$BASELINE_API_FILE" ] || die 2 "BASELINE_API_FILE '$BASELINE_API_FILE' is not an existing file"
  BASE="$BASELINE_API_FILE"
else
  [ -n "$ROOT" ] || die 2 "not inside a git repository"
  # Must be a real tag (refs/tags/), never a branch or HEAD: moving refs are forbidden as baselines.
  git -C "$ROOT" rev-parse --verify --quiet "refs/tags/$TAG^{commit}" >/dev/null || die 2 "baseline '$TAG' is not an existing git tag"
  git -C "$ROOT" show "refs/tags/$TAG:api.txt" > "$W/baseline-api.txt" 2>/dev/null || die 2 "tag '$TAG' has no api.txt"
  BASE="$W/baseline-api.txt"
fi

if [ -n "${HEAD_API_FILE:-}" ]; then
  HEAD_FILE="$HEAD_API_FILE"
else
  [ -n "$ROOT" ] || die 2 "not inside a git repository"
  HEAD_FILE="$ROOT/api.txt"
fi
[ -f "$HEAD_FILE" ] || die 2 "head api file '$HEAD_FILE' is not an existing file"

rc=0
python3 -I "$HERE/verify-api-source-additive.py" "$BASE" "$HEAD_FILE" "$TAG" || rc=$?
exit "$rc"
