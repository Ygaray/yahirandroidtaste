---
phase: 06-forward-port-reunification
plan: 02
subsystem: ui
tags: [android, jetpack-compose, kotlin, component-registry]

# Dependency graph
requires: ["06-01"]
provides:
  - "PresetChip.kt (both public overloads) forward-ported from v1.13.0 onto main, registered in ComponentRegistry (tier = PATTERN), gallery-navigable in the Chips family"
  - "PresetChip.kt now exists on disk at component/PresetChip.kt — unblocks Plan 06-03's PlaceMapPicker restore (same-package ChipBar itemContent call site)"
  - "\"Preset\" allowlisted in DomainVocabularyDriftGuardTest.PRIMITIVE_NOUN_ALLOWLIST"
affects: [06-03-forward-port-reunification]

# Actuals (#2632)
actuals:
  tokens: 4200
  tasks: 2
  commits: 2
plan_head_before: cbb7220b1853b894f38f582b0f1225c001b1dcb5

# Tech tracking
tech-stack:
  added: []
  patterns: ["restore-from-tag via targeted `git checkout v1.13.0 -- <path>` (never whole-file, when the destination file has independently diverged)", "hand-insert new ComponentRegistry.Entry(...) blocks into a diverged family-screen file, matching in-file precedent"]

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/PresetChip.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/PresetChipTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt

key-decisions:
  - "Restored PresetChip.kt + PresetChipTest.kt byte-faithful via targeted `git checkout v1.13.0 -- <path>` (not whole-file) — no hand-editing of ported logic; both overloads (7-arg required-contentDescription, 6-arg convenience delegate) confirmed present via grep count."
  - "Inserted the new ComponentRegistry.Entry(\"PresetChip\", ...) by hand into main's current ChipsFamilyScreen.kt (not restored from v1.13.0), preserving main's own independently-evolved entries (AppChip, TagChipWithContextMenu, ChipBar, SortControl) and their tier/controls/preview enrichments — matching RESEARCH.md Pitfall 2 guidance and the pattern established in Plan 06-01."
  - "PresetChipVariants() demo restored verbatim from v1.13.0, placed between ChipBarVariants() and SortControlVariants() to match v1.13.0's original entry ordering (AppChip, TagChipWithContextMenu, ChipBar, PresetChip, SortControl)."

patterns-established: []

requirements-completed: [REUNI-03, REUNI-04]

