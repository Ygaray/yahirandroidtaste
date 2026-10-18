#!/usr/bin/env bash
# Fixture test for tools/verify-publish-range.sh (Phase 29, plan 29-04). Fully offline.
#
# Every temp repository is built with git plumbing (add, write-tree, commit-tree, update-ref), which
# runs no commit hook, so a global pre-commit scanner is never involved. Every synthetic hit string
# is assembled from fragments at runtime, so THIS file contains no whole match of any scan class
# (the next scan would otherwise flag the fixture itself). The credential case never touches a repo:
# it feeds the tool's --diff-file / --history-file test overrides.
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
SCRIPT="$DIR/verify-publish-range.sh"
[ -f "$SCRIPT" ] || { echo "FAIL: script under test is missing: $SCRIPT"; exit 1; }

# Isolate from the host git configuration (hooks path, signing, default branch).
export GIT_CONFIG_GLOBAL=/dev/null GIT_CONFIG_NOSYSTEM=1
export GIT_AUTHOR_EMAIL=t@example.invalid GIT_COMMITTER_EMAIL=t@example.invalid

# The home-path fixtures use a fixed user name; the tool's own default is the running account.
export PUBLISH_RANGE_HOST_USER=yahir

TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT

pass=0; fail=0
check(){ if [ "$1" = "$2" ]; then pass=$((pass+1)); else echo "FAIL: $3 (got $1 want $2)"; fail=$((fail+1)); fi; }
check_grep(){ # <text> <pattern> <label>
  if printf '%s\n' "$1" | grep -Eq -- "$2"; then pass=$((pass+1)); else echo "FAIL: $3 (pattern '$2' not in output)"; fail=$((fail+1)); fi
}
check_nogrep(){ # <text> <pattern> <label>
  if printf '%s\n' "$1" | grep -Eq -- "$2"; then echo "FAIL: $3 (pattern '$2' present in output)"; fail=$((fail+1)); else pass=$((pass+1)); fi
}

# ---- synthetic hit strings, assembled from fragments ----
O1=100; O2=64; O3=1; O4=9
ADDR="$O1.$O2.$O3.$O4"                       # tailnet-shaped address
HOSTSUF="box.ts"".net"                        # host suffix (two halves)
HOMEP="/ho""me/yahir/work"                   # home path (two halves)
FILL=$(printf 'a%.0s' $(seq 1 24))
CRED="s""k-$FILL"                            # credential-shaped text

# ---- temp repo builders (plumbing only) ----
mkrepo(){ git init -q -b main "$1"; }
mkcommit(){ # <repo> <message> [author-name]   commits the whole working tree on refs/heads/main
  local r="$1" msg="$2" an="${3:-Tester One}" tree parent sha
  git -C "$r" add -A
  tree=$(git -C "$r" write-tree)
  parent=$(git -C "$r" rev-parse -q --verify refs/heads/main || true)
  if [ -n "$parent" ]; then
    sha=$(GIT_AUTHOR_NAME="$an" GIT_COMMITTER_NAME="$an" git -C "$r" commit-tree "$tree" -p "$parent" -m "$msg")
  else
    sha=$(GIT_AUTHOR_NAME="$an" GIT_COMMITTER_NAME="$an" git -C "$r" commit-tree "$tree" -m "$msg")
  fi
  git -C "$r" update-ref refs/heads/main "$sha"
  echo "$sha"
}
put(){ # <repo> <path> <content>
  mkdir -p "$(dirname "$1/$2")"; printf '%s\n' "$3" > "$1/$2"
}
newrepo(){ # <name>: a repo with a base commit; sets REPO; refs/heads/base = base commit
  REPO="$TMP/$1"; mkrepo "$REPO"
  put "$REPO" README.md "base"; put "$REPO" tools/a.sh "echo a"
  local s; s=$(mkcommit "$REPO" "base")
  git -C "$REPO" update-ref refs/heads/base "$s"
}
finish(){ git -C "$REPO" update-ref refs/heads/tip refs/heads/main; }

