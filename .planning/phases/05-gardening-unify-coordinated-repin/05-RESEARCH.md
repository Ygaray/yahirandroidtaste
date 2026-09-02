# Phase 5: Gardening — Unify & Coordinated Repin - Research

**Researched:** 2026-09-01
**Domain:** Android/Compose design-system library — API-breaking component unification + Metalava
rebaseline + multi-repo human-gated coordinated repin
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01 [unify-scope]:** implement exactly Phase 2's dispositioned unify tuples 1:1, adding/removing
  no scope at execution time — re-analyzing overlaps desyncs the unify count from the audit and
  breaks the 1:1 mapping the `api.txt` rebaseline diff review depends on. _(provisional — refresh at
  execution; depends on Phase 2)_ _(source: ai-auto)_
- **D-02 [fold-mechanism]:** decide per fold from Phase 2's disposition wording — default to **remove
  entirely** for a `prune`, and **fold-then-demote to `INTENTIONALLY_UNREGISTERED` with a rationale
  (or remove)** for a `unify` — keeping registered-XOR-allowlisted true either way. The general
  mechanic (update the registry cell, `./gradlew apiDump` rebaseline with line-by-line review, commit
  via curation lane `HUB_LANE_OVERRIDE=3`) is settled. _(provisional — refresh at execution; depends
  on Phase 2)_ _(source: ai-auto)_
- **D-03 [semver]:** cut the gardening tag as **v2.0.0** (semver-correct major) — this is the first
  true break in an all-additive `v1.0.0→v1.10.0` history; a break under a non-breaking version lies
  to consumers and any future range-based resolution would silently pull it. The tag-cut itself stays
  human-gated regardless. _(source: human)_
- **D-04 [repin-seam]:** the hub cuts the tag on `main` here; each consumer repin runs through that
  consumer's **own channel** (human-gated), staged so the ecosystem isn't "moved" until both are
  ready + Gate-1 re-verified — editing consumer files from this hub phase would violate
  sequential-in-hub. `repin_status.py reconcile` (Phase 4) proves both pins moved. _(source: ai-auto)_
- **D-05 [caltracker-catchup]:** CalTracker (at `v1.5.0`, 7 additive tags behind) gets an
  **intermediate catch-up repin to `v1.10.0` (Gate-1 verified)** before moving to the gardening tag —
  a single jump across `v1.6.0→v1.10.0` plus the break gives it a far wider blast radius than
  SecondBrain's single-tag move, raising the odds an unrelated regression strands it while
  SecondBrain passes. _(source: human)_

**Runtime Decisions (refreshed against Phase 2's shipped `docs/COHERENCE-AUDIT.md`):**
- **unify-scope confirmed concrete:** exactly two unify dispositions exist — **WO-1** (fold
  `FilterBar` into `ChipBar` as an optional expandable mode; retire `FilterBar` as a standalone
  registered entry; Chips family; SecondBrain 5 files, CalTracker 0) and **WO-2** (extract the shared
  header/menu/rename-dialog from `TextCardBottomSheet`/`ListCardBottomSheet` into a new shared
  composable; both sheets retained; Sheets family; SecondBrain 2 files each, CalTracker 0). No other
  unify dispositions exist — do not re-analyze.
- **fold-mechanism confirmed:** WO-1 folds `FilterBar`'s behavior INTO `ChipBar` (nullable expandable
  config + raw-content slot), then removes or demotes `FilterBar`'s registry cell to
  `INTENTIONALLY_UNREGISTERED` with a one-line reason (SecondBrain's 5 usages mean source-compat /
  deprecation should be considered). WO-2 retains both sheets and only ADDS a new shared composable —
  nothing removed from any family list, no demote/delete needed on that side.

### Claude's Discretion
None explicitly separated out in CONTEXT.md beyond the "per-fold Phase-5 design call" language in
D-02/the Runtime Decisions above (e.g. exact `ChipBar` param shape, exact new composable name/shape,
delete-vs-demote for `FilterBar`) — treated as open design work this research surfaces but does not
resolve (see Assumptions Log, A1-A3).

