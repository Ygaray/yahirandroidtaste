---
phase: "11"
slug: "voice-outcome-failure-sheet"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-09-30"
validated: "2026-09-30"
---

# Phase 11 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric + Compose UI test (`androidx.compose.ui.test.junit4`) + `kotlinx-coroutines-test` (confirmed in `build.gradle.kts`) |
| **Config file** | `build.gradle.kts` (module root; no separate test config) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.model.*"` (scope to new model/component test classes during development) |
| **Full suite command** | `./gradlew testDebugUnitTest` |
| **Estimated runtime** | ~90–180 seconds (full suite) |

**Drift guards (must run in the FULL suite — CATALOG-03):** `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `ComponentRegistryTierTest`, `GeneratedSymbolDriftGuardTest`. Plus `./gradlew detekt` (zero baseline) and `./gradlew apiCheck` (additive vs `v2.3.0`).

---

## Sampling Rate

- **After every task commit:** Run the scoped test command for the model/composable touched (see Per-Task map below).
- **After every plan wave:** Run `./gradlew testDebugUnitTest` (full — the drift guards only fail here per PITFALLS.md Pitfall 4).
- **Before `/gsd-verify-work`:** Full suite + `detekt` + `apiCheck` all green. Gate-1 self-UAT must explicitly drive the Failure and undo-refused/partial branches on-device, not just the happy path (CONTEXT.md "Specific Ideas").
- **Max feedback latency:** ~180 seconds

---

## Per-Task Verification Map

Finalized post-execution by the Nyquist finalizer (`/gsd-validate-phase 11 --auto`, auto-mode
chain, 2026-09-30) — zero gaps found, all requirements automated-covered.

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 11-01 Task 1 (tracer) | 01 | 1 | VUNDO-01 | T-11-01..04 | Grouped undo API (`openGroup`/`attemptUndoGroup`/`groupStatus`), Mutex-guarded atomic claim-run-resolve, releasable-on-Refused | unit (coroutines-test) | `./gradlew testDebugUnitTest --tests "*UndoHistoryStoreTest*" --tests "*OutcomeSheetTest*"` | ✅ | ✅ green |
| 11-01 Task 2 (auto) | 01 | 1 | VUNDO-01, VOUT-01/02 | T-11-01..04 | "Undo all (N)" + per-item undo + `Unavailable`/`Refused(reason, changedItem)` states render correctly; `inFlight` disables undo controls (CR-02 fix) | compose-ui | `./gradlew testDebugUnitTest && ./gradlew detekt && ./gradlew apiCheck` | ✅ | ✅ green |
| 11-02 Task 1 (auto) | 02 | 1 | VCLAR-01 | T-11-05..07 | Clarification composable renders question + options; `onSelect`/`onDismiss` fire correctly; opaque-id passthrough, no auto-resolve | compose-ui | `./gradlew testDebugUnitTest --tests "*ClarificationBarTest*"` | ✅ | ✅ green |
| 11-02 Task 2 (auto) | 02 | 1 | VCLAR-01 | T-11-05..07 | All four specless-probe VCLAR-01 edges pass as automated tests; id-uniqueness documented as consumer responsibility | compose-ui | `./gradlew testDebugUnitTest && ./gradlew detekt && ./gradlew apiCheck` | ✅ | ✅ green |
| CR-01/02, WR-01..04 (review-fix) | fix | — | VOUT-01/02/03, VUNDO-01, VCLAR-01 | — | api.txt regenerated; `inFlight`→`locked` threaded into undo render path; `require()` maxTierId/onMaxTierChange pairing; `ClarificationBar` `FlowRow` wrap; `ProviderKeyCard` KDoc corrected | unit + compose-ui + static | `./gradlew testDebugUnitTest detekt apiCheck --no-daemon` | ✅ | ✅ green |
| Naming-guard compliance (Pitfall 1) | both | 1 | — | — | Every new public composable's head token clears both drift guards | existing full-suite guard tests | `./gradlew testDebugUnitTest --tests "*DriftGuardTest*"` | ✅ | ✅ green |
| API additivity | both | 1 | — | — | `v2.4.0`-in-progress diff vs `v2.3.0` stays additive | static/build-time | `./gradlew apiCheck` | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

**Coverage verified independently three times this cycle:** full suite + detekt + apiCheck re-run
by the orchestrator post-fix (`BUILD SUCCESSFUL`); targeted re-run by `gsd-verifier`
(`OutcomeSheetTest` 15/15, `ClarificationBarTest` 9/9, `UndoHistoryStoreTest` group tests 25/25); and
a fresh on-device re-run by `gsd-agentic-tester` during Gate-1 self-UAT (`OutcomeSheetTest` 15/15,
`ClarificationBarTest` 9/9, `UndoHistoryStoreTest` 25/25). Zero gaps.

---

## Wave 0 Requirements

- [x] New test file for the outcome/failure composable — `OutcomeSheetTest.kt` covers VOUT-01/02/03
- [x] New test file for the undo affordance rendering — `UndoHistoryStoreTest.kt` + `OutcomeSheetTest.kt` cover VUNDO-01
- [x] New test file for the clarification-choices composable — `ClarificationBarTest.kt` covers VCLAR-01
- [x] No new shared fixtures/conftest-equivalent needed — this repo's convention is per-family fixture functions declared in `explorer/*FamilyScreen.kt` (see `ProviderKeyCardFixture`/`ModelSelectCardFixture`/`ApproachLadderCardFixture` in `explorer/VoiceCommandFamilyScreen.kt`), plus ordinary Robolectric Compose test rules per test file — no framework install needed.

---

## Manual-Only Verifications

**Resolved at Gate-1 (2026-09-30) — no items remain manual-only.** The goal-backward verifier
flagged two visual/interaction-judgment items as `human_verification`; `gsd-agentic-tester` drove
both live on-device (Samsung SM-S908U, `yahirs-s22-ultra-2`) in light + dark and recorded PASS for
all 6 Gate-1 criteria (see `11-02-SELF-UAT.md`). The original manual-only row is preserved below
for history; it is no longer outstanding.

| Behavior | Requirement | Why Manual | Resolution |
|----------|-------------|------------|------------|
| Loud-failure and undo-refused visual treatment (error roles, sticky, not `AttentionCue`); clarification chips/buttons swappable design call | VOUT-03 / VUNDO-01 / VCLAR-01 | Visual/interaction judgment (Yahir is design-conscious); CONTEXT.md explicitly defers chips-vs-buttons/bar-vs-sheet-state to gallery review | **Resolved via Gate-1 self-UAT** — driven live on-device (ExplorerActivity → Voice Command family), held-press ripple-capture technique confirmed genuine per-element tap-responsiveness; all 6 criteria PASS. Gate-2 fragment registered in `.planning/HUMAN-UAT-PENDING.md` as a read-confirmation-only item (no outstanding visual concerns). |

---

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-------------------|
| V2 Authentication | No | No auth surface — renders outcomes/undo/clarification from props only |
| V3 Session Management | No | No session concept in this library |
| V4 Access Control | No | Library never gates access, only renders |
| V5 Input Validation | Marginal/Yes | Clarification `id` is an OPAQUE string the library never interprets or executes — the only "validation" responsibility is to pass the tapped `id` back verbatim via `onSelect`, never parse/execute it. No injection surface exists. |
| V6 Cryptography | No | No cryptographic material touched; undo/outcome data is plain display state |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|----------------------|
| A consumer accidentally logs or displays a secret (e.g. an API key) inside a `Failure.reason` string | Information Disclosure | Out of this phase's direct control — but the library must not itself log any prop value anywhere |
| A malformed/duplicate clarification option `id` causes `onSelect` to resolve the wrong engine-side clarification | Tampering (logic-level) | The library renders `options` in the order given and calls `onSelect(option.id)` verbatim — uniqueness of `id` within one `options` list is the CONSUMER's responsibility; document in KDoc |

**Formal threat verification:** see `.planning/phases/11-voice-outcome-failure-sheet/11-SECURITY.md`
— 7/7 threats (T-11-01..07) closed, `threats_open: 0`, audited at ASVS L1 (`gsd-security-auditor`,
2026-09-30).

---

## Validation Audit 2026-09-30

| Metric | Count |
|--------|-------|
| Requirements checked | 5 (VOUT-01, VOUT-02, VOUT-03, VUNDO-01, VCLAR-01) |
| Gaps found | 0 |
| Resolved | 0 (none needed) |
| Escalated | 0 |

**Mode:** auto (`--auto` chain, `finalize_nyquist_gate`). Per the auto-mode contract, the
`gsd-nyquist-auditor` was not spawned — a gap audit against existing SUMMARY/PLAN/VALIDATION
artifacts plus the independently-confirmed green full suite found zero gaps, so no gap-filling
was needed.

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 180s
- [x] `nyquist_compliant: true` — finalizer's gap analysis found zero gaps (2026-09-30)

**Approval:** verified 2026-09-30 (Nyquist finalizer, auto-mode chain)
