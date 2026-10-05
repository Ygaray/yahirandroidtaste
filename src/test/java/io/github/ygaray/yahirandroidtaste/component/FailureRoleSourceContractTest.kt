package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins VFAIL-01's render wiring: the private `FailureBody` in `OutcomeSheet.kt` must hand the
 * Failure action button the caller's `FailureActionUiModel.role` instead of a hardcoded role.
 *
 * ## Why this is a source-structural contract, not a rendered test
 * Robolectric in this repo cannot assert a rendered button's color: every `ActionButtonRole`
 * measures identically (same size, same `Role.Button` semantics), and the harness documents that no
 * `captureToImage` is used anywhere under `src/test` (see the `CardTagRowTest` header). So the
 * wiring is pinned structurally here — plain JUnit4 file reads, no Robolectric, no compose rule —
 * and the actual color is a Gate-2 / consumer-integration check. The model default (Neutral) and
 * the "each role renders one clickable action button" behavior are covered separately in
 * `VoiceModelLabelDefaultsTest` and `OutcomeSheetTest`.
 */
class FailureRoleSourceContractTest {

    private fun failureBody(): String {
        val stripped = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("OutcomeSheet.kt"))
        return SourceContractTestSupport.functionBody(stripped, "private fun FailureBody(")
    }

    @Test
    fun `FailureBody extraction hit the function that renders the action button`() {
        assertTrue(
            "the extracted FailureBody text must contain the action button tag",
            failureBody().contains("outcome_sheet_action_button")
        )
    }

    @Test
    fun `FailureBody binds the action button role to the action's own role`() {
        assertTrue(
            "FailureBody must pass role = action.role to DynamicActionButton",
            failureBody().contains("role = action.role")
        )
    }

    @Test
    fun `FailureBody no longer hardcodes the Neutral role`() {
        assertFalse(
            "FailureBody must not reference the Neutral enum entry (it ignores the caller's role)",
            failureBody().contains("Neutral")
        )
    }
}
