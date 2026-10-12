package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Geometry proof for the Receipt pinned action bar (ROADMAP SC2): at 384dp the start action's start
 * edge sits at 16dp and the end action's end edge at 368dp, the 1dp divider lies above the buttons,
 * and a body taller than its host never starves the bar. Bounds are read UNCLIPPED in px against the
 * density-scaled dp value, one `setContent` per test method, no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w384dp-h800dp")
class OutcomeSheetReceiptBarGeometryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var density = 1f

    private fun setContent(outcome: VoiceOutcomeUiState, hostHeightDp: Int = 800) {
        composeTestRule.setContent {
            density = LocalDensity.current.density
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.width(384.dp).height(hostHeightDp.dp)) {
                    OutcomeSheetReceiptContent(outcome)
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun tagged(tag: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    /** Unclipped horizontal layout edges in root px: (left, right). */
    private fun SemanticsNodeInteraction.edges(): Pair<Float, Float> {
        val node = fetchSemanticsNode()
        val left = node.positionInRoot.x
        return left to left + node.size.width
    }

    private fun SemanticsNodeInteraction.vertical(): Pair<Float, Float> {
        val node = fetchSemanticsNode()
        val top = node.positionInRoot.y
        return top to top + node.size.height
    }

    @Test
    fun `at 384dp the cancel starts at 16dp and the confirm ends at 368dp`() {
        setContent(OutcomeFixtures.needsConfirmation())

        val (cancelLeft, _) = tagged("outcome_sheet_confirmation_cancel").edges()
        val (_, confirmRight) = tagged("outcome_sheet_confirmation_confirm").edges()

        assertEquals("cancel start edge", 16f * density, cancelLeft, 1f)
        assertEquals("confirm end edge", 368f * density, confirmRight, 1f)
    }

    @Test
    fun `the action bar spans the full 384dp width`() {
        setContent(OutcomeFixtures.needsConfirmation())

        val (left, right) = tagged("outcome_sheet_action_bar").edges()

        assertEquals("bar start edge", 0f, left, 1f)
        assertEquals("bar end edge", 384f * density, right, 1f)
    }

    @Test
    fun `the divider is 1dp thick and sits above both buttons`() {
        setContent(OutcomeFixtures.needsConfirmation())

        val (dividerTop, dividerBottom) = tagged("outcome_sheet_action_bar_divider").vertical()
        val (cancelTop, _) = tagged("outcome_sheet_confirmation_cancel").vertical()
        val (confirmTop, _) = tagged("outcome_sheet_confirmation_confirm").vertical()

        assertEquals("divider thickness", 1f * density, dividerBottom - dividerTop, 1f)
        assertTrue("divider bottom $dividerBottom must be above the cancel top $cancelTop", dividerBottom <= cancelTop + 1f)
        assertTrue("divider bottom $dividerBottom must be above the confirm top $confirmTop", dividerBottom <= confirmTop + 1f)
    }

    @Test
    fun `a body taller than a 240dp host never starves the bar buttons`() {
        setContent(OutcomeFixtures.tallNeedsConfirmation(), hostHeightDp = 240)

        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(40.dp)
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_cancel")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(40.dp)
    }

    @Test
    fun `at 384dp the Success start action starts at 16dp and the end action ends at 368dp`() {
        setContent(
            OutcomeFixtures.success(
                startAction = OutcomeFixtures.sheetAction(label = "Undo"),
                endAction = OutcomeFixtures.sheetAction(label = "Done")
            )
        )

        val (startLeft, _) = tagged("outcome_sheet_action_start").edges()
        val (_, endRight) = tagged("outcome_sheet_action_end").edges()

        assertEquals("Success start edge", 16f * density, startLeft, 1f)
        assertEquals("Success end edge", 368f * density, endRight, 1f)
    }
}
