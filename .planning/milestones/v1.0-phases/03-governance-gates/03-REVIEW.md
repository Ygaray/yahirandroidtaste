---
phase: 03-governance-gates
reviewed: 2026-09-01T00:00:00Z
depth: standard
files_reviewed: 6
files_reviewed_list:
  - tools/verify-additive-diff.sh
  - tools/test/test-verify-additive-diff.sh
  - tools/test/test-precommit-hook.sh
  - tools/test/test-verify-api-additive.sh
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
  - docs/DESIGN-INTENT.md
findings:
  critical: 1
  warning: 4
  info: 1
  total: 6
status: resolved
---

# Phase 03: Code Review Report

**Reviewed:** 2026-09-01T00:00:00Z
**Depth:** standard
**Files Reviewed:** 6
**Status:** issues_found

## Summary

Reviewed the GOV-03 pre-commit additive-guard fix (`tools/verify-additive-diff.sh` + its three
test fixtures) and the GOV-02 domain-vocabulary drift guard (`DomainVocabularyDriftGuardTest.kt` +
`docs/DESIGN-INTENT.md`).

The GOV-02 drift-guard test and its documentation are solid — the litmus is decidable, the worked
examples check out against the actual `CardBase`/`ChipBar`/`HeatSwatch` signatures in
`src/main`, and the vacuous-pass guards are real (verified the three-tier `assertTrue` sequence).

The GOV-03 fix, however, has a genuine regression that I reproduced live against the actual
script: the commit that switched the diff basis from "baseline vs. working tree" to "staged
index vs. HEAD" (5ea861d) left the **default path-enumeration** wired to the old baseline tree.
In the guard's real production call path (`hooks/pre-commit` → `classify-hub-change.sh` →
`verify-additive-diff.sh "$BASE"`, no explicit paths), this means any file created *after* the
last release tag is invisible to the guard for the rest of that tag's lifetime — a line-rewrite in
such a file is silently classified as clean. I reproduced this with a throwaway repo (see CR-01).
I also reproduced a second, narrower gap: blank-line-only removals are invisible to the guard
regardless of file scope, because the removed/added line regexes require a second character after
the diff marker.

## Critical Issues

### CR-01: DS-05 default path scope pins to the stale baseline tree, not HEAD — files created after the last tag are never protected by the append-only guard

**File:** `tools/verify-additive-diff.sh:58, 80`
**Issue:** The GOV-03 fix (commit 5ea861d, "switch verify-additive-diff.sh to staged-vs-HEAD diff
basis") changed the diff endpoints from `git diff "$BASELINE_COMMIT" -- paths` (baseline vs.
working tree) to `git diff --cached -U0 -- paths` (staged index vs. HEAD) — but did **not** update
the default `PATHS` enumeration, which is still `git ls-tree -r --name-only "$BASELINE_COMMIT" --
src/main` (line 58). Before the fix this was consistent (the diff itself was baseline-anchored, so
"files tracked at baseline" was the right universe). After the fix the diff is HEAD-anchored, so
the correct universe of "pre-existing files this commit must not rewrite a line in" is everything
tracked in `HEAD`, not everything tracked at the tag.

Net effect: any file added to `src/main` in a commit *after* the last release tag is completely
outside `PATHS` until the next tag is cut, so the guard's own `git diff --cached -- "${PATHS[@]}"`
never even looks at it. A later commit that rewrites a line in that file (not a pure append) is
silently classified as lane-1/clean by the pre-commit hook. This is exactly the class of change
the guard exists to catch, and it is the real production call path (`hooks/pre-commit` invokes
`verify-additive-diff.sh "$BASE"` with no explicit path args).

