package io.github.ygaray.yahirandroidtaste.explorer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * GOV-02 drift guard (03-CONTEXT.md D-02/D-03): source-scans the same universe as
 * [ComponentRegistryDriftGuardTest] (every top-level yahirandroidtaste package EXCEPT `explorer/`)
 * for public top-level `@Composable` functions and asserts each name's **head token** (the leading
 * PascalCase word, e.g. `VoiceCard` -> `Voice`) is either an established, domain-agnostic
 * UI-primitive noun ([PRIMITIVE_NOUN_ALLOWLIST]) or an explicitly acknowledged, rationale-carrying
 * entry in [DOMAIN_VOCABULARY]. A name satisfying neither fails the build.
 *
 * This is a **fail-until-allowlisted** guard (D-02) — never advisory-only. It answers a different
 * question from [ComponentRegistryDriftGuardTest]'s coverage check ("is this composable registered
 * or intentionally unregistered?"): this guard asks "does this composable's name couple the hub to
 * consumer-domain vocabulary, and if so, has a human acknowledged that coupling with a rationale?"
 * [DOMAIN_VOCABULARY] is therefore a wholly independent allowlist from
 * [ComponentRegistry.INTENTIONALLY_UNREGISTERED] — never conflated with it, even though both mirror
 * the same `Map<String, String>` name -> rationale audit-trail shape.
 *
 * Detection is a **structural inverse allowlist** (D-03): [PRIMITIVE_NOUN_ALLOWLIST] seeds the
 * hub's own established UI-archetype nouns (the distinct trailing-word vocabulary of the live
 * corpus, per 03-RESEARCH.md Open Questions #3) rather than a consumer-term denylist — the guard
 * ships no consumer vocabulary itself, only the hub's own structural nouns.
 *
 * This is a plain JVM JUnit source-TEXT scan (no reflection) for the exact same reason as its
 * sibling guard: Compose's compiler adds synthetic `Composer`/`$changed`/`$default` parameters to
 * every `@Composable` function's compiled signature, which makes reflection-based detection
 * fragile. See [ComponentRegistryDriftGuardTest]'s own KDoc for the full rationale — the extraction
 * machinery below (`resolveModuleSourceRoot`, `extractPublicTopLevelComposableNames`,
 * `findDeclarationLineIndex`, `FUN_DECLARATION_REGEX`, `MAX_DECLARATION_LOOKAHEAD_LINES`) is
 * duplicated verbatim from that class rather than imported — it is `private fun`/`private val` on
 * that class, and 03-RESEARCH.md's "Don't Hand-Roll" table + this phase's Anti-Patterns section
 * explicitly direct duplication over a shared-utility refactor for this phase.
 *
 * Vacuous-pass guard (mirrors [ComponentRegistryDriftGuardTest]): the same three-tier
 * `assertTrue` sequence (non-empty `.kt` file scan, non-empty post-exclusion file scan, non-empty
 * extracted-name set) runs before drawing any coverage conclusion, so a broken working-
 * directory/source-root assumption fails loudly instead of vacuously passing on a zero-file scan.
 *
 * Red->green demonstration (03-02-SUMMARY.md records the actual manual run): temporarily add a
 * hypothetical public composable whose head token is in neither list (e.g. `ProjectCard` — head
 * token `Project`) to any non-`explorer` package; this test goes RED, listing the offending name.
 * Add it to [DOMAIN_VOCABULARY] with a one-line rationale, or confirm its head token belongs in
 * [PRIMITIVE_NOUN_ALLOWLIST] -> GREEN again.
 */
class DomainVocabularyDriftGuardTest {

    private val excludedPackages = setOf("explorer")

