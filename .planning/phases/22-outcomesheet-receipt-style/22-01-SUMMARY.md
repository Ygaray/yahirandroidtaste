---
phase: 22-outcomesheet-receipt-style
plan: 01
subsystem: ui
tags: [compose, api-surface, source-compat, outcome-sheet, receipt, metalava]

requires:
  - phase: 20-tone-and-action-buttons
    provides: "ToneColors, ToneDefaults, ActionButtonEmphasis, ActionButtonDefaults.ActionButtonRole"
  - phase: 21-receipt-row-pill-selector-sheet-shape
    provides: "SheetScaffold(shape) for the Receipt frame (consumed in 22-02)"
provides:
  - "OutcomeBadge, FailureTone, SheetActionUiModel (with activeWhileInFlight) public types"
  - "HandledByUiModel.prefixLabel/onDevice, FailureActionUiModel.icon"
  - "Success.badge/titleActions/startAction/endAction; Failure.title/transcript/tone/tip/secondaryAction/badge/undo with redacting toString; NeedsConfirmation.confirmEnabled/titleActions"
  - "OutcomeSheetStyle, OutcomeSheetColors, OutcomeSheetDefaults (colors, ReceiptTitleStyle), Dimens.OutcomeReceipt"
  - "OutcomeSheetSourceCompatV251Test: compile-only v2.5.1 call-shape canary"
affects: [22-02, 22-03, 22-04, 22-05, 24-docs]

actuals:
  tokens: 15000
  tasks: 3
  commits: 3
plan_head_before: fc4308d99bda2fc2a9bd48232172aabcd3e4613b

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/OutcomeBadge.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureTone.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/SheetActionUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetDefaults.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/model/OutcomeReceiptModelsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetSourceCompatV251Test.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetDefaultsTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/FailureActionUiModel.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/theme/DimensTest.kt
    - api.txt

key-decisions:
  - "No hand-written forwarding was needed in the HandledByUiModel / FailureActionUiModel / Failure legacy copies: the generated full-arity copy defaults omitted fields to the receiver, so the existing delegations already carry every new field (proved by OutcomeReceiptModelsTest)"
  - "Failure.toString prints only the tone and the presence of action, secondaryAction, undo, body and tip (T-22-01)"
  - "OutcomeSheetColors is a plain @Immutable class with value equality, not a data class, so no copy/componentN is published"

requirements-completed: [VRCPT-01, VRCPT-02, VRCPT-03, VRCPT-04, VRCPT-05, VXR-02]

coverage:
  - id: D1
    description: "Every v2.5.1 call shape of the models still compiles and yields the old behavior (Success 1..6 positional args, Failure 1..3 args, destructuring, NeedsConfirmation named shape, 5/6-arg HandledByUiModel, FailureActionUiModel trailing lambda, positional 3-arg OutcomeSheet)"
    requirement: VRCPT-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetSourceCompatV251Test.kt#v251StateArmShapes_compileAndDefaultTheNewFields"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetSourceCompatV251Test.kt#v251PositionalOutcomeSheetCallShape_compiles"
        status: pass
    human_judgment: false
  - id: D2
    description: "Legacy-arity copies carry every new field forward; Failure.toString never prints reason, title, transcript or semanticsPrefix"
    requirement: VRCPT-04
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/model/OutcomeReceiptModelsTest.kt#Failure legacy three-argument copy keeps every new field"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/model/OutcomeReceiptModelsTest.kt#Failure toString omits the reason, title, transcript and semanticsPrefix"
        status: pass
    human_judgment: false
  - id: D3
    description: "Receipt model fields and tokens exist with the D-01 shape: OutcomeSheetStyle/OutcomeSheetColors/OutcomeSheetDefaults, Dimens.OutcomeReceipt, SheetActionUiModel.activeWhileInFlight"
    requirement: VRCPT-03
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetDefaultsTest.kt#colors defaults equal the ToneDefaults warn and error tones"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/theme/DimensTest.kt#OutcomeReceipt tokens equal their phase 22 values"
        status: pass
    human_judgment: false

