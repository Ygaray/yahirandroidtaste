# Phase 29: Docs & v2.8.0 cut readiness - Context

**Gathered:** 2026-10-17
**Status:** Ready for planning
**Mode:** ai (every gray area took research's recommendation, Source: ai-auto; recommendations were set by the ui-designer seat that owns YAT)

<domain>
## Phase Boundary

Docs & v2.8.0 cut readiness — milestone v2.8 (release v2.8.0, additive only). SHIP-05. The phase ends at a PROPOSAL: no tag, no push, no repin. Scan the unpushed range for tailnet IPs, home paths and keys. Working-tree debris (graphify-out/, .oc-audit/, docs/superpowers/, .planning/graphs/*, state.json, WAITING.json, stage markers) never gets committed.

</domain>

<decisions>
## Implementation Decisions

Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P29-D-xx in prose but use the parser shape.

### proposal-route
- **D-01 [proposal-route]:** director@office with range + scan result, cc teamlead@vae-bilingual

### release-flip
- **D-02 [release-flip]:** last docs commit before the proposal, so the tagged README is current

### spanish-check
- **D-03 [spanish-check]:** Robolectric + harness probe

### Claude's Discretion
- Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

</decisions>

<canonical_refs>
## Canonical References

- .planning/ROADMAP.md (§ Phase 29)
- .planning/REQUIREMENTS.md
- .planning/v2.8-DECISION-MAP.md (§ Phase 29) — source of the decisions below
- .planning/research/v2.8-BRIEF.md — LOCKED decisions D-01..D-17 (Yahir 2026-10-17 + designer D-13..D-17); they bind every phase
- .planning/research/SUMMARY.md, .planning/research/ARCHITECTURE.md, .planning/research/PITFALLS.md, .planning/research/STACK.md, .planning/research/FEATURES.md
- `docs/design/snackbar-go-to-item/index.html — the approved mock (direction A)`
- `docs/STYLE.md, docs/STYLE.REVERTED.md — the hub's accepted look and rejected ideas`
- `API.md, CLAUDE.md (source-compat rules, ComponentRegistry, detekt zero-baseline)`

</canonical_refs>

<code_context>
## Existing Code Insights

Cited per decision in `.planning/v2.8-DECISION-MAP.md` and the research files (feedback/*.kt, component/OutcomeSheet*.kt, feedback/UndoCenterScreen.kt, modifier/SwipeableActionRow.kt, explorer/*). Concrete file:line maps are surfaced at plan time.

</code_context>

<specifics>
## Specific Ideas

Match the approved mock (direction A) and docs/STYLE.md; Yahir's picks: keep the › chevron on openable cards, pause timers while touched or fanned.

</specifics>

<deferred>
## Deferred Ideas

999.4 voice-surface polish → v2.8.1; combined "Undo & open"; share/MCP deep links; SB sheet-swap blink (app-side).

</deferred>

## Runtime Decisions

- **2026-10-18, ui-designer: carry-forward from Phases 25 to 28, to cover in the docs (API.md, CHANGELOG/README, INTEGRATION.md):**
  - **POL-03, the OSM tile user agent.** Document that `PlaceMapPicker`'s `userAgent` should be `"AppName/version (+contact)"` and not the bare application id: a `com.example.*` id is blocked by tile.openstreetmap.org. Also document that the hub sets the header via `additionalHttpRequestProperties` after a single `load()` per process; that a blank or bare-package UA falls back to `"<pkg>/<versionName> (Android <sdk>)"` with one `Log.w`; and that placeholder tiles are purged once per install. This is a call to SecondBrain/CalTracker integrators. It names no consumer app, keeping the hub domain-agnostic.
  - **TalkBack behavior worth a line in the docs:**
    - The refused-undo message takes accessibility focus once when its sheet settles.
    - Collapsed deck cards are named from `item.message`, and Undo from `undoLabel`.
    - Both Dismiss forms are kept (D-07).
  - **Gate-2 for Yahir, recorded, not fixed here:**
    - The physical TalkBack steps parked by Phases 25 to 28 (the spoken cross-card order, the Dismiss duplicate in the menu, the refusal read-out by swipe with TalkBack alone).
    - The German font-2.0 narrow body column on the stacked card.
    - The German batch Receipt sheet's actions under the nav bar at font 2.0.
    - Three throwaway harness apps left on the tester (`com.example.yat28head/control/obs`).
- **Process rules for every worker this phase dispatches (teamlead, INC-2026-10-17-04):**
  - Never use `--no-verify`. If a hook blocks a commit, stop and report it; do not bypass it.
  - Commit explicit files only, never debris.
  - Never run `./gradlew --stop`.
  - No tag, no push.
  - The proposal goes to director@office (D-01) with the range from `git log origin/main..HEAD` and the scan result for 100.x IPs, /home/yahir paths and keys.
