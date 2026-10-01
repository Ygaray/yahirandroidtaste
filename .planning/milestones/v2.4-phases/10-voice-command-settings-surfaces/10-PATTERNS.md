# Phase 10: Voice command settings surfaces - Pattern Map

**Mapped:** 2026-09-29
**Files analyzed:** 12 (6 NEW composables/models, 3 EDITs, 3 NEW explorer/test artifacts)
**Analogs found:** 12 / 12 (all in-tree, all git-tracked)

All analog paths below are git-TRACKED source under `src/main` / `src/test` in the single-module
hub — no mirror/gitignored paths. Package root: `io.github.ygaray.yahirandroidtaste`.

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `component/ProviderKeyCard.kt` (NEW) | component (card) | request-response (props in / callbacks out) | `component/HeroStatCard.kt` | role-match (prop-driven presentational card) |
| `component/ModelSelectCard.kt` (NEW) | component (card) | request-response | `component/HeroStatCard.kt` | role-match |
| `component/ApproachLadderCard.kt` (NEW) | component (card) | request-response + ordered-list render | `component/HeroStatCard.kt` + `component/SegmentedOptionSelector.kt` | role-match |
| `component/ClearableTextField.kt` (EDIT) | component (field) | transform (additive masking param) | itself (additive append) / `SegmentedOptionSelector.kt` (additive-param precedent) | exact |
| provider/model dropdown (private, inside cards) | component (selector) | request-response | `component/SegmentedOptionSelector.kt` | role-match (no `ExposedDropdownMenuBox` in tree — first use) |
| `model/ProviderOptionUiModel.kt` (NEW) | model | data-holder | `model/ListItemUiModel.kt` | exact (all-`val` immutable) |
| `model/ModelOptionUiModel.kt` (NEW) | model | data-holder | `model/ListItemUiModel.kt` | exact |
| `model/ApproachRungUiModel.kt` (NEW) | model | data-holder | `model/ListItemUiModel.kt` + `model/TagChipUiModel.kt` (STABLE lesson) | exact |
| `explorer/VoiceCommandFamilyScreen.kt` (NEW) | explorer/registry family | batch (entries list) | `explorer/PickersFamilyScreen.kt` | exact |
| `explorer/ComponentRegistry.kt` (EDIT) | config/registry | — | `entries` concatenation lines 94-102 | exact |
| `explorer/ExplorerIndexScreen.kt` (EDIT) | config | — | `object ExplorerFamilies` lines 55-78 | exact |
| `DomainVocabularyDriftGuardTest.kt` (EDIT) | test (allowlist) | — | `PRIMITIVE_NOUN_ALLOWLIST` lines 297-321 | exact |

## Pattern Assignments

### `component/ProviderKeyCard.kt` / `ModelSelectCard.kt` (component, request-response)

**Analog:** `component/HeroStatCard.kt` — the closest prop-driven, presentational, hoisted-state
card that takes plain content + callbacks and holds no domain logic. (`CardBase.kt` is a CRUD
swipe/dropdown shell — heavier than these settings cards need; use `HeroStatCard` as the shape.)

**Prop-driven card signature pattern** (`HeroStatCard.kt:55-65`): required content params first,
then `modifier: Modifier = Modifier`, then optional callbacks/slots defaulted. Note the
"null callback ⇒ no clickable semantics node, never a disabled one" convention (matches D-05):
```kotlin
@Composable
fun HeroStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,   // null → no clickable node at all (no `enabled` param)
    shape: Shape = MaterialTheme.expressive.cardShapeLarge,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    accentBrush: Brush = MaterialTheme.expressive.heroGradient,
    content: (@Composable ColumnScope.() -> Unit)? = null,
)
```

**KDoc-per-param convention** (`HeroStatCard.kt:29-54`): every param documented; anti-patterns
called out inline ("Do not 'fix' this later..."). Mirror this density for the new cards.

**Surface + Column body + `Dimens` padding** (`HeroStatCard.kt:79-112`): wrap in `Surface(shape,
color)`, body in `Column(Modifier.padding(Dimens.HorizontalPadding))`, `testTag` on structural
nodes. `Text` uses `maxLines = 1` + `TextOverflow.Ellipsis`.

**Dropdown (provider/model) — FIRST in-tree use of `ExposedDropdownMenuBox`.** No existing analog;
nearest selection primitive is `SegmentedOptionSelector.kt`. Use the canonical M3 shape from
RESEARCH.md Q2 (`10-RESEARCH.md:232-262`); requires `@OptIn(ExperimentalMaterial3Api::class)` — an
established, detekt-clean pattern already used at `explorer/ExplorerIndexScreen.kt:95`.

