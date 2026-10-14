package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import io.github.ygaray.yahirandroidtaste.component.ActionButtonEmphasis
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheet
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheetStyle
import io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard
import io.github.ygaray.yahirandroidtaste.component.ProviderKeyDefaults
import io.github.ygaray.yahirandroidtaste.component.ReceiptRow
import io.github.ygaray.yahirandroidtaste.component.SegmentedOptionSelector
import io.github.ygaray.yahirandroidtaste.component.SegmentedOptionStyle
import io.github.ygaray.yahirandroidtaste.component.ToneCard
import io.github.ygaray.yahirandroidtaste.feedback.UndoCenterScreen
import io.github.ygaray.yahirandroidtaste.feedback.UndoGroupRowUiModel
import io.github.ygaray.yahirandroidtaste.feedback.UndoGroupStatus
import io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryEntry
import io.github.ygaray.yahirandroidtaste.feedback.UndoStatus
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyLabels
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel
import io.github.ygaray.yahirandroidtaste.model.SheetActionUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoAffordanceUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ContentTone
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.ToneDefaults
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme

/*
 * v2.6.0 release demos (Phase 24, DEMO-01). Explorer-only: this package is drift-guard denylisted,
 * nothing here is registered, and the DEMO_TAG_* strings are explorer-only demo hooks that the
 * device walkthrough drives. They are NOT library test tags and never go into API.md. All copy is
 * domain-neutral (Item A/B/C, Provider A/B/C, Batch edit) and no demo renders typed key text: the
 * saved-key fingerprint keeps only the last four characters (T-24-01).
 */

internal const val DEMO_TAG_KEY_LIFECYCLE = "demo_key_lifecycle"
internal const val DEMO_TAG_KEY_LIFECYCLE_GERMAN = "demo_key_lifecycle_german"
internal const val DEMO_TAG_UNDO_GROUPS = "demo_undo_groups"
internal const val DEMO_TAG_UNDO_GROUPS_GERMAN = "demo_undo_groups_german"
internal const val DEMO_TAG_COUNT_EDITOR = "demo_count_editor"
internal const val DEMO_TAG_MARK_CHECKED = "demo_mark_checked"

// --- ProviderKeyCard key lifecycle ---------------------------------------------------------------

/** Three neutral providers; the second one starts out with a saved key. */
private val DEMO_PROVIDERS = listOf(
    ProviderOptionUiModel(id = "alpha", label = "Provider A"),
    ProviderOptionUiModel(id = "beta", label = "Provider B"),
    ProviderOptionUiModel(id = "gamma", label = "Provider C")
)

/** Display-safe fingerprint of the initially saved key (the ellipsis is U+2026). */
private const val DEMO_INITIAL_FINGERPRINT = "key-…c3d4"

private val DEMO_GERMAN_KEY_LABELS = ProviderKeyDefaults.labels(
    save = "Speichern",
    update = "Aktualisieren",
    clear = "Entfernen",
    cancel = "Abbrechen",
    savedPrefix = "Gespeichert",
    savedMarker = "gespeichert"
)

/**
 * The English and the German key lifecycle, each a real stateful [ProviderKeyCard]: three providers,
 * one saved, with the edit (Update, Cancel, Save) and clear flows driven by demo state.
 */
@Composable
internal fun ProviderKeyLifecycleDemos() {
    SectionLabel("ProviderKeyCard — key lifecycle (3 providers, one saved; edit and clear flows)")
    ProviderKeyLifecycleDemo(
        labels = ProviderKeyDefaults.labels(),
        providerLabel = "Provider",
        keyLabel = "API key",
        tag = DEMO_TAG_KEY_LIFECYCLE
    )
    SectionLabel("ProviderKeyCard — key lifecycle, German labels")
    ProviderKeyLifecycleDemo(
        labels = DEMO_GERMAN_KEY_LABELS,
        providerLabel = "Anbieter",
        keyLabel = "Zugangsschlüssel",
        tag = DEMO_TAG_KEY_LIFECYCLE_GERMAN
    )
}

