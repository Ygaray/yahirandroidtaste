# Phase 2: Coherence Audit - Research

**Researched:** 2026-09-01
**Domain:** Internal code archaeology of a Compose design-system registry (no external
libraries/frameworks involved) — enumerating `ComponentRegistry`, applying the ratified D-03
litmus to find altitude mismatches, and pre-computing consumer blast radius via read-only grep.
**Confidence:** HIGH (every claim below is `[VERIFIED]` against a file this session actually
read — this phase requires no external documentation research, only exhaustive internal
reading, which was performed in full for the sections below).

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01 [tier-source]:** consume Phase 1's ratified `entries[i].tier` labels + the
  `DESIGN-INTENT.md` litmus to name "altitude mismatch," rather than re-deriving tier ad hoc per
  component — re-deriving rests findings on an unratified taxonomy that can contradict the
  shipped gallery badge and makes the finding unfalsifiable. _(provisional — refresh at
  execution; depends on Phase 1)_ _(source: ai-auto)_

- **D-02 [blast-radius]:** for each "unify" finding, pre-compute consumer blast radius by
  read-only grep of both on-disk consumer repos (`~/Projects/SecondBrain`,
  `~/Projects/CalTracker_Android`) now, recording call-sites per unify — read-for-blast-radius is
  sanctioned (only *editing* consumer files violates sequential-in-hub), and it gives Phase 5 a
  pre-flight against the documented stranded-consumer failure mode. _(source: ai-auto)_

**Runtime Decisions (refreshed during milestone execution, tier-source confirmed real at HEAD
`464af01`):** the audit consumes Phase 1's ratified per-entry `tier` labels (all 53
`ComponentRegistry.Entry` sites) and `docs/DESIGN-INTENT.md`'s D-03 litmus to name "altitude
mismatch" — it does **not** re-derive tier judgements. This research independently re-confirmed
both facts this session (see `## Sources`).

### Claude's Discretion

No specific requirements — open to standard approaches (per CONTEXT.md `<specifics>`).

### Deferred Ideas (OUT OF SCOPE)

Call-site *editing* / actual unification is deferred to Phase 5 — this phase only records
call-sites for blast-radius pre-flight.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| AUD-01 | A coherence audit enumerates the 9 families and flags overlaps, near-duplicate sibling components, and altitude mismatches, with a documented disposition (unify / keep-with-rationale / prune) for each finding. | `## Code Examples` gives the verified 9-family enumeration + full 53-entry name/tier list to walk; `## Common Pitfalls` (1-5) and the near-duplicate-sibling/overlap code examples give real seed findings (Cards 5-way pinned-card shape, Chips AppChip/TagChipWithContextMenu, ChipBar/FilterBar, Tactile Foundation Swatch pair) so family-audit tasks aren't starting from a blank template; `## Validation Architecture` gives the structural grep checks that verify all 4 ROADMAP success criteria are met before phase gate. |

</phase_requirements>

## Summary

This phase produces one documentation deliverable, `docs/COHERENCE-AUDIT.md`, with zero code
changes. All the raw material the audit needs already exists in the repo and was read this
session: `ComponentRegistry.kt` (the object + its 9 concatenated family lists), the 9 individual
family-screen files that declare each list, and `docs/DESIGN-INTENT.md` (the ratified tier
contracts + D-03 litmus). The registry currently holds exactly **53 registered entries across 9
families** (confirmed by direct count, matching `ComponentRegistry.kt`'s own doc-comment claim),
plus 4 `INTENTIONALLY_UNREGISTERED` sub-parts that sit outside the tier taxonomy entirely and are
out of this audit's scope.

Both consumer repos referenced by CONTEXT.md's D-02 blast-radius decision exist on disk and are
genuinely wired to the hub via Gradle version-catalog aliases (not stale/vestigial deps) —
SecondBrain pinned to hub tag `v1.10.0`, CalTracker_Android pinned to `v1.5.0`. Grep against both
is proven feasible: SecondBrain alone has 52 call sites for `TextCard` and real hits for `AppChip`,
`ChipBar`, etc.; CalTracker consumes a narrow, almost entirely non-overlapping slice (Progress/
Metrics + Feedback + `RevealActionRow`) — meaning per-finding blast radius will vary sharply by
family and some "unify" findings will show zero CalTracker exposure while others (`AnimatedStatValue`,
`ConfirmationDialog`, `HeroStatCard`, `ProgressRing`, `EmptyState`, `RevealActionRow`) touch both
consumers and deserve the closest pre-flight attention in Phase 5.

A first-pass skim (this session, not exhaustive) already surfaced concrete, real candidate
findings — not hypothetical ones — in the Cards, Chips, and Tactile Foundation families, detailed
in `## Common Pitfalls` and `## Code Examples` below, giving the planner genuine seed material
instead of an empty template to fill from scratch.

**Primary recommendation:** Structure `docs/COHERENCE-AUDIT.md` as one section per family (9
total), each walking that family's entries verbatim from the family-screen file (never
re-deriving tier), flagging candidates against the three finding types, then a final
"Unify Work-Order" section that aggregates every "unify" disposition with its
pre-computed consumer call-site counts — this final section is what Phase 5 (Gardening) consumes
1:1.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Enumerate 9 families + entries | Docs (this repo) | — | Pure read of `ComponentRegistry.kt` + 9 family-screen files; no runtime tier involved |
| Name overlaps / near-duplicate siblings | Docs (this repo) | Library / Component (`component/` package) | The audit *names* the finding here; any eventual code merge is Phase 5, in the library's `component/` package |
| Altitude (tier) mismatch detection | Docs (this repo) | Library (`ComponentRegistry.Entry.tier`) | Audit compares each entry's *shipped* `tier` value (already ratified, Phase 1) against the D-03 litmus applied to its real public signature — a documentation judgment, not a code change |
| Consumer blast-radius pre-compute | Docs (this repo), read-only against 2 external repos | — | Read-only grep against `~/Projects/SecondBrain` and `~/Projects/CalTracker_Android` on-disk trees — no writes to either (sequential-in-hub convention) |
| Unify work-order for Phase 5 | Docs (this repo) | — | The audit's final section *is* Phase 5's task list; Phase 5 (a future phase) is the tier that will act on it |

