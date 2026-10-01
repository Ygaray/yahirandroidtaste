# Phase 10: Voice command settings surfaces - Research

**Researched:** 2026-09-29
**Domain:** Reusable Jetpack Compose (Material 3) presentational settings composables + design-system drift-guard/registry integration
**Confidence:** HIGH (grounded in the live source: `ClearableTextField.kt`, `SegmentedOptionSelector.kt`, `ComponentRegistry.kt`, `DomainVocabularyDriftGuardTest.kt`, `ExplorerIndexScreen.kt`, `api.txt`, `build.gradle.kts`; all package/coordinate claims verified against the pinned BOM in `.planning/research/STACK.md`)

## Summary

Phase 10 adds three **prop-driven, presentational** composables to a flat `component/` package: a provider/API-key card, a model card, and a single command-approach card (tier-ladder display + offline-only toggle + max-tier cap). Everything is data-in / callbacks-out — the library holds **no key**, makes **no network call**, and names **no consumer concept**. Every required Material 3 / Compose API (`ExposedDropdownMenuBox`, `PasswordVisualTransformation`) already ships in the pinned Compose BOM `2026.04.01`; there is **zero new dependency** `[VERIFIED: .planning/research/STACK.md:12-13,28-31; grep of src/main confirms neither symbol is used in-tree today]`.

The load-bearing integration work is **not** the UI — it is passing two build-gating drift guards. Every new public `@Composable` outside `explorer/` must (a) be **registered** in `ComponentRegistry.entries` XOR allowlisted, and (b) have its **leading PascalCase token** present in `PRIMITIVE_NOUN_ALLOWLIST` or `DOMAIN_VOCABULARY`. Per the R1-APPROVED naming decision (CONTEXT D-01), the composables use structural names leading with `Provider` / `Model` / `Approach`, and those three tokens get added to `PRIMITIVE_NOUN_ALLOWLIST` (NOT `DOMAIN_VOCABULARY`). Registration lands in a **new tenth "Voice Command" family** (`voiceCommandFamilyEntries`), each `Entry` with `tier = PATTERN` and a full 4-cell states matrix.

**Primary recommendation:** Add masking to `ClearableTextField` as two appended defaulted params (Metalava-additive), build the three cards in flat `component/`, back the offline-only toggle with the existing `SegmentedOptionSelector` (2-option) or a `Switch`, use `ExposedDropdownMenuBox` for provider/model selection, render the cap as tap-a-rung on the ladder behind a swappable seam, define all prop models as all-`val` immutables in `model/`, add `Provider`/`Model`/`Approach` to `PRIMITIVE_NOUN_ALLOWLIST`, and register the cohort in a new Voice Command family. Verify with `./gradlew testDebugUnitTest detekt apiCheck`.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01 [naming] — APPROVED at R1:** Structural composable names whose leading token is generic (e.g. `ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`); widen `PRIMITIVE_NOUN_ALLOWLIST` with the new generic leading tokens (Provider/Model/Approach/Outcome/Command/Undo) — do **NOT** grandfather full names into `DOMAIN_VOCABULARY`. Reversibility costly (renaming public composables/allowlist after v2.4.0 tag is a breaking change for both consumers).
- **D-02 [key-entry]:** Render masked API-key entry by adding an **additive** `visualTransformation` + reveal-toggle parameter to `ClearableTextField` (it currently exposes none); fall back to a purpose-built masked key field only if the additive param muddies that primitive. `PasswordVisualTransformation` is in the pinned Compose BOM (no new dependency).
- **D-03 [cap-control]:** Render the max-tier cap as **tap-a-rung** on the ladder (cap marker with rungs-above greyed-but-present, matching conditional-render-no-dead-space); `SingleChoiceSegmentedButtonRow` is the fallback for a short fixed ladder. `SegmentedOptionSelector` hard-requires exactly 2 options → can back the offline-only toggle but NOT a 3–4-tier cap. Default to tap-a-rung and **build it so the cap control is easy to swap** — Yahir reviews it in the gallery at Gate-1.
- **D-04 [prop-models]:** All settings prop-models are all-`val` immutable models in `model/` with frozen constructors. One-way — model shapes freeze into the v2.4.0 public API; a `var` reproduces the `TagChipUiModel` Compose-`STABLE`/`copy()` regression.
- **D-05 [control-visibility]:** Every settings/approach control is optional and hideable by props — a null/absent prop means "not shown", never shown-disabled.
- **D-06 [offline-capable]:** `offlineCapable` per rung is an **APP-derived prop**; YAT never depends on VAE's `TierPolicy` type (L7 — no hub-to-hub edge). Props are self-contained and only need to be renderable.

### Claude's Discretion
- The exact cap-control widget (tap-a-rung vs segmented) may be finalized at plan/UI time against the expected tier count; both are dependency-free. **Build the cap control so it is easy to swap.**

