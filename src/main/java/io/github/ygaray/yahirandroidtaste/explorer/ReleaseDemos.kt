package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard
import io.github.ygaray.yahirandroidtaste.component.ProviderKeyDefaults
import io.github.ygaray.yahirandroidtaste.feedback.UndoCenterScreen
import io.github.ygaray.yahirandroidtaste.feedback.UndoGroupRowUiModel
import io.github.ygaray.yahirandroidtaste.feedback.UndoGroupStatus
import io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryEntry
import io.github.ygaray.yahirandroidtaste.feedback.UndoStatus
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyLabels
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel

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

/** The English and the German Undo Center with one open group, one resolved group and one flat row. */
@Composable
internal fun UndoGroupsDemos() {
    SectionLabel("UndoCenterScreen — group rows (Undo all, indented members)")
    UndoGroupsDemo(undoAllLabel = "Undo all", batchLabel = "Batch edit", tag = DEMO_TAG_UNDO_GROUPS)
    SectionLabel("UndoCenterScreen — group rows, German Undo all")
    UndoGroupsDemo(
        undoAllLabel = GERMAN_UNDO_ALL,
        batchLabel = GERMAN_GROUP_LABEL,
        tag = DEMO_TAG_UNDO_GROUPS_GERMAN
    )
}

@Composable
private fun UndoGroupsDemo(undoAllLabel: String, batchLabel: String, tag: String) {
    var entries by remember { mutableStateOf(demoUndoEntries()) }
    val groups = listOf(
        demoGroup(DEMO_GROUP_BATCH, batchLabel, undoAllLabel, entries),
        demoGroup(DEMO_GROUP_EARLIER, "Earlier batch", undoAllLabel, entries)
    )
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
