# INTEGRATION.md — adopt `yahirandroidtaste` in a new Android app

Operational checklist for consuming the design-system library in a **consumer app**. The public
surface it references is in **`API.md`**; the deeper reuse doctrine is in **`CLAUDE.md`**;
`SecondBrain` is the first (pending) reference consumer, repinned in Phase 103.

**Prerequisites (the host must already have) — read these two first, they are the ones that bite:**

- **A Hilt-enabled `Application`.** The library provides `@Singleton` bindings (e.g. `UndoHistoryStore`,
  declared `@Singleton class UndoHistoryStore @Inject constructor()`) but declares **NO
  `@HiltAndroidApp` and NO `@AndroidEntryPoint`** — it is *bindings-only*. Your app must be a Hilt app
  (`@HiltAndroidApp class MyApp : Application()`) so its `SingletonComponent` aggregates the library's
  `@Singleton` bindings. **Without a Hilt application host, injection of the library's singletons fails
  at the consumer** (no component to install the bindings into). See §3.
- **A Compose BOM aligned with the library's.** The library builds against **Compose BOM 2026.02.01**.
  Your consumer must align its own Compose BOM so the Compose runtime/UI/material3 versions match and
  there is no duplicate/mismatched Compose on the classpath (a mismatch surfaces as
  `NoSuchMethodError` / composition crashes at runtime, not at compile time). See §4.
- Android `minSdk 35`, `compileSdk 36` (minor API 36.1), JDK 17.

---

## 1. Add the JitPack repository

In **`settings.gradle.kts`** → `dependencyResolutionManagement { repositories { … } }`:

```kotlin
maven { url = uri("https://jitpack.io") }
```

(Project-level repos are forbidden under `FAIL_ON_PROJECT_REPOS`.)

## 2. Depend on the library (pin an immutable tag)

> **The tag is cut in Phase 102 (human-gated) — none exists yet.** Use the coordinate below once it
> lands.

Prefer a version-catalog entry in **`gradle/libs.versions.toml`**:

```toml
[versions]
yahirandroidtaste = "vX.Y.Z"   # immutable tag — never main-SNAPSHOT

[libraries]
yahirandroidtaste = { group = "com.github.Ygaray", name = "yahirandroidtaste", version.ref = "yahirandroidtaste" }
```

Then in your app module's **`build.gradle.kts`**:

```kotlin
implementation(libs.yahirandroidtaste)
// equivalently: implementation("com.github.Ygaray:yahirandroidtaste:vX.Y.Z")
```

Confirm it resolved (Gradle/JitPack caching can hand back stale bytes):

```bash
./gradlew --refresh-dependencies :app:dependencies | grep yahirandroidtaste   # must print the pinned tag
```

> The library exposes `sh.calvin.reorderable` via `api` (the `EditorItemRow` receiver type,
> `ReorderableCollectionItemScope`), so that type lands transitively on your compile classpath — you
> do not add it yourself, but be aware it is on the graph.

## 3. Prerequisite — host the Hilt `SingletonComponent` (bindings-only library)

The library ships **no application host**. You provide one; its `SingletonComponent` is where the
library's `@Singleton` bindings live.

