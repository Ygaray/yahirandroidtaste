#!/usr/bin/env bash
# Probe ONE tile of the OSM tile server with an exact User-Agent string and report whether the
# server blocks it (POL-03, Phase 28).
#
# Usage: tools/probe-osm-user-agent.sh [--expect-blocked] "<agent>"
#
# Fetches exactly one tile (https://tile.openstreetmap.org/3/2/3.png). Never loops, never requests
# a second tile (OSM tile usage policy: https://operations.osmfoundation.org/policies/tiles/).
#
# A response is "blocked" when an x-blocked response header is present OR the body is the
# 6987-byte placeholder tile (md5 c069a15b2cc2d6b6f527ad09eb93c61a).
#
# Prints one line: AGENT=<agent> BLOCKED=<yes|no> SIZE=<bytes> MD5=<md5>
# Exit codes: 0 outcome matches the expectation (default: a real tile; --expect-blocked: the
# blocked placeholder), 1 it does not, 2 usage error, curl failure or non-200 status (a network
# problem is infrastructure, never a pass).
set -euo pipefail

PLACEHOLDER_SIZE=6987
PLACEHOLDER_MD5="c069a15b2cc2d6b6f527ad09eb93c61a"
TILE_URL="https://tile.openstreetmap.org/3/2/3.png"

expect_blocked=no
if [ "${1:-}" = "--expect-blocked" ]; then
  expect_blocked=yes
  shift
fi
if [ "$#" -ne 1 ] || [ -z "$1" ]; then
  echo "usage: $0 [--expect-blocked] \"<agent>\"" >&2
  exit 2
fi
agent="$1"

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

if ! status="$(curl -sS --max-time 20 -A "$agent" -D "$tmp/headers" -o "$tmp/body" -w '%{http_code}' "$TILE_URL")"; then
  echo "ERROR: curl failed (network problem, not a verdict)" >&2
  exit 2
fi
if [ "$status" != "200" ]; then
  echo "ERROR: unexpected HTTP status $status (not a verdict)" >&2
  exit 2
fi

size="$(wc -c < "$tmp/body" | tr -d ' ')"
md5="$(md5sum "$tmp/body" | cut -d' ' -f1)"

blocked=no
if grep -qi '^x-blocked:' "$tmp/headers"; then
  blocked=yes
elif [ "$size" = "$PLACEHOLDER_SIZE" ] && [ "$md5" = "$PLACEHOLDER_MD5" ]; then
  blocked=yes
fi

echo "AGENT=$agent BLOCKED=$blocked SIZE=$size MD5=$md5"

if [ "$blocked" = "$expect_blocked" ]; then
  exit 0
fi
exit 1
