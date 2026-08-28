package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.DataOutputStream
import java.io.File

/**
 * Unit tests for the pure, `internal` [readAmplitudeBars] helper (Phase 129 DS-03 D-02,
 * 129-REVIEWS.md cycle-1 MEDIUM/T-129-07) — the helper extracted out of [VoiceCard]'s former
 * inline decode so the shared [WaveformCanvas]'s renderability is proven with **real bytes**
 * inside this phase, not deferred to Phase 133.
 *
 * The helper is JVM-only (no Compose runtime, no Robolectric), so this runs as a plain JUnit4
 * class — the same precedent [SwipeThresholdTest][io.github.ygaray.yahirandroidtaste.modifier.SwipeThresholdTest]
 * follows for another `internal` main-source helper.
 *
 * The malformed-header case (T-129-07) is the actual behavioral proof of the corrupt-header
 * allocation guard: a real fixture `.bin` whose declared count far exceeds what its byte length
 * can hold must decode to an empty list **without throwing or exhausting memory**, paired with a
 * well-formed-file case asserting a non-empty result so the empty-result assertions cannot pass
 * vacuously against a decode that always returns empty.
 */
class AmplitudeBarsDecodeTest {

    /** Writes a `.bin` with a declared count that lies about the actual payload that follows. */
    private fun writeSamplesFileWithDeclaredCount(declaredCount: Int, actualValues: List<Float>): File {
        val file = File.createTempFile("amplitude_bars_decode_test_malformed", ".bin")
        file.deleteOnExit()
        DataOutputStream(file.outputStream().buffered()).use { dos ->
            dos.writeInt(declaredCount)
            actualValues.forEach { dos.writeFloat(it) }
        }
        return file
    }

    @Test
    fun `well-formed file with more samples than the target downsamples to a non-empty bounded result`() {
        val samples = List(200) { i -> (i % 10) / 10f }
        val file = writeAmplitudeSamplesFile(samples)

        val bars = readAmplitudeBars(file.absolutePath, targetBars = 24)

        assertTrue("Expected a non-empty bar list", bars.isNotEmpty())
        assertTrue("Bar count must never exceed the requested target", bars.size <= 24)
        assertTrue("Every bar value must lie in 0f..1f", bars.all { it in 0f..1f })
    }

    @Test
    fun `well-formed file with fewer samples than the target returns the samples unchanged`() {
        val samples = listOf(0.1f, 0.5f, 0.9f)
        val file = writeAmplitudeSamplesFile(samples)

        val bars = readAmplitudeBars(file.absolutePath, targetBars = 24)

        assertEquals(samples, bars)
    }

    @Test
    fun `malformed header declaring far more floats than the file can hold decodes to empty without throwing`() {
        // Declare 10 million floats but only actually write 3 — the count-versus-file-length
        // check must catch this BEFORE any List(count) { ... } allocation is attempted.
        val file = writeSamplesFileWithDeclaredCount(
            declaredCount = 10_000_000,
            actualValues = listOf(0.1f, 0.2f, 0.3f)
        )

        val bars = readAmplitudeBars(file.absolutePath, targetBars = 24)

        assertEquals(
            "A corrupt/hostile header must degrade to an empty bar list, never throw or hang",
            emptyList<Float>(),
            bars
        )
    }

    @Test
    fun `payload truncated mid-float decodes to empty without throwing`() {
        val file = File.createTempFile("amplitude_bars_decode_test_truncated", ".bin")
        file.deleteOnExit()
        DataOutputStream(file.outputStream().buffered()).use { dos ->
            dos.writeInt(5)
            dos.writeFloat(0.1f)
            dos.writeFloat(0.2f)
            // Declares 5 floats but only writes 2 full ones plus 1 stray byte — truncated mid-float.
            dos.writeByte(0)
        }

        val bars = readAmplitudeBars(file.absolutePath, targetBars = 24)

        assertEquals(emptyList<Float>(), bars)
    }

    @Test
    fun `nonexistent path decodes to empty without throwing`() {
        val bars = readAmplitudeBars("/tmp/does-not-exist-${System.nanoTime()}.bin", targetBars = 24)

        assertEquals(emptyList<Float>(), bars)
    }

    @Test
    fun `null path decodes to empty without throwing`() {
        val bars = readAmplitudeBars(null, targetBars = 24)

        assertEquals(emptyList<Float>(), bars)
    }
}
