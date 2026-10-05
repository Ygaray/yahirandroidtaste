#!/usr/bin/env bash
# Fixture test for tools/verify-binary-abi.sh (D-03). Fully offline: fixture AARs are built with
# javac + jar only (an .aar is a zip whose root member is classes.jar). No Gradle, no network.
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
SCRIPT="$DIR/verify-binary-abi.sh"
[ -f "$SCRIPT" ] || { echo "FAIL: script under test is missing: $SCRIPT"; exit 1; }
for t in javac jar javap unzip; do
  command -v "$t" >/dev/null || { echo "FAIL: $t not found (need a JDK)"; exit 1; }
done

TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
REPO="$TMP/repo"; FX="$TMP/fx"; mkdir -p "$REPO/tools" "$FX"
cp "$SCRIPT" "$REPO/tools/verify-binary-abi.sh"; chmod 755 "$REPO/tools/verify-binary-abi.sh"
cd "$REPO"; git init -q -b main; git config user.email t@t; git config user.name t
printf 'x\n' > README; git add -A; git commit -qm base; git tag v0.0.0

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }
check_grep(){ # file pattern label
  if grep -Eq -- "$2" "$1"; then pass=$((pass+1)); else echo "FAIL: $3 (pattern '$2' not in $1)"; fail=$((fail+1)); fi
}

