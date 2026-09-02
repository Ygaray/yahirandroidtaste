---
phase: 05-gardening-unify-coordinated-repin
reviewed: 2026-09-02T04:31:18Z
depth: standard
files_reviewed: 8
files_reviewed_list:
  - API.md
  - api.txt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ChipBar.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/FilterBar.kt (deleted)
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ListCardBottomSheet.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/SheetHeaderMenu.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/TextCardBottomSheet.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ChipsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/TextListBottomSheetEditMenuSourceContractTest.kt
findings:
  critical: 0
  warning: 2
  info: 1
  total: 3
status: issues_found
---

# Phase 05: Code Review Report

**Reviewed:** 2026-09-02T04:31:18Z
**Depth:** standard
**Files Reviewed:** 10 (9 source + API.md, api.txt; FilterBar.kt reviewed as its pre-deletion git blob)
**Status:** issues_found

## Summary

Reviewed WO-1 (fold `FilterBar` into `ChipBar`'s new `expandable` mode) and WO-2 (extract
`SheetHeaderMenu` out of `TextCardBottomSheet`/`ListCardBottomSheet`) end to end: read every
changed file plus the pre-change git blobs for `ChipBar.kt`, `FilterBar.kt`,
`TextCardBottomSheet.kt`, and `ListCardBottomSheet.kt` to diff actual before/after behavior line
by line, not just the after-state. Ran the full local verification surface myself rather than
trusting the summaries' claims: `./gradlew testDebugUnitTest` (full suite, incl.
`ComponentRegistryDriftGuardTest`, `TextListBottomSheetEditMenuSourceContractTest`,
`DomainVocabularyDriftGuardTest`), `./gradlew apiCheck`, and `./gradlew detekt` — all green,
confirming the two summaries' pass claims independently rather than accepting them at face value.

**Correctness assessment (the five focus areas):**
1. **ChipBar expandable fold-in** — correct. The `expandable == null` branch is byte-identical to
   the pre-WO-1 `ChipBar` (confirmed via git diff against the pre-phase blob: same `FlowRow`,
   same modifier chain, same composition order) — no regression to any existing bare-mode call
   site. The `expandable != null` branch reproduces `FilterBar`'s exact chrome (`Surface`,
   chevron, `maxLines`, height-cap + scroll, tight 2dp vertical gap) verbatim, confirmed against
   the deleted `FilterBar.kt`'s git blob.
