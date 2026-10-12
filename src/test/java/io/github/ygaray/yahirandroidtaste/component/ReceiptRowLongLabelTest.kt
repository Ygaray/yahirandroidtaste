package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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
 * Overflow proof for [ReceiptRow] at 360dp (Phase 21 VROW-01, D-03): with a long localized title the
 * VALUE stays whole and the TITLE ellipsizes, and the chevron or trailing action stays inside the
 * window. Same harness as [ReceiptRowTest]: w360dp, NATIVE graphics (real font metrics), unclipped
 * geometry, one `setContent` per test method, no pixel capture.
 *
 * The extreme-value case only asserts that nothing crashes and nothing leaves the window. A plain
 * Row gives no minimum chevron width when the value alone is wider than the row, which is a
 * documented limit of the component (see the KDoc "Overflow" paragraph), not a defect.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReceiptRowLongLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val longTitle = "Außergewöhnlich lange Bezeichnung des Eintrags, die unmöglich in eine einzige Zeile passt"
    private val longSubtitle = "Außergewöhnlich lange Beschreibung der Position, die ebenfalls nicht in eine Zeile passt"

    private fun setRow(fontScale: Float? = null, content: @Composable () -> Unit) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale ?: density.fontScale)
            ) {
                YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    Box(Modifier.width(360.dp)) { content() }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun windowWidthPx(): Float = 360f * composeTestRule.density.density

    private fun text(value: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(value, useUnmergedTree = true)

    private fun tagged(tag: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    /** Unclipped horizontal edges in root px: (left, right). */
    private fun SemanticsNodeInteraction.edges(): Pair<Float, Float> {
        val node = fetchSemanticsNode()
        val left = node.positionInRoot.x
        return left to left + node.size.width
    }

    private fun assertWhole(value: SemanticsNodeInteraction, label: String) {
        val layout = value.textLayout()
        assertEquals("$label line count", 1, layout.lineCount)
        assertFalse("$label must not be ellipsized", layout.isLineEllipsized(0))
        // didOverflowWidth compares the node size with the paragraph's layout width (the wide
        // constraint), so it is true for ANY text narrower than its slot in this Compose version.
        // The meaningful instrument is the drawn line against the node's own width.
        assertTrue(
            "$label text (${layout.getLineRight(0)}px) must fit its own node width (${layout.size.width}px)",
            layout.getLineRight(0) <= layout.size.width + 1f
        )
        val (_, right) = value.edges()
        assertTrue("$label right edge $right must be inside the ${windowWidthPx()}px window", right <= windowWidthPx() + 1f)
    }

    private fun assertEllipsizedOneLine(node: SemanticsNodeInteraction, label: String) {
        val layout = node.textLayout()
        assertEquals("$label line count", 1, layout.lineCount)
        assertTrue("$label must be ellipsized", layout.isLineEllipsized(0))
    }

    @Test
    fun `tracer - a long German title ellipsizes and the value stays whole before the chevron`() {
        setRow {
            ReceiptRow(
                title = longTitle,
                subtitle = "2 Scheiben · 180 g",
                value = "1.250",
                valueCaption = "Kalorien",
                onClick = {}
            )
        }

        assertEllipsizedOneLine(text(longTitle), "title")
        assertWhole(text("1.250"), "value")
        val (_, titleRight) = text(longTitle).edges()
        val (valueColumnLeft, _) = tagged("receipt_row_value_column").edges()
        assertTrue("title right edge $titleRight must end before the value column at $valueColumnLeft", titleRight <= valueColumnLeft + 1f)

        val chevron = tagged("receipt_row_chevron")
        val (_, chevronRight) = chevron.edges()
        assertTrue("chevron right edge $chevronRight must be inside the window", chevronRight <= windowWidthPx() + 1f)
        assertEquals("chevron width in dp", 24f, chevron.fetchSemanticsNode().size.width / composeTestRule.density.density, 0.5f)
    }

    @Test
    fun `a long German subtitle ellipsizes on one line and the value stays whole`() {
        setRow {
            ReceiptRow(
                title = "Eintrag",
                subtitle = longSubtitle,
                value = "1.250",
                valueCaption = "Kalorien",
                onClick = {}
            )
        }

        assertEllipsizedOneLine(text(longSubtitle), "subtitle")
        assertWhole(text("1.250"), "value")
    }

    @Test
    fun `a long German title with a trailing action keeps the action and the value inside the window`() {
        setRow {
            ReceiptRow(
                title = longTitle,
                subtitle = "2 Scheiben · 180 g",
                value = "1.250",
                valueCaption = "Kalorien",
                trailingAction = { TextButton(onClick = {}) { Text("Rückgängig") } }
            )
        }

        assertEllipsizedOneLine(text(longTitle), "title")
        assertWhole(text("1.250"), "value")
        val (actionLeft, actionRight) = tagged("receipt_row_trailing_action").edges()
        assertTrue("trailing action $actionLeft..$actionRight must lie fully inside the window", actionLeft >= 0f && actionRight <= windowWidthPx() + 1f)
    }

    @Test
    fun `control - short title and subtitle with a normal value are not ellipsized`() {
        setRow {
            ReceiptRow(
                title = "Eintrag",
                subtitle = "Kurz",
                value = "1.250",
                valueCaption = "Kalorien",
                onClick = {}
            )
        }

        listOf("Eintrag", "Kurz", "1.250", "Kalorien").forEach { shown ->
            val layout = text(shown).textLayout()
            assertEquals("$shown line count", 1, layout.lineCount)
            assertFalse("$shown must not be ellipsized", layout.isLineEllipsized(0))
        }
    }

    @Test
    fun `at font scale 1_5 the value stays whole and inside the window beside a long title`() {
        setRow(fontScale = 1.5f) {
            ReceiptRow(
                title = longTitle,
                subtitle = "2 Scheiben · 180 g",
                value = "1.250",
                valueCaption = "Kalorien",
                onClick = {}
            )
        }

        assertWhole(text("1.250"), "value")
        assertEllipsizedOneLine(text(longTitle), "title")
    }

    @Test
    fun `an extreme 80 digit value neither crashes nor leaves the window`() {
        val extreme = "1234567890".repeat(8)
        setRow {
            ReceiptRow(title = "Eintrag", value = extreme, onClick = {})
        }

        val summary = tagged("receipt_row_summary")
        val (summaryLeft, summaryRight) = summary.edges()
        assertTrue("summary width ${summaryRight - summaryLeft}px must be within the window", summaryRight - summaryLeft <= windowWidthPx() + 1f)
        val (_, valueRight) = text(extreme).edges()
        assertTrue("value right edge $valueRight must stay within the summary's right edge $summaryRight", valueRight <= summaryRight + 1f)
        val heightDp = summary.fetchSemanticsNode().size.height / composeTestRule.density.density
        assertTrue("summary is ${heightDp}dp tall, expected at least 48dp", heightDp >= 48f - 1f)
    }
}
