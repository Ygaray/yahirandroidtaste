---
phase: "16"
slug: "a11y-failure-enrichment"
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-05"
---

# Phase 16 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric 4.16.1 + Compose UI test (`createComposeRule`), Metalava `apiCheck`, detekt |
| **Config file** | `build.gradle.kts` (existing — no install needed) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest' --tests '*OutcomeSheetTest' --tests '*VoiceModelLabelDefaultsTest' --tests '*VoiceI18nSourceCompatTest'` |
| **Full suite command** | `./gradlew testDebugUnitTest && ./gradlew apiCheck && ./gradlew detekt` |
| **Estimated runtime** | ~80 seconds quick; several minutes full |

If Gradle fails with `Already watching path`, rerun with `-Dorg.gradle.vfs.watch=false`.

---

## Sampling Rate

- **After every task commit:** Run the quick run command + `./gradlew apiCheck` + `./gradlew detekt`
- **After every plan wave:** Run `./gradlew testDebugUnitTest detekt apiCheck`
- **Before `/gsd-verify-work`:** Full suite must be green; `git diff v2.4.1 -- api.txt` shows only appended lines for `Failure` / `FailureActionUiModel` (plus the two expected `copy(optional …)` replacements)
- **Max feedback latency:** ~300 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 16-XX-XX | TBD | TBD | VA11Y-01 | — | N/A | Compose UI | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest'` | ❌ W0 | ⬜ pending |
| 16-XX-XX | TBD | TBD | VFAIL-01 | — | N/A | JVM + source-contract | `./gradlew testDebugUnitTest --tests '*VoiceModelLabelDefaultsTest' --tests '*FailureRoleSourceContractTest'` | ❌ W0 | ⬜ pending |
| 16-XX-XX | TBD | TBD | VFAIL-02 | — | N/A | Compose UI | `./gradlew testDebugUnitTest --tests '*OutcomeSheetTest'` | ❌ W0 | ⬜ pending |
| 16-XX-XX | TBD | TBD | VFAIL-03 | T-16-01 (info disclosure via toString) | prefix/body never carry sensitive text | Compose UI | `./gradlew testDebugUnitTest --tests '*OutcomeSheetTest'` | ❌ W0 | ⬜ pending |
| 16-XX-XX | TBD | TBD | SC5 additive | — | N/A | compile + apiCheck | `./gradlew testDebugUnitTest --tests '*VoiceI18nSourceCompatTest' && ./gradlew apiCheck` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

(Task IDs are filled by the planner; the Requirement → test map in `16-RESEARCH.md` § Validation Architecture is authoritative.)

---

## Wave 0 Requirements

- [ ] New tests in `ApproachLadderCardTest.kt` and `OutcomeSheetTest.kt` (non-default prefix sentinel, e.g. "Erreur :")
- [ ] Extend `VoiceModelLabelDefaultsTest.kt` + `VoiceI18nSourceCompatTest.kt` for `Failure` / `FailureActionUiModel`
- [ ] New `FailureRoleSourceContractTest` (source-contract idiom)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Rung row visual height / spacing after min-size modifier (D-04) | VA11Y-01 | Robolectric cannot judge visual pitch | Gate-2: open ExplorerActivity ApproachLadderCard, confirm rows comfortable (~58dp pitch) |
| Failure role color + TalkBack announcement of prefix | VFAIL-01, VFAIL-03 | Robolectric cannot assert color / speech | Gate-2 on device with TalkBack |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 300s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time

**Approval:** pending — finalizer-owned, not set at plan time
