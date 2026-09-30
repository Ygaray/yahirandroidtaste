# Phase 11: Voice outcome & failure sheet - Research (FORCE-REFRESH)

**Researched:** 2026-09-30
**Domain:** Additive extension of an existing in-memory undo state machine (`UndoHistoryStore`/`UndoHistoryEntry`) with an atomic, releasable group-claim + group-eviction API, plus a new standalone clarification-choices composable — inside a reusable Android Compose design-system library
**Confidence:** HIGH (every current-code claim below is grounded in source files read this session, cited with path + line range and quoted verbatim; the store-side grouping *mechanism* is original synthesis against the frozen D-01 contract — clearly separated and flagged below, not asserted as already-decided)

**Why this file was force-refreshed:** The prior `11-RESEARCH.md` (2026-09-30 15:13) was written *before* the R1-seam reconvene landed. `11-CONTEXT.md` D-01 was amended at 16:13 (commit `2e02abf`) with a fully-specified, SB-confirmed, FROZEN store-side grouping API (`openGroup`/grouped `append`/`group`/`groupIdOf`/`groupLabel`/`groupStatus`/`attemptUndoGroup` + `UndoGroupStatus`/`UndoGroupResult`) that the prior research's "build a presentational-only projection, never touch the store" recommendation does not account for. The prior 11-01/11-02 plans were retired to `superseded/` for exactly this reason (commit `d5d426b`). This research is grounded in the *current* frozen D-01 text and does the concrete signature-level design work the amendment defers to "resolve in the plan."

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01 [undo-shape].** REVISED at R1 by SecondBrain (the seam owner), then **FROZEN at the 2026-09-30 R1-seam reconvene, SB-confirmed via orchestrator.** Full frozen text (quoted in full — this is the authoritative field-level shape for the re-plan, freezing into `v2.4.0`):

  > **D-01 — R1-seam decision 2026-09-30, SB-confirmed via orchestrator (FROZEN VUNDO-01 seam).** The blocking-human checkpoint (11-01-PLAN Task 2) is resolved: `new-projection` (presentational models) PLUS an additive store-side grouping API. This is the authoritative field-level shape the re-plan must implement; it freezes into `v2.4.0`. **This supersedes the stale "notify-only mutations (e.g. SB edits) are excluded" clause above:** per Yahir's SB Phase-178 D-01, edits ARE undoable, so **"Undo all (N)" = the count of `Available` group members and INCLUDES SB edits.** `Unavailable(reason)` stays a generic per-item state for any action without an undo adapter — docs/tests MUST NOT assume edits are excluded.
  >
  > **Existing API is UNTOUCHED (no ABI break):** the `internal constructor`, the 3-arg `append(message, preview?=null, undoAction)` (ungrouped `WithUndo` path), `attemptUndo(id)` (per-action refusal → `Failed`, unchanged), `clearSpent()` semantics for ungrouped entries, and `entries: StateFlow<List<UndoHistoryEntry>>`. **Do NOT add any `UndoStatus` enum member** (would break consumers' exhaustive `when`s).
  >
  > **Store additions** — grouping lives in a private `entryId → groupId` map (SB's pick; primary constructor stays frozen, so no `copy()`/`componentN()` break):
  > - `fun openGroup(groupId: String, label: String, undoAll: suspend () -> Unit)` — registers the group-level ATOMIC action + label. May be folded into the first grouped `append` if the planner prefers, as long as semantics match.
  > - `fun append(message: String, preview: UndoPreview? = null, groupId: String, undoAction: suspend () -> Unit): String` — grouped overload (existing 3-arg `append` stays, ungrouped).
  > - `fun group(groupId: String): List<UndoHistoryEntry>` — newest-first.
  > - `fun groupIdOf(entryId: String): String?` — REQUIRED (SB derives its grouped view from `entries`). A reactive `groups` `StateFlow` is OPTIONAL.
  > - `fun groupLabel(groupId: String): String?`
  > - `fun groupStatus(groupId: String): UndoGroupStatus`, where `UndoGroupStatus` = `Undoable` (all members `Available`) | `PartiallyResolved` (≥1 `Available` AND ≥1 spent) | `FullyResolved` (0 `Available`) | `Empty` (unknown or evicted).
  > - `suspend fun attemptUndoGroup(groupId: String): UndoGroupResult` — **ATOMIC:** claims all `Available` members, runs `undoAll` ONCE (never a loop over member lambdas), and on success marks all claimed members `Undone`. `UndoGroupResult` = `Undone(count: Int)` | `Refused(reason: String, changedItem: String?)` | `Failed` | `NothingToUndo`. A TYPED refusal exception thrown by the consumer's `undoAll` (`reason`, `changedItem?`) → `Refused`: nothing was written, so claimed members go BACK to `Available` (user can retry). **The group path therefore needs a RELEASABLE claim (a mutex or release), NOT the one-way `consumedGuard`.** Any other throw → `Failed`, marking the claimed members `Failed`.
  > - **Eviction is group-atomic:** the 50-cap never drops a single member — evict/count a group as ONE unit. `clearSpent()` removes a group only once it is `FullyResolved`.
  > - Session-scoped, in-memory, NO serialization. `UNDO-PERSIST` is Future.
  >
  > **UI projections** (presentational; built by the consumer FROM store reads — `group()`/`groupStatus()`/`groupIdOf()` — never from consumer-side bookkeeping):
  > - `UndoAffordanceUiModel(allLabel: String, rows: List<UndoRowUiModel> = emptyList(), onUndoAll: (() -> Unit)? = null, refused: UndoRefusedUiModel? = null)`
  > - `UndoRowUiModel(id: String, label: String, state: UndoRowState)` with `sealed interface UndoRowState { Available(onUndo: () -> Unit) | Undone | Unavailable(reason: String) }`
  > - `UndoRefusedUiModel(reason: String, changedItem: String? = null)` — fed from `UndoGroupResult.Refused`.

- **D-02 [undo-placement].** Undo lives as a field ON `Success`, undo-refused/partial is a nested substate — NOT a new top-level arm of the sealed `VoiceOutcomeUiState`. Keeps the top-level sealed type stable for Phase 12's additive `NeedsConfirmation`.
- **D-03 [undo-crossrepo].** Validate the per-item `Unavailable(reason)` + `Refused(reason, changedItem)` union against SB/CT real call-sites at the A13 reconvene BEFORE authoring. **Status: this validation is exactly what D-01's 2026-09-30 amendment *is* — SB confirmed the shape via the orchestrator. Treat D-03 as satisfied by D-01's frozen text; do not reopen the shape question.**
- **D-04 [handled-by].** `HandledByUiModel`: required tier + optional approach/provider/model/escalationCount. **Status: ALREADY BUILT** — verified live at `model/HandledByUiModel.kt` (quoted below).
- **D-05 [success-editable].** Sealed outcome needs a committed-but-EDITABLE `Success` state (CT). **Status: ALREADY BUILT** — `VoiceOutcomeUiState.Success.editableContent`/`.inFlight` (quoted below).
- **D-06 [batch-results].** Batch outcomes render per-row result + in-flight lock. **Status: ALREADY BUILT** — `VoiceOutcomeUiState.Success.batchResults`, `BatchRowResultUiModel`, `OutcomeSheet`'s `BatchResultsList` (quoted below).
- **D-07 [clarify-choices].** Standalone, prop-driven clarification composable (VCLAR-01, contract A19): question + `{id, label}` options + `onSelect(id)` + `onDismiss`. Pressable chip/button surface, your design call on chips-vs-buttons and bar-vs-sheet-state. **Status: NOT YET BUILT — no commit has touched this.**
- **D-08 [failure-action].** Failure carries an OPTIONAL action slot (label+callback). **Status: ALREADY BUILT** — `FailureActionUiModel`, `VoiceOutcomeUiState.Failure.action` (quoted below).

### Claude's Discretion

- Loud-failure and undo-refused visual treatment: use theme `error`/`errorContainer` roles (icon + headline + reason string, sticky); explicitly NOT `AttentionCue` (KDoc forbids use as a failure signal). **Status: the Failure half is ALREADY BUILT this way** (verified in `OutcomeSheet.kt`'s `FailureBody`); the undo-refused nested substate still needs the same treatment applied fresh.
- VCLAR-01: chips-vs-buttons and bar-vs-sheet-state is a swappable design call (Gate-1 gallery review).

### Deferred Ideas (OUT OF SCOPE)

- The `NeedsConfirmation` state itself is Phase 12 (extends this sheet's sealed state).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Status | Research Support |
|----|-------------|--------|-------------------|
| VOUT-01 | Outcome/failure sheet renders a command outcome from props, domain-neutral | **DONE** (commit `6a946d3`) | `model/VoiceOutcomeUiState.kt`, `component/OutcomeSheet.kt` — verified live, quoted below |
| VOUT-02 | "Handled by: tier/approach" indicator from props | **DONE** | `model/HandledByUiModel.kt`, `OutcomeSheet.kt`'s `HandledByRow` — verified live |
| VOUT-03 | Failure states render loudly and visibly, optional action slot | **DONE** | `OutcomeSheet.kt`'s `FailureBody` on `errorContainer`/`onErrorContainer`, `FailureActionUiModel` — verified live |
| VUNDO-01 | "Undo all (N)" + per-item Undo + unavailable state + undo-refused/partial state | **NOT BUILT — this phase's remaining core work** | The frozen D-01 store-side grouping API design (Architecture Patterns, Code Examples) + the exact current `UndoHistoryEntry`/`UndoHistoryStore` shapes (verified, quoted) |
| VCLAR-01 | Clarification choices: question + options `{id, label}` + onSelect + onDismiss, pressable, registered | **NOT BUILT** | `AppChip`/`SegmentedOptionSelector` precedent (verified) + the `DomainVocabularyDriftGuardTest` naming gate (verified — `Clarification` is NOT yet allowlisted) |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- **One-way dependency:** library imports no host code, holds no secrets, makes no domain assumptions. New code here must import only Android SDK / AndroidX / Compose / Hilt / Coil / navigation-compose / reorderable / osmdroid — never a consumer, never `voice-action-engine`. `kotlinx.coroutines.sync.Mutex` is part of `kotlinx-coroutines-core`, already a transitive dependency of this module (confirmed: `UndoHistoryStore.kt` already imports `kotlinx.coroutines.flow.*`/`CancellationException` from the same artifact) — using `Mutex` for the group-claim lock introduces **no new Gradle dependency**.
- **`ComponentRegistry` is the single source of truth + drift guard:** every new public top-level `@Composable` must be registered in a family list XOR allowlisted in `INTENTIONALLY_UNREGISTERED`.
- **`DomainVocabularyDriftGuardTest` (independent second guard):** every public composable's head token (leading PascalCase word) must be in `PRIMITIVE_NOUN_ALLOWLIST` or individually grandfathered in `DOMAIN_VOCABULARY`. **Verified this session: `"Outcome"` was already added to `PRIMITIVE_NOUN_ALLOWLIST` for `OutcomeSheet`** (`src/test/.../DomainVocabularyDriftGuardTest.kt:326-329`, quoted below). **`"Clarification"` and `"Undo"` need checking for the new work:** `"Undo"` is already present (line 314, part of the "widening" block). `"Clarification"` is **NOT present anywhere** in `PRIMITIVE_NOUN_ALLOWLIST` or `DOMAIN_VOCABULARY` (confirmed via grep this session — zero matches) — the new clarification composable's head token must clear this gate.
- **Bindings-only Hilt, no application host:** `UndoHistoryStore` stays `@Singleton @Inject constructor()` — the additive change must preserve that; still no `@HiltAndroidApp`/`@AndroidEntryPoint`.
- **Detekt zero-baseline:** no new findings buried in a regenerated baseline.
- **AGP 9.2.1 / Kotlin 2.3.20 / Hilt 2.60.1 / Compose BOM 2026.04.01 / JDK 17, minSdk 35, compileSdk 36**, single-module hub — every Gradle command drops the `:yahirandroidtaste` module prefix.

## Summary

Three of the five phase requirements (VOUT-01/02/03) are **already shipped** (commit `6a946d3`) and verified live this session — `model/VoiceOutcomeUiState.kt`, `model/HandledByUiModel.kt`, `model/FailureActionUiModel.kt`, `model/BatchRowResultUiModel.kt`, and `component/OutcomeSheet.kt` exist exactly as the D-04/D-05/D-06/D-08 decisions describe, registered in `ComponentRegistry`, with `OutcomeSheetTest.kt` covering the success/failure/handled-by/action-slot matrix. **Nothing in this research changes that code or those decisions.**

The entire remaining engineering surface is **VUNDO-01** (the frozen D-01 store-side grouping API, not yet touched) and **VCLAR-01** (the clarification composable, not yet touched). VUNDO-01 is the hard problem: `11-CONTEXT.md`'s D-01 amendment specifies the *external* contract in full (method signatures, the two new sealed types, the "atomic claim, releasable on Refused, group-atomic eviction" behavior) but explicitly defers the *internal mechanism* — how to satisfy "releasable claim, NOT the one-way `consumedGuard`" against the CURRENT store's actual machinery — to the plan. This research does that mechanism-level design work concretely, against the verified current shapes:

1. **`UndoHistoryEntry`'s `tryConsume()` (`feedback/UndoHistoryEntry.kt:58`) is a ONE-WAY `AtomicBoolean` CAS — it can never be "released."** Any group-claim mechanism that calls `tryConsume()` to claim a member permanently strands that entry if the group undo is later `Refused` — directly contradicting D-01's explicit "claimed members go BACK to `Available`" requirement. **The group claim must therefore be implemented ENTIRELY OUTSIDE `tryConsume()`/`consumedGuard`** — a `kotlinx.coroutines.sync.Mutex` held for the whole claim→run→resolve critical section, with `Available`/`Undone`/`Failed` status transitions as the only state that changes (no separate "claimed" flag needed on success or terminal-failure paths; the group's `Refused` path simply never writes a status change at all, since nothing needs reverting — the entries were never marked anything other than `Available`).
2. **This creates one residual, honestly-disclosed concurrency gap the plan must decide how to handle:** a grouped entry keeps its own individual `undoAction` (used by the pre-existing, unchanged, per-item `attemptUndo(id)` path) *and* participates in a group's shared `undoAll` (used by the new `attemptUndoGroup`). Because `attemptUndoGroup`'s claim is deliberately NOT `tryConsume()`-based, a concurrent single-item `attemptUndo(id)` tap on the same entry id is not mutually excluded by the store alone — see Pitfall 4 and the concrete mitigation recommended there (UI-level mutual exclusion via a `groupBusy` display flag, not a store-level lock spanning both paths, which would require touching the frozen-unchanged `attemptUndo(id)`).
3. **Eviction and `clearSpent()` must both become group-aware without changing their signatures** — the current `evictIfNeeded`/`clearSpent()` (`feedback/UndoHistoryStore.kt:99-121`) operate entry-by-entry; the group-atomic requirement means both need a "resolve the group, then act on every member of it together" branch for any entry that has a `groupId`, while staying byte-for-byte identical for ungrouped entries.

**Primary recommendation:** Implement the frozen D-01 signatures exactly as specified in `UndoHistoryStore.kt`, backed by: (a) a private `MutableMap<String, String>` (`entryId -> groupId`), a private `MutableMap<String, Pair<String, suspend () -> Unit>>` (`groupId -> (label, undoAll)`) populated by `openGroup`, and one `Mutex` (`groupUndoMutex`) scoped to the whole store (simplest — groups are not expected to be undone concurrently by a single user) guarding only `attemptUndoGroup`'s claim→run→resolve section; (b) `group()`/`groupIdOf()`/`groupLabel()`/`groupStatus()` as pure reads over `_entries.value` + the two maps, no new `StateFlow`; (c) rewritten `evictIfNeeded`/`clearSpent()` with a groupId-aware branch that treats a group as one eviction/clear unit. Build `UndoAffordanceUiModel`/`UndoRowUiModel`/`UndoRefusedUiModel` as new, consumer-facing, all-`val` types in `model/`, constructed by the **consumer** from `UndoHistoryStore.group(groupId)`/`.groupStatus(groupId)` reads (never inside the library itself, since the library never holds a reference to any app's `UndoHistoryStore` instance — INV-01). Wire `VoiceOutcomeUiState.Success.undo: UndoAffordanceUiModel?` and a nested `refused: UndoRefusedUiModel?` per D-02. For VCLAR-01, add `"Clarification"` to `PRIMITIVE_NOUN_ALLOWLIST` in the same commit that introduces the new composable (mirrors the already-landed `"Outcome"` precedent verified this session), and build it as a standalone composable (not nested in `OutcomeSheet`'s `when`) reusing `AppChip` for the option row.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Outcome rendering (success/failure, from props) | Browser/Client (Compose UI, this library) | — | **Already shipped.** Pure presentation; engine/consumer computes the outcome (INV-01) |
| "Handled by" provenance data | Consumer app (owns `CommandTrace` mapping) | Library (renders the model it's given) | **Already shipped.** `voice-action-engine`'s `TEL-01 CommandTrace` is a peer hub this library must never import (L7) |
| Undo group bookkeeping (`groupId -> entries`, group label, group undoAll action) | **Library** (`UndoHistoryStore`, NEW this phase) | — | D-01's frozen decision: this is a STORE-side addition, not a consumer-side projection — the store already owns first-consumer-wins semantics and is the only place that can make the group claim atomic against the existing `entries` flow |
| Group-claim atomicity + releasability (`attemptUndoGroup`) | **Library** (`UndoHistoryStore`, NEW this phase) | — | Must live beside `_entries`/`attemptUndo` to be atomic against them; a consumer-side claim could not be atomic against the store's own concurrent mutations |
| Undo-group UI projection (`UndoAffordanceUiModel`/`UndoRowUiModel`/`UndoRefusedUiModel`) | Consumer app (builds the projection by reading the store) | Library (renders the projection it's given) | D-01: "built by the consumer FROM store reads... never from consumer-side bookkeeping" — the library never holds an app's `UndoHistoryStore` instance itself (no DI wiring crosses that boundary in this library) |
| Clarification resolution (mapping a tapped option id back to the engine's `Clarification`) | Consumer app | Library (renders options, emits the tapped opaque id via `onSelect`) | Library never interprets `id` — L7, no engine dependency |
| Registry/gallery wiring | Library (`explorer/VoiceCommandFamilyScreen.kt`) | — | Established pattern; `OutcomeSheet` entry already lands here |

## Standard Stack

### Core

No new external library dependency. Everything is Compose/Material3 + `kotlinx.coroutines` primitives already on the classpath.

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|---------------|
| Jetpack Compose / Material3 | Compose BOM 2026.04.01 (pinned, root `CLAUDE.md`) | Sealed-state rendering, error color roles, `AppChip` reuse | Already the library's only UI toolkit |
| `kotlinx.coroutines.sync.Mutex` | Transitively available via `kotlinx-coroutines-core` (already imported in `UndoHistoryStore.kt` for `flow`/`CancellationException`) | Guards `attemptUndoGroup`'s claim→run→resolve critical section — the RELEASABLE claim D-01 requires, deliberately NOT `AtomicBoolean`/`tryConsume()` | `Mutex.withLock { }` is the standard Kotlin-coroutines mutual-exclusion primitive for a suspend critical section; no new Gradle dependency needed |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `SheetScaffold` (`component/SheetScaffold.kt:50-69`, verified) | in-repo | Already the host for `OutcomeSheet` — no change needed | N/A — already wired |
| `AppChip` (`component/AppChip.kt:88-175`, verified) | in-repo | Per-item undo row affordance; clarification-choice chip option | Any compact labeled/pressable token |
| `DynamicActionButton` (`component/DynamicActionButton.kt:37-58`, verified) | in-repo | "Undo all" action button; already used for Failure's action slot | Role-colored button, `Neutral` role for undo-all |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| A store-level `Mutex` scoped per-group (`Map<String, Mutex>`) | ONE store-wide `groupUndoMutex` guarding all `attemptUndoGroup` calls | Per-group mutexes allow true cross-group concurrency but add unbounded-map-growth/eviction bookkeeping for the mutexes themselves; a single-user session library has no realistic concurrent-multi-group-undo scenario, so ONE mutex is simpler and sufficient — document this as a scoping choice, not an oversight, if the plan wants to revisit |
| Using `tryConsume()` to claim group members (mirrors the existing single-item path) | A `Mutex`-only claim with no `AtomicBoolean` involvement | D-01 explicitly rules out the one-way guard for exactly the reason verified in `UndoHistoryEntry.kt:57` ("Returns true only for the FIRST caller... false for every caller after" — no reset path exists) |
| A reactive `groups: StateFlow<Map<String, ...>>` | Pure-function reads (`group()`/`groupStatus()`/`groupIdOf()`) over the existing `entries` `StateFlow` | D-01 marks the reactive `groups` flow explicitly OPTIONAL; the required surface is non-reactive reads, which is strictly less code and lets a consumer combine/derive its own `StateFlow` via `entries.map { ... }` if it wants reactivity |

**Installation:** None — no new Gradle dependency for this phase.

## Package Legitimacy Audit

**Not applicable.** Zero new external packages. Every new symbol is authored against the existing Compose/Material3/`kotlinx.coroutines` dependency set already declared in `build.gradle.kts` (verified: `implementation(libs.androidx.compose.*)`, `testImplementation(libs.kotlinx.coroutines.test)` — `kotlinx-coroutines-core` itself arrives transitively, already used non-test in `UndoHistoryStore.kt`).

## Architecture Patterns

### System Architecture Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│ CONSUMER APP (SecondBrain / CalTracker)                                    │
│                                                                             │
│  1. Voice command produces N mutations (e.g. batch-create/edit)           │
│     -> store.openGroup(groupId, label, undoAll = { /* one atomic undo */})│
│     -> store.append(msg, groupId = groupId, undoAction = { /* one item */}│
│        )  x N   (grouped overload — per-member undoAction still usable    │
│                   via the UNCHANGED attemptUndo(id) path)                 │
│                                                                             │
│  2. Building the outcome sheet's props, the consumer READS the store:     │
│     val members = store.group(groupId)                                   │
│     val status  = store.groupStatus(groupId)                             │
│     -> maps members/status into UndoAffordanceUiModel/UndoRowUiModel      │
│        (this mapping is the consumer's job — library never does it)      │
│                                                                             │
│  3. "Undo all (N)" tapped -> onUndoAll() -> consumer calls                │
│     store.attemptUndoGroup(groupId) -> UndoGroupResult                    │
│     -> on Refused, consumer re-maps into UndoRefusedUiModel and re-renders│
└───────────────────────────────────┬─────────────────────────────────────┬─┘
             one-way dependency (INV-01)   props                          │
                                     ▼                                    │ reads
┌───────────────────────────────────────────────────────────────────────────┐
│ yahirandroidtaste LIBRARY                                                  │
│                                                                             │
│  feedback/UndoHistoryStore.kt (EXISTING file, ADDITIVE changes)           │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │ _entries: MutableStateFlow<List<UndoHistoryEntry>>   (unchanged)     │ │
│  │ entries: StateFlow<...>                               (unchanged)    │ │
│  │ append(message, preview?, undoAction)                (unchanged)     │ │
│  │ attemptUndo(id)                                       (unchanged)    │ │
│  │ ── NEW, additive ──                                                  │ │
│  │ private val groupIdByEntryId: MutableMap<String, String>             │ │
│  │ private val groupMeta: MutableMap<String, GroupMeta>  (label+undoAll)│ │
│  │ private val groupUndoMutex = Mutex()                                 │ │
│  │ openGroup(groupId, label, undoAll)                                   │ │
│  │ append(message, preview?, groupId, undoAction)   [grouped overload]  │ │
│  │ group(groupId): List<UndoHistoryEntry>                               │ │
│  │ groupIdOf(entryId): String?                                          │ │
│  │ groupLabel(groupId): String?                                         │ │
│  │ groupStatus(groupId): UndoGroupStatus                                │ │
│  │ attemptUndoGroup(groupId): UndoGroupResult    <- Mutex-guarded       │ │
│  │ clearSpent()   [rewritten: group-atomic branch added]                │ │
│  │ evictIfNeeded  [rewritten: group-atomic branch added]                │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│  feedback/UndoHistoryEntry.kt  — ZERO CHANGES (internal ctor untouched)   │
│  feedback/UndoGroupTypes.kt (NEW) — UndoGroupStatus, UndoGroupResult,     │
│                                      UndoGroupRefusedException           │
│                                                                             │
│  model/ (new, all-val)              component/OutcomeSheet.kt (EXTEND)   │
│  ┌──────────────────────┐           ┌──────────────────────────────────┐ │
│  │ UndoAffordanceUiModel │──────────▶│ Success arm: renders undo.rows,  │ │
│  │ UndoRowUiModel/       │           │ "Undo all", nested Refused state │ │
│  │   UndoRowState        │           └──────────────────────────────────┘ │
│  │ UndoRefusedUiModel    │                                                │
│  │ ClarificationOption   │           component/ClarificationBar.kt (NEW) │
│  │   UiModel             │──────────▶│ question + N AppChip options +    │ │
│  └──────────────────────┘           │ onSelect/onDismiss                 │
│                                       └──────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Status |
|-----------|----------------|--------|
| `model/VoiceOutcomeUiState.kt` | Sealed `Success`/`Failure`, additive-ready for Phase 12 | **DONE** — add `undo: UndoAffordanceUiModel?` field to `Success` this phase |
| `model/HandledByUiModel.kt` | Tier + optional provenance | **DONE** |
| `model/FailureActionUiModel.kt` | Failure's optional action slot | **DONE** |
| `model/BatchRowResultUiModel.kt` | Per-row batch result | **DONE** |
| `component/OutcomeSheet.kt` | Renders `Success`/`Failure` | **DONE** — extend `SuccessBody` to render `undo` |
| `feedback/UndoHistoryStore.kt` | Append/attemptUndo/clearSpent/eviction | **EXTEND (this phase)** — add the 7-member grouping API |
| `feedback/UndoHistoryEntry.kt` | Single undo record | **ZERO CHANGES** |
| `feedback/UndoGroupTypes.kt` (new file) | `UndoGroupStatus`, `UndoGroupResult`, `UndoGroupRefusedException` | **NEW (this phase)** |
| `model/UndoAffordanceUiModel.kt`, `UndoRowUiModel.kt`, `UndoRefusedUiModel.kt` (new) | Presentational undo-group projection | **NEW (this phase)** |
| `model/ClarificationOptionUiModel.kt` (new) | `{id, label}` | **NEW (this phase)** |
| `component/ClarificationBar.kt` (new, name TBD) | Question + pressable options | **NEW (this phase)** |

## Recommended Project Structure

```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/
│   ├── OutcomeSheet.kt                     # EXISTING — extend SuccessBody for undo affordance
│   └── ClarificationBar.kt                 # NEW — question + pressable options (VCLAR-01)
├── feedback/
│   ├── UndoHistoryEntry.kt                 # UNCHANGED
│   ├── UndoHistoryStore.kt                 # EXTEND — additive grouping API
│   └── UndoGroupTypes.kt                   # NEW — UndoGroupStatus / UndoGroupResult / UndoGroupRefusedException
├── model/
│   ├── VoiceOutcomeUiState.kt              # EXISTING — add `undo` field to Success
│   ├── UndoAffordanceUiModel.kt            # NEW
│   ├── UndoRowUiModel.kt                   # NEW (contains UndoRowState sealed interface)
│   ├── UndoRefusedUiModel.kt               # NEW
│   └── ClarificationOptionUiModel.kt       # NEW
└── explorer/
    └── VoiceCommandFamilyScreen.kt         # EXISTING — append undo states to OutcomeSheet's matrix + a new ClarificationBar entry
```

## Verified Current Code (read in full this session)

### `feedback/UndoHistoryEntry.kt` (47 lines total)

```kotlin
data class UndoHistoryEntry internal constructor(
    val id: String,
    val message: String,
    val timestamp: Long,
    val undoAction: suspend () -> Unit,
    val preview: UndoPreview? = null,
    val status: UndoStatus = UndoStatus.Available,
    private val consumedGuard: AtomicBoolean = AtomicBoolean(false)
) {
    fun tryConsume(): Boolean = consumedGuard.compareAndSet(false, true)
    fun withStatus(newStatus: UndoStatus): UndoHistoryEntry = copy(status = newStatus)
}
```
`[VERIFIED: src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryEntry.kt:47-66]`. `UndoStatus` is `enum class UndoStatus { Available, Undone, Failed }` `[VERIFIED: feedback/UndoHistoryEntry.kt:13]` — **exactly 3 members, D-01 forbids adding a 4th.**

KDoc quoted verbatim (the one-way-guard fact that drives the whole "must not use `tryConsume` for group claim" conclusion): *"Returns true only for the FIRST caller to claim this entry; false for every caller after."* `[VERIFIED: feedback/UndoHistoryEntry.kt:57]`. There is no method anywhere in this file or `UndoHistoryStore.kt` that resets `consumedGuard` back to `false` — confirmed by reading both files in full this session.

### `feedback/UndoHistoryStore.kt` (122 lines total)

```kotlin
@Singleton
class UndoHistoryStore @Inject constructor() {
    private val _entries = MutableStateFlow<List<UndoHistoryEntry>>(emptyList())
    val entries: StateFlow<List<UndoHistoryEntry>> = _entries.asStateFlow()

    fun append(message: String, preview: UndoPreview? = null, undoAction: suspend () -> Unit): String {
        val entry = UndoHistoryEntry(id = UUID.randomUUID().toString(), message = message,
            timestamp = System.currentTimeMillis(), undoAction = undoAction, preview = preview)
        _entries.update { current -> evictIfNeeded(listOf(entry) + current) }
        return entry.id
    }

    suspend fun attemptUndo(id: String) {
        val entry = _entries.value.firstOrNull { it.id == id } ?: return
        if (entry.status != UndoStatus.Available) return
        if (!entry.tryConsume()) return
        val newStatus = try { entry.undoAction(); UndoStatus.Undone }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { UndoStatus.Failed }
        _entries.update { current -> current.map { if (it.id == id) it.withStatus(newStatus) else it } }
    }

    fun clearSpent() {
        _entries.update { current -> current.filterNot { it.status == UndoStatus.Undone || it.status == UndoStatus.Failed } }
    }

    private fun evictIfNeeded(current: List<UndoHistoryEntry>): List<UndoHistoryEntry> {
        if (current.size <= 50) return current
        val oldestSpentIndex = current.indexOfLast { it.status == UndoStatus.Undone || it.status == UndoStatus.Failed }
        return if (oldestSpentIndex >= 0) current.filterIndexed { index, _ -> index != oldestSpentIndex }
        else current.dropLast(1)
    }
}
```
`[VERIFIED: src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStore.kt:23-122]` — the full current file, reproduced (comments/KDoc elided for space; no logic elided). This is the exact surface D-01 requires stay byte-for-byte behaviorally unchanged for ungrouped entries.

### `model/VoiceOutcomeUiState.kt`, `HandledByUiModel.kt`, `FailureActionUiModel.kt`, `BatchRowResultUiModel.kt`, `component/OutcomeSheet.kt`

All five files verified read in full this session — **all already implement D-04/D-05/D-06/D-08 exactly as specified**, registered live in `ComponentRegistry` via `voiceCommandFamilyEntries` (`explorer/VoiceCommandFamilyScreen.kt:138-152`, confirmed `name = "OutcomeSheet"` with a full 4-state matrix and a `content = { OutcomeSheetVariants() }` block). `OutcomeSheetTest.kt` (131 lines, verified) covers: Success summary render, handled-by present/absent, Failure surface+reason, action-button present/click/absent. **Nothing here needs to change except adding an `undo` field to `Success` and rendering it** — do not re-touch VOUT-01/02/03's existing logic.

### `explorer/DomainVocabularyDriftGuardTest.kt` — current allowlist state

`PRIMITIVE_NOUN_ALLOWLIST` (quoted in full, current live contents) `[VERIFIED: src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt:297-330]`:
```
"Control","Sheet","Field","Canvas","State","Dialog","Bar","Card","Value","Item","Content","Row",
"Scaffold","Swatch","Grid","View","Chip","Popup","Picker","Button","Fab","Badge","Selector","Ring",
"Menu","Overlay","Preview","Cue","Editor","Base","Screen","Theme","Ladder","Showcase",
"Accent","Adaptive","Animated","App","Attention","Bulk","Clearable","Confirmation","Count","Crop",
"Cycle","Dynamic","Elevation","Empty","Expandable","Filter","Gradient","Hero","Icon","List","Metric",
"Name","Progress","Segmented","Sort","Tactile","Text","Undo",
"Date","Preset","Provider","Model","Approach",
"Outcome"   // Phase 11 (VOUT-01/02/03, VUNDO-01) — already landed, comment at line 326-329 confirms it
```
**`"Undo"` is already present** (part of the widened block, line 314) — a composable named e.g. `UndoRow`/`UndoAllButton` needs no allowlist edit. **`"Clarification"` is NOT present** anywhere in `PRIMITIVE_NOUN_ALLOWLIST` or `DOMAIN_VOCABULARY` (confirmed via `grep -rn "Clarification"` across both files this session — zero matches). The new clarification composable's head token (if named e.g. `ClarificationBar`) MUST be added to `PRIMITIVE_NOUN_ALLOWLIST` with a one-line rationale in the same commit, mirroring the already-landed `"Outcome"` precedent verbatim: `[VERIFIED: explorer/DomainVocabularyDriftGuardTest.kt:326-329]` *"Phase 11 (VOUT-01/02/03, VUNDO-01): 'Outcome' is OutcomeSheet's head token — a generic UI-archetype noun (a command-result presentation surface), not consumer-domain vocabulary; the library authors no app-specific noun of its own."*

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Compact labeled pressable token (per-item undo row label, clarification option) | A bespoke `Row`+`Surface`+`clickable` | `AppChip` (`component/AppChip.kt:88-175`, verified) | Already handles 48dp touch target, selected/unselected roles, long-press/double-click |
| Role-colored action button (undo-all, clarification dismiss) | Hand-rolled `TextButton` | `DynamicActionButton` (`component/DynamicActionButton.kt:37-58`, verified) | `Neutral`/`Destructive`/`Save` → M3 color mapping already exists |
| Mutual-exclusion / atomic critical section for a `suspend` operation | A hand-rolled spin-loop or a second `AtomicBoolean` | `kotlinx.coroutines.sync.Mutex.withLock { }` | Standard-library primitive, already transitively on the classpath, exactly fits a `suspend fun attemptUndoGroup`'s claim→run→resolve shape |
| Group-membership bookkeeping duplicated on both the store and the consumer | A second copy of "which entries are in group X" kept consumer-side | Store-side `groupIdByEntryId`/`group(groupId)` as the SINGLE source of truth (D-01 explicit: "never from consumer-side bookkeeping") | Two copies of group membership drift the instant an entry is evicted from one side but not the other |

**Key insight:** The presentational primitives (chip, role-button, sheet chrome) all already exist and are already reused by the shipped half of this phase (`OutcomeSheet`). The remaining hard work is entirely in `UndoHistoryStore`'s internal state-machine extension — a data-structure and concurrency-correctness problem, not a widget problem.

## Runtime State Inventory

Not applicable — additive phase, no rename/refactor/migration. `UndoHistoryStore` stays session-scoped, in-memory, no serialization (D-01 explicit: "Session-scoped, in-memory, NO serialization. `UNDO-PERSIST` is Future.") — no persisted runtime state to migrate.

## Common Pitfalls

### Pitfall 1: Using `tryConsume()`/`consumedGuard` to claim group members (the D-01 trap)

**What goes wrong:** `attemptUndoGroup` calls `entry.tryConsume()` per member to "claim" them before running `undoAll()`, mirroring the existing single-item `attemptUndo(id)` pattern. On a typed `Refused` throw, the implementation tries to "release" the claim back to `Available` — but `consumedGuard` has no release method (`compareAndSet(false, true)` only, one direction, confirmed no reset call exists anywhere in either file). The claimed entries are now permanently unconsumable via the CAS guard even though their `.status` was set back to `Available` — a future retry attempt's `tryConsume()` silently returns `false` and the retry becomes an invisible no-op.

**Why it happens:** It's the "obvious" reuse of the existing single-item mechanism, and D-01's own text ("claimed all `Available` members") reads similarly to the single-item "claim" language — but D-01 explicitly flags this exact trap: *"The group path therefore needs a RELEASABLE claim (a mutex or release), NOT the one-way `consumedGuard`."*

**How to avoid:** Implement the group claim with a `Mutex` held for the whole `attemptUndoGroup` body. Never call `tryConsume()` from `attemptUndoGroup`. On `Refused`, simply do not write any status change — the entries were read as `Available` and stay `Available`, no "release" step is needed because nothing was ever marked otherwise.

**Warning signs:** Any diff adding `.tryConsume()` calls inside a new `attemptUndoGroup` implementation; a test asserting "retry after Refused re-attempts successfully" failing silently (the retry's group() read shows `Available` status but the undo never actually re-fires).

**Phase to address:** Phase 11 (this phase), at design/implementation time — this is the single highest-risk defect in the whole undo extension.

### Pitfall 2: `clearSpent()`/eviction dropping one member of a still-partially-live group

**What goes wrong:** The existing `clearSpent()` (`feedback/UndoHistoryStore.kt:99-103`) removes every entry with status `Undone`/`Failed`, regardless of group. Applied unchanged to a grouped entry, this silently removes a spent member from a group that is only `PartiallyResolved` (some members still `Available`) — `group(groupId)` now returns an incomplete member list, and a later `attemptUndoGroup` or UI re-render sees a group missing history it should still show (e.g., "2 of 3 undone" becomes unrenderable because the 2 undone rows vanished).

**Why it happens:** `clearSpent()`'s filter predicate is per-entry, with no concept of "this entry belongs to a group that isn't fully resolved yet." The same blind spot applies to `evictIfNeeded`'s single-entry eviction (`feedback/UndoHistoryStore.kt:110-121`) — it could evict exactly one member out of a group, leaving the group's remaining members orphaned (their `groupId` still points at a group whose total member count the consumer can no longer reconstruct correctly).

**How to avoid:** Both functions need a groupId-aware branch:
- `clearSpent()`: for each entry, if `groupIdByEntryId[entry.id] == null` (ungrouped), keep the existing per-entry Undone/Failed filter unchanged. If grouped, only drop the entry if its ENTIRE group's `groupStatus(groupId) == UndoGroupStatus.FullyResolved` — i.e., compute per-group resolution first, then filter.
- `evictIfNeeded`: when the chosen eviction target (oldest-spent, or oldest-by-insertion fallback) is a grouped entry, expand the removal to every entry sharing that `groupId` in the same pass, not just the one entry — the resulting list can drop below 50 by more than 1, which is fine (the invariant is `<= 50`, not `== 50`).

**Warning signs:** A test appending a 3-member group, undoing only 1 member via the group path (not possible per D-01's atomic design, but useful as a stress test via direct store manipulation in a unit test), calling `clearSpent()`, and finding `group(groupId).size` has shrunk below its true remaining-live count.

**Phase to address:** Phase 11 — both functions must be rewritten in the SAME commit that adds the grouping API, since the 50-cap and clear-spent invariants are unconditionally live the moment any grouped entry exists.

### Pitfall 3: `"Clarification"` (or whatever head token is chosen) failing `DomainVocabularyDriftGuardTest`

**What goes wrong:** A new composable is named e.g. `ClarificationBar`, compiles, registers fine in `ComponentRegistry`, then fails `DomainVocabularyDriftGuardTest` in the full suite because `"Clarification"` is in neither `PRIMITIVE_NOUN_ALLOWLIST` nor `DOMAIN_VOCABULARY` (confirmed via grep this session).

**Why it happens:** Same category as the already-resolved `"Outcome"` precedent from this phase's first plan wave — a fresh head token always needs an explicit allowlist decision, never assumed.

**How to avoid:** Add the chosen head token to `PRIMITIVE_NOUN_ALLOWLIST` with a one-line rationale in the SAME commit, exactly mirroring the already-landed `"Outcome"` entry's comment style (quoted above in Verified Current Code).

**Warning signs:** `DomainVocabularyDriftGuardTest` going RED in a full-suite run.

**Phase to address:** Phase 11 — settle before implementation, same as the already-resolved Outcome naming decision.

### Pitfall 4: A concurrent per-item `attemptUndo(id)` racing a live `attemptUndoGroup(groupId)` on the same member

**What goes wrong:** A grouped entry's individual `undoAction` remains reachable via the pre-existing, unchanged `attemptUndo(id)` (its own per-item Undo button in the UI, independent of the group's "Undo all"). If a user (or a double-tap) fires both paths on the same entry at nearly the same moment, `attemptUndo(id)`'s `tryConsume()`-based claim and `attemptUndoGroup`'s `Mutex`-based claim are NOT mutually exclusive with each other — they guard different things. In the worst case, the group's `undoAll()` (one atomic batch action) and the single entry's own `undoAction()` both execute for logically the same underlying mutation.

**Why it happens:** D-01's frozen text explicitly forbids reusing `tryConsume()` for the group path (Pitfall 1) and does not introduce any NEW synchronization between the two independent call paths (`attemptUndo(id)` is explicitly "unchanged"). This is a genuine, disclosed gap in the frozen contract, not an oversight of this research.

**How to avoid (recommended, not frozen by D-01 — a plan-level decision):** Handle this at the UI/consumer layer, not the store layer: while `attemptUndoGroup` is in flight for a groupId (a transient, display-only "busy" signal — e.g., the consumer disables the group's individual `UndoRowUiModel.Available.onUndo` callbacks, or the presentational model swaps each row to a distinct disabled-but-visible state, while `attemptUndoGroup` is running), don't offer the per-item Undo tap at all. This keeps the store's two methods simple and independently correct, and matches this library's own "hideable-by-null-prop, never shown-disabled-without-explanation" convention (`ApproachLadderCard.kt:29`, verified) if extended thoughtfully — or, more simply, the plan may accept the residual race as sufficiently rare (single-user, requires a near-simultaneous double-tap) and document it rather than build UI-level mutual exclusion. **This is an Open Question for the plan to resolve explicitly, not something this research resolves for it.**

**Warning signs:** A double-undo test (fire both `attemptUndo(id)` and `attemptUndoGroup(groupId)` concurrently on a group containing that id) observing the underlying mutation reversed twice.

**Phase to address:** Phase 11 — the plan must make an explicit, documented choice here (UI mutual exclusion vs. accepted-and-documented residual race); do not silently pick one without noting the tradeoff in the plan itself.

## Code Examples

### Recommended `UndoGroupTypes.kt` (NEW file) — sealed results, no `UndoStatus` member added

```kotlin
package io.github.ygaray.yahirandroidtaste.feedback

/** D-01: group-level lifecycle, derived from member statuses — never stored as a field. */
sealed interface UndoGroupStatus {
    data object Undoable : UndoGroupStatus          // all members Available
    data object PartiallyResolved : UndoGroupStatus // >=1 Available AND >=1 spent
    data object FullyResolved : UndoGroupStatus      // 0 Available (all spent)
    data object Empty : UndoGroupStatus              // unknown groupId, or fully evicted
}

/** D-01: result of an atomic attemptUndoGroup call. */
sealed interface UndoGroupResult {
    data class Undone(val count: Int) : UndoGroupResult
    data class Refused(val reason: String, val changedItem: String? = null) : UndoGroupResult
    data object Failed : UndoGroupResult
    data object NothingToUndo : UndoGroupResult
}

/**
 * Typed refusal the consumer's `undoAll` lambda throws to signal a Refused outcome (D-01) —
 * "nothing was written" semantics. Any OTHER exception type maps to UndoGroupResult.Failed.
 */
class UndoGroupRefusedException(
    val reason: String,
    val changedItem: String? = null
) : Exception(reason)
```
*(Illustrative — exact file/placement is a planning decision; sealed-type shape and field names match D-01's frozen text verbatim. `[ASSUMED]` — this is original synthesis to satisfy D-01's requirements, not itself a quoted/verified source.)*

### Recommended `UndoHistoryStore.kt` additions (illustrative skeleton)

```kotlin
// Added fields:
private val groupIdByEntryId = mutableMapOf<String, String>()
private data class GroupMeta(val label: String, val undoAll: suspend () -> Unit)
private val groupMeta = mutableMapOf<String, GroupMeta>()
private val groupUndoMutex = Mutex()

fun openGroup(groupId: String, label: String, undoAll: suspend () -> Unit) {
    groupMeta[groupId] = GroupMeta(label, undoAll)
}

fun append(message: String, preview: UndoPreview? = null, groupId: String, undoAction: suspend () -> Unit): String {
    val entry = UndoHistoryEntry(
        id = UUID.randomUUID().toString(), message = message,
        timestamp = System.currentTimeMillis(), undoAction = undoAction, preview = preview
    )
    groupIdByEntryId[entry.id] = groupId
    _entries.update { current -> evictIfNeeded(listOf(entry) + current) } // eviction now group-aware
    return entry.id
}

fun group(groupId: String): List<UndoHistoryEntry> =
    _entries.value.filter { groupIdByEntryId[it.id] == groupId } // already newest-first (source list is)

fun groupIdOf(entryId: String): String? = groupIdByEntryId[entryId]
fun groupLabel(groupId: String): String? = groupMeta[groupId]?.label

fun groupStatus(groupId: String): UndoGroupStatus {
    val members = group(groupId)
    if (members.isEmpty()) return UndoGroupStatus.Empty
    val availableCount = members.count { it.status == UndoStatus.Available }
    return when {
        availableCount == members.size -> UndoGroupStatus.Undoable
        availableCount == 0 -> UndoGroupStatus.FullyResolved
        else -> UndoGroupStatus.PartiallyResolved
    }
}

@Suppress("TooGenericExceptionCaught")
suspend fun attemptUndoGroup(groupId: String): UndoGroupResult = groupUndoMutex.withLock {
    val meta = groupMeta[groupId] ?: return@withLock UndoGroupResult.NothingToUndo
    val claimed = group(groupId).filter { it.status == UndoStatus.Available }
    if (claimed.isEmpty()) return@withLock UndoGroupResult.NothingToUndo

    // NOTE: deliberately NO tryConsume() call here (Pitfall 1) -- the Mutex IS the atomicity
    // boundary; nothing is marked until the outcome is known.
    try {
        meta.undoAll()
        val claimedIds = claimed.map { it.id }.toSet()
        _entries.update { current ->
            current.map { if (it.id in claimedIds) it.withStatus(UndoStatus.Undone) else it }
        }
        UndoGroupResult.Undone(count = claimed.size)
    } catch (e: CancellationException) {
        throw e
    } catch (e: UndoGroupRefusedException) {
        // Refused: nothing was written -- entries were never modified, no "release" needed.
        UndoGroupResult.Refused(reason = e.reason, changedItem = e.changedItem)
    } catch (e: Exception) {
        val claimedIds = claimed.map { it.id }.toSet()
        _entries.update { current ->
            current.map { if (it.id in claimedIds) it.withStatus(UndoStatus.Failed) else it }
        }
        UndoGroupResult.Failed
    }
}

// clearSpent() and evictIfNeeded() both need a groupId-aware branch -- see Pitfall 2.
```
`[ASSUMED]` — this is this research's own mechanism-level synthesis against the frozen D-01 signatures; it is NOT itself quoted from any existing source (no such code exists in the repo yet). The plan must review, refine, and is free to diverge on internal details (e.g., mutex granularity) as long as the external D-01 signatures and behavioral contract are met.

### Recommended consumer-facing UI projection models (new, in `model/`)

```kotlin
// model/UndoRowUiModel.kt
sealed interface UndoRowState {
    data class Available(val onUndo: () -> Unit) : UndoRowState
    data object Undone : UndoRowState
    data class Unavailable(val reason: String) : UndoRowState
}

data class UndoRowUiModel(val id: String, val label: String, val state: UndoRowState)

// model/UndoRefusedUiModel.kt
data class UndoRefusedUiModel(val reason: String, val changedItem: String? = null)

// model/UndoAffordanceUiModel.kt
data class UndoAffordanceUiModel(
    val allLabel: String,
    val rows: List<UndoRowUiModel> = emptyList(),
    val onUndoAll: (() -> Unit)? = null,
    val refused: UndoRefusedUiModel? = null
)
```
Field names/shapes are copied verbatim from D-01's frozen text (`[CITED: 11-CONTEXT.md D-01]`) — not this research's own invention, unlike the store-side mechanism above.

### Extending `OutcomeSheet.kt`'s `SuccessBody` (illustrative — exact diff is a planning decision)

```kotlin
// VoiceOutcomeUiState.Success gains one new field (additive, defaulted null):
// val undo: UndoAffordanceUiModel? = null   // D-02: undo lives ON Success

@Composable
private fun SuccessBody(success: VoiceOutcomeUiState.Success) {
    Column(/* unchanged */) {
        Text(success.summary, style = MaterialTheme.typography.headlineSmall)
        success.handledBy?.let { HandledByRow(it) }
        success.batchResults.takeIf { it.isNotEmpty() }?.let { BatchResultsList(it) }
        success.editableContent?.takeIf { !success.inFlight }?.invoke()
        success.undo?.let { UndoAffordanceBody(it) }   // NEW
    }
}

@Composable
private fun UndoAffordanceBody(undo: UndoAffordanceUiModel) {
    Column {
        undo.onUndoAll?.let { onUndoAll ->
            DynamicActionButton(label = undo.allLabel, role = ActionButtonDefaults.ActionButtonRole.Neutral, onClick = onUndoAll)
        }
        undo.rows.forEach { row -> UndoRowItem(row) }
        undo.refused?.let { refused ->
            // Loud per VOUT-03's discipline: error container, not a muted caption.
            Surface(color = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer) {
                Text("Couldn't undo: ${refused.reason}" + (refused.changedItem?.let { ", $it changed since" } ?: ""))
            }
        }
    }
}
```

### Clarification choices, reusing `AppChip` (VCLAR-01)

```kotlin
// New file: model/ClarificationOptionUiModel.kt
data class ClarificationOptionUiModel(val id: String, val label: String) // id opaque -- library never interprets it

// New file: component/ClarificationBar.kt -- head token "Clarification" needs a
// PRIMITIVE_NOUN_ALLOWLIST entry in the SAME commit (Pitfall 3).
@Composable
fun ClarificationBar(
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
                AppChip(label = option.label, isSelected = false, onClick = { onSelect(option.id) })
            }
        }
        TextButton(onClick = onDismiss) { Text("Dismiss") }
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|-------------------|---------------|--------|
| Prior `11-RESEARCH.md`'s recommendation: "never touch `UndoHistoryStore`, build a pure consumer-side presentational projection" | The frozen D-01 amendment (2026-09-30 16:13, commit `2e02abf`): an additive STORE-side grouping API IS required, alongside the presentational projection | This session — the R1-seam reconvene resolved the ambiguity the prior research correctly flagged as unresolved (its own Open Question 2) | The store-side mechanism design (this file's primary content) did not exist in the prior research at all; the prior research's "don't touch the store" framing is now superseded and must not be carried forward into the plan |
| Prior research's speculative `UndoRowUiModel`/`UndoAffordanceUiModel` field names | D-01's frozen text now specifies these exact field names/shapes verbatim | Same reconvene | Field names in this research's Code Examples are copied from the frozen text, not re-guessed |

**Deprecated/outdated:** The prior `11-RESEARCH.md` (now overwritten by this file) and the two plans it fed (`11-01-PLAN.md`, `11-02-PLAN.md`, both retired to `superseded/` per commit `d5d426b`) — do not resurrect either plan's undo-shape sections; they predate the frozen D-01 seam.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|----------------|
| A1 | A single store-wide `Mutex` (not per-group) is sufficient for `attemptUndoGroup` | Architecture Patterns, Alternatives Considered | Low — a single-user session library has no realistic concurrent-multi-group-undo scenario; if wrong, upgrading to a per-group `Mutex` map is a localized internal change, no external signature impact |
| A2 | The concrete claim/resolve mechanism (Mutex-guarded, no `tryConsume()` involvement, no separate "claimed" set) fully satisfies D-01's "releasable claim" requirement | Code Examples, Pitfall 1 | Medium — this is original synthesis, not verified against any authoritative source (no such code exists yet); the plan should sanity-check this against a concrete concurrent-Refused-then-retry unit test before considering it settled |
| A3 | The `attemptUndo(id)`/`attemptUndoGroup` concurrent-race gap (Pitfall 4) should be mitigated at the UI/consumer layer (a busy flag), not the store layer | Common Pitfalls, Pitfall 4 | Medium — D-01's text is silent on this interaction entirely; if the plan decides a store-level fix is actually required, that would need to touch `attemptUndo(id)`'s behavior, which D-01 says must stay "unchanged" — flagging this tension explicitly is the main point, not asserting my mitigation is the only valid one |
| A4 | `ClarificationBar` is a reasonable head-token choice that needs adding to `PRIMITIVE_NOUN_ALLOWLIST` | Pitfall 3, Code Examples | Low — any allowlisted-or-added head token works structurally; the plan is free to pick a different name as long as it clears the same gate |

**If this table is empty:** N/A — see above; A1/A4 are low-risk implementation-detail recommendations, A2/A3 are the genuinely load-bearing design points the plan must scrutinize before treating the undo mechanism as settled.

## Open Questions

1. **Does the plan accept the `attemptUndo(id)`/`attemptUndoGroup` concurrent-race gap (Pitfall 4), or build UI-level mutual exclusion against it?**
   - What we know: D-01's frozen text does not address this interaction; `attemptUndo(id)` must stay unchanged per D-01.
   - What's unclear: whether SB's/CT's real UI ever exposes both a per-item Undo AND a live "Undo all" for the same row simultaneously in a way that makes the race practically reachable.
   - Recommendation: plan should make and document an explicit choice (see Pitfall 4's two options) rather than let it be an unstated side effect of implementation order.

2. **Mutex granularity: one store-wide `groupUndoMutex`, or per-group?**
   - What we know: a single mutex is simpler and sufficient for the single-user-session case this library targets.
   - What's unclear: whether any consumer app ever fires two `attemptUndoGroup` calls for two DIFFERENT groups genuinely concurrently (e.g., two independent voice commands' outcome sheets open at once) — if so, a single mutex serializes them, which is a correctness non-issue but could be a latency concern.
   - Recommendation: default to one mutex (per Alternatives Considered); revisit only if a concrete concurrent-groups UI flow surfaces.

3. **Exact new-file layout: does `UndoGroupStatus`/`UndoGroupResult`/`UndoGroupRefusedException` live in one new `UndoGroupTypes.kt`, or split across `UndoHistoryStore.kt`'s own file (companion types) per this repo's convention (e.g. `KeyFieldState.kt` is its own file, `ActionButtonDefaults` lives beside `DynamicActionButton` in the same file)?**
   - What we know: both conventions exist live in this repo (small sealed-type-per-file, and object-plus-enum-in-same-file-as-its-composable).
   - What's unclear: no single rule in this repo dictates which to follow for a non-composable sealed-result type living in `feedback/`.
   - Recommendation: a single `UndoGroupTypes.kt` is fine (keeps `UndoHistoryStore.kt` from growing unbounded) — this is a purely organizational call for the plan to make, not a design-risk item.

## Environment Availability

No new external dependency, service, or CLI tool. `kotlinx.coroutines.sync.Mutex` ships inside the already-transitively-present `kotlinx-coroutines-core` artifact (confirmed: `UndoHistoryStore.kt` already imports sibling `kotlinx.coroutines.*` symbols from the same artifact). No environment audit table needed.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric + Compose UI test (`androidx.compose.ui.test.junit4`) + `kotlinx-coroutines-test` (confirmed in `build.gradle.kts` lines 126-134) |
| Config file | `build.gradle.kts` (module root) |
| Quick run command | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.feedback.*"` (scope to `UndoHistoryStore`/new group-types tests during development) |
| Full suite command | `./gradlew testDebugUnitTest` (runs both drift guards — full-suite-only) |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| VOUT-01/02/03 | Already covered | unit/Robolectric Compose test | `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"` | ✅ Already exists (`OutcomeSheetTest.kt`, verified) |
| VUNDO-01 (store) | `openGroup`/grouped `append`/`group`/`groupIdOf`/`groupLabel`/`groupStatus`/`attemptUndoGroup` behave per D-01 (incl. Refused-then-retry, group-atomic eviction/clearSpent) | pure-Kotlin JUnit unit test (no Compose needed — store has zero Android/Compose imports) | `./gradlew testDebugUnitTest --tests "*UndoHistoryStoreTest*"` | ❌ Wave 0 — new test file, or extend an existing `UndoHistoryStoreTest.kt` if one already exists (grep before assuming new) |
| VUNDO-01 (UI) | `OutcomeSheet`'s `Success.undo` renders "Undo all (N)", per-row Available/Undone/Unavailable, nested Refused surface loudly | unit/Robolectric Compose test | extend `OutcomeSheetTest.kt` | ❌ Wave 0 — new test cases in the existing file |
| VCLAR-01 | `ClarificationBar` renders question + options; `onSelect`/`onDismiss` fire correctly | unit/Robolectric Compose test | `./gradlew testDebugUnitTest --tests "*ClarificationBarTest*"` | ❌ Wave 0 (new file) |
| Naming-guard compliance | New head token(s) clear both drift guards | existing full-suite guard tests | `./gradlew testDebugUnitTest --tests "*DriftGuardTest*"` | ✅ guards exist; must be satisfied |
| API additivity | `v2.4.0`-in-progress diff vs `v2.3.0` stays additive | static/build-time | `./gradlew apiCheck` | ✅ tooling exists |

**Before authoring tests, `grep -rl "UndoHistoryStore" src/test/` to check whether a `UndoHistoryStoreTest.kt` already exists from Phase 53/58 — extend it rather than creating a duplicate test class if so.**

### Sampling Rate

- **Per task commit:** the scoped test command for the file touched.
- **Per wave merge:** `./gradlew testDebugUnitTest` (full suite — required for both drift guards).
- **Phase gate:** full suite green before `/gsd-verify-work`; Gate-1 self-UAT must explicitly drive the undo-refused/partial branch and the Failure branch on-device, not just the happy path (CONTEXT.md's "Specific Ideas" note).

### Wave 0 Gaps

- [ ] `UndoHistoryStoreTest.kt` (new or extended) — covers the 7-member grouping API, atomicity, releasability-on-Refused, group-atomic eviction/clearSpent
- [ ] New test cases in `OutcomeSheetTest.kt` — covers the `undo` field rendering (Undo all, per-row states, nested Refused)
- [ ] `ClarificationBarTest.kt` (new file) — covers VCLAR-01
- [ ] No new shared fixtures needed — this repo's per-family fixture-function convention (`explorer/VoiceCommandFamilyScreen.kt`) already covers gallery demo needs

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|----------------|---------|-------------------|
| V2 Authentication | No | No auth surface |
| V3 Session Management | No | No session concept beyond the existing in-memory `UndoHistoryStore` |
| V4 Access Control | No | Library never gates access, only renders/stores display state |
| V5 Input Validation | Marginal/Yes | Clarification `id` and group `label`/`reason`/`changedItem` strings are OPAQUE — library never parses/executes them, only displays and echoes them back verbatim via callbacks |
| V6 Cryptography | No | No cryptographic material touched |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|------------------------|
| A consumer's `undoAll`/`undoAction` lambda throws an exception containing a secret (e.g. an API error body with a token) | Information Disclosure | Out of library control (it only catches/classifies `Exception`, never logs the message) — the library must not log any caught exception's message anywhere, matching the existing `attemptUndo`'s `@Suppress("TooGenericExceptionCaught")` best-effort-never-crash convention |
| A duplicate `groupId` reused across two unrelated voice commands | Tampering (logic-level) | `openGroup`/grouped `append` accept whatever `groupId` string the consumer supplies — uniqueness is the CONSUMER's responsibility; document this in the new store method KDoc, mirroring the existing clarification-`id`-uniqueness note in the prior research |

## Sources

### Primary (HIGH confidence — read directly this session)

- `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryEntry.kt` (full file, 66 lines) — current shape, `internal` ctor, `AtomicBoolean` one-way guard (quoted)
- `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStore.kt` (full file, 122 lines) — `append`/`attemptUndo`/`clearSpent`/`evictIfNeeded` (quoted in full)
- `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoPreview.kt` (full file) — sibling shape, confirms no `AtomicBoolean`-style special-casing elsewhere in `feedback/`
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/VoiceOutcomeUiState.kt`, `HandledByUiModel.kt`, `FailureActionUiModel.kt`, `BatchRowResultUiModel.kt` (full files) — confirm D-04/D-05/D-06/D-08 already shipped
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt` (full file, 167 lines) — confirms VOUT-01/02/03 already shipped, exact current `SuccessBody`/`FailureBody`/`HandledByRow` shapes
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt` (full file, 131 lines) — confirms existing test coverage scope
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` (full file) — registry integrity invariants, confirmed `voiceCommandFamilyEntries` concatenation live
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt` (grepped + partial read) — confirms `OutcomeSheet` entry already registered with a full states matrix and variants content
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` (full file, 415 lines) — current `PRIMITIVE_NOUN_ALLOWLIST`/`DOMAIN_VOCABULARY` contents (quoted in full), confirmed `"Outcome"` present, `"Clarification"` absent
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt` (full file) — the `copy()`/ABI-break lesson (not directly triggered this phase since `UndoHistoryEntry`'s ctor is untouched, but corroborates why D-01 forbids touching it)
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/AppChip.kt`, `ApproachLadderCard.kt`, `SegmentedOptionSelector.kt`, `SheetScaffold.kt`, `DynamicActionButton.kt`, `AttentionCue.kt` (full files) — reusable pressable-token, host/content, role-button, and "never a failure signal" precedent
- `src/main/java/io/github/ygaray/yahirandroidtaste/theme/Color.kt` (partial, lines 1-50) — verified `ErrorRed`/`ErrorRedContainer`/`OnErrorRedContainer` raw values
- `src/main/java/io/github/ygaray/yahirandroidtaste/model/ApproachRungUiModel.kt`, `KeyFieldState.kt` — all-val/sealed-interface conventions to mirror for new models
- `build.gradle.kts` (grep) — confirmed test dependency set, confirmed no `kotlinx-coroutines-*` explicit `implementation` line is needed (already transitive)
- `.planning/config.json` — confirmed `nyquist_validation: true`, `security_enforcement: true`, `security_asvs_level: 1`
- `.planning/phases/11-voice-outcome-failure-sheet/11-CONTEXT.md` (full file, current, post-amendment) — the frozen D-01 text quoted in full above
- `git log`/`git show 2e02abf` — confirms the exact amendment commit and its diff scope (CONTEXT.md only, 20 lines added, no code)
- `.planning/REQUIREMENTS.md`, `.planning/STATE.md` — milestone-level requirement text and current phase status

### Secondary (MEDIUM confidence)

- The prior (now-superseded) `11-RESEARCH.md` and the two retired `superseded/11-01-PLAN.md`/`11-02-PLAN.md` — read for context on what already executed (commit `6a946d3`) and what was explicitly deferred; their undo-shape sections are superseded and NOT carried forward.

### Tertiary (LOW confidence)

- None — no WebSearch/external lookups needed; every technical claim resolves against in-repo source read this session, and the store-side mechanism (marked `[ASSUMED]` throughout) is original design synthesis against a fully-specified frozen contract, not an external-source claim.

## Metadata

**Confidence breakdown:**
- Already-shipped VOUT-01/02/03 status: HIGH — every file read in full this session, matches D-04/D-05/D-06/D-08 exactly
- Current `UndoHistoryStore`/`UndoHistoryEntry` shape: HIGH — both files read in full, quoted verbatim
- Frozen D-01 external contract (signatures, behavior): HIGH — quoted verbatim from `11-CONTEXT.md`, SB-confirmed per the commit message
- Recommended internal mechanism (Mutex-based claim, group-aware eviction/clearSpent): MEDIUM — original synthesis, logically sound against the verified constraints, but not itself verified against any authoritative source since no such code exists yet; flagged `[ASSUMED]` throughout and the two riskiest sub-decisions (A2, A3) are logged explicitly
- Naming-guard status for the remaining work: HIGH — `DomainVocabularyDriftGuardTest.kt` read in full, `"Clarification"` absence confirmed via direct grep this session

**Research date:** 2026-09-30
**Valid until:** Until the plan author either confirms or revises the Mutex-granularity/race-mitigation choices (Open Questions 1-2) — those are implementation decisions this research recommends but does not lock. Re-verify `PRIMITIVE_NOUN_ALLOWLIST`'s exact contents immediately before authoring if any other phase lands concurrently.
