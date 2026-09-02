---
phase: 02-coherence-audit
plan: 01
subsystem: docs
tags: [compose, design-system, coherence-audit, component-registry]

# Dependency graph
requires:
  - phase: 01-tier-legibility
    provides: Ratified per-entry `ComponentRegistry.Entry.tier` values (all 53 entries) and `docs/DESIGN-INTENT.md`'s D-03 litmus, consumed verbatim (D-01) rather than re-derived.
provides:
  - "docs/COHERENCE-AUDIT.md with the full 10-heading skeleton (9 families + Unify Work-Order)"
  - "Cards (11 entries), Chips (5 entries), Sheets (18 entries), Tactile Foundation (4 entries) fully enumerated and dispositioned — 38 of 53 registry entries"
  - "2 unify findings with D-02 blast-radius counts (ChipBar/FilterBar; TextCardBottomSheet/ListCardBottomSheet), ready for 02-02-PLAN.md's Unify Work-Order aggregation"
affects: [02-02-PLAN.md, phase-05-gardening]

# Actuals (#2632)
actuals:
  tokens: 5300
  tasks: 3
  commits: 3

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Coherence-audit disposition pattern: every overlap/near-duplicate-sibling/altitude finding cites the component's real signature/body (never assumed from name/family/tier match alone, per RESEARCH.md Pitfall 3) and carries an explicit unify/keep-with-rationale/prune disposition."
    - "D-02 blast-radius grep recipe: `grep -rl \"<ComponentName>\" ~/Projects/SecondBrain/app/src ~/Projects/CalTracker_Android/app/src`, recorded as a per-repo file count for every unify finding, with a consumer-pin-skew note when CalTracker's count is zero."

key-files:
  created:
    - docs/COHERENCE-AUDIT.md
  modified: []

key-decisions:
  - "ChipBar vs FilterBar (Chips) dispositioned unify — FilterBar's expand/collapse chrome recommended folded into ChipBar as an optional mode, retiring FilterBar as a standalone PRIMITIVE. Blast radius: SecondBrain 5/5 files, CalTracker 0/0 (consistent with post-v1.5.0 additions, not rejected usage)."
  - "TextCardBottomSheet vs ListCardBottomSheet (Sheets) dispositioned unify — confirmed substantial verbatim duplication (identical header row, three-dot menu, rename dialog) beyond the already-shared CardQuickView body; recommended extracting the shared chrome into a new composable mirroring CardQuickView's own D-04 precedent. Blast radius: SecondBrain 2/2 files, CalTracker 0/0."
  - "TextCard/ListCard/AlbumCard/VoiceCard (Cards) 4-way CardBase-sharing group dispositioned keep-with-rationale — the shared CardBase shell is correct, intentional reuse (not duplication); each sibling's body content is genuinely distinct per the D-03 domain-noun test."
  - "CardQuickView (Cards), AppChip/TagChipWithContextMenu (Chips), the 3 Sheet+Content shell/body pairs (Sheets), and GradientSwatch/HeatSwatch (Tactile Foundation) all dispositioned keep-with-rationale after reading actual signatures/bodies — each is either a confirmed decorator/wrapper relationship or a genuinely different purpose despite surface-level naming/family/tier similarity."

requirements-completed: []
# AUD-01 is partially fulfilled by this plan (Cards/Chips/Sheets/Tactile Foundation — 38 of 53
# entries dispositioned). Full completion is deferred to 02-02-PLAN.md, which dispositions the
# remaining 5 families (Buttons/FAB, Pickers, Feedback, Empty State, Progress/Metrics) and
# assembles the Unify Work-Order. Left empty here rather than marked complete to avoid a
# premature signal; 02-02's own SUMMARY should carry `requirements-completed: [AUD-01]`.

