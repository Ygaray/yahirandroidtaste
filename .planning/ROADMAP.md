# Roadmap: yahirandroidtaste — Hub Stewardship

## Milestones

- ✅ **v1.0 — Hub Stewardship** — Phases 1-5 (shipped 2026-09-02, library `v2.0.0`)
- ✅ **v2.0 — Line Reunification** — Phases 6-9 (shipped 2026-09-27, library `v2.2.0`)
- ✅ **v2.4 — AI-Voice Command UI** — Phases 10-14 (shipped 2026-10-01, library `v2.4.0`)
- ✅ **v2.5 — Voice UI Localization & Accessibility** — Phases 15-19 (shipped 2026-10-11, library `v2.5.0`; the G-15-1 long-label fix is pending as `v2.5.1`)

## Phases

_No milestone in progress. Start the next one with `/gsd-new-milestone` (phase numbering continues at 20)._

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

<details>
<summary>✅ v2.4 — AI-Voice Command UI (Phases 10-14) — SHIPPED 2026-10-01, library <code>v2.4.0</code></summary>

Shipped the shared AI-voice UI layer in the hub — generic, prop-driven presentational composables
(`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`, `OutcomeSheet` with a "handled by:
tier/approach" indicator + loud failure states + a generic undo affordance + a domain-neutral
needs-confirmation state, and `ClarificationBar` tap-to-clarify choices) as the tenth "Voice Command"
`ComponentRegistry` family, all strictly additive versus `v2.3.0` and engine-free (no OkHttp, no
`voice-action-engine` dependency — one-way-dependency invariant, INV-01). Cut as library `v2.4.0`.
Wave 0, §6.3 of the frozen `vae-bilingual` cross-repo contract.

- [x] Phase 10: Voice command settings surfaces (2/2 plans) — ProviderKeyCard, ModelSelectCard, ApproachLadderCard — completed 2026-09-30
- [x] Phase 11: Voice outcome & failure sheet (2/2 plans) — OutcomeSheet (handled-by + loud failure + generic undo) + ClarificationBar — completed 2026-09-30
- [x] Phase 12: Generic needs-confirmation state (1/1 plan) — additive NeedsConfirmation sealed arm (single + batch) — completed 2026-09-30
- [x] Phase 13: Catalog integrity & docs (1/1 plan, cuts NO tag) — registry registration + CATALOG-03 drift guard + additive API + doc-drift fix — completed 2026-09-30
- [x] Phase 14: Cut v2.4.0 (1/1 plan) — cut + push + JitPack-confirm `v2.4.0`, §11 ledger row relayed — completed 2026-10-01

**Gate-2:** Phases 10-13 human-signed-off (Yahir, 2026-10-01, PASS via orchestrator); Phase 14 had no
deferred device checkpoint (release-tooling phase). Audit: 16/16 requirements satisfied, cross-phase
integration CLEAN, Nyquist compliant. Closed via override_closeout (phase verifications read
mtime-`stale` after the downstream tag/doc commits — the known mtime artifact; real state verified by
certify `all_pass` + the full suite green at the tagged commit). Full detail:
[`.planning/milestones/v2.4-ROADMAP.md`](milestones/v2.4-ROADMAP.md).

**Deferred (future polish, Gate-2-waived):** two non-blocking `ApproachLadderCard` UI-polish notes
(combined-subdued-label right-edge crowding; light-theme capped-rung dimming) — see
`KNOWN-ISSUES.md` KI-2026-10-01-01. Consumer repins (SecondBrain `v2.3.0`→, CalTracker `v2.1.0`→
`v2.4.0`) are Wave-1, in each consumer's own channel.

</details>

<details>
<summary>✅ v2.5 — Voice UI Localization & Accessibility (Phases 15-19) — SHIPPED 2026-10-11, library <code>v2.5.0</code></summary>

