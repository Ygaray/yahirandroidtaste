---
phase: 27-open-from-the-sheet-undo-center-pill-fixes
plan: 04
subsystem: ui
tags: [compose, shape, contrast, wcag, receipt, handled-by-pill, accessibility]
status: complete

requires:
  - phase: 27-01..27-03
    provides: "go-to-item rows and the Undo Center overload, merged into the base of this worktree"
provides:
  - "Internal capped-radius pill shape (HandledByPillShape, handledByPillCornerRadius, handledByPillCapPx, rememberHandledByPillShape)"
  - "Internal contrast decision and ring (wcagContrast, handledByDotRing, Modifier.handledByDotRingBehind)"
  - "Phase-wide hygiene source contract (OpenTargetHygieneSourceContractTest)"
affects: [Gate-1 self-UAT of phase 27, 29 docs]

actuals:
  tokens: 7000
  tasks: 3
  commits: 3
plan_head_before: 6f274ecb146b887f9e5f0e7a51257d1127f71cdb

tech-stack:
  added: []
  patterns:
    - "Custom Shape whose outline is computed from the final size and the draw Density, so no layout-state feedback loop"
    - "Pure decision function plus a draw-behind modifier for a guard that reacts only to a failing pair"

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByPillShape.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByDot.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/HandledByPillShapeTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/HandledByDotContrastTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetHygieneSourceContractTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt

key-decisions:
  - "PD-15..PD-19 as listed below; api.txt unchanged (all new declarations internal)"

requirements-completed: [POL-01, POL-02]

coverage:
  - id: D1
    description: "A wrapped long German pill at font scale 2.0 keeps all four text-box corners inside its painted capped shape; the CircleShape control fails the same check"
    requirement: POL-01
    verification:
      - kind: unit
        ref: "HandledByPillShapeTest#wrappedLongGermanPill_atFontScale2_keepsEveryTextCornerInsideTheShape"
        status: pass
      - kind: unit
        ref: "HandledByPillShapeTest#controlCircleShape_failsTheSameCornerCheck_whenWrapped"
        status: pass
    human_judgment: false
  - id: D2
    description: "A one-line pill is the exact old capsule at font scales 1.0, 1.5 and 2.0"
    requirement: POL-01
    verification:
      - kind: unit
        ref: "HandledByPillShapeTest#oneLinePill_radiusEqualsCircleShape_atFontScales"
        status: pass
    human_judgment: true
    rationale: "Visual parity with the v2.7.1 look at font scale 1.0 is a Gate-1 screenshot comparison"
  - id: D3
    description: "The warn dot gets a ring of at least 3:1 only when its fill fails 3:1 against the pill; hub defaults need none; the ring decision is total over a grey ramp"
    requirement: POL-02
    verification:
      - kind: unit
        ref: "HandledByDotContrastTest#syntheticWhiteOnLightPill_getsARing_withContrastAtLeast3"
        status: pass
      - kind: unit
        ref: "HandledByDotContrastTest#ringIsTotal_overAGreyRamp"
        status: pass
      - kind: unit
        ref: "HandledByDotContrastTest#pillDotAppliesTheRingBeforeItsBackground"
        status: pass
    human_judgment: true
    rationale: "Real dynamic color on the tester and the drawn ring pixels are Gate-1 evidence"
  - id: D4
    description: "Phase-wide hygiene: no key access, no logging, nothing public in the new files, Classic dispatch literals once"
    requirement: POL-01
    verification:
      - kind: unit
        ref: "OpenTargetHygieneSourceContractTest"
        status: pass
    human_judgment: false
---

# Phase 27 Plan 04: Pill shape, dot contrast ring and the phase gate Summary

**Receipt handled-by pill now uses a capped-radius shape (`min(height/2, one-line half-height)`) so a pill wrapped at font scale 2.0 keeps its text inside its outline, the warn dot gets a drawn 3:1 contrast ring only when its fill fails against the pill, and the fresh full gate passes with `api.txt` unchanged.**

## Accomplishments

