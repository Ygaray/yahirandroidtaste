---
phase: 09-ship-coordinated-repin
plan: 01
subsystem: infra
tags: [metalava, dagger, hilt, detekt, jitpack, apicheck]

requires:
  - phase: 06-forward-port-reunification
    provides: final additive api.txt / ComponentRegistry surface this rebaseline builds on
  - phase: 07-chip-color-slot
    provides: additive api.txt surface (TagChipUiModel.color) this rebaseline builds on
  - phase: 08-micbutton-hardening
    provides: additive api.txt surface (MicButton hardening) this rebaseline builds on
provides:
  - KI-2026-09-02-01 closed: hiddenAnnotations excludes dagger.internal.DaggerGenerated from the metalava surface
  - api.txt rebaselined with no Dagger-generated symbol, real UndoHistoryStore API intact
  - All hub gates (testDebugUnitTest incl. both drift guards + new GeneratedSymbolDriftGuardTest, zero-baseline detekt, apiCheck both variants, publishReleasePublicationToMavenLocal) green on commit bfd4c3b
  - Code review (issues_found, no blockers) and security audit (SECURED, 0 open threats) completed for this plan's diff
  - The exact v2.2.0 tag-cut command surfaced for owner go-ahead (not executed)
affects: [09-02-jitpack-verify-ecosystem-reconcile]

actuals:
  tokens: 5200
  tasks: 2
  commits: 3

tech-stack:
  added: []
  patterns:
    - "metalava hiddenAnnotations SetProperty<String> excludes Dagger/Hilt-generated factory classes from the tracked api.txt surface on every variant, not just Release"
    - "Durable drift-guard JUnit tests (GeneratedSymbolDriftGuardTest, mirroring ComponentRegistryDriftGuardTest/DomainVocabularyDriftGuardTest) catch a generated symbol silently re-entering api.txt -- a case apiCheck/metalavaCheckCompatibility* cannot catch since they only fail on removals, not additions"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/GeneratedSymbolDriftGuardTest.kt
    - .planning/phases/09-ship-coordinated-repin/VALIDATION.md
    - .planning/phases/09-ship-coordinated-repin/09-01-REVIEW.md
    - .planning/phases/09-ship-coordinated-repin/09-01-SECURITY.md
  modified:
    - build.gradle.kts
    - api.txt
    - .planning/KNOWN-ISSUES.md

key-decisions:
  - "D-01 applied exactly as FINALIZED in 09-CONTEXT.md Runtime Decisions: fix option 1 (hide the annotation + rebaseline), not fix option 2 (rebaseline-only, which would let the class re-enter on a future dump)."
  - "D-02 confirmed: build.gradle.kts version marker (1.10.0) left untouched -- JitPack overrides from the resolved git ref; confirmed via git status showing no diff to that line."
  - "Task 3 (checkpoint:decision, gate=blocking-human) is a designed stop, not executed -- per this repo's CLAUDE.md and ECOSYSTEM.md Section 7, the v2.2.0 tag cut is human-gated and this plan must not run git tag/git push."
  - "Nyquist auditor found a real coverage gap (metalava compat checks can't catch a re-added symbol) and closed it with a new persisted test rather than leaving the KI-2026-09-02-01 fix verified only by one-off shell commands."
  - "Code reviewer's IN-01 finding (undocumented failure mode if Dagger/Hilt ever renames the DaggerGenerated annotation) addressed by adding an explanatory comment in build.gradle.kts pointing at the new drift guard; WR-01 (git-hygiene note about an unrelated KI touched in the same commit) accepted as-is -- historical commit not rewritten."

patterns-established:
  - "Pattern: build-config accuracy fix (metalava hiddenAnnotations) + api.txt regen via ./gradlew apiDump, committed together in one commit, matching this repo's historical api.txt regen discipline (commit 000bf89, v2.1.0 tag commit f690efc)."

requirements-completed: [SHIP-01]

