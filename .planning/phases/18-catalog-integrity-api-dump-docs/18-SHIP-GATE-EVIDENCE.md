# Phase 18 Ship-Gate Evidence - CAT-02 / API-02 / INV-02 (milestone v2.5)

**Captured:** 2026-10-05
**HEAD at capture:** `b9848a9c7b710dc44d34e466aa6bb88f6e910280` (branch `main`, plan 18-01 docs already applied)
**Baseline tags used for API-02:** `v2.4.1` (authoritative, the last released tag) and `v2.4.0` (corroborating)

This is the independent re-verification transcript that Phase 19's D-03 binary gate and section 11 consume.
Phase 18 performs no tag cut, no push and no JitPack check. Every command below was run fresh on the HEAD
recorded above; nothing is cached, filtered or hand-typed. Phase-start commit for the boundary checks: `625c714`.

## CAT-02 Evidence

### 1. Clean, uncached, unscoped full suite

Marker file created with `mktemp` immediately before the run (freshness proof).

```
$ ./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --no-build-cache
> Task :cleanTestDebugUnitTest
> Task :detekt UP-TO-DATE
> Task :testDebugUnitTest
> Task :apiCheck
BUILD SUCCESSFUL in 1m 54s
44 actionable tasks: 4 executed, 40 up-to-date
```

(`:detekt UP-TO-DATE` is Gradle's input-hash skip, not a build-cache hit; it is re-run explicitly with `--rerun`
in the INV-02 section so its green is fresh evidence.)

Freshness and totals:

```
JUnit XML files: 77, newer than the pre-run marker: 77
tests=751 skipped=22 failures=0 errors=0
```

The totals are identical to the research observation at `fa6b7ac` (751 / 22 skipped / 0 / 0), as expected: Phase 18
adds no test.

### 2. The four drift-guard classes (each XML fresh, newer than the marker)

```
ComponentRegistryDriftGuardTest    tests="1" skipped="0" failures="0" errors="0"
DomainVocabularyDriftGuardTest     tests="2" skipped="0" failures="0" errors="0"
GeneratedSymbolDriftGuardTest      tests="2" skipped="0" failures="0" errors="0"
ComponentRegistryTierTest          tests="4" skipped="0" failures="0" errors="0"
```

### 3. Registry state (independently re-counted)

```
$ grep -c 'ComponentRegistry.Entry(' src/main/java/io/github/ygaray/yahirandroidtaste/explorer/*FamilyScreen.kt
EmptyStateFamilyScreen.kt:1
ChipsFamilyScreen.kt:5
CardsFamilyScreen.kt:11
ButtonsFabFamilyScreen.kt:4
PickersFamilyScreen.kt:6
ProgressFamilyScreen.kt:4
TactileFoundationFamilyScreen.kt:4
VoiceCommandFamilyScreen.kt:5
SheetsFamilyScreen.kt:18
FeedbackFamilyScreen.kt:3
total=61
$ grep -n 'voiceCommandFamilyEntries' .../explorer/ComponentRegistry.kt
98:        voiceCommandFamilyEntries
$ git diff --name-only 625c714 -- src
(empty)
```

### 4. Added public composable names since v2.4.1

```
$ git diff v2.4.1..HEAD -U0 -- src/main | grep -E '^\+fun [A-Z]'
+fun ApproachLadderCard(
+fun ClarificationBar(
+fun ModelSelectCard(
+fun ProviderKeyCard(
```

Each is registered in `VoiceCommandFamilyScreen.kt` (`name = "ApproachLadderCard"` line 121, `"ClarificationBar"` line 164,
`"ModelSelectCard"` line 88, `"ProviderKeyCard"` line 58). These four are the `@Deprecated(level = HIDDEN)` same-name v2.4.x binary-compatibility shims
(restoring the pre-v2.5 JVM descriptors; added by the 261005-dmc quick, INC-2026-10-05-02 F1). `ComponentRegistryDriftGuardTest`
compares sets of names, so each shim merges into the already-registered name: the guard cannot see the shims by design,
and no `ComponentRegistry` entry and no `INTENTIONALLY_UNREGISTERED` line is added for them. The added-name diff is
exactly those four.

