package com.example.secondbrain.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.component.AlbumSourcePickerSheet
import com.example.secondbrain.yahirandroidtaste.component.AlbumTitleConfirmSheet
import com.example.secondbrain.yahirandroidtaste.component.BulkCreatePopup
import com.example.secondbrain.yahirandroidtaste.component.BulkCreatePopupContent
import com.example.secondbrain.yahirandroidtaste.component.CardEditorShellContent
import com.example.secondbrain.yahirandroidtaste.component.ClearableTextField
import com.example.secondbrain.yahirandroidtaste.component.EditorItemRow
import com.example.secondbrain.yahirandroidtaste.component.ListCardBottomSheet
import com.example.secondbrain.yahirandroidtaste.component.NameAndTagsEditor
import com.example.secondbrain.yahirandroidtaste.component.RecordingBottomSheetContent
import com.example.secondbrain.yahirandroidtaste.component.RecordingSheetUiState
import com.example.secondbrain.yahirandroidtaste.component.SheetScaffold
import com.example.secondbrain.yahirandroidtaste.component.TagChipEditorContent
import com.example.secondbrain.yahirandroidtaste.component.TagCreateSheet
import com.example.secondbrain.yahirandroidtaste.component.TagCreateSheetContent
import com.example.secondbrain.yahirandroidtaste.component.TagPickerSheet
import com.example.secondbrain.yahirandroidtaste.component.TagPickerSheetContent
import com.example.secondbrain.yahirandroidtaste.component.TextCardBottomSheet
import com.example.secondbrain.yahirandroidtaste.component.VoiceRenameTagsSheet
import com.example.secondbrain.yahirandroidtaste.explorer.ComponentRegistry.StateCell
import com.example.secondbrain.yahirandroidtaste.feedback.LocalFeedbackController
import com.example.secondbrain.yahirandroidtaste.theme.YahirAndroidTasteTheme
import com.example.secondbrain.yahirandroidtaste.theme.ThemeMode
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * D-05: this family's slice of [ComponentRegistry.entries], declared here (not in
 * `ComponentRegistry.kt`) so this file stays the single place Plans 03-05 enrich Sheets'
 * `states`/`content` without touching the shared registry file. Order preserved exactly from
 * the pre-Phase-62-Plan-02 inline registry list.
 *
 * Phase 62 Plan 04 (D-02/D-03): every entry below now carries a 4-cell States matrix (fixed
 * order Default -> Pressed / Selected -> Disabled -> Focused) and `content` wired to its
 * `*Variants()` wrapper. Most Sheets entries are modal/dialog components whose only
 * caller-controllable "state" is opening the sheet itself (Default), so the remaining 3 cells
 * are curated `null` (visible "Not applicable", D-03) with a one-line reason -- never
 * fabricated. Three entries (`TagChipEditorContent`, `ClearableTextField`, `EditorItemRow`)
 * expose real, caller-facing params that drive genuinely distinct Default/Pressed-Selected (and,
 * for `EditorItemRow`, Disabled/Focused) previews -- curated against their actual API, per D-03's
 * "curated in code against each component's real API" discretion note.
 */
