package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Compile-only evidence behind success criterion 5 of Phase 15 (VI18N-01..04): the new label
 * parameters were appended AFTER the former last parameter of each composable and AFTER the former
 * last property of each model, so every v2.4.0 call shape still binds the same values and renders
 * the English defaults.
 *
 * Mirrors [ShowTagColorsSourceCompatTest]: the composable calls live inside non-invoked
 * `@Composable () -> Unit` lambdas (they only have to COMPILE), while the model shapes are plain
 * JVM objects with runtime assertions.
 *
 * This file is also the compile-only evidence behind success criterion 5 of Phase 16 (VFAIL-01..03):
 * `VoiceOutcomeUiState.Failure` and `FailureActionUiModel` gained fields appended after the former
 * last property, so every v2.4 call shape (positional, named, destructuring, legacy `copy`) binds
 * the same values and carries the defaults.
 *
 * It is also the v2.4.0 trailing-lambda evidence for INC-2026-10-05-02 F1b: the
 * `FailureActionUiModel("l") { }` shape still binds the lambda to `onClick` (not `role`).
 */
class VoiceI18nSourceCompatTest {

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun v240PositionalComposableCallShapes_compileAgainstV25Signatures() {
        // Fully positional through the former last parameter (emptyProvidersReason).
        val providerKeyCard: @androidx.compose.runtime.Composable () -> Unit = {
            ProviderKeyCard(
                emptyList(),
                null,
                { _: String -> },
                "",
                { _: String -> },
                KeyFieldState.Empty,
                "API key",
                Modifier,
                "No providers"
            )
        }

        // Positional through `modifier`.
        val modelSelectCard: @androidx.compose.runtime.Composable () -> Unit = {
            ModelSelectCard(emptyList(), null, { _: String -> }, "No models", Modifier)
        }

        val clarificationBar: @androidx.compose.runtime.Composable () -> Unit = {
            ClarificationBar("Which list?", emptyList(), { _: String -> }, {}, Modifier)
        }

        val approachLadderCard: @androidx.compose.runtime.Composable () -> Unit = {
            ApproachLadderCard(
                emptyList(),
                false,
                { _: Boolean -> },
                null,
                { _: String -> },
                Modifier
            )
        }

        // Phase 16 (SC5): a v2.4-shaped Failure (two positional arguments) positionally through
        // `modifier`.
        val outcomeSheet: @androidx.compose.runtime.Composable () -> Unit = {
            OutcomeSheet(VoiceOutcomeUiState.Failure("r", null), {}, Modifier)
        }

        // Compile-only: the lambdas above are intentionally never invoked. The test's value is that
        // these v2.4.0 call shapes still COMPILE; asserting non-null on a just-assigned lambda would
        // be vacuous. Runtime default-label rendering is covered by the per-component tests.
    }

    @Test
    fun v240ModelShapes_compileAndCarryEnglishDefaults() {
        val row = UndoRowUiModel("1", "l", UndoRowState.Undone)
        val (id, label, state) = row
        assertEquals("1", id)
        assertEquals("l", label)
        assertEquals(UndoRowState.Undone, state)
        assertEquals("Undone", row.undoneLabel)
        assertEquals("Undone", row.copy("2", "m", UndoRowState.Undone).undoneLabel)

        val refusedOne = UndoRefusedUiModel("r")
        val refusedTwo = UndoRefusedUiModel("r", "item")
        val refusedCopy = refusedTwo.copy("r2", null)
        listOf(refusedOne, refusedTwo, refusedCopy).forEach {
            assertEquals("Couldn't undo:", it.refusedPrefix)
            assertEquals("changed since", it.changedSinceSuffix)
        }

        val handledBy = HandledByUiModel("Local", null, null, null, 2)
        assertEquals("Escalations:", handledBy.escalationsLabel)
        assertEquals("Escalations:", handledBy.copy("Cloud", null, null, null, 3).escalationsLabel)

        val item = ProposedItemUiModel("1", "t", null, null, false, null, null)
        assertEquals("Remove", item.removeContentDescription)
        assertEquals("Remove", item.copy("2", "u", null, null, true, null, null).removeContentDescription)
    }

