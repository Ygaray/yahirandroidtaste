---
phase: 09-ship-coordinated-repin
reviewed: 2026-09-27T00:00:00Z
depth: standard
files_reviewed: 3
files_reviewed_list:
  - build.gradle.kts
  - api.txt
  - .planning/KNOWN-ISSUES.md
findings:
  critical: 0
  warning: 1
  info: 1
  total: 2
status: issues_found
---

# Phase 9 (Plan 01, Task 1): Code Review Report — commit `ff3d9c6`

**Reviewed:** 2026-09-27
**Depth:** standard (independent build verification performed — not just diff reading)
**Files Reviewed:** 3 (`build.gradle.kts`, `api.txt`, `.planning/KNOWN-ISSUES.md`)
**Status:** issues_found (both findings are low-severity documentation/process quality, not correctness or security)

## Summary

Reviewed `ff3d9c6` against `09-01-PLAN.md`'s must-haves and STRIDE table (T-09-01/T-09-02/T-09-03).
This is a narrowly-scoped, purely-additive build-config change (one `hiddenAnnotations.add(...)`
line) plus a purely-subtractive `api.txt` rebaseline and a documentation update. I did not just read
the diff — I independently re-ran the checks the commit claims pass, on the actual working tree at
this commit (HEAD is `5f48f95`, one docs-only commit ahead of `ff3d9c6`; `build.gradle.kts`/`api.txt`
are unchanged since `ff3d9c6`, so this is a faithful re-check):

- `./gradlew metalavaCheckCompatibilityDebug metalavaCheckCompatibilityRelease --rerun-tasks` →
  `BUILD SUCCESSFUL`, both tasks actually executed (not cached UP-TO-DATE). Confirms T-09-01's claim
  that both variants are genuinely green, not just Release.
- `diff <(git show HEAD:api.txt) api.txt` → identical. `grep -c DaggerGenerated api.txt` → `0`,
  `grep -c UndoHistoryStore_Factory api.txt` → `0`, `class UndoHistoryStore {` → `1`,
  `emitTrackedWithUndo` → `1`. Matches the "Closing evidence" block verbatim.
  `grep -in 'dagger\|hilt\|_Factory\|Generated'` across the whole file → no hits at all, so no other
  Dagger/Hilt-generated symbol slipped in unnoticed.
  Brace count in `api.txt` is balanced (162/162) and the removed block's surrounding blank-line
  spacing is clean (no double-blank-line artifact left behind by the subtractive edit).
- `./gradlew detekt --rerun-tasks` → `0 number of total code smells`, matching the KI-2026-09-27-01
  note added in this same commit.

One transient false failure was observed mid-review (`Aborting: Unable to parse signature file:
api.txt:1192: expected package got public`) when two overlapping `./gradlew` invocations from my own
review session raced on the same Gradle daemon; a clean sequential re-run immediately after showed
`api.txt` byte-identical to `git show HEAD:api.txt` before and after, and the check passed. That is
an artifact of my own review tooling launching overlapping builds, not a defect in the commit — flagging
it here for transparency but not carrying it into the findings below.

The `hiddenAnnotations.add("dagger.internal.DaggerGenerated")` fix is well-targeted: it's scoped to
exactly the one FQN Dagger stamps on every type it generates (confirmed by the annotation appearing
on the class-level of the now-removed `UndoHistoryStore_Factory` block, alongside
`@QualifierMetadata`/`@ScopeMetadata`/`javax.annotation.processing.Generated` — hiding the "master"
marker is sufficient since no hand-authored code can carry an annotation from `dagger.internal`).
Because `metalava { }` is a single global extension (not per-variant), the fix applies uniformly to
`metalavaGenerateSignatureDebug`/`Release` and both `metalavaCheckCompatibilityDebug`/`Release` — this
correctly satisfies the plan's must-have that Debug is fixed too, not routed around via Release-only.

The `api.txt` diff is exactly what the commit message claims: 7 lines removed (the
`UndoHistoryStore_Factory` block + its trailing blank line), zero lines touching
`UndoHistoryStore`'s real class/ctor or `emitTrackedWithUndo`. No security concerns — this is a
build-time signature-tracking change with no runtime input, consistent with the plan's STRIDE table
(T-09-01 mitigated as designed; T-09-02/T-09-03 correctly out of scope for this commit, which stops
short of the tag cut).

## Narrative Findings (AI reviewer)

### WR-01: Commit mixes an unrelated KI edit into a commit scoped to a different KI, undisclosed in the commit message

**File:** `.planning/KNOWN-ISSUES.md:184-189` (KI-2026-09-27-01 section, "Note (Phase 9, 2026-09-27)")
**Issue:** The commit message and Task 1's `<action>` in `09-01-PLAN.md` describe this commit as
closing exactly one known issue, `KI-2026-09-02-01` (the Dagger factory leak). The diff, however,
also appends a note to the *unrelated* `KI-2026-09-27-01` entry (detekt's `TextCard.kt` cyclomatic
complexity finding), stating detekt now shows 0 smells but deliberately leaving that KI's `Status`
as `open` rather than closing it or letting a dedicated commit do so. Independent verification
confirms the technical claim is currently true (`./gradlew detekt --rerun-tasks` → 0 smells), so this
isn't a factual error — but bundling a second, unrelated KI's documentation update into a commit
whose message only advertises the first KI is a git-hygiene defect: anyone bisecting or auditing
`git log --follow` for KI-2026-09-02-01's history gets a diff hunk about a different, unrelated
known issue with no mention of it in the commit subject/body, and the "note added without closing"
pattern is inherently transient (it will silently rot true→false the next time someone touches
`TextCard.kt` without following up here).
**Fix:** Either fold the KI-2026-09-27-01 observation into its own small commit (e.g.
`docs(09): note KI-2026-09-27-01 no longer reproduces`), or mention it explicitly in this commit's
body if it's staying bundled (`Also note in KI-2026-09-27-01 that detekt is currently clean...`).
Going forward, keep one KI's open/close lifecycle per commit unless the commit message says
otherwise.

### IN-01: `hiddenAnnotations` config lacks a regression comment tying it to the drift-guard tests

**File:** `build.gradle.kts:16-25`
**Issue:** The new `hiddenAnnotations.add(...)` line is well-commented for *why* it exists (ties back
to KI-2026-09-02-01), but nothing in the comment or in `ComponentRegistryDriftGuardTest.kt` /
`DomainVocabularyDriftGuardTest.kt` guards against a *future* Dagger/Hilt/KSP upgrade changing the
generated-code marker annotation (e.g., a hypothetical future Dagger major version renaming or
dropping `dagger.internal.DaggerGenerated`). If that ever happens, the fix silently stops working —
the generated factory reappears in `api.txt` and `metalavaCheckCompatibilityDebug` starts failing
again with the exact same false-positive this commit closes, and nothing regression-tests that
scenario today. This is speculative/low-probability (Dagger has used this annotation FQN for years),
so it's Info rather than Warning.
**Fix:** Optional — a one-line addition to `tools/README-api-guard.md` (referenced in the file's own
header comment at `build.gradle.kts:11-15`) noting "if `hiddenAnnotations` silently stops working
after a Dagger/Hilt upgrade, `apiDump` will start re-introducing generated `_Factory` classes; check
`dagger.internal.DaggerGenerated` is still the annotation FQN Dagger stamps" would make the failure
mode self-documenting for whoever hits it next.

---

_Reviewed: 2026-09-27_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
