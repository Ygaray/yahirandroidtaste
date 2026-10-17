---
phase: "25"
slug: feedback-target-model-stack-state
status: verified
threats_open: 0
asvs_level: 1
audited_head: 347aec0a5a927222b15d9bf011fd57ddf0b1a20f
created: "2026-10-17"
---

# Phase 25 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| consumer app to library | The consumer builds `FeedbackTarget.key` and `openLabel`; the library treats `key` as an opaque string and `openLabel` as display text | opaque id string, display text |
| library singleton to consumer lifetimes | `UndoHistoryStore` is a `@Singleton`; anything stored on an entry outlives the screen that created it | two Strings only (no lambda, no Context) |
| caller coroutine to stack state | `FeedbackStackState.show()` suspends on a caller coroutine; cancellation, racing taps and eviction must each resolve exactly once | none (control flow) |

---

## Threat Register

29 threats across plans 25-01 to 25-05 (27 mitigate, 2 accept). Full per-threat evidence (file:line, test names) is in the auditor verdict of 2026-10-17; the key controls are summarized below.

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-25-01 | Information disclosure | FeedbackTarget retained by singleton store | medium | mitigate | Two Strings only; FeedbackTargetTest reflects over declared fields | closed |
| T-25-02 | Tampering | `key` parsed/logged/echoed | medium | mitigate | No parsing code; only import is `Immutable`; `toString` omits key | closed |
| T-25-03 | Tampering | Source-compat break of Notify/WithUndo | high | mitigate | Appended defaulted params; V271CallSiteCanaryTest; `verify-api-source-additive.sh v2.7.1` OK | closed |
| T-25-04 | Denial of service | Throwing `init` on blank openLabel | low | mitigate | No `init`/`require`; blank label construct test | closed |
| T-25-05 | Information disclosure | Secret-looking fixtures | low | mitigate | Neutral fixtures; gitleaks v2.7.1..HEAD clean | closed |
| T-25-06 | Tampering | Openable target after Undone/Failed | high | mitigate | `openableTarget` null unless Available; store tests | closed |
| T-25-07 | Tampering | Undo guard bypass | high | mitigate | `attemptUndo` untouched; CAS `tryConsume`; race tests | closed |
| T-25-08 | Tampering | Material3 tracked path regression | medium | mitigate | Only `target` forwarded; UndoTrackingMaterialPathTest | closed |
| T-25-09 | Information disclosure | Target retained in singleton history | low | accept | See Accepted Risks Log AR-25-01 | closed |
| T-25-10 | Tampering | `append` overload ambiguity | medium | mitigate | Required `target` param, canary compiles old shapes | closed |
| T-25-11 | Tampering | Double resolution of a card | high | mitigate | Winner claimed under lock; 8-way parallel resolver test | closed |
| T-25-12 | Denial of service | Leaked slot / timers | medium | mitigate | `finally` cancels timer and watcher; freed-slot tests | closed |
| T-25-13 | Denial of service | Caller cancellation leaves card | medium | mitigate | `finally` resolves Dismissed and removes item | closed |
| T-25-14 | Information disclosure | Stale accessibility scaler binding | medium | mitigate | Reference-counted binding, null after last detach | closed |
| T-25-15 | Denial of service | Non-cooperative caller holds a slot | low | accept | See Accepted Risks Log AR-25-02 | closed |
| T-25-16 | Information disclosure | Logging of event content | low | mitigate | No logging in phase code; items hold no lambda/Context | closed |
| T-25-17 | Tampering | Undo lambda run outside the store guard | high | mitigate | Only `store.attemptUndo` runs the lambda; both race orders tested | closed |
| T-25-18 | Tampering | Cancellation drops an Undone undo | medium | mitigate | `withContext(NonCancellable)`; cancel-during and cancel-after-tap tests | closed |
| T-25-19 | Denial of service | Watcher coroutine leak | medium | mitigate | Watcher cancelled in `finally` | closed |
| T-25-20 | Repudiation | Entry missing while snackbar visible | medium | mitigate | `append` before `show`; ordering test | closed |
| T-25-21 | Tampering | Undo on Opened/Dismissed/TimedOut | high | mitigate | `attemptUndo` only on `Undone`; negative tests | closed |
| T-25-22 | Information disclosure | Logging in tracked path | low | mitigate | No logging | closed |
| T-25-23 | Tampering | Double-open from double tap | high | mitigate | `onOpenTarget` only for the tap that resolved Opened | closed |
| T-25-24 | Tampering | Open/Undo on a spent card | high | mitigate | `openable` excludes spent; spent hides Undo and chevron | closed |
| T-25-25 | Information disclosure | Scaler binding outlives composition | medium | mitigate | `DisposableEffect` detach on dispose | closed |
| T-25-26 | Elevation of privilege | Blank openLabel renders empty action | low | mitigate | `ifBlank { message }` fallback | closed |
| T-25-27 | Tampering | Library invariant breach (consumer import, Hilt host) | medium | mitigate | LibraryInvariantsGuardTest | closed |
| T-25-28 | Information disclosure | Secrets in docs/demo | low | mitigate | Neutral fixtures, gitleaks clean | closed |
| T-25-29 | Repudiation | Stale registry counts in docs | low | mitigate | CatalogDocsDriftGuardTest (67/6/73) | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above `workflow.security_block_on` (high) count toward threats_open*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-25-01 | T-25-09 | The singleton `UndoHistoryStore` already retains message and preview; retaining a two-String `FeedbackTarget` adds no new class of data. Enforced by the reflection test (no lambda, no Context). | plan 25-02 threat model (disposition: accept) | 2026-10-17 |
| AR-25-02 | T-25-15 | A caller that never cancels or resolves holds one stack slot; documented in the `FeedbackStackState` KDoc. Cancelling the caller or `dismissAll` resolves the card. | plan 25-03 threat model (disposition: accept) | 2026-10-17 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-17 | 29 | 29 | 0 | gsd-security-auditor (verdict SECURED, ASVS 1, block_on high, audited at 347aec0) |

Advisory (non-blocking): T-25-24 has a short residual window between an entry being undone elsewhere and the watcher flipping `spent`; this matches the declared mitigation. Unregistered flags: none.

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-17
