package com.example.secondbrain.yahirandroidtaste.explorer

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * CATALOG-03/05 drift guard (D-03, widened at CATALOG-05/Phase 87): source-scans every
 * yahirandroidtaste top-level package EXCEPT `explorer/` (a denylist, not an allowlist — see
 * below) for public top-level `@Composable` functions and asserts each has a
 * [ComponentRegistry.entries] entry OR an [ComponentRegistry.INTENTIONALLY_UNREGISTERED]
 * allowlist entry (D-04's single source of truth, shared with the explorer's own rendering). An
 * unregistered export fails the build.
 *
 * This is a plain JVM JUnit source-TEXT scan — no Robolectric `@RunWith`/`@Config`, no Compose
 * UI-test rule, no runtime reflection/ClassGraph. Compose's compiler adds synthetic
 * `Composer`/`$changed`/`$default` parameters to every `@Composable` function's compiled
 * signature, which makes reflection-based detection fragile (D-03's own rejection rationale) —
 * reading `.kt` source text instead sidesteps that entirely.
 *
 * "Top-level" is detected via zero-indentation: a `@Composable`-annotated declaration is
 * counted only when both the annotation line and the `fun` line it decorates start at column 0
 * (no leading whitespace). This deliberately excludes member functions nested inside an
 * `object`/`class` body (e.g. `ConfirmationDialogDefaults.confirmButtonColors`) — those are not
 * independently showcaseable exports per D-04's "public top-level @Composable" definition, and
 * ComponentRegistry.kt's own doc-comment count (44) already excludes them.
 *
 * CATALOG-05 (Phase 87): the scan boundary is [excludedPackages] — a denylist of just
 * `explorer/` (this file's own package — the showcase harness, correctly excluded per
 * PITFALLS.md Pitfall 10: its own family screens and fake-data fixtures are infrastructure, not
 * showcaseable components) — rather than the former 4-name allowlist (`component/`,
 * `feedback/`, `modifier/`, `theme/`). Every current-and-future non-`explorer` top-level package
 * is scanned automatically, so a brand-new package landing an unregistered public `@Composable`
 * fails the build structurally instead of requiring someone to remember to widen an allowlist.
 *
 * Vacuous-pass guard (RESEARCH.md Pitfall 2): the Gradle `Test` task's working directory
 * defaults to the module project directory, so a path relative to CWD normally resolves
 * correctly — but this test does not rely on that alone. [resolveModuleSourceRoot] first tries
 * to anchor the scan by walking upward from the process CWD looking for the `yahirandroidtaste`
 * module's own `build.gradle.kts`, falling back to the Gradle-default relative path only if that
 * walk fails. Either way, the test asserts the walk found a NON-ZERO number of `.kt` files
 * BEFORE checking registry coverage — a zero-file scan (broken working-directory assumption)
 * fails loudly with a clear message instead of vacuously passing.
 *
 * SC4 demonstration: add an unregistered public `@Composable` to any non-`explorer` package ->
 * this test goes RED, listing the offending name(s). Register it in [ComponentRegistry.entries]
 * (or allowlist it in [ComponentRegistry.INTENTIONALLY_UNREGISTERED]) -> GREEN again. See
 * 61-04-SUMMARY.md for the original CATALOG-03 recorded evidence and 87-01-SUMMARY.md for the
 * CATALOG-05 denylist-widening red->register->green demonstration.
 */
class ComponentRegistryDriftGuardTest {

    private val excludedPackages = setOf("explorer")

    @Test
    fun everyPublicComposableIsRegisteredOrAllowlisted() {
        val sourceRoot = resolveModuleSourceRoot()

        // Vacuous-pass guard FIRST (RESEARCH Pitfall 2): assert the scan actually found source
        // files before trusting any coverage conclusion drawn from an empty scan.
        val allScannedKtFiles = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
        assertTrue(
            "Source scan found 0 .kt files under $sourceRoot — the working-directory/source-root " +
                "assumption broke. The drift guard would vacuously pass if this were not asserted; " +
                "failing loudly instead (RESEARCH.md Pitfall 2).",
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
                "functions — the extraction logic or source root is wrong (expected dozens, per " +
                "ComponentRegistry.kt's own doc-comment count). Failing loudly instead of " +
                "vacuously passing.",
            scannedComposableNames.isNotEmpty()
        )

        val registeredNames = ComponentRegistry.entries.map { it.name }.toSet()
        val allowlistedNames = ComponentRegistry.INTENTIONALLY_UNREGISTERED.keys

        val offendingNames = scannedComposableNames - registeredNames - allowlistedNames

        if (offendingNames.isNotEmpty()) {
            fail(
                "Found ${offendingNames.size} public top-level @Composable function(s) outside " +
                    "the excluded packages ($excludedPackages) that are neither registered in " +
                    "ComponentRegistry.entries nor allowlisted in " +
                    "ComponentRegistry.INTENTIONALLY_UNREGISTERED: " +
                    "${offendingNames.sorted()} — register each as a new explorer entry, or add " +
                    "it to INTENTIONALLY_UNREGISTERED with a one-line reason (D-04)."
            )
        }
    }

    /**
     * Resolves the `yahirandroidtaste` module's `src/main/java/.../yahirandroidtaste` source
     * root robustly (RESEARCH.md Pitfall 2) — do not rely solely on the test process's current
     * working directory. Walks upward from the process CWD looking for a directory named
     * `yahirandroidtaste` that owns both a `build.gradle.kts` and the expected source-root path;
     * falls back to the Gradle `Test` task's documented default (CWD = module project dir) only
     * if that walk doesn't find one.
     */
    private fun resolveModuleSourceRoot(): File {
        val relativeSourceRoot = "src/main/java/com/example/secondbrain/yahirandroidtaste"

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
     * source-text scanning (no reflection). A declaration counts only when:
     *  - its `@Composable` annotation line starts at column 0 (top-level, not nested inside an
     *    `object`/`class`/function body), and
     *  - the `fun` declaration line it decorates (found by skipping over blank lines and any
     *    other stacked annotations) also starts at column 0, and
     *  - that `fun` line is not `private`/`internal`.
     *
     * Handles generic type params (`fun <T> ChipBar(`) and extension-receiver composables
     * (`fun ReorderableCollectionItemScope.EditorItemRow(`) — both patterns are live in this
     * codebase (`ChipBar.kt`, `EditorItemRow.kt`).
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
                            "parse a function name from it. Failing loudly (WR-02) instead of " +
                            "silently dropping this composable from the coverage scan."
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
     * [annotationLineIndex] decorates, skipping forward past blank lines, stacked annotations
     * (including their own multi-line argument lists, e.g. `@Suppress(\n    "MagicNumber"\n)`),
     * and any interleaved KDoc — none of those lines contain the `fun` keyword, so they are all
     * skipped uniformly rather than stopping at the first non-blank/non-`@` line (WR-02: that
     * older heuristic silently mis-located the declaration — and therefore silently dropped the
     * composable from the coverage scan — for either pattern).
     *
     * Fails loudly (test failure with a clear message) instead of silently returning a wrong
     * line if no line that looks like a `fun` declaration is found within
     * [MAX_DECLARATION_LOOKAHEAD_LINES] lines — a malformed/unrecognizable construct here must
     * not vacuously drop the composable from the drift guard's coverage accounting.
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
                "loudly (WR-02) instead of silently dropping this composable from the coverage " +
                "scan."
        )
    }

    private companion object {
        /**
         * Matches a top-level `fun` declaration, capturing the function name. Tolerates an
         * optional `public` modifier, an optional generic type-parameter list (`<T>`), and an
         * optional extension-receiver type prefix (`ReceiverType.`) before the name.
         */
        val FUN_DECLARATION_REGEX = Regex(
            """^(?:public\s+)?fun\s+(?:<[^>]*>\s+)?(?:[A-Za-z_][\w.]*\.)?(\w+)\s*\("""
        )

        /**
         * Bound on how many lines [findDeclarationLineIndex] will scan forward past an
         * `@Composable` annotation looking for the `fun` line it decorates, before failing
         * loudly (WR-02). Generous enough for any stacked-annotation argument list or interleaved
         * KDoc block plausible in this codebase, while still catching a genuinely malformed or
         * unrecognized construct instead of scanning the rest of the file.
         */
        const val MAX_DECLARATION_LOOKAHEAD_LINES = 25
    }
}
