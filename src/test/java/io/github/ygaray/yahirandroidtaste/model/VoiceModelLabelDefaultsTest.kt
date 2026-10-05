package io.github.ygaray.yahirandroidtaste.model

import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM (no Robolectric) pins for the v2.5 label-field defaults and the old-arity
 * compatibility of the voice models (Phase 15 / VI18N-04). Each model gained caller-localizable
 * `String` fields appended LAST with English defaults; the shipped v2.4.0 constructor and `copy`
 * arities must survive (`@JvmOverloads constructor` + a hand-written old-arity `copy`), which the
 * reflection tests below pin. Plan 15-03 extends this file for the remaining two models, and
 * Phase 16 (plan 16-02) extends it for the Failure models (`FailureActionUiModel.role`,
 * `VoiceOutcomeUiState.Failure.body` / `semanticsPrefix`), which follow the same recipe.
 */
class VoiceModelLabelDefaultsTest {

    private fun constructorArities(cls: Class<*>): Set<Int> =
        cls.constructors.map { it.parameterCount }.toSet()

    private fun copyArities(cls: Class<*>): Set<Int> =
        cls.declaredMethods
            .filter { it.name == "copy" && !it.isSynthetic }
            .map { it.parameterCount }
            .toSet()

    /**
     * Drift guard (IN-02): invokes the SHORTEST non-synthetic `copy` (the hand-written legacy arity)
     * with the instance's own leading component values and asserts the result still equals the
     * original. [original] must have EVERY field set to a non-default value, so a legacy copy that
     * resets or drops a (later-appended) field makes the equality fail.
     */
    private fun assertLegacyCopyCarriesEveryField(original: Any) {
        val cls = original.javaClass
        val legacyCopy = cls.declaredMethods
            .filter { it.name == "copy" && !it.isSynthetic }
            .minByOrNull { it.parameterCount }!!
        val args = (1..legacyCopy.parameterCount).map { n ->
            cls.getMethod("component$n").invoke(original)
        }.toTypedArray()

        val copied = legacyCopy.invoke(original, *args)

        assertEquals(original, copied)
    }

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

    // ---- UndoRefusedUiModel.refusedPrefix / changedSinceSuffix ----

    @Test
    fun `UndoRefusedUiModel label fields default to the English fragments`() {
        val refused = UndoRefusedUiModel("r")

        assertEquals("Couldn't undo:", refused.refusedPrefix)
        assertEquals("changed since", refused.changedSinceSuffix)
    }

    @Test
    fun `UndoRefusedUiModel v2_4_0 one and two argument construction shapes carry the defaults`() {
        val one = UndoRefusedUiModel("r")
        val two = UndoRefusedUiModel("r", "item")

        assertEquals("Couldn't undo:", one.refusedPrefix)
        assertEquals("changed since", two.changedSinceSuffix)
        assertEquals("item", two.changedItem)
    }

    @Test
    fun `UndoRefusedUiModel legacy two-argument copy preserves custom label fields`() {
        val refused = UndoRefusedUiModel(
            "r",
            "item",
            refusedPrefix = "Impossible d'annuler :",
            changedSinceSuffix = "modifié depuis"
        )

        val copied = refused.copy("r2", null)

        assertEquals("Impossible d'annuler :", copied.refusedPrefix)
        assertEquals("modifié depuis", copied.changedSinceSuffix)
        assertEquals("r2", copied.reason)
    }

    @Test
    fun `UndoRefusedUiModel keeps the old two-arg and the new four-arg constructor and copy arities`() {
        assertTrue(constructorArities(UndoRefusedUiModel::class.java).containsAll(listOf(2, 4)))
        assertTrue(copyArities(UndoRefusedUiModel::class.java).containsAll(listOf(2, 4)))
    }

    // ---- HandledByUiModel.escalationsLabel ----

    @Test
    fun `HandledByUiModel escalationsLabel defaults to the English Escalations colon`() {
        assertEquals("Escalations:", HandledByUiModel("Local").escalationsLabel)
    }

    @Test
    fun `HandledByUiModel v2_4_0 construction shapes carry the default label`() {
        val one = HandledByUiModel("Local")
        val named = HandledByUiModel(tier = "Local", escalationCount = 3)
        val full = HandledByUiModel("Local", "Hybrid", "OpenAI", "gpt-4", 2)

        assertEquals("Escalations:", one.escalationsLabel)
        assertEquals("Escalations:", named.escalationsLabel)
        assertEquals("Escalations:", full.escalationsLabel)
        assertEquals(3, named.escalationCount)
    }

    @Test
    fun `HandledByUiModel legacy five-argument copy preserves a custom escalationsLabel`() {
        val handledBy = HandledByUiModel("Local", null, null, null, 1, escalationsLabel = "Escalades :")

        val copied = handledBy.copy("Cloud", "A", "P", "M", 4)

        assertEquals("Escalades :", copied.escalationsLabel)
        assertEquals("Cloud", copied.tier)
        assertEquals(4, copied.escalationCount)
    }