OUT=""; RC=0; N=0; OUTD=""
run(){ # <repo> [args...]  (default args: --base base --head tip --out <fresh dir>)
  local r="$1"; shift
  N=$((N+1)); OUTD="$TMP/out-$N"
  RC=0
  if [ "$#" -eq 0 ]; then set -- --base base --head tip --out "$OUTD"; fi
  OUT=$(cd "$r" && bash "$SCRIPT" "$@" 2>&1) || RC=$?
}

# 1. Clean range: two benign commits, allowlisted root files, docs/ and src/.
newrepo clean
put "$REPO" docs/x.md "docs"; put "$REPO" API.md "api"; put "$REPO" AGENT-DEVICE-TESTING.md "adt"
mkcommit "$REPO" "c1" >/dev/null
put "$REPO" src/main/K.kt "class K"; put "$REPO" tools/b.sh "echo b"
mkcommit "$REPO" "c2" >/dev/null; finish
run "$REPO"
check "$RC" "0" "clean range exits 0"
check_grep "$OUT" '^RANGE [0-9a-f]+\.\.[0-9a-f]+ commits=2$' "range line with two commits"
check_grep "$OUT" '^FASTFORWARD yes$' "fast-forward yes"
check_grep "$OUT" '^AUTHORS distinct=1$' "one distinct author"
for cls in tailnet_ip ts_net home_path credential; do check_grep "$OUT" "^CONTROL $cls ok\$" "control $cls ok"; done
check_grep "$OUT" '^OUTSIDE count=0$' "no path outside the allowlist"
check_grep "$OUT" '^BINARY files=0$' "no binary files"
check_grep "$OUT" '^SUMMARY net_credential=0 net_shipped=0 net_planning=0 history_credential=0 history_shipped=0 history_planning=0 outside=0 binary=0 fastforward=yes$' "summary is all zero"
check_grep "$OUT" '^RESULT OK$' "result OK"
check_nogrep "$OUT" '^CAVEAT ' "a clean range prints no CAVEAT line"
check "$([ -s "$OUTD/range.diff" ] && [ -s "$OUTD/range-log.txt" ] && [ -s "$OUTD/authors.txt" ] && echo yes)" "yes" "the three artifact files are written under --out"

# 2. A tailnet-shaped address in a .planning/ file: reported, classified, not a failure.
newrepo planning
put "$REPO" .planning/n.md "tester at $ADDR for the run"
mkcommit "$REPO" "p1" >/dev/null; finish
run "$REPO"
check "$RC" "0" "planning-area tailnet hit exits 0"
check_grep "$OUT" '^NET tailnet_ip \.planning/n\.md count=1 area=planning$' "planning hit line"
check_grep "$OUT" 'net_planning=1 ' "net_planning is 1"
check_grep "$OUT" 'net_shipped=0 ' "net_shipped stays 0"

# 3. The same shape in a shipped path fails the scan.
newrepo shipped
put "$REPO" tools/c.sh "serial=$ADDR:1496"
mkcommit "$REPO" "s1" >/dev/null; finish
run "$REPO"
check "$RC" "1" "shipped-area tailnet hit exits 1"
check_grep "$OUT" '^NET tailnet_ip tools/c\.sh count=1 area=shipped$' "shipped hit line"
check_grep "$OUT" '^RESULT REVIEW$' "result REVIEW"

