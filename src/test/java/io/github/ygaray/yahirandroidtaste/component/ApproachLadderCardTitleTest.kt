package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The optional `title` of [ApproachLadderCard] (v2.8.1, Yahir's pick M, T-3109): a header above the
 * card that reads as a heading. No title, or a blank one, renders today's tree, with the caller's
 * modifier on the card surface itself (F10, F12).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp")
class ApproachLadderCardTitleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ladder = listOf(
        ApproachRungUiModel(id = "grammar", label = "Local grammar", rank = 0, enabled = true, offlineCapable = true),
        ApproachRungUiModel(id = "single", label = "Single-shot", rank = 1, enabled = true, offlineCapable = false)
    )

    private fun set(title: String?) {
        composeTestRule.setContent {
            Box {
                ApproachLadderCard(ladder = ladder, modifier = Modifier.semantics { contentDescription = CALLER }, title = title)
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun caller() = composeTestRule.onNode(hasContentDescription(CALLER), useUnmergedTree = true)

    private fun assertTodaysTree() {
        composeTestRule.onNodeWithTag(TITLE_TAG, useUnmergedTree = true).assertDoesNotExist()
        // The caller's modifier is on the surface node itself, as before v2.8.1.
        caller().assert(SemanticsMatcher.expectValue(SemanticsProperties.TestTag, SURFACE_TAG))
    }

    @Test
    fun noTitle_rendersTodaysTree() {
        set(null)
        assertTodaysTree()
    }

    @Test
    fun blankTitle_rendersTodaysTree() {
        set("  ")
        assertTodaysTree()
    }

    @Test
    fun title_isAHeading_aboveTheCard() {
        set("Command approach")
        composeTestRule.onNodeWithTag(TITLE_TAG, useUnmergedTree = true)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeTestRule.onNodeWithText("Command approach").assertExists()
        val title = composeTestRule.onNodeWithTag(TITLE_TAG, useUnmergedTree = true).getUnclippedBoundsInRoot()
        val surface = composeTestRule.onNodeWithTag(SURFACE_TAG, useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("the title sits above the card ($title vs $surface)", title.bottom <= surface.top)
        // The caller's modifier now wraps title and card together.
        caller().assert(SemanticsMatcher.expectValue(SemanticsProperties.TestTag, SURFACE_TAG).not())
        val caller = caller().getUnclippedBoundsInRoot()
        assertEquals(title.top, caller.top)
        assertEquals(surface.bottom, caller.bottom)
    }

    private companion object {
        const val CALLER = "caller"
        const val TITLE_TAG = "approach_ladder_card_title"
        const val SURFACE_TAG = "approach_ladder_card_surface"
    }
}
