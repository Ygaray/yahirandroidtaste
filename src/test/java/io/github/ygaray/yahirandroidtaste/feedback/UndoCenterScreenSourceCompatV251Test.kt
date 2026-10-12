package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.Composable
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Compile-only canary for the v2.5.1 call shapes of [UndoCenterScreen] (VUNDO-02), plus the v2.6
 * shapes appended in Task 4. The lambdas are never invoked; the file only has to compile, which is
 * the whole proof. The positional
 * four-argument shape `UndoCenterScreen(e, b, u, c)` is deliberately NOT part of the canary: it no
 * longer compiles once `groups` and `onUndoGroup` sit before the trailing `onClear` (accepted,
 * owner-flagged consequence of the mandated parameter placement).
 */
class UndoCenterScreenSourceCompatV251Test {

    @Suppress("unused")
    private val v251AllNamedCallShape_compiles: @Composable () -> Unit = {
        UndoCenterScreen(
            entries = emptyList(),
            onNavigateBack = {},
            onUndo = { _ -> },
            onClear = {}
        )
    }

    @Suppress("unused")
    private val v251TrailingLambdaCallShape_compiles: @Composable () -> Unit = {
        UndoCenterScreen(
            entries = emptyList(),
            onNavigateBack = {},
            onUndo = { _ -> }
        ) {}
    }

    @Suppress("unused")
    private val v260AllNamedGroupsShape_compiles: @Composable () -> Unit = {
        UndoCenterScreen(
            entries = emptyList(),
            onNavigateBack = {},
            onUndo = { _ -> },
            groups = emptyList(),
            onUndoGroup = { _ -> },
            onClear = {}
        )
    }

    @Suppress("unused")
    private val v260TrailingLambdaWithNamedGroups_compiles: @Composable () -> Unit = {
        UndoCenterScreen(
            entries = emptyList(),
            onNavigateBack = {},
            onUndo = { _ -> },
            groups = emptyList(),
            onUndoGroup = null
        ) {}
    }

    @Test
    fun v251CallShapesCompile() {
        // The two properties above are the proof; reaching here means they compiled.
    }

    @Test
    fun undoGroupRowUiModel_defaultsAndShape() {
        val model = UndoGroupRowUiModel(
            groupId = "g",
            label = "Batch",
            memberIds = listOf("a", "b"),
            status = UndoGroupStatus.Undoable
        )
        assertEquals("Undo all", model.undoAllLabel)
        assertEquals(
            model,
            UndoGroupRowUiModel("g", "Batch", listOf("a", "b"), UndoGroupStatus.Undoable, "Undo all")
        )
        val (groupId, label, memberIds, status, undoAllLabel) = model
        assertEquals("g", groupId)
        assertEquals("Batch", label)
        assertEquals(listOf("a", "b"), memberIds)
        assertEquals(UndoGroupStatus.Undoable, status)
        assertEquals("Undo all", undoAllLabel)
    }
}