## Package Legitimacy Audit

**N/A — this phase installs no external packages.** It is a read/analyze/document-only phase
(per CONTEXT.md's Phase Boundary): no `build.gradle.kts` changes, no new Gradle dependency, no
Kotlin/Compose code written. The Package Legitimacy Gate does not apply.

## Standard Stack

**N/A — no libraries are introduced by this phase.** The audit is authored as a single Markdown
file (`docs/COHERENCE-AUDIT.md`) alongside the existing `docs/DESIGN-INTENT.md`, `API.md`,
`INTEGRATION.md`, `ECOSYSTEM.md` — same format, same location convention, no tooling required
beyond `Read`/`grep`/`Write`.

## Architecture Patterns

### System Architecture Diagram

```
                     ComponentRegistry.kt (root aggregator)
                             │
   ┌───────────┬───────────┬┴──────────┬────────────┬────────────┬───────────┬──────────────┬────────────────────┐
   │           │           │            │            │            │           │              │                    │
cardsFamily  chipsFamily sheetsFamily buttonsFab  pickersFamily feedbackFamily emptyState  progressFamily  tactileFoundation
Entries      Entries     Entries      Entries     Entries       Entries       Entries     Entries         FamilyEntries
(11)         (5)         (18)         (3)         (4)           (3)           (1)         (4)             (4)
   │           │           │            │            │            │           │              │                    │
   └───────────┴───────────┴────────────┴────────────┴────────────┴───────────┴──────────────┴────────────────────┘
                             │  (declared in each family-screen .kt file, D-05, Phase 62)
                             ▼
                  ComponentRegistry.entries  (53 total, flat list, tier-labeled)
                             │
              ┌──────────────┴───────────────────┐
              ▼                                   ▼
   docs/DESIGN-INTENT.md D-03 litmus      This audit walks entries
   (2-question test: domain noun? /       per family, applies the
    caller-content-only?) — decides       litmus to each entry's real
    PRIMITIVE vs PATTERN                  signature, compares vs
                                          shipped `tier`, and separately
                                          scans for cross-entry overlap /
                                          near-duplicate-sibling shape
                                                    │
                                                    ▼
                                    docs/COHERENCE-AUDIT.md
                                    (9 family sections, each finding
                                     dispositioned unify / keep / prune)
                                                    │
                                                    ▼
                              "unify" findings × blast-radius grep against
                              ~/Projects/SecondBrain (pinned v1.10.0) and
                              ~/Projects/CalTracker_Android (pinned v1.5.0)
                                                    │
                                                    ▼
                                    Phase 5 (Gardening) work-order
                                    — consumed 1:1, out of this phase's scope
```

### Recommended Project Structure

No new files/folders beyond the one deliverable:
```
docs/
├── DESIGN-INTENT.md        # existing — Phase 1, the tier contracts + litmus this audit cites
└── COHERENCE-AUDIT.md      # NEW — this phase's sole deliverable
```

### Pattern 1: Family-by-family audit walk (don't re-derive tier)
**What:** For each of the 9 families, read its `*FamilyEntries` list from its own
`*FamilyScreen.kt` file (never from `ComponentRegistry.kt`'s concatenation, which has no
per-entry detail), and record each entry's `name` + shipped `tier` verbatim.
**When to use:** Every family section of the audit — this is the enumeration backbone required
by AUD-01's "enumerates all 9 registered families."
**Example (real, from this session):**
```kotlin
// Source: ChipsFamilyScreen.kt:56-256 [VERIFIED]
internal val chipsFamilyEntries: List<ComponentRegistry.Entry> = listOf(
    ComponentRegistry.Entry(name = "AppChip", ..., tier = ComponentRegistry.Tier.PATTERN),
    ComponentRegistry.Entry(name = "TagChipWithContextMenu", ..., tier = ComponentRegistry.Tier.PATTERN),
    ComponentRegistry.Entry(name = "ChipBar", ..., tier = ComponentRegistry.Tier.PRIMITIVE),
    ComponentRegistry.Entry(name = "SortControl", ..., tier = ComponentRegistry.Tier.PATTERN),
    ComponentRegistry.Entry(name = "FilterBar", ..., tier = ComponentRegistry.Tier.PRIMITIVE)
)
```

### Pattern 2: Applying the D-03 litmus per candidate (not per family)
**What:** For each candidate flagged as a possible altitude mismatch, re-state the entry's real
public signature (not its usage) and answer both D-03 questions explicitly in the audit prose —
mirroring the "Worked Examples" section of `docs/DESIGN-INTENT.md`.
**When to use:** Only for entries the skim (or the planner's fuller pass) actually suspects of a
tier/litmus mismatch — not every entry needs this treatment, only flagged ones. Note that the
codebase already contains one *resolved* precedent of exactly this exercise — see Pitfall 1 below
— which the audit should cite as the model to imitate, not re-litigate.

### Pattern 3: Blast-radius grep recipe (D-02)
**What:** For every "unify" disposition, run a read-only grep for the entry's exact composable
name against both consumer trees and record the file list + count.
**When to use:** Only for "unify" findings — "keep-with-rationale" and "prune" dispositions do
not need blast radius (prune's blast radius matters more in Phase 5 when it actually happens).
**Example (real command + real output, this session):**
```bash
# Source: this research session, executed against live on-disk repos [VERIFIED]
grep -rl "TextCard" ~/Projects/SecondBrain/app/src 2>/dev/null | wc -l
# -> 52
grep -rl "TextCard\|AppChip\|CardBase" ~/Projects/CalTracker_Android/app/src 2>/dev/null | wc -l
# -> 0   (CalTracker does not touch the Cards or Chips families at all)
```

### Anti-Patterns to Avoid
- **Re-deriving tier ad hoc:** CONTEXT.md's D-01 explicitly forbids this — always cite the
  shipped `tier` value from the family-screen file, then argue *against* it if you believe it's
  wrong; never invent a fresh judgment call disconnected from the ratified value.
- **Grepping only by exact composable name for blast radius:** a consumer may alias the import
  (`import ... as X`) or reference it only via a wrapper composable. A single-name grep is a
  reasonable default (per D-02) but the audit should note this limitation inline rather than
  presenting the count as exhaustive.
- **Treating the Sheet+Content split as a duplicate:** `TagPickerSheet`/`TagPickerSheetContent`,
  `BulkCreatePopup`/`BulkCreatePopupContent`, `TagCreateSheet`/`TagCreateSheetContent` are an
  established, intentional shell/body split (a modal wrapper composable + its content-only
  variant, both independently registered) — not a near-duplicate-sibling finding. See Pitfall 3.

## Don't Hand-Roll

Not applicable in the conventional sense (no library/problem-domain code is being written this
phase). The one relevant "don't hand-roll" is procedural: **don't hand-roll the tier taxonomy.**
Use the ratified `Entry.tier` values and the D-03 litmus verbatim (Pattern 2 above) rather than
inventing new criteria for "overlap" or "near-duplicate" — CONTEXT.md's D-01 rationale
(unfalsifiable findings if the taxonomy isn't ratified) applies equally to any home-grown overlap
heuristic; ground every finding in the entry's real public signature, read from source.

## Common Pitfalls

### Pitfall 1: Assuming every borderline tier assignment is still unresolved
**What goes wrong:** Treating every domain-noun-adjacent component as an open altitude-mismatch
finding, duplicating work Phase 1 already did.
**Why it happens:** The registry doesn't visibly show *why* a tier was chosen — only the final
value — so it's tempting to re-litigate every borderline case from scratch.
**How to avoid:** `ChipsFamilyScreen.kt:97-102` `[VERIFIED]` already documents one resolved case
inline: `AppChip` was originally miscategorized PRIMITIVE and was corrected to PATTERN during
Phase 1 ("WR-01 fix") because its `relatednessStrength` param bakes in the hub's own
"Relatedness" domain vocabulary and a computed visual encoding — the exact reasoning
`docs/DESIGN-INTENT.md`'s `HeatSwatch` worked example uses. Cite this as evidence Phase 1 already
swept obvious mismatches; the audit should focus on *cross-entry* overlap/near-duplicate findings
(which Phase 1's per-entry pass could not have caught, since it worked one entry at a time) rather
than re-scanning every entry's individual tier.
**Warning signs:** A "finding" that only restates the shipped tier without citing anything Phase
1 missed — that's not a finding, it's a re-verification, and doesn't belong in the audit.

### Pitfall 2: Conflating the two different "litmus" documents in this repo
**What goes wrong:** Citing `ECOSYSTEM.md`'s litmus when the audit means `docs/DESIGN-INTENT.md`'s
D-03 litmus, or vice versa — they answer different questions and use overlapping vocabulary.
**Why it happens:** Both documents use the word "litmus" and both mention "domain noun[s]."
**How to avoid:** `ECOSYSTEM.md:413-419` `[VERIFIED]` states: `"Could a different app render this
with zero domain assumptions?"` — this decides **hub vs. consumer** placement (does a component
belong in this library at all). `docs/DESIGN-INTENT.md`'s two-question D-03 litmus (quoted
verbatim in `## Code Examples` below) decides **PRIMITIVE vs. PATTERN tier** *within* the hub —
an entirely different, narrower question that only applies to components already inside the
registry. AUD-01 (this phase) uses **only** the D-03 litmus; `ECOSYSTEM.md`'s litmus is
irrelevant here (every entry the audit walks is already inside the hub by definition — it's in
`ComponentRegistry.entries`).
**Warning signs:** The audit draft asks "could a different app render this?" for any entry — that
question was already answered "yes" the moment the component was registered.

### Pitfall 3: Treating a same-family naming pattern as a duplicate without reading both signatures
**What goes wrong:** Flagging `GradientSwatch` and `HeatSwatch` (both `TACTILE_FOUNDATION`
family, both PATTERN tier, both named `*Swatch`) as near-duplicate siblings on name-similarity
alone.
**Why it happens:** Superficial family/tier/naming match is easy to spot in the registry listing
and looks exactly like a duplicate at a glance.
**How to avoid:** Read the actual signatures first — `docs/DESIGN-INTENT.md`'s own worked example
(`HeatSwatch -> PATTERN`) states *why* it's PATTERN: `"Takes no caller-supplied content — it
hardcodes its own sample data and renders a specific visual convention (the Heat relatedness
ramp) targeting mindmap nodes/edges."` `[VERIFIED: docs/DESIGN-INTENT.md:64-66]` `GradientSwatch`
(`TactileFoundationFamilyScreen.kt:75-80` `[VERIFIED]`) takes a caller-supplied `accentColor:
Color` and renders a generic accent-gradient ramp — a different, more reusable shape than
`HeatSwatch`'s hardcoded mindmap-specific data. These likely disposition as
**keep-with-rationale** (same family, same naming convention, genuinely different purposes), not
unify — but confirm by reading both files' full bodies before writing the disposition, don't
decide from the registry listing alone.
**Warning signs:** A disposition rationale that cites only "similar name" or "same family" without
quoting each component's actual signature/behavior.

### Pitfall 4: Assuming uniform consumer exposure across all "unify" candidates
**What goes wrong:** Running one blast-radius grep pass and generalizing "the consumers use N%
of the hub" instead of recording per-finding numbers, understating risk for widely-used entries
and overstating it for narrow ones.
**Why it happens:** It's tempting to sample a couple of components and extrapolate.
**How to avoid:** This session's own spot-check proves consumption is highly uneven by family:
SecondBrain imports `TextCard` in 52 files; CalTracker imports zero Cards-family or Chips-family
symbols at all — its entire hub surface is `AnimatedStatValue`, `ConfirmationDialog`,
`ConfirmationDialogDefaults`, `EmptyState`, `HeroStatCard`, `ProgressRing`,
`FeedbackDispatcher`/`FeedbackEvent`/`LocalFeedbackController`/`UndoHistoryStore`/`UndoStatus`,
`RevealActionRow`/`RevealAnchor`, and theme tokens (`Dimens`, `expressive`, `ExpressiveMotion`,
`ThemeMode`, `YahirAndroidTasteTheme`) `[VERIFIED: grep of ~/Projects/CalTracker_Android/app/src
this session]`. Any "unify" finding touching Cards/Chips/Sheets/Pickers/Buttons-FAB/Tactile-
Foundation families is very likely SecondBrain-only exposure; any finding touching
Progress/Metrics or Feedback families needs both-consumer pre-flight.
**Warning signs:** A blast-radius note that says "used by consumers" without a per-repo count.

### Pitfall 5: Trusting `ComponentRegistry.kt`'s own doc-comment count without recounting
**What goes wrong:** Citing "seven family lists" (the phrasing still present in
`ComponentRegistry.kt`'s own KDoc, e.g. line 92: `"the seven per-family lists below"`) when the
concatenation on lines 94-102 actually sums **9** lists.
**Why it happens:** The doc-comment predates two later family additions (Progress/Metrics,
Tactile Foundation — see `ECOSYSTEM.md`'s extraction history) and was never updated; it's a real,
pre-existing piece of doc drift, though outside AUD-01's component-overlap scope so not a
"finding" in the audit's own sense.
**How to avoid:** Count the `+`-joined list literally (`ComponentRegistry.kt:94-102`
`[VERIFIED]`): `cardsFamilyEntries + chipsFamilyEntries + sheetsFamilyEntries +
buttonsFabFamilyEntries + pickersFamilyEntries + feedbackFamilyEntries +
emptyStateFamilyEntries + progressFamilyEntries + tactileFoundationFamilyEntries` = 9 terms.
Cross-checked against `ExplorerFamilies.ORDERED_KEYS` (`ExplorerIndexScreen.kt:67-77`
`[VERIFIED]`), which also lists exactly 9 `(key, label)` pairs. CONTEXT.md's own code-context
section already hedges this as "seven/nine" — resolve it to **9** in the audit, citing both
sources.
**Warning signs:** Any audit sentence that says "seven families" — the current, correct number is 9.

## Code Examples

### The 9 families, their registry entry counts, and family display labels
```text
// Source: ExplorerIndexScreen.kt:55-76 (ExplorerFamilies object) [VERIFIED]
// + per-family *FamilyScreen.kt files (entry counts by direct grep/read, this session) [VERIFIED]

Registry key            Display label         File declaring the list                  Entry count
────────────────────────────────────────────────────────────────────────────────────────────────
CARDS                   "Cards"                CardsFamilyScreen.kt:83-282ish              11
CHIPS                   "Chips"                ChipsFamilyScreen.kt:56-256                  5
SHEETS                  "Sheets"               SheetsFamilyScreen.kt:75-...                18
BUTTONS_FAB             "Buttons / FAB"        ButtonsFabFamilyScreen.kt:37-...             3
PICKERS                 "Pickers"              PickersFamilyScreen.kt:38-...                4
FEEDBACK                "Feedback"             FeedbackFamilyScreen.kt:46-...               3
EMPTY_STATE             "Empty State"          EmptyStateFamilyScreen.kt:29-...             1
PROGRESS_METRICS        "Progress / Metrics"   ProgressFamilyScreen.kt:36-...               4
TACTILE_FOUNDATION      "Tactile Foundation"   TactileFoundationFamilyScreen.kt:35-112       4
────────────────────────────────────────────────────────────────────────────────────────────────
                                                                              TOTAL:         53
```
`ExplorerFamilies.ORDERED_KEYS` (`ExplorerIndexScreen.kt:67-76` `[VERIFIED]`) declares the
canonical iteration order the audit should follow (matches the table above top-to-bottom):
```kotlin
val ORDERED_KEYS: List<Pair<String, String>> = listOf(
    CARDS to "Cards",
    CHIPS to "Chips",
    SHEETS to "Sheets",
    BUTTONS_FAB to "Buttons / FAB",
    PICKERS to "Pickers",
    FEEDBACK to "Feedback",
    EMPTY_STATE to "Empty State",
    PROGRESS_METRICS to "Progress / Metrics",
    TACTILE_FOUNDATION to "Tactile Foundation"
)
```

### Every entry name + shipped tier, all 9 families (full enumeration, this session's direct read)
```text
// Source: <Family>FamilyScreen.kt name="..."/tier=ComponentRegistry.Tier.* pairs, parsed from
// each file's literal `ComponentRegistry.Entry(...)` blocks this session. [VERIFIED]

CARDS (11):
  CardBase               PATTERN     CardTypeChip           PATTERN
  TextCard               PATTERN     ListCard               PATTERN
  AlbumCard              PATTERN     VoiceCard              PATTERN
  AdaptiveMediaPreview   PATTERN     CardTagRow             PATTERN
  CardQuickView          PATTERN     CountBadge             PRIMITIVE
  TagListItem            PATTERN

CHIPS (5):
  AppChip                PATTERN     TagChipWithContextMenu PATTERN
  ChipBar                PRIMITIVE   SortControl            PATTERN
  FilterBar              PRIMITIVE

SHEETS (18):
  AlbumSourcePickerSheet PATTERN     AlbumTitleConfirmSheet PATTERN
  CardEditorShellContent PATTERN     ListCardBottomSheet    PATTERN
  RecordingBottomSheetContent PATTERN  SheetScaffold        PRIMITIVE
  TagChipEditorContent   PATTERN     TagPickerSheetContent  PATTERN
  TextCardBottomSheet    PATTERN     TagPickerSheet         PATTERN
  BulkCreatePopup        PATTERN     BulkCreatePopupContent PATTERN
  NameAndTagsEditor      PATTERN     TagCreateSheet         PATTERN
  TagCreateSheetContent  PATTERN     VoiceRenameTagsSheet   PATTERN
  ClearableTextField     PRIMITIVE   EditorItemRow          PATTERN

BUTTONS_FAB (3):
  ExpandableFab          PATTERN     CycleSubTypeButton     PATTERN
  DynamicActionButton    PATTERN

PICKERS (4):
  AccentColorPicker      PATTERN     IconPickerGrid         PATTERN
  CropOverlay            PATTERN     SegmentedOptionSelector PRIMITIVE

FEEDBACK (3):
  ConfirmationDialog     PRIMITIVE   UndoCenterScreen       PATTERN
  AttentionCue           PRIMITIVE

EMPTY_STATE (1):
  EmptyState             PRIMITIVE

PROGRESS_METRICS (4):
  MetricBar              PATTERN     ProgressRing           PATTERN
  AnimatedStatValue      PRIMITIVE   HeroStatCard           PATTERN

TACTILE_FOUNDATION (4):
  ElevationLadder        PATTERN     TactileTypeShowcase    PATTERN
  GradientSwatch         PATTERN     HeatSwatch             PATTERN

Tier tally: 10 PRIMITIVE, 43 PATTERN, 53 total.
```

### `docs/DESIGN-INTENT.md`'s D-03 litmus, verbatim (cite exactly — do not paraphrase)
```text
// Source: docs/DESIGN-INTENT.md:39-52 [VERIFIED]

The test is decidable, not adjective-based — "simpler" vs. "more opinionated" is exactly the
kind of test that lets the same component be tiered two ways, which is why this litmus asks two
yes/no questions instead:

1. Does the name or any parameter introduce a domain noun (e.g. "Card", "Tag", "Voice",
   "Album")?
2. Does the component render only caller-passed content, with no baked-in interaction
   convention (swipe-to-reveal, modal-chrome pattern, etc.) or composition opinion of its own?

PRIMITIVE = "no" to (1) AND "yes" to (2).
PATTERN = "yes" to (1) OR "no" to (2) — either condition alone is enough to make it a
pattern.
```
The three worked examples (`docs/DESIGN-INTENT.md:54-66` `[VERIFIED]`) — `CardBase -> PATTERN`
(fails condition 2, bakes in reveal-confirm swipe via `SwipeableActionRow`), `ChipBar ->
PRIMITIVE` (fully generic `<T>`, KDoc states it "holds no chip-rendering opinions"), `HeatSwatch
-> PATTERN` (fails condition 2, hardcodes its own sample data) — are the audit's model for how to
write up any newly-flagged borderline case.

### Near-duplicate-sibling candidate: the 5-way Cards family pinned-toggle shape
```kotlin
// Source: CardsFamilyScreen.kt:120-251 (TextCard/ListCard/AlbumCard/VoiceCard/CardQuickView
// Entry declarations) [VERIFIED] + component source files confirming CardBase composition:
//   TextCard.kt:177 `CardBase(` [VERIFIED]
//   ListCard.kt:175 `CardBase(` [VERIFIED]
//   AlbumCard.kt:139 `CardBase(` [VERIFIED]
//   VoiceCard.kt:462 `CardBase(` [VERIFIED]
// (CardQuickView's own composition was not individually re-verified against CardBase this
// session — its registry shape matches the other four's isPinned-toggle pattern, but the audit
// should confirm its component source before dispositioning it alongside the other four.)

private val textCardPinnedControl = Control.Toggle(label = "Pinned")
private val listCardPinnedControl = Control.Toggle(label = "Pinned")
private val albumCardPinnedControl = Control.Toggle(label = "Pinned")
private val voiceCardPinnedControl = Control.Toggle(label = "Pinned")
// four near-identical Control.Toggle declarations, one per card type, all labeled "Pinned"
```
`TextCard`, `ListCard`, `AlbumCard`, `VoiceCard` are confirmed (by reading each component's own
source file) to all wrap `CardBase` and all expose an identically-shaped `isPinned` toggle in
their registry `Entry` (same control label, same States-matrix shape: Default/Pinned as the only
two non-N/A cells). This is real seed material for a "near-duplicate sibling" finding — whether
the disposition is unify (e.g., parameterize `CardBase` callers around a shared content-type enum)
or keep-with-rationale (each renders genuinely different domain content: text/list/album/voice)
is a judgment call for the audit's author, but the *shape* overlap is verified fact, not a guess.

### Overlap candidate: `ChipBar` vs. `FilterBar` (both PRIMITIVE, both generic containers)
```kotlin
// Source: ChipsFamilyScreen.kt:148-171 (ChipBar) and :213-256 (FilterBar) [VERIFIED]
ChipBar(
    items = ExplorerFakeData.manyTagChips,
    key = { it.id },
    itemContent = { tag -> AppChip(label = tag.name, isSelected = false, onClick = {}) },
    modifier = Modifier.padding(horizontal = 16.dp)
)
// ...
FilterBar<TagChipUiModel>(
    expanded = false,
    onExpand = {},
    onCollapse = {}
) {
    ExplorerFakeData.tagChips.forEach { tag -> AppChip(label = tag.name, isSelected = false, onClick = {}) }
}
```
Both are fully generic (`<T>`), both PRIMITIVE, both render caller-supplied chip-shaped content in
a row/wrap layout — `ChipBar` is a plain items-list container; `FilterBar` adds an
expand/collapse affordance around the same shape of content. Real candidate for "does FilterBar's
expand/collapse belong as a `ChipBar` mode instead of a sibling primitive?" — seed material, not a
pre-decided disposition.

### Sheet+Content split — confirm as intentional pattern, not a duplicate (seed for Pitfall 3's sibling)
```text
// Source: SheetsFamilyScreen.kt name list [VERIFIED] — pairs sharing a base name:
TagPickerSheet / TagPickerSheetContent
BulkCreatePopup / BulkCreatePopupContent
TagCreateSheet / TagCreateSheetContent
```
Each pair is independently registered (6 separate entries, not 3) — the `*Sheet`/`*Popup` member
is the modal-chrome wrapper, the `*Content` member is the same content composable usable outside
a sheet context. Confirm this reading against each pair's actual signatures before writing the
audit entry; if confirmed, this is exactly the shape "keep-with-rationale" exists for.

## State of the Art

Not applicable — this is a from-scratch audit of an internal, versioned registry (no external
ecosystem "current approach" vs. "old approach" axis; the registry itself has no prior audit to
compare against — Phase 2 is the first coherence audit this hub has had).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `CardQuickView`'s tier/shape (isPinned+isFavorite toggle pair) mirrors the other 4 Cards siblings closely enough to include in the same near-duplicate-sibling finding | Code Examples → "5-way Cards family" | Low — the audit author reads `CardQuickView`'s actual source before finalizing the disposition (flagged explicitly in that example as unverified against `CardBase`); worst case the finding narrows from 5 to 4 siblings |
| A2 | A single-name grep (`grep -rl "<ComponentName>"`) is an adequate blast-radius proxy, per D-02's own text ("grep of both on-disk consumer repos") | Pattern 3 / Anti-Patterns | Low-Medium — could undercount if a consumer aliases an import; the audit should note the count as a lower bound, not exhaustive |

**If this table is empty:** N/A — two low-risk assumptions logged above, both self-mitigating
(the audit-writing step itself re-reads the flagged source before finalizing).

## Open Questions

1. **Should `CardQuickView` be folded into the same "5-way pinned-card" finding as
   TextCard/ListCard/AlbumCard/VoiceCard, or treated as its own finding?**
   - What we know: its registry `Entry` shape (isPinned + isFavorite `Control.Toggle` pair,
     `CardQuickViewContent(...)` states matching the other four's Default/Pinned pattern) looks
     identical in shape.
   - What's unclear: whether its component source actually composes `CardBase` the same way (not
     re-verified this session, unlike the other four which were explicitly confirmed).
   - Recommendation: the audit-writing task should `Read` `CardQuickView.kt` before finalizing
     this finding's scope — a 10-second check, not a research gap that blocks planning.

2. **Does the D-02 blast-radius grep need to run against CalTracker's on-disk source (pinned
   v1.5.0) even for hub components added to the registry after v1.5.0 was cut?**
   - What we know: CalTracker's Gradle pin is `v1.5.0`; SecondBrain's is `v1.10.0` — CalTracker's
     on-disk tree reflects whatever component surface existed at that older tag, not current HEAD.
   - What's unclear: whether any "unify" candidate involves a component that didn't exist yet at
     `v1.5.0` (in which case a zero-hit CalTracker grep genuinely means "not yet available to
     that consumer," not "not used").
   - Recommendation: the audit should note each "unify" finding's target component's approximate
     introduction point (family-screen file's own phase-history comments, e.g. Tactile Foundation
     entries carry "Phase 123" provenance) alongside the grep result, so a zero-hit CalTracker
     count reads correctly as "too new for that pin" vs. "genuinely unused."

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| `~/Projects/SecondBrain` (on-disk repo) | D-02 blast-radius grep | Yes | hub pin `v1.10.0` (`gradle/libs.versions.toml:37`) `[VERIFIED]` | — |
| `~/Projects/CalTracker_Android` (on-disk repo) | D-02 blast-radius grep | Yes | hub pin `v1.5.0` (`gradle/libs.versions.toml:73`) `[VERIFIED]` | — |
| `grep` | blast-radius pre-compute | Yes (standard shell tool) | — | — |

**Missing dependencies with no fallback:** none.
**Missing dependencies with fallback:** none — both required consumer trees are present and
genuinely wired (real Gradle dependency lines, real call sites), confirmed by direct grep this
session (SecondBrain: 52 `TextCard` call-sites; CalTracker: 18 distinct hub symbols imported,
concentrated in Progress/Metrics + Feedback + `RevealActionRow`/theme tokens).

## Validation Architecture

This is a documentation-only phase (`workflow.nyquist_validation: true` in `.planning/config.json`
`[VERIFIED]`, but no Kotlin/Compose code is written — there is no unit-test framework to invoke).
"Validation" here means structural verification that the produced `docs/COHERENCE-AUDIT.md`
actually satisfies AUD-01's four success criteria, run as `grep`/manual-read checks against the
doc itself, not an automated test suite.

### Test Framework
| Property | Value |
|----------|-------|
| Framework | None — plain-text structural verification (grep against the produced `.md`) |
| Config file | none |
| Quick run command | `grep -c "^### " docs/COHERENCE-AUDIT.md` (expect >= 9, one heading per family, plus any cross-family findings section) |
| Full suite command | manual read-through against the 4 success criteria in ROADMAP.md §Phase 2 |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| AUD-01 | All 9 families enumerated | structural grep | `for f in Cards Chips Sheets "Buttons / FAB" Pickers Feedback "Empty State" "Progress / Metrics" "Tactile Foundation"; do grep -q "$f" docs/COHERENCE-AUDIT.md || echo "MISSING: $f"; done` | ❌ Wave 0 (doc doesn't exist yet) |
| AUD-01 | Every finding carries a disposition | structural grep | `grep -c "unify\|keep-with-rationale\|prune" docs/COHERENCE-AUDIT.md` (manually cross-check count matches number of findings raised) | ❌ Wave 0 |
| AUD-01 | "unify" findings form a Phase 5 work-order | manual read | N/A — human/planner read of the final "Unify Work-Order" section | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** re-read the family section just written against its source file's entry
  list, confirm no entry/tier was silently dropped or mis-transcribed.
- **Per wave merge:** N/A — this phase is small enough to be a single wave/single plan
  candidate (one deliverable file); if split across plans, re-run the family-enumeration grep
  after each merge.
- **Phase gate:** the three structural greps above, green, before `/gsd-verify-work`.

### Wave 0 Gaps
- [ ] `docs/COHERENCE-AUDIT.md` does not exist yet — created by this phase's own execution.
- [ ] No test framework install needed — plain Markdown + grep verification only.

*(No pytest/jest-equivalent gaps: this phase produces no executable code.)*

## Security Domain

`security_enforcement: true` in `.planning/config.json` `[VERIFIED]`, but this phase touches no
runtime code path, no auth/session/crypto/input-handling surface, and makes no network calls — it
reads existing source files and writes one Markdown file. All ASVS categories are N/A.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-------------------|
| V2 Authentication | No | — |
| V3 Session Management | No | — |
| V4 Access Control | No | — |
| V5 Input Validation | No | — |
| V6 Cryptography | No | — |

### Known Threat Patterns for {stack}
None applicable — no attack surface is created or modified by a documentation-only phase.

## Sources

### Primary (HIGH confidence — all `[VERIFIED]`, read this session)
- `ComponentRegistry.kt` (full file, 153 lines) — the `Entry`/`Tier` data model, the 9-list
  concatenation, `INTENTIONALLY_UNREGISTERED`.
- `CardsFamilyScreen.kt`, `ChipsFamilyScreen.kt` (both read in full for entries section),
  `SheetsFamilyScreen.kt` (entries section + full name list),
  `ButtonsFabFamilyScreen.kt`/`PickersFamilyScreen.kt`/`FeedbackFamilyScreen.kt`/
  `EmptyStateFamilyScreen.kt`/`ProgressFamilyScreen.kt`/`TactileFoundationFamilyScreen.kt`
  (name+tier extracted via targeted read/grep) — the 9 family entry-list declarations.
- `ExplorerIndexScreen.kt` (`ExplorerFamilies` object, lines 55-77) — canonical family
  keys/labels/order.
- `docs/DESIGN-INTENT.md` (full file, 75 lines) — tier contracts + D-03 litmus, verbatim.
- `ECOSYSTEM.md` (§4-6, lines 400-441) — the *different* hub-vs-consumer litmus (Pitfall 2).
- `TextCard.kt:177`, `ListCard.kt:175`, `AlbumCard.kt:139`, `VoiceCard.kt:462` — confirmed each
  wraps `CardBase(`.
- `~/Projects/SecondBrain/gradle/libs.versions.toml:37,94`,
  `~/Projects/SecondBrain/app/build.gradle.kts:105` — confirmed real hub dependency, pin
  `v1.10.0`.
- `~/Projects/CalTracker_Android/gradle/libs.versions.toml:73,136` — confirmed real hub
  dependency, pin `v1.5.0`.
- Live grep of both consumer trees this session (`TextCard`, `AppChip`, `CardBase` name
  searches) — proves D-02's blast-radius approach is executable and returns real, non-trivial
  results.
- `.planning/phases/02-coherence-audit/02-CONTEXT.md`, `.planning/REQUIREMENTS.md`,
  `.planning/STATE.md`, `.planning/phases/01-tier-legibility/01-05-SUMMARY.md`,
  `.planning/phases/01-tier-legibility/01-COVERAGE.md`, `FOLLOWUPS.md`, `.planning/config.json`.

### Secondary (MEDIUM confidence)
None — no web/external documentation was needed for this phase; all research was internal
codebase reading.

### Tertiary (LOW confidence)
None.

## Metadata

**Confidence breakdown:**
- Registry structure / family enumeration: HIGH — every count directly read from source, cross-
  checked two ways (concatenation term count + `ExplorerFamilies.ORDERED_KEYS` length both = 9;
  per-family entry sum = 53, matching `ComponentRegistry.kt`'s own doc-comment claim).
- Candidate findings (Cards/Chips/Tactile Foundation skims): HIGH for the cited facts (signatures,
  tier values, `CardBase` composition) — MEDIUM for the eventual disposition recommendation itself
  (unify vs. keep-with-rationale is a judgment call left to the audit's actual author, correctly
  flagged as such throughout).
- Consumer blast-radius feasibility: HIGH — both repos verified to exist, verified to have real
  Gradle dependencies on the hub, verified via live grep to contain real call sites.
- D-03 litmus text: HIGH — quoted verbatim from `docs/DESIGN-INTENT.md`, not paraphrased.

**Research date:** 2026-09-01
**Valid until:** Effectively unbounded for the registry snapshot cited (tied to HEAD `464af01`) —
re-verify entry counts/tiers if any commits land on `ComponentRegistry.kt` or any
`*FamilyScreen.kt` file between this research and plan execution. Consumer pin versions
(`v1.10.0`/`v1.5.0`) should be re-checked at plan-execution time if either consumer repo has been
repinned since 2026-09-01.
