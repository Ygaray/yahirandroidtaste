---
phase: 27-open-from-the-sheet-undo-center-pill-fixes
verified: 2026-10-17T16:30:00Z
status: passed
score: 5/5 must-haves verified
covered_files:
  - ".planning/REQUIREMENTS.md#FOPEN-01"
  - ".planning/REQUIREMENTS.md#FOPEN-02"
  - ".planning/REQUIREMENTS.md#FOPEN-03"
  - ".planning/REQUIREMENTS.md#POL-01"
  - ".planning/REQUIREMENTS.md#POL-02"
  - ".planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-01-PLAN.md"
  - ".planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-01-SUMMARY.md"
  - ".planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-02-PLAN.md"
  - ".planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-02-SUMMARY.md"
  - ".planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-03-PLAN.md"
  - ".planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-03-SUMMARY.md"
  - ".planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-04-PLAN.md"
  - ".planning/phases/27-open-from-the-sheet-undo-center-pill-fixes/27-04-SUMMARY.md"
  - "api.txt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByDot.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/HandledByPillShape.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/OpenTargetRow.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetReceipt.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/model/BatchRowResultUiModel.kt"
  - "src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt"
covered_digest: "v1:sha256:d5345985841c85a55fc4f37f40bef9bd0ac052708d19b6dce25ffd5fa59f46ce"
behavior_unverified: 0
overrides_applied: 0
---

# Phase 27: Open from the sheet and Undo Center, pill fixes: Verification Report

**Phase Goal:** From a success sheet or the Undo Center, the user taps the row that names an item to jump to it (a chevron, no label), in both the Receipt and Classic `OutcomeSheet` styles, and the handled-by pill stays readable at large font sizes and under dynamic color.
**Verified:** 2026-10-17T16:30:00Z
**Status:** passed (device-only aspects are deferred to the Gate-1 on-device self-UAT, see below)
**Re-verification:** No, initial verification

## Goal Achievement

Verified against the code on `main`, not the SUMMARYs. I ran the named tests myself with a forced fresh run (`--no-daemon cleanTestDebugUnitTest testDebugUnitTest`, 2 tasks executed, not reused from cache), then `--rerun` on `detekt` and the Metalava tasks.

