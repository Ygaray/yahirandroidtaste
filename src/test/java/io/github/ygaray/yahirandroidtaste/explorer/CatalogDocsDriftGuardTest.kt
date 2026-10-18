package io.github.ygaray.yahirandroidtaste.explorer

import io.github.ygaray.yahirandroidtaste.component.SourceContractTestSupport
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CAT-03 permanent guard (Phase 24, plain JVM, no Robolectric): the documentation cannot lie about
 * the catalog again. It compares `API.md`, `README.md` and `INTEGRATION.md` with the live
 * [ComponentRegistry], with `api.txt` and with the tag literals found in `src/main`:
 *
 *  - the five v2.6.0 composables are registered, once each, in their families;
 *  - the registered, unregistered and total counts, and the per-family counts of the "Surface at a
 *    glance" table, equal the live registry;
 *  - every public type and composable new since v2.5.1 (the list below, derived from
 *    `git diff v2.5.1 -- api.txt`) is named in `API.md` and still exists in `api.txt`;
 *  - every library test tag with a stable prefix is documented in `API.md` either as stable API or
 *    as a test-only hook, never both; the 17 VTAG-01 tags, the two confirmation tags and every
 *    `provider_key_*` tag are stable; explorer-only `demo_` tags and the internal kebab-case undo
 *    tags are in neither list;
 *  - `INTEGRATION.md` names the v2.6.0 entry points;
 *  - the `### Additions in v2.8.0` section of `API.md` names every v2.8.0 target-and-stack symbol
 *    (Phase 29 tracer) inside that section, and `api.txt` still declares each one.
 */
class CatalogDocsDriftGuardTest {

    /** Public composables and types new since v2.5.1; `Dimens.X` entries are the token groups. */
    private val newInV260 = listOf(
        "BarSegment", "OutcomeSheetColors", "ProviderKeyActions", "ProviderKeyLabels", "SavedKeyUiModel",
        "SheetActionUiModel", "ToneColors", "UndoGroupRowUiModel", "ActionButtonEmphasis", "ContentTone",
        "FailureTone", "OutcomeBadge", "OutcomeSheetStyle", "SegmentedOptionStyle", "OutcomeSheetDefaults",
        "ProviderKeyDefaults", "ToneDefaults", "PillChip", "ReceiptRow", "RoundIconButton",
        "StackedProgressBar", "ToneCard",
        "Dimens.ActionButton", "Dimens.OutcomeReceipt", "Dimens.PillChip", "Dimens.ReceiptRow",
        "Dimens.RoundIconButton", "Dimens.SegmentedPill", "Dimens.StackedProgressBar"
    )

    /** Public types and functions of the v2.8.0 target-and-stack half (Phase 29, SHIP-05). */
    private val newInV280 = listOf(
        "FeedbackTarget", "FeedbackResult", "FeedbackStackState", "FeedbackStackHost",
        "rememberFeedbackStackState"
    )

    private val v280Heading = "### Additions in v2.8.0"

    /** The 17 tags of VTAG-01 (Y-14). */
    private val vtag01 = listOf(
        "outcome_sheet_title", "outcome_sheet_badge", "outcome_sheet_title_actions", "outcome_sheet_transcript",
        "outcome_sheet_action_bar", "outcome_sheet_action_start", "outcome_sheet_action_end",
        "outcome_sheet_failure_surface", "outcome_sheet_failure_tip", "outcome_sheet_handled_by",
        "receipt_row_summary", "receipt_row_trailing_action", "receipt_row_expanded_content",
        "stacked_progress_bar", "tone_card", "round_icon_button", "pill_chip"
    )

    private val singleTags = setOf(
        "stacked_progress_bar", "stacked_progress_bar_segment", "tone_card", "round_icon_button", "pill_chip"
    )
    private val tagPrefixes = listOf("outcome_sheet_", "receipt_row_", "provider_key_")

