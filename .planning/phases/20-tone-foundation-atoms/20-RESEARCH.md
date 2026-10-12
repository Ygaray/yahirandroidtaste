# Phase 20: Tone foundation & atoms - Research

**Researched:** 2026-10-12
**Domain:** Jetpack Compose / Material 3 UI library (hub): theme tokens, one widened composable, four new atoms, registry/API gates
**Confidence:** HIGH on repo mechanics and gates; MEDIUM on Compose/M3 internals (flagged `[ASSUMED]`/`[CITED]` per claim)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01 [signatures]:** Public signatures are exactly those in `.planning/seams/v2.6.0-SKETCH.md` §2 Y-1, Y-8, Y-9, Y-10 and Y-12. CT and SB signed them off with no deltas. Do not rename or reorder params. — **Reversibility:** one-way — consumers code against the signed-off signatures.
- **D-02 [compat]:** Source compatibility only (P1). Do not add `@Deprecated(HIDDEN)` overloads, hand-written old-arity `copy`, hidden ctors or `copy$default` shims. New params go after `modifier` with defaults. New behavior uses new enums; no existing enum gains constants.
- **D-03 [api-dump]:** Regenerate `api.txt` (`./gradlew apiDump`) in the same commit as each public API change so `apiCheck` stays green. Expect lane-2 pre-commit classification when an existing source line is rewritten; declare it with `HUB_LANE_OVERRIDE=2`.
- **D-04 [tone-pkg]:** `ToneColors`, `ToneDefaults` and `ContentTone` live in `theme/`, since they are tokens like `Dimens`. _(ai-auto)_
- **D-05 [tone-defaults]:** `ToneDefaults.error()` = `errorContainer`/`error`, and `warn()` = `tertiaryContainer`/`onTertiaryContainer`. Both are `@Composable` and read `MaterialTheme.colorScheme`. `ToneColors` is an `@Immutable class` with value `equals`/`hashCode`. The KDoc says consumers pass their own tokens when dynamic color makes `tertiary` non-amber.
- **D-06 [atom-family]:** Register `RoundIconButton` → ButtonsFab, `StackedProgressBar` → Progress, `ToneCard` → Cards and `PillChip` → Chips, in this phase, so the drift guard stays green. _(ai-auto)_
- **D-07 [emphasis-impl]:** Build the emphases from M3 `Button`/`TextButton` with color and `contentPadding` overrides:
  - **Filled:** pill, `primary`/`onPrimary`, 20dp × 8dp padding, 40dp tall.
  - **Tonal:** a `Button` with `containerColor = primary.copy(alpha = 0.14f)` and `primary` content, same padding.
  - **Text:** a `TextButton`, 12dp × 8dp padding.
  - **TextSmall:** a `TextButton` with a `bodySmall` label, 8dp × 4dp padding, and a ≥ 48dp touch target.
  Filled, Tonal and Text labels are `labelLarge` SemiBold. An optional leading icon is 18dp with an 8dp gap. `role` picks the hue (Destructive → `error`, otherwise `primary`). With `emphasis = null`, the existing branch is untouched. _(ai-auto)_
- **D-08 [round-icon]:** `RoundIconButton` is a 40dp (or `size`) circle with a 1dp `outlineVariant` border, a transparent container and a 20dp `onSurface` icon, inside a 48dp touch target. It uses `outlineVariant`, not M3 `OutlinedIconButton`'s `outline`.
- **D-09 [bar]:** For `StackedProgressBar`, a negative fraction clamps to 0. Fractions accumulate left to right and the total clamps to ≤ 1. The bar clips to a fully rounded track. `trackColor` is required. Tag: `stacked_progress_bar`.
- **D-10 [tags]:** The atoms carry Y-14 tags from the start: `round_icon_button`, `stacked_progress_bar`, `tone_card`, `pill_chip`. A consumer `modifier.testTag` can override them. Never set `testTagsAsResourceId`.

### Claude's Discretion
- Internal file split, private helpers, and test file names.

### Deferred Ideas (OUT OF SCOPE)
None. This phase stays within its scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VTONE-01 | `ToneColors` / `ToneDefaults.error()/warn()` / `ContentTone` in `theme/` | Contrast already computed >= 4.5 on hub palettes (Verified Values); hand-written equals (not `data class`); test via `YahirAndroidTasteTheme(themeMode=…, dynamicColor=false)` |
| VBTN-01 | `DynamicActionButton` `emphasis`/`icon` + `ActionButtonEmphasis` | Early-branch pattern keeps legacy `when(role)` lines byte-identical; M3 `Button`/`TextButton` internals; source-contract + width-delta + touch-bounds tests |
| VBTN-02 | `RoundIconButton` | Own `Box`/`clickable` build (not `OutlinedIconButton`); `touchBoundsInRoot` assertion precedent; tag-override ordering |
| VBAR-01 | `StackedProgressBar` + `BarSegment` | Pure clamp function + Row/`weight` segments as taggable nodes; NaN guard; weight>0 pitfall |
| VCARD-01 | `ToneCard` | `Surface` + `Column` with existing `Dimens` tokens; `LocalContentColor` assertion; bounds tests |
| VCHIP-01 | `PillChip` | Mirror `PresetChip` semantics pattern; single-layer clickable root; long-German-label test |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- **One-way dependency:** library imports only Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable, osmdroid. No consumer package, no consumer nouns/colors, no secrets. [VERIFIED: /home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md]
- **Registry XOR rule:** every public top-level `@Composable` in `component/`, `feedback/`, `modifier/`, `theme/` is registered in a family list XOR in `INTENTIONALLY_UNREGISTERED`. [VERIFIED: CLAUDE.md + `ComponentRegistryDriftGuardTest.kt`]
- **Source-compatible, not binary-compatible:** append defaulted params; trailing lambdas stay last; no new `@Deprecated(HIDDEN)` overloads / `copy$default` shims. `apiCheck` is the per-commit gate; `verify-binary-abi.sh` informational only. [VERIFIED: CLAUDE.md]
- **No `@HiltAndroidApp` / `@AndroidEntryPoint`** in the library.
- **Detekt zero-baseline:** do not regenerate a baseline; fix or tune with justification. Commands drop the module prefix (`./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew apiDump`, `./gradlew apiCheck`).
- **Shipping is human-gated:** do not tag, push or repin in this phase. Commit on `main`, sequential-in-hub, no consumer worktrees.
- **Session constraints (binding):** unattended; one gradle invocation at a time; no push; never `git add .planning/` wholesale.
- **Memory lesson (long localized labels):** any caller-overridable label needs a long German/Spanish case at ~360dp before layout sign-off. [VERIFIED: memory `test-localized-long-strings-before-signoff`]

