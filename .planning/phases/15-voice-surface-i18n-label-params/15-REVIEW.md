---
phase: 15-voice-surface-i18n-label-params
reviewed: 2026-10-05T00:00:00Z
depth: standard
files_reviewed: 17
files_reviewed_list:
  - api.txt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBar.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCard.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRefusedUiModel.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/model/UndoRowUiModel.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ClarificationBarTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ModelSelectCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheetTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/ProviderKeyCardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/model/VoiceModelLabelDefaultsTest.kt
findings:
  critical: 0
  warning: 4
  info: 2
  total: 6
status: resolved
---

# Phase 15: Code Review Report

**Reviewed:** 2026-10-05
**Depth:** standard
**Files Reviewed:** 17
**Status:** issues_found

## Summary

The change is mechanically sound. Every new label is appended last, English defaults are byte-identical to the
removed literals (checked each one: "Unavailable", "Capped", "Needs network", "Online", "Offline only", "Dismiss",
"Model", "Provider", "Undone", "Remove", "Escalations: $n", "Couldn't undo: <reason>, <item> changed since"). The
hand-written old-arity `copy` overloads resolve unambiguously in Kotlin (the overload without defaults wins for
positional calls, the full overload wins for any named or partial call). They delegate with the instance's current
label, so no custom label gets reset. `ProposedItemUiModel.toString()` is unchanged and does not leak the new
field. The api.txt diff is additive: no removals, and the old ctor/copy arities are retained. No literal English
strings remain in the five touched composables except the documented SegmentedOptionSelector residual.

Remaining concerns are about what the additive shape does NOT protect (binary compat of synthetic members),
a half-localized accessibility string, and fragile string concatenation. No blockers.

## Warnings

### WR-01: Synthetic `$default` constructor/`copy` members of the four data classes (and the composables' `$default`) are removed; Metalava does not see this

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt:25`
(same shape at `ProposedItemUiModel.kt:36`, `UndoRefusedUiModel.kt:28`, `UndoRowUiModel.kt:37`)
**Issue:** `@JvmOverloads` plus the hand-written old-arity `copy` keep the *public non-synthetic* v2.4.1 JVM
surface, which is all Metalava's `api.txt` tracks. A consumer already compiled against v2.4.x that uses Kotlin
defaults (`HandledByUiModel("Local")`, `item.copy(title = "x")`) links to synthetic members instead:
the `(…, int, DefaultConstructorMarker)` constructor and `copy$default(…, int, Object)`. Those have changed
arity (one more parameter and a different mask) and the old ones no longer exist. Such a consumer fails with
`NoSuchMethodError` until it is recompiled. The same holds for the five composables, whose `$default` and
Composer/changed-bitmask signatures change. The phase success criterion states "binary/source compat", and api.txt
cleanliness is being read as proof of binary compat, which it is not for default-using Kotlin call sites.
Recompile-on-bump (JitPack coordinate bump, ECOSYSTEM.md section 7) makes this tolerable, but only if it is stated.
**Fix:** Do not claim binary compat in the phase/closing-gate docs. Say "source-compatible; consumers must recompile
(Kotlin default-arg synthetic members change)". Note it in the v2.5.0 release notes / INTEGRATION.md repin section.
If true binary compat is required, add a hidden `@Deprecated(level = HIDDEN)` constructor/`copy$default`-equivalent
shim, which is not practical for `$default`. The doc statement is the realistic fix.

### WR-02: `removeContentDescription` is per item, so a partially localized list mixes languages

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/ProposedItemUiModel.kt:41` and
`src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:436`
**Issue:** The label is a field on every `ProposedItemUiModel`. A caller must set it on each item that has
`onRemove`. Any item that forgets it silently announces the English "Remove" in an otherwise localized TalkBack
session. The KDoc admits this ("Set it on every item that supplies onRemove"), which means the API makes the
mistake easy. It also puts a presentation constant into `equals`/`hashCode`/`copy`, so two otherwise identical
items compare unequal if only the description differs. The other labels (`escalationsLabel`, `undoneLabel`) share
this per-row cost.
**Fix:** Accept as the approved design for v2.5 (it is source-additive), but record a follow-up for a sheet-level
parameter (e.g. an `OutcomeSheet(removeItemContentDescription = ...)` overriding the per-item default) so callers
set it once. At minimum add a test that a list mixing a custom and a default description renders each as set.

