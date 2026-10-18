#!/usr/bin/env bash
# Pre-push facts and scan for an unpushed range (Phase 29, plan 29-04).
#
# Everything in <base>..<head> becomes public when the owner pushes, so this tool produces, in one
# plain command: the range facts (commit count, fast-forward, authors, binary files, changed paths
# outside the allowlist) and a scan of the ADDED lines for four classes:
#   tailnet_ip  a 100.x.y.z address        ts_net  the tailnet host suffix
#   home_path   the host user's home path  credential  API keys, tokens, private keys
# The scan runs twice: over the net diff (<base>..<head>) and, separately, over every commit in
# `git log -p <base>..<head>` (a hit added and later removed still ships in history).
#
# READ-ONLY against the repository (git diff, git log, git rev-parse, git merge-base only), no
# network. It writes only under --out (range.diff, range-log.txt, authors.txt, binary-files.txt) plus a private scratch
# directory made with mktemp and removed on exit. A matched credential is NEVER printed: credential
# hits print the class, the path and a count only. range.diff is the text that was scanned, so it can
# hold a credential if one was added; keep --out untracked.
#
# Every pattern and every positive-control sample below is assembled from fragments, so this file
# contains no whole match of its own classes (the next scan would otherwise flag the tool).
#
# Usage: verify-publish-range.sh --base <ref> --head <ref> --out <dir>
#          [--diff-file <path>] [--history-file <path>]
#   --diff-file / --history-file are TEST overrides: scan that text instead of `git diff` /
#   `git log -p` (the fixture uses them to feed a credential-shaped line without committing one).
#   env PUBLISH_RANGE_HOST_USER   the user name in the home-path class (default: the account running the tool)
#   env PUBLISH_RANGE_TEST_BLANK_CLASS  TEST hook: replace one class pattern with a never-matching
#                                 one so its positive control must fail
#
# Output lines, in order:
#   RANGE <base7>..<head7> commits=<n>
#   FASTFORWARD yes|no            (base is an ancestor of head)
#   AUTHORS distinct=<n>
#   BINARY files=<n>              files git cannot show as text (counted, not text-scanned; the
#                                 paths are listed in <out>/binary-files.txt)
#   OUTSIDE <path> ... / OUTSIDE count=<n>   changed paths outside the allowlist
#   CONTROL <class> ok|FAILED     positive control run BEFORE scanning
#   NET <class> <path> count=<n> area=planning|shipped
#   HISTORY <class> <sha7> <path> area=planning|shipped
#   SUMMARY net_credential=.. net_shipped=.. net_planning=.. history_credential=..
#           history_shipped=.. history_planning=.. outside=.. binary=.. fastforward=..
#           (net_* sum the hits; history_* count the unique class/commit/path lines)
#   CAVEAT history_shipped=<n> ... / CAVEAT binary=<n> ...   only when RESULT would be OK but that
#                                 class is not enforced (printed just before the RESULT line)
#   RESULT OK | RESULT OK (history_shipped=<n> binary=<n> not enforced) | RESULT REVIEW | RESULT INVALID
#
# area=planning is a path under .planning/ (prose and evidence); anything else is shipped.
# Exit: 0 RESULT OK: no credential (net or history), no shipped-path hit in the net diff, nothing
#         outside the allowlist, and the base is an ancestor of the head. Hits under .planning/ and
#         history-only shipped hits are reported but do not fail the scan. When history_shipped or
#         binary is above 0 the verdict line reads "RESULT OK (history_shipped=<n> binary=<n> not
#         enforced)" and CAVEAT lines precede it: a bare "RESULT OK" means those two were 0;
#       1 RESULT REVIEW: a human decision is needed;
#       2 usage error, or a failed positive control (RESULT INVALID: the scan itself is invalid).
set -euo pipefail
export LC_ALL=C

die(){ echo "ERROR: $*" >&2; exit 2; }

