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
| **Framework** | JUnit4 + Robolectric (`@Config(sdk = [35])`) + Compose UI test (`createComposeRule`), Metalava `apiCheck`, detekt |
| **Config file** | `build.gradle.kts` (existing) — no install needed |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest' --tests '*VoiceI18nSourceCompatTest' --tests '*GalleryDemoInteractionTest'` |
| **Full suite command** | `./gradlew testDebugUnitTest detekt && ./gradlew apiCheck` |
| **Estimated runtime** | ~300 seconds (full), ~90 seconds (quick) |

---

## Sampling Rate

- **After every task commit:** Run the quick run command plus `./gradlew detekt` (plus the api tail when the `ApproachLadderCard` signature changed)
- **After every plan wave:** Run the full suite command
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** ~300 seconds

---

## Per-Task Verification Map

Task IDs are finalized by the planner; see the PLAN.md `<verify>` blocks. Requirement coverage:

| Behavior | Requirement | Test Type | Automated Command | File Exists | Status |
|----------|-------------|-----------|-------------------|-------------|--------|
| Both non-null renders toggle reflecting state (SC1) | VAPPR-04 | Compose UI | `./gradlew testDebugUnitTest --tests '*ApproachLadderCardTest'` | new cases W0 | pending |
| Tap invokes `onRouterChange` (index 1 -> true) (SC2) | VAPPR-04 | Compose UI | same | new cases W0 | pending |
| Both null: no router node, existing tests unchanged (SC3) | VAPPR-04 | Compose UI + compile-only | `... --tests '*ApproachLadderCardTest' --tests '*VoiceI18nSourceCompatTest'` | existing + new | pending |
| Exactly one of pair throws (SC4) | VAPPR-04 | Compose UI (`assertThrows`) | same | new cases W0 | pending |
| Row tap still selects cap; no per-rung nav (SC5) | VAPPR-04 | Compose UI | same | new cases W0 | pending |
| `routerOnLabel`/`routerOffLabel` overrides (D-01) | VAPPR-04 | Compose UI | same | new cases W0 | pending |
| Placement + gallery fixture (D-02) | VAPPR-04 | Compose UI (registry-reached) | `... --tests '*GalleryDemoInteractionTest'` | W0 | pending |
| api.txt additive | VAPPR-04 | gradle + git | `./gradlew apiCheck` and released-baseline apiCheck vs `v2.4.1` | tooling exists | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] New router cases in `ApproachLadderCardTest.kt` (SC1-SC5, D-01 overrides, placement)
- [ ] Registry-reached gallery render test for the `ApproachLadderCard` fixture (D-02)
- [ ] (Optional) extra positional pin in `VoiceI18nSourceCompatTest.kt`

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Two stacked segmented toggles: spacing, card height growth, light/dark contrast | VAPPR-04 | Visual judgment (owner) | Gate-2: open Explorer, Voice family, ApproachLadderCard; compare router + offline toggles |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> Finalized only post-execution by the Nyquist finalizer.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 300s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time

**Approval:** pending
