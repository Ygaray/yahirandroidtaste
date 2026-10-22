# Project Retrospective

*A living document updated after each milestone. Lessons feed forward into future planning.*

## Milestone: v2.0 — Line Reunification

**Shipped:** 2026-09-27
**Phases:** 4 (6–9) | **Plans:** 7 | **Tasks:** 17

### What Was Built
- Forward-ported the six v1.x-only components onto `main` net-additively — `DateTimePicker`, the `PlaceMap*` cluster (`PlaceMapPicker`/`PlaceMapOsmdroidConfig`/`PlaceMapPickerModel`/`SavedPlaceUiModel`), and `PresetChip` (both overloads) — each byte-faithful from `v1.13.0`, registered PATTERN-tier, gallery-navigable; admitted `osmdroid` + `androidx-lifecycle-runtime-compose` as approved deps.
- Opt-in per-tag chip container-color slot (`TagChipUiModel.color` → `AppChip`/`TagChipWithContextMenu.containerColorOverride`, auto-threaded at `CardTagRow`), backward-compatible, precedence `isSelected > relatedness > override > else`.
- MicButton hardening — neutral parameterized microcopy, latest-callback safety via `rememberUpdatedState`, hub-vocabulary KDoc — ready for SecondBrain adoption.
- Cut library `v2.2.0` (human-gated), closed `KI-2026-09-02-01` (hid Dagger-generated symbols from the metalava surface + durable regression guard), reconciled the ECOSYSTEM.md repin matrix, cleared tech-debt W-1.

### What Worked
- Gate-1 self-UAT device verification (real Samsung SM-S908U) was thorough and adversarial — the PlaceMapPicker "errors and resolving" variant was deliberately chosen as a falsifying probe for the "saved-places absent when empty" claim and settled it correctly. Gate-2 was then a fast, high-confidence sign-off.
- Coverage-classification (`auto_passed` vs `present`) kept human UAT focused on genuinely device-only checkpoints; phase 8 was fully auto-covered with zero human checkpoints.
- The additive-only discipline held: `api.txt` net-additive across all four phases, `TagChipUiModel.copy()`/`component1..5` byte-identical to the pre-Phase-7 shape.

