---
phase: 01-tier-legibility
plan: 01
subsystem: ui
tags: [kotlin, jetpack-compose, component-registry, design-system, api-surface]

# Dependency graph
requires: []
provides:
  - "ComponentRegistry.Tier nested enum (PRIMITIVE, PATTERN)"
  - "ComponentRegistry.Entry.tier required field (last constructor param, no default)"
  - "All 11 Cards-family entries tiered per the D-03 litmus"
  - "All 5 Chips-family entries tiered per the D-03 litmus"
  - "Two locked worked examples verified: CardBase=PATTERN, ChipBar=PRIMITIVE"
affects: [01-02, 01-03, 01-04, 01-05]

# Actuals (#2632)
actuals:
  tokens: 2392
  tasks: 2
  commits: 2

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Entry.tier: required, no-default enum field on a data class instantiated at 53 call sites across 9 files in one compilation unit — forces every remaining family to be tiered before the module compiles again."
    - "D-03 litmus applied per-entry: domain noun in name+params OR baked-in interaction/composition convention => PATTERN; neither => PRIMITIVE."

key-files:
  created: []
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt

key-decisions:
  - "Entry.tier added as the LAST constructor parameter, required, no default — per locked decision D-01, no parallel Map<String, Tier> was introduced (entries alone stays the single source of truth)."
  - "This plan's own build is NOT expected to compile end-to-end: 7 of 9 family files still lack tier= on their Entry(...) call sites. Full green compile is deferred to 01-05-PLAN.md (Wave 3), which is the first point all 53 sites exist together — a hard compiler constraint of a required field on a class used module-wide, not an oversight."
  - "Both task commits required the hub's HUB_LANE_OVERRIDE=2 pre-commit override — the classifier correctly flags a new required field on a public data class as a non-additive (lane 2) API change. This is the intentional, already-locked D-01 tradeoff (reversibility rating: costly), not a misfire."

patterns-established:
  - "D-03 two-question litmus: domain noun in name+params OR baked-in interaction/composition convention => PATTERN; neither => PRIMITIVE. Applied verbatim per-entry in this plan and carried forward to the remaining 7 families."

requirements-completed: [LEG-01]

coverage:
  - id: D1
    description: "ComponentRegistry.Tier enum (PRIMITIVE, PATTERN) added as a nested type in ComponentRegistry.kt, mirroring the existing StateCell nesting precedent"
    requirement: "LEG-01"
    verification:
      - kind: other
        ref: "grep -c 'enum class Tier' ComponentRegistry.kt == 1"
        status: pass
    human_judgment: false
  - id: D2
    description: "Entry.tier required, no-default field appended as the last constructor parameter of Entry"
    requirement: "LEG-01"
    verification:
      - kind: other
        ref: "grep -c 'val tier: Tier$' ComponentRegistry.kt == 1"
        status: pass
    human_judgment: false
  - id: D3
    description: "All 11 Cards-family Entry(...) call sites carry an explicit tier value per the D-03 litmus; CardBase=PATTERN and CountBadge is the sole Cards PRIMITIVE"
    requirement: "LEG-01"
    verification:
      - kind: other
        ref: "grep -c 'tier = ComponentRegistry\\.Tier\\.' CardsFamilyScreen.kt == 11; CardBase region contains Tier.PATTERN; PRIMITIVE count == 1"
        status: pass
    human_judgment: false
  - id: D4
    description: "All 5 Chips-family Entry(...) call sites carry an explicit tier value per the D-03 litmus; ChipBar=PRIMITIVE and AppChip/ChipBar/FilterBar are the 3 Chips PRIMITIVEs"
    requirement: "LEG-01"
    verification:
      - kind: other
        ref: "grep -c 'tier = ComponentRegistry\\.Tier\\.' ChipsFamilyScreen.kt == 5; ChipBar region contains Tier.PRIMITIVE; PRIMITIVE count == 3"
        status: pass
    human_judgment: false

duration: 12min
completed: 2026-09-01
status: complete
---

# Phase 01 Plan 01: Tier Enum + Cards/Chips Tiering Summary

**Added `ComponentRegistry.Tier` (PRIMITIVE/PATTERN) and the required `Entry.tier` field, then tiered all 16 Cards + Chips entries per the D-03 litmus, proving the mechanism on both locked worked examples (CardBase=PATTERN, ChipBar=PRIMITIVE).**