    @Test
    fun v24FailureAndActionShapes_compileAndCarryDefaults() {
        // Positional / named construction at every v2.4 arity binds the same values.
        val one = VoiceOutcomeUiState.Failure("r")
        val two = VoiceOutcomeUiState.Failure("r", null)
        val three = VoiceOutcomeUiState.Failure("r", null, null)
        val named = VoiceOutcomeUiState.Failure(reason = "r", action = null)
        listOf(one, two, three, named).forEach {
            assertEquals("r", it.reason)
            assertNull(it.handledBy)
            assertNull(it.action)
            assertNull(it.body)
            assertNull(it.semanticsPrefix)
        }

        // Positional destructuring of the first three components still compiles and yields the
        // original values (the appended components are component4/component5).
        val handledBy = HandledByUiModel("Local")
        val action = FailureActionUiModel("l", {})
        val (reason, destructuredHandledBy, destructuredAction) =
            VoiceOutcomeUiState.Failure("r", handledBy, action)
        assertEquals("r", reason)
        assertSame(handledBy, destructuredHandledBy)
        assertSame(action, destructuredAction)

        // Legacy three-argument copy: a plain Failure keeps null body/prefix; one with a custom body
        // (ONE lambda instance) and prefix preserves both.
        val plainCopy = three.copy("r2", handledBy, action)
        assertNull(plainCopy.body)
        assertNull(plainCopy.semanticsPrefix)
        val customBody: @androidx.compose.runtime.Composable () -> Unit = {}
        val custom = VoiceOutcomeUiState.Failure("r", null, null, customBody, "Error:")
        val customCopy = custom.copy("r3", handledBy, action)
        assertEquals("r3", customCopy.reason)
        assertSame(customBody, customCopy.body)
        assertEquals("Error:", customCopy.semanticsPrefix)

        // FailureActionUiModel: two-argument construction defaults the role to Neutral.
        val plainAction = FailureActionUiModel("l", {})
        assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, plainAction.role)
        val (label, onClick) = plainAction
        assertEquals("l", label)
        assertSame(plainAction.onClick, onClick)

        // Legacy two-argument copy keeps Neutral, and keeps a custom role.
        assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, plainAction.copy("m", {}).role)
        val destructive = FailureActionUiModel("l", {}, ActionButtonDefaults.ActionButtonRole.Destructive)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Destructive, destructive.copy("m", {}).role)
    }

    @Test
    fun v240TrailingLambdaActionShape_compilesAndDefaultsNeutral() {
        // v2.4.0 trailing-lambda shape: the lambda must bind to onClick, and role stays Neutral.
        var clicked = false
        val trailing = FailureActionUiModel("l") { clicked = true }
        assertEquals("l", trailing.label)
        assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, trailing.role)
        trailing.onClick()
        assertTrue("Trailing lambda must be the onClick callback", clicked)

        // Named label plus trailing lambda.
        val namedTrailing = FailureActionUiModel(label = "l") { }
        assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, namedTrailing.role)

        // Two positional / two named arguments keep Neutral; an explicit role is kept.
        assertEquals(ActionButtonDefaults.ActionButtonRole.Neutral, FailureActionUiModel("l", {}).role)
        assertEquals(
            ActionButtonDefaults.ActionButtonRole.Neutral,
            FailureActionUiModel(label = "l", onClick = {}).role
        )
        assertEquals(
            ActionButtonDefaults.ActionButtonRole.Destructive,
            FailureActionUiModel("l", {}, ActionButtonDefaults.ActionButtonRole.Destructive).role
        )

        // JVM: the v2.4.1 (Ljava/lang/String;Lkotlin/jvm/functions/Function0;)V constructor persists.
        FailureActionUiModel::class.java.getConstructor(String::class.java, Function0::class.java)
    }
}
