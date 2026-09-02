---
phase: 03-governance-gates
fixed_at: 2026-09-02T02:42:29Z
review_path: .planning/phases/03-governance-gates/03-REVIEW.md
iteration: 1
findings_in_scope: 6
fixed: 6
skipped: 0
status: all_fixed
---

# Phase 03: Code Review Fix Report

**Fixed at:** 2026-09-02T02:42:29Z
**Source review:** .planning/phases/03-governance-gates/03-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 6
- Fixed: 6
- Skipped: 0

**Verification environment:** All fixes applied and verified inside an isolated git worktree
(`.claude/worktrees/rf-03-3067862-1788316676`, branch `gsd-reviewfix/03-3067862`), then fast-forward
merged onto `main` in the primary checkout. All shell test suites and the Gradle unit-test run below
were executed inside that worktree; results are reproducible from `main` after the merge since the
worktree contained no untracked/uncommitted state beyond what is now on `main`.

## Fixed Issues

### CR-01: DS-05 default path scope pins to the stale baseline tree, not HEAD

**Files modified:** `tools/verify-additive-diff.sh`, `tools/test/test-verify-additive-diff.sh`
**Commit:** dc5bf6f
**Applied fix:** Changed the default `PATHS` enumeration from `git ls-tree -z -r --name-only
"$BASELINE_COMMIT" -- src/main` to `git ls-tree -z -r --name-only HEAD -- src/main`, matching the
diff basis (staged-vs-HEAD, per the GOV-03 fix) rather than the stale baseline tag. Updated the
error message and surrounding comments accordingly. Added a new regression case (d) to
`test-verify-additive-diff.sh`: creates a file in a post-tag commit, rewrites a line in it in the
next commit, and asserts the guard now catches it (previously untested). Verified live against the
reviewer's exact reproduction (`printf 'val b = 1\n' > src/main/B.kt` post-tag, then rewrite) — the
guard now correctly fails with `DS-05 FAIL`.

### WR-01: Blank-line-only removals are invisible to the append-only guard

**Files modified:** `tools/verify-additive-diff.sh`, `tools/test/test-verify-additive-diff.sh`
**Commit:** 1845d3b
**Applied fix:** Widened the `REMOVED_RAW`/`ADDED_RAW` extraction from `grep -E '^-[^-]'` /
`grep -E '^\+[^+]'` (which require a second character, dropping bare blank-line markers) to
`grep -E '^-' | grep -v -E '^--- '` / `grep -E '^\+' | grep -v -E '^\+\+\+ '`, per the reviewer's
suggested approach. During verification, discovered the regex fix alone was insufficient: the
`OFFENDERS="$(comm -23 ... )"` command-substitution pattern strips trailing newlines, which
silently collapses a single offending line that normalizes to an empty string (exactly the
blank-line-removal case) back down to an empty string, making `[ -n "$OFFENDERS" ]` false. Fixed
this adjacent bug by writing `comm`'s output to a file (`$OFFENDERS_FILE`) and testing with `[ -s
"$OFFENDERS_FILE" ]` (byte-count based, immune to trailing-newline stripping) instead. Added
regression case (e) to `test-verify-additive-diff.sh` reproducing the reviewer's exact scenario
(delete a blank line with no replacement) — confirmed it now fails the guard as intended.

### WR-02: `headToken()` mis-parses composable names with a leading acronym run

**Files modified:**
`src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt`
**Commit:** 7986934
**Applied fix:** Widened `HEAD_TOKEN_REGEX` from `[A-Z][a-z0-9]*` to
`[A-Z]+(?=[A-Z][a-z]|$)|[A-Z][a-z0-9]*`, per the reviewer's suggested regex — the acronym branch
consumes a leading all-caps run as one token when followed by another capital+lowercase (the next
word starting) or end-of-string, falling back to the ordinary single-capital word otherwise. Added
a dedicated `@Test` (`headTokenExtractsLeadingAcronymRunAsASingleToken`) asserting `URLPreviewCard`
-> `"URL"`, `UIStateBadge` -> `"UI"`, `APIKeyField` -> `"API"`, and confirming ordinary names
(`CardBase` -> `"Card"`, `VoiceCard` -> `"Voice"`) are unaffected. Verified via a real Gradle
`testDebugUnitTest` run against the exact class — both tests pass.

### WR-03: Test setup silently swallows missing source-script errors

**Files modified:** `tools/test/test-precommit-hook.sh`
**Commit:** 280b7cc
**Applied fix:** Dropped `2>/dev/null || true` from both `cp` invocations (source-script copy and
pre-commit hook copy) so a missing/renamed source file now aborts the test loudly under the
script's existing `set -euo pipefail`, per the reviewer's suggested fix. Committed together with
WR-04 (same lines in the same file).

### WR-04: `mkdir -p tools/hooks tools/explorer src/main api` creates an unused directory

**Files modified:** `tools/test/test-precommit-hook.sh`
**Commit:** 280b7cc
**Applied fix:** Dropped the unused `tools/explorer` path from the `mkdir -p` line. Committed
together with WR-03 (same lines in the same file). Full `test-precommit-hook.sh` suite re-run after
both changes: `PASS=7 FAIL=0`.

### IN-01: `verify-api-additive.sh`'s cumulative-baseline defect not cross-referenced against CR-01

**Files modified:** `tools/test/test-verify-api-additive.sh`
**Commit:** d23d715
**Applied fix:** Added a cross-reference comment above case (e) noting that `verify-additive-diff.sh`'s
sibling instance of this exact bug shape (CR-01, the default-PATHS-pinned-to-baseline-tag issue)
was fixed this phase in commit `dc5bf6f`, and that `verify-api-additive.sh`'s own instance (case
(e), above) remains a distinct, still-open issue tracked for Phase 5 — so a future reader doesn't
assume both guards now share identical remaining risk. Comment-only change; suite re-run:
`PASS=5 FAIL=0`.

## Skipped Issues

None — all findings were fixed.

---

_Fixed: 2026-09-02T02:42:29Z_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
