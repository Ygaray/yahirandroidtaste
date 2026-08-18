package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.TagManagementUiModel

/**
 * TagListItem — one row per tag (D-02, UI-SPEC §TagListItem).
 *
 * UIQ-07 / D-09: [tag] parameter changed from TagWithCardCount (DAO projection)
 * to [TagManagementUiModel] (UI model) — no DAO import in this composable.
 *
 * Phase 86 (GADGET-03): function-level extraction from `:app`'s
 * `feature/settings/tagmanagement/TagManagementScreen.kt` into
 * `:yahirandroidtaste/component/` — body verbatim, imports trimmed to only what this function
 * needs (a per-function pass, not a blind copy of the origin screen's shared import block).
 * [ICON_MAP] is already library-resident (`IconPickerGrid.kt`, same package here) so it needs
 * no import; [TagManagementUiModel] is co-moved alongside this file (86-RESEARCH.md Pitfall A).
 */
@Composable
fun TagListItem(
    tag: TagManagementUiModel,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${tag.cardCount} card${if (tag.cardCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = ICON_MAP[tag.iconName] ?: Icons.AutoMirrored.Filled.Label,
                contentDescription = null,
                tint = if (tag.isHome && tag.color != null)
                    Color(tag.color)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
