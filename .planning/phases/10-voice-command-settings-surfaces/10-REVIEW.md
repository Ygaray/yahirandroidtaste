---
phase: 10-voice-command-settings-surfaces
reviewed: 2026-09-30T00:00:00Z
depth: standard
files_reviewed: 3
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
findings:
  critical: 0
  warning: 3
  info: 2
  total: 5
status: issues_found
---

# Phase 10: Code Review Report

**Reviewed:** 2026-09-30
**Depth:** standard
**Files Reviewed:** 3
**Status:** issues_found

## Summary

This is a fresh, independent review of exactly three files, scoped to what changed since
`bca91ce` (Phase 11's edits to shared Phase-10 files: commits `d46d7c1` enforcing the
`maxTierId`/`onMaxTierChange` pairing via `require()` and documenting the unmatched-id fallback,
and `3a6dfa4` correcting `ProviderKeyCard`'s KDoc on key-field trimming). Both fixes were verified
correct on their own terms — the `require()` throws exactly when the pairing is violated, and the
trim KDoc now matches the code's actual (and unchanged) behavior.

However, reviewing the current state of these files top-to-bottom (not just the diff hunks)
surfaces one genuinely new problem introduced by the asymmetry of the `d46d7c1` fix (WR-01 below),
a documentation-hygiene regression introduced by both fixes leaking internal review-ticket IDs into
shipped public KDoc (IN-02 below), and three pre-existing gaps in these same files that Phase 11
did not touch and that remain open (WR-02, WR-03, IN-01 — carried forward from the original
`05e6c08` review of this phase, since they live in the files back in scope here).

No critical/security findings. This remains a pure presentational Compose library slice with no
I/O, no logging, and no stored key material (INV-01 still holds — unchanged by this diff).

## Warnings

### WR-01: The `require()` pairing fix was applied to one prop pair but not its documented twin

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:45-47,68-71,96-106`
**Issue:** The `d46d7c1` fix added `require((maxTierId == null) == (onMaxTierChange == null))` so a
mismatched `maxTierId`/`onMaxTierChange` pair now throws immediately instead of silently leaving
every rung clickable with no visible cap (the original WR-01). But the KDoc makes the *identical*
contract claim for the other optional pair:

```
@param offlineOnly ... or `null` to hide the toggle entirely (D-05).
@param onOfflineOnlyChange ... or `null` to hide the toggle entirely (D-05) — must be non-null
  exactly when [offlineOnly] is non-null.
```

Yet the actual gate for that pair is still the soft check at line 96:

```kotlin
if (offlineOnly != null && onOfflineOnlyChange != null) {
    SegmentedOptionSelector(...)
}
```

No `require()` backs this one. A caller that violates the documented pairing (e.g. passes
`offlineOnly = true` but `onOfflineOnlyChange = null`, or vice versa) gets no exception and no
signal that it broke the contract — the toggle is just silently omitted. This is the same class of
"documented contract vs. enforced behavior" gap the `require()` fix was written to close for the
*other* pair, now left standing in the very same function right next to the fix. Reviewers/future
maintainers skimming the function for "is pairing enforced here?" will reasonably assume yes, since
one of the two identical-looking pairs now throws and the other doesn't.
**Fix:** Mirror the same `require()` for the offline pair (or, if divergent behavior is actually
intentional, say so explicitly in both KDoc blocks instead of using the same "must be non-null
exactly when" wording for both):
```kotlin
require((offlineOnly == null) == (onOfflineOnlyChange == null)) {
    "ApproachLadderCard: offlineOnly and onOfflineOnlyChange must both be null or both be " +
        "non-null (got offlineOnly=$offlineOnly, onOfflineOnlyChange=" +
        "${if (onOfflineOnlyChange == null) "null" else "non-null"})"
}
```

### WR-02 (carried forward, unfixed): A merely-`disabled` rung has no visible affordance at all

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:111-159`
**Issue:** `RungRow` still renders explanatory text for only two of the three "ineffective"
causes — `"Capped"` (`isCapped`) and `"Needs network"` (`needsNetwork`). A rung that is
ineffective purely because `rung.enabled == false` (default `true` per
`ApproachRungUiModel.enabled`) gets the same dimmed `onSurfaceVariant` text color as the other two
cases but zero explanatory label — a dimmed row with no indication of why. `ApproachLadderCardTest`
still has no fixture setting `enabled = false` (the shared `ladder` fixture and every test in the
file omit it, relying on the default), so this path remains completely untested as well as
unaffordanced. This is the original `WR-03` finding from the `05e6c08` review, unchanged by the
Phase 11 edits even though they touched this same file.
**Fix:** Add a third affordance label (e.g. `"Unavailable"`) rendered when `!rung.enabled`, and add
a test case asserting it:
```kotlin
if (!rung.enabled) {
    Text(
        text = "Unavailable",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
```

### WR-03 (carried forward, unfixed): `ProviderKeyCard` has no empty-`providers` handling

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:56-99,106-143`
**Issue:** `ProviderDropdown` still renders a normal-looking, tappable `ExposedDropdownMenuBox`
anchor with a blank label and an empty (contentless) menu when `providers` is empty — no
explanatory text, no disabled state, nothing distinguishing "no providers configured yet" from "a
provider is selected but its label happens to be blank." This is the original `WR-05` finding from
the `05e6c08` review; the Phase 11 diff touched only this file's KDoc (the `onKeyChange` trim
comment), not this behavior, so the gap is unchanged.
**Fix:** Either accept an `emptyReason`-style param (mirroring whatever convention a sibling card in
this family uses for its own empty-list case) or document explicitly why `ProviderKeyCard` is
exempt from that convention.

## Info

### IN-01 (carried forward, unfixed): Unmatched `selectedProviderId` silently renders a blank dropdown anchor

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:115`
**Issue:** `val selectedLabel = providers.firstOrNull { it.id == selectedProviderId }?.label ?: ""`
— a `selectedProviderId` that doesn't match any entry in `providers` (stale id, typo, catalog
changed under the caller) silently renders a blank anchor with no signal of the mismatch. Low risk
since ids are caller-supplied, but still untested and unchanged by this diff (original `IN-03` from
`05e6c08`).
**Fix:** No action required unless this proves to be a recurring integration bug source; consider a
debug-only `check()` if it does.

