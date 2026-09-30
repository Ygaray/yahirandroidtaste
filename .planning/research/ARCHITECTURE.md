# Architecture Research

**Domain:** Additive integration of a prop-driven AI-voice UI cohort into an existing reusable Jetpack Compose design-system library (`yahirandroidtaste`)
**Researched:** 2026-09-29
**Confidence:** HIGH (grounded in the live source: `ComponentRegistry.kt`, `ComponentRegistryDriftGuardTest.kt`, `ExplorerIndexScreen.kt`, `model/`, `API.md`)

## Executive Answer (the five questions, up front)

1. **Package:** put every new composable **flat in `component/`** — NOT a `component/voice/` sub-package. All ~60 existing components are flat; the "families" are a *registry taxonomy*, not a package layout. **Family:** register the whole cohort in **one new tenth registry family, "Voice Command"** (`voiceCommandFamilyEntries`), mirroring how Progress/Metrics (family 8) and Tactile Foundation (family 9) each landed as a cohesive new-family drop. Do **not** scatter the cohort across Cards/Sheets/Feedback.
2. **Prop models:** all-`val` immutable data classes in `model/` — `VoiceProviderUiModel`, `VoiceModelUiModel`, `CommandTierUiModel` (the ladder is `List<CommandTierUiModel>`), `HandledByUiModel`, a **sealed `VoiceOutcomeUiState`**, and `ConfirmRequestUiModel` + `ProposedItemUiModel` (batch is `List`, single = list-of-1). Generic field names only; the consumer maps engine output → these at the call site.
3. **P11 ↔ P12:** **one composable with a single sealed state param**, not siblings. The ROADMAP is explicit ("extends the outcome sheet with a confirmation state", P12 depends on P11). Ship the sealed `VoiceOutcomeUiState` type in P11 (Success / Failure) so P12 is purely additive — a new `NeedsConfirmation` subtype + one `when` branch, no signature change → Metalava stays additive.
4. **Build order:** P10 ∥ P11 (independent) → P12 (gates on P11) → P13 (gates on all). Integration points: `SheetScaffold`, `ClearableTextField`, `SegmentedOptionSelector`, `AppChip`/`ChipBar`, `DynamicActionButton`. **Do not** use `AttentionCue` for failure (its KDoc forbids it) or `ConfirmationDialog` for the confirm state (roadmap wants it *in* the sheet).
5. **Gallery:** one `ComponentRegistry.Entry` per new composable in `voiceCommandFamilyEntries`, each with `tier = PATTERN` (required, no default) + a 4-cell states matrix; add the family to `ExplorerFamilies` consts + `ORDERED_KEYS`. Fake fixtures live in the family-screen file (auto-excluded from the drift guard — `explorer/` is denylisted).

> ⚠ **Doc-drift flag:** `CLAUDE.md` and the milestone brief say "**seven** ComponentRegistry family lists." The live `ComponentRegistry.entries` concatenates **nine** (`cards + chips + sheets + buttonsFab + pickers + feedback + emptyState + progress + tactileFoundation`). Adding Voice Command makes **ten**. P13 should correct the "seven" wording where it appears.

## Standard Architecture

### System Overview — where the cohort sits

