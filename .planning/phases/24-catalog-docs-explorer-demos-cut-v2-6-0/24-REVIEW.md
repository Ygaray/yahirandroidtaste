---
phase: 24-catalog-docs-explorer-demos-cut-v2-6-0
reviewed: 2026-10-12T00:00:00Z
depth: standard
files_reviewed: 21
files_reviewed_list:
  - src/main/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRow.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ReleaseDemos.kt
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/V251CallSiteCanaryTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/CatalogDocsDriftGuardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/LibraryInvariantsGuardTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ProviderKeyLifecycleDemoRegistryRenderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptDemosRegistryRenderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptRowRegistryRenderTest.kt
  - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/UndoGroupDemoRegistryRenderTest.kt
  - tools/verify-api-source-additive.sh
  - tools/verify-api-source-additive.py
  - tools/test/test-verify-api-source-additive.sh
  - tools/README-api-guard.md
  - API.md
  - INTEGRATION.md
  - README.md
findings:
  critical: 1
  warning: 5
  info: 5
  total: 11
status: issues_found
---

# Phase 24: Code Review Report

**Reviewed:** 2026-10-12
**Depth:** standard
**Files Reviewed:** 21
**Status:** issues_found

## Summary

Verified at HEAD (read-only runs): `tools/test/test-verify-api-source-additive.sh` passes (22 cases),
`verify-api-source-additive.sh v2.5.1` prints `OK`, and `detekt`, `apiCheck`, `CatalogDocsDriftGuardTest`,
`LibraryInvariantsGuardTest` and `V251CallSiteCanaryTest` are green. The Explorer demos and render tests
are sound; I found no functional bug in `ReleaseDemos.kt` or the tests.

The defects that matter for an immutable v2.6.0 tag are about claims: one public doc statement is false
(and contradicted lower in the same file), and the new source-additivity tool prints `API SOURCE-ADDITIVE
OK` for a head that contains known source breaks, because rule R4 is weaker than the claim in its header.
The tool also has a parser truncation bug.

## Critical Issues

### CR-01: API.md and INTEGRATION.md say every v2.5.1 call still compiles; the same files document source breaks

**File:** `API.md:243-246`, `INTEGRATION.md:152-156` (contradicted by `API.md:504-517`, `API.md:508-512`)
**Issue:** `API.md` says "Everything is additive and opt-in: each new parameter or field defaults to the
earlier behaviour, so a call that compiled against v2.5.1 still compiles". `INTEGRATION.md` 5b says a
v2.5.1 call site "recompiles and renders as before, with one exception: a positional four-argument
`UndoCenterScreen` call". Both are false. `API.md` itself (lines 508-517) documents two breaks, and
the `api.txt` diff shows a third:

- `UndoCenterScreen(entries, back, undo, onClear)` positional (documented).
- `NeedsConfirmation(...)` positional construction and destructuring (`API.md:508-512`), which
  `INTEGRATION.md` does not list as an exception.
- `SheetScaffold` called with all six or more positional arguments, since `shape` is inserted before
  `content` (`api.txt` diff). `API.md:504` only says "every trailing-content call still binds".

A tag is immutable and these docs are what agent consumers read first; the first paragraph is the
one they will act on.
**Fix:** Reword both places and keep the exceptions list complete and identical:
```markdown
Everything is opt-in and defaulted, and named and trailing-lambda call shapes from v2.5.1 still
compile. Three positional shapes do not: UndoCenterScreen with four positional arguments,
NeedsConfirmation built or destructured positionally, and SheetScaffold with more than
onDismissRequest positional. See "Adding / changing components" for the fix for each.
```
Better, make `CatalogDocsDriftGuardTest` assert that the three phrases `UndoCenterScreen`,
`NeedsConfirmation` and `SheetScaffold` appear in the compatibility-scope paragraph, so the exceptions
list cannot drift again.

## Warnings

### WR-01: R4 passes optional parameters inserted before a required one, so the tool says OK for a known source break

**File:** `tools/verify-api-source-additive.py:99-103` (rule R4), `tools/verify-api-source-additive.sh:3-18`,
`tools/test/test-verify-api-source-additive.sh:62-68, 84-90`
**Issue:** R4 compares only the ordered list of REQUIRED parameter types and drops every `optional`
one. Inserting an `optional` parameter in front of a required one therefore passes, but it breaks every
call that passes the later parameter positionally. The script header claims the question "does every call
that compiled before still compile" "reduces to four mechanical rules". It does not. I ran the tool
against `v2.5.1` and it printed `API SOURCE-ADDITIVE OK ... required_param_changes=0`. A positional-prefix
comparison of the same two files finds three regressed signatures:

