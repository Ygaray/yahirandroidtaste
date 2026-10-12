---
phase: 21-receipt-row-pill-selector-sheet-shape
plan: 04
subsystem: ui
tags: [compose, receipt-row, motion, accordion, phase-gate, api-surface]

requires:
  - phase: 21-receipt-row-pill-selector-sheet-shape
    provides: "21-03 ReceiptRow with the internal motion seams and the Cards registry entry; 21-01 and 21-02 shape and Pill changes"
provides:
  - "ReceiptRowMotionTest: controlled-clock proof of the 200ms chevron rotation, 200ms dim fade and expandedContent toggling"
  - "ReceiptRowLongLabelTest: 360dp German value-wins overflow proof and the 80-digit edge"
  - "ReceiptRowRegistryRenderTest: registry render in both themes and the single-open accordion"
  - "Gallery single-open accordion in ReceiptRowVariants (state held in the demo)"
  - "Phase 21 gate evidence and Gate-1 handoff"
affects: [22-receipt-outcome-sheet, 24-docs]

actuals:
  tokens: 6500
  tasks: 3
  commits: 2
plan_head_before: 36719b980702903b750a669643fa8fe47d7bb7da

key-files:
  created:
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowMotionTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowLongLabelTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptRowRegistryRenderTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowSourceContractTest.kt

key-decisions:
  - "No ReceiptRow.kt defect was found by the new tests, so the component file is untouched in this plan"
  - "didOverflowWidth is not a usable instrument in this harness (it is true for any text narrower than its slot); the value-whole check uses lineCount, isLineEllipsized and getLineRight against the node width instead"
  - "The registry render test uses a 3000dp-tall window (w360dp-h3000dp) so every accordion row is on screen for performClick"

requirements-completed: [VROW-01]

coverage:
  - id: D1
    description: "The chevron rotates 0 to 180 degrees over 200ms and back, and the dim alpha fades 1 to 0.38 over 200ms, through the seams the real row calls"
    requirement: VROW-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowMotionTest.kt#tracer - the chevron rotates from 0 toward 180 over 200ms"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowMotionTest.kt#the dim alpha fades from 1 to 38 percent over 200ms"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowSourceContractTest.kt#every animation uses the shared 200ms tween"
        status: pass
    human_judgment: false
  - id: D2
    description: "expandedContent appears and disappears when expanded toggles, below the summary and above the divider"
    requirement: VROW-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowMotionTest.kt#toggling expanded through the real row shows and hides the expanded content"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowSourceContractTest.kt#the expanded content sits below the summary and above the divider"
        status: pass
    human_judgment: false
  - id: D3
    description: "At 360dp with a long German title the value is whole on one line, the title is one ellipsized line ending before the value column, and the chevron or trailing action stays in the window; an 80-digit value stays bounded"
    requirement: VROW-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowLongLabelTest.kt#tracer - a long German title ellipsizes and the value stays whole before the chevron"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRowLongLabelTest.kt#an extreme 80 digit value neither crashes nor leaves the window"
        status: pass
    human_judgment: false
  - id: D4
    description: "The gallery holds a three-row single-open accordion with state in the demo; the Cards entry renders cells and Variants under both themes"
    requirement: VROW-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptRowRegistryRenderTest.kt#the accordion keeps one row open at a time with the state held in the demo"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptRowRegistryRenderTest.kt#ReceiptRow is a Cards pattern with canonical states and renders in both themes"
        status: pass
    human_judgment: false
  - id: D5
    description: "How the 200ms motion, the dimmed look and the 24dp-corner sheet and Pill selector actually look on the tester"
    verification: []
    human_judgment: true
    rationale: "No pixel capture exists in this harness; the rendered look is the Gate-1 tester's check (see the handoff section)."

duration: 35min
completed: 2026-10-12
status: complete
---

# Phase 21 Plan 04: Motion, 360dp overflow, accordion and phase gate Summary

