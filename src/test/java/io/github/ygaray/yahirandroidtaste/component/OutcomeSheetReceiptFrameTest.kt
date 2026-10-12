package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Frame proof for the Receipt content (VRCPT-01, VRCPT-02): title row slots, transcript rendering
 * rule, shared item rows, the badge, and the pure badge seams. The Success and Failure arms are
 * checked only for their title row here; plans 22-04 and 22-05 prove their remaining sections.
 * Hosts [OutcomeSheetReceiptContent] in a bounded box (a vertical scroll needs a bounded height).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetReceiptFrameTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var density = 1f

    private fun setContent(outcome: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            density = LocalDensity.current.density
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.height(600.dp)) { OutcomeSheetReceiptContent(outcome) }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun tagged(tag: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun count(tag: String) = composeTestRule.onAllNodesWithTag(tag, useUnmergedTree = true)

    // ---- Bar structure (IN-05) ------------------------------------------------------------------

    @Test
    fun `the pinned bar is a sibling below the scroll body, not a descendant of it`() {
        // Enough items that the body overflows the 600dp host and must scroll.
        setContent(OutcomeFixtures.needsConfirmation(items = OutcomeFixtures.proposedItems(30)))

        val scrollBody = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        // Positive control: the scroll matcher really contains the body content.
        composeTestRule.onAllNodes(hasTestTag("outcome_sheet_title") and hasAnyAncestor(scrollBody), true)
            .assertCountEquals(1)
        composeTestRule.onAllNodes(hasTestTag("outcome_sheet_action_bar") and hasAnyAncestor(scrollBody), true)
            .assertCountEquals(0)

        val body = composeTestRule.onAllNodes(scrollBody, true).fetchSemanticsNodes().single()
        val bar = tagged("outcome_sheet_action_bar").fetchSemanticsNode()
        assertTrue(
            "bar top ${bar.positionInRoot.y} must be at or below the scroll body bottom " +
                "${body.positionInRoot.y + body.size.height}",
            bar.positionInRoot.y >= body.positionInRoot.y + body.size.height - 1f
        )
    }

    // ---- Title row ----------------------------------------------------------------------------

    @Test
    fun `a titled confirmation shows the title as a heading`() {
        setContent(OutcomeFixtures.needsConfirmation(title = "Log these?"))

        composeTestRule.onNodeWithText("Log these?", useUnmergedTree = true).assertExists()
        val node = tagged("outcome_sheet_title").fetchSemanticsNode()
        assertTrue("title must carry heading semantics", node.config.contains(SemanticsProperties.Heading))
    }

    @Test
    fun `no title no badge and no title actions renders no title row nodes`() {
        setContent(OutcomeFixtures.needsConfirmation(title = null, titleActions = null))

        count("outcome_sheet_title").assertCountEquals(0)
        count("outcome_sheet_badge").assertCountEquals(0)
        count("outcome_sheet_title_actions").assertCountEquals(0)
    }

    @Test
    fun `title actions render inside their tag to the right of the title`() {
        setContent(
            OutcomeFixtures.needsConfirmation(
                title = "Log these?",
                titleActions = { Text("EDIT", Modifier.testTag("title_action_probe")) }
            )
        )

        tagged("outcome_sheet_title_actions").assertExists()
        composeTestRule.onNodeWithTag("title_action_probe", useUnmergedTree = true).assertExists()
        val title = tagged("outcome_sheet_title").fetchSemanticsNode()
        val actions = tagged("outcome_sheet_title_actions").fetchSemanticsNode()
        val titleRight = title.positionInRoot.x + title.size.width
        assertTrue(
            "actions left ${actions.positionInRoot.x} must be at or after the title right $titleRight",
            actions.positionInRoot.x >= titleRight - 1f
        )
    }

    // ---- Transcript, items, slots -------------------------------------------------------------

    @Test
    fun `a blank reason renders no transcript`() {
        setContent(OutcomeFixtures.needsConfirmation(reason = "   "))

        count("outcome_sheet_transcript").assertCountEquals(0)
    }

    @Test
    fun `a reason renders as the transcript`() {
        setContent(OutcomeFixtures.needsConfirmation(reason = "two eggs and toast"))

        composeTestRule.onNodeWithText("two eggs and toast", useUnmergedTree = true).assertExists()
        tagged("outcome_sheet_transcript").assertExists()
    }

    @Test
    fun `empty items with top level content renders the content and no item rows`() {
        setContent(
            OutcomeFixtures.needsConfirmation(
                items = emptyList(),
                topLevelContent = { Text("custom rows") }
            )
        )

        tagged("outcome_sheet_confirmation_top_level_content").assertExists()
        count("outcome_sheet_confirmation_item").assertCountEquals(0)
    }

    @Test
    fun `an item with onRemove shows the remove control`() {
        setContent(
            OutcomeFixtures.needsConfirmation(
                items = listOf(
                    io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel(
                        id = "a",
                        title = "Eggs",
                        onRemove = {}
                    )
                )
            )
        )

        tagged("outcome_sheet_confirmation_item_remove").assertExists()
    }

    // ---- Success and Failure arms (title row only) --------------------------------------------

    @Test
    fun `success shows the summary as the title and a 32dp badge`() {
        setContent(VoiceOutcomeUiState.Success(summary = "Logged 2 items", badge = OutcomeBadge.Success))

        composeTestRule.onNodeWithText("Logged 2 items", useUnmergedTree = true).assertExists()
        tagged("outcome_sheet_title").assertExists()
        val badge = tagged("outcome_sheet_badge").fetchSemanticsNode()
        assertEquals("badge width", 32f * density, badge.size.width.toFloat(), 1f)
        assertEquals("badge height", 32f * density, badge.size.height.toFloat(), 1f)
    }

    @Test
    fun `success without a badge renders no badge`() {
        setContent(VoiceOutcomeUiState.Success(summary = "Logged 2 items"))

        tagged("outcome_sheet_title").assertExists()
        count("outcome_sheet_badge").assertCountEquals(0)
    }

    @Test
    fun `failure without a title and badge renders no title`() {
        setContent(VoiceOutcomeUiState.Failure(reason = "Network down", title = null, badge = null))

        count("outcome_sheet_title").assertCountEquals(0)
    }

    @Test
    fun `failure with a title shows it`() {
        setContent(VoiceOutcomeUiState.Failure(reason = "Network down", title = "Could not log"))

        composeTestRule.onNodeWithText("Could not log", useUnmergedTree = true).assertExists()
        tagged("outcome_sheet_title").assertExists()
    }

    // ---- Pure seams ---------------------------------------------------------------------------

    @Test
    fun `badge icons map to the four glyphs`() {
        assertSame(Icons.Filled.Check, outcomeBadgeIcon(OutcomeBadge.Success))
        assertSame(Icons.AutoMirrored.Filled.Undo, outcomeBadgeIcon(OutcomeBadge.Reverted))
        assertSame(Icons.Filled.Close, outcomeBadgeIcon(OutcomeBadge.Error))
        assertSame(Icons.Filled.PriorityHigh, outcomeBadgeIcon(OutcomeBadge.Warn))
    }

    @Test
    fun `badge colors follow the hue rules`() {
        val scheme = lightColorScheme()
        val warn = ToneColors(Color(0xFFFFCC00), Color(0xFF332200))
        val error = ToneColors(Color(0xFFFF0000), Color(0xFFFFFFFF))
        val colors = OutcomeSheetColors(warnTone = warn, errorTone = error)

        assertEquals(ToneColors(scheme.primary, scheme.onPrimary), outcomeBadgeColors(OutcomeBadge.Success, colors, scheme))
        assertEquals(
            ToneColors(scheme.onSurfaceVariant, scheme.surface),
            outcomeBadgeColors(OutcomeBadge.Reverted, colors, scheme)
        )
        assertEquals(error, outcomeBadgeColors(OutcomeBadge.Error, colors, scheme))
        assertEquals(ToneColors(warn.content, warn.container), outcomeBadgeColors(OutcomeBadge.Warn, colors, scheme))
    }
}
