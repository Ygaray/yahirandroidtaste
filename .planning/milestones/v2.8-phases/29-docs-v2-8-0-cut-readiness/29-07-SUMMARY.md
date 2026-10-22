---
phase: 29-docs-v2-8-0-cut-readiness
plan: 07
subsystem: feedback-deck
tags: [gap-closure, gate-1, sc3, long-string, spanish, german, font-scale-2, capped-pill, scroll-inside-card, internal-only]
status: complete
gap_closure: true
requires:
  - phase: 29-05
    provides: "29-05-SELF-UAT.md: the Gate-1 SC3 failure on the Spanish stacked card at font scale 2.0 (has_fail)"
provides:
  - "the feedback card Undo keeps a capped-radius shape once its label wraps (internal CappedPillShape, internal CappedPillTextActionButton)"
  - "a stacked card never taller than the host minus one peek strip per older card; a taller card scrolls its own content inside itself"
  - "forced-fresh unit gate on the final tree and the untracked .gate/PLAN07_SRC_HEAD marker for plan 29-06"
affects: [29-06]
requirements-completed: []
commits: 3
plan_head_before: 7ceac5599488394d2d3237e56e319f73832d5a28
actuals:
  tokens: 11200
  tasks: 3
  commits: 3
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/CappedPillShape.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/CappedPillShapeTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCardUndoShapeTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCardTallContentTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetrics.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetricsTest.kt
    - docs/STYLE.md
---

# Phase 29 Plan 07: Clipped Spanish stacked card (Gate-1 SC3) Summary

The stacked feedback card's Undo now keeps the handled-by pill's capped-radius rule through an internal path, and a card taller than the space the deck gives it scrolls its own content, so the Gate-1 SC3 defect (Spanish snackbar, font scale 2.0, about 360 dp) is closed in Robolectric. The device re-run is the orchestrator's. `api.txt` is byte-identical and the additive check against v2.7.1 passes.

## Premise result (PD-1)

Confirmed by the RED run, with measured numbers. The plan's premise correction held: a bounded host reaches `DeckLayout` through `Modifier.verticalScroll`, so `measureDeck` sees an infinite `constraints.maxHeight` and never limited a card. In the 360 dp host at font scale 2.0 the unmodified tree measured the Spanish card at **774 px** and the German card at **742 px** against a **360 px** host (density 1.0), and a three-card deck's front card at 774 px against a limit of 332 px. The card was as tall as its content and was only clipped by the deck's scroll viewport, as the Gate-1 dump showed. The claim in 29-05 that "the collapsed card does not scroll" was inferred from code and had never been tried; the fix first makes the clamp real, then adds the scroll.

## RED runs (never committed, logs under the untracked `.gate/`)

Task 1, `.gate/p07-red-task1.log` (13 tests, 5 failed, exactly the expected five; the other eight guards were green before and after):
- `spanishTallCard_atFontScale2_isClampedToTheHostHeight`, `germanTallCard_atFontScale2_isClampedToTheHostHeight`
- `spanishTallCard_verticalDrag_scrollsTheContentInsideTheCard_untilTheLastLineIsVisible`, `germanTallCard_verticalDrag_scrollsTheContentInsideTheCard_untilTheLastLineIsVisible`
- `threeCardDeck_withATallFront_fitsTheHost_andKeepsTheTwoPeekStrips`

The metric tests for `feedbackCardMaxHeight` were written after that run (they would not compile before the function existed), so their RED is a compile error, one line.

Task 2, `.gate/p07-red-task2.log`, against the capsule scaffold (15 tests, 6 failed, exactly the expected six): `wrappedSpanishUndo_atFontScale2_keepsEveryTextCornerInsideTheShape`, `wrappedGermanUndo_atFontScale2_keepsEveryTextCornerInsideTheShape`, `wrappedHeight_isCappedAtTheOneLineHalfHeight`, `minimumHeightRaisesTheCap_atFontScale1`, `unspecifiedOrEmLineHeight_usesTheFallback_andNeverThrows`, and the wiring source contract. The CircleShape controls, the bounds tests, the one-line tests and the zero-size test were green before and after.

## Commits

| Task | Commit | Message |
|------|--------|---------|
| 1 (tracer) | `e5392d0` | fix(29-07): a tall stacked card scrolls its own content inside the host (Gate-1 SC3, D-03) |
| 2 | `46da199` | fix(29-07): the stacked card's Undo keeps a capped-radius shape once its label wraps (Gate-1 SC3, D-03) |
| 3 | `0bad7b6` | docs(29-07): STYLE.md wrap-safe button and scrolling card rule |

