---
phase: "11"
slug: "voice-outcome-failure-sheet"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-09-30"
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

Populated by the planner/executor as tasks are authored; the Nyquist finalizer completes it post-execution.

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 11-xx-xx | xx | 1 | VOUT-01/02 | — | Success/Failure render from props, no app nouns; "handled by" tier indicator renders | compose-ui | `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"` | ❌ W0 | ⬜ pending |
| 11-xx-xx | xx | 1 | VOUT-03 | — | Failure renders with error color role; optional action slot renders iff prop present | compose-ui | `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"` | ❌ W0 | ⬜ pending |
| 11-xx-xx | xx | 1 | VUNDO-01 | — | "Undo all (N)" + per-item undo + `Unavailable`/`Refused(reason, changedItem)` states render correctly | compose-ui | `./gradlew testDebugUnitTest --tests "*UndoAffordance*"` | ❌ W0 | ⬜ pending |
| 11-xx-xx | xx | 1 | VCLAR-01 | — | Clarification composable renders question + options; `onSelect`/`onDismiss` fire correctly | compose-ui | `./gradlew testDebugUnitTest --tests "*Clarification*"` | ❌ W0 | ⬜ pending |
| 11-xx-xx | xx | 1 | Naming-guard compliance (Pitfall 1) | — | Every new public composable's head token clears both drift guards | existing full-suite guard tests | `./gradlew testDebugUnitTest --tests "*DriftGuardTest*"` | ✅ | ⬜ pending |
| 11-xx-xx | xx | 1 | API additivity | — | `v2.4.0`-in-progress diff vs `v2.3.0` stays additive | static/build-time | `./gradlew apiCheck` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] New test file for the outcome/failure composable (name pending naming decision) — covers VOUT-01/02/03
- [ ] New test file for the undo affordance rendering — covers VUNDO-01
- [ ] New test file for the clarification-choices composable — covers VCLAR-01
- [ ] No new shared fixtures/conftest-equivalent needed — this repo's convention is per-family fixture functions declared in `explorer/*FamilyScreen.kt` (see `ProviderKeyCardFixture`/`ModelSelectCardFixture`/`ApproachLadderCardFixture` in `explorer/VoiceCommandFamilyScreen.kt`), plus ordinary Robolectric Compose test rules per test file — no framework install needed.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Loud-failure and undo-refused visual treatment (error roles, sticky, not `AttentionCue`); clarification chips/buttons swappable design call | VOUT-03 / VUNDO-01 / VCLAR-01 | Visual/interaction judgment (Yahir is design-conscious); CONTEXT.md explicitly defers chips-vs-buttons/bar-vs-sheet-state to gallery review | Launch `ExplorerActivity` → Voice Command family → drive Failure, undo-refused/partial, and clarification states in light + dark at Gate-1 on the tester |

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

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer (the `verify:post` →
> `validate-phase` hook, invoked by execute-phase `finalize_nyquist_validation` after Gate-1). Never
> set `nyquist_compliant: true` — or otherwise "sign off" compliance — at plan time, and do not let
> the plan-checker do so (INC-2026-07-27-01).

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 180s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time; the
      finalizer sets `true` iff its gap analysis finds zero gaps

**Approval:** pending — finalizer-owned, not set at plan time
