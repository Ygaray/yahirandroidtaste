package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.automirrored.filled.Label
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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.CardBase
import io.github.ygaray.yahirandroidtaste.component.titleSlotVisible
import io.github.ygaray.yahirandroidtaste.model.TagChipUiModel
import io.github.ygaray.yahirandroidtaste.modifier.SwipeAnchor
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * Album card face for the module card list (list-context).
 *
 * Wraps [CardBase] with:
 * - Title header row with pin/favorite indicators and three-dot menu (Edit/Pin/Fav/Delete)
 * - Full-width [AdaptiveMediaPreview] at fixed 196dp height (D-05, D-06)
 * - Optional category path footer
 *
 * Three-dot menu per D-08: Edit, Pin/Unpin, Favorite/Unfavorite, Delete (plus a guarded "Edit
 * tags" row, retained but suppressed by :app via onEditTags = null for the unified-Edit presentation).
 * NO "Show hidden images" — access is inside AlbumViewerScreen.
 *
 * Edit uses the shared [AlbumTitleConfirmSheet] (rename mode) hosted by the screen — this card
 * only emits [onRename]; the host opens the sheet with title + category. There is no card-local
 * rename dialog.
 *
 * Swipe behaviors (via [CardBase] + [SwipeableActionRow], reveal-then-confirm):
 * - Right swipe reveals the Edit button → tapping it triggers [onRename] (opens the rename sheet, D-05).
 * - Left swipe reveals the Delete button → tapping it triggers [onDelete] (D-04).
 *
 * @param id Stable card ID used for keyed remember state.
 * @param title Card title.
 * @param isPinned Whether card is pinned.
 * @param isFavorite Whether card is favorited.
 * @param thumbnailItems List of [MediaThumbnailCell]s for the thumbnail strip (visible images only).
 * @param totalImageCount Real total number of images in the album (including those not in
 *   [thumbnailItems]). Used to compute the correct "+N" overflow label. Defaults to
 *   [thumbnailItems.size] for callers that don't have overflow.
 * @param categoryPath Optional category breadcrumb.
 * @param onTap Navigate to AlbumViewerScreen.
 * @param onDelete Soft-delete this card.
 * @param onRename Request the shared rename sheet for this album (title + category).
 * @param onTogglePin Toggle pin state.
 * @param onToggleFavorite Toggle favorite state.
 * @param onEditTags Optional — invoked when the "Edit tags" overflow item is tapped (D-02).
 * @param onManageLinks Optional — invoked when the "Manage links" overflow item is tapped (D-04).
 *   Opens [RelationshipPanelSheet] hosted in ModuleScreen.
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
fun AlbumCard(
    id: String,
    title: String,
    isPinned: Boolean,
    isFavorite: Boolean,
    thumbnailItems: List<MediaThumbnailCell>,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    openRowState: MutableState<AnchoredDraggableState<SwipeAnchor>?>,
    modifier: Modifier = Modifier,
    totalImageCount: Int = thumbnailItems.size,
    categoryPath: String? = null,
    onEditTags: (() -> Unit)? = null,
    onManageLinks: (() -> Unit)? = null,
    tags: List<TagChipUiModel> = emptyList(),
    onTagClick: (tagId: String) -> Unit = {},
    onSiblingsClick: (allTagIds: List<String>) -> Unit = {},
    onCloseSiblingsClick: (cardId: String) -> Unit = {},
    onTagEdit: ((tagId: String) -> Unit)? = null,
    onTagDelete: ((tagId: String, name: String) -> Unit)? = null,
    onTagRemoveFromCard: ((tagId: String) -> Unit)? = null
) {
    CardBase(
        showThreeDot = true,
        onDeleteClick = onDelete,
        onEditClick = onRename,
        openRowState = openRowState,
        onClick = onTap,
        dropdownMenuContent = { dismissMenu ->
            // Edit — opens the shared AlbumTitleConfirmSheet (rename mode) hosted by the screen.
            // EDIT-01: relabelled "Rename" -> "Edit"; the guarded "Edit tags" row below is retained
            // and suppressed by :app (onEditTags = null) so the menu shows one unified "Edit" action.
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    dismissMenu()
                    onRename()
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
            // Edit tags — opens TagEditSheet (D-02, UI-SPEC §6; before Delete).
            // Only rendered when a handler is wired — avoids a silent dead menu item.
            if (onEditTags != null) {
                DropdownMenuItem(
                    text = { Text("Edit tags") },
                    onClick = {
                        dismissMenu()
                        onEditTags()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Label,
                            contentDescription = "Edit tags"
                        )
                    }
                )
            }
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
            // Delete — error color
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    dismissMenu()
                    onDelete()
                },
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
        },
        bodyContent = {
            AdaptiveMediaPreview(
                cells = thumbnailItems,
                totalImageCount = totalImageCount,
                onCellTap = { onTap() },
                onOverflowTap = onTap,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp)
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
        // G2-01/D-05: net-new trailing icon cluster (AlbumCard previously only used footerContent
        // for the optional categoryPath label) — adds a single OpenInFull open-editor icon wired
        // to onEditTags (guarded: nullable callback), preserving the existing categoryPath label
        // ahead of it. No expand/collapse arrow (Album has none) and no second MoreVert — CardBase
        // supplies the one trailing ⋮ via showThreeDot. Resulting trailing cluster: [✎][⋮].
        footerContent = {
            if (categoryPath != null) {
                Text(
                    text = categoryPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            IconButton(onClick = { onEditTags?.invoke() }) {
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
