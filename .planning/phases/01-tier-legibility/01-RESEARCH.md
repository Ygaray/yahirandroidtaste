# Phase 1: Tier Legibility - Research

**Researched:** 2026-09-01
**Domain:** Kotlin/Jetpack Compose library metadata modeling + Metalava API-surface governance
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01 [tier-field]:** required (no default) `tier` on `ComponentRegistry.Entry`, placed last — the Metalava/api.txt rewrite cost is paid either way, so defaulting only surrenders the compile-time "every entry is tiered" guarantee and risks LEG-01 going technically-green but semantically empty. The enum-on-Entry representation is settled; a parallel `Map<name,Tier>` is forbidden by the "entries alone is authoritative" invariant. — **Reversibility:** costly — the `tier` field lands in the published `api.txt`; removing/redefaulting it later is an API break requiring a Metalava rebaseline. _(source: ai-auto)_
- **D-02 [gallery-display]:** badge on **both** the list row (`ComponentRow` in `ExplorerIndexScreen`) and the `ComponentDetailScreen` header, reusing Material3 `Badge`/`AssistChip` rather than a new primitive — one `ComponentRow` change covers search + all per-family lists; single-surface (detail only) fails "a developer browsing the catalog can see tiering without reading source." _(source: human)_
- **D-03 [litmus]:** anchor the per-tier litmus in `docs/DESIGN-INTENT.md` to the existing one-way-dependency / no-domain-assumption invariant — **primitive** = zero domain nouns in name+params, renders only caller-passed content; **pattern** = encodes an opinion/composition/interaction convention — a decidable test. An adjective-based ("simpler" vs "more opinionated") litmus lets the same component be tiered two ways, making Phase 2's altitude-mismatch findings un-defensible and Phase 3's tier-aware gate unwritable. Borderline components (`CardBase`, `ChipBar`, `HeatSwatch`) are tiered by applying this decidable test. _(source: ai-auto)_

### Claude's Discretion

None explicitly separated in CONTEXT.md beyond the three locked decisions above — badge component choice (`Badge` vs `AssistChip`) and the `Tier` enum's declaration site (nested vs top-level) are implementation details within D-01/D-02's boundaries; see this doc's Standard Stack / Alternatives Considered for reasoned recommendations.

### Deferred Ideas (OUT OF SCOPE)