### Deferred Ideas (OUT OF SCOPE)
None — this is the terminal phase; unify scope is bounded to Phase 2's dispositions.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| GARD-01 | Additive-duplicate siblings identified by AUD-01 are unified into single components (removing/renaming where needed) | Pattern 1 (WO-1 `FilterBar`→`ChipBar` fold, concrete signature sketch + registry edit) and Pattern 2 (WO-2 shared sheet-header extraction, concrete signature sketch + registry edit) both derived from full reads of the four affected source files; Pitfall 1-3 cover the exact test/registry/API-signature breakage each unify edit must account for |
| GARD-02 | Gardening changes land via the human-gated coordinated repin ritual — new immutable tag → both consumers repinned and Gate-1 re-verified — with no consumer left stranded | Architecture Diagram + Pitfall 4 (pre-commit `HUB_LANE_OVERRIDE=3` mechanics) + Pitfall 5 (CalTracker's two-hop catch-up blast radius, version-skew facts confirmed) + Code Examples' `repin_status.py reconcile` mechanics (Phase 4 tooling this phase's repin proves) |
</phase_requirements>

## Summary

This phase implements exactly two unify dispositions from `docs/COHERENCE-AUDIT.md`'s Unify
Work-Order (WO-1, WO-2) as concrete, breaking source edits in this hub repo, rebaselines the
`ComponentRegistry` drift guard + Metalava `apiCheck` for the intentional break, and stops at a
human-gated tag cut + coordinated consumer repin. Both unify targets were read in full this
session — `FilterBar.kt` (113 lines), `ChipBar.kt` (65 lines), `TextCardBottomSheet.kt` (299
lines), `ListCardBottomSheet.kt` (423 lines) — along with the registry (`ComponentRegistry.kt`),
its drift-guard test, `api.txt`'s current signature lines for all four components, the pre-commit
lane-classifier hook, and SecondBrain's real call sites for both.

The single most important finding for planning: **WO-2's extraction will break an existing test**
(`TextListBottomSheetEditMenuSourceContractTest.kt`) unless the plan explicitly updates it. That
test does a plain-text source scan of `TextCardBottomSheet.kt`/`ListCardBottomSheet.kt` looking
for `AlertDialog(`, `showRenameDialog`, `onConfirmRename(`, and the `// region:edit-menu-item` /
`// endregion:edit-menu-item` marker comments — all of which the WO-2 extraction will *move out*
of those two files into the new shared composable. The plan must retarget (or add coverage in)
this test against the new shared file, or it goes red the moment the extraction lands. WO-1 has no
equivalent pre-existing source-contract test to break (`FilterBar`/`ChipBar` have zero dedicated
unit tests today — confirmed by grep across `src/test/`), but `API.md`'s hand-written component
table (lines 65/67) documents both components' pre-unify shapes and will silently go stale unless
a task updates it (no automated doc-parity guard exists — confirmed, no such test/tool found).

**Primary recommendation:** Implement WO-1 (fold `FilterBar` into `ChipBar` via a new nullable
`expandable: ExpandableConfig?` param + a raw-content slot) and WO-2 (extract a new
`SheetHeaderMenu`-shaped composable carrying the header `Row` + three-dot `DropdownMenu` + rename
`AlertDialog`, composed by both sheets) as two separate lane-3 commits under
`HUB_LANE_OVERRIDE=3`, each followed by `./gradlew apiDump` + registry/test rebaseline, then cut
`v2.0.0` and run the two-step CalTracker catch-up + gardening repin per the CONTEXT.md's D-05
decision — all already locked, not re-litigated here.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| `FilterBar`→`ChipBar` fold (WO-1) | Hub (`component/`) | Hub (`explorer/` registry) | Pure Compose UI unification; registry entry moves with it |
| Sheet header/menu/rename extraction (WO-2) | Hub (`component/`) | — | New shared composable, same package, no cross-tier concern |
| `ComponentRegistry` drift-guard rebaseline | Hub (`explorer/` test) | — | Registry is the hub's own single source of truth |
| `api.txt` Metalava rebaseline | Hub (build config / CI) | — | Public-API signature file lives in hub repo root |
| Tag cut (`v2.0.0`) | Hub (git) | — | Human-gated per CLAUDE.md; hub `main` is the tag source |
| Consumer repin (SecondBrain, CalTracker) | Consumer (each repo) | — | Sequential-in-hub forbids editing consumer files from this hub phase; each consumer repins via its own channel |
| Gate-1 re-verification | Consumer (device/emulator) | — | Runs in each consumer's own GSD project, out of this phase's file scope |

## Package Legitimacy Audit

Not applicable — this phase adds no new third-party dependency. All work is internal Kotlin/Compose
source restructuring within the existing module (Compose BOM, Hilt, Metalava plugin already pinned
in `build.gradle.kts`, unchanged this phase).

## Standard Stack

No new libraries. Confirmed unchanged toolchain relevant to this phase (read `build.gradle.kts`
this session):
- Metalava via `me.tylerbwong.gradle.metalava` plugin, `filename = "api.txt"` (`build.gradle.kts:16-17`).
- `apiDump`/`apiCheck` tool-agnostic alias tasks delegate to `metalavaGenerateSignatureRelease` /
  `metalavaCheckCompatibilityRelease` (`build.gradle.kts:20-29`).
- Robolectric + Compose UI-test-junit4/manifest already on `testImplementation` (`build.gradle.kts`
  dependencies block) — but note the Sheets family's own established pattern (see Pitfall 1 below):
  `SheetScaffold`'s live `ModalBottomSheet` is NOT drivable by this module's Robolectric harness, so
  new coverage for the extraction must be a source-structural contract test (plain `File.readText()`
  + string assertions via `SourceContractTestSupport`), matching every existing sheet test in this
  codebase, not a rendered Compose-UI-test.

## Architecture Patterns

### System Architecture Diagram

```
Phase 2 audit (docs/COHERENCE-AUDIT.md, WO-1 + WO-2)
        │
        ▼
┌─────────────────────────── Hub repo (this phase, autonomous) ───────────────────────────┐
│                                                                                            │
│  WO-1: FilterBar.kt ──(fold behavior in)──▶ ChipBar.kt (+ExpandableConfig? param,         │
│         │                                    +raw-content slot)                           │
│         └─(retire)─▶ ChipsFamilyScreen.kt: remove FilterBar Entry from chipsFamilyEntries  │
│                       (+ demote/remove per D-02 fold-mechanism)                            │
│                                                                                             │
│  WO-2: TextCardBottomSheet.kt ─┐                                                          │
│                                 ├─(extract shared header+menu+rename)─▶ new shared         │
│  ListCardBottomSheet.kt ───────┘         composable (component/ package)                  │
│         both retained, both now call the new composable                                   │
│                                                                                             │
│  ├─▶ ComponentRegistry drift guard rebaseline (registered XOR INTENTIONALLY_UNREGISTERED)  │
│  ├─▶ TextListBottomSheetEditMenuSourceContractTest retargeted at the new shared file        │
│  ├─▶ ./gradlew apiDump  →  api.txt rebaseline (line-by-line diff review)                    │
│  ├─▶ API.md component table updated (manual doc, no automated guard)                        │
│  └─▶ Commit(s) via HUB_LANE_OVERRIDE=3 (lane-3 API break, pre-commit hook)                   │
│                                                                                             │
└──────────────────────────────────┬─────────────────────────────────────────────────────────┘
                                    │  (human-gated stop — CLAUDE.md §"shipping is human-gated")
                                    ▼
                      git tag v2.0.0 on hub main (human confirms)
                                    │
                     ┌──────────────┴───────────────┐
                     ▼                               ▼
        SecondBrain repin (single hop)     CalTracker repin (two hops)
        v1.10.0 → v2.0.0                   v1.5.0 → v1.10.0 (catch-up) → v2.0.0
        each: coordinate bump → resolve  → rebuild → suite → Gate-1 device re-verify
                     │                               │
                     └──────────────┬────────────────┘
                                    ▼
                repin_status.py reconcile (Phase 4 tooling)
                proves both pins moved; ECOSYSTEM.md repin-matrix updated
```

### Recommended Project Structure

No new directories. Both unify targets stay in the existing flat package:
```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/
│   ├── ChipBar.kt              # gains ExpandableConfig? + raw-content slot (WO-1)
│   ├── FilterBar.kt            # deleted OR reduced to nothing (see D-02 disposition)
│   ├── TextCardBottomSheet.kt  # shrinks — delegates header/menu/rename to new composable
│   ├── ListCardBottomSheet.kt  # shrinks — delegates header/menu/rename to new composable
│   └── <NewSheetHeaderMenu>.kt # new file — WO-2's extracted shared composable
├── explorer/
│   ├── ChipsFamilyScreen.kt    # chipsFamilyEntries loses the FilterBar Entry
│   └── ComponentRegistry.kt    # INTENTIONALLY_UNREGISTERED gains/loses FilterBar per D-02
└── (api.txt at repo root — rebaselined, not moved)
```

### Pattern 1: WO-1 — Fold `FilterBar`'s chrome into `ChipBar` as an optional mode

**What:** `ChipBar` (`component/ChipBar.kt:42-64`, read in full this session) is a typed
`items: List<T>` FlowRow container with `leadingContent`/`trailingContent` slots and no chrome.
`FilterBar` (`component/FilterBar.kt:54-113`, read in full) is a slot-based (`content: @Composable
FlowRowScope.() -> Unit`) FlowRow with `Surface` chrome, an `expanded`/`onExpand`/`onCollapse`
chevron `IconButton`, and a height-capped/scrolling expanded state.

**When to use:** Any caller that today reaches for `FilterBar` because it needs the
expand/collapse chrome on a chip row — after this phase, that caller reaches for `ChipBar`'s new
`expandable` param instead.

**Concrete shape derived from the two real signatures** (this is design work per the audit's own
words — the following is the most direct mechanical synthesis of the two, offered as a starting
point, not a locked signature):

```kotlin
// Source: this session's read of component/ChipBar.kt + component/FilterBar.kt
data class ExpandableConfig(
    val expanded: Boolean,
    val onExpand: () -> Unit,
    val onCollapse: () -> Unit,
    val contentDescription: String = "Tag filters" // FilterBar's filterContentDescription default
)

@Composable
fun <T> ChipBar(
    items: List<T>,
    key: (T) -> Any,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "chip_bar",
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    expandable: ExpandableConfig? = null,          // NEW — null preserves every existing call site
    rawContent: (@Composable FlowRowScope.() -> Unit)? = null  // NEW — carries FilterBar's freeform callers
) { /* Surface+chevron chrome gated on expandable != null, wraps the existing FlowRow body */ }
```

Both new params are trailing and defaulted (`null`) — every existing `ChipBar(...)` call site
(5 files in SecondBrain, confirmed by grep this session) keeps compiling unchanged. This mirrors
the exact "trailing, nullable, defaulted" idiom `TextCardBottomSheet`/`ListCardBottomSheet` already
used for `onEditRequest`, `readOnlyPreview`, `previewOverflowCount` (established codebase pattern,
confirmed by reading those files' KDoc this session — see Pattern 2).

**Registry side (WO-1):** `ChipsFamilyScreen.kt`'s `chipsFamilyEntries` list (read in full,
`explorer/ChipsFamilyScreen.kt:56-257`) has 5 entries today: `AppChip`, `TagChipWithContextMenu`,
`ChipBar`, `SortControl`, `FilterBar`. Retiring `FilterBar` means deleting its `Entry(...)` block
(`ChipsFamilyScreen.kt:213-256`) and its two demo functions (`FilterBarVariants()`,
`:407-430`), and adding an `expandable`-mode demo into `ChipBarVariants()` (`:343-381`) so the new
mode is still showcased. No hardcoded entry-count assertion exists anywhere in the test suite
(confirmed — grepped `ComponentRegistryTierTest.kt`/`ComponentRegistrySearchTest.kt` for count
literals, found none), so removing one entry cannot break a numeric assertion, only the
registered-XOR-allowlisted invariant (see Pitfall 2).

### Pattern 2: WO-2 — Extract the shared sheet header/menu/rename-dialog

**What:** Both `TextCardBottomSheet.kt` (lines 96-297, read in full) and `ListCardBottomSheet.kt`
(lines 119-331, read in full) contain a byte-for-byte-identical structure across three regions:

1. **Header `Row`** — title (weight-1 `Column`), conditional Pin icon, conditional Favorite icon,
   three-dot `IconButton` (`TextCardBottomSheet` additionally inserts an `ImageCountIndicator`
   between Favorite and the three-dot button — the one real header difference).
2. **Three-dot `DropdownMenu`** — Edit (routes to `onEditRequest()` + `onDismiss()` when non-null,
   else opens `showRenameDialog`) → Pin/Unpin → Favorite/Unfavorite → Delete (error-tinted). Both
   files wrap the Edit row in identical `// region:edit-menu-item` / `// endregion:edit-menu-item`
   marker comments (`TextCardBottomSheet.kt:169-188`, `ListCardBottomSheet.kt:187-206`) — these
   markers are load-bearing test anchors (see Pitfall 1), not decoration.
3. **Rename `AlertDialog`** — identical `ClearableTextField` + confirm/cancel `TextButton` pair
   (`TextCardBottomSheet.kt:268-296`, `ListCardBottomSheet.kt:302-330`), gated by local
   `showRenameDialog`/`renameText` state.

**When to use:** Both sheets keep their own body slot (differing per audit: `content: String?` +
`imageCount: Int` for Text vs. `items` + `onToggleItem` + `readOnlyPreview` +
`previewOverflowCount` for List) — only the header+menu+dialog triad is shared.

**Precedent this extraction should mirror (D-04):** `CardQuickView` was itself "extracted from the
common structure of `TextCardBottomSheet` and `ListCardBottomSheet`" per its own KDoc
(`docs/COHERENCE-AUDIT.md` Finding C-2, confirmed) — it is a **content-only, non-swipeable display
archetype** that both sheets already compose with a blank `title` to suppress its own internal
header. The new WO-2 composable should follow the same shape: a standalone, registered
component in `component/`, taking every varying piece as a parameter (title, pin/favorite booleans
+ callbacks, delete callback, `imageCount: Int = 0` — nullable/defaulted so
`ListCardBottomSheet`'s call site simply never passes it — `onEditRequest`, and the rename
callback), composed by both sheets in place of their current inline `Row { ... } / Box { ... } /
if (showRenameDialog) { ... }` blocks.

```kotlin
// Source: derived from TextCardBottomSheet.kt:110-225,268-296 and
// ListCardBottomSheet.kt:136-243,302-330 (both read in full this session) — the union of both
// files' actual header/menu/dialog code, not invented from scratch.
@Composable
internal fun SheetHeaderMenu(   // name illustrative — planner's own naming call
    title: String,
    isPinned: Boolean,
    isFavorite: Boolean,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmRename: (String) -> Unit,
    onEditRequest: (() -> Unit)? = null,
    imageCount: Int = 0,   // TextCardBottomSheet passes its real count; ListCardBottomSheet omits
    modifier: Modifier = Modifier
) { /* header Row + three-dot DropdownMenu + rename AlertDialog, verbatim union of both bodies */ }
```

**Registry side (WO-2):** Per D-02's fold-mechanism, WO-2 is purely additive at the registry level
— both `TextCardBottomSheet` and `ListCardBottomSheet` entries in `sheetsFamilyEntries`
(`explorer/SheetsFamilyScreen.kt`) are untouched; the new shared composable needs its own
disposition: either give it a standalone `ComponentRegistry.Entry` (if judged independently
showcaseable) or add it to `INTENTIONALLY_UNREGISTERED` with a one-line reason mirroring
`SwipeableActionRow`'s existing allowlist entry ("infrastructure...already exercised indirectly via
every card entry's..."). The `CardQuickView` precedent (which composes `TextCardBottomSheet`
internally) IS independently registered in `CardsFamilyScreen.kt`, suggesting the planner should
default to registering the new composable too — but this is a Phase-5 design call, not settled by
this research.

### Anti-Patterns to Avoid

- **Removing the `// region:edit-menu-item` markers during extraction.** They are the literal
  string anchors `TextListBottomSheetEditMenuSourceContractTest.editMenuItemRegion()` searches for
  (`start = src.indexOf("// region:edit-menu-item")`) — if the extraction deletes them without
  updating the test, the test's `require(start >= 0 && end > start)` throws with a message that
  correctly diagnoses the break, but the build still goes red. Move the markers to the new shared
  file and retarget the test's `source(file)` calls, or the CI break is immediate (see Pitfall 1).
- **Forgetting `ImageCountIndicator` is `TextCardBottomSheet`-only.** It is the one genuine header
  asymmetry between the two sheets (confirmed: absent from `ListCardBottomSheet.kt`'s header Row
  entirely) — a naive "extract the identical Row" pass that doesn't parameterize it will either
  drop the indicator from Text or wrongly add it to List.
- **Treating `FilterBar`'s retirement as a silent deletion.** SecondBrain has 5 real files
  importing/calling it (confirmed via direct grep + read this session — `BrowseScreen.kt` calls
  `FilterBar<TagEntity>(...)` at two call sites, `TagFilterBarContent.kt` supplies its `content`
  slot fixtures, `BrowseViewModel.kt` + 2 test files reference it) — this is a real breaking change
  for a real consumer, which is exactly why it's gated on the coordinated repin, not a
  hypothetical.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| API-break detection | A manual `api.txt` diff review process from scratch | `./gradlew apiCheck` (Metalava, already wired) | Already proven in this repo across 10 prior tag cuts (v1.1.0→v1.10.0); catches removed/renamed symbols mechanically |
| Registry coverage enforcement | A new "did I register this" checklist | `ComponentRegistryDriftGuardTest` (source-scans every non-`explorer` package) | Already fails loudly (`fail(...)` with the exact offending name) if the new shared composable is neither registered nor allowlisted |
| Sheet interaction test coverage | A Robolectric/Compose-UI-test rule for the new composable | The existing source-structural-contract pattern (`SourceContractTestSupport`, plain `File.readText()` + string assertions) | This module's own established precedent — `SheetScaffold`'s live `ModalBottomSheet` is confirmed NOT drivable by this module's Robolectric harness (stated in 3 existing test KDocs read this session: `TextListBottomSheetEditMenuSourceContractTest`, `ListCardBottomSheetReadOnlyPreviewSourceContractTest`, `VoiceRenameTagsSheetGateTest`) |
| Consumer repin automation | A script that edits SecondBrain/CalTracker files from this hub phase | Nothing — sequential-in-hub forbids it | CLAUDE.md's explicit convention; repin runs in each consumer's own GSD project/channel |

**Key insight:** Every mechanism this phase needs (API-break detection, registry-coverage
enforcement, source-contract test pattern, repin reconciliation) already exists and is proven in
this exact repo across prior phases — this phase's job is applying them to two new edits, not
inventing new tooling.

## Common Pitfalls

### Pitfall 1: WO-2's extraction silently breaks `TextListBottomSheetEditMenuSourceContractTest`
**What goes wrong:** The test (`src/test/.../component/TextListBottomSheetEditMenuSourceContractTest.kt`,
204 lines, read in full) asserts against `SourceContractTestSupport.source("TextCardBottomSheet.kt")`
and `source("ListCardBottomSheet.kt")` directly — plain `File(...).readText()` on those two exact
filenames. It checks for `onEditRequest: (() -> Unit)? = null` in the signature, `if (onEditRequest
!= null)` branching, `showRenameDialog`, `AlertDialog(`, `onConfirmRename(`, and (via
`editMenuItemRegion()`) exactly one `Text("Edit")` and zero `Text("Rename")` inside the
`// region:edit-menu-item` markers.
**Why it happens:** WO-2 moves all of this code into a new shared file. After the move,
`TextCardBottomSheet.kt`'s own source text no longer contains any of it — the test reads the wrong
file for its own assertions.
**How to avoid:** The plan MUST include a task that updates this test — either (a) change its
`source(file)` calls to point at the new shared composable's filename for the moved assertions
(header/menu/dialog), while keeping any body-slot-specific assertions pointed at the original
files, or (b) split it into a new test file colocated with the new composable. Either way this is
NOT optional cleanup — it is required or the phase's own `testDebugUnitTest` gate goes red.
**Warning signs:** `./gradlew testDebugUnitTest` reporting `require(start >= 0 && end > start)`
failures with the exact message "could not locate the // region:edit-menu-item ... markers" is the
literal failure mode if this is missed.

### Pitfall 2: Registry `Entry` XOR `INTENTIONALLY_UNREGISTERED` invariant is enforced at
**object-init time**, not just by the test
**What goes wrong:** `ComponentRegistry`'s own `init { ... }` block (`ComponentRegistry.kt:130-152`,
read in full) throws `IllegalArgumentException` at class-load time if a name appears in both
`entries` and `INTENTIONALLY_UNREGISTERED`, or if `INTENTIONALLY_UNREGISTERED` has a blank-reason
entry. This fires the moment ANY code touches `ComponentRegistry` — including
`ComponentRegistryDriftGuardTest` itself, `ComponentRegistryTierTest`,
`ComponentRegistrySearchTest`, `DomainVocabularyDriftGuardTest`, and the `ExplorerActivity` gallery
at runtime.
**Why it happens:** A half-finished WO-1 edit (e.g. `FilterBar`'s `Entry` deleted from
`chipsFamilyEntries` but its name simultaneously left in — or added twice to —
`INTENTIONALLY_UNREGISTERED`) fails ALL of the above tests simultaneously, not just the drift
guard, making the failure look broader than it is.
**How to avoid:** Make the `FilterBar` registry edit (delete `Entry` block AND, per D-02, either
fully delete the `FilterBar` composable or add exactly one `INTENTIONALLY_UNREGISTERED` entry for
it) a single atomic edit, verified by running `ComponentRegistryDriftGuardTest` alone immediately
after.
**Warning signs:** Multiple unrelated-looking registry tests failing at once with the same
`IllegalArgumentException` message about duplicate/overlapping names.

