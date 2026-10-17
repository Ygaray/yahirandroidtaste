# Phase 28: TalkBack sweep & Explorer demos - Research

**Researched:** 2026-10-17
**Domain:** Jetpack Compose accessibility semantics verification (Robolectric + real-device AccessibilityService), Explorer gallery demos/theme pinning, osmdroid 6.1.20 User-Agent/tile-cache handling
**Confidence:** HIGH for code maps, guards and POL-03 mechanics (read this session, bytecode and curl verified); MEDIUM-LOW for "can TalkBack itself be driven from adb" (not touched on a device, by instruction)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P28-D-xx in prose but use the parser shape.

#### scheme-pin
- **D-01 [scheme-pin]:** per-screen helper + source-scan guard + one TESTER screencap

#### demo-stage
- **D-02 [demo-stage]:** fixed-height stage box; demo copy in ReleaseDemos.kt (guard-scanned)

#### talkback-method
- **D-03 [talkback-method]:** two layers; park spoken-only items in HUMAN-UAT-PENDING (never waive); restore TalkBack off, wm density 450, font scale

#### Runtime Decisions (POL-03, binding; copied from CONTEXT.md `## Runtime Decisions`)

- **POL-03 added (ui-designer, 2026-10-17, from teamlead@vae-bilingual / SB 999.111):** OSM user-agent foot-gun.
  - Probe from the host (curl 3/2/3.png):
    - Blocked (200 + `x-blocked`, 6987 B placeholder): `""`, and every `com.example.*` id in any form, for example `com.example.secondbrain/1.4.0 (Android 15)`.
    - Real tile: `com.ygaray.secondbrain`, bare or `/1.4.0 (Android 15)`.
  - So the fallback helps only real ids, and it must be loud otherwise.
  - Fix scope: internal only in `component/PlaceMapOsmdroidConfig.kt` (resolver + builder + one `Log.w`) and the `PlaceMapPicker` KDoc. No API change.
  - `x-blocked` response detection only if cheap (osmdroid's downloader is internal); otherwise it goes in the backlog.
  - Verification must re-run the curl probe with the exact resolved strings.
- **POL-03 scope change (2026-10-17, teamlead@vae-bilingual from SB 181.1 Gate-1 FAIL, verified by ui-designer in the osmdroid 6.1.20 bytecode):**
  - Why the current code fails:
    - `TileDownloader` checks `TileSourcePolicy.normalizesUserAgent()`. MAPNIK normalizes, so the `User-Agent` header is `Configuration.getNormalizedUserAgent()` (`mNormalizedUserAgent`, `<packageName>/<versionCode>`, for example `com.example.secondbrain/1`, which is blocked).
    - `userAgentValue` is ignored for MAPNIK, so today's `configureOsmdroid` setting of `userAgentValue` has NO effect on the wire.
    - `TileDownloader` then applies `getAdditionalHttpRequestProperties()` with `setRequestProperty` AFTER the UA header, so a `"User-Agent"` entry there wins.
  - Fix in `configureOsmdroid` (internal, no API change):
    - After `load()`, put the resolved caller UA into `Configuration.getInstance().additionalHttpRequestProperties["User-Agent"]`. Keep setting `userAgentValue` too, for non-normalizing sources.
    - SB compatibility: SB already sets the same map key app-side. Use the same header and key. If a non-blank `User-Agent` entry already exists, decide the precedence explicitly and pin it with a test. Proposed: the caller's `userAgent` param wins (it is the hub's documented input; SB passes the same value). Never add a second header.
    - Check whether `load()` clears `additionalHttpRequestProperties`, and set the entry after `load()` either way.
  - Keep the blank / `com.example` `Log.w`. Note that the fallback built from a `com.example` id is still blocked.
  - Stale cache: a blocked fetch re-stamps a 7-day expiry on the cached 6987-byte placeholder. SHOULD: when the resolved UA differs from the last one stored in the hub's osmdroid prefs, clear the tile cache once, so fixed apps don't show blocked tiles for a week. A cache purge is not a bulk download, so it is policy-safe. *(superseded by the amendment below)*
  - Consumers: only SecondBrain uses PlaceMapPicker today (CalTracker checked: no use).
  - Verification MUST check the wire, not just a host curl:
    - On the TESTER, render the picker in the Explorer with a real-id UA, then pull the cached tile (or capture the request) and assert its md5/size is not the 6987-byte placeholder and that no `x-blocked` placeholder is cached.
    - Plus a unit/Robolectric test that `additionalHttpRequestProperties["User-Agent"]` equals the resolved UA after `configureOsmdroid`.
    - The host curl matrix stays as a supporting check.
