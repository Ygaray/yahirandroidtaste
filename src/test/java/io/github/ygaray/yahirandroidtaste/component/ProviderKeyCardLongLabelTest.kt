package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyLabels
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel
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
 * German and Spanish labels at 360dp (NATIVE graphics, real text metrics) for [ProviderKeyCard]'s
 * action rows and summary (memory lesson: caller-overridable labels need a long-string check before
 * layout sign-off). Geometry is read UNCLIPPED (`positionInRoot` + `size`) because `boundsInRoot`
 * is clipped to ancestors and would hide an overflow. Structural and bounds proofs only; no pixel
 * capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProviderKeyCardLongLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var density: Density = Density(1f)
    private val fingerprint = "sk-ant-…abcdef123456"

    private val german = ProviderKeyLabels(
        save = "Speichern",
        update = "Aktualisieren",
        clear = "Entfernen",
        cancel = "Abbrechen",
        savedPrefix = "Gespeichert",
        savedMarker = "gespeichert"
    )
    private val spanish = ProviderKeyLabels(
        save = "Guardar",
        update = "Actualizar",
        clear = "Borrar",
        cancel = "Cancelar",
        savedPrefix = "Guardada",
        savedMarker = "guardada"
    )

    private fun renderCard(selected: String, labels: ProviderKeyLabels, actions: ProviderKeyActions) {
        composeTestRule.setContent {
            density = LocalDensity.current
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                ProviderKeyCard(
                    providers = ProviderKeyCardGoldens.providers,
                    selectedProviderId = selected,
                    onProviderSelected = {},
                    keyValue = "",
                    onKeyChange = {},
                    keyState = KeyFieldState.Empty,
                    keyLabel = "API key",
                    savedKeys = mapOf("anthropic" to SavedKeyUiModel(fingerprint)),
                    actions = actions,
                    labels = labels
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

    private fun assertInsideWindow(name: String, tag: String) {
        val node = composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
        val windowPx = 360 * density.density
        val left = node.positionInRoot.x
        val right = left + node.size.width
        assertTrue("$name: left edge $left must be >= 0", left >= 0f)
        assertTrue("$name: right edge $right must be <= window $windowPx", right <= windowPx)
    }

    /**
     * `TextLayoutResult.hasVisualOverflow` / `didOverflowWidth` read true for ANY wrap-content label
     * narrower than its max width in this NATIVE setup (a 59px "Cancelar" in a 136px slot), so they
     * prove nothing. The overflow proof is: no ellipsized line, no height overflow, the laid-out text
     * fits its constraint, and its unclipped right edge is inside the 360dp window.
     */
    private fun assertNoVisualOverflow(name: String, text: String) {
        val node = composeTestRule.onNodeWithText(text, useUnmergedTree = true)
        val layout = node.textLayout()
        for (line in 0 until layout.lineCount) {
            assertFalse("$name: \"$text\" line $line must not be ellipsized", layout.isLineEllipsized(line))
        }
        assertFalse("$name: \"$text\" must not overflow height", layout.didOverflowHeight)
        assertTrue(
            "$name: \"$text\" laid-out width ${layout.size.width} must fit ${layout.layoutInput.constraints.maxWidth}",
            layout.size.width <= layout.layoutInput.constraints.maxWidth
        )
        val windowPx = 360 * density.density
        val right = node.fetchSemanticsNode().let { it.positionInRoot.x + it.size.width }
        assertTrue("$name: \"$text\" right edge $right must be <= window $windowPx", right <= windowPx)
    }

    private fun assertSummaryMode(labels: ProviderKeyLabels) {
        renderCard(
            selected = "anthropic",
            labels = labels,
            actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {})
        )
        assertInsideWindow("summary Update", "provider_key_action_update")
        assertInsideWindow("summary Clear", "provider_key_action_clear")
        assertInsideWindow("summary text", "provider_key_saved_summary")
        assertNoVisualOverflow("summary", labels.update)
        assertNoVisualOverflow("summary", labels.clear)
        assertNoVisualOverflow("summary", "${labels.savedPrefix} · $fingerprint")
    }

    private fun assertFieldMode(labels: ProviderKeyLabels) {
        renderCard(
            selected = "openai",
            labels = labels,
            actions = ProviderKeyActions(onSave = {}, onCancelEdit = {})
        )
        assertInsideWindow("field Cancel", "provider_key_action_cancel")
        assertInsideWindow("field Save", "provider_key_action_save")
        assertNoVisualOverflow("field", labels.cancel)
        assertNoVisualOverflow("field", labels.save)
    }

    @Test
    fun `German summary Update and Clear stay inside 360dp without overflow`() = assertSummaryMode(german)

    @Test
    fun `Spanish summary Update and Clear stay inside 360dp without overflow`() = assertSummaryMode(spanish)

    @Test
    fun `German field Cancel and Save stay inside 360dp without overflow`() = assertFieldMode(german)

    @Test
    fun `Spanish field Cancel and Save stay inside 360dp without overflow`() = assertFieldMode(spanish)

    @Test
    fun `the open dropdown shows the German marker row`() {
        renderCard(
            selected = "openai",
            labels = german,
            actions = ProviderKeyActions(onSave = {})
        )
        composeTestRule.onNodeWithTag("provider_key_card_dropdown").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Anthropic · gespeichert").assertExists()
    }
}
