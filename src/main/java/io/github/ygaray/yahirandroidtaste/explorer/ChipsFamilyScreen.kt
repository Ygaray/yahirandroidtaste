@file:OptIn(ExperimentalLayoutApi::class)

package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.AppChip
import io.github.ygaray.yahirandroidtaste.component.ChipBar
import io.github.ygaray.yahirandroidtaste.component.FilterBar
import io.github.ygaray.yahirandroidtaste.component.SortControl
import io.github.ygaray.yahirandroidtaste.component.TagChipWithContextMenu
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import io.github.ygaray.yahirandroidtaste.model.TagSortMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode

/**
 * D-05: this family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) so this file stays the single place Plans 03-05 enrich Chips'
 * `states`/`content` without touching the shared registry file. Order preserved exactly from
 * the pre-Phase-62-Plan-02 inline registry list.
 *
 * Pitfall 2 (RESEARCH.md): the assembled `ChipBar`+`SortControl`-embedded demo is assigned to
 * `ChipBar`'s Variants ONLY (it demonstrates ChipBar's own `leadingContent` slot). `SortControl`
 * keeps its own standalone demo as its Variants — the combined demo is never duplicated here.
 */
private val tagSortModeControl = Control.Enum(
    label = "Sort mode",
    options = TagSortMode.entries.map { it.label },
    default = TagSortMode.DEFAULT.label
)

// IN-02 fix: shared with AppChip's `controls`/`preview` below (same pattern as
// tagSortModeControl) so the label string is a single source of truth.
private val appChipSelectedControl = Control.Toggle(label = "Selected")

