package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * Showcase composable for [accentGradient] / [accentTint] (Phase 123 DS-01, D-02,
 * `123-UI-SPEC.md` § "Primitive Family 3"). Renders two stacked bands for the single supplied
 * [accentColor]: an [accentGradient] hero band and an [accentTint] flat-fill band, so the pairing
 * these two functions form is legible side by side. Reads [MaterialTheme.colorScheme] live so
 * both bands are correct in each Explorer theme mode.
 *
 * Both captions resolve their color via [contrastingForeground] applied to a representative
 * background color for that band — never a hardcoded black or white — per the UI-SPEC's
 * contrast rule.
 */
@Composable
fun GradientSwatch(accentColor: Color, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing)
    ) {
        // Band 1 — accentGradient hero band.
        val gradientStops = accentGradientStops(accentColor, colorScheme)
        val gradientForeground = contrastingForeground(gradientStops[1])
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.TouchTarget)
                .clip(RoundedCornerShape(Dimens.CornerRadius.Medium))
                .background(accentGradient(accentColor, colorScheme))
        ) {
            Text(
                text = "accentGradient",
                style = MaterialTheme.typography.labelMedium,
                color = gradientForeground,
                modifier = Modifier.padding(horizontal = Dimens.HorizontalPadding)
            )
        }

        // Band 2 — accentTint flat fill band.
        val tint = accentTint(accentColor, colorScheme)
        val tintForeground = contrastingForeground(tint)
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.TouchTarget)
                .clip(RoundedCornerShape(Dimens.CornerRadius.Medium))
                .background(tint)
        ) {
            Text(
                text = "accentTint",
                style = MaterialTheme.typography.labelMedium,
                color = tintForeground,
                modifier = Modifier.padding(horizontal = Dimens.HorizontalPadding)
            )
        }
    }
}
