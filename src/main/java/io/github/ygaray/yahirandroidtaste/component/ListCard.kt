package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ygaray.yahirandroidtaste.component.CardBase
import io.github.ygaray.yahirandroidtaste.component.titleSlotVisible
import io.github.ygaray.yahirandroidtaste.icon.cardTypeIcon
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.model.ListItemUiModel
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.TactileType

/**
 * List card face component with expand/collapse, swipe gestures, three-dot menu, and bottom sheet.
 *
 * Renders list items with sub-type indicators:
 * - BULLETED: bullet character prefix
 * - ORDERED: 1./2./3. numeric prefix
 * - CHECKBOX: read-only CheckBox/CheckBoxOutlineBlank icons (D-06)
 *
 * Expand/collapse: collapsed shows ≤3 items, expanded shows ≤10 items.
 * Footer: OpenInFull → opens bottom sheet, expand arrow. The CHECKBOX completion signal ("N / M"
 * pill + a thin progress bar) lives in the header/body, not the footer (Phase 132 FACE-02).
 * Swipe: left=Delete (D-04), right=opens list content editor (D-05). Reveal-then-confirm via CardBase.
 *
 * @param id Stable ID for keyed remember state.
 * @param title Card title.
 * @param subType "BULLETED" | "ORDERED" | "CHECKBOX"
 * @param items List item states.
 * @param categoryPath Breadcrumb path (nullable).
 * @param createdAt Creation timestamp in millis, forwarded to the hosted [ListCardBottomSheet]'s
 *   [CardQuickView] timestamps footer (D-04).
 * @param updatedAt Last update timestamp in millis, forwarded to the hosted [ListCardBottomSheet].
 * @param isPinned Whether card is pinned.
 * @param isFavorite Whether card is favorited.
 * @param onEdit Called by right-swipe action (D-05: opens list content editor).
 * @param onDelete Called by left-swipe action (D-04) and Delete menu item.
 * @param onTogglePin Called by Pin/Unpin menu item.
 * @param onToggleFavorite Called by Favorite/Unfavorite menu item.
 * @param onConfirmRename Called with new title when rename is confirmed.
 * @param showBottomSheet Whether the bottom sheet is currently open (lifted state).
 * @param onShowBottomSheet Called when OpenInFull is tapped.
 * @param onDismissBottomSheet Called when the bottom sheet is dismissed.
 * @param onToggleItem Called with itemId when a checkbox item is toggled (bottom sheet).
 * @param openRowState Single-open row state hoisted at CardListSection (D-02).
 * @param modifier Optional outer modifier.
 * @param tagContent Optional canonical tag-row slot forwarded to the hosted [ListCardBottomSheet]
 *   (ASSIGN-03). Filled by the `:app` caller with a live `TagChipEditor`.
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
 * @param onEditRequest EDIT-01/EDIT-03: external trigger for the host-owned shared name-and-tags
 *   Edit sheet. When non-null, the three-dot "Edit" row invokes it (mirroring Voice); when null
 *   (default), the row falls back to this card's local tag-less rename dialog, so every existing
 *   call site compiles and behaves as before. The consumer app wires it at Phase 113.
 * @param accent FACE-02: caller-supplied per-card colour, forwarded verbatim into [CardBase]'s
 *   accent spine, into the header [CardTypeChip], and into the completion pill/progress bar. The
 *   hub performs zero tag-resolution of its own — `:app`'s `CardAccentResolver` (Phase 131)
 *   resolves the actual value. `null` (default) renders every accent-reading surface in its
 *   designed neutral state.
 * @param tactileDepth FACE-02: opts this card into [CardBase]'s Tactile depth-card chrome —
 *   elevation, corner radius, and the accent spine. Defaults to `false` so every pre-existing call
 *   site renders exactly as before until the consumer app opts in (Phase 132 Plan 03).
 */