### Observable Truths (ROADMAP success criteria, the contract)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | With `Success.target` and `onOpenTarget` on either `OutcomeSheet` overload (incl. hoisted `sheetState`), the row naming the item shows a chevron and calls `onOpenTarget(target)` on tap, in Receipt and Classic, no action label. No target or no handler renders exactly as `v2.7.1`; existing `OutcomeSheet` tests pass unedited (FOPEN-01) | VERIFIED | `OpenTargetRow.kt` (merged `Role.Button`, `clickable(onClickLabel = openLabel, ...)`, decorative `KeyboardArrowRight` chevron, no visible action text). Receipt: `ReceiptSuccessBody` calls `SuccessOpenTargetRow` after the pill. Classic: new `OutcomeSheetContentOpenTarget` / `SuccessBodyOpenTarget` inside the single `SheetScaffold` lambda, pinned v2.5.1 functions untouched (`git diff v2.7.1 -- OutcomeSheet.kt` shows only additions, `HandledByRow`, `SuccessBody`, `BatchResultsList` not edited). Both overloads gained `onOpenTarget: ((FeedbackTarget) -> Unit)? = null` as the last param. Tests pass: `receipt_/classic_targetRow_tapOpensOnce_via{Hoisted,DefaultState}Overload` (4 combos), `noHandler_withTarget_rendersTheSameTreeAsNoTarget_*`, `handler_withoutTarget_*`, `libraryNeverHidesTheSheet_*`, `bothStyles_rowCarriesButtonRole_openLabelClickLabel_noStateDescription*`. Existing OutcomeSheet tests (`OutcomeSheetTest`, `ClassicParityTest`, `ClassicParitySourceContractTest`, `ReceiptSourceContractTest`, etc.) were not edited in this phase (`git diff 65bc8cd..HEAD --name-status -- src` lists only `OutcomeSheetFixtures.kt` (additive fixture params), `V271CallSiteCanaryTest.kt` and `UndoCenterScreenSourceContractTest.kt` as modified tests) and all pass |
| 2 | In a batch success each `BatchRowResultUiModel` with a `target` opens its own item; rows without one show no chevron, aren't clickable, expose no click action (FOPEN-02) | VERIFIED | `BatchRowResultUiModel.target` appended last with default `null`. `OpenTargetBatchRow` applies `semantics(mergeDescendants)` + `openTargetClick` and the chevron only when `target != null`; target-less rows reserve no chevron space and no min height. Used by both styles (`ReceiptSuccessBody`, `SuccessBodyOpenTarget`). Passing tests: `receipt_/classic_mixedBatch_eachTargetedRowOpensItsOwnItem_targetlessRowIsInert`, `equalTargetsOnTwoRows_eachCallsTheHandlerOnItsOwnTap`, `targetedRowBetweenTargetlessRows_leavesNeighboursInert`, `targetlessRow_reservesNoChevronSpace`, `classic_batchOnlyOutcome_reachesTheOpenPath`, `allRowsWithoutTargets_rendersTheV271List`, `noHandler_withTargetedRows_rendersTheV271List_bothStyles` |
| 3 | A new `UndoCenterScreen` overload with `onOpenTarget`: an Available entry with a target opens on tap; non-Available or target-less entries don't. The existing call renders exactly as `v2.7.1` and trailing-lambda call sites still compile (FOPEN-03) | VERIFIED | `UndoCenterScreen.kt`: second overload with `onOpenTarget` REQUIRED (between defaulted `onUndoGroup` and trailing `onClear`), delegating to the v2.7.1 overload through a `CompositionLocal`. `UndoHistoryRow` takes the open branch only when `entry.openableTarget != null` (Phase 25 derives null unless Available). Passing tests: `availableEntryWithTarget_tapOpensOnce`, `undoneEntry_doesNotOpen_andHasNoChevronOrClick`, `failedEntry_doesNotOpen`, `entryWithoutTarget_doesNotOpen_andRendersTheV271Row`, `groupHeader_neverOpens_membersDo`, `afterUndoViaTheStore_tappingTheRow_doesNotOpen` (stale-callback guard), `tappingUndo_runsUndoOnce_neitherOpensNorPeeks`, `UndoCenterScreenOpenParityTest` (D1-D4 goldens through the new overload), `UndoCenterScreenDefaultParityTest` and `UndoCenterScreenSourceCompatV251Test` (v2.7.1 shapes). The one edited contract test (`there are exactly two public UndoCenterScreen functions`) is the deliberate D-04 count edit 1 to 2 with an added parameter-list assertion |
| 4 | At font scale 2.0 a long handled-by pill that wraps keeps all its text inside its shape (corner radius capped when multi-line); at 1.0 a one-line pill looks as in `v2.7.1` (POL-01) | VERIFIED (Robolectric); on-device visual wrap deferred to Gate-1 | `HandledByPillShape.kt`: radius = `min(height/2, cap)`, cap = half a one-line pill, computed in `createOutline` from final size and `Density`. `ReceiptHandledByPill` uses `rememberHandledByPillShape()` in place of `CircleShape`. Native-graphics Robolectric test `wrappedLongGermanPill_atFontScale2_keepsEveryTextCornerInsideTheShape` measures real wrapped text corners (asserts line count > 1) against the production outline; `controlCircleShape_failsTheSameCornerCheck_whenWrapped` proves the check is not vacuous; `oneLinePill_radiusEqualsCircleShape_atFontScales`, `zeroSizeOutline_neverThrows`, `unspecifiedOrEmLineHeight_usesTheFallback_andNeverThrows`, `pillBackgroundUsesTheCappedShape` all pass. Classic `HandledByRow` untouched (pin test unedited, passing) |
| 5 | The Grammar (warn, on-device) dot meets 3:1 non-text contrast against the pill container under the hub light and dark schemes and a dynamic-color sample (POL-02) | VERIFIED (JVM); real-device dynamic color deferred to Gate-1 | `HandledByDot.kt`: `handledByDotRing(fill, pill, scheme)` returns null when the fill (composited over the pill) is already >= 3:1, otherwise a ring (`onSurface`, else the better of black/white, always >= 4.58:1). `ReceiptHandledByPill` draws it via `handledByDotRingBehind` before the dot's own background; `ToneDefaults.warn()` and `handledByDotColor` unchanged (`handledByDotColor_isUnchanged`, `handledByDotColorSourceIsByteStable` pass). Passing tests: `hubLightAndDarkDefaults_needNoRing` (hub fill `onTertiaryContainer` = `ToneDefaults.warn().content` passes 3:1 without help), `syntheticWhiteOnLightPill_getsARing_withContrastAtLeast3`, `dynamicSamples_eitherPassOrGetAPassingRing` (4 wallpaper-style schemes, at least one needs a ring), `ringIsTotal_overAGreyRamp` (0..255), `threeToOneBoundary_flipsBetweenAdjacentGreys`, `translucentFill_isMeasuredAsPaintedOverThePill`. The roadmap wording "dot meets 3:1" is realized by the locked D-07 design: fill unchanged, a contrast ring supplies the 3:1 boundary when the fill alone fails |