    @Test
    fun everyPublicComposableHeadTokenIsPrimitiveOrAcknowledgedDomainVocabulary() {
        val sourceRoot = resolveModuleSourceRoot()

        // Vacuous-pass guard FIRST (mirrors ComponentRegistryDriftGuardTest, RESEARCH.md Pitfall
        // 2): assert the scan actually found source files before trusting any conclusion drawn
        // from an empty scan.
        val allScannedKtFiles = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
        assertTrue(
            "Source scan found 0 .kt files under $sourceRoot — the working-directory/source-root " +
                "assumption broke. The drift guard would vacuously pass if this were not asserted; " +
                "failing loudly instead (mirrors ComponentRegistryDriftGuardTest / RESEARCH.md " +
                "Pitfall 2).",
            allScannedKtFiles.isNotEmpty()
        )

        val scannedPackageFiles = allScannedKtFiles.filter { file ->
            val relativePath = file.relativeTo(sourceRoot).invariantSeparatorsPath
            val topLevelPackage = relativePath.substringBefore("/")
            topLevelPackage !in excludedPackages
        }
        assertTrue(
            "Source scan found 0 .kt files outside the excluded packages ($excludedPackages) " +
                "inside $sourceRoot even though $sourceRoot itself is non-empty — the " +
                "package-path filter or source root is wrong. Failing loudly instead of " +
                "vacuously passing.",
            scannedPackageFiles.isNotEmpty()
        )

        val scannedComposableNames = scannedPackageFiles
            .flatMap { extractPublicTopLevelComposableNames(it) }
            .toSet()
        assertTrue(
            "Source scan outside the excluded packages found 0 public top-level @Composable " +
                "functions — the extraction logic or source root is wrong (expected dozens). " +
                "Failing loudly instead of vacuously passing.",
            scannedComposableNames.isNotEmpty()
        )

        val offendingNames = scannedComposableNames.filter { name ->
            headToken(name) !in PRIMITIVE_NOUN_ALLOWLIST && name !in DOMAIN_VOCABULARY.keys
        }.toSet()

        if (offendingNames.isNotEmpty()) {
            fail(
                "Found ${offendingNames.size} public top-level @Composable function(s) outside " +
                    "the excluded packages ($excludedPackages) whose leading name token is " +
                    "neither an established UI-primitive noun (PRIMITIVE_NOUN_ALLOWLIST) nor an " +
                    "acknowledged domain name (DOMAIN_VOCABULARY): ${offendingNames.sorted()} — " +
                    "add each to DOMAIN_VOCABULARY with a one-line rationale, or confirm its head " +
                    "token belongs in PRIMITIVE_NOUN_ALLOWLIST (D-02/D-03)."
            )
        }
    }

    /**
     * WR-02 regression: [headToken] must extract the leading all-caps acronym run as a single
     * token (not just its first letter) when a composable name's leading word is an acronym like
     * `URL`/`UI`/`API`, and must leave ordinary leading-word extraction unaffected. Verified
     * against the exact cases the finding reproduced live.
     */
    @Test
    fun headTokenExtractsLeadingAcronymRunAsASingleToken() {
        assertEquals("URL", headToken("URLPreviewCard"))
        assertEquals("UI", headToken("UIStateBadge"))
        assertEquals("API", headToken("APIKeyField"))
        // Ordinary (non-acronym) leading words are unaffected by the acronym branch.
        assertEquals("Card", headToken("CardBase"))
        assertEquals("Voice", headToken("VoiceCard"))
    }

    /**
     * Computes a composable name's head token — the FIRST/leading PascalCase word (03-RESEARCH.md
     * Open Question 2's corroborated reading, e.g. `VoiceCard` -> `Voice`, `CardBase` -> `Card`),
     * not the trailing/linguistic-head word. See this file's own KDoc for why: every domain-
     * flavored name in this codebase leads with its domain word and trails with a structural word,
     * so reading "head token" as trailing would make the guard structurally blind to every
     * domain-coupled name it exists to catch.
     */
    private fun headToken(name: String): String {
        val match = HEAD_TOKEN_REGEX.find(name)
        check(match != null) {
            "headToken: could not extract a leading PascalCase word from '$name' — the name " +
                "does not match the expected Composable-naming convention. Failing loudly " +
                "instead of silently treating this as an unclassifiable offender."
        }
        return match.value
    }

