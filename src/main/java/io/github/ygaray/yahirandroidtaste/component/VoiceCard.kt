package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ygaray.yahirandroidtaste.component.CardBase
import io.github.ygaray.yahirandroidtaste.component.WaveformCanvas
import io.github.ygaray.yahirandroidtaste.component.downsample
import io.github.ygaray.yahirandroidtaste.component.titleSlotVisible
import io.github.ygaray.yahirandroidtaste.icon.cardTypeIcon
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceClipUiModel
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.TactileType
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
// Amplitude decode helper (Phase 129 DS-03 D-02, T-129-07)
// ---------------------------------------------------------------------------

/** Bar target for the overview strip — unchanged from the pre-extraction inline call. */
private const val OVERVIEW_STRIP_BAR_TARGET = 60

/** Bar target for a compact per-clip mini-row (Phase 129 DS-03 D-02). */
private const val CLIP_ROW_BAR_TARGET = 24

/**
 * Decodes a companion `.bin` amplitude-samples file into a downsampled bar list, extracted
 * verbatim from the overview strip's former inline `LaunchedEffect` body (Phase 129 DS-03 D-02,
 * task 2) so it is directly unit-testable against real bytes ([AmplitudeBarsDecodeTest]) — closing
 * 129-REVIEWS.md's cycle-1 MEDIUM finding that the shared [WaveformCanvas] was otherwise never
 * proven to paint real bars anywhere in this phase.
 *
 * File layout: a 4-byte big-endian `Int` sample count, followed by that many 4-byte `Float`
 * amplitude values (each expected in `0f..1f`).
 *
 * [samplesPath] `null`, a nonexistent path, any read failure (truncated payload, IO error), or a
 * header declaring a count the file's actual byte length cannot hold all degrade to an
 * **empty list** rather than throwing — the caller renders a blank waveform track for that case,
 * never a crash. The count-versus-file-length check in particular guards against a corrupt or
 * hostile header driving a huge allocation (T-129-07): `count` is validated against `maxCount`
 * (derived from the file's real length) **before** any `List(count) { ... }` allocation is made.
 *
 * `internal` rather than `private` is deliberate (Planner Decision 5, 129-03-PLAN.md): it makes
 * this helper directly unit-testable from this module's own test source set, exactly as
 * `thresholdSide`/`nextSubType`/`installedAnchors`/`filterIconEntries` already are — and Metalava
 * excludes Kotlin `internal` from `api.txt`, so no published surface grows.
 *
 * @param samplesPath Path to the `.bin` file, or `null` for "no samples" (returns empty).
 * @param targetBars Bar count [downsample] downsamples the raw decode to.
 */
internal fun readAmplitudeBars(samplesPath: String?, targetBars: Int): List<Float> {
    if (samplesPath == null) return emptyList()
    val file = File(samplesPath)
    if (!file.exists()) return emptyList()
    return try {
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
                // Clamp each decoded float (Phase 129 REVIEWS.md WR-01): a well-formed header
                // with a corrupted/garbage float bit pattern can decode to NaN or Infinity. NaN
                // in particular "wins" downsample()'s max() under IEEE total ordering (NaN
                // compares greater than every other value, including +Infinity), so an
                // unclamped NaN sample would flow into a Canvas drawRoundRect paint call as a
                // NaN-sized rect. Clamp at the decode boundary so every returned bar is finite
                // and within the documented 0f..1f range, regardless of what the bytes decoded to.
                List(count) { dis.readFloat().let { if (it.isNaN()) 0f else it.coerceIn(0f, 1f) } }
            }
        }
    } catch (_: Exception) {
        emptyList()
    }.let { samples -> downsample(samples, targetBars) }
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
// Clip-count header pill (Phase 129 DS-03 D-02)
// ---------------------------------------------------------------------------