### Pitfall 3: `api.txt` rebaseline must be reviewed line-by-line, not blindly regenerated
**What goes wrong:** `./gradlew apiDump` will happily regenerate `api.txt` to match whatever the
current public API is — including accidentally-public symbols that were meant to stay `internal`.
**Why it happens:** The new WO-2 shared composable, if declared without an explicit `internal`
modifier, becomes a NEW public API surface member that `apiDump` will add — this is fine if
intentional (see WO-2 registry-side note above on independent registration) but must be a
deliberate choice, not an accident of a missing modifier.
**How to avoid:** D-02's own settled mechanic already specifies "line-by-line review" of the
`apiDump` diff — confirmed current `api.txt` lines for the 4 affected symbols this session
(`ChipBarKt` line 100-101, `FilterBarKt` line 161-162, `ListCardBottomSheetKt` line 205-206,
`TextCardBottomSheetKt` line 337-338) so the planner can diff the post-edit dump against these
exact baseline lines.
**Warning signs:** `apiCheck` (pre-tag-cut) reporting unexpected new/changed public symbols beyond
`ChipBarKt.ChipBar`, `FilterBarKt.FilterBar` (removed), `TextCardBottomSheetKt.TextCardBottomSheet`,
`ListCardBottomSheetKt.ListCardBottomSheet`, and the new shared composable's own class.

