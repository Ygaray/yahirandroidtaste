package io.github.ygaray.yahirandroidtaste.feedback

import io.github.ygaray.yahirandroidtaste.component.SourceContractTestSupport
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Source contracts for [UndoCenterScreen] (VUNDO-02, D-06, T-23-07, T-23-13). Comments are stripped
 * and whitespace removed before comparing. The shared support object only reads the component
 * directory, so the feedback file is read by path here.
 */
class UndoCenterScreenSourceContractTest {

    private fun String.compact(): String = filterNot { it.isWhitespace() }

    private val source = SourceContractTestSupport.stripComments(
        File("src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt").readText()
    )

    @Test
    fun `the parameter list places groups and onUndoGroup between onUndo and the trailing onClear`() {
        val screen = SourceContractTestSupport.functionBody(source, "fun UndoCenterScreen(").compact()
        val expected = "funUndoCenterScreen(entries:List<UndoHistoryEntry>,onNavigateBack:()->Unit," +
            "onUndo:(String)->Unit,groups:List<UndoGroupRowUiModel>=emptyList()," +
            "onUndoGroup:((String)->Unit)?=null,onClear:()->Unit)"
        assertTrue("parameter list drifted:\n${screen.take(300)}", screen.startsWith(expected))
    }

    @Test
    fun `there are exactly two public UndoCenterScreen functions, the v2-7-1 one first`() {
        // D-04: deliberate edit (1 to 2) for the FOPEN-03 overload with a required onOpenTarget.
        assertEquals(2, SourceContractTestSupport.countOccurrences(source, "fun UndoCenterScreen("))
        assertTrue(
            "UndoCenterScreen must stay a public @Composable",
            source.compact().contains("@ComposablefunUndoCenterScreen(")
        )
        val second = SourceContractTestSupport.functionBody(source, "fun UndoCenterScreen(", occurrence = 2).compact()
        val expected = "funUndoCenterScreen(entries:List<UndoHistoryEntry>,onNavigateBack:()->Unit," +
            "onUndo:(String)->Unit,groups:List<UndoGroupRowUiModel>=emptyList()," +
            "onUndoGroup:((String)->Unit)?=null,onOpenTarget:(FeedbackTarget)->Unit,onClear:()->Unit)"
        assertTrue("second overload drifted:\n${second.take(300)}", second.startsWith(expected))
    }

    @Test
    fun `buildUndoCenterRows returns the flat list early when groups is empty`() {
        val body = SourceContractTestSupport.functionBody(source, "internal fun buildUndoCenterRows(").compact()
        assertTrue(
            "early return missing",
            body.contains("if(groups.isEmpty())returnentries.map{UndoCenterRow.Entry(it,indented=false)}")
        )
    }

    @Test
    fun `the group header carries no pointer input gesture`() {
        val body = SourceContractTestSupport.functionBody(source, "private fun UndoGroupHeaderRow(")
        assertFalse("header must not carry a gesture", body.contains("pointerInput"))
        assertFalse("header must not carry a gesture", body.contains("detectTapGestures"))
    }

    @Test
    fun `the Clear history rule still reads the entries only`() {
        val screen = SourceContractTestSupport.functionBody(source, "fun UndoCenterScreen(").compact()
        assertTrue(screen.contains("if(entries.any{it.status!=UndoStatus.Available})"))
    }

    @Test
    fun `the group tag prefixes are the signed kebab-case strings`() {
        val compact = source.compact()
        assertEquals(
            1,
            SourceContractTestSupport.countOccurrences(
                compact,
                "constvalUNDO_GROUP_HEADER_TEST_TAG_PREFIX=\"undo-group-header-\""
            )
        )
        assertEquals(
            1,
            SourceContractTestSupport.countOccurrences(
                compact,
                "constvalUNDO_GROUP_UNDO_ALL_TEST_TAG_PREFIX=\"undo-group-undo-all-\""
            )
        )
    }

    @Test
    fun `the file contains no Log call and no println`() {
        assertFalse("Log call", source.contains("Log."))
        assertFalse("println", source.contains("println("))
    }
}
