---
phase: "12"
slug: "generic-needs-confirmation-state"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-09-30"
---

# Phase 12 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric + Compose UI test (`androidx.compose.ui.test.junit4`), confirmed live in `src/test/.../OutcomeSheetTest.kt:1-24` (`@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`) |
| **Config file** | `build.gradle.kts` (module root) — unchanged |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"` |
| **Full suite command** | `./gradlew testDebugUnitTest` (runs both drift guards — full-suite-only) |
| **Estimated runtime** | ~60 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"`
- **After every plan wave:** Run `./gradlew testDebugUnitTest` (full suite — required for both drift guards)
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 12-01-xx | 01 | 1 | VOUT-04 | T-12-01 | Single-item `NeedsConfirmation` renders reason/title/item; Confirm/Cancel fire callbacks | unit/Robolectric Compose | `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"` | ✅ existing file, new test cases | ⬜ pending |
| 12-01-xx | 01 | 1 | VOUT-04 | T-12-01 | Batch `NeedsConfirmation` renders every row; `topLevelContent` renders once; per-item `onRemove` fires when supplied | unit/Robolectric Compose | `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"` | ✅ existing file, new test cases | ⬜ pending |
| 12-01-xx | 01 | 1 | VOUT-04 | T-12-02 | `severity = Destructive` renders the Confirm button with error-tinted styling (`ActionButtonDefaults.colors`) | unit/Robolectric Compose | `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*"` | ✅ existing file, new test case | ⬜ pending |
| 12-01-xx | 01 | 1 | VOUT-04 | T-12-03 | `ProposedItemUiModel.toString()` omits `title`/`subtitle`/`confidenceCue` (no PII leak into logs) | unit | extend `OutcomeSheetTest.kt` or a dedicated model test | ✅ existing file, new test case | ⬜ pending |
| 12-01-xx | 01 | 1 | Naming-guard compliance | — | No new public composable; neither drift guard needs a new allowlist entry | full-suite guard tests | `./gradlew testDebugUnitTest --tests "*DriftGuardTest*"` | ✅ guards exist | ⬜ pending |
| 12-01-xx | 01 | 1 | API additivity | — | `v2.4.0`-in-progress diff vs `v2.3.0` stays additive (new `NeedsConfirmation`/`ProposedItemUiModel`/`SelectionMode` symbols only; `Success`/`Failure` unchanged) | static/build-time | `./gradlew apiCheck` | ✅ tooling exists | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. No new test framework install or shared fixtures needed — existing Robolectric+Compose harness is sufficient.

- [ ] New test cases in `OutcomeSheetTest.kt` under a `// ── VOUT-04 ── ...` section (matching the file's existing naming convention) — covers `NeedsConfirmation` single/batch/destructive/topLevelContent/onRemove rendering and callback firing.
- [ ] New fixture outcomes + "Show sheet" buttons in `VoiceCommandFamilyScreen.kt`'s `OutcomeSheetVariants()` — gallery coverage for Gate-1 visual review.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Swipe-to-dismiss / outside-tap / back on an open `NeedsConfirmation` sheet behaves as decline, without crashing or flashing | VOUT-04 | Gesture-driven dismissal interaction is not exercised by Robolectric unit tests | Gate-1 self-UAT: open a `NeedsConfirmation` sheet in the Explorer gallery, trigger back/outside-tap/swipe, confirm it declines cleanly |
| Destructive-severity confirm's visual treatment reads correctly on-device (not just asserted via semantics in test) | VOUT-04 | Visual color/contrast review needs human eyes | Gate-1 self-UAT: open a Destructive-severity `NeedsConfirmation` fixture, visually confirm red/error-tinted Confirm button |
| Batch confirm with at least one `onRemove` tap end-to-end | VOUT-04 | Full interactive flow (remove a row, then confirm the remainder) is best confirmed by a human walkthrough even though the callback itself is unit-tested | Gate-1 self-UAT: open a batch `NeedsConfirmation` fixture, tap remove on one row, confirm the remaining items |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer (the `verify:post` →
> `validate-phase` hook, invoked by execute-phase `finalize_nyquist_validation` after Gate-1). Never
> set `nyquist_compliant: true` — or otherwise "sign off" compliance — at plan time, and do not let
> the plan-checker do so (INC-2026-07-27-01: a premature plan-time flip is what caused inconsistent
> COMPLIANT/PARTIAL milestone-audit states).

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time; the
      finalizer sets `true` iff its gap analysis finds zero gaps

**Approval:** pending — finalizer-owned, not set at plan time
