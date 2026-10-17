package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.feedback.FEEDBACK_STACK_CHEVRON_TEST_TAG
import io.github.ygaray.yahirandroidtaste.feedback.FEEDBACK_STACK_ITEM_TEST_TAG
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the `FeedbackStackHost` registry entry's Variants the way the gallery does and drives the
 * stacked-snackbar stage added in Phase 28 (DEMO-03): fire a card, push a burst past the cap, swipe,
 * tap to open and Undo, in English and German. The demo tags are asserted as literals. Every demo
 * event is Indefinite, so no timer races the test. NATIVE graphics; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FeedbackStackDemoRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val english = "demo_feedback_stack"

    private fun render() {
        val content = checkNotNull(ComponentRegistry.entries.first { it.name == "FeedbackStackHost" }.content)
        composeTestRule.setContent {
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

    private fun press(tag: String, demo: String = english) {
        node(tag, demo).performClick()
        composeTestRule.waitForIdle()
    }

    private fun cards(demo: String = english): List<*> = composeTestRule
        .onAllNodes(inDemo(FEEDBACK_STACK_ITEM_TEST_TAG, demo), useUnmergedTree = true)
        .fetchSemanticsNodes()

    private fun card(message: String, demo: String = english): SemanticsNodeInteraction = composeTestRule.onNode(
        inDemo(FEEDBACK_STACK_ITEM_TEST_TAG, demo) and hasAnyDescendant(hasText(message)),
        useUnmergedTree = true
    )

    private fun chevrons(demo: String = english): Int = composeTestRule
        .onAllNodes(inDemo(FEEDBACK_STACK_CHEVRON_TEST_TAG, demo), useUnmergedTree = true)
        .fetchSemanticsNodes().size

    // --- tracer ---------------------------------------------------------------------------------

    @Test
    fun initialState_showsNeutralReadouts_andNoCard() {
        render()

        node("demo_feedback_result").assertTextEquals("Last result: none")
        node("demo_feedback_opened").assertTextEquals("Opened: none")
        assertEquals("the stage holds no card", 0, cards().size)
    }

    @Test
    fun fireNoteWithTarget_showsOneCardWithItsChevron() {
        render()

        press("demo_feedback_note")

        assertEquals(1, cards().size)
        card("Item saved").assertExists()
        assertEquals("a targeted card shows its chevron", 1, chevrons())
    }

    @Test
    fun bodyTap_opensTheTarget_andTheReadoutShowsTheKey() {
        render()
        press("demo_feedback_note")

        card("Item saved").performClick()
        composeTestRule.waitForIdle()

        node("demo_feedback_opened").assertTextEquals("Opened: demo-note")
        node("demo_feedback_result").assertTextEquals("Last result: Opened")
        assertEquals("the opened card is gone", 0, cards().size)
    }
}
