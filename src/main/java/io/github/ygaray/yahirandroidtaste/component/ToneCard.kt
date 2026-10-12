package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.ToneColors

/**
 * A tinted card: a [Surface] filled with [tone]`.container`, whose content defaults to
 * [tone]`.content`, with 12dp corners, 12dp vertical by 16dp horizontal padding and 4dp spacing
 * between children.
 *
 * Intended for static UI copy only, never sensitive text.
 *
 * @param tone Container and content colors, for example `ToneDefaults.error()`.
 * @param modifier Applied to the card surface. The card carries the stable test tag `tone_card`
 *   after [modifier], so a caller `testTag` wins. The library never sets `testTagsAsResourceId`.
 * @param content Column content; children are spaced 4dp apart.
 */
@Composable
fun ToneCard(
    tone: ToneColors,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.testTag("tone_card"),
        shape = RoundedCornerShape(Dimens.CornerRadius.Medium),
        color = tone.container,
        contentColor = tone.content
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = Dimens.HorizontalPadding,
                vertical = Dimens.CompactPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing),
            content = content
        )
    }
}