### What Was Inefficient
- `certify-status` produced 8 false-positive "gaps" from resolver/parser defects (non-canonical `verdict:` vs `result:` keys, `*-NYQUIST.md` vs `*-VALIDATION.md` naming, suffixed `09-01/02` vs canonical single-file expectations), forcing an escalate-with-evidence certification instead of a clean pass. Tracked as INC-2026-09-25-01 / -09-27-03 / -09-17-02.
- Verification "staleness" is mtime-based and re-trips whenever a *later* phase commits source (Phase 9's metalava fix re-staled Phase 6), even when the earlier phase's own deliverables are untouched — noisy for milestone close.

### Patterns Established
- On this pure-`com.android.library` repo, Gate-1 re-derives a throwaway same-package-Intent harness every run (no installable APK). Promotion to a committed harness is backlogged as Phase 999.1.
- Milestone versions are GSD planning markers; the real release is the JitPack semver tag. This milestone departed slightly — tagged `v2.2` (owner decision) to align the marker with the release line going forward.

### Key Lessons
1. When `certify-status` reports gaps, verify against the raw artifacts before churning fix-agents — the resolver-defect family here manufactures false-positives from filename/key mismatches.
2. Treat verification "stale" at milestone close as a mtime artifact: confirm whether the *phase's own* covered source changed (vs a downstream phase committing), then proceed on the real state.

### Cost Observations
- Model mix: predominantly Opus (driver + integration checker); no separate cost telemetry captured this milestone.
- Notable: memory capture (claude-mem) was offline (openrouter allowance exhausted) for the entire close — no observations persisted.

---

## Milestone: v2.4 — AI-Voice Command UI

**Shipped:** 2026-10-01 (library `v2.4.0`)
**Phases:** 5 (10–14) | **Plans:** 7 | **Tasks:** 15

### What Was Built
- Tenth "Voice Command" `ComponentRegistry` family: `ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard` (settings), `OutcomeSheet` (handled-by indicator + loud failure + generic undo + needs-confirmation), `ClarificationBar` (tap-to-clarify) — all prop-driven, engine-free (INV-01), strictly additive vs `v2.3.0`.
- Additive `VoiceOutcomeUiState.NeedsConfirmation` sealed arm covering SB risk-confirm and CT weak-match/batch confirm with zero per-consumer library change.
- Cut library `v2.4.0` autonomously (A12 tag-gate waiver), JitPack-confirmed, §11 ledger relayed to the orchestrator.

### What Worked
- The vae-bilingual orchestrator-driven close: Gate-2 batched at the shared device with the owner, verdict relayed and recorded with explicit "via orchestrator" provenance — cleanly decoupled the headless hub session from the human's physical presence.
- Isolating the immutable tag cut in its own phase (14, split from 13) so the cut provably followed a green Phase 13 (§11 step 1).
- A live A3 gallery fixture added post-tag (explorer-only, drift-guard-denylisted) made the one screenshot-only Gate-2 item reviewable on-device without touching the tag or public API.

### What Was Inefficient
- Phase verifications re-staled (mtime) after the downstream Phase 13 doc + Phase 14 tag commits, forcing an override_closeout again — the same noise flagged at v2.0's close; still no mtime-vs-content discrimination at milestone close.
- A transient session rate-limit killed the Phase 13 plan stage mid-run; recovered cleanly on re-drive, but a long autonomous milestone run stays exposed to session-limit interruptions.

### Patterns Established
- Gate-2-via-orchestrator relay for an owner who reviews at a shared device while the repo session stays headless.
- Post-tag, gallery-only review fixtures (additive, explorer-denylisted) as a sanctioned way to make a deferred visual item reviewable without disturbing an immutable release.

### Key Lessons
1. For a library repo the **tag**, not `main`, is what consumers resolve — `main` can lag `origin` without affecting the coordinate, but back it up deliberately rather than letting the whole milestone history sit local-only.
2. Dispatch GSD-generated child prompts **verbatim** (INC-2026-09-30-04): adding even accurate, CONTEXT-redundant claims to a child prompt is a protocol deviation; the planner reads CONTEXT.md regardless.

### Cost Observations
- Model mix: Opus driver + Sonnet milestone-phase orchestrators/executors; one agentic-tester run (device).
- Notable: claude-mem capture degraded again (opencode outage at session start); one session rate-limit mid-run.

---

## Milestone: v2.5 — Voice UI Localization & Accessibility

**Shipped:** 2026-10-11 (library `v2.5.0`; G-15-1 fix pending as `v2.5.1`)
**Phases:** 5 | **Plans:** 12

### What Was Built
- Caller-localizable labels on the four settings/voice-surface composables, plus the OutcomeSheet literals moved into defaulted model fields. English defaults are byte-identical.
- `ApproachLadderCard` rung rows get the minimum interactive size and selected-radio semantics. `Failure` gets an action role, a body slot and a semantics prefix.
- A `require()`-paired Router ON/OFF toggle on `ApproachLadderCard`.
- An informational binary-ABI javap gate (`tools/verify-binary-abi.sh`), and the immutable `v2.5.0` cut.
- After the tag, the G-15-1 long-label layout fix: a private `RungLabelLayout` and a segmented-label icon-slot reservation.

### What Worked
- **Gate-2 on a real phone with deliberately hostile input** (long German labels) caught a latent layout bug that the unit tests had missed since v2.4: the rung name went to 0 width. The automated suite used fake text measurement.
- **The code-review gate after gap closure.** It caught that the first fix (a fixed 60% cap) regressed the English defaults. The reviewer proved it by measuring both the old and new code with real text measurement.
- **Requiring each fix's tests to fail against the old code.** It exposed two guard tests that passed trivially.

### What Was Inefficient
- **`/gsd-verify-milestone --personal:<port>` can't install for a library repo.** It expects an app APK; the Gate-2 harness APK had to be rebuilt by hand (backlog 999.1).
- **A post-tag gap closure made every later phase's VERIFICATION fingerprint stale.** It took a manual re-confirmation of each verdict, then a fingerprint refresh, before close.
- **The code-review scope auto-detection over-reached.** Its cross-check against the diff since the last review would have pulled in every file from phases 16–18, so the review was narrowed to 15-04's files by hand.

### Patterns Established
- **Long-label torture fixtures**, both a gallery cell and NATIVE-graphics Robolectric geometry tests, for any composable whose labels the caller can override.
- **Ship post-tag gap fixes as a patch tag (`v2.5.1`)**, never by moving the immutable tag.

### Key Lessons
1. Making a label caller-overridable creates a new layout contract (arbitrary length). Test it at the same time as the param, not at Gate-2.
2. Fake text measurement hides wrap and starvation bugs. Use real (NATIVE) text measurement for any geometry assertion about text.
3. A fix needs a test that fails without it; a guard that passes on the broken code is not a guard.

### Cost Observations
- Sessions: the original 2026-10-05 run, plus a 2026-10-11 close-out session (gap execution, review/fix, re-verify, Gate-2, close).
- Notable: the review-plus-fix loop, about 25 minutes of agent time, prevented shipping an English-label regression in `v2.5.1`.

## Milestone: v2.6 — Receipt voice surfaces + ProviderKeyCard lifecycle

**Shipped:** 2026-10-14 (library `v2.6.0`; follow-up `v2.7.0` for CalTracker 76-14 on 2026-10-13)
**Phases:** 5 | **Plans:** 19

### What Was Built
- An opt-in `OutcomeSheet(style = Receipt)` for NeedsConfirmation, Success and Failure, with a pinned
  bar, Y-14 stable test tags, an in-flight action and XR-178-01 a/b. Classic stays pixel-identical to
  v2.5.1.
- Atoms: tone tokens, the button emphasis axis, `RoundIconButton`, `StackedProgressBar`, `ToneCard`,
  `PillChip`, `ReceiptRow`, a Pill selector with `disabledOptions`, and a `SheetScaffold` shape.
- `ProviderKeyCard` Save/Update/Clear/Cancel with a fingerprint-only saved summary, and Undo Center
  group rows.
- Explorer demos for all of it, 33 TESTER screencaps for the owner, and the `v2.6.0` cut.

### What Worked
- **Using the sketch as a signed decision record.** Writing `v2.6.0-SKETCH.md` and having CT and SB
  sign it off before planning gave zero signature deltas across five phases.
- **Goldens captured before the first src edit.** They proved "default call = v2.5.1 pixels" by
  diff sum 0, with no screenshot tooling.
- **The cut first, then the screencap review (Y-15).** The tag shipped on gates and owner taste was
  judged after it; an objection would have shipped as a patch. Yahir signed it off with no v2.6.1.

### What Was Inefficient
- **Mid-milestone tags made the close's gates stale.** `v2.7.0` landed on `main` before v2.6 closed,
  so phase 24's security and verification had to be re-run at HEAD before settle-phase would pass,
  and the stage marker then waited out its quiesce window.
- **The archive move silently dropped gitignored evidence.** `*.log` is ignored, so the six signed
  logcat captures had to be force-added again at the archive path (`da89f92`).
- **The TalkBack Gate-2 items had no consumer to run in.** All three went to a backlog sweep rather
  than being checked.

### Patterns Established
- **Experimental-typed params become a separate overload** (`OutcomeSheet` + `SheetState`), not a
  defaulted param, so callers need no opt-in.
- **The additivity tool's accepted exceptions are keyed by baseline tag.** For example the v2.6.0
  `NeedsConfirmation` R5/R6 exceptions, which come with the owner's acceptance.

### Key Lessons
1. Release tags cut for a downstream app mid-milestone are fine, but plan the close to re-run the
   gates that cover the touched files.
2. When archiving, check renames against `git ls-files`. An ignore rule can turn a tracked file into a
   silent deletion.
3. Hard accessibility checks (TalkBack) need a consumer surface or a committed harness; library-only
   Gate-1 can't reach them.

### Cost Observations
- Sessions: the 2026-10-12 milestone run plus the cut, a 2026-10-13 v2.7.0 cut, and a 2026-10-14
  close (settle, certify, Gate-2 via the teamlead relay, audit, complete).

## Milestone: v2.8 — Snackbar go-to-item + stacked feedback

**Shipped:** 2026-10-22 (library `v2.8.0`, tag cut by Yahir at `ce855d2` on 2026-10-18)
**Phases:** 5 | **Plans:** 34

### What Was Built
- An opaque `FeedbackTarget(key, openLabel)` on events, outcome Success, batch rows and Undo history,
  opened through one caller `onOpenTarget` from a snackbar card, a sheet row or an Undo Center row.
- `FeedbackStackState` / `FeedbackStackHost`: a Receipt-card deck of up to three with fan, swipe,
  pausing timers, exactly-one results and tracked undo through the store guard.
- A scoped TalkBack sweep, the hub scheme pinned across the Explorer, the capped handled-by pill, the
  warn-dot contrast ring, and an OSM user agent that reaches the tile server.
- Release tooling (`verify-publish-range.sh`, `verify-junit-fresh.sh`) and a DE/ES long-string Gate-1.

### What Worked
- **A locked brief before planning** (D-01..D-12 in `v2.8-BRIEF.md`): every phase ran without a
  scope question back to the owner.
- **Gate-1 caught real defects on the TESTER** that unit tests passed: the peek tap over a back card's
  Undo (26-06), TalkBack's silent middle strip and doubled speech (28-10..28-12), and the clipped
  Spanish card at font 2.0 (29-07).
- **Gate-2 on the published artifact.** A throwaway Explorer built straight from JitPack `v2.8.0` let
  the owner test exactly what consumers get.

### What Was Inefficient
- **Phase 28 used all three gap cycles** chasing TalkBack speech behaviour that only a real device
  shows; the last five gestures still could not be synthesized and were left for Gate-2.
- **Close-time tooling voided a signed drain.** `audit-open acknowledge` writes a marker into the
  SELF-UAT frontmatter, which the Gate-2 drain is bound to, so Phase 28 had to be re-signed.
- **The archive move hit the `*.log` ignore rule again** (as in v2.6); the tracked evidence had to be
  force-added at the archive path.
- **A stale `.git/index.lock`** from 2026-10-18 and a leftover blocked shell from Phase 25 sat for days.

### Patterns Established
- **Opaque keys, not lambdas, for navigation targets** stored in singletons.
- **Deck internals stay internal**: fixes like the card scroll and the capped Undo (29-07) land after
  device testing without touching `api.txt`.
- **Build the Gate-2 app from the published coordinate**, not mavenLocal.

### Key Lessons
1. TalkBack behaviour needs a device loop early; plan the physical-gesture checks as owner steps from
   the start instead of discovering them at the third gap cycle.
2. Any tool that edits a SELF-UAT file after a drain must re-drain, or not edit it at all; report it.
3. Before an archive move, diff `git ls-files` against the destination and force-add ignored tracked files.

### Cost Observations
- Model mix: orchestration on Opus, phase stages and leaf agents on Sonnet (per the milestone runner).
- Sessions: one long ui-designer session (with a park and resume) plus GSD subagents.
- Notable: 314 commits from `v2.7.1` to the tag; Phase 28 alone took 12 plans.

## Cross-Milestone Trends

### Process Evolution

| Milestone | Phases | Key Change |
|-----------|--------|------------|
| v1.0 — Hub Stewardship | 5 | Introduced two-tier design system, governance drift guards, repin matrix |
| v2.0 — Line Reunification | 4 | Forward-port reunification onto one line; coverage-classified UAT; escalate-with-evidence certification over buggy resolver |
| v2.4 — AI-Voice Command UI | 5 | Cross-repo orchestrator-driven milestone run + Gate-2-via-orchestrator relay; autonomous A12 tag cut; isolated tag-cut phase |
| v2.5 — Voice UI Localization & Accessibility | 5 | Gate-2 with hostile-input fixtures reopened a phase post-tag; gap closure → code-review → re-verify loop; patch-tag follow-up instead of tag mutation |
| v2.6 — Receipt voice surfaces + ProviderKeyCard lifecycle | 5 | Consumer-signed sketch as the decision record; cut-before-screencap-review (Y-15); mid-milestone release tag for a consumer ask; Gate-2 relayed via the teamlead |
| v2.8 — Snackbar go-to-item + stacked feedback | 5 | Locked brief (D-01..D-12) up front; three-cycle device gap closure for TalkBack; owner-cut tag before Gate-2; Gate-2 on an app built from the JitPack artifact |

### Cumulative Quality

| Milestone | Requirements | Integration | Zero-Baseline detekt |
|-----------|--------------|-------------|----------------------|
| v1.0 | (shipped, library v2.0.0) | — | green |
| v2.0 | 10/10 satisfied | CLEAN | green except pre-existing KI-2026-09-27-01 (TextCard.kt, unrelated) |
| v2.4 | 16/16 satisfied | CLEAN | green (zero baseline) |
| v2.5 | 14/14 satisfied | WIRED (2 combined-scenario test-gap warnings) | green (zero baseline) |
| v2.6 | 27/27 satisfied | WIRED (2 advisory warnings) | green (zero baseline) |
| v2.8 | 20/20 satisfied | WIRED (4 warnings, W1-W4, to v2.8.1) | green (zero baseline) |

### Top Lessons (Verified Across Milestones)

1. Gate-1 device self-UAT + Gate-2 human sign-off is a strong two-gate combination for a UI library with no installable app of its own.
2. Additive-only API discipline (net-additive `api.txt`, byte-identical existing signatures) keeps consumer repins low-risk.
