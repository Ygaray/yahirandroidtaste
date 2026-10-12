package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.SelectionMode
import io.github.ygaray.yahirandroidtaste.model.SheetActionUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoAffordanceUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Compile-only canary for Phase 22 (VRCPT-01..05, VXR-02): every v2.5.1 call shape of the voice
 * outcome models and of `OutcomeSheet` still compiles and yields the old behavior after the Receipt
 * additions. Source compatibility only (D-02): consumers recompile on repin, so no binary shims are
 * added. Mirrors [VoiceI18nSourceCompatTest]: composable calls live inside non-invoked
 * `@Composable () -> Unit` lambdas (they only have to COMPILE), model shapes get runtime assertions.
 *
 * Extended in plan 22-01 tasks 2 (state arms) and 3.
 */
class OutcomeSheetSourceCompatV251Test {

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun v251PositionalOutcomeSheetCallShape_compiles() {
        // The positional 3-arg call (outcome, onDismissRequest, modifier) must keep binding.
        val classic: @androidx.compose.runtime.Composable () -> Unit = {
            OutcomeSheet(VoiceOutcomeUiState.Failure("r", null), {}, Modifier)
        }

        // Compile-only: the lambda is intentionally never invoked.
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun receiptOutcomeSheetCallShape_compiles() {
        // The widened shape: style and colors appended after modifier.
        val receipt: @androidx.compose.runtime.Composable () -> Unit = {
            OutcomeSheet(
                VoiceOutcomeUiState.Failure("r", null),
                {},
                Modifier,
                OutcomeSheetStyle.Receipt,
                OutcomeSheetDefaults.colors()
            )
        }

        // Compile-only: the lambda is intentionally never invoked.
    }

    @Test
    fun v251HandledByShapes_compileAndDefaultTheNewFields() {
        val one = HandledByUiModel("Local")
        val five = HandledByUiModel("Local", null, null, null, 2)
        val six = HandledByUiModel("Local", null, null, null, 2, "Escalades :")

        listOf(one, five, six).forEach {
            assertEquals("Handled by", it.prefixLabel)
            assertFalse(it.onDevice)
        }
        assertEquals("Escalations:", five.escalationsLabel)
        assertEquals("Escalades :", six.escalationsLabel)

        // Destructuring of the first components still compiles and yields the original values.
        val (tier, approach, _, _, count) = five
        assertEquals("Local", tier)
        assertNull(approach)
        assertEquals(2, count)

        // The full new named shape.
        val full = HandledByUiModel(
            tier = "Local",
            approach = "grammar",
            provider = "p",
            model = "m",
            escalationCount = 1,
            escalationsLabel = "Escalations:",
            prefixLabel = "Bearbeitet von",
            onDevice = true
        )
        assertEquals("Bearbeitet von", full.prefixLabel)
        assertTrue(full.onDevice)
    }

    @Test
    fun v251FailureActionShapes_compileAndDefaultTheIcon() {
        var clicked = false
        val trailing = FailureActionUiModel("l") { clicked = true }
        assertNull(trailing.icon)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, trailing.role)
        trailing.onClick()
        assertTrue("Trailing lambda must be the onClick callback", clicked)

        val onClick: () -> Unit = {}
        val positional = FailureActionUiModel("l", onClick, ActionButtonDefaults.ActionButtonRole.Destructive)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, positional.role)
        assertNull(positional.icon)

