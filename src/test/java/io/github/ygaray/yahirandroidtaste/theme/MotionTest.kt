package io.github.ygaray.yahirandroidtaste.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Pure-JUnit tests for [ExpressiveMotion] (Phase 44 Plan 01 Task 3, DS-02). Motion tokens are
 * plain data, not composables, so no `createComposeRule()` is needed — mirrors
 * `UndoHistoryStoreTest`'s plain-JUnit shape.
 */
class MotionTest {

    @Test
    fun `standardMillis is 300`() {
        assertEquals(300, ExpressiveMotion.standardMillis)
    }

    @Test
    fun `spatial and effects specs are non-null`() {
        assertNotNull(ExpressiveMotion.emphasizedSpatialSpec)
        assertNotNull(ExpressiveMotion.fastSpatialSpec)
        assertNotNull(ExpressiveMotion.emphasizedEffectsSpec)
    }

    @Test
    fun `enterFade and exitFade are non-null`() {
        assertNotNull(ExpressiveMotion.enterFade)
        assertNotNull(ExpressiveMotion.exitFade)
    }
}
