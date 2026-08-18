package io.github.ygaray.yahirandroidtaste.explorer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * EXPLORE-01: the `detail/{component}` route-arg resolution contract — a name is looked up via
 * `entries.find { it.name == arg }` (Pattern 1/2, D-05 single source, no reflection/second
 * dispatch). Plain JUnit4, no Robolectric — mirrors [ComponentRegistryDriftGuardTest]'s
 * vacuous-pass-guard discipline.
 */
class ComponentDetailResolutionTest {

    @Test
    fun registryIsNotEmpty_vacuousPassGuard() {
        assertTrue(
            "ComponentRegistry.entries is empty — resolution assertions below would vacuously " +
                "pass against an empty list. Failing loudly instead.",
            ComponentRegistry.entries.isNotEmpty()
        )
    }

    @Test
    fun everyRegisteredName_resolvesToExactlyOneEntry() {
        val names = ComponentRegistry.entries.map { it.name }
        names.forEach { name ->
            val matchCount = ComponentRegistry.entries.count { it.name == name }
            assertEquals(
                "Expected exactly one entry named \"$name\" but found $matchCount.",
                1,
                matchCount
            )
            val found = ComponentRegistry.entries.find { it.name == name }
            assertTrue("entries.find { it.name == \"$name\" } was null.", found != null)
        }
    }

    @Test
    fun unknownRouteArg_resolvesToNull() {
        val found = ComponentRegistry.entries.find { it.name == "NoSuchComponent__" }
        assertNull(
            "An unknown detail/{component} route arg must resolve to null so the caller can " +
                "render the not-found fallback instead of crashing.",
            found
        )
    }
}
