package io.github.ygaray.yahirandroidtaste.component

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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.ListItemUiModel

/**
 * Bottom sheet for list cards — shows full scrollable item list with interactive checkboxes.
 *
 * Rides [SheetScaffold] (D-01 chrome canon) and renders its body via the shared [CardQuickView]
 * display archetype (D-04) — the sheet delegates its title/pin/favorite/three-dot-menu header row
 * to the shared [SheetHeaderMenu] archetype (WO-2) and keeps its own category-path line (chrome
 * neither [CardQuickView] nor [SheetHeaderMenu] own, per Plan 02 / WO-2), then hands
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
 *   compiles and behaves as before. The consumer app wires it at Phase 115. Forwarded to
 *   [SheetHeaderMenu].
 * @param readOnlyPreview LIST-04: when true, the CHECKBOX item-row branch renders a static,
 *   non-interactive check icon instead of a live [Checkbox] — the toggle callback ([onToggleItem])
 *   is never wired into this branch. Used by callers presenting a read-only truncated preview
 *   (e.g. a tap-to-open sheet showing a 3-item prefix) rather than the full editable list.
 *   Defaults to false so every existing call site keeps its live, interactive checkbox.
 * @param previewOverflowCount LIST-04: the number of items the CALLER truncated away before
 *   passing [items] to this composable — this component never truncates on its own (the caller
 *   owns how many items to pass). When greater than zero, a "+N more" hint renders below the
 *   item rows; a value of zero (the default) renders nothing.
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
    onEditRequest: (() -> Unit)? = null,
    readOnlyPreview: Boolean = false,
    previewOverflowCount: Int = 0
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            SheetHeaderMenu(
                title = title,
                isPinned = isPinned,
                isFavorite = isFavorite,
                onTogglePin = onTogglePin,
                onToggleFavorite = onToggleFavorite,
                onDelete = onDelete,
                onDismiss = onDismiss,
                onConfirmRename = onConfirmRename,
                onEditRequest = onEditRequest
            )

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
                    ListPreviewItemRow(
                        item = item,
                        subType = subType,
                        index = index,
                        readOnlyPreview = readOnlyPreview,
                        onToggleItem = onToggleItem
                    )
                }
                // region:preview-overflow-hint
                if (previewOverflowCount > 0) {
                    Text(
                        text = "+$previewOverflowCount more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // endregion:preview-overflow-hint
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
}

/**
 * Single item row for [ListCardBottomSheet]'s item list — extracted from the parent composable
 * to keep it under the module's LongMethod/LongParameterList detekt thresholds (Phase 118 Task 1
 * step 4). Renders the subType-specific leading indicator (checkbox/number/bullet) plus the item
 * text, matching the pre-extraction inline behavior exactly.
 *
 * @param readOnlyPreview LIST-04: when true, the CHECKBOX branch renders a static, non-interactive
 *   check [Icon] instead of a live [Checkbox] — [onToggleItem] is never invoked from this branch.
 */
@Composable
private fun ListPreviewItemRow(
    item: ListItemUiModel,
    subType: String,
    index: Int,
    readOnlyPreview: Boolean,
    onToggleItem: (itemId: String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        // region:readonly-item-row
        when (subType) {
            "CHECKBOX" -> {
                if (readOnlyPreview) {
                    // Static, non-interactive indicator (LIST-04) — no item-toggle callback wired
                    Icon(
                        imageVector = if (item.isCompleted) {
                            Icons.Default.CheckBox
                        } else {
                            Icons.Default.CheckBoxOutlineBlank
                        },
                        contentDescription = if (item.isCompleted) "Checked" else "Unchecked",
                        modifier = Modifier.size(16.dp),
                        tint = if (item.isCompleted) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                } else {
                    // Interactive checkbox per D-11
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { onToggleItem(item.id) },
                        modifier = Modifier.size(24.dp)
                    )
                }
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
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(24.dp)
                )
            }
        }
        // endregion:readonly-item-row

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