@Composable
private fun ProviderKeyLifecycleDemo(
    labels: ProviderKeyLabels,
    providerLabel: String,
    keyLabel: String,
    tag: String
) {
    var selectedId by remember { mutableStateOf("beta") }
    var savedKeys by remember { mutableStateOf(mapOf("beta" to SavedKeyUiModel(DEMO_INITIAL_FINGERPRINT))) }
    var typed by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(false) }
    val actions = ProviderKeyActions(
        onSave = {
            // Only the last four characters are kept, so the typed text never reaches the saved map.
            savedKeys = savedKeys + (selectedId to SavedKeyUiModel("key-…" + typed.trim().takeLast(4)))
            typed = ""
            editing = false
        },
        saveEnabled = typed.isNotBlank(),
        onUpdate = {
            editing = true
            typed = ""
        },
        onClear = {
            savedKeys = savedKeys - selectedId
            typed = ""
            editing = false
        },
        onCancelEdit = if (editing) {
            {
                editing = false
                typed = ""
            }
        } else {
            null
        }
    )
    Box(modifier = Modifier.testTag(tag)) {
        ProviderKeyCard(
            providers = DEMO_PROVIDERS,
            selectedProviderId = selectedId,
            onProviderSelected = {
                // The library KDoc tells consumers to reset the typed text and the edit flag on a switch.
                selectedId = it
                typed = ""
                editing = false
            },
            keyValue = typed,
            onKeyChange = { typed = it },
            keyState = if (typed.isBlank()) KeyFieldState.Empty else KeyFieldState.Entered,
            keyLabel = keyLabel,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            providerLabel = providerLabel,
            savedKeys = savedKeys,
            editingSavedKey = editing,
            actions = actions,
            labels = labels
        )
    }
}

// --- Undo Center group rows ----------------------------------------------------------------------

private const val DEMO_GROUP_BATCH = "demo-batch"
private const val DEMO_GROUP_EARLIER = "demo-earlier"
private const val GERMAN_UNDO_ALL = "Alle rückgängig machen"
private const val GERMAN_GROUP_LABEL = "Sammelbearbeitung der Positionen aus der letzten Sitzung"

private val DEMO_GROUP_MEMBERS = mapOf(
    DEMO_GROUP_BATCH to listOf("demo-g1-a", "demo-g1-b", "demo-g1-c"),
    DEMO_GROUP_EARLIER to listOf("demo-g2-a", "demo-g2-b")
)

/**
 * The English and the German Undo Center with one open group, one resolved group and one flat row. The
 * German demo also localizes the header count and status words (v2.7.1).
 */
@Composable
internal fun UndoGroupsDemos() {
    SectionLabel("UndoCenterScreen — group rows (Undo all, indented members)")
    UndoGroupsDemo(undoAllLabel = "Undo all", batchLabel = "Batch edit", tag = DEMO_TAG_UNDO_GROUPS)
    SectionLabel("UndoCenterScreen — group rows, German labels")
    UndoGroupsDemo(
        undoAllLabel = GERMAN_UNDO_ALL,
        batchLabel = GERMAN_GROUP_LABEL,
        tag = DEMO_TAG_UNDO_GROUPS_GERMAN,
        localize = ::germanGroupWords
    )
}