### Pitfall 4: The pre-commit hook blocks lane-3 (API break) commits by default
**What goes wrong:** `tools/hooks/pre-commit` (read in full, 29 lines) runs
`tools/classify-hub-change.sh` and exits 1 with "BLOCKED — lane 3 (non-additive) change on the fast
path" unless `HUB_LANE_OVERRIDE=3` is set for that exact commit.
**Why it happens:** This is intentional coordination-gating (per `tools/README-api-guard.md`), and
CONTEXT.md's D-02 already names `HUB_LANE_OVERRIDE=3` as the settled mechanic — but it must be set
per-commit (an env var on the `git commit` invocation itself), not once per session.
**How to avoid:** Every commit in this phase that touches `FilterBar.kt`/`ChipBar.kt`/
`TextCardBottomSheet.kt`/`ListCardBottomSheet.kt`/the new shared file/`ComponentRegistry`-adjacent
files/`api.txt` needs `HUB_LANE_OVERRIDE=3 git commit ...`.
**Warning signs:** `git commit` exiting 1 with the exact "BLOCKED — lane 3" message from the hook.

### Pitfall 5: CalTracker's two-hop repin (v1.5.0 → v1.10.0 → v2.0.0) crosses 5 intervening
**additive** tags whose combined diff is much larger than SecondBrain's single hop
**What goes wrong:** Per `ECOSYSTEM.md`'s own release records (read in full this session),
`v1.6.0`→`v1.10.0` alone adds 4 new Tactile-family components + a Heat-tier widening + multiple
Card-face pass-through param sets — CalTracker's catch-up hop is not a no-op, even though every
step is individually additive.
**Why it happens:** CalTracker has been pinned at `v1.5.0` since Phase 48 (confirmed —
`~/Projects/CalTracker_Android/gradle/libs.versions.toml:73` reads `yahirandroidtaste = "v1.5.0"`
this session) — 5 minor versions behind SecondBrain's `v1.10.0` (confirmed —
`~/Projects/SecondBrain/gradle/libs.versions.toml:37` reads `yahirandroidtaste = "v1.10.0"`).
**How to avoid:** This is already the exact reasoning behind CONTEXT.md's D-05 decision (catch-up
hop first, Gate-1-verified, BEFORE the gardening tag) — this research confirms the version-skew
facts underlying that decision are accurate, not a re-litigation of the decision itself. The plan
should treat the catch-up hop as its own checkpoint with its own Gate-1 pass, distinct from the
gardening-tag hop.
**Warning signs:** A CalTracker build/resolve failure during the catch-up hop that has nothing to
do with WO-1/WO-2 (since none of the intervening v1.6.0-v1.10.0 tags touch Chips or Sheets — the
audit's own blast-radius grep confirms CalTracker is 0 files on both `FilterBar`/`ChipBar` and
`TextCardBottomSheet`/`ListCardBottomSheet`) — such a failure would be a pre-existing catch-up
issue, not caused by this phase's unify work.

