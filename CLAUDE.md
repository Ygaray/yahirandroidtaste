# CLAUDE.md — `yahirandroidtaste` (the reusable Compose UI design-system library)

You are working in a **reusable, domain-agnostic Android UI library**: a curated Jetpack Compose
component catalog (cards, chips, sheets, buttons/FAB, pickers, feedback, empty-state), its theme
tokens, its interaction conventions, and a self-launching `ExplorerActivity` gallery, that
independent consumer apps (SecondBrain, and future apps) import via **JitPack**. This repo names
**no app-specific concepts** — every component takes its content + callbacks as parameters.

- **Import root (current):** `com.example.secondbrain.yahirandroidtaste` — **not yet renamed.** The
  rename to `io.github.ygaray.yahirandroidtaste` is a later phase (LIB-03 / Phase 101). Until then,
  keep the `com.example.secondbrain.yahirandroidtaste` namespace verbatim; do **not** pre-rename.
- **JitPack coordinate:** `com.github.Ygaray:yahirandroidtaste:<tag>` (first tag cut in Phase 102 —
  none exists yet).
- **Public repo:** `github.com/Ygaray/yahirandroidtaste`
- **First consumer (pending repin, Phase 103):** SecondBrain.

**Read `API.md` for the public surface and `INTEGRATION.md` to wire a new app.** The essentials of
how this library relates to its consumers:

## The invariants (what keeps it reusable — do not break these)

- **One-way dependency.** The library imports **no host code** (no consumer package), holds **no
  secrets**, and makes **no domain assumptions** — a component renders whatever content + callbacks
  the caller passes. This is exactly what makes it drop-in. Anything you add must keep this litmus
  clean: library → (Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable)
  only, never → a consumer.
- **Bindings-only Hilt, no application host.** The library provides `@Singleton` state holders (e.g.
  `UndoHistoryStore`, `@Inject constructor()`) but declares **no `@HiltAndroidApp` and no
  `@AndroidEntryPoint`**. The consuming app owns the Hilt `Application`; its `SingletonComponent`
  aggregates the library's bindings. Never add an `@HiltAndroidApp` here — that is the consumer's
  job (see `INTEGRATION.md`).
- **`ComponentRegistry` is the single source of truth + a drift guard.** Every public top-level
  `@Composable` in the visual packages (`component/`, `feedback/`, `modifier/`, `theme/`) must be
  **registered** in one of the seven family lists XOR **allowlisted** in
  `INTENTIONALLY_UNREGISTERED` — never neither, never both. The registry's integrity test (and the
  CATALOG drift guard) fails the build otherwise. When you add a public component, register it in
  its family screen's entries list; when you add a private sub-part, no action needed.
- **Interaction conventions travel with the components.** Reveal-confirm destructive swipe
  (left=delete, right=edit), standardized snackbar/undo feedback, and conditional-render-no-dead-
  space are library-wide conventions realized in these components — preserve them.

## Changes here ripple to every consumer — and shipping is human-gated

A change in this repo is **not live in any consumer** until: **new tag → JitPack builds it → the
consumer bumps its coordinate** (`implementation("com.github.Ygaray:yahirandroidtaste:<newtag>")` →
Gradle sync → rebuild → re-verify on-device). That tag/bump/deploy step is **human-gated**: make the
fix + tests autonomously here, then **surface the tag + consumer-bump for confirmation** — do not
tag or repin a consumer without the owner's go-ahead. The full ritual is `ECOSYSTEM.md` §7 +
`~/.claude/context/workflows/repin.md`.

- **Tags are immutable.** Consumers pin an immutable tag (or a commit-SHA); **never `main-SNAPSHOT`**
  and never a moving branch ref (supply-chain integrity).
- JitPack builds from GitHub, not any local clone — so this repo's directory location is irrelevant
  to consumers. Local edits don't reach a consumer until pushed + tagged + the coordinate is bumped.

## Cross-repo work convention (sequential-in-hub)

When a SecondBrain (or other consumer) phase lands code **here** in the hub: run **sequential in the
hub** — **no consumer worktrees.** Commit here on `main`; the consumer's orchestrator owns its own
STATE/ROADMAP tracking. Do not modify consumer files from a hub-scoped task. (This is the
cross-repo-hub-phase convention.)

## Toolchain

- AGP **9.2.1** / Kotlin **2.3.20** / Hilt **2.60.1** / Compose BOM **2026.02.01** / JDK **17**,
  `minSdk 35`, `compileSdk 36`. Android **library** module with Compose enabled (no `applicationId`;
  the only `Activity` is the standalone `.explorer.ExplorerActivity` gallery).
- **Single-module hub (D-01):** the repo root **IS** the publishable `:yahirandroidtaste` library
  (`build.gradle.kts` + `src/` at the root, `rootProject.name = "yahirandroidtaste"`). There is no
  `:yahirandroidtaste:yahirandroidtaste` nesting — so **every Gradle command drops the module
  prefix**:
  - Build: `./gradlew build` (or `./gradlew assembleRelease`)
  - Unit / Robolectric / Compose-UI tests: `./gradlew testDebugUnitTest`
  - Static analysis: `./gradlew detekt`
  - Publish locally (what JitPack runs, see `jitpack.yml`):
    `./gradlew publishReleasePublicationToMavenLocal`
- **Detekt zero-baseline policy.** This extracted library is a genuinely-clean **zero-baseline**
  module (idiomatic-Compose patterns are config-tuned out, not banked as debt). Keep detekt green at
  zero baseline — do **not** regenerate a baseline to bury a new finding; fix it or tune the rule
  with justification.
- The module ships a component library + a standalone gallery `Activity`. It is imported and wired
  by each consumer's Hilt composition root; the gallery `Activity` is the only self-runnable part.
