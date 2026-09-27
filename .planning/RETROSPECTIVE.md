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

## Cross-Milestone Trends

### Process Evolution

| Milestone | Phases | Key Change |
|-----------|--------|------------|
| v1.0 — Hub Stewardship | 5 | Introduced two-tier design system, governance drift guards, repin matrix |
| v2.0 — Line Reunification | 4 | Forward-port reunification onto one line; coverage-classified UAT; escalate-with-evidence certification over buggy resolver |

### Cumulative Quality

| Milestone | Requirements | Integration | Zero-Baseline detekt |
|-----------|--------------|-------------|----------------------|
| v1.0 | (shipped, library v2.0.0) | — | green |
| v2.0 | 10/10 satisfied | CLEAN | green except pre-existing KI-2026-09-27-01 (TextCard.kt, unrelated) |

### Top Lessons (Verified Across Milestones)

1. Gate-1 device self-UAT + Gate-2 human sign-off is a strong two-gate combination for a UI library with no installable app of its own.
2. Additive-only API discipline (net-additive `api.txt`, byte-identical existing signatures) keeps consumer repins low-risk.