BASE=""; HEAD_REF=""; OUTDIR=""; DIFF_FILE=""; HIST_FILE=""
while [ "$#" -gt 0 ]; do
  case "$1" in
    --base) [ "$#" -ge 2 ] || die "option --base needs a value"; BASE="$2"; shift 2 ;;
    --head) [ "$#" -ge 2 ] || die "option --head needs a value"; HEAD_REF="$2"; shift 2 ;;
    --out) [ "$#" -ge 2 ] || die "option --out needs a value"; OUTDIR="$2"; shift 2 ;;
    --diff-file) [ "$#" -ge 2 ] || die "option --diff-file needs a value"; DIFF_FILE="$2"; shift 2 ;;
    --history-file) [ "$#" -ge 2 ] || die "option --history-file needs a value"; HIST_FILE="$2"; shift 2 ;;
    *) die "unknown option: $1" ;;
  esac
done
[ -n "$BASE" ] || die "--base is required"
[ -n "$HEAD_REF" ] || die "--head is required"
[ -n "$OUTDIR" ] || die "--out is required"
REF_RE='^[A-Za-z0-9][A-Za-z0-9._/-]*$'
[[ "$BASE" =~ $REF_RE ]] || die "malformed --base: $BASE"
[[ "$HEAD_REF" =~ $REF_RE ]] || die "malformed --head: $HEAD_REF"
git rev-parse --git-dir >/dev/null 2>&1 || die "not inside a git repository"
BASE_SHA=$(git rev-parse --verify --quiet "${BASE}^{commit}") || die "cannot resolve --base: $BASE"
HEAD_SHA=$(git rev-parse --verify --quiet "${HEAD_REF}^{commit}") || die "cannot resolve --head: $HEAD_REF"
[ -z "$DIFF_FILE" ] || [ -r "$DIFF_FILE" ] || die "cannot read --diff-file: $DIFF_FILE"
[ -z "$HIST_FILE" ] || [ -r "$HIST_FILE" ] || die "cannot read --history-file: $HIST_FILE"

mkdir -p "$OUTDIR"
SCRATCH=$(mktemp -d); trap 'rm -rf "$SCRATCH"' EXIT

GIT_DIFF_OPTS=(-c core.quotepath=false diff --no-color --no-ext-diff --no-textconv --src-prefix=a/ --dst-prefix=b/)
RANGE_SPEC="$BASE_SHA..$HEAD_SHA"

# ---- range artifacts and facts ----
if [ -n "$DIFF_FILE" ]; then cp -- "$DIFF_FILE" "$OUTDIR/range.diff"
else git "${GIT_DIFF_OPTS[@]}" -U0 "$RANGE_SPEC" > "$OUTDIR/range.diff"; fi
git log --oneline "$RANGE_SPEC" > "$OUTDIR/range-log.txt"
git log --format=%an "$RANGE_SPEC" | sort -u > "$OUTDIR/authors.txt"

COMMITS=$(git rev-list --count "$RANGE_SPEC")
echo "RANGE ${BASE_SHA:0:7}..${HEAD_SHA:0:7} commits=$COMMITS"
if git merge-base --is-ancestor "$BASE_SHA" "$HEAD_SHA"; then FF=yes; else FF=no; fi
echo "FASTFORWARD $FF"
AUTHORS=$(grep -c . "$OUTDIR/authors.txt" || true)
echo "AUTHORS distinct=$AUTHORS"

BIN_LIST=$(git "${GIT_DIFF_OPTS[@]}" --numstat "$RANGE_SPEC" | awk -F'\t' '$1 == "-" && $2 == "-" { print $3 }')
BINARY=$(printf '%s' "$BIN_LIST" | grep -c . || true)
echo "BINARY files=$BINARY"
printf '%s\n' "$BIN_LIST" | grep . > "$OUTDIR/binary-files.txt" || : > "$OUTDIR/binary-files.txt"

