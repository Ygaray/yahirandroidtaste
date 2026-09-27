# Phase 9: Ship & Coordinated Repin - Pattern Map

**Mapped:** 2026-09-27
**Files analyzed:** 3 (build.gradle.kts, api.txt, ECOSYSTEM.md) + 1 read-only precedent source (git tags/log)
**Analogs found:** 3 / 3 (this is an ops/ship phase — analogs are prior states of the SAME files, not sibling files)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `build.gradle.kts` (metalava block) | config | transform (build-config → api.txt) | same file, `metalava { }` block + `publishing.publications["release"].version` marker | exact (self) |
| `api.txt` | config (generated signature baseline) | transform (metalava output) | same file, prior `metalavaGenerateSignatureRelease` regen commits (e.g. `000bf89`, v2.1.0 tag commit `f690efc`) | exact (self, historical regen pattern) |
| `ECOSYSTEM.md` §1 + repin-matrix + §7 | doc/config | batch (reconcile script rewrites machine block; human rewrites prose) | same file, prior reconcile commits (`8432531`, `2c3e3f9`) | exact (self, historical reconcile pattern) |

This phase does not create new source files — it is a config-fix + ship + doc-reconcile phase. "Analog" here means the precedent established by prior tag cuts and reconciles in this exact repo, not a sibling file elsewhere.

---

## Pattern Assignments

### `build.gradle.kts` (config, metalava exclusion fix)

**Current block (lines 16-30), the KI-2026-09-02-01 fix target:**
```kotlin
metalava {
    filename = "api.txt"
}

tasks.register("apiDump") {
    group = "verification"
    description = "Regenerates the committed public-API signature file (delegates to Metalava)."
    dependsOn("metalavaGenerateSignatureRelease")
}

tasks.register("apiCheck") {
    group = "verification"
    description = "Checks the current public API against the committed signature file (delegates to Metalava)."
    dependsOn("metalavaCheckCompatibilityRelease")
}
```

**No existing hide/exclude config exists yet in this block.** Per KI-2026-09-02-01's evidence
(`.planning/KNOWN-ISSUES.md` lines 9-49), fix option 1 is to hide the `@DaggerGenerated` surface from
metalava. The `metalava { }` extension (io.github.ygaray's metalava gradle plugin, same family as
Android's `metalava` CLI) supports a `hiddenAnnotations` / `hidePackages` style config — planner
should check the plugin's actual DSL (likely `hiddenAnnotations.add("dagger.internal.DaggerGenerated")`
or an `excludeAnnotations`/`hideAnnotations` list) rather than assume Android metalava's `--hide-annotation`
flag name verbatim; consult the applied plugin version's KDoc/source under
`~/.gradle/caches/.../metalava-gradle-plugin-*.jar` if the DSL name is ambiguous at execution time.

**Version marker (line 134), untouched historically (D-02 precedent):**
```kotlin
version = "1.10.0"
```
Per CONTEXT.md D-02: leave this stale (JitPack overrides from the resolved git ref); this was never
bumped for v2.0.0 or v2.1.0 tag cuts either — confirmed by `git show v2.1.0` (tag commit `f690efc`)
touching only `ComponentRegistry`/api.txt, not this marker. An optional bump to `2.2.0` in the tag
commit is hygiene-only per D-02, not required.

---

### `api.txt` (generated signature baseline, rebaseline target)

**Offending entry, `api.txt:739` area (already excerpted verbatim in KNOWN-ISSUES.md lines 35-42):**
```
@dagger.internal.DaggerGenerated @dagger.internal.QualifierMetadata
@dagger.internal.ScopeMetadata("javax.inject.Singleton")
@javax.annotation.processing.Generated(value="dagger.internal.codegen.ComponentProcessor" …)
public final class UndoHistoryStore_Factory implements dagger.internal.Factory<…UndoHistoryStore!> {
```

