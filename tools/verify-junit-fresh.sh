#!/usr/bin/env bash
# Proves a Gradle JUnit result directory is FRESH, sums its totals and checks named guard classes
# ran (Phase 29, plan 29-04). READ-ONLY: it never writes, never touches the network and never runs
# Gradle. It only reads the TEST-*.xml files.
#
# "Fresh" means every TEST-*.xml is strictly newer than the marker file. The caller creates the marker
# with `touch` just before the forced Gradle run (for example with --rerun-tasks), so a result left
# over from an earlier build, or reused as up-to-date output, shows up as STALE and fails the check.
#
# Usage: verify-junit-fresh.sh <results-dir> --marker <file> [--require <ClassSimpleName>]...
#
# Output lines:
#   FILES <n>                                  result files found
#   STALE <n>                                  result files not newer than the marker
#   TOTALS tests=<n> skipped=<n> failures=<n> errors=<n>
#   CLASS <name> tests=<n> | CLASS <name> MISSING   (one per --require)
#   JUNIT-FRESH OK | JUNIT-FRESH FAIL <reason>[,<reason>...]
#
# A skipped test did not run: "ran" is counted as tests minus skipped, both in total and per class,
# so a class whose tests are all @Ignore'd (or all skipped by a failed assume) fails as zero_<Class>.
# The TOTALS and CLASS lines still print the raw tests= attribute.
#
# Exit: 0 fresh, executed tests above 0, no failures, no errors, every required class ran with
#         executed tests above 0;
#       1 any of those fails (the final line names every reason);
#       2 usage error (nothing is evaluated).
set -euo pipefail

die(){ echo "ERROR: $*" >&2; exit 2; }

DIR=""; MARKER=""; REQUIRED=()
while [ "$#" -gt 0 ]; do
  case "$1" in
    --marker) [ "$#" -ge 2 ] || die "option --marker needs a value"; MARKER="$2"; shift 2 ;;
    --require)
      [ "$#" -ge 2 ] || die "option --require needs a value"
      [[ "$2" =~ ^[A-Za-z0-9_]+$ ]] || die "invalid --require value (letters, digits and underscores only): $2"
      REQUIRED+=("$2"); shift 2 ;;
    -*) die "unknown option: $1" ;;
    *) [ -z "$DIR" ] || die "unexpected extra argument: $1"; DIR="$1"; shift ;;
  esac
done
[ -n "$DIR" ] || die "usage: verify-junit-fresh.sh <results-dir> --marker <file> [--require <Class>]..."
[ -n "$MARKER" ] || die "--marker is required"
[ -d "$DIR" ] || die "results directory does not exist: $DIR"
[ -f "$MARKER" ] || die "marker file does not exist: $MARKER"

FILES=$(find "$DIR" -maxdepth 1 -type f -name 'TEST-*.xml' | wc -l | tr -d ' ')
STALE=$(find "$DIR" -maxdepth 1 -type f -name 'TEST-*.xml' ! -newer "$MARKER" | wc -l | tr -d ' ')
echo "FILES $FILES"
echo "STALE $STALE"

# attrs <file>: print "tests skipped failures errors" read from the first <testsuite line.
attrs(){
  awk '
    /<testsuite / {
      line = $0
      split("tests skipped failures errors", names, " ")
      out = ""
      for (i = 1; i <= 4; i++) {
        v = 0
        if (match(line, " " names[i] "=\"[0-9]+\"")) {
          s = substr(line, RSTART, RLENGTH)
          gsub(/[^0-9]/, "", s)
          v = s
        }
        out = out (i > 1 ? " " : "") v
      }
      print out
      exit
    }
  ' "$1"
}

T=0; S=0; F=0; E=0
while IFS= read -r -d '' f; do
  read -r t s fl e <<< "$(attrs "$f")"
  T=$((T + ${t:-0})); S=$((S + ${s:-0})); F=$((F + ${fl:-0})); E=$((E + ${e:-0}))
done < <(find "$DIR" -maxdepth 1 -type f -name 'TEST-*.xml' -print0)
echo "TOTALS tests=$T skipped=$S failures=$F errors=$E"

REASONS=()
[ "$FILES" -gt 0 ] || REASONS+=("no_files")
[ "$STALE" -eq 0 ] || REASONS+=("stale")
if [ "$FILES" -gt 0 ] && [ $((T - S)) -le 0 ]; then REASONS+=("no_tests"); fi
[ "$F" -eq 0 ] || REASONS+=("failures")
[ "$E" -eq 0 ] || REASONS+=("errors")

for cls in "${REQUIRED[@]+"${REQUIRED[@]}"}"; do
  CT=""; CX=0; FOUND=0
  while IFS= read -r -d '' f; do
    FOUND=1
    read -r t sk _ <<< "$(attrs "$f")"
    CT=$(( ${CT:-0} + ${t:-0} ))
    CX=$(( CX + ${t:-0} - ${sk:-0} ))
  done < <(find "$DIR" -maxdepth 1 -type f \( -name "TEST-*.$cls.xml" -o -name "TEST-$cls.xml" \) -print0)
  if [ "$FOUND" -eq 0 ]; then
    echo "CLASS $cls MISSING"
    REASONS+=("missing_$cls")
  else
    echo "CLASS $cls tests=$CT"
    [ "$CX" -gt 0 ] || REASONS+=("zero_$cls")
  fi
done

if [ "${#REASONS[@]}" -eq 0 ]; then
  echo "JUNIT-FRESH OK"
  exit 0
fi
( IFS=,; echo "JUNIT-FRESH FAIL ${REASONS[*]}" )
exit 1
