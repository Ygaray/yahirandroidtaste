package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit + Compose tests for [CycleSubTypeButton] / [nextSubType] (EDIT-02, D-07/D-08/D-09).
 *
 * Locks:
 *  - The fixed cycle order (BULLETED -> ORDERED -> CHECKBOX -> BULLETED) plus the
 *    unrecognized-value -> BULLETED fallback, as a plain (no-Compose) JUnit test of the pure
 *    [nextSubType] function.
 *  - The predictive-icon contract (D-08): the rendered contentDescription always names the NEXT
 *    sub-type, not the current one.
 *  - Tap behavior: [onCycle] is invoked exactly once with `nextSubType(currentSubType)`.
 *  - Disabled gating: `enabled = false` does not invoke [onCycle] on tap (readOnly surfaces,
 *    T-65-01).
 *
 * Infra mirrors this module's established Robolectric+Compose harness
 * (`UndoCenterScreenTest.kt`): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()` — `isIncludeAndroidResources = true` in `yahirandroidtaste/build.gradle.kts`
 * enables this. Renders [CycleSubTypeButton] directly (no Hilt, no wrapping screen).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CycleSubTypeButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- nextSubType: pure function, no Compose needed ---

    @Test
    fun `nextSubType cycles bulleted to ordered`() {
        assertEquals("ORDERED", nextSubType("BULLETED"))
    }

    @Test
    fun `nextSubType cycles ordered to checkbox`() {
        assertEquals("CHECKBOX", nextSubType("ORDERED"))
    }

    @Test
    fun `nextSubType cycles checkbox to bulleted`() {
        assertEquals("BULLETED", nextSubType("CHECKBOX"))
    }

    @Test
    fun `nextSubType falls back to bulleted for an unrecognized value`() {
        assertEquals("BULLETED", nextSubType("SOMETHING_UNKNOWN"))
    }

    // --- Predictive icon (D-08): contentDescription names the NEXT sub-type ---

    @Test
    fun `from bulleted shows the ordered predictive description`() {
        composeTestRule.setContent {
            CycleSubTypeButton(currentSubType = "BULLETED", onCycle = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Switch to Ordered list").assertExists()
    }

    @Test
    fun `from ordered shows the checkbox predictive description`() {
        composeTestRule.setContent {
            CycleSubTypeButton(currentSubType = "ORDERED", onCycle = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Switch to Checkbox list").assertExists()
    }

    @Test
    fun `from checkbox shows the bulleted predictive description`() {
        composeTestRule.setContent {
            CycleSubTypeButton(currentSubType = "CHECKBOX", onCycle = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Switch to Bulleted list").assertExists()
    }

    // --- Tap behavior ---

    @Test
    fun `tapping the enabled button invokes onCycle exactly once with the next sub-type`() {
        var captured: String? = null
        var callCount = 0

        composeTestRule.setContent {
            CycleSubTypeButton(
                currentSubType = "BULLETED",
                onCycle = {
                    captured = it
                    callCount++
                }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Switch to Ordered list").performClick()
        composeTestRule.waitForIdle()

        assertEquals("ORDERED", captured)
        assertEquals(1, callCount)
    }

    // --- Disabled gating (T-65-01: readOnly surfaces never mutate) ---

    @Test
    fun `a disabled button does not invoke onCycle on tap`() {
        var captured: String? = null

        composeTestRule.setContent {
            CycleSubTypeButton(
                currentSubType = "BULLETED",
                onCycle = { captured = it },
                enabled = false
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Switch to Ordered list").performClick()
        composeTestRule.waitForIdle()

        assertNull("Disabled button must not invoke onCycle", captured)
    }
}
