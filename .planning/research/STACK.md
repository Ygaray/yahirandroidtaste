# Stack Research

**Domain:** Stewardship/governance of an existing reusable Jetpack Compose UI library (the "hub")
**Researched:** 2026-08-28
**Confidence:** HIGH (grounded in the repo's own pinned versions, `api.txt`, drift-guard test, governance shell tooling, and the external `repin_status.py` — not invented)

> **Framing.** This is NOT a greenfield stack pick. Every version below is **already pinned** in
> this repo and must not change. The real "stack" question for these five phases is *which
> language features, test patterns, and tooling techniques* implement tier-labeling, audit,
> governance gates, repin bookkeeping, and a coordinated breaking unification **without adding a
> single new dependency** (the one-way-dependency invariant forbids it). The single most
> load-bearing finding is in **Version Compatibility** below: the set-based `verify-api-additive.sh`
> guard treats **any** re-signatured `ComponentRegistry.Entry` constructor line as a lane-3
> removal — so adding `tier`, whether required or defaulted, is unavoidably a curation-lane commit
> plus a Metalava rebaseline. Plan Phase 1 around that, not around "it's just an additive field."

## Recommended Stack

### Core Technologies (all already pinned — cited, not chosen)

| Technology | Version | Purpose | Why (for these phases) |
|------------|---------|---------|------------------------|
| Kotlin | 2.3.20 | Language | `enum class Tier { PRIMITIVE, PATTERN }` is the tier representation; exhaustive `when`, `.name` for gallery display, zero-dependency. Kotlin 2.x built-in-kotlinc under AGP 9 is why classic ABI validators were rejected (see What NOT to Use). |
| AGP (android-library) | 9.2.1 | Build / single-module hub at repo root | Every Gradle command **drops the module prefix** (`./gradlew apiDump`, `./gradlew testDebugUnitTest`, `./gradlew detekt`) — root-as-module (D-01). Do not write `:yahirandroidtaste:` nested paths. |
| Metalava (Gradle plugin) | via `libs.plugins.metalava` | Public-API signature freeze-gate | `./gradlew apiDump` → writes `$rootDir/api.txt`; `./gradlew apiCheck` → compat-checks against it. The **rebaseline mechanism** for Phase 1 (tier field) and Phase 5 (unify break). Task names `apiDump`/`apiCheck` are stable wrappers over `metalavaGenerateSignatureRelease` / `metalavaCheckCompatibilityRelease`. |
| Compose BOM | 2026.02.01 | UI toolkit | Phase 1 gallery tier badge reuses Material3 `Text`/`Badge` already on the classpath; ExplorerIndex/Detail already render `entry.name` + `familyLabelFor(entry.family)` — tier display is one added label, no new dep. |
| Hilt | 2.60.1 (bindings-only) | DI | **Untouched by this milestone.** No `@HiltAndroidApp`/`@AndroidEntryPoint` may be added — reusability invariant. No phase here needs Hilt work. |
| JDK | 17 (toolchain); bytecode target 11 | Toolchain | Cited from CLAUDE.md; `compileOptions` pins source/target `VERSION_11`. No change. |
| minSdk / compileSdk | 35 / 36 (minor API 36.1) | Platform | No change. |

### Supporting Libraries (test + guard substrate — all already present)

| Library | Version | Purpose | When to use in these phases |
|---------|---------|---------|-----------------------------|
| JUnit4 | `libs.junit` | Plain-JVM source-scan tests | GOV-01 tier-aware litmus + GOV-02 domain-noun drift guard — implement as **source-TEXT-scan JUnit tests** in the exact style of `ComponentRegistryDriftGuardTest` (no `@RunWith`, no reflection). |
| Robolectric + compose-ui-test-junit4 | `libs.robolectric`, `libs.androidx.compose.ui.test.junit4` | Compose UI tests under JVM | Only if Phase 1 wants an on-screen assertion that the gallery renders the tier label. Optional — the tier value is guaranteed by the type system; a UI test is belt-and-suspenders. |
| kotlinx-coroutines-test | `libs.kotlinx.coroutines.test` | Coroutine tests | Not needed by any stewardship phase; listed for completeness. |
| Python 3 stdlib (`re`, `json`, `subprocess`, `pathlib`) | external tool | `repin_status.py` reconcile | Phase 4 — the tool lives **outside this repo** (`~/.claude/context/deps/repin_status.py`). This repo contributes **only** a Markdown marker block; no Python is added here. |

### Development Tools (existing governance substrate to extend, not replace)

| Tool | Purpose | Notes for these phases |
|------|---------|------------------------|
| `tools/classify-hub-change.sh` | Lane classifier (1 inert-additive / 2 behavior / 3 API-break) | Has a **`--mode curation`** path that permits a lane-2/3 verdict (exit 0) while still reporting it — this is the sanctioned route for Phase 1's tier field and Phase 5's unify break. |
| `tools/verify-api-additive.sh` | The append-only API signal | **Set-based** (`comm -23` over sorted `api.txt`). Any changed/removed line = lane 3. This is why the `Entry` ctor change trips it (see Version Compatibility). |
| `tools/verify-additive-diff.sh` | The append-only source signal | **Phase 3 (GOV-03) fixes here or in the hook**: it must be scoped to AAR paths (`src/main/`, `api.txt`, `build.gradle.kts`) via a git pathspec so `.planning/`/docs changes are not classified as lane-2. |
| `tools/hooks/pre-commit` | Wires classifier into commits | Baselines against `git describe --tags` (last tag), honors `HUB_LANE_OVERRIDE=<lane>`. GOV-03 candidate fix point: filter the changed-file set before invoking the classifier. |
| detekt (zero-baseline) | Static analysis | Keep green at **zero baseline**. New tests/guards must be idiomatic; do NOT regenerate a baseline to bury a finding. |
| `ComponentRegistryDriftGuardTest` | Source-scan coverage guard | The **reusable machinery** for GOV-02: its `extractPublicTopLevelComposableNames` + `FUN_DECLARATION_REGEX` already extract public composable names reflection-free. Phase 3's name-lint should reuse this pattern, not reinvent it. |

## Installation

```bash
# NOTHING to install. The one-way-dependency invariant forbids new runtime deps, and every
# tool these phases need is already on the classpath / in tools/. The commands you will run:

./gradlew testDebugUnitTest      # drift guard + new GOV-01/GOV-02 litmus tests (module prefix dropped)
./gradlew detekt                 # keep zero-baseline green
./gradlew apiDump                # regenerate api.txt after the Entry tier field (P1) and unify (P5)
./gradlew apiCheck               # verify current surface vs committed api.txt
./gradlew build                  # full build / assembleRelease

# Phase-4 reconcile is an EXTERNAL tool (not in this repo):
python3 ~/.claude/context/deps/repin_status.py reconcile   # succeeds once ECOSYSTEM.md has the markers
```

## Phase-by-Phase Technique Notes

### Phase 1 — Tier Legibility (LEG-01, LEG-02)

- **Representation: `enum class Tier { PRIMITIVE, PATTERN }`** — not a sealed class (no per-tier
  state/behavior to carry), not a `String`/`Boolean` (loses exhaustiveness and self-documentation).
  Precedent: the repo already ships public enums (`HeatTier`, `RelatednessTier`) and treats adding
  `enum_constant` lines as additive. `Tier.name.lowercase()` is the gallery display string — no
  serialization lib.
- **Where it lives: a new `val tier: Tier` on `ComponentRegistry.Entry`.** It must be on `Entry`,
  not a parallel `name → tier` map — the Entry KDoc's own invariant is "`entries` alone is
  authoritative … never a parallel map."
- **Required vs defaulted param — recommendation: REQUIRED (no default).** Rationale: the
  Metalava/lane-3 cost is paid *either way* (see Version Compatibility), so the usual reason to
  default (avoid it) evaporates. A required `tier` makes "every entry carries a tier" (LEG-01) a
  **compile-time invariant** and partially pre-satisfies the deferred GOV-04 ("fail build if a
  composable ships without a tier"). Cost: mechanically edit **53** `ComponentRegistry.Entry(...)`
  call sites (they already use named args, so param position is free — place `tier` right after
  `family`). Call-site counts per family: Cards 11, Sheets 18, Chips 5, Pickers 4, Progress 4,
  Tactile 4, Buttons/FAB 3, Feedback 3, EmptyState 1.
- **Gallery display: extend the existing `ComponentRow`** (`ExplorerIndexScreen.kt`, already renders
  `headlineContent = Text(name)` + `supportingLabel`) and `ComponentDetailScreen` header with a
  small Material3 `Text`/`Badge` reading `entry.tier`. No new component, no new dep.
- **LEG-02 design-intent doc** is a Markdown artifact (e.g. `docs/DESIGN-INTENT.md`) — prose, no
  stack. Keep it distinct from the registry-of-what-exists.

### Phase 2 — Coherence Audit (AUD-01)

- Pure documentation (a Markdown audit). **Optional aid:** a throwaway JVM unit test that prints
  `ComponentRegistry.entries.groupBy { it.family }` with each `it.tier` to seed the author's
  overlap/altitude-mismatch analysis. No stack change, no new dep.

### Phase 3 — Governance Gates (GOV-01, GOV-02, GOV-03)

- **GOV-01 tier-aware litmus:** documented prose + enforced "where feasible" as a JUnit source-scan
  test. If `tier` is a required param (Phase 1), the compiler already enforces *presence*; the test
  enforces the *rule* (primitives get the strict no-domain-vocabulary gate; patterns get the looser
  gate).
- **GOV-02 domain-noun drift guard:** a **plain-JVM JUnit source-scan test** reusing
  `ComponentRegistryDriftGuardTest`'s `extractPublicTopLevelComposableNames` machinery. "Flag, not
  forbid" → implement as an **allowlist-based** test (mirror the `INTENTIONALLY_UNREGISTERED`
  precedent): a new public name containing a domain noun goes RED, and the human either renames or
  adds it to a curated allowlist *with a rationale string*. That is the "surface coupling for human
  review" behavior with teeth, done reflection-free (Compose synthetic `$composer`/`$changed` params
  make reflection fragile — the existing test documents exactly this).