## Summary

Phase 20 is almost entirely additive Kotlin in a single-module Compose library: three theme tokens, one widened composable (`DynamicActionButton`) and four new atoms, each requiring (a) a registry entry with a full 4-cell states matrix + `content` + `tier`, (b) a regenerated `api.txt`, and (c) Robolectric/Compose tests. All signatures are frozen by the sketch; the planner's real freedom is file layout, internal helpers and test design.

Three non-obvious gate hazards will break `testDebugUnitTest` if missed. First, `DomainVocabularyDriftGuardTest` checks the leading PascalCase word of every new public composable against `PRIMITIVE_NOUN_ALLOWLIST`; `Round`, `Stacked`, `Tone` and `Pill` are not in it, so the four atoms fail that guard until the four tokens are appended (a `src/test` edit). Second, CONTEXT.md says pixel colors are asserted elsewhere with NATIVE graphics, but no `captureToImage` exists anywhere under `src/test` (NATIVE is used only for real text measurement), so pixel assertions would be a first for this repo and must be probe-gated with structural fallbacks. Third, the `ComponentStatesMatrixTest` gate requires exactly the four canonical state labels and non-null `content` on every new entry.

**Primary recommendation:** Implement `DynamicActionButton` as an early-return branch placed *before* the untouched legacy `when(role)` (keeps the legacy lines byte-identical, likely lane-1, and makes "same code path" provable by a source-contract test); build the four atoms as single-root composables that apply `modifier.testTag("<tag>")` on the interactive/root node; verify colors structurally (`LocalContentColor`, pure palette functions, contrast math) with at most one probe-gated NATIVE pixel test per atom.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Tone tokens (`ToneColors`, `ToneDefaults`, `ContentTone`) | Client UI theme layer (`theme/`) | — | Pure token types reading `MaterialTheme.colorScheme`; no state |
| Button emphasis, `RoundIconButton`, `PillChip` | Client UI component (`component/`) | — | Stateless state-in/callback-out widgets |
| `StackedProgressBar`, `ToneCard` | Client UI component (`component/`) | — | Presentational; caller supplies fractions/colors/content |
| Registry + gallery entries | Explorer harness (`explorer/`) | — | Drift-guard source of truth, not shipped behavior |
| API surface gate | Build/tooling (Metalava `api.txt`) | `tools/` hooks | Source-level compatibility gate |

(Single-tier library: no backend, network, persistence.)

## Standard Stack

### Core (all already in the build; nothing new to install)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Compose BOM | 2026.04.01 | UI toolkit | [VERIFIED: gradle/libs.versions.toml:12 `composeBom = "2026.04.01"`] |
| Material3 | 1.4.0 (resolved) | `Button`, `TextButton`, `Surface`, `Icon`, `MaterialTheme` | [VERIFIED: ~/.gradle cache `material3-android/1.4.0`] |
| material-icons-extended | via BOM | Icons (already a dependency) | [VERIFIED: build.gradle.kts:102 `implementation(libs.androidx.compose.material.icons.extended)`] |
| Robolectric | 4.16.1 | JVM Compose UI tests | [VERIFIED: libs.versions.toml:19] |
| compose ui-test-junit4 | via BOM | `createComposeRule`, `touchBoundsInRoot`, `captureToImage` | [VERIFIED: build.gradle.kts:133] |
| Metalava plugin | `me.tylerbwong.gradle.metalava` 0.5.0 | `apiDump`/`apiCheck` -> `api.txt` | [VERIFIED: tools/README-api-guard.md] |
| Detekt | project-pinned | zero-baseline static analysis | [VERIFIED: config/detekt/detekt.yml] |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Hand-built `RoundIconButton` | M3 `OutlinedIconButton` | Locked out by D-08 (draws `outline`); a thin own `Box`+`clickable` also removes dependence on M3 1.4 expressive container-size changes |
| Roborazzi/Paparazzi | — | Rejected by P3 (separate opt-in decision) |
| `data class ToneColors` | hand-written `equals/hashCode` | `data class` would add `copy`/`componentN` to `api.txt`; D-05 says plain `@Immutable class` |

**Installation:** none. **Package Legitimacy Audit:** not applicable — this phase installs no external packages.

## Architecture Patterns

### System Architecture Diagram

```
Consumer call site (SB / CT / Explorer)
        |
        v
 theme/ToneColors, ToneDefaults.error()/warn(), ContentTone      (tokens, read MaterialTheme.colorScheme)
        |                                  \
        v                                   \---> component/ToneCard(tone, content)  --> Surface + Column(12dp x 16dp, gap 4dp)
 component/DynamicActionButton(label, role, onClick, enabled, modifier, emphasis?, icon?)
        |-- emphasis == null && icon == null --> [UNCHANGED legacy when(role): Button | TextButton]
        '-- else --> effective emphasis = emphasis ?: legacyMapping(role) --> Button/TextButton + hue(role) + padding + label style + optional 18dp icon
 component/RoundIconButton  -> Box(48dp touch target) -> 40dp circle (1dp outlineVariant) -> 20dp Icon
 component/PillChip         -> single clickable root, 32dp tall, CircleShape, 1dp outlineVariant, bodySmall 1 line, 18dp icons
 component/StackedProgressBar(segments, trackColor) --> clampSegmentFractions() --> clipped pill track --> Row of weighted segment Boxes
        |
        v
 explorer/*FamilyScreen.kt (ButtonsFab, Progress, Cards, Chips) -> ComponentRegistry.entries (drift guards)
        |
        v
 api.txt (apiDump) <-> apiCheck ; tools/classify-hub-change.sh (lane 1/2) in pre-commit
```

