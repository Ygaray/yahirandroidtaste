package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.Composable
import org.junit.Test

/**
 * Compile-only canary for the v2.5.1 call shapes of [UndoCenterScreen] (VUNDO-02). The lambdas are
 * never invoked; the file only has to compile, which is the whole proof. The positional
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

    @Test
    fun v251CallShapesCompile() {
        // The two properties above are the proof; reaching here means they compiled.
    }
}
