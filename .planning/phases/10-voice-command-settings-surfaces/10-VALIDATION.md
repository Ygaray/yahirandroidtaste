---
phase: "10"
slug: "voice-command-settings-surfaces"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-09-29"
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

Populated by the planner/executor as tasks are authored; the Nyquist finalizer completes it post-execution.

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 10-xx-xx | xx | 1 | VSET-01 | — | no key echoed/persisted; masked by default | compose-ui | `./gradlew testDebugUnitTest --tests "*ProviderKeyCard*"` | ❌ W0 | ⬜ pending |
| 10-xx-xx | xx | 1 | VSET-02 | — | model selection emits via callback | compose-ui | `./gradlew testDebugUnitTest --tests "*ModelSelectCard*"` | ❌ W0 | ⬜ pending |
| 10-xx-xx | xx | 1 | VAPPR-01/02/03 | — | ladder renders; cap + offline-only emit; hidden when prop null | compose-ui | `./gradlew testDebugUnitTest --tests "*ApproachLadderCard*"` | ❌ W0 | ⬜ pending |
| 10-xx-xx | xx | 1 | VSET/VAPPR | — | all new composables registered | registry | `./gradlew testDebugUnitTest --tests "*RegistryDriftGuard*"` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] New Compose UI test files for each card under `src/test/java/io/github/ygaray/yahirandroidtaste/component/`
- [ ] Registration assertions covered by the existing drift-guard tests (no new infra needed)

*Existing JUnit/Robolectric/Compose-UI infrastructure covers all phase requirements — no framework install.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Cap-control tap-a-rung feel; masked-key reveal affordance; controls hidden vs shown-disabled | VAPPR-03 / VSET-01 | Visual/interaction judgment (Yahir is design-conscious) | Launch `ExplorerActivity` → Voice Command family → inspect the three cards in light + dark at Gate-1 on the tester |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Frontmatter stays `status: draft` / `nyquist_compliant: false`; the Nyquist finalizer sets these post-execution after Gate-1.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 180s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time

**Approval:** pending — finalizer-owned, not set at plan time