`commits: 3` is measured (`git rev-list --count 7ceac55..HEAD` before this SUMMARY). The two `src/main` commits went in with `HUB_LANE_OVERRIDE=2` after the pre-commit hook blocked them as lane 2; no `--no-verify` anywhere.

## What changed

- `feedback/FeedbackDeckMetrics.kt`: `FEEDBACK_CARD_MIN_HEIGHT` (56 dp, moved from the card) and `feedbackCardMaxHeight(available, count, peekPx, floorPx)`: host height minus one peek strip per older card, floored at the card minimum or the host, never negative, infinite stays infinite.
- `feedback/FeedbackDeck.kt`: the bounded host's height goes from the `BoxWithConstraints` into `DeckLayout` and `measureDeck`, which gives every card that finite max height. Collapsed back-card clamp, fanned geometry, fan cap and the container scroll are unchanged.
- `feedback/FeedbackCard.kt`: the row sits in `BoxWithConstraints(propagateMinConstraints = true)`; when the incoming max height is bounded and the card is not a back card of a collapsed deck, the row gets `verticalScroll(cardScroll, enabled = cardScroll.maxValue > 0)` as its first modifier (the 8 dp padding scrolls with the text). The Undo calls the internal `CappedPillTextActionButton`. Gesture stack, semantics and blocker untouched.
- `component/CappedPillShape.kt` (new, internal): `CappedPillShape` (data class), `cappedPillCapPx` (the pill cap, `handledByPillCapPx`, or half the 40 dp text-button minimum, whichever is larger) and `rememberTextButtonCappedShape()`. `HandledByPillShape.kt` is not edited; no `Dimens` token added.
- `component/DynamicActionButton.kt`: internal `CappedPillTextActionButton`; the private `EmphasisActionButton` gains an optional last `shape` used by the Text emphasis only (`shape ?: ButtonDefaults.textShape`). The public `DynamicActionButton` signature, body and default shape are untouched.
- `docs/STYLE.md`: one bullet in section 2 next to the handled-by pill rule.

## Tests

Three new classes plus three added metric cases: `FeedbackCardTallContentTest` 13, `FeedbackCardUndoShapeTest` 10, `CappedPillShapeTest` 5, `FeedbackDeckMetricsTest` +3 (`cardMaxHeight_unboundedHostStaysUnbounded`, `cardMaxHeight_reservesOnePeekStripPerOlderCard`, `cardMaxHeight_neverBelowTheFloorOrTheHost`; 21 in the class now). Spanish and German, 360 dp, font scale 2.0: card inside the host, last line reachable by a vertical drag while the card stays put, vertical drag neither dismisses nor opens, swipe still dismisses, tap still opens, finger down pauses, long hold does nothing, spoken name and actions kept, three-card deck keeps its 14 dp strips, short card is 56 dp with no scroll range, unbounded host does not crash and is not clamped; Undo text corners inside the production outline while the CircleShape control loses a corner, label inside the button inside the card, single-line Undo is the exact capsule at 1.0, 1.5 and 2.0.

## Fresh unit gate on the final tree (HEAD `0bad7b6`)

| Check | Result |
|-------|--------|
| `apiDump` on its own, then `git diff --exit-code -- api.txt` and against `7ceac55` | unchanged, byte-identical |
| `testDebugUnitTest detekt apiCheck --rerun-tasks --no-build-cache --no-daemon` (one invocation, 43 tasks executed) | BUILD SUCCESSFUL in 2m 23s, rc 0 |
| `tools/verify-junit-fresh.sh` with 13 required classes | **JUNIT-FRESH OK**: FILES 182, STALE 0, tests 1942, skipped 22, failures 0, errors 0 (1911 in 29-05 plus 31 new) |
| Per-class | CappedPillShapeTest 5, FeedbackCardUndoShapeTest 10, FeedbackCardTallContentTest 13, V271CallSiteCanaryTest 12, HandledByPillShapeTest 10, CatalogDocsDriftGuardTest 16, ComponentRegistryDriftGuardTest 1, FeedbackStackHostLongLabelTest 7 |
| UP-TO-DATE or FROM-CACHE on `:testDebugUnitTest`, `:detekt`, `:metalavaCheckCompatibilityRelease` | none (grep rc 1) |
| `tools/test/run-all.sh` | rc 0, no FAILED line |
| `tools/verify-api-source-additive.sh v2.7.1` | `SEAMS: none`; `API SOURCE-ADDITIVE OK baseline=v2.7.1 removed_classes=0 removed_members=0 enum_constants_intact=73 required_param_changes=0 added_classes=5 added_members=48 signature_changes=0 accepted_exceptions=0` |
| `git diff --numstat v2.7.1 -- api.txt` | 78 added, 11 removed (same as 29-05) |
| INV-03 greps (hidden deprecation, Hilt host, consumer imports), configuration diff against v2.7.1 | all empty / identical |
| `git diff --name-only 55f5288 HEAD -- src/main` | exactly the five files of this plan |
| `git tag --list 'v2.8*'` | empty |
| Binary ABI (informational only) | exit 3, 37 lane-3 lines, the same count as 29-05 (BatchRowResultUiModel, VoiceOutcomeUiState, plus the PlaceMap user-agent and FeedbackEvent.Notify members already seen); no new line from this plan |

