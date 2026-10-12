---
phase: 20-tone-foundation-atoms
plan: 04
subsystem: progress-and-phase-gate
tags: [compose, stacked-progress-bar, clamp, invariants, api-surface, registry, phase-gate]
requires: ["20-01", "20-02", "20-03"]
provides:
  - StackedProgressBar(segments, trackColor, modifier, height) in component/ (Y-9), registered in Progress / Metrics (PRIMITIVE)
  - "@Immutable data class BarSegment(fraction, color) in component/StackedProgressBar.kt"
  - internal clampSegmentFractions (D-09)
  - tags stacked_progress_bar (stable) and stacked_progress_bar_segment (test-only hook)
  - allowlist token "Stacked"
  - ToneFoundationInvariantsTest (phase-wide source-scan guard)
affects: [21, 22, 24]
tech-stack:
  added: []
  patterns:
    - pure clamp function tested without Compose, then a weighted Row that never receives a non-positive weight
    - clip(CircleShape) before background(trackColor), pinned by a source-contract test
    - source-scan invariants with forbidden identifiers assembled from fragments
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBar.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/StackedProgressBarTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ToneFoundationInvariantsTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ProgressFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ToneAtomsRegistryRenderTest.kt
    - api.txt
key-decisions:
  - "BarSegment lives next to the composable in component/StackedProgressBar.kt (OQ-4)"
  - "Segment tag stacked_progress_bar_segment is a documented test-only hook (OQ-5)"
  - "No pixel test: the bar probe hit the same captureToImage ComposeTimeoutException as wave 1; width and color wiring proven structurally"
requirements-completed: [VBAR-01]
status: complete
plan_head_before: 58d42f2d527d52f2118760298c25ac732e89ba37
duration: ~30min
completed: 2026-10-12
actuals:
  tokens: 6800
  tasks: 3
  commits: 2
---

# Phase 20 Plan 04: StackedProgressBar and phase gate Summary

`StackedProgressBar` and `BarSegment` landed as the final vertical slice (VBAR-01), and the whole phase passed its gate: full suite, detekt, apiCheck, a fresh `api.txt` with exactly one removed line, and `tools/test/run-all.sh`.

PIXEL_PROBE: FAIL (20-01; captureToImage threw ComposeTimeoutException in WindowCapture.forceRedraw under Robolectric NATIVE)
PIXEL_BAR: FAIL (probe test written anyway per the session constraint; it threw the same ComposeTimeoutException at the `captureToImage()` call, so `StackedProgressBarPixelTest.kt` was deleted and never committed) - pixel proof deferred to the Phase 24 owner screencap review

## Tasks

| Task | Name | Commit | Lane |
|------|------|--------|------|
| 1 | StackedProgressBar slice (clamp, bar, tests, Progress entry, "Stacked", api.txt) | 116a08e | 1 |
| 2 | Phase invariants test, bar registry render cases (pixel probe failed, not committed) | a816016 | 1 |
| 3 | Phase gate (no code change; api.txt already fresh, so no commit) | - | - |

Both commits classified LANE 1 (`classify-hub-change.sh --baseline v2.5.1`, mode=additive). No `HUB_LANE_OVERRIDE` was needed anywhere in this plan.

## What was built

- `clampSegmentFractions(raw)`: walks with `remaining = 1f`; NaN counts as 0 (handled explicitly because `coerceAtLeast` leaves NaN), everything else is `coerceIn(0f, remaining)`; the crossing segment is truncated, not rescaled; output size equals input size.
- `StackedProgressBar`: a `Box` with chain caller modifier, `testTag("stacked_progress_bar")`, `fillMaxWidth()`, `height(height)`, `clip(CircleShape)`, then `background(trackColor)`. A `Row(Modifier.matchParentSize())` holds one weighted `Box` per fraction > 0 (tag `stacked_progress_bar_segment`) and a trailing `Spacer` weight for the remainder only when it is positive, so `Modifier.weight` never sees a non-positive value. `trackColor` is required. No color literals.
- Gallery: Progress entry `StackedProgressBar` (PRIMITIVE, four canonical labels; Pressed / Selected, Disabled, Focused are N/A with comments) on `colorScheme.outline`; `StackedProgressBarVariants` shows two segments, a sum above 1, a negative fraction, an empty list and a 12dp track.
- `ToneFoundationInvariantsTest` (plain JUnit, five tests): no resource-id tag flag and no Hilt application/entry-point annotation anywhere under `src/main` (comments stripped, vacuous-pass guard on file count), import prefix litmus for the six phase files, no `Color(0x` literal in them, column-0 `@Composable` / `fun Name(` shape for the four new composables, and the four stable tag literals. The forbidden identifiers are assembled from fragments, so the test file never contains them whole.