- `UndoCenterScreen` (`groups`, `onUndoGroup` inserted before required `onClear`)
- `SheetScaffold` (`shape` inserted before required `content`)
- `NeedsConfirmation` ctor (`confirmEnabled`, `titleActions` inserted before `onConfirm`/`onCancel`)

Test cases 3 and 6 in the fixture script enshrine the unsound pass ("mid-list optional parameter
passes"). The repo's own rule (`CLAUDE.md`, `API.md:553`) is "trailing lambdas stay last / never insert
before"; the gate cannot enforce it.
**Fix:** Add rule R5: for every baseline signature, some head signature must have the baseline's full
parameter type list (optional or not) as a positional prefix, or the tool reports `WARN R5` and exits 1
unless the signature is on an explicit, reviewed allowlist (for v2.6.0: the three above, citing
`API.md`). Flip fixtures 3 and 6 to expect failure and add one that is allowlisted. If you would rather
keep the tool as is, rename the verdict (`API NO-REMOVAL OK`) and delete the "does every call still
compile" sentence from the header and `tools/README-api-guard.md`, so a green line is not read as a
compile guarantee.

### WR-02: Parameter-type parser truncates unnamed parameters containing a space, hiding suspend return-type changes

**File:** `tools/verify-api-source-additive.py:102`
**Issue:** `param.rsplit(" ", 1)[0]` assumes every parameter is `type name`. Metalava emits the
synthetic `Continuation` of a `suspend` function and extension receivers with no name, and these types
contain spaces. Confirmed by running the parser:
`parse_signature("public suspend Object? f(String id, kotlin.coroutines.Continuation<? super Foo>);")`
returns `('String', 'kotlin.coroutines.Continuation<? super')`. The type argument is cut off, so a
suspend function's return type (carried only in `Continuation<? super X>`) can change and R4 still
passes. Six signatures in the current `api.txt` hit this
(`UndoHistoryStore.attemptUndo`/`attemptUndoGroup`, `emit`, `emitTrackedWithUndo` x2, `EditorItemRow` receiver).
**Fix:** Parse the parameter by splitting off the name only when the last token has no `<`, `>`, `,`
or `?` and the remainder is non-empty. Better, split at the last top-level space (outside `<>`):
```python
def split_type_name(param):
    depth = 0
    for i in range(len(param) - 1, -1, -1):
        ch = param[i]
        if ch == ">": depth += 1
        elif ch == "<": depth -= 1
        elif ch == " " and depth == 0:
            tail = param[i + 1:]
            return (param[:i], tail) if re.fullmatch(r"[A-Za-z_]\w*", tail) else (param, "")
    return param, ""
```
Add a fixture with a suspend method whose `Continuation` type argument changes.

### WR-03: R4 ignores parameter names, return and property types, and new abstract members or enum constants

**File:** `tools/verify-api-source-additive.py:99-103`, `tools/README-api-guard.md` (Scope paragraph)
**Issue:** Renaming a parameter (`count` to `total`) leaves the required type list unchanged, so the
tool passes it, yet every named-argument call breaks. The canary shows both consumers call these
composables almost exclusively with named arguments, so a rename is the most likely real break in this
ecosystem. Return-type changes, property-type changes, an existing enum gaining a constant (breaks
exhaustive `when`) and an interface or sealed type gaining an abstract member are also invisible. The
README Scope line is honest about "does not look at behavior" but does not list these gaps.
**Fix:** Keep the parameter name in the compared tuple for required parameters (the `api.txt` carries
them), compare the return type for `method`, and report enum constants added to a baseline enum as a
warning. At minimum, list these gaps in the README Scope paragraph.

### WR-04: INTEGRATION.md key-lifecycle sample shows a pointless Cancel button for unsaved providers

**File:** `INTEGRATION.md:197-205` (the `ProviderKeyActions(... onCancelEdit = { editing = false; typed = "" } ...)` sample)
**Issue:** `ProviderKeyCard` renders Cancel in field mode whenever `onCancelEdit != null`
(`ProviderKeyCard.kt:310`). The sample passes it unconditionally, so a provider with no saved key (field
mode, nothing to cancel back to) also gets a Cancel button. The Explorer demo deliberately passes `null`
unless `editing` is true, and `ProviderKeyLifecycleDemoRegistryRenderTest` asserts "no Cancel for an
unsaved provider". The doc teaches the opposite of the behaviour the demo and test pin.
**Fix:** Change the sample to `onCancelEdit = if (editing) { { editing = false; typed = "" } } else null`
and add a bullet: "Pass `onCancelEdit` only while `editingSavedKey` is true; an unsaved provider has nothing to cancel back to."

### WR-05: LibraryInvariantsGuardTest does not enforce "no Hilt application host"

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/LibraryInvariantsGuardTest.kt:27-39, 86-108`
**Issue:** The class KDoc promises the library "hosts no application", and `CLAUDE.md` forbids both
`@HiltAndroidApp` and `@AndroidEntryPoint`. The import allow-list accepts any `dagger.` import, which
includes `dagger.hilt.android.HiltAndroidApp` and `dagger.hilt.android.AndroidEntryPoint`. The manifest
check only catches an `android:name` on `<application>`, so an `@AndroidEntryPoint` on
`ExplorerActivity` (which makes the library need a consumer Hilt host) would pass.
**Fix:** Add a test over comment-stripped main sources:
```kotlin
@Test
fun `the library declares no Hilt entry point or application`() {
    val offenders = mainKotlinFiles().filter { f ->
        Regex("@(HiltAndroidApp|AndroidEntryPoint)\\b").containsMatchIn(SourceContractTestSupport.stripComments(f.readText()))
    }.map { it.name }
    assertTrue("Hilt host annotations in the library: $offenders", offenders.isEmpty())
}
```

## Info

### IN-01: Demo "fingerprint" keeps the whole key when it is four characters or shorter

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ReleaseDemos.kt:131`
**Issue:** `typed.trim().takeLast(4)` returns the entire typed text when it has 4 or fewer characters, so
the "typed text never reaches the saved map" comment (T-24-01) is only true for longer input. Demo-only,
but a sample consumers copy.
**Fix:** `"key-…" + typed.trim().takeLast(4).takeIf { typed.trim().length > 4 }.orEmpty()`, or document the
minimum length in the comment.

### IN-02: Canary's "positional break is excluded" guard only matches all-lowercase identifiers

**File:** `src/test/java/io/github/ygaray/yahirandroidtaste/V251CallSiteCanaryTest.kt:687`
**Issue:** `Regex("UndoCenterScreen\\(\\s*[a-z]+\\s*,")` does not match `UndoCenterScreen(someEntries, ...)`
or `UndoCenterScreen(listOf(), ...)`, so a future positional call would slip past the assertion.
**Fix:** Use `UndoCenterScreen\\(\\s*(?![a-z]+\\s*=)[^)]*,\\s*[^=]*\\)` or simply `UndoCenterScreen\\(\\s*[^\\s=]+\\s*,`.

### IN-03: Stale and misleading names and comments in the new tests

**File:** `tools/test/test-verify-api-source-additive.sh:139-140`, `src/test/java/.../CatalogDocsDriftGuardTest.kt:52`
**Issue:** The comment for case 14 ("keeps the old call shape compiling, but R4 is strict on purpose only
when...") is garbled. `statsHeading` holds the "Stable test tags" heading (typo for `tagsHeading`).
**Fix:** Reword case 14 to "a required parameter turned optional changes the REQUIRED list, so R4
fails by design"; rename the constant.

### IN-04: Undo group demo uses a fixed 680dp height

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ReleaseDemos.kt:211`
**Issue:** The Box is `height(680.dp)` so every row composes in the lazy list. At large font scale or
with the long German group label the content is taller and the last rows are clipped or scroll inside a
nested scroller; the render test runs at the default font scale and would not notice.
**Fix:** Add a short comment that the height assumes the default font scale, or size with
`heightIn(min = 680.dp)` inside a non-lazy host for the demo.

### IN-05: Fixture seams can silently turn the cut-gate verdict into a fixture verdict

**File:** `tools/verify-api-source-additive.sh:37-41, 57-74`
**Issue:** If `BASELINE_API_FILE` or `HEAD_API_FILE` leaks into the environment, the tool compares
arbitrary files, exits 0 and prints `OK`. Only line 1 (`SEAMS: ...`) reveals it, and the README relies on
a human noticing. `ROOT` is also derived from the current directory, not the script location (consistent
with the sibling tools, and it fails closed outside a repo).
**Fix:** Print `API SOURCE-ADDITIVE OK (FIXTURE SEAMS: ...)` in the final line when a seam is set, or exit
non-zero when a seam is set and `RELEASE_CUT=1`.

---

_Reviewed: 2026-10-12_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
