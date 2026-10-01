# Phase 13: Catalog integrity & docs - Research

**Researched:** 2026-09-30
**Domain:** Verification/integrity gate for a reusable Jetpack Compose design-system library (ComponentRegistry drift guards, Metalava `apiCheck` additive-API proof, one-way-dependency audit, doc-drift correction) — NOT new feature authoring
**Confidence:** HIGH (every claim below is grounded in this session's own `Read`/`Bash` inspection of the live repo — source files, test files, and actual tool runs — not training-data assumption)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**D-01 [family]:** Register the voice cohort as ONE new tenth "Voice Command" `ComponentRegistry` family — a new entries slice appended to the concatenation, a new `ExplorerFamilies` const, and a new `ORDERED_KEYS` row — with each `Entry` carrying `tier = PATTERN` and a full 4-cell states matrix (rather than scattering into Cards/Sheets/Feedback). Matches the Tactile Foundation family-9 precedent.

**D-02 [tag-procedure]:** `v2.4.0` is "cut" only after: (1) green FULL suite (`./gradlew testDebugUnitTest` — all four drift guards: registry, tier, domain-vocabulary, generated-symbol — plus zero-baseline detekt), (2) Metalava `apiCheck` additive vs `v2.3.0` (refresh + commit `api.txt`), (3) the tagged commit pushed, and (4) JitPack resolves `v2.4.0` from a clean Gradle cache. THEN message the orchestrator the full §11 ledger row (repo, tag, commit, coordinate, contents, evidence) — never self-write the §11 ledger (A14). — **Reversibility:** one-way — the tag is immutable once pushed; a defect needs a new `v2.4.1` patch tag + superseded row. **NOTE:** per the orchestrator SPLIT ruling (INC-2026-09-30-01), this decision's actual tag-cut now belongs to **Phase 14** — Phase 13 cuts NO tag; it only produces the green-suite + additive-API evidence D-02 requires as input to Phase 14.

**D-03 [doc-drift]:** Correct the stale "seven families" wording to the post-cohort count (ten) in the 4 load-bearing files — root `CLAUDE.md`, `README.md`, `ComponentRegistry` KDoc, and `API.md` — as a P13 gate; treat the generated `.planning/codebase/*` map snapshots as out of scope (regenerated artifacts).

### Claude's Discretion

Do NOT edit the hardcoded publishing `version` in `build.gradle.kts` — JitPack derives the version from the resolved git ref; the tag is authoritative.

### Ship guards (research confident — not gray areas, but binding; Phase 14 scope, kept here as handoff context)

Milestone close cuts NO git tag (SHIP-02): the ONLY tag this milestone produces is the `v2.4.0` release coordinate. A bare `v2.2` milestone-marker tag already leaked into the JitPack coordinate namespace at the v2.0 close — do not repeat it. Git tags in this repo ARE JitPack coordinates.

### Deferred Ideas (OUT OF SCOPE)

Consumer repins (SecondBrain, CalTracker → `v2.4.0`) are Wave-1 work in the consumers' own channels, not this phase.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CAT-01 | Every new public composable is registered in `ComponentRegistry` (or allowlisted in `INTENTIONALLY_UNREGISTERED`) with its full 4-cell states matrix, and the CATALOG-03 drift guard passes in the **full** test suite | Already satisfied at current HEAD — verified by reading `ComponentRegistry.kt`/`VoiceCommandFamilyScreen.kt` and running `./gradlew testDebugUnitTest` (full suite, green). Phase 13's task is to RE-RUN and CAPTURE this as evidence, not to author new registrations (see Pitfall 2) |
| API-01 | The public API is strictly additive versus `v2.3.0` — Metalava `apiCheck` net-additive, no removals or signature changes to existing symbols | The naive baseline script (`verify-api-additive.sh`/`classify-hub-change.sh`) reports a false-positive break on `ClearableTextField`'s new trailing optional params; the authoritative Metalava semantic check (swap-baseline technique, Code Examples section) confirms true additivity. Phase 13's task must use the swap-baseline technique as its evidence, not the raw shell-script exit code (see Pitfall 1) |
| INV-01 | The new UI adds no dependency on OkHttp or any `voice-action-engine` module; every new composable takes data + actions as parameters | Already satisfied — `build.gradle.kts` diff since Phase 10's start is empty, and a grep of every new `component/`/`model/` file found zero forbidden imports (only KDoc prose documenting the absence). Phase 13's task is to re-run this cheap verification fresh, not inherit it from memory (see Pitfall 5) |
</phase_requirements>

## Summary

Phase 13 is almost entirely a **verification and doc-correction gate**, not an authoring phase. Direct inspection of the current working tree shows Phases 10, 11, and 12 already did the registration work CAT-01 names: `ExplorerFamilies.VOICE_COMMAND` exists, `ComponentRegistry.entries` already concatenates `voiceCommandFamilyEntries` as its tenth list, and all 5 new public composables (`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`, `OutcomeSheet`, `ClarificationBar`) are already registered with full 4-cell `states` matrices and `tier = PATTERN`. Running the actual drift-guard tests, `apiCheck`, and `detekt` at current HEAD all pass green already (`./gradlew testDebugUnitTest`, `./gradlew apiCheck`, `./gradlew detekt` were all run this session — see Validation Architecture section for exact commands/output). `DomainVocabularyDriftGuardTest`'s `PRIMITIVE_NOUN_ALLOWLIST` was also pre-emptively widened in Phases 10/11 with `Provider`, `Model`, `Approach`, `Outcome`, `Clarification`. `build.gradle.kts` gained no new dependency across Phases 10-12, and a source grep of the new files found zero OkHttp/Retrofit/serialization/engine imports — INV-01 already holds.

The one substantive finding requiring a verification task (not a code fix) is an **additive-API false positive**: the repo's own `tools/verify-api-additive.sh` / `tools/classify-hub-change.sh --baseline v2.3.0` reports **LANE 3 (API break)** right now, because `ClearableTextField` gained two new trailing optional parameters (`visualTransformation`, `revealToggle`, Phase 10 VSET-01) and the script does a naive per-line text diff of `api.txt` — it cannot tell "appended optional params" from "removed line." This session verified, via the **authoritative tool** (temporarily swapping `api.txt` to v2.3.0's committed content and running the real `./gradlew apiCheck` → `metalavaCheckCompatibilityRelease`), that Metalava's actual semantic compatibility check **passes** — the API genuinely is additive. Phase 13's API-01 task must use this swap-and-check technique (or an equivalent human-reviewed override) rather than trusting the shell script's raw exit code, or the plan will incorrectly conclude a real regression needs fixing.