coverage:
  - id: D1
    description: "PresetChip (both overloads) forward-ported from v1.13.0 with its test, registered in ComponentRegistry (tier = PATTERN), and shown in the Chips family gallery with both restored demo variants (label-only, with supporting label) inside ChipBar"
    requirement: "REUNI-03"
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/component/PresetChipTest.kt (./gradlew testDebugUnitTest --tests \"*PresetChipTest*\")"
        status: pass
      - kind: unit
        ref: "ComponentRegistryDriftGuardTest (./gradlew testDebugUnitTest --tests \"*ComponentRegistryDriftGuardTest*\")"
        status: pass
    human_judgment: true
    rationale: "Gallery navigation/rendering (ExplorerActivity -> Chips family -> PresetChip detail page showing both ChipBar-wrapped demo variants) is a device-only-verifiable UI behavior — routes to Gate-1 agentic/human UAT, not self-certified here."
  - id: D2
    description: "\"Preset\" head token allowlisted so both drift guards stay green with PresetChip registered"
    requirement: "REUNI-04"
    verification:
      - kind: unit
        ref: "DomainVocabularyDriftGuardTest (./gradlew testDebugUnitTest --tests \"*DomainVocabularyDriftGuardTest*\")"
        status: pass
      - kind: integration
        ref: "./gradlew build -x detekt -x metalavaCheckCompatibilityDebug -x metalavaCheckCompatibilityRelease (assemble+test+lint+check, excluding two pre-existing unrelated failures)"
        status: pass
    human_judgment: false

duration: 18min
completed: 2026-09-27
status: complete
---

# Phase 6 Plan 2: PresetChip Forward-Port Summary

**Forward-ported `PresetChip` (2-overload public API) byte-faithful from v1.13.0 onto `main` (tier=PATTERN, gallery-navigable in the Chips family), unblocking Plan 06-03's `PlaceMapPicker` compile.**

## Performance

- **Duration:** ~18 min
- **Started:** 2026-09-27T06:52:00Z (approx)
- **Completed:** 2026-09-27T07:10:00Z (approx)
- **Tasks:** 2
- **Files modified:** 4 (2 created, 2 modified)

## Accomplishments
- `PresetChip.kt` + `PresetChipTest.kt` restored byte-for-byte from tag `v1.13.0`; both public overloads confirmed present (`grep -n "^fun PresetChip("` → 2 lines), compiling clean against `main`'s current `theme/Dimens` and `ChipBar` (same-package `itemContent` shape).
- `PresetChip` registered in `chipsFamilyEntries` (`tier = PATTERN`), with its own 4-cell States matrix (Default, Pressed/Selected, Disabled, Focused N/A) and both v1.13.0 demo variants (label-only 7-item `ChipBar<String>`, with-supporting-label 2-item `ChipBar<Pair<String,String>>`) restored verbatim inside the new private `PresetChipVariants()` function.
- `"Preset"` allowlisted in `DomainVocabularyDriftGuardTest.PRIMITIVE_NOUN_ALLOWLIST` as a primitive UI-archetype noun.
- `component/PresetChip.kt` now exists on disk on `main` — Plan 06-03's `PlaceMapPicker.kt` restore can proceed (its saved-places section calls `PresetChip(...)` directly as a same-package `ChipBar` `itemContent`).

## Task Commits

Each task was committed atomically:

1. **Task 1: Restore PresetChip.kt (both overloads) + PresetChipTest.kt from v1.13.0** - `adfa6fc` (feat)
2. **Task 2: Register PresetChip in ChipsFamilyScreen.kt; allowlist "Preset"** - `69f3b12` (feat)

**Plan metadata:** committed separately after this SUMMARY (docs: complete plan)

## Files Created/Modified
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/PresetChip.kt` - New (restored verbatim from v1.13.0): the two-overload `ChipBar`-`itemContent` preset chip
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/PresetChipTest.kt` - New (restored verbatim): full test suite (9 tests, Robolectric+Compose UI)
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt` - Hand-inserted `PresetChip` import, `Entry(...)` block (positioned between `ChipBar` and `SortControl`, matching v1.13.0 ordering), and `PresetChipVariants()` demo function; all pre-existing entries (`AppChip`, `TagChipWithContextMenu`, `ChipBar`, `SortControl`) untouched
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt` - Added `"Preset"` to `PRIMITIVE_NOUN_ALLOWLIST`

## Decisions Made

- **Restore-not-rewrite discipline:** `PresetChip.kt`/`PresetChipTest.kt` restored via targeted `git checkout v1.13.0 -- <path>` (never whole-file on a diverged file); the registry entry, demo function, and allowlist edit were hand-authored to match main's current, independently-evolved `ChipsFamilyScreen.kt`/`DomainVocabularyDriftGuardTest.kt` shape — per CONTEXT.md D-01 and RESEARCH.md Pitfall 2 (whole-file restore would have clobbered main's own `TagChipWithContextMenu`/`SortControl` entries and their tier/controls/preview enrichments).
- **Entry ordering preserved from v1.13.0:** the new `PresetChip` entry was placed between `ChipBar` and `SortControl` in `chipsFamilyEntries`, matching v1.13.0's own original list order, rather than appended at the end — a cosmetic but deliberate fidelity choice with no functional effect (the gallery's `LazyColumn` iterates `ComponentRegistry.entries.filter { ... }` in list order).
- **No `apiDump`/`apiCheck` run this plan:** consistent with Plan 06-01's precedent (`DateTimePicker` also isn't yet in `api.txt`), the metalava freeze-gate update is deferred — `KI-2026-09-02-01` already blocks `metalavaCheckCompatibilityDebug` for unrelated reasons and is tracked as a Phase 9 (`SHIP-01`) tag-cut blocker, not this plan's scope.

## Deviations from Plan

None. Both tasks executed exactly as planned — no bugs found, no missing functionality, no blocking issues, no architectural questions. The one pre-existing, unrelated build failure encountered (`TextCard.kt`'s detekt `CyclomaticComplexMethod` finding, already logged in Plan 06-01's `deferred-items.md`) was re-confirmed as out-of-scope via the same targeted-exclusion verification Plan 06-01 established, not re-logged as a new deviation.

## Issues Encountered

None. `PresetChipTest` passed on first run; both drift guards passed on first run; no compile errors against `main`'s current `ChipBar`/`theme.Dimens`.

## User Setup Required

None - no external service configuration required. (Device-only Gate-1 verification of `PresetChip`'s gallery rendering — per this plan's `must_haves.truths` — is deferred to the Gate-1 tester, not self-certified here; see `D1`'s `human_judgment: true` in the coverage block above.)

## Next Phase Readiness

- Plan 06-03 (`PlaceMapPicker` cluster) can now proceed: `component/PresetChip.kt` exists on disk on `main`, so `PlaceMapPicker.kt`'s saved-places section (which calls `PresetChip(...)` directly as a same-package `ChipBar` `itemContent`) will compile.
- The pre-existing, unrelated `TextCard.kt` detekt finding and the already-tracked `KI-2026-09-02-01` metalava issue remain open, tracked outside this plan's scope (per Plan 06-01's SUMMARY) — neither blocks Plan 06-03.
- Device-only Gate-1 verification (ExplorerActivity → Chips family → `PresetChip` renders + navigates + both demo variants display inside `ChipBar`) is required before this phase's completion — not performed here per this executor's device-verification boundary; routes to the Gate-1 agentic/human tester.

---
*Phase: 06-forward-port-reunification*
*Completed: 2026-09-27*

## Self-Check: PASSED

All 4 claimed files verified present on disk (2 created + 2 modified); both task commit hashes
(`adfa6fc`, `69f3b12`) confirmed present in `git log --oneline --all`.