# --- fixture AAR builder: build_aar <name> <variant> -------------------------------------------
gen_src(){ # <srcdir> <variant>
  local s="$1" v="$2"; mkdir -p "$s/p"
  local b="int"; [ "$v" = chg ] && b="long"
  { echo 'package p;'; echo 'public class Alpha {'
    [ "$v" = rm ] || echo '  public void a() {}'
    echo "  public void b($b x) {}"
    [ "$v" = add ] && echo '  public void c() {}'
    echo '}'; } > "$s/p/Alpha.java"
  # Modifier-less (package-private) class: javap -public prints "class p.Hidden {" with its public members (WR-05).
  [ "$v" = nohid ] || printf 'package p;\nclass Hidden { public void hid() {} }\n' > "$s/p/Hidden.java"
  # Protected member of a public open class: binary API for subclassing consumers; javap -public would hide it (WR-02).
  { echo 'package p;'; echo 'public class Prot {'
    [ "$v" = noprot ] || echo '  protected void prot(int x) {}'
    echo '  public void keep() {}'; echo '}'; } > "$s/p/Prot.java"
  # Class-header facts with no members of their own (WR-01): a member-less marker interface, a class that
  # implements it, and an open (non-final) class.
  [ "$v" = rmmarker ] || printf 'package p;\npublic interface Marker {}\n' > "$s/p/Marker.java"
  { echo 'package p;'
    if [ "$v" = rmmarker ] || [ "$v" = nosuper ]; then echo 'public class Sub extends Alpha {}'
    else echo 'public class Sub extends Alpha implements Marker {}'; fi; } > "$s/p/Sub.java"
  if [ "$v" = final ]; then printf 'package p;\npublic final class Open {}\n' > "$s/p/Open.java"
  else printf 'package p;\npublic class Open {}\n' > "$s/p/Open.java"; fi
  # Hand-written class whose name merely CONTAINS _Factory must still be compared (IN-01).
  [ "$v" = nohelper ] || printf 'package p;\npublic class Foo_FactoryHelper { public void h() {} }\n' > "$s/p/Foo_FactoryHelper.java"
  [ "$v" = nofactory ] || printf 'package p;\npublic class Foo_Factory { public void make() {} }\n' > "$s/p/Foo_Factory.java"
  [ "$v" = nocs ] || printf 'package p;\npublic class ComposableSingletons$FooKt { public void lam() {} }\n' > "$s/p/ComposableSingletons\$FooKt.java"
  [ "$v" = add ] && printf 'package p;\npublic class Beta { public void z() {} }\n' > "$s/p/Beta.java"
  return 0
}
build_aar(){ # <name> <variant>
  local n="$1" v="$2" d="$FX/$1"; mkdir -p "$d/src" "$d/out" "$d/aar"
  gen_src "$d/src" "$v"
  javac -d "$d/out" "$d"/src/p/*.java
  case "$v" in
    hostile) printf 'junk' > "$d/out/-evil.class" ;;
    corrupt) printf 'this is not a class file' > "$d/out/p/Garbage.class" ;;
  esac
  ( cd "$d/aar" && jar cf classes.jar -C "$d/out" . && jar cfM "$FX/$n.aar" classes.jar )
}
build_aar base base
build_aar same base
build_aar add add
build_aar rm rm
build_aar nocs nocs
build_aar nofactory nofactory
build_aar chg chg
build_aar nohid nohid
build_aar noprot noprot
build_aar rmmarker rmmarker
build_aar nosuper nosuper
build_aar final final
build_aar nohelper nohelper
build_aar hostile hostile
build_aar corrupt corrupt

OUT="$TMP/out.txt"; ERR="$TMP/err.txt"
run(){ # <expected-exit> <label> <args...>  (env prefix supplied by caller via env)
  local want="$1" label="$2"; shift 2
  set +e; "$@" >"$OUT" 2>"$ERR"; local rc=$?; set -e
  check "$rc" "$want" "$label"
}

# (a) identical AARs -> pass
run 0 "(a) identical AARs exit 0" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$OUT" '^ABI-ADDITIVE PASS: 0 missing public descriptors vs v0\.0\.0$' "(a) PASS line printed"
# (l) the SEAMS line names the seams that were set
check_grep "$OUT" '^SEAMS:.*BASELINE_AAR' "(l) SEAMS line names BASELINE_AAR"
check_grep "$OUT" '^SEAMS:.*SKIP_BUILD' "(l) SEAMS line names SKIP_BUILD"
check_grep "$OUT" '^SEAMS:.*MIN_LINES' "(l) SEAMS line names MIN_LINES"
check_grep "$OUT" '^BASELINE_AAR sha256=[0-9a-f]{64} source=override$' "baseline hash + source printed"

# (b) head adds a method and a class -> still passes (append-only)
run 0 "(b) additive head exit 0" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/add.aar" \
  bash tools/verify-binary-abi.sh v0.0.0

# (c) head removes Alpha.a() -> exit 3 naming the descriptor
run 3 "(c) removed method exit 3" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/rm.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'ABI-ADDITIVE FAIL \(lane 3\).*p\.Alpha#a\(\)V' "(c) stderr names p.Alpha#a()V"

# (c2) WR-05: a modifier-less class header must still own its members; removing p.Hidden.hid() names p.Hidden
run 3 "(c2) removed member of package-private class exit 3" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/nohid.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'ABI-ADDITIVE FAIL \(lane 3\).*p\.Hidden#hid\(\)V' "(c2) stderr names p.Hidden#hid()V"

# (c3) WR-02: removing a protected method is binary-breaking for subclassers -> exit 3
run 3 "(c3) removed protected method exit 3" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/noprot.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'ABI-ADDITIVE FAIL \(lane 3\).*p\.Prot#prot\(I\)V' "(c3) stderr names p.Prot#prot(I)V"

# (c4) WR-01: class-header facts are compared even when the class has no members
run 3 "(c4) removed member-less interface exit 3" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/rmmarker.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'ABI-ADDITIVE FAIL \(lane 3\).*p\.Marker#@interface' "(c4) stderr names p.Marker#@interface"
run 3 "(c4) dropped supertype exit 3" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/nosuper.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'ABI-ADDITIVE FAIL \(lane 3\).*p\.Sub#@super p\.Marker' "(c4) stderr names the dropped supertype"
run 3 "(c4) open class made final exit 3" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/final.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'ABI-ADDITIVE FAIL \(lane 3\).*p\.Open#@nonfinal' "(c4) stderr names p.Open#@nonfinal"
# the reverse direction (final -> open, supertype added) is additive and must pass
run 0 "(c4) final class opened up exit 0" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/final.aar" HEAD_AAR="$FX/base.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
run 0 "(c4) supertype added exit 0" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/nosuper.aar" HEAD_AAR="$FX/base.aar" \
  bash tools/verify-binary-abi.sh v0.0.0

# (d) ComposableSingletons class dropped -> pass, counted as filtered
run 0 "(d) ComposableSingletons loss ignored" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/nocs.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$OUT" 'filtered_ComposableSingletons=[1-9][0-9]* missing=0$' "(d) filtered count > 0, missing=0"

# (e) *_Factory class dropped -> pass (excluded class)
run 0 "(e) Foo_Factory loss ignored" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/nofactory.aar" \
  bash tools/verify-binary-abi.sh v0.0.0

# (e2) IN-01: only Dagger-style suffixes are excluded; Foo_FactoryHelper dropped -> exit 3
run 3 "(e2) Foo_FactoryHelper loss detected" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/nohelper.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'ABI-ADDITIVE FAIL \(lane 3\).*p\.Foo_FactoryHelper#h\(\)V' "(e2) stderr names p.Foo_FactoryHelper#h()V"

# (f) sanity floor above fixture size -> exit 2
run 2 "(f) sanity floor exit 2" env SKIP_BUILD=1 MIN_LINES=100000 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" '^ABI FAIL: .*sanity floor' "(f) stderr names the sanity floor"

# (g) usage / precondition errors -> exit 1
run 1 "(g) unresolvable tag exit 1" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh no-such-tag
run 1 "(g) no argument exit 1" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh
run 1 "(g) hyphen-leading tag exit 1" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh -v0.0.0

run 1 "(g) branch name rejected as baseline exit 1" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh main
run 1 "(g) HEAD rejected as baseline exit 1" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh HEAD

# (g2) dirty artifact inputs (WR-03): an UNTRACKED file under src/ would be compiled into the AAR -> exit 1
#      before any build is attempted (no SKIP_BUILD here, so the dirty-input check is the code under test).
mkdir -p src/main; printf 'val u = 1\n' > src/main/Untracked.kt
run 1 "(g2) untracked src file exit 1" env MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'artifact inputs are dirty' "(g2) stderr says artifact inputs are dirty"
check_grep "$ERR" 'src/main/Untracked\.kt' "(g2) stderr names the untracked file"
rm -rf src

# (h) baseline cannot be obtained -> exit 2
run 2 "(h) BASELINE_AAR missing file exit 2" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/does-not-exist.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
mkdir -p "$TMP/empty-gradle"
run 2 "(h) no cache + dead JitPack exit 2" env SKIP_BUILD=1 MIN_LINES=1 HEAD_AAR="$FX/same.aar" \
  GRADLE_USER_HOME="$TMP/empty-gradle" JITPACK_BASE="http://127.0.0.1:1" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'BASELINE_AAR' "(h) failure message names the BASELINE_AAR fallback"

# (i) changed descriptor b(int) -> b(long) is a removal of the old descriptor -> exit 3
run 3 "(i) changed descriptor exit 3" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/chg.aar" \
  bash tools/verify-binary-abi.sh v0.0.0

# (j) hostile class entry name (leading hyphen) -> exit 2, never reaches javap
run 2 "(j) hostile entry name exit 2" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/hostile.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
check_grep "$ERR" 'unsafe' "(j) stderr says unsafe entry name"

# (k) a garbage class entry -> javap error -> exit 2, never a silent pass
run 2 "(k) corrupt class entry exit 2" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/corrupt.aar" \
  bash tools/verify-binary-abi.sh v0.0.0

# with seams set the output must never claim "SEAMS: none"
run 0 "seam-set run still exits 0" env SKIP_BUILD=1 MIN_LINES=1 BASELINE_AAR="$FX/base.aar" HEAD_AAR="$FX/same.aar" \
  bash tools/verify-binary-abi.sh v0.0.0
if grep -q '^SEAMS: none$' "$OUT"; then echo "FAIL: SEAMS reported none while seams were set"; fail=$((fail+1)); else pass=$((pass+1)); fi

echo "PASS=$pass FAIL=$fail"; [ "$fail" -eq 0 ]
