package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import io.github.ygaray.yahirandroidtaste.theme.ContentTone
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.ToneDefaults

/** Duration of the chevron rotation, the dim fade and the expanded-content expand and collapse. */
internal const val RECEIPT_ROW_MOTION_MS = 200

/** Chevron rotation, in degrees, of an expanded row (collapsed is 0). */
internal const val RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES = 180f

/** Alpha of a dimmed row's summary line; the same 38% the other disabled atoms use. */
internal const val RECEIPT_ROW_DIMMED_ALPHA = DISABLED_CONTENT_ALPHA

/** Animated chevron rotation for [ReceiptRow]; a seam so the timing can be tested on its own. */
@Composable
internal fun rememberReceiptRowChevronRotation(expanded: Boolean): State<Float> =
    animateFloatAsState(
        targetValue = if (expanded) RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES else 0f,
        animationSpec = tween(RECEIPT_ROW_MOTION_MS),
        label = "receipt_row_chevron"
    )

/** Animated summary-line alpha for [ReceiptRow]; a seam so the timing can be tested on its own. */
@Composable
internal fun rememberReceiptRowAlpha(dimmed: Boolean): State<Float> =
    animateFloatAsState(
        targetValue = if (dimmed) RECEIPT_ROW_DIMMED_ALPHA else 1f,
        animationSpec = tween(RECEIPT_ROW_MOTION_MS),
        label = "receipt_row_alpha"
    )

/**
 * Makes a single line exactly as tall as its `lineHeight`. Without this, Compose's default line
 * height style trims the space above the first line and below the last one, so a one-line text can
 * be shorter than its `lineHeight` and the row's fixed-height anatomy would not add up. Applied to
 * [ReceiptRow] text only, never to the theme typography.
 */
