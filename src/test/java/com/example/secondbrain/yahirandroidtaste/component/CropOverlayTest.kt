package com.example.secondbrain.yahirandroidtaste.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [CropOverlay] (GADGET-03, Phase 86).
 *
 * Covers only the deterministic legs per 86-RESEARCH.md's Wave 0 gap note — drag-gesture
 * simulation is deferred to Gate-1 (device-driven), not practical to assert precisely under
 * Robolectric's pointer-input simulation.
 *
 * Infra mirrors this module's established Robolectric+Compose harness
 * ([DynamicActionButtonTest]): `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`,
 * `createComposeRule()`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CropOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `emits normalized full-image bounds on initial measure when initialCropBounds is null`() {
        var emittedBounds: List<Float>? = null

        composeTestRule.setContent {
            CropOverlay(
                bitmapWidth = 400,
                bitmapHeight = 300,
                aspectRatio = null,
                initialCropBounds = null,
                onCropBoundsChanged = { l, t, r, b -> emittedBounds = listOf(l, t, r, b) },
                modifier = Modifier.fillMaxSize().height(240.dp)
            )
        }
        composeTestRule.waitForIdle()

        val bounds = emittedBounds
        assertTrue("onCropBoundsChanged should have been invoked on initial measure", bounds != null)
        assertEquals(listOf(0f, 0f, 1f, 1f), bounds)
    }

    @Test
    fun `restores from a non-full-image initialCropBounds instead of emitting full-image bounds`() {
        var emittedBounds: List<Float>? = null
        val restoredBounds = listOf(0.1f, 0.2f, 0.8f, 0.7f)

        composeTestRule.setContent {
            CropOverlay(
                bitmapWidth = 400,
                bitmapHeight = 300,
                aspectRatio = null,
                initialCropBounds = restoredBounds,
                onCropBoundsChanged = { l, t, r, b -> emittedBounds = listOf(l, t, r, b) },
                modifier = Modifier.fillMaxSize().height(240.dp)
            )
        }
        composeTestRule.waitForIdle()

        // Restoration from initialCropBounds does NOT re-emit through onCropBoundsChanged (the
        // caller already holds these bounds) — composition must still succeed without crashing
        // and without emitting the full-image [0,0,1,1] reset. Drag/measure pixel-level parity
        // beyond this is Gate-1-owned (device-driven), per 86-RESEARCH.md Wave 0 gap note.
        assertEquals(
            "restoring from a non-full-image initialCropBounds must not emit any bounds",
            null,
            emittedBounds
        )
    }
}