- **GOV-03 pre-commit false-flag:** a **git pathspec** fix in `verify-additive-diff.sh` (or a
  changed-file filter in `tools/hooks/pre-commit`) so the additive guards only inspect AAR paths
  (`src/main/`, `api.txt`, `build.gradle.kts`) and ignore `.planning/`, `docs/`, `tools/`. Bash +
  git only. This also retires the standing need for `HUB_LANE_OVERRIDE=2` on planning-doc commits
  (cross-referenced by the operator's own memory note about the hub additive guard).

### Phase 4 — Repin Bookkeeping Hardening (REPIN-01)

- **Exact contract `repin_status.py` parses** (verified against the tool source):
  - Wrap the consumer matrix in literal HTML-comment markers:
    `<!-- repin-matrix:begin -->` … `<!-- repin-matrix:end -->`.
  - Inside, a GitHub-flavored Markdown table whose **header row contains the word "Consumer"**
    (case-insensitive). Canonical columns: `| Consumer | Pinned | Latest | Status |`.
  - Parser (`parse_ecosystem_matrix`) reads the **first table cell as the consumer name** and the
    **first `v\d+\.\d+\.\d+` match anywhere in the row** as the claimed pin. `reconcile` rewrites the
    whole block between markers via `render_matrix_block` and **raises `ValueError` if the markers
    are absent** — which is exactly the current failure (INC-2026-08-28-03).
  - Consumer-name matching is fuzzy/normalized (casefold, strip non-alphanumerics, prefix match), so
    `SecondBrain` / `CalTracker` match regardless of formatting.
  - Truth pins today: **SecondBrain `v1.10.0`, CalTracker `v1.5.0`** — the seed rows.
- **Repo contribution = the Markdown marker block only.** No Python added here; the tool stays
  external. `python3 ~/.claude/context/deps/repin_status.py reconcile` is the acceptance check.

### Phase 5 — Gardening: Unify & Coordinated Repin (GARD-01, GARD-02)

- **The intentional break.** Removing/renaming duplicate-sibling composables deletes public-API
  lines → a genuine lane-3 break under `verify-api-additive.sh`. Sequence:
  1. Implement the unify dispositions (fold/rename siblings) in `component/`/`feedback/`/etc.
  2. Update `ComponentRegistry` `entries` + `INTENTIONALLY_UNREGISTERED` so the **drift guard stays
     green** (every public composable registered XOR allowlisted).
  3. **Rebaseline Metalava:** `./gradlew apiDump` overwrites `api.txt` with the new (smaller) surface;
     commit that file — the commit *is* the rebaseline. `./gradlew apiCheck` then passes.
  4. Commit through the **curation path**: the set-based guard still diffs against the last *git tag*
     (not the working `api.txt`), so it will report removals until the new tag lands — commit with
     `HUB_LANE_OVERRIDE=3` (or `classify-hub-change.sh --mode curation`).
  5. **Cut a new immutable tag.** Because this is a breaking change, semver argues for **`v2.0.0`**
     (the ecosystem has only ever cut additive `v1.x` minors; a rename/removal is the first true
     break). Flag this as a decision for the owner — the human-gated ritual (`ECOSYSTEM.md` §7 +
     `~/.claude/context/workflows/repin.md`) owns the actual tag/bump.
  6. **Coordinated repin:** both SecondBrain (`v1.10.0` → new) and CalTracker (`v1.5.0` → new) bump
     their `gradle/libs.versions.toml` coordinate, Gradle sync, rebuild, **Gate-1 re-verify each**.
     Human-gated; sequential-in-hub (no consumer worktrees from this project).

## Alternatives Considered

| Recommended | Alternative | When the alternative would win |
|-------------|-------------|--------------------------------|
| `enum class Tier` | Sealed class / interface | If tiers needed per-tier data or behavior. They don't — it's a closed 2-value label. Enum is lighter and gives `.name` for free. |
| `tier: Tier` **required** on `Entry` | `tier: Tier = Tier.PATTERN` **defaulted** | Would win only if it avoided the Metalava/lane-3 hit — but it does **not** (any ctor re-signature trips the set guard). Defaulting merely surrenders the compile-time "every entry is tiered" guarantee for no cost saving. |
| JUnit source-TEXT-scan for GOV-02 | Reflection / ClassGraph name scan | Reflection sees Compose's synthetic `$composer`/`$changed`/`$default` params — fragile. The repo already rejected reflection for exactly this reason (drift-guard KDoc). |
| JUnit source-scan for GOV-02 | A new `tools/*.sh` name-lint | A shell lint is fine too, but the Kotlin test **reuses proven extraction code** and runs inside `./gradlew testDebugUnitTest` with the rest of the guards. Prefer the test; a shell script duplicates the regex. |
| Curation-lane commit + `apiDump` rebaseline (P1, P5) | Tuning `verify-api-additive.sh` to ignore `Entry` | Don't weaken the guard. `--mode curation` / `HUB_LANE_OVERRIDE` is the *designed* escape hatch for deliberate non-additive stewardship. |
| `v2.0.0` for the Phase-5 break | Continue `v1.x` minor bump | Only if the owner explicitly prefers to keep the enum-style "minor for everything" cadence. Semver says a rename/removal is a major. |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| Any new runtime dependency | Violates the one-way-dependency invariant (library → Android SDK / AndroidX-Compose / Hilt / Coil / navigation-compose / reorderable **only**). | Kotlin stdlib `enum`; Material3 already on classpath. |
| A parallel `name → Tier` map | Violates the Entry invariant "`entries` alone is authoritative … never a parallel map"; also drifts. | A `tier` field **on** `Entry`. |
| Kotlin built-in `abiValidation` / `org.jetbrains.kotlinx.binary-compatibility-validator` | Already spiked and **rejected** — neither works on this AGP-9 built-in-kotlinc / Compose stack (see `tools/README-api-guard.md`). | Metalava (`apiDump`/`apiCheck`), already wired. |
| Reflection/ClassGraph for the name-lint | Compose synthetic params make reflected signatures fragile. | Source-TEXT scan (reuse `ComponentRegistryDriftGuardTest`). |
| Regenerating the detekt baseline to clear a new finding | Zero-baseline policy — burying debt. | Fix the finding or tune the rule with justification. |
| Adding `@HiltAndroidApp`/`@AndroidEntryPoint` | Consumer's job; breaks bindings-only invariant. | Nothing — no phase here needs it. |
| `main-SNAPSHOT` / moving branch ref for the Phase-5 repin | Supply-chain integrity — consumers pin immutable tags. | A fresh immutable `vX.Y.Z` tag. |
| Adding Python to this repo for reconcile | The tool is external and stdlib-only. | Contribute only the `repin-matrix` Markdown markers to `ECOSYSTEM.md`. |

## Stack Patterns by Variant

**If Phase 1 makes `tier` a required `Entry` param (recommended):**
- Every one of the 53 `ComponentRegistry.Entry(...)` call sites must add `tier = Tier.X` (named,
  after `family`). One mechanical pass per family screen file.
- Expect a lane-3 pre-commit verdict → land via `HUB_LANE_OVERRIDE=3` (or curation mode) + a
  `./gradlew apiDump` refresh of `api.txt` in the same commit. No consumer is affected (no tag cut
  until Phase 5).

**If the owner prefers minimal call-site churn:**
- Default `tier` and enforce presence with a GOV-01 test instead of the compiler. Still incurs the
  Metalava rebaseline — you trade a compile guarantee for a test guarantee at equal API cost.

**If Phase 3's GOV-03 fix goes in the hook vs. the guard script:**
- Hook filter (`git diff --name-only … -- src/main api.txt build.gradle.kts`) is the least-blast-
  radius fix; editing `verify-additive-diff.sh`'s pathspec fixes it for *all* callers (CI too).
  Prefer the guard-script pathspec so CI and pre-commit agree.

## Version Compatibility

| Concern | Interaction | Notes / Mitigation |
|---------|-------------|--------------------|
| `Entry` ctor change vs `verify-api-additive.sh` | **CRITICAL** | The guard compares `api.txt` as a **set of lines** (`comm -23`). Metalava emits the whole ctor on ONE line: `ctor public …Entry(String name, String family, optional …states, …content, …controls, …preview)`. Adding `tier` — **required OR optional** — rewrites that line, so the baseline line "disappears" → flagged as removed → **lane 3**. There is no additive way to add a constructor param under this guard. Plan Phase 1 as a curation commit + `apiDump`. |
| Guard baseline = last **git tag**, not working `api.txt` | Phases 1 & 5 | `pre-commit` uses `git describe --tags --abbrev=0`. After `apiDump` the working file is fresh, but the guard still diffs vs the last *tag* — so it keeps reporting the change until a new tag lands. Use the override for every intervening commit; the new tag (Phase 5) resets the baseline. |
| Metalava tasks | All apiDump/apiCheck use | `apiDump` → `metalavaGenerateSignatureRelease` (writes `$rootDir/api.txt`); `apiCheck` → `metalavaCheckCompatibilityRelease`. Root-as-module: **no `:module:` prefix**. |
| `Entry` copy()/componentN() | Phase 1 | Adding a property also regenerates `copy(...)` and shifts `componentN()` lines in `api.txt` — all part of the same additive rebaseline; none is a *semantic* consumer break because no consumer constructs `Entry`. |
| Drift guard ↔ Phase 5 removals | Phase 5 | Removing a public composable without updating `entries`/`INTENTIONALLY_UNREGISTERED` fails `ComponentRegistryDriftGuardTest`. Update the registry in the same change. |
| `repin_status.py` ↔ ECOSYSTEM.md | Phase 4 | Tool is Python-3 stdlib; needs the literal `<!-- repin-matrix:begin/end -->` markers around a table with a "Consumer" header. Absent markers → `ValueError` (the current INC-2026-08-28-03 symptom). |
| detekt zero-baseline | Phases 1, 3 | New enum, new tests, hook edits must pass detekt at zero baseline. |

## Sources

- `build.gradle.kts`, `CLAUDE.md` — pinned versions (AGP 9.2.1, Kotlin 2.3.20, Hilt 2.60.1, Compose
  BOM 2026.02.01, JDK 17, minSdk 35 / compileSdk 36.1), Metalava wiring, single-module Gradle
  commands — HIGH (repo-authoritative).
- `api.txt` (lines 390–398) — confirms `ComponentRegistry.Entry` is public API with a single
  all-params ctor line — HIGH.
- `tools/verify-api-additive.sh`, `tools/classify-hub-change.sh`, `tools/hooks/pre-commit` — set-based
  additive semantics, lane mapping, `--mode curation` / `HUB_LANE_OVERRIDE`, tag-baseline behavior —
  HIGH.
- `src/test/.../ComponentRegistryDriftGuardTest.kt` — reflection-free source-scan pattern + name
  extraction reused for GOV-02 — HIGH.
- `src/main/.../explorer/ComponentRegistry.kt`, `CardsFamilyScreen.kt`, `ChipsFamilyScreen.kt`,
  `ExplorerIndexScreen.kt` — `Entry` shape, 53 named-arg call sites (per-family counts), gallery
  render points — HIGH.
- `~/.claude/context/deps/repin_status.py` (lines 110–200) — exact `repin-matrix` marker + table
  contract, `ValueError` on absent markers — HIGH.
- `ECOSYSTEM.md` — current truth pins (SecondBrain v1.10.0, CalTracker v1.5.0), repin ritual §7 — HIGH.

---
*Stack research for: hub-stewardship of an existing Compose UI library (no new deps)*
*Researched: 2026-08-28*
