# Requirements: yahirandroidtaste — Line Reunification (v2.0)

**Defined:** 2026-09-26
**Core Value:** The hub stays a coherent design system as more consumers contribute — legible and prunable.
**Milestone goal:** Reunify the divergent v1.x / v2.x lines onto one forward line on `main`, cut as
library `v2.2.0`; complete v1.0's deferred GARD-02 coordinated repin and clear tech-debt W-1.
**Design spec:** `docs/superpowers/specs/2026-09-26-hub-line-reunification-design.md`

## v1 Requirements

Requirements for this milestone. Each maps to exactly one roadmap phase.

### Reunification (forward-port v1.x-only components onto `main`)

- [x] **REUNI-01**: `DateTimePicker` is available on `main` — forward-ported from `v1.13.0` with its test, registered in `ComponentRegistry`, and shown in the ExplorerActivity gallery.
- [x] **REUNI-02**: The `PlaceMap*` cluster (`PlaceMapPicker`, `PlaceMapOsmdroidConfig`, `PlaceMapPickerModel`, `model/SavedPlaceUiModel`) is available on `main` — forward-ported with its 4 tests, registered, and shown in the gallery.
- [x] **REUNI-03**: `PresetChip` is available on `main` — forward-ported with its test, registered, and shown in the gallery.
- [x] **REUNI-04**: `osmdroid` is admitted as an approved implementation-scope dependency (`.planning/APPROVED-DEPS.md` + `CLAUDE.md` allowed-deps, noting it ships in the `.aar`), and the ported composables' domain-vocabulary head tokens (`Date`, `Preset` → primitive; `Place` → domain-vocab w/ rationale) are allowlisted so both drift guards stay green.

### Chip color (consumer-requested additive capability)

- [x] **TAGCOLOR-01**: A caller can render a per-tag chip in an opt-in color — `color: Color?` on `TagChipUiModel`, `containerColorOverride: Color?` on `AppChip` and `TagChipWithContextMenu`, auto-threaded from `tag.color` at `CardTagRow`. Backward-compatible (defaulted `null` = today's theme rendering); the hub renders as-is (consumer owns the muted/theme-aware policy); override loses to `isSelected`/`relatednessStrength`.

### MicButton hardening (reusability + correctness)

- [x] **MICBTN-01**: `MicButton` carries no consumer-specific microcopy — its three content descriptions are parameters with generic defaults (`disabledDescription = "Microphone unavailable"`, `tapToTalkDescription`, `listeningDescription`) so any consumer can reword/localize (WR-01).
- [x] **MICBTN-02**: `MicButton`'s `onTap`/`onDisabledTap` fire the latest callback identity across recomposition (routed through `rememberUpdatedState`), with a regression test that flips callback identity mid-press (WR-02).
- [x] **MICBTN-03**: `MicButton` KDoc uses hub vocabulary (`enabled`, not `config`), and `enabled`/`onDisabledTap` have sensible defaults (`true` / `{}`) (IN-01, IN-02).

### Ship & converge (completes v1.0's GARD-02, now onto `v2.2.0`)

- [ ] **SHIP-01**: Library `v2.2.0` is cut — an immutable annotated tag on `main` after all gates pass (`testDebugUnitTest`, `detekt` zero-baseline, both drift guards, `apiCheck`, `publishReleasePublicationToMavenLocal`). Human-gated tag cut.
- [ ] **SHIP-02**: Both consumers are coordinated onto `v2.2.0` (SecondBrain single repin + FilterBar→ChipBar migration in its own channel; CalTracker `v2.1.0→v2.2.0`), and the hub's ECOSYSTEM.md repin matrix is reconciled via `repin_status.py reconcile` (clears tech-debt W-1). Each consumer Gate-1 runs in its own channel per the cross-repo convention.

## Future Requirements

Deferred, tracked, not in this roadmap.

- **ECO-02**: Auto-repin tooling across all consumers (carried from v1.0).
- **GOV-04**: Fail the build if a new public composable ships without a `Tier` (carried from v1.0).
- **Backlog 999.1**: Committed Gate-2 visualization harness APK + `AGENT-DEVICE-TESTING.md`.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Restoring standalone `FilterBar` | Kept the v2.0.0 ChipBar consolidation (owner decision); restoring re-creates the duplicate the coherence audit removed |
| Additive v1.14.0 cherry-pick onto the v1.x line | Rejected in favor of forward reunification — perpetuating two lines is the liability being eliminated |
| SB's on-device Anthropic voice pivot | App-local; zero hub surface (SB confirmed) |
| Hub-side muting / contrast policy for chip color | Consumer owns the muted, theme-aware color policy; the hub renders as-is to stay domain-free |
| Forcing consumers onto a shared pin / lockstep | Convergence coordinates repins, it doesn't mandate lockstep |

## Traceability

Filled during roadmap creation (2026-09-26).

| Requirement | Phase | Status |
|-------------|-------|--------|
| REUNI-01 | Phase 6 | Complete |
| REUNI-02 | Phase 6 | Complete |
| REUNI-03 | Phase 6 | Complete |
| REUNI-04 | Phase 6 | Complete |
| TAGCOLOR-01 | Phase 7 | Complete |
| MICBTN-01 | Phase 8 | Complete |
| MICBTN-02 | Phase 8 | Complete |
| MICBTN-03 | Phase 8 | Complete |
| SHIP-01 | Phase 9 | Pending |
| SHIP-02 | Phase 9 | Pending |

**Coverage:**

- v1 requirements: 10 total
- Mapped to phases: 10 ✓
- Unmapped: 0 ✓

---
*Requirements defined: 2026-09-26*
*Last updated: 2026-09-26 after milestone v2.0 roadmap creation (Phases 6-9)*