internal val chipsFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        name = "AppChip",
        family = ExplorerFamilies.CHIPS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    AppChip(
                        label = ExplorerFakeData.tagChips[1].name,
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            ),
            ComponentRegistry.StateCell(
                "Pressed / Selected",
                render = {
                    AppChip(
                        label = ExplorerFakeData.tagChips.first().name,
                        isSelected = true,
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            ),
            // AppChip has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // AppChip has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { AppChipVariants() },
        controls = listOf(appChipSelectedControl),
        preview = { state ->
            AppChip(
                label = ExplorerFakeData.tagChips.first().name,
                isSelected = state.boolean(appChipSelectedControl),
                onClick = {}
            )
        }
    ),
    ComponentRegistry.Entry(
        name = "TagChipWithContextMenu",
        family = ExplorerFamilies.CHIPS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    TagChipWithContextMenu(
                        label = ExplorerFakeData.tagChips[1].name,
                        isSelected = false,
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onEdit = {},
                        onRemoveFromContext = {},
                        onDelete = {},
                        removeLabel = "Remove from this card"
                    )
                }
            ),
            ComponentRegistry.StateCell(
                "Pressed / Selected",
                render = {
                    TagChipWithContextMenu(
                        label = ExplorerFakeData.tagChips.first().name,
                        isSelected = true,
                        onClick = {},
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onEdit = {},
                        onRemoveFromContext = {},
                        onDelete = {},
                        removeLabel = "Remove from this card"
                    )
                }
            ),
            // TagChipWithContextMenu has no disabled param — N/A (mirrors AppChip).
            ComponentRegistry.StateCell("Disabled"),
            // TagChipWithContextMenu has no focus-visual override — N/A (mirrors AppChip).
            ComponentRegistry.StateCell("Focused")
        ),
        content = { TagChipWithContextMenuVariants() }
    ),
    ComponentRegistry.Entry(
        name = "ChipBar",
        family = ExplorerFamilies.CHIPS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    ChipBar(
                        items = ExplorerFakeData.manyTagChips,
                        key = { it.id },
                        itemContent = { tag -> AppChip(label = tag.name, isSelected = false, onClick = {}) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            ),
            // Selection lives on the child AppChip items, not ChipBar itself — the assembled
            // with-selection + sort-control demo is in ChipBar's Variants (Pitfall 2), not here.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // ChipBar has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // ChipBar has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { ChipBarVariants() }
    ),
    ComponentRegistry.Entry(
        name = "SortControl",
        family = ExplorerFamilies.CHIPS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    var mode by remember { mutableStateOf(TagSortMode.DEFAULT) }
                    SortControl(
                        sortMode = mode,
                        options = TagSortMode.entries,
                        optionLabel = { it.label },
                        onSortModeChange = { mode = it },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            ),
            // Sort-mode cycling is demonstrated live in the Default render — no separate discrete
            // pressed/selected visual state.
            ComponentRegistry.StateCell("Pressed / Selected"),
            // SortControl has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // SortControl has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { SortControlVariants() },
        controls = listOf(tagSortModeControl),
        preview = { state ->
            val selectedLabel = state.selected(tagSortModeControl)
            val mode = TagSortMode.entries.firstOrNull { it.label == selectedLabel }
                ?: TagSortMode.DEFAULT
            SortControl(
                sortMode = mode,
                options = TagSortMode.entries,
                optionLabel = { it.label },
                onSortModeChange = { state.setSelected(tagSortModeControl, it.label) }
            )
        }
    ),
    ComponentRegistry.Entry(
        name = "FilterBar",
        family = ExplorerFamilies.CHIPS,
        states = listOf(
            ComponentRegistry.StateCell(
                "Default",
                render = {
                    FilterBar<TagChipUiModel>(
                        expanded = false,
                        onExpand = {},
                        onCollapse = {}
                    ) {
                        ExplorerFakeData.tagChips.forEach { tag ->
                            AppChip(label = tag.name, isSelected = false, onClick = {})
                        }
                    }
                }
            ),
            // FilterBar has no discrete pressed/selected visual — repurposed to showcase the
            // expanded (wrapped, height-capped, scrollable) state instead of a second collapsed
            // rendering.
            ComponentRegistry.StateCell(
                "Pressed / Selected",
                render = {
                    var expanded by remember { mutableStateOf(true) }
                    FilterBar<TagChipUiModel>(
                        expanded = expanded,
                        onExpand = { expanded = true },
                        onCollapse = { expanded = false }
                    ) {
                        ExplorerFakeData.manyTagChips.forEach { tag ->
                            AppChip(label = tag.name, isSelected = false, onClick = {})
                        }
                    }
                }
            ),
            // FilterBar has no disabled param — N/A.
            ComponentRegistry.StateCell("Disabled"),
            // FilterBar has no focus-visual override — N/A.
            ComponentRegistry.StateCell("Focused")
        ),
        content = { FilterBarVariants() }
    )
)