### Deferred Ideas (OUT OF SCOPE)
- None — discussion stayed within phase scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VSET-01 | Provider/API-key settings card renders provider selection + API-key entry purely from props + callbacks (no key persistence, no network) | Provider dropdown via `ExposedDropdownMenuBox`; masked key via additive `ClearableTextField` masking (§Q1); no storage/OkHttp anywhere (INV litmus) |
| VSET-02 | Model settings card renders selected/available model(s) from props, emits selection via callback | `ExposedDropdownMenuBox` for the model list; empty/disabled state driven by an empty `models` prop + reason string (§Q2) |
| VAPPR-01 | Command-approach card displays the configured tier ladder (ordered approaches) from props | ONE composable, ladder = `List<rung model>` rendered in list order (§Q3) |
| VAPPR-02 | Offline-only toggle reflects + emits offline-only state via props + callback | `SegmentedOptionSelector` (2-option) or `Switch`; per-rung `offlineCapable` annotation derived in-composable (§Q3, §Q4) |
| VAPPR-03 | Max-tier cap control reflects + emits the cap via props + callback | Tap-a-rung cap marker behind a swappable seam; `SingleChoiceSegmentedButtonRow` fallback (§Q3) |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- **One-way dependency:** library imports no consumer code, holds no secrets, makes no domain assumptions; allowed deps only (Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable, osmdroid). No OkHttp/engine/`TierPolicy`.
- **Bindings-only Hilt:** no `@HiltAndroidApp`, no `@AndroidEntryPoint`. (These P10 composables are stateless presentational — no Hilt binding needed at all.)
- **`ComponentRegistry` is single source of truth + drift guard:** every public top-level `@Composable` in the visual packages must be registered XOR allowlisted in `INTENTIONALLY_UNREGISTERED`.
- **Interaction conventions travel with components:** reveal-confirm destructive swipe, standardized snackbar/undo, conditional-render-no-dead-space.
- **Detekt zero-baseline policy:** keep detekt green at zero baseline; do NOT regenerate a baseline to bury a finding — fix it or tune the rule with justification.
- **Additive-only public API:** appending a defaulted param is source-compatible for named/positional/parenthesized calls (Metalava `apiCheck` passes) but NOT for a trailing-lambda call of the previously-last callback, and NOT binary-compatible in general — ships as a minor bump; consumers rebuild from the immutable tag `[CITED: API.md:191-214]`.
- **Toolchain:** AGP 9.2.1 / Kotlin 2.3.20 / Compose BOM 2026.04.01 / JDK 17; single-module hub — Gradle commands drop the module prefix.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Provider selection UI | Client / Compose UI (library) | — | Pure presentation; provider list + selection arrive as props, choice emitted via callback |
| API-key entry (masked) | Client / Compose UI (library) | Consumer storage | Library renders + emits; key persistence (EncryptedSharedPrefs/DataStore/Keystore) is the consumer's job (INV-01) |
| Key validation / network probe | Consumer / engine | — | Library exposes `keyState` prop + `onValidateKey` callback only; never calls the network |
| Model list fetch | Consumer / engine | — | Library renders `models` prop + `modelsState`; consumer fetches |
| Tier ladder policy (order, enforcement) | Consumer / engine (`TierPolicy`) | — | Library **displays + emits** only; never enforces/reorders the ladder (D-06, VAPPR anti-feature) |
| Offline-only / max-tier cap state | Client / Compose UI (library) | Consumer / engine | Library reflects + emits via callbacks; engine applies the policy |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `androidx.compose.material3:material3` | via BOM `2026.04.01` | `ExposedDropdownMenuBox`/`ExposedDropdownMenu`, `SegmentedButton`, `SingleChoiceSegmentedButtonRow`, `Switch`, `OutlinedTextField`, `Card` | The design system's primitive layer; every voice-settings control is a stock M3 component `[CITED: .planning/research/STACK.md:29]` |
| `androidx.compose.ui:ui` (+ `ui-text`) | via BOM `2026.04.01` | `PasswordVisualTransformation`, `VisualTransformation` (in `androidx.compose.ui.text.input`) | Masking the key field needs nothing beyond `androidx.compose.ui` — already declared `[CITED: .planning/research/STACK.md:30]` |
| `androidx.compose.material:material-icons-extended` | via BOM `2026.04.01` | Reveal eye glyph, offline/network glyph, cap/warn glyphs | Already declared; no new dep `[CITED: .planning/research/STACK.md:31]` |

### Supporting (in-repo, reuse)
| Component | File | Purpose | When to Use |
|-----------|------|---------|-------------|
| `ClearableTextField` | `component/ClearableTextField.kt` | `OutlinedTextField` wrapper with clear-✕ | Extend additively for masked key entry (§Q1) |
| `SegmentedOptionSelector` | `component/SegmentedOptionSelector.kt` | 2-option segmented toggle with disabled+reason affordance | Back the offline-only toggle (exactly 2 options) — NOT the cap `[VERIFIED: component/SegmentedOptionSelector.kt:49 `require(options.size == 2)`]` |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `ExposedDropdownMenuBox` (provider/model) | `SingleChoiceSegmentedButtonRow` | Few (≤3) options → segmented reads better; many/unknown-count → dropdown (Material 3 count rule). Both stock M3, no dep either way `[CITED: .planning/research/STACK.md:100]` |
| Tap-a-rung cap marker | `SingleChoiceSegmentedButtonRow` (discrete tiers) | Segmented is the fallback for a short fixed ladder; tap-a-rung is the default (D-03). Build behind a swap seam. `Slider` steps available but rejected for a small discrete ladder `[CITED: .planning/research/STACK.md:60]` |
| `SegmentedOptionSelector` for offline-only | `Switch` + label + supporting text | Both dependency-free; `SegmentedOptionSelector` already ships the disabled+reason affordance and matches D-05's "hideable, never shown-disabled" spirit |

**Installation:** None. All APIs resolve from the pinned Compose BOM `2026.04.01`; there is nothing to install `[VERIFIED: .planning/research/STACK.md:67; grep of src/main shows PasswordVisualTransformation/ExposedDropdownMenuBox are first in-tree use]`.

## Package Legitimacy Audit

> **Not applicable — this phase installs NO external packages.** Every API used (`ExposedDropdownMenuBox`, `PasswordVisualTransformation`, `SegmentedButton`, `Switch`) resolves from the already-declared, pinned Compose BOM `2026.04.01`. No new coordinate enters `build.gradle.kts` or `libs.versions.toml`. INV-01 (no OkHttp/engine dep) stays trivially true.

## Architecture Patterns

### System Data-Flow Diagram

```
Consumer (SecondBrain / CalTracker)
  owns: key storage, provider/model catalog, engine TierPolicy, network probes
        │  passes props (data)                    ▲  emits callbacks (events)
        ▼                                         │
┌─────────────────────────────────────────────────────────────────┐
│  yahirandroidtaste  component/  (flat, @Composable, stateless)   │
│                                                                   │
│  ProviderKeyCard ──renders──> ExposedDropdownMenuBox (provider)   │
│     selectedProviderId, providers ──▶                             │
│     keyValue, keyMasked, onKeyChange, onToggleReveal ──▶          │
│                       └─ ClearableTextField(+visualTransformation)│
│                                                                   │
│  ModelSelectCard ──renders──> ExposedDropdownMenuBox (models)     │
│     models(empty→disabled+reason), selectedModelId, onModelSel ──▶│
│                                                                   │
│  ApproachLadderCard  (ONE composable, three prop groups)          │
│     ladder: List<rung>  ──▶ vertical ordered rungs                │
│     offlineOnly + onOfflineOnlyChange ──▶ SegmentedOptionSelector │
│     maxTierId + onMaxTierChange ──▶ tap-a-rung cap marker         │
│     derive per-rung effective state IN-composable:                │
│       enabled && (!offlineOnly || rung.offlineCapable)            │
│                    && rung.rank <= cap                            │
└─────────────────────────────────────────────────────────────────┘
        │
        ▼  every public @Composable must pass BOTH build gates:
   ComponentRegistry.entries (registered XOR allowlisted)   [CATALOG-03]
   DomainVocabularyDriftGuardTest (head token allowlisted)  [GOV-02]
```

