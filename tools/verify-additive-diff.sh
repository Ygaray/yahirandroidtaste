#!/usr/bin/env bash
#
# verify-additive-diff.sh — DS-05 append-only line guard.
#
# Complements verify-additive-surface.sh's DS-04 guard (which proves no registry entry NAME was
# removed) by proving the other half of the additive-only owner directive: that no LINE inside a
# pre-existing source file was rewritten since a given baseline — only pure appends. Any diff
# that rewrites a value, renames a symbol, rewords a KDoc line, or restyles a composable body
# leaves a removed line with no identical added counterpart and fails this guard.
#
# This is a durable, reusable check (Phase 123 DS-01, SecondBrain): run it again at any future
# hub growth phase by passing a new baseline ref as $1, and optionally a list of paths as $2+ (it
# defaults to every file tracked at the baseline).
#
# Usage:
#   ./tools/verify-additive-diff.sh <baseline-ref> [path...]
#
#   baseline-ref  Required. The git ref to diff HEAD against.
#   path...       Optional. Defaults to every file tracked at the baseline.
#
# Exit codes:
#   0  Every removed content line since baseline has an identical (modulo trailing continuation
#      punctuation) added counterpart — append-only holds.
#   1  One or more removed lines have no added counterpart (a rewrite/removal), or the given
#      baseline ref could not be resolved.
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

if [ "$#" -lt 1 ]; then
  echo "Usage: $0 <baseline-ref> [path...]" >&2
  exit 1
fi

BASELINE_COMMIT="$1"
shift

# Fail loudly if the given ref cannot be resolved, rather than silently diffing against nothing
# (mirrors verify-additive-surface.sh's own ref-resolution check and error wording).
if ! git show "$BASELINE_COMMIT" --stat >/dev/null 2>&1; then
  echo "DS-05 FAIL: baseline ref '$BASELINE_COMMIT' could not be resolved via 'git show'." >&2
  exit 1
fi

if [ "$#" -gt 0 ]; then
  PATHS=("$@")
else
  # Default: every file tracked at the baseline. Closes the path-list gap (spec §5.1) —
  # a contributor cannot silently edit an unguarded pre-existing file.
  # Use NUL-safe input (git ls-tree -z) to handle filenames with unusual characters (non-ASCII,
  # backslash, quote) that would otherwise be C-quoted under core.quotepath=true.
  # Default: every SOURCE file tracked at the baseline (under src/). The additive invariant protects
  # the CONSUMABLE surface (public API / behavior) — docs, .planning/, and build files are not
  # consumed by pinning apps and their edits are not breaking changes, so they are excluded to avoid
  # false blocks (a doc reword is not a lane-2 change). Pass explicit [path...] to override for other
  # layouts. Source additivity for a genuinely different tree is the caller's responsibility.
  mapfile -d '' -t PATHS < <(git ls-tree -z -r --name-only "$BASELINE_COMMIT" -- src/main)
  [ "${#PATHS[@]}" -gt 0 ] || { echo "DS-05 FAIL: baseline '$BASELINE_COMMIT' has no tracked production source under src/main." >&2; exit 1; }
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

REMOVED_RAW="$TMP_DIR/removed.raw"
ADDED_RAW="$TMP_DIR/added.raw"
REMOVED_NORM="$TMP_DIR/removed.norm"
ADDED_NORM="$TMP_DIR/added.norm"

: > "$REMOVED_RAW"
: > "$ADDED_RAW"

# Single-ref form (BASELINE, not BASELINE..HEAD): diffs the baseline commit against the current
# WORKING TREE, not just the last commit — so an uncommitted regression is caught too (this is
# what makes the negative-control demonstration below meaningful, and lets Plans 02-05 run this
# as a pre-commit check on their own working tree). `-- <paths>` filters to only the paths that
# matter to this guard.
git diff -U0 "$BASELINE_COMMIT" -- "${PATHS[@]}" > "$TMP_DIR/full.diff" || true

# REMOVED content lines: start with a single '-', excluding the '---' file header.
grep -E '^-[^-]' "$TMP_DIR/full.diff" > "$REMOVED_RAW" || true
# ADDED content lines: start with a single '+', excluding the '+++' file header.
grep -E '^\+[^+]' "$TMP_DIR/full.diff" > "$ADDED_RAW" || true

# Normalize identically: strip leading diff marker, strip trailing whitespace, strip one
# trailing '+' or ',' if present (the only legitimate rewrites this codebase's appends produce —
# a list operand gaining a trailing ' +', or a list element gaining a trailing ','), strip
# trailing whitespace again.
normalize() {
  sed -E \
    -e 's/^.//' \
    -e 's/[[:space:]]+$//' \
    -e 's/[+,]$//' \
    -e 's/[[:space:]]+$//' \
    "$1" | sort -u
}

normalize "$REMOVED_RAW" > "$REMOVED_NORM"
normalize "$ADDED_RAW" > "$ADDED_NORM"

OFFENDERS="$(comm -23 "$REMOVED_NORM" "$ADDED_NORM" || true)"

if [ -n "$OFFENDERS" ]; then
  while IFS= read -r line; do
    echo "DS-05 FAIL: line rewritten/removed in a pre-existing file: $line" >&2
  done <<< "$OFFENDERS"
  exit 1
fi

REMOVED_COUNT="$(grep -c . "$REMOVED_NORM" || true)"
echo "DS-05 PASS: $REMOVED_COUNT removed line(s), all accounted for by an identical added line (append-only)"
exit 0
