package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistry
import io.github.ygaray.yahirandroidtaste.explorer.ExplorerFamilies
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Compose tests for [ReceiptRow] (Phase 21 VROW-01, VROW-02): geometry, structure and semantics.
 *
 * Text geometry needs real font metrics, so the class runs at w360dp with NATIVE graphics (legacy
 * graphics measure about one pixel per character and would pass vacuously). Bounds are read
 * UNCLIPPED. No pixel capture: it times out in this harness. One `setContent` per test method.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReceiptRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setRow(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.width(360.dp)) { content() }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `tracer - a collapsed row with every line and no cue is 92dp tall`() {
        setRow {
            ReceiptRow(
                title = "Item title",
                subtitle = "Subtitle detail",
                supportingContent = { Text("Supporting line") },
                value = "1,250",
                valueCaption = "Total",
                onClick = {}
            )
        }

        val bounds = composeTestRule.onNodeWithTag("receipt_row_summary").getUnclippedBoundsInRoot()
        assertEquals("summary height", 92f, (bounds.bottom - bounds.top).value, 1f)
    }

    @Test
    fun `tracer - the merged summary holds the title and the value`() {
        setRow {
            ReceiptRow(title = "Item title", value = "1,250", onClick = {})
        }

        composeTestRule.onNodeWithTag("receipt_row_summary").assertTextContains("Item title", substring = true)
        composeTestRule.onNodeWithTag("receipt_row_summary").assertTextContains("1,250", substring = true)
    }

    @Test
    fun `tracer - the registry lists ReceiptRow as a Cards pattern with canonical states`() {
        val entry = ComponentRegistry.entries.first { it.name == "ReceiptRow" }

        assertEquals(ExplorerFamilies.CARDS, entry.family)
        assertEquals(ComponentRegistry.Tier.PATTERN, entry.tier)
        assertEquals(
            listOf("Default", "Pressed / Selected", "Disabled", "Focused"),
            entry.states.map { it.label }
        )
        listOf("Default", "Pressed / Selected", "Disabled").forEach { label ->
            assertNotNull("$label cell", entry.states.first { it.label == label }.render)
        }
        assertNotNull(entry.content)
    }
}