/**
 * Aggregate clip-count-and-total-duration header pill for [VoiceCard]'s clip-list header
 * (Phase 129 DS-03 D-02). Renders in the neutral accent-fallback roles
 * (`colorScheme.surfaceVariant` background / `onSurfaceVariant` text) rather than an accent
 * tint: `VoiceCard` has no `accent` parameter of its own in this phase — D-01 scopes `accent` to
 * [CardBase] only, and threading it through the individual card faces is Phase 132/133's job
 * (129-03-PLAN.md Planner Decision 1). This is the specified null-accent fallback rendering, not
 * a reduced version of the pill — the pill itself ships complete.
 *
 * @param clipCount Total number of clips, including any hidden/overflowed ones — not just the
 *   visible/capped rows.
 * @param totalDurationMs Sum of every clip's duration, including hidden/overflowed clips.
 */
@Composable
private fun VoiceClipCountPill(
    clipCount: Int,
    totalDurationMs: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = voiceClipPillCopy(clipCount, totalDurationMs),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Pure clip-count-and-total-duration pill copy builder (Phase 129 DS-03 D-02), extracted out of
 * [VoiceClipCountPill] so it is directly unit-testable without composing the full [VoiceCard] —
 * this hub's Robolectric harness cannot render any `CardBase`-based card at all (`CardBase`'s
 * unconditional `SwipeableActionRow` throws `IllegalStateException` on the first frame; see
 * `VoiceCardClipListTest`'s class KDoc for the full, already-documented precedent). `internal`
 * rather than `private` for the same reason `readAmplitudeBars` is `internal` (task 2, Planner
 * Decision 5): Metalava excludes Kotlin `internal` from `api.txt`, so this widens no published
 * surface while making the copy logic testable from this module's own test source set.
 *
 * Branches on [clipCount] so the singular and plural forms are each a complete, readable literal
 * — never a raw plural-suffix concatenation.
 */
internal fun voiceClipPillCopy(clipCount: Int, totalDurationMs: Long): String =
    if (clipCount == 1) {
        "$clipCount clip · ${formatDuration(totalDurationMs)}"
    } else {
        "$clipCount clips · ${formatDuration(totalDurationMs)}"
    }

// ---------------------------------------------------------------------------
// Capped clip mini-rows + overflow line (Phase 129 DS-03 D-02, T-129-06)
// ---------------------------------------------------------------------------

/**
 * Visible clip-row cap (Phase 133 D-02): raised from the Phase 129 value of `2` to `3` to match
 * `LIST_PREVIEW_ITEM_LIMIT`, keeping the clip-row cap consistent with the List face's own preview
 * cap. The cap-agnostic `take`/hidden-count mechanism below is unchanged — only this constant's
 * value moved.
 */
private const val CLIP_ROW_CAP = 3

/**
 * Pure overflow-line copy builder (Phase 129 DS-03 D-02) — mirrors [voiceClipPillCopy]'s
 * branch-per-literal pluralization discipline and the established List-card "+N more" overflow
 * convention. `internal` for the same direct-unit-testability reason as [voiceClipPillCopy] and
 * [readAmplitudeBars].
 */
internal fun voiceClipOverflowCopy(hiddenCount: Int): String =
    if (hiddenCount == 1) "+1 more clip" else "+$hiddenCount more clips"

/**
 * One read-only per-clip mini-row (Phase 129 DS-03 D-02): a one-based index label (derived from
 * [VoiceClipUiModel.sortOrder], never from list position), a mini waveform on the **shared**
 * [WaveformCanvas] — not the file-private [VoiceWaveformCanvas] the overview strip uses — and a
 * duration label. Owns its own amplitude-bars decode, keyed on this clip's own `samplesPath`,
 * following the exact background-IO-decode-then-assign discipline the overview strip already
 * uses (T-129-08): the read happens inside `withContext(Dispatchers.IO)` and the resulting state
 * is assigned back on the composition dispatcher.
 *
 * Carries **no** `clickable`, `combinedClickable` or `pointerInput` modifier anywhere in this
 * function or its children (D-02, T-129-09 — guards the SWIPE-02 defect class): every touch
 * inside the card face still belongs to [CardBase]'s single `combinedClickable`.
 *
 * `internal` rather than `private` so it — like [VoiceClipRowsSection] — is directly
 * Compose-testable in isolation from [VoiceCard]'s enclosing `CardBase`/`SwipeableActionRow`,
 * which this hub's Robolectric harness cannot render at all (see `VoiceCardClipListTest`'s class
 * KDoc). This is what lets the real-bytes waveform-renderability proof (129-REVIEWS.md cycle-1
 * MEDIUM) actually compose and settle under test, not merely compile.
 */
@Composable
internal fun VoiceClipRow(clip: VoiceClipUiModel, modifier: Modifier = Modifier) {
    var amplitudeBars by remember(clip.samplesPath) { mutableStateOf<List<Float>>(emptyList()) }
    LaunchedEffect(clip.samplesPath) {
        val bars = withContext(Dispatchers.IO) {
            readAmplitudeBars(clip.samplesPath, CLIP_ROW_BAR_TARGET)
        }
        amplitudeBars = bars
    }
    Row(
        modifier = modifier.testTag("voice_clip_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${clip.sortOrder + 1}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(min = 16.dp)
        )
        WaveformCanvas(
            bars = amplitudeBars,
            activeColor = MaterialTheme.colorScheme.onSurfaceVariant,
            inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        )
        Text(
            text = formatDuration(clip.durationMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * The clip-list body content (Phase 129 DS-03 D-02): up to [CLIP_ROW_CAP] read-only
 * [VoiceClipRow]s in the caller's exact list order, followed by a pluralized overflow line when
 * [clips] exceeds the cap. Applies `take(CLIP_ROW_CAP)` **before** composing any row, so a decode
 * is never started for a hidden clip — the real mitigation for T-129-06, not a cosmetic trim.
 * Never sorts, filters or dedupes [clips].
 *
 * `internal` rather than `private` so it is directly Compose-testable in isolation from
 * [VoiceCard]'s enclosing `CardBase`/`SwipeableActionRow`, which this hub's Robolectric harness
 * cannot render at all (see `VoiceCardClipListTest`'s class KDoc).
 */
@Composable
internal fun VoiceClipRowsSection(clips: List<VoiceClipUiModel>, modifier: Modifier = Modifier) {
    val hiddenCount = (clips.size - CLIP_ROW_CAP).coerceAtLeast(0)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // take() runs before any row composes — no decode starts for a clip past the cap.
        clips.take(CLIP_ROW_CAP).forEach { clip ->
            VoiceClipRow(clip = clip, modifier = Modifier.fillMaxWidth())
        }
        if (hiddenCount > 0) {
            Text(
                text = voiceClipOverflowCopy(hiddenCount),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                // Indented to align under the waveform column (index label width + row gap).
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
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
 * Three-dot menu: Edit, Manage links, Pin/Unpin, Favorite/Unfavorite, Delete.
 * The single "Edit" row invokes [onRenameOrTagsRequest] — the host opens one
 * [VoiceRenameTagsSheet] instance (EDIT-01: the former separate "Rename" + "Edit tags" rows,
 * which both called this same trigger, are collapsed into this one row; D-01, Pattern 3).
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
 *   from right-swipe Edit and the three-dot "Edit" item (D-01, EDIT-01 unified action).
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
 * @param clips Read-only clip list (Phase 129 DS-03 D-02). An empty default (the current state
 *   of every existing call site) keeps the card's rendering exactly as it is today — the compact
 *   overview waveform strip plus a standalone duration text. A non-empty list replaces those with
 *   an aggregate clip-count-and-total-duration header pill and a capped set of read-only per-clip
 *   mini-rows, each carrying no gesture of its own. The hub preserves the caller's list order and
 *   never sorts, filters or dedupes it.
 * @param accent FACE-03: caller-supplied per-card colour, forwarded verbatim into [CardBase]'s
 *   accent spine and into the header [CardTypeChip]. The hub performs zero tag-resolution of its
 *   own — `:app`'s `CardAccentResolver` (Phase 131) resolves the actual value. `null` (default)
 *   renders every accent-reading surface in its designed neutral state.
 * @param tactileDepth FACE-03: opts this card into [CardBase]'s Tactile depth-card chrome —
 *   elevation, corner radius, and the accent spine. Defaults to `false` so every pre-existing call
 *   site renders exactly as before until the consumer app opts in (Phase 133 Plan 03).
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
    onTagRemoveFromCard: ((tagId: String) -> Unit)? = null,
    clips: List<VoiceClipUiModel> = emptyList(),
    accent: Color? = null,
    tactileDepth: Boolean = false
) {
    // Load amplitude samples from .bin file on IO dispatcher
    var amplitudeBars by remember(samplesPath) { mutableStateOf<List<Float>>(emptyList()) }
    LaunchedEffect(samplesPath) {
        // UIBUG-05 (D-07): compute downsample inside withContext(IO) and return the result;
        // assign `amplitudeBars` on the composition dispatcher (the line after withContext
        // resumes). This matches TextCard / ListCard convention — never write Compose state
        // from Dispatchers.IO.
        val bars = withContext(Dispatchers.IO) {
            readAmplitudeBars(samplesPath, OVERVIEW_STRIP_BAR_TARGET)
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
            // Edit — single unified action (EDIT-01): the former "Rename" + "Edit tags" rows,
            // which both already called onRenameOrTagsRequest, collapsed into one "Edit" row that
            // fires the same host-owned rename+tags sheet trigger (D-01, single hosted sheet).
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    dismissMenu()
                    onRenameOrTagsRequest()
                },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
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
        // Planner Decision 4 (129-03-PLAN.md): the header slot must survive a blank title when
        // clips are present, so it renders whenever the title is visible OR clips is non-empty —
        // and the title Text inside it stays independently conditional so a blank title still
        // renders nothing (conditional-render-no-dead-space).
        headerContent = if (!titleSlotVisible(title) && clips.isEmpty()) null else {
            {
                // Type chip (FACE-03, Phase 132 pattern): leads the header on the combined gate
                // above (title visible OR clips non-empty), never only when a title exists — a
                // blank title with non-empty clips must still show the Voice type identity. No
                // explicit tint — the chip resolves the icon's size and colour itself.
                CardTypeChip(
                    accent = accent,
                    modifier = Modifier.padding(start = Dimens.HorizontalPadding, top = Dimens.TopPadding)
                ) {
                    Icon(imageVector = cardTypeIcon("VOICE"), contentDescription = null)
                }
                if (titleSlotVisible(title)) {
                    Text(
                        text = title,
                        style = TactileType.CardTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_card_title")
                            .padding(
                                start = Dimens.ChipToTitleGap,
                                top = Dimens.TopPadding,
                                bottom = Dimens.ContentSpacing
                            )
                    )
                }
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
                // Clip-count header pill (Phase 129 DS-03 D-02) — trailing element, only when
                // clips is non-empty. Total sums every clip, not just the visible/capped rows.
                if (clips.isNotEmpty()) {
                    VoiceClipCountPill(
                        clipCount = clips.size,
                        totalDurationMs = clips.sumOf { it.durationMs },
                        modifier = Modifier.padding(end = Dimens.HorizontalPadding)
                    )
                }
            }
        },
        bodyContent = {
            if (clips.isEmpty()) {
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
            } else {
                // Clip-list rows (Phase 129 DS-03 D-02) replace the overview strip when clips is
                // non-empty — the aggregate total already lives in the header pill.
                VoiceClipRowsSection(
                    clips = clips,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.HorizontalPadding)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.ContentSpacing))

            // DURATION + CATEGORY PATH row — the standalone duration text only renders when
            // clips is empty (the clip-count header pill already carries the total when clips is
            // present); when it is dropped, Arrangement.End keeps a lone category path pinned to
            // the trailing edge instead of jumping to the leading edge.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.HorizontalPadding,
                        vertical = Dimens.BottomPadding
                    ),
                horizontalArrangement = if (clips.isEmpty()) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (clips.isEmpty()) {
                    Text(
                        text = formatDuration(durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
        accent = accent,
        tactileDepth = tactileDepth,
        modifier = modifier
    )
}