### Recommended Project Structure
```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── theme/ToneTokens.kt            # ToneColors, ToneDefaults, ContentTone (D-04) [+ optional Dimens appends]
├── component/DynamicActionButton.kt  # append ActionButtonEmphasis, emphasis branch, internal palette fn
├── component/RoundIconButton.kt
├── component/StackedProgressBar.kt   # + BarSegment + internal clampSegmentFractions
├── component/ToneCard.kt
├── component/PillChip.kt
└── explorer/{ButtonsFab,Progress,Cards,Chips}FamilyScreen.kt  # append entries + Variants
src/test/java/.../{theme,component,explorer}/   # see Validation Architecture
```

### Pattern 1: Early-branch widening that keeps the legacy path byte-identical
**What:** Append `emphasis`/`icon` params, then insert *above* the existing `when (role)`: `if (emphasis != null || icon != null) { EmphasisButton(...); return }`. Do not re-indent or edit the legacy lines.
**Why:** `verify-additive-diff.sh` normalizes only a trailing `,`/`+`; leading indentation is not stripped, so wrapping the legacy block in `if/else` rewrites every line (lane 2) and weakens the "same code path" evidence. [VERIFIED: tools/verify-additive-diff.sh normalize(): `-e 's/^.//' -e 's/[[:space:]]+$//' -e 's/[+,]$//'`]
**Note:** Early `return` inside a Unit `@Composable` is permitted by the Compose compiler `[ASSUMED]` (widely used; no detekt `ReturnCount` rule is enabled here: only LongMethod, LongParameterList, CyclomaticComplexMethod, naming, UnusedPrivateMember are active [VERIFIED: config/detekt/detekt.yml]).
**Decision the sketch does not state (planner must lock):** `emphasis == null && icon != null`. Recommend: effective emphasis = legacy mapping (`Save` -> `Filled`, else `Text`), then add the icon. [ASSUMED — A4]

### Pattern 2: Single-root atoms with the library tag appended to the caller's modifier
Apply `modifier.testTag("<tag>")` (caller modifier first) on the root/interactive node, as `ModelSelectCard` already does (`modifier = modifier.testTag("model_select_card_surface")`). Compose builds a node's semantics config `tailToHead` (innermost modifier first) and a later `set` overwrites, so the *outermost* modifier — the caller's — wins. [CITED: androidx `LayoutNode.calculateSemanticsConfiguration` uses `nodes.tailToHead(Nodes.Semantics)`; `SemanticsConfiguration.collapsePeer` doc: outermost value used] The override only works if both tags land on the **same layout node**, so the tag must sit on the same chain as `modifier`, not on a nested child. Pin with a test (D-10). [MEDIUM]

### Pattern 3: Structural color verification (no pixels needed)
- `ToneDefaults`: read inside `setContent { MaterialTheme(colorScheme = …) { captured = ToneDefaults.error() } }` and `assertEquals(ToneColors(cs.errorContainer, cs.error), captured)`.
- `ToneCard` content color: capture `LocalContentColor.current` inside the card `content` lambda.
- Button hue/palette: factor an `internal fun` (e.g. `emphasisPalette(emphasis, role, scheme)` returning container/content) and unit-test it with `LightColorScheme`/`DarkColorScheme` (both are public vals in `theme/Theme.kt`). [VERIFIED: Theme.kt `val LightColorScheme`/`val DarkColorScheme`]
- Contrast: WCAG ratio from `Color.luminance()` over the resolved `ToneColors`.
- Pixel tests (container fill, track pixels): `@GraphicsMode(NATIVE)` + `captureToImage().toPixelMap()`; **probe first** (Open Question 1).

### Anti-Patterns to Avoid
- **`data class ToneColors`** — leaks `copy`/`component1/2` into `api.txt`.
- **Wrapping the legacy `when` in `if/else`** — rewrites existing lines (lane 2).
- **`Modifier.fillMaxWidth(fraction)` for bar segments inside a `Row`** — a Row child's max width is the *remaining* width, so fractions compound; use `weight` or a clamped-cumulative layout.
- **Zero-weight children** — `Modifier.weight(0f)` throws; skip zero-fraction segments.
- **Putting the clickable on a 32dp inner `Surface` with a non-clickable 48dp outer `Box` that carries the tag** (the `AppChip` shape) — the tagged node is then not the clickable node. Prefer one root that is clickable, tagged and described.
- **`@Composable fun Foo(` on one line, or the annotation not alone on its line at column 0** — `ComponentRegistryDriftGuardTest` only recognises a line whose trim is exactly `@Composable` at column 0, then a column-0 `fun`; a one-line form silently escapes both guards.
- **Setting `testTagsAsResourceId`** — never (grep confirms none in `src/main` or `src/test` today).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Button widgets for Filled/Tonal/Text/TextSmall | Custom clickable + ripple + disabled colors | M3 `Button`/`TextButton` with `colors`, `contentPadding`, `shape` (D-07) | Ripple, disabled alpha, `Role.Button`, focus come free |
| Touch-target semantics | Custom pointer plumbing | `minimumInteractiveComponentSize()` / `clickable`; assert `touchBoundsInRoot` | Framework already expands clickable touch bounds to 48dp [VERIFIED: comment in ApproachLadderCardTest.kt:393-396] |
| Disabled gating | Manual `if (enabled)` around `onClick` | `clickable(enabled = …)` / M3 `enabled` | Correct disabled semantics node |
| Contrast math | New color library | `androidx.compose.ui.graphics.luminance` + 4-line WCAG ratio in the test | Same formula used for the verified numbers below |
| Rounded track clipping | Path drawing | `Modifier.clip(CircleShape)` on a fixed-height `Box` | 50% corners = fully rounded for any width |
| Source-contract parsing | New regex helpers | `SourceContractTestSupport` (`source`, `stripComments`, `functionBody`, `countOccurrences`) | Already used by 4 contract tests; note `source()` is hard-wired to `component/` |

