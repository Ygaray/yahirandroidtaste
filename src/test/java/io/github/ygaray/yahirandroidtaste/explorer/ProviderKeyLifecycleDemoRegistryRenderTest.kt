package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
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
 * Renders the `ProviderKeyCard` registry entry's Variants the way the gallery does and drives the
 * key lifecycle demo added in Phase 24 (DEMO-01): three providers, one saved, edit and clear flows,
 * a raw-key sentinel scan with a positive control (T-24-01), and the German labels at 360dp
 * (T-24-05). The existing English `ProviderKeyCard` demo shares the page, so every query is scoped
 * to the demo container. The demo tags are asserted as literals. NATIVE graphics; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProviderKeyLifecycleDemoRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val english = "demo_key_lifecycle"
    private val german = "demo_key_lifecycle_german"
    private var density: Density = Density(1f)

    private fun render() {
        val content = checkNotNull(ComponentRegistry.entries.first { it.name == "ProviderKeyCard" }.content)
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun inDemo(tag: String, demo: String = english): SemanticsMatcher =
        hasTestTag(tag) and hasAnyAncestor(hasTestTag(demo))

    private fun node(tag: String, demo: String = english): SemanticsNodeInteraction =
        composeTestRule.onNode(inDemo(tag, demo))

    private fun unmerged(tag: String, demo: String = english): SemanticsNodeInteraction =
        composeTestRule.onNode(inDemo(tag, demo), useUnmergedTree = true)

    private fun count(tag: String, demo: String = english): Int =
        composeTestRule.onAllNodes(inDemo(tag, demo)).fetchSemanticsNodes().size

    private fun click(tag: String, demo: String = english) {
        node(tag, demo).performClick()
        composeTestRule.waitForIdle()
    }

    private fun type(text: String, demo: String = english) {
        node("provider_key_card_key_field", demo).performTextInput(text)
        composeTestRule.waitForIdle()
    }

    private fun assertSummary(expected: String, demo: String = english) {
        node("provider_key_saved_summary", demo).assertTextEquals(expected)
        assertEquals("no key field while the summary shows", 0, count("provider_key_card_key_field", demo))
    }

    private fun assertFieldMode(demo: String = english) {
        assertEquals("key field", 1, count("provider_key_card_key_field", demo))
        assertEquals("no summary", 0, count("provider_key_saved_summary", demo))
        assertEquals("no Update in field mode", 0, count("provider_key_action_update", demo))
        assertEquals("no Clear in field mode", 0, count("provider_key_action_clear", demo))
    }

    /** A dropdown row is plain `Text`; the anchor's value is `EditableText`, which this matcher skips. */
    private fun menuRow(label: String): SemanticsNodeInteraction =
        composeTestRule.onNode(hasTextExactly(label, includeEditableText = false), useUnmergedTree = true)

    private fun menuRowCount(label: String): Int =
        composeTestRule.onAllNodes(hasTextExactly(label, includeEditableText = false), useUnmergedTree = true)
            .fetchSemanticsNodes().size

    private fun openDropdown(demo: String = english) = click("provider_key_card_dropdown", demo)

    private fun selectProvider(label: String, demo: String = english) {
        openDropdown(demo)
        menuRow(label).performClick()
        composeTestRule.waitForIdle()
    }

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    // --- Initial state ----------------------------------------------------------------------

    @Test
    fun `the lifecycle starts on the saved provider with a summary and Update and Clear`() {
        render()

        assertSummary("Saved · key-…c3d4")
        assertEquals(1, count("provider_key_action_update"))
        assertEquals(1, count("provider_key_action_clear"))
        assertEquals("no Save in summary mode", 0, count("provider_key_action_save"))
    }

    @Test
    fun `the dropdown marks only the saved provider`() {
        render()

        openDropdown()

        assertEquals("Provider B carries the saved marker", 1, menuRowCount("Provider B · saved"))
        assertEquals("Provider A has no marker", 1, menuRowCount("Provider A"))
        assertEquals("Provider C has no marker", 1, menuRowCount("Provider C"))
        assertEquals("no marker on A", 0, menuRowCount("Provider A · saved"))
        assertEquals("no marker on C", 0, menuRowCount("Provider C · saved"))
    }

    // --- Edit flow ---------------------------------------------------------------------------

    @Test
    fun `Update shows the field with Save disabled until text is typed, and Cancel returns to the summary`() {
        render()

        click("provider_key_action_update")

        assertFieldMode()
        assertEquals("Cancel is offered while editing", 1, count("provider_key_action_cancel"))
        node("provider_key_action_save").assertIsNotEnabled()
        type("abcd1234")
        node("provider_key_action_save").assertIsEnabled()

        click("provider_key_action_cancel")

        assertSummary("Saved · key-…c3d4")
    }

    @Test
    fun `Update then Save returns to a summary with the last four characters`() {
        render()

        click("provider_key_action_update")
        type("abcd1234")
        click("provider_key_action_save")

        assertSummary("Saved · key-…1234")
        assertEquals(1, count("provider_key_action_update"))
        assertEquals(1, count("provider_key_action_clear"))
    }

    // --- Clear flow ---------------------------------------------------------------------------

    @Test
    fun `Clear removes the summary and the dropdown marker and shows the field with Save only`() {
        render()

        click("provider_key_action_clear")

        assertFieldMode()
        assertEquals("a cleared provider has no Cancel", 0, count("provider_key_action_cancel"))
        assertEquals(1, count("provider_key_action_save"))
        openDropdown()
        assertEquals("the marker is gone", 0, menuRowCount("Provider B · saved"))
        assertEquals("Provider B reads plain", 1, menuRowCount("Provider B"))
    }

    @Test
    fun `an unsaved provider shows the field with Save and no Cancel, and switching back shows the saved summary`() {
        render()

        selectProvider("Provider A")

        assertFieldMode()
        assertEquals("no Cancel for an unsaved provider", 0, count("provider_key_action_cancel"))
        assertEquals(1, count("provider_key_action_save"))

        selectProvider("Provider B · saved")

        assertSummary("Saved · key-…c3d4")
    }

    @Test
    fun `after Clear, selecting Provider A and then B shows B's field because it was cleared`() {
        render()

        click("provider_key_action_clear")
        selectProvider("Provider A")
        assertFieldMode()
        selectProvider("Provider B")

        assertFieldMode()
    }

    // --- Raw-key safety (T-24-01) --------------------------------------------------------------

    private val sentinel = "RAW-SENTINEL-0123456789"

    @Test
    fun `the typed sentinel never reaches the tree while the saved summary shows`() {
        render()

        click("provider_key_action_update")
        type(sentinel)
        click("provider_key_action_save")

        assertSummary("Saved · key-…6789")
        val dump = SemanticsDump.allRawRoots(composeTestRule)
        assertTrue("scan must see the demo (non-vacuous)", dump.contains("provider_key_saved_summary"))
        assertFalse("the full typed text leaked into the semantics tree", dump.contains(sentinel))
    }

    @Test
    fun `positive control - a revealed field mode exposes the sentinel, so the scan is not vacuous`() {
        render()

        click("provider_key_action_update")
        type(sentinel)
        composeTestRule.onNode(
            hasContentDescription("Show key") and hasAnyAncestor(hasTestTag(english)),
            useUnmergedTree = true
        ).performClick()
        composeTestRule.waitForIdle()

        val dump = SemanticsDump.allRawRoots(composeTestRule)
        assertTrue("the scan must find the sentinel when the field shows it", dump.contains(sentinel))
    }

    // --- German labels at 360dp (T-24-05) -------------------------------------------------------

    private fun assertInWindowAndOneLine(name: String, tag: String, text: String) {
        val windowPx = 360 * density.density
        val bounds = unmerged(tag, german).fetchSemanticsNode()
        val left = bounds.positionInRoot.x
        val right = left + bounds.size.width
        assertTrue("$name: left $left must be >= 0", left >= 0f)
        assertTrue("$name: right $right must be <= window $windowPx", right <= windowPx)
        val layout = composeTestRule
            .onNode(hasText(text) and hasAnyAncestor(hasTestTag(german)), useUnmergedTree = true)
            .textLayout()
        for (line in 0 until layout.lineCount) {
            assertFalse("$name: \"$text\" line $line must not be ellipsized", layout.isLineEllipsized(line))
        }
        assertFalse("$name: \"$text\" must not overflow height", layout.didOverflowHeight)
        assertEquals("$name: \"$text\" one line expected", 1, layout.lineCount)
    }

    @Test
    fun `the German section shows Gespeichert with Aktualisieren and Entfernen`() {
        render()

        node("provider_key_saved_summary", german).assertTextEquals("Gespeichert · key-…c3d4")
        composeTestRule.onNode(
            hasText("Aktualisieren") and hasAnyAncestor(hasTestTag(german)),
            useUnmergedTree = true
        ).assertExists()
        composeTestRule.onNode(
            hasText("Entfernen") and hasAnyAncestor(hasTestTag(german)),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun `the German summary and action buttons stay inside 360dp with no overflow`() {
        render()

        assertInWindowAndOneLine("summary", "provider_key_saved_summary", "Gespeichert · key-…c3d4")
        assertInWindowAndOneLine("update", "provider_key_action_update", "Aktualisieren")
        assertInWindowAndOneLine("clear", "provider_key_action_clear", "Entfernen")
    }
}
