# Technology Stack

**Analysis Date:** 2026-08-21

## Languages

**Primary:**
- **Kotlin** 2.3.20 - Core implementation language for all library components and Compose UI

**Secondary:**
- **Java** (via JDK 17) - Runtime and bytecode target

## Runtime

**Environment:**
- Android runtime (minSdk 35, compileSdk 36, minor API level 36.1)

**Package Manager:**
- **Gradle** 9.4.1 (via gradle-wrapper)
- **Lockfile:** `gradle/wrapper/gradle-wrapper.properties` (SHA256-pinned distribution)

## Frameworks

**Core:**
- **Jetpack Compose** 2026.02.01 (via Compose BOM) - Declarative UI framework and component toolkit
- **AndroidX Compose Material3** 2026.02.01 - Material Design 3 components and theming
- **Hilt** 2.60.1 - Dependency injection (bindings-only, no `@HiltAndroidApp` in library)

**Build/Dev:**
- **Android Gradle Plugin (AGP)** 9.2.1 - Android library build and packaging
- **Kotlin Compose Plugin** 2.3.20 - Kotlin compiler plugin for Compose
- **KSP (Kotlin Symbol Processing)** 2.3.9 - Code generation for Hilt annotation processing
- **Detekt** 1.23.8 - Static code analysis and linting (zero-baseline policy)

**Testing:**
- **JUnit** 4.13.2 - Unit test framework
- **Robolectric** 4.16.1 - Android framework emulation for local JVM tests
- **Compose UI Test** (JUnit4) 2026.02.01 - Compose-native UI testing with `createComposeRule()`
- **KotlinX Coroutines Test** 1.11.0 - Coroutine testing utilities

## Key Dependencies

**Critical:**
- **androidx.activity:activity-compose** 1.8.0 - Activity integration with Compose
- **androidx.navigation:navigation-compose** 2.9.8 - Navigation composables for sheet/dialog routing
- **io.coil-kt.coil3:coil-compose** 3.0.4 - Image loading and caching for thumbnail cells
- **sh.calvin.reorderable:reorderable** 3.1.0 - Re-orderable collection support (exposed via `api` for `EditorItemRow` receiver type)
- **com.google.dagger:hilt-android** 2.60.1 - Dependency injection framework (provides `@Singleton` bindings only)
- **com.google.devtools.ksp:symbol-processing-gradle-plugin** 2.3.9 - KSP annotation processor for Hilt

**Build/Infrastructure:**
- **Compose BOM** 2026.02.01 - Version alignment for all Compose libraries (UI, Material3, Icons, Material, Foundation)
- **Gradle Foojay Resolver** 1.0.0 - JDK auto-detection via toolchains

## Configuration

**Environment:**
- **JDK:** OpenJDK 17 (configured in `jitpack.yml` for JitPack builds, enforced in `build.gradle.kts` via `sourceCompatibility = JavaVersion.VERSION_11` and `targetCompatibility = JavaVersion.VERSION_11`)
- **Repository Management:** `FAIL_ON_PROJECT_REPOS` mode enforced in `settings.gradle.kts` — all dependencies resolve from `google()`, `mavenCentral()`, gradle plugin portal only (no project-level repo declarations)

**Build:**
- **Namespace:** `io.github.ygaray.yahirandroidtaste` (publisher-owned, consumer-name-free, renamed in Phase 101 / LIB-03)
- **Single-module hub (D-01):** Repository root IS the publishable library — `build.gradle.kts` and `src/` at root level, `rootProject.name = "yahirandroidtaste"`. No module prefix on Gradle commands (e.g., `./gradlew build`, not `./gradlew :yahirandroidtaste:build`).
- **Publishing:** 
  - Local: `./gradlew publishReleasePublicationToMavenLocal` (writes version `1.0.0` with groupId `com.github.Ygaray`)
  - Remote: JitPack builds from GitHub tag refs (`v1.0.0`, `v1.0.1`, etc.), overrides groupId to `com.github.Ygaray` and version from the resolved ref
  - AGP `singleVariant("release")` opt-in for JitPack compatibility (mandatory for Android library publishing)
- **Detekt:** Configuration in `config/detekt/detekt.yml` (strict, ~10 targeted rules: complexity, naming, style) + Compose-idiomatic threshold overrides in `config/detekt-compose.yml`; zero-baseline policy enforced (`maxIssues: 0`)
- **Compose UI Testing:** `testOptions.unitTests.isIncludeAndroidResources = true` enables `createComposeRule()` under Robolectric

## Platform Requirements

**Development:**
- JDK 17
- Gradle 9.4.1 (via wrapper)
- Android SDK with API level 36 (compileSdk) and level 35 (minSdk)
- IDE/tooling support for Kotlin 2.3.20 and Compose

**Production:**
- JitPack (GitHub-hosted, builds on tag push)
- Consumers: Android minSdk 35, compileSdk 36, Hilt-enabled `Application`, Compose BOM 2026.02.01 alignment (see `INTEGRATION.md`)

---

*Stack analysis: 2026-08-21*
