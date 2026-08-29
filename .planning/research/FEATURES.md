# Feature Research

**Domain:** Design-system stewardship (governance/gardening of a mature, additive-only reusable Compose UI hub with two consumers)
**Researched:** 2026-08-28
**Confidence:** MEDIUM — the *practices* (interface inventory, component tiering, tiered governance, adoption/version tracking) are cross-corroborated across multiple authoritative design-system sources; their *application to this specific two-consumer code-catalog hub* is opinionated synthesis, deliberately scaled down from the big-org versions those sources describe.

> **Framing.** This is not a feature build. The "features" are the five stewardship **deliverables** the roadmap phases produce. For each, this doc gives the *shape* of a good version (what artifact, what fields/sections), then splits **table stakes** (the checkbox floor) from **differentiators** (what separates a genuinely useful steward's output from a box-ticked one) from **anti-features** (over-engineering that a two-consumer hub must NOT build — several already deferred to v2/Future). The single most important discipline here is **scale**: every mature-org practice below has a heavyweight form (dashboards, CI adoption trackers, multi-tier taxonomies, forbidding lint gates) that is *wrong* at two consumers. The steward's job is the smallest artifact that makes coherence legible and prunable — not the enterprise apparatus.

---

## Deliverable 1 — Tier Legibility (Phase 1 · LEG-01, LEG-02)

**What "good" looks like end-to-end.** The industry pattern is a layered taxonomy — *primitives* (unopinionated, composable, domain-blind) beneath *patterns/recipes* (opinionated compositions that solve a specific problem). Mature systems keep this to **2–3 tiers**; more than that is a documented failure mode. This hub is a *latent two-tier* system, so the deliverable is: (a) attach a machine-readable `tier: primitive | pattern` to every `ComponentRegistry` entry; (b) surface it in the `ExplorerActivity` gallery as a badge on both the list row and the detail header; (c) write a **design-INTENT doc** — distinct from the registry-of-what-exists — that states, per tier, its *contract* (the promise the tier makes to a consumer) and its *litmus* (the decidable test that qualifies a component into that tier).

**Shape of the intent doc** (what sections it needs): (1) one-paragraph purpose per tier; (2) the **contract** per tier — for primitives: domain-blind, no domain vocabulary, renders only caller-supplied content+callbacks; for patterns: opinionated, encodes an interaction convention, may name a UI concept but not a *host-domain* concept; (3) the **litmus** as a single decidable question per tier (e.g. primitive: "could this render unchanged in a finance app, a recipe app, and a note app without renaming?"); (4) **worked examples + borderline cases** drawn from the real 51-composable catalog; (5) a short note on tier **promotion/demotion** (what changes when a component moves tiers).

### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| `tier` enum on every registry entry, queryable in code | LEG-01 core; tests + gallery must read it; no entry left `null`/untiered | LOW | Additive field on the existing entry model; the drift guard already enumerates every entry, so "every entry has a tier" is a natural test assertion. |
| Tier badge visible in the gallery (list + detail) | LEG-01 explicitly wants tiering visible "without reading source" | LOW–MED | Reuse existing Explorer chrome; a small labeled chip, not a redesign. |
| Intent doc states each tier's **contract** | LEG-02; a tier label with no stated contract is decoration | LOW | Prose doc; the contract is the reusability invariant restated per-tier. |
| Intent doc states each tier's **litmus** | LEG-02; without a test, tiering is unfalsifiable and drifts | LOW | Phrase as a decidable question, not a vibe. |
| Intent doc is **separate from the registry/API catalog** | Charter is explicit: intent (what it means to be) ≠ inventory (what exists) | LOW | New file (e.g. `DESIGN-INTENT.md`); do not bolt onto `API.md`. |

### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Worked examples + an explicit **borderline-cases** section | Turns the litmus from aspiration into a usable decision tool; pre-answers the arguments Phase 2/3 will have | LOW | e.g. "`ProgressRing`/`HeatSwatch` = primitive; `VoiceCard`/`TagPickerSheet` = pattern; `EmptyState` = borderline, ruled primitive because…". |
| Litmus phrased as **one decidable question per tier** | Makes the Phase 3 tier-aware gate and the Phase 2 altitude-mismatch findings mechanical, not subjective | LOW | The single highest-leverage sentence in the milestone — everything downstream references it. |
| A promotion/demotion note (tier is a lifecycle, not a birthmark) | Anticipates that today's pattern can distil into tomorrow's primitive (exactly how the Progress/Tactile families arrived) | LOW | 2–3 sentences; signals the tiering is a living contract. |

### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| A **third/fourth tier** (tokens, recipes, organisms…) | "Real design systems have more layers" | Over-tiering is a named failure mode; there's exactly one contribution each way — evidence supports two tiers, not five | Ship two tiers; note in intent doc that more can be split out *if evidence appears*. |
| **Build-failing** tier enforcement now | "Make it airtight" | Explicitly deferred to **GOV-04 / v2**; premature hard gate blocks the additive channel and creates busywork before the taxonomy has settled | Assert "every entry has a tier" as a soft test this phase; defer fail-the-build-on-missing-tier. |
| Splitting tiers into **separate Gradle modules/packages** | "Enforce the boundary physically" | Breaks the single-module D-01 hub invariant and forces a coordinated repin for zero consumer benefit | Tier is metadata on the registry entry; packaging stays flat. |
| Re-taxonomising the **9 families** by tier | "Reorganise while we're here" | The 9 families are the drift-guard SSOT and the `API.md` contract; churning them is a breaking change with no consumer ask | Tier is an *orthogonal* axis layered over the existing families, not a replacement for them. |

---

## Deliverable 2 — Coherence Audit (Phase 2 · AUD-01)

**What "good" looks like.** The canonical method is Brad Frost's **interface inventory** / interface audit: round up every UI element, categorise, and *quantify the duplication* ("37 button styles") so the incoherence becomes undeniable and measurable before anything changes. The hub gets this almost for free — the `ComponentRegistry` **is** the inventory (no screenshot-and-cut exercise needed). So the deliverable is a written audit that walks all 9 families and, for each, flags three finding types the roadmap names: **overlap** (two components doing substantially the same job), **near-duplicate siblings** (e.g. `…Sheet`/`…SheetContent` host/body pairs, `TagPickerSheet` vs `TagChipEditorContent`, the several tag-editing surfaces), and **altitude/tier mismatch** (a component sitting in the wrong tier per Phase 1's litmus). Every finding gets a **disposition**.

