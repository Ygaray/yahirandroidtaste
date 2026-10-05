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

## INV-02 Evidence

D-03 and its 2026-10-05 runtime re-resolution: there is no automated forbidden-import test and none is added this
milestone. The check is the explicit import-inspection step over ALL `src/main` diffs `v2.4.1..HEAD` (Phases 15-17
plus the binary-compat quicks 261005-dmc and 261005-e2e).

```
$ git diff --name-only v2.4.1..HEAD -- src/main        (12 files)
component/ApproachLadderCard.kt   component/ClarificationBar.kt   component/ModelSelectCard.kt
component/OutcomeSheet.kt         component/ProviderKeyCard.kt    explorer/VoiceCommandFamilyScreen.kt
model/FailureActionUiModel.kt     model/HandledByUiModel.kt       model/ProposedItemUiModel.kt
model/UndoRefusedUiModel.kt       model/UndoRowUiModel.kt         model/VoiceOutcomeUiState.kt
(all under src/main/java/io/github/ygaray/yahirandroidtaste/)

$ (a) added import lines (non-vacuity), git diff v2.4.1..HEAD -U0 -- src/main | grep -E '^\+import '    -> count=10
+import androidx.compose.foundation.selection.selectable
+import androidx.compose.foundation.selection.selectableGroup
+import androidx.compose.material3.minimumInteractiveComponentSize
+import androidx.compose.ui.semantics.Role
+import androidx.compose.ui.semantics.contentDescription
+import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
+import kotlin.jvm.internal.DefaultConstructorMarker      (x4, the hidden-shim synthetic markers)

$ (b) same set, filtered to exclude androidx|kotlin|kotlinx.coroutines|java|javax|io.github.ygaray.yahirandroidtaste
(empty)

$ (c) git diff --exit-code v2.4.1..HEAD -- build.gradle.kts gradle/ settings.gradle.kts jitpack.yml config/    -> rc=0 (no diff)
$ (d) added-lines grep for Log.[dewiv]( | println( | Timber                                                 -> (empty)
$ (e) added-lines grep for @HiltAndroidApp | @AndroidEntryPoint                                             -> (empty)

$ (f) ./gradlew detekt --rerun            -> > Task :detekt   BUILD SUCCESSFUL in 5s   (rc=0)
$ git diff --exit-code -- config/detekt-baseline.xml   -> rc=0 (unchanged)
$ cat config/detekt-baseline.xml
<SmellBaseline><ManuallySuppressedIssues/><CurrentIssues/></SmellBaseline>     (grep -c 'ID>' = 0)
```

Detekt stays green at zero baseline because every new parameter and field is defaulted: `config/detekt-compose.yml`
sets `LongParameterList` `functionThreshold: 18`, `constructorThreshold: 18` with `ignoreDefaultParameters: true`
(`LongMethod` 240, `CyclomaticComplexMethod` 26 are untouched). The `kotlin.jvm.internal.DefaultConstructorMarker`
imports are the Kotlin stdlib's own synthetic-constructor marker used by the hidden v2.4.x binary-compat shims; they
sit under the allowed `kotlin` root, are not a dependency (no build file changed) and add no host coupling.

Residual (the unclassified INV-02 probe item, accepted per D-03 and reviewed manually): the import filter does not see
transitive or annotation-processor coupling or non-import (fully-qualified) references, and there is no automated
forbidden-import test this milestone. Mitigations on file: no build or dependency file changed since v2.4.1, no
Hilt application-host or entry-point annotation was added, and no logging or print call was added.

INV-02 PASS: import inspection is non-vacuous (10 added import lines across 12 changed `src/main` files) and the foreign-import filter is empty; the only added `io.github...` import is the library's own `ActionButtonDefaults`.
INV-02 PASS: `build.gradle.kts`, `gradle/`, `settings.gradle.kts`, `jitpack.yml` and `config/` are byte-unchanged vs v2.4.1; no dependency was added.
INV-02 PASS: the `src/main` diff adds no logging or print call and no Hilt application-host or entry-point annotation (library stays bindings-only, no secrets).
INV-02 PASS: `./gradlew detekt --rerun` is BUILD SUCCESSFUL and `config/detekt-baseline.xml` is unchanged with no issue entries (zero baseline preserved).

## Restore Confirmation

```
$ git status --short api.txt          -> (empty)
$ git diff --exit-code -- api.txt     -> rc=0
$ sha256sum api.txt                   -> 218bd9c2acb1c01c3a988dfb462449dc3fd43f3ba387c88cb3ae01160d23740e  (equals the pre-run value)
```

RESTORE PASS: `api.txt` is byte-identical to the committed file after every swap-and-restore in the API-02 section (v2.4.1, v2.4.0 and the forced v2.4.0 rerun), and no run committed a modified `api.txt`.

## DOC-02 Evidence

Corroboration of plan 18-01 on the final HEAD:

```
$ grep -c '^## 10\. Voice Command$' API.md                      -> 1
$ grep -c 'trailingContent = {' API.md                          -> 1  (the trailing-lambda caveat)
$ grep -n 'Appending the Failure enrichment and router toggle' API.md
303:**Appending the Failure enrichment and router toggle (v2.5.0, VFAIL-01..03, VAPPR-04) ...
$ grep -c '^### Localizing the voice surface' INTEGRATION.md    -> 1
$ grep -c '66 public composables total' API.md                  -> 1
```

