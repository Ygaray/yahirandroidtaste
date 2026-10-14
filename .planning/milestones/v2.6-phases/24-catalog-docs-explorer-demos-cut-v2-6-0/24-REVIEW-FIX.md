---
phase: 24-catalog-docs-explorer-demos-cut-v2-6-0
fixed_at: 2026-10-12T00:20:00Z
review_path: .planning/phases/24-catalog-docs-explorer-demos-cut-v2-6-0/24-REVIEW.md
iteration: 1
findings_in_scope: 6
fixed: 6
skipped: 0
status: all_fixed
---

# Phase 24: Code Review Fix Report

**Fixed at:** 2026-10-12
**Source review:** `.planning/phases/24-catalog-docs-explorer-demos-cut-v2-6-0/24-REVIEW.md`
**Iteration:** 1

**Summary:**
- Findings in scope: 6 (CR-01, WR-01 to WR-05; fix_scope `critical_warning`)
- Fixed: 6
- Skipped: 0 (the five Info findings are outside the scope, listed below)

Constraint check: `git diff --exit-code 13039d1 -- src/main api.txt` exits 0 (no library source and no
`api.txt` change). No tag, no push, no device or adb use. Fixes touch `API.md`, `INTEGRATION.md`,
`tools/` and `src/test` only.

**Where verification ran:** the isolated review-fix worktree (`.claude/worktrees/rf-24-...`, branch
`gsd-reviewfix/24-3327884`, `local.properties` copied in), one gradle invocation at a time. The four commits were
then fast-forwarded onto `main` and the worktree, temp branch and recovery sentinel were removed. The tool
and fixture runs are plain bash and python on committed files, so they reproduce from the main checkout.

## Fixed Issues

### CR-01: API.md and INTEGRATION.md say every v2.5.1 call still compiles; the same files document source breaks

**Files modified:** `API.md`, `INTEGRATION.md`, `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/CatalogDocsDriftGuardTest.kt`
**Commit:** be61b21
**Applied fix:** Reworded the API.md section 11 lead-in and the INTEGRATION.md 5b lead-in to say that named and
trailing-lambda v2.5.1 calls still compile and that exactly three positional shapes do not
(`UndoCenterScreen` with four positional arguments, `NeedsConfirmation` built or destructured positionally
and its `copy`, `SheetScaffold` with `content` as the sixth positional argument), identically in both files.
The "Adding / changing components" `SheetScaffold` bullet now states the break and its fix, and the
`NeedsConfirmation` bullet mentions the positional `copy`. Added
`CatalogDocsDriftGuardTest."the compatibility lead-ins list the same three positional source breaks"`, which
asserts both lead-ins name all three symbols so the list cannot drift. Not a behavior change, but the reviewer
classified it as a false public claim, so the wording is worth a human read before the tag.

### WR-01: R4 passes optional parameters inserted before a required one

**Files modified:** `tools/verify-api-source-additive.py`, `tools/verify-api-source-additive.sh`, `tools/test/test-verify-api-source-additive.sh`, `tools/README-api-guard.md`, `API.md`
**Commit:** d081250 (shared with WR-02 and WR-03, same files and one rewrite of the review loop)
**Status:** fixed: requires human verification (rule logic)
**Applied fix:** Added R5 (the baseline's full parameter list, type and name, must be a positional prefix of a
head signature) and R7 (nothing appended after a trailing lambda). The three documented v2.6.0 breaks are a
reviewed allowlist, `ACCEPTED_EXCEPTIONS`, keyed on the `v2.5.1` baseline label so it cannot waive anything
under another baseline: `SheetScaffold`, `UndoCenterScreen`, and `NeedsConfirmation` (ctor, `copy`,
`component10`, `component11`), six signatures in total. Each is printed as an `ACCEPTED R<n>: ...` line and
counted in `accepted_exceptions=<n>`. The script header and README now state what a green line proves, that a
non-zero `accepted_exceptions` means "every call compiles except the accepted documented shapes", and the
limits. Fixtures 3 and 6, which enshrined the unsound passes, now expect failure (R5) or use only appended
optionals; new fixtures cover the R7 case and the allowlist (passes under `v2.5.1` and is reported, fails
under another label, does not waive a new required parameter). Real data: `SEAMS: none` first line, exit 0,
`API SOURCE-ADDITIVE OK baseline=v2.5.1 ... required_param_changes=0 ... signature_changes=0 accepted_exceptions=6`
(the original tokens are kept in order, new ones are appended). The plan 24-04 gate greps stay satisfied.

