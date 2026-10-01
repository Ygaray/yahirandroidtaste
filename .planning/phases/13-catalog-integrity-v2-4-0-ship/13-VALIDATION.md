---
phase: "13"
slug: "catalog-integrity-v2-4-0-ship"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-09-30"
---

# Phase 13 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 (plain, no `@RunWith`) for the 4 drift-guard tests; Robolectric + Compose UI test for per-composable tests (already present) |
| **Config file** | `build.gradle.kts` (root-as-module, single-module hub) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*ComponentRegistry*" --tests "*DomainVocabularyDriftGuardTest*" --tests "*GeneratedSymbolDriftGuardTest*"` |
| **Full suite command** | `./gradlew testDebugUnitTest && ./gradlew detekt && ./gradlew apiCheck` |
| **Estimated runtime** | ~90 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew testDebugUnitTest --tests "*ComponentRegistry*"`
- **After every plan wave:** Run `./gradlew testDebugUnitTest && ./gradlew detekt && ./gradlew apiCheck`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 90 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 13-01-01 | 01 | 1 | CAT-01 | — | N/A | unit (source-text scan) | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*"` | ✅ already exists | ✅ green |
| 13-01-02 | 01 | 1 | API-01 | — | N/A | build-tool (Metalava semantic diff) | v2.3.0 `api.txt` swap-baseline + `./gradlew apiCheck` | ✅ task exists | ✅ green |
| 13-01-03 | 01 | 1 | INV-01 | T-13-01 | No forbidden dependency; data/callback-only params re-confirmed | manual grep + diff | `git diff f10b560^..HEAD -- build.gradle.kts` + grep for OkHttp/Retrofit/engine imports | ✅ commands exist | ✅ green |
| 13-02-01 | 02 | 1 | CAT-01 (doc-drift) | — | N/A | doc assertion | `grep -rn "seven" CLAUDE.md README.md API.md src/main/**/ComponentRegistry.kt` (expect zero matches post-fix) | ✅ files exist | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. Research (13-RESEARCH.md) verified CAT-01 and INV-01 are already satisfied at current HEAD via live tool runs; API-01's apparent false-positive from the naive `tools/classify-hub-change.sh` line-diff was reproduced and resolved via the authoritative Metalava `apiCheck` swap-baseline technique. No new test authoring is required — this phase is evidence-capture + doc correction.

---

## Manual-Only Verifications

All phase behaviors have automated verification. The only manual step is the human-readable doc-text correction (seven→ten family count + full family roster), which is verified by grep assertions above, not a runtime behavior.

---

## Validation Audit 2026-09-30

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

All 4 per-task automated commands were independently re-run green (not trusted from the plan-time draft alone) across this phase's execution chain: `13-01-SUMMARY.md` (executor), `13-SHIP-GATE-EVIDENCE.md` (plan Task 1 transcript), `13-REVIEW.md`/`13-REVIEW-FIX.md` (code-review + fix pass), `13-VERIFICATION.md` (gsd-verifier, 4/4 must-haves, independent re-run), `13-01-SELF-UAT.md` (Gate-1 agentic tester, all_pass, independent re-run), and a final `./gradlew testDebugUnitTest` full-suite re-run by the execute-phase orchestrator immediately before `verify_phase_goal` — all BUILD SUCCESSFUL. Zero MISSING/PARTIAL requirements. `nyquist_compliant: true` per the zero-gaps short-circuit (validate-phase.md §3).

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 90s
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant` — set `true`; gap analysis found zero gaps

**Approval:** verified 2026-09-30 — finalized by execute-phase's `finalize_nyquist_gate` (--auto)
