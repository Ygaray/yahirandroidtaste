package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Read-only preview bottom sheet for text cards.
 *
 * Rides [SheetScaffold] (D-01 chrome canon) and renders its body via the shared [CardQuickView]
 * display archetype (D-04) — the sheet delegates its title/pin/favorite/three-dot-menu header row
 * to the shared [SheetHeaderMenu] archetype (WO-2) and keeps its own category-path line (chrome
 * neither [CardQuickView] nor [SheetHeaderMenu] own, per Plan 02 / WO-2), then hands
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
 *   computes the real value and binds it at Phase 109. Forwarded to [SheetHeaderMenu].
 * @param onEditRequest EDIT-04: external trigger for the host-owned shared name-and-tags Edit
 *   sheet. When non-null, the three-dot "Edit" row invokes it (the host opens the tag-inclusive
 *   sheet, mirroring the already-shipped card-face [TextCard] pattern); when null (default), the
 *   row falls back to this sheet's local tag-less rename dialog, so every existing call site
 *   compiles and behaves as before. The consumer app wires it at Phase 115. Forwarded to
 *   [SheetHeaderMenu].
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
    imageCount: Int = 0,
    onEditRequest: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    SheetScaffold(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
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
                onEditRequest = onEditRequest,
                imageCount = imageCount
            )

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
}