Model-to-file mapping is in the Component Responsibilities table below, not the diagram.

### Recommended Project Structure (flat `component/`, one new registry family)
```
component/                              # FLAT — every component lives here (no sub-packages)
├── ClearableTextField.kt              # EDIT — append masking params (§Q1)
├── ProviderKeyCard.kt                 # NEW — VSET-01
├── ModelSelectCard.kt                 # NEW — VSET-02
└── ApproachLadderCard.kt              # NEW — VAPPR-01/02/03 (ONE composable)
model/                                  # immutable, all-val, domain-free UI models
├── ProviderOptionUiModel.kt           # NEW
├── ModelOptionUiModel.kt              # NEW
└── ApproachRungUiModel.kt             # NEW (+ cap/state enums as needed)
explorer/
├── VoiceCommandFamilyScreen.kt        # NEW — voiceCommandFamilyEntries + fake fixtures + screen
├── ComponentRegistry.kt              # EDIT — concat voiceCommandFamilyEntries
└── ExplorerIndexScreen.kt            # EDIT — ExplorerFamilies const + ORDERED_KEYS row
```
`[CITED: .planning/research/ARCHITECTURE.md:9,56-80]` — flat `component/` (all ~60 existing components are flat; families are a *registry taxonomy*, not a package layout).

### Component Responsibilities
| Element | Responsibility | Shape |
|---------|----------------|-------|
| `ProviderKeyCard` (`component/`) | Render provider dropdown + masked key field + `keyState`; emit selection/edit/reveal | Presentational `@Composable`, hoisted state |
| `ModelSelectCard` (`component/`) | Render model list/selection or empty+reason; emit selection | Presentational `@Composable` |
| `ApproachLadderCard` (`component/`) | Render ordered ladder + offline-only toggle + tap-a-rung cap; derive per-rung effective visual | ONE `@Composable`, three prop groups |
| `*UiModel` (`model/`) | Carry domain-neutral shapes the consumer maps engine output into | all-`val` `data class` — Compose-`STABLE`-inferable |
| `voiceCommandFamilyEntries` (`explorer/VoiceCommandFamilyScreen.kt`) | Register the cohort + drive gallery detail pages | slice of `ComponentRegistry.entries`, `tier=PATTERN` |
| `ExplorerFamilies` (edit) | Declare + order the new family | new const `VOICE_COMMAND` + `ORDERED_KEYS` row |

---

### Q1 — Masked API-key entry: additive change to `ClearableTextField`

**Current public signature** (10 params, all trailing ones defaulted; `keyboardActions` is the last) `[VERIFIED: component/ClearableTextField.kt:38-50]`:

```kotlin
@Composable
fun ClearableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
)
```

The committed Metalava signature confirms the exact param order and that all after `onValueChange` are `optional` `[VERIFIED: api.txt:104-105]`.

**Proposed additive signature** — append two defaulted params **after** `keyboardActions` (new LAST params). Appending defaulted params is source-compatible for named/positional/parenthesized calls and `apiCheck` passes `[CITED: API.md:191-214]`:

```kotlin
@Composable
fun ClearableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    // --- appended (v2.4.0, additive) ---
    visualTransformation: VisualTransformation = VisualTransformation.None,
    revealToggle: RevealToggle? = null   // null (default) → no eye affordance shown (D-05)
)
```

- `VisualTransformation.None` default preserves every existing caller's behavior byte-for-byte (all current callers pass no masking) — an unmasked field stays unmasked.
- The reveal toggle is modeled as a small nullable value type so it is **hideable** (D-05: null ⇒ "not shown"): e.g. `data class RevealToggle(val revealed: Boolean, val onToggle: () -> Unit)`. When non-null, the composable renders an eye `IconButton` in the trailing slot **beside** the existing clear-✕, with an accessible label ("Show key" / "Hide key").
- **Trailing-slot interaction:** `ClearableTextField` already occupies `trailingIcon` with the clear-✕ `[VERIFIED: component/ClearableTextField.kt:57-68]`. Adding a reveal eye means the trailing slot renders a `Row { revealButton?; clearButton }` — the plan must reconcile the two-icon trailing content (both stay visible; do not drop the clear affordance).

**Caller usage (in `ProviderKeyCard`), `PasswordVisualTransformation`:**
```kotlin
// import androidx.compose.ui.text.input.PasswordVisualTransformation
// import androidx.compose.ui.text.input.VisualTransformation
var revealed by rememberSaveable { mutableStateOf(false) }
ClearableTextField(
    value = keyValue,
    onValueChange = { onKeyChange(it.trim()) },       // paste-trim: pure formatting, allowed
    label = { Text(keyLabel) },
    isError = keyState is KeyFieldState.Invalid,
    supportingText = { /* reason from keyState */ },
    visualTransformation =
        if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
    revealToggle = RevealToggle(revealed = revealed, onToggle = { revealed = !revealed })
)
```
`PasswordVisualTransformation()` masks with `'•'` bullets by default and is a stable member of `androidx.compose.ui.text.input`, resolved by the already-declared `androidx.compose.ui:ui` under the pinned BOM `[CITED: .planning/research/STACK.md:30,56]`. First in-tree use — verified no existing reference `[VERIFIED: grep -rln "PasswordVisualTransformation" src/main → no matches]`.

**Fallback (D-02):** if the two appended params visibly muddy the primitive's KDoc/signature, build a purpose-built `MaskedKeyField` in `component/` instead — but the additive route is preferred and is the documented default.

### Q2 — Provider/model selection: `ExposedDropdownMenuBox` (first in-tree use) + where `SegmentedOptionSelector` fits

