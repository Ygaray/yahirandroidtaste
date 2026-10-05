---
phase: 18-catalog-integrity-api-dump-docs
reviewed: 2026-10-05T00:00:00Z
depth: standard
files_reviewed: 2
files_reviewed_list:
  - API.md
  - INTEGRATION.md
findings:
  critical: 0
  warning: 2
  info: 3
  total: 5
status: issues_found
---

# Phase 18: Code Review Report

**Reviewed:** 2026-10-05
**Depth:** standard
**Files Reviewed:** 2
**Status:** issues_found

## Summary

Reviewed `git diff bee155d..HEAD -- API.md INTEGRATION.md` (new API.md section 10, the v2.5.0
compatibility notes, the INTEGRATION BOM / family-count fixes and the "Localizing the voice surface"
note). Every documented claim was checked against `src/main` and `api.txt`.

Verified correct:

- `ApproachLadderCard` parameter names, order and defaults match the source and `api.txt`
  (`router: Boolean? = null`, `onRouterChange: ((Boolean) -> Unit)? = null`, `routerOnLabel = "Router on"`,
  `routerOffLabel = "Router off"`, and the other labels).
- The router toggle renders below the offline-only toggle. It emits `index == 1` (the target value),
  not a negation. The three `require()` pairs match. Rung `Role.RadioButton` + `selected` and the
  `minimumInteractiveComponentSize` apply only when `onMaxTierChange != null`, and a stale id selects none.
- Appended-field defaults match the source: `escalationsLabel = "Escalations:"`, `undoneLabel = "Undone"`,
  `refusedPrefix = "Couldn't undo:"` (ASCII apostrophe), `changedSinceSuffix = "changed since"`,
  `removeContentDescription = "Remove"` and `FailureActionUiModel.role = Neutral`.
- `Failure.body` renders after the handled-by row and before the action, with no wrapper node when
  null. The generated `Failure.toString` includes `semanticsPrefix` (no override), and
  `ProposedItemUiModel.toString` omits `removeContentDescription`.
- The `ProposedItemUiModel` trailing-lambda caveat is correct. Kotlin binds a trailing lambda to the
  last declared parameter. In v2.5.0 that is `removeContentDescription: String`, so
  `ProposedItemUiModel(id, title) { }` no longer compiles. The hidden 9-argument constructor is
  invisible to Kotlin and cannot rescue it. `trailingContent = { }` is the correct fix. The claim
  that no other appended-last addition has a function-typed former last parameter holds
  (`HandledBy` Int?, `UndoRow` UndoRowState, `UndoRefused` String?, `Failure` FailureActionUiModel?,
  composables `Modifier` / String).
- The `FailureActionUiModel("l") { }` claim holds through the explicit two-argument constructor.
- `Compose BOM 2026.04.01` matches `libs.versions.toml`. "ten-family" matches the registry.
- No consumer names in section 10 or the INTEGRATION localization note.
- There is no secret-handling advice problem in the new text. The `semanticsPrefix` "never sensitive"
  caution is accurate.

Two inaccurate or incomplete claims need fixing.

## Warnings

### WR-01: Section 10 claims every user-visible literal is overridable, but ProviderKeyCard has non-overridable English strings

**File:** `API.md:178-184` (also `INTEGRATION.md:121-134`)
**Issue:** Section 10 states "every user-visible literal is a defaulted, caller-overridable parameter
or model field". The INTEGRATION note says the only non-localizable text is the segmented toggles'
"selected" / "not selected" state words. That is false for `ProviderKeyCard`. It renders
`ClearableTextField`, which hardcodes the accessibility strings `"Show key"`, `"Hide key"`
(`component/ClearableTextField.kt:82`) and `"Clear text"` (`:92`). `ProviderKeyCard` exposes no
parameter for them. A consumer following the guide will ship an English-only reveal and clear
control on an otherwise localized settings card. The doc names only one residual, so this one is
undocumented.
**Fix:** Either add defaulted label parameters (a source-level, additive, hub-gated change), or
correct both docs to list these as a second documented residual. Suggested wording for API.md and
INTEGRATION.md: "The key field's reveal / hide toggle and clear control ("Show key", "Hide key",
"Clear text") are announced in English and are not yet overridable." Weaken "every" in section 10
accordingly.

### WR-02: ProviderKeyCard row omits behavior that affects callers (trim on every edit, masked by default)

**File:** `API.md:190`
**Issue:** The row says the card "stores, validates and transmits nothing". The source trims every
edit before calling `onKeyChange` (`onValueChange = { onKeyChange(it.trim()) }`), so the caller
never receives leading or trailing whitespace. That is a behavioral transform of the caller's
secret. A caller that round-trips `keyValue` (e.g. a key with intentional trailing characters, or
comparing against a stored value) will see it differ. The field is also masked by default with a
reveal toggle, which is a relevant secret-handling fact for a "key field" row. Neither appears in
the new section, although the source KDoc documents the trim. In the same section, `semanticsPrefix`
is documented as joined by one space, but the code `.trim()`s the prefix first
(`OutcomeSheet.kt:307`). Leading or trailing whitespace in the prefix is silently dropped, which
contradicts "any punctuation belongs to the prefix" only at the whitespace edge.
**Fix:** Append to the row: "masked by default with a reveal toggle; every edit is trimmed of
leading/trailing whitespace before `onKeyChange`, pure formatting, no validation." In the
`semanticsPrefix` cell add: "the prefix is trimmed before joining."

## Info

### IN-01: Table omits the default for `emptyProvidersReason`

**File:** `API.md:190`
**Issue:** The row lists `emptyProvidersReason` with no default. In source it is
`emptyProvidersReason: String = "No providers configured yet"`, a user-visible default that is also
the target of the "every literal is defaulted" claim. Other defaulted strings in the same row and
the sibling rows list their defaults.
**Fix:** Write `emptyProvidersReason = "No providers configured yet"`.

### IN-02: Consumer names in the new compatibility paragraph

**File:** `API.md:295-296`
**Issue:** The new trailing-lambda paragraph names `SecondBrain` and `CalTracker` as swept call
sites. `CLAUDE.md` says this repo names no app-specific concepts. This is not new in kind (the
v2.3.0 paragraph at lines 256-257 does the same and `INTEGRATION.md` names SecondBrain), and it
does not appear in the section 10 component docs. It is still consumer-name leakage in the public
doc. The 2026-10-05 sweep result is also unverifiable from this repo.
**Fix:** Optional. Reword to "known consumer call sites" and keep the named-consumer sweep evidence
in `.planning/` or `ECOSYSTEM.md`.

### IN-03: Binary-compat bullet asserts a proof the same section later disclaims

**File:** `API.md:276-285` vs `API.md:~313-316`
**Issue:** The label-fields block says the `javap` diff "shows zero missing descriptors". The new
Failure/router block says the claim "rests on the `javap` proof and is re-proven at the v2.5.0 cut;
it is not asserted here as freshly verified". The two blocks give different confidence levels for
the same mechanism. A reader cannot tell whether v2.5.0 has actually been proven.
**Fix:** State once, in the binary-compat rule section, whether the proof covers the VFAIL and
VAPPR-04 additions, and reference it from both blocks.

---

_Reviewed: 2026-10-05_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