## Code Examples

### The registered-XOR-allowlisted invariant enforcement (verbatim, what WO-1/WO-2's registry
edits must satisfy)
```kotlin
// Source: explorer/ComponentRegistry.kt:130-152 (read in full this session)
init {
    val entryNames = entries.map { it.name }
    val duplicateEntryNames = entryNames
        .groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    require(duplicateEntryNames.isEmpty()) { /* ... */ }

    val overlap = entryNames.toSet() intersect INTENTIONALLY_UNREGISTERED.keys
    require(overlap.isEmpty()) {
        "Name(s) present in both entries and INTENTIONALLY_UNREGISTERED: $overlap — a " +
            "component must be registered XOR allowlisted, never both."
    }

    val blankReasons = INTENTIONALLY_UNREGISTERED.filterValues { it.isBlank() }.keys
    require(blankReasons.isEmpty()) { /* ... */ }
}
```

### Existing `INTENTIONALLY_UNREGISTERED` allowlist entry shape (precedent for demoting `FilterBar`
if D-02's fold-mechanism resolves to demote-rather-than-delete)
```kotlin
// Source: explorer/ComponentRegistry.kt:115-118 (read in full this session)
"SwipeableActionRow" to
    "Swipe-reveal mechanics powering CardBase and EditorItemRow — infrastructure, not " +
    "an independent visual archetype; already exercised indirectly via every card " +
    "entry's reveal-confirm swipe and via EditorItemRow's own demo.",
```

