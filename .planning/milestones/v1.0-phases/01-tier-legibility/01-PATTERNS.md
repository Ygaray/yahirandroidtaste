# Phase 1: Tier Legibility - Pattern Map

**Mapped:** 2026-09-01
**Files analyzed:** 13 (1 data model, 9 family-screen data files, 2 Compose UI files, 1 new doc, 1 build artifact)
**Analogs found:** 13 / 13 (all touch existing code — no genuinely new architecture; the one net-new file, `docs/DESIGN-INTENT.md`, has no in-repo analog and is listed under "No Analog Found")

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `explorer/ComponentRegistry.kt` (`Entry` data class + new `Tier` enum) | model | CRUD (schema field addition) | itself — existing `StateCell` nested data class/enum-nesting precedent in the same file | exact (self-analog) |
| `explorer/CardsFamilyScreen.kt` (11 call sites) | model (data authoring) | CRUD | `explorer/ChipsFamilyScreen.kt` (or any sibling `*FamilyScreen.kt`) | exact |
| `explorer/ChipsFamilyScreen.kt` (5 call sites) | model (data authoring) | CRUD | `explorer/CardsFamilyScreen.kt` | exact |
| `explorer/SheetsFamilyScreen.kt` (18 call sites) | model (data authoring) | CRUD | `explorer/CardsFamilyScreen.kt` | exact |
| `explorer/ButtonsFabFamilyScreen.kt` (3 call sites) | model (data authoring) | CRUD | `explorer/CardsFamilyScreen.kt` | exact |
| `explorer/PickersFamilyScreen.kt` (4 call sites) | model (data authoring) | CRUD | `explorer/CardsFamilyScreen.kt` | exact |
| `explorer/FeedbackFamilyScreen.kt` (3 call sites) | model (data authoring) | CRUD | `explorer/CardsFamilyScreen.kt` | exact |
| `explorer/EmptyStateFamilyScreen.kt` (1 call site) | model (data authoring) | CRUD | `explorer/CardsFamilyScreen.kt` | exact |
| `explorer/ProgressFamilyScreen.kt` (4 call sites) | model (data authoring) | CRUD | `explorer/CardsFamilyScreen.kt` | exact |
| `explorer/TactileFoundationFamilyScreen.kt` (4 call sites) | model (data authoring) | CRUD | `explorer/CardsFamilyScreen.kt` | exact |
| `explorer/ExplorerIndexScreen.kt` (`ComponentRow`) | component | request-response (render-per-state) | itself — existing `ComponentRow` signature/body | exact (extend in place) |
| `explorer/ComponentDetailScreen.kt` (`TopAppBar` title) | component | request-response (render-per-state) | itself — existing `TopAppBar` title slot | exact (extend in place) |
| `api.txt` | config (generated API baseline) | batch (regenerated via `apiDump`) | itself — existing `Entry`/`ComponentRow`/`ComponentDetailScreen` entries | exact (mechanical regen) |
| `docs/DESIGN-INTENT.md` | config/doc (prose) | — | none in-repo (new file, new dir) | no analog — see below |

## Pattern Assignments

### `explorer/ComponentRegistry.kt` (model, schema field addition)

**Analog:** itself (`StateCell` nested-declaration precedent, same file)

**Current `Entry` shape** (`ComponentRegistry.kt:60-67`):
```kotlin
data class Entry(
    val name: String,
    val family: String,
    val states: List<StateCell> = emptyList(),
    val content: (@Composable () -> Unit)? = null,
    val controls: List<Control> = emptyList(),
    val preview: (@Composable (PlaygroundState) -> Unit)? = null
)
```

**Existing nested-declaration precedent to mirror for `Tier`** (`ComponentRegistry.kt:41`):
```kotlin
data class StateCell(val label: String, val render: (@Composable () -> Unit)? = null)
```

