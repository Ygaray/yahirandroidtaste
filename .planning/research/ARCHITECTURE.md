# Architecture Research

**Domain:** Governance/coherence stewardship of an existing single-module reusable Jetpack Compose UI library (yahirandroidtaste hub)
**Researched:** 2026-08-28
**Confidence:** HIGH (all integration points verified against live source, tests, tooling, and api.txt; two items flagged MEDIUM below)

> This is **integration research**, not greenfield architecture. The system already exists. The job
> of the five stewardship phases is to slot NEW data, docs, and gates into the EXISTING
> registry → drift-guard → gallery → governance-chain → repin substrate **without breaking the four
> load-bearing invariants** (one-way dependency, ComponentRegistry drift guard, zero detekt
> baseline, Metalava `apiCheck`). Everything below maps where each phase touches, what is NEW vs
> MODIFIED, how tier data flows, and a build order that respects both the stated phase dependencies
> and those invariants.

## Standard Architecture

### System Overview — the existing substrate the stewardship work plugs into

```
┌──────────────────────────────────────────────────────────────────────────┐
│  SINGLE-MODULE HUB  (repo root IS :yahirandroidtaste — no module prefix)   │
│                                                                            │
│  ┌────────────────────────  SOURCE OF TRUTH  ───────────────────────────┐ │
│  │ ComponentRegistry.kt                                                  │ │
│  │   object ComponentRegistry {                                          │ │
│  │     data class Entry(name, family, states, content, controls, preview)│ │  ← P1 adds `tier`
│  │     val entries = cardsFamilyEntries + chipsFamilyEntries + ... (×9)   │ │
│  │     INTENTIONALLY_UNREGISTERED: Map<String,String>                     │ │
│  │     init { dup-name / overlap / blank-reason require() }               │ │
│  │   }                                                                    │ │
│  └───────┬──────────────────────────────┬───────────────────────────────┘ │
│          │ 9 per-family lists            │ authoritative name→Entry         │
│          ▼ (each in its own screen file) ▼                                 │
│  ┌────────────────────┐        ┌──────────────────────┐   ┌──────────────┐ │
│  │ *FamilyScreen.kt ×9│        │ ExplorerActivity /   │   │ Drift-Guard  │ │
│  │ xxxFamilyEntries=  │───────▶│ ExplorerEntry NavHost│   │ Test (JVM    │ │
│  │  listOf(Entry(...))│  53    │  index → family →    │   │ source scan) │ │
│  │  (call sites)      │ Entry()│  detail/{name}       │   │ registered   │ │
│  └────────────────────┘        │ IndexScreen /        │   │ XOR allowlist│ │
│          ▲ P1 authors tier     │ ComponentDetailScreen│   └──────┬───────┘ │
│          │ on each of the 53   └──────────┬───────────┘          │ enforces │
│          │                     P1 renders tier badge here        │          │
│  ┌───────┴────────────────────────────────────────────────────────────┐   │
│  │  GOVERNANCE CHAIN  (tools/ + tools/hooks/pre-commit)                 │   │
│  │  pre-commit → classify-hub-change.sh --baseline <latest v-tag>      │   │
│  │      ├─ verify-additive-diff.sh  (DS-05 src/main line-append guard)  │   │  ← P3 fixes false-flag
│  │      └─ verify-api-additive.sh   (api.txt line-append / lane-3 API)  │   │  ← P1 trips this
│  │  lane 1→exit0 · lane 2→block(HUB_LANE_OVERRIDE=2) · lane 3→block(=3) │   │
│  │  --mode curation → lane 2/3 permitted (exit 0) but reported          │   │  ← P5 lands here
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                            │
│  api.txt  (Metalava apiDump freeze surface — INCLUDES ComponentRegistry.Entry ctor) │
└──────────────────────────────────────────────────────────────────────────┘
        │ inert until tag cut + consumer repin (§7 ritual, human-gated)
        ▼
┌───────────────────────── ECOSYSTEM (spokes) ─────────────────────────┐
│ ECOSYSTEM.md §1 prose consumer table (rich, human)                    │  ← P4 adds machine matrix + markers
│   SecondBrain → v1.10.0    CalTracker → v1.5.0                        │
│ repin_status.py reconcile  (CONTROL PLANE, ~/.claude/context/deps/,    │  ← reads hub ECOSYSTEM.md,
│   OUTSIDE this repo)  needs <!-- repin-matrix:begin/end --> markers    │     NOT modified by hub phases
└───────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities (existing — do not re-architect)

| Component | Responsibility | Where it lives |
|-----------|----------------|----------------|
| `ComponentRegistry.Entry` | The single authoritative record per showcaseable composable. **`entries` alone is the name→composable dispatch — no parallel map allowed.** | `explorer/ComponentRegistry.kt` |
| 9 `xxxFamilyEntries` lists | Per-family slices of `entries`, each declared in its own `*FamilyScreen.kt` so family plans edit disjoint files (D-05). 53 `Entry(...)` call sites total. | `explorer/*FamilyScreen.kt` |
| `ComponentRegistryDriftGuardTest` | JVM source-text scan: every public top-level `@Composable` outside `explorer/` must be registered XOR allowlisted. **Does NOT currently check tier.** | `src/test/.../explorer/` |
| Gallery (`ExplorerEntry` NavHost) | `index → family/{family} → detail/{name}`, driven entirely by `entries`. | `explorer/ExplorerEntry.kt`, `ExplorerIndexScreen.kt`, `ComponentDetailScreen.kt` |
| `classify-hub-change.sh` | Combines the two guards into one lane verdict (1/2/3); `--mode curation` permits lane 2/3. | `tools/` |
| `verify-additive-diff.sh` (DS-05) | Proves no pre-existing **source** line rewritten. **Already defaults its path scope to `src/main` only.** | `tools/` |
| `verify-api-additive.sh` | Proves no `api.txt` line removed/renamed (lane-3 = API break). | `tools/` |
| `pre-commit` | Runs the classifier against the latest `v*` tag; blocks lane 2/3 unless `HUB_LANE_OVERRIDE=<lane>`. | `tools/hooks/pre-commit` |
| `repin_status.py reconcile` | Reconciles ECOSYSTEM.md consumer matrix against true pins. **Lives in the control plane, outside this repo.** Requires marker-delimited matrix. | `~/.claude/context/deps/repin_status.py` |

## Recommended Project Structure — where each phase's NEW/MODIFIED artifacts land

```
yahirandroidtaste/                       (repo root = the module, D-01)
├── src/main/java/io/github/ygaray/yahirandroidtaste/
│   └── explorer/
│       ├── ComponentRegistry.kt         # P1 MODIFY: add `tier` to Entry; add Tier enum
│       ├── CardsFamilyScreen.kt …×9      # P1 MODIFY: author tier on all 53 Entry(...) sites
│       │                                 # P5 MODIFY: remove/fold "unify"-dispositioned entries
│       ├── ExplorerIndexScreen.kt        # P1 MODIFY: render tier on the list row
│       └── ComponentDetailScreen.kt      # P1 MODIFY: render tier on the detail header
├── src/test/java/.../explorer/
│   └── ComponentRegistryDriftGuardTest.kt# P3 (optional/GOV-04-adjacent) or P5 MODIFY
│   └── DomainVocabularyDriftGuardTest.kt  # P3 NEW: flag-not-forbid domain-noun name check
├── docs/                                 # NEW dir (none today)
│   ├── DESIGN-INTENT.md                  # P1 NEW (LEG-02): per-tier contract + litmus
│   └── COHERENCE-AUDIT.md                # P2 NEW (AUD-01): 9-family findings + dispositions
├── tools/
│   ├── verify-additive-diff.sh           # P3 MODIFY (if repro confirms residual false-flag)
│   ├── classify-hub-change.sh            # P3 MODIFY (if false-flag lives here)
│   └── hooks/pre-commit                  # P3 MODIFY (if false-flag lives here)
├── api.txt                               # P1 REGEN (Entry ctor gains tier); P5 REBASELINE
├── ECOSYSTEM.md                          # P4 MODIFY: add repin-matrix markers + machine block
│                                         # P5 MODIFY: append new tag's release record
├── API.md                                # P1/P5 MODIFY: doc-parity (tier vocabulary, counts)
└── CONTRIBUTING/litmus                   # P3: tier-aware litmus (in DESIGN-INTENT.md or CONTRIBUTING.md)
```

### Structure Rationale

- **Design-intent doc is a NEW `docs/` file, deliberately distinct from `ComponentRegistry` and `API.md`.** `API.md` and the registry are the *registry-of-what-exists*; LEG-02 wants a *statement-of-intent* (what the hub means to be per tier + the litmus). Keeping them separate is the whole point of LEG-02 — do not fold tier prose into `API.md`.
- **Tier lives ON `Entry`, not in a side map.** The registry KDoc is explicit: "`entries` alone is authoritative … never a parallel map." A `Map<name,Tier>` would violate the single-source-of-truth invariant and drift. This forces the api.txt interaction below — that cost is accepted deliberately.
- **P4's machine matrix is a NEW marker-delimited block, NOT the existing §1 prose table.** See Integration Points.

## Architectural Patterns (the ones this milestone must honor or extend)

### Pattern 1: Single-source-of-truth registry with append-only authoring

**What:** `entries` is the one dispatch; family lists live in family files so edits stay disjoint.
**When to use:** P1's tier authoring — set `tier` inside each family file's `Entry(...)`, not centrally.
**Trade-offs:** Edits touch 9 files + 53 call sites, but stay collision-free across parallel plans.

### Pattern 2: Flag-not-forbid drift guards (surface coupling, don't block)

**What:** The existing CATALOG drift guard *forbids* (fails build). GOV-02's domain-vocabulary guard must *flag* — emit a warning/report a human reviews, never fail the build.
**When to use:** P3's `DomainVocabularyDriftGuardTest` (or a `tools/` script) — a new public composable name containing a domain noun prints a warning; it does not go red.
**Trade-offs:** No hard enforcement (a determined contributor can ignore it), but that matches the requirement (coupling is a judgment call, not a mechanical violation). GOV-04 (v2, out of scope) would harden this into a build-failing tier check.

**Example:**
```kotlin
// P3 NEW: a test that ASSERTS-true-with-a-printed-warning, never fails, on domain-noun match
@Test fun domainVocabularyIsFlaggedNotForbidden() {
    val flagged = scanPublicComposableNames().filter { it.containsDomainNoun() }
    if (flagged.isNotEmpty()) println("DOMAIN-VOCAB FLAG (review): $flagged")
    // no assert/fail — this guard surfaces, it does not gate
}
```

### Pattern 3: Curation lane for deliberate non-additive stewardship

**What:** `classify-hub-change.sh --mode curation` reports lane 2/3 but exits 0; `HUB_LANE_OVERRIDE=<lane>` unblocks a specific pre-commit.
**When to use:** P5's unification (deliberate lane-3 API break) AND — see below — P1's api.txt/source changes, which are *semantically* additive but *mechanically* trip the line-based guards.
**Trade-offs:** The override is per-commit and explicit, preserving the audit trail; the risk is using it to bury an *unintended* break — so each override must be justified in the commit.

## Data Flow

### Tier data flow (Phase 1 — the milestone's spine vocabulary)

```
P1 defines:  enum class Tier { PRIMITIVE, PATTERN }         (new, in ComponentRegistry.kt)
                        │
        add `val tier: Tier` to Entry data class            (MODIFIES public ctor → api.txt)
                        │
   author tier on each of 53 Entry(...) call sites          (MODIFIES 9 family files → DS-05 lane-2)
                        │
        ┌───────────────┼────────────────────────────┐
        ▼               ▼                            ▼
  entries[i].tier   IndexScreen row badge     ComponentDetailScreen header
  (queryable        (LEG-01 "shown in         (LEG-01 "shown in gallery")
   in code)          gallery")
        │
        ▼
  P2 audit reads entries[i].tier to name "altitude mismatch" (tier of a component
  vs the tier its family/siblings imply)  → COHERENCE-AUDIT.md
        │
        ▼
  P2 "unify" dispositions → concrete list → P5 executes removals/folds against family files
```

### Governance-chain data flow (per commit, unchanged mechanism)

```
git commit
   │
pre-commit → BASE = git describe --tags --abbrev=0 --match 'v*'   (= v1.10.0 today)
   │
classify-hub-change.sh --baseline v1.10.0
   ├── verify-additive-diff.sh v1.10.0   → src_rc  (0 = src append-only, 1 = a src line rewritten)
   └── verify-api-additive.sh  v1.10.0   → api_rc  (0 = api append-only, 3 = api line removed)
   │
   lane = 3 if api_rc==3 ;  2 if src_rc!=0 ;  else 1
   │
   pre-commit: lane1→commit ; lane2/3→BLOCK unless HUB_LANE_OVERRIDE==lane
```

### Repin bookkeeping data flow (Phase 4 → Phase 5)

```
P4 adds to ECOSYSTEM.md:  <!-- repin-matrix:begin -->
                          | Consumer | Pinned | Latest | Status |   ← header MUST contain "Consumer"
                          |---|---|---|---|
                          | SecondBrain | v1.10.0 | … | … |
                          | CalTracker  | v1.5.0  | … | … |
                          <!-- repin-matrix:end -->
                          │
repin_status.py reconcile (control plane) reads hub ECOSYSTEM.md
   parse_ecosystem_matrix() → finds region between markers → validates vs true pins
   reconcile_ecosystem()    → OVERWRITES the marker region with its rendered block
                          │
P5 cuts new tag → both consumers repinned → reconcile re-run reflects new pins (no hand edit)
```

## Build Order (respects stated deps + invariants)

Roadmap mandates numeric order `1 → 2 → 3 → 4 → 5`. That order is **safe and correct**; the
dependency graph permits some parallelism but the serial order violates nothing:

| Step | Phase | Hard prereq | Why this position | Invariant interaction |
|------|-------|-------------|-------------------|-----------------------|
| 1 | **P1 Tier Legibility** | none | Defines the tier vocabulary P2 and P3 both consume | **Trips api.txt (Entry ctor) + DS-05 (53 sites).** Land with api.txt regen + `HUB_LANE_OVERRIDE` / `--mode curation`. No tag, no consumer impact. |
| 2 | **P2 Coherence Audit** | P1 (tier vocab) | "altitude mismatch" only nameable once tier exists; produces the "unify" list P5 needs | Doc-only (`docs/COHERENCE-AUDIT.md`) → lane 1 once P3's false-flag fix lands (or override). |
| 3 | **P3 Governance Gates** | P1 (litmus references tiers) | Independent of P2; fixes the false-flag that P2/P4 doc-commits otherwise hit | MODIFIES `tools/`; adds a *flag-not-forbid* test. Must keep detekt zero-baseline + drift guard green. |
| 4 | **P4 Repin Bookkeeping** | none (independent track) | Sequenced before P5 so reconcile tooling is proven before the real coordinated repin | ECOSYSTEM.md-only (non-AAR) → must be lane 1 after P3's fix (this is the motivating case for GOV-03). |
| 5 | **P5 Gardening** | P2 (unify list) + P4 (proven repin) | The ONE deliberate breaking phase | **Deliberate lane-3:** removes/renames composables → drift guard updated, api.txt REBASELINED, `--mode curation`/`HUB_LANE_OVERRIDE=3`, new tag, coordinated repin of BOTH consumers, each Gate-1 re-verified. |

**Recommended sequencing refinement:** run **P3 before P2 and P4** in practice (still ≥ P1). P3's
GOV-03 false-flag fix is what lets the pure-doc commits of P2 (`COHERENCE-AUDIT.md`) and P4
(`ECOSYSTEM.md` markers) land *without* `HUB_LANE_OVERRIDE`. The roadmap's numeric order puts P2
before P3; that still works (P2/P4 just use the override for their doc commits until P3 lands), but
doing P3 first removes that friction. Either is invariant-safe. Flag this as a discuss decision.

## Integration Points

### Per-phase: NEW vs MODIFIED (the downstream handoff)

| Phase | NEW | MODIFIED | Cross-phase handoff |
|-------|-----|----------|---------------------|
| **P1** | `Tier` enum; `docs/DESIGN-INTENT.md` (per-tier contract + litmus) | `Entry` (add `tier`); 9 family files (53 sites); `IndexScreen` + `ComponentDetailScreen` (render badge); `api.txt` (regen); `API.md` (parity) | **→ P2:** `entries[i].tier` queryable + design-intent litmus. **→ P3:** tiers the litmus references. |
| **P2** | `docs/COHERENCE-AUDIT.md` (9 families, findings, dispositions) | none (doc-only) | **→ P5:** the "unify" disposition list = P5's exact work scope. |
| **P3** | `DomainVocabularyDriftGuardTest` (flag-not-forbid); tier-aware litmus prose (in DESIGN-INTENT.md or CONTRIBUTING.md) | `tools/` false-flag fix (verify-additive-diff.sh / classify / pre-commit — locus TBD by repro) | Enables clean doc-commits for P2/P4. |
| **P4** | repin-matrix markers + machine matrix block in ECOSYSTEM.md | ECOSYSTEM.md | **→ P5:** reconcile proven, so the coordinated repin verifies mechanically. |
| **P5** | new immutable tag; new release record in ECOSYSTEM.md | family files (remove/fold unify entries); drift guard (if allowlist shifts); `api.txt` (REBASELINE); both consumers' pins (human-gated, in their repos) | Consumes P2 list + P4 tooling. |

### External / cross-repo boundaries

| Boundary | Communication | Notes / gotchas |
|----------|---------------|-----------------|
| Hub → `repin_status.py` | Tool READS hub `ECOSYSTEM.md` (path `hubs_root/<hub>/ECOSYSTEM.md`) | Tool is in the **control plane**, not this repo. Under sequential-in-hub, P4's in-repo deliverable is the *markers*; running the tool is a verification step, not a repo edit. Do **not** modify the control-plane `.py` from a hub task. |
| Hub → consumers (SecondBrain, CalTracker) | New tag → each consumer bumps its `libs.versions.toml` → resolve → Gate-1 | P5 only. **Human-gated** (§3/§7). Never modify consumer files from a hub task (sequential-in-hub). Never strand a consumer — both repinned or neither. |
| `pre-commit` → tag | `git describe` picks the latest `v*` tag as baseline | After P5 cuts the new tag, subsequent baselines shift to it automatically. |

### The two load-bearing integration hazards (verify before executing)

**HAZARD 1 — P1 is NOT a free additive change (HIGH confidence, verified in `api.txt:390-410`).**
`ComponentRegistry.Entry` is in the public `api.txt`: its `ctor` line (391), `copy(...)` (398), and
`componentN()` lines enumerate every parameter. Adding `tier` to the data class **changes the ctor
and copy lines** → `verify-api-additive.sh` sees those baseline lines as *removed* → **exit 3 =
lane-3 API break**, even though the param is optional/defaulted and source-compatible (the guard is
a pure line-set diff, not a semantic compatibility checker). Additionally the 53 `Entry(...)` call
sites gain a `tier =` argument → `verify-additive-diff.sh` sees rewritten source lines → lane-2.
**Consequence:** P1 must (a) regenerate `api.txt` (`apiDump`) so HEAD's surface is truthful, and
(b) land the commit with `HUB_LANE_OVERRIDE` (=3, or =2 if apiDump keeps the old ctor via Metalava
overload emission — verify) / `classify-hub-change.sh --mode curation`. This is local-only (no tag,
no consumer sees it until P5's tag), so it does **not** count as a shipped breaking change — but the
Phase-1 planner MUST budget for the api.txt regen + governance override. *Assumptions-analyzer
action: confirm whether Metalava emits an overloaded ctor (keeping the old line, → lane-2 only) or
rewrites the single ctor line (→ lane-3) for a defaulted-param addition on a Kotlin data class.*

**HAZARD 2 — GOV-03 false-flag root cause is not yet located (MEDIUM confidence).**
The milestone brief says non-AAR paths (`.planning/`, docs) false-flag as lane-2. **But the current
`verify-additive-diff.sh` already defaults its path scope to `src/main` only** (lines 54-59, a
comment block explicitly added to exclude docs/.planning), and the classifier diffs baseline vs
working tree — so a pure `.planning/` commit should produce `src_rc=0 → lane 1` today. Either the
false-flag was already partially fixed by that scoping and a residual path remains, or the observed
block came from a mixed commit / an earlier script version (the `[[hub-additive-guard-blocks-planning-docs]]`
memory records `HUB_LANE_OVERRIDE=2` as the standing bypass, so it is genuinely observed).
**P3 must REPRODUCE the false-flag first** (commit a `.planning/`-only change with the hook active)
and pin the exact offending step before editing — do not assume the fix locus. *Assumptions-analyzer
action: reproduce, then locate whether the residual comes from the classifier invocation, the
api-additive lane, or the pre-commit itself.*

### P4 marker placement (HIGH confidence, verified in `repin_status.py:110-200`)

`reconcile_ecosystem()` **overwrites everything between `<!-- repin-matrix:begin -->` and
`<!-- repin-matrix:end -->`** with its own rendered `| Consumer | Pinned | Latest | Status |` block.
`parse_ecosystem_matrix()` requires a header row whose cells contain the word "consumer" and rows
carrying a `vX.Y.Z`. **Do NOT wrap the markers around the existing §1 prose table** (header
`| Consumer | Repo | Dev checkout | Pins hub at | Pin file |`) — reconcile would destroy that rich
human table on first run. P4 must add a **separate, dedicated machine-owned matrix block** (its own
4-column table inside the markers), leaving the §1 prose table intact as the human narrative. Seed
it with the true pins (SecondBrain `v1.10.0`, CalTracker `v1.5.0`) so the first `reconcile` reports
"matrix matches truth" with zero hand edits — the REPIN-01 / INC-2026-08-28-03 success condition.

## Anti-Patterns (specific to this milestone)

### Anti-Pattern 1: Storing tier in a parallel map to "avoid the api.txt hit"
**What people do:** Add `val tierByName: Map<String, Tier>` beside `entries` to dodge Hazard 1.
**Why it's wrong:** Violates the explicit single-source-of-truth invariant ("`entries` alone is
authoritative … never a parallel map") and will drift out of sync with `entries`.
**Do this instead:** Put `tier` on `Entry`, regenerate `api.txt`, land under the curation lane.

### Anti-Pattern 2: Wrapping repin markers around the human §1 table
**What people do:** Put `begin/end` markers around the existing consumer table for convenience.
**Why it's wrong:** `reconcile` overwrites the region → the rich prose table is lost on first run.
**Do this instead:** A separate machine-owned 4-column block; keep §1 prose untouched.

### Anti-Pattern 3: Making the domain-vocabulary guard fail the build
**What people do:** Assert-false on a domain-noun name match (mirroring the CATALOG drift guard).
**Why it's wrong:** GOV-02 says **flag, not forbid** — coupling is a review judgment, and hard
enforcement is explicitly deferred to GOV-04 (v2, out of scope).
**Do this instead:** Print a warning / emit a report; never `fail()`.

### Anti-Pattern 4: Bundling P5's breaking removals into an earlier phase's commit
**What people do:** "While I'm in the family file for tier, let me also fold the duplicate."
**Why it's wrong:** P5 is the *only* sanctioned breaking phase, gated by the audit list + the
coordinated repin. A stray removal outside P5 breaks consumers with no repin plan.
**Do this instead:** Keep P1–P4 strictly additive/doc/tooling; concentrate every removal in P5.

## Sources

- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` (Entry, entries, init guards) — HIGH
- `src/main/java/.../explorer/*FamilyScreen.kt` (9 `xxxFamilyEntries`, 53 `Entry(...)` sites) — HIGH
- `src/test/java/.../explorer/ComponentRegistryDriftGuardTest.kt` (registered-XOR-allowlisted, no tier check today) — HIGH
- `api.txt:363-413` (ComponentRegistry.Entry ctor/copy/component in the public surface) — HIGH
- `tools/classify-hub-change.sh`, `verify-additive-diff.sh`, `verify-api-additive.sh`, `hooks/pre-commit` — HIGH
- `~/.claude/context/deps/repin_status.py:110-200` (marker requirement, reconcile overwrite semantics) — HIGH
- `.planning/ROADMAP.md`, `.planning/PROJECT.md`, `.planning/REQUIREMENTS.md`, `ECOSYSTEM.md`, root `CLAUDE.md` — HIGH
- GOV-03 false-flag root cause — MEDIUM (guard already scopes to src/main; needs repro)
- P1 lane-2 vs lane-3 under Metalava defaulted-ctor emission — MEDIUM (needs apiDump confirmation)

---
*Architecture research for: yahirandroidtaste hub stewardship milestone (integration mode)*
*Researched: 2026-08-28*
