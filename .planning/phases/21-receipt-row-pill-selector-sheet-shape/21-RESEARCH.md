# Phase 21: Receipt row, Pill selector & sheet shape - Research

**Researched:** 2026-10-12
**Domain:** Jetpack Compose / Material 3 UI library (hub): one new stateless row, one widened selector (new Pill branch), one widened sheet scaffold, registry/API gates
**Confidence:** HIGH on repo mechanics, guards and insertion points; MEDIUM on Compose text-metric behavior (the 92dp math, flagged as the top risk and `[ASSUMED]` where not run)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

### Signatures & compat
- **D-01 [signatures]:** The signatures are exactly those in `.planning/seams/v2.6.0-SKETCH.md` §2 Y-2, Y-11 and Y-13. — **Reversibility:** one-way — signed off by both consumers.
- **D-02 [compat]:** Source compatibility only (P1): no hidden overloads or shims.
  - `SegmentedOptionSelector`: params are appended after `disabledReason`.
  - `SheetScaffold.shape`: inserted before the trailing `content` lambda, never after it.
  - Regenerate `api.txt` in the same commit.

### row-overflow
- **D-03 [row-overflow]:** When the title and value collide, the value wins and the title ellipsizes. Verify at 360dp with a long German title. _(ai-auto)_
- **D-04 [row-layout]:** Summary layout:
  - A `Row` of a title column (weight 1f): title, subtitle, `supportingContent`, cue pill.
  - A value column: wraps, min 56dp, end-aligned, holding value and `valueCaption`.
  - A chevron, only when `onClick != null && trailingAction == null`. It rotates 0→180° in 200ms.
  - `trailingAction`.

  `expandedContent` renders full width below the summary, never as a Row sibling. Title, subtitle and value are single-line with ellipsis.

  Other specs:
  - **Cue pill:** full radius on `warnTone`, `bodySmall`, 4dp × 8dp padding, 24dp tall, after a 4dp gap.
  - **`dimmed`:** 38% alpha plus strikethrough on title and subtitle, with a 200ms fade.
  - **Collapsed height:** with all lines and no cue, 92dp ±1dp.
- **D-05 [row-tone]:** `ContentTone.Error` resolves through `ToneDefaults.error()`. There is no `errorTone` param.

### row semantics
- **D-06 [row-semantics]:** The summary root uses `semantics(mergeDescendants = true)`.
  - With `onClick`, it also gets `clickable(role = Role.Button, onClickLabel)` and a `stateDescription` from `expandedStateLabel`/`collapsedStateLabel`.
  - `trailingAction` and `expandedContent` stay outside the merge.
  - The row is stateless; single-open state belongs to the consumer.
  - Tags: `receipt_row_summary`, `receipt_row_trailing_action`, `receipt_row_expanded_content`.

### pill-impl
- **D-07 [pill-impl]:** Pill is a separate private Row-based branch, because M3 `SegmentedButton` forces the check icon and its shape.
  - **Container:** one full-radius container with a 1dp `outlineVariant` outline.
  - **Selected segment:** `primary`/`onPrimary`, no check icon.
  - **Labels:** single-line `bodyMedium` with ellipsis, 40dp tall.
  - **Default path:** the Material path stays byte-unchanged.

  _(ai-auto)_
- **D-08 [disabled-options]:** A segment in `disabledOptions` renders at 38% alpha, exposes `enabled = false` and never fires `onSelect`. This applies in both styles. The whole-control `enabled`/`disabledReason` behavior is unchanged. The KDoc warns that long localized labels truncate in Pill.

### row-family
- **D-09 [row-family]:** `ReceiptRow` registers in the Cards family. Its gallery demo holds single-open state in demo state, not in the row. _(ai-auto)_

### Claude's Discretion
- How the Material branch gets `disabledOptions` (per-segment `enabled`), as long as an empty set changes nothing.

### Deferred Ideas (OUT OF SCOPE)
None. This phase stays within its scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VROW-01 | Stateless `ReceiptRow`: summary Row anatomy, value-wins overflow, full-width `expandedContent`, cue pill, 200ms chevron, `dimmed`, 92dp ±1dp | Layout skeleton + S1 math feasibility (**line-height trim hazard, Pitfall 1**), motion seams for clock-controlled tests, long-German 360dp harness (NATIVE), bounds tests via `getUnclippedBoundsInRoot` |
| VROW-02 | One merged summary node; `Role.Button` + `onClickLabel` + `stateDescription`; `trailingAction`/`expandedContent` separate nodes | Two-level Row so the merge domain excludes `trailingAction` (Pattern 2); matchers `SemanticsProperties.Role/StateDescription`, `SemanticsActions.OnClick.label`, `hasAnyAncestor` |
| VSEG-01 | `disabledOptions` + `SegmentedOptionStyle.Pill`; Material + empty set == v2.5.1 incl. G-15-1 | Exact insertion points (early branch after `require`, append params after `disabledReason`), the ONE unavoidable Material-path line edit (lane 2) and its pin test, Pill anatomy verified against the CT 004 sketch CSS |
| VSHEET-01 | `SheetScaffold(shape = BottomSheetDefaults.ExpandedShape)` before the trailing `content` | Exact insertion point; `ModalBottomSheet` has a `shape` param in the resolved 1.4.0 binary (javap); source-contract + recording-Shape probe tests |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- **One-way dependency:** library imports only Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable, osmdroid; no consumer package/nouns/colors/secrets. `ReceiptRow` must name no CalTracker/food concepts (title, value, caption are caller strings). [VERIFIED: /home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md]
- **Registry XOR rule:** every public top-level `@Composable` in `component/`, `feedback/`, `modifier/`, `theme/` is registered in a family list XOR in `INTENTIONALLY_UNREGISTERED`. Only `ReceiptRow` is a new public composable this phase; the Pill branch and all row sub-parts must be `private`/`internal`. [VERIFIED: CLAUDE.md + `ComponentRegistryDriftGuardTest.kt`]
- **Source-compatible, not binary-compatible:** append defaulted params; never after a trailing lambda; NO new `@Deprecated(HIDDEN)` overloads / `copy$default` shims. `apiCheck` is the per-commit gate; `verify-binary-abi.sh` is informational. [VERIFIED: CLAUDE.md]
- **Detekt zero baseline:** fix or tune with justification; never regenerate a baseline. Gradle commands drop the module prefix.
- **Shipping is human-gated:** no tag, no push, no repin. Commit on `main`; do not touch consumer files.
- **Session constraints (binding):** owner asleep; one gradle invocation at a time; no pixel capture (`captureToImage` times out); never `git add .planning/` wholesale; docs/README/API.md count edits deferred to Phase 24 (but `api.txt` is regenerated in the same commit as each public-API change).
- **Memory lesson:** caller-overridable labels need a long German/Spanish ~360dp check before layout sign-off (`ReceiptRow` title, Pill segments). [VERIFIED: MEMORY.md `test-localized-long-strings-before-signoff`]
- **Memory lesson:** milestone tags are JitPack coordinates; do not create any tag (config `git.create_tag` is `false`). [VERIFIED: .planning/config.json]

## Summary

Phase 21 is three independent, small-surface changes plus one real component. (1) `SheetScaffold` gains one defaulted `shape` param between `contentWindowInsets` and the trailing `content`, forwarded by name to `ModalBottomSheet`. The existing `contentWindowInsets` line already ends in a comma, so the diff is pure addition. (2) `SegmentedOptionSelector` gains `disabledOptions` and `style` appended after `disabledReason` (already comma-terminated), a new `SegmentedOptionStyle` enum, and a private Pill branch reached by an early return placed after the `require(options.size == 2)` and before the existing `Column`. The Material branch stays textually identical except for ONE unavoidable line (`enabled = enabled` becomes per-segment), which is a lane-2 rewrite and needs `HUB_LANE_OVERRIDE=2` for that commit. (3) `ReceiptRow` is a new, stateless composable whose only genuinely hard parts are the merge domain (`trailingAction` must be a SIBLING of the merged clickable region, not a descendant) and the 92dp collapsed height.

**The top risk is the 92dp spec.** The S1 arithmetic (12 + 24 + 4 + 20 + 4 + 16 + 12 = 92) assumes each single-line `Text` is exactly as tall as its `lineHeight`. The hub's `Typography` builds plain `TextStyle`s with no `lineHeightStyle`, and the Compose default `LineHeightStyle` is `Alignment.Proportional` + `Trim.Both`, which removes the extra space above the first line and below the last line that `lineHeight` generates. A single line can therefore measure LESS than its `lineHeight` (the same applies to the 24dp cue pill). The fix is local and safe: give every `ReceiptRow` text style `LineHeightStyle(Alignment.Center, Trim.None)` (what M3's own default type scale uses), and pin the 24/20/16 heights with a probe-style regression test. With that, the math is feasible: 92dp at density 1.0 and 260px (92.4dp) at the reference density 2.8125, inside ±1dp.

