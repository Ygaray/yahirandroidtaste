package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [CountBadge] (GADGET-03, Phase 86).
 *
 * Locks the "999+" overflow convention that survived the `:app` -> `:yahirandroidtaste`
 * relocation verbatim (D-02/D-03 parity).
 *
 * Infra mirrors this module's established Robolectric+Compose harness
 * ([DynamicActionButtonTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CountBadgeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders raw count under 999`() {
        composeTestRule.setContent {
            CountBadge(count = 42, tileAccentColor = Color.Blue)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("42").assertExists()
    }

    @Test
    fun `renders 999+ overflow above 999`() {
        composeTestRule.setContent {
            CountBadge(count = 1200, tileAccentColor = Color.Blue)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("999+").assertExists()
    }
}