@Composable
private fun UndoGroupsDemo(
    undoAllLabel: String,
    batchLabel: String,
    tag: String,
    localize: (UndoGroupRowUiModel) -> UndoGroupRowUiModel = { it }
) {
    var entries by remember { mutableStateOf(demoUndoEntries()) }
    val groups = listOf(
        demoGroup(DEMO_GROUP_BATCH, batchLabel, undoAllLabel, entries),
        demoGroup(DEMO_GROUP_EARLIER, "Earlier batch", undoAllLabel, entries)
    ).map(localize)
    // Tall enough that every row is composed in the lazy list: 3 + 1 + 2 members and two headers.
    Box(modifier = Modifier.fillMaxWidth().height(680.dp).testTag(tag)) {
        UndoCenterScreen(
            entries = entries,
            onNavigateBack = {},
            onUndo = { id -> entries = entries.map { if (it.id == id) it.withStatus(UndoStatus.Undone) else it } },
            groups = groups,
            onUndoGroup = { groupId ->
                val members = DEMO_GROUP_MEMBERS.getValue(groupId)
                entries = entries.map {
                    if (it.id in members && it.status == UndoStatus.Available) it.withStatus(UndoStatus.Undone) else it
                }
            },
            onClear = { entries = entries.filter { it.status == UndoStatus.Available } }
        )
    }
}

/** The German count (with its own plural rule) and status words a consumer would pass. */
private fun germanGroupWords(group: UndoGroupRowUiModel) = group.copy(
    countLabel = { count -> if (count == 1) "1 Eintrag" else "$count Einträge" },
    undoableLabel = "Rückgängig möglich",
    partlyResolvedLabel = "Teilweise erledigt",
    resolvedLabel = "Erledigt"
)

private fun demoUndoEntries(): List<UndoHistoryEntry> {
    val now = System.currentTimeMillis()
    fun entry(id: String, message: String, status: UndoStatus = UndoStatus.Available) =
        UndoHistoryEntry(id = id, message = message, timestamp = now, undoAction = {}, status = status)
    return listOf(
        entry("demo-g1-a", "Changed item A"),
        entry("demo-g1-b", "Changed item B"),
        entry("demo-g1-c", "Changed item C"),
        entry("demo-solo", "Renamed item D"),
        entry("demo-g2-a", "Changed item E", UndoStatus.Undone),
        entry("demo-g2-b", "Changed item F", UndoStatus.Undone)
    )
}

/** Builds a group projection the way a consumer would: status derived from the members present. */
private fun demoGroup(
    groupId: String,
    label: String,
    undoAllLabel: String,
    entries: List<UndoHistoryEntry>
): UndoGroupRowUiModel {
    val memberIds = DEMO_GROUP_MEMBERS.getValue(groupId)
    val members = entries.filter { it.id in memberIds }
    val status = when {
        members.isEmpty() -> UndoGroupStatus.Empty
        members.none { it.status == UndoStatus.Available } -> UndoGroupStatus.FullyResolved
        members.all { it.status == UndoStatus.Available } -> UndoGroupStatus.Undoable
        else -> UndoGroupStatus.PartiallyResolved
    }
    return UndoGroupRowUiModel(
        groupId = groupId,
        label = label,
        memberIds = memberIds,
        status = status,
        undoAllLabel = undoAllLabel
    )
}

// --- Hub static scheme -----------------------------------------------------------------------------

/**
 * Re-wraps [content] in the hub's static palette ([YahirAndroidTasteTheme] with `dynamicColor = false`)
 * for the current light or dark mode, decided from the active scheme's surface luminance. The Receipt
 * and tone demos use it so the warn tone shows the hub amber the owner reviews instead of the device
 * wallpaper tone (uat-pending/20 item 2, an owner-flagged decision). Every other Explorer page keeps
 * dynamic color; no global theme changes.
 */
@Composable
internal fun HubSchemeScope(content: @Composable () -> Unit) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < DARK_SURFACE_LUMINANCE
    YahirAndroidTasteTheme(
        themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT,
        dynamicColor = false,
        content = content
    )
}

private const val DARK_SURFACE_LUMINANCE = 0.5f

// --- Count editor ------------------------------------------------------------------------------------