internal fun TextStyle.receiptRowLine(): TextStyle = copy(
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

/** Resolves a [ContentTone] to a concrete color. [warn] and [error] supply the tinted tones. */
internal fun receiptRowToneColor(
    tone: ContentTone,
    scheme: ColorScheme,
    warn: ToneColors,
    error: ToneColors
): Color = when (tone) {
    ContentTone.Default -> scheme.onSurface
    ContentTone.Muted -> scheme.onSurfaceVariant
    ContentTone.Warn -> warn.content
    ContentTone.Error -> error.content
}

/**
 * A stateless list row that reads as one accessible button: a title with an optional subtitle,
 * supporting line and attention cue, an end-aligned value with an optional caption, an optional
 * expand chevron, an optional trailing action and optional full-width expanded content.
 *
 * **Anatomy.** The summary line is a title column (takes the leftover width: title, subtitle,
 * [supportingContent], cue pill), a value column (at least 56dp wide, end-aligned: [value] then
 * [valueCaption]), a chevron, then [trailingAction], with 8dp between the pieces and 12dp vertical
 * padding. With a title, subtitle, supporting line and value, and no cue, a collapsed row is 92dp
 * tall (72dp without the supporting line); the cue adds a 4dp gap and a 24dp pill. The chevron is
 * shown only when [onClick] is set and there is no [trailingAction].
 *
 * **Overflow.** The title, subtitle and value are single-line and ellipsize. The value column keeps
 * its natural width and the title takes what is left, so when they collide the value wins and the
 * title ellipsizes. Pass short, already-formatted values: a value wider than the row squeezes the
 * title to nothing and can squeeze the chevron.
 *
 * **Semantics.** The summary is ONE merged node (tag `receipt_row_summary`) holding the title,
 * subtitle, value, caption and cue text. With [onClick] it is a button
 * ([Role.Button]) carrying [onClickLabel] and a state description equal to [expandedStateLabel] or
 * [collapsedStateLabel]; with `onClick = null` it has no role and no state description.
 * [trailingAction] (tag `receipt_row_trailing_action`) is a sibling of the merged summary and
 * [expandedContent] (tag `receipt_row_expanded_content`) sits below it, so both are separate
 * semantics nodes; tapping the trailing action never invokes [onClick].
 *
 * **Tags.** `receipt_row_summary`, `receipt_row_trailing_action` and `receipt_row_expanded_content`
 * are stable. `receipt_row_chevron`, `receipt_row_value_column` and `receipt_row_cue` are test-only
 * hooks and may change. The tags live on inner nodes, so a `Modifier.testTag` passed in [modifier]
 * lands on the root and does not replace them. The library never exposes tags as resource ids.
 *
 * **State.** The row is stateless: it remembers nothing. Keep [expanded] (and any single-open
 * accordion rule) in the caller.
 *
 * **Tones.** [ContentTone.Default] is `onSurface`, [ContentTone.Muted] is `onSurfaceVariant`,
 * [ContentTone.Warn] is [warnTone]'s content color and [ContentTone.Error] resolves through
 * [ToneDefaults.error]. The default [warnTone] text is near-black in the hub's light palette; pass
 * your own [ToneColors] for an amber cue.
 *
 * Text must be static UI copy, never sensitive text: it reaches accessibility services through the
 * merged node. The state labels default to English; callers localize them.
 *
 * @param title Primary line. Single line, ellipsized.
 * @param modifier Applied to the root column.
 * @param subtitle Optional second line. Single line, ellipsized.
 * @param subtitleTone Tone of [subtitle].
 * @param supportingContent Optional third line, rendered in `bodySmall` with `onSurfaceVariant` as the
 *   default content color. A `Text` that sets its own `style` replaces the row's line-height
 *   normalization and makes the row about 2dp shorter than 92dp, so omit `style` and let the row
 *   supply `bodySmall`.
 * @param value Optional end-aligned value. Single line, ellipsized.
 * @param valueCaption Optional small line under [value].
 * @param valueTone Tone of [value].
 * @param titleTone Tone of [title].
 * @param attentionCue Optional cue shown as a full-radius pill under the title column.
 * @param warnTone Container and content colors of the cue pill and of [ContentTone.Warn].
 * @param expanded Whether [expandedContent] is shown and the chevron points up.
 * @param onClick Click handler of the summary; `null` makes the summary non-interactive.
 * @param onClickLabel Accessibility label of the click action.
 * @param expandedStateLabel State description announced while [expanded].
 * @param collapsedStateLabel State description announced while collapsed.
 * @param trailingAction Optional action at the end of the summary line, outside the merged node.
 * @param dimmed Dims the summary (not [trailingAction]) to 38% and strikes through the title and
 *   subtitle. It is a visual state only: it neither blocks [onClick] nor adds anything to the
 *   semantics, so a caller that needs to tell screen-reader users the row is dimmed must say so in
 *   its own [onClickLabel] or text, and should drop [onClick] and [trailingAction] on a dimmed row
 *   that must not be interactive.
 * @param showDivider Adds a 1dp hairline under the row.
 * @param expandedContent Optional content shown full width below the summary while [expanded].
 */
@Composable
fun ReceiptRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    subtitleTone: ContentTone = ContentTone.Muted,
    supportingContent: (@Composable () -> Unit)? = null,
    value: String? = null,
    valueCaption: String? = null,
    valueTone: ContentTone = ContentTone.Default,
    titleTone: ContentTone = ContentTone.Default,
    attentionCue: String? = null,
    warnTone: ToneColors = ToneDefaults.warn(),
    expanded: Boolean = false,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    expandedStateLabel: String = "Expanded",
    collapsedStateLabel: String = "Collapsed",
    trailingAction: (@Composable () -> Unit)? = null,
    dimmed: Boolean = false,
    showDivider: Boolean = true,
    expandedContent: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val error = ToneDefaults.error()
    val summaryAlpha = rememberReceiptRowAlpha(dimmed)
    val text = ReceiptRowText(
        title = title,
        subtitle = subtitle,
        supportingContent = supportingContent,
        value = value,
        valueCaption = valueCaption,
        attentionCue = attentionCue,
        dimmed = dimmed
    )
    val palette = ReceiptRowPalette(
        title = receiptRowToneColor(titleTone, scheme, warnTone, error),
        subtitle = receiptRowToneColor(subtitleTone, scheme, warnTone, error),
        value = receiptRowToneColor(valueTone, scheme, warnTone, error),
        caption = scheme.onSurfaceVariant,
        warn = warnTone
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.ReceiptRow.ColumnGap)
        ) {
            ReceiptRowSummary(
                // Only the summary dims; a trailingAction keeps full opacity (WR-02).
                modifier = Modifier.weight(1f).graphicsLayer { alpha = summaryAlpha.value },
                text = text,
                palette = palette,
                expanded = expanded,
                onClick = onClick,
                onClickLabel = onClickLabel,
                expandedStateLabel = expandedStateLabel,
                collapsedStateLabel = collapsedStateLabel,
                showChevron = onClick != null && trailingAction == null
            )
            if (trailingAction != null) {
                Box(Modifier.testTag("receipt_row_trailing_action")) { trailingAction() }
            }
        }
        if (expandedContent != null) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(RECEIPT_ROW_MOTION_MS)) +
                    fadeIn(tween(RECEIPT_ROW_MOTION_MS)),
                exit = shrinkVertically(tween(RECEIPT_ROW_MOTION_MS)) +
                    fadeOut(tween(RECEIPT_ROW_MOTION_MS))
            ) {
                Box(Modifier.fillMaxWidth().testTag("receipt_row_expanded_content")) {
                    expandedContent()
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = Dimens.HairlineBorder,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

/** The text content of a [ReceiptRow] summary, grouped to keep the private parts small. */
private class ReceiptRowText(
    val title: String,
    val subtitle: String?,
    val supportingContent: (@Composable () -> Unit)?,
    val value: String?,
    val valueCaption: String?,
    val attentionCue: String?,
    val dimmed: Boolean
)

/** The resolved colors of a [ReceiptRow] summary. */
private class ReceiptRowPalette(
    val title: Color,
    val subtitle: Color,
    val value: Color,
    val caption: Color,
    val warn: ToneColors
)

/**
 * The merged, optionally clickable region of the row. The modifier order is tag, semantics,
 * clickable, then padding, so the node bounds and the click area both include the vertical padding.
 */
@Composable
private fun ReceiptRowSummary(
    modifier: Modifier,
    text: ReceiptRowText,
    palette: ReceiptRowPalette,
    expanded: Boolean,
    onClick: (() -> Unit)?,
    onClickLabel: String?,
    expandedStateLabel: String,
    collapsedStateLabel: String,
    showChevron: Boolean
) {
    Row(
        modifier = modifier
            .testTag("receipt_row_summary")
            .semantics(mergeDescendants = true) {
                if (onClick != null) {
                    stateDescription = if (expanded) expandedStateLabel else collapsedStateLabel
                }
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(vertical = Dimens.ReceiptRow.VerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ReceiptRow.ColumnGap)
    ) {
        ReceiptRowTitleColumn(modifier = Modifier.weight(1f), text = text, palette = palette)
        if (text.value != null || text.valueCaption != null) {
            ReceiptRowValueColumn(text = text, palette = palette)
        }
        if (showChevron) {
            ReceiptRowChevron(expanded = expanded, tint = palette.caption)
        }
    }
}

@Composable
private fun ReceiptRowTitleColumn(modifier: Modifier, text: ReceiptRowText, palette: ReceiptRowPalette) {
    val typography = MaterialTheme.typography
    val strike = if (text.dimmed) TextDecoration.LineThrough else null
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.ReceiptRow.LineGap)
    ) {
        Text(
            text = text.title,
            style = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold).receiptRowLine(),
            color = palette.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = strike
        )
        if (text.subtitle != null) {
            Text(
                text = text.subtitle,
                style = typography.bodyMedium.receiptRowLine(),
                color = palette.subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = strike
            )
        }
        if (text.supportingContent != null) {
            // Theme-safe default color for a plain Text in the slot; a caller can still set its own.
            CompositionLocalProvider(LocalContentColor provides palette.caption) {
                ProvideTextStyle(typography.bodySmall.receiptRowLine()) { text.supportingContent.invoke() }
            }
        }
        if (text.attentionCue != null) {
            ReceiptRowCue(cue = text.attentionCue, tone = palette.warn)
        }
    }
}

