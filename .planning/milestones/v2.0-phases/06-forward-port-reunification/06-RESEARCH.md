# Phase 6: Forward-port reunification - Research

**Researched:** 2026-09-26
**Domain:** Android/Kotlin/Jetpack Compose — forward-porting components across a diverged git history (restore-from-tag), reusable-library governance gates (ComponentRegistry drift guard, domain-vocabulary drift guard, metalava API baseline, detekt zero-baseline)
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**D-01 [tier]:** `DateTimePicker`, `PlaceMapPicker`, and `PresetChip` all register with `tier = PATTERN` (human). They bake in a composition/interaction convention, matching peers (AccentColorPicker/IconPickerGrid). NOTE: there is **no drift guard on `tier`** — a wrong value ships silently as a mislabeled gallery altitude badge, so this was confirmed deliberately, not defaulted.

**D-02 [osmdroid-pin]:** Pin `org.osmdroid:osmdroid-android` at `6.1.20` (implementation scope), verbatim from v1.13.0; **before the tag is cut, verify 6.1.20 is current / carries no open advisories** (it becomes a transitive runtime dep for every consumer); add an INTEGRATION.md/ECOSYSTEM.md note that a consumer rendering `PlaceMapPicker` must declare INTERNET itself (tiles cache to `cacheDir`, no storage permission) (human). Pre-approved in `.planning/APPROVED-DEPS.md`; also add to `CLAUDE.md` allowed-deps.

**D-03 [map-render]:** Rely on the hub's `configureOsmdroid` (cacheDir tiles, `userAgent = packageName`) for the live `MapView` gallery preview; confirm it actually renders **on-device at Gate-1** — it is device-only-verifiable, not resolvable from source (ai-auto).

### Claude's Discretion

None explicitly marked — this phase's discussion produced only locked decisions (D-01/D-02/D-03), all confirmed deliberately per CONTEXT.md's own framing ("NOT defaulted").

### Deferred Ideas (OUT OF SCOPE)

None — discussion stayed within phase scope (per CONTEXT.md `<deferred>`).

</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| REUNI-01 | `DateTimePicker` is available on `main` — forward-ported from `v1.13.0` with its test, registered in `ComponentRegistry`, and shown in the ExplorerActivity gallery. | Restore path confirmed via `git ls-tree v1.13.0` + `Read` of `PickersFamilyScreen.kt`'s v1.13.0 demo code (Pattern 1, Code Examples); exact `Entry` ctor shape on `main` confirmed via direct `Read` of `ComponentRegistry.kt` (Pitfall 2) |
| REUNI-02 | The `PlaceMap*` cluster (`PlaceMapPicker`, `PlaceMapOsmdroidConfig`, `PlaceMapPickerModel`, `model/SavedPlaceUiModel`) is available on `main` — forward-ported with its 4 tests, registered, and shown in the gallery. | Path correction identified (Pitfall 1 — `PlaceMapPickerModel.kt` is under `component/`, not `model/`); `ChipBar` same-package call-site compatibility directly verified (Pattern 3); `configureOsmdroid` call chain directly verified (Pattern 2, Code Examples) |
| REUNI-03 | `PresetChip` is available on `main` — forward-ported with its test, registered, and shown in the gallery. | Two-overload public API confirmed via full-file `git show` (Code Examples); `ChipBar` `itemContent` integration confirmed |
| REUNI-04 | `osmdroid` is admitted as an approved implementation-scope dependency (`.planning/APPROVED-DEPS.md` + `CLAUDE.md` allowed-deps), and the ported composables' domain-vocabulary head tokens (`Date`, `Preset` → primitive; `Place` → domain-vocab w/ rationale) are allowlisted so both drift guards stay green. | Currency verified against Maven Central metadata (Package Legitimacy Audit); exact `PRIMITIVE_NOUN_ALLOWLIST`/`DOMAIN_VOCABULARY` key-shape difference confirmed by direct `Read` of `DomainVocabularyDriftGuardTest.kt` (Code Examples) |
</phase_requirements>

## Summary

