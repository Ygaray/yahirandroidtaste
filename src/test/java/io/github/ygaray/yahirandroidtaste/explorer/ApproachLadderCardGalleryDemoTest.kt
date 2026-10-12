package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
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
 * Renders the [ComponentRegistry]'s OWN `ApproachLadderCard` entry (Phase 17 D-02, VAPPR-04): the
 * private `ApproachLadderCardFixture` / `ApproachLadderCardVariants` in `VoiceCommandFamilyScreen.kt`
 * are composed by nothing else in the test tree, so reaching them through `ComponentRegistry.entries`
 * (the same way `ComponentStatesMatrixTest` and `GalleryDemoInteractionTest` do) is the only way to
 * prove the router demo state is live rather than a no-op binding.
 *
 * One rendered cell per click-driven test: two fixture cells in one composition would expose the
 * same content descriptions twice on the detail page.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ApproachLadderCardGalleryDemoTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun entry(): ComponentRegistry.Entry =
        ComponentRegistry.entries.first { it.name == "ApproachLadderCard" }

    private fun cellRender(label: String): @androidx.compose.runtime.Composable () -> Unit =
        entry().states.first { it.label == label }.render
            ?: error("ApproachLadderCard's $label StateCell has no render lambda")

    private fun variantsContent(): @androidx.compose.runtime.Composable () -> Unit =
        entry().content ?: error("ApproachLadderCard has no Variants content lambda")

    @Test
    fun defaultCell_rendersLiveRouterToggle_belowOfflineToggle() {
        composeTestRule.setContent { cellRender("Default").invoke() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle").assertExists()
        composeTestRule.onNodeWithContentDescription("Router off, selected").assertExists()

        val offline = composeTestRule.onNodeWithTag("approach_ladder_card_offline_toggle")
            .fetchSemanticsNode().boundsInRoot
        val router = composeTestRule.onNodeWithTag("approach_ladder_card_router_toggle")
            .fetchSemanticsNode().boundsInRoot
        assertTrue(
            "router top ${router.top} must be >= offline bottom ${offline.bottom}",
            router.top >= offline.bottom
        )

        // Live state, not a no-op: tapping ON flips the selected segment.
        composeTestRule.onNodeWithContentDescription("Router on, not selected").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Router on, selected").assertExists()
        composeTestRule.onNodeWithContentDescription("Router off, not selected").assertExists()
    }

    @Test
    fun variantsContent_showsRouterOffAndRouterOnCells_andHiddenCellsStayToggleFree() {
        composeTestRule.setContent { Column { variantsContent().invoke() } }
        composeTestRule.waitForIdle()

        // Default fixture (OFF) + the appended "router on" cell; the every-control-hidden,
        // combined-subdued-labels and long-localized-labels (German) cells pass no router and
        // render none.
        composeTestRule.onAllNodesWithTag("approach_ladder_card_router_toggle").assertCountEquals(2)
        composeTestRule.onAllNodesWithContentDescription("Router on, selected").assertCountEquals(1)
        composeTestRule.onAllNodesWithContentDescription("Router off, selected").assertCountEquals(1)
    }

    /**
     * G-15-1 Gate-2 cell guard (WR-01). Needs real text measurement (`GraphicsMode.NATIVE`) and the
     * Gate-2 width class (`w360dp`): under the legacy shadow measurement the German labels never
     * wrap, so this test passed against the pre-fix layout. The assertion that matters is the
     * German cell's Cloud name keeping a nonzero, unellipsized width -- the collapse G-15-1 reported.
     */
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(qualifiers = "w360dp-h800dp")
    fun variantsContent_includesLongGermanLabelCell_withBoundedAffordanceWrapping() {
        // Scrollable so every Variants cell gets its natural height instead of being squeezed.
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) { variantsContent().invoke() }
            }
        }
        composeTestRule.waitForIdle()

        // "Cloud" names a rung in every cell: the German cell's is the first one below its label.
        val sectionTop = composeTestRule
            .onNodeWithText("ApproachLadderCard — long localized labels (German)", useUnmergedTree = true)
            .fetchSemanticsNode().positionInRoot.y
        val clouds = composeTestRule.onAllNodesWithText("Cloud", useUnmergedTree = true).fetchSemanticsNodes()
        val germanCloud = clouds.filter { it.positionInRoot.y > sectionTop }.minBy { it.positionInRoot.y }
        assertTrue(
            "German cell's Cloud name width must be > 0, was ${germanCloud.size.width}",
            germanCloud.size.width > 0
        )
        assertFalse(
            "German cell's Cloud name must not be ellipsized",
            textLayout(germanCloud).isLineEllipsized(0)
        )

        listOf("Derzeit nicht verfügbar", "Netzwerkverbindung erforderlich").forEach { label ->
            val matches = composeTestRule.onAllNodesWithText(label, useUnmergedTree = true)
            matches.assertCountEquals(1)
            val node = matches[0].fetchSemanticsNode()
            assertTrue("\"$label\" width must be > 0, was ${node.size.width}", node.size.width > 0)
            val lines = textLayout(node).lineCount
            assertTrue("\"$label\" must render on <= 2 lines, rendered on $lines", lines <= 2)
        }
        composeTestRule.onAllNodesWithContentDescription("Ausschließlich offline, selected")
            .assertCountEquals(1)
    }

    private fun textLayout(node: androidx.compose.ui.semantics.SemanticsNode): TextLayoutResult {
        val layouts = mutableListOf<TextLayoutResult>()
        checkNotNull(node.config[SemanticsActions.GetTextLayoutResult].action) {
            "node has no GetTextLayoutResult action"
        }.invoke(layouts)
        return layouts.single()
    }

    @Test
    fun defaultAndPressedSelectedCells_haveIsolatedRouterState() {
        composeTestRule.setContent {
            Column {
                Box(Modifier.testTag("cell_default")) { cellRender("Default").invoke() }
                Box(Modifier.testTag("cell_pressed_selected")) { cellRender("Pressed / Selected").invoke() }
            }
        }
        composeTestRule.waitForIdle()

        fun inCell(description: String, cell: String) = composeTestRule.onNode(
            hasContentDescription(description) and hasAnyAncestor(hasTestTag(cell))
        )

        inCell("Router off, selected", "cell_default").assertExists()
        inCell("Router off, selected", "cell_pressed_selected").assertExists()

        inCell("Router on, not selected", "cell_default").performClick()
        composeTestRule.waitForIdle()

        inCell("Router on, selected", "cell_default").assertExists()
        // The sibling cell's own `remember` must not have been touched.
        inCell("Router off, selected", "cell_pressed_selected").assertExists()
        inCell("Router on, selected", "cell_pressed_selected").assertDoesNotExist()
    }
}