- **POL-01 (D-06):** `HandledByPillShape` replaces `CircleShape` on the pill background only. Its corner radius is `min(height / 2, cap)` with cap = (body-small line height + 2 x 4dp) / 2 at the draw density, so a one-line pill is the old capsule (tested at font scales 1.0, 1.5, 2.0 within 0.01 px) and a wrapped pill keeps the one-line corner. A native-graphics test wraps the long German pill at font scale 2.0 on 360dp and requires every corner of the text box inside the painted outline; the same check with `CircleShape` fails, so it is not vacuous.
- **POL-02 (D-07):** `handledByDotRing(fill, pill, scheme)` returns `null` when the fill has at least 3:1 against `surfaceContainerHighest`, else `scheme.onSurface` if that reaches 3:1, else black or white by larger contrast. `Modifier.handledByDotRingBehind` draws a circle of radius half-size plus 1.5dp behind the dot (outside its 6dp footprint). `ToneDefaults.warn()` and `handledByDotColor` are byte-unchanged (asserted by a source contract and an equality test).
- **Hygiene (T-27-01/16/17):** `OpenTargetHygieneSourceContractTest` scans eight files for key access and logging, the three new files for public declarations, the open-target tags for documented prefixes, and the Classic dispatch literals.

## Task Commits

1. Task 1 (tracer): `046998d` fix(27-04): cap the Receipt handled-by pill's corner radius ... (committed with `HUB_LANE_OVERRIDE=2`)
2. Task 2: `88c3014` feat(27-04): add a contrast ring to the handled-by dot ... (lane 1, plain commit)
3. Task 3: `f3a0db9` test(27-04): pin the phase-wide key, logging and visibility hygiene (lane 1)

Tracer note: the geometry, control and math tests were green as soon as `HandledByPillShape.kt` existed (they build the shape themselves). The wiring contract `pillBackgroundUsesTheCappedShape` was RED (AssertionError, 7 of 8 green) before the background swap and GREEN after it. The control's failing corner is not printed on a pass; hand computation in dp terms puts the text box's top-left corner about 40 from the CircleShape arc centre against radius 36 (outside), and about 17.9 from the capped arc centre against radius 20 (inside, about 2dp margin, FA-8).

## Gate results

Run in the executor worktree `.../yahirandroidtaste/.claude/worktrees/agent-a94052a8c4a8d389b` on HEAD `f3a0db9`, one Gradle invocation at a time, `--no-daemon`, no `--stop`.

| Command | Result |
|---|---|
| `./gradlew testDebugUnitTest --tests '*HandledByPillShapeTest' ...(5 classes)` | BUILD SUCCESSFUL |
| `./gradlew testDebugUnitTest --tests '*HandledByDotContrastTest' ...(7 classes)` | BUILD SUCCESSFUL |
| `./gradlew testDebugUnitTest --tests '*OpenTargetHygieneSourceContractTest'` | BUILD SUCCESSFUL |
| `./gradlew apiDump` then `git diff --exit-code api.txt` (after tasks 1 and 2) | exit 0, api.txt unchanged |
| `./gradlew detekt` (after tasks 1 and 2) | BUILD SUCCESSFUL, zero baseline |
| FRESH FULL: `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-daemon` | BUILD SUCCESSFUL in 7m 57s, 44 of 44 tasks executed (none UP-TO-DATE or cached), job rc 0 |
| Result XML totals | 167 suites, 1731 tests, 22 skipped, 0 failures, 0 errors |
| `git diff --exit-code api.txt` after the full run | exit 0 (api.txt equals the file after 27-03) |
| `bash tools/verify-api-source-additive.sh v2.7.1` | `API SOURCE-ADDITIVE OK baseline=v2.7.1 removed_classes=0 removed_members=0 enum_constants_intact=73 required_param_changes=0 added_classes=5 added_members=48 signature_changes=0 accepted_exceptions=0` |
| `bash tools/test/run-all.sh` | `PASS=52 FAIL=0` (additive script cases passed=34 failed=0) |
| `git tag --list 'v2.8*'` | prints nothing |
| `git log --stat v2.7.1..HEAD` debris scan (graphs, state.json, config.json, .oc-audit, .gsd-stage-, .gate, uat-evidence) | no hits |

Comparison with the pre-phase baseline (27-VALIDATION.md: 1613 tests, 22 skips, run-all PASS=52 FAIL=0): +118 tests across phase 27 (this plan adds 8 + 13 + 5 = 26), skips unchanged at 22, run-all unchanged at 52 passes.

Registry counts 67, 6, 73: no public composable was added (all new declarations internal, api.txt unchanged by this plan), `ComponentRegistryIntegrityTest` and the other registry tests pass in the fresh full run, and the additive script reports `enum_constants_intact=73`. No test pins a literal 67 or 6, so the numbers are confirmed by "the registry tests pass and nothing public was added", not by a printed count.

