package io.github.ygaray.yahirandroidtaste.feedback

import io.github.ygaray.yahirandroidtaste.component.SourceContractTestSupport
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Source contracts for the feedback deck gestures (phase 26 plan 03, T-26-09, T-26-10, T-26-12).
 * Comments are stripped and whitespace removed before comparing. The shared support object only
 * reads the component directory, so the feedback files are read by path here.
 */
class FeedbackStackSourceContractTest {

    private fun String.compact(): String = filterNot { it.isWhitespace() }

    private fun feedbackSource(file: String): String = SourceContractTestSupport.stripComments(
        File("src/main/java/io/github/ygaray/yahirandroidtaste/feedback/$file").readText()
    )

    @Test
    fun exactlyOneTapGestureOwnerPerCard() {
        val card = feedbackSource("FeedbackCard.kt")
        assertEquals(1, SourceContractTestSupport.countOccurrences(card, "detectTapGestures("))
    }

    @Test
    fun cardAddsNoClickableModifier() {
        val card = feedbackSource("FeedbackCard.kt")
        assertFalse("a clickable modifier is a second touch owner", card.contains("clickable("))
        assertFalse(card.contains("combinedClickable("))
    }

    @Test
    fun pointerObserverIsInitialPassAndNeverConsumes() {
        val deck = feedbackSource("FeedbackDeck.kt")
        assertTrue(deck.contains("PointerEventPass.Initial"))
        assertFalse("the observer must not consume a change", deck.contains("consume("))
        assertFalse("the observer must not wait for up or cancellation", deck.contains("waitForUpOrCancellation"))
        assertFalse(deck.contains("consumeAllChanges"))
    }

    @Test
    fun hostKeepsTheResolveBeforeOpenRule() {
        val host = feedbackSource("FeedbackStackHost.kt").compact()
        assertTrue(
            "resolve-before-open rule drifted",
            host.contains("if(state.resolve(item,FeedbackResult.Opened))")
        )
    }

    @Test
    fun noLoggingNorPrintln() {
        listOf(
            "FeedbackCard.kt",
            "FeedbackDeck.kt",
            "FeedbackDeckMetrics.kt",
            "FeedbackStackHost.kt",
            "FeedbackStackState.kt"
        ).forEach { file ->
            val source = feedbackSource(file)
            assertFalse("$file logs", source.contains("Log."))
            assertFalse("$file logs", source.contains("android.util.Log"))
            assertFalse("$file prints", source.contains("println("))
        }
    }

    // ---- plan 26-04: the swipe crash rule (T-26-13, T-26-14) ----

    private val feedbackMainDir = File("src/main/java/io/github/ygaray/yahirandroidtaste/feedback")

    private fun allFeedbackMainSources(): Map<String, String> =
        feedbackMainDir.listFiles { file -> file.extension == "kt" }.orEmpty()
            .associate { it.name to SourceContractTestSupport.stripComments(it.readText()) }

    /** The text from [openIndex] (which must hold [open]) through its matching close, string aware. */
    private fun balancedFrom(src: String, openIndex: Int, open: Char, close: Char): String {
        require(src[openIndex] == open) { "index $openIndex holds ${src[openIndex]}, not $open" }
        var depth = 0
        var inString = false
        var i = openIndex
        while (i < src.length) {
            val c = src[i]
            when {
                c == '\\' && inString -> i++
                c == '"' -> inString = !inString
                !inString && c == open -> depth++
                !inString && c == close -> {
                    depth--
                    if (depth == 0) return src.substring(openIndex, i + 1)
                }
            }
            i++
        }
        error("$open never balances after index $openIndex")
    }

    /** Every brace block that follows [prefix] (compacted text, ending in `{`), braces included. */
    private fun blocksAfter(compact: String, prefix: String): List<String> {
        val blocks = mutableListOf<String>()
        var from = compact.indexOf(prefix)
        while (from >= 0) {
            blocks += balancedFrom(compact, from + prefix.length - 1, '{', '}')
            from = compact.indexOf(prefix, from + prefix.length)
        }
        return blocks
    }

    private fun draggableStateName(card: String): String =
        requireNotNull(
            Regex("""val\s+(\w+)\s*=\s*remember\([^)]*\)\s*\{\s*AnchoredDraggableState\(""")
                .find(card)?.groupValues?.get(1)
        ) { "the AnchoredDraggableState declaration was not found" }

    @Test
    fun noFeedbackFileCallsTheThrowingOffsetAccessor() {
        val sources = allFeedbackMainSources()
        assertTrue("no feedback sources were read", sources.size >= 10)
        sources.forEach { (name, source) ->
            assertFalse("$name calls the throwing offset accessor", source.contains("requireOffset"))
        }
    }

    @Test
    fun noRevealOrSwipeableRowReuse() {
        allFeedbackMainSources().forEach { (name, source) ->
            assertFalse("$name reuses SwipeableActionRow", source.contains("SwipeableActionRow"))
            assertFalse("$name reuses RevealActionRow", source.contains("RevealActionRow"))
        }
    }

