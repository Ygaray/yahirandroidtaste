---
phase: 07-chip-color-slot
gate: nyquist-validation
run: 2026-09-27
requirement: TAGCOLOR-01
status: gaps_filled
---

# Phase 07: Nyquist Validation Gap-Fill Report

**Trigger:** Phase 07 completed two rounds of code review and a mid-execution ABI-break code fix
(commits `7f57492`, `91e2521`, `395bcd6`) that reshaped `TagChipUiModel.color` into a mutable body
`var` outside the primary constructor, and added a `Companion.of(...)` factory. Neither of these
new/changed surfaces had ever had this Nyquist gate run against them.

## Gaps Investigated

### Gap 1 — `equals()`/`hashCode()`/`copy()` exclusion of `color` (untested)
**Finding: real gap, confirmed.** `07-REVIEW-02.md` itself states: "no call site in this repo...
invokes `.copy(...)` on a `TagChipUiModel` at all" and the two existing color-focused test files
(`AppChipTest.kt`, `CardTagRowTest.kt`) are source-text-parsing tests that "make no assumptions
about `equals()`/`copy()` semantics." No `src/test/.../model/` directory existed in this repo prior
to this run — confirmed via direct filesystem search. The deliberately-chosen semantic (two
instances differing only in `color` are still `.equals()`; `.copy()` never carries `color` forward)
was documented in KDoc and the deviation log but had zero behavioral test coverage.

**Filled:** New `TagChipUiModelTest.kt` (plain JUnit, no Robolectric — mirrors the pure-JVM
`PlaceMapPickerModelTest.kt` idiom already established in this repo), asserting:
- two instances differing only in `color` are `.equals()` and share `hashCode()`
- `.copy()` never carries a non-null `color` forward, and a null `color` stays null across `.copy()`

### Gap 2 — `Companion.of(...)` factory (untested, possibly dead code)
**Finding: real gap, confirmed.** `07-REVIEW-02.md` IN-02 explicitly flags: "no Java test exists to
confirm it" (the factory's real callability), and no Kotlin test exercised it either — a
newly-introduced public API surface with zero test coverage.

**Filled:** Same test file, asserting `Companion.of(...)`:
- constructs a model with the given `color` actually set
- defaults `color` to `null` when omitted
- populates every base field identically to the primary constructor (cross-checked via `equals()`,
  which — per Gap 1 — ignores `color`, confirming the non-color fields match exactly)

### Gap 3 — Pre-existing `AppChipTest.kt`/`CardTagRowTest.kt` coverage (D2/D3) still accurate post-fix
**Finding: no gap.** The 2026-09-27 fix touched only `TagChipUiModel.kt` and `api.txt` — it did not
touch `AppChip.kt` or `CardTagRow.kt`, which is what these two structural test files assert against.
Confirmed directly by re-running both suites live (not trusted from SUMMARY.md):
`./gradlew testDebugUnitTest --tests "*AppChipTest*" --tests "*CardTagRowTest*" --tests
"*TagChipWithContextMenuTest*" --tests "*TagChipUiModelTest*"` → `BUILD SUCCESSFUL`, all suites
green, 0 failures/errors.

### Gap 4 — other Nyquist-relevant gaps
No further gaps found. The `api.txt` ABI-coverage blind spot for `Color`-mangled JVM members
(07-REVIEW-02.md WR-01) and the Compose-stability consequence of `var color` (WR-02) are both
already explicitly risk-accepted/documented findings from code review, not untested-behavior gaps —
out of this gate's scope (this gate targets missing/failing tests, not open design risk-acceptances,
which already went through the review + operator-ruling process on 2026-09-27).

## Verification

```
./gradlew testDebugUnitTest --tests "*TagChipUiModelTest*" --tests "*AppChipTest*" \
  --tests "*CardTagRowTest*" --tests "*TagChipWithContextMenuTest*"
```
Result: `BUILD SUCCESSFUL`. `TagChipUiModelTest`: 7/7 pass, 0 failures, 0 errors (confirmed via
`test-results/testDebugUnitTest/*TagChipUiModelTest*.xml`). All pre-existing suites unaffected.

## Files Created
- `src/test/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModelTest.kt` (new — first test
  file in a new `model/` test package for this repo)

## Verdict
TAGCOLOR-01's genuinely new/changed public API surface from the 2026-09-27 ABI-break fix
(`TagChipUiModel.color`'s exclusion semantics, `Companion.of(...)`) is now behaviorally
test-covered. Combined with the already-passing `AppChipTest`/`CardTagRowTest`/
`TagChipWithContextMenuTest` suites, Phase 07 / TAGCOLOR-01 has full behavioral test coverage with
no outstanding Nyquist gaps.
