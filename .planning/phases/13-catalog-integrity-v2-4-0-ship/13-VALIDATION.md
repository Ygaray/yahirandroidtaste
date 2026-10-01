---
phase: "13"
slug: "catalog-integrity-v2-4-0-ship"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
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
| 13-01-01 | 01 | 1 | CAT-01 | — | N/A | unit (source-text scan) | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*"` | ✅ already exists | ⬜ pending |
| 13-01-02 | 01 | 1 | API-01 | — | N/A | build-tool (Metalava semantic diff) | v2.3.0 `api.txt` swap-baseline + `./gradlew apiCheck` | ✅ task exists | ⬜ pending |
| 13-01-03 | 01 | 1 | INV-01 | T-13-01 | No forbidden dependency; data/callback-only params re-confirmed | manual grep + diff | `git diff f10b560^..HEAD -- build.gradle.kts` + grep for OkHttp/Retrofit/engine imports | ✅ commands exist | ⬜ pending |
| 13-02-01 | 02 | 1 | CAT-01 (doc-drift) | — | N/A | doc assertion | `grep -rn "seven" CLAUDE.md README.md API.md src/main/**/ComponentRegistry.kt` (expect zero matches post-fix) | ✅ files exist | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. Research (13-RESEARCH.md) verified CAT-01 and INV-01 are already satisfied at current HEAD via live tool runs; API-01's apparent false-positive from the naive `tools/classify-hub-change.sh` line-diff was reproduced and resolved via the authoritative Metalava `apiCheck` swap-baseline technique. No new test authoring is required — this phase is evidence-capture + doc correction.

---

## Manual-Only Verifications

All phase behaviors have automated verification. The only manual step is the human-readable doc-text correction (seven→ten family count + full family roster), which is verified by grep assertions above, not a runtime behavior.

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
- [ ] Feedback latency < 90s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time; the
      finalizer sets `true` iff its gap analysis finds zero gaps

**Approval:** pending — finalizer-owned, not set at plan time
