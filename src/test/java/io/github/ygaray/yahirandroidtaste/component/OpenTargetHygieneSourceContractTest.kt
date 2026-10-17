package io.github.ygaray.yahirandroidtaste.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase-wide hygiene contract for the go-to-item work and the pill fixes (T-27-01, T-27-16, T-27-17).
 * Source text of the eight new or changed library files, comments stripped: the opaque target key is
 * never read, nothing logs or prints, the three new files declare nothing public, the new tags avoid
 * the documented prefixes, and the Classic dispatch literals still occur exactly once.
 */
class OpenTargetHygieneSourceContractTest {

    private val root = "src/main/java/io/github/ygaray/yahirandroidtaste/"

    private val files = listOf(
        "component/OpenTargetRow.kt",
        "component/OutcomeSheet.kt",
        "component/OutcomeSheetReceipt.kt",
        "component/HandledByDot.kt",
        "component/HandledByPillShape.kt",
        "feedback/UndoCenterScreen.kt",
        "model/VoiceOutcomeUiState.kt",
        "model/BatchRowResultUiModel.kt"
    )

    private fun raw(path: String): String = File(root + path).readText()

    private fun stripped(path: String): String = SourceContractTestSupport.stripComments(raw(path))

    private fun String.compact(): String = filterNot { it.isWhitespace() }

    @Test
    fun noFileReadsTheOpaqueTargetKey() {
        // A member access of the key property; a named argument written `key =` is allowed (the Undo
        // Center list uses one).
        val keyAccess = Regex("""\.key\b""")
        files.forEach { path ->
            val hit = keyAccess.find(stripped(path))
            assertTrue("$path reads a .key member: ${hit?.value}", hit == null)
        }
    }

    @Test
    fun noFileLogsOrPrints() {
        files.forEach { path ->
            val source = stripped(path)
            assertTrue("$path logs", !Regex("""\bLog\.""").containsMatchIn(source))
            assertTrue("$path prints", !source.contains("println("))
        }
    }

    @Test
    fun theThreeNewFilesDeclareNothingPublic() {
        val publicStart = Regex(
            """^(fun |class |object |val |var |interface |enum |const val |data class |open class |abstract class |sealed )"""
        )
        listOf("component/OpenTargetRow.kt", "component/HandledByDot.kt", "component/HandledByPillShape.kt").forEach { path ->
            val offending = raw(path).lines().filter { publicStart.containsMatchIn(it) }
            assertTrue("$path declares public top-level code: $offending", offending.isEmpty())
        }
    }

    @Test
    fun theOpenTargetTagsAvoidTheDocumentedPrefixes() {
        val source = stripped("component/OpenTargetRow.kt")
        val tags = listOf("OPEN_TARGET_ROW_TEST_TAG", "OPEN_TARGET_CHEVRON_TEST_TAG").map { name ->
            val match = Regex("""const val $name\s*=\s*"([^"]+)"""").find(source)
            checkNotNull(match) { "constant $name not found" }.groupValues[1]
        }
        assertEquals(2, tags.size)
        tags.forEach { tag ->
            listOf("outcome_sheet_", "receipt_row_", "provider_key_").forEach { prefix ->
                assertTrue("tag $tag must not start with $prefix", !tag.startsWith(prefix))
            }
        }
    }

    @Test
    fun theClassicDispatchLiteralsAreStillExactlyOnce() {
        val source = stripped("component/OutcomeSheet.kt").compact()
        listOf(
            "SheetScaffold(onDismissRequest=onDismissRequest,modifier=modifier,sheetState=sheetState){",
            "OutcomeSheetContent(outcome,colors=colors)",
            "SheetScaffold("
        ).forEach { literal ->
            assertEquals("literal $literal", 1, SourceContractTestSupport.countOccurrences(source, literal))
        }
    }
}
