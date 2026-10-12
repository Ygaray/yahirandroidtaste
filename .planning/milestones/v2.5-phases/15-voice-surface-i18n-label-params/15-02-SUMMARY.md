---
phase: 15-voice-surface-i18n-label-params
plan: 02
subsystem: ui
tags: [compose, i18n, label-params, additive-api, metalava, data-class]

requires:
  - phase: 15-voice-surface-i18n-label-params
    provides: plan 15-01 composable label params; api.txt fresh at HEAD
provides:
  - UndoRowUiModel.undoneLabel (VI18N-04, D-02 per-row)
  - UndoRefusedUiModel.refusedPrefix and changedSinceSuffix (VI18N-04)
  - The proven "@JvmOverloads constructor + hand-written old-arity copy" Metalava-safe recipe for appending fields to a shipped data class
affects: [15-03, 16-approach-ladder-a11y, 17-router-toggle, 18-catalog-api-docs]

plan_head_before: 1923ab9f7bf875db05a1bd1567645a1b638a8568
actuals:
  tokens: 9000
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Append defaulted field to shipped data class: @JvmOverloads constructor + hand-written old-arity copy delegating to generated full-arity copy with the instance's current new fields"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRowUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRefusedUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt
    - api.txt

key-decisions:
  - "Commits carry HUB_LANE_OVERRIDE=2 (the lane the pre-commit hook detects against v2.4.1), not the plan's 3"
  - "UndoRowState.Undone left a data object; undoneLabel is per-row on UndoRowUiModel (D-02)"
  - "Refused-message word order is fixed (prefix, reason, optional ', item suffix'); languages needing another order fold the item into reason and pass changedItem = null (documented in KDoc)"

patterns-established:
  - "Model-field append recipe (see tech-stack.patterns); apiCheck green BEFORE apiDump; api.txt in the same commit"

requirements-completed: [VI18N-04]

coverage:
  - id: D1
    description: "An Undone row in OutcomeSheet renders caller-supplied UndoRowUiModel.undoneLabel; default stays 'Undone'; row stays non-clickable"
    requirement: VI18N-04
    verification:
      - kind: unit
        ref: "OutcomeSheetTest#an Undone row renders a caller-supplied undoneLabel instead of the English default; #an Undone row renders muted trailing text and is never clickable (pre-existing, unchanged)"
        status: pass
    human_judgment: false
  - id: D2
    description: "Refused surface renders '<refusedPrefix> <reason>' plus ', <changedItem> <changedSinceSuffix>' when changedItem non-null; English defaults byte-identical"
    requirement: VI18N-04
    verification:
      - kind: unit
        ref: "OutcomeSheetTest#a refused with caller-supplied prefix and suffix...; #a refused with a custom prefix and a null changedItem...; two pre-existing refused English tests unchanged and green"
        status: pass
    human_judgment: false
  - id: D3
    description: "Old constructor and copy JVM arities survive; legacy copy preserves custom labels; defaults pinned; new field participates in equality"
    requirement: VI18N-04
    verification:
      - kind: unit
        ref: "VoiceModelLabelDefaultsTest (9 tests: defaults, legacy copy preservation, equality, reflection constructor/copy arity sets)"
        status: pass
    human_judgment: false
  - id: D4
    description: "Metalava apiCheck green via the recipe; api.txt regenerated per commit with exactly one removed line; UndoRowState.Undone untouched; explorer/ and feedback/ unchanged vs v2.4.1"
    verification:
      - kind: other
        ref: "./gradlew apiCheck (before each apiDump), cmp-fresh api.txt, detekt, full testDebugUnitTest (675 tests, 0 failures)"
        status: pass
    human_judgment: false

duration: 8min
completed: 2026-10-05
status: complete
---

# Phase 15 Plan 02: OutcomeSheet undo-surface label fields Summary

**OutcomeSheet's "Undone" row text and "Couldn't undo: ... changed since" refused message are now caller-overridable through defaulted fields on `UndoRowUiModel` and `UndoRefusedUiModel`, using a `@JvmOverloads` + old-arity `copy` recipe that keeps Metalava `apiCheck` green.**

## Performance

- **Duration:** ~8 min
- **Tasks:** 2 (1 tracer + 1 auto)
- **Files modified:** 6 (1 created)

