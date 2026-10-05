---
phase: 15-voice-surface-i18n-label-params
fixed_at: 2026-10-05T00:00:00Z
review_path: .planning/phases/15-voice-surface-i18n-label-params/15-REVIEW.md
iteration: 1
findings_in_scope: 6
fixed: 4
skipped: 2
status: resolved
---

# Phase 15: Code Review Fix Report

**Fixed at:** 2026-10-05
**Source review:** .planning/phases/15-voice-surface-i18n-label-params/15-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 6
- Fixed: 4
- Skipped: 2 (both documented acceptable-skips: each needs new public API beyond VI18N-01..04 scope)

**Verification:** run in the isolated review-fix worktree (`.claude/worktrees/rf-15-*`, since torn down), not the
main checkout. `./gradlew testDebugUnitTest --tests '*VoiceI18nSourceCompatTest*'` (2 tests, 0 failures),
`--tests '*VoiceModelLabelDefaultsTest*'` (18 tests, 0 failures) and `./gradlew detekt` (exit 0) were run on
the touched test files. No main-source or api.txt changes were made, so `apiCheck` is unaffected (the commit
hook classified every commit as LANE 1, additive). The full test suite was not run (out of scope for the fixer).

## Fixed Issues

### WR-01: Synthetic `$default` members change; Metalava does not see this

**Files modified:** `API.md`
**Commit:** 0d94c11
**Applied fix:** Added an API.md section "Appending label fields to the voice models and composables (v2.5.0)"
stating the change is source-compatible but NOT binary-compatible for Kotlin default-arg call sites (synthetic
constructor / `copy$default` / composable `$default` and changed-mask members), that a clean `apiCheck` is not
proof of binary compat, and that consumers must recompile on repin to v2.5.0. Doc-only, as the review
recommended. Note: the phase closing-gate / v2.5.0 release notes were not edited here; if they claim "binary
compat" they should be reworded to match (not found in the reviewed files).

### WR-03: Hard-coded joining spaces mean empty or punctuation-sensitive labels produce malformed text

**Files modified:** `API.md`
**Commit:** 156f99f
**Applied fix:** Documented (the review's first, doc-level option) that fragments are joined by a single ASCII
space and a literal `", "`, that punctuation/colon belongs to the label, that a blank label must not be used to
mean "no lead-in", and that other orders/separators require folding the item into `reason`. The runtime
empty-fragment skipping (the second option) and any separator redesign were NOT applied (see Follow-ups).

### IN-01: Vacuous assertions in the source-compat test

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt`
**Commit:** e9a5adc
**Applied fix:** Removed the four `assertNotNull` lines and the unused import, added an explanatory
compile-only comment and `@Suppress("UNUSED_VARIABLE")` on the test. The second half of the finding (exercising
the positional shapes' default rendering at runtime) is already covered by the per-component tests named in the
review and was not duplicated.

### IN-02: No guard against legacy-arity `copy` drift

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt`
**Commit:** 37266ed
**Applied fix:** Added a reflection-based helper `assertLegacyCopyCarriesEveryField` and a test that, for each of
the four models built with every field non-default, invokes the shortest hand-written `copy` with the instance's
own leading components and asserts `copy == original`. A legacy copy that resets or drops a later-appended field
now fails.

## Skipped Issues

### WR-02: `removeContentDescription` is per item, so a partially localized list mixes languages

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt:41`
**Reason:** Documented acceptable-skip. The fix is a sheet-level `OutcomeSheet(removeItemContentDescription = ...)`
override, i.e. new public API outside the VI18N-01..04 scope; the review itself says to accept the per-item
design for v2.5 and record a follow-up. The optional "mixed custom + default list renders each as set" test was
not added either (it would pin behavior of a design slated for revision).
**Original issue:** The label is a per-item field, so any item that forgets it announces English "Remove" in an
otherwise localized TalkBack session; it also enters `equals`/`hashCode`/`copy`.

### WR-04: Localized segment label is announced with English state words

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:132`
**Reason:** Documented acceptable-skip. Either fix (replace the manual `contentDescription` with
`Role.RadioButton`/`selected` semantics, or add `selectedStateLabel`/`notSelectedStateLabel` params to
`SegmentedOptionSelector`) changes shared component behavior or public API beyond VI18N-01..04, and the existing
tests intentionally pin the current announcement (`"En ligne, selected"`) as the documented residual.
**Original issue:** TalkBack announces "En ligne, selected": localized label plus English state words.

## Follow-ups (not applied, for the orchestrator/backlog)

- WR-02: sheet-level remove-description override on `OutcomeSheet`.
- WR-03: skip empty fragments / configurable separators when joining label fragments (behavioral change).
- WR-04: localizable or semantics-based selected state in `SegmentedOptionSelector`; then re-baseline the tests
  that pin the English state words.

---

_Fixed: 2026-10-05_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
