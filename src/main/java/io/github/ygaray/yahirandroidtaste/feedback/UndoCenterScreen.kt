package io.github.ygaray.yahirandroidtaste.feedback

import android.text.format.DateUtils
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.ygaray.yahirandroidtaste.component.EmptyState
import io.github.ygaray.yahirandroidtaste.icon.cardTypeIcon
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import java.io.File
import kotlinx.coroutines.delay

/**
 * Undo Center — pure `:designsystem` composable (UNDO-03). Displays session-durable
 * [UndoHistoryEntry] state from [UndoHistoryStore], newest-first. Takes store state and
 * callbacks as parameters only — no Hilt/Nav imports, matching the established
 * `:designsystem` purity convention (mirrors the Trash/TagManagement
 * screen shell exactly, D-01).
 *
 * The Settings/TopAppBar display label reads "Undo History" (D-05) while this composable's
 * name stays `UndoCenterScreen`.
 *
 * @param entries Session-durable undo entries, expected newest-first (caller/ViewModel sorts).
 * @param onNavigateBack Invoked when the back arrow is tapped.
 * @param onUndo Invoked with an entry id when its "Undo" button is tapped (Available rows only).
 * @param groups Optional group projections (VUNDO-02, D-06). Each group with at least one member
 *   present in [entries] renders one header row (label, member count, status and an "Undo all"
 *   button) followed by its members, indented. Entries that belong to no group stay flat. The
 *   default `emptyList()` renders exactly the flat list of earlier releases.
 * @param onUndoGroup Invoked with a group id when its "Undo all" button is tapped. `null` (the
 *   default) hides every "Undo all" button. The button is also hidden for a group whose status is
 *   FullyResolved or Empty.
 * @param onClear Invoked when "Clear history" is tapped — scoped clear of spent entries (D-08).
 *   It stays the trailing parameter, so a positional four-argument call must now name `onClear`
 *   or pass it as the trailing lambda.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UndoCenterScreen(
    entries: List<UndoHistoryEntry>,
    onNavigateBack: () -> Unit,
    onUndo: (String) -> Unit,
    groups: List<UndoGroupRowUiModel> = emptyList(),
    onUndoGroup: ((String) -> Unit)? = null,
    onClear: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Undo History") }, // D-05: display label only
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // D-08: scoped clear — shown only when a spent (Undone/Failed) entry exists,
                    // no confirmation dialog (nothing recoverable is destroyed).
                    if (entries.any { it.status != UndoStatus.Available }) {
                        TextButton(onClick = onClear) { Text("Clear history") }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (entries.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
                title = "No undo history yet",
                body = "Undoable actions you take will appear here.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(buildUndoCenterRows(entries, groups), key = ::undoCenterRowKey) { row ->
                    when (row) {
                        is UndoCenterRow.Group -> UndoGroupHeaderRow(row, onUndoGroup)
                        is UndoCenterRow.Entry -> UndoHistoryRow(
                            entry = row.entry,
                            onUndo = { onUndo(row.entry.id) },
                            modifier = if (row.indented) {
                                Modifier.padding(start = Dimens.HorizontalPadding)
                            } else {
                                Modifier
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Test tag prefix for [UndoHistoryRow]'s outer `Box` — a Compose UI test targets `"$UNDO_ROW_TEST_TAG_PREFIX${entry.id}"`. */
internal const val UNDO_ROW_TEST_TAG_PREFIX = "undo-row-"

/** Test tag prefix for a group header row — a Compose UI test targets `"$UNDO_GROUP_HEADER_TEST_TAG_PREFIX$groupId"`. */
internal const val UNDO_GROUP_HEADER_TEST_TAG_PREFIX = "undo-group-header-"

/** Test tag prefix for a group's "Undo all" button — `"$UNDO_GROUP_UNDO_ALL_TEST_TAG_PREFIX$groupId"`. */
internal const val UNDO_GROUP_UNDO_ALL_TEST_TAG_PREFIX = "undo-group-undo-all-"

/** Test tag for the read-only hold-to-peek overlay ([UndoPreviewPeek]) — asserted present/absent by the row's Compose UI test. */
internal const val UNDO_PEEK_TEST_TAG = "undo-preview-peek"

/** Test tag for the null-preview (message-only) branch of [UndoPreviewPeek] — D-01's graceful fallback, locked by the row's Compose UI test. */
internal const val UNDO_PEEK_MESSAGE_TEST_TAG = "undo-preview-peek-message"

/** One row of the Undo Center list: a group header or an entry (VUNDO-02). */
internal sealed interface UndoCenterRow {
    /** A group header; [memberCount] is the number of the group's members present in `entries`. */
    data class Group(val group: UndoGroupRowUiModel, val memberCount: Int) : UndoCenterRow

    /** An entry row; [indented] is true for a member rendered under its group header. */
    data class Entry(val entry: UndoHistoryEntry, val indented: Boolean) : UndoCenterRow
}