/**
 * A count editor: a Pill [SegmentedOptionSelector] with the second option disabled, and a warn
 * [ToneCard] explaining why. [SegmentedOptionSelector] takes exactly two options, so the editor offers
 * Few and Many with Many unavailable. State lives here, never in the control.
 */
@Composable
internal fun CountEditorDemo() {
    var selected by remember { mutableIntStateOf(0) }
    HubSchemeScope {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag(DEMO_TAG_COUNT_EDITOR)) {
            Text("Count", style = MaterialTheme.typography.labelLarge)
            SegmentedOptionSelector(
                selectedIndex = selected,
                options = listOf("Few", "Many"),
                onSelect = { selected = it },
                modifier = Modifier.padding(vertical = 8.dp),
                disabledOptions = setOf(1),
                style = SegmentedOptionStyle.Pill
            )
            ToneCard(tone = ToneDefaults.warn(), modifier = Modifier.fillMaxWidth()) {
                Text("“Many” is unavailable for this selection.", modifier = Modifier.padding(12.dp))
            }
        }
    }
}

// --- Receipt OutcomeSheet demos ----------------------------------------------------------------------

/** One trigger per Receipt sheet demo; [label] and [tag] are the contract the device walkthrough drives. */
internal enum class ReceiptDemo(val label: String, val tag: String) {
    ConfirmSingleOpen(
        "OutcomeSheet Receipt — Confirm, single item open",
        "demo_receipt_confirm_single_open"
    ),
    ConfirmBatchCollapsed(
        "OutcomeSheet Receipt — Confirm, batch collapsed",
        "demo_receipt_confirm_batch_collapsed"
    ),
    ConfirmBatchAttention(
        "OutcomeSheet Receipt — Confirm, batch with an attention row",
        "demo_receipt_confirm_batch_attention"
    ),
    ConfirmGerman(
        "OutcomeSheet Receipt — Confirm, batch, German labels",
        "demo_receipt_confirm_german"
    ),
    SuccessSingle("OutcomeSheet Receipt — Success, single", "demo_receipt_success_single"),
    SuccessBatch("OutcomeSheet Receipt — Success, batch", "demo_receipt_success_batch"),
    SuccessAllUndone("OutcomeSheet Receipt — Success, all undone", "demo_receipt_success_all_undone"),
    FailureError("OutcomeSheet Receipt — Failure, error", "demo_receipt_failure_error"),
    FailureWarn("OutcomeSheet Receipt — Failure, warn", "demo_receipt_failure_warn")
}

/** The single-open accordion index and the attention flow's flag. Held by the demo, never by a row. */
@Stable
internal class ReceiptDemoState {
    var openIndex by mutableStateOf<Int?>(null)
    var checkedB by mutableStateOf(false)

    fun toggle(index: Int) {
        openIndex = if (openIndex == index) null else index
    }

    fun reset(demo: ReceiptDemo) {
        openIndex = if (demo == ReceiptDemo.ConfirmSingleOpen) 0 else null
        checkedB = false
    }
}

private val GERMAN_ROW_TITLES = listOf(
    "Sehr lange deutsche Positionsbezeichnung Nummer eins",
    "Sehr lange deutsche Positionsbezeichnung Nummer zwei",
    "Sehr lange deutsche Positionsbezeichnung Nummer drei"
)

/**
 * Nine Receipt-style [OutcomeSheet] demos (Confirm x4, Success x3, Failure x2). One hoisted slot
 * composes at most one sheet at a time (WR-01) and the modal scrim blocks opening a second.
 */
@Composable
internal fun ReceiptOutcomeSheetDemos() {
    var shown by remember { mutableStateOf<ReceiptDemo?>(null) }
    val state = remember { ReceiptDemoState() }
    ReceiptDemo.entries.forEach { demo ->
        SectionLabel(demo.label)
        Button(
            onClick = {
                state.reset(demo)
                shown = demo
            },
            modifier = Modifier.padding(horizontal = 16.dp).testTag(demo.tag)
        ) { Text("Show sheet") }
    }
    shown?.let { demo ->
        HubSchemeScope {
            OutcomeSheet(
                outcome = receiptOutcome(demo, state) { shown = null },
                onDismissRequest = { shown = null },
                style = OutcomeSheetStyle.Receipt
            )
        }
    }
}