### WR-02: Parameter-type parser truncates unnamed parameters containing a space

**Files modified:** `tools/verify-api-source-additive.py`, `tools/test/test-verify-api-source-additive.sh`
**Commit:** d081250
**Status:** fixed: requires human verification (parser logic)
**Applied fix:** Parameters are split into type and name at the last top-level space (outside `<>`), and a tail
that is not an identifier means the parameter is unnamed, so `kotlin.coroutines.Continuation<? super X>` and
extension receivers keep their whole type. Added fixtures: a `suspend` function whose `Continuation` type
argument changes now fails (R4), and the unchanged one passes.

### WR-03: R4 ignores parameter names, return and property types, and new enum constants

**Files modified:** `tools/verify-api-source-additive.py`, `tools/verify-api-source-additive.sh`, `tools/test/test-verify-api-source-additive.sh`, `tools/README-api-guard.md`
**Commit:** d081250
**Status:** fixed: requires human verification (rule logic)
**Applied fix:** Parameter names are now part of the compared tuples (R4 for required parameters, R5 for all),
R6 compares the return type of methods and the declared type of properties and fields, and R8 fails on a
constant added to an existing enum. Each was checked against the real `v2.5.1` to head pair first: the only hits
are the six allowlisted signatures, so no new false positive on the cut. New fixtures: renamed required
parameter (R4), renamed optional parameter (R5), changed return type (R6), changed property type (R6), added
enum constant (R8). The README Scope paragraph and the script header list what is still not covered (new
abstract members, new sealed subclasses, modifier or visibility changes of kept members, bounds, defaults,
behavior, binary compatibility, anything Metalava does not dump).

### WR-04: INTEGRATION.md key-lifecycle sample shows a pointless Cancel button for unsaved providers

**Files modified:** `INTEGRATION.md`
**Commit:** 9e397e5
**Applied fix:** The sample now passes `onCancelEdit = if (editing) { { editing = false; typed = "" } } else null`,
and a bullet says to pass `onCancelEdit` only while `editingSavedKey` is true because the card draws Cancel
whenever the callback is non-null.

### WR-05: LibraryInvariantsGuardTest does not enforce "no Hilt application host"

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/LibraryInvariantsGuardTest.kt`
**Commit:** 7f139c2
**Applied fix:** Added `the library declares no Hilt application host or entry point`, scanning comment-stripped
`src/main` Kotlin for the two Hilt host annotations (assembled from fragments, per the file's convention), with a
vacuity guard on the scanned file count. Updated the class KDoc. The library has no such annotation today, so the
test is green; it fails the build if one is added.

## Verification run (after all fixes)

- `bash tools/test/run-all.sh`: all suites ok (`test-verify-api-source-additive.sh`: 34 cases, 0 failed).
- `bash tools/verify-api-source-additive.sh v2.5.1`: exit 0, line 1 `SEAMS: none`, `API SOURCE-ADDITIVE OK ... accepted_exceptions=6`.
- `./gradlew testDebugUnitTest --offline --console=plain --tests '*CatalogDocsDriftGuardTest' --tests '*LibraryInvariantsGuardTest' --tests '*V251CallSiteCanaryTest'`: BUILD SUCCESSFUL; 10, 5 and 12 tests, 0 failures, 0 errors.
- `./gradlew detekt --offline --console=plain`: BUILD SUCCESSFUL (.kt test files changed).
- `git diff --exit-code 13039d1 -- src/main api.txt`: exit 0.

## Skipped Issues

None in scope. The following are skipped by scope (`fix_scope: critical_warning`), not by judgment:

- IN-01: Demo "fingerprint" keeps the whole key when it is four characters or shorter (`ReleaseDemos.kt:131`, also under `src/main`, which is frozen).
- IN-02: Canary's "positional break is excluded" guard only matches all-lowercase identifiers (`V251CallSiteCanaryTest.kt:687`).
- IN-03: Stale and misleading names and comments in the new tests (`statsHeading` typo in `CatalogDocsDriftGuardTest`; case 14 comment in the fixture script was reworded as a side effect of WR-01 since that case was edited).
- IN-04: Undo group demo uses a fixed 680dp height (`ReleaseDemos.kt:211`, `src/main`, frozen).
- IN-05: Fixture seams can silently turn the cut-gate verdict into a fixture verdict (`verify-api-source-additive.sh`).

---

_Fixed: 2026-10-12_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
