---
phase: 29-docs-v2-8-0-cut-readiness
plan: 02
subsystem: docs
tags: [api-docs, integration-docs, drift-guard, onOpenTarget, osm-user-agent]
status: complete
requires:
  - phase: 29-01
    provides: "API.md v2.8.0 target-and-stack half, v280Section helper in CatalogDocsDriftGuardTest"
provides:
  - "API.md: second v2.8.0 table (Success, BatchRowResultUiModel, OutcomeSheet and UndoCenterScreen onOpenTarget), Fixes (POL-01, POL-02), Map picker tile agent (POL-03), v2.8.0 compatibility scope"
  - "INTEGRATION.md: (vi) v2.8.0 wiring with one app-level onOpenTarget, 5c map picker tile policy"
  - "Four more CatalogDocsDriftGuardTest cases for the v2.8.0 docs"
affects: [29-03, 29-05, 29-06]
plan_head_before: b957f30eccdd2dd2839fb4b101a54c74176a6b55
actuals:
  tokens: 12000
  tasks: 2
  commits: 2
key-files:
  created: []
  modified:
    - API.md
    - INTEGRATION.md
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/CatalogDocsDriftGuardTest.kt
key-decisions:
  - "Docs follow the shipped source where 29-CONTEXT.md Runtime Decisions or the brief disagree (PD-1)"
  - "No CHANGELOG file is created (PD-3); TalkBack lines stay in docs/STYLE.md (plan 29-03), INTEGRATION (vi) only points to it (PD-2)"
requirements-completed: [SHIP-05]
duration: 40 min
completed: 2026-10-18
---

# Phase 29 Plan 02: v2.8.0 API and INTEGRATION docs Summary

API.md and INTEGRATION.md now describe the whole v2.8 surface from the source: the sheet and Undo Center go-to-item rows, one app-level `onOpenTarget` that maps opaque keys to routes (neutral `note:42` / `list:7` scheme, unknown key is a no-op), both `emitTrackedWithUndo` paths, the two pill fixes, the map picker `AppName/version (+contact)` user-agent policy and the v2.8.0 compatibility scope. Four plain-JVM guard tests keep that true. With plan 29-01 this completes ROADMAP Phase 29 criterion 1 (counts still 67 / 6 / 73).

## Commits

| Task | Commit | Files |
|------|--------|-------|
| 1 API.md sheet, Undo Center, map agent, compatibility scope | a44f436 | API.md, CatalogDocsDriftGuardTest.kt |
| 2 INTEGRATION.md (vi) and 5c | f8371b7 | INTEGRATION.md, CatalogDocsDriftGuardTest.kt |

## RED output (before the docs were written)

Task 1:
```
CatalogDocsDriftGuardTest > the API md UndoCenterScreen and OutcomeSheet rows name onOpenTarget FAILED
    java.lang.AssertionError at CatalogDocsDriftGuardTest.kt:256
CatalogDocsDriftGuardTest > the API md v2_8_0 section names the member surface and the map agent guidance FAILED
    java.lang.AssertionError at CatalogDocsDriftGuardTest.kt:238
13 tests completed, 2 failed
```
Task 2:
```
CatalogDocsDriftGuardTest > the v2_8_0 doc text names no consumer app and claims no focus move FAILED
CatalogDocsDriftGuardTest > INTEGRATION md names the v2_8_0 entry points and the map agent guidance FAILED
15 tests completed, 2 failed
```

## Verification

| Check | Result |
|-------|--------|
| `CatalogDocsDriftGuardTest` after Task 1 | tests=13 failures=0 errors=0 (with `V271CallSiteCanaryTest`, BUILD SUCCESSFUL) |
| `CatalogDocsDriftGuardTest` after Task 2 | tests=15 failures=0 errors=0, both new testcase names present |
| `./gradlew detekt` | BUILD SUCCESSFUL (zero baseline) |
| `grep -c '^\*\*(vi) v2.8.0' INTEGRATION.md` | 1 |
| `git show --format= HEAD -- INTEGRATION.md`, removed lines | 0 (nothing before the first `**(i)` of 5b changed) |
| `git diff --stat 55f5288 HEAD -- src/main`, `git diff --stat c1681ff HEAD -- api.txt` | empty |
| `git tag --list 'v2.8*'` | empty |
| Commit file lists | exactly the planned files per task |

## Discrepancies for the master

1. **Success row title vs brief D-15.** D-15 said the row title is `summary`. The shipped code (VoiceOutcomeUiState.kt L55-65) uses `targetLabel`, falling back to `summary` when it is null or blank; `targetDetail` is an optional second line. The docs describe the code.
2. **Log.w wording vs the Runtime Decision.** The Runtime Decision says a blank or bare-package agent falls back "with one `Log.w`". The code (PlaceMapOsmdroidConfig.kt L151-158) warns when the sanitized agent is blank or the resolved agent starts with `com.example.`. A bare package name that is not a template id is expanded to the fallback silently; a bare template id still warns. The warning is logged on each configure call, which is once per picker instance (the factory runs once per `PlaceMapPicker`, PlaceMapPicker.kt L691-717), not "one" per process or install. The docs say so.
3. **Purge cadence vs "once per install".** The Runtime Decision says placeholder tiles are purged once per install. The code (L163-167) schedules the scan on the first call of every process, and on later calls only while the install flag `PREF_PLACEHOLDER_PURGED` is unset. The docs say that; they match only on size AND md5 and never wipe the cache.
4. **Extra: the library manifest declares no `INTERNET`.** The docs say the host declares it (verified in src/main/AndroidManifest.xml).

## Deviations from Plan

**1. [Rule 1 - Bug] Test row lookups were ambiguous.** `the API md UndoCenterScreen and OutcomeSheet rows name onOpenTarget` first used the bare prefixes `| \`UndoCenterScreen\`` and `| \`OutcomeSheet\``; API.md already has a second `OutcomeSheet` row in the v2.7.x tables (and the new second-overload row for `UndoCenterScreen`), so "exactly one" failed. The test now selects the catalog row by a longer prefix (`| \`UndoCenterScreen\` | The Undo Center`, `| \`OutcomeSheet\` | Bottom sheet`, `| \`PlaceMapPicker\` | Map-based`). Intent of the plan is unchanged.

**2. Transient compile failure.** One Gradle run ended with `compileDebugUnitTestKotlin FAILED` on the busy shared host with no source error; a plain re-run of `compileDebugUnitTestKotlin` and the tests succeeded. No file was changed for it.

Otherwise the plan was executed as written. No tag, no push, no repin, no rig message, no device, no `src/main` or `api.txt` change.

## Issues Encountered

None beyond the above. The pre-commit hook classified both commits as LANE 1 (additive, baseline v2.7.1) and passed.

## Self-Check: PASSED

- a44f436 and f8371b7 exist in `git log`; API.md, INTEGRATION.md and the test file exist on disk.
- Counts `**67 registered public composables**`, `**6 intentionally-unregistered**` and `= **73 public composables total**` are still asserted green by the guard.