    /**
     * Resolves the `yahirandroidtaste` module's `src/main/java/.../yahirandroidtaste` source
     * root robustly (duplicated verbatim from [ComponentRegistryDriftGuardTest] — CWD-independent
     * walk-up-then-fallback source-root resolver, see that class's KDoc for the full rationale).
     */
    private fun resolveModuleSourceRoot(): File {
        val relativeSourceRoot = "src/main/java/io/github/ygaray/yahirandroidtaste"

        var dir: File? = File(".").absoluteFile
        var depth = 0
        while (dir != null && depth < 8) {
            if (dir.name == "yahirandroidtaste") {
                val candidateBuildFile = File(dir, "build.gradle.kts")
                val candidateSourceRoot = File(dir, relativeSourceRoot)
                if (candidateBuildFile.isFile && candidateSourceRoot.isDirectory) {
                    return candidateSourceRoot
                }
            }
            dir = dir.parentFile
            depth++
        }

        // Fall back to the Gradle-default relative path (Test task workingDir = project dir).
        val fallback = File(relativeSourceRoot)
        check(fallback.isDirectory) {
            "Could not resolve the yahirandroidtaste source root by walking up from the process " +
                "CWD (${File(".").absoluteFile}) looking for a yahirandroidtaste module " +
                "directory, nor via the Gradle-default relative path " +
                "($relativeSourceRoot, resolved absolute: ${fallback.absoluteFile}). The drift " +
                "guard cannot scan for @Composable coverage without a valid source root."
        }
        return fallback
    }

    /**
     * Extracts the names of public top-level `@Composable` functions declared in [file] via
     * source-text scanning (no reflection). Duplicated verbatim from
     * [ComponentRegistryDriftGuardTest] — see that class's KDoc for the full column-0/
     * private-internal/generic-type-param/extension-receiver handling rationale.
     */
    private fun extractPublicTopLevelComposableNames(file: File): List<String> {
        val lines = file.readLines()
        val names = mutableListOf<String>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val isTopLevelComposableAnnotation =
                line.isNotEmpty() && !line[0].isWhitespace() && line.trim() == "@Composable"

            if (isTopLevelComposableAnnotation) {
                val j = findDeclarationLineIndex(lines, i, file)
                val declLine = lines[j]
                val isTopLevelDeclaration = declLine.isNotEmpty() && !declLine[0].isWhitespace()
                val trimmedDecl = declLine.trim()
                val isPrivateOrInternal =
                    trimmedDecl.startsWith("private ") || trimmedDecl.startsWith("internal ")

                if (isTopLevelDeclaration && !isPrivateOrInternal) {
                    val match = FUN_DECLARATION_REGEX.find(trimmedDecl)
                    check(match != null) {
                        "extractPublicTopLevelComposableNames: line ${j + 1} of ${file.path} " +
                            "('$trimmedDecl') was located as the declaration for the @Composable " +
                            "at line ${i + 1} but does not match FUN_DECLARATION_REGEX — the " +
                            "extraction heuristic found a plausible 'fun' line but could not " +
                            "parse a function name from it. Failing loudly instead of silently " +
                            "dropping this composable from the coverage scan."
                    }
                    names.add(match.groupValues[1])
                }
            }
            i++
        }

