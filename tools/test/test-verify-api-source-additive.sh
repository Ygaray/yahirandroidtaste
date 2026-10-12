#!/usr/bin/env bash
# Fixture-based test for verify-api-source-additive.sh: permanent negative controls for the rules
# (R1 removed class, R2 removed member, R3 dropped enum constant, R4 changed required parameters,
# R5 parameter inserted before an existing one, R6 changed return or property type, R7 parameter appended
# after a trailing lambda, R8 enum constant added to an existing enum), the reviewed-exception allowlist, and
# the positive cases that must keep passing (appended optional parameter, added class or member, overload
# rewrite). Each case writes tiny baseline and head signature files and drives the tool through its fixture
# seams. A pass that would be unsound (an optional parameter inserted mid-list, an optional parameter
# appended after a trailing lambda) is a FAILING case here, never a passing one.
# Prints one PASS or FAIL line per case, `ok` when every case behaved.
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

# case <name> <want-rc> <want-grep-or-empty> <baseline-file> <head-file> [baseline-label, default `fixture`]
run_case() {
  local name="$1" want="$2" pattern="$3" base="$4" head="$5" label="${6:-fixture}" out rc
  set +e
  out="$(BASELINE_API_FILE="$base" HEAD_API_FILE="$head" bash "$TOOL" "$label" 2>&1)"; rc=$?
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

# 2. head appends an optional parameter to a constructor (the old line is rewritten)
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id, optional boolean quiet);" \
  "    method public String label();" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "appended optional parameter passes" 0 "API SOURCE-ADDITIVE OK" "$BASE" "$HEAD"

# 3. head inserts an optional parameter in the middle: a positional call `run(1, cb)` now binds `cb` to the
#    new parameter, so this is NOT additive (R5), even though the required list is unchanged
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int count, optional String? note, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "mid-list optional parameter fails (R5)" 1 "FAIL R5: .*Alpha method run: " "$BASE" "$HEAD"

# 3b. head appends an optional parameter AFTER a trailing lambda: `run(1) { }` no longer compiles (R7)
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done, optional boolean quiet);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "optional parameter after a trailing lambda fails (R7)" 1 "FAIL R7: .*Alpha method run: " "$BASE" "$HEAD"

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

# 6. overload rewrite: the old signatures are gone, the same names carry the same parameters plus appended optional ones
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id, optional int size, optional String? tag);" \
  "    method public String label(optional int width);" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "overload rewrite with appended defaults passes" 0 "API SOURCE-ADDITIVE OK" "$BASE" "$HEAD"

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
run_case "added required parameter fails (R4)" 1 "FAIL R4: .*Alpha method run: .*required parameters \(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done\)" "$BASE" "$HEAD"

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
run_case "removed required constructor parameter fails (R4)" 1 "FAIL R4: .*Alpha ctor Alpha: " "$BASE" "$HEAD"

# 14. a required parameter turned optional changes the REQUIRED list, so R4 fails by design (strict, conservative)
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int count, optional kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "required parameter turned optional fails (R4, strict by design)" 1 "FAIL R4: " "$BASE" "$HEAD"

# 14b. annotated class headers (`@androidx.compose.runtime.Immutable public final class ...`) are real classes:
#      removing one is R1, removing one of its members is R2 and is attributed to that class
GAMMA=("  @androidx.compose.runtime.Immutable public final class Gamma {"
       "    ctor @KotlinOnly public Gamma(float size);"
       "    method public float component1();"
       "    property public float size;"
       "  }")
write_api "$BASE" "${CLASS_A[@]}" "" "${GAMMA[@]}" "" "${ENUM_E[@]}"
write_api "$HEAD" "${CLASS_A[@]}" "" "${ENUM_E[@]}"
run_case "removed annotated class fails (R1)" 1 "FAIL R1: class removed: $P.Gamma" "$BASE" "$HEAD"
write_api "$HEAD" "${CLASS_A[@]}" "" "  @androidx.compose.runtime.Immutable public final class Gamma {" \
  "    ctor @KotlinOnly public Gamma(float size);" "    property public float size;" "  }" "" "${ENUM_E[@]}"
run_case "removed member of an annotated class fails (R2, right class)" 1 "FAIL R2: member removed: $P.Gamma method component1" "$BASE" "$HEAD"
write_api "$HEAD" "${CLASS_A[@]}" "" "${GAMMA[@]}" "" "${ENUM_E[@]}"
run_case "annotated class kept passes" 0 "API SOURCE-ADDITIVE OK" "$BASE" "$HEAD"
write_api "$BASE" "${CLASS_A[@]}" "" "${ENUM_E[@]}"

# 14c. R4: a renamed required parameter breaks every named-argument call
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int total, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "renamed required parameter fails (R4)" 1 "FAIL R4: .*Alpha method run: " "$BASE" "$HEAD"

# 14d. R5: a renamed OPTIONAL parameter also breaks named-argument calls
OPT_BASE=("  public final class Zeta {"
          "    method public void go(int count, optional boolean quiet);"
          "  }")
OPT_HEAD=("  public final class Zeta {"
          "    method public void go(int count, optional boolean silent);"
          "  }")