`PLAN07_SRC_HEAD` (untracked, `.gate/PLAN07_SRC_HEAD`): `0bad7b6694a37ae7770bfb02706c0cbe2be14802`, the last commit that can touch `src`, `api.txt`, `tools` or `build.gradle.kts`.

## Deviations from Plan

**1. [Rule 1 - Test premise] The two scroll-inside tests were vacuous on the unmodified tree**
- Found during: Task 1 first RED run. Only the clamp tests and the three-card test failed.
- Issue: on the unclamped tree the card is as tall as its content, so "the last line is inside the card" held trivially and the loop never dragged.
- Fix: added two assertions to those two tests (the content starts taller than the card at rest, and the last line ends inside the host). The second RED run then failed exactly the five tests the plan names. The first run's log is kept as `.gate/p07-red-task1-first-run.log`. The test was strengthened, not weakened.
- Files: `FeedbackCardTallContentTest.kt`. Commit `e5392d0`.

**2. [Scope note] `docs/superpowers/` untracked files** are pre-existing debris inside the docs path the Task 1 precondition lists; they are outside this plan and were left alone.

Total deviations: 1 auto-fixed (test strengthened), 0 architectural. Impact: none on scope; no public symbol changed.

## Discrepancy between the Gate-1 diagnosis and the code

The 29-05 root-cause block said the card is "as tall as the window and still too short" and that the collapsed card does not scroll. In code the card was not bounded by `measureDeck` at all (infinite max height inside the deck's own vertical scroll); it was only cut by the deck's viewport. The effect on the screen was the same; the cause and therefore the fix location (give the card a finite max height first) differ from what the diagnosis suggested.

## Hand-over

For the orchestrator (device work is not part of this plan; no `adb`, no `wm`, no harness ran here, the tester was not touched, no tag, no push, no repin, no rig message):

1. **HEAD to republish:** `0bad7b6694a37ae7770bfb02706c0cbe2be14802` (this SUMMARY and the STATE and ROADMAP commits come after it and touch no `src`). Its `src` equals `.gate/PLAN07_SRC_HEAD`.
2. **Before republishing:** delete the stale mavenLocal `1.10.0` directory and its `maven-metadata-local.xml`, then `./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q` (AGENT-DEVICE-TESTING.md), so the harness does not pick up the old build.
3. **Look at first:** es-snackbar at font scale 2.0, about 360 dp. Expect the Undo label over several lines with no glyph touching a rounded end, the card inside the window with the two peek strips above it, and one vertical swipe on the card bringing "por la noche" into view while the card itself does not move.
4. **Then repeat** the 27 other scenes that already passed in 29-05 (including de-snackbar at 1.0 and 2.0: a one-line "Rückgängig" must still be a capsule).
5. **Addendum form plan 29-06 expects:** a heading line starting with `## Addendum` in `29-05-SELF-UAT.md`, a line containing `Verdict: PASS`, the uat_run id, the HEAD sha the run used (its `src` must equal `PLAN07_SRC_HEAD`) and the Gate-2 fragment registered at `.planning/uat-pending/29-docs-v2-8-0-cut-readiness.md`.

**Plan 29-06 must not start before that Addendum exists and says pass.** This plan's green Robolectric run is not a Gate-1 pass. SHIP-05 is NOT marked complete here; plan 29-06 closes it. This is gap cycle 2 of 3.

## Self-Check: PASSED

All created files exist and are committed (verified by `git show --name-only` on each commit); commits `e5392d0`, `46da199`, `0bad7b6` are in `git log`; every acceptance criterion of Tasks 1 to 3 was run and passed (counts 13, 10, 5; five-file confinement; api.txt untouched; no tag).