`ExposedDropdownMenuBox` is **not yet used anywhere in the tree** — this is its first use `[VERIFIED: grep -rln "ExposedDropdownMenuBox" src/main → no matches]`. It ships in the pinned Compose BOM `2026.04.01` (stable), no new dependency `[CITED: .planning/research/STACK.md:55,123]`.

Canonical Material 3 shape (both provider card and model card):
```kotlin
// import androidx.compose.material3.ExposedDropdownMenuBox
// import androidx.compose.material3.ExposedDropdownMenuDefaults
// import androidx.compose.material3.MenuAnchorType
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionDropdown(
    options: List<T>,
    selectedId: String?,
    label: (T) -> String,
    idOf: (T) -> String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { idOf(it) == selectedId }?.let(label) ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel, onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(label(opt)) },
                    onClick = { onSelected(idOf(opt)); expanded = false },
                )
            }
        }
    }
}
```
- **Model card (VSET-02):** dropdown is the correct default — model lists are open-ended/long `[CITED: .planning/research/FEATURES.md:52]`. Empty `models` ⇒ render a **disabled** state with a reason-string prop ("Set a provider and key first"), never a blank control `[CITED: .planning/research/FEATURES.md:53]`.
- **Provider card (VSET-01):** dropdown when many providers; `SingleChoiceSegmentedButtonRow` reads better for ≤3 (Claude's discretion at UI time by expected count).

**Where `SegmentedOptionSelector` fits:** the **offline-only toggle only** (exactly 2 states). It hard-`require`s exactly 2 options and throws otherwise `[VERIFIED: component/SegmentedOptionSelector.kt:49-51]`, so it **cannot** back a 3–4-tier cap. It already ships an always-visible disabled+reason affordance that matches D-05's "hideable, never shown-disabled" posture `[VERIFIED: component/SegmentedOptionSelector.kt:38-46,79-87]`.

⚠ **Note (`ExperimentalMaterial3Api`):** `ExposedDropdownMenuBox` requires `@OptIn(ExperimentalMaterial3Api::class)` — already used elsewhere in the module (e.g. `ExplorerIndexScreen` at line 95) `[VERIFIED: explorer/ExplorerIndexScreen.kt:95]`, so the opt-in is an established, detekt-clean pattern here.

### Q3 — Command-approach card: ONE composable, three prop groups, swappable cap

`ApproachLadderCard` is a **single** composable with three prop groups (per CONTEXT `<specifics>` and FEATURES) — not three widgets `[CITED: .planning/research/FEATURES.md:234]`:

1. **Ladder display (VAPPR-01):** `ladder: List<ApproachRungUiModel>` rendered as a **vertical ordered list** of labeled rungs (order = list order; do not rely on a separate sort key). Vertical preferred — labels like "Grammar / SingleShot / Plan / Agentic" are long and the ladder can grow `[CITED: .planning/research/FEATURES.md:81]`.
2. **Offline-only toggle (VAPPR-02):** `offlineOnly: Boolean?` + `onOfflineOnlyChange: ((Boolean) -> Unit)?` → `SegmentedOptionSelector` (2-option) or `Switch`. Null ⇒ the toggle is not shown (D-05: SB hides it until it has an offline tier).
3. **Max-tier cap (VAPPR-03):** `maxTierId: String?` + `onMaxTierChange: ((String) -> Unit)?`. **Default = tap-a-rung** — each rung acts as the cap selector; rungs **above** the cap render greyed-but-present with a "capped" affordance (conditional-render-no-dead-space) `[CITED: .planning/research/FEATURES.md:83,90]`.

**Swappable cap seam (D-03 "easy to swap"):** isolate the cap control behind a private composable slot so tap-a-rung vs `SingleChoiceSegmentedButtonRow` is a one-line swap at plan/UI time:
```kotlin
@Composable
private fun CapControl(               // swap point — tap-a-rung today, segmented tomorrow
    rungs: List<ApproachRungUiModel>,
    maxTierId: String?,
    onMaxTierChange: (String) -> Unit,
) { /* default: tap-a-rung marker on the ladder */ }
```

**Derive effective per-rung state IN the composable** (D-06 — pass primitives, not pre-rendered styling) `[CITED: .planning/research/FEATURES.md:84,236]`:
```
effective(rung) = rung.enabled
                && (!offlineOnly || rung.offlineCapable)   // offlineCapable is APP-derived (D-06)
                && rung.rank <= capRank
```
When `offlineOnly` is on, network-requiring rungs get a "needs network" affordance; capped rungs stay visible with a "capped" tag. `offlineCapable` is a **per-rung prop the consumer sets** — YAT never imports VAE's `TierPolicy` (D-06, L7 no hub-to-hub edge). The card **displays + emits only**; it never enforces/reorders/executes the ladder (VAPPR anti-feature) `[CITED: .planning/research/FEATURES.md:99-102]`.

**Every control hideable (D-05):** offline-only and cap props are nullable; a null/absent prop ⇒ the control is not rendered (never shown-disabled).

### Q4 — Prop / UI models: all-`val` immutables in `model/`

All prop-models are all-`val` `data class`es in `model/` (D-04). Generic field names only — the consumer maps engine output → these at the call site `[CITED: .planning/research/ARCHITECTURE.md:10,57]`. Suggested shapes (names/fields finalized at plan time):

```kotlin
// model/ProviderOptionUiModel.kt
data class ProviderOptionUiModel(val id: String, val label: String)

// model/ModelOptionUiModel.kt
data class ModelOptionUiModel(
    val id: String, val label: String,
    val subtitle: String? = null, val badge: String? = null,   // optional metadata slot
)

// model/ApproachRungUiModel.kt   (the ladder is List<ApproachRungUiModel>)
data class ApproachRungUiModel(
    val id: String,
    val label: String,
    val rank: Int,                 // ordering / cap comparison
    val enabled: Boolean = true,
    val offlineCapable: Boolean = false,   // APP-derived (D-06) — NOT from VAE TierPolicy
    val description: String? = null,
)
```
Plus a small render-only `keyState`/`modelsState` enum (e.g. `sealed interface KeyFieldState { Empty; Entered; Validating; Valid; data class Invalid(reason) }`) `[CITED: .planning/research/FEATURES.md:22,60]`.

**The `TagChipUiModel` copy()/STABLE lesson (D-04) — must-heed** `[VERIFIED: model/TagChipUiModel.kt:64-76,84-85]`: `TagChipUiModel`'s only `var` is `color`, declared **outside** the primary constructor. That single `var` makes the class **un-`STABLE`** to the Compose stability checker (line 64-70), and its out-of-constructor placement exists solely to avoid a `copy()` ABI break (line 42-55). **Keep every P10 model all-`val` in the primary constructor** — this both (a) preserves Compose-inferred `STABLE` (skippability across ~a dozen composables) and (b) avoids the ABI trap entirely, since an all-`val` immutable never needs the out-of-constructor workaround. Do NOT introduce any `var`.

⚠ **Model naming:** the `DomainVocabularyDriftGuardTest` head-token guard scans **`@Composable` functions only**, not data classes `[VERIFIED: DomainVocabularyDriftGuardTest.kt:86-98,186-221]` — so model names are not gated by that test. They ARE still governed by the reusability invariant (domain-neutral). ARCHITECTURE.md proposes `VoiceProviderUiModel`/`CommandTierUiModel` `[CITED: .planning/research/ARCHITECTURE.md:10]`; those lead with domain-flavored `Voice`/`Command`. Recommend generic model names aligned with the approved composable tokens (`ProviderOptionUiModel`, `ModelOptionUiModel`, `ApproachRungUiModel`) to stay consistent with the R1 naming approval. **Flag for plan-time decision.**

### Q5 — Naming + drift guard: `PRIMITIVE_NOUN_ALLOWLIST` edit

`DomainVocabularyDriftGuardTest` source-scans every non-`explorer` package for public top-level `@Composable`s and asserts each name's **leading PascalCase token (head token)** is in `PRIMITIVE_NOUN_ALLOWLIST` **or** the name is in `DOMAIN_VOCABULARY`; neither ⇒ build fails `[VERIFIED: DomainVocabularyDriftGuardTest.kt:96-109,136-144]`. Head-token examples confirmed in the test: `VoiceCard → Voice`, `CardBase → Card` `[VERIFIED: DomainVocabularyDriftGuardTest.kt:119-126]`.

With the approved names, the P10 head tokens are `Provider`, `Model`, `Approach` (`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`). None is currently listed `[VERIFIED: DomainVocabularyDriftGuardTest.kt:297-321 — PRIMITIVE_NOUN_ALLOWLIST contains no "Provider"/"Model"/"Approach"; "Card"/"Ladder"/"Undo" ARE present at lines 302,314]`. Per D-01 (APPROVED), add the **leading tokens** to `PRIMITIVE_NOUN_ALLOWLIST` — NOT the full names to `DOMAIN_VOCABULARY`.

**Location:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt`, companion `PRIMITIVE_NOUN_ALLOWLIST` set `[VERIFIED: DomainVocabularyDriftGuardTest.kt:297-321]`.

**Exact edit** — append a P10 block (keep it in its own block for auditability, matching the existing widening-block convention at lines 303-320):
```kotlin
            // Phase 10 (VSET/VAPPR, v2.4.0): structural leading tokens for the Voice Command
            // settings cards — ProviderKeyCard/ModelSelectCard/ApproachLadderCard. Generic
            // UI-archetype nouns (a provider/model choice, an escalation approach), NOT
            // consumer-domain vocabulary — added to PRIMITIVE_NOUN_ALLOWLIST per D-01 (APPROVED),
            // deliberately NOT grandfathered into DOMAIN_VOCABULARY.
            "Provider", "Model", "Approach"
```
(Insert before the closing `)` of the `setOf(...)`. If Phase 11/12 land in the same milestone, `Outcome`/`Command` join here too; `Undo` is already present at line 314.)

**Red→green check:** the test documents the exact demonstration — temporarily add a composable whose head token is in neither list (`ProjectCard → Project`) → RED; add the token → GREEN `[VERIFIED: DomainVocabularyDriftGuardTest.kt:45-49]`.

### Q6 — Registration: new tenth "Voice Command" family

Every new public `@Composable` outside `explorer/` must be registered XOR allowlisted; the scan denylists only `explorer/` (so any package under the scan root is covered) `[VERIFIED: ComponentRegistryDriftGuardTest.kt:54 `excludedPackages = setOf("explorer")`; DomainVocabularyDriftGuardTest.kt:53 same]`. Register the cohort in a **new tenth family "Voice Command"** with `tier = PATTERN` (required, no default) and a full 4-cell states matrix `[CITED: .planning/research/ARCHITECTURE.md:9,58-59,144-156]`.

**Three edits:**

1. **`explorer/ExplorerIndexScreen.kt` → `object ExplorerFamilies`** `[VERIFIED: explorer/ExplorerIndexScreen.kt:55-78]`:
   - Add const alongside the nine existing (after line 64):
     ```kotlin
     const val VOICE_COMMAND = "voice_command"
     ```
   - Append an `ORDERED_KEYS` row (after the `TACTILE_FOUNDATION` row, line 76):
     ```kotlin
         TACTILE_FOUNDATION to "Tactile Foundation",
         VOICE_COMMAND to "Voice Command"
     ```

2. **`explorer/VoiceCommandFamilyScreen.kt` (NEW)** — declare `internal val voiceCommandFamilyEntries: List<ComponentRegistry.Entry>`, one `Entry` per composable, mirroring the existing per-family file pattern `[VERIFIED: explorer/PickersFamilyScreen.kt:44-70]`. `Entry` shape: `name`, `family = ExplorerFamilies.VOICE_COMMAND`, `states` (4 cells), `content`, optional `controls`/`preview`, `tier = ComponentRegistry.Tier.PATTERN` `[VERIFIED: explorer/ComponentRegistry.kt:72-80]`. Fake fixtures live in this file — `explorer/` is drift-guard-denylisted, so fixtures don't need registration `[CITED: .planning/research/ARCHITECTURE.md:13,156]`. Example skeleton:
   ```kotlin
   internal val voiceCommandFamilyEntries: List<ComponentRegistry.Entry> = listOf(
       ComponentRegistry.Entry(
           name = "ProviderKeyCard",
           family = ExplorerFamilies.VOICE_COMMAND,
           states = listOf(
               ComponentRegistry.StateCell("Empty", render = { /* … */ }),
               ComponentRegistry.StateCell("Filled-hidden", render = { /* … */ }),
               ComponentRegistry.StateCell("Filled-revealed", render = { /* … */ }),
               ComponentRegistry.StateCell("Error / Invalid", render = { /* … */ }),
           ),
           content = { /* ProviderKeyCardVariants() */ },
           tier = ComponentRegistry.Tier.PATTERN,
       ),
       // ModelSelectCard, ApproachLadderCard …
   )
   ```

3. **`explorer/ComponentRegistry.kt` → `entries` concatenation** `[VERIFIED: explorer/ComponentRegistry.kt:94-102]` — append the tenth list:
   ```kotlin
   val entries: List<Entry> = cardsFamilyEntries +
       chipsFamilyEntries + sheetsFamilyEntries + buttonsFabFamilyEntries +
       pickersFamilyEntries + feedbackFamilyEntries + emptyStateFamilyEntries +
       progressFamilyEntries + tactileFoundationFamilyEntries +
       voiceCommandFamilyEntries
   ```

**Coordination note (13-prep groundwork):** CONTEXT/roadmap frame the Voice Command family scaffold as Phase 13 groundwork; Phase 10 must add the family scaffold **now** (const + ORDERED_KEYS row + `voiceCommandFamilyEntries` file + concat) so P10's three composables register the moment they land — otherwise the `ComponentRegistryDriftGuardTest` fails the build for unregistered composables. Later voice composables (P11/P12) append their own entries to the same family list.

⚠ **Doc-drift flag:** `CLAUDE.md` (line 33-38) and `API.md` (line 8-11) say "**seven**" / "**nine**" family lists. The live `ComponentRegistry.entries` concatenates **nine** `[VERIFIED: explorer/ComponentRegistry.kt:94-102]`; CLAUDE.md's "seven" is stale. Adding Voice Command makes **ten**. P13 should correct the "seven"/"nine" wording where it appears `[CITED: .planning/research/ARCHITECTURE.md:15]`.

### Q7 — Verify commands

Single-module hub → drop the module prefix `[CITED: CLAUDE.md:69-77]`:

| Command | What it gates |
|---------|---------------|
| `./gradlew testDebugUnitTest` | Unit / Robolectric / Compose-UI tests — includes `ComponentRegistryDriftGuardTest` (registered XOR allowlisted) and `DomainVocabularyDriftGuardTest` (head-token) and `ComponentRegistry`'s own `init{}` integrity checks `[VERIFIED: ComponentRegistry.kt:134-156]` |
| `./gradlew detekt` | Zero-baseline static analysis — must stay green at zero baseline (no new baseline) `[CITED: CLAUDE.md:78-81]` |
| `./gradlew apiCheck` | Metalava compatibility of the public API vs committed `api.txt` — delegates to `metalavaCheckCompatibilityRelease` `[VERIFIED: build.gradle.kts:39-43]` |
| `./gradlew apiDump` | Regenerates committed `api.txt` after an intended additive change — delegates to `metalavaGenerateSignatureRelease` `[VERIFIED: build.gradle.kts:33-37]` |

**Full-suite is required** for the CATALOG-03 / domain-vocab drift guards (both are JVM JUnit tests run under `testDebugUnitTest`) `[VERIFIED: DomainVocabularyDriftGuardTest.kt:51-110]`. Sequence for this phase: after appending masking params to `ClearableTextField`, run `./gradlew apiDump` to regenerate `api.txt`, review the additive diff, then `./gradlew testDebugUnitTest detekt apiCheck` to gate.

`./gradlew build` (full build incl. `assembleRelease`) and `./gradlew publishReleasePublicationToMavenLocal` (what JitPack runs) are available for a full local verification `[CITED: CLAUDE.md:73-77]`.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Key masking | Custom char-replacement `VisualTransformation` | `PasswordVisualTransformation()` (stock M3/Compose) | Handles bullet rendering + offset mapping correctly; first in-tree use, no dep `[CITED: .planning/research/STACK.md:30]` |
| Provider/model dropdown | Custom popup + anchor + dismiss | `ExposedDropdownMenuBox` + `ExposedDropdownMenu` | Stock M3 anchoring/focus/dismiss semantics; no dep `[CITED: .planning/research/STACK.md:55]` |
| 2-option offline toggle | New segmented row | `SegmentedOptionSelector` (in-tree) | Already ships the disabled+reason affordance and enforces the 2-option contract `[VERIFIED: component/SegmentedOptionSelector.kt:38-51]` |
| Key persistence / validation | Any storage or network in the library | Consumer owns it (props in, callbacks out) | INV-01 — library holds no secrets, no network `[CITED: .planning/research/FEATURES.md:37-38]` |
| Registry drift detection | A parallel name→composable map | The existing `ComponentRegistry.entries` single source of truth | `entries` is authoritative; a second map drifts `[VERIFIED: ComponentRegistry.kt:5-16]` |

**Key insight:** the entire P10 surface is deliberately data-in / callbacks-out so INV-01 (no secret, no network, no domain noun) is trivially true — any "helpful" storage/validation/fetch belongs in the consumer, not the library `[CITED: .planning/research/FEATURES.md:303-311]`.

## Common Pitfalls

### Pitfall 1: Trailing-lambda break when appending params
**What goes wrong:** appending a param after a previously-last **callback** silently breaks a caller that passed that callback as a trailing lambda.
**Why it happens:** Kotlin binds a trailing lambda to the LAST parameter `[CITED: API.md:200-204]`.
**How to avoid:** `ClearableTextField`'s current last param is `keyboardActions` (a non-lambda `KeyboardActions`), so appending after it is safe. Still, sweep call sites; if any masking/reveal param is a callback, callers must pass earlier callbacks as named args. `apiCheck` does NOT catch call-syntax breaks — only the compile fixture / manual sweep does.

### Pitfall 2: Forgetting to register a new composable (build-red)
**What goes wrong:** a new public `@Composable` in `component/` that is neither registered nor allowlisted fails `ComponentRegistryDriftGuardTest`.
**Why it happens:** the guard scans all non-`explorer` packages `[VERIFIED: ComponentRegistryDriftGuardTest.kt:54]`.
**How to avoid:** add every P10 composable to `voiceCommandFamilyEntries` (Q6) in the same change that introduces it.

### Pitfall 3: Head token not allowlisted (build-red)
**What goes wrong:** `ProviderKeyCard`/`ModelSelectCard`/`ApproachLadderCard` fail `DomainVocabularyDriftGuardTest` until `Provider`/`Model`/`Approach` are in `PRIMITIVE_NOUN_ALLOWLIST`.
**How to avoid:** land the Q5 allowlist edit in the same change (D-01 APPROVED).

### Pitfall 4: A `var` in a prop model
**What goes wrong:** a single `var` makes the model un-`STABLE` and can force an out-of-constructor `copy()` workaround (the `TagChipUiModel` regression).
**How to avoid:** keep every model all-`val` in the primary constructor (D-04) `[VERIFIED: model/TagChipUiModel.kt:64-76]`.

### Pitfall 5: Control shown-disabled instead of hidden
**What goes wrong:** rendering an offline-only/cap control greyed-out when the consumer hasn't opted in violates D-05.
**How to avoid:** make control props nullable; null/absent ⇒ not rendered (never shown-disabled). (Rungs *above the cap* ARE greyed-but-present — that is the cap affordance, a different case.)

## Runtime State Inventory

> Greenfield-additive phase (new composables + additive param + allowlist/registry edits). No rename/refactor/migration of stored data, live service config, OS-registered state, secrets, or build artifacts. **None — verified: this phase only adds new files and appends to allowlists/registry; it renames nothing and migrates no data.**

## Code Examples

All patterns are inline under Q1–Q6 above (verified against the pinned BOM and live source). External Material 3 references for `ExposedDropdownMenuBox` / masked-field / segmented patterns are in `.planning/research/FEATURES.md:315-319` (Android Developers segmented-button, composables.com, UX password patterns) `[CITED: .planning/research/FEATURES.md:315-319]`.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `TextField` + manual `DropdownMenu` anchoring | `ExposedDropdownMenuBox` (`menuAnchor(MenuAnchorType.…)`) | M3 stabilized well before BOM `2026.04.01` | Use the M3 box; `menuAnchor()` now takes a `MenuAnchorType` arg |
| `var` field + out-of-ctor workaround (`TagChipUiModel`) | all-`val` immutable models | this milestone's convention (D-04) | Preserves Compose `STABLE`; no ABI trap |

**Deprecated/outdated:** none relevant — nothing this phase touches is deprecated in BOM `2026.04.01`.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `menuAnchor(MenuAnchorType.PrimaryNotEditable, …)` is the current signature in BOM `2026.04.01` (vs the older no-arg `menuAnchor()`) | Q2 | LOW — compile error surfaces immediately; plan adjusts to whichever overload the pinned BOM exposes. Not verified against the BOM's exact `material3` API in this session |
| A2 | `RevealToggle` as a nullable value type is the cleanest hideable reveal affordance (vs two booleans) | Q1 | LOW — a plan-time modeling choice; either shape is additive/dependency-free |
| A3 | Generic model names (`ProviderOptionUiModel`, etc.) preferred over ARCHITECTURE.md's `VoiceProviderUiModel`/`CommandTierUiModel` | Q4 | LOW — models aren't head-token-gated; naming is a coherence call to resolve at plan time |
| A4 | Exact `KeyFieldState`/`modelsState` enum shape | Q4 | LOW — render-only enum; final shape is a plan-time detail |

## Open Questions (RESOLVED)

**All three are decision-closed** (plan-checker hygiene, 2026-09-29): (1) cap widget → **tap-a-rung** behind the `CapControl` swap seam (D-03 + CONTEXT Claude's Discretion; Yahir confirms in the gallery at Gate-1); (2) provider selector → **`ExposedDropdownMenuBox`**; (3) model naming → generic **`…OptionUiModel`/`ApproachRungUiModel`**, frozen in 10-01 Task 1's signature list. No residual risk to the phase goal.

1. **Cap widget final choice (tap-a-rung vs segmented).** _(RESOLVED: tap-a-rung behind `CapControl` seam.)_
   - What we know: default is tap-a-rung (D-03); `SingleChoiceSegmentedButtonRow` is the fallback; both dependency-free; must be built behind a swap seam.
   - What's unclear: the expected tier count at Gate-1 (3 vs 4+ rungs).
   - Recommendation: implement tap-a-rung behind the `CapControl` seam (Q3); Yahir reviews in the gallery and the swap is one line if segmented reads better.

2. **Provider selector: dropdown vs segmented.**
   - What we know: dropdown for many providers, segmented for ≤3 (Material 3 count rule).
   - Recommendation: default `ExposedDropdownMenuBox` (open-ended provider set); revisit at UI time if the consumer's provider set is known-small.

3. **Model naming coherence (see A3).**
   - Recommendation: adopt generic `…OptionUiModel`/`ApproachRungUiModel` names; confirm at plan time.

## Environment Availability

> Skipped — this phase is code-only against the already-declared, pinned Compose BOM `2026.04.01`. No external tool, service, runtime, or new package dependency. Verified: `ExposedDropdownMenuBox` and `PasswordVisualTransformation` resolve from existing `material3`/`ui` artifacts `[CITED: .planning/research/STACK.md:67]`.

## Validation Architecture

> `workflow.nyquist_validation: true` `[VERIFIED: .planning/config.json:25]` — section required.

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric `4.16.1` + Compose UI test (`createComposeRule()`) `[VERIFIED: gradle/libs.versions.toml:19; build.gradle.kts:79,131]` |
| Config file | `build.gradle.kts` (root-as-module); detekt configs `config/detekt/detekt.yml` + `config/detekt-compose.yml` `[VERIFIED: build.gradle.kts:79-131]` |
| Quick run command | `./gradlew testDebugUnitTest --tests "*ProviderKeyCardTest" --tests "*ModelSelectCardTest" --tests "*ApproachLadderCardTest"` |
| Full suite command | `./gradlew testDebugUnitTest detekt apiCheck` |

Established Compose-UI test harness (mirror for new tests) `[VERIFIED: src/test/.../component/CountBadgeTest.kt:22-37]`:
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProviderKeyCardTest {
    @get:Rule val composeTestRule = createComposeRule()
    @Test fun `masks key by default and reveals on toggle`() { /* setContent + onNodeWith… */ }
}
```
64 test files already use this pattern `[VERIFIED: find src/test -name "*Test.kt" | wc -l → 64]`.

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| VSET-01 | Key masked by default; reveal toggle flips masking; provider selection emits callback; no storage/network symbol in file | Compose UI + source-contract | `./gradlew testDebugUnitTest --tests "*ProviderKeyCardTest"` | ❌ Wave 0 |
| VSET-02 | Model selection emits callback; empty `models` renders disabled + reason (not blank) | Compose UI | `./gradlew testDebugUnitTest --tests "*ModelSelectCardTest"` | ❌ Wave 0 |
| VAPPR-01 | Ladder renders rungs in list order with rank affordance | Compose UI | `./gradlew testDebugUnitTest --tests "*ApproachLadderCardTest"` | ❌ Wave 0 |
| VAPPR-02 | Offline-only toggle reflects prop + emits change; network rungs annotated when on | Compose UI | `./gradlew testDebugUnitTest --tests "*ApproachLadderCardTest"` | ❌ Wave 0 |
| VAPPR-03 | Cap reflects `maxTierId`; tapping a rung emits `onMaxTierChange`; rungs above greyed-but-present | Compose UI | `./gradlew testDebugUnitTest --tests "*ApproachLadderCardTest"` | ❌ Wave 0 |
| CATALOG-03 | All three composables registered in a family; head tokens allowlisted | Registry/drift (JVM JUnit) | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest" --tests "*DomainVocabularyDriftGuardTest"` | ✅ (guards exist; go green once Q5/Q6 edits land) |
| API-01 | Public API additive vs `v2.3.0` (`ClearableTextField` masking append) | Metalava | `./gradlew apiCheck` | ✅ (task exists) |