## Accomplishments

- `UndoRowUiModel.undoneLabel` (default `"Undone"`) appended after `state`; OutcomeSheet's private `UndoRowItem` Undone arm renders `row.undoneLabel`.
- `UndoRefusedUiModel.refusedPrefix` (`"Couldn't undo:"`) then `changedSinceSuffix` (`"changed since"`) appended after `changedItem`; the refused Surface composes `"<prefix> <reason>[, <item> <suffix>]"`, English output byte-identical.
- New pure-JVM `VoiceModelLabelDefaultsTest` pins defaults, legacy-copy label preservation, equality and the old+new constructor/copy arities by reflection (plan 15-03 extends it).

## Task Commits

1. **Task 1 (tracer): UndoRowUiModel undoneLabel** - `db8a069` (feat)
2. **Task 2: UndoRefusedUiModel refusedPrefix + changedSinceSuffix** - `f3f1e1c` (feat)

## api.txt delta per commit

apiCheck was green BEFORE each apiDump (against the previously committed api.txt).

- `db8a069`: removed 1 - `UndoRowUiModel copy(optional String id, optional String label, optional ...UndoRowState state)`. Added: 4-arg ctor, `component4`, hand-written `copy(String id, String label, ...UndoRowState state)`, full-arity `copy(... optional String undoneLabel)`, `getUndoneLabel`, `property undoneLabel`. The 3-arg ctor line remains.
- `f3f1e1c`: removed 1 - `UndoRefusedUiModel copy(optional String reason, optional String? changedItem)`. Added: ctors with 1/3/4 params (2-param ctor line remains), `component3`, `component4`, hand-written `copy(String reason, String? changedItem)`, full-arity 4-param copy, `getRefusedPrefix`, `getChangedSinceSuffix`, two property lines.
- Cumulative `git diff v2.4.1 -- api.txt` removed lines: the four composables from 15-01 plus the two superseded generated `copy(optional ...)` lines (6 total), as the plan predicted.

## Decisions Made

- `UndoRowState.Undone` stays a `data object` (converting would delete the public `INSTANCE` symbol); label is per-row on the model (D-02).
- Legacy-arity `copy` delegates the CURRENT instance's new fields, so a three-/two-argument legacy copy never resets a custom label.
- **Word-order residual:** the refused message order is fixed (prefix, space, reason, optionally comma + item + suffix). A language needing a different order should fold the item into `reason` and pass `changedItem = null`; documented in `UndoRefusedUiModel` KDoc.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] HUB_LANE_OVERRIDE=2 instead of 3**
- **Found during:** Task 1 commit (pre-flagged by 15-01 and the orchestrator)
- **Issue:** Plan says `HUB_LANE_OVERRIDE=3`; the pre-commit hook (baseline v2.4.1, mode=additive) detects lane 2 and only accepts an override equal to the detected lane.
- **Fix:** Both commits use `HUB_LANE_OVERRIDE=2` per `tools/README-api-guard.md`, applied only after apiCheck was green and the api.txt delta matched expectations.
- **Committed in:** db8a069, f3f1e1c

---

**Total deviations:** 1 (Rule 3). **Impact:** none on code.

## Issues Encountered

None. Full suite: 675 tests, 0 failures; detekt (zero baseline) green; `apiDump` idempotent against the committed api.txt; `explorer/` and `feedback/` unchanged vs v2.4.1.

## Note for Phase 16 / 17 planners

The same Metalava data-class trap applies to any field appended to `FailureActionUiModel` or `VoiceOutcomeUiState.Failure`: reuse this plan's `@JvmOverloads constructor` + hand-written old-arity `copy` recipe (pass the instance's current new fields in the delegating copy). Expect lane 2, not 3, from the hook.

## Next Phase Readiness

- Plan 15-03 (HandledByUiModel, ProposedItemUiModel, VI18N-04 part 2) can extend `VoiceModelLabelDefaultsTest` and reuse the recipe. api.txt is fresh and apiCheck green at HEAD.

## Self-Check: PASSED

- Commits `db8a069`, `f3f1e1c` present in `git log`; all five source/test/api files per commit exist and are committed; SUMMARY frontmatter `commits: 2` measured from `1923ab9..HEAD`.
