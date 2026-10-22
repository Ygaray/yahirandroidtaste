---
phase: 29-docs-v2-8-0-cut-readiness
reviewed: 2026-10-18T00:00:00Z
depth: standard
files_reviewed: 21
files_reviewed_list:
  - AGENT-DEVICE-TESTING.md
  - API.md
  - docs/STYLE.md
  - docs/STYLE.REVERTED.md
  - INTEGRATION.md
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/CappedPillShape.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeck.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetrics.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/CappedPillShapeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/HandledByPillShapeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/CatalogDocsDriftGuardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCardTallContentTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCardUndoShapeTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackDeckMetricsTest.kt
  - tools/verify-junit-fresh.sh
  - tools/verify-publish-range.sh
  - tools/test/test-verify-junit-fresh.sh
  - tools/test/test-verify-publish-range.sh
  - tools/test/test-verify-tester-restored.sh
findings:
  critical: 0
  warning: 5
  info: 5
  total: 10
status: issues_found
---

# Phase 29: Code Review Report

**Reviewed:** 2026-10-18
**Depth:** standard
**Files Reviewed:** 21
**Status:** issues_found

## Summary

Reviewed the internal tall-card fix (card scrolls its own content under a finite bound, Undo through an
internal capped-radius button), the v2.8.0 doc set, the drift-guard and card tests, and the two new
pre-cut shell tools with their fixtures.

What checked out:
- `api.txt` is untouched in `732ea62..HEAD`. The new `CappedPillShape`, `cappedPillCapPx`,
  `rememberTextButtonCappedShape`, `CappedPillTextActionButton` and `feedbackCardMaxHeight` are all
  `internal`, so the public surface is unchanged. No consumer imports, no Hilt host, no secrets.
- `feedbackCardMaxHeight` is correct for the edge cases I traced (unbounded stays unbounded, floor,
  `count = 0`, zero host), and matches the added unit tests.
- Doc cross-references hold: the files, symbols, tests and `emitTrackedWithUndo(store, event)` /
  `FeedbackStackHost` parameter orders named in API.md, INTEGRATION.md and STYLE.md exist and match.
- Both shell fixtures and the tester-restored fixture pass here (71, 34 and 82 checks). Running
  `verify-publish-range.sh` over the real range gives `RESULT OK` with one planning-area `ts_net` hit, as
  expected.

No blockers. The concerns are one behavioural risk in `FeedbackCard`, a false-positive-prone credential
pattern and two weak "OK" verdicts in the new tools, and a regression-guard gap in the Undo shape test.

## Warnings

