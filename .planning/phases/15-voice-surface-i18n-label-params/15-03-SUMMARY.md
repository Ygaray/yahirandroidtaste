---
phase: 15-voice-surface-i18n-label-params
plan: 03
subsystem: ui
tags: [compose, i18n, label-params, additive-api, metalava, data-class, source-compat]

requires:
  - phase: 15-voice-surface-i18n-label-params
    provides: plans 15-01 (composable label params) and 15-02 (undo-model fields + the JvmOverloads/old-arity copy recipe)
provides:
  - HandledByUiModel.escalationsLabel (VI18N-04)
  - ProposedItemUiModel.removeContentDescription (VI18N-04)
  - VoiceI18nSourceCompatTest compile-only v2.4.0 call-shape fixture (SC5 evidence)
  - Phase 15 closing gate green (full suite, detekt, apiCheck vs released v2.4.1, allowlist, untouched paths, imports)
affects: [16-approach-ladder-a11y, 17-router-toggle, 18-catalog-api-docs, 19-release]

plan_head_before: 3a5d5e91ab3d1b8e3cc3cec222a1896946e640f8
actuals:
  tokens: 11000
  tasks: 3
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Append defaulted field to shipped data class: @JvmOverloads constructor + hand-written old-arity copy delegating the instance's current new field (reused from 15-02)"

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt
    - api.txt

key-decisions:
  - "Commits for source+api.txt used HUB_LANE_OVERRIDE=2 (the lane the hook detects vs v2.4.1), not the plan's 3"
  - "ProposedItemUiModel.toString left byte-identical; the new label never enters it (privacy)"
  - "HandledByUiModel KDoc sentence claiming it 'introduces no copy()/var ABI trap' was reworded since a field is now appended"

patterns-established:
  - "Same Metalava-safe model recipe now applied to all four voice models; mandatory for FailureActionUiModel / VoiceOutcomeUiState.Failure in Phases 16/17"

requirements-completed: [VI18N-04]

coverage:
  - id: D1
    description: "HandledByUiModel.escalationsLabel renders '<label> <count>' in the handled-by caption; default 'Escalations: N' byte-identical; separator/order unchanged"
    requirement: VI18N-04
    verification:
      - kind: unit
        ref: "OutcomeSheetTest#handledBy renders a caller-supplied escalationsLabel in the secondary caption; #handledBy escalation caption keeps the English default when no label is supplied"
        status: pass
    human_judgment: false
  - id: D2
    description: "ProposedItemUiModel.removeContentDescription is the remove icon's content description; default 'Remove'; toString unchanged"
    requirement: VI18N-04
    verification:
      - kind: unit
        ref: "OutcomeSheetTest#remove controls announce a caller-supplied removeContentDescription...; #remove control keeps the English Remove description...; pre-existing toString test unchanged; VoiceModelLabelDefaultsTest custom-label toString"
        status: pass
    human_judgment: false
  - id: D3
    description: "Old constructor/copy arities survive for both models; legacy copy preserves custom labels (reflection pins 5/6 and 7/8)"
    requirement: VI18N-04
    verification:
      - kind: unit
        ref: "VoiceModelLabelDefaultsTest (17 tests)"
        status: pass
    human_judgment: false
  - id: D4
    description: "SC5 phase-wide: v2.4.0 call shapes (positional through former last param, old-arity ctor/copy, destructuring) compile and render English defaults; strictly additive vs released v2.4.1"
    verification:
      - kind: unit
        ref: "VoiceI18nSourceCompatTest (2 tests); full suite 689 tests 0 failures; apiCheck vs v2.4.1 baseline api.txt green"
        status: pass
    human_judgment: false

duration: 15min
completed: 2026-10-05
status: complete
---

# Phase 15 Plan 03: HandledBy / ProposedItem label fields + Phase 15 closing gate Summary

**`HandledByUiModel.escalationsLabel` and `ProposedItemUiModel.removeContentDescription` are now caller-overridable via the proven `@JvmOverloads` + old-arity `copy` recipe, English byte-identical, and the phase-wide additive gate (apiCheck against released v2.4.1) is green.**

## Accomplishments

- `HandledByUiModel.escalationsLabel` (default `"Escalations:"`) appended last; `HandledByRow` renders `"${handledBy.escalationsLabel} $it"`. KDoc summary corrected (no longer claims the class sidesteps the copy/var hazard).
- `ProposedItemUiModel.removeContentDescription` (default `"Remove"`) appended last; remove `Icon` uses it. `toString()` untouched (`ProposedItemUiModel(id=x, amended=false)`), re-pinned with a custom-label test.
- `VoiceModelLabelDefaultsTest` extended to 17 tests; `OutcomeSheetTest` +4 (override and default pin for each model).
- New compile-only `VoiceI18nSourceCompatTest` (positional ProviderKeyCard through `emptyProvidersReason`; ModelSelectCard/ClarificationBar/ApproachLadderCard through `modifier`; old-arity ctor/copy/destructuring of all four models).

