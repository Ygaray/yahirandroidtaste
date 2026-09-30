# Reconvene brief — yahirandroidtaste (YAT) — R1

**Milestone:** v2.4 AI-Voice Command UI  ·  **Contract rev read:** A1–A18 / E1–E3 (VAE `ccfebdf`)  ·  **Date:** 2026-09-29
**Slice:** §6.3 (Wave 0)  ·  **Cuts:** library `com.github.Ygaray:yahirandroidtaste:v2.4.0`  ·  **Depends on:** nothing (SB + CT repin to my tag in Wave 1)

## 1. Phases (from ROADMAP)

| Phase | Goal | Contract steps / seams touched | Needs from other repos |
|---|---|---|---|
| 10 — Voice command settings surfaces | Provider/key card, model card, command-approach card (tier-ladder display + offline-only toggle + max-tier cap), all prop-driven | §6.3 settings; renders `TierPolicy`/tier-ladder as props (no execution) | Rough shape of `TierPolicy` (offline-only, max-tier cap) + whether `offlineCapable` is per-rung data |
| 11 — Voice outcome & failure sheet | Domain-neutral outcome sheet: outcome, "handled by: tier/approach" indicator, loud failures, generic undo affordance (Undo-all + per-item + unavailable + refused/partial) | §6.3 outcome sheet; A18 undo; maps engine `outcome` → props | SB + CT undo call-site shapes; the fields of "handled by" (tier label vs tier+approach+trace) |
| 12 — Generic needs-confirmation state | One domain-neutral confirm state (single-or-batch) inside the sheet | §6.3, A2/E1 — renders SB `MutationGate`/`VoiceConfirmGate` risk confirm + CT `VoiceResultSheet` weak-match single/batch | SB + CT real confirm signatures to validate the one prop shape |
| 13 — Catalog integrity & v2.4.0 ship | Register all (new "Voice Command" family), API additive + engine-free, cut `v2.4.0` via §11 | §11 steps 1–4, A12 (waiver), A14 (orchestrator writes ledger), CATALOG-03 | Orchestrator to record + re-check the §11 ledger row |

Phases 10 & 11 are independent (parallelizable); 12 gates on 11; 13 gates on all.

## 2. Public surface this milestone adds or changes (strictly additive vs `v2.3.0`)

- **Composables (names provisional — see §4 naming):** a provider/API-key card, a model card, a command-approach card (ladder + offline-only + cap), an outcome/failure sheet (with handled-by indicator, loud failures, undo affordance), and a needs-confirmation state rendered inside that sheet.
- **New UI models (`model/`, all-`val`):** provider, model, tier-ladder/rung, handled-by, a sealed `VoiceOutcomeUiState` (Success/Failure, + `NeedsConfirmation` added additively in P12), an undo model (per-item status incl. `Unavailable(reason)` + refused/partial), and a confirm model (`reason` + `List<ProposedItemUiModel>` + `SelectionMode`).
- **New tenth `ComponentRegistry` family:** "Voice Command"; possible additive `visualTransformation` param on `ClearableTextField`.
- **Coordinate:** `v2.4.0` (no other tag). No existing symbol removed or changed.

## 3. Assumptions about other repos (each needs a peer confirm/correct)

1. **SB confirm seam** is `MutationGate` (impl `VoiceConfirmGate`), not `MutationTierPolicy` — already fixed by E1. Assumed the needs-confirmation prop shape (`reason` + uniform `List<ProposedItemUiModel>` size-1-or-N + `SelectionMode` + opaque per-item slot) covers SB's risk confirm. **SB to confirm** against the real call-site.
2. **CT confirm seam** is `VoiceResultSheet` Proposed/ProposedBatch — assumed covered by the same one shape (batch = list size > 1, `SelectionMode.PerItem` for "confirm each"). **CT to confirm.**
3. **A18 undo (VUNDO-01):** assumed a per-item `Unavailable(reason)` + top-level `Refused(reason, changedItem)` union covers both consumers' "entangled / changed since" semantics. **SB + CT to confirm** their undo call-site shapes.
4. **"Handled by" fields:** assumed a small `HandledByUiModel` (tier label; approach optional). **VAE to confirm** what the engine's `PipelineTelemetry`/outcome actually emits (tier only vs tier+approach+trace).
5. **Tier-ladder / `TierPolicy` display:** assumed per-rung data includes `offlineCapable` so the card can derive the offline-only ⊗ cap visual. **VAE to confirm** the `TierPolicy` prop shape.