@Composable
fun ListCard(
    id: String,
    title: String,
    subType: String,
    items: List<ListItemUiModel>,
    categoryPath: String?,
    createdAt: Long,
    updatedAt: Long,
    isPinned: Boolean,
    isFavorite: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onConfirmRename: (String) -> Unit,
    onToggleItem: (itemId: String) -> Unit,
    openRowState: MutableState<AnchoredDraggableState<SwipeAnchor>?>,
    modifier: Modifier = Modifier,
    showBottomSheet: Boolean = false,
    onShowBottomSheet: () -> Unit = {},
    onDismissBottomSheet: () -> Unit = {},
    tagContent: (@Composable () -> Unit)? = null,
    tags: List<TagChipUiModel> = emptyList(),
    onTagClick: (tagId: String) -> Unit = {},
    onSiblingsClick: (allTagIds: List<String>) -> Unit = {},
    onCloseSiblingsClick: (cardId: String) -> Unit = {},
    onTagEdit: ((tagId: String) -> Unit)? = null,
    onTagDelete: ((tagId: String, name: String) -> Unit)? = null,
    onTagRemoveFromCard: ((tagId: String) -> Unit)? = null,
    onEditRequest: (() -> Unit)? = null,
    accent: Color? = null,
    tactileDepth: Boolean = false
) {
    // Re-keyed by id so state resets correctly when a different card occupies this slot (UIQ-02).
    var isExpanded by remember(id) { mutableStateOf(false) }
    var showRenameDialog by remember(id) { mutableStateOf(false) }
    var renameText by remember(id) { mutableStateOf(title) }
    // UIQ-02 / D-05: keep renameText in sync with externally-changed title while dialog is closed.
    // The `if (!showRenameDialog)` guard prevents clobbering an in-progress edit.
    LaunchedEffect(title) { if (!showRenameDialog) renameText = title }

    // Legibility guard (D-04): when title is absent the floating overlay occupies the top-end
    // corner; give the first body element 48dp end clearance so the first line is not occluded.
    val titleAbsent = !titleSlotVisible(title)

    CardBase(
        showThreeDot = true,
        onDeleteClick = onDelete,
        onEditClick = onEdit,
        openRowState = openRowState,
        onClick = onShowBottomSheet,
        dropdownMenuContent = { dismissMenu ->
            ListCardDropdownMenuContent(
                dismissMenu = dismissMenu,
                title = title,
                isPinned = isPinned,
                isFavorite = isFavorite,
                tags = tags,
                id = id,
                onTogglePin = onTogglePin,
                onToggleFavorite = onToggleFavorite,
                onSiblingsClick = onSiblingsClick,
                onCloseSiblingsClick = onCloseSiblingsClick,
                onDelete = onDelete,
                onEditRequest = onEditRequest,
                onRenameRequested = {
                    renameText = title
                    showRenameDialog = true
                }
            )
        },
        // PD-3 (132-01-PLAN.md): widened from `if (!titleSlotVisible(title)) null else` — a
        // titleless CHECKBOX list with completion items must still render the header row for its
        // completion pill, mirroring VoiceCard.kt:542-546's identical shape.
        headerContent = if (!titleSlotVisible(title) && !listCompletionVisible(subType, items.size)) {
            null
        } else {
            {
                ListCardHeaderContent(
                    title = title,
                    isPinned = isPinned,
                    isFavorite = isFavorite,
                    accent = accent,
                    subType = subType,
                    completed = items.count { it.isCompleted },
                    total = items.size
                )
            }
        },
        bodyContent = {
            ListCardBodyContent(
                items = items,
                subType = subType,
                categoryPath = categoryPath,
                isExpanded = isExpanded,
                titleAbsent = titleAbsent,
                accent = accent
            )
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
        // G2-01/D-05: emits ONLY the trailing icon cluster — no inner fillMaxWidth/SpaceBetween
        // wrapper — so it slots directly into CardBase's combined bottom row (tags leading,
        // footer + the single relocated MoreVert trailing, one Row, SpaceBetween owned by
        // CardBase). The CHECKBOX completion signal that used to live here (a plain-text "N/M
        // done" footer label) moved to the header's ListCompletionPill in Phase 132 (FACE-02,
        // RESEARCH Pitfall 4) — rendering it in both places would duplicate the same count.
        footerContent = {
            ListCardFooterContent(
                items = items,
                isExpanded = isExpanded,
                onExpandChange = { isExpanded = it },
                onShowBottomSheet = onShowBottomSheet
            )
        },
        modifier = modifier,
        accent = accent,
        tactileDepth = tactileDepth
    )

    // Bottom sheet — only composed when shown; state is lifted to CardListSection (WR-06)
    if (showBottomSheet) {
        ListCardBottomSheetHost(
            title = title,
            items = items,
            subType = subType,
            categoryPath = categoryPath,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isPinned = isPinned,
            isFavorite = isFavorite,
            onToggleItem = onToggleItem,
            onEdit = onEdit,
            onDismissBottomSheet = onDismissBottomSheet,
            onTogglePin = onTogglePin,
            onToggleFavorite = onToggleFavorite,
            onConfirmRename = onConfirmRename,
            onDelete = onDelete,
            tagContent = tagContent
        )
    }

    // Inline rename dialog
    if (showRenameDialog) {
        ListCardRenameDialog(
            renameText = renameText,
            onRenameTextChange = { renameText = it },
            onDismiss = { showRenameDialog = false },
            onConfirm = onConfirmRename
        )
    }
}

/**
 * Three-dot dropdown menu items for [ListCard] — extracted as a private helper so the branching
 * (rename/pin/favorite/siblings/close-siblings/delete) lives outside [ListCard]'s own cyclomatic
 * complexity count (behavior-preserving; DETEKT-02 pre-req refactor, 97-03).
 */
@Composable
private fun ListCardDropdownMenuContent(
    dismissMenu: () -> Unit,
    title: String,
    isPinned: Boolean,
    isFavorite: Boolean,
    tags: List<TagChipUiModel>,
    id: String,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSiblingsClick: (allTagIds: List<String>) -> Unit,
    onCloseSiblingsClick: (cardId: String) -> Unit,
    onDelete: () -> Unit,
    onEditRequest: (() -> Unit)?,
    onRenameRequested: () -> Unit
) {
    // Edit (EDIT-01/EDIT-03) — external trigger to the host-owned shared name-and-tags sheet when
    // wired; otherwise the null-fallback opens the local tag-less rename dialog via onRenameRequested.
    DropdownMenuItem(
        text = { Text("Edit") },
        onClick = {
            dismissMenu()
            if (onEditRequest != null) onEditRequest() else onRenameRequested()
        },
        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
    )
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
    // See exact siblings — discoverable backup to the tag-row band gesture (D-07/D-08)
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
}

/**
 * Header row content (type chip + title + pin/favorite indicators + completion pill) —
 * extracted from [ListCard] (DETEKT-02 pre-req refactor, 97-03; restyled Phase 132 FACE-02).
 */
@Composable
private fun RowScope.ListCardHeaderContent(
    title: String,
    isPinned: Boolean,
    isFavorite: Boolean,
    accent: Color?,
    subType: String,
    completed: Int,
    total: Int
) {
    // Type chip (FACE-02, Phase 132 DS-02): leads the header, carries the 16dp leading inset
    // the title used to own (PD-1). No explicit tint — the chip resolves the icon's size and
    // colour itself via LocalContentColor.
    CardTypeChip(
        accent = accent,
        modifier = Modifier.padding(start = Dimens.HorizontalPadding, top = Dimens.TopPadding)
    ) {
        Icon(imageVector = cardTypeIcon("LIST"), contentDescription = null)
    }
    // Title — independently conditional (PD-3) so a titleless CHECKBOX list with a completion
    // pill still renders no title, while the header row itself stays composed for the pill.
    if (titleSlotVisible(title)) {
        Text(
            text = title,
            style = TactileType.CardTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = Dimens.ChipToTitleGap,
                    top = Dimens.TopPadding,
                    bottom = Dimens.ContentSpacing
                )
        )
    }
    // Pin indicator
    if (isPinned) {
        Icon(
            imageVector = Icons.Filled.PushPin,
            contentDescription = "Pinned",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
    // Favorite indicator
    if (isFavorite) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Favourite",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
    }
    // Completion pill (FACE-02) — trailing-most element, gated on the one shared
    // listCompletionVisible predicate the body progress bar and this header gate also read.
    if (listCompletionVisible(subType, total)) {
        ListCompletionPill(
            completed = completed,
            total = total,
            accent = accent,
            modifier = Modifier.padding(start = Dimens.ContentSpacing, end = Dimens.HorizontalPadding)
        )
    }
}

// ---------------------------------------------------------------------------
// Completion pill + progress bar (Phase 132 FACE-02 DS-02) — CHECKBOX-only signal
// ---------------------------------------------------------------------------

/**
 * Single source of truth for whether a List card face shows any completion UI — the header
 * pill, the body progress bar, and the widened [ListCard] header gate (PD-3, 132-01-PLAN.md) all
 * read this one predicate so they can never drift apart or disagree. Reproduces the exact
 * predicate the outgoing footer completion text already used (RESEARCH Pitfall 4): an equality
 * check against the stored `"CHECKBOX"` token — case-sensitive, matching the pre-existing footer
 * gate exactly, never silently widened to a case-insensitive match — ANDed with a non-empty item
 * list so a zero-item list reserves no space (conditional-render-no-dead-space).
 *
 * `internal` rather than `private` for the same reason [voiceClipPillCopy] is: Metalava excludes
 * Kotlin `internal` from `api.txt`, so this widens no published surface while making the
 * predicate directly unit-testable without composing a card this module's Robolectric harness
 * cannot render.
 */
internal fun listCompletionVisible(subType: String, itemCount: Int): Boolean =
    subType == "CHECKBOX" && itemCount > 0

/**
 * Pure "N / M" completion pill copy builder (Phase 132 FACE-02) — a single fixed template with
 * no singular/plural branch (unlike [voiceClipPillCopy], whose copy genuinely pluralizes a
 * noun): renders identically at `0 / M` and `M / M`. `internal` for the same Metalava-exclusion
 * reason as [listCompletionVisible].
 */
internal fun listCompletionPillCopy(completed: Int, total: Int): String = "$completed / $total"

/**
 * Pure, divide-by-zero-guarded completion fraction (Phase 132 FACE-02, T-132-01-02) — returns
 * `0f` when [total] is not positive, otherwise `completed.toFloat() / total`. The guard is
 * load-bearing: an unguarded division would produce `NaN` and feed it to
 * [LinearProgressIndicator]. `internal` for the same Metalava-exclusion reason as
 * [listCompletionVisible].
 */
internal fun listCompletionFraction(completed: Int, total: Int): Float =
    if (total <= 0) 0f else completed.toFloat() / total

/**
 * "N / M" completion header pill (Phase 132 FACE-02 DS-02) — adapts [CountBadge]'s pill *shape*
 * (D-02) but NOT its colour formula. [CountBadge] derives its foreground via
 * [contrastingForeground] and its container as an 18%-alpha wash of that same foreground — a
 * formula built for the Home dashboard tile where the tile itself IS the accent colour. Reused
 * verbatim on a white card surface it resolves to white-on-white for any mid-to-dark accent —
 * invisible.
 *
 * ⚠ Deliberate deviation, mirroring [CardTypeChip]'s own "deliberate deviation" note: this pill
 * instead adopts [CardTypeChip]'s colour pairing — background via [accentTint] (or
 * `colorScheme.surfaceVariant` when [accent] is null), foreground at full accent strength (or
 * `colorScheme.onSurfaceVariant` when null) — so the two badges on the same untagged card render
 * identically, and a mid-to-dark accent stays legible on the white card surface. Never
 * force-unwraps [accent]; the null branch is a designed neutral state, not an error path
 * (T-132-01-03).
 */
@Composable
private fun ListCompletionPill(
    completed: Int,
    total: Int,
    accent: Color?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (accent != null) {
        accentTint(accent, colorScheme)
    } else {
        colorScheme.surfaceVariant
    }
    val foregroundColor = accent ?: colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = listCompletionPillCopy(completed, total),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = foregroundColor
        )
    }
}

