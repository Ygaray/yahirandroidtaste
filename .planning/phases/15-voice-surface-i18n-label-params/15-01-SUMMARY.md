---
phase: 15-voice-surface-i18n-label-params
plan: 01
subsystem: ui
tags: [compose, i18n, label-params, additive-api, metalava]

requires:
  - phase: 14-v2.4-voice-outcome-surface
    provides: ProviderKeyCard, ModelSelectCard, ClarificationBar, ApproachLadderCard (v2.4.x English-literal baseline)
provides:
  - ProviderKeyCard.providerLabel, ModelSelectCard.modelLabel (VI18N-01)
  - ClarificationBar.dismissLabel (VI18N-02)
  - ApproachLadderCard unavailableLabel / cappedLabel / needsNetworkLabel / onlineLabel / offlineOnlyLabel (VI18N-03)
affects: [15-02, 15-03, 16-approach-ladder-a11y, 17-router-toggle, 18-catalog-api-docs]

plan_head_before: 2f3d943dbe353660d23052b4fbd10fbe594347fe
actuals:
  tokens: 7000
  tasks: 3
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Append-last defaulted String label params; English default lives only on the public signature, private helpers take the label as a non-default param"

key-files:
  created: []
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBarTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
    - api.txt

key-decisions:
  - "Pre-commit hook classifies each re-signatured composable commit as lane 2 (not lane 3 as the plan stated); commits carry HUB_LANE_OVERRIDE=2, the detected lane, per tools/README-api-guard.md"
  - "SegmentedOptionSelector's accessibility state words (selected / not selected) stay English - out of VI18N-01..04"

patterns-established:
  - "apiCheck (vs committed api.txt) -> apiDump -> api.txt committed in the SAME commit as the source change"

requirements-completed: [VI18N-01, VI18N-02, VI18N-03]

coverage:
  - id: D1
    description: "ProviderKeyCard renders a caller-supplied providerLabel on the provider dropdown field; default stays 'Provider'"
    requirement: VI18N-01
    verification:
      - kind: unit
        ref: "ProviderKeyCardTest#a supplied providerLabel replaces the dropdown label text; #omitting providerLabel keeps the English default Provider label"
        status: pass
    human_judgment: false
  - id: D2
    description: "ModelSelectCard renders a caller-supplied modelLabel on the model dropdown field; default stays 'Model'"
    requirement: VI18N-01
    verification:
      - kind: unit
        ref: "ModelSelectCardTest#a supplied modelLabel replaces the dropdown label text; #omitting modelLabel keeps the English default Model label"
        status: pass
    human_judgment: false
  - id: D3
    description: "ClarificationBar renders a caller-supplied dismissLabel on the dismiss button; default stays 'Dismiss'; tap still emits onDismiss"
    requirement: VI18N-02
    verification:
      - kind: unit
        ref: "ClarificationBarTest#a supplied dismissLabel replaces the dismiss text and tapping it still invokes onDismiss; #omitting dismissLabel keeps the English default Dismiss text"
        status: pass
    human_judgment: false
  - id: D4
    description: "ApproachLadderCard renders caller-supplied rung-state (unavailable/capped/needs-network) and toggle (online/offline-only) labels; each defaults to today's English"
    requirement: VI18N-03
    verification:
      - kind: unit
        ref: "ApproachLadderCardTest#supplied rung-state labels replace...; #supplied toggle labels replace...; #omitting the toggle labels keeps the English Online segment"
        status: pass
    human_judgment: false
  - id: D5
    description: "All new params appended last and defaulted; Metalava apiCheck green; api.txt regenerated with exactly the re-signatured lines; detekt zero-baseline green"
    verification:
      - kind: other
        ref: "./gradlew apiCheck (before each apiDump); ./gradlew detekt; git diff v2.4.1 -- api.txt shows exactly 4 removed lines (the 4 composables)"
        status: pass
    human_judgment: false

duration: 6min
completed: 2026-10-05
status: complete
---

# Phase 15 Plan 01: Voice-surface i18n label params (composables) Summary

**Four settings/voice-surface composables (ProviderKeyCard, ModelSelectCard, ClarificationBar, ApproachLadderCard) now take caller-localizable defaulted `String` label params (8 total) appended last, with every English default preserved and apiCheck/detekt/full suite green.**

## Performance

