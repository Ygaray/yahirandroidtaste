# Roadmap: yahirandroidtaste — Hub Stewardship

## Milestones

- ✅ **v1.0 — Hub Stewardship** — Phases 1-5 (shipped 2026-09-02, library `v2.0.0`)
- ✅ **v2.0 — Line Reunification** — Phases 6-9 (shipped 2026-09-27, library `v2.2.0`)
- ✅ **v2.4 — AI-Voice Command UI** — Phases 10-14 (shipped 2026-10-01, library `v2.4.0`)
- 🚧 **v2.5 — Voice UI Localization & Accessibility** — Phases 15-19 (in progress)

## Phases

### 🚧 v2.5 — Voice UI Localization & Accessibility (In Progress)

**Milestone Goal:** Make the AI-voice UI surface fully caller-localizable and more accessible —
strictly additively, with no behavior change for existing callers. Scope locked to
`.planning/cross-repo/RECONVENE-BRIEF-R-v1.1.md` (`cb5f047`), vae-bilingual R-v1.1 GO; Wave 0, §6.3
of the `vae-bilingual` effort (orchestrator `yahir-gsd-control-plane-6e`). Phase numbering continues
from v2.4 (which ended at Phase 14).

- [ ] **Phase 15: Voice-surface i18n label params** - Caller-overridable English-default labels on the settings cards + voice-surface composables (VI18N-01..04)
- [ ] **Phase 16: A11y + Failure enrichment** - Approach-row min interactive size + selected semantics; `Failure` action role + body slot + semantics prefix (VA11Y-01, VFAIL-01..03)
- [ ] **Phase 17: ApproachLadderCard Router ON/OFF toggle** - Additive `router`/`onRouterChange` pair mirroring the `offlineOnly` pattern (VAPPR-04)
- [ ] **Phase 18: Catalog integrity + API dump + docs** - Registry drift guard green, regenerated `api.txt`, docs updated, invariants preserved — cuts NO tag (CAT-02, API-02, DOC-02, INV-02)
- [ ] **Phase 19: Cut v2.5.0** - Immutable `v2.5.0` tag via §11 (human-gated), ledger relayed, no stray marker tag (SHIP-03)

## Phase Details

### Phase 15: Voice-surface i18n label params

**Goal**: Every hardcoded label on the voice-command settings cards and voice-surface composables becomes caller-overridable — via optional defaulted params or additive defaulted model fields — so a consumer can pass localized strings. English defaults preserved; the hub itself localizes nothing (INV-01).
**Depends on**: Phase 14 (milestone v2.4 — library `v2.4.0` baseline)
**Requirements**: VI18N-01, VI18N-02, VI18N-03, VI18N-04
**Success Criteria** (what must be TRUE):

  1. A caller can pass `providerLabel` to `ProviderKeyCard` and `modelLabel` to `ModelSelectCard`, and the dropdown labels render the supplied text; omitting them shows "Provider"/"Model" exactly as today (VI18N-01).
  2. `ClarificationBar` renders a caller-supplied `dismissLabel`, defaulting to "Dismiss" (VI18N-02).
  3. `ApproachLadderCard`'s rung-state + toggle literals (`unavailableLabel`, `cappedLabel`, `needsNetworkLabel`, `onlineLabel`, `offlineOnlyLabel`) render caller-supplied text, each defaulting to today's English (VI18N-03).
  4. `OutcomeSheet`'s embedded literals ("Escalations:", "Undone", "Couldn't undo:", the "Remove" content description) render caller-supplied values via additive defaulted model fields on `HandledByUiModel`, the undo models, and `ProposedItemUiModel` — English defaults preserved (VI18N-04).
  5. Every existing caller that passes none of the new params/fields observes byte-identical English behavior — the change is strictly additive (no param/field removed or reshaped).

**Plans**: 3 plans
**UI hint**: yes

Plans:

- [x] 15-01-PLAN.md — Composable label params: ProviderKeyCard/ModelSelectCard/ClarificationBar/ApproachLadderCard, ProviderKeyCard tracer + apiCheck/apiDump/lane-3 flow (VI18N-01, VI18N-02, VI18N-03)
- [x] 15-02-PLAN.md — Undo-model label fields via `@JvmOverloads` + old-arity `copy` recipe: UndoRowUiModel.undoneLabel tracer + UndoRefusedUiModel prefix/suffix through OutcomeSheet (VI18N-04)
- [x] 15-03-PLAN.md — HandledByUiModel.escalationsLabel + ProposedItemUiModel.removeContentDescription, v2.4.0 call-shape compile fixture, and the phase closing gate vs the released v2.4.1 api.txt (VI18N-04)

### Phase 16: A11y + Failure enrichment

