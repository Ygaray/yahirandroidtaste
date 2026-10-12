#!/usr/bin/env bash
# Fixture-based test for verify-api-source-additive.sh: permanent negative controls for the four rules
# (R1 removed class, R2 removed member, R3 dropped enum constant, R4 changed required parameters) and the
# positive cases that must keep passing (appended or mid-list optional parameter, added class or member,
# overload rewrite). Each case writes tiny baseline and head signature files and drives the tool through
# its fixture seams. Prints one PASS or FAIL line per case, `ok` when every case behaved.
set -euo pipefail
TOOL="$(cd "$(dirname "$0")/.." && pwd)/verify-api-source-additive.sh"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
pass=0; fail=0
P="io.github.ygaray.yahirandroidtaste.component"

# Writes an api file: $1 = path, remaining args = body lines of ONE package (already indented).
write_api() {
  local path="$1"; shift
  { echo "// Signature format: 4.0"; echo "package $P {"; echo; printf '%s\n' "$@"; echo; echo "}"; } > "$path"
}

# case <name> <want-rc> <want-grep-or-empty> <baseline-file> <head-file>
run_case() {
  local name="$1" want="$2" pattern="$3" base="$4" head="$5" out rc
  set +e
  out="$(BASELINE_API_FILE="$base" HEAD_API_FILE="$head" bash "$TOOL" fixture 2>&1)"; rc=$?
  set -e
  if [ "$rc" != "$want" ]; then
    echo "FAIL: $name (exit $rc, want $want)"; echo "$out" | sed 's/^/    /'; fail=$((fail+1)); return
  fi
  if [ -n "$pattern" ] && ! printf '%s\n' "$out" | grep -Eq -- "$pattern"; then
    echo "FAIL: $name (output lacks /$pattern/)"; echo "$out" | sed 's/^/    /'; fail=$((fail+1)); return
  fi
  if [ "$(printf '%s\n' "$out" | head -1)" != "SEAMS: BASELINE_API_FILE HEAD_API_FILE" ]; then
    echo "FAIL: $name (first line is not the SEAMS line)"; fail=$((fail+1)); return
  fi
  echo "PASS: $name"; pass=$((pass+1))
}

CLASS_A=("  public final class Alpha {"
         "    ctor public Alpha(String id);"
         "    method public String label();"
         "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);"
         "    property public String id;"
         "  }")
ENUM_E=("  public enum Mode {"
        "    enum_constant public static final $P.Mode Fast;"
        "    enum_constant public static final $P.Mode Slow;"
        "  }")
BASE="$TMP/base.api"; HEAD="$TMP/head.api"
write_api "$BASE" "${CLASS_A[@]}" "" "${ENUM_E[@]}"

# 1. identical files pass
write_api "$HEAD" "${CLASS_A[@]}" "" "${ENUM_E[@]}"
run_case "identical files pass" 0 "API SOURCE-ADDITIVE OK .*removed_classes=0 removed_members=0 enum_constants_intact=2 required_param_changes=0 added_classes=0 added_members=0" "$BASE" "$HEAD"

# 2. head appends an optional parameter to a method (the old line is rewritten)
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done, optional boolean quiet);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "appended optional parameter passes" 0 "API SOURCE-ADDITIVE OK" "$BASE" "$HEAD"

# 3. head inserts an optional parameter in the middle
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int count, optional String? note, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "mid-list optional parameter passes" 0 "API SOURCE-ADDITIVE OK" "$BASE" "$HEAD"

# 4. head adds a class
write_api "$HEAD" "${CLASS_A[@]}" "" "${ENUM_E[@]}" "" "  public final class Beta {" "    ctor public Beta();" "  }"
run_case "added class passes" 0 "added_classes=1 added_members=1" "$BASE" "$HEAD"

# 5. head adds a method and an overload
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public String label(int width);" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    method public void extra();" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "added method passes" 0 "API SOURCE-ADDITIVE OK" "$BASE" "$HEAD"