CAT-02 PASS: unscoped uncached `./gradlew cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --no-build-cache` is BUILD SUCCESSFUL; 77 JUnit XML files all written after the run started; tests=751 skipped=22 failures=0 errors=0 (non-vacuous).
CAT-02 PASS: the four drift guards (ComponentRegistryDriftGuardTest, DomainVocabularyDriftGuardTest, GeneratedSymbolDriftGuardTest, ComponentRegistryTierTest) each ran fresh with tests > 0 and no failure or error.
CAT-02 PASS: Voice family has exactly 5 Entry blocks, the ten family screens sum to 61, the Voice list is in the entries concatenation, and nothing under `src/` changed since `625c714`.
CAT-02 PASS: the set of public top-level composable names added since v2.4.1 is exactly the four registered hidden shims (ApproachLadderCard, ClarificationBar, ModelSelectCard, ProviderKeyCard); no registry or allowlist entry is needed.
CAT-02 INFO: test totals match the research numbers (751 / 22 / 0 / 0); no explanation of a difference is required.

## API-02 Evidence

D-01 reading applied: the pass condition is Metalava `apiCheck` green plus a current `api.txt`. The raw-line
script's exit 3 is recorded and allowlisted below, never overridden. Every step ran on the HEAD recorded in the
header, one Gradle task at a time, every swap in a single shell invocation with
`trap 'git checkout -- api.txt' EXIT INT TERM` plus an explicit `git checkout -- api.txt` before the exit code was
inspected.

### 1. apiDump idempotence, then apiCheck

```
$ cp api.txt <scratch>/api.head.txt ; sha256sum api.txt
218bd9c2acb1c01c3a988dfb462449dc3fd43f3ba387c88cb3ae01160d23740e  api.txt
$ ./gradlew apiDump -q            -> rc=0
$ cmp api.txt <scratch>/api.head.txt   -> IDENTICAL (no output)
218bd9c2acb1c01c3a988dfb462449dc3fd43f3ba387c88cb3ae01160d23740e  api.txt
$ ./gradlew apiCheck              -> rc=0, BUILD SUCCESSFUL
$ git status --short api.txt      -> (empty)
```

### 2. Swap-baseline `apiCheck` against the released tags

```
$ T=v2.4.1; git show $T:api.txt > api.txt     # sha256 3fa9d02a4401a2e6...
$ ./gradlew apiCheck                          # rc=0
> Task :metalavaCheckCompatibilityRelease     (executed, not up-to-date)
> Task :apiCheck
BUILD SUCCESSFUL in 34s
$ git checkout -- api.txt ; git status --short api.txt   -> (empty); sha256 218bd9c2acb1c01c... (HEAD file)

$ T=v2.4.0; git show $T:api.txt > api.txt     # sha256 3fa9d02a4401a2e6... (same blob as v2.4.1)
$ ./gradlew apiCheck                          # rc=0 (Gradle reused the identical-input v2.4.1 result)
> Task :metalavaCheckCompatibilityRelease UP-TO-DATE
> Task :apiCheck UP-TO-DATE
BUILD SUCCESSFUL in 1s
$ git checkout -- api.txt ; git status --short api.txt   -> (empty)
```

`git rev-parse v2.4.0:api.txt v2.4.1:api.txt` prints the same blob (`f5832527695eb837c794cf4bf2cb59bcd3ba3e84`) for both
tags: v2.4.1 did not change the API surface, so the v2.4.0 swap is byte-identical input to the v2.4.1 swap and Gradle
legitimately reported it up-to-date. To keep the v2.4.0 proof from resting on an up-to-date skip, it was forced to
execute:

```
$ git show v2.4.0:api.txt > api.txt
$ ./gradlew metalavaCheckCompatibilityRelease --rerun    -> rc=0
> Task :metalavaCheckCompatibilityRelease
BUILD SUCCESSFUL in 32s
$ git checkout -- api.txt ; git status --short api.txt   -> (empty)
```

Debug variant (forced to execute, against the restored committed `api.txt`):

