package io.github.ygaray.yahirandroidtaste

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheet
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheetDefaults
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheetStyle
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackEvent
import io.github.ygaray.yahirandroidtaste.feedback.UndoCenterScreen
import io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryEntry
import io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryStore
import io.github.ygaray.yahirandroidtaste.feedback.UndoPreview
import io.github.ygaray.yahirandroidtaste.feedback.UndoStatus
import io.github.ygaray.yahirandroidtaste.feedback.emitTrackedWithUndo
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v2.8 cut-gate canary (Phase 25, FTGT-01): VERBATIM v2.7.1-shaped call sites of the feedback
 * surfaces that v2.8.0 widens. Every shape here is a v2.7.1 call site, must compile at HEAD, and
 * must keep its v2.7.1 defaults. A later signature slip (a param inserted before an old one, a new
 * required param, a default that changed) fails this class first.
 *
 * Provenance legend (one comment per shape): `SB` is SecondBrain, `CT` is CalTracker. The shapes
 * follow the real consumer call sites named in 25-RESEARCH.md Pitfall 6: SB `FeedbackController`
 * and `SheetFeedbackHost` (the `WithUndo` and `Notify` emitters, the fully named non-declaration
 * order `WithUndo`), and CT's `UndoHistoryStore.append` call that passes the undo lambda as a
 * function value in third position. Consumer-only identifiers become neutral holders and neutral
 * strings; the argument shape (positional, named, trailing lambda) is kept as written.
 *
 * Nothing composable is called: the Material3 tracked path is referenced inside a non-invoked
 * suspend lambda, so it only has to COMPILE.
 *
 * Phase 27 (FOPEN-01) extended the canary with the `OutcomeSheet` call shapes and the ten-component
 * `VoiceOutcomeUiState.Success`, which now carries the appended `target`, `targetLabel` and
 * `targetDetail` fields and an appended `onOpenTarget` parameter.
 */
class V271CallSiteCanaryTest {

    // ------------------------------------------------------------------------------------------
    // FeedbackEvent.Notify
    // ------------------------------------------------------------------------------------------

    @Test
    fun notify_v271Shapes_keepTheirDefaults() {
        // SB FeedbackController: message only, positional.
        val messageOnly = FeedbackEvent.Notify("m")
        // SB FeedbackController: message and duration, both positional.
        val positionalDuration = FeedbackEvent.Notify("m", SnackbarDuration.Long)
        // Named message and duration.
        val named = FeedbackEvent.Notify(message = "m", duration = SnackbarDuration.Indefinite)

        assertEquals(SnackbarDuration.Short, messageOnly.duration)
        assertNull(messageOnly.target)
        assertNull(messageOnly.supportingText)
        assertEquals(SnackbarDuration.Long, positionalDuration.duration)
        assertNull(positionalDuration.target)
        assertEquals(SnackbarDuration.Indefinite, named.duration)
        assertNull(named.supportingText)
    }

    // ------------------------------------------------------------------------------------------
    // FeedbackEvent.WithUndo
    // ------------------------------------------------------------------------------------------

    @Test
    fun withUndo_v271Shapes_keepTheirDefaults() {
        // SB FeedbackController: message and the undo lambda, positional.
        val minimal = FeedbackEvent.WithUndo("m", { })
        // All four v2.7.1 params, positional.
        val positionalAll = FeedbackEvent.WithUndo("m", { }, SnackbarDuration.Short, UndoPreview.Tag("x"))
        // SB SheetFeedbackHost: fully named, NOT in declaration order.
        val sbNamedOutOfOrder = FeedbackEvent.WithUndo(
            message = "m",
            preview = UndoPreview.Tag("x"),
            undoAction = { },
            duration = SnackbarDuration.Short,
        )
        // Named message and undoAction only.
        val namedMinimal = FeedbackEvent.WithUndo(message = "m", undoAction = { })

        assertEquals(SnackbarDuration.Long, minimal.duration)
        assertNull(minimal.preview)
        assertNull(minimal.target)
        assertNull(minimal.supportingText)
        assertEquals(SnackbarDuration.Short, positionalAll.duration)
        assertEquals(UndoPreview.Tag("x"), positionalAll.preview)
        assertNull(positionalAll.target)
        assertEquals(UndoPreview.Tag("x"), sbNamedOutOfOrder.preview)
        assertNull(sbNamedOutOfOrder.supportingText)
        assertEquals(SnackbarDuration.Long, namedMinimal.duration)
        assertNull(namedMinimal.target)
    }

