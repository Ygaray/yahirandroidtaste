package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import io.github.ygaray.yahirandroidtaste.theme.DarkColorScheme
import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WCAG contrast of the deck card's text after the 92% group opacity, over any backdrop: a 15-step
 * RGB cube, which includes the extremes (v2.8.1, T-3159 item 4). The whole layer, text and fill
 * alike, is composited over the backdrop at [FEEDBACK_DECK_ALPHA].
 *
 * Body text must stay at or above 4.5:1. The secondary line and the Undo accent are reported only:
 * the dark Undo accent's 4.21:1 was accepted as is by Yahir (T-3163, Undo = a), so it is not a gate.
 */
class FeedbackDeckContrastTest {

    private fun contrast(a: Color, b: Color): Double {
        val la = a.luminance().toDouble()
        val lb = b.luminance().toDouble()
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private val backdrops: List<Color> = buildList {
        for (r in 0..255 step 15) for (g in 0..255 step 15) for (b in 0..255 step 15) add(Color(r, g, b))
    }

    /** Worst contrast of [text] (already composited over the card if translucent) over every backdrop. */
    private fun worst(text: Color, fill: Color): Double = backdrops.minOf { backdrop ->
        val layerText = text.compositeOver(fill)
        contrast(
            layerText.copy(alpha = FEEDBACK_DECK_ALPHA).compositeOver(backdrop),
            fill.copy(alpha = FEEDBACK_DECK_ALPHA).compositeOver(backdrop)
        )
    }

    @Test
    fun bodyText_staysAtOrAbove4_5_overAnyBackdrop_inLightAndDark() {
        val report = mutableListOf<String>()
        listOf("light" to LightColorScheme, "dark" to DarkColorScheme).forEach { (name, scheme) ->
            val colors = feedbackCardColors(scheme)
            val body = worst(colors.content, colors.container)
            val supporting = worst(colors.supporting, colors.container)
            val accent = worst(colors.accent, colors.container)
            report += "%s: body %.2f, secondary %.2f, accent %.2f".format(name, body, supporting, accent)
            assertTrue("$name body text is %.2f:1 at the deck alpha".format(body), body >= 4.5)
        }
        // The SUMMARY quotes these numbers.
        println("FeedbackDeckContrast @${FEEDBACK_DECK_ALPHA}: " + report.joinToString("; "))
    }
}