coverage:
  - id: D1
    description: "api.txt contains no @DaggerGenerated-annotated symbol (UndoHistoryStore_Factory gone); UndoHistoryStore's real class, @Inject constructor, and emitTrackedWithUndo extension are unchanged."
    requirement: "SHIP-01"
    verification:
      - kind: unit
        ref: "GeneratedSymbolDriftGuardTest#apiTxtNeverReadmitsAHiddenDaggerGeneratedFactorySymbol, #apiTxtStillContainsUndoHistoryStoresRealPublicApi"
        status: pass
    human_judgment: false
  - id: D2
    description: "metalavaCheckCompatibilityDebug and metalavaCheckCompatibilityRelease both pass; KI-2026-09-02-01 marked closed in KNOWN-ISSUES.md."
    requirement: "SHIP-01"
    verification:
      - kind: integration
        ref: "./gradlew metalavaCheckCompatibilityDebug metalavaCheckCompatibilityRelease -> BUILD SUCCESSFUL"
        status: pass
    human_judgment: false
  - id: D3
    description: "All hub gates (testDebugUnitTest incl. both drift guards + new GeneratedSymbolDriftGuardTest, zero-baseline detekt, apiCheck, metalavaCheckCompatibilityDebug, publishReleasePublicationToMavenLocal) are green on commit bfd4c3b, with nothing left uncommitted; version marker confirmed untouched."
    requirement: "SHIP-01"
    verification:
      - kind: integration
        ref: "./gradlew testDebugUnitTest detekt apiCheck metalavaCheckCompatibilityDebug publishReleasePublicationToMavenLocal --rerun-tasks -> BUILD SUCCESSFUL, 0 test failures, 0 detekt smells, mavenLocal publish present at ~/.m2/repository/com/github/Ygaray/yahirandroidtaste/1.10.0/"
        status: pass
    human_judgment: false
  - id: D4
    description: "The exact v2.2.0 tag-cut command and JitPack-verify step are surfaced via a checkpoint:decision with gate=blocking-human -- no git tag or git push executed by this plan."
    human_judgment: true
    rationale: "Cutting and pushing an immutable git tag is a one-way door explicitly reserved for the repo owner per root CLAUDE.md and ECOSYSTEM.md Section 7 -- no amount of automated verification substitutes for that explicit human go-ahead."

duration: 35min
completed: 2026-09-27
status: halted
---

# Phase 9 Plan 01: apiCheck KI closure + closing governance battery + tag-cut checkpoint Summary

**Hid Dagger-generated `@DaggerGenerated` symbols from the metalava-tracked surface, rebaselined `api.txt`, closed KI-2026-09-02-01, added a durable regression guard, cleared code review/security audit, and confirmed every hub gate green -- halted at the human-gated `v2.2.0` tag-cut checkpoint as designed.**

## Performance

- **Duration:** ~35 min
- **Started:** 2026-09-27
- **Completed:** 2026-09-27 (Tasks 1-2 + post-hoc code review/security/nyquist gates; Task 3 halted for human input)
- **Tasks:** 2 of 3 (Task 3 is a designed blocking-human stop, not executed)
- **Files modified:** 6 (across 3 commits)