**Target shape** (append `tier` last, required, no default, per D-01):
```kotlin
enum class Tier { PRIMITIVE, PATTERN }

data class Entry(
    val name: String,
    val family: String,
    val states: List<StateCell> = emptyList(),
    val content: (@Composable () -> Unit)? = null,
    val controls: List<Control> = emptyList(),
    val preview: (@Composable (PlaygroundState) -> Unit)? = null,
    val tier: Tier
)
```
Nest `Tier` inside `ComponentRegistry` (same declaration pattern as `StateCell`) — do not declare it top-level in the `explorer` package, to stay visually distinct from the unrelated `HeatTier`/`RelatednessTier` enums already public elsewhere in `component/`.

**Anti-pattern to avoid:** no parallel `Map<String, Tier>` keyed by name — `Entry.tier` is the only source of truth (existing "entries alone is authoritative" invariant, `ComponentRegistry.kt:9-10`).

---

### `explorer/CardsFamilyScreen.kt` and the 8 sibling `*FamilyScreen.kt` files (model, data authoring — 53 call sites total)

**Analog:** each sibling file is the analog for every other — 100% identical named-argument call convention across all 9 files.

**Representative call site — `CardsFamilyScreen.kt:84-101`** (before):
```kotlin
ComponentRegistry.Entry(
    name = "CardBase",
    family = ExplorerFamilies.CARDS,
    states = listOf(
        ComponentRegistry.StateCell("Default") { CardBasePreview(tactileDepth = true) },
        ComponentRegistry.StateCell("Pressed / Selected"),
        ComponentRegistry.StateCell("Disabled"),
        ComponentRegistry.StateCell("Focused")
    ),
    content = { CardBaseVariants() },
    controls = listOf(cardBaseTactileDepthControl),
    preview = { state ->
        CardBasePreview(tactileDepth = state.boolean(cardBaseTactileDepthControl))
    }
)
```
**After** (add `tier = ComponentRegistry.Tier.PATTERN` as the trailing named arg — `CardBase` is tiered PATTERN per the D-03 litmus worked in RESEARCH.md: it encodes the reveal-confirm destructive-swipe interaction convention, not caller-content-only).

**Second representative call site — `ChipsFamilyScreen.kt:57-` (`AppChip`)**:
```kotlin
ComponentRegistry.Entry(
    name = "AppChip",
    family = ExplorerFamilies.CHIPS,
    states = listOf(
        ComponentRegistry.StateCell(
            "Default",
            ...
```
Same shape — every one of the 53 sites needs exactly one trailing `tier = ComponentRegistry.Tier.X` line added, no other structural change.

**Per-file call-site counts** (for task-splitting, from RESEARCH.md, grep-verified):
`CardsFamilyScreen.kt` 11, `ChipsFamilyScreen.kt` 5, `SheetsFamilyScreen.kt` 18, `ButtonsFabFamilyScreen.kt` 3, `PickersFamilyScreen.kt` 4, `FeedbackFamilyScreen.kt` 3, `EmptyStateFamilyScreen.kt` 1, `ProgressFamilyScreen.kt` 4, `TactileFoundationFamilyScreen.kt` 4 — total 53.

**Completeness check pattern:** `grep -c 'ComponentRegistry.Entry(' <file>` against the counts above before compiling; `./gradlew compileDebugKotlin` is the authoritative completeness gate (missing `tier` is a hard compile error naming the exact call site).

---

### `explorer/ExplorerIndexScreen.kt` — `ComponentRow` (component, request-response render)

**Analog:** itself — extend in place; this is the single shared row composable (per its own KDoc, reused by index search results and every family screen's own row list).

**Current signature and body** (`ExplorerIndexScreen.kt:260-281`):
```kotlin
fun ComponentRow(name: String, supportingLabel: String? = null, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(name) },
        supportingContent = supportingLabel?.let {
            {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
```
**Existing call site to update** (`ExplorerIndexScreen.kt:161`):
```kotlin
ComponentRow(
    // ... existing args — add tier = entry.tier here, plus every family screen's own row-list call site
```

**Target per UI-SPEC.md's Component Placement contract:** add `tier: ComponentRegistry.Tier` param; change `headlineContent` from bare `Text(name)` to `Row { Text(name); Spacer(Modifier.width(8.dp)); Badge { Text(tierLabel) } }`. Do not touch `leadingContent` (unused, reserved) or `trailingContent` (existing chevron). Every caller of `ComponentRow` (index search results + each family screen's own list) must pass `entry.tier`.

