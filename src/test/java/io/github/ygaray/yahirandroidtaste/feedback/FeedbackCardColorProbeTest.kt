package io.github.ygaray.yahirandroidtaste.feedback

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The card's colours reach the pixels (F3 in `.planning/quick/2026-11-13-v2.8.1-deck-styling/RULINGS.md`).
 * A mapping test alone passes while the Surface keeps `surfaceContainerHighest` or the Undo button
 * ignores its `contentColor`, so this draws one card (no deck, so no group alpha) and samples it.
 *
 * The window is drawn into a bitmap with `View.draw`: `captureToImage` does not settle under this
 * harness. That path draws no elevation shadow, which the probes avoid.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FeedbackCardColorProbeTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val item = FeedbackStackItem(1L, "Deleted Granola bar", null, null, true, null, "u1")

    private lateinit var expected: FeedbackCardColors

    private fun render(dark: Boolean): Bitmap {
        rule.setContent {
            YahirAndroidTasteTheme(themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT, dynamicColor = false) {
                expected = feedbackCardColors(MaterialTheme.colorScheme)
                Box(Modifier.fillMaxSize()) {
                    FeedbackCard(
                        item = item, undoLabel = "Undo", dismissLabel = "Dismiss",
                        onBodyTap = {}, onUndo = {}, onDismiss = {},
                        collapsedBack = false, onPeekTap = {}, index = 0,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).testTag(CARD)
                    )
                }
            }
        }
        rule.waitForIdle()
        val view = rule.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { view.draw(Canvas(bitmap)) }
        return bitmap
    }

    private fun windowBounds(tag: String): Rect =
        rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().boundsInWindow

    private fun distance(argb: Int, color: Color): Int {
        val c = color.toArgb()
        return abs((argb shr 16 and 0xFF) - (c shr 16 and 0xFF)) +
            abs((argb shr 8 and 0xFF) - (c shr 8 and 0xFF)) +
            abs((argb and 0xFF) - (c and 0xFF))
    }

    private fun probe(dark: Boolean) {
        val bitmap = render(dark)
        val card = windowBounds(CARD)
        val density = rule.activity.resources.displayMetrics.density
        // Inside the card's 8dp content padding, at mid height: only the fill is drawn there.
        val fill = bitmap.getPixel((card.left + 4 * density).toInt(), card.center.y.toInt())
        assertTrue(
            "card fill ${Integer.toHexString(fill)} is the container ${expected.container}",
            distance(fill, expected.container) <= TOLERANCE
        )
        val undo = windowBounds(FEEDBACK_STACK_UNDO_TEST_TAG)
        var best = Int.MAX_VALUE
        for (x in undo.left.toInt() until undo.right.toInt()) {
            for (y in undo.top.toInt() until undo.bottom.toInt()) {
                best = minOf(best, distance(bitmap.getPixel(x, y), expected.accent))
            }
        }
        assertTrue("the Undo label draws in the accent ${expected.accent} (closest $best)", best <= TOLERANCE)
    }

    @Test
    fun light_fillAndUndo_drawTheInverseRoles() {
        probe(dark = false)
        assertTrue(expected.container == io.github.ygaray.yahirandroidtaste.theme.LightColorScheme.inverseSurface)
    }

    @Test
    fun dark_fillAndUndo_drawTheInverseRoles() {
        probe(dark = true)
        assertTrue(expected.container == io.github.ygaray.yahirandroidtaste.theme.DarkColorScheme.inverseSurface)
    }

    private companion object {
        const val CARD = "probe_card"
        const val TOLERANCE = 24
    }
}
