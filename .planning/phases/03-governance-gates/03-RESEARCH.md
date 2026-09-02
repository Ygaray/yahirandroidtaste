# Phase 3: Governance Gates - Research

**Researched:** 2026-09-01
**Domain:** Bash-based git pre-commit governance tooling + JUnit source-text-scan drift guards, in an
existing single-module Jetpack Compose design-system library
**Confidence:** HIGH — every claim below is grounded in files read this session or commands run
against the live repo; no invented APIs or guessed schemas.

## Summary

All four success criteria are implementation work against **existing, well-understood substrate** —
this phase adds no new dependency and no new architecture, it extends two things that already exist:
the `tools/` bash governance chain (GOV-03) and the `ComponentRegistryDriftGuardTest`-style JVM
source-scan pattern (GOV-02), plus a prose addition to `docs/DESIGN-INTENT.md` (GOV-01). The milestone
-level research already on disk (`.planning/research/{ARCHITECTURE,STACK,PITFALLS,FEATURES,SUMMARY}.md`,
dated 2026-08-28) correctly scoped this phase in advance; this document narrows that down to concrete
file/line targets now that Phase 1 has actually landed and the false-flag has been reproduced live.

**GOV-03 root cause, confirmed live in this session:** `tools/verify-additive-diff.sh` (the DS-05
src-line guard) diffs a fixed **baseline git tag** (`v1.10.0`) against the **current working tree**,
not against what a given commit actually changes. Running `bash tools/verify-additive-diff.sh
v1.10.0` on the repo **right now, with nothing staged**, still fails and names lines from
`HeatSwatch.kt` (comment reword, commit `5b01532`, landed on `main` after `v1.10.0`) plus a dozen
lines from Phase 1's legitimate tier-field rollout — because those rewrites are still physically
present in the tree and `v1.10.0` predates all of them. **Every commit from now until the next tag is
cut inherits this classification**, including a commit that touches only `.planning/`. D-01's fix
(diff the commit's own staged delta, `git diff --cached` vs `HEAD`, intersected with `src/main`)
is the right fix and is directly implementable: `git diff --cached -U0 -- src/main` on this repo
right now (nothing staged) returns clean, empty, exit 0 — confirmed by running it live.

**Critical follow-on finding (not yet in CONTEXT.md's D-01 scope — flagged for planner decision):**
the sibling guard `tools/verify-api-additive.sh` (the lane-3 API-line check) has the **identical
architectural flaw** — it compares the *current* `api.txt` content against the baseline tag's
`api.txt` content as full file-content sets, not as a per-commit delta. It hasn't manifested yet only
because `v1.10.0` (the current baseline tag) predates `api.txt`'s existence, so the check currently
**degrades to a no-op skip** (`API-ADDITIVE SKIP: baseline v1.10.0 has no api.txt yet`) — confirmed
live. The moment a new tag is cut that *includes* `api.txt` (Phase 5 will do exactly this), any
commit after that tag that is NOT the one which changed `api.txt` will start seeing the exact same
"stuck lane-3 forever until the next tag" bug GOV-03 exists to kill — just for API lines instead of
source lines. See `## Open Questions` below.

**Primary recommendation:** Fix `tools/hooks/pre-commit`'s src-line classification to diff
`git diff --cached` against `HEAD` scoped to `src/main`, add a `DomainVocabularyDriftGuardTest.kt`
next to the existing `ComponentRegistryDriftGuardTest.kt` reusing its scanning pattern (not its
private code — duplicate the ~40-line extraction method, see `## Don't Hand-Roll`), and append two
new sections to `docs/DESIGN-INTENT.md` rather than creating `CONTRIBUTING.md`.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Pre-commit lane classification (GOV-03) | Build/tooling (bash, git plumbing) | — | Runs entirely in the git hook layer, outside the Android/Compose runtime; no app tier involved. |
| Domain-vocabulary drift guard (GOV-02) | Test infra (JVM/JUnit, `src/test/`) | Build/tooling (feeds `./gradlew testDebugUnitTest`) | Source-text-scans `.kt` files at build/test time; not a runtime component, not UI. |
| Tier-aware contribution litmus (GOV-01) | Documentation (`docs/DESIGN-INTENT.md`) | Test infra (the strict half is enforced by the GOV-02 guard) | Prose lives in docs; enforcement piggybacks on GOV-02's test, not a new surface. |

There is no Browser/SSR/API/CDN tier in this repo (it's a library, not an app) — this map exists to
confirm none of GOV-01/02/03 belong in `src/main` runtime code, which they don't: all three are
either tooling (`tools/`) or test-only (`src/test/`) or documentation (`docs/`).

## Standard Stack

No new dependency of any kind. Everything below is already present and pinned; this phase's job is
technique, not tool selection.

### Core (already pinned, reused as-is)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Bash + git plumbing | system | GOV-03 fix lives entirely in `tools/hooks/pre-commit` / `tools/classify-hub-change.sh` / `tools/verify-additive-diff.sh` | Matches the existing governance chain's implementation language; no reason to introduce anything else for a git-diff scoping fix. |
| JUnit4 (`libs.junit`) | pinned in `libs.versions.toml` | GOV-02's `DomainVocabularyDriftGuardTest` | Exact same test runner as `ComponentRegistryDriftGuardTest` — plain JVM, no `@RunWith`, no Robolectric needed for a source-text scan. |
| Kotlin stdlib (`Regex`, `File`) | 2.3.20 (pinned) | Source-text scanning + head-token extraction | No parsing library needed — the existing drift guard proves regex-based source scanning is sufficient and reflection-free (Compose synthetic params make reflection fragile — documented rationale already in `ComponentRegistryDriftGuardTest.kt`'s own KDoc). |

### Supporting

None — GOV-01/02/03 add zero new libraries.

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Bash pathspec/staged-diff fix in `tools/` | Rewrite the guard chain in a JVM/Gradle task | Would touch a working, tested, documented bash toolchain for no benefit; bash is already the established language for this governance layer (`tools/README-api-guard.md`, `tools/test/*.sh`). Do not introduce a second implementation language here. |
| JUnit source-text scan for GOV-02 | Reflection / ClassGraph | Already rejected by this codebase's own drift guard for the same domain (Compose's synthetic `$composer`/`$changed`/`$default` params make reflected signatures fragile) — see `ComponentRegistryDriftGuardTest.kt:16-20`. |
| Extending `docs/DESIGN-INTENT.md` | New `CONTRIBUTING.md` | Rejected by the locked D-04 decision (CONTEXT.md) and confirmed live this session: no `CONTRIBUTING.md` exists, no `.github/` directory exists to wire a PR-template/CI-review checklist into (`ls .github` → no such directory, confirmed this session). |

**Installation:** None required — no new packages for this phase (Kotlin/Android or otherwise).

## Package Legitimacy Audit

Not applicable — this phase installs no external packages (no new Gradle dependency, no new npm/pip
package). Skipping the Package Legitimacy Gate per its own trigger condition ("every phase that
installs external packages").

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────── GOV-03: pre-commit lane classification ───────────────────────────┐
│                                                                                                  │
│  git commit (staged files in the index)                                                         │
│        │                                                                                        │
│        ▼                                                                                        │
│  tools/hooks/pre-commit                                                                         │
│        │  TODAY:  BASE = git describe --tags --abbrev=0 --match 'v*'   (= v1.10.0, STALE)      │
│        │          classify-hub-change.sh --baseline $BASE                                       │
│        │              ├─ verify-additive-diff.sh $BASE   (diffs BASE vs WORKING TREE, cumulative)│
│        │              └─ verify-api-additive.sh  $BASE   (diffs BASE's api.txt vs CURRENT api.txt)│
│        │          → any src rewrite EVER SINCE the tag → lane 2 forever, on EVERY later commit  │
│        │                                                                                        │
│        │  FIX (D-01): classify what THIS COMMIT changes, not history since the tag              │
│        │          git diff --cached -U0 -- src/main   (INDEX vs HEAD, staged delta ONLY)        │
│        │          → a commit that touches nothing in src/main → lane 1, unconditionally         │
│        ▼                                                                                        │
│  lane 1 → commit proceeds                                                                       │
│  lane 2/3 → BLOCKED unless HUB_LANE_OVERRIDE=<lane> is set for THIS commit                       │
└──────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────── GOV-02: domain-vocabulary drift guard (flag-until-allowlisted) ─────────┐
│                                                                                                  │
│  ./gradlew testDebugUnitTest                                                                    │
│        │                                                                                        │
│        ▼                                                                                        │
│  DomainVocabularyDriftGuardTest  (NEW, src/test/.../explorer/, same package as the existing      │
│        │                          ComponentRegistryDriftGuardTest, same source-scan approach)    │
│        │  1. Walk src/main/, exclude explorer/ (identical scope to the existing drift guard)     │
│        │  2. Extract every public top-level @Composable name (duplicate the existing regex-      │
│        │     based extraction method — do not import it, it's `private` on another class)        │
│        │  3. For each name, take its HEAD TOKEN (the leading PascalCase word — see Open           │
│        │     Questions for exact definition + evidence)                                          │
│        │  4. If head token NOT IN the seed primitive-noun allowlist AND name NOT IN the new       │
│        │     DOMAIN_VOCABULARY allowlist (name -> rationale, INTENTIONALLY_UNREGISTERED-style)   │
│        │     → fail() listing the offending name(s)                                              │
│        ▼                                                                                        │
│  RED until a human adds the name+rationale to the allowlist (audit trail) — never advisory-only  │
└──────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────── GOV-01: tier-aware contribution litmus ─────────────────────────────┐
│  docs/DESIGN-INTENT.md (existing file, Phase 1)                                                 │
│        │  Append: a short section stating primitives get the strict gate (GOV-02's guard         │
│        │  enforces it mechanically) and patterns get the loose/opinion-allowed gate (prose only, │
│        │  no enforcement surface exists — no .github/, no CI-review — confirmed this session)    │
└──────────────────────────────────────────────────────────────────────────────────────────────┘
```

### Recommended Project Structure

```
tools/
├── hooks/
│   └── pre-commit                       # MODIFY: staged-delta classification (D-01)
├── classify-hub-change.sh               # MODIFY or leave untouched — see Open Questions
├── verify-additive-diff.sh              # MODIFY (new --staged mode) or leave untouched — see below
├── verify-api-additive.sh               # investigate-only this phase unless scope is widened
└── test/
    ├── test-precommit-hook.sh           # MODIFY: add the reproduced-bug regression fixture
    └── test-verify-additive-diff.sh     # MODIFY (if verify-additive-diff.sh gains a new mode)

src/test/java/io/github/ygaray/yahirandroidtaste/explorer/
├── ComponentRegistryDriftGuardTest.kt   # untouched (existing, unrelated guard)
└── DomainVocabularyDriftGuardTest.kt    # NEW (GOV-02)

docs/
└── DESIGN-INTENT.md                     # MODIFY: append litmus-application/enforcement section (GOV-01)
```

### Pattern 1: Staged-delta classification (GOV-03)

**What:** Replace "diff a stale tag against the whole working tree" with "diff what THIS commit is
about to add, scoped to the AAR-relevant path."
**When to use:** The src-line-rewrite leg of the pre-commit classifier.
**Example (verified live against this repo, exit 0, no offenders, right now with nothing staged):**
```bash
# Source: this session, run against /home/yahir/Projects/Reusable/yahirandroidtaste
git diff --cached -U0 -- src/main
```
This mirrors `verify-additive-diff.sh`'s own REMOVED/ADDED normalize+`comm -23` reconciliation logic
(lines 89-108 of that file) — the fix is which diff feeds that logic, not the reconciliation itself.

### Pattern 2: Fail-until-allowlisted drift guard, reusing an existing extraction shape (GOV-02)

**What:** `ComponentRegistryDriftGuardTest.everyPublicComposableIsRegisteredOrAllowlisted` already
does: walk `.kt` files outside `explorer/`, extract public top-level `@Composable` names via
regex-based source-text scanning (never reflection), compare against a `Map<String,String>`
allowlist, `fail()` with a clear message listing offenders. `DomainVocabularyDriftGuardTest` follows
the exact same shape with a different predicate (head-token-not-in-primitive-noun-list) and a
different allowlist (new, separate from `INTENTIONALLY_UNREGISTERED` — that map answers "is this
composable independently showcased," a different question from "does this name carry domain
vocabulary").
**When to use:** GOV-02's implementation.
**Example (the reusable shape, from the actual live source read this session,
`src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistryDriftGuardTest.kt:150-198`):**
```kotlin
// Source: ComponentRegistryDriftGuardTest.kt, read in full this session — reuse this SHAPE,
// duplicate the body (it's `private fun` on another class, not importable).
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
                // ... (fail loudly if no match — see full source for findDeclarationLineIndex
                //      and the FUN_DECLARATION_REGEX pattern, both quoted verbatim below)
                names.add(match!!.groupValues[1])
            }
        }
        i++
    }
    return names
}
```
`FUN_DECLARATION_REGEX` verbatim (`ComponentRegistryDriftGuardTest.kt:240-241`):
```kotlin
val FUN_DECLARATION_REGEX = Regex(
    """^(?:public\s+)?fun\s+(?:<[^>]*>\s+)?(?:[A-Za-z_][\w.]*\.)?(\w+)\s*\("""
)
```
The `excludedPackages` set (`ComponentRegistryDriftGuardTest.kt:54`): `private val excludedPackages =
setOf("explorer")` — reuse this exact scoping so GOV-02 scans the same universe as GOV-02's sibling
guard (consistency, and it already correctly excludes the showcase harness itself).

### Anti-Patterns to Avoid

- **Making GOV-02 advisory-only (always green / exit 0).** This was the milestone-level `FEATURES.md`
  research's original proposal (dated 2026-08-28, before CONTEXT.md's decisions were locked) — it has
  since been **explicitly superseded** by the locked D-02 decision (fail-until-allowlisted,
  CI-enforced). Do not build the always-green version; it does not match this phase's CONTEXT.md.
- **Refactoring `ComponentRegistryDriftGuardTest`'s private extraction method into a shared utility
  as part of this phase.** Tempting for DRY, but it touches a working, heavily-documented, currently
  green guard test for a phase whose job is adding a *new*, independent guard. Duplicate the ~40-line
  method into the new test file instead (see `## Don't Hand-Roll` for the tradeoff explicitly).
- **Widening `verify-additive-diff.sh`'s path scope instead of fixing the comparison basis.** The
  path scope (`src/main` only) is already correct and already excludes `.planning/`/docs (confirmed
  live: `git ls-tree -z -r --name-only "$BASELINE_COMMIT" -- src/main` at line 58 of that file). The
  bug is NOT an over-broad path scope — it is a stale, cumulative comparison basis. Do not "fix" this
  by touching the path filter; the milestone-level `STACK.md`/`ARCHITECTURE.md` research (dated
  2026-08-28, before the repro) speculated the bug might be path-scope-related — **this session's live
  repro disproves that theory**: `.planning/`-only commits are correctly excluded already; the
  poison is HeatSwatch.kt / the tier-field rollout persisting in-tree since the tag.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Source-text scanning for public top-level `@Composable` names | A new regex/parsing approach from scratch | Duplicate `ComponentRegistryDriftGuardTest`'s `extractPublicTopLevelComposableNames` + `findDeclarationLineIndex` + `FUN_DECLARATION_REGEX` body (it's `private`, not importable, but the logic is proven — handles generic type params, extension-receiver composables, stacked annotations, interleaved KDoc; see its own KDoc for the exact edge cases it was hardened against) | Reinventing this regex will silently regress on the exact edge cases (`fun <T> ChipBar(`, `fun ReorderableCollectionItemScope.EditorItemRow(`) the existing one was specifically hardened for. |
| Vacuous-pass protection for a source scan | Nothing (skip it) | Copy the three-tier assert pattern (`allScannedKtFiles.isNotEmpty()`, `scannedPackageFiles.isNotEmpty()`, `scannedComposableNames.isNotEmpty()`) from `ComponentRegistryDriftGuardTest.kt:60-94` | A broken working-directory assumption in CI silently produces a 0-file scan that vacuously "passes" — the existing guard was specifically hardened against this (its own KDoc calls it out as "Vacuous-pass guard (RESEARCH.md Pitfall 2)"). GOV-02's guard walks the same file tree and is exposed to the identical risk. |
| A "did this commit rewrite a line" diff-reconciliation algorithm | A new normalize/comm-based line-set diff | Reuse `verify-additive-diff.sh`'s existing `normalize()` + `comm -23` logic (lines 89-108) — just point it at `git diff --cached` output instead of `git diff BASE` output | Already handles the one legitimate "false rewrite" case this codebase's own diffs produce (a list operand or list element gaining a trailing `+`/`,`) — a naive re-implementation would likely reintroduce that false positive. |

**Key insight:** Every piece of this phase already has a proven, working analog somewhere in `tools/`
or `src/test/`. The work is narrow, surgical reuse — not new algorithm design.

## Runtime State Inventory

Not applicable — this is not a rename/refactor/migration phase. No stored data, live-service config,
OS-registered state, secrets, or build artifacts are touched. GOV-01/02/03 add prose, a new JVM test
file, and a bash diff-scoping change; nothing renames or migrates existing runtime state.

## Common Pitfalls

### Pitfall 1: Assuming GOV-03's false-flag is a path-scoping bug (it isn't — confirmed live)

**What goes wrong:** Widening/re-tuning `verify-additive-diff.sh`'s path filter (already `src/main`
-only) in the belief that `.planning/`/docs paths are somehow leaking into the scan.
**Why it happens:** The milestone-level research (`STACK.md`/`ARCHITECTURE.md`, both dated
2026-08-28) flagged this as an open hazard ("GOV-03 false-flag root cause is not yet located") because
at that time the bug hadn't been reproduced yet.
**How to avoid:** This session reproduced it live: `bash tools/verify-additive-diff.sh v1.10.0` fails
right now with zero files staged, listing `HeatSwatch.kt` and Phase-1 tier-rollout lines — none of
which are `.planning/` paths. The path filter is already correct; the comparison BASIS (stale tag vs.
current working tree, cumulative) is the actual bug. Fix the basis (D-01: staged delta vs `HEAD`), not
the path filter.
**Warning signs:** A "fix" that changes `git ls-tree -z -r --name-only "$BASELINE_COMMIT" -- src/main`
(line 58) to some other pathspec and the bug persists on the next unrelated commit — that's the tell
this pitfall was walked into.