# 4. A hit added in one commit and removed in the next: net clean, history reports it.
newrepo history
put "$REPO" tools/d.sh "serial=$ADDR:1496"
sha_add=$(mkcommit "$REPO" "h1")
put "$REPO" tools/d.sh "serial=none"
mkcommit "$REPO" "h2" >/dev/null; finish
run "$REPO"
check "$RC" "0" "history-only hit exits 0"
check_grep "$OUT" '^CAVEAT history_shipped=1 ' "history-only hit prints a CAVEAT line"
check_grep "$OUT" '^RESULT OK \(history_shipped=1 binary=0 not enforced\)$' "history-only hit result carries the caveat"
check_nogrep "$OUT" '^RESULT OK$' "history-only hit is not a bare RESULT OK"
check_grep "$OUT" "^HISTORY tailnet_ip ${sha_add:0:7} tools/d\\.sh area=shipped\$" "history line names the commit and path"
check_grep "$OUT" 'history_shipped=1 ' "history_shipped is 1"
check_grep "$OUT" 'net_shipped=0 ' "net is clean for a removed hit"
check_nogrep "$OUT" '^NET ' "no NET line for a removed hit"

# 5. The host-suffix and home-path classes behave like the tailnet class.
newrepo suffix
put "$REPO" tools/e.sh "host=$HOSTSUF"
mkcommit "$REPO" "x1" >/dev/null; finish
run "$REPO"
check "$RC" "1" "shipped host-suffix hit exits 1"
check_grep "$OUT" '^NET ts_net tools/e\.sh count=1 area=shipped$' "host-suffix hit line"
newrepo suffixp
put "$REPO" .planning/e.md "host=$HOSTSUF"
mkcommit "$REPO" "x2" >/dev/null; finish
run "$REPO"
check "$RC" "0" "planning host-suffix hit exits 0"
check_grep "$OUT" '^NET ts_net \.planning/e\.md count=1 area=planning$' "planning host-suffix line"
newrepo homep
put "$REPO" tools/f.sh "cd $HOMEP"
mkcommit "$REPO" "y1" >/dev/null; finish
run "$REPO"
check "$RC" "1" "shipped home-path hit exits 1"
check_grep "$OUT" '^NET home_path tools/f\.sh count=1 area=shipped$' "home-path hit line"
newrepo homepp
put "$REPO" .planning/f.md "cd $HOMEP"
mkcommit "$REPO" "y2" >/dev/null; finish
run "$REPO"
check "$RC" "0" "planning home-path hit exits 0"
check_grep "$OUT" '^NET home_path \.planning/f\.md count=1 area=planning$' "planning home-path line"

# 5b. Without the override the home-path class protects the account that runs the tool.
newrepo homeu
put "$REPO" tools/f2.sh "cd /ho""me/fixtureuser/work"
mkcommit "$REPO" "y3" >/dev/null; finish
N=$((N+1)); OUTD="$TMP/out-$N"; RC=0
OUT=$(cd "$REPO" && env -u PUBLISH_RANGE_HOST_USER USER=fixtureuser bash "$SCRIPT" --base base --head tip --out "$OUTD" 2>&1) || RC=$?
check "$RC" "1" "the default home-path user is the running account"
check_grep "$OUT" '^NET home_path tools/f2\.sh count=1 area=shipped$' "default-user home-path hit line"

# 6. A credential-shaped added line, supplied through the overrides, is never echoed.
CLEAN="$TMP/clean"
DIFFF="$TMP/cred.diff"
{
  echo "diff --git a/tools/x.sh b/tools/x.sh"
  echo "--- a/tools/x.sh"
  echo "+++ b/tools/x.sh"
  echo "@@ -0,0 +1 @@"
  echo "+token=$CRED"
} > "$DIFFF"
run "$CLEAN" --base base --head tip --out "$TMP/out-cred" --diff-file "$DIFFF"
CREDOUT="$OUT"; CREDOUTD="$TMP/out-cred"
check "$RC" "1" "a net credential exits 1"
check_grep "$CREDOUT" '^NET credential tools/x\.sh count=1 area=shipped$' "credential line names class and path only"
check_grep "$CREDOUT" 'net_credential=1 ' "net_credential is 1"
check "$(printf '%s' "$CREDOUT" | grep -c -F -- "$CRED" || true)" "0" "the credential text is absent from stdout"
check "$(grep -rl -F -- "$CRED" "$CREDOUTD" | sed "s#^$CREDOUTD/##" | sort | tr '\n' ' ')" "range.diff " "the credential text appears only in range.diff under --out"
# A credential only in history also needs a human (the strict reading of "no credential").
HISTF="$TMP/cred.hist"
{
  echo "commit:0123456789abcdef0123456789abcdef01234567"
  echo "diff --git a/tools/y.sh b/tools/y.sh"
  echo "+++ b/tools/y.sh"
  echo "@@ -0,0 +1 @@"
  echo "+token=$CRED"
} > "$HISTF"
run "$CLEAN" --base base --head tip --out "$TMP/out-credh" --history-file "$HISTF"
check "$RC" "1" "a history-only credential exits 1"
check_grep "$OUT" '^HISTORY credential 0123456 tools/y\.sh area=shipped$' "history credential line"
check "$(printf '%s' "$OUT" | grep -c -F -- "$CRED" || true)" "0" "the history credential text is absent from stdout"

