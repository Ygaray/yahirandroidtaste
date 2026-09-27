---
phase: "08"
slug: "micbutton-hardening"
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-09-27"
---

# Phase 08 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric + androidx.compose.ui.test (Compose UI testing) |
| **Config file** | `build.gradle.kts` (testDebugUnitTest source set) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*MicButtonGestureTest*"` |
| **Full suite command** | `./gradlew testDebugUnitTest detekt apiCheck publishReleasePublicationToMavenLocal` |
| **Estimated runtime** | ~15-20 seconds (quick) / ~30-60 seconds (full battery) |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew testDebugUnitTest --tests "*MicButtonGestureTest*"`
- **After every plan wave:** Run `./gradlew testDebugUnitTest detekt apiCheck publishReleasePublicationToMavenLocal`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** ~60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 08-01-01 | 01 | 1 | MICBTN-02 | T-08-01 | `onTap`/`onDisabledTap` dispatch reads the latest closure at release, never a stale mid-press one | unit (Compose gesture test) | `./gradlew testDebugUnitTest --tests "*MicButtonGestureTest*"` | ✅ | ✅ green |
| 08-01-02 | 01 | 1 | MICBTN-01 / MICBTN-03 | — / — | Parameterized microcopy with generic defaults; hub-vocabulary KDoc; sensible `enabled`/`onDisabledTap` defaults | unit (Compose gesture test) + static grep | `./gradlew testDebugUnitTest --tests "*MicButtonGestureTest*"` | ✅ | ✅ green |
| 08-01-03 | 01 | 1 | MICBTN-01 / MICBTN-02 / MICBTN-03 | T-08-03 | Additive API surface; zero-baseline detekt; publish-clean | unit + static (apiCheck/detekt) | `./gradlew apiDump && ./gradlew testDebugUnitTest detekt apiCheck publishReleasePublicationToMavenLocal` | ✅ | ✅ green |
| 08-gap-01 | 01 (gap-closure) | 1 | MICBTN-02 (CR-01 remediation) | T-08-01 | Accessibility semantics `OnClick` action dispatches to the latest `onTap`/`onDisabledTap`, mirroring the touch path — proven via `performSemanticsAction(SemanticsActions.OnClick)`, not `performTouchInput` | unit (Compose semantics-action test) | `./gradlew testDebugUnitTest --tests "*MicButtonGestureTest*"` | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

*Existing infrastructure covers all phase requirements.*

---

## Manual-Only Verifications

*All phase behaviors have automated verification.* (Gate-1 self-UAT additionally confirmed the
semantics-node's `clickable=true`/`focusable=true` reachability live on-device via uiautomator,
supplementing — not substituting for — the unit-level `performSemanticsAction` proof above.)

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references (none — existing infra sufficient)
- [x] No watch-mode flags
- [x] Feedback latency < 60s
- [x] `nyquist_compliant: true` — gap analysis found zero gaps: all 3 requirements
      (MICBTN-01/02/03), including the code-review-fix remediation (CR-01's accessibility
      semantics action), have automated test coverage that runs green.

**Approval:** approved 2026-09-27

---

## Validation Audit 2026-09-27

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

Reconstructed from `08-01-PLAN.md`/`08-01-SUMMARY.md`/`08-REVIEW-FIX.md` (State B — no prior
`08-VALIDATION.md` existed). All 3 requirements cross-reference cleanly to
`.planning/REQUIREMENTS.md` with no orphans. The one gap the phase's own verification loop
surfaced (CR-01's accessibility semantics action lacking a behavioral test) was already closed
before this finalizer ran (commit `15b41bc`, confirmed passing in `08-VERIFICATION.md`'s
re-verification pass) — so this audit finds zero remaining gaps, not a gap it is deferring.
