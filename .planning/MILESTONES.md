# Milestones — yahirandroidtaste (Hub Stewardship)

## v2.4 AI-Voice Command UI (Shipped: 2026-10-01)

**Phases completed:** 5 phases, 7 plans, 15 tasks

**Key accomplishments:**

- Landed the Phase 10 tracer: a provider-selection dropdown + masked API-key card (`ProviderKeyCard`, VSET-01) wired end-to-end through the new tenth "Voice Command" registry family, an additively-extended `ClearableTextField`, and a full drift-guard-green + additive-`apiCheck` pass.
- Expanded the Voice Command settings surface with `ModelSelectCard` (VSET-02) and `ApproachLadderCard` (VAPPR-01/02/03) via full RED→GREEN TDD cycles, both registered in the tenth registry family with a strictly-additive API surface.
- Additive `UndoHistoryStore` grouping API (Mutex-guarded, releasable-on-Refused, group-atomic eviction) plus `OutcomeSheet`'s new "Undo all (N)" / per-item-row / loud-refused rendering, closing VUNDO-01.
- `ClarificationBar` -- a standalone, prop-driven tap-to-clarify choices surface (question + opaque-id/label options + onSelect/onDismiss), registered in the Voice Command family, closing VCLAR-01 and Phase 11.
- `VoiceOutcomeUiState.NeedsConfirmation` -- a purely additive, domain-neutral third sealed arm rendering both single-item destructive confirms (SecondBrain) and batch confirms with a shared `topLevelContent` slot and per-item remove (CalTracker), reusing `ActionButtonDefaults.ActionButtonRole` for severity with zero new public enum.
- Independently re-proved CAT-01/API-01/INV-01 green at HEAD via the Metalava v2.3.0-swap-baseline technique, captured as `13-SHIP-GATE-EVIDENCE.md`, and corrected the stale "seven"/"nine" family-count doc-drift to "ten" across CLAUDE.md, README.md, API.md, and `ComponentRegistry.kt`'s KDoc.
- Cut, pushed, and JitPack-confirmed the annotated `v2.4.0` tag on fresh green verification, autonomously per A12's waiver, and produced the section-11 ledger row for orchestrator relay — fixing two latent bugs in the plan's own verify scripts along the way.

**Gate-2:** Phases 10–13 human-signed-off (Yahir, 2026-10-01, PASS via orchestrator — the pre-agreed vae-bilingual Gate-2 relay channel); Phase 14 had no deferred device checkpoint (release-tooling phase). Audit: 16/16 requirements satisfied, cross-phase integration CLEAN, Nyquist compliant.

**Closeout type:** override_closeout — phase verifications read mtime-`stale` after the downstream Phase 13 doc + Phase 14 tag/evidence commits moved HEAD past their verification mtimes (the known mtime artifact, per RETROSPECTIVE guidance). Real state verified independently: `/gsd-certify-milestone` `all_pass` (0 gaps) and the full `testDebugUnitTest` suite + additive `apiCheck` green at the tagged commit. **Known verification overrides:** 0 newly acknowledged, 7 carried forward from a prior close (see STATE.md Deferred Items).

**Deferred (future polish, Gate-2-waived):** two non-blocking `ApproachLadderCard` UI-polish notes (combined-subdued-label right-edge crowding; light-theme capped-rung dimming) — `KNOWN-ISSUES.md` KI-2026-10-01-01. **Consumer repins** (SecondBrain `v2.3.0`→, CalTracker `v2.1.0`→ `v2.4.0`) are Wave-1, in each consumer's own channel.

---

## v2.0 Line Reunification (Shipped: 2026-09-27)

**Phases completed:** 4 phases, 7 plans, 17 tasks

**Key accomplishments:**