**Key insight:** every dimension/color in this phase has an existing token or M3 default; the work is wiring + proof, not invention.

## Runtime State Inventory

Not applicable — greenfield additive phase (no rename/refactor/migration). None — verified by the Phase 20 scope in CONTEXT.md (new types + one widened composable; no stored data, services, OS registrations, env vars or renamed artifacts).

## Common Pitfalls

### Pitfall 1: DomainVocabularyDriftGuardTest rejects the four new names
**What goes wrong:** `testDebugUnitTest` fails: head tokens `Round`, `Stacked`, `Tone`, `Pill` are neither in `PRIMITIVE_NOUN_ALLOWLIST` nor `DOMAIN_VOCABULARY`.
**Why:** Guard scans every non-`explorer` public top-level `@Composable`; `HEAD_TOKEN_REGEX = Regex("[A-Z]+(?=[A-Z][a-z]|$)|[A-Z][a-z0-9]*")` yields `Round`/`Stacked`/`Tone`/`Pill`. A grep of the test for `"Round"`, `"Stacked"`, `"Tone"`, `"Pill"` finds nothing. [VERIFIED: DomainVocabularyDriftGuardTest.kt + grep this session]
**How to avoid:** In the same task that adds each atom, append the token to `PRIMITIVE_NOUN_ALLOWLIST` with a phase comment (the file's own convention, e.g. the `"Date"`, `"Preset"`, `"Outcome"` additions). This is a `src/test` edit (not covered by the `src/main` lane classifier).
**Warning signs:** failure message "whose leading name token is neither an established UI-primitive noun…".

### Pitfall 2: "NATIVE graphics pixel assertions" have no precedent here
**What goes wrong:** Plans assume an existing pixel-test pattern; there is none. `captureToImage` appears nowhere under `src/test`; `ApproachLadderCardGalleryDemoTest` uses NATIVE only for text measurement, and `CardTagRowTest`/`FailureRoleSourceContractTest` headers state the harness is not used to assert rendered colors. [VERIFIED: grep `captureToImage|toPixelMap|getPixel` over src/test returned nothing; headers read this session]
**How to avoid:** Make color wiring provable structurally (Pattern 3). Add one `@GraphicsMode(NATIVE)` pixel test per atom only after a throwaway probe proves `captureToImage` works in this harness; if it does not, keep the structural proof and record a Gate-2 note rather than weakening requirements.

### Pitfall 3: Registry entries need all four canonical state cells, content and tier
**What goes wrong:** `ComponentStatesMatrixTest` fails: states must equal `listOf("Default", "Pressed / Selected", "Disabled", "Focused")` in order and `content` must be non-null. `Entry.tier` has no default. N/A cells are `ComponentRegistry.StateCell("<label>")` with no render lambda (documented "Not applicable"). [VERIFIED: ComponentStatesMatrixTest.kt + ComponentRegistry.kt `data class Entry`]
**How to avoid:** copy the `MetricBar`/`PresetChip`-style entry shape; put real renders in Default and, where meaningful, Disabled (`PillChip`, `RoundIconButton`).

### Pitfall 4: M3 Button internals set minimum sizes the spec ignores
**What goes wrong:** `TextSmall` "8dp × 4dp padding" is expected to shrink the control; M3's inner Row applies `defaultMinSize(minWidth = ButtonDefaults.MinWidth /*58dp*/, minHeight = ButtonDefaults.MinHeight /*~40dp*/)` before padding, so Text/TextSmall are never shorter than ~40dp or narrower than 58dp. [CITED: androidx-main Button.kt via WebFetch; version-skew from 1.4.0 possible — MEDIUM]
**How to avoid:** Write bounds tests as `>=` (touch >= 48dp, drawn height >= 40dp where Button-based) rather than equality; document that padding governs horizontal rhythm only.

### Pitfall 5: Tonal container is translucent; Button elevation shadows bleed through
**What goes wrong:** `containerColor = hue.copy(alpha = 0.14f)` on a default-elevation `Button` draws elevation shadow under a see-through fill (visible when pressed/focused).
**How to avoid:** pass `elevation = null` for Tonal (and Text/TextSmall already default to none). [ASSUMED — A7; confirm visually on the Phase 24 screencaps]

### Pitfall 6: Robolectric legacy graphics does not wrap/measure real text
**What goes wrong:** German-label tests pass vacuously. The G-15-1 precedent needed `@GraphicsMode(NATIVE)` and `w360dp-h800dp`, and reads geometry **unclipped** (`positionInRoot` + `size`) because `boundsInRoot` is clipped to ancestors. [VERIFIED: ApproachLadderCardLongLabelTest.kt header + class annotations]
**How to avoid:** copy that harness for the long-label tests; assert node width <= 360dp and no ellipsis on `DynamicActionButton` (wraps), and an ellipsized-but-in-bounds label for `PillChip` (single line by spec).

### Pitfall 7: Theme defaults to dynamic color
**What goes wrong:** `YahirAndroidTasteTheme(dynamicColor = true)` is the default; under Robolectric, scheme-dependent assertions drift. [VERIFIED: Theme.kt signature `dynamicColor: Boolean = true`]
**How to avoid:** tests use `YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT|DARK, dynamicColor = false)` (existing tests already pass `dynamicColor = false`), or `MaterialTheme(colorScheme = LightColorScheme|DarkColorScheme)`.

### Pitfall 8: `Float` NaN/negative handling in the bar
`NaN.coerceAtLeast(0f)` stays NaN (comparison is false). Sanitise NaN explicitly before clamping; clamp cumulatively (truncate the segment that crosses 1, do not rescale). `remaining -= v` with `v <= remaining` never goes negative in IEEE arithmetic.

### Pitfall 9: Stale counts in docs
`ComponentRegistry` KDoc, `README.md` ("61 registered … 5 intentionally-unregistered") and `API.md` "Surface at a glance" counts will be stale after registering 4 entries. They are not enforced by any test (grep: only `ShowTagColorsSourceCompatTest` mentions API.md). Docs are Phase 24 (DOC-03); do not edit them here, but note it in the SUMMARY.

### Pitfall 10: One gradle at a time
Several `--tests` filters must be batched into one invocation; `--tests` binds to the task before it, so order is `./gradlew apiDump testDebugUnitTest --tests A --tests B detekt apiCheck`.

## Code Examples

> Illustrative skeletons — **not compile-verified** this session (no gradle run). Material/Compose call shapes are `[ASSUMED]` unless cited; repo constants are in "Verified in-repo values".

### Tone tokens (`theme/ToneTokens.kt`)
```kotlin
@Immutable
class ToneColors(val container: Color, val content: Color) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is ToneColors && container == other.container && content == other.content)
    override fun hashCode(): Int = 31 * container.hashCode() + content.hashCode()
    override fun toString(): String = "ToneColors(container=$container, content=$content)"
}

object ToneDefaults {
    /** errorContainer / error. KDoc: pass your own tokens if dynamic color makes tertiary non-amber (warn). */
    @Composable fun error(): ToneColors =
        MaterialTheme.colorScheme.let { ToneColors(it.errorContainer, it.error) }
    @Composable fun warn(): ToneColors =
        MaterialTheme.colorScheme.let { ToneColors(it.tertiaryContainer, it.onTertiaryContainer) }
}

enum class ContentTone { Default, Muted, Warn, Error }
```

### DynamicActionButton early branch (legacy lines untouched)
```kotlin
fun DynamicActionButton(
    label: String,
    role: ActionButtonDefaults.ActionButtonRole,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,                 // gains a trailing comma (tolerated by the guard)
    emphasis: ActionButtonEmphasis? = null,
    icon: ImageVector? = null
) {
    if (emphasis != null || icon != null) {
        EmphasisActionButton(label, role, onClick, enabled, modifier,
            emphasis ?: legacyEmphasisFor(role), icon)
        return
    }
    when (role) { /* …existing lines, byte-identical… */ }
}
enum class ActionButtonEmphasis { Filled, Tonal, Text, TextSmall }   // top-level, public (component/)
```

### Clamp + bar (pure function is the unit-test seam)
```kotlin
internal fun clampSegmentFractions(raw: List<Float>): List<Float> {
    var remaining = 1f
    return raw.map { f ->
        val v = if (f.isNaN()) 0f else f.coerceIn(0f, remaining)
        remaining -= v
        v
    }
}
// Track: Box(modifier.testTag("stacked_progress_bar").fillMaxWidth().height(height).clip(CircleShape).background(trackColor))
//   Row(Modifier.matchParentSize()) { fractions.forEachIndexed { i, f -> if (f > 0f) Box(Modifier.weight(f).fillMaxHeight().background(segments[i].color).testTag("stacked_progress_bar_segment")) }
//                                     Spacer(Modifier.weight(restOrNothing)) }   // rest = 1 - sum, only if > 0
```

### Registry entry shape (append; last entry's `)` -> `),` is tolerated by the additive guard)
```kotlin
ComponentRegistry.Entry(
    name = "PillChip",
    family = ExplorerFamilies.CHIPS,
    states = listOf(
        ComponentRegistry.StateCell("Default") { PillChip(label = "Today", onClick = {}) },
        ComponentRegistry.StateCell("Pressed / Selected"),
        ComponentRegistry.StateCell("Disabled") { PillChip(label = "Today", onClick = {}, enabled = false) },
        ComponentRegistry.StateCell("Focused")
    ),
    content = { PillChipVariants() },
    tier = ComponentRegistry.Tier.PRIMITIVE
)
```

### Test idioms to copy
- Touch target: `fetchSemanticsNode().touchBoundsInRoot` with `with(density) { 48.dp.toPx() }` (ApproachLadderCardTest.kt:393-400).
- Long German: class-level `@Config(sdk = [35], qualifiers = "w360dp-h800dp")` + `@GraphicsMode(GraphicsMode.Mode.NATIVE)`; unclipped geometry via `positionInRoot` + `size`; ellipsis via `GetTextLayoutResult` (`isLineEllipsized(0)`).
- Icon presence without a new tag: width of the same button with and without `icon` differs by ~26dp (18 + 8) ± 1px (use a label wide enough to exceed the 58dp min width).

## Verified in-repo values (verbatim, read this session)

- DynamicActionButton today: `fun DynamicActionButton(` … `label: String,` `role: ActionButtonDefaults.ActionButtonRole,` `onClick: () -> Unit,` `enabled: Boolean = true,` `modifier: Modifier = Modifier` [VERIFIED: component/DynamicActionButton.kt]. Legacy body: `ActionButtonDefaults.ActionButtonRole.Save -> Button(` … `else -> TextButton(`. Enum: `enum class ActionButtonRole { Destructive, Save, Neutral }`.
- api.txt today: `method @KotlinOnly @androidx.compose.runtime.Composable public static void DynamicActionButton(String label, io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults.ActionButtonRole role, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, optional boolean enabled, optional androidx.compose.ui.Modifier modifier);` [VERIFIED: api.txt:158]. Expect exactly this line to be rewritten (params appended) plus new classes/methods; no other removal.
- Dimens: `val HorizontalPadding: Dp = 16.dp`, `val ContentSpacing: Dp = 4.dp`, `val TouchTarget: Dp = 48.dp`, `val CompactPadding: Dp = 12.dp`, `val HairlineBorder: Dp = 1.dp`, `val Medium: Dp = 12.dp` (in `object CornerRadius`), `val ChipIcon: Dp = 18.dp` and `val MenuIcon: Dp = 20.dp` (in `object Icons`) [VERIFIED: theme/Dimens.kt]. `DimensTest` regression-locks every existing value, so only append new tokens.
- Registry: `const val CARDS = "cards"`, `const val CHIPS = "chips"`, `const val BUTTONS_FAB = "buttons_fab"`, `const val PROGRESS_METRICS = "progress_metrics"` [VERIFIED: explorer/ExplorerIndexScreen.kt]; `enum class Tier { PRIMITIVE, PATTERN }`; list names `cardsFamilyEntries`, `chipsFamilyEntries`, `buttonsFabFamilyEntries`, `progressFamilyEntries` [VERIFIED: ComponentRegistry.kt].
- Guard allow-list scope: `private val excludedPackages = setOf("explorer")` [VERIFIED: ComponentRegistryDriftGuardTest.kt].
- Hub palettes (Color.kt): light `ErrorRed = Color(0xFFBA1A1A)`, `ErrorRedContainer = Color(0xFFFFDAD6)`, `AmberOnTertiaryContainer = Color(0xFF251A00)`, `AmberTertiaryContainer = Color(0xFFFFE0A0)`; dark `ErrorRedDark = Color(0xFFFFB4AB)`, `ErrorRedContainerDark = Color(0xFF93000A)`, `AmberOnTertiaryContainerDark = Color(0xFFFFE0A0)`, `AmberTertiaryContainerDark = Color(0xFF563D00)` [VERIFIED: theme/Color.kt]. **Computed WCAG contrast (this session, python from those hex values):** light error/errorContainer 5.00, dark 5.51; light warn 13.39, dark 7.96 — all >= 4.5, so VCARD-01's contrast criterion holds on the hub schemes with `content = error` as D-05 specifies.
- Tonal (primary @14% over background) contrast, computed: light 5.02, dark 7.31; destructive light 4.85, dark 7.50. Filled: onPrimary/primary light 6.49, dark 7.66.
- Detekt rules active: `LongMethod` 240, `LongParameterList` 18 (`ignoreDefaultParameters: true`), `CyclomaticComplexMethod` 26, naming (`FunctionNaming ignoreAnnotated: ['Composable']`), `UnusedPrivateMember`; `maxIssues: 0`; baseline empty [VERIFIED: config/detekt-compose.yml, detekt.yml, detekt-baseline.xml].
- Pre-commit: baseline = `git describe --tags --abbrev=0 --match 'v*'` -> `v2.5.1`; lane 2 requires `HUB_LANE_OVERRIDE=2` [VERIFIED: tools/hooks/pre-commit + `git describe` run this session]. The lane guard diffs **only `src/main`** staged-vs-HEAD [VERIFIED: verify-additive-diff.sh `git ls-tree … HEAD -- src/main`].

## State of the Art

| Old Approach | Current Approach | Impact |
|--------------|------------------|--------|
| Binary-compat shims for every signature change | Source-compat only (v2.6 P1) | No hidden overloads/`copy$default`; the v2.5.0 shims stay untouched |
| `verify-api-additive.sh` | `apiCheck` on regenerated `api.txt` + `tools/test/run-all.sh` + Phase 24 canary | Retired in Phase 19; `api.txt` diff is signature-line rewrite, not a literal append |
| Golden screenshots for "pixel-identical" | Structural proof (same code path) + Robolectric assertions | P3; Receipt pixels go through Phase 24 owner screencaps |

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `captureToImage().toPixelMap()` works under `@GraphicsMode(NATIVE)` in this Robolectric 4.16.1 + Compose setup | Pattern 3 / Pitfall 2 | Pixel tests can't be written; fall back to structural proof (already planned) |
| A2 | Outermost modifier's `testTag` wins on a shared node, so `modifier.testTag("x")` lets a caller override | Pattern 2 | D-10 override requirement fails; flip order or move the tag — a test pins it |
| A3 | M3 1.4.0 `Button` has the same `defaultMinSize` (58dp x ~40dp) as androidx-main | Pitfall 4 | `>=` assertions still hold; only equality tests would be wrong |
| A4 | `emphasis == null && icon != null` => legacy role->widget mapping plus the icon | Pattern 1 | Behavior consumers may not expect; sketch silent — confirm or lock in plan |
| A5 | PillChip padding/gap: 12dp label-only, 8dp on the icon side, 4dp (`ContentSpacing`) icon gap | Standard Stack/PillChip | Visual mismatch vs mockup; Y-12/VCHIP-01 give no padding — caught at Phase 24 screencaps |
| A6 | `BarSegment` lives in `component/StackedProgressBar.kt` (mirrors `MetricBand`, `MediaThumbnailCell`) though sketch §0 says UI models go in `model/` | Recommended Structure | CT/SB import path differs; sketch/UI-SPEC give no package for it |
| A7 | Tonal needs `elevation = null` to avoid shadow bleed | Pitfall 5 | Minor visual |
| A8 | Robolectric default density is mdpi (1dp = 1px) | Test idioms | Pixel offsets in tests need `density` math — use `Density` conversions regardless |
| A9 | All four atoms are `Tier.PRIMITIVE` (generic, caller-supplied content, no baked convention) per `docs/DESIGN-INTENT.md` litmus | Registry | Tier test expectations; low risk |
| A10 | `ActionButtonEmphasis` is a top-level public enum in `component/DynamicActionButton.kt` next to `ActionButtonDefaults`; `model/` already imports `component.ActionButtonDefaults` | Code Examples | Import path differs for Phase 22 `SheetActionUiModel` |
| A11 | Early `return` in a Unit composable compiles cleanly under the Kotlin 2.3.20 Compose compiler | Pattern 1 | Fallback: delegate to a private composable via `if/else` placed after the legacy block as `else`-less guard |
| A12 | Disabled `RoundIconButton`/`PillChip` render border and content at 38% alpha (mirrors `PresetChip`'s `contentAlpha = 0.38f`) | Atoms | Spec silent on disabled visuals |
| A13 | Appending the new members keeps the commit lane 1 (trailing-comma/pure-add only); `HUB_LANE_OVERRIDE=2` may not be needed | Project Constraints | If the classifier says lane 2, declare it per D-03 |

## Open Questions

1. **Does `captureToImage()` work in this Robolectric harness?**
   - Known: nothing in `src/test` uses it; NATIVE mode is already used for text metrics.
   - Unclear: whether 4.16.1 + current Compose test artifacts render Surface/background pixels reliably.
   - Recommendation: Wave 0 throwaway probe (one `ToneCard` + one pixel read, one gradle invocation). Pass => add one pixel test each for `ToneCard` fill and `StackedProgressBar` track/segment; fail => keep structural proof only and log a Gate-2 item for Phase 24.
2. **Behavior of `emphasis = null` with `icon != null`** (A4) — lock in the plan; recommended mapping in Pattern 1.
3. **New `Dimens` tokens or local constants?** CONTEXT says use `Dimens` for every dimension. Reuse where the *concept* matches (`TouchTarget`, `CornerRadius.Medium`, `CompactPadding`, `HorizontalPadding`, `ContentSpacing`, `HairlineBorder`, `Icons.ChipIcon` for 18dp). For values with no matching concept (40dp round size, 20dp round icon, 32dp pill height, 6dp bar height, 20dp×8dp / 12dp / 8dp×4dp button paddings) the file's own KDoc warns against reusing same-valued tokens of a different concept (see `ChipToTitleGap`). Recommend appending small named tokens (e.g. nested objects) and extending `DimensTest`; default-param values use them. Planner's call.
4. **Segment tag** (`stacked_progress_bar_segment`) is an extra, non-Y-14 tag needed for per-segment width assertions; document it as test-only (not stable API) in Phase 24 or avoid by asserting through pixel reads once A1 passes.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | Gradle/AGP | ✓ | OpenJDK 17.0.19 | — |
| Gradle wrapper | all gates | ✓ | 9.4.1 (`distributionUrl`) | — |
| Android SDK | compile | ✓ | `local.properties` sdk.dir set | — |
| Metalava plugin / detekt | `apiDump`/`apiCheck`/`detekt` | ✓ | resolved in build | — |
| git + hooks | commits | ✓ | tag `v2.5.1` present | — |
| Other gradle daemons | build lock | ⚠ | 3 gradle processes were running at probe time | Session holds one-invocation-at-a-time rule; do not start a second concurrently |

**Missing dependencies with no fallback:** none. (No gradle command was run during research.)

## Validation Architecture

> `workflow.nyquist_validation` is `true` in `.planning/config.json` [VERIFIED].

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric 4.16.1 (`@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`) + Compose ui-test (`createComposeRule`) |
| Config file | `build.gradle.kts` `testOptions { unitTests { isIncludeAndroidResources = true } }` |
| Quick run command | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.<pkg>.<Class>" [--tests …]` (batch all filters for the task into ONE invocation) |
| Full suite command | `./gradlew apiDump testDebugUnitTest detekt apiCheck` then `bash tools/test/run-all.sh` |
| Static | `./gradlew detekt` (zero baseline) |
| API gate | `./gradlew apiDump` (commit `api.txt` in the same commit) then `./gradlew apiCheck` |
| Lane check | `bash tools/classify-hub-change.sh --baseline v2.5.1` on the staged diff before committing |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command (class filter) | File Exists? |
|--------|----------|-----------|----------------------------------|-------------|
| VTONE-01 | `error()`/`warn()` resolve from active scheme (light + dark); `ToneColors` value equality/hashCode; contrast >= 4.5; `ContentTone` constants | Robolectric unit | `--tests "…theme.ToneTokensTest"` | ❌ Wave 0 |
| VCARD-01 | `ToneCard` 12dp corners (source/Shape), 12dp×16dp padding, 4dp child gap (bounds), `LocalContentColor == tone.content`, tag `tone_card`, tag override | Robolectric (+ optional NATIVE pixel) | `--tests "…component.ToneCardTest"` | ❌ Wave 0 |
| VBTN-01 | Each emphasis renders its widget in role hue; icon shown (width delta); TextSmall touch >= 48dp; `enabled=false` never fires; legacy path unchanged | Robolectric + source-contract + pure palette unit | `--tests "…component.DynamicActionButtonEmphasisTest" --tests "…component.DynamicActionButtonLegacyPathSourceContractTest" --tests "…component.DynamicActionButtonTest"` | new ❌ / existing ✅ (must pass unedited) |
| VBTN-02 | `RoundIconButton` touch bounds >= 48dp, content description, disabled never fires, tag `round_icon_button` + override, size param | Robolectric | `--tests "…component.RoundIconButtonTest"` | ❌ Wave 0 |
| VBAR-01 | `clampSegmentFractions` (sum>1 clamps, negative->0, NaN->0, empty); 0.4+0.18 => 58% width; per-segment widths; default height 6dp; tag `stacked_progress_bar` | JVM unit + Robolectric | `--tests "…component.StackedProgressBarTest"` | ❌ Wave 0 |
| VCHIP-01 | `PillChip` 32dp drawn, touch >= 48dp, single line, icons 18dp, label/contentDescription exposed, disabled never fires, tag `pill_chip`; `AppChip`/`PresetChip` tests unchanged | Robolectric | `--tests "…component.PillChipTest" --tests "…component.AppChipTest" --tests "…component.PresetChipTest"` | ❌ new / ✅ existing |
| Long German label (~360dp) | `DynamicActionButton` (all emphases) stays in-bounds, not clipped; `PillChip` ellipsizes within bounds | Robolectric NATIVE `w360dp-h800dp` | `--tests "…component.ToneAtomsLongLabelTest"` | ❌ Wave 0 |
| Registry / drift | 4 entries registered; states matrix, playground, detail resolution, domain-vocabulary guards green | JVM | `--tests "io.github.ygaray.yahirandroidtaste.explorer.*"` | ✅ existing (edit `DomainVocabularyDriftGuardTest` allowlist) |
| API | `api.txt` regenerated, `apiCheck` green, `GeneratedSymbolDriftGuardTest` green | Gradle | `./gradlew apiDump apiCheck` | ✅ |
| Dimens (if tokens added) | New token values + old values unchanged | JVM | `--tests "…theme.DimensTest"` | ✅ extend |

### Sampling Rate
- **Per task commit:** one gradle invocation: `apiDump` (only if public API changed) + `testDebugUnitTest` with that task's `--tests` filters **plus** `io.github.ygaray.yahirandroidtaste.explorer.*` (cheap, catches Pitfalls 1 and 3) + `bash tools/classify-hub-change.sh --baseline v2.5.1` after staging.
- **Per wave merge:** `./gradlew apiDump testDebugUnitTest detekt apiCheck` (full suite) and `bash tools/test/run-all.sh`.
- **Phase gate:** full suite green; `git diff v2.5.1 -- api.txt` reviewed (only the `DynamicActionButton` signature line rewritten, everything else additions); existing `DynamicActionButtonTest`, `AppChipTest`, `PresetChipTest` pass unedited; no `testTagsAsResourceId` in `src/main` (`grep -rn testTagsAsResourceId src/main` empty).

### Wave 0 Gaps
- [ ] `theme/ToneTokensTest.kt` — VTONE-01 (+ contrast helper)
- [ ] `component/ToneCardTest.kt`, `RoundIconButtonTest.kt`, `StackedProgressBarTest.kt`, `PillChipTest.kt`
- [ ] `component/DynamicActionButtonEmphasisTest.kt` + `DynamicActionButtonLegacyPathSourceContractTest.kt` (pin the legacy `when` text via `SourceContractTestSupport.source("DynamicActionButton.kt")` + `stripComments`, and the guard `if (emphasis != null || icon != null)` appearing before it)
- [ ] `component/ToneAtomsLongLabelTest.kt` (NATIVE, `w360dp-h800dp`, long German strings, e.g. "Änderungen speichern und fortfahren")
- [ ] `explorer/DomainVocabularyDriftGuardTest.kt` — append `"Round"`, `"Stacked"`, `"Tone"`, `"Pill"` to `PRIMITIVE_NOUN_ALLOWLIST`
- [ ] Optional probe: one throwaway NATIVE `captureToImage` test (Open Question 1)
- [ ] Framework install: none

## Security Domain

> `security_enforcement` is `true`, ASVS level 1 [VERIFIED: .planning/config.json].

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | — (no auth surface) |
| V3 Session Management | no | — |
| V4 Access Control | no | — |
| V5 Input Validation | limited | Clamp caller-supplied floats in `StackedProgressBar` (NaN/negative/>1 never throw or overflow layout) |
| V6 Cryptography | no | — |
| V14 Config / build | yes | Never set `testTagsAsResourceId`; no secrets; no network; one-way dependency (INV-01/INV-03) |

### Known Threat Patterns for this stack
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Test tags exposed as resource ids in release builds (info disclosure of UI structure) | Information disclosure | Library never sets `testTagsAsResourceId`; consumers opt in. Add a grep assertion in the phase gate |
| Crash on hostile numeric input (`NaN`, `Inf`, negative weights -> `IllegalArgumentException`) | Denial of service | `clampSegmentFractions` + skip zero-weight children |
| Caller `contentDescription`/label leaking sensitive text | Information disclosure | Atoms only render caller strings; KDoc must state "static UI copy, never sensitive text" (precedent: `semanticsPrefix` note in API.md) |

## Sources

### Primary (HIGH confidence — read this session)
- `.planning/phases/20-tone-foundation-atoms/20-CONTEXT.md`, `.planning/seams/v2.6.0-SKETCH.md` (§0, §2 Y-1/8/9/10/12/14, §3), `.planning/ROADMAP.md` (Phase 20), `.planning/REQUIREMENTS.md`, `.planning/STATE.md`
- CT `76-UI-SPEC.md` "YAT Evolution Asks" (Y-1, Y-8..Y-10, Y-12, Y-14) and Spacing/Typography sections
- `CLAUDE.md`, `API.md` (Compatibility rule), `tools/README-api-guard.md`, `tools/classify-hub-change.sh`, `tools/verify-additive-diff.sh`, `tools/hooks/pre-commit`
- Source: `component/DynamicActionButton.kt`, `AppChip.kt`, `PresetChip.kt`, `MetricBar.kt`, `theme/Dimens.kt`, `theme/Theme.kt`, `theme/Color.kt`, `explorer/ComponentRegistry.kt`, `explorer/*FamilyScreen.kt` (Buttons/Progress/Chips), `explorer/ExplorerIndexScreen.kt`, `api.txt`
- Tests: `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `ComponentStatesMatrixTest`, `ComponentPlaygroundIntegrityTest`, `GeneratedSymbolDriftGuardTest`, `ApproachLadderCardGalleryDemoTest`, `ApproachLadderCardLongLabelTest`, `ApproachLadderCardTest` (touch bounds), `PresetChipTest`, `DimensTest`, `DynamicActionButtonTest`, `AppChipTest`, `ProgressRingTest`, `CardTagRowTest`, `FailureRoleSourceContractTest`, `SourceContractTestSupport`
- Config: `build.gradle.kts`, `gradle/libs.versions.toml`, `config/detekt/detekt.yml`, `config/detekt-compose.yml`

### Secondary (MEDIUM confidence)
- androidx-main `Button.kt` and `LayoutNode.kt`/`SemanticsConfiguration.kt` via WebFetch (raw.githubusercontent.com/androidx/androidx): Button signature, `defaultMinSize`, semantics collapse order. Branch is `androidx-main`, not the 1.4.0 tag.

### Tertiary (LOW confidence)
- WebSearch on Robolectric NATIVE + Compose screenshots: confirms NATIVE is the enabling mode (Roborazzi docs) but not plain `captureToImage()` — hence A1/probe.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new dependencies; versions read from the catalog.
- Architecture: HIGH for gates/registry/lane mechanics (read the sources); MEDIUM for M3/Compose internals (A2, A3, A11).
- Pitfalls: HIGH for Pitfalls 1, 3, 7, 9 (verified in repo); MEDIUM for 4, 5, 6.

**Research date:** 2026-10-12
**Valid until:** 2026-11-11 (stable toolchain; re-check if Compose BOM or M3 is bumped)
