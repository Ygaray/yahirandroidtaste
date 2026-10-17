package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.down
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.feedback.FEEDBACK_STACK_CHEVRON_TEST_TAG
import io.github.ygaray.yahirandroidtaste.feedback.FEEDBACK_STACK_ITEM_TEST_TAG
import io.github.ygaray.yahirandroidtaste.feedback.FEEDBACK_STACK_UNDO_TEST_TAG
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
 * event is Indefinite except the Short and Long notes, which only need to push a card. NATIVE
 * graphics; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FeedbackStackDemoRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val english = "demo_feedback_stack"
    private val german = "demo_feedback_stack_german"
    private var density: Density = Density(1f)

    private fun render() {
        val content = checkNotNull(ComponentRegistry.entries.first { it.name == "FeedbackStackHost" }.content)
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

    private fun undoButtons(demo: String = english): Int = composeTestRule
        .onAllNodes(inDemo(FEEDBACK_STACK_UNDO_TEST_TAG, demo), useUnmergedTree = true)
        .fetchSemanticsNodes().size

    private fun textCount(text: String, demo: String = english): Int = composeTestRule
        .onAllNodes(hasText(text) and hasAnyAncestor(hasTestTag(demo)), useUnmergedTree = true)
        .fetchSemanticsNodes().size

    /** Waits until [demo] holds [count] cards, driving the 350 ms burst spacing on the test clock. */
    private fun awaitCards(count: Int, demo: String = english) {
        composeTestRule.mainClock.advanceTimeBy(1_200)
        composeTestRule.waitUntil(timeoutMillis = 10_000) { cards(demo).size == count }
        composeTestRule.waitForIdle()
    }

    /** Presses, moves [dxDp] in 16 ms steps (so the release carries a little velocity) and lifts. */
    private fun TouchInjectionScope.slowSwipe(dxDp: Float, steps: Int = 50) {
        down(Offset(width * 0.3f, height * 0.5f))
        repeat(steps) { moveBy(Offset(dxDp.dp.toPx() / steps, 0f), delayMillis = 16) }
        up()
    }

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

    // --- the other cards ------------------------------------------------------------------------

    @Test
    fun fireNoteWithUndoAndTarget_showsAnUndoButton() {
        render()

        press("demo_feedback_note_undo")

        assertEquals(1, cards().size)
        card("Item deleted").assertExists()
        assertEquals("the Undo card shows its Undo button", 1, undoButtons())
        assertEquals("and its chevron", 1, chevrons())
    }

    @Test
    fun plainNote_hasNoChevronAndNoOpenAction() {
        render()

        press("demo_feedback_plain")

        assertEquals(1, cards().size)
        card("Plain note").assertExists()
        assertEquals("a plain note has no chevron", 0, chevrons())
        val merged = node(FEEDBACK_STACK_ITEM_TEST_TAG).fetchSemanticsNode()
        assertNull("a plain note has no open action", merged.config.getOrNull(SemanticsActions.OnClick))
    }

    // --- burst past the cap ---------------------------------------------------------------------

    @Test
    fun burstOfFour_keepsExactlyThree_andTheOldestIsDismissed() {
        render()

        press("demo_feedback_burst")
        awaitCards(3)

        assertEquals("the deck holds exactly the cap", 3, cards().size)
        assertEquals("the oldest card is gone", 0, textCount("Burst one"))
        card("Burst four").assertExists()
        node("demo_feedback_result").assertTextEquals("Last result: Dismissed")
    }

    @Test
    fun bodyTapAfterTheBurst_opensTheNewestTarget() {
        render()
        press("demo_feedback_burst")
        awaitCards(3)

        card("Burst four").performClick()
        composeTestRule.waitForIdle()

        node("demo_feedback_opened").assertTextEquals("Opened: demo-burst-4")
        node("demo_feedback_result").assertTextEquals("Last result: Opened")
        assertEquals(2, cards().size)
    }

    @Test
    fun swipeFront_dismissesWithoutOpeningOrUndoing() {
        render()
        press("demo_feedback_note_undo")

        card("Item deleted").performTouchInput { slowSwipe(-120f) }
        composeTestRule.waitForIdle()

        assertEquals("the swiped card is gone", 0, cards().size)
        node("demo_feedback_result").assertTextEquals("Last result: Dismissed")
        node("demo_feedback_opened").assertTextEquals("Opened: none")
    }

    @Test
    fun undoButton_resolvesUndone_andOpensNothing() {
        render()
        press("demo_feedback_note_undo")

        composeTestRule.onNode(inDemo(FEEDBACK_STACK_UNDO_TEST_TAG, english), useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        assertEquals(0, cards().size)
        node("demo_feedback_result").assertTextEquals("Last result: Undone")
        node("demo_feedback_opened").assertTextEquals("Opened: none")
    }

    @Test
    fun dismissAll_clearsTheStage() {
        render()
        press("demo_feedback_note")
        press("demo_feedback_plain")
        assertEquals(2, cards().size)

        press("demo_feedback_dismiss_all")

        assertEquals(0, cards().size)
        node("demo_feedback_result").assertTextEquals("Last result: Dismissed")
    }

    @Test
    fun shortAndLongNotes_pushACard() {
        render()

        press("demo_feedback_short")
        assertEquals(1, cards().size)
        card("Short note").assertExists()

        press("demo_feedback_long")
        assertEquals(2, cards().size)
        card("Long note").assertExists()
    }

    // --- German stage ---------------------------------------------------------------------------

    @Test
    fun germanStage_usesGermanLabels() {
        render()

        node("demo_feedback_note", german).assertTextEquals("Hinweis mit Ziel")
        node("demo_feedback_note_undo", german).assertTextEquals("Hinweis mit Rückgängig und Ziel")
        node("demo_feedback_plain", german).assertTextEquals("Einfacher Hinweis")
        node("demo_feedback_burst", german).assertTextEquals("Serie x4")
        node("demo_feedback_short", german).assertTextEquals("Kurzer Hinweis")
        node("demo_feedback_long", german).assertTextEquals("Langer Hinweis mit Rückgängig")
        node("demo_feedback_dismiss_all", german).assertTextEquals("Alle verwerfen")
        node("demo_feedback_result", german).assertTextEquals("Letztes Ergebnis: keins")
        node("demo_feedback_opened", german).assertTextEquals("Geöffnet: keins")

        press("demo_feedback_note_undo", german)

        assertTrue("the Undo button reads Rückgängig", textCount("Rückgängig", german) > 0)
        val config = node(FEEDBACK_STACK_ITEM_TEST_TAG, german).fetchSemanticsNode().config
        val actionLabels = config[SemanticsActions.CustomActions].map { it.label }
        assertTrue("custom Dismiss action is Verwerfen: $actionLabels", "Verwerfen" in actionLabels)
        assertTrue("custom Undo action is Rückgängig: $actionLabels", "Rückgängig" in actionLabels)
        assertEquals("Eintrag öffnen", config[SemanticsActions.OnClick].label)
    }

    @Test
    fun germanBodyTap_showsTheKeyInTheGermanReadout() {
        render()
        press("demo_feedback_note", german)

        card("Eintrag gespeichert und abgelegt", german).performClick()
        composeTestRule.waitForIdle()

        node("demo_feedback_opened", german).assertTextEquals("Geöffnet: demo-note")
        node("demo_feedback_result", german).assertTextEquals("Letztes Ergebnis: Geöffnet")
        assertEquals(0, cards(german).size)
    }

    @Test
    fun theTwoStagesAreIndependent() {
        render()

        press("demo_feedback_note")
        assertEquals(1, cards().size)
        assertEquals("the German stage is untouched", 0, cards(german).size)

        press("demo_feedback_plain", german)
        assertEquals(1, cards(german).size)
        assertEquals("the English stage keeps its card", 1, cards().size)
        node("demo_feedback_opened").assertTextEquals("Opened: none")
        node("demo_feedback_opened", german).assertTextEquals("Geöffnet: keins")
    }

    @Test
    fun germanStage_staysInsideA360dpWindow() {
        render()
        press("demo_feedback_burst", german)
        awaitCards(3, german)

        // A back card is scaled about its top centre, which getUnclippedBoundsInRoot reports as a shifted
        // left edge with the unscaled width. The visual box is boundsInRoot, checked against the 16dp
        // stage padding: a card that overflowed the stage would be clipped to it or beyond it.
        val windowPx = 360 * density.density
        val edgePx = 16 * density.density
        val cardBounds = composeTestRule.onAllNodes(inDemo(FEEDBACK_STACK_ITEM_TEST_TAG, german), useUnmergedTree = true)
            .fetchSemanticsNodes().map { it.boundsInRoot }
        assertEquals(3, cardBounds.size)
        cardBounds.forEach {
            assertTrue("card left ${it.left} must be >= $edgePx", it.left >= edgePx - 1f)
            assertTrue("card right ${it.right} must be <= ${windowPx - edgePx}", it.right <= windowPx - edgePx + 1f)
        }
        val tags = listOf(
            "demo_feedback_note",
            "demo_feedback_note_undo",
            "demo_feedback_plain",
            "demo_feedback_burst",
            "demo_feedback_short",
            "demo_feedback_long",
            "demo_feedback_dismiss_all",
            "demo_feedback_result",
            "demo_feedback_opened"
        )
        var checked = 0
        tags.forEach { tag ->
            val matches = composeTestRule.onAllNodes(inDemo(tag, german), useUnmergedTree = true)
            matches.fetchSemanticsNodes().indices.forEach { index ->
                val bounds = matches[index].getUnclippedBoundsInRoot()
                assertTrue("$tag[$index] left ${bounds.left} must be >= 0", bounds.left >= 0.dp)
                assertTrue("$tag[$index] right ${bounds.right} must be <= 360dp", bounds.right <= 360.dp)
                checked++
            }
        }
        assertTrue("the bounds check reached the buttons and readouts ($checked nodes)", checked >= 9)

        val inCards = hasAnyAncestor(inDemo(FEEDBACK_STACK_ITEM_TEST_TAG, german))
        val nodes = composeTestRule.onAllNodes(inCards, useUnmergedTree = true).fetchSemanticsNodes()
        var laidOut = 0
        nodes.forEach { semantics ->
            val action = semantics.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action ?: return@forEach
            val results = mutableListOf<TextLayoutResult>()
            action.invoke(results)
            results.forEach { assertFalse("a card text overflows its height", it.didOverflowHeight) }
            laidOut++
        }
        assertTrue("card texts were laid out ($laidOut)", laidOut >= 3)
    }
}
