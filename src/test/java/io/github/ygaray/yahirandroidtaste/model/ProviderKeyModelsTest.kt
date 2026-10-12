package io.github.ygaray.yahirandroidtaste.model

import io.github.ygaray.yahirandroidtaste.component.ProviderKeyDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Defaults and shape of the Phase 23 key-lifecycle models (VKEY-01, T-23-04): none of them can hold a
 * raw key; the labels carry the English defaults and each factory argument overrides only its field.
 */
class ProviderKeyModelsTest {

    private val fingerprint = "sk-ant-…abcd"

    @Test
    fun `labels default to the English strings and equal the factory`() {
        val labels = ProviderKeyLabels()
        assertEquals("Save", labels.save)
        assertEquals("Update", labels.update)
        assertEquals("Clear", labels.clear)
        assertEquals("Cancel", labels.cancel)
        assertEquals("Saved", labels.savedPrefix)
        assertEquals("saved", labels.savedMarker)
        assertEquals(labels, ProviderKeyDefaults.labels())
    }

    @Test
    fun `each factory argument overrides only its own field`() {
        val base = ProviderKeyLabels()
        assertEquals(base.copy(save = "S"), ProviderKeyDefaults.labels(save = "S"))
        assertEquals(base.copy(update = "U"), ProviderKeyDefaults.labels(update = "U"))
        assertEquals(base.copy(clear = "C"), ProviderKeyDefaults.labels(clear = "C"))
        assertEquals(base.copy(cancel = "X"), ProviderKeyDefaults.labels(cancel = "X"))
        assertEquals(base.copy(savedPrefix = "P"), ProviderKeyDefaults.labels(savedPrefix = "P"))
        assertEquals(base.copy(savedMarker = "M"), ProviderKeyDefaults.labels(savedMarker = "M"))
    }

    @Test
    fun `actions default to save enabled, no optional callbacks and not in flight`() {
        val actions = ProviderKeyActions(onSave = {})
        assertTrue(actions.saveEnabled)
        assertNull(actions.onUpdate)
        assertNull(actions.onClear)
        assertNull(actions.onCancelEdit)
        assertFalse(actions.inFlight)
    }

    @Test
    fun `SavedKeyUiModel equality is by fingerprint and toString shows only it`() {
        assertEquals(SavedKeyUiModel(fingerprint), SavedKeyUiModel(fingerprint))
        assertFalse(SavedKeyUiModel(fingerprint) == SavedKeyUiModel("other"))
        assertEquals("SavedKeyUiModel(fingerprint=$fingerprint)", SavedKeyUiModel(fingerprint).toString())
    }

    @Test
    fun `no model has a property that could hold a raw key`() {
        listOf(SavedKeyUiModel::class.java, ProviderKeyActions::class.java, ProviderKeyLabels::class.java)
            .forEach { type ->
                type.declaredFields.map { it.name }.filterNot { it.startsWith("$") }.forEach { name ->
                    assertFalse(
                        "${type.simpleName}.$name looks like a key holder",
                        name.contains("keyValue", ignoreCase = true) ||
                            name.contains("rawKey", ignoreCase = true) ||
                            name.contains("secret", ignoreCase = true)
                    )
                }
            }
        assertEquals(
            listOf("fingerprint"),
            SavedKeyUiModel::class.java.declaredFields.map { it.name }.filterNot { it.startsWith("$") }
        )
    }
}
