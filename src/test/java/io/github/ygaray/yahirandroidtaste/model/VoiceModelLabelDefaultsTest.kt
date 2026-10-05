package io.github.ygaray.yahirandroidtaste.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM (no Robolectric) pins for the v2.5 label-field defaults and the old-arity
 * compatibility of the voice models (Phase 15 / VI18N-04). Each model gained caller-localizable
 * `String` fields appended LAST with English defaults; the shipped v2.4.0 constructor and `copy`
 * arities must survive (`@JvmOverloads constructor` + a hand-written old-arity `copy`), which the
 * reflection tests below pin. Plan 15-03 extends this file for the remaining two models.
 */
class VoiceModelLabelDefaultsTest {

    private fun constructorArities(cls: Class<*>): Set<Int> =
        cls.constructors.map { it.parameterCount }.toSet()

    private fun copyArities(cls: Class<*>): Set<Int> =
        cls.declaredMethods
            .filter { it.name == "copy" && !it.isSynthetic }
            .map { it.parameterCount }
            .toSet()

    // ---- UndoRowUiModel.undoneLabel ----

    @Test
    fun `UndoRowUiModel undoneLabel defaults to the English word Undone`() {
        val row = UndoRowUiModel(id = "1", label = "Card", state = UndoRowState.Undone)

        assertEquals("Undone", row.undoneLabel)
    }

    @Test
    fun `UndoRowUiModel legacy three-argument copy preserves a custom undoneLabel`() {
        val row = UndoRowUiModel("1", "Card", UndoRowState.Undone, undoneLabel = "Annulé")

        val copied = row.copy("2", "Other", UndoRowState.Undone)

        assertEquals("Annulé", copied.undoneLabel)
        assertEquals("2", copied.id)
    }

    @Test
    fun `UndoRowUiModel partial named copy preserves a custom undoneLabel`() {
        val row = UndoRowUiModel("1", "Card", UndoRowState.Undone, undoneLabel = "Annulé")

        val copied = row.copy(label = "x")

        assertEquals("Annulé", copied.undoneLabel)
        assertEquals("x", copied.label)
    }

    @Test
    fun `UndoRowUiModel rows differing only in undoneLabel are not equal`() {
        val a = UndoRowUiModel("1", "Card", UndoRowState.Undone)
        val b = UndoRowUiModel("1", "Card", UndoRowState.Undone, undoneLabel = "Annulé")

        assertNotEquals(a, b)
    }

    @Test
    fun `UndoRowUiModel keeps the old three-arg and the new four-arg constructor and copy arities`() {
        assertTrue(constructorArities(UndoRowUiModel::class.java).containsAll(listOf(3, 4)))
        assertTrue(copyArities(UndoRowUiModel::class.java).containsAll(listOf(3, 4)))
    }
}
