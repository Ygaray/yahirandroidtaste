---
phase: "17"
slug: "approachladdercard-router-on-off-toggle"
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-05"
---

# Phase 17 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric (`@Config(sdk = [35])`) + Compose UI test; Metalava `apiCheck`; detekt; javap descriptor diff |
| **Config file** | `build.gradle.kts` (existing); none to install |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest' --tests '*VoiceBinaryCompatShimTest' --tests '*VoiceI18nSourceCompatTest' --tests '*GalleryDemoInteractionTest'` |
| **Full suite command** | `./gradlew testDebugUnitTest && ./gradlew apiCheck && ./gradlew detekt` |
| **Estimated runtime** | ~300 seconds (cold Gradle) |

---

## Sampling Rate

- **After every task commit:** Run the quick run command plus `./gradlew detekt`
- **After the signature-changing task:** the api tail (`apiCheck` -> `apiDump` -> `cmp` -> `apiCheck`) and `./gradlew assembleRelease`
- **After every plan wave:** Run the full suite command
- **Before `/gsd-verify-work`:** Full suite must be green and the closing gate (incl. D-03 binary javap gate) must pass
- **Max feedback latency:** 300 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 17-01-01 | 01 | 1 | VAPPR-04 | — | N/A | Compose UI | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest'` | ❌ W0 | ⬜ pending |
| 17-01-02 | 01 | 1 | VAPPR-04 | — | N/A | Compose UI (registry-reached) | `./gradlew testDebugUnitTest --tests '*GalleryDemoInteractionTest'` | ❌ W0 | ⬜ pending |
| 17-01-03 | 01 | 1 | VAPPR-04 | — | N/A | gradle + git + javap | closing gate (see 17-RESEARCH.md Validation Architecture) | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] Router cases in `ApproachLadderCardTest.kt` (SC1-SC5, label overrides, placement, rung behaviour unchanged)
- [ ] Registry-reached gallery render test for the `ApproachLadderCard` fixture (D-02)
- [ ] `.planning/uat-pending/17-approachladdercard-router-on-off-toggle.md` Gate-2 registration

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Two stacked segmented toggles at card bottom: spacing, card height growth, light/dark contrast | VAPPR-04 | Visual/UX judgement (Gate-2, owner) | Open the Explorer gallery, Voice family, ApproachLadderCard; toggle Router on/off in light and dark |

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

**Approval:** pending