@Composable
private fun ReceiptRowCue(cue: String, tone: ToneColors) {
    Box(
        modifier = Modifier
            .testTag("receipt_row_cue")
            .background(tone.container, CircleShape)
            .padding(
                horizontal = Dimens.ReceiptRow.CueHorizontalPadding,
                vertical = Dimens.ReceiptRow.CueVerticalPadding
            )
    ) {
        Text(
            text = cue,
            style = MaterialTheme.typography.bodySmall.receiptRowLine(),
            color = tone.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReceiptRowValueColumn(text: ReceiptRowText, palette: ReceiptRowPalette) {
    val typography = MaterialTheme.typography
    Column(
        modifier = Modifier
            .widthIn(min = Dimens.ReceiptRow.ValueMinWidth)
            .testTag("receipt_row_value_column"),
        horizontalAlignment = Alignment.End
    ) {
        if (text.value != null) {
            Text(
                text = text.value,
                style = typography.titleMedium
                    .copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")
                    .receiptRowLine(),
                color = palette.value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (text.valueCaption != null) {
            Text(
                text = text.valueCaption,
                style = typography.bodySmall.receiptRowLine(),
                color = palette.caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReceiptRowChevron(expanded: Boolean, tint: Color) {
    val rotation = rememberReceiptRowChevronRotation(expanded)
    Icon(
        imageVector = Icons.Filled.KeyboardArrowDown,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(Dimens.ReceiptRow.ChevronSize)
            .testTag("receipt_row_chevron")
            .graphicsLayer { rotationZ = rotation.value }
    )
}
