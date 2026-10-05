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

<!-- gsd:write-continue -->
