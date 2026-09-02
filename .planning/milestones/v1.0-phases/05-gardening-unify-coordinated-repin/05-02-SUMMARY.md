---
phase: 05-gardening-unify-coordinated-repin
plan: 02
subsystem: ui
tags: [compose, design-system, sheets, metalava, component-registry, source-contract-test]

# Dependency graph
requires:
  - phase: 02-coherence-audit
    provides: "docs/COHERENCE-AUDIT.md's Unify Work-Order — WO-2's exact extraction boundary, locked signature, blast radius, and disposition"
  - phase: 05-gardening-unify-coordinated-repin/05-01
    provides: "Proven fold -> registry -> apiDump -> apiCheck -> lane-gated-commit mechanic (WO-1) reused here for WO-2"
provides:
  - "SheetHeaderMenu internal composable owning the header Row + three-dot DropdownMenu + rename AlertDialog triad, shared by both TextCardBottomSheet and ListCardBottomSheet"
  - "ComponentRegistry.INTENTIONALLY_UNREGISTERED gains a SheetHeaderMenu entry (registered-XOR-allowlisted invariant holds)"
  - "TextListBottomSheetEditMenuSourceContractTest retargeted at the new file boundary — region-marker/branch-logic coverage preserved, not silently dropped"
  - "api.txt confirmed byte-identical (SheetHeaderMenu stayed internal, neither host sheet's public signature changed); API.md documents the new allowlisted sub-part"
affects: [05-03-gardening-unify-coordinated-repin]

# Actuals (#2632)
actuals:
  tokens: 12790
  tasks: 2
  commits: 1

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Extract-shared-sheet-chrome idiom mirroring CardQuickView's D-04 precedent: a standalone internal composable owning exactly the byte-identical triad two near-duplicate sheets shared, taking every varying piece as a parameter, leaving each host sheet's own body/categoryPath untouched"
    - "Source-contract test retarget: when an extraction moves the code a test's plain-text scan asserts against, collapse duplicated per-host-file assertion pairs into single assertions against the new shared file, keeping only the per-file assertions (param declaration, forwarding wiring, unrelated copy) that are genuinely still per-file"

key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/SheetHeaderMenu.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/TextCardBottomSheet.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ListCardBottomSheet.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/component/TextListBottomSheetEditMenuSourceContractTest.kt
    - API.md

key-decisions:
  - "Committed via HUB_LANE_OVERRIDE=2, not the plan-specified HUB_LANE_OVERRIDE=3 — the live pre-commit classifier (`tools/classify-hub-change.sh --baseline v1.10.0`) returned LANE 2 (mode=additive), the same pre-existing verify-api-additive.sh absolute-vs-relative path bug (T-05-04, tracked in STATE.md and this plan's own threat register, disposition accept/out-of-scope) that made 05-01 land on lane 2 as well. Confirmed live before committing (ran the classifier manually with API_FILE exported, matching the pre-commit hook's own invocation) rather than assuming the plan's session-time lane guess."
  - "api.txt required zero rebaseline — `./gradlew apiDump` produced no diff at all, because SheetHeaderMenu stayed `internal` (per UI-SPEC's locked default) and neither host sheet's public signature changed. The plan's acceptance criterion 'git show --stat HEAD includes ... api.txt' assumed a rebaseline diff would exist; since the correct outcome here is a byte-identical api.txt, there was nothing to stage. Verified the zero-diff outcome directly (`git diff api.txt` empty) and via `grep -c 'SheetHeaderMenuKt' api.txt` == 0, rather than treating the absent file from the commit as a gap."
  - "Task 1 and Task 2 landed as ONE combined commit (not two atomic commits), reusing 05-01's own precedent — Task 2's <action> stages Task 1's files too, and its acceptance criteria require all touched files in the same `git show --stat HEAD`. Avoids ever committing a state where the source already changed but the registry/test/api rebaseline hasn't caught up."
  - "Retargeted TextListBottomSheetEditMenuSourceContractTest by collapsing the previously-duplicated per-sheet assertion pairs (Edit-row branching, region-marker copy count, rename-dialog structural presence, onConfirmRename invocation) into single assertions against SheetHeaderMenu.kt, since both sheets now share one implementation — rather than keeping two copies checking the same underlying shared source. Added a new assertion class proving each host sheet still declares its own onEditRequest param AND forwards it into its SheetHeaderMenu(...) call site, so the wiring survival is explicitly checked, not just implied by the shared-file assertions passing."

patterns-established:
  - "Extract-then-delegate-verbatim: when two near-duplicate sheets/components share byte-identical chrome, extract it into one new internal composable taking every varying piece (including internal state that used to be hoisted per-caller) as a parameter, mirroring the codebase's own CardQuickView D-04 precedent rather than inventing a new extraction shape."

requirements-completed: [GARD-01]

