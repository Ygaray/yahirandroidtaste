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
    fun `OutcomeSheet declares exactly one public entry point`() {
        assertEquals(1, SourceContractTestSupport.countOccurrences(sheetSource, "fun OutcomeSheet("))
    }

    @Test
    fun `the Receipt guard precedes the call which precedes the return which precedes the legacy scaffold`() {
        val body = SourceContractTestSupport.functionBody(sheetSource, "fun OutcomeSheet(").compact()
        val guard = body.indexOf("if(style==OutcomeSheetStyle.Receipt){")
        val call = body.indexOf("OutcomeSheetReceipt(")
        val ret = body.indexOf("return")
        val legacy = body.indexOf("SheetScaffold(onDismissRequest=onDismissRequest,modifier=modifier){")

        assertTrue("guard missing", guard >= 0)
        assertTrue("guard $guard must precede the call $call", guard < call)
        assertTrue("call $call must precede the return $ret", call < ret)
        assertTrue("return $ret must precede the legacy scaffold $legacy", ret < legacy)
    }

    @Test
    fun `the legacy scaffold line occurs exactly once`() {
        val legacy = "SheetScaffold(onDismissRequest=onDismissRequest,modifier=modifier){"
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
        // P7 escape hatch: both slots share the row, no Spacer(weight 1f).
        val row = receiptBody("fun ReceiptActionRow(")
        assertTrue(row.contains("barArrangement("))
        assertEquals(2, SourceContractTestSupport.countOccurrences(row, "weight(1f,fill=false)"))
        assertFalse("no Spacer may combine with weighted actions", row.contains("Spacer("))
        assertTrue(receiptSource.compact().contains("hasStart&&hasEnd->Arrangement.SpaceBetween"))
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
}
