# Roadmap: yahirandroidtaste — Hub Stewardship

## Milestones

- ✅ **v1.0 — Hub Stewardship** — Phases 1-5 (shipped 2026-09-02, library `v2.0.0`)
- 🚧 **v2.0 — Line Reunification** — Phases 6-9 (in progress, cuts library `v2.2.0`)

## Completed Milestones

<details>
<summary>✅ v1.0 — Hub Stewardship (Phases 1-5) — SHIPPED 2026-09-02, library <code>v2.0.0</code></summary>

The hub went from a flat, additive-only catalog to a legible, audited, governed two-tier design
system — Tier Legibility → Coherence Audit → Governance Gates → Repin Bookkeeping → Gardening (unify
shipped; the coordinated consumer repin, GARD-02, was deferred human-gated and is now **absorbed into
v2.0's Phase 9** onto `v2.2.0`). Full detail:
[`.planning/milestones/v1.0-ROADMAP.md`](milestones/v1.0-ROADMAP.md).

</details>

## Active Milestone: v2.0 — Line Reunification

**Milestone Goal:** Collapse the divergent v1.x (SecondBrain's `v1.13.0`) and v2.x/`main`
(CalTracker's `v2.1.0`) release lines into one forward line on `main`, cut as library `v2.2.0`, so
every consumer converges on one tag and the v1.x line retires — while keeping every v2.x improvement.
Also lands two additive riders (per-tag chip color, MicButton hardening), completes v1.0's deferred
**GARD-02** coordinated repin (now onto `v2.2.0`), and clears tech-debt **W-1**.

## Phases

**Phase Numbering:**

- Integer phases (6, 7, 8, 9): planned milestone work (continues v1.0's 1-5).
- Decimal phases (e.g., 6.1): urgent insertions, if any (marked INSERTED).

- [ ] **Phase 6: Forward-port reunification** - Port the six v1.x-only components onto `main` net-additively, admit `osmdroid`, keep both drift guards green
- [ ] **Phase 7: Chip-color slot** - Add an opt-in, backward-compatible per-tag chip container-color override
- [ ] **Phase 8: MicButton hardening** - Make `MicButton` consumer-agnostic and callback-correct (CalTracker findings)
- [ ] **Phase 9: Ship & coordinated repin** - Cut library `v2.2.0` (human-gated), surface the coordinated consumer repins, reconcile the ECOSYSTEM.md matrix

## Phase Details

### Phase 6: Forward-port reunification

**Goal**: `main` gains the six v1.x-only components (`DateTimePicker`, the `PlaceMap*` cluster, `PresetChip`) net-additively, so the forward line carries both lines' capabilities and the v1.x line can retire.
**Depends on**: Nothing (first phase of v2.0; distinct files — parallelizable with Phases 7 and 8)
**Requirements**: REUNI-01, REUNI-02, REUNI-03, REUNI-04
**Success Criteria** (what must be TRUE):

  1. `DateTimePicker`, `PlaceMapPicker`, and `PresetChip` each render in the ExplorerActivity gallery (family-screen previews).
  2. The 5 ported tests (`DateTimePickerTest`, `PlaceMapPickerTest`, `PlaceMapOsmdroidConfigTest`, `PlaceMapPickerModelTest`, `PresetChipTest`) pass under `./gradlew testDebugUnitTest`.
  3. The three public composables are each registered in exactly one `ComponentRegistry` family list; the ComponentRegistry integrity test and the CATALOG drift guard pass.
  4. `osmdroid` is recorded in `.planning/APPROVED-DEPS.md` and `CLAUDE.md` allowed-deps (impl-scope; ships in the `.aar`), and the head tokens (`Date`, `Preset` → `PRIMITIVE_NOUN_ALLOWLIST`; `Place` → `DOMAIN_VOCABULARY` w/ rationale) are allowlisted so `DomainVocabularyDriftGuardTest` stays green.
  5. `detekt` stays green at zero baseline (no new baseline banked).

**Plans**: 3 plans

Plans:
**Wave 1**

- [ ] 06-01-PLAN.md — Tracer: DateTimePicker restored/registered/gallery-rendering end-to-end + admit osmdroid/androidx-lifecycle-runtime-compose

**Wave 2** *(blocked on Wave 1 completion)*

- [ ] 06-02-PLAN.md — PresetChip (both overloads) restored, registered, allowlisted

**Wave 3** *(blocked on Wave 2 completion)*

- [ ] 06-03-PLAN.md — PlaceMapPicker cluster restored, registered, allowlisted; phase-closing governance battery

**UI hint**: yes

### Phase 7: Chip-color slot

**Goal**: A caller can opt into a per-tag chip container color, backward-compatibly, with the hub rendering the supplied color as-is (consumer owns the muted/theme-aware policy).
**Depends on**: Nothing (additive rider, independent of Phase 6; distinct files — parallelizable with Phases 6 and 8)
**Requirements**: TAGCOLOR-01
**Success Criteria** (what must be TRUE):

  1. `TagChipUiModel` carries `color: Color? = null`; `AppChip` and `TagChipWithContextMenu` carry `containerColorOverride: Color? = null`.
  2. A `CardTagRow` test proves each `tag.color` auto-threads to the rendered chip's `containerColorOverride` (both the `TagChipWithContextMenu` and plain `AppChip` render paths).
  3. An `AppChip` render test with a non-null override renders the overridden container, and the default-`null` path is byte-identical to today's theme-role rendering.
  4. The override loses to `isSelected`/`relatednessStrength` (theme roles win when selection is active) — asserted by test.
  5. No new public composables are added; `api.txt` is updated additively and `apiCheck`, both drift guards, and zero-baseline `detekt` stay green.

**Plans**: TBD
**UI hint**: yes

### Phase 8: MicButton hardening

**Goal**: `MicButton` is consumer-agnostic and callback-correct — no hardcoded microcopy, latest-callback safety, hub-vocabulary KDoc, sensible defaults — ready for SecondBrain to adopt.
**Depends on**: Nothing (additive rider, independent of Phase 6; distinct files — parallelizable with Phases 6 and 7)
**Requirements**: MICBTN-01, MICBTN-02, MICBTN-03
**Success Criteria** (what must be TRUE):

  1. `MicButton`'s three content descriptions are parameters with generic neutral defaults (`disabledDescription = "Microphone unavailable"`, `tapToTalkDescription = "Tap to talk"`, `listeningDescription = "Listening…"`); no CalTracker-specific microcopy remains in the source.
  2. `onTap`/`onDisabledTap` fire the latest callback identity across recomposition (routed through `rememberUpdatedState`), proven by a regression test that flips callback identity mid-press.
  3. `MicButton` KDoc uses hub vocabulary (`enabled`, not `config`), and `enabled`/`onDisabledTap` have defaults (`true` / `{}`).
  4. All changes are backward-compatible (existing call sites compile unchanged); `testDebugUnitTest`, both drift guards, and zero-baseline `detekt` stay green.

**Plans**: TBD

### Phase 9: Ship & coordinated repin

**Goal**: Cut library `v2.2.0` (human-gated) once all gates pass, surface the coordinated consumer repins, and reconcile the hub's own ECOSYSTEM.md matrix — completing v1.0's GARD-02 and clearing W-1.
**Depends on**: Phase 6, Phase 7, Phase 8 (gates on all three)
**Requirements**: SHIP-01, SHIP-02
**Success Criteria** (what must be TRUE):

  1. All hub gates pass on `main`: `testDebugUnitTest`, zero-baseline `detekt`, ComponentRegistry + DomainVocabulary drift guards, `apiCheck`, and `publishReleasePublicationToMavenLocal`.
  2. An immutable annotated tag `v2.2.0` exists on `main` at `origin`, and JitPack builds the coordinate `com.github.Ygaray:yahirandroidtaste:v2.2.0` (verified resolvable). Human-gated tag cut — surfaced for the owner's go-ahead, not cut autonomously.
  3. The hub's `ECOSYSTEM.md` repin matrix is reconciled via `repin_status.py reconcile`, clearing tech-debt W-1.
  4. The coordinated repin is surfaced for the owner (tag + each consumer's bump path) and runs in each **consumer's own channel** per the cross-repo-hub convention: SecondBrain single repin `v1.13.0→v2.2.0` (+ its own FilterBar→ChipBar migration + color wiring) and CalTracker `v2.1.0→v2.2.0` (purely additive, passes its own disabled copy) — the hub run edits **no consumer repos**.

**Plans**: TBD

## Progress

**Execution Order:**
Phases 6, 7, and 8 are mutually independent (distinct files) — parallelizable as waves. Phase 9 gates
on all three. Numeric order: 6 → 7 → 8 → 9.

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 6. Forward-port reunification | v2.0 | 0/3 | Not started | - |
| 7. Chip-color slot | v2.0 | 0/TBD | Not started | - |
| 8. MicButton hardening | v2.0 | 0/TBD | Not started | - |
| 9. Ship & coordinated repin | v2.0 | 0/TBD | Not started | - |

## Backlog

### Phase 999.1: Formalize reusable Gate-2 visualization harness APK (BACKLOG)

**Goal:** Promote the throwaway same-package-Intent harness — which every Gate-1 agent currently re-derives from scratch — into a committed, launchable Gate-2 visualization app for this library-only repo.

**Requirements:** TBD

**Plans:** 0 plans

Context:

- `yahirandroidtaste` is a pure `com.android.library` (no `applicationId`), so it ships **no installable APK**. Human Gate-2 on-device review therefore has nothing to open. Every Gate-1 self-UAT run rebuilds the same throwaway harness (a 1-Activity app that depends on the mavenLocal AAR and `startActivity(Intent(this, ExplorerActivity::class.java))`) just to see the gallery — documented in `01-05-SELF-UAT.md`'s "Driver-mechanism note" and re-derived by later verify sessions too.
- Deliverables to scope when promoted: (a) a committed harness — a dedicated app module or a gradle task that assembles an installable debug APK opening `ExplorerActivity`; (b) a project-local `AGENT-DEVICE-TESTING.md` documenting the same-package-harness driver pattern (the SELF-UAT logs explicitly recommend authoring one so future Gate-1 runs don't re-derive it).
- **Invariant guard:** harness → library only, never the reverse (one-way dependency). The harness is host/consumer-side tooling; it must name no library-internal concepts and must not become something the library depends on.

Plans:

- [ ] TBD (promote with /gsd-review-backlog when ready)
