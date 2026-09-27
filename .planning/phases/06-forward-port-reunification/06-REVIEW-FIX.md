---
phase: 06-forward-port-reunification
fixed_at: 2026-09-27T07:17:15Z
review_path: .planning/phases/06-forward-port-reunification/06-REVIEW.md
iteration: 1
findings_in_scope: 4
fixed: 2
skipped: 2
status: resolved
---

# Phase 06: Code Review Fix Report

**Fixed at:** 2026-09-27T07:17:15Z
**Source review:** .planning/phases/06-forward-port-reunification/06-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 4 (fix_scope=all: 2 warnings, 2 info)
- Fixed: 2
- Skipped: 2

**Verification environment:** all edits, syntax checks, and commits ran in an isolated git worktree
(`.claude/worktrees/rf-06-*`, branch `gsd-reviewfix/06-*`) fast-forwarded into `main` on cleanup —
not the main checkout. `./gradlew compileDebugKotlin` (Tier 2 verification for the Kotlin-source
fixes) and the hub's own `tools/classify-hub-change.sh` additive-guard probe both ran there and are
reproducible from the same worktree path while it exists; after cleanup, reproduce by re-checking
out `main` at the commits below.

## Fixed Issues

### WR-01: CLAUDE.md's documented Compose BOM version has drifted from the actual pin

**Files modified:** `CLAUDE.md`
**Commit:** fc4c98f
**Applied fix:** Changed the documented toolchain line from `Compose BOM **2026.02.01**` to
`Compose BOM **2026.04.01**`, matching the actual pin in `gradle/libs.versions.toml:12`
(`composeBom = "2026.04.01"`). Verified by re-reading the edited line; this is a doc-only change
outside `src/main`, so it did not trip the hub's additive-guard pre-commit hook.

### WR-02: `androidx-lifecycle-runtime-compose` hardcodes its version, breaking the catalog's single-source-of-truth convention

**Files modified:** `gradle/libs.versions.toml`
**Commit:** cde36b8
**Applied fix:** Added a `lifecycleRuntimeCompose = "2.9.4"` entry to `[versions]` and changed the
`androidx-lifecycle-runtime-compose` library entry to `version.ref = "lifecycleRuntimeCompose"`,
matching every other catalog entry's convention. Verified with `python3 -c "import tomllib; ...
tomllib.load(...)"` (Tier 2 TOML parse, passed) in addition to a re-read of the file. Also outside
`src/main`, so the additive-guard hook did not apply.

## Skipped Issues

### IN-01: `PlaceMapPicker.kt` mixes several concerns in one ~900-line file

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/PlaceMapPicker.kt`
**Reason:** Applied the suggested extraction (moved `buildPinIcon`/`buildHandleIcon`/`buildOvalIcon`
and both `Marker.OnMarkerDragListener` implementations into a new sibling
`PlaceMapPickerOverlays.kt`, widening `PlaceMapPickerHolder`/`PlaceMapOverlayColors`/
`handlePointGeoPoint` from `private` to `internal` for the cross-file references) and confirmed it
compiles clean (`./gradlew compileDebugKotlin`, no errors) with no behavior change. However,
committing it triggers this hub repo's own `tools/hooks/pre-commit` additive-guard: any diff that
rewrites/removes a pre-existing line in a tracked `src/main` file (not a pure append) is classified
`LANE 2 (non-additive)` by `tools/classify-hub-change.sh` and blocked on the fast path — exactly
what moving code between files does, even though the compiled behavior is identical. The hook's own
escape valve (`HUB_LANE_OVERRIDE=2 git commit …`) exists precisely so a *human* can deliberately
declare a coordinated non-additive change; self-granting that override from this fixer would defeat
the gate's purpose (this is a reusable library consumed by other apps via immutable JitPack tags —
see `CLAUDE.md`'s "shipping is human-gated" section). Rolled back cleanly via `git checkout --`; no
partial state remains. This is an Info-level, explicitly "Optional" finding — no blocker/critical/
high is open as a result of skipping it. If you want this landed, re-run with
`HUB_LANE_OVERRIDE=2 git commit …` yourself, or ask a future fixer run with that override
pre-authorized.

**Original issue:** The file combines the public composable, private section composables, osmdroid
`MapView` construction/lifecycle glue, two `Marker.OnMarkerDragListener` implementations, and
drawable-icon builders in one ~900-line file — a readability/navigability observation only, not a
regression (the module's zero-baseline detekt policy already accepts this file's size).

### IN-02: Redundant blank-check duplicated between the gallery demo and the hub's own gate

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt:362-367`
**Reason:** Applied the suggested simplification exactly as given in REVIEW.md (dropped the inner
`if (query.isNotBlank())` and always set the fixture coordinates, relying on `PlaceMapPicker`'s own
`canSubmitSearch` gate) and confirmed it compiles clean. Hit the identical hub-additive-guard lane-2
block as IN-01 — removing the `if` line rewrites a pre-existing `src/main` line, which
`tools/classify-hub-change.sh` classifies as non-additive regardless of the change being behavior-
preserving dead-code removal. Rolled back cleanly via `git checkout --`; no partial state remains.
Also an explicitly "Optional" Info-level finding — no blocker/critical/high is open as a result.
Same remediation path as IN-01: land it deliberately with `HUB_LANE_OVERRIDE=2`.

**Original issue:** `PlaceMapPickerDemo`'s `onSearch` lambda re-checks `if (query.isNotBlank())`
before setting the fixture pin, even though `PlaceMapPicker` itself already gates the search commit
through `canSubmitSearch` (blank/whitespace-only queries never reach `onSearch`). Harmless
defense-in-depth in demo-only code, but dead logic at this specific call site.

---

_Fixed: 2026-09-27T07:17:15Z_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