Everything else is mechanical and well-precedented by Phase 20: early-branch widening pinned by a source-contract test, vertical slices that each land code + registry entry + vocabulary-guard edit + `api.txt`, structural (non-pixel) proofs, NATIVE graphics for any text-geometry test, one `setContent` per test.

**Primary recommendation:** Land it as four vertical slices (sheet shape, selector Pill + disable, `ReceiptRow` component with semantics, `ReceiptRow` motion/long-label/accordion demo + gate). Give `ReceiptRow` text `LineHeightStyle(Center, None)` unconditionally, nest the merged clickable region inside the summary line so `trailingAction` is its sibling, and edit the Material path by exactly one line under `HUB_LANE_OVERRIDE=2`.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| `ReceiptRow` layout, tones, semantics, motion | Client UI component (`component/`) | Theme tokens (`theme/Dimens`, `ToneColors`) | Stateless state-in/callback-out widget; colors come from `ToneColors`/`ColorScheme` |
| Single-open accordion state | Consumer / Explorer demo (`explorer/`) | — | Hub row is stateless (D-06); demo holds `remember` state (D-09) |
| Pill selector branch + per-option disable | Client UI component (`component/`) | — | Private branch inside the existing public composable |
| Sheet shape | Client UI component (`component/SheetScaffold`) | M3 `ModalBottomSheet` | Pure pass-through to the M3 `shape` param |
| Registry / vocabulary / API guards | Explorer harness + tests + tooling (`explorer/`, `src/test`, `api.txt`, `tools/`) | — | Drift-guard source of truth, not shipped behavior |

(Single-tier library: no backend, network, persistence.)

## Standard Stack

### Core (all already in the build; nothing new to install)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Compose BOM | 2026.04.01 | UI toolkit | [VERIFIED: gradle/libs.versions.toml:12 per 20-RESEARCH.md; CLAUDE.md toolchain line "Compose BOM **2026.04.01**"] |
| Material3 | 1.4.0 (resolved) | `ModalBottomSheet`, `SegmentedButton`, `HorizontalDivider`, `Text`, `Surface` | [VERIFIED: `~/.gradle/.../material3-android/1.4.0/.../material3.aar`, javap this session] |
| material-icons (core/extended) | via BOM | chevron (`KeyboardArrowDown`) | [VERIFIED: extended is a dependency per 20-RESEARCH.md `build.gradle.kts:102`] |
| Robolectric | 4.16.1 | JVM Compose UI tests | [VERIFIED: 20-VALIDATION.md] |
| Compose ui-test-junit4 | via BOM | `createComposeRule`, `mainClock`, `getUnclippedBoundsInRoot` | [VERIFIED: existing tests import them] |
| Metalava plugin | 0.5.0 | `apiDump` / `apiCheck` -> `api.txt` | [VERIFIED: tools/README-api-guard.md] |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Private Row-based Pill branch (D-07) | Restyled M3 `SegmentedButton` | Locked out: M3 forces the check icon and its shape |
| Two-level Row (merged clickable region + sibling `trailingAction`) | One Row with `semantics(mergeDescendants)` on the whole Row | Single Row would merge `trailingAction` into the summary node (violates D-06) |
| `LineHeightStyle(Center, None)` on `ReceiptRow` text | Fixed `Modifier.height(...)` on each Text | Fixed heights clip at large font scales; Center/None keeps scaling |
| Plain Row for the summary line | Custom `Layout` to guarantee "value wins" even for absurd values | Plain Row is enough for the spec'd case; see Pitfall 3 for the one edge it does not cover |

**Installation:** none. **Package Legitimacy Audit:** not applicable. This phase installs no external packages (verified: scope is three Kotlin files, tokens, registry and tests; `gsd-tools package-legitimacy` not run because there is nothing to check).

## Package Legitimacy Audit

No external packages are installed in this phase.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| (none) | — | — | — | — | — | — |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
Consumer call site (CT topLevelContent / SB / Explorer)
   |
   +--> SheetScaffold(onDismissRequest, modifier, sheetState, dragHandle, contentWindowInsets, shape = ExpandedShape, content)
   |        '--> ModalBottomSheet(..., contentWindowInsets = ..., shape = shape) { Column(imePadding) { content() } }
   |
   +--> SegmentedOptionSelector(selectedIndex, options, onSelect, modifier, enabled, disabledReason, disabledOptions = {}, style = Material)
   |        require(options.size == 2)                       (unchanged, runs for BOTH styles)
   |        if (style == Pill) { PillSegmentedOptionSelector(...); return }      <-- new, early branch
   |        [UNCHANGED Material: Column{ SingleChoiceSegmentedButtonRow(height(IntrinsicSize.Min)) { SegmentedButton(
   |              enabled = enabled && index !in disabledOptions   <-- the ONE edited line (lane 2) ... maxLines = 2, reserveSegmentIconSlot() } } caption }]
   |
   '--> ReceiptRow(title, ..., expanded, onClick, trailingAction, dimmed, showDivider, expandedContent)
            Column(root, modifier)
              |- Row(summary line, CenterVertically)               (no semantics of its own)
              |     |- Row(MERGED + clickable + tag receipt_row_summary, weight 1f, padding vertical 12dp, spacedBy 8dp)
              |     |     |- Column(weight 1f, spacedBy 4dp): title / subtitle / supportingContent / cue pill
              |     |     |- Column(value: widthIn(min 56dp), End): value / valueCaption        (iff value or caption)
              |     |     '- Chevron (24dp, rotate 0->180 / 200ms)                              (iff onClick != null && trailingAction == null)
              |     '- Box(tag receipt_row_trailing_action) { trailingAction() }                (SIBLING, outside the merge)
              |- AnimatedVisibility(expanded && expandedContent != null) { Box(fillMaxWidth, tag receipt_row_expanded_content) }
              '- HorizontalDivider(1dp outlineVariant)                                           (iff showDivider)