---

### `component/ClearableTextField.kt` (EDIT — additive masking, transform)

**Analog:** itself — current signature `ClearableTextField.kt:38-50` (10 params, all after
`onValueChange` defaulted; last is `keyboardActions: KeyboardActions` — a non-lambda, so appending
after it is trailing-lambda-safe). Trailing `Close` clear-icon at `ClearableTextField.kt:57-68`.

**Additive-param precedent to mirror:** `keyboardActions` itself was appended additively as the last
param "preserving existing callers' behavior unchanged" (`ClearableTextField.kt:34-36` KDoc). Append
`visualTransformation: VisualTransformation = VisualTransformation.None` and a nullable
`revealToggle: RevealToggle? = null` (null ⇒ no eye affordance, D-05) after `keyboardActions`,
per RESEARCH.md Q1 (`10-RESEARCH.md:184-205`). The two-icon trailing slot (reveal eye beside the
existing clear-✕) must render `Row { revealButton?; clearButton }` — do not drop the clear icon.
Run `./gradlew apiDump` after the change, review the additive `api.txt` diff, then `apiCheck`.

---

### `component/ApproachLadderCard.kt` (component, request-response + ordered-list)

**Analogs:** `component/HeroStatCard.kt` (card shape, above) + `component/SegmentedOptionSelector.kt`
for the offline-only 2-state toggle.

**Ordered-list / ladder rendering:** no dedicated ladder composable exists; render
`ladder: List<ApproachRungUiModel>` as a vertical `Column` of rungs in list order (order = list
order; do not re-sort). The `entries` iteration convention (fixed declaration order is authoritative)
mirrors `ComponentRegistry.kt:82-86`. Derive per-rung effective state IN-composable
(`10-RESEARCH.md:289-295`), never pass pre-styled props.

**Offline-only toggle — `SegmentedOptionSelector` (`SegmentedOptionSelector.kt:38-89`):** exactly
2 options (`require(options.size == 2)` at line 49 — cannot back the 3-4-tier cap), ships an
always-visible disabled+reason caption (lines 79-87) that matches D-05's "hideable, never
shown-disabled" posture. Contrast/disabled discipline documented at lines 18-36.

**Cap control — build behind a swap seam (D-03):** isolate a private `CapControl` composable
(RESEARCH.md Q3 `10-RESEARCH.md:280-287`) so tap-a-rung ↔ `SingleChoiceSegmentedButtonRow` is a
one-line swap. `SingleChoiceSegmentedButtonRow`/`SegmentedButton`/`SegmentedButtonDefaults` usage
is demonstrated at `SegmentedOptionSelector.kt:53-66`.

---

### `model/ProviderOptionUiModel.kt` / `ModelOptionUiModel.kt` / `ApproachRungUiModel.kt` (model)

**Analog:** `model/ListItemUiModel.kt:14-20` — the canonical all-`val` immutable data class with
trailing optional (`= null` / defaulted) fields:
```kotlin
data class ListItemUiModel(
    val id: String,
    val text: String,
    val isCompleted: Boolean,
    val sortOrder: Int,
    val completedAt: Long? = null
)
```

**MUST-HEED STABLE/`copy()` lesson — `model/TagChipUiModel.kt:64-76,84-85`:** the single `var color`
(line 85, declared OUTSIDE the primary constructor) makes the class un-`STABLE` to the Compose
checker (lines 64-70) and only lives out-of-constructor to dodge a `copy()` ABI break (lines 42-55).
Keep every P10 model **all-`val` in the primary constructor** (D-04) — this preserves Compose-inferred
`STABLE` across the ~dozen composables and sidesteps the ABI trap entirely. Introduce NO `var`.

Note: model names are NOT head-token-gated (`DomainVocabularyDriftGuardTest` scans `@Composable`
functions only, `10-RESEARCH.md:326-327`) — but stay domain-neutral per the reusability invariant.

---

### `explorer/VoiceCommandFamilyScreen.kt` (NEW — registry family screen)

