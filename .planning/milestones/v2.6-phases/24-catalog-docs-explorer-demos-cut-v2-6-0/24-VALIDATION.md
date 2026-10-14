---
phase: "24"
slug: "catalog-docs-explorer-demos-cut-v2-6-0"
# status lifecycle: draft (seeded by plan-phase) -> validated (set by validate-phase §6)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-10-12"
---

# Phase 24 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric 4.16.1 + Compose ui-test (`createComposeRule`), `@Config(sdk = [35])`; shell fixtures under `tools/test/` |
| **Config file** | `build.gradle.kts` (`isIncludeAndroidResources = true`), `tools/test/run-all.sh` |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "*V251CallSiteCanaryTest" --tests "*CatalogDocsDriftGuardTest" --tests "*LibraryInvariantsGuardTest" --tests "*ReceiptRowRegistryRenderTest" --tests "*ReceiptDemosRegistryRenderTest" --tests "*ProviderKeyLifecycleDemoRegistryRenderTest" --tests "*UndoGroupDemoRegistryRenderTest"` |
| **Full suite command** | `./gradlew apiDump cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck` then `bash tools/test/run-all.sh` and `bash tools/verify-api-source-additive.sh v2.5.1` |
| **Estimated runtime** | ~300 seconds |

One gradle invocation at a time. No pixel capture under Robolectric: semantics, bounds and source-contract proofs; on-device look is Gate-1 on the TESTER.

---

## Sampling Rate

- **After every task commit:** the task's `--tests` filters in ONE gradle invocation (`apiDump` first when public API changed), then `apiCheck` and `detekt` when `src/main` changed
- **After every plan wave:** `./gradlew apiDump testDebugUnitTest detekt apiCheck`
- **Before `/gsd-verify-work`:** full unfiltered suite + detekt + apiCheck green + `tools/test/run-all.sh`
- **Max feedback latency:** 600 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 24-01-01 | 01 | 1 | DEMO-01, CAT-03 | T-24-04 | ReceiptRow Default gallery cell measures 92dp +/- 1 (failing-first recorded at 90.0dp) | Robolectric bounds + registry render | `./gradlew testDebugUnitTest --tests "*ReceiptRowRegistryRenderTest"` | yes | green |
| 24-01-02 | 01 | 1 | DEMO-01, CAT-03, INV-03 | T-24-01, T-24-05, T-24-06 | key-lifecycle and Undo-group demos reached through the registry; raw typed key absent from every semantics root (positive control); DE labels in window at 360dp | Robolectric tree scan + NATIVE geometry | `./gradlew testDebugUnitTest --tests "*ProviderKeyLifecycleDemoRegistryRenderTest" --tests "*UndoGroupDemoRegistryRenderTest"` | yes | green |
| 24-01-03 | 01 | 1 | DEMO-01, CAT-03 | T-24-03, T-24-05 | Receipt confirm/success/failure demos and count editor reached by the registry under the hub scheme | Robolectric registry render | `./gradlew testDebugUnitTest --tests "*ReceiptDemosRegistryRenderTest"` | yes | green |
| 24-02-01 | 02 | 2 | API-03 | T-24-08 | verbatim v2.5.1 call shapes for the nine D-01 surfaces compile and render (canary; pristine v2.5.1 archive pass recorded) | compile canary + Robolectric | `./gradlew testDebugUnitTest --tests "*V251CallSiteCanaryTest"` | yes | green |
| 24-02-02 | 02 | 2 | API-03, INV-03 | T-24-09, T-24-12 | api source-additivity tool (R1-R8, ACCEPTED allowlist) with permanent negative controls; no Hilt host / entry point / consumer import | shell fixtures (34 cases) + Robolectric source contract | `bash tools/test/run-all.sh` and `./gradlew testDebugUnitTest --tests "*LibraryInvariantsGuardTest"` | yes | green |
| 24-02-03 | 02 | 2 | DOC-03, CAT-03 | T-24-10, T-24-11 | API.md / INTEGRATION.md / README counts and Stable test tags equal the live registry and source | Robolectric source-contract drift guard | `./gradlew testDebugUnitTest --tests "*CatalogDocsDriftGuardTest" --tests "*ComponentRegistryDriftGuardTest"` | yes | green |
| 24-03-01 | 03 | 3 | API-03, INV-03 | T-24-17 | fresh uncached battery on the HEAD under test (1315 tests, 0 failures), AAR md5 recorded | Gradle + git audits | `./gradlew apiDump cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --rerun-tasks` | n/a (gate) | green |
| 24-03-02 | 03 | 3 | DEMO-01 | T-24-13, T-24-15, T-24-16 | every demo driven on the TESTER, 33 screencaps, SELF-UAT all_pass | uat-run (Gate-1) | `gsd-tools uat-run verify --run <id>` and `uat-run verify --self-uat 24-03-SELF-UAT.md` | yes | green |
| 24-03-03 | 03 | 3 | DEMO-01 | T-24-14, T-24-19 | SELF-UAT frontmatter is the tool output; evidence staged by explicit paths | uat-run verify --self-uat | `gsd-tools uat-run verify --self-uat .planning/phases/24-.../24-03-SELF-UAT.md` | yes | green |
| 24-04-01 | 04 | 4 | API-03, INV-03, DOC-03 | T-24-20 | fresh D-02 battery on GATED_HEAD 78d89cf (1317 tests, 0 failures; detekt re-run 0 findings; additivity OK; ABI informational) | Gradle + shell | `./gradlew apiDump apiCheck cleanTestDebugUnitTest testDebugUnitTest detekt --rerun-tasks` + `bash tools/test/run-all.sh` | n/a (gate) | green |
| 24-04-02 | 04 | 4 | SHIP-04 | T-24-21..T-24-25 | annotated tag by SHA, range/author/path/secret checks, exact push forms | git audit | `git ls-remote origin 'refs/tags/v2.6*'` (peeled commit == GATED_HEAD) | n/a (gate) | green |
| 24-04-03 | 04 | 4 | SHIP-04 | T-24-26, T-24-27 | JitPack builds API status ok, commit == tagged commit, aar/pom/module HTTP 200 | HTTP probe | `curl -s https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v2.6.0` and `curl -sI .../v2.6.0/yahirandroidtaste-v2.6.0.{aar,pom,module}` | n/a (gate) | green |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [x] Phase 20-23 test support (unmerged-tree dumper, raw-key scan, bounds helper) reused; new tests only append to `src/test` (protected tests byte-identical to cffad79 and v2.5.1)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Owner review of the Receipt look, count editor, key lifecycle and Undo Center group row (screencaps under `screencaps/`); hub-palette vs dynamic-colour decision | DEMO-01 | Human taste; Gate-2 after the cut (objection ships as v2.6.1) | `.planning/uat-pending/24-catalog-docs-explorer-demos-cut-v2-6-0.md` |
| TalkBack read-out of the saved-key summary and the Undo Center group header | DEMO-01 | uiautomator cannot observe TalkBack read-out (physical-only) | Gate-2 how-to-verify item 4 |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 600s
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant`

**Approval:** validated 2026-10-12

---

## Validation Audit 2026-10-12

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

Every requirement (CAT-03, API-03, DOC-03, INV-03, DEMO-01, SHIP-04) maps to an automated command that ran green: the fresh
unfiltered suite on the tagged commit is 1317 tests, 22 pre-existing skips, 0 failures/errors, detekt and apiCheck green,
`tools/test/run-all.sh` green. SHIP-04 is evidenced by git ls-remote and JitPack HTTP probes recorded in 24-04-GATE-RECORD.md
and 24-04-SUMMARY.md. The human-only items above are registered Gate-2 obligations, not automatable gaps.
