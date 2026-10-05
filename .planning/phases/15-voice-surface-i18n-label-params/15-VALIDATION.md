---
phase: "15"
slug: "voice-surface-i18n-label-params"
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-05"
---

# Phase 15 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric + Compose UI test (`createComposeRule`), Metalava `apiCheck`, detekt |
| **Config file** | `build.gradle.kts` (existing — no install needed) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*ProviderKeyCardTest' --tests '*ModelSelectCardTest' --tests '*ClarificationBarTest' --tests '*ApproachLadderCardTest' --tests '*OutcomeSheetTest'` |
| **Full suite command** | `./gradlew testDebugUnitTest && ./gradlew apiCheck && ./gradlew detekt` |
| **Estimated runtime** | ~80 seconds quick; several minutes full |

If Gradle fails with `Already watching path`, rerun with `-Dorg.gradle.vfs.watch=false`.

---

## Sampling Rate

- **After every task commit:** Run the quick run command
- **After every plan wave:** Run the full suite command
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

Populated by the planner from PLAN.md task `<automated>` commands (see each PLAN.md). Requirements: VI18N-01 (ProviderKeyCard/ModelSelectCard labels), VI18N-02 (ClarificationBar dismissLabel), VI18N-03 (ApproachLadderCard rung/toggle labels), VI18N-04 (OutcomeSheet model fields).

*Status: pending*

---

## Wave 0 Requirements

- [ ] Override tests (non-English sentinels) in the five existing component test classes — VI18N-01..04
- [ ] Model default + old-arity `copy` equivalence test in `src/test/.../model/` — VI18N-04
- [ ] Optional compile-only source-compat fixture modeled on `ShowTagColorsSourceCompatTest`

---

## Manual-Only Verifications

All phase behaviors have automated verification.

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 120s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time

**Approval:** pending