### Pitfall 2: Fixing only `verify-additive-diff.sh` and leaving `verify-api-additive.sh` with the identical latent bug

**What goes wrong:** GOV-03's stated success criterion ("planning/doc commits land without needing
`HUB_LANE_OVERRIDE`") appears satisfied today because `verify-api-additive.sh` currently degrades to a
no-op (`API-ADDITIVE SKIP`, confirmed live) — `v1.10.0` predates `api.txt`. The moment Phase 5 cuts a
new tag that *includes* `api.txt`, this skip stops happening, and the exact same "stuck lane-3 forever
until the next tag" failure mode reappears for API lines — silently reopening the bug this phase was
supposed to close, on a later phase's timeline, with no test coverage warning anyone.
**Why it happens:** `verify-api-additive.sh` (lines 22-30) does a full-content `comm -23` between the
current on-disk `api.txt` and the baseline tag's `api.txt` — architecturally identical to
`verify-additive-diff.sh`'s bug (a stale, cumulative comparison basis), just currently dormant because
of a data coincidence (no tag yet contains `api.txt`), not because it was designed correctly.
**How to avoid:** See `## Open Questions` — this needs an explicit planner decision (fix both legs now
for symmetry, or fix only the CONTEXT.md-scoped src leg and file a tracked follow-up risk). Either is
defensible, but going in blind (not knowing this) is not.
**Warning signs:** A regression test that only exercises `src/main` rewrites, not `api.txt` rewrites,
will pass today and still miss this — the dormancy means the test suite cannot currently distinguish
"fixed" from "not yet triggered."

