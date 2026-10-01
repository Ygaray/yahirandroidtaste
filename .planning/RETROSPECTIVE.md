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

## Cross-Milestone Trends

### Process Evolution

| Milestone | Phases | Key Change |
|-----------|--------|------------|
| v1.0 — Hub Stewardship | 5 | Introduced two-tier design system, governance drift guards, repin matrix |
| v2.0 — Line Reunification | 4 | Forward-port reunification onto one line; coverage-classified UAT; escalate-with-evidence certification over buggy resolver |
| v2.4 — AI-Voice Command UI | 5 | Cross-repo orchestrator-driven milestone run + Gate-2-via-orchestrator relay; autonomous A12 tag cut; isolated tag-cut phase |

### Cumulative Quality

| Milestone | Requirements | Integration | Zero-Baseline detekt |
|-----------|--------------|-------------|----------------------|
| v1.0 | (shipped, library v2.0.0) | — | green |
| v2.0 | 10/10 satisfied | CLEAN | green except pre-existing KI-2026-09-27-01 (TextCard.kt, unrelated) |
| v2.4 | 16/16 satisfied | CLEAN | green (zero baseline) |

### Top Lessons (Verified Across Milestones)

1. Gate-1 device self-UAT + Gate-2 human sign-off is a strong two-gate combination for a UI library with no installable app of its own.
2. Additive-only API discipline (net-additive `api.txt`, byte-identical existing signatures) keeps consumer repins low-risk.
