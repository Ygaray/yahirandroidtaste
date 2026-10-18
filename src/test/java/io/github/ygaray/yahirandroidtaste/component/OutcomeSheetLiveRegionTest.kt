package io.github.ygaray.yahirandroidtaste.component

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import android.view.accessibility.AccessibilityRecord
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel
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
import org.robolectric.annotation.GraphicsMode

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

    // -- Gate-1 C-1 (Phase 28-12): ONE mechanism (the live region), spoken once, after the sheet settles --
    //
    // History: 28-09 merged the live region (event dropped, it fires before the sheet window exists),
    // 28-11 added a focus move (spoken, but twice: live region plus focus, and each utterance held the
    // message twice because the explicit description was exposed beside the drawn Text). 28-12 removes
    // the focus move, hides the drawn Text from accessibility and gives the live-region node its name
    // only once the sheet is at rest. These tests read the PLATFORM node (what TalkBack reads) through
    // the Compose accessibility delegate; Robolectric has no screen reader, so "spoken" is the
    // TalkBack-style aggregation below and the utterance count itself is a Gate-1 device measurement.

    private val refusedTag = "outcome_sheet_undo_refused"
    private val englishMessage = "Couldn't undo: it was edited elsewhere"

    private fun refusedConfig() =
        composeTestRule.onNodeWithTag(refusedTag, useUnmergedTree = false).fetchSemanticsNode().config

    private fun refusedNode() = composeTestRule.onNodeWithTag(refusedTag, useUnmergedTree = false).fetchSemanticsNode()

    @Test
    fun refusedNode_hasNoClickAction_andIsTheOnlyAssertiveRegion() {
        host(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        liveRegionNodes().assertCountEquals(1)
        val config = refusedConfig()
        assertEquals(LiveRegionMode.Assertive, config.getOrNull(SemanticsProperties.LiveRegion))
        assertNull("the message is not a button", config.getOrNull(SemanticsActions.OnClick))
        assertNull(config.getOrNull(SemanticsProperties.Role))
    }

    @Test
    fun refusedNode_isNotFocusRequested_andHasNoFocusMachinery() {
        val source = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("OutcomeSheet.kt"))
        listOf(
            "focusRequester(",
            "FocusRequester",
            "focusable(",
            "requestFocus",
            "awaitSheetSettled",
            "announceForAccessibility",
            "TYPE_ANNOUNCEMENT",
            "sendAccessibilityEvent"
        ).forEach { assertFalse("OutcomeSheet.kt must not contain $it", source.contains(it)) }
        assertEquals(
            "the one live region stays",
            1,
            SourceContractTestSupport.countOccurrences(source, "liveRegion = LiveRegionMode.")
        )
        host(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))
        settle()
        assertFalse("the refusal is no focus target", refusedConfig().contains(SemanticsProperties.Focused))
    }

    private var reason by mutableStateOf("it was edited elsewhere")
    private var tick by mutableIntStateOf(0)

    /** A refusal host whose outcome is rebuilt (equal or different) when [tick] or [reason] change. */
    private fun hostRebuilding(changedItem: String? = null) {
        composeTestRule.setContent {
            check(tick >= 0) // the read recomposes the host, which rebuilds an equal outcome
            Box(Modifier.height(800.dp)) {
                OutcomeSheetContent(
                    OutcomeFixtures.success(
                        undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused(reason, changedItem))
                    )
                )
            }
        }
    }

    private fun settle() {
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()
    }

    private fun enableAccessibility(): org.robolectric.shadows.ShadowAccessibilityManager {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val shadow = Shadows.shadowOf(manager)
        shadow.setEnabled(true)
        shadow.setTouchExplorationEnabled(true)
        shadow.setInstalledAccessibilityServiceList(listOf(AccessibilityServiceInfo()))
        shadow.setEnabledAccessibilityServiceList(listOf(AccessibilityServiceInfo()))
        return shadow
    }

    private fun provider(node: SemanticsNode): AccessibilityNodeProvider {
        val view = node.root as View
        // Make the delegate rebuild its node map from the tree as it is now (it normally does so on the
        // next semantics-change event); a private field of Compose, read by reflection in a test only.
        val delegateField = view.javaClass.getDeclaredField("composeAccessibilityDelegate").apply { isAccessible = true }
        val delegate = delegateField.get(view)
        delegate.javaClass.getDeclaredField("currentSemanticsNodesInvalidated").apply { isAccessible = true }
            .set(delegate, true)
        return checkNotNull(view.accessibilityNodeProvider) { "no accessibility node provider" }
    }

    private fun childIds(info: AccessibilityNodeInfo): List<Int> {
        val childId = AccessibilityNodeInfo::class.java.getDeclaredMethod("getChildId", Int::class.javaPrimitiveType)
        val virtualId = AccessibilityNodeInfo::class.java
            .getDeclaredMethod("getVirtualDescendantId", Long::class.javaPrimitiveType)
        childId.isAccessible = true
        virtualId.isAccessible = true
        return (0 until info.childCount).map { virtualId.invoke(null, childId.invoke(info, it) as Long) as Int }
    }

    /**
     * What a screen reader assembles for the platform node [id]: its own text and description, then
     * the same for every descendant that is visible to the user and is not a stop of its own.
     */
    private fun spokenPieces(provider: AccessibilityNodeProvider, id: Int, isRoot: Boolean = true): List<String> {
        val info = checkNotNull(provider.createAccessibilityNodeInfo(id)) { "no platform node for $id" }
        if (!isRoot && (!info.isVisibleToUser || info.isScreenReaderFocusable || info.isFocusable)) return emptyList()
        val own = listOfNotNull(info.text?.toString(), info.contentDescription?.toString()).filter { it.isNotEmpty() }
        return own + childIds(info).flatMap { spokenPieces(provider, it, isRoot = false) }
    }

    private fun refusedSpeech(): String {
        val node = refusedNode()
        return spokenPieces(provider(node), node.id).joinToString(" | ")
    }

    private fun occurrences(haystack: String, needle: String): Int =
        Regex(Regex.escape(needle)).findAll(haystack).count()

    /** Content-change events of the platform source node [virtualId], since the last call. */
    private class EventLog(private val shadow: org.robolectric.shadows.ShadowAccessibilityManager) {
        private var seen = 0

        fun next(): List<AccessibilityEvent> {
            val all = shadow.sentAccessibilityEvents
            val fresh = all.subList(seen, all.size).filter { it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED }
            seen = all.size
            return fresh
        }

        companion object {
            fun virtualId(event: AccessibilityEvent): Int =
                (AccessibilityRecord::class.java.getMethod("getSourceNodeId").invoke(event) as Long shr 32).toInt()
        }
    }

    private fun EventLog.nextFor(node: SemanticsNode): List<AccessibilityEvent> =
        next().filter { EventLog.virtualId(it) == node.id }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun refusedNode_messageIsExposedOnceAcrossTextAndDescription() {
        enableAccessibility()
        hostRebuilding()
        settle()
        assertEquals(1, occurrences(refusedSpeech(), englishMessage))
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun refusedNode_withChangedItem_messageIsExposedOnce() {
        enableAccessibility()
        hostRebuilding(changedItem = "Oats")
        settle()

        val message = "$englishMessage, Oats changed since"
        val speech = refusedSpeech()
        assertEquals("with a changed item the message is spoken once: $speech", 1, occurrences(speech, message))
        assertEquals("the lead of the message is not spoken twice: $speech", 1, occurrences(speech, "Couldn't undo"))
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun refusedNode_messageIsExposedOnce_german() {
        enableAccessibility()
        val german = UndoRefusedUiModel(
            reason = "wurde woanders bearbeitet",
            changedItem = "Haferflocken",
            refusedPrefix = "Rückgängig nicht möglich:",
            changedSinceSuffix = "hat sich seitdem geändert"
        )
        host(OutcomeFixtures.failure(undo = OutcomeFixtures.undo(refused = german)))
        settle()

        val message = "Rückgängig nicht möglich: wurde woanders bearbeitet, Haferflocken hat sich seitdem geändert"
        val speech = refusedSpeech()
        assertEquals("the German message is spoken once: $speech", 1, occurrences(speech, message))
        assertEquals(1, occurrences(speech, "Rückgängig nicht möglich"))
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun refusedNode_exposesNoSpeechBeforeTheSheetHasSettled() {
        enableAccessibility()
        composeTestRule.mainClock.autoAdvance = false
        hostRebuilding()
        composeTestRule.mainClock.advanceTimeBy(100)

        // The surface is composed (it is on screen and takes its space) but silent.
        composeTestRule.onNodeWithText(englishMessage).assertExists()
        assertEquals("nothing is spoken before the sheet has settled", "", refusedSpeech())
        assertNull(refusedConfig().getOrNull(SemanticsProperties.ContentDescription))
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun refusedNode_exposesTheFullMessageAfterTheSettle() {
        enableAccessibility()
        composeTestRule.mainClock.autoAdvance = false
        hostRebuilding(changedItem = "Oats")
        composeTestRule.mainClock.advanceTimeBy(100)
        assertEquals("", refusedSpeech())

        composeTestRule.mainClock.advanceTimeBy(3_000)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        val message = "$englishMessage, Oats changed since"
        assertEquals(listOf(message), refusedConfig().getOrNull(SemanticsProperties.ContentDescription))
        assertEquals(1, occurrences(refusedSpeech(), message))
        // The drawn Text is still there, hidden from accessibility only.
        composeTestRule.onNodeWithText(message).assertExists()
        assertTrue(
            composeTestRule.onNode(hasText(message) and !hasTestTag(refusedTag), useUnmergedTree = true)
                .fetchSemanticsNode().config.contains(SemanticsProperties.HideFromAccessibility)
        )
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun exactlyOneContentChangeEventPerShowing() {
        val shadow = enableAccessibility()
        val log = EventLog(shadow)
        composeTestRule.mainClock.autoAdvance = false
        hostRebuilding()
        composeTestRule.mainClock.advanceTimeBy(100)
        log.next()

        composeTestRule.mainClock.advanceTimeBy(3_000)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        val node = refusedNode()
        val events = log.nextFor(node)
        assertEquals(
            "one showing, one content-change event on the live-region node: ${events.map { it.contentChangeTypes }}",
            1,
            events.size
        )
        assertEquals(1, occurrences(refusedSpeech(), englishMessage))
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun equalStateRecomposition_causesNoSecondChange() {
        val shadow = enableAccessibility()
        val log = EventLog(shadow)
        tick = 0
        reason = "it was edited elsewhere"
        hostRebuilding()
        settle()
        log.next()

        tick++
        settle()
        tick++
        settle()

        val node = refusedNode()
        assertEquals("an equal outcome rebuilt twice causes no event", 0, log.nextFor(node).size)
        assertEquals(1, occurrences(refusedSpeech(), englishMessage))
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun aNewRefusal_causesOneNewChange() {
        val shadow = enableAccessibility()
        val log = EventLog(shadow)
        tick = 0
        reason = "it was edited elsewhere"
        hostRebuilding()
        settle()
        log.next()

        reason = "it was removed elsewhere"
        settle()

        val node = refusedNode()
        val events = log.nextFor(node)
        assertEquals("a new message is one new change: ${events.map { it.contentChangeTypes }}", 1, events.size)
        val speech = refusedSpeech()
        assertEquals(1, occurrences(speech, "Couldn't undo: it was removed elsewhere"))
        assertEquals("the old message is gone: $speech", 0, occurrences(speech, "edited elsewhere"))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun refusalInARealSheet_isSilentUntilTheSheetHasSettled_thenSpokenOnce() {
        enableAccessibility()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())),
                onDismissRequest = {},
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            )
        }
        composeTestRule.mainClock.advanceTimeBy(100)
        // While the sheet slides in, the node is below the window edge: Compose then leaves it out of the
        // platform tree altogether, so there is nothing to speak; either way, no message is exposed.
        val early = refusedNode()
        val earlyInfo = provider(early).createAccessibilityNodeInfo(early.id)
        assertEquals("silent while the sheet is still moving", "", earlyInfo?.let { refusedSpeech() }.orEmpty())
        assertNull(refusedConfig().getOrNull(SemanticsProperties.ContentDescription))

        composeTestRule.mainClock.advanceTimeBy(3_000)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        assertEquals(1, occurrences(refusedSpeech(), englishMessage))
    }
}
