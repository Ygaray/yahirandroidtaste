# Phase 11: Voice outcome & failure sheet - Research

**Researched:** 2026-09-30
**Domain:** Prop-driven Compose UI (sealed outcome state, additive undo-model extension, domain-vocabulary-safe naming) inside a reusable Android design-system library
**Confidence:** HIGH (every claim below is grounded in source files read this session, cited with path + line range; the few genuinely undecided items are marked `[ASSUMED]` and listed in the Assumptions Log)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01 [undo-shape].** REVISED at R1 by SecondBrain (the seam owner). SB's Undo Center already IS this repo's `UndoHistoryStore`/`UndoHistoryEntry` (`feedback/`; SB `UndoCenterViewModel.kt`). So the A18 run-level undo shape must EXTEND that existing entry model ADDITIVELY — e.g. an optional run/group id + group status — NOT a new parallel store. The outcome sheet's undo affordance ("Undo all (N)" + per-item Undo) renders a group's entries and emits undo callbacks. "Undo all (N)" counts ONLY items undoable as part of the group; notify-only mutations (e.g. SB edits) are excluded or shown as `Unavailable("can't be undone")`. The existing per-action snackbar `WithUndo` path MUST keep working unchanged (SB stays on it until its Phase 178). Reversibility: one-way — the additive entry-model extension freezes into the `v2.4.0` API. Plan-time tension: the current `UndoHistoryEntry` has an `internal` ctor + `suspend` lambda + `AtomicBoolean` (not a clean Compose-`STABLE` prop model), so the additive extension must expose a presentational, `STABLE`-friendly projection for the sheet WITHOUT breaking the entry's first-consumer-wins invariant — resolve in the plan. CT (R1): CT's voice writes are always CREATEs, so undo = delete the inserted row; a loud `Refused(reason)` suffices and CT needs NO restore payload — make `changedItem` OPTIONAL.
- **D-02 [undo-placement].** Put the undo affordance as a field on the `Success` outcome + model the undo-refused/partial state as a nested undo substate — NOT a new top-level arm of the sealed `VoiceOutcomeUiState`. This keeps the top-level sealed type stable so Phase 12's `NeedsConfirmation` stays a one-branch additive add.
- **D-03 [undo-crossrepo].** Validate the per-item `Unavailable(reason)` + top-level `Refused(reason, changedItem)` union against SecondBrain's and CalTracker's real undo call-sites at the A13 reconvene BEFORE authoring — a post-tag reshape is a breaking library change. (cross-repo — reconvene item)
- **D-04 [handled-by].** The "handled by" UI model carries a REQUIRED tier label + OPTIONAL approach, provider, model, and escalation count — all optional beyond the tier so the shape grows additively. Apps fill these from VAE's committed TEL-01 `CommandTrace` (per-tier attempts, escalation reasons, provider/model).
- **D-05 [success-editable].** The sealed outcome type needs a committed-but-EDITABLE `Success` state — CT auto-logs a high-confidence single item, then allows in-place edits AFTER commit — distinct from the needs-confirmation (pre-commit) state. Model it as a `Success` variant that can carry an editable payload + edit callbacks (prop-driven; CT injects the editor).
- **D-06 [batch-results].** For batch outcomes, the sheet renders per-row result reporting — a partial-success summary ("Logged 2 of 3") from a `failedCount` / per-row `success|fail` status — plus a batch-write-in-flight LOCK state (disable actions while the write is running). All prop-driven.
- **D-07 [clarify-choices].** A generic, prop-driven "clarification choices" composable (VCLAR-01, contract A19) — question text + a list of options, each `{ id: opaque String, label: String }`, + `onSelect(id)` + `onDismiss` (dismiss = cancel). When the model needs clarification ("Which list?"), the user resolves it by TAPPING an option, never by speaking again. Render it as a compact PRESSABLE choice surface (chips/buttons) — a Material snackbar holds only one action, so use a small choice bar or an outcome-sheet state, **your design call, easy to swap**. Visually informative, NOT an error. Apps map the engine's `Clarification` → these props (no engine dependency, L7). Domain-neutral; registered in `ComponentRegistry` (Voice Command family) with a full states matrix.
- **D-08 [failure-action].** The Failure state carries an OPTIONAL action slot — a label + callback (e.g. "Open Settings" for a missing/invalid key; "Retry" ONLY when the app says the failure is retry-safe). Prop-driven and optional: the app decides whether and what to show; absent → no action rendered. Keeps VOUT-03 loud AND actionable without the library assuming any action is always safe.

### Claude's Discretion

- Loud-failure and undo-refused visual treatment: research is confident (not a gray area) — use the theme `error`/`errorContainer` roles (icon + headline + reason string, sticky), and explicitly NOT `AttentionCue` (its KDoc forbids use as a failure signal).
- For VCLAR-01, chips-vs-buttons and bar-vs-sheet-state is a swappable design call (Yahir reviews in the gallery at Gate-1).

### Deferred Ideas (OUT OF SCOPE)