**Badge label mapping (exact strings, per UI-SPEC.md Copywriting Contract):** `Tier.PRIMITIVE` → `"Primitive"`, `Tier.PATTERN` → `"Pattern"`.

**Badge color mapping (per UI-SPEC.md Color contract):**
```kotlin
val (container, content) = when (tier) {
    ComponentRegistry.Tier.PRIMITIVE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    ComponentRegistry.Tier.PATTERN -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
}
Badge(containerColor = container, contentColor = content) { Text(tierLabel) }
```
Do not use `MaterialTheme.colorScheme.primary`/Teal accent — reserved for `SectionLabel`/FAB/interactive emphasis per the UI-SPEC.

---

### `explorer/ComponentDetailScreen.kt` — `TopAppBar` title (component, request-response render)

**Analog:** itself — extend in place; single detail-page composable resolved per `Entry`.

**Current imports** (`ComponentDetailScreen.kt:1-20`, package + relevant Compose imports):
```kotlin
package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
// ... (Icon, IconButton, HorizontalDivider, Box/Column already imported)
```

**Current title render** (`ComponentDetailScreen.kt:59-60`):
```kotlin
TopAppBar(
    title = { Text(entry.name, fontWeight = FontWeight.Medium) },
```
**Target** — mirror `ComponentRow`'s pattern for visual consistency (UI-SPEC.md §Component Placement item 2):
```kotlin
TopAppBar(
    title = {
        Row {
            Text(entry.name, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(8.dp))
            Badge(containerColor = ..., contentColor = ...) { Text(tierLabel(entry.tier)) }
        }
    },
```
`entry` is already in scope (`fun ComponentDetailScreen(entry: ComponentRegistry.Entry, ...)`, `ComponentDetailScreen.kt:50`) — no new parameter needed here, unlike `ComponentRow`.

---

### `api.txt` (config, generated API baseline — mechanical regen)

**Analog:** itself — the current `Entry` constructor line is the exact diff target.

**Current line** (`api.txt:391`):
```
ctor public ComponentRegistry.Entry(String name, String family, optional java.util.List<io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistry.StateCell> states, optional kotlin.jvm.functions.Function0<kotlin.Unit>? content, optional java.util.List<? extends io.github.ygaray.yahirandroidtaste.explorer.Control> controls, optional kotlin.jvm.functions.Function1<? super io.github.ygaray.yahirandroidtaste.explorer.PlaygroundState,kotlin.Unit>? preview);
```
Note zero trailing `optional` — this is the line that must gain a trailing non-optional `Tier tier` param after the field is added. Additionally, `ComponentDetailScreen`'s existing public signature is confirmed at `api.txt:378`:
```
method @KotlinOnly @androidx.compose.runtime.Composable public static void ComponentDetailScreen(io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistry.Entry entry, kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, io.github.ygaray.yahirandroidtaste.theme.ThemeMode themeMode, kotlin.jvm.functions.Function0<kotlin.Unit> onToggleTheme);
```
`ComponentDetailScreen` itself needs no signature change (it already receives the full `Entry`, which now carries `tier`) — only `ComponentRow`'s signature change and the new `Entry`/`Tier` lines land in the regenerated `api.txt`.

**Regeneration pattern:** `./gradlew apiDump` after the code compiles; commit the regenerated `api.txt` in the same commit as the code change; then `./gradlew apiCheck` passes. Do not hand-edit `api.txt`.

---