**Score:** 5/5 truths verified (0 present-but-behavior-unverified). The behavior-dependent truths (tap calls the handler exactly once, in-flight lock then unlock, open branch dropped after Undo, hold never opens) each have a passing behavioral test, so none rests on symbol presence alone.

### Plan-level must-haves not in the roadmap (spot-checked)

| Must-have | Status | Evidence |
|-----------|--------|----------|
| Library never hides the sheet, dismisses, or navigates (D-05) | VERIFIED | `libraryNeverHidesTheSheet_orCallsDismiss_afterATap_{classic,receipt}` pass; KDoc on both overloads states hoist-and-`hide()` and idempotent handler |
| Classic path is not a second `SheetScaffold` call site (PD-2) | VERIFIED | One `SheetScaffold` call with an if/else inside the lambda; `swappingTargetOnOneCallSite_keepsTheSheetVisible` passes |
| In-flight lock (PD-1) | VERIFIED | `inFlight_locksTheRow_dimNoClick_thenUnlocks{,_classic}`, `inFlight_locksTargetedRows_thenUnlocks`; review fix IN-01 also adds `disabled()` semantics |
| Long German and Spanish ~360dp: chevron must not push the value off | VERIFIED (JVM) | `OutcomeSheetOpenTargetLongLabelTest`, `UndoCenterScreenOpenTargetLongLabelTest` pass; chevron is a fixed sibling measured before the weighted text column |
| Source compatibility | VERIFIED | `V271CallSiteCanaryTest` passes; `tools/verify-api-source-additive.sh v2.7.1` prints `API SOURCE-ADDITIVE OK ... removed_members=0 required_param_changes=0 signature_changes=0 accepted_exceptions=0`; `metalavaCheckCompatibilityRelease` (the `apiCheck` gate) green on a forced rerun |
| `ComponentRegistry` drift guard and CATALOG drift guard | VERIFIED | `ComponentRegistryDriftGuardTest`, `ComponentRegistryTierTest`, `CatalogDocsDriftGuardTest` pass. No new public composable name was added (the overload shares the name; new tags `open_target_row` / `open_target_chevron` deliberately avoid the `outcome_sheet_` docs-guard prefix) |

### Judgment-tier prohibitions (non-authoritative verdict)

Three judgment-tier prohibitions are carried by the plans. Verdict: held, each backed by deterministic tests, but recorded as a non-authoritative judge result. Complete with 3 judgment-tier prohibitions flagged `unverified-prohibition` (human review optional, not blocking).