    @Test
    fun anchoredDraggableStateIsSeededAtConstruction() {
        val card = feedbackSource("FeedbackCard.kt").compact()
        val call = "AnchoredDraggableState("
        assertEquals(1, SourceContractTestSupport.countOccurrences(card, call))
        val arguments = balancedFrom(card, card.indexOf(call) + call.length - 1, '(', ')')
        assertTrue("the anchors must be given at construction", arguments.contains("DraggableAnchors"))
        assertTrue("the seed must be the Settled anchor", arguments.contains("FeedbackCardAnchor.Settledat0f"))
    }

    @Test
    fun anchorsAreWidenedOnlyOnceTheWidthIsPositive() {
        val card = feedbackSource("FeedbackCard.kt").compact()
        val update = card.indexOf(".updateAnchors(")
        assertTrue("updateAnchors is missing", update > 0)
        assertTrue(
            "updateAnchors must sit behind a positive-width guard",
            card.substring(0, update).endsWith("if(size.width>0){valwidth=size.width.toFloat()drag")
        )
    }

    @Test
    fun offsetIsReadThroughTheNaNGuard() {
        val raw = feedbackSource("FeedbackCard.kt")
        val card = raw.compact()
        val name = draggableStateName(raw)
        assertTrue(card.contains("isNaN()"))
        val reads = SourceContractTestSupport.countOccurrences(card, "$name.offset")
        val guarded = SourceContractTestSupport.countOccurrences(card, "$name.offset.takeUnless{it.isNaN()}")
        assertTrue("the card never reads the offset", reads >= 1)
        assertEquals("every offset read must go through the NaN guard", reads, guarded)
        val insideAllowedBlocks = (blocksAfter(card, "graphicsLayer{") + blocksAfter(card, "derivedStateOf{"))
            .sumOf { SourceContractTestSupport.countOccurrences(it, "$name.offset") }
        assertEquals(
            "an offset read outside a graphicsLayer or derivedStateOf block can throw on the first frame",
            reads,
            insideAllowedBlocks
        )
    }

    @Test
    fun translationLivesInsideTheDraggableRootNotOnIt() {
        val card = feedbackSource("FeedbackCard.kt").compact()
        val draggable = card.indexOf(".anchoredDraggable(")
        val translation = card.indexOf("translationX=")
        assertTrue(draggable > 0 && translation > 0)
        assertTrue(
            "the draggable root must be stationary: the translation belongs to the Surface inside it",
            translation > draggable
        )
        assertEquals(1, SourceContractTestSupport.countOccurrences(card, "translationX="))
    }

    @Test
    fun swipeSettleCallsOnlyTheDismissCallback() {
        val card = feedbackSource("FeedbackCard.kt").compact()
        val blocks = blocksAfter(card, "LaunchedEffect(drag){")
        assertEquals(1, blocks.size)
        val effect = blocks.single()
        assertTrue("the settle must call the dismiss callback", effect.contains("latestOnDismiss()"))
        assertFalse("a swipe must never undo", effect.contains("onUndo"))
        assertFalse("a swipe must never open", effect.contains("onBodyTap") || effect.contains("latestBodyTap"))
        assertFalse("a swipe must never fan", effect.contains("onPeekTap") || effect.contains("latestPeekTap"))
    }

    // ---- plan 26-05: the A11Y-01 semantics (T-26-17) and library hygiene ----

    @Test
    fun cardSemanticsCarryPoliteRegionDismissAndTraversalIndex() {
        val card = feedbackSource("FeedbackCard.kt")
        assertTrue(card.contains("LiveRegionMode.Polite"))
        assertTrue(card.contains("dismiss(label"))
        assertTrue(card.contains("traversalIndex"))
    }

    @Test
    fun semanticsClickCallsTheResolveFirstCallback() {
        val card = feedbackSource("FeedbackCard.kt").compact()
        val call = card.indexOf("onClick(")
        assertTrue("the semantics onClick is missing", call > 0)
        val arguments = balancedFrom(card, call + "onClick".length, '(', ')')
        val lambda = balancedFrom(card, call + "onClick".length + arguments.length, '{', '}')
        assertTrue("the semantics click must go through the body tap callback", lambda.contains("latestBodyTap("))
        assertFalse("the semantics click must not navigate by itself", lambda.contains("onOpenTarget"))
        assertFalse("the semantics click must not resolve a card by itself", lambda.contains("resolve("))
        assertFalse("the semantics click must not call the raw callback", lambda.contains("onBodyTap("))
    }

    @Test
    fun noConsumerOrDomainImportInTheFeedbackDeckFiles() {
        val allowed = listOf("androidx.", "kotlin.", "kotlinx.", "io.github.ygaray.yahirandroidtaste.")
        listOf("FeedbackCard.kt", "FeedbackDeck.kt", "FeedbackDeckMetrics.kt").forEach { file ->
            val imports = File("src/main/java/io/github/ygaray/yahirandroidtaste/feedback/$file")
                .readLines()
                .filter { it.startsWith("import ") }
                .map { it.removePrefix("import ").trim() }
            assertTrue("$file has no imports to check", imports.isNotEmpty())
            imports.forEach { import ->
                assertTrue("$file imports $import", allowed.any { import.startsWith(it) })
            }
        }
    }
}