What remains, concretely: (1) one verification task that runs the full suite + the correct additive-proof technique and produces the evidence Phase 14 needs, (2) a doc-correction task touching the 4 named files — but note the "seven" wording actually appears in **5 separate line locations across 2 files** (`CLAUDE.md:35`, `README.md:21`, `README.md:59-60`, `README.md:73`, `README.md:79`), not one spot per file as the phase description's line estimates imply, plus `ComponentRegistry.kt`'s KDoc (lines 88, 92) and `API.md`'s "nine" wording (lines 1, 7-11, 27).

**Primary recommendation:** Treat Phase 13 as a verification-and-correction gate with exactly two plans: (1) run + document the full-suite/apiCheck/detekt/INV-01 evidence using the swap-baseline technique for the additive proof, (2) correct every "seven"/"nine" family-count occurrence to "ten" across the 4 named files (5+ line locations). Do not add new composables, do not touch `ClearableTextField`'s signature, and do not expand scope to the separately-stale composable-count numbers in README.md/API.md (41/51/53/56/57 vs the live 61 registered + 5 unregistered = 66) — CONTEXT.md D-03 scopes this phase to the family-count wording only.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| ComponentRegistry registration (CAT-01) | Library (design-system hub, `explorer/`) | — | Registry is a hub-internal single source of truth; no consumer or backend involvement |
| Metalava additive-API proof (API-01) | Library build tooling (Gradle/Metalava) | — | Pure build-time static analysis against the committed `api.txt`; no runtime component |
| One-way-dependency audit (INV-01) | Library (`build.gradle.kts` + source imports) | — | Structural property of the library's own dependency graph; verified by absence, not by a running system |
| Doc-drift correction | Library docs (`CLAUDE.md`, `README.md`, `API.md`, KDoc) | — | Static markdown/KDoc text; no code or runtime behavior change |

This phase has no Browser/SSR/Backend/CDN tiers — it is 100% library-internal static verification and documentation, consistent with `yahirandroidtaste` being a Compose UI component library with no network or persistence layer of its own.

## Standard Stack

No new libraries are introduced or required by this phase. INV-01 explicitly forbids adding any dependency. The existing toolchain already in `build.gradle.kts` is sufficient and verified working this session:

