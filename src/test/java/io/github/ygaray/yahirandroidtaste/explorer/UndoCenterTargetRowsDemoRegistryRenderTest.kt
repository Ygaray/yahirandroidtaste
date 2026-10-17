package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.feedback.UNDO_OPEN_CHEVRON_TEST_TAG_PREFIX
import io.github.ygaray.yahirandroidtaste.feedback.UNDO_ROW_TEST_TAG_PREFIX
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the `UndoCenterScreen` registry entry's Variants the way the gallery does and drives the
 * go-to-item rows added in Phase 28 (DEMO-03): an Available row with a target opens, a row without a
 * target and an Undone row do not, the rows keep their input order, and pressing Undo makes a row lose
 * its chevron and click action. The flat demo and the group demos share the page, so every query is
 * scoped to the demo container. The demo tags are asserted as literals. NATIVE graphics; no pixel
 * capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UndoCenterTargetRowsDemoRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val english = "demo_undo_targets"
    private val german = "demo_undo_targets_german"

    private fun render() {
        val content = checkNotNull(ComponentRegistry.entries.first { it.name == "UndoCenterScreen" }.content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun inDemo(tag: String, demo: String): SemanticsMatcher =
        hasTestTag(tag) and hasAnyAncestor(hasTestTag(demo))

    private fun row(id: String, demo: String = english): SemanticsNodeInteraction =
        composeTestRule.onNode(inDemo("$UNDO_ROW_TEST_TAG_PREFIX$id", demo))

    private fun chevrons(id: String, demo: String = english): Int = composeTestRule
        .onAllNodes(inDemo("$UNDO_OPEN_CHEVRON_TEST_TAG_PREFIX$id", demo), useUnmergedTree = true)
        .fetchSemanticsNodes().size

    private fun opened(demo: String = english): SemanticsNodeInteraction =
        composeTestRule.onNode(inDemo("demo_undo_targets_opened", demo))

    private fun text(value: String, demo: String = english): SemanticsNodeInteraction =
        composeTestRule.onNode(hasText(value) and hasAnyAncestor(hasTestTag(demo)))

    /** A tap left of the row's trailing controls, so it can never land on the Undo button. */
    private fun tapRow(id: String, demo: String = english) {
        composeTestRule.onNode(inDemo("$UNDO_ROW_TEST_TAG_PREFIX$id", demo), useUnmergedTree = true)
            .performTouchInput { click(Offset(10f, center.y)) }
        composeTestRule.waitForIdle()
    }

    private fun pressUndoOf(id: String, demo: String = english) {
        composeTestRule.onNode(
            hasText("Undo") and hasAnyAncestor(inDemo("$UNDO_ROW_TEST_TAG_PREFIX$id", demo)),
            useUnmergedTree = true
        ).performClick()
        composeTestRule.waitForIdle()
    }

    private fun clickLabel(id: String, demo: String = english): String? =
        row(id, demo).fetchSemanticsNode().config.getOrNull(SemanticsActions.OnClick)?.label

    private fun hasOpenAction(id: String, demo: String = english): Boolean =
        row(id, demo).fetchSemanticsNode().config.contains(SemanticsActions.OnClick)

    @Test
    fun availableRowWithATarget_isOpenable_andOthersAreNot() {
        render()

        assertTrue("row A has a click action", hasOpenAction("demo-row-a"))
        assertEquals("Open item A", clickLabel("demo-row-a"))
        row("demo-row-a").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
        )
        assertEquals("row A shows a chevron", 1, chevrons("demo-row-a"))

        assertTrue("row B has no target and no click action", !hasOpenAction("demo-row-b"))
        assertEquals(0, chevrons("demo-row-b"))
        assertTrue("row C is Undone and has no click action", !hasOpenAction("demo-row-c"))
        assertEquals(0, chevrons("demo-row-c"))
        text("Undone").assertExists()
    }

    @Test
    fun tapOnTheOpenableRow_showsTheKey_andRunsNoUndo() {
        render()
        opened().assertTextEquals("Opened: none")

        tapRow("demo-row-a")

        opened().assertTextEquals("Opened: demo-row-a")
        assertEquals("a tap runs no undo: the chevron stays", 1, chevrons("demo-row-a"))
        assertTrue("and the click action stays", hasOpenAction("demo-row-a"))
    }

    @Test
    fun undoMakesTheRowLoseItsTarget() {
        render()

        pressUndoOf("demo-row-a")

        assertEquals("the chevron is gone", 0, chevrons("demo-row-a"))
        assertTrue("the click action is gone", !hasOpenAction("demo-row-a"))
        opened().assertTextEquals("Opened: none")

        tapRow("demo-row-a")
        opened().assertTextEquals("Opened: none")
    }

    @Test
    fun rowsKeepTheirInputOrder() {
        render()

        val tops = listOf("demo-row-a", "demo-row-b", "demo-row-c").map {
            row(it).fetchSemanticsNode().boundsInRoot.top
        }
        assertTrue("rows must read A, B, C from top to bottom: $tops", tops[0] < tops[1] && tops[1] < tops[2])
    }

    @Test
    fun germanDemo_usesGermanCopyAndOpenLabel() {
        render()

        text("Eintrag A geändert", german).assertExists()
        text("Eintrag B umbenannt", german).assertExists()
        text("Eintrag C verschoben", german).assertExists()
        assertEquals("Eintrag A öffnen", clickLabel("demo-row-a", german))
        opened(german).assertTextEquals("Geöffnet: keins")

        tapRow("demo-row-a", german)

        opened(german).assertTextEquals("Geöffnet: demo-row-a")
        opened().assertTextEquals("Opened: none")
    }

    @Test
    fun germanRows_stayInsideA360dpWindow() {
        render()

        val tags = listOf("demo-row-a", "demo-row-b", "demo-row-c").map { "$UNDO_ROW_TEST_TAG_PREFIX$it" }
        var checked = 0
        tags.forEach { tag ->
            val bounds = composeTestRule.onNode(inDemo(tag, german), useUnmergedTree = true).getUnclippedBoundsInRoot()
            assertTrue("$tag left ${bounds.left} must be >= 0", bounds.left >= 0.dp)
            assertTrue("$tag right ${bounds.right} must be <= 360dp", bounds.right <= 360.dp)
            checked++
        }
        val undoButtons = composeTestRule.onAllNodes(
            hasText("Undo") and hasAnyAncestor(hasTestTag(german)),
            useUnmergedTree = true
        )
        undoButtons.fetchSemanticsNodes().indices.forEach { index ->
            val bounds = undoButtons[index].getUnclippedBoundsInRoot()
            assertTrue("Undo[$index] right ${bounds.right} must be <= 360dp", bounds.right <= 360.dp)
            checked++
        }
        val chevron = composeTestRule.onNode(
            inDemo("${UNDO_OPEN_CHEVRON_TEST_TAG_PREFIX}demo-row-a", german),
            useUnmergedTree = true
        ).getUnclippedBoundsInRoot()
        assertTrue("chevron right ${chevron.right} must be <= 360dp", chevron.right <= 360.dp)
        assertTrue("the check reached the rows, Undo buttons and chevron ($checked)", checked >= 5)
    }
}
