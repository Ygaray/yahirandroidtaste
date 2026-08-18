# yahirandroidtaste

A reusable, JitPack-published **Jetpack Compose + Hilt design-system library** for Android:
a curated catalog of production UI components — cards, chips, sheets, buttons/FAB, pickers,
feedback, and empty-state surfaces — plus a self-launching component gallery
(`ExplorerActivity`) that browses every component with a live States / Variants / Playground
detail page. Extracted from [SecondBrain](https://github.com/Ygaray) as a self-contained module.

The library owns the visual language (theme tokens, component archetypes, interaction
conventions such as reveal-confirm swipe and standardized snackbar feedback). It ships
`@Singleton` bindings (e.g. `UndoHistoryStore`) but declares **no `@HiltAndroidApp` and no
`@AndroidEntryPoint`** — the consuming app owns the Hilt composition root and aggregates the
library's bindings into its `SingletonComponent`. See **[`INTEGRATION.md`](INTEGRATION.md)**.

> **Namespace note:** the import root is `io.github.ygaray.yahirandroidtaste`. The coordinate below
> is the *artifact* coordinate, independent of the package.

**Docs for agents & integrators:**
- **[`INTEGRATION.md`](INTEGRATION.md)** — step-by-step checklist to consume the library, including
  the two prerequisites (Hilt `SingletonComponent` host + Compose-BOM alignment).
- **[`API.md`](API.md)** — the full public surface, organized as the seven-family composable catalog.
- **[`ECOSYSTEM.md`](ECOSYSTEM.md)** — the hub-and-consumers constitution (who owns what, where new
  components go, the repin ritual).
- **[`CLAUDE.md`](CLAUDE.md)** — the reuse invariants and the (human-gated) tag → JitPack → repin flow.
- Reference wiring: [`SecondBrain`](https://github.com/Ygaray) is the first (pending) consumer,
  repinned in a later phase.

## Install (JitPack)

> **The tag is not cut yet.** The immutable JitPack tag is cut in **Phase 102** (LIB, tag-cut is
> human-gated). Until then, no published coordinate exists. When it lands, wire it as below.

Add the JitPack repository in your **`settings.gradle.kts`** (inside
`dependencyResolutionManagement { repositories { … } }`):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then depend on it in your module's **`build.gradle.kts`**. Pin an immutable release tag (or a
commit-SHA) — never `main-SNAPSHOT`:

```kotlin
implementation("com.github.Ygaray:yahirandroidtaste:<tag>")   // <tag> cut in Phase 102
```

## Usage

1. **Wrap your UI in the theme.** `YahirAndroidTasteTheme { … }` establishes the design tokens
   (colors, typography, shapes, accent) every component reads. Every component assumes it renders
   inside this theme.
2. **Call components directly.** They are plain public `@Composable` functions grouped into seven
   families — see **[`API.md`](API.md)** for the catalog and the key parameters each one takes.
3. **Host the Hilt bindings.** The library provides `@Singleton`-scoped state holders (e.g.
   `UndoHistoryStore`) via constructor injection; your app must be a Hilt app so its
   `SingletonComponent` aggregates them. See **[`INTEGRATION.md`](INTEGRATION.md)**.
4. **(Optional) Browse the gallery.** The library ships a self-launching `ExplorerActivity`
   (declared in its own manifest as `.explorer.ExplorerActivity`) that renders the whole catalog
   with per-component States, Variants, and a live Playground.

```kotlin
YahirAndroidTasteTheme {
    EmptyState(/* … */)
    AppChip(/* … */)
    ConfirmationDialog(/* … */)
    // …the full seven-family catalog is enumerated in API.md
}
```

## The surface at a glance

- **Seven component families:** cards, chips, sheets, buttons/FAB, pickers, feedback, empty-state.
- **41 registered public composables** in `ComponentRegistry` (the single source of truth that
  drives the gallery and the CATALOG drift guard), plus **4 intentionally-unregistered** structural
  sub-parts (`CardBase`, `WaveformCanvas`, `SwipeableActionRow`, `YahirAndroidTasteTheme`) — 45
  public composables total. The exact per-family enumeration lives in **[`API.md`](API.md)**.
- **`ExplorerActivity`** — a self-contained component gallery, launchable standalone.

## Requirements

- Android `minSdk 35`, `compileSdk 36` (minor API 36.1)
- AGP 9.2.1 / Kotlin 2.3.20 / Hilt 2.60.1 / Compose BOM 2026.02.01 / JDK 17
- Consumer prerequisites: a Hilt-enabled `Application` (`@HiltAndroidApp`) and a Compose BOM aligned
  with the library's — see **[`INTEGRATION.md`](INTEGRATION.md)**.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
