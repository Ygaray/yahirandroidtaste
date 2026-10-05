package io.github.ygaray.yahirandroidtaste.model

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.jvm.internal.DefaultConstructorMarker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier as JModifier

/**
 * Binary-level evidence for INC-2026-10-05-02 F1c: each data class's v2.4.1 compiler-generated
 * default-argument synthetics exist with their exact JVM descriptors.
 *
 * A consumer compiled against v2.4.x calls `<init>(<v2.4.1 params>, int, DefaultConstructorMarker)`
 * or the static `copy$default(self, <v2.4.1 params>, int, Object)` when it omits defaulted
 * arguments. These tests resolve that exact descriptor reflectively and invoke it with a v2.4-style
 * mask (null or false in each defaulted slot), which is what a v2.4-compiled caller executes.
 *
 * Each test also passes deliberately WRONG values in some masked slots: the compiler-generated
 * synthetic ignores a slot whose mask bit is set, so the hand-written shim must too. That pins the
 * bit-to-parameter mapping, not just the all-defaults path.
 *
 * Deliberately no overload-count or exact-arity assertions: Phase 17 may add members.
 */
class DataClassBinaryCompatShimTest {

    private fun defaultCtor(cls: Class<*>, vararg v241Params: Class<*>): Constructor<*> {
        val ctor = cls.getDeclaredConstructor(
            *v241Params,
            Int::class.javaPrimitiveType,
            DefaultConstructorMarker::class.java
        )
        assertTrue(JModifier.isPublic(ctor.modifiers))
        return ctor
    }

    private fun copyDefault(cls: Class<*>, vararg v241Params: Class<*>): Method {
        val method = cls.getMethod(
            "copy\$default",
            cls,
            *v241Params,
            Int::class.javaPrimitiveType,
            Any::class.java
        )
        assertTrue(JModifier.isPublic(method.modifiers))
        assertTrue(JModifier.isStatic(method.modifiers))
        return method
    }

    @Test
    fun handledByUiModel_v241DefaultCtorAndCopyDefault_behaveLikeV241() {
        val str = String::class.java
        val int = Int::class.javaObjectType

        val ctor = defaultCtor(HandledByUiModel::class.java, str, str, str, str, int)
        val allDefaults = ctor.newInstance("Local", null, null, null, null, 0b11110, null) as HandledByUiModel
        assertEquals(HandledByUiModel("Local"), allDefaults)
        assertEquals("Escalations:", allDefaults.escalationsLabel)
        assertEquals(
            HandledByUiModel("Cloud", "rag", "openai", "gpt-4", 2),
            ctor.newInstance("Cloud", "rag", "openai", "gpt-4", 2, 0, null)
        )
        // Mixed mask: bits 2 and 4 set, so the slot values passed there are ignored.
        val mixed = ctor.newInstance("Cloud", "rag", "IGNORED", "gpt-4", 99, 0b10100, null) as HandledByUiModel
        assertEquals(HandledByUiModel("Cloud", "rag", null, "gpt-4", null), mixed)
        assertEquals("Escalations:", mixed.escalationsLabel)

        val copy = copyDefault(HandledByUiModel::class.java, str, str, str, str, int)
        val original = HandledByUiModel("Local", "rag", "openai", "gpt-4", 3, "Escaladas:")
        val tierOnly = copy.invoke(null, original, "Cloud", null, null, null, null, 0b11110, null) as HandledByUiModel
        assertEquals(original.copy(tier = "Cloud"), tierOnly)
        assertEquals("Escaladas:", tierOnly.escalationsLabel)
        assertEquals(original, copy.invoke(null, original, null, null, null, null, null, 0b11111, null))
        // Mixed mask: bits 0, 2 and 4 keep the receiver's values; bits 1 and 3 bind (a null binds as null).
        val mixedCopy = copy.invoke(
            null, original, "IGNORED", "bm25", "IGNORED", null, 42, 0b10101, null
        ) as HandledByUiModel
        assertEquals(original.copy(approach = "bm25", model = null), mixedCopy)
        assertEquals("Escaladas:", mixedCopy.escalationsLabel)

        // Source-shape pins (question 3): these call shapes must resolve without ambiguity.
        assertEquals(HandledByUiModel("a"), HandledByUiModel("a", null, null, null, null))
        assertEquals("m", original.copy(tier = "m").tier)
        assertEquals(original, original.copy())
    }
}
