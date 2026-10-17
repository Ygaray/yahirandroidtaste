---
phase: "26"
slug: receipt-card-stacked-snackbar-deck
status: verified
threats_open: 0
asvs_level: 1
audited_head: d87e12a6761177ab11a6d18ca680461675e57b20
created: "2026-10-17"
---

# Phase 26 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Caller to library | Host app passes `FeedbackEvent` content, `FeedbackTarget` (opaque key + labels) and callbacks | Display strings, opaque target key (never rendered) |
| Touch and accessibility input to card | Pointer, swipe and TalkBack actions drive resolve (Opened / Undone / Dismissed / TimedOut) | User intent; no secrets |
| Library to consumer | `onOpenTarget(target)` is the only outbound call that carries the key, once per card | Opaque target key |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-26-01 | Denial of service | Pause reasons never cleared | high | mitigate | resetPause, showInternal finally, observer finally, host DisposableEffect (FeedbackStackState.kt, FeedbackDeck.kt, FeedbackStackHost.kt) | closed |
| T-26-02 | Tampering | Pause at expiry instant | high | mitigate | Lock-guarded single-winner resolve (FeedbackStackState.kt) | closed |
| T-26-03 | Tampering | internal timeSource / idleCollapseMillis seams | low | accept | Internal, volatile, not in api.txt, tests only | closed (accepted) |
| T-26-04 | Information disclosure | Secret-looking fixtures | low | mitigate | No credential patterns in feedback sources/tests; global secret scanner pre-commit | closed |
| T-26-05 | Information disclosure | FeedbackTarget.key leaks into text, description or tag | medium | mitigate | Card renders message/labels only; unmerged-tree key-absence tests | closed |
| T-26-06 | Elevation of privilege | New public API | medium | mitigate | All new composables internal/private; api.txt unchanged; source contract tests | closed |
| T-26-07 | Denial of service | Unbounded or tiny constraints | medium | mitigate | feedbackFannedCap infinite-height guard, 96dp floor, bounded-only scroll | closed |
| T-26-08 | Spoofing | Tapjacking by another window | low | accept | No overlay windows or permissions in feedback sources | closed (accepted) |
| T-26-09 | Tampering | Double navigation | high | mitigate | Single resolve-first onBodyTap on pointer and semantics paths | closed |
| T-26-10 | Tampering | Untargeted/spent/peek/long-hold/drag opens an item | high | mitigate | openTarget gating, pastSlop tracker, empty onLongPress | closed |
| T-26-11 | Denial of service | Stuck pause reason | high | mitigate | Observer try/finally, remember(state) keys, DisposableEffect, idle collapse | closed |
| T-26-12 | Information disclosure | Key/message logged | medium | mitigate | No Log/println; FeedbackTarget.toString omits key | closed |
| T-26-13 | Tampering | Swipe/Dismiss/timeout/eviction runs Undo | high | mitigate | FeedbackStackItem holds no undo lambda; settle calls dismiss only | closed |
| T-26-14 | Denial of service | Draggable offset read before anchors | high | mitigate | Seeded Settled anchor, NaN guards, no requireOffset | closed |
| T-26-15 | Tampering | List change mid-gesture | high | mitigate | key(item.id), remember(item.id), pointerInput(item.id) | closed |
| T-26-16 | Tampering | Second resolver vs swipe | medium | mitigate | Lock-guarded single-winner resolve | closed |
| T-26-17 | Elevation of privilege | Accessibility action bypasses exactly-once | high | mitigate | Semantics actions route through resolve-first callbacks, gated by openTarget/hasUndo/spent | closed |
| T-26-18 | Information disclosure | Key via contentDescription/stateDescription/tag/action label | medium | mitigate | Semantics inputs are labels only; raw-tree dump test | closed |
| T-26-19 | Repudiation | Registry/docs/api.txt drift | low | mitigate | Registry + allowlist entries, API.md rows, drift guards, api.txt unchanged | closed |
| T-26-20 | Spoofing | Tapjacking by another window | low | accept | Same basis as T-26-08 | closed (accepted) |
| T-26-SC | Tampering | Package installs | low | accept | No dependency, catalog or BOM change in the phase | closed (accepted) |

*Status: open, closed, or open below block_on threshold (non-blocking)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-26-01 | T-26-03 | internal seams are not public API; only tests set them | plan disposition (accept) | 2026-10-17 |
| AR-26-02 | T-26-08 | Library draws no overlay windows and requests no permissions; tapjacking is a host concern | plan disposition (accept) | 2026-10-17 |
| AR-26-03 | T-26-20 | Same as AR-26-02 | plan disposition (accept) | 2026-10-17 |
| AR-26-04 | T-26-SC | No dependency changes in the phase | plan disposition (accept) | 2026-10-17 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-17 | 21 | 21 | 0 | gsd-security-auditor (ASVS L1, block_on high) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-17
