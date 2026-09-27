---
phase: "08"
slug: "micbutton-hardening"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
# audited_head = git HEAD sha at audit time — freshness stamp. child-result re-checks it: if
# implementation (outside .planning) changed since this sha, the audit is stale (INC-2026-08-06-04).
audited_head: 15b41bc0563878a185ed5e882b343323111ba503
created: "2026-09-27"
---

# Phase 08 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Caller code -> `MicButton` parameters | Caller-supplied `String`s (description overrides) and lambdas (`onTap`/`onDisabledTap`) cross into library rendering + gesture-dispatch code. Caller is trusted app code (CalTracker/SecondBrain), not attacker-controlled input. | Plain-text UI strings + callback references |
| Compose runtime -> Android Accessibility Services | Rendered `contentDescription` text and the new semantics `OnClick` action are exposed to any active accessibility service (e.g. TalkBack) — read-only text exposure plus a caller-controlled activation action, no markup/code execution. | Plain text + semantics action invocation |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-08-01 | Tampering | `MicButton`'s `onPress` release-time dispatch (`onTap`/`onDisabledTap`) | medium | mitigate | Task 1 routes both callbacks through `rememberUpdatedState` so release-time dispatch always reads the current closure; proven by two RED->GREEN mid-press identity-swap regression tests (`tap_midPressCallbackIdentitySwap_firesOnlyLatestOnTap`, `tap_midPressDisabledTapCallbackIdentitySwap_firesOnlyLatestOnDisabledTap`) plus a mid-press `enabled`-flip test (`tap_midPressEnabledFlip_firesOnlyOnDisabledTap`). The code-review fix (CR-01) added a second dispatch path — a semantics-only `OnClick` action for TalkBack/keyboard activation — that reads the SAME `latestEnabled`/`latestOnTap`/`latestOnDisabledTap` bindings; this new path is itself proven by two dedicated tests (`semanticsOnClick_onEnabledMic_invokesOnTapExactlyOnce`, `semanticsOnClick_onDisabledMic_invokesOnDisabledTap_andNeverFiresOnTap`, commit `15b41bc`) that invoke `performSemanticsAction(SemanticsActions.OnClick)` directly rather than synthesizing a touch gesture. | closed |
| T-08-02 | Information Disclosure | `disabledDescription`/`tapToTalkDescription`/`listeningDescription` parameters | low | accept | Plain caller-supplied UI strings rendered only into Android's accessibility tree — no markup/HTML parsing, no code execution, no persistence, no network transmission. No new surface added by the CR-01 accessibility fix (it reuses the same description strings for the semantics tree). | closed |
| T-08-03 | Tampering (API surface) | `api.txt` / public `MicButton` signature | low | accept | Additive signature verified empirically by `apiCheck` (D-02) across all commits in this phase, including the code-review fixes — zero diff at every gate run (initial plan execution, review-fix, and the gap-closure test-only commit). No existing caller can be broken. | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on (high) count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-08-01 | T-08-02 | Plain-text accessibility label strings carry no injection/parsing surface; caller owns wording/localization policy. | Phase 08 plan threat model (accept disposition) | 2026-09-27 |
| AR-08-02 | T-08-03 | Additive-only API surface change, verified by `apiCheck` at zero diff on every commit in this phase. | Phase 08 plan threat model (accept disposition) | 2026-09-27 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-27 | 3 | 3 | 0 | gsd-secure-phase (L1, register authored at plan time — short-circuit per ASVS level 1, no auditor spawn required) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-27