        // Destructuring of the v2.5.1 components still compiles.
        val (label, destructuredOnClick, role) = positional
        assertEquals("l", label)
        assertSame(onClick, destructuredOnClick)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, role)

        // The full new named shape.
        val icon = Icons.Filled.Close
        val full = FailureActionUiModel(
            label = "l",
            onClick = onClick,
            role = ActionButtonDefaults.ActionButtonRole.Save,
            icon = icon
        )
        assertSame(icon, full.icon)
    }

    @Test
    fun v251StateArmShapes_compileAndDefaultTheNewFields() {
        // Success with 1 to 6 positional arguments.
        val handledBy = HandledByUiModel("Local")
        val undo = UndoAffordanceUiModel("Undo all")
        listOf(
            VoiceOutcomeUiState.Success("s"),
            VoiceOutcomeUiState.Success("s", handledBy),
            VoiceOutcomeUiState.Success("s", handledBy, null),
            VoiceOutcomeUiState.Success("s", handledBy, null, false),
            VoiceOutcomeUiState.Success("s", handledBy, null, false, emptyList()),
            VoiceOutcomeUiState.Success("s", handledBy, null, false, emptyList(), undo)
        ).forEach {
            assertNull(it.badge)
            assertNull(it.titleActions)
            assertNull(it.startAction)
            assertNull(it.endAction)
        }

        // Destructuring of Success (six) and Failure (five) still compiles.
        val (summary, _, _, inFlight, _, destructuredUndo) =
            VoiceOutcomeUiState.Success("s", handledBy, null, true, emptyList(), undo)
        assertEquals("s", summary)
        assertTrue(inFlight)
        assertSame(undo, destructuredUndo)
        val (reason, _, _, _, prefix) = VoiceOutcomeUiState.Failure("r", null, null, null, "Error:")
        assertEquals("r", reason)
        assertEquals("Error:", prefix)

        // Failure at every v2.5.1 arity.
        listOf(
            VoiceOutcomeUiState.Failure("r"),
            VoiceOutcomeUiState.Failure("r", null),
            VoiceOutcomeUiState.Failure("r", null, null)
        ).forEach {
            assertNull(it.title)
            assertNull(it.transcript)
            assertEquals(FailureTone.Error, it.tone)
            assertNull(it.tip)
            assertNull(it.secondaryAction)
            assertNull(it.badge)
            assertNull(it.undo)
        }

        // NeedsConfirmation, the named v2.5.1 shape (both consumers use named arguments).
        val confirmation = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "Delete?",
            items = emptyList(),
            onConfirm = {},
            onCancel = {}
        )
        assertTrue(confirmation.confirmEnabled)
        assertNull(confirmation.titleActions)
    }

    @Test
    fun newStateArmShapes_compileWithEveryNamedArgument() {
        val action = SheetActionUiModel("Cancel", {}, ActionButtonEmphasis.Text)
        val titleActions: @androidx.compose.runtime.Composable () -> Unit = {}

        val success = VoiceOutcomeUiState.Success(
            summary = "s",
            badge = OutcomeBadge.Success,
            titleActions = titleActions,
            startAction = action,
            endAction = action
        )
        assertEquals(OutcomeBadge.Success, success.badge)
        assertSame(action, success.startAction)

        val failure = VoiceOutcomeUiState.Failure(
            reason = "r",
            title = "t",
            transcript = "spoken",
            tone = FailureTone.Warn,
            tip = titleActions,
            secondaryAction = action,
            badge = OutcomeBadge.Warn,
            undo = UndoAffordanceUiModel("Undo all")
        )
        assertEquals(FailureTone.Warn, failure.tone)
        assertEquals("spoken", failure.transcript)

        val confirmation = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "Delete?",
            items = emptyList(),
            selectionMode = SelectionMode.AllOrNothing,
            title = "Delete card?",
            severity = ActionButtonDefaults.ActionButtonRole.Destructive,
            reversibilityHint = "Irreversible",
            confirmLabel = "Delete",
            cancelLabel = "Keep",
            topLevelContent = titleActions,
            confirmEnabled = false,
            titleActions = titleActions,
            onConfirm = {},
            onCancel = {}
        )
        assertFalse(confirmation.confirmEnabled)
        assertSame(titleActions, confirmation.titleActions)
    }

    @Test
    fun newSheetActionShape_compilesWithEveryNamedArgument() {
        val onClick: () -> Unit = {}
        val action = SheetActionUiModel(
            label = "Cancel",
            onClick = onClick,
            emphasis = ActionButtonEmphasis.Text,
            enabled = false,
            icon = Icons.Filled.Close,
            role = ActionButtonDefaults.ActionButtonRole.Destructive,
            activeWhileInFlight = true
        )

        assertFalse(action.enabled)
        assertTrue(action.activeWhileInFlight)
        assertSame(onClick, action.onClick)
    }
}
