---
phase: 07-chip-color-slot
reviewed: 2026-09-27T14:37:02Z
depth: standard
files_reviewed: 3
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt
  - api.txt
  - .planning/phases/07-chip-color-slot/07-01-SUMMARY.md
findings:
  critical: 0
  warning: 2
  info: 2
  total: 4
status: resolved
---

# Phase 07: Code Review Report (Follow-up — WR-01 closure verification)

**Reviewed:** 2026-09-27T14:37:02Z
**Depth:** standard
**Files Reviewed:** 3
**Status:** issues_found

## Summary

This is a targeted follow-up review of commits `7f57492` (fix) and `91e2521` (docs) against the
original `07-REVIEW.md` WR-01 finding: the compiler-synthesized `TagChipUiModel.copy()` had a
genuine non-additive ABI break (old 5-arg overload removed) once `color` was added as a 6th
primary-constructor parameter. The fix moves `color` out of the primary constructor into a mutable
body `var`, adding a `@JvmStatic @JvmOverloads Companion.of(...)` factory for single-call
construction with color.

**WR-01 is genuinely resolved.** This was verified two ways, not just by trusting the deviation
log:

1. **Text-level (`api.txt`).** Diffed the current `TagChipUiModel` block (`api.txt:933-966`)
   against the true pre-Phase-7 baseline (`git show 000bf89~1:api.txt`, lines 933-950). Every
   diff line is a pure addition (3 new constructor-overload lines, the `of(...)` factory family,
   the `Companion` class, the `color` property line). `copy()`, all five `componentN()` methods,
   and the five `getXxx()` accessors are **character-for-character identical** to the pre-Phase-7
   text.
2. **Bytecode-level (decompiled, not just api.txt text).** Force-recompiled (`--rerun-tasks`) and
   ran `javap` against the freshly built debug class. The real JVM method is
   `public final TagChipUiModel copy(String, String, int, long, Double)` — no name-mangling
   suffix, i.e. truly unmangled and identical to the pre-Phase-7 signature. This matters because
   `androidx.compose.ui.graphics.Color` is a Kotlin **inline/value class**: any JVM member whose
   signature includes `Color`/`Color?` gets a compiler-generated mangled name (confirmed by
   decompiling the *pre-fix* release class, where `copy()` was actually
   `copy-_GLGcWc(..., Color)` and `component6()` was `component6-QN2ZGVo()` — mangled, not just
   longer). Since `color` is now fully outside the constructor parameter list, `copy()`,
   `component1()`-`component5()`, `equals()`, `hashCode()`, and `toString()` have zero exposure to
   `Color` and are unmangled — genuinely, verifiably byte-identical to the pre-Phase-7 baseline.

The technical claim in the KDoc/deviation log — "`@JvmOverloads` never affects `copy()`'s
signature; moving `color` out of the primary constructor is the only way to keep `copy()`
unchanged" — is **correct**. `@JvmOverloads` only changes what overloads a constructor/function
itself exposes; Kotlin always derives `copy()`/`componentN()`/`equals()`/`hashCode()`/`toString()`
from the full primary-constructor parameter list, and Kotlin does not allow annotating those
compiler-synthesized members. There is no third option that keeps `color` in the primary
constructor and also keeps `copy()`'s JVM signature unchanged.

No call site in this repo constructs `TagChipUiModel` with `color` (`grep -rn "TagChipUiModel("
src/` — confirmed empty for any `color =` argument), and no call site invokes `.copy(...)` on a
`TagChipUiModel` at all, so this repo has zero present-day exposure to the accepted semantic
consequence (two instances differing only in `color` are `equals()`; `copy()` never carries
`color` forward). The two color-focused test files (`AppChipTest.kt`, `CardTagRowTest.kt`) are
source-text-parsing tests (pre-existing WR-02 from `07-REVIEW.md`, unaffected by this change) and
make no assumptions about `equals()`/`copy()` semantics.

Governance battery re-verified with forced re-execution (not just cache hits):
`./gradlew apiCheck --rerun-tasks` → BUILD SUCCESSFUL (7/7 executed); `./gradlew detekt
--rerun-tasks` → BUILD SUCCESSFUL, 0 code smells across 169 files; `./gradlew testDebugUnitTest
--rerun-tasks` → BUILD SUCCESSFUL, 35/35 executed. All green.

Two new, narrower issues surfaced during bytecode-level verification that neither `07-REVIEW.md`
nor this fix's own deviation log caught (both are about `api.txt`'s *coverage*, not about
`copy()` — WR-01's actual scope is genuinely closed either way).

## Warnings