2. **SheetHeaderMenu extraction** — correct. State hoisting (`showMenu`, `showRenameDialog`,
   `renameText`) is fully internal to `SheetHeaderMenu`, not leaked to either host sheet. Both
   sheets wire every callback through unchanged, and the diff against each sheet's pre-extraction
   git blob confirms byte-identical behavior (same trim-guard, same `onDismiss` firing rules, same
   Cancel-doesn't-dismiss behavior). `ListCardBottomSheet`'s omission of `imageCount` is a true
   no-op — verified `ImageCountIndicator.kt`'s `if (imageCount <= 0) return` guard directly.
3. **No accidental public API leak** — confirmed. `SheetHeaderMenu` is declared `internal`;
   `api.txt` contains zero `SheetHeaderMenuKt` entries (verified by direct grep), matching the
   zero-diff `apiDump` outcome both summaries claim.
4. **ComponentRegistry invariant** — correct. `FilterBar`'s `Entry` and demo were fully removed;
   `SheetHeaderMenu` was added to `INTENTIONALLY_UNREGISTERED` with a one-line rationale mirroring
   `SwipeableActionRow`'s existing shape. `ComponentRegistryDriftGuardTest` passes.
5. **Code quality / Compose best practices** — mostly clean (no unused imports, `detekt` zero
   findings, `key()` usage preserved correctly). Two gaps below are worth fixing before this ships
   to a consumer: a genuinely unexercised new public API path, and a self-contradicting doc
   summary line in a file this phase directly edited.

No critical/blocker-level defects found — the fold and the extraction are both behaviorally
correct and verified byte-identical against their respective pre-phase implementations.

## Warnings

### WR-01: `ChipBar`'s new `rawContent` freeform mode ships with zero test or gallery coverage

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ChipBar.kt:106,175-176`
**Issue:** WO-1's whole rationale for adding `rawContent: (@Composable FlowRowScope.() -> Unit)? =
null` was to carry `FilterBar`'s former freeform, slot-based `content: @Composable
FlowRowScope.() -> Unit` callers forward. It is real, shipped public API surface — it appears in
`api.txt`'s `ChipBarKt.ChipBar` signature — but nothing in the repo actually exercises the
`rawContent != null` branch: `ChipBarVariants()`'s new "expandable mode" demos (both collapsed and
expanded) use the typed `items`/`itemContent` path exclusively (confirmed by grep: `rawContent` is
referenced nowhere outside `ChipBar.kt` itself — no test, no gallery call site). 05-01-SUMMARY.md's
own Key Decisions section acknowledges choosing the typed mode "so no `TagChipUiModel` import
needed to come back," but that means the one genuinely new runtime code path this plan added
(`if (rawContent != null) rawContent() else items.forEach { ... }`) has never actually been
composed or rendered by anything in this repository — only inspected by eye. A caller migrating a
real `FilterBar<TagChipUiModel> { ... }` freeform site onto this new param is the first thing to
actually exercise it.
**Fix:** Add a third `ChipBarVariants()` demo section exercising `rawContent` (mirroring
`FilterBarVariants()`'s old freeform-`content` shape, e.g. `items = emptyList<TagChipUiModel>()`,
`key = { it.id }`, `itemContent = {}`, `rawContent = { ExplorerFakeData.tagChips.forEach { tag ->
AppChip(label = tag.name, isSelected = false, onClick = {}) } }`), or add a lightweight Robolectric
render test asserting the `rawContent` lambda actually gets invoked when non-null and that `items`
is correctly ignored in that mode.

### WR-02: API.md's top-of-file composable-count summary now contradicts its own
"Intentionally-unregistered sub-parts" section, which this phase directly edited

**File:** `API.md:27-28` vs. `API.md:170`
**Issue:** Line 27-28 still reads "**51 registered public composables** across the nine families,
plus **5 intentionally-unregistered** structural sub-parts ... = **56 public composables total**."
But the "Intentionally-unregistered sub-parts" heading three sections below — the exact heading
05-02-PLAN.md's Task 2 instructed to "bump ... by one" — now correctly reads "(6)" and lists 6 rows
(`CardBase`, `WaveformCanvas`, `SwipeableActionRow`, `RevealActionRow`, `YahirAndroidTasteTheme`,
`SheetHeaderMenu`), per this phase's own `SheetHeaderMenu` addition. The top summary line was not
updated alongside it, so the same file now asserts "5" in one place and "(6)" a few dozen lines
later — a direct, self-inflicted inconsistency in a file this task edited, not merely inherited
staleness (the pre-existing Cards-row/51-count mismatch predates this phase and was explicitly
flagged out-of-scope in 05-01-SUMMARY.md's Key Decisions — that part is fine to leave as-is — but
the 5-vs-6 unregistered-count mismatch is new, introduced by this phase's own edit landing
adjacent to, but not touching, the top summary sentence).
**Fix:** Update line 27-28 to read "plus **6 intentionally-unregistered** structural sub-parts ...
= **57 public composables total**" (leaving the separately-tracked Cards/51 staleness for its own
doc-audit pass, per 05-01-SUMMARY.md's explicit scoping decision).

## Info

### IN-01: Triple-duplicated assertions across three test methods in the retargeted source-contract test

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/TextListBottomSheetEditMenuSourceContractTest.kt:119-198`
**Issue:** 05-02-PLAN.md's Task 2 explicitly instructed collapsing "duplicated per-sheet assertion
pairs ... into single assertions against the shared file rather than keeping two copies." The
retarget achieved that goal for the *per-sheet* duplication (each fact about `SheetHeaderMenu.kt`
is now checked once per-sheet-pair, not twice), but introduced new *intra-file* duplication instead:
`src.contains("AlertDialog(")` against `SheetHeaderMenu.kt` is asserted in three separate `@Test`
methods (lines 132, 161, 181), `showRenameDialog` presence in three (128, 156-157, 175-177), and
`onConfirmRename(` presence in two (164, 184-185) — all checking the exact same three facts about
the exact same file. This doesn't affect correctness (the coverage is real, just repeated), but it
works against the plan's own stated simplification goal and adds maintenance surface (three call
sites to update if any of these markers legitimately change).
**Fix:** Consolidate the three overlapping `showRenameDialog`/`AlertDialog(`/`onConfirmRename(`
presence checks into the single `` `SheetHeaderMenu retains showRenameDialog and onConfirmRename
local dialog fallback` `` test method, and narrow the other two methods (`Edit row branches...` and
`both bottom sheets retain the full null-hook fallback structurally intact`) to only the assertions
genuinely unique to their own stated purpose (branch-inversion check; cross-file per-sheet wiring
check, respectively).

---

_Reviewed: 2026-09-02T04:31:18Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
