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
- Android `minSdk 35`, `compileSdk 36`, JDK 17.

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
- **The package is still `com.example.secondbrain.yahirandroidtaste`.** The rename to
  `io.github.ygaray.yahirandroidtaste` is Phase 101 — after that lands and a new tag is cut, your
  imports change (a breaking repin). Until then, import from the current root.
- **Bumping to a new library version is human-gated** (see `CLAUDE.md` / `ECOSYSTEM.md` §7): change the
  coordinate, `--refresh-dependencies` + resolve-confirm, rebuild, re-verify on-device before shipping.
