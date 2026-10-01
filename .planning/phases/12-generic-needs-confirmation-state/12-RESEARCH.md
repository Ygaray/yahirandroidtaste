# Phase 12: Generic needs-confirmation state - Research

**Researched:** 2026-09-30
**Domain:** A purely-additive third sealed arm (`NeedsConfirmation`) on an already-shipped `VoiceOutcomeUiState`, inside a reusable Jetpack Compose design-system library — rendered by the SAME `OutcomeSheet` composable Phase 11 shipped, with no new public composable required
**Confidence:** HIGH (every current-code claim below is grounded in source files read in full this session, cited with path + line range and quoted verbatim where load-bearing; the concrete `NeedsConfirmation`/`ProposedItemUiModel` field-level design is original synthesis against the frozen D-01–D-06 contract — clearly flagged `[ASSUMED]`, not asserted as already-decided)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

Resolved in `ai` mode (`source: ai-auto`); updated per **R1 GO-WITH-CHANGES**. Both SB and CT confirmed the confirm-shape against their real call-sites (R1, 2026-09-29) — **UNGATED**, planning may proceed.

- **D-01 [per-item-metadata].** Carry the per-item metadata that differs between consumers (SB risk tier vs CT match score) as an OPAQUE presentational per-item slot (e.g. `trailingContent: @Composable () -> Unit`, or a nullable/defaulted field the library never interprets) — never a library-defined risk/score/severity enum. Reversibility: one-way — freezes into `v2.4.0`.
- **D-02 [additivity].** Phase 12 is purely additive — a new `NeedsConfirmation` subtype of the sealed `VoiceOutcomeUiState` + one `when` branch, reusing `SheetScaffold` + `DynamicActionButton` — CONTINGENT on Phase 11 having shipped the sealed `VoiceOutcomeUiState` param. **Resolved 2026-09-30 (Runtime Decision below): Phase 11 shipped it exactly as required — verified live this session, see Phase Requirements table.**
- **D-03 [confirm-crossrepo].** Model single-or-batch as one uniform `List<ProposedItemUiModel>` (size 1 = single) + a `SelectionMode` (AllOrNothing | PerItem). Validated against SB's `MutationGate`/`VoiceConfirmGate` and CT's `VoiceResultSheet` Proposed/ProposedBatch call-sites at the A13 reconvene — **this research re-verified both call-sites live this session (see Verified Current Code, cross-repo)**.
- **D-04 [confirm-scope].** The confirm state must ALSO cover per-item EDIT before confirm, per-item REMOVE, and a "Confirm all (N)" action. The opaque per-item slot may carry the edit UI, but the state model itself needs an item-level amended/removed representation. Reversibility: one-way — freezes into `v2.4.0`.
- **D-05 [confirm-sb-props].** SB always sends a SINGLE item (size 1 — its gate is sequential). Add OPTIONAL confirm props: (1) a `title` separate from the reason/body; (2) a per-confirm confirm-button VERB (default "Confirm"; e.g. "Delete"/"Merge"/"Allow"); (3) a style/severity (Destructive = red — all 4 SB subjects are destructive); (4) a reversibility hint (Undoable/Irreversible, or free text) that stays visible. **Behavior:** dismiss/outside-tap/back = decline; the host may WITHDRAW the sheet while it is showing (SB's gate auto-holds after 120s) — handle without crashing or flashing. **Privacy:** `ProposedItemUiModel.toString()` must NOT print item names (SB T-166-05) — SB passes finished strings, so YAT never sees raw subjects.
- **D-06 [confirm-ct].** CT must-haves: (1) a TOP-LEVEL (sheet-level) content slot IN ADDITION to the per-item slots — CT's `target_date` is ONE shared value for all N items; (2) `SelectionMode` must include a WHOLE-BATCH mode (edit/remove rows, then ONE "Confirm all (N)" via `onConfirmAllProposed`) — CT is NOT per-item-confirm; keep `PerItem` as another option; (3) an OPTIONAL per-item reason/confidence cue on `ProposedItemUiModel` ("weak match, check this one") — thresholds stay app-side, only the cue is rendered.

### Claude's Discretion

Do NOT reuse `component/ConfirmationDialog.kt` (it is an `AlertDialog` with scalar title/body — can't host a batch list; the confirm state lives INSIDE the sheet). **Verified live this session** — see Verified Current Code; `ConfirmationDialog.kt:38-65` confirms `title: String`/`body: String` scalars with no list-rendering capability.

### Deferred Ideas (OUT OF SCOPE)

None — discussion stayed within phase scope.

### Runtime Decision (2026-09-30, D-02 provisional refreshed)

Phase 11 shipped `sealed interface VoiceOutcomeUiState` (`src/main/.../model/VoiceOutcomeUiState.kt`) with exactly `Success` and `Failure` arms and an explicit KDoc contract that the top-level type stays stable and that Phase 12 adds `NeedsConfirmation` as a NEW sealed subtype + one `when` branch, never a reshape of either arm. **Re-verified live this session** (see Verified Current Code below) — the KDoc text is unchanged and the contingency is satisfied. Plan Phase 12 as a purely additive 3rd arm reusing `SheetScaffold` + `DynamicActionButton`; do not reshape `Success`/`Failure`.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-------------------|
| VOUT-04 | The outcome sheet renders a generic needs-confirmation state from props — a reason string, single-or-batch proposed item(s), and confirm/cancel actions — domain-neutral so it covers both SB's `MutationGate`/`VoiceConfirmGate` risk confirm and CT's weak-match single/batch confirm | The concrete `NeedsConfirmation`/`ProposedItemUiModel`/`SelectionMode` design below (Architecture Patterns, Code Examples), cross-validated against BOTH consumers' real, currently-live source files this session (see Verified Current Code, cross-repo) |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- **One-way dependency:** library imports no host code, holds no secrets, makes no domain assumptions. New code here imports only Android SDK / AndroidX / Compose / Hilt / Coil / navigation-compose / reorderable / osmdroid — never a consumer, never `voice-action-engine`. No new dependency is needed this phase (confirmed below).
- **`ComponentRegistry` is the single source of truth + a drift guard.** Every public top-level `@Composable` in the visual packages must be registered XOR allowlisted. **Key finding this phase: `NeedsConfirmation` needs NO new registry `Entry`** — see Architectural Responsibility Map / Pitfall 5 below.
- **Interaction conventions travel with the components:** reveal-confirm destructive swipe, standardized snackbar/undo feedback, conditional-render-no-dead-space — preserve them.
- **Detekt zero-baseline policy:** keep detekt green at zero baseline.
- **AGP 9.2.1 / Kotlin 2.3.20 / Hilt 2.60.1 / Compose BOM 2026.04.01 / JDK 17, minSdk 35, compileSdk 36**, single-module hub — every Gradle command drops the `:yahirandroidtaste` module prefix.
- **Frozen all-`val` constructors; the `TagChipUiModel.kt` `copy()`-ABI lesson** — new public data classes must be designed additive-safe from day one (see Common Pitfalls).

## Summary

Phase 12's entire engineering surface is **one new sealed subtype** (`VoiceOutcomeUiState.NeedsConfirmation`) plus two new small model types (`ProposedItemUiModel`, `SelectionMode`), rendered by **extending `OutcomeSheet.kt`'s existing exhaustive `when`** with a new private body function — mirroring exactly how `SuccessBody`/`FailureBody`/`UndoAffordanceBody` are already built (private, `OutcomeSheet.kt`-local, not independently registered). This session re-verified live, at HEAD, that: (1) Phase 11 shipped `VoiceOutcomeUiState` with exactly the `Success`/`Failure` two-arm shape the KDoc promises, explicitly reserving a third arm for this phase (`model/VoiceOutcomeUiState.kt:12-14`, quoted below); (2) `ConfirmationDialog` is unambiguously the wrong host (scalar `title`/`body` strings, `AlertDialog`, no list slot — `component/ConfirmationDialog.kt:38-48`); (3) `DynamicActionButton`'s existing `ActionButtonDefaults.ActionButtonRole` enum (`Destructive`/`Save`/`Neutral`, already public) is a ready-made severity primitive — **no new severity enum needs inventing**, directly satisfying D-05's "destructive" ask without adding a new public symbol; (4) the `DomainVocabularyDriftGuardTest` and `ComponentRegistryDriftGuardTest` both scan ONLY public top-level `@Composable` functions — since this phase adds **zero** new top-level composables, **neither drift guard needs an edit**, and the already-registered `OutcomeSheet` `ComponentRegistry.Entry` (`explorer/VoiceCommandFamilyScreen.kt:143-160`) needs no new `Entry`, only new gallery fixture demo buttons inside the existing `OutcomeSheetVariants()`.

This session also read BOTH real consumer call-sites live (not from CONTEXT.md's paraphrase) to cross-validate D-03/D-05/D-06: SecondBrain's `MutationGate`/`VoiceConfirmGate` (`core/agent/MutationGate.kt`, `VoiceConfirmGate.kt`, confirmed `ConfirmSubject` sealed type with `DeleteCard`/`DeleteTag`/`MergeTag`/`Unclassified` arms, a 120s auto-hold timeout, and a `toString()` override that deliberately omits the subject's own fields — directly corroborating D-05's privacy requirement) and CalTracker's `VoiceLogUiState`/`VoiceResultSheet` (confirmed `Proposed` single-item state, `ProposedBatch(rows: List<BatchItemState>, date: LocalDate, transcript: String)` with the date as a genuinely sheet-level shared value, `BatchItemState.needsAttention: Boolean` as the exact "weak match" cue D-06 describes, and a live `"Confirm all ($resolvableCount)"` button wired to `onConfirmAllProposed()` with per-row deletion via `onDeleteBatchItem(Int)` — never a checkbox-based per-item selection). Both real shapes slot cleanly into the single proposed `NeedsConfirmation`/`ProposedItemUiModel` design below with no field left meaningless on either side.

**Primary recommendation:** Add `NeedsConfirmation` as a new `data class` arm of `VoiceOutcomeUiState` (next to `Success`/`Failure`, same file), add `ProposedItemUiModel` and `SelectionMode` as new files in `model/`, and extend `OutcomeSheet.kt`'s `when` with one new branch calling a new **private** `NeedsConfirmationBody` function that follows the exact `SuccessBody`/`UndoAffordanceBody` pattern already in that file (Column + `Dimens` tokens + `testTag`s). Reuse `DynamicActionButton`/`ActionButtonDefaults.ActionButtonRole` for confirm/cancel/severity — do not invent a new severity enum. No new `ComponentRegistry.Entry`, no new drift-guard allowlist edit, no new Gradle dependency. Extend the existing `OutcomeSheetTest.kt` and `VoiceCommandFamilyScreen.kt`'s `OutcomeSheetVariants()` fixtures rather than creating new files.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Needs-confirmation prompt rendering (reason, title, items list, confirm/cancel buttons) | **Library** (`OutcomeSheet.kt`, extended) | — | Pure presentation from props; same tier as the already-shipped Success/Failure bodies |
| Confirm/cancel DECISION and what happens next (apply mutation, delete row, retry) | Consumer app | — | `onConfirm`/`onCancel` are callbacks only; the library never executes a voice command or a mutation (INV-01) |
| Per-item edit widget (SB's severity badge; CT's `AmountEditor` + `ItemCorrectionDropdown`) | Consumer app (fills the opaque slot) | Library (renders whatever slot it's given, D-01) | The slot is deliberately opaque — the library never defines or interprets risk tiers or match scores |
| Per-item removal signal (a row disappearing from the next render) | Consumer app (owns `items` list mutation) | Library (renders an optional `onRemove` callback per row) | Mirrors CT's REAL pattern verified live: `onDeleteBatchItem(Int)` mutates the ViewModel's own `rows` list; the library never maintains item-list state itself |
| Shared batch-level value (CT's `target_date`, ONE value for N items) | Consumer app (owns the value + its own picker UI) | Library (reserves a single top-level slot, renders it once) | D-06; verified live CT keeps `date: LocalDate` at the `ProposedBatch` level, not per-row — the library's top-level slot must mirror that altitude |
| Confirm/cancel styling (destructive red vs neutral) | **Library** (`ActionButtonDefaults.ActionButtonRole`, already shipped) | — | No new type needed — reuse the existing public enum (see Don't Hand-Roll) |
| Registry/gallery wiring | Library (`explorer/VoiceCommandFamilyScreen.kt`) | — | Extends the ALREADY-REGISTERED `OutcomeSheet` entry's fixture demos; no new `Entry` |

## Standard Stack

### Core

No new external library dependency. Everything is Compose/Material3 + the library's own existing public symbols.

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|---------------|
| Jetpack Compose / Material3 | Compose BOM 2026.04.01 (pinned) | Rendering the new sealed arm | Already the library's only UI toolkit |
| `DynamicActionButton` + `ActionButtonDefaults.ActionButtonRole` (`component/DynamicActionButton.kt:37-85`, verified) | in-repo | Confirm/cancel buttons; `Destructive` role directly satisfies D-05's severity ask | Already public, already used for the Failure action slot and the Undo-all button — zero new public surface needed for severity |
| `SheetScaffold` (`component/SheetScaffold.kt:50-69`, verified) | in-repo | Already `OutcomeSheet`'s host — no change needed | N/A — already wired |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `Dimens` tokens (`theme/Dimens.kt`, used verbatim in `OutcomeSheet.kt` as `Dimens.HorizontalPadding`/`Dimens.ContentSpacing`/`Dimens.HairlineSpacing`, verified by direct use in the file read this session) | in-repo | Spacing for the new `NeedsConfirmationBody`/per-item rows | Keep every new row visually consistent with `UndoRowItem`/`BatchResultsList` |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| A new `ConfirmSeverity` enum (`Normal`/`Destructive`) as FEATURES.md originally sketched | Reuse the EXISTING public `ActionButtonDefaults.ActionButtonRole` (`Destructive`/`Save`/`Neutral`) | Reuse adds zero new public API surface and automatically gets the `Save`-role filled-button treatment for a non-destructive confirm "for free"; the tradeoff is the enum also carries a `Save` arm that has no obvious confirm-sheet meaning — the plan should pick which of the 3 existing values maps to "normal confirm" (likely `Save`, filled/primary) vs "destructive confirm" (`Destructive`, red text) and document that mapping, rather than treating this as self-evident |
| `LazyColumn` for the proposed-items list (PITFALLS.md's documented perf-trap concern for "CT batch with many weak matches") | Plain `Column` + `forEach`, matching `BatchResultsList`/`UndoAffordanceBody`'s existing precedent | Verified live: CT's real batches come from ONE spoken utterance (a handful of food items, not hundreds) — the existing non-lazy precedent is proportionate; flagged as an Open Question below rather than decided, since a `LazyColumn` nested inside `SheetScaffold`'s plain `Column` would also need explicit height-bounding to avoid an infinite-constraint crash, which the existing code has never had to solve |
| A checkbox-driven literal `SelectionMode.PerItem` UI (toggle per row, "confirm selected") | Reserve `PerItem` as an enum value with NO concrete rendering recipe yet (neither live consumer uses it) | CT's real "batch" flow is edit/remove-then-confirm-all, NOT select-a-subset — `PerItem`'s actual UI is genuinely undesigned against any live call-site; over-designing it now risks guessing a shape no real consumer has validated (mirrors Pitfall 2's warning against building for an imagined third consumer) |

**Installation:** None — no new Gradle dependency for this phase.

## Package Legitimacy Audit

**Not applicable.** Zero new external packages. Every new symbol (one sealed-arm data class, two new `model/` files) is authored against the existing Compose/Material3 dependency set already declared in `build.gradle.kts`; no new `implementation(...)` line is needed.

## Architecture Patterns

### System Architecture Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│ CONSUMER APP (SecondBrain / CalTracker)                                    │
│                                                                             │
│  SB: VoiceConfirmGate.admit() suspends on a destructive tool call          │
│      -> builds ConfirmSubject (DeleteCard/DeleteTag/MergeTag/Unclassified) │
│      -> maps to NeedsConfirmation(reason, items = listOf(ONE item), ...)   │
│         title/confirmLabel/severity/reversibilityHint chosen PER SUBJECT   │
│                                                                             │
│  CT: VoiceLogViewModel resolves a weak parse/match                         │
│      -> Proposed (single)        -> items = listOf(ONE item)              │
│      -> ProposedBatch(rows, date)-> items = N ProposedItemUiModel,         │
│         topLevelContent = { the ONE shared date-picker row }               │
│         per-row onRemove -> onDeleteBatchItem(stableId)                   │
│         onConfirm -> onConfirmAllProposed()  (NOT a per-item selection)    │
│                                                                             │
│  Both: onConfirm / onCancel are plain callbacks; the CONSUMER already      │
│  owns which items remain (removal already shrank `items` before the next  │
│  recomposition) — the library never returns an affirmed-id subset back.   │
└───────────────────────────────────┬─────────────────────────────────────┬─┘
             one-way dependency (INV-01)   props                          │
                                     ▼                                    │
┌───────────────────────────────────────────────────────────────────────────┐
│ yahirandroidtaste LIBRARY                                                  │
│                                                                             │
│  model/VoiceOutcomeUiState.kt (EXISTING FILE, additive edit)              │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │ sealed interface VoiceOutcomeUiState {                               │ │
│  │   Success(...)   (unchanged)                                         │ │
│  │   Failure(...)   (unchanged)                                         │ │
│  │   NeedsConfirmation(reason, items, selectionMode, title?, severity,  │ │
│  │     reversibilityHint?, confirmLabel, cancelLabel, topLevelContent?, │ │
│  │     onConfirm, onCancel)   <-- NEW, this phase                       │ │
│  │ }                                                                     │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│  model/ProposedItemUiModel.kt (NEW)     model/SelectionMode.kt (NEW)      │
│                                                                             │
│  component/OutcomeSheet.kt (EXISTING FILE, additive edit)                 │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │ when (outcome) {                                                     │ │
│  │   is Success -> SuccessBody(outcome)            (unchanged)          │ │
│  │   is Failure -> FailureBody(outcome)             (unchanged)         │ │
│  │   is NeedsConfirmation -> NeedsConfirmationBody(outcome)  <-- NEW    │ │
│  │ }                                                                     │ │
│  │ private fun NeedsConfirmationBody(...)  -- mirrors SuccessBody/      │ │
│  │   UndoAffordanceBody's existing private-function pattern             │ │
│  │ private fun ProposedItemRow(...)        -- mirrors UndoRowItem       │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  explorer/VoiceCommandFamilyScreen.kt (EXISTING, extend fixtures only)    │
│  -- NO new ComponentRegistry.Entry (OutcomeSheet's Entry already exists)  │
│  -- add fixture outcomes + "Show sheet" buttons to OutcomeSheetVariants() │
└───────────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Status |
|-----------|----------------|--------|
| `model/VoiceOutcomeUiState.kt` | Sealed `Success`/`Failure`/`NeedsConfirmation` | **DONE** (Success/Failure) — ADD `NeedsConfirmation` this phase |
| `model/ProposedItemUiModel.kt` (new) | One proposed item: `id`, `title`, `subtitle?`, `confidenceCue?`, `amended`, `onRemove?`, `trailingContent?` | **NEW (this phase)** |
| `model/SelectionMode.kt` (new, or nested enum) | `AllOrNothing` \| `PerItem` | **NEW (this phase)** |
| `component/OutcomeSheet.kt` | Renders `Success`/`Failure`/`NeedsConfirmation` | **EXTEND (this phase)** — add one `when` branch + 2 new private functions |
| `explorer/VoiceCommandFamilyScreen.kt` | Registry entry (unchanged) + gallery fixtures | **EXTEND (this phase)** — new fixture outcomes + demo buttons only, NO new `Entry` |
| `src/test/.../OutcomeSheetTest.kt` | Compose test coverage | **EXTEND (this phase)** — new test cases for the confirm branch |

## Recommended Project Structure

```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/
│   └── OutcomeSheet.kt                     # EXISTING — extend `when`, add NeedsConfirmationBody + ProposedItemRow (private)
├── model/
│   ├── VoiceOutcomeUiState.kt              # EXISTING — add `NeedsConfirmation` data class arm
│   ├── ProposedItemUiModel.kt              # NEW
│   └── SelectionMode.kt                    # NEW (or nest as `enum class SelectionMode` inside ProposedItemUiModel.kt/VoiceOutcomeUiState.kt — plan's call)
└── explorer/
    └── VoiceCommandFamilyScreen.kt         # EXISTING — add NeedsConfirmation fixtures to OutcomeSheetVariants(), NO new Entry
```

## Verified Current Code (read in full this session)

### `model/VoiceOutcomeUiState.kt` — the seam this phase extends

`[VERIFIED: src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt:1-68]` (full file read). The load-bearing KDoc, quoted verbatim:

> "Exactly two arms this phase (D-02): [Success] and [Failure]. The top level MUST stay stable — Phase 12 adds a `NeedsConfirmation` third arm purely additively (a new sealed subtype), never a reshape of either arm below. Do not guess at or pre-model that third arm here." (lines 12-14)

Current full shape, quoted:
```kotlin
sealed interface VoiceOutcomeUiState {
    data class Success(
        val summary: String,
        val handledBy: HandledByUiModel? = null,
        val editableContent: (@Composable () -> Unit)? = null,
        val inFlight: Boolean = false,
        val batchResults: List<BatchRowResultUiModel> = emptyList(),
        val undo: UndoAffordanceUiModel? = null
    ) : VoiceOutcomeUiState

    data class Failure(
        val reason: String,
        val handledBy: HandledByUiModel? = null,
        val action: FailureActionUiModel? = null
    ) : VoiceOutcomeUiState
}
```
`[VERIFIED: model/VoiceOutcomeUiState.kt:41-68]`. **Neither arm needs to change.** The additivity contingency (D-02) is satisfied — confirmed by direct read, not from plan text.

### `component/OutcomeSheet.kt` — the exhaustive `when` this phase extends

`[VERIFIED: src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:1-280]` (full file read). The top-level composable:

```kotlin
@Composable
fun OutcomeSheet(
    outcome: VoiceOutcomeUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier) {
        when (outcome) {
            is VoiceOutcomeUiState.Success -> SuccessBody(outcome)
            is VoiceOutcomeUiState.Failure -> FailureBody(outcome)
        }
    }
}
```
`[VERIFIED: component/OutcomeSheet.kt:47-60]`. Its own KDoc states: *"[outcome]'s sealed `when` match is exhaustive (compiler-enforced); Phase 12 adds a `NeedsConfirmation` third arm explicitly, never silently (D-02)."* `[VERIFIED: component/OutcomeSheet.kt:35-36]` — the compiler itself will force this edit (an un-extended `when` fails to compile the moment `NeedsConfirmation` exists), so there is no risk of silently forgetting the branch.

Every existing body function (`SuccessBody` lines 70-83, `UndoAffordanceBody` lines 94-131, `UndoRowItem` lines 148-187, `BatchResultsList` lines 190-219, `FailureBody` lines 229-254, `HandledByRow` lines 261-279) is a **private**, file-local `@Composable` function — none are separately registered in `ComponentRegistry`, none need a `DomainVocabularyDriftGuardTest` allowlist entry (both guards scan only PUBLIC top-level composables — confirmed by reading `DomainVocabularyDriftGuardTest.kt:51-56` and `ComponentRegistry.kt`'s own KDoc, both quoted below). `NeedsConfirmationBody`/`ProposedItemRow` should follow the exact same private, file-local pattern.

### `component/ConfirmationDialog.kt` — confirmed wrong tool (Claude's Discretion, verified)

`[VERIFIED: src/main/java/io/github/ygaray/yahirandroidtaste/component/ConfirmationDialog.kt:37-48]`:
```kotlin
fun ConfirmationDialog(
    title: String,
    body: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = ConfirmationDialogDefaults.confirmLabel,
    dismissLabel: String? = ConfirmationDialogDefaults.dismissLabel,
    confirmStyle: ConfirmationDialogDefaults.ConfirmStyle = ConfirmationDialogDefaults.ConfirmStyle.Destructive,
    properties: DialogProperties = DialogProperties()
)
```
`title`/`body` are scalar `String`s wrapped in M3's `AlertDialog` — there is no slot for a `List<ProposedItemUiModel>`, confirming CONTEXT.md's Claude's Discretion verbatim. Notably, `ConfirmationDialogDefaults.ConfirmStyle` (`Destructive`/`Neutral`, `[VERIFIED: ConfirmationDialog.kt:71-90]`) is a SEPARATE existing enum from `DynamicActionButton`'s `ActionButtonDefaults.ActionButtonRole` — the plan should reuse `ActionButtonRole` (since `NeedsConfirmationBody` reuses `DynamicActionButton`, not `ConfirmationDialog`), not `ConfirmStyle`.

### `component/DynamicActionButton.kt` — the existing severity primitive

`[VERIFIED: src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt:64-85]`:
```kotlin
object ActionButtonDefaults {
    enum class ActionButtonRole { Destructive, Save, Neutral }
    @Composable
    fun colors(role: ActionButtonRole) = when (role) {
        ActionButtonRole.Destructive -> ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ActionButtonRole.Save -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        ActionButtonRole.Neutral -> ButtonDefaults.textButtonColors()
    }
}
```
Already PUBLIC, already used in `OutcomeSheet.kt` for the undo-all button (`role = ActionButtonDefaults.ActionButtonRole.Neutral`, line 104) and the Failure action button (line 245). This is a ready-made severity primitive for D-05's "destructive confirm" ask — no new public enum needs inventing.

### `explorer/ComponentRegistry.kt` + `DomainVocabularyDriftGuardTest.kt` — confirmed scan scope

`ComponentRegistry`'s own KDoc: *""Component" = a public top-level `@Composable` function in one of the library's visual packages"* `[VERIFIED: explorer/ComponentRegistry.kt:12-13]`. `DomainVocabularyDriftGuardTest`'s own KDoc: *"source-scans... for public top-level `@Composable` functions"* `[VERIFIED: explorer/DomainVocabularyDriftGuardTest.kt:11-12]`. Both guards are scoped to public top-level composables only — private functions (what `NeedsConfirmationBody`/`ProposedItemRow` will be) are outside their scan entirely. **`PRIMITIVE_NOUN_ALLOWLIST` already contains `"Confirmation"`** `[VERIFIED: explorer/DomainVocabularyDriftGuardTest.kt:312]` (part of the widening block, grandfathered from `ConfirmationDialog`) — so even if the plan DID introduce a new public composable with a `Confirmation`-headed name, no allowlist edit would be needed. Since this phase adds zero new public composables, this is moot either way, but worth confirming for completeness.

`ComponentRegistry.entries` currently concatenates exactly **10** family lists, including `voiceCommandFamilyEntries` `[VERIFIED: explorer/ComponentRegistry.kt:94-103]` — the `OutcomeSheet` `Entry` already exists inside `voiceCommandFamilyEntries` (`explorer/VoiceCommandFamilyScreen.kt:143-160`, verified: `name = "OutcomeSheet"`, `content = { OutcomeSheetVariants() }`). No new `Entry` is needed this phase; `OutcomeSheetVariants()` (currently lines 395-443 of `VoiceCommandFamilyScreen.kt`, verified) is the place to add new "Show sheet" demo buttons for the confirm state.

### Cross-repo: SecondBrain's real `MutationGate`/`VoiceConfirmGate` (single confirm)

`[VERIFIED: /home/yahir/Projects/AndroidApps/Personal/SecondBrain/app/src/main/java/com/example/secondbrain/core/agent/MutationGate.kt:1-85, VoiceConfirmGate.kt:1-149]` (both files read in full this session). Confirmed:
- `ConfirmSubject` is a sealed interface with EXACTLY 4 arms: `DeleteCard(cardTitle: String?)`, `DeleteTag(tagName: String?, affectedCardCount: Int)`, `MergeTag(sourceName: String?, targetName: String?, newName: String?)`, `Unclassified(toolName: String)` `[VERIFIED: VoiceConfirmGate.kt:22-27]` — directly corroborating D-05's "per-confirm verb" ask (Delete/Merge/Allow map naturally to these 3+1 arms).
- `PendingConfirmation`'s own `toString()` is deliberately overridden: *"[toString] prints only [id] and the subject's simple class name — never any name/title the subject carries (T-166-05)."* `[VERIFIED: VoiceConfirmGate.kt:30-35]`, with the actual override: `override fun toString(): String = "PendingConfirmation(id=$id, subject=${subject::class.simpleName})"`. This is a LIVE precedent for the exact privacy discipline D-05 requires of `ProposedItemUiModel` — see Common Pitfalls below.
- The 120-second auto-hold: `const val CONFIRM_TIMEOUT_MS = 120_000L` `[VERIFIED: VoiceConfirmGate.kt:147]`, and `awaitConfirmation`'s `finally` block clears `_pendingConfirmation.value = null` on timeout `[VERIFIED: VoiceConfirmGate.kt:140-143]` — confirming D-05's "the host may WITHDRAW the sheet while it is showing" is a REAL, already-implemented behavior on the SB side, not a hypothetical.
- SB always gates ONE tool call at a time (`awaitMutex` serializes `awaitConfirmation`, `[VERIFIED: VoiceConfirmGate.kt:56-57, 120-131]`) — confirming D-05's "SB always sends a SINGLE item."

### Cross-repo: CalTracker's real `VoiceLogUiState`/`VoiceResultSheet` (single + batch)

`[VERIFIED: /home/yahir/Projects/AndroidApps/Personal/CalTracker_Android/app/src/main/java/com/caltracker/app/ui/voice/VoiceLogUiState.kt:1-230]` (full file read this session). Confirmed:
- `Proposed(transcript, parseQuantity, parseUnit, selectedIngredient?, selectedMeal?, parseDate)` — the single weak-match state `[VERIFIED: VoiceLogUiState.kt:98-105]`.
- `ProposedBatch(rows: List<BatchItemState>, date: LocalDate, transcript: String)` `[VERIFIED: VoiceLogUiState.kt:117-121]` — `date` is confirmed to be a GENUINELY sheet-level shared value (its own KDoc: *"[date] is the ONE sheet-level date shared by the whole batch (D-01, mutable via ...onSelectDate)"*, `[VERIFIED: VoiceLogUiState.kt:113-114]`), directly matching D-06's "TOP-LEVEL content slot" ask — this is not a design guess, it is what CT's real code already does.
- `BatchItemState(stableId: Int, foodQuery: String, parseQuantity: Double, parseUnit: String, selectedIngredient: Ingredient? = null, selectedMeal: MealWithIngredientAmounts? = null, needsAttention: Boolean = false)` `[VERIFIED: VoiceLogUiState.kt:221-229]` — `needsAttention` is EXACTLY the "weak match, check this one" cue D-06 describes, already a plain `Boolean` the library-side `ProposedItemUiModel.confidenceCue`/similar field can mirror.
- Via `grep` on `VoiceResultSheet.kt` this session: the batch confirm button reads `onClick = onConfirmAllProposed` with label `"Confirm all ($resolvableCount)"` `[VERIFIED: VoiceResultSheet.kt:508-527, grep-confirmed line numbers]`, and per-row removal is `onDelete = { if (!batchWriteInFlight) onDeleteBatchItem(it) }` `[VERIFIED: VoiceResultSheet.kt:508]` — confirming CT's batch flow is edit/remove-then-confirm-all, with NO per-item checkbox/selection UI anywhere in the real call-site. This directly supports the Alternatives Considered recommendation to leave `SelectionMode.PerItem` unrendered-by-design this phase (no real consumer exercises it).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Destructive/neutral confirm-button styling | A new `ConfirmSeverity` enum | `ActionButtonDefaults.ActionButtonRole` (`Destructive`/`Save`/`Neutral`, already public) | Zero new public API surface; already used identically for the Failure action slot and Undo-all button |
| A standalone confirm dialog/sibling composable | `ConfirmationDialog` (`AlertDialog`, scalar title/body) or a new top-level composable | A new private body function inside `OutcomeSheet.kt`, matching `SuccessBody`/`UndoAffordanceBody`'s existing pattern | Confirmed live: `ConfirmationDialog` structurally cannot host a list; a new TOP-LEVEL composable would need registry + drift-guard work neither drift guard otherwise requires this phase |
| Per-item privacy-safe string representation | Rely on a `data class`'s default, auto-generated `toString()` | An explicit `toString()` override on `ProposedItemUiModel` (and/or `NeedsConfirmation`) that omits `title`/`subtitle`, mirroring SB's own `PendingConfirmation.toString()` override verbatim | SB's own live code already solved this exact problem (T-166-05) for the identical privacy requirement — copy the technique, don't re-derive it |

**Key insight:** Every primitive this phase needs — the host (`SheetScaffold`), the action button + severity enum (`DynamicActionButton`/`ActionButtonRole`), and the private-body-function pattern for new sealed-state rendering — already exists and is already exercised by the shipped half of `OutcomeSheet.kt`. This phase is a data-modeling exercise (does the `NeedsConfirmation`/`ProposedItemUiModel` shape genuinely cover both consumers' real call-sites) far more than a widget-building one.

## Runtime State Inventory

Not applicable — additive phase, no rename/refactor/migration. No stored data, live service config, OS-registered state, secrets, or build artifacts are touched.

## Common Pitfalls

### Pitfall 1: Designing the confirm shape against CONTEXT.md's paraphrase instead of the real consumer code (now closed, verified this session)

**What goes wrong:** Trusting D-05/D-06's prose summary of SB's/CT's call-sites without reading the actual current source, risking a shape that technically satisfies the CONTEXT.md wording but doesn't compile cleanly against the real `ConfirmSubject`/`BatchItemState` types.

**Why it happens:** CONTEXT.md's decisions were themselves written from an R1 reconvene summary, one level removed from the source.

**How to avoid:** This research read `MutationGate.kt`/`VoiceConfirmGate.kt`/`VoiceLogUiState.kt` in full this session (see Verified Current Code, cross-repo) — the proposed `NeedsConfirmation`/`ProposedItemUiModel` shape below is cross-checked against the REAL field names and REAL callback signatures, not just CONTEXT.md's prose. **Status: closed by this research** — the plan can proceed directly from the Code Examples below without re-reading the cross-repo files itself, though it should spot-check if either consumer's code has changed since 2026-09-30.

**Phase to address:** Phase 12 (this research closes it; the plan should not need to re-open it).

### Pitfall 2: Treating "no new top-level composable" as "no registry work at all"

**What goes wrong:** Assuming CAT-01 (full registration) requires a brand-new `ComponentRegistry.Entry` for the confirm state, leading to either (a) skipping gallery coverage entirely (the confirm state becomes invisible in Gate-1 self-UAT) or (b) wastefully creating a redundant second `Entry` for the same `OutcomeSheet` composable, which would trip `ComponentRegistry`'s own duplicate-name `init` check (`explorer/ComponentRegistry.kt:136-145`, verified).

**Why it happens:** Every other phase in this milestone (10, 11) DID add new `Entry` rows because they added new top-level composables; Phase 12 breaks that pattern by design (D-02's additivity), which is easy to not notice mid-plan.

**How to avoid:** Confirmed this session: `OutcomeSheet`'s `Entry` already exists (`explorer/VoiceCommandFamilyScreen.kt:143-160`) with `content = { OutcomeSheetVariants() }`. The correct action is adding NEW fixture outcomes + "Show sheet" buttons INSIDE the existing `OutcomeSheetVariants()` function (currently lines 395-443) — not touching the `Entry` itself, not adding a second `Entry`.

**Warning signs:** A plan task titled "register NeedsConfirmation in ComponentRegistry" (wrong framing); `ComponentRegistry`'s `init` block throwing a duplicate-name error.

**Phase to address:** Phase 12, at plan-authoring time.

### Pitfall 3: `ProposedItemUiModel`'s default `data class` `toString()` leaking a subject's name into logs/crash reports

**What goes wrong:** `ProposedItemUiModel` ships as a plain `data class` with no `toString()` override. Kotlin's auto-generated `toString()` prints every field verbatim, including `title`/`subtitle` — which for SB's `DeleteCard`/`DeleteTag`/`MergeTag` subjects IS the user's own card/tag name. Any Compose recomposition log, crash report, or `Log.d` call that happens to print the model (directly or via a parent's `toString()`) leaks that name — exactly the information-disclosure risk SB's OWN `PendingConfirmation.toString()` override was written to prevent (T-166-05, verified live this session).

**Why it happens:** `data class` auto-`toString()` is the path of least resistance; there is no compiler warning for "this field shouldn't be printed."

**How to avoid:** Override `toString()` on `ProposedItemUiModel` (and consider the same on `NeedsConfirmation` if `reason`/`title` could ever carry a raw subject name) to print only `id` and a type/class marker — mirroring SB's own `PendingConfirmation.toString()` line verbatim: `override fun toString(): String = "ProposedItemUiModel(id=$id)"`.

**Warning signs:** A test or review diff showing `ProposedItemUiModel`'s auto-generated `toString()` in a log/crash-reporting call path; absence of an explicit `toString()` override in the new model file.

**Phase to address:** Phase 12, at model-authoring time — this is a cheap one-line fix if caught at design time, a real (if low-severity) privacy regression if caught only at Gate-2.

### Pitfall 4: The ambient `OutcomeSheet.onDismissRequest` and `NeedsConfirmation.onCancel` silently diverging

**What goes wrong:** `OutcomeSheet`'s existing top-level `onDismissRequest: () -> Unit` param (fires on scrim-tap/back-press/drag-to-dismiss, per `SheetScaffold`'s own contract) is passed once, independent of `outcome`. `NeedsConfirmation.onCancel` (a NEW field, fired only by an explicit Cancel-button tap inside `NeedsConfirmationBody`) is a SEPARATE callback. If the consumer wires these to two different ViewModel actions (e.g. `onDismissRequest = { viewModel.justCloseTheSheet() }` vs `onCancel = { viewModel.declineConfirmation() }`), D-05's explicit requirement — *"dismiss/outside-tap/back = decline"* — silently breaks: swiping the sheet away leaves the pending confirmation (and, for SB, the 120s-timeout gate) still open in the ViewModel even though the UI looks dismissed.

**Why it happens:** `OutcomeSheet`'s signature doesn't (and per D-02 should not) change — `onDismissRequest` stays one param regardless of `outcome`'s arm. The library has no way to enforce that a consumer wires both callbacks to the same decline action; this is a consumer-side wiring discipline, not something the type system catches.

**How to avoid:** Document explicitly in `NeedsConfirmation`'s KDoc (and surface in the plan's integration notes / Gate-1 checklist) that a consumer constructing a `NeedsConfirmation` outcome MUST route `OutcomeSheet`'s `onDismissRequest` to the SAME decline logic as `NeedsConfirmation.onCancel` — typically by passing the identical lambda reference to both. This is analogous to how `AlbumTitleConfirmSheet`'s own KDoc explicitly documents its `onDismiss`/`onSave` distinction (`component/AlbumTitleConfirmSheet.kt:81-85`, verified) — the precedent for calling out this exact kind of dual-callback trap already exists in this codebase.

**Warning signs:** A Gate-1 self-UAT where swiping away a confirm sheet does NOT behave like tapping Cancel (e.g. the pending mutation stays gated open, or a subsequent voice command re-shows the same confirm).

**Phase to address:** Phase 12, documented at design time; verified behaviorally at Gate-1 self-UAT (explicitly drive the swipe-dismiss path on a `NeedsConfirmation` fixture, not just the Confirm/Cancel buttons).

### Pitfall 5 (inherited from PITFALLS.md, re-verified against the real shapes this session): Designing the confirm shape too narrowly for one consumer

**Status:** This is the milestone-level pitfall PITFALLS.md already documents in depth (its own Pitfall 2). **This research's contribution is closing it with live evidence**: the Code Examples below are checked against BOTH `ConfirmSubject`'s real 4 arms (SB) and `ProposedBatch`/`BatchItemState`'s real fields (CT), not a hypothetical shape. No field in the proposed `NeedsConfirmation`/`ProposedItemUiModel` design is meaningless for either consumer (SB leaves `topLevelContent`/`confidenceCue` null; CT leaves `severity`/`reversibilityHint` at their defaults) — satisfying VOUT-04's success criterion 4 by direct cross-check, not by construction alone.

**Phase to address:** Phase 12 — already addressed by this research; the plan should still re-confirm both consumer files haven't changed since 2026-09-30 before finalizing.

## Code Examples

### `model/VoiceOutcomeUiState.kt` addition (illustrative — exact field order/defaults are a planning decision)

```kotlin
// ADDED to the existing sealed interface, alongside Success/Failure (unchanged):
data class NeedsConfirmation(
    val reason: String,
    val items: List<ProposedItemUiModel>,           // size 1 = single (SB, CT weak single); size >1 = batch (CT ProposedBatch)
    val selectionMode: SelectionMode = SelectionMode.AllOrNothing,
    val title: String? = null,                       // D-05: optional headline separate from `reason`
    val severity: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral, // D-05: reuse the EXISTING role enum, never a new one
    val reversibilityHint: String? = null,            // D-05: "Undoable" / "Irreversible" / free text, stays visible
    val confirmLabel: String = "Confirm",             // D-05: per-confirm verb ("Delete"/"Merge"/"Allow" for SB's 4 ConfirmSubject arms)
    val cancelLabel: String = "Cancel",
    val topLevelContent: (@Composable () -> Unit)? = null, // D-06: CT's shared target_date row; null for SB's single-item case
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit
) : VoiceOutcomeUiState
```
`[ASSUMED]` — original synthesis against the frozen D-01–D-06 contract, cross-checked against the live SB/CT shapes above; not itself quoted from any existing source (no such code exists in the repo yet). Field names for `reason`/`items`/`selectionMode` are carried forward verbatim from FEATURES.md's/ARCHITECTURE.md's already-converged sketch (`[CITED: .planning/research/FEATURES.md]`, `[CITED: .planning/research/ARCHITECTURE.md]`); `title`/`severity`/`reversibilityHint`/`confirmLabel`/`cancelLabel`/`topLevelContent` are this research's own field-level resolution of D-05/D-06's prose asks.

### `model/ProposedItemUiModel.kt` (new file)

```kotlin
package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Composable

/**
 * One proposed item inside a [VoiceOutcomeUiState.NeedsConfirmation] (D-01/D-04/D-06). A
 * [VoiceOutcomeUiState.NeedsConfirmation.items] list of size 1 is a single confirm (SB's
 * MutationGate risk confirm, CT's weak single match); size >1 is a batch (CT's ProposedBatch).
 *
 * [toString] intentionally omits [title]/[subtitle]/[confidenceCue] -- never print a subject's
 * own name (D-05 privacy, mirrors SecondBrain's own PendingConfirmation.toString() precedent).
 */
data class ProposedItemUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val confidenceCue: String? = null,        // D-06: optional "weak match" cue (CT's needsAttention)
    val amended: Boolean = false,             // D-04: whether the caller has in-place-edited this item
    val onRemove: (() -> Unit)? = null,       // D-04: null hides the remove control entirely (null-prop-hides)
    val trailingContent: (@Composable () -> Unit)? = null  // D-01: opaque per-item slot (SB risk badge / CT AmountEditor+ItemCorrectionDropdown)
) {
    override fun toString(): String = "ProposedItemUiModel(id=$id, amended=$amended)"
}
```
`[ASSUMED]` — original synthesis. `confidenceCue`/`amended`/`onRemove`/`trailingContent` field names are this research's own resolution of D-04/D-06; cross-checked against CT's real `BatchItemState.needsAttention: Boolean` (`[VERIFIED: VoiceLogUiState.kt:228]`) and CT's real per-row removal callback `onDeleteBatchItem: (Int) -> Unit` (`[VERIFIED: VoiceResultSheet.kt:120, grep-confirmed]`).

### `model/SelectionMode.kt` (new file)

```kotlin
package io.github.ygaray.yahirandroidtaste.model

/** D-03/D-06: how a batch [VoiceOutcomeUiState.NeedsConfirmation] resolves to a confirm action. */
enum class SelectionMode {
    /** Confirm acts on EVERY item currently in `items` (after any per-item removals). CT's real batch flow. */
    AllOrNothing,
    /** Reserved for a future per-item include/exclude UI. No live consumer (SB or CT) currently needs this. */
    PerItem
}
```
`[ASSUMED]` — `PerItem` is deliberately left WITHOUT a designed rendering recipe this phase (see Alternatives Considered) since neither real consumer exercises it; the enum value exists only so a future third consumer can map onto it without a breaking change (Pitfall 2 / PITFALLS.md's "favor slot APIs... make optional fields genuinely optional" guidance).

### `component/OutcomeSheet.kt` extension (illustrative — exact diff is a planning decision)

```kotlin
// The exhaustive `when` gains one branch (compiler enforces this edit, D-02):
when (outcome) {
    is VoiceOutcomeUiState.Success -> SuccessBody(outcome)
    is VoiceOutcomeUiState.Failure -> FailureBody(outcome)
    is VoiceOutcomeUiState.NeedsConfirmation -> NeedsConfirmationBody(outcome)  // NEW
}

@Composable
private fun NeedsConfirmationBody(confirmation: VoiceOutcomeUiState.NeedsConfirmation) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(Dimens.HorizontalPadding)
    ) {
        confirmation.title?.let { Text(it, style = MaterialTheme.typography.headlineSmall) }
        Text(confirmation.reason, style = MaterialTheme.typography.bodyLarge)
        confirmation.reversibilityHint?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        confirmation.topLevelContent?.invoke()  // D-06: CT's shared target_date row renders ONCE here
        confirmation.items.forEach { item -> ProposedItemRow(item) }
        Row(modifier = Modifier.padding(top = Dimens.ContentSpacing)) {
            DynamicActionButton(
                label = confirmation.cancelLabel,
                role = ActionButtonDefaults.ActionButtonRole.Neutral,
                onClick = confirmation.onCancel,
                modifier = Modifier.testTag("outcome_sheet_confirmation_cancel")
            )
            DynamicActionButton(
                label = confirmation.confirmLabel,
                role = confirmation.severity,
                onClick = confirmation.onConfirm,
                modifier = Modifier.testTag("outcome_sheet_confirmation_confirm")
            )
        }
    }
}

@Composable
private fun ProposedItemRow(item: ProposedItemUiModel) {
    Row(modifier = Modifier.fillMaxWidth().testTag("outcome_sheet_confirmation_item")) {
        Column(Modifier.weight(1f)) {
            Text(item.title)
            item.subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            item.confidenceCue?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
        }
        item.trailingContent?.invoke()
        item.onRemove?.let { onRemove ->
            IconButton(onClick = onRemove) { /* remove icon */ }
        }
    }
}
```
`[ASSUMED]` — this research's own mechanism-level synthesis, following `SuccessBody`/`UndoAffordanceBody`'s already-verified structural pattern (private functions, `Dimens` tokens, `testTag` convention) exactly. The plan is free to diverge on layout/spacing details as long as the public `NeedsConfirmation`/`ProposedItemUiModel` shape above is honored.

## State of the Art

Not applicable — this is wholly new surface, not a replacement of any existing pattern. The one relevant historical note: FEATURES.md's/ARCHITECTURE.md's original `ConfirmRequestUiModel` naming (a single wrapper class holding `reason`+`items`) was considered and is NOT carried forward here — this research instead folds `reason`/`items`/`selectionMode`/etc. directly onto the `NeedsConfirmation` sealed-arm data class itself (no separate wrapper type), since `Success`/`Failure` already establish the precedent of putting all of an arm's fields directly on the arm (no nested "request" model) and a separate wrapper would be an unnecessary extra public type with no behavioral benefit.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|----------------|
| A1 | `onConfirm: () -> Unit` (no returned id/subset) is sufficient — the consumer already owns which items remain via its own `items` list state, mirroring CT's real `onConfirmAllProposed()` (no-arg) | Code Examples, Architecture Patterns | Medium — if a future consumer genuinely needs the library to hand back an affirmed subset (e.g. a real `PerItem` consumer), this would need a new overload or a breaking shape change; no real consumer today needs it |
| A2 | Reusing `ActionButtonDefaults.ActionButtonRole` for `severity` (rather than a new `ConfirmSeverity` enum) is the right call, with `Save` mapping to "normal confirm" and `Destructive` to "destructive confirm" | Standard Stack (Alternatives Considered), Code Examples | Low — this is an internal implementation choice; even if the plan prefers a dedicated enum, the field name/position on `NeedsConfirmation` stays the same, only its type changes |
| A3 | `SelectionMode.PerItem` needs no concrete rendering recipe this phase, since neither SB nor CT's real, currently-live code exercises it | Alternatives Considered, Pitfall 5 | Low — if a future consumer needs it before this is revisited, the enum value already exists and is a pure addition; only the rendering logic (not yet designed) would need to be built then |
| A4 | A plain `Column` + `forEach` (matching `BatchResultsList`/`UndoAffordanceBody`'s existing precedent) is sufficient for `items`, rather than a `LazyColumn` | Alternatives Considered | Low-Medium — CT's real batches come from one spoken utterance (realistically a handful of items); if a future consumer's batches are large, this would need revisiting as a performance fix, not a shape change |
| A5 | An explicit `toString()` override on `ProposedItemUiModel` omitting `title`/`subtitle`/`confidenceCue` is necessary to satisfy D-05's privacy requirement, mirroring SB's live `PendingConfirmation.toString()` precedent | Common Pitfalls (Pitfall 3), Code Examples | Medium if skipped — a title/subtitle leak into a log or crash report is a real (if narrow) information-disclosure regression; cheap to fix if caught at design time |

**If this table is empty:** N/A — see above. A1/A2/A3/A4 are low-risk implementation-detail recommendations the plan can adjust without reworking the overall shape; A5 is the one item worth treating as a near-requirement rather than a pure suggestion, given it directly operationalizes a decision (D-05) CONTEXT.md already locked.

## Open Questions

1. **Does `onConfirm` ever need to return the affirmed item-id subset, rather than a plain `() -> Unit`?**
   - What we know: neither SB (`MutationGateDecision.Admit`, no payload) nor CT (`onConfirmAllProposed()`, no payload) needs one — both consumers already track "what remains" in their own state.
   - What's unclear: whether a future `SelectionMode.PerItem` consumer would need the library to hand back which items were checked, vs. tracking that itself via per-item callbacks.
   - Recommendation: ship `onConfirm: () -> Unit` now (A1); this is additive-safe to extend later with a new overload if a real `PerItem` consumer appears.

2. **Which of `ActionButtonDefaults.ActionButtonRole`'s 3 existing values maps to "normal" (non-destructive) confirm?**
   - What we know: `Destructive` (red text) clearly maps to SB's "all 4 subjects are destructive" case. `Save` (filled, primary-color) and `Neutral` (plain text) are both plausible for CT's non-destructive weak-match confirm.
   - What's unclear: no existing confirm-sheet precedent in this codebase to copy from directly (`AlbumTitleConfirmSheet`'s "Save Album" button is a `NameAndTagsEditor`-internal convention, not `DynamicActionButton`-based).
   - Recommendation: default `severity` to `Neutral` (matches the existing Undo-all button's role choice) and let the plan/Gate-1 visual review decide if `Save` reads better for CT's case.

3. **Exact `topLevelContent` placement — above or below the `items` list?**
   - What we know: CT's real `VoiceResultSheet.kt` renders its date-picker row ABOVE the batch item rows (`VoiceDatePickerRow(date = state.date, ...)` appears before the batch `LazyColumn`/row list, per the grep-confirmed line ordering in `VoiceResultSheet.kt`).
   - What's unclear: whether that ordering is load-bearing UX or incidental to CT's own layout.
   - Recommendation: place `topLevelContent` above `items` in `NeedsConfirmationBody` (matches the one real precedent); flag for Gate-1 visual review since this is a design call, not a locked decision.

## Environment Availability

Not applicable — no new external dependency, service, or CLI tool. No environment audit table needed.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric + Compose UI test (`androidx.compose.ui.test.junit4`), confirmed live in `src/test/.../OutcomeSheetTest.kt:1-24` (`@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`) |
| Config file | `build.gradle.kts` (module root) — unchanged |
| Quick run command | `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"` |
| Full suite command | `./gradlew testDebugUnitTest` (runs both drift guards — full-suite-only) |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| VOUT-04 (single confirm) | `NeedsConfirmation` with `items.size == 1` renders reason/title/item, Confirm/Cancel both fire their callback | unit/Robolectric Compose test | extend `OutcomeSheetTest.kt` | ✅ existing file, new test cases |
| VOUT-04 (batch confirm) | `items.size > 1` renders every row, `topLevelContent` renders once, per-item `onRemove` fires when supplied | unit/Robolectric Compose test | extend `OutcomeSheetTest.kt` | ✅ existing file, new test cases |
| VOUT-04 (destructive styling) | `severity = Destructive` renders the Confirm button with error-tinted text (via `ActionButtonDefaults.colors`) | unit/Robolectric Compose test | extend `OutcomeSheetTest.kt` | ✅ existing file, new test case |
| Naming-guard compliance | No new public composable, so neither drift guard needs a new allowlist entry — full suite should stay green with ZERO allowlist edits | existing full-suite guard tests | `./gradlew testDebugUnitTest --tests "*DriftGuardTest*"` | ✅ guards exist; verify no edit was needed |
| API additivity | `v2.4.0`-in-progress diff vs `v2.3.0` stays additive (new `NeedsConfirmation`/`ProposedItemUiModel`/`SelectionMode` symbols only; `Success`/`Failure` unchanged) | static/build-time | `./gradlew apiCheck` | ✅ tooling exists |

**Before authoring tests:** confirm `OutcomeSheetTest.kt`'s existing test-naming convention (backtick-quoted descriptive names, grouped by `// ── VOUT-0N ── ...` comments) and extend it with a `// ── VOUT-04 ── ...` section rather than a new test class.

### Sampling Rate

- **Per task commit:** `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"`.
- **Per wave merge:** `./gradlew testDebugUnitTest` (full suite — required for both drift guards, even though this phase shouldn't need to touch either allowlist).
- **Phase gate:** full suite green before `/gsd-verify-work`; Gate-1 self-UAT must explicitly drive: (a) the single-item confirm, (b) the batch confirm with at least one `onRemove` tap, (c) the swipe-to-dismiss path on an open `NeedsConfirmation` sheet (Pitfall 4), and (d) a destructive-severity confirm's visual treatment — not just the default Confirm/Cancel tap.

### Wave 0 Gaps

- [ ] New test cases in `OutcomeSheetTest.kt` — covers `NeedsConfirmation` single/batch/destructive/topLevelContent/onRemove rendering and callback firing.
- [ ] New fixture outcomes + "Show sheet" buttons in `VoiceCommandFamilyScreen.kt`'s `OutcomeSheetVariants()` — gallery coverage for Gate-1 visual review.
- No new shared fixtures or test framework install needed — existing Robolectric+Compose harness covers this phase's needs.

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|----------------|---------|-------------------|
| V2 Authentication | No | No auth surface |
| V3 Session Management | No | No session concept introduced |
| V4 Access Control | No | Library never gates access, only renders/emits callbacks |
| V5 Input Validation | Marginal/Yes | `reason`/`title`/item `id`/`title`/`subtitle`/`confidenceCue` are OPAQUE strings the library never parses or executes, only displays and (for `id`) echoes back verbatim via the consumer's own `onRemove` closure |
| V6 Cryptography | No | No cryptographic material touched |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|------------------------|
| `ProposedItemUiModel`'s default `toString()` printing a subject's real name/title into a log or crash report | Information Disclosure | Explicit `toString()` override omitting `title`/`subtitle`/`confidenceCue` (Pitfall 3) — mirrors SB's own live `PendingConfirmation.toString()` precedent, verified this session |
| `OutcomeSheet.onDismissRequest` and `NeedsConfirmation.onCancel` diverging, leaving a gate "open" in the consumer's ViewModel after the UI appears dismissed | Tampering (logic-level; a stale pending-confirm state could later be resolved unexpectedly) | Consumer-side wiring discipline, documented in the new `NeedsConfirmation` KDoc (Pitfall 4) — not a library-enforceable control, but a documented integration contract |

## Sources

### Primary (HIGH confidence — read directly this session)

- `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt` (full file, 68 lines) — the exact seam this phase extends, additivity contingency re-confirmed
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt` (full file, 280 lines) — exhaustive `when`, every existing private body function, `testTag` convention
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffold.kt` (full file, 69 lines) — host contract
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt` (full file, 86 lines) — `ActionButtonDefaults.ActionButtonRole`, reused for severity
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/ConfirmationDialog.kt` (full file, 91 lines) — confirmed wrong tool, confirmed separate `ConfirmStyle` enum (not reused)
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/AlbumTitleConfirmSheet.kt`, `TagCreateSheet.kt` (full files) — in-sheet confirm precedents, dual-callback documentation precedent
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt` (full file) — the `copy()`/ABI-break lesson, applied to new model design discipline
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt`, `FailureActionUiModel.kt`, `BatchRowResultUiModel.kt`, `UndoAffordanceUiModel.kt` (full files) — all-`val` model-shape precedent
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/AppChip.kt` (full file) — considered, not directly reused (opaque slot preferred over a chip primitive for per-item content)
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` (full file, 159 lines) — registry scan scope ("public top-level @Composable" only), 10-family concatenation, duplicate-name `init` check
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt` (full file, 495 lines) — the already-registered `OutcomeSheet` `Entry`, `OutcomeSheetVariants()`'s current fixture set
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerIndexScreen.kt` (partial, `ExplorerFamilies` object) — confirmed 10 ordered family keys
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` (partial, KDoc + `PRIMITIVE_NOUN_ALLOWLIST`) — confirmed scan scope and `"Confirmation"` already allowlisted
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt` (partial, 60 lines) — existing test harness/naming convention
- `/home/yahir/Projects/AndroidApps/Personal/SecondBrain/app/src/main/java/com/example/secondbrain/core/agent/MutationGate.kt` (full file, 85 lines) — SB's real confirm seam
- `/home/yahir/Projects/AndroidApps/Personal/SecondBrain/app/src/main/java/com/example/secondbrain/core/agent/VoiceConfirmGate.kt` (full file, 149 lines) — SB's real confirm UI-binding point, `ConfirmSubject`, privacy `toString()` precedent, 120s timeout
- `/home/yahir/Projects/AndroidApps/Personal/CalTracker_Android/app/src/main/java/com/caltracker/app/ui/voice/VoiceLogUiState.kt` (full file, 230 lines) — CT's real single/batch confirm states, `BatchItemState`
- `/home/yahir/Projects/AndroidApps/Personal/CalTracker_Android/app/src/main/java/com/caltracker/app/ui/voice/VoiceResultSheet.kt` (grep-confirmed line numbers for `onConfirmAllProposed`/`onDeleteBatchItem`/button label) — CT's real rendering call-site
- `.planning/phases/12-generic-needs-confirmation-state/12-CONTEXT.md`, `.planning/phases/11-voice-outcome-failure-sheet/11-CONTEXT.md`, `.planning/phases/11-voice-outcome-failure-sheet/11-RESEARCH.md`, `.planning/REQUIREMENTS.md`, `.planning/STATE.md` — upstream project state
- `.planning/cross-repo/HANDOFF.md`, `.planning/cross-repo/RECONVENE-BRIEF.md` — cross-repo milestone coordination state
- `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` (grepped §5.2, A2, E1) — confirmed `PreApplyGate`/`MutationGate` contract text matches CONTEXT.md's D-05/D-06 paraphrase
- `.planning/config.json` — confirmed `nyquist_validation: true`, `security_enforcement: true`, `security_asvs_level: 1`

### Secondary (MEDIUM confidence)

- `.planning/research/FEATURES.md`, `ARCHITECTURE.md`, `PITFALLS.md` — milestone-level research; this phase's research re-verifies and narrows their sketches against live code rather than treating them as settled

### Tertiary (LOW confidence)

- None — no WebSearch/external lookups needed; every technical claim resolves against in-repo or cross-repo source read this session. The `NeedsConfirmation`/`ProposedItemUiModel` field-level design is original synthesis against a fully-specified frozen contract, flagged `[ASSUMED]` throughout, not an external-source claim.

## Metadata

**Confidence breakdown:**
- Additivity contingency (D-02) satisfied: HIGH — `VoiceOutcomeUiState.kt` read in full, quoted verbatim
- No new ComponentRegistry Entry / no drift-guard edit needed: HIGH — both guards' scan scope confirmed by reading their own KDoc/source this session
- Cross-consumer shape validation (D-03/D-05/D-06): HIGH — both SB's and CT's real, currently-live source files read in full/grepped this session, not inferred from CONTEXT.md's paraphrase
- Concrete `NeedsConfirmation`/`ProposedItemUiModel` field-level design: MEDIUM — original synthesis, logically sound against the verified constraints and cross-checked against both consumers' real fields, but not itself verified against any authoritative source since no such code exists yet; the 5 Assumptions above are the load-bearing design points the plan should scrutinize

**Research date:** 2026-09-30
**Valid until:** Until the plan author either confirms or revises the severity-enum-reuse and `onConfirm` payload-vs-no-payload choices (Open Questions 1-2) — those are implementation decisions this research recommends but does not lock. Re-verify SB's/CT's real confirm call-sites immediately before authoring if either repo has landed new commits since 2026-09-30.
