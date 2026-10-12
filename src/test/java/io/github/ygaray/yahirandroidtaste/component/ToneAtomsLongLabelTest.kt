package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults.ActionButtonRole
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Long localized label checks for the Phase 20 tone atoms at the 360dp Gate-2 width class.
 *
 * NATIVE graphics gives real text metrics (legacy shadow measurement would pass vacuously). Plan
 * 20-02 covers [DynamicActionButton]; plan 20-03 appends PillChip cases. Geometry is read
 * UNCLIPPED (`positionInRoot` + `size`) because `boundsInRoot` is clipped to ancestors and would
 * hide an overflow.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ToneAtomsLongLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var density: Density = Density(1f)

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    /** Right edge in root px, unclipped. */
    private fun SemanticsNodeInteraction.rightEdge(): Float {
        val node = fetchSemanticsNode()
        return node.positionInRoot.x + node.size.width
    }

    private fun renderButton(emphasis: ActionButtonEmphasis, withIcon: Boolean, label: String) {
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column(Modifier.fillMaxWidth()) {
                    DynamicActionButton(
                        label = label,
                        role = ActionButtonRole.Neutral,
                        onClick = {},
                        emphasis = emphasis,
                        icon = if (withIcon) Icons.Filled.Check else null
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun assertLongLabelWraps(emphasis: ActionButtonEmphasis, withIcon: Boolean) {
        renderButton(emphasis, withIcon, LONG_DE)
        val text = composeTestRule.onNodeWithText(LONG_DE, useUnmergedTree = true)
        val windowPx = 360 * density.density
        val right = text.rightEdge()
        assertTrue("$emphasis icon=$withIcon: right edge $right must be <= window $windowPx", right <= windowPx)

        val layout = text.textLayout()
        assertTrue("$emphasis icon=$withIcon: expected >= 2 lines, was ${layout.lineCount}", layout.lineCount >= 2)
        for (line in 0 until layout.lineCount) {
            assertFalse("$emphasis icon=$withIcon: line $line must not be ellipsized", layout.isLineEllipsized(line))
        }
        assertFalse("$emphasis icon=$withIcon: must not overflow height", layout.didOverflowHeight)

        val button = composeTestRule.onNodeWithText(LONG_DE)
        val buttonNode = button.fetchSemanticsNode()
        val minHeightPx = 40 * density.density
        assertTrue(
            "$emphasis icon=$withIcon: button height ${buttonNode.size.height} must be >= $minHeightPx",
            buttonNode.size.height >= minHeightPx
        )
        assertTrue(
            "$emphasis icon=$withIcon: button right edge ${button.rightEdge()} must be <= window $windowPx",
            button.rightEdge() <= windowPx
        )
    }

    @Test
    fun `Filled wraps a long German label inside 360dp`() = assertLongLabelWraps(ActionButtonEmphasis.Filled, false)

    @Test
    fun `Filled with icon wraps a long German label inside 360dp`() =
        assertLongLabelWraps(ActionButtonEmphasis.Filled, true)

    @Test
    fun `Tonal wraps a long German label inside 360dp`() = assertLongLabelWraps(ActionButtonEmphasis.Tonal, false)

    @Test
    fun `Tonal with icon wraps a long German label inside 360dp`() =
        assertLongLabelWraps(ActionButtonEmphasis.Tonal, true)

    @Test
    fun `Text wraps a long German label inside 360dp`() = assertLongLabelWraps(ActionButtonEmphasis.Text, false)

    @Test
    fun `Text with icon wraps a long German label inside 360dp`() =
        assertLongLabelWraps(ActionButtonEmphasis.Text, true)

    @Test
    fun `TextSmall wraps a long German label inside 360dp`() =
        assertLongLabelWraps(ActionButtonEmphasis.TextSmall, false)

    @Test
    fun `TextSmall with icon wraps a long German label inside 360dp`() =
        assertLongLabelWraps(ActionButtonEmphasis.TextSmall, true)

    @Test
    fun `a short German label stays on one line`() {
        renderButton(ActionButtonEmphasis.Filled, withIcon = false, label = SHORT_DE)
        val layout = composeTestRule.onNodeWithText(SHORT_DE, useUnmergedTree = true).textLayout()
        assertEquals("short label line count", 1, layout.lineCount)
    }

    private fun renderPill(label: String) {
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column(Modifier.fillMaxWidth()) {
                    PillChip(
                        label = label,
                        onClick = {},
                        leadingIcon = Icons.Filled.DateRange,
                        trailingIcon = Icons.Filled.ArrowDropDown
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `PillChip keeps a long German label on one ellipsized line inside 360dp`() {
        renderPill(LONG_DE)
        val windowPx = 360 * density.density
        val chip = composeTestRule.onNodeWithTag("pill_chip")
        val chipRight = chip.rightEdge()
        assertTrue("chip right edge $chipRight must be <= window $windowPx", chipRight <= windowPx)

        val text = composeTestRule.onNodeWithText(LONG_DE, useUnmergedTree = true)
        val layout = text.textLayout()
        assertEquals("label line count", 1, layout.lineCount)
        assertTrue("line 0 must be ellipsized", layout.isLineEllipsized(0))

        // 8dp end padding + 18dp icon + 4dp gap must stay free to the right of the label.
        val reservePx = 30 * density.density
        val tolerancePx = 1 * density.density
        val textRight = text.rightEdge()
        assertTrue(
            "label right edge $textRight must leave $reservePx for the trailing icon (chip right $chipRight)",
            textRight <= chipRight - reservePx + tolerancePx
        )
    }

    @Test
    fun `PillChip short German label is not ellipsized and the chip is 32dp tall`() {
        renderPill(SHORT_PILL_DE)
        val layout = composeTestRule.onNodeWithText(SHORT_PILL_DE, useUnmergedTree = true).textLayout()
        assertEquals("short label line count", 1, layout.lineCount)
        assertFalse("short label must not be ellipsized", layout.isLineEllipsized(0))

        val heightPx = composeTestRule.onNodeWithTag("pill_chip").fetchSemanticsNode().size.height
        assertEquals("chip height", 32 * density.density, heightPx.toFloat(), 0.5f * density.density)
    }

    private companion object {
        const val SHORT_PILL_DE = "Heute"
        const val LONG_DE = "Alle ausstehenden Änderungen jetzt speichern und die Aufnahme anschließend fortsetzen"
        const val SHORT_DE = "Speichern"
    }
}
