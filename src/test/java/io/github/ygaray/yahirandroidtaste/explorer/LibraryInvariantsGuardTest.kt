package io.github.ygaray.yahirandroidtaste.explorer

import io.github.ygaray.yahirandroidtaste.component.SourceContractTestSupport
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * INV-03 permanent guard (Phase 24, plain JVM, no Robolectric): the library keeps its one-way
 * dependency, hosts no application and names no consumer concept, enforced on every test run instead
 * of promised in CLAUDE.md.
 *
 *  - every import of every `.kt` under `src/main` is rooted in the allowed set of the charter
 *    (Android SDK, AndroidX and Compose, Kotlin and kotlinx, java and javax, Hilt/Dagger bindings,
 *    Coil, osmdroid, the reorderable library and the library's own root);
 *  - the manifest declares exactly one `activity` (the Explorer) and an `application` element with no
 *    `android:name`, so the library can never become the Hilt host;
 *  - comment-stripped code names no consumer package or app identifier;
 *  - the Explorer demo copy file `ReleaseDemos.kt` holds no consumer noun, no domain noun and no
 *    key-looking prefix.
 *
 * Every forbidden identifier is assembled from fragments so this file never contains one whole.
 */
class LibraryInvariantsGuardTest {

    private val allowedImportPrefixes = listOf(
        "androidx.",
        "android.",
        "kotlin.",
        "kotlinx.",
        "java.",
        "javax.",
        "dagger.",
        "coil3.",
        "org.osmdroid.",
        "sh.calvin.reorderable.",
        "io.github.ygaray.yahirandroidtaste."
    )

    private val consumerAppFragments = listOf("second" + "brain", "cal" + "tracker")
    private val consumerPackageFragments = listOf("com.example." + "second", "com." + "cal" + "tracker")
    private val domainNounFragments = listOf("calor" + "ie", "me" + "al", "mac" + "ro", "gro" + "cery", "mind" + "map")
    private val keyPrefix = "s" + "k-"

    private fun projectRoot(): File {
        var dir: File? = File(".").absoluteFile
        var depth = 0
        while (dir != null && depth < 8) {
            if (File(dir, "build.gradle.kts").isFile && File(dir, "src/main").isDirectory) return dir
            dir = dir.parentFile
            depth++
        }
        error("Could not resolve the project root from ${File(".").absoluteFile}")
    }

    private fun sourceRoot(): File {
        val relative = "src/main/java/io/github/ygaray/yahirandroidtaste"
        val direct = File(relative)
        if (direct.isDirectory) return direct
        return File(projectRoot(), relative)
    }

    private fun mainKotlinFiles(): List<File> =
        sourceRoot().walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    @Test
    fun `every import in src main is rooted in the allowed set`() {
        val files = mainKotlinFiles()
        assertTrue("source scan found only ${files.size} files; the source root is wrong", files.size > 100)

        val offenders = files.flatMap { file ->
            file.readLines()
                .map { it.trim() }
                .filter { it.startsWith("import ") }
                .map { it.removePrefix("import ").trim() }
                .filterNot { import -> allowedImportPrefixes.any { import.startsWith(it) } }
                .map { "${file.name}: import $it" }
        }
        assertTrue("imports outside the allowed set (one-way dependency): $offenders", offenders.isEmpty())

        val importCount = files.sumOf { file -> file.readLines().count { it.trim().startsWith("import ") } }
        assertTrue("import scan saw only $importCount imports; the scan is vacuous", importCount > 1000)
    }

    @Test
    fun `the manifest declares exactly one activity and no application class`() {
        val manifest = File(projectRoot(), "src/main/AndroidManifest.xml").readText()
        val withoutComments = manifest.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")

        val activities = Regex("<activity\\b[^>]*>", RegexOption.DOT_MATCHES_ALL).findAll(withoutComments).toList()
        assertEquals("exactly one activity element", 1, activities.size)
        assertTrue(
            "the only activity is the Explorer",
            activities.single().value.contains("android:name=\".explorer.ExplorerActivity\"")
        )

        val application = Regex("<application\\b[^>]*>", RegexOption.DOT_MATCHES_ALL).findAll(withoutComments).toList()
        assertEquals("exactly one application element", 1, application.size)
        assertTrue(
            "the application element must not name an Application class (the consumer owns the host)",
            !application.single().value.contains("android:name")
        )

        listOf("service", "receiver", "provider").forEach { component ->
            assertTrue("the library declares no <$component>", !Regex("<$component\\b").containsMatchIn(withoutComments))
        }
    }

    @Test
    fun `code names no consumer package or app identifier`() {
        val offenders = mainKotlinFiles().flatMap { file ->
            val code = SourceContractTestSupport.stripComments(file.readText()).lowercase()
            (consumerAppFragments + consumerPackageFragments)
                .filter { code.contains(it) }
                .map { "${file.name}: $it" }
        }
        assertTrue("consumer identifiers in code (one-way dependency): $offenders", offenders.isEmpty())
    }

    @Test
    fun `the release demo copy holds no consumer noun and no key-looking prefix`() {
        val demos = File(sourceRoot(), "explorer/ReleaseDemos.kt")
        assertTrue("ReleaseDemos.kt must exist (vacuity guard)", demos.isFile)
        val code = SourceContractTestSupport.stripComments(demos.readText())
        assertTrue("ReleaseDemos.kt is unexpectedly short (${code.length} chars)", code.length > 2000)

        val lowered = code.lowercase()
        val nouns = (consumerAppFragments + domainNounFragments).filter { lowered.contains(it) }
        assertTrue("consumer or domain nouns in the demo copy: $nouns", nouns.isEmpty())

        val keyLike = Regex("(?<![A-Za-z0-9])" + Regex.escape(keyPrefix)).findAll(code).count()
        assertEquals("a key-looking prefix in the demo copy", 0, keyLike)
    }
}
