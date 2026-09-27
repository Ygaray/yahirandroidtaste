---
phase: 07-chip-color-slot
reviewed: 2026-09-27T00:00:00Z
depth: standard
files_reviewed: 8
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/AppChip.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/TagChipWithContextMenu.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/CardTagRow.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/CardTagRowTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/AppChipTest.kt
  - api.txt
  - config/detekt-compose.yml
findings:
  critical: 0
  warning: 2
  info: 2
  total: 4
status: issues_found
---

# Phase 07: Code Review Report

**Reviewed:** 2026-09-27T00:00:00Z
**Depth:** standard
**Files Reviewed:** 8
**Status:** issues_found

## Summary

Reviewed the `containerColorOverride` chip-color-slot feature end to end: `TagChipUiModel.color` →
`AppChip`/`TagChipWithContextMenu.containerColorOverride` → `CardTagRow` auto-threading, plus the
two documented execution-time deviations (`@JvmOverloads` on `TagChipUiModel`'s constructor, and the
detekt `CyclomaticComplexMethod` threshold bump 25→26).

**Core precedence contract verified correct.** `AppChip.kt`'s three `when`-blocks
(`containerColor`, `contentColor`, `borderStroke`) implement exactly the documented precedence:
`isSelected > relatedness > containerColorOverride > else` for `containerColor`, and
`containerColorOverride` is genuinely absent from the `contentColor` and `borderStroke` arms — the
design contract (override must never influence content color or border) holds in the actual code,
not just the KDoc. `CardTagRow` correctly threads `tag.color` into both the `TagChipWithContextMenu`
branch and the plain-`AppChip` branch, and correctly withholds it from the "+N" overflow chip (which
has no backing single tag to derive a color from). Both structural test files independently confirm
this via source-parsing assertions, and I confirmed by direct code reading (not just re-trusting the
tests) that the assertions match the real `when`-block contents.

**`@JvmOverloads` placement and mechanics verified correct** — annotation is on the constructor
keyword, generates the expected 3/4/5-arg Java-visible overloads mirroring the pre-phase API shape,
and every existing named-argument call site in the repo (`ExplorerFakeData.kt`,
`TagPickerSheetContentTest.kt`, `TagChipEditorDoubleTapRemovalTest.kt`) continues to resolve because
they use named arguments against a superset-compatible signature. No runtime semantic change for any
existing call site.

**Detekt threshold bump verified scoped and justified** — `git diff` against the phase's base commit
confirms `TextCard.kt` (the cited pre-existing complexity-25 offender) is untouched by this phase,
and neither `AppChip.kt` nor `CardTagRow.kt` (the two files whose logic actually grew this phase)
comes close to the new threshold of 26 — the added logic per file is a handful of extra `when`-arms
and a nullable-color-forwarding parameter, not deep branching. The bump does not mask a real
complexity problem in code this review covers.

Two things are worth flagging even though they don't block: the `copy()` method's own signature did
change non-additively (a real, if narrow, binary-compat gap that `@JvmOverloads` structurally cannot
close), and the two test files added for this phase are 100%-source-text-parsing tests (no actual
rendered-color assertion is possible under this repo's Robolectric-only harness), so a future refactor
that reorders or renames these `when`-arms without changing behavior would break the tests, while a
behavior-changing refactor that keeps the same textual arm order/names would pass them silently.

## Warnings

### WR-01: `TagChipUiModel.copy()` signature change is a real, non-additive ABI break beyond the documented Factory-class known issue

**File:** `api.txt:936-937` (and `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt:37-42`)
**Issue:** The task's review brief asks to verify `api.txt`'s new entries are "genuinely additive
… beyond the documented Factory-class known-issue." They are not fully additive: the compiler-
generated `copy()` method's JVM signature changed from
`copy(String, String, int, long, Double)` to `copy(String, String, int, long, Double, Color)`
— the old 5-parameter overload no longer exists. `@JvmOverloads` cannot be applied to a
data class's synthetic `copy()`/`componentN()` functions (Kotlin doesn't allow annotating compiler-
generated members), so this residual break is structurally unfixable without abandoning
`copy()`-based construction entirely. The `TagChipUiModel.kt` KDoc (lines 37-42) and 07-01-SUMMARY.md
already disclose and accept this as a known deviation — but it is a distinct issue from the
"Factory-class known-issue" the review brief names, and is easy to miss if a reviewer only checks
for that one named exception. In this repo's actual consumer model (JitPack tag bump → Gradle
sync → full rebuild, per root `CLAUDE.md`) the practical blast radius is low, since consumers always
recompile against the new coordinate rather than swapping a JAR under old compiled bytecode — but if
any consumer ever caches/vendors a compiled `.class`/`.dex` of this model without a full rebuild
(e.g. a stale build cache, a multi-module consumer that only partially rebuilds), it will surface as
a `NoSuchMethodError` at link time, not a compile error, which is a nasty failure mode to debug.
**Fix:** No code change required if the risk is accepted as-is (it already is, on paper) — but
surface this explicitly in the phase's deviation log/PR description as its own bullet (not folded
into the `@JvmOverloads` explanation), so a future ABI audit doesn't have to re-derive it from the
KDoc. If stronger guarantees are wanted, consider exposing a `@JvmStatic` `Companion.of(...)`
factory with `@JvmOverloads` for Java/ABI-sensitive callers instead of relying on `copy()`.

