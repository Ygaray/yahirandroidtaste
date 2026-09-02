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
  # Default: every file tracked as of HEAD. Closes the path-list gap (spec §5.1) —
  # a contributor cannot silently edit an unguarded pre-existing file.
  # Use NUL-safe input (git ls-tree -z) to handle filenames with unusual characters (non-ASCII,
  # backslash, quote) that would otherwise be C-quoted under core.quotepath=true.
  # Default: every SOURCE file tracked as of HEAD (under src/). The diff basis is staged-vs-HEAD
  # (D-01, GOV-03 fix), so "pre-existing" now means "tracked in HEAD" — a file created after the
  # last baseline tag but before this commit is still pre-existing from this commit's point of
  # view and must be protected. Enumerating from $BASELINE_COMMIT instead would leave every file
  # created since the last tag permanently invisible to this guard until the next tag is cut
  # (CR-01). The additive invariant protects the CONSUMABLE surface (public API / behavior) — docs,
  # .planning/, and build files are not consumed by pinning apps and their edits are not breaking
  # changes, so they are excluded to avoid false blocks (a doc reword is not a lane-2 change). Pass
  # explicit [path...] to override for other layouts. Source additivity for a genuinely different
  # tree is the caller's responsibility.
  mapfile -d '' -t PATHS < <(git ls-tree -z -r --name-only HEAD -- src/main)
  [ "${#PATHS[@]}" -gt 0 ] || { echo "DS-05 FAIL: HEAD has no tracked production source under src/main." >&2; exit 1; }
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

REMOVED_RAW="$TMP_DIR/removed.raw"
ADDED_RAW="$TMP_DIR/added.raw"
REMOVED_NORM="$TMP_DIR/removed.norm"
ADDED_NORM="$TMP_DIR/added.norm"

: > "$REMOVED_RAW"
: > "$ADDED_RAW"

# Staged-vs-HEAD form (D-01, GOV-03 fix): diffs the INDEX (what THIS commit is about to add)
# against HEAD, NOT a stale baseline tag against the whole working tree. This makes classification
# per-commit rather than cumulative-since-the-tag: a commit that stages nothing under `${PATHS[@]}`
# is unconditionally clean, with no dependency on how long ago the last tag was cut or on any
# pre-existing rewrite that landed (with or without override) in prior history. `BASELINE_COMMIT`
# is used ONLY above, for the default `PATHS` enumeration and the ref-resolution fail-loudly check —
# never as a diff endpoint. `-- <paths>` filters to only the paths that matter to this guard.
git diff --cached -U0 -- "${PATHS[@]}" > "$TMP_DIR/full.diff" || true

# REMOVED content lines: start with '-' (including a bare '-' for a removed blank line), excluding
# the '---' file header (which is always followed by a space, never end-of-line).
grep -E '^-' "$TMP_DIR/full.diff" | grep -v -E '^--- ' > "$REMOVED_RAW" || true
# ADDED content lines: start with '+' (including a bare '+' for an added blank line), excluding
# the '+++' file header (which is always followed by a space, never end-of-line).
grep -E '^\+' "$TMP_DIR/full.diff" | grep -v -E '^\+\+\+ ' > "$ADDED_RAW" || true

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

OFFENDERS_FILE="$TMP_DIR/offenders"
comm -23 "$REMOVED_NORM" "$ADDED_NORM" > "$OFFENDERS_FILE" || true

# Use `[ -s ]` (file has bytes), not a `$(...)`-captured string test: a lone offending line that
# normalizes to EMPTY (a removed blank line, WR-01) is itself a single newline byte in the file,
# but command substitution unconditionally strips trailing newlines, which would collapse that
# one-byte file down to an empty string and make `[ -n "$OFFENDERS" ]` false — silently swallowing
# exactly the blank-line-removal case this guard exists to catch.
if [ -s "$OFFENDERS_FILE" ]; then
  while IFS= read -r line; do
    echo "DS-05 FAIL: line rewritten/removed in a pre-existing file: $line" >&2
  done < "$OFFENDERS_FILE"
  exit 1
fi

REMOVED_COUNT="$(grep -c . "$REMOVED_NORM" || true)"
echo "DS-05 PASS: $REMOVED_COUNT removed line(s), all accounted for by an identical added line (append-only)"
exit 0