This phase is a **git-restore-from-tag** operation, not new feature development: six files (5 components + 1 model) and 5 tests exist verbatim at tag `v1.13.0` and must be copied onto `main` unmodified except for one mandatory registration-shape change (`Entry(...)` on `main` requires an explicit `tier` argument that didn't exist on the v1.x line). Every shared dependency the ported files touch (`theme.Dimens`, `ChipBar`) was directly diffed/read this session and confirmed compatible: `theme/Dimens.kt` is byte-identical between `v1.13.0` and `main`, and `PlaceMapPicker`'s same-package call into `ChipBar(items=..., key=..., itemContent=..., testTag=...)` uses only named params that are unchanged on `main`'s `ChipBar` (which only *added* two optional trailing params, `expandable`/`rawContent`). `PresetChip` is confirmed to be a genuine two-overload public API (a full signature with required `contentDescription: String?`, and a legacy overload without it that delegates with `contentDescription = null`) — both must port, matching CONTEXT.md.

One path correction versus the phase brief's own investigation targets: `PlaceMapPickerModel.kt` lives at `component/PlaceMapPickerModel.kt` in `v1.13.0` (git-confirmed), **not** `model/PlaceMapPickerModel.kt` as one of this phase's investigation-target bullets and REQUIREMENTS.md's prose imply — CONTEXT.md's own `<code_context>` section already has this right (it lists `PlaceMapPickerModel` inside the `component/{...}` group). Only `SavedPlaceUiModel.kt` is under `model/`. The planner must restore `PlaceMapPickerModel.kt` to `component/`, matching its test file's existing location at `src/test/.../component/PlaceMapPickerModelTest.kt`.

The `osmdroid-android` dependency pin (`6.1.20`) is confirmed current: Maven Central's own metadata lists `6.1.20` as both `<latest>` and `<release>` — there is no newer version to consider. No CVE/advisory was found for osmdroid in this session, but per this project's absent-evidence rule that is reported as an unresolved absence, not a verified clean bill of health (no authoritative advisory database was queried directly).

**Primary recommendation:** Treat this as three restore batches, each independently verifiable — (1) `git checkout v1.13.0 -- <6 files + 5 tests>`, adjust import paths only where the model moved (`model/PlaceMapPickerModel.kt` doesn't exist — restore stays at `component/`), (2) add `osmdroid` to `gradle/libs.versions.toml` + `build.gradle.kts` + `CLAUDE.md` + confirm `.planning/APPROVED-DEPS.md` (already recorded), (3) register the 3 composables in `ComponentRegistry` (via `PickersFamilyScreen.kt`/`ChipsFamilyScreen.kt`, adding `tier = ComponentRegistry.Tier.PATTERN` to each restored `Entry(...)`) and add the corresponding `PRIMITIVE_NOUN_ALLOWLIST`/`DOMAIN_VOCABULARY` entries in `DomainVocabularyDriftGuardTest.kt`. Run `./gradlew testDebugUnitTest detekt apiDump apiCheck` after each batch.

## Architectural Responsibility Map

This phase has a single architectural tier: it is entirely within the **reusable Compose UI library** (no client/server split, no backend). All six capabilities below live in the same tier.

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Date/time selection UI (`DateTimePicker`) | Library / Compose component | — | Self-contained `@Composable`, no I/O, matches existing `PickersFamilyScreen` peers |
| Location pin + radius + map surface (`PlaceMapPicker` cluster) | Library / Compose component | Library / osmdroid (map tile rendering, implementation-scope) | `PlaceMapPicker` owns the UI; osmdroid's `MapView` is an embedded `AndroidView` the component wraps — the map-tile rendering itself is delegated to the osmdroid runtime dependency, never exposed in the public signature |
| Preset one-tap chip (`PresetChip`) | Library / Compose component | — | `ChipBar` `itemContent` shape, same pattern as `AppChip`/`TagChipWithContextMenu` |
| Component catalog registration | Library / Explorer gallery (`ComponentRegistry` + family screens) | — | D-05 single source of truth; not app logic |
| Governance gates (drift guards, detekt, metalava) | Library / build-time tooling | — | Enforced at build time, ships in no `.aar` |
| osmdroid map tile transport | Library / implementation dependency | Consumer app (must declare `INTERNET` permission) | Hub configures cache-dir tiles + user-agent (`configureOsmdroid`, `internal`); consumer owns its own manifest permission — this is a documented cross-tier contract, not a hub responsibility gap |

## Package Legitimacy Audit

`osmdroid-android` is a **Maven** dependency (not npm/PyPI/crates) — the `package-legitimacy check` seam does not cover this ecosystem. Verification was performed manually per this project's own execution-note pattern (see `.planning/APPROVED-DEPS.md`, which already records a full human-approved entry for this exact package/version as of 2026-09-26, Phase 6).

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| `org.osmdroid:osmdroid-android` | Maven Central | Project active since ~2013 (osmdroid v3.0.7, 2013); `6.1.20` is a 2020s-era release line | Not measurable via Maven Central API (no npm-style download counts) | `github.com/osmdroid/osmdroid` — 2.4k+ stars, actively maintained (dependabot bump PRs exist against this exact 6.1.18→6.1.20 range) [VERIFIED: repo1.maven.org/maven2/org/osmdroid/osmdroid-android/maven-metadata.xml] | OK (human-approved, already recorded) | Approved — already in `.planning/APPROVED-DEPS.md` and pending `CLAUDE.md` addition (this phase's D-02) |

**Packages removed due to [SLOP] verdict:** none.
**Packages flagged as suspicious [SUS]:** none. This is a pre-approved, already-vetted, already-shipped-in-v1.13.0 dependency — the only new action this phase performs is re-confirming currency before the tag cut (D-02) and formally adding it to `CLAUDE.md`'s allowed-deps list (currently absent — verified by reading `CLAUDE.md` line 24, which lists only "Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable").

**Currency check [VERIFIED: repo1.maven.org/maven2/org/osmdroid/osmdroid-android/maven-metadata.xml — fetched this session]:** Maven Central's `maven-metadata.xml` for `org.osmdroid:osmdroid-android` lists `<latest>6.1.20</latest>` and `<release>6.1.20</release>` as of this session — **6.1.20 is the current release, no newer version exists to consider.**

**Advisory check [reported: no observation, not a verified absence]:** A WebSearch for "osmdroid CVE security advisory vulnerability" returned no osmdroid-specific CVE records, and Sonatype OSS Index listed 6.1.0 with no known vulnerabilities. This is an absence of found evidence, not a queried authoritative advisory database (no direct GHSA/OSV/Snyk API query was run against this exact artifact this session) — per this project's absent-evidence provenance rule, this is `[ASSUMED]`, not `[VERIFIED]`. If a stricter check is wanted before the `v2.2.0` tag cut, query `https://osv.dev/list?ecosystem=Maven&q=osmdroid` or GitHub's advisory database directly for `org.osmdroid:osmdroid-android`.

## Standard Stack

No new *framework* is being introduced — this phase restores existing, already-designed components. The only stack addition is the dependency below.

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `org.osmdroid:osmdroid-android` | `6.1.20` [VERIFIED: Maven Central metadata, this session] | Backs `PlaceMapPicker`'s `MapView` surface (OSM tile rendering) | Already shipped on the v1.x line at this exact pin; open-source, actively maintained, no viable simpler alternative for OSM-tile Android map rendering without a Google Maps API key dependency |

### Supporting
None — no other new library is introduced. `theme.Dimens`, `ChipBar`, and Robolectric/Compose-UI-test infra used by the 5 ported tests all already exist on `main`.

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| osmdroid | Google Maps Compose | Requires a Maps API key + billing account per consumer — breaks the hub's zero-config drop-in invariant; not considered, this is a forward-port of an already-shipped, already-approved v1.x decision, not a fresh library choice |

**Installation:**
```kotlin
// gradle/libs.versions.toml — [versions] block (verbatim from v1.13.0, confirmed via
// `git show v1.13.0:gradle/libs.versions.toml`)
osmdroid = "6.1.20"

// [libraries] block
osmdroid-android = { group = "org.osmdroid", name = "osmdroid-android", version.ref = "osmdroid" }
```
```kotlin
// build.gradle.kts — dependencies block (verbatim comment from v1.13.0, confirmed via
// `git show v1.13.0:build.gradle.kts`, lines 94-97)
// HUBW-02 map surface for PlaceMapPicker (D-01): an implementation dependency because
// no osmdroid type appears in a public signature. Archived upstream, human-approved in
// .planning/APPROVED-DEPS.md.
implementation(libs.osmdroid.android)
```

**Version verification:** [VERIFIED: repo1.maven.org/maven2/org/osmdroid/osmdroid-android/maven-metadata.xml, fetched this session] — `<latest>` and `<release>` both report `6.1.20`. No `npm view`/`pip index` equivalent exists for Maven; this is the authoritative Maven Central registry metadata endpoint.

## Architecture Patterns

### System Architecture Diagram

```
git tag v1.13.0 (v1.x line, retired after this phase)
        |
        |  git checkout v1.13.0 -- <path>   (restore, file-for-file)
        v
+-------------------------------------------------------------+
|  main (this repo, single-module hub)                        |
|                                                               |
|  component/DateTimePicker.kt        --uses--> theme/Dimens   |
|  component/PresetChip.kt            --uses--> theme/Dimens   |
|  component/PlaceMapPicker.kt        --uses--> theme/Dimens   |
|        |                             --uses--> model/SavedPlaceUiModel.kt (occurrenceIndices,
|        |                                         savedPlaceChipLabels, SavedPlaceChipLabel)
|        |                             --calls--> component/ChipBar.kt (same-package,
|        |                                         items/key/itemContent/testTag only)
|        |                             --calls--> component/PresetChip.kt (as ChipBar itemContent)
|        v
|  component/PlaceMapPickerModel.kt (pure math: distance/bearing/etc, kotlin.math only)
|        |
|        v
|  component/PlaceMapOsmdroidConfig.kt --calls--> org.osmdroid.config.Configuration
|        (configureOsmdroid: internal fun, invoked from createPlaceMapView() inside
|         PlaceMapPicker.kt's AndroidView factory — NOT called separately by any caller)
|
|  explorer/ComponentRegistry.kt  <--registers-- explorer/PickersFamilyScreen.kt
|        ^                                        (DateTimePicker, PlaceMapPicker entries)
|        |                        <--registers-- explorer/ChipsFamilyScreen.kt
|        |                                        (PresetChip entry)
|        |
|  explorer/DomainVocabularyDriftGuardTest.kt --scans--> every non-explorer .kt file's
|        (PRIMITIVE_NOUN_ALLOWLIST / DOMAIN_VOCABULARY)   public top-level @Composable names
|  explorer/ComponentRegistryDriftGuardTest.kt --scans--> same universe, cross-checks against
|        (registered XOR intentionally-unregistered)      ComponentRegistry.entries
|
+-------------------------------------------------------------+
        |
        v
   ./gradlew testDebugUnitTest / detekt / apiDump+apiCheck / publishReleasePublicationToMavenLocal
   (Gate-1, in-hub, autonomous) --> ExplorerActivity gallery launch (device-only, Gate-1 human/agentic UAT)
```

### Recommended Project Structure
No new directories. Restored files land in their existing package locations:
```
src/main/java/io/github/ygaray/yahirandroidtaste/
├── component/
│   ├── DateTimePicker.kt          # restored verbatim from v1.13.0
│   ├── PlaceMapPicker.kt          # restored verbatim from v1.13.0
│   ├── PlaceMapOsmdroidConfig.kt  # restored verbatim from v1.13.0
│   ├── PlaceMapPickerModel.kt     # restored verbatim — stays under component/, NOT model/
│   └── PresetChip.kt              # restored verbatim (2-overload public API)
├── model/
│   └── SavedPlaceUiModel.kt       # restored verbatim from v1.13.0
├── explorer/
│   ├── PickersFamilyScreen.kt     # edited: add DateTimePicker + PlaceMapPicker Entry(...)
│   ├── ChipsFamilyScreen.kt       # edited: add PresetChip Entry(...)
│   └── DomainVocabularyDriftGuardTest.kt  # edited: allowlist Date/Preset, add PlaceMapPicker to DOMAIN_VOCABULARY
src/test/java/io/github/ygaray/yahirandroidtaste/component/
├── DateTimePickerTest.kt           # restored verbatim
├── PlaceMapPickerTest.kt           # restored verbatim
├── PlaceMapOsmdroidConfigTest.kt   # restored verbatim
├── PlaceMapPickerModelTest.kt      # restored verbatim
└── PresetChipTest.kt               # restored verbatim
```

### Pattern 1: Restore-then-adapt registration (not a rewrite)
**What:** The 6 component/model files and 5 tests are restored byte-for-byte via `git checkout v1.13.0 -- <path>`. The *only* code that must be hand-written (not restored) is the `ComponentRegistry.Entry(...)` call sites in `PickersFamilyScreen.kt`/`ChipsFamilyScreen.kt` and the two drift-guard allowlist edits, because `main`'s `Entry` data class gained fields (`states`, `content`, `controls`, `preview`, and the no-default `tier`) that didn't exist on the v1.x line.
**When to use:** Any forward-port from a diverged tag where the destination's registration/governance surface evolved independently.
**Example — v1.13.0's actual registration (no `tier`, pre-States-matrix shape):**
```kotlin
// Source: git show v1.13.0:src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
ComponentRegistry.Entry(
    name = "DateTimePicker",
    family = ExplorerFamilies.PICKERS,
    states = listOf(/* ...same 4-cell shape main already uses for peers... */),
    content = { DateTimePickerVariants() }
    // no `tier` field existed at v1.13.0
)
```
**What main's `Entry(...)` requires today** [VERIFIED: src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt:72-80 — read this session]:
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
So the planner's task must add exactly one field per restored `Entry(...)` call site: `tier = ComponentRegistry.Tier.PATTERN` (per CONTEXT.md D-01, confirmed deliberately, no drift guard on this field). `states`/`content` can be restored as-is from v1.13.0's shape (already 4-cell, already matches main's `StateCell` structure — confirmed identical field usage in `PickersFamilyScreen.kt`'s live peers `AccentColorPicker`/`IconPickerGrid`/`SegmentedOptionSelector`, read this session).

### Pattern 2: `PlaceMapPicker`'s internal osmdroid wiring — no separate gallery call needed
**What:** `configureOsmdroid(context, userAgent)` is `internal` and is called automatically from `PlaceMapPicker.kt`'s private `createPlaceMapView(...)` factory function, itself invoked from the `AndroidView(factory = { context -> createPlaceMapView(context, userAgent, holder, lifecycleOwner) })` call inside `PlaceMapPicker`'s own composition [VERIFIED: `git show v1.13.0:src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt` lines 489, 704-711, read this session]. The gallery preview only needs to call the public `PlaceMapPicker(...)` composable with a `userAgent` param (e.g. `LocalContext.current.packageName`) — it does **not** need to call `configureOsmdroid` itself.
**When to use:** Writing the `PlaceMapPicker` gallery preview (`PickersFamilyScreen.kt`).
**Example (v1.13.0's actual gallery demo, restorable near-verbatim):**
```kotlin
// Source: git show v1.13.0:src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
PlaceMapPicker(
    pinLatitude = latitude,
    pinLongitude = longitude,
    onPinChange = { lat, lng -> latitude = lat; longitude = lng },
    radiusMeters = radius,
    onRadiusChange = { radius = it },
    minRadiusMeters = 50f,
    maxRadiusMeters = 1000f,
    defaultRadiusMeters = 150f,
    radiusStepMeters = 50f,
    userAgent = LocalContext.current.packageName,
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    onUseCurrentLocation = { latitude = 47.6; longitude = -122.3 },
    searchQuery = searchQuery,
    onSearchQueryChange = { searchQuery = it },
    onSearch = { /* sets a second fixture coordinate */ },
    savedPlaces = listOf(SavedPlaceUiModel("Home", 40.0, -74.0, 150f), /* ... */),
    onSavedPlaceSelected = { place -> /* ... */ }
)
```
Note: this demo uses hard-coded fixture lat/lng (never rendered as text — a deliberate v1.x convention, "T-164-04" in the original code comments) — preserve that convention when restoring, don't add a live-location permission flow to the gallery.

### Pattern 3: `ChipBar` same-package call compiles clean — verified, not assumed
**What:** `PlaceMapPicker.kt`'s `PlaceMapSavedPlacesSection` composable calls `ChipBar(items = chipItems, key = { it.place to it.occurrence }, itemContent = { item -> PresetChip(...) }, testTag = "${testTag}_saved_places_chips")` [VERIFIED: `git show v1.13.0:.../PlaceMapPicker.kt` lines 384-395, read this session]. `main`'s `ChipBar<T>` signature is `fun <T> ChipBar(items: List<T>, key: (T) -> Any, itemContent: @Composable (T) -> Unit, modifier: Modifier = Modifier, testTag: String = "chip_bar", leadingContent: (@Composable () -> Unit)? = null, trailingContent: (@Composable () -> Unit)? = null, expandable: ExpandableConfig? = null, rawContent: (@Composable FlowRowScope.() -> Unit)? = null)` [VERIFIED: src/main/java/io/github/ygaray/yahirandroidtaste/component/ChipBar.kt:97-106, read this session]. All 4 named args the call site passes (`items`, `key`, `itemContent`, `testTag`) exist unchanged on `main` — `main` only *added* two optional trailing params after the v1.x line diverged. **This confirms CONTEXT.md's compile-clean claim** with a direct byte-level read of both sides, not an inference.
**When to use:** Confidence check before restoring `PlaceMapPicker.kt` — no source edit to the call site is needed.

### Anti-Patterns to Avoid
- **Restoring standalone `FilterBar`:** Explicitly out of scope (design spec §2/§3.3, REQUIREMENTS.md Out-of-Scope table). `ChipBar`'s `expandable`/`rawContent` modes are the FilterBar successor — do not reintroduce the duplicate the v2.0.0 coherence audit removed.
- **Editing the ported component/model/test files' logic:** This is a restore, not a refactor. Any deviation from the `v1.13.0` byte content (beyond the mandatory `tier` field addition to `Entry(...)` call sites) should be treated as scope creep and flagged.
- **Calling `configureOsmdroid` from the gallery layer:** It's `internal` and already wired inside `PlaceMapPicker` itself — see Pattern 2.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| OSM tile map rendering | A custom `Canvas`-based tile renderer or a hand-rolled `WebView`+Leaflet wrapper | `org.osmdroid:osmdroid-android:6.1.20` (already the v1.x-approved choice) | Tile-server protocol compliance (User-Agent, cache-header honoring, no bulk pre-fetch — the OSM tile usage policy) is exactly what `PlaceMapOsmdroidConfig.kt`'s `configureOsmdroid` already encodes; reimplementing this is pure risk with zero reusability benefit |
| Distance/bearing/radius math for the saved-places feature | New geo-math utilities | `component/PlaceMapPickerModel.kt` (pure `kotlin.math`, no Android/osmdroid dependency) [VERIFIED: imports are `kotlin.math.{PI,abs,atan2,cos,max,min,pow,round,sin,sqrt}` only, read via `git show v1.13.0` this session] | Already written, already tested (`PlaceMapPickerModelTest.kt`), zero external dependency — a rewrite would only reintroduce bugs already fixed on the v1.x line |
| Public-API-surface drift detection | A manual "did anyone forget to update api.txt" checklist | `./gradlew apiDump` / `apiCheck` (metalava, already wired) [VERIFIED: build.gradle.kts lines 7, 20-29, read this session] | Already the enforced mechanism (`api.txt` exists at repo root, confirmed empty of any `DateTimePicker`/`PlaceMapPicker`/`PresetChip` symbol today — confirming the net-additive claim) |

**Key insight:** Nothing in this phase should be *built* — every artifact needed already exists at `v1.13.0`. The only genuinely new code this phase writes is (a) the `tier` field on 3 `Entry(...)` call sites, (b) 3 domain-vocabulary allowlist entries, (c) 2 build-config lines + 1 `CLAUDE.md` sentence for osmdroid. Treating this as "restore + 3 small additive edits" rather than "port a feature" is the accurate mental model for planning task granularity.

## Runtime State Inventory

> Not applicable — this is a code-only forward-port within a single git repo. No renamed identifiers, no external stored data, no live service config, no OS-registered state. `osmdroid`'s tile cache is scoped to each **consumer's** own `cacheDir` at first `PlaceMapPicker` use post-repin — not a migration this hub phase performs (SHIP-02/consumer-side, out of this phase's scope).

**Nothing found in category:** Confirmed by direct inspection — no rename/refactor is happening in this phase; only net-new file restoration.

## Common Pitfalls

### Pitfall 1: Restoring `PlaceMapPickerModel.kt` to the wrong package
**What goes wrong:** Creating `model/PlaceMapPickerModel.kt` instead of `component/PlaceMapPickerModel.kt` (or leaving a duplicate in both places).
**Why it happens:** REQUIREMENTS.md's REUNI-02 prose and this phase's own investigation-target bullet #1 name it ambiguously ("model/PlaceMapPickerModel.kt"), which could be misread as a package path rather than a category label. Git ground truth says otherwise.
**How to avoid:** `git ls-tree -r v1.13.0 --name-only | grep PlaceMapPickerModel` returns exactly `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPickerModel.kt` [VERIFIED: `git ls-tree`, run this session] — restore there, not under `model/`. The test file's own existing path (`src/test/.../component/PlaceMapPickerModelTest.kt`) is a second confirming signal.
**Warning signs:** A compile error inside `PlaceMapPicker.kt` on `import io.github.ygaray.yahirandroidtaste.component.PlaceMapPickerModel*` symbols if the model landed under `model/` instead.

### Pitfall 2: Forgetting the mandatory `tier` field breaks the whole module's compile
**What goes wrong:** Restoring the v1.13.0 `Entry(...)` call sites verbatim (no `tier` field) fails to compile against `main`'s `Entry` data class, which has no default for `tier` — "the whole module will not compile again until every `Entry(...)` call site across all 9 family files supplies an explicit value" [VERIFIED: src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt:69-70, read this session, KDoc on the `tier` param].
**Why it happens:** The two lines diverged before `tier`/D-01 existed; a naive `git checkout v1.13.0 -- PickersFamilyScreen.kt` would silently overwrite the *entire file* including the 4 pre-existing entries (`AccentColorPicker`, `IconPickerGrid`, `CropOverlay`, `SegmentedOptionSelector`) that already have `tier` set on `main` today — reverting their tier assignments and breaking the build for reasons unrelated to the 2 new entries.
**How to avoid:** Do NOT `git checkout v1.13.0 -- PickersFamilyScreen.kt` (whole-file restore) — this file already diverged (both lines added *different* new entries on top of the same base). Instead, manually insert only the 2 new `Entry(...)` blocks (`DateTimePicker`, `PlaceMapPicker`) into the *current main* file, adding `tier = ComponentRegistry.Tier.PATTERN` to each, preserving all of `main`'s existing entries and their current `states`/`content`/`controls`/`preview` enrichments. Same for `ChipsFamilyScreen.kt` + `PresetChip`.
**Warning signs:** A git diff on `PickersFamilyScreen.kt`/`ChipsFamilyScreen.kt` that shows *removed* lines for entries other than the 2-3 new ones being added — that's the signal a whole-file restore clobbered main's own evolution.

### Pitfall 3: Vacuous-pass drift guards make a missing registration look green
**What goes wrong:** Both `ComponentRegistryDriftGuardTest` and `DomainVocabularyDriftGuardTest` are source-text scanners with an explicit "vacuous-pass guard" (they assert non-zero file/name counts before drawing conclusions) — so a genuinely broken scan fails loudly, but a **correctly-scoped scan that simply never sees the new files** (e.g. because they landed under the wrong package, per Pitfall 1) will report a clean pass on the *files it did find*, while the registry/allowlist gap on the new files goes undetected until `ComponentRegistryDriftGuardTest`'s own separate `everyPublicComposableIsRegisteredOrAllowlisted` check runs — which WILL still catch an unregistered composable in the right package, per its own design. The residual risk is specifically Pitfall 1 (wrong package) making the scan miss the file entirely if it lands outside `src/main/java/io/github/ygaray/yahirandroidtaste/` in some way.
**Why it happens:** Both guards resolve their own source root independently (duplicated verbatim resolution logic) and scan every top-level package except `explorer/` — so any correctly-placed new file IS caught; only a wrong location (outside the module source root proper) would evade it.
**How to avoid:** After restoring, run `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` explicitly and confirm both actually execute (not skipped) and go green — don't infer green from `testDebugUnitTest`'s aggregate pass alone during early restore iterations.
**Warning signs:** Either test reporting 0 scanned files (its own vacuous-pass assertion should already fail this loudly) or passing without any change in behavior after a restore you expected to change coverage.

### Pitfall 4: Detekt's zero-baseline policy meets v1.x-authored code that predates current lint config
**What goes wrong:** The 6 restored files were last touched under whatever detekt ruleset was active in the v1.x line as of 2026-09-15 (tag date). If `main`'s `config/detekt/detekt.yml`/`config/detekt-compose.yml` tightened any rule since the merge-base, a restored file could introduce a *new* detekt finding on `main`, and per CLAUDE.md's zero-baseline policy this must be fixed, not baselined away.
**Why it happens:** Two lines evolving independently for ~2 months (merge-base ~v1.10.0 to now) can each tighten lint config differently.
**How to avoid:** Run `./gradlew detekt` immediately after each restore batch, not only at the end — isolate any new finding to the specific restored file rather than discovering a pile of findings after all 3 are in.
**Warning signs:** `detekt` failing on a restored file for a rule that wasn't violated in the file's git history at any prior commit on the v1.x line — that's the tightened-config signal, not a v1.x-era code-quality issue.

## Code Examples

### `PresetChip`'s public overload pair (both must port)
```kotlin
// Source: git show v1.13.0:src/main/java/io/github/ygaray/yahirandroidtaste/component/PresetChip.kt
// (read verbatim this session — full file, not excerpted)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingLabel: String? = null,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    contentDescription: String?   // no default — required on this overload
) { /* ... */ }

@Composable
fun PresetChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingLabel: String? = null,
    enabled: Boolean = true,
    isSelected: Boolean = false
) {
    PresetChip(label, onClick, modifier, supportingLabel, enabled, isSelected, contentDescription = null)
}
```

### `configureOsmdroid`'s exact signature (D-03's live MapView preview reference)
```kotlin
// Source: git show v1.13.0:src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapOsmdroidConfig.kt
// (read verbatim this session — full file)
internal fun configureOsmdroid(context: Context, userAgent: String) {
    val appContext = context.applicationContext
    val prefs = appContext.getSharedPreferences("yahirandroidtaste_osmdroid", Context.MODE_PRIVATE)
    Configuration.getInstance().load(appContext, prefs)
    Configuration.getInstance().userAgentValue = resolveOsmdroidUserAgent(userAgent, appContext.packageName)
    val basePath = File(appContext.cacheDir, "osmdroid")
    Configuration.getInstance().osmdroidBasePath = basePath
    Configuration.getInstance().osmdroidTileCache = File(basePath, "tiles")
}
```
Called only from `PlaceMapPicker.kt`'s private `createPlaceMapView(context, userAgent, holder, lifecycleOwner): MapView` at its first line — never called directly by the gallery or any other caller.

### Domain-vocabulary allowlist additions needed (exact shape to add)
```kotlin
// PRIMITIVE_NOUN_ALLOWLIST is a Set<String> of HEAD TOKENS (single leading PascalCase word).
// Add "Date" and "Preset":
val PRIMITIVE_NOUN_ALLOWLIST: Set<String> = setOf(
    // ...existing entries [VERIFIED verbatim, DomainVocabularyDriftGuardTest.kt:297-315]...
    "Date", "Preset"
)

// DOMAIN_VOCABULARY is a Map<String, String> keyed by the FULL COMPOSABLE NAME (not the head
// token alone) -> rationale. Add "PlaceMapPicker":
val DOMAIN_VOCABULARY: Map<String, String> = mapOf(
    // ...existing entries [VERIFIED verbatim, DomainVocabularyDriftGuardTest.kt:325-394]...
    "PlaceMapPicker" to
        "Head token 'Place' leans location-domain; acknowledged explicitly per Phase 6 D-05.2 " +
        "rather than treated as a domain-agnostic primitive."
)
```
Note the key-shape difference between the two lists — `PRIMITIVE_NOUN_ALLOWLIST` keys on the bare head token (`"Card"`, `"Chip"`), `DOMAIN_VOCABULARY` keys on the full composable name (`"VoiceCard"`, `"TagChipWithContextMenu"`) — confirmed by reading both existing populated lists this session; the planner must not conflate the two shapes when writing the new entries.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Two divergent hub release lines (`v1.x` for SecondBrain, `v2.x`/`main` for CalTracker) | Single `main` line, tagged `v2.2.0` | This milestone (v2.0), Phase 6 | v1.x line retires after Phase 9's coordinated repin; no more cherry-picks needed either direction |
| `ComponentRegistry.Entry(name, family)` (2-arg, v1.x shape) | `Entry(name, family, states, content, controls, preview, tier)` (7-arg, `tier` required, others default) | Phases 1-63 (tier legibility → States matrix → Playground), all landed on `main` after the v1.x/v2.x split | Any v1.x-authored `Entry(...)` call site needs the `tier` field added on restore — this phase's core mechanical task |

**Deprecated/outdated:** Standalone `FilterBar` — consolidated into `ChipBar`'s `expandable`/`rawContent` modes (Coherence-Audit Finding CH-1, commit `a966282`, prior to this milestone). Not reintroduced by this phase.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | No open osmdroid CVE/advisory exists against `6.1.20` | Package Legitimacy Audit | Low — this is a pre-approved, already-shipped-on-v1.x dependency; if a advisory did exist it would already be a live risk on the v1.x line today, not something this phase introduces. Recommend a direct OSV/GHSA query before the `v2.2.0` tag cut if the owner wants certainty rather than absence-of-search-hits. |
| A2 | Detekt config on `main` has not tightened any rule the 6 restored files would newly violate | Common Pitfalls, Pitfall 4 | Medium — would surface immediately as a `detekt` failure during execution (fail-loud, not silent), just adds an unplanned fix-up task; does not risk shipping a defect |

**If this table is empty:** N/A — see rows above; both are low-risk, fail-loud-if-wrong items, not silent-failure risks.

## Open Questions

1. **Should the `v1.13.0`-era `states`/`content` gallery demo code for the 3 composables be restored verbatim, or re-authored to match main's slightly-evolved demo conventions (e.g. `ExplorerFakeData` fixture usage patterns seen in `ChipsFamilyScreen.kt`'s other entries)?**
   - What we know: v1.13.0's demos are self-contained (don't reference any `ExplorerFakeData` member that doesn't exist on `main` — `DateTimePicker`/`PlaceMapPicker`/`PresetChip` demos use only local `remember`-scoped state and inline fixture literals, confirmed by reading the full v1.13.0 `PickersFamilyScreen.kt`/`ChipsFamilyScreen.kt` demo bodies this session).
   - What's unclear: Whether the planner should also add `controls`/`preview` (Playground live-knob) support to these 3 new entries to match the *most* fully-enriched main peers (e.g. `AppChip`'s `controls`/`preview`), or whether `states`/`content` alone (matching `IconPickerGrid`/`CropOverlay`'s simpler shape) satisfies this phase's success criteria.
   - Recommendation: Success criterion 1 only requires the 3 composables to "render in the ExplorerActivity gallery (family-screen previews)" — `states`/`content` alone satisfies this net-additively with the least restore-scope risk. Treat `controls`/`preview` enrichment as explicitly out of scope for this phase (it wasn't part of REUNI-01/02/03's requirement text either) unless CONTEXT.md is amended.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| `osmdroid-android` Maven artifact | `PlaceMapPicker`'s map surface | ✓ (resolvable from Maven Central, confirmed via metadata fetch this session) | `6.1.20` | — |
| Robolectric + Compose UI test infra | 4 of 5 ported tests (`DateTimePickerTest`, `PlaceMapPickerTest`, `PlaceMapOsmdroidConfigTest`, `PresetChipTest` — all `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`) | ✓ — already the established pattern on `main` (33 existing test files use this exact convention, confirmed via grep this session) | `robolectric = "4.16.1"` (already pinned in `gradle/libs.versions.toml`) | — |
| metalava Gradle plugin (`apiDump`/`apiCheck`) | Success criteria's implicit "additive api.txt" requirement | ✓ — already wired, already human-approved (`.planning/APPROVED-DEPS.md`) | `0.5.0` | — |
| Physical/emulator Android device | D-03's "confirm `MapView` actually renders on-device at Gate-1" | Not verifiable from this research session (device-only check, explicitly called out in CONTEXT.md as `ai-auto`/device-only-verifiable) | — | None — this is an acknowledged phase-completion gate, not a blocker for planning; the plan must include an explicit Gate-1 device-render check as a task, not skip it |

**Missing dependencies with no fallback:** None blocking planning. The device-render check (D-03) is a **planned verification step**, not a missing environment dependency — it cannot be resolved by research, only by execution-time device access.

**Missing dependencies with fallback:** None.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 (`junit:junit:4.13.2`) + Robolectric (`4.16.1`) + Compose UI Test (`androidx.compose.ui:ui-test-junit4`, via BOM `2026.04.01`) [VERIFIED: gradle/libs.versions.toml, read this session] |
| Config file | `build.gradle.kts` (module-root, no separate test config file) |
| Quick run command | `./gradlew testDebugUnitTest --tests "*DateTimePickerTest*" --tests "*PlaceMapPickerTest*" --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*" --tests "*PresetChipTest*"` |
| Full suite command | `./gradlew testDebugUnitTest` (includes both drift guards + all 5 ported tests + every existing test) |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| REUNI-01 | `DateTimePicker` renders + behaves correctly | unit (Robolectric Compose UI) | `./gradlew testDebugUnitTest --tests "*DateTimePickerTest*"` | ✅ (restore from `v1.13.0`) |
| REUNI-02 | `PlaceMapPicker` cluster renders + behaves correctly | unit (Robolectric Compose UI + plain JUnit for the model) | `./gradlew testDebugUnitTest --tests "*PlaceMapPickerTest*" --tests "*PlaceMapOsmdroidConfigTest*" --tests "*PlaceMapPickerModelTest*"` | ✅ (restore from `v1.13.0`) |
| REUNI-03 | `PresetChip` (both overloads) renders + behaves correctly | unit (Robolectric Compose UI) | `./gradlew testDebugUnitTest --tests "*PresetChipTest*"` | ✅ (restore from `v1.13.0`) |
| REUNI-04 | osmdroid admitted + drift guards green | unit (source-text scan guards) + manual doc edit | `./gradlew testDebugUnitTest --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*"` | ✅ (guards exist; new allowlist entries are the task) |
| (all) | Gallery renders the 3 composables | manual/device (Gate-1) | Launch `ExplorerActivity`, navigate to Pickers/Chips families | ❌ — device-only, not automatable from this research |
| (all) | Net-additive public API | build-time gate | `./gradlew apiDump && ./gradlew apiCheck` | ✅ (metalava already wired; `api.txt` confirmed to contain none of the 3 new symbols today) |
| (all) | detekt zero-baseline | build-time gate | `./gradlew detekt` | ✅ (already wired) |

### Sampling Rate
- **Per task commit:** Run the specific ported test(s) for whichever file was just restored, plus `./gradlew detekt` on the touched files.
- **Per wave merge:** `./gradlew testDebugUnitTest` (full suite, both drift guards).
- **Phase gate:** Full suite green + `apiCheck` green + `publishReleasePublicationToMavenLocal` succeeds + device Gate-1 gallery render confirmed, before `/gsd-verify-work`.

### Wave 0 Gaps
None — existing test infrastructure (Robolectric + Compose UI Test, already configured for `sdk=[35]` across 33 files) and existing governance tests (both drift guards) fully cover this phase's requirements. The 5 test files themselves are restored, not authored fresh.

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | No | N/A — library has no auth surface |
| V3 Session Management | No | N/A |
| V4 Access Control | No | N/A — library-internal, no access boundaries |
| V5 Input Validation | Partial | `PlaceMapPickerModel.kt`'s pure-math functions (distance/bearing/radius clamping) already validate ranges internally (existing, tested behavior — not new to this phase) |
| V6 Cryptography | No | N/A — no crypto surface introduced |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Unbounded/bulk tile pre-fetching abusing the OSM tile server (a real OSM Foundation tile-usage-policy concern for any osmdroid integrator) | Denial of Service (against the *upstream* tile server, not this app) | `configureOsmdroid`'s own design already addresses this — it calls no bulk-download/cache-manager API, sets a proper identifying User-Agent, and leaves cache-expiry-header honoring at osmdroid's default [VERIFIED: PlaceMapOsmdroidConfig.kt KDoc + code, read this session]. No new mitigation needed — this phase restores, not authors, this control. |
| Storage-permission overreach for tile caching | Information Disclosure / unnecessary permission grant | Tiles cache to the consumer app's private `cacheDir` (`File(appContext.cacheDir, "osmdroid")`), never external/shared storage — no storage permission is required. Consumer must still declare `INTERNET` (network access for tile fetch), which is the hub's documented cross-tier responsibility split (see Architectural Responsibility Map above). |

## Sources

### Primary (HIGH confidence)
- Direct git tag inspection (`git show v1.13.0:<path>`, `git ls-tree -r v1.13.0`, `git diff v1.13.0 main -- <path>`) — this repo, this session, for all 6 component/model files, 5 test files, `theme/Dimens.kt`, `ChipBar.kt`
- Direct file reads (`Read` tool) this session: `ComponentRegistry.kt`, `DomainVocabularyDriftGuardTest.kt`, `ComponentRegistryDriftGuardTest.kt`, `PickersFamilyScreen.kt`, `ChipsFamilyScreen.kt`, `06-CONTEXT.md`, `REQUIREMENTS.md`, `STATE.md`, the design spec, `APPROVED-DEPS.md`, `CLAUDE.md`
- Maven Central `maven-metadata.xml` for `org.osmdroid:osmdroid-android` — fetched directly this session (authoritative registry endpoint)

### Secondary (MEDIUM confidence)
- WebSearch results confirming osmdroid 6.1.20 is a genuine, actively-referenced release (dependabot bump PRs from 6.1.18→6.1.20 in third-party repos)

### Tertiary (LOW confidence)
- WebSearch "no CVE found" result for osmdroid — absence of search hits, not a queried advisory database; logged as Assumption A1, not treated as verified

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — single pre-approved dependency, version currency directly verified against Maven Central metadata this session
- Architecture: HIGH — every file/call-site claim backed by a direct `git show`/`Read` this session, including the exact `Entry` constructor shape, `ChipBar` signature compatibility, and `configureOsmdroid` call chain
- Pitfalls: HIGH — Pitfall 1 (path) and Pitfall 2 (tier field, whole-file-restore risk) are both directly evidenced by this session's git inspection, not inferred

**Research date:** 2026-09-26
**Valid until:** 30 days (stable — this is a fixed-point restore from an immutable git tag; the only decay risk is `main`'s own continued evolution of `ComponentRegistry`/drift-guard shape between now and execution, which is low-probability within a 30-day window)