### Pitfall 3: Choosing the wrong "head token" definition for GOV-02, silently blinding the guard

**What goes wrong:** "Head token" is not defined precisely anywhere in CONTEXT.md. Two readings are
plausible — (a) the linguistic head of an English noun compound (the RIGHTMOST word: `AlbumCard` is
"a kind of Card"), or (b) the leading/first word (the CS "head of a list" sense: `AlbumCard` starts
with `Album`). **These give opposite, non-overlapping results on this exact corpus.** Every
domain-flavored name in this codebase (`VoiceCard`, `AlbumCard`, `AlbumSourcePickerSheet`,
`HeatSwatch`, `RecordingBottomSheetContent`, `TagListItem`, `TagCreateSheet`, `TagPickerSheet`,
`TagChipWithContextMenu`, `TagChipEditorContent`) carries its domain word (`Voice`/`Album`/`Heat`/
`Recording`/`Tag`) as the LEADING word, and a **structural, non-domain** UI-archetype word
(`Card`/`Sheet`/`Swatch`/`Content`/`Item`/`Chip`) as the TRAILING word — verified by a full survey of
every public top-level `@Composable` in `component/`, `feedback/`, `modifier/`, `theme/` this session
(58 names extracted, tail words alone: `Control, Sheet, Field, Canvas, State, Dialog, Bar, Card,
Value, Item, Content, Row, Scaffold, Swatch, Grid, View, Chip, Popup, Picker, Button, Fab, Badge,
Selector, Ring, Menu, Overlay, Preview, Cue, Editor, Base, Screen, Theme, Ladder, Showcase` — zero of
which are domain-specific). **If "head token" is read as the trailing/rightmost word, the guard is
structurally blind to every domain-coupled name this codebase actually has** — it would seed-allowlist
`Card/Chip/Sheet/Button/Bar/Swatch/Picker` (matching D-03's illustrative list, all of which ARE real
trailing words here) and then never flag anything, because domain words never occupy that position.
**How to avoid:** Read "head token" as the FIRST/leading PascalCase word (interpretation (b)). This is
also the reading that makes the illustrative D-03 allowlist internally consistent as a set of words
"safe to lead a name with" (`Card`, `Chip`, `Sheet` DO already lead real names — `CardBase`,
`CardQuickView`, `CardTypeChip`, `CardEditorShellContent`, `CardTagRow`, `ChipBar`, `SheetScaffold` —
confirmed live), and it is the reading the milestone-level `FEATURES.md` research independently landed
on when naming its own domain-noun examples ("the existing corpus already contains domain-ish names
`VoiceCard`, `AlbumCard`, `TagChip…`" — `FEATURES.md:83`, i.e. flagged by their LEADING word).
**Warning signs:** If, after implementation, the guard's day-one run shows zero names needing
grandfathering, this pitfall was walked into — see `## Open Questions` for the concrete expected
grandfather list under the recommended reading.

**Phase to address:** GOV-02, before writing the test — this is a design decision, not an
implementation detail, and gets it wrong silently (the test still compiles and runs green either way).

## Code Examples

### The reproduced GOV-03 bug, live (run this session)

```bash
# Source: this session, /home/yahir/Projects/Reusable/yahirandroidtaste, nothing staged
bash tools/verify-additive-diff.sh v1.10.0
# → DS-05 FAIL: line rewritten/removed in a pre-existing file: (13 lines, incl. HeatSwatch.kt
#   comment reword from commit 5b01532 and Phase 1's ComponentRow/ExplorerIndexScreen tier rollout)
# → exit 1  (this is why EVERY commit today, including a pure .planning/ one, needs HUB_LANE_OVERRIDE)
```

### The D-01 fix, verified live (run this session, exit 0, clean)

```bash
# Source: this session, same repo state as above
git diff --cached -U0 -- src/main
# → (empty output)
# → exit 0
```

### `verify-api-additive.sh`'s current dormant-skip state (why Pitfall 2 above is real)

```bash
# Source: this session
API_FILE="$(pwd)/api.txt" bash tools/verify-api-additive.sh v1.10.0
# → API-ADDITIVE SKIP: baseline v1.10.0 has no /home/…/api.txt yet — API-surface check degraded to
#   source-only until a tag includes it
# → exit 0   (silently masks the identical latent bug — see Pitfall 2)
```

### Existing fixture-test pattern to extend for the GOV-03 regression (from `tools/test/test-precommit-hook.sh`, read in full this session)

```bash
# Source: tools/test/test-precommit-hook.sh (existing, passing test — extend with a new case)
# The NEW regression scenario GOV-03 needs, matching the exact reproduced bug shape:
#   1. commit a src/main rewrite (lane 2) with HUB_LANE_OVERRIDE=2 — allowed, as today
#   2. commit an UNRELATED, docs-only change afterward, with NO override
#   3. assert commit #2 succeeds (today, under the un-fixed hook, it would incorrectly be blocked —
#      this is the exact 5b01532-then-.planning/-commit sequence this phase reproduces and fixes)
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|---------------|--------|
| Pre-commit classifies "everything since the last tag" (cumulative, tag-baseline) | Pre-commit classifies "what this commit itself stages" (per-commit, `HEAD`-baseline) | This phase (GOV-03, D-01) | A commit that touches nothing in `src/main` is unconditionally lane 1 — no dependency on tree history, no dependency on when the last tag was cut. |
| `INTENTIONALLY_UNREGISTERED` = "sub-part exempt from independent showcase" | (unchanged) + a NEW, separate allowlist = "name acknowledged as carrying domain vocabulary, with rationale" | This phase (GOV-02) | Two allowlists answering two different questions — do not conflate them into one map; `ComponentRegistry.kt`'s own KDoc states `entries` + `INTENTIONALLY_UNREGISTERED` is a closed registration concern, not a naming-review concern. |

**Deprecated/outdated:** None — nothing in this phase replaces a previously-shipped mechanism; it's
purely additive tooling plus a bug fix.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | "Head token" = the leading/first PascalCase word of a composable name (not the linguistic-head/trailing word) | Pitfall 3, Architecture Patterns, Open Questions | If wrong, GOV-02's guard is built with an inverted/blind detection rule and will need a rewrite of its predicate + seed allowlist + grandfather list. Mitigated by strong corroborating evidence (full corpus survey + independent milestone-research naming the same examples), but CONTEXT.md itself does not pin this down explicitly — flagged for a quick confirm at plan/discuss time if there's any doubt. |
| A2 | The ~10 "grandfathered domain names" D-02's rationale refers to are the composables whose FIRST token is `Voice`, `Album`, `Heat`, `Recording`, or `Tag` (14 exact names found, see Open Questions) | Open Questions | If the actual intended count/set differs, the seed `DOMAIN_VOCABULARY` allowlist will be incomplete on day one and the guard will go red on names nobody expected — not harmful (fail-until-allowlisted is designed to be fixed by adding a line), but will require an extra pass before the test suite goes green. |
| A3 | `verify-api-additive.sh` shares GOV-03's exact root-cause bug shape, currently dormant only because the baseline tag predates `api.txt` | Pitfall 2, Summary | If this analysis is wrong (e.g. if Phase 5's tag-cut ritual re-derives the baseline differently), the flagged residual risk is a false alarm — low cost either way, since it's presented as an open question, not baked into a plan task. |

## Open Questions

1. **Should GOV-03's staged-delta fix also cover `verify-api-additive.sh` (the lane-3 API-line
   check), or only `verify-additive-diff.sh` (the lane-2 src-line check) as CONTEXT.md's D-01 text
   literally scopes it to (`src/main` only)?**
   - What we know: both scripts share the identical "compare full current state vs. a stale tag"
     architecture; `verify-additive-diff.sh`'s manifestation is reproduced and live today,
     `verify-api-additive.sh`'s is dormant only because `v1.10.0` predates `api.txt` (confirmed live,
     `API-ADDITIVE SKIP`).
   - What's unclear: whether fixing only the literal D-01 scope (src/main) leaves a ticking time-bomb
     for Phase 5 (the first phase to cut a tag that includes `api.txt`), or whether that's an
     acceptable, explicitly-tracked residual risk for a later phase to own.
   - Recommendation: extend the same `git diff --cached` vs `HEAD` principle to the API-line check
     too (compare staged `api.txt` content against `HEAD`'s committed `api.txt` content, not against
     a tag) for symmetry and to close the whole bug class in one phase — but this is a scope call for
     the planner/human, since CONTEXT.md's D-01 doesn't explicitly commit to it. At minimum, add a
     regression test asserting the current dormant state is understood (so a future maintainer isn't
     surprised), even if the fix itself is deferred.

2. **Exact "head token" extraction rule for GOV-02 (see Pitfall 3 for the full analysis).**
   - What we know: reading it as the FIRST PascalCase word is strongly corroborated by (a) a live,
     full corpus survey showing every domain-flavored name in this codebase leads with its domain
     word and trails with a structural word, and (b) the milestone-level `FEATURES.md` research
     independently naming `VoiceCard`/`AlbumCard`/`TagChip…` as "domain-ish" by their leading word.
   - What's unclear: CONTEXT.md's D-03 text does not explicitly define "head token" as first-vs-last;
     it is inferred, not stated.
   - Recommendation: proceed with FIRST-word extraction (a simple split on the PascalCase-word
     boundary regex `[A-Z][a-z0-9]*`, taking element `[0]`). If the planner or a quick human check-in
     disagrees, this is a single-line change to the extraction function, not an architectural rework.

3. **Concrete seed allowlists, derived from the live survey this session (component/, feedback/,
   modifier/, theme/ packages, 58 public top-level `@Composable` names extracted):**
   - **Primitive-noun allowlist (safe leading OR trailing words already established in this corpus)**
     should be seeded from ALL distinct trailing words found live: `Control, Sheet, Field, Canvas,
     State, Dialog, Bar, Card, Value, Item, Content, Row, Scaffold, Swatch, Grid, View, Chip, Popup,
     Picker, Button, Fab, Badge, Selector, Ring, Menu, Overlay, Preview, Cue, Editor, Base, Screen,
     Theme, Ladder, Showcase` — plus the leading uses already established for `Card`, `Chip`, `Sheet`
     (`CardBase`, `CardQuickView`, `CardTypeChip`, `CardEditorShellContent`, `CardTagRow`, `ChipBar`,
     `SheetScaffold`, all confirmed live this session).
   - **`DOMAIN_VOCABULARY` grandfather allowlist (names needing an explicit acknowledged entry on
     day one, under the FIRST-word reading)** — 14 names, 5 distinct domain nouns, found live:
     `VoiceCard`, `VoiceRenameTagsSheet` (Voice); `AlbumCard`, `AlbumSourcePickerSheet`,
     `AlbumTitleConfirmSheet` (Album); `HeatSwatch` (Heat); `RecordingBottomSheetContent`
     (Recording); `TagListItem`, `TagCreateSheet`, `TagCreateSheetContent`, `TagChipWithContextMenu`,
     `TagPickerSheet`, `TagPickerSheetContent`, `TagChipEditorContent` (Tag). This is close to, but
     not exactly, CONTEXT.md's "~10" estimate (14 names / 5 distinct nouns) — the discrepancy is
     expected for a rough discussion-time estimate; treat the live-surveyed 14 as authoritative.
   - **Edge case:** `YahirAndroidTasteTheme` (in `INTENTIONALLY_UNREGISTERED`, so in-scope for the
     scan if GOV-02 reuses the identical scan universe) has a first-token of `YahirAndroidTaste` —
     the library's own brand name, not a domain noun at all. Recommend a one-off allowlist entry with
     a rationale noting it's the hub's own theme wrapper, not a consumer-domain leak, rather than
     trying to special-case brand names generically.
   - **Borderline, left to the planner/executor's judgment:** `WaveformCanvas`, `SwipeableActionRow`,
     `RevealActionRow` (all in `INTENTIONALLY_UNREGISTERED`) — `Waveform`/`Swipeable`/`Reveal` read as
     generic UI-interaction/visualization descriptors, not consumer-domain nouns, but were not as
     unambiguous as the Voice/Album/Heat/Recording/Tag set above.

## Environment Availability

Skipped — this phase has no external tool/service/runtime dependency beyond what's already installed
and verified working in this session (bash, git, the JVM/Gradle toolchain already pinned per root
`CLAUDE.md`).

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit4 (plain JVM, no Robolectric needed for either new artifact) for GOV-02; bash fixture-tests (existing pattern in `tools/test/*.sh`) for GOV-03 |
| Config file | none beyond the existing Gradle module config (`build.gradle.kts`, root) |
| Quick run command | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` (GOV-02); `bash tools/test/test-precommit-hook.sh` (GOV-03) |
| Full suite command | `./gradlew testDebugUnitTest` (GOV-02, runs alongside `ComponentRegistryDriftGuardTest` and everything else); `bash tools/test/run-all.sh` (GOV-03, runs all `tools/test/test-*.sh`) |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| GOV-01 | Litmus documented, strict half enforced where feasible | doc-review + delegated to GOV-02's test | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` | ❌ Wave 0 — new test file |
| GOV-02 | Domain-noun drift guard flags (fail-until-allowlisted) | unit (JVM source-scan) | `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` | ❌ Wave 0 — new file, `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` |
| GOV-03 | `.planning/`/docs commits land without `HUB_LANE_OVERRIDE`; source additivity still guarded | integration (bash fixture) | `bash tools/test/test-precommit-hook.sh` | ✅ exists — extend with the new regression case (see Code Examples) |

### Sampling Rate

- **Per task commit:** `./gradlew testDebugUnitTest --tests "*DomainVocabularyDriftGuardTest*"` (once
  the file exists) and `bash tools/test/test-precommit-hook.sh` for any `tools/` edit.
- **Per wave merge:** `./gradlew testDebugUnitTest` (full) + `bash tools/test/run-all.sh` (full).
- **Phase gate:** both full suites green before `/gsd-verify-work`; additionally, manually reproduce
  the ORIGINAL bug's exact commit sequence (a `.planning/`-only commit after a `src/main` rewrite in
  history) against the fixed hook to close the loop on the phase's own stated success criterion.

### Wave 0 Gaps

- [ ] `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` —
  new file, covers GOV-01 (strict-half enforcement) + GOV-02.
- [ ] `tools/test/test-precommit-hook.sh` — extend with the "rewrite commit, then unrelated commit"
  regression case that reproduces and proves the fix for GOV-03's exact bug.
- [ ] Framework install: none — JUnit4 and bash are already wired into `./gradlew testDebugUnitTest`
  and `tools/test/run-all.sh` respectively.

## Security Domain

Not applicable in the ASVS sense — this phase touches no authentication, session management, access
control, input validation of untrusted external data, or cryptography. It is internal governance
tooling (a git hook and a JVM source-text scanner operating only on this repo's own tracked files) and
documentation. No STRIDE-relevant threat pattern applies to a local pre-commit hook or a build-time
JUnit test that reads files from the local `src/main` tree it is itself part of.

## Sources

### Primary (HIGH confidence — read/run live this session)

- `tools/hooks/pre-commit` — full source read, classification logic understood line-by-line.
- `tools/classify-hub-change.sh` — full source read, lane-mapping logic understood.
- `tools/verify-additive-diff.sh` — full source read; **live-executed** (`bash tools/verify-additive-diff.sh v1.10.0`) confirming the reproduced bug.
- `tools/verify-api-additive.sh` — full source read; **live-executed** confirming the dormant-skip state.
- `tools/README-api-guard.md` — full source read (override syntax, apiDump discipline).
- `tools/test/test-precommit-hook.sh`, `tools/test/test-classify-hub-change.sh`, `tools/test/test-verify-additive-diff.sh` — full source read, existing fixture-test pattern.
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` — full source read (Entry, entries, Tier enum, INTENTIONALLY_UNREGISTERED, init guards).
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistryDriftGuardTest.kt` — full source read (the exact pattern GOV-02 reuses).
- `docs/DESIGN-INTENT.md` — full source read (existing litmus prose, worked examples).
- `.planning/phases/03-governance-gates/03-CONTEXT.md` — full source read (the four locked decisions + Runtime Decisions refresh).
- `.planning/REQUIREMENTS.md` — full source read (GOV-01/02/03 exact text).
- Live survey of every public top-level `@Composable` in `component/`, `feedback/`, `modifier/`, `theme/` (58 names extracted via direct `grep`/`awk` against the tracked source tree this session) — HIGH.
- `git describe --tags`, `git tag -l`, `git show 5b01532 --stat`, `git log v1.10.0..HEAD -- 'src/main/**/*.kt'`, `git diff --stat v1.10.0 -- src/main`, `git diff --cached -U0 -- src/main` — all run live this session.
- `ls .github` — confirmed no `.github/` directory exists (supports Runtime Decision on GOV-01 location).
- `config/detekt/detekt.yml` — spot-checked for a duplication rule that might bear on the reuse-vs-duplicate decision for GOV-02's extraction method; none found in a 72-line config.

### Secondary (MEDIUM confidence — milestone-level research, dated 2026-08-28, cross-checked against live state this session)

- `.planning/research/ARCHITECTURE.md` — cross-checked; its GOV-03 "not yet located" hazard is now resolved (see Pitfall 1) and its Hazard 1 (Phase 1 lane-2/3) is now historical fact (confirmed via `git log`).
- `.planning/research/STACK.md` — cross-checked; its Phase 3 technique notes match this document's recommendations (JUnit source-scan reuse, bash pathspec fix).
- `.planning/research/PITFALLS.md`, `.planning/research/FEATURES.md` — cross-checked; `FEATURES.md`'s domain-noun examples (`VoiceCard`, `AlbumCard`, `TagChip…`) independently corroborate this document's "head token = leading word" reading (Assumption A1).

### Tertiary (LOW confidence)

- None used — every claim in this document traces to a primary or secondary source above.

## Metadata

**Confidence breakdown:**
- GOV-03 root cause + fix mechanism: HIGH — reproduced live, fix mechanism verified live (both the
  bug and its fix were executed as actual commands this session, not inferred).
- GOV-02 implementation shape: HIGH — directly reuses a fully-read, well-documented existing pattern.
- GOV-02 "head token" exact semantics: MEDIUM — strongly corroborated but not explicitly pinned down
  in CONTEXT.md; flagged as Assumption A1 / Open Question 2.
- GOV-01 doc location + enforcement split: HIGH — confirmed live (`docs/DESIGN-INTENT.md` exists,
  `CONTRIBUTING.md` does not, `.github/` does not exist).
- `verify-api-additive.sh` residual-risk finding: MEDIUM — the architectural analysis is HIGH
  confidence (read + live-executed), but whether it's in-scope for THIS phase is an open decision,
  not a research fact.

**Research date:** 2026-09-01
**Valid until:** Effectively indefinite for the architectural findings (this is a stable, mature,
already-built system); the specific live command outputs (e.g. exact `HUB_LANE_OVERRIDE` blockers,
exact composable counts) should be re-verified if execution starts more than a few days after this
research, since the milestone's other phases (2, 4, 5) are actively landing commits that shift the
working tree.

---

## RESEARCH COMPLETE
