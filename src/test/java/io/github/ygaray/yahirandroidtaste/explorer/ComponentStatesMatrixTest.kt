package io.github.ygaray.yahirandroidtaste.explorer

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * EXPLORE-01 / Phase-62 completeness gate: every [ComponentRegistry.entries] item must have a
 * States matrix with exactly the 4 canonical cell labels, in fixed order, and a non-null
 * `content` (Variants) lambda.
 *
 * Was RED through Plans 03-05 (the Wave-0 plan only extended the `Entry` shape with
 * defaulted-empty/null fields, per D-05 — it did not yet curate per-component states/content);
 * now GREEN — every family (Cards, Chips, Sheets, Buttons/FAB, Pickers, Feedback, Empty State)
 * carries a complete 4-cell matrix + non-null content. Kept as a standing completeness gate: any
 * future entry added to the registry without a full matrix/content will fail this test.
 * Do NOT weaken this test to pass early.
 */
class ComponentStatesMatrixTest {

    private val canonicalLabels = listOf("Default", "Pressed / Selected", "Disabled", "Focused")

    @Test
    fun registryIsNotEmpty_vacuousPassGuard() {
        assertTrue(
            "ComponentRegistry.entries is empty — completeness assertions below would " +
                "vacuously pass against an empty list. Failing loudly instead.",
            ComponentRegistry.entries.isNotEmpty()
        )
    }

    @Test
    fun everyEntry_hasCanonicalStatesInOrder_andNonNullContent() {
        val offendingStates = ComponentRegistry.entries.filter { entry ->
            entry.states.map { it.label } != canonicalLabels
        }
        val offendingContent = ComponentRegistry.entries.filter { it.content == null }

        if (offendingStates.isNotEmpty() || offendingContent.isNotEmpty()) {
            fail(
                "Phase-62 completeness gate failed — a registry entry is missing a full " +
                    "4-cell states matrix or non-null content. " +
                    "Entries missing the canonical 4-cell states matrix " +
                    "($canonicalLabels): ${offendingStates.map { it.name }.sorted()}. " +
                    "Entries missing a non-null content (Variants) lambda: " +
                    "${offendingContent.map { it.name }.sorted()}."
            )
        }
    }
}