**The regeneration precedent (how api.txt was rebaselined historically):** every prior phase that
changed public API regenerated `api.txt` via `metalavaGenerateSignatureRelease` (the same task
`apiDump` delegates to) and committed the diff in the same commit as the source change — e.g. commit
`000bf89` ("feat(07-01): regenerate api.txt additively and pass the closing governance battery
(TAGCOLOR-01)") and the v2.1.0 tag commit `f690efc` ("Regenerate api.txt via
metalavaGenerateSignatureRelease."). Planner should have the executor run:
```
./gradlew apiDump
```
(or `metalavaGenerateSignatureRelease` directly) AFTER the `build.gradle.kts` exclusion fix lands, so
the regenerated `api.txt` no longer contains `UndoHistoryStore_Factory` — then diff-review to confirm
it is purely subtractive (removing only the Dagger-generated noise), not an accidental public-API
drop. Verify with:
```
./gradlew metalavaCheckCompatibilityDebug metalavaCheckCompatibilityRelease
```
both passing clean (KI-2026-09-02-01's stated acceptance criterion).

---

### `ECOSYSTEM.md` (doc, machine block + prose reconcile)

**§1 prose row, live, stale (lines 33-34 verbatim):**
```
| SecondBrain | `github.com/Ygaray/…` (private working tree) | `~/Projects/SecondBrain` | **`v1.11.0`** (repinned in **v4.0 Phase 155 Plan 04**, `REMIND-09`) — cut on a branch forked from `v1.10.0` (NOT this hub's `main` tip), deliberately bypassing this hub's own concurrent `v2.0.0` ("v1.0 Hub Stewardship") milestone... | `gradle/libs.versions.toml` |
| CalTracker | `github.com/Ygaray/…` | `~/Projects/AndroidApps/Personal/CalTracker_Android` | **`v2.1.0`** (repinned + Gate-1-confirmed on real hardware, Phase 64 / MIC-02...) | `gradle/libs.versions.toml` |
```
Note: the §1 table rows themselves are actually already fairly current (SecondBrain shows `v1.11.0`,
CalTracker shows `v2.1.0`) — the CONTEXT.md's claim of "still claims SB pins v1.11.0 / latest v1.10.0"
refers to the **prose narrative below the table**, specifically the "Current published tag" paragraph
(line 52) which still reads `v1.10.0` and the SecondBrain-pins sentence (line 80) which still reads
`v1.10.0` — both stale relative to the actual current tags (`v2.0.0`, `v2.1.0`, and post-phase `v2.2.0`).
Planner/executor must hand-correct these prose sentences (lines ~52, ~80, and the whole historical
narrative block lines 52-114 as needed) to reflect `v2.2.0` as latest once cut — this is D-03's
"hand-correct the stale narrative prose" scope, distinct from the machine block below.

**Machine-reconciled pin matrix (lines 40-50 verbatim, current state):**
```
### Machine-reconciled pin matrix

> Regenerated by `repin_status.py reconcile --hub yahirandroidtaste` (see
> `~/.claude/context/workflows/repin.md`). Do not hand-edit the rows between the markers.

<!-- repin-matrix:begin -->
| Consumer | Pinned | Latest | Status |
|---|---|---|---|
| CalTracker_Android | v2.1.0 | v2.1.0 | current |
| SecondBrain | v1.13.0 | v2.1.0 | behind |
<!-- repin-matrix:end -->
```
**Reconcile command precedent** (from the comment above the block + `repin_status.py`'s `argparse`
setup at line 298-341 of `~/.claude/context/deps/repin_status.py`, subcommands `validate`/`reconcile`):
```
python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste
```
Run this **after** the `v2.2.0` tag is pushed to `origin` (per CONTEXT.md Integration Points: `reconcile`
derives `latest` via `git ls-remote --tags`, so it must run post-push). It rewrites only the
`<!-- repin-matrix:begin -->`...`<!-- repin-matrix:end -->` block in place — never the surrounding
prose (confirmed by `reconcile_ecosystem()` at line 198 of the script, which raises `ValueError` if the
markers are absent, and only touches text between them).

**§7 shipping ritual (lines 505-517 verbatim — no changes needed, cite as the ritual this phase follows):**
```
## 7. Versioning & the repin ritual (shipping a hub change)

- The hub uses **semver immutable JitPack tags** (`v1.0.0`, …). **Never `-SNAPSHOT`**...
- Removing/renaming a public composable, or changing a component's required parameters, is a
  **breaking change** → major-ish bump, human-gated...
- Each consumer pins **one tag** and repins on its own cadence...
- The full, ordered ritual is **`~/.claude/context/workflows/repin.md` (§ Mechanism B)**...
```

---

## Shared Patterns

### Human-gated tag cut (applies to the whole phase, not a single file)
**Source:** git tag history (`v2.0.0`, `v2.1.0` annotated tags) + CONTEXT.md `code_context` section.
**Pattern:** gates green on `main` → surface `vX.Y.Z` for owner go-ahead (never auto-tag) → cut +
push an **annotated** tag (`git tag -a v2.2.0 -m "..."`, matching `v2.1.0`'s annotated tag with a
descriptive message — see `git show v2.1.0` output: `tag v2.1.0 \n Tagger: ... \n v2.1.0 -- MicButton
(MIC-01): generic tap-to-talk mic control, PATTERN tier`) → verify JitPack resolves
`com.github.Ygaray:yahirandroidtaste:v2.2.0` (retry-tolerant lazy-build check, e.g. curling the `.pom`
and `.aar` URLs as ECOSYSTEM.md line 396-397 documents for `v1.11.0`) → THEN run `repin_status.py
reconcile`.

### api.txt regeneration discipline
**Source:** commit `000bf89`, v2.1.0 tag commit `f690efc`.
**Apply to:** the `build.gradle.kts` fix + `api.txt` rebaseline pair — always regenerate via the
Gradle task (`apiDump` / `metalavaGenerateSignatureRelease`), never hand-edit `api.txt`, and commit the
config fix + regenerated baseline together in one commit (matching historical practice of pairing
source/config changes with their api.txt diff in the same commit).

### ECOSYSTEM.md machine-block vs. prose separation
**Source:** `repin_status.py` `reconcile_ecosystem()` (line 198) + D-03.
**Apply to:** ECOSYSTEM.md edits — the `<!-- repin-matrix -->` block is script-owned (never hand-edit
between the markers per the comment on line 43); the surrounding prose (§1 narrative sentences, the
"Current published tag" paragraph) is human/executor-owned and must be hand-corrected separately,
in the same phase per D-03, since the script does not touch it.

## No Analog Found

None — all three touch points have direct historical precedent within this same repo (prior tag
cuts, prior api.txt regens, prior ECOSYSTEM.md reconciles).

## Metadata

**Analog search scope:** repo root (`build.gradle.kts`, `api.txt`, `ECOSYSTEM.md`), git tag/log
history (`git tag -l`, `git log --oneline --all`, `git show v2.1.0`), `.planning/KNOWN-ISSUES.md`,
`~/.claude/context/deps/repin_status.py`.
**Files scanned:** 3 target files + 1 external script + git history (2 tag-cut commits inspected in
detail: `v2.0.0`, `v2.1.0`).
**Pattern extraction date:** 2026-09-27
