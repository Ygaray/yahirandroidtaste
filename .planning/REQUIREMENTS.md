# Requirements: yahirandroidtaste — Milestone v2.5

**Defined:** 2026-10-05
**Core Value:** The hub stays a coherent, legible, prunable design system as more consumers contribute.
**Milestone goal:** Make the AI-voice UI surface fully caller-localizable and more accessible — strictly additively, no behavior change for existing callers.
**Scope source:** `.planning/cross-repo/RECONVENE-BRIEF-R-v1.1.md` (`cb5f047`), vae-bilingual R-v1.1 GO. All requirements verified against `src/main` code.

## Milestone v2.5 Requirements

### Localization (caller-overridable labels)

- [ ] **VI18N-01**: `ProviderKeyCard` exposes an optional `providerLabel: String = "Provider"` and `ModelSelectCard` an optional `modelLabel: String = "Model"`, so the caller can localize the dropdown labels (XR-172-01). Additive; existing callers unchanged.
- [ ] **VI18N-02**: `ClarificationBar` exposes an optional `dismissLabel: String = "Dismiss"` (XR-175-02 a,b).
- [ ] **VI18N-03**: `ApproachLadderCard` exposes optional label params for its rung-state + toggle literals (`unavailableLabel`, `cappedLabel`, `needsNetworkLabel`, `onlineLabel`, `offlineOnlyLabel`), each defaulting to today's English text (XR-175-02 a,b).
- [ ] **VI18N-04**: `OutcomeSheet`'s embedded literals become caller-overridable via additive, defaulted model fields — `HandledByUiModel` ("Escalations:"), the undo models ("Undone", "Couldn't undo:"), and `ProposedItemUiModel` ("Remove" content description) (XR-175-02 a,b). English defaults preserved.

### Accessibility

- [ ] **VA11Y-01**: `ApproachLadderCard` rung rows enforce a minimum interactive size and expose selected semantics for the capped rung (XR-175-02 c). Internal + semantics only — no public API change.

### Failure enrichment

- [ ] **VFAIL-01**: `FailureActionUiModel` gains an optional `role: ActionButtonDefaults.ActionButtonRole = Neutral`, wired to the Failure action button (today hardcoded `Neutral`) (XR-175-02 d).
- [ ] **VFAIL-02**: `VoiceOutcomeUiState.Failure` gains an optional `body: (@Composable () -> Unit)? = null` content slot, rendered inside the error surface (XR-175-02 d).
- [ ] **VFAIL-03**: `VoiceOutcomeUiState.Failure` gains an optional `semanticsPrefix: String? = null` for the failure's accessibility announcement (XR-175-02 d).

### Command-approach card

- [ ] **VAPPR-04**: `ApproachLadderCard` gains a Router ON/OFF toggle via `router: Boolean? = null` + `onRouterChange: ((Boolean) -> Unit)? = null`, `require()`-paired (both null or both non-null) exactly like `offlineOnly`/`onOfflineOnlyChange`; `null` hides the toggle (XR-175-02 e, SB 177 SC3). No per-rung navigation.

### Catalog integrity & additive ship

- [ ] **CAT-02**: Every new/changed public composable stays registered in `ComponentRegistry` (or allowlisted); the CATALOG drift guard is green in the full suite.
- [ ] **API-02**: The public API is strictly additive vs `v2.4.x`; `api.txt` is regenerated and `tools/verify-api-additive.sh` passes.
- [ ] **DOC-02**: `API.md` / `INTEGRATION.md` updated for the new label params, Failure enrichment, and router toggle.
- [ ] **INV-02**: One-way-dependency invariant preserved — no engine/consumer import added (INV-01 holds); detekt zero-baseline green.
- [ ] **SHIP-03**: Immutable tag `v2.5.0` cut via the §11 protocol (verification green, API additive, seams honored, pushed, JitPack builds it), the ledger row relayed to the orchestrator; human-gated (A12 waiver pending Yahir's direct OK), no stray marker tag (`git.create_tag` false).

## Future Requirements

- Two Gate-2-waived `ApproachLadderCard` UI-polish notes (`KNOWN-ISSUES.md` KI-2026-10-01-01) remain future polish unless pulled into scope.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Shipping translated strings / a string catalog in the hub | The hub localizes nothing (INV-01) — it ships English defaults; the consumer passes localized strings |
| Per-rung navigation on `ApproachLadderCard` | SB 177 clarified (e) is a policy-card ON/OFF toggle, not navigation; row-click stays the cap selector |
| Any new engine/consumer dependency | Violates the one-way-dependency invariant |
| Reshaping existing public APIs | Milestone is strictly additive (owner directive, hub guards) |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| VI18N-01 | Phase 1 | Pending |
| VI18N-02 | Phase 1 | Pending |
| VI18N-03 | Phase 1 | Pending |
| VI18N-04 | Phase 1 | Pending |
| VA11Y-01 | Phase 2 | Pending |
| VFAIL-01 | Phase 2 | Pending |
| VFAIL-02 | Phase 2 | Pending |
| VFAIL-03 | Phase 2 | Pending |
| VAPPR-04 | Phase 3 | Pending |
| CAT-02 | Phase 4 | Pending |
| API-02 | Phase 4 | Pending |
| DOC-02 | Phase 4 | Pending |
| INV-02 | Phase 4 | Pending |
| SHIP-03 | Phase 5 | Pending |

**Coverage:**
- v2.5 requirements: 14 total
- Mapped to phases: 14
- Unmapped: 0 ✓

---
*Requirements defined: 2026-10-05 (milestone v2.5, scope locked to RECONVENE-BRIEF-R-v1.1 cb5f047)*
*Last updated: 2026-10-05 after initial definition*
