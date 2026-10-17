package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * FOPEN-01: the single-target go-to-item row of a success sheet, in the Receipt and Classic styles,
 * through both public [OutcomeSheet] overloads. Robolectric tree assertions only; behavior on a real
 * device belongs to the Gate-1 tester.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetOpenTargetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var state: SheetState
    private var dismissals = 0
    private val opened = mutableListOf<FeedbackTarget>()

    private val target = FeedbackTarget("item-42", "Open item")

    private fun targetSuccess() = VoiceOutcomeUiState.Success(
        summary = "Saved",
        target = target,
        targetLabel = "Weekend list",
        targetDetail = "Updated just now"
    )

    private fun hostHoisted(outcome: VoiceOutcomeUiState, style: OutcomeSheetStyle) {
        composeTestRule.setContent {
            state = rememberModalBottomSheetState()
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = { dismissals++ },
                style = style,
                sheetState = state,
                onOpenTarget = { opened += it }
            )
        }
        composeTestRule.waitForIdle()
    }

    private fun row(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(OPEN_TARGET_ROW_TEST_TAG, useUnmergedTree = true)

    @Test
    fun receipt_targetRow_tapOpensOnce_viaHoistedOverload() {
        hostHoisted(targetSuccess(), OutcomeSheetStyle.Receipt)

        val config = row().assertExists().fetchSemanticsNode().config
        assertEquals("Open item", config[SemanticsActions.OnClick].label)
        assertEquals(Role.Button, config[SemanticsProperties.Role])

        row().performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(target), opened)
        assertEquals("the library must never call the dismiss callback", 0, dismissals)
        assertTrue("the library must never hide the sheet", state.isVisible)
    }
}
