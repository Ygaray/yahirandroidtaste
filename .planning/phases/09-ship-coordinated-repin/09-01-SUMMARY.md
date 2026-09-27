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
  - All hub gates (testDebugUnitTest incl. both drift guards, zero-baseline detekt, apiCheck both variants, publishReleasePublicationToMavenLocal) green on commit ff3d9c6
  - The exact v2.2.0 tag-cut command surfaced for owner go-ahead (not executed)
affects: [09-02-jitpack-verify-ecosystem-reconcile]

actuals:
  tokens: 3500
  tasks: 2
  commits: 1

tech-stack:
  added: []
  patterns:
    - "metalava hiddenAnnotations SetProperty<String> excludes Dagger/Hilt-generated factory classes from the tracked api.txt surface on every variant, not just Release"

key-files:
  created: []
  modified:
    - build.gradle.kts
    - api.txt
    - .planning/KNOWN-ISSUES.md

key-decisions:
  - "D-01 applied exactly as FINALIZED in 09-CONTEXT.md Runtime Decisions: fix option 1 (hide the annotation + rebaseline), not fix option 2 (rebaseline-only, which would let the class re-enter on a future dump)."
  - "D-02 confirmed: build.gradle.kts version marker (1.10.0) left untouched -- JitPack overrides from the resolved git ref; confirmed via git status showing no diff to that line."
  - "Task 3 (checkpoint:decision, gate=blocking-human) is a designed stop, not executed -- per this repo's CLAUDE.md and ECOSYSTEM.md Section 7, the v2.2.0 tag cut is human-gated and this plan must not run git tag/git push."

patterns-established:
  - "Pattern: build-config accuracy fix (metalava hiddenAnnotations) + api.txt regen via ./gradlew apiDump, committed together in one commit, matching this repo's historical api.txt regen discipline (commit 000bf89, v2.1.0 tag commit f690efc)."

requirements-completed: [SHIP-01]

coverage:
  - id: D1
    description: "api.txt contains no @DaggerGenerated-annotated symbol (UndoHistoryStore_Factory gone); UndoHistoryStore's real class, @Inject constructor, and emitTrackedWithUndo extension are unchanged."
    requirement: "SHIP-01"
    verification:
      - kind: unit
        ref: "grep -c DaggerGenerated api.txt == 0; grep -c UndoHistoryStore_Factory api.txt == 0; grep -c 'class UndoHistoryStore {' api.txt == 1; grep -c emitTrackedWithUndo api.txt == 1"
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
    description: "All hub gates (testDebugUnitTest incl. ComponentRegistryDriftGuardTest + DomainVocabularyDriftGuardTest, zero-baseline detekt, apiCheck, metalavaCheckCompatibilityDebug, publishReleasePublicationToMavenLocal) are green on commit ff3d9c6, with nothing left uncommitted; version marker confirmed untouched."
    requirement: "SHIP-01"
    verification:
      - kind: integration
        ref: "./gradlew testDebugUnitTest detekt apiCheck metalavaCheckCompatibilityDebug publishReleasePublicationToMavenLocal --rerun-tasks -> BUILD SUCCESSFUL in 1m48s, 0 test failures, 0 detekt smells, mavenLocal publish present at ~/.m2/repository/com/github/Ygaray/yahirandroidtaste/1.10.0/"
        status: pass
    human_judgment: false
  - id: D4
    description: "The exact v2.2.0 tag-cut command and JitPack-verify step are surfaced via a checkpoint:decision with gate=blocking-human -- no git tag or git push executed by this plan."
    human_judgment: true
    rationale: "Cutting and pushing an immutable git tag is a one-way door explicitly reserved for the repo owner per root CLAUDE.md and ECOSYSTEM.md Section 7 -- no amount of automated verification substitutes for that explicit human go-ahead."

duration: 12min
completed: 2026-09-27
status: halted
---

# Phase 9 Plan 01: apiCheck KI closure + closing governance battery + tag-cut checkpoint Summary

**Hid Dagger-generated `@DaggerGenerated` symbols from the metalava-tracked surface, rebaselined `api.txt`, closed KI-2026-09-02-01, and confirmed every hub gate green on the tag-candidate commit -- halted at the human-gated `v2.2.0` tag-cut checkpoint as designed.**

## Performance

- **Duration:** ~12 min
- **Started:** 2026-09-27
- **Completed:** 2026-09-27 (Tasks 1-2; Task 3 halted for human input)
- **Tasks:** 2 of 3 (Task 3 is a designed blocking-human stop, not executed)
- **Files modified:** 3

