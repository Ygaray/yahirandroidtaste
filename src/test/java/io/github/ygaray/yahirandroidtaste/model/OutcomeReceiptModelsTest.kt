package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import io.github.ygaray.yahirandroidtaste.component.ActionButtonEmphasis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM pins for the Phase 22 Receipt model additions (VRCPT-01..05, VXR-02): enum constant
 * order, the new fields' defaults, value equality, and legacy-arity `copy` carry-forward. Extended
 * in plan 22-01 tasks 2 and 3 for the state arms and the style enum.
 */
class OutcomeReceiptModelsTest {

    // ---- enums (D-03) ----

    @Test
    fun `OutcomeBadge constants are Success Reverted Error Warn in that order`() {
        assertEquals(
            listOf("Success", "Reverted", "Error", "Warn"),
            OutcomeBadge.entries.map { it.name }
        )
    }

    @Test
    fun `FailureTone constants are Error then Warn`() {
        assertEquals(listOf("Error", "Warn"), FailureTone.entries.map { it.name })
    }

    // ---- SheetActionUiModel ----

    @Test
    fun `SheetActionUiModel defaults are enabled, no icon, Neutral role, locked while in flight`() {
        val action = SheetActionUiModel("l", {}, ActionButtonEmphasis.Text)

        assertTrue(action.enabled)
        assertEquals(null, action.icon)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, action.role)
        assertFalse(action.activeWhileInFlight)
    }

    @Test
    fun `SheetActionUiModel is equal by lambda reference and fields`() {
        val onClick: () -> Unit = {}
        val a = SheetActionUiModel("l", onClick, ActionButtonEmphasis.Filled)
        val b = SheetActionUiModel("l", onClick, ActionButtonEmphasis.Filled)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(activeWhileInFlight = true))
        assertNotEquals(a, a.copy(emphasis = ActionButtonEmphasis.Tonal))
        assertNotEquals(a, SheetActionUiModel("l", {}, ActionButtonEmphasis.Filled))
    }

    // ---- HandledByUiModel.prefixLabel / onDevice ----

    @Test
    fun `HandledByUiModel prefixLabel defaults to Handled by and onDevice to false`() {
        val model = HandledByUiModel("Local grammar")

        assertEquals("Handled by", model.prefixLabel)
        assertFalse(model.onDevice)
    }

    @Test
    fun `HandledByUiModel legacy five-argument copy keeps a custom prefixLabel and onDevice`() {
        val model = HandledByUiModel(
            "Local",
            null,
            null,
            null,
            1,
            escalationsLabel = "Escalades :",
            prefixLabel = "Bearbeitet von",
            onDevice = true
        )

        val copied = model.copy("Cloud", "A", "P", "M", 4)

        assertEquals("Bearbeitet von", copied.prefixLabel)
        assertTrue(copied.onDevice)
        assertEquals("Escalades :", copied.escalationsLabel)
        assertEquals("Cloud", copied.tier)
    }

    @Test
    fun `HandledByUiModel models differing only in onDevice are not equal`() {
        assertNotEquals(HandledByUiModel("Local"), HandledByUiModel("Local", onDevice = true))
    }

    // ---- FailureActionUiModel.icon ----

    @Test
    fun `FailureActionUiModel icon defaults to null for the trailing lambda shape`() {
        assertEquals(null, FailureActionUiModel("l") { }.icon)
        assertEquals(null, FailureActionUiModel("l", {}).icon)
    }

    @Test
    fun `FailureActionUiModel legacy two-argument copy keeps a custom icon and role`() {
        val icon = Icons.Filled.Close
        val action = FailureActionUiModel(
            "l",
            {},
            ActionButtonDefaults.ActionButtonRole.Destructive,
            icon
        )

        val copied = action.copy("m", {})

        assertSame(icon, copied.icon)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, copied.role)
        assertEquals("m", copied.label)
    }

    @Test
    fun `FailureActionUiModel three positional arguments still bind the role`() {
        val action = FailureActionUiModel("l", {}, ActionButtonDefaults.ActionButtonRole.Destructive)

        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, action.role)
        assertEquals(null, action.icon)
    }

    // ---- Success ----

    @Test
    fun `Success new fields default to null and destructuring of six components still compiles`() {
        val success = VoiceOutcomeUiState.Success("s")

        assertNull(success.badge)
        assertNull(success.titleActions)
        assertNull(success.startAction)
        assertNull(success.endAction)

        val (summary, handledBy, editable, inFlight, results, undo) = success
        assertEquals("s", summary)
        assertNull(handledBy)
        assertNull(editable)
        assertFalse(inFlight)
        assertTrue(results.isEmpty())
        assertNull(undo)
    }

    // ---- Failure ----

    @Test
    fun `Failure v2_5_1 shapes default every new field`() {
        listOf(VoiceOutcomeUiState.Failure("r"), VoiceOutcomeUiState.Failure("r", null, null)).forEach {
            assertNull(it.title)
            assertNull(it.transcript)
            assertEquals(FailureTone.Error, it.tone)
            assertNull(it.tip)
            assertNull(it.secondaryAction)
            assertNull(it.badge)
            assertNull(it.undo)
        }

        val (reason, handledBy, action, body, prefix) = VoiceOutcomeUiState.Failure("r")
        assertEquals("r", reason)
        assertNull(handledBy)
        assertNull(action)
        assertNull(body)
        assertNull(prefix)
    }

    @Test
    fun `Failure legacy three-argument copy keeps every new field`() {
        val tip: @androidx.compose.runtime.Composable () -> Unit = {}
        val body: @androidx.compose.runtime.Composable () -> Unit = {}
        val secondary = SheetActionUiModel("Cancel", {}, ActionButtonEmphasis.Text)
        val undo = UndoAffordanceUiModel("Undo all")
        val failure = VoiceOutcomeUiState.Failure(
            reason = "r",
            handledBy = null,
            action = null,
            body = body,
            semanticsPrefix = "Erreur :",
            title = "Didn't catch that",
            transcript = "add milk",
            tone = FailureTone.Warn,
            tip = tip,
            secondaryAction = secondary,
            badge = OutcomeBadge.Warn,
            undo = undo
        )

        val copied = failure.copy("r2", null, null)

        assertEquals("r2", copied.reason)
        assertSame(body, copied.body)
        assertEquals("Erreur :", copied.semanticsPrefix)
        assertEquals("Didn't catch that", copied.title)
        assertEquals("add milk", copied.transcript)
        assertEquals(FailureTone.Warn, copied.tone)
        assertSame(tip, copied.tip)
        assertSame(secondary, copied.secondaryAction)
        assertEquals(OutcomeBadge.Warn, copied.badge)
        assertSame(undo, copied.undo)
        assertEquals(failure.copy(reason = "r2"), copied)
    }

    @Test
    fun `Failure toString omits the reason, title, transcript and semanticsPrefix`() {
        val failure = VoiceOutcomeUiState.Failure(
            reason = "secret reason",
            semanticsPrefix = "secret prefix",
            title = "secret title",
            transcript = "secret transcript",
            tone = FailureTone.Warn
        )

        val text = failure.toString()

        assertFalse(text.contains("secret"))
        assertTrue(text.contains("Warn"))
    }

    // ---- NeedsConfirmation ----

    @Test
    fun `NeedsConfirmation named v2_5_1 shape defaults confirmEnabled and titleActions`() {
        val confirmation = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "Delete?",
            items = emptyList(),
            onConfirm = {},
            onCancel = {}
        )

        assertTrue(confirmation.confirmEnabled)
        assertNull(confirmation.titleActions)
        assertEquals(
            "NeedsConfirmation(items=0, selectionMode=AllOrNothing, severity=Neutral)",
            confirmation.toString()
        )
    }
}
