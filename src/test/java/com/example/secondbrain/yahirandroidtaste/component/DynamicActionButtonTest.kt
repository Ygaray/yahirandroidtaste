package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [DynamicActionButton] (BTN-01, D-01).
 *
 * Locks:
 *  - The D-01 role-to-widget mapping's label rendering across all 3 [ActionButtonDefaults.ActionButtonRole]
 *    values (Save, Destructive, Neutral).
 *  - Tap behavior: an enabled button invokes [onClick] exactly once per tap.
 *  - Disabled gating: `enabled = false` does not invoke [onClick] on tap.
 *
 * Infra mirrors this module's established Robolectric+Compose harness
 * ([CycleSubTypeButtonTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`. Renders [DynamicActionButton] directly (no Hilt, no wrapping screen).
 * Node lookup is label-based ([onNodeWithText]) since this component is label-driven, not
 * icon-driven.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DynamicActionButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Label rendering across all 3 roles ---

    @Test
    fun `renders the label for the Save role`() {
        composeTestRule.setContent {
            DynamicActionButton(
                label = "Save",
                role = ActionButtonDefaults.ActionButtonRole.Save,
                onClick = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save").assertExists()
    }

    @Test
    fun `renders the label for the Destructive role`() {
        composeTestRule.setContent {
            DynamicActionButton(
                label = "Discard",
                role = ActionButtonDefaults.ActionButtonRole.Destructive,
                onClick = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Discard").assertExists()
    }

    @Test
    fun `renders the label for the Neutral role`() {
        composeTestRule.setContent {
            DynamicActionButton(
                label = "Cancel",
                role = ActionButtonDefaults.ActionButtonRole.Neutral,
                onClick = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cancel").assertExists()
    }

    // --- Tap behavior ---

    @Test
    fun `tapping an enabled button invokes onClick exactly once`() {
        var callCount = 0

        composeTestRule.setContent {
            DynamicActionButton(
                label = "Save",
                role = ActionButtonDefaults.ActionButtonRole.Save,
                onClick = { callCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, callCount)
    }

    // --- Disabled gating ---

    @Test
    fun `a disabled button does not invoke onClick on tap`() {
        var callCount = 0

        composeTestRule.setContent {
            DynamicActionButton(
                label = "Save",
                role = ActionButtonDefaults.ActionButtonRole.Save,
                enabled = false,
                onClick = { callCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()

        assertEquals(0, callCount)
    }
}