/**
 * 4dp accent-tinted completion progress bar (Phase 132 FACE-02 DS-02) — a bare Material3
 * [LinearProgressIndicator] call, mirroring [MetricBar]'s exact call shape (D-02: [MetricBar] is
 * NOT used wholesale). Deliberately thinner than [MetricBar]'s 14dp — this is a compact
 * card-face signal, not a hero metric widget. `trackColor = colorScheme.outline`, not
 * `surfaceVariant`/`outlineVariant`: [MetricBar]'s own recorded finding is that on this fixed
 * palette those alternatives collapse against `surfaceVariant`/`surface` in one theme or the
 * other; only `outline` stays visibly distinct in BOTH color schemes. Never force-unwraps
 * [accent] — `color = accent ?: onSurfaceVariant` is the designed neutral fallback, mirroring
 * [ListCompletionPill]'s own null-safe colour pairing.
 */
@Composable
private fun ListCompletionProgressBar(
    fraction: Float,
    accent: Color?,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { fraction },
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        color = accent ?: MaterialTheme.colorScheme.onSurfaceVariant,
        trackColor = MaterialTheme.colorScheme.outline,
        strokeCap = StrokeCap.Round
    )
}

/**
 * Body content (completion progress bar + item list preview + category path) — extracted from
 * [ListCard] (DETEKT-02 pre-req refactor, 97-03; extended Phase 132 FACE-02).
 */
