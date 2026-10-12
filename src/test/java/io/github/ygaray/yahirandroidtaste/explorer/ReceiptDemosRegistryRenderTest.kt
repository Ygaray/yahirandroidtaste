package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the `OutcomeSheet` and `SegmentedOptionSelector` registry entries' Variants the way the
 * gallery does and drives the Phase 24 Receipt demos (DEMO-01): nine sheet demos reached through
 * their explorer-only trigger tags, the count editor, the hub-scheme scope and the German confirm bar
 * at 360dp (T-24-05). Trigger tags are asserted as literals. NATIVE graphics, one `setContent` per
 * method, no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReceiptDemosRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun render(entryName: String) {
        val content = checkNotNull(ComponentRegistry.entries.first { it.name == entryName }.content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun open(trigger: String) {
        render("OutcomeSheet")
        composeTestRule.onNode(hasTestTag(trigger)).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    private fun count(tag: String): Int =
        composeTestRule.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().size

    private fun textCount(text: String): Int =
        composeTestRule.onAllNodes(hasText(text), useUnmergedTree = true).fetchSemanticsNodes().size

    private fun assertExistsTags(vararg tags: String) =
        tags.forEach { assertEquals("exactly one $it", 1, count(it)) }

    private fun row(title: String): SemanticsNodeInteraction =
        composeTestRule.onNode(hasTestTag("receipt_row_summary") and hasText(title))

    private fun click(tag: String) {
        composeTestRule.onNode(hasTestTag(tag)).performClick()
        composeTestRule.waitForIdle()
    }

    private fun clickRow(title: String) {
        row(title).performClick()
        composeTestRule.waitForIdle()
    }

    // --- Confirm ---------------------------------------------------------------------------

    @Test
    fun `Confirm single item open shows the sheet parts, one expanded row and the warn tone card`() {
        open("demo_receipt_confirm_single_open")

        assertExistsTags(
            "outcome_sheet_title",
            "outcome_sheet_transcript",
            "outcome_sheet_action_bar",
            "outcome_sheet_confirmation_cancel",
            "outcome_sheet_confirmation_confirm"
        )
        assertEquals(1, count("receipt_row_summary"))
        assertEquals(1, count("receipt_row_expanded_content"))
        val toneCardsInExpanded = composeTestRule.onAllNodes(
            hasTestTag("tone_card") and hasAnyAncestor(hasTestTag("receipt_row_expanded_content")),
            useUnmergedTree = true
        ).fetchSemanticsNodes().size
        assertEquals("the count editor's warn ToneCard sits in the expanded content", 1, toneCardsInExpanded)
        assertEquals("that is the only ToneCard in the sheet", 1, count("tone_card"))
        composeTestRule.onNode(hasTestTag("outcome_sheet_confirmation_confirm")).assertIsEnabled()
    }

    @Test
    fun `Confirm batch collapsed keeps one row open at a time`() {
        open("demo_receipt_confirm_batch_collapsed")

        assertEquals(3, count("receipt_row_summary"))
        assertEquals(0, count("receipt_row_expanded_content"))
        composeTestRule.onNode(hasTestTag("outcome_sheet_confirmation_confirm")).assertIsEnabled()

        clickRow("Item B")
        assertEquals("one open after Item B", 1, count("receipt_row_expanded_content"))
        assertEquals(1, textCount("More about Item B"))

        clickRow("Item C")
        assertEquals("still one open after Item C", 1, count("receipt_row_expanded_content"))
        assertEquals("Item B closed", 0, textCount("More about Item B"))
        assertEquals(1, textCount("More about Item C"))
    }

    @Test
    fun `Confirm batch with an attention row enables Confirm only after the row is marked checked`() {
        open("demo_receipt_confirm_batch_attention")

        assertEquals(3, count("receipt_row_summary"))
        assertEquals("one attention cue, on Item B", 1, count("receipt_row_cue"))
        composeTestRule.onNode(hasTestTag("outcome_sheet_confirmation_confirm")).assertIsNotEnabled()

        clickRow("Item B")
        assertEquals(1, count("receipt_row_expanded_content"))
        assertEquals(1, count("demo_mark_checked"))

        click("demo_mark_checked")

        assertEquals("the cue is cleared", 0, count("receipt_row_cue"))
        composeTestRule.onNode(hasTestTag("outcome_sheet_confirmation_confirm")).assertIsEnabled()
    }

    @Test
    fun `Cancel closes the sheet so the single slot is empty again`() {
        open("demo_receipt_confirm_batch_collapsed")
        assertEquals(1, count("outcome_sheet_title"))

        click("outcome_sheet_confirmation_cancel")

        assertEquals(0, count("outcome_sheet_title"))
    }

    @Test
    fun `German Confirm keeps the bar and the rows inside 360dp`() {
        open("demo_receipt_confirm_german")

        assertEquals(1, textCount("Alle bestätigen (3)"))
        assertEquals(1, textCount("Abbrechen"))
        listOf("outcome_sheet_confirmation_confirm", "outcome_sheet_confirmation_cancel").forEach { tag ->
            val bounds = composeTestRule.onNode(hasTestTag(tag), useUnmergedTree = true).getUnclippedBoundsInRoot()
            assertTrue("$tag left ${bounds.left} must be >= 0", bounds.left.value >= 0f)
            assertTrue("$tag right ${bounds.right} must be <= 360dp", bounds.right.value <= 360f)
        }
        val rows = composeTestRule.onAllNodes(hasTestTag("receipt_row_summary"), useUnmergedTree = true)
        assertEquals(3, rows.fetchSemanticsNodes().size)
        for (i in 0 until 3) {
            val bounds = rows[i].getUnclippedBoundsInRoot()
            assertTrue("row $i right ${bounds.right} must be <= 360dp", bounds.right.value <= 360f)
        }
    }

    // --- Success ----------------------------------------------------------------------------

    @Test
    fun `Success single shows the badge, the handled-by pill and Done`() {
        open("demo_receipt_success_single")

        assertExistsTags("outcome_sheet_badge", "outcome_sheet_handled_by", "outcome_sheet_action_end")
        composeTestRule.onNode(hasTestTag("outcome_sheet_handled_by")).assertTextContains("Handled by", substring = true)
        composeTestRule.onNode(hasTestTag("outcome_sheet_handled_by")).assertTextContains("Local grammar", substring = true)
        composeTestRule.onNode(hasTestTag("outcome_sheet_action_end")).assertTextEquals("Done")
    }

    @Test
    fun `Success batch lists three rows, one failed with its detail, and a start action`() {
        open("demo_receipt_success_batch")

        assertEquals(3, count("outcome_sheet_batch_row"))
        assertEquals(1, textCount("could not be saved"))
        assertExistsTags("outcome_sheet_action_start", "outcome_sheet_action_end")
    }

    @Test
    fun `Success all undone has a badge, two Undone rows and no Undo all`() {
        open("demo_receipt_success_all_undone")

        assertExistsTags("outcome_sheet_badge")
        assertEquals(0, count("outcome_sheet_undo_all"))
        val rows = composeTestRule.onAllNodes(hasTestTag("outcome_sheet_undo_row"))
        assertEquals(2, rows.fetchSemanticsNodes().size)
        for (i in 0 until 2) rows[i].assertTextContains("Undone", substring = true)
    }

    // --- Failure ----------------------------------------------------------------------------

    @Test
    fun `Failure error shows the surface, the tip and both actions`() {
        open("demo_receipt_failure_error")

        assertExistsTags(
            "outcome_sheet_failure_surface",
            "outcome_sheet_failure_tip",
            "outcome_sheet_action_end",
            "outcome_sheet_action_start"
        )
    }

    @Test
    fun `Failure warn shows the same parts`() {
        open("demo_receipt_failure_warn")

        assertExistsTags(
            "outcome_sheet_failure_surface",
            "outcome_sheet_failure_tip",
            "outcome_sheet_action_end",
            "outcome_sheet_action_start"
        )
    }

    @Test
    fun `the Failure builders differ in tone and badge`() {
        val error = receiptFailureErrorOutcome {}
        val warn = receiptFailureWarnOutcome {}

        assertEquals(FailureTone.Error, error.tone)
        assertEquals(OutcomeBadge.Error, error.badge)
        assertEquals(FailureTone.Warn, warn.tone)
        assertEquals(OutcomeBadge.Warn, warn.badge)
    }

    // --- Count editor -------------------------------------------------------------------------

    private fun countEditor(): SemanticsMatcher = hasTestTag("demo_count_editor")

    private fun segment(label: String, selected: Boolean): SemanticsNodeInteraction =
        composeTestRule.onNode(
            hasContentDescription("$label, ${if (selected) "selected" else "not selected"}") and
                hasAnyAncestor(countEditor())
        )

    @Test
    fun `the count editor disables Many, keeps Few selected and shows one warn ToneCard`() {
        render("SegmentedOptionSelector")

        segment("Many", selected = false).assertIsNotEnabled().performClick()
        composeTestRule.waitForIdle()
        segment("Few", selected = true).assertIsEnabled()
        segment("Many", selected = false).assertIsNotEnabled()

        segment("Few", selected = true).performClick()
        composeTestRule.waitForIdle()
        segment("Few", selected = true).assertExists()

        val toneCards = composeTestRule.onAllNodes(
            hasTestTag("tone_card") and hasAnyAncestor(countEditor()),
            useUnmergedTree = true
        ).fetchSemanticsNodes().size
        assertEquals(1, toneCards)
        composeTestRule.onNodeWithText("“Many” is unavailable for this selection.", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `the registry entries keep their tier, family and canonical state labels`() {
        val labels = listOf("Default", "Pressed / Selected", "Disabled", "Focused")
        val selector = ComponentRegistry.entries.first { it.name == "SegmentedOptionSelector" }
        val sheet = ComponentRegistry.entries.first { it.name == "OutcomeSheet" }

        assertEquals(labels, selector.states.map { it.label })
        assertEquals(labels, sheet.states.map { it.label })
        assertEquals(ComponentRegistry.Tier.PRIMITIVE, selector.tier)
        assertEquals(ExplorerFamilies.PICKERS, selector.family)
        assertEquals(ComponentRegistry.Tier.PATTERN, sheet.tier)
        assertEquals(ExplorerFamilies.VOICE_COMMAND, sheet.family)
    }

    // --- HubSchemeScope -----------------------------------------------------------------------

    @Test
    fun `HubSchemeScope resolves the hub static scheme under a foreign light or dark outer scheme`() {
        var hubLight = Color.Unspecified
        var hubDark = Color.Unspecified
        var scopedLight = Color.Unspecified
        var scopedDark = Color.Unspecified
        val foreignLight = Color(0xFFFFEEFF)
        val foreignDark = Color(0xFF001100)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                hubLight = MaterialTheme.colorScheme.surface
            }
            YahirAndroidTasteTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
                hubDark = MaterialTheme.colorScheme.surface
            }
            MaterialTheme(colorScheme = lightColorScheme(surface = foreignLight)) {
                HubSchemeScope { scopedLight = MaterialTheme.colorScheme.surface }
            }
            MaterialTheme(colorScheme = darkColorScheme(surface = foreignDark)) {
                HubSchemeScope { scopedDark = MaterialTheme.colorScheme.surface }
            }
        }
        composeTestRule.waitForIdle()

        assertNotEquals("the hub schemes differ from each other", hubLight, hubDark)
        assertEquals("light outer resolves the hub light scheme", hubLight, scopedLight)
        assertEquals("dark outer resolves the hub dark scheme", hubDark, scopedDark)
        assertNotEquals(foreignLight, scopedLight)
        assertNotEquals(foreignDark, scopedDark)
    }
}