# 6b. The sk- pattern needs a left boundary: ordinary hyphenated prose is not a credential, and the
# other GitHub token prefixes are covered.
PROSE_DIFF="$TMP/prose.diff"
{
  echo "diff --git a/docs/x.md b/docs/x.md"
  echo "--- a/docs/x.md"
  echo "+++ b/docs/x.md"
  echo "@@ -0,0 +3 @@"
  echo "+the risk-adjusted-assessment-of-things pass"
  echo "+a task-force-review-board-with-long-name"
} > "$PROSE_DIFF"
run "$CLEAN" --base base --head tip --out "$TMP/out-prose" --diff-file "$PROSE_DIFF"
check_nogrep "$OUT" '^NET credential ' "hyphenated prose ending in sk is not a credential"
check "$RC" "0" "hyphenated prose alone exits 0"
for tokpre in "gh""o_" "gh""s_" "gh""u_" "gh""r_" "github_""pat_"; do
  TOKDIFF="$TMP/tok.diff"
  {
    echo "diff --git a/tools/z.sh b/tools/z.sh"
    echo "--- a/tools/z.sh"
    echo "+++ b/tools/z.sh"
    echo "@@ -0,0 +1 @@"
    echo "+x=${tokpre}$(printf 'c%.0s' $(seq 1 36))"
  } > "$TOKDIFF"
  run "$CLEAN" --base base --head tip --out "$TMP/out-tok" --diff-file "$TOKDIFF"
  check "$RC" "1" "a $tokpre token exits 1"
  check_grep "$OUT" '^NET credential tools/z\.sh count=1 area=shipped$' "a $tokpre token is a credential hit"
done

# 7. A changed path outside the allowlist.
newrepo outside
put "$REPO" build.gradle.kts "plugins {}"
mkcommit "$REPO" "o1" >/dev/null; finish
run "$REPO"
check "$RC" "1" "a path outside the allowlist exits 1"
check_grep "$OUT" '^OUTSIDE build\.gradle\.kts$' "outside path line"
check_grep "$OUT" '^OUTSIDE count=1$' "outside count"

# 8. A base that is not an ancestor of the head.
newrepo nonff
put "$REPO" tools/g.sh "echo g"
mkcommit "$REPO" "n1" >/dev/null; finish
git -C "$REPO" update-ref refs/heads/base "$(git -C "$REPO" commit-tree "$(git -C "$REPO" write-tree)" -m unrelated)"
run "$REPO"
check "$RC" "1" "a non-ancestor base exits 1"
check_grep "$OUT" '^FASTFORWARD no$' "fast-forward no"