@Composable
private fun ListCardBodyContent(
    items: List<ListItemUiModel>,
    subType: String,
    categoryPath: String?,
    isExpanded: Boolean,
    titleAbsent: Boolean,
    accent: Color?
) {
    val displayedItems = if (isExpanded) items.take(10) else items.take(3)
    val completionVisible = listCompletionVisible(subType, items.size)

    Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        // COMPLETION PROGRESS BAR (FACE-02) — first child, ahead of the item rows; shares the
        // one listCompletionVisible gate with the header pill so they can never drift apart.
        if (completionVisible) {
            ListCompletionProgressBar(
                fraction = listCompletionFraction(items.count { it.isCompleted }, items.size),
                accent = accent,
                modifier = Modifier.padding(
                    start = Dimens.HorizontalPadding,
                    end = Dimens.HorizontalPadding,
                    top = Dimens.CompactPadding
                )
            )
        }

        // ITEM LIST — collapsed ≤3, expanded ≤10
        if (items.isEmpty()) {
            Text(
                text = "No items yet",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = Dimens.HorizontalPadding,
                    top = Dimens.ContentSpacing,
                    // D-04: 48dp end clearance so floating overlay doesn't occlude first line
                    end = if (titleAbsent) 48.dp else Dimens.HorizontalPadding,
                    bottom = Dimens.ContentSpacing
                )
            )
        } else {
            Column(
                modifier = Modifier.padding(
                    start = Dimens.HorizontalPadding,
                    // 12dp rhythm below the progress bar (FACE-02) when it's showing; the
                    // standard 4dp ContentSpacing rhythm otherwise — unchanged from before.
                    top = if (completionVisible) Dimens.CompactPadding else Dimens.ContentSpacing,
                    // D-04: 48dp end clearance so floating overlay doesn't occlude first line
                    end = if (titleAbsent) 48.dp else Dimens.HorizontalPadding,
                    bottom = Dimens.ContentSpacing
                )
            ) {
                displayedItems.forEachIndexed { index, item ->
                    ListItemPreviewRow(
                        item = item,
                        subType = subType,
                        index = index
                    )
                }
            }
        }

        // CATEGORY PATH
        if (categoryPath != null) {
            Text(
                text = categoryPath,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    horizontal = Dimens.HorizontalPadding,
                    vertical = Dimens.ContentSpacing
                )
            )
        }
    }
}