coverage:
  - id: D1
    description: "docs/COHERENCE-AUDIT.md skeleton: intro + Scope & Method (D-01 tier-source, D-02 blast-radius, consumer pin-skew note) + exactly 10 `### ` headings (9 families + Unify Work-Order) in ExplorerFamilies.ORDERED_KEYS order, each carrying the tracer placeholder before content tasks ran."
    requirement: AUD-01
    verification:
      - kind: other
        ref: "structural grep: per-family `grep -q '^### <name>$'` loop (prints nothing) + `grep -c '^### '` == 10 + `grep -c 'PENDING - filled by a later task'` == 10, all run and confirmed after Task 1"
        status: pass
    human_judgment: false
  - id: D2
    description: "Cards family (11 entries) enumerated with name+tier verbatim from CardsFamilyScreen.kt. TextCard/ListCard/AlbumCard/VoiceCard 4-way near-duplicate-sibling shape and CardQuickView (evaluated separately, confirmed it does NOT compose CardBase) both dispositioned keep-with-rationale with cited signatures."
    requirement: AUD-01
    verification:
      - kind: other
        ref: "task done-criteria grep: `grep -q CardQuickView docs/COHERENCE-AUDIT.md`"
        status: pass
    human_judgment: true
    rationale: "Disposition correctness (keep-with-rationale vs. unify) is an editorial/architectural judgment call on real trade-offs — structural grep can confirm the finding was written up and cited, but not that the disposition itself is the right call. A human/reviewer should confirm the rationale holds before Phase 5 acts on it."
  - id: D3
    description: "Chips family (5 entries) enumerated. ChipBar vs FilterBar dispositioned unify with D-02 blast-radius counts (SecondBrain 5/5, CalTracker 0/0). AppChip vs TagChipWithContextMenu dispositioned keep-with-rationale after confirming a decorator/wrapper relationship (not duplicate implementations)."
    requirement: AUD-01
    verification:
      - kind: other
        ref: "task done-criteria grep: `grep -q TagChipWithContextMenu docs/COHERENCE-AUDIT.md`"
        status: pass
    human_judgment: true
    rationale: "Same as D2 — the ChipBar/FilterBar unify recommendation and its proposed merged shape are architectural judgment calls a human should confirm before Phase 5 scopes the actual code change."
  - id: D4
    description: "Sheets family (18 entries) enumerated. TextCardBottomSheet vs ListCardBottomSheet dispositioned unify with blast-radius counts (SecondBrain 2/2, CalTracker 0/0). The 3 Sheet+Content shell/body pairs (TagPickerSheet, BulkCreatePopup, TagCreateSheet) confirmed via actual signatures as the intentional precedent and dispositioned keep-with-rationale. The remaining 10 non-paired entries were each checked and explicitly stated to carry no further finding."
    requirement: AUD-01
    verification:
      - kind: other
        ref: "task done-criteria grep: `grep -q TagPickerSheetContent docs/COHERENCE-AUDIT.md`"
        status: pass
    human_judgment: true
    rationale: "The TextCardBottomSheet/ListCardBottomSheet unify finding and its recommended extraction shape are architectural judgment calls a human should confirm; the 'no finding' calls on the 10 remaining entries are also editorial judgments, not mechanically verifiable."
  - id: D5
    description: "Tactile Foundation family (4 entries) enumerated. GradientSwatch vs HeatSwatch dispositioned keep-with-rationale after confirming genuinely different purposes via their actual signatures (caller-supplied generic accent ramp vs. hardcoded mindmap-specific visual). ElevationLadder/TactileTypeShowcase checked, no finding."
    requirement: AUD-01
    verification:
      - kind: other
        ref: "task done-criteria grep: `grep -q GradientSwatch docs/COHERENCE-AUDIT.md && grep -q HeatSwatch docs/COHERENCE-AUDIT.md`"
        status: pass
    human_judgment: true
    rationale: "Same as D2 — disposition correctness is an editorial judgment a human should confirm."

duration: 6min
completed: 2026-09-01
status: complete
---

# Phase 2 Plan 01: Coherence Audit (Cards/Chips/Sheets/Tactile Foundation) Summary

**`docs/COHERENCE-AUDIT.md` created with its full 9-family + Unify-Work-Order skeleton, then
dispositioned 38 of 53 registry entries across Cards, Chips, Sheets, and Tactile Foundation —
surfacing 2 real unify findings (ChipBar/FilterBar, TextCardBottomSheet/ListCardBottomSheet)
with D-02 blast-radius counts, alongside 6 keep-with-rationale findings confirmed by reading
actual component signatures rather than assumed from naming/family/tier match.**

## Performance

- **Duration:** ~6 min (per git commit timestamps; base commit 17:56:25 → last task commit
  18:01:47 local time)