    @Test
    fun dismissScreen_v271Shape_compiles() {
        // SB FeedbackController: message only.
        val dismiss = FeedbackEvent.DismissScreen("m")
        assertEquals("m", dismiss.message)
    }

    // ------------------------------------------------------------------------------------------
    // copy, destructuring, exhaustiveness
    // ------------------------------------------------------------------------------------------

    @Test
    fun copyAndDestructuring_v271Shapes_compile() {
        val notify = FeedbackEvent.Notify("m", SnackbarDuration.Indefinite)
        val withUndo = FeedbackEvent.WithUndo("m", { }, SnackbarDuration.Short, UndoPreview.Tag("x"))

        // copy with one named argument; the other fields are carried over.
        val renamed = notify.copy(message = "x")
        val longer = notify.copy(duration = SnackbarDuration.Long)
        val renamedUndo = withUndo.copy(message = "x")

        assertEquals("x", renamed.message)
        assertEquals(SnackbarDuration.Indefinite, renamed.duration)
        assertEquals("m", longer.message)
        assertEquals(SnackbarDuration.Long, longer.duration)
        assertEquals("x", renamedUndo.message)
        assertEquals(UndoPreview.Tag("x"), renamedUndo.preview)

        // Destructuring: the v2.7.1 components keep their positions (new ones are appended).
        val (notifyMessage, notifyDuration) = notify
        val (m, u, d, p) = withUndo
        assertEquals("m", notifyMessage)
        assertEquals(SnackbarDuration.Indefinite, notifyDuration)
        assertEquals("m", m)
        assertNotNull(u)
        assertEquals(SnackbarDuration.Short, d)
        assertEquals(UndoPreview.Tag("x"), p)
    }

    @Test
    fun feedbackEvent_whenWithThreeBranches_isExhaustive() {
        // Compile-time proof that no fourth variant was added: this `when` has no else branch.
        fun label(event: FeedbackEvent): String = when (event) {
            is FeedbackEvent.Notify -> "notify"
            is FeedbackEvent.WithUndo -> "undo"
            is FeedbackEvent.DismissScreen -> "dismiss"
        }
        assertEquals("notify", label(FeedbackEvent.Notify("m")))
        assertEquals("undo", label(FeedbackEvent.WithUndo("m", { })))
        assertEquals("dismiss", label(FeedbackEvent.DismissScreen("m")))
    }

    // ------------------------------------------------------------------------------------------
    // UndoHistoryStore.append
    // ------------------------------------------------------------------------------------------

    @Test
    fun storeAppend_v271Shapes_keepTheirGroupingDefaults() {
        val store = UndoHistoryStore()

        // SB FeedbackController: message plus trailing undo lambda.
        val ungrouped = store.append("m") { }
        // Message, preview, trailing lambda.
        val withPreview = store.append("m", UndoPreview.Tag("x")) { }
        // Named message and preview, trailing lambda.
        val namedPreview = store.append(message = "m", preview = null) { }
        // Grouped overload, named groupId.
        val grouped = store.append("m", groupId = "g") { }
        // Grouped overload, positional null preview then groupId.
        val groupedPositional = store.append("m", null, "g") { }
        // CT: the undo lambda passed as a function VALUE in third position.
        val fn: suspend () -> Unit = { }
        val ctFunctionValue = store.append("m", UndoPreview.Tag("x"), fn)

        assertNull(store.groupIdOf(ungrouped))
        assertNull(store.groupIdOf(withPreview))
        assertNull(store.groupIdOf(namedPreview))
        assertEquals("g", store.groupIdOf(grouped))
        assertEquals("g", store.groupIdOf(groupedPositional))
        assertNull(store.groupIdOf(ctFunctionValue))
        assertEquals(6, store.entries.value.size)

        // Phase 25 (FTGT-02): entries created through any v2.7.1 shape carry no target.
        listOf(ungrouped, withPreview, namedPreview, grouped, groupedPositional, ctFunctionValue).forEach { id ->
            val entry = store.entries.value.single { it.id == id }
            assertNull(entry.target)
            assertNull(entry.openableTarget)
        }
    }

