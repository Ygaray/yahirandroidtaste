package io.github.ygaray.yahirandroidtaste.component

import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Source contract for the Classic path of `OutcomeSheet.kt` (VRCPT-01, T-22-12): every Classic
 * function that plan 22-03 must NOT touch is pinned to the SHA-256 of its v2.5.1 text, and each of
 * the four named deltas is pinned to its exact line.
 *
 * ## How the pins were derived
 * Each digest is the SHA-256 of the function text from the `fun Name(` declaration start through its
 * closing brace, after [SourceContractTestSupport.stripComments] and with every whitespace
 * character removed, computed on `git show v2.5.1:.../OutcomeSheet.kt` with a faithful port of
 * [SourceContractTestSupport.functionBody]. They are NOT derived from HEAD, so a change to one of
 * these bodies fails here instead of re-baselining itself. The extraction starts at `fun`, so the
 * declaration visibility is not part of any digest (22-02 made `ProposedItemRow` internal, 22-04
 * made `UndoAffordanceBody` and `BatchResultsList` internal); visibility is pinned separately by
 * one dedicated test below.
 */
class OutcomeSheetClassicParitySourceContractTest {

    private val stripped: String by lazy {
        SourceContractTestSupport.stripComments(SourceContractTestSupport.source("OutcomeSheet.kt"))
    }

    private fun squash(text: String): String = text.filterNot { it.isWhitespace() }

    private fun body(name: String): String =
        squash(SourceContractTestSupport.functionBody(stripped, "fun $name("))

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private fun assertUnchangedSinceV251(name: String, expectedDigest: String) {
        assertEquals(
            "$name must stay byte-equal (comments and whitespace aside) to its v2.5.1 text",
            expectedDigest,
            sha256(body(name))
        )
    }

    // -- Unchanged Classic functions -------------------------------------------------------------

    @Test
    fun `SuccessBody equals its v251 text`() =
        assertUnchangedSinceV251("SuccessBody", "819354de5ed3175715b7daa67acfe7ada3187876fdfc28891c9d7b8fee1c6866")

    @Test
    fun `UndoRowItem equals its v251 text`() =
        assertUnchangedSinceV251("UndoRowItem", "5a56e1d9b11ff55b58d1a2b4aca28f66cfb29e384fffbd9419a42dfcf9f3abdb")

    @Test
    fun `HandledByRow equals its v251 text`() =
        assertUnchangedSinceV251("HandledByRow", "3539e2512048b1fd0049eef8b26e401510aa9f4f4b18cbc9af0baf533f44bb8d")

    @Test
    fun `NeedsConfirmationBody equals its v251 text`() =
        assertUnchangedSinceV251(
            "NeedsConfirmationBody",
            "2b5f0571a23bb7bda3d1f1be12264cb1d722af940eca3fb662cf77e0f6ed18ed"
        )

    @Test
    fun `BatchResultsList equals its v251 text`() =
        assertUnchangedSinceV251("BatchResultsList", "97e3fa2c0fcc31d7c02fa2f386fb07f4c6b9b66533c2658068c5554d537828c3")

    @Test
    fun `ProposedItemRow equals its v251 text`() =
        assertUnchangedSinceV251("ProposedItemRow", "9155aa36fc9e3835bc1c5083ab5b7c5185204168e459d9eab161d7a1a4f28cdd")

    // -- OutcomeSheetContent: scroll body and pinned footer untouched ----------------------------

    @Test
    fun `OutcomeSheetContent keeps the weighted scroll body`() {
        assertTrue(
            body("OutcomeSheetContent").contains("Modifier.weight(1f,fill=false).verticalScroll(rememberScrollState())")
        )
    }

    @Test
    fun `OutcomeSheetContent footer pins only NeedsConfirmationActions`() {
        assertTrue(
            body("OutcomeSheetContent").contains(
                "when(outcome){isVoiceOutcomeUiState.Success->UnitisVoiceOutcomeUiState.Failure->Unit" +
                    "isVoiceOutcomeUiState.NeedsConfirmation->NeedsConfirmationActions(outcome)}"
            )
        )
    }

