---
phase: 07-chip-color-slot
fixed_at: 2026-09-27T00:00:00Z
review_path: .planning/phases/07-chip-color-slot/07-REVIEW.md
iteration: 1
findings_in_scope: 4
fixed: 2
skipped: 2
status: resolved
---

# Phase 07: Code Review Fix Report

**Fixed at:** 2026-09-27T00:00:00Z
**Source review:** .planning/phases/07-chip-color-slot/07-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 4 (0 critical, 2 warning, 2 info — `fix_scope: all`)
- Fixed: 2
- Skipped: 2 (both documented acceptable-skips — "no action required, awareness only" with nothing
  concrete to change)

Both in-scope Warning findings requested a documentation-only fix ("no code change required" but a
concrete note to add) rather than a source-code change, and were treated as fixable per that
distinction. Both in-scope Info findings were genuinely "no action required" backlog/awareness-only
items with no concrete edit requested, and were skipped as documented acceptable-skips. No open
blocker/critical/high finding remains — this phase's review→fix loop is resolved.

## Fixed Issues

### WR-01: `TagChipUiModel.copy()` signature change is a real, non-additive ABI break beyond the documented Factory-class known issue

**Files modified:** `.planning/phases/07-chip-color-slot/07-01-SUMMARY.md`
**Commit:** `3280c48`
**Applied fix:** Added a new, standalone deviation-log entry — `**[Accepted known issue, flagged by
Phase 07 code review WR-01] TagChipUiModel.copy()'s JVM signature is a real, non-additive ABI break
— distinct from the documented Factory-class known issue**` — to the "Deviations from Plan" section,
separate from the existing Rule 1/3 `@JvmOverloads` deviation it had previously been folded into.
The new entry states the issue explicitly (old 5-arg `copy()` overload no longer exists, cannot be
fixed with `@JvmOverloads` since Kotlin disallows annotating synthetic data-class members), the
accepted risk (low blast radius under this repo's JitPack-full-recompile consumer model; would
surface as `NoSuchMethodError` at link time only for a consumer that vendors a stale precompiled
artifact), and the suggested stronger-guarantee alternative (`@JvmStatic Companion.of(...)` factory)
per the review's Fix guidance. No source code was changed — the finding explicitly said "no code
change required" and asked only for this documentation move, which is what was applied. Updated the
"Total deviations" rollup line and the "Issues Encountered" line to reference the new entry for
accuracy.

### WR-02: New structural tests assert source text, not runtime behavior — a silent-pass risk for future refactors

**Files modified:**
`src/test/java/io/github/ygaray/yahirandroidtaste/component/AppChipTest.kt`,
`src/test/java/io/github/ygaray/yahirandroidtaste/component/CardTagRowTest.kt`
**Commit:** `a9c5385`
**Applied fix:** Added the one-line-note-plus-context the finding requested to each test class's
KDoc: in `AppChipTest.kt`, a `**Ordering/absence only, not value-correctness (WR-02, Phase 07 code
review):**` paragraph appended to the existing class-level KDoc, explaining that the
`containerColorOverride` precedence-order and D-01 tests assert arm presence/relative position in
`AppChip`'s raw `when`-block source text (via the `whenBlock` helper), not the resolved color value
each arm evaluates to — and that a same-order value swap would pass silently. Same treatment applied
to `CardTagRowTest.kt`'s class KDoc, scoped to its `containerColorOverride = tag.color` textual-
presence assertions. No test logic or harness was changed (out of scope, as the finding stated) —
only the requested documentation note was added.

## Skipped Issues

### IN-01: Two incompatible tag-color representations now coexist in the public model surface

**File:** `api.txt:933` (`TagChipUiModel.color: Color?`) vs. `api.txt:966`
(`TagManagementUiModel.color: Long?`)
**Reason:** Documented acceptable-skip. The finding's own Fix guidance states "No action required
for this phase (out of scope — `TagManagementUiModel` isn't in this phase's file list)" and asks
only for "a backlog note for a future pass" — a backlog/roadmap concern, not a concrete doc edit
inside this phase's own artifacts. Not introduced by this phase's diff; `TagManagementUiModel` is an
unrelated pre-existing model this phase did not touch. Left for a future backlog triage pass rather
than force a change into files this phase does not own.
**Original issue:** `TagChipUiModel.color` (this phase, `Color?`) and the pre-existing
`TagManagementUiModel.color` (`Long?`) are both named `color` but use incompatible types for the
same domain concept, requiring consumers to convert between them depending on which model they hold.

### IN-02: `CardTagRowTest`'s occurrence-indexed `AppChip(` lookup is fragile to unrelated reordering

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/CardTagRowTest.kt:35,46`
**Reason:** Documented acceptable-skip. The finding's Fix guidance is explicit: "No action required
— flagging for awareness only, consistent with the existing convention this file intentionally
mirrors." The occurrence-indexed lookup pattern (`callRegion("AppChip(", occurrence = N)`) is the
established repo-wide `CardBaseTest` convention this new test file deliberately follows, not a new
anti-pattern this phase introduced — changing it here would diverge from that convention rather than
fix a defect.
**Original issue:** `callRegion("AppChip(", occurrence = 1/2)` identifies calls purely by ordinal
position in the source text; a future reorder of `CardTagRow`'s branches or an earlier
`AppChip(`-prefixed call would silently misalign the occurrence indices.

---

_Fixed: 2026-09-27T00:00:00Z_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
