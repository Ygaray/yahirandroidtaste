package io.github.ygaray.yahirandroidtaste.component

import java.io.DataOutputStream
import java.io.File

/**
 * Shared test fixture writer (Phase 129 DS-03 D-02, task 2) — produces a real `.bin` amplitude
 * samples file (a 4-byte count followed by that many 4-byte floats, the same layout
 * [readAmplitudeBars] consumes) on disk via [File.createTempFile]. Shared between
 * [AmplitudeBarsDecodeTest] and [VoiceCardClipListTest] so the byte layout used by a
 * well-formed fixture is written in exactly one place, per 129-03-PLAN.md task 2's own
 * instruction not to duplicate it across two test files.
 */
internal fun writeAmplitudeSamplesFile(values: List<Float>): File {
    val file = File.createTempFile("amplitude_bars_test", ".bin")
    file.deleteOnExit()
    DataOutputStream(file.outputStream().buffered()).use { dos ->
        dos.writeInt(values.size)
        values.forEach { dos.writeFloat(it) }
    }
    return file
}
