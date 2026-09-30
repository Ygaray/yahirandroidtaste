---
phase: "10"
slug: "voice-command-settings-surfaces"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-09-29"
validated: "2026-09-30"
---

# Phase 10 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric + Compose UI test (`createComposeRule`), matching the existing `src/test` suite |
| **Config file** | `build.gradle.kts` (Android library module; no separate config) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*VoiceCommand*"` |
| **Full suite command** | `./gradlew testDebugUnitTest` |
| **Estimated runtime** | ~90–180 seconds (full suite) |

**Drift guards (must run in the FULL suite — CATALOG-03):** `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `ComponentRegistryTierTest`, `GeneratedSymbolDriftGuardTest`. Plus `./gradlew detekt` (zero baseline) and `./gradlew apiCheck` (additive vs `v2.3.0`).

---

## Sampling Rate

- **After every task commit:** Run `./gradlew testDebugUnitTest --tests "*VoiceCommand*"`
- **After every plan wave:** Run `./gradlew testDebugUnitTest` (full — the drift guards only fail here)
- **Before `/gsd-verify-work`:** Full suite + `detekt` + `apiCheck` all green
- **Max feedback latency:** ~180 seconds

---

## Per-Task Verification Map

Filled by the Nyquist finalizer post-execution (Gate-1 + goal-verification + code review + security
review all confirmed complete; see `10-VERIFICATION.md`, `10-REVIEW.md`, security threat
verification).

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 10-01 Task 2 (tracer) | 10-01 | 1 | VSET-01 | T-10-01, T-10-02, T-10-03, T-10-04 | no key echoed/persisted; masked by default | compose-ui | `./gradlew testDebugUnitTest --tests "*ProviderKeyCard*"` | ✅ | ✅ green (5/5) |
| 10-02 Task 1 | 10-02 | 2 | VSET-02 | T-10-06, T-10-07 | model selection emits via callback; empty→disabled+reason | compose-ui | `./gradlew testDebugUnitTest --tests "*ModelSelectCard*"` | ✅ | ✅ green (3/3) |
| 10-02 Task 2 | 10-02 | 2 | VAPPR-01/02/03 | T-10-05, T-10-06 | ladder renders; cap + offline-only emit; hidden when prop null | compose-ui | `./gradlew testDebugUnitTest --tests "*ApproachLadderCard*"` | ✅ | ✅ green (7/7) |
| 10-01 Task 2 / 10-02 Tasks 1-2 | 10-01, 10-02 | 1, 2 | VSET/VAPPR (all) | T-10-04, T-10-06 | all new composables registered XOR allowlisted | registry drift guard | `./gradlew testDebugUnitTest --tests "*DriftGuard*"` | ✅ | ✅ green (`DomainVocabularyDriftGuardTest` 2/2, `ComponentRegistryDriftGuardTest` 1/1) |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

Live re-confirmation for this finalization pass: `./gradlew testDebugUnitTest` run fresh from repo
root on 2026-09-30 → `BUILD SUCCESSFUL in 45s` (35 actionable tasks, 1 executed / 34 up-to-date —
no stale cache reuse of failing state, the executed task was `:testDebugUnitTest` itself).

---

## Wave 0 Requirements

- [x] New Compose UI test files for each card under `src/test/java/io/github/ygaray/yahirandroidtaste/component/` — confirmed present on disk: `ProviderKeyCardTest.kt`, `ModelSelectCardTest.kt`, `ApproachLadderCardTest.kt`
- [x] Registration assertions covered by the existing drift-guard tests (no new infra needed) — `ComponentRegistryDriftGuardTest` (1/1) + `DomainVocabularyDriftGuardTest` (2/2) both green

*Existing JUnit/Robolectric/Compose-UI infrastructure covers all phase requirements — no framework install.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Cap-control tap-a-rung feel; masked-key reveal affordance; controls hidden vs shown-disabled | VAPPR-03 / VSET-01 | Visual/interaction judgment (Yahir is design-conscious) | Launch `ExplorerActivity` → Voice Command family → inspect the three cards in light + dark at Gate-1 on the tester |

---

## Validation Sign-Off

> Finalized post-execution by the Nyquist finalizer (gates-only tail-gate re-drive) after Gate-1
> self-UAT (5/5 pass), goal-verification (13/13 must-haves), code review (skipped — zero diff since
> prior clean review), and security threat verification (0 open threats) all completed.

- [x] All tasks have `<automated>` verify or Wave 0 dependencies — every task in `10-01-PLAN.md` and `10-02-PLAN.md` carries an `<automated>` verify block (Task 2 of 10-01 has two; both tasks of 10-02 have two each); Task 1 of 10-01 is a `checkpoint:decision` gate, not an implementation task
- [x] Sampling continuity: no 3 consecutive tasks without automated verify — 3 implementation tasks total across both plans (10-01 Task 2, 10-02 Task 1, 10-02 Task 2), each with its own automated verify; the one non-implementation task (10-01 Task 1, decision checkpoint) does not break continuity since it precedes and gates all three
- [x] Wave 0 covers all MISSING references — the three new Compose UI test files + the two drift-guard tests are the only Wave 0 dependencies named in this file, and all five are confirmed present and green
- [x] No watch-mode flags — all commands in the Per-Task Verification Map and both PLAN.md files are one-shot `./gradlew testDebugUnitTest [...]` invocations; none use `--continuous`/`-t`
- [x] Feedback latency < 180s — live full-suite run measured 45s (`BUILD SUCCESSFUL in 45s`), well under the 180s budget; filtered single-card runs are faster still
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant` — set `true`: full suite genuinely green on a fresh live run (not inferred from prior SUMMARY/VERIFICATION claims), all three card test files exist, both drift guards pass, apiCheck/detekt independently confirmed green in `10-VERIFICATION.md`'s own live re-run

## Gaps

No blocking gaps. One test-quality (not test-coverage) gap was surfaced by code review and is
tracked, non-blocking:

- **WR-06** (`10-REVIEW.md`): `ProviderKeyCardTest.kt:168-184`'s error-state test is named for
  error-state coverage but only asserts the error text's presence, not that `isError` is actually
  set from an invalid `keyState`. This is a real assertion-strength gap in that one test — not a
  missing-test or missing-automated-command gap, so it does not block `nyquist_compliant`. The
  underlying production behavior (`isError = invalidState != null` at `ProviderKeyCard.kt:90`) was
  independently confirmed correct by direct code read during `10-VERIFICATION.md`'s review, so the
  requirement itself (VSET-01 error display) is not at risk — only this one test's assertion depth
  is weaker than its name implies. Tracked as a backlog test-hardening item, not re-opened here
  since fixing it would mean touching an already-passing, already-reviewed test file outside this
  finalization pass's docs-only scope.

**Approval:** approved — Nyquist finalizer, 2026-09-30, gates-only tail-gate re-drive