```
┌──────────────────────────────────────────────────────────────────────┐
│  CONSUMER APP (SecondBrain / CalTracker) — owns the domain + engine    │
│  ┌────────────────────┐   maps engine outcome → props   ┌───────────┐ │
│  │ voice-action-engine │ ─────────────────────────────► │ call site │ │
│  │ (OkHttp, tiers…)    │                                 └─────┬─────┘ │
│  └────────────────────┘                                        │ props │
└────────────────────────────────────────────────────────────────┼──────┘
                                one-way dependency (INV-01)        ▼
┌──────────────────────────────────────────────────────────────────────┐
│  yahirandroidtaste LIBRARY  (Android SDK + Compose + Hilt + …only)     │
│                                                                        │
│  model/  (immutable, domain-free)     component/  (flat, @Composable)  │
│  ┌───────────────────────────┐        ┌────────────────────────────┐  │
│  │ VoiceProviderUiModel      │        │ VoiceProviderSettingsCard   │  │
│  │ VoiceModelUiModel         │  props │ VoiceModelSettingsCard      │  │
│  │ CommandTierUiModel        │ ─────► │ CommandApproachSettingsCard │  │
│  │ HandledByUiModel          │        │ VoiceOutcomeSheet(+Content) │  │
│  │ VoiceOutcomeUiState (seal)│        └──────────────┬─────────────┘  │
│  │ ConfirmRequestUiModel     │           reuses      │                 │
│  │ ProposedItemUiModel       │        ┌──────────────▼─────────────┐  │
│  └───────────────────────────┘        │ SheetScaffold, AppChip,    │  │
│                                        │ ClearableTextField,        │  │
│  explorer/  (denylisted from guard)    │ SegmentedOptionSelector,   │  │
│  ┌───────────────────────────┐        │ DynamicActionButton        │  │
│  │ voiceCommandFamilyEntries │        └────────────────────────────┘  │
│  │ (registry + gallery)      │  ← ComponentRegistry.entries (SSOT)     │
│  └───────────────────────────┘                                        │
└──────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Implementation |
|-----------|----------------|----------------|
| `component/*` (new files, flat) | Render voice settings/outcome UI from props+callbacks; hold no key, make no network call | Presentation-only `@Composable`, hoisted state, matches existing flat-package convention |
| `model/*` (new immutable models) | Carry the domain-neutral shapes the consumer maps its engine output into | All-`val` `data class` / sealed interface — Compose-`STABLE`-inferable |
| `voiceCommandFamilyEntries` (new `explorer/VoiceCommandFamilyScreen.kt`) | Register the cohort + drive its gallery detail pages | Slice of `ComponentRegistry.entries`, one `Entry` per composable, `tier=PATTERN` |
| `ExplorerFamilies` (edit) | Declare + order the new family | Add const `VOICE_COMMAND` and an `ORDERED_KEYS` row |

## Recommended Project Structure

```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/                          # FLAT — every component lives here (no sub-packages)
│   ├── VoiceProviderSettingsCard.kt    # P10 — provider select + API-key entry
│   ├── VoiceModelSettingsCard.kt       # P10 — model select
│   ├── CommandApproachSettingsCard.kt  # P10 — tier ladder + offline-only + max-tier cap
│   └── VoiceOutcomeSheet.kt            # P11 (Success/Failure) → P12 (NeedsConfirmation)
│                                       #   host + `VoiceOutcomeSheetContent` body (split convention)
├── model/                              # immutable, all-val, domain-free UI models
│   ├── VoiceProviderUiModel.kt
│   ├── VoiceModelUiModel.kt
│   ├── CommandTierUiModel.kt
│   ├── HandledByUiModel.kt
│   ├── VoiceOutcomeUiState.kt          # sealed: Success | Failure | NeedsConfirmation
│   ├── ConfirmRequestUiModel.kt
│   └── ProposedItemUiModel.kt
└── explorer/                           # denylisted from the drift guard
    └── VoiceCommandFamilyScreen.kt     # voiceCommandFamilyEntries + fake fixtures + family screen
```

### Structure Rationale

- **Flat `component/`:** 60 existing components are flat; the drift guard, `API.md`, and every `*FamilyScreen.kt` import from `component.X` flat. A `component/voice/` sub-package would be the *only* nested one — inconsistent, and buys nothing (the family taxonomy already provides logical grouping). Note: it would still be drift-guard-safe (the guard denylists only `explorer/`, so any package under the scan root is covered), so the reason to avoid it is convention, not safety.
- **One new family, not scatter:** Cards is specifically the five card archetypes (Text/List/Album/Voice) — settings cards are an altitude mismatch there. A cohesive family keeps the gallery legible and honors this project's *stewardship/coherence* mandate. Direct precedent: Tactile Foundation (Phase 123, `DS-01`) shipped as one cohesive family.
- **Host-vs-content split for the sheet:** existing sheets ship both `…Sheet` (owns `ModalBottomSheet`) and `…Content` (body only) and register both; `VoiceOutcomeSheet` should follow so a consumer can embed the body in its own host.

## Architectural Patterns

### Pattern 1: One outcome sheet, sealed state param (P11 → P12)

**What:** A single `VoiceOutcomeSheet(state: VoiceOutcomeUiState, …)` renders all outcome modes via a `when`.
**When:** Whenever P11 and P12 are the same visual surface differing only by mode — which the ROADMAP asserts.
**Trade-offs:** One registered composable + one gallery tile (simpler catalog) vs. a slightly larger `when`. The additive win is decisive: shipping the sealed type in P11 makes P12 a new subtype + branch with **zero** signature change → Metalava `apiCheck` passes trivially.

```kotlin
sealed interface VoiceOutcomeUiState {
    data class Success(val summary: String, val handledBy: HandledByUiModel) : VoiceOutcomeUiState
    data class Failure(val reason: String, val handledBy: HandledByUiModel?) : VoiceOutcomeUiState
    // Added in P12 — purely additive:
    data class NeedsConfirmation(val request: ConfirmRequestUiModel) : VoiceOutcomeUiState
}
// Carry confirm/cancel on the state arm (cohesion — they only exist for confirmation),
// or as nullable sheet callbacks. Prefer on the arm.
```

### Pattern 2: Single-or-batch as a uniform list

**What:** `ConfirmRequestUiModel(reason: String, items: List<ProposedItemUiModel>)`; a single risk-confirm is a list of size 1, a batch weak-match is size N.
**When:** The one shape must satisfy both SB `MutationGate`/`VoiceConfirmGate` risk-confirm (single) and CT weak-match (single **or** batch) with **no** library change per consumer (VOUT-04, success criterion 4).
**Trade-offs:** Consumer always wraps even a single item — trivial. The alternative (a `Single | Batch` sealed split) forces a library edit if a consumer's cardinality assumption shifts; the list shape is future-proof and domain-neutral.

### Pattern 3: Immutable, `STABLE`-inferable models (the `TagChipUiModel` lesson)

**What:** Every new `model/` type is all-`val`. Avoid the `var color` trap that made `TagChipUiModel` un-`STABLE` and forced an out-of-primary-constructor workaround.
**When:** Always, for a widely-shared prop type on many composables.
**Trade-offs:** None here — voice models have no ABI-history to preserve, so start clean and immutable, keeping every voice composable skippable by the Compose runtime.

## Data Flow

### Prop-in / callback-out (no state held in the library)

```
[engine outcome / settings]  (consumer)
        ↓ map to model
[VoiceOutcomeUiState / *UiModel]  → prop → [Composable] → render
        ↑ callback (onSelectProvider / onToggleOffline / onConfirm / onCancel)
[consumer handles it]  (persists key, calls engine — all outside the library)
```

**Key flows:**
1. **Settings (P10):** consumer passes current provider/model/ladder/toggles as props; card emits selection/toggle/cap changes via callbacks. Library persists nothing, makes no network call (success criterion 1).
2. **Outcome (P11):** consumer maps the engine result to `Success`/`Failure` incl. `HandledByUiModel`; the sheet renders it, failure loud.
3. **Confirmation (P12):** consumer maps a pending gate to `NeedsConfirmation(ConfirmRequestUiModel)`; confirm/cancel callbacks return the decision to the consumer.

## Registry & Gallery Wiring (Question 5, concretely)

Each new composable needs **one** `ComponentRegistry.Entry` in `voiceCommandFamilyEntries`:

```kotlin
ComponentRegistry.Entry(
    name = "VoiceOutcomeSheet",              // MUST match the fn name exactly (drift-guard scans source text)
    family = ExplorerFamilies.VOICE_COMMAND, // new const
    tier = ComponentRegistry.Tier.PATTERN,   // required, no default — domain nouns in name/params
    states = listOf(
        StateCell("Default"),                        // Success variant
        StateCell("Pressed / Selected"),             // real cell or curated null + reason
        StateCell("Disabled"),
        StateCell("Focused"),
    ),
    content = { VoiceOutcomeSheetVariants() },        // Success / Failure / single-confirm / batch-confirm demos
)
```

Then edit `ExplorerFamilies`: add `const val VOICE_COMMAND = "voice_command"` and a `VOICE_COMMAND to "Voice Command"` row in `ORDERED_KEYS`; append `voiceCommandFamilyEntries` to the `ComponentRegistry.entries` concatenation. The outcome sheet's states matrix is the natural place to showcase Success/Failure and single-vs-batch confirm — making the gallery the living proof that both consumers' shapes render. Fake fixtures go **in the family-screen file** (infrastructure; `explorer/` is the drift guard's only denylisted package).

## Anti-Patterns

### Anti-Pattern 1: Using `AttentionCue` for failure states
**What people do:** Reuse `AttentionCue` (Feedback family) for P11's loud failure.
**Why it's wrong:** Its KDoc explicitly says it is a *"Caution/verify signal glyph — **never** a failure signal."* Using it for failure violates the component's own contract and undercuts the "loud, visible failure" requirement (VOUT-03).
**Do this instead:** Give failure a distinct loud treatment (error-color container + icon) inside the sheet body — a small internal element or an error-tinted surface, not the caution glyph.

### Anti-Pattern 2: A separate confirmation dialog/sibling for P12
**What people do:** Reach for `ConfirmationDialog` (Feedback family) or a new sibling composable.
**Why it's wrong:** The ROADMAP requires the confirm state to live **in the outcome sheet** (VOUT-04, "the outcome sheet renders a needs-confirmation state"); a sibling fragments the surface and forces a second gallery tile + registry entry.
**Do this instead:** The sealed-state param on `VoiceOutcomeSheet` (Pattern 1). Reuse `DynamicActionButton` for confirm (save/destructive role) and cancel (neutral).

### Anti-Pattern 3: A `component/voice/` sub-package
**What people do:** Nest the new files to "group" them.
**Why it's wrong:** Breaks the established flat convention (60 flat files, every import is `component.X`) for zero benefit — grouping already comes from the registry family.
**Do this instead:** Flat `component/`, grouped via the `voiceCommandFamilyEntries` registry family.

### Anti-Pattern 4: A `var` field on a shared model
**What people do:** Add mutable styling metadata to a model (as `TagChipUiModel.color` had to, for ABI reasons).
**Why it's wrong:** Any `var` disqualifies the class from Compose `STABLE` inference, de-optimizing every composable that takes it.
**Do this instead:** All-`val`. These are new models with no ABI history to protect.

## Integration Points

### Reused existing primitives

| New surface | Reuse | Notes |
|-------------|-------|-------|
| `VoiceProviderSettingsCard` | `ClearableTextField` (key entry), `AppChip`/`ChipBar` or `SortControl` (provider select), `DynamicActionButton` (apply) | Holds no key; emits via callback |
| `VoiceModelSettingsCard` | `AppChip`/`ChipBar` or `SegmentedOptionSelector` | Model list from props |
| `CommandApproachSettingsCard` | `SegmentedOptionSelector` (offline-only toggle + max-tier cap — 2-option toggle w/ disabled+reason), ordered rows for the ladder | Ladder = `List<CommandTierUiModel>`; ladder render is new |
| `VoiceOutcomeSheet` | `SheetScaffold` (host + drag handle), `AppChip`/`CountBadge` (handled-by indicator), `DynamicActionButton` (confirm/cancel) | Host/content split; failure ≠ `AttentionCue` |

### Internal boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| consumer ↔ library | props in / callbacks out | one-way (INV-01); no OkHttp, no engine module, no consumer import |
| composable ↔ registry | `Entry` in `voiceCommandFamilyEntries` | name string must match fn name; drift guard is a source-text scan |
| family ↔ explorer index | `ExplorerFamilies.ORDERED_KEYS` | add the tenth row |

## Build Order (P10 → P13)

1. **P10 ∥ P11** (independent per ROADMAP). Land the models each needs. **Critical:** define the sealed `VoiceOutcomeUiState` with `Success` + `Failure` in P11 *and* leave the sheet's param typed as the sealed interface, so P12 is additive.
2. **P12** (gates on P11): add `NeedsConfirmation` subtype + `ConfirmRequestUiModel`/`ProposedItemUiModel` + one `when` branch. No signature change.
3. **P13** (gates on all): register every new composable in `voiceCommandFamilyEntries` with `tier=PATTERN` + 4-cell states; add the family to `ExplorerFamilies`; run the drift guard, Metalava `apiCheck` (strictly additive vs `v2.3.0`), detekt (zero baseline); correct the "seven families" doc drift; cut `v2.4.0`.

## Validation gates the cohort must clear (P13)

- **ComponentRegistryDriftGuardTest** — every new public top-level `@Composable` registered XOR allowlisted (denylist scan already covers new files/packages automatically).
- **Metalava `apiCheck`** — additive only; the sealed-type-in-P11 approach keeps P12 additive.
- **detekt** — zero baseline; do not regenerate a baseline to bury findings.
- **Registry `init` invariants** — no duplicate `Entry.name`, no entry both registered and allowlisted.
- **INV-01** — no OkHttp / `voice-action-engine` dependency; every composable prop-driven.

## Sources

- `src/main/java/.../explorer/ComponentRegistry.kt` — `Entry` shape, required `tier`, 9-list concatenation, `init` invariants (HIGH)
- `src/test/java/.../explorer/ComponentRegistryDriftGuardTest.kt` — denylist scan (`explorer/` only), source-text detection (HIGH)
- `src/main/java/.../explorer/ExplorerIndexScreen.kt` — `ExplorerFamilies` consts + `ORDERED_KEYS` (HIGH)
- `src/main/java/.../model/TagChipUiModel.kt` — the `var`/Compose-`STABLE` lesson (HIGH)
- `API.md` — host-vs-content split convention, `AttentionCue` "never a failure signal", `SheetScaffold`/`SegmentedOptionSelector`/`ClearableTextField`/`DynamicActionButton` signatures (HIGH)
- `.planning/ROADMAP.md` — P10∥P11, P12-extends-P11-sheet, P13 gates + ship criteria (HIGH)
- `.planning/PROJECT.md` — INV-01 one-way dependency, prop-driven mandate, VOUT-04 dual-consumer confirm shape (HIGH)

---
*Architecture research for: AI-voice UI cohort integration into `yahirandroidtaste`*
*Researched: 2026-09-29*