DOC-02 PASS: API.md has exactly one `## 10. Voice Command` heading and the `ProposedItemUiModel` trailing-lambda caveat (named argument `trailingContent = { ... }`).
DOC-02 PASS: API.md has the Failure / router sibling paragraph "Appending the Failure enrichment and router toggle (v2.5.0, VFAIL-01..03, VAPPR-04)".
DOC-02 PASS: INTEGRATION.md has exactly one `### Localizing the voice surface (optional)` subsection.
DOC-02 PASS: the documented counts (61 registered, 5 Voice, 66 public composables total) are unchanged and match the registry counted in the CAT-02 section.

## Phase Boundary

```
$ git tag --list 'v2.5*'                       -> (empty)
$ git tag --points-at HEAD                     -> (empty)
$ git diff --name-only 625c714..HEAD -- src tools api.txt config build.gradle.kts gradle settings.gradle.kts jitpack.yml CLAUDE.md README.md ECOSYSTEM.md   -> (empty)
$ git diff --name-only 625c714..HEAD           -> API.md, INTEGRATION.md and .planning/ files only
```

BOUNDARY PASS: no tag matching `v2.5*` exists and no tag points at HEAD; none was cut, moved or pushed by Phase 18.
BOUNDARY PASS: nothing under `src/`, `tools/`, `api.txt`, `config/`, the Gradle build files, `CLAUDE.md`, `README.md` or `ECOSYSTEM.md` changed between the phase-start commit `625c714` and HEAD; the only changed non-planning files are `API.md` and `INTEGRATION.md` (plan 18-01, DOC-02).
BOUNDARY PASS: no consumer repository was touched.

## Known Caveat

`ProposedItemUiModel(id, title) { ... }` (the v2.4.x trailing-lambda call shape) no longer compiles on v2.5.0. Kotlin
binds a trailing lambda to the last constructor parameter, which is now the appended `removeContentDescription`
String instead of `trailingContent`. The fix is the named argument `trailingContent = { ... }`. Per the orchestrator
ruling on research open question 1, this is documented in API.md only (v2.3.0 `showTagColors` style), with no code
change. It is a source-level break only; v2.4.x-compiled binaries still link (javap diff above: missing=0).

The claim at line 60 of `15-VERIFICATION.md` ("Positional, named and trailing-lambda v2.4.0 call shapes bind the same
values") is incorrect for that type. That file was not edited (earlier-phase records are out of scope here). It is
surfaced for the orchestrator to decide before Phase 19. Known consumer call sites (per the Phase 18 context sweep)
use named arguments, so none breaks.

## Summary

| Requirement | Verdict | Authoritative evidence |
|-------------|---------|------------------------|
| CAT-02 | PASS | Unscoped uncached full suite BUILD SUCCESSFUL: tests=751 skipped=22 failures=0 errors=0, 77 fresh XML, four drift guards green; Voice 5 / total 61; added public names = the four registered hidden shims; no registry change |
| API-02 | PASS (D-01 reading) | Metalava `apiCheck` green vs HEAD, v2.4.1 and v2.4.0; Debug variant green; `api.txt` byte-identical after `apiDump` and after every restore; raw-line exit 3 = exactly the 10-line known false positive, not overridden |
| DOC-02 | PASS | API.md section 10 plus compatibility notes and trailing-lambda caveat; INTEGRATION.md localization note (plan 18-01, corroborated above) |
| INV-02 | PASS | 10 added imports, none foreign; build, dependency and config files unchanged since v2.4.1; no logging or Hilt host added; detekt zero baseline green |

Phase 19 hand-off:

- Code HEAD covered by every gate above: `b9848a9c7b710dc44d34e466aa6bb88f6e910280`. The commits that carry this
  evidence file add only `.planning/` content on top of it, so `src/`, `tools/`, `api.txt`, `config/` and the build
  files are identical to that code HEAD. Phase 19 must re-run its own binary gate (D-03) on the commit it tags.
- Test totals: 751 tests, 22 skipped, 0 failures, 0 errors (77 JUnit XML files).
- Raw-line allowlist: 10 lines (4 composable: ApproachLadderCard, ClarificationBar, ModelSelectCard, ProviderKeyCard; 6
  `copy(optional ...)`: FailureActionUiModel, HandledByUiModel, ProposedItemUiModel, UndoRefusedUiModel, UndoRowUiModel,
  VoiceOutcomeUiState.Failure). `tools/verify-api-additive.sh` keeps exiting 3 and the hook's absolute-path API half is
  a silent SKIP until Phase 19 D-03 retires them.
- Informational javap diff vs the cached v2.4.1 AAR: missing=0 (filtered ComposableSingletons=12); the binary gate is
  re-run on the tagged HEAD by Phase 19 D-03 (`tools/verify-binary-abi.sh` is Phase 19's).
- Open caveat for the orchestrator: the `ProposedItemUiModel` trailing-lambda source break (documented only).
- No tag was cut; tagging, push, JitPack check and consumer repins remain human-gated (Phase 19).