### WR-01: `BoxWithConstraints` in the card makes the whole deck non-intrinsic-safe

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt:277`
**Issue:** The scroll guard is read through `BoxWithConstraints`, which is built on `SubcomposeLayout`.
Compose documents that asking for intrinsic measurements of a `SubcomposeLayout` (and of anything built on
it, `BoxWithConstraints` included) throws `IllegalStateException`, at least on the Compose versions where
`SubcomposeLayout` has no intrinsics policy. Before this change `FeedbackCardRow` sat directly under
`Surface`, so a deck hosted under a parent that queries intrinsics (`IntrinsicSize.Min/Max`, a
`Row(Modifier.height(IntrinsicSize.Min))`) worked. After it, the same host can crash at layout. The library
is host-agnostic and cannot know how consumers place `FeedbackStackHost`. No test covers an intrinsic
query. The subcomposition also moves the card content's composition into the layout phase for every card,
including short ones that never scroll.
**Fix:** Decide boundedness in measure instead of in a subcomposition. For example, drop
`BoxWithConstraints` and put a small `Modifier.layout` before the scroll step that records
`constraints.hasBoundedHeight` into a remembered `MutableState<Boolean>`, applying `verticalScroll` only once
it is true. Or write a tiny custom `Layout` that chooses between the scrolling and plain path inside its
own measure lambda. At minimum, add a test that wraps the host in `IntrinsicSize.Max` and confirm whether
it throws on the pinned BOM. If it does not on this BOM, record that in a comment so a later Compose bump
re-checks it.

### WR-02: The `sk-` credential pattern has no left boundary and flags ordinary prose

**File:** `tools/verify-publish-range.sh:119`
**Issue:** `s''k-[A-Za-z0-9_-]{16,}` matches inside any word ending in "sk" followed by a long hyphenated
run, for example "risk-adjusted-assessment-of-things" or "task-force-review-board". I reproduced it by
feeding such a line through `--diff-file`: the tool printed `NET credential docs/x.md count=1 area=shipped`
and `RESULT REVIEW`. Credential hits fail the scan in every area, `.planning/` prose included
(`NET_CRED` ignores `area`). A phase tree full of hyphenated prose will therefore force a false `RESULT
REVIEW`, and a reviewer who sees noise a few times stops trusting the signal. The pattern list also omits
GitHub `gho_`, `ghs_`, `ghu_` and `github_pat_` tokens.
**Fix:** Anchor the left edge and extend the prefixes:
```bash
's''k-[A-Za-z0-9_-]{16,}'  ->  '(^|[^A-Za-z0-9])s''k-[A-Za-z0-9_-]{16,}'
'gh''p_[A-Za-z0-9]{30,}'   ->  'gh''[pousr]_[A-Za-z0-9]{30,}|github_''pat_[A-Za-z0-9_]{30,}'
```
Keep each positive-control sample consistent (the `sk-` sample then needs a non-alphanumeric or
line-start before it, which the current `"sk-$F20"` sample already has).

### WR-03: `verify-junit-fresh.sh` counts skipped tests as "ran"

**File:** `tools/verify-junit-fresh.sh:80-97`
**Issue:** Gradle's JUnit XML `tests` attribute includes skipped tests. A required guard class whose tests
are all `@Ignore`d, or all skipped by a failed `assume`, reports `tests=N` with `skipped=N`, so `CT > 0` and
the class passes. `JUNIT-FRESH OK` can then be printed for a guard that executed no assertion. The header
promises that "every required class ran". The global totals have the same hole: `TOTALS` prints `skipped`
but never uses it. The fixture (`test-verify-junit-fresh.sh`) covers `tests=0` but not all-skipped.
**Fix:** Count executed tests as `tests - skipped` per class and for the total. Add `zero_<cls>` when the
executed count is 0 and `no_tests` when the total executed count is 0, and add a fixture such as
`mk_suite "$D" AllSkippedTest 3 3 0 0 "$FRESH"` expecting exit 1.

### WR-04: `RESULT OK` is printed with unscanned and history-only exposure

**File:** `tools/verify-publish-range.sh:230-233`
**Issue:** The header says a hit "added and later removed still ships in history", yet `RESULT OK` /
exit 0 is returned when `history_shipped > 0`: a tailnet address, host suffix or home path in a shipped
path that was later reverted is public the moment the range is pushed. The scan also text-scans nothing in
binary files, and `binary=N` does not affect the verdict. The real range has `binary=75` (device
screenshots) and still reports `RESULT OK`, although screenshots can show serials or addresses. A caller
that gates only on the exit code or the final `RESULT` line never sees either exposure. This is documented
in the header, but it makes the single-word verdict weaker than its name.
**Fix:** Return `RESULT REVIEW` (exit 1) when `H_SHIPPED > 0` or `BINARY > 0`, or add a distinct
`RESULT OK-WITH-NOTES` (exit 0) naming both counts. If the lenient behaviour is intended, say in the
`RESULT OK` branch which classes were not enforced, for example
`echo "RESULT OK (history_shipped=$H_SHIPPED binary=$BINARY not enforced)"`, and update the fixture.

### WR-05: The Undo shape tests do not pin the one wire that makes the fix work

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCardUndoShapeTest.kt:98-135, 223-238`
**Issue:** `measure()` obtains the shape from its own `rememberTextButtonCappedShape()` call in the test
composition, not from the button inside the card. It then checks that detached shape against the measured
text box. The source contract only proves that `CappedPillTextActionButton` contains the string
`rememberTextButtonCappedShape()`. Nothing proves that `EmphasisActionButton` forwards `shape` into the
`TextButton`. Today that is `shape = shape ?: ButtonDefaults.textShape` in the `Text` branch of
`DynamicActionButton.kt:244`. If that line reverted to `ButtonDefaults.textShape`, or `shape = shape` was
dropped in a refactor, every test here and the source contract would still pass while the Spanish Undo is
clipped again. That is the Gate-1 SC3 regression the plan exists to prevent.
**Fix:** Extend the source contract to assert the forwarding. Use `functionBody(button,
"fun EmphasisActionButton(")` and assert that the `ActionButtonEmphasis.Text ->` branch contains
`shape ?: ButtonDefaults.textShape`, and that `CappedPillTextActionButton` passes `shape = rememberTextButtonCappedShape()`.
Better, make the wiring observable: give the internal button a `shapeForTest` hook, or assert the rendered
outline through a screenshot-style check.