**Analog:** `explorer/PickersFamilyScreen.kt:44-70` — the per-family `internal val
xxxFamilyEntries: List<ComponentRegistry.Entry>` pattern. Each `Entry` supplies `name`,
`family = ExplorerFamilies.VOICE_COMMAND`, a 4-cell `states` matrix (`StateCell(label, render=…)`),
`content = { XxxVariants() }`, and `tier = ComponentRegistry.Tier.PATTERN` (required, no default —
`ComponentRegistry.kt:68-80`). Fixtures live in this file (explorer/ is drift-guard-denylisted).
`StateCell` render blocks hoist local state with `var … by remember { mutableStateOf(...) }`
(`PickersFamilyScreen.kt:52-58`).

**`Entry`/`StateCell`/`Tier` shapes:** `ComponentRegistry.kt:41` (`StateCell`), `:50` (`Tier`),
`:72-80` (`Entry`).

---

## Registry / Allowlist Edit Sites (exact)

### `explorer/ExplorerIndexScreen.kt` → `object ExplorerFamilies` (lines 55-78)
- Add const after line 64 (`TACTILE_FOUNDATION`): `const val VOICE_COMMAND = "voice_command"`
- Append `ORDERED_KEYS` row after line 76: `VOICE_COMMAND to "Voice Command"` (add comma to the
  prior `TACTILE_FOUNDATION to "Tactile Foundation"` line).

### `explorer/ComponentRegistry.kt` → `entries` concatenation (lines 94-102)
Append `+ voiceCommandFamilyEntries` to the nine-list chain (making ten). Registry `init{}` integrity
checks (duplicate-name + XOR-allowlist, lines 134-149) run at first access.

### `DomainVocabularyDriftGuardTest.kt` → `PRIMITIVE_NOUN_ALLOWLIST` (lines 297-321)
Head tokens `Provider`, `Model`, `Approach` are currently ABSENT (verified — `Card`/`Ladder`/`Undo`
ARE present at lines 299,302,314). Per D-01 (APPROVED) append a P10 block before the closing `)` of
`setOf(...)` — add the leading tokens to `PRIMITIVE_NOUN_ALLOWLIST`, NOT full names to
`DOMAIN_VOCABULARY`. Follow the existing per-phase commented-block convention (lines 303-320).

## Shared Patterns

### Prop-driven / hideable controls (D-05)
**Source:** `component/HeroStatCard.kt:56-65` (`onClick: (() -> Unit)? = null` → no node when null) +
`component/SegmentedOptionSelector.kt:44-46,79-87` (disabled+reason caption, always visible).
**Apply to:** all three cards — nullable control props ⇒ not rendered (never shown-disabled).

### Additive public-API append
**Source:** `component/ClearableTextField.kt:34-49` (`keyboardActions` appended as defaulted last
param) + `model/TagChipUiModel.kt:78-114` (`@JvmOverloads` / out-of-ctor ABI-avoidance rationale).
**Apply to:** `ClearableTextField` masking params. Verify with `apiDump` → `apiCheck`.

### Registry registration (CATALOG-03)
**Source:** `explorer/PickersFamilyScreen.kt:44-70` (family entries) + `ComponentRegistry.kt:94-102`
(concat) + `:134-149` (integrity `init`).
**Apply to:** every new public `@Composable` — register in `voiceCommandFamilyEntries`, `tier=PATTERN`.

### Compose `STABLE` / all-`val` models (D-04)
**Source:** `model/ListItemUiModel.kt:14-20` (do) + `model/TagChipUiModel.kt:64-76` (the `var` trap).
**Apply to:** all three new `model/` classes.

### `@OptIn(ExperimentalMaterial3Api::class)`
**Source:** `explorer/ExplorerIndexScreen.kt:95` (established, detekt-clean).
**Apply to:** the private provider/model `ExposedDropdownMenuBox` dropdown.

## No Analog Found

| Element | Role | Data Flow | Reason |
|---------|------|-----------|--------|
| `ExposedDropdownMenuBox` dropdown | selector | request-response | FIRST in-tree use — no existing `ExposedDropdownMenuBox`. Nearest selection analog is `SegmentedOptionSelector.kt`; use canonical M3 shape from `10-RESEARCH.md:232-262`. |
| tap-a-rung cap marker | control | request-response | No ladder-cap composable exists. Build behind the `CapControl` swap seam; `SingleChoiceSegmentedButtonRow` fallback demoed at `SegmentedOptionSelector.kt:53-66`. |

## Metadata

**Analog search scope:** `src/main/.../component/`, `.../model/`, `.../explorer/`, `src/test/.../explorer/`
**Files scanned:** 12 read in full/targeted; all git-tracked (verified via `git ls-files`)
**Pattern extraction date:** 2026-09-29
