package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * T-23-01 (VKEY-02): while the saved-key summary shows, the raw `keyValue` must be in NO config
 * entry of NO semantics root. The key field is not composed in Summary mode (a real if/else), and
 * this scan proves it over the whole unmerged tree, popup roots included, for the plain summary, the
 * summary with the dropdown open, with `inFlight`, with `actions == null` and with an Invalid
 * `keyState`.
 *
 * Positive control: the same scan DOES find the sentinel in Field mode once the key is revealed, so
 * a passing Summary scan is not vacuous. (With the key masked the field's `EditableText` carries the
 * transformed bullets, not the raw value, in this Compose version, so the control reveals the key.)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProviderKeyCardRawKeySemanticsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sentinel = "RAW-SENTINEL-0123456789"
    private val saved = mapOf("anthropic" to SavedKeyUiModel("sk-ant-…abcd"))

    private fun setCard(
        savedKeys: Map<String, SavedKeyUiModel> = saved,
        actions: ProviderKeyActions? = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}),
        keyState: KeyFieldState = KeyFieldState.Entered
    ) {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = ProviderKeyCardGoldens.providers,
                selectedProviderId = "anthropic",
                onProviderSelected = {},
                keyValue = sentinel,
                onKeyChange = {},
                keyState = keyState,
                keyLabel = "API key",
                savedKeys = savedKeys,
                actions = actions
            )
        }
        composeTestRule.waitForIdle()
    }

    private fun assertNoSentinelAnywhere() {
        composeTestRule.waitForIdle()
        val dump = SemanticsDump.allRawRoots(composeTestRule)
        assertTrue("scan must see the card (non-vacuous)", dump.contains("provider_key_card_surface"))
        assertFalse("the raw key leaked into the semantics tree", dump.contains(sentinel))
        // The read-only provider dropdown anchor is the only legitimate EditableText in Summary mode;
        // an editable key field would add another (and carry the key).
        val editable = dump.lines().filter { it.startsWith("EditableText=") }.toSet()
        assertTrue("Summary mode must not compose the key field, saw $editable", editable.all { it == "EditableText=Anthropic" })
        assertFalse("no key field tag", dump.contains("provider_key_card_key_field"))
    }

    @Test
    fun `positive control - a revealed Field mode exposes the sentinel, so the scan is not vacuous`() {
        setCard(savedKeys = emptyMap())
        composeTestRule.onNodeWithContentDescription("Show key").performClick()
        composeTestRule.waitForIdle()
        val dump = SemanticsDump.allRawRoots(composeTestRule)
        assertTrue("the scan must find the sentinel when the field shows it", dump.contains(sentinel))
    }

    @Test
    fun `summary mode keeps the sentinel out of every root`() {
        setCard()
        composeTestRule.onNodeWithTag("provider_key_saved_summary").assertExists()
        assertNoSentinelAnywhere()
    }

    @Test
    fun `summary mode with the dropdown open keeps the sentinel out of every root`() {
        setCard()
        composeTestRule.onNodeWithTag("provider_key_card_dropdown").performClick()
        composeTestRule.waitForIdle()
        assertTrue(
            "the popup must be a second root so the scan covers it",
            SemanticsDump.roots(composeTestRule).size >= 2
        )
        assertNoSentinelAnywhere()
    }

    @Test
    fun `summary mode with inFlight keeps the sentinel out of every root`() {
        setCard(actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}, inFlight = true))
        assertNoSentinelAnywhere()
    }

    @Test
    fun `summary mode with actions null keeps the sentinel out of every root`() {
        setCard(actions = null)
        assertNoSentinelAnywhere()
    }

    @Test
    fun `summary mode with an Invalid keyState keeps the sentinel out of every root`() {
        setCard(keyState = KeyFieldState.Invalid("Key rejected by provider"))
        assertNoSentinelAnywhere()
    }

    @Test
    fun `summary mode shows the Invalid reason as its own error line, and only when Invalid`() {
        setCard(keyState = KeyFieldState.Invalid("Key rejected by provider"))
        composeTestRule.onNodeWithTag("provider_key_saved_error").assertTextEquals("Key rejected by provider")
        // The summary itself stays the one merged fingerprint node.
        composeTestRule.onNodeWithTag("provider_key_saved_summary").assertTextEquals("Saved · sk-ant-…abcd")
    }

    @Test
    fun `summary mode without an Invalid keyState renders no error line`() {
        setCard(keyState = KeyFieldState.Entered)
        assertTrue(
            composeTestRule.onAllNodesWithTag("provider_key_saved_error").fetchSemanticsNodes().isEmpty()
        )
    }
}
