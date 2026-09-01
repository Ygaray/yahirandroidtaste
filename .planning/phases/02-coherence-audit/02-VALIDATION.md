---
phase: 02
slug: coherence-audit
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-09-01
---

# Phase 02 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | None — plain-text structural verification (`grep` against the produced `.md`); this is a documentation-only phase with zero Kotlin/Compose code changes, so no unit-test framework applies |
| **Config file** | none |
| **Quick run command** | `grep -c "^### " docs/COHERENCE-AUDIT.md` (expect >= 9 — one heading per family, plus any cross-family findings section) |
| **Full suite command** | manual read-through of `docs/COHERENCE-AUDIT.md` against the 4 success criteria in `ROADMAP.md` §Phase 2 |
| **Estimated runtime** | ~seconds (grep) / minutes (manual read) |

---

## Sampling Rate

- **After every task commit:** re-read the family section just written against its source
  family-screen file's entry list — confirm no entry/tier was silently dropped or
  mis-transcribed (tier is consumed from Phase 1's ratified labels, never re-derived).
- **After every plan wave:** re-run the family-enumeration grep below; if the phase spans
  multiple plans, confirm no family section was duplicated or overwritten across waves.
- **Before `/gsd-verify-work`:** all three structural checks in the requirements map below must
  pass green, and the "Unify Work-Order" section must exist and be non-empty (or explicitly
  state "no unify findings").
- **Max feedback latency:** ~5 seconds (grep-based).

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | AUD-01 | — / N/A | N/A (docs-only, no attack surface) | structural | `for f in Cards Chips Sheets "Buttons / FAB" Pickers Feedback "Empty State" "Progress / Metrics" "Tactile Foundation"; do grep -q "$f" docs/COHERENCE-AUDIT.md \|\| echo "MISSING: $f"; done` | ❌ W0 | ⬜ pending |
| 02-01-02 | 01 | 1 | AUD-01 | — / N/A | N/A | structural | `grep -c "unify\|keep-with-rationale\|prune" docs/COHERENCE-AUDIT.md` (cross-check count matches number of findings raised) | ❌ W0 | ⬜ pending |
| 02-01-03 | 01 | 1 | AUD-01 | — / N/A | N/A | manual | N/A — human/planner read of the final "Unify Work-Order" section, confirming each entry carries pre-computed consumer call-site counts (D-02) | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

*Exact task IDs above are placeholders pending the planner's actual task breakdown — the planner
must map its real task IDs onto these three checks (or split/merge as the plan's shape requires).*

---

## Wave 0 Requirements

- [ ] `docs/COHERENCE-AUDIT.md` does not exist yet — created by this phase's own execution; no
  scaffolding needed beyond the file itself.
- No test framework install needed — plain Markdown + `grep` verification only.

*No pytest/jest-equivalent gaps: this phase produces no executable code.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| "unify" dispositions form a concrete, actionable Phase 5 work-order | AUD-01 (success criterion 4) | Whether the work-order is genuinely *actionable* (each item has enough detail — component names, families, pre-computed blast radius) for Phase 5 to execute against is a judgment call, not a structural grep | Read the final "Unify Work-Order" section of `docs/COHERENCE-AUDIT.md`; confirm every "unify" finding elsewhere in the doc has a corresponding entry there, and that each entry lists the components to unify + their consumer call-site counts (SecondBrain / CalTracker_Android) per D-02 |

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
- [ ] Feedback latency < 5s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time; the
      finalizer sets `true` iff its gap analysis finds zero gaps

**Approval:** pending — finalizer-owned, not set at plan time
