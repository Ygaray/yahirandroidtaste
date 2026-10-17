package io.github.ygaray.yahirandroidtaste.explorer

import io.github.ygaray.yahirandroidtaste.component.SourceContractTestSupport
import io.github.ygaray.yahirandroidtaste.component.isBlockedClassUserAgent
import io.github.ygaray.yahirandroidtaste.component.resolveOsmdroidUserAgent
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * POL-03 (Phase 28): the Explorer's PlaceMapPicker demos pass a hub-owned real agent rather than
 * the host package id, so a wire-level check discriminates the User-Agent fix. Plain JVM, no
 * Robolectric.
 */
class ExplorerMapUserAgentTest {

    private val expected = "YahirAndroidTasteExplorer/2.8 (+https://github.com/Ygaray/yahirandroidtaste)"

    private fun screenSource(): String {
        val relative = "src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt"
        var dir: File? = File(".").absoluteFile
        var depth = 0
        while (dir != null && depth < 8) {
            val candidate = File(dir, relative)
            if (candidate.isFile) return candidate.readText()
            dir = dir.parentFile
            depth++
        }
        error("Could not resolve $relative from ${File(".").absoluteFile}")
    }

    @Test
    fun `the constant is the probed real identifying agent`() {
        assertEquals(expected, EXPLORER_MAP_USER_AGENT)
        assertTrue(
            EXPLORER_MAP_USER_AGENT,
            Regex("""^[A-Za-z][A-Za-z0-9]*/[0-9.]+ \(\+https://\S+\)$""").matches(EXPLORER_MAP_USER_AGENT),
        )
    }

    @Test
    fun `the constant passes through the resolver unchanged for another host package`() {
        assertEquals(
            EXPLORER_MAP_USER_AGENT,
            resolveOsmdroidUserAgent(EXPLORER_MAP_USER_AGENT, "io.github.some.host", "1.0", "15"),
        )
    }

    @Test
    fun `the constant is not a blocked class agent`() {
        assertFalse(isBlockedClassUserAgent(EXPLORER_MAP_USER_AGENT))
    }

    @Test
    fun `both PlaceMapPicker demos pass the constant and none reads the host package name`() {
        val raw = screenSource()
        assertTrue("screen source suspiciously short", raw.length > 2000)
        val code = SourceContractTestSupport.stripComments(raw)
        assertEquals(2, SourceContractTestSupport.countOccurrences(code, "userAgent = EXPLORER_MAP_USER_AGENT"))
        assertFalse(code.contains(".packageName"))
        assertEquals(2, SourceContractTestSupport.countOccurrences(code, "PlaceMapPicker("))
    }
}
