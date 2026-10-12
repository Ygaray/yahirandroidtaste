package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Source contracts for `ReceiptRow.kt` (Phase 21 VROW-01, VROW-02): plain JVM, no Robolectric.
 * These pin the structure that behavior tests cannot see (the exact signature, the modifier order
 * that makes the node bounds include the padding, what the merged summary may mention, and the
 * single-line text rules).
 */
class ReceiptRowSourceContractTest {

    private val stripped: String =
        SourceContractTestSupport.stripComments(SourceContractTestSupport.source("ReceiptRow.kt"))

    private fun String.compact(): String = filterNot { it.isWhitespace() }

    private fun count(needle: String): Int = SourceContractTestSupport.countOccurrences(stripped, needle)

    private val rowBody: String
        get() = SourceContractTestSupport.functionBody(stripped, "fun ReceiptRow(").compact()

    private val summaryBody: String
        get() = SourceContractTestSupport.functionBody(stripped, "fun ReceiptRowSummary(").compact()

    @Test
    fun `the ReceiptRow declaration is the sketch Y-2 signature in order`() {
        val expected = "funReceiptRow(" +
            "title:String," +
            "modifier:Modifier=Modifier," +
            "subtitle:String?=null," +
            "subtitleTone:ContentTone=ContentTone.Muted," +
            "supportingContent:(@Composable()->Unit)?=null," +
            "value:String?=null," +
            "valueCaption:String?=null," +
            "valueTone:ContentTone=ContentTone.Default," +
            "titleTone:ContentTone=ContentTone.Default," +
            "attentionCue:String?=null," +
            "warnTone:ToneColors=ToneDefaults.warn()," +
            "expanded:Boolean=false," +
            "onClick:(()->Unit)?=null," +
            "onClickLabel:String?=null," +
            "expandedStateLabel:String=\"Expanded\"," +
            "collapsedStateLabel:String=\"Collapsed\"," +
            "trailingAction:(@Composable()->Unit)?=null," +
            "dimmed:Boolean=false," +
            "showDivider:Boolean=true," +
            "expandedContent:(@Composable()->Unit)?=null," +
            "){"
        assertTrue("signature drifted from D-01:\n${rowBody.take(expected.length + 40)}", rowBody.startsWith(expected))
    }

    @Test
    fun `each of the six tag literals is applied exactly once`() {
        listOf(
            "receipt_row_summary",
            "receipt_row_trailing_action",
            "receipt_row_expanded_content",
            "receipt_row_chevron",
            "receipt_row_value_column",
            "receipt_row_cue"
        ).forEach { tag ->
            assertEquals("testTag(\"$tag\") occurrences", 1, count("testTag(\"$tag\")"))
        }
    }

    @Test
    fun `the summary applies tag then merge then click then padding`() {
        val tag = summaryBody.indexOf("testTag(\"receipt_row_summary\")")
        val merge = summaryBody.indexOf("semantics(mergeDescendants=true)")
        val click = summaryBody.indexOf("clickable(")
        val padding = summaryBody.indexOf("padding(vertical=")
        assertTrue("all four present: $tag $merge $click $padding", listOf(tag, merge, click, padding).all { it >= 0 })
        assertTrue("order tag < merge < click < padding: $tag $merge $click $padding", tag < merge && merge < click && click < padding)
    }

    @Test
    fun `the merged summary never mentions the trailing action or the expanded content`() {
        assertFalse(summaryBody.contains("trailingAction"))
        assertFalse(summaryBody.contains("expandedContent"))
    }

    @Test
    fun `the row is stateless`() {
        listOf("mutableStateOf", "mutableIntStateOf", "mutableFloatStateOf", "rememberSaveable", "remember{", "remember(")
            .forEach { needle ->
                assertFalse("ReceiptRow.kt must not hold \"$needle\"", stripped.compact().contains(needle))
            }
    }

    @Test
    fun `there is no separate error tone parameter and the error tone resolves through ToneDefaults`() {
        val forbidden = "error" + "Tone"
        assertFalse(stripped.contains(forbidden))
        assertTrue(stripped.contains("ToneDefaults.error()"))
    }

    @Test
    fun `text lines use the exact line height style`() {
        val compact = stripped.compact()
        assertTrue(compact.contains("LineHeightStyle.Alignment.Center"))
        assertTrue(compact.contains("LineHeightStyle.Trim.None"))
        val uses = count("receiptRowLine()") - 1
        assertTrue("receiptRowLine() used $uses times, expected at least 5", uses >= 5)
    }

    @Test
    fun `title subtitle value and caption are single line with ellipsis`() {
        val compact = stripped.compact()
        assertTrue(SourceContractTestSupport.countOccurrences(compact, "maxLines=1") >= 4)
        assertTrue(SourceContractTestSupport.countOccurrences(compact, "TextOverflow.Ellipsis") >= 4)
        assertTrue(compact.contains("TextDecoration.LineThrough"))
    }

    @Test
    fun `the dimmed alpha reuses the shared disabled constant`() {
        assertTrue(stripped.compact().contains("RECEIPT_ROW_DIMMED_ALPHA=DISABLED_CONTENT_ALPHA"))
        assertFalse("must not redefine 0.38f", stripped.contains("0.38f"))
    }

    // --- Motion contract (plan 21-04) --------------------------------------------------------

    @Test
    fun `the motion constants are the sketch values`() {
        val compact = stripped.compact()
        assertTrue(compact.contains("internalconstvalRECEIPT_ROW_MOTION_MS=200"))
        assertTrue(compact.contains("internalconstvalRECEIPT_ROW_CHEVRON_EXPANDED_DEGREES=180f"))
        assertTrue(compact.contains("internalconstvalRECEIPT_ROW_DIMMED_ALPHA=DISABLED_CONTENT_ALPHA"))
    }

    @Test
    fun `the row reads the motion seams it defines`() {
        listOf("rememberReceiptRowChevronRotation(", "rememberReceiptRowAlpha(").forEach { seam ->
            val uses = count(seam)
            assertTrue("$seam occurs $uses times, expected the definition plus a use in the row", uses >= 2)
        }
    }

    @Test
    fun `every animation uses the shared 200ms tween`() {
        val tweens = count("tween(RECEIPT_ROW_MOTION_MS)")
        assertTrue("tween(RECEIPT_ROW_MOTION_MS) occurs $tweens times, expected at least 6", tweens >= 6)
        listOf("expandVertically(", "fadeIn(", "shrinkVertically(", "fadeOut(").forEach { spec ->
            assertTrue("$spec is missing", count(spec) >= 1)
        }
    }

    @Test
    fun `the expanded content sits below the summary and above the divider`() {
        val summary = rowBody.indexOf("ReceiptRowSummary(")
        val visibility = rowBody.indexOf("AnimatedVisibility(")
        val divider = rowBody.indexOf("HorizontalDivider(")
        assertTrue("all three present: $summary $visibility $divider", listOf(summary, visibility, divider).all { it >= 0 })
        assertTrue("order summary < AnimatedVisibility < divider: $summary $visibility $divider", summary < visibility && visibility < divider)
    }
}
