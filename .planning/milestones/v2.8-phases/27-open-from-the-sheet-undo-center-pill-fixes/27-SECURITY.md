---
phase: "27"
slug: open-from-the-sheet-undo-center-pill-fixes
status: verified
threats_open: 0
asvs_level: 1
audited_head: 5017ce6c46b8bb5c43816d862f33ea723acaf01d
created: "2026-10-17"
---

# Phase 27 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Caller to library | Host app passes `Success.target`, `BatchRowResultUiModel.target`, `UndoHistoryEntry.target` (opaque key + labels) and the `onOpenTarget` callback | Display strings, opaque target key (never rendered, never read, only handed back) |
| Touch and accessibility input to row | Tap, long-press, TalkBack actions and keyboard activation drive open / peek / undo | User intent; no secrets |
| Library to consumer | `onOpenTarget(target)` is the only outbound call that carries the key | Opaque target key |
| Tester device (Gate-1) | Gate-1 drives the wired tester over USB adb | Display settings only (font scale, density) |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-27-01 | Information disclosure | Opaque target key | medium | mitigate | No file reads the key or logs; the row hands the whole target to the handler and reads only `openLabel`; `OpenTargetHygieneSourceContractTest` plus semantics-tree key-leak tests; `FeedbackTarget.toString` omits the key | closed |
| T-27-02 | Tampering | Open an item whose write is unfinished or already Undone | high | mitigate | Row locked while `inFlight` (dim, `disabled()`, no click); KDoc requires the caller to clear `target` after Undo; lock tests in single and batch rows | closed |
| T-27-03 | Denial of service | Double tap navigates twice | low | accept | Library keeps no state; KDoc requires an idempotent handler | closed (accepted) |
| T-27-04 | Tampering | Source-compat break of OutcomeSheet, Success, BatchRowResultUiModel | high | mitigate | Params and fields appended with defaults; `V271CallSiteCanaryTest`; `apiCheck`; additive script `API SOURCE-ADDITIVE OK` | closed |
| T-27-05 | Tampering | Pinned Classic functions or `BatchResultsList` edited | medium | mitigate | Classic gets new functions only; both source-contract tests and parity tests unedited and green | closed |
| T-27-06 | Elevation of privilege | New helper becomes public API | low | mitigate | All new helpers internal or private; `theThreeNewFilesDeclareNothingPublic`; registry and catalog drift guards | closed |
| T-27-07 | Tampering | Tap opens the wrong batch row's item | high | mitigate | Each row binds its own target by value in its own lambda; equal-target and equal-label tests | closed |
| T-27-08 | Tampering | Target-less batch row clickable | high | mitigate | Merge and click only when `target != null`; inert-row semantics tests in both styles | closed |
| T-27-09 | Tampering | Open for Undone or Failed entry, group header, or stale lambda | high | mitigate | Open branch only for Available entries with a target (`openableTarget`); `rememberUpdatedState` guard; Undone, Failed, header and stale-after-Undo tests | closed |
| T-27-10 | Tampering | Hold, scroll or Undo tap opens or undoes by accident | medium | mitigate | One `combinedClickable` owner (replaced the planned `detectTapGestures` in review fix WR-01/WR-02; same intent): hold only peeks, tap is the only open path, Undo is its own node; gesture tests | closed |
| T-27-11 | Tampering | New UndoCenterScreen overload breaks or is ambiguous with existing calls | high | mitigate | Required non-null `onOpenTarget`; v2.7.1 overload unchanged and first; compat and canary tests | closed |
| T-27-12 | Tampering | Deliberate source-contract edit weakens the guard | medium | mitigate | Exact count 2 and full parameter list asserted; single-hunk edit | closed |
| T-27-13 | Denial of service | `createOutline` throws for Unspecified or em line height | medium | mitigate | 16sp fallback and non-negative clamp; unspecified-line-height and zero-size tests | closed |
| T-27-14 | Tampering | One-line pill look regresses | medium | mitigate | Radius equals `CircleShape` at font scales 1.0, 1.5, 2.0 (test); Gate-1 screenshot at 1.0 is the runtime half | closed |
| T-27-15 | Information disclosure | Low-contrast warn dot (WCAG 1.4.11) | medium | mitigate | `handledByDotRing` guards 3:1 over the composited fill; hub, synthetic and dynamic samples and grey-ramp totality tests | closed |
| T-27-16 | Tampering | Slip into pinned Classic code, dispatch literals or public surface | medium | mitigate | Dispatch-literal hygiene test; pin tests unedited; additive `api.txt` | closed |
| T-27-17 | Elevation of privilege | New file leaks a public declaration | low | mitigate | Same evidence as T-27-06 | closed |
| T-27-18 | Tampering | Gate-1 leaves tester font scale or density changed, or addresses the wrong device | medium | mitigate | Procedural control in 27-04-PLAN.md: USB serial R5CT10XNKQN only, restore font scale 1.0 and `wm density 450` (never `reset`); runtime compliance is checked against the Gate-1 log | closed |
| T-27-SC | Tampering | Dependency installs | low | accept | No dependency, catalog entry or BOM bump in the phase | closed (accepted) |

*Status: open · closed · open - below high threshold (non-blocking)*
*Severity: critical > high > medium > low - only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-27-01 | T-27-03 | The library adds no navigation state; the host handler must be idempotent (KDoc, FA-2 / PD-6) | plan disposition (accept) | 2026-10-17 |
| AR-27-02 | T-27-SC | No dependency changes in the phase | plan disposition (accept) | 2026-10-17 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-17 | 19 | 19 | 0 | gsd-security-auditor (ASVS L1, block_on high), audited_head 5017ce6 |

Implementation notes from the audit: (1) the T-27-10 plan text said `detectTapGestures`; the review fix replaced it with `combinedClickable`, with the same guarantees. (2) 27-04-PLAN.md labels `100.118.21.106:1496` as the personal phone, but per the device docs that address is the tester's wireless alias; the binding rule (USB serial R5CT10XNKQN only) is unaffected.

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-17
