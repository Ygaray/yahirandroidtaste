package io.github.ygaray.yahirandroidtaste.explorer

import io.github.ygaray.yahirandroidtaste.component.SourceContractTestSupport
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 28 hygiene contract (T-28-01, T-28-06, plain JVM, no Robolectric): the policy and privacy
 * promises of the POL-03 tile-agent work and the visibility of the Explorer additions are enforced on
 * every test run instead of only narrated.
 *
 * The assertions state invariants, never an exact file hash or a count of `load()` calls, so an
 * internal edit of the config file (for example loading osmdroid's configuration once per process)
 * does not break them. Every forbidden identifier is assembled from fragments so this file never
 * contains one whole.
 */
class Phase28HygieneSourceContractTest {

    private val root = "src/main/java/io/github/ygaray/yahirandroidtaste/"

    private val configPath = "component/PlaceMapOsmdroidConfig.kt"
    private val pickerPath = "component/PlaceMapPicker.kt"

    private fun mainKotlinFiles(): List<File> =
        File(root).walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    /** Source text without line comments, KDoc and block comments, so prose never trips a scan. */
    private fun code(file: File): String {
        val withoutBlocks = file.readText().replace(Regex("(?s)/\\*.*?\\*/"), "")
        return SourceContractTestSupport.stripComments(withoutBlocks)
    }

    private fun code(path: String): String = code(File(root + path))

    private fun explorerFiles(): List<File> =
        File(root + "explorer").listFiles { f -> f.isFile && f.extension == "kt" }!!.sortedBy { it.name }

    private fun count(haystack: String, needle: String): Int =
        SourceContractTestSupport.countOccurrences(haystack, needle)

    @Test
    fun placeMapConfigHasNoWipeNoCacheManagerNoExpiryOverrideAndNoRequestPropertyPrefsKey() {
        val source = code(configPath)
        assertTrue("config scan is vacuous", source.contains("fun configureOsmdroid("))

        val forbidden = mapOf(
            "whole-cache wipe call" to "purge" + "Cache",
            "cache manager type" to "Cache" + "Manager",
            "expiration override" to "xpiration",
            "expiry override" to "Expiry",
            "persisted request-property key family" to "additionalHttp" + "RequestProperty" + ".",
            "request-property prefs key name" to "osmdroid." + "additionalHttp",
            "string preference write" to "put" + "String(",
            "string-set preference write" to "put" + "StringSet(",
        )
        forbidden.forEach { (label, literal) ->
            assertTrue("$configPath names the $label ($literal)", !source.contains(literal))
        }

        // The hub-owned install flag is a boolean write, never a string.
        assertTrue("the install flag must be written as a boolean", source.contains("putBoolean("))
    }

    @Test
    fun exactlyOnePlaceholderPackageLiteralExistsInAllMainCode() {
        val prefix = "com." + "example."
        val files = mainKotlinFiles()
        assertTrue("source scan found only ${files.size} files; the source root is wrong", files.size > 100)

        val hits = files.associate { it.path to count(code(it), prefix) }.filterValues { it > 0 }
        assertEquals("the placeholder prefix must occur in exactly one file: $hits", 1, hits.size)
        assertEquals("exactly one occurrence overall: $hits", 1, hits.values.sum())
        assertTrue("the one literal must live in $configPath: $hits", hits.keys.single().endsWith(configPath))
    }

    @Test
    fun placeMapConfigWarnsExactlyOnce_andNoOtherLogLevelIsUsed() {
        val source = code(configPath)
        assertTrue("config scan is vacuous", source.contains("fun configureOsmdroid("))

        assertEquals("exactly one warning call", 1, count(source, "Log" + ".w("))
        assertEquals("every log call is the one warning", 1, Regex("""\bLog\.""").findAll(source).count())
        listOf("d", "i", "e", "v", "wtf").forEach { level ->
            assertTrue("$configPath uses log level $level", !source.contains("Log" + ".$level("))
        }
        assertTrue("$configPath prints", !source.contains("println("))
    }

    @Test
    fun pickerAndExplorerFilesNeverLogOrPrint() {
        val files = explorerFiles() + File(root + pickerPath)
        assertTrue("explorer scan found only ${files.size} files", files.size > 10)

        files.forEach { file ->
            val source = code(file)
            assertTrue("${file.name} logs", !Regex("""\bLog\.""").containsMatchIn(source))
            assertTrue("${file.name} uses Timber", !source.contains("Tim" + "ber."))
            assertTrue("${file.name} prints", !source.contains("println(") && !Regex("""\bprint\(""").containsMatchIn(source))
        }
    }

    @Test
    fun newTopLevelDeclarationsAreNeverPublic() {
        val publicStart = Regex(
            """^(public |inline |suspend |tailrec |operator |infix |open |abstract |sealed |data |enum |annotation |value |const |fun |class |object |val |var |interface |typealias )"""
        )
        listOf(configPath, "explorer/ExplorerTheme.kt", "explorer/ReleaseDemos.kt").forEach { path ->
            val source = code(path)
            assertTrue("$path scan is vacuous", source.lines().any { it.startsWith("internal ") || it.startsWith("private ") })
            val offending = source.lines().filter { publicStart.containsMatchIn(it) }
            assertTrue("$path declares public top-level code: $offending", offending.isEmpty())
        }
    }

    @Test
    fun explorerThemeHelperPassesDynamicColorFalse() {
        val source = code("explorer/ExplorerTheme.kt").filterNot { it.isWhitespace() }
        assertTrue("helper missing", source.contains("funExplorerTheme("))
        assertTrue("the helper must pin the static palette", source.contains("dynamicColor=false"))
        assertTrue("the helper must never enable dynamic color", !source.contains("dynamicColor=true"))
    }

    @Test
    fun theAgentFixIsInternalToTheConfigFile() {
        val source = code(pickerPath)
        assertTrue("picker scan is vacuous", source.contains("configureOsmdroid("))

        val forbidden = listOf(
            "additionalHttp" + "RequestProperties",
            "RequestProperty",
            "purge" + "PlaceholderTiles",
            "purge" + "Cache",
            "userAgent" + "Value",
        )
        forbidden.forEach { literal ->
            assertTrue("$pickerPath names $literal; the agent fix lives in the config file", !source.contains(literal))
        }
    }
}
