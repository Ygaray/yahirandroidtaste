---
phase: 10-voice-command-settings-surfaces
reviewed: 2026-09-30T00:00:00Z
depth: standard
files_reviewed: 16
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ClearableTextField.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerEntry.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ExplorerIndexScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ApproachRungUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/KeyFieldState.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ModelOptionUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProviderOptionUiModel.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/DomainVocabularyDriftGuardTest.kt
findings:
  critical: 0
  warning: 6
  info: 3
  total: 9
status: issues_found
---

# Phase 10: Code Review Report

**Reviewed:** 2026-09-30
**Depth:** standard
**Files Reviewed:** 16
**Status:** issues_found

## Summary

Reviewed all source + test files changed across Plan 01 (`ProviderKeyCard` tracer) and Plan 02
(`ModelSelectCard` + `ApproachLadderCard`) of Phase 10. Cross-checked the `api.txt` diff (confirmed
strictly additive), the `ComponentRegistry`/`DomainVocabularyDriftGuardTest` wiring, and — per the
explicit security ask — traced `keyValue`/`KeyFieldState` end-to-end for INV-01 compliance.

**INV-01 holds.** `ProviderKeyCard.keyValue` is a plain hoisted `String` parameter; no `Log.*` call
exists anywhere in the diff; the only local state the card owns (`revealed` via `rememberSaveable`)
is a `Boolean` UI-toggle flag, never the key itself; `KeyFieldState` is a render-only sealed
interface that stores no key material. No model in `model/` carries a `var`, so the
Compose-STABLE/`copy()` trap the plan called out does not recur. No hardcoded secrets, no
`eval`/injection-shaped sinks, no unsafe deserialization — this is a pure presentational Compose
library slice with no I/O.

