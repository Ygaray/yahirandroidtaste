---
phase: "28"
slug: "talkback-sweep-explorer-demos"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-17"
---

# Phase 28 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.
> Source of truth for the test map: `28-RESEARCH.md` § Validation Architecture.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 + Robolectric 4.16.1 + `createComposeRule` (ui-test 1.11.0) |
| **Config file** | `build.gradle.kts`; detekt `config/detekt/detekt.yml` + `config/detekt-compose.yml` (zero baseline) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfig*' --tests '*PlaceMapTilePurge*' --tests '*DemoRegistryRender*' --tests '*ReceiptDemosRegistryRenderTest' --tests '*ExplorerSchemePinGuardTest' --no-daemon` |
| **Full suite command** | `./gradlew testDebugUnitTest detekt apiCheck --rerun --no-build-cache --no-daemon`, then `git diff --stat api.txt` (empty), `bash tools/verify-api-source-additive.sh v2.7.1`, `bash tools/test/run-all.sh` |
| **Estimated runtime** | quick ~60-120 s; full ~400 s |

Rules: one Gradle invocation at a time, always `--no-daemon`, never `--stop`, never `--offline`.

---

## Sampling Rate

- **After every task commit:** the narrowest `--tests` set for the touched area
- **After every plan wave:** guard run (`*CatalogDocsDriftGuardTest`, `*DomainVocabularyDriftGuardTest`, `*ComponentRegistryDriftGuardTest`, `*ComponentStatesMatrixTest`, `*ComponentPlaygroundIntegrityTest`, `*LibraryInvariantsGuardTest`) plus quick run
- **Before `/gsd-verify-work`:** full suite fresh must be green
- **Max feedback latency:** ~420 seconds

---

## Per-Task Verification Map

Filled by the planner/executor per task; the requirement-to-test map is in `28-RESEARCH.md` § Validation Architecture (POL-03, DEMO-02, DEMO-03, A11Y-02 rows) and in each PLAN.md `<verify>` block.

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 28-xx-xx | see PLAN.md | — | A11Y-02, DEMO-02, DEMO-03, POL-03 | — | N/A | unit / Robolectric / device | see PLAN.md | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `explorer/ExplorerTheme.kt` helper + `ExplorerSchemePinGuardTest` — DEMO-02
- [ ] `FeedbackStackDemoRegistryRenderTest`, `GoToItemDemosRegistryRenderTest`, `HoistedSheetDemoRegistryRenderTest` — DEMO-02, DEMO-03
- [ ] `A11ySweepSemanticsTest` (incl. the missing `Heading` assertion) — A11Y-02
- [ ] `PlaceMapTilePurgeTest` + real placeholder PNG fixture (6987 B, md5 `c069a15b2cc2d6b6f527ad09eb93c61a`) — POL-03
- [ ] Widen `LibraryInvariantsGuardTest` demo-copy scan if demo copy leaves `ReleaseDemos.kt`

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Spoken TalkBack read-out, reading feel, double Dismiss listing | A11Y-02 | Speech output cannot be asserted from adb | Parked in `.planning/uat-pending/` / `HUMAN-UAT-PENDING.md` for Yahir; never waived. The agentic Gate-1 covers the structural layer on the TESTER (`R5CT10XNKQN`) |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 420s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time

**Approval:** pending — finalizer-owned, not set at plan time
