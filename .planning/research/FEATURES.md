# Feature Research

**Domain:** AI-voice command UI — generic, prop-driven presentational Compose composables for a reusable design-system hub (settings surfaces + outcome/confirmation surfaces)
**Researched:** 2026-09-29
**Confidence:** HIGH (established Material 3 / Compose UX patterns; consumer prop-shapes derived from the frozen §6.3 contract + SB `MutationGate`/`VoiceConfirmGate` and CT weak-match confirm)

> Scope note: this is a **presentational library slice**. "Table stakes vs differentiators vs anti-features" here means *component-behavior expectations* (what a well-built settings/outcome composable must do), not product-market features. The load-bearing output for the roadmap is the **prop-shape** and **states-matrix** implications per phase — those are called out inline and consolidated at the end.

---

## Phase 10 — Settings surfaces (VSET-01/02, VAPPR-01/02/03)

### 10a. Provider / API-key card (VSET-01)

#### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Provider selector | User must pick which provider the key belongs to | LOW | Small fixed set (OpenAI/Anthropic/local/…) → **segmented buttons** if ≤4, **exposed dropdown** if more. Prop: `providers: List<ProviderOption>`, `selectedProviderId`, `onProviderSelected`. Never hardcode provider names in the library. |
| Masked key entry, default hidden | Keys are secrets; shoulder-surf protection is assumed | LOW | Compose `TextField` with `visualTransformation = PasswordVisualTransformation()` by default. |
| Reveal (show/hide) toggle | Confirmed universal best practice — reduces entry errors without a confirm field | LOW | Trailing eye icon toggles `PasswordVisualTransformation()` ↔ `VisualTransformation.None`. Focus/caret must stay put. Accessible label ("Show key"/"Hide key"). |
| Validation state surface | User needs to know if the key is empty/invalid/accepted | MEDIUM | Prop-driven enum, e.g. `keyState: KeyFieldState { Empty, Entered, Validating, Valid, Invalid(reason) }`. Library **renders** the state; it never validates or calls the network itself (INV-01). Maps to `TextField` `isError` + `supportingText`. |
| "Key set / not set" affordance without echoing the secret | User returns to settings and needs to know a key exists without it being displayed | LOW | Render a masked placeholder ("••••  ••1234" — last-4 only, passed as a prop) rather than the raw stored value. The library holds nothing. |

#### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Inline "Validate / Test key" action slot | One tap to confirm the key works before leaving settings | LOW | An **action callback + a state prop** only — `onValidateKey`, plus `keyState = Validating/Valid/Invalid`. The *actual* test call lives in the consumer/engine. Library shows a spinner + result chip. |
| Paste-affinity + trim | Keys are always pasted; trailing whitespace is the #1 "invalid key" cause | LOW | Emit trimmed text on change (a pure formatting concern, allowed in a presentational layer). |
| "Get a key" helper link slot | Reduces the "where do I get this" dead-end | LOW | Optional `helpLinkSlot: (@Composable () -> Unit)?` — consumer supplies the URL/text; library reserves the slot. |

#### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Persist the key in the library | "Just remember it for me" | Breaks INV-01 (library holds no secrets, no storage); makes the component non-reusable and a supply-chain liability | Consumer owns storage (EncryptedSharedPrefs/DataStore/Keystore); library is stateless, key arrives via prop, changes emit via callback |
| Library-side key validation / network probe | "Tell me if it's valid" | Requires OkHttp + provider knowledge → violates INV-01 and one-way-dependency | Expose `keyState` prop + `onValidateKey` callback; consumer/engine does the probe and pushes state back |
| Baking real provider names/logos into the component | "Show the OpenAI logo" | Domain coupling; every new provider = a library change | Provider is a `ProviderOption(id, label, iconSlot?)` prop list; consumer supplies labels/icons |

**States matrix (4-cell) implication:** Empty (no key) · Filled-hidden (key entered, masked) · Filled-revealed · Error/Invalid. Validating (spinner) is a 5th state worth a preview.

---

### 10b. Model card (VSET-02)

#### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Show available models, mark selected | Core purpose of the card | LOW | Prop: `models: List<ModelOption>`, `selectedModelId`, `onModelSelected`. |
| Right control for the set size | Legibility | LOW | **Exposed dropdown** is the correct default — model lists are open-ended and often long (confirmed Material 3 guidance: dropdown for many/unknown-count options, segmented only for a small all-visible set). |
| Empty / unavailable state | Models can't be listed until a provider+key are set | LOW | `models` empty → render a disabled state with a reason string prop ("Set a provider and key first"), not a blank control. Ties to the dependency below. |

#### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Per-model metadata slot (context window, cost, "recommended") | Helps the user choose without leaving the app | LOW | `ModelOption` carries optional `subtitle`/`badge` fields; library renders them if present. Keep generic (strings), no provider-specific schema. |
| Loading state for async model fetch | Model lists are fetched after a key is entered | LOW | `modelsState: { Idle, Loading, Loaded, Error }` prop — again render-only. |

#### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Library fetches the model list | "Populate it automatically" | Network in the library → INV-01 violation | Consumer fetches; passes `models` + `modelsState` as props |
| Hardcoded model catalog | "Ship a default list of GPT-4/Claude/…" | Stale the day it ships; domain coupling | All models arrive via props |

**States matrix implication:** Empty/disabled (no provider) · Loading · Loaded-with-selection · Error.

---

### 10c. Command-approach card (VAPPR-01/02/03) — the novel surface

This is the least-standard component (no off-the-shelf Material pattern), so it needs the most deliberate design. It shows an **ordered escalation ladder** plus two policy controls. Keep it strictly **display + emit** — the library never *executes* or *enforces* the ladder (that's the engine's `TierPolicy`, explicitly out of scope).

#### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Ordered, legible tier ladder | The whole point of VAPPR-01 — the escalation order must read top-to-bottom / left-to-right unambiguously | MEDIUM | Render as a **vertical ordered list of labeled steps** with rank affordance (1→2→3→4, or connector arrows). Prop: `tiers: List<TierRung>` where `TierRung(id, label, description?, enabled)`. Order is the list order — don't rely on a separate sort key. Vertical > horizontal: labels like "Grammar / SingleShot / Plan / Agentic" are long and the ladder can grow. |
| Offline-only toggle (VAPPR-02) | Privacy/cost control users expect | LOW | Standard `Switch` + label + supporting text. Props: `offlineOnly: Boolean`, `onOfflineOnlyChange`. When on, it should *visibly* dim/annotate the network-requiring rungs (see differentiator) — but the library only reflects state, doesn't decide which rungs are offline-capable (that's a per-rung prop). |
| Max-tier cap control (VAPPR-03) | Cost/safety ceiling — "never escalate past Plan" | MEDIUM | The interesting one. Cap = "highest rung the system may escalate to." Best rendered **as part of the same ladder** (a selectable cap marker) rather than a separate detached dropdown, so cap is legible *against* the ladder it caps. Props: `maxTierId`, `onMaxTierChange`. Rungs above the cap render visibly capped (greyed + a "capped" affordance). |
| Reflect derived/effective state | User must see the *combined* effect of offline-only + cap | MEDIUM | A rung's effective availability = `enabled && (!offlineOnly || rung.offlineCapable) && rung.rank <= cap`. Compute-and-pass **or** pass the primitives and let the composable derive presentation. Recommend: pass primitives per rung (`offlineCapable: Boolean`), derive the visual in the composable — keeps the consumer's job to "describe the rungs," not "pre-render the styling." |

#### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Rungs above the cap shown greyed-but-present (not hidden) | Preserves the mental model of the full ladder while showing the ceiling — matches the hub's **conditional-render-no-dead-space** convention's spirit (show structure, not emptiness) | LOW | A capped rung stays visible with a "capped" tag; it's not removed. This makes the cap legible *as a ceiling*. |
| Per-rung offline-capability annotation | When offline-only is on, the user sees exactly which rungs drop out | LOW | `TierRung.offlineCapable`; disabled rungs get an "needs network" affordance under offline-only. |
| Tap-a-rung-to-set-cap interaction | Fewer controls; cap is set *on the ladder itself* | MEDIUM | Ladder rung acts as the cap selector — one interaction model instead of ladder + separate dropdown. Emits `onMaxTierChange(rungId)`. |
| Ladder tag colors (reuse existing `TAGCOLOR` slot) | Visual differentiation of tiers reuses shipped per-tag color infra rather than inventing new theming | LOW | Optional per-rung color slot, consistent with v2.3.0's opt-in tag color. |

#### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Enforcing/executing the ladder (skip disabled tiers, actually escalate) | "Make the cap actually do something" | That's `TierPolicy` in the engine — explicitly out of scope; would drag engine logic into the UI | Library displays + emits; engine enforces |
| Baking in the tier names (Grammar/SingleShot/Plan/Agentic) as an enum | "They're always these four" | Domain coupling; CT and SB may name/number tiers differently; future tiers = library change | `tiers` is a prop list of `TierRung`; names/order/count all arrive from the consumer |
| A free-form drag-to-reorder ladder editor | "Let users reorder tiers" | The ladder order is an engine policy artifact, not a user preference; reordering implies the UI owns policy | Order is display-only (from props); the only user-editable policy here is offline-only + cap |
| Encoding "offline-capable" as a hardcoded rule per known tier name | "Grammar is always offline" | Couples to specific tier identities | `offlineCapable` is a per-rung prop the consumer sets |

**States matrix implication:** All-enabled/no-cap · Cap-set (rungs above greyed) · Offline-only-on (network rungs annotated) · Offline-only + cap combined (the derived/worst-case cell). This card needs its combined-constraint cell as the "interesting" 4th state.

---

## Phase 11 — Outcome / failure sheet (VOUT-01/02/03)

Domain-neutral bottom sheet (hub already has a bottom-sheet family) that renders the result of a voice command.

#### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Success vs failure outcome, from props | Core purpose | LOW | `outcome: Outcome { Success, Failure(...), NeedsConfirmation(...) }` (sealed shape; NeedsConfirmation lands in Phase 12). No app nouns — title/detail are strings passed in. |
| "Handled by: tier/approach" provenance indicator (VOUT-02) | Trust/debuggability — user (and dev) sees which rung answered | LOW | Prop: `handledBy: HandledByInfo(tierLabel, approachLabel?)`. Render as a labeled chip/row ("Handled by · SingleShot"). Reuse chip family. Must be optional/nullable (some outcomes have no attributable tier). |
| **Loud** failure state (VOUT-03) | Explicit requirement + matches the user's stated "make failures loud and visible in the UI" philosophy | MEDIUM | Failure ≠ subtle grey text. Use error container color, error icon, prominent title, and the failure reason string. Contrast this hard against success. This is the load-bearing UX call of the phase. |
| Failure reason + optional retry action | A loud failure the user can't act on is a dead-end | LOW | `failureReason: String`, optional `onRetry`/`onDismiss` callbacks. Action slot, not baked logic. |

#### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Provenance detail expander | Power users/devs want more than a tier label (e.g. which fallbacks were tried) | LOW | Optional `handledBy.trace: List<String>` rendered in a collapsible — conditional-render-no-dead-space (absent → no expander). |
| Distinct visual per outcome class beyond color | Accessibility (don't rely on color alone for success/failure) | LOW | Icon + shape + text, not just red/green. Ties to the hub's accessibility posture. |
| Copy-error affordance | Users report failures; one-tap copy of the reason helps | LOW | Action slot; consumer wires clipboard. |

#### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Silent/auto-dismissing failure toast | "Don't nag the user" | Directly violates VOUT-03 (loud, visible) and the user's regression sensitivity to swallowed failures | Failures are sticky/loud in the sheet until dismissed or retried |
| Parsing/inferring the failure category in the library | "Show a nicer message for rate-limit errors" | Requires engine/provider knowledge → domain coupling | Consumer maps engine error → a `failureReason` string + optional category enum passed as a prop |
| App-specific success nouns ("Added to your Second Brain") | Reads nicer | Kills reuse across SB/CT | Title/detail are consumer-supplied strings |

**States matrix implication:** Success · Failure (loud) · Success-with-provenance · Failure-with-retry. (NeedsConfirmation is Phase 12's cells.)

---

## Phase 12 — Generic needs-confirmation state (VOUT-04) — the single-OR-batch shape

This is the phase with the sharpest prop-shape risk, so it gets a concrete treatment. It **extends the Phase 11 sheet** with a `NeedsConfirmation` outcome. It must render, from **one** composable and **one** prop shape, *both*:

- **SB `MutationGate`/`VoiceConfirmGate` risk confirm** — a *single* proposed action the user must approve because it's destructive/risky ("Delete 3 notes?").
- **CT weak-match confirm** — a *weak-match* case that can be **either** a single proposed item ("Did you mean *Running*?") **or** a batch ("These 5 entries matched loosely — confirm each / all").

#### The concrete shape (addresses the single-vs-batch gate)

The trap is modelling "single" and "batch" as two different types. **Model single as a batch of one** so there is exactly one code path:

```
NeedsConfirmation(
    reason: String,                     // "This will delete data" / "Weak match — please confirm"
    items: List<ProposedItem>,          // size 1 = single confirm; size >1 = batch confirm
    confirmLabel: String = "Confirm",
    cancelLabel: String = "Cancel",
    onConfirm: (List<ProposedItemId>) -> Unit,   // ids the user affirmed
    onCancel: () -> Unit,
    // batch-only affordances, degrade cleanly when items.size == 1:
    selectionMode: SelectionMode = AllOrNothing  // AllOrNothing | PerItem
)

ProposedItem(
    id, title: String, subtitle: String? = null, detailSlot: (@Composable () -> Unit)? = null
)
```

Rendering rule (drives the states matrix):
- `items.size == 1` → render a **single-item confirm** (no checkboxes, no "select all"); confirm returns that one id. Covers SB risk confirm and CT single weak-match.
- `items.size > 1` + `AllOrNothing` → render the list read-only with one Confirm/Cancel pair; confirm returns all ids. Covers a batch the user accepts wholesale.
- `items.size > 1` + `PerItem` → render each item with an include/exclude affordance; confirm returns the affirmed subset. Covers CT "confirm each of these loose matches."

This one shape covers all four consumer cases with **no per-consumer library change** (satisfies VOUT-04's success criterion 4).

#### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Reason string always shown | User must know *why* confirmation is needed | LOW | Prominent, above the items. |
| Single-item confirm | SB risk confirm + CT single weak-match | LOW | `items.size == 1` path — no batch chrome. |
| Batch confirm (all-or-nothing) | Bulk approve loose matches | MEDIUM | Read-only list + one action pair. |
| Confirm / Cancel, both emit | Destructive-safe: cancel is a first-class, easily-reachable action | LOW | Cancel must be as reachable as confirm (no dark-pattern burying) — matches reveal-confirm destructive convention already in the hub. |
| Domain-neutral throughout | Reuse across SB/CT | LOW | Every string is a prop; items carry only generic title/subtitle/slot. |

#### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Per-item include/exclude (`PerItem` mode) | CT's "confirm each loose match" without a bespoke screen | MEDIUM | Checkbox/switch per row; confirm returns the affirmed subset. This is what makes the batch case genuinely useful, not just a bulk yes/no. |
| Risk emphasis for destructive confirms | SB `MutationGate` is destructive — it should *look* riskier than a benign weak-match | LOW | Optional `severity: Confirm { Normal, Destructive }` prop → destructive uses error-tinted confirm button + warning affordance. Keeps one composable, two visual weights. |
| Item detail slot | Some items need richer preview than title/subtitle | LOW | `detailSlot` composable; conditional-render-no-dead-space when absent. |

#### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Two separate composables (SingleConfirm + BatchConfirm) | "They look different" | Doubles the surface, drifts, and fails VOUT-04's "one prop shape covers both" | One composable, `items: List` (single = list of one) + `selectionMode` |
| Executing the confirmed mutation in the library | "Just do the delete on confirm" | Domain/engine action in the UI layer → INV-01 violation | `onConfirm(ids)` callback; consumer performs the mutation |
| Auto-selecting/auto-confirming a "best" match | "Save a tap on strong matches" | The whole point is *weak* match / *risky* action — auto-confirm defeats the gate; also encodes matching logic | Library only presents; the consumer decides what reaches the confirm sheet |
| Making cancel harder than confirm (or defaulting to confirm) | "Encourage the happy path" | Dark pattern; dangerous for destructive SB confirms | Symmetric, clearly-labeled Confirm/Cancel; destructive variant if anything biases toward caution |

**States matrix implication (this composable's 4 cells):** Single confirm · Batch all-or-nothing · Batch per-item (partial selection) · Destructive single confirm (risk emphasis). Consider a 5th preview for empty-subset (per-item with nothing selected → confirm disabled).

---

## Phase 13 — Catalog & ship (CAT-01, API-01, INV-01, SHIP-01/02)

Not a feature-research surface — it's the integrity gate. The feature-research implication is only: **every composable above must land in `ComponentRegistry` with a full 4-cell states matrix** (the states called out per phase feed directly into CAT-01), the API stays strictly additive vs `v2.3.0` (favor nullable/defaulted new params and new symbols, never change existing signatures), and no OkHttp/engine dep enters (INV-01). The prop shapes above are deliberately all data-in / callbacks-out to keep INV-01 trivially true.

---

## Feature Dependencies

```
Provider/key card (VSET-01)
    └──gates──> Model card (VSET-02)          [models can't populate until provider+key set]
                    └──feeds──> (consumer engine, not library)

Command-approach card (VAPPR-01)
    ├──contains──> offline-only toggle (VAPPR-02)
    └──contains──> max-tier cap (VAPPR-03)     [cap is rendered ON the ladder, not detached]
        offline-only ⊗ cap ──derive──> effective per-rung availability  [the "interesting" state cell]

Outcome sheet (VOUT-01/02/03)
    └──extended-by──> NeedsConfirmation state (VOUT-04)   [Phase 12 adds a sealed variant]

All composables ──must-register──> ComponentRegistry + 4-cell states matrix (CAT-01)
```

### Dependency Notes

- **Model card depends on provider/key (presentationally):** the model card's empty/disabled state is driven by whether a provider+key exist — but this is a *prop* relationship (consumer passes empty `models` + a reason), not a library-internal coupling. Roadmap already keeps both in Phase 10.
- **Cap is coupled to the ladder, not standalone:** VAPPR-03 reads best rendered *inside* the VAPPR-01 ladder (cap marker on the rungs), so plan them as one composable with three prop groups, not three separate widgets.
- **VOUT-04 extends VOUT-01's sheet:** Phase 12 gating on Phase 11 (per the roadmap) is correct — `NeedsConfirmation` is a variant of the same `Outcome` sealed type, so the sheet's shell must exist first.
- **offline-only ⊗ cap is the derived state:** don't ask the consumer to pre-compute styling; pass per-rung primitives (`enabled`, `offlineCapable`, rank) + the two policy values and derive the visual in the composable.

---

## MVP Definition

Everything in scope is required by the frozen §6.3 contract, so "MVP" here = the required prop surface per phase; "add after" = optional differentiator slots that don't change the core prop shape.

### Launch With (v2.4.0 — required)

- [ ] Provider/key card: provider selector + masked key entry + reveal toggle + `keyState` render + set/not-set masked affordance — VSET-01
- [ ] Model card: `models` list + selection + empty/loading/error states — VSET-02
- [ ] Command-approach card: ordered ladder + offline-only toggle + max-tier cap, all prop-driven, display-only — VAPPR-01/02/03
- [ ] Outcome sheet: success/failure from props + "handled by" indicator + **loud** failure — VOUT-01/02/03
- [ ] Needs-confirmation state: one composable, `items: List` (single = list-of-one) + AllOrNothing/PerItem, reason string, symmetric confirm/cancel — VOUT-04
- [ ] All registered in `ComponentRegistry` with 4-cell states matrices — CAT-01

### Add After Validation (consumer Wave-1, not this milestone)

- [ ] `onValidateKey` "test key" round-trip — trigger: a consumer wants in-settings validation (slot already reserved)
- [ ] Provenance `trace` expander on the outcome sheet — trigger: debugging demand from a consumer
- [ ] Per-rung tag colors on the ladder — trigger: a consumer opts into TAGCOLOR for tiers

### Future Consideration (deferred)

- [ ] Cost/usage surfacing in the model card — defer: needs a usage data contract not yet defined
- [ ] Multi-key / key-rotation UI — defer: no consumer needs it; would tempt library-side storage

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Masked key entry + reveal toggle | HIGH | LOW | P1 |
| Provider selector (segmented/dropdown by count) | HIGH | LOW | P1 |
| `keyState` validation render (no library validation) | MEDIUM | LOW | P1 |
| Model card list + selection + empty state | HIGH | LOW | P1 |
| Ordered tier ladder (display-only) | HIGH | MEDIUM | P1 |
| Offline-only toggle | MEDIUM | LOW | P1 |
| Max-tier cap on the ladder | HIGH | MEDIUM | P1 |
| Loud failure state | HIGH | MEDIUM | P1 |
| "Handled by" provenance indicator | MEDIUM | LOW | P1 |
| Single-or-batch confirm (one shape, `List`) | HIGH | MEDIUM | P1 |
| Per-item include/exclude (`PerItem`) | MEDIUM | MEDIUM | P1 (needed for CT) |
| Destructive severity emphasis | MEDIUM | LOW | P2 |
| `onValidateKey` test round-trip slot | MEDIUM | LOW | P2 |
| Provenance trace expander | LOW | LOW | P3 |

## Competitor / prior-art feature analysis

| Feature | Prior art A | Prior art B | Our approach |
|---------|-------------|-------------|--------------|
| API-key entry | OpenAI/Anthropic settings (masked, reveal, last-4 shown) | 1Password/secret managers (masked default + reveal) | Masked default + reveal toggle + set/not-set via last-4 prop; library stores nothing |
| Provider/model select | ChatGPT/Claude model pickers (dropdown for models) | VS Code Copilot model dropdown | Segmented for ≤4 providers, exposed dropdown for models (Material 3 count rule) |
| Escalation/tier display | CI/CD stage pipelines (ordered rungs) | Feature-flag "environment ladder" UIs | Vertical ordered ladder, cap marker rendered on the ladder, capped rungs greyed-but-present |
| Outcome + provenance | LLM tool-call "handled by tool X" chips; "generated with model Y" footers | Observability "which route handled this" badges | Nullable `handledBy` chip; loud error-container failure state |
| Single-vs-batch confirm | Gmail bulk-action confirm; file-manager "apply to all" | IDE refactor-preview (per-item checkboxes) | One composable: `items: List` (single = list-of-one) + AllOrNothing/PerItem selection mode |

## Consolidated states-matrix implications (feeds CAT-01)

| Composable | Cell 1 | Cell 2 | Cell 3 | Cell 4 (the "interesting" one) |
|-----------|--------|--------|--------|-------------------------------|
| Provider/key card | Empty | Filled-hidden | Filled-revealed | Error/Invalid (+ Validating preview) |
| Model card | Empty/disabled (no provider) | Loading | Loaded-with-selection | Error |
| Command-approach card | All-enabled/no-cap | Cap-set (rungs above greyed) | Offline-only on (network rungs annotated) | Offline-only + cap combined (derived) |
| Outcome sheet | Success | Failure (loud) | Success-with-provenance | Failure-with-retry |
| Needs-confirmation | Single confirm | Batch all-or-nothing | Batch per-item (partial) | Destructive single (risk emphasis) |

## Consolidated anti-feature list (the reusability litmus for this milestone)

1. **No secret persistence** in the library (keys are props in, changes out).
2. **No network / OkHttp / engine dependency** — no key validation, no model fetch, no tier execution in the library (INV-01).
3. **No domain nouns** — provider names, model names, tier names, success/failure copy all arrive as props; nothing hardcoded.
4. **No two-composable single/batch split** — one confirm composable, `items: List` where single = list-of-one.
5. **No silent failures** — VOUT-03 failures are loud, sticky, and visible.
6. **No dark-pattern confirm** — cancel is as reachable as confirm; destructive variant biases toward caution.
7. **No ladder execution/reordering** — the approach card displays + emits policy (offline-only, cap) only; it never enforces or reorders the ladder.

## Sources

- [Segmented button | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/components/segmented-button) — segmented for small all-visible sets; single-choice semantics
- [Choices of Choices: User Selection Components in Compose Material 3 (Medium)](https://medium.com/@kerry.bisset/choices-of-choices-exploring-user-selection-components-in-jetpack-compose-with-material-3-e7fd9b1418c6) — segmented vs dropdown by option count
- [SingleChoiceSegmentedButtonRow – Material 3 Compose (composables.com)](https://composables.com/docs/androidx.compose.material3/material3/components/SingleChoiceSegmentedButtonRow) — single-choice usage
- [Password Pattern | UX Patterns for Developers](https://uxpatterns.dev/patterns/forms/password) — masked default + reveal toggle, single field
- [Creating an Accessible Password Field for WCAG Compliance | ADA Compliance Pros](https://www.adacompliancepros.com/blog/accessible-password-field) — accessible reveal toggle, focus retention
- Frozen contract §6.3 + SB `MutationGate`/`VoiceConfirmGate` and CT weak-match confirm (project `.planning/` — REQUIREMENTS.md, ROADMAP.md, PROJECT.md)

---
*Feature research for: AI-voice command UI — prop-driven presentational Compose composables*
*Researched: 2026-09-29*