    @Test
    fun `HandledByUiModel keeps the old five-arg and the new six-arg constructor and copy arities`() {
        assertTrue(constructorArities(HandledByUiModel::class.java).containsAll(listOf(5, 6)))
        assertTrue(copyArities(HandledByUiModel::class.java).containsAll(listOf(5, 6)))
    }

    // ---- ProposedItemUiModel.removeContentDescription ----

    @Test
    fun `ProposedItemUiModel removeContentDescription defaults to the English word Remove`() {
        assertEquals("Remove", ProposedItemUiModel(id = "1", title = "t").removeContentDescription)
    }

    @Test
    fun `ProposedItemUiModel legacy seven-argument copy preserves a custom removeContentDescription`() {
        val item = ProposedItemUiModel(id = "1", title = "t", removeContentDescription = "Supprimer")

        val copied = item.copy("2", "u", null, null, true, null, null)

        assertEquals("Supprimer", copied.removeContentDescription)
        assertEquals("2", copied.id)
        assertEquals(true, copied.amended)
    }

    @Test
    fun `ProposedItemUiModel toString with a custom removeContentDescription still prints only id and amended`() {
        val item = ProposedItemUiModel(
            id = "x",
            title = "Secret title",
            subtitle = "Secret subtitle",
            confidenceCue = "Secret cue",
            removeContentDescription = "Supprimer"
        )

        assertEquals("ProposedItemUiModel(id=x, amended=false)", item.toString())
    }

    @Test
    fun `ProposedItemUiModel keeps the old seven-arg and the new eight-arg constructor and copy arities`() {
        assertTrue(constructorArities(ProposedItemUiModel::class.java).containsAll(listOf(7, 8)))
        assertTrue(copyArities(ProposedItemUiModel::class.java).containsAll(listOf(7, 8)))
    }

    // ---- FailureActionUiModel.role (Phase 16 / VFAIL-01) ----

    @Test
    fun `FailureActionUiModel role defaults to Neutral for the v2_4 two-argument shape`() {
        val action = FailureActionUiModel("l", {})

        assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, action.role)
    }

    @Test
    fun `FailureActionUiModel carries an explicit role`() {
        val action = FailureActionUiModel("l", {}, ActionButtonDefaults.ActionButtonRole.Destructive)

        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, action.role)
    }

    @Test
    fun `FailureActionUiModel legacy two-argument copy preserves a custom role`() {
        val action = FailureActionUiModel("l", {}, role = ActionButtonDefaults.ActionButtonRole.Destructive)

        val copied = action.copy("m", {})

        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, copied.role)
        assertEquals("m", copied.label)
    }

    @Test
    fun `FailureActionUiModel partial named copy preserves a custom role`() {
        val action = FailureActionUiModel("l", {}, role = ActionButtonDefaults.ActionButtonRole.Save)

        val copied = action.copy(label = "x")

        assertEquals(ActionButtonDefaults.ActionButtonRole.Save, copied.role)
        assertEquals("x", copied.label)
    }

    @Test
    fun `FailureActionUiModel models differing only in role are not equal`() {
        val onClick: () -> Unit = {}
        val a = FailureActionUiModel("l", onClick)
        val b = FailureActionUiModel("l", onClick, ActionButtonDefaults.ActionButtonRole.Destructive)

        assertNotEquals(a, b)
    }

    @Test
    fun `FailureActionUiModel keeps the old two-arg and the new three-arg constructor and copy arities`() {
        assertTrue(constructorArities(FailureActionUiModel::class.java).containsAll(listOf(2, 3)))
        assertTrue(copyArities(FailureActionUiModel::class.java).containsAll(listOf(2, 3)))
    }

    // ---- IN-02: legacy-arity copy drift guards (every field non-default) ----

    @Test
    fun `legacy-arity copy of every voice model carries every field forward`() {
        val onRemove: () -> Unit = {}
        val trailing: @androidx.compose.runtime.Composable () -> Unit = {}
        val failureOnClick: () -> Unit = {}

        assertLegacyCopyCarriesEveryField(
            HandledByUiModel("Cloud", "ladder", "prov", "mod", 3, escalationsLabel = "Escalades :")
        )
        assertLegacyCopyCarriesEveryField(
            ProposedItemUiModel(
                id = "1",
                title = "t",
                subtitle = "s",
                confidenceCue = "c",
                amended = true,
                onRemove = onRemove,
                trailingContent = trailing,
                removeContentDescription = "Supprimer"
            )
        )
        assertLegacyCopyCarriesEveryField(
            UndoRefusedUiModel("r", "item", refusedPrefix = "Impossible :", changedSinceSuffix = "modifié")
        )
        assertLegacyCopyCarriesEveryField(
            UndoRowUiModel("1", "l", UndoRowState.Undone, undoneLabel = "Annulé")
        )
        assertLegacyCopyCarriesEveryField(
            FailureActionUiModel("l", failureOnClick, role = ActionButtonDefaults.ActionButtonRole.Destructive)
        )
    }
}
