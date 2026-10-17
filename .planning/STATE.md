---
gsd_state_version: "1.0"
milestone: v2.8
milestone_name: Snackbar go-to-item + stacked feedback
current_phase: 25
current_phase_name: feedback-target-model-stack-state
status: executing
stopped_at: v2.8 roadmap created (Phases 25-29); ready to plan Phase 25
last_updated: "2026-10-17T07:41:05.440Z"
last_activity: 2026-10-17
last_activity_desc: v2.8 roadmap created (5 phases, 19/19 requirements mapped)
state_head: e1c87f4fd2d2797c36d50e9ed4c982379f673618
progress:
  total_phases: 5
  completed_phases: 0
  total_plans: 5
  completed_plans: 0
  percent: 0
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-10-17)

**Core value:** An agent building one of Yahir's apps finds the widget it needs already here, coherent and legible, and drops it in instead of writing a new one.
**Current focus:** Milestone v2.8 (Snackbar go-to-item + stacked feedback, release `v2.8.0`, additive). Next: Phase 25, Feedback target model & stack state. Baseline `v2.7.1` (tag commit `f60bfc4`), the current release.

## Current Position

Phase: 25 (feedback-target-model-stack-state) — READY TO EXECUTE
Plan: —
Status: Ready to execute
Last activity: 2026-10-17 — v2.8 roadmap created (5 phases, 19/19 requirements mapped)

Progress: [░░░░░░░░░░] 0%

## Performance Metrics

**Velocity (milestone v2.8):**

- Total plans completed: 0
- Average duration: - min
- Total execution time: 0 hours
- Previous milestone (v2.6): 19 plans across Phases 20-24

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 25 | - | - | - |
| 26 | - | - | - |
| 27 | - | - | - |
| 28 | - | - | - |
| 29 | - | - | - |

**Recent Trend:**

- Last 5 plans: -
- Trend: -

*Updated after each plan completion*
**Per-Plan Metrics:**

| Plan | Duration | Tasks | Files |
|------|----------|-------|-------|

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- v2.8 scope comes from the locked brief `.planning/research/v2.8-BRIEF.md` (D-01..D-12, Yahir 2026-10-17 via director@office, "I like A" + "All recs") and the options page `docs/design/snackbar-go-to-item/index.html`. Don't re-litigate the locked decisions.
- Roadmap: phase numbering continues from v2.6 (ended at Phase 24), so v2.8 is Phases 25-29. The split was set by the orchestrator: 25 target model + stack state, 26 Receipt-card deck UI, 27 sheet/Undo Center go-to-item + pill fixes, 28 TalkBack sweep + Explorer, 29 docs + cut readiness.
- Roadmap: Phase 25 lands `FeedbackStackHost`'s public D-09 signature as a tracer host (newest-first items, body tap and Undo wired). The drift guard forces registration (or allowlisting) of public `@Composable`s in `feedback/` in the phase that adds them. Phase 26 owns the deck look, the cap (FSTK-02), timers/pause (FSTK-06), gestures and semantics, even if the cap/timer logic lives in the state.
- Roadmap: phases run in numeric order. Phase 27 only needs Phase 25, but it runs after 26 because every phase rewrites the shared `api.txt`.
- Roadmap: every phase that widens an existing signature (25: `FeedbackEvent`, `UndoHistoryEntry`; 27: `OutcomeSheet`, `Success`, `BatchRowResultUiModel`, `UndoCenterScreen`) carries "the default call behaves/renders exactly as v2.7.1 (same code path; Robolectric state assertions)". No Roborazzi.
- Roadmap: backlog 999.2 (TalkBack sweep) and 999.3 (Explorer demo polish) are promoted into Phase 28 (A11Y-02, DEMO-02), kept in ROADMAP Backlog as history. 999.4 stays backlog, targeted at `v2.8.1`.
- Roadmap: already in the code, so Phase 28 verifies rather than rebuilds: the hoisted-`sheetState` demo that calls `hide()` (`VoiceCommandFamilyScreen.kt`, `5206be9` v2.7.0, `a5b29eb` v2.7.1) and `heading()` on the Undo Center group label (`UndoGroupHeaderRow`). `HubSchemeScope` (`ReleaseDemos.kt`) wraps only the Receipt and tone demos today.
- Carried from v2.5: commits that re-signature a composable are classified lane 2 by the pre-commit hook, so use `HUB_LANE_OVERRIDE=2` (the detected lane). Regenerate `api.txt` in the same commit as the signature change.
- Carried from v2.7.0: an experimental-typed param (e.g. a Material3 `@ExperimentalMaterial3Api` state) goes in a separate overload, not a defaulted param, so existing call sites need no opt-in.

### Pending Todos

None yet.

### Blockers/Concerns

