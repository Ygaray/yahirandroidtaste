---
phase: "15"
slug: "voice-surface-i18n-label-params"
status: validated
nyquist_compliant: true
wave_0_complete: true
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

Every task below uses the same additive-gate tail: `F=$(mktemp) && cp api.txt "$F" && ./gradlew apiDump && cmp "$F" api.txt && ./gradlew apiCheck` (api.txt freshness + Metalava). Within each task `./gradlew apiCheck` is ALSO run before `apiDump` (authoritative additive gate, D-01). Each task writes its own new tests first (tdd), so the new test files/cases are created inside the task rather than in a separate Wave 0.

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command (head; additive-gate tail above unless noted) | Status |
|---------|------|------|-------------|-----------|------------------------------------------------------------------|--------|
| 15-01-01 | 01 | 1 | VI18N-01 | Compose UI (tracer) | `./gradlew testDebugUnitTest --tests '*ProviderKeyCardTest' detekt` | ✅ green |
| 15-01-02 | 01 | 1 | VI18N-01, VI18N-02 | Compose UI | `./gradlew testDebugUnitTest --tests '*ModelSelectCardTest' --tests '*ClarificationBarTest' --tests '*ProviderKeyCardTest' detekt` | ✅ green |
| 15-01-03 | 01 | 1 | VI18N-03 | Compose UI + full-suite wave gate | `./gradlew testDebugUnitTest detekt` | ✅ green |
| 15-02-01 | 02 | 2 | VI18N-04 | JVM + Compose UI (tracer) | `./gradlew testDebugUnitTest --tests '*VoiceModelLabelDefaultsTest' --tests '*OutcomeSheetTest' detekt` | ✅ green |
| 15-02-02 | 02 | 2 | VI18N-04 | JVM + Compose UI + full-suite wave gate | `./gradlew testDebugUnitTest detekt` | ✅ green |
| 15-03-01 | 03 | 3 | VI18N-04 | JVM + Compose UI (tracer) | `./gradlew testDebugUnitTest --tests '*VoiceModelLabelDefaultsTest' --tests '*OutcomeSheetTest' detekt` | ✅ green |
| 15-03-02 | 03 | 3 | VI18N-04 | JVM + Compose UI | `./gradlew testDebugUnitTest --tests '*VoiceModelLabelDefaultsTest' --tests '*OutcomeSheetTest' detekt` | ✅ green |
| 15-03-03 | 03 | 3 | VI18N-01..04 (SC5, D-03, INV-01) | compile-only fixture + closing gate | `./gradlew testDebugUnitTest detekt`; apiCheck against released v2.4.1 `api.txt`; removed-line allowlist; untouched-path + import checks (see 15-03-PLAN.md Task 3) | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] Override tests (non-English sentinels) in the five existing component test classes — VI18N-01..04
- [x] Model default + old-arity `copy` equivalence test in `src/test/.../model/` — VI18N-04
- [x] Optional compile-only source-compat fixture modeled on `ShowTagColorsSourceCompatTest`

---

## Manual-Only Verifications

All phase behaviors have automated verification. (Gate-2 spot check, non-blocking, deferred to the owner via
`.planning/uat-pending/15-voice-surface-i18n-label-params.md`: real long localized strings do not truncate in the
dropdown labels, segmented toggle, or sheet rows. This is a visual judgement, not an automatable gap.)

---

## Validation Audit 2026-10-05

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

All four requirements (VI18N-01..04) are COVERED by green automated tests: `ProviderKeyCardTest`,
`ModelSelectCardTest`, `ClarificationBarTest`, `ApproachLadderCardTest`, `OutcomeSheetTest`
(override + English-default pins), `VoiceModelLabelDefaultsTest` (model defaults, old-arity `copy`,
drift guard), and `VoiceI18nSourceCompatTest` (v2.4.0 call shapes). Full `testDebugUnitTest detekt apiCheck`
green on HEAD after the code-review fixes (689 tests, 0 failures); `15-VERIFICATION.md` passed 5/5 and
`15-03-SELF-UAT.md` (Gate-1) is all_pass. No test generation was required.

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer.

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 120s
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant: true` — gap analysis found zero automatable gaps

**Approval:** verified 2026-10-05 — finalized by the execute-phase Nyquist finalizer (auto mode)
