package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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

        // Default fixture (OFF) + the appended "router on" cell; the every-control-hidden and
        // combined-subdued-labels cells pass no router and render none.
        composeTestRule.onAllNodesWithTag("approach_ladder_card_router_toggle").assertCountEquals(2)
        composeTestRule.onAllNodesWithContentDescription("Router on, selected").assertCountEquals(1)
        composeTestRule.onAllNodesWithContentDescription("Router off, selected").assertCountEquals(1)
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
