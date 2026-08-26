#!/usr/bin/env bash
#
# verify-additive-surface.sh — DS-04 additive-safety guard.
#
# Proves that the ComponentRegistry.Entry name+family set at a given baseline git ref is a
# strict subset of the current working-tree set — i.e. no registered composable was
# removed or renamed since the baseline. This is a durable, reusable check: run it again at
# Phase 48's repin (or any future growth phase) by passing a new baseline ref as $1.
#
# Usage:
#   ./tools/verify-additive-surface.sh [baseline-ref]
#
#   baseline-ref  Optional. Defaults to a338ee5 (the pre-Phase-44 HEAD, per D-03's
#                 canary-baseline decision).
#
# Exit codes:
#   0  All baseline entry names are present in the current working tree (additive-only holds).
#   1  One or more baseline entry names are missing from the current working tree (regression),
#      or the given baseline ref could not be resolved.
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

BASELINE_COMMIT="${1:-a338ee5}"

# Fail loudly if the given ref cannot be resolved, rather than silently diffing against nothing.
if ! git show "$BASELINE_COMMIT" --stat >/dev/null 2>&1; then
  echo "DS-04 FAIL: baseline ref '$BASELINE_COMMIT' could not be resolved via 'git show'." >&2
  exit 1
fi

FAMILY_GLOB='src/main/java/io/github/ygaray/yahirandroidtaste/explorer/*FamilyScreen.kt'

# Extracts sorted, unique registry entry `name = "..."` values.
#   $1 = git ref to read at, or empty string to read the working tree.
extract_names() {
  local ref="$1"
  if [ -z "$ref" ]; then
    # Working tree: grep directly across the glob.
    grep -ohE 'name = "[^"]+"' $FAMILY_GLOB \
      | sed -E 's/^name = "(.*)"$/\1/' \
      | sort -u
  else
    # A given ref: git show cannot take a directory, so iterate per-file.
    local file
    for file in $FAMILY_GLOB; do
      git show "$ref:$file" 2>/dev/null | grep -ohE 'name = "[^"]+"' || true
    done | sed -E 's/^name = "(.*)"$/\1/' | sort -u
  fi
}

BASELINE_NAMES="$(extract_names "$BASELINE_COMMIT")"
CURRENT_NAMES="$(extract_names "")"

MISSING="$(comm -23 <(echo "$BASELINE_NAMES") <(echo "$CURRENT_NAMES") || true)"

BASELINE_COUNT="$(echo "$BASELINE_NAMES" | grep -c . || true)"
CURRENT_COUNT="$(echo "$CURRENT_NAMES" | grep -c . || true)"

if [ -n "$MISSING" ]; then
  echo "$MISSING" | while IFS= read -r name; do
    echo "DS-04 FAIL: registry entry removed/renamed since baseline $BASELINE_COMMIT: $name" >&2
  done
  exit 1
fi

echo "DS-04 PASS: $BASELINE_COUNT baseline entries all present in $CURRENT_COUNT current entries"
exit 0