`git diff v2.7.1 -- api.txt`: 78 insertions, 11 deletions. Every deletion is a rewritten signature line: the two `OutcomeSheet` composables (new `onOpenTarget` param), `FeedbackEvent.Notify`/`WithUndo` ctor and `copy`, `UndoHistoryEntry.copy`, `BatchRowResultUiModel` ctor and `copy`, `VoiceOutcomeUiState.Success` ctor and `copy` (all appended defaulted params from Phases 25 to 27). Everything else is additions. The additive script confirms no removal, no required-param change.

**Drift guards and the worktree.** `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `GeneratedSymbolDriftGuardTest` (and the CATALOG guard) anchor on the first parent directory named `yahirandroidtaste`. From this worktree the Gradle test CWD is `.../yahirandroidtaste/.claude/worktrees/agent-.../`, so the walk-up resolves to the MAIN checkout `.../Reusable/android/yahirandroidtaste`, not to the worktree. These guards therefore did NOT scan this worktree's new sources (they scanned the main checkout at its current state). They passed, but that pass is not evidence about plans 27-01..27-04 sources. The new files in this plan declare nothing public and add no tags with documented prefixes (hygiene test, which reads by relative path and does cover the worktree), so no drift is expected; the orchestrator must re-run the three guards from the merged main checkout.

## Lane-2 lines

Across the four plans (from the lane-2 commit messages): plan 27-01 re-indents the content call inside the existing Classic scaffold lambda and forwards the new defaulted `onOpenTarget` through the Receipt sheet (single-line rewrites, commits declared with `HUB_LANE_OVERRIDE=2`); plan 27-02 rewrites unreleased Phase 27 lines and one v2.7.1 batch-list line; plan 27-04 (this plan) rewrites exactly one v2.7.1 line, the pill background in `ReceiptHandledByPill` (`.background(scheme.surfaceContainerHighest, CircleShape)` becomes `.background(scheme.surfaceContainerHighest, pillShape)`), committed with `HUB_LANE_OVERRIDE=2` and the reason "swaps the pill background shape; default call path unchanged, no v2.7.1 signature removed". Plan 27-03 and this plan's tasks 2 and 3 are lane 1 (pure additions). The additive script confirms no v2.7.1 signature was removed or changed.

## Runtime decisions PD-1 to PD-19

- PD-1 (27-01): the open row is locked (dimmed, no click action) while `Success.inFlight`.
- PD-2 (27-01): Classic open path is an if/else inside the single existing scaffold lambda.
- PD-3 (27-01): tags `open_target_row` and `open_target_chevron`, avoiding the scanned `outcome_sheet_` prefix.
- PD-4 (27-01): title and detail wrap without a cap; the 24dp chevron is a fixed sibling measured first.
- PD-5 (27-01): blank fallbacks (label to summary, blank detail draws nothing, blank open label to row title).
- PD-6 (27-01): no library debounce; the handler must be idempotent.
- PD-7 (27-01): no API.md, STYLE.md or INTEGRATION.md edit in Phase 27.
- PD-8 (27-01): Receipt threads `onOpenTarget` as a trailing defaulted parameter.
- PD-9 (27-02): batch rows share one rhythm; 48dp minimum only on clickable rows.
- PD-10 (27-02): target-less rows reserve no chevron space (D-03 wins over the mock).
- PD-11 (27-03): a hold never opens; the peek shows from `onLongPress`.
- PD-12 (27-03): trailing slot is Undo then chevron.
- PD-13 (27-03): the new `UndoCenterScreen` overload wraps the old one through a private composition local.
- PD-14 (27-03): row tag `undo-row-<id>` kept; chevron prefix `undo-open-chevron-`.
- PD-15 (27-04): the pill and its tests obtain the shape from one internal composable, `rememberHandledByPillShape()`; a source contract proves the pill uses it.
- PD-16 (27-04): a body-small line height that is Unspecified or in em falls back to a fixed 16sp for the cap (test `unspecifiedOrEmLineHeight_usesTheFallback_andNeverThrows`).
- PD-17 (27-04): ring color is `scheme.onSurface` if it has 3:1, else black or white by larger contrast; `contrastingForeground` is not used (its 0.5 luminance threshold can fall below 3:1 on a mid-tone pill; a grey-ramp test would catch it).
- PD-18 (27-04): ring width 1.5dp as an internal constant in `HandledByDot.kt`, drawn outside the 6dp footprint, not a `Dimens` token.
- PD-19 (27-04): no API.md edit.

## Flagged assumptions FA-1 to FA-10

- FA-1 (27-01): PD-1 locks the target row while `inFlight`; the ui-designer confirms on Gate-1.
- FA-2 (27-01): no debounce on a double tap; the handler must be idempotent.
- FA-3 (27-01, 27-04): normalization forms, grapheme widths, CJK, emoji and right-to-left strings not separately tested; the pill shape depends only on size and density, so any script that wraps is covered by the same geometry, but only German is tested.
- FA-4 (27-02): batch row anatomy with 48dp minimum on clickable rows only; the designer may make it uniform.
- FA-5 (27-03): PD-11 reads D-04's "no-op onLongPress" as "a hold never opens but still previews"; orchestrator to confirm.
- FA-6 (27-03): `detectTapGestures` not calling `onTap` after a long press is pinned by a test and has an in-repo analogue.
- FA-7 (27-04): POL-02 resolved by the manual edge list; a translucent fill is treated as opaque (luminance ignores alpha), documented not tested.
- FA-8 (27-04): the cap leaves about 2dp of corner margin at font scale 2.0; the geometry test passes, the Gate-1 screenshot decides. If Gate-1 shows clipping, adjust the cap by a pixel margin.
- FA-9 (27-04): the near-invisible warn dot originally reported is not reproduced by the hub default tone (it needs no ring); the ring is generic and Gate-1 checks real dynamic color and a forced white-on-light tone.
- FA-10 (27-04): a custom typography with an em or unspecified body-small line height uses the fixed 16sp cap fallback.

## Deviations from Plan

**1. [Rule 3 - Blocking/Environment] Task 3 precondition read against the worktree**
- **Found during:** Task 3
- **Issue:** The precondition requires the toplevel directory to be literally named `yahirandroidtaste`. This run is a parallel executor in a worktree whose toplevel is `.../yahirandroidtaste/.claude/worktrees/agent-a94052a8c4a8d389b`, so the literal check is unmet (the `v2.7.1` tag part is met: `7068729a`). The dispatch prompt for this run explicitly acknowledged this and asked for the guard coverage to be reported in the SUMMARY and for the orchestrator to re-run the guards post-merge.
- **Fix:** Ran the whole gate in the worktree (fresh, `--rerun-tasks`), and documented above that the three walk-up drift guards covered the main checkout rather than this worktree's sources.
- **Verification needed:** orchestrator re-runs `*CatalogDocsDriftGuardTest`, `*DomainVocabularyDriftGuardTest`, `*ComponentRegistryDriftGuardTest`, `*ComponentStatesMatrixTest`, `*ComponentPlaygroundIntegrityTest`, `*LibraryInvariantsGuardTest` from the merged main checkout.

**2. [Rule 1 - Bug in plan text] Test import**
- `androidx.compose.ui.geometry.contains` does not exist in this Compose version (`RoundRect.contains(Offset)` is a member); the import was dropped. No behavior change.

**Total deviations:** 2 (one environment note, one trivial import fix). **Impact:** none on scope.

## Issues Encountered

None beyond the deviations. A `.planning/.jobs/` log directory was created by the long-command job runner; it is untracked/ignored and was not committed.

## Device and behavioral verification

Not performed here (out of the executor lane). The Gate-1 inputs for ROADMAP criteria SC1 to SC5 are in the plan's `<verification>` section and belong to `verify_work_agentic_gate`: SC4 (wrapped pill at font scale 2.0, one-line pill at 1.0) and SC5 (warn dot under real dynamic color, forced white-on-light ring) are the two items this plan feeds. No tag, push, repin or adb action took place.

## Self-Check: PASSED

- Created files exist: HandledByPillShape.kt, HandledByDot.kt, HandledByPillShapeTest.kt, HandledByDotContrastTest.kt, OpenTargetHygieneSourceContractTest.kt (all FOUND).
- Commits present on the branch: 046998d, 88c3014, f3a0db9 (FOUND via `git log`).
- All task `<acceptance_criteria>` greps and XML checks re-run and passing; `git diff --exit-code api.txt` exit 0.
