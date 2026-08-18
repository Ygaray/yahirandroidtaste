package io.github.ygaray.yahirandroidtaste.explorer

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * EXPLORE-04 registry-integrity gate: every [ComponentRegistry.entries] item with a non-empty
 * `controls` list must carry a non-null `preview`, every [Control.Enum]'s `options` must be
 * non-empty and contain its `default`, no entry may declare two controls with the same label, and
 * the registry as a whole must satisfy the SC2 hard floor (at least one [Control.Toggle]-driven
 * entry AND at least one [Control.Enum]-driven entry).
 *
 * Because Plan 63-04's Task 2 curated `TextCard` (Toggle) and `TagSortControl` (Enum) among
 * others, this test is GREEN on authoring — mirrors [ComponentStatesMatrixTest]'s shape (vacuous-
 * pass guard first, then a single collecting `fail()` over all offenders).
 */
class ComponentPlaygroundIntegrityTest {

    @Test
    fun registryIsNotEmpty_vacuousPassGuard() {
        assertTrue(
            "ComponentRegistry.entries is empty — completeness assertions below would " +
                "vacuously pass against an empty list. Failing loudly instead.",
            ComponentRegistry.entries.isNotEmpty()
        )
    }

    @Test
    fun everyEntryWithControls_hasNonNullPreview() {
        val offenders = ComponentRegistry.entries.filter {
            it.controls.isNotEmpty() && it.preview == null
        }

        if (offenders.isNotEmpty()) {
            fail(
                "Entries declare non-empty controls but a null preview (knobs with nothing " +
                    "to drive): ${offenders.map { it.name }.sorted()}."
            )
        }
    }

    @Test
    fun everyEnumControl_hasNonEmptyOptionsContainingDefault() {
        val offenders = mutableListOf<String>()
        ComponentRegistry.entries.forEach { entry ->
            entry.controls.filterIsInstance<Control.Enum>().forEach { control ->
                if (control.options.isEmpty() || control.default !in control.options) {
                    offenders += "${entry.name}.${control.label}"
                }
            }
        }

        if (offenders.isNotEmpty()) {
            fail(
                "Control.Enum controls with an empty options list or a default not present " +
                    "in options: $offenders."
            )
        }
    }

    @Test
    fun everyEntry_hasDistinctControlLabels() {
        val offenders = ComponentRegistry.entries.filter { entry ->
            entry.controls.map { it.label }.distinct().size != entry.controls.size
        }

        if (offenders.isNotEmpty()) {
            fail(
                "Entries with duplicate control labels within the same entry: " +
                    "${offenders.map { it.name }.sorted()}."
            )
        }
    }

    @Test
    fun registry_meetsSc2HardFloor_atLeastOneToggleAndOneEnum() {
        val hasToggle = ComponentRegistry.entries.any { entry ->
            entry.controls.any { it is Control.Toggle }
        }
        val hasEnum = ComponentRegistry.entries.any { entry ->
            entry.controls.any { it is Control.Enum }
        }

        if (!hasToggle || !hasEnum) {
            fail(
                "SC2 hard floor not met — need at least one registry entry with a " +
                    "Control.Toggle AND at least one with a Control.Enum. " +
                    "hasToggle=$hasToggle, hasEnum=$hasEnum."
            )
        }
    }
}