### `ComponentRegistryDriftGuardTest`'s exact failure-mode text (what a mis-registered symbol
produces)
```kotlin
// Source: explorer/ComponentRegistryDriftGuardTest.kt:101-109 (read in full this session)
fail(
    "Found ${offendingNames.size} public top-level @Composable function(s) outside " +
        "the excluded packages ($excludedPackages) that are neither registered in " +
        "ComponentRegistry.entries nor allowlisted in " +
        "ComponentRegistry.INTENTIONALLY_UNREGISTERED: " +
        "${offendingNames.sorted()} — register each as a new explorer entry, or add " +
        "it to INTENTIONALLY_UNREGISTERED with a one-line reason (D-04)."
)
```

### Current `api.txt` baseline lines for the four affected public symbols (verified via `grep`
this session, `api.txt` is 1137 lines total)
```
// Source: api.txt:100-101, 161-162, 205-206, 337-338 (read via grep this session)
100:  public final class ChipBarKt {
101:    method @KotlinOnly @androidx.compose.runtime.Composable public static <T> void ChipBar(java.util.List<T> items, kotlin.jvm.functions.Function1<T,java.lang.Object> key, kotlin.jvm.functions.Function1<T,kotlin.Unit> itemContent, optional androidx.compose.ui.Modifier modifier, optional String testTag, optional kotlin.jvm.functions.Function0<kotlin.Unit>? leadingContent, optional kotlin.jvm.functions.Function0<kotlin.Unit>? trailingContent);
161:  public final class FilterBarKt {
162:    method @KotlinOnly @androidx.compose.runtime.Composable public static <T> void FilterBar(boolean expanded, kotlin.jvm.functions.Function0<kotlin.Unit> onExpand, kotlin.jvm.functions.Function0<kotlin.Unit> onCollapse, optional androidx.compose.ui.Modifier modifier, optional String filterContentDescription, kotlin.jvm.functions.Function1<androidx.compose.foundation.layout.FlowRowScope,kotlin.Unit> content);
205:  public final class ListCardBottomSheetKt {
206:    method @KotlinOnly @androidx.compose.runtime.Composable public static void ListCardBottomSheet(String title, java.util.List<io.github.ygaray.yahirandroidtaste.model.ListItemUiModel> items, String subType, String? categoryPath, long createdAt, long updatedAt, boolean isPinned, boolean isFavorite, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> onToggleItem, kotlin.jvm.functions.Function0<kotlin.Unit> onEdit, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onTogglePin, kotlin.jvm.functions.Function0<kotlin.Unit> onToggleFavorite, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> onConfirmRename, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, optional kotlin.jvm.functions.Function0<kotlin.Unit>? tagContent, optional kotlin.jvm.functions.Function0<kotlin.Unit>? onEditRequest, optional boolean readOnlyPreview, optional int previewOverflowCount);
337:  public final class TextCardBottomSheetKt {
338:    method @KotlinOnly @androidx.compose.runtime.Composable public static void TextCardBottomSheet(String title, String? content, String? categoryPath, long createdAt, long updatedAt, boolean isPinned, boolean isFavorite, kotlin.jvm.functions.Function0<kotlin.Unit> onEdit, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, kotlin.jvm.functions.Function0<kotlin.Unit> onTogglePin, kotlin.jvm.functions.Function0<kotlin.Unit> onToggleFavorite, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, kotlin.jvm.functions.Function1<java.lang.String,kotlin.Unit> onConfirmRename, optional kotlin.jvm.functions.Function0<kotlin.Unit>? tagContent, optional int imageCount, optional kotlin.jvm.functions.Function0<kotlin.Unit>? onEditRequest);
```

### `repin_status.py`'s `reconcile` / matrix-parsing mechanics (Phase 4 tooling this phase's repin
step proves)
```python
# Source: ~/.claude/context/deps/repin_status.py:186-200 (read in full this session)
def reconcile_ecosystem(eco_path: Path, statuses) -> bool:
    if any(s.get("status") == "unknown" for s in statuses):
        raise ValueError("refusing to reconcile with unknown tag status (offline?) — "
                         "Latest/Status would be lost")
    text = eco_path.read_text()
    if _MATRIX_BEGIN not in text or _MATRIX_END not in text:
        raise ValueError(f"{eco_path}: no <!-- repin-matrix:begin/end --> markers "
                         "(add them once around the consumer matrix before reconcile)")
    b = text.index(_MATRIX_BEGIN) + len(_MATRIX_BEGIN)
    e = text.index(_MATRIX_END, b)
    new_block = "\n" + render_matrix_block(statuses) + "\n"
    if text[b:e] == new_block:
        return False
    eco_path.write_text(text[:b] + new_block + text[e:])
    return True
```
Invocation for this phase (per `~/.claude/context/workflows/repin.md` §Mechanism B and the
`ECOSYSTEM.md` header pointing at it): `python3 repin_status.py reconcile --hub
yahirandroidtaste` — walks `~/Projects` for `libs.versions.toml`/`build.gradle.kts` manifests,
classifies each consumer's pin against the hub's live GitHub tags (`git ls-remote --tags`), and
rewrites the `<!-- repin-matrix:begin/end -->` block in `ECOSYSTEM.md` in place. Both markers
already exist in `ECOSYSTEM.md` today (Phase 4 seeded them — confirmed present at lines 45/50 of
`ECOSYSTEM.md`, read this session), so this phase's repin step needs no re-seeding, just a
post-repin `reconcile` run to prove both pins moved.