duration: 40min
completed: 2026-10-12
status: complete
---

# Phase 22 Plan 01: Receipt model, style and token API Summary

**Every public type and model field the Receipt OutcomeSheet needs now exists (three leaf-first commits, source-compatible, `api.txt` regenerated in each, apiCheck and detekt green at the zero baseline); no rendering changed.**

## Accomplishments

- New public types: `OutcomeBadge { Success, Reverted, Error, Warn }`, `FailureTone { Error, Warn }`, `@Immutable data class SheetActionUiModel` (with the accepted `activeWhileInFlight = false`), `OutcomeSheetStyle { Classic, Receipt }`, `@Immutable class OutcomeSheetColors`, `object OutcomeSheetDefaults` (`colors()` defaulting to `ToneDefaults.warn()` / `error()`, `ReceiptTitleStyle` = `titleLarge` 18/24 SemiBold) and the `Dimens.OutcomeReceipt` token group (15 tokens).
- Widened models (fields appended with defaults, D-02): `HandledByUiModel` (+`prefixLabel`, `onDevice`), `FailureActionUiModel` (+`icon`), `Success` (+`badge`, `titleActions`, `startAction`, `endAction`), `Failure` (+`title`, `transcript`, `tone`, `tip`, `secondaryAction`, `badge`, `undo`), `NeedsConfirmation` (`confirmEnabled`, `titleActions` inserted before `onConfirm`).
- `Failure.toString` is overridden to print only the tone and presence booleans (T-22-01); `NeedsConfirmation.toString` is unchanged.
- No hidden-deprecated overload, copy shim or old-arity copy was added; the v2.5.0 shims were not edited.

## Task Commits

| Task | Commit | Lane (classifier on the staged diff) | Message |
| ---- | ------ | ------------------------------------ | ------- |
| 1 | 51c905f | LANE 1, committed without override | feat(22-01): Receipt leaf types and HandledByUiModel / FailureActionUiModel widening |
| 2 | 3f58240 | LANE 2, committed with `HUB_LANE_OVERRIDE=2` | feat(22-01): widen Success, Failure and NeedsConfirmation for the Receipt style |
| 3 | 4ab499f | LANE 1 (additions only), no override | feat(22-01): OutcomeSheetStyle, OutcomeSheetColors, OutcomeSheetDefaults and Dimens.OutcomeReceipt |
| SUMMARY | (this commit) | docs only | docs(22-01): summary |

The plan expected LANE 2 for all of tasks 1 and 2. The classifier reported LANE 1 for task 1: its only rewritten `src/main` lines are the old last constructor parameter gaining a trailing comma, which `verify-additive-diff.sh` tolerates (the same as the 21-02 forwarding-line comma). Task 2 rewrote a non-comma line (the `Failure` KDoc sentence about `toString`), so it classified LANE 2 and was blocked until `HUB_LANE_OVERRIDE=2` was declared. Task 3 is pure additions plus an appended `Dimens` group.

## Arity pins (A2, hand-derived vs observed)

All three hand-derived sets were confirmed by running `VoiceModelLabelDefaultsTest` unchanged after the edit (no difference to record):

| Class | Pin | Derived | Observed |
| ----- | --- | ------- | -------- |
| HandledByUiModel | constructors | contains {5, 6} | pass |
| HandledByUiModel | copy | {5, 8} | pass |
| FailureActionUiModel | constructors and copy | {2, 4} | pass |
| Failure | constructors | {1, 2, 3, 4, 5} (unchanged) | pass |
| Failure | copy | {3, 12} | pass |

Test titles that claimed the old "six-arg" / "three-arg" copy were renamed. No other test in that file was touched.

## `api.txt` removed lines per commit (each has a same-member added counterpart)