Reproduced live:
```
$ git init; ... commit A.kt; git tag v1.0.0
$ printf 'val b = 1\n' > src/main/B.kt; git commit -m "add B.kt (post-tag)"
$ sed -i 's/val b = 1/val b = 999/' src/main/B.kt; git add -A
$ tools/verify-additive-diff.sh v1.0.0
DS-05 PASS: 0 removed line(s), all accounted for by an identical added line (append-only)
```
The rewrite of `B.kt` (a file that did not exist at `v1.0.0`) is completely invisible — no removed
line is even reported, let alone flagged. Note also that this regression class is untested: none
of `test-verify-additive-diff.sh`'s three cases create a file in one commit and rewrite it in a
later commit against the same fixed baseline tag; all cases exercise a single staged delta against
files that were already tracked at the tag.

**Fix:** Enumerate the default path list from `HEAD`, not `$BASELINE_COMMIT` — the diff basis is
`--cached` vs `HEAD`, so "pre-existing" now means "tracked in HEAD":
```bash
# Default: every SOURCE file tracked as of HEAD (the diff's own base), not the baseline tag —
# the diff basis is staged-vs-HEAD (D-01), so a file created after the last tag but before this
# commit is still "pre-existing" from this commit's point of view and must be protected.
mapfile -d '' -t PATHS < <(git ls-tree -z -r --name-only HEAD -- src/main)
[ "${#PATHS[@]}" -gt 0 ] || { echo "DS-05 FAIL: HEAD has no tracked production source under src/main." >&2; exit 1; }
```
Also add a regression fixture to `test-verify-additive-diff.sh` mirroring the GOV-03 case in
`test-precommit-hook.sh`: create a new file in commit N (after the tag), rewrite one of its lines
in commit N+1, and assert the guard catches it (currently missing).

## Warnings

### WR-01: Blank-line-only removals are invisible to the append-only guard

**File:** `tools/verify-additive-diff.sh:83, 85`
**Issue:** `grep -E '^-[^-]'` and `grep -E '^\+[^+]'` both require a second character after the
diff marker to match. A removed (or added) *blank* line appears in unified diff output as a bare
`-` (or `+`) with nothing after it, which does not satisfy `[^-]`/`[^+]` (there is no second
character at all), so it is silently dropped from both `REMOVED_RAW` and `ADDED_RAW`. Reproduced:

```
$ printf 'line one\n\nline three\n' > src/main/A.kt; git commit; git tag v1.0.0
$ printf 'line one\nline three\n' > src/main/A.kt   # deletes the blank line, no replacement
$ git add -A
$ git diff --cached -U0 -- src/main/A.kt
@@ -2 +1,0 @@ line one
-
$ tools/verify-additive-diff.sh v1.0.0 src/main/A.kt
DS-05 PASS: 0 removed line(s), all accounted for by an identical added line (append-only)
```
A genuine content removal (deleting a blank line with no counterpart) passes the guard
unconditionally. Lower real-world risk than CR-01 (whitespace-only), but it is a real, provable
hole in the "no line removed without an identical counterpart" invariant the script's own header
comment promises.
**Fix:** Change both extraction regexes to also match a bare marker line, e.g. `grep -E '^-'` /
`grep -E '^\+'` and exclude the file-header lines (`---`/`+++`) by anchoring on the fact they only
ever appear as the first two lines of a per-file hunk block, or by filtering with `grep -v -E
'^(---|\+\+\+) '` instead of relying on the second-character trick.

