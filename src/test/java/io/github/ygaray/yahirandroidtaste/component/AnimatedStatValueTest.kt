package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.animation.core.tween
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [AnimatedStatValue] (Phase 44-02, DS-03).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([CountBadgeTest]):
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`.
 *
 * Every test uses `tween(durationMillis = 0)` so the count-up animation settles synchronously to
 * `targetValue` within a single frame, making the rendered text deterministic without depending
 * on the test rule's animation-clock advancement behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AnimatedStatValueTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `default format renders the bare integer string`() {
        composeTestRule.setContent {
            AnimatedStatValue(targetValue = 42f, animationSpec = tween(durationMillis = 0))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("42").assertExists()
    }

    @Test
    fun `caller-supplied format override is honored`() {
        composeTestRule.setContent {
            AnimatedStatValue(
                targetValue = 42f,
                animationSpec = tween(durationMillis = 0),
                format = { "$" + it.toInt() }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("$42").assertExists()
    }

    @Test
    fun `renders with a zero target value without throwing`() {
        composeTestRule.setContent {
            AnimatedStatValue(targetValue = 0f, animationSpec = tween(durationMillis = 0))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("0").assertExists()
    }

    @Test
    fun `precision contract - default format truncates toward zero, not round-half-up`() {
        composeTestRule.setContent {
            AnimatedStatValue(targetValue = 41.9f, animationSpec = tween(durationMillis = 0))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("41").assertExists()
    }
}
