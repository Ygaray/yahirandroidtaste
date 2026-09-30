---
phase: 10-voice-command-settings-surfaces
plan: 02
subsystem: ui
tags: [compose, material3, jetpack-compose, api-surface, exposed-dropdown-menu, registry, tdd]

# Dependency graph
requires:
  - phase: 10-voice-command-settings-surfaces
    provides: "Plan 01's ProviderKeyCard tracer — family scaffold, PRIMITIVE_NOUN_ALLOWLIST widening (Provider/Model/Approach), ExposedDropdownMenuBox pattern, all-val model convention"
provides:
  - ModelSelectCard (VSET-02) — model dropdown from props, empty→disabled+reason, prop-driven
  - ApproachLadderCard (VAPPR-01/02/03) — ordered tier ladder, offline-only toggle, max-tier cap
    behind a swappable CapControl seam, all prop-driven
  - ModelOptionUiModel / ApproachRungUiModel all-val UI models
  - Voice Command registry family now holds all three Phase 10 settings cards
affects: [13-catalog-ship]

# Actuals (#2632) — pairs with the plan's estimate to calibrate future estimates.
actuals:
  tokens: 10674
  tasks: 2
  commits: 4

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "RED-phase stub pattern for typed-language TDD: a compiling-but-behaviorally-empty composable lets the test suite produce real assertion failures instead of compile errors, satisfying a genuine RED gate"
    - "Shared-testTag + onAllNodesWithTag ordering assertion (mirrors VoiceCardClipListTest's 'voice_clip_row' convention) — Compose semantics carries exactly one TestTag per node, so per-item identification during tests uses fixture-unique label text instead of a second tag"
    - "Modifier.semantics(mergeDescendants = true) on a non-Material row composable so child Text nodes merge into one queryable/clickable semantics node — needed whenever a plain Row (not a Material clickable primitive) must read as one unit to Compose UI tests"
    - "CapControl swap seam (D-03): a private composable isolating ONLY the interaction wrapper (clickable Row) around rung content, so swapping tap-a-rung for SingleChoiceSegmentedButtonRow later touches one function, not the ladder's rendering shape"

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/ModelOptionUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/ApproachRungUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
    - api.txt

key-decisions:
  - "TDD RED phase for both tasks used a compiling-but-empty stub composable (Surface with no content) rather than omitting the production file entirely — Kotlin's static typing means a test file referencing a non-existent composable is a compile error (INVALID_RED per #3770's classification, not a real assertion failure), so the stub is what makes the target tests fail on a genuine AssertionError instead."
  - "gsd_run check tdd-red-evidence was not run for either task's RED phase: that verb's TAP parser (parseNodeTestSummary/tapFailedTestNames) is hardcoded to Node's `--test` TAP output format (# tests/# pass/# fail, ok/not ok lines) and cannot parse Gradle/JUnit XML/console output at all — it would misclassify every genuine Kotlin/Gradle RED as zero_tests_discovered regardless of test quality. workflow.tdd_mode is false in this project's config, so the automated gate-enforcement step (which is conditioned on that flag) does not apply here; the dispatch prompt's explicit instruction was the narrower 'RED commit → GREEN commit contract' (test-first, confirm real failure, then implement), which both tasks followed and verified manually via the actual Gradle/JUnit XML failure reports (AssertionError at the target test, not a compile/discovery failure)."
  - "ApproachLadderCard's rung rows needed explicit Modifier.semantics(mergeDescendants = true) — a plain Row (unlike Material3's clickable/Button primitives, which merge by default) does not merge child Text semantics into itself, so the list-order test's onAllNodesWithTag(...)[i].assert(hasText(...)) initially failed even though the GREEN implementation was otherwise correct. Fixed inline during the same GREEN cycle (not a separate deviation — discovered via the TDD loop's own 'iterate until green' step)."
  - "Per-rung identification in tests uses fixture-unique label text (not a second testTag) — Compose semantics carries exactly one TestTag value per node, so a shared ordering tag and a per-rung unique tag cannot coexist on the same node; the shared tag (onAllNodesWithTag) proves order/count, and onNodeWithText proves per-rung state."

requirements-completed: [VSET-02, VAPPR-01, VAPPR-02, VAPPR-03]

