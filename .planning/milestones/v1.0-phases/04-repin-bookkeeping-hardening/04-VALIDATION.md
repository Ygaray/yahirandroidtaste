---
phase: 04
slug: repin-bookkeeping-hardening
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: 2026-09-01
---

# Phase 04 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | None — this phase makes no Kotlin/Compose source changes. Validation is functional/CLI verification of `repin_status.py` against the live `ECOSYSTEM.md`, plus the repo's pre-commit lane classifier and the `incident` skill lifecycle. |
| **Config file** | none — existing tooling covers it |
| **Quick run command** | `python3 ~/.claude/context/deps/repin_status.py validate --hub yahirandroidtaste` |
| **Full suite command** | `python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste` (run twice — second run must report "no drift", proving idempotency) |
| **Estimated runtime** | ~5 seconds (network round-trip to GitHub, 1h-cached) |

---

## Sampling Rate

- **After every task commit:** Run `python3 ~/.claude/context/deps/repin_status.py validate --hub yahirandroidtaste`
- **After every plan wave:** Run the reconcile ×2 idempotency sequence above
- **Before `/gsd-verify-work`:** `reconcile` must report `no drift` (or a real write followed by a `no drift` re-run) and `validate` must report matrix-matches-truth
- **Max feedback latency:** ~10 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 04-01-01 | 01 | 1 | REPIN-01 | T-04-01 (accept) | Markers exist, seeded values match derived truth | functional/CLI | `python3 ~/.claude/context/deps/repin_status.py validate --hub yahirandroidtaste` | ✅ (tool pre-exists) | ✅ green |
| 04-01-02 | 01 | 1 | REPIN-01 | T-04-01 (accept) | `reconcile` exits 0, idempotent on second run | functional/CLI | `python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste` (×2) | ✅ (tool pre-exists) | ✅ green |
| 04-01-03 | 01 | 1 | REPIN-01 | — / N/A | Commit lands without triggering the lane-gate override | functional/CLI | `git add ECOSYSTEM.md && API_FILE=$(git rev-parse --show-toplevel)/api.txt bash tools/classify-hub-change.sh --baseline $(git describe --tags --abbrev=0 --match 'v*')` | ✅ (tool pre-exists) | ✅ green |
| 04-01-04 | 01 | 1 | REPIN-01 (D-02) | — / N/A | `INC-2026-08-28-03` closed with evidence 1:1-mapped to its acceptance text | process | `incident` skill `diagnose`/`fix`/`verify`/`close --resolution fixed` against the control-plane repo | ✅ (skill pre-exists) | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements — `repin_status.py`, `tools/classify-hub-change.sh`, and the `incident` skill already exist and are unchanged by this phase. No new test scaffolding needed.

---

## Manual-Only Verifications

All phase behaviors have automated verification.

---

## Validation Audit 2026-09-02

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

All 4 tasks' automated commands were independently re-run and confirmed green post-execution (not
merely trusted from SUMMARY.md):
- `validate` / `reconcile` (×3, exceeding the required ×2) against the committed `ECOSYSTEM.md` —
  exit 0, `no drift`, no `ValueError` each time (re-run by the phase-goal verifier, 04-VERIFICATION.md).
- `tools/classify-hub-change.sh --baseline v1.10.0` dry-run — `LANE 1`, exit 0; the real `git commit`
  (`bfec0c9`) also classified Lane 1 with no `HUB_LANE_OVERRIDE`.
- `INC-2026-08-28-03` frontmatter independently re-read: `status: closed`, `resolution: fixed`,
  `verified_by` set — control-plane commit `4cd1e86`.
- Cross-phase regression: `tools/test/run-all.sh` (all 4 shell fixture suites, PASS=4/7/5/5 FAIL=0 —
  identical to the Phase 3 baseline) and `./gradlew testDebugUnitTest detekt` (BUILD SUCCESSFUL) both
  re-run clean, confirming this phase's pre-commit/classifier-adjacent change introduced no regression.

Zero gaps — every task in the Per-Task Verification Map has a passing automated command. Per Step 3
of the validate-phase workflow, this phase is Nyquist-compliant.

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references (none — existing infra covers everything)
- [x] No watch-mode flags
- [x] Feedback latency < 10s
- [x] `nyquist_compliant: true` — finalizer's gap analysis found zero gaps

**Approval:** approved 2026-09-02
