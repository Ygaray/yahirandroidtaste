---
phase: "12"
slug: "generic-needs-confirmation-state"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
# audited_head = git HEAD sha at audit time — freshness stamp. child-result re-checks it: if
# implementation (outside .planning) changed since this sha, the audit is stale (INC-2026-08-06-04).
audited_head: 26cc481915d5343de536648fe2551458cff7f085
created: "2026-09-30"
---

# Phase 12 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Consumer app -> Library (`NeedsConfirmation` props) | All content (`reason`/`title`/`items`/labels/`reversibilityHint`) and callbacks (`onConfirm`/`onCancel`/`onRemove`) cross from the consumer into the library as data the library renders and echoes back verbatim -- never interprets, executes, or persists (INV-01). | Caller-formatted display strings + consumer-owned closures |
| Library -> Consumer app (callback firing) | `onConfirm`/`onCancel`/`onRemove` invoke consumer-owned closures; the library never knows or assumes what mutation/deletion/navigation they trigger. | Control-flow only, no payload |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-12-01 | Information Disclosure | `model/ProposedItemUiModel.kt`, `model/VoiceOutcomeUiState.kt` | medium | mitigate | `ProposedItemUiModel.toString()` overridden to print only `id`/`amended` (unit-tested). Code review (CR-01) additionally found and closed a related gap: `NeedsConfirmation`'s own default `toString()` would have leaked `title`/`reason` verbatim -- fixed with its own privacy-safe `toString()` override (`VoiceOutcomeUiState.kt:118-127`), unit-tested, mirroring SB's `PendingConfirmation.toString()` precedent (T-166-05). | closed |
| T-12-02 | Tampering | `component/OutcomeSheet.kt` (`OutcomeSheet.onDismissRequest` vs `NeedsConfirmation.onCancel` divergence) | low | mitigate | Documented in `NeedsConfirmation`'s own KDoc (`VoiceOutcomeUiState.kt:84-88`) that the consumer MUST route `onDismissRequest` to the same decline logic as `onCancel`. Not library-enforceable by design (pure data class, no composition-time wiring to intercept) -- the documented contract is the control; behavioral confirmation (swipe-dismiss/outside-tap/back declines cleanly) is Gate-1 self-UAT's job per 12-VALIDATION.md's Manual-Only Verifications table, not this audit's. | closed (documentation control verified present; behavioral check owned by Gate-1, not a precondition for threat closure since no code-level enforcement point exists to verify) |
| T-12-03 | Denial of Service | `component/OutcomeSheet.kt` (`NeedsConfirmationBody`'s `Column`+`forEach` over `items`) | low | accept | Real consumer batches are a handful of spoken items (CalTracker's `ProposedBatch`), never hundreds -- verified against the live call sites in 12-RESEARCH.md. A plain `Column` (matching `UndoAffordanceBody`/`BatchResultsList`'s existing precedent) is proportionate. A future large-batch consumer would need a `LazyColumn` revisit -- tracked as 12-RESEARCH.md Open Question #4, out of this phase's scope. | closed (accepted risk) |

*Status: open · closed · open — below {block_on} threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on (`high`) count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-12-01 | T-12-03 | Unbounded `Column`+`forEach` over a confirmation batch is a theoretical DoS only if a caller supplies an unbounded `items` list; both live consumers (SecondBrain single-item, CalTracker few-item batch) never do. Proportionate to current usage; revisit if a future consumer needs large batches. | orchestrator (phase-12 execute stage, --auto) | 2026-09-30 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-30 | 3 | 3 | 0 | orchestrator (plan-authored register, ASVS L1 short-circuit per secure-phase.md Step 3 -- `threats_open: 0 AND register_authored_at_plan_time: true AND asvs_level == 1`; direct classification against post-code-review-fix HEAD, no auditor dispatch needed) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-30
