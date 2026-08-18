package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Shared chip composable — single source of truth for all non-Material-FilterChip chip styling
 * across the app (UIQ-05 zero-drift / D-07).
 *
 * Visual invariants (UIQ-01, D-04, UI-SPEC):
 * - Drawn height: 32dp — inner Surface keeps [Modifier.height(32.dp)]; never changes.
 * - Touch target: ≥ 48dp — outer Box carries [Modifier.minimumInteractiveComponentSize()].
 *   The interaction area is LARGER than the drawn chip; this is intentional (D-04).
 * - Corner radius: [RoundedCornerShape(8.dp)]
 * - Selected: [MaterialTheme.colorScheme.secondaryContainer] fill, no border.
 * - Unselected: [MaterialTheme.colorScheme.surface] fill + 1dp [MaterialTheme.colorScheme.outline] border.
 * - Label text: [MaterialTheme.typography.labelLarge]
 * - Horizontal content padding: 12dp for label-only chips; 8dp when a leading or trailing icon is present.
 *
 * Colors are M3 theme roles only — never hard-coded (D-01/D-02: theme frozen, no hex literals here).
 *
 * @param label      Text to display on the chip. May be empty when showing an icon-only chip (e.g.
 *                   the Home or Add chips in [CategoryChipBar]).
 * @param isSelected Whether the chip is in the filled/selected state.
 * @param onClick    Called when the chip is tapped. Applied to the inner Surface so that
 *                   callers can add extra pointer-input modifiers (e.g. a drag handle) to the
 *                   outer [modifier] without gesture conflicts.
 * @param modifier   Modifier applied to the outer Box (the 48dp touch-target container). Callers
 *                   may append drag handles, semantics, or other pointer-input modifiers here.
 * @param leadingIcon Optional composable rendered before the label (e.g. Home icon, Add icon).
 *                    When non-null, horizontal padding shrinks from 12dp to 8dp.
 * @param trailingIcon Optional composable rendered after the label (e.g. expand/collapse arrow).
 *                    When non-null, horizontal padding shrinks from 12dp to 8dp.
 * @param relatednessStrength Optional normalized Jaccard relatedness score (Phase 91,
 *                    VISUAL-01/02/03). When non-null and `!isSelected`, encodes the value via
 *                    [relatednessVisual] into container/content color, border-stroke width, and
 *                    label font weight. Ignored entirely when `isSelected == true` — the
 *                    isSelected branch always wins (D-02 non-negotiable). Defaults to `null`, so
 *                    every existing call site renders byte-identical to today.
 * @param onLongClick Optional long-press callback (Phase 93, TMENU-01). When non-null, the inner
 *                    Surface's gesture switches from a plain [clickable] to a [combinedClickable]
 *                    that fires [onClick] on tap and [onLongClick] on long-press — the same
 *                    Surface, no separate gesture layer. Defaults to `null`, so every existing
 *                    call site keeps its plain-`clickable` gesture and renders byte-identical to
 *                    today (D-04 backward compatibility).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    relatednessStrength: Float? = null,
    onLongClick: (() -> Unit)? = null
) {
    val relatedness = if (!isSelected && relatednessStrength != null)
        relatednessVisual(relatednessStrength, MaterialTheme.colorScheme)
    else null

    val shape = RoundedCornerShape(8.dp)
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        relatedness != null -> relatedness.containerColor
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
        relatedness != null -> relatedness.contentColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderStroke = when {
        isSelected -> null
        relatedness != null -> relatedness.borderStroke
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }
    val labelFontWeight = relatedness?.fontWeight
    val hasIcon = leadingIcon != null || trailingIcon != null
    val horizontalPadding = if (hasIcon) 8.dp else 12.dp

    // Outer Box: reserves ≥ 48dp touch target (UIQ-01 / D-04) without growing the visible chip.
    // Callers append drag handles or semantics to [modifier], which lands here on the touch layer.
    Box(
        modifier = modifier.minimumInteractiveComponentSize(),
        contentAlignment = Alignment.Center
    ) {
        // Inner Surface: 32dp drawn height only — click is wired here so drag-handle
        // modifiers on the outer Box don't conflict with the tap gesture.
        Surface(
            modifier = Modifier
                .height(32.dp)
                .clip(shape)
                .let { base ->
                    if (onLongClick != null) {
                        base.combinedClickable(onClick = onClick, onLongClick = onLongClick)
                    } else {
                        base.clickable(onClick = onClick)
                    }
                },
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = borderStroke
        ) {
            Row(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.invoke()
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = labelFontWeight ?: MaterialTheme.typography.labelLarge.fontWeight
                    ),
                    // A chip is single-line by construction: the inner Surface is a fixed 32dp,
                    // so a wrapped 2-line label clips vertically. When the card-face tag row
                    // (CardTagRow) squeezes the last of its 1–3 chips below its content width,
                    // truncate with an ellipsis instead of wrapping — preserving UI-SPEC §5's
                    // "the row must stay single-line" invariant (G2-01 gap fix).
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                trailingIcon?.invoke()
            }
        }
    }
}