### WR-02: New structural tests assert source text, not runtime behavior — a silent-pass risk for future refactors

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/AppChipTest.kt:265-306`,
`src/test/java/io/github/ygaray/yahirandroidtaste/component/CardTagRowTest.kt:22-54`
**Issue:** The precedence-order and D-01 (`contentColor`/`borderStroke` never reference
`containerColorOverride`) tests work by extracting `AppChip.kt`'s raw source text for the three
`when`-blocks and grepping for substrings/ordering (`whenBlock("containerColor").indexOf("isSelected
->")` etc.), not by rendering the composable and asserting actual resolved colors. This is
consistent with — and explicitly justified by — this repo's established `SourceContractTestSupport`
idiom for composables the Robolectric harness can't `captureToImage()` on, so it's not a new
anti-pattern introduced by this phase. However, it means: (a) a change that alters `when`-arm
*order* while preserving arm names would fail correctly, but (b) a change that keeps the arm
order/names identical while altering which value each arm resolves to (e.g. swapping
`relatedness.containerColor` for `relatedness.contentColor` by typo inside an arm whose header text
is unchanged) would pass all four of these tests silently, because none of them inspect the RHS
expression of each arm — only arm presence/absence/relative position. The actual precedence-*order*
contract is well covered; the actual color-*value* contract per arm is not, and can't be with the
current harness.
**Fix:** No harness change requested here (out of scope), but worth a one-line note in the test class
KDoc (already present for the broader limitation) that these tests provide ordering/absence
guarantees only, not value-correctness guarantees — so a reviewer of a *future* PR touching this file
knows to read the arm bodies by eye rather than trusting green tests alone.

## Info

### IN-01: Two incompatible tag-color representations now coexist in the public model surface

**File:** `api.txt:933` (`TagChipUiModel.color: Color?`) vs. `api.txt:966`
(`TagManagementUiModel.color: Long?`)
**Issue:** Not introduced by this phase's diff, but this phase is the first to give `TagChipUiModel`
a `color` field, and it lands as `androidx.compose.ui.graphics.Color?` while the pre-existing
`TagManagementUiModel.color` (a different, unrelated model in the same package) is `Long?` (almost
certainly a packed ARGB value, decoupled from Compose). Both are named `color` and represent the same
underlying domain concept ("this tag's color"), but consumers now have two different types to
convert between depending on which model they're holding. This is a pre-existing API surface
inconsistency this phase's naming choice inherits rather than resolves.
**Fix:** No action required for this phase (out of scope — `TagManagementUiModel` isn't in this
phase's file list), but worth a backlog note for a future pass to either standardize on one
representation or document the conversion boundary between the two models explicitly.

### IN-02: `CardTagRowTest`'s occurrence-indexed `AppChip(` lookup is fragile to unrelated reordering

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/CardTagRowTest.kt:35,46`
**Issue:** `callRegion("AppChip(", occurrence = 1)` and `callRegion("AppChip(", occurrence = 2)`
identify the plain-branch call and the overflow-chip call purely by their ordinal position in the
source text. If a future edit reorders the `if (hasCapability) { … } else { … }` branches, or adds
any other `AppChip(`-prefixed call anywhere earlier in `CardTagRow`'s body (e.g. a new debug/preview
helper), the occurrence indices silently point at the wrong call and either assert against the wrong
region or (worse) pass against genuinely unrelated code that happens to also lack
`containerColorOverride`. This mirrors the pre-existing `CardBaseTest` convention this file
deliberately follows, so it's a repo-wide pattern rather than a new risk unique to this phase.
**Fix:** No action required — flagging for awareness only, consistent with the existing convention
this file intentionally mirrors.

---

_Reviewed: 2026-09-27T00:00:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