## Accomplishments
- `build.gradle.kts`'s `metalava { }` block now hides `dagger.internal.DaggerGenerated` from every variant's tracked surface, not just Release.
- `api.txt` rebaselined via `./gradlew apiDump` -- purely subtractive (the generated `UndoHistoryStore_Factory` sibling only); `UndoHistoryStore`'s real class, `@Inject` constructor, and `emitTrackedWithUndo` extension are byte-for-byte unchanged.
- Both `metalavaCheckCompatibilityDebug` and `metalavaCheckCompatibilityRelease` pass clean -- KI-2026-09-02-01 closed for real, not routed around.
- Full closing governance battery (`testDebugUnitTest detekt apiCheck metalavaCheckCompatibilityDebug publishReleasePublicationToMavenLocal`) green on commit `bfd4c3b`: 0 test failures (both `ComponentRegistryDriftGuardTest`/`DomainVocabularyDriftGuardTest` plus the new `GeneratedSymbolDriftGuardTest` pass), 0 detekt code smells (zero-baseline, no new baseline entries), mavenLocal publish artifact present.
- `gsd-code-reviewer` reviewed the closing commit: **issues found, no blockers** (1 warning, 1 info) -- see `09-01-REVIEW.md`. The info finding (undocumented failure mode) was addressed with a code comment; the warning (an unrelated KI's status note bundled into this commit) was accepted as a hygiene note, not re-litigated via history rewrite.
- `gsd-security-auditor` verified the plan's STRIDE threats: **SECURED**, 3/3 mitigated, 0 open -- see `09-01-SECURITY.md`.
- `gsd-nyquist-auditor` found a real, non-cosmetic coverage gap (metalava's compatibility checks only catch breaking removals, never a generated symbol quietly re-entering `api.txt`) and closed it with a new persisted JUnit test (`GeneratedSymbolDriftGuardTest`), adversarially proven red/green -- see `VALIDATION.md`.
- Gate-1 agentic self-UAT: **n/a** for this plan -- the diff is build-config/tooling/doc-only with no user-visible app behavior to drive on device.
- Exact `v2.2.0` tag-cut command surfaced to the owner; no `git tag`/`git push` executed.

## Task Commits

1. **Task 1: Hide Dagger-generated symbols from metalava + rebaseline api.txt (D-01, KI-2026-09-02-01)** - `ff3d9c6` (fix)
2. **Task 2: Closing governance battery** - no new commit (read-only verification battery on the exact commit Task 1 produced; `git status --porcelain` confirmed nothing dirty before and after)
3. **Post-hoc gates: code review + security audit + nyquist gap-fill** - `bfd4c3b` (test: adds `GeneratedSymbolDriftGuardTest`, `VALIDATION.md`, `09-01-REVIEW.md`, `09-01-SECURITY.md`, and a `build.gradle.kts` comment addressing the reviewer's IN-01 finding)

**Plan metadata:** SUMMARY commit follows this file's creation/update.

## Files Created/Modified
- `build.gradle.kts` - added `hiddenAnnotations.add("dagger.internal.DaggerGenerated")` to the `metalava { }` extension block, plus a comment documenting the failure mode and pointing at the new drift guard
- `api.txt` - regenerated via `./gradlew apiDump`; the Dagger-generated `UndoHistoryStore_Factory` entry (7 lines) dropped, all real public API entries unchanged
- `.planning/KNOWN-ISSUES.md` - KI-2026-09-02-01 status changed `open` -> `closed` with closing evidence appended; also appended a non-blocking note to the unrelated `KI-2026-09-27-01` (`TextCard.kt` detekt complexity) recording that it no longer reproduces as of this commit's `detekt --rerun-tasks` run (0 code smells), left `open` since fixing it is out of this plan's scope
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/GeneratedSymbolDriftGuardTest.kt` - new persisted regression guard closing the nyquist-identified coverage gap
- `.planning/phases/09-ship-coordinated-repin/VALIDATION.md` - nyquist gap analysis + adversarial red/green proof
- `.planning/phases/09-ship-coordinated-repin/09-01-REVIEW.md` - code review findings (issues found, no blockers)
- `.planning/phases/09-ship-coordinated-repin/09-01-SECURITY.md` - security verdict (SECURED, 0 open threats)

## Decisions Made
- Applied D-01 exactly as FINALIZED (fix option 1: hide + rebaseline, not fix option 2's weaker rebaseline-only).
- Left the `build.gradle.kts` version marker (`1.10.0`) untouched per D-02 -- confirmed via `git status` showing no diff on that line.
- Confirmed KI-2026-09-27-01 (detekt/TextCard.kt) is currently non-reproducing (0 smells in this run) but is out of scope for this plan's `files_modified` -- documented via a note rather than silently closing an issue this plan didn't fix.
- Accepted the nyquist auditor's coverage-gap finding and its fix (a new drift-guard test) rather than treating the plan's one-off shell verification as sufficient long-term coverage.
- Addressed the code reviewer's IN-01 info finding directly (added a build.gradle.kts comment); accepted WR-01 as a hygiene note without amending the already-pushed-nowhere-yet but already-committed `ff3d9c6` commit message.

## Deviations from Plan

**1. [Coverage gap - Nyquist] Added a durable regression test not specified in the original plan**
- **Found during:** post-Task-2 nyquist validation pass
- **Issue:** The plan's own `<verify>` blocks for Task 1 (grep counts, `metalavaCheckCompatibilityDebug`/`Release` exit codes) are real and correct but one-off -- they proved the fix on this commit, not that the fix stays proven if `hiddenAnnotations` is later reverted or a Dagger/Hilt upgrade renames the annotation. metalava's compatibility checks structurally cannot catch a symbol being added back (only breaking removals fail them).
- **Fix:** Added `GeneratedSymbolDriftGuardTest`, mirroring this repo's existing drift-guard pattern (`ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`), asserting both the absence of the generated symbol and the presence of the real API on every `testDebugUnitTest` run.
- **Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/GeneratedSymbolDriftGuardTest.kt`
- **Verification:** Adversarially proven red (synthetic symbol injected, test failed) then green (reverted, full battery passed) -- see `VALIDATION.md`.
- **Committed in:** `bfd4c3b`

**2. [Code review IN-01] Documented the metalava hiddenAnnotations failure mode**
- **Found during:** post-Task-2 code review pass
- **Issue:** No comment explained what happens if the `dagger.internal.DaggerGenerated` FQN this fix relies on is ever renamed/dropped by a future Hilt/Dagger version.
- **Fix:** Added an explanatory comment in `build.gradle.kts`'s `metalava { }` block naming the failure mode and pointing at `GeneratedSymbolDriftGuardTest` as the durable catch.
- **Files modified:** `build.gradle.kts`
- **Verification:** Comment-only change; full battery re-run green after the edit.
- **Committed in:** `bfd4c3b`

---

**Total deviations:** 2 auto-fixed (1 coverage gap from nyquist, 1 documentation gap from code review). Both are additive hardening on top of the plan's own scope -- no scope creep into Task 3's human-gated tag decision.

## Issues Encountered

None beyond the two items above. All Task 1/2 verification commands passed on first attempt; the KI's stated root cause and fix matched the actual `metalava` plugin DSL (`hiddenAnnotations: SetProperty<String>`, confirmed by decompiling `me.tylerbwong.gradle.metalava:plugin:0.5.0`'s `MetalavaExtension` class before editing `build.gradle.kts`).

## User Setup Required

None - no external service configuration required. **However, a human decision is required to proceed:** see "Next Phase Readiness" below.

## Next Phase Readiness

**BLOCKED on human go-ahead.** All hub gates, code review, and security audit are green on `main` at commit `bfd4c3b`. To cut and push the immutable `v2.2.0` tag, run from this repo root:

```
git tag -a v2.2.0 -m "v2.2.0 -- Ship & coordinated repin: line reunification (REUNI-01..04), chip-color slot (TAGCOLOR-01), MicButton hardening (MICBTN-01..03), apiCheck KI-2026-09-02-01 fix (SHIP-01)"
git push origin v2.2.0
```

Once pushed, Plan 09-02 (JitPack resolution verification + ECOSYSTEM.md reconcile) can proceed -- its precondition requires the tag to already exist at origin. This plan does not, and must not, execute the above commands itself.

---
*Phase: 09-ship-coordinated-repin*
*Completed (through Task 2 + post-hoc gates; halted at Task 3): 2026-09-27*