**Shape of a finding record** (the load-bearing artifact): `{ id, family, type: overlap|near-duplicate-sibling|altitude-mismatch, components: [names], evidence: what makes them overlap/mismatch, disposition: unify|keep-with-rationale|prune, rationale, [if unify] target unified shape + consumer call-sites affected }`. The **consumer-call-site** field is what makes the audit feed Phase 5 cleanly: it pre-computes the blast radius of each unify.

### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| All **9 families enumerated** in one written audit | AUD-01 success criterion #1 | LOW | The registry gives the enumeration; the work is the judgment per family. |
| Every finding flags type (overlap / sibling / altitude) | AUD-01 wants all three surfaced, not just "duplicates" | MED | Altitude-mismatch findings *depend on Phase 1* tiers existing — that's the phase-ordering reason. |
| Every finding has a **disposition** from a fixed set | unify / keep-with-rationale / prune — a finding with no verdict is just a complaint | LOW | Three-value enum; forces a decision per finding. |
| The **unify list** is concrete + actionable | It is literally Phase 5's scope-of-work | MED | Each unify must name the survivor component + what folds into it. |

### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **Quantify** the duplication (counts, sibling pairs) | Makes the problem measurable, per the interface-inventory method; gives a "before" number to show gardening moved | LOW | e.g. "18 sheet composables, of which N are host/body pairs and M are near-duplicate tag editors." |
| **`keep-with-rationale` treated as a first-class outcome** | The honest majority verdict; prevents the audit from becoming a demolition mandate. The host/body split, for instance, is *intentional* (documented in `API.md`) and should be kept-with-rationale, not unified | LOW | Signals a real steward, not a change-for-its-own-sake pass. |
| Record **consumer call-sites** per unify finding | Pre-computes Phase 5 blast radius and stranded-consumer risk; ties directly to the Phase 4 repin matrix | MED | Requires peeking at how SB/CalTracker call the duplicates — read-only, no consumer edits. |
| Explicitly mark **clean families** ("no finding") | Distinguishes "audited, coherent" from "not looked at"; proves coverage | LOW | A one-line "clean" per family is a positive result, not a gap. |

### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Screenshot/visual interface inventory | "That's how Brad Frost does it" | That method exists to *build* an inventory where none exists; this hub already has the registry as SSOT — screenshots add effort, not signal | Audit against the registry + `API.md`; the inventory already exists. |
| **Severity scoring / weighted matrices** | "Prioritise rigorously" | Two-consumer, ~51-component scope — a scoring rubric is ceremony that outweighs the decisions it informs | Three-value disposition is the whole priority model needed. |
| **Unifying during the audit** | "Fix it while we see it" | Collapses the diagnose/execute separation the roadmap deliberately draws (P2 diagnoses, P5 executes under the coordinated-repin gate) | Audit *only* dispositions; all breaking work waits for Phase 5's gated repin. |
| Auditing **consumer** code / proposing new components | "Be thorough" | Out of scope — consumers own authorship via their give-legs; the hub audits *itself* | Read consumer call-sites for blast-radius only; propose no new components. |

---

## Deliverable 3 — Governance Gates (Phase 3 · GOV-01, GOV-02, GOV-03)

**What "good" looks like.** Mature governance is **tiered by decision type**: brand/foundation-level changes get the strictest review; team/local patterns get flexibility. That maps exactly onto a **tier-aware contribution litmus** — primitives get the strict *no-domain-vocabulary* gate, patterns get the *opinion-allowed* gate. The second half is a **domain-vocabulary drift guard** that **flags, not forbids**: given a *new* public component name, it warns when a net-new domain noun appears, surfacing coupling for a human to judge. The distinction "flag not forbid" is the whole design — the existing corpus already contains domain-ish names (`VoiceCard`, `AlbumCard`, `TagChip…`), so a *forbidding* gate would fail the build on day one. The guard must (a) run against **net-new** names, (b) carry an **allowlist** of already-blessed nouns so it's low-noise, (c) emit an advisory line, never a non-zero exit. GOV-03 is a pure bug-fix: the additive-guard pre-commit hook mis-classifies `.planning/`/docs as lane-2 (AAR) changes, forcing `HUB_LANE_OVERRIDE`; fix the path classification.

**Shape of the litmus doc + guard.** Litmus doc: a short table keyed by tier → the gate that tier must pass, with the Phase-1 litmus question referenced verbatim. Guard: a script/test that (1) reads each entry's tier from the registry, (2) for primitives, checks the name/signature against a domain-noun wordlist, (3) prints `FLAG: new component 'X' introduces domain noun 'voice' — confirm this is a pattern, not a primitive`, (4) exits 0. Enforced "where feasible" means wired into the *existing* drift-guard test / pre-commit lane, not a new CI pipeline.

### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Tier-aware litmus **documented** (strict primitives / loose patterns) | GOV-01; the governance half CalTracker's DS-03 never built | LOW | A doc table; references Phase-1 litmus. Depends on Phase 1 tiers. |
| Drift guard **flags, never forbids** | GOV-02 is explicit; forbidding breaks the existing corpus + additive channel | MED | Advisory output + exit 0; the "flag not forbid" contract is the acceptance test. |
| Guard keyed to **net-new** names + an allowlist | Otherwise it screams on every existing `Voice/Album/Tag` name | MED | Allowlist file of blessed nouns; guard diffs against it. |
| **GOV-03 false-flag fixed** | `.planning/`/doc commits must land without `HUB_LANE_OVERRIDE` | LOW | Path-classification bug in the pre-commit hook; narrow fix + a regression test. |
| Litmus **enforced where feasible** | GOV-01 wants tooling, not just prose | MED | Reuse the drift-guard test harness; don't stand up new infra. |

### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Guard **suggests the resolution** in its flag ("→ likely a pattern; confirm tier") | Turns a warning into a decision prompt; matches "surface coupling for review" intent | LOW | One well-worded advisory line beats a silent non-zero exit. |
| **Allowlist as the audit trail** of accepted domain nouns | Every blessed noun is a recorded governance decision; the list itself documents the hub's domain-coupling budget | LOW | Low-noise + self-documenting; grows only by deliberate human add. |
| Litmus wired into the **same** drift-guard/Metalava lane | One gate to reason about, not a parallel governance pipeline | MED | Respects zero-baseline detekt + existing test discipline. |

### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| **Build-failing** on domain nouns (forbid) | "Enforce purity" | Directly contradicts GOV-02 ("flag not forbid") and would red-build the existing catalog; the deferred **GOV-04/v2** is the *soft* tier-presence check, not this | Advisory flag, exit 0, human judges. |
| NLP / large dictionary / ML name analysis | "Catch every domain leak" | Massive overkill for a curated ~51-name surface; a small wordlist + allowlist is sufficient and inspectable | Hand-curated noun wordlist. |
| Enforcing the litmus on **consumer** PRs / give-legs | "Govern the whole ecosystem" | Consumers own authorship; the hub gates what lands *in the hub*, not consumer repos | Gate hub contributions only; consumers self-govern. |
| Heavy approval workflow / CODEOWNERS board / RFC process | "That's real governance" | Enterprise ceremony for a solo-steward, two-consumer hub; pure friction | The litmus + flag + human-gated repin ritual *is* the governance. |

---

## Deliverable 4 — Repin Bookkeeping Hardening (Phase 4 · REPIN-01)

**What "good" looks like.** Big systems track adoption with automated cross-repo scanners (Radius Tracker, dependency-freshness dashboards) so they know "which teams still run the old version" before shipping a break. At **two consumers**, that entire apparatus collapses to one artifact: a **repin matrix** embedded in `ECOSYSTEM.md` between machine-readable sentinel markers, that `repin_status.py reconcile` can read/rewrite **without hand edits** (closing `INC-2026-08-28-03`). The matrix is simultaneously human-readable (a Markdown table a person scans) and machine-parseable (delimited block a script owns).

**Shape of the matrix.** Rows = consumers (SecondBrain, CalTracker); columns = `currently-pinned tag | latest hub tag | drift status (current | behind)`. The `reconcile` op reads the true pin state and rewrites the block idempotently. The **drift status** column is the load-bearing bit for Phase 5: it's the pre-flight check that nobody will be stranded by the gardening tag.

### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| `repin-matrix` sentinel markers present in `ECOSYSTEM.md` | REPIN-01 success criterion #1; `reconcile` requires them | LOW | Delimited comment block the script targets. |
| `reconcile` runs with **no hand edits** | The whole point — reconciliation becomes a tooling op | MED | Script writes the block; humans never hand-edit it. |
| Output reflects **true** pin state for both consumers | `INC-2026-08-28-03` closed only if the numbers are real | LOW–MED | Source of truth for each consumer's current pin must be defined (recorded value or read). |

### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| A **drift/behind flag** per consumer | Surfaces stranded-consumer risk *before* Phase 5 cuts the tag | LOW | The single most useful column for the gardening gate. |
| **Idempotent** reconcile (re-run = no diff) | Trustworthy tooling; safe to run in CI or pre-repin | LOW | Standard for generated blocks; makes it a reliable check. |
| Human-readable table **and** machine block in one | Anyone scanning `ECOSYSTEM.md` sees ecosystem state at a glance | LOW | Dual-purpose artifact; no separate dashboard needed. |

### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| **Auto-repin** (script opens PRs against consumers) | "Automate the whole repin" | Explicitly deferred to **ECO-02 / v2**; also crosses the sequential-in-hub line (no consumer edits from hub tasks) and drops the human gate | Matrix *reports*; humans perform the gated repin. |
| Cross-repo adoption **scanner / dashboard / service** | "Measure adoption like the big systems" | Radius-Tracker-class tooling is built for dozens of repos; two consumers need a text table | One Markdown matrix + one reconcile script. |
| Forcing SB/CalTracker onto a **shared pin** | "Keep everyone in lockstep" | Out of scope — consumers repin on their own cadence; gardening *coordinates*, it doesn't mandate | Matrix tolerates divergent pins; only flags *stranded*, not *different*. |

---

## Deliverable 5 — Gardening: Unify & Coordinated Repin (Phase 5 · GARD-01, GARD-02)

**What "good" looks like.** This is the one deliberately **breaking** phase — the thing the additive-only consumer channel structurally cannot do. Mature systems handle breaking design-system changes by (a) batching breaks, (b) shipping a **migration map** (old→new API) so downstream migration is mechanical, and (c) knowing exactly who consumes the old surface before cutting. Here: implement every **unify** disposition from Phase 2 (remove/rename/fold duplicate siblings into one component), keep the registry drift guard green, **rebaseline Metalava `apiCheck`** for the intentional break, cut **one** new immutable tag, then run the human-gated coordinated repin of **both** consumers, each Gate-1 re-verified, nobody stranded.

**Shape of the migration artifact.** A changelog/migration note mapping each removed/renamed symbol → its survivor, per unified component. This is what makes the two consumer repins mechanical rather than archaeological, and it's the record that proves "no consumer stranded."

### Table Stakes

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Every Phase-2 **unify** disposition implemented | GARD-01 core; scope = the audit's unify list, no more | HIGH | Removes/renames/folds public composables — genuinely breaking. |
| Registry drift guard **+ Metalava `apiCheck` pass** (rebaselined) | GARD-01 criterion; the break is intentional, so `apiCheck` is re-based on purpose | MED | Rebaseline is the sanctioned move for a deliberate break — not baseline-burying a regression. |
| **One** new immutable tag cut | GARD-02; tags are immutable, never a moving ref | LOW | Human-gated tag-cut per `ECOSYSTEM.md` §7. |
| **Both** consumers repinned + Gate-1 re-verified, none stranded | GARD-02; the coordinated-repin ritual is the deliverable's proof | HIGH | Human-gated; uses the Phase-4 matrix to confirm no strand. |

### Differentiators

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| A per-component **old→new migration map** | Makes both consumer repins mechanical; is the stranded-nobody evidence | LOW–MED | Ship in the changelog/tag notes; cite the survivor per removed symbol. |
| **All breaks batched into a single tag** | One coordinated repin event, not repeated consumer pain | MED | Consolidating breaking work is the core reason this milestone exists. |
| Repin verified against the **Phase-4 matrix** | Ties the hardening track to the breaking track; pre-flight strand check | LOW | The matrix's drift flag is the go/no-go. |

### Anti-Features

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Pruning **beyond** the audit's unify list | "Clean up more while breaking anyway" | Scope creep; `keep-with-rationale` items were deliberately kept; every extra removal widens the repin blast radius | Execute exactly the unify list; nothing the audit didn't disposition. |
| **Multiple** gardening tags / drip-feed breaks | "Ship incrementally" | Each break = a full coordinated repin of both consumers; multiplies the one expensive ritual | Batch into a single tag + single coordinated repin. |
| Silent removal with **no migration map** | "Consumers will figure it out" | Strands consumers / turns repin into archaeology; violates the no-strand guarantee | Migration map is mandatory, not optional. |
| Editing consumer files **from hub tasks** | "Just repin them here" | Violates sequential-in-hub + the human-gated repin invariant | Hub cuts the tag + surfaces the bump; consumer owners perform + Gate-1 verify. |