**The 200ms chevron and dim motion, the value-wins German overflow at 360dp and a demo-held single-open accordion are proven; the full suite (968 tests), detekt at the zero baseline, apiCheck and the repo guard fixtures are green, and the user-visible criteria are handed to the Gate-1 tester.**

## Accomplishments

- `ReceiptRowMotionTest` (6 cases) pauses the main clock and reads the internal `rememberReceiptRowChevronRotation` and `rememberReceiptRowAlpha` seams: value 0 (or 180, or 1.0, or 0.38) before, strictly between at 100ms, exactly the target after 200ms plus a frame, in both directions, plus no animation on first composition. A real-row case toggles `expanded` and sees `receipt_row_expanded_content` appear (with "detail") and disappear.
- Four source contracts appended to `ReceiptRowSourceContractTest`: the three constants, seam use inside the row, at least six `tween(RECEIPT_ROW_MOTION_MS)` plus the four expand/collapse specs, and the order summary < AnimatedVisibility < divider.
- `ReceiptRowLongLabelTest` (6 cases, w360dp, NATIVE graphics): long German title ellipsizes on one line and ends before the value column while the value stays whole and the 24dp chevron stays in the window; long German subtitle; trailing action stays inside the window; short-text control not ellipsized; fontScale 1.5; an 80-digit value does not crash and stays within the summary, which stays at least 48dp tall.
- Gallery: `ReceiptRowVariants()` gained "ReceiptRow — single-open accordion (state held in the demo)" with three rows ("Accordion row N", detail "Accordion detail N"); the only state is `openIndex` in the demo. `CardsFamilyScreen.kt` vs v2.5.1 is `184 0` (pure additions across the phase).
- `ReceiptRowRegistryRenderTest` (5 cases): Cards family, PATTERN tier, four canonical labels, Focused null, both themes render; Default cell has one chevron; Pressed / Selected cell has one expanded content; Disabled cell title carries LineThrough; the accordion opens row 2 (one detail, positioned between row 2 and row 3), switches to row 3 (row 2 closes), and closes on a second tap of row 3.

## Task Commits

| Task | Commit | Lane | Message |
| ---- | ------ | ---- | ------- |
| 1 | 80a55f6 | LANE 1 (additive, baseline v2.5.1) | test(21-04): ReceiptRow motion seams, expand toggling and motion contract |
| 2 | 2a7d8e0 | LANE 1 (additive, baseline v2.5.1) | feat(21-04): ReceiptRow accordion demo, 360dp overflow proof and registry render test |
| 3 | (this SUMMARY commit) | docs only | docs(21-04): complete motion, overflow, accordion and phase gate plan |

No commit needed `HUB_LANE_OVERRIDE=2`. `api.txt` was already fresh, so no `chore(21-04): refresh api.txt` commit was needed. `ReceiptRow.kt` was not touched by this plan.

## Phase roll-up (plans 21-01 to 21-04)

| Plan | Outcome | Commits (all LANE 1) |
| ---- | ------- | -------------------- |
| 21-01 | `SheetScaffold.shape` before the trailing `content`, recording-Shape probe, 24dp-corner gallery demo | 4043cdb, 3ec011a, 9e21bc8 |
| 21-02 | `SegmentedOptionStyle { Material, Pill }`, `disabledOptions`, `style`, private Pill branch, `Dimens.SegmentedPill`, gallery demos | 2c1b4e0, 93dbd05, a42b03c |
| 21-03 | `ReceiptRow`, `Dimens.ReceiptRow`, Cards PATTERN entry, vocabulary acknowledgment; tags `receipt_row_summary`, `receipt_row_trailing_action`, `receipt_row_expanded_content` (stable), `receipt_row_chevron`, `receipt_row_value_column`, `receipt_row_cue` (test-only) | a273dea, f78bd24, c96e05b |
| 21-04 | motion, long-label and accordion proof, phase gate | 80a55f6, 2a7d8e0 |