/** The outcome of [demo], built fresh on every composition so the demo state is read live. */
internal fun receiptOutcome(demo: ReceiptDemo, state: ReceiptDemoState, dismiss: () -> Unit): VoiceOutcomeUiState =
    when (demo) {
        ReceiptDemo.ConfirmSingleOpen,
        ReceiptDemo.ConfirmBatchCollapsed,
        ReceiptDemo.ConfirmBatchAttention,
        ReceiptDemo.ConfirmGerman -> receiptConfirmOutcome(demo, state, dismiss)
        ReceiptDemo.SuccessSingle -> receiptSuccessSingleOutcome(dismiss)
        ReceiptDemo.SuccessBatch -> receiptSuccessBatchOutcome(dismiss)
        ReceiptDemo.SuccessAllUndone -> receiptSuccessAllUndoneOutcome(dismiss)
        ReceiptDemo.FailureError -> receiptFailureErrorOutcome(dismiss)
        ReceiptDemo.FailureWarn -> receiptFailureWarnOutcome(dismiss)
    }

/**
 * Receipt confirm: `items` stays empty and the rows live in `topLevelContent` (sketch Y-4). The
 * attention demo starts with Confirm disabled until row B is marked checked.
 */
internal fun receiptConfirmOutcome(
    demo: ReceiptDemo,
    state: ReceiptDemoState,
    dismiss: () -> Unit
): VoiceOutcomeUiState.NeedsConfirmation {
    val german = demo == ReceiptDemo.ConfirmGerman
    return VoiceOutcomeUiState.NeedsConfirmation(
        reason = if (german) "Sie sagten: „den ersten Eintrag auf zwei ändern“" else "You said: “update the first item to two”",
        items = emptyList(),
        title = if (german) "Änderungen bestätigen?" else "Confirm changes?",
        severity = ActionButtonDefaults.ActionButtonRole.Save,
        confirmLabel = if (german) "Alle bestätigen (3)" else "Confirm",
        cancelLabel = if (german) "Abbrechen" else "Cancel",
        topLevelContent = { ReceiptConfirmRows(demo, state) },
        confirmEnabled = demo != ReceiptDemo.ConfirmBatchAttention || state.checkedB,
        onConfirm = dismiss,
        onCancel = dismiss
    )
}

@Composable
private fun ReceiptConfirmRows(demo: ReceiptDemo, state: ReceiptDemoState) {
    val german = demo == ReceiptDemo.ConfirmGerman
    val titles = when {
        demo == ReceiptDemo.ConfirmSingleOpen -> listOf("Item A")
        german -> GERMAN_ROW_TITLES
        else -> listOf("Item A", "Item B", "Item C")
    }
    val values = listOf("2", "1", "3")
    Column(modifier = Modifier.fillMaxWidth()) {
        titles.forEachIndexed { index, title ->
            val needsCheck = demo == ReceiptDemo.ConfirmBatchAttention && index == 1 && !state.checkedB
            ReceiptRow(
                title = title,
                subtitle = if (german) "Detailzeile" else "Detail line",
                subtitleTone = if (needsCheck) ContentTone.Warn else ContentTone.Muted,
                value = values[index],
                valueCaption = if (german) "Anzahl" else "count",
                valueTone = if (needsCheck) ContentTone.Warn else ContentTone.Default,
                attentionCue = if (needsCheck) "Check this one" else null,
                expanded = state.openIndex == index,
                onClick = { state.toggle(index) },
                onClickLabel = if (german) "Details anzeigen" else "Show details",
                expandedContent = { ReceiptRowDetail(demo, index, title, state) }
            )
        }
    }
}