    @Test
    fun `OutcomeSheetContent routes Failure through FailureBody with colors and the padded undo block`() {
        val content = body("OutcomeSheetContent")
        assertTrue(content.contains("isVoiceOutcomeUiState.Failure->{FailureBody(outcome,colors)"))
        assertTrue(
            content.contains(
                "outcome.undo?.let{undo->Box(Modifier.padding(horizontal=Dimens.HorizontalPadding))" +
                    "{UndoAffordanceBody(undo,locked=false)}}"
            )
        )
    }

    // -- NeedsConfirmationActions: delta (a) -----------------------------------------------------

    @Test
    fun `Cancel call is unchanged and Confirm call is the v251 call plus enabled`() {
        val actions = body("NeedsConfirmationActions")
        assertTrue(
            "Cancel must equal its v2.5.1 call",
            actions.contains(
                "DynamicActionButton(label=confirmation.cancelLabel," +
                    "role=ActionButtonDefaults.ActionButtonRole.Neutral,onClick=confirmation.onCancel," +
                    "modifier=Modifier.testTag(\"outcome_sheet_confirmation_cancel\"))"
            )
        )
        assertTrue(
            "Confirm must equal its v2.5.1 call plus enabled = confirmation.confirmEnabled",
            actions.contains(
                "DynamicActionButton(label=confirmation.confirmLabel,role=confirmation.severity," +
                    "onClick=confirmation.onConfirm,enabled=confirmation.confirmEnabled," +
                    "modifier=Modifier.testTag(\"outcome_sheet_confirmation_confirm\"))"
            )
        )
    }

    // -- Live region: delta (d) ------------------------------------------------------------------

    @Test
    fun `refused surface carries the assertive live region directly after its testTag`() {
        val undoBody = body("UndoAffordanceBody")
        assertTrue(
            undoBody.contains(".testTag(\"outcome_sheet_undo_refused\").semantics{liveRegion=LiveRegionMode.Assertive}")
        )
        assertFalse("the live region must not merge descendants", undoBody.contains("mergeDescendants"))
        assertEquals(
            "exactly one live region in the Classic file",
            1,
            SourceContractTestSupport.countOccurrences(squash(stripped), "liveRegion=LiveRegionMode.")
        )
    }

    // -- Warn seam: delta (b) --------------------------------------------------------------------

    @Test
    fun `classicFailureSurfaceColors returns the warn tone for Warn and the scheme roles otherwise`() {
        val seam = body("classicFailureSurfaceColors")
        assertTrue(
            seam.contains(
                "if(tone==FailureTone.Warn){returncolors.warnTone}" +
                    "returnToneColors(scheme.errorContainer,scheme.onErrorContainer)"
            )
        )
        assertFalse("the Error branch must not read the colors error tone", seam.contains("errorTone"))
    }

    @Test
    fun `FailureBody paints through the seam and keeps the action wiring`() {
        val failure = body("FailureBody")
        assertTrue(
            failure.contains("classicFailureSurfaceColors(failure.tone,colors,MaterialTheme.colorScheme)")
        )
        assertTrue(failure.contains("color=surface.container"))
        assertTrue(failure.contains("contentColor=surface.content"))
        assertTrue(failure.contains("outcome_sheet_action_button"))
        assertTrue(failure.contains("role=action.role"))
    }

    @Test
    fun `FailureBody signature carries colors as its only extra parameter`() {
        assertTrue(
            stripped.contains("private fun FailureBody(failure: VoiceOutcomeUiState.Failure, colors: OutcomeSheetColors)")
        )
    }

    // -- Visibility pin (22-04, T-22-12) ---------------------------------------------------------

    @Test
    fun `the shared blocks are internal and every other Classic block stays private`() {
        val file = squash(stripped)
        listOf("UndoAffordanceBody(", "BatchResultsList(", "ProposedItemRow(").forEach { name ->
            assertTrue("$name must be internal", file.contains("internalfun$name"))
        }
        listOf(
            "FailureBody(",
            "SuccessBody(",
            "HandledByRow(",
            "NeedsConfirmationBody(",
            "NeedsConfirmationActions(",
            "UndoRowItem("
        ).forEach { name ->
            assertTrue("$name must stay private", file.contains("privatefun$name"))
        }
    }

    // -- Dispatch line stays untouched -----------------------------------------------------------

    @Test
    fun `legacy SheetScaffold line and Classic content call are the 22-03 shape`() {
        assertTrue(stripped.contains("SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier) {"))
        assertTrue(stripped.contains("OutcomeSheetContent(outcome, colors = colors)"))
    }
}