/**
 * Decides every row of the list (VUNDO-02). With empty [groups] it returns [entries] 1:1 as flat
 * rows (the v2.5.1 list). Otherwise the first member found in [entries] order emits the group
 * header followed by ALL its present members (indented, entries order); later members of that
 * group are skipped; ungrouped entries stay flat; a group with no present member emits nothing.
 * A duplicate groupId, or an id listed by two groups, resolves first-wins so row keys stay unique.
 */
internal fun buildUndoCenterRows(
    entries: List<UndoHistoryEntry>,
    groups: List<UndoGroupRowUiModel>
): List<UndoCenterRow> {
    if (groups.isEmpty()) return entries.map { UndoCenterRow.Entry(it, indented = false) }
    val groupsById = LinkedHashMap<String, UndoGroupRowUiModel>()
    groups.forEach { groupsById.putIfAbsent(it.groupId, it) }
    val groupIdByEntryId = HashMap<String, String>()
    groupsById.values.forEach { group ->
        group.memberIds.forEach { groupIdByEntryId.putIfAbsent(it, group.groupId) }
    }
    val emitted = HashSet<String>()
    val rows = ArrayList<UndoCenterRow>(entries.size + groupsById.size)
    for (entry in entries) {
        val groupId = groupIdByEntryId[entry.id]
        when {
            groupId == null -> rows += UndoCenterRow.Entry(entry, indented = false)
            emitted.add(groupId) -> {
                val members = entries.filter { groupIdByEntryId[it.id] == groupId }
                rows += UndoCenterRow.Group(groupsById.getValue(groupId), members.size)
                members.forEach { rows += UndoCenterRow.Entry(it, indented = true) }
            }
        }
    }
    return rows
}

/**
 * Stable LazyColumn key of a row. Both kinds are namespaced (`g:` plus the groupId for a header,
 * `e:` plus the entry id for an entry), so a header key can never equal an entry key whatever ids
 * the caller chose.
 */
internal fun undoCenterRowKey(row: UndoCenterRow): String = when (row) {
    is UndoCenterRow.Group -> "g:${row.group.groupId}"
    is UndoCenterRow.Entry -> "e:${row.entry.id}"
}

/** `1 item` for one, otherwise the number plus ` items`. English-only library text (D-06). */
internal fun groupCountText(count: Int): String = if (count == 1) "1 item" else "$count items"

/** The status words of a group header, or `null` for [UndoGroupStatus.Empty]. English-only library text. */
internal fun groupStatusText(status: UndoGroupStatus): String? = when (status) {
    UndoGroupStatus.Undoable -> "Undoable"
    UndoGroupStatus.PartiallyResolved -> "Partly resolved"
    UndoGroupStatus.FullyResolved -> "Resolved"
    UndoGroupStatus.Empty -> null
}

/**
 * Undo all is shown only for a caller that wired [onUndoGroup] and a group that still has
 * something to undo: Undoable or PartiallyResolved. FullyResolved and Empty groups hide it.
 */
internal fun undoAllVisible(status: UndoGroupStatus, onUndoGroup: ((String) -> Unit)?): Boolean =
    onUndoGroup != null &&
        (status == UndoGroupStatus.Undoable || status == UndoGroupStatus.PartiallyResolved)

/** The count text joined by a middle dot to the status text (just the count when there is no status text). */
internal fun groupSubtitle(count: Int, status: UndoGroupStatus): String {
    val statusText = groupStatusText(status)
    return if (statusText == null) groupCountText(count) else "${groupCountText(count)} · $statusText"
}

/**
 * A group header: label, `count · status` subtitle and an "Undo all" button. It carries no
 * hold-to-peek gesture and its semantics are not merged, so the button stays its own node.
 */
@Composable
private fun UndoGroupHeaderRow(row: UndoCenterRow.Group, onUndoGroup: ((String) -> Unit)?) {
    val group = row.group
    Row(
        modifier = Modifier
            .testTag("$UNDO_GROUP_HEADER_TEST_TAG_PREFIX${group.groupId}")
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = Dimens.HorizontalPadding, vertical = Dimens.TopPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.label,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = groupSubtitle(row.memberCount, group.status),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        if (onUndoGroup != null && undoAllVisible(group.status, onUndoGroup)) {
            TextButton(
                onClick = { onUndoGroup(group.groupId) },
                modifier = Modifier.testTag("$UNDO_GROUP_UNDO_ALL_TEST_TAG_PREFIX${group.groupId}")
            ) { Text(group.undoAllLabel) }
        }
    }
}

/**
 * A single row in the Undo History list — message, auto-ticking relative timestamp, and a
 * per-status trailing control (D-01/D-03/D-04). Wrapped in a `Box` carrying the hold-to-peek
 * gesture (D-04) so the read-only [UndoPreviewPeek] overlay can render above it while held.
 */