        return names
    }

    /**
     * Finds the line index of the `fun` declaration that the `@Composable` at
     * [annotationLineIndex] decorates. Duplicated verbatim from
     * [ComponentRegistryDriftGuardTest] — see that class's KDoc for the full rationale (skips
     * blank lines, stacked annotations, and interleaved KDoc uniformly).
     */
    private fun findDeclarationLineIndex(lines: List<String>, annotationLineIndex: Int, file: File): Int {
        var j = annotationLineIndex + 1
        while (j < lines.size && j - annotationLineIndex <= MAX_DECLARATION_LOOKAHEAD_LINES) {
            val candidate = lines[j].trim()
            if (candidate.startsWith("fun ") || candidate.contains(" fun ")) {
                return j
            }
            j++
        }
        error(
            "extractPublicTopLevelComposableNames: found @Composable at " +
                "${file.path}:${annotationLineIndex + 1} but no line starting with (or " +
                "containing) 'fun ' within $MAX_DECLARATION_LOOKAHEAD_LINES lines afterward — " +
                "could not locate the decorated declaration (a stacked multi-line annotation " +
                "argument list, interleaved KDoc, or something else unrecognized?). Failing " +
                "loudly instead of silently dropping this composable from the coverage scan."
        )
    }

    private companion object {
        /**
         * Matches a top-level `fun` declaration, capturing the function name. Duplicated
         * verbatim from [ComponentRegistryDriftGuardTest] — tolerates an optional `public`
         * modifier, an optional generic type-parameter list (`<T>`), and an optional
         * extension-receiver type prefix (`ReceiverType.`) before the name.
         */
        val FUN_DECLARATION_REGEX = Regex(
            """^(?:public\s+)?fun\s+(?:<[^>]*>\s+)?(?:[A-Za-z_][\w.]*\.)?(\w+)\s*\("""
        )

        /**
         * Matches the leading PascalCase word of a composable name (the "head token"). Tries a
         * leading all-caps acronym run first (`[A-Z]+`, only when followed by another capital +
         * lowercase — i.e. the next word starting — or by end-of-string), falling back to the
         * ordinary single-capital-plus-lowercase-run word. Without the acronym branch,
         * `[A-Z][a-z0-9]*` alone extracts only a single letter from a name with a leading
         * acronym (`URLPreviewCard` -> "U", `UIStateBadge` -> "U", `APIKeyField` -> "A") because
         * `[a-z0-9]*` can match zero characters — a latent correctness bug (WR-02) that would
         * force a future generic composable with an acronym prefix into DOMAIN_VOCABULARY with a
         * false "consumer-domain noun" rationale. With the acronym branch: `URLPreviewCard` ->
         * "URL", `UIStateBadge` -> "UI", `APIKeyField` -> "API"; ordinary names are unaffected
         * (`CardBase` -> "Card", `VoiceCard` -> "Voice").
         */
        val HEAD_TOKEN_REGEX = Regex("[A-Z]+(?=[A-Z][a-z]|$)|[A-Z][a-z0-9]*")

        /**
         * Bound on how many lines [findDeclarationLineIndex] will scan forward past an
         * `@Composable` annotation looking for the `fun` line it decorates, before failing
         * loudly. Duplicated verbatim from [ComponentRegistryDriftGuardTest].
         */
        const val MAX_DECLARATION_LOOKAHEAD_LINES = 25

        /**
         * D-03: the hub's own established UI-archetype nouns, seeded from the distinct trailing
         * words of the live corpus's public top-level @Composable names (03-RESEARCH.md Open
         * Questions #3) — a structural inverse allowlist, never a consumer-term denylist. A head
         * token matching one of these is presumed domain-agnostic structure, not a consumer leak.
         *
         * Widened beyond RESEARCH.md's 34-word trailing-word seed (deviation Rule 1 — the seed
         * list's own day-one-green acceptance criterion, 03-02-PLAN.md must_haves, failed against
         * the live corpus without this widening): RESEARCH.md's list was derived from trailing
         * words only, but this guard's predicate tests the LEADING word (head token) of every
         * name, and a first live run surfaced 28 additional distinct leading words that are
         * equally generic, non-domain UI/structural descriptors (verified against every
         * component/feedback/modifier/theme composable's actual signature and purpose — none
         * names a specific consumer app's business object the way Voice/Album/Heat/Recording/Tag
         * do). Added below the original 34, kept in their own block for auditability.
         */
        val PRIMITIVE_NOUN_ALLOWLIST: Set<String> = setOf(
            // Original 34 — RESEARCH.md Open Questions #3, distinct trailing words.
            "Control", "Sheet", "Field", "Canvas", "State", "Dialog", "Bar", "Card", "Value",
            "Item", "Content", "Row", "Scaffold", "Swatch", "Grid", "View", "Chip", "Popup",
            "Picker", "Button", "Fab", "Badge", "Selector", "Ring", "Menu", "Overlay", "Preview",
            "Cue", "Editor", "Base", "Screen", "Theme", "Ladder", "Showcase",
            // Widening (this task, live-corpus first run) — additional leading words confirmed
            // generic/structural, not consumer-domain nouns: AccentColorPicker, AdaptiveMediaPreview,
            // AnimatedStatValue, AppChip, AttentionCue, BulkCreatePopup(Content), ClearableTextField,
            // ConfirmationDialog, CountBadge, CropOverlay, CycleSubTypeButton, DynamicActionButton,
            // ElevationLadder, EmptyState, ExpandableFab, FilterBar, GradientSwatch, HeroStatCard,
            // IconPickerGrid, ListCard(BottomSheet), MetricBar, NameAndTagsEditor, ProgressRing,
            // SegmentedOptionSelector, SortControl, TactileTypeShowcase, TextCard(BottomSheet),
            // UndoCenterScreen.
            "Accent", "Adaptive", "Animated", "App", "Attention", "Bulk", "Clearable",
            "Confirmation", "Count", "Crop", "Cycle", "Dynamic", "Elevation", "Empty",
            "Expandable", "Filter", "Gradient", "Hero", "Icon", "List", "Metric", "Name",
            "Progress", "Segmented", "Sort", "Tactile", "Text", "Undo"
        )

        /**
         * D-02's day-one grandfather list: every currently-known public top-level @Composable
         * whose head token carries genuine consumer-domain vocabulary (or a borderline
         * near-domain descriptor), each explicitly acknowledged with a one-line rationale so the
         * guard is green against the live corpus on day one while still gating any FUTURE
         * domain-coupled name for review — mirrors [ComponentRegistry.INTENTIONALLY_UNREGISTERED]'s
         * shape, but answers an independent question (never conflated with that map).
         */
        val DOMAIN_VOCABULARY: Map<String, String> = mapOf(
            "VoiceCard" to
                "Head token 'Voice' is a consumer-domain noun (voice recordings/clips) — " +
                "grandfathered per D-02's day-one allowlist.",
            "VoiceRenameTagsSheet" to
                "Head token 'Voice' is a consumer-domain noun — grandfathered per D-02's " +
                "day-one allowlist.",
            "AlbumCard" to
                "Head token 'Album' is a consumer-domain noun (photo/media albums) — " +
                "grandfathered per D-02's day-one allowlist.",
            "AlbumSourcePickerSheet" to
                "Head token 'Album' is a consumer-domain noun — grandfathered per D-02's " +
                "day-one allowlist.",
            "AlbumTitleConfirmSheet" to
                "Head token 'Album' is a consumer-domain noun — grandfathered per D-02's " +
                "day-one allowlist.",
            "HeatSwatch" to
                "Head token 'Heat' is a consumer-domain noun (mindmap relatedness heat ramp) — " +
                "grandfathered per D-02's day-one allowlist.",
            "RecordingBottomSheetContent" to
                "Head token 'Recording' is a consumer-domain noun (voice recordings) — " +
                "grandfathered per D-02's day-one allowlist.",
            "TagListItem" to
                "Head token 'Tag' is a consumer-domain noun (tagging system) — grandfathered " +
                "per D-02's day-one allowlist.",
            "TagCreateSheet" to
                "Head token 'Tag' is a consumer-domain noun — grandfathered per D-02's day-one " +
                "allowlist.",
            "TagCreateSheetContent" to
                "Head token 'Tag' is a consumer-domain noun — grandfathered per D-02's day-one " +
                "allowlist.",
            "TagChipWithContextMenu" to
                "Head token 'Tag' is a consumer-domain noun — grandfathered per D-02's day-one " +
                "allowlist.",
            "TagPickerSheet" to
                "Head token 'Tag' is a consumer-domain noun — grandfathered per D-02's day-one " +
                "allowlist.",
            "TagPickerSheetContent" to
                "Head token 'Tag' is a consumer-domain noun — grandfathered per D-02's day-one " +
                "allowlist.",
            "TagChipEditorContent" to
                "Head token 'Tag' is a consumer-domain noun — grandfathered per D-02's day-one " +
                "allowlist.",
            "YahirAndroidTasteTheme" to
                "Head token 'Yahir' is the library's own brand prefix, not a consumer-domain " +
                "leak — it IS the chrome every explorer screen composes around (mirrors " +
                "ComponentRegistry.INTENTIONALLY_UNREGISTERED's own rationale for this same name).",
            "WaveformCanvas" to
                "Head token 'Waveform' reads as a generic UI-interaction/visualization " +
                "descriptor rather than a true consumer-domain noun, but is grandfathered here " +
                "(not added to PRIMITIVE_NOUN_ALLOWLIST) so a genuinely domain-coupled future " +
                "name sharing a similar shape still gets flagged for review.",
            "SwipeableActionRow" to
                "Head token 'Swipeable' reads as a generic UI-interaction descriptor rather " +
                "than a true consumer-domain noun, but is grandfathered here (not added to " +
                "PRIMITIVE_NOUN_ALLOWLIST) so a genuinely domain-coupled future name sharing a " +
                "similar shape still gets flagged for review.",
            "RevealActionRow" to
                "Head token 'Reveal' reads as a generic UI-interaction descriptor rather than " +
                "a true consumer-domain noun, but is grandfathered here (not added to " +
                "PRIMITIVE_NOUN_ALLOWLIST) so a genuinely domain-coupled future name sharing a " +
                "similar shape still gets flagged for review."
        )
    }
}