### Sampling Rate
- **Per task commit:** `./gradlew testDebugUnitTest --tests "*<ComponentUnderEdit>Test"` (fast, targeted).
- **Per wave merge:** `./gradlew testDebugUnitTest detekt` (full unit + drift guards + static analysis).
- **Phase gate:** `./gradlew testDebugUnitTest detekt apiCheck` all green before `/gsd-verify-work`; regenerate `api.txt` with `./gradlew apiDump` after the intended `ClearableTextField` additive diff.

### Wave 0 Gaps
- [ ] `src/test/.../component/ProviderKeyCardTest.kt` — covers VSET-01
- [ ] `src/test/.../component/ModelSelectCardTest.kt` — covers VSET-02
- [ ] `src/test/.../component/ApproachLadderCardTest.kt` — covers VAPPR-01/02/03
- [ ] (optional) `ClearableTextFieldMaskingTest.kt` — locks the additive masking/reveal behavior + a source-compat fixture (mirror of `ShowTagColorsSourceCompatTest`)
- [ ] Framework install: none — Robolectric + Compose UI test infra already present `[VERIFIED: build.gradle.kts:79,129-131]`

## Security Domain

> API keys are user secrets, so V6/V5 posture is called out — but the load-bearing control is architectural: the library **never holds the key**. `security_enforcement` not explicitly disabled; section included.

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | Library renders a key field; it performs no auth |
| V3 Session Management | no | Stateless presentational composables |
| V4 Access Control | no | No access decisions in the library |
| V5 Input Validation | partial | Key trimmed on change (pure formatting); real validation is the consumer's `keyState` — library never validates/probes |
| V6 Cryptography / Secret handling | yes (by exclusion) | **Library stores no secret, logs no secret, transmits no secret.** Key arrives via prop, edits emit via callback; consumer owns storage (EncryptedSharedPrefs/DataStore/Keystore). Default masking via `PasswordVisualTransformation` prevents shoulder-surf |

