---
phase: "22"
slug: "outcomesheet-receipt-style"
status: secured
threats_open: 0
asvs_level: 1
audited_head: 91d2aa2786136c1373fcf6a56cd09e76952916f2
created: "2026-10-12"
---

# Phase 22 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| caller -> OutcomeSheet (Classic and Receipt) | Caller-supplied strings (reason, title, transcript, summary, tip, labels), actions and composable slots cross into the library; rendered as given | static UI copy, user speech transcript |
| library -> consumer test tooling | Stable test tags visible only if a consumer opts in to resource-id exposure | test tags |
| source tree -> api.txt | Committed signature file is the public-surface contract consumers recompile against | public API |
| repo -> release (tag/push) | Nothing in this phase is tagged or pushed; shipping is human-gated | commits |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Status | Evidence |
|-----------|----------|-----------|----------|-------------|--------|----------|
| T-22-01 | Info Disclosure | Failure.toString | medium | mitigate | CLOSED | `VoiceOutcomeUiState.kt:122-124` prints only tone and null-ness flags; `OutcomeReceiptModelsTest.kt:216-229` asserts secrets absent |
| T-22-02 | Info Disclosure | test tags | medium | mitigate | CLOSED | `testTagsAsResourceId` appears only in two KDoc comments; `ToneFoundationInvariantsTest.kt:70-84` scans all of `src/main` |
| T-22-03 | Tampering | one-way dependency | medium | mitigate | CLOSED | new files in `ToneFoundationInvariantsTest.phaseFiles`; imports are androidx/kotlin/own-package only; no new public composable |
| T-22-05 | Tampering | release | high | mitigate | CLOSED | `create_tag: false`; no v2.6* tag; origin/main unchanged (`b6c363c`); no consumer edits |
| T-22-06 | DoS | long labels | medium | mitigate | CLOSED | title `maxLines = 2`, pill text weight, measured `ReceiptActionRow`; `OutcomeSheetReceiptLongLabelTest` at 360dp |
| T-22-07 | Tampering | confirm gating | high | mitigate | CLOSED | `confirmEnabled` honoured in Receipt and Classic via `DynamicActionButton`; tests click a disabled confirm with zero `onConfirm` |
| T-22-08 | Tampering | binary-compat shims | medium | mitigate | CLOSED | 9 api.txt removals each with a widened counterpart; no HIDDEN/copy$default/new JvmOverloads shims |
| T-22-09 | Tampering | NeedsConfirmation call sites | low | mitigate | CLOSED | all constructions (library and read-only consumer check) use named args; v2.5.1 shape pinned by `OutcomeSheetSourceCompatV251Test` |
| T-22-10 | Info Disclosure | caller strings | low | accept | CLOSED | accepted risk AR-22-01; no logging in `src/main` |
| T-22-11 | DoS | pinned bar | medium | mitigate | CLOSED | bar is a sibling after the weighted scroll body; geometry test at 240dp |
| T-22-12 | Tampering | Classic parity | high | mitigate | CLOSED | SHA-256 pins of unchanged v2.5.1 functions plus per-arm tag/text inventories; protected tests byte-identical |
| T-22-13 | Info Disclosure | live region | low | mitigate | CLOSED | Assertive live region only on the refused surface; exactly-one-node tests |
| T-22-14 | Tampering | api.txt churn | medium | mitigate | CLOSED | only 22-01 and 22-02 commits touch api.txt |
| T-22-15 | Tampering | in-flight actions | high | mitigate | CLOSED | `actionEnabled = enabled && (!inFlight \|\| activeWhileInFlight)`; matrix test |
| T-22-16 | Info Disclosure | handled-by pill | low | accept | CLOSED | accepted risk AR-22-01 |
| T-22-17 | Repudiation | Gate-1 waiver | high | mitigate | CLOSED | no `device_exempt`; Gate-1 not waived or hand-written; handoff recorded |
| T-22-20 | Tampering | protected tests | high | mitigate | CLOSED | `OutcomeSheetTest`, `FailureRoleSourceContractTest`, `DataClassBinaryCompatShimTest` identical to v2.5.1 |

*Status: open / closed. Disposition: mitigate / accept / transfer.*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-22-01 | T-22-10, T-22-16 | The library renders caller strings and never logs them; low severity; `Failure.toString` redacts free text and user speech | plan-time (22-01/03/04) | 2026-10-12 |

Non-blocking caveats from the audit: the "static UI copy only" KDoc sentence exists on `Failure.semanticsPrefix` and `SheetActionUiModel` but not on `HandledByUiModel`, `Failure.transcript` or the `Success` text fields; `Success` has no `toString` redaction (pre-existing, outside the register); T-22-12 carried inconsistent severities across plans (high taken).

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-12 | 17 | 17 | 0 | gsd-security-auditor (secure-phase --auto) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: secured` set in frontmatter

**Approval:** verified 2026-10-12