```kotlin
@HiltAndroidApp
class MyApp : Application()          // registered as android:name in AndroidManifest.xml
```

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Inject library singletons anywhere in your graph — e.g. into a ViewModel:
    //   class MyViewModel @Inject constructor(private val undoHistory: UndoHistoryStore) : ViewModel()
    // They resolve because MyApp's SingletonComponent aggregated the library's @Singleton bindings.
}
```

**If you skip this** (no `@HiltAndroidApp` app), Hilt has no `SingletonComponent` to install the
library's bindings into and injection of `UndoHistoryStore` (and any other library singleton) fails —
this is the #1 integration mistake for a bindings-only library.

## 4. Prerequisite — align your Compose BOM

Match the library's Compose BOM so a single, consistent Compose is on the classpath:

```kotlin
// build.gradle.kts (consumer)
implementation(platform("androidx.compose:compose-bom:2026.02.01"))   // align with the library
```

Then wrap your UI in the library theme — every component assumes it renders inside it:

```kotlin
setContent {
    YahirAndroidTasteTheme {
        // …call the seven-family components (see API.md)…
    }
}
```

## 5. Call components from your UI

The components are plain public `@Composable` functions — call them directly, passing your domain
data + callbacks (the library holds no domain state). See **`API.md`** for the full catalog and each
component's key parameters:

```kotlin
YahirAndroidTasteTheme {
    EmptyState(icon = Icons.Default.Inbox, title = "Nothing here yet")
    AppChip(label = "Work", isSelected = true, onClick = { /* … */ })
    ConfirmationDialog(title = "Delete?", body = "This can't be undone", onDismissRequest = { /* … */ })
}
```

## 6. (Optional) The component gallery

The library ships a self-launching `ExplorerActivity` (declared in its own `AndroidManifest.xml` as
`.explorer.ExplorerActivity`, `exported=false`, `singleTop`) that browses the whole catalog with
per-component States / Variants / Playground pages. Manifest-merge pulls it in automatically; launch
it explicitly (`Intent` to `…explorer.ExplorerActivity`) if you want the gallery in-app.

---

## Notes & gotchas

- **Bindings-only, not an app.** The library never calls `@HiltAndroidApp`/`@AndroidEntryPoint` — that
  is *your* job (§3). Adding one to the library would be wrong (a library owns no application).
- **Compose version skew is a runtime failure, not a compile failure.** A mismatched BOM compiles fine
  and crashes at composition — align the BOM (§4) and, if in doubt, check
  `./gradlew :app:dependencies | grep androidx.compose` for a single resolved Compose version.
- **The package is `io.github.ygaray.yahirandroidtaste`.** Import composables from that root.
- **Bumping to a new library version is human-gated** (see `CLAUDE.md` / `ECOSYSTEM.md` §7): change the
  coordinate, `--refresh-dependencies` + resolve-confirm, rebuild, re-verify on-device before shipping.
- **`PlaceMapPicker` (HUBW-02) depends on `osmdroid-android` 6.1.20** (archived upstream; accepted
  by the owner, see `.planning/APPROVED-DEPS.md`), delivered transitively at runtime — no consumer
  dependency line is needed.
- **Declare `android.permission.INTERNET`** in your consuming app's manifest; `PlaceMapPicker`'s
  `MapView` fetches OpenStreetMap tiles over the network and will not render without it.
- **Pass an app-identifying `userAgent`** (e.g. your application id) to `PlaceMapPicker` — the
  OpenStreetMap tile usage policy blocks generic/default user agents. The widget draws the required
  attribution itself and never bulk-downloads tiles.
- **Under `LocalInspectionMode`** (Compose previews, Robolectric tests) `PlaceMapPicker` renders a
  same-size, same-tagged placeholder instead of constructing a live `MapView`, so Compose UI tests
  should wrap their content in `CompositionLocalProvider(LocalInspectionMode provides true)`.
- **`PlaceMapPicker` requests no location permission itself.** Current location and address search
  are consumer callbacks (`onUseCurrentLocation`, `onSearch`) — the hub never touches
  `android.location` or a geocoder; the consumer resolves the coordinate and calls `onPinChange`.
- **Source arbitration is the consumer's duty.** `isResolving` disables only the controls that
  *start* a lookup ("Use current location" and the search field's IME Search action) — saved-place
  chips and map placement (a tap, or the "Drop pin at map center" accessibility action) stay
  interactive throughout, so a slow geocoder never blocks the user. Because of this, the consumer
  must treat every `onUseCurrentLocation`, `onSearch`, `onSavedPlaceSelected` and `onPinChange` call
  as starting a new source, and apply an in-flight lookup's result only if no newer source has been
  chosen since it started (e.g. a generation counter incremented on every source call and checked
  before the result is applied).
- **Saved-place chip labels are caller-supplied and need not be unique.** A repeated label is
  disambiguated with its radius and a 1-based ordinal among same-labelled entries — e.g. `"Saved
  places Home, 150 m radius, 1 of 2"`. `PlaceMapPicker` never reorders `savedPlaces`, so the ordinal
  follows your list's own order: pass a **deterministic** order (e.g. sort by label, then radius,
  latitude and longitude) or the same place can change ordinal between emissions.
