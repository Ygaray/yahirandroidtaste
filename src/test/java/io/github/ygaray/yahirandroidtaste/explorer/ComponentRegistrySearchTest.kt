package io.github.ygaray.yahirandroidtaste.explorer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * EXPLORE-02 (D-04): [ComponentSearch.filter] pure-logic coverage. Plain JUnit4, no Robolectric —
 * mirrors [ComponentRegistryDriftGuardTest]'s vacuous-pass-guard discipline (assert the registry
 * actually has entries before trusting any filter-behavior conclusion drawn from it).
 */
class ComponentRegistrySearchTest {

    @Test
    fun registryIsNotEmpty_vacuousPassGuard() {
        assertTrue(
            "ComponentRegistry.entries is empty — every assertion below would vacuously pass " +
                "against an empty list. Failing loudly instead.",
            ComponentRegistry.entries.isNotEmpty()
        )
    }

    @Test
    fun blankQuery_returnsNull() {
        assertEquals(null, ComponentSearch.filter(""))
    }

    @Test
    fun whitespaceOnlyQuery_returnsNull() {
        assertEquals(null, ComponentSearch.filter("   "))
    }

    @Test
    fun caseInsensitive_lowerAndUpperQueryReturnSameResult() {
        val lower = ComponentSearch.filter("chip")
        val upper = ComponentSearch.filter("CHIP")
        assertEquals(lower, upper)
    }

    @Test
    fun filter_matchesSubstringAcrossFamilies_excludesNonMatches() {
        val result = ComponentSearch.filter("chip")
        assertTrue(
            "Expected a non-null result for a non-blank query.",
            result != null
        )
        val names = result!!.map { it.name }
        assertTrue("Expected \"ChipBar\" in filtered results: $names", names.contains("ChipBar"))
        assertTrue("Expected \"AppChip\" in filtered results: $names", names.contains("AppChip"))
        assertTrue(
            "Expected \"TextCard\" to be excluded from a \"chip\" query: $names",
            !names.contains("TextCard")
        )
    }

    @Test
    fun filter_preservesRegistryDeclarationOrder() {
        val result = ComponentSearch.filter("chip")!!
        val registryOrder = ComponentRegistry.entries.map { it.name }
        val filteredNames = result.map { it.name }
        // The filtered list must be a subsequence of the full registry order (stable — EDGE
        // ordering): each filtered name's index in the registry must be strictly increasing.
        val registryIndices = filteredNames.map { registryOrder.indexOf(it) }
        assertEquals(registryIndices, registryIndices.sorted())
    }
}
