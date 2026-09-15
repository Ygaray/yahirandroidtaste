package io.github.ygaray.yahirandroidtaste.component

import java.io.File

/**
 * Shared source-structural-contract test helpers, extracted from
 * `TextListBottomSheetEditMenuSourceContractTest` and `TagChipEditorDoubleTapRemovalTest`
 * (114-REVIEW.md WR-01) where they were byte-identical duplicates. This is the string-literal-
 * aware comment-stripping logic that gates TAG-03's one security-relevant source assertion (the
 * marker-scoped `onRemoveTagNoUndo` absence check) as well as EDIT-04's label-copy assertions --
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
     *  3. trailing inline `//` comments -- the tail of a line from a `//` that appears *after*
     *     code on the same line, cut only when that `//` sits outside a double-quoted string
     *     (scanned left to right, honouring backslash escapes) so a legitimate `//` inside a
     *     string literal (a URL, a path) is never truncated.
     *
     * Documented blind spots -- deliberately NOT handled, extend this helper if a future
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

    /**
     * Extracts one function's full source text -- from the start of the [occurrence]-th
     * (1-based) match of [declaration] (e.g. `"fun clearPinOverlays("`, including the opening
     * `(` of its parameter list, by this file's own calling convention) through the closing
     * brace that matches its body's opening brace -- from [src] (intended for [stripComments]
     * output; the same blind spots [stripComments] already documents apply here: a char literal
     * holding a brace/paren, and a raw triple-quoted string).
     *
     * Reused across several source-contract assertions (164-02, and Plan 04 on top of it) so
     * each one names the function it cares about instead of matching a brittle
     * line-ordering-sensitive substring (164-02 review round 2 LOW, raised again on 164-04).
     *
     * Algorithm: first balances *parentheses only* (so a default-valued lambda parameter such as
     * `= {}` is skipped -- its braces never affect this phase) to find the end of the parameter
     * list; then scans forward requiring a body-opening `{` to appear before any `=` (an
     * expression-bodied function throws instead of silently extracting the wrong thing); then
     * balances *braces* from that `{` to its matching `}`. Both balancing passes skip characters
     * inside double-quoted string literals using the same quote/backslash-escape scan as
     * [stripTrailingInlineComment].
     *
     * @throws IllegalStateException if [declaration] does not occur [occurrence] times in [src],
     *   if the parameter list or the body's braces never balance before the end of [src], or if
     *   an `=` (an expression body) appears before the body's opening `{`.
     */
    fun functionBody(src: String, declaration: String, occurrence: Int = 1): String {
        val declarationStart = findDeclarationStart(src, declaration, occurrence)
        val afterParams = balanceFrom(src, declarationStart + declaration.length, open = '(', close = ')')
        val bodyStart = findBodyStart(src, afterParams, declaration)
        val bodyEnd = balanceFrom(src, bodyStart + 1, open = '{', close = '}')
        return src.substring(declarationStart, bodyEnd)
    }

    private fun findDeclarationStart(src: String, declaration: String, occurrence: Int): Int {
        var searchFrom = 0
        var found = -1
        repeat(occurrence) {
            found = src.indexOf(declaration, searchFrom)
            check(found >= 0) {
                "functionBody: declaration \"$declaration\" does not occur $occurrence time(s) " +
                    "in the given source."
            }
            searchFrom = found + declaration.length
        }
        return found
    }

    /**
     * Scans forward from [start] balancing [open]/[close] (already at depth 1, since the caller
     * consumed the opening character as part of [declaration] or the body-start scan), skipping
     * string literals, and returns the index just past the matching close character.
     */
    private fun balanceFrom(src: String, start: Int, open: Char, close: Char): Int {
        var depth = 1
        var i = start
        var inString = false
        while (i < src.length && depth > 0) {
            val c = src[i]
            when {
                c == '\\' && inString -> i++
                c == '"' -> inString = !inString
                !inString && c == open -> depth++
                !inString && c == close -> depth--
            }
            i++
        }
        check(depth == 0) {
            "functionBody: \"$open$close\" never balances for a declaration starting near " +
                "index $start before the end of the source."
        }
        return i
    }

    /** Scans forward from [start] (just past the closed parameter list) for the body's opening `{`. */
    private fun findBodyStart(src: String, start: Int, declaration: String): Int {
        var i = start
        var inString = false
        while (i < src.length) {
            val c = src[i]
            when {
                c == '\\' && inString -> i++
                c == '"' -> inString = !inString
                !inString && c == '{' -> return i
                !inString && c == '=' ->
                    error(
                        "functionBody: \"$declaration\" is an expression-bodied function " +
                            "(found '=' before '{') -- functionBody only supports block bodies."
                    )
            }
            i++
        }
        error("functionBody: no body '{' found for \"$declaration\" before the end of the source.")
    }
}