## 4. Contract drift found (code vs contract)

1. **Stray `v2.2` milestone-marker tag (SHIP-02 hazard — confirmed real).** `v2.2` is an **annotated** git tag (tagger Yahir, 2026-09-27), subject *"v2.2 — Line Reunification (GSD milestone marker)"*, pointing at commit `de9e7c0` ("docs: update retrospective for v2.0") — a **different commit** from the release `v2.2.0` (`5310b9a`). It **is pushed to origin (public)**, so it stays (immutable). **Mechanism:** the v2.0 milestone close cut a bare release-line-aligned marker tag; because git tags in this repo ARE JitPack coordinates, `:v2.2` resolves as a buildable coordinate that shadows the real `v2.2.0`. v2.4's `SHIP-02` requirement + Phase 13 gate prevent repeating it (milestone close cuts NO git tag).
2. **Family-count doc drift.** Root `CLAUDE.md` (~line 35), `README.md` (~79), and `ComponentRegistry` KDoc (~88/92) say **"seven"** family lists; the live registry concatenates **nine** (ten with the new "Voice Command" family). `API.md` already says "nine". Scoped as a Phase 13 doc-fix gate (4 load-bearing files).
3. **Naming vs the domain-vocabulary drift guard.** `DomainVocabularyDriftGuardTest` keys off the LEADING PascalCase token; `Voice`, `Command`, `Outcome`, `Provider`, `Approach` are all NOT in `PRIMITIVE_NOUN_ALLOWLIST`. So both the reflexive `Voice*` names AND research's own `CommandOutcomeSheet` fail as-is. **Resolution (ai-auto default):** structural composable names + widen `PRIMITIVE_NOUN_ALLOWLIST` with the new generic leading tokens (not `DOMAIN_VOCABULARY`). Not a contract change — flagged so the reconvene is aware the composable names are still provisional.

## 5. Proposed amendments

None. The §6.3 scope (plus A2/E1/A18) reads clean against the code. The items in §3/§4 are confirmations and an internal naming decision, not contract changes. If any peer's real confirm/undo signature can't be expressed by the one proposed prop shape (§3.1–3.3), that would become an amendment then.

## 6. Risks and open questions for Yahir (via the orchestrator)

- **Naming/allowlist (§4.3):** OK to widen `PRIMITIVE_NOUN_ALLOWLIST` with `Provider`/`Model`/`Approach`/`Outcome`/`Command`/`Undo`-style generic leading tokens, rather than grandfathering full names into `DOMAIN_VOCABULARY`? (The ai-auto default assumes yes.)
- **Cap-control UX:** tap-a-rung on the ladder (default) vs a segmented row vs a slider for the max-tier cap.
- **Cross-repo shapes (§3.1–3.4):** the confirm shape, the undo shape, and the "handled by" fields should be validated against SB's/CT's/VAE's real signatures before P11/P12 authoring — the natural place is this reconvene / plan time.

## 7. Tag / repin intent

- **Tag:** cut `v2.4.0` at the end of Phase 13, on green verification (full suite incl. all 4 drift guards + detekt + Metalava additive + JitPack resolves from a clean cache), then message the orchestrator the full §11 row (A14). Human gate waived (A12, confirmed by Yahir in-session). No milestone-close git tag (SHIP-02).
- **Repin:** SecondBrain (`v2.3.0` → `v2.4.0`) and CalTracker (`v2.1.0` → `v2.4.0`) repin to the ledger tag in Wave 1, their own channels.