## Info

### IN-01: `shape` is accepted by `EmphasisActionButton` but only honoured for `Text`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt:215, 224-261`
**Issue:** `shape: Shape? = null` is silently ignored for `Filled`, `Tonal` and `TextSmall`. The private
function's signature reads as general. `CappedPillTextActionButton` also orders its parameters
`(label, role, onClick, modifier, enabled)`, while `DynamicActionButton` uses `(…, onClick, enabled, modifier,
…)`. Both are internal and compile fine today.
**Fix:** Document on `EmphasisActionButton` that `shape` applies to `Text` only, or rename it
`textShape`. Match the `enabled`-before-`modifier` order of the public button.

### IN-02: The single-line capsule test is largely self-referential

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCardUndoShapeTest.kt:200-211`
**Issue:** `painted` is computed as `max(40dp, label.height + 2*8dp)`, the same formula `cappedPillCapPx` uses,
and then compared to the shape's own output. It does not read the painted button height from the layout
(the tagged node includes the `minimumInteractiveComponentSize` wrapper, so it cannot). If the TextButton's
real minimum height or vertical padding drifted away from `ButtonDefaults.MinHeight` / `ContentVerticalPadding`,
this test would still pass.
**Fix:** Keep it as a pure-math check (it duplicates `CappedPillShapeTest`), and add a measured assertion for
a one-line label: the label node's height plus `2 * ContentVerticalPadding` (or the 40dp minimum) must equal
the inner button height, taken from a node inside the min-interactive wrapper.

### IN-03: API.md does not state that tall `FeedbackStackHost` cards scroll

**File:** `API.md:43` (the `FeedbackStackHost` row), `docs/STYLE.md:334`
**Issue:** The user-visible behaviour changed: a card taller than the host space now scrolls its own content
and has no ellipsis or line cap. STYLE.md records it, but the API.md row, which lists the host's behaviours
(fan, swipe, timers), is silent. A consumer reading only API.md would not know a vertical drag on a long
localized card scrolls instead of dismissing or opening.
**Fix:** Add one sentence to the row: "A card taller than the space the host gives it scrolls its content
inside itself; a vertical drag scrolls and a horizontal swipe still dismisses."

### IN-04: The publish-range tool hard-codes a personal user name

**File:** `tools/verify-publish-range.sh:114`
**Issue:** `HOST_USER="${PUBLISH_RANGE_HOST_USER:-yahir}"` ships a personal login in a `tools/` file that
the same tool classifies as shipped. The home-path class also only protects that one user. It is
overridable, and the string is not itself a leak, but it is a default that fits one machine.
**Fix:** Default to `${USER:-$(id -un)}` and keep the env override, so the tool protects whichever account
runs it.

### IN-05: Undo scrolls out of reach inside a scrolled tall card

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt:277-292, 390-418`
**Issue:** The Undo is part of the scrolled row, vertically centered in the full content height. On a tall
scrolling card the pointer path to Undo is off-screen until the user scrolls back up, and
`FeedbackStackState` timers are paused only while a pointer is down. TalkBack still reaches Undo through the
custom action, so this is a touch-only affordance gap, not a blocker.
**Fix:** Acceptable for the cut. Consider pinning Undo outside the scrolled region (a column of
`[scrolled text][pinned action]`) in a later release, and note the current behaviour in `docs/STYLE.md`
section 5 so it is a recorded choice.

---

_Reviewed: 2026-10-18_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