| Prohibition | Verdict | Backing |
|-------------|---------|---------|
| FOPEN-01: never expose the opaque `FeedbackTarget.key` (tag, description, semantics, log) | Held | `grep` of phase main sources for `.key`, `Log.`, `println` returns nothing; `opaqueKey_neverAppearsInTheSemanticsTree{,_receipt}`, `opaqueKeys_neverAppearInTheSemanticsTree` (sheet, batch, Undo Center) and `OpenTargetHygieneSourceContractTest` pass |
| FOPEN-01: a v2.7.1 caller sees identical tree, path and call shapes; library never hides, navigates, undoes or dismisses | Held | parity tests, canary, additive check above |
| FOPEN-03: never open an Undone/Failed entry, a group header, or as a side effect of hold, scroll, fling or Undo tap | Held | `undoneEntry_*`, `failedEntry_*`, `groupHeader_neverOpens_membersDo`, `holdingAnOpenableRow_showsThePeek_andReleaseNeitherOpensNorUndoes`, `dragOverAnOpenableRow_neverOpens`, `tappingUndo_runsUndoOnce_neitherOpensNorPeeks` |
| POL-02: never recolor `warn()`, resize the dot, or alter a passing caller | Held | `hubLightAndDarkDefaults_needNoRing` (passing callers get `null`, no draw call), `handledByDotColor_isUnchanged`, `pillDotAppliesTheRingBeforeItsBackground` |

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `component/OpenTargetRow.kt` | shared single-row and batch open anatomy | VERIFIED | 282 lines, wired from `OutcomeSheetReceipt.kt` and `OutcomeSheet.kt`; all declarations internal/private |
| `component/HandledByPillShape.kt` | capped-radius pill shape | VERIFIED | Wired in `ReceiptHandledByPill` (replaces `CircleShape`) |
| `component/HandledByDot.kt` | ring decision and modifier | VERIFIED | Wired in `ReceiptHandledByPill` |
| `model/VoiceOutcomeUiState.kt` | `target`, `targetLabel`, `targetDetail` appended after `endAction` | VERIFIED | All defaulted `null` |
| `model/BatchRowResultUiModel.kt` | `target` appended | VERIFIED | Defaulted `null` |
| `component/OutcomeSheet.kt` / `OutcomeSheetReceipt.kt` | `onOpenTarget` on both overloads, Classic path in new functions | VERIFIED | See truth 1 |
| `feedback/UndoCenterScreen.kt` | second overload and open row | VERIFIED | See truth 3 |
| `api.txt` | regenerated signature | VERIFIED | Matches `metalavaGenerateSignatureRelease` on a forced rerun (`apiCheck` equivalent green) |

### Key Link Verification

| From | To | Via | Status |
|------|----|-----|--------|
| `OutcomeSheet` (both overloads) | `OutcomeSheetReceipt` / `OutcomeSheetContentOpenTarget` | `onOpenTarget` forwarded | WIRED |
| `ReceiptSuccessBody` / `SuccessBodyOpenTarget` | `SuccessOpenTargetRow`, `OpenTargetBatchList` | direct calls, gated on target + handler | WIRED |
| `UndoCenterScreen` (new) | `UndoHistoryRow` | `LocalUndoCenterOpenTarget` provided around the v2.7.1 overload | WIRED |
| `UndoHistoryRow` | `UndoHistoryOpenRow` | `openableTarget != null` and local handler non-null | WIRED |
| `ReceiptHandledByPill` | `rememberHandledByPillShape`, `handledByDotRing`, `handledByDotRingBehind` | direct calls | WIRED |

### Data-Flow Trace (Level 4)

| Artifact | Data | Source | Real data | Status |
|----------|------|--------|-----------|--------|
| `OpenTargetRow` | title, detail, target | `Success.targetLabel/targetDetail/target`, caller supplied | Yes | FLOWING |
| `OpenTargetBatchRow` | per-row target | `BatchRowResultUiModel.target`, caller supplied | Yes | FLOWING |
| `UndoHistoryOpenRow` | target | `UndoHistoryEntry.openableTarget` (Phase 25, store-derived, Available only) | Yes | FLOWING |

### Behavioral Spot-Checks and Gates (run by the verifier)

| Check | Command | Result | Status |
|-------|---------|--------|--------|
| Phase tests, fresh run | `./gradlew --no-daemon cleanTestDebugUnitTest testDebugUnitTest --tests '*OpenTarget*' '*HandledBy*' '*OutcomeSheet*' '*UndoCenter*' '*V271CallSite*' '*ComponentRegistry*' '*Catalog*'` | 43 classes, 438 tests, 0 failures, 0 errors, 0 skipped | PASS |
| Source-additive API check | `tools/verify-api-source-additive.sh v2.7.1` | `API SOURCE-ADDITIVE OK` (0 removed, 0 signature changes, 0 accepted exceptions) | PASS |
| apiCheck (forced) | `metalavaGenerateSignatureRelease --rerun metalavaCheckCompatibilityRelease --rerun` | BUILD SUCCESSFUL | PASS |
| detekt zero-baseline (forced) | `detekt --rerun` | BUILD SUCCESSFUL | PASS |
| Debt markers | `grep TBD/FIXME/XXX/TODO/HACK` over files changed since plan start | none | PASS |