### WR-02: `headToken()` mis-parses composable names with a leading acronym run

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt:119-127, 243`
**Issue:** `HEAD_TOKEN_REGEX = Regex("[A-Z][a-z0-9]*")` extracts only a single uppercase letter
when the name begins with two or more consecutive capitals, because `[a-z0-9]*` can match zero
characters and there's nothing to advance past the second capital. Verified:
```
URLPreviewCard -> "U"
UIStateBadge    -> "U"
APIKeyField     -> "A"
```
against
```
CardBase   -> "Card"
VoiceCard  -> "Voice"
```
which behave as intended. This is currently dormant — no existing public top-level `@Composable`
in `src/main` (outside `explorer/`) has this shape — but it is a latent correctness bug in the
"leading PascalCase word" extraction the KDoc (lines 111-118) explicitly claims to implement. The
practical failure mode: a future, entirely generic composable named e.g. `URLPreviewCard` or
`APIKeyField` would be forced into `DOMAIN_VOCABULARY` with a rationale claiming its head token is
"a consumer-domain noun" — which would be false (URL/API are generic, not domain nouns) — purely
because the regex can't parse the acronym prefix, undermining the D-02/D-03 audit-trail's own
accuracy.
**Fix:** Widen the regex to consume a leading all-caps run as a single token when followed by a
lowercase continuation, e.g. `Regex("[A-Z]+(?=[A-Z][a-z]|$)|[A-Z][a-z0-9]*")`, or explicitly test
and document the acronym case. Since this class (`private fun headToken`) is duplicated verbatim
from nowhere else (it's unique to this file, not shared with `ComponentRegistryDriftGuardTest`),
fixing it here is self-contained.

### WR-03: Test setup silently swallows missing source-script errors, contradicting this codebase's own "fail loudly" convention

**File:** `tools/test/test-precommit-hook.sh:7-9`
**Issue:**
```bash
cp "$DIR"/verify-additive-diff.sh "$DIR"/verify-api-additive.sh "$DIR"/verify-additive-surface.sh \
   "$DIR"/classify-hub-change.sh tools/ 2>/dev/null || true
cp "$DIR"/hooks/pre-commit tools/hooks/pre-commit 2>/dev/null || true
```
If any one of these five source paths is renamed, deleted, or typo'd in a future edit, `cp` fails
and the failure is completely swallowed (`2>/dev/null || true`). The test would then proceed to
wire a symlinked pre-commit hook pointing at files that may not exist, and the eventual failure
signal (if any) surfaces several `check()` calls later as a confusing "got 1 want 0" rather than a
clear "setup file missing" error. This directly contradicts the loud-failure philosophy this same
governance phase champions elsewhere (`verify-additive-diff.sh`'s own comments explicitly favor
"failing loudly instead of silently", and `DomainVocabularyDriftGuardTest.kt`'s vacuous-pass guards
exist for the same reason).
**Fix:** Drop the `2>/dev/null || true` (or replace it with an explicit check) so a missing source
script fails the test setup immediately with a clear message, e.g.:
```bash
cp "$DIR"/verify-additive-diff.sh "$DIR"/verify-api-additive.sh "$DIR"/verify-additive-surface.sh \
   "$DIR"/classify-hub-change.sh tools/
cp "$DIR"/hooks/pre-commit tools/hooks/pre-commit
```
(both already run under `set -euo pipefail`, so a genuine `cp` failure will now abort the test
loudly instead of silently degrading it).

### WR-04: `mkdir -p tools/hooks tools/explorer src/main api` creates an unused directory

**File:** `tools/test/test-precommit-hook.sh:6`
**Issue:** `tools/explorer` is created but never referenced anywhere else in the script — dead
setup left over from an earlier iteration of the fixture.
**Fix:** Drop `tools/explorer` from the `mkdir -p` line.

## Info

### IN-01: `verify-api-additive.sh`'s known cumulative-baseline defect is inherited but not re-flagged by these tests — worth a cross-reference note

**File:** `tools/test/test-verify-api-additive.sh:38-58`
**Issue:** Case (e) already candidly documents (and locks in, via an expected `rc=3`) a known
"stale baseline" defect in `verify-api-additive.sh`, tracked for Phase 5. This is the *exact same
architectural shape* as CR-01 above (a per-commit-intended guard whose scope/behavior is still
keyed to the tag baseline rather than the current commit) — not a new finding, but worth noting
that fixing CR-01 in `verify-additive-diff.sh` without also revisiting `verify-api-additive.sh`
leaves the two guards inconsistent in a way that's easy to lose track of once CR-01 lands.
**Fix:** When CR-01 is fixed, add a cross-reference comment in `test-verify-api-additive.sh`'s case
(e) noting that its sibling guard's default-scope bug (a related but distinct issue) was fixed in
this phase, so a future reader doesn't assume both guards share identical remaining risk.

---

_Reviewed: 2026-09-01T00:00:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
