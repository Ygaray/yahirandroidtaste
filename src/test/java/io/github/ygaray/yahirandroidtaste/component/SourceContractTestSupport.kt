package io.github.ygaray.yahirandroidtaste.component

import java.io.File

/**
 * Shared source-structural-contract test helpers, extracted from
 * `TextListBottomSheetEditMenuSourceContractTest` and `TagChipEditorDoubleTapRemovalTest`
 * (114-REVIEW.md WR-01) where they were byte-identical duplicates. This is the string-literal-
 * aware comment-stripping logic that gates TAG-03's one security-relevant source assertion (the
 * marker-scoped `onRemoveTagNoUndo` absence check) as well as EDIT-04's label-copy assertions —
 * sharing it means a future bug-fix or extension (e.g. handling block `/* ... */` comments, one of
 * the two explicitly documented blind spots) is applied and tested in exactly one place.
 */
internal object SourceContractTestSupport {

    fun source(file: String): String =
        File("src/main/java/io/github/ygaray/yahirandroidtaste/component/$file").readText()

    fun countOccurrences(haystack: String, needle: String): Int =
        haystack.split(needle).size - 1

    /**
     * Strips comment noise from a source excerpt so label-literal counts are not polluted by
     * comment prose. Strips three things:
     *  1. whole lines whose first non-whitespace characters are `//`;
     *  2. whole lines whose first non-whitespace character is `*` (KDoc/block-comment
     *     continuation lines);
     *  3. trailing inline `//` comments — the tail of a line from a `//` that appears *after*
     *     code on the same line, cut only when that `//` sits outside a double-quoted string
     *     (scanned left to right, honouring backslash escapes) so a legitimate `//` inside a
     *     string literal (a URL, a path) is never truncated.
     *
     * Documented blind spots — deliberately NOT handled, extend this helper if a future
     * assertion needs either: block `/* ... */` comments that open and close on a code line, and
     * raw triple-quoted strings (a `//` inside one may be mis-treated as a comment start).
     */
    fun stripComments(src: String): String =
        src.lineSequence()
            .filterNot { line ->
                val trimmed = line.trimStart()
                trimmed.startsWith("//") || trimmed.startsWith("*")
            }
            .map { line -> stripTrailingInlineComment(line) }
            .joinToString("\n")

    fun stripTrailingInlineComment(line: String): String {
        var inString = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '\\' && inString -> i++ // skip escaped char inside string
                c == '"' -> inString = !inString
                c == '/' && !inString && i + 1 < line.length && line[i + 1] == '/' ->
                    return line.substring(0, i)
            }
            i++
        }
        return line
    }
}
