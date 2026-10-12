---
phase: "23"
slug: "providerkeycard-lifecycle-undo-center-groups"
status: secured
threats_open: 0
asvs_level: 1
audited_head: 36ac52c5982c641181d5fdebaef173284b15a250
created: "2026-10-12"
---

# Phase 23 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| caller -> ProviderKeyCard | Caller supplies the typed key (`keyValue`), a saved-key fingerprint, labels and callbacks; the card renders them and never stores or logs them | raw API key (field mode only), fingerprint, UI copy |
| caller -> UndoCenterScreen | Caller supplies entries, group labels and callbacks; rendered as given | static UI copy, entry messages |
| library -> semantics tree / accessibility / test tooling | The semantics tree is readable by accessibility services and UI tests | node text, tags |
| source tree -> api.txt | Committed signature file is the public-surface contract consumers recompile against | public API |
| repo -> release (tag/push) | Nothing in this phase is tagged or pushed; shipping is human-gated | commits |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Status | Evidence |
|-----------|----------|-----------|----------|-------------|--------|----------|
| T-23-01 | Info Disclosure | raw key vs semantics tree in summary mode | high | mitigate | CLOSED | `ProviderKeyCard.kt:125-152` real if/else; the key field is not composed in Summary mode; `ProviderKeyCardRawKeySemanticsTest` scans every config entry of every root for the sentinel with a Field-mode positive control |
| T-23-02 | Info Disclosure | fingerprint is a raw key | medium | accept | CLOSED | accepted risk AR-23-01; KDoc on `SavedKeyUiModel.kt:9-11` and the parameter |
| T-23-03 | Tampering | in-flight double submit | medium | mitigate | CLOSED | every action `enabled = !inFlight` (Save also `saveEnabled`); dropdown disabled while in flight; `ProviderKeyCardLifecycleTest` asserts not-enabled and zero callback on click |
| T-23-04 | Info Disclosure | key via toString/logs | medium | mitigate | CLOSED | new models carry no key property; no Log/println in phase main files; `ProviderKeyModelsTest` and `ProviderKeyCardSourceContractTest` pin it |
| T-23-05 | Tampering | protected v2.5.1 tests | high | mitigate | CLOSED | `git diff --exit-code v2.5.1` on the five protected tests is empty; golden constants unchanged since capture (one 3-line comment added in 5525b5c) |
| T-23-06 | Info Disclosure | key-looking test literals | low | mitigate | CLOSED | only the sentinel and the short `sk-ant-` ellipsis `abcd` fingerprint; no key-shaped literals in 23 changed files |
| T-23-07 | Tampering | api.txt churn / hidden shims | medium | mitigate | CLOSED | 16 removed lines, each with a same-member counterpart; no `DeprecationLevel.HIDDEN` added since v2.5.1; source contract pins two `ProviderKeyCard` functions |
| T-23-08 | DoS | long localized labels (card) | medium | mitigate | CLOSED | `ProviderKeyCardLongLabelTest` German and Spanish at 360dp NATIVE |
| T-23-09 | Repudiation | Gate-1 waiver | high | mitigate | CLOSED | `has-uat-criteria` true; no `device_exempt`; no hand-written SELF-UAT; Gate-1 run pending at audit time |
| T-23-10 | Tampering | accidental tag/push/repin | high | mitigate | CLOSED | `git tag --list "v2.6*"` empty; no push by this phase; no consumer edits |
| T-23-11 | Tampering | Undo all wrong id / twice / empty | medium | mitigate | CLOSED | `undoAllVisible` gating; `UndoCenterScreenGroupsTest` asserts exact id, one call, absence for FullyResolved/Empty/null callback; real-store end-to-end |
| T-23-12 | DoS | long localized labels (Undo Center) | medium | mitigate | CLOSED | `UndoCenterScreenLongLabelTest` German, Spanish and long group label at 360dp NATIVE |
| T-23-13 | Info Disclosure | caller group labels / entry messages | low | accept | CLOSED | accepted risk AR-23-02; no logging in the file, source contract forbids Log/println |

*Status: open / closed. Disposition: mitigate / accept / transfer.*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-23-01 | T-23-02 | A presentational card cannot tell a fingerprint from a key; the model and parameter KDoc state fingerprint only; residual risk owned by the consumer | plan-time (23-01) | 2026-10-12 |
| AR-23-02 | T-23-13 | The screen is presentational and renders what the consumer passes by design; nothing is persisted or logged by the library | plan-time (23-02) | 2026-10-12 |

Non-blocking observations: the golden `ProviderKeyCardDefaultParityTest` gained a 3-line comment after capture (constants unchanged); WR-03 now renders `KeyFieldState.Invalid.reason` (caller-supplied, not `keyValue`) as an error line in summary mode.

---

## Security Audit 2026-10-12

| Metric | Count |
|--------|-------|
| Threats found | 13 |
| Closed | 13 |
| Open | 0 |