```

### Recommended Project Structure
```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/ReceiptRow.kt                 # NEW: public ReceiptRow + private/internal parts, motion seams
├── component/SegmentedOptionSelector.kt    # EDIT: append params, early Pill branch, private PillSegmentedOptionSelector, enum at file end
├── component/SheetScaffold.kt              # EDIT: shape param + forward
├── theme/Dimens.kt                         # APPEND: Dimens.ReceiptRow, Dimens.SegmentedPill nested objects
├── explorer/CardsFamilyScreen.kt           # APPEND: ReceiptRow entry + Variants (accordion demo)
├── explorer/PickersFamilyScreen.kt         # APPEND: Pill / disabled demos inside SegmentedOptionSelectorVariants
└── explorer/SheetsFamilyScreen.kt          # APPEND: shaped-sheet button inside SheetScaffoldSection
src/test/java/.../{component,explorer,theme}/   # see Validation Architecture
api.txt                                     # apiDump in the same commit as each signature change
```

### Pattern 1: Early-branch widening that leaves the legacy block untouched (Phase 20 precedent)
**What:** Insert the new branch ABOVE the existing body and `return`; do not re-indent the legacy lines. Phase 20 did exactly this for `DynamicActionButton` and pinned it with a whitespace-stripped source-contract test. [VERIFIED: 20-02-SUMMARY.md "the compose compiler accepted the early return"; DynamicActionButtonLegacyPathSourceContractTest.kt read this session]
**Where for the selector:** immediately after the `require(options.size == 2) { ... }` block and before `Column(`. The signature gains two lines after `disabledReason: String? = null,` (verbatim current text below), which is a pure append.
**Example (skeleton, illustrative; not compile-verified):**
```kotlin
// SegmentedOptionSelector.kt, after the require(...) block:
if (style == SegmentedOptionStyle.Pill) {
    PillSegmentedOptionSelector(
        selectedIndex = selectedIndex, options = options, onSelect = onSelect, modifier = modifier,
        enabled = enabled, disabledReason = disabledReason, disabledOptions = disabledOptions,
    )
    return
}
// ...existing Column { SingleChoiceSegmentedButtonRow ... } stays as is, except:
//     enabled = enabled,   ->   enabled = enabled && index !in disabledOptions,
```

### Pattern 2: Merge domain = a nested region, `trailingAction` is its sibling
**What:** The semantics root for the summary is the INNER Row (title column + value column + chevron) carrying `.testTag("receipt_row_summary")`, `semantics(mergeDescendants = true)` and (iff `onClick != null`) `clickable(role = Role.Button, onClickLabel = onClickLabel)` plus `stateDescription`. The OUTER summary line holds that Row (weight 1f) and, after it, the `trailingAction` wrapper `Box` carrying `receipt_row_trailing_action`. `expandedContent` is outside the line entirely.
**Why:** a button nested inside a merging parent is a semantics-merge question we cannot test here; making it a sibling removes the question. Visual geometry is identical to the one-Row layout in D-04 (the inner Row simply occupies the left part of the line).
**Modifier order inside the merged Row (so the node bounds and the click area both equal the full 92dp):** `tag -> semantics -> clickable -> padding(vertical 12dp)`. Padding must come AFTER clickable so it is inside the node and inside the click area. [ASSUMED: Compose modifier-order semantics; confirm with the 92dp bounds test]
**Merge facts:** `clickable` itself merges descendants and the merged node reports `MergeDescendants = 'true'`, `Role = 'Button'`, `Text = '[Hello, World]'`, `Actions = [OnClick, GetTextLayoutResult]` [CITED: developer.android.com/develop/ui/compose/testing/apis]. Add the explicit `semantics(mergeDescendants = true)` anyway for the `onClick == null` case, where nothing else merges.

### Pattern 3: Structural proofs (no pixels)
Phase 20 proved `captureToImage` times out in this harness (`ComposeTimeoutException` in `WindowCapture.forceRedraw`). [VERIFIED: 20-01-SUMMARY.md "PIXEL_PROBE: FAIL"; 20-VALIDATION.md] So:
- **Bounds:** `getUnclippedBoundsInRoot()` (Dp) on tagged nodes; unclipped because `boundsInRoot` is clipped to ancestors. [VERIFIED: ToneCardTest.kt and ApproachLadderCardLongLabelTest.kt header]
- **Text style/color/decoration:** read `GetTextLayoutResult` then `layoutInput.style` (`color`, `textDecoration`) and `isLineEllipsized(0)`. [ASSUMED: `layoutInput.style` carries the merged color/decoration; `isLineEllipsized` precedent is VERIFIED in ApproachLadderCardLongLabelTest.kt:144]
- **Containers/alpha/shape:** source-contract tests (`SourceContractTestSupport.stripComments` + `functionBody`), the Phase 20 precedent for `RoundIconButton`/`PillChip`. [VERIFIED: RoundIconButtonTest.kt source-contract test read this session]
- **Animations:** factor the animated values into `internal` composables that the row calls and the test calls directly (Pattern 4).

### Pattern 4: Testable motion seams (clock control)
`composeTestRule.mainClock.autoAdvance = false`, then `advanceTimeBy(ms)` drives recomposition and animations. [CITED: developer.android.com/develop/ui/compose/testing/synchronization] No test in this repo uses `mainClock` yet (grep over `src/test` found none), so this is a first. Factor:
```kotlin
internal const val RECEIPT_ROW_MOTION_MS = 200
internal const val RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES = 180f
internal const val RECEIPT_ROW_DIMMED_ALPHA = 0.38f
@Composable internal fun rememberReceiptRowChevronRotation(expanded: Boolean): State<Float> = animateFloatAsState(
    targetValue = if (expanded) RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES else 0f,
    animationSpec = tween(RECEIPT_ROW_MOTION_MS), label = "receipt_row_chevron")
@Composable internal fun rememberReceiptRowAlpha(dimmed: Boolean): State<Float> = /* same, to RECEIPT_ROW_DIMMED_ALPHA / 1f */
```
A test composes only the seam, records the value per frame, and asserts: at t=0 it is 0 (or 1), at t=100ms strictly between, at t >= 200ms(+1 frame) it equals the target. The row calls the same functions; a source-contract test proves it (`rememberReceiptRowChevronRotation(` appears in the `ReceiptRow` body). `internal` composables are skipped by both drift guards (they ignore `private ` and `internal ` declarations) and absent from `api.txt`. [VERIFIED: ComponentRegistryDriftGuardTest.kt/DomainVocabularyDriftGuardTest.kt extraction logic read this session]
`ExpandableFab` already uses `animateFloatAsState(targetValue = if (expanded) 45f else 0f, label = "fab_rotation")` as an in-repo precedent for rotating an icon. [VERIFIED: ExpandableFab.kt:116-119]

### Anti-Patterns to Avoid
- **Wrapping the Material block in `if/else`:** re-indents every line (lane 2 and breaks "byte-unchanged"). Use the early return.
- **Putting `trailingAction` inside the merged Row:** merges its text/click into the summary (violates D-06/VROW-02).
- **`padding` before `clickable` on the summary:** shrinks the click area and node bounds below 92dp.
- **`softWrap = false` on the single-line Texts:** the UI-SPEC says it, but `maxLines = 1` + `TextOverflow.Ellipsis` is the verified hub idiom (PillChip, proven by `ToneAtomsLongLabelTest`) and avoids the unbounded-width measure quirk. Use `maxLines = 1` + `Ellipsis`. [VERIFIED: PillChip.kt text block; 20-03-SUMMARY "T-20-11"]
- **A one-line `@Composable fun ReceiptRow(` or an indented annotation:** both drift guards only recognise a column-0 `@Composable` line followed by a column-0 `fun`. [VERIFIED: guard source read this session]
- **Any `captureToImage`/pixel assertion:** times out in this harness (binding session constraint).
- **Two `setContent` calls in one test:** the Compose rule forbids it; Phase 20 hit this in every plan. [VERIFIED: 20-01/02/03 SUMMARY deviations]

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Selectable segments with radio semantics | Custom pointer plumbing | `Modifier.selectable(selected, enabled, role = Role.RadioButton, onClick)` inside a `Modifier.selectableGroup()` Row | M3's own `SingleChoiceSegmentedButtonRow` uses `selectableGroup()` and `SegmentedButton` sets `Role.RadioButton` [CITED: androidx-main SegmentedButton.kt via WebFetch; branch is androidx-main, not the 1.4.0 tag, MEDIUM] |
| Touch-target expansion | Manual 48dp wrappers | Framework default: clickable touch bounds are expanded to 48dp [VERIFIED: ApproachLadderCardTest.kt:393-396 comment] | Assert with `touchBoundsInRoot`, do not add layout slack to the 40dp Pill |
| Divider | Custom `Box(background)` | `HorizontalDivider(thickness = Dimens.HairlineBorder, color = colorScheme.outlineVariant)` | Already used in the hub (`TagChipWithContextMenu`, `ExplorerIndexScreen` import) |
| Expand/collapse of `expandedContent` | Manual height animation | `AnimatedVisibility(expandVertically + fadeIn, 200ms)` [UI-SPEC S1 line 341] | Standard; initial `expanded = true` composes without an animation |
| Disabled dimming | Per-color math | `Modifier.alpha(DISABLED_CONTENT_ALPHA)` (`internal const val ... = 0.38f`, same package) | Already the Phase 20 atoms' convention |
| Source-text assertions | New regex helpers | `SourceContractTestSupport.{source, stripComments, functionBody, countOccurrences}` | Used by 4+ contract tests; note `source()` is hard-wired to `component/` |
| WCAG/contrast | New math | `theme/ContrastTestSupport.contrastRatio` | Added in Phase 20 |

**Key insight:** every dimension and color here already has a token or an M3/Phase-20 default; the work is wiring, the line-height guard, and structural proof.

## Common Pitfalls

### Pitfall 1: The 92dp math silently fails because of `LineHeightStyle` trimming (HIGH RISK)
**What goes wrong:** the spec math (12+24+4+20+4+16+12) is the sum of `lineHeight`s. A single-line `Text` is only as tall as its `lineHeight` if the line box is not trimmed. The hub's `Typography` is built from bare `TextStyle(fontFamily, fontWeight, fontSize, lineHeight, letterSpacing)` with no `lineHeightStyle`. [VERIFIED: Type.kt:17,20,21 `titleMedium ... lineHeight = 24.sp`, `bodyMedium ... lineHeight = 20.sp`, `bodySmall ... lineHeight = 16.sp`; `grep -rn "LineHeightStyle|lineHeightStyle|includeFontPadding|platformStyle" src` returned nothing] The Compose default is `LineHeightStyle(alignment = Alignment.Proportional, trim = Trim.Both, mode = Mode.Fixed)` [CITED: androidx-main LineHeightStyle.kt], and `Trim` "allows you to leave or remove the extra space to the top of the first line and bottom of the last line of your text, generated from any `lineHeight`" and only works with `includeFontPadding = false` (default since BOM 2024.01.01) [CITED: developer.android.com/develop/ui/compose/text/style-paragraph]. So a one-line 16sp title with `lineHeight = 24.sp` can measure roughly its font height (about 19dp), not 24dp, and the total lands several dp under 92. The cue pill (16sp line + 4 + 4 = 24dp) has the same problem. [ASSUMED: exact shrink not measured; the mechanism is documented, the magnitude is not]
**How to avoid:** in `ReceiptRow` only, map every text style through a private helper that sets `lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.None)`. This is the configuration the Compose docs sample uses and what M3's default type scale applies, so it makes line box == `lineHeight` regardless of the hub's typography. Do NOT change `Type.kt` (it would restyle every consumer and rewrite existing lines).
**Warning signs / proof:** a Wave-0 `ReceiptRowLineHeightTest` (NATIVE) measures three tagged Texts (`titleMedium`, `bodyMedium`, `bodySmall`) and asserts 24 / 20 / 16 ±0.5dp. It is a permanent regression test of the S1 assumption, and it is the first thing the plan runs.
**Rounding at the reference density (2.8125):** fixed Dp rounds per `roundToPx` (12dp -> 34px, 4dp -> 11px), text heights ceil (24sp -> 68px, 20sp -> 57px, 16sp -> 45px): 34+68+11+57+11+45+34 = 260px = 92.4dp. Inside ±1dp; CT's Gate-1 C3 band is 259±8 px. At Robolectric's default density 1.0 it is exactly 92. [computed this session from the verified numbers; CT's 259px is `92 × 2.8125` rounded]

### Pitfall 2: The merge swallows `trailingAction`
**What goes wrong:** one Row with `mergeDescendants = true` around everything makes the trailing button's text part of the summary node and breaks the "separate node" requirement.
**How to avoid:** Pattern 2. Test with `onNodeWithTag("receipt_row_trailing_action")` plus `.assert(!hasAnyAncestor(hasTestTag("receipt_row_summary")))` (`hasAnyAncestor`/`hasTestTag` precedent: OutcomeSheetTest.kt imports). [VERIFIED: import lines read]

### Pitfall 3: "Value wins" is true only for the measurement order Row actually has
**What goes wrong:** a Row measures its non-weighted children in declaration order and gives weighted children the leftover. With value (unweighted) before chevron (unweighted), an absurdly wide value consumes the remaining width and the chevron collapses to 0 width. The spec'd case (long title, normal value) is fine: the value is measured first at its natural width, the weighted title gets the rest and ellipsizes. [ASSUMED: Row measurement order; standard Compose behavior]
**How to avoid:** keep value, chevron declared in that order; value Text is `maxLines = 1` + Ellipsis so it cannot exceed the row; add one edge test (very long value + chevron still has non-zero width inside the window). If it ever fails, cap the value column with `widthIn(max = ...)`. Not worth a custom `Layout` now.

### Pitfall 4: Registry / vocabulary guards fail on `ReceiptRow`
**What goes wrong:** `DomainVocabularyDriftGuardTest` rejects a head token that is in neither `PRIMITIVE_NOUN_ALLOWLIST` nor `DOMAIN_VOCABULARY`. The head regex is `[A-Z]+(?=[A-Z][a-z]|$)|[A-Z][a-z0-9]*`, so `ReceiptRow` -> `Receipt`, which is in neither. [VERIFIED: DomainVocabularyDriftGuardTest.kt read this session; `Segmented` and `Sheet` are already in the allowlist, `Receipt` is not]
**How to avoid:** add exactly one entry in the same task that adds the composable (a `src/test` edit): `"ReceiptRow" to "<rationale>"` in `DOMAIN_VOCABULARY` (recommended, see Open Question 4) OR `"Receipt"` in `PRIMITIVE_NOUN_ALLOWLIST`. Nothing else is needed for the selector (`Segmented` present) or the sheet.

### Pitfall 5: Registry entry shape
`ComponentStatesMatrixTest` requires exactly `listOf("Default", "Pressed / Selected", "Disabled", "Focused")` in order, non-null `content`, and `Entry.tier` has no default. [VERIFIED: ComponentStatesMatrixTest.kt + ComponentRegistry.kt `data class Entry(... val tier: Tier)` read this session] Appending the new `ComponentRegistry.Entry(...)` after the `ToneCard` entry requires changing that entry's closing `)` to `),`, which the additive guard tolerates (trailing `,` is stripped by `normalize`). [VERIFIED: verify-additive-diff.sh normalize(): `-e 's/[+,]$//'`]

### Pitfall 6: Lane classification
**What goes wrong:** the pre-commit hook blocks a lane-2 commit. Which edits are lane 2? `verify-additive-diff.sh` compares removed vs added lines modulo a trailing `,`/`+`; a removed line with no identical added counterpart is a rewrite. [VERIFIED: script read this session]
- `SheetScaffold.kt`: adding `shape: Shape = ...,` before `content` is a pure add. In the body, `contentWindowInsets = contentWindowInsets` gains a trailing comma (tolerated) and a new `shape = shape` line. **Lane 1.** KDoc: only ADD a `@param shape` line.
- `SegmentedOptionSelector.kt` signature: `disabledReason: String? = null,` already ends in a comma, so appended params are a pure add. **The Material loop line `enabled = enabled,` must become `enabled = enabled && index !in disabledOptions,` -> lane 2.** Declare it: `HUB_LANE_OVERRIDE=2 git commit ...` (documented in tools/README-api-guard.md; Phase 20's STATE note says the same for re-signatured composables). Alternative that stays lane 1: shadow the parameter inside the loop (`val enabled = enabled && index !in disabledOptions` as an inserted line before `SegmentedButton(`) so the existing `enabled = enabled,` line is untouched. It compiles with a "name shadowed" warning (no `allWarningsAsErrors`/`Werror` in build.gradle.kts [VERIFIED: grep returned nothing]) and detekt does not enable a shadowing rule (only LongMethod/LongParameterList/CyclomaticComplexMethod/naming/UnusedPrivateMember are active [VERIFIED: config/detekt/detekt.yml]). **Recommendation: the honest one-line edit under lane-2 override**; shadowing is the fallback if the override is unwanted.
- KDoc edits on the selector: append new lines only; do not reword existing KDoc lines (a reworded line is a removed line).
- Everything else (new files, appended `Dimens`, appended registry entries, appended Variants lines, appended enum at file end) is lane 1.

### Pitfall 7: `Set<Int>` default and Compose stability
`disabledOptions: Set<Int> = emptySet()` is a signed-off signature. With Kotlin 2.3.20 strong skipping is on by default, so it does not cause the selector to stop skipping. [ASSUMED: strong skipping default since Kotlin 2.0.20; no action needed]

### Pitfall 8: Text geometry tests need NATIVE graphics
Under legacy Robolectric graphics, text is measured at ~1px/char and German-label tests pass vacuously (Phase 20's repeated finding). [VERIFIED: 20-02-SUMMARY deviation 1; SegmentedOptionSelectorTest.kt header "GraphicsMode.NATIVE"] Use `@Config(sdk = [35], qualifiers = "w360dp-h800dp")` + `@GraphicsMode(GraphicsMode.Mode.NATIVE)` for every `ReceiptRow` and Pill geometry test.

### Pitfall 9: The `SheetScaffold` shape cannot be seen in a semantics tree
`ModalBottomSheet` renders in a separately-rooted composition window; node resolution works (OutcomeSheetTest, AlbumTitleConfirmSheetTest), interaction does not. [VERIFIED: GalleryDemoInteractionTest.kt header; AlbumTitleConfirmSheetTest.kt:125-136] The shape itself is not a semantics property. Prove it with (a) a source-contract test and (b) a probe-gated recording `Shape` (Validation Architecture). Default parity is proven by construction: v2.5.1 passed no `shape`, so `ModalBottomSheet` used its own default, which is the same expression `BottomSheetDefaults.ExpandedShape` the new default uses.

### Pitfall 10: Light-theme default warn text is nearly black
`ContentTone.Warn` text and the cue pill content use `warnTone.content` = `onTertiaryContainer` by default. In the hub's light palette that is `Color(0xFF251A00)` (near-black), indistinguishable from `onSurface`; in dark it is `0xFFFFE0A0`. [VERIFIED: 20-RESEARCH.md quoting theme/Color.kt `AmberOnTertiaryContainer = Color(0xFF251A00)`, `AmberOnTertiaryContainerDark = Color(0xFFFFE0A0)`] CT passes its own `WarnOnContainer` tokens (#7A5900 light), so this only affects the Explorer demo and the KDoc. Say so in the `warnTone` KDoc; do not try to fix it in the hub.

### Pitfall 11: Stale docs counts
README/API.md/`ComponentRegistry` KDoc counts go stale after registering `ReceiptRow` (no test enforces them). Deferred to Phase 24; note it in the SUMMARY. [VERIFIED: 20-04-SUMMARY "Deferred to Phase 24"]

### Pitfall 12: One gradle at a time, and `--tests` binds to the preceding task
Two gradle-related JVMs were already running at probe time. Batch filters into ONE invocation, in the order `./gradlew apiDump testDebugUnitTest --tests A --tests B detekt apiCheck`. [VERIFIED: `pgrep` this session; 20-RESEARCH.md Pitfall 10]

## Code Examples

> Illustrative skeletons, **not compile-verified** (no gradle run, by constraint). In-repo values come from the "Verified in-repo values" section; Compose/M3 call shapes are `[ASSUMED]` unless cited.

### SheetScaffold (exact insertion)
```kotlin
// new import: androidx.compose.ui.graphics.Shape
fun SheetScaffold(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    shape: Shape = BottomSheetDefaults.ExpandedShape,          // NEW, before the trailing lambda
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,             // gains a trailing comma (tolerated)
        shape = shape                                          // NEW, by name
    ) { ... }
```
`BottomSheetDefaults.ExpandedShape` is a `@Composable` getter, usable as a default expression inside a `@Composable` function (the binary exposes `getExpandedShape(Composer, int)`; `ModalBottomSheet`'s own param list contains a `Shape` right after the `sheetMaxWidth` float). [VERIFIED: javap of material3.aar classes.jar this session; CITED: androidx-main SheetDefaults.kt "public val ExpandedShape: Shape @Composable get()"]

### Pill branch skeleton
```kotlin
@Composable
private fun PillSegmentedOptionSelector(/* params as above */) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing)) {
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min).heightIn(min = Dimens.SegmentedPill.Height)
                .clip(CircleShape).border(Dimens.HairlineBorder, colors.outlineVariant, CircleShape)
                .selectableGroup()
        ) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val segmentEnabled = enabled && index !in disabledOptions
                Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .alpha(if (segmentEnabled) 1f else DISABLED_CONTENT_ALPHA)
                        .background(if (selected) colors.primary else Color.Transparent)
                        .selectable(selected = selected, enabled = segmentEnabled, role = Role.RadioButton,
                            onClick = { onSelect(index) })
                        .semantics { contentDescription = "$label, ${if (selected) "selected" else "not selected"}" }
                        .padding(horizontal = Dimens.SegmentedPill.LabelHorizontalPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) colors.onPrimary else colors.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                }
            }
        }
        if (!enabled && disabledReason != null) { /* same caption as Material: labelLarge, onSurfaceVariant, maxLines 2 */ }
    }
}
```
Notes: the selected fill is full-bleed within the clipped container (the CT 004 sketch CSS is `.seg { display:inline-flex; border:1px solid var(--border); border-radius:999px; overflow:hidden; }` and `.seg button.on { background:var(--primary); color:var(--on-primary); }` and `.seg button:disabled { opacity:.35; }`; the spec normalizes .35 to 38%). [VERIFIED: /home/yahir/Projects/AndroidApps/Personal/CalTracker_Android/.planning/sketches/004-count-amounts-piece-weight/index.html:14-17] Keep the SAME `contentDescription` string as the Material branch so Gate-1/test addressing is identical across styles. The caption is duplicated into the Pill branch (7 lines) so the Material block is not touched.

### ReceiptRow skeleton (structure, not full code)
```kotlin
@Composable
fun ReceiptRow(/* signature exactly as sketch Y-2 */) {
    val tones = ReceiptRowTones(titleTone, subtitleTone, valueTone, warnTone, ToneDefaults.error())   // ContentTone -> Color
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.ReceiptRow.ColumnGap)) {
            ReceiptRowSummary(Modifier.weight(1f), ...)      // private: tag, semantics, clickable, padding, 3 children
            if (trailingAction != null) Box(Modifier.testTag("receipt_row_trailing_action")) { trailingAction() }
        }
        if (expandedContent != null) AnimatedVisibility(expanded, enter = expandVertically(tween(MS)) + fadeIn(tween(MS)), exit = ...) {
            Box(Modifier.fillMaxWidth().testTag("receipt_row_expanded_content")) { expandedContent() }
        }
        if (showDivider) HorizontalDivider(thickness = Dimens.HairlineBorder, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
private fun TextStyle.receiptLine(): TextStyle =
    copy(lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None))
