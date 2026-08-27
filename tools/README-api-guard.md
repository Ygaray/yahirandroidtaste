# Hub additive guards

## Declaring non-additive (lane-2/3) changes

The pre-commit hook (`tools/hooks/pre-commit`) runs the lane classifier to detect whether a commit is additive (lane 1, fast path) or non-additive (lane 2: behavior change, lane 3: API break).

Lane 2/3 changes are blocked by default because they require coordination (hub mutex, semantic versioning bump). To land a deliberately non-additive change, re-run your commit with the `HUB_LANE_OVERRIDE` environment variable set to the detected lane:

```bash
HUB_LANE_OVERRIDE=2 git commit …   # Behavior change (source-only modification, API append-only)
HUB_LANE_OVERRIDE=3 git commit …   # API break (public symbol removed/renamed)
```

The hook will then allow the commit with the explicit declaration. This ensures that non-additive changes are intentional and coordinated, not accidental.

## API dump discipline

On every additive change, run `./gradlew apiDump` and commit the updated `$API_FILE` in the same commit — otherwise the `.api` is stale and the pre-commit guard sees no new symbols.

Before any release, run the full guard test suite:

```bash
bash tools/test/run-all.sh
```

This ensures all additive-detection guards pass (verify-api-additive, verify-additive-diff, classify-hub-change, and precommit-hook).

## Installation

Run `bash tools/hooks/install.sh` to symlink the pre-commit hook into your `.git/hooks/` directory.

## ABI-dump mechanism (Task 1 spike, 2026-08-27)

**Chosen mechanism: Metalava** (mechanism 3 of the spike order), via the community
`me.tylerbwong.gradle.metalava` Gradle plugin (v0.5.0).

**`$API_FILE` = `api.txt`** (repo root — root-as-module, so this is the module root too).

Mechanisms 1 and 2 were tried first per the spike order and both failed on this exact stack
(AGP 9.2.1 built-in Kotlin support + Kotlin 2.3.20 + `com.android.library` + Compose):

- **Mechanism 1 — Kotlin built-in `abiValidation`**: `kotlin { abiValidation { enabled.set(true) } }`
  fails with `Unresolved reference 'abiValidation'`. Diagnosed the cause: the `kotlin {}` extension
  present here (`KotlinAndroidProjectExtension` from `kotlin-gradle-plugin:2.3.20`, confirmed via
  reflection) genuinely has no ABI methods — the `abiValidation` sub-extension is registered only by
  the classic `org.jetbrains.kotlin.android` plugin's `apply()` path. Applying that plugin explicitly
  to get it is a dead end: AGP 9's built-in Kotlin support hard-fails the build
  (`⛔ The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin support since AGP
  9.0` / "Remove the plugin from this project's build file"). So on AGP-9-built-in-Kotlin, the
  `abiValidation` DSL is architecturally unreachable — not a version or config problem.
- **Mechanism 2 — classic `org.jetbrains.kotlinx.binary-compatibility-validator` (0.18.1)**: applies
  cleanly (plugin resolves, no errors), but registers **no** `apiDump`/`apiCheck` tasks at all —
  `./gradlew apiDump` fails with `Task 'apiDump' not found`, and `./gradlew tasks --all` confirms zero
  api-related tasks exist. This matches the known risk called out in spec §9.1: the plugin does not
  see `com.android.library` components on this AGP/Kotlin combination. Silent no-op, not a crash.
- **Mechanism 3 — Metalava (`me.tylerbwong.gradle.metalava:0.5.0`)**: applies cleanly and auto-detects
  the Android library's `release` variant, registering `metalavaGenerateSignatureRelease` /
  `metalavaCheckCompatibilityRelease` (plus `...Debug` variants). Dump is clean — Kotlin declarations
  are correctly annotated `@KotlinOnly` and Compose composables show no `$composer`/`$changed`
  synthetic-parameter leakage. This is the mechanism now wired in `build.gradle.kts`.

Two tool-agnostic alias tasks are registered in `build.gradle.kts` so downstream guards depend only
on stable names, not on Metalava specifically:

```kotlin
tasks.register("apiDump")  { dependsOn("metalavaGenerateSignatureRelease") }
tasks.register("apiCheck") { dependsOn("metalavaCheckCompatibilityRelease") }
```

Commands:

```bash
./gradlew apiDump -q    # regenerates api.txt from the current public API (release variant)
./gradlew apiCheck -q   # fails if the current public API diverges from the committed api.txt
```

**Verification performed:**

- Baseline `./gradlew :assembleDebug -q` was green before any changes, and remains green with the
  guard wired in.
- **Determinism**: `apiDump` run twice in a row produced byte-identical `api.txt` (`diff` empty).
- **Negative control**: made `contrastingForeground` in `component/ColorUtils.kt` `internal` (removes
  it from the public API without breaking the many in-module callers that depend on it — deleting it
  outright would have produced a compile error instead of a clean API-diff failure). `apiCheck`
  failed with exit code 1 and an error naming the exact symbol:
  `error: Source breaking change: Removed method
  io.github.ygaray.yahirandroidtaste.component.ColorUtilsKt.contrastingForeground(...) [RemovedMethod]`.
  Restoring the function to `fun` (public) made `apiCheck` pass again (exit 0).
