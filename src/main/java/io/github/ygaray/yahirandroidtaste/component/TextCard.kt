package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.CardBase
import io.github.ygaray.yahirandroidtaste.component.bodySlotVisible
import io.github.ygaray.yahirandroidtaste.component.titleSlotVisible
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * Text note card composable with two-stage expand, reveal-then-confirm swipe gestures, a
 * dialog-based rename, and a three-dot overflow menu via [CardBase].
 *
 * ## Layout
 * - Single column, full-width card. Pin/favorite indicators shown in the header row.
 * - Three-dot menu in the top-right (from [CardBase]).
 *
 * ## Interactions
 * - Tap = open read-only bottom sheet
 * - Right swipe (reveals Edit button) → confirm = [onEdit] (navigate to content editor, D-05)
 * - Left swipe (reveals Delete button) → confirm = [onDelete] (soft-delete, D-04)
 * - Long press = open three-dot overflow menu (LongPress haptic via [CardBase])
 *
 * ## Expansion (two stages — D-03)
 * - Stage 0 (collapsed): body preview up to 3 lines, KeyboardArrowDown when overflow
 * - Stage 1 (expanded): body preview up to 10 lines, KeyboardArrowUp to collapse
 * - OpenInFull always shown — opens editor directly
 *
 * ## Rename
 * - Three-dot menu → Rename opens an [AlertDialog] with a [ClearableTextField].
 *   (Replaced the previous inline BasicTextField rename — Gate-1-verifiable UX change.)
 *
 * @param id Stable ID for keyed remember state.
 * @param title Card title.
 * @param content Card body text (nullable).
 * @param categoryPath Breadcrumb path string (nullable).
 * @param createdAt Creation timestamp in millis.
 * @param updatedAt Last update timestamp in millis.
 * @param isPinned Whether card is pinned.
 * @param isFavorite Whether card is favorited.
 * @param onEdit Called by the confirmed right-swipe Edit button and the OpenInFull footer button.
 * @param onDelete Called by the confirmed left-swipe Delete button and the Delete menu item.
 * @param onTogglePin Called by Pin/Unpin menu item.
 * @param onToggleFavorite Called by Favorite/Unfavorite menu item.
 * @param onConfirmRename Called with the new title string when rename dialog is confirmed.
 * @param showBottomSheet Whether this card's bottom sheet is currently open (lifted state, WR-06).
 * @param onShowBottomSheet Called when the card is tapped to request opening the bottom sheet.
 * @param onDismissBottomSheet Called when the bottom sheet is dismissed.
 * @param openRowState Single-open row reference hoisted at [CardListSection] (D-02).
 * @param modifier Optional outer modifier.
 * @param tagContent Optional canonical tag-row slot forwarded to the hosted [TextCardBottomSheet]
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
 * @param imageCount IMG-02: caller-supplied number of inline images the card body contains.
 *   Defaulted to zero so every existing call site compiles and shows nothing. Forwarded unchanged
 *   to the hosted [TextCardBottomSheet]. The consumer app computes the real value and binds it at
 *   Phase 109.
 * @param onEditRequest EDIT-01/EDIT-03: external trigger for the host-owned shared name-and-tags
 *   Edit sheet. When non-null, the three-dot "Edit" row invokes it (the host opens the tag-inclusive
 *   sheet, mirroring Voice); when null (default), the row falls back to this card's local tag-less
 *   rename dialog, so every existing call site compiles and behaves as before. The consumer app
 *   wires it at Phase 113.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextCard(
    id: String,
    title: String,
    content: String?,
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
    imageCount: Int = 0,
    onEditRequest: (() -> Unit)? = null
) {
    // Rename dialog state (replaces inline BasicTextField rename — UX change: now dialog-based)
    var showRenameDialog by remember(id) { mutableStateOf(false) }
    var renameText by remember(id) { mutableStateOf(title) }
    // UIQ-02 / D-05: keep renameText in sync with externally-changed title while dialog is closed.
    // The `if (!showRenameDialog)` guard prevents clobbering an in-progress edit.
    LaunchedEffect(title) { if (!showRenameDialog) renameText = title }

    // Two-stage expand (D-03): 0 = 3 lines, 1 = 10 lines
    var expansionLevel by remember(id) { mutableIntStateOf(0) }
    val maxLines = if (expansionLevel == 0) 3 else 10
    var textOverflows by remember(id) { mutableStateOf(false) }

    // Legibility guard (D-04): when title is absent the floating overlay occupies the top-end
    // corner; give the first body element 48dp end clearance so the first line is not occluded.
    val titleAbsent = !titleSlotVisible(title)

    CardBase(
        showThreeDot = true,
        onDeleteClick = onDelete,       // D-04: left swipe → Delete
        onEditClick = onEdit,           // D-05: right swipe → content editor
        openRowState = openRowState,
        onClick = onShowBottomSheet,
        dropdownMenuContent = { dismissMenu ->
            // Edit (EDIT-01/EDIT-03) — external trigger to the host-owned shared name-and-tags
            // sheet when wired; otherwise falls back to the local tag-less rename AlertDialog.
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    dismissMenu()
                    if (onEditRequest != null) {
                        onEditRequest()
                    } else {
                        renameText = title
                        showRenameDialog = true
                    }
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
            // Delete — last, error color (D-11)
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
                // Title
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
                // Image-count indicator (IMG-02) — leading spacer only, additive; does not
                // touch the existing (unspaced) Pin<->Favourite gap. Both the spacer and the
                // indicator are gated on a positive count so nothing at all composes and no
                // space is reserved at zero (conditional-render-no-dead-space).
                if (imageCount > 0) {
                    Spacer(modifier = Modifier.width(Dimens.ContentSpacing))
                    ImageCountIndicator(imageCount = imageCount)
                }
            }
        },
        bodyContent = if (!bodySlotVisible(content)) null else {
            {
            // Wrap in animating Column for smooth expand/collapse height transitions (D-03)
            Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                // CONTENT PREVIEW
                // IN-01: inner isNullOrBlank guard removed — bodyContent is only reached
                // when bodySlotVisible(content) is true, which guarantees non-blank content.
                Text(
                    text = content!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { result -> textOverflows = result.hasVisualOverflow },
                    modifier = Modifier.padding(
                        start = Dimens.HorizontalPadding,
                        top = Dimens.ContentSpacing,
                        // D-04: 48dp end clearance so floating overlay doesn't occlude first line
                        end = if (titleAbsent) 48.dp else Dimens.HorizontalPadding,
                        bottom = Dimens.ContentSpacing
                    )
                )

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
        // WR-01: footer extracted from bodyContent so it renders when body is blank/null
        // (UI-SPEC card-state matrix: header+footer must remain for a title-only text card).
        // G2-01/D-05: emits ONLY the trailing icon cluster — no inner fillMaxWidth/SpaceBetween
        // wrapper — so it slots directly into CardBase's combined bottom row (tags leading,
        // footer + the single relocated MoreVert trailing, one Row, SpaceBetween owned by
        // CardBase). Expand/collapse arrows self-suppress via textOverflows/expansionLevel guards.
        footerContent = {
            // Expand arrow: shown when content overflows at stage 0
            if (expansionLevel == 0 && textOverflows) {
                IconButton(onClick = { expansionLevel = 1 }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand"
                    )
                }
            }
            // Collapse arrow: shown when at stage 1
            if (expansionLevel == 1) {
                IconButton(onClick = { expansionLevel = 0 }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Collapse"
                    )
                }
            }
            // Open in editor — always shown
            IconButton(onClick = { onEdit() }) {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = "Open editor",
                    modifier = Modifier.size(16.dp)
                )
            }
        },
        modifier = modifier
    )

    // Rename dialog (replaces inline BasicTextField — same result, modal instead of inline)
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename") },
            text = {
                ClearableTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Title") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = renameText.trim()
                    if (trimmed.isNotEmpty()) { onConfirmRename(trimmed) }
                    showRenameDialog = false
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Bottom sheet — only composed when shown; state is lifted to CardListSection (WR-06)
    if (showBottomSheet) {
        TextCardBottomSheet(
            title = title,
            content = content,
            categoryPath = categoryPath,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isPinned = isPinned,
            isFavorite = isFavorite,
            onEdit = onEdit,
            onDismiss = onDismissBottomSheet,
            onTogglePin = onTogglePin,
            onToggleFavorite = onToggleFavorite,
            onDelete = onDelete,
            onConfirmRename = onConfirmRename,
            tagContent = tagContent,
            imageCount = imageCount
        )
    }
}