internal val sheetsFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(
        "AlbumSourcePickerSheet",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- this modal sheet's only "state" is opening it, which
            // Variants demonstrates via its own "Show sheet" trigger below. Rendering the same
            // Variants demo again here previously created a second, independently-triggerable
            // live modal instance -- N/A instead.
            StateCell("Default"),
            // Pressed / Selected: modal source-picker sheet has no pressed/selected visual
            // distinct from Default -- opening the sheet is the interaction itself -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: AlbumSourcePickerSheet exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: modal sheet has no focus-visual param -- N/A
            StateCell("Focused")
        ),
        content = { AlbumSourcePickerSheetVariants() }
    ),
    ComponentRegistry.Entry(
        "AlbumTitleConfirmSheet",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- this modal sheet's only "state" is opening it, which
            // Variants demonstrates via its own "Show sheet" trigger below -- N/A instead of
            // rendering (and independently opening) the same demo twice.
            StateCell("Default"),
            // Pressed / Selected: modal confirm sheet has no pressed/selected visual distinct
            // from Default -- opening the sheet is the interaction itself -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: AlbumTitleConfirmSheet exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: modal sheet has no focus-visual param -- N/A
            StateCell("Focused")
        ),
        content = { AlbumTitleConfirmSheetVariants() }
    ),
    ComponentRegistry.Entry(
        "CardEditorShellContent",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- the full-screen editor demo is gated behind its own "Show"
            // button (its own remember-scoped toggle); Variants below owns that single demo --
            // rendering it again here as "Default" previously created a second, independently
            // triggerable instance -- N/A instead.
            StateCell("Default"),
            // Pressed / Selected: full-screen editor shell demo, gated behind "Show" -- no
            // caller-facing pressed/selected param -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: CardEditorShellContent's own `readOnly` param governs its internal
            // BackHandler/save wiring, not this preview's fixed demo -- N/A
            StateCell("Disabled"),
            // Focused: no focus-visual param exposed -- N/A
            StateCell("Focused")
        ),
        content = { CardEditorShellContentVariants() }
    ),
    ComponentRegistry.Entry(
        "ListCardBottomSheet",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- this modal sheet's only "state" is opening it, which
            // Variants demonstrates via its own "Show sheet" trigger below -- N/A instead of
            // rendering (and independently opening) the same demo twice.
            StateCell("Default"),
            // Pressed / Selected: modal sheet has no pressed/selected visual distinct from
            // Default -- opening the sheet is the interaction itself -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: ListCardBottomSheet exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: modal sheet has no focus-visual param -- N/A
            StateCell("Focused")
        ),
        content = { ListCardBottomSheetVariants() }
    ),
    ComponentRegistry.Entry(
        "RecordingBottomSheetContent",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- RecordingBottomSheetContent wraps its own live
            // ModalBottomSheet internally (via SheetScaffold), so composing it unconditionally
            // here would auto-open a second live sheet instance alongside the state-selector
            // demo in Variants below -- N/A instead.
            StateCell("Default"),
            // Pressed / Selected: RECORDING/PAUSED/TITLE/IDLE state selection is already
            // exercised via the Default preview's own state-selector buttons -- no separate
            // pressed/selected visual distinct from picking one of those states -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: no caller-facing enabled param on this preview beyond the uiState
            // buttons already shown in Default -- N/A
            StateCell("Disabled"),
            // Focused: no focus-visual param exposed -- N/A
            StateCell("Focused")
        ),
        content = { RecordingBottomSheetContentVariants() }
    ),
    ComponentRegistry.Entry(
        "SheetScaffold",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- SheetScaffold always renders a live ModalBottomSheet the
            // instant it is composed (no closed/static appearance exists to preview), so
            // rendering it unconditionally here would auto-open a second live sheet alongside
            // the gated demo in Variants below -- N/A instead.
            StateCell("Default"),
            // Pressed / Selected: generic chrome-only scaffold has no pressed/selected concept
            // of its own -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: SheetScaffold exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: modal sheet chrome has no focus-visual param -- N/A
            StateCell("Focused")
        ),
        content = { SheetScaffoldVariants() }
    ),
    ComponentRegistry.Entry(
        "TagChipEditorContent",
        ExplorerFamilies.SHEETS,
        states = listOf(
            StateCell("Default") {
                WithSheetFeedback {
                    TagChipEditorContent(
                        currentTags = ExplorerFakeData.tagChips.take(2),
                        isLastTag = false,
                        allTags = ExplorerFakeData.tagChips,
                        onRemoveTag = {},
                        onAddTags = {},
                        onRemoveTagNoUndo = {},
                        onCreateTag = {},
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            },
            // Pressed / Selected: isLastTag = true drives the real last-tag warning row
            // (D-03/D-03a in TagChipEditorContent's own doc) -- a genuine, non-fabricated
            // state distinct from Default, curated against the real `isLastTag` param.
            StateCell("Pressed / Selected") {
                WithSheetFeedback {
                    TagChipEditorContent(
                        currentTags = ExplorerFakeData.tagChips.take(1),
                        isLastTag = true,
                        allTags = ExplorerFakeData.tagChips,
                        onRemoveTag = {},
                        onAddTags = {},
                        onRemoveTagNoUndo = {},
                        onCreateTag = {},
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            },
            // Disabled: TagChipEditorContent exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: no focus-visual param exposed -- N/A
            StateCell("Focused")
        ),
        content = { TagChipEditorContentVariants() }
    ),
    ComponentRegistry.Entry(
        "TagPickerSheetContent",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- TagPickerSheetContent is the sheet's body without the
            // ModalBottomSheet wrapper, so (unlike the true modal entries above) it can be
            // composed directly here without triggering a live sheet or duplicating the
            // Variants section's own label -- a genuinely lean, label-free preview.
            StateCell("Default") { TagPickerSheetContentPreview() },
            // Pressed / Selected: embedded picker body has no pressed/selected visual beyond
            // its own internal chip-tap state, which this fixed demo does not drive
            // externally -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: TagPickerSheetContent exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: no focus-visual param exposed -- N/A
            StateCell("Focused")
        ),
        content = { TagPickerSheetContentVariants() }
    ),
    ComponentRegistry.Entry(
        "TextCardBottomSheet",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- this modal sheet's only "state" is opening it, which
            // Variants demonstrates via its own "Show sheet" trigger below -- N/A instead of
            // rendering (and independently opening) the same demo twice.
            StateCell("Default"),
            // Pressed / Selected: modal sheet has no pressed/selected visual distinct from
            // Default -- opening the sheet is the interaction itself -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: TextCardBottomSheet exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: modal sheet has no focus-visual param -- N/A
            StateCell("Focused")
        ),
        content = { TextCardBottomSheetVariants() }
    ),
    // TagPickerSheet (the ModalBottomSheet wrapper around TagPickerSheetContent, above) was
    // NOT in RESEARCH.md's/UI-SPEC.md's gap tables — found only by this plan's live source
    // scan. Registered + rendered as its own Pattern B modal section (Task 2) since it
    // exercises real sheet chrome the already-covered inline TagPickerSheetContent demo does
    // not.
    ComponentRegistry.Entry(
        "TagPickerSheet",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- this modal sheet's only "state" is opening it, which
            // Variants demonstrates via its own "Show sheet" trigger below -- N/A instead of
            // rendering (and independently opening) the same demo twice.
            StateCell("Default"),
            // Pressed / Selected: modal sheet has no pressed/selected visual distinct from
            // Default -- opening the sheet is the interaction itself -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: TagPickerSheet exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: modal sheet has no focus-visual param -- N/A
            StateCell("Focused")
        ),
        content = { TagPickerSheetVariants() }
    ),
    // BulkCreatePopup wraps BulkCreatePopupContent (Dialog chrome + IME padding only,
    // mirrors the TagPickerSheet/TagPickerSheetContent split above) — Task 2 renders one
    // Pattern B section for BulkCreatePopup, which composes BulkCreatePopupContent for real
    // on every demo trigger; both names are registered since both are public exports.
    ComponentRegistry.Entry(
        "BulkCreatePopup",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- this modal dialog's only "state" is opening it, which
            // Variants demonstrates via its own "Show sheet" trigger below -- N/A instead of
            // rendering (and independently opening) the same demo twice.
            StateCell("Default"),
            // Pressed / Selected: modal dialog has no pressed/selected visual distinct from
            // Default -- opening the dialog is the interaction itself -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: BulkCreatePopup exposes no caller-facing enabled param (its internal
            // confirm button is self-gated by blank-name state, not a prop this preview
            // controls) -- N/A
            StateCell("Disabled"),
            // Focused: no focus-visual param exposed -- N/A
            StateCell("Focused")
        ),
        content = { BulkCreatePopupVariants() }
    ),
    // Registered because both BulkCreatePopup and BulkCreatePopupContent are public exports
    // (Pattern B) -- BulkCreatePopupVariants' demo composes BulkCreatePopupContent for real on
    // every trigger, so this entry reuses the same Variants wrapper rather than authoring a
    // second, duplicate demo.
    ComponentRegistry.Entry(
        "BulkCreatePopupContent",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- unlike BulkCreatePopup (its Dialog-wrapping sibling above),
            // BulkCreatePopupContent is the dialog's body without the Dialog wrapper, so it can
            // be composed directly here as a genuinely lean, label-free preview -- no live
            // dialog or duplicated trigger involved.
            StateCell("Default") { BulkCreatePopupContentPreview() },
            StateCell("Pressed / Selected"), // same reasoning as BulkCreatePopup above -- N/A
            StateCell("Disabled"), // same reasoning as BulkCreatePopup above -- N/A
            StateCell("Focused") // same reasoning as BulkCreatePopup above -- N/A
        ),
        content = { BulkCreatePopupVariants() }
    ),
    ComponentRegistry.Entry(
        "NameAndTagsEditor",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- NameAndTagsEditor is composed directly here (no gate/toggle
            // and no ModalBottomSheet wrapper), so it can be shown as a genuinely lean,
            // label-free preview, distinct from the labeled Variants section below.
            StateCell("Default") { NameAndTagsEditorPreview() },
            // Pressed / Selected: embedded editor has no pressed/selected visual of its own
            // beyond internal text-field/tag-chip interaction, not driven externally by this
            // fixed demo -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: NameAndTagsEditor exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: no focus-visual param exposed -- N/A
            StateCell("Focused")
        ),
        content = { NameAndTagsEditorVariants() }
    ),
    // TagCreateSheet/TagCreateSheetContent — same wrapper/body split as above; one Pattern B
    // section for TagCreateSheet exercises both for real.
    ComponentRegistry.Entry(
        "TagCreateSheet",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- this modal sheet's only "state" is opening it, which
            // Variants demonstrates via its own "Show sheet" trigger below -- N/A instead of
            // rendering (and independently opening) the same demo twice.
            StateCell("Default"),
            // Pressed / Selected: modal sheet has no pressed/selected visual distinct from
            // Default -- opening the sheet is the interaction itself -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: TagCreateSheet exposes no caller-facing enabled param (its internal
            // confirm button is self-gated by blank-name state, not a prop this preview
            // controls) -- N/A
            StateCell("Disabled"),
            // Focused: no focus-visual param exposed -- N/A
            StateCell("Focused")
        ),
        content = { TagCreateSheetVariants() }
    ),
    // Registered because both TagCreateSheet and TagCreateSheetContent are public exports
    // (Pattern B) -- TagCreateSheetVariants' demo composes TagCreateSheetContent for real on
    // every trigger, so this entry reuses the same Variants wrapper rather than authoring a
    // second, duplicate demo.
    ComponentRegistry.Entry(
        "TagCreateSheetContent",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- unlike TagCreateSheet (its ModalBottomSheet-wrapping
            // sibling above), TagCreateSheetContent is the sheet's body without the
            // SheetScaffold/ModalBottomSheet wrapper, so it can be composed directly here as a
            // genuinely lean, label-free preview -- no live sheet or duplicated trigger involved.
            StateCell("Default") { TagCreateSheetContentPreview() },
            StateCell("Pressed / Selected"), // same reasoning as TagCreateSheet above -- N/A
            StateCell("Disabled"), // same reasoning as TagCreateSheet above -- N/A
            StateCell("Focused") // same reasoning as TagCreateSheet above -- N/A
        ),
        content = { TagCreateSheetVariants() }
    ),
    ComponentRegistry.Entry(
        "VoiceRenameTagsSheet",
        ExplorerFamilies.SHEETS,
        states = listOf(
            // Default: WR-01 fix -- this modal sheet's only "state" is opening it, which
            // Variants demonstrates via its own "Show sheet" trigger below -- N/A instead of
            // rendering (and independently opening) the same demo twice.
            StateCell("Default"),
            // Pressed / Selected: modal sheet has no pressed/selected visual distinct from
            // Default -- opening the sheet is the interaction itself -- N/A
            StateCell("Pressed / Selected"),
            // Disabled: VoiceRenameTagsSheet exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: modal sheet has no focus-visual param -- N/A
            StateCell("Focused")
        ),
        content = { VoiceRenameTagsSheetVariants() }
    ),
    ComponentRegistry.Entry(
        "ClearableTextField",
        ExplorerFamilies.SHEETS,
        states = listOf(
            StateCell("Default") {
                ClearableTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            },
            // Pressed / Selected: a non-empty value drives the real, caller-visible trailing
            // clear-`x` affordance (ClearableTextField's own gate: `value.isNotEmpty()`) --
            // a genuine, non-fabricated distinction from the empty Default state.
            StateCell("Pressed / Selected") {
                ClearableTextField(
                    value = ExplorerFakeData.SHORT_TITLE,
                    onValueChange = {},
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            },
            // Disabled: ClearableTextField exposes no caller-facing enabled param -- N/A
            StateCell("Disabled"),
            // Focused: no caller-facing focus param exposed (no FocusRequester slot) -- N/A
            StateCell("Focused")
        ),
        content = { ClearableTextFieldVariants() }
    ),
    // EditorItemRow: registered as its own standalone entry per the UI-SPEC default
    // recommendation (not allowlisted) — demonstrates the reorderable list-item row inside a
    // minimal embedded LazyColumn + ReorderableItem scope (Task 2), independent of the real
    // app's ListCardEditorScreen call site.
    ComponentRegistry.Entry(
        "EditorItemRow",
        ExplorerFamilies.SHEETS,
        states = listOf(
            StateCell("Default") { EditorItemRowStatePreview() },
            // Pressed / Selected: isDragging = true drives EditorItemRow's own elevated-shadow
            // drag visual -- a genuine, caller-facing param, not fabricated.
            StateCell("Pressed / Selected") { EditorItemRowStatePreview(isDragging = true) },
            // Disabled: readOnly = true grays out the checkbox/text field and suppresses the
            // swipe affordance (T-11-08) -- a genuine, caller-facing param.
            StateCell("Disabled") { EditorItemRowStatePreview(readOnly = true) },
            // Focused: autoFocus = true requests focus on the row's text field on composition
            // -- a genuine, caller-facing param.
            StateCell("Focused") { EditorItemRowStatePreview(autoFocus = true) }
        ),
        content = { EditorItemRowVariants() }
    )
)

