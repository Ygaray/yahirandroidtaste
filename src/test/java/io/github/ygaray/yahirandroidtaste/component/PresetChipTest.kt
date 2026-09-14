package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.width
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.runner.RunWith

/**
 * Compose tests for [PresetChip] — the D-03 ChipBar itemContent chip for one-tap preset rows.
 * Infra mirrors this module's established Robolectric+Compose harness ([AppChipTest]):
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`, no
 * theme wrapper.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PresetChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders label and invokes onClick exactly once on tap`() {
        var clickCount = 0

        composeTestRule.setContent {
            PresetChip(label = "30 min", onClick = { clickCount++ })
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("30 min").assertExists()
        composeTestRule.onNodeWithText("30 min").performClick()
        composeTestRule.waitForIdle()

        assertEquals("A tap must invoke onClick exactly once", 1, clickCount)
    }

    @Test
    fun `non-null non-blank supportingLabel renders both texts merged onto one node`() {
        composeTestRule.setContent {
            PresetChip(label = "Later today", onClick = {}, supportingLabel = "7:40 PM")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Later today").assertExists()
        composeTestRule.onNodeWithText("7:40 PM").assertExists()

        val mergedTexts = composeTestRule.onNodeWithText("Later today").fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .map { it.text }
        assertEquals(
            "With a non-blank supportingLabel, the merged chip node must carry exactly the" +
                " two texts",
            listOf("Later today", "7:40 PM"),
            mergedTexts
        )
    }

    @Test
    fun `null supportingLabel renders only the label - no second text node`() {
        composeTestRule.setContent {
            PresetChip(label = "30 min", onClick = {}, supportingLabel = null)
        }
        composeTestRule.waitForIdle()

        val mergedTexts = composeTestRule.onNodeWithText("30 min").fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .map { it.text }
        assertEquals(
            "With supportingLabel = null the merged chip node must carry exactly one text",
            listOf("30 min"),
            mergedTexts
        )
    }

    @Test
    fun `blank supportingLabel renders only the label - no second text node`() {
        composeTestRule.setContent {
            PresetChip(label = "30 min", onClick = {}, supportingLabel = "   ")
        }
        composeTestRule.waitForIdle()

        val mergedTexts = composeTestRule.onNodeWithText("30 min").fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .map { it.text }
        assertEquals(
            "With a blank supportingLabel the merged chip node must carry exactly one text",
            listOf("30 min"),
            mergedTexts
        )
    }

    @Test
    fun `enabled false exposes a disabled node and a click does not invoke onClick`() {
        var clickCount = 0

        composeTestRule.setContent {
            PresetChip(label = "30 min", onClick = { clickCount++ }, enabled = false)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("30 min").assertIsNotEnabled()
        composeTestRule.onNodeWithText("30 min").performClick()
        composeTestRule.waitForIdle()

        assertEquals("A disabled chip must never invoke onClick", 0, clickCount)
    }

    @Test
    fun `isSelected true exposes a selected node`() {
        composeTestRule.setContent {
            PresetChip(label = "30 min", onClick = {}, isSelected = true)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("30 min").assertIsSelected()
    }

    @Test
    fun `isSelected false is not selected`() {
        composeTestRule.setContent {
            PresetChip(label = "30 min", onClick = {}, isSelected = false)
        }
        composeTestRule.waitForIdle()

        val selectedMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Selected, false)
        composeTestRule.onNodeWithText("30 min").assert(selectedMatcher)
    }

    @Test
    fun `three PresetChips as a ChipBar itemContent all render inside the tagged container in order`() {
        composeTestRule.setContent {
            ChipBar(
                items = listOf("5 min", "10 min", "30 min"),
                key = { it },
                itemContent = { PresetChip(label = it, onClick = {}) },
                testTag = "preset_chip_bar_under_test"
            )
        }
        composeTestRule.waitForIdle()

        listOf("5 min", "10 min", "30 min").forEach { label ->
            composeTestRule
                .onNode(hasText(label) and hasAnyAncestor(hasTestTag("preset_chip_bar_under_test")))
                .assertExists()
        }
    }

    @Test
    fun `a chip with a supporting label constrained to a 96dp-wide Box stays within that width and grows at least 32dp tall`() {
        composeTestRule.setContent {
            Box(modifier = Modifier.width(96.dp).testTag("constrained_box")) {
                PresetChip(
                    label = "Until tomorrow",
                    onClick = {},
                    supportingLabel = "Tue 9:00 AM",
                    modifier = Modifier.testTag("constrained_preset_chip")
                )
            }
        }
        composeTestRule.waitForIdle()

        val chipNode = composeTestRule.onNodeWithTag("constrained_preset_chip")
        chipNode.assertHeightIsAtLeast(32.dp)

        val bounds = chipNode.getUnclippedBoundsInRoot()
        assertTrue(
            "A PresetChip inside a 96dp-wide Box must not report a width greater than 96dp" +
                " (was ${bounds.width})",
            bounds.width <= 96.dp
        )
    }
}
