package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp

/**
 * Read-only preview bottom sheet for text cards.
 *
 * Rides [SheetScaffold] (D-01 chrome canon) and renders its body via the shared [CardQuickView]
 * display archetype (D-04) — the sheet keeps its own title/pin/favorite/three-dot-menu header row
 * and category-path line (chrome [CardQuickView] deliberately omits, per Plan 02), then hands
 * [CardQuickView] a blank `title` (suppressing its internal duplicate header) so it owns the
 * tag row, body preview, and Created/Updated timestamps. Uses `skipPartiallyExpanded = true` for
 * full expand.
 *
 * @param title Card title.
 * @param content Full card body text (nullable).
 * @param categoryPath Breadcrumb path (nullable).
 * @param createdAt Creation timestamp in millis for display.
 * @param updatedAt Last update timestamp in millis for display.
 * @param isPinned Whether the card is pinned.
 * @param isFavorite Whether the card is favorited.
 * @param onEdit Called when the Edit button is tapped.
 * @param onDismiss Called when the sheet is dismissed.
 * @param onTogglePin Called when Pin/Unpin is selected.
 * @param onToggleFavorite Called when Favorite/Unfavorite is selected.
 * @param onDelete Called when Delete is selected.
 * @param onConfirmRename Called with the new title string when rename is confirmed.
 * @param tagContent Optional canonical tag-row slot (bare, no label), rendered between the
 *   header/category-path block and the body content. Filled by the `:app` caller with a live
 *   `TagChipEditor` — `:designsystem` cannot import it directly (ASSIGN-03).
 * @param imageCount IMG-02: caller-supplied number of inline images the card body contains.
 *   Defaulted to zero so every existing call site compiles and shows nothing. The consumer app
 *   computes the real value and binds it at Phase 109.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextCardBottomSheet(
    title: String,
    content: String?,
    categoryPath: String?,
    createdAt: Long,
    updatedAt: Long,
    isPinned: Boolean,
    isFavorite: Boolean,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onConfirmRename: (String) -> Unit,
    tagContent: (@Composable () -> Unit)? = null,
    imageCount: Int = 0
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember(title) { mutableStateOf(title) }

    SheetScaffold(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Header row: title + three-dot menu (CardQuickView doesn't own menu chrome — Plan 02)
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

                // Image-count indicator (IMG-02) — after Favourite, before the overflow control
                // so the overflow control remains the rightmost affordance on this surface.
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
                        // Rename — opens inline dialog
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = { showMenu = false; showRenameDialog = true },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
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

            if (categoryPath != null) {
                Text(
                    text = categoryPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // CardQuickView (D-04): title suppressed (blank) — the header row above already
            // rendered title/pin/favorite/menu; CardQuickView owns the tag row, body preview,
            // and Created/Updated timestamps.
            CardQuickView(
                title = "",
                createdAt = createdAt,
                updatedAt = updatedAt,
                tagContent = tagContent
            ) {
                if (!content.isNullOrBlank()) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = { onEdit(); onDismiss() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Edit note")
            }

            Spacer(modifier = Modifier.height(16.dp))
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

