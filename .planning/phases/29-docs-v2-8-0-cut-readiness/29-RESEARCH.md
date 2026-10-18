# Phase 29: Docs & v2.8.0 cut readiness - Research

**Researched:** 2026-10-18
**Domain:** Hub documentation + release-gate battery + owner-gated cut proposal (Kotlin/Compose library, Gradle/Metalava, Robolectric, adb tester). No new libraries, no web research needed: everything below was read from this repo this session.
**Confidence:** HIGH (all findings are in-repo and read at HEAD `11953ae`; the few `[ASSUMED]` items are listed in the Assumptions Log)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P29-D-xx in prose but use the parser shape.

- **D-01 [proposal-route]:** director@office with range + scan result, cc teamlead@vae-bilingual
- **D-02 [release-flip]:** last docs commit before the proposal, so the tagged README is current
- **D-03 [spanish-check]:** Robolectric + harness probe

### Claude's Discretion
- Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

### Deferred Ideas (OUT OF SCOPE)
999.4 voice-surface polish -> v2.8.1; combined "Undo & open"; share/MCP deep links; SB sheet-swap blink (app-side).

### Runtime Decisions (bottom of 29-CONTEXT.md, binding)
- **2026-10-18, ui-designer: carry-forward from Phases 25 to 28, to cover in the docs (API.md, CHANGELOG/README, INTEGRATION.md):**
  - POL-03, the OSM tile user agent: `PlaceMapPicker`'s `userAgent` should be `"AppName/version (+contact)"`, not the bare application id (a `com.example.*` id is blocked by tile.openstreetmap.org); the hub sets the header via `additionalHttpRequestProperties` after a single `load()` per process; a blank or bare-package UA falls back to `"<pkg>/<versionName> (Android <sdk>)"` with one `Log.w`; placeholder tiles are purged once per install. Names no consumer app.
  - TalkBack lines: the refused-undo message takes accessibility focus once when its sheet settles; collapsed deck cards are named from `item.message`, and Undo from `undoLabel`; both Dismiss forms are kept (D-07).
  - Gate-2 for Yahir, recorded, not fixed here: the physical TalkBack steps parked by Phases 25-28 (spoken cross-card order, the Dismiss duplicate in the menu, the refusal read-out by swipe with TalkBack alone); the German font-2.0 narrow body column on the stacked card; the German batch Receipt sheet's actions under the nav bar at font 2.0; three throwaway harness apps left on the tester (`com.example.yat28head/control/obs`).
- **Process rules (teamlead, INC-2026-10-17-04):** never `--no-verify` (if a hook blocks a commit, stop and report); commit explicit files only, never debris; never `./gradlew --stop`; no tag, no push; the proposal goes to director@office (D-01) with the `git log origin/main..HEAD` range and the scan result for 100.x IPs, /home/yahir paths and keys.

**Research correction (important, see Pitfalls 1 and 2):** two Runtime Decision sentences do not match the shipped code. The docs must describe the code, not the paraphrase.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| SHIP-05 | Docs (API.md, STYLE.md, INTEGRATION/CATALOG as needed) describe the new API; `apiCheck` shows additions only; tests + detekt green; long-string (DE/ES ~360dp, font scale 2.0) and Gate-1 device checks pass; the `v2.8.0` tag + push are proposed to Yahir (not cut by the agent) | "Doc gap inventory" (what to write where), "Release gate battery" (exact commands, forced fresh), "Long-string coverage" (what exists, the one Robolectric gap, the harness probe), "Proposal artifact" (range, scan, debris rules) |
</phase_requirements>

## Summary

Phase 29 adds no library code. It (a) finishes the docs for the v2.8 surface that Phases 25-28 shipped, (b) flips the release line to `v2.8.0` in the last docs commit, (c) proves every gate on a forced-fresh run of that exact commit, (d) closes the one Robolectric long-string gap (a Spanish handled-by pill) and runs a Spanish harness probe plus a German/Explorer re-check on the TESTER, and (e) writes a PROPOSAL artifact. It cuts no tag, pushes nothing, repins nothing, and (per the dispatch) sends no rig message: the orchestrator relays the artifact to director@office with teamlead@vae-bilingual in cc.

The docs are only partly written. `API.md` already has the long `FeedbackStackHost` row (Phase 26) and the allowlist row for `rememberFeedbackStackState`, and the counts (67 registered / 6 unregistered / 73) are already right and test-enforced. Everything else is missing: there is no v2.8.0 section, the `UndoCenterScreen` and `OutcomeSheet` rows do not mention `onOpenTarget`, `Success.target`/`targetLabel`/`targetDetail`, `BatchRowResultUiModel.target`, `FeedbackEvent.supportingText`, `UndoHistoryEntry.openableTarget`, the `UndoHistoryStore.append(target)` overload, `FeedbackStackState` itself, `FeedbackResult` semantics, or the two `emitTrackedWithUndo` paths. `INTEGRATION.md` has no v2.8 wiring and no OSM text at all. `STYLE.md` has no deck/go-to-item rules and still says the TalkBack gap "is open as backlog 999.2". `README.md` still says `Current release: v2.6.0` (it never got the v2.7.x flip), and `CLAUDE.md` says `v2.7.1`. There is no CHANGELOG file in this repo and none should be created.

**Primary recommendation:** Four plans in this order: (1) tests first (Spanish pill Robolectric case, `newInV280` + INTEGRATION entry-point guard tests), (2) the docs (API, INTEGRATION, STYLE, STYLE.REVERTED), (3) Gate-1 on the TESTER (Spanish harness probe + German Explorer re-check; needs no docs, only final `src`), (4) the release flip as the single last non-`.planning` commit = GATED_HEAD, then the forced-fresh battery on that commit, then the proposal artifact in `.planning` (a descendant of GATED_HEAD that touches only `.planning/`). Model plan 4 on `.planning/milestones/v2.6-phases/24-catalog-docs-explorer-demos-cut-v2-6-0/24-04-GATE-RECORD.md`.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Documenting the v2.8 API | Repo docs (API.md, INTEGRATION.md, STYLE.md) | `CatalogDocsDriftGuardTest` (JVM guard) | Docs are the contract agents read; the guard keeps counts/symbols/tags true |
| Source-additivity proof | `tools/verify-api-source-additive.sh` + Metalava `apiCheck` | `V271CallSiteCanaryTest` | Python rules R1-R8 on `api.txt`; canary proves real call shapes compile |
| Long-string proof (DE/ES) | Robolectric NATIVE graphics tests | Throwaway device harness (screencap, TESTER) | Robolectric is the gate; the device probe is corroboration (D-03), STYLE says only screenshots finally count |
| Release flip | Root docs (`CLAUDE.md`, `README.md`) | none (version is the git tag, not in code) | `ECOSYSTEM.md` section 7: the only in-code literal is the Explorer OSM UA, already `/2.8` |
| Proposal + scan | `.planning/phases/29-*/` artifact | git (`origin/main..HEAD`) | Owner-gated; the agent only writes the artifact |

## Standard Stack

No external packages are installed or added in this phase. **Package Legitimacy Audit: not applicable** (no `npm`/`pip`/`cargo`/Gradle dependency changes; `git diff v2.7.1 HEAD -- build.gradle.kts settings.gradle.kts gradle.properties gradle config jitpack.yml` is empty [VERIFIED: ran it]). The `research-plan` seam was not used: nothing here needs a library or web source.

### Tools and versions already in the repo (verified present)
| Tool | Version / path | Purpose |
|------|----------------|---------|
| Gradle wrapper | `./gradlew` (AGP 9.2.1, Kotlin 2.3.20, JDK 17 `openjdk 17.0.20.1`) | build, tests, detekt, Metalava |
| Metalava plugin | `me.tylerbwong.gradle.metalava`, tasks aliased `apiDump` / `apiCheck` (`build.gradle.kts:33-43`) | API signature file `api.txt` |
| `tools/verify-api-source-additive.sh <tag>` (+ `.py`) | rules R1-R8 | additivity review against `git show <tag>:api.txt` |
| `tools/verify-binary-abi.sh <tag>` | `javap` AAR diff | INFORMATIONAL only, not a gate (CLAUDE.md) |
| `tools/test/run-all.sh` | loops `tools/test/test-*.sh` | fixture suites for all the tools (7 scripts today) |
| `tools/verify-tester-restored.sh` | read-only oracle | proves density/font/accessibility restore (`RESTORED=yes`) |
| `tools/probe-osm-user-agent.sh`, `tools/verify-osm-tile-cache.py` | POL-03 wire/cache probes | not needed again unless the harness opens a map |
| `yrig` | `/home/yahir/.local/bin/yrig` | leases `device:tester` and `build` |
| python3 3.12.3, adb, javap, curl, git | present on host | |

