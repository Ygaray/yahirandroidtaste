package io.github.ygaray.yahirandroidtaste.feedback

import android.text.format.DateUtils
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.ygaray.yahirandroidtaste.component.EmptyState
import io.github.ygaray.yahirandroidtaste.icon.cardTypeIcon
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
 * @param onClear Invoked when "Clear history" is tapped — scoped clear of spent entries (D-08).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UndoCenterScreen(
    entries: List<UndoHistoryEntry>,
    onNavigateBack: () -> Unit,
    onUndo: (String) -> Unit,
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
                items(entries, key = { it.id }) { entry ->
                    UndoHistoryRow(entry = entry, onUndo = { onUndo(entry.id) })
                }
            }
        }
    }
}

/** Test tag prefix for [UndoHistoryRow]'s outer `Box` — a Compose UI test targets `"$UNDO_ROW_TEST_TAG_PREFIX${entry.id}"`. */
internal const val UNDO_ROW_TEST_TAG_PREFIX = "undo-row-"

/** Test tag for the read-only hold-to-peek overlay ([UndoPreviewPeek]) — asserted present/absent by the row's Compose UI test. */
internal const val UNDO_PEEK_TEST_TAG = "undo-preview-peek"

/** Test tag for the null-preview (message-only) branch of [UndoPreviewPeek] — D-01's graceful fallback, locked by the row's Compose UI test. */
internal const val UNDO_PEEK_MESSAGE_TEST_TAG = "undo-preview-peek-message"

/**
 * A single row in the Undo History list — message, auto-ticking relative timestamp, and a
 * per-status trailing control (D-01/D-03/D-04). Wrapped in a `Box` carrying the hold-to-peek
 * gesture (D-04) so the read-only [UndoPreviewPeek] overlay can render above it while held.
 */
@Composable
private fun UndoHistoryRow(entry: UndoHistoryEntry, onUndo: () -> Unit) {
    var showPeek by remember(entry.id) { mutableStateOf(false) }
    Box(
        modifier = Modifier
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
