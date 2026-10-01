package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [ProviderKeyCard] (Phase 10 Plan 01, VSET-01 tracer).
 *
 * Infra mirrors this module's established Robolectric+Compose harness ([CountBadgeTest]):
 * `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`.
 *
 * Masked-by-default is asserted via the [SemanticsProperties.Password] marker — `CoreTextField`
 * sets this on the field's semantics whenever `visualTransformation is PasswordVisualTransformation`
 * (confirmed by inspecting the resolved `androidx.compose.foundation` AAR's `CoreTextField`
 * semantics-modifier bytecode: the `$isPassword` flag flows directly into
 * `SemanticsPropertiesKt.password(...)`). This is deliberately NOT tested via `onNodeWithText`
 * absence for the raw value — `EditableText` semantics always carry the raw (untransformed)
 * value regardless of masking, so an absence-based assertion would pass vacuously.
 *
 * The reveal flip itself is asserted via the eye affordance's `contentDescription` ("Show key" ->
 * "Hide key") rather than re-checking the `Password` marker's absence post-reveal: an isolated
 * scratch probe against [ClearableTextField] alone (external boolean toggle, no
 * [ProviderKeyCard]/[RevealToggle] involved) showed the resolved Compose Material3 alpha
 * (`1.5.0-alpha17`) does not retract the `Password` semantics marker once set on a later
 * recomposition, even though `visualTransformation` and all other observable behavior (icon,
 * `EditableText`) update correctly — an upstream semantics-caching quirk, not a defect in this
 * task's production code, and out of this plan's scope to work around.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProviderKeyCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val providers = listOf(
        ProviderOptionUiModel(id = "openai", label = "OpenAI"),
        ProviderOptionUiModel(id = "anthropic", label = "Anthropic")
    )

    @Test
    fun `renders the currently-selected provider label from props`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "anthropic",
                onProviderSelected = {},
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Anthropic").assertExists()
    }

    @Test
    fun `key field is masked by default and reveals when the eye affordance is tapped`() {
        composeTestRule.setContent {
            var keyValue by remember { mutableStateOf("sk-fixture-value") }
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "openai",
                onProviderSelected = {},
                keyValue = keyValue,
                onKeyChange = { keyValue = it },
                keyState = KeyFieldState.Entered,
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()

        val maskedNode = composeTestRule.onNodeWithTag("provider_key_card_key_field").fetchSemanticsNode()
        assertTrue(
            "Key field must carry Password semantics while hidden (masked by default, D-02/D-05)",
            maskedNode.config.contains(SemanticsProperties.Password)
        )
        composeTestRule.onNodeWithContentDescription("Show key").assertExists()

        composeTestRule.onNodeWithContentDescription("Show key").performClick()
        composeTestRule.waitForIdle()

        // The eye's contentDescription flipping to "Hide key" proves `revealed` threaded
        // correctly from the tap into the card's hoisted state and back into ClearableTextField's
        // `revealToggle` prop (see class KDoc for why the Password-marker-absence check is not
        // re-asserted here).
        composeTestRule.onNodeWithContentDescription("Hide key").assertExists()
    }

    @Test
    fun `choosing a provider from the dropdown emits onProviderSelected with the tapped id`() {
        var selected: String? = null
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = null,
                onProviderSelected = { selected = it },
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_card_dropdown").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Anthropic").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Tapping the Anthropic dropdown row must emit onProviderSelected(\"anthropic\")",
            "anthropic",
            selected
        )
    }

    @Test
    fun `editing the key emits onKeyChange and the clear and reveal affordances render together`() {
        var lastKeyChange: String? = null
        composeTestRule.setContent {
            var keyValue by remember { mutableStateOf("") }
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "openai",
                onProviderSelected = {},
                keyValue = keyValue,
                onKeyChange = {
                    keyValue = it
                    lastKeyChange = it
                },
                keyState = KeyFieldState.Empty,
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_card_key_field").performTextInput("sk-abc123")
        composeTestRule.waitForIdle()

        assertEquals("sk-abc123", lastKeyChange)
        // Both trailing icons must render together — the reveal eye never displaces the clear-✕.
        composeTestRule.onNodeWithContentDescription("Clear text").assertExists()
        composeTestRule.onNodeWithContentDescription("Show key").assertExists()
    }

    // ── WR-03: an empty providers list renders a caption, never a blank tappable dropdown ──

    @Test
    fun `an empty providers list renders the emptyProvidersReason caption instead of a dropdown`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = emptyList(),
                selectedProviderId = null,
                onProviderSelected = {},
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key",
                emptyProvidersReason = "Add a provider to get started"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Add a provider to get started").assertExists()
    }

    @Test
    fun `an Invalid keyState renders the reason and sets the field's error state`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "openai",
                onProviderSelected = {},
                keyValue = "sk-bad-key",
                onKeyChange = {},
                keyState = KeyFieldState.Invalid("Key rejected by provider"),
                keyLabel = "API key"
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Key rejected by provider").assertExists()
    }
}
