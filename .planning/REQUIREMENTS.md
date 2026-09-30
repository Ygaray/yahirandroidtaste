# Requirements: yahirandroidtaste — Milestone v2.4 (AI-Voice Command UI)

**Defined:** 2026-09-29
**Core Value:** The hub stays a coherent design system as more consumers contribute — here, one domain-neutral AI-voice UI surface both SecondBrain and CalTracker can reuse.
**Cross-repo source of truth:** `~/Projects/Reusable/android/voice-action-engine/CROSS-REPO-SCOPE-CONTRACT.md` §6.3 (bindings L3/L7/A2/A12–A14/E1). Every requirement below cites its contract source.

## v2.4 Requirements

Generic, **presentational** Compose composables only. Each composable takes its content + actions as parameters; the consuming app maps engine outcomes → props. `MicButton` already exists and is not re-authored.

### Settings surfaces

- [x] **VSET-01**: A provider/API-key settings card composable renders provider selection and API-key entry purely from props + callbacks (no key persistence, no network in the library) (§6.3)
- [x] **VSET-02**: A model settings card composable renders the selected/available model(s) from props and emits selection via callback (§6.3)

### Command-approach settings (NEW)

- [x] **VAPPR-01**: A command-approach settings card composable displays the configured tier ladder (ordered approaches, e.g. Grammar → SingleShot → Plan → Agentic) from props (§6.3)
- [x] **VAPPR-02**: The command-approach card exposes an offline-only toggle that reflects and emits offline-only state via props + callback (§6.3)
- [x] **VAPPR-03**: The command-approach card exposes a max-tier cap control that reflects and emits the cap via props + callback (§6.3)

### Outcome / failure sheet

- [x] **VOUT-01**: An outcome/failure sheet composable renders a command outcome from props, domain-neutral (no app-specific nouns) (§6.3)
- [x] **VOUT-02**: The outcome sheet shows a "handled by: tier/approach" indicator identifying which tier/approach handled the command, from props (§6.3)
- [x] **VOUT-03**: The outcome sheet renders failure states loudly and visibly (prominent, not silent or subtle) (§6.3)
- [x] **VUNDO-01**: The outcome sheet renders a generic, prop-driven undo affordance — an "Undo all (N)" action alongside per-item Undo — where a per-item-undo-**unavailable** state is representable (e.g. an item entangled with another cannot be undone alone), plus a loud undo-refused / partial state (e.g. "couldn't undo: <reason>, <item> changed since"). Domain-neutral, registered with a full states matrix (A18, §6.3)
- [ ] **VCLAR-01**: A generic, prop-driven "clarification choices" composable renders a question + a list of options — each `{ id: opaque String, label: String }` — with `onSelect(id)` and a dismiss (= cancel), as a compact PRESSABLE choice surface (chips/buttons) — so the user resolves a model clarification ("Which list?") by TAPPING, never by speaking again. Visually informative, NOT an error; domain-neutral (apps map the engine's `Clarification` → these props; no engine dependency, L7); registered with a full states matrix (§6.3, A19, Yahir 2026-09-30)
- [ ] **VOUT-04**: The outcome sheet renders a generic needs-confirmation state from props — a reason string, single-or-batch proposed item(s), and confirm/cancel actions — domain-neutral so it covers both SB's `MutationGate`/`VoiceConfirmGate` risk confirm and CT's weak-match single/batch confirm (A2/E1)

### Catalog & API integrity

- [ ] **CAT-01**: Every new public composable is registered in `ComponentRegistry` (or allowlisted in `INTENTIONALLY_UNREGISTERED`) with its full 4-cell states matrix, and the CATALOG-03 drift guard passes in the **full** test suite (§6.3, CATALOG-03)
- [ ] **API-01**: The public API is strictly additive versus `v2.3.0` — Metalava `apiCheck` net-additive, no removals or signature changes to existing symbols (§6.3, §11 step 2)
- [ ] **INV-01**: The new UI adds no dependency on OkHttp or any `voice-action-engine` module; every composable takes data + actions as parameters, preserving the one-way-dependency invariant (§6.3, L7)

### Ship / tag

- [ ] **SHIP-01**: Cut library `v2.4.0` per §11 steps 1–4 (green full suite incl. CATALOG-03, Metalava additive, tagged commit pushed, JitPack resolves the coordinate from a clean cache), then message the orchestrator the full §11 ledger row (§11, A12/A14)
- [ ] **SHIP-02**: Milestone close creates **no** git tag; the only tag this milestone produces is the `v2.4.0` release coordinate, guarding against the stray milestone-marker-tag hazard (a bare `v2.2` tag already leaked into the JitPack coordinate namespace at the v2.0 close) (orchestrator condition, §11)

## Future Requirements

Deferred to consumer Wave-1 work (not this milestone).

- **Consumer wiring** — SecondBrain and CalTracker map their engine outcomes → these composables' props and adopt the settings/outcome UI (§6.4, §6.5 — consumers' own milestones)

## Out of Scope

Explicitly excluded — belongs to the engine or the consumer apps, not this presentational hub slice.

| Feature | Reason |
|---------|--------|
| Any HTTP / OkHttp client, provider transport, API-key persistence | Engine/consumer responsibility; would break the one-way-dependency invariant (INV-01, L7) |
| Tier ladder *execution*, escalation logic, `TierPolicy` enforcement | Engine (`voice-action-engine` `:core`) — YAT only *displays* the ladder/policy from props |
| Domain-specific nouns/strings in any composable | Would break reusability across SB + CT; all content arrives via props |
| `MicButton` re-authoring | Already exists (shipped in v2.0/`v2.2.0`); reused as-is |
| Cutting a git tag at milestone close | SHIP-02 — the only tag is the `v2.4.0` release coordinate via §11 |

## Traceability

Each requirement maps to exactly one phase. Roadmap: `.planning/ROADMAP.md` (Phases 10-13).

| Requirement | Phase | Status |
|-------------|-------|--------|
| VSET-01 | Phase 10 | Complete |
| VSET-02 | Phase 10 | Complete |
| VAPPR-01 | Phase 10 | Complete |
| VAPPR-02 | Phase 10 | Complete |
| VAPPR-03 | Phase 10 | Complete |
| VOUT-01 | Phase 11 | Complete |
| VOUT-02 | Phase 11 | Complete |
| VOUT-03 | Phase 11 | Complete |
| VUNDO-01 | Phase 11 | Complete |
| VCLAR-01 | Phase 11 | Pending |
| VOUT-04 | Phase 12 | Pending |
| CAT-01 | Phase 13 | Pending |
| API-01 | Phase 13 | Pending |
| INV-01 | Phase 13 | Pending |
| SHIP-01 | Phase 14 | Pending |
| SHIP-02 | Phase 14 | Pending |

**Coverage:**

- v2.4 requirements: 16 total
- Mapped to phases: 16 ✓ (Phase 10: 5, Phase 11: 5, Phase 12: 1, Phase 13: 3, Phase 14: 2)
- Unmapped: 0 ✓ (no orphans, no duplicates)

---
*Requirements defined: 2026-09-29*
*Last updated: 2026-09-30 — added VCLAR-01 (tap-to-clarify choices composable, §6.3 Yahir UX) mapped to Phase 11*
