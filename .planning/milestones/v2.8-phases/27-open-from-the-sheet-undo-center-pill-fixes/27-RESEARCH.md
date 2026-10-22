# Phase 27: Open from the sheet & Undo Center, pill fixes - Research

**Researched:** 2026-10-17
**Domain:** Additive Compose UI work on the shipped v2.7.1 `OutcomeSheet` (Classic + Receipt), `UndoCenterScreen` and the Receipt handled-by pill. No new dependency, no new public composable.
**Confidence:** HIGH for the file map, pins, constraints and test infrastructure (all read from live source this session). MEDIUM for pill-shape geometry and the Undo Center gesture details until the Wave 0 NATIVE and Robolectric tests run. Device behaviour (font scale 2.0, dynamic color on the TESTER) is covered by Gate-1, TalkBack by Phase 28.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P27-D-xx in prose but use the parser shape.

- **D-01 [sheet-row-content]:** add `targetLabel`/`targetDetail` (amends D-15); falls back to summary
- **D-02 [row-placement]:** after the handled-by indicator, both styles (matches the mock)
- **D-03 [batch-rows]:** whole list switches; rows without target get no chevron, no click, no reserved space
- **D-04 [undo-center-tap]:** onTap + no-op onLongPress, merged Role.Button node; required-param overload; update the count contract deliberately _(provisional - refresh at execution; depends on Phase 25)_
- **D-05 [sheet-hide]:** callback only
- **D-06 [pill-shape]:** capped-radius shape, Receipt pill only; Classic HandledByRow untouched
- **D-07 [dot-contrast]:** contrast ring; ToneDefaults.warn() unchanged; proven on a synthetic white-on-light scheme in a unit test

Runtime refresh of D-04 (CONTEXT.md "Runtime Decisions"): refreshed after Phase 25 (ai-auto): onTap + no-op onLongPress, merged Role.Button node; required-param overload; update the count contract deliberately. A row is openable iff `entry.openableTarget != null` (Phase 25 derives it null unless Available, so Undone/Failed rows lose the chevron and click); grouped entries carry targets through the Phase 25 append overload, and a group header itself never opens.

Boundary text (CONTEXT.md): "Open from the sheet & Undo Center, pill fixes (UI) - milestone v2.8 (release v2.8.0, additive only). FOPEN-01..03, POL-01, POL-02. Classic SuccessBody/HandledByRow/BatchResultsList are SHA-pinned by OutcomeSheetClassicParitySourceContractTest: put go-to-item in NEW functions and keep `OutcomeSheetContent(outcome, colors = colors)` literal. Use an internal OpenTargetRow, never ReceiptRow (it announces Expanded/Collapsed)."

Also binding on this phase (brief): D-09 (`OutcomeSheet(..., onOpenTarget = null)` on both overloads, Receipt AND Classic; `UndoCenterScreen` overload with `onOpenTarget`), D-10 (label-less row, chevron, no target = no chevron and not clickable), D-14 (caller clears `Success.target` / batch `target` after Undo; the Undo Center derives it from entry status), D-16 (POL-02 bar is 3:1 non-text contrast in light, dark and dynamic schemes), D-11 (a11y: merged Role.Button node with `onClickLabel = openLabel`).

### Claude's Discretion
Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

### Deferred Ideas (OUT OF SCOPE)
999.4 voice-surface polish -> v2.8.1; combined "Undo & open"; share/MCP deep links; SB sheet-swap blink (app-side).

Specifics: Match the approved mock (direction A) and docs/STYLE.md; Yahir's picks: keep the chevron on openable cards, pause timers while touched or fanned.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| FOPEN-01 | `Success.target` makes the receipt row that names the item a label-less tap target with a chevron, Receipt and Classic, via a defaulted `onOpenTarget` on both `OutcomeSheet` overloads | The fields do NOT exist yet (see Key Correction). New model fields, one shared internal `OpenTargetRow`, a new Classic body (pins force this), Receipt threading, early branch in the Classic dispatch, `onOpenTarget` appended to both overloads. Parity: no target or no handler runs the unchanged v2.7.1 functions. |
| FOPEN-02 | Each `BatchRowResultUiModel` with a `target` opens its own item; rows without one: no chevron, not clickable, no click action in semantics | `BatchRowResultUiModel.target` appended (also absent today). New `OpenTargetBatchList` used only when a handler is passed AND at least one row has a target (D-03); `BatchResultsList` stays SHA-pinned and is still what v2.7.1 callers get. |
| FOPEN-03 | New `UndoCenterScreen` overload with `onOpenTarget`; Available entries with a target open on tap; others do not; v2.7.1 call renders as before | Required non-null `onOpenTarget` is what keeps the two overloads unambiguous. Phase 25 already ships `UndoHistoryEntry.openableTarget`. Row gets tap + semantics + chevron only when `openableTarget != null`; hold-to-peek must survive (Pitfall 4, 5). |
| POL-01 | At font scale 2.0 a long wrapped handled-by pill keeps its text inside its shape; one-line pill at 1.0 unchanged | Replace `CircleShape` in `ReceiptHandledByPill` by an internal capped-radius `Shape` whose cap is the one-line pill half-height at the current density. No `onTextLayout` state (flicker). |
| POL-02 | Warn handled-by dot meets 3:1 non-text contrast vs the pill in light, dark and dynamic schemes | Keep `handledByDotColor` and `ToneDefaults.warn()` byte-unchanged; add a pure `contrast-ring` decision function and a `drawBehind` ring that only exists when the fill is below 3:1 against `surfaceContainerHighest`. Proven on synthetic schemes in a JVM unit test. |
</phase_requirements>

## Summary

Phase 27 is five small, independent pieces of additive UI work that all touch one shared file, `api.txt`, so they must run serially: (1) model fields, (2) one shared internal go-to-item row, (3) routing it into both `OutcomeSheet` styles, (4) a second `UndoCenterScreen` overload, (5) two Receipt-pill fixes. The dominant engineering constraint is not the UI, it is the existing contract tests: `OutcomeSheetClassicParitySourceContractTest` SHA-pins `SuccessBody`, `HandledByRow`, `BatchResultsList` and four more Classic functions, `OutcomeSheetReceiptSourceContractTest` pins the dispatch order and the Success section order, `UndoCenterScreenSourceContractTest` pins the first `UndoCenterScreen` overload's parameter list and its count, and `UndoCenterScreenDefaultParityTest` pins the unmerged semantics tree of four default states. Every recommendation below is shaped to keep those green without edits, except the one edit the decisions explicitly authorize (the `UndoCenterScreen` count 1 to 2).

**Key correction to the briefing (verified in source):** the orchestrator prompt and the brief say `VoiceOutcomeUiState.Success.target` and `BatchRowResultUiModel.target` were "shipped by Phase 25". They were not. Phase 25 delivered `FeedbackTarget`, `FeedbackEvent.Notify/WithUndo.target`, `UndoHistoryEntry.target` + `openableTarget`, the store's `append` overload, `FeedbackStackState` and the host. `Success` and `BatchRowResultUiModel` have no `target` today and `OutcomeSheet` has no `onOpenTarget` (`api.txt` lines 1745 and 1122-1127 show the v2.7.1 shapes). Phase 27 must add `Success.target`, `Success.targetLabel`, `Success.targetDetail`, `BatchRowResultUiModel.target` and `onOpenTarget` on both overloads, and re-generate `api.txt`. Plans must not assume they exist.