```
Tone mapping (ContentTone -> Color): Default -> `onSurface`, Muted -> `onSurfaceVariant`, Warn -> `warnTone.content`, Error -> `ToneDefaults.error().content` (D-05; no `errorTone` param). Title style: `titleMedium.copy(fontWeight = SemiBold)`; subtitle `bodyMedium`; supporting wrapped in `ProvideTextStyle(bodySmall.receiptLine())`; value `titleMedium` SemiBold `fontFeatureSettings = "tnum"` (UI-SPEC S1); caption `bodySmall`; cue `bodySmall`. [VERIFIED roles: UI-SPEC Typography table lines 266-271]

### Test idioms to copy
- Bounds in Dp: `composeTestRule.onNodeWithTag("x").getUnclippedBoundsInRoot()` then `assertEquals(msg, 92f, (b.bottom - b.top).value, 1f)` [VERIFIED: ToneCardTest.kt `getUnclippedBoundsInRoot` + `assertDpEquals`].
- Line/ellipsis: `GetTextLayoutResult` action + `isLineEllipsized(0)`, `lineCount` [VERIFIED: SegmentedOptionSelectorTest.kt `textLayout()` helper].
- Role/selected: `SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)`, `assertIsSelected()` [VERIFIED: ApproachLadderCardTest.kt:325, 331-336].
- Disabled: `assertIsNotEnabled()` [VERIFIED: used in 6 test files].
- Merged text: `onNodeWithTag("receipt_row_summary").assertTextContains("…")`; unmerged children via `useUnmergedTree = true` [CITED: developer.android.com/develop/ui/compose/testing/apis].
- Long German @360dp: copy `SegmentedOptionSelectorTest`/`ApproachLadderCardLongLabelTest` harness (class annotations quoted in Verified in-repo values).

## Verified in-repo values (read this session)

- **`SegmentedOptionSelector` signature today** (SegmentedOptionSelector.kt:45-53):
  `fun SegmentedOptionSelector(` / `selectedIndex: Int,` / `options: List<String>,` / `onSelect: (Int) -> Unit,` / `modifier: Modifier = Modifier,` / `enabled: Boolean = true,` / `disabledReason: String? = null,` / `) {`. Then `require(options.size == 2) {` (line 56), `Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {` (59), `SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {` (62), `enabled = enabled,` (68), `maxLines = 2,` (80), `modifier = Modifier.reserveSegmentIconSlot(),` (83), `private fun Modifier.reserveSegmentIconSlot(): Modifier = layout { measurable, constraints ->` (116), `private val SegmentedButtonIconSpacing = 8.dp` (139). The per-segment node carries `contentDescription = "$label, ${if (selected) "selected" else "not selected"}"` (74).
- **`SheetScaffold` signature today** (SheetScaffold.kt:50-57): `fun SheetScaffold(` / `onDismissRequest: () -> Unit,` / `modifier: Modifier = Modifier,` / `sheetState: SheetState = rememberModalBottomSheetState(),` / `dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },` / `contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },` / `content: @Composable ColumnScope.() -> Unit` / `) {`. The `ModalBottomSheet(` call (58-64) passes `onDismissRequest`, `modifier`, `sheetState`, `dragHandle`, `contentWindowInsets` by name.
- **`api.txt` today** (lines 388 and 392), the two lines that WILL be rewritten:
  `method @KotlinOnly @androidx.compose.runtime.Composable public static void SegmentedOptionSelector(int selectedIndex, java.util.List<java.lang.String> options, kotlin.jvm.functions.Function1<java.lang.Integer,kotlin.Unit> onSelect, optional androidx.compose.ui.Modifier modifier, optional boolean enabled, optional String? disabledReason);`
  `method @KotlinOnly @androidx.compose.runtime.Composable public static void SheetScaffold(kotlin.jvm.functions.Function0<kotlin.Unit> onDismissRequest, optional androidx.compose.ui.Modifier modifier, optional androidx.compose.material3.SheetState sheetState, optional kotlin.jvm.functions.Function0<kotlin.Unit>? dragHandle, optional kotlin.jvm.functions.Function0<androidx.compose.foundation.layout.WindowInsets> contentWindowInsets, kotlin.jvm.functions.Function1<androidx.compose.foundation.layout.ColumnScope,kotlin.Unit> content);`
  `git diff --numstat v2.5.1 -- api.txt` is `83  1` today (the one removed line is Phase 20's `DynamicActionButton`). After Phase 21 it should be exactly **3 removed** lines (that one + these two). The enum prints in the existing style (`public enum ActionButtonEmphasis {` / `enum_constant public static final ...Filled;` sorted).
- **Hub typography** (Type.kt:17,20,21): `titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,    fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp)`, `bodyMedium  = TextStyle(... fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp)`, `bodySmall   = TextStyle(... fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp)`. No `lineHeightStyle` anywhere in `src`.
- **Dimens** (theme/Dimens.kt): `val ContentSpacing: Dp = 4.dp`, `val TouchTarget: Dp = 48.dp`, `val CompactPadding: Dp = 12.dp`, `val HairlineBorder: Dp = 1.dp`, `val HorizontalPadding: Dp = 16.dp`, `val Medium: Dp = 12.dp` (`object CornerRadius`), `val ChipIcon: Dp = 18.dp` (`object Icons`); Phase 20 nested objects `ActionButton`, `RoundIconButton`, `PillChip`, `StackedProgressBar`. `DimensTest` regression-locks existing values, so only append new nested objects and extend the test (append-only).
- **Alpha constant** (PillChip.kt:33): `internal const val DISABLED_CONTENT_ALPHA = 0.38f`.
- **Phase 20 tone API** (theme/ToneTokens.kt): `class ToneColors(val container: Color, val content: Color)`, `object ToneDefaults { @Composable fun error(): ToneColors = MaterialTheme.colorScheme.let { ToneColors(it.errorContainer, it.error) }; @Composable fun warn(): ToneColors = ... ToneColors(it.tertiaryContainer, it.onTertiaryContainer) }`, `enum class ContentTone { Default, Muted, Warn, Error }`. Tag convention: `modifier.testTag("tone_card")` placed AFTER the caller modifier so a caller tag wins (ToneCard.kt); `PillChip` places the tag after sizing modifiers so node bounds equal the drawn size.
- **Registry** (explorer/ExplorerIndexScreen.kt:55-60): `object ExplorerFamilies { const val CARDS = "cards" ... const val PICKERS = "pickers" }` (also `SHEETS = "sheets"`); list names `cardsFamilyEntries` (CardsFamilyScreen.kt), the `ToneCard` entry (states: `StateCell("Default") { ToneCardDefaultPreview() }`, three N/A cells with comments, `content = { ToneCardVariants() }`, `tier = ComponentRegistry.Tier.PRIMITIVE`). `SegmentedOptionSelector` is a `PICKERS` PRIMITIVE entry with `content = { SegmentedOptionSelectorVariants() }` (private fun at PickersFamilyScreen.kt:449-460); `SheetScaffold` is a `SHEETS` PRIMITIVE entry with `content = { SheetScaffoldVariants() }` and a private `SheetScaffoldSection()` holding `var show by remember { mutableStateOf(false) }` + `ShowSheetButton("SheetScaffold — generic chrome") { show = true }` (SheetsFamilyScreen.kt:794-806).
- **Test class annotations to copy** (SegmentedOptionSelectorTest.kt): `@RunWith(RobolectricTestRunner::class)` / `@Config(sdk = [35], qualifiers = "w360dp-h800dp")` / `@GraphicsMode(GraphicsMode.Mode.NATIVE)`.
- **UI-SPEC S1 height math** (76-UI-SPEC.md:339): "12 + 24 + 4 + 20 + 4 + 16 + 12 = **92dp** (259px). No macro line: **72dp** (203px). Cue adds 4 gap + 24 pill (16 line + 4 + 4 padding) = **28dp** (79px)." Row anatomy: "Row(fillMaxWidth, CenterVertically, spacedBy 8dp, padding vertical 12dp)", chevron "24dp, `onSurfaceVariant`", "(YAT `AnimatedVisibility`, expandVertically + fadeIn 200ms)" for the editor.
- **Toolchain facts:** JDK `openjdk version "17.0.19"`; latest baseline tag `v2.5.1` (`git describe --tags --abbrev=0 --match 'v*'`); HEAD `b781d20 docs(phase-20): complete phase execution`; working tree has no uncommitted `src`/`api.txt` changes.

## State of the Art

| Old Approach | Current Approach | Impact |
|--------------|------------------|--------|
| Binary-compat shims for every signature change | Source-compat only (P1) | No hidden overloads / `copy$default`; v2.5.0 shims stay |
| `verify-api-additive.sh` | `apiCheck` on regenerated `api.txt` + `tools/test/run-all.sh` + Phase 24 canary | `api.txt` diff is signature-line rewrite, not a literal append |
| Golden screenshots for "pixel-identical" | Structural proof (same code path) + Robolectric assertions | P3; Receipt pixels via the Phase 24 owner screencap review |
| `includeFontPadding = true` text | `false` by default since Compose BOM 2024.01.01 | Makes `LineHeightStyle` trimming effective, hence Pitfall 1 |

**Deprecated/outdated:** none relevant.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | With the hub's plain `TextStyle`s (no `lineHeightStyle`) a one-line Text measures LESS than its `lineHeight` (default Proportional + Trim.Both) | Pitfall 1 | If wrong the Center/None guard is harmless; if right and unguarded, the 92dp test fails by several dp |
| A2 | `LineHeightStyle(Center, Trim.None)` with `includeFontPadding = false` makes the line box exactly `lineHeight` | Pitfall 1 | The Wave-0 height probe fails; fallback is `Modifier.heightIn(min = lineHeightDp)` per Text |
| A3 | `TextLayoutResult.layoutInput.style` exposes the merged `color` and `textDecoration` for tone/strikethrough assertions | Pattern 3 | Tone/strike tests fall back to source-contract tests |
| A4 | `ModalBottomSheet` applies `shape` to its container so a recording `Shape` sees `createOutline` under Robolectric | Validation (VSHEET-01) | Probe test dropped; source-contract test remains the proof |
| A5 | Material-branch "38% alpha" for a disabled option is satisfied by M3's own disabled colors (`DisabledLabelTextOpacity`, standard value 0.38), not an extra `Modifier.alpha` (which would double-dim) | Open Question 2 | Owner wants an exact alpha on the Material path; costs a visible wrapper |
| A6 | Framework expands `selectable` touch bounds to 48dp like `clickable` (comment verified for clickable) | Don't Hand-Roll | Add a `touchBoundsInRoot` assertion; if short, add `minimumInteractiveComponentSize` |
| A7 | `Row` measures unweighted children in declaration order, then weighted ones | Pitfall 3 | Edge test catches a crushed chevron |
| A8 | `ReceiptRow` is `Tier.PATTERN` (baked-in disclosure convention, fixed anatomy) and belongs in `DOMAIN_VOCABULARY` | Open Question 4 | Wrong tier/entry is a one-line change; no test pins it |
| A9 | Modifier order `tag -> semantics -> clickable -> padding` yields a node/click area of the full 92dp | Pattern 2 | 92dp bounds test fails; reorder |
| A10 | `internal` composable seams are invisible to both drift guards and to `api.txt` | Pattern 4 | Guard fails loudly; make them `private` and test through the row instead |
| A11 | BOM 2026.04.01 ships a Compose UI where `LineHeightStyle.Default` equals the androidx-main value (the doc was fetched from `androidx-main`) | Pitfall 1 | Same as A1 |
| A12 | `internal fun`/`const` names inside `ReceiptRow.kt` do not collide with Phase 20 internals (`DISABLED_CONTENT_ALPHA` is reused, not redefined) | Don't Hand-Roll | Compile error, trivial fix |

## Open Questions

1. **Pill selected-segment shape (inset pill vs full-bleed half).**
   - What we know: the CT 004 sketch draws the selected segment full-bleed inside a clipped, 1px-bordered, `border-radius:999px` container. [VERIFIED: sketch index.html:14-17]
   - What's unclear: the Compose spec text only says "one full-radius container... selected segment primary/onPrimary".
   - Recommendation: full-bleed fill clipped by the container (as in the Skeleton). Phase 24 screencaps are the check.
2. **Does D-08's "38% alpha" need an explicit alpha on the Material path?**
   - What we know: whole-control `enabled=false` already uses "the Material3 default disabled treatment" (existing KDoc) and the M3 default disabled content color uses a token alpha (`DisabledLabelTextOpacity`). [CITED: androidx-main SegmentedButton.kt via WebFetch; the numeric 0.38 is not in the file, MEDIUM]
   - Recommendation: Material = per-segment `enabled` only (M3 treatment); Pill = explicit `alpha(0.38f)`. Document in KDoc; assert `enabled=false` in both and `alpha` via source-contract on Pill.
3. **A value wide enough to crush the chevron.** See Pitfall 3. Recommendation: edge test, no custom `Layout`.
4. **Tier and vocabulary route for `ReceiptRow`.** Recommendation: `Tier.PATTERN` (it bakes in a chevron/disclosure convention and a fixed anatomy) and an acknowledged `DOMAIN_VOCABULARY` entry (mirrors `PlaceMapPicker`'s "acknowledged explicitly" precedent, keeps the allowlist for truly generic nouns). Adding `"Receipt"` to `PRIMITIVE_NOUN_ALLOWLIST` is equally guard-green; pick exactly one. `ComponentRegistryTierTest` only pins three other names.
5. **A test-only tag for the chevron.** Counting chevrons needs a hook that is not one of the Y-14 tags. Recommendation: `receipt_row_chevron`, documented as a test-only hook exactly like Phase 20's `stacked_progress_bar_segment`; keep it out of the stable-tag docs in Phase 24. [VERIFIED precedent: 20-04-SUMMARY key-decisions]
6. **Y-14 tag override.** `ReceiptRow` tags live on inner nodes, so a caller `testTag` on `modifier` goes on the root and does not override them. This is fine (the root has no hub tag); state it in the KDoc.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | Gradle/AGP | ✓ | openjdk 17.0.19 | — |
| Gradle wrapper | all gates | ✓ (not run, by constraint) | 9.4.1 per 20-RESEARCH.md | — |
| Android SDK / Robolectric | compile + tests | ✓ | `local.properties` set; Robolectric 4.16.1 | — |
| Metalava / detekt | `apiDump` `apiCheck` `detekt` | ✓ | resolved in build | — |
| git hooks / lane classifier | commits | ✓ | tag `v2.5.1` present | — |
| TESTER device | Phase 24 screencaps only | not needed here | — | — |
| Other gradle JVMs | build lock | ⚠ 2 Java processes (gradle) running at probe time | — | Keep one invocation at a time; do not start a second concurrently |

**Missing dependencies with no fallback:** none.

## Validation Architecture

> `workflow.nyquist_validation` is `true` in `.planning/config.json` [VERIFIED: file read this session].

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric 4.16.1 (`@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`) + Compose ui-test (`createComposeRule`) |
| Config file | `build.gradle.kts` (`testOptions { unitTests { isIncludeAndroidResources = true } }`) |
| Geometry harness | `@Config(sdk = [35], qualifiers = "w360dp-h800dp")` + `@GraphicsMode(GraphicsMode.Mode.NATIVE)` |
| Quick run command | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.<pkg>.<Class>" [--tests ...]` (batch ALL filters of a task into ONE invocation) |
| Full suite command | `./gradlew apiDump testDebugUnitTest detekt apiCheck` then `bash tools/test/run-all.sh` |
| Lane check | `bash tools/classify-hub-change.sh --baseline v2.5.1` on the staged diff before each commit |
| Static | `./gradlew detekt` (zero baseline) |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command (class filter) | File Exists? |
|--------|----------|-----------|----------------------------------|-------------|
| VROW-01 | Wave-0 line-height probe: titleMedium/bodyMedium/bodySmall single-line heights 24/20/16 dp | Robolectric NATIVE | `--tests "…component.ReceiptRowLineHeightTest"` | ❌ Wave 0 |
| VROW-01 | Collapsed height 92±1 (all lines, no cue), 72±1 (no supporting), +28±1 with cue; cue pill 24±1 tall with 4dp gap; value column min 56dp, End-aligned | Robolectric NATIVE | `--tests "…component.ReceiptRowTest"` | ❌ Wave 0 |
| VROW-01 | Long German title at 360dp: value not ellipsized and fully inside; title single-line ellipsized; chevron still inside the window; very-long-value edge | Robolectric NATIVE | `--tests "…component.ReceiptRowTest"` | ❌ Wave 0 |
| VROW-01 | Chevron only when `onClick != null && trailingAction == null` (3-cell matrix, via test-only tag) | Robolectric | `--tests "…component.ReceiptRowTest"` | ❌ Wave 0 |
| VROW-01 | `expandedContent` full width, top >= summary bottom, absent when collapsed or null; `showDivider` adds 1dp | Robolectric NATIVE | `--tests "…component.ReceiptRowTest"` | ❌ Wave 0 |
| VROW-01 | `dimmed`: title and subtitle `LineThrough` (value is not); non-dimmed none | Robolectric NATIVE | `--tests "…component.ReceiptRowTest"` | ❌ Wave 0 |
| VROW-01 | Chevron 0 -> 180 over 200ms and dim alpha 1 -> 0.38 over 200ms (clock-controlled seams); constants and seam usage pinned | Robolectric (`mainClock`) + source contract | `--tests "…component.ReceiptRowMotionTest" --tests "…component.ReceiptRowSourceContractTest"` | ❌ Wave 0 |
| VROW-01 | Tone colors: Default/Muted/Warn/Error resolve to onSurface / onSurfaceVariant / `warnTone.content` / `ToneDefaults.error().content`; no `errorTone` identifier in `ReceiptRow.kt` | Robolectric + source contract | `--tests "…component.ReceiptRowTest" --tests "…component.ReceiptRowSourceContractTest"` | ❌ Wave 0 |
| VROW-02 | Summary is ONE merged node: text contains title, subtitle, value, caption, cue; `Role.Button` + `OnClick.label == onClickLabel`; `StateDescription` toggles expanded/collapsed labels; no Role/StateDescription when `onClick == null` | Robolectric | `--tests "…component.ReceiptRowTest"` | ❌ Wave 0 |
| VROW-02 | `trailingAction` and `expandedContent` are separate nodes (`!hasAnyAncestor(hasTestTag("receipt_row_summary"))`); tapping the trailing action does not call the row `onClick`; tapping the summary calls it once | Robolectric | `--tests "…component.ReceiptRowTest"` | ❌ Wave 0 |
| VSEG-01 | Pill: 40±1dp tall, label centered identically selected/unselected (no check-icon slot), selected label color `onPrimary`, others `onSurface`, `RadioButton` role + `assertIsSelected`, same `contentDescription` strings as Material | Robolectric NATIVE | `--tests "…component.SegmentedOptionSelectorPillTest"` | ❌ Wave 0 |
| VSEG-01 | `disabledOptions = {0}` in BOTH styles: seg 0 `assertIsNotEnabled` and never fires `onSelect`; seg 1 enabled and fires `onSelect(1)`; Pill 38% alpha (source contract); whole-control `enabled=false` + `disabledReason` caption still shown in Pill | Robolectric NATIVE + source contract | `--tests "…component.SegmentedOptionSelectorPillTest"` | ❌ Wave 0 |
| VSEG-01 | German long labels in Pill at 360dp: one line, ellipsized (documented truncation), inside segment edges, equal segment heights | Robolectric NATIVE | `--tests "…component.SegmentedOptionSelectorPillTest"` | ❌ Wave 0 |
| VSEG-01 | Material parity: `SegmentedOptionSelectorTest` + `ApproachLadderCardLongLabelTest` + `ApproachLadderCardTest` pass UNEDITED; Material block pinned (`IntrinsicSize.Min`, `maxLines = 2`, `reserveSegmentIconSlot()`, `SegmentedButtonDefaults.colors()`, the single edited `enabled` line); `require` precedes the Pill guard; guard precedes `Column(` | Robolectric NATIVE + source contract | `--tests "…component.SegmentedOptionSelectorTest" --tests "…component.ApproachLadderCardLongLabelTest" --tests "…component.ApproachLadderCardTest" --tests "…component.SegmentedOptionMaterialParitySourceContractTest"` | existing ✅ (must stay byte-identical) / new ❌ Wave 0 |
| VSHEET-01 | `shape` param is declared before `content`, default is `BottomSheetDefaults.ExpandedShape`, forwarded as `shape = shape`; a recording `Shape` passed through `SheetScaffold` is invoked (probe-gated); existing `SheetScaffold` callers (AlbumTitleConfirmSheet, OutcomeSheet tests) still pass | source contract + Robolectric | `--tests "…component.SheetScaffoldShapeTest" --tests "…component.OutcomeSheetTest" --tests "…component.AlbumTitleConfirmSheetComposeTest"` | ❌ Wave 0 / existing ✅ |
| Registry (D-09) | `ReceiptRow` is a Cards entry, PATTERN, canonical four labels, renders in both themes; the Variants accordion is single-open (open row 2 -> exactly one `receipt_row_expanded_content`, under row 2); selector/sheet Variants still render | JVM + Robolectric | `--tests "io.github.ygaray.yahirandroidtaste.explorer.*"` | ✅ guards existing; ❌ new `explorer.ReceiptRowRegistryRenderTest` |
| Vocabulary | `ReceiptRow` head token acknowledged | JVM | `--tests "…explorer.DomainVocabularyDriftGuardTest"` | ✅ existing (edit the allowlist/map) |
| Dimens | new tokens + old values unchanged | JVM | `--tests "…theme.DimensTest"` | ✅ extend (append methods only) |
| API | `api.txt` regenerated; `apiCheck` green; `git diff --numstat v2.5.1 -- api.txt` shows exactly 3 removed lines | Gradle | `./gradlew apiDump apiCheck` | ✅ |
| Invariants | no `testTagsAsResourceId`, no consumer imports/colors in the new files | JVM | `--tests "…component.ToneFoundationInvariantsTest" --tests "…component.ReceiptRowSourceContractTest"` | ✅ existing + ❌ new |

### Sampling Rate
- **Per task commit:** ONE gradle invocation: `apiDump` (only if public API changed) + `testDebugUnitTest` with that task's `--tests` filters PLUS `io.github.ygaray.yahirandroidtaste.explorer.*` (cheap; catches the registry, states-matrix, vocabulary and generated-symbol guards) + `bash tools/classify-hub-change.sh --baseline v2.5.1` after staging. The Pill/selector commit that edits the `enabled` line is expected to print `LANE 2`; commit with `HUB_LANE_OVERRIDE=2`.
- **Per wave merge:** `./gradlew apiDump testDebugUnitTest detekt apiCheck` then `bash tools/test/run-all.sh`.
- **Phase gate:** full suite green; `git diff v2.5.1 -- src/test/.../SegmentedOptionSelectorTest.kt ApproachLadderCardLongLabelTest.kt ApproachLadderCardTest.kt` empty (unedited); `git diff --numstat v2.5.1 -- api.txt` removed == 3 and the two selector/sheet lines are the only new removals; `grep -rn testTagsAsResourceId src/main` empty.

### Wave 0 Gaps
- [ ] `component/ReceiptRowLineHeightTest.kt` — the 24/20/16 probe (runs first; decides nothing, because Center/None is unconditional, but pins the S1 assumption forever)
- [ ] `component/ReceiptRowTest.kt` (geometry, German, tones, dimmed, expanded, semantics, trailing/expanded separation)
- [ ] `component/ReceiptRowMotionTest.kt` (`mainClock` seams)
- [ ] `component/ReceiptRowSourceContractTest.kt` (constants, tags, `Role.Button`, `mergeDescendants = true`, no `errorTone`, no `testTagsAsResourceId`, no `Color(0x`, import litmus)
- [ ] `component/SegmentedOptionSelectorPillTest.kt`
- [ ] `component/SegmentedOptionMaterialParitySourceContractTest.kt`
- [ ] `component/SheetScaffoldShapeTest.kt`
- [ ] `explorer/ReceiptRowRegistryRenderTest.kt`
- [ ] `explorer/DomainVocabularyDriftGuardTest.kt` — add the `ReceiptRow` acknowledgment (Open Question 4)
- [ ] `theme/DimensTest.kt` — append methods for the new token groups
- [ ] Framework install: none

## Security Domain

> `security_enforcement` is `true`, ASVS level 1 [VERIFIED: .planning/config.json].

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | — (no auth surface) |
| V3 Session Management | no | — |
| V4 Access Control | no | — |
| V5 Input Validation | limited | All strings are caller-supplied display text rendered as plain `Text`; `disabledOptions` indices outside `0..1` are inert (`index !in set`); `require(options.size == 2)` keeps the existing contract |
| V6 Cryptography | no | — |
| V14 Config / build | yes | Never set `testTagsAsResourceId` (library exposes tags, consumers opt in); no secrets; no network; one-way dependency (INV-01/INV-03) |

### Known Threat Patterns for this stack
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Test tags exposed as resource ids in release builds | Information disclosure | Library never sets `testTagsAsResourceId`; `ToneFoundationInvariantsTest` already scans all of `src/main`; repeat the grep in the phase gate |
| Sensitive text leaking via `title`/`value`/`stateDescription`/`contentDescription` into accessibility services | Information disclosure | KDoc on `ReceiptRow` and the selector: "static UI copy, never sensitive text" (Phase 20 precedent) |
| Layout DoS from an extreme string (very long title/value) | Denial of service | `maxLines = 1` + Ellipsis on title/subtitle/value; the title takes the leftover width; edge test for a very long value |

## Sources

### Primary (HIGH confidence, read this session)
- `.planning/phases/21-receipt-row-pill-selector-sheet-shape/21-CONTEXT.md`, `.planning/REQUIREMENTS.md`, `.planning/STATE.md`, `.planning/ROADMAP.md` (Phase 21), `.planning/v2.6-DECISION-MAP.md` (Phase 21), `.planning/seams/v2.6.0-SKETCH.md` (§0, §2 Y-2/Y-11/Y-13/Y-14, §3)
- CT `76-UI-SPEC.md` (YAT Evolution Asks, S1, S3, Spacing, Typography, Single-line rules, Copywriting, UI Considerations)
- CT sketches `002-voice-confirm-sheets/index.html` (row CSS) and `004-count-amounts-piece-weight/index.html` (`.seg` CSS)
- Phase 20: `20-RESEARCH.md`, `20-VALIDATION.md`, `20-01..04-SUMMARY.md`
- Source: `component/SegmentedOptionSelector.kt`, `SheetScaffold.kt`, `PillChip.kt`, `ToneCard.kt`, `ExpandableFab.kt` (rotation lines), `theme/ToneTokens.kt`, `Dimens.kt`, `Type.kt`, `Motion.kt`, `explorer/ComponentRegistry.kt`, `CardsFamilyScreen.kt`, `PickersFamilyScreen.kt`, `SheetsFamilyScreen.kt`, `ExplorerIndexScreen.kt`, `api.txt` (lines 385-393)
- Tests: `DomainVocabularyDriftGuardTest`, `ComponentRegistryDriftGuardTest`, `ComponentStatesMatrixTest`, `ComponentRegistryTierTest`, `ComponentPlaygroundIntegrityTest`, `SegmentedOptionSelectorTest`, `ApproachLadderCardLongLabelTest`, `ApproachLadderCardTest`, `ToneCardTest`, `ToneAtomsLongLabelTest`, `ToneAtomsRegistryRenderTest`, `DynamicActionButtonLegacyPathSourceContractTest`, `RoundIconButtonTest`, `SourceContractTestSupport`, `GalleryDemoInteractionTest`, `OutcomeSheetTest`, `AlbumTitleConfirmSheetTest`
- Tooling: `tools/verify-additive-diff.sh`, `tools/classify-hub-change.sh`, `tools/README-api-guard.md`, `config/detekt/detekt.yml`, `config/detekt-compose.yml`, `docs/DESIGN-INTENT.md`
- `javap` of `material3.aar` (`ModalBottomSheet_androidKt`, `BottomSheetDefaults`) for the resolved 1.4.0 binary

### Secondary (MEDIUM confidence)
- Context7 `/websites/developer_android_develop_ui_compose_testing`: `mainClock.autoAdvance = false`, `advanceTimeBy`, merged vs unmerged semantics tree output ([developer.android.com/develop/ui/compose/testing/synchronization](https://developer.android.com/develop/ui/compose/testing/synchronization), [.../testing/apis](https://developer.android.com/develop/ui/compose/testing/apis))
- [developer.android.com/develop/ui/compose/text/style-paragraph](https://developer.android.com/develop/ui/compose/text/style-paragraph): `LineHeightStyle` Alignment/Trim semantics, `includeFontPadding` default
- androidx-main (not the 1.4.0 tag) via WebFetch: `LineHeightStyle.kt` (`Default = Proportional + Trim.Both + Mode.Fixed`), `SegmentedButton.kt` (`selectableGroup()`, `Role.RadioButton`, disabled-color tokens), `SheetDefaults.kt` (`ExpandedShape` composable getter), `ModalBottomSheet.kt` (signature with `shape: Shape = BottomSheetDefaults.ExpandedShape`)

### Tertiary (LOW confidence)
- None relied on without a flag; the magnitude of the line-height shrink is `[ASSUMED]` (A1/A2) and is covered by the Wave-0 probe.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH (nothing new; versions read from the repo/binary)
- Architecture and insertion points: HIGH (exact current code read; lane behavior read from the scripts)
- Registry/vocabulary/API guards: HIGH (guard sources read)
- 92dp feasibility: MEDIUM (arithmetic verified; text-metric behavior documented but not run; mitigated by Center/None + probe)
- Pitfalls: HIGH for 2, 4, 5, 6, 8, 9, 12; MEDIUM for 1, 3, 7

**Research date:** 2026-10-12
**Valid until:** 2026-11-11 (stable toolchain; re-check if the Compose BOM or M3 is bumped)