/**
 * Sheets family (SHOW-01, D-04): the real bottom-sheet + shell-chrome composables. Each modal
 * sheet is gated behind its own "Show <name>" button (matching normal call-site usage — showing
 * every ModalBottomSheet unconditionally would stack them on top of one another).
 *
 * Phase 62 Plan 04 (D-01): restructured into a registry-filtered row-list picker — one
 * [ComponentRow] per Sheets entry, each opening that component's own detail page. The former
 * top-level `CompositionLocalProvider(LocalFeedbackController provides ExplorerFeedbackStub)`
 * wrap is no longer needed here (rows have no feedback consumers); [WithSheetFeedback] applies
 * it locally, per demo, so each lifted sheet section still resolves the stub when composed on
 * its own detail page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetsFamilyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    YahirAndroidTasteTheme(themeMode = themeMode) {
        Scaffold(
            topBar = { SheetsFamilyTopBar(onNavigateBack, themeMode, onToggleTheme) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                items(
                    ComponentRegistry.entries.filter { it.family == ExplorerFamilies.SHEETS },
                    key = { it.name }
                ) { entry ->
                    ComponentRow(name = entry.name, onClick = { onNavigateToDetail(entry.name) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetsFamilyTopBar(onNavigateBack: () -> Unit, themeMode: ThemeMode, onToggleTheme: () -> Unit) {
    TopAppBar(
        title = { Text("Sheets") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { ExplorerThemeToggleAction(themeMode = themeMode, onToggleTheme = onToggleTheme) }
    )
}

/**
 * Provides [ExplorerFeedbackStub] locally to a single sheet demo (D-02) — the family screen's
 * body is now a row-list picker with no feedback consumers of its own (unlike the pre-Phase-62
 * gallery, which wrapped the whole body once in [CompositionLocalProvider] at the top level).
 * Each per-component `*Variants()` wrapper below applies this individually so the lifted demo
 * still resolves `LocalFeedbackController` when composed on its own detail page.
 */