- The `NeedsConfirmation` state itself is Phase 12 (extends this sheet's sealed state).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VOUT-01 | Outcome/failure sheet renders a command outcome from props, domain-neutral | Sealed `VoiceOutcomeUiState`-equivalent pattern (Architecture Pattern 1); naming-guard finding (Pitfall 1) governs the composable's own name |
| VOUT-02 | "Handled by: tier/approach" indicator from props | `HandledByUiModel` shape per D-04, rendered as a chip/row reusing `AppChip` |
| VOUT-03 | Failure states render loudly and visibly, optional action slot | `theme/Color.kt` error roles (verified, quoted below) + `DynamicActionButton` for the optional action; explicit anti-pattern: `AttentionCue` |
| VUNDO-01 | "Undo all (N)" + per-item Undo + unavailable state + undo-refused/partial state | `UndoHistoryEntry`/`UndoHistoryStore` current shape (verified, quoted below) + additive-extension pattern (Pitfall 3 / TagChipUiModel lesson) |
| VCLAR-01 | Clarification choices: question + options `{id, label}` + onSelect + onDismiss, pressable, registered | `SegmentedOptionSelector`/`AppChip`/`ChipBar` precedent for compact pressable choice surfaces (Code Examples) |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- **One-way dependency:** library imports no host code, holds no secrets, makes no domain assumptions. New code here must import only Android SDK / AndroidX / Compose / Hilt / Coil / navigation-compose / reorderable / osmdroid — never a consumer, never `voice-action-engine`.
- **`ComponentRegistry` is the single source of truth + drift guard:** every new public top-level `@Composable` in `component/`, `feedback/`, `modifier/`, `theme/` must be registered in one of the family lists XOR allowlisted in `INTENTIONALLY_UNREGISTERED` — never neither, never both.
- **A second, independent guard exists and is load-bearing for this phase specifically:** `DomainVocabularyDriftGuardTest` — every public composable's **head token** (leading PascalCase word) must be in `PRIMITIVE_NOUN_ALLOWLIST` or explicitly grandfathered in `DOMAIN_VOCABULARY` with a rationale. See Pitfall 1 below — this is the single highest-risk naming trap for this phase.
- **Interaction conventions travel with the components:** reveal-confirm destructive swipe, standardized snackbar/undo feedback, conditional-render-no-dead-space — preserve them; failure content is never "no dead space," it is present content that must render loudly (see Pitfall 6 in the milestone PITFALLS.md, corroborated below).
- **Bindings-only Hilt, no application host:** `UndoHistoryStore` is already `@Singleton @Inject constructor()` — this phase's additive change to it (if any) must preserve that shape; no `@HiltAndroidApp`/`@AndroidEntryPoint` anywhere in this library.
- **Detekt zero-baseline:** no new findings buried in a regenerated baseline.
- **AGP 9.2.1 / Kotlin 2.3.20 / Hilt 2.60.1 / Compose BOM 2026.04.01 / JDK 17, minSdk 35, compileSdk 36**, single-module hub — every Gradle command drops the `:yahirandroidtaste` module prefix (`./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew apiCheck`).

## Summary

Phase 11 adds one new public composable family — the outcome/failure sheet plus a standalone clarification-choices surface — to a library that already shipped Phase 10's settings cards (`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`) into a brand-new tenth registry family, `ExplorerFamilies.VOICE_COMMAND` (confirmed live in `explorer/VoiceCommandFamilyScreen.kt` and `explorer/ExplorerIndexScreen.kt:65,78`). Phase 11 extends that same family. The two hardest, most load-bearing facts this research surfaces, both verified directly against source:

1. **The naming guard is live and it will reject the obvious names.** `DomainVocabularyDriftGuardTest.kt` enforces that a composable's head token (its leading PascalCase word) must be in `PRIMITIVE_NOUN_ALLOWLIST` or individually grandfathered in `DOMAIN_VOCABULARY`. Neither list currently contains `Outcome`, `Command`, `Clarification`, or `Undo` as a *head* token context that would cover a new top-level `VoiceOutcomeSheet`-style name — `Voice` itself is present ONLY as grandfathered `DOMAIN_VOCABULARY` (for pre-existing `VoiceCard`/`VoiceRenameTagsSheet`), and Phase 10 explicitly avoided reusing it: `ProviderKeyCard`/`ModelSelectCard`/`ApproachLadderCard` all use head tokens (`Provider`, `Model`, `Approach`) that Phase 10 added fresh to `PRIMITIVE_NOUN_ALLOWLIST` rather than reaching for `Voice*` names. Phase 11 must do the same: pick head tokens for the new composables that are either already allowlisted or added to `PRIMITIVE_NOUN_ALLOWLIST`/`DOMAIN_VOCABULARY` with a rationale, in the same commit that introduces them.
2. **The undo model this phase must extend additively already exists, and its current shape is NOT presentational-ready.** `UndoHistoryEntry`'s primary constructor is `internal`, carries a `suspend () -> Unit` lambda and a private `AtomicBoolean`, and is a `data class` (`feedback/UndoHistoryEntry.kt:47-54`). None of that can be a Compose prop type as-is. The phase needs a new, public, all-`val`, `STABLE`-inferable projection model (mirroring the `TagChipUiModel`/`ApproachRungUiModel` convention already used for every other new model in this milestone) that the consumer derives FROM `UndoHistoryEntry` — never a change to `UndoHistoryEntry`'s own constructor shape (that would risk exactly the `copy()` ABI break `TagChipUiModel.kt`'s KDoc documents, verified below).

**Primary recommendation:** Author `VoiceOutcomeUiState` (or an equivalently domain-neutral sealed type) as a NEW public sealed interface in `model/`, with `Success` (carrying optional undo + optional editable payload) and `Failure` (carrying optional action slot) arms only — leave `NeedsConfirmation` for Phase 12. Author a new, all-`val`, public `UndoRowUiModel`/`UndoGroupUiModel`-shaped projection in `model/` that a consumer builds from its own `UndoHistoryEntry` list (never a shape change to `UndoHistoryEntry` itself). Author the clarification-choices composable as a small, separate, registrable composable (not nested inside the outcome sheet's sealed state) so its "chip bar vs bottom-sheet-state" swap stays free, per the user's explicit discretion grant. Settle every new public composable's head-token naming against both drift guards BEFORE writing the first line of implementation — this is cheaper to fix in planning than after `v2.4.0` is tagged.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Outcome rendering (success/failure, from props) | Browser/Client (Compose UI, this library) | — | Pure presentation; the engine/consumer computes the outcome, this library only renders it (INV-01) |
| "Handled by" provenance data (tier/approach/provider/model/escalation count) | Consumer app (owns `CommandTrace` mapping) | Library (renders the `HandledByUiModel` it's given) | The engine's `TEL-01 CommandTrace` lives in `voice-action-engine`, a peer hub this library must never import (L7); consumer maps it to the UI model |
| Undo execution (the actual mutation reversal) | Consumer app / `UndoHistoryStore.attemptUndo` (existing, in this library but driven by app-supplied `undoAction` lambdas) | — | `UndoHistoryStore` already lives in this library (`feedback/`) as the shared first-consumer-wins CAS store; this phase's job is a presentational projection over it, not new execution logic |
| Undo-group accounting ("Undo all (N)", per-item unavailable) | Library (derives group membership/count from props or from an app-supplied group id on the entry) | Consumer (decides which entries belong to a group, and marks non-restorable ones `Unavailable`) | Display + emit only, mirroring the `ApproachLadderCard` derived-state precedent (`rung.enabled && …` computed IN the composable from plain primitives) |
| Clarification resolution (mapping a tapped option id back to the engine's `Clarification`) | Consumer app | Library (renders options, emits the tapped opaque id via `onSelect`) | Library never knows what the `id` means — L7, no engine dependency |
| Registry/gallery wiring | Library (`explorer/VoiceCommandFamilyScreen.kt`, denylisted from the drift guard) | — | Already the established pattern for this family |

## Standard Stack

### Core

No new external library dependency is introduced by this phase — everything is built from Jetpack Compose/Material3 primitives and this library's own existing components. Per INV-01 and the milestone PITFALLS.md Pitfall 5, adding any dependency to `build.gradle.kts` for this phase would itself be a defect.

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Jetpack Compose / Material3 | Compose BOM 2026.04.01 (already pinned, `CLAUDE.md` root) | Sealed-state rendering, `Surface`/`Column`/error color roles | Already the library's only UI toolkit; no alternative considered |
| Kotlin coroutines (`kotlinx.coroutines`) | Already a transitive dependency via `UndoHistoryStore`'s `suspend` API | The undo projection must remain compatible with `UndoHistoryEntry.undoAction: suspend () -> Unit` | Existing convention; this phase reads, never redefines, the coroutine shape |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `SheetScaffold` (`component/SheetScaffold.kt`) | in-repo | Host chrome for the outcome sheet if it ships as a `ModalBottomSheet` | Whenever the sheet needs to be a bottom sheet rather than an inline card — see Open Questions for the "sheet vs. inline surface" call |
| `AppChip` (`component/AppChip.kt`) | in-repo | "Handled by" indicator chip; clarification-choice chip option | Any compact labeled/pressable token; already supports `onLongClick`/`onDoubleClick`/`leadingIcon` |
| `DynamicActionButton` (`component/DynamicActionButton.kt`) | in-repo | Failure's optional action slot ("Open Settings"/"Retry"); Undo-all action | `Destructive`/`Save`/`Neutral` role-to-color mapping already exists; reuse rather than hand-roll a colored button |
| `SegmentedOptionSelector` (`component/SegmentedOptionSelector.kt`) | in-repo | Reference precedent ONLY (2-option toggle) — not a direct fit for an N-option clarification list, but its "always-visible reason, never conditionally hidden" discipline is the pattern to copy | Read for convention, not reused verbatim (VCLAR-01 needs an arbitrary-length option list, not exactly 2) |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| A new `UndoHistoryEntry`-shape change (adding a `groupId` field to its primary constructor) | A separate, new, public `UndoRowUiModel` that a consumer constructs FROM its own `UndoHistoryEntry` reads | The direct-field-add is the more "obvious" extension but risks the exact `copy()`/`componentN()` ABI break `TagChipUiModel.kt`'s KDoc documents (verified below) — a new projection type is the additive-safe choice and costs nothing in expressiveness since the library never constructs `UndoHistoryEntry` itself outside `UndoHistoryStore.append` (ctor is `internal`, per `feedback/UndoHistoryEntry.kt:47`) |
| `AttentionCue` for the loud-failure surface | A dedicated error-container `Surface` + icon + headline inside the sheet body | `AttentionCue`'s own KDoc states it is "**never** a failure signal" (`component/AttentionCue.kt:29`, verified) — using it would violate the component's own documented contract |
| A separate `NeedsConfirmation`-shaped sibling composable now, "to save a round-trip later" | Ship only `Success`/`Failure` arms in the sealed type this phase; let Phase 12 add the third arm | Explicitly deferred per CONTEXT.md; adding it now duplicates Phase 12's design-room work and risks guessing the shape wrong before the Phase-12-specific dual-consumer validation happens |

**Installation:** None — no new Gradle dependency for this phase.

## Package Legitimacy Audit

**Not applicable.** This phase introduces zero new external packages (npm/PyPI/Maven/etc.). Every new symbol is authored in-repo against the existing Compose/Material3/Kotlin-coroutines dependency set already declared in `build.gradle.kts`. The Package Legitimacy Gate is a no-op for this phase; verify at Phase 13 (the ship gate) that `build.gradle.kts` gained no new `implementation(...)` line, per the milestone PITFALLS.md Pitfall 5.

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│ CONSUMER APP (SecondBrain / CalTracker)                                  │
│                                                                           │
│  engine outcome / CommandTrace / UndoHistoryEntry group ──┐              │
│                                                            │ maps        │
│                                                            ▼              │
│                                            ┌───────────────────────────┐ │
│                                            │  call site                │ │
│                                            │  builds:                 │ │
│                                            │  - VoiceOutcomeUiState    │ │
│                                            │    (Success/Failure)     │ │
│                                            │  - HandledByUiModel       │ │
│                                            │  - UndoRowUiModel list    │ │
│                                            │  - ClarificationOption    │ │
│                                            │    list                  │ │
│                                            └─────────────┬─────────────┘ │
└──────────────────────────────────────────────────────────┼──────────────┘
                              one-way dependency (INV-01)    │ props
                                                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ yahirandroidtaste LIBRARY                                                 │
│                                                                           │
│  model/ (new, all-val)         component/ (new, flat)                    │
│  ┌──────────────────────┐      ┌────────────────────────────────────┐  │
│  │ VoiceOutcomeUiState   │      │ (outcome sheet composable)          │  │
│  │  ├─ Success           │─────▶│   when(state) {                     │  │
│  │  │   (undo?, edit?)   │      │     Success -> … + undo row/all     │  │
│  │  └─ Failure           │      │     Failure -> loud error surface   │  │
│  │      (action?)        │      │              + optional action     │  │
│  │ HandledByUiModel      │      │   }                                 │  │
│  │ UndoRowUiModel        │      │ (clarification choices composable)  │  │
│  │ ClarificationOption   │      │   question + pressable option chips │  │
│  └──────────────────────┘      └───────────────┬────────────────────┘  │
│                                                  │ reuses                │
│                                  ┌───────────────▼────────────────────┐ │
│                                  │ SheetScaffold, AppChip,             │ │
│                                  │ DynamicActionButton,                │ │
│                                  │ theme/Color.kt error roles          │ │
│                                  └──────────────────────────────────────┘│
│                                                                           │
│  feedback/ (existing, UNCHANGED shape)         explorer/ (denylisted)    │
│  ┌──────────────────────┐                      ┌───────────────────────┐│
│  │ UndoHistoryStore      │ ◀── consumer reads ──│ voiceCommandFamily-   ││
│  │ UndoHistoryEntry      │    .entries, builds  │ Entries += new         ││
│  │ (internal ctor,       │    UndoRowUiModel     │ Entry(...) per        ││
│  │  unchanged)           │    projection         │ new composable        ││
│  └──────────────────────┘                      └───────────────────────┘│
└─────────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Implementation |
|-----------|----------------|-----------------|
| New outcome/failure composable (name TBD — see Pitfall 1) | Render `Success`/`Failure` from a sealed state prop; loud failure; handled-by indicator; undo-all + per-item undo | `@Composable`, prop-driven, likely hosted in `SheetScaffold` or an equivalent card surface — host/content split precedent applies |
| New clarification-choices composable (name TBD — see Pitfall 1) | Render a question + N pressable options; `onSelect(id)`; `onDismiss` | Small standalone `@Composable`, NOT nested inside the outcome sheet's sealed state (keeps the "chip bar vs sheet state" swap free) |
| `model/VoiceOutcomeUiState.kt` (new) | Sealed `Success`/`Failure` shape, additive-ready for Phase 12's `NeedsConfirmation` | Sealed interface, all-`val` data classes, no `var` |
| `model/HandledByUiModel.kt` (new) | Required tier label + optional approach/provider/model/escalation count (D-04) | All-`val` data class |
| `model/UndoRowUiModel.kt` (new, name TBD) | Presentational projection of one undo-able row (message, id, `Available`/`Undone`/`Failed`/`Unavailable(reason)`), built by the CONSUMER from its own `UndoHistoryEntry` reads | All-`val` data class — never touches `UndoHistoryEntry`'s own constructor |
| `model/ClarificationOptionUiModel.kt` (new, name TBD) | `{ id: String, label: String }` per D-07 | All-`val` data class |

## Recommended Project Structure

```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/                              # FLAT — matches all 60+ existing components
│   ├── ProviderKeyCard.kt                 # Phase 10 (existing)
│   ├── ModelSelectCard.kt                 # Phase 10 (existing)
│   ├── ApproachLadderCard.kt              # Phase 10 (existing)
│   ├── <OutcomeSheetName>.kt              # Phase 11 NEW — Success/Failure sealed-state renderer
│   └── <ClarificationChoicesName>.kt      # Phase 11 NEW — question + pressable options
├── model/
│   ├── ApproachRungUiModel.kt             # Phase 10 (existing) — the all-val convention to mirror
│   ├── KeyFieldState.kt                   # Phase 10 (existing) — sealed-interface convention to mirror
│   ├── VoiceOutcomeUiState.kt             # Phase 11 NEW — sealed Success/Failure (Phase 12 adds NeedsConfirmation)
│   ├── HandledByUiModel.kt                # Phase 11 NEW
│   ├── UndoRowUiModel.kt                  # Phase 11 NEW — name TBD, presentational undo-row projection
│   └── ClarificationOptionUiModel.kt      # Phase 11 NEW — name TBD
└── explorer/
    └── VoiceCommandFamilyScreen.kt         # Phase 10 (existing) — Phase 11 APPENDS two new Entry(...) blocks here
```

### Structure Rationale

- **Flat `component/`, unchanged family:** Phase 10 already established the tenth registry family (`ExplorerFamilies.VOICE_COMMAND`, verified live at `explorer/ExplorerIndexScreen.kt:65,78`) and the `voiceCommandFamilyEntries` list in `explorer/VoiceCommandFamilyScreen.kt:43-132`. Phase 11 appends to that same file/list — no new family, no new sub-package.
- **`UndoRowUiModel` is a NEW type, not an edit to `UndoHistoryEntry`:** `UndoHistoryEntry`'s primary constructor is `internal` (`feedback/UndoHistoryEntry.kt:47`) specifically so only `UndoHistoryStore.append` can construct a fresh entry — "Callers in other modules can only obtain instances via the store and can only transition status via `withStatus`" (`feedback/UndoHistoryEntry.kt:34-39`, quoted verbatim). A consumer-facing Compose prop type must be a different, public, all-`val` class the consumer builds by reading `UndoHistoryStore.entries` and `UndoHistoryEntry.status`/`.message`/`.id` — never a constructor change to `UndoHistoryEntry` itself.

## Architectural Patterns

### Pattern 1: Sealed outcome state, Success/Failure only this phase

**What:** `VoiceOutcomeUiState` as a sealed interface with exactly two arms this phase (`Success`, `Failure`); Phase 12 adds `NeedsConfirmation` as a third, purely additive arm.
**When to use:** Any time the sheet needs to render one of several mutually-exclusive outcome shapes from a single prop.
**Example:**
```kotlin
// New file: model/VoiceOutcomeUiState.kt
// Pattern verified against this repo's existing sealed-interface convention,
// e.g. model/KeyFieldState.kt:10-30 (data object / data class arms, all-val).
sealed interface VoiceOutcomeUiState {

    data class Success(
        val summary: String,
        val handledBy: HandledByUiModel? = null,
        val undo: UndoAffordanceUiModel? = null,   // D-02: undo lives ON Success, not top-level
        val editablePayload: EditablePayloadUiModel? = null // D-05: committed-but-editable (CT)
    ) : VoiceOutcomeUiState

    data class Failure(
        val reason: String,
        val handledBy: HandledByUiModel? = null,
        val action: FailureActionUiModel? = null    // D-08: optional label+callback slot
    ) : VoiceOutcomeUiState

    // Phase 12 adds, purely additively:
    // data class NeedsConfirmation(val request: ConfirmRequestUiModel) : VoiceOutcomeUiState
}
```
This mirrors the milestone ARCHITECTURE.md's Pattern 1 exactly, adjusted to fold D-02's undo-on-Success placement and D-05's editable-payload requirement into the `Success` arm, and D-08's optional action slot into the `Failure` arm.

### Pattern 2: Undo group as a presentational projection, not a store change

**What:** A new `UndoAffordanceUiModel` (or similarly named) carrying `allLabel: String` (e.g. "Undo all (3)"), `rows: List<UndoRowUiModel>`, `onUndoAll: () -> Unit`, and each row carrying its own `onUndo: () -> Unit` or an `Unavailable(reason)` marker — built by the CONSUMER from its own read of `UndoHistoryStore.entries` (or an app-side equivalent), never by the library reaching into the store itself.
**When to use:** Whenever the sheet needs to render "Undo all (N)" + per-item undo + an unavailable/refused state.
**Trade-offs:** Requires the consumer to do a small mapping step (group membership, "which items count toward N") — but this keeps the library from ever depending on `UndoHistoryStore`'s internal CAS/first-consumer-wins machinery for its *rendering* concerns, and keeps `UndoHistoryEntry`'s `internal` constructor and `AtomicBoolean` untouched (per D-01's own "Plan-time tension" note).
```kotlin
// New file: model/UndoAffordanceUiModel.kt — illustrative shape, exact field names are a planning decision
data class UndoAffordanceUiModel(
    val allLabel: String,               // e.g. "Undo all (3)" — consumer formats the count
    val rows: List<UndoRowUiModel>,
    val onUndoAll: (() -> Unit)? = null, // null hides "Undo all" entirely (D-05 hideable-by-null-prop convention)
    val refused: UndoRefusedUiModel? = null  // loud undo-refused/partial state, nested per D-02
)

data class UndoRowUiModel(
    val id: String,
    val label: String,
    val state: UndoRowState
)

sealed interface UndoRowState {
    data object Available : UndoRowState
    data object Undone : UndoRowState
    data class Unavailable(val reason: String) : UndoRowState  // entangled-with-another-item case
}

data class UndoRefusedUiModel(
    val reason: String,
    val changedItem: String? = null   // D-01/CT: OPTIONAL — CT's create-only undo has no restore payload
)
```

### Pattern 3: Clarification choices as a standalone composable

**What:** A dedicated composable taking `question: String`, `options: List<ClarificationOptionUiModel>` (`{id, label}`), `onSelect: (String) -> Unit`, `onDismiss: () -> Unit` — NOT nested as an arm of `VoiceOutcomeUiState`.
**When to use:** Whenever the model needs the user to disambiguate via tap rather than re-speaking.
**Trade-offs:** CONTEXT.md D-07 explicitly grants "your design call, easy to swap" for chips-vs-buttons and bar-vs-sheet-state — building it as its own composable (rather than a sealed-state arm) preserves that swappability; folding it into the outcome sheet's `when` would couple its visual form to the sheet's own chrome decisions.
```kotlin
// New file: model/ClarificationOptionUiModel.kt
data class ClarificationOptionUiModel(
    val id: String,     // opaque — the library never interprets it
    val label: String
)
```

### Anti-Patterns to Avoid

- **Using `AttentionCue` for the loud-failure surface.** Its own KDoc: *"A caution/verify signal glyph — **never a failure signal**. `tint` defaults to `MaterialTheme.colorScheme.tertiary` and must never be wired to the error color role"* (`component/AttentionCue.kt:28-30`, verified/quoted). Use the `theme/Color.kt` error roles directly instead (see Code Examples).
- **Changing `UndoHistoryEntry`'s primary constructor to add a group id.** Its ctor is `internal` and its fields are already carefully frozen (`suspend () -> Unit`, `AtomicBoolean`) — any primary-constructor edit to a `data class` regenerates `copy()`/`componentN()` over the full field list as one non-overloadable signature, the exact `TagChipUiModel.kt` lesson (quoted in Pitfalls below). Build a new presentational type instead.
- **Reaching for a `Voice*`/`Command*`/`Outcome*` head token without checking both drift guards first.** See Pitfall 1 — this is the single highest-probability rework trigger for this phase.
- **Folding the clarification-choices composable into the outcome sheet's sealed `when`.** Defeats D-07's explicit swappability grant and couples an unrelated visual decision (chips vs. sheet-state) to the outcome sheet's own chrome.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Compact labeled pressable token (handled-by chip, a clarification option) | A bespoke `Row` + `Surface` + `clickable` | `AppChip` (`component/AppChip.kt:88-175`, verified) | Already handles 48dp touch target, selected/unselected color roles, long-press/double-click, `containerColorOverride` — reinventing it duplicates ~90 lines of already-hardened code |
| Role-colored action button (failure's optional action, "Undo all") | A hand-rolled `TextButton`/`Button` with inline color logic | `DynamicActionButton` (`component/DynamicActionButton.kt:37-58`, verified) | `Destructive`/`Save`/`Neutral` → M3 color mapping already exists and is the established convention (its own KDoc: "generalizing the hand-rolled `TextButton`/filled-`Button` split") |
| Bottom-sheet chrome (drag handle, ime padding, window insets) | A raw `ModalBottomSheet` call | `SheetScaffold` (`component/SheetScaffold.kt:50-69`, verified) | "the single shared implementation for all `ModalBottomSheet` call sites app-wide" per its own KDoc; re-implementing risks the documented double-ime-padding bug it exists to prevent |
| Swipe-reveal-then-undo gesture (if a per-row swipe-to-undo interaction is wanted) | A new gesture wrapper | `SwipeableActionRow` or `RevealActionRow` (`modifier/`, verified) | Both already implement the reveal-then-confirm contract with tested threshold haptics; a third gesture wrapper would be a second implementation of the same contract |

**Key insight:** Every visual primitive this phase needs (chip, role-colored button, sheet chrome) already exists in this library. The genuinely new work is the **data shape** (`VoiceOutcomeUiState` and its undo/clarification satellite models) and the **naming clearance** against the two drift guards — not new widgets.

## Runtime State Inventory

Not applicable — this is a greenfield additive phase (new composables + new models in an existing library), not a rename/refactor/migration. No existing runtime state (stored data, live service config, OS-registered state, secrets, build artifacts) is being renamed or relocated.

## Common Pitfalls

### Pitfall 1: A `Voice*`/`Outcome*`/`Command*`/`Clarification*` head token will fail `DomainVocabularyDriftGuardTest` unless pre-cleared

**What goes wrong:** A composable is named, e.g., `VoiceOutcomeSheet` or `ClarificationChoiceBar`, compiles fine, passes `ComponentRegistryDriftGuardTest` (it IS registered), and then fails `DomainVocabularyDriftGuardTest` in the full suite because its head token (`Voice`/`Clarification`) is in neither `PRIMITIVE_NOUN_ALLOWLIST` nor `DOMAIN_VOCABULARY`.

**Why it happens:** The milestone's own architecture research (written before Phase 10 executed) proposed `VoiceOutcomeSheet`/`VoiceProviderSettingsCard`-style names — but Phase 10's actual implementation deliberately avoided `Voice*` entirely (`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard` — confirmed live in `explorer/VoiceCommandFamilyScreen.kt:43-131`), and the test file's own comment confirms why: `Voice` exists in `DOMAIN_VOCABULARY` ONLY as a day-one grandfather entry for the two pre-existing `VoiceCard`/`VoiceRenameTagsSheet` composables (`src/test/.../DomainVocabularyDriftGuardTest.kt:336-341`, verified), not as a generally-available prefix. Separately, Phase 10 WIDENED `PRIMITIVE_NOUN_ALLOWLIST` with `"Provider", "Model", "Approach"` specifically so its new cards would pass (`src/test/.../DomainVocabularyDriftGuardTest.kt:320-326`, verified) — establishing the precedent that a new settings/outcome-surface phase is expected to widen this list deliberately, not reach for a grandfathered domain word.

**How to avoid:**
- Before authoring, check every candidate composable name's head token against the verified current list: `PRIMITIVE_NOUN_ALLOWLIST` = `{Control, Sheet, Field, Canvas, State, Dialog, Bar, Card, Value, Item, Content, Row, Scaffold, Swatch, Grid, View, Chip, Popup, Picker, Button, Fab, Badge, Selector, Ring, Menu, Overlay, Preview, Cue, Editor, Base, Screen, Theme, Ladder, Showcase, Accent, Adaptive, Animated, App, Attention, Bulk, Clearable, Confirmation, Count, Crop, Cycle, Dynamic, Elevation, Empty, Expandable, Filter, Gradient, Hero, Icon, List, Metric, Name, Progress, Segmented, Sort, Tactile, Text, Undo, Date, Preset, Provider, Model, Approach}` (`src/test/.../DomainVocabularyDriftGuardTest.kt:297-326`, verified/quoted in full).
- `Outcome`, `Handled`, `Clarification` are NOT currently in either list. The planner must either (a) choose a head token already in the list (e.g. name the sheet so its leading word is `Card`/`Sheet`/`Content`/`Badge`/etc.), or (b) add the new word(s) to `PRIMITIVE_NOUN_ALLOWLIST` with a one-line rationale in the SAME commit that introduces the composable — mirroring Phase 10's `Provider`/`Model`/`Approach` widening exactly. `Outcome` and `Clarification` both read as generic, domain-agnostic UI-result/UI-disambiguation nouns (no more consumer-specific than `Confirmation`, already allowlisted) and are reasonable widening candidates, but this is a naming decision for the plan to make explicitly, not an assumption to carry forward silently.
- `Undo` is ALREADY in `PRIMITIVE_NOUN_ALLOWLIST` (line 314, verified) — a composable or sub-part named e.g. `UndoRow`/`UndoAllButton` needs no allowlist change.
- Remember this is a head-token (leading word) check, not a full-name check — `ClarificationBar` and `OutcomeCard` are single new head tokens to clear, not two separate problems per word.
- Sub-parts that stay `private`/`internal` are invisible to this guard entirely (it only scans public top-level composables) — if a piece doesn't need its own gallery tile, keep it private and the naming question disappears for that piece.

**Warning signs:** `DomainVocabularyDriftGuardTest` going RED in a full-suite run naming the new composable(s); a plan that names composables without cross-checking the current allowlist/grandfather-map contents.

**Phase to address:** Phase 11 (this phase) — settle the naming vocabulary in the plan, before implementation. This is per-composable discipline, same as the milestone-level PITFALLS.md Pitfall 1, but concretely actionable now because Phase 10's actual choices are known (not just the pre-Phase-10 architecture doc's guesses).

### Pitfall 2: Extending `UndoHistoryEntry`'s shape instead of building a presentational projection

**What goes wrong:** The "obvious" way to add group/run-level undo support is to add a `groupId: String?`/`groupStatus: …?` field to `UndoHistoryEntry`'s primary constructor. This compiles, looks additive (new field is nullable/defaulted), and then breaks `apiCheck`/Metalava because Kotlin regenerates `copy()`/`componentN()` over the FULL primary-constructor parameter list as one non-overloadable signature — the exact, already-documented `TagChipUiModel` lesson.

**Why it happens:** `UndoHistoryEntry` already has 7 constructor parameters (`id, message, timestamp, undoAction, preview, status, consumedGuard` — `feedback/UndoHistoryEntry.kt:47-54`, verified) and has grown additively before (`preview` was added this way, per its own KDoc: *"[preview] (Phase 58, UNDO-04, D-03) is an optional snapshot payload... Nullable/defaulted so every existing producer keeps compiling unchanged"*, `feedback/UndoHistoryEntry.kt:41-45`). That prior success can make a 9th-parameter addition look safe by analogy — but `TagChipUiModel.kt`'s own KDoc is explicit that a **6th** parameter addition was REJECTED for exactly this reason and fixed by moving the new field OUTSIDE the primary constructor as a body `var` (`model/TagChipUiModel.kt:38-56`, verified/quoted): *"adding a 6th primary-ctor parameter would have made `copy()`'s old 5-arg overload a genuine, unfixable API removal (`api.txt` `RemovedMethod`) — exactly the non-additive ABI break Phase 07 code review flagged... and the repo owner ruled must be a code fix, not an accepted break."* Whether `UndoHistoryEntry`'s own prior `preview` addition was itself safe is a fact about its OWN ABI history (it was pre-`v2.3.0`, so no external consumer had yet pinned the old arity) — but `v2.3.0` is the CURRENT frozen baseline for this milestone's `apiCheck`, so any further constructor-arity change now risks the identical break `TagChipUiModel` already hit.

**How to avoid:**
- Do not touch `UndoHistoryEntry`'s primary constructor. Build a new, separate, public, all-`val` model (e.g. `UndoRowUiModel`/`UndoAffordanceUiModel`) that the CONSUMER constructs by reading `UndoHistoryStore.entries.value` and mapping each `UndoHistoryEntry.id`/`.message`/`.status` into the new shape, plus whatever the consumer's own group-membership bookkeeping decides.
- If a genuine "does this entry belong to group X" concept is needed on the STORE side (not just presentationally), that is a decision for the plan to make explicitly and validate against SecondBrain's real `UndoCenterViewModel.kt` call site per D-03's cross-repo validation requirement — not something to default into without that check.
- Verify additivity locally before proposing any model change: `./gradlew apiCheck` against the `v2.3.0` baseline, plus the repo's `verify-api-additive.sh`/`verify-additive-surface.sh` tooling (per root `CLAUDE.md` and the milestone PITFALLS.md Pitfall 3).

**Warning signs:** Any diff touching `UndoHistoryEntry.kt`'s primary constructor parameter list; `apiCheck` reporting `RemovedMethod`/`ChangedType` on `UndoHistoryEntry.copy`/`componentN`.

**Phase to address:** Phase 11 (author additively) — Phase 13 is the hard backstop (`apiCheck` gate) but a break authored here and caught there costs a Phase-11 rework.

### Pitfall 3: Rendering failure quietly because the "no dead space" convention is misread

**What goes wrong:** An unhandled/failure branch of the outcome `when` renders an empty `Box`/nothing, or failure is styled as muted body text rather than an error-emphatic surface — directly violating VOUT-03 and the repo owner's documented "loud failures" UX rule.

**Why it happens:** This library's `conditional-render-no-dead-space` convention (used correctly elsewhere — e.g. `ApproachLadderCard`'s null-prop-hides-the-control pattern, verified at `component/ApproachLadderCard.kt:58-61`) can be misapplied to failure, which is PRESENT content, not absent optional content.

**How to avoid:** Use the verified theme error roles directly — `ErrorRed`/`OnErrorRed`/`ErrorRedContainer`/`OnErrorRedContainer` (light) and their `*Dark` counterparts (`theme/Color.kt:36-43`, verified/quoted: `val ErrorRed = Color(0xFFBA1A1A)`, `val ErrorRedContainer = Color(0xFFFFDAD6)`, `val OnErrorRedContainer = Color(0xFF410002)`, plus `*Dark` variants). In practice, call sites should reach for `MaterialTheme.colorScheme.error`/`.errorContainer`/`.onErrorContainer` (the theme roles these raw values feed — confirm the exact M3 `ColorScheme` wiring in `theme/Theme.kt`/`theme/Color.kt`'s surrounding file if not already exposed) rather than the raw `Color` constants, matching the existing convention seen in `RecordingBottomSheetContent.kt`'s Stop button (`MaterialTheme.colorScheme.errorContainer`/`.onErrorContainer`, verified at `component/RecordingBottomSheetContent.kt:244-246`) and `SwipeableActionRow.kt`'s Delete slot (`MaterialTheme.colorScheme.errorContainer`, verified at `modifier/SwipeableActionRow.kt:278`).

**Warning signs:** A `when` branch with no visible content for `Failure`; failure text at `bodyMedium`/neutral color with no icon/headline.

**Phase to address:** Phase 11 (owns VOUT-03). Verify on-device via Gate-1 self-UAT driving the failure branch explicitly, not just success.

## Code Examples

### Loud failure surface, reusing verified error color roles

```kotlin
// Illustrative — exact composable name pending the Pitfall-1 naming decision.
// Verified theme roles: theme/Color.kt:36-43 (ErrorRed/ErrorRedContainer/OnErrorRedContainer + *Dark),
// confirmed usage convention: component/RecordingBottomSheetContent.kt:244-246 (errorContainer/onErrorContainer)
@Composable
private fun FailureBody(
    failure: VoiceOutcomeUiState.Failure,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = failure.reason, style = MaterialTheme.typography.titleMedium)
            failure.handledBy?.let { HandledByRow(it) }
            failure.action?.let { action ->
                DynamicActionButton(
                    label = action.label,
                    role = ActionButtonDefaults.ActionButtonRole.Neutral,
                    onClick = action.onClick
                )
            }
        }
    }
}
```

### Undo-all + per-item undo, reusing `DynamicActionButton` and the null-prop-hides convention

```kotlin
// Mirrors ApproachLadderCard's "hideable-by-null-prop, never shown-disabled" convention
// (component/ApproachLadderCard.kt:58-61, verified KDoc: "Every control is hideable-by-null-prop,
// never shown-disabled").
@Composable
private fun UndoAffordanceBody(undo: UndoAffordanceUiModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        undo.onUndoAll?.let { onUndoAll ->
            DynamicActionButton(
                label = undo.allLabel,
                role = ActionButtonDefaults.ActionButtonRole.Neutral,
                onClick = onUndoAll
            )
        }
        undo.rows.forEach { row ->
            UndoRowItem(row)
        }
        undo.refused?.let { refused ->
            // Loud, per Pitfall 3 — error container, not a muted caption.
            Surface(color = MaterialTheme.colorScheme.errorContainer) {
                Text("Couldn't undo: ${refused.reason}" +
                    (refused.changedItem?.let { ", $it changed since" } ?: ""))
            }
        }
    }
}
```

### Clarification choices, compact pressable option list reusing `AppChip`

```kotlin
// AppChip verified at component/AppChip.kt:88-175 — reused here as the "pressable option" surface
// per D-07's chips-vs-buttons discretion grant.
@Composable
private fun ClarificationOptionBar(
    question: String,
    options: List<ClarificationOptionUiModel>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(question, style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                AppChip(
                    label = option.label,
                    isSelected = false,
                    onClick = { onSelect(option.id) }
                )
            }
        }
        TextButton(onClick = onDismiss) { Text("Dismiss") }
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|---------------|--------|
| Milestone architecture doc's `VoiceOutcomeSheet`/`VoiceProviderSettingsCard` naming proposal | `ProviderKeyCard`/`ModelSelectCard`/`ApproachLadderCard` (no `Voice*` prefix) | Phase 10 execution (2026-09-30, this session) | Phase 11's composable names must follow the SAME pattern — avoid `Voice*`/other ungrandfathered domain words; the milestone-level ARCHITECTURE.md's exact proposed names are now stale for this reason |
| `PRIMITIVE_NOUN_ALLOWLIST` as a fixed 34-word seed | A living, phase-by-phase widened allowlist (Phase 10 added `Provider`/`Model`/`Approach`) | Ongoing, this milestone | Phase 11 is expected to widen it again for its own new head tokens, following the established precedent rather than treating the list as frozen |

**Deprecated/outdated:** None specific to this phase beyond the naming-proposal drift noted above.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `Outcome` and `Clarification` are reasonable candidates to ADD to `PRIMITIVE_NOUN_ALLOWLIST` (rather than reaching for a `DOMAIN_VOCABULARY` grandfather entry) | Pitfall 1 | Low — this is presented as a recommendation for the plan to make explicitly, not asserted as already-decided; if the planner disagrees, any allowlisted head token works equally well structurally |
| A2 | `MaterialTheme.colorScheme.error`/`.errorContainer`/`.onErrorContainer` are wired to the verified `ErrorRed`/`ErrorRedContainer`/`OnErrorRedContainer` raw values in `theme/Color.kt` via the app's `ColorScheme` builder (not re-read in `theme/Theme.kt` this session) | Common Pitfalls, Code Examples | Low — even if the exact M3 role name differs, the existing verified call sites (`RecordingBottomSheetContent.kt:244-246`, `SwipeableActionRow.kt:278`) already use `MaterialTheme.colorScheme.errorContainer`/`.onErrorContainer` directly, so the pattern is confirmed correct by usage even without re-reading the `ColorScheme` builder itself |
| A3 | A new `UndoRowUiModel`/`UndoAffordanceUiModel`-shaped presentational projection (built by the consumer from `UndoHistoryStore.entries`) is preferable to any store-side group-tracking change, absent the D-03 cross-repo reconvene's actual verdict | Architecture Patterns, Pitfall 2 | Medium — D-03 explicitly requires validating the undo shape against SecondBrain's and CalTracker's real call-sites at the A13 reconvene BEFORE authoring; if that reconvene produces a different verdict (e.g. the store itself needs a `groupId` concept), this recommendation must be revisited — this is flagged in CONTEXT.md as a cross-repo item, not fully resolved by this phase-level research |
| A4 | The clarification-choices composable should be a standalone composable rather than nested in the outcome sheet's sealed state | Architecture Patterns Pattern 3 | Low — explicitly grounded in CONTEXT.md D-07's "your design call, easy to swap" language, but the FINAL visual form (chips vs buttons, bar vs sheet-state) is still an open design call for the plan/Gate-1 review, not settled here |

## Open Questions (RESOLVED)

1. **Exact head-token/composable names for the outcome sheet and clarification-choices composable.**
   - What we know: the naming MUST clear `DomainVocabularyDriftGuardTest` (head token in `PRIMITIVE_NOUN_ALLOWLIST` or `DOMAIN_VOCABULARY`) and `ComponentRegistryDriftGuardTest` (registered XOR allowlisted); Phase 10's precedent is to widen `PRIMITIVE_NOUN_ALLOWLIST` with fresh, genuinely-generic words rather than reuse `Voice*`.
   - What's unclear: the exact chosen name(s) — this is a planning/authoring decision, not something research should pre-decide.
   - Recommendation: the plan should pick names, check them against the verified current allowlist (reproduced in full in Pitfall 1), and add any new head token to `PRIMITIVE_NOUN_ALLOWLIST` with a rationale in the same commit — do not defer this to Phase 13.
   - **RESOLVED:** Names picked and the allowlist widened accordingly. 11-01-PLAN.md Task 1 names the sheet `OutcomeSheet` and widens `PRIMITIVE_NOUN_ALLOWLIST` with `"Outcome"` in the same commit; 11-02-PLAN.md Task 1 names the clarification composable `ClarificationBar` and widens the same allowlist with `"Clarification"` in its own commit — both mirror Phase 10's widening pattern exactly.

2. **Whether the undo-group concept needs any store-side change at all, per D-03's cross-repo validation.**
   - What we know: D-01/D-03 require validating the `Unavailable(reason)` + `Refused(reason, changedItem)` union against SecondBrain's and CalTracker's actual call-sites at the A13 reconvene before authoring.
   - What's unclear: whether that reconvene has happened, and what its verdict was — this session found no `.planning/cross-repo/RECONVENE-BRIEF.md` verdict artifact to confirm against (STATE.md shows the project still at "ready for cross-repo reconvene" as of its last update, predating Phase 11 planning).
   - Recommendation: the plan should either confirm the reconvene verdict exists and cite it, or explicitly flag this as an unresolved cross-repo dependency requiring a `checkpoint:human-verify` before the undo-shape models are frozen.
   - **RESOLVED:** The A13/R1 reconvene did happen and its verdict is folded into 11-CONTEXT.md's D-01 ("REVISED at R1 by SecondBrain... extend additively... presentational projection" — the store-side-change question is answered: no, extend via a new projection, never a store/`UndoHistoryEntry` shape change). 11-01-PLAN.md Task 2 (`checkpoint:decision`, blocking-human) cites D-01/D-03 and the RECONVENE-BRIEF verdict explicitly and gates the concrete Kotlin field-level shape (not the reconvene-happened-or-not question, which D-01 already settles) before Task 3 authors `UndoAffordanceUiModel`/`UndoRowUiModel`/`UndoRefusedUiModel`.

3. **Outcome sheet host: `SheetScaffold`-hosted bottom sheet, or an inline card surface?**
   - What we know: the milestone ARCHITECTURE.md recommends a host/content split mirroring `RecordingBottomSheetContent.kt`/`ListCardBottomSheet.kt`; CONTEXT.md doesn't lock this choice explicitly for Phase 11 (D-07 only grants discretion for the clarification composable's form, not the outcome sheet's).
   - What's unclear: whether "sheet" in "outcome/failure sheet" (the phase's own name) is meant literally (a `ModalBottomSheet`) or is just the phase's working title.
   - Recommendation: default to the `SheetScaffold` host/content split (matches the phase name and existing precedent) unless the plan finds a reason to diverge.
   - **RESOLVED:** Defaulted as recommended. 11-01-PLAN.md Task 1's action has `OutcomeSheet` call `SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier)` internally and render its exhaustive `when(outcome)` inside that scaffold's content — the host/content split, settled at authoring time, no divergence found.

## Environment Availability

No new external dependency, service, or CLI tool is introduced by this phase. The existing toolchain (AGP 9.2.1, Kotlin 2.3.20, Compose BOM 2026.04.01, JDK 17, Robolectric + Compose UI test + kotlinx-coroutines-test — all confirmed present in `build.gradle.kts`'s `testImplementation` block this session) is sufficient. No environment audit table is needed.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric + Compose UI test (`androidx.compose.ui.test.junit4`) + `kotlinx-coroutines-test` (confirmed in `build.gradle.kts` lines 126-134) |
| Config file | `build.gradle.kts` (module root; no separate test config file) |
| Quick run command | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.model.*"` (scope to new model/component test classes during development) |
| Full suite command | `./gradlew testDebugUnitTest` (required before declaring the phase done — this is what runs `ComponentRegistryDriftGuardTest` and `DomainVocabularyDriftGuardTest`, per the milestone PITFALLS.md Pitfall 4's explicit warning that these guards are full-suite-only) |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| VOUT-01 | Sheet renders `Success`/`Failure` from props, no app nouns | unit/Robolectric Compose test | `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"` | ❌ Wave 0 (new file) |
| VOUT-02 | "Handled by" indicator renders tier (+ optional fields) from props | unit/Robolectric Compose test | same file as above | ❌ Wave 0 |
| VOUT-03 | Failure renders with error color role, optional action slot renders iff prop present | unit/Robolectric Compose test | same file as above | ❌ Wave 0 |
| VUNDO-01 | "Undo all (N)" + per-item undo + `Unavailable`/`Refused` states render correctly | unit/Robolectric Compose test | `./gradlew testDebugUnitTest --tests "*UndoAffordance*"` | ❌ Wave 0 (new file) |
| VCLAR-01 | Clarification composable renders question + options; `onSelect`/`onDismiss` fire correctly | unit/Robolectric Compose test | `./gradlew testDebugUnitTest --tests "*Clarification*"` | ❌ Wave 0 (new file) |
| Naming-guard compliance (Pitfall 1) | Every new public composable's head token clears both drift guards | existing full-suite guard tests | `./gradlew testDebugUnitTest --tests "*DriftGuardTest*"` | ✅ (guards already exist; this phase must satisfy them, not create them) |
| API additivity | `v2.4.0`-in-progress diff vs `v2.3.0` stays additive | static/build-time | `./gradlew apiCheck` | ✅ (tooling exists) |

### Sampling Rate

- **Per task commit:** the scoped test command for the model/composable touched.
- **Per wave merge:** `./gradlew testDebugUnitTest` (full suite — required to catch the drift guards per Pitfall 4/Pitfall 1).
- **Phase gate:** full suite green (including both drift guards) before `/gsd-verify-work`; Gate-1 self-UAT must explicitly drive the Failure and undo-refused branches on-device, not just the happy path (per CONTEXT.md's "Specific Ideas" note and the milestone PITFALLS.md Pitfall 6).

### Wave 0 Gaps

- [ ] A new test file for the outcome/failure composable (name pending Pitfall 1's naming decision) — covers VOUT-01/02/03
- [ ] A new test file for the undo affordance rendering — covers VUNDO-01
- [ ] A new test file for the clarification-choices composable — covers VCLAR-01
- [ ] No new shared fixtures/conftest-equivalent needed — this repo's convention is per-family fixture functions declared directly in the `explorer/*FamilyScreen.kt` file (see `ProviderKeyCardFixture`/`ModelSelectCardFixture`/`ApproachLadderCardFixture` in `explorer/VoiceCommandFamilyScreen.kt:171-259`, verified) plus ordinary Robolectric Compose test rules per test file — no framework install needed.

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-------------------|
| V2 Authentication | No | This phase has no auth surface — it renders outcomes/undo/clarification from props only |
| V3 Session Management | No | No session concept in this library |
| V4 Access Control | No | No access-control surface; the library never gates access, only renders |
| V5 Input Validation | Marginal/Yes | Clarification `id` is an OPAQUE string the library never interprets or executes (D-07: "Apps map the engine's `Clarification` → these props; no engine dependency") — the library's only "validation" responsibility is to pass the tapped `id` back verbatim via `onSelect`, never to parse/execute it. No injection surface exists because no string from this phase is ever used to construct a query, command, or file path inside the library. |
| V6 Cryptography | No | No cryptographic material touched; the undo/outcome data is plain display state |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|----------------------|
| A consumer accidentally logs or displays a secret (e.g. an API key) inside a `Failure.reason` string | Information Disclosure | Out of this phase's direct control (the library only renders the string it's given) — but the library must not itself log any prop value anywhere (matches the milestone PITFALLS.md Security Mistakes table: "No logging of the key value") |
| A malformed/duplicate clarification option `id` causes `onSelect` to resolve the wrong engine-side clarification | Tampering (logic-level, not a security vulnerability per se) | The library renders `options` in the order given and calls `onSelect(option.id)` verbatim — uniqueness of `id` within one `options` list is the CONSUMER's responsibility to guarantee; document this in the new model's KDoc |

## Sources

### Primary (HIGH confidence — read directly this session)

- `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryEntry.kt` (full file) — current shape, `internal` ctor, `AtomicBoolean`, `withStatus`, `preview` addition precedent
- `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStore.kt` (full file) — `append`/`attemptUndo`/`clearSpent`/eviction, first-consumer-wins CAS
- `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoPreview.kt`, `FeedbackEvent.kt` — sibling shapes, `WithUndo` snackbar path
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/SheetScaffold.kt` (full file) — host chrome contract
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/AttentionCue.kt` (full file) — "never a failure signal" KDoc, quoted
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/RecordingBottomSheetContent.kt`, `ListCardBottomSheet.kt` (full files) — host/content split precedent, error-container usage convention
- `src/main/java/io/github/ygaray/yahirandroidtaste/theme/Color.kt` (full file) — verified `ErrorRed`/`ErrorRedContainer`/`OnErrorRedContainer` + dark variants
- `src/main/java/io/github/ygaray/yahirandroidtaste/modifier/SwipeableActionRow.kt`, `RevealActionRow.kt` (full files) — reveal-confirm gesture contract, error-container usage
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` (full file) — registry integrity invariants, `Entry` shape, `voiceCommandFamilyEntries` concatenation confirmed live
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt` (full file) — Phase 10's ACTUAL naming choices (`ProviderKeyCard`/`ModelSelectCard`/`ApproachLadderCard`), fixture-function convention
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` (full file) — `PRIMITIVE_NOUN_ALLOWLIST`/`DOMAIN_VOCABULARY` contents, verbatim quoted in Pitfall 1
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt` (full file) — the `copy()`/ABI-break lesson, verbatim quoted
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/ApproachRungUiModel.kt`, `KeyFieldState.kt` — all-`val`/sealed-interface conventions to mirror for new models
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/AppChip.kt`, `ChipBar.kt`, `DynamicActionButton.kt`, `SegmentedOptionSelector.kt`, `ApproachLadderCard.kt` (full/partial reads) — reusable pressable-token and role-button precedent
- `build.gradle.kts` (grep) — confirmed test dependency set (JUnit4, Robolectric, Compose UI test junit4, coroutines-test)
- `.planning/config.json` — confirmed `nyquist_validation: true`, `security_enforcement: true`, `security_asvs_level: 1`

### Secondary (MEDIUM confidence)

- `.planning/phases/11-voice-outcome-failure-sheet/11-CONTEXT.md`, `.planning/REQUIREMENTS.md`, `.planning/STATE.md`, `.planning/research/ARCHITECTURE.md`, `.planning/research/FEATURES.md`, `.planning/research/PITFALLS.md` — milestone-level research and locked decisions, all provided as required reading and treated as authoritative for scope/decisions (not re-verified against external sources, but internally consistent with the source code verified above)

### Tertiary (LOW confidence)

- None — no WebSearch/external lookups were needed for this phase; every technical claim resolves against in-repo source already read this session.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new dependency, everything reused is verified in-repo
- Architecture: HIGH — sealed-state and additive-model patterns verified against this repo's own existing conventions (`KeyFieldState`, `ApproachRungUiModel`, `TagChipUiModel`)
- Pitfalls: HIGH — both drift guards read in full; the naming trap (Pitfall 1) is the single most valuable, concretely-actionable finding of this research and is grounded in a live test file's exact current allowlist contents
- Cross-repo undo validation (D-03): MEDIUM/LOW — this phase-level research could not confirm whether the A13 reconvene verdict exists; flagged as Open Question 2 and Assumption A3

**Research date:** 2026-09-30
**Valid until:** Until Phase 10's `ProviderKeyCard`/`ModelSelectCard`/`ApproachLadderCard` land (if they change names before merge) or the A13 cross-repo reconvene produces a verdict on the undo shape — whichever comes first. Re-verify the drift-guard allowlist contents (Pitfall 1) immediately before authoring, since Phase 10 may still be mid-execution.