### IN-02: Internal review-finding IDs ("WR-01", "WR-02", "WR-04") are now baked into shipped public KDoc

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:51,56`; `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:47`
**Issue:** Both Phase 11 fixes wrote this project's internal code-review ticket IDs directly into
the public `@param` KDoc of exported, published-to-JitPack API surface:

```
/* ApproachLadderCard.kt:51 */ ... the cap silently falls back to "no cap at all" (WR-02) ...
/* ApproachLadderCard.kt:56 */ ... throws immediately rather than silently leaving every rung
                                   clickable with no visible cap, WR-01).
/* ProviderKeyCard.kt:47    */ ... before this callback fires (WR-04 — not scoped to paste alone;
                                   pure formatting, no validation — INV-01).
```

This KDoc ships as part of the library's generated API documentation and IDE hover text for every
external consumer app that imports `yahirandroidtaste` via JitPack. "WR-01"/"WR-02"/"WR-04" are
this repo's own internal review-finding identifiers (from `10-REVIEW.md`) — they carry no meaning
to a consumer reading the published docs and will drift further out of context as this project's
internal numbering is reused across future phases/reviews. `INV-01` (an invariant ID, documented
elsewhere in the project) is at least more durable/project-wide, but the `WR-*` ones are review-run-
scoped and not meant to be permanent public-facing identifiers.
**Fix:** Keep the behavioral explanation, drop the ticket-ID parentheticals from the public KDoc
(move the "why"/history to a code comment near the `require()`/implementation instead of the
`@param` doc), e.g.:
```kotlin
/**
 * @param onMaxTierChange ... must be non-null exactly when [maxTierId] is non-null; violating this
 *   pairing throws immediately rather than silently leaving every rung clickable with no visible
 *   cap.
 */
```

---

_Reviewed: 2026-09-30_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