**Primary recommendation:** Build in this serial order: (A) model fields + `api.txt`; (B) internal `OpenTargetRow` / `OpenTargetBatchList` in a new file; (C) Classic via new functions in `OutcomeSheet.kt` plus an appended branch between the Receipt `return` and the legacy `SheetScaffold`, Receipt via an optional `onOpenTarget` threaded through `OutcomeSheetReceipt` to `ReceiptSuccessBody`; (D) `UndoCenterScreen` provider overload with a private CompositionLocal and an early branch in `UndoHistoryRow`; (E) POL-01 and POL-02 (independent, may go first); (F) canary + contract edits + KDoc + Gate-1. Keep every v2.7.1 function body unchanged wherever an append can do the job; where a line rewrite is unavoidable commit with `HUB_LANE_OVERRIDE=2` and say why.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Which item a row points at | Consumer (builds `FeedbackTarget` into caller-built state) | Library (carries it opaquely) | Opaque key, no lambda (brief D-08); library never knows routes |
| Opening the item (navigation) | Consumer (`onOpenTarget`) | - | D-05: callback only. The library does not hide the sheet. KDoc + demo show hide-then-navigate |
| Dropping the target after Undo (sheet, batch rows) | Consumer (clears `target`) | - | D-14: `Success`/`BatchRowResultUiModel` are caller-built state; the library has no link to store entries |
| Dropping the target after Undo (Undo Center) | Library (`entry.openableTarget`) | - | Phase 25 derives null unless Available |
| Row semantics (merged Role.Button, `onClickLabel`) | Library `OpenTargetRow` | - | D-11; one shared row for both styles and the batch list |
| Pill corner radius at large fonts | Library (internal `Shape`) | - | Pure layout concern of the Receipt pill |
| Dot contrast guarantee | Library (pure function + draw) | Consumer (can still pass own `warnTone`) | Default tone unchanged; the guard only reacts to a failing pair |
| Long-string/font-scale proof | Test (Robolectric NATIVE) | Device Gate-1 | STYLE section 7: only screenshots count for sign-off |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Compose BOM | 2026.04.01 | foundation / ui / material3 for the new row, shape and tests | Already the project BOM [CITED: /home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md toolchain section, "Compose BOM 2026.04.01"] |
| `androidx.compose.foundation` `Modifier.clickable(onClickLabel, role)` | BOM | Sheet row tap target (no competing gesture owner on that node) | House precedent in `ReceiptRowSummary` [VERIFIED: component/ReceiptRow.kt "Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)"] |
| `detectTapGestures` + hand-written `semantics { role; onClick(label) }` | BOM | Undo Center row (hold-to-peek already owns the pointer) | House precedent: MicButton and `FeedbackCard` [VERIFIED: feedback/FeedbackCard.kt:157-158 `role = Role.Button` / `onClick(label = openTarget.openLabel.ifBlank { item.message })`; :197-199 `detectTapGestures(` / `onLongPress = { },`] |
| `Icons.AutoMirrored.Filled.KeyboardArrowRight` + `Dimens.ReceiptRow.ChevronSize` (24dp) | in-repo | The chevron | Same chevron the deck card uses [VERIFIED: feedback/FeedbackCard.kt:347,351] |
| JUnit 4.13.2, Robolectric 4.16.1, ui-test-junit4 1.11.0, kotlinx-coroutines-test 1.11.0 | existing | All tests | [CITED: 26-VALIDATION.md "Test Infrastructure"] |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `androidx.compose.ui.graphics.luminance` | BOM | WCAG luminance in the (main-source) ring decision | `contrastRatio` exists only in test sources [VERIFIED: src/test/.../theme/ContrastTestSupport.kt:10 `internal fun contrastRatio(a: Color, b: Color): Float`]; main needs its own 6-line internal copy |
| Existing `contrastingForeground(background)` | in-repo | Last-resort ring color | [VERIFIED: component/ColorUtils.kt:13 `fun contrastingForeground(background: Color): Color =`] |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| New internal `OpenTargetRow` | `ReceiptRow(onClick = ...)` | Rejected by CONTEXT: it always emits `stateDescription` Expanded/Collapsed and a rotating expand chevron [VERIFIED: component/ReceiptRow.kt ReceiptRowSummary `stateDescription = if (expanded) expandedStateLabel else collapsedStateLabel` and `showChevron = onClick != null && trailingAction == null`] |
| `onTextLayout { lineCount }` state for the pill | Custom capped-radius `Shape` | `onTextLayout` costs an extra frame and flickers; D-06 chose the shape |
| Darker/lighter dot tone | Contrast ring | D-07: `ToneDefaults.warn()` stays unchanged (it recolors every warn surface in both apps) |

**Installation:** none. No dependency, catalog entry or BOM change.

## Package Legitimacy Audit

No external packages are installed by this phase. Nothing to audit.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| (none) | - | - | - | - | - | - |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
 consumer builds caller-owned state (and clears target after Undo / Undo all, D-14)
   VoiceOutcomeUiState.Success(..., target, targetLabel, targetDetail)      [NEW fields, appended]
   BatchRowResultUiModel(label, succeeded, detail, target)                  [NEW field, appended]
        |
        v
 OutcomeSheet(outcome, onDismissRequest, modifier, style, colors[, sheetState], onOpenTarget = null)
   |  (both overloads; no-state overload forwards onOpenTarget into the hoisted one)
   |
   |- style == Receipt ---------> OutcomeSheetReceipt(..., onOpenTarget)  --> OutcomeSheetReceiptContent --> ReceiptSuccessBody
   |                                       title row, pill, [OpenTargetRow if target+handler], editable,
   |                                       [OpenTargetBatchList if handler && any row target ELSE BatchResultsList], undo, actions
   |
   |- Classic && handler && hasOpenTarget(outcome) ---> NEW Classic open path (appended branch, own helper)
   |                                       summary, HandledByRow, [OpenTargetRow], [OpenTargetBatchList|BatchResultsList],
   |                                       editable, undo   (all existing blocks reused, none edited)
   |
   `- everything else ----------> UNCHANGED v2.7.1: SheetScaffold { OutcomeSheetContent(outcome, colors = colors) }

 OpenTargetRow (internal, one file, both styles)
   Row[ text column (title, optional detail; wraps, weight 1) | chevron (fixed 24dp, never pushed off) ]
   merged node: semantics(mergeDescendants) + clickable(onClickLabel = openLabel.ifBlank{title}, role = Button)
   click -> onOpenTarget(target)   (library never hides the sheet, D-05)

 UndoCenterScreen (v2.7.1 overload, byte-unchanged)  <-- delegated to by -->
 UndoCenterScreen(..., groups, onUndoGroup, onOpenTarget: (FeedbackTarget)->Unit, onClear)   [NEW overload]
   CompositionLocalProvider(LocalUndoCenterOpenTarget provides onOpenTarget) { v2.7.1 overload }
     UndoHistoryRow: early branch iff local != null && entry.openableTarget != null
        -> one detectTapGestures owner: onPress(hold = peek) / onLongPress(show peek) / onTap(open) + hand-written semantics

 ReceiptHandledByPill
   background(surfaceContainerHighest, CappedPillShape)      [was CircleShape]   POL-01
   dot: handledByDotColor(...) unchanged; drawBehind ring only if contrast(fill, pill) < 3:1   POL-02
```

### Recommended Project Structure
```
src/main/java/io/github/ygaray/yahirandroidtaste/
|- model/VoiceOutcomeUiState.kt      # MOD: Success + target, targetLabel, targetDetail (appended)
|- model/BatchRowResultUiModel.kt    # MOD: + target (appended)
|- component/OutcomeSheet.kt         # MOD: onOpenTarget param (both overloads), appended Classic open branch + private Classic open body
|- component/OutcomeSheetReceipt.kt  # MOD: thread onOpenTarget; pill shape; dot ring
|- component/OpenTargetRow.kt        # NEW internal: OpenTargetRow, OpenTargetBatchList, hasOpenTarget()
|- component/HandledByPillShape.kt   # NEW internal: capped-radius Shape + pure cap math
|- component/HandledByDot.kt         # NEW internal: contrast ring decision (pure) + ring Modifier
|- feedback/UndoCenterScreen.kt      # MOD: new overload (appended), private CompositionLocal, early branch in UndoHistoryRow
|- theme/Dimens.kt                   # MOD (optional): ring width token under OutcomeReceipt
src/test/java/.../
|- component/OpenTargetRowTest.kt                      # semantics + click + fallback + chevron
|- component/OutcomeSheetOpenTargetTest.kt             # Classic + Receipt single row, hoisted overload, parity with no handler
|- component/OutcomeSheetBatchOpenTargetTest.kt        # mixed rows, per-row targets, no click action on target-less rows
|- component/OutcomeSheetOpenTargetLongLabelTest.kt    # NATIVE w360dp, DE strings, fontScale 2.0
|- component/HandledByPillShapeTest.kt                 # pure cap math + Robolectric geometry at 1.0 and 2.0
|- component/HandledByDotContrastTest.kt               # JVM, synthetic + hub + dynamic-sample schemes
|- feedback/UndoCenterScreenOpenTargetTest.kt          # open, not-openable cases, peek survives, stale-callback guard
|- feedback/UndoCenterScreenOpenParityTest.kt          # new overload, no openable rows == D1..D4 goldens
|- V271CallSiteCanaryTest.kt                           # EXTEND: OutcomeSheet / Success / BatchRowResultUiModel / UndoCenterScreen shapes
```
No new public top-level `@Composable`, so `ComponentRegistry` counts (67 / 6 / 73 after Phase 26 [CITED: 26-VALIDATION.md "Fresh full gate" / 26-05 plan]) and the drift guards are unaffected. The registry and `DomainVocabularyDriftGuardTest` key on function NAME, so an added overload of an existing name needs no registration [VERIFIED: explorer/ComponentRegistryDriftGuardTest.kt "scanned ... - registeredNames - allowlistedNames" operates on name sets].