`gradle.properties` sets `org.gradle.caching=true`, so a plain run can be `FROM-CACHE`. That is exactly why the gate must be forced (see Gate battery).

## Architecture Patterns

### Phase flow

```
   tests first                docs                 device                      release                    proposal
 ┌──────────────┐        ┌────────────┐       ┌───────────────┐        ┌─────────────────────┐     ┌───────────────────┐
 │29-01  Spanish│        │29-02  API, │       │29-03  Gate-1  │        │29-04  flip commit    │     │ .planning-only    │
 │pill test +   │──────► │INTEGRATION,│──────►│Spanish harness│──────► │(README + CLAUDE.md) │───► │ PROPOSAL + gate   │
 │newInV280 &   │        │STYLE(+REV),│       │probe + DE     │        │= GATED_HEAD          │     │ record + uat-     │
 │INTEGRATION   │        │README counts│      │Explorer check │        │then FORCED battery   │     │ pending fragment  │
 │guard tests   │        │unchanged   │       │(src unchanged)│        │apiDump,apiCheck,     │     │ (descendant of    │
 └──────────────┘        └────────────┘       └───────────────┘        │additive v2.7.1,tests,│     │  GATED_HEAD)      │
                                                                        │detekt,run-all,scan   │     └─────────┬─────────┘
                                                                        └─────────────────────┘               ▼
                                                                                          orchestrator sends to director@office
                                                                                          cc teamlead@vae-bilingual (NOT this phase)
```

Gate-1 (29-03) may run before or after the flip because the flip changes no byte of `src/` or the AAR; record `git diff --stat <build-head> HEAD -- src tools api.txt` empty as build identity (the Phase 27/28 SELF-UATs did exactly this). Anything that changes `src/` after Gate-1 invalidates it.

### Recommended plan split (planner discretion; mirrors v2.6 Phase 24)
| Plan | Content | Commits touch |
|------|---------|---------------|
| 29-01 | Add the Spanish handled-by pill Robolectric case; extend `CatalogDocsDriftGuardTest` with `newInV280` + v2.8 entry-point checks (written against the docs of 29-02: write tests RED first, or land 29-01 and 29-02 as one wave) | `src/test` |
| 29-02 | Docs: `API.md`, `INTEGRATION.md`, `docs/STYLE.md`, `docs/STYLE.REVERTED.md` (and the PlaceMapPicker/KDoc stays as is) | docs only |
| 29-03 | Gate-1 on the TESTER via `gsd-verify-work-agentic` flow: throwaway Spanish harness (scratchpad, never committed), DE Explorer re-check, restore | `.planning` only (SELF-UAT + evidence + `uat-pending/29-*.md`) |
| 29-04 | Flip commit (`README.md`, `CLAUDE.md`), forced-fresh battery, hygiene greps, range + scan, PROPOSAL artifact | flip: 2 root docs; rest `.planning` only |

## Doc gap inventory (what exists vs what must be written)

Every line number below was read this session at HEAD.

### API.md (613 lines)
| Location | State | Action |
|----------|-------|--------|
| L14-30 "Surface at a glance" + counts | 67 / 6 / 73, Feedback = 4, **correct and test-enforced** | No change (no composable added in Phase 29). Do NOT add any table row starting `\| 12. ` (the guard counts rows matching `^\|\s*\d+\.\s`) |
| L132 `UndoCenterScreen` row | describes v2.6.0 `groups`/`onUndoGroup`/`onClear` only | Add the second overload `UndoCenterScreen(entries, onNavigateBack, onUndo, groups, onUndoGroup, onOpenTarget: (FeedbackTarget) -> Unit, onClear)`; `onOpenTarget` is REQUIRED (so v2.7.1 trailing-lambda calls stay unambiguous); only `Available` rows with `openableTarget` open; hold shows preview, never opens; group headers/Undone/Failed rows never open |
| L134 `FeedbackStackHost` row | **already finished** (deck, fan, swipe, timers, a11y, `show`/`emitTrackedWithUndo`, `FeedbackResult`) | Leave; add only a pointer to the new v2.8.0 section for `FeedbackStackState` details |
| L199 `OutcomeSheet` row | mentions `sheetState` overload, not `onOpenTarget` | Add `onOpenTarget: ((FeedbackTarget) -> Unit)? = null` on BOTH overloads (appended last; the sheetState overload keeps its required named `sheetState` before it) |
| L291/L294 appended-field tables | no `target`, `targetLabel`, `targetDetail` | covered by the new section below |
| L296-324 sections "Additions in v2.7.0" / "Changes in v2.7.1" | pattern to copy | **Add `### Additions in v2.8.0` after L324 (before `### XR-178-01 mapping`).** Keep `## 11. Tone foundation and Receipt types (v2.6.0)` heading verbatim: `CatalogDocsDriftGuardTest` does `substringAfter` on it |
| L333-421 stable tags | no `feedback_stack_*`, `open_target_*`, `undo-open-chevron-*` | **Decision needed, see Pitfall 5.** Default: leave undocumented (they are `internal const` test tags and none has the guarded prefixes) and say so in one sentence in the "Also outside the contract" paragraph (L417-421) |
| L425-437 unregistered (6) | already lists `rememberFeedbackStackState` | none |
| L439-591 compat scopes | latest block is v2.6.0 (+ "After v2.6.0") | Add a "v2.8.0 compatibility scope" paragraph (below) |
| L593 compat rule | still valid | none |

Content for `### Additions in v2.8.0` (all verified against `api.txt`/KDoc this session; quote exact signatures from `git diff v2.7.1 -- api.txt`):

