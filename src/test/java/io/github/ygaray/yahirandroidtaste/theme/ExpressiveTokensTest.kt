package io.github.ygaray.yahirandroidtaste.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Pure-JUnit tests for [ExpressiveTokens]/[expressiveTokensFor] (Phase 44 Plan 01 Task 2, DS-01).
 *
 * Locks that every color role resolves to the corresponding [ColorScheme] role — in both
 * [LightColorScheme] and [DarkColorScheme] — rather than a duplicated raw color constant, and
 * that the non-color fields ([ExpressiveTokens.heroGradient], [ExpressiveTokens.cardShapeLarge])
 * are always non-null.
 */
class ExpressiveTokensTest {

    @Test
    fun `light scheme color roles match ColorScheme roles`() {
        val tokens = expressiveTokensFor(LightColorScheme)

        assertEquals(LightColorScheme.outline, tokens.ringTrack)
        assertEquals(LightColorScheme.primary, tokens.onTrack)
        assertEquals(LightColorScheme.error, tokens.overBudget)
        assertEquals(LightColorScheme.tertiary, tokens.nearGoal)
    }

    @Test
    fun `dark scheme color roles match ColorScheme roles`() {
        val tokens = expressiveTokensFor(DarkColorScheme)

        assertEquals(DarkColorScheme.outline, tokens.ringTrack)
        assertEquals(DarkColorScheme.primary, tokens.onTrack)
        assertEquals(DarkColorScheme.error, tokens.overBudget)
        assertEquals(DarkColorScheme.tertiary, tokens.nearGoal)
    }

    @Test
    fun `heroGradient and cardShapeLarge are non-null`() {
        val tokens = expressiveTokensFor(LightColorScheme)

        assertNotNull(tokens.heroGradient)
        assertNotNull(tokens.cardShapeLarge)
    }
}