## Shared Patterns

### Required-last-field addition to a multi-call-site data class
**Source:** `explorer/ComponentRegistry.kt:60-67` (current `Entry`) + `explorer/StateCell` nesting precedent (`ComponentRegistry.kt:41`)
**Apply to:** `Entry` itself, and by extension every one of the 53 call sites across the 9 `*FamilyScreen.kt` files.
```kotlin
// Pattern: append new required field last, named-argument call sites everywhere
data class Entry(/* existing params */, val tier: Tier)
// every call site:
ComponentRegistry.Entry(name = ..., family = ..., /* ... */, tier = ComponentRegistry.Tier.X)
```

### Shared-composable single-edit-covers-all-surfaces
**Source:** `explorer/ExplorerIndexScreen.kt:260-281` (`ComponentRow`) + `explorer/ComponentDetailScreen.kt:50-60` (`ComponentDetailScreen`)
**Apply to:** both gallery display surfaces — one `ComponentRow` edit covers index search + all 9 family screens' row lists; one `ComponentDetailScreen` edit covers the detail header.

### Metalava api.txt regen-then-commit
**Source:** `api.txt:378,391,398` (existing `Entry`/`ComponentDetailScreen`/`copy` entries) + `build.gradle.kts:16-29` (`apiDump`/`apiCheck` tasks)
**Apply to:** any file whose public signature changes this phase (`Entry`, `ComponentRow`) — run `./gradlew apiDump`, review the diff, commit `api.txt` alongside the code change, then `./gradlew apiCheck` must pass.

### Tier badge visual (Material3 `Badge`, no new component)
**Source:** UI-SPEC.md §Component Placement & Interaction, §Color, §Copywriting Contract
**Apply to:** both `ComponentRow` and `ComponentDetailScreen`.
```kotlin
Badge(
    containerColor = if (tier == ComponentRegistry.Tier.PRIMITIVE)
        MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
    contentColor = if (tier == ComponentRegistry.Tier.PRIMITIVE)
        MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
) {
    Text(if (tier == ComponentRegistry.Tier.PRIMITIVE) "Primitive" else "Pattern")
}
```
Non-interactive (no `clickable`, no ripple); text-only (no color-alone reliance); `labelSmall` default style, no override.

## No Analog Found

| File | Role | Data Flow | Reason |
|---|---|---|---|
| `docs/DESIGN-INTENT.md` | doc (prose) | — | `docs/` does not exist yet (verified via `find` this session per RESEARCH.md) — wholly new directory and file, no in-repo prose-doc analog of this exact shape. Nearest siblings for tone/structure are root `CLAUDE.md`'s "invariants" framing and `API.md`'s public-surface framing, but neither is a structural template — write directly from D-03's litmus text (CONTEXT.md:25) and RESEARCH.md's worked `CardBase`/`ChipBar`/`HeatSwatch` litmus applications (RESEARCH.md "Code Examples" section). Must state: (1) primitives contract, (2) patterns contract, (3) the decidable litmus, (4) worked examples for the 3 borderline components, (5) a one-line scope note that `INTENTIONALLY_UNREGISTERED` sub-parts are outside the tier taxonomy (RESEARCH.md Open Question 1's recommendation). |

## Metadata

**Analog search scope:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/` (all 9 `*FamilyScreen.kt` + `ComponentRegistry.kt` + `ExplorerIndexScreen.kt` + `ComponentDetailScreen.kt`), `api.txt`, repo root (`docs/` absence confirmed).
**Files scanned:** 13 target files + `api.txt`, all read/grepped directly this session (not re-reading RESEARCH.md's already-cited ranges, only spot-verifying: `Entry` ctor, `StateCell` nesting, one `ChipsFamilyScreen.kt` call site, `ComponentRow` full body + one call site, `ComponentDetailScreen` imports + `TopAppBar` title line, `api.txt` lines 378/391/398).
**Pattern extraction date:** 2026-09-01
</content>
