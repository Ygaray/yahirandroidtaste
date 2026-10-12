package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
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
 * Renders the `UndoCenterScreen` registry entry's Variants the way the gallery does and drives the
 * group-row demos added in Phase 24 (DEMO-01): the header, the Undo all button, the indented
 * members, a resolved group, an ungrouped row, and the German Undo all at 360dp (T-24-05). The
 * existing flat demo shares the page, so every query is scoped to the demo container. The demo tags
 * are asserted as literals. NATIVE graphics; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UndoGroupDemoRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val english = "demo_undo_groups"
    private val german = "demo_undo_groups_german"
    private var density: Density = Density(1f)

    private fun render() {
        val content = checkNotNull(ComponentRegistry.entries.first { it.name == "UndoCenterScreen" }.content)
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun inDemo(tag: String, demo: String): SemanticsMatcher =
        hasTestTag(tag) and hasAnyAncestor(hasTestTag(demo))

    private fun node(tag: String, demo: String = english): SemanticsNodeInteraction =
        composeTestRule.onNode(inDemo(tag, demo))

    private fun count(tag: String, demo: String = english): Int =
        composeTestRule.onAllNodes(inDemo(tag, demo)).fetchSemanticsNodes().size

    private fun text(value: String, demo: String = english): SemanticsNodeInteraction =
        composeTestRule.onNode(hasText(value) and hasAnyAncestor(hasTestTag(demo)))

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    // --- English group rows -----------------------------------------------------------------

    @Test
    fun `the first group shows its header, subtitle and Undo all, the second is resolved without Undo all`() {
        render()

        assertEquals(1, count("undo-group-header-demo-batch"))
        text("Batch edit").assertExists()
        text("3 items · Undoable").assertExists()
        node("undo-group-undo-all-demo-batch").assertTextEquals("Undo all")

        assertEquals(1, count("undo-group-header-demo-earlier"))
        text("Earlier batch").assertExists()
        text("2 items · Resolved").assertExists()
        assertEquals("a resolved group has no Undo all", 0, count("undo-group-undo-all-demo-earlier"))
    }

    @Test
    fun `members are rows and the ungrouped entry stays flat`() {
        render()

        listOf("demo-g1-a", "demo-g1-b", "demo-g1-c", "demo-solo", "demo-g2-a", "demo-g2-b").forEach { id ->
            assertEquals("row $id", 1, count("undo-row-$id"))
        }
    }

    @Test
    fun `clicking Undo all resolves the first group and removes its button`() {
        render()

        node("undo-group-undo-all-demo-batch").performClick()
        composeTestRule.waitForIdle()

        text("3 items · Resolved").assertExists()
        assertEquals("Undo all is gone", 0, count("undo-group-undo-all-demo-batch"))
        assertEquals("the group header stays", 1, count("undo-group-header-demo-batch"))
    }

    // --- German Undo all at 360dp ------------------------------------------------------------

    @Test
    fun `the German Undo all label reads Alle rückgängig machen`() {
        render()

        node("undo-group-undo-all-demo-batch", german).assertTextEquals("Alle rückgängig machen")
    }

    @Test
    fun `the German Undo all stays inside 360dp with no overflow and the label column does not overlap it`() {
        render()
        val windowPx = 360 * density.density
        val label = "Alle rückgängig machen"

        val button = node("undo-group-undo-all-demo-batch", german).fetchSemanticsNode()
        val buttonLeft = button.positionInRoot.x
        val buttonRight = buttonLeft + button.size.width
        assertTrue("button left $buttonLeft must be >= 0", buttonLeft >= 0f)
        assertTrue("button right $buttonRight must be <= window $windowPx", buttonRight <= windowPx)

        val layout = composeTestRule
            .onNode(hasText(label) and hasAnyAncestor(hasTestTag(german)), useUnmergedTree = true)
            .textLayout()
        for (line in 0 until layout.lineCount) {
            assertFalse("\"$label\" line $line must not be ellipsized", layout.isLineEllipsized(line))
        }
        assertFalse("\"$label\" must not overflow height", layout.didOverflowHeight)
        assertEquals("\"$label\" one line expected", 1, layout.lineCount)

        val groupLabel = text("Sammelbearbeitung der Positionen aus der letzten Sitzung", german).fetchSemanticsNode()
        val labelRight = groupLabel.positionInRoot.x + groupLabel.size.width
        assertTrue("group label right $labelRight must be <= button left $buttonLeft", labelRight <= buttonLeft)
        val subtitle = text("3 items · Undoable", german).fetchSemanticsNode()
        val subtitleRight = subtitle.positionInRoot.x + subtitle.size.width
        assertTrue("subtitle right $subtitleRight must be <= button left $buttonLeft", subtitleRight <= buttonLeft)
    }
}
