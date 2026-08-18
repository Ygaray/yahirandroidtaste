package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.CardBase
import io.github.ygaray.yahirandroidtaste.component.titleSlotVisible
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.model.ListItemUiModel
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * List card face component with expand/collapse, swipe gestures, three-dot menu, and bottom sheet.
 *
 * Renders list items with sub-type indicators:
 * - BULLETED: bullet character prefix
 * - ORDERED: 1./2./3. numeric prefix
 * - CHECKBOX: read-only CheckBox/CheckBoxOutlineBlank icons (D-06)
 *
 * Expand/collapse: collapsed shows ≤3 items, expanded shows ≤10 items.
 * Footer: progress badge for CHECKBOX sub-type, OpenInFull → opens bottom sheet, expand arrow.
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
    onTagRemoveFromCard: ((tagId: String) -> Unit)? = null
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
                onRenameRequested = {
                    renameText = title
                    showRenameDialog = true
                }
            )
        },
        headerContent = if (!titleSlotVisible(title)) null else {
            { ListCardHeaderContent(title = title, isPinned = isPinned, isFavorite = isFavorite) }
        },
        bodyContent = {
            ListCardBodyContent(
                items = items,
                subType = subType,
                categoryPath = categoryPath,
                isExpanded = isExpanded,
                titleAbsent = titleAbsent
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
        // CardBase). The CHECKBOX progress badge is pre-existing card-face functionality kept
        // alongside the icon cluster (not called out by 52-UI-SPEC §5's icon-only wording, but
        // dropping it would be an unplanned regression — it self-suppresses for non-CHECKBOX
        // sub-types / empty lists, same as before).
        footerContent = {
            ListCardFooterContent(
                subType = subType,
                items = items,
                isExpanded = isExpanded,
                onExpandChange = { isExpanded = it },
                onShowBottomSheet = onShowBottomSheet
            )
        },
        modifier = modifier
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
    onRenameRequested: () -> Unit
) {
    // Rename
    DropdownMenuItem(
        text = { Text("Rename") },
        onClick = {
            dismissMenu()
            onRenameRequested()
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
 * Header row content (title + pin/favorite indicators) — extracted from [ListCard] (DETEKT-02
 * pre-req refactor, 97-03).
 */
@Composable
private fun RowScope.ListCardHeaderContent(
    title: String,
    isPinned: Boolean,
    isFavorite: Boolean
) {
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
}

/**
 * Body content (item list preview + category path) — extracted from [ListCard] (DETEKT-02
 * pre-req refactor, 97-03).
 */
@Composable
private fun ListCardBodyContent(
    items: List<ListItemUiModel>,
    subType: String,
    categoryPath: String?,
    isExpanded: Boolean,
    titleAbsent: Boolean
) {
    val displayedItems = if (isExpanded) items.take(10) else items.take(3)

    Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
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
                    top = Dimens.ContentSpacing,
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
 * Trailing icon cluster (progress badge, expand/collapse, open-in-editor) — extracted from
 * [ListCard] (DETEKT-02 pre-req refactor, 97-03).
 */
@Composable
private fun ListCardFooterContent(
    subType: String,
    items: List<ListItemUiModel>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onShowBottomSheet: () -> Unit
) {
    if (subType == "CHECKBOX" && items.isNotEmpty()) {
        Text(
            text = "${items.count { it.isCompleted }}/${items.size} done",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )
    }
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
