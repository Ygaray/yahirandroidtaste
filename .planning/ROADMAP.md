# Roadmap: yahirandroidtaste — Hub Stewardship

## Milestones

- ✅ **v1.0 — Hub Stewardship** — Phases 1-5 (shipped 2026-09-02, library `v2.0.0`)
- ✅ **v2.0 — Line Reunification** — Phases 6-9 (shipped 2026-09-27, cut library `v2.2.0`)
- 🚧 **v2.4 — AI-Voice Command UI** — Phases 10-13 (in progress, cuts library `v2.4.0`)

## Completed Milestones

<details>
<summary>✅ v1.0 — Hub Stewardship (Phases 1-5) — SHIPPED 2026-09-02, library <code>v2.0.0</code></summary>

The hub went from a flat, additive-only catalog to a legible, audited, governed two-tier design
system — Tier Legibility → Coherence Audit → Governance Gates → Repin Bookkeeping → Gardening (unify
shipped; the coordinated consumer repin, GARD-02, was deferred human-gated and was **absorbed into
v2.0's Phase 9** onto `v2.2.0`). Full detail:
[`.planning/milestones/v1.0-ROADMAP.md`](milestones/v1.0-ROADMAP.md).

</details>

<details>
<summary>✅ v2.0 — Line Reunification (Phases 6-9) — SHIPPED 2026-09-27, library <code>v2.2.0</code></summary>

Collapsed the divergent v1.x (SecondBrain `v1.13.0`) and v2.x/`main` (CalTracker `v2.1.0`) release
lines into one forward line on `main`, cut as library `v2.2.0`, so every consumer converges on one
tag and the v1.x line can retire — while keeping every v2.x improvement. Also landed two additive
riders (per-tag chip color, MicButton hardening), completed v1.0's deferred **GARD-02** coordinated
repin (onto `v2.2.0`), and cleared tech-debt **W-1**.

- [x] Phase 6: Forward-port reunification (3/3 plans) — DateTimePicker, PlaceMap* cluster, PresetChip forward-ported net-additively; osmdroid admitted; both drift guards green — completed 2026-09-27
- [x] Phase 7: Chip-color slot (1/1 plan) — opt-in backward-compatible per-tag chip container-color override — completed 2026-09-27
- [x] Phase 8: MicButton hardening (1/1 plan) — consumer-agnostic microcopy, latest-callback safety, hub-vocabulary KDoc — completed 2026-09-27
- [x] Phase 9: Ship & coordinated repin (2/2 plans) — cut library `v2.2.0` (human-gated), reconciled ECOSYSTEM.md matrix, surfaced consumer repins, closed KI-2026-09-02-01 — completed 2026-09-27

**Gate-2:** Phases 6, 7, 8 human-signed-off (Yahir, 2026-09-27); Phase 9 had no deferred device
checkpoint (doc/config ship phase). Audit: 10/10 requirements satisfied, cross-phase integration
CLEAN. Full detail: [`.planning/milestones/v2.0-ROADMAP.md`](milestones/v2.0-ROADMAP.md).

**Deferred (human-gated, cross-repo convention):** consumer repin execution — SecondBrain
(`v1.13.0→v2.2.0` + FilterBar→ChipBar migration) and CalTracker (`v2.1.0→v2.2.0`) — runs in each
consumer's own channel; repin paths surfaced in `ECOSYSTEM.md`.

</details>

## Active Milestone

### 🚧 v2.4 — AI-Voice Command UI (Phases 10-14)

**Milestone Goal:** Ship the shared AI-voice UI layer in the hub — generic, prop-driven
presentational composables that let SecondBrain and CalTracker render voice-command *settings* and
*outcomes* with zero engine or HTTP coupling — and cut it as library `v2.4.0`. This is Wave 0, §6.3
of the frozen `vae-bilingual` cross-repo contract. Every composable takes data + actions as
parameters; the library imports no consumer code, no OkHttp, and no `voice-action-engine` module
(one-way-dependency invariant, INV-01).

**Phase Numbering:**
- Integer phases (10, 11, …): Planned milestone work (continues from v2.0's Phase 9 — no reset)
- Decimal phases (2.1, 2.2): Urgent insertions (marked INSERTED)

- [ ] **Phase 10: Voice command settings surfaces** - Provider/key, model, and command-approach settings cards, all prop-driven
- [ ] **Phase 11: Voice outcome & failure sheet** - Domain-neutral outcome sheet with a "handled by: tier/approach" indicator, loud failure states, a generic undo affordance (Undo all + per-item, with unavailable/refused states), and a tap-to-clarify choices surface
- [ ] **Phase 12: Generic needs-confirmation state** - One domain-neutral confirm prompt covering both SB risk confirm and CT weak-match single/batch confirm
- [ ] **Phase 13: Catalog integrity & docs** - Register all new composables in the new "Voice Command" family, keep the public API strictly additive and engine-free, and fix the seven→ten family-count doc drift (verified — cuts NO tag)
- [ ] **Phase 14: Cut v2.4.0** - Cut the `v2.4.0` tag on green verification, confirm JitPack resolves it, and message the orchestrator the §11 ledger row (the ONLY tag; split from 13 so the cut follows a green Phase 13)

## Phase Details

### Phase 10: Voice command settings surfaces
**Goal**: Consumers can render voice-command provider/model and command-approach settings entirely from props + callbacks — with no key persistence and no network in the library.
**Depends on**: Nothing (first phase of milestone v2.4)
**Requirements**: VSET-01, VSET-02, VAPPR-01, VAPPR-02, VAPPR-03
**Success Criteria** (what must be TRUE):
  1. A provider/API-key settings card renders provider selection and API-key entry from props + callbacks — holding no key and making no network call in the library
  2. A model settings card renders the available/selected model(s) from props and emits the selection via callback
  3. A command-approach settings card displays the configured tier ladder (ordered approaches, e.g. Grammar → SingleShot → Plan → Agentic) from props
  4. The command-approach card's offline-only toggle reflects and emits offline-only state via props + callback
  5. The command-approach card's max-tier cap control reflects and emits the cap via props + callback
**Plans**: 2 plans
**UI hint**: yes

Plans:
- [ ] 10-01-PLAN.md — ProviderKeyCard tracer: masked API-key card + Voice Command family scaffold + additive ClearableTextField masking (VSET-01)
- [ ] 10-02-PLAN.md — ModelSelectCard + ApproachLadderCard expansion: model selection, tier ladder, offline-only toggle, max-tier cap (VSET-02, VAPPR-01/02/03)

### Phase 11: Voice outcome & failure sheet
**Goal**: Consumers can render a domain-neutral command outcome — including which tier/approach handled it, loud visible failure states, a generic undo affordance ("Undo all (N)" + per-item undo with an unavailable state, plus a loud undo-refused/partial state), and a tap-to-clarify choices surface — from props alone.
**Depends on**: Nothing (independent of Phase 10 — a separate composable in the same voice UI package; can proceed in parallel)
**Requirements**: VOUT-01, VOUT-02, VOUT-03, VUNDO-01, VCLAR-01
**Success Criteria** (what must be TRUE):
  1. An outcome/failure sheet renders a command outcome from props with no app-specific nouns
  2. The sheet surfaces a "handled by: tier/approach" indicator identifying which tier/approach handled the command, from props
  3. Failure states render prominently and visibly — loud, not silent or subtle — with an OPTIONAL prop-driven action slot (e.g. "Open Settings", or "Retry" only when the app flags it retry-safe); absent prop → no action rendered
  4. The sheet renders a prop-driven "Undo all (N)" action alongside per-item Undo, and can represent a per-item-undo-unavailable state (an item that cannot be undone alone because it is entangled with another)
  5. An undo-refused / partial-undo state renders loudly with a reason (e.g. "couldn't undo: <reason>, <item> changed since"), domain-neutral
  6. A prop-driven clarification-choices surface renders a question + pressable options (label + opaque id) with onSelect + dismiss — visually informative (not an error); tapping an option resolves the clarification without re-speaking
**Plans**: TBD
**UI hint**: yes

Plans:
- [ ] TBD

### Phase 12: Generic needs-confirmation state
**Goal**: Consumers can render one domain-neutral needs-confirmation prompt that covers both SecondBrain's `MutationGate`/`VoiceConfirmGate` risk confirm and CalTracker's weak-match single/batch confirm — from props, with no library changes per consumer.
**Depends on**: Phase 11 (extends the outcome sheet with a confirmation state)
**Requirements**: VOUT-04
**Success Criteria** (what must be TRUE):
  1. The outcome sheet renders a needs-confirmation state from props: a reason string, proposed item(s), and confirm/cancel actions
  2. The same composable renders both a single proposed item and a batch of proposed items
  3. Confirm and cancel each emit via callback, domain-neutral (no app-specific nouns)
  4. The prop shape satisfies both SB's `MutationGate`/`VoiceConfirmGate` risk confirm and CT's weak-match single/batch confirm without any library-side change
**Plans**: TBD
**UI hint**: yes

Plans:
- [ ] TBD

### Phase 13: Catalog integrity & docs
**Goal**: Every new composable is registered in the new "Voice Command" family, the public API is strictly additive and engine-free, and the seven→ten family-count doc drift is corrected — all verified. This phase cuts NO tag.
**Depends on**: Phases 10, 11, 12
**Requirements**: CAT-01, API-01, INV-01
**Success Criteria** (what must be TRUE):
  1. The full test suite passes — including the CATALOG-03 drift guard — with every new public composable registered in `ComponentRegistry` (or allowlisted in `INTENTIONALLY_UNREGISTERED`) with its full 4-cell states matrix
  2. Metalava `apiCheck` confirms the public API is strictly additive versus `v2.3.0` — no removals or signature changes to existing symbols
  3. The library declares no OkHttp or `voice-action-engine` dependency; every new composable takes data + actions as parameters (one-way-dependency invariant preserved)
  4. The stale "seven families" wording is corrected to ten in the load-bearing files (root `CLAUDE.md`, `README.md`, `ComponentRegistry` KDoc, `API.md`)
**Plans**: TBD

Plans:
- [ ] TBD

### Phase 14: Cut v2.4.0
**Goal**: With Phase 13 green, cut the `v2.4.0` tag, confirm JitPack resolves it, and message the orchestrator the §11 ledger row — the ONLY tag this milestone produces.
**Depends on**: Phase 13
**Requirements**: SHIP-01, SHIP-02
**Success Criteria** (what must be TRUE):
  1. §11 steps 1–4 hold BEFORE the tag is declared cut: full suite green (all 4 drift guards + detekt zero-baseline), Metalava additive vs `v2.3.0`, the tagged commit pushed, and JitPack resolves `v2.4.0` from a clean Gradle cache
  2. The full §11 ledger row (repo, tag, commit, coordinate, contents, evidence path) is messaged to the orchestrator; no peer writes the §11 ledger (A14)
  3. Milestone close cuts NO git tag beyond the `v2.4.0` release coordinate — `git.create_tag` is `false` (guards the stray milestone-marker-tag hazard, INC-2026-09-30-01)
**Plans**: TBD

Plans:
- [ ] TBD

**Rationale for the 13/14 split:** GSD execute-phase runs ALL of a phase's plans before that phase's verification/Gate-1, so bundling the immutable `v2.4.0` cut with catalog/doc work would tag before Phase 13's own verification is green (violating §11 step 1). Isolating the cut in Phase 14 guarantees it follows a fully-verified Phase 13.

## Backlog

### Phase 999.1: Formalize reusable Gate-2 visualization harness APK (BACKLOG)

**Goal:** Promote the throwaway same-package-Intent harness — which every Gate-1 agent currently re-derives from scratch — into a committed, launchable Gate-2 visualization app for this library-only repo.

**Requirements:** TBD

**Plans:** 0 plans

Context:

- `yahirandroidtaste` is a pure `com.android.library` (no `applicationId`), so it ships **no installable APK**. Human Gate-2 on-device review therefore has nothing to open. Every Gate-1 self-UAT run rebuilds the same throwaway harness (a 1-Activity app that depends on the mavenLocal AAR and `startActivity(Intent(this, ExplorerActivity::class.java))`) just to see the gallery — documented in `01-05-SELF-UAT.md`'s "Driver-mechanism note" and re-derived by later verify sessions too.
- Deliverables to scope when promoted: (a) a committed harness — a dedicated app module or a gradle task that assembles an installable debug APK opening `ExplorerActivity`; (b) a project-local `AGENT-DEVICE-TESTING.md` documenting the same-package-harness driver pattern (the SELF-UAT logs explicitly recommend authoring one so future Gate-1 runs don't re-derive it).
- **Invariant guard:** harness → library only, never the reverse (one-way dependency). The harness is host/consumer-side tooling; it must name no library-internal concepts and must not become something the library depends on.

Plans:

- [ ] TBD (promote with /gsd-review-backlog when ready)

## Progress

**Execution Order:**
Phases execute in numeric order: 10 → 11 → 12 → 13 → 14 (Phases 10 and 11 are mutually independent and may run in parallel; Phase 12 gates on 11; Phase 13 gates on 10, 11, 12; Phase 14 (the tag cut) gates on a green Phase 13).

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 10. Voice command settings surfaces | v2.4 | 0/2 | Planned | - |
| 11. Voice outcome & failure sheet | v2.4 | 0/TBD | Not started | - |
| 12. Generic needs-confirmation state | v2.4 | 0/TBD | Not started | - |
| 13. Catalog integrity & docs | v2.4 | 0/TBD | Not started | - |
| 14. Cut v2.4.0 | v2.4 | 0/TBD | Not started | - |
