# Hub additive guards

## Declaring non-additive (lane-2) changes

The pre-commit hook (`tools/hooks/pre-commit`) runs the lane classifier to detect whether a commit rewrites an existing `src/main` line: lane 1 (fast path) is append-only and allowed, lane 2 (a source-line rewrite, i.e. a behavior change) is blocked unless declared.

A lane-2 change is blocked by default because it requires coordination (hub mutex, semantic versioning bump). To land a deliberately non-additive change, re-run your commit with the `HUB_LANE_OVERRIDE` environment variable set to the detected lane:

```bash
HUB_LANE_OVERRIDE=2 git commit …   # Behavior change (an existing source line was rewritten)
```

The hook will then allow the commit with the explicit declaration. This ensures that non-additive changes are intentional and coordinated, not accidental. The hook fails closed: if the classifier cannot classify the commit, the commit is blocked.

API-surface checks are not done by the hook: Metalava `apiCheck` is the source-level gate and `tools/verify-binary-abi.sh` is the binary gate (below).

## API dump discipline

On every change to the public API, run `./gradlew apiDump` and commit the updated `api.txt` in the same commit, so `./gradlew apiCheck` (the source-level Metalava gate, run in the release battery and whenever public API changes) stays green.

Before any release, run the full guard test suite:

```bash
bash tools/test/run-all.sh
```

This ensures the remaining guards pass (verify-additive-diff, classify-hub-change, precommit-hook, and verify-binary-abi).

## Installation

Run `bash tools/hooks/install.sh` to symlink the pre-commit hook into your `.git/hooks/` directory.

## Binary ABI gate

Metalava `apiCheck` models Kotlin signatures, not the JVM descriptors that Compose `$default` / `$changed` parameters and data-class synthetics compile to, so a clean `apiCheck` does not prove binary compatibility. `tools/verify-binary-abi.sh` is the binary gate.

```bash
tools/verify-binary-abi.sh <previous-tag>     # for the v2.5.0 cut: tools/verify-binary-abi.sh v2.4.1
```

- **What it compares:** `javap -protected -s` descriptors (public and protected members; protected members are binary API for subclassing consumers) of every class in the release AAR built at HEAD versus the baseline tag's AAR, normalized to sorted unique `class#member descriptor` lines and diffed append-only. Each class also contributes header lines (`class#@class`/`@interface`, `@public`, `@nonfinal`, `@concrete`, and one `@super <type>` per extends/implements entry, generics erased), so removing a member-less class or dropping a supertype, `public`, `open` or non-`abstract` fails too. Adding or opening is fine; replacing a direct supertype with an intermediate one is reported as missing (strict by design). Every baseline descriptor must still exist at HEAD; additions are fine.
- **Exit codes:** `0` pass (zero missing public or protected descriptors); `1` usage or precondition (no argument, malformed or unresolvable tag, dirty artifact inputs when the script builds); `2` tool, sanity-floor, javap-error, unsafe-entry-name or baseline-resolution failure; `3` lane 3, at least one public or protected descriptor from the baseline is missing at HEAD. Exit 3 means STOP and is never waived.
- **Baseline resolution order:** `BASELINE_AAR` (an existing file, no fall-through), then the Gradle cache copy of the tag, then an HTTPS download from JitPack. Both AAR hashes and the baseline source are printed so the evidence names the exact bytes.
- **Exclusions:** Dagger `*_Factory` and `*_MembersInjector` classes are not compared; missing `ComposableSingletons$*` lines are ignored and counted (`filtered_ComposableSingletons`).
- **Sanity floor:** both normalized listings must hold at least `MIN_LINES` lines (default 2000, the v2.4.1 baseline has 3885), and any javap stderr output or non-zero javap exit is a hard failure, so an empty or partial javap run can never pass.
- **Seams:** `BASELINE_AAR`, `HEAD_AAR`, `SKIP_BUILD`, `MIN_LINES` and `JITPACK_BASE` exist for the offline fixture test and diagnostics only. The first output line is `SEAMS: none` or names the seams set; a release cut must show `SEAMS: none`.
- **When to run:** at every release cut, on the exact tagged HEAD, before tagging; and whenever a tagged public signature is touched.

## ABI-dump mechanism (Task 1 spike, 2026-08-27)

**Chosen mechanism: Metalava** (mechanism 3 of the spike order), via the community
`me.tylerbwong.gradle.metalava` Gradle plugin (v0.5.0).

**The committed signature file is `api.txt`** (repo root — root-as-module, so this is the module root too).

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

## Known limitation: mangled (value-class-bearing) members are not tracked (found 2026-09-27, Phase 07 WR-01)

Metalava's signature dump does not track JVM members whose signature includes a Kotlin
inline/value class (e.g. `androidx.compose.ui.graphics.Color`) beyond the bare `property` line.
Confirmed by decompiling `TagChipUiModel`'s compiled class (`javap`): the real, callable JVM
methods `getColor-QN2ZGVo()` / `setColor-Y2TPw74(Color)` (both compiler-mangled because `Color` is
a value class) do not appear anywhere in `api.txt` — only `property public
androidx.compose.ui.graphics.Color? color;` does. The same applies to any `@JvmOverloads`-expanded
overload whose parameter list includes a value-class type.

**Practical consequence:** `apiCheck` cannot detect a rename/removal/visibility change to a
value-class-typed member's accessor or overload — it would pass green even if such a change broke
a real Java/ABI-sensitive caller. This is not new to `TagChipUiModel.color` specifically; it is
inherent to this project's Metalava configuration for any public property/function whose signature
touches a Compose `Color` (or any other Kotlin value class, e.g. `Dp`).

**Mitigation (historical finding, now closed):** at the time, the advice was to verify such members
with `javap` (or `abidiff`) on the compiled `.class`, not just `api.txt`, per the review that
surfaced this (07-REVIEW-02.md, Phase 07). The javap gate in "Binary ABI gate" above
(`tools/verify-binary-abi.sh`) now closes this gap: it diffs every public JVM descriptor, including
the compiler-mangled value-class accessors, against the previous tag's AAR.
