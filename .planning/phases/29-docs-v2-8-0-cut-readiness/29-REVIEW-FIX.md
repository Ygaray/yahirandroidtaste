---
phase: 29-docs-v2-8-0-cut-readiness
fixed_at: 2026-10-18T00:00:00Z
review_path: .planning/phases/29-docs-v2-8-0-cut-readiness/29-REVIEW.md
iteration: 1
findings_in_scope: 10
fixed: 6
skipped: 4
status: partial
---

# Phase 29: Code Review Fix Report

**Fixed at:** 2026-10-18
**Source review:** .planning/phases/29-docs-v2-8-0-cut-readiness/29-REVIEW.md
**Iteration:** 1

**Constraint:** Gate-1 SC3 passed on the real tester for the exact current `src/main` tree
(`.gate/PLAN07_SRC_HEAD` = 0bad7b6). No `src/main` file was touched; `git diff 647f9e6 HEAD` lists only
`API.md`, one test, and `tools/` files. `api.txt` is byte-identical.

**Summary:**
- Findings in scope: 10 (5 Warning, 5 Info)
- Fixed: 6
- Skipped: 4 (1 needs a `src/main` change, 3 deferred)

`status: partial` because WR-01 is a real, still-open gap (the failing evidence is below), not an
acceptable skip. Everything else is fixed or a documented deferral.

## Fixed Issues

### WR-03: `verify-junit-fresh.sh` counted skipped tests as "ran"

**Files modified:** `tools/verify-junit-fresh.sh`, `tools/test/test-verify-junit-fresh.sh`
**Commit:** 3ad051f
**Applied fix:** Executed tests are now `tests - skipped`, per required class and in total. A class
whose tests are all skipped fails as `zero_<Class>`; a directory where nothing executed fails as
`no_tests`. Fixtures were added RED first (4 failures), then green (40 checks pass).
**Output contract:** unchanged. `FILES`, `STALE`, `TOTALS tests= skipped= failures= errors=`,
`CLASS <name> tests=<n>` (still the raw `tests=` attribute) and `JUNIT-FRESH OK|FAIL <reasons>` print
exactly as before. Only the pass/fail decision is stricter, and no new token was added. Plan 29-06's
`verify-junit-fresh.sh` call is unaffected as long as the thirteen required classes execute at least one
test, which the 29-07 battery already shows.

### WR-02: `sk-` credential pattern had no left boundary

**Files modified:** `tools/verify-publish-range.sh`, `tools/test/test-verify-publish-range.sh`
**Commit:** d08d8a1
**Applied fix:** The `sk-` pattern is now `(^|[^A-Za-z0-9])sk-...` (assembled from fragments as before),
and the GitHub prefixes cover `gh[pousr]_` plus `github_pat_` (own pattern, own positive control).
Fixtures (RED first): hyphenated prose ending in "sk" is not a credential; `gho_`, `ghs_`, `ghu_`,
`ghr_` and `github_pat_` shaped lines each give `NET credential ... count=1` and exit 1. The self-scan
fixture (tool and fixture add no hits of their own) stays green.

### WR-04: `RESULT OK` was printed with unscanned and history-only exposure

**Files modified:** `tools/verify-publish-range.sh`, `tools/test/test-verify-publish-range.sh`
**Commit:** d08d8a1
**Applied fix:** Kept exit 0 (making it exit 1 would trip plan 29-06's PD-5 "exit 1 stops the task" on
the real range, which has `history_shipped=1` and a large binary count), but the verdict is no longer a
bare word when a class is not enforced. When `history_shipped > 0` or `binary > 0` the tool prints
`CAVEAT history_shipped=<n> ...` and/or `CAVEAT binary=<n> ...` lines and then
`RESULT OK (history_shipped=<n> binary=<m> not enforced)`. A bare `RESULT OK` now means both were 0.
Fixtures were updated: the clean range still expects the bare `RESULT OK` and no CAVEAT line; the
history-only and binary fixtures expect the caveat form and no bare `RESULT OK`.
Real-range check: `origin/main..HEAD` gives `net_credential=0 net_shipped=0 outside=0
history_shipped=1 binary=188 fastforward=yes` and `RESULT OK (history_shipped=1 binary=188 not enforced)`.

