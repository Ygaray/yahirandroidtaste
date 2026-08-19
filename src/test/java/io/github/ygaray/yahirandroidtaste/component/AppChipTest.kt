package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [AppChip]'s additive `onDoubleClick` parameter (Phase 106-02, TAG-02).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([TagListItemTest]):
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`. The
 * composable is rendered directly with no theme wrapper, matching the established convention.
 *
 * Double-tap and long-press gestures are driven through the compose test rule's touch-input API
 * ([performTouchInput] with [doubleClick]/[longClick]) rather than issuing two separate clicks, so
 * the gesture actually reaches the [AppChip] inner Surface's `combinedClickable` modifier as a real
 * double-tap/long-press — not merely two independent single-tap invocations.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `double-tap with a non-null onDoubleClick invokes it exactly once`() {
        var doubleClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = {},
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "Double-tapping a chip with a non-null onDoubleClick must invoke it exactly once",
            1,
            doubleClickCount
        )
    }

    @Test
    fun `single click with a non-null onDoubleClick still invokes onClick exactly once`() {
        var clickCount = 0
        var doubleClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = { clickCount++ },
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.waitUntil(timeoutMillis = 2_000) { clickCount == 1 }

        assertEquals(
            "A single click on a chip whose double-tap slot is occupied must still invoke onClick" +
                " exactly once",
            1,
            clickCount
        )
        assertEquals(
            "A single click must never invoke onDoubleClick",
            0,
            doubleClickCount
        )
    }

    @Test
    fun `double-tap with both callbacks supplied fires only onDoubleClick, never onLongClick`() {
        var doubleClickCount = 0
        var longClickCount = 0

        composeTestRule.setContent {
            AppChip(
                label = "Work",
                isSelected = false,
                onClick = {},
                onLongClick = { longClickCount++ },
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "A double-tap must invoke onDoubleClick exactly once when both callbacks are supplied",
            1,
            doubleClickCount
        )
        assertEquals(
            "A double-tap must invoke onLongClick exactly zero times",
            0,
            longClickCount
        )
    }
}
