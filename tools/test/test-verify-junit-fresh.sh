#!/usr/bin/env bash
# Fixture test for tools/verify-junit-fresh.sh (Phase 29, plan 29-04). Fully offline: it builds
# synthetic Gradle result directories and orders file ages with `touch -d`, so nothing depends on
# the clock or on a real Gradle run.
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
SCRIPT="$DIR/verify-junit-fresh.sh"
[ -f "$SCRIPT" ] || { echo "FAIL: script under test is missing: $SCRIPT"; exit 1; }

TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }
check_grep(){ # <text> <pattern> <label>
  if printf '%s\n' "$1" | grep -Eq -- "$2"; then pass=$((pass+1)); else echo "FAIL: $3 (pattern '$2' not in output)"; fail=$((fail+1)); fi
}

MARKER="$TMP/marker"; : > "$MARKER"; touch -d '2026-01-01 10:00:00' "$MARKER"

mk_suite(){ # <dir> <class> <tests> <skipped> <failures> <errors> <when>
  local d="$1" cls="$2" t="$3" s="$4" f="$5" e="$6" when="$7" file
  mkdir -p "$d"
  file="$d/TEST-pkg.$cls.xml"
  {
    echo '<?xml version="1.0" encoding="UTF-8"?>'
    echo "<testsuite name=\"pkg.$cls\" tests=\"$t\" skipped=\"$s\" failures=\"$f\" errors=\"$e\" timestamp=\"2026-01-01T12:00:00.000Z\" hostname=\"h\" time=\"0.5\">"
    echo '  <properties/>'
    echo '</testsuite>'
  } > "$file"
  touch -d "$when" "$file"
}
FRESH='2026-01-01 12:00:00'
OLD='2026-01-01 08:00:00'

OUT=""; RC=0
run(){ RC=0; OUT=$(bash "$SCRIPT" "$@" 2>&1) || RC=$?; }

# 1. Fresh, green, required classes present.
D1="$TMP/d1"
mk_suite "$D1" AlphaTest 10 1 0 0 "$FRESH"
mk_suite "$D1" BetaTest 5 0 0 0 "$FRESH"
run "$D1" --marker "$MARKER" --require AlphaTest --require BetaTest
check "$RC" "0" "fresh green run exits 0"
check_grep "$OUT" '^FILES 2$' "file count line"
check_grep "$OUT" '^STALE 0$' "stale count line"
check_grep "$OUT" '^TOTALS tests=15 skipped=1 failures=0 errors=0$' "totals line shape and sums"
check_grep "$OUT" '^CLASS AlphaTest tests=10$' "per-class line"
check_grep "$OUT" '^CLASS BetaTest tests=5$' "second per-class line"
check_grep "$OUT" '^JUNIT-FRESH OK$' "final OK line"

# 2. One stale result file.
D2="$TMP/d2"
mk_suite "$D2" AlphaTest 10 0 0 0 "$FRESH"
mk_suite "$D2" OldTest 3 0 0 0 "$OLD"
run "$D2" --marker "$MARKER"
check "$RC" "1" "a stale file exits 1"
check_grep "$OUT" '^STALE 1$' "stale count is 1"
check_grep "$OUT" '^JUNIT-FRESH FAIL .*stale' "final line names stale"

# 3. Failures and errors.
D3="$TMP/d3"
mk_suite "$D3" AlphaTest 10 0 2 0 "$FRESH"
run "$D3" --marker "$MARKER"
check "$RC" "1" "failures above 0 exits 1"
check_grep "$OUT" '^JUNIT-FRESH FAIL .*failures' "final line names failures"
D3B="$TMP/d3b"
mk_suite "$D3B" AlphaTest 10 0 0 1 "$FRESH"
run "$D3B" --marker "$MARKER"
check "$RC" "1" "errors above 0 exits 1"
check_grep "$OUT" '^JUNIT-FRESH FAIL .*errors' "final line names errors"

# 4. Empty directory (and a directory with only non-result files).
D4="$TMP/d4"; mkdir -p "$D4"
run "$D4" --marker "$MARKER"
check "$RC" "1" "an empty directory exits 1"
check_grep "$OUT" '^JUNIT-FRESH FAIL .*no_files' "final line names no_files"

# 5. A required class with no file, and one whose file has zero tests.
run "$D1" --marker "$MARKER" --require GammaTest
check "$RC" "1" "a missing required class exits 1"
check_grep "$OUT" '^CLASS GammaTest MISSING$' "missing class line"
check_grep "$OUT" '^JUNIT-FRESH FAIL .*missing_GammaTest' "final line names the missing class"
D5="$TMP/d5"
mk_suite "$D5" AlphaTest 10 0 0 0 "$FRESH"
mk_suite "$D5" ZeroTest 0 0 0 0 "$FRESH"
run "$D5" --marker "$MARKER" --require ZeroTest
check "$RC" "1" "a required class with zero tests exits 1"
check_grep "$OUT" '^CLASS ZeroTest tests=0$' "zero-test class line"
check_grep "$OUT" '^JUNIT-FRESH FAIL .*zero_ZeroTest' "final line names the zero-test class"

# 6. A required class is matched by its exact simple name, not by a suffix of another class.
D6="$TMP/d6"
mk_suite "$D6" ExtraAlphaTest 4 0 0 0 "$FRESH"
run "$D6" --marker "$MARKER" --require AlphaTest
check "$RC" "1" "a longer class name does not satisfy the requirement"

# 7. Usage errors evaluate nothing.
run
check "$RC" "2" "no directory exits 2"
run "$D1"
check "$RC" "2" "no marker exits 2"
run "$D1" --marker "$TMP/no-such-marker"
check "$RC" "2" "a marker that does not exist exits 2"
check_grep "$OUT" '^ERROR' "usage error prints ERROR"
run "$D1" --marker "$MARKER" --bogus
check "$RC" "2" "an unknown option exits 2"
run "$D1" --marker "$MARKER" --require 'Bad;Name'
check "$RC" "2" "a --require with a shell metacharacter exits 2"
run "$D1" --marker "$MARKER" --require 'a*'
check "$RC" "2" "a --require with a glob exits 2"
run "$D1" --marker "$MARKER" --require 'Bad.Name'
check "$RC" "2" "a --require with a dot exits 2"
run "$TMP/no-such-dir" --marker "$MARKER"
check "$RC" "2" "a directory that does not exist exits 2"
check "$(printf '%s\n' "$OUT" | grep -c '^JUNIT-FRESH')" "0" "a usage error prints no verdict line"

# 8. The script writes nothing into the directory under test.
BEFORE=$(ls -la --time-style=full-iso "$D1" | md5sum)
run "$D1" --marker "$MARKER" --require AlphaTest
AFTER=$(ls -la --time-style=full-iso "$D1" | md5sum)
check "$AFTER" "$BEFORE" "the result directory is unchanged by a run"

echo "PASS=$pass FAIL=$fail"
[ "$fail" -eq 0 ]