coverage:
  - id: D1
    description: "SheetHeaderMenu owns the header/menu/rename triad; both TextCardBottomSheet and ListCardBottomSheet delegate to it and keep their own registered Entry unchanged"
    requirement: "GARD-01"
    verification:
      - kind: unit
        ref: "ComponentRegistryDriftGuardTest"
        status: pass
      - kind: unit
        ref: "testDebugUnitTest (full suite)"
        status: pass
    human_judgment: false
  - id: D2
    description: "TextListBottomSheetEditMenuSourceContractTest passes retargeted at the new file boundary — region-marker and branch-logic coverage not silently dropped"
    requirement: "GARD-01"
    verification:
      - kind: unit
        ref: "TextListBottomSheetEditMenuSourceContractTest"
        status: pass
    human_judgment: false
  - id: D3
    description: "testDebugUnitTest, detekt, and apiCheck all pass clean; api.txt confirmed byte-identical (SheetHeaderMenu stayed internal, neither host sheet's signature changed); API.md documents the new allowlisted sub-part"
    requirement: "GARD-01"
    verification:
      - kind: other
        ref: "./gradlew testDebugUnitTest detekt apiCheck"
        status: pass
      - kind: other
        ref: "git diff api.txt (empty); grep -c 'SheetHeaderMenuKt' api.txt == 0; grep -c 'SheetHeaderMenu' API.md >= 1"
        status: pass
    human_judgment: false

duration: 35min
completed: 2026-09-02
status: complete
---

# Phase 5 Plan 02: Extract shared sheet header/menu/rename composable (WO-2) Summary

**Extracted `SheetHeaderMenu` — the byte-identical header-Row + three-dot-DropdownMenu + rename-AlertDialog triad duplicated across `TextCardBottomSheet`/`ListCardBottomSheet` — into one new internal, allowlisted composable both sheets now delegate to, retargeted the moved source-contract test coverage, and confirmed `api.txt` stayed byte-identical (zero rebaseline needed).**

## Performance

- **Duration:** ~35 min
- **Completed:** 2026-09-02
- **Tasks:** 2/2 completed
- **Files modified:** 6 (1 created: `SheetHeaderMenu.kt`, 241 lines)

## Accomplishments

- `SheetHeaderMenu` (`component/SheetHeaderMenu.kt`, 241 lines, `internal`) owns the header `Row`
  (title + Pin/Favorite indicators + `ImageCountIndicator` + three-dot menu trigger), the
  three-dot `DropdownMenu` (Edit → Pin/Unpin → Favorite/Unfavorite → Delete), and the rename
  `AlertDialog` — moved verbatim from both host sheets, including the load-bearing
  `// region:edit-menu-item` / `// endregion:edit-menu-item` marker comments and the three pieces
  of local state (`showMenu`, `showRenameDialog`, `renameText`) that are no longer hoisted in
  either host sheet.
- `TextCardBottomSheet.kt` shrank from 299 to 140 lines and `ListCardBottomSheet.kt` from 423 to
  275 lines — both now delegate to `SheetHeaderMenu(...)`, forwarding their own
  `onEditRequest`/`imageCount` (Text) or omitting `imageCount` entirely (List, reproducing its
  current no-indicator appearance via `ImageCountIndicator`'s own existing no-op at
  `imageCount <= 0`, not a new conditional branch). Both sheets' own public signatures, their
  `categoryPath` line, `CardQuickView(...)` call, and Edit button are byte-identical to before.
- `ComponentRegistry.INTENTIONALLY_UNREGISTERED` gained a `SheetHeaderMenu` entry mirroring the
  existing `SwipeableActionRow` allowlist shape — the registered-XOR-allowlisted `init{}` invariant
  holds, confirmed by `ComponentRegistryDriftGuardTest` passing both immediately after Task 1's
  source edits and again in the full suite after Task 2.
- `TextListBottomSheetEditMenuSourceContractTest` retargeted: the moved structural assertions
  (Edit-row branching, region-marker Edit/Rename copy count, rename-dialog structural presence,
  `onConfirmRename(` invocation) now check `SheetHeaderMenu.kt` once instead of duplicating the
  same check against both host sheets; a new assertion proves each host sheet still declares its
  own `onEditRequest` param and forwards it into its `SheetHeaderMenu(...)` call site, so wiring
  survival is explicitly checked rather than only implied.
- `./gradlew apiDump` produced **zero diff** — `api.txt` is byte-identical to its pre-extraction
  baseline, confirming `SheetHeaderMenu` stayed `internal` (no leaked public symbol) and neither
  `TextCardBottomSheetKt.TextCardBottomSheet` nor `ListCardBottomSheetKt.ListCardBottomSheet`'s
  public method line changed. `API.md`'s "Intentionally-unregistered sub-parts" section bumped
  5→6 with a new `SheetHeaderMenu` row.
