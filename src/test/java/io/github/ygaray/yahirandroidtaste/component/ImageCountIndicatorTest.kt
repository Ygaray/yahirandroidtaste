package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [ImageCountIndicator] (IMG-02), rendering the shared composable directly
 * with no card and no sheet around it — matching the established convention in this package
 * ([DynamicActionButtonTest]).
 *
 * The zero and negative cases lock the conditional-render-no-dead-space convention: nothing
 * composes at all, so both node types are asserted absent with existence-negative assertions
 * (a rendered-but-blank cluster cannot pass). The multi-digit cases (ten, 137) lock the
 * deliberate no-cap contract that distinguishes this cluster from the hub's existing
 * [CountBadge] pill count badge — the exact integer always renders, with no cap, abbreviation,
 * or truncation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ImageCountIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `zero count composes no accessibility node and no visible text`() {
        composeTestRule.setContent {
            ImageCountIndicator(imageCount = 0)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("0 inline images").assertDoesNotExist()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun `negative count also composes nothing, exercising the not-positive guard`() {
        composeTestRule.setContent {
            ImageCountIndicator(imageCount = -1)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("-1 inline images").assertDoesNotExist()
        composeTestRule.onNodeWithText("-1").assertDoesNotExist()
    }

    @Test
    fun `count of one renders the numeral 1 with singular accessibility wording`() {
        composeTestRule.setContent {
            ImageCountIndicator(imageCount = 1)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1").assertExists()
        composeTestRule.onNodeWithContentDescription("1 inline image").assertExists()
    }

    @Test
    fun `count of two renders plural accessibility wording, proving the branch is value-driven`() {
        composeTestRule.setContent {
            ImageCountIndicator(imageCount = 2)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("2").assertExists()
        composeTestRule.onNodeWithContentDescription("2 inline images").assertExists()
    }

    @Test
    fun `count of ten renders the exact two-character numeral with no cap or abbreviation`() {
        composeTestRule.setContent {
            ImageCountIndicator(imageCount = 10)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("10").assertExists()
        composeTestRule.onNodeWithContentDescription("10 inline images").assertExists()
    }

    @Test
    fun `count of 137 renders the exact three-digit numeral — the precision truth`() {
        composeTestRule.setContent {
            ImageCountIndicator(imageCount = 137)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("137").assertExists()
        composeTestRule.onNodeWithContentDescription("137 inline images").assertExists()
    }
}