```
$ ./gradlew metalavaCheckCompatibilityDebug --rerun      -> rc=0
> Task :metalavaCheckCompatibilityDebug
BUILD SUCCESSFUL in 32s
$ git status --short api.txt                             -> (empty)
```

### 3. Raw-line script with a RELATIVE `API_FILE` (exit 3, recorded, not hidden)

```
$ API_FILE=api.txt bash tools/verify-api-additive.sh v2.4.1 2> raw.err ; echo rc=$?
rc=3
$ grep -c 'API-ADDITIVE FAIL (lane 3)' raw.err                              -> 10
composable lines: 4    copy(optional ...) lines: 6
```

The ten lines, verbatim (cut to about 250 characters after the common prefix
`API-ADDITIVE FAIL (lane 3): public API line removed/renamed since v2.4.1:`):

```
method @KotlinOnly @androidx.compose.runtime.Composable public static void ApproachLadderCard(java.util.List<io.github.ygaray.yahirandroidtaste.model.ApproachRungUiModel> ladder, optional Boolean? offlineOnly, option
method @KotlinOnly @androidx.compose.runtime.Composable public static void ClarificationBar(String question, java.util.List<io.github.ygaray.yahirandroidtaste.model.ClarificationOptionUiModel> options, kotlin.jvm.fun
method @KotlinOnly @androidx.compose.runtime.Composable public static void ModelSelectCard(java.util.List<io.github.ygaray.yahirandroidtaste.model.ModelOptionUiModel> models, String? selectedModelId, kotlin.jvm.funct
method @KotlinOnly @androidx.compose.runtime.Composable public static void ProviderKeyCard(java.util.List<io.github.ygaray.yahirandroidtaste.model.ProviderOptionUiModel> providers, String? selectedProviderId, kotlin.
method public io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel copy(optional String label, optional kotlin.jvm.functions.Function0<kotlin.Unit> onClick);
method public io.github.ygaray.yahirandroidtaste.model.HandledByUiModel copy(optional String tier, optional String? approach, optional String? provider, optional String? model, optional Integer? escalationCount);
method public io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel copy(optional String id, optional String title, optional String? subtitle, optional String? confidenceCue, optional boolean amended, optional
method public io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel copy(optional String reason, optional String? changedItem);
method public io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel copy(optional String id, optional String label, optional io.github.ygaray.yahirandroidtaste.model.UndoRowState state);
method public io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState.Failure copy(optional String reason, optional io.github.ygaray.yahirandroidtaste.model.HandledByUiModel? handledBy, optional io.github.ygaray
```

Allowlist (known false positive; Metalava `apiCheck` is authoritative):

| # | Superseded v2.4.1 line | Replacement at HEAD | Why it is not a removal |
|---|------------------------|---------------------|-------------------------|
| 1 | `ApproachLadderCard` composable line | same method, params appended and defaulted (rung-state and toggle labels, `router`, `onRouterChange`, `routerOnLabel`, `routerOffLabel`) | appended defaulted parameters rewrite the line (Phases 15 and 17) |
| 2 | `ClarificationBar` composable line | same method, `dismissLabel` appended and defaulted | appended defaulted parameter (Phase 15) |
| 3 | `ModelSelectCard` composable line | same method, `modelLabel` appended and defaulted | appended defaulted parameter (Phase 15) |
| 4 | `ProviderKeyCard` composable line | same method, `emptyProvidersReason` / `providerLabel` appended and defaulted | appended defaulted parameters (Phase 15) |
| 5 | `FailureActionUiModel.copy(label, onClick)` | `copy` with `role` appended | `copy(optional ...)` gains a trailing optional parameter (Phase 16) |
| 6 | `HandledByUiModel.copy(tier, approach, provider, model, escalationCount)` | `copy` with `escalationsLabel` appended | same (Phase 15) |
| 7 | `ProposedItemUiModel.copy(... trailingContent)` | `copy` with `removeContentDescription` appended | same (Phase 15) |
| 8 | `UndoRefusedUiModel.copy(reason, changedItem)` | `copy` with `refusedPrefix` / `changedSinceSuffix` appended | same (Phase 15) |
| 9 | `UndoRowUiModel.copy(id, label, state)` | `copy` with `undoneLabel` appended | same (Phase 15) |
| 10 | `VoiceOutcomeUiState.Failure.copy(reason, handledBy, action)` | `copy` with `body` / `semanticsPrefix` appended | same (Phase 16) |

