# Pitfalls Research

**Domain:** Stewardship / governance of a mature, invariant-guarded, multi-consumer reusable Jetpack Compose design-system library (yahirandroidtaste hub)
**Researched:** 2026-08-28
**Confidence:** HIGH (grounded in the live code — `ComponentRegistry.kt`, `api.txt`, `tools/classify-hub-change.sh` + `verify-additive-diff.sh`, the pre-commit hook, `ECOSYSTEM.md` §7 — not generic advice)

> Scope note: these are the mistakes specific to *adding the five stewardship capabilities to THIS system*. Generic Android/Compose pitfalls are deliberately excluded. Every pitfall names the invariant at risk and the owning phase.

---

## Critical Pitfalls

### Pitfall 1: Adding `tier` to `Entry` the wrong way — silent default that defeats legibility, or a non-additive break that panics the guards

**What goes wrong:**
Phase 1 adds a `tier` field to `data class Entry(name, family, states, content, controls, preview)`. Two opposite failures:
- **Too soft:** ship `tier: Tier = Tier.PRIMITIVE` (defaulted). All ~53 call sites keep compiling *without ever stating a tier* → the registry now claims a tier for every component that nobody actually decided. LEG-01 ("explicit tier, queryable") is technically green but semantically empty, and GOV-04 (future build-enforcement) has nothing to enforce.
- **Too hard / wrong placement:** insert `tier` mid-parameter-list. This shifts `data class` `componentN()` destructuring positions (api.txt currently exposes `component3()`… for `states`), reorders the Metalava ctor, and rewrites existing `Entry(...)` lines across the 9 family files.

**Why it happens:**
`Entry` has 6 params and default-valued tails already (added defaults were the Phase-62 compatibility trick per its own KDoc). Reaching for the same "default it so nothing breaks" reflex is natural but here it hollows out the deliverable.

**How to avoid:**
Add `tier` as a **required, no-default** param (forcing all 53 sites to state a tier is the *point* of legibility), placed **last** to keep existing `componentN()` ordering stable. If a default is kept for staged migration, pair it with a **drift-guard test that fails the build if any entry still holds the default** — never ship the default as the resting state. Treat threading `tier` through the 9 family-list files as part of the change, not a follow-up.

**Warning signs:**
`tier` has a default and the audit/gallery shows every component as the same tier; api.txt diff shows `component3()`/copy() reordering rather than a clean append; family files compile untouched.

**Phase to address:** Phase 1 (LEG-01), with the enforcement test seeded for Phase 3 / GOV-04.

---

### Pitfall 2: Treating Phase 1's `Entry` edit as "just a refactor" — forgetting it is a public-API + additive-guard event

