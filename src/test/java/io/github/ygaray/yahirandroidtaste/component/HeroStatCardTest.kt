package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [HeroStatCard] (Phase 44-02, DS-03).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([CountBadgeTest]), with
 * the callback-capture-via-mutable-var tap-interaction pattern from [AppChipTest]:
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HeroStatCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders both label and value text`() {
        composeTestRule.setContent {
            HeroStatCard(label = "Total", value = "1,204")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Total").assertExists()
        composeTestRule.onNodeWithText("1,204").assertExists()
    }

    @Test
    fun `tapping the card with a non-null onClick invokes it exactly once`() {
        var callCount = 0

        composeTestRule.setContent {
            HeroStatCard(label = "Total", value = "1,204", onClick = { callCount++ })
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("1,204").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Tapping a HeroStatCard with a non-null onClick must invoke it exactly once",
            1,
            callCount
        )
    }

    @Test
    fun `onClick null composes with no clickable semantics node and does not throw`() {
        composeTestRule.setContent {
            HeroStatCard(label = "Total", value = "1,204", onClick = null)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Total").assertExists()
        composeTestRule.onNodeWithText("Total").assertHasNoClickAction()
    }

    @Test
    fun `empty label and value compose without throwing`() {
        composeTestRule.setContent {
            HeroStatCard(label = "", value = "")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().assertExists()
    }
}