@Composable
private fun UndoHistoryRow(entry: UndoHistoryEntry, onUndo: () -> Unit, modifier: Modifier = Modifier) {
    var showPeek by remember(entry.id) { mutableStateOf(false) }
    Box(
        modifier = modifier
            .testTag("$UNDO_ROW_TEST_TAG_PREFIX${entry.id}")
            .pointerInput(entry.id) {
                // A1 (RESEARCH.md): detectTapGestures's awaitFirstDown defaults to
                // requireUnconsumed = true. The nested "Undo" TextButton's clickable
                // consumes the pointer-down during the Main pass (leaf-to-root) BEFORE
                // this ancestor pointerInput sees it — so tapping Undo never triggers a
                // spurious peek; holding anywhere else on the row does. Strictly
                // read-only: invokes no undo action, never mutates the entry (D-04/D-05).
                detectTapGestures(
                    onPress = {
                        showPeek = true
                        tryAwaitRelease()
                        showPeek = false
                    }
                )
            }
    ) {
        ListItem(
            headlineContent = { Text(entry.message) },
            supportingContent = { RelativeTimestampText(entry.timestamp) }, // D-02
            trailingContent = {
                when (entry.status) {
                    // Available: live Undo action.
                    UndoStatus.Available -> TextButton(onClick = onUndo) { Text("Undo") }
                    // D-03: hide the button, show a plain status label — no disabled control.
                    UndoStatus.Undone -> Text(
                        "Undone",
                        color = MaterialTheme.colorScheme.onSurfaceVariant // D-04
                    )
                    UndoStatus.Failed -> Text(
                        "Couldn't undo",
                        color = MaterialTheme.colorScheme.error // D-04: failures are loud
                    )
                }
            }
        )
        if (showPeek) {
            UndoPreviewPeek(preview = entry.preview, message = entry.message)
        }
    }
}

/**
 * Transient, read-only preview overlay shown while a row is held (D-04) — renders the
 * entry's snapshot [preview] payload (D-01/D-02/D-03): thumbnail (when present) + title/type
 * for [UndoPreview.Card]/[UndoPreview.Photo], tag name for [UndoPreview.Tag], both card
 * identities for [UndoPreview.Link], and tag+card identity for [UndoPreview.TagOnCard]. A
 * null [preview] renders a graceful message-only peek (D-01 — every row is previewable, never
 * crashes). Every element renders only when it has content — no empty thumbnail slot when a
 * thumbnail path is absent ([[conditional-render-no-dead-space]]). Invokes no undo action —
 * strictly a read-only snapshot render (D-04/D-05).
 */
@Composable
private fun UndoPreviewPeek(preview: UndoPreview?, message: String) {
    Surface(
        modifier = Modifier
            .testTag(UNDO_PEEK_TEST_TAG)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (preview) {
                null -> Text(
                    text = message,
                    modifier = Modifier.testTag(UNDO_PEEK_MESSAGE_TEST_TAG),
                    style = MaterialTheme.typography.bodyMedium
                )

                is UndoPreview.Card -> {
                    if (preview.thumbnailPath != null) {
                        AsyncImage(
                            model = File(preview.thumbnailPath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(PEEK_THUMBNAIL_SIZE)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Icon(imageVector = cardTypeIcon(preview.cardType), contentDescription = null)
                    Text(text = preview.title, style = MaterialTheme.typography.bodyMedium)
                }

                is UndoPreview.Photo -> {
                    AsyncImage(
                        model = File(preview.thumbnailPath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(PEEK_THUMBNAIL_SIZE)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Text(text = preview.albumTitle, style = MaterialTheme.typography.bodyMedium)
                }

                is UndoPreview.Tag -> {
                    Text(text = preview.name, style = MaterialTheme.typography.bodyMedium)
                }

                is UndoPreview.Link -> {
                    Icon(imageVector = cardTypeIcon(preview.cardAType), contentDescription = null)
                    Text(text = preview.cardATitle, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "↔", style = MaterialTheme.typography.bodyMedium)
                    Icon(imageVector = cardTypeIcon(preview.cardBType), contentDescription = null)
                    Text(text = preview.cardBTitle, style = MaterialTheme.typography.bodyMedium)
                }

                is UndoPreview.TagOnCard -> {
                    Icon(imageVector = cardTypeIcon(preview.cardType), contentDescription = null)
                    Text(
                        text = "${preview.tagName} on ${preview.cardTitle}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private val PEEK_THUMBNAIL_SIZE = 48.dp

/**
 * Relative, auto-ticking timestamp text (D-02) — e.g. "2 min ago". Ticks locally on a
 * ~20s cadence (within the 15-30s window) via its own [LaunchedEffect]; only this leaf
 * `Text` recomposes on each tick, never the parent [ListItem]/list (Pitfall 3-adjacent,
 * per RESEARCH.md).
 *
 * Formats via [DateUtils.getRelativeTimeSpanString] — the platform API for locale-aware
 * relative time, rather than a hand-rolled "X min ago" ladder.
 */
@Composable
private fun RelativeTimestampText(timestampMillis: Long, modifier: Modifier = Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(TICK_INTERVAL_MS)
            now = System.currentTimeMillis()
        }
    }
    val relativeText = DateUtils.getRelativeTimeSpanString(
        timestampMillis,
        now,
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
    Text(text = relativeText, modifier = modifier)
}

private const val TICK_INTERVAL_MS = 20_000L