Carried-over flags: SHAPE_PROBE: PASS (21-01). A2 confirmed (single line exactly `lineHeight` tall with `LineHeightStyle(Center, None)`), A3 confirmed (`layoutInput.style.color` and `textDecoration` readable), A6 confirmed (Pill touch target at least 48dp) (21-02, 21-03). PIXEL_PROBE: not run anywhere in the phase; no `captureToImage` in any Phase 21 test, so there is no PIXEL_PROBE: FAIL to record either.

## Phase gate outputs

- `./gradlew apiDump testDebugUnitTest detekt apiCheck` (one invocation, 1m 18s): BUILD SUCCESSFUL. Full unfiltered suite: 100 suites, 968 tests, 22 skipped (pre-existing ignores), 0 failures, 0 errors. detekt: no findings at the zero baseline. apiCheck passed.
- `git diff --exit-code -- api.txt` after the dump: exit 0 (committed api.txt equals a fresh dump).
- `bash tools/test/run-all.sh`: all five fixture scripts `PASS`, none `FAIL` (classify 12/12, precommit hook 6/6, additive diff 5/5, binary ABI 52/52).
- Protected G-15-1 / Material tests (`SegmentedOptionSelectorTest`, `ApproachLadderCardLongLabelTest`, `ApproachLadderCardTest`) vs v2.5.1: `git diff --exit-code` exit 0, byte-identical.
- `git diff --numstat v2.5.1 -- api.txt`: `111 3`. The three removed lines are the old `DynamicActionButton(`, `SheetScaffold(` and `SegmentedOptionSelector(` signatures. `SegmentedOptionStyle` and `ReceiptRowKt` are in `api.txt` (5 matching lines).
- `git diff --numstat v2.5.1 -- SegmentedOptionSelector.kt SheetScaffold.kt`: `119 0` and `7 1` (the one removed line is the forwarding line that gained a comma). The plan 21-02 contingency was not used.
- `git diff --exit-code v2.5.1 -- config/detekt-baseline.xml config/detekt/detekt.yml config/detekt-compose.yml`: exit 0.
- `git log v2.5.1..HEAD -S"DeprecationLevel.HIDDEN" -- src/main`: prints nothing (no hidden-deprecated overload or shim added or touched).
- `git tag --list "v2.6*"`: prints nothing. Nothing pushed.
- `bash tools/classify-hub-change.sh --baseline v2.5.1`: `LANE 1 (mode=additive, baseline=v2.5.1)`.
- Nothing staged under `.planning/graphs`, `.planning/uat-pending`, `.planning/WAITING.json` or the v2.5 `.gate` directory.

### Worktree note on the drift guards

This plan ran in the `agent-*` worktree `.../yahirandroidtaste/.claude/worktrees/agent-a13b34a1924661c8b`. `DomainVocabularyDriftGuardTest` and `ComponentRegistryDriftGuardTest` walk up to the first directory named `yahirandroidtaste`, which is the MAIN checkout, not the worktree (the same 20-04 finding). Their green result here therefore says nothing certain about the worktree's new sources. The real confirmation is one full `./gradlew testDebugUnitTest` in the main checkout after this worktree merges.

## Gate-1 handoff

`node gsd-tools.cjs query phase.has-uat-criteria 21 --pick has_uat_criteria` prints `true` (5 criteria, binding). `21-CONTEXT.md` carries no `device_exempt` line (count 0). This plan did not waive Gate-1, edit a ROADMAP criterion, write any `*-SELF-UAT.md`, write under `.planning/uat-pending`, or touch a device. The orchestrator's `verify_work_agentic_gate` owns it.

For the dedicated tester:

- **Surfaces (Explorer gallery).** Cards family -> ReceiptRow detail: Default / Pressed-Selected (expanded) / Disabled (dimmed) state cells, the Variants rows, and the single-open accordion at the bottom. Pickers family -> SegmentedOptionSelector detail: the Pill demo, the Pill with a disabled first option, the Material demo with a disabled second option, and the Pill with long German labels (truncation). Sheets family -> SheetScaffold detail: the "SheetScaffold — custom shape (24dp top corners)" button.
- **Addresses.** Tags `receipt_row_summary`, `receipt_row_trailing_action`, `receipt_row_expanded_content`. Segment content descriptions of the form "<label>, selected" and "<label>, not selected". Accordion rows are found by title text "Accordion row 1" to "Accordion row 3" and click label "Toggle row N"; detail texts are "Accordion detail 1" to "Accordion detail 3". Button labels as shown in the gallery.
- **Dynamic-color caveat.** The Explorer runs with dynamic color, so on a Material You device the tone and `primary` colors are wallpaper-derived. Hub-palette hex claims are Robolectric-only by design.
- **Device work is TESTER-only** (`adb -s R5CT10XNKQN`); nothing in this plan touched it.

## Deviations from Plan

**1. [Rule 1 - test instrument] `didOverflowWidth` replaced.** The plan asked for the value's `didOverflowWidth` to be false. In this Compose/Robolectric NATIVE harness the reported text layout has `size` 42 x 24 against `multiParagraph.width` 360 (the slot), so `didOverflowWidth` is true for ANY text narrower than its slot, including a plainly whole "1.250" (observed on all four value cases). The test instead asserts one line, not ellipsized, `getLineRight(0) <= size.width`, and the node's right edge inside the 360dp window. Same intent (value shown in full), valid instrument. Test-only; no component change.

**2. [Rule 3 - blocking, test-only] Four small test fixes before the first green run.** A Kotlin platform clash (`setFlag` helper vs the property setter) was renamed to `driveFlag`. With the clock paused, a state write from `runOnUiThread` never reached the composition, so the helper calls `Snapshot.sendApplyNotifications()`; the tag-node text check became `hasText("detail") and hasAnyAncestor(hasTestTag(...))` because the tagged `Box` does not merge its child's text. Window height raised to `h3000dp` for the registry render test so `performClick` reaches every accordion row.

**3. [Tooling] Commit ledger not persisted.** As in 21-01 to 21-03 the sandbox refuses the compound `git rev-parse --git-dir` redirect, so no ledger file was written. `plan_head_before` is the worktree base 36719b9 and `commits: 2` was measured with `git rev-list --count 36719b9..HEAD` before this SUMMARY commit.

**4. [Plan expectation] `CardsFamilyScreen.kt` removed-line count.** The plan expected a single removed line (a ToneCard trailing comma) against v2.5.1; the measured value is `184 0` (no removed lines), which is stricter than the plan. Nothing to fix.

**5. [Process] Tracer red step not run separately** (same as 21-02 and 21-03).

**Total deviations:** 5 (none changed scope or behavior; no `ReceiptRow.kt` defect was found).

## Documented limit (R-04)

A plain `Row` gives the chevron no minimum width when the value alone is wider than the row (the 80-digit case squeezes the title to nothing and can squeeze the chevron). The extreme-value test asserts only boundedness; the KDoc "Overflow" paragraph already tells callers to pass short, already-formatted values.

## Deferred to Phase 24

- README.md, API.md and the `ComponentRegistry` KDoc counts are stale after the fifth new registration; no test enforces them.
- The API.md stable-tag section for the three `receipt_row_*` stable tags and the Y-14 docs (the three test-only hooks stay out).
- The owner screencap review of the Receipt row, the Pill selector and the 24dp-corner sheet on the TESTER (Y-15).
- Phase 22 consumption: `OutcomeSheet` passes the 24dp shape to `SheetScaffold(shape)`; CT uses `ReceiptRow` in `topLevelContent`.
- One full `./gradlew testDebugUnitTest` in the main checkout after merge (worktree drift-guard note above).

## Self-Check: PASSED

- Files exist: ReceiptRowMotionTest.kt, ReceiptRowLongLabelTest.kt, ReceiptRowRegistryRenderTest.kt, ReceiptRowSourceContractTest.kt, CardsFamilyScreen.kt, and this SUMMARY.
- Commits 80a55f6 and 2a7d8e0 exist on `worktree-agent-a13b34a1924661c8b`.
- All task acceptance criteria and plan verification commands green; STATE.md and ROADMAP.md untouched.