Made the AI-voice UI surface fully caller-localizable and more accessible, strictly additively.
This is Wave 0, §6.3 of the `vae-bilingual` effort, with scope locked to R-v1.1 `cb5f047`.

- [x] Phase 15: Voice-surface i18n label params (4/4 plans) — caller-overridable English-default labels on the settings cards and voice-surface composables (VI18N-01..04). Gate-2 reopened it with G-15-1 (long German labels crammed the rung rows); 15-04 plus the code-review fixes closed it with the private `RungLabelLayout` — completed 2026-10-11
- [x] Phase 16: A11y + Failure enrichment (3/3 plans) — minimum interactive size and selected-radio semantics on rung rows; `Failure` gets an action role, a body slot and a semantics prefix (VA11Y-01, VFAIL-01..03) — completed 2026-10-05
- [x] Phase 17: ApproachLadderCard Router ON/OFF toggle (1/1 plan) — `require()`-paired `router`/`onRouterChange` (VAPPR-04) — completed 2026-10-05
- [x] Phase 18: Catalog integrity + API dump + docs (2/2 plans) — registry and drift guard green, additive API, docs, invariants (CAT-02, API-02, DOC-02, INV-02) — completed 2026-10-05
- [x] Phase 19: Cut v2.5.0 (2/2 plans) — immutable `v2.5.0` tag via §11, ledger relayed, no stray marker tag (SHIP-03) — completed 2026-10-05

**Gate-2:** all five phases are human-signed-off; Phase 15 was re-verified 17/17 on the personal
device on 2026-10-11. Audit: 14/14 requirements, integration WIRED, status `tech_debt`. Full detail:
[`.planning/milestones/v2.5-ROADMAP.md`](milestones/v2.5-ROADMAP.md).

**Follow-up (human-gated):** the G-15-1 fix landed after the immutable `v2.5.0` tag. It ships as
`v2.5.1` (layout-only, `api.txt` unchanged), plus SecondBrain/CalTracker repins, routed through the
orchestrator.

</details>

## Backlog

### Phase 999.1: Formalize reusable Gate-2 visualization harness APK (BACKLOG)

**Goal:** Promote the throwaway same-package-Intent harness — which every Gate-1 agent currently re-derives from scratch — into a committed, launchable Gate-2 visualization app for this library-only repo.

**Requirements:** TBD

**Plans:** 0 plans

Context:

- `yahirandroidtaste` is a pure `com.android.library` (no `applicationId`), so it ships **no installable APK**. Human Gate-2 on-device review therefore has nothing to open. Every Gate-1 self-UAT run rebuilds the same throwaway harness (a 1-Activity app that depends on the mavenLocal AAR and `startActivity(Intent(this, ExplorerActivity::class.java))`) just to see the gallery — documented in `01-05-SELF-UAT.md`'s "Driver-mechanism note" and re-derived by later verify sessions too (incl. v2.4's Gate-2 prep).
- Deliverables to scope when promoted: (a) a committed harness — a dedicated app module or a gradle task that assembles an installable debug APK opening `ExplorerActivity`; (b) a project-local `AGENT-DEVICE-TESTING.md` documenting the same-package-harness driver pattern (the SELF-UAT logs explicitly recommend authoring one so future Gate-1 runs don't re-derive it).
- **Invariant guard:** harness → library only, never the reverse (one-way dependency). The harness is host/consumer-side tooling; it must name no library-internal concepts and must not become something the library depends on.

Plans:

- [ ] TBD (promote with /gsd-review-backlog when ready)

## Progress

| Milestone | Phases | Plans | Status | Shipped |
|-----------|--------|-------|--------|---------|
| v1.0 | 1-5 | — | Complete | 2026-09-02 |
| v2.0 | 6-9 | 7/7 | Complete | 2026-09-27 |
| v2.4 | 10-14 | — | Complete | 2026-10-01 |
| v2.5 | 15-19 | 12/12 | Complete | 2026-10-11 |