- **Started:** 2026-09-01T17:56:25-06:00
- **Completed:** 2026-09-01T18:01:47-06:00
- **Tasks:** 3
- **Files modified:** 1 (`docs/COHERENCE-AUDIT.md`, created)

## Accomplishments

- Created `docs/COHERENCE-AUDIT.md` with the full 10-heading skeleton (9 families in
  `ExplorerFamilies.ORDERED_KEYS` order + Unify Work-Order), structurally verified before any
  content was added (tracer task).
- Fully enumerated and dispositioned Cards (11 entries), Chips (5 entries), Sheets (18 entries),
  and Tactile Foundation (4 entries) — 38 of the 53 total registered components.
- Surfaced 2 genuine "unify" findings, each with D-02 read-only blast-radius grep counts against
  both on-disk consumer repos:
  - **`ChipBar` vs `FilterBar`** (Chips) — fold `FilterBar`'s expand/collapse chrome into `ChipBar`
    as an optional mode. SecondBrain 5/5 files, CalTracker 0/0.
  - **`TextCardBottomSheet` vs `ListCardBottomSheet`** (Sheets) — extract the shared header-row +
    three-dot-menu + rename-dialog chrome into a new composable, mirroring `CardQuickView`'s own
    D-04 precedent for the body region. SecondBrain 2/2 files, CalTracker 0/0.
- Confirmed and dispositioned 6 "keep-with-rationale" findings by reading actual component
  signatures/bodies (never assumed from naming/family/tier match alone, per RESEARCH.md
  Pitfall 3): the `TextCard`/`ListCard`/`AlbumCard`/`VoiceCard` `CardBase`-sharing group,
  `CardQuickView` (confirmed it does NOT compose `CardBase`), `AppChip`/`TagChipWithContextMenu`
  (confirmed decorator/wrapper relationship), the 3 Sheet+Content shell/body pairs
  (`TagPickerSheet`, `BulkCreatePopup`, `TagCreateSheet`), and `GradientSwatch`/`HeatSwatch`.
- Explicitly stated "no finding" for entries checked and found clean rather than leaving them
  unaddressed: the remaining 10 non-paired Sheets entries and `ElevationLadder`/
  `TactileTypeShowcase` in Tactile Foundation.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create docs/COHERENCE-AUDIT.md skeleton** - `6ce389d` (docs)
2. **Task 2: Audit Cards + Chips families** - `6f8e4bd` (docs)
3. **Task 3: Audit Sheets + Tactile Foundation families** - `2c71ce2` (docs)

**Plan metadata:** committed as part of this SUMMARY.md commit (worktree mode — orchestrator
handles the shared-file metadata commit after wave merge).

## Files Created/Modified

- `docs/COHERENCE-AUDIT.md` - The coherence audit deliverable: intro, Scope & Method, and the
  fully dispositioned Cards/Chips/Sheets/Tactile Foundation sections (remaining 6 sections still
  carry the tracer placeholder for 02-02-PLAN.md).

## Decisions Made

See `key-decisions` in frontmatter above — 2 unify findings (`ChipBar`/`FilterBar`,
`TextCardBottomSheet`/`ListCardBottomSheet`) and their rationale, plus the keep-with-rationale
dispositions for the remaining findings.

## Deviations from Plan

None — plan executed exactly as written. All three tasks' `<verify>`/`<done>` criteria were run
and confirmed before each commit (structural heading grep after Task 1; per-task pending-count
and required-string greps after Tasks 2 and 3).

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required (docs-only phase, no code/dependency changes).

## Next Phase Readiness

- `docs/COHERENCE-AUDIT.md` is ready for 02-02-PLAN.md (wave 2, same file) to disposition the
  remaining 5 families (Buttons / FAB, Pickers, Feedback, Empty State, Progress / Metrics) and
  assemble the final "Unify Work-Order" section from all unify findings across both plans
  (currently 2: `ChipBar`/`FilterBar` and `TextCardBottomSheet`/`ListCardBottomSheet`).
- No blockers. AUD-01 remains partially complete until 02-02-PLAN.md dispositions the remaining
  families and assembles the Unify Work-Order.

---
*Phase: 02-coherence-audit*
*Completed: 2026-09-01*
