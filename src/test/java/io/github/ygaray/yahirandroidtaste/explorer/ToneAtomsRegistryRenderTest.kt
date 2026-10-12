package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
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
}
