package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The appended `OutcomeSheet(sheetState)` parameter (CalTracker 76-14, D-UAT-1): a consumer that
 * hoists the sheet state can slide a finished sheet down with `hide()` (the animation runs on the
 * state the sheet is actually attached to) before it clears the outcome, in both styles. Swapping the
 * outcome under one call site keeps the same hoisted state visible. Omitting the parameter keeps the
 * previous internal state (every pre-existing OutcomeSheet test runs that path unchanged).
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetHoistedStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var state: SheetState
    private lateinit var scope: CoroutineScope
    private var dismissals = 0

    private fun hostHoisted(outcome: VoiceOutcomeUiState, style: OutcomeSheetStyle) {
        composeTestRule.setContent {
            state = rememberModalBottomSheetState()
            scope = rememberCoroutineScope()
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = { dismissals++ },
                style = style,
                sheetState = state
            )
        }
        composeTestRule.waitForIdle()
    }

    private fun hideAndSettle() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.runOnIdle { scope.launch { state.hide() } }
        composeTestRule.mainClock.advanceTimeBy(HIDE_ANIMATION_BUDGET_MS)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
    }

    private fun assertHideAnimatesTheHoistedState(style: OutcomeSheetStyle) {
        hostHoisted(OutcomeFixtures.needsConfirmation(), style)
        assertTrue("the hoisted state must report the shown sheet as visible", state.isVisible)
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm", useUnmergedTree = true).assertExists()

        hideAndSettle()

        assertFalse("hide() on the hoisted state must take the sheet down", state.isVisible)
        assertEquals("hide() itself must not invoke onDismissRequest", 0, dismissals)
    }

    @Test
    fun `Classic hide on a hoisted state takes the sheet down without a dismiss callback`() =
        assertHideAnimatesTheHoistedState(OutcomeSheetStyle.Classic)

    @Test
    fun `Receipt hide on a hoisted state takes the sheet down without a dismiss callback`() =
        assertHideAnimatesTheHoistedState(OutcomeSheetStyle.Receipt)

    private fun assertOutcomeSwapKeepsTheSheetUp(style: OutcomeSheetStyle) {
        var outcome: VoiceOutcomeUiState by mutableStateOf(OutcomeFixtures.needsConfirmation())
        val visibility = mutableListOf<Boolean>()
        composeTestRule.setContent {
            state = rememberModalBottomSheetState()
            LaunchedEffect(state) { snapshotFlow { state.isVisible }.collect { visibility += it } }
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = { dismissals++ },
                style = style,
                sheetState = state
            )
        }
        composeTestRule.waitForIdle()
        assertTrue(state.isVisible)

        outcome = VoiceOutcomeUiState.Success(summary = "Logged 1 item")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Logged 1 item").assertExists()
        assertTrue("the same hoisted state stays visible across the swap", state.isVisible)
        val firstShown = visibility.indexOf(true)
        assertTrue("the sheet must have been shown ($visibility)", firstShown >= 0)
        assertFalse(
            "no hide/show cycle after the first show ($visibility)",
            visibility.drop(firstShown).contains(false)
        )
        assertEquals(0, dismissals)
    }

    @Test
    fun `Classic outcome swap under one call site keeps the hoisted state visible`() =
        assertOutcomeSwapKeepsTheSheetUp(OutcomeSheetStyle.Classic)

    @Test
    fun `Receipt outcome swap under one call site keeps the hoisted state visible`() =
        assertOutcomeSwapKeepsTheSheetUp(OutcomeSheetStyle.Receipt)

    @Test
    fun `omitting sheetState still renders the sheet`() {
        composeTestRule.setContent {
            OutcomeSheet(outcome = OutcomeFixtures.needsConfirmation(), onDismissRequest = {})
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm", useUnmergedTree = true).assertExists()
    }

    private companion object {
        /** Comfortably longer than the M3 sheet hide animation; the clock is driven manually. */
        const val HIDE_ANIMATION_BUDGET_MS = 2_000L
    }
}