### Known Threat Patterns for {Compose presentational key field}
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Secret leaked to logs / analytics | Information Disclosure | Library never logs the key value; no telemetry in the library (INV-01) |
| Shoulder-surfing the key | Information Disclosure | `PasswordVisualTransformation` masked by default; reveal is explicit + opt-in |
| Secret persisted in a reusable lib (supply-chain surface) | Tampering / Info Disclosure | Library holds nothing; storage is the consumer's job (anti-feature: no library-side persistence) `[CITED: .planning/research/FEATURES.md:37]` |
| Domain/engine logic dragged into UI | — (reuse break) | Display + emit only; no `TierPolicy`, no OkHttp (D-06, INV-01) |

## Sources

### Primary (HIGH confidence)
- Live source (opened this session): `component/ClearableTextField.kt`, `component/SegmentedOptionSelector.kt`, `model/TagChipUiModel.kt`, `explorer/ComponentRegistry.kt`, `explorer/DomainVocabularyDriftGuardTest.kt`, `explorer/ExplorerIndexScreen.kt`, `explorer/PickersFamilyScreen.kt`, `build.gradle.kts`, `api.txt`, `.planning/config.json`, `src/test/.../component/CountBadgeTest.kt`
- `CLAUDE.md` (root) — invariants, toolchain, verify commands, detekt zero-baseline
- `API.md` — additive-compatibility scope (lines 191-214)
- `.planning/phases/10-.../10-CONTEXT.md` — locked decisions D-01…D-06 (R1 APPROVED)

### Secondary (MEDIUM confidence)
- `.planning/research/STACK.md` — BOM/API map, no-new-dep claim
- `.planning/research/FEATURES.md` — prop shapes, states matrices, anti-features
- `.planning/research/ARCHITECTURE.md` — flat-`component/` placement, new-family registration, doc-drift flag

### Tertiary (LOW confidence)
- Material 3 external refs cited in FEATURES.md (segmented-button / dropdown-by-count / password patterns) — not re-fetched this session

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — every API verified present in the pinned BOM; both new-use symbols confirmed absent in-tree
- Architecture / registration / drift-guard edits: HIGH — verified against live `ComponentRegistry.kt`, `DomainVocabularyDriftGuardTest.kt`, `ExplorerIndexScreen.kt`, `PickersFamilyScreen.kt` (line-cited)
- Additive API change: HIGH — verified against `api.txt` signature + `API.md` compatibility scope
- Pitfalls: HIGH — each grounded in a live-source line reference
- Model naming coherence: MEDIUM — a plan-time coherence call (research vs approved-naming tension flagged)

**Research date:** 2026-09-29
**Valid until:** 2026-10-29 (stable — pinned toolchain, no fast-moving deps)
