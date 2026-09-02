# Phase 5: Gardening — Unify & Coordinated Repin - Pattern Map

**Mapped:** 2026-09-01
**Files analyzed:** 9 (2 modified for WO-1, 4 modified + 1 new for WO-2, plus registry/api/test bookkeeping)
**Analogs found:** 9 / 9 (all files have a strong in-repo analog — this phase is a same-repo refactor, not new-territory work)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|--------------------|------|-----------|-----------------|----------------|
| `component/ChipBar.kt` (modified — gains `expandable`/`rawContent`) | component (PRIMITIVE) | request-response (stateless render of caller-owned state) | `component/FilterBar.kt` (the sibling being folded in) | exact — same file pair, this session's own read |
| `component/FilterBar.kt` (modified/removed — folded into ChipBar) | component (PRIMITIVE, retiring) | request-response | `component/ChipBar.kt` | exact |
| `explorer/ChipsFamilyScreen.kt` (modified — remove `FilterBar` `Entry`, extend `ChipBarVariants()`) | route/registry-entry (explorer) | CRUD (list mutation of a static demo-entry list) | itself (prior revision) — structurally identical family-screen pattern also in `explorer/SheetsFamilyScreen.kt` | exact (self) |
| `component/TextCardBottomSheet.kt` (modified — delegates header/menu/dialog) | component (PATTERN) | request-response | `component/ListCardBottomSheet.kt` (near-duplicate sibling) | exact |
| `component/ListCardBottomSheet.kt` (modified — delegates header/menu/dialog) | component (PATTERN) | request-response | `component/TextCardBottomSheet.kt` | exact |
| new shared composable (e.g. `component/SheetHeaderMenu.kt`) | component (extracted shared sub-part) | request-response | `component/CardQuickView.kt` — the repo's own D-04 precedent for "extract the shared structure from Text/List sheets into a standalone content-only composable" | exact — explicitly cited by the audit as the shape to mirror |
| `explorer/SheetsFamilyScreen.kt` (unchanged entries, possible new `Entry` or `INTENTIONALLY_UNREGISTERED` line) | route/registry-entry (explorer) | CRUD | `explorer/ChipsFamilyScreen.kt` (WO-1's own sibling edit) | exact |
| `explorer/ComponentRegistry.kt` (`INTENTIONALLY_UNREGISTERED` map edit only) | config/registry (single source of truth) | CRUD (map entry add/remove) | itself — `SwipeableActionRow` allowlist entry is the direct precedent shape | exact (self) |
| `api.txt` (Metalava rebaseline) | config (generated signature file) | batch (regenerate + diff-review) | itself (`./gradlew apiDump`, tool-generated, not hand-authored) | exact (tool-owned) |
| `src/test/.../component/TextListBottomSheetEditMenuSourceContractTest.kt` (retargeted) | test | transform (plain-text source-scan assertions) | `src/test/.../component/ListCardBottomSheetReadOnlyPreviewSourceContractTest.kt` + `SourceContractTestSupport.kt` | exact — same source-contract test family/pattern |

## Pattern Assignments

### `component/ChipBar.kt` (component, request-response) — WO-1

**Analog:** `component/FilterBar.kt` (the file being folded in) and the file's own current shape.

**Current `ChipBar` signature** (source: `api.txt:100-101`, confirmed current):
```kotlin
@Composable
fun <T> ChipBar(
    items: List<T>,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "chip_bar",
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
)
```

**`FilterBar`'s chrome being folded in** (source: `component/FilterBar.kt:54-113`, read in full this session — expand/collapse `Surface` + chevron `IconButton` + height-capped scrolling `FlowRow`):
```kotlin
@Composable
fun FilterBar(
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    filterContentDescription: String = "Tag filters",
    content: @Composable FlowRowScope.() -> Unit
)
```

**Target shape (trailing, nullable, defaulted params — this is the established codebase idiom, seen identically in `TextCardBottomSheet`/`ListCardBottomSheet`'s own `onEditRequest`/`readOnlyPreview`/`previewOverflowCount` additions):**
```kotlin
data class ExpandableConfig(
    val expanded: Boolean,
    val onExpand: () -> Unit,
    val onCollapse: () -> Unit,
    val contentDescription: String = "Tag filters"
)

@Composable
fun <T> ChipBar(
    items: List<T>,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "chip_bar",
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    expandable: ExpandableConfig? = null,
    rawContent: (@Composable FlowRowScope.() -> Unit)? = null
) { /* Surface+chevron chrome gated on expandable != null, wraps the existing FlowRow body */ }
```

**Conditional-render-no-dead-space precedent to reuse** (source: `component/CardQuickView.kt:70-108`) — the header/tagContent/expandable-chrome-gated-by-null idiom used across this codebase:
```kotlin
if (titleSlotVisible(title)) { /* ... */ }
if (tagContent != null) { tagContent(); Spacer(...) }
```
Apply the same `if (expandable != null) { /* chevron+Surface chrome */ }` gating in `ChipBar`.

---

### `explorer/ChipsFamilyScreen.kt` (registry-entry, CRUD) — WO-1

**Analog:** itself; structurally identical to `explorer/SheetsFamilyScreen.kt`.

**What to edit** (source: `explorer/ChipsFamilyScreen.kt:56-257`, `:213-256`, `:343-381`, `:407-430`, confirmed by RESEARCH.md's full read this session):
- Delete the `FilterBar` `Entry(...)` block at `ChipsFamilyScreen.kt:213-256`.
- Delete its demo function `FilterBarVariants()` at `:407-430`.
- Extend `ChipBarVariants()` at `:343-381` with a new expandable-mode demo so the fold-in behavior stays showcased.

**Entry-list shape precedent** (mirrors the `Entry(...)` calls throughout this file and `SheetsFamilyScreen.kt`):
```kotlin
Entry(
    name = "ChipBar",
    family = "Chips",
    tier = Tier.PRIMITIVE,
    states = listOf(/* ... */),
    content = { ChipBarVariants() }
)
```
No numeric entry-count assertion exists anywhere (confirmed grep of `ComponentRegistryTierTest.kt`/`ComponentRegistrySearchTest.kt`), so deleting one `Entry` cannot break a count-based test — only the registered-XOR-allowlisted invariant below.

---

### New shared composable (e.g. `component/SheetHeaderMenu.kt`) — WO-2

**Analog:** `component/CardQuickView.kt` (D-04 precedent, explicitly cited by the coherence audit as the shape to mirror) — a standalone, registered, content-only composable extracted from the common structure of the same two files (`TextCardBottomSheet`/`ListCardBottomSheet`).

**Imports pattern to copy** (source: `component/CardQuickView.kt:1-25`):
```kotlin
package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.theme.Dimens
```

**KDoc precedent-citation pattern** (source: `component/CardQuickView.kt:27-51`) — the header comment style to copy verbatim in structure (states the extraction rationale, cites the disposition ID, documents each param, calls out what the archetype deliberately does NOT own):
```kotlin
/**
 * Shared display (non-edit) archetype (D-04), extracted from the common structure of
 * `TextCardBottomSheet` and `ListCardBottomSheet`: ...
 *
 * Content-only — this archetype renders NO ... chrome; adopters own ...
 *
 * @param title ...
 */
```
Apply the same pattern for the new composable, citing WO-2 instead of D-04, and documenting that it owns the header `Row` + three-dot `DropdownMenu` + rename `AlertDialog` triad only (not the sheet body).

**Target signature** (source: derived from `TextCardBottomSheet.kt:110-225,268-296` and `ListCardBottomSheet.kt:136-243,302-330`, both read in full in RESEARCH.md this session — union of both files' actual code):
```kotlin
@Composable
internal fun SheetHeaderMenu(
    title: String,
    isPinned: Boolean,
    isFavorite: Boolean,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmRename: (String) -> Unit,
    onEditRequest: (() -> Unit)? = null,
    imageCount: Int = 0,
    modifier: Modifier = Modifier
) { /* header Row + three-dot DropdownMenu + rename AlertDialog, verbatim union of both bodies */ }
```

**CRITICAL — region-marker preservation (Pitfall 1):** the `// region:edit-menu-item` / `// endregion:edit-menu-item` comments (source: `TextCardBottomSheet.kt:169-188`, `ListCardBottomSheet.kt:187-206`) are load-bearing test anchors for `TextListBottomSheetEditMenuSourceContractTest.editMenuItemRegion()`. They MUST move into the new file verbatim, not be dropped during extraction.

**`internal` visibility caution (Pitfall 3):** declare the new composable `internal` unless a deliberate decision is made to register it publicly (A2 in RESEARCH.md — mirrors `CardQuickView`, which IS public/registered). Either way, `./gradlew apiDump`'s diff must be reviewed line-by-line to confirm the visibility choice was intentional, not an accident of a missing modifier.

---

### `component/TextCardBottomSheet.kt` / `component/ListCardBottomSheet.kt` (modified) — WO-2

**Analog:** each other (near-duplicate siblings) — post-edit, both should call the new `SheetHeaderMenu` composable in place of their current inline header `Row` / three-dot `DropdownMenu` / rename `AlertDialog` blocks.

**Current call-site shape to replace** (source: `TextCardBottomSheet.kt:169-188` region markers, `:268-296` rename dialog; `ListCardBottomSheet.kt:187-206`, `:302-330`) — replace the inline blocks with:
```kotlin
SheetHeaderMenu(
    title = title,
    isPinned = isPinned,
    isFavorite = isFavorite,
    onTogglePin = onTogglePin,
    onToggleFavorite = onToggleFavorite,
    onDelete = onDelete,
    onDismiss = onDismiss,
    onConfirmRename = onConfirmRename,
    onEditRequest = onEditRequest,
    imageCount = imageCount // TextCardBottomSheet only; ListCardBottomSheet omits (defaults to 0)
)
```
**Asymmetry to preserve (anti-pattern flagged in RESEARCH.md):** `ImageCountIndicator` is `TextCardBottomSheet`-only — absent entirely from `ListCardBottomSheet.kt`'s header `Row`. The extraction must parameterize this via `imageCount: Int = 0`, not silently add/drop it.

---

### `explorer/ComponentRegistry.kt` — `INTENTIONALLY_UNREGISTERED` edit (WO-1 fold-mechanism, WO-2 optional)

**Analog:** the file's own existing `SwipeableActionRow` allowlist entry (source: `explorer/ComponentRegistry.kt:115-118`, read in full this session).

**Exact shape to copy for `FilterBar`'s demote (if D-02 resolves to demote-rather-than-delete):**
```kotlin
"SwipeableActionRow" to
    "Swipe-reveal mechanics powering CardBase and EditorItemRow — infrastructure, not " +
    "an independent visual archetype; already exercised indirectly via every card " +
    "entry's reveal-confirm swipe and via EditorItemRow's own demo.",
```
Copy this exact `"Name" to "One-line reason..."` map-entry idiom for `"FilterBar" to "..."` (folded into ChipBar's expandable mode, see WO-1) and/or the new WO-2 shared composable if it is allowlisted rather than registered.

**Object-init invariant that any registry edit must keep satisfied** (source: `explorer/ComponentRegistry.kt:130-152`, read in full):
```kotlin
init {
    val entryNames = entries.map { it.name }
    val duplicateEntryNames = entryNames.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    require(duplicateEntryNames.isEmpty()) { /* ... */ }

    val overlap = entryNames.toSet() intersect INTENTIONALLY_UNREGISTERED.keys
    require(overlap.isEmpty()) {
        "Name(s) present in both entries and INTENTIONALLY_UNREGISTERED: $overlap — a " +
            "component must be registered XOR allowlisted, never both."
    }

    val blankReasons = INTENTIONALLY_UNREGISTERED.filterValues { it.isBlank() }.keys
    require(blankReasons.isEmpty()) { /* ... */ }
}
```
This fires at class-load time — a half-finished registry edit (name left in both `entries` and `INTENTIONALLY_UNREGISTERED`, or removed from `entries` without a corresponding allowlist/delete) fails EVERY test that touches `ComponentRegistry` (`ComponentRegistryDriftGuardTest`, `ComponentRegistryTierTest`, `ComponentRegistrySearchTest`, `DomainVocabularyDriftGuardTest`), not just the drift guard. Make each registry edit atomic and re-run `ComponentRegistryDriftGuardTest` alone immediately after.

---

### `src/test/.../component/TextListBottomSheetEditMenuSourceContractTest.kt` (retargeted)

**Analog:** `src/test/.../component/ListCardBottomSheetReadOnlyPreviewSourceContractTest.kt` + `src/test/.../component/SourceContractTestSupport.kt` — same source-contract test family (plain `File.readText()` + string assertions), the established pattern for testing Compose sheet chrome that Robolectric cannot render (`ModalBottomSheet` is confirmed not drivable by this module's Robolectric harness, per 3 existing test KDocs).

**What must change** (source: RESEARCH.md Pitfall 1, from the 204-line test read in full this session): the test currently calls `SourceContractTestSupport.source("TextCardBottomSheet.kt")` and `source("ListCardBottomSheet.kt")` directly and asserts on `onEditRequest: (() -> Unit)? = null`, `if (onEditRequest != null)`, `showRenameDialog`, `AlertDialog(`, `onConfirmRename(`, and the `editMenuItemRegion()` marker-scan. All of this code moves into the new shared file — the test must be retargeted:
```kotlin
// Retarget the moved assertions (header/menu/dialog) at the new shared file:
val headerMenuSource = SourceContractTestSupport.source("SheetHeaderMenu.kt")
// Keep any body-slot-specific assertions (if any remain) pointed at the original two files.
```
This is a required in-scope edit, not optional cleanup — `./gradlew testDebugUnitTest` goes red the moment WO-2 lands without this retarget (`require(start >= 0 && end > start)` failure on the missing region markers).

---

## Shared Patterns

### Registered-XOR-allowlisted invariant (applies to every registry-touching file this phase)
**Source:** `explorer/ComponentRegistry.kt:130-152` (verbatim, see excerpt above under the ComponentRegistry section)
**Apply to:** `explorer/ChipsFamilyScreen.kt`, `explorer/SheetsFamilyScreen.kt`, `explorer/ComponentRegistry.kt` itself, and any new composable's registration choice.

### `HUB_LANE_OVERRIDE=3` commit gate (applies to every commit touching the 4 affected source files, the registry, or `api.txt`)
**Source:** `tools/hooks/pre-commit:16-25` + `tools/README-api-guard.md:9-12`
```bash
HUB_LANE_OVERRIDE=3 git commit -m "..."
```
**Apply to:** WO-1 commit, WO-2 commit, the registry/api.txt rebaseline commit — every commit this phase makes to `ChipBar.kt`, `FilterBar.kt`, `TextCardBottomSheet.kt`, `ListCardBottomSheet.kt`, the new shared file, `ComponentRegistry.kt`, `ChipsFamilyScreen.kt`, `SheetsFamilyScreen.kt`, or `api.txt`.

### Trailing-nullable-defaulted param idiom (applies to every signature change this phase)
**Source:** existing `TextCardBottomSheet`/`ListCardBottomSheet` additions of `onEditRequest`, `readOnlyPreview`, `previewOverflowCount` — confirmed established codebase pattern this session.
**Apply to:** `ChipBar`'s new `expandable`/`rawContent` params (WO-1) — always append new params as trailing, nullable, defaulted so every existing call site (5 files in SecondBrain for `ChipBar`/`FilterBar`) keeps compiling unchanged.

### `apiDump`/`apiCheck` line-by-line rebaseline review (Pitfall 3)
**Source:** `build.gradle.kts:16-29` (Metalava plugin wiring); current baseline lines confirmed at `api.txt:100-101` (`ChipBarKt`), `:161-162` (`FilterBarKt`), `:205-206` (`ListCardBottomSheetKt`), `:337-338` (`TextCardBottomSheetKt`).
**Apply to:** the rebaseline commit — diff the post-edit `./gradlew apiDump` output against these 4 baseline lines; confirm only the intended symbols changed (`ChipBarKt.ChipBar` signature grows, `FilterBarKt` removed, and — deliberately — whether the new shared composable appears at all, per its `internal`/public choice).

## No Analog Found

None — every file this phase touches has a strong, directly-cited in-repo analog (this is a same-repo refactor of already-shipped code, not new-territory work). `api.txt` and the pre-commit lane gate are tool/process-owned, not hand-authored-pattern files, but their exact mechanics are fully documented above from direct reads.

## Metadata

**Analog search scope:** `component/`, `explorer/`, `src/test/.../component/`, `src/test/.../explorer/` (all read in full or targeted this session and in RESEARCH.md's own session)
**Files scanned:** `FilterBar.kt`, `ChipBar.kt`, `TextCardBottomSheet.kt`, `ListCardBottomSheet.kt`, `CardQuickView.kt`, `ChipsFamilyScreen.kt`, `SheetsFamilyScreen.kt`, `ComponentRegistry.kt`, `ComponentRegistryDriftGuardTest.kt`, `TextListBottomSheetEditMenuSourceContractTest.kt`, `ListCardBottomSheetReadOnlyPreviewSourceContractTest.kt`, `SourceContractTestSupport.kt`, `api.txt`, `tools/hooks/pre-commit`, `build.gradle.kts`
**Pattern extraction date:** 2026-09-01
