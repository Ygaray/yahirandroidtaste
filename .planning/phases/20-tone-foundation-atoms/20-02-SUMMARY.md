---
phase: 20-tone-foundation-atoms
plan: 02
subsystem: buttons
tags: [compose, dynamic-action-button, emphasis, icon, api-surface, source-contract]
requires: ["20-01"]
provides:
  - ActionButtonEmphasis enum (Filled, Tonal, Text, TextSmall) in component/DynamicActionButton.kt
  - DynamicActionButton(label, role, onClick, enabled, modifier, emphasis, icon) widened signature
  - internal legacyEmphasisFor, EmphasisColors, emphasisColors, TONAL_CONTAINER_ALPHA
  - gallery emphasis x role rows and icon rows in the DynamicActionButton Variants
affects: [20-03, 20-04, 22, 23]
tech-stack:
  added: []
  patterns:
    - early guarded branch above an untouched legacy block, pinned by a whitespace-stripped source-contract test
    - NATIVE graphics for any width/delta test (legacy shadow text is about 1px per character)
key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButtonEmphasisTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButtonLegacyPathSourceContractTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneAtomsLongLabelTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ButtonsFabFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ToneAtomsRegistryRenderTest.kt
    - api.txt
key-decisions:
  - "Early return branch above the legacy when(role) block (no A11 contingency needed: the compose compiler accepted the early return)"
  - "emphasis = null with icon != null keeps the legacy role mapping plus the icon (OQ-2)"
  - "Tonal passes elevation = null; M3 minimum sizes are not fought"
requirements-completed: [VBTN-01]
status: complete
plan_head_before: 8b2583e58f97f0776baf39820c4aa8cfb9d1bbfa
duration: ~30min
completed: 2026-10-12
actuals:
  tokens: 8000
  tasks: 3
  commits: 3
---

# Phase 20 Plan 02: DynamicActionButton emphasis and icon Summary

`DynamicActionButton` now takes an optional `emphasis: ActionButtonEmphasis?` (widget) and `icon: ImageVector?` (decorative leading icon) while `role` keeps meaning hue only for the new path; a call with neither runs the byte-identical legacy `when (role)` block.

## Tasks

| Task | Name | Commit | Lane |
|------|------|--------|------|
| 1 | ActionButtonEmphasis + emphasis/icon branch above the untouched legacy block; api.txt regenerated | 0c716ba | 1 |
| 2 | Emphasis behavior, palette and legacy-path source-contract tests | e5470d5 | 1 |
| 3 | Gallery emphasis rows, registry render tests, long German label at 360dp | 577a52e | 1 |

All three commits classified LANE 1 (`classify-hub-change.sh --baseline v2.5.1`, mode=additive; the guard tolerates the trailing comma on the `modifier` line), so no `HUB_LANE_OVERRIDE` was needed.

## What was built

- `ActionButtonEmphasis { Filled, Tonal, Text, TextSmall }`; `emphasisColors` is pure (Destructive -> error hue with onError on Filled, others -> primary; Tonal container is the hue at alpha 0.14; Text and TextSmall are transparent with hue content).
- `EmphasisActionButton` (private): Filled and Tonal are pill (`CircleShape`) M3 `Button`s with 20dp x 8dp padding and `heightIn(min = 40dp)`; Text is a `TextButton` 12dp x 8dp; TextSmall is a `TextButton` 8dp x 4dp with `bodySmall`. All use `minimumInteractiveComponentSize()`. Every dimension reads `Dimens.ActionButton.*`. Icon is 18dp with an 8dp gap, shared via one private `RowScope.EmphasisContent`.
- Gallery: Variants gained a row per emphasis (Neutral, Save, Destructive) plus a Filled and a Text button with `Icons.Filled.Check`; the registry entry's four state cells are unchanged (removed column 0 for `ButtonsFabFamilyScreen.kt`).
- API surface: `git diff --numstat v2.5.1 -- api.txt` = 61 added, 1 removed (the old `DynamicActionButton(` signature), as required. `DynamicActionButton.kt` removed column is 1 (the `modifier` line gaining a comma).

## Observed sizes (for the Phase 24 screencap review)

Measured at density 1.0, NATIVE graphics, tagged node size: Filled 40dp, Tonal 40dp, Text 40dp, TextSmall 40dp tall. TextSmall's content (bodySmall + 4dp vertical padding) is shorter than the M3 40dp internal minimum, so the M3 minimum governs the drawn height (Pitfall 4, not fought). The layout reserve and touch bounds stay at least 48dp (verified: touch bounds >= 48dp both dimensions, stacked TextSmall tops >= 48dp apart). A tighter drawn TextSmall would need a custom (non-M3) button; flagged for the owner screencap review.

## Verification

- Task 1: `apiDump` + `DynamicActionButtonTest` + `DimensTest` + `explorer.*` green.
- Task 2: `DynamicActionButtonEmphasisTest` (13 tests), `DynamicActionButtonLegacyPathSourceContractTest` (3), `DynamicActionButtonTest` green.
- Task 3: `ToneAtomsLongLabelTest` (9: four emphases with and without icon at w360dp, plus a short-label control), `explorer.*` (including 3 tests in `ToneAtomsRegistryRenderTest`), `DynamicActionButtonTest`, `detekt` (zero findings, no baseline change), `apiCheck` green in one invocation.
- `DynamicActionButtonTest`, `AppChipTest`, `PresetChipTest` byte-identical to v2.5.1.

## Deviations from Plan

**1. [Rule 1 - Test design] NATIVE graphics + tall window for the emphasis test.** The first run of `DynamicActionButtonEmphasisTest` failed on the icon delta (12 vs 26dp), padding delta (2 vs 16dp) and a click on an off-screen button. Cause: under legacy graphics Robolectric measures text at about 1px per character, so every label sat under the Material 58dp minimum width and hid the deltas; the 12-button column also overflowed the default 470dp window. Fixed in the test only (`@GraphicsMode(NATIVE)`, `qualifiers = "w360dp-h1600dp"`); no production change. No production-file defects were exposed.

**2. [Plan note] One `setContent` per test.** As in plan 20-01, multi-variant measurements render all variants in one tagged composition.

**3. No pixel tests** (per the wave-1 finding that `captureToImage` times out in this harness). Color wiring is proven structurally by the pure `emphasisColors` tests for both hub schemes; pixel-level proof is deferred to the Phase 24 owner screencap review. Nothing ignored or failing was committed.

No auth gates. Nothing deferred beyond the TextSmall drawn-height note above.

## Threat model

T-20-05 mitigated (guard-before-legacy and exact legacy text pinned by the source contract; existing tests unedited). T-20-06 mitigated (exactly one removed api.txt line; `ActionButtonRole` untouched; no overloads or shims). T-20-07 mitigated (`ToneAtomsLongLabelTest`). T-20-08 accepted (icon decorative, `contentDescription = null`).

## Self-Check: PASSED

- Files present: `DynamicActionButtonEmphasisTest.kt`, `DynamicActionButtonLegacyPathSourceContractTest.kt`, `ToneAtomsLongLabelTest.kt`, edited `DynamicActionButton.kt`, `ButtonsFabFamilyScreen.kt`, `ToneAtomsRegistryRenderTest.kt`, `api.txt`.
- Commits 0c716ba, e5470d5, 577a52e present on the worktree branch (`rev-list --count 8b2583e..HEAD` = 3 before this SUMMARY commit).
