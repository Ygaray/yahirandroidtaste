package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * VXR-01 / D-12: the refused-undo surface carries an Assertive live region so TalkBack announces
 * it when it appears, in both the Success and the Failure arm. The region sits on the refused
 * `Surface` only, so exactly one node defines the live region key when `refused` is set and none
 * when it is `null` (T-22-13). What TalkBack actually speaks is a Gate-2 read-out item (A9).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetLiveRegionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun host(outcome: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            Box(Modifier.height(800.dp)) { OutcomeSheetContent(outcome) }
        }
        composeTestRule.waitForIdle()
    }

    private fun liveRegionNodes() =
        composeTestRule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion),
            useUnmergedTree = true
        )

    private fun assertAssertiveRefusedSurface() {
        composeTestRule.onNodeWithTag("outcome_sheet_undo_refused", useUnmergedTree = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive))
        liveRegionNodes().assertCountEquals(1)
        composeTestRule.onNodeWithText("Couldn't undo: it was edited elsewhere").assertExists()
    }

    @Test
    fun `Success refused surface is an assertive live region and the only one`() {
        host(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        assertAssertiveRefusedSurface()
    }

    @Test
    fun `Failure refused surface is an assertive live region and the only one`() {
        host(OutcomeFixtures.failure(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        assertAssertiveRefusedSurface()
    }

    @Test
    fun `Success without a refusal defines no live region`() {
        host(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = null)))

        liveRegionNodes().assertCountEquals(0)
    }

    @Test
    fun `Failure without a refusal defines no live region`() {
        host(OutcomeFixtures.failure(undo = OutcomeFixtures.undo(refused = null)))

        liveRegionNodes().assertCountEquals(0)
    }

    @Test
    fun `Success without undo defines no live region`() {
        host(OutcomeFixtures.success())

        liveRegionNodes().assertCountEquals(0)
    }

    // -- Gate-1 C-1 (Phase 28-09): the live-region node itself carries the message --------------

    private fun refusedNodeText(): String {
        val config = composeTestRule.onNodeWithTag("outcome_sheet_undo_refused", useUnmergedTree = false)
            .fetchSemanticsNode().config
        return config.getOrNull(SemanticsProperties.Text).orEmpty().joinToString(" ") { it.text }
    }

    @Test
    fun `refusedSurface liveRegionNodeCarriesTheFullMessageAsItsOwnText`() {
        host(
            OutcomeFixtures.success(
                undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused(changedItem = "Oats"))
            )
        )

        assertEquals("Couldn't undo: it was edited elsewhere, Oats changed since", refusedNodeText())
    }

    @Test
    fun `refusedSurface withoutChangedItem nodeTextHasNoSuffix`() {
        host(OutcomeFixtures.failure(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        assertEquals("Couldn't undo: it was edited elsewhere", refusedNodeText())
    }

    @Test
    fun `refusedSurface isStillTheOnlyAssertiveRegion andHasNoClickAction`() {
        host(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        liveRegionNodes().assertCountEquals(1)
        val config = composeTestRule.onNodeWithTag("outcome_sheet_undo_refused", useUnmergedTree = false)
            .fetchSemanticsNode().config
        assertEquals(LiveRegionMode.Assertive, config.getOrNull(SemanticsProperties.LiveRegion))
        assertNull(config.getOrNull(SemanticsActions.OnClick))
    }
}