No Critical/blocker findings. The Warnings below are real correctness/robustness/coverage gaps —
none crash or leak data, but several create a documented-contract-vs-enforced-behavior gap
(`ApproachLadderCard`'s cap control), a silent-no-op edge case, an untested/unaffordanced UI state,
dead API surface frozen into the immutable v2.4.0 tag, and a test whose name over-claims what it
verifies.

## Warnings

### WR-01: `ApproachLadderCard`'s cap-control clickability is gated on a different prop than its documented contract

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:64-65,83`
**Issue:** The KDoc states `maxTierId`/`onMaxTierChange` "must be non-null exactly when" the other
is, and that a `null` pair hides the cap control entirely (no rung clickable). But the actual gate
for clickability is `onCapClick = onMaxTierChange?.let { callback -> { callback(rung.id) } }` —
it checks ONLY `onMaxTierChange`, never `maxTierId`. Contrast this with the offline-only toggle two
lines below, which correctly requires both: `if (offlineOnly != null && onOfflineOnlyChange != null)`.
If a caller violates the documented pairing (passes `onMaxTierChange` non-null while `maxTierId` is
`null`), every rung silently becomes clickable — invoking the callback — with `capRank` still
`Int.MAX_VALUE` (no rung shows "Capped"), i.e. a functioning-but-invisible cap control. No test
exercises this mismatched-pair path; every test in `ApproachLadderCardTest` passes the two props as
a matched pair.
**Fix:**
```kotlin
onCapClick = if (maxTierId != null && onMaxTierChange != null) {
    { onMaxTierChange(rung.id) }
} else {
    null
}
```

### WR-02: An unmatched `maxTierId` silently disables the cap instead of failing loudly

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:65`
**Issue:** `val capRank = maxTierId?.let { id -> ladder.firstOrNull { it.id == id }?.rank } ?: Int.MAX_VALUE`
collapses two different situations into the same result: "no cap requested" (`maxTierId == null`)
and "cap id doesn't match any rung in `ladder`" (stale id after the ladder list changed, or a typo).
In the second case the cap control still renders as active with no visible cap marker on any rung
(`capRank` == `Int.MAX_VALUE` either way) — a caller-side bug (stale `maxTierId`) becomes invisible
instead of surfacing.
**Fix:** Distinguish the two cases, e.g. log nothing (library holds no logging per INV-01) but at
minimum fall back visibly, or `require`/assert in debug builds that a non-null `maxTierId` resolves
to a rung in `ladder`:
```kotlin
val capRank = maxTierId?.let { id ->
    ladder.firstOrNull { it.id == id }?.rank
        ?: error("ApproachLadderCard: maxTierId '$id' does not match any rung in `ladder`")
} ?: Int.MAX_VALUE
```
(or, if silent tolerance is intentional, document why explicitly rather than relying on the `?:`
side effect being read correctly by future maintainers).

### WR-03: A merely-`disabled` rung (`enabled = false`) has no visible affordance at all

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:75-77,128-149`; untested in `ApproachLadderCardTest.kt`
**Issue:** `RungRow` only renders explanatory text for two of the three "ineffective" causes:
`"Capped"` when `isCapped`, `"Needs network"` when `needsNetwork`. A rung that is ineffective purely
because `rung.enabled == false` gets the same dimmed `onSurfaceVariant` text color as the other two
cases but NO explanatory label — a user sees a dimmed row with zero indication of why. This breaks
the "conditional-render-no-dead-space" convention this card's own KDoc invokes for the other two
cases, and this path has zero test coverage: no fixture or test in either
`ApproachLadderCardTest.kt` or `VoiceCommandFamilyScreen.kt`'s fixtures ever sets
`enabled = false`.
**Fix:** Add a third affordance label (e.g. `"Unavailable"`) rendered when `!rung.enabled`, and add
a test case asserting it.

### WR-04: Dead public model fields frozen into the immutable v2.4.0 API surface

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt:118-127`; `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:128-149`
**Issue:** `ModelOptionUiModel.subtitle` and `ModelOptionUiModel.badge` are declared and documented
("a short capability summary", "e.g. 'New' or a context-window size") but `ModelDropdown`'s
`DropdownMenuItem` renders only `option.label` — `subtitle`/`badge` are read nowhere in the diff.
Likewise `ApproachRungUiModel.description` is declared/documented but `RungRow` never reads
`rung.description`. These three fields ship as part of a one-way-frozen (D-04) public data class
with no behavior behind them yet — any consumer populating them today gets silently dropped
content, and per D-04/CLAUDE.md's reversibility note, fixing the *rendering* gap later is easy, but
the fields themselves cannot be removed without a breaking change once tagged.
**Fix:** Either wire the fields into their cards now (render `subtitle`/`badge` in the dropdown
item, `description` under the rung label) or drop the unused fields from this phase's frozen shape
and add them additively later when a consumer actually needs them.

### WR-05: `ProviderKeyCard` has no empty-`providers` handling, unlike its sibling `ModelSelectCard`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:55-98,107-142`; untested in `ProviderKeyCardTest.kt`
**Issue:** `ModelSelectCard` explicitly handles `models.isEmpty()` with a disabled+reason caption
(VSET-02's own must-have). `ProviderKeyCard` has no analogous branch for `providers.isEmpty()`: if
called with an empty list, `ProviderDropdown` still renders a normal-looking, tappable
`ExposedDropdownMenuBox` anchor with a blank label and an empty (contentless) menu when tapped — no
explanatory text, unlike its sibling card's explicit empty-state convention. `ProviderKeyCardTest`
never exercises `providers = emptyList()`.
**Fix:** Either accept an `emptyReason`-style param mirroring `ModelSelectCard`'s convention for
this case, or document explicitly why `ProviderKeyCard` is exempt from the empty-list convention
`ModelSelectCard` established one plan later in the same phase.

### WR-06: `ProviderKeyCardTest`'s "sets the field's error state" test doesn't assert any error state

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardTest.kt:168-184`
**Issue:** The test named `` `an Invalid keyState renders the reason and sets the field's error state` ``
only asserts `composeTestRule.onNodeWithText("Key rejected by provider").assertExists()`. It never
asserts anything about the field's actual error/semantics state (e.g. that `isError` propagated to
`OutlinedTextField`, via a content-description, error semantics property, or similar). A regression
that kept rendering the reason text as a plain caption while dropping
`isError = invalidState != null` (`ProviderKeyCard.kt:90`) would pass this test undetected, despite
the test's own name and this plan's `<behavior>` spec both explicitly claiming error-state coverage.
**Fix:** Add an assertion on the field's error semantics, e.g.:
```kotlin
val node = composeTestRule.onNodeWithTag("provider_key_card_key_field").fetchSemanticsNode()
assertTrue(node.config.contains(SemanticsProperties.Error))
```

## Info

### IN-01: `ComponentRegistry`'s class-doc entry count is now stale

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt:18-31`
**Issue:** The class KDoc's audited count ("53 registered, 4 intentionally unregistered = 57
total") was not updated when this phase appended `voiceCommandFamilyEntries` (3 new entries:
`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`) to the `entries` concatenation
(`ComponentRegistry.kt:94-103`). The comment now undercounts the live registry by 3. Not a
functional defect — the `init` block's duplicate/overlap checks are computed live, not from this
comment — but misleading for the next auditor performing the "recount" ritual this comment itself
describes.
**Fix:** Bump the count comment (56 registered / 4 unregistered = 60 total) or note explicitly that
per-phase deltas are not tracked here going forward.

### IN-02: No horizontal spacing between the reveal-eye and clear-✕ `IconButton`s

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ClearableTextField.kt:73-98`
**Issue:** The trailing-icon `Row` added for the reveal-eye + clear-✕ pair has no
`horizontalArrangement = Arrangement.spacedBy(...)`. Each `IconButton` supplies its own internal
padding, so this is likely fine visually, but this plan's own deferred `<verification>` item
("masked-key reveal affordance ... reviewed in `ExplorerActivity`, light + dark") is the right place
to confirm the two icons don't visually crowd each other.
**Fix:** If Gate-1 review finds the icons crowded, add `horizontalArrangement = Arrangement.spacedBy(4.dp)` (or similar) to the `Row`.

### IN-03: Unmatched `selectedProviderId`/`selectedModelId` silently renders a blank dropdown anchor

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:114`; `src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt:102`
**Issue:** Both `ProviderDropdown` and `ModelDropdown` resolve `selectedLabel` via
`options.firstOrNull { it.id == selectedId }?.label ?: ""` — a `selectedId` that doesn't match any
option in the supplied list (stale id, typo, catalog changed under the caller) silently renders a
blank anchor rather than surfacing the mismatch in any way. Consistent between both cards, low risk
since ids are caller-supplied and validated at the caller's own layer, but neither card's test
suite exercises this edge case.
**Fix:** No action required unless Gate-1/consumer integration surfaces this as confusing; consider
a debug-only `check()` if it proves to be a recurring integration bug source.

---

_Reviewed: 2026-09-30_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