The full workspace suite and `tools/test/run-all.sh` were not re-run by the verifier (one filtered battery only, per the single-run rule); 27-04-SUMMARY records a full-battery pass, and the review-fix commits after it were covered by the filtered run above, which includes every file those fixes touched.

### Probe Execution

SKIPPED: the phase declares no `probe-*.sh` scripts.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| FOPEN-01 | 27-01, 27-02 | `Success.target` row, label-less, chevron, both styles, `onOpenTarget` on both overloads | SATISFIED | Truth 1 |
| FOPEN-02 | 27-02 | Per-row batch targets; target-less rows inert, no chevron | SATISFIED | Truth 2 |
| FOPEN-03 | 27-03 | `UndoCenterScreen` overload with `onOpenTarget` | SATISFIED | Truth 3 |
| POL-01 | 27-04 | Pill text stays inside its shape when wrapped at font scale 2.0 | SATISFIED | Truth 4 |
| POL-02 | 27-04 | Grammar dot contrast-safe under dynamic color | SATISFIED | Truth 5 |

All five requirement IDs from the PLAN frontmatter (`requirements:` of 27-01 to 27-04) map to the five Phase 27 rows in REQUIREMENTS.md. No orphaned requirements: REQUIREMENTS.md maps only these five to Phase 27 (POL-03 belongs to Phase 28).

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `feedback/UndoCenterScreen.kt` | 453 | `onLongClickLabel = "Preview"` hardcoded English | Info | New TalkBack action label is not caller-localizable. Consistent with the screen's existing hardcoded `"Undo"` / `"Clear history"`, so not a regression; worth a localizable param in a later polish pass |
| 27-03 plan truth text | n/a | Plan said "single `detectTapGestures` owner, no `clickable`"; code review WR-01/WR-02 replaced it with `combinedClickable` | Info | Superseded deliberately and documented in 27-REVIEW-FIX; the roadmap criterion (tap opens, hold peeks and never opens, Undo separate) still holds and is test-covered |
| `.planning/REQUIREMENTS.md`, `ROADMAP.md` | n/a | FOPEN-01..03, POL-01/02 still `Pending`; Phase 27 header still `0/4 plans complete` | Info | Tracking not yet updated; the orchestrator owns this on phase completion |

No stubs, no hollow props, no debt markers.

### Deferred to Gate-1 (on-device self-UAT; not failed here)

These are Gate-1 criteria, not phase failures, and are not repeated as `human_verification`:

- Visual wrap of the long German pill at font scale 2.0 on the TESTER, and the one-line pill looking as in `v2.7.1` at 1.0 (JVM geometry covered above).
- Warn dot contrast under the device's real dynamic color and the white-on-light ring pixel probe (JVM synthetic and sample schemes covered above).
- Ripple/press indication, keyboard/D-pad activation, and TalkBack/switch-access long-click "Preview" action on the Undo Center open row (review fixes WR-01/WR-02 are exercised in Robolectric for focusability and the `OnLongClick` action only).
- TalkBack announcement of the sheet target rows' open label (also on the Phase 28 sweep list, A11Y-02).
- Real-sheet tap in Receipt and Classic with German strings at font scale 2.0, mixed batch, and Undo Center target rows (the plans' `backstop` statements).

### Deferred Items (later phases, informational)

| Item | Addressed In | Evidence |
|------|--------------|----------|
| API.md / STYLE / INTEGRATION docs for the new params and tags | Phase 29 | ROADMAP Phase 29 "API/STYLE/INTEGRATION docs"; 27-01 deliberately keeps tags out of the `outcome_sheet_` docs guard |
| Explorer go-to-item demos (English and German) | Phase 28 | Phase 28 SC3 (DEMO-03) |
| TalkBack sweep of the new rows | Phase 28 | Phase 28 SC1 (A11Y-02) |

### Human Verification Required

None beyond the Gate-1 criteria above.

### Gaps Summary

No gaps. All five roadmap success criteria hold in the code on `main`, every requirement ID is accounted for, the additive and `apiCheck` gates are green on forced reruns, detekt is clean, and 438 targeted tests pass with 0 failures. The remaining confirmation is physical (ripple, keyboard, TalkBack, real dynamic color, font-scale 2.0 visuals), which belongs to the Gate-1 on-device self-UAT.

---

_Verified: 2026-10-17T16:30:00Z_
_Verifier: Claude (gsd-verifier)_
