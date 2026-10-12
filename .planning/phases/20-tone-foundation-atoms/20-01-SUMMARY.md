---
phase: 20-tone-foundation-atoms
plan: 01
subsystem: theme-tokens-and-cards
tags: [compose, tone, theme, toncard, dimens, registry, api-surface]
requires: []
provides:
  - ToneColors / ToneDefaults / ContentTone in theme/ (Y-1)
  - ToneCard atom in component/ (Y-10), registered in Cards family (PRIMITIVE)
  - Dimens.ActionButton / RoundIconButton / PillChip / StackedProgressBar token groups
  - ContrastTestSupport (WCAG contrast helper) and ToneAtomsRegistryRenderTest (appendable per atom)
affects: [20-02, 20-03, 20-04, 21, 22]
tech-stack:
  added: []
  patterns:
    - structural (non-pixel) color proof via LocalContentColor + contrast math
    - one setContent per Robolectric test (rule limit); loop over schemes inside one composition
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/theme/ToneTokens.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ToneCard.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/theme/ToneTokensTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/theme/ContrastTestSupport.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ToneAtomsRegistryRenderTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/theme/Dimens.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/theme/DimensTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneCardTest.kt
    - api.txt
key-decisions:
  - "ToneColors is a plain @Immutable class with hand-written equals/hashCode/toString (no copy/componentN leak)"
  - "Dimens tokens appended as nested objects; same-valued tokens of other concepts stay distinct"
  - "Pixel proof deferred: captureToImage times out in this harness (PIXEL_PROBE: FAIL)"
requirements-completed: [VTONE-01, VCARD-01]
status: complete
plan_head_before: 83f5559430a35ded3299f51efb8906c4a0bea9bd
duration: ~35min
completed: 2026-10-12
actuals:
  tokens: 9700
  tasks: 3
  commits: 3
---

# Phase 20 Plan 01: Tone foundation and ToneCard Summary

Tone tokens (`ToneColors`, `ToneDefaults.error()/warn()`, `ContentTone`) and the `ToneCard` atom landed as a proven vertical path through theme, component, Cards registry, drift-guard allowlist and `api.txt`, plus the phase's append-only Dimens token groups.

PIXEL_PROBE: FAIL (captureToImage threw ComposeTimeoutException "Condition still not satisfied after 2000 ms" in WindowCapture.forceRedraw under Robolectric NATIVE) - pixel proof deferred to Phase 24 owner screencap review

## Tasks

| Task | Name | Commit | Lane |
|------|------|--------|------|
| 1 | Tracer: tone tokens + ToneCard through registry, drift guards, api.txt | 0ad6374 | 1 |
| 2 | Dimens token foundation (append-only) + DimensTest locks | 0ffa1c8 | 1 |
| 3 | Structural proof of VTONE-01 / VCARD-01; pixel probe | c9297ad | 1 |

All three commits classified LANE 1 (`classify-hub-change.sh --baseline v2.5.1`, mode=additive); no `HUB_LANE_OVERRIDE` needed.

## What was built

- `theme/ToneTokens.kt`: `ToneColors` plain class (value equality), `ToneDefaults` (`error()` = errorContainer/error, `warn()` = tertiaryContainer/onTertiaryContainer, read from the active `MaterialTheme.colorScheme`), `ContentTone { Default, Muted, Warn, Error }`. KDoc tells consumers to pass their own tokens when dynamic color makes tertiary non-amber.
- `component/ToneCard.kt`: Surface on `tone.container` with `contentColor = tone.content`, 12dp corners (`Dimens.CornerRadius.Medium`), 12dp vertical x 16dp horizontal padding, 4dp child spacing, tag `tone_card` applied after the caller modifier (caller tag wins). Signature `(tone, modifier, content: ColumnScope.() -> Unit)`.
- Cards registry entry `ToneCard` (PRIMITIVE, four canonical labels, Default cell + Variants showing error and warn tones); `"Tone"` added to `PRIMITIVE_NOUN_ALLOWLIST`.
- `Dimens.ActionButton`, `RoundIconButton`, `PillChip`, `StackedProgressBar` groups with values per the plan; `DimensTest` gained four methods, original three untouched.

## Verification

- Task 1: `apiDump` + `ToneCardTest` + `explorer.*` green (registry, vocabulary, states-matrix, generated-symbol guards).
- Task 2: `apiDump` + `DimensTest` + `explorer.*` green.
- Task 3: `ToneTokensTest` (8), `ToneCardTest` (7), `ToneAtomsRegistryRenderTest` (1), `detekt` (zero findings, no baseline change), `apiCheck` all green in one invocation.
- `git diff --numstat v2.5.1 -- api.txt` reports 0 removed lines (23 added after Task 1, 53 after Task 2); `DimensTest` removed column 0; `DynamicActionButtonTest`/`AppChipTest`/`PresetChipTest` byte-identical to v2.5.1.

## Deviations from Plan

**1. [Rule 1 - Bug in test design] One `setContent` per test.** The plan's tests loop over several schemes/tones; Compose's test rule forbids calling `setContent` twice in one test. `ToneTokensTest`, `ToneCardTest` and `ToneAtomsRegistryRenderTest` render all combinations inside a single composition instead. No production change. The registry render test therefore counts `tone_card` nodes across one combined composition (>= 6) rather than per-render.

**2. Pixel probe failed (planned outcome, not a defect).** `captureToImage` itself timed out in the harness before any pixel was read, so per the plan `ToneCardPixelTest.kt` was deleted and not committed. Color wiring is proven structurally. Plan 20-04 should therefore NOT add pixel tests for the bar.

**3. Plan-level note.** The plan-commit ledger file (`gsd-plan-head-before-20-01`) was created after the first commit (value is the pre-plan base `83f5559`), so `commits: 3` is the measured `rev-list` count.

No production-file defects were exposed by the tests. No auth gates. Nothing deferred.

## Threat model

T-20-01 (tag exposure): library never sets `testTagsAsResourceId`; source-scan invariant test is planned in 20-04. T-20-02: `ToneColors` has no `copy`/`componentN` (asserted by test), api.txt diff is pure addition. T-20-03/04 accepted as planned.

## Self-Check: PASSED

- Files: ToneTokens.kt, ToneCard.kt, ToneTokensTest.kt, ContrastTestSupport.kt, ToneAtomsRegistryRenderTest.kt present; ToneCardPixelTest.kt absent (matches PIXEL_PROBE: FAIL).
- Commits 0ad6374, 0ffa1c8, c9297ad present on the worktree branch.