### WR-01: `api.txt`/`apiCheck` has zero binary-compatibility coverage of `color`'s own accessors — a pre-existing gap this fix inherits and slightly widens

**File:** `api.txt:933-966`, `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt:62`
**Issue:** Decompiling the freshly-built class shows the real, Java-callable JVM members
`getColor-QN2ZGVo()` and `setColor-Y2TPw74(Color)` — both mangled because `Color` is a Kotlin
value class. Neither appears anywhere in `api.txt`; only the bare `property public
androidx.compose.ui.graphics.Color? color;` line does. The same is true for the
`Color`-bearing overload of the new factory: `Companion.of-_GLGcWc(...)` (the real static
forwarding method generated by `@JvmStatic` for the 6-arg overload) exists in the compiled class
but is entirely absent from `api.txt`'s class-level `of(...)` listing (only the 3 non-`color`
overloads are listed there; the `Color`-bearing one is listed once, under `Companion`, tagged
`@KotlinOnly`). This is **not a regression introduced by this fix** — decompiling confirms the
original (pre-fix) `TagChipUiModel` never had a `getColor()`/mangled-accessor entry in `api.txt`
either, even when `color` was a primary-constructor `val`. But the fix's own closing claim
("`api.txt` is now genuinely additive... no consumer can hit `NoSuchMethodError`") is only as
strong as `api.txt`'s coverage, and that coverage has a real, confirmed blind spot for every
member whose JVM signature touches `Color`: apiCheck would not fail if a future change silently
renamed, removed, or changed the visibility of `color`'s getter/setter, or of the
`Color`-accepting `of(...)` overloads, because none of them are tracked. `TagChipUiModel.color` is
this repo's first-ever public model property of type `Color?`, so this gap wasn't previously
exercised anywhere in the library.
**Fix:** No action required to close WR-01 (out of scope for that finding). Worth a backlog note
for `tools/README-api-guard.md`: Metalava's signature dump silently drops JVM-mangled
(inline/value-class-bearing) members for this project's config, and any future public property or
function whose signature involves a Compose `Color` (or any other Kotlin value class, e.g. `Dp`)
carries the same untracked-ABI risk. Consider validating with `javap`/`abidiff` on the compiled
`.class`, not just `api.txt`, whenever the diff touches a `Color`/value-class-typed public member.

### WR-02: `var color` is the first public, freely-mutable, non-constructor property in this repo's `model/` package — undocumented Compose-stability and aliasing consequences

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt:62`
**Issue:** Every other public model in `src/main/java/io/github/ygaray/yahirandroidtaste/model/`
(`ListItemUiModel`, `TagManagementUiModel`, `BrowseSortPreference`, `SavedPlaceUiModel`,
`VoiceClipUiModel`, and `TagChipUiModel` itself pre-fix) is a plain data class of all-`val`
primary-constructor properties — the Compose compiler's stability inferencer marks such a class
`STABLE` automatically (all-`val`, all field types themselves stable/immutable, `Color` included).
Adding `var color: Color? = null` as a class-body property makes `TagChipUiModel` the first public
model in this repo with a mutable, non-`State`-backed field, which downgrades the compiler's
inferred stability for the whole class to unstable — a class containing any `var` is not eligible
for compiler-inferred `STABLE` regardless of the var's own type. `TagChipUiModel`/`List<TagChipUiModel>`
is a parameter on roughly a dozen public composables in this library (`AlbumCard`, `ListCard`,
`TextCard`, `VoiceCard`, `CardTagRow`, `TagPickerSheetContent`, …), so this is a real, silent
behavior change to how the Compose runtime treats a widely-shared type — not flagged anywhere in
the KDoc or the deviation log (which documents the `equals()`/`copy()` consequence but not this
one). Separately, a public mutable `var` breaks the otherwise-universal "value object, change via
`copy()`" convention this codebase relies on for every other model: two variables holding a
reference to the *same* `TagChipUiModel` instance can now silently diverge/re-converge on `color`
without either holder's knowledge, unlike every other field on this (or any other) model in the
package, which requires an explicit `copy()` to change.
**Fix:** No code change required to close WR-01. If tighter guarantees are wanted, consider (a)
annotating the class `@Immutable`/`@Stable` is not applicable here since it genuinely contains a
mutable var — instead, consider making the setter `private`/internal and exposing color changes
only via `Companion.of(...)` or a dedicated `withColor(Color?)` copy-like helper that returns a new
instance, restoring both stability inference (still blocked by the `var`, so this alone doesn't
fully fix it) and the value-object convention; or at minimum, add a one-line KDoc/deviation-log
note calling out the Compose-stability consequence explicitly, the same way the `equals()`/`copy()`
consequence is already called out, so a future reviewer doesn't have to re-derive it.

## Info

### IN-01: KDoc overstates "the constructor... stay byte-identical to the pre-Phase-7 shape"

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt:38-53`
**Issue:** The KDoc (and the commit message) states "Keeping `[color]` out of the primary
constructor means the constructor, `copy()`, `equals()`, `hashCode()`, `toString()`, and every
`componentN()` stay byte-identical to the pre-Phase-7 shape." This is true for `copy()`,
`equals()`, `hashCode()`, `toString()`, and every `componentN()` (verified above), but not for
the constructor itself: relative to the *true* pre-Phase-7 baseline (before `@JvmOverloads` was
ever added, `000bf89~1:api.txt` shows a single ctor line), the constructor gained two additional
JVM-visible overloads (3-arg, 4-arg) via `@JvmOverloads` — added in the *original* Phase 7 Plan 01
landing and simply kept (correctly) by this fix. That's a purely additive change (confirmed via
`api.txt` diff and via `javap`, which shows 4 real constructors post-fix vs. 2 pre-@JvmOverloads),
not a regression, but it is not "byte-identical" either — it's a superset. This is the same
distinction the original `07-REVIEW.md` already drew ("`@JvmOverloads` placement... verified
correct") for the constructor separately from `copy()`; this KDoc paragraph blurs the two together
under one "byte-identical" claim.
**Fix:** Tighten the KDoc wording to scope "byte-identical" to `copy()`/`equals()`/`hashCode()`/
`toString()`/`componentN()` only, and describe the constructor's own change as "additive (gained
two `@JvmOverloads`-provided shorter overloads, same as the original Phase 7 landing)" rather than
folding it into the same "zero change" claim.

