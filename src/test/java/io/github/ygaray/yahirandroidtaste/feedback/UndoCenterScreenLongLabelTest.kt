package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * German and Spanish `undoAllLabel` plus a long group label at 360dp (NATIVE graphics, real text
 * metrics) for the [UndoCenterScreen] group header (memory lesson: caller-overridable labels need a
 * long-string check before layout sign-off). Geometry is read UNCLIPPED (`positionInRoot` + `size`)
 * because `boundsInRoot` is clipped to ancestors and would hide an overflow. Structural and bounds
 * proofs only; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UndoCenterScreenLongLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var density: Density = Density(1f)

    private val shortGroupLabel = "Import batch"
    private val longGroupLabel = "Importación de la carpeta de fotografías de las vacaciones de verano en familia"

    private fun render(undoAllLabel: String, groupLabel: String) {
        val store = UndoHistoryStore()
        store.openGroup("g1", groupLabel) { }
        store.append("First imported", groupId = "g1") { }
        store.append("Second imported", groupId = "g1") { }
        val group = UndoGroupRowUiModel(
            groupId = "g1",
            label = groupLabel,
            memberIds = store.group("g1").map { it.id },
            status = store.groupStatus("g1"),
            undoAllLabel = undoAllLabel
        )
        val entries = store.entries.value
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                UndoCenterScreen(
                    entries = entries,
                    onNavigateBack = {},
                    onUndo = {},
                    groups = listOf(group),
                    onUndoGroup = {},
                    onClear = {}
                )
            }
        }
        composeTestRule.waitForIdle()
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

    /**
     * Same proof as the ProviderKeyCard long-label test: `hasVisualOverflow` reads true for any
     * wrap-content label narrower than its slot here, so the proof is no ellipsized line, no height
     * overflow and a single line (a laid-out width within its constraint is always true, so it is not asserted).
     */
    private fun assertNoVisualOverflow(name: String, text: String) {
        val node = composeTestRule.onNodeWithText(text, useUnmergedTree = true)
        val layout = node.textLayout()
        for (line in 0 until layout.lineCount) {
            assertFalse("$name: \"$text\" line $line must not be ellipsized", layout.isLineEllipsized(line))
        }
        assertFalse("$name: \"$text\" must not overflow height", layout.didOverflowHeight)
        assertTrue("$name: \"$text\" one line expected, got ${layout.lineCount}", layout.lineCount == 1)
    }

    private fun assertHeaderLayout(name: String, undoAllLabel: String, groupLabel: String) {
        render(undoAllLabel, groupLabel)
        val windowPx = 360 * density.density
        val button = composeTestRule.onNodeWithTag("${UNDO_GROUP_UNDO_ALL_TEST_TAG_PREFIX}g1", useUnmergedTree = true)
        val (buttonLeft, buttonRight) = edges(button)
        assertTrue("$name: button left $buttonLeft must be >= 0", buttonLeft >= 0f)
        assertTrue("$name: button right $buttonRight must be <= window $windowPx", buttonRight <= windowPx)
        assertNoVisualOverflow(name, undoAllLabel)

        val (_, labelRight) = edges(composeTestRule.onNodeWithText(groupLabel, useUnmergedTree = true))
        val (_, subtitleRight) = edges(composeTestRule.onNodeWithText("2 items · Undoable", useUnmergedTree = true))
        assertTrue("$name: group label right $labelRight must be <= button left $buttonLeft", labelRight <= buttonLeft)
        assertTrue("$name: subtitle right $subtitleRight must be <= button left $buttonLeft", subtitleRight <= buttonLeft)
    }

    @Test
    fun `German Undo all stays inside 360dp with no overflow and no overlap`() =
        assertHeaderLayout("German", "Alle rückgängig machen", shortGroupLabel)

    @Test
    fun `Spanish Undo all stays inside 360dp with no overflow and no overlap`() =
        assertHeaderLayout("Spanish", "Deshacer todo", shortGroupLabel)

    @Test
    fun `a long group label with the German Undo all still keeps the button inside and the text clear of it`() =
        assertHeaderLayout("long label", "Alle rückgängig machen", longGroupLabel)

    @Test
    fun `a long group label with the default Undo all keeps the button inside and the text clear of it`() =
        assertHeaderLayout("default label", "Undo all", longGroupLabel)
}
