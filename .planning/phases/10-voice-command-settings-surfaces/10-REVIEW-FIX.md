---
phase: 10-voice-command-settings-surfaces
fixed_at: 2026-09-30T00:00:00Z
review_path: .planning/phases/10-voice-command-settings-surfaces/10-REVIEW.md
iteration: 1
findings_in_scope: 5
fixed: 4
skipped: 1
status: resolved
---

# Phase 10: Code Review Fix Report

**Fixed at:** 2026-09-30
**Source review:** .planning/phases/10-voice-command-settings-surfaces/10-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 5 (3 warning, 2 info — `fix_scope: all`)
- Fixed: 4
- Skipped: 1 (reviewer-documented acceptable-skip)

All fixes were applied and committed inside an isolated git worktree
(`gsd-reviewfix/10-665081`, created from `main`) and fast-forwarded onto `main` on cleanup. Each
commit required `HUB_LANE_OVERRIDE=2` — the repo's `classify-hub-change.sh` pre-commit guard
classifies any edit to a pre-existing function body as lane 2 ("non-additive" / behavior-changing)
relative to the `v2.3.0` baseline tag, even when the public signature is unchanged (WR-01, WR-02,
IN-02) or only additive-with-a-default (WR-03). This matches the exact precedent already on `main`
for the sibling fix (`d46d7c1`, `fix(11): WR-01/WR-02 …`), which is the same class of change.

## Fixed Issues

### WR-01: The `require()` pairing fix was applied to one prop pair but not its documented twin

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt`
**Commit:** `0f51eff`
**Applied fix:** Added `require((offlineOnly == null) == (onOfflineOnlyChange == null))`, mirroring
the existing `maxTierId`/`onMaxTierChange` guard exactly (same message shape). Updated the
`onOfflineOnlyChange` KDoc to state the pairing is now enforced via `require`. Added two regression
tests (`ApproachLadderCardTest`) mirroring the existing `maxTierId` pairing tests: a non-null
`onOfflineOnlyChange` with a null `offlineOnly` throws, and vice versa.

### WR-02: A merely-`disabled` rung has no visible affordance at all

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt`
**Commit:** `87a0458`
**Applied fix:** `RungRow` now renders an `"Unavailable"` label (same `labelSmall` /
`onSurfaceVariant` styling as `"Capped"`/`"Needs network"`) whenever `!rung.enabled`. Added a test
with a fixture rung set to `enabled = false`, asserting the rung stays visible
(conditional-render-no-dead-space) and carries the new affordance text.

### WR-03: `ProviderKeyCard` has no empty-`providers` handling

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt`, `src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardTest.kt`
**Commit:** `3c5a132`
**Applied fix:** Added an `emptyProvidersReason: String = "No providers configured yet"` param
(defaulted, so both existing call sites — `VoiceCommandFamilyScreen.kt`'s gallery screen and
`ProviderKeyCardTest.kt` — remain source-compatible with no edits needed). `ProviderDropdown` now
renders this caption (mirroring `ModelSelectCard`'s established `emptyReason` convention —
`labelLarge`, `onSurfaceVariant`, `maxLines = 2`, ellipsis) instead of a blank, still-tappable
dropdown anchor when `providers` is empty. Added a test asserting the caption renders for an empty
`providers` list.

### IN-02: Internal review-finding IDs baked into shipped public KDoc

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt`
**Commit:** `390e847`
**Applied fix:** Removed the `(WR-02)` and `(WR-01)` parentheticals from the `maxTierId` and
`onMaxTierChange` `@param` KDoc. The behavioral explanation is unchanged; the "why both pairs are
now enforced identically" history was moved to a code comment directly above the two `require()`
calls instead of living in published API docs. `ProviderKeyCard.kt:47`'s `(WR-04 …)` parenthetical
was already removed incidentally while applying the WR-03 fix (same KDoc paragraph was being
edited) — see commit `3c5a132`.

## Skipped Issues

### IN-01: Unmatched `selectedProviderId` silently renders a blank dropdown anchor

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:115`
**Reason:** Reviewer-documented acceptable-skip — the finding's own **Fix:** guidance states "No
action required unless this proves to be a recurring integration bug source; consider a debug-only
`check()` if it does." No evidence of recurrence was raised in this review cycle, so no change was
made. This is Info-severity, not a blocker/critical/high, and not treated as a gap.
**Original issue:** `val selectedLabel = providers.firstOrNull { it.id == selectedProviderId }?.label ?: ""`
— a `selectedProviderId` that doesn't match any entry in `providers` silently renders a blank
anchor with no signal of the mismatch. Low risk since ids are caller-supplied.

---

_Fixed: 2026-09-30_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