Classification: this is the known false positive. `tools/verify-api-additive.sh` compares raw `api.txt` lines, and an
in-place appended-defaulted-parameter rewrites the line, while Metalava models the signature and judges it additive
(green against both tags above). Each of the ten lines is a superseded line whose replacement is an
appended-defaulted-parameter line. The count is 10, not the 8 recorded in Phase 15-03, because Phase 16 added two
more (`FailureActionUiModel.role` and the `VoiceOutcomeUiState.Failure` enrichment). The exit 3 was not hidden, no
`HUB_LANE_OVERRIDE` was set, `--no-verify` was not used, and the script was not edited.

Absolute-path form (informational):

```
$ API_FILE="$PWD/api.txt" bash tools/verify-api-additive.sh v2.4.1 ; echo rc=$?
API-ADDITIVE SKIP: baseline v2.4.1 has no /home/yahir/Projects/Reusable/android/yahirandroidtaste/api.txt yet — API-surface check degraded to source-only until a tag includes it
rc=0
```

API-02 INFO: with an absolute `API_FILE` the script looks the path up as a git object name and silently SKIPs with exit 0. This is a known latent defect of the guard (the hook's own API half uses the absolute form, so it is a silent SKIP). It is not fixed here; Phase 19 D-03 retires it.

### 4. Guard self-tests

```
$ bash tools/test/run-all.sh ; echo rc=$?
== tools/test/test-precommit-hook.sh ==      PASS=7 FAIL=0   ok
== tools/test/test-verify-additive-diff.sh == PASS=5 FAIL=0   ok
== tools/test/test-verify-api-additive.sh == PASS=5 FAIL=0   ok
rc=0
```

### 5. Informational binary javap diff (never a gate)

The v2.4.1 AAR was cached locally
(`~/.gradle/caches/modules-2/files-2.1/com.github.Ygaray/yahirandroidtaste/v2.4.1/e574584823b9cd55c67b50baabfc4875b9203fee/yahirandroidtaste-v2.4.1.aar`),
so nothing was fetched from the network. `./gradlew assembleRelease --offline` was BUILD SUCCESSFUL (the release AAR
was up-to-date for the unchanged `src/`; no `src/main` file is newer than it). The exact descriptor-diff recipe from
the 261005-e2e closing gate (`javap -public -s` over both `classes.jar`, `_Factory` / `_MembersInjector` excluded,
`comm -23 base head`) printed:

```
base=2526 head=2584 filtered_ComposableSingletons=12 missing=0
```

API-02 INFO: javap descriptor diff vs the v2.4.1 AAR: base=2526 head=2584 filtered_ComposableSingletons=12 missing=0. Informational only; Phase 19 D-03 owns the binary gate on the tagged HEAD.

API-02 PASS: `./gradlew apiDump` leaves `api.txt` byte-identical to the committed file (sha256 `218bd9c2...3740e`, cmp clean) and `./gradlew apiCheck` is BUILD SUCCESSFUL.
API-02 PASS: swap-baseline `apiCheck` against v2.4.1 is BUILD SUCCESSFUL (executed), with `api.txt` restored by `git checkout` before any exit (status empty).
API-02 PASS: swap-baseline against v2.4.0 is BUILD SUCCESSFUL (`metalavaCheckCompatibilityRelease --rerun` executed; blob is identical to the v2.4.1 one), `api.txt` restored (status empty).
API-02 PASS: `metalavaCheckCompatibilityDebug` (forced `--rerun`) is BUILD SUCCESSFUL.
API-02 PASS: `API_FILE=api.txt bash tools/verify-api-additive.sh v2.4.1` exits 3 with exactly the 10 allowlisted lines (4 composable plus 6 copy), recorded verbatim above as the known false positive; no override used, script not edited.
API-02 PASS: `bash tools/test/run-all.sh` exits 0 (PASS=7, 5, 5; FAIL=0).

<!-- gsd:write-continue -->
