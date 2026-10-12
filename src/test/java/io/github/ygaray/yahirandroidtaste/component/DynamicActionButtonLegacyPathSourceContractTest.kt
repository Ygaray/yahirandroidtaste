package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Structural same-code-path proof (P3) for [DynamicActionButton]: a call with neither `emphasis`
 * nor `icon` must run the SAME legacy `when (role)` block as the baseline tag. The new branch has
 * to sit ABOVE that block and return early, and the legacy block text must stay exactly as it was.
 *
 * Plain JUnit4 (no Robolectric): reads the main source via [SourceContractTestSupport], strips
 * comments and whitespace, and asserts on the function body.
 */
class DynamicActionButtonLegacyPathSourceContractTest {

    private val stripped: String = SourceContractTestSupport.stripComments(
        SourceContractTestSupport.source("DynamicActionButton.kt")
    )

    private fun body(): String =
        SourceContractTestSupport.functionBody(stripped, "fun DynamicActionButton(").filterNot { it.isWhitespace() }

    @Test
    fun `the emphasis guard returns early and sits strictly above the legacy when block`() {
        val b = body()
        val guard = b.indexOf("if(emphasis!=null||icon!=null){")
        val ret = b.indexOf("return", guard)
        val legacy = b.indexOf("when(role){")
        assertTrue("guard must exist", guard >= 0)
        assertTrue("return must follow the guard", ret > guard)
        assertTrue("legacy when must exist", legacy >= 0)
        assertTrue("guard ($guard) and return ($ret) must precede the legacy block ($legacy)", ret < legacy)
    }

    @Test
    fun `the function body ends with the exact untouched legacy block`() {
        val legacy = "when(role){ActionButtonDefaults.ActionButtonRole.Save->Button(onClick=onClick," +
            "enabled=enabled,colors=ActionButtonDefaults.colors(role),modifier=modifier){Text(label)}" +
            "else->TextButton(onClick=onClick,enabled=enabled,colors=ActionButtonDefaults.colors(role)," +
            "modifier=modifier){Text(label)}}}"
        val b = body()
        assertTrue("body must end with the legacy block, was tail: ${b.takeLast(legacy.length + 20)}", b.endsWith(legacy))
        assertEquals("legacy block must occur exactly once", 1, SourceContractTestSupport.countOccurrences(b, "when(role){"))
    }

    @Test
    fun `the emphasis path reads its dimensions from Dimens ActionButton tokens and uses a pill shape`() {
        val tokens = listOf(
            "FilledMinHeight",
            "FilledHorizontalPadding",
            "ContentVerticalPadding",
            "TextHorizontalPadding",
            "TextSmallHorizontalPadding",
            "TextSmallVerticalPadding",
            "IconSize",
            "IconGap"
        )
        // Scoped to the emphasis path so an unrelated occurrence elsewhere in the file cannot satisfy it.
        val emphasisPath = SourceContractTestSupport.functionBody(stripped, "fun EmphasisActionButton(") +
            SourceContractTestSupport.functionBody(stripped, "fun RowScope.EmphasisContent(")
        tokens.forEach { token ->
            assertTrue("missing Dimens.ActionButton.$token", emphasisPath.contains("Dimens.ActionButton.$token"))
        }
        assertTrue("pill shape expected", emphasisPath.contains("CircleShape"))
        assertTrue("SemiBold label expected", emphasisPath.contains("FontWeight.SemiBold"))
    }
}
