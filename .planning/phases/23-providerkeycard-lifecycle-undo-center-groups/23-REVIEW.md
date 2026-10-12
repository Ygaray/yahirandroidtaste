---
phase: 23-providerkeycard-lifecycle-undo-center-groups
reviewed: 2026-10-12T00:00:00Z
depth: standard
files_reviewed: 23
files_reviewed_list:
  - api.txt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyDefaults.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoGroupRowUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProviderKeyActions.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProviderKeyLabels.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/SavedKeyUiModel.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardDefaultParityTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardLifecycleTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardLongLabelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardPerProviderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardRawKeySemanticsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardSourceCompatV251Test.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardSourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterRowsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenDefaultParityTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenGroupsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenLongLabelTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenSourceCompatV251Test.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenSourceContractTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/model/ProviderKeyModelsTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/testsupport/SemanticsDump.kt
findings:
  critical: 0
  warning: 4
  info: 6
  total: 10
status: issues_found
---

# Phase 23: Code Review Report

**Reviewed:** 2026-10-12
**Depth:** standard
**Files Reviewed:** 23
**Status:** issues_found

## Summary

Reviewed the ProviderKeyCard saved-key lifecycle (summary vs field mode, Update/Clear/Save/Cancel, dropdown
saved marker), the UndoCenterScreen group rows (`buildUndoCenterRows`, header, Undo all, indent), the new
model types, `api.txt`, and the 15 test files plus the shared `SemanticsDump` helper.

The core contracts hold up. In Summary mode `ClearableTextField` is a real `if/else` branch, so the raw
`keyValue` is never composed and cannot appear in any semantics root (T-23-01 is genuinely proven, with a
non-vacuous positive control). The default (empty `savedKeys`, `actions == null`; empty `groups`) paths are
structurally unchanged and pinned by goldens. `api.txt` matches the source signatures. The only existing
consumer call site of `UndoCenterScreen` (SecondBrain `AppNavGraph.kt:869`) uses named arguments, so the
`groups`/`onUndoGroup` insertion before `onClear` does not break it. `./gradlew detekt --offline` is green.

No critical issues. The warnings are about lifecycle edge cases the card cannot protect against by itself
(unscoped `editingSavedKey`, sticky reveal, a dropped error), plus one LazyColumn key collision that can crash.

## Warnings

### WR-01: `editingSavedKey` is not provider-scoped, and the dropdown stays live during editing and `inFlight`

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:105-122` (and the
`editingSavedKey`/`inFlight` KDoc at lines 75-76, `ProviderKeyActions.kt:19`)
**Issue:** `editingSavedKey` is a single card-wide boolean, while `savedKeys` is per provider. If the user taps
Update for provider A (consumer sets `editingSavedKey = true`) and then picks provider B from the dropdown, B
also renders the key field plus Save instead of its own summary. The typed text (and the consumer's `keyValue`)
then sits under B, so a Save can write the key meant for A under B. The dropdown is also never disabled while
`actions.inFlight` is true, although the KDoc says `inFlight` "disables every action": the user can switch
provider mid-save, which flips the body between field and summary under an in-progress request. Nothing in the
KDoc tells the consumer that it must reset `editingSavedKey` on provider change. The tests
(`ProviderKeyCardPerProviderTest`) never combine `editingSavedKey = true` with a provider switch, so this path
is unproven.
**Fix:** Disable the provider dropdown while a save is in flight, and document the reset requirement.
```kotlin
// ProviderDropdown: add `enabled: Boolean` and pass `actions?.inFlight != true`
.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled)
// OutlinedTextField(..., enabled = enabled, ...)
```
```text
 * @param editingSavedKey ... Applies to whichever provider is selected: the consumer must set it back to
 *   `false` when [onProviderSelected] fires.