@Composable
private fun WithSheetFeedback(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalFeedbackController provides ExplorerFeedbackStub) {
        content()
    }
}


@Composable
private fun ShowSheetButton(label: String, onClick: () -> Unit) {
    SectionLabel(label)
    Button(onClick = onClick, modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Show sheet")
    }
}

@Composable
private fun TextCardSheetSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("TextCardBottomSheet") { show = true }
    if (show) {
        TextCardBottomSheet(
            title = ExplorerFakeData.SHORT_TITLE,
            content = ExplorerFakeData.NOTE_BODY,
            categoryPath = "Notes / Groceries",
            createdAt = ExplorerFakeData.CREATED_AT,
            updatedAt = ExplorerFakeData.UPDATED_AT,
            isPinned = true,
            isFavorite = false,
            onEdit = {},
            onDismiss = { show = false },
            onTogglePin = {},
            onToggleFavorite = {},
            onDelete = {},
            onConfirmRename = {}
        )
    }
}

@Composable
private fun ListCardSheetSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("ListCardBottomSheet") { show = true }
    if (show) {
        ListCardBottomSheet(
            title = ExplorerFakeData.SHORT_TITLE,
            items = ExplorerFakeData.checklistItems,
            subType = "CHECKBOX",
            categoryPath = null,
            createdAt = ExplorerFakeData.CREATED_AT,
            updatedAt = ExplorerFakeData.UPDATED_AT,
            isPinned = false,
            isFavorite = true,
            onToggleItem = {},
            onEdit = {},
            onDismiss = { show = false },
            onTogglePin = {},
            onToggleFavorite = {},
            onConfirmRename = {},
            onDelete = {}
        )
    }
}