- Forward-ported `DateTimePicker` byte-faithful from v1.13.0 onto `main` (tier=PATTERN, gallery-navigable), and admitted `osmdroid-android`/`androidx-lifecycle-runtime-compose` as resolvable Gradle deps for Plan 06-03's `PlaceMapPicker`.
- Forward-ported `PresetChip` (2-overload public API) byte-faithful from v1.13.0 onto `main` (tier=PATTERN, gallery-navigable in the Chips family), unblocking Plan 06-03's `PlaceMapPicker` compile.
- Forward-ported the `PlaceMapPicker` cluster (4 files + 3 tests) byte-faithful from v1.13.0 onto `main` (tier=PATTERN, gallery-navigable with a live `MapView` surface), and closed out Phase 6 with a green full-suite governance battery (tests, detekt, apiCheck net-additive, publish).
- Opt-in per-tag chip container-color override (`TagChipUiModel.color` -> `AppChip`/`TagChipWithContextMenu`'s `containerColorOverride`, auto-threaded at `CardTagRow`) landed additively across all four production files. `color` was initially added as a 6th primary-constructor parameter (with `@JvmOverloads` to keep the constructor's own Metalava check green); a 2026-09-27 operator-directed code fix (see Deviations) later moved `color` out of the primary constructor entirely to close a residual `copy()` ABI break that `@JvmOverloads` could never fix.
- Parameterized MicButton's three content descriptions with generic hub-neutral defaults, routed `onTap`/`onDisabledTap` through `rememberUpdatedState` for mid-press callback-identity safety, and rewrote its KDoc to hub vocabulary — all backward-compatible, proven by a RED-then-GREEN regression test and a green governance battery (tests, zero-baseline detekt, apiCheck, mavenLocal publish).
- Hid Dagger-generated `@DaggerGenerated` symbols from the metalava-tracked surface, rebaselined `api.txt`, closed KI-2026-09-02-01, added a durable regression guard, cleared code review/security audit, and confirmed every hub gate green -- halted at the human-gated `v2.2.0` tag-cut checkpoint as designed.
- Confirmed `v2.2.0` is a real, non-cached JitPack build on the first request, reconciled the machine repin matrix (after forcing a cache refresh past a stale 1h TTL window), and hand-corrected ECOSYSTEM.md's stale "current tag" narrative with a new v2.2.0 tag-cut record and a "Pending repins" section for both consumers -- closing W-1 fully per D-03.

**Library release:** `com.github.Ygaray:yahirandroidtaste:v2.2.0` (JitPack) — the real shipped
artifact, cut at SHIP-01. Per owner decision (2026-09-27), the GSD milestone marker is tagged
`v2.2` (aligned to the release line going forward) rather than the planning version `v2.0`; unlike
v1.0 (which cut no milestone tag), this milestone carries a `v2.2` git tag. NOTE: `v2.2` ≠ the
release `v2.2.0`; consumers pin the three-part release coordinate.

**Gate-2:** Phases 6, 7, 8 human-signed-off (Yahir, 2026-09-27) via `/gsd-verify-milestone`; Phase 9
had no deferred device checkpoint. Milestone audit: **passed** — 10/10 requirements satisfied,
cross-phase integration CLEAN.

**Known verification overrides:** 7 newly acknowledged, 0 carried forward from a prior close (see
STATE.md Deferred Items) — all scanner false-positives (all_pass SELF-UATs read as `[unknown]`),
archived-v1.0 carryovers (phases 1 & 5), or tracked/accepted known-issues (`KI-2026-09-27-01`
pre-existing detekt; `KI-2026-09-02-01` metalava, already CLOSED by SHIP-01). Closeout type:
override_closeout.

---

A running ledger of shipped milestones. Newest first.

## v1.0 — Hub Stewardship ✅ SHIPPED 2026-09-02

**Library release:** `com.github.Ygaray:yahirandroidtaste:v2.0.0` (JitPack). The milestone version
`v1.0` is a GSD planning marker and is intentionally **not** a git tag — tags in this repo are
JitPack coordinates.

**Spine:** Tier Legibility → Coherence Audit → Governance Gates → Repin Bookkeeping → Gardening.
5 phases, 13 plans. The hub went from a flat, additive-only catalog to a legible, audited, governed
two-tier (primitives/patterns) design system.

**Shipped:**

- P1 — `Tier` enum on every registry entry + gallery badges + DESIGN-INTENT.md (LEG-01/02)
- P2 — COHERENCE-AUDIT.md across all 9 families + Unify Work-Order (AUD-01)
- P3 — tier-aware litmus + domain-vocabulary drift guard + pre-commit lane fix (GOV-01/02/03)
- P4 — ECOSYSTEM.md repin-matrix markers; closed INC-2026-08-28-03 (REPIN-01)
- P5 — unify (FilterBar→ChipBar, SheetHeaderMenu) + `v2.0.0` tag cut (GARD-01)

**Gate-2:** Phases 1–3 human-signed-off (Yahir, 2026-09-02); Phase 1 re-verified on-device (tester).

**Deferred (human-gated):** GARD-02 coordinated consumer repin (SecondBrain + CalTracker onto
`v2.0.0`, each Gate-1 re-verified) + `repin_status.py reconcile` (clears tech-debt W-1). Tracked for
a downstream repin phase.

Archive: [`milestones/v1.0-ROADMAP.md`](milestones/v1.0-ROADMAP.md) ·
[`milestones/v1.0-REQUIREMENTS.md`](milestones/v1.0-REQUIREMENTS.md) ·
audit: [`v1.0-MILESTONE-AUDIT.md`](v1.0-MILESTONE-AUDIT.md)