@Composable
private fun ReceiptRowDetail(demo: ReceiptDemo, index: Int, title: String, state: ReceiptDemoState) {
    when {
        demo == ReceiptDemo.ConfirmSingleOpen -> CountEditorDemo()
        demo == ReceiptDemo.ConfirmBatchAttention && index == 1 -> Button(
            onClick = { state.checkedB = true },
            modifier = Modifier.testTag(DEMO_TAG_MARK_CHECKED)
        ) { Text("Mark checked") }
        demo == ReceiptDemo.ConfirmGerman -> Text("Weitere Angaben zu Position ${index + 1}")
        else -> Text("More about $title")
    }
}

private fun doneAction(dismiss: () -> Unit) = SheetActionUiModel("Done", dismiss, ActionButtonEmphasis.Filled)

internal fun receiptSuccessSingleOutcome(dismiss: () -> Unit): VoiceOutcomeUiState.Success =
    VoiceOutcomeUiState.Success(
        summary = "Saved 1 item",
        handledBy = HandledByUiModel(tier = "Local grammar", provider = "offline", onDevice = true),
        badge = OutcomeBadge.Success,
        endAction = doneAction(dismiss)
    )

internal fun receiptSuccessBatchOutcome(dismiss: () -> Unit): VoiceOutcomeUiState.Success =
    VoiceOutcomeUiState.Success(
        summary = "Saved 2 of 3 items",
        batchResults = listOf(
            BatchRowResultUiModel(label = "Item A", succeeded = true),
            BatchRowResultUiModel(label = "Item B", succeeded = true),
            BatchRowResultUiModel(label = "Item C", succeeded = false, detail = "could not be saved")
        ),
        badge = OutcomeBadge.Success,
        startAction = SheetActionUiModel("Edit", {}, ActionButtonEmphasis.Text),
        endAction = doneAction(dismiss)
    )

internal fun receiptSuccessAllUndoneOutcome(dismiss: () -> Unit): VoiceOutcomeUiState.Success =
    VoiceOutcomeUiState.Success(
        summary = "Reverted 2 items",
        undo = UndoAffordanceUiModel(
            allLabel = "Undo all (0)",
            rows = listOf(
                UndoRowUiModel(id = "undo-a", label = "Item A", state = UndoRowState.Undone),
                UndoRowUiModel(id = "undo-b", label = "Item B", state = UndoRowState.Undone)
            ),
            onUndoAll = null
        ),
        badge = OutcomeBadge.Reverted,
        endAction = doneAction(dismiss)
    )

internal fun receiptFailureErrorOutcome(dismiss: () -> Unit): VoiceOutcomeUiState.Failure =
    receiptFailure(
        reason = "The request could not be completed",
        title = "Couldn't complete",
        tone = FailureTone.Error,
        badge = OutcomeBadge.Error,
        dismiss = dismiss
    )

internal fun receiptFailureWarnOutcome(dismiss: () -> Unit): VoiceOutcomeUiState.Failure =
    receiptFailure(
        reason = "Only part of the request was understood",
        title = "Needs a closer look",
        tone = FailureTone.Warn,
        badge = OutcomeBadge.Warn,
        dismiss = dismiss
    )

private fun receiptFailure(
    reason: String,
    title: String,
    tone: FailureTone,
    badge: OutcomeBadge,
    dismiss: () -> Unit
): VoiceOutcomeUiState.Failure = VoiceOutcomeUiState.Failure(
    reason = reason,
    action = FailureActionUiModel(label = "Retry", onClick = dismiss, icon = Icons.Filled.Refresh),
    title = title,
    transcript = "You said: “update the first item”",
    tone = tone,
    tip = { Text("Check the connection and try again.") },
    secondaryAction = SheetActionUiModel("Dismiss", dismiss, ActionButtonEmphasis.Text),
    badge = badge
)