@Composable
private fun AlbumTitleConfirmSheetSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("AlbumTitleConfirmSheet") { show = true }
    if (show) {
        AlbumTitleConfirmSheet(
            photoPaths = emptyList(),
            defaultTitle = ExplorerFakeData.SHORT_TITLE,
            onSave = { show = false },
            onDismiss = { show = false }
        )
    }
}

@Composable
private fun AlbumSourcePickerSheetSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("AlbumSourcePickerSheet") { show = true }
    if (show) {
        AlbumSourcePickerSheet(
            onNavigateToCamera = { show = false },
            onNavigateToGallery = { show = false },
            onDismiss = { show = false }
        )
    }
}

@Composable
private fun TagPickerContentSection() {
    SectionLabel("TagPickerSheetContent — embedded inline")
    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        TagPickerSheetContent(
            allTags = ExplorerFakeData.tagChips,
            onDone = {},
            onCreate = {},
            onDismiss = {}
        )
    }
}

@Composable
private fun TagChipEditorSection() {
    SectionLabel("TagChipEditorContent")
    TagChipEditorContent(
        currentTags = ExplorerFakeData.tagChips.take(1),
        isLastTag = true,
        allTags = ExplorerFakeData.tagChips,
        onRemoveTag = {},
        onAddTags = {},
        onRemoveTagNoUndo = {},
        onCreateTag = {},
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun CardEditorShellSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("CardEditorShellContent") { show = true }
    if (show) {
        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            CardEditorShellContent(
                accentColor = 0xFF6750A4L,
                onSave = {},
                onNavigateBack = { show = false },
                content = { Text("Editor content slot") }
            )
        }
    }
}