### The pre-commit lane-3 override invocation (D-02's settled mechanic, exact syntax)
```bash
# Source: tools/hooks/pre-commit:16-25 + tools/README-api-guard.md:9-12 (both read in full)
HUB_LANE_OVERRIDE=3 git commit -m "..."   # required for every commit touching the 4 affected
                                            # files, the registry, or api.txt in this phase
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|---------------|--------|
| `FilterBar` as a standalone PRIMITIVE sibling to `ChipBar` | `ChipBar` with an optional `expandable` mode | This phase (WO-1) | One fewer registered PRIMITIVE; `FilterBar` name retired from the public API |
| Duplicated header/menu/rename-dialog verbatim in both sheets | Single shared composable both sheets compose | This phase (WO-2), mirroring `CardQuickView`'s D-04 precedent for the body region | Real code dedup; both sheets get smaller |
| `v1.x` additive-only tag line (`v1.0.0`→`v1.10.0`, 10 tags, zero breaks — every `apiCheck` passed clean per `ECOSYSTEM.md`'s own release records) | First intentional break, `v2.0.0` | This phase | First time `apiCheck` is EXPECTED to fail pre-rebaseline — the rebaseline itself is the deliverable, not a bug |

**Deprecated/outdated:**
- `FilterBar` — retired as a standalone registered component (per WO-1's disposition); its behavior
  lives on inside `ChipBar`'s new `expandable` mode.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | The exact final signature/name for `ChipBar`'s new `expandable` param (`ExpandableConfig?`) and the new WO-2 shared composable's name/param order | Pattern 1, Pattern 2 (Code Examples) | Low — explicitly flagged in-line as "illustrative"/"planner's own naming call," not a locked contract; the audit itself calls this "real Phase-5 design work, not a mechanical merge" |
| A2 | Whether the new WO-2 shared composable should get its own standalone registered `Entry` vs. `INTENTIONALLY_UNREGISTERED` allowlisting | Pattern 2 (registry side) | Low-medium — either choice keeps the registered-XOR-allowlisted invariant satisfied (verified via `ComponentRegistry.kt`'s own `init` block, read this session); wrong choice only affects Explorer gallery discoverability, not correctness |
| A3 | `FilterBar`'s full retirement (delete outright) vs. demote-with-rationale to `INTENTIONALLY_UNREGISTERED` — CONTEXT.md's D-02 leaves this as a "per-fold Phase-5 design call" | Pattern 1 | Low — CONTEXT.md already flags this as open; this research documents both mechanical paths (delete `FilterBar.kt` entirely and its `Entry`, vs. keep the function `internal`/private and allowlist it) without picking one |

**All other claims in this research were verified by direct file reads or grep this session** — no
other user confirmation needed beyond the two items above, both already flagged as open design
calls in the locked CONTEXT.md itself.

## Open Questions

1. **Exact final param names/shape for `ChipBar`'s `expandable` mode and the WO-2 shared
   composable's name.**
   - What we know: the behavioral union of both source pairs (`FilterBar`+`ChipBar`,
     `TextCardBottomSheet`+`ListCardBottomSheet` header/menu/dialog) — read in full this session.
   - What's unclear: the audit itself defers this to Phase 5 as real design work, not a mechanical
     merge.
   - Recommendation: the planner should treat Pattern 1/Pattern 2's code sketches above as a
     starting point derived from the real signatures, adjustable during task execution — this is
     not a blocker to planning task structure (extract → wire callers → register → rebaseline is
     the same regardless of exact param names).

2. **Whether the new WO-2 shared composable needs its own dedicated source-contract test, or
   whether retargeting `TextListBottomSheetEditMenuSourceContractTest` at it is sufficient.**
   - What we know: the existing test's assertions (Edit-row branching, region-marker Edit/Rename
     copy count, rename-dialog structural presence) all currently target the exact code being
     moved.
   - What's unclear: whether the planner wants a net-new test class colocated with the new file
     (cleaner ownership) or an in-place retarget of the existing test's `source(file)` calls
     (smaller diff).
   - Recommendation: either satisfies the phase's success criteria; flag as a task-level
     implementation choice, not a planning blocker.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| `./gradlew` (Gradle wrapper) | apiDump/apiCheck/testDebugUnitTest/detekt | ✓ | project-pinned (AGP 9.2.1/Kotlin 2.3.20/JDK 17 per CLAUDE.md) | — |
| `git` | tag cut, pre-commit hook, `repin_status.py` (`git ls-remote --tags`) | ✓ | system | — |
| Network access to `jitpack.io` | tag-cut JitPack build verification (post-tag, human-gated step) | Not verified this session (no network probe run) | — | Prior 10 tag cuts all confirmed HTTP 200 per `ECOSYSTEM.md`'s own release records — treat as available by strong precedent |
| SecondBrain repo checkout (`~/Projects/SecondBrain`) | Blast-radius verification (informational only, out of scope for hub-side tasks) | ✓ | — | — |
| CalTracker_Android repo checkout (`~/Projects/CalTracker_Android`) | Same, informational | ✓ | — | — |

**Missing dependencies with no fallback:** None identified for the hub-side (autonomous) portion
of this phase.

**Missing dependencies with fallback:** JitPack reachability untested this session but has 100%
precedent across 10 prior tag cuts (`v1.0.0` through `v1.10.0`) per `ECOSYSTEM.md`'s own recorded
evidence — not re-verified here since the tag cut itself is human-gated and out of this phase's
autonomous file-edit scope.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric (`libs.robolectric`) + Compose UI-test-junit4/manifest, all already wired in `build.gradle.kts` |
| Config file | `build.gradle.kts` (module root — no separate test config file) |
| Quick run command | `./gradlew testDebugUnitTest --tests "*ComponentRegistry*"` (registry-scoped) or `--tests "*TextListBottomSheetEditMenuSourceContractTest*"` (WO-2-scoped) |
| Full suite command | `./gradlew testDebugUnitTest detekt apiCheck` |

### Phase Requirement → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| GARD-01 (WO-1) | `FilterBar` retired, folded into `ChipBar` | registry drift-guard + source-contract | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*"` | ✅ exists, needs no new file (registry-level assertion already generic) |
| GARD-01 (WO-2) | Sheets' shared header/menu/rename extracted, both sheets retained | source-contract (retargeted) | `./gradlew testDebugUnitTest --tests "*TextListBottomSheetEditMenuSourceContractTest*"` | ✅ exists but MUST be edited this phase (Pitfall 1) — not a Wave-0 gap, a required in-phase edit |
| GARD-01 | `ComponentRegistry` registered-XOR-allowlisted invariant holds post-unify | object-init `require()` (fires on any registry touch) + `ComponentRegistryDriftGuardTest` | `./gradlew testDebugUnitTest` | ✅ exists, self-enforcing |
| GARD-01 | `api.txt` matches the intentional post-unify public API | Metalava `apiCheck` | `./gradlew apiCheck` | ✅ tooling exists; `api.txt` itself needs the `apiDump` rebaseline this phase produces |
| GARD-02 | Tag cut + both consumers repinned, neither stranded | `repin_status.py reconcile` (Phase 4 tooling) + each consumer's own Gate-1 | `python3 ~/.claude/context/deps/repin_status.py reconcile --hub yahirandroidtaste` | ✅ tooling exists (Phase 4); execution is human-gated / runs post-tag |