    private val statsHeading = "## Stable test tags (documented API)"
    private val hooksHeading = "### Not part of the contract (test-only hooks)"

    private fun projectRoot(): File {
        var dir: File? = File(".").absoluteFile
        var depth = 0
        while (dir != null && depth < 8) {
            if (File(dir, "build.gradle.kts").isFile && File(dir, "API.md").isFile) return dir
            dir = dir.parentFile
            depth++
        }
        error("Could not resolve the project root from ${File(".").absoluteFile}")
    }

    private fun doc(name: String): String {
        val text = File(projectRoot(), name).readText()
        assertTrue("$name is empty (vacuity guard)", text.length > 500)
        return text
    }

    private fun wholeWord(name: String): Regex =
        Regex("(?<![A-Za-z0-9_])" + Regex.escape(name) + "(?![A-Za-z0-9_])")

    private val registered get() = ComponentRegistry.entries.size
    private val unregistered get() = ComponentRegistry.INTENTIONALLY_UNREGISTERED.size
    private val total get() = registered + unregistered

    // ---------------------------------------------------------------------------------------
    // Registration (CAT-03)
    // ---------------------------------------------------------------------------------------

    @Test
    fun `the five v2_6_0 composables are registered once in their families`() {
        val expected = mapOf(
            "ToneCard" to ExplorerFamilies.CARDS,
            "ReceiptRow" to ExplorerFamilies.CARDS,
            "PillChip" to ExplorerFamilies.CHIPS,
            "RoundIconButton" to ExplorerFamilies.BUTTONS_FAB,
            "StackedProgressBar" to ExplorerFamilies.PROGRESS_METRICS
        )
        assertTrue("registry is empty (vacuity guard)", ComponentRegistry.entries.size > 50)
        expected.forEach { (name, family) ->
            val matches = ComponentRegistry.entries.filter { it.name == name }
            assertEquals("$name must be registered exactly once", 1, matches.size)
            assertEquals("$name must be in family $family", family, matches.single().family)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Counts
    // ---------------------------------------------------------------------------------------

    @Test
    fun `API md states the live registered unregistered and total counts`() {
        val api = doc("API.md")
        assertTrue("API.md must state **$registered registered public composables**", api.contains("**$registered registered public composables**"))
        assertTrue("API.md must state **$unregistered intentionally-unregistered**", api.contains("**$unregistered intentionally-unregistered**"))
        assertTrue("API.md must state = **$total public composables total**", api.contains("= **$total public composables total**"))
        assertTrue(
            "API.md heading must state the unregistered count ($unregistered)",
            api.contains("## Intentionally-unregistered sub-parts ($unregistered)")
        )
    }

    @Test
    fun `README states the live registered unregistered and total counts`() {
        val readme = doc("README.md")
        assertTrue("README must state **$registered registered public composables**", readme.contains("**$registered registered public composables**"))
        assertTrue("README must state **$unregistered intentionally-unregistered**", readme.contains("**$unregistered intentionally-unregistered**"))
        assertTrue("README must state $total public composables total", readme.contains("$total public composables total"))
    }

    @Test
    fun `the Surface at a glance table carries the live per-family counts`() {
        val api = doc("API.md")
        val rows = api.lines()
            .filter { Regex("^\\|\\s*\\d+\\.\\s").containsMatchIn(it) }
            .map { line -> line.split("|").map { it.trim() } }
        assertEquals("the table must have one row per family", ExplorerFamilies.ORDERED_KEYS.size, rows.size)

        ExplorerFamilies.ORDERED_KEYS.forEachIndexed { index, (key, label) ->
            val live = ComponentRegistry.entries.count { it.family == key }
            val documented = rows[index][2].toIntOrNull()
            assertEquals("API.md count for family '$label' (row ${index + 1}: ${rows[index][1]})", live, documented)
        }
        assertEquals(
            "the per-family counts must add up to the registered total",
            registered,
            rows.sumOf { it[2].toInt() }
        )
    }

    // ---------------------------------------------------------------------------------------
    // New symbols
    // ---------------------------------------------------------------------------------------

    /**
     * Reads `api.txt`, waiting briefly for a concurrent `apiDump` (a Gradle invocation that lists
     * `apiDump` next to the tests rewrites the file while they run) to finish writing it.
     */
    private fun readApiTxt(): String {
        val file = File(projectRoot(), "api.txt")
        repeat(40) {
            if (file.isFile) {
                val text = runCatching { file.readText() }.getOrDefault("")
                if (text.length > 50_000) return text
            }
            Thread.sleep(250)
        }
        return if (file.isFile) file.readText() else error("api.txt is missing at ${file.absolutePath}")
    }

    @Test
    fun `every symbol new since v2_5_1 is documented in API md and still exists in api txt`() {
        assertTrue("new-symbol list is too short (vacuity guard)", newInV260.size >= 28)
        val api = doc("API.md")
        val apiTxt = readApiTxt()
        assertTrue("api.txt is unexpectedly small", apiTxt.length > 50_000)

        val missingFromDocs = newInV260.filterNot { name ->
            if (name.startsWith("Dimens.")) api.contains(name) else wholeWord(name).containsMatchIn(api)
        }
        assertTrue("API.md does not mention: $missingFromDocs", missingFromDocs.isEmpty())

        val goneFromApi = newInV260.filterNot { name ->
            val declared = Regex("(class|enum|interface) " + Regex.escape(name) + " [{i]").containsMatchIn(apiTxt)
            val composable = Regex("static void " + Regex.escape(name) + "\\(").containsMatchIn(apiTxt)
            declared || composable
        }
        assertTrue("no longer in api.txt (the list went stale): $goneFromApi", goneFromApi.isEmpty())
    }

    /** The API.md section that starts at [v280Heading] and ends before the next `###` or `##` heading. */
    private fun v280Section(api: String): String {
        val lines = api.lines()
        assertEquals(
            "API.md needs exactly one '$v280Heading' heading",
            1,
            lines.count { it == v280Heading }
        )
        val start = lines.indexOfFirst { it == v280Heading }
        val end = (start + 1 until lines.size).firstOrNull {
            lines[it].startsWith("### ") || lines[it].startsWith("## ")
        } ?: lines.size
        return lines.subList(start, end).joinToString("\n")
    }

    @Test
    fun `every symbol new in v2_8_0 is named in the API md v2_8_0 section and declared in api txt`() {
        assertEquals("the v2.8.0 symbol list changed size (vacuity guard)", 5, newInV280.size)
        val section = v280Section(doc("API.md"))
        assertTrue("the v2.8.0 section is too short (vacuity guard): ${section.length}", section.length > 1000)
        val apiTxt = readApiTxt()
        assertTrue("api.txt is unexpectedly small", apiTxt.length > 50_000)

        val missingFromSection = newInV280.filterNot { wholeWord(it).containsMatchIn(section) }
        assertTrue("the v2.8.0 section does not name: $missingFromSection", missingFromSection.isEmpty())

        val goneFromApi = newInV280.filterNot { name ->
            val type = Regex("(class|enum|interface) " + Regex.escape(name) + " [{i]").containsMatchIn(apiTxt)
            val function = Regex("static (void|\\S+) " + Regex.escape(name) + "\\(").containsMatchIn(apiTxt)
            type || function
        }
        assertTrue("no longer in api.txt (the list went stale): $goneFromApi", goneFromApi.isEmpty())
    }

    @Test
    fun `the API md v2_8_0 section names the member surface and the map agent guidance`() {
        val section = v280Section(doc("API.md"))
        assertTrue("the v2.8.0 section is too short (vacuity guard): ${section.length}", section.length > 4000)
        val apiTxt = readApiTxt()
        assertTrue("api.txt is unexpectedly small", apiTxt.length > 50_000)

        val members = listOf("onOpenTarget", "openableTarget", "targetLabel", "targetDetail", "supportingText")
        val words = members + listOf("emitTrackedWithUndo", "FeedbackTarget")
        val missingWords = words.filterNot { wholeWord(it).containsMatchIn(section) }
        assertTrue("the v2.8.0 section does not name: $missingWords", missingWords.isEmpty())

        val literals = listOf("AppName/version (+contact)", "INTERNET", "additionalHttpRequestProperties")
        val missingLiterals = literals.filterNot { section.contains(it) }
        assertTrue("the v2.8.0 section lacks the map agent guidance: $missingLiterals", missingLiterals.isEmpty())

        val goneFromApi = members.filterNot { wholeWord(it).containsMatchIn(apiTxt) }
        assertTrue("no longer in api.txt (the member list went stale): $goneFromApi", goneFromApi.isEmpty())
    }

    @Test
    fun `the API md UndoCenterScreen and OutcomeSheet rows name onOpenTarget`() {
        val lines = doc("API.md").lines()
        fun row(prefix: String): String {
            val matches = lines.filter { it.startsWith(prefix) }
            assertEquals("exactly one table row must start with $prefix", 1, matches.size)
            return matches.single()
        }
        assertTrue("UndoCenterScreen row must name onOpenTarget", wholeWord("onOpenTarget").containsMatchIn(row("| `UndoCenterScreen` | The Undo Center")))
        assertTrue("OutcomeSheet row must name onOpenTarget", wholeWord("onOpenTarget").containsMatchIn(row("| `OutcomeSheet` | Bottom sheet")))
        assertTrue("PlaceMapPicker row must carry the agent form", row("| `PlaceMapPicker` | Map-based").contains("AppName/version"))
    }

    // ---------------------------------------------------------------------------------------
    // Stable tags
    // ---------------------------------------------------------------------------------------

    private fun sourceRoot(): File = File(projectRoot(), "src/main/java/io/github/ygaray/yahirandroidtaste")

    /** Every string literal with a stable tag shape in library code outside the explorer package. */
    private fun scanLibraryTags(): Set<String> {
        val explorer = File(sourceRoot(), "explorer")
        val files = sourceRoot().walkTopDown()
            .filter { it.isFile && it.extension == "kt" && !it.startsWith(explorer) }
            .toList()
        assertTrue("tag scan saw only ${files.size} files", files.size > 80)
        // An animation `label = "..."` is a debugging name, not a tag, so it is skipped.
        val literal = Regex("(?<!label = )(?<!label=)\"([a-z][a-z0-9_]*)\"")
        val all = files.flatMap { file ->
            literal.findAll(SourceContractTestSupport.stripComments(file.readText())).map { it.groupValues[1] }.toList()
        }.toSet()
        val tags = all.filter { tag -> tag in singleTags || tagPrefixes.any { tag.startsWith(it) } }.toSet()
        assertTrue("tag scan found only ${tags.size} tags (vacuity guard)", tags.size > 40)
        return tags
    }

    private class TagSections(val stable: List<String>, val hooks: List<String>, val raw: String)

    private fun parseTagSections(api: String): TagSections {
        val lines = api.lines()
        val start = lines.indexOfFirst { it == statsHeading }
        assertTrue("API.md needs the heading '$statsHeading'", start >= 0)
        assertEquals("the heading '$statsHeading' must appear once", 1, lines.count { it == statsHeading })
        val end = (start + 1 until lines.size).firstOrNull { lines[it].startsWith("## ") } ?: lines.size
        val section = lines.subList(start, end)
        val hooksAt = section.indexOfFirst { it == hooksHeading }
        assertTrue("the section needs the sub-heading '$hooksHeading'", hooksAt >= 0)
        val row = Regex("^\\|\\s*`([^`]+)`\\s*\\|")
        fun tagsIn(part: List<String>) = part.mapNotNull { row.find(it)?.groupValues?.get(1) }
        return TagSections(
            stable = tagsIn(section.subList(0, hooksAt)),
            hooks = tagsIn(section.subList(hooksAt, section.size)),
            raw = section.joinToString("\n")
        )
    }

    @Test
    fun `every library tag with a stable prefix is documented as stable or as a test-only hook`() {
        val scanned = scanLibraryTags()
        val sections = parseTagSections(doc("API.md"))

        assertEquals("a tag is listed twice in the stable list", sections.stable.size, sections.stable.toSet().size)
        assertEquals("a tag is listed twice in the hook list", sections.hooks.size, sections.hooks.toSet().size)
        val both = sections.stable.toSet() intersect sections.hooks.toSet()
        assertTrue("tags listed as both stable and test-only: $both", both.isEmpty())

        val documented = sections.stable.toSet() + sections.hooks.toSet()
        assertTrue("tags in the source but not documented: ${scanned - documented}", (scanned - documented).isEmpty())
        assertTrue("tags documented but not in the source: ${documented - scanned}", (documented - scanned).isEmpty())
    }

    @Test
    fun `the Y-14 provider key and confirmation tags are in the stable list`() {
        val scanned = scanLibraryTags()
        val stable = parseTagSections(doc("API.md")).stable.toSet()

        assertEquals("VTAG-01 lists 17 tags", 17, vtag01.toSet().size)
        val required = vtag01.toSet() +
            setOf("outcome_sheet_confirmation_cancel", "outcome_sheet_confirmation_confirm") +
            scanned.filter { it.startsWith("provider_key_") }
        assertTrue("fewer than 6 provider_key_ tags found", scanned.count { it.startsWith("provider_key_") } >= 6)
        assertTrue("not in the stable list: ${required - stable}", (required - stable).isEmpty())
    }

    @Test
    fun `explorer-only demo tags and internal undo tags are in neither list`() {
        val sections = parseTagSections(doc("API.md"))
        val offenders = (sections.stable + sections.hooks).filter { it.startsWith("demo_") || it.startsWith("undo-") }
        assertTrue("explorer-only or internal tags are listed as API: $offenders", offenders.isEmpty())
        assertTrue(
            "the hook sub-list must say the undo- tags are internal",
            sections.raw.contains("undo-")
        )
    }

    // ---------------------------------------------------------------------------------------
    // INTEGRATION
    // ---------------------------------------------------------------------------------------

    @Test
    fun `the compatibility lead-ins list the same three positional source breaks`() {
        val breaks = listOf("UndoCenterScreen", "NeedsConfirmation", "SheetScaffold")
        val apiLead = doc("API.md").substringAfter("## 11. Tone foundation and Receipt types (v2.6.0)").substringBefore("### New types")
        val integrationLead = doc("INTEGRATION.md").substringAfter("### 5b. Wiring the v2.6.0").substringBefore("**(i)")
        assertTrue("API.md section 11 lead-in not found (vacuity guard)", apiLead.length in 200..3000)
        assertTrue("INTEGRATION.md 5b lead-in not found (vacuity guard)", integrationLead.length in 200..3000)
        breaks.forEach { name ->
            assertTrue("the API.md section 11 lead-in must name the positional break $name", apiLead.contains(name))
            assertTrue("the INTEGRATION.md 5b lead-in must name the positional break $name", integrationLead.contains(name))
        }
    }

    @Test
    fun `INTEGRATION md names the v2_6_0 entry points`() {
        val integration = doc("INTEGRATION.md")
        listOf(
            "OutcomeSheetStyle.Receipt",
            "ProviderKeyActions",
            "SavedKeyUiModel",
            "UndoGroupRowUiModel",
            "attemptUndoGroup"
        ).forEach { name ->
            assertTrue("INTEGRATION.md must name $name", integration.contains(name))
        }
    }
}
