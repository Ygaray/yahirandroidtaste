package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Composable
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.jvm.internal.DefaultConstructorMarker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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

    @Test
    fun proposedItemUiModel_v241DefaultCtorAndCopyDefault_behaveLikeV241() {
        val str = String::class.java
        val bool = Boolean::class.javaPrimitiveType!!
        val fn0 = Function0::class.java
        val fn2 = Function2::class.java
        val onRemove: () -> Unit = {}
        val otherOnRemove: () -> Unit = {}
        val trailing: @Composable () -> Unit = {}
        val otherTrailing: @Composable () -> Unit = {}

        val ctor = defaultCtor(ProposedItemUiModel::class.java, str, str, str, str, bool, fn0, fn2)
        val allDefaults = ctor.newInstance(
            "i", "t", null, null, false, null, null, 0b1111100, null
        ) as ProposedItemUiModel
        assertEquals(ProposedItemUiModel("i", "t"), allDefaults)
        assertEquals("Remove", allDefaults.removeContentDescription)

        val explicit = ctor.newInstance("i", "t", "s", "c", true, onRemove, null, 0, null) as ProposedItemUiModel
        assertEquals("i", explicit.id)
        assertEquals("t", explicit.title)
        assertEquals("s", explicit.subtitle)
        assertEquals("c", explicit.confidenceCue)
        assertEquals(true, explicit.amended)
        assertSame(onRemove, explicit.onRemove)
        assertNull(explicit.trailingContent)
        assertEquals("Remove", explicit.removeContentDescription)

        // Mixed mask: bits 3, 4 and 6 set, so the values passed there (including a primitive true) are ignored.
        val mixed = ctor.newInstance(
            "i", "t", "s", "IGNORED", true, onRemove, trailing, 0b1011000, null
        ) as ProposedItemUiModel
        assertEquals(ProposedItemUiModel("i", "t", "s", null, false, onRemove, null), mixed)
        assertEquals("Remove", mixed.removeContentDescription)

        val copy = copyDefault(ProposedItemUiModel::class.java, str, str, str, str, bool, fn0, fn2)
        val original = ProposedItemUiModel(
            id = "1",
            title = "t",
            subtitle = "s",
            confidenceCue = "c",
            amended = true,
            onRemove = onRemove,
            trailingContent = trailing,
            removeContentDescription = "Quitar"
        )
        val titleOnly = copy.invoke(
            null, original, null, "T2", null, null, false, null, null, 0b1111101, null
        ) as ProposedItemUiModel
        assertEquals(original.copy(title = "T2"), titleOnly)
        assertEquals("Quitar", titleOnly.removeContentDescription)
        assertEquals(original, copy.invoke(null, original, null, null, null, null, false, null, null, 0b1111111, null))
        // Mixed mask: bits 1, 2 and 5 keep the receiver's values; the rest bind (null and false bind as given).
        val mixedCopy = copy.invoke(
            null, original, "id2", "IGNORED", "IGNORED", null, false, otherOnRemove, otherTrailing, 0b0100110, null
        ) as ProposedItemUiModel
        assertEquals(
            original.copy(id = "id2", confidenceCue = null, amended = false, trailingContent = otherTrailing),
            mixedCopy
        )
        assertSame(onRemove, mixedCopy.onRemove)
        assertEquals("Quitar", mixedCopy.removeContentDescription)

        // Source-shape pins: these call shapes must resolve without ambiguity.
        assertEquals(ProposedItemUiModel("i", "t"), ProposedItemUiModel("i", "t", null, null, false, null, null))
        assertEquals("T3", original.copy(title = "T3").title)
        assertEquals(original, original.copy())
    }

    @Test
    fun undoRefusedUiModel_v241DefaultCtorAndCopyDefault_behaveLikeV241() {
        val str = String::class.java

        val ctor = defaultCtor(UndoRefusedUiModel::class.java, str, str)
        val allDefaults = ctor.newInstance("why", null, 0b10, null) as UndoRefusedUiModel
        assertEquals(UndoRefusedUiModel("why"), allDefaults)
        assertEquals("Couldn't undo:", allDefaults.refusedPrefix)
        assertEquals("changed since", allDefaults.changedSinceSuffix)
        val explicit = ctor.newInstance("why", "Card", 0, null) as UndoRefusedUiModel
        assertEquals("why", explicit.reason)
        assertEquals("Card", explicit.changedItem)
        assertEquals("Couldn't undo:", explicit.refusedPrefix)
        assertEquals("changed since", explicit.changedSinceSuffix)
        // Mixed mask: bit 1 set, so the value passed there is ignored.
        assertEquals(UndoRefusedUiModel("why"), ctor.newInstance("why", "IGNORED", 0b10, null))

        val copy = copyDefault(UndoRefusedUiModel::class.java, str, str)
        val original = UndoRefusedUiModel("why", "Card", "No se pudo:", "cambió")
        val reasonOnly = copy.invoke(null, original, "new", null, 0b10, null) as UndoRefusedUiModel
        assertEquals(original.copy(reason = "new"), reasonOnly)
        assertEquals("No se pudo:", reasonOnly.refusedPrefix)
        assertEquals("cambió", reasonOnly.changedSinceSuffix)
        assertEquals(original, copy.invoke(null, original, null, null, 0b11, null))
        // Mixed mask: bit 0 keeps the receiver's reason; bit 1 is clear, so an explicit null binds.
        val mixedCopy = copy.invoke(null, original, "IGNORED", null, 0b01, null) as UndoRefusedUiModel
        assertEquals(original.copy(changedItem = null), mixedCopy)
        assertEquals("No se pudo:", mixedCopy.refusedPrefix)

        // Source-shape pins: these call shapes must resolve without ambiguity.
        assertEquals(UndoRefusedUiModel("why"), UndoRefusedUiModel("why", null))
        assertEquals("new", original.copy(reason = "new").reason)
        assertEquals(original, original.copy())
    }

    @Test
    fun failure_v241DefaultCtorAndCopyDefault_behaveLikeV241() {
        val str = String::class.java
        val handledByCls = HandledByUiModel::class.java
        val actionCls = FailureActionUiModel::class.java
        val onClick: () -> Unit = {}
        val body: @Composable () -> Unit = {}
        val handledBy = HandledByUiModel("Local")
        val action = FailureActionUiModel("Retry", onClick, ActionButtonDefaults.ActionButtonRole.Destructive)

        val ctor = defaultCtor(VoiceOutcomeUiState.Failure::class.java, str, handledByCls, actionCls)
        val allDefaults = ctor.newInstance("r", null, null, 0b110, null) as VoiceOutcomeUiState.Failure
        assertEquals(VoiceOutcomeUiState.Failure("r"), allDefaults)
        assertNull(allDefaults.body)
        assertNull(allDefaults.semanticsPrefix)
        val explicit = ctor.newInstance("r", handledBy, action, 0, null) as VoiceOutcomeUiState.Failure
        assertEquals("r", explicit.reason)
        assertEquals(handledBy, explicit.handledBy)
        assertSame(action, explicit.action)
        assertNull(explicit.body)
        assertNull(explicit.semanticsPrefix)
        // Mixed mask: bit 1 set, so the handledBy passed there is ignored; action binds.
        val mixed = ctor.newInstance("r", HandledByUiModel("IGNORED"), action, 0b010, null) as VoiceOutcomeUiState.Failure
        assertEquals(VoiceOutcomeUiState.Failure("r", null, action), mixed)

        val copy = copyDefault(VoiceOutcomeUiState.Failure::class.java, str, handledByCls, actionCls)
        val original = VoiceOutcomeUiState.Failure(
            reason = "r",
            handledBy = handledBy,
            action = action,
            body = body,
            semanticsPrefix = "Error:"
        )
        val reasonOnly = copy.invoke(null, original, "r2", null, null, 0b110, null) as VoiceOutcomeUiState.Failure
        assertEquals(original.copy(reason = "r2"), reasonOnly)
        assertSame(body, reasonOnly.body)
        assertEquals("Error:", reasonOnly.semanticsPrefix)
        assertEquals(original, copy.invoke(null, original, null, null, null, 0b111, null))
        // Mixed mask: bits 0 and 2 keep the receiver's values; bit 1 binds the new handledBy.
        val otherAction = FailureActionUiModel("IGNORED", onClick)
        val mixedCopy = copy.invoke(
            null, original, "IGNORED", HandledByUiModel("Cloud"), otherAction, 0b101, null
        ) as VoiceOutcomeUiState.Failure
        assertEquals(original.copy(handledBy = HandledByUiModel("Cloud")), mixedCopy)
        assertSame(action, mixedCopy.action)
        assertSame(body, mixedCopy.body)
        assertEquals("Error:", mixedCopy.semanticsPrefix)

        // Source-shape pins: these call shapes must resolve without ambiguity.
        assertEquals(VoiceOutcomeUiState.Failure("r"), VoiceOutcomeUiState.Failure("r", null, null))
        assertEquals("r3", original.copy(reason = "r3").reason)
        assertEquals(original, original.copy())
    }

    // UndoRowUiModel and FailureActionUiModel had no defaulted parameter in v2.4.1, so no
    // DefaultConstructorMarker constructor shipped for them; only copy$default needs restoring.

    @Test
    fun undoRowUiModel_v241CopyDefault_behavesLikeV241() {
        val str = String::class.java
        val stateCls = UndoRowState::class.java

        val copy = copyDefault(UndoRowUiModel::class.java, str, str, stateCls)
        val original = UndoRowUiModel("1", "Card", UndoRowState.Undone, "Deshecho")
        val idOnly = copy.invoke(null, original, "2", null, null, 0b110, null) as UndoRowUiModel
        assertEquals(original.copy(id = "2"), idOnly)
        assertEquals("Deshecho", idOnly.undoneLabel)
        assertEquals(original, copy.invoke(null, original, null, null, null, 0b111, null))
        // Mixed mask: bits 0 and 1 keep the receiver's values; bit 2 binds the new state.
        val newState = UndoRowState.Unavailable("entangled")
        val mixedCopy = copy.invoke(null, original, "IGNORED", "IGNORED", newState, 0b011, null) as UndoRowUiModel
        assertEquals(original.copy(state = newState), mixedCopy)
        assertEquals("Deshecho", mixedCopy.undoneLabel)

        // Source-shape pins: these call shapes must resolve without ambiguity.
        assertEquals(
            UndoRowUiModel("1", "Card", UndoRowState.Undone, "Undone"),
            UndoRowUiModel("1", "Card", UndoRowState.Undone)
        )
        assertEquals("3", original.copy(id = "3").id)
        assertEquals(original, original.copy())
    }

    @Test
    fun failureActionUiModel_v241CopyDefault_behavesLikeV241() {
        val str = String::class.java
        val fn0 = Function0::class.java
        val cb: () -> Unit = {}
        val otherCb: () -> Unit = {}

        val copy = copyDefault(FailureActionUiModel::class.java, str, fn0)
        val original = FailureActionUiModel("l", cb, ActionButtonDefaults.ActionButtonRole.Destructive)
        val labelOnly = copy.invoke(null, original, "m", null, 0b10, null) as FailureActionUiModel
        assertEquals(original.copy(label = "m"), labelOnly)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, labelOnly.role)
        assertSame(cb, labelOnly.onClick)
        assertEquals(original, copy.invoke(null, original, null, null, 0b11, null))
        // Mixed mask: bit 0 keeps the receiver's label; bit 1 binds the new callback.
        val mixedCopy = copy.invoke(null, original, "IGNORED", otherCb, 0b01, null) as FailureActionUiModel
        assertEquals("l", mixedCopy.label)
        assertSame(otherCb, mixedCopy.onClick)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, mixedCopy.role)

        // Source-shape pins: these call shapes must resolve without ambiguity.
        assertEquals("m", original.copy(label = "m").label)
        assertEquals(original, original.copy())
    }
}
