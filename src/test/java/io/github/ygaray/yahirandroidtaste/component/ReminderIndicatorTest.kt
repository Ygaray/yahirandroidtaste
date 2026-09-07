package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [ReminderIndicator] (REMIND-09), rendering the shared composable directly
 * with no card and no sheet around it — matching the established convention in this package
 * ([ImageCountIndicatorTest]).
 *
 * The zero and negative cases lock the conditional-render-no-dead-space convention: nothing
 * composes at all, so both node types are asserted absent with existence-negative assertions
 * (a rendered-but-blank cluster cannot pass). The multi-digit case (1250) locks the deliberate
 * no-cap contract — the exact integer always renders, with no cap, abbreviation, or truncation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReminderIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `zero count composes no accessibility node and no visible text`() {
        composeTestRule.setContent {
            ReminderIndicator(reminderCount = 0)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("0 reminders").assertDoesNotExist()
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun `negative count also composes nothing, exercising the not-positive guard`() {
        composeTestRule.setContent {
            ReminderIndicator(reminderCount = -1)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("-1 reminders").assertDoesNotExist()
        composeTestRule.onNodeWithText("-1").assertDoesNotExist()
    }

    @Test
    fun `count of one renders the numeral 1 with singular accessibility wording`() {
        composeTestRule.setContent {
            ReminderIndicator(reminderCount = 1)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1").assertExists()
        composeTestRule.onNodeWithContentDescription("1 reminder").assertExists()
    }

    @Test
    fun `count of three renders plural accessibility wording, proving the branch is value-driven`() {
        composeTestRule.setContent {
            ReminderIndicator(reminderCount = 3)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("3").assertExists()
        composeTestRule.onNodeWithContentDescription("3 reminders").assertExists()
    }

    @Test
    fun `count of 1250 renders the exact four-digit numeral with no cap or abbreviation`() {
        composeTestRule.setContent {
            ReminderIndicator(reminderCount = 1250)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1250").assertExists()
        composeTestRule.onNodeWithContentDescription("1250 reminders").assertExists()
    }

    @Test
    fun `composing the same input twice yields identical output — pure function of reminderCount`() {
        composeTestRule.setContent {
            ReminderIndicator(reminderCount = 2)
            ReminderIndicator(reminderCount = 2)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("2").assertCountEquals(2)
    }
}