    // ------------------------------------------------------------------------------------------
    // UndoHistoryEntry copy, withStatus, destructuring
    // ------------------------------------------------------------------------------------------

    @Test
    fun entry_v271Shapes_copyChangesOnlyStatus() {
        val store = UndoHistoryStore()
        val id = store.append("m", UndoPreview.Tag("x")) { }
        val entry: UndoHistoryEntry = store.entries.value.single { it.id == id }

        // The entry constructor is internal, so the entry is obtained through the store.
        val undone = entry.copy(status = UndoStatus.Undone)
        val failed = entry.withStatus(UndoStatus.Failed)
        val (eid, message, timestamp, undoAction, preview, status) = entry

        assertEquals(UndoStatus.Available, entry.status)
        assertEquals(UndoStatus.Undone, undone.status)
        assertEquals(entry.id, undone.id)
        assertEquals(entry.message, undone.message)
        assertEquals(entry.timestamp, undone.timestamp)
        assertEquals(entry.preview, undone.preview)
        assertEquals(UndoStatus.Failed, failed.status)
        assertEquals(entry.id, failed.id)
        assertEquals(id, eid)
        assertEquals("m", message)
        assertEquals(entry.timestamp, timestamp)
        assertNotNull(undoAction)
        assertEquals(UndoPreview.Tag("x"), preview)
        assertEquals(UndoStatus.Available, status)

        // Phase 25 (FTGT-02): the widened entry keeps a null target through copy and withStatus.
        assertNull(entry.target)
        assertNull(entry.openableTarget)
        assertNull(undone.target)
        assertNull(failed.target)
        assertNull(undone.openableTarget)
    }

    // ------------------------------------------------------------------------------------------
    // Material3 tracked path
    // ------------------------------------------------------------------------------------------

    @Test
    fun emitTrackedWithUndo_v271Shape_compiles() = runTest {
        val store = UndoHistoryStore()
        val event = FeedbackEvent.WithUndo("m", { })
        // CT/SB SheetFeedbackHost: the single tracked-append helper; referenced, never invoked.
        val tracked: suspend (SnackbarHostState) -> Unit = { host ->
            host.emitTrackedWithUndo(store, event)
        }
        assertNotNull(tracked)
    }

