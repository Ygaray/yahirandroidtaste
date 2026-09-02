---
phase: 05-gardening-unify-coordinated-repin
plan: 01
subsystem: ui
tags: [compose, design-system, chips, metalava, api-break, component-registry]

# Dependency graph
requires:
  - phase: 02-coherence-audit
    provides: "docs/COHERENCE-AUDIT.md's Unify Work-Order — WO-1's exact fold shape, blast radius, and disposition"
provides:
  - "ChipBar's expandable mode (ExpandableConfig? param + rawContent freeform slot) folding in FilterBar's chrome"
  - "FilterBar retired entirely — deleted source file, registry entry, and gallery demo"
  - "api.txt/API.md rebaselined for the intentional WO-1 break; apiCheck green"
  - "Proven fold -> registry -> apiDump -> apiCheck -> lane-gated-commit mechanic for 05-02-PLAN.md (WO-2) to reuse"
affects: [05-02-gardening-unify-coordinated-repin, 05-03-gardening-unify-coordinated-repin]

# Actuals (#2632)
actuals:
  tokens: 7921
  tasks: 2
  commits: 1

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Trailing, nullable, defaulted param idiom for an opt-in composable mode (expandable: ExpandableConfig? = null, rawContent: (@Composable FlowRowScope.() -> Unit)? = null) — every existing call site keeps compiling unchanged"
    - "apiDump line-by-line diff review before an intentional public-API-breaking commit, gated behind HUB_LANE_OVERRIDE"

key-files:
  created: []
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ChipBar.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/FilterBar.kt (deleted)
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt
    - api.txt
    - API.md

key-decisions:
  - "Task 1's source edits and Task 2's api.txt/API.md rebaseline landed as ONE combined commit (not two atomic commits), per the plan's own explicit design — Task 2's <action> stages Task 1's files too, and its acceptance criteria require all 5 files in the same `git show --stat HEAD`. This avoids ever committing a state where the source already breaks the API but api.txt/apiCheck haven't caught up (dangerous on a library with a Metalava freeze gate)."
  - "Committed via HUB_LANE_OVERRIDE=2, not the plan-specified HUB_LANE_OVERRIDE=3 — the live pre-commit classifier returned LANE 2, not LANE 3, because of the pre-existing verify-api-additive.sh absolute-vs-relative path bug (tracked in STATE.md Blockers/Concerns and this plan's own threat register T-05-04) that silently no-ops the API-break detector; only the source-side classifier fired. Used the override value the tool's live output actually required."
  - "Used ChipBar's typed items/key/itemContent mode (not the new rawContent freeform mode) for the gallery's new expandable-mode demo, so no TagChipUiModel import needed to come back after the plan's instructed import removal — type is inferred from ExplorerFakeData.tagChips/manyTagChips's own List<TagChipUiModel> declared type."
  - "Also updated API.md's 'Surface at a glance' Chips row count (5 -> 4) alongside the Chips table edit the plan asked for, since leaving it stale would be a direct, obvious inconsistency introduced by this task's own row removal (left the pre-existing, unrelated 51/56 grand-total staleness untouched — that's a separate, larger doc-audit issue out of this task's scope)."

patterns-established:
  - "Fold-then-retire unify shape: add a nullable config param to the surviving component covering the retired sibling's exclusive behavior, add a freeform-content escape hatch for the retired sibling's slot-based callers, delete the retired sibling outright (no compat shim, per UI-SPEC's locked default), then rebaseline registry + api.txt in the same commit."

requirements-completed: [GARD-01]

coverage:
  - id: D1
    description: "ChipBar carries FilterBar's expand/collapse chrome as an opt-in `expandable` mode; every existing bare-mode call site is unaffected"
    requirement: "GARD-01"
    verification:
      - kind: unit
        ref: "ComponentRegistryDriftGuardTest, ComponentRegistryTierTest, ComponentRegistrySearchTest"
        status: pass
      - kind: unit
        ref: "testDebugUnitTest (full suite)"
        status: pass
    human_judgment: false
  - id: D2
    description: "FilterBar retired: FilterBar.kt deleted, its registry Entry and demo removed, ComponentRegistry's registered-XOR-allowlisted invariant holds"
    requirement: "GARD-01"
    verification:
      - kind: unit
        ref: "ComponentRegistryDriftGuardTest"
        status: pass
    human_judgment: false
  - id: D3
    description: "api.txt rebaselined and apiCheck passes clean; API.md documents the unified shape with no stale FilterBar mention"
    requirement: "GARD-01"
    verification:
      - kind: other
        ref: "./gradlew apiCheck"
        status: pass
      - kind: other
        ref: "grep -c 'FilterBar' API.md == 0; grep -c 'ExpandableConfig' api.txt >= 1"
        status: pass
    human_judgment: false

duration: 20min
completed: 2026-09-02
status: complete
---

# Phase 5 Plan 01: Fold FilterBar into ChipBar's expandable mode (WO-1) Summary