**Goal**: `ApproachLadderCard` rung rows meet the minimum interactive-size and selected-semantics accessibility bar (internal/semantics only), and `VoiceOutcomeUiState.Failure` can carry a caller-chosen action role, an optional body content slot, and an optional accessibility semantics prefix.
**Depends on**: Phase 15
**Requirements**: VA11Y-01, VFAIL-01, VFAIL-02, VFAIL-03
**Success Criteria** (what must be TRUE):

  1. Each `ApproachLadderCard` rung row presents at least the minimum interactive target size and announces selected state to accessibility services (capped rung = selected, RadioButton role) — with no public API change (VA11Y-01).
  2. A caller can set `FailureActionUiModel.role` (default `Neutral`) and the Failure action button renders with that role/severity; omitting it keeps today's hardcoded `Neutral` (VFAIL-01).
  3. A caller can pass `VoiceOutcomeUiState.Failure.body` (an optional `@Composable` slot) and the content renders inside the error surface; a null body renders today's surface unchanged (VFAIL-02).
  4. A caller can pass `VoiceOutcomeUiState.Failure.semanticsPrefix` and the failure's accessibility announcement is prefixed with it; null announces today's text (VFAIL-03).
  5. All additions are strictly additive — existing `Failure` callers are unchanged and no public API is removed or reshaped.

**Plans**: TBD
**UI hint**: yes

### Phase 17: ApproachLadderCard Router ON/OFF toggle

**Goal**: `ApproachLadderCard` can surface a Router ON/OFF policy toggle driven by an additive `router: Boolean? = null` + `onRouterChange: ((Boolean) -> Unit)? = null` pair, `require()`-paired exactly like the existing `offlineOnly`/`onOfflineOnlyChange`. Null hides it. This is a policy-card toggle, NOT per-rung navigation.
**Depends on**: Phase 16
**Requirements**: VAPPR-04
**Success Criteria** (what must be TRUE):

  1. When a caller passes both `router` (non-null Boolean) and `onRouterChange`, the card renders a Router ON/OFF toggle reflecting the boolean state (VAPPR-04).
  2. Toggling the control invokes `onRouterChange` with the new boolean value.
  3. When `router`/`onRouterChange` are both null (the default), no toggle renders and the card is byte-identical to today.
  4. Passing exactly one of the pair fails the `require()`-paired contract (both null or both non-null), exactly like `offlineOnly`/`onOfflineOnlyChange`.
  5. Row-click still selects the tier cap — no per-rung navigation is introduced (no gesture collision).

**Plans**: TBD
**UI hint**: yes

### Phase 18: Catalog integrity + API dump + docs

**Goal**: Prove the milestone's additions are registry-clean, API-additive, documented, and invariant-preserving across the full suite — with NO tag cut (mirrors v2.4's Phase 13→14 split so the cut follows a green catalog).
**Depends on**: Phase 15, Phase 16, Phase 17
**Requirements**: CAT-02, API-02, DOC-02, INV-02
**Success Criteria** (what must be TRUE):

  1. Every new/changed public composable stays registered in `ComponentRegistry` (or allowlisted in `INTENTIONALLY_UNREGISTERED`), and the CATALOG drift guard is green in the full suite (CAT-02).
  2. `api.txt` is regenerated and `tools/verify-api-additive.sh` passes — the public API is strictly additive vs `v2.4.x` (API-02).
  3. `API.md` and `INTEGRATION.md` are updated for the new label params, the Failure enrichment, and the router toggle (DOC-02).
  4. The one-way-dependency invariant holds — no engine/consumer import is added (INV-01 holds) — and detekt is green at zero baseline (INV-02).
  5. No git tag is cut in this phase.

**Plans**: TBD

### Phase 19: Cut v2.5.0

**Goal**: Cut the immutable `v2.5.0` library tag via the §11 protocol after a green Phase 18 — human-gated (A12 waiver pending Yahir's direct OK), with the §11 ledger row relayed to the orchestrator and no stray milestone-marker tag.
**Depends on**: Phase 18
**Requirements**: SHIP-03
**Success Criteria** (what must be TRUE):

  1. `v2.5.0` is cut via the §11 protocol only after Phase 18 verification is green and the API is additive (verification green, seams honored).
  2. The tag is pushed and JitPack builds it — `com.github.Ygaray:yahirandroidtaste:v2.5.0` resolves.
  3. The full §11 ledger row is relayed to the orchestrator (`yahir-gsd-control-plane-6e`).
  4. The cut proceeds only on Yahir's direct OK (A12 tag-cut waiver confirmation) — human-gated.
  5. No stray milestone-marker tag is created (`git.create_tag` false) — the only tag is the `v2.5.0` release coordinate (SHIP-03).

**Plans**: TBD

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

**Execution Order:** Phases execute in numeric order: 15 → 16 → 17 → 18 → 19

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 15. Voice-surface i18n label params | v2.5 | 3/3 | In Progress|  |
| 16. A11y + Failure enrichment | v2.5 | 0/TBD | Not started | - |
| 17. ApproachLadderCard Router ON/OFF toggle | v2.5 | 0/TBD | Not started | - |
| 18. Catalog integrity + API dump + docs | v2.5 | 0/TBD | Not started | - |
| 19. Cut v2.5.0 | v2.5 | 0/TBD | Not started | - |
