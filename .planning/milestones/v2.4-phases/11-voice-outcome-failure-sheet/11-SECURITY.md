---
phase: "11"
slug: "voice-outcome-failure-sheet"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
# audited_head = git HEAD sha at audit time — freshness stamp. child-result re-checks it: if
# implementation (outside .planning) changed since this sha, the audit is stale (INC-2026-08-06-04).
audited_head: 3a6dfa4a05a0136eb74ed1aad321827d1b575cba
created: "2026-09-30"
---

# Phase 11 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Consumer app -> UndoHistoryStore/OutcomeSheet undo inputs | The consumer supplies arbitrary strings (label, allLabel, reason, changedItem) and suspend lambdas (undoAll, undoAction, per-row onUndo); the library stores/renders/invokes them as opaque callbacks and never inspects, persists, or logs their content (INV-01). | Opaque display strings + suspend callbacks |
| UndoHistoryStore.attemptUndoGroup -> consumer's undoAll lambda | The library invokes a consumer-supplied suspend lambda inside a Mutex-guarded critical section; a throwing lambda is caught and classified (Refused/Failed), never propagated as a crash except CancellationException. | Exception/result classification only |
| Consumer app -> ClarificationBar props | question/options[].label/options[].id and the onSelect/onDismiss callbacks are consumer-supplied; the library renders them as-is and never interprets or executes them (INV-01). | Opaque display strings + opaque id passthrough |
| ClarificationBar.onSelect(id) -> consumer's engine mapping | The opaque id crosses back out to the consumer, which maps it to the engine's real Clarification resolution; the library never inspects it. | Opaque id string |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-11-01 | Information Disclosure | attemptUndoGroup's caught UndoGroupRefusedException/generic Exception (reason/changedItem/message) | high | mitigate | No `Log.*`/`println` anywhere in `UndoHistoryStore.kt`, `UndoGroupTypes.kt`, `OutcomeSheet.kt` (verified by grep, zero matches) — mirrors `attemptUndo`'s existing best-effort-never-log convention. | closed |
| T-11-02 | Tampering (logic) | attemptUndoGroup's Mutex-guarded claim racing a concurrent attemptUndo(id) on the same grouped entry | medium | accept | Residual race documented in `attemptUndoGroup`'s KDoc (`UndoHistoryStore.kt:167-173`) with the consumer-side `Unavailable`-projection workaround; D-01 requires `attemptUndo(id)` stay unchanged. | closed |
| T-11-03 | Repudiation / Elevation of Privilege | attemptUndoGroup's atomic claim-run-resolve section | medium | mitigate | Entire claim->run->resolve section runs inside `groupUndoMutex.withLock` (`UndoHistoryStore.kt:176-199`); a Refused result leaves every claimed member `Available` again. Verified by executable test `UndoHistoryStoreTest.kt:446` (`attemptUndoGroup_refused_leavesEveryClaimedMemberAvailable_andARetriedCallCanSucceed`), not just doc intent. | closed |
| T-11-04 | Denial of Service (logic) | Group-atomic evictIfNeeded/clearSpent() removing more than one entry per pass | low | accept | Bounded-by-design; documented as expected in `evictIfNeeded`'s KDoc (`UndoHistoryStore.kt:263-274`) — group size is bounded by one voice-command batch, the <=50 cap invariant still holds. | closed |
| T-11-05 | Tampering | ClarificationBar.onSelect(id) passthrough on duplicate/malformed id | medium | mitigate | Library renders options in given order and calls `onSelect(option.id)` verbatim (`ClarificationBar.kt:79-86`), never dedups/normalizes; id-uniqueness documented as the CONSUMER's responsibility in `ClarificationOptionUiModel`'s KDoc (`ClarificationOptionUiModel.kt:9-13`). | closed |
| T-11-06 | Information Disclosure | ClarificationBar.question / options[].label rendering | medium | mitigate | No `Log.*`/`println` anywhere in `ClarificationBar.kt` (verified by grep, zero matches) — pure Compose rendering only. | closed |
| T-11-07 | Spoofing (silent auto-resolve) | ClarificationBar option resolution path | medium | mitigate | `onSelect`/`onDismiss` fire ONLY from a direct user tap (`AppChip.onClick` line 83 / `TextButton.onClick` line 89) — no `LaunchedEffect`/`DisposableEffect`/timer, no default-selected option, no programmatic auto-invoke path exists in the file. | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on (high) count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| R-11-01 | T-11-02 | Residual `attemptUndo`/`attemptUndoGroup` race on the same grouped entry is a single-user, near-simultaneous double-tap edge case; D-01 requires `attemptUndo(id)` stay unchanged, so no cross-path lock spans both methods. Consumer can close the gap by rendering in-flight group rows as `Unavailable`. | gsd-security-auditor (automated) | 2026-09-30 |
| R-11-02 | T-11-04 | Group-atomic `evictIfNeeded`/`clearSpent()` may drop below the 50-entry cap by more than one entry in a single eviction pass; bounded by design since one voice-command batch realistically appends single-digit entries. | gsd-security-auditor (automated) | 2026-09-30 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-30 | 7 | 7 | 0 | gsd-security-auditor (automated, ASVS L1) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-30