None — discussion stayed within phase scope. (Verbatim from `01-CONTEXT.md`'s `<deferred>` block.)

### Canonical References (from CONTEXT.md)

- `.planning/v1.0-DECISION-MAP.md` §Phase 1 — source of the three decisions above.
- `.planning/ROADMAP.md` §"Phase 1: Tier Legibility" — goal + 4 success criteria.
- `.planning/REQUIREMENTS.md` — LEG-01, LEG-02.
- `CLAUDE.md` (repo root) §"The invariants" — one-way dependency / no-domain-assumption, and the `ComponentRegistry` single-source-of-truth + drift-guard rule the tier field must not break.
- `API.md` — the public surface the new `tier` field is added to (distinct from the new `docs/DESIGN-INTENT.md`).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| LEG-01 | Every component registered in `ComponentRegistry` carries an explicit **tier** label (`primitive` \| `pattern`), queryable in the registry and shown in the `ExplorerActivity` gallery. | `ComponentRegistry.Entry` current shape, all 53 call sites, and the `Badge`/`ComponentRow`/`ComponentDetailScreen` wiring are documented in Architecture Patterns and Code Examples; the `apiDump`/`apiCheck` gate this change triggers is covered in Pitfall 2. |
| LEG-02 | A design-**intent** doc (distinct from the registry-of-what-exists) states what the hub is *per tier* — the primitives contract and the patterns contract — and the litmus each tier must pass. | `docs/` confirmed not to exist yet (net-new file/dir); the decidable D-03 litmus is restated and pre-applied to the three named borderline components (`CardBase`, `ChipBar`, `HeatSwatch`) in Code Examples, ready for the planner/design-intent doc to ratify or adjust. |
</phase_requirements>

## Summary

This phase adds one required enum field to an existing, well-understood data structure
(`ComponentRegistry.Entry`), threads it through two already-shared gallery row/header composables,
and writes one new prose doc. There is no new library dependency, no new architecture, and no
ambiguity about *where* the mechanical work lands — every touch point was located and read this
session. The only real risk is mechanical: `Entry`'s constructor is called **53 times** across
9 family-screen files with 100% named-argument style, and D-01 makes the new `tier` field
**required with no default**, so all 53 call sites must gain a `tier = ...` argument in the same
change (a single-file signature edit does not compile the library until every call site is
updated). This is a big-surface-area, low-difficulty task — best split by family-screen file
across parallel plan tasks — not a design problem.

The Metalava `api.txt` gate (`apiCheck`) will fail the moment `Entry`'s constructor signature
changes; the fix is `./gradlew apiDump` to regenerate the committed `api.txt`, which must be
committed alongside the code change. `docs/DESIGN-INTENT.md` does not exist yet — it is a wholly
new file, and `docs/` is a wholly new directory (verified: `find` found neither).

**Primary recommendation:** Add `enum class Tier { PRIMITIVE, PATTERN }` nested inside
`ComponentRegistry` (mirroring the existing nested `StateCell` pattern, and staying visually
distinct from the unrelated `HeatTier`/`RelatednessTier` enums already in the `component` package),
add `val tier: Tier` as the **last** constructor parameter on `Entry` (required, no default), update
all 53 call sites across the 9 `*FamilyScreen.kt` files, add a `Badge { Text(tier label) }` (or
`AssistChip`) to both `ComponentRow` (`ExplorerIndexScreen.kt`) and the `ComponentDetailScreen`
header, run `./gradlew apiDump` to rebaseline `api.txt`, and write `docs/DESIGN-INTENT.md` with the
decidable litmus D-03 already specifies.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Tier data modeling (`Entry.tier`) | Library / Data model (`explorer/ComponentRegistry.kt`) | — | Single source of truth per the D-05 "entries alone is authoritative" invariant; no parallel map. |
| Tier authoring (53 call sites) | Library / Data model (`explorer/*FamilyScreen.kt`) | — | Each family screen owns its own entries per the existing D-05 file-split convention. |
| Gallery list-row tier display | Presentation / Compose UI (`ExplorerIndexScreen.kt` `ComponentRow`) | — | One shared composable already used by both the index search results and (from Plan 03-05 precedent) reused directly by all 9 family screens' own row lists — one edit covers every surface. |
| Gallery detail-header tier display | Presentation / Compose UI (`ComponentDetailScreen.kt`) | — | Single detail-page composable resolved per `Entry`, already renders `entry.name` in its `TopAppBar` title — the badge sits beside it. |
| API-surface governance | Build tooling (Metalava `api.txt` via `build.gradle.kts`) | — | Any public constructor-signature change to `Entry` is a binary/source API change; `apiDump`/`apiCheck` is the existing, non-negotiable gate — not something this phase introduces. |
| Design-intent litmus | Documentation (`docs/DESIGN-INTENT.md`, new) | — | LEG-02 explicitly requires a doc distinct from the registry-of-what-exists (`ComponentRegistry.kt`/`API.md`) — prose intent, not code. |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin | 2.3.20 | `enum class Tier` on `Entry` | Already the module's language toolchain — no new dependency. `[VERIFIED: build.gradle.kts and CLAUDE.md toolchain section]` |
| Jetpack Compose Material3 | BOM `2026.04.01` | `Badge`/`AssistChip` for the tier chip | Already on the classpath (`gradle/libs.versions.toml:12` `composeBom = "2026.04.01"`) — no new dependency. `[VERIFIED: gradle/libs.versions.toml:12]` Note: this differs from the `2026.02.01` figure in root `CLAUDE.md`'s toolchain section — the lockfile is the source of truth for the actual resolved version; flag this doc drift, do not "fix" it as part of this phase. |
| Metalava (AGP built-in plugin) | via `alias(libs.plugins.metalava)` | `apiDump`/`apiCheck` custom Gradle tasks | Already wired (`build.gradle.kts:7,16-29`) as the chosen API-signature-diffing mechanism after a documented spike rejected two alternatives (`tools/README-api-guard.md`, referenced in `build.gradle.kts` comment). `[VERIFIED: build.gradle.kts:7,16-29]` |

### Supporting
No new supporting libraries — this phase is additive metadata + one new prose doc, no runtime
dependency change.

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Nested `ComponentRegistry.Tier` enum | Top-level `Tier` enum in `explorer` package | Nested reads as "this enum only makes sense in the context of a registry Entry" and avoids a bare `Tier` symbol sitting next to the unrelated `component.HeatTier`/`component.RelatednessTier` enums (`[VERIFIED: api.txt:173,279]` both already public, unrelated concepts). Either compiles; nested is the more legible choice given the existing `StateCell` nesting precedent on the same class. |
| `Badge` (Material3) | `AssistChip` (Material3) | D-02 allows either. `Badge` is visually smaller/lighter (a true "badge," matches "tier at a glance" framing); `AssistChip` is a full tappable chip shape, heavier weight for a purely informational label. Recommend `Badge` for the list row (space-constrained `ListItem` trailing/leading slot) and either for the detail header. Both already on the classpath — zero cost either way. |
| Required `tier` (no default) | Defaulted `tier = Tier.PATTERN` (or similar) | D-01 already locked "required, no default" — documented here only so the planner does not second-guess it: a default silently mistiers all 53 entries green until someone manually audits, defeating LEG-01's "queryable in code" guarantee. |

**Installation:** None — no new dependency declarations needed in `build.gradle.kts` or
`gradle/libs.versions.toml`. `Badge`/`AssistChip`/enum support are already available.

**Version verification:** Not applicable — no new package to verify against a registry (this is a
Kotlin-language + already-resolved-Compose-BOM change, not an external package addition).

## Package Legitimacy Audit

**Not applicable.** This phase installs no external packages (no new Gradle dependency, no npm/pip
package). `Badge`, `AssistChip`, and `enum class` are already-resolved parts of the existing
Kotlin/Compose toolchain. The Package Legitimacy Gate is skipped per its own trigger condition
("whenever this phase installs external packages").

## Architecture Patterns

### System Architecture Diagram

```
ComponentRegistry.kt (explorer/)
  │
  │  data class Entry(name, family, states, content, controls, preview, tier: Tier)  ◄── NEW required field, placed last
  │  enum class Tier { PRIMITIVE, PATTERN }                                          ◄── NEW nested enum
  │
  ├─► cardsFamilyEntries (CardsFamilyScreen.kt)              11 × Entry(...) call sites, each gains `tier = Tier.X`
  ├─► chipsFamilyEntries (ChipsFamilyScreen.kt)                5 × …
  ├─► sheetsFamilyEntries (SheetsFamilyScreen.kt)             18 × …
  ├─► buttonsFabFamilyEntries (ButtonsFabFamilyScreen.kt)      3 × …
  ├─► pickersFamilyEntries (PickersFamilyScreen.kt)            4 × …
  ├─► feedbackFamilyEntries (FeedbackFamilyScreen.kt)          3 × …
  ├─► emptyStateFamilyEntries (EmptyStateFamilyScreen.kt)      1 × …
  ├─► progressFamilyEntries (ProgressFamilyScreen.kt)          4 × …
  └─► tactileFoundationFamilyEntries (TactileFoundationFamilyScreen.kt) 4 × …
         = 53 total Entry(...) call sites, ALL must add `tier = ...` in the same change
                                │
                                ▼
                 entries: List<Entry>  (concatenation, unchanged shape)
                                │
                 ┌──────────────┴───────────────┐
                 ▼                               ▼
   ExplorerIndexScreen.kt                ComponentDetailScreen.kt
   `ComponentRow(name, supportingLabel,   `ComponentDetailScreen(entry, …)`
    onClick)` — reused by index search     TopAppBar title = entry.name
    results AND (per family-screen         ◄── ADD tier Badge/AssistChip beside/below title
    convention) each family's own row      ◄── ADD tier param to ComponentRow, render
    list                                       Badge/AssistChip in leading/trailing slot
                 │
                 ▼
   Developer browsing gallery sees tier on every list row AND every detail header
   (LEG-01 success criterion 2 satisfied on both surfaces per D-02)

Parallel, independent output:
   docs/DESIGN-INTENT.md (NEW FILE, NEW DIRECTORY — neither exists yet)
     — states the primitives contract + patterns contract
     — states the decidable litmus (D-03): domain-noun-free name+params +
       "renders only caller-passed content" = primitive; encodes an opinion/
       composition/interaction convention = pattern
     — applies the litmus to the 3 named borderline components (CardBase, ChipBar, HeatSwatch)

Gate that must go green after the Entry signature change:
   ./gradlew apiCheck  (Metalava) — WILL FAIL until `./gradlew apiDump` regenerates api.txt
   ./gradlew testDebugUnitTest — ComponentRegistryDriftGuardTest, ComponentRegistrySearchTest,
     ComponentDetailResolutionTest, ComponentPlaygroundIntegrityTest, GalleryDemoInteractionTest
     (none of these construct a raw `Entry(...)`; only read `ComponentRegistry.entries` — should
     stay green through the signature change with no test-code edits, see Pitfall 3)
   ./gradlew detekt — LongParameterList constructorThreshold=6 with ignoreDefaultParameters=true;
     Entry currently has 2 non-defaulted params (name, family); adding tier as a 3rd non-defaulted
     param stays well under threshold — will NOT trip (see Pitfall 4)
```

### Recommended Project Structure
No new packages/directories inside `src/`. One new top-level directory:
```
docs/
└── DESIGN-INTENT.md    # NEW — primitives contract, patterns contract, the litmus
```

### Pattern 1: Required-last-field enum addition to a multi-call-site data class
**What:** Add a new required (no-default) field as the **last** constructor parameter of an
existing `data class`, then update every call site with a named argument.
**When to use:** When compile-time "every instance is populated" is a hard requirement (D-01's
explicit rationale — LEG-01 says "queryable in code," not "queryable in code for entries someone
remembered to tier").
**Example — current `Entry` shape (exact, read this session):**
```kotlin
// Source: src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt:60-67
data class Entry(
    val name: String,
    val family: String,
    val states: List<StateCell> = emptyList(),
    val content: (@Composable () -> Unit)? = null,
    val controls: List<Control> = emptyList(),
    val preview: (@Composable (PlaygroundState) -> Unit)? = null
)
```
**Target shape (tier appended last per D-01):**
```kotlin
data class Entry(
    val name: String,
    val family: String,
    val states: List<StateCell> = emptyList(),
    val content: (@Composable () -> Unit)? = null,
    val controls: List<Control> = emptyList(),
    val preview: (@Composable (PlaygroundState) -> Unit)? = null,
    val tier: Tier
)
```
**One real call site that must gain `tier = ...` (exact, read this session):**
```kotlin
// Source: src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt:84-101
ComponentRegistry.Entry(
    name = "CardBase",
    family = ExplorerFamilies.CARDS,
    states = listOf(
        ComponentRegistry.StateCell("Default") { CardBasePreview(tactileDepth = true) },
        ComponentRegistry.StateCell("Pressed / Selected"),
        ComponentRegistry.StateCell("Disabled"),
        ComponentRegistry.StateCell("Focused")
    ),
    content = { CardBaseVariants() },
    controls = listOf(cardBaseTactileDepthControl),
    preview = { state ->
        CardBasePreview(tactileDepth = state.boolean(cardBaseTactileDepthControl))
    }
    // tier = ComponentRegistry.Tier.PATTERN  ← must be added
)
```

### Pattern 2: Shared row/detail composable extension (D-02 badge placement)
**What:** `ComponentRow` is a single non-private composable reused by the index screen's search
results and (per its own KDoc) by all 9 family screens' own row lists. `ComponentDetailScreen` is a
single composable resolved per `Entry`. Adding one parameter/render call to each of these two
functions covers every surface at once — no per-family-screen UI edits needed.
**When to use:** Exactly this situation — D-02 requires the badge on "both the list row … and the
`ComponentDetailScreen` header," and both are single shared composables.
**Example — current `ComponentRow` (exact, read this session):**
```kotlin
// Source: src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerIndexScreen.kt:259-281
@Composable
fun ComponentRow(name: String, supportingLabel: String? = null, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(name) },
        supportingContent = supportingLabel?.let {
            { Text(it, style = MaterialTheme.typography.bodyMedium,
                   color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                 tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
```
Note `ComponentRow` currently has NO `tier`/`Entry` parameter at all — callers pass `name` and
`supportingLabel` as separate strings, not the `Entry` itself. Adding a `tier: ComponentRegistry.Tier`
parameter here is itself a public-API change requiring an `api.txt` rebaseline (confirmed:
`ComponentRow` is a public top-level `@Composable`, not `internal`/`private`; no `[VERIFIED: api.txt line]`
entry currently exists for it in the ~700-line file scanned, but its public visibility is
`[VERIFIED: ExplorerIndexScreen.kt:259 "fun ComponentRow(" with no `internal`/`private` modifier]`).
**Badge render — Material3 API confirmed via search this session:**
```kotlin
// Signature source: developer.android.com Compose Material3 Badge — [CITED: developer.android.com/jetpack/androidx/releases/compose-material3]
@Composable
fun Badge(
    modifier: Modifier = Modifier,
    containerColor: Color = BadgeDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable (RowScope.() -> Unit)? = null,
)
// Usage: Badge { Text(if (tier == ComponentRegistry.Tier.PRIMITIVE) "Primitive" else "Pattern") }
```

### Anti-Patterns to Avoid
- **A parallel `Map<String, Tier>` keyed by entry name:** Explicitly forbidden by D-01 and the
  existing D-05 "entries alone is authoritative" invariant (`ComponentRegistry.kt:9-10`
  `[VERIFIED: ComponentRegistry.kt:9-10]` "a parallel `Map<name,Tier>` is forbidden by the 'entries
  alone is authoritative' invariant" — quoted verbatim from `01-CONTEXT.md:19`).
- **Defaulting `tier`:** Rejected by D-01 for the reason stated above (silent mistiering).
- **Adjective-based litmus wording** ("simpler" vs "more opinionated") in `docs/DESIGN-INTENT.md`:
  explicitly rejected by D-03 — "lets the same component be tiered two ways" (quoted from
  `01-CONTEXT.md:25`). Use the decidable domain-noun/caller-content-only test instead.
- **A brand-new chip/badge primitive component:** D-02 and the phase's `<specifics>` block both
  require reusing Material3 `Badge`/`AssistChip` — do not add a new `TierBadge` component to
  `component/` (that would itself need registering in `ComponentRegistry`, is out of scope, and
  duplicates existing chip primitives `AppChip`/`CardTypeChip`).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Tier badge visual | A new `component/TierBadge.kt` composable | Material3 `Badge` or `AssistChip` (already on classpath) | D-02 + phase `<specifics>` explicitly require reuse; a new primitive would itself need `ComponentRegistry` registration (circular — the badge showing tier would need its own tier), violating scope. |
| API-signature diffing | A hand-rolled `.txt` diff script or CI check | The already-wired Metalava `apiDump`/`apiCheck` Gradle tasks (`build.gradle.kts:16-29`) | This module already evaluated and rejected two alternative mechanisms (Kotlin built-in abiValidation, classic binary-compatibility-validator) per the `build.gradle.kts:10-13` comment and `tools/README-api-guard.md` — do not reopen that decision. |

**Key insight:** Everything this phase needs already exists in the codebase or classpath. The work
is entirely mechanical application across 53 call sites plus two shared-composable edits plus one
new prose file — not net-new architecture.

## Common Pitfalls

### Pitfall 1: Forgetting a call site in the 53-site sweep breaks the build loudly (good) but wastes a cycle if done manually
**What goes wrong:** A required (no-default) constructor parameter means the Kotlin compiler
rejects any `Entry(...)` call site missing `tier = ...` — but with 53 call sites spread across 9
files, a manual sweep risks missing one on a first pass, discovered only at `./gradlew build` /
`testDebugUnitTest` time.
**Why it happens:** No `@Deprecated`/migration tooling is wired for this; it's a plain Kotlin
compile error per site.
**How to avoid:** Task the sweep per-family-screen-file (matches the existing D-05 file-split
convention — `cardsFamilyEntries` in `CardsFamilyScreen.kt`, etc.), and treat "does
`./gradlew build` succeed" as the mechanical completeness check rather than a manual `grep` count,
though `grep -c "ComponentRegistry.Entry(" <file>` against the counts in this doc (11/5/18/3/4/3/1/4/4)
is a fast sanity check per file before compiling.
**Warning signs:** A compile error citing "No value passed for parameter 'tier'" at a specific file/line.

### Pitfall 2: `api.txt` rebaseline is a two-step, not one-step, gate
**What goes wrong:** Running `./gradlew apiCheck` immediately after editing `Entry`'s signature
will fail (expected — the committed `api.txt` no longer matches). A common mistake is treating this
failure as a bug rather than the expected first half of the workflow.
**Why it happens:** `apiCheck` diffs the *current* generated signature against the *committed*
`api.txt:390-398` `Entry` constructor entry (`[VERIFIED: api.txt:391]` current signature: `ctor
public ComponentRegistry.Entry(String name, String family, optional
java.util.List<io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistry.StateCell> states,
optional kotlin.jvm.functions.Function0<kotlin.Unit>? content, optional
java.util.List<? extends io.github.ygaray.yahirandroidtaste.explorer.Control> controls, optional
kotlin.jvm.functions.Function1<? super io.github.ygaray.yahirandroidtaste.explorer.PlaygroundState,kotlin.Unit>? preview);` —
note ZERO trailing `optional` for a would-be `tier` param; adding it as non-optional last changes
this exact line) — any signature change fails until the baseline is regenerated.
**How to avoid:** After the code change compiles, run `./gradlew apiDump` to regenerate `api.txt`,
review the diff (should show exactly the new `Entry` ctor line, the new `Tier` enum's
`enum_constant` entries, and — if `ComponentRow`/`ComponentDetailScreen` gain new public
parameters — their new signature lines too), then commit the regenerated `api.txt` alongside the
code in the same commit. Then `./gradlew apiCheck` passes.
**Warning signs:** `apiCheck` failure listing `Entry`'s constructor as changed/missing.

### Pitfall 3: The registry integrity / drift-guard tests are name-based, not tier-based — they will NOT catch a missing/wrong tier
**What goes wrong:** Assuming `ComponentRegistryDriftGuardTest` (`[VERIFIED:
ComponentRegistryDriftGuardTest.kt:47-108]`) or `ComponentRegistrySearchTest` will fail if a `tier`
is wrong or forgotten. They will not — the drift guard scans for public-composable-name coverage
only (`entryNames`, never touches `.tier`); the search test asserts filter/ordering behavior only.
**Why it happens:** These tests predate this phase and were not written with `tier` in mind; LEG-01
requires the field exist and be queryable, not that a new automated correctness check exists for it
(that's explicitly deferred to v2 as **GOV-04**, per `REQUIREMENTS.md:53` `[VERIFIED:
REQUIREMENTS.md:53]` "Automate tier-labeling enforcement in the drift-guard test (fail the build if
a new public composable ships without a tier)").
**How to avoid:** Do not assume existing green tests validate tier correctness. If the planner
wants a compile-time-adjacent safety net beyond "the constructor call compiles," that is out of
this phase's scope (GOV-04, v2) — flag rather than silently add.
**Warning signs:** None automated in this phase — tier *values* (which of PRIMITIVE/PATTERN each
of the 53 components gets) are a judgment call per D-03's litmus, not something a test currently
enforces.

### Pitfall 4: LongParameterList detekt rule — verified safe, but do not add unrelated required params in the same change
**What goes wrong:** A future contributor might assume adding `tier` "uses up headroom" against
detekt's `LongParameterList` rule and hesitate, or conversely stack another required param onto the
same `Entry.copy` diff without checking the budget.
**Why it happens:** `Entry`'s constructor currently has 6 total parameters, 4 of them defaulted.
**How to avoid:** `[VERIFIED: config/detekt/detekt.yml:30-34]` quoted verbatim: `LongParameterList:
active: true / functionThreshold: 6 / constructorThreshold: 6 / ignoreDefaultParameters: true`.
With `ignoreDefaultParameters: true`, only non-defaulted params count: today that's 2 (`name`,
`family`); after adding required `tier`, it's 3 — still well under the `constructorThreshold: 6`
ceiling. No detekt suppression or config change is needed for this phase alone.
**Warning signs:** N/A — confirmed non-issue this session; noted so the planner doesn't
over-engineer a detekt workaround.

### Pitfall 5: `ComponentRow`'s current signature has no `Entry`/tier hook — this is itself a public API change
**What goes wrong:** Treating the `ComponentRow` badge addition as "just render logic" and
forgetting it also needs an `api.txt` rebaseline pass (folds into Pitfall 2's single `apiDump`, but
easy to under-scope if planned as a separate, later task from the `Entry` field change).
**Why it happens:** `ComponentRow(name: String, supportingLabel: String? = null, onClick: () -> Unit)`
`[VERIFIED: ExplorerIndexScreen.kt:260]` takes primitive `String` params, not an `Entry` — so a
`tier` parameter must be threaded through explicitly (either `tier: ComponentRegistry.Tier` as a
new param, or change the call sites to pass `entry.tier`). Every call site of `ComponentRow` (index
search results in `ExplorerIndexScreen.kt:161-166`, plus each family screen's own row list per the
KDoc at `ExplorerIndexScreen.kt:252-258`) needs updating too — a second, smaller sweep alongside
the 53-site `Entry` sweep.
**How to avoid:** Plan `ComponentRow`'s signature change and its call-site sweep as one task
alongside (not separate from) the `Entry.tier` field addition, and roll both into the single
`apiDump` pass.
**Warning signs:** `apiCheck` diff showing `ComponentRow`'s signature changed but the commit
message/task only mentions `Entry`.

## Code Examples

### Applying the D-03 litmus to the three named borderline components
D-03 names `CardBase`, `ChipBar`, `HeatSwatch` as borderline and requires they be "tiered by
applying this decidable test" (primitive = zero domain nouns in name+params, renders only
caller-passed content; pattern = encodes an opinion/composition/interaction convention). This is
worked reasoning to seed the planner/design-intent doc — not a locked decision; the discuss-phase
or planner should ratify it:

- **`ChipBar`** — `[VERIFIED: ChipBar.kt:41-49]` signature `fun <T> ChipBar(items: List<T>, key:
  (T) -> Any, itemContent: @Composable (T) -> Unit, modifier: Modifier = Modifier, testTag: String
  = "chip_bar", leadingContent: (@Composable () -> Unit)? = null, trailingContent: (@Composable ()
  -> Unit)? = null)`. Zero domain nouns (fully generic `<T>`), and its own KDoc states verbatim
  `[VERIFIED: ChipBar.kt:19-20]` "`ChipBar` holds no chip-rendering opinions and imports no `:app`
  type — it is pure presentation." → reads as **primitive** under the litmus.
- **`HeatSwatch`** — `[VERIFIED: HeatSwatch.kt:37-42]` signature `fun HeatSwatch(modifier: Modifier
  = Modifier)`. Takes no caller-supplied content at all; its body hardcodes its own sample data
  (`[VERIFIED: HeatSwatch.kt:39]` `val samples = listOf(0.04f, 0.12f, 0.24f, 0.37f, 0.55f, 0.80f)`)
  and its own KDoc calls it a "Showcase composable for [heatTier] / [heatVisual] / [hubNodeVisual]"
  targeting "mindmap nodes/edges" `[VERIFIED: HeatSwatch.kt:24,32]` — it encodes a specific visual
  convention (the Heat relatedness ramp), fails "renders only caller-passed content" → reads as
  **pattern** under the litmus.
- **`CardBase`** — encodes the reveal-confirm destructive-swipe interaction convention (left=delete,
  right=edit) that root `CLAUDE.md`'s invariants section names as a library-wide convention "realized
  in these components" — an opinionated interaction convention, not caller-supplied content →
  reads as **pattern** under the litmus. (Full `CardBase` signature not re-quoted here; its
  KDoc/behavior is already summarized in `01-CONTEXT.md`'s own `<specifics>` framing and confirmed
  present at `component/CardBase.kt`.)

### Current 9-family concatenation (unchanged shape, for the planner's task-splitting reference)
```kotlin
// Source: src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt:81-89
val entries: List<Entry> = cardsFamilyEntries +
    chipsFamilyEntries +
    sheetsFamilyEntries +
    buttonsFabFamilyEntries +
    pickersFamilyEntries +
    feedbackFamilyEntries +
    emptyStateFamilyEntries +
    progressFamilyEntries +
    tactileFoundationFamilyEntries
```
Per-file `Entry(...)` call-site counts (exact, counted this session via `grep -c
'ComponentRegistry.Entry(' <file>`, sums to 53):

| File | Entry(...) call sites |
|------|----|
| `CardsFamilyScreen.kt` | 11 |
| `ChipsFamilyScreen.kt` | 5 |
| `SheetsFamilyScreen.kt` | 18 |
| `ButtonsFabFamilyScreen.kt` | 3 |
| `PickersFamilyScreen.kt` | 4 |
| `FeedbackFamilyScreen.kt` | 3 |
| `EmptyStateFamilyScreen.kt` | 1 |
| `ProgressFamilyScreen.kt` | 4 |
| `TactileFoundationFamilyScreen.kt` | 4 |
| **Total** | **53** |

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|---------------|--------|
| Two-tier structure is implicit tribal knowledge (no field, no doc) | Explicit `tier: Tier` field + `docs/DESIGN-INTENT.md` | This phase | LEG-01/LEG-02 satisfied; unblocks Phase 2's "altitude mismatch" as a nameable, code-queryable finding. |

**Deprecated/outdated:** Nothing in this phase deprecates prior code — purely additive to `Entry`,
`ComponentRow`, and `ComponentDetailScreen`.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Recommendation to nest `Tier` inside `ComponentRegistry` (vs. top-level in `explorer`) | Standard Stack / Alternatives Considered | Low — purely a naming/organization choice, either compiles and satisfies LEG-01; a top-level `Tier` would just sit next to unrelated `HeatTier`/`RelatednessTier` and be slightly less legible. Not a locked decision — CONTEXT.md settled "enum-on-Entry representation," not the enum's declaration site. |
| A2 | `Badge` recommended over `AssistChip` for the list-row surface specifically (detail header either works) | Architecture Patterns / Alternatives | Low — cosmetic; D-02 permits either Material3 primitive. |
| A3 | Litmus application to `CardBase`/`ChipBar`/`HeatSwatch` (pattern/primitive/pattern) | Code Examples | Medium — this is reasoning applied to inputs read this session, not a ratified decision; if the planner or a discuss-phase disagrees on one of the three, only the `docs/DESIGN-INTENT.md` worked examples and that one entry's `tier` value need correction, not the mechanism. |
| A4 | `Badge`/`AssistChip` Material3 composable signatures | Architecture Patterns, Code Examples | Low — `[CITED: developer.android.com]`, a stable, long-shipped Compose Material3 API; unlikely to have changed shape, but not verified against this repo's exact resolved BOM version's KDoc. |

## Open Questions

1. **Should `docs/DESIGN-INTENT.md` also cover `INTENTIONALLY_UNREGISTERED` sub-parts?**
   - What we know: LEG-01/LEG-02 scope to `ComponentRegistry.entries` (registered components).
     `INTENTIONALLY_UNREGISTERED` (`ComponentRegistry.kt:96-115`, 4 entries: `WaveformCanvas`,
     `SwipeableActionRow`, `RevealActionRow`, `YahirAndroidTasteTheme`) is a separate `Map<String,
     String>` of excluded sub-parts, not `Entry` instances — no `tier` field applies to it.
   - What's unclear: Whether the design-intent doc should mention that sub-parts are out of the
     tier taxonomy entirely (a one-line scope note) or stay silent.
   - Recommendation: A one-line scope note in `docs/DESIGN-INTENT.md` ("this litmus applies to
     registered showcaseable components; `INTENTIONALLY_UNREGISTERED` sub-parts are infrastructure,
     not independently tiered") costs nothing and pre-empts a Phase 2 audit question.

2. **`API.md`'s composable/family counts are stale relative to the live registry — does this phase touch `API.md`?**
   - What we know: `API.md:27-28` states "51 registered public composables… plus 5
     intentionally-unregistered… = 56 public composables total" `[VERIFIED: API.md:27-28]`, but
     `ComponentRegistry.kt`'s own doc comment and this session's live counts show **53 registered +
     4 intentionally-unregistered = 57 total** `[VERIFIED: ComponentRegistry.kt:19-20 and the 53
     grep-counted call sites above and the 4-entry INTENTIONALLY_UNREGISTERED map read this
     session]`.
   - What's unclear: Whether fixing this pre-existing drift is in scope for Phase 1 or a
     pre-existing, unrelated doc-staleness issue.
   - Recommendation: Out of scope for this phase's LEG-01/LEG-02 requirements — `docs/DESIGN-INTENT.md`
     is explicitly a *new*, *distinct* doc from `API.md` (per canonical_refs). Flag but do not fix
     `API.md`'s stale count as part of this phase's plan; note it as a candidate follow-up (or file
     to `FOLLOWUPS.md`) instead of silently expanding scope.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Gradle / AGP toolchain | Build, `apiDump`/`apiCheck`, `testDebugUnitTest`, `detekt` | ✓ (repo builds today per `[VERIFIED: build.gradle.kts]` and `./gradlew tasks` ran successfully this session) | AGP 9.2.1 / Kotlin 2.3.20 (per root `CLAUDE.md`) | — |
| Robolectric | Unit tests touching Compose UI (`GalleryDemoInteractionTest`, etc.) | ✓ (`testImplementation(libs.robolectric)` `[VERIFIED: build.gradle.kts:109]`) | — | — |
| Metalava plugin | `apiDump`/`apiCheck` | ✓ (`alias(libs.plugins.metalava)` `[VERIFIED: build.gradle.kts:7]`) | — | — |

No missing dependencies — this phase requires nothing beyond what's already resolved in the
project's Gradle setup.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric (`RobolectricTestRunner`, `@Config(sdk = [35])`) for Compose-UI-touching tests; plain JUnit4 for pure-logic tests |
| Config file | `build.gradle.kts` `testOptions { unitTests { ... } }` block (line 67+) |
| Quick run command | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.explorer.*"` |
| Full suite command | `./gradlew testDebugUnitTest` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| LEG-01 | Every `Entry` carries a queryable `tier` | compile-time (no runtime test needed — the compiler enforces "required field" at every call site) | `./gradlew compileDebugKotlin` (compile gate) | N/A — enforced by the Kotlin compiler itself, not a JUnit test |
| LEG-01 | Registry stays coherent (no dupes/overlap) after the field addition | unit | `./gradlew testDebugUnitTest --tests "*ComponentRegistrySearchTest*" --tests "*ComponentRegistryDriftGuardTest*"` | ✅ both exist, `[VERIFIED: ComponentRegistrySearchTest.kt, ComponentRegistryDriftGuardTest.kt]` |
| LEG-01 | Gallery displays tier on list row + detail header (D-02) | manual / UI (Robolectric Compose-UI test could assert the badge text node exists, but no such test exists yet) | Wave 0 gap — see below | ❌ new test needed if automated assertion desired |
| LEG-02 | `docs/DESIGN-INTENT.md` exists, states both contracts + litmus | manual (doc content, not code) | N/A — human/plan-checker review | ❌ N/A, doc-only requirement |

### Sampling Rate
- **Per task commit:** `./gradlew compileDebugKotlin` (fast: catches missing `tier = ...` at any
  of the 53 call sites immediately) then `./gradlew testDebugUnitTest --tests
  "io.github.ygaray.yahirandroidtaste.explorer.*"`
- **Per wave merge:** `./gradlew testDebugUnitTest` (full suite) + `./gradlew apiCheck` + `./gradlew detekt`
- **Phase gate:** Full suite + `apiCheck` + `detekt` green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] Optional: a Robolectric Compose-UI test asserting `ComponentRow` and
  `ComponentDetailScreen` actually render a tier badge/text node for a known entry (e.g. assert
  `onNodeWithText("Primitive")` or similar exists for a known-primitive entry). Not required by
  LEG-01/LEG-02's literal text (which asks for *display*, verifiable manually by running the
  gallery, per the existing `GalleryDemoInteractionTest` KDoc precedent of treating some things as
  "confirmed visually by running the gallery app — NOT by a rendered test"), but strengthens the
  D-02 success criterion if the planner wants automated coverage beyond manual UAT.
- [ ] No fixture/conftest-equivalent gaps — this module's existing `ComponentRegistry.entries` IS
  the fixture data; no new test infrastructure needed.

*(If the planner elects manual UAT only for the gallery-display criterion, matching this codebase's
own precedent for un-drivable UI states: "None beyond compile/registry-integrity tests — gallery
display verified manually per this module's own established UAT convention.")*

## Security Domain

`security_enforcement: true`, `security_asvs_level: 1` per `.planning/config.json`. This phase adds
a compile-time enum field to a library data model and a text badge to a debug-only gallery
(`ExplorerActivity` — the standalone showcase harness, not shipped app UI). No user input, no
network call, no auth/session, no persisted/serialized data, no cryptography.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | No | No auth surface touched. |
| V3 Session Management | No | No session state touched. |
| V4 Access Control | No | No access-control surface touched. |
| V5 Input Validation | No | `Tier` is a closed Kotlin `enum class` (exhaustive `when`, no free-form string input) — the type system is the validation; no external/user-supplied string is parsed into it. |
| V6 Cryptography | No | No cryptography touched. |

### Known Threat Patterns for this stack
None applicable — this is a compile-time metadata field plus a debug-gallery text label; no
attack surface is introduced.

## Sources

### Primary (HIGH confidence)
- Direct file reads this session (all `[VERIFIED: path:lines]` tags above):
  `ComponentRegistry.kt`, `ExplorerIndexScreen.kt`, `ComponentDetailScreen.kt`,
  `ComponentRegistryDriftGuardTest.kt`, `ComponentRegistrySearchTest.kt`,
  `GalleryDemoInteractionTest.kt`, `api.txt`, `build.gradle.kts`, `config/detekt/detekt.yml`,
  `gradle/libs.versions.toml`, `API.md`, `CardBase.kt`, `ChipBar.kt`, `HeatSwatch.kt`,
  `CardTypeChip.kt`, `AppChip.kt`, `RelatednessEncoding.kt`, `ThemeMode.kt`, all 9
  `*FamilyScreen.kt` files (call-site counting), `.planning/REQUIREMENTS.md`,
  `.planning/ROADMAP.md`, `.planning/v1.0-DECISION-MAP.md`, `.planning/config.json`.

### Secondary (MEDIUM confidence)
- [developer.android.com Compose Material3 releases](https://developer.android.com/jetpack/androidx/releases/compose-material3) — `Badge` composable signature, confirmed via WebSearch this session.

### Tertiary (LOW confidence)
- None used as load-bearing claims; general Compose Material3 API familiarity (e.g. `AssistChip`'s
  broad shape) is training knowledge and is flagged `[ASSUMED]` implicitly wherever not backed by
  a `[VERIFIED]`/`[CITED]` tag above — no such claim is load-bearing to this phase's plan.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new dependencies; every touch point read directly this session.
- Architecture: HIGH — exact file paths, line numbers, and call-site counts confirmed via `Read`/`grep` this session.
- Pitfalls: HIGH — derived from reading the actual `detekt.yml` thresholds, `api.txt` current signature, and test files rather than assumed Kotlin/Gradle behavior.

**Research date:** 2026-09-01
**Valid until:** 2026-10-01 (30 days — stable toolchain, low churn risk; re-verify if `build.gradle.kts`, `ComponentRegistry.kt`, or `api.txt` change materially before planning executes)