OUTSIDE=0
while IFS= read -r p; do
  [ -n "$p" ] || continue
  case "$p" in
    .planning/*|src/*|tools/*|docs/*) ;;
    api.txt|API.md|INTEGRATION.md|README.md|CLAUDE.md|ECOSYSTEM.md|AGENT-DEVICE-TESTING.md) ;;
    *) echo "OUTSIDE $p"; OUTSIDE=$((OUTSIDE+1)) ;;
  esac
done < <(git "${GIT_DIFF_OPTS[@]}" --no-renames --name-only "$RANGE_SPEC")
echo "OUTSIDE count=$OUTSIDE"

# ---- the four classes, assembled from fragments ----
HOST_USER="${PUBLISH_RANGE_HOST_USER:-${USER:-$(id -un)}}"
P_TAILNET='(^|[^0-9.])'"1""00"'\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}'
P_TSNET='\.ts''\.net'
P_HOME='/ho''me/'"$HOST_USER"
CRED_PATS=(
  '(^|[^A-Za-z0-9])s''k-[A-Za-z0-9_-]{16,}'
  'AI''za[0-9A-Za-z_-]{30,}'
  'gh''[pousr]_[A-Za-z0-9]{30,}'
  'github_''pat_[A-Za-z0-9_]{30,}'
  'xo''x[baprs]-[A-Za-z0-9-]{10,}'
  'Author''ization: Bearer [A-Za-z0-9._-]{20,}'
  'BE''GIN [A-Z ]*PRIVATE KEY'
  'AK''IA[0-9A-Z]{16}'
)
P_CRED=""
for p in "${CRED_PATS[@]}"; do P_CRED="${P_CRED:+$P_CRED|}$p"; done

# Test hook: a class pattern that can never match, so its positive control must fail.
NEVER='x^x'
case "${PUBLISH_RANGE_TEST_BLANK_CLASS:-}" in
  "") ;;
  tailnet_ip) P_TAILNET="$NEVER" ;;
  ts_net) P_TSNET="$NEVER" ;;
  home_path) P_HOME="$NEVER" ;;
  credential) P_CRED="$NEVER"; CRED_PATS=("$NEVER") ;;
  *) die "unknown PUBLISH_RANGE_TEST_BLANK_CLASS value" ;;
esac

# ---- positive controls: each class must match a synthetic line built at runtime ----
count_matches(){ printf '%s\n' "$2" | grep -aEc -e "$1" || true; }
ctl_fail=0
control(){ # <class> <pattern> <sample>
  if [ "$(count_matches "$2" "$3")" = "1" ]; then echo "CONTROL $1 ok"
  else echo "CONTROL $1 FAILED"; ctl_fail=1; fi
}
F20=$(printf 'a%.0s' $(seq 1 20)); F30=$(printf 'b%.0s' $(seq 1 30)); U16=$(printf 'Q%.0s' $(seq 1 16))
control tailnet_ip "$P_TAILNET" "addr=1""00.64.1.9:22"
control ts_net "$P_TSNET" "host=box.ts"".net"
control home_path "$P_HOME" "cd /ho""me/$HOST_USER/work"
CRED_SAMPLES=(
  "key=s""k-$F20"
  "AI""za$F30"
  "gh""p_$F30"
  "github_""pat_$F30"
  "xo""xb-$F20"
  "Author""ization: Bearer $F30"
  "-----BE""GIN RSA PRIVATE KEY-----"
  "AK""IA$U16"
)
cred_ok=1
for i in "${!CRED_PATS[@]}"; do
  [ "$(count_matches "${CRED_PATS[$i]}" "${CRED_SAMPLES[$i]}")" = "1" ] || cred_ok=0
done
if [ "$cred_ok" = "1" ]; then echo "CONTROL credential ok"; else echo "CONTROL credential FAILED"; ctl_fail=1; fi
if [ "$ctl_fail" -ne 0 ]; then
  echo "RESULT INVALID"
  exit 2
fi

# ---- added-line extraction: aligned files <prefix>.meta (sha<TAB>path) and <prefix>.text ----
extract(){ # <diff-or-log text file> <prefix>
  awk -v meta="$2.meta" -v text="$2.text" '
    BEGIN { sha = "-"; path = ""; inhdr = 0 }
    /^commit:/ { sha = substr($0, 8, 7); inhdr = 0; next }
    /^diff --git / { inhdr = 1; path = ""; next }
    inhdr && /^\+\+\+ / {
      p = substr($0, 5)
      if (p == "/dev/null") { path = "" } else { sub(/^b\//, "", p); sub(/\t.*$/, "", p); path = p }
      next
    }
    /^@@ / { inhdr = 0; next }
    !inhdr && substr($0, 1, 1) == "+" { print sha "\t" path > meta; print substr($0, 2) > text }
  ' "$1"
  : >> "$2.meta"; : >> "$2.text"
}
extract "$OUTDIR/range.diff" "$SCRATCH/net"
if [ -n "$HIST_FILE" ]; then cp -- "$HIST_FILE" "$SCRATCH/hist.raw"
else git -c core.quotepath=false log -p -U0 --no-color --no-ext-diff --no-textconv --no-renames \
  --src-prefix=a/ --dst-prefix=b/ --format='commit:%H' "$RANGE_SPEC" > "$SCRATCH/hist.raw"
fi
extract "$SCRATCH/hist.raw" "$SCRATCH/hist"

area(){ case "$1" in .planning/*) echo planning ;; *) echo shipped ;; esac; }

# hit_lines <prefix> <pattern>: print the 1-based numbers of the added lines matching the pattern.
hit_lines(){ { grep -anE -e "$2" "$1.text" || true; } | cut -d: -f1; }

NET_CRED=0; NET_SHIPPED=0; NET_PLANNING=0; H_CRED=0; H_SHIPPED=0; H_PLANNING=0
scan_class(){ # <class> <pattern>
  local cls="$1" pat="$2" nums="$SCRATCH/nums" path n a sha
  # net
  hit_lines "$SCRATCH/net" "$pat" > "$nums"
  if [ -s "$nums" ]; then
    while IFS=$'\t' read -r path n; do
      a=$(area "$path")
      echo "NET $cls $path count=$n area=$a"
      if [ "$cls" = "credential" ]; then NET_CRED=$((NET_CRED+n)); fi
      if [ "$a" = "planning" ]; then NET_PLANNING=$((NET_PLANNING+n)); else NET_SHIPPED=$((NET_SHIPPED+n)); fi
    done < <(awk -F'\t' 'NR==FNR { w[$1] = 1; next } (FNR in w) { c[$2]++ } END { for (p in c) print p "\t" c[p] }' "$nums" "$SCRATCH/net.meta" | sort)
  fi
  # history
  hit_lines "$SCRATCH/hist" "$pat" > "$nums"
  if [ -s "$nums" ]; then
    while IFS=$'\t' read -r sha path; do
      a=$(area "$path")
      echo "HISTORY $cls $sha $path area=$a"
      if [ "$cls" = "credential" ]; then H_CRED=$((H_CRED+1)); fi
      if [ "$a" = "planning" ]; then H_PLANNING=$((H_PLANNING+1)); else H_SHIPPED=$((H_SHIPPED+1)); fi
    done < <(awk -F'\t' 'NR==FNR { w[$1] = 1; next } (FNR in w) { k[$1 "\t" $2] = 1 } END { for (x in k) print x }' "$nums" "$SCRATCH/hist.meta" | sort)
  fi
}
scan_class tailnet_ip "$P_TAILNET"
scan_class ts_net "$P_TSNET"
scan_class home_path "$P_HOME"
scan_class credential "$P_CRED"

echo "SUMMARY net_credential=$NET_CRED net_shipped=$NET_SHIPPED net_planning=$NET_PLANNING history_credential=$H_CRED history_shipped=$H_SHIPPED history_planning=$H_PLANNING outside=$OUTSIDE binary=$BINARY fastforward=$FF"

if [ "$NET_CRED" -eq 0 ] && [ "$H_CRED" -eq 0 ] && [ "$NET_SHIPPED" -eq 0 ] && [ "$OUTSIDE" -eq 0 ] && [ "$FF" = "yes" ]; then
  # OK is not a clean bill for two classes the scan cannot enforce: say so on the verdict line itself,
  # so a caller reading only the last line or the exit code still sees the exposure.
  if [ "$H_SHIPPED" -gt 0 ] || [ "$BINARY" -gt 0 ]; then
    [ "$H_SHIPPED" -eq 0 ] || echo "CAVEAT history_shipped=$H_SHIPPED shipped-path hits were added and later removed; they still ship in history (see the HISTORY lines)"
    [ "$BINARY" -eq 0 ] || echo "CAVEAT binary=$BINARY files were not text-scanned (see $OUTDIR/binary-files.txt); review them by eye"
    echo "RESULT OK (history_shipped=$H_SHIPPED binary=$BINARY not enforced)"
    exit 0
  fi
  echo "RESULT OK"
  exit 0
fi
echo "RESULT REVIEW"
exit 1