@Composable
private fun RecordingSheetSection() {
    var uiState by remember { mutableStateOf<RecordingSheetUiState?>(null) }
    SectionLabel("RecordingBottomSheetContent — RECORDING / PAUSED / TITLE / IDLE")
    RecordingStateButtons(onSelect = { uiState = it })
    val state = uiState
    if (state != null) {
        RecordingBottomSheetContent(
            uiState = state,
            elapsedSeconds = 42L,
            amplitudeBars = if (state == RecordingSheetUiState.IDLE) emptyList() else ExplorerFakeData.waveformBars,
            titleText = "",
            defaultTitle = ExplorerFakeData.SHORT_TITLE,
            permissionDenied = false,
            onTitleChange = {},
            onPause = {},
            onResume = {},
            onStop = {},
            onSave = { uiState = null },
            onDiscard = { uiState = null },
            onDismissRequest = { uiState = null }
        )
    }
}

@Composable
private fun RecordingStateButtons(onSelect: (RecordingSheetUiState) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Button(onClick = { onSelect(RecordingSheetUiState.RECORDING) }) { Text("Show RECORDING") }
        Button(onClick = { onSelect(RecordingSheetUiState.PAUSED) }) { Text("Show PAUSED") }
        Button(onClick = { onSelect(RecordingSheetUiState.TITLE) }) { Text("Show TITLE") }
        Button(onClick = { onSelect(RecordingSheetUiState.IDLE) }) { Text("Show IDLE") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetScaffoldSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("SheetScaffold — generic chrome") { show = true }
    if (show) {
        SheetScaffold(onDismissRequest = { show = false }) {
            Text(
                text = "Generic SheetScaffold content",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// --- Coverage-refresh additions (CATALOG-02) -----------------------------------------------

// TagPickerSheet (the ModalBottomSheet wrapper) — found only by this plan's live source scan,
// not in RESEARCH.md's/UI-SPEC.md's gap tables. TagPickerSheetContent (its body) is already
// demoed inline above (TagPickerContentSection); this section exercises the real sheet chrome
// that inline demo does not.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagPickerSheetSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("TagPickerSheet") { show = true }
    if (show) {
        TagPickerSheet(
            allTags = ExplorerFakeData.tagChips,
            onDone = {},
            onCreate = {},
            onDismiss = { show = false }
        )
    }
}

// Registry: also covers BulkCreatePopupContent (Pattern B) — this section exercises it for real
// via BulkCreatePopup's composition, so it has no dedicated section of its own.
@Composable
private fun BulkCreatePopupSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("BulkCreatePopup") { show = true }
    if (show) {
        BulkCreatePopup(
            onDismissRequest = { show = false },
            actionLabel = "Create",
            actionAndMoreLabel = "Create and more",
            onAction = {}
        )
    }
}

@Composable
private fun NameAndTagsEditorSection() {
    SectionLabel("NameAndTagsEditor — embedded inline")
    var name by remember { mutableStateOf(ExplorerFakeData.SHORT_TITLE) }
    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        NameAndTagsEditor(
            name = name,
            onNameChange = { name = it },
            nameLabel = "Name",
            tagsContent = {
                TagChipEditorContent(
                    currentTags = ExplorerFakeData.tagChips.take(1),
                    isLastTag = true,
                    allTags = ExplorerFakeData.tagChips,
                    onRemoveTag = {},
                    onAddTags = {},
                    onRemoveTagNoUndo = {},
                    onCreateTag = {}
                )
            },
            onSave = {},
            onDismiss = {},
            saveLabel = "Save"
        )
    }
}

// Registry: also covers TagCreateSheetContent (Pattern B) — this section exercises it for real
// via TagCreateSheet's composition, so it has no dedicated section of its own.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagCreateSheetSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("TagCreateSheet") { show = true }
    if (show) {
        TagCreateSheet(
            onDismiss = { show = false },
            onConfirm = { _, _, _, _ -> show = false },
            showAppearanceOption = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceRenameTagsSheetSection() {
    var show by remember { mutableStateOf(false) }
    ShowSheetButton("VoiceRenameTagsSheet") { show = true }
    if (show) {
        VoiceRenameTagsSheet(
            defaultTitle = ExplorerFakeData.SHORT_TITLE,
            onSave = { show = false },
            onDismiss = { show = false },
            tagContent = {
                TagChipEditorContent(
                    currentTags = ExplorerFakeData.tagChips.take(1),
                    isLastTag = true,
                    allTags = ExplorerFakeData.tagChips,
                    onRemoveTag = {},
                    onAddTags = {},
                    onRemoveTagNoUndo = {},
                    onCreateTag = {}
                )
            }
        )
    }
}

@Composable
private fun ClearableTextFieldSection() {
    SectionLabel("ClearableTextField")
    var text by remember { mutableStateOf(ExplorerFakeData.SHORT_TITLE) }
    ClearableTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Name") },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

// EditorItemRow requires a ReorderableCollectionItemScope receiver (sh.calvin.reorderable) —
// this minimal LazyColumn + ReorderableItem wrapper is the smallest real host that provides it,
// mirroring app/.../feature/cardeditor/ListCardEditorScreen.kt's own call-site shape.
@Composable
private fun EditorItemRowSection() {
    SectionLabel("EditorItemRow — reorderable list-item row")
    var items by remember { mutableStateOf(ExplorerFakeData.checklistItems) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        items = items.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }
    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->
                    EditorItemRow(
                        item = item,
                        itemIndex = index,
                        isDragging = isDragging,
                        autoFocus = false,
                        onAutoFocused = {},
                        dragHandleModifier = Modifier.draggableHandle(),
                        subType = "CHECKBOX",
                        onToggleChecked = {},
                        onTextChange = {},
                        onDelete = {},
                        onEnterPressed = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// --- Task 1 (batch A): Variants wrappers reusing the existing sections verbatim ------------

@Composable
private fun TextCardBottomSheetVariants() {
    WithSheetFeedback { TextCardSheetSection() }
}

@Composable
private fun ListCardBottomSheetVariants() {
    WithSheetFeedback { ListCardSheetSection() }
}

@Composable
private fun AlbumTitleConfirmSheetVariants() {
    WithSheetFeedback { AlbumTitleConfirmSheetSection() }
}

@Composable
private fun AlbumSourcePickerSheetVariants() {
    WithSheetFeedback { AlbumSourcePickerSheetSection() }
}

@Composable
private fun TagPickerSheetContentVariants() {
    WithSheetFeedback { TagPickerContentSection() }
}

@Composable
private fun TagPickerSheetVariants() {
    WithSheetFeedback { TagPickerSheetSection() }
}

// --- Task 2 (batch B): Variants wrappers + wrapper/content pairs ---------------------------

@Composable
private fun TagChipEditorContentVariants() {
    WithSheetFeedback { TagChipEditorSection() }
}

@Composable
private fun CardEditorShellContentVariants() {
    WithSheetFeedback { CardEditorShellSection() }
}

@Composable
private fun RecordingBottomSheetContentVariants() {
    WithSheetFeedback { RecordingSheetSection() }
}

@Composable
private fun SheetScaffoldVariants() {
    WithSheetFeedback { SheetScaffoldSection() }
}

@Composable
private fun BulkCreatePopupVariants() {
    WithSheetFeedback { BulkCreatePopupSection() }
}

@Composable
private fun NameAndTagsEditorVariants() {
    WithSheetFeedback { NameAndTagsEditorSection() }
}

// --- Task 3 (batch C): Variants wrappers + wrapper/content pair -----------------------------

@Composable
private fun TagCreateSheetVariants() {
    WithSheetFeedback { TagCreateSheetSection() }
}

@Composable
private fun VoiceRenameTagsSheetVariants() {
    WithSheetFeedback { VoiceRenameTagsSheetSection() }
}

@Composable
private fun ClearableTextFieldVariants() {
    WithSheetFeedback { ClearableTextFieldSection() }
}

@Composable
private fun EditorItemRowVariants() {
    WithSheetFeedback { EditorItemRowSection() }
}

// --- WR-01 fix: lean, label-free Default-cell previews -------------------------------------
// Consumed only by the States-matrix "Default" cells above. Unlike the modal/gated Sheets
// entries (whose Default cell is N/A -- see the states = listOf(...) comments), these four
// components' bodies are composable directly without a ModalBottomSheet/Dialog wrapper or a
// "Show sheet" gate, so a genuinely distinct, unlabeled preview is possible here (mirroring the
// XxxPreview pattern Cards/Chips/Buttons-FAB/Pickers/Feedback/Empty State already use).

@Composable
private fun TagPickerSheetContentPreview() {
    WithSheetFeedback {
        Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            TagPickerSheetContent(
                allTags = ExplorerFakeData.tagChips,
                onDone = {},
                onCreate = {},
                onDismiss = {}
            )
        }
    }
}

@Composable
private fun BulkCreatePopupContentPreview() {
    WithSheetFeedback {
        BulkCreatePopupContent(
            actionLabel = "Create",
            actionAndMoreLabel = "Create and more",
            onAction = {},
            onDismissRequest = {}
        )
    }
}

@Composable
private fun NameAndTagsEditorPreview() {
    WithSheetFeedback {
        var name by remember { mutableStateOf(ExplorerFakeData.SHORT_TITLE) }
        Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            NameAndTagsEditor(
                name = name,
                onNameChange = { name = it },
                nameLabel = "Name",
                tagsContent = {
                    TagChipEditorContent(
                        currentTags = ExplorerFakeData.tagChips.take(1),
                        isLastTag = true,
                        allTags = ExplorerFakeData.tagChips,
                        onRemoveTag = {},
                        onAddTags = {},
                        onRemoveTagNoUndo = {},
                        onCreateTag = {}
                    )
                },
                onSave = {},
                onDismiss = {},
                saveLabel = "Save"
            )
        }
    }
}

@Composable
private fun TagCreateSheetContentPreview() {
    WithSheetFeedback {
        TagCreateSheetContent(
            onConfirm = { _, _, _, _ -> },
            showAppearanceOption = true
        )
    }
}

/**
 * Single-row `EditorItemRow` state preview (D-03) — a minimal `LazyColumn` + `ReorderableItem`
 * host (same shape as [EditorItemRowSection], sized to one row) so each States-matrix cell can
 * curate a genuinely distinct, caller-facing param combination (`isDragging`/`readOnly`/
 * `autoFocus`) without duplicating the full 3-item reorderable demo already preserved in
 * [EditorItemRowVariants].
 */
@Composable
private fun EditorItemRowStatePreview(
    isDragging: Boolean = false,
    readOnly: Boolean = false,
    autoFocus: Boolean = false
) {
    val item = ExplorerFakeData.checklistItems.first()
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { _, _ -> }
    Box(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
            itemsIndexed(listOf(item), key = { _, row -> row.id }) { index, row ->
                ReorderableItem(reorderableLazyListState, key = row.id) { dragging ->
                    EditorItemRow(
                        item = row,
                        itemIndex = index,
                        isDragging = isDragging || dragging,
                        autoFocus = autoFocus,
                        onAutoFocused = {},
                        dragHandleModifier = Modifier.draggableHandle(),
                        subType = "CHECKBOX",
                        onToggleChecked = {},
                        onTextChange = {},
                        onDelete = {},
                        onEnterPressed = {},
                        readOnly = readOnly,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