write_api "$TMP/zbase.api" "${OPT_BASE[@]}"; write_api "$TMP/zhead.api" "${OPT_HEAD[@]}"
run_case "renamed optional parameter fails (R5)" 1 "FAIL R5: .*Zeta method go: " "$TMP/zbase.api" "$TMP/zhead.api"

# 14e. R6: a changed return type
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public int label();" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public String id;" "  }" "" "${ENUM_E[@]}"
run_case "changed return type fails (R6)" 1 "FAIL R6: .*Alpha method label: " "$BASE" "$HEAD"

# 14f. R6: a changed property type
write_api "$HEAD" "  public final class Alpha {" \
  "    ctor public Alpha(String id);" \
  "    method public String label();" \
  "    method public void run(int count, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> done);" \
  "    property public int id;" "  }" "" "${ENUM_E[@]}"
run_case "changed property type fails (R6)" 1 "FAIL R6: .*Alpha property id: " "$BASE" "$HEAD"

# 14g. R8: an enum constant added to an existing enum breaks an exhaustive `when`
write_api "$HEAD" "${CLASS_A[@]}" "" "  public enum Mode {" "    enum_constant public static final $P.Mode Fast;" \
  "    enum_constant public static final $P.Mode Slow;" "    enum_constant public static final $P.Mode Turbo;" "  }"
run_case "enum constant added to an existing enum fails (R8)" 1 "FAIL R8: .*Mode.Turbo" "$BASE" "$HEAD"

# 14h. a suspend function's return type lives only in the synthetic, unnamed Continuation parameter:
#      changing its type argument must be seen (the parameter text contains spaces)
SUSP_BASE=("  public final class Eta {"
           "    method public suspend Object? load(String id, kotlin.coroutines.Continuation<? super $P.Foo>);"
           "  }")
SUSP_HEAD=("  public final class Eta {"
           "    method public suspend Object? load(String id, kotlin.coroutines.Continuation<? super $P.Bar>);"
           "  }")
write_api "$TMP/sbase.api" "${SUSP_BASE[@]}"; write_api "$TMP/shead.api" "${SUSP_HEAD[@]}"
run_case "changed suspend result type fails (R4)" 1 "FAIL R4: .*Eta method load: " "$TMP/sbase.api" "$TMP/shead.api"
run_case "unchanged suspend function passes" 0 "API SOURCE-ADDITIVE OK" "$TMP/sbase.api" "$TMP/sbase.api"

# 14i. the reviewed exceptions: a documented v2.6.0 shape passes ONLY under the v2.5.1 baseline label and is printed
SHEET_BASE=("  public final class SheetScaffoldKt {"
            "    method public static void SheetScaffold(kotlin.jvm.functions.Function0<kotlin.Unit> onDismissRequest, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> content);"
            "  }")
SHEET_HEAD=("  public final class SheetScaffoldKt {"
            "    method public static void SheetScaffold(kotlin.jvm.functions.Function0<kotlin.Unit> onDismissRequest, optional androidx.compose.ui.graphics.Shape shape, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> content);"
            "  }")
write_api "$TMP/hbase.api" "${SHEET_BASE[@]}"; write_api "$TMP/hhead.api" "${SHEET_HEAD[@]}"
run_case "accepted exception passes under v2.5.1 and is reported" 0 "ACCEPTED R5: .*SheetScaffoldKt method SheetScaffold" "$TMP/hbase.api" "$TMP/hhead.api" v2.5.1
run_case "accepted exception is counted in the OK line" 0 "API SOURCE-ADDITIVE OK .*accepted_exceptions=1" "$TMP/hbase.api" "$TMP/hhead.api" v2.5.1
run_case "the same shape fails under another baseline label (R5)" 1 "FAIL R5: .*SheetScaffoldKt method SheetScaffold: " "$TMP/hbase.api" "$TMP/hhead.api" some-other-tag
SHEET_HEAD2=("  public final class SheetScaffoldKt {"
             "    method public static void SheetScaffold(kotlin.jvm.functions.Function0<kotlin.Unit> onDismissRequest, optional androidx.compose.ui.graphics.Shape shape, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> content, boolean extra);"
             "  }")
write_api "$TMP/hhead2.api" "${SHEET_HEAD2[@]}"
run_case "an accepted exception does not waive a new required parameter (R4)" 1 "FAIL R4: .*SheetScaffoldKt method SheetScaffold: " "$TMP/hbase.api" "$TMP/hhead2.api" v2.5.1

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

# 19. an unrecognised block opener is a tooling error, never silently skipped
{ echo "// Signature format: 4.0"; echo "package $P {"; echo; echo "  public final weird Delta {"; echo "    ctor public Delta();"; echo "  }"; echo; echo "}"; } > "$TMP/weird.api"
set +e
out="$(BASELINE_API_FILE="$TMP/weird.api" HEAD_API_FILE="$TMP/weird.api" bash "$TOOL" fixture 2>&1)"; rc=$?
set -e
if [ "$rc" = 2 ] && printf '%s\n' "$out" | grep -q "unrecognised class header"; then echo "PASS: unrecognised class header exits 2"; pass=$((pass+1)); else echo "FAIL: unrecognised class header (exit $rc)"; fail=$((fail+1)); fi

echo "cases passed=$pass failed=$fail"
if [ "$fail" -ne 0 ]; then exit 1; fi
echo ok
