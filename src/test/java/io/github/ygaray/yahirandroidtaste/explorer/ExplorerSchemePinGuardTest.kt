package io.github.ygaray.yahirandroidtaste.explorer

import io.github.ygaray.yahirandroidtaste.component.SourceContractTestSupport
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * D-01 [scheme-pin] permanent guard (plain JVM, no Robolectric): every Explorer page renders under
 * the static hub scheme because every wrap goes through `ExplorerTheme`, never the library theme
 * directly. A future screen that wraps `YahirAndroidTasteTheme(...)` itself would silently bring
 * dynamic color (and the wallpaper warn tone) back; this scan fails the build instead.
 */
class ExplorerSchemePinGuardTest {

    private val expectedScreenFiles = listOf(
        "ExplorerActivity.kt",
        "ExplorerEntry.kt",
        "ComponentDetailScreen.kt",
        "ExplorerIndexScreen.kt",
        "TokenBrowserScreen.kt",
        "CardsFamilyScreen.kt",
        "ChipsFamilyScreen.kt",
        "SheetsFamilyScreen.kt",
        "ButtonsFabFamilyScreen.kt",
        "PickersFamilyScreen.kt",
        "FeedbackFamilyScreen.kt",
        "EmptyStateFamilyScreen.kt",
        "ProgressFamilyScreen.kt",
        "TactileFoundationFamilyScreen.kt",
        "VoiceCommandFamilyScreen.kt",
        "ReleaseDemos.kt"
    )

    private fun explorerDir(): File {
        val relative = "src/main/java/io/github/ygaray/yahirandroidtaste/explorer"
        val direct = File(relative)
        if (direct.isDirectory) return direct
        var dir: File? = File(".").absoluteFile
        var depth = 0
        while (dir != null && depth < 8) {
            val candidate = File(dir, relative)
            if (candidate.isDirectory) return candidate
            dir = dir.parentFile
            depth++
        }
        error("Could not resolve the explorer source directory from ${File(".").absoluteFile}")
    }

    private fun explorerFiles(): List<File> =
        explorerDir().listFiles { f -> f.isFile && f.extension == "kt" }.orEmpty().sortedBy { it.name }

    private fun stripped(file: File): String = SourceContractTestSupport.stripComments(file.readText())

    private fun squashed(file: File): String = stripped(file).filterNot { it.isWhitespace() }

    private val directWrap = Regex("""YahirAndroidTasteTheme\s*[({]""")

    @Test
    fun noExplorerFileOutsideTheHelperWrapsTheLibraryTheme() {
        val files = explorerFiles()
        assertTrue("source scan found only ${files.size} explorer files; the source root is wrong", files.size > 15)

        val offenders = files
            .filter { it.name != "ExplorerTheme.kt" }
            .filter { directWrap.containsMatchIn(stripped(it)) }
            .map { it.name }
        assertTrue(
            "these explorer files wrap the library theme directly instead of ExplorerTheme: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun theHelperPinsTheStaticScheme() {
        val helper = File(explorerDir(), "ExplorerTheme.kt")
        assertTrue("ExplorerTheme.kt is missing", helper.isFile)
        val text = squashed(helper)
        assertTrue("the helper must pass dynamicColor = false", text.contains("dynamicColor=false"))
        assertTrue("the helper must call the library theme", text.contains("YahirAndroidTasteTheme("))
        assertTrue("the helper must stay internal", text.contains("internalfunExplorerTheme("))
    }

    @Test
    fun everyExpectedScreenCallsTheHelper() {
        val missing = expectedScreenFiles.filterNot { name ->
            val file = File(explorerDir(), name)
            file.isFile && stripped(file).contains("ExplorerTheme(")
        }
        assertTrue("these explorer files do not call ExplorerTheme(: $missing", missing.isEmpty())
    }

    @Test
    fun theNotFoundFallbackIsWrapped() {
        val entry = File(explorerDir(), "ExplorerEntry.kt")
        assertTrue("ExplorerEntry.kt is missing", entry.isFile)
        assertTrue(
            "the detail/{component} not-found EmptyState must sit inside ExplorerTheme(themeMode)",
            squashed(entry).contains("ExplorerTheme(themeMode){EmptyState(")
        )
    }
}