/**
 * Trailing icon cluster (expand/collapse, open-in-editor) — extracted from [ListCard]
 * (DETEKT-02 pre-req refactor, 97-03). The CHECKBOX completion signal that used to render here
 * moved to the header's [ListCompletionPill] in Phase 132 (FACE-02, RESEARCH Pitfall 4) — this
 * footer no longer reads `subType` or the completed-item count.
 */
@Composable
private fun ListCardFooterContent(
    items: List<ListItemUiModel>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onShowBottomSheet: () -> Unit
) {
    // Expand arrow: shown when content overflows at stage 0
    if (items.size > 3 && !isExpanded) {
        IconButton(onClick = { onExpandChange(true) }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand"
            )
        }
    }
    // Collapse arrow: shown when at stage 1
    if (isExpanded) {
        IconButton(onClick = { onExpandChange(false) }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Collapse"
            )
        }
    }
    // Open in editor — always shown
    IconButton(onClick = { onShowBottomSheet() }) {
        Icon(
            imageVector = Icons.Default.OpenInFull,
            contentDescription = "Open preview",
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Hosts the [ListCardBottomSheet] invocation — extracted from [ListCard] (DETEKT-02 pre-req
 * refactor, 97-03; pure line-count reduction, no branching moved).
 */
@Composable
private fun ListCardBottomSheetHost(
    title: String,
    items: List<ListItemUiModel>,
    subType: String,
    categoryPath: String?,
    createdAt: Long,
    updatedAt: Long,
    isPinned: Boolean,
    isFavorite: Boolean,
    onToggleItem: (itemId: String) -> Unit,
    onEdit: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onConfirmRename: (String) -> Unit,
    onDelete: () -> Unit,
    tagContent: (@Composable () -> Unit)?
) {
    ListCardBottomSheet(
        title = title,
        items = items,
        subType = subType,
        categoryPath = categoryPath, createdAt = createdAt, updatedAt = updatedAt,
        isPinned = isPinned,
        isFavorite = isFavorite,
        onToggleItem = onToggleItem,
        onEdit = { onEdit(); onDismissBottomSheet() },
        onDismiss = onDismissBottomSheet,
        onTogglePin = onTogglePin,
        onToggleFavorite = onToggleFavorite,
        onConfirmRename = onConfirmRename,
        onDelete = onDelete,
        tagContent = tagContent,
    )
}

/**
 * Inline rename [AlertDialog] — extracted from [ListCard] (DETEKT-02 pre-req refactor, 97-03).
 * Mirrors the original behavior exactly: the dialog is always dismissed on confirm, but
 * [onConfirm] only fires when the trimmed text is non-empty.
 */
@Composable
private fun ListCardRenameDialog(
    renameText: String,
    onRenameTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename") },
        text = {
            ClearableTextField(
                value = renameText,
                onValueChange = onRenameTextChange,
                singleLine = true,
                label = { Text("Title") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = renameText.trim()
                    if (trimmed.isNotEmpty()) {
                        onConfirm(trimmed)
                    }
                    onDismiss()
                }
            ) { Text("Rename") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Internal preview row for a single list item on the card face.
 * Read-only — checkboxes are not interactive (D-06).
 */
@Composable
private fun ListItemPreviewRow(
    item: ListItemUiModel,
    subType: String,
    index: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        when (subType) {
            "BULLETED" -> {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(16.dp)
                )
            }
            "ORDERED" -> {
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(16.dp)
                )
            }
            "CHECKBOX" -> {
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = if (item.isCompleted) "Checked" else "Unchecked",
                    modifier = Modifier.size(16.dp),
                    tint = if (item.isCompleted) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        Text(
            text = item.text,
            style = MaterialTheme.typography.bodySmall,
            color = if (subType == "CHECKBOX" && item.isCompleted) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textDecoration = if (subType == "CHECKBOX" && item.isCompleted) {
                TextDecoration.LineThrough
            } else {
                TextDecoration.None
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
    }
}