---

## Feature Dependencies

```
Phase 1: Tier Legibility (tiers + intent doc + litmus)
    ├──enables──> Phase 2: Coherence Audit
    │                 (an "altitude/tier mismatch" is only a nameable
    │                  finding once tiers exist)
    │                     └──feeds (unify list)──> Phase 5: Gardening
    │
    └──enables──> Phase 3: Governance Gates
                      (the tier-aware litmus references the Phase-1 tiers
                       + reuses the Phase-1 litmus question verbatim)

Phase 4: Repin Bookkeeping Hardening   (independent hardening track)
    └──verifies (no-strand pre-flight)──> Phase 5: Gardening

Phase 3 ⟂ Phase 4   (independent; parallelizable with each other and
                     sequenceable ahead of Phase 5)
```

### Dependency Notes

- **Phase 2 requires Phase 1:** "altitude/tier mismatch" is undefinable without an explicit tier per component. The tier vocabulary is precisely what upgrades a vague "these feel redundant" into a documented finding type.
- **Phase 3 requires Phase 1:** the strict-primitives / loose-patterns split *is* the tiers; the drift-guard flag decides "primitive or pattern?" using the Phase-1 litmus. Building the gate before the tiers exist would hard-code a taxonomy that isn't legible yet.
- **Phase 5 requires Phase 2 (scope) and Phase 4 (verification):** the audit's unify list is Phase 5's entire work order; the repin matrix's drift flag is Phase 5's no-strand pre-flight. Phase 4 is deliberately sequenced *before* Phase 5 so the reconcile tooling is proven on the current pins before it's trusted on the real coordinated break.
- **Phase 3 ⟂ Phase 4:** governance hardening and repin bookkeeping share nothing; both can run in parallel with — or ahead of — the breaking Phase 5.

---

## MVP Definition

For a defined stewardship milestone, the "MVP" is the **checkbox floor** of each deliverable; the differentiators are what make the milestone worth doing rather than performative. Everything below "Launch With" is already deferred by the milestone's own v2/Future list — do not pull it forward.

### Launch With (this milestone) — the table-stakes floor

- [ ] Every registry entry tiered + tier badge in gallery + intent doc with per-tier contract & litmus — LEG-01/02
- [ ] Written audit of all 9 families, every finding dispositioned, concrete unify list — AUD-01
- [ ] Tier-aware litmus documented + flag-not-forbid drift guard + pre-commit false-flag fixed — GOV-01/02/03
- [ ] `repin-matrix` markers + `reconcile` runs hand-edit-free reflecting true pins — REPIN-01
- [ ] Unify list implemented, drift guard + Metalava green (rebaselined), one tag, both consumers repinned + Gate-1 verified — GARD-01/02

### The good-vs-checkbox delta (do these too — they are the point)

- [ ] Litmus as one decidable question + borderline-cases section (Phase 1)
- [ ] Quantified duplication + `keep-with-rationale` as first-class + consumer call-sites per unify (Phase 2)
- [ ] Guard suggests the resolution + allowlist as the domain-coupling audit trail (Phase 3)
- [ ] Drift/behind flag per consumer + idempotent reconcile (Phase 4)
- [ ] Per-component old→new migration map + single batched tag (Phase 5)

### Explicitly Deferred (v2+ — DO NOT build this milestone)

- [ ] **GOV-04:** build-failing tier-presence enforcement in the drift-guard test — soft check only now
- [ ] **ECO-02:** auto-repin tooling across consumers — the matrix *reports*, humans repin
- [ ] Third/fourth taxonomy tier, per-tier module split, cross-repo adoption scanner/dashboard, severity-scoring rubrics, forbidding name gates, RFC/approval boards — all enterprise-scale apparatus that two consumers do not warrant