- **Owner-gated cut (SHIP-05 / Phase 29):** the agent proposes `v2.8.0` and never cuts or pushes it. Route tag/version/sequencing confirmations to the teamlead/orchestrator first, not Yahir directly.
- **Stray-tag hazard:** milestone close must cut NO git marker tag (`git.create_tag` false). The only tag is the `v2.8.0` release coordinate, cut by Yahir.
- **Tag push publishes unpushed commits:** `origin/main` is at `c2182ec`; 4 docs commits (`426fcbf`..`1ecab5f`) are already unpushed as of 2026-10-17. The Phase 29 proposal must list the full `git log origin/main..HEAD` range.
- **Source-compat gate (SHIP-05):** `./gradlew apiCheck` on a regenerated `api.txt` (additions only vs `v2.7.1`), `bash tools/test/run-all.sh` and a `v2.7.1` call-site compile canary. `tools/verify-binary-abi.sh` is information only. No new `@Deprecated(HIDDEN)` overloads or `copy$default` shims. Force a fresh Gradle run for gate evidence (up-to-date results can be another build's cache).
- **Long localized labels:** the snackbar card, the sheet target rows and the handled-by pill need a long German/Spanish ~360dp and font-scale 2.0 check before layout sign-off. English defaults hide overflow.
- **TalkBack on device (Phase 28):** v2.6's TalkBack checks were blocked at Gate-2. If the agent can't drive TalkBack on the TESTER (`R5CT10XNKQN`), park those checks for Yahir; never waive them. After UAT, restore the TESTER density with `wm density 450`, never `wm density reset`.
- **Open questions for discuss-phase:** what `FeedbackStackState.show()` does with `FeedbackEvent.DismissScreen` (Phase 25); how a sheet row drops its target after Undo / Undo all, library-derived or caller-cleared (D-10, Phase 27); which `Success` receipt row "names the item" (Phase 27).
- **Working-tree debris:** untracked `.oc-audit/`, `graphify-out/`, stage markers, `.gate/` dirs and the modified `.planning/graphs/*` / `state.json` stay out of every commit.

### Quick Tasks Completed

| # | Description | Date | Commit | Directory |
|---|-------------|------|--------|-----------|

## Deferred Items

Items acknowledged and carried forward, most recent first:

| Category | Item | Status | Deferred At | Milestone |
|----------|------|--------|-------------|-----------|
| Backlog | 999.4 voice-surface polish (pinned-bar gap, saved-summary exit, long group label, key field in flight, group count, count-editor inset, German subtitle) | Backlog, targeted at patch `v2.8.1` | 2026-10-17 | v2.8 |
| uat_gaps | 21/21-UAT.md | partial (TalkBack blocked) — signed-off-with-gap by Yahir; sweep promoted into v2.8 Phase 28 (A11Y-02) | 2026-10-14 | v2.6 |
| uat_gaps | 22/22-UAT.md | partial (TalkBack blocked) — signed-off-with-gap by Yahir; sweep promoted into v2.8 Phase 28 (A11Y-02) | 2026-10-14 | v2.6 |
| uat_gaps | 23/23-UAT.md | partial (TalkBack blocked) — signed-off-with-gap by Yahir; sweep promoted into v2.8 Phase 28 (A11Y-02) | 2026-10-14 | v2.6 |
| uat_gaps | 24/24-UAT.md | partial (TalkBack blocked) — signed-off-with-gap by Yahir; sweep promoted into v2.8 Phase 28 (A11Y-02) | 2026-10-14 | v2.6 |
| Release | v2.7.1 patch (tall sheets skip partial detent; localizable group count/status words; count-editor demo padding) | Done: `v2.7.1` cut 2026-10-14 at `f60bfc4`, Gate-1 4/4 on the TESTER | 2026-10-14 | v2.6 |
| uat_gaps | 16/16-03-SELF-UAT.md | partial (0 open scenarios) — superseded by Phase 16 Gate-2 sign-off | 2026-10-11 | v2.5 |
| Release | v2.5.1 patch tag for G-15-1 long-label fix (+ SB/CT repins) | Done: `v2.5.1` cut 2026-10-11 at `b6c363c`, JitPack-confirmed, SB + CT repinned | 2026-10-11 | v2.5 |
| Future | Two Gate-2-waived `ApproachLadderCard` UI-polish notes (KI-2026-10-01-01) | Deferred (future polish unless pulled into scope) | v2.5 requirements | v2.5 |
| Future | Consumer repins onto `v2.5.0` (SB for 172/175/177; CT optional) | Superseded: both consumers are on `v2.5.1` | v2.5 requirements | v2.5 |
| Future | Consumer wiring — SB/CT map engine outcomes → these composables' props | Deferred (Wave-1, consumers' own milestones) | v2.4 requirements | v2.4 |
| Future | GOV-04: fail the build if a new public composable ships without a `Tier` | Deferred | v2.0 requirements | v2.0 |
| Future | ECO-02: auto-repin tooling across all consumers | Deferred | v2.0 requirements | v2.0 |
| Backlog | 999.1: committed Gate-2 visualization harness APK + `AGENT-DEVICE-TESTING.md` | Backlog | v2.0 requirements | v2.0 |

## Session Continuity

Last session: 2026-10-17
Stopped at: v2.8 roadmap created (Phases 25-29); ready to plan Phase 25
Resume file: None

## Operator Next Steps

- `/gsd-discuss-phase 25` (or `/gsd-research-milestone` → `/gsd-discuss-milestone` to settle all five phases' gray areas in one pass), then `/gsd-plan-phase 25`
