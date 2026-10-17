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
}