coverage:
  - id: D1
    description: "ModelSelectCard renders the selected model label from props and emits selection via onModelSelected; empty models renders a disabled affordance with a visible reason string, never a blank control"
    requirement: "VSET-02"
    verification:
      - kind: automated_ui
        ref: "ModelSelectCardTest#renders the currently-selected model label from props"
        status: pass
      - kind: automated_ui
        ref: "ModelSelectCardTest#choosing a model from the dropdown emits onModelSelected with the tapped id"
        status: pass
      - kind: automated_ui
        ref: "ModelSelectCardTest#when models is empty renders a disabled state with the reason string, not a blank control"
        status: pass
    human_judgment: false
  - id: D2
    description: "ApproachLadderCard renders the ordered tier ladder in LIST order from props (never re-sorted by rank)"
    requirement: "VAPPR-01"
    verification:
      - kind: automated_ui
        ref: "ApproachLadderCardTest#renders each ladder rung label in list order"
        status: pass
    human_judgment: false
  - id: D3
    description: "The offline-only toggle reflects and emits offlineOnly state via onOfflineOnlyChange; hidden entirely when the offlineOnly/onOfflineOnlyChange props are null"
    requirement: "VAPPR-02"
    verification:
      - kind: automated_ui
        ref: "ApproachLadderCardTest#toggling offline-only emits onOfflineOnlyChange with the new value"
        status: pass
      - kind: automated_ui
        ref: "ApproachLadderCardTest#offline-only toggle is not rendered when the prop is null"
        status: pass
    human_judgment: false
  - id: D4
    description: "The max-tier cap reflects maxTierId and emits onMaxTierChange via tap-a-rung; rungs above the cap render greyed-but-present (still visible); hidden entirely when the cap props are null (no rung is clickable)"
    requirement: "VAPPR-03"
    verification:
      - kind: automated_ui
        ref: "ApproachLadderCardTest#tapping a rung emits onMaxTierChange with the tapped id when cap props are present"
        status: pass
      - kind: automated_ui
        ref: "ApproachLadderCardTest#a rung above the cap renders greyed-but-present"
        status: pass
      - kind: automated_ui
        ref: "ApproachLadderCardTest#cap control is not rendered when maxTierId is null -- rungs are not clickable"
        status: pass
    human_judgment: false
  - id: D5
    description: "Per-rung effective state (needs-network affordance when offline-only is on and the rung is not offline-capable) is derived in-composable from plain primitives, with no TierPolicy engine dependency"
    requirement: "VAPPR-02"
    verification:
      - kind: automated_ui
        ref: "ApproachLadderCardTest#offline-only on gives an offline-incapable rung a needs-network affordance while staying visible"
        status: pass
    human_judgment: false
  - id: D6
    description: "Both new cards are registered in the Voice Command family (CATALOG-03); full suite + detekt + apiCheck stay green; public API additive vs v2.3.0"
    requirement: "VSET-02"
    verification:
      - kind: unit
        ref: "./gradlew testDebugUnitTest (full suite, includes ComponentRegistryDriftGuardTest + DomainVocabularyDriftGuardTest + GeneratedSymbolDriftGuardTest, all zero failures)"
        status: pass
      - kind: unit
        ref: "./gradlew detekt (0 code smells)"
        status: pass
      - kind: unit
        ref: "./gradlew apiCheck (api.txt additive diff vs v2.3.0, manually reviewed)"
        status: pass
    human_judgment: true
    rationale: "Automated verification proves the props/callbacks/registration/API-surface contract is correct. It cannot judge visual polish (tap-a-rung feel, hidden-vs-shown-disabled control layout, light/dark contrast) — that visual review is explicitly deferred to Gate-1 per this plan's own <verification> section, matching Plan 01's precedent."

# Metrics
duration: 40min
completed: 2026-09-30
status: complete
---

# Phase 10 Plan 02: Model Select + Approach Ladder Cards Summary

**Expanded the Voice Command settings surface with `ModelSelectCard` (VSET-02) and `ApproachLadderCard` (VAPPR-01/02/03) via full RED→GREEN TDD cycles, both registered in the tenth registry family with a strictly-additive API surface.**

## Performance

- **Duration:** ~40 min
- **Started:** 2026-09-30T19:30:00Z
- **Completed:** 2026-09-30T20:08:17Z
- **Tasks:** 2 (both `type="auto" tdd="true"`)
- **Files modified:** 8 (6 created, 2 modified)