### Pattern 1: Model fields (FOPEN-01/02), appended with defaults
**What:** `Success` gains `val target: FeedbackTarget? = null, val targetLabel: String? = null, val targetDetail: String? = null` after `endAction`; `BatchRowResultUiModel` gains `val target: FeedbackTarget? = null` after `detail`. KDoc states: the row title is `targetLabel ?: summary` (D-01 fallback), `targetDetail` is the optional second line, `target.openLabel` is the TalkBack click label and is never shown, and **the CALLER clears `target` after Undo / Undo all** (D-14). Pass `targetLabel` to avoid repeating the headline.
**Current shapes (verbatim):** [VERIFIED: model/VoiceOutcomeUiState.kt:55-66]
```
    data class Success(
        val summary: String,
        val handledBy: HandledByUiModel? = null,
        ...
        val startAction: SheetActionUiModel? = null,
        val endAction: SheetActionUiModel? = null
    ) : VoiceOutcomeUiState
```
and [VERIFIED: model/BatchRowResultUiModel.kt:14-18]
```
data class BatchRowResultUiModel(
    val label: String,
    val succeeded: Boolean,
    val detail: String? = null
)
```
**Rules:** append only; no `@Deprecated(HIDDEN)` shims, no hand-written `copy$default` (CLAUDE.md: v2.5.0 shims stay, add no more). `FeedbackTarget` is `@Immutable` and its `toString` omits the key [VERIFIED: feedback/FeedbackTarget.kt:19,21 `data class FeedbackTarget(val key: String, val openLabel: String) {` / `override fun toString(): String = "FeedbackTarget(openLabel=$openLabel)"`], so the new fields do not leak keys through `Success`'s generated `toString`. Regenerate `api.txt` with `./gradlew apiDump` in the same commit. `verify-api-source-additive.py` accepts appended defaulted params (Phase 25 precedent: 25-01 SUMMARY "required_param_changes=0 ... signature_changes=0") and only flags a param added after a trailing lambda (rule R7); neither `Success` nor `BatchRowResultUiModel` ends in a lambda.

### Pattern 2: One shared internal `OpenTargetRow` (both styles, single and batch)
**What:** `Row(fillMaxWidth)` with a weighted text column (title, optional detail, both wrapping, at most 2 lines each, ellipsis) and a trailing fixed chevron. Merged node: `semantics(mergeDescendants = true) {}` then `clickable(onClickLabel = target.openLabel.ifBlank { title }, role = Role.Button, onClick = ...)`, `heightIn(min = Dimens.TouchTarget)` (48dp) for clickable rows. No `stateDescription` (that is exactly what makes `ReceiptRow` wrong). Chevron: `Icons.AutoMirrored.Filled.KeyboardArrowRight`, `contentDescription = null`, tint `onSurfaceVariant`, `Modifier.size(Dimens.ReceiptRow.ChevronSize)`. Because the chevron is a sibling measured before the weighted column, a long German title wraps instead of pushing the chevron off (verify in the NATIVE test).
**Tags:** `outcome_sheet_target_row` (single row), keep `outcome_sheet_batch_row` on every batch row of the open list so row counting tests keep one inventory, `outcome_sheet_target_chevron` as a test-only hook. None of these are in `RECEIPT_ONLY_TAGS` or the stable Y-14 set, so existing inventories are unaffected [VERIFIED: OutcomeSheetClassicParityTest.kt:226-236 and OutcomeSheetReceiptTagsTest.kt:36-50 list the sets].
**Never put `target.key` in a testTag, contentDescription, semantics text or log.** Use `openLabel` only.
**Fallback title** when `targetLabel` is null is `summary` (D-01). Note this repeats the headline in Classic (summary heading, then the same text as the row title); document "pass `targetLabel`" in the KDoc rather than hiding the row.

### Pattern 3: Classic path through NEW functions only (SHA pins)
The pins are exact. What is pinned [VERIFIED: OutcomeSheetClassicParitySourceContractTest.kt:49-74]:
```
    fun `SuccessBody equals its v251 text`() =
        assertUnchangedSinceV251("SuccessBody", "819354de5ed3175715b7daa67acfe7ada3187876fdfc28891c9d7b8fee1c6866")
    ... "UndoRowItem" "5a56e1d9b11ff55b58d1a2b4aca28f66cfb29e384fffbd9419a42dfcf9f3abdb"
    ... "HandledByRow" "3539e2512048b1fd0049eef8b26e401510aa9f4f4b18cbc9af0baf533f44bb8d"
    ... "NeedsConfirmationBody" "2b5f0571a23bb7bda3d1f1be12264cb1d722af940eca3fb662cf77e0f6ed18ed"
    ... "BatchResultsList" "97e3fa2c0fcc31d7c02fa2f386fb07f4c6b9b66533c2658068c5554d537828c3"
    ... "ProposedItemRow" "9155aa36fc9e3835bc1c5083ab5b7c5185204168e459d9eab161d7a1a4f28cdd"
```
How the pin works [VERIFIED: :32-33, :39-45]: `body(name)` = `squash(functionBody(stripped, "fun $name("))`, i.e. comments stripped, all whitespace removed, extraction starts at `fun Name(` (the opening paren is part of the match). Consequences for planning:
1. A new function named `SuccessBodyOpenTarget` (anything but exactly `SuccessBody`, `HandledByRow`, ...) never collides, because the search string includes `(`.
2. These six functions must keep their bodies **and** their visibility: [VERIFIED: :182-197] `UndoAffordanceBody(`, `BatchResultsList(`, `ProposedItemRow(` must stay `internalfun`, and `FailureBody(`, `SuccessBody(`, `HandledByRow(`, `NeedsConfirmationBody(`, `NeedsConfirmationActions(`, `UndoRowItem(` must stay `privatefun`. `HandledByRow` is private, so the new Classic body that reuses it must live in `OutcomeSheet.kt` (append at the end of the file; a different file could not call it).
3. `OutcomeSheetContent` is pinned only by fragments: the weighted/scroll fragment, the footer `when`, the `Failure` arm and the padded undo block [VERIFIED: :78-105]. Its Success arm line `is VoiceOutcomeUiState.Success -> SuccessBody(outcome)` is not pinned, but there is no need to touch it.
4. The dispatch literals [VERIFIED: :201-210]:
```
        assertTrue(
            stripped.contains(
                "SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState) {"
            )
        )
        assertTrue(stripped.contains("OutcomeSheetContent(outcome, colors = colors)"))
```
   The call `OutcomeSheetContent(outcome, colors = colors)` must therefore remain EXACTLY as is, with no appended argument. So the open path cannot be threaded by adding `onOpenTarget =` to that call; it must be a separate branch that does not reach that line.
5. `OutcomeSheetReceiptSourceContractTest` constrains where such a branch may sit [VERIFIED: OutcomeSheetReceiptSourceContractTest.kt:27-55]: exactly two `fun OutcomeSheet(`; in the first body `if(style==OutcomeSheetStyle.Receipt){` precedes the first `OutcomeSheetReceipt(`, which precedes the **first** `return`, which precedes the legacy `SheetScaffold(onDismissRequest=onDismissRequest,modifier=modifier,sheetState=sheetState){`; and that legacy line occurs exactly once in the file. So: (a) do not add any `return` (or text `OutcomeSheetReceipt(`) before the Receipt guard; (b) do not write a second `SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState)` in `OutcomeSheet.kt` (put the open path's scaffold in a helper in another file or give it different arguments); (c) the second overload body must still contain `sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)` and no `SheetScaffold(` [VERIFIED: :27-35].
**Prescribed shape for Classic** (inserted AFTER the Receipt `return` block and BEFORE the legacy scaffold, all lines pure additions):
```kotlin
    if (onOpenTarget != null && outcome.hasOpenTarget()) {
        OutcomeSheetClassicOpenTarget(outcome, onDismissRequest, modifier, colors, sheetState, onOpenTarget)
        return
    }
```
where `OutcomeSheetClassicOpenTarget` (internal, in `OpenTargetRow.kt` or `OutcomeSheet.kt`) owns its own `SheetScaffold` and renders only the Success arm through the new private Classic body; every non-Success arm is excluded by `hasOpenTarget()` (it is `this is Success && (target != null || batchResults.any { it.target != null })`). With `onOpenTarget == null`, or no target anywhere, control falls to the untouched legacy lines: that is the ROADMAP "same code path" proof, and the legacy scaffold line count stays 1.
**New Classic body** (private in `OutcomeSheet.kt`, appended): same column as `SuccessBody` plus the open row: summary `Text` (headlineSmall), `HandledByRow` if any, `OpenTargetRow` when `target != null` (D-02: after the handled-by indicator), then the batch list switch, editable slot (hidden while `inFlight`), `UndoAffordanceBody`. It reuses the pinned functions by calling them; it does not copy their text.

### Pattern 4: Receipt path by threading an optional parameter
`OutcomeSheetReceipt.kt` has no SHA pins, only `contains`/order assertions, and they tolerate an inserted row [VERIFIED: OutcomeSheetReceiptSourceContractTest.kt:127-146 requires `ReceiptTitleRow(`, `ReceiptHandledByPill(`, `success.editableContent`, `BatchResultsList(`, `UndoAffordanceBody(`, `ReceiptSuccessActions(` present in that sorted-index order and `UndoAffordanceBody(it,locked=success.inFlight)`]). Thread `onOpenTarget: ((FeedbackTarget) -> Unit)? = null` as the LAST parameter of `OutcomeSheetReceipt`, `OutcomeSheetReceiptContent` and `ReceiptSuccessBody`. In `ReceiptSuccessBody` insert `ReceiptOpenTargetRow` between the pill and the editable slot, and replace the single batch line by an if/else that still contains the literal `BatchResultsList(`:
```
    if (onOpenTarget != null && success.batchResults.any { it.target != null }) OpenTargetBatchList(rows, onOpenTarget)
    else success.batchResults.takeIf { it.isNotEmpty() }?.let { BatchResultsList(it) }
```
This keeps the existing test text anchors and the D-08 order. `OutcomeSheetReceiptContent(outcome)` is hosted directly by many tests with one argument [VERIFIED: OutcomeSheetReceiptSuccessTest.kt host() `OutcomeSheetReceiptContent(outcome)`], so the new parameter MUST be defaulted. In `OutcomeSheet` the one line `OutcomeSheetReceipt(outcome, onDismissRequest, modifier, colors, sheetState)` [VERIFIED: OutcomeSheet.kt:96] needs `, onOpenTarget` appended: a line rewrite (lane 2, see Pitfall 1). Alternative if the team prefers zero Receipt rewrites: a parallel `ReceiptSuccessBodyOpenTarget` + content shell (about 25 duplicated lines, lane 1, but two copies of the section order to keep in sync). Recommendation: thread; it is smaller and the order stays in one place.

