package io.github.ygaray.yahirandroidtaste.component

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 20 source-scan invariants (T-20-14, T-20-15): plain JVM, no Robolectric.
 *
 * Guards that the tone-foundation atoms keep the hub reusable and safe:
 *  - the library never exposes test tags as resource ids and declares no Hilt application host or
 *    entry point (consumer-owned, see INTEGRATION.md);
 *  - the files added or widened in this phase import only AndroidX, Kotlin, Java, Android and the
 *    hub itself (the one-way dependency litmus), and hold no color literal constructor (theme roles
 *    or caller colors only);
 *  - the four new composables keep the declaration shape the registry drift guards parse, and each
 *    carries its stable tag literal (D-10).
 *
 * The forbidden identifiers are assembled from fragments so this file never contains them whole.
 */
class ToneFoundationInvariantsTest {

    private val resourceIdFlag = "testTagsAs" + "ResourceId"
    private val hiltHostNames = listOf("Hilt" + "AndroidApp", "Android" + "EntryPoint")

    private val allowedImportPrefixes = listOf(
        "androidx.",
        "kotlin.",
        "kotlinx.",
        "java.",
        "javax.",
        "android.",
        "io.github.ygaray.yahirandroidtaste."
    )

    private val phaseFiles = listOf(
        "theme/ToneTokens.kt",
        "component/ToneCard.kt",
        "component/RoundIconButton.kt",
        "component/StackedProgressBar.kt",
        "component/PillChip.kt",
        "component/DynamicActionButton.kt",
        "component/ReceiptRow.kt",
        "component/SegmentedOptionSelector.kt",
        "component/SheetScaffold.kt"
    )

    /** Gradle runs unit tests with the project directory as the working directory. */
    private fun sourceRoot(): File {
        val relative = "src/main/java/io/github/ygaray/yahirandroidtaste"
        val direct = File(relative)
        if (direct.isDirectory) return direct

        var dir: File? = File(".").absoluteFile
        var depth = 0
        while (dir != null && depth < 8) {
            val candidate = File(dir, relative)
            if (File(dir, "build.gradle.kts").isFile && candidate.isDirectory) return candidate
            dir = dir.parentFile
            depth++
        }
        error("Could not resolve $relative from ${File(".").absoluteFile}")
    }

    private fun read(relativePath: String): String = File(sourceRoot(), relativePath).readText()

    private fun code(relativePath: String): String =
        SourceContractTestSupport.stripComments(read(relativePath))

    @Test
    fun `the library never sets the resource id tag flag and declares no Hilt host`() {
        val files = sourceRoot().walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        assertTrue("source scan found only ${files.size} files; the source root is wrong", files.size > 20)

        val offenders = files.flatMap { file ->
            val src = SourceContractTestSupport.stripComments(file.readText())
            buildList {
                if (src.contains(resourceIdFlag)) add("${file.name}: $resourceIdFlag")
                hiltHostNames.filter { src.contains(it) }.forEach { add("${file.name}: $it") }
            }
        }
        assertTrue("forbidden identifiers found: $offenders", offenders.isEmpty())
    }

    @Test
    fun `phase files import only AndroidX, Kotlin, Java, Android and the hub`() {
        val offenders = phaseFiles.flatMap { path ->
            val imports = read(path).lines().filter { it.startsWith("import ") }
            assertTrue("$path has no imports; the scan is vacuous", imports.isNotEmpty())
            imports
                .map { it.removePrefix("import ").trim() }
                .filter { name -> allowedImportPrefixes.none { name.startsWith(it) } }
                .map { "$path: $it" }
        }
        assertTrue("imports outside the allowed prefixes: $offenders", offenders.isEmpty())
    }

    @Test
    fun `phase files hold no color literal constructor`() {
        val offenders = phaseFiles.filter { code(it).contains("Color(0x") }
        assertTrue("color literal constructor found in: $offenders", offenders.isEmpty())
    }

    @Test
    fun `new composables keep the column-0 declaration shape the registry guards parse`() {
        val names = mapOf(
            "component/ToneCard.kt" to "ToneCard",
            "component/RoundIconButton.kt" to "RoundIconButton",
            "component/StackedProgressBar.kt" to "StackedProgressBar",
            "component/PillChip.kt" to "PillChip",
            "component/ReceiptRow.kt" to "ReceiptRow"
        )
        names.forEach { (path, name) ->
            val src = read(path)
            assertTrue(
                "$path must declare '@Composable' alone on a column-0 line, then 'fun $name(' on the next",
                src.contains("\n@Composable\nfun $name(") || src.startsWith("@Composable\nfun $name(")
            )
            assertTrue("$path must not use the one-line '@Composable fun' form", !src.contains("@Composable fun "))
        }
    }

    @Test
    fun `stable tag literals are present in each atom source`() {
        val tags = mapOf(
            "component/ToneCard.kt" to "tone_card",
            "component/RoundIconButton.kt" to "round_icon_button",
            "component/StackedProgressBar.kt" to "stacked_progress_bar",
            "component/PillChip.kt" to "pill_chip"
        )
        tags.forEach { (path, tag) ->
            assertTrue("$path must contain the tag literal \"$tag\"", code(path).contains("\"$tag\""))
        }
    }
}
