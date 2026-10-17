package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A long German or Spanish Undo Center message on an openable row, with the Undo button AND the
 * chevron beside it, at 360dp (NATIVE graphics, real text metrics) and at font scales 1.0 and 2.0
 * (memory lesson: caller-supplied text needs a long-string check before layout sign-off). Geometry
 * is read UNCLIPPED (`positionInRoot` plus `size`) because `boundsInRoot` is clipped to ancestors
 * and would hide an overflow. Structural and bounds proofs only; STYLE.md section 7 keeps the
 * layout sign-off on a real-device screenshot.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UndoCenterScreenOpenTargetLongLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var density: Density = Density(1f)

    private val german =
        "Eintrag Wocheneinkauf für das lange Wochenende wurde gelöscht und kann hier rückgängig gemacht werden"
    private val spanish =
        "La entrada de la compra semanal para el fin de semana largo se eliminó y se puede deshacer desde aquí"
    private val target = FeedbackTarget("item-42", "Eintrag öffnen")

    private fun render(message: String, fontScale: Float, withTarget: Boolean = true): String {
        val store = UndoHistoryStore()
        val id = if (withTarget) {
            store.append(message, target = target) { }
        } else {
            store.append(message) { }
        }
        val entries = store.entries.value
        composeTestRule.setContent {
            val base = LocalDensity.current
            density = base
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = fontScale)) {
                YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    UndoCenterScreen(
                        entries = entries,
                        onNavigateBack = {},
                        onUndo = {},
                        onOpenTarget = {},
                        onClear = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        return id
    }

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    private fun edges(interaction: SemanticsNodeInteraction): Pair<Float, Float> {
        val node = interaction.fetchSemanticsNode()
        return node.positionInRoot.x to node.positionInRoot.x + node.size.width
    }

    private fun assertRowGeometry(name: String, message: String, fontScale: Float) {
        val id = render(message, fontScale)
        val windowPx = 360 * density.density
        val (undoLeft, undoRight) = edges(composeTestRule.onNodeWithText("Undo", useUnmergedTree = true))
        val (chevronLeft, chevronRight) = edges(
            composeTestRule.onNodeWithTag("$UNDO_OPEN_CHEVRON_TEST_TAG_PREFIX$id", useUnmergedTree = true)
        )
        val messageNode = composeTestRule.onNodeWithText(message, useUnmergedTree = true)
        val (messageLeft, messageRight) = edges(messageNode)

        assertTrue("$name: Undo left $undoLeft must be >= 0", undoLeft >= 0f)
        assertTrue("$name: chevron right $chevronRight must be <= window $windowPx", chevronRight <= windowPx)
        assertTrue("$name: message left $messageLeft must be >= 0", messageLeft >= 0f)
        assertTrue("$name: message right $messageRight must be <= Undo left $undoLeft", messageRight <= undoLeft)
        assertTrue("$name: Undo right $undoRight must be <= chevron left $chevronLeft", undoRight <= chevronLeft)

        val layout = messageNode.textLayout()
        for (line in 0 until layout.lineCount) {
            assertFalse("$name: message line $line must not be ellipsized", layout.isLineEllipsized(line))
        }
        assertFalse("$name: message must not overflow height", layout.didOverflowHeight)
    }

    @Test
    fun `German message keeps Undo and the chevron inside 360dp at font scale 1`() =
        assertRowGeometry("German 1.0", german, 1f)

    @Test
    fun `German message keeps Undo and the chevron inside 360dp at font scale 2`() =
        assertRowGeometry("German 2.0", german, 2f)

    @Test
    fun `Spanish message keeps Undo and the chevron inside 360dp at font scale 1`() =
        assertRowGeometry("Spanish 1.0", spanish, 1f)

    @Test
    fun `Spanish message keeps Undo and the chevron inside 360dp at font scale 2`() =
        assertRowGeometry("Spanish 2.0", spanish, 2f)

    @Test
    fun `a row without a target has no chevron and its Undo reaches the v2-7-1 trailing inset`() {
        val store = UndoHistoryStore()
        val old = store.append("Alt: $german") { }
        val plain = store.append("Neu: $german") { }
        val entriesOld = store.entries.value.filter { it.id == old }
        val entriesNew = store.entries.value.filter { it.id == plain }
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column {
                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                        UndoCenterScreen(entries = entriesOld, onNavigateBack = {}, onUndo = {}, onClear = {})
                    }
                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                        UndoCenterScreen(
                            entries = entriesNew,
                            onNavigateBack = {},
                            onUndo = {},
                            onOpenTarget = {},
                            onClear = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("$UNDO_OPEN_CHEVRON_TEST_TAG_PREFIX$plain", useUnmergedTree = true)
            .assertDoesNotExist()
        val undoButtons = composeTestRule.onAllNodesWithText("Undo", useUnmergedTree = true)
        val (_, oldRight) = edges(undoButtons[0])
        val (_, newRight) = edges(undoButtons[1])
        assertEquals("the new overload must not move Undo on a targetless row", oldRight, newRight, 0.5f)
    }
}
