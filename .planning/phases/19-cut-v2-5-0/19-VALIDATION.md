---
phase: "19"
slug: "cut-v2-5-0"
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-10-05"
---

# Phase 19 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Gradle `testDebugUnitTest` (JUnit4 + Robolectric + Compose UI test) for the library; bash shell tests under `tools/test/` for tooling |
| **Config file** | `build.gradle.kts`; detekt `config/detekt/detekt.yml` (zero baseline); shell tests need no config |
| **Quick run command** | `bash tools/test/run-all.sh` |
| **Full suite command** | `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck metalavaCheckCompatibilityDebug publishReleasePublicationToMavenLocal --no-build-cache && bash tools/test/run-all.sh && tools/verify-binary-abi.sh v2.4.1` |
| **Estimated runtime** | quick ~2 s; full suite several minutes |

---

## Sampling Rate

- **After every task commit:** Run `bash tools/test/run-all.sh` (plus the new or edited test file)
- **After every plan wave:** `bash tools/test/run-all.sh` and `SKIP_BUILD=1 tools/verify-binary-abi.sh v2.4.1`
- **Before `/gsd-verify-work`:** Full suite green on the exact tagged HEAD (`GATED_HEAD`)
- **Max feedback latency:** 5 s for shell tests

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 19-01-xx | 01 | 1 | D-03 | T-19-01 | ABI gate exits 3 on any missing public descriptor, 2 on sanity-floor failure | shell fixture | `bash tools/test/test-verify-binary-abi.sh` | ✅ | ✅ green |
| 19-01-xx | 01 | 1 | D-03 | T-19-01 | Real v2.4.1 vs HEAD ABI diff, missing=0 | integration | `tools/verify-binary-abi.sh v2.4.1` | ✅ | ✅ green |
| 19-01-xx | 01 | 1 | D-03 | - | Raw-line api.txt check fully unwired; hook still blocks lane 2 | shell | `bash tools/test/run-all.sh` | ✅ (rewrite) | ✅ green |
| 19-02-xx | 02 | 2 | SHIP-03 | T-19-02 | Battery green on `GATED_HEAD`; tag by SHA; no stray tag | Gradle + git | full suite command; `git rev-parse v2.5.0^{commit}` equals `GATED_HEAD` | ✅ | ✅ green |
| 19-02-xx | 02 | 2 | SHIP-03 | T-19-02 | JitPack resolves v2.5.0 (status ok, isTag true, commit match) | network | JitPack poll | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `tools/test/test-verify-binary-abi.sh` - fixtures for D-03 script behavior
- [x] `tools/verify-binary-abi.sh` - unit under test (created RED-test-first)
- [x] Rewrite api cases in `tools/test/test-classify-hub-change.sh` and `tools/test/test-precommit-hook.sh`

---

## Manual-Only Verifications

All phase behaviors have automated verification. (Orchestrator ledger-row relay is a message, not a test.)

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 5s (shell tests)
- [x] `nyquist_compliant: true` - finalized post-execution

**Approval:** validated 2026-10-05

---

## Validation Audit 2026-10-05

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

Evidence: `bash tools/test/run-all.sh` green (classify 12/12, hook 6/6, additive-diff 5/5, binary-abi 52/52); GATED_HEAD battery 751 tests 0 failures; ABI gate missing=0 vs v2.4.1; tag v2.5.0 peeled SHA == GATED_HEAD; JitPack pom/aar 200, status ok, commit match (see 19-SHIP-GATE-EVIDENCE.md, 19-VERIFICATION.md).