## Performance

- **Duration:** 12 min
- **Started:** 2026-09-01T22:21:00Z
- **Completed:** 2026-09-01T22:33:50Z
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments
- `ComponentRegistry.Tier` nested enum (`PRIMITIVE`, `PATTERN`) added immediately before `Entry`, mirroring the `StateCell` nesting precedent
- `Entry.tier: Tier` added as the required, no-default, last constructor parameter — per D-01, no parallel map was introduced
- All 11 Cards-family entries tiered (10 PATTERN, 1 PRIMITIVE — `CountBadge`)
- All 5 Chips-family entries tiered (2 PATTERN, 3 PRIMITIVE — `AppChip`, `ChipBar`, `FilterBar`)
- Both locked worked examples verified correct: `CardBase` = PATTERN (bakes in the reveal-confirm destructive-swipe convention via `SwipeableActionRow`), `ChipBar` = PRIMITIVE (fully generic `<T>`, zero domain nouns, renders only caller-passed content)

## Task Commits

Each task was committed atomically:

1. **Task 1: Add Tier enum + Entry.tier field; tier all 11 Cards-family entries** - `7eadad7` (feat)
2. **Task 2: Tier all 5 Chips-family entries** - `668e287` (feat)

**Plan metadata:** committed as part of the SUMMARY.md commit (worktree mode — orchestrator handles final metadata commit after merge)

## Files Created/Modified
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` - Added `Tier` enum + `Entry.tier` required field
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt` - Added `tier = ...` to all 11 Cards `Entry(...)` call sites
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt` - Added `tier = ...` to all 5 Chips `Entry(...)` call sites

## Decisions Made
- `Entry.tier` placed as the LAST constructor parameter, required, no default — matches D-01 exactly and forces a compile error at any of the 53 call sites that omits it, which is the intended tracer mechanism this plan proves.
- No parallel `Map<String, Tier>` was added — `entries` alone remains the registry's single source of truth (existing invariant, `ComponentRegistry.kt:9-10`).
- Tier assignments applied exactly the plan's pre-computed table (D-03 litmus already applied by the planner) with no deviation — every assignment double-checked against its component's real signature during editing.

## Deviations from Plan

None — plan executed exactly as written. One process note (not a deviation from plan content):

### Process Note: Lane-2 pre-commit override

Both task commits triggered the hub's `classify-hub-change.sh` pre-commit guard, which correctly classified the change as lane 2 (non-additive API surface change) — adding a required field to a public `data class` is exactly what that guard exists to catch. Per the plan's own `<reversibility rating="costly">` annotation on Task 1 (D-01, already a locked human decision — "the tier field lands in the published api.txt; removing/redefaulting it later is an API break requiring a Metalava rebaseline"), this is the correct, expected classification, not a guard misfire. Committed via the sanctioned `HUB_LANE_OVERRIDE=2` bypass documented in root `CLAUDE.md`.

## Issues Encountered
None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `ComponentRegistry.Tier` and the required `Entry.tier` field now exist; the tiering mechanism (read signature -> apply D-03 litmus -> assign) is proven on both locked worked examples.
- 16 of 53 total registry entries are now tiered (Cards + Chips). The remaining 37 entries across 7 families (Sheets, Buttons/FAB, Pickers, Feedback, Empty State, Progress, Tactile Foundation) land in `01-03-PLAN.md` and `01-04-PLAN.md` (Wave 2).
- **Known, documented limitation carried forward by design:** the module does NOT compile end-to-end yet — 7 families still lack `tier =` at their `Entry(...)` call sites, which is a hard compiler consequence of a required field on a class instantiated module-wide (not a defect in this plan). Full `./gradlew compileDebugKotlin` green build is deferred to `01-05-PLAN.md` (Wave 3), the first point all 53 sites exist together.
- No blockers for Wave 2 plans — they can proceed independently against the now-existing `ComponentRegistry.Tier` enum and `Entry.tier` field.

---
*Phase: 01-tier-legibility*
*Completed: 2026-09-01*

## Self-Check: PASSED

- FOUND: .planning/phases/01-tier-legibility/01-01-SUMMARY.md
- FOUND: 7eadad7 (Task 1 commit)
- FOUND: 668e287 (Task 2 commit)