## Accomplishments
- `build.gradle.kts`'s `metalava { }` block now hides `dagger.internal.DaggerGenerated` from every variant's tracked surface, not just Release.
- `api.txt` rebaselined via `./gradlew apiDump` -- purely subtractive (the generated `UndoHistoryStore_Factory` sibling only); `UndoHistoryStore`'s real class, `@Inject` constructor, and `emitTrackedWithUndo` extension are byte-for-byte unchanged.
- Both `metalavaCheckCompatibilityDebug` and `metalavaCheckCompatibilityRelease` pass clean -- KI-2026-09-02-01 closed for real, not routed around.
- Full closing governance battery (`testDebugUnitTest detekt apiCheck metalavaCheckCompatibilityDebug publishReleasePublicationToMavenLocal`) green on commit `ff3d9c6`: 0 test failures (both `ComponentRegistryDriftGuardTest` and `DomainVocabularyDriftGuardTest` pass), 0 detekt code smells (zero-baseline, no new baseline entries), mavenLocal publish artifact present.
- Exact `v2.2.0` tag-cut command surfaced to the owner; no `git tag`/`git push` executed.

## Task Commits

1. **Task 1: Hide Dagger-generated symbols from metalava + rebaseline api.txt (D-01, KI-2026-09-02-01)** - `ff3d9c6` (fix)
2. **Task 2: Closing governance battery** - no new commit (read-only verification battery on the exact commit Task 1 produced; `git status --porcelain` confirmed nothing dirty before and after)

**Plan metadata:** SUMMARY commit follows this file's creation.

## Files Created/Modified
- `build.gradle.kts` - added `hiddenAnnotations.add("dagger.internal.DaggerGenerated")` to the `metalava { }` extension block
- `api.txt` - regenerated via `./gradlew apiDump`; the Dagger-generated `UndoHistoryStore_Factory` entry (7 lines) dropped, all real public API entries unchanged
- `.planning/KNOWN-ISSUES.md` - KI-2026-09-02-01 status changed `open` -> `closed` with closing evidence appended; also appended a non-blocking note to the unrelated `KI-2026-09-27-01` (`TextCard.kt` detekt complexity) recording that it no longer reproduces as of this commit's `detekt --rerun-tasks` run (0 code smells), left `open` since fixing it is out of this plan's scope and its resolution wasn't attributable to this plan's changes

## Decisions Made
- Applied D-01 exactly as FINALIZED (fix option 1: hide + rebaseline, not fix option 2's weaker rebaseline-only).
- Left the `build.gradle.kts` version marker (`1.10.0`) untouched per D-02 -- confirmed via `git status` showing no diff on that line.
- Confirmed KI-2026-09-27-01 (detekt/TextCard.kt) is currently non-reproducing (0 smells in this run) but is out of scope for this plan's `files_modified` -- documented via a note rather than silently closing an issue this plan didn't fix.

## Deviations from Plan

None - plan executed exactly as written through Task 2. Task 3 is the plan's own designed halt (`checkpoint:decision`, `gate="blocking-human"`) -- executing it would violate the plan's own reversibility gate and this repo's CLAUDE.md, not a deviation.

## Issues Encountered

None. All verification commands passed on first attempt; the KI's stated root cause and fix matched the actual `metalava` plugin DSL (`hiddenAnnotations: SetProperty<String>`, confirmed by decompiling `me.tylerbwong.gradle.metalava:plugin:0.5.0`'s `MetalavaExtension` class before editing `build.gradle.kts`).

## User Setup Required

None - no external service configuration required. **However, a human decision is required to proceed:** see "Next Phase Readiness" below.

## Next Phase Readiness

**BLOCKED on human go-ahead.** All hub gates are green on `main` at commit `ff3d9c6`. To cut and push the immutable `v2.2.0` tag, run from this repo root:

```
git tag -a v2.2.0 -m "v2.2.0 -- Ship & coordinated repin: line reunification (REUNI-01..04), chip-color slot (TAGCOLOR-01), MicButton hardening (MICBTN-01..03), apiCheck KI-2026-09-02-01 fix (SHIP-01)"
git push origin v2.2.0
```

Once pushed, Plan 09-02 (JitPack resolution verification + ECOSYSTEM.md reconcile) can proceed -- its precondition requires the tag to already exist at origin. This plan does not, and must not, execute the above commands itself.

---
*Phase: 09-ship-coordinated-repin*
*Completed (through Task 2; halted at Task 3): 2026-09-27*