**Folded `FilterBar`'s expand/collapse chrome into `ChipBar` as a new opt-in `expandable: ExpandableConfig?` mode, deleted `FilterBar.kt` outright, and rebaselined `api.txt`/`API.md`/the registry for the intentional break — proving the fold -> registry -> apiDump -> apiCheck -> lane-gated-commit mechanic 05-02-PLAN.md (WO-2) reuses.**

## Performance

- **Duration:** ~20 min
- **Started:** 2026-09-02 (session start)
- **Completed:** 2026-09-02T04:11:00Z
- **Tasks:** 2/2 completed
- **Files modified:** 5 (1 deleted)

## Accomplishments

- `ChipBar<T>` gained two new trailing, nullable, defaulted params — `expandable: ExpandableConfig? = null` and `rawContent: (@Composable FlowRowScope.() -> Unit)? = null` — that fold in `FilterBar`'s tonal `Surface` + chevron `IconButton` + single-line-clip-collapsed/height-capped-scroll-expanded chrome as an opt-in mode, while every existing bare-mode call site (`expandable == null`) renders byte-identical to before.
- `FilterBar.kt` deleted entirely (no source-compat shim, per UI-SPEC's locked default — this is a hub-only break already gated behind the human-gated `v2.0.0` coordinated repin in `05-03-PLAN.md`). Its registry `Entry` and `FilterBarVariants()` demo removed from `ChipsFamilyScreen.kt`; `ChipBarVariants()` gained a new collapsed + expanded expandable-mode demo pair so the fold-in behavior stays showcased in the gallery.
- `api.txt` rebaselined via `./gradlew apiDump` and reviewed line-by-line: `FilterBarKt` class removed, `ChipBarKt.ChipBar`'s method line gained exactly the two new optional trailing params and nothing else changed, and a new public `ExpandableConfig` data-class entry appears (intentional — it's part of `ChipBar`'s own public signature now). `API.md`'s Chips table updated to match (row removed, `ChipBar`'s description extended, family count corrected 5 -> 4).
- `./gradlew testDebugUnitTest detekt apiCheck` all pass clean against the rebaseline (zero detekt findings, zero-baseline policy preserved).

## Task Commits

Task 1's source edits and Task 2's rebaseline landed together in a single commit, per the plan's own explicit design (see Key Decisions above — Task 2's action stages Task 1's files too, and its acceptance criteria require all 5 files in one `git show --stat HEAD`).

1. **Task 1 (source fold) + Task 2 (rebaseline + commit)** - `a966282` (feat) - `HUB_LANE_OVERRIDE=2` (see Key Decisions — live classifier lane, not the plan-assumed lane 3)

**Plan metadata:** pending final metadata commit (STATE.md/ROADMAP.md updates owned by the orchestrator, not this plan).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking issue] Pre-commit lane classifier returned LANE 2, not the plan-specified LANE 3**
- **Found during:** Task 2's commit step
- **Issue:** The plan's `<action>` instructed `HUB_LANE_OVERRIDE=3 git commit ...`, but `tools/classify-hub-change.sh` returned `LANE 2 (mode=additive, baseline=v1.10.0)` against the staged diff, and the pre-commit hook only accepts an override matching the lane it actually computed. Root cause: `verify-api-additive.sh`'s known, pre-existing absolute-vs-relative path bug (documented in `.planning/STATE.md` Blockers/Concerns and this plan's own threat register, T-05-04, disposition "accept, out of scope") silently no-ops the API-break detector, so only the source-side classifier (`verify-additive-diff.sh`, correctly flagging that pre-existing `ChipBar.kt` lines changed) fired — landing on lane 2 instead of lane 3.
- **Fix:** Committed with `HUB_LANE_OVERRIDE=2` (the value the live classifier actually required) instead of the plan's `=3`. Same coordination-gate mechanism, same deliberate-non-additive-change intent — just the override value matching the tool's real output rather than the plan's session-time assumption.
- **Files modified:** None (commit-flow only, no source change).
- **Commit:** `a966282`

Also see Key Decisions above for two additional non-bug deviations from the plan's literal wording (combined-commit sequencing, typed-mode gallery demo instead of rawContent) — both were plan-consistent implementation choices, not bugs, so not listed here as auto-fixes.

## Self-Check: PASSED

- FOUND: `src/main/java/io/github/ygaray/yahirandroidtaste/component/ChipBar.kt` (modified, contains `ExpandableConfig` + `expandable`/`rawContent` params)
- FOUND: `src/main/java/io/github/ygaray/yahirandroidtaste/component/FilterBar.kt` deleted (confirmed absent)
- FOUND: `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt` (modified, zero `FilterBar` references)
- FOUND: `api.txt` rebaselined, `ExpandableConfig` present, `apiCheck` passes
- FOUND: `API.md` Chips table updated, zero `FilterBar` references
- FOUND commit `a966282` in `git log --oneline --all`
