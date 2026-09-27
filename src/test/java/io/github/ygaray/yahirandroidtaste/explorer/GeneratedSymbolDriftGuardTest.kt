package io.github.ygaray.yahirandroidtaste.explorer

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * SHIP-01 regression guard (09-01-PLAN.md D-01, KI-2026-09-02-01): the committed `api.txt`
 * signature file must never re-admit a Hilt/Dagger-generated factory symbol (e.g.
 * `UndoHistoryStore_Factory`, annotated `@dagger.internal.DaggerGenerated`), and the real
 * hand-authored `UndoHistoryStore` API (its class, `@Inject` constructor, and the
 * `emitTrackedWithUndo` extension) must remain present.
 *
 * Why this exists as a persisted JUnit test rather than only a one-off shell grep: Phase 9 Plan 01
 * verified this exact invariant via ad hoc `grep -c` commands run once against the tag-candidate
 * commit, plus `metalavaCheckCompatibilityDebug`/`Release`. Neither is a durable regression guard:
 * metalava's compatibility check only fails on BREAKING removals from the tracked surface, not on
 * a generated symbol being ADDED back in (e.g. if `build.gradle.kts`'s `hiddenAnnotations` entry
 * for `dagger.internal.DaggerGenerated` is ever reverted, or a future Hilt/Dagger upgrade renames
 * the generated annotation/class), so `UndoHistoryStore_Factory` could silently re-enter `api.txt`
 * on the next `./gradlew apiDump` without any hub gate going red. This test closes that gap by
 * running the same assertion on every `testDebugUnitTest` invocation, mirroring
 * [ComponentRegistryDriftGuardTest] and [DomainVocabularyDriftGuardTest]'s fail-until-fixed shape.
 */
class GeneratedSymbolDriftGuardTest {

    @Test
    fun apiTxtNeverReadmitsAHiddenDaggerGeneratedFactorySymbol() {
        val apiTxt = resolveApiTxt()
        val lines = apiTxt.readLines()

        assertTrue(
            "api.txt at ${apiTxt.absolutePath} was empty -- the source-root resolution or the " +
                "signature file itself is broken. Failing loudly instead of vacuously passing.",
            lines.isNotEmpty()
        )

        val daggerGeneratedLines = lines.filter { it.contains("DaggerGenerated") }
        if (daggerGeneratedLines.isNotEmpty()) {
            fail(
                "api.txt contains ${daggerGeneratedLines.size} line(s) referencing " +
                    "'DaggerGenerated' -- a Hilt/Dagger-generated symbol has re-entered the " +
                    "tracked API surface (KI-2026-09-02-01 regression). Check build.gradle.kts's " +
                    "metalava { hiddenAnnotations } still names " +
                    "'dagger.internal.DaggerGenerated' before re-running apiDump. " +
                    "Offending lines: $daggerGeneratedLines"
            )
        }

        val generatedFactoryLines = lines.filter { it.contains("UndoHistoryStore_Factory") }
        if (generatedFactoryLines.isNotEmpty()) {
            fail(
                "api.txt contains ${generatedFactoryLines.size} line(s) referencing " +
                    "'UndoHistoryStore_Factory' -- the specific generated factory class " +
                    "KI-2026-09-02-01 named is back in the tracked API surface. " +
                    "Offending lines: $generatedFactoryLines"
            )
        }
    }

    @Test
    fun apiTxtStillContainsUndoHistoryStoresRealPublicApi() {
        val apiTxt = resolveApiTxt()
        val lines = apiTxt.readLines()

        val realClassLines = lines.count { it.contains("class UndoHistoryStore {") }
        val realCtorLines = lines.count {
            it.contains("ctor") && it.contains("UndoHistoryStore()")
        }
        val extensionLines = lines.count { it.contains("emitTrackedWithUndo") }

        assertTrue(
            "Expected exactly 1 'class UndoHistoryStore {' entry in api.txt but found " +
                "$realClassLines -- the real hand-authored UndoHistoryStore class was removed " +
                "or duplicated, which would be a regression, not the intended Dagger-symbol fix.",
            realClassLines == 1
        )
        assertTrue(
            "Expected at least 1 UndoHistoryStore() constructor entry in api.txt but found " +
                "$realCtorLines -- the real @Inject constructor was removed, which would be a " +
                "regression, not the intended Dagger-symbol fix.",
            realCtorLines >= 1
        )
        assertTrue(
            "Expected at least 1 emitTrackedWithUndo entry in api.txt but found " +
                "$extensionLines -- the real emitTrackedWithUndo extension was removed, which " +
                "would be a regression, not the intended Dagger-symbol fix.",
            extensionLines >= 1
        )
    }

    /**
     * Resolves the `yahirandroidtaste` module's committed `api.txt` (sits at the module/repo root
     * next to `build.gradle.kts`) via the same CWD-independent walk-up-then-fallback strategy as
     * [ComponentRegistryDriftGuardTest]'s `resolveModuleSourceRoot`, adapted for a single file
     * instead of a source directory.
     */
    private fun resolveApiTxt(): File {
        var dir: File? = File(".").absoluteFile
        var depth = 0
        while (dir != null && depth < 8) {
            if (dir.name == "yahirandroidtaste") {
                val candidateBuildFile = File(dir, "build.gradle.kts")
                val candidateApiTxt = File(dir, "api.txt")
                if (candidateBuildFile.isFile && candidateApiTxt.isFile) {
                    return candidateApiTxt
                }
            }
            dir = dir.parentFile
            depth++
        }

        val fallback = File("api.txt")
        check(fallback.isFile) {
            "Could not resolve api.txt by walking up from the process CWD " +
                "(${File(".").absoluteFile}) looking for a yahirandroidtaste module directory, " +
                "nor via the Gradle-default relative path (resolved absolute: " +
                "${fallback.absoluteFile}). This drift guard cannot verify the API signature " +
                "file without a valid path to it."
        }
        return fallback
    }
}