| Tool | Version/Task | Purpose | Status this session |
|------|------|---------|--------------|
| Metalava (via `me.tylerbwong.gradle.metalava` plugin) | `apiDump`/`apiCheck` → `metalavaGenerateSignatureRelease`/`metalavaCheckCompatibilityRelease` | Additive-API proof (API-01) | [VERIFIED: ran `./gradlew apiCheck` at HEAD → BUILD SUCCESSFUL, 2026-09-30] |
| detekt | `./gradlew detekt` | Zero-baseline static analysis | [VERIFIED: ran `./gradlew detekt` at HEAD → BUILD SUCCESSFUL, UP-TO-DATE, `config/detekt-baseline.xml` confirmed literally empty (`<CurrentIssues/>`), 2026-09-30] |
| JUnit4 (plain, no Robolectric) | `./gradlew testDebugUnitTest` | `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `ComponentRegistryTierTest`, `GeneratedSymbolDriftGuardTest` + all other unit/Robolectric tests | [VERIFIED: ran `./gradlew testDebugUnitTest` at HEAD → BUILD SUCCESSFUL, 2026-09-30] |
| `tools/verify-api-additive.sh`, `tools/classify-hub-change.sh` | shell, line-diff based | Intended additive-vs-baseline signal; **has a known false positive this phase must route around** (see Common Pitfalls) | [VERIFIED: ran both against `--baseline v2.3.0` at HEAD this session — `verify-api-additive.sh` exits 3 (FAIL), `classify-hub-change.sh` reports LANE 3, both due to the single `ClearableTextField` line below] |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Trusting `verify-api-additive.sh`'s raw exit code for API-01 evidence | Swap-baseline + `./gradlew apiCheck` (Metalava's real semantic check) | The shell script is fast and zero-setup but line-diff-naive; Metalava's real check costs one extra `cp`/`git show`/`gradlew apiCheck`/restore cycle (~1 min) but gives the authoritative verdict |

**Installation:** None — no new packages.

## Package Legitimacy Audit

**Not applicable.** This phase installs no external packages. `build.gradle.kts`'s dependency block is unchanged across Phases 10, 11, and 12 [VERIFIED: `git diff f10b560^..7c3b5fb -- build.gradle.kts` produced no output, 2026-09-30], and INV-01 (success criterion #3) explicitly forbids adding one. No `npm view`/`pip index`/`cargo search` legitimacy check is needed because no package name is being introduced.

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│  Phase 13 — verification & doc-correction gate (no new runtime code)  │
│                                                                         │
│  INPUT: current HEAD (Phases 10-12 already merged)                    │
│       │                                                                 │
│       ▼                                                                 │
│  ┌───────────────────────────┐   ┌──────────────────────────────────┐ │
│  │ 1. Full test suite          │   │ 2. Additive-API proof             │ │
│  │ ./gradlew testDebugUnitTest │   │  a. ./gradlew apiCheck (local      │ │
│  │  → ComponentRegistryDrift   │   │     sync: code == committed api.txt)│ │
│  │    GuardTest (CAT-01)       │   │  b. swap api.txt ← v2.3.0, re-run  │ │
│  │  → DomainVocabularyDrift    │   │     apiCheck (TRUE v2.3.0 compat), │ │
│  │    GuardTest                │   │     then restore api.txt           │ │
│  │  → ComponentRegistryTier    │   │  c. (optional corroboration only)  │ │
│  │    Test                     │   │     tools/classify-hub-change.sh   │ │
│  │  → GeneratedSymbolDrift     │   │     --baseline v2.3.0 — expect     │ │
│  │    GuardTest                │   │     LANE 3 false positive, documented│
│  └──────────────┬──────────────┘   └───────────────┬──────────────────┘ │
│                 │                                     │                 │
│                 ▼                                     ▼                 │
│  ┌───────────────────────────┐   ┌──────────────────────────────────┐ │
│  │ 3. INV-01 dependency audit  │   │ 4. Doc-drift correction            │ │
│  │  grep new component/model   │   │  CLAUDE.md:35, README.md:21/59-60/ │ │
│  │  files for okhttp/retrofit/ │   │  73/79, ComponentRegistry.kt:88/92,│ │
│  │  serialization/engine       │   │  API.md:1/7-11/27 — "seven"/"nine" │ │
│  │  imports + diff             │   │  → "ten"                           │ │
│  │  build.gradle.kts           │   │                                    │ │
│  └──────────────┬──────────────┘   └───────────────┬──────────────────┘ │
│                 │                                     │                 │
│                 └───────────────┬─────────────────────┘                 │
│                                 ▼                                       │
│                    Phase 13 verification evidence                       │
│                    (handed to Phase 14's ship gate, §11 step 1-2)       │
└─────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Implementation |
|-----------|----------------|----------------|
| `ComponentRegistryDriftGuardTest` | Asserts every public top-level `@Composable` outside `explorer/` is registered XOR allowlisted | Already green at HEAD; no code change expected |
| `DomainVocabularyDriftGuardTest` | Asserts every public composable's head token is a primitive noun or acknowledged domain vocabulary | Already green; `Provider`/`Model`/`Approach`/`Outcome`/`Clarification` pre-widened in Phases 10/11 |
| `ComponentRegistryTierTest` | Spot-checks 3 named entries' `Tier` values match `docs/DESIGN-INTENT.md` | Unrelated to voice cohort; already green, unaffected by this phase |
| `GeneratedSymbolDriftGuardTest` | Asserts no Hilt/Dagger-generated symbol re-enters `api.txt` | Already green; unaffected by this phase |
| `tools/verify-api-additive.sh` + `tools/classify-hub-change.sh` | Line-diff based additive-vs-baseline signal, used by the pre-commit hook | Currently reports a false-positive LANE 3 against `v2.3.0` for this milestone's range — see Pitfall below |
| `./gradlew apiCheck` (Metalava) | Real semantic compatibility check — but as wired, only checks current code vs. the **currently committed** `api.txt`, not a historical tag | Must be re-pointed at `v2.3.0`'s `api.txt` (temporarily) to get the true cross-version verdict |
| 4 doc files (`CLAUDE.md`, `README.md`, `ComponentRegistry.kt` KDoc, `API.md`) | Carry the stale "seven"/"nine" family-count wording | Pure text edits |

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Proving API-01 additivity vs `v2.3.0` | A new/custom API-diff script | The existing Metalava `apiCheck` task, re-pointed at `v2.3.0`'s `api.txt` via a temporary swap (`git show v2.3.0:api.txt > api.txt`, run, restore) | Metalava already does real JVM-signature-level compatibility analysis (proven in `tools/README-api-guard.md`'s own negative-control test); writing a new script duplicates existing, better tooling |
| Confirming no forbidden dependency | A dependency-graph analysis tool | `git diff <baseline>..HEAD -- build.gradle.kts` (expect empty) + `grep -rniE "okhttp|retrofit|voice.?action.?engine|kotlinx\.serialization|com\.google\.gson"` over the new `component/`/`model/` files | Already-verified-empty this session; a heavier tool is unnecessary for a presentational-only library with zero gradle changes |

**Key insight:** Every mechanism this phase needs to invoke already exists and is already wired into the build (`testDebugUnitTest`, `apiCheck`, `detekt`, `verify-api-additive.sh`). The work is in correctly INTERPRETING their output — especially the one script with a known false-positive mode — not in building new tooling.

## Common Pitfalls

### Pitfall 1: Trusting `tools/verify-api-additive.sh` / `classify-hub-change.sh`'s raw exit code for API-01 evidence

**What goes wrong:** Running `tools/classify-hub-change.sh --baseline v2.3.0` (or the pre-commit hook, which auto-selects the same baseline via `git describe --tags --abbrev=0 --match 'v*'`) returns **LANE 3 (exit 3, API break)** right now. A plan that treats this as ground truth will conclude the milestone's public API has a real breaking change and either (a) incorrectly block the Phase 14 ship gate, or (b) attempt to "fix" `ClearableTextField` by reverting/restructuring its already-shipped, already-tested, already-consumed (by `ProviderKeyCard`) signature — which would itself be wasted/regressive rework.

**Why it happens:** `verify-api-additive.sh` does `comm -23 <(git show $BASE:$API_FILE | sort -u) <(sort -u "$API_FILE")` — a per-LINE set-difference. `ClearableTextField`'s signature line in `api.txt` changed from a 10-param to a 12-param line when Phase 10 (VSET-01) added `visualTransformation` and `revealToggle` as new trailing, defaulted (optional) parameters — a textbook *additive* signature change (no removal, no required param, no type change to any existing param, verified below). But because the OLD exact line of text is no longer present verbatim in the file, the script reports it as "removed."

**How to avoid:**
1. Run `./gradlew apiCheck` normally first — this will PASS (it only checks current code vs. the currently-committed `api.txt`, so it's a "no local drift" check, not a cross-version check). [VERIFIED: ran this exact command at HEAD, 2026-09-30, BUILD SUCCESSFUL]
2. For the TRUE v2.3.0-compatibility verdict, temporarily swap in v2.3.0's `api.txt` and re-run the real Metalava check:
   ```bash
   cp api.txt /tmp/api.txt.current.bak
   git show v2.3.0:api.txt > api.txt
   ./gradlew apiCheck   # the authoritative verdict
   cp /tmp/api.txt.current.bak api.txt   # MUST restore before committing anything
   ```
   [VERIFIED: ran this exact sequence this session, 2026-09-30 — result: `BUILD SUCCESSFUL in 52s` (metalavaCheckCompatibilityRelease executed fresh, not cached) — Metalava's real semantic check confirms the full API surface, including `ClearableTextField`'s new params, is backward-compatible with `v2.3.0`.]
3. Document this result (and the single offending line it supersedes) as the API-01 evidence, rather than the shell script's exit code. Confirm `api.txt` is restored to its current (HEAD) content before any commit — `git status --short api.txt` must be empty afterward. [VERIFIED: confirmed empty after restoring, 2026-09-30]
4. If a reviewer wants the lane classifier to also go green, the correct action per `tools/README-api-guard.md` is `HUB_LANE_OVERRIDE=3` on a commit that includes a written rationale — not a code change. (Note: the original Phase 10 commit message that introduced this exact line records `HUB_LANE_OVERRIDE=2`, not 3 — there is a discrepancy between what the author recorded at commit time and what `classify-hub-change.sh --baseline v2.3.0` reports when run against current HEAD. This session did not determine why; it is flagged in Open Questions below. It does not change the Metalava-verified conclusion that the API is additive.)

**Warning signs:** `classify-hub-change.sh`/the pre-commit hook reporting LANE 2 or 3 for a range that "feels" purely additive; a `comm -23` style diff citing a line that is a SUPERSET of an old line (more params, same prefix) rather than a genuinely deleted symbol.

**Phase to address:** Phase 13 (API-01's verification task). This is a verification-interpretation task, not a code-authoring task.

---

### Pitfall 2: Assuming CAT-01 requires NEW registration work

**What goes wrong:** A plan that schedules tasks to "add a tenth ComponentRegistry family and register the 5 voice composables" duplicates work already complete. `ExplorerFamilies.VOICE_COMMAND` [VERIFIED: `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerIndexScreen.kt:65,78` — `const val VOICE_COMMAND = "voice_command"` and `VOICE_COMMAND to "Voice Command"`], the `voiceCommandFamilyEntries` concatenation into `ComponentRegistry.entries` [VERIFIED: `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt:94-103` — the `val entries` list literally ends with `... + tactileFoundationFamilyEntries + voiceCommandFamilyEntries`], and all 5 `Entry(...)` declarations with full 4-cell `states` and `tier = PATTERN` [VERIFIED: `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt:56-185` — 5 `ComponentRegistry.Entry(` blocks for `ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`, `OutcomeSheet`, `ClarificationBar`] already exist. Running `./gradlew testDebugUnitTest` (full suite) at HEAD is already green [VERIFIED: ran this exact command, 2026-09-30, BUILD SUCCESSFUL].

**Why it happens:** The phase description and CONTEXT.md D-01 read as a forward-looking design decision ("register the voice cohort as ONE new tenth family") because that IS what Phase 10 Plan 01's own commit message calls "13-prep groundwork" — Phases 10-12 did the registration incrementally, following PITFALLS.md's own advice ("Make registration part of the definition of done for each new composable in the phase that authors it ... not a Phase-13 cleanup"). CONTEXT.md was written before this incremental work was fully confirmed complete.

**How to avoid:** Phase 13's plan should open with a verification task ("confirm CAT-01 is already satisfied: run the full suite, inspect `ComponentRegistry.entries` for all 5 new names") BEFORE scheduling any registration work. If the full suite is green (it is, as of this research), CAT-01's actual remaining work is the full-suite run + evidence capture, not new registration.

**Warning signs:** A plan with tasks like "add `ComponentRegistry.Entry` for ProviderKeyCard" when that entry already exists in the file.

**Phase to address:** Phase 13 planning — verify-first, author-only-if-a-gap-is-found.

---

### Pitfall 3: Treating README.md's "seven" wording as a single-line fix

**What goes wrong:** CONTEXT.md's canonical_refs cite "README.md line ~79" (singular). A plan that edits only line 79 will leave 3 OTHER "seven" occurrences in the same file uncorrected, and the Phase 13 success criterion ("the stale 'seven families' wording is corrected ... in the load-bearing files") will not actually be satisfied.

**Why it happens:** CONTEXT.md's line estimate was approximate/illustrative, not an exhaustive grep.

**How to avoid:** Fix ALL of the following (verified this session via `grep -n` on the live files):

| File | Line(s) | Current text |
|------|---------|--------------|
| `CLAUDE.md` | 35 | `**registered** in one of the seven family lists XOR **allowlisted** in` |
| `README.md` | 21 | `**[\`API.md\`](API.md)** — the full public surface, organized as the seven-family composable catalog.` |
| `README.md` | 59-60 | `2. **Call components directly.** They are plain public \`@Composable\` functions grouped into seven\n   families — see **[\`API.md\`](API.md)** for the catalog and the key parameters each one takes.` |
| `README.md` | 73 | `    // …the full seven-family catalog is enumerated in API.md` |
| `README.md` | 79 | `- **Seven component families:** cards, chips, sheets, buttons/FAB, pickers, feedback, empty-state.` |
| `src/main/java/.../explorer/ComponentRegistry.kt` | 88, 92 | `D-05 (Phase 62 Plan 02): the seven per-family lists below are each declared ...` / `... This concatenation ... is the only place the seven lists are combined.` |
| `API.md` | 1, 7-11, 27 | `# API.md — \`yahirandroidtaste\` public surface (the nine-family composable catalog)` / `organized into the library's **nine families** — the same\ntaxonomy the library ships in \`explorer/ComponentRegistry.kt\` (\`cardsFamilyEntries + ... + tactileFoundationFamilyEntries\`)` / `**51 registered public composables** across the nine families, plus **5 intentionally-unregistered**` |

[VERIFIED: all rows above read via `Read`/`grep -n` on the live files this session, 2026-09-30, text quoted verbatim]

Note `README.md:79` ALSO lists an explicit family roster ("cards, chips, sheets, buttons/FAB, pickers, feedback, empty-state") that omits Progress/Metrics, Tactile Foundation, AND Voice Command — three families short, not just a count-word fix. `API.md:8-11`'s concatenation listing is similarly short the `voiceCommandFamilyEntries` term (and already was short `tactileFoundationFamilyEntries`... no, it does include tactileFoundationFamilyEntries, just not voiceCommandFamilyEntries). Both need the full ten-item roster, not a word-swap.

**Warning signs:** `grep -rn "seven\|nine family\|nine-family" CLAUDE.md README.md API.md src/.../ComponentRegistry.kt` returning any hits after the fix is declared done.

**Phase to address:** Phase 13's doc-drift task.

---

### Pitfall 4: Scope-creeping into the separately-stale composable COUNT numbers

**What goes wrong:** While fixing the family-count wording, it's tempting to also fix the equally-stale absolute composable counts (README.md's "41 registered ... 4 intentionally-unregistered ... 45 public composables total"; API.md's "51 registered ... 5 intentionally-unregistered ... 56 public composables total") since they're adjacent text and obviously wrong too — live count is **61 registered + 5 intentionally-unregistered = 66 total** [VERIFIED: `grep -c "ComponentRegistry.Entry(" src/main/java/.../explorer/*FamilyScreen.kt` summed to 61 across all 10 family files, plus 5 entries in `ComponentRegistry.INTENTIONALLY_UNREGISTERED`, 2026-09-30].

**Why it happens:** The counts and the family-name wording sit in the same sentences/tables, so touching one naturally invites touching the other.

**How to avoid:** CONTEXT.md D-03 scopes this phase explicitly to "the stale 'seven families' wording ... to the post-cohort count (ten)" — it does not mention the composable-count numbers, and the ROADMAP/REQUIREMENTS success criteria only name the family-count correction. Treat the composable-count staleness as **out of scope** for Phase 13 unless the planner deliberately widens scope (in which case, flag it as a discretionary addition, not an implicit requirement). Updating the family COUNT word without updating the composable COUNT numbers in the same sentence is acceptable per D-03's literal scope, if slightly awkward prose — fixing the surrounding numbers is a reasonable judgment call but not a gate requirement.

**Phase to address:** Phase 13 doc-drift task — scope decision point for the planner.

---

### Pitfall 5: Re-verifying INV-01 only by reading CONTEXT.md/PITFALLS.md instead of the live diff

**What goes wrong:** INV-01 ("no OkHttp or voice-action-engine dependency ... every composable takes data + actions as parameters") could be marked done purely because Phases 10-12's own PITFALLS.md-driven authoring discipline claimed to avoid it — without a fresh grep at Phase 13 time catching a last-minute accidental import.

**How to avoid:** Phase 13 should still run its own fresh verification (cheap, ~1 command):
```bash
git diff f10b560^..HEAD -- build.gradle.kts   # expect empty
grep -rniE "okhttp|retrofit|voice.?action.?engine|kotlinx\.serialization|com\.google\.gson" \
  src/main/java/io/github/ygaray/yahirandroidtaste/component/{ProviderKeyCard,ModelSelectCard,ApproachLadderCard,OutcomeSheet,ClarificationBar}.kt \
  src/main/java/io/github/ygaray/yahirandroidtaste/model/*.kt
```
[VERIFIED: ran both this session, 2026-09-30 — `build.gradle.kts` diff empty; the grep's only hits are KDoc prose explicitly documenting the ABSENCE of an engine dependency (`ApproachLadderCard.kt:36`, `ApproachRungUiModel.kt:10`, `HandledByUiModel.kt:10-11`), not actual imports]

**Phase to address:** Phase 13's INV-01 verification task — cheap, should run fresh rather than inherited from memory.

## Runtime State Inventory

Not applicable — this is a verification/doc-correction phase with no rename, refactor, or data migration. No stored data, live service config, OS-registered state, secrets, or build artifacts are touched. **Nothing found in any category** — verified by the phase scope itself (CONTEXT.md's domain boundary names only registration-verification, API-check, dependency-audit, and doc-text edits).

## Code Examples

### The authoritative additive-API proof technique (the one new procedure this phase needs)

```bash
# Source: this session's live verification against the actual repo, 2026-09-30.
# Run from the repo root.

# Step 1 — confirm no LOCAL drift (code vs. the currently-committed api.txt):
./gradlew apiCheck
# Expected: BUILD SUCCESSFUL (this only proves api.txt is in sync with HEAD's code,
# NOT that HEAD is compatible with v2.3.0 — see Step 2).

# Step 2 — the TRUE v2.3.0-compatibility verdict:
cp api.txt /tmp/api.txt.current.bak
git show v2.3.0:api.txt > api.txt
./gradlew apiCheck
# Expected: BUILD SUCCESSFUL — Metalava's real semantic compatibility check (not a naive
# text diff) confirms every v2.3.0 symbol is still present with a compatible signature.
cp /tmp/api.txt.current.bak api.txt
git status --short api.txt   # MUST be empty before proceeding — confirms the swap was undone

# Step 3 — corroborate (expect a KNOWN false positive, not a real regression):
API_FILE=api.txt bash tools/classify-hub-change.sh --baseline v2.3.0 --mode additive
# Expected: exit 3, "LANE 3" — caused solely by ClearableTextField's new trailing optional
# params (visualTransformation, revealToggle) triggering the script's line-diff heuristic.
# Confirm via: comm -23 <(git show v2.3.0:api.txt | sort -u) <(sort -u api.txt)
# — expect EXACTLY ONE line, the old 10-param ClearableTextField signature.
```

### Confirming CAT-01's registration state (verification-first, not authoring)

```kotlin
// Source: live read of src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt:94-103
val entries: List<Entry> = cardsFamilyEntries +
    chipsFamilyEntries +
    sheetsFamilyEntries +
    buttonsFabFamilyEntries +
    pickersFamilyEntries +
    feedbackFamilyEntries +
    emptyStateFamilyEntries +
    progressFamilyEntries +
    tactileFoundationFamilyEntries +
    voiceCommandFamilyEntries   // <- already present; this is the tenth family CAT-01 asks for
```

```bash
# Run the full suite — this IS the CAT-01 gate (CATALOG-03 only fails here, not in scoped runs):
./gradlew testDebugUnitTest
# Verified this session: BUILD SUCCESSFUL at current HEAD.
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|---------------|--------|
| Register-at-Phase-13 (cleanup-at-the-end) | Register-as-you-author (each of Phases 10/11/12 added its own `Entry` immediately) | Phases 10-12 (this milestone), per PITFALLS.md Pitfall 4's own prevention advice | Phase 13 inherits a near-already-green CAT-01 instead of a registration backlog |

**Deprecated/outdated:** The phase-description framing ("register every new composable in the new 'Voice Command' family") describes work that is largely already done; the planner should write tasks that VERIFY this rather than re-author it.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | The discrepancy between the Phase 10 commit message's `HUB_LANE_OVERRIDE=2` and this session's reproduced `LANE 3` result for the same symbol was not root-caused (possible explanations: the author declared intent without running the classifier, a hook bypass, or an environment difference at commit time vs. now) | Pitfall 1 | Low — the Metalava-verified additive conclusion does not depend on resolving this; it only affects whether a NEW commit in this range would need `HUB_LANE_OVERRIDE=2` or `=3` if the pre-commit hook runs again |
| A2 | `Entry.tier = PATTERN` is the correct tier for all 5 voice composables (not re-litigated this session — this reflects Phases 10-12's own authored decision, already in the live file) | Architecture Patterns / CAT-01 | Low — `ComponentRegistryTierTest` doesn't check these 5 names specifically, so an incorrect tier wouldn't fail a test, but would be a design-intent inconsistency; low-cost to eyeball-confirm during Phase 13's verification task |

**If this table is empty:** N/A — see above; everything else in this document is `[VERIFIED]` against a live `Read`/`Bash` check performed this session, or `[CITED]` from the project's own prior research docs (ARCHITECTURE.md/PITFALLS.md) which this phase does not need to re-verify since they cover Phases 10-12's authoring decisions, not Phase 13's verification work.

## Open Questions

1. **Why does `classify-hub-change.sh --baseline v2.3.0` report LANE 3 at HEAD when the introducing commit (91a5599) recorded `HUB_LANE_OVERRIDE=2`?**
   - What we know: The `ClearableTextField` api.txt line has been byte-identical since commit `91a5599` (confirmed by diffing the line across every api.txt-touching commit since `v2.3.0`); the baseline tag resolved by `git describe --tags --abbrev=0 --match 'v*'` is `v2.3.0` both then and now (no intervening tag).
   - What's unclear: Whether the lane-2 declaration at commit time reflected an actual tool run that produced a different verdict, or a manual/narrative classification by the author that didn't match what the script would have said.
   - Recommendation: Not a blocker — the Metalava-verified additive conclusion (Pitfall 1, Step 2) is independent of this discrepancy. If a future commit needs to pass the pre-commit hook on this exact range, use `HUB_LANE_OVERRIDE=3` (matching what the script currently reports) with a rationale citing this RESEARCH.md's Metalava verification. No action needed for Phase 13's own tasks.

2. **Should Phase 13 add a regression test asserting the family count is 10?**
   - What we know: No such test exists today; `ComponentRegistryTierTest` only spot-checks 3 named entries' tiers, nothing counts families.
   - What's unclear: Whether this is worth the authoring cost for a one-time doc-drift-correction phase that "cuts no tag."
   - Recommendation: Claude's Discretion (not in CONTEXT.md decisions or REQUIREMENTS.md) — low priority; the 4 doc-drift success criterion is satisfied by text edits alone, and a new test adds maintenance surface for marginal benefit.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Gradle wrapper (`./gradlew`) | All verification tasks | ✓ | project-pinned | — |
| Metalava via `me.tylerbwong.gradle.metalava` plugin | API-01 | ✓ | pinned in `libs.versions.toml` (plugin alias `libs.plugins.metalava`) | — |
| detekt | zero-baseline check | ✓ | pinned via `libs.plugins.detekt` | — |
| JitPack / network | NOT needed by Phase 13 (deferred to Phase 14's ship gate) | — | — | — |

No missing dependencies; this phase runs entirely offline against the local Gradle build.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 (plain, no `@RunWith`) for the 4 drift-guard tests; Robolectric + Compose UI test for the per-composable tests (`ProviderKeyCardTest.kt` etc., already present) |
| Config file | `build.gradle.kts` (Kotlin DSL, `testOptions`/`android {}` block — root-as-module) |
| Quick run command | `./gradlew testDebugUnitTest --tests "*ComponentRegistry*" --tests "*DomainVocabularyDriftGuardTest*" --tests "*GeneratedSymbolDriftGuardTest*"` [VERIFIED: ran this exact command, 2026-09-30, BUILD SUCCESSFUL] |
| Full suite command | `./gradlew testDebugUnitTest` [VERIFIED: ran this exact command, 2026-09-30, BUILD SUCCESSFUL] |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| CAT-01 | Every new public composable registered XOR allowlisted, full suite green | unit (source-text scan) | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*"` | ✅ already exists, already green |
| API-01 | Public API strictly additive vs `v2.3.0` | build-tool (Metalava semantic diff) | swap-baseline technique (Code Examples section) + `./gradlew apiCheck` | ✅ task exists (`apiCheck`); the v2.3.0-baseline swap is a manual procedure, not a persisted script — Phase 13 should capture its output as evidence, optionally as a `tools/` script if the planner wants it repeatable |
| INV-01 | No forbidden dependency; prop/callback only | manual grep + diff | `git diff f10b560^..HEAD -- build.gradle.kts` + grep (Common Pitfalls Pitfall 5) | ✅ commands exist; no persisted test. Claude's Discretion whether to add a durable `GeneratedSymbolDriftGuardTest`-style regression test for "build.gradle.kts gained no new `implementation(...)` since v2.3.0" — not required by REQUIREMENTS.md |

### Sampling Rate
- **Per task commit:** `./gradlew testDebugUnitTest --tests "*ComponentRegistry*"` (fast, scoped) — but remember Pitfall 4 from PITFALLS.md: this does NOT exercise `ComponentRegistryDriftGuardTest`'s full-suite-only failure mode by itself; it is already known to pass, so this is a regression-speed-check, not the gate.
- **Per wave merge / phase gate:** `./gradlew testDebugUnitTest` (full suite, unscoped) + `./gradlew apiCheck` + the v2.3.0-swap technique + `./gradlew detekt` — all four must be green before this phase is declared done, per its own success criteria.

### Wave 0 Gaps
None — existing test infrastructure (`ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `ComponentRegistryTierTest`, `GeneratedSymbolDriftGuardTest`, Metalava `apiCheck`, detekt zero-baseline) already covers every Phase 13 requirement. This phase needs evidence-capture and doc edits, not new test authoring.

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | No | Library renders a masked API-key FIELD only; holds, validates, and transmits nothing (already verified INV-01-adjacent in Phase 10) |
| V3 Session Management | No | No session state in a presentational component library |
| V4 Access Control | No | No access-control surface in this phase's scope |
| V5 Input Validation | No | Phase 13 adds no new input surface — it verifies existing composables, edits static docs |
| V6 Cryptography | No | Library performs no cryptographic operation; `ClearableTextField`'s `visualTransformation`/masking is a UI presentation concern (obscuring on-screen characters), not cryptography |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Secret leakage via a public composable accidentally logging/persisting a key | Information Disclosure | Already mitigated in Phase 10 (`ProviderKeyCard` holds no key, no logging) — Phase 13's INV-01 verification (Pitfall 5) re-confirms no new leak was introduced |
| Supply-chain risk from a new dependency | Tampering | N/A this phase — no new dependency is added (verified); Package Legitimacy Audit section states not-applicable |

This phase introduces no new security-relevant surface; its security-relevant contribution is RE-CONFIRMING (not introducing) the no-network/no-secret-persistence invariants already established in Phases 10-12.

## Sources

### Primary (HIGH confidence — this session's own tool runs and file reads)
- `src/main/java/.../explorer/ComponentRegistry.kt` — read in full; confirmed `voiceCommandFamilyEntries` already in `entries`, `INTENTIONALLY_UNREGISTERED` has 5 entries
- `src/main/java/.../explorer/VoiceCommandFamilyScreen.kt` — read in full; confirmed 5 `Entry(...)` declarations with 4-cell states + `tier = PATTERN`
- `src/main/java/.../explorer/ExplorerIndexScreen.kt` — grepped; confirmed `ExplorerFamilies.VOICE_COMMAND` const + `ORDERED_KEYS` row already present
- `src/test/java/.../explorer/ComponentRegistryDriftGuardTest.kt`, `DomainVocabularyDriftGuardTest.kt`, `ComponentRegistryTierTest.kt`, `GeneratedSymbolDriftGuardTest.kt` — read in full
- `src/main/java/.../model/VoiceOutcomeUiState.kt` — read in full; verbatim sealed-interface shape quoted above
- `tools/verify-api-additive.sh`, `tools/classify-hub-change.sh`, `tools/hooks/pre-commit`, `tools/README-api-guard.md` — read in full
- `CLAUDE.md`, `README.md`, `API.md` — grepped and read for exact "seven"/"nine" line locations and text
- Live tool runs this session (all 2026-09-30): `./gradlew testDebugUnitTest` (full + scoped), `./gradlew apiCheck` (at HEAD and with v2.3.0's `api.txt` swapped in), `./gradlew detekt`, `tools/verify-api-additive.sh v2.3.0`, `tools/classify-hub-change.sh --baseline v2.3.0`, `comm -23` diff of `api.txt` vs. `v2.3.0`'s, `git diff`/`git log`/`grep` across the Phase 10-12 commit range

### Secondary (MEDIUM confidence)
- `.planning/research/ARCHITECTURE.md`, `.planning/research/PITFALLS.md` — milestone-level research from 2026-09-29, covering Phases 10-13's design decisions; this document does not re-verify their Phase 10-12-scoped claims, only builds on their Phase 13-scoped guidance

### Tertiary (LOW confidence)
- None — every claim in this document was either directly verified this session or cited from the project's own prior research artifacts.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new stack; existing tools verified working this session
- Architecture: HIGH — CAT-01's registration state directly read from live source, not inferred
- Pitfalls: HIGH — the central API-01 finding (Pitfall 1) is based on actually running the tools and reproducing both the false-positive and the true-positive verdicts

**Research date:** 2026-09-30
**Valid until:** This research is tied to the exact current HEAD commit (`7c3b5fb`) and the `v2.3.0` tag's `api.txt`. If either moves before Phase 13 executes (e.g., another commit lands on `main`), re-run the verification commands in Code Examples before trusting this document's "already green" claims — the underlying mechanisms (how to run the swap-baseline check, which files have the doc-drift) remain valid for ~30 days regardless.