- `./gradlew testDebugUnitTest detekt apiCheck` all pass clean (0 detekt code smells, zero-baseline
  policy preserved).

## Task Commits

Task 1's source edits and Task 2's test retarget + registry/API doc updates landed together in a
single commit, reusing 05-01's own combined-commit precedent (see Key Decisions).

1. **Task 1 (extract SheetHeaderMenu, wire both sheets, registry entry) + Task 2 (retarget test,
   confirm zero-diff api.txt rebaseline, update API.md, commit)** - `dcc367d` (refactor) -
   `HUB_LANE_OVERRIDE=2` (live classifier lane, not the plan-assumed lane 3 — see Deviations)

**Plan metadata:** pending final metadata commit (STATE.md/ROADMAP.md updates owned by the
orchestrator, not this plan).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking issue] Pre-commit lane classifier returned LANE 2, not the plan-specified LANE 3**
- **Found during:** Task 2's commit step
- **Issue:** The plan's `<action>` instructed `HUB_LANE_OVERRIDE=3 git commit ...`, but running
  `tools/classify-hub-change.sh --baseline v1.10.0` against the staged diff (with `API_FILE`
  exported, matching the pre-commit hook's own invocation) returned `LANE 2 (mode=additive,
  baseline=v1.10.0)`. Root cause: the same pre-existing `verify-api-additive.sh`
  absolute-vs-relative path bug that made 05-01 land on lane 2 (documented in `.planning/STATE.md`
  Blockers/Concerns and this plan's own threat register, T-05-04, disposition "accept, out of
  scope") — it silently no-ops the API-break detector, so only the source-side classifier
  (`verify-additive-diff.sh`, correctly flagging that pre-existing `TextCardBottomSheet.kt`/
  `ListCardBottomSheet.kt`/`ComponentRegistry.kt` lines changed) fired, landing on lane 2 instead
  of lane 3.
- **Fix:** Committed with `HUB_LANE_OVERRIDE=2` (the value the live classifier actually required),
  confirmed by manually invoking the classifier before committing rather than assuming the plan's
  session-time lane guess. Same coordination-gate mechanism, same deliberate-non-additive-change
  intent — just the override value matching the tool's real output.
- **Files modified:** None (commit-flow only, no source change).
- **Commit:** `dcc367d`

**2. [Informational, not a bug] `api.txt` required zero rebaseline**
- **Found during:** Task 2's `./gradlew apiDump` step
- **Issue:** The plan's acceptance criteria list `api.txt` among the files `git show --stat HEAD`
  should include, implicitly assuming the rebaseline would produce a diff (as it did in 05-01).
  Here, `apiDump` produced no changes at all — `SheetHeaderMenu` correctly stayed `internal` (no
  new public symbol) and neither host sheet's public method signature changed, so `api.txt` is
  byte-identical to its pre-extraction state.
- **Resolution:** This is the correct outcome per Pitfall 3's own guidance (a clean line-by-line
  diff review confirming "no other symbol changed" — here that review found literally nothing
  changed). Verified directly via `git diff api.txt` (empty) and `grep -c 'SheetHeaderMenuKt'
  api.txt` (0) rather than treating the file's absence from the commit as a gap. Not listed as a
  changed file in `git show --stat HEAD` for exactly this reason — there was nothing to stage.
- **Files modified:** None.

Also see Key Decisions above for the test-retarget collapsing approach (non-bug, a
plan-consistent implementation choice under the plan's own "either satisfies the phase's success
criteria" latitude for the retarget task).

## Self-Check: PASSED

- FOUND: `src/main/java/io/github/ygaray/yahirandroidtaste/component/SheetHeaderMenu.kt` (created,
  241 lines, `internal`, owns header/menu/rename triad with region markers intact)
- FOUND: `src/main/java/io/github/ygaray/yahirandroidtaste/component/TextCardBottomSheet.kt`
  (modified, 140 lines, delegates to `SheetHeaderMenu(...)`, public signature unchanged)
- FOUND: `src/main/java/io/github/ygaray/yahirandroidtaste/component/ListCardBottomSheet.kt`
  (modified, 275 lines, delegates to `SheetHeaderMenu(...)`, public signature unchanged)
- FOUND: `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt`
  (modified, `SheetHeaderMenu` entry present in `INTENTIONALLY_UNREGISTERED`, `init{}` invariant
  holds per `ComponentRegistryDriftGuardTest`)
- FOUND: `src/test/java/io/github/ygaray/yahirandroidtaste/component/TextListBottomSheetEditMenuSourceContractTest.kt`
  (retargeted, passes against `SheetHeaderMenu.kt`)
- FOUND: `API.md` (Intentionally-unregistered sub-parts bumped 5→6, `SheetHeaderMenu` row present)
- CONFIRMED: `api.txt` byte-identical (`git diff api.txt` empty; `apiCheck` passes)
- FOUND commit `dcc367d` in `git log --oneline --all`