## Accomplishments

- `model/ModelOptionUiModel.kt` and `model/ApproachRungUiModel.kt` — all-`val` immutable UI models (D-04), mirroring `ListItemUiModel`'s shape and avoiding the `TagChipUiModel` `var`/STABLE trap; `ApproachRungUiModel.offlineCapable` is explicitly app-derived (D-06), never sourced from any voice-action-engine `TierPolicy` type.
- `component/ModelSelectCard.kt` — a presentational, hoisted-state card reusing Plan 01's `ExposedDropdownMenuBox` pattern for model selection; when `models` is empty it renders a disabled caption carrying the caller's `emptyReason`, never a blank dropdown.
- `component/ApproachLadderCard.kt` — ONE composable with three prop groups (ladder + offline-only + cap), per CONTEXT specifics:
  - Renders `ladder` as a vertical column in LIST order (VAPPR-01) — proven by a dedicated order test using an intentionally-unsorted fixture.
  - An optional offline-only toggle (`SegmentedOptionSelector`, 2 options) reflects/emits `offlineOnly` and is entirely absent when the prop pair is null (VAPPR-02, D-05).
  - An optional max-tier cap: tapping a rung emits `onMaxTierChange`, implemented behind a private `CapControl` seam (D-03) so swapping to `SingleChoiceSegmentedButtonRow` later is a one-line change; rungs above the cap stay visible with a "Capped" affordance (conditional-render-no-dead-space); the whole control is absent when the cap prop pair is null (VAPPR-03, D-05).
  - Per-rung effective state (`enabled && (!offlineOnly || offlineCapable) && rank<=cap`) is derived in-composable from primitives only (D-06) — a rung needing network gets a "Needs network" affordance while staying visible.
- Both cards registered in `voiceCommandFamilyEntries` (`explorer/VoiceCommandFamilyScreen.kt`), `tier = PATTERN`, each with a 4-cell states matrix and fixtures/Variants demos.
- `ModelSelectCardTest.kt` (3 tests) and `ApproachLadderCardTest.kt` (7 tests) — 10 new Compose-UI tests, all green.
- `api.txt` regenerated via `apiDump` twice (once per task); both diffs are strictly additive vs `v2.3.0` (confirmed by manual diff review and `./gradlew apiCheck`).

## Task Commits

