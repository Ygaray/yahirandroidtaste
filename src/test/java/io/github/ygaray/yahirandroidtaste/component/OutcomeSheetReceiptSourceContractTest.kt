package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Source contracts for the Receipt dispatch and frame (VRCPT-01, D-04, D-05). The sheet `shape` is
 * not visible in the semantics tree (Phase 21 Pitfall 9), so this contract and the Gate-1 corner
 * measurement are its proof. Comments are stripped and whitespace removed before comparing.
 */
class OutcomeSheetReceiptSourceContractTest {

    private fun String.compact(): String = filterNot { it.isWhitespace() }

    private val sheetSource = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("OutcomeSheet.kt"))
    private val receiptSource =
        SourceContractTestSupport.stripComments(SourceContractTestSupport.source("OutcomeSheetReceipt.kt"))

    private fun receiptBody(declaration: String): String =
        SourceContractTestSupport.functionBody(receiptSource, declaration).compact()

    // ---- Dispatch -----------------------------------------------------------------------------

    @Test
    fun `OutcomeSheet declares exactly two public entry points, the hoisted-state one first`() {
        // CalTracker 76-14: the dispatching overload takes a required, named sheetState; the v2.6.0
        // signature (no sheetState, no opt-in) delegates to it with a state that skips the partial
        // detent (v2.7.1), so a tall sheet opens with its pinned bar on screen.
        assertEquals(2, SourceContractTestSupport.countOccurrences(sheetSource, "fun OutcomeSheet("))
        val delegating = SourceContractTestSupport.functionBody(sheetSource, "fun OutcomeSheet(", occurrence = 2).compact()
        assertTrue(delegating.contains("sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)"))
        assertFalse(delegating.contains("SheetScaffold("))
    }

    @Test
    fun `the Receipt guard precedes the call which precedes the return which precedes the legacy scaffold`() {
        val body = SourceContractTestSupport.functionBody(sheetSource, "fun OutcomeSheet(").compact()
        val guard = body.indexOf("if(style==OutcomeSheetStyle.Receipt){")
        val call = body.indexOf("OutcomeSheetReceipt(")
        val ret = body.indexOf("return")
        val legacy = body.indexOf("SheetScaffold(onDismissRequest=onDismissRequest,modifier=modifier,sheetState=sheetState){")

        assertTrue("guard missing", guard >= 0)
        assertTrue("guard $guard must precede the call $call", guard < call)
        assertTrue("call $call must precede the return $ret", call < ret)
        assertTrue("return $ret must precede the legacy scaffold $legacy", ret < legacy)
    }

    @Test
    fun `the legacy scaffold line occurs exactly once`() {
        val legacy = "SheetScaffold(onDismissRequest=onDismissRequest,modifier=modifier,sheetState=sheetState){"
        assertEquals(1, SourceContractTestSupport.countOccurrences(sheetSource.compact(), legacy))
    }

    // ---- Frame --------------------------------------------------------------------------------

    @Test
    fun `the Receipt scaffold carries the 24dp top corner shape`() {
        val body = receiptBody("fun OutcomeSheetReceipt(")
        val scaffold = body.indexOf("SheetScaffold(")
        val shape = body.indexOf(
            "shape=RoundedCornerShape(topStart=Dimens.OutcomeReceipt.CornerRadius,topEnd=Dimens.OutcomeReceipt.CornerRadius)"
        )
        assertTrue("SheetScaffold call missing", scaffold >= 0)
        assertTrue("shape must be passed to SheetScaffold (scaffold $scaffold, shape $shape)", shape > scaffold)
    }

    @Test
    fun `the bar is a sibling placed after the weighted scroll body`() {
        val body = receiptBody("fun OutcomeSheetReceiptContent(")
        val weight = body.indexOf("weight(1f,fill=false)")
        val scroll = body.indexOf("verticalScroll(")
        val bar = body.indexOf("ReceiptNeedsConfirmationBar(")

        assertTrue("weighted body missing", weight >= 0)
        assertTrue("weight $weight must precede the scroll $scroll", weight < scroll)
        assertTrue("weight $weight must precede the bar $bar", weight < bar)
    }

    @Test
    fun `the confirmation bar keeps the Classic tags and none of the generic action tags`() {
        val body = receiptBody("fun ReceiptNeedsConfirmationBar(")
        assertTrue(body.contains("\"outcome_sheet_confirmation_cancel\""))
        assertTrue(body.contains("\"outcome_sheet_confirmation_confirm\""))
        assertFalse(body.contains("outcome_sheet_action_start"))
        assertFalse(body.contains("outcome_sheet_action_end"))
        assertTrue("cancel is a Text action", body.contains("emphasis=ActionButtonEmphasis.Text"))
        assertTrue("confirm is a Filled action", body.contains("emphasis=ActionButtonEmphasis.Filled"))
        assertTrue("confirm honors confirmEnabled", body.contains("enabled=confirmation.confirmEnabled"))
    }

    @Test
    fun `the bar reads the dimension tokens and the escape hatch layout`() {
        val body = receiptBody("fun ReceiptActionBar(")
        assertTrue(body.contains("Dimens.OutcomeReceipt.BarTopPadding"))
        assertTrue(body.contains("Dimens.OutcomeReceipt.BarBottomPadding"))
        assertTrue(body.contains("start=Dimens.HorizontalPadding"))
        assertTrue(body.contains("end=Dimens.HorizontalPadding"))
        assertTrue(body.contains("thickness=Dimens.HairlineBorder"))
        // 22-04 extracted the row into ReceiptActionRow so the pinned bar and the Success actions share it.
        assertTrue(body.contains("ReceiptActionRow("))
        // P7 escape hatch (WR-01): a measured Layout gives each slot its natural width and only
        // constrains an action when the two do not fit, so no equal-weight slots and no Spacer.
        val row = receiptBody("fun ReceiptActionRow(")
        assertTrue(row.contains("Layout("))
        assertTrue(row.contains("receiptActionSlotWidths("))
        assertFalse("equal weights strand unused space (WR-01)", row.contains("weight("))
        assertFalse("no Spacer may combine with the measured row", row.contains("Spacer("))
    }

    // ---- Success (22-04, D-08, D-10) ------------------------------------------------------------

    @Test
    fun `the pill is one annotated Text`() {
        val body = receiptBody("fun ReceiptHandledByPill(")
        assertTrue(body.contains("buildAnnotatedString{"))
        assertTrue(body.contains("withStyle("))
        assertEquals("exactly one Text call", 1, SourceContractTestSupport.countOccurrences(body, "Text("))
        assertTrue(body.contains("weight(1f,fill=false)"))
        assertTrue(body.contains("\"outcome_sheet_handled_by\""))
        assertTrue(body.contains("semantics(mergeDescendants=true)"))
    }

    @Test
    fun `the Success body keeps the D-08 order and the shared blocks`() {
        val body = receiptBody("fun ReceiptSuccessBody(")
        assertTrue(body.contains("UndoAffordanceBody(it,locked=success.inFlight)"))
        assertTrue(body.contains("takeIf{!success.inFlight}"))
        val order = listOf(
            "ReceiptTitleRow(",
            "ReceiptHandledByPill(",
            "success.editableContent",
            "BatchResultsList(",
            "UndoAffordanceBody(",
            "ReceiptSuccessActions("
        ).map { body.indexOf(it) }
        assertTrue("every section must be present: $order", order.all { it >= 0 })
        assertEquals("sections must appear in order", order.sorted(), order)
    }

    @Test
    fun `the Success actions are neither pinned nor tagged as the bar`() {
        val actions = receiptBody("fun ReceiptSuccessActions(")
        val body = receiptBody("fun ReceiptSuccessBody(")
        assertFalse(actions.contains("outcome_sheet_action_bar"))
        assertFalse(body.contains("outcome_sheet_action_bar"))
        assertTrue(actions.contains("actionEnabled("))
        assertTrue(actions.contains("\"outcome_sheet_action_start\""))
        assertTrue(actions.contains("\"outcome_sheet_action_end\""))
        assertTrue(actions.contains("Dimens.OutcomeReceipt.SuccessActionsTopPadding"))
        assertTrue(
            "the pinned `when` keeps Success as Unit",
            receiptBody("fun OutcomeSheetReceiptContent(").contains("isVoiceOutcomeUiState.Success->Unit")
        )
    }

    @Test
    fun `actionEnabled follows the in-flight rule`() {
        // Expression-bodied, so the brace-based functionBody extractor does not apply.
        assertTrue(
            receiptSource.compact().contains(
                "internalfunactionEnabled(action:SheetActionUiModel,inFlight:Boolean):Boolean=" +
                    "action.enabled&&(!inFlight||action.activeWhileInFlight)"
            )
        )
    }

    // ---- Public surface -----------------------------------------------------------------------

    @Test
    fun `the Receipt file declares nothing public and exposes no resource ids`() {
        val raw = SourceContractTestSupport.source("OutcomeSheetReceipt.kt")
        val offenders = raw.lines().filter { line ->
            listOf("fun ", "class ", "object ", "enum ", "val ", "var ", "interface ").any { line.startsWith(it) }
        }
        assertTrue("column-0 (public) declarations found: $offenders", offenders.isEmpty())
        assertFalse(receiptSource.contains("testTagsAsResourceId"))
        assertFalse(receiptSource.contains("Color(0x"))
    }

    // ---- Failure and the final contract (22-05, D-09, D-11, D-13) -------------------------------

    @Test
    fun `the Receipt file contains every stable Y-14 tag literal`() {
        val tags = listOf(
            "outcome_sheet_title",
            "outcome_sheet_badge",
            "outcome_sheet_title_actions",
            "outcome_sheet_transcript",
            "outcome_sheet_action_bar",
            "outcome_sheet_action_start",
            "outcome_sheet_action_end",
            "outcome_sheet_failure_surface",
            "outcome_sheet_failure_tip",
            "outcome_sheet_handled_by",
            "outcome_sheet_confirmation_cancel",
            "outcome_sheet_confirmation_confirm",
            "outcome_sheet_confirmation_top_level_content"
        )
        val missing = tags.filterNot { receiptSource.contains("\"$it\"") }
        assertTrue("tag literals missing from OutcomeSheetReceipt.kt: $missing", missing.isEmpty())
    }

    @Test
    fun `the Failure bar keeps the role hue and the icon on a Filled action`() {
        val body = receiptBody("fun ReceiptFailureBar(")
        assertTrue(body.contains("ActionButtonEmphasis.Filled"))
        assertTrue(body.contains("role=it.role"))
        assertTrue(body.contains("icon=it.icon"))
        assertTrue(body.contains("\"outcome_sheet_action_end\""))
        assertTrue(body.contains("\"outcome_sheet_action_start\""))
        assertTrue("start is the secondary action", body.contains("start=secondary?.let"))
        assertTrue("no bar without an action", body.contains("if(action==null&&secondary==null)return"))
    }

    @Test
    fun `the Failure body is a ToneCard with the body in the body medium style and the undo after the tip`() {
        val body = receiptBody("fun ReceiptFailureBody(")
        assertTrue(body.contains("ToneCard("))
        assertTrue(body.contains("failureToneColors(failure.tone,colors)"))
        assertTrue(body.contains("ProvideTextStyle("))
        assertTrue(body.contains("MaterialTheme.typography.bodyMedium"))
        assertTrue(body.contains("FontWeight.SemiBold"))
        assertTrue(body.contains("MaterialTheme.colorScheme.onSurface"))
        assertTrue(body.contains("semanticsPrefix"))
        val tip = body.indexOf("\"outcome_sheet_failure_tip\"")
        val undo = body.indexOf("UndoAffordanceBody(")
        assertTrue("tip tag must exist", tip >= 0)
        assertTrue("the undo block ($undo) must follow the tip ($tip)", undo > tip)
        assertTrue("undo is not locked in Failure", body.contains("UndoAffordanceBody(it,locked=false)"))
    }

    @Test
    fun `the pinned bar follows a Failure only when it has an action`() {
        val content = receiptBody("fun OutcomeSheetReceiptContent(")
        assertTrue(content.contains("is VoiceOutcomeUiState.Failure->ReceiptFailureBar(outcome)".compact()))
        assertTrue(content.contains("valbarFollows=receiptHasBar(outcome)"))
        assertTrue(content.contains("valbodyEndsFlush=barFollows||receiptSuccessHasActions(outcome)"))
        // Expression-bodied, so the brace-based functionBody extractor does not apply.
        val compact = receiptSource.compact()
        assertTrue(compact.contains("privatefunreceiptHasBar(outcome:VoiceOutcomeUiState):Boolean=when(outcome){"))
        assertTrue(compact.contains("isVoiceOutcomeUiState.Failure->outcome.action!=null||outcome.secondaryAction!=null"))
    }

    @Test
    fun `failureToneColors maps Warn to the warn tone and the rest to the error tone`() {
        assertTrue(
            receiptSource.compact().contains(
                "internalfunfailureToneColors(tone:FailureTone,colors:OutcomeSheetColors):ToneColors=" +
                    "if(tone==FailureTone.Warn)colors.warnToneelsecolors.errorTone"
            )
        )
    }
}
