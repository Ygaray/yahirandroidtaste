---
phase: 23-providerkeycard-lifecycle-undo-center-groups
fixed_at: 2026-10-12T17:30:00Z
review_path: .planning/phases/23-providerkeycard-lifecycle-undo-center-groups/23-REVIEW.md
iteration: 1
findings_in_scope: 10
fixed: 7
skipped: 3
status: resolved
---

# Phase 23: Code Review Fix Report

**Fixed at:** 2026-10-12
**Source review:** .planning/phases/23-providerkeycard-lifecycle-undo-center-groups/23-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 10 (4 Warning, 6 Info; fix_scope = all)
- Fixed: 7 (IN-04 and IN-06 fixed in part, see notes)
- Skipped: 3 (all documented acceptable-skips, no open blocker/critical/high)

**Verification:** run in the main checkout on `main` (no worktree, per the session constraint), not an
isolated worktree, so the numbers are reproducible from the tree. Before every commit batch and again
on the final state: `./gradlew testDebugUnitTest detekt apiCheck --offline` was green (zero-baseline
detekt, apiCheck green, all default-parity goldens unchanged). No public API changed, so `api.txt` was
not regenerated. Commits that touch `src/main` used `HUB_LANE_OVERRIDE=2`; test-only commits are lane 1.

## Fixed Issues

### WR-04: Group header key `group-<id>` can collide with an entry id

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterRowsTest.kt`
**Commit:** 54f6566
**Applied fix:** both row kinds are namespaced (`g:<groupId>`, `e:<entryId>`), so they can never meet. The key test was updated to the new strings and a regression test builds an entry whose id is `group-g` (and one `g:g`) beside group `g`.

### WR-01: `editingSavedKey` not provider-scoped; dropdown live during `inFlight`

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt`, `src/main/java/io/github/ygaray/yahirandroidtaste/model/ProviderKeyActions.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardPerProviderTest.kt`
**Commit:** e422f3e
**Status:** fixed: requires human verification (behavioral change on a disabled `ExposedDropdownMenuBox`)
**Applied fix:** the private `ProviderDropdown` takes `enabled = actions?.inFlight != true` (anchor, text field and menu expansion), so the provider cannot change mid-save. With `actions == null` the dropdown is enabled exactly as before (goldens unchanged). KDoc on `editingSavedKey` now says it is card-wide and the consumer must reset it (and clear the typed key) when `onProviderSelected` fires; `inFlight` KDoc mentions the dropdown. Public API shape unchanged. A test proves a tap opens no menu while in flight; the enabled control is the existing `dropdown marks saved providers...` test. (A two-click enabled-after-disabled test hit a Robolectric legacy-graphics bitmap NPE, so it was reduced to one click.)

### WR-03: `KeyFieldState.Invalid` reason dropped in Summary mode

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardRawKeySemanticsTest.kt`
**Commit:** 4371bd6
**Applied fix:** `SavedKeySummary` gets `errorReason` and renders it as an error-colored `bodySmall` line (tag `provider_key_saved_error`) between the summary row and the button row, only when `keyState` is `Invalid`. CONTEXT.md D-03 only forbids the raw `keyValue` rendering, not an error reason. Tests assert the reason shows, the summary node stays the one merged fingerprint, no line is rendered otherwise, and the raw-key scan still passes.

### IN-03: Clear is one-tap destructive with no documented confirmation contract

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/ProviderKeyActions.kt`
**Commit:** 63a1ea7
**Applied fix:** `onClear` KDoc states the card does not confirm and the consumer should wrap it in a confirmation dialog or undo snackbar.

### IN-05: Duplication and token misuse in ProviderKeyCard

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardLifecycleTest.kt`
**Commit:** 2cb7702
**Applied fix:** the body now branches once on `providerKeyMode(...)` and reads the saved entry inside the Summary arm (the reveal declaration still precedes it, so the source-contract test holds). The horizontal 8dp gaps use two private named values instead of the vertical `Dimens.TopPadding` and a repeated `ContentSpacing * 2` (same 8dp, bounds goldens unchanged). `savedKeys` KDoc now names `SavedKeyUiModel`. The stale "Later tasks append..." class doc in `ProviderKeyCardLifecycleTest` was corrected. The LongMethod concern was left alone (detekt passes at zero baseline).

### IN-04: Long-label tests include a tautological check (partial)

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardLongLabelTest.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenLongLabelTest.kt`
**Commit:** 936048f
**Applied fix:** removed the always-true `layout.size.width <= maxWidth` assertion from both tests and updated their KDoc. The second half of the finding (a ~48-character fingerprint truncating its distinguishing suffix) was not addressed: it needs a product decision on the ellipsize policy for the summary (more lines vs start-ellipsis), a main-source behavior change beyond a minimal fix. Fingerprints are consumer-shortened (`sk-ant-…abcd`). Left for the owner if a long fingerprint is ever expected.

### IN-06: Source-text and pixel-golden tests are brittle (partial)

**Files modified:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardDefaultParityTest.kt`
**Commit:** 5525b5c
**Applied fix:** added the suggested comment on how to regenerate the bounds goldens (the assertion message already prints a paste-ready dump). The "prefer behavior assertions over source matching" part was not applied: the source-contract tests are plan must-haves (23-01-PLAN.md) and replacing them would weaken pinned contracts, including the reveal-before-branch order that WR-02 below depends on.

## Skipped Issues

### WR-02: Reveal state is sticky across provider and mode switches

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:118`
**Reason:** documented acceptable-skip. The reveal flag surviving a Summary/Field flip and a provider switch is the signed contract, not an accident: 23-01-PLAN.md must-have ("the reveal flag is declared before the Summary/Field branch so it survives a Summary/Field flip, as in v2.5.1 it survived a provider switch", VKEY-02, ROADMAP SC3), pinned by `ProviderKeyCardPerProviderTest` `the reveal flag survives a summary and field flip` and `ProviderKeyCardSourceContractTest`. Re-keying on `selectedProviderId`/mode would change v2.5.1 default behavior (reveal persisting across a provider switch) and break that contract. If the owner wants the stricter secret-exposure hygiene, it is a new decision that supersedes SC3.
**Original issue:** `rememberSaveable { mutableStateOf(false) }` sits outside the Summary/Field branch, so a revealed key stays revealed after Update or a provider switch and across process death.

### IN-01: Positional four-argument `UndoCenterScreen` call no longer compiles

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt:80-87`
**Reason:** documented acceptable-skip. Owner-accepted decision D-06 (reversibility: costly, adopted by SecondBrain on repin); the reviewer itself says no action is needed and the only known consumer uses named arguments.
**Original issue:** inserting `groups`/`onUndoGroup` before the required trailing `onClear` breaks positional four-argument calls.

### IN-02: Group header text is English-only and naively pluralized

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt:206-229`
**Reason:** documented acceptable-skip. The suggested fix appends new fields to `UndoGroupRowUiModel`, but D-06 (with D-01's one-way sign-off) locks that model to `(groupId, label, memberIds, status, undoAllLabel)` and states the English-only header text as library text. Changing the signed shape is outside a review fix; a localization holder belongs in a later phase.
**Original issue:** `groupCountText`/`groupStatusText` hard-code English and a naive plural rule while only `undoAllLabel` is overridable.

---

_Fixed: 2026-10-12_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
