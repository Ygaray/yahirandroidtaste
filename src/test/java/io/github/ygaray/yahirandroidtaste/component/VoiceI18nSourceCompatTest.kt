package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.Modifier
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import org.junit.Assert.assertEquals
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
}