### WR-03: Hard-coded joining spaces mean empty or punctuation-sensitive labels produce malformed text

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/OutcomeSheet.kt:180` and `:341`
(`"${refused.refusedPrefix} ${refused.reason}$suffix"`, `", $it ${refused.changedSinceSuffix}"`,
`"${handledBy.escalationsLabel} $it"`)
**Issue:** The composable inserts a literal ASCII space and comma between caller-supplied fragments. Consequences:
(a) a blank prefix/label (a plausible choice for "no lead-in") renders a leading space (`" reason"`, `" 3"`);
(b) languages that do not separate with ASCII spaces or use a full-width colon directly attached (Japanese/Chinese,
`元に戻せません：…`) cannot be expressed. The KDoc's "word-order limitation" covers order only, not spacing or the
hard-coded ", " list separator (a non-Latin comma is impossible). The colon is baked into the English defaults
("Couldn't undo:", "Escalations:") but not into `onlineLabel`, which is inconsistent for callers deciding whether
to include punctuation.
**Fix:** Either document explicitly that fragments are joined with a single ASCII space and ", ", or build the
strings so an empty fragment is skipped, for example
`listOfNotNull(refused.refusedPrefix.takeIf { it.isNotEmpty() }, refused.reason).joinToString(" ")`.
Also add a one-line KDoc note that the colon belongs to the label.

### WR-04: Localized segment label is announced with English state words

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/ApproachLadderCard.kt:132` (consumes
`SegmentedOptionSelector.kt:72`: `"$label, ${if (selected) "selected" else "not selected"}"`)
**Issue:** `onlineLabel`/`offlineOnlyLabel` are localized, but the segment's `contentDescription` overrides the
visible text and always appends English "selected"/"not selected". The tests even pin `"En ligne, selected"`.
A French TalkBack user hears "En ligne, selected". The phase is about caller-localizable text, and this
mixed-language string is the accessibility surface for the very labels just made localizable. It is documented as
a residual, but it is a user-visible defect in the feature being delivered, and the test locks the defect in as
expected behavior.
**Fix:** Either replace the manual `contentDescription` with proper semantics (`Role.RadioButton` plus
`selected = ...` state, which Material3 `SegmentedButton` already provides, so TalkBack localizes the state words
itself), or add `selectedStateLabel`/`notSelectedStateLabel` params to `SegmentedOptionSelector`. Track as a
follow-up requirement if out of scope for VI18N-01..04, and do not pin the English state words in the new tests.

## Info

### IN-01: Vacuous assertions in the source-compat test

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceI18nSourceCompatTest.kt:63-66`
**Issue:** `assertNotNull(providerKeyCard)` etc. assert on a lambda that was just assigned a non-null literal, so
they can never fail. The value of the test is that it compiles; the assertions add noise and could mislead a
reader into thinking runtime behavior is checked. Also the test never exercises the composables positionally
through the NEW trailing parameter or confirms the default-label rendering of the positional shapes (covered
elsewhere only for the named shapes).
**Fix:** Drop the `assertNotNull` lines (keep the compile-only lambdas with a comment), or replace them with a
`@Suppress("UNUSED_VARIABLE")` note.

### IN-02: Same five-line old-arity `copy` + doc comment is hand-duplicated across four models; no guard against drift

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/model/HandledByUiModel.kt:30-49` (and the other three)
**Issue:** Each model carries a hand-written parameter-by-parameter copy of the shipped `copy` signature. The next
appended field (v2.6+) must update the compat `copy` in lock-step or the compat copy will silently drop the new
field, because it forwards only the named subset and the new field falls back to the instance value, which is
correct only if the author remembers to add it to the delegation. The reflection tests only check arities, not that
the compat `copy` carries every field forward. `equals`/`hashCode` of `HandledByUiModel`/`UndoRowUiModel` also now
include the label, so deduplication/diffing on those models changes semantics when labels differ, which is
documented only for `UndoRowUiModel` in a test.
**Fix:** Add a reflection-based test per model that builds an instance with every field set to a non-default
value, calls the legacy-arity `copy`, and asserts `copy == original` for the untouched fields. This catches the
next forgotten field automatically.

---

_Reviewed: 2026-10-05_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
