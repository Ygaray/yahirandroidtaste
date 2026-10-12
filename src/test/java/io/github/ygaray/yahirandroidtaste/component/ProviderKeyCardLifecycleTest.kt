package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyLabels
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Lifecycle behavior of [ProviderKeyCard] (VKEY-01, VKEY-02): the saved-key summary with Update and
 * Clear (Phase 23 tracer), and the pure mode / text seams. Later tasks append the Field-mode
 * Save/Cancel row, `inFlight` and `saveEnabled` gating, 48dp targets, custom labels and the
 * explicit-default parity cases.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProviderKeyCardLifecycleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val providers = ProviderKeyCardGoldens.providers
    private val fingerprint = "sk-ant-…abcd"
    private val saved = mapOf("anthropic" to SavedKeyUiModel(fingerprint))

    @Composable
    private fun Card(
        selected: String? = "anthropic",
        savedKeys: Map<String, SavedKeyUiModel> = saved,
        editing: Boolean = false,
        actions: ProviderKeyActions? = null
    ) {
        ProviderKeyCard(
            providers = providers,
            selectedProviderId = selected,
            onProviderSelected = {},
            keyValue = "",
            onKeyChange = {},
            keyState = KeyFieldState.Empty,
            keyLabel = "API key",
            savedKeys = savedKeys,
            editingSavedKey = editing,
            actions = actions
        )
    }

    private fun assertTagAbsent(tag: String) =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).assertDoesNotExist()

    private fun assertTagPresent(tag: String) =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).assertExists()

    // ── Tracer ──

    @Test
    fun `summary shows the exact text, no key field, Update and Clear but no Save or Cancel`() {
        composeTestRule.setContent {
            Card(actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_saved_summary")
            .assertTextEquals("Saved · $fingerprint")
        assertTagAbsent("provider_key_card_key_field")
        assertTagPresent("provider_key_action_update")
        assertTagPresent("provider_key_action_clear")
        assertTagAbsent("provider_key_action_save")
        assertTagAbsent("provider_key_action_cancel")
    }

    @Test
    fun `clicking Clear invokes onClear once and onUpdate never`() {
        var updates = 0
        var clears = 0
        composeTestRule.setContent {
            Card(actions = ProviderKeyActions(onSave = {}, onUpdate = { updates++ }, onClear = { clears++ }))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_action_clear").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, clears)
        assertEquals(0, updates)
    }

    @Test
    fun `clicking Update invokes onUpdate once`() {
        var updates = 0
        var clears = 0
        composeTestRule.setContent {
            Card(actions = ProviderKeyActions(onSave = {}, onUpdate = { updates++ }, onClear = { clears++ }))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_action_update").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, updates)
        assertEquals(0, clears)
    }

    @Test
    fun `a summary with actions null renders the summary and no action tag`() {
        composeTestRule.setContent { Card(actions = null) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_saved_summary")
            .assertTextEquals("Saved · $fingerprint")
        assertTagAbsent("provider_key_card_key_field")
        listOf("save", "update", "clear", "cancel").forEach { assertTagAbsent("provider_key_action_$it") }
    }

    @Test
    fun `a null onUpdate hides Update and a null onClear hides Clear`() {
        composeTestRule.setContent {
            Card(actions = ProviderKeyActions(onSave = {}, onUpdate = null, onClear = {}))
        }
        composeTestRule.waitForIdle()
        assertTagAbsent("provider_key_action_update")
        assertTagPresent("provider_key_action_clear")
    }

    @Test
    fun `a null onClear hides Clear and keeps Update`() {
        composeTestRule.setContent {
            Card(actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = null))
        }
        composeTestRule.waitForIdle()
        assertTagPresent("provider_key_action_update")
        assertTagAbsent("provider_key_action_clear")
    }

    @Test
    fun `both onUpdate and onClear null render no action row at all`() {
        composeTestRule.setContent {
            Card(actions = ProviderKeyActions(onSave = {}))
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("provider_key_saved_summary").assertExists()
        listOf("save", "update", "clear", "cancel").forEach { assertTagAbsent("provider_key_action_$it") }
    }

    // ── Pure seams ──

    @Test
    fun `providerKeyMode is Summary only when the selected provider has a saved key and is not editing`() {
        val summary = ProviderKeyMode.Summary
        val field = ProviderKeyMode.Field
        assertEquals(summary, providerKeyMode(saved, "anthropic", editingSavedKey = false))
        assertEquals(field, providerKeyMode(saved, "openai", editingSavedKey = false))
        assertEquals(field, providerKeyMode(saved, "anthropic", editingSavedKey = true))
        assertEquals(field, providerKeyMode(saved, null, editingSavedKey = false))
        assertEquals(field, providerKeyMode(emptyMap(), "anthropic", editingSavedKey = false))
    }

    @Test
    fun `savedSummaryText keeps the middle dot format with a custom prefix`() {
        val model = SavedKeyUiModel(fingerprint)
        assertEquals("Saved · $fingerprint", savedSummaryText(ProviderKeyLabels(), model))
        assertEquals(
            "Guardada · $fingerprint",
            savedSummaryText(ProviderKeyLabels(savedPrefix = "Guardada"), model)
        )
    }
}