| Surface | What to state (verified) |
|---------|--------------------------|
| `FeedbackTarget(key, openLabel)` | `@Immutable data class`; holds no lambda/Context; `toString()` omits `key` (`"FeedbackTarget(openLabel=...)"`); blank `openLabel` makes the host fall back to the card message as click label; library never parses the key [VERIFIED: `feedback/FeedbackTarget.kt`] |
| `FeedbackEvent.Notify` / `.WithUndo` | appended `target: FeedbackTarget? = null`, `supportingText: String? = null` (after `duration` / `preview`); `DismissScreen` unchanged and rendered as a plain message card by the stack (the stack sends no dismiss signal; the caller owns navigation back) [VERIFIED: `feedback/FeedbackEvent.kt`] |
| `FeedbackResult` | `enum class { Opened, Undone, Dismissed, TimedOut }`; enum on purpose (a fifth value would break exhaustive `when`) [VERIFIED: `feedback/FeedbackResult.kt`] |
| `FeedbackStackState` / `rememberFeedbackStackState(maxVisible = 3)` | `show(event): FeedbackResult` suspends until exactly one result or throws `CancellationException`; `dismissAll()`; `maxVisible` (<1 reads back as 1); 4th arrival dismisses the oldest early (`Dismissed`); Short 4 s / Long 10 s / Indefinite never; timers pause while pointer down or deck fanned; a plain `show(WithUndo)` only REPORTS `Undone` and never runs the lambda; **do not hoist into a ViewModel/singleton** (callers' coroutines are suspended on it); **launch one coroutine per event, never await serially** (25-RESEARCH L205) [VERIFIED: `FeedbackStackState.kt` KDoc L38-80] |
| `FeedbackStackState.emitTrackedWithUndo(store, event)` | appends to `UndoHistoryStore` BEFORE the card shows; only `Undone` runs `store.attemptUndo(entryId)` (under `NonCancellable`); `Opened`/`Dismissed`/`TimedOut` never undo; a card whose entry was spent elsewhere stays visible but inert [VERIFIED: `feedback/UndoTracking.kt` L47-111] |
| `SnackbarHostState.emitTrackedWithUndo(store, event)` | unchanged behavior; now forwards `event.target` into the entry, so the **Undo Center** can open it, but the Material3 snackbar itself has no open affordance and still uses the literal action label `"Undo"` [VERIFIED: `UndoTracking.kt` L24-43] |
| `UndoHistoryEntry` | appended `target: FeedbackTarget? = null` (after `consumedGuard`); computed `openableTarget` = `target` only while `status == Available` (null once Undone/Failed) [VERIFIED: `UndoHistoryEntry.kt` diff] |
| `UndoHistoryStore.append(message, preview, groupId, target, undoAction)` | new overload; `target` deliberately has **no default** so every v2.7.1 `append` shape binds to the older overloads [VERIFIED: `UndoHistoryStore.kt` diff] |
| `VoiceOutcomeUiState.Success` | appended `target`, `targetLabel`, `targetDetail`. **This deviates from brief D-15** (which said the row shows `summary` as its title): the row title is `targetLabel`, falling back to `summary` when null/blank; `targetDetail` is an optional second line; `openLabel` is the TalkBack click label only and is never drawn; row shows only when `target != null` AND the caller passed `onOpenTarget`; locked (dimmed, no click) while `inFlight` [VERIFIED: `model/VoiceOutcomeUiState.kt` KDoc] |
| `BatchRowResultUiModel` | appended `target`; a row with a target gets chevron + click; without: neither; locked while in flight [VERIFIED: `model/BatchRowResultUiModel.kt` diff] |
| `OutcomeSheet(..., onOpenTarget)` | both overloads, both styles; the library never hides the sheet, calls `onDismissRequest`, or navigates; handler must be **idempotent** (double tap not debounced); to slide down first, hoist `sheetState`, `hide()`, then navigate [VERIFIED: `component/OutcomeSheet.kt` L88-94, L140-144] |
| Who clears the target after Undo | Snackbars and Undo Center rows derive it from the store entry status (library-side, D-14). `Success.target` and `BatchRowResultUiModel.target` are caller-built, so **the caller clears them after Undo / Undo all** (KDoc says so) |
| POL-01 / POL-02 | handled-by pill corner radius capped when the text wraps (one-line pill unchanged at every font scale); warn dot gets a contrast ring so it stays visible under dynamic color (3:1, D-16) [VERIFIED: tests `HandledByPillShapeTest`, `HandledByDotContrastTest` exist; 27-04-SELF-UAT block 4/5] |
| POL-03 | see Pitfall 2 for the exact, code-true wording |

**v2.8.0 compatibility scope paragraph (add under "Adding / changing components"):** all additions are appended with defaults and no new `@Deprecated(HIDDEN)` shims; the one required-param addition is `UndoCenterScreen`'s NEW overload (the v2.7.1 signature is untouched); `OutcomeSheet` gains a trailing optional `onOpenTarget` on both overloads (the former last parameters were `colors`/`sheetState`, non-function, so no trailing lambda is displaced); positional `copy`/`componentN` of `Notify`, `WithUndo`, `UndoHistoryEntry`, `BatchRowResultUiModel`, `Success` keep their v2.7.1 positions (new members are appended). Guards: `V271CallSiteCanaryTest`, `tools/verify-api-source-additive.sh v2.7.1`, `CatalogDocsDriftGuardTest`. Expected reviewed exceptions: **none** (`accepted_exceptions=0`, because `ACCEPTED_EXCEPTIONS` in the `.py` is keyed by baseline label and holds nothing for `v2.7.1`; Phase 28 SELF-UAT recorded `API SOURCE-ADDITIVE OK` with removed 0 / signature changes 0). `[ASSUMED]` that the output is unchanged at the final HEAD until the gate re-runs it.

### INTEGRATION.md (333 lines)
Current structure: `## 5. Call components` L107, `### 5b. Wiring the v2.6.0 ...` L149, items (i)..(v) at L160/L202/L239/L280/L284, `## 6. component gallery` L315. Nothing about OSM/INTERNET/`PlaceMapPicker` exists.
- **Add `**(vi) v2.8.0: go-to-item snackbars, sheet rows and the Undo Center**` after L313** (end of (v)). Do not rename `### 5b. Wiring the v2.6.0` and do not touch text before the first `**(i)` (the guard `the compatibility lead-ins list the same three positional source breaks` slices exactly that span).
- Content: (1) remember one `FeedbackStackState` at the app root, place `FeedbackStackHost` in the Scaffold snackbar slot/overlay (host adds no inset padding), pass localized `undoLabel`/`dismissLabel`; (2) `scope.launch { state.emitTrackedWithUndo(store, event) }` per event, never serial; (3) **one app-level `onOpenTarget` that maps keys to routes**, the same function passed to `FeedbackStackHost`, `OutcomeSheet` and the `UndoCenterScreen` overload. Use a neutral example key scheme (for example `"note:42"`, `"list:7"`) with a `when (key.substringBefore(':'))` router and an unknown-key no-op; the library never parses the key; handler must be idempotent and should guard against being already on the destination (the library cannot know the route); (4) caller clears `Success.target`/batch `target` after Undo; (5) hoisted `sheetState` + `hide()` before navigating; (6) M3 `SnackbarHostState.emitTrackedWithUndo` keeps working and still shows no open affordance; (7) the `UndoCenterScreen` positional-call note carries over (name `onClear`); (8) Explorer demo pointers (FeedbackStackHost page, go-to-item sheet demos, Undo Center target rows; EN + DE).
- **Add a short `PlaceMapPicker` / OSM subsection** (e.g. under the same (vi) or a new bullet in "Notes & gotchas") for POL-03: host app must declare `INTERNET`; pass `userAgent = "AppName/version (+contact)"`; never the bare application id; exact behavior in Pitfall 2. Name no consumer app.
- Update the TalkBack lines here or in STYLE (one place is enough; the planner should pick STYLE section 7 and link from INTEGRATION).

### docs/STYLE.md (91 lines)
- Header L8-10 says "drawn from the v2.6 ... v2.7.x": extend to v2.8.
- Section 5 (L66-77) "Feedback and errors": add the deck rules: Receipt card (`surfaceContainerHighest`, 16dp), tone dot, message + optional second line, Undo as `primary` text action, trailing `›` chevron only when a target exists, **no action label** (D-02/D-03), swipe dismisses and never undoes, timers pause while touched or fanned, fan re-collapses after 4 s idle, max 3 visible.
- Section 4 "Sheets": add the go-to-item row rule (label-less tap target with chevron, no target = no chevron and not clickable, caller clears after Undo).
- Section 2/3 or 5: handled-by pill wraps with a capped corner radius; warn dot needs 3:1 against the pill.
- **Section 7 L87-88 is now stale**: "The v2.6 TalkBack gap is open as backlog 999.2." Replace with the closed sweep plus the parked Gate-2 physical gestures (code-true wording in Pitfall 1).
- `docs/STYLE.REVERTED.md` (file says "Read before editing STYLE.md", newest first, one entry per idea): candidate new entries, each with a pointer: (a) a text "Open" action label on snackbars rejected for body-tap + chevron (D-02/D-03; options page B and C rejected, A chosen); (b) moving TalkBack focus / `announceForAccessibility` to speak the refused-undo message (28-11 route, replaced by the single live-region mechanism in 28-12); (c) a focusable, unlabeled middle peek strip (28-10 hides it). Planner discretion.

### README.md (100 lines) and CLAUDE.md
See "Release-flip inventory" below.

### Where each Runtime Decision item goes
| Item | Destination |
|------|-------------|
| POL-03 OSM user agent | `API.md` PlaceMapPicker row (L122) one-sentence + a "Map picker tile agent" bullet in the v2.8.0 section; `INTEGRATION.md` (INTERNET + UA recipe). `PlaceMapPicker` KDoc already carries it (`PlaceMapPicker.kt` L128-135, L185-188): do not edit `src/main` |
| TalkBack lines | `docs/STYLE.md` section 7 (single source), one-line pointer in `INTEGRATION.md` (vi) |
| Gate-2 carry-forward list | NOT in API/INTEGRATION. It belongs in the new `.planning/uat-pending/29-docs-v2-8-0-cut-readiness.md` fragment and in the PROPOSAL "Owner items" section (Yahir's list), referencing the existing fragments 25-28 |
| "CHANGELOG/README" | There is **no CHANGELOG** in the repo (`ls CHANGELOG*` fails); do not invent one. README only needs the release flip. The per-release narrative lives in `API.md` "Additions in v2.x" sections |

## Release-flip inventory (D-02: last docs commit, flip WITHOUT tagging)

Searched every tracked non-`.planning` file for release-line strings [VERIFIED: grep this session].

| File : line | Current | Target |
|-------------|---------|--------|
| `README.md:33` | `> **Current release: \`v2.6.0\`** (\`com.github.Ygaray:yahirandroidtaste:v2.6.0\`). Tags are immutable` | `v2.8.0` (both strings) |
| `README.md:54` | `implementation("com.github.Ygaray:yahirandroidtaste:v2.6.0")` | `v2.8.0` |
| `CLAUDE.md:17-19` | `(current release: \`v2.7.1\`, Receipt voice surfaces + ProviderKeyCard lifecycle, the confirm-sheet handler pill, a hoistable \`OutcomeSheet\` sheet state, tall sheets ... localizable Undo Center group words; see \`API.md\`)` | `\`v2.8.0\`` with a v2.8 descriptor ("go-to-item snackbars: FeedbackTarget, stacked FeedbackStackHost deck, onOpenTarget on OutcomeSheet and UndoCenterScreen, TalkBack sweep, OSM user agent fix"); v2.6 precedent: Phase 24 plan 24-04 edited exactly these two files in the GATED_HEAD commit |
| `explorer/PickersFamilyScreen.kt:42-43` `EXPLORER_MAP_USER_AGENT` | already `YahirAndroidTasteExplorer/2.8 (+https://github.com/Ygaray/yahirandroidtaste)`, pinned by `ExplorerMapUserAgentTest` (`expected` literal, L19) | **No change** ("2.8" is the release line; `ECOSYSTEM.md` section 7 L604-607 says bump both when the line changes; it is already 2.8; do not touch `src/main` after Gate-1) |
| `build.gradle.kts` `publishing.version = "1.10.0"` | hardcoded local-publish constant; JitPack overrides it from the tag; `AGENT-DEVICE-TESTING.md` relies on it | **Do not change** (changing it would alter the AAR identity and the harness coordinates) |
| `jitpack.yml`, `gradle.properties`, manifest | no version string | none |
| `API.md` | no "current release" string; section 11 title keeps `(v2.6.0)` | none; new section is "Additions in v2.8.0" |
| `ECOSYSTEM.md` | consumer table is a "best-effort cache"; no current-release line; consumers are repinned by others | none; the hub never edits consumer pins |
| `.planning/ROADMAP.md:346`, PROJECT/STATE/MILESTONES | planning records | not part of the flip; milestone close (a later step) must create NO marker tag (`create_tag:false`) |

The flip commit stages exactly `README.md` and `CLAUDE.md` (explicit paths), message e.g. `docs(29-04): current release v2.8.0 (D-02)`. Pre-commit hook: `tools/hooks/pre-commit` runs `classify-hub-change.sh --baseline <git describe --tags>` (= `v2.7.1`); a docs-only commit is lane 1 (exit 0). The git hooks are chained from `/home/yahir/.config/git/hooks-chain`; if any hook blocks, stop and report (no `--no-verify`).

## Doc-count drift guards

| Guard | File | What it checks | Run |
|-------|------|----------------|-----|
| `ComponentRegistryDriftGuardTest` | `src/test/.../explorer/` | every public top-level `@Composable` in `component/ feedback/ modifier/ theme/` registered XOR allowlisted | in `testDebugUnitTest` |
| `CatalogDocsDriftGuardTest` (10 tests) | same dir | `API.md`/`README.md` literals `**67 registered public composables**`, `**6 intentionally-unregistered**`, `= **73 public composables total**`, heading `## Intentionally-unregistered sub-parts (6)`; per-family counts in the "Surface at a glance" table vs `ComponentRegistry.entries`; every symbol of the `newInV260` list present in `API.md` and in `api.txt`; every library tag with prefix `outcome_sheet_`, `receipt_row_`, `provider_key_` or in `singleTags` documented in `API.md` as stable XOR test-only (set equality in BOTH directions); `INTEGRATION.md` names the v2.6.0 entry points; lead-in slices `## 11. Tone foundation and Receipt types (v2.6.0)` and `### 5b. Wiring the v2.6.0 ... **(i)` | in `testDebugUnitTest` (`--tests '*CatalogDocsDriftGuardTest'` for a quick check) |
| `DomainVocabularyDriftGuardTest`, `LibraryInvariantsGuardTest`, `GeneratedSymbolDriftGuardTest` | same dir | domain vocabulary in composable names; no consumer imports/noun in code; no `@HiltAndroidApp`; no Dagger-generated symbols in `api.txt` | in `testDebugUnitTest` |

Regeneration: counts are static text, edited by hand and verified by the test (there is no generator). `api.txt` is regenerated with `./gradlew apiDump` (Metalava, release variant). The only test that reads docs is `CatalogDocsDriftGuardTest` [VERIFIED: grep of `src/test` for `.md`]; none of the guards scans docs for consumer names (CLAUDE.md still forbids them: keep docs consumer-name-free for the new text, per the Runtime Decision).

**Recommended guard extension (29-01):** add `newInV280` (symbols: `FeedbackTarget`, `FeedbackResult`, `FeedbackStackState`, `FeedbackStackHost`, `rememberFeedbackStackState`) following `newInV260`'s two checks, but note the existing `api.txt` regexes: `(class|enum|interface) Name [{i]` matches `FeedbackTarget`, `FeedbackStackState`, `FeedbackResult` (enum) [VERIFIED: api.txt diff lines]; composable form `static void Name(` matches `FeedbackStackHost` only. `rememberFeedbackStackState` returns a value (`public static ...FeedbackStackState rememberFeedbackStackState(`), so it needs its own regex or a doc-only check. Also assert `API.md` names `onOpenTarget`, `openableTarget`, `targetLabel`, `targetDetail`, `supportingText`, and `INTEGRATION.md` names `FeedbackStackHost`, `rememberFeedbackStackState`, `onOpenTarget`, `emitTrackedWithUndo`, `FeedbackTarget`.

## Release gate battery (exact commands, forced fresh)

Run on the **main checkout** (not a worktree) at GATED_HEAD, one Gradle invocation at a time, never in the background, never `./gradlew --stop`, always `--no-daemon`. The drift guards locate the repo by directory walk and are vacuous in a worktree (26-02/03/04/05 SUMMARY warnings): the orchestrator must re-run them from the main checkout after merging any worktree work (`use_worktrees: true` in config).

```bash
cd /home/yahir/Projects/Reusable/android/yahirandroidtaste
export PYTHONDONTWRITEBYTECODE=1                       # v2.6 gate: a stray tools/__pycache__/*.pyc left the tree dirty
git rev-parse HEAD > "$GATE/GATED_HEAD"                # $GATE = a scratch dir under the phase dir, untracked, never staged
git status --porcelain --untracked-files=all -- src tools build.gradle.kts settings.gradle.kts gradle.properties gradle config api.txt jitpack.yml API.md INTEGRATION.md README.md CLAUDE.md ECOSYSTEM.md docs/STYLE.md docs/STYLE.REVERTED.md   # must be empty

# (a) regenerate the API file in its OWN invocation (CatalogDocsDriftGuardTest waits on api.txt if apiDump runs beside the tests)
./gradlew apiDump --no-daemon --console=plain
git diff --exit-code -- api.txt                        # regenerated == committed (exit 0)

# (b) source-level compatibility
./gradlew apiCheck --no-daemon --console=plain         # Metalava vs committed api.txt (green after (a))
bash tools/verify-api-source-additive.sh v2.7.1        # THE additions-only-vs-v2.7.1 proof; first line must be "SEAMS: none"
git diff --numstat v2.7.1 -- api.txt                   # record added/removed line counts; removed lines each have an appended-default twin

# (c) forced-fresh unit suite + detekt (Phase 28 Gate-1 used this exact set; 5m31s, 43/43 tasks executed, 1866 tests, 22 skipped)
touch "$GATE/test-marker"
./gradlew testDebugUnitTest detekt --rerun-tasks --no-build-cache --no-daemon --console=plain
find build/test-results/testDebugUnitTest -name '*.xml' ! -newer "$GATE/test-marker" | wc -l    # must be 0 (non-vacuous)
# parse totals: tests, skipped, failures, errors; confirm CatalogDocsDriftGuardTest, ComponentRegistryDriftGuardTest,
# V271CallSiteCanaryTest, LibraryInvariantsGuardTest, DomainVocabularyDriftGuardTest, GeneratedSymbolDriftGuardTest each ran

# (d) tool fixtures
bash tools/test/run-all.sh                             # exit 0; the word FAILED must not appear

# (e) informational only, never a gate
bash tools/verify-binary-abi.sh v2.7.1                 # exit 3 is expected after signature changes; record it, do not block
```

- **Why `--rerun-tasks --no-build-cache`:** `org.gradle.caching=true` is on, and v2.6's first `detekt` came back `1 from cache`. Up-to-date/FROM-CACHE results can be another build's output (memory: gradle-up-to-date-reuses-others-results). Also assert zero `UP-TO-DATE`/`FROM-CACHE` on `:testDebugUnitTest`, `:detekt` and `:metalavaCheckCompatibilityRelease` in the console log.
- **Why `apiCheck` alone is not "additions only":** `apiCheck` compares the dump with the committed `api.txt`; it is trivially green once `apiDump` has run and only fails on removals. The additions-only-vs-`v2.7.1` claim is carried by `verify-api-source-additive.sh v2.7.1` (R1-R8) plus the review of `git diff v2.7.1 -- api.txt`. Current numbers: `78 insertions, 11 deletions` in `api.txt` vs `v2.7.1` [VERIFIED: `git diff v2.7.1 --stat -- api.txt`]; the 11 removed lines are the old `OutcomeSheet` (2), `Notify`, `WithUndo`, `Success`, `BatchRowResultUiModel` and `UndoHistoryEntry` ctor/`copy` signature lines, each rewritten as a line with an appended optional parameter (no removed member, per the tool). The roadmap wording should be read as "no removal, no required-param change, no mid-list insert".
- **The v2.7.1 call-site canary exists:** `src/test/java/io/github/ygaray/yahirandroidtaste/V271CallSiteCanaryTest.kt` (384 lines, 12 tests: `Notify`, `WithUndo`, `DismissScreen`, `copy`/destructuring, exhaustive `when`, `UndoHistoryStore.append` shapes, `UndoHistoryEntry`, `emitTrackedWithUndo`, `OutcomeSheet`, `Success`, `BatchRowResultUiModel`, `UndoCenterScreen`) [VERIFIED: file read]. It runs inside `testDebugUnitTest`; the plan should assert it appears in the result XML. No new canary is needed. Optional belt-and-braces: none required.
- **Runtime:** forced `testDebugUnitTest detekt apiCheck` took 5m31s at Phase 28 (1866 tests across 179 classes); expect 6-8 min now. `apiDump` and `run-all.sh` are short. `verify-binary-abi.sh` builds an AAR and may fetch the baseline AAR from JitPack (network) unless the Gradle cache has it.
- **Hygiene greps (INV-03), all clean today** [VERIFIED: ran]: `git log v2.7.1..HEAD -S"DeprecationLevel.HIDDEN" -- src/main` = 0 commits; no `@HiltAndroidApp`/`@AndroidEntryPoint` in `src/main`; no `com.example.secondbrain`/`com.caltracker` import in `src`; `git diff v2.7.1 HEAD -- config build.gradle.kts settings.gradle.kts gradle.properties gradle jitpack.yml` empty; `git tag --list 'v2.8*'` empty; `.planning/config.json` `create_tag: false`. Re-run after the flip.
- **Registry count today:** 67 `ComponentRegistry.Entry(` in the family files [VERIFIED: grep], matching the docs.

## Long-string coverage (D-03 spanish-check)

| Surface | German | Spanish | Where (all `@Config(sdk=[35], qualifiers="w360dp-h800dp")`, NATIVE graphics, fontScale 1.0 and 2.0) |
|---------|--------|---------|------------------------------|
| Snackbar card | yes | yes | `feedback/FeedbackStackHostLongLabelTest.kt` (`germanMessageAndSecondLine_wrapInsideCard_atFontScale2`, `spanishLongUndoLabel_isCapped_andBodyKeepsTheMajority`, `undoAndChevronStayInsideCard_german/_spanish`, RTL, back-card height) |
| Sheet target rows (single + batch, Receipt AND Classic) | yes | yes | `component/OutcomeSheetOpenTargetLongLabelTest.kt` (8 tests: `receipt_/classic_ german/spanish` target row and batch row, scale 1 and 2) |
| Undo Center openable rows | yes | yes | `feedback/UndoCenterScreenOpenTargetLongLabelTest.kt` (4 tests) |
| **Handled-by pill** | **yes** | **NO** | `component/HandledByPillShapeTest.kt` uses only German strings (`germanPrefix="Bearbeitet von"`, `germanTier`, `germanProvider`); wrapped pill at 360dp / 2.0 keeps every text corner inside the shape, with a failing `CircleShape` control |

**Gap to close in 29-01:** add a Spanish wrapped-pill case (for example prefix `"Gestionado por"`, tier `"Procesamiento en el dispositivo sin conexión de red"`, provider `"Gramática local sin conexión"`, `onDevice = true`) mirroring `measureWrappedGermanPill()` (generalize that helper to take the three strings, keep the existing German test byte-identical in behavior). The test must assert the line count > 1 (non-vacuous), as the German one does. This is a `src/test` change only (no lane-2 concern; `classify-hub-change` looks at `src/main`).

**Device side (the "harness probe"):** no Spanish string exists anywhere in the Explorer (`grep -i spanish explorer/*.kt` is empty) and no earlier SELF-UAT covered Spanish on device (0 hits in the 25-28 SELF-UATs). German is covered on device for all three surfaces (26-05, 27-04, 28-07). So Gate-1 needs a **throwaway Spanish harness** (scratchpad, never committed, `com.example.*` application id, follows `AGENT-DEVICE-TESTING.md` D2 and the Phase 27 "P27Activity-style" harness): one `MainActivity` that composes (1) `FeedbackStackHost` with a long Spanish message + second line + long `undoLabel`, (2) `OutcomeSheet` Receipt and Classic with a Spanish `Success(target, targetLabel, targetDetail)` and a Spanish handled-by pill with `onDevice = true`, at 360dp (`wm density 480` makes 1080 px = 360dp) and `font_scale 2.0`, then a screencap per surface; also reuse the German path through the Explorer (already proven) as the re-check. Judge with screenshots, not dumps (STYLE section 7: "accessibility dumps show text the screen truncates, so only screenshots count"). Build identity to record: library AAR md5 @ short sha (delete `~/.m2/repository/com/github/Ygaray/yahirandroidtaste/1.10.0` first, then `./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q`), harness APK md5, and `git diff --stat <build-head> HEAD -- src tools api.txt` empty.

TESTER rules (all binding; sources `~/.claude/context/devices/common.md`, `test-android.md`, memory `tester-device-serial-and-density-restore`):
- Only `adb -s R5CT10XNKQN` (USB). Never `100.118.21.106:1496` (wireless) and never the personal phone.
- Hold leases first: `yrig lease request device:tester` and `yrig lease request build` (Phase 28 pattern); one agent device-tester at a time; never drive while Yahir is testing; release at the end.
- Never uninstall, clear data or `-d`; keep the three existing harness packages (`com.example.yat28head/control/obs`) in place (a standing dispatch ruling: they are Yahir's to remove) and do not install more than one new throwaway package; if you add one, list it for Yahir.
- Restore: `wm density 450` (**never** `wm density reset`), `system font_scale 1.0`, TalkBack and the observer service off (`enabled_accessibility_services` null, `accessibility_enabled` 0, `touch_exploration_enabled` 0), then prove it: `bash tools/verify-tester-restored.sh --density 450 --font-scale 1.0 --services null` must print `RESTORED=yes` (exit 0). Record the pre-state first.
- TalkBack stays OFF in this phase (not needed for long-string checks); airplane mode not needed.
- Evidence: signed `uat-run` captures per the `gsd-verify-work-agentic` workflow (the `uat-run` binary is provided by that skill, not on PATH). Keep evidence in `.planning/phases/29-.../29-0x-uat-evidence/` (do not commit the debris classes listed below; the Phase 24-28 evidence folders are committed and are the precedent for the SELF-UAT evidence dir itself).

## Proposal artifact

**Name/place:** `.planning/phases/29-docs-v2-8-0-cut-readiness/29-PROPOSAL.md` (+ `29-04-GATE-RECORD.md` in the v2.6 format; the executor may fold them). Scratch logs go in an untracked `.gate/` under the phase dir (never staged; `.gate/` dirs are listed debris).

**Contents (checklist):**
1. GATED_HEAD sha (the flip commit) and `git rev-parse HEAD` of the final docs-only descendant; proof it is a `.planning`-only descendant: `git diff --name-only <GATED_HEAD> HEAD` shows only `.planning/` paths. The proposed tag target is **GATED_HEAD**, not the artifact commit (v2.6 precedent). If Yahir pushes `main` instead of only the tag, all later `.planning` commits go too: say so.
2. Remote state, re-measured at proposal time: `git ls-remote origin refs/heads/main 'refs/tags/v2.8*'` (today `c2182ec`, no v2.8 tag [VERIFIED: ran, read-only]); `git merge-base --is-ancestor origin/main <GATED_HEAD>` (fast-forward; true today).
3. The full unpushed range: `git log --oneline origin/main..<GATED_HEAD>` (**277 commits today**, all by `Yahir <yahir.acts@gmail.com>`; `git diff --shortstat origin/main..HEAD` = 888 files, +54122/-1295). **STATE.md "Tag push publishes unpushed commits" says "4 docs commits" - that is stale** (memory: verify-brief-git-state-claims). Save the list to `.gate/range-log.txt` and put counts, newest, oldest in the artifact. 109 of the 279 commits since `v2.7.1` touch shipped paths (src, tools, api.txt, root docs, docs).
4. Path allowlist of the range (v2.6 style): `git diff --name-only origin/main..<GATED_HEAD> | /usr/bin/grep -vE '^(\.planning/|src/|tools/|docs/|api\.txt$|API\.md$|INTEGRATION\.md$|README\.md$|CLAUDE\.md$|ECOSYSTEM\.md$)'` must print nothing (note `docs/` is needed because Phase 25-28 added `docs/design/**` and STYLE files; today the only non-`.planning/`,`src/` paths are the root docs, `docs/*`, `api.txt` and five `tools/*` files; the `.gitignore` edit is uncommitted).
5. Gate results table: apiDump no-diff, apiCheck, additive-v2.7.1 line (verbatim), forced fresh test totals (tests/skipped/failures/errors, result file count, 0 files older than the marker), detekt zero findings at zero baseline (also `git diff --exit-code v2.7.1 -- config build.gradle.kts settings.gradle.kts gradle.properties gradle jitpack.yml`), run-all, binary ABI (informational), INV-03 greps, long-string table (Robolectric class list + device probe verdicts), Gate-1 run id and `uat-run verify` result.
6. **Scan result** (below), and "Owner items" for Yahir (Gate-2 list from the Runtime Decision plus `.planning/uat-pending/25..28` and the new 29 fragment), and the exact, owner-run commands to cut (`git tag -a v2.8.0 <GATED_HEAD>`, `git push origin main`, `git push origin v2.8.0`) written as TEXT the proposal quotes, never executed. A JitPack confirmation step (`https://jitpack.io/api/builds/com.github.Ygaray/yahirandroidtaste/v2.8.0`) belongs to whoever cuts.
7. Debris statement: list of paths deliberately NOT committed.

**Pre-push scan (no dedicated tool exists; run these over the ADDED lines of the range diff, with positive controls):**
```bash
git diff origin/main..<GATED_HEAD> > "$GATE/range.diff"       # 7.7 MB today
# 1) tailnet IPs (leading [^0-9.] avoids matching 2100.x / version strings)
/usr/bin/grep -nE '^\+.*(^|[^0-9.])100\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' "$GATE/range.diff"
/usr/bin/grep -nE '^\+.*\.ts\.net' "$GATE/range.diff"
# 2) home paths
/usr/bin/grep -nE '^\+.*/home/yahir' "$GATE/range.diff"
# 3) credentials (v2.6 director-confirmed regex, wider than the plan text)
/usr/bin/grep -nE '^\+.*(sk-[A-Za-z0-9_-]{16,}|AIza[0-9A-Za-z_-]{30,}|ghp_[A-Za-z0-9]{30,}|xox[baprs]-[A-Za-z0-9-]{10,}|Authorization: Bearer [A-Za-z0-9._-]{20,}|BEGIN [A-Z ]*PRIVATE KEY|AKIA[0-9A-Z]{16})' "$GATE/range.diff"
# positive control per class: grep a synthetic line first and require count 1
# binary files (113 in the range, screenshots/PNG evidence) cannot be text-scanned: list them with `git diff --stat | grep Bin` and say so
```
**Baseline hits found today (HEAD `11953ae`, dry run of the above) [VERIFIED: ran]:** credentials 0; `.ts.net` 0; **100.x: 8 added lines**, 7 of them in `.planning/` prose/evidence (26-05-SELF-UAT, 27-04-PLAN/SELF-UAT/RESEARCH/SECURITY, 28-07-PLAN) naming the tester's wireless address `100.118.21.106:1496`, and **1 in shipped tooling**: `tools/test/test-verify-tester-restored.sh:134` (`run -- --serial 100.118.21.106:1496`, the negative control that proves a wireless serial is refused); **/home/yahir: 27 added lines**, all under `.planning/` (research/summaries/evidence logs, e.g. `~/.m2/...1.10.0` md5 listings). The repo is **PUBLIC** (`gh repo view` = PUBLIC) and the already-pushed tree holds 52 files with `/home/yahir` and 12 with `100.x`, all under `.planning/`; the v2.6 gate "reviewed and accepted, not scrubbed" this class. So the scan will not come back empty: the artifact must list hits per file, classify each (reviewed and accepted vs action), and let the director decide. The one `tools/test/` hit is the only one outside `.planning/`; scrubbing it at HEAD would not remove it from the already-committed range (history must not be rewritten), so the recommendation is to report it and let the director choose (optional follow-up commit swapping the fixture to a `192.0.2.x:1496` documentation address, which the refusal test only needs to see as `host:port`; `[ASSUMED]` that the script's wireless-serial check is shape-based, read `tools/verify-tester-restored.sh` before changing).

**Debris that must never be committed** (all confirmed present as untracked/modified in the working tree; 3526 untracked files and 8 modified tracked files today): `graphify-out/`, `.oc-audit/`, `docs/superpowers/` (untracked specs), `.planning/graphs/*` (tracked but modified: **`git commit -a` or `git add -A` would sweep them in**), `.planning/state.json`, `.planning/config.json`, `.planning/WAITING.json`, `.planning/metrics/`, `.planning/v2.8-MILESTONE-RUN.md` (tracked, modified), `.gsd-stage-*.done|start.json` markers, `.gate/` dirs, abandoned `*-uat-evidence/` run dirs, `.gitignore` (modified: adds `.oc-audit/`; not this phase's change). Rule: `git add <explicit file paths>` only, then `git diff --cached --name-only` must equal the intended list; never `-a`/`-A`/`.`. Memory: working-tree-debris-stays-out-of-commits.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Additivity proof | a custom diff of `api.txt` | `tools/verify-api-source-additive.sh v2.7.1` + `V271CallSiteCanaryTest` | a literal append-only diff is impossible (rewritten overload lines); the tool encodes R1-R8 |
| Doc count checking | a new script | `CatalogDocsDriftGuardTest` (extend it) | already compares docs, registry, `api.txt` and tags |
| Restore proof for the tester | ad-hoc `adb shell settings get` reads | `tools/verify-tester-restored.sh` | read-only oracle with `RESTORED=yes` and USB-serial enforcement |
| Fresh-run proof | trusting Gradle "BUILD SUCCESSFUL" | `--rerun-tasks --no-build-cache` + result-XML `! -newer marker` count | cache is on; v2.6 and Phase 28 both used this |
| Gate-1 evidence capture | handwritten SELF-UAT with unsigned screenshots | the `gsd-verify-work-agentic` / `uat-run` flow | signed, verified evidence the owner can audit |
| Release version in code | a version constant | the git tag only | `ECOSYSTEM.md` section 7; the one literal (`/2.8`) is already right |

## Common Pitfalls

### Pitfall 1: The Runtime Decision's TalkBack sentence is wrong about focus
**What goes wrong:** Writing "the refused-undo message takes accessibility focus once when its sheet settles" into STYLE/INTEGRATION.
**Why:** that was the Phase 28-11 route; **28-12 replaced it**. Final code has no `focusRequester`/`requestFocus` in `component/OutcomeSheet.kt` (grep: only `LiveRegionMode.Assertive` L309, `spoken` flag L289-313, `awaitPositionAtRest` L359) [VERIFIED]. 28-VERIFICATION C-1 section: no focus move, no `announceForAccessibility`; the refusal `Surface` stays an Assertive live region, silent (no description, drawn text hidden) until its window position is still (6 frames, at least 300 ms, 1.5 s cap), then the node is recomposed from a second call site carrying `contentDescription = refusedMessage`; Samsung TalkBack 15.1.01.1 then speaks it once (27 of 27 showings; designer accepted 2026-10-18, commit `b7495de`).
**How to avoid:** document: "the refused-undo message is an Assertive live region that becomes audible once the sheet has settled (about 0.3 to 1.5 s after it appears), so TalkBack reads it once; the library never moves focus. Proven on one TalkBack build; explicit swipe focus is a Gate-2 item." Card names: a collapsed card is named `message` plus `", supportingText"` when present (`feedbackCardName`, `FeedbackCard.kt` L300-305), the open label is the click label, Undo is named from `undoLabel`; both Dismiss forms are kept (custom action labelled `dismissLabel` AND the `dismiss` semantics action, the duplicate in TalkBack's menu is cosmetic and a Gate-2 note); the middle peek strip of a collapsed deck is hidden from accessibility.
**Warning signs:** the words "focus" or "announce" in the new doc text about the refusal.

### Pitfall 2: POL-03 wording drifts from the code (silent vs warned; "once per install")
**What goes wrong:** The Runtime Decision says "a blank or bare-package UA falls back ... with one `Log.w`" and "placeholder tiles are purged once per install".
**Code truth** [VERIFIED: `component/PlaceMapOsmdroidConfig.kt` L18-30, L58, L80-120; `PlaceMapPicker.kt` L128-135]: a **blank** agent or a `com.example.`-prefixed placeholder application id logs **one** `Log.w` (with the tile-policy URL); a **bare non-placeholder package name** is expanded silently to `<packageName>/<versionName> (Android <release>)`; the caller's agent wins and the hub never writes the `osmdroid.additionalHttpRequestProperty.User-Agent` prefs slot (the consumer owns it); `load()` runs at most once per process, the resolved agent is injected as the `User-Agent` request property right after it (osmdroid 6.1.20's MAPNIK source normalizes the agent to `<packageName>/<versionCode>` and ignores `userAgentValue`) and re-applied without reload if missing/different; precondition: call before any osmdroid `MapView` exists in the process. Cached 6987-byte placeholder tiles (md5 `c069a15b2cc2d6b6f527ad09eb93c61a`) are deleted by a background scan on the **first call of every process** and, on later calls, only while the install flag `placeholder_tiles_purged` is unset (a failed scan leaves it unset); only rows matching size AND md5 are deleted, never the whole cache. Tiles/cache live in the host's private `cacheDir`; the host must declare `INTERNET` itself.
**How to avoid:** write the docs from those lines; keep `com.example.*`/consumer names out of the prose except as the generic "template application id" (the doc may say "a template id such as `com.example.*`").

### Pitfall 3: Doc edits that break `CatalogDocsDriftGuardTest`
Do not: rename `## 11. Tone foundation and Receipt types (v2.6.0)` or `### 5b. Wiring the v2.6.0 ...`; change the four count literals; add a table row beginning `| <digits>. ` outside the Surface table; add a tag row to the "Stable test tags" tables for a tag that is not scanned (the guard fails with "tags documented but not in the source" because it requires set equality in both directions). Run the guard after every docs edit: `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --no-daemon` (quick, still cached unless `--rerun-tasks`).

### Pitfall 4: Running the battery before the flip or on a dirty tree
The flip is part of the tagged tree (D-02) and `CatalogDocsDriftGuardTest` reads the docs, so the forced-fresh battery must run **after** the flip commit and on a clean artifact path list. Any later non-`.planning` change invalidates the whole gate record (v2.6 asserted `git rev-parse HEAD == GATED_HEAD` and an empty `git status --porcelain` of the artifact paths after the battery).

### Pitfall 5: Undocumented test tags
`feedback_stack_item|undo|chevron|deck|dot` (`FeedbackStackHost.kt` L16-22, `FeedbackDeck.kt` L49, `FeedbackCard.kt` L65), `open_target_row`/`open_target_chevron` (`component/OpenTargetRow.kt` L40-43) and `undo-open-chevron-<id>` are `internal const` and outside the guard's prefixes, so they are **not** forced into API.md. 27's PD-3 chose non-`outcome_sheet_` names precisely so the guard would not demand docs. Default: do not publish them as stable API. If the owner wants them stable, extend `tagPrefixes`/`singleTags` in the guard AND list them in API.md in the same commit (never one without the other).

### Pitfall 6: Worktree runs are vacuous
Guards scan by directory name; executors in worktrees (`use_worktrees: true`) get green-but-vacuous guard runs. All gate evidence must come from the main checkout.

### Pitfall 7: Stale facts in briefs and in STATE
STATE.md says 4 unpushed commits (actual 277); the roadmap SC2 says "apiCheck shows additions only" (apiCheck cannot show that); the brief D-15 says the Success row title is `summary` (code: `targetLabel` with `summary` fallback); README says v2.6.0 (never flipped for v2.7.x). Re-derive from git/code in each plan, and cite the file in the doc.

### Pitfall 8: Commit hygiene
Never `git commit -a`/`git add -A`/`.`; the tracked-but-modified `.planning/graphs/*`, `state.json`, `config.json`, `v2.8-MILESTONE-RUN.md` and `.gitignore` would be swept in. Never `--no-verify`; chained hooks live in `/home/yahir/.config/git/hooks-chain`; a blocked commit is a stop-and-report. Commit messages end with the `Co-Authored-By` line given by the harness.

### Pitfall 9: Overstating device evidence for Spanish
Robolectric NATIVE graphics gives real font metrics but is not a device. The device probe is corroboration; a clipped Spanish string on the screencap is a genuine FAIL routed to gap-closure (Gate-1 is diagnose-only; the agent never fixes in the verify step). `src/main` changes after Gate-1 start re-open Gate-1.

## Code Examples

### One app-level `onOpenTarget` (INTEGRATION.md (vi) skeleton; neutral keys, no consumer names)
```kotlin
// Source: API of this repo (feedback/FeedbackStackHost.kt, UndoCenterScreen overload, OutcomeSheet) at HEAD
fun openTarget(target: FeedbackTarget) {            // the ONE mapper; idempotent; the library never parses the key
    when (target.key.substringBefore(':')) {
        "note" -> navController.navigate("note/${target.key.substringAfter(':')}") { launchSingleTop = true }
        "list" -> navController.navigate("list/${target.key.substringAfter(':')}") { launchSingleTop = true }
        else -> Unit                                 // unknown key: do nothing
    }
}
val feedback = rememberFeedbackStackState()          // maxVisible = 3
FeedbackStackHost(feedback, onOpenTarget = ::openTarget, undoLabel = stringResource(R.string.undo), dismissLabel = stringResource(R.string.dismiss))
scope.launch {                                        // one launch per event, never serial
    val result = feedback.emitTrackedWithUndo(store, FeedbackEvent.WithUndo(
        message = "Saved", undoAction = { repo.delete(id) },
        target = FeedbackTarget("note:$id", openLabel = stringResource(R.string.open_note)), supportingText = title))
}
OutcomeSheet(outcome, onDismissRequest = { outcome = null }, style = OutcomeSheetStyle.Receipt, onOpenTarget = ::openTarget)
UndoCenterScreen(entries, onNavigateBack, onUndo = ::undo, onOpenTarget = ::openTarget, onClear = ::clear)
```
(`R.string.*` and `navController` are the caller's; `stringResource` is evaluated in composition, not inside `scope.launch` - the planner should hoist those strings before the example is final.)

### Quick commands
```bash
git rev-list --count origin/main..HEAD                       # 277 today
git diff v2.7.1 --stat -- api.txt                            # 78 insertions, 11 deletions
bash tools/verify-tester-restored.sh --density 450 --font-scale 1.0 --services null   # RESTORED=yes
```

## State of the Art

| Old | Current | Impact |
|-----|---------|--------|
| Snackbar via `SnackbarHostState` + literal "Undo", no open affordance | `FeedbackStackState`/`FeedbackStackHost` deck with body-tap open, caller-localized labels | both paths stay supported; docs must show both |
| Success sheet with no navigation | `Success.target` + `onOpenTarget` rows | caller clears target after Undo |
| README "Current release" lagged (v2.6.0 while v2.7.1 shipped) | flip in the GATED_HEAD commit (D-02) | tagged README is current |

**Deprecated/outdated text to fix:** STYLE.md section 7 "The v2.6 TalkBack gap is open as backlog 999.2"; STYLE header "drawn from v2.6/v2.7.x"; `AGENT-DEVICE-TESTING.md` does not mention the observer AccessibilityService or the baseline-AAR control (28-RESEARCH L490 marked it Phase 29/housekeeping; optional, planner discretion, it is not a gate).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `verify-api-source-additive.sh v2.7.1` prints `accepted_exceptions=0` at the final HEAD | Doc gap / v2.8.0 compat scope | the compat paragraph would need to list the reviewed exceptions; rerun is part of the battery, so caught before commit |
| A2 | Forced battery takes 6-8 minutes (5m31s measured at Phase 28 plus new tests) | Gate battery | none; only scheduling |
| A3 | `tools/verify-tester-restored.sh`'s wireless-serial refusal is shape-based, so a `192.0.2.x:1496` fixture would still exercise it | Proposal / scan | an optional scrub could break `test-verify-tester-restored.sh`; read the script before editing (`run-all.sh` would show it) |
| A4 | Leaving the `feedback_stack_*`/`open_target_*` tags undocumented is acceptable to the owner | Pitfall 5 | owner may want them stable; then extend the guard and docs together |
| A5 | The `uat-run` / `gsd-verify-work-agentic` tooling is available in the executing session (it is "provided by the workflow", not on PATH) | Long-string / device | Gate-1 cannot be signed; fall back to documenting INFRA and parking for Yahir, never waiving |
| A6 | Release-flip descriptor wording for CLAUDE.md (no owner-approved text exists) | Release-flip inventory | cosmetic; the director/owner can reword before cut |
| A7 | The tester is idle and leases are free when 29-03 runs | Device | INFRA wait or park, not a FAIL |

## Open Questions

1. **Does the director want the one `tools/test` tailnet-IP hit scrubbed before the push, or reported and accepted?**
   - Known: public repo; 12 files in the already-pushed tree carry 100.x (all `.planning`); the new hit is the only one in shipped tooling; history cannot be rewritten.
   - Recommendation: report it in the PROPOSAL with both options; do not make a second code change after GATED_HEAD without re-running the whole gate.
2. **Should the v2.8.0 tag target be GATED_HEAD (flip commit) or the final artifact commit?** Recommendation: GATED_HEAD (v2.6 precedent, keeps the tagged tree identical to the gated tree).
3. **Publish `feedback_stack_*`/`open_target_*` tags as stable API?** Recommendation: no (Pitfall 5); ask only if the owner raises it.
4. **Are the three `com.example.yat28*` harness packages to be removed by this phase?** No: the Runtime Decision lists them as Gate-2 items for Yahir; the planner should not uninstall (rule: never uninstall on the tester).

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | Gradle | yes | openjdk 17.0.20.1 | - |
| Gradle wrapper / AGP 9.2.1 | gate battery | yes (repo) | - | - |
| python3 | `verify-api-source-additive.sh` | yes | 3.12.3 | - |
| git, curl, javap | scan, ABI, JitPack check | yes | - | - |
| adb | Gate-1 | binary present at `/usr/bin/adb`; **device not probed** (research is read-only, no device touched) | - | INFRA park |
| `yrig` | leases | present | - | no lease = do not drive the device |
| TESTER `R5CT10XNKQN` | Gate-1 | not probed | - | park device checks for Yahir, never waive |
| `uat-run` (workflow-provided) | signed evidence | not on PATH (by design) | - | see A5 |
| Network (JitPack, GitHub) | ABI baseline, `ls-remote` | `git ls-remote origin` worked | - | informational ABI check can use the Gradle-cached AAR (`BASELINE_AAR`) |
| Disk | builds | `/` 175G free, `/data` 289G free | - | - |
| mavenLocal `~/.m2/.../yahirandroidtaste/1.10.0` | harness | exists (stale until republished) | - | delete then republish before the harness build |

## Validation Architecture

(`workflow.nyquist_validation: true` in `.planning/config.json`.)

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 + Robolectric (NATIVE graphics) + Compose UI test, run by `testDebugUnitTest`; shell fixtures under `tools/test/` |
| Config file | `build.gradle.kts`, `config/detekt/detekt.yml`, `config/detekt-compose.yml`, `config/detekt-baseline.xml` (zero baseline) |
| Quick run command | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --tests '*HandledByPillShapeTest' --tests '*V271CallSiteCanaryTest' --no-daemon` |
| Full suite command | `./gradlew testDebugUnitTest detekt --rerun-tasks --no-build-cache --no-daemon --console=plain` then `bash tools/test/run-all.sh` |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| SHIP-05 / SC1 | docs name the v2.8 symbols; counts 67/6/73 | JVM guard | `./gradlew testDebugUnitTest --tests '*CatalogDocsDriftGuardTest' --no-daemon` | yes, **extend with `newInV280` + INTEGRATION entry points (Wave 0)** |
| SHIP-05 / SC2 | additions only vs `v2.7.1` | tool + canary | `bash tools/verify-api-source-additive.sh v2.7.1`; `--tests '*V271CallSiteCanaryTest'` | yes |
| SHIP-05 / SC2 | apiCheck green on regenerated api.txt | gradle | `./gradlew apiDump` then `git diff --exit-code -- api.txt` then `./gradlew apiCheck` | yes |
| SHIP-05 / SC2 | tests, detekt, tools green, fresh | gradle + shell | full suite command above | yes |
| SHIP-05 / SC3 | DE/ES snackbar card, sheet rows, Undo Center rows at 360dp/2.0 | Robolectric | `--tests '*FeedbackStackHostLongLabelTest' --tests '*OutcomeSheetOpenTargetLongLabelTest' --tests '*UndoCenterScreenOpenTargetLongLabelTest'` | yes |
| SHIP-05 / SC3 | **ES handled-by pill at 360dp/2.0** | Robolectric | `--tests '*HandledByPillShapeTest'` | **no Spanish case: Wave 0** |
| SHIP-05 / SC3 | DE + ES on the TESTER, evidence kept | Gate-1 device (screencaps) | `gsd-verify-work-agentic` flow, `tools/verify-tester-restored.sh` | harness is throwaway (scratchpad) |
| SHIP-05 / SC4 | proposal artifact, no tag, no push | manual check + git | `git tag --list 'v2.8*'` empty; `git ls-remote origin refs/heads/main` unchanged; `git diff --name-only <GATED_HEAD> HEAD` only `.planning/` | artifact is new |

### Sampling Rate
- **Per task commit:** the quick run command (guard + pill + canary) after any doc or test edit.
- **Per wave merge:** `./gradlew testDebugUnitTest detekt --no-daemon` from the main checkout.
- **Phase gate:** the forced-fresh battery on GATED_HEAD, run once, last.

### Wave 0 Gaps
- [ ] Spanish case in `component/HandledByPillShapeTest.kt` (generalize `measureWrappedGermanPill`), covers SC3
- [ ] `newInV280` list + v2.8 entry-point assertions in `explorer/CatalogDocsDriftGuardTest.kt` (API.md and INTEGRATION.md), covers SC1
- [ ] Throwaway Spanish harness (scratchpad, not committed), covers SC3 device half
- Framework install: none (all present)

## Security Domain

`security_enforcement: true` (ASVS level 1, block on high). This phase adds no runtime code, no network or storage surface and no dependency.

| ASVS Category | Applies | Standard Control |
|---------------|---------|------------------|
| V2 Authentication / V3 Session / V4 Access control | no | no auth surface in a UI library |
| V5 Input validation | no (no new input) | n/a; docs mention the key is opaque and never parsed |
| V6 Cryptography | no | none used; do not hand-roll |
| V14 Configuration / supply chain | yes | immutable tag only; never `main-SNAPSHOT`; no dependency change (`git diff v2.7.1 HEAD -- build.gradle.kts gradle ...` empty); agent cuts no tag (`create_tag:false`) |

### Known Threat Patterns
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Publishing internal addresses/paths/keys to a PUBLIC repo | Information disclosure | the pre-push scan above on added lines of `origin/main..GATED_HEAD`, hits classified for the director; keys must be 0 |
| Tag push publishes every reachable unpushed commit | Tampering / disclosure | list the full range; confirm fast-forward; owner cuts |
| Debris committed (state.json, graphs, evidence, specs) | Information disclosure | explicit-path adds + `git diff --cached --name-only` review |
| Stale-cache false pass in gates | Repudiation | forced-fresh run + result-XML freshness count |
| Device left altered after Gate-1 | Denial of service on the shared rig | `wm density 450` (never reset), `RESTORED=yes`, leases released |
| Opaque `FeedbackTarget.key` logged | Information disclosure | `toString` omits it (already shipped); docs must not tell callers to log it |

## Sources

### Primary (HIGH confidence, read this session)
- Repo files: `29-CONTEXT.md`, `REQUIREMENTS.md`, `STATE.md`, `ROADMAP.md` (Phase 29), `v2.8-DECISION-MAP.md`, `research/v2.8-BRIEF.md`, `CLAUDE.md`, `API.md`, `INTEGRATION.md`, `README.md`, `docs/STYLE.md`, `docs/STYLE.REVERTED.md`, `ECOSYSTEM.md` section 7, `AGENT-DEVICE-TESTING.md`, `tools/README-api-guard.md`, `tools/verify-api-source-additive.sh`, `tools/test/run-all.sh`, `tools/hooks/pre-commit`, `build.gradle.kts`, `gradle.properties`, `.planning/config.json`
- Source: `feedback/FeedbackTarget.kt`, `FeedbackResult.kt`, `FeedbackEvent.kt`, `FeedbackStackHost.kt`, `FeedbackStackState.kt`, `UndoTracking.kt`, `UndoCenterScreen.kt` (diff), `UndoHistoryEntry.kt`/`UndoHistoryStore.kt` (diff), `model/VoiceOutcomeUiState.kt`/`BatchRowResultUiModel.kt` (diff), `component/OutcomeSheet.kt`, `component/PlaceMapPicker.kt`, `component/PlaceMapOsmdroidConfig.kt`, `explorer/PickersFamilyScreen.kt`
- Tests: `CatalogDocsDriftGuardTest`, `V271CallSiteCanaryTest`, `HandledByPillShapeTest`, `FeedbackStackHostLongLabelTest`, `OutcomeSheetOpenTargetLongLabelTest`, `UndoCenterScreenOpenTargetLongLabelTest`, `LibraryInvariantsGuardTest`, `ExplorerMapUserAgentTest`
- Phase records: `28-VERIFICATION.md`, `28-07-SELF-UAT.md` (rung-1 command and device protocol), `28-CONTEXT.md` (designer rulings), `28-RESEARCH.md`, `25-/26-/27-` summaries and research cross-references to Phase 29, `.planning/uat-pending/25..28`, v2.6 `24-04-GATE-RECORD.md` (template)
- Git: `git diff v2.7.1 -- api.txt`, `git log origin/main..HEAD`, `git ls-remote origin`, range diff scan (dry run), `gh repo view` visibility
- Global context: `~/.claude/context/devices/common.md`, `test-android.md`, `workflows/two-gate-uat.md`

### Secondary / Tertiary
- None. No web or Context7 sources were needed for this phase.

## Metadata

**Confidence breakdown:**
- Standard stack / commands: HIGH, all tools present and the commands were used verbatim by Phase 24/28 evidence
- Doc gap inventory: HIGH, read line by line
- Pitfalls 1 and 2 (Runtime Decision vs code): HIGH, verified against final source
- Device protocol: MEDIUM-HIGH, copied from Phase 27/28 SELF-UATs; device not probed

**Research date:** 2026-10-18
**Valid until:** until the next `src/` or docs commit on `main` (re-run the `git`/grep facts if HEAD moves from `11953ae`); counts and ranges in particular are time-sensitive.
