#!/usr/bin/env bash
# verify-binary-abi.sh <baseline-tag> - binary (JVM descriptor) additivity gate for the release AAR (D-03).
#
# Why: Metalava apiCheck is a SOURCE-level gate and is blind to Compose $default / $changed synthetics and
# data-class synthetics (INC-2026-10-05-02). This gate runs `javap -public -s` over every class in the
# release AAR's classes.jar at HEAD and in the baseline tag's AAR, normalizes both to sorted unique
# `class#member descriptor` lines, and diffs append-only: every public descriptor present in the baseline
# must still exist at HEAD.
#
# Usage:   tools/verify-binary-abi.sh <baseline-tag>        e.g. tools/verify-binary-abi.sh v2.4.1
# Exit codes:
#   0  PASS    zero baseline descriptors are missing at HEAD
#   1  usage / precondition (no argument, malformed tag or a name that is not an existing git tag, dirty artifact inputs when this
#      script builds)
#   2  tool, sanity-floor, javap-error, unsafe-entry-name or baseline-resolution failure
#   3  lane 3: at least one public descriptor present in the baseline is missing at HEAD. STOP, never waive.
#
# Baseline AAR resolution order: BASELINE_AAR (must be an existing file, no fall-through) ->
#   Gradle cache copy of the tag -> HTTPS download from JitPack.
# Excluded from the comparison: Dagger *_Factory and *_MembersInjector classes; missing ComposableSingletons$*
#   lines are ignored (counted and reported as filtered_ComposableSingletons).
# Sanity floor: both normalized listings must hold at least MIN_LINES lines (default 2000); any javap stderr
#   output or non-zero javap exit is a hard failure, so an empty or partial javap run can never pass.
# Test/diagnostic seams (fixture test only; the release cut MUST run with NONE set, and the SEAMS line proves it):
#   BASELINE_AAR, HEAD_AAR, SKIP_BUILD, MIN_LINES, JITPACK_BASE
# RULE: run this on the exact tagged HEAD, before tagging.
set -euo pipefail

SEAMS=""
for v in BASELINE_AAR HEAD_AAR SKIP_BUILD MIN_LINES JITPACK_BASE; do
  [ -n "${!v:-}" ] && SEAMS="$SEAMS $v"
done
if [ -z "$SEAMS" ]; then echo "SEAMS: none"; else echo "SEAMS:$SEAMS"; fi

die() { # <exit-code> <message>
  echo "ABI FAIL: $2" >&2
  exit "$1"
}

[ "$#" -ge 1 ] || die 1 "usage: tools/verify-binary-abi.sh <baseline-tag>"
TAG="$1"
printf '%s' "$TAG" | grep -Eq '^[A-Za-z0-9][A-Za-z0-9._/-]*$' || die 1 "malformed baseline tag '$TAG'"

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || die 1 "not inside a git repository"
cd "$ROOT"
# Must be a real tag (refs/tags/), never a branch or HEAD: JitPack resolves the literal string as the version
# and moving refs are forbidden as baselines.
git rev-parse --verify --quiet "refs/tags/$TAG^{commit}" >/dev/null || die 1 "baseline '$TAG' is not an existing git tag"
for t in javap unzip curl sha256sum; do
  command -v "$t" >/dev/null || die 2 "$t not found (need a JDK 17 + unzip + curl)"
done

MIN_LINES="${MIN_LINES:-2000}"
W="$(mktemp -d)"; trap 'rm -rf "$W"' EXIT

# Build the HEAD release AAR (unless a seam supplies it); refuse if artifact inputs are dirty.
if [ -z "${SKIP_BUILD:-}" ]; then
  DIRTY="$(git status --porcelain --untracked-files=all -- src build.gradle.kts settings.gradle.kts gradle.properties gradle config api.txt jitpack.yml)"
  [ -z "$DIRTY" ] || die 1 "artifact inputs are dirty; the AAR would not reflect the commit:
$DIRTY"
  # Gradle compiles ignored files under src/ too; they are invisible to `git status` but still reach the AAR.
  IGNORED="$(git ls-files --others --ignored --exclude-standard -- src)"
  [ -z "$IGNORED" ] || die 1 "ignored files under src/ would be compiled into the AAR:
$IGNORED"
  ./gradlew assembleRelease -q >&2 || die 2 "./gradlew assembleRelease failed"
fi
echo "HEAD=$(git rev-parse HEAD)"

HEAD_AAR="${HEAD_AAR:-$ROOT/build/outputs/aar/yahirandroidtaste-release.aar}"
[ -f "$HEAD_AAR" ] || die 2 "HEAD AAR not found at $HEAD_AAR"
echo "HEAD_AAR sha256=$(sha256sum "$HEAD_AAR" | cut -d' ' -f1) path=$HEAD_AAR"