- Commit 1 (3 removed): `FailureActionUiModel` three-argument constructor, `FailureActionUiModel.copy(label, onClick, role)`, `HandledByUiModel.copy(... escalationsLabel)` (full-arity copy, now with the extra params). Added: the 4-parameter `FailureActionUiModel` constructor, `component4`, `getIcon`, `property icon`; the new `FailureTone`, `OutcomeBadge`, `SheetActionUiModel`; the two extra `HandledByUiModel` `@JvmOverloads` constructors, `component7`, `component8`, `getPrefixLabel`, `getOnDevice`, the 8-parameter `copy`.
- Commit 2 (6 removed): `Failure.copy` (5-parameter full-arity form), `NeedsConfirmation` constructor, `component10`, `component11`, `NeedsConfirmation.copy`, `Success` constructor and `Success.copy` (all old full-arity forms, each replaced by the widened form).
- Commit 3: `git diff --numstat -- api.txt` reported `38 0` (additions only: `OutcomeSheetStyle`, `OutcomeSheetColors`, `OutcomeSheetDefaults`, `Dimens.OutcomeReceipt`). `git diff --exit-code -- api.txt` after the final dump: exit 0.

## Verification

- Task 1 command (focused classes plus `OutcomeSheetTest`, `DataClassBinaryCompatShimTest`, `VoiceI18nSourceCompatTest`, detekt, apiCheck): BUILD SUCCESSFUL in 1m 10s.
- Task 2 command (`apiDump testDebugUnitTest detekt apiCheck`, full unfiltered suite): BUILD SUCCESSFUL in 1m 22s; 989 tests, 22 skipped (pre-existing), 0 failures, 0 errors.
- Task 3 command (`OutcomeSheetDefaultsTest`, `OutcomeReceiptModelsTest`, `DimensTest`, `explorer.*`, detekt, apiCheck): BUILD SUCCESSFUL in 45s. No registry or vocabulary allowlist edit was needed (no top-level `@Composable fun` was added).
- `git diff --exit-code v2.5.1 -- OutcomeSheetTest.kt FailureRoleSourceContractTest.kt DataClassBinaryCompatShimTest.kt`: exit 0 (byte-identical).
- `NeedsConfirmation.toString` body unchanged (no diff line against v2.5.1); the exact text is still pinned by `OutcomeSheetTest`.
- No `DeprecationLevel.HIDDEN` line was added in any commit.

## Deviations from Plan

**1. [Tooling] Commit ledger not persisted.** The sandbox refuses compound `git rev-parse --git-dir` redirects, so no ledger file was written. `plan_head_before` is the worktree base fc4308d and `commits: 3` was measured with `git rev-list --count fc4308d..HEAD` before this SUMMARY commit (same approach as 21-01 to 21-04).

**2. [Plan expectation] Task 1 classified LANE 1, not LANE 2** (see Task Commits); no override was declared there since none was needed. Task 2 did need the override.

**3. [Process] Tests written together with the implementation.** The new test classes were written first and the implementation next, but the red step was not run as a separate gradle invocation (the plan allows "where cheap"; one gradle run at a time was the binding constraint).

**4. [Scope] Extra `VoiceModelLabelDefaultsTest` titles.** Only the three pin assertions and their titles changed, as specified.

**Total deviations:** 4, none changing behavior or scope.

## Worktree note on the drift guards

`ComponentRegistryDriftGuardTest` and `DomainVocabularyDriftGuardTest` walk up to the first directory named `yahirandroidtaste`, which is the MAIN checkout when run from this `agent-*` worktree (the 20-04 / 21-04 finding). No new public top-level composable was added here so nothing is at risk, but their green result in this worktree says nothing about this worktree's sources; the real confirmation is one full `./gradlew testDebugUnitTest` in the main checkout after merge.

## Deferred

- README.md, API.md and `ComponentRegistry` KDoc counts (Phase 24).
- Rendering of every new field (`OutcomeSheet` signature change in 22-02; Receipt composables in 22-02..22-05).

## Self-Check: PASSED

- Files exist: all seven created and seven modified files listed in the frontmatter.
- Commits 51c905f, 3f58240 and 4ab499f exist on `worktree-agent-ae749bf0c68f73ad1`.
- STATE.md and ROADMAP.md untouched; nothing pushed or tagged; no device touched.