- **POL-03 cache purge amended (teamlead, 2026-10-17):** do NOT key the purge on "resolved UA changed". A UA carries versionName, so every app update would wipe the cache and cause needless re-downloads, which the tile policy discourages. Supersedes the "clear the tile cache once" line above.
  - Purge only placeholder rows: cached tiles whose bytes match the blocked PNG (size 6987 B plus its md5, pinned as a constant from the probe).
  - When: once per install (flag in the hub's osmdroid prefs), and again whenever a placeholder is detected.
  - Same approach as SB's contingency, so the two don't conflict.
  - Placeholder fingerprint (probe 2026-10-17, blank UA, 3/2/3.png): 6987 bytes, md5 `c069a15b2cc2d6b6f527ad09eb93c61a`. Size AND md5 must match before a row is purged. If OSM changes the image, the purge just misses (fail-safe), and the Log.w still fires.
- **POL-03 load() detail (teamlead, from SB's javap check, 2026-10-17):** `DefaultConfigurationProvider.load()` clears `additionalHttpRequestProperties` on every non-first load, then refills it from the prefs keys `osmdroid.additionalHttpRequestProperty.*`. `configureOsmdroid` runs `load()` on every MapView creation, so:
  - Set `additionalHttpRequestProperties["User-Agent"]` in memory AFTER EVERY `load()`, inside `configureOsmdroid` on each call, never once per process.
  - The hub does NOT write the `osmdroid.additionalHttpRequestProperty.User-Agent` prefs key. SB persists that key in the hub's prefs file; leaving it alone keeps one owner per storage slot.
  - Precedence (pin with a Robolectric test): `load()` restores SB's persisted value, then the hub overwrites it in memory with the resolved caller `userAgent`. The caller param wins; SB passes the same string, so the wire value is identical either way.
  - Test cases: header present after a first and a second `configureOsmdroid` call; a pre-seeded prefs value is overwritten in memory by the caller UA; exactly one User-Agent entry.

### Claude's Discretion
- Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

### Deferred Ideas (OUT OF SCOPE)
999.4 voice-surface polish -> v2.8.1; combined "Undo & open"; share/MCP deep links; SB sheet-swap blink (app-side).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| A11Y-02 | TalkBack sweep: clickable ReceiptRow = one button + expanded/collapsed state, Undo separate stop; refused-undo Assertive live region spoken; saved-key summary one item with Update/Clear separate; Undo Center group header is a heading; new sheet target rows and snackbar cards announce their open label | Section "A11Y-02 sweep map" (per-item current semantics, existing Layer-1 tests, gaps, candidate defects), "Gate-1 method" (real AccessibilityService dump + what must be parked) |
| DEMO-02 | Whole Explorer in pinned hub scheme; hoisted `sheetState` demo closes via `hide()` | "Explorer theme-site map" (14 sites with lines), "ExplorerTheme helper + source-scan guard", "Hoisted sheetState demo (verify)" |
| DEMO-03 | Stacked-snackbar + go-to-item demos EN+DE, each reached by a registry render test; every new public composable from Phases 25-27 registered/allowlisted; registry + CATALOG guards green | "Demo placement map", "Registry status of Phase 25-27 public composables", render-test pattern, guard interactions |
| POL-03 | Caller UA reaches the wire; blank/com.example warns once; placeholder rows purged; TESTER wire check; curl probe | "POL-03 mechanics" (bytecode-verified), test design, wire-level method |
</phase_requirements>

## Summary

Phase 28 is a verification-and-demo phase. It adds **no public API**: `api.txt` must stay byte-identical and every change is internal to `explorer/` plus one internal file (`component/PlaceMapOsmdroidConfig.kt`) and a KDoc. Four workstreams share almost no code, so they can be planned as separate plans: (1) POL-03 osmdroid UA + placeholder purge, (2) Explorer scheme pin + hoisted-sheet demo test, (3) new EN/DE Explorer demos + registry render tests, (4) the A11Y-02 sweep (Robolectric layer, then a TESTER layer) with defect fixes, ending in parked items for Yahir.

Findings that change the plan: (a) the Explorer already has a stacked-snackbar demo (English only, 240 dp stage, no render test) and **no** go-to-item demos at all; (b) the hoisted `sheetState` demo calls `hide()` correctly but **no test and no demo tag exercises it**; (c) 13 `YahirAndroidTasteTheme(themeMode = ...)` sites in `explorer/` plus the Activity wrapper keep dynamic color, and the `detail/{component}` not-found fallback is not wrapped by any screen theme; (d) the Undo Center group-header `heading()` has **no Robolectric assertion** anywhere; (e) POL-03: today's `userAgentValue` assignment is dead for MAPNIK (bytecode confirmed), and the Explorer picker passes `LocalContext.current.packageName` as its UA, so the planned TESTER wire check only discriminates the fix if the harness uses a `com.example.*` application id (normalized UA blocked) while the Explorer passes a hub-owned real UA; (f) Gate-1 on this repo already has a proven way to see what TalkBack's framework sees (a throwaway harness `AccessibilityService` dumping `AccessibilityNodeInfo`), but nothing in the repo shows TalkBack's **speech** can be captured, so spoken-only items must be parked, never waived.

**Primary recommendation:** Plan five plans in this order: 28-01 POL-03 (component/ only + 2 UA constants), 28-02 scheme pin + hoisted-demo test (explorer/ theme sites), 28-03 EN/DE demos + registry render tests (FeedbackFamilyScreen, VoiceCommandFamilyScreen, ReleaseDemos, guard widening), 28-04 A11Y-02 Layer-1 sweep test + defect fixes, 28-05 Gate-1 TESTER sweep (AccessibilityService layer) + wire check + parking of spoken-only items. 28-02 and 28-03 must not run in parallel (both touch `ReleaseDemos.kt` / family screens); 28-01 touches `PickersFamilyScreen.kt` (UA constant), so keep it in a different wave from 28-02.

## Project Constraints (from CLAUDE.md)

Extracted from `/home/yahir/Projects/Reusable/android/yahirandroidtaste/CLAUDE.md` and `.claude/CLAUDE.md` (pointer only) and the user's global `~/.claude/CLAUDE.md`:

- **One-way dependency, no secrets, no domain assumptions.** Library imports no consumer code. `LibraryInvariantsGuardTest` enforces: import prefixes (`androidx.`, `android.`, `kotlin.`, `kotlinx.`, `java.`, `javax.`, `dagger.`, `coil3.`, `org.osmdroid.`, `sh.calvin.reorderable.`, own root), no `second`+`brain` / `cal`+`tracker` identifiers or `com.example.second` / `com.cal`+`tracker` package fragments in comment-stripped main code, and `ReleaseDemos.kt` holds no consumer noun, no domain noun (calorie/meal/macro/grocery/mindmap, assembled from fragments) and no `sk-` key prefix.
- **Bindings-only Hilt**: never add `@HiltAndroidApp` / `@AndroidEntryPoint`.
- **`ComponentRegistry` single source of truth + drift guard**: every public top-level `@Composable` in visual packages registered XOR in `INTENTIONALLY_UNREGISTERED`.
- **Source-compatible, not binary-compatible**: append defaulted params, trailing lambdas last, no new `@Deprecated(HIDDEN)` overloads. Phase 28 should change **no** public signature; `api.txt` must not change.
- **Detekt zero-baseline**: do not regenerate a baseline; `LongMethod` threshold 240 (compose override), `LongParameterList` ignores defaults.
- **Gradle**: no module prefix (`./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew apiCheck`). **Never `./gradlew --stop`** on the shared host; use `--no-daemon`, never `--offline`, force fresh runs for gate evidence (`--rerun` / `--rerun-tasks`, `--no-build-cache`).
- **Shipping is human-gated**: no tag, no push, no consumer repin from this phase (Phase 29 proposes `v2.8.0`).
- **Device work**: TESTER only (`adb -s R5CT10XNKQN`), one agent device-tester at a time, hold `device:tester` + `build` leases, never drive while Yahir is testing, restore `wm density 450` (never `wm density reset`), font scale, `enabled_accessibility_services`; airplane mode is allowed on the tester only and drops wifi (irrelevant here; the wire check needs wifi).
- **Working-tree debris stays out of commits**: `.planning/graphs/*`, `state.json`, `config.json`, `.gsd-stage-*` markers, `.gate/` dirs, abandoned `uat-evidence` dirs, `WAITING.json`, `metrics/`.
- **Commits that re-signature a composable** need `HUB_LANE_OVERRIDE=2`; Phase 28 should not need it (no signature change). Verify with `bash tools/verify-api-source-additive.sh v2.7.1`.
- **Long localized labels**: caller-overridable labels need a long German/Spanish ~360 dp check before layout sign-off (applies to the new German demos).
- **Global**: handing Yahir something to test means a tailnet URL via `yahir-tn`, never `localhost`; harness-agnostic core (nothing here is harness-specific); explain root cause with fixes; surface UI issues as first-class.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Semantics (role, state, heading, live region, actions) of cards/rows/sheets | Library composables (`feedback/`, `component/`) | Robolectric semantics tests | Semantics are authored in the composables; the platform bridge turns them into `AccessibilityNodeInfo` for TalkBack |
| TalkBack speech / traversal order | Device (TalkBack app) | Throwaway harness `AccessibilityService` (structural proxy) | Speech is produced by the screen reader, not the app; only the node tree it consumes is observable agentically |
| Explorer scheme pin | Explorer composables (`explorer/` per-screen wrap) | Source-scan guard test | Theme is a composition-local wrap; the guard prevents regressions |
| Demo stages (EN/DE stacked snackbar, go-to-item) | Explorer (`explorer/ReleaseDemos.kt`, family screens) | Registry render tests | Demos are Explorer-only and drift-guard denylisted; tests reach them through `ComponentRegistry.entries[...].content` |
| OSM User-Agent on the wire | `component/PlaceMapOsmdroidConfig.kt` (process-global osmdroid `Configuration`) | osmdroid `TileDownloader` (internal) | The hub owns the configuration step; the downloader applies headers; the OSM server decides block vs tile |
| Placeholder purge | App-private tile cache (`<cacheDir>/osmdroid/tiles/cache.db`, SQLite) | Hub prefs flag | Pure storage hygiene inside the app sandbox |

## Standard Stack

No new libraries. Phase 28 uses what is already pinned.

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Jetpack Compose BOM / ui-test-junit4 | BOM 2026.04.01 (ui-test 1.11.0 per 27-VALIDATION) | Semantics + render tests via `createComposeRule` | Existing infra for all 1735 tests [VERIFIED: CLAUDE.md toolchain; 27-VALIDATION.md] |
| Robolectric | 4.16.1 | JVM Android runtime for Compose and SQLite tests | [VERIFIED: gradle/libs.versions.toml `robolectric = "4.16.1"`] |
| osmdroid-android | 6.1.20 | Map tiles; `Configuration`, `TileDownloader`, `SqlTileWriter` | [VERIFIED: gradle/libs.versions.toml `osmdroid = "6.1.20"`], archived upstream, human-approved |
| JUnit | 4.13.2 | Test runner | [VERIFIED: gradle/libs.versions.toml `junit = "4.13.2"`] |
| kotlinx-coroutines-test | 1.11.0 | Virtual time | [VERIFIED: gradle/libs.versions.toml `coroutinesTest = "1.11.0"`] |

### Supporting (host tools, not dependencies)
| Tool | Version | Purpose |
|------|---------|---------|
| `sqlite3`, `python3` (3.12.3, use `python3 -I script.py` for files), `xxd`, `curl` | host | Inspect the pulled `cache.db`, probe OSM |
| `adb` | host | Tester driving (`-s R5CT10XNKQN`) |
| `yrig` | `/home/yahir/.local/bin/yrig` | `yrig lease request device:tester` / `build` (as in 27-04-SELF-UAT) |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Harness `AccessibilityService` dump (already proven in 26-05 and 27-04) | TalkBack for Developers / Axer "Latte" (third-party adb-controllable TalkBack forks) | Would add third-party APKs to the tester and a non-Samsung TalkBack; unverified on Samsung One UI; park rather than install unvetted tools |
| Per-screen `ExplorerTheme` wrap (D-01, locked) | Root-only pin in `ExplorerActivity` | Locked out by D-01; root-only misses `ExplorerEntry` embedded by a consumer and the detail fallback |

**Installation:** none.

**Version verification:** no package is added. osmdroid 6.1.20 aar was opened from the Gradle cache (`/data/yahir/.gradle/caches/modules-2/files-2.1/org.osmdroid/osmdroid-android/6.1.20/.../osmdroid-android-6.1.20.aar`) and its `classes.jar` disassembled with `javap` this session.

## Package Legitimacy Audit

No external package is installed or added in this phase (osmdroid 6.1.20 is already pinned and listed in `.planning/APPROVED-DEPS.md`). The throwaway Gate-1 harness (never committed, built in the session scratchpad) adds nothing beyond AGP/Kotlin already used by earlier Gate-1 runs.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| (none) | - | - | - | - | - | - |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none
Third-party TalkBack-control tools found by web search (`qbalsdon/talkback`, `qbalsdon/accessibility_broadcast_dev`, `noidsirius/Axer`) are `[ASSUMED]` unvetted and are NOT recommended for install on the tester.

## Architecture Patterns

### System Architecture Diagram

```
                    +----------------------- Phase 28 -----------------------+
 POL-03             |                                                         |
 PlaceMapPicker --> createPlaceMapView --> configureOsmdroid(ctx, userAgent)  |
   (explorer demo        |                    |                               |
    passes hub UA)       |                    +-> Configuration.load(ctx,prefs)  (clears additionalHttpRequestProperties,
                         |                    |                                   refills from osmdroid.additionalHttpRequestProperty.*)
                         |                    +-> resolveUA(userAgent, pkg, versionName, release)
                         |                    +-> userAgentValue = resolved ; additionalHttpRequestProperties["User-Agent"] = resolved
                         |                    +-> base/tile cache dirs (cacheDir/osmdroid[/tiles])
                         |                    +-> Log.w once if blank-derived or com.example.*
                         |                    +-> purgePlaceholderTiles(cache.db)  (length=6987 AND md5 match -> DELETE row)
                         v
                    MapView -> TileDownloader: header(getUserAgentHttpHeader = normalized UA)
                                               THEN additionalHttpRequestProperties (setRequestProperty) -> WINS
                                  -> tile.openstreetmap.org -> real tile | 200 + x-blocked + 6987 B PNG
                    SqlTileWriter -> <cacheDir>/osmdroid/tiles/cache.db  (table tiles)

 DEMO-02/03         ExplorerActivity / ExplorerEntry (NavHost)
                      |-> index, tokens, 10 family screens, detail   each: ExplorerTheme(themeMode){...}  (dynamicColor=false)
                      |-> detail/{component} not-found EmptyState   also wrapped
                    registry entries -> content lambdas -> demos in ReleaseDemos.kt + family files
                      FeedbackStackHost entry : EN stage + DE stage (own FeedbackStackState each, "Opened: key" readout)
                      OutcomeSheet entry      : go-to-item Success single / batch mixed (EN, DE) + hoisted sheetState demo (tagged)
                      UndoCenterScreen entry  : go-to-item rows (EN, DE) via the onOpenTarget overload
                    tests: <Entry>RegistryRenderTest (Robolectric) + source-scan guards (plain JVM)

 A11Y-02            Layer 1: Robolectric semantics assertions (SemanticsProperties.* on merged/unmerged tree)
                    Layer 2a: TESTER harness AccessibilityService -> AccessibilityNodeInfo dump + performAction (what TalkBack consumes)
                    Layer 2b: spoken output / traversal feel -> parked in HUMAN-UAT-PENDING + .planning/uat-pending/28-*.md
```

### Recommended Project Structure (additions only)
```
src/main/java/io/github/ygaray/yahirandroidtaste/
  explorer/ExplorerTheme.kt          # internal fun ExplorerTheme(themeMode, content) = YahirAndroidTasteTheme(themeMode, dynamicColor = false, content)
  explorer/ReleaseDemos.kt           # + stacked-snackbar EN/DE stage, go-to-item demos, DEMO_TAG_* constants (guard-scanned)
  component/PlaceMapOsmdroidConfig.kt# + UA resolver/builder, header injection, Log.w, placeholder purge (all internal)
src/test/java/io/github/ygaray/yahirandroidtaste/
  explorer/ExplorerSchemePinGuardTest.kt            # plain JVM source scan
  explorer/FeedbackStackDemoRegistryRenderTest.kt   # EN + DE stage, burst, swipe, open readout
  explorer/GoToItemDemosRegistryRenderTest.kt       # Success single, batch mixed, Undo Center rows, EN + DE
  explorer/HoistedSheetDemoRegistryRenderTest.kt    # Show -> Log it -> hide() -> gone
  component/PlaceMapOsmdroidConfigTest.kt           # extend (existing 122 lines)
  component/PlaceMapTilePurgeTest.kt                # Robolectric real SQLite
  component/A11ySweepSemanticsTest.kt (or feedback/ + component/ split)  # one named Layer-1 command for A11Y-02
```

### Pattern 1: Per-screen theme helper + source-scan guard (D-01)
**What:** one internal helper pins the hub scheme; every `YahirAndroidTasteTheme(` occurrence in comment-stripped `explorer/*.kt` must be either inside the helper file or pass `dynamicColor = false`.
**When to use:** all 13 screen wraps, the Activity wrap, and the detail not-found fallback.
**Example:**
```kotlin
// Source: pattern from theme/Theme.kt:91-96 (signature) and explorer/ReleaseDemos.kt:292-300 (HubSchemeScope)
@Composable
internal fun ExplorerTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    YahirAndroidTasteTheme(themeMode = themeMode, dynamicColor = false, content = content)
}
```
Keep `HubSchemeScope` (an existing test `HubSchemeScope resolves the hub static scheme under a foreign light or dark outer scheme` in `ReceiptDemosRegistryRenderTest.kt` calls it). After the pin it is redundant in-app but harmless; update its KDoc (it still says "Every other Explorer page keeps dynamic color").

### Pattern 2: Registry render test (copy `UndoGroupDemoRegistryRenderTest.kt` / `ReceiptDemosRegistryRenderTest.kt`)
`@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35], qualifiers = "w360dp-h3000dp")`, `@GraphicsMode(GraphicsMode.Mode.NATIVE)`, `createComposeRule()`, render `checkNotNull(ComponentRegistry.entries.first { it.name == "<Entry>" }.content)` inside `YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) { Column(Modifier.verticalScroll(rememberScrollState())) { content() } }`, query by explorer-only `demo_*` tags (literals in the test), scope with `hasAnyAncestor(hasTestTag(demo))`. No pixel capture (times out under Robolectric, per `testsupport/SemanticsDump.kt` header). German 360 dp bounds via `getUnclippedBoundsInRoot()` / `positionInRoot` as in the Undo group test.

### Pattern 3: Source-contract test with `SourceContractTestSupport.stripComments`
Used by `LibraryInvariantsGuardTest`, `OpenTargetHygieneSourceContractTest`; plain JVM, vacuity assertions first (non-empty file list). Reuse for the scheme-pin guard.

### Anti-Patterns to Avoid
- **Root-only pin** (`ExplorerActivity` only): `ExplorerEntry` is public and embeddable; the nav screens re-wrap themselves with the toggle's `themeMode`, so a root pin is overridden by every screen. Locked out by D-01.
- **Adding the new demos' copy outside `ReleaseDemos.kt` without widening the guard**: `LibraryInvariantsGuardTest` scans only that one file for consumer/domain nouns (`LibraryInvariantsGuardTest.kt:136`). Either keep copy there or change the guard to glob `explorer/*Demos*.kt` (with a vacuity check).
- **Re-pinning with `HubSchemeScope` inside screens**: it derives light/dark from surface luminance and cannot honor the toggle on a foreign scheme; use `ExplorerTheme(themeMode)`.
- **Keying the placeholder purge on UA change** (explicitly superseded in CONTEXT).
- **Deleting rows through `SqlTileWriter.purgeCache()`** or any `CacheManager` API: wipes everything / violates the hub's "no cache-manager API" rule asserted in `PlaceMapPickerTest` (`CacheManager` must not appear in `PlaceMapPicker.kt`; expiration overrides must not appear in `PlaceMapOsmdroidConfig.kt`).

## File:line maps

### Explorer theme-site map (all `YahirAndroidTasteTheme(themeMode = themeMode)` unless noted) [VERIFIED: grep this session]
| File | Line | Note |
|------|------|------|
| `explorer/ExplorerIndexScreen.kt` | 110 | index |
| `explorer/TokenBrowserScreen.kt` | 60 | color swatches use RAW `theme/Color.kt` vals, unaffected by the pin |
| `explorer/CardsFamilyScreen.kt` | 335 | |
| `explorer/ChipsFamilyScreen.kt` | 294 | |
| `explorer/SheetsFamilyScreen.kt` | 501 | |
| `explorer/ButtonsFabFamilyScreen.kt` | 182 | |
| `explorer/PickersFamilyScreen.kt` | 242 | also PlaceMapPicker UA at 354 and 400 |
| `explorer/FeedbackFamilyScreen.kt` | 186 | |
| `explorer/EmptyStateFamilyScreen.kt` | 67 | |
| `explorer/ProgressFamilyScreen.kt` | 177 | |
| `explorer/TactileFoundationFamilyScreen.kt` | 123 | |
| `explorer/VoiceCommandFamilyScreen.kt` | 200 | |
| `explorer/ComponentDetailScreen.kt` | 58 | every detail page (all registry content renders here) |
| `explorer/ExplorerActivity.kt` | 21-25 | `YahirAndroidTasteTheme { ExplorerEntry(...) }` (SYSTEM + dynamic) |
| `explorer/ExplorerEntry.kt` | 153 | `detail/{component}` not-found `EmptyState(...)`: NOT inside any screen theme; wrap it |
| `explorer/ReleaseDemos.kt` | 292-300 | `HubSchemeScope` (only the sheet/tone demos at 314 and 400 use it) |

`theme/Theme.kt:91-96`: `fun YahirAndroidTasteTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, dynamicColor: Boolean = true, content: ...)`. The helper must pass `dynamicColor = false`.

### Demo placement map
| Demo | Registry entry (content fn) | File:line | Status today |
|------|----------------------------|-----------|--------------|
| Stacked snackbar (fire/burst/swipe/open) | `FeedbackStackHost` -> `FeedbackStackHostVariants()` | `FeedbackFamilyScreen.kt:132-169` entry, `306-417` demo | EN only; buttons: "Note with target", "Note with Undo and target", "Plain note", "Burst x4", "Short note", "Long note with Undo", "Dismiss all"; `Text("Last result: $lastResult, last opened: $lastOpened")`; stage `Box(...height(240.dp)...)` at 410; `burstEvents()` at 279-298; NO test tags, NO DE, NO render test |
| Stacked snackbar States cell | same entry | `FeedbackFamilyScreen.kt:136-158` | EN Indefinite note |
| Go-to-item: Success single / batch mixed | `OutcomeSheet` -> `OutcomeSheetVariants()` (+ `ReceiptOutcomeSheetDemos()`) | `VoiceCommandFamilyScreen.kt:149-166` entry, `575-660` demo; `ReleaseDemos.kt:381-408` | **absent** (no `target`, no `onOpenTarget` anywhere in `explorer/`) |
| Go-to-item: Undo Center target rows | `UndoCenterScreen` -> `UndoCenterScreenVariants()` | `FeedbackFamilyScreen.kt:80-107` entry, `228-256` demo; `ReleaseDemos.kt:194-235` group demos use the 6-arg overload | **absent** |
| Hoisted `sheetState` + `hide()` | `OutcomeSheet` | `VoiceCommandFamilyScreen.kt:668-704` | present, **untested, no tag** |
| Existing demo tags | `ReleaseDemos.kt:67-72` | `DEMO_TAG_KEY_LIFECYCLE`, `..._GERMAN`, `DEMO_TAG_UNDO_GROUPS`, `..._GERMAN`, `DEMO_TAG_COUNT_EDITOR`, `DEMO_TAG_MARK_CHECKED` | pattern to copy: `internal const val`, `demo_` prefix |

`CatalogDocsDriftGuardTest` leaves explorer-only `demo_` tags in neither API.md list (class KDoc), so new `demo_*` tags need no docs. Do NOT use `outcome_sheet_` / `receipt_row_` / `provider_key_` prefixes for new tags.

### Registry status of Phase 25-27 public composables
Derived from `git diff v2.7.1 HEAD -- src/main api.txt` this session:
| Public composable / overload | Added in | Registry status |
|------------------------------|----------|-----------------|
| `FeedbackStackHost` | 25 | Registered, `FeedbackFamilyScreen.kt:132-169` (family `FEEDBACK`, tier `PATTERN`) |
| `rememberFeedbackStackState` | 25 | Allowlisted, `ComponentRegistry.kt:127-129`: `"rememberFeedbackStackState" to "State holder remembered by callers and passed to FeedbackStackHost; non-visual, exercised indirectly via the FeedbackStackHost entry."` |
| `UndoCenterScreen` second overload (with `onOpenTarget`) | 27 | Same name as the registered entry; the guard is by name, so covered |
| `OutcomeSheet` overloads with `onOpenTarget` | 27 | Same name, registered |
| `FeedbackCard`, `FeedbackDeck`, `OpenTargetRow`, `SuccessOpenTargetRow`, `OpenTargetBatchList`, `HandledByDot`/`HandledByPillShape` helpers | 26-27 | `internal`: out of scope of the guard |
Non-composable new public types (`FeedbackTarget`, `FeedbackResult`, `FeedbackStackState`) need no registration. Counts remain **67 registered / 6 unregistered / 73** (re-counted: 67 `ComponentRegistry.Entry(` across the family files; `API.md:29-30` and `README.md:84-85` state the same). Criterion 4 is therefore verification only: run the registry/CATALOG guards, add no entries. Phase 28 must not change these counts (any new registered entry forces API.md/README count edits, which belong to Phase 29).

### A11Y-02 sweep map (current semantics, existing Layer-1 coverage, gaps)
| # | Roadmap item | Current implementation | Layer-1 coverage today | Gap / candidate defect |
|---|--------------|------------------------|------------------------|------------------------|
| 1 | Clickable `ReceiptRow`: one button + expanded/collapsed state; Undo separate stop | `ReceiptRow.kt` `ReceiptRowSummary`: `.testTag("receipt_row_summary").semantics(mergeDescendants = true) { if (onClick != null) stateDescription = if (expanded) expandedStateLabel else collapsedStateLabel }` then `Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)`; `trailingAction` is a sibling in a `Box(Modifier.testTag("receipt_row_trailing_action"))` | `ReceiptRowTest.kt` (Role at ~462, `StateDescription` at ~485-516) | Covered. Device confirm only for spoken form |
| 2 | Refused-undo "Couldn't undo" Assertive live region | `OutcomeSheet.kt:264-285`: `Surface(...).testTag("outcome_sheet_undo_refused").semantics { liveRegion = LiveRegionMode.Assertive }` with the message in a child `Text`; the Surface is NOT `mergeDescendants` | `OutcomeSheetLiveRegionTest.kt` (value Assertive, exactly one node, none without refusal) | **Candidate C-1:** a live region node whose own text is empty and whose message is a separate child node; whether TalkBack speaks child text for it is unverified. Layer 2a can show it: dump the node and the `TYPE_WINDOW_CONTENT_CHANGED` event text when the surface appears. Cheap fix if event/node text is empty: `semantics(mergeDescendants = true) { liveRegion = Assertive }` (keeps the single live node) |
| 3 | Saved-key summary: one item; Update and Clear separate buttons | `ProviderKeyCard.kt:236-298` `SavedKeySummary`: summary `Row` with `.semantics(mergeDescendants = true) {}.testTag(TAG_SAVED_SUMMARY)` (check icon `contentDescription = null`); Update/Clear `DynamicActionButton`s in a separate `Row` | `ProviderKeyCardLifecycleTest.kt:82` (`assertTextEquals("Saved · $fingerprint")`), tags present | Add an explicit assertion that Update/Clear are NOT inside the summary node (separate nodes with Role.Button) |
| 4 | Undo Center group header is a heading | `UndoCenterScreen.kt:312-337` `UndoGroupHeaderRow`: `Text(text = group.label, modifier = Modifier.semantics { heading() }, ...)`; subtitle and "Undo all" `TextButton` are separate nodes by design (KDoc: "its semantics are not merged") | **None** (grep for `Heading` finds only `OutcomeSheetReceiptFrameTest.kt:102` and `SemanticsDump` whitelist) | **Add** a Robolectric test asserting `SemanticsProperties.Heading` on the `undo-group-header-*` label node |
| 5a | Sheet target rows announce open label | `OpenTargetRow.kt:102-161`: `.semantics(mergeDescendants = true) {}` then `clickable(onClickLabel = target.openLabel.ifBlank { fallbackLabel }, role = Role.Button, ...)`; batch rows same via `openTargetClick`; in-flight: `alpha(DISABLED_CONTENT_ALPHA).semantics { disabled() }` | `OpenTargetRowTest.kt`, `OutcomeSheetOpenTargetTest.kt`, `OutcomeSheetBatchOpenTargetTest.kt` | Covered at Layer 1 |
| 5b | Undo Center openable row | `UndoCenterScreen.kt:403-465` `UndoHistoryOpenRow`: `.semantics(mergeDescendants = true) {}.combinedClickable(onClickLabel = target.openLabel.ifBlank { entry.message }, role = Role.Button, onLongClickLabel = "Preview", ...)` around a Material3 `ListItem` | `UndoCenterScreenOpenTargetTest.kt` | **Candidate C-2 (FA-7, from 27-03-SUMMARY):** `ListItem` is itself a merging node, so the row's merged node carries Role.Button + open label but NOT the message; three stops per row (row button, message, Undo). Fix options: avoid a nested merge (replace `ListItem` internals) or `clearAndSetSemantics` on the ListItem child with the message folded into the row node. Geometry must not change |
| 5c | Snackbar card | `FeedbackCard.kt:143-179`: root `semantics(mergeDescendants = true) { liveRegion = Polite; traversalIndex = index.toFloat(); dismiss(label = dismissLabel){...}; if (openTarget != null) { role = Role.Button; onClick(label = openTarget.openLabel.ifBlank { item.message }) {...} }; customActions = [Undo?, Dismiss] }` | `FeedbackStackHostSemanticsTest.kt` (16 tests) | **Candidates C-3/C-4** (both forwarded by 26-05 and 26-VERIFICATION): (C-3) the OLDEST card's separate Undo child reads outside its card in the framework `traversalBefore` chain (fanned: Undo(B2) -> B4 -> B3 -> B2; collapsed: B4 -> Undo(B2) -> B3 -> B2); (C-4) both Dismiss forms (ACTION_DISMISS and a custom action) likely appear twice in TalkBack's actions list. Both are **designer decisions** (brief D-11 locks "Undo stays a separate focusable child and is also a custom action" and D-07 says "keep both Dismiss forms and verify on device"); the sweep reports, the ui-designer seat decides |

Hardcoded English a11y labels in `UndoCenterScreen.kt` (`"Preview"` at line 453, `"Undo"` in `UndoOpenTrailing`, `"Clear history"`, `"Back"`) are a known, non-regression localization gap (27-VERIFICATION.md Info row); do not widen API in this phase; log it for v2.8.1.

### Hoisted `sheetState` demo (verify, do not rebuild)
`VoiceCommandFamilyScreen.kt:668-704` `ReceiptConfirmHandledByVariant`: `val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)`, `val slideDown: () -> Unit = { scope.launch { sheetState.hide() }.invokeOnCompletion { shown = false } }`, passed as `onConfirm = slideDown` and `onCancel = slideDown`, and `sheetState = sheetState` into `OutcomeSheet(..., style = OutcomeSheetStyle.Receipt, sheetState = sheetState)`. Library-level proof exists (`OutcomeSheetHoistedStateTest.kt`: hide on a hoisted state takes the sheet down, Classic and Receipt), but the **Explorer demo wiring** has no test and its "Show sheet" `Button` (lines 679-685) has no `testTag`. Plan: add an explorer-only tag (e.g. `DEMO_TAG_HOISTED_SHEET_SHOW = "demo_hoisted_sheet_show"` in `ReleaseDemos.kt`), then a registry render test: render the `OutcomeSheet` entry -> click the tag -> assert `outcome_sheet_confirmation_confirm` exists -> click it -> `mainClock.autoAdvance = false; advanceTimeBy(2_000)` (the same budget `OutcomeSheetHoistedStateTest` uses) -> assert `outcome_sheet_title` count 0.

## POL-03 mechanics (bytecode- and probe-verified this session)

### What `javap` on osmdroid 6.1.20 shows
- `TileDownloader`: `OnlineTileSourceBase.getTileSourcePolicy().normalizesUserAgent()`; if true `Configuration.getInstance().getNormalizedUserAgent()`; if still null `getUserAgentValue()`. Then `connection.setRequestProperty(Configuration.getUserAgentHttpHeader(), <that UA>)`, **then** a loop over `Configuration.getInstance().getAdditionalHttpRequestProperties().entrySet()` calling `setRequestProperty(key, value)`. [VERIFIED: `javap -c org.osmdroid.tileprovider.modules.TileDownloader`, offsets 12-53 and 286-368]
- `DefaultConfigurationProvider`: `userAgentHttpHeader` defaults to the literal `"User-Agent"` (`ldc "User-Agent"` / `putfield userAgentHttpHeader`); `getNormalizedUserAgent()` returns `mNormalizedUserAgent`, computed by `computeNormalizedUserAgent(Context)` as `packageName + "/" + versionCode`. [VERIFIED: `javap -c org.osmdroid.config.DefaultConfigurationProvider`]
- The private `load(SharedPreferences, Map, String)` helper begins with `Map.clear()` then refills from prefs keys with the prefix string `"osmdroid.additionalHttpRequestProperty."`. [VERIFIED: same class, `ldc_w "osmdroid.additionalHttpRequestProperty."` and `Map.clear` at the head of the helper]
- `SqlTileWriter`: `DATABASE_FILENAME = "cache.db"`, `TABLE = "tiles"`, `COLUMN_KEY = "key"`, `COLUMN_PROVIDER = "provider"`, `COLUMN_TILE = "tile"`, `COLUMN_EXPIRES = "expires"`; DDL `CREATE TABLE IF NOT EXISTS tiles (key INTEGER , provider TEXT, tile BLOB, expires INTEGER, PRIMARY KEY (key, provider));`; DB path = `Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + "cache.db"`; `mDb` is a **static** shared connection; `purgeCache()` / `purgeCache(String)` exist but wipe whole tables/providers (do not use). [VERIFIED: `javap -p -c -constants org.osmdroid.tileprovider.modules.SqlTileWriter`]

So: with `configureOsmdroid`'s `osmdroidTileCache = File(File(cacheDir, "osmdroid"), "tiles")` the file is `<app cacheDir>/osmdroid/tiles/cache.db` [VERIFIED: `PlaceMapOsmdroidConfig.kt` current body + the `cache.db` constant].

### Probe results (host, today, tile 3/2/3.png) [VERIFIED: curl this session]
| `-A` value | x-blocked | size | md5 |
|------------|-----------|------|-----|
| `""` | `Access denied. See https://operations.osmfoundation.org/policies/tiles/` | 6987 | `c069a15b2cc2d6b6f527ad09eb93c61a` |
| `com.example.secondbrain/1` | blocked | 6987 | same |
| `com.example.secondbrain/1.4.0 (Android 15)` | blocked | 6987 | same |
| `com.example.yahirandroidtasteharness/1.0 (Android 15)` | blocked | 6987 | same |
| `io.github.ygaray.yahirandroidtasteharness/1` | none | 4472 | `ce9c4e9084891d7cae5390520b1756ea` |
| `io.github.ygaray.yahirandroidtasteharness/1.0 (Android 15)` | none | 4472 | same |
| `com.ygaray.secondbrain/1.4.0 (Android 15)` | none | 4472 | same |
| `YahirAndroidTasteExplorer/2.8 (+https://github.com/Ygaray/yahirandroidtaste)` | none | 4473 | `ccb5da77a581b6e93e4aa8fa6e4409d0` |
| `org.robolectric.default` | none | 4472 | same as above |
The placeholder fingerprint in CONTEXT (6987 B, md5 `c069a15b2cc2d6b6f527ad09eb93c61a`) re-confirmed byte for byte; the file is a 256x256 8-bit PNG. Real tile bytes vary by one byte between runs (4472/4473), so never assert a real tile's md5; assert "not the placeholder" (size != 6987 or md5 != fingerprint).

### Implementation shape (internal only, no API change)
```kotlin
// Source: current PlaceMapOsmdroidConfig.kt body + bytecode facts above; names are proposals
internal fun configureOsmdroid(context: Context, userAgent: String) {
    val appContext = context.applicationContext
    val prefs = appContext.getSharedPreferences("yahirandroidtaste_osmdroid", Context.MODE_PRIVATE)
    val config = Configuration.getInstance()
    config.load(appContext, prefs)                       // clears + refills additionalHttpRequestProperties
    val resolved = resolveOsmdroidUserAgent(userAgent, appContext.packageName, versionName(appContext), Build.VERSION.RELEASE)
    config.userAgentValue = resolved                     // still set, for non-normalizing sources
    config.additionalHttpRequestProperties.keys.removeAll { it.equals("User-Agent", ignoreCase = true) }
    config.additionalHttpRequestProperties["User-Agent"] = resolved   // AFTER every load(); wins over the normalized header
    // ... base path / tile cache as today ...
    // one Log.w if blank-derived or resolved starts with "com.example."
    // purgePlaceholderTiles(File(tileCache, "cache.db"))
}
```
Rules to encode:
- Remove case-variant duplicates (`user-agent`) before the `put`: `HttpURLConnection.setRequestProperty` is case-insensitive and the map is a `HashMap`, so two case-variant keys would race by iteration order. This is what makes "exactly one User-Agent entry" true even if a consumer persisted a differently cased key. [ASSUMED: case-insensitive `setRequestProperty` is standard JDK behavior; the exactly-one-entry test is what pins it]
- Do not write `osmdroid.additionalHttpRequestProperty.User-Agent` to prefs (locked).
- Fallback: blank (after trim) -> `"<pkg>/<versionName> (Android <release>)"`; a trimmed value equal to `packageName` (a "bare package name", REQUIREMENTS wording) -> same expansion; anything else passes through trimmed. `Build.VERSION.RELEASE` gives "15" like the probe strings `(Android 15)`; `Build.VERSION.SDK_INT` would give 35. [ASSUMED: RELEASE chosen to match the `(Android 15)` examples; confirm in 28-01 discuss]
- `versionName` can be null (Robolectric default app, some installs): fall back to a literal such as `0`; never throw.
- `Log.w` (one line, constant tag, includes the OSM policy URL `https://operations.osmfoundation.org/policies/tiles/`, never the opaque coordinates or a key). Fires when the caller UA was blank, or the resolved UA starts with `com.example.` (case-insensitive). A `com.example` package falling through the blank fallback still warns.
- Existing tests that must be REWRITTEN (behavior is intentionally changing, locked by REQUIREMENTS): `resolveOsmdroidUserAgent falls back to packageName when agent is empty/blank` and `configureOsmdroid with a blank agent falls back to the app package name` (`PlaceMapOsmdroidConfigTest.kt`) currently assert the bare package name.
- The test class's `setUp/tearDown` currently snapshots only `userAgentValue`, `osmdroidBasePath`, `osmdroidTileCache`; it must also snapshot/restore `additionalHttpRequestProperties` (process-global, would leak across the suite).
- Source-contract guards that stay true: `PlaceMapOsmdroidConfig.kt` must not contain `expirationOverrideDuration` / `setExpirationOverrideDuration` (`PlaceMapPickerTest.kt:1106-1113`); `PlaceMapPicker.kt` must not contain `android.util.Log` or `CacheManager` (so put the `Log.w` and purge in the config file, not the picker). `LibraryInvariantsGuardTest` forbids `secondbrain` / `com.example.second` in comment-stripped main code: the literal `"com.example."` alone is fine; never write a consumer name in main code or its KDoc-free strings.

### Placeholder purge design
- A pure internal function over a `File`: `purgePlaceholderTiles(dbFile: File, size: Int = 6987, md5Hex: String = "c069a15b2cc2d6b6f527ad09eb93c61a"): Int`. Skip silently if the file does not exist (never create it). Open with `SQLiteDatabase.openDatabase(path, null, OPEN_READWRITE)` inside try/catch `SQLiteException`; `SELECT key, provider, tile FROM tiles WHERE length(tile) = ?` (bound arg), compute MD5 with `java.security.MessageDigest` (fingerprint, not security), `DELETE FROM tiles WHERE key = ? AND provider = ?` (parameterized) only when size AND md5 match. Size-only matches are never purged.
- Trigger: once per install via a boolean in the existing prefs file (`yahirandroidtaste_osmdroid`, new key owned by the hub), plus "whenever a placeholder is detected". The downloader is internal, so detection can only mean "found by this scan". Recommended: also scan on the first `configureOsmdroid` of each process (static `AtomicBoolean`); the `length(tile)` filter reads record headers, not payloads, so it is cheap [ASSUMED: SQLite `length()` on a BLOB is derived from the record header], and run it off the main thread or accept one short synchronous scan (planner call; keep the pure function synchronous for tests).
- The static `SqlTileWriter.mDb` may already hold the file open in the same process; a second `SQLiteDatabase` connection is fine (SQLite file locking, Android's default busy handling). Wrap everything best-effort: a purge failure must never break map creation.
- `x-blocked` detection in the downloader: not cheap (osmdroid's `MapTileDownloader`/`TileDownloader` are not hookable without subclassing). Put it in the backlog as CONTEXT allows.
- Test with real SQLite under Robolectric 4.16.1 (`SQLiteDatabase.openOrCreateDatabase(File, null)` + the exact DDL above). Prefer a binary fixture of the real placeholder PNG (`curl -s -A "" -o src/test/resources/... https://tile.openstreetmap.org/3/2/3.png`, then verify `md5sum` equals the fingerprint) so the pinned constant is proven against real bytes; keep a second test with a size-match/md5-mismatch row that must survive, and a real-tile-sized row that must survive.

### Explorer picker UA and the wire-level check
`PickersFamilyScreen.kt:354` and `:400` pass `userAgent = LocalContext.current.packageName`. On the tester the harness id (`io.github.ygaray.yahirandroidtasteharness`) is already accepted by OSM (probe above), so a wire check with that id would pass **before and after** the fix and prove nothing. To discriminate:
1. Change both Explorer call sites to a hub-owned, real, descriptive constant (for example `YahirAndroidTasteExplorer/<ver> (+https://github.com/Ygaray/yahirandroidtaste)`; probed: real tile). Keep it out of `ReleaseDemos.kt` consumer-noun scanning concerns (no consumer names).
2. Build the Gate-1 harness with `applicationId = "com.example.<something>"` so the normalized header (`<pkg>/<versionCode>`) is blocked, and (as in Phase 27) a baseline harness over the `v2.7.1` AAR as a control: baseline caches the 6987-byte placeholder, HEAD caches a real tile.
3. The harness manifest must declare `android.permission.INTERNET` (the library declares none; `PlaceMapPicker.kt:128` says the host owns it). The earlier `p27` harness manifest has no INTERNET permission: do not reuse it unchanged.
4. Pull the DB over USB: `adb -s R5CT10XNKQN exec-out run-as <harness-id> cat cache/osmdroid/tiles/cache.db > <host-file>` (debuggable harness; copy `-wal`/`-shm` too if present), then `sqlite3 <file> "select key, provider, length(tile), expires from tiles"` and compute md5 of a blob with a **script file** (`python3 -I <script>` or `sqlite3 ... "select hex(tile) ..." | xxd -r -p | md5sum`); earlier Gate-1 runs had inline `python3 -c` and shell `for` loops refused by the command guard (`uninspectable`), so use literal commands and script files.
5. Assert: no row with `length(tile) = 6987` and md5 `c069a15b2cc2d6b6f527ad09eb93c61a`; at least one real tile row; the control run shows the placeholder.

## Gate-1 method (agentic vs parked) for A11Y-02

Established, repo-proven method [VERIFIED: 26-05-SELF-UAT.md and 27-04-SELF-UAT.md]:
- Publish HEAD to mavenLocal after deleting the prior artifact (`AGENT-DEVICE-TESTING.md` still shows `1.10.0`; both Gate-1 logs use the same hardcoded coordinate `com.github.Ygaray:yahirandroidtaste:1.10.0`; the coordinate does not bump per phase), build a throwaway harness (`P27Activity`-style consumer + `DeckA11yService` observer + a `MainActivity` into the Explorer) in the session scratchpad, never in the repo. Lease `device:tester` and `build` with `yrig lease request`. Use only the USB serial.
- Device state changed and always restored, recorded in an `arrange-and-restore` record: `font_scale` (1.0 -> 2.0 -> 1.0), `wm density` (450 -> 480 so 1080 px is exactly 360 dp -> **450**, never `wm density reset`), secure `enabled_accessibility_services` and `accessibility_enabled` (null/0 -> service -> null/0), harness uninstalled.
- A prior harness with the observer service exists in this session's scratchpad (`.../scratchpad/p27/harness/app/src/main/java/io/github/ygaray/yahirandroidtasteharness/DeckA11yService.kt`, 165 lines). It logs per node: class, text, content-desc, bounds, `isClickable`, `isScreenReaderFocusable`, `liveRegion`, `isDismissable`, roleDescription extra, `traversalBefore/After`, the full `actionList` (id:label), plus `AccessibilityEvent`s of type content-changed/announcement/accessibility-focused with `contentChangeTypes`, `text`, source live region. **It does not log `isHeading` (API 28), `stateDescription` (API 30) or `paneTitle`; extend `walk()` with `heading=${n.isHeading}`, `state=${n.stateDescription}` before using it for items 1 and 4.**
- `uiautomator dump` cannot see `heading`, `liveRegion` or TalkBack speech (stated in 22-05, 23-02 and 24-03 SELF-UATs), so the observer service is the only agentic instrument for those.

### What can be proven where
| Item | Layer 1 Robolectric | Layer 2a TESTER AccessibilityService dump / actions | Physical TalkBack (park) |
|------|--------------------|---------------------------------------------------|--------------------------|
| ReceiptRow button + state | `Role`, `StateDescription` | class `android.widget.Button`, `state=Expanded/Collapsed`, click label in `actionList`, tap toggles | Spoken "button, expanded" phrase and order |
| Undo separate stop | trailingAction sibling node | Undo is its own `a11yFocusable` node | Swipe order |
| Refused live region | `LiveRegion == Assertive`, one node | `liveRegion=2` on the surface node; the `TYPE_WINDOW_CONTENT_CHANGED` event and its `text` when the refused surface appears (settles C-1 structurally) | That it is actually spoken, and spoken in full |
| Saved-key summary | merged text node + separate buttons | one focusable node with text `Saved · key-...`, Update and Clear separate Buttons | Spoken read-out |
| Group header heading | `Heading` (to add) | `isHeading=true` on the label node | "heading" announcement, heading-navigation |
| Sheet target rows / batch rows / Undo Center row / card open label | `Role.Button`, click label | `Open <label>` in `actionList`, Button class, merged text | Spoken label, 3-stop feel of Undo Center rows (C-2) |
| Card reading order, duplicate Dismiss | traversalIndex asserted | `traversalBefore` chain, `actionList` shows `1048576:Dismiss` plus custom `Dismiss` | How TalkBack lists the two Dismiss forms (C-4), oldest-card Undo order (C-3) |

### Could TalkBack itself be driven? [ASSUMED / LOW, not tested; instruction forbids touching devices]
- Enabling is a settings write (`adb shell settings put secure enabled_accessibility_services <pkg>/<service>`), the same mechanism the harness service already uses; the Samsung build on a One UI device is likely `com.samsung.android.accessibility.talkback` (Samsung fork) alongside Google's `com.google.android.marvin.talkback` [ASSUMED: package names from training/web; confirm with `pm list packages | grep -i talkback` during the Gate-1 preflight].
- Raw `adb shell input tap/swipe` goes through TalkBack's touch exploration (a tap only moves focus; a double tap is needed to activate); the community consensus is that gestures and next/previous cannot be issued by plain adb and need an `AccessibilityService` that dispatches gestures, or a TalkBack fork that exposes adb broadcasts [CITED: https://github.com/qbalsdon/talkback ; https://qbalsdon.github.io/android/kotlin/accessibility/adb/2021/04/15/accessibility-service.html ; https://stackoverflow.com/questions/66790141/adb-accessibility-focus-change]. Those tools are third-party and unvetted here.
- TalkBack speech is not exposed to adb in any source found; screen-read of its "Display speech output" overlay would be a screencap, which only works if that developer option can be enabled without touch.
- Recommendation (consistent with D-03): do Layer 1 + Layer 2a agentically; attempt TalkBack on the tester only as a time-boxed smoke (enable, one `uiautomator`/screencap, disable). If speech cannot be captured, park each spoken-only item with the exact physical steps in `.planning/uat-pending/28-talkback-sweep-explorer-demos.md` and a `### Phase 28 - ...` entry in `HUMAN-UAT-PENDING.md` (same format as the Phase 26/27 entries: Status `pending`, Gate-1 log link, Items covered, Owner how-to-verify, Note). Never waive, never edit artifacts to clear a gap (memory: "Certify parks agentic gaps"). When the sweep is parked, the v2.6 `uat_gaps` rows (21/22/23/24 TalkBack) in STATE.md `Deferred Items` stay open.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Reading what TalkBack consumes | A custom accessibility tree parser | The existing harness `AccessibilityService` pattern (`DeckA11yService`) + `AccessibilityNodeInfo` fields | Proven twice in this repo; `uiautomator` hides heading/live region |
| Source scanning for the pin/guards | New regex scanning infrastructure | `SourceContractTestSupport.stripComments` / `source` / `functionBody` and the `resolveModuleSourceRoot` + vacuity pattern in `ComponentRegistryDriftGuardTest` | Already handles comments, source-root resolution and vacuity |
| Semantics dumps in tests | Ad hoc printlns | `testsupport/SemanticsDump.kt` (`normalizedTree`, `rawTree`) | Deterministic whitelist form already includes Heading, LiveRegion, Role, StateDescription |
| Deleting placeholder tiles | `SqlTileWriter.purgeCache()` / `CacheManager` | A parameterized `DELETE ... WHERE key=? AND provider=?` gated by length AND md5 | Whole-cache wipes cause re-downloads the tile policy discourages; `CacheManager` is guard-banned in the picker |
| HTTP header on osmdroid | A custom `ITileSource` / downloader subclass | `Configuration.additionalHttpRequestProperties["User-Agent"]` | Applied after the normalized header by `TileDownloader` (bytecode-verified) |
| Registry render harness | New Compose test scaffolding | Copy `UndoGroupDemoRegistryRenderTest.kt` / `ReceiptDemosRegistryRenderTest.kt` | Same Robolectric config, tags, 360 dp bounds idioms |
| Swipe/hold gestures on cards in tests | New gesture helpers | Patterns in `FeedbackStackHostSwipeTest.kt` / `FeedbackStackHostDeckTest.kt` (`tapStrip`, clock advance helpers in `FeedbackStackTestSupport.kt`) | Compose + Robolectric timing already solved |

**Key insight:** every instrument this phase needs already exists in the repo; the work is wiring them to the new surfaces and being honest about which claims are structural (provable) versus spoken (physical).

## Common Pitfalls

### Pitfall 1: Pinning only the screen wraps
**What goes wrong:** the `detail/{component}` not-found `EmptyState` (`ExplorerEntry.kt:153`) and a consumer-embedded `ExplorerEntry` remain dynamic.
**Why:** that branch is outside every screen's `YahirAndroidTasteTheme`.
**Avoid:** wrap it in `ExplorerTheme(themeMode)` and make the guard fail on any `YahirAndroidTasteTheme(` outside the helper file in `explorer/*.kt`.
**Warning sign:** the guard passes while `ExplorerActivity` still has a bare `YahirAndroidTasteTheme {`.

### Pitfall 2: A wire-level check that cannot fail
**What goes wrong:** the harness id is a real id, so OSM serves a real tile with or without the fix.
**Avoid:** `com.example.*` harness application id + hub-owned real UA from the Explorer + v2.7.1 baseline control (see "Explorer picker UA and the wire-level check").

### Pitfall 3: Forgetting `INTERNET` in the harness
The library declares no permission; the picker silently shows gray tiles. Declare it in the throwaway harness manifest.

### Pitfall 4: `load()` ordering and map clearing
`load()` runs on every MapView creation and clears the map; set the header after every `load()` (CONTEXT). Seed a prefs value `osmdroid.additionalHttpRequestProperty.User-Agent` before the call in the test and assert the in-memory value is the caller UA, one entry, and that the prefs value was not rewritten.

### Pitfall 5: Process-global osmdroid state leaking between tests
Snapshot and restore `additionalHttpRequestProperties`, `userAgentValue`, `osmdroidBasePath`, `osmdroidTileCache` in `@Before/@After`; reset any process-static "scanned already" flag the purge adds (expose an internal reset for tests).

### Pitfall 6: Guard collisions when adding demos
`LibraryInvariantsGuardTest` rejects `calorie`, `meal`, `macro`, `grocery`, `mindmap` substrings and `sk-` key prefixes in `ReleaseDemos.kt`, and forbids consumer names anywhere in main code. Use neutral copy (`Item A`, `Batch edit`, `Open item`). German strings are fine but scan substrings (`meal` can hide in longer words).

### Pitfall 7: Stage too small to fan
`FeedbackStackHost` caps the fanned deck at the smaller of 60 percent of the available height and what the keyboard leaves (`FeedbackStackHost.kt` KDoc). The current 240 dp stage caps the fan at about 144 dp. Use a fixed stage of at least ~320-360 dp so the burst of 4 past the cap, the fan and the swipe are exercisable, and Indefinite durations for the burst so timers do not race the tester (the existing demo already does this at `FeedbackFamilyScreen.kt:278-298`).

### Pitfall 8: Sheet demos stacking
`ModalBottomSheet` demos must keep the one-slot rule (WR-01): `OutcomeSheetVariants` (`visibleOutcome`), `ReceiptOutcomeSheetDemos` (`shown`) and `ReceiptConfirmHandledByVariant` (`shown`) are three separate slots separated by the modal scrim; adding go-to-item triggers into the existing `ReceiptDemo`-style slot (new enum or entries) must not break the `ReceiptDemosRegistryRenderTest` assertion that there are "nine" sheet demos (`ReceiptDemo.entries.forEach` renders all entries; adding entries to that enum changes counts, so use a separate enum/slot for go-to-item).

### Pitfall 9: Hardcoded English a11y labels hide German TalkBack gaps
`"Preview"`, `"Undo"`, `"Clear history"` are not caller-localizable (27-VERIFICATION). A German Undo Center demo will still read English for those; record, do not "fix" with API changes here.

### Pitfall 10: Gradle verification trust
"Up-to-date" can be a prior build's output (memory). Gate evidence runs use `--rerun` / `--rerun-tasks --no-build-cache --no-daemon`; never `--stop`, never `--offline`.

### Pitfall 11: Enabling accessibility services resets the harness observer
`am start -S` force-stops the package and disables the observer service (27-04 method notes); re-enable after each launch where a dump is needed.

## Code Examples

### Source-scan guard skeleton (plain JVM)
```kotlin
// Source: pattern from LibraryInvariantsGuardTest.kt (sourceRoot / mainKotlinFiles / stripComments)
class ExplorerSchemePinGuardTest {
    @Test fun `every explorer theme wrap pins the hub scheme`() {
        val files = File(sourceRoot(), "explorer").listFiles { f -> f.extension == "kt" }!!.toList()
        assertTrue("vacuity: explorer scan saw ${files.size} files", files.size > 15)
        val offenders = files.filter { it.name != "ExplorerTheme.kt" && it.name != "ReleaseDemos.kt" }
            .filter { Regex("""\bYahirAndroidTasteTheme\s*\(""").containsMatchIn(SourceContractTestSupport.stripComments(it.readText())) ||
                      Regex("""\bYahirAndroidTasteTheme\s*\{""").containsMatchIn(SourceContractTestSupport.stripComments(it.readText())) }
            .map { it.name }
        assertTrue("explorer files wrap the library theme directly (use ExplorerTheme): $offenders", offenders.isEmpty())
    }
}
```
(`ReleaseDemos.kt` legitimately keeps `HubSchemeScope`'s `dynamicColor = false`; if `ExplorerTheme` replaces it, drop that exclusion.)

### Registry render of a stage (outline)
```kotlin
// Source: ReceiptDemosRegistryRenderTest.render(...) pattern; tags below are PROPOSALS (do not exist yet)
private fun render(entry: String) { /* identical to UndoGroupDemoRegistryRenderTest.render with ComponentRegistry.entries.first { it.name == entry }.content */ }

@Test fun `English stage - burst of 4 evicts the oldest, body tap opens and the readout shows the key`() {
    render("FeedbackStackHost")
    composeTestRule.onNode(hasTestTag("demo_feedback_burst")).performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(1_200)            // 4 cards 350 ms apart
    assertEquals(3, composeTestRule.onAllNodes(hasTestTag("feedback_stack_item")).fetchSemanticsNodes().size)
    composeTestRule.onAllNodes(hasTestTag("feedback_stack_item")).onFirst().performClick()   // newest-first open
    composeTestRule.onNode(hasTestTag("demo_feedback_opened")).assertTextContains("demo-item", substring = true)
}
```
`feedback_stack_item` is quoted from `FeedbackStackHost.kt` (`FEEDBACK_STACK_ITEM_TEST_TAG = "feedback_stack_item"`, internal const, visible to same-module tests).

### Observer extension for Layer 2a
```kotlin
// Source: DeckA11yService.walk() in the Gate-1 harness; add two fields (API 28 / API 30)
"heading=${n.isHeading} state=${n.stateDescription} "
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| TalkBack sweep deferred as "Gate-2 physical" (v2.5, v2.6) | Scoped sweep with a structural agentic layer plus parked spoken items | v2.8 (999.2 promoted to A11Y-02) | Most claims become checkable at Gate-1; only speech/feel stays physical |
| Hub userAgentValue as the OSM identifier | `additionalHttpRequestProperties["User-Agent"]` because MAPNIK normalizes the UA | POL-03 (2026-10-17) | The header the caller sets finally reaches the wire |
| Dynamic color on every Explorer page except sheet/tone demos | Whole Explorer pinned to the hub scheme | This phase | Warn tone no longer wallpaper-dependent anywhere |

**Deprecated/outdated:** `AGENT-DEVICE-TESTING.md` text still mentions a `Voice Command`-below-the-fold note and version `1.10.0` (still the hardcoded coordinate); it does not mention the observer `AccessibilityService` or the baseline-AAR control (Phase 29/housekeeping, not this phase).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `Build.VERSION.RELEASE` (e.g. "15") is the intended `<sdk>` in `"<pkg>/<versionName> (Android <sdk>)"`, matching the `(Android 15)` examples | POL-03 | UA string differs from teamlead's intent; still a real UA on the wire, low risk. Confirm in plan review |
| A2 | "bare-package-name UA falls back" (REQUIREMENTS) means: a trimmed UA equal to the app's own `packageName` is expanded to `<pkg>/<versionName> (Android <sdk>)`; other non-blank values pass through | POL-03 | If teamlead meant "any value without a slash", the passthrough test differs |
| A3 | "again whenever a placeholder is detected" can only be implemented as a scan (once per install flag plus the first `configureOsmdroid` per process), since downloader hooks are not cheap | POL-03 | Extra scans per process (cheap) or, if the teamlead wants literal once-per-install only, drop the per-process scan |
| A4 | SQLite `length(blob)` is header-derived so the size filter is cheap on a large tile DB | POL-03 | A slow synchronous scan on the main thread; mitigate by running off-main |
| A5 | `HttpURLConnection.setRequestProperty` is case-insensitive for header names | POL-03 | Removing case variants is still harmless; the one-entry test stays the source of truth |
| A6 | TalkBack speaks the child `Text` of a live-region `Surface` that is not `mergeDescendants` (candidate defect C-1) | A11Y sweep | If it does not, the refused-undo message is silent on device; Layer 2a event text will show it; fix is one `mergeDescendants = true` |
| A7 | TalkBack lists both Dismiss forms (ACTION_DISMISS and the custom action) as two entries (C-4) | A11Y sweep | Cosmetic duplicate in the actions menu; designer decides |
| A8 | The tester's TalkBack package/service names are the Samsung ones and enabling via `settings put secure enabled_accessibility_services` works without a dialog | Gate-1 method | The optional TalkBack smoke fails; items park (already the default) |
| A9 | A `com.example.*` harness application id makes osmdroid's normalized UA `<pkg>/<versionCode>` blocked on the tester exactly as in the host probe | Wire check | The control run would not show the placeholder; probe on the host with the exact normalized string first (done for `com.example.yahirandroidtasteharness/1.0 (Android 15)`; the versionCode form `/1` was also blocked for `com.example.secondbrain/1`) |
| A10 | Third-party TalkBack control tools are not worth installing on the tester | Gate-1 method | Missing an agentic speech path; Yahir would then run the parked checks |

## Open Questions

1. **Per-process scan vs. strictly once per install (A3)**
   - Known: CONTEXT says "once per install ... and again whenever a placeholder is detected"; detection in the downloader is not cheap.
   - Unclear: whether a per-process scan is acceptable.
   - Recommendation: implement `purgePlaceholderTiles` as a pure function, call it on the install flag and on the first call per process; document that "detected" means "found by the scan"; ask teamlead only if a reviewer objects.
2. **Fix-or-decide for C-3 and C-4 (oldest card's Undo child order, double Dismiss)**
   - Known: brief D-11 locks both; 26-05 and 26-VERIFICATION forwarded them to this sweep; D-07 says the designer decides after device evidence.
   - Unclear: whether TalkBack really lists two Dismiss entries or reads the oldest Undo out of order.
   - Recommendation: Gate-1 reports the `actionList` / `traversalBefore` evidence; route the decision to the ui-designer seat (via the orchestrator) rather than changing locked semantics unilaterally. Park the spoken confirmation for Yahir.
3. **C-2 (Undo Center row = three stops) fix approach**
   - Known: `ListItem` self-merges so the row node lacks the message text (27-03-SUMMARY).
   - Unclear: acceptable as is, or fix in this phase ("Defects the sweep finds are fixed in this phase").
   - Recommendation: write the Layer-1 test that expects a single node containing message + open label first (red), then fix without changing geometry; if the fix needs a `ListItem` replacement, raise it as a plan-level checkpoint.
4. **Should the Explorer UA constant include a version?**
   - Recommendation: a static descriptive id without a build-time version (the library has no BuildConfig); `YahirAndroidTasteExplorer/<n> (+https://github.com/Ygaray/yahirandroidtaste)` probed as accepted.
5. **Split of `ReleaseDemos.kt`**
   - D-02 says copy lives in `ReleaseDemos.kt` (561 lines today). If the plan splits files, widen `LibraryInvariantsGuardTest` to glob demo files (and keep its vacuity assertion).

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | Gradle/tests | yes | OpenJDK 17.0.20.1 | - |
| `./gradlew` (wrapper) | all test/detekt/apiCheck runs | yes (repo) | AGP 9.2.1 | - |
| `adb` | Gate-1 | yes | host | - |
| `sqlite3`, `python3` 3.12.3, `xxd`, `curl` | wire-level inspection, probes | yes | host | - |
| `yrig` | leases | yes | `~/.local/bin/yrig` | - |
| `uat-run` | signed evidence capture | not on PATH (provided by the `gsd-verify-work-agentic` workflow) | - | use the skill's tooling |
| Tester `R5CT10XNKQN` (Android 15, SM-S908U) | Gate-1 sweep + wire check | not probed (research is read-only); previous Gate-1 on 2026-10-17 used it successfully | - | If not ready after one retry it is INFRA, not FAIL |
| Tester internet (wifi) | OSM tile fetch for the wire check | not probed | - | If offline the wire check cannot run; park with reason |
| Samsung/Google TalkBack on the tester | optional TalkBack smoke | not probed | - | Park spoken items |

**Missing dependencies with no fallback:** none for the code work.
**Missing dependencies with fallback:** TalkBack drivability and tester wifi (park/INFRA).

## Validation Architecture

(`workflow.nyquist_validation` is `true` in `.planning/config.json`.)

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 + Robolectric 4.16.1 + `createComposeRule` (ui-test 1.11.0) |
| Config file | `build.gradle.kts`; detekt `config/detekt/detekt.yml` + `config/detekt-compose.yml` (zero baseline) |
| Quick run command | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfig*' --tests '*PlaceMapTilePurge*' --tests '*DemoRegistryRender*' --tests '*ReceiptDemosRegistryRenderTest' --tests '*ExplorerSchemePinGuardTest' --no-daemon` |
| Guard run command | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --tests '*DomainVocabularyDriftGuardTest' --tests '*ComponentRegistryDriftGuardTest' --tests '*ComponentStatesMatrixTest' --tests '*ComponentPlaygroundIntegrityTest' --tests '*LibraryInvariantsGuardTest' --no-daemon` |
| A11Y Layer-1 command | `./gradlew testDebugUnitTest --tests '*A11ySweep*' --tests '*ReceiptRowTest' --tests '*OutcomeSheetLiveRegionTest' --tests '*ProviderKeyCardLifecycleTest' --tests '*UndoCenterScreen*' --tests '*OpenTarget*' --tests '*FeedbackStackHostSemanticsTest' --no-daemon` |
| Full suite command | `./gradlew testDebugUnitTest detekt apiCheck --rerun --no-build-cache --no-daemon`, then `git diff --stat api.txt` (must be empty), `bash tools/verify-api-source-additive.sh v2.7.1`, `bash tools/test/run-all.sh` |
| Baseline | 1735 tests, 22 pre-existing skips, 0 failures (27-04-SELF-UAT.md rung 1); `tools/test/run-all.sh` PASS=52 FAIL=0 (26-VALIDATION.md) |
| Estimated runtime | quick ~60-120 s; full ~6-7 min (27-04: 6m31s) |

Rules: one Gradle invocation at a time, always `--no-daemon`, never `--stop`, never `--offline`; fresh runs for gate evidence.

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| POL-03 | Header present after first and second `configureOsmdroid`; pre-seeded prefs value overwritten in memory and prefs untouched; exactly one User-Agent entry (case variants removed) | Robolectric | `./gradlew testDebugUnitTest --tests '*PlaceMapOsmdroidConfigTest' --no-daemon` | exists (122 lines), extend + rewrite blank-fallback tests, restore `additionalHttpRequestProperties` in tearDown |
| POL-03 | Blank / bare-package / `com.example.*` -> fallback string and exactly one `Log.w` (ShadowLog) | Robolectric | same class | extend |
| POL-03 | Purge: real placeholder bytes (6987 B + md5) deleted; size-only match and real tiles survive; missing DB file is a no-op; flag prevents rescans | Robolectric real SQLite | `./gradlew testDebugUnitTest --tests '*PlaceMapTilePurgeTest' --no-daemon` | Wave 0 (new + `src/test/resources` placeholder PNG fetched with curl, md5 checked) |
| POL-03 | Source contract: no expiration override, no `CacheManager`, picker has no `Log`; config file has no consumer identifier | plain JVM | `--tests '*PlaceMapPickerTest' --tests '*LibraryInvariantsGuardTest'` | exists |
| POL-03 | Wire: TESTER cache has no placeholder row, has real tile rows; v2.7.1 control has placeholder | Gate-1 device | see Gate-1 checks G1-6 | Gate-1 |
| POL-03 | Host curl with exact resolved strings | manual command | `curl -s -A "<resolved>" -D - -o /dev/null https://tile.openstreetmap.org/3/2/3.png` (no `x-blocked`, size != 6987) | n/a |
| DEMO-02 | Every `YahirAndroidTasteTheme(` in `explorer/` goes through the helper / passes `dynamicColor = false`; detail fallback wrapped | plain JVM source scan | `--tests '*ExplorerSchemePinGuardTest'` | Wave 0 (new) |
| DEMO-02 | `ExplorerTheme` resolves the hub scheme for LIGHT and DARK under a foreign outer scheme | Robolectric | extend the `HubSchemeScope` test pattern in `ReceiptDemosRegistryRenderTest` | new test |
| DEMO-02 | Hoisted demo: Show -> confirm -> `hide()` -> sheet gone | Robolectric | `--tests '*HoistedSheetDemoRegistryRenderTest'` | Wave 0 (new; needs the demo tag) |
| DEMO-02 | TESTER screencap: warn tone is the hub amber, not wallpaper tone, on a page that previously had no pin | Gate-1 device | G1-1 | Gate-1 |
| DEMO-03 | FeedbackStackHost stage EN + DE: fire one, burst of 4 (3 visible, oldest Dismissed), swipe one away (`Dismissed`, readout), body tap opens, "Opened: key" readout; DE labels (`undoLabel`/`dismissLabel`) and 360 dp bounds | Robolectric | `--tests '*FeedbackStackDemoRegistryRenderTest'` | Wave 0 (new) |
| DEMO-03 | Go-to-item: Success single (row + chevron, open readout), batch mixed (targeted rows open their own key, target-less row not clickable, no chevron), Undo Center rows (Available opens, Undone does not), EN + DE | Robolectric | `--tests '*GoToItemDemosRegistryRenderTest'` | Wave 0 (new) |
| DEMO-03 | Registry integrity + CATALOG guard + vocabulary + library invariants green; counts still 67/6/73 | plain JVM | guard run command above | exists |
| DEMO-03 | `api.txt` unchanged, additive check OK | gradle/script | `./gradlew apiCheck --no-daemon` ; `git diff --stat api.txt` empty ; `bash tools/verify-api-source-additive.sh v2.7.1` | exists |
| A11Y-02 | Layer 1 per row of the sweep map, incl. new `Heading` assertion on `undo-group-header-*` label, saved-key summary node excludes Update/Clear, C-1/C-2 expectations | Robolectric | A11Y Layer-1 command | partly exists; new `A11ySweepSemanticsTest` (Wave 0) |
| A11Y-02 | Layer 2a TESTER AccessibilityService dump + actions | Gate-1 device | G1-2..G1-5 | Gate-1 |
| A11Y-02 | Spoken read-out, reading feel, double Dismiss listing | physical | parked | `.planning/uat-pending/28-*.md` |

### Sampling Rate
- **Per task commit:** the narrowest `--tests` set for the touched area (quick run command), one Gradle invocation at a time.
- **Per wave merge:** guard run command + quick run command + the source-contract tests `OutcomeSheetClassicParitySourceContractTest`, `OutcomeSheetReceiptSourceContractTest`, `UndoCenterScreenSourceContractTest`, `FeedbackStackSourceContractTest`.
- **Phase gate:** full suite fresh (`--rerun --no-build-cache --no-daemon`), `git diff --stat api.txt` empty, additive script OK, `run-all.sh` PASS, detekt zero baseline; then Gate-1.

### Gate-1 device checks (TESTER only, serial `R5CT10XNKQN`, USB)
| ID | Check | Instrument | Pass condition |
|----|-------|-----------|----------------|
| G1-0 | Preflight: leases `device:tester` + `build`; awake, no keyguard; record `font_scale`, `wm density` (Physical 450, no override), `enabled_accessibility_services`, `accessibility_enabled`; `pm list packages | grep -i talkback` | adb + `settings get` | state recorded for restore; not-ready after one retry = INFRA |
| G1-1 | DEMO-02 pin: one screencap of a page with a warn tone / `primary` card (for example Voice Command -> Receipt Success/Confirm demos and one page that had NO `HubSchemeScope`, e.g. Feedback deck dot/chevron colors) | `screencap`, pixel probe | Colors equal hub scheme values (compare a v2.7.1/HEAD~ baseline harness if the wallpaper tone is not obviously different); toggling the theme button flips light/dark within the pin |
| G1-2 | A11Y items 1-4 structure: ReceiptRow Button + `state=Expanded/Collapsed`; Undo separate node; saved-key summary one node + Update/Clear Buttons; group header `heading=true` | harness observer (with `isHeading`, `stateDescription` added) | values as in the sweep map |
| G1-3 | Refused-undo live region: node `liveRegion=2`, and the event(s) emitted when the surface appears (text non-empty) | observer events | settles C-1; if event text empty, fix (`mergeDescendants`) and re-run |
| G1-4 | Target rows and cards: `actionList` contains the exact `openLabel` (sheet single row, batch targeted rows, Undo Center openable row, snackbar cards), non-target rows have no click action; card actions Undo before Dismiss, `1048576:Dismiss` plus custom Dismiss; `traversalBefore` chain | observer dump + performAction click/dismiss/custom | matches Layer-1; record C-3/C-4 evidence for the designer |
| G1-5 | Optional TalkBack smoke (time-boxed): enable, speech capture attempt; else park | settings + screencap | result recorded either way |
| G1-6 | POL-03 wire: Explorer picker (hub-owned UA) in a `com.example.*` harness: pull `cache.db`, assert zero placeholder rows and at least one real tile; control with the `v2.7.1` AAR shows the placeholder; host curl with the exact resolved strings | `adb exec-out run-as`, `sqlite3`, script file md5, `curl` | per Pitfall 2/3 |
| G1-7 | DEMO-03 on device: EN and DE stacked-snackbar stage and go-to-item demos reachable from their registry detail pages; DE at 360 dp (`wm density 480`) and font scale 2.0 | `uiautomator`, screencaps | no overflow, readouts update |
| G1-8 | Restore: `wm density 450` (never reset), `font_scale` 1.0, accessibility settings back to recorded values, harness uninstalled, leases released; record in the arrange-and-restore file | adb | read back equals recorded |

### Wave 0 Gaps
- [ ] `explorer/ExplorerTheme.kt` helper + `ExplorerSchemePinGuardTest` (guard goes red on the current 13 sites before the sweep of edits)
- [ ] `FeedbackStackDemoRegistryRenderTest`, `GoToItemDemosRegistryRenderTest`, `HoistedSheetDemoRegistryRenderTest` (red until demos/tags exist)
- [ ] `A11ySweepSemanticsTest` (or additions to existing classes) incl. the missing `Heading` assertion
- [ ] `PlaceMapTilePurgeTest` + real placeholder PNG fixture (curl, `md5sum` must print `c069a15b2cc2d6b6f527ad09eb93c61a`, size 6987)
- [ ] Widen `LibraryInvariantsGuardTest` demo-copy scan if demo copy leaves `ReleaseDemos.kt`
- [ ] Framework install: none (Robolectric/Compose test infra already present)

## Security Domain

`security_enforcement` is enabled (ASVS level 1 in `.planning/config.json`).

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | no auth in this phase |
| V3 Session Management | no | - |
| V4 Access Control | no | the library has no access control; harness is throwaway |
| V5 Input Validation | yes | Trim the caller UA; bind SQL args (`WHERE length(tile) = ?`, `DELETE ... WHERE key = ? AND provider = ?`); treat `FeedbackTarget.key` as opaque (never read, logged or echoed; the demo only shows the key in an Explorer readout, which is an Explorer-only fixture string) |
| V6 Cryptography | partial | MD5 is a content fingerprint of a public placeholder image, not a security control; use `java.security.MessageDigest`, comment it so a reviewer does not flag it |
| V8 Data Protection | yes | Tile cache stays under the app-private `cacheDir`; purge deletes only matching rows; no coordinates or keys in logs (`Log.w` carries only the policy URL and the UA string class) |
| V10 / Malicious code | yes (tooling) | Do not install third-party TalkBack-control APKs on the tester; harness is built from local source and never committed |

### Known Threat Patterns for this stack
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| SQL injection through the purge | Tampering | Parameterized statements only; no string-built SQL |
| Logging the opaque target key / coordinates | Information disclosure | Hygiene contract tests (`OpenTargetHygieneSourceContractTest` pattern: no `.key` reads, no `Log.` in the target files); `Log.w` only in `PlaceMapOsmdroidConfig.kt` |
| Spoofing another app's UA / policy abuse | Spoofing | The hub passes the caller's identifying UA, never a faked third-party one; blocked UAs warn loudly |
| Bulk tile fetch | Policy | No `CacheManager`/bulk API (guard-asserted); purge only deletes |
| Unbounded purge scan blocking the UI | Denial of service | Header-only `length()` filter, best-effort try/catch, optional off-main run |
| Secrets in demos | Information disclosure | `ReleaseDemos.kt` guard bans `sk-` prefixes and consumer nouns; the key-lifecycle demo keeps only the last four characters |

## Sources

### Primary (HIGH confidence)
- Repository source and tests read this session (paths and line ranges cited inline): `explorer/*.kt`, `feedback/FeedbackCard.kt`, `feedback/FeedbackStackHost.kt`, `feedback/UndoCenterScreen.kt`, `component/OpenTargetRow.kt`, `component/ReceiptRow.kt`, `component/OutcomeSheet.kt`, `component/ProviderKeyCard.kt`, `component/PlaceMapOsmdroidConfig.kt`, `component/PlaceMapPicker.kt`, tests `PlaceMapOsmdroidConfigTest`, `ReceiptDemosRegistryRenderTest`, `UndoGroupDemoRegistryRenderTest`, `LibraryInvariantsGuardTest`, `ComponentRegistryDriftGuardTest`, `CatalogDocsDriftGuardTest`, `OutcomeSheetLiveRegionTest`, `OutcomeSheetHoistedStateTest`
- `.planning/phases/26-*/26-05-SELF-UAT.md`, `27-*/27-04-SELF-UAT.md`, `27-03-SUMMARY.md`, `26-VERIFICATION.md`, `27-VERIFICATION.md`, `27-VALIDATION.md`, `HUMAN-UAT-PENDING.md`
- osmdroid-android 6.1.20 aar from the local Gradle cache, disassembled with `javap` (TileDownloader, DefaultConfigurationProvider, SqlTileWriter)
- Live `curl` probes of `https://tile.openstreetmap.org/3/2/3.png` with the user agents tabulated above (2026-10-17)
- `~/.claude/context/devices/common.md`, `test-android.md`

### Secondary (MEDIUM confidence)
- https://github.com/qbalsdon/talkback and https://qbalsdon.github.io/android/kotlin/accessibility/adb/2021/04/15/accessibility-service.html (adb control of TalkBack requires an AccessibilityService or a TalkBack fork)
- https://stackoverflow.com/questions/66790141/adb-accessibility-focus-change

### Tertiary (LOW confidence)
- https://github.com/noidsirius/Axer ; https://www.browserstack.com/docs/app-accessibility/screen-reader-automation/get-started-with-screen-reader-automation (TalkBack automation exists only through external services/forks; not evaluated)
- Samsung TalkBack package names ([ASSUMED])

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH, no changes, versions read from `libs.versions.toml`.
- Architecture / file maps: HIGH, every site read this session.
- POL-03 mechanics: HIGH for bytecode and probe facts; MEDIUM for the purge trigger interpretation (A3) and `Build.VERSION.RELEASE` (A1).
- A11Y candidate defects (C-1..C-4): MEDIUM, structural reasoning only; device evidence decides.
- TalkBack drivability: LOW, deliberately untested.
- Pitfalls: HIGH, drawn from the repo's own Gate-1 histories.

**Research date:** 2026-10-17
**Valid until:** 2026-10-24 (the v2.8 milestone is moving fast; re-check `git diff v2.7.1 HEAD --stat` and the OSM probe before executing)