## Verification (gate evidence)

- Task 1: `apiDump` + `StackedProgressBarTest` (20 tests: 8 pure clamp, 6 width / hostile-input cases at 300dp, height 6dp / 12dp, tag default / override, 2 source contracts) + `DimensTest` + `explorer.*` green.
- Task 2: `ToneFoundationInvariantsTest` (5), `ToneAtomsRegistryRenderTest`, `StackedProgressBarTest` green.
- Task 3, one invocation `./gradlew apiDump testDebugUnitTest detekt apiCheck`: BUILD SUCCESSFUL. Unfiltered suite: 867 tests, 0 failures, 0 errors, 22 skipped (the skips are pre-existing; nothing was ignored in this phase). detekt: 0 findings.
- `git diff --exit-code -- api.txt` after the dump: exit 0 (fresh).
- `bash tools/test/run-all.sh`: exit 0 (classify 12/12, precommit hook 6/6, additive diff 5/5, binary ABI 52/52).
- `git diff --numstat v2.5.1 -- api.txt`: `83  1  api.txt`. The one removed line is the old signature:
  `method @KotlinOnly @androidx.compose.runtime.Composable public static void DynamicActionButton(String label, ...ActionButtonRole role, Function0<Unit> onClick, optional boolean enabled, optional Modifier modifier);` (replaced by the widened signature with `emphasis` and `icon`).
- `git diff --stat v2.5.1 -- DynamicActionButtonTest.kt AppChipTest.kt PresetChipTest.kt`: empty (byte-identical).
- `git diff --exit-code v2.5.1 -- config/detekt-baseline.xml config/detekt/detekt.yml config/detekt-compose.yml`: exit 0.
- `ActionButtonDefaults.ActionButtonRole` constants in `api.txt`: Destructive, Neutral, Save (unchanged).
- `bash tools/classify-hub-change.sh --baseline v2.5.1` (informational): `LANE 1 (mode=additive, baseline=v2.5.1)`.
- No push, no tag, nothing staged under `.planning/graphs`, `.planning/uat-pending`, `.planning/WAITING.json` or the v2.5 `.gate` directory. No device touched.

## Phase 20 roll-up

| Plan | Outcome | Commits (lane) |
|------|---------|----------------|
| 20-01 | `ToneColors` / `ToneDefaults` / `ContentTone`, `ToneCard`, Dimens groups, `ContrastTestSupport` | 0ad6374, 0ffa1c8, c9297ad (all lane 1) |
| 20-02 | `ActionButtonEmphasis`, widened `DynamicActionButton` (emphasis, icon), legacy path pinned | 0c716ba, e5470d5, 577a52e (all lane 1) |
| 20-03 | `RoundIconButton`, `PillChip`, long German label coverage | b376f45, 2a4d58e, ef88586 (all lane 1) |
| 20-04 | `StackedProgressBar`, `BarSegment`, invariants test, phase gate | 116a08e, a816016 (both lane 1) |

Phase surface: four registry entries (ToneCard, RoundIconButton, StackedProgressBar, PillChip), allowlist tokens Tone, Round, Pill, Stacked, tags tone_card, round_icon_button, pill_chip, stacked_progress_bar. Requirements covered across the four plans: VTONE-01, VBTN-01, VBTN-02, VBAR-01, VCARD-01, VCHIP-01.

