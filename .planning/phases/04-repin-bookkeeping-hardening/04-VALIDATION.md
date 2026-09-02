---
phase: 04
slug: repin-bookkeeping-hardening
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-09-01
---

# Phase 04 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | None — this phase makes no Kotlin/Compose source changes. Validation is functional/CLI verification of `repin_status.py` against the live `ECOSYSTEM.md`, plus the repo's pre-commit lane classifier. |
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
| 04-01-01 | 01 | 1 | REPIN-01 | — / N/A | Markers exist, seeded values match derived truth | functional/CLI | `python3 ~/.claude/context/deps/repin_status.py validate --hub yahirandroidtaste` | ✅ (tool pre-exists) | ⬜ pending |
| 04-01-02 | 01 | 1 | REPIN-01 | — / N/A | `reconcile` exits 0, idempotent on second run | functional/CLI | `python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste` (×2) | ✅ (tool pre-exists) | ⬜ pending |
| 04-01-03 | 01 | 1 | REPIN-01 | — / N/A | Commit lands without triggering the lane-gate override | functional/CLI | `git add ECOSYSTEM.md && API_FILE=$(git rev-parse --show-toplevel)/api.txt bash tools/classify-hub-change.sh --baseline $(git describe --tags --abbrev=0 --match 'v*')` | ✅ (tool pre-exists) | ⬜ pending |
| 04-01-04 | 01 | 1 | REPIN-01 (D-02) | — / N/A | `INC-2026-08-28-03` closed with evidence 1:1-mapped to its acceptance text | process | `incident` skill `diagnose`/`fix`/`verify`/`close --resolution fixed` against the control-plane repo | ✅ (skill pre-exists) | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements — `repin_status.py`, `tools/classify-hub-change.sh`, and the `incident` skill already exist and are unchanged by this phase. No new test scaffolding needed.

---

## Manual-Only Verifications

All phase behaviors have automated verification.

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
- [ ] Feedback latency < 10s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time; the
      finalizer sets `true` iff its gap analysis finds zero gaps

**Approval:** pending
