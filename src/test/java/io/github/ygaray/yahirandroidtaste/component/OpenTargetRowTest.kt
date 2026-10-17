package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The internal go-to-item row on its own (FOPEN-01, brief D-10 and D-11): one merged node, a button
 * with the target's open label as the click label, no state description, a decorative chevron, a 48dp
 * minimum height and a locked variant for an in-flight success (PD-1).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OpenTargetRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Open item")
    private val opened = mutableListOf<FeedbackTarget>()

    private fun host(
        detail: String? = "Updated just now",
        openLabel: String = "Open item",
        enabled: Boolean = true
    ) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                OpenTargetRow(
                    title = "Weekend list",
                    detail = detail,
                    target = FeedbackTarget("item-42", openLabel),
                    onOpenTarget = { opened += it },
                    enabled = enabled
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun mergedRow(): SemanticsNodeInteraction = composeTestRule.onNodeWithTag(OPEN_TARGET_ROW_TEST_TAG)

    private fun mergedTexts(): List<String> =
        mergedRow().fetchSemanticsNode().config[SemanticsProperties.Text].map { it.text }

    @Test
    fun rowIsOneMergedButton_withOpenLabelAsClickLabel_andNoStateDescription() {
        host()

        val config = mergedRow().assertExists().fetchSemanticsNode().config
        assertEquals(Role.Button, config[SemanticsProperties.Role])
        assertEquals("Open item", config[SemanticsActions.OnClick].label)
        assertFalse("the row announces no Expanded or Collapsed", config.contains(SemanticsProperties.StateDescription))
        assertEquals(listOf("Weekend list", "Updated just now"), mergedTexts())
    }

    @Test
    fun blankOpenLabel_fallsBackToTheTitleAsClickLabel() {
        host(openLabel = "   ")

        val config = mergedRow().fetchSemanticsNode().config
        assertEquals("Weekend list", config[SemanticsActions.OnClick].label)
    }

    @Test
    fun nullDetail_drawsNoSecondLine() {
        host(detail = null)

        assertEquals(listOf("Weekend list"), mergedTexts())
    }

    @Test
    fun chevronIsDecorative_noContentDescription() {
        host()

        val chevron = composeTestRule.onNodeWithTag(OPEN_TARGET_CHEVRON_TEST_TAG, useUnmergedTree = true)
            .assertExists()
            .fetchSemanticsNode()
        assertFalse(chevron.config.contains(SemanticsProperties.ContentDescription))
    }

    @Test
    fun clickableRow_isAtLeast48dpHigh() {
        host(detail = null)

        val heightPx = mergedRow().fetchSemanticsNode().size.height
        val minPx = 48f * composeTestRule.density.density
        assertTrue("row height $heightPx px must be at least $minPx px", heightPx >= minPx - 1f)
    }

    @Test
    fun lockedRow_hasNoClickAction_noRole_butKeepsItsText() {
        host(enabled = false)

        val config = mergedRow().assertExists().fetchSemanticsNode().config
        assertFalse("a locked row has no click action", config.contains(SemanticsActions.OnClick))
        assertFalse("a locked row has no button role", config.contains(SemanticsProperties.Role))
        assertEquals(listOf("Weekend list", "Updated just now"), mergedTexts())
        composeTestRule.onNodeWithTag(OPEN_TARGET_CHEVRON_TEST_TAG, useUnmergedTree = true).assertExists()
    }

    @Test
    fun tapCallsTheHandlerOnceWithTheTarget() {
        host()

        mergedRow().performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(target), opened)
    }
}
