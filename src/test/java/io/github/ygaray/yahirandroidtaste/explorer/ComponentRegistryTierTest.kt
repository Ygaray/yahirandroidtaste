package io.github.ygaray.yahirandroidtaste.explorer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * LEG-01: proves `ComponentRegistry.Entry.tier` is genuinely queryable in code — a real, runnable
 * cross-check that `docs/DESIGN-INTENT.md`'s three worked examples (CardBase/ChipBar/HeatSwatch)
 * match the actual registry values, not merely claimed. Plain JUnit4, no Robolectric — mirrors
 * [ComponentRegistrySearchTest]'s vacuous-pass-guard discipline.
 */
class ComponentRegistryTierTest {

    @Test
    fun registryIsNotEmpty_vacuousPassGuard() {
        assertTrue(
            "ComponentRegistry.entries is empty — every assertion below would vacuously pass " +
                "against an empty list. Failing loudly instead.",
            ComponentRegistry.entries.isNotEmpty()
        )
    }

    @Test
    fun cardBase_isTieredPattern() {
        assertEquals(
            ComponentRegistry.Tier.PATTERN,
            ComponentRegistry.entries.first { it.name == "CardBase" }.tier
        )
    }

    @Test
    fun chipBar_isTieredPrimitive() {
        assertEquals(
            ComponentRegistry.Tier.PRIMITIVE,
            ComponentRegistry.entries.first { it.name == "ChipBar" }.tier
        )
    }

    @Test
    fun heatSwatch_isTieredPattern() {
        assertEquals(
            ComponentRegistry.Tier.PATTERN,
            ComponentRegistry.entries.first { it.name == "HeatSwatch" }.tier
        )
    }
}
