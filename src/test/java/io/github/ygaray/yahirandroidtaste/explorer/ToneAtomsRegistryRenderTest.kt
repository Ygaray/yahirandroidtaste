package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Renders the registry entries added by the tone-foundation atoms (Phase 20) the way the gallery
 * does: every non-null state cell and the Variants content, under the light and dark themes.
 * Plans 20-02 to 20-04 append one test per atom here.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ToneAtomsRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val canonicalLabels = listOf("Default", "Pressed / Selected", "Disabled", "Focused")

    private fun entry(name: String): ComponentRegistry.Entry =
        ComponentRegistry.entries.first { it.name == name }

    /** Renders each non-null state cell and the content lambda once per theme mode (one composition). */
    private fun renderEverything(entry: ComponentRegistry.Entry) {
        val renders = entry.states.mapNotNull { it.render } + listOfNotNull(entry.content)
        composeTestRule.setContent {
            Column {
                listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                    renders.forEach { render ->
                        YahirAndroidTasteTheme(themeMode = mode, dynamicColor = false) { render() }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `ToneCard is a Cards primitive with canonical states and renders in both themes`() {
        val toneCard = entry("ToneCard")
        assertEquals(ExplorerFamilies.CARDS, toneCard.family)
        assertEquals(ComponentRegistry.Tier.PRIMITIVE, toneCard.tier)
        assertEquals(canonicalLabels, toneCard.states.map { it.label })
        assertNotNull(toneCard.states.first { it.label == "Default" }.render)
        assertNotNull(toneCard.content)

        renderEverything(toneCard)

        // Default cell + Variants, rendered twice (light and dark): 1 + 2 cards per theme.
        val count = composeTestRule.onAllNodesWithTag("tone_card").fetchSemanticsNodes().size
        assertTrue("expected tone cards from cells and Variants, found $count", count >= 2 * 3)
    }

    @Test
    fun `DynamicActionButton keeps its Buttons family entry and renders cells and Variants in both themes`() {
        val button = entry("DynamicActionButton")
        assertEquals(ExplorerFamilies.BUTTONS_FAB, button.family)
        assertEquals(canonicalLabels, button.states.map { it.label })
        assertNotNull(button.states.first { it.label == "Default" }.render)
        assertNotNull(button.states.first { it.label == "Disabled" }.render)
        assertNotNull(button.content)

        renderEverything(button)

        val clickable = composeTestRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size
        // Per theme: 2 cells + 3 legacy + 12 emphasis x role + 2 icon buttons.
        assertTrue("expected at least ${2 * 19} click targets, found $clickable", clickable >= 2 * 19)
    }

    @Test
    fun `DynamicActionButton Variants render the legacy trio, 12 emphasis buttons and 2 icon buttons`() {
        val content = checkNotNull(entry("DynamicActionButton").content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column { content() }
            }
        }
        composeTestRule.waitForIdle()

        val clickable = composeTestRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size
        assertTrue("expected at least 17 click targets in Variants, found $clickable", clickable >= 3 + 12 + 2)
    }

    @Test
    fun `RoundIconButton is a Buttons primitive with canonical states and renders in both themes`() {
        val button = entry("RoundIconButton")
        assertEquals(ExplorerFamilies.BUTTONS_FAB, button.family)
        assertEquals(ComponentRegistry.Tier.PRIMITIVE, button.tier)
        assertEquals(canonicalLabels, button.states.map { it.label })
        assertNotNull(button.states.first { it.label == "Default" }.render)
        assertNotNull(button.states.first { it.label == "Disabled" }.render)
        assertNotNull(button.content)

        renderEverything(button)

        // Per theme: Default + Disabled cells and 6 Variants buttons.
        val count = composeTestRule.onAllNodesWithTag("round_icon_button").fetchSemanticsNodes().size
        assertTrue("expected round icon buttons from cells and Variants, found $count", count >= 2 * 8)
    }

    @Test
    fun `RoundIconButton Variants render at least four tagged buttons`() {
        val content = checkNotNull(entry("RoundIconButton").content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column { content() }
            }
        }
        composeTestRule.waitForIdle()

        val count = composeTestRule.onAllNodesWithTag("round_icon_button").fetchSemanticsNodes().size
        assertTrue("expected at least 4 buttons in Variants, found $count", count >= 4)
    }

    @Test
    fun `RoundIconButton Disabled cell renders a not-enabled node`() {
        val disabled = checkNotNull(entry("RoundIconButton").states.first { it.label == "Disabled" }.render)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) { disabled() }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("round_icon_button").assertIsNotEnabled()
    }

    @Test
    fun `PillChip is a Chips primitive with canonical states and renders in both themes`() {
        val chip = entry("PillChip")
        assertEquals(ExplorerFamilies.CHIPS, chip.family)
        assertEquals(ComponentRegistry.Tier.PRIMITIVE, chip.tier)
        assertEquals(canonicalLabels, chip.states.map { it.label })
        assertNotNull(chip.states.first { it.label == "Default" }.render)
        assertNotNull(chip.states.first { it.label == "Disabled" }.render)
        assertNotNull(chip.content)

        renderEverything(chip)

        val count = composeTestRule.onAllNodesWithTag("pill_chip").fetchSemanticsNodes().size
        // Per theme: Default + Disabled cells and 6 Variants chips.
        assertTrue("expected pill chips from cells and Variants, found $count", count >= 2 * 8)
    }

    @Test
    fun `PillChip Variants render at least five tagged chips`() {
        val content = checkNotNull(entry("PillChip").content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column { content() }
            }
        }
        composeTestRule.waitForIdle()

        val count = composeTestRule.onAllNodesWithTag("pill_chip").fetchSemanticsNodes().size
        assertTrue("expected at least 5 chips in Variants, found $count", count >= 5)
    }

    @Test
    fun `StackedProgressBar is a Progress primitive with canonical states and renders in both themes`() {
        val bar = entry("StackedProgressBar")
        assertEquals(ExplorerFamilies.PROGRESS_METRICS, bar.family)
        assertEquals(ComponentRegistry.Tier.PRIMITIVE, bar.tier)
        assertEquals(canonicalLabels, bar.states.map { it.label })
        assertNotNull(bar.states.first { it.label == "Default" }.render)
        assertNotNull(bar.content)

        renderEverything(bar)

        // Per theme: the Default cell plus 5 Variants bars.
        val count = composeTestRule.onAllNodesWithTag("stacked_progress_bar").fetchSemanticsNodes().size
        assertTrue("expected bars from the cell and Variants, found $count", count >= 2 * 6)
    }

    @Test
    fun `StackedProgressBar Variants render at least five tagged bars`() {
        val content = checkNotNull(entry("StackedProgressBar").content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column { content() }
            }
        }
        composeTestRule.waitForIdle()

        val count = composeTestRule.onAllNodesWithTag("stacked_progress_bar").fetchSemanticsNodes().size
        assertTrue("expected at least 5 bars in Variants, found $count", count >= 5)
    }
}
