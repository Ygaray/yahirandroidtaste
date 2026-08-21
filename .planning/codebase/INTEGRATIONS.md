# External Integrations

**Analysis Date:** 2026-08-21

## Library Type & Dependency Invariant

**This is a UI component library, not an API client or application host.**

The library operates under a **one-way dependency invariant** (CLAUDE.md):
- **Imports:** Android SDK, AndroidX/Compose, Hilt (bindings only), Coil, navigation-compose, reorderable
- **Never imports:** Consumer apps, app-specific packages, secrets, domain modules
- **Result:** A component renders whatever content and callbacks the caller passes — zero domain assumptions

This invariant makes the library reusable across independent consumers without modification.

## APIs & External Services

**None.** The library does not directly consume external APIs. Consumers are responsible for:
- Fetching data that populates card/sheet content
- Invoking actions triggered by component callbacks
- Network, authentication, and data-layer concerns

**Indirect usage via consumer callbacks:**
- Card swipe/tap callbacks (`onDelete`, `onRename`, `onEditTags`, etc.) invoke consumer logic — they may trigger API calls or domain operations, but the library itself has no knowledge of or dependency on those operations.

## Data Storage

**None in the library.**

**Database:**
- Not applicable — the library is presentation-only and holds no persistent state
- Consumers own all Room, database, or persistence layers
- Components accept `List<T>` parameters (consumer maps domain data into UI models)

**File Storage:**
- Not applicable — the library does not read or write files
- Consumers provide image files/URIs to `AlbumCard`, `AdaptiveMediaPreview`, and `CropOverlay`

**Caching:**
- **Coil** 3.0.4 (`io.coil-kt.coil3:coil-compose`) — optional image caching
  - Used by: `AlbumCard`, `AdaptiveMediaPreview`, `VoiceCard` thumbnail display
  - Consumer provides image URIs/Requests; Coil caches locally per Coil's configuration
  - No library-specific cache configuration — uses Coil defaults (HTTP cache, memory cache)

## Authentication & Identity

**None in the library.** The library declares **no `@HiltAndroidApp` and no `@AndroidEntryPoint`** — it is bindings-only.

**Hilt Architecture:**
- Library provides: `@Singleton` state holders (e.g., `UndoHistoryStore` at `io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryStore`)
- Consumer provides: `@HiltAndroidApp class MyApp : Application()` (host application)
- How it works: Consumer's `SingletonComponent` aggregates the library's `@Singleton` bindings; components inject them via constructor parameters
- If consumer skips the `@HiltAndroidApp` host, Hilt injection of library singletons fails at runtime (see `INTEGRATION.md` §3)

## Monitoring & Observability

**Error Tracking:**
- Not applicable — the library does not send telemetry, logs, or error events to any external service

**Logs:**
- **Approach:** Standard Android logging via `android.util.Log` (Logcat)
- No remote log aggregation, Crashlytics, or external observability service
- Consumers can wrap component callbacks to instrument analytics/logging

## CI/CD & Deployment

**Hosting:**
- **JitPack** (GitHub-integrated build service)
  - Public repository: `github.com/Ygaray/yahirandroidtaste`
  - JitPack coordinate: `com.github.Ygaray:yahirandroidtaste:<tag>`
  - Build trigger: Git tag push (e.g., `git tag v1.0.0 && git push origin v1.0.0`)
  - Artifacts produced: `.aar` (compiled library), `.sources.jar` (Kotlin source), `.pom` (Maven metadata)
  - Build command: `./gradlew publishReleasePublicationToMavenLocal` (configured in `jitpack.yml`)

**CI Pipeline:**
- None detected — no GitHub Actions, GitLab CI, or Jenkins configuration in repo
- Builds are on-demand: JitPack builds a tag only on first artifact request
- Consumer CI is responsible for re-testing after repin

**Local Publishing (Development):**
- `./gradlew publishReleasePublicationToMavenLocal` — publishes to `~/.m2/repository` with version `1.0.0`
- Used in development when a consumer needs to test hub changes before a formal tag cut

## Environment Configuration

**Required env vars:**
- None — the library is self-contained and does not read environment variables
- Consumers may use `BuildConfig` or `@ConfigValue` for their own app-level config

**Secrets location:**
- Not applicable — the library holds no secrets
- Consumers must not hard-code secrets in component callbacks or pass secrets through component parameters

## Webhooks & Callbacks

**Incoming:**
- None — the library is not a server and does not expose endpoints

**Outgoing:**
- **Callback-driven only:** Components do not initiate outbound calls; instead, they invoke callbacks passed by the consumer:
  - Card swipe callbacks: `onDelete(cardId)`, `onEditRequest(cardId)`, `onRename(cardId)`
  - Sheet callbacks: `onSave(…)`, `onNavigateToCamera()`, `onDone(selectedIds)`
  - FAB callbacks: `onCreateTextCard()`, `onCreateListCard()`, `onCreateAlbumCard()`, `onCreateVoiceCard()`
  - Picker callbacks: `onColorSelected(Long)`, `onIconSelected(String)`
  - Undo callback: `onUndo(entryId)`
- Consumers implement the callback body — the library has no knowledge of what the callback does

## Cross-Consumer Dependency: ComponentRegistry & CATALOG Drift Guard

**Single source of truth:** `io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistry`

Every public composable must be:
1. **Registered** in one of seven family lists (`cardsFamilyEntries`, `chipsFamilyEntries`, `sheetsFamilyEntries`, `buttonsFabFamilyEntries`, `pickersFamilyEntries`, `feedbackFamilyEntries`, `emptyStateFamilyEntries`), OR
2. **Allowlisted** in `ComponentRegistry.INTENTIONALLY_UNREGISTERED` (structural sub-parts like `CardBase`, `WaveformCanvas`, `SwipeableActionRow`, `YahirAndroidTasteTheme`)

Violation (public composable neither registered nor allowlisted) causes the build to fail — this prevents silent API drift.

## Version Pinning & Repin Ritual

**Consumers pin an immutable JitPack tag (never `main-SNAPSHOT`):**

```kotlin
implementation("com.github.Ygaray:yahirandroidtaste:v1.0.0")   // immutable tag
```

**To ship a hub change to consumers:**
1. Fix/add in the hub, verify locally via `publishReleasePublicationToMavenLocal` + `mavenLocal()` overlay in consumer
2. Run hub's test suite and Detekt (keep zero-baseline)
3. Cut a new immutable tag (e.g., `v1.0.1`)
4. Update consumer's coordinate (`v1.0.0` → `v1.0.1`) + `./gradlew --refresh-dependencies` to confirm resolution
5. Rebuild and device re-verify
6. Merge and tag both repos (human-gated; agents stop before this step)

See `~/.claude/context/workflows/repin.md` (Mechanism B) for the full ritual.

## Current Published Versions

**Latest immutable tag:** `v1.0.0` (cut Phase 102 / LIB-06, commit `4584b60`, device-verified on SecondBrain)

**Consumers:**

| Consumer | Pins at | Pin file | Status |
|----------|---------|----------|--------|
| SecondBrain | `v1.0.0` | `gradle/libs.versions.toml` | Device-verified Phase 103 (REPIN-01/02) |

---

*Integration audit: 2026-08-21*
