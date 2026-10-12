#!/usr/bin/env bash
# verify-api-source-additive.sh <baseline-tag> - source-additivity REVIEW of api.txt against a baseline tag (API-03, D-02 step 5).
#
# Why this exists: Metalava `apiCheck` compares the dump to the committed api.txt, and a hand review of
# `git diff v2.5.1 -- api.txt` is unreliable because a literal append-only text diff is impossible
# (sketch P2): appending a defaulted parameter REWRITES the old overload line, and adding or inserting
# defaulted fields before the two required lambdas of a model reorders its component functions.
# So the tool parses the baseline and head signature files and checks eight mechanical rules.
#
# What a green `API SOURCE-ADDITIVE OK` PROVES (for every baseline signature, on the api.txt text):
#   R1  every public or protected class present in the baseline still exists
#   R2  every (class, kind, member name) triple present in the baseline still exists
#       (kinds: ctor, method, property, field; a constructor's name is the class simple name)
#   R3  every baseline enum constant still exists
#   R4  some current signature of the same class, kind and name has an IDENTICAL ordered list of
#       REQUIRED (non-optional) parameters, type and name
#   R5  ... and its full parameter list (type and name, optional or not) STARTS WITH the baseline's, so
#       nothing is inserted before an existing parameter and a positional call keeps binding
#   R6  ... and its return type is unchanged (a property or field keeps its declared type)
#   R7  ... and, when the baseline's last parameter is a function type (a trailing lambda), nothing is
#       appended after it
#   R8  no enum constant is added to an existing enum (it breaks an exhaustive `when`)
# Appending an `optional` parameter after a non-lambda parameter, adding classes, members and overloads
# are never violations.
#
# REVIEWED EXCEPTIONS: ACCEPTED_EXCEPTIONS in the .py waives named rules for exact signatures, keyed by
# the baseline label. For v2.5.1 it holds exactly the three documented, accepted v2.6.0 positional
# breaks (SheetScaffold, UndoCenterScreen, NeedsConfirmation incl. its copy/component10/component11);
# each is printed as an `ACCEPTED ...` line and counted in `accepted_exceptions=<n>`. A green line with
# a non-zero count means "every call compiles EXCEPT the accepted, documented shapes", not "every call".
#
# What it does NOT prove (the call-site canary V251CallSiteCanaryTest covers the shapes consumers
# write): a new abstract member on an interface or a new subclass of a sealed type, a changed
# modifier (open/final/suspend/visibility narrowing of a kept member), changed generics bounds or
# supertypes, a changed default value, behaviour, binary compatibility (see verify-binary-abi.sh), or
# anything Metalava does not dump (internal members, value-class-mangled accessors).
#
# Usage:   tools/verify-api-source-additive.sh <baseline-tag>        e.g. tools/verify-api-source-additive.sh v2.5.1
# Output:  line 1 is `SEAMS: none` (or the seams that are set); then any `ACCEPTED R<n> ...` lines, then either
#          one `FAIL R<n>: ...` line per violation plus a `API SOURCE-ADDITIVE FAILED ...` summary, or
#          `API SOURCE-ADDITIVE OK baseline=<tag> removed_classes=0 removed_members=0 enum_constants_intact=<n>
#          required_param_changes=0 added_classes=<n> added_members=<n> signature_changes=0 accepted_exceptions=<n>`.
# Exit codes:
#   0  PASS    none of R1 to R8 is violated (apart from the reviewed exceptions)
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