# 9. A binary file added in the range is counted, not text-scanned.
newrepo binary
mkdir -p "$REPO/docs"; printf 'AB\000\001\002CD\000' > "$REPO/docs/blob.bin"
mkcommit "$REPO" "b1" >/dev/null; finish
run "$REPO"
check "$RC" "0" "a binary file alone exits 0"
check_grep "$OUT" '^BINARY files=1$' "binary count"
check_grep "$OUT" '^CAVEAT binary=1 ' "binary file prints a CAVEAT line"
check_grep "$OUT" '^RESULT OK \(history_shipped=0 binary=1 not enforced\)$' "binary result carries the caveat"
check_nogrep "$OUT" '^RESULT OK$' "binary range is not a bare RESULT OK"

# 10. More than one author is counted.
newrepo authors
put "$REPO" tools/h.sh "echo h"; mkcommit "$REPO" "a1" "Tester One" >/dev/null
put "$REPO" tools/i.sh "echo i"; mkcommit "$REPO" "a2" "Tester Two" >/dev/null; finish
run "$REPO"
check_grep "$OUT" '^AUTHORS distinct=2$' "two distinct authors"

# 11. Usage errors exit 2 and evaluate nothing.
run "$CLEAN" --head tip --out "$TMP/u1"
check "$RC" "2" "missing --base exits 2"
check "$([ -e "$TMP/u1" ] && echo made || echo none)" "none" "a usage error creates no --out directory"
run "$CLEAN" --base base --out "$TMP/u2"
check "$RC" "2" "missing --head exits 2"
run "$CLEAN" --base base --head tip
check "$RC" "2" "missing --out exits 2"
run "$CLEAN" --base no-such-ref --head tip --out "$TMP/u3"
check "$RC" "2" "an unresolvable ref exits 2"
run "$CLEAN" --base 'base;id' --head tip --out "$TMP/u4"
check "$RC" "2" "a ref with a shell metacharacter exits 2"
run "$CLEAN" --base '-x' --head tip --out "$TMP/u5"
check "$RC" "2" "a ref starting with a dash exits 2"
run "$CLEAN" --base base --head tip --out "$TMP/u6" --bogus
check "$RC" "2" "an unknown option exits 2"
check_nogrep "$OUT" '^RESULT' "a usage error prints no RESULT line"
run "$CLEAN" --base base --head tip --out "$TMP/u7" --diff-file "$TMP/no-such-file"
check "$RC" "2" "an unreadable --diff-file exits 2"

# 12. A failed positive control invalidates the scan (exit 2), whichever class it is.
for cls in tailnet_ip ts_net home_path credential; do
  N=$((N+1)); OUTD="$TMP/out-$N"; RC=0
  OUT=$(cd "$CLEAN" && PUBLISH_RANGE_TEST_BLANK_CLASS="$cls" bash "$SCRIPT" --base base --head tip --out "$OUTD" 2>&1) || RC=$?
  check "$RC" "2" "a failed $cls control exits 2"
  check_grep "$OUT" "^CONTROL $cls FAILED\$" "failed control names $cls"
done

# 13. The tool and the fixtures add no hits of their own (the tool text must not match its own classes).
newrepo selfscan
mkdir -p "$REPO/tools/test"
cp "$DIR/verify-publish-range.sh" "$REPO/tools/"; cp "$DIR/test/test-verify-publish-range.sh" "$REPO/tools/test/"
mkcommit "$REPO" "self" >/dev/null; finish
run "$REPO"
check "$RC" "0" "the tool and its fixture scan clean against their own classes"
check_nogrep "$OUT" '^(NET|HISTORY) ' "no hit lines for the tool and fixture text"

# 14. The tool is read-only against the repository.
BEFORE=$(git -C "$CLEAN" for-each-ref | md5sum); STATUS_BEFORE=$(git -C "$CLEAN" status --porcelain | md5sum)
run "$CLEAN"
check "$(git -C "$CLEAN" for-each-ref | md5sum)" "$BEFORE" "refs are unchanged by a run"
check "$(git -C "$CLEAN" status --porcelain | md5sum)" "$STATUS_BEFORE" "the working tree is unchanged by a run"

echo "PASS=$pass FAIL=$fail"
[ "$fail" -eq 0 ]
