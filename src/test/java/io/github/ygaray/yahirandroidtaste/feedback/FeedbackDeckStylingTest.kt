package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import io.github.ygaray.yahirandroidtaste.theme.DarkColorScheme
import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * v2.8.1 deck styling (Yahir's picks A F J, T-3109, D-1683): 24dp side insets, an 8dp bottom inset,
 * the whole deck as one offscreen layer at 0.92, and inverse card colours. An empty deck still takes
 * no space (F1 in `.planning/quick/2026-11-13-v2.8.1-deck-styling/RULINGS.md`).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp")
class FeedbackDeckStylingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun items(count: Int): List<FeedbackStackItem> =
        (0 until count).map { FeedbackStackItem(it.toLong(), "m$it", null, null, false, null, null) }

    private fun setDeck(count: Int, direction: LayoutDirection = LayoutDirection.Ltr) {
        val deckItems = items(count)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                Box(Modifier.testTag(OUTER)) {
                    FeedbackDeck(items = deckItems) { _, index ->
                        Box(Modifier.fillMaxWidth().height(60.dp).testTag("c$index"))
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun bounds(tag: String) =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()

    @Test
    fun card_isInset24dpFromEachSide_at360dp() {
        setDeck(2)
        val card = bounds("c0")
        assertEquals(24f, card.left.value, 0.5f)
        assertEquals(24f, 360f - card.right.value, 0.5f)
    }

    @Test
    @Config(qualifiers = "w840dp-h600dp")
    fun card_staysCappedAt560dp_andCentered_at840dp() {
        setDeck(2)
        val card = bounds("c0")
        assertEquals(560f, card.width.value, 0.5f)
        assertEquals(140f, card.left.value, 0.5f)
        assertEquals(140f, 840f - card.right.value, 0.5f)
    }

    @Test
    fun deck_leaves8dpBelowTheFrontCard() {
        setDeck(2)
        assertEquals(8f, bounds(OUTER).bottom.value - bounds("c0").bottom.value, 0.5f)
    }

    @Test
    fun insets_areSymmetric_inRtl() {
        setDeck(2, LayoutDirection.Rtl)
        val card = bounds("c0")
        assertEquals(24f, card.left.value, 0.5f)
        assertEquals(24f, 360f - card.right.value, 0.5f)
    }

    @Test
    fun emptyDeck_outerNode_isZeroByZero() {
        setDeck(0)
        val outer = bounds(OUTER)
        assertEquals(0f, outer.width.value, 0.5f)
        assertEquals(0f, outer.height.value, 0.5f)
    }

    @Test
    fun deck_isOneOffscreenLayer_at92Percent() {
        assertEquals(0.92f, FEEDBACK_DECK_ALPHA, 0f)
        setDeck(2)
        val expected = Modifier.graphicsLayer(
            alpha = FEEDBACK_DECK_ALPHA,
            compositingStrategy = CompositingStrategy.Offscreen
        )
        val modifiers = composeTestRule.onNodeWithTag(FEEDBACK_STACK_DECK_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNode().layoutInfo.getModifierInfo().map { it.modifier }
        assertTrue("the deck node carries the offscreen 0.92 layer: $modifiers", modifiers.any { it == expected })
    }

    @Test
    fun cardColors_areTheInverseRoles_inLightAndDark() {
        listOf(LightColorScheme, DarkColorScheme).forEach { scheme ->
            val colors = feedbackCardColors(scheme)
            assertEquals(scheme.inverseSurface, colors.container)
            assertEquals(scheme.inverseOnSurface, colors.content)
            assertEquals(scheme.inverseOnSurface.copy(alpha = 0.78f), colors.supporting)
            assertEquals(scheme.inversePrimary, colors.accent)
        }
    }

    private companion object {
        const val OUTER = "outer"
    }
}
