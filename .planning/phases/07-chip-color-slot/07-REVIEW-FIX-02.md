---
phase: 07-chip-color-slot
fixed_at: 2026-09-27T00:00:00Z
review_path: .planning/phases/07-chip-color-slot/07-REVIEW-02.md
iteration: 2
findings_in_scope: 4
fixed: 4
skipped: 0
status: resolved
---

# Phase 07: Code Review Fix Report (Iteration 2 — WR-01 closure follow-up review)

**Fixed at:** 2026-09-27T00:00:00Z
**Source review:** .planning/phases/07-chip-color-slot/07-REVIEW-02.md
**Iteration:** 2

**Summary:**
- Findings in scope: 4 (0 critical, 2 warning, 2 info)
- Fixed: 4 (WR-01, WR-02, IN-01, IN-02 — all doc-note fixes, see below). WR-01 and WR-02 were both
  "no code change required to close WR-01, doc note is the suggested fallback fix" findings,
  treated as fixable via documentation per the same distinction this phase's iteration-1 fix pass
  used for its own WR-01/WR-02.
- Skipped: 0

`07-REVIEW-02.md`'s own verdict already confirmed WR-01 (the original `copy()` ABI break) is
resolved by commit `7f57492` — this iteration addresses the two new, narrower findings that
review's own bytecode-level verification surfaced, plus its two Info nits.

## Fixed Issues

### WR-01 (07-REVIEW-02.md): `api.txt`/`apiCheck` has zero binary-compatibility coverage of `color`'s own accessors — a pre-existing Metalava blind spot for value-class-typed members

**Files modified:** `tools/README-api-guard.md`
**Applied fix:** Added a "Known limitation: mangled (value-class-bearing) members are not tracked"
section documenting the gap found by decompilation (real JVM methods `getColor-QN2ZGVo()` /
`setColor-Y2TPw74(Color)` exist but are invisible to `api.txt`), its practical consequence
(`apiCheck` cannot detect a break to a value-class-typed member), and the recommended mitigation
(`javap`/`abidiff` on the compiled class whenever a diff touches a value-class-typed public
member) — exactly per the finding's own "Fix" guidance. No source/tooling code was changed (the
finding explicitly scoped this as a backlog note, not a required code change).

### WR-02 (07-REVIEW-02.md): `var color` is the first public, freely-mutable, non-constructor property in this repo's `model/` package — undocumented Compose-stability and aliasing consequences

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt`
**Applied fix:** Extended the class KDoc with an explicit, numbered "Two accepted consequences"
block covering (1) the already-documented `equals()`/`copy()` exclusion and (2) the new
Compose-stability consequence: `color` is the class's only `var`, which disqualifies
`TagChipUiModel` from compiler-inferred `STABLE` (a class with any `var` cannot be `STABLE`
regardless of that var's own type), affecting the ~dozen public composables that take
`TagChipUiModel`/`List<TagChipUiModel>` as a parameter. The KDoc also records why the finding's own
suggested deeper fix (private setter + `Companion.of`/`withColor` only) would not fully restore
stability inference (a `var` disqualifies `STABLE` regardless of setter visibility) and why a
genuinely immutable alternative is not possible without reopening the `copy()` ABI break (Kotlin
requires every primary-constructor parameter of a `data class` to be `val`/`var` — confirmed by a
direct compile-error test performed during this fix pass: `Primary constructor of data class must
only have property ('val' / 'var') parameters`, ruling out a plain non-property constructor
parameter as an escape hatch). No deeper code redesign (e.g. `private set` + a `withColor()`
helper) was attempted in this pass: the finding's own "Fix" guidance explicitly says no code
change is required to close WR-01, and the suggested deeper fix is disclosed as only a partial
mitigation even if attempted — a larger architectural change than either the original operator
ruling or this review asked for. Flagged here as a documented, low-severity,
already-security-audit-accepted tradeoff (see `07-SECURITY.md` UF-07-01/AR-07-04) rather than
re-engineered.

### IN-01 (07-REVIEW-02.md): KDoc overstated "the constructor... stay byte-identical to the pre-Phase-7 shape"

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt`
**Applied fix:** Reworded the KDoc to scope the "byte-identical" claim to `copy()`/`equals()`/
`hashCode()`/`toString()`/`componentN()` only, and added a separate sentence noting the primary
constructor's own change is additive (two extra `@JvmOverloads`-provided short-arity overloads,
introduced by the original Phase 7 landing, independent of and predating this `color` re-shape) —
per the finding's exact suggested wording fix.

### IN-02 (07-REVIEW-02.md): `Companion.of(...)`'s color-bearing overload is `@KotlinOnly`-tagged in `api.txt`, in tension with its "Java-ergonomic" stated purpose

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/TagChipUiModel.kt`
**Applied fix:** Added a one-line clarifying KDoc note on `Companion.of` explaining that
`@KotlinOnly` here is this project's Metalava bookkeeping convention for the literal full-arity
`@JvmOverloads` source declaration (already applied to this class's own primary constructor), not
an actual Java-visibility restriction, and that decompilation confirms the real JVM method is
genuinely callable from Java — per the finding's own suggested fix.

## Verification

`./gradlew apiCheck testDebugUnitTest detekt` — all green (`BUILD SUCCESSFUL`), confirming the
documentation-only changes introduced no regression. A throwaway compile-error test (plain,
non-`val`/`var` `color` parameter on the primary constructor) was performed and reverted during
investigation of WR-02's suggested deeper fix, confirming Kotlin's data-class constructor
restriction cited above; this did not touch the committed working tree beyond the documentation
changes listed here.

---

_Fixed: 2026-09-27T00:00:00Z_
_Fixer: Claude (gsd-milestone-phase-orchestrator, Phase 07 execute-stage)_
_Iteration: 2_