```
Add a test: edit A, select a saved B, assert the contract you want (B shows its summary only if the consumer resets).

### WR-02: Reveal state is sticky across provider and mode switches, and survives process death

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:118`
**Issue:** `var revealed by rememberSaveable { mutableStateOf(false) }` lives at card level, outside the
Summary/Field branch. If the user reveals a key, saves, then taps Update (or switches to another unsaved
provider whose `keyValue` is stale), the new field comes up already in plaintext. Because it is
`rememberSaveable`, the "revealed" state is also restored after process death or rotation. This is a secret-
exposure hygiene gap (shoulder-surfing) in a card whose whole purpose is not to expose keys. It is deliberate
(`ProviderKeyCardPerProviderTest` `the reveal flag survives a summary and field flip` and the source-contract
test pin the declaration order), so it is a design defect that the tests lock in, not an accident.
**Fix:** Reset the reveal whenever the body identity changes, and update the pinning test.
```kotlin
val mode = providerKeyMode(savedKeys, selectedProviderId, editingSavedKey)
var revealed by rememberSaveable(selectedProviderId, mode) { mutableStateOf(false) }
```

### WR-03: A `KeyFieldState.Invalid` reason is silently dropped in Summary mode

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:120-124` (error read
only at line 125, inside the `else` branch)
**Issue:** `invalidState` is computed only for the Field branch. If a consumer reports a failure while a summary
is showing (for example the provider rejects the saved key, or Clear fails and the consumer surfaces
`Invalid("Couldn't clear key")`), the card shows an unchanged "Saved · fingerprint" with no error at all.
`ProviderKeyCardRawKeySemanticsTest` renders exactly this combination and only asserts the key is absent; no
test checks that the failure is visible. The project's UX rule is that failures must be loud.
**Fix:** Render the reason under the summary row, or document that Summary mode ignores `keyState`.
```kotlin
(keyState as? KeyFieldState.Invalid)?.let {
    Text(it.reason, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}
```

### WR-04: Group header key `group-<id>` can collide with an entry id and crash the LazyColumn

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt:200-204` (KDoc claim at 171)
**Issue:** `undoCenterRowKey` returns `"group-${groupId}"` for a header and the raw `entry.id` for an entry.
`UndoHistoryEntry.id` is a plain `String` on a public type, and `UndoCenterRowsTest` constructs entries with
arbitrary ids. An entry whose id is `group-g` next to a group `g` yields two identical keys, and `LazyColumn`
throws `IllegalArgumentException: Key ... was already used`. `buildUndoCenterRows` documents that "row keys stay
unique", but the test only covers `groupId == entryId`, which the prefix does handle. The store itself uses
UUIDs, so this needs a hand-built entry; the crash risk is low-probability but the documented guarantee is false.
**Fix:** Namespace both kinds so they can never meet.
```kotlin
internal fun undoCenterRowKey(row: UndoCenterRow): String = when (row) {
    is UndoCenterRow.Group -> "g:${row.group.groupId}"
    is UndoCenterRow.Entry -> "e:${row.entry.id}"
}
```
Add a test with an entry id of `group-g` beside group `g`.

## Info

### IN-01: Positional four-argument `UndoCenterScreen` call no longer compiles

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt:80-87`
**Issue:** Inserting `groups`/`onUndoGroup` before the required trailing `onClear` breaks any positional
`UndoCenterScreen(e, b, u, c)` call (the 4th argument would bind to `groups`). This conflicts with the hub rule
"existing call sites must keep compiling". It is a documented, owner-accepted decision (D-06, KDoc, and the
canary test comment), and the only known consumer (SecondBrain) uses named arguments, so no action is needed
now. Recorded so the trade-off stays visible when the next consumer repins.
**Fix:** None required. If positional callers appear, there is no source-compatible alternative given the
trailing-lambda rule; note it in `API.md`/`CHANGELOG`.

### IN-02: Group header text is English-only and naively pluralized

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreen.kt:206-229`
**Issue:** `groupCountText`/`groupStatusText` hard-code "N items", "Undoable", "Partly resolved", "Resolved",
while the same phase makes every ProviderKeyCard string caller-localizable and the project keeps a
long-German/Spanish layout rule. Only `undoAllLabel` is overridable, so a localized app gets a half-translated
header. The "1 item"/"N items" rule is also wrong for languages with other plural classes. This is stated as
accepted under D-06, so it is Info.
**Fix:** Defer to a later `UndoGroupLabels`-style holder, or add optional `countText: (Int) -> String` and
`statusText` fields appended with defaults to `UndoGroupRowUiModel`.

### IN-03: Clear is a one-tap destructive action with no documented confirmation contract

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:254-262`,
`src/main/java/io/github/ygaray/yahirandroidtaste/model/ProviderKeyActions.kt:17`
**Issue:** Clear (Destructive role) invokes `onClear` immediately, next to Update, and the `ProviderKeyActions`
KDoc does not say that the consumer must confirm or offer undo. The hub's convention is reveal-confirm for
destructive actions.
**Fix:** Add a KDoc line: "The card does not confirm; wrap `onClear` in a confirmation dialog or undo snackbar."

### IN-04: Long-label tests include a tautological check and miss the realistic truncation case

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardLongLabelTest.kt:112-115`,
`src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenLongLabelTest.kt:95-98`
**Issue:** `layout.size.width <= layout.layoutInput.constraints.maxWidth` is always true for a laid-out text
(the layout is clamped to its constraints), so it proves nothing; only `isLineEllipsized` carries weight. The
summary uses a 20-character fingerprint, while `Text(maxLines = 2, overflow = Ellipsis)` at
`ProviderKeyCard.kt:229-235` truncates the end of the string, which is where the distinguishing fingerprint
suffix (`…abcd`) lives. A longer fingerprint at 360dp would hide exactly the part that identifies the key, and no
test covers it.
**Fix:** Drop the tautological assertion. Add a case with a ~48-character fingerprint and either allow more
lines or use `TextOverflow.StartEllipsis`-style handling (or ellipsize the label and keep the fingerprint).

### IN-05: Small duplication and token misuse in ProviderKeyCard

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt:119-122, 217, 239, 272`
**Issue:** `saved` is looked up, then `providerKeyMode(...)` repeats the same null/editing check, so the
Summary condition lives in two places. `Arrangement.spacedBy(Dimens.TopPadding)` uses a vertical-named token
for horizontal icon spacing, and `Dimens.ContentSpacing * 2` is a repeated magic expression in two rows. The
`savedKeys` KDoc says `providerId -> fingerprint` although the value type is `SavedKeyUiModel`. The class doc
of `ProviderKeyCardLifecycleTest` still says "Later tasks append..." (stale). The main `ProviderKeyCard` body is
about 60 lines, right at the detekt LongMethod threshold (currently passing).
**Fix:** Branch on `providerKeyMode(...)` and read `saved` inside the Summary arm; hoist
`ContentSpacing * 2` into one private constant; correct the KDoc wording.

### IN-06: Source-text and pixel-golden tests are brittle

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardSourceContractTest.kt:21-22, 61`,
`src/test/java/io/github/ygaray/yahirandroidtaste/feedback/UndoCenterScreenSourceContractTest.kt:19-21`,
`src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardDefaultParityTest.kt:276-285`
**Issue:** Several assertions parse source text (whitespace-stripped substring matches, "exactly four
`DynamicActionButton(` calls", "exactly two `fun ProviderKeyCard(`", a path relative to the working directory).
Any refactor that keeps behavior but changes shape (a helper, a rename, an extra button) fails them, and the
second one also locks in the v2.4.x HIDDEN shim that the project says not to grow. The bounds goldens
(`h=189`, `h=190`) depend on exact font metrics and will drift on a Compose/Material bump without any
behavior change. They are deterministic today, so this is maintenance cost, not a bug.
**Fix:** Prefer behavior assertions (semantics, enabled state, role) over source matching where the same fact
is already covered by `ProviderKeyCardLifecycleTest`; keep the golden bounds but add a comment on how to
regenerate them.

---

_Reviewed: 2026-10-12_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