**OUTPUT TOKEN CHANGE FOR PLAN 29-06:** the final line can now read `RESULT OK (history_shipped=N
binary=M not enforced)` instead of exactly `RESULT OK`, and `CAVEAT ...` lines can appear before it. The
exit codes (0, 1, 2) and the `RANGE`, `FASTFORWARD`, `AUTHORS`, `BINARY`, `OUTSIDE`, `CONTROL`, `NET`,
`HISTORY` and `SUMMARY` lines are unchanged. 29-06 Task 3 step 3 and its automated verify rely on the
exit code and on the `CONTROL`/`SUMMARY` fields, which are intact. Anything that matches `^RESULT OK$`
exactly would no longer match on the real range. Use `^RESULT OK` or the exit code, and quote the CAVEAT
lines in the Scan result section (the plan already asks for binary files to be named as unscanned).

### IN-04: Publish-range tool hard-coded a personal user name

**Files modified:** `tools/verify-publish-range.sh`, `tools/test/test-verify-publish-range.sh`
**Commit:** d08d8a1
**Applied fix:** `HOST_USER` defaults to `${USER:-$(id -un)}`; `PUBLISH_RANGE_HOST_USER` still overrides.
The fixture exports the override for its fixed-name cases and adds one case proving the default follows
the running account. The real-range scan still protects the same account on this host.

### WR-05: Undo shape tests did not pin the wire that makes the fix work

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCardUndoShapeTest.kt`
**Commit:** 06b1410
**Applied fix:** New test `theCappedShapeReachesTheTextButton_throughEmphasisActionButton` (source
contract on comment-stripped source). It asserts that `CappedPillTextActionButton` calls
`EmphasisActionButton(` with `shape = rememberTextButtonCappedShape()` and the Text emphasis, that the
Text branch of `EmphasisActionButton` forwards `shape = shape ?: ButtonDefaults.textShape` to its
`TextButton`, and that `FeedbackCardRow` uses the capped variant. Tests only; no `src/main` change.
Result: `FeedbackCardUndoShapeTest` 11 tests, 0 failures.

### IN-03: API.md did not say tall `FeedbackStackHost` cards scroll

**Files modified:** `API.md`
**Commit:** 6a45421
**Applied fix:** Added to the `FeedbackStackHost` row: "a card taller than the space the host gives it
scrolls its content inside itself (a vertical drag scrolls, a horizontal swipe still dismisses)". Both
halves are backed by existing tests (`tallCard_verticalDrag_doesNotDismissOrOpen`,
`tallCard_horizontalSwipe_stillDismisses`). `CatalogDocsDriftGuardTest` is green (16 tests).

## Skipped Issues

### WR-01: `BoxWithConstraints` makes the host non-intrinsic-safe

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt:277`
**Status:** needs src change (confirmed real; test left out of the suite, not committed)
**Reason:** A Robolectric test (`FeedbackStackHostIntrinsicSizeTest`, four cases) hosted
`FeedbackStackHost` under `Box(Modifier.width(IntrinsicSize.Max))`, `Row(Modifier.height(IntrinsicSize.Min))`
and `Row(Modifier.height(IntrinsicSize.Max))`. All four FAIL on the pinned BOM (2026.04.01), including
the case with no card on screen:

```
java.lang.IllegalStateException: Asking for intrinsic measurements of SubcomposeLayout layouts is not
supported. This includes components that are built on top of SubcomposeLayout, such as lazy lists,
BoxWithConstraints, TabRow, etc. ...
  at androidx.compose.ui.node.LayoutNode$NoIntrinsicsMeasurePolicy.maxIntrinsicWidth(LayoutNode.kt:689)
```

Because the empty host crashes too, the crash is not caused only by the card's `BoxWithConstraints`
that plan 29-07 added: `FeedbackDeck.kt:114` has used a `BoxWithConstraints` since feat 26-02 (the deck
is new in v2.8.0; neither file exists in v2.7.1). So moving the card's bound into measure (the review's
suggested fix) would not by itself make the host intrinsic-safe; the deck's own `BoxWithConstraints`
would have to change too. Either is a `src/main` edit and would invalidate the Gate-1 SC3 device
evidence, so none was made. The orchestrator decides: (a) accept and document "do not host
`FeedbackStackHost` under an intrinsic-size parent" (it is a decks-in-an-overlay component and the
harness apps do not do this), (b) fix deck and card in a later phase with a Gate-1 re-run, or (c) add
the test as a known-failing guard after the fix.
**Evidence kept (untracked):**
- `.planning/phases/29-docs-v2-8-0-cut-readiness/.gate/FeedbackStackHostIntrinsicSizeTest.kt.WR01-red`
  (the test, ready to move into `src/test/.../feedback/` once the source is fixed)
