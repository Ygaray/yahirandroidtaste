package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Namespace for [AttentionCue]'s style options and other defaults. */
object AttentionCueDefaults {
    /** [AttentionCue.style] options — an enum, never a boolean flag. */
    enum class Style { Inline, Dot }
}

/**
 * A caution/verify signal glyph — **never a failure signal**. `tint` defaults to
 * [androidx.compose.material3.ColorScheme.tertiary] and must never be wired to the error color
 * role: this component exists for the "double-check this value" case, not the "this is invalid"
 * case.
 *
 * Two visual variants:
 * - [AttentionCueDefaults.Style.Inline] — a small leading [icon] plus [text], for a specific
 *   field/row that deserves a second look.
 * - [AttentionCueDefaults.Style.Dot] — a bare colored dot with no text, a rolled-up "something in
 *   this group needs a second look" signal (renders no text at all, [text] is ignored).
 *
 * @param text Required for [AttentionCueDefaults.Style.Inline]; ignored for
 *   [AttentionCueDefaults.Style.Dot].
 */
@Composable
fun AttentionCue(
    text: String?,
    modifier: Modifier = Modifier,
    style: AttentionCueDefaults.Style = AttentionCueDefaults.Style.Inline,
    icon: ImageVector = Icons.Outlined.Info,
    tint: Color = MaterialTheme.colorScheme.tertiary,
    contentDescription: String = text.orEmpty(),
) {
    when (style) {
        AttentionCueDefaults.Style.Inline -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    // WR-03: null, not `contentDescription` — the adjacent Text already carries
                    // the same string, and a non-null description here made TalkBack announce it
                    // twice per row. The icon is decorative alongside visible text; the public
                    // contentDescription param is reserved for the Dot style (WR-02), which has
                    // no visible text to derive an announcement from.
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = text.orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = tint,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        AttentionCueDefaults.Style.Dot -> {
            Surface(
                // WR-02: apply contentDescription via semantics — previously silently dropped for
                // Dot, the one style where there's no visible text to derive an announcement from.
                modifier = modifier.size(10.dp).semantics {
                    if (contentDescription.isNotEmpty()) {
                        this.contentDescription = contentDescription
                    }
                },
                shape = MaterialTheme.shapes.small,
                color = tint,
                content = {},
            )
        }
    }
}