### Sampling Rate
- **Per task commit (WO-1, WO-2 each):** `./gradlew testDebugUnitTest --tests "*ComponentRegistry*"`
  plus the directly-touched source-contract test class.
- **Per wave merge (after both WO-1 and WO-2 land, before rebaseline commit):**
  `./gradlew testDebugUnitTest detekt` (full suite, zero-baseline detekt per CLAUDE.md).
- **Phase gate (before the human-gated tag cut):** `./gradlew testDebugUnitTest detekt apiCheck` —
  `apiCheck` must be green against the freshly-committed rebaselined `api.txt`, proving the
  intentional break is fully captured, not a residual mismatch.

### Wave 0 Gaps
None — this phase's test infrastructure (Robolectric, Compose-UI-test, the source-contract
pattern, `ComponentRegistryDriftGuardTest`, Metalava `apiCheck`) is fully in place; no new
framework install or shared fixture is needed. The only required test EDIT (not a gap, a mandatory
in-scope change) is retargeting `TextListBottomSheetEditMenuSourceContractTest` per Pitfall 1.

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-------------------|
| V2 Authentication | No | Hub holds no auth/session state (invariant, CLAUDE.md) |
| V3 Session Management | No | Same |
| V4 Access Control | No | Same |
| V5 Input Validation | Marginal | `ClearableTextField` rename-dialog input already exists pre-phase, untouched in shape by this phase (only its host file changes); no new user input surface introduced |
| V6 Cryptography | No | Not applicable to this UI-library refactor |

### Known Threat Patterns for this stack

No new threat surface. This phase is a same-package Compose UI refactor (moving existing,
already-shipped rendering logic between files) plus a registry bookkeeping update — it introduces
no new network call, no new persisted data, no new external input parsing, and touches no secret
or credential material (hub holds no secrets, per CLAUDE.md's own invariant). The one
pre-existing, unrelated concern flagged in `.planning/STATE.md`'s "Blockers/Concerns" — the
`verify-api-additive.sh` absolute-vs-relative path bug making the lane-3 API-break pre-commit check
silently no-op — is a hub tooling defect predating this phase (traced to commit `534ec10`, before
Phase 3) and is out of this phase's scope to fix; note it for the planner as a known limitation
of the pre-commit safety net this phase relies on (the `apiCheck` Gradle task itself is unaffected
by that bug — it is a separate, working mechanism from the pre-commit hook's own additive-diff
check).

## Sources

### Primary (HIGH confidence — direct file reads this session)
- `component/FilterBar.kt` (113 lines, read in full)
- `component/ChipBar.kt` (65 lines, read in full)
- `component/TextCardBottomSheet.kt` (299 lines, read in full)
- `component/ListCardBottomSheet.kt` (423 lines, read in full)
- `explorer/ChipsFamilyScreen.kt` (446 lines, read in full)
- `explorer/SheetsFamilyScreen.kt` (partial read — header + `sheetsFamilyEntries` structure)
- `explorer/ComponentRegistry.kt` (154 lines, read in full)
- `src/test/.../explorer/ComponentRegistryDriftGuardTest.kt` (254 lines, read in full)
- `src/test/.../component/TextListBottomSheetEditMenuSourceContractTest.kt` (204 lines, read in full)
- `src/test/.../component/ListCardBottomSheetReadOnlyPreviewSourceContractTest.kt` (read in full)
- `src/test/.../component/SourceContractTestSupport.kt` (read in full)
- `api.txt` (1137 lines total; 8 lines grepped directly for the 4 affected symbols)
- `tools/hooks/pre-commit` (29 lines, read in full)
- `tools/README-api-guard.md` (89 lines, read in full)
- `build.gradle.kts` (Metalava/publishing sections read)
- `ECOSYSTEM.md` (494 lines, read in full — every prior tag-cut release record)
- `~/.claude/context/deps/repin_status.py` (351 lines, read in full)
- `docs/COHERENCE-AUDIT.md` (634 lines, read in full — the Unify Work-Order this phase implements)
- `.planning/phases/05-gardening-unify-coordinated-repin/05-CONTEXT.md` (locked decisions + Runtime Decisions)
- `.planning/phases/04-repin-bookkeeping-hardening/04-CONTEXT.md`
- `.planning/REQUIREMENTS.md`, `.planning/STATE.md`, `.planning/config.json`
- `~/Projects/SecondBrain/app/src/.../feature/browse/{BrowseScreen.kt,TagFilterBarContent.kt}` (`FilterBar` call sites, grepped + partially read)
- `~/Projects/SecondBrain/app/src/.../ui/component/CardListSection.kt` (`TextCardBottomSheet`/`ListCardBottomSheet` call sites, grepped)
- `~/Projects/SecondBrain/gradle/libs.versions.toml`, `~/Projects/CalTracker_Android/gradle/libs.versions.toml` (current pins, confirmed `v1.10.0` / `v1.5.0`)

### Secondary (MEDIUM confidence)
- None — this phase required no external web research; the entire domain is local-repo mechanics
  already fully documented in-repo.

### Tertiary (LOW confidence)
- None.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new dependencies, all tooling read directly from `build.gradle.kts`.
- Architecture (WO-1/WO-2 shapes): HIGH for the mechanical facts (current signatures, blast radius,
  registry structure); MEDIUM for the exact final new-composable signatures (explicitly flagged as
  open Phase-5 design work by the audit itself, not a research gap).
- Pitfalls: HIGH — Pitfall 1 (test breakage) was discovered by direct read of the actual test file
  and its exact string-anchor assertions, not inferred.

**Research date:** 2026-09-01
**Valid until:** Phase 5 execution (this is same-session-adjacent research for a bounded, already-
scoped 1:1 work order — no external-ecosystem drift risk; valid indefinitely until the hub's own
source files change again).