# 6. overload rewrite: the old signature is gone, the same name carries the same required parameters plus optional ones
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id, optional int size, optional String? tag);" \
  "    method public String label();" \
  "    method public void run(int count, optional boolean quiet, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done, optional String? note);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "overload rewrite with defaults passes" 0 "API SOURCE-ADDITIVE OK" "$BASE" "$HEAD"

# 7. R1: head REMOVES a class
write_api "$HEAD" "${CLASS_A[@]}"
run_case "removed class fails (R1)" 1 "FAIL R1: class removed: $P.Mode" "$BASE" "$HEAD"

# 8. R2: head REMOVES a method name
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "removed method fails (R2)" 1 "FAIL R2: member removed: $P.Alpha method label" "$BASE" "$HEAD"

# 9. R2: head REMOVES a property
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "  }" "" "${ENUM_E[@]}"
run_case "removed property fails (R2)" 1 "FAIL R2: member removed: $P.Alpha property id" "$BASE" "$HEAD"

# 10. R3: head drops an enum constant
write_api "$HEAD" "${CLASS_A[@]}" "" "  public enum Mode {" "    enum_constant public static final $P.Mode Fast;" "  }"
run_case "dropped enum constant fails (R3)" 1 "FAIL R3: enum constant removed: $P.Mode.Slow" "$BASE" "$HEAD"

# 11. R4: head adds a REQUIRED parameter
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done, boolean quiet);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "added required parameter fails (R4)" 1 "FAIL R4: .*Alpha method run .*required parameters \(int, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit>\)" "$BASE" "$HEAD"

# 12. R4: head changes a required parameter type
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(long count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "changed required parameter type fails (R4)" 1 "FAIL R4: " "$BASE" "$HEAD"

# 13. R4: head removes a required parameter (a constructor)
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha();" \
  "    method public String label();" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "removed required constructor parameter fails (R4)" 1 "FAIL R4: .*Alpha ctor Alpha " "$BASE" "$HEAD"

# 14. a required parameter turned optional keeps the old call shape compiling, but R4 is strict on purpose
#     only when the REQUIRED list changes; this one drops a required parameter from the list, so it must fail
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int count, optional kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "required parameter turned optional fails (R4, strict by design)" 1 "FAIL R4: " "$BASE" "$HEAD"

# 15-17. tooling errors exit 2
set +e
out="$(bash "$TOOL" no-such-tag-xyz 2>&1)"; rc=$?
set -e
if [ "$rc" = 2 ] && printf '%s\n' "$out" | head -1 | grep -qx "SEAMS: none" && printf '%s\n' "$out" | grep -q "not an existing git tag"; then
  echo "PASS: unknown tag exits 2"; pass=$((pass+1)); else echo "FAIL: unknown tag (exit $rc)"; echo "$out"; fail=$((fail+1)); fi

set +e
out="$(BASELINE_API_FILE="$BASE" HEAD_API_FILE="$TMP/does-not-exist.api" bash "$TOOL" fixture 2>&1)"; rc=$?
set -e
if [ "$rc" = 2 ]; then echo "PASS: unreadable head file exits 2"; pass=$((pass+1)); else echo "FAIL: unreadable head file (exit $rc)"; fail=$((fail+1)); fi

set +e
out="$(bash "$TOOL" 2>&1)"; rc=$?
set -e
if [ "$rc" = 2 ]; then echo "PASS: missing argument exits 2"; pass=$((pass+1)); else echo "FAIL: missing argument (exit $rc)"; fail=$((fail+1)); fi

# 18. a file with no classes is a tooling error, never a pass
echo "// Signature format: 4.0" > "$TMP/empty.api"
set +e
out="$(BASELINE_API_FILE="$TMP/empty.api" HEAD_API_FILE="$TMP/empty.api" bash "$TOOL" fixture 2>&1)"; rc=$?
set -e
if [ "$rc" = 2 ]; then echo "PASS: empty api files exit 2"; pass=$((pass+1)); else echo "FAIL: empty api files (exit $rc)"; fail=$((fail+1)); fi

echo "cases passed=$pass failed=$fail"
if [ "$fail" -ne 0 ]; then exit 1; fi
echo ok
