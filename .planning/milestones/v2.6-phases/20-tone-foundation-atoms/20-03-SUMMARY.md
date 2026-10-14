---
phase: 20-tone-foundation-atoms
plan: 03
subsystem: buttons-and-chips
tags: [compose, round-icon-button, pill-chip, touch-target, api-surface, registry]
requires: ["20-01", "20-02"]
provides:
  - RoundIconButton(icon, contentDescription, onClick, modifier, enabled, size) in component/ (Y-8), registered in Buttons/FAB (PRIMITIVE)
  - PillChip(label, onClick, modifier, leadingIcon, trailingIcon, enabled, contentDescription) in component/ (Y-12), registered in Chips (PRIMITIVE)
  - tags round_icon_button and pill_chip (caller testTag overrides)
  - allowlist tokens "Round" and "Pill"
affects: [20-04, 22]
tech-stack:
  added: []
  patterns:
    - one clickable root carrying caller modifier, tag, click and description (PillChip)
    - tag placed after sizing modifiers so the tagged node's bounds are the drawn size
    - NATIVE graphics for width-delta and ellipsis tests
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/RoundIconButton.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/PillChip.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/RoundIconButtonTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/PillChipTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ButtonsFabFamilyScreen.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ToneAtomsRegistryRenderTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneAtomsLongLabelTest.kt
    - api.txt
key-decisions:
  - "Semantic node bounds report the DRAWN size (40/36/64dp, 32dp), not the 48dp wrapper, because the tag sits after the sizing modifiers"
  - "Both atoms are hand-built (no Material outlined icon button) to draw outlineVariant, not outline"
  - "PillChip height is a minimum (grows at large font scales); 12dp label-only padding, 8dp on an icon side, 4dp icon gap (OQ-5)"
requirements-completed: [VBTN-02, VCHIP-01]
status: complete
plan_head_before: 0360d95d1c0e5e40f50ac6fb6d6b46957aace495
duration: ~30min
completed: 2026-10-12
actuals:
  tokens: 12000
  tasks: 3
  commits: 3
---

# Phase 20 Plan 03: RoundIconButton and PillChip Summary

Two interactive atoms landed as full vertical slices: `RoundIconButton` (40dp outlineVariant-bordered transparent circle, 20dp onSurface icon) and `PillChip` (32dp-minimum pill, single-line ellipsizing bodySmall label, optional 18dp icons), each with an accessible 48dp touch region, a stable tag, a registry entry, an allowlisted head token and a regenerated `api.txt`.

## Tasks

| Task | Name | Commit | Lane |
|------|------|--------|------|
| 1 | RoundIconButton slice (component, tests, Buttons/FAB entry, "Round", api.txt) | b376f45 | 1 |
| 2 | PillChip slice (component, tests, Chips entry, "Pill", api.txt) | 2a4d58e | 1 |
| 3 | Registry render coverage and long German label at 360dp | ef88586 | 1 |

All three commits classified LANE 1 (`classify-hub-change.sh --baseline v2.5.1`, mode=additive); no `HUB_LANE_OVERRIDE` was needed.

## What was built

- `RoundIconButton`: one `Box`; chain is caller modifier, `minimumInteractiveComponentSize()`, `size(size)`, tag, 38% disabled alpha, `CircleShape` clip, 1dp `outlineVariant` border, `clickable(role = Button)`, content-description semantics. No background. `contentDescription` is required.
- `PillChip`: one root `Row` with the same chain shape (`heightIn(min = 32dp)` instead of `size`), then per-side padding (12dp label-only, 8dp icon side). Label has `weight(1f, fill = false)`, `maxLines = 1`, `Ellipsis`, so a trailing icon keeps its room. Spacers appear only next to an icon. `AppChip` and `PresetChip` untouched (8dp shape).
- Gallery: `RoundIconButtonVariants` (Remove/Add/Mic at 40dp, then 36dp, 56dp, disabled) and `PillChipVariants` (label only, leading, trailing, both, 200dp ellipsis, disabled). Four canonical state cells each; Pressed/Selected and Focused are documented N/A.

## Semantic bounds observation (requested by the plan)

Semantic node bounds report the DRAWN size, not the 48dp wrapper: tagged nodes measured 40, 36 and 64dp (RoundIconButton) and 32dp tall (PillChip). The fallback branch in the plan was not needed. The 48dp reserve shows in layout instead: neighbouring `RoundIconButton` left edges are 48dp apart for 40dp and 36dp and 64dp apart for the 64dp size, and stacked `PillChip` tops are 48dp apart. `touchBoundsInRoot` is at least 48dp in both dimensions in all measured cases.

## Verification

- Task 1: `apiDump` + `RoundIconButtonTest` (8) + `DimensTest` + `explorer.*` green.
- Task 2: `apiDump` + `PillChipTest` (11) + `AppChipTest` (12) + `PresetChipTest` (9) + `explorer.*` green.
- Task 3 (one invocation): `ToneAtomsLongLabelTest` (11), `explorer.*` (`ToneAtomsRegistryRenderTest` 8), `AppChipTest`, `PresetChipTest`, `DynamicActionButtonTest` (5), `detekt` (zero findings, no baseline change), `apiCheck` all green.
- `git diff --numstat v2.5.1 -- api.txt` = 69 added, 1 removed (only the earlier `DynamicActionButton(` line). `RoundIconButtonKt` and `PillChipKt` each appear in `api.txt`.
- `AppChipTest`, `PresetChipTest`, `DynamicActionButtonTest` byte-identical to v2.5.1.

## Deviations from Plan

**1. [Rule 1 - Test design] Combined compositions.** Compose's rule allows one `setContent` per test, so the size / touch / pitch checks render default, 36dp and 64dp pairs in a single composition (`RoundIconButtonTest`), and the icon-delta check renders four chips in one `Column` (`PillChipTest`).

**2. [Plan note] NATIVE graphics for `PillChipTest`.** The width-delta assertions use real text metrics (`@GraphicsMode(NATIVE)`, `w360dp-h800dp`), consistent with the wave-2 finding. No production change.

**3. [Additive] Extra registry-render tests.** Besides one main test per atom, `ToneAtomsRegistryRenderTest` gained a Variants-only test per atom and a Disabled-cell not-enabled test for `RoundIconButton`, to meet the "at least 4 / 5 tagged nodes in Variants" criteria independent of the cell count.

No pixel tests were added (the wave-1 `captureToImage` timeout still applies); color wiring is proven by the source-contract tests (`colorScheme.outlineVariant`, `colorScheme.onSurface`, `CircleShape`, tokens). Pixel review is deferred to the Phase 24 owner screencap review. No production defects were exposed by the tests. No auth gates.

## Threat model

T-20-09: no resource-id exposure flag set in either atom. T-20-10: `minimumInteractiveComponentSize` on both; touch bounds and 48dp pitch asserted. T-20-11: `ToneAtomsLongLabelTest` proves one line, ellipsis, in-window and trailing-icon room at 360dp with an ~85-char German label. T-20-12 accepted.

## Self-Check: PASSED

- Files present: RoundIconButton.kt, PillChip.kt, RoundIconButtonTest.kt, PillChipTest.kt, edited registry screens, allowlist test, api.txt.
- Commits b376f45, 2a4d58e, ef88586 present on the worktree branch (`rev-list --count 0360d95..HEAD` = 3 before this SUMMARY commit).
