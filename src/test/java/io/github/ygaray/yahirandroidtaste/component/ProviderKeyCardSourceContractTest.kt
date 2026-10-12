package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Source contracts for [ProviderKeyCard] (VKEY-01, VKEY-02, D-04, D-05, T-23-04, T-23-07). Comments
 * are stripped and whitespace removed before comparing.
 */
class ProviderKeyCardSourceContractTest {

    private fun String.compact(): String = filterNot { it.isWhitespace() }

    private val source =
        SourceContractTestSupport.stripComments(SourceContractTestSupport.source("ProviderKeyCard.kt"))

    @Test
    fun `each action button pairs its tag with its role and emphasis`() {
        val segments = source.compact().split("DynamicActionButton(").drop(1)
        assertEquals("four DynamicActionButton calls", 4, segments.size)

        fun segmentFor(tagConst: String): String {
            val matches = segments.filter { it.contains("testTag($tagConst)") }
            assertEquals("exactly one button per $tagConst", 1, matches.size)
            return matches.single()
        }

        val save = segmentFor("TAG_ACTION_SAVE")
        assertTrue(save.contains("role=ActionButtonDefaults.ActionButtonRole.Save"))
        assertTrue(save.contains("emphasis=ActionButtonEmphasis.Filled"))

        val update = segmentFor("TAG_ACTION_UPDATE")
        assertTrue(update.contains("role=ActionButtonDefaults.ActionButtonRole.Neutral"))
        assertTrue(update.contains("emphasis=ActionButtonEmphasis.Tonal"))

        val clear = segmentFor("TAG_ACTION_CLEAR")
        assertTrue(clear.contains("role=ActionButtonDefaults.ActionButtonRole.Destructive"))
        assertTrue(clear.contains("emphasis=ActionButtonEmphasis.Text"))

        val cancel = segmentFor("TAG_ACTION_CANCEL")
        assertTrue(cancel.contains("role=ActionButtonDefaults.ActionButtonRole.Neutral"))
        assertTrue(cancel.contains("emphasis=ActionButtonEmphasis.Text"))
    }

    @Test
    fun `the tag constants carry the stable D-04 strings`() {
        val compact = source.compact()
        listOf(
            "TAG_SAVED_SUMMARY=\"provider_key_saved_summary\"",
            "TAG_ACTION_SAVE=\"provider_key_action_save\"",
            "TAG_ACTION_UPDATE=\"provider_key_action_update\"",
            "TAG_ACTION_CLEAR=\"provider_key_action_clear\"",
            "TAG_ACTION_CANCEL=\"provider_key_action_cancel\""
        ).forEach { assertEquals(it, 1, SourceContractTestSupport.countOccurrences(compact, it)) }
    }

    @Test
    fun `the file declares exactly two ProviderKeyCard functions`() {
        assertEquals(2, SourceContractTestSupport.countOccurrences(source, "fun ProviderKeyCard("))
    }

    @Test
    fun `the reveal declaration precedes the first providerKeyMode call in the card body`() {
        val body = SourceContractTestSupport.functionBody(source, "fun ProviderKeyCard(").compact()
        val reveal = body.indexOf("varrevealedbyrememberSaveable")
        val mode = body.indexOf("providerKeyMode(")
        assertTrue("reveal declaration missing", reveal >= 0)
        assertTrue("providerKeyMode call missing", mode >= 0)
        assertTrue("reveal $reveal must precede the Summary/Field branch $mode", reveal < mode)
    }

    @Test
    fun `the file contains no Log call and no println`() {
        assertFalse("Log call", source.contains("Log."))
        assertFalse("println", source.contains("println("))
    }
}
