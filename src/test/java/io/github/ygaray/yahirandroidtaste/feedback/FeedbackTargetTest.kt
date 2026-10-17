package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import java.lang.reflect.Modifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FTGT-01: the opaque [FeedbackTarget] value type and the appended `target` / `supportingText`
 * params on [FeedbackEvent.Notify] and [FeedbackEvent.WithUndo].
 */
class FeedbackTargetTest {

    // ---- (a) value equality ----

    @Test
    fun sameStrings_areEqual_withSameHashCode() {
        val a = FeedbackTarget("item-42", "Open item")
        val b = FeedbackTarget("item-42", "Open item")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun differingKeyOrLabel_areUnequal() {
        val base = FeedbackTarget("item-42", "Open item")
        assertNotEquals(base, FeedbackTarget("item-43", "Open item"))
        assertNotEquals(base, FeedbackTarget("item-42", "Open other"))
    }

    // ---- (b) no lambda, no Context: exactly two String fields ----

    @Test
    fun fields_areExactlyKeyAndOpenLabel_bothStrings() {
        val fields = FeedbackTarget::class.java.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) }
        assertTrue("vacuity guard: FeedbackTarget has instance fields", fields.isNotEmpty())
        assertEquals(setOf("key", "openLabel"), fields.map { it.name }.toSet())
        fields.forEach { assertEquals(String::class.java, it.type) }
    }

    // ---- (c) construction never throws ----

    @Test
    fun blankLabel_constructsWithoutThrowing() {
        val blankLabel = FeedbackTarget("item-42", "")
        val allBlank = FeedbackTarget("", "")
        assertEquals("", blankLabel.openLabel)
        assertEquals("", allBlank.key)
    }

    // ---- (d) event defaults and read-back ----

    @Test
    fun notifyDefaults_carryNulls() {
        val n = FeedbackEvent.Notify("m")
        assertNull(n.target)
        assertNull(n.supportingText)
        assertEquals(SnackbarDuration.Short, n.duration)
    }

    @Test
    fun withUndoDefaults_carryNulls() {
        val w = FeedbackEvent.WithUndo("m", { })
        assertEquals(SnackbarDuration.Long, w.duration)
        assertNull(w.preview)
        assertNull(w.target)
        assertNull(w.supportingText)
    }

    @Test
    fun notify_readsBackTargetAndSupportingText_andCopyKeepsThem() {
        val t = FeedbackTarget("item-42", "Open item")
        val n = FeedbackEvent.Notify("m", target = t, supportingText = "s")
        assertEquals(t, n.target)
        assertEquals("s", n.supportingText)
        val copied = n.copy(message = "x")
        assertEquals("x", copied.message)
        assertEquals(t, copied.target)
        assertEquals("s", copied.supportingText)
    }

    @Test
    fun withUndo_readsBackTargetAndSupportingText_andCopyKeepsThem() {
        val t = FeedbackTarget("item-42", "Open item")
        val w = FeedbackEvent.WithUndo("m", { }, target = t, supportingText = "s")
        assertEquals(t, w.target)
        assertEquals("s", w.supportingText)
        val copied = w.copy(message = "x")
        assertEquals("x", copied.message)
        assertEquals(t, copied.target)
        assertEquals("s", copied.supportingText)
    }

    // ---- (e) destructuring and DismissScreen are unchanged ----

    @Test
    fun destructuring_firstComponentsUnchanged() {
        val notify = FeedbackEvent.Notify("m", SnackbarDuration.Indefinite)
        val (m, d) = notify
        assertEquals("m", m)
        assertEquals(SnackbarDuration.Indefinite, d)
    }

    @Test
    fun dismissScreen_declaresOnlyMessage() {
        val fields = FeedbackEvent.DismissScreen::class.java.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) }
        assertNotNull(fields)
        assertEquals(listOf("message"), fields.map { it.name })
    }
}
