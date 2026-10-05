---
phase: "19"
slug: "cut-v2-5-0"
status: draft
nyquist_compliant: false
wave_0_complete: false
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
| 19-01-xx | 01 | 1 | D-03 | T-19-01 | ABI gate exits 3 on any missing public descriptor, 2 on sanity-floor failure | shell fixture | `bash tools/test/test-verify-binary-abi.sh` | ❌ W0 | ⬜ pending |
| 19-01-xx | 01 | 1 | D-03 | T-19-01 | Real v2.4.1 vs HEAD ABI diff, missing=0 | integration | `tools/verify-binary-abi.sh v2.4.1` | ❌ W0 | ⬜ pending |
| 19-01-xx | 01 | 1 | D-03 | - | Raw-line api.txt check fully unwired; hook still blocks lane 2 | shell | `bash tools/test/run-all.sh` | ✅ (rewrite) | ⬜ pending |
| 19-02-xx | 02 | 2 | SHIP-03 | T-19-02 | Battery green on `GATED_HEAD`; tag by SHA; no stray tag | Gradle + git | full suite command; `git rev-parse v2.5.0^{commit}` equals `GATED_HEAD` | ✅ | ⬜ pending |
| 19-02-xx | 02 | 2 | SHIP-03 | T-19-02 | JitPack resolves v2.5.0 (status ok, isTag true, commit match) | network | JitPack poll | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `tools/test/test-verify-binary-abi.sh` - fixtures for D-03 script behavior
- [ ] `tools/verify-binary-abi.sh` - unit under test (created RED-test-first)
- [ ] Rewrite api cases in `tools/test/test-classify-hub-change.sh` and `tools/test/test-precommit-hook.sh`

---

## Manual-Only Verifications

All phase behaviors have automated verification. (Orchestrator ledger-row relay is a message, not a test.)

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`; the Nyquist finalizer owns sign-off.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 5s (shell tests)
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` - leave `false` at plan time

**Approval:** pending (finalizer-owned)
