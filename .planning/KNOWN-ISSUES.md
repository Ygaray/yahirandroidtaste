# Known Issues — yahirandroidtaste

Project-local defects (this repo's own build config / source), tracked here rather than in the
control-plane incident log. Control-plane (GSD tooling / devices / ecosystem infra) defects go to
`~/Projects/yahir-agentic-tools/yahir-gsd-control-plane/incidents/` via the `incident` skill.

---

## KI-2026-09-02-01 — `metalavaCheckCompatibilityDebug` fails: Dagger-generated `UndoHistoryStore_Factory` leaked into the tracked `api.txt` baseline

**Status:** open · **Severity:** build-config defect (does NOT block the JitPack publish path) ·
**Pre-existing since:** commit `534ec10` (before Phase 3) · **Opened:** 2026-09-02

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
