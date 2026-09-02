package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared sheet-chrome archetype (WO-2, `docs/COHERENCE-AUDIT.md` Finding S-1), extracted from the
 * byte-identical header-`Row` + three-dot `DropdownMenu` + rename `AlertDialog` duplicated across
 * `TextCardBottomSheet` and `ListCardBottomSheet` — the last non-trivial duplication the audit
 * flagged between the two near-duplicate sheets (both already shared their body region via
 * [CardQuickView], per D-04's precedent; this closes the remaining header/menu/dialog gap the same
 * way).
 *
 * Owns only the header `Row` (title + Pin/Favorite indicators + [ImageCountIndicator] + three-dot
 * menu trigger), the three-dot `DropdownMenu` (Edit -> Pin/Unpin -> Favorite/Unfavorite -> Delete),
 * and the rename `AlertDialog` triad — nothing else. It does **not** own either sheet's body
 * content, and it does **not** own the categoryPath line: that line sits between the header and
 * `CardQuickView` in each host sheet today, is not part of Finding S-1's cited duplication, and
 * (per the two sheets' pre-existing, divergent categoryPath color role) is explicitly out of this
 * extraction's boundary.
 *
 * `internal` + allowlisted in `ComponentRegistry.INTENTIONALLY_UNREGISTERED` (not independently
 * registered) — this is chrome extracted out of two already-registered sheets, not an
 * independently showcase-able visual archetype on its own.
 *
 * @param title Sheet title. The title `Text` is skipped entirely when blank — same guard as both
 *   host sheets carried before this extraction.
 * @param isPinned Whether the card is pinned — shows a pin indicator icon in the header.
 * @param isFavorite Whether the card is favorited — shows a star indicator icon in the header.
 * @param onTogglePin Called when Pin/Unpin is selected.
 * @param onToggleFavorite Called when Favorite/Unfavorite is selected.
 * @param onDelete Called when Delete is selected; fires immediately, no confirmation dialog.
 * @param onDismiss Called after Delete fires and after the rename dialog's confirm button
 *   (matching both sheets' pre-extraction behavior); NOT called when the rename dialog's cancel
 *   button is tapped.
 * @param onConfirmRename Called with the trimmed rename text when the rename dialog is confirmed
 *   with non-empty input; empty/whitespace-only input silently no-ops (existing trim guard).
 * @param onEditRequest EDIT-04: external trigger for the host-owned shared name-and-tags Edit
 *   sheet. When non-null, the three-dot "Edit" row invokes it (host opens the tag-inclusive sheet);
 *   when null (default), the row falls back to this composable's local tag-less rename dialog.
 * @param imageCount IMG-02: caller-supplied number of inline images. [ImageCountIndicator] is
 *   composed unconditionally — it already no-ops (renders nothing, reserves no space) when this is
 *   not positive, so `ListCardBottomSheet`'s call site simply omits this param to reproduce its
 *   current no-indicator appearance, with no new conditional branch here.
 * @param modifier Applied to the outer header [Row].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SheetHeaderMenu(
    title: String,
    isPinned: Boolean,
    isFavorite: Boolean,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmRename: (String) -> Unit,
    onEditRequest: (() -> Unit)? = null,
    imageCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember(title) { mutableStateOf(title) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Pin / favourite indicators
        if (isPinned) {
            Icon(
                imageVector = Icons.Filled.PushPin,
                contentDescription = "Pinned",
                modifier = Modifier
                    .padding(top = 4.dp, end = 4.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        if (isFavorite) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Favourite",
                modifier = Modifier
                    .padding(top = 4.dp, end = 4.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }

        // Image-count indicator (IMG-02) — after Favourite, before the overflow control so the
        // overflow control remains the rightmost affordance on this surface. Composed
        // unconditionally: ImageCountIndicator already no-ops at imageCount <= 0.
        ImageCountIndicator(
            imageCount = imageCount,
            modifier = Modifier.padding(top = 4.dp, end = 4.dp)
        )

        // Three-dot menu
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // region:edit-menu-item
                // Edit (EDIT-04) — external trigger to the host-owned shared name-and-tags sheet
                // when wired; otherwise falls back to the local tag-less rename AlertDialog.
                // TextListBottomSheetEditMenuSourceContractTest anchors on the region markers
                // below — they are load-bearing, not decorative.
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        if (onEditRequest != null) {
                            onEditRequest()
                            onDismiss()
                        } else {
                            renameText = title
                            showRenameDialog = true
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                // endregion:edit-menu-item
                // Pin/Unpin
                DropdownMenuItem(
                    text = { Text(if (isPinned) "Unpin" else "Pin") },
                    onClick = { showMenu = false; onTogglePin() },
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
                    onClick = { showMenu = false; onToggleFavorite() },
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
                    onClick = { showMenu = false; onDelete(); onDismiss() },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }

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
                TextButton(
                    onClick = {
                        val trimmed = renameText.trim()
                        if (trimmed.isNotEmpty()) {
                            onConfirmRename(trimmed)
                        }
                        showRenameDialog = false
                        onDismiss()
                    }
                ) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }
}
