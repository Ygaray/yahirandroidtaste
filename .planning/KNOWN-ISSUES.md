# Known Issues — yahirandroidtaste

Project-local defects (this repo's own build config / source), tracked here rather than in the
control-plane incident log. Control-plane (GSD tooling / devices / ecosystem infra) defects go to
`~/Projects/yahir-agentic-tools/yahir-gsd-control-plane/incidents/` via the `incident` skill.

---

## KI-2026-09-02-01 — `metalavaCheckCompatibilityDebug` fails: Dagger-generated `UndoHistoryStore_Factory` leaked into the tracked `api.txt` baseline

**Status:** closed · **Severity:** build-config defect (does NOT block the JitPack publish path) ·
**Pre-existing since:** commit `534ec10` (before Phase 3) · **Opened:** 2026-09-02 · **Closed:** 2026-09-27 (Phase 9, SHIP-01, D-01)

### Summary

`./gradlew build`'s `metalavaCheckCompatibilityDebug` task fails with a false "Removed class"
breaking-change error, because the tracked `api.txt` baseline includes a Hilt/Dagger **generated**
class (`UndoHistoryStore_Factory`) that the regenerated API no longer contains. This does **not**
affect the JitPack release path (`publishReleasePublicationToMavenLocal`), which was verified green
immediately before the v2.0.0 tag cut — it only breaks the full `./gradlew build` and any workflow
that runs `metalavaCheckCompatibilityDebug`.

### Evidence

```
$ ./gradlew metalavaCheckCompatibilityDebug --console=plain
> Task :metalavaCheckCompatibilityDebug FAILED
api.txt:739: error: Binary breaking change: Removed class
  io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryStore_Factory [RemovedClass]
Aborting: Found compatibility problems checking the public API
  (build/metalava/current.txt) against the API in api.txt
BUILD FAILED
```

The offending baseline line — a Dagger-generated factory, `api.txt:739`:

```
@dagger.internal.DaggerGenerated @dagger.internal.QualifierMetadata
@dagger.internal.ScopeMetadata("javax.inject.Singleton")
@javax.annotation.processing.Generated(value="dagger.internal.codegen.ComponentProcessor" …)
public final class UndoHistoryStore_Factory implements dagger.internal.Factory<…UndoHistoryStore!> {
```

The regenerated API no longer contains it:

```
$ grep -c UndoHistoryStore_Factory build/metalava/current.txt
0
```

### Root cause (verified against the running system)

`UndoHistoryStore_Factory` is a Dagger/Hilt code-generation artifact (the `_Factory` suffix +
`@DaggerGenerated`), not part of the library's authored public API. It was captured into `api.txt`
when the baseline was generated at a point where codegen emitted it into the metalava-visible
surface; the current build no longer surfaces it, so the strict compatibility check reads its
absence as a removed public class. Generated factories should never have been part of the tracked
API contract in the first place.

### Impact

- `./gradlew build` and any `metalavaCheckCompatibilityDebug` run fail. Recurs deterministically.
- Release path is unaffected: `apiCheck`, `testDebugUnitTest`, `detekt`, and
  `publishReleasePublicationToMavenLocal` all pass; v2.0.0 was cut on a green publish path.
- The risk is that a future contributor treats the broken `build` as a real API regression, or
  that a workflow wired to `build`/`metalavaCheckCompatibilityDebug` (rather than the publish
  task) blocks spuriously.

### Proposed fix (pick one, with an acceptance test)

1. **Exclude generated classes from the metalava surface (preferred):** configure the metalava
   Gradle extension to hide `@dagger.internal.DaggerGenerated` / `@javax.annotation.processing.Generated`
   types, then regenerate `api.txt`. Acceptance: `api.txt` no longer contains any `_Factory` /
   `@DaggerGenerated` entry, and `./gradlew metalavaCheckCompatibilityDebug` passes.
2. **Rebaseline only:** `./gradlew <apiDump-equivalent>` to regenerate `api.txt` from current
   codegen so the generated class is dropped. Weaker — leaves the door open for the same class to
   re-enter the baseline on a future dump. Acceptance: `metalavaCheckCompatibilityDebug` green.

Either fix must keep the real public API entries intact (`UndoHistoryStore` itself, its `@Inject`
ctor, and the `emitTrackedWithUndo` surface all stay).

### Closing evidence (fix option 1 landed, Phase 9 Task 1, 2026-09-27)

`build.gradle.kts`'s `metalava { }` extension now sets
`hiddenAnnotations.add("dagger.internal.DaggerGenerated")`, hiding every Dagger-generated symbol
from the metalava-tracked surface on **every** variant (not just Release). After
`./gradlew apiDump` regenerated `api.txt`:

- `grep -c DaggerGenerated api.txt` → `0`
- `grep -c UndoHistoryStore_Factory api.txt` → `0`
- `grep -c 'class UndoHistoryStore {' api.txt` → `1` (real class intact)
- `grep -c emitTrackedWithUndo api.txt` → `1` (real extension intact)
- `./gradlew metalavaCheckCompatibilityDebug metalavaCheckCompatibilityRelease` → both `BUILD
  SUCCESSFUL`, no "Removed class" / compatibility-problem output on either variant.

Fix option 1 landed exactly as specified in the 2026-09-27 Runtime Decision (09-CONTEXT.md): the
generated symbol is gone from `api.txt` and both compatibility-check variants pass. This is the
zero-baseline-consistent path (a genuine surface-tracking fix), not an accepted-override.

---

## KI-2026-09-27-01 — `detekt` fails: `TextCard.kt`'s `CyclomaticComplexMethod` finding (25/25) breaches the zero-baseline gate

**Status:** open · **Severity:** build-config defect (does NOT block `apiCheck`, `testDebugUnitTest`,
or `publishReleasePublicationToMavenLocal`) · **Pre-existing since:** at least commit `b58e9ee`
(2026-09-07, `TextCard.kt`'s last touch) — confirmed present at commit `60381d2` (immediately
before Phase 6's first execution commit) · **Opened:** 2026-09-27 (discovered during Phase 6
forward-port-reunification verification)

### Summary

`./gradlew detekt` fails with a single weighted issue: `CyclomaticComplexMethod` (25/25, over
threshold) in `TextCard.kt` at the `TextCard` composable's signature (`TextCard.kt:132`). This is
unrelated to Phase 6 (Forward-Port Reunification) — `TextCard.kt` is not in any of that phase's
plans' `files_modified` lists, and `TextCard.kt` was last touched on 2026-09-07, well before Phase 6
began. Phase 6's own verifier confirmed this by checking out the commit immediately preceding
Phase 6 (`60381d2`) into an isolated worktree and re-running `./gradlew detekt` there: identical
failure, identical "1 number of total code smells" Complexity Report both before and after Phase 6.

### Evidence

```
$ ./gradlew detekt
> Task :detekt FAILED
Analysis failed with 1 weighted issues.
BUILD FAILED
```

`TextCard.kt:132` — the flagged composable's signature (large parameter list drives the branch
count inside the function body):

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextCard(
    id: String,
    title: String,
    content: String?,
    categoryPath: String?,
    createdAt: Long,
    updatedAt: Long,
    isPinned: Boolean,
    isFavorite: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    // ... additional optional params (accent, tactileDepth, onEditRequest, etc.)
```

### Root cause

`TextCard` has accreted optional parameters and conditional branches across multiple prior phases
(FACE-01 accent/tactileDepth, EDIT-01/EDIT-03 onEditRequest, etc. — see its own KDoc), each
individually reasonable, that collectively pushed its cyclomatic complexity over detekt's threshold.
No single change introduced the breach; it crossed the threshold incrementally.

### Impact

- `./gradlew detekt` (the project's zero-baseline gate) fails on `main` right now.
- Release/test paths are unaffected: `apiCheck`, `testDebugUnitTest`, and
  `publishReleasePublicationToMavenLocal` all pass — confirmed during Phase 6's governance battery
  and independently re-confirmed during Phase 6 verification.
- Per this repo's zero-baseline detekt policy (root `CLAUDE.md`), the correct remediation is NOT to
  regenerate `config/detekt-baseline.xml` to bury this finding — it must be fixed at the source
  (extract helper functions) or the rule tuned with justification.

### Proposed fix (pick one, with an acceptance test)

1. **Extract helper functions (preferred):** split `TextCard`'s body into smaller private
   `@Composable` helpers (e.g. a header-row helper, an actions-row helper) to bring its cyclomatic
   complexity under threshold without changing its public signature or behavior. Acceptance:
   `./gradlew detekt` passes with zero new baseline entries; `TextCard`'s existing tests
   (call-site compilation + any Robolectric/Compose UI tests) stay green.
2. **Tune the rule with justification (fallback):** if the complexity is judged inherent to a
   single large-surface composable rather than genuinely reducible, raise
   `CyclomaticComplexMethod`'s threshold for this specific function via a documented, justified
   detekt suppression (`@Suppress("CyclomaticComplexMethod")` with a comment explaining why, per
   this repo's zero-baseline policy of tuning-with-justification rather than banking debt) —
   never a blanket baseline regeneration.

Whichever fix lands, it is out of scope for Phase 6 (which does not touch `TextCard.kt`) — tracked
here per the same precedent as `KI-2026-09-02-01` (pre-existing, unrelated build-config defect,
does not block release paths, formally tracked rather than silently deferred).

**Note (Phase 9, 2026-09-27):** re-checked during Phase 9's closing governance battery —
`./gradlew detekt --rerun-tasks` now reports `0 number of total code smells` on `main`. Left `open`
here rather than self-closed by Phase 9 (out of that phase's scope per its plan), but the blocking
condition described above no longer reproduces as of this commit; whoever next touches this KI
should re-verify and close with attribution to whichever phase's change resolved it.
