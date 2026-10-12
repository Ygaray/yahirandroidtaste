package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyLabels
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Lifecycle behavior of [ProviderKeyCard] (VKEY-01, VKEY-02): the saved-key summary with Update and
 * Clear, the Field-mode Save/Cancel row, `inFlight` and `saveEnabled` gating, 48dp targets, custom
 * labels, the explicit-default parity cases and the pure mode / text seams.
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
        actions: ProviderKeyActions? = null,
        labels: ProviderKeyLabels = ProviderKeyDefaults.labels()
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
            actions = actions,
            labels = labels
        )
    }

    private var density: Density = Density(1f)

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

    // ── Field mode: Save and Cancel ──

    @Test
    fun `field mode with actions renders Save and no Update or Clear, Cancel only when onCancelEdit is set`() {
        composeTestRule.setContent {
            Card(selected = "openai", actions = ProviderKeyActions(onSave = {}))
        }
        composeTestRule.waitForIdle()
        assertTagPresent("provider_key_card_key_field")
        assertTagPresent("provider_key_action_save")
        assertTagAbsent("provider_key_action_cancel")
        assertTagAbsent("provider_key_action_update")
        assertTagAbsent("provider_key_action_clear")
        assertTagAbsent("provider_key_saved_summary")
    }

    @Test
    fun `field mode renders Cancel when onCancelEdit is set and each click fires its own callback once`() {
        var saves = 0
        var cancels = 0
        composeTestRule.setContent {
            Card(
                selected = "openai",
                actions = ProviderKeyActions(onSave = { saves++ }, onCancelEdit = { cancels++ })
            )
        }
        composeTestRule.waitForIdle()
        assertTagPresent("provider_key_action_cancel")

        composeTestRule.onNodeWithTag("provider_key_action_save").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, saves)
        assertEquals(0, cancels)

        composeTestRule.onNodeWithTag("provider_key_action_cancel").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, saves)
        assertEquals(1, cancels)
    }

    @Test
    fun `saveEnabled false disables Save and a click never fires onSave`() {
        var saves = 0
        composeTestRule.setContent {
            Card(selected = "openai", actions = ProviderKeyActions(onSave = { saves++ }, saveEnabled = false))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_action_save").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("provider_key_action_save").performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, saves)
    }

    @Test
    fun `inFlight in field mode disables Save and Cancel and no click fires`() {
        var saves = 0
        var cancels = 0
        composeTestRule.setContent {
            Card(
                selected = "openai",
                actions = ProviderKeyActions(
                    onSave = { saves++ },
                    onCancelEdit = { cancels++ },
                    inFlight = true
                )
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_action_save").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("provider_key_action_cancel").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("provider_key_action_save").performClick()
        composeTestRule.onNodeWithTag("provider_key_action_cancel").performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, saves)
        assertEquals(0, cancels)
    }

    @Test
    fun `inFlight in summary mode disables Update and Clear and no click fires`() {
        var updates = 0
        var clears = 0
        composeTestRule.setContent {
            Card(
                actions = ProviderKeyActions(
                    onSave = {},
                    onUpdate = { updates++ },
                    onClear = { clears++ },
                    inFlight = true
                )
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("provider_key_action_update").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("provider_key_action_clear").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("provider_key_action_update").performClick()
        composeTestRule.onNodeWithTag("provider_key_action_clear").performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, updates)
        assertEquals(0, clears)
    }

    @Test
    fun `without inFlight the summary actions are enabled`() {
        composeTestRule.setContent {
            Card(actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}))
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("provider_key_action_update").assertIsEnabled()
        composeTestRule.onNodeWithTag("provider_key_action_clear").assertIsEnabled()
    }

    @Test
    fun `editingSavedKey with a saved key shows the field plus Save and Cancel, not the summary`() {
        composeTestRule.setContent {
            Card(
                editing = true,
                actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}, onCancelEdit = {})
            )
        }
        composeTestRule.waitForIdle()
        assertTagPresent("provider_key_card_key_field")
        assertTagAbsent("provider_key_saved_summary")
        assertTagPresent("provider_key_action_save")
        assertTagPresent("provider_key_action_cancel")
        assertTagAbsent("provider_key_action_update")
        assertTagAbsent("provider_key_action_clear")
    }

    @Test
    fun `actions null in field mode renders no action tag`() {
        composeTestRule.setContent { Card(selected = "openai", actions = null) }
        composeTestRule.waitForIdle()
        assertTagPresent("provider_key_card_key_field")
        listOf("save", "update", "clear", "cancel").forEach { assertTagAbsent("provider_key_action_$it") }
    }

    // ── 48dp touch targets ──

    private fun assertTouchTargetsAtLeast48dp(vararg tags: String) {
        val minPx = 48f * density.density
        tags.forEach { tag ->
            val touch = composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNode().touchBoundsInRoot
            assertTrue("$tag touch width ${touch.width} >= $minPx", touch.width >= minPx)
            assertTrue("$tag touch height ${touch.height} >= $minPx", touch.height >= minPx)
        }
    }

    @Test
    fun `summary actions have touch bounds of at least 48dp both ways`() {
        composeTestRule.setContent {
            density = LocalDensity.current
            Card(actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}))
        }
        composeTestRule.waitForIdle()
        assertTouchTargetsAtLeast48dp("provider_key_action_update", "provider_key_action_clear")
    }

    @Test
    fun `field actions have touch bounds of at least 48dp both ways`() {
        composeTestRule.setContent {
            density = LocalDensity.current
            Card(selected = "openai", actions = ProviderKeyActions(onSave = {}, onCancelEdit = {}))
        }
        composeTestRule.waitForIdle()
        assertTouchTargetsAtLeast48dp("provider_key_action_save", "provider_key_action_cancel")
    }

    // ── Custom labels ──

    private val spanish = ProviderKeyDefaults.labels(
        save = "Guardar",
        update = "Actualizar",
        clear = "Borrar",
        cancel = "Cancelar",
        savedPrefix = "Guardada",
        savedMarker = "guardada"
    )

    private fun hasText(text: String): Boolean =
        composeTestRule.onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `custom labels replace Update, Clear and the Saved prefix in summary mode`() {
        composeTestRule.setContent {
            Card(actions = ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}), labels = spanish)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("provider_key_saved_summary")
            .assertTextEquals("Guardada · $fingerprint")
        assertTrue(hasText("Actualizar"))
        assertTrue(hasText("Borrar"))
        listOf("Update", "Clear", "Saved", "Save", "Cancel").forEach {
            assertTrue("\"$it\" must be absent", !hasText(it))
        }
    }

    @Test
    fun `custom labels replace Save and Cancel in field mode`() {
        composeTestRule.setContent {
            Card(
                selected = "openai",
                actions = ProviderKeyActions(onSave = {}, onCancelEdit = {}),
                labels = spanish
            )
        }
        composeTestRule.waitForIdle()
        assertTrue(hasText("Guardar"))
        assertTrue(hasText("Cancelar"))
        listOf("Save", "Cancel").forEach {
            assertTrue("\"$it\" must be absent", !hasText(it))
        }
    }

    // ── Explicit-default parity with the Wave 0 goldens ──

    private fun assertEqualsS1() {
        composeTestRule.waitForIdle()
        assertEquals(ProviderKeyCardGoldens.S1, SemanticsDump.allRoots(composeTestRule))
    }

    @Test
    fun `an explicit-default call equals the Wave 0 S1 golden`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "anthropic",
                onProviderSelected = {},
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key",
                savedKeys = emptyMap(),
                editingSavedKey = false,
                actions = null,
                labels = ProviderKeyDefaults.labels()
            )
        }
        assertEqualsS1()
    }

    @Test
    fun `editingSavedKey true alone still equals the S1 golden`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "anthropic",
                onProviderSelected = {},
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key",
                editingSavedKey = true
            )
        }
        assertEqualsS1()
    }

    @Test
    fun `custom labels alone with no saved keys and no actions still equal the S1 golden`() {
        composeTestRule.setContent {
            ProviderKeyCard(
                providers = providers,
                selectedProviderId = "anthropic",
                onProviderSelected = {},
                keyValue = "",
                onKeyChange = {},
                keyState = KeyFieldState.Empty,
                keyLabel = "API key",
                labels = spanish
            )
        }
        assertEqualsS1()
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
    fun `providerMenuLabel appends the marker only for saved ids`() {
        val option = ProviderKeyCardGoldens.providers.first { it.id == "anthropic" }
        assertEquals("Anthropic", providerMenuLabel(option, emptySet(), ProviderKeyLabels()))
        assertEquals("Anthropic", providerMenuLabel(option, setOf("openai"), ProviderKeyLabels()))
        assertEquals("Anthropic \u00B7 saved", providerMenuLabel(option, setOf("anthropic"), ProviderKeyLabels()))
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