## Task Commits

1. **Task 1 (tracer): HandledByUiModel escalationsLabel** - `9908eb9` (feat)
2. **Task 2: ProposedItemUiModel removeContentDescription** - `755205b` (feat)
3. **Task 3: source-compat fixture + closing gate** - `adc0ffa` (test)

## api.txt delta per source commit

apiCheck was green BEFORE each apiDump; `apiDump` re-run was idempotent (cmp fresh).

- `9908eb9`: removed 1 - `HandledByUiModel copy(optional String tier, ..., optional Integer? escalationCount)`. Added: ctors with 1/2/3/4/6 params (old 5-param ctor line remains), `component6`, hand-written `copy(String tier, String? approach, String? provider, String? model, Integer? escalationCount)`, full-arity six-param `copy`, `getEscalationsLabel`, property line.
- `755205b`: removed 1 - `ProposedItemUiModel copy(optional ... optional trailingContent)` (seven-param). Added: ctors with 2/3/4/5/6/8 params (old 7-param ctor line remains), `component8`, hand-written seven-argument `copy`, full-arity eight-param `copy`, `getRemoveContentDescription`, property line. `toString` line unchanged.
- `adc0ffa`: no api.txt change.

## Closing-gate results (Task 3)

- Full `testDebugUnitTest detekt`: BUILD SUCCESSFUL; 689 tests, 0 failures, 0 errors (22 skipped are pre-existing in unrelated classes, e.g. CardBaseTest, VoiceCardClipListTest); ComponentRegistryDriftGuardTest, DomainVocabularyDriftGuardTest, GeneratedSymbolDriftGuardTest, GalleryDemoInteractionTest all ran green; detekt zero baseline green.
- `apiCheck` against the RELEASED v2.4.1 `api.txt` (temporarily written, restored via `git checkout -- api.txt`): **BUILD SUCCESSFUL**; `git status --porcelain -- api.txt` empty afterwards.
- `apiDump` fresh: `cmp` identical to committed `api.txt`.
- Removed lines since v2.4.1 (8 total, all in the allowlist): ProviderKeyCard, ModelSelectCard, ClarificationBar, ApproachLadderCard composable methods (4) + generated `copy(optional ...)` of HandledByUiModel, ProposedItemUiModel, UndoRefusedUiModel, UndoRowUiModel (4). Allowlist check: OK.
- `explorer/`, `SegmentedOptionSelector.kt`, `feedback/` byte-identical to v2.4.1: OK.
- No new non-library import in `src/main` vs v2.4.1 (INV-01): OK.
- No `v2.5*` tag; `15-VALIDATION.md` untouched (`status: draft`, `nyquist_compliant: false` still present); no consumer touched.

## Deviations from Plan

**1. [Rule 3 - Blocking] HUB_LANE_OVERRIDE=2 instead of 3** on `9908eb9` and `755205b`. The pre-commit hook (baseline v2.4.1, mode=additive) detects lane 2 and accepts only an override equal to the detected lane (per 15-01/15-02 and `tools/README-api-guard.md`). Applied only after apiCheck was green and the api.txt delta matched.

**2. [Rule 3 - Informational] Task 3 test-only commit classified LANE 1** by the hook (no api.txt/src/main change in that commit), so the override was unnecessary; commit passed.

**3. Sequencing note:** tests for both tasks were authored together, then the Task 2 tests were held back from the Task 1 commit (so each commit compiles and is green alone) and re-applied for Task 2. No impact on content.

Total deviations: 3 (all process-level, no code impact).

## Residuals for Phase 18 docs / owner

1. `SegmentedOptionSelector.kt:65` accessibility state words "selected"/"not selected" stay English.
2. Refused-undo message fixes word order (prefix, reason, optional ", item suffix"); other grammars fold the item into `reason` and pass `changedItem = null`.
3. `feedback/UndoCenterScreen.kt:159` has its own English "Undone" on a different surface (untouched).
4. Field names `refusedPrefix` / `changedSinceSuffix` (and `undoneLabel`, `escalationsLabel`, `removeContentDescription`) are discretionary names a consumer adopts on repin (research assumption A2).
5. `tools/verify-api-additive.sh` raw-line check will report lane 2/3 for all of v2.5 until v2.5.0 is cut (declared D-01 false-positive; real hook lane here is 2).

## Note for Phase 16 / 17 planners

The `@JvmOverloads constructor` + hand-written old-arity `copy` recipe is mandatory for any field appended to `FailureActionUiModel` or `VoiceOutcomeUiState.Failure`. Expect lane 2 from the hook; run `apiCheck` before `apiDump`.

## Self-Check: PASSED

- Commits `9908eb9`, `755205b`, `adc0ffa` present in `git log`; `VoiceI18nSourceCompatTest.kt` exists; `commits: 3` measured from `3a5d5e9..HEAD`; `git status --porcelain -- api.txt src` empty.