- **Duration:** ~6 min
- **Started:** 2026-10-05T08:51:22Z
- **Completed:** 2026-10-05T08:57:00Z
- **Tasks:** 3 (1 tracer + 2 auto)
- **Files modified:** 9

## Accomplishments

- `ProviderKeyCard.providerLabel` and `ModelSelectCard.modelLabel` thread through the private `ProviderDropdown` / `ModelDropdown` into the `OutlinedTextField` label slot (VI18N-01).
- `ClarificationBar.dismissLabel` renders on the dismiss button; `clarification_bar_dismiss` tag and onClick untouched (VI18N-02).
- `ApproachLadderCard` gains `unavailableLabel`, `cappedLabel`, `needsNetworkLabel`, `onlineLabel`, `offlineOnlyLabel` in the contract order; the first three reach private `RungRow`, the last two feed the `SegmentedOptionSelector` options (VI18N-03).
- 8 new override tests (non-English sentinels, unmerged-tree absence checks) + default pins; all pre-existing English-pinning assertions untouched and green.

## Task Commits

1. **Task 1 (tracer): ProviderKeyCard providerLabel** - `1bf64e4` (feat)
2. **Task 2: ModelSelectCard modelLabel + ClarificationBar dismissLabel** - `7adde40` (feat)
3. **Task 3: ApproachLadderCard five label params** - `31595d4` (feat)

## api.txt delta per commit

- `1bf64e4`: 1 removed / 1 added - `ProviderKeyCard(...)` gains trailing `optional String providerLabel`.
- `7adde40`: 2 removed / 2 added - `ModelSelectCard(...)` gains `optional String modelLabel`; `ClarificationBar(...)` gains `optional String dismissLabel`.
- `31595d4`: 1 removed / 1 added - `ApproachLadderCard(...)` gains `optional String unavailableLabel, cappedLabel, needsNetworkLabel, onlineLabel, offlineOnlyLabel` (in that order).
- Cumulative `git diff v2.4.1 -- api.txt`: exactly 4 removed lines, one each for the four composables (the lane false-positive set).

## Decisions Made

- Appended every param last and defaulted; English default text exists only on the public signatures.
- Residual (not in VI18N-01..04): `SegmentedOptionSelector`'s accessibility state words ("selected" / "not selected", `SegmentedOptionSelector.kt:65`) remain English; candidate follow-up for the Phase 18 docs note. Documented in the `onlineLabel` / `offlineOnlyLabel` KDoc.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] HUB_LANE_OVERRIDE value 3 rejected; hook detects lane 2**
- **Found during:** Task 1 commit
- **Issue:** The plan specified `HUB_LANE_OVERRIDE=3`, but the pre-commit hook (`tools/hooks/pre-commit`, baseline v2.4.1, mode=additive) classified the commit as `LANE 2` and blocked it; it only accepts an override equal to the detected lane.
- **Fix:** Re-ran the identical commit with `HUB_LANE_OVERRIDE=2` (the detected lane, per `tools/README-api-guard.md`). Used the same value for Tasks 2 and 3. No guard bypass beyond what the plan authorized; the override was still applied only after apiCheck was green and the api.txt delta matched expectations.
- **Files modified:** none (commit env only)
- **Verification:** hook output "lane 2 change explicitly declared (HUB_LANE_OVERRIDE=2) - allowed."
- **Committed in:** 1bf64e4, 7adde40, 31595d4

---

**Total deviations:** 1 auto-fixed (Rule 3). **Impact:** none on the code; later plans (15-02, 15-03) and any "lane 3" wording in them should expect lane 2 for re-signatured composables (the hook decides, check its output).

## Issues Encountered

None. Tests were written before the implementation but the red (non-compiling) state was not run separately; the verify run covers the green state. Full suite: 663 tests, 0 failures, 22 skipped (pre-existing skips).

## Next Phase Readiness

- Plans 15-02 / 15-03 (OutcomeSheet model fields, VI18N-04) can proceed; api.txt is fresh and apiCheck green at HEAD.
- Phase 16/17 append to `ApproachLadderCard` after `offlineOnlyLabel` (the last param now).
- No tag, no consumer touched, explorer/ and SegmentedOptionSelector.kt unchanged vs v2.4.1.

## Self-Check: PASSED

- Commits `1bf64e4`, `7adde40`, `31595d4` present in `git log`.
- Modified source/test files and `api.txt` exist and are committed.
