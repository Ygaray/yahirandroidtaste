package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Source-structural-contract tests for [CardTagRow]'s per-tag `containerColorOverride`
 * auto-threading (TAGCOLOR-01, Phase 7). `CardTagRow` has never had a dedicated test in this
 * repo — this class follows the same idiom [CardBaseTest] established for composables this
 * Robolectric harness cannot assert rendered colors on (no `captureToImage` usage anywhere under
 * `src/test/java`): parsing the real, committed `CardTagRow.kt` source via
 * [SourceContractTestSupport] rather than inventing pixel-capture infrastructure.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CardTagRowTest {

    @Test
    fun `TagChipWithContextMenu branch threads tag color into containerColorOverride`() {
        val callRegion = callRegion("TagChipWithContextMenu(")

        assertTrue(
            "CardTagRow's TagChipWithContextMenu(...) call (the hasCapability branch) must pass " +
                "containerColorOverride = tag.color so the auto-thread wiring is proven",
            callRegion.contains("containerColorOverride = tag.color")
        )
    }

    @Test
    fun `plain AppChip branch (no capability) also threads tag color into containerColorOverride`() {
        val callRegion = callRegion("AppChip(", occurrence = 1)

        assertTrue(
            "CardTagRow's plain AppChip(...) call (the !hasCapability branch) must pass " +
                "containerColorOverride = tag.color, mirroring the TagChipWithContextMenu branch",
            callRegion.contains("containerColorOverride = tag.color")
        )
    }

    @Test
    fun `overflow +N AppChip call never receives a containerColorOverride`() {
        val callRegion = callRegion("AppChip(", occurrence = 2)

        assertEquals(
            "The '+N' overflow AppChip call has no backing TagChipUiModel and must stay " +
                "theme-default — it must never receive containerColorOverride",
            0,
            SourceContractTestSupport.countOccurrences(callRegion, "containerColorOverride")
        )
    }

    // --- Source-reading helpers -------------------------------------------------------------

    private fun cardTagRowBody(): String =
        SourceContractTestSupport.stripComments(
            SourceContractTestSupport.functionBody(
                SourceContractTestSupport.source("CardTagRow.kt"),
                "fun CardTagRow("
            )
        )

    /**
     * Isolates one call's full argument-list text (from the call name through its matching
     * closing paren) inside [CardTagRow]'s body — depth-aware paren balancing, mirroring
     * [CardBaseTest]'s brace-balancing helper convention for source-structural assertions.
     */
    private fun callRegion(callPrefix: String, occurrence: Int = 1): String {
        val body = cardTagRowBody()
        var searchFrom = 0
        var callStart = -1
        repeat(occurrence) {
            callStart = body.indexOf(callPrefix, searchFrom)
            assertTrue(
                "Could not locate occurrence $occurrence of '$callPrefix' inside CardTagRow's body",
                callStart >= 0
            )
            searchFrom = callStart + callPrefix.length
        }
        val openParen = body.indexOf('(', callStart)
        val closeParen = matchingCloseParenIndex(body, openParen)
        return body.substring(callStart, closeParen + 1)
    }

    private fun matchingCloseParenIndex(text: String, openParenIndex: Int): Int {
        var depth = 1
        var i = openParenIndex + 1
        while (i < text.length) {
            when (text[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        error("No matching closing paren found for opening paren at index $openParenIndex")
    }
}