/**
 * Chips family (SHOW-01, D-04): a registry-filtered row-list picker. Each row navigates to that
 * component's own detail page (States matrix + Variants, EXPLORE-01) rather than rendering demos
 * inline here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipsFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { ChipsFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.CHIPS },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

/** AppChip's Phase-61 curated demo (selected + unselected), reused verbatim as its Variants. */
@Composable
private fun AppChipVariants() {
    SectionLabel("AppChip — selected")
    AppChip(
        label = ExplorerFakeData.tagChips.first().name,
        isSelected = true,
        onClick = {},
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    SectionLabel("AppChip — unselected")
    AppChip(
        label = ExplorerFakeData.tagChips[1].name,
        isSelected = false,
        onClick = {},
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/**
 * TagChipWithContextMenu's demo (Phase 93, TMENU-01/CATALOG-03) — a representative wiring of all
 * 3 capability callbacks (Edit + scoped-Remove + Delete), matching the 3-item, per-surface
 * Capability Table's most common shape. Long-press to open the menu.
 */
@Composable
private fun TagChipWithContextMenuVariants() {
    SectionLabel("TagChipWithContextMenu — Edit + Remove + Delete (long-press to open menu)")
    TagChipWithContextMenu(
        label = ExplorerFakeData.tagChips.first().name,
        isSelected = false,
        onClick = {},
        modifier = Modifier.padding(horizontal = 16.dp),
        onEdit = {},
        onRemoveFromContext = {},
        onDelete = {},
        removeLabel = "Remove from this card"
    )
    SectionLabel("TagChipWithContextMenu — Edit + Delete only (e.g. CoOccurrenceChip, no scope)")
    TagChipWithContextMenu(
        label = ExplorerFakeData.tagChips[1].name,
        isSelected = false,
        onClick = {},
        modifier = Modifier.padding(horizontal = 16.dp),
        onEdit = {},
        onDelete = {}
    )
}

/**
 * ChipBar's Phase-61 curated demos (D-01 A+ assembled examples), reused verbatim — BOTH the
 * unselected assembled example AND the with-selection + sort-control example that embeds
 * [SortControl] as `leadingContent`. The combined demo belongs here (Pitfall 2) because it
 * demonstrates ChipBar's own `leadingContent` slot, not SortControl's standalone API.
 */
@Composable
private fun ChipBarVariants() {
    SectionLabel("ChipBar — assembled example (unselected)")
    ChipBar(
        items = ExplorerFakeData.manyTagChips,
        key = { it.id },
        itemContent = { tag -> AppChip(label = tag.name, isSelected = false, onClick = {}) },
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    var sortMode by remember { mutableStateOf(TagSortMode.DEFAULT) }
    var selectedIds by remember { mutableStateOf(setOf(ExplorerFakeData.manyTagChips.first().id)) }
    SectionLabel("ChipBar — assembled example (with selection + sort control)")
    ChipBar(
        items = ExplorerFakeData.manyTagChips,
        key = { it.id },
        leadingContent = {
            SortControl(
                sortMode = sortMode,
                options = TagSortMode.entries,
                optionLabel = { it.label },
                onSortModeChange = { sortMode = it }
            )
        },
        itemContent = { tag ->
            AppChip(
                label = tag.name,
                isSelected = tag.id in selectedIds,
                onClick = {
                    selectedIds = if (tag.id in selectedIds) {
                        selectedIds - tag.id
                    } else {
                        selectedIds + tag.id
                    }
                }
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/**
 * SortControl's Phase-61 curated standalone demo, reused verbatim — this is its ONLY Variants
 * content; the assembled ChipBar-embedded demo lives on [ChipBarVariants] instead (Pitfall 2, no
 * duplication).
 */
@Composable
private fun SortControlVariants() {
    var standaloneSortMode by remember { mutableStateOf(TagSortMode.DEFAULT) }
    SectionLabel("SortControl — standalone")
    SortControl(
        sortMode = standaloneSortMode,
        options = TagSortMode.entries,
        optionLabel = { it.label },
        onSortModeChange = { standaloneSortMode = it },
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/**
 * FilterBar's showcase — a domain-free `content` slot supplying a few [AppChip] fixtures
 * (D-01: this is a shell-only extraction, so no [SortControl] is embedded here; that assembled
 * demo belongs to [ChipBarVariants]/[SortControlVariants] instead — Pitfall 2, no duplication).
 */
@Composable
private fun FilterBarVariants() {
    SectionLabel("FilterBar — collapsed")
    FilterBar<TagChipUiModel>(
        expanded = false,
        onExpand = {},
        onCollapse = {}
    ) {
        ExplorerFakeData.tagChips.forEach { tag ->
            AppChip(label = tag.name, isSelected = false, onClick = {})
        }
    }

    var expanded by remember { mutableStateOf(false) }
    SectionLabel("FilterBar — expanded (wraps + scrolls, height-capped)")
    FilterBar<TagChipUiModel>(
        expanded = expanded,
        onExpand = { expanded = true },
        onCollapse = { expanded = false }
    ) {
        ExplorerFakeData.manyTagChips.forEach { tag ->
            AppChip(label = tag.name, isSelected = false, onClick = {})
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipsFamilyTopBar(onNavigateBack: () -> Unit, themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    TopAppBar(
        title = { Text("Chips") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}