### Pattern 5: Both `OutcomeSheet` overloads
Append `onOpenTarget: ((FeedbackTarget) -> Unit)? = null` LAST on both. Neither ends in a lambda today: the hoisted one ends in `sheetState: SheetState` [VERIFIED: OutcomeSheet.kt:93], the other in `colors: OutcomeSheetColors = OutcomeSheetDefaults.colors()` [VERIFIED: :124]. `FeedbackTarget` is not experimental, so a plain defaulted param is right (the `sheetState` overload exists only because `SheetState` is `@ExperimentalMaterial3Api`; do not mirror that here). The no-state overload forwards `onOpenTarget = onOpenTarget` into the hoisted call. `verify-additive-diff.sh` compares removed lines modulo trailing continuation punctuation, so adding the comma to the previous parameter line is not a rewrite [VERIFIED: tools/verify-additive-diff.sh header "identical (modulo trailing continuation punctuation) added counterpart"]. KDoc must say: the library does not hide the sheet; to slide it down first call `hide()` on a hoisted `sheetState`, then navigate (D-05; STYLE section 4 "slide down on every close").

### Pattern 6: `UndoCenterScreen` second overload and the row (FOPEN-03)
**Current v2.7.1 signature** [VERIFIED: feedback/UndoCenterScreen.kt:78-87]:
```
fun UndoCenterScreen(
    entries: List<UndoHistoryEntry>,
    onNavigateBack: () -> Unit,
    onUndo: (String) -> Unit,
    groups: List<UndoGroupRowUiModel> = emptyList(),
    onUndoGroup: ((String) -> Unit)? = null,
    onClear: () -> Unit
) {
```
**New overload:** `(entries, onNavigateBack, onUndo, groups = emptyList(), onUndoGroup = null, onOpenTarget: (FeedbackTarget) -> Unit, onClear: () -> Unit)`. `onOpenTarget` MUST be required and non-null: a defaulted one would make `UndoCenterScreen(e, b, u) { }` applicable to both overloads (overload ambiguity, a compile error for every v2.7.1 trailing-lambda call). Required, it is applicable only to calls that name it, so every v2.7.1 shape in `UndoCenterScreenSourceCompatV251Test` still binds to the old overload [VERIFIED: that file's four shapes: all-named, trailing-lambda, groups all-named, groups + trailing lambda]. Callers must name `onOpenTarget` (it follows defaulted params, the same idiom `sheetState` uses). detekt `LongParameterList` is `functionThreshold: 6` with `ignoreDefaultParameters: true` in the base config and 18 in the compose config [VERIFIED: config/detekt/detekt.yml:30-34, config/detekt-compose.yml:29-33]; the new overload has 5 non-default params.
**Implementation (recommended, lane 1):** the new overload wraps the old one:
```kotlin
CompositionLocalProvider(LocalUndoCenterOpenTarget provides onOpenTarget) {
    UndoCenterScreen(entries = entries, onNavigateBack = onNavigateBack, onUndo = onUndo,
        groups = groups, onUndoGroup = onUndoGroup, onClear = onClear)
}
```
with `private val LocalUndoCenterOpenTarget = compositionLocalOf<((FeedbackTarget) -> Unit)?> { null }`. The v2.7.1 body, the Clear history rule and the group header stay byte-identical, so `UndoCenterScreenDefaultParityTest` (D1..D4 unmerged-tree goldens, "never edited afterwards" [VERIFIED: its KDoc]) and the existing source contract stay green. In `UndoHistoryRow` insert an early branch at the top of the body: `val open = LocalUndoCenterOpenTarget.current; val target = entry.openableTarget; if (open != null && target != null) { UndoHistoryOpenRow(...); return }`. Alternative (lane 2): extract the Scaffold body into an internal `UndoCenterScreenContent` shared by both overloads; then `UndoCenterScreenSourceContractTest."the Clear history rule still reads the entries only"` (which reads the FIRST `fun UndoCenterScreen(` body [VERIFIED: UndoCenterScreenSourceContractTest.kt:58-62]) must be re-pointed. Prefer the wrapper.
**Contract edit that D-04 authorizes:** `there is exactly one public UndoCenterScreen function` asserts `countOccurrences(source, "fun UndoCenterScreen(") == 1` [VERIFIED: UndoCenterScreenSourceContractTest.kt:33-40]; change it to 2 and add an assertion for the second overload's parameter list. Keep the v2.7.1 overload FIRST in the file (the parameter-list test reads the first occurrence [VERIFIED: :23-31]). Say in the commit that this edit is deliberate (D-04).
**The open row** (`UndoHistoryOpenRow`, private, appended): same `Box` + testTag `"$UNDO_ROW_TEST_TAG_PREFIX${entry.id}"` and `ListItem` as the v2.7.1 row, with these differences only: (1) `semantics(mergeDescendants = true) { role = Role.Button; onClick(label = target.openLabel.ifBlank { entry.message }) { open(target); true } }`; (2) `trailingContent` = Undo `TextButton` plus the chevron (an Available openable row always has Undo); (3) gesture block below. Rows without an openable target never reach this branch, so Undone/Failed rows, targetless rows and group headers keep the exact v2.7.1 tree.
**Gesture block (reconciles D-04 with PITFALLS 10):** one `pointerInput` owner, no `clickable` on the row (a `clickable` consumes the down in the Main pass and kills hold-to-peek [CITED: .planning/research/PITFALLS.md section 10]). Use `detectTapGestures(onPress = { tryAwaitRelease(); showPeek = false }, onLongPress = { showPeek = true }, onTap = { latestOpen(target) })`. D-04's "no-op onLongPress" must not be read as "no peek": the pitfalls checklist requires "hold-to-peek still works on rows with a target" [CITED: PITFALLS.md line 128]. Showing the peek from `onLongPress` (not from `onPress`) also avoids a peek flash on every tap, which the v2.7.1 `onPress = { showPeek = true; ...}` would cause if `onTap` were merely added. After a long press `detectTapGestures` does not call `onTap` on release, so a hold never opens (same mechanism `FeedbackCard` relies on with `onLongPress = { }` [VERIFIED: FeedbackCard.kt:197-199] and its green "Long hold then release does not open" test [CITED: 26-VALIDATION.md]). The nested Undo `TextButton` consumes its own down, so tapping Undo neither opens nor peeks (the v2.7.1 A1 behaviour [VERIFIED: UndoCenterScreen.kt:302-307 comment]).

### Pattern 7: POL-01 capped-radius pill shape
Today: `.background(scheme.surfaceContainerHighest, CircleShape)` [VERIFIED: OutcomeSheetReceipt.kt:286]. `CircleShape` is `RoundedCornerShape(50%)`, so a 4-line pill at font scale 2.0 gets a radius of half its height (tens of dp) and the arc cuts past the 12dp horizontal padding, which is the observed overrun.
Prescription: an internal `Shape` (data-class-like equality so `background` does not invalidate) whose `createOutline` returns `Outline.Rounded(RoundRect(size.toRect(), CornerRadius(min(size.height / 2f, cap))))`, with `cap = (bodySmall.lineHeight.toPx() + 2 * PillVerticalPadding.toPx()) / 2` computed from the `Density` passed to `createOutline`. Rationale: the cap equals the half-height of a ONE-line pill at the current font scale, so (a) a one-line pill at any scale is exactly the old capsule (`min` returns `height/2`), and (b) a wrapped pill reuses the corner geometry of the one-line pill, whose text is known to fit (arithmetic, not measured: a radius r pulls the arc in by r - sqrt(r^2 - (r - 4dp)^2) at the 4dp vertical inset, about 3dp at r = 12dp (scale 1.0) and 8dp at r = 20dp (scale 2.0), both under the 12dp horizontal padding [ASSUMED: confirmed by the Wave 0 geometry test]). Dimensions in play [VERIFIED: theme/Dimens.kt OutcomeReceipt]: `PillVerticalPadding = 4.dp`, `PillHorizontalPadding = 12.dp`, `DotSize = 6.dp`, `DotGap = 6.dp`. Classic `HandledByRow` is untouched (D-06). The pill's contract test must keep passing: exactly one `Text(` call in the pill body, `weight(1f,fill=false)`, the `"outcome_sheet_handled_by"` tag and `semantics(mergeDescendants=true)` [VERIFIED: OutcomeSheetReceiptSourceContractTest.kt:116-125].

### Pattern 8: POL-02 contrast ring
`handledByDotColor` stays exactly as is (a test asserts it returns `colors.warnTone.content` on device and `scheme.primary` otherwise [VERIFIED: OutcomeSheetReceiptSuccessTest.kt:202-212]). Add a pure `internal fun handledByDotRing(fill: Color, pill: Color, scheme: ColorScheme): Color?` that returns `null` when `contrast(fill, pill) >= 3f` and otherwise the first of `scheme.onSurface`, `contrastingForeground(pill)` whose contrast vs `pill` is `>= 3f` (the black/white fallback always reaches at least 4.5:1, so the function is total). `pill` is `scheme.surfaceContainerHighest`, opaque in every M3 scheme. Draw the ring OUTSIDE the 6dp footprint (`Modifier.drawBehind { drawCircle(ring, radius = size.minDimension / 2f + ringWidth.toPx()) }` before the existing `.background(fill, CircleShape)`), so the node size, tag and `DimensTest` `DotSize == 6.dp` [VERIFIED: theme/DimensTest.kt:130] are unchanged and the non-ring path is pixel-identical. An inside `Modifier.border` would shrink the fill to 4dp; avoid it. Hub numbers (informational): light `onTertiaryContainer #FF251A00` on pill `#FFDCE3E5`, dark `#FFFFE0A0` on `#FF213836` [VERIFIED: theme/Color.kt:29,33,64,86], both far above 3:1, so the hub default never gets a ring.

### Anti-Patterns to Avoid
- **Editing a SHA-pinned function** (`SuccessBody`, `HandledByRow`, `BatchResultsList`, `UndoRowItem`, `NeedsConfirmationBody`, `ProposedItemRow`) or changing its visibility, "just to add a parameter".
- **Appending an argument to `OutcomeSheetContent(outcome, colors = colors)`** or adding a `return` before the Receipt guard.
- **Using `ReceiptRow`** for the go-to-item row.
- **`Modifier.clickable` on the Undo Center row** (kills hold-to-peek) or `onPress = { showPeek = true }` kept together with `onTap` on an openable row (peek flashes on every tap).
- **Capturing `onOpen` / `target` in `pointerInput(entry.id)` without `rememberUpdatedState`** (Pitfall 5).
- **Unconditional wrapper nodes** (`semantics(mergeDescendants = true)`, `.then(...)`) added to the default path "for the clickable case": they change the v2.7.1 tree and break parity.
- **Changing `ToneDefaults.warn()`** or `handledByDotColor`.
- **Putting `target.key` anywhere user-visible or loggable.**

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Sheet row tap + a11y | Custom `pointerInput` on the sheet row | `Modifier.clickable(onClickLabel, role = Role.Button)` after `semantics(mergeDescendants = true)` | Gives ripple, 48dp, focus, key activation; nothing else owns that node's pointer |
| Undo Center row semantics click | `clickable` | hand-written `semantics { role; onClick(label) }` + the existing `detectTapGestures` owner | `detectTapGestures` has no semantics, `clickable` would starve the peek |
| WCAG ratio | A new color library | `Color.luminance()` + `(L1 + 0.05) / (L2 + 0.05)` as in `ContrastTestSupport.kt:10` | Same formula the tone tests use |
| Pill radius | `onTextLayout` + `lineCount` state | Capped-radius `Shape` | No extra frame, no flicker |
| Chevron | New icon asset | `Icons.AutoMirrored.Filled.KeyboardArrowRight` | RTL flips for free; same icon as the deck card |
| Source-contract extraction in tests | New string scanners | `SourceContractTestSupport.functionBody / stripComments / countOccurrences` | Already the repo's contract tooling |
| Semantics tree comparison | Pixel capture (times out under Robolectric here) | `SemanticsDump.normalizedTree/allRoots` + tag/text inventories | The repo's established parity proof [VERIFIED: testsupport/SemanticsDump.kt KDoc "no pixel capture"] |

**Key insight:** the phase is wiring plus two tiny pure functions (shape cap, contrast ring). Everything risky is about not disturbing pinned code, so prefer new functions and appended lines, and prove "unchanged" with the existing contract and golden tests rather than new ones.

## Common Pitfalls

### Pitfall 1: Lane-2 classification on line rewrites
**What goes wrong:** the pre-commit hook blocks a commit that rewrites a line of a file tracked in HEAD ("lane 2 ... on the fast path").
**Why:** `classify-hub-change.sh` calls `verify-additive-diff.sh` against the staged index vs HEAD; any removed line without an identical added counterpart (modulo trailing continuation punctuation) is lane 2 [VERIFIED: tools/classify-hub-change.sh "lane 2 (exit 2): a pre-existing src/main line was rewritten/removed"; tools/hooks/pre-commit blocks unless `HUB_LANE_OVERRIDE=2`].
**How to avoid / handle:** appends and added-comma continuation lines are lane 1. Unavoidable rewrites in this phase: the `OutcomeSheetReceipt(...)` call line, `OutcomeSheetReceiptContent(outcome, colors = colors)`, the `ReceiptSuccessBody(...)` arm and signature, the `ReceiptSuccessBody` batch line, and the pill `.background(...)` line. Commit those with `HUB_LANE_OVERRIDE=2` and a body line such as "forwards onOpenTarget / swaps pill shape; default call path unchanged, no v2.7.1 signature removed". The v2.7.1 baseline tag is the latest `v*` tag [VERIFIED: `git describe --tags --abbrev=0 --match 'v*'` printed `v2.7.1`].

### Pitfall 2: Exact-line and order contracts in `OutcomeSheet.kt`
See Pattern 3 items 4 and 5. A second `SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState)` or an early `return` breaks `OutcomeSheetReceiptSourceContractTest`. Put the open branch after the Receipt `return` and keep its scaffold in a helper.

### Pitfall 3: Reusing a pinned private function from another file
`HandledByRow` is `private fun` and must stay private [VERIFIED: ParitySourceContractTest.kt:187-196]. A new Classic body in a new file cannot call it. Keep the new Classic body in `OutcomeSheet.kt`.

### Pitfall 4: Hold-to-peek and the tap flash on Undo Center rows
`clickable` kills the peek; `onTap` added next to the existing `onPress = { showPeek = true; ... }` flashes the peek on every tap [CITED: PITFALLS.md section 10]. Use `onLongPress` to show the peek and `onPress` only to hide it on release (Pattern 6). Test both: hold shows the peek and releasing does not open or undo; a tap opens once with no peek node ever composed.

### Pitfall 5: Stale open callback after Undo
The v2.7.1 row uses `pointerInput(entry.id)` [VERIFIED: UndoCenterScreen.kt:301]. The entry keeps its id when its status flips to Undone (`withStatus` copies the entry), so a lambda captured at first composition would keep opening an Undone row's old target. In the open row, read `target` and `open` through `rememberUpdatedState`, or key `pointerInput` on `(entry.id, target)`; and the row leaves the open branch anyway when `openableTarget` turns null. Test: undo via the store, then tap the row: `onOpenTarget` count stays 0.

### Pitfall 6: Overload ambiguity and call-site breaks in `UndoCenterScreen`
A defaulted `onOpenTarget` makes `UndoCenterScreen(e, b, u) { }` ambiguous. Keep it required and non-null (Pattern 6). Extend the canary with a call that names `onOpenTarget` plus a trailing `onClear`.

### Pitfall 7: Pill geometry assumptions
(a) The `outcome_sheet_handled_by` tag sits AFTER the inner padding modifier [VERIFIED: OutcomeSheetReceipt.kt:284-292 order: `padding(top)`, `background`, `padding(vertical, horizontal)`, `semantics`, `testTag`], so the tagged node's bounds are the CONTENT area; the painted pill is that rect inflated by `PillHorizontalPadding` and `PillVerticalPadding`. A "text inside shape" test must add those paddings back. (b) Compose's default line-height trimming can make a one-line text shorter than its `lineHeight` (the ReceiptRow author documents this [VERIFIED: ReceiptRow.kt `receiptRowLine` KDoc]); because the cap is computed from `lineHeight` and the actual one-line height is less than or equal to it, `min(height/2, cap)` stays `height/2` at one line, but this is ASSUMED until the Wave 0 test measures it. (c) Prove containment with a control: the same wrapped pill under `CircleShape` must FAIL the corner-containment check (red first), so the test is not vacuous. (d) The dot is vertically centered, so only the text lines threaten the top/bottom arcs.

### Pitfall 8: Long German in the target rows
English defaults hide overflow (STYLE section 7: "English defaults hide overflow, and accessibility dumps show text the screen truncates, so only screenshots count"). Test title and detail wrapping, chevron fully inside the window, in German at w360dp and at font scale 2.0 with NATIVE graphics, plus Gate-1 screenshots.

### Pitfall 9: Opening an in-flight or already-undone item
`Success.inFlight` locks undo controls and hides the editable slot. Nothing in the decisions says whether the target row stays tappable during a batch write, and after Undo the caller must clear `target` (D-14). Library behavior for in-flight is an Open Question (below); whatever is chosen, state it in the KDoc.

### Pitfall 10: Double open
`clickable` does not debounce; a double tap calls `onOpenTarget` twice. The deck gets exactly-once from a CAS in `FeedbackStackState`; sheet rows are stateless (D-05 callback only). Document that the handler must be idempotent (navigating to the same route twice) rather than adding library state. [ASSUMED: acceptable; confirm in Open Questions]

### Pitfall 11: Registry, vocabulary and docs guards
No new public `@Composable`, so no registry change. `LibraryInvariantsGuardTest` forbids domain nouns in the Explorer copy file `ReleaseDemos.kt` and consumer identifiers in comment-stripped code [VERIFIED: explorer/LibraryInvariantsGuardTest.kt KDoc list]; do not copy the mock's sample item names into code or `ReleaseDemos.kt`. Phase 28 owns the go-to-item demos (DEMO-03) and Phase 29 the docs (SHIP-05): this phase needs KDoc only, plus a short `API.md` row if cheap.

### Pitfall 12: Worktree runs hide directory-name guards
Drift guards that walk up to a directory literally named `yahirandroidtaste` pass vacuously in a differently named worktree [CITED: 25-01 SUMMARY Notes]. Re-run the guard suite in the main checkout at phase end.

## Code Examples

Proposed (not shipped) skeletons. Every API call below was seen in the repo this session (anchors listed after).

### Shared row
```kotlin
// New file component/OpenTargetRow.kt (internal). Proposed.
@Composable
internal fun OpenTargetRow(
    title: String,
    detail: String?,
    target: FeedbackTarget,
    onOpenTarget: (FeedbackTarget) -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.TouchTarget)
            .testTag("outcome_sheet_target_row")
            .semantics(mergeDescendants = true) {}
            .clickable(
                onClickLabel = target.openLabel.ifBlank { title },
                role = Role.Button,
                onClick = { onOpenTarget(target) }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ReceiptRow.ColumnGap)
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = titleColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.ReceiptRow.ChevronSize).testTag("outcome_sheet_target_chevron"))
    }
}
```
Anchors: `clickable(onClickLabel = ..., role = Role.Button, ...)` and `semantics(mergeDescendants = true)` order from ReceiptRow.kt `ReceiptRowSummary`; chevron lines from FeedbackCard.kt:347-351; `Dimens.TouchTarget` is `val TouchTarget: Dp = 48.dp` [VERIFIED: theme/Dimens.kt:70]; `Dimens.ReceiptRow.ColumnGap` is 8.dp [VERIFIED: Dimens.kt:259-260]. The `titleMedium`/`bodyMedium` choice mirrors `ReceiptRowTitleColumn` and the mock (16sp semibold title, 13sp muted detail) [VERIFIED: docs/design/snackbar-go-to-item/index.html `.rrow .n b{...font-size:16px;font-weight:600}` / `.rrow .n span{font-size:13px;color:var(--muted)}`]; exact styling is Claude's discretion.

### Batch list, Classic and Receipt
```kotlin
// Rows keep the v2.7.1 text rules: label onSurface when succeeded else error; detail only when !succeeded.
// Every row uses the same anatomy (D-03); only rows with a target get clickable + chevron + 48dp minimum.
@Composable
internal fun OpenTargetBatchList(rows: List<BatchRowResultUiModel>, onOpenTarget: (FeedbackTarget) -> Unit) { /* ... */ }
```
v2.7.1 text rules [VERIFIED: OutcomeSheet.kt:334-352]: `color = if (row.succeeded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error`, and `if (!row.succeeded && row.detail != null)` renders the detail in `labelSmall` error color. Keep `Modifier.testTag("outcome_sheet_batch_row")` on each row.

### Ring decision (pure, JVM-testable)
```kotlin
internal fun handledByDotRing(fill: Color, pill: Color, scheme: ColorScheme): Color? {
    if (contrast(fill, pill) >= 3f) return null
    return listOf(scheme.onSurface, contrastingForeground(pill)).first { contrast(it, pill) >= 3f }
}
```
(`contrast` = the WCAG formula; define internal in main since the test copy is test-only.) `.first` is total because black or white against any opaque pill reaches at least 4.5:1.

### Shape
```kotlin
internal data class HandledByPillShape(val oneLineContentHeight: TextUnit, val verticalPadding: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cap = with(density) { (oneLineContentHeight.toPx() + 2 * verticalPadding.toPx()) / 2f }
        return Outline.Rounded(RoundRect(size.toRect(), CornerRadius(minOf(size.height / 2f, cap))))
    }
}
```
(API shape of `Shape.createOutline(size, layoutDirection, density)`, `Outline.Rounded`, `RoundRect`, `CornerRadius` is standard Compose [ASSUMED: from training knowledge; Wave 0 compile confirms].)

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Sheet rows with no way to open the item | Label-less chevron row, opaque `FeedbackTarget` | v2.8 (this phase) | One app-level `onOpenTarget` serves snackbars, sheets and the Undo Center |
| `CircleShape` pill | Capped-radius shape | v2.8 | Wrapped pills at font scale 2.0 stay inside their shape |

**Deprecated/outdated:** none relevant. No new `@Deprecated(HIDDEN)` overloads or `copy$default` shims (CLAUDE.md).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Cap = one-line pill half-height from `bodySmall.lineHeight + 2 * 4dp`, `min` with `height/2`, keeps a one-line pill pixel-identical to `CircleShape` at 1.0 (and a wrapped pill's text inside the arc) | Pattern 7, Pitfall 7 | Visible 1px radius drift at 1.0 or residual clipping at 2.0; the Wave 0 NATIVE tests decide, adjust cap by a pixel margin |
| A2 | `Shape.createOutline` / `Outline.Rounded` / `RoundRect` / `CornerRadius` API as sketched | Code Examples | Compile fix only |
| A3 | `detectTapGestures` does not call `onTap` after `onLongPress` fires, and `onPress`'s `tryAwaitRelease` returns on release after a long press | Pattern 6 | Hold would open the item; mitigated by an explicit test (and by FeedbackCard's green analogue) |
| A4 | The originally observed "near-invisible" warn dot under Material You is not reproduced by the hub default `ToneDefaults.warn()` (dynamic `onTertiaryContainer` is a dark tone on a light pill); the failing pair arises with caller-passed `warnTone` or other schemes. The ring is generic, so the fix does not depend on the exact cause | Pattern 8 | Fix still correct; only the test fixtures' realism is affected. Gate-1 screenshots under dynamic color on the TESTER add evidence |
| A5 | No library debounce for double taps on sheet rows; handler must be idempotent | Pitfall 10 | Duplicate navigation in a consumer that is not idempotent |
| A6 | `compositionLocalOf` provider overload is acceptable to the owner for the Undo Center plumbing (vs a shared content refactor) | Pattern 6 | Choose the lane-2 refactor instead; no functional difference |
| A7 | A visual rhythm of identical row anatomy in the open batch list, with the 48dp minimum only on clickable rows | Pattern 2 | Designer may want uniform heights; Claude's-discretion layout detail |

## Open Questions

1. **Should the target row be tappable while `Success.inFlight` is true?**
   - What we know: `inFlight` locks undo controls, hides `editableContent`, disables non-exempt actions; the item of a still-running create may not exist yet.
   - What's unclear: no decision covers the target row.
   - Recommendation: follow the CR-02 locked-row pattern: dim to the disabled alpha and drop the click action while `inFlight`. Needs confirmation (ui-designer / owner). If unconfirmed, ship tappable and document.

2. **Reconcile D-04 "no-op onLongPress" with hold-to-peek.**
   - What we know: the pitfalls checklist requires peek to survive on openable rows; D-04 says no-op `onLongPress`.
   - Recommendation: no-op for the open action (a hold never opens) but `onLongPress` shows the peek (Pattern 6). Confirm this reading with the orchestrator; it is the only reading that keeps both D-04 and the checklist true.

3. **Which version of `UndoCenterScreen` plumbing?** Wrapper + CompositionLocal (lane 1, recommended) or shared internal content (lane 2). Planner's call; both satisfy FOPEN-03.

4. **Does the Undo Center chevron sit after Undo or before it?** The deck card places Undo then chevron. Recommendation: same order (Undo, chevron). Confirm at Gate-1 with a German 360dp screenshot.

5. **Does `API.md` get a row in this phase or only in Phase 29?** Phase 26 updated the API.md row in its closing plan; SHIP-05 (Phase 29) owns the full docs. Recommendation: KDoc now, one `API.md` line per widened signature only if the docs-count guard needs nothing else; otherwise defer to 29.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | Gradle build | yes | openjdk 17.0.20.1 | - |
| Gradle wrapper | all tests/gates | yes (not probed by running a build) | `./gradlew` in repo; JitPack uses `./gradlew publishReleasePublicationToMavenLocal` | - |
| adb | Gate-1 on the TESTER | yes | `/usr/bin/adb` | - |
| TESTER device (USB serial `R5CT10XNKQN`) | Gate-1 (font scale 2.0, dynamic color, row taps) | yes (`adb devices` lists it as `device`) | SM-S908U | If a lease or the device is unavailable, park the device checks for Yahir, never waive |
| Second adb entry `100.118.21.106:1496` | - | present | - | **Never address it** (wireless; not the tester). Always `adb -s R5CT10XNKQN` |
| Running Gradle daemons | shared host hygiene | none running at probe time | - | Use `--no-daemon`, never `./gradlew --stop` |

**Missing dependencies with no fallback:** none.
**Missing dependencies with fallback:** none.

## Validation Architecture

> `workflow.nyquist_validation` is `true` in `.planning/config.json`.

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 + Robolectric 4.16.1 + `createComposeRule` (ui-test 1.11.0); kotlinx-coroutines-test 1.11.0 for virtual time (not needed here) |
| Config file | none beyond `build.gradle.kts`; detekt: `config/detekt/detekt.yml` + `config/detekt-compose.yml`, zero baseline |
| Quick run command | `./gradlew testDebugUnitTest --tests '*OpenTarget*' --tests '*HandledByPill*' --tests '*HandledByDot*' --tests '*OutcomeSheet*' --tests '*UndoCenterScreen*' --no-daemon` |
| Guard run command | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --tests '*DomainVocabularyDriftGuardTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*ComponentStatesMatrixTest' --tests '*ComponentPlaygroundIntegrityTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` [CITED: 26-VALIDATION.md] |
| Full suite command | `./gradlew apiDump testDebugUnitTest detekt apiCheck --rerun-tasks --no-daemon`, then `git diff api.txt` (expect additions only), `bash tools/verify-api-source-additive.sh v2.7.1` (expect `API SOURCE-ADDITIVE OK`), `bash tools/test/run-all.sh` (never in parallel with Gradle). Baseline before this phase: 1613 tests, 22 pre-existing skips, `run-all.sh` PASS=52 FAIL=0 [CITED: 26-VALIDATION.md "Validation Audit"] |
| Rules | exactly one Gradle invocation at a time, always `--no-daemon`, never `--offline`, never `--stop`; force a fresh run for gate evidence (up-to-date can be another build's cache) |

### Phase Requirements to Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|--------------|
| FOPEN-01 | Classic + Receipt: row shows title (`targetLabel ?: summary`), detail, chevron; tap calls `onOpenTarget(target)` once; merged `Role.Button` with `OnClick` label = `openLabel`; blank `openLabel` falls back to title; no `stateDescription` | Robolectric | `--tests '*OpenTargetRowTest' --tests '*OutcomeSheetOpenTargetTest'` | Wave 0 (new) |
| FOPEN-01 | Hoisted-`sheetState` overload takes the same path in both styles | Robolectric | `--tests '*OutcomeSheetOpenTargetTest'` (host like `OutcomeSheetHoistedStateTest.hostHoisted`) | Wave 0 (new) |
| FOPEN-01 | No `onOpenTarget` (target set) and no target (handler set): tag and text inventory equal the v2.7.1 render; existing tests pass unedited | Robolectric parity | `--tests '*OutcomeSheetClassicParityTest' --tests '*OutcomeSheetReceipt*' --tests '*OutcomeSheetTest'` + new inventory-equality case | ✅ existing + Wave 0 case |
| FOPEN-01 | Classic SHA pins, dispatch literal, scaffold-line count, Receipt order tests stay green unedited | source contract | `--tests '*OutcomeSheetClassicParitySourceContractTest' --tests '*OutcomeSheetReceiptSourceContractTest'` | ✅ |
| FOPEN-01 | Source-compat of v2.7.1 shapes (`OutcomeSheet` positional/named, `Success(...)` 10 positional args, `copy`, destructuring; `BatchRowResultUiModel(label, succeeded, detail)`) | compile canary | `--tests '*V271CallSiteCanaryTest'` | ✅ extend |
| FOPEN-02 | Mixed batch: targeted rows each call their own target; target-less rows have NO `SemanticsActions.OnClick`, no chevron tag, same anatomy; all-null targets or no handler renders `BatchResultsList` (tag count and text inventory unchanged) | Robolectric | `--tests '*OutcomeSheetBatchOpenTargetTest'` | Wave 0 (new) |
| FOPEN-03 | Available + target opens on tap; Undone, Failed, no-target rows and group headers do not; Undo button tap neither opens nor peeks; hold shows the peek and release neither opens nor undoes; after Undo via the store a tap does not open (stale-callback guard) | Robolectric | `--tests '*UndoCenterScreenOpenTargetTest'` | Wave 0 (new) |
| FOPEN-03 | New overload with no openable rows has the D1..D4 unmerged trees and row bounds; old overload unchanged | Robolectric golden | `--tests '*UndoCenterScreenDefaultParityTest' --tests '*UndoCenterScreenOpenParityTest'` | ✅ existing + Wave 0 (reuse `UndoCenterScreenGoldens`) |
| FOPEN-03 | Source contract updated deliberately: two `fun UndoCenterScreen(`, first param list unchanged, second param list asserted; all v2.5.1/v2.6 call shapes compile | source contract + canary | `--tests '*UndoCenterScreenSourceContractTest' --tests '*UndoCenterScreenSourceCompatV251Test'` | ✅ edit (count 1 to 2, authorized by D-04) |
| FOPEN-01/02/03 | Long German strings at w360dp and font scale 2.0 (NATIVE): title/detail wrap to at most 2 lines, chevron fully inside the window, Undo + chevron fit on an Undo Center row | Robolectric NATIVE | `--tests '*OutcomeSheetOpenTargetLongLabelTest' --tests '*UndoCenterScreenOpenTargetLongLabelTest'` | Wave 0 (new) |
| POL-01 | Shape math: radius = `min(height/2, cap)`; equals `CircleShape` radius for a one-line size at fontScale 1.0, 1.5, 2.0 | JVM unit | `--tests '*HandledByPillShapeTest'` | Wave 0 (new) |
| POL-01 | At fontScale 2.0, w360dp, long German pill wraps to more than 1 line and every text-line corner lies inside the rounded rect (control: the same pill under `CircleShape` fails); one-line pill at 1.0 unchanged | Robolectric NATIVE | `--tests '*HandledByPillShapeTest' --tests '*OutcomeSheetReceiptLongLabelTest'` | Wave 0 (new) + ✅ existing pill test |
| POL-02 | `handledByDotRing`: null for hub light/dark; non-null with contrast >= 3 for a synthetic white-on-light pair and for hard-coded dynamic-sample schemes (light and dark); fill contrast >= 3 OR ring contrast >= 3 for every scheme; `handledByDotColor` still returns `warnTone.content` / `primary` | JVM unit | `--tests '*HandledByDotContrastTest' --tests '*OutcomeSheetReceiptSuccessTest'` | Wave 0 (new) + ✅ existing |
| POL-02 | Dot node keeps tag, 6dp size, position; ring present only in the failing case | Robolectric | `--tests '*OutcomeSheetReceiptSuccessTest' --tests '*HandledByDotContrastTest'` | ✅ + Wave 0 |
| API | Additive only: `Success` +3 fields, `BatchRowResultUiModel` +1, `OutcomeSheet` +1 param x2, `UndoCenterScreen` +1 overload; no public composable added | gate | `./gradlew apiDump apiCheck`, `git diff api.txt`, `bash tools/verify-api-source-additive.sh v2.7.1` | n/a |
| Guards | Registry integrity, CATALOG drift, vocabulary, library invariants unchanged (counts 67 / 6 / 73) | guard | guard run command | ✅ |
| Style | detekt zero baseline (test sources too) | static | `./gradlew detekt --no-daemon` | n/a |

### Sampling Rate
- **Per task commit:** the quick run command narrowed to the touched area (about 30-60 s).
- **Per wave merge:** guard run command plus quick run command plus `OutcomeSheetClassicParitySourceContractTest`, `OutcomeSheetReceiptSourceContractTest`, `UndoCenterScreenSourceContractTest`.
- **Phase gate:** full suite command, fresh (`--rerun-tasks`), then `git diff api.txt` (additions only) and `tools/test/run-all.sh`, before `/gsd-verify-work`.

### Wave 0 Gaps
- [ ] `component/OpenTargetRowTest.kt`, `OutcomeSheetOpenTargetTest.kt`, `OutcomeSheetBatchOpenTargetTest.kt`, `OutcomeSheetOpenTargetLongLabelTest.kt` (FOPEN-01/02)
- [ ] `feedback/UndoCenterScreenOpenTargetTest.kt`, `UndoCenterScreenOpenParityTest.kt`, `UndoCenterScreenOpenTargetLongLabelTest.kt` (FOPEN-03)
- [ ] `component/HandledByPillShapeTest.kt`, `HandledByDotContrastTest.kt` (POL-01/02)
- [ ] Extend `component/OutcomeSheetFixtures.kt` `success(...)` and add a `batchResultsWithTargets()` helper (defaults keep every existing caller compiling)
- [ ] Extend `V271CallSiteCanaryTest` with the four widened surfaces
- [ ] Edit `UndoCenterScreenSourceContractTest` count 1 to 2 deliberately (D-04)
- [ ] Pure-JVM contrast helper in main plus a test that mirrors `ContrastTestSupport.contrastRatio` so the two cannot diverge

### Gate-1 (device) applicability
Gate-1 is binding: FOPEN-01..03, POL-01 and POL-02 are user-visible and device-checkable, and STYLE section 7 says long-string layout sign-off needs screenshots. Method as Phase 25/26: the TESTER (`R5CT10XNKQN`) via a throwaway harness consumer built against the library AAR republished from HEAD (never committed), driving the public API only. Items: (1) Success single and batch with mixed targets in Classic and Receipt, German, tap opens the right target; (2) Undo Center with Available/Undone/targetless rows, tap vs hold; (3) font scale 2.0 long pill (screenshot, text inside the shape) and 1.0 pill unchanged; (4) dot under the device's real dynamic color and under a forced white-on-light warn tone. Restore state after: font scale to 1.0, `wm density 450` (never `wm density reset`), accessibility settings. TalkBack confirmation is Phase 28 (A11Y-02); if it cannot be driven, park it for Yahir, never waive. Explorer demos for these surfaces are Phase 28 (DEMO-03), so Phase 27 needs no Explorer changes.

## Security Domain

> `security_enforcement` is enabled (absent = enabled; `security_asvs_level: 1` in config).

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | - |
| V3 Session Management | no | - |
| V4 Access Control | no | Library holds no authority; the consumer's `onOpenTarget` decides what a key may open |
| V5 Input Validation | yes (limited) | `FeedbackTarget.key` is opaque consumer data forwarded verbatim; the library never parses, renders or logs it. The consumer must validate the key in its `onOpenTarget` mapping |
| V6 Cryptography | no | - |
| V7 Error handling/logging | yes | Never log or render `key`; `FeedbackTarget.toString` already omits it |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Opaque key leaks into logs, semantics or test tags | Information disclosure | Use `openLabel` only in semantics/testTag; contract test greps the new files for `.key` use in `semantics`/`testTag`/`Log`; keep `FeedbackTarget.toString` as is |
| Stale callback opens an item that Undo removed | Tampering (wrong item) | `rememberUpdatedState` / leave the open branch when `openableTarget` is null; test it (Pitfall 5); callers clear `Success`/batch `target` after Undo (D-14) |
| Double tap fires navigation twice | Denial of service (UX) | Documented idempotent handler (Pitfall 10) |
| A hold on an Undo Center row accidentally opens or undoes | Tampering | `onLongPress` shows the peek only; Undo button isolated by its own consumed down; tests for both |
| Forged/forwarded target for a sheet row | Spoofing | Out of scope for the library; consumer maps keys to routes it owns |

## Project Constraints (from CLAUDE.md)

- One-way dependency: the library imports no consumer code, holds no secrets, makes no domain assumptions. New code may import only Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable, osmdroid [VERIFIED: /home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md invariants section; enforced by `LibraryInvariantsGuardTest`].
- Never add `@HiltAndroidApp` / `@AndroidEntryPoint`.
- `ComponentRegistry` drift guards: this phase adds no public top-level `@Composable`.
- Source-compatible, not binary-compatible: append defaulted params, keep trailing lambdas last, never append after a trailing lambda; no new `@Deprecated(HIDDEN)` overloads or `copy$default` shims. Metalava `apiCheck` is the per-commit gate; `tools/verify-binary-abi.sh` is informational only.
- Detekt zero baseline: do not regenerate a baseline; fix findings or tune with justification. Detekt analyses test sources too.
- Shipping is human-gated: no tag, no push, no consumer repin from this phase. v2.8.0 and every push are Yahir's; tags are immutable and are JitPack coordinates; never cut a GSD milestone tag.
- Sequential-in-hub convention: commit on `main` here, do not edit consumer files.
- Shared host: never `./gradlew --stop`; use `--no-daemon`; one Gradle at a time.
- Interaction conventions travel with the components; long localized strings need a DE/ES ~360dp check before sign-off [CITED: memory "Test localized long strings before signoff"].
- Working-tree debris (`.planning/config.json`, `state.json`, `graphs/*`, stage markers, `.gate/`, abandoned `uat-evidence` dirs, `.oc-audit/`) must never be committed; commit only named files.
- User preferences (global): concise explanations, root cause with every fix, design-conscious UI treatment, avoid regressions.

## Sources

### Primary (HIGH confidence, read this session)
- `/home/yahir/Projects/Reusable/android/yahirandroidtaste/src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt` (full file)
- `.../component/OutcomeSheetReceipt.kt` (full file), `.../component/ReceiptRow.kt` (full file), `.../component/OutcomeSheetDefaults.kt`, `.../component/ColorUtils.kt` (contrast helpers)
- `.../model/VoiceOutcomeUiState.kt`, `.../model/BatchRowResultUiModel.kt`, `.../model/HandledByUiModel.kt`
- `.../feedback/UndoCenterScreen.kt`, `UndoHistoryEntry.kt`, `UndoHistoryStore.kt` (append overload), `FeedbackCard.kt` (gesture/semantics/chevron lines), `FeedbackTarget.kt`
- `.../theme/ToneTokens.kt`, `Theme.kt`, `Color.kt`, `Dimens.kt`
- Tests: `OutcomeSheetClassicParitySourceContractTest.kt`, `OutcomeSheetReceiptSourceContractTest.kt`, `UndoCenterScreenSourceContractTest.kt`, `UndoCenterScreenSourceCompatV251Test.kt`, `UndoCenterScreenDefaultParityTest.kt`, `UndoCenterScreenTest.kt`, `UndoCenterScreenLongLabelTest.kt`, `OutcomeSheetReceiptLongLabelTest.kt`, `OutcomeSheetReceiptSuccessTest.kt`, `OutcomeSheetReceiptTagsTest.kt`, `OutcomeSheetFixtures.kt`, `OutcomeSheetHoistedStateTest.kt`, `OutcomeSheetSourceCompatV251Test.kt`, `V271CallSiteCanaryTest.kt`, `ToneTokensTest.kt`, `ContrastTestSupport.kt`, `SourceContractTestSupport.kt`, `SemanticsDump.kt`, drift/invariant guards
- `api.txt` (UndoCenterScreen, OutcomeSheet, Success, BatchRowResultUiModel, UndoHistoryEntry, FeedbackTarget lines), `tools/classify-hub-change.sh`, `tools/hooks/pre-commit`, `tools/verify-additive-diff.sh`, `tools/verify-api-source-additive.py`
- `.planning/phases/27-.../27-CONTEXT.md`, `.planning/research/v2.8-BRIEF.md`, `.planning/research/PITFALLS.md` (sections 9-13), `STACK.md`, `ARCHITECTURE.md`, `FEATURES.md`, `SUMMARY.md`, `.planning/ROADMAP.md` Phase 27, `.planning/REQUIREMENTS.md`, `.planning/v2.8-DECISION-MAP.md` Phase 27, `.planning/STATE.md`, Phase 25/26 SUMMARY/VALIDATION/SELF-UAT for precedent
- `docs/design/snackbar-go-to-item/index.html` (sheet mock, section 2/3), `docs/STYLE.md` sections 4-7, `API.md` rows

### Secondary (MEDIUM confidence)
- `.planning/research/STACK.md` section 6 suggestion to use `onTextLayout` for the pill: superseded by D-06 (capped shape) and by PITFALLS 12.

### Tertiary (LOW confidence)
- Compose `Shape`/`Outline` API sketch and `detectTapGestures` post-long-press behaviour are from training knowledge (A2, A3); both are covered by Wave 0 tests, and A3 has an in-repo green analogue (`FeedbackCard` long-hold test).

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH, no new dependency, every API already used in the repo.
- Architecture: HIGH for the pin-driven structure (derived from the actual contract tests); MEDIUM for the UndoCenter wrapper vs refactor choice (both valid).
- Pitfalls: HIGH for the contract/lane/overload ones (read from source), MEDIUM for pill geometry and the stale-callback behavior until the tests run.

**Research date:** 2026-10-17
**Valid until:** 2026-11-16 (stable; re-check only if `OutcomeSheet.kt`, `OutcomeSheetReceipt.kt`, `UndoCenterScreen.kt` or their contract tests change, e.g. if Phase 28 lands first)
