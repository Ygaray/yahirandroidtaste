package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric A11Y-01 tree assertions for [FeedbackStackHost] (phase 26, plan 05): each card is one
 * merged `Role.Button` node with the open label and a Polite live region, Undo is a separate
 * focusable child that also appears as a custom action, both Dismiss forms resolve the card
 * `Dismissed`, and the cards read newest first. Every event is [SnackbarDuration.Indefinite]
 * because the card timer runs on the `show()` caller's dispatcher, not on the Compose clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackStackHostSemanticsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Eintrag öffnen")

    private lateinit var scope: CoroutineScope

    private fun setHost(
        state: FeedbackStackState,
        opened: MutableList<FeedbackTarget> = mutableListOf(),
        undoLabel: String = "Rückgängig",
        dismissLabel: String = "Schließen"
    ) {
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            FeedbackStackHost(
                state = state,
                onOpenTarget = { opened += it },
                undoLabel = undoLabel,
                dismissLabel = dismissLabel
            )
        }
    }

    private fun withUndo(
        message: String,
        target: FeedbackTarget? = this.target,
        undoAction: suspend () -> Unit = {}
    ) = FeedbackEvent.WithUndo(
        message = message,
        undoAction = undoAction,
        duration = SnackbarDuration.Indefinite,
        target = target
    )

    private fun notify(message: String, target: FeedbackTarget? = null) = FeedbackEvent.Notify(
        message = message,
        duration = SnackbarDuration.Indefinite,
        target = target
    )

    private fun show(
        state: FeedbackStackState,
        event: FeedbackEvent,
        key: String,
        results: MutableMap<String, FeedbackResult>
    ) {
        scope.launch { results[key] = state.show(event) }
        composeTestRule.waitForIdle()
    }

    private fun mergedCard(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG)

    /** The merged card node whose text contains [message]. */
    private fun cardByText(message: String): SemanticsNodeInteraction = composeTestRule.onNode(
        hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG) and hasText(message, substring = true)
    )

    /** The same card in the unmerged tree, the one a touch lands on. */
    private fun unmergedCard(message: String): SemanticsNodeInteraction = composeTestRule.onNode(
        hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG) and hasAnyDescendant(hasText(message)),
        useUnmergedTree = true
    )

    /** A tap inside the 14dp peek strip, which fans a collapsed deck. */
    private fun tapStrip(message: String) {
        unmergedCard(message).performTouchInput { click(Offset(width * 0.5f, 4.dp.toPx())) }
        composeTestRule.waitForIdle()
    }

    private fun advanceClock(millis: Long) {
        composeTestRule.mainClock.advanceTimeBy(millis, ignoreFrameDuration = true)
        composeTestRule.mainClock.advanceTimeByFrame()
    }

    private fun collapseByIdle(state: FeedbackStackState) {
        composeTestRule.mainClock.autoAdvance = false
        advanceClock(state.idleCollapseMillis + 1_000)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
    }

    private fun traversalIndexOf(message: String): Float =
        cardByText(message).fetchSemanticsNode().config[SemanticsProperties.TraversalIndex]

    private fun cardCount(): Int =
        composeTestRule.onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).fetchSemanticsNodes().size

    private fun mergedTexts(node: SemanticsNode): List<String> =
        node.config.getOrNull(SemanticsProperties.Text)?.map { it.text } ?: emptyList()

    private fun isDescendantOf(node: SemanticsNode, ancestorTag: String): Boolean {
        var current: SemanticsNode? = node.parent
        while (current != null) {
            if (current.config.getOrNull(SemanticsProperties.TestTag) == ancestorTag) return true
            current = current.parent
        }
        return false
    }

    /** Everything a screen reader announces or offers for a card, except its reading position. */
    private data class Announcement(
        val texts: List<String>,
        val role: Role?,
        val liveRegion: LiveRegionMode?,
        val openLabel: String?,
        val customActionLabels: List<String>,
        val dismissLabel: String?
    )

    private fun announcementOf(message: String): Announcement {
        val config = cardByText(message).fetchSemanticsNode().config
        return Announcement(
            texts = config.getOrNull(SemanticsProperties.Text)?.map { it.text }.orEmpty(),
            role = config.getOrNull(SemanticsProperties.Role),
            liveRegion = config.getOrNull(SemanticsProperties.LiveRegion),
            openLabel = config.getOrNull(SemanticsActions.OnClick)?.label,
            customActionLabels = config.getOrNull(SemanticsActions.CustomActions)
                ?.map { it.label }.orEmpty(),
            dismissLabel = config.getOrNull(SemanticsActions.Dismiss)?.label
        )
    }

    private fun showThree(state: FeedbackStackState) {
        val results = mutableMapOf<String, FeedbackResult>()
        listOf("Ältester", "Mittlerer", "Neuester").forEach { message ->
            show(state, notify(message, target), message, results)
        }
    }

    private fun assertNewestFirstIndices() {
        val indices = listOf("Neuester", "Mittlerer", "Ältester").map { traversalIndexOf(it) }
        assertEquals(listOf(0f, 1f, 2f), indices)
        assertEquals("traversal indices must be unique", 3, indices.toSet().size)
    }

    @Test
    fun targetedCard_isOneMergedButtonNode_withOpenLabelPoliteRegionAndDismiss() {
        val state = FeedbackStackState()
        setHost(state)
        scope.launch { state.show(withUndo("Eintrag gelöscht")) }
        composeTestRule.waitForIdle()

        val config = mergedCard().fetchSemanticsNode().config
        assertEquals(Role.Button, config[SemanticsProperties.Role])
        assertEquals("Eintrag öffnen", config[SemanticsActions.OnClick].label)
        assertEquals(LiveRegionMode.Polite, config[SemanticsProperties.LiveRegion])
        assertEquals("Schließen", config[SemanticsActions.Dismiss].label)
        assertEquals(
            listOf("Rückgängig", "Schließen"),
            config[SemanticsActions.CustomActions].map { it.label }
        )
        assertEquals(0f, config[SemanticsProperties.TraversalIndex], 0f)

        // The unmerged tree is the one accessibility services merge themselves: Undo stays a
        // separate focusable child of the card, and the card's merged text does not carry its label.
        val undo = composeTestRule
            .onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
        assertNotNull(undo.config.getOrNull(SemanticsActions.OnClick))
        assertTrue(isDescendantOf(undo, FEEDBACK_STACK_ITEM_TEST_TAG))
        assertFalse(mergedTexts(mergedCard().fetchSemanticsNode()).contains("Rückgängig"))
        state.dismissAll()
    }

    @Test
    fun undoIsASeparateFocusableChild_andAlsoACustomAction() {
        val state = FeedbackStackState()
        setHost(state)
        scope.launch { state.show(withUndo("Eintrag gelöscht")) }
        composeTestRule.waitForIdle()

        val undo = composeTestRule
            .onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
        assertNotNull(undo.config.getOrNull(SemanticsActions.OnClick))
        assertTrue(isDescendantOf(undo, FEEDBACK_STACK_ITEM_TEST_TAG))

        val card = mergedCard().fetchSemanticsNode()
        assertFalse(mergedTexts(card).contains("Rückgängig"))
        assertTrue(card.config[SemanticsActions.CustomActions].any { it.label == "Rückgängig" })
        state.dismissAll()
    }

    @Test
    fun customActionOrder_isUndoThenDismiss_andNotifyHasOnlyDismiss() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        show(state, withUndo("Mit Undo"), "undo", results)
        assertEquals(
            listOf("Rückgängig", "Schließen"),
            mergedCard().fetchSemanticsNode().config[SemanticsActions.CustomActions].map { it.label }
        )
        state.dismissAll()
        composeTestRule.waitForIdle()

        show(state, notify("Nur Hinweis", target), "notify", results)
        assertEquals(
            listOf("Schließen"),
            mergedCard().fetchSemanticsNode().config[SemanticsActions.CustomActions].map { it.label }
        )
        state.dismissAll()
    }

    @Test
    fun dismissSemantics_resolvesDismissed_notUndone() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        var undoRuns = 0
        setHost(state, opened)
        show(state, withUndo("Eintrag gelöscht") { undoRuns++ }, "card", results)

        mergedCard().performSemanticsAction(SemanticsActions.Dismiss)
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Dismissed, results["card"])
        assertEquals(0, undoRuns)
        assertTrue(opened.isEmpty())
        mergedCard().assertDoesNotExist()
    }

    @Test
    fun dismissCustomAction_resolvesDismissed_notUndone() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        var undoRuns = 0
        setHost(state, opened)
        show(state, withUndo("Eintrag gelöscht") { undoRuns++ }, "card", results)

        val dismiss = mergedCard().fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .single { it.label == "Schließen" }
        composeTestRule.runOnIdle { dismiss.action() }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Dismissed, results["card"])
        assertEquals(0, undoRuns)
        assertTrue(opened.isEmpty())
        mergedCard().assertDoesNotExist()
    }

    @Test
    fun openAction_viaSemantics_opensOnce() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        show(state, notify("Eintrag gespeichert", target), "card", results)

        val open = requireNotNull(
            mergedCard().fetchSemanticsNode().config[SemanticsActions.OnClick].action
        )
        composeTestRule.runOnIdle {
            assertTrue(open())
            assertTrue(open())
        }
        composeTestRule.waitForIdle()

        assertEquals(listOf(target), opened)
        assertEquals(FeedbackResult.Opened, results["card"])
    }

    @Test
    fun openAction_isAbsent_onSpentAndUntargetedCards() {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        scope.launch { state.emitTrackedWithUndo(store, withUndo("Tracked gelöscht")) }
        composeTestRule.waitForIdle()
        show(state, notify("Ohne Ziel"), "plain", results)
        scope.launch { store.attemptUndo(store.entries.value.single().id) }
        composeTestRule.waitForIdle()

        assertEquals(2, cardCount())
        listOf("Tracked gelöscht", "Ohne Ziel").forEach { message ->
            val config = cardByText(message).fetchSemanticsNode().config
            assertNull("$message must expose no open action", config.getOrNull(SemanticsActions.OnClick))
            assertNull("$message must expose no button role", config.getOrNull(SemanticsProperties.Role))
        }
        assertTrue(opened.isEmpty())
        state.dismissAll()
    }

    @Test
    fun cardWithoutTarget_hasNoRoleNoClick_butKeepsPoliteRegionAndDismiss() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        show(state, notify("Ohne Ziel"), "plain", results)

        val config = mergedCard().fetchSemanticsNode().config
        assertNull(config.getOrNull(SemanticsProperties.Role))
        assertNull(config.getOrNull(SemanticsActions.OnClick))
        assertEquals(LiveRegionMode.Polite, config[SemanticsProperties.LiveRegion])
        assertEquals("Schließen", config[SemanticsActions.Dismiss].label)
        assertEquals(1, cardCount())
        state.dismissAll()
    }

    @Test
    fun spentCard_dropsRoleOnClickAndUndoAction_keepsDismissAndRegion() {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        setHost(state)
        scope.launch { state.emitTrackedWithUndo(store, withUndo("Tracked gelöscht")) }
        composeTestRule.waitForIdle()
        assertEquals(
            listOf("Rückgängig", "Schließen"),
            mergedCard().fetchSemanticsNode().config[SemanticsActions.CustomActions].map { it.label }
        )

        scope.launch { store.attemptUndo(store.entries.value.single().id) }
        composeTestRule.waitForIdle()

        val config = mergedCard().fetchSemanticsNode().config
        assertNull(config.getOrNull(SemanticsProperties.Role))
        assertNull(config.getOrNull(SemanticsActions.OnClick))
        assertEquals(listOf("Schließen"), config[SemanticsActions.CustomActions].map { it.label })
        assertEquals("Schließen", config[SemanticsActions.Dismiss].label)
        assertEquals(LiveRegionMode.Polite, config[SemanticsProperties.LiveRegion])
        composeTestRule
            .onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
        state.dismissAll()
    }

    // ---- Plan 26-06: the touch blocker on a collapsed back card is touch-only (A11Y-01 intact) ----

    /** The Undo button of the card holding [message], from the unmerged tree. */
    private fun undoOf(message: String): SemanticsNodeInteraction = composeTestRule.onNode(
        hasTestTag(FEEDBACK_STACK_UNDO_TEST_TAG) and hasAnyAncestor(
            hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG) and hasAnyDescendant(hasText(message))
        ),
        useUnmergedTree = true
    )

    /** A collapsed two-card deck: "Hinten" with Undo behind a plain "Vorne". */
    private fun collapsedDeck(
        state: FeedbackStackState,
        results: MutableMap<String, FeedbackResult>,
        undoAction: suspend () -> Unit = {}
    ) {
        setHost(state)
        show(state, withUndo("Hinten", undoAction = undoAction), "Hinten", results)
        show(state, notify("Vorne"), "Vorne", results)
        assertFalse(state.pauseState.value.fanned)
    }

    @Test
    fun collapsedBackCard_undoChildStaysEnabledAndClickable() {
        val state = FeedbackStackState()
        collapsedDeck(state, mutableMapOf())

        val undo = undoOf("Hinten").fetchSemanticsNode()
        assertNotNull(undo.config.getOrNull(SemanticsActions.OnClick))
        assertNull("the Undo must not be reported disabled", undo.config.getOrNull(SemanticsProperties.Disabled))
        assertTrue(isDescendantOf(undo, FEEDBACK_STACK_ITEM_TEST_TAG))
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun collapsedBackCard_undoSemanticClick_resolvesUndone_withoutFanning() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        collapsedDeck(state, results)

        undoOf("Hinten").performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Undone, results["Hinten"])
        assertNull(results["Vorne"])
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun collapsedBackCard_undoCustomAction_resolvesUndone() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        collapsedDeck(state, results)

        val undo = cardByText("Hinten").fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .single { it.label == "Rückgängig" }
        composeTestRule.runOnIdle { undo.action() }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Undone, results["Hinten"])
        assertNull(results["Vorne"])
        state.dismissAll()
    }

    @Test
    fun traversalIndex_isNewestFirst_whenCollapsed() {
        val state = FeedbackStackState()
        setHost(state)
        showThree(state)

        assertFalse(state.pauseState.value.fanned)
        assertNewestFirstIndices()
        state.dismissAll()
    }

    @Test
    fun traversalIndex_isNewestFirst_whenFanned() {
        val state = FeedbackStackState()
        setHost(state)
        showThree(state)

        tapStrip("Ältester")

        assertTrue(state.pauseState.value.fanned)
        assertNewestFirstIndices()
        state.dismissAll()
    }

    @Test
    fun deckContainer_isATraversalGroup() {
        val state = FeedbackStackState()
        setHost(state)
        showThree(state)

        val deck = composeTestRule
            .onNodeWithTag(FEEDBACK_STACK_DECK_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
        assertEquals(true, deck.config.getOrNull(SemanticsProperties.IsTraversalGroup))
        state.dismissAll()
    }

    @Test
    fun everyCardIsPolite_collapsedAndFanned() {
        val state = FeedbackStackState()
        setHost(state)
        showThree(state)

        fun assertAllPolite() {
            val cards = composeTestRule.onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG)
                .fetchSemanticsNodes()
            assertEquals(3, cards.size)
            cards.forEach {
                assertEquals(LiveRegionMode.Polite, it.config.getOrNull(SemanticsProperties.LiveRegion))
            }
        }
        assertAllPolite()
        tapStrip("Ältester")
        assertTrue(state.pauseState.value.fanned)
        assertAllPolite()
        state.dismissAll()
    }

    @Test
    fun semanticsAreStableAcrossFanCollapseAndNewerArrival() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        show(state, withUndo("Ältester"), "Ältester", results)
        show(state, notify("Mittlerer", target), "Mittlerer", results)
        val arrived = announcementOf("Ältester")
        assertTrue(arrived.texts.contains("Ältester"))
        assertEquals(LiveRegionMode.Polite, arrived.liveRegion)

        tapStrip("Ältester")
        assertTrue(state.pauseState.value.fanned)
        assertEquals("fanning", arrived, announcementOf("Ältester"))

        collapseByIdle(state)
        assertFalse(state.pauseState.value.fanned)
        assertEquals("collapsing", arrived, announcementOf("Ältester"))

        show(state, notify("Neuester", target), "Neuester", results)
        assertEquals("a newer arrival", arrived, announcementOf("Ältester"))

        // Promotion to newest: the newer cards leave, the oldest keeps announcing the same content.
        listOf("Neuester", "Mittlerer").forEach { message ->
            cardByText(message).performSemanticsAction(SemanticsActions.Dismiss)
            composeTestRule.waitForIdle()
        }
        assertEquals("promotion to newest", arrived, announcementOf("Ältester"))
        assertEquals(0f, traversalIndexOf("Ältester"), 0f)
        state.dismissAll()
    }

    @Test
    fun emptyDeck_exposesNoCardNode() {
        val state = FeedbackStackState()
        setHost(state)

        assertEquals(0, cardCount())
        composeTestRule.onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun keyNeverAppearsInTheSemanticsTree() {
        val state = FeedbackStackState()
        setHost(state)
        scope.launch { state.show(withUndo("Eintrag gelöscht")) }
        composeTestRule.waitForIdle()

        val dump = SemanticsDump.allRawRoots(composeTestRule)
        assertTrue("the dump must contain the card, or the scan proves nothing", dump.contains("Eintrag gelöscht"))
        assertTrue(dump.contains("Eintrag öffnen"))
        assertFalse("the opaque key leaked into the semantics tree", dump.contains("item-42"))
        state.dismissAll()
    }
}