**What goes wrong:**
`ComponentRegistry.Entry` is **public API** (it's in `api.txt`: `ctor public ComponentRegistry.Entry(...)`). Adding `tier` changes the Metalava signature *and* rewrites pre-existing lines in `ComponentRegistry.kt` and in every family file's `Entry(...)` call. So Phase 1:
1. trips **Metalava apiCheck** (needs a deliberate `apiDump`), and
2. trips the **additive-guard** (`verify-additive-diff.sh` flags rewritten source lines → the classifier returns **lane 2**, and the pre-commit hook **blocks** without `HUB_LANE_OVERRIDE=2` / `--mode curation`).

Developers surprised by the red guard may blindly override or, worse, conclude Phase 1 "broke the API for consumers."

**Why it happens:**
The change *feels* internal (it's the explorer registry), but the registry type is on the published surface, and the additive guard scans `src/main` line-by-line.

**How to avoid:**
Plan Phase 1 as a **known lane-2 curation commit**: run `classify-hub-change.sh --mode curation` (or the sanctioned `HUB_LANE_OVERRIDE=2`), and `apiDump` the new optional field as an **intentional additive** API change. Confirm the api.txt delta is *only* the `tier` addition (see Pitfall 6). Note: adding an optional public field is additive — it does **not** break existing consumers — but it still requires a new tag before any consumer's gallery shows tiers.

**Warning signs:**
Pre-commit "BLOCKED — lane 2"; apiCheck failure on `Entry` ctor; a reviewer claiming Phase 1 must be a coordinated breaking repin (it isn't — that's Phase 5).

**Phase to address:** Phase 1 (recognize + curate); the guard-noise fix itself is Phase 3 / GOV-03.

---

### Pitfall 3: The tier taxonomy is subjective / unfalsifiable — the whole milestone is built on sand

**What goes wrong:**
If "primitive vs. pattern" is decided by feel, the `tier` field is un-auditable, Phase 2's "altitude mismatch" findings are un-defensible, and Phase 3's tier-aware litmus can't be written as a test. Two reviewers tier the same component differently and there is no tiebreaker.

**Why it happens:**
The two-tier structure is currently *latent tribal knowledge* (PROJECT.md: "still treat it as flat"). Naming it without a decidable litmus just relocates the ambiguity into a label.

**How to avoid:**
Phase 1's design-intent doc (LEG-02) must state the litmus as a **decidable test**, anchored to the existing invariant that already IS falsifiable: the one-way-dependency / no-domain-assumption rule. E.g. *primitive = zero domain nouns in name & params AND renders only caller-passed content; pattern = encodes an opinion/composition/interaction convention*. If a human can't apply it to a component without debate, it isn't done.

**Warning signs:**
The design-intent doc describes tiers in adjectives ("simpler", "more opinionated") with no test; Phase 2 findings say "feels higher-altitude"; the same component gets re-tiered between phases.

**Phase to address:** Phase 1 (LEG-02) — it is the dependency Phase 2 and Phase 3 both consume.

---

### Pitfall 4: Governing against domain vocabulary by importing domain vocabulary — self-inflicted invariant breach

**What goes wrong:**
To build the GOV-02 domain-noun drift guard, the tempting implementation is a **denylist of consumer terms** ("note", "task", "meal", "heat", "brain"…). Hardcoding that list ships SecondBrain/CalTracker domain knowledge *into the hub* — the exact `no domain assumptions` invariant the milestone is supposed to protect. The governance tool becomes the invariant violation.

**Why it happens:**
A denylist is the obvious way to "detect domain nouns," and the reviewer is thinking about *those two* consumers.

**How to avoid:**
Keep the guard **structural/heuristic and flag-not-forbid** (GOV-02 is explicitly "flags, not forbids"): flag names that look proper-noun-ish / non-generic and route them to a human, rather than matching a consumer-term list. Any concrete consumer terms belong in **test fixtures**, never in shipped guard code. Same trap in Phase 1: don't let tier examples in the design-intent doc smuggle in domain assumptions.

**Warning signs:**
The drift-guard source contains words like "meal" or "note"; the hub can now "recognize" a consumer concept; the guard forbids rather than flags.

**Phase to address:** Phase 3 (GOV-02), watch also in Phase 1.

---

### Pitfall 5: Stranding a consumer during the coordinated repin — the milestone's marquee failure

**What goes wrong:**
Phase 5's breaking unify tag must reach **both** consumers (SB `v1.10.0`, CalTracker `v1.5.0` — *different* pins), each **Gate-1 re-verified**, before the milestone closes. Partial completion — repin SB, "do CalTracker later," or bump a coordinate but skip on-device verify, or cut the tag before both consumer branches are staged — leaves a consumer on an unusable pin (its old duplicate is gone from the new tag) or forces it to stay behind while its ecosystem row rots.

**Why it happens:**
Two consumers on two pins on two cadences is genuinely more work, and the hub owner's context is hub-side. **This has already happened here:** ECOSYSTEM.md records SB "stranded at `v1.8.2` through two cycles" because repins landed without running the reconcile. The precedent is real, not hypothetical.

**How to avoid:**
Treat the coordinated repin as **atomic across both consumers within Phase 5**: stage both consumer bumps, don't cut the tag until both are ready, verify both at Gate-1, and use the Phase-4-hardened `repin_status.py reconcile` to *prove* both §1 pins moved. GARD-02 success = *neither* consumer stranded. Do not mark Phase 5 done on one green consumer.

**Warning signs:**
One consumer verified, the other "pending"; an ECOSYSTEM §1 row still on the old pin after tag cut; a consumer build red after bump but the phase marked complete; reconcile output shows a stale pin.

**Phase to address:** Phase 5 (GARD-02), enabled by Phase 4 (reconcile must be proven *before* it's exercised on the real repin).

---

### Pitfall 6: Metalava/API rebaseline masking an unintended break

**What goes wrong:**
Both Phase 1 (add `tier`) and Phase 5 (remove/rename unified duplicates) require regenerating `api.txt`. A blind `apiDump` **silently accepts every** api delta in the tree — including an accidental removal or signature change you didn't intend. The freeze-gate's entire purpose (catching unplanned breaks) is defeated by the very command used to update it.

**Why it happens:**
`apiDump` is a one-shot "make the check pass" reflex, and the diff is large during a breaking gardening phase, so extra changed lines hide in the noise.

**How to avoid:**
Review the `api.txt` diff **line-by-line before committing the rebaseline**. Every removed/changed line must map 1:1 to your intended change set (the single `tier` addition in P1; exactly the unified/renamed components in P5). If a line changed that you didn't touch, stop — it's an accidental break.

**Warning signs:**
api.txt diff has more changed lines than components you touched; unrelated composables' signatures shifted; the diff includes families you weren't unifying.

**Phase to address:** Phase 5 (primary), Phase 1 (the smaller additive rebaseline).

---

### Pitfall 7: Breaking the ComponentRegistry drift guard / integrity `require()`s during unify or tier-threading

**What goes wrong:**
The registry enforces **registered XOR allowlisted, never neither/both**, plus `require()` no-duplicate-names and non-blank unregistered-reasons in its `init` block, plus a source-scanning drift-guard test. Phase 5 removing a duplicate sibling *without* removing its `Entry` (or moving it to `INTENTIONALLY_UNREGISTERED` with a reason) trips these; Phase 1 threading `tier` through the 9 family lists can break the concatenation or a family file's list.

**Why it happens:**
The registry is edited in nine separate family files then concatenated in `ComponentRegistry.kt`; it's easy to change a composable and forget its registry cell, or fold two entries and leave a dangling name.

**How to avoid:**
Treat the registry cell as **part of** the component change, never an afterthought. Run `./gradlew testDebugUnitTest` (drift guard + integrity + `ComponentRegistryDriftGuardTest` source scan) after every unify and after tier-threading. When folding a duplicate, decide explicitly: removed entirely, or demoted to `INTENTIONALLY_UNREGISTERED` with a one-line reason.

**Warning signs:**
Drift-guard test red; `require(duplicateEntryNames.isEmpty())` trips; "present in both entries and INTENTIONALLY_UNREGISTERED"; a public composable with no registry cell.

**Phase to address:** Phase 5 (GARD-01), Phase 1 (LEG-01).

---

### Pitfall 8: The audit produces findings nobody can action

**What goes wrong:**
Phase 2 lists "these feel overlappy / near-duplicate" as prose without a concrete disposition and, for each "unify," a concrete target surface. Phase 5 then has no executable backlog — it must re-do the analysis, or the "unify count" doesn't match the work items, and gardening stalls or scope-drifts.

**Why it happens:**
Audits naturally drift toward observation ("there's overlap here") instead of decision ("fold A into B, new name C, remove A's entry").

**How to avoid:**
Enforce AUD-01 success criterion: **every** finding carries a disposition (unify / keep-with-rationale / prune), and **every "unify" is a concrete (componentA, componentB…) → unified-surface tuple**. Phase 2's "unify" list *is* Phase 5's backlog — they must be countable and 1:1.

**Warning signs:**
Dispositions written as adjectives, not verbs; "unify" items lack a target component/name; Phase 5 can't derive a task list from the audit doc.

**Phase to address:** Phase 2 (AUD-01), consumed by Phase 5.

---

### Pitfall 9: Over-engineering governance for a two-consumer ecosystem

**What goes wrong:**
Phase 3/4 scope creeps toward CI-enforced auto-tiering, an auto-repin engine, elaborate multi-consumer matrices — burning the milestone on tooling that two consumers will never stress, at the cost of the actual audit (P2) and gardening (P5).

**Why it happens:**
Governance work is fun and open-ended; "harden" invites gold-plating. The requirements deliberately **defer** GOV-04 (build-fail on missing tier) and ECO-02 (auto-repin) to v2 for exactly this reason — that line is easy to cross.

**How to avoid:**
Respect the "where feasible" / "flag, not forbid" wording and the v2 boundary. GOV reconcile is a **bookkeeping aid**, not an auto-repin engine. If a governance sub-task costs more than the audit or gardening it serves, cut it to v2.

**Warning signs:**
Time on the drift guard exceeds time on the audit/gardening; Phase 3/4 start implementing GOV-04/ECO-02; a matrix designed for N consumers when N=2.

**Phase to address:** Phase 3 (GOV-01/02) and Phase 4 (REPIN-01).

---

### Pitfall 10: repin-matrix markers become a *second* source of truth that drifts from ECOSYSTEM §1

**What goes wrong:**
ECOSYSTEM §1 already has a **human prose** pin table. Phase 4 adds machine-readable `repin-matrix` markers for `repin_status.py reconcile`. If both are hand-maintained, they diverge — recreating the very `INC-2026-08-28-03` reconciliation-drift class this phase is meant to close (and §1 already shows a stranded/skipped-cycle row where prose lagged reality).

**Why it happens:**
Adding markers next to an existing table looks additive, but now two artifacts encode the same fact and nothing forces them to agree.

**How to avoid:**
Make the markers the **single machine source** the prose is derived from / reconciled against — and have `reconcile` **fail on disagreement** rather than silently trust one. Don't leave two independently hand-edited pin records. Verify against the *actual* consumer manifests (the authoritative pin per ECOSYSTEM's own "manifest wins" rule), not against the prose.

**Warning signs:**
Marker block and §1 prose show different pins; reconcile passes while a row is visibly stale; the markers are updated by hand separately from §1.

**Phase to address:** Phase 4 (REPIN-01).

---

### Pitfall 11: Shipping autonomously — auto-tagging or auto-repinning past the human gate

**What goes wrong:**
An agent "finishing" Phase 5 by running `git tag`, pushing, or editing a consumer's `libs.versions.toml` violates the **human-gated** shipping rule and the **sequential-in-hub / no-consumer-worktrees** convention. Tags are immutable — a premature tag can't be taken back cleanly.

**Why it happens:**
The phase's success criteria mention "new tag" and "both consumers repinned," which reads like agent work; but those steps are explicitly owner-gated.

**How to avoid:**
Do the code + tests + api/registry updates autonomously **in the hub**, then **surface the proposed tag + consumer bumps for the owner's go-ahead**. Never `git tag`/push or touch consumer files from a hub-scoped task without explicit confirmation. Never pin `-SNAPSHOT` or a moving branch.

**Warning signs:**
A `git tag` or consumer coordinate edit appears without owner sign-off; a hub task modifies files under `~/Projects/SecondBrain` or CalTracker; a `-SNAPSHOT` pin.

**Phase to address:** Phase 5 (GARD-02); applies to any phase that "completes" by tagging.

---

### Pitfall 12: Burying a new finding under a regenerated detekt baseline

**What goes wrong:**
New governance scripts/tests (P3), the tier refactor (P1), and unification refactors (P5) can introduce detekt findings. Regenerating the baseline to make detekt green banks debt — explicitly forbidden by the zero-baseline policy.

**Why it happens:**
`detekt --baseline` is the fastest way to green, and this is a large refactor milestone.

**How to avoid:**
Fix the finding, or tune the rule **with written justification** — never regenerate a baseline to hide it. Keep detekt at zero baseline.

**Warning signs:**
detekt red after a phase; a new/changed `detekt-baseline.xml` appears in the diff.

**Phase to address:** All code phases; highest churn in Phase 3 and Phase 5.

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| `tier` defaulted, no enforcement test | 53 call sites compile untouched | Tiers are unowned/empty; LEG-01 hollow; GOV-04 has nothing to enforce | Only as a *migration step* with a fail-on-default test landing same phase |
| Domain-noun guard as a consumer-term denylist | Trivial to implement, "works" | Imports domain assumptions into the hub — breaks the core invariant the guard exists to protect | Never |
| Blind `apiDump` to green the freeze-gate | apiCheck passes instantly | Masks accidental API breaks; defeats the gate | Never — always diff-review the rebaseline |
| Regenerate detekt baseline | detekt green now | Banks debt in a genuinely-clean zero-baseline module | Never (policy) |
| Repin one consumer, defer the other | Phase "looks" done | Stranded consumer on an unusable pin; ecosystem row rots (precedent: SB@v1.8.2) | Never — both or neither |
| Hand-maintain markers *and* §1 prose | Fast to add markers | Two drifting pin records — the incident this phase closes | Never — one source, reconcile-checked |
| Audit findings as prose without dispositions | Faster audit write-up | Phase 5 has no executable backlog; re-analysis | Never — disposition is the deliverable |

## Integration Gotchas (consumer / repin / ecosystem coupling)

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| JitPack tag → consumer | Assume a hub commit is "live"; pin `-SNAPSHOT` or a branch | Immutable semver tag; change is live only after tag → JitPack build → consumer coordinate bump → rebuild → device re-verify |
| Coordinated repin (P5) | Cut the breaking tag before both consumer branches are staged | Stage both consumer bumps first; cut tag; verify both at Gate-1; reconcile proves both pins moved |
| `repin_status.py reconcile` | Trust ECOSYSTEM §1 prose as the pin source | Reconcile against the consumers' actual manifests ("manifest wins"); fail on marker/prose disagreement |
| Metalava freeze-gate | Rebaseline blindly during a breaking phase | Line-by-line diff review; delta must equal the intended change set |
| Additive pre-commit guard | Override every lane-2 block reflexively | Distinguish *intended* curation (P1 tier add, P5 unify → `--mode curation`/override) from an *accidental* break |
| Hub → consumer files | Edit consumer worktrees from a hub task | Sequential-in-hub; commit on `main`; surface repins for the owner, don't perform them |

## "Looks Done But Isn't" Checklist

- [ ] **Tier field (P1):** compiles and the gallery shows a tier — but verify *no entry rests on a default*; every one of the ~53 sites states its tier deliberately.
- [ ] **Design-intent doc (P1):** reads well — but verify the litmus is a **decidable test** a human can apply without debate (not adjectives).
- [ ] **Coherence audit (P2):** enumerates 9 families — but verify **every** finding has a disposition and every "unify" is a concrete (A,B)→C tuple that Phase 5 can execute.
- [ ] **Domain-noun guard (P3):** flags names — but verify it holds **zero consumer vocabulary** in shipped code and **flags, never forbids**.
- [ ] **Pre-commit false-flag fix (P3):** planning/doc commit lands — but verify it landed **without** `HUB_LANE_OVERRIDE`, and that source additivity is *still* guarded (didn't widen the exclusion into src/main).
- [ ] **Reconcile (P4):** runs green — but verify it reflects the **true** pin state of *both* consumers and fails on marker/prose disagreement; INC-2026-08-28-03 actually closed.
- [ ] **Gardening tag (P5):** tag exists — but verify **both** SB and CalTracker repinned **and** Gate-1 device-verified, neither stranded, and the tag cut was owner-confirmed.
- [ ] **Invariants (all):** build green — but verify one-way-dependency, bindings-only-Hilt (no `@HiltAndroidApp`), drift guard, zero-baseline detekt, and Metalava apiCheck **all still hold**.

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Premature/wrong immutable tag cut (P11) | HIGH | Tags are immutable — cannot recut; publish a superseding tag, notify consumers, correct ECOSYSTEM §1; avoid via the human gate |
| Stranded consumer after partial repin (P5) | HIGH | Immediately stage + repin + Gate-1 the lagging consumer to the same tag; run reconcile to confirm; don't close milestone until both green |
| Blind rebaseline masked a break (P6) | MEDIUM | Diff api.txt against the pre-phase tag, identify the unintended delta, restore the surface or re-plan it as intentional |
| Domain term leaked into guard (P4) | MEDIUM | Move terms to test fixtures; re-implement guard structurally; re-verify no-domain-assumption invariant |
| Tier taxonomy unfalsifiable (P3) | MEDIUM | Rewrite litmus as a decidable test in the intent doc; re-tier against it; re-check Phase 2 findings for consistency |
| Registry drift-guard tripped (P7) | LOW | Add/remove/allowlist the registry cell to restore registered-XOR-allowlisted; rerun `testDebugUnitTest` |
| Detekt baseline regenerated (P12) | LOW | Delete the new baseline, fix or justify-tune the finding |

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| 1. `tier` added wrong (soft default / mid-list) | Phase 1 | No entry rests on a default; api.txt shows clean append (tier last) |
| 2. Phase 1 not recognized as API+guard event | Phase 1 (+ Phase 3 for the guard fix) | Curated lane-2 commit; deliberate `apiDump`; delta == tier only |
| 3. Tier taxonomy subjective | Phase 1 (LEG-02) | Litmus is a decidable test; independent reviewers agree |
| 4. Domain vocab imported into governance | Phase 3 (GOV-02), watch Phase 1 | Guard code has zero consumer terms; flags not forbids |
| 5. Consumer stranded in repin | Phase 5 (GARD-02), enabled by Phase 4 | Both consumers repinned + Gate-1 verified; reconcile confirms both pins |
| 6. Rebaseline masks a break | Phase 5 (+ Phase 1) | api.txt diff reviewed line-by-line; 1:1 to intended change set |
| 7. Drift guard / registry integrity broken | Phase 5 (+ Phase 1) | `testDebugUnitTest` green; registered-XOR-allowlisted holds |
| 8. Audit findings un-actionable | Phase 2 (AUD-01) | Every finding dispositioned; unify list is concrete tuples = Phase 5 backlog |
| 9. Over-engineered governance | Phase 3 & 4 | GOV-04/ECO-02 stay v2; reconcile is bookkeeping not auto-repin |
| 10. Marker/prose double source drifts | Phase 4 (REPIN-01) | Single machine source; reconcile fails on disagreement |
| 11. Autonomous tag/repin past the gate | Phase 5 (any tagging phase) | Owner-confirmed tag + repin; no consumer files edited from hub task |
| 12. Detekt baseline burial | All code phases | Zero baseline preserved; no new baseline in diff |

## Sources

- Live code: `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` (Entry type, init `require()`s, INTENTIONALLY_UNREGISTERED, 9 family concatenation) — HIGH
- `api.txt` (Metalava freeze surface: `ComponentRegistry.Entry` ctor/copy/componentN — proves Entry is public API) — HIGH
- `tools/classify-hub-change.sh`, `tools/verify-additive-diff.sh`, `tools/hooks/pre-commit` (lane 1/2/3 classifier, src/main-only scan, `HUB_LANE_OVERRIDE` gate) — HIGH
- `ECOSYSTEM.md` §1 + §7 (repin ritual, the documented SB "stranded at v1.8.2 through two cycles" precedent, immutable-tag rule) — HIGH
- Root `CLAUDE.md` (five invariants, human-gated shipping, sequential-in-hub, tags-immutable) — HIGH
- `.planning/PROJECT.md` / `ROADMAP.md` / `REQUIREMENTS.md` (phase deps, v2 deferrals GOV-04/ECO-02, INC-2026-08-28-03) — HIGH
- Auto-memory: "Hub additive guard blocks planning docs — `HUB_LANE_OVERRIDE=2` sanctioned bypass" — HIGH

---
*Pitfalls research for: yahirandroidtaste hub stewardship (v1.0 milestone)*
*Researched: 2026-08-28*
