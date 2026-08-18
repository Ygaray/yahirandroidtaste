package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.component.CardBase
import com.example.secondbrain.yahirandroidtaste.component.WaveformCanvas
import com.example.secondbrain.yahirandroidtaste.component.downsample
import com.example.secondbrain.yahirandroidtaste.component.titleSlotVisible
import com.example.secondbrain.yahirandroidtaste.model.TagChipUiModel
import com.example.secondbrain.yahirandroidtaste.modifier.SwipeAnchor
import com.example.secondbrain.yahirandroidtaste.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.File

// ---------------------------------------------------------------------------
// Note: downsample() is provided by WaveformCanvas.kt in the same package.
// The VoiceWaveformCanvas below uses its own rendering style (2dp bar / 2dp gap)
// which differs from the shared WaveformCanvas (proportional bar width).
// ---------------------------------------------------------------------------

/**
 * Renders amplitude bars with a progress cursor.
 * Bars to the left of [progressFraction] are drawn in [activeColor]; the rest in [inactiveColor].
 */
@Composable
private fun VoiceWaveformCanvas(
    bars: List<Float>,
    progressFraction: Float,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (bars.isEmpty()) return@Canvas
        val barWidth = 2.dp.toPx()
        val barGap = 2.dp.toPx()
        val cornerRadius = 1.dp.toPx()
        val totalBarWidth = barWidth + barGap
        val activeUpToX = progressFraction * size.width

        bars.forEachIndexed { index, amplitude ->
            val left = index * totalBarWidth
            val barHeight = (amplitude * size.height).coerceAtLeast(2.dp.toPx())
            val top = (size.height - barHeight) / 2f
            val color = if (left <= activeUpToX) activeColor else inactiveColor
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(cornerRadius)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Duration formatting helper
// ---------------------------------------------------------------------------

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "$hours:${"%02d".format(minutes)}:${"%02d".format(seconds)}"
    else "$minutes:${"%02d".format(seconds)}"
}

// ---------------------------------------------------------------------------
// VoiceCard composable
// ---------------------------------------------------------------------------

/**
 * Voice card face component.
 *
 * Shows a compact waveform strip (loaded from companion .bin file), title, and duration.
 * Renders through [CardBase] + [SwipeableActionRow] — inherits reveal-then-confirm swipe,
 * long-press haptic menu, and single-open row policy (D-02).
 *
 * Swipe behaviors (via [CardBase] + [SwipeableActionRow], reveal-then-confirm):
 * - Right swipe reveals the Edit button → tapping it invokes [onRenameOrTagsRequest] (D-05).
 * - Left swipe reveals the Delete button → tapping it fires [onDelete] (D-04).
 *
 * Tap: navigates to full-screen playback (onClick = [onTap] passed to CardBase, not to an inner
 * Card — CardBase's combinedClickable owns both tap and long-press; this fixes the previously-dead
 * long-press that was shadowed by Card(onClick=...) (SWIPE-02, Pitfall 3)).
 *
 * Three-dot menu: Rename, Edit tags, Manage links, Pin/Unpin, Favorite/Unfavorite, Delete.
 * Rename and Edit tags both invoke the same [onRenameOrTagsRequest] trigger — the host opens a
 * single [VoiceRenameTagsSheet] instance for both entry points (D-01, Pattern 3).
 *
 * @param id Stable card ID used for keyed remember state.
 * @param title Card title.
 * @param durationMs Total audio duration in milliseconds.
 * @param samplesPath Path to companion .bin amplitude file (nullable — shows empty canvas if null).
 * @param categoryPath Optional breadcrumb (nullable).
 * @param isPinned Whether card is pinned.
 * @param isFavorite Whether card is favorited.
 * @param onTap Navigate to full-screen playback.
 * @param onDelete Soft-delete this card.
 * @param onTogglePin Toggle pin state.
 * @param onToggleFavorite Toggle favorite state.
 * @param onRenameOrTagsRequest Single external trigger for the hosted rename/tags sheet — invoked
 *   from right-swipe Edit, the three-dot "Rename" item, and the three-dot "Edit tags" item (D-01).
 * @param onManageLinks Optional — invoked when the "Manage links" overflow item is tapped (D-04).
 *   Opens [RelationshipPanelSheet] hosted in ModuleScreen.
 * @param openRowState Single-open row reference hoisted at [CardListSection] (D-02).
 * @param modifier Optional outer modifier.
 * @param tags Frequency-pre-sorted tags rendered via [CardTagRow] on the card face (FACE-01).
 * @param onTagClick Called with the tag id when a chip on the card-face tag row is tapped.
 * @param onSiblingsClick Called with the full (non-truncated) list of this card's tag ids when
 *   the tag-row band or the "See exact siblings" menu entry is activated (FACE-02, D-01).
 * @param onCloseSiblingsClick BROWSE-10 / D-04/D-06: called with this card's id when the "Close
 *   siblings" overflow menu entry is activated — a distinct, labeled discovery action from "See
 *   exact siblings" (D-12), visible only when [tags] is non-empty (D-10).
 * @param onTagEdit Phase 93 (TMENU-01/D-01 row 3): forwarded verbatim to [CardTagRow]'s
 *   [CardTagRow.onTagEdit]. Null (default) keeps the card-face tag row's plain-chip, no-menu
 *   behavior (backward-compat).
 * @param onTagDelete Phase 93 (TMENU-01/04): forwarded verbatim to [CardTagRow]'s
 *   [CardTagRow.onTagDelete]. Null (default) omits the menu's "Delete tag everywhere" item.
 * @param onTagRemoveFromCard Phase 93 (TMENU-01/04/05): forwarded verbatim to [CardTagRow]'s
 *   [CardTagRow.onTagRemoveFromCard]. Null (default) omits the menu's "Remove from this card"
 *   item.
 */
@Composable
fun VoiceCard(
    id: String,
    title: String,
    durationMs: Long,
    samplesPath: String?,
    categoryPath: String?,
    isPinned: Boolean,
    isFavorite: Boolean,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRenameOrTagsRequest: () -> Unit,
    openRowState: MutableState<AnchoredDraggableState<SwipeAnchor>?>,
    modifier: Modifier = Modifier,
    onManageLinks: (() -> Unit)? = null,
    tags: List<TagChipUiModel> = emptyList(),
    onTagClick: (tagId: String) -> Unit = {},
    onSiblingsClick: (allTagIds: List<String>) -> Unit = {},
    onCloseSiblingsClick: (cardId: String) -> Unit = {},
    onTagEdit: ((tagId: String) -> Unit)? = null,
    onTagDelete: ((tagId: String, name: String) -> Unit)? = null,
    onTagRemoveFromCard: ((tagId: String) -> Unit)? = null
) {
    // Load amplitude samples from .bin file on IO dispatcher
    var amplitudeBars by remember(samplesPath) { mutableStateOf<List<Float>>(emptyList()) }
    LaunchedEffect(samplesPath) {
        if (samplesPath == null) return@LaunchedEffect
        // UIBUG-05 (D-07): compute downsample inside withContext(IO) and return the result;
        // assign `amplitudeBars` on the composition dispatcher (the line after withContext
        // resumes). This matches TextCard / ListCard convention — never write Compose state
        // from Dispatchers.IO.
        val bars = withContext(Dispatchers.IO) {
            val file = File(samplesPath)
            if (!file.exists()) return@withContext emptyList()
            try {
                DataInputStream(file.inputStream().buffered()).use { dis ->
                    val count = dis.readInt()
                    // Validate against the actual payload before allocating: the file is a
                    // 4-byte count followed by `count` 4-byte floats. A corrupt/negative header
                    // would otherwise pre-allocate a huge List and throw OutOfMemoryError, which
                    // is an Error (not caught below). Cap to what the file can actually hold.
                    val maxCount = ((file.length() - 4) / 4).coerceAtLeast(0).toInt()
                    if (count < 0 || count > maxCount) {
                        emptyList()
                    } else {
                        List(count) { dis.readFloat() }
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }.let { samples -> downsample(samples, 60) }
        }
        amplitudeBars = bars  // composition dispatcher — safe Compose state write
    }

    CardBase(
        showThreeDot = true,
        onDeleteClick = onDelete,                       // D-04: left swipe → Delete (uniform)
        onEditClick = onRenameOrTagsRequest,             // D-05/D-01: right swipe → single hosted sheet trigger
        openRowState = openRowState,
        onClick = onTap,                                // tap → playback; combinedClickable in CardBase owns long-press
        dropdownMenuContent = { dismissMenu ->
            // Rename — same trigger as Edit tags and right-swipe Edit (D-01, single hosted sheet)
            DropdownMenuItem(
                text = { Text("Rename") },
                onClick = {
                    dismissMenu()
                    onRenameOrTagsRequest()
                },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
            // Edit tags — same trigger as Rename and right-swipe Edit (D-01, single hosted sheet)
            DropdownMenuItem(
                text = { Text("Edit tags") },
                onClick = {
                    dismissMenu()
                    onRenameOrTagsRequest()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = "Edit tags"
                    )
                }
            )
            // Manage links — opens RelationshipPanelSheet (D-04).
            // Only rendered when a handler is wired — avoids a silent dead menu item.
            if (onManageLinks != null) {
                DropdownMenuItem(
                    text = { Text("Manage links") },
                    onClick = {
                        dismissMenu()
                        onManageLinks()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Manage links"
                        )
                    }
                )
            }
            // See exact siblings — discoverable backup to the tag-row band gesture (D-07/D-08).
            // Distinct icon (Group) from "Edit tags" (AutoMirrored.Filled.Label) above.
            if (tags.size >= 2) {
                DropdownMenuItem(
                    text = { Text("See exact siblings") },
                    onClick = { dismissMenu(); onSiblingsClick(tags.map { it.id }) },
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
            }
            // Close siblings (BROWSE-10 / D-04) — a distinct, labeled near-match discovery
            // action (symmetric-diff <=2) adjacent to "See exact siblings"; visibility-gated on
            // tags.isNotEmpty() (D-10, broader than the exact-siblings tags.size >= 2 guard).
            if (tags.isNotEmpty()) {
                DropdownMenuItem(
                    text = { Text("Close siblings") },
                    onClick = { dismissMenu(); onCloseSiblingsClick(id) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null) }
                )
            }
            // Pin/Unpin
            DropdownMenuItem(
                text = { Text(if (isPinned) "Unpin" else "Pin") },
                onClick = { dismissMenu(); onTogglePin() },
                leadingIcon = {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null
                    )
                }
            )
            // Favorite/Unfavorite
            DropdownMenuItem(
                text = { Text(if (isFavorite) "Unfavorite" else "Favorite") },
                onClick = { dismissMenu(); onToggleFavorite() },
                leadingIcon = {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null
                    )
                }
            )
            // Delete — error color
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                onClick = { dismissMenu(); onDelete() },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        },
        headerContent = if (!titleSlotVisible(title)) null else {
            {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = Dimens.HorizontalPadding,
                            top = Dimens.TopPadding,
                            bottom = Dimens.ContentSpacing
                        )
                )
                // Pin / favourite indicators
                if (isPinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Favourite",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        },
        bodyContent = {
            // COMPACT WAVEFORM STRIP — 48.dp height, downsampled to ~60 bars
            VoiceWaveformCanvas(
                bars = amplitudeBars,
                progressFraction = 1f,
                activeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = Dimens.HorizontalPadding)
            )

            Spacer(modifier = Modifier.height(Dimens.ContentSpacing))

            // DURATION + CATEGORY PATH row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.HorizontalPadding,
                        vertical = Dimens.BottomPadding
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDuration(durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (categoryPath != null) {
                    Text(
                        text = categoryPath,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        // WR-01: caller owns "no tags → no slot" — pass null so CardBase composes no tag-row Box
        // for an untagged card, honoring the same optional-slot contract as header/body/footer.
        tagRowContent = if (tags.isNotEmpty()) {
            {
                CardTagRow(
                    tags = tags,
                    onTagClick = onTagClick,
                    onSiblingsClick = { onSiblingsClick(tags.map { it.id }) },
                    onTagEdit = onTagEdit,
                    onTagDelete = onTagDelete,
                    onTagRemoveFromCard = onTagRemoveFromCard
                )
            }
        } else null,
        // G2-01/D-05: net-new trailing icon cluster (VoiceCard previously had no footerContent) —
        // a single OpenInFull open-editor icon wired to the same rename/tags trigger used by
        // right-swipe Edit and the "Rename"/"Edit tags" menu items (A1). No expand/collapse arrow
        // (Voice has none) and no second MoreVert — CardBase supplies the one trailing ⋮ via
        // showThreeDot. Resulting trailing cluster: [✎][⋮].
        footerContent = {
            IconButton(onClick = { onRenameOrTagsRequest() }) {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = "Open editor",
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        modifier = modifier
    )
}