Both tasks followed the full TDD RED→GREEN cycle (per dispatch-prompt instruction, `tdd="true"` binds per-task regardless of the project's global `workflow.tdd_mode: false`):

1. **Task 1: ModelSelectCard (VSET-02)**
   - RED: `203b036` (test) — failing tests against a compiling-but-empty stub; confirmed real `AssertionError`s (not compile errors) via `./gradlew testDebugUnitTest --tests "*ModelSelectCard*"`.
   - GREEN: `eed36ba` (feat) — full implementation + registration; all 3 tests pass, full suite + detekt + apiCheck green.
2. **Task 2: ApproachLadderCard (VAPPR-01/02/03)**
   - RED: `b4d0401` (test) — failing tests against a compiling-but-empty stub; confirmed 6/7 target tests failed with real `AssertionError`s (the 7th, "toggle not rendered when null," legitimately passes in both RED and GREEN — a negative-existence test, not a target behavior proof).
   - GREEN: `9e9021f` (feat) — full implementation + registration (including an inline `mergeDescendants` semantics fix found during the GREEN iteration loop — see Decisions); all 7 tests pass, full suite + detekt + apiCheck green.

No REFACTOR commits — both GREEN implementations mirror established Plan 01 patterns cleanly with no follow-up cleanup needed.

**Plan metadata:** committed alongside this SUMMARY.

## Files Created/Modified

- `src/main/java/io/github/ygaray/yahirandroidtaste/model/ModelOptionUiModel.kt` — model dropdown row model (id, label, subtitle?, badge?)
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/ApproachRungUiModel.kt` — tier-ladder rung model (id, label, rank, enabled, offlineCapable, description?)
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt` — the new VSET-02 card + private `ModelDropdown`
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt` — the new VAPPR card + private `RungRow`/`CapControl`
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt` — two new registry entries + fixtures/Variants
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCardTest.kt` — 3 Compose-UI tests
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt` — 7 Compose-UI tests
- `api.txt` — regenerated via `apiDump` (twice), additive-only diff both times

## Decisions Made

See `key-decisions` in frontmatter for the full rationale on: (1) the RED-phase stub pattern needed for statically-typed TDD, (2) why `gsd_run check tdd-red-evidence` was not run (TAP-format-only parser, inapplicable to Gradle/JUnit, and not gated by this project's `workflow.tdd_mode: false`), (3) the `mergeDescendants` semantics fix, and (4) shared-tag-plus-text per-rung test targeting.

## Deviations from Plan

None — plan executed exactly as written. Both tasks' production edits stayed within each task's declared `<files>` list; no architectural changes, no missing-critical-functionality gaps, and no blocking issues required a fix outside the plan's own scope. The `mergeDescendants` fix and the test-targeting adjustment (documented above) were both iterations WITHIN Task 2's own GREEN phase on files already in that task's file list — exactly the TDD reference's "iterate until green" step, not unplanned work.

## Issues Encountered

- **`gsd_run check tdd-red-evidence`'s TAP parser cannot classify Gradle/JUnit test output** (see key-decisions) — a known tooling gap for non-Node ecosystems, out of this plan's scope to fix. Both RED phases were instead verified manually via the actual Gradle/JUnit XML failure reports, confirming genuine `AssertionError`s at the named target tests (not compile errors, not zero-test discovery) before proceeding to GREEN.
- **Compose semantics merge surprised the first GREEN attempt for `ApproachLadderCard`** — a plain `Row` (unlike Material3's own clickable primitives) does not merge child `Text` semantics by default, so `onAllNodesWithTag(...)[i].assert(hasText(...))` failed even though the visual implementation was correct. Resolved by adding `Modifier.semantics(mergeDescendants = true)` to the row wrapper — documented as a reusable pattern in `tech-stack.patterns` for future non-Material interactive rows in this hub.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- All three Phase 10 settings cards (`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`) are now built, registered, and prop-driven with zero engine/network coupling — the Voice Command family is complete for this phase's scope (VSET-01/02, VAPPR-01/02/03).
- `PRIMITIVE_NOUN_ALLOWLIST`'s Provider/Model/Approach widening from Plan 01 covered both this plan's new head tokens with zero further edits to `DomainVocabularyDriftGuardTest.kt`.
- The `CapControl` swap seam (D-03) is isolated and documented — if Gate-1/design review finds tap-a-rung's feel wrong, swapping to `SingleChoiceSegmentedButtonRow` touches one private composable, not the ladder's rendering shape.
- No blockers. Gate-1 (agentic/human on-device verification, out of this executor's lane) should confirm per this plan's own deferred `<verification>` item: tap-a-rung cap feel, and hidden-vs-shown-disabled control layout, in `ExplorerActivity` → Voice Command family, light + dark.
- Phase 10 (both plans) is now complete pending Gate-1 sign-off; ready for Phase 13 (catalog + API + one-way-dependency integrity ship gate) per STATE.md's roadmap sequencing note.

---
*Phase: 10-voice-command-settings-surfaces*
*Completed: 2026-09-30*

## Self-Check: PASSED

- All 6 created files verified present on disk (`test -f`): `ModelOptionUiModel.kt`, `ApproachRungUiModel.kt`, `ModelSelectCard.kt`, `ApproachLadderCard.kt`, `ModelSelectCardTest.kt`, `ApproachLadderCardTest.kt`.
- All 4 commits verified in `git log --oneline`: `203b036` (test, Task 1 RED), `eed36ba` (feat, Task 1 GREEN), `b4d0401` (test, Task 2 RED), `9e9021f` (feat, Task 2 GREEN).
- All plan-level `<acceptance_criteria>`/must_haves re-verified: `./gradlew testDebugUnitTest detekt apiCheck` green (0 failures, 0 code smells); `./gradlew testDebugUnitTest --tests "*ModelSelectCard*"` green (3/3); `./gradlew testDebugUnitTest --tests "*ApproachLadderCard*"` green (7/7); `ComponentRegistryDriftGuardTest`/`DomainVocabularyDriftGuardTest`/`GeneratedSymbolDriftGuardTest` all `failures="0"`; `api.txt` diff additive-only vs `v2.3.0` (manually reviewed both `apiDump` diffs).
