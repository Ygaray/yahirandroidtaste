package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM (no Robolectric) tests for [PlaceMapPicker]'s handle-drag math -- [haversineMeters],
 * [handlePoint], [RadiusSpec.radiusFromHandle] -- and [MapLifecycleGate]'s idempotent
 * resume/pause pairing (164-02 Task 2, review round 2 MEDIUM: `MapView.onResume()` invoked
 * twice). See [PlaceMapPickerTest] for the render-behavior and other model tests this file's
 * sibling covers.
 */
class PlaceMapPickerModelTest {

    // ---- haversineMeters ----

    @Test
    fun `haversineMeters over a small known span is close to the expected meters`() {
        val meters = haversineMeters(0.0, 0.0, 0.001, 0.0)
        assertTrue("expected within [111.0, 111.4], was $meters", meters in 111.0..111.4)
    }

    @Test
    fun `haversineMeters of a point to itself is zero`() {
        assertEquals(0.0, haversineMeters(12.3, 45.6, 12.3, 45.6), 1e-9)
    }

    @Test
    fun `haversineMeters is symmetric`() {
        val forward = haversineMeters(10.0, 20.0, 11.0, 21.5)
        val backward = haversineMeters(11.0, 21.5, 10.0, 20.0)
        assertEquals(forward, backward, 1e-9)
    }

    // ---- handlePoint ----

    @Test
    fun `handlePoint at the equator round trips through haversineMeters within a meter`() {
        val handle = handlePoint(0.0, 0.0, 1000f)
        assertEquals(0.0, handle.latitude, 0.0)
        val distance = haversineMeters(0.0, 0.0, handle.latitude, handle.longitude)
        assertTrue("expected within 1m of 1000, was $distance", Math.abs(distance - 1000.0) <= 1.0)
        assertTrue("handle longitude must be east of the center", handle.longitude > 0.0)
    }

    @Test
    fun `handlePoint at latitude 45 round trips through haversineMeters within a meter`() {
        val handle = handlePoint(45.0, 0.0, 1000f)
        val distance = haversineMeters(45.0, 0.0, handle.latitude, handle.longitude)
        assertTrue("expected within 1m of 1000, was $distance", Math.abs(distance - 1000.0) <= 1.0)
        assertTrue("handle longitude must be east of the center", handle.longitude > 0.0)
    }

    @Test
    fun `handlePoint at latitude 80 round trips through haversineMeters within a meter`() {
        val handle = handlePoint(80.0, 0.0, 1000f)
        val distance = haversineMeters(80.0, 0.0, handle.latitude, handle.longitude)
        assertTrue("expected within 1m of 1000, was $distance", Math.abs(distance - 1000.0) <= 1.0)
        assertTrue("handle longitude must be east of the center", handle.longitude > 0.0)
    }

    @Test
    fun `handlePoint wraps across the antimeridian and stays in range`() {
        val handle = handlePoint(0.0, 179.999, 1000f)
        assertTrue(handle.longitude in -180.0..180.0)
        assertTrue("expected a wrapped (negative) longitude, was ${handle.longitude}", handle.longitude < 0.0)
    }

    // ---- RadiusSpec.radiusFromHandle ----

    @Test
    fun `radiusFromHandle sanitizes the haversine distance to the step grid`() {
        val spec = RadiusSpec(50f, 1000f, 150f, 50f)
        val center = PinKey(10.0, 20.0)
        val handle = handlePoint(10.0, 20.0, 437f)
        assertEquals(450f, spec.radiusFromHandle(center, handle))
    }

    @Test
    fun `radiusFromHandle with a zero step rounds to the nearest whole meter`() {
        val spec = RadiusSpec(50f, 1000f, 150f, 0f)
        val center = PinKey(10.0, 20.0)
        val handle = handlePoint(10.0, 20.0, 437f)
        val result = spec.radiusFromHandle(center, handle)
        assertTrue("expected 437 +/- 1, was $result", Math.abs(result - 437f) <= 1f)
    }

    @Test
    fun `radiusFromHandle clamps a far handle to maxMeters`() {
        val spec = RadiusSpec(50f, 1000f, 150f, 50f)
        val center = PinKey(10.0, 20.0)
        val handle = handlePoint(10.0, 20.0, 5000f)
        assertEquals(1000f, spec.radiusFromHandle(center, handle))
    }

    @Test
    fun `radiusFromHandle clamps a near handle to minMeters`() {
        val spec = RadiusSpec(50f, 1000f, 150f, 50f)
        val center = PinKey(10.0, 20.0)
        val handle = handlePoint(10.0, 20.0, 10f)
        assertEquals(50f, spec.radiusFromHandle(center, handle))
    }

    // ---- MapLifecycleGate ----

    @Test
    fun `a fresh gate runs resume and reports it ran`() {
        val gate = MapLifecycleGate()
        var resumes = 0
        val ran = gate.resume { resumes++ }
        assertTrue(ran)
        assertEquals(1, resumes)
    }

    @Test
    fun `a second resume before any pause is a no-op`() {
        val gate = MapLifecycleGate()
        gate.resume {}
        var resumes = 0
        val ran = gate.resume { resumes++ }
        assertFalse(ran)
        assertEquals(0, resumes)
    }

    @Test
    fun `pause after resume runs once`() {
        val gate = MapLifecycleGate()
        gate.resume {}
        var pauses = 0
        val ran = gate.pause { pauses++ }
        assertTrue(ran)
        assertEquals(1, pauses)

        var secondPauses = 0
        val secondRan = gate.pause { secondPauses++ }
        assertFalse(secondRan)
        assertEquals(0, secondPauses)
    }

    @Test
    fun `pause on a fresh gate is a no-op`() {
        val gate = MapLifecycleGate()
        var pauses = 0
        val ran = gate.pause { pauses++ }
        assertFalse(ran)
        assertEquals(0, pauses)
    }

    @Test
    fun `resume, pause, resume leaves two resumes and one pause`() {
        val gate = MapLifecycleGate()
        var resumes = 0
        var pauses = 0
        gate.resume { resumes++ }
        gate.pause { pauses++ }
        gate.resume { resumes++ }
        assertEquals(2, resumes)
        assertEquals(1, pauses)
    }
}
