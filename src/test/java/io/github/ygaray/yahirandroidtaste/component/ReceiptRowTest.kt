package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistry
import io.github.ygaray.yahirandroidtaste.explorer.ExplorerFamilies
import io.github.ygaray.yahirandroidtaste.theme.ContentTone
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.ToneDefaults
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Compose tests for [ReceiptRow] (Phase 21 VROW-01, VROW-02): geometry, structure and semantics.
 *
 * Text geometry needs real font metrics, so the class runs at w360dp with NATIVE graphics (legacy
 * graphics measure about one pixel per character and would pass vacuously). Bounds are read
 * UNCLIPPED. No pixel capture: it times out in this harness. One `setContent` per test method.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReceiptRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setRow(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.width(360.dp)) { content() }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun summaryHeight(): Float {
        val bounds = composeTestRule.onNodeWithTag("receipt_row_summary").getUnclippedBoundsInRoot()
        return (bounds.bottom - bounds.top).value
    }

    private fun unmerged(tag: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun chevronCount(): Int =
        composeTestRule.onAllNodesWithTag("receipt_row_chevron", useUnmergedTree = true)
            .fetchSemanticsNodes().size

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    private fun text(value: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(value, useUnmergedTree = true)

    /** Unclipped horizontal edges in root px: (left, right). */
    private fun SemanticsNodeInteraction.edges(): Pair<Float, Float> {
        val node = fetchSemanticsNode()
        val left = node.positionInRoot.x
        return left to left + node.size.width
    }

    @Test
    fun `tracer - a collapsed row with every line and no cue is 92dp tall`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                subtitle = "Subtitle detail",
                supportingContent = { Text("Supporting line") },
                value = "1,250",
                valueCaption = "Total",
                onClick = {}
            )
        }

        assertEquals("summary height", 92f, summaryHeight(), 1f)
    }

    @Test
    fun `tracer - the merged summary holds the title and the value`() {
        setRow {
            ReceiptRow(title = "Item title", value = "1,250", onClick = {})
        }

        composeTestRule.onNodeWithTag("receipt_row_summary").assertTextContains("Item title", substring = true)
        composeTestRule.onNodeWithTag("receipt_row_summary").assertTextContains("1,250", substring = true)
    }

    @Test
    fun `tracer - the registry lists ReceiptRow as a Cards pattern with canonical states`() {
        val entry = ComponentRegistry.entries.first { it.name == "ReceiptRow" }

        assertEquals(ExplorerFamilies.CARDS, entry.family)
        assertEquals(ComponentRegistry.Tier.PATTERN, entry.tier)
        assertEquals(
            listOf("Default", "Pressed / Selected", "Disabled", "Focused"),
            entry.states.map { it.label }
        )
        listOf("Default", "Pressed / Selected", "Disabled").forEach { label ->
            assertNotNull("$label cell", entry.states.first { it.label == label }.render)
        }
        assertNotNull(entry.content)
    }

    // --- Collapsed heights -------------------------------------------------------------------

    @Test
    fun `without the supporting line and the cue a collapsed row is 72dp`() {
        setRow {
            ReceiptRow(title = "Item title", subtitle = "Subtitle detail", value = "1,250", valueCaption = "Total")
        }

        assertEquals("summary height", 72f, summaryHeight(), 1f)
    }

    @Test
    fun `with every line and a cue a collapsed row is 120dp`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                subtitle = "Subtitle detail",
                supportingContent = { Text("Supporting line") },
                value = "1,250",
                valueCaption = "Total",
                attentionCue = "Check this"
            )
        }

        assertEquals("summary height", 120f, summaryHeight(), 1f)
    }

    @Test
    fun `without the supporting line but with a cue a collapsed row is 100dp`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                subtitle = "Subtitle detail",
                value = "1,250",
                valueCaption = "Total",
                attentionCue = "Check this"
            )
        }

        assertEquals("summary height", 100f, summaryHeight(), 1f)
    }

    @Test
    fun `the cue pill is 24dp tall and sits 4dp below the supporting line`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                subtitle = "Subtitle detail",
                supportingContent = { Text("Supporting line", modifier = Modifier.testTag("supporting")) },
                value = "1,250",
                attentionCue = "Check this"
            )
        }

        val cue = unmerged("receipt_row_cue").getUnclippedBoundsInRoot()
        val supporting = unmerged("supporting").getUnclippedBoundsInRoot()
        assertEquals("cue height", 24f, (cue.bottom - cue.top).value, 1f)
        assertEquals("gap above the cue", 4f, (cue.top - supporting.bottom).value, 1f)
    }

    // --- Value column ------------------------------------------------------------------------

    @Test
    fun `a short value keeps the value column at its 56dp minimum`() {
        setRow {
            ReceiptRow(title = "Item title", value = "5")
        }

        val bounds = unmerged("receipt_row_value_column").getUnclippedBoundsInRoot()
        assertEquals("value column width", 56f, (bounds.right - bounds.left).value, 1f)
    }

    @Test
    fun `the value and its caption are end aligned`() {
        setRow {
            ReceiptRow(title = "Item title", value = "5", valueCaption = "Total amount")
        }

        val (_, valueRight) = text("5").edges()
        val (_, captionRight) = text("Total amount").edges()
        assertEquals("right edges", valueRight, captionRight, 1f)
    }

    // --- Chevron matrix ----------------------------------------------------------------------

    @Test
    fun `no onClick and no trailing action shows no chevron`() {
        setRow { ReceiptRow(title = "Item title", value = "5") }

        assertEquals(0, chevronCount())
    }

    @Test
    fun `onClick without a trailing action shows one 24dp chevron`() {
        setRow { ReceiptRow(title = "Item title", value = "5", onClick = {}) }

        assertEquals(1, chevronCount())
        val bounds = unmerged("receipt_row_chevron").getUnclippedBoundsInRoot()
        assertEquals("chevron width", 24f, (bounds.right - bounds.left).value, 0.5f)
        assertEquals("chevron height", 24f, (bounds.bottom - bounds.top).value, 0.5f)
    }

    @Test
    fun `onClick with a trailing action shows no chevron`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                value = "5",
                onClick = {},
                trailingAction = { TextButton(onClick = {}) { Text("Undo") } }
            )
        }

        assertEquals(0, chevronCount())
    }

    @Test
    fun `a trailing action without onClick shows no chevron`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                value = "5",
                trailingAction = { TextButton(onClick = {}) { Text("Undo") } }
            )
        }

        assertEquals(0, chevronCount())
    }

    // --- Trailing action ---------------------------------------------------------------------

    @Test
    fun `the trailing action sits right of the summary and the pieces span the row`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                value = "5",
                onClick = {},
                trailingAction = { TextButton(onClick = {}) { Text("Undo") } }
            )
        }

        val summary = composeTestRule.onNodeWithTag("receipt_row_summary").getUnclippedBoundsInRoot()
        val action = composeTestRule.onNodeWithTag("receipt_row_trailing_action").getUnclippedBoundsInRoot()
        assertTrue("action is right of the summary", action.left >= summary.right)
        val span = (summary.right - summary.left) + 8.dp + (action.right - action.left)
        assertEquals("summary + gap + action", 360f, span.value, 1f)
    }

    // --- Expanded content --------------------------------------------------------------------

    @Test
    fun `expanded content is full width and below the summary`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                value = "5",
                expanded = true,
                onClick = {},
                expandedContent = { Text("Expanded detail") }
            )
        }

        val summary = composeTestRule.onNodeWithTag("receipt_row_summary").getUnclippedBoundsInRoot()
        val expanded = composeTestRule.onNodeWithTag("receipt_row_expanded_content").getUnclippedBoundsInRoot()
        assertEquals("expanded width", 360f, (expanded.right - expanded.left).value, 1f)
        assertTrue("expanded content starts at or below the summary", expanded.top >= summary.bottom - 1.dp)
    }

    @Test
    fun `collapsed rows do not render their expanded content`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                value = "5",
                expanded = false,
                onClick = {},
                expandedContent = { Text("Expanded detail") }
            )
        }

        composeTestRule.onNodeWithTag("receipt_row_expanded_content").assertDoesNotExist()
    }

    @Test
    fun `an expanded row without expanded content renders no content node`() {
        setRow { ReceiptRow(title = "Item title", value = "5", expanded = true, onClick = {}) }

        composeTestRule.onNodeWithTag("receipt_row_expanded_content").assertDoesNotExist()
    }

    // --- Divider -----------------------------------------------------------------------------

    @Test
    fun `the divider adds a 1dp hairline under the row`() {
        setRow {
            Column {
                ReceiptRow(title = "Item title", value = "5", modifier = Modifier.testTag("with_divider"))
                ReceiptRow(
                    title = "Item title",
                    value = "5",
                    showDivider = false,
                    modifier = Modifier.testTag("without_divider")
                )
            }
        }

        val with = composeTestRule.onNodeWithTag("with_divider").getUnclippedBoundsInRoot()
        val without = composeTestRule.onNodeWithTag("without_divider").getUnclippedBoundsInRoot()
        assertEquals(
            "divider height",
            1f,
            ((with.bottom - with.top) - (without.bottom - without.top)).value,
            0.5f
        )
    }

    // --- Dimmed ------------------------------------------------------------------------------

    @Test
    fun `a dimmed row strikes through the title and subtitle but not the value`() {
        setRow {
            ReceiptRow(title = "Item title", subtitle = "Subtitle detail", value = "1,250", dimmed = true)
        }

        fun struck(value: String): Boolean =
            text(value).textLayout().layoutInput.style.textDecoration?.contains(TextDecoration.LineThrough) == true
        assertTrue("title struck", struck("Item title"))
        assertTrue("subtitle struck", struck("Subtitle detail"))
        assertFalse("value not struck", struck("1,250"))
    }

    @Test
    fun `a row that is not dimmed strikes nothing through`() {
        setRow {
            ReceiptRow(title = "Item title", subtitle = "Subtitle detail", value = "1,250")
        }

        listOf("Item title", "Subtitle detail", "1,250").forEach { value ->
            val decoration = text(value).textLayout().layoutInput.style.textDecoration
            assertFalse("$value struck", decoration?.contains(TextDecoration.LineThrough) == true)
        }
    }

    // --- Tones -------------------------------------------------------------------------------

    @Test
    fun `receiptRowToneColor maps the four tones`() {
        val warn = ToneColors(Color(0xFF111111), Color(0xFF222222))
        val error = ToneColors(Color(0xFF333333), Color(0xFF444444))
        var scheme: androidx.compose.material3.ColorScheme? = null
        setRow { scheme = MaterialTheme.colorScheme }
        val colors = checkNotNull(scheme)

        assertEquals(colors.onSurface, receiptRowToneColor(ContentTone.Default, colors, warn, error))
        assertEquals(colors.onSurfaceVariant, receiptRowToneColor(ContentTone.Muted, colors, warn, error))
        assertEquals(warn.content, receiptRowToneColor(ContentTone.Warn, colors, warn, error))
        assertEquals(error.content, receiptRowToneColor(ContentTone.Error, colors, warn, error))
    }

    @Test
    fun `a warn subtitle uses the warn tone and an error title uses the error tone`() {
        val warn = ToneColors(Color(0xFF111111), Color(0xFF223344))
        var errorContent = Color.Unspecified
        setRow {
            errorContent = ToneDefaults.error().content
            ReceiptRow(
                title = "Item title",
                titleTone = ContentTone.Error,
                subtitle = "Subtitle detail",
                subtitleTone = ContentTone.Warn,
                warnTone = warn
            )
        }

        assertEquals(warn.content, text("Subtitle detail").textLayout().layoutInput.style.color)
        assertEquals(errorContent, text("Item title").textLayout().layoutInput.style.color)
    }
}
