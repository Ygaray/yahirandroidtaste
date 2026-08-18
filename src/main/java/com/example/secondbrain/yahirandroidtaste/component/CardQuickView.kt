package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.secondbrain.yahirandroidtaste.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared display (non-edit) archetype (D-04), extracted from the common structure of
 * `TextCardBottomSheet` and `ListCardBottomSheet`: a title/pin/favorite header, an optional
 * tag-row slot, the type-specific [body] content, and creation/update timestamps.
 *
 * Content-only — this archetype renders NO `SheetScaffold`/`ModalBottomSheet` chrome; adopters
 * own the sheet chrome and any menu/action affordances (Plan 08 adopts this on all 4 display
 * sheets: Text/List/Album/Voice quick-view).
 *
 * D-10 (conditional render, no dead space): the title region self-suppresses when [title] is
 * blank (mirrors [titleSlotVisible]/`CardFaceSlots`' discipline), and [tagContent] is rendered
 * only when non-null — callers pass `null`, not an empty composable, when there is no tag content
 * to show.
 *
 * @param title Card title. Header row (title + pin/favorite indicators) is skipped entirely when
 *   blank.
 * @param createdAt Creation timestamp in millis, shown at the bottom of the content.
 * @param updatedAt Last-update timestamp in millis, shown at the bottom of the content.
 * @param isPinned Whether the card is pinned — shows a pin indicator icon in the header.
 * @param isFavorite Whether the card is favorited — shows a star indicator icon in the header.
 * @param tagContent Optional canonical tag-row slot (bare, no label), rendered between the header
 *   and [body] only when non-null.
 * @param modifier Applied to the outer [Column].
 * @param body Type-specific content slot (text preview vs. list items).
 */
@Composable
fun CardQuickView(
    title: String,
    createdAt: Long,
    updatedAt: Long,
    isPinned: Boolean = false,
    isFavorite: Boolean = false,
    tagContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    body: @Composable ColumnScope.() -> Unit
) {
    val timestampFormatter = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.HorizontalPadding, vertical = Dimens.TopPadding)
    ) {
        if (titleSlotVisible(title)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
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
                            .padding(top = 4.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (tagContent != null) {
            tagContent()
            Spacer(modifier = Modifier.height(8.dp))
        }

        body()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Created: ${timestampFormatter.format(Date(createdAt))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Updated: ${timestampFormatter.format(Date(updatedAt))}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