---

## Feature Prioritization Matrix

| Deliverable | Steward Value | Implementation Cost | Priority |
|-------------|---------------|---------------------|----------|
| Tier Legibility (P1) | HIGH (unblocks P2 + P3) | LOW–MED | P1 |
| Coherence Audit (P2) | HIGH (defines the gardening scope) | MED | P1 |
| Governance Gates (P3) | MEDIUM (prevents future drift) | MED | P2 |
| Repin Bookkeeping (P4) | MEDIUM (de-risks P5; closes an incident) | LOW–MED | P2 |
| Gardening (P5) | HIGH (the one thing the consumer channel can't do) | HIGH | P1 |

**Priority key:** P1 = the spine (Legibility → Audit → Gardening); P2 = independent hardening tracks (Governance, Repin) that de-risk the spine and can run in parallel.

**Cost note:** Phases 1–4 are documentation/tooling — low physical risk, the "cost" is judgment quality. Phase 5 is the only phase with real breaking-change and multi-repo-coordination cost; the value of Phases 1–4 is largely in making Phase 5 mechanical and safe.

---

## Competitor Feature Analysis

Rather than product competitors, these are the mature-org reference practices each deliverable scales *down* from:

| Practice | Big-org form | This hub's right-sized form |
|----------|--------------|-----------------------------|
| Component tiering | 3+ tiers: tokens → primitives → components → patterns → recipes | Two tiers: primitive \| pattern (evidence-backed, not aspirational) |
| Coherence audit | Interface inventory: screenshot, cut, count "37 buttons" | Audit against the registry SSOT (inventory already exists); dispose per finding |
| Contribution governance | RFCs, working-group review, CODEOWNERS, tiered approval boards | A documented tier-aware litmus + a flag-not-forbid guard + the human-gated repin ritual |
| Adoption/version tracking | Radius Tracker / dashboards / dependency-freshness metrics across dozens of repos | One `repin-matrix` table in `ECOSYSTEM.md` + a reconcile script |
| Breaking-change rollout | Deprecation windows, codemods, multi-quarter migration campaigns | One batched tag + a migration map + a coordinated two-consumer repin, each Gate-1 verified |

---

## Sources

- Component tiers (primitives / components / patterns / recipes) and the "2–3 tiers, more is a failure mode" guidance — Brad Frost, "The art of design system recipes"; Rad UI, "The Three-Layer Rule"; UXPin; NN/G, "Design Systems 101" — MEDIUM (cross-corroborated)
- Interface inventory / interface audit method (quantify variations, deduplicate) — Brad Frost, "Conducting an Interface Inventory"; 18F Methods, "Interface audit"; Marcin Treder, "Design Systems Sprint 1" — MEDIUM (cross-corroborated, canonical)
- Tiered governance by decision type (strict foundation review vs. flexible team patterns), contribution criteria, naming/lint automation — UXPin, "Design System Contribution Model"; Miro, "Design System Governance"; designsystems.com, "Keeping contributions in check" — MEDIUM (cross-corroborated)
- Version/adoption tracking, dependency-freshness, breaking-change impact ("which teams still use old versions") — Netguru, "Design System Metrics"; Rangle Radius Tracker (GitHub); zeroheight adoption guides — MEDIUM (establishes that the automated form is built for many-repo scale, i.e. the anti-feature here)
- Two-consumer-scale right-sizing and the mapping onto this hub's specific phases/invariants — author synthesis grounded in `.planning/PROJECT.md`, `ROADMAP.md`, `REQUIREMENTS.md`, `API.md` — LOW→MEDIUM (opinionated)

---
*Feature research for: design-system stewardship deliverables (two-consumer reusable Compose UI hub)*
*Researched: 2026-08-28*