### IN-02: `Companion.of(...)`'s `Color`-bearing overload is Java-invisible per `api.txt`'s own `@KotlinOnly` tag, which is in tension with the factory's stated Java-ergonomics purpose

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt:64-84`, `api.txt:964`
**Issue:** The KDoc for `Companion.of(...)` says it exists so "Java/ABI-sensitive callers have a
single-call equivalent [that sets `color`] without touching `copy()`." But `api.txt` tags the one
overload that actually carries `color` (`Companion.of(String, String, int, optional long, optional
Double?, optional Color?)`, line 964) `@KotlinOnly` — the same convention this project already
used (and the original review already accepted) for the pre-fix 6-arg constructor. Decompiling
confirms the underlying JVM methods (`of-_GLGcWc` on both `Companion` and, via `@JvmStatic`, the
outer class) do physically exist and are callable from Java bytecode — `@KotlinOnly` here appears
to be this project's Metalava-tooling convention for "the literal full-arity declaration, as
opposed to the `@JvmOverloads`-synthesized shorter ones," not an actual Java-visibility
restriction. Given the pattern is inherited unchanged from the already-reviewed constructor
convention, this is not a new defect, but it does mean the “Java-ergonomic … construction path
that sets color in one call” KDoc claim rests on a `@KotlinOnly`-tagged member whose real
Java-callability isn't independently demonstrated anywhere in this repo's test suite (no Java test
exists to confirm it).
**Fix:** No action required to close WR-01. Optional: a one-line clarifying comment on what
`@KotlinOnly` means in this project's Metalava setup (a bookkeeping tag on the literal
full-arity source declaration, not an actual access restriction) would save a future reviewer from
re-deriving this the way this review just did.

---

## WR-01 Closure Verdict

**`07-REVIEW.md`'s WR-01 (the `copy()` non-additive ABI break) is resolved**, verified
independently at both the `api.txt` text level and the compiled-bytecode level (via `javap`
decompilation of a freshly forced-rebuilt class, which also surfaced the Kotlin value-class
name-mangling mechanics that make "byte-identical" a provable, not just plausible, claim for
`copy()`/`componentN()`/`equals()`/`hashCode()`/`toString()`). The technical mechanism described in
the fix's commit message and KDoc — that `@JvmOverloads` cannot fix `copy()` and that removing
`color` from the primary constructor is the only structural fix — is correct. The closing
governance battery (`apiCheck`, `detekt`, `testDebugUnitTest`) passes on a forced, non-cached
re-run. The two Warnings above are new, narrower findings about `api.txt` coverage and
Compose-stability/aliasing consequences of the chosen fix shape — neither reopens WR-01, both are
worth a backlog note.

---

_Reviewed: 2026-09-27T14:37:02Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
