package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.ListItemUiModel

/**
 * Bottom sheet for list cards — shows full scrollable item list with interactive checkboxes.
 *
 * Rides [SheetScaffold] (D-01 chrome canon) and renders its body via the shared [CardQuickView]
 * display archetype (D-04) — the sheet keeps its own title/pin/favorite/three-dot-menu header row
 * and category-path line (chrome [CardQuickView] deliberately omits, per Plan 02), then hands
 * [CardQuickView] a blank `title` (suppressing its internal duplicate header) so it owns the
 * tag row, item-list body, and Created/Updated timestamps.
 *
 * Uses Column + verticalScroll (NOT LazyColumn) to avoid scroll conflicts inside the sheet
 * (RESEARCH Pitfall 7).
 *
 * Interactive checkboxes are only active for the CHECKBOX sub-type (D-11).
 * BULLETED and ORDERED sub-types render read-only prefix indicators.
 *
 * @param title Card title displayed in header.
 * @param items Full list of items (no preview truncation).
 * @param subType "BULLETED" | "ORDERED" | "CHECKBOX"
 * @param categoryPath Breadcrumb path (nullable).
 * @param createdAt Creation timestamp in millis, rendered by [CardQuickView].
 * @param updatedAt Last update timestamp in millis, rendered by [CardQuickView].
 * @param isPinned Whether card is pinned.
 * @param isFavorite Whether card is favorited.
 * @param onToggleItem Called with itemId when a CHECKBOX item is toggled.
 * @param onEdit Called when "Edit list" button is tapped.
 * @param onDismiss Called when sheet is dismissed.
 * @param onTogglePin Called by Pin/Unpin menu item.
 * @param onToggleFavorite Called by Favorite/Unfavorite menu item.
 * @param onConfirmRename Called with new title when rename is confirmed.
 * @param onDelete Called by Delete menu item.
 * @param onMoveTo Optional — enables "Move to" when non-null (Phase 7).
 * @param tagContent Optional canonical tag-row slot (bare, no label), rendered between the
 *   header/category-path block and the list body. Filled by the `:app` caller with a live
 *   `TagChipEditor` — `:designsystem` cannot import it directly (ASSIGN-03).
 * @param onEditRequest EDIT-04: external trigger for the host-owned shared name-and-tags Edit
 *   sheet. When non-null, the three-dot "Edit" row invokes it (the host opens the tag-inclusive
 *   sheet, mirroring the already-shipped card-face [ListCard] pattern); when null (default), the
 *   row falls back to this sheet's local tag-less rename dialog, so every existing call site
 *   compiles and behaves as before. The consumer app wires it at Phase 115.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCardBottomSheet(
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
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onConfirmRename: (String) -> Unit,
    onDelete: () -> Unit,
    tagContent: (@Composable () -> Unit)? = null,
    onEditRequest: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember(title) { mutableStateOf(title) }

    SheetScaffold(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        // CRITICAL (RESEARCH Pitfall 7): Column + verticalScroll, NOT LazyColumn
        // Nested LazyColumn inside a bottom sheet causes scroll conflicts
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Header row: title + pin/fav indicators + three-dot menu
            Row(
                modifier = Modifier
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
                        // Edit (EDIT-04) — external trigger to the host-owned shared
                        // name-and-tags sheet when wired; otherwise falls back to the local
                        // tag-less rename AlertDialog. TextListBottomSheetEditMenuSourceContractTest
                        // anchors on the region markers below — they are load-bearing, not decorative.
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

            // Category path
            if (categoryPath != null) {
                Text(
                    text = categoryPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // CardQuickView (D-04): title suppressed (blank) — the header row above already
            // rendered title/pin/favorite/menu; CardQuickView owns the tag row, item-list body,
            // and Created/Updated timestamps.
            CardQuickView(
                title = "",
                createdAt = createdAt,
                updatedAt = updatedAt,
                tagContent = tagContent
            ) {
                // Item rows — forEachIndexed for full list (no truncation in bottom sheet)
                items.forEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        when (subType) {
                            "CHECKBOX" -> {
                                // Interactive checkbox per D-11
                                Checkbox(
                                    checked = item.isCompleted,
                                    onCheckedChange = { onToggleItem(item.id) },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            "ORDERED" -> {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(24.dp)
                                )
                            }
                            "BULLETED" -> {
                                Text(
                                    text = "\u2022",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(24.dp)
                                )
                            }
                        }

                        Text(
                            text = item.text,
                            style = if (subType == "CHECKBOX" && item.isCompleted) {
                                MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.LineThrough
                                )
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            color = if (subType == "CHECKBOX" && item.isCompleted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        )
                    }
                }
            }

            // "Edit list" button — navigates to editor
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Edit list")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Rename dialog
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