    // ------------------------------------------------------------------------------------------
    // Phase 27: OutcomeSheet and VoiceOutcomeUiState.Success
    // ------------------------------------------------------------------------------------------

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun outcomeSheet_v271Shapes_compile() {
        val outcome = VoiceOutcomeUiState.Success("s")
        // Positional three-argument call (outcome, onDismissRequest, modifier).
        val positional: @Composable () -> Unit = {
            OutcomeSheet(outcome, {}, Modifier)
        }
        // Receipt five-argument call: style and colors appended after modifier.
        val receipt: @Composable () -> Unit = {
            OutcomeSheet(outcome, {}, Modifier, OutcomeSheetStyle.Receipt, OutcomeSheetDefaults.colors())
        }
        // Hoisted named call with the sheet state (the v2.7.1 overload).
        val hoisted: @Composable () -> Unit = {
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = {},
                style = OutcomeSheetStyle.Receipt,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            )
        }
        // Compile-only: the lambdas are intentionally never invoked.
        assertNotNull(listOf(positional, receipt, hoisted))
    }

    @Test
    fun success_v271Shapes_keepTheirDefaults() {
        val one = VoiceOutcomeUiState.Success("s")
        val ten = VoiceOutcomeUiState.Success("s", null, null, false, emptyList(), null, null, null, null, null)
        val copied = one.copy(summary = "x")
        val (summary, handledBy, editable, inFlight, batch, undo, badge, titleActions, start, end) = ten

        listOf(one, ten, copied).forEach {
            assertNull(it.target)
            assertNull(it.targetLabel)
            assertNull(it.targetDetail)
        }
        assertEquals("x", copied.summary)
        assertEquals("s", summary)
        assertNull(handledBy)
        assertNull(editable)
        assertEquals(false, inFlight)
        assertTrue(batch.isEmpty())
        assertNull(undo)
        assertNull(badge)
        assertNull(titleActions)
        assertNull(start)
        assertNull(end)
    }

    // ------------------------------------------------------------------------------------------
    // Phase 27 (FOPEN-02): BatchRowResultUiModel gains an appended `target`
    // ------------------------------------------------------------------------------------------

    @Test
    fun batchRow_v271Shapes_keepTheirDefaults() {
        val two = BatchRowResultUiModel("Oats", true)
        val three = BatchRowResultUiModel("Milk", false, "not found")
        val named = BatchRowResultUiModel(label = "Rice", succeeded = false, detail = "gone")
        val copied = two.copy(label = "x")
        val (label, succeeded, detail) = three

        listOf(two, three, named, copied).forEach { assertNull(it.target) }
        assertEquals("x", copied.label)
        assertEquals("Milk", label)
        assertEquals(false, succeeded)
        assertEquals("not found", detail)
        assertNull(two.detail)
    }

    // ------------------------------------------------------------------------------------------
    // Phase 27 (FOPEN-03): UndoCenterScreen gains a second overload with a required onOpenTarget
    // ------------------------------------------------------------------------------------------

    /**
     * Every v2.7.1 call shape of [UndoCenterScreen] still binds to the old overload (compile-only:
     * the lambdas are never invoked), while a call that names `onOpenTarget` binds to the new one.
     * The positional four-argument shape is intentionally excluded, as in
     * UndoCenterScreenSourceCompatV251Test: it no longer compiles since `groups` and `onUndoGroup`
     * sit before the trailing `onClear`.
     */
    @Test
    fun undoCenterScreen_v271Shapes_bindToTheOldOverload_andOnOpenTargetBindsToTheNewOne() {
        val allNamed: @Composable () -> Unit = {
            UndoCenterScreen(entries = emptyList(), onNavigateBack = {}, onUndo = { _ -> }, onClear = {})
        }
        val trailingLambda: @Composable () -> Unit = {
            UndoCenterScreen(entries = emptyList(), onNavigateBack = {}, onUndo = { _ -> }) {}
        }
        val groupsAllNamed: @Composable () -> Unit = {
            UndoCenterScreen(
                entries = emptyList(),
                onNavigateBack = {},
                onUndo = { _ -> },
                groups = emptyList(),
                onUndoGroup = { _ -> },
                onClear = {}
            )
        }
        val groupsTrailingLambda: @Composable () -> Unit = {
            UndoCenterScreen(
                entries = emptyList(),
                onNavigateBack = {},
                onUndo = { _ -> },
                groups = emptyList(),
                onUndoGroup = null
            ) {}
        }
        val openAllNamed: @Composable () -> Unit = {
            UndoCenterScreen(
                entries = emptyList(),
                onNavigateBack = {},
                onUndo = { _ -> },
                onOpenTarget = { _ -> },
                onClear = {}
            )
        }
        val openTrailingClear: @Composable () -> Unit = {
            UndoCenterScreen(
                entries = emptyList(),
                onNavigateBack = {},
                onUndo = { _ -> },
                groups = emptyList(),
                onUndoGroup = null,
                onOpenTarget = { _ -> }
            ) {}
        }
        val shapes = listOf(
            allNamed, trailingLambda, groupsAllNamed, groupsTrailingLambda, openAllNamed, openTrailingClear
        )
        assertTrue(shapes.isNotEmpty())
    }
}
