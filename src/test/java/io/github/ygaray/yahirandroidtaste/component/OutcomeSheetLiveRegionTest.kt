package io.github.ygaray.yahirandroidtaste.component

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/** The id of the (deprecated) announcement event type, which the refusal route must never send. */
private const val ANNOUNCEMENT_EVENT_TYPE = 0x4000

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

    // -- Gate-1 C-1 (Phase 28-11): accessibility focus reaches the message once the sheet settles ---

    private val refusedTag = "outcome_sheet_undo_refused"

    private fun refusedConfig() =
        composeTestRule.onNodeWithTag(refusedTag, useUnmergedTree = false).fetchSemanticsNode().config

    @Test
    fun refusedNode_isFocusable_andNamedWithTheFullMessage() {
        host(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        val config = refusedConfig()
        val message = "Couldn't undo: it was edited elsewhere"
        assertTrue("the refusal must take input focus (it drives TalkBack)", config.contains(SemanticsProperties.Focused))
        // The explicit description, not only the drawn text: the live-region node carries a
        // non-empty name by construction.
        assertEquals(listOf(message), config.getOrNull(SemanticsProperties.ContentDescription))
        assertEquals(listOf(message), config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text })
    }

    @Test
    fun refusedNode_withChangedItem_nameIncludesTheSuffix() {
        host(
            OutcomeFixtures.failure(
                undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused(changedItem = "Oats"))
            )
        )

        assertEquals(
            listOf("Couldn't undo: it was edited elsewhere, Oats changed since"),
            refusedConfig().getOrNull(SemanticsProperties.ContentDescription)
        )
    }

    @Test
    fun refusedNode_hasNoClickAction_andIsTheOnlyAssertiveRegion() {
        host(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        liveRegionNodes().assertCountEquals(1)
        val config = refusedConfig()
        assertEquals(LiveRegionMode.Assertive, config.getOrNull(SemanticsProperties.LiveRegion))
        assertNull("the message is not a button", config.getOrNull(SemanticsActions.OnClick))
        assertNull(config.getOrNull(SemanticsProperties.Role))
    }

    private var reason by mutableStateOf("it was edited elsewhere")
    private var tick by mutableIntStateOf(0)
    private var gains = 0
    private val rival = FocusRequester()

    /** A refusal host with a rival focus target outside it, so a repeated request would be visible. */
    private fun hostWithRival() {
        var had = false
        composeTestRule.setContent {
            check(tick >= 0) // the read recomposes the host, which rebuilds an equal outcome
            Column(Modifier.height(800.dp)) {
                Box(
                    Modifier.onFocusChanged {
                        if (it.hasFocus && !had) gains++
                        had = it.hasFocus
                    }
                ) {
                    OutcomeSheetContent(
                        OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused(reason)))
                    )
                }
                Box(Modifier.testTag("rival").focusRequester(rival).focusable())
            }
        }
        settle()
    }

    private fun settle() {
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()
    }

    private fun refusedIsFocused() = composeTestRule.onNodeWithTag(refusedTag, useUnmergedTree = true)

    @Test
    fun exactlyOneFocusRequestPerShowing() {
        gains = 0
        hostWithRival()

        refusedIsFocused().assertIsFocused()
        assertEquals("one showing, one focus move", 1, gains)
    }

    @Test
    fun equalStateRecomposition_makesNoSecondRequest() {
        gains = 0
        tick = 0
        hostWithRival()
        refusedIsFocused().assertIsFocused()

        // The user (or the system) moved focus on; recomposing with an equal outcome must not take it back.
        composeTestRule.runOnIdle { rival.requestFocus() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("rival").assertIsFocused()
        tick++
        settle()
        tick++
        settle()

        composeTestRule.onNodeWithTag("rival").assertIsFocused()
        refusedIsFocused().assertIsNotFocused()
        assertEquals(1, gains)
    }

    @Test
    fun aNewRefusal_makesANewSingleRequest() {
        gains = 0
        reason = "it was edited elsewhere"
        hostWithRival()
        refusedIsFocused().assertIsFocused()
        composeTestRule.runOnIdle { rival.requestFocus() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("rival").assertIsFocused()

        reason = "it was removed elsewhere"
        settle()

        refusedIsFocused().assertIsFocused()
        assertEquals("a new message is a new showing: exactly one more move", 2, gains)
        assertEquals(
            listOf("Couldn't undo: it was removed elsewhere"),
            refusedConfig().getOrNull(SemanticsProperties.ContentDescription)
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun refusalInARealSheet_takesFocusOnlyAfterTheSheetHasSettled() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())),
                onDismissRequest = {},
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            )
        }
        composeTestRule.mainClock.advanceTimeBy(100)
        refusedIsFocused().assertIsNotFocused()

        composeTestRule.mainClock.advanceTimeBy(3_000)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        refusedIsFocused().assertIsFocused()
    }

    @Test
    fun neverBothAFocusMoveAndAnAnnouncement() {
        val source = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("OutcomeSheet.kt"))
        assertFalse(source.contains("announceForAccessibility"))
        assertFalse(source.contains("TYPE_ANNOUNCEMENT"))
        assertFalse(source.contains("sendAccessibilityEvent"))
        assertEquals(
            "the one route is a single focus request",
            1,
            SourceContractTestSupport.countOccurrences(source, ".requestFocus()")
        )
        assertEquals(
            "the one live region stays",
            1,
            SourceContractTestSupport.countOccurrences(source, "liveRegion = LiveRegionMode.")
        )
    }

    /** What the Compose delegate really sends to an enabled accessibility service (event type ids). */
    private fun sentEventTypesWithAServiceEnabled(): List<Int> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val shadow = Shadows.shadowOf(manager)
        shadow.setEnabled(true)
        shadow.setTouchExplorationEnabled(true)
        shadow.setInstalledAccessibilityServiceList(listOf(AccessibilityServiceInfo()))
        shadow.setEnabledAccessibilityServiceList(listOf(AccessibilityServiceInfo()))
        gains = 0
        hostWithRival()
        return shadow.sentAccessibilityEvents.map { it.eventType }
    }

    @Test
    fun refusalFocus_sendsAViewFocusedEvent_thatATalkBackServiceFollows() {
        val types = sentEventTypesWithAServiceEnabled()

        assertTrue("TYPE_VIEW_FOCUSED expected in $types", types.contains(AccessibilityEvent.TYPE_VIEW_FOCUSED))
        // The route is a focus move, never a spoken announcement.
        assertFalse(types.contains(ANNOUNCEMENT_EVENT_TYPE))
    }
}