- `.planning/phases/29-docs-v2-8-0-cut-readiness/.gate/WR01-failure-evidence.txt`

### IN-01: `shape` accepted by `EmphasisActionButton` but only honoured for `Text`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt:215`
**Reason:** Deferred. Documentation/parameter-order cleanup in `src/main`; any `src/main` edit
invalidates the Gate-1 SC3 device evidence for the exact tree. The function is private and the
behaviour is correct (WR-05 now pins the Text-branch forwarding). Revisit in the next library change.
**Original issue:** `shape` is silently ignored for Filled, Tonal and TextSmall; parameter order differs
from the public button.

### IN-02: Single-line capsule test is largely self-referential

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCardUndoShapeTest.kt:200-211`
**Reason:** Deferred, not trivial. The measured assertion the review asks for needs the inner button
height from a node inside the `minimumInteractiveComponentSize` wrapper, and the existing tagged node
includes that wrapper; there is no tag on the inner button and adding one is a `src/main` change. The
pure-math check is kept as is (it duplicates `CappedPillShapeTest`); the wrapped-label measured tests
cover the real geometry.
**Original issue:** `painted` uses the same formula as `cappedPillCapPx`, so drift in the real
`TextButton` minimum height would not be seen.

### IN-05: Undo scrolls out of reach inside a scrolled tall card

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt:277-292`
**Reason:** Deferred. The review itself rates it acceptable for the cut (touch-only gap, TalkBack has the
custom action); pinning Undo outside the scrolled region is a `src/main` layout change that would
invalidate the Gate-1 device evidence. Candidate for a later release; the STYLE.md section 5 note was
left alone to keep this pass to tools, tests and API.md.
**Original issue:** Undo is part of the scrolled row, so on a tall scrolling card it is off-screen until
the user scrolls back up.

## Verification

Verification ran in the main checkout (no worktree, `workflow.use_worktrees` not used for this run),
branch `main`, one Gradle invocation at a time with `--no-daemon`.

| Check | Result |
|---|---|
| `bash tools/test/run-all.sh` | exit 0, all fixtures pass (junit-fresh 40, publish-range 92, tester-restored 82) |
| `FeedbackCardUndoShapeTest` | 11 tests, 0 failures, 0 skipped |
| `FeedbackCardTallContentTest` | 13 tests, 0 failures, 0 skipped |
| `CatalogDocsDriftGuardTest` | 16 tests, 0 failures, 0 skipped |
| `./gradlew --no-daemon detekt` | BUILD SUCCESSFUL, zero baseline |
| `bash tools/verify-api-source-additive.sh v2.7.1` | `SEAMS: none`, `API SOURCE-ADDITIVE OK ... accepted_exceptions=0` |
| `api.txt` and `src/main` vs 647f9e6 | no diff |

The full forced-fresh battery was not run; plan 29-06 does that.

---

_Fixed: 2026-10-18_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
