# Phase 10: Voice command settings surfaces - Context

**Gathered:** 2026-09-29
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver three prop-driven, presentational settings composables — a provider/API-key card, a model card, and a command-approach card (tier-ladder display + offline-only toggle + max-tier cap) — that hold no key and make no network call. Everything arrives via props + callbacks.

</domain>

<decisions>
## Implementation Decisions

Resolved in `ai` mode (research recommendations adopted as working defaults; `source: ai-auto`), then updated per the **R1 reconvene GO-WITH-CHANGES** (orchestrator, 2026-09-29). The **[naming]** decision is now APPROVED (change 5).

### naming
- **D-01 [naming]:** Use structural composable names whose leading token is generic (e.g. `ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`) and widen `PRIMITIVE_NOUN_ALLOWLIST` with the new leading tokens — NOT `DOMAIN_VOCABULARY` (that self-brands the cohort as domain-coupled). The `DomainVocabularyDriftGuardTest` keys off the leading PascalCase token, and `Provider`/`Model`/`Approach`/`Command`/`Outcome`/`Voice` are all currently unlisted. — **Reversibility:** costly — renaming public composables or the allowlist after `v2.4.0` is tagged is a breaking change for both consumers. **APPROVED at R1** (orchestrator): structural names + widen `PRIMITIVE_NOUN_ALLOWLIST` with generic leading tokens (Provider/Model/Approach/Outcome/Command/Undo); do NOT grandfather full names into `DOMAIN_VOCABULARY`.

### key-entry
- **D-02 [key-entry]:** Render masked API-key entry by adding an additive `visualTransformation` + reveal-toggle parameter to `ClearableTextField` (it currently exposes none); fall back to a purpose-built masked key field only if the additive param muddies that primitive. `PasswordVisualTransformation` is available in the pinned Compose BOM (no new dependency).

### cap-control
- **D-03 [cap-control]:** Render the max-tier cap as tap-a-rung on the ladder (a cap marker with rungs-above greyed-but-present, matching conditional-render-no-dead-space); a `SingleChoiceSegmentedButtonRow` is the fallback for a short fixed ladder. `SegmentedOptionSelector` hard-requires exactly 2 options, so it can back the offline-only toggle but NOT a 3–4-tier cap. **R1:** default to tap-a-rung, and build it so the cap control is easy to swap — Yahir (design-conscious) reviews it in the gallery at Gate-1.

### prop-models
- **D-04 [prop-models]:** All settings prop-models are all-`val` immutable models in `model/` with frozen constructors. — **Reversibility:** one-way — model shapes freeze into the `v2.4.0` public API; a `var` reproduces the `TagChipUiModel` Compose-`STABLE`/`copy()` regression.

### control-visibility
- **D-05 [control-visibility]:** Every settings/approach control is optional and hideable by props — a null/absent prop means "not shown", never shown-disabled. (SB hides the offline-only toggle until it has an offline tier, and the router toggle, until engine v1.1.) _(R1 orchestrator change 2)_

### offline-capable
- **D-06 [offline-capable]:** `offlineCapable` per rung is an APP-derived prop; YAT never depends on VAE's `TierPolicy` type (L7 — no hub-to-hub edge). Props are self-contained and only need to be renderable. _(R1 orchestrator change 4)_

### Claude's Discretion
The exact cap-control widget (tap-a-rung vs segmented) may be finalized at plan/UI time against the expected tier count; both are dependency-free. Build the cap control so it is easy to swap.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone planning
- `.planning/ROADMAP.md` § Phase 10 — goal + success criteria
- `.planning/REQUIREMENTS.md` — VSET-01, VSET-02, VAPPR-01, VAPPR-02, VAPPR-03
- `.planning/v2.4-DECISION-MAP.md` § Phase 10 — the decisions above

### Research
- `.planning/research/SUMMARY.md`, `.planning/research/STACK.md` — no new deps; Material3 API map
- `.planning/research/FEATURES.md` — prop shapes; cap-on-the-ladder rationale
- `.planning/research/ARCHITECTURE.md` — package/family placement; prop-model conventions

### Cross-repo contract
- `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` §6.3 — the slice
- `.planning/cross-repo/HANDOFF.md` — §6.3 deliverables, invariants
- `CLAUDE.md` (root) — reusability invariants, CATALOG-03, toolchain

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `component/SegmentedOptionSelector.kt`: backs the offline-only 2-state toggle (but `require(options.size == 2)` — cannot back the tier cap).
- `component/ClearableTextField.kt`: wraps `OutlinedTextField`; needs an additive `visualTransformation`/reveal param for masked key entry.
- `model/` UI-model conventions (all-`val`; see `TagChipUiModel.kt` for the `copy()`/STABLE lesson).

### Established Patterns
- `ExposedDropdownMenuBox` + `PasswordVisualTransformation` ship in the pinned Compose BOM `2026.04.01` — first in-tree use here, zero new coordinates.
- Registry registration with a required `tier` field (these are `PATTERN` tier); `DomainVocabularyDriftGuardTest` head-token guard.

### Integration Points
- New composables register in `ComponentRegistry` (CATALOG-03) — likely a new "Voice Command" family (decided in Phase 13).

</code_context>

<specifics>
## Specific Ideas

Command-approach surface is ONE composable with three prop groups (ladder + offline-only + cap), per research — not three separate widgets.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

## Runtime Decisions

### 2026-09-30 — Phase 10 UAT visual items deferred to milestone Gate-2 (orchestrator ruling)

**Decision point:** Phase 10 execute stage finished with 13/13 automated must-haves verified (tests/detekt/apiCheck green live), but VERIFICATION.md was `human_needed` for two visual/interaction-feel items both plans explicitly deferred to Gate-1: (1) masked API-key reveal + provider dropdown legibility (light/dark, plan 10-01); (2) tap-a-rung cap feel + hidden-vs-shown-disabled layout (light/dark, plan 10-02).

**Ruling (yahir-gsd-control-plane-f2, 2026-09-30):** Option (a) — defer both visual items to milestone v2.4 Gate-2 and advance. This applies Yahir's existing standing policy ("Gallery Gate-1 visual review deferred to milestone-close (Yahir)", HANDOFF §Current state) — not a new decision. Conditions attached: (1) both items recorded in the pending-UAT ledger with light+dark, phase+plan refs, and the policy source [done in 10-UAT.md, skipped-with-reason]; (2) run the skipped tail gates (secure, nyquist, update_roadmap) and Gate-1 self-UAT for any non-visual behavior before Phase 11 — stop and report on any red; (3) the 6 advisory code-review warnings (WR-01…06) stay advisory but must be durably recorded (they are, in 10-REVIEW.md and 10-VERIFICATION.md Anti-Patterns).

**Applied:** 10-UAT.md → both tests `skipped` with policy reason + `## Deferred Follow-Ups` (drain at Gate-2); 10-VERIFICATION.md status canonicalized `human_needed → passed` (mirrors verify-work complete_session, lines 638-640). git.create_tag stays false; §11 remains the orchestrator's.

---

*Phase: 10-voice-command-settings-surfaces*
*Context gathered: 2026-09-29*