Observed sizes (from 20-02 / 20-03, for the owner screencap review): TextSmall draws 40dp tall (the M3 internal minimum governs; its 48dp touch bounds and layout reserve hold) and Filled draws 40dp tall. Semantic-bounds observation from 20-03: tagged nodes report the DRAWN size (RoundIconButton 40 / 36 / 64dp, PillChip 32dp), while the 48dp reserve shows in layout pitch and `touchBoundsInRoot`. The StackedProgressBar track node reports its drawn 6dp (12dp when set) height and the full 300dp width.

## Deviations from Plan

**1. [Rule 1 - Compile fixes in my own new files] `matchParentSize` and `assertExists` imports.** `matchParentSize` is a `BoxScope` member and `assertExists` is a `SemanticsNodeInteraction` member, so neither is imported. Fixed before the first green run; no behavior change.

**2. [Plan note] One `setContent` per test.** Each width case is its own test (same rule as waves 1-3). `StackedProgressBarTest` uses NATIVE graphics and `w360dp-h800dp` like the other atom tests.

**3. [Plan note] RED step not run as a separate gradle invocation.** The clamp tests were written first against a not-yet-existing function (a guaranteed compile failure), and I went straight to implementation to avoid a wasted build; all 20 tests passed on the first run that compiled.

**4. [Planned outcome] Pixel test.** The plan made it conditional on `ToneCardPixelTest.kt`, which does not exist (20-01 probe failed). The session constraint asked for a probe anyway; it failed with the same `ComposeTimeoutException`, so the file was deleted, per the plan, and `PIXEL_BAR: FAIL` is recorded above.

No production defects were exposed by the tests. No auth gates.

## Notes for the orchestrator

- **Worktree blind spot in the existing drift guards.** `DomainVocabularyDriftGuardTest` (and, by the same copied idiom, `ComponentRegistryDriftGuardTest`) resolve the source root by walking up to a directory named `yahirandroidtaste` that holds `build.gradle.kts`. In a worktree whose root is named `agent-*`, that walk lands on the MAIN checkout, so those two guards scanned main's sources, not this worktree's. They passed, but not against the new files. `ToneFoundationInvariantsTest` deliberately tries the Gradle-default relative path first, so it did scan this worktree. After the wave merge, a full `./gradlew testDebugUnitTest` in the main checkout is the real confirmation for those two guards (the registry integrity test itself is in-memory and was valid here).
- The `.git` plan-head ledger file for 20-04 could not be written (the sandbox refused the compound command), so `plan_head_before` is the worktree base `58d42f2` and `commits: 2` is `git rev-list --count 58d42f2..HEAD` measured before this SUMMARY commit.

## Deferred to Phase 24

- Documentation counts in `README.md`, `API.md` and the `ComponentRegistry` KDoc are stale after four new registrations (no test enforces them); Phase 24 (DOC-03) owns them. Not edited here.
- `API.md` stable-tag section and the Y-14 docs (DOC-03).
- Owner screencap review of the gallery entries for all four atoms, including the pixel-level color checks that `captureToImage` cannot deliver in Robolectric here, and the TextSmall drawn height. TESTER only (`adb -s R5CT10XNKQN`); no device work was planned or done in this phase.
- `20-VALIDATION.md` untouched (the Nyquist finalizer owns it).

## Threat model

T-20-13 mitigated (clamp handles NaN, infinity, negatives; zero segments never reach `weight`; unit and Robolectric cases). T-20-14 mitigated (`ToneFoundationInvariantsTest` scans all of `src/main` for the resource-id flag; segment tag documented test-only). T-20-15 mitigated (import litmus, Hilt host absence, no color literals asserted). T-20-16 mitigated (fresh `api.txt`, `apiCheck`, exactly one removed line audited). T-20-17 accepted.

## Self-Check: PASSED

- Files present: StackedProgressBar.kt, StackedProgressBarTest.kt, ToneFoundationInvariantsTest.kt, edited ProgressFamilyScreen.kt, DomainVocabularyDriftGuardTest.kt, ToneAtomsRegistryRenderTest.kt, api.txt; StackedProgressBarPixelTest.kt absent (matches PIXEL_BAR: FAIL).
- Commits 116a08e and a816016 present on the worktree branch.
