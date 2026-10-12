package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.SheetActionUiModel
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
