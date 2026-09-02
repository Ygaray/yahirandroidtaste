---
phase: 01-tier-legibility
plan: 02
subsystem: docs
tags: [design-system, tiering, documentation, litmus]

# Dependency graph
requires: []
provides:
  - "docs/DESIGN-INTENT.md — primitives contract, patterns contract, and decidable D-03 litmus (LEG-02)"
affects: [01-01, 01-03, 01-04, 01-05, 02-coherence-audit, 03-tier-aware-contribution-litmus]

actuals:
  tokens: 1010
  tasks: 2
  commits: 1

tech-stack:
  added: []
  patterns:
    - "Two-yes/no-question litmus (domain-noun name/param test + caller-content-only-render test) as the decidable tier-assignment test, replacing adjective-based judgment."

key-files:
  created:
    - docs/DESIGN-INTENT.md
  modified: []

key-decisions:
  - "Wrote docs/DESIGN-INTENT.md verbatim per the plan's exact prescribed content — no paraphrasing, since the litmus text must stay byte-identical to what 01-01/01-03/01-04 apply to their per-entry tier assignments (must not diverge, per must_haves.key_links)."
  - "Bypassed the hub's lane-2 additive-guard pre-commit hook via the documented HUB_LANE_OVERRIDE=2 convention — the lane-2 flag traced to a pre-existing, already-committed comment reword in HeatSwatch.kt (commit 5b01532, landed on main before this plan started, unrelated to this docs-only change)."

patterns-established:
  - "Design-intent doc distinct from ComponentRegistry.kt (what exists) and API.md (public surface) — states what the hub means to be, per tier."

requirements-completed: [LEG-02]

coverage:
  - id: D1
    description: "docs/DESIGN-INTENT.md states the primitives contract, the patterns contract, and the decidable two-question D-03 litmus as independently-readable, unconditional sections."
    requirement: "LEG-02"
    verification:
      - kind: other
        ref: "grep -c '## The Primitives Contract' / '## The Patterns Contract' / '## The Litmus' docs/DESIGN-INTENT.md each == 1"
        status: pass
    human_judgment: false
  - id: D2
    description: "The litmus is applied to all three named borderline components (CardBase -> PATTERN, ChipBar -> PRIMITIVE, HeatSwatch -> PATTERN), each assigned to exactly one tier."
    requirement: "LEG-02"
    verification:
      - kind: other
        ref: "grep -A2 'CardBase|ChipBar|HeatSwatch' docs/DESIGN-INTENT.md — each names its tier verdict explicitly"
        status: pass
    human_judgment: false
  - id: D3
    description: "No specific downstream consumer app name (SecondBrain, CalTracker) leaks into the consumer-agnostic doc."
    requirement: "LEG-02"
    verification:
      - kind: other
        ref: "grep -Eic 'SecondBrain|CalTracker' docs/DESIGN-INTENT.md == 0"
        status: pass
    human_judgment: false

duration: 12min
completed: 2026-09-01
status: complete
---

# Phase 01 Plan 02: Design Intent Doc Summary

**`docs/DESIGN-INTENT.md` states the primitives contract, the patterns contract, and a decidable two-question D-03 litmus applied to the three named borderline components (CardBase -> PATTERN, ChipBar -> PRIMITIVE, HeatSwatch -> PATTERN).**

## Performance

- **Duration:** 12 min
- **Started:** 2026-09-01T22:xx:xxZ
- **Completed:** 2026-09-01
- **Tasks:** 2
- **Files modified:** 1

## Accomplishments
- Created new top-level `docs/` directory and `docs/DESIGN-INTENT.md`
- Stated the primitives contract (zero domain vocabulary, caller-content-only render, no baked-in interaction convention)
- Stated the patterns contract (may carry domain vocabulary that is the hub's own, bakes in an interaction/composition opinion, never names a consumer's business objects)
- Stated the decidable D-03 litmus (two yes/no questions; either "yes" to Q1 or "no" to Q2 is sufficient to make it a PATTERN)
- Applied the litmus to the three named borderline cases with explicit tier verdicts and rationale tied to each component's actual public signature/behavior

## Task Commits

Each task was committed atomically:

1. **Task 1: Write docs/DESIGN-INTENT.md** - `a6dc9d5` (docs)
2. **Task 2: Verify content completeness and the consumer-agnostic prohibition** - no additional commit (verification-only; the doc as written in Task 1 already satisfied every check — zero consumer-name matches, both `## Worked Examples` and `## Applying the Litmus` sections present exactly once)

**Plan metadata:** pending (this SUMMARY commit)

## Files Created/Modified
- `docs/DESIGN-INTENT.md` - New design-intent doc: primitives contract, patterns contract, decidable D-03 litmus, worked examples for CardBase/ChipBar/HeatSwatch, and an "Applying the Litmus" authoring-guidance section

## Decisions Made
- Followed the plan's prescribed content verbatim (byte-for-byte, modulo the plan's own `---` markdown delimiters which are plan-doc boundaries, not part of the file) to guarantee the litmus text stays identical to what sibling plans 01-01/01-03/01-04 apply — this was an explicit `key_links` requirement, not a stylistic choice.
- Used `HUB_LANE_OVERRIDE=2` to land the doc-only commit past the hub's additive-guard pre-commit hook. Root-caused the lane-2 flag to a pre-existing rewritten comment line in `HeatSwatch.kt` (already committed on `main` via commit `5b01532`, predating this plan's branch point) — confirmed via `git log --oneline v1.10.0..934e45b0 -- '*.kt'` and by running `tools/verify-additive-diff.sh v1.10.0` directly, which reported the rewritten `HeatSwatch.kt` comment lines as the sole cause. This docs-only commit adds no source lines and could not itself have caused the flag.

## Deviations from Plan

None - plan executed exactly as written. (The pre-commit lane-2 override above is process, not a deviation from the plan's content requirements — the doc content matches the plan's prescribed text exactly.)

## Issues Encountered

**Pre-existing hub additive-guard false-positive.** The repo's `tools/hooks/pre-commit` lane-classifies every commit against the `v1.10.0` API baseline tag. Committing this purely-additive, doc-only file tripped a lane-2 (non-additive) block. Investigation traced the cause to an already-landed, pre-existing comment reword in `HeatSwatch.kt` (commit `5b01532`, on `main` before this plan's worktree branched) — a rewritten line since the baseline tag, unrelated to this task's diff. Resolved via the documented `HUB_LANE_OVERRIDE=2` bypass (see `keep-global-claude-md-slim`-adjacent memory `hub-additive-guard-blocks-planning-docs`), with the root cause and evidence recorded in the commit message. No code was changed to "fix" this — it is out of this task's scope (a different file, already committed on `main`, not part of `01-02-PLAN.md`'s `files_modified`).

## Next Phase Readiness
- `docs/DESIGN-INTENT.md` is committed and ready for `01-03-PLAN.md`/`01-04-PLAN.md`/`01-05-PLAN.md` (and future Phase 2/3 work) to reference as the canonical statement of tier intent.
- No blockers. The pre-existing lane-2 false-positive on `HeatSwatch.kt`'s comment reword (unrelated to this plan) may resurface on any future commit that touches unrelated files until the baseline tag advances or the comment-reword is otherwise reconciled — worth a note for whichever plan next commits near `HeatSwatch.kt`.

---
*Phase: 01-tier-legibility*
*Completed: 2026-09-01*

## Self-Check: PASSED

- FOUND: docs/DESIGN-INTENT.md
- FOUND: a6dc9d5 (Task 1 commit)