# Resolve the baseline AAR.
BASE_SRC=""
BASE_AAR="${BASELINE_AAR:-}"
if [ -n "$BASE_AAR" ]; then
  [ -f "$BASE_AAR" ] || die 2 "BASELINE_AAR '$BASE_AAR' is not an existing file"
  BASE_SRC="override"
else
  BASE_AAR="$(ls "${GRADLE_USER_HOME:-$HOME/.gradle}"/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/"$TAG"/*/yahirandroidtaste-"$TAG".aar 2>/dev/null | sort | head -1 || true)"
  if [ -n "$BASE_AAR" ]; then
    BASE_SRC="gradle-cache"
  else
    BASE_AAR="$W/baseline.aar"; BASE_SRC="jitpack"
    URL_BASE="${JITPACK_BASE:-https://jitpack.io}"
    PROTO=(--proto '=https' --proto-redir '=https')
    [ -z "${JITPACK_BASE:-}" ] || PROTO=()
    curl -fsSL --max-time 120 "${PROTO[@]}" -o "$BASE_AAR" \
      "$URL_BASE/com/github/Ygaray/yahirandroidtaste/$TAG/yahirandroidtaste-$TAG.aar" \
      || die 2 "cannot obtain baseline AAR for $TAG (no Gradle cache copy, JitPack unreachable); set BASELINE_AAR=<path> as the manual fallback"
  fi
fi
echo "BASELINE_AAR sha256=$(sha256sum "$BASE_AAR" | cut -d' ' -f1) source=$BASE_SRC"

normalize() { # <aar> <out-listing>
  local aar="$1" out="$2" d="$W/$(basename "$2" .txt)"
  mkdir -p "$d"
  # Extract exactly one named member, into the private workdir.
  unzip -q -o "$aar" classes.jar -d "$d" || die 2 "cannot extract classes.jar from $aar"
  local entries
  entries="$(unzip -Z1 "$d/classes.jar")" || die 2 "cannot list classes.jar of $aar"
  local names=() e n
  while IFS= read -r e; do
    case "$e" in *.class) ;; *) continue ;; esac
    n="${e%.class}"; n="${n//\//.}"
    case "$n" in *_Factory*|*_MembersInjector*) continue ;; esac
    printf '%s' "$n" | grep -Eq '^[A-Za-z0-9_$][A-Za-z0-9_$.-]*$' \
      || die 2 "unsafe class entry name in $aar: '$e'"
    names+=("$n")
  done <<< "$entries"
  [ "${#names[@]}" -gt 0 ] || die 2 "no class survives the filters in $aar"
  local rc=0
  javap -public -s -cp "$d/classes.jar" "${names[@]}" >"$d/javap.out" 2>"$d/javap.err" || rc=$?
  [ "$rc" -eq 0 ] || die 2 "javap exited $rc on $aar: $(head -3 "$d/javap.err")"
  [ ! -s "$d/javap.err" ] || die 2 "javap wrote to stderr on $aar: $(head -3 "$d/javap.err")"
  awk '
    /^([a-z]+ )*(class|interface) [^ ]+.*\{$/ { match($0, /(class|interface) [^ <{]+/); s=substr($0, RSTART, RLENGTH); sub(/^(class|interface) /, "", s); cls=s }
    /descriptor:/ {
      if (cls == "") { print "descriptor line before any class header: " $0 > "/dev/stderr"; exit 2 }
      n=prev; sub(/\(.*/, "", n); k=split(n, a, " "); print cls "#" a[k] $2 }
    { prev=$0 }' "$d/javap.out" | LC_ALL=C sort -u > "$out" \
    || die 2 "cannot attribute javap members to a class in $aar (unparsed class header)"
}
normalize "$BASE_AAR" "$W/base.txt"
normalize "$HEAD_AAR" "$W/head.txt"

nb="$(wc -l < "$W/base.txt")"; nh="$(wc -l < "$W/head.txt")"
{ [ "$nb" -ge "$MIN_LINES" ] && [ "$nh" -ge "$MIN_LINES" ]; } \
  || die 2 "sanity floor $MIN_LINES not met (base=$nb head=$nh)"

LC_ALL=C comm -23 "$W/base.txt" "$W/head.txt" > "$W/all-missing.txt"
grep -F 'ComposableSingletons$' "$W/all-missing.txt" > "$W/filtered.txt" || true
grep -vF 'ComposableSingletons$' "$W/all-missing.txt" > "$W/missing.txt" || true
echo "base=$nb head=$nh filtered_ComposableSingletons=$(wc -l < "$W/filtered.txt") missing=$(wc -l < "$W/missing.txt")"

if [ -s "$W/missing.txt" ]; then
  while IFS= read -r l; do
    echo "ABI-ADDITIVE FAIL (lane 3): public descriptor missing vs $TAG: $l" >&2
  done < "$W/missing.txt"
  exit 3
fi
echo "ABI-ADDITIVE PASS: 0 missing public descriptors vs $TAG"
