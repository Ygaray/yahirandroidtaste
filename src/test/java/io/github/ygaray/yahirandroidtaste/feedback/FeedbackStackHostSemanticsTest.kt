package io.github.ygaray.yahirandroidtaste.feedback

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
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
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

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

        val undo = composeTestRule.undoOf("Hinten").fetchSemanticsNode()
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

        composeTestRule.undoOf("Hinten").performSemanticsAction(SemanticsActions.OnClick)
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

    // ---- Plan 28-10: the silent middle peek strip is not an accessibility stop (Gate-1 C-3/C-4) ----

    /** True when the card whose merged text contains [message] is hidden from accessibility. */
    private fun isInvisibleToUser(message: String): Boolean =
        cardByText(message).fetchSemanticsNode().config
            .getOrNull(SemanticsProperties.InvisibleToUser) != null

    /** The cards a screen reader can stop on: every card not flagged invisible to the user. */
    private fun accessibleCards(): List<SemanticsNode> =
        composeTestRule.onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).fetchSemanticsNodes()
            .filter { it.config.getOrNull(SemanticsProperties.InvisibleToUser) == null }

    private fun mergedLabel(node: SemanticsNode): String =
        (
            mergedTexts(node) +
                (node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty())
            ).joinToString(" ").trim()

    private fun assertNoAccessibleCardHasAnEmptyLabel() {
        val cards = accessibleCards()
        assertTrue("a collapsed deck of three must keep two accessible cards", cards.size == 2)
        cards.forEach {
            assertTrue("an accessibility stop must not be silent", mergedLabel(it).isNotEmpty())
        }
    }

    @Test
    fun collapsedDeckOfThree_middleCardIsInvisibleToUser_andFrontAndOldestAreNot() {
        val state = FeedbackStackState()
        setHost(state)
        showThree(state)

        assertFalse(state.pauseState.value.fanned)
        assertTrue("the middle strip must be hidden", isInvisibleToUser("Mittlerer"))
        assertFalse("the front card must stay a stop", isInvisibleToUser("Neuester"))
        assertFalse("the oldest strip must stay a stop", isInvisibleToUser("Ältester"))
        state.dismissAll()
    }

    @Test
    fun collapsedDeckOfThree_hasNoFocusableNodeWithAnEmptyLabel() {
        val state = FeedbackStackState()
        setHost(state)
        showThree(state)

        assertNoAccessibleCardHasAnEmptyLabel()
        state.dismissAll()
    }

    @Test
    fun collapsedDeckOfThreeInGerman_hasNoFocusableNodeWithAnEmptyLabel() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, undoLabel = "Rückgängig machen", dismissLabel = "Meldung schließen")
        listOf(
            "Serie zwei: Eintrag entfernt",
            "Serie eins: Eintrag gespeichert",
            "Serie drei: Eintrag verschoben"
        ).forEach { message -> show(state, withUndo(message), message, results) }

        assertFalse(state.pauseState.value.fanned)
        assertTrue(isInvisibleToUser("Serie eins: Eintrag gespeichert"))
        assertFalse(isInvisibleToUser("Serie drei: Eintrag verschoben"))
        assertFalse(isInvisibleToUser("Serie zwei: Eintrag entfernt"))
        assertNoAccessibleCardHasAnEmptyLabel()
        state.dismissAll()
    }

    @Test
    fun collapsedDeckOfOneOrTwo_hidesNoCard() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        show(state, notify("Ältester", target), "Ältester", results)
        assertFalse(isInvisibleToUser("Ältester"))

        show(state, notify("Neuester", target), "Neuester", results)
        assertEquals(2, cardCount())
        assertFalse(state.pauseState.value.fanned)
        assertFalse(isInvisibleToUser("Neuester"))
        assertFalse(isInvisibleToUser("Ältester"))
        state.dismissAll()
    }

    @Test
    fun fannedDeckOfThree_hidesNoCard() {
        val state = FeedbackStackState()
        setHost(state)
        showThree(state)

        tapStrip("Ältester")

        assertTrue(state.pauseState.value.fanned)
        listOf("Neuester", "Mittlerer", "Ältester").forEach {
            assertFalse("$it must be accessible once fanned", isInvisibleToUser(it))
        }
        state.dismissAll()
    }

    @Test
    fun tapOnTheHiddenMiddleStrip_stillFansTheDeck() {
        val state = FeedbackStackState()
        setHost(state)
        showThree(state)
        assertTrue(isInvisibleToUser("Mittlerer"))
        assertFalse(state.pauseState.value.fanned)

        tapStrip("Mittlerer")

        assertTrue(state.pauseState.value.fanned)
        assertFalse(isInvisibleToUser("Mittlerer"))
        state.dismissAll()
    }

    // ---- Plan 28-11: every focusable card node is named from the data (Gate-1 C-5) ----

    /**
     * What a screen reader speaks for [node]: its ContentDescription when it has one (it
     * is the speech source of the card; see the platform-node tests below), otherwise its Text.
     */
    private fun accessibleName(node: SemanticsNode): String {
        val description = node.config.getOrNull(SemanticsProperties.ContentDescription)
        if (!description.isNullOrEmpty()) return description.joinToString(", ").trim()
        return mergedTexts(node).joinToString(", ").trim()
    }

    /** Every node of the merged tree a screen reader can land on: it offers a click or an action. */
    private fun focusableNodes(): List<SemanticsNode> = composeTestRule.onAllNodes(
        SemanticsMatcher("focusable card node") {
            it.config.contains(SemanticsActions.OnClick) ||
                it.config.contains(SemanticsActions.CustomActions) ||
                it.config.contains(SemanticsActions.Dismiss)
        }
    ).fetchSemanticsNodes()

    private fun assertEveryFocusableNodeIsNamed(expectedCards: Int) {
        val stops = focusableNodes()
            .filter { it.config.getOrNull(SemanticsProperties.InvisibleToUser) == null }
        val roots = stops.filter { it.config.getOrNull(SemanticsProperties.TestTag) == FEEDBACK_STACK_ITEM_TEST_TAG }
        assertTrue("expected at least $expectedCards card stops, got ${roots.size}", roots.size >= expectedCards)
        stops.forEach {
            assertTrue(
                "a focusable node must have a name, but this one is empty: ${it.config}",
                accessibleName(it).isNotEmpty()
            )
        }
    }

    private fun showThreeWithUndo(state: FeedbackStackState, messages: List<String>) {
        val results = mutableMapOf<String, FeedbackResult>()
        messages.forEach { message -> show(state, withUndo(message), message, results) }
    }

    @Test
    fun collapsedDeckOfThree_everyVisibleFocusableNodeHasAName() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        showThreeWithUndo(state, listOf("Burst two", "Burst three", "Burst four"))

        assertFalse(state.pauseState.value.fanned)
        assertTrue("the middle strip must stay hidden", isInvisibleToUser("Burst three"))
        assertEveryFocusableNodeIsNamed(expectedCards = 2)
        state.dismissAll()
    }

    @Test
    fun collapsedDeckOfThreeInGerman_everyVisibleFocusableNodeHasAName() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Rückgängig machen", dismissLabel = "Meldung schließen")
        showThreeWithUndo(
            state,
            listOf("Serie zwei: Eintrag entfernt", "Serie eins: Eintrag gespeichert", "Serie drei: Eintrag verschoben")
        )

        assertFalse(state.pauseState.value.fanned)
        assertTrue(isInvisibleToUser("Serie eins: Eintrag gespeichert"))
        assertEveryFocusableNodeIsNamed(expectedCards = 2)
        state.dismissAll()
    }

    @Test
    fun fannedDeckOfThree_everyFocusableNodeHasAName() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        showThreeWithUndo(state, listOf("Burst two", "Burst three", "Burst four"))

        tapStrip("Burst two")

        assertTrue(state.pauseState.value.fanned)
        assertEveryFocusableNodeIsNamed(expectedCards = 3)
        state.dismissAll()
    }

    @Test
    fun decksOfOneAndTwo_everyFocusableNodeHasAName() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        show(state, withUndo("Only card"), "Only card", results)
        assertEveryFocusableNodeIsNamed(expectedCards = 1)

        show(state, withUndo("Second card"), "Second card", results)
        assertEquals(2, cardCount())
        assertEveryFocusableNodeIsNamed(expectedCards = 2)
        state.dismissAll()
    }

    @Test
    fun cardRoot_nameIsExactlyTheMessage_notDoubled() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        val long = "A rather long message that wraps over several lines in a narrow card, ".repeat(3).trim()
        scope.launch { state.show(withUndo(long)) }
        composeTestRule.waitForIdle()

        val card = mergedCard().fetchSemanticsNode()
        assertEquals(long, accessibleName(card))
        assertEquals(listOf(long), card.config.getOrNull(SemanticsProperties.ContentDescription))
        // Compose-semantics view of the name; the platform view (spoken once) is asserted by the platformNode_ tests.
        assertEquals(1, Regex(Regex.escape(long)).findAll(accessibleName(card)).count())
        state.dismissAll()
    }

    @Test
    fun undoChild_nameIsTheCallersUndoLabel_andStaysSeparate() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Rückgängig machen", dismissLabel = "Meldung schließen")
        scope.launch { state.show(withUndo("Eintrag gelöscht")) }
        composeTestRule.waitForIdle()

        val undo = composeTestRule.onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
        assertEquals(listOf("Rückgängig machen"), undo.config.getOrNull(SemanticsProperties.ContentDescription))
        assertEquals("Rückgängig machen", accessibleName(undo))
        // Still its own focusable child with its click, and still not part of the card's merged name.
        assertNotNull(undo.config.getOrNull(SemanticsActions.OnClick))
        assertTrue(isDescendantOf(undo, FEEDBACK_STACK_ITEM_TEST_TAG))
        val card = mergedCard().fetchSemanticsNode()
        assertEquals("Eintrag gelöscht", accessibleName(card))
        assertTrue(card.config[SemanticsActions.CustomActions].any { it.label == "Rückgängig machen" })
        state.dismissAll()
    }

    @Test
    fun cardNameComesFromAnExplicitContentDescription_notOnlyFromTheDrawnText() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        show(state, withUndo("Burst two"), "Burst two", results)
        show(state, notify("Burst three"), "Burst three", results)

        // Robolectric shows unclipped text, so the property itself is the proof that a clipped or
        // zero-height text layout cannot leave the node unnamed on a device.
        listOf("Burst two", "Burst three").forEach { message ->
            val config = cardByText(message).fetchSemanticsNode().config
            assertEquals(listOf(message), config.getOrNull(SemanticsProperties.ContentDescription))
        }
        val undo = composeTestRule.undoOf("Burst two").fetchSemanticsNode()
        assertEquals(listOf("Undo"), undo.config.getOrNull(SemanticsProperties.ContentDescription))
        state.dismissAll()
    }

    @Test
    fun cardWithASecondLine_nameKeepsTheSecondLine_afterTheMessage() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        scope.launch {
            state.show(
                FeedbackEvent.Notify(
                    message = "Saved",
                    duration = SnackbarDuration.Indefinite,
                    supportingText = "Draft"
                )
            )
        }
        composeTestRule.waitForIdle()

        // The drawn lines are hidden from speech, so the description must carry the second line.
        assertEquals("Saved, Draft", accessibleName(mergedCard().fetchSemanticsNode()))
        state.dismissAll()
    }

    // ---- Plan 28-12: one speech source per node, read from the PLATFORM node tree (Gate-1 C-6) ----
    //
    // The platform node is what TalkBack reads. On the Compose version used here a node that merges
    // its descendants exposes its own contentDescription as a fake child node and every visible
    // descendant Text as a child TextView; TalkBack speaks both, so a card with an explicit name AND a
    // drawn Text is read twice (device evidence 29, 36, 43). The tests below build the real
    // AccessibilityNodeInfo through the Compose accessibility delegate and count what is spoken.
    // Limit: Robolectric has no screen reader, so "spoken" is the TalkBack-style aggregation below
    // (own text and description, plus every visible, non-focusable descendant, skipping descendants
    // that are their own stop); the real utterance count is a Gate-1 device measurement. The node
    // provider needs real Region support, hence GraphicsMode.NATIVE on these tests.

    private fun enableAccessibility() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val shadow = Shadows.shadowOf(manager)
        shadow.setEnabled(true)
        shadow.setTouchExplorationEnabled(true)
        shadow.setInstalledAccessibilityServiceList(listOf(AccessibilityServiceInfo()))
        shadow.setEnabledAccessibilityServiceList(listOf(AccessibilityServiceInfo()))
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

    private fun spoken(node: SemanticsNode): String = spokenPieces(provider(node), node.id).joinToString(" | ")

    private fun occurrences(haystack: String, needle: String): Int =
        Regex(Regex.escape(needle)).findAll(haystack).count()

    private fun platformCard(message: String): SemanticsNode = cardByText(message).fetchSemanticsNode()

    private fun platformUndo(message: String): SemanticsNode = composeTestRule.undoOf(message).fetchSemanticsNode()

    private fun assertSpokenOnce(speech: String, vararg parts: String) {
        parts.forEach {
            assertEquals("'$it' must be spoken exactly once, but the node speaks: $speech", 1, occurrences(speech, it))
        }
    }

    /**
     * An older card under the card being tested. Robolectric's node provider only exposes a card's
     * description node once the deck holds more than one card, which is also the case on the device
     * (Gate-1 evidence 29, 36), so every platform test below hosts a deck of at least two.
     */
    private fun showOlderCard(state: FeedbackStackState) {
        scope.launch { state.show(notify("Older card")) }
        composeTestRule.waitForIdle()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun platformNode_frontCardMessageIsSpokenOnce() {
        enableAccessibility()
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        showOlderCard(state)
        scope.launch { state.show(notify("Burst four", target)) }
        composeTestRule.waitForIdle()

        assertSpokenOnce(spoken(platformCard("Burst four")), "Burst four")
        state.dismissAll()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun platformNode_cardWithSecondLine_speaksMessageAndSecondLineOnce() {
        enableAccessibility()
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        showOlderCard(state)
        scope.launch {
            state.show(
                FeedbackEvent.Notify(
                    message = "Saved",
                    duration = SnackbarDuration.Indefinite,
                    supportingText = "Draft"
                )
            )
        }
        composeTestRule.waitForIdle()

        assertSpokenOnce(spoken(platformCard("Saved")), "Saved", "Draft")
        state.dismissAll()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun platformNode_cardWithUndo_speaksMessageOnce_andNotTheUndoLabel() {
        enableAccessibility()
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        showOlderCard(state)
        scope.launch { state.show(withUndo("Item deleted")) }
        composeTestRule.waitForIdle()

        val speech = spoken(platformCard("Item deleted"))
        assertSpokenOnce(speech, "Item deleted")
        assertEquals("the Undo child is its own stop, not part of the card name: $speech", 0, occurrences(speech, "Undo"))
        state.dismissAll()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun platformNode_undoChild_speaksItsLabelOnce_english() {
        enableAccessibility()
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        showOlderCard(state)
        scope.launch { state.show(withUndo("Item deleted")) }
        composeTestRule.waitForIdle()

        assertSpokenOnce(spoken(platformUndo("Item deleted")), "Undo")
        state.dismissAll()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun platformNode_undoChild_speaksItsLabelOnce_german() {
        enableAccessibility()
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Rückgängig machen", dismissLabel = "Meldung schließen")
        showOlderCard(state)
        scope.launch { state.show(withUndo("Serie zwei: Eintrag entfernt")) }
        composeTestRule.waitForIdle()

        assertSpokenOnce(spoken(platformUndo("Serie zwei: Eintrag entfernt")), "Rückgängig machen")
        assertSpokenOnce(spoken(platformCard("Serie zwei: Eintrag entfernt")), "Serie zwei: Eintrag entfernt")
        state.dismissAll()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun platformNode_collapsedDeckInEnglish_everyStopSpeaksItsMessageOnce() {
        enableAccessibility()
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        showThreeWithUndo(state, listOf("Burst two", "Burst three", "Burst four"))

        assertFalse(state.pauseState.value.fanned)
        listOf("Burst two", "Burst four").forEach { message ->
            assertSpokenOnce(spoken(platformCard(message)), message)
            assertSpokenOnce(spoken(platformUndo(message)), "Undo")
        }
        state.dismissAll()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun platformNode_collapsedDeckInGerman_everyStopSpeaksItsMessageOnce() {
        enableAccessibility()
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Rückgängig machen", dismissLabel = "Meldung schließen")
        val messages = listOf(
            "Serie zwei: Eintrag entfernt",
            "Serie eins: Eintrag gespeichert",
            "Serie drei: Eintrag verschoben"
        )
        showThreeWithUndo(state, messages)

        assertFalse(state.pauseState.value.fanned)
        listOf(messages[0], messages[2]).forEach { message ->
            assertSpokenOnce(spoken(platformCard(message)), message)
            assertSpokenOnce(spoken(platformUndo(message)), "Rückgängig machen")
        }
        state.dismissAll()
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun platformNode_fannedDeck_everyCardSpeaksItsMessageOnce() {
        enableAccessibility()
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        showThreeWithUndo(state, listOf("Burst two", "Burst three", "Burst four"))
        tapStrip("Burst two")

        assertTrue(state.pauseState.value.fanned)
        listOf("Burst two", "Burst three", "Burst four").forEach { message ->
            assertSpokenOnce(spoken(platformCard(message)), message)
            assertSpokenOnce(spoken(platformUndo(message)), "Undo")
        }
        state.dismissAll()
    }

    @Test
    fun cardBodyText_staysDrawnAndInTheTree_butIsHiddenFromAccessibility() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Undo", dismissLabel = "Dismiss")
        scope.launch {
            state.show(
                FeedbackEvent.Notify(
                    message = "Saved",
                    duration = SnackbarDuration.Indefinite,
                    supportingText = "Draft"
                )
            )
        }
        composeTestRule.waitForIdle()

        // Both lines stay in the unmerged tree (every finder and the drawn card are unchanged) and the
        // merged card still lists them; only the platform speech source is one: the card name.
        listOf("Saved", "Draft").forEach { line ->
            val text = composeTestRule
                .onNode(hasText(line) and !hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG), useUnmergedTree = true)
                .fetchSemanticsNode()
            assertNotNull(
                "$line must be hidden from accessibility",
                text.config.getOrNull(SemanticsProperties.HideFromAccessibility)
            )
        }
        assertEquals(listOf("Saved", "Draft"), mergedTexts(mergedCard().fetchSemanticsNode()))
        assertEquals(
            listOf("Saved, Draft"),
            mergedCard().fetchSemanticsNode().config.getOrNull(SemanticsProperties.ContentDescription)
        )
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
