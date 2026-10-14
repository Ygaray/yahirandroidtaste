# Roadmap: yahirandroidtaste — Hub Stewardship

## Milestones

- ✅ **v1.0 — Hub Stewardship** — Phases 1-5 (shipped 2026-09-02, library `v2.0.0`)
- ✅ **v2.0 — Line Reunification** — Phases 6-9 (shipped 2026-09-27, library `v2.2.0`)
- ✅ **v2.4 — AI-Voice Command UI** — Phases 10-14 (shipped 2026-10-01, library `v2.4.0`)
- ✅ **v2.5 — Voice UI Localization & Accessibility** — Phases 15-19 (shipped 2026-10-11, library `v2.5.0`; the G-15-1 long-label fix shipped as patch `v2.5.1` on 2026-10-11)
- 🚧 **v2.6 — Receipt voice surfaces + ProviderKeyCard lifecycle** — Phases 20-24 (in progress, ships library `v2.6.0`)

## Phases

### 🚧 v2.6 — Receipt voice surfaces + ProviderKeyCard lifecycle (In Progress)

**Milestone Goal:** Ship library `v2.6.0`, an additive, opt-in minor: an opt-in "Receipt" rendering
of the voice outcome sheet plus the atoms it needs, and the full key lifecycle on `ProviderKeyCard`.
Every default reproduces today's pixels, so SecondBrain stays on Classic, unchanged, and recompiles
with no edits. Decision record: `.planning/seams/v2.6.0-SKETCH.md` (`105255d`; signed off by CT and
SB with no signature deltas; P1-P3 rulings applied). Inputs: CT `76-UI-SPEC.md` Y-1..Y-15
(`0a1da128`), SB `178-XR-01.md`. Phase numbering continues from v2.5 (which ended at Phase 19).

**Rules for every phase:**

- **Source compatibility only (P1).** New params and fields have defaults. No new
  `@Deprecated(HIDDEN)` overloads, hand-written old-arity `copy`, hidden ctors or `copy$default`
  shims. Composable params go after `modifier`, or before a trailing lambda, never after one.
  `NeedsConfirmation` fields go before `onConfirm`/`onCancel`.
- **New behavior uses new enums.** No existing enum gains constants.
- **Strings and tokens come from the caller.** No consumer nouns, colors or types in the hub.
- **Parity is structural plus Robolectric (P3).** No Roborazzi. Receipt pixels are proven by the
  owner's TESTER screencap review in Phase 24.
- **Register in the same phase.** A phase that adds a public composable registers it in its
  `ComponentRegistry` family screen in that phase (the drift guard fails otherwise). A phase that
  changes a public signature regenerates `api.txt` in the same commit and keeps `./gradlew apiCheck`
  green.

- [x] **Phase 20: Tone foundation & atoms** - Tone tokens, button emphasis + `RoundIconButton`, `StackedProgressBar`, `ToneCard`, `PillChip` (Y-1, Y-8, Y-9, Y-10, Y-12) (completed 2026-10-12)
- [x] **Phase 21: Receipt row, Pill selector & sheet shape** - `ReceiptRow`, `SegmentedOptionSelector` Pill style + per-option disable, `SheetScaffold` shape (Y-2, Y-11, Y-13) (completed 2026-10-12)
- [x] **Phase 22: OutcomeSheet Receipt style** - Receipt frame + NeedsConfirmation/Success/Failure/handled-by additions, stable test tags, in-flight action, XR-178-01 a/b (Y-3..Y-7, Y-14) (completed 2026-10-12)
- [x] **Phase 23: ProviderKeyCard lifecycle & Undo Center groups** - Per-provider saved-key summary with Save/Update/Clear/Cancel, plus the Undo Center group row (sketch §1, XR-178-01 c) (completed 2026-10-12)
- [x] **Phase 24: Catalog, docs, Explorer demos & cut v2.6.0** - Registry/API/docs/invariant gate, Explorer demos + TESTER screencaps, then the `v2.6.0` cut (owner screencap review after the cut, Y-15) (completed 2026-10-12)

## Phase Details

### Phase 20: Tone foundation & atoms

**Goal**: A caller can color surfaces with tone tokens, drop in four new atoms (round icon button, stacked progress bar, tone card, pill chip), and pick a button's emphasis separately from its role, while every existing `DynamicActionButton` call looks exactly as it does in v2.5.1.
**Depends on**: Phase 19 (milestone v2.5; library `v2.5.1` baseline, `b6c363c`)
**Requirements**: VTONE-01, VBTN-01, VBTN-02, VBAR-01, VCARD-01, VCHIP-01
**Success Criteria** (what must be TRUE):

  1. `ToneDefaults.error()` and `ToneDefaults.warn()` resolve `errorContainer`/`error` and `tertiaryContainer`/`onTertiaryContainer` from the active `ColorScheme`, and `ToneColors` compares by value. A `ToneCard` on either default tone has 12dp corners, 12dp × 16dp padding, 4dp child spacing and content-on-container contrast ≥ 4.5:1 (Robolectric color + bounds assertions under the hub's light and dark schemes) (VTONE-01, VCARD-01).
  2. `DynamicActionButton` renders each `ActionButtonEmphasis` as its Y-8 widget in the role's hue and shows `icon` when given, and `TextSmall` keeps a ≥ 48dp touch target. With `emphasis = null` and `icon = null`, the defaulted call renders exactly as v2.5.1 (same code path; Robolectric state assertions), and the existing `DynamicActionButton` tests pass unedited (VBTN-01).
  3. `RoundIconButton` (40dp circle, 1dp `outlineVariant` border, 20dp `onSurface` icon) and `PillChip` (32dp tall, single-line `bodySmall` label, 18dp icons) each expose a ≥ 48dp touch target and their content description/label, and a click with `enabled = false` never fires the callback (Robolectric semantics + bounds tests). The `AppChip` and `PresetChip` tests pass unchanged (VBTN-02, VCHIP-01).
  4. `StackedProgressBar` draws segments left to right inside a fully rounded track. A segment list summing to more than 1 is clamped to ≤ 1, and a negative fraction draws as 0 (a unit test on the clamp + Robolectric per-segment width assertions) (VBAR-01).
  5. `RoundIconButton`, `StackedProgressBar`, `ToneCard` and `PillChip` are registered in their family screens (suggested: Buttons/FAB, Progress, Cards, Chips). The registry integrity test and the CATALOG drift guard are green, and `apiCheck` is green on the `api.txt` regenerated in the same commit.

**Notes**: The atoms may carry their Y-14 tags (`round_icon_button`, `stacked_progress_bar`, `tone_card`, `pill_chip`) from the start. VTAG-01 acceptance (the per-Receipt-state tag test) belongs to Phase 22.
**Plans**: 4 plans

Plans:
**Wave 1**

- [x] 20-01-PLAN.md — Tracer: tone tokens + `ToneCard` end to end (registry, allowlist, api.txt), phase Dimens token groups, structural proof and pixel-capture probe (VTONE-01, VCARD-01)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 20-02-PLAN.md — `DynamicActionButton` emphasis/icon axis above the untouched legacy path, legacy-path contract test, long German label at 360dp (VBTN-01)

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 20-03-PLAN.md — `RoundIconButton` and `PillChip` slices with registry entries, allowlist and tests (VBTN-02, VCHIP-01)

**Wave 4** *(blocked on Wave 3 completion)*

- [x] 20-04-PLAN.md — `StackedProgressBar` slice, phase invariants test and the phase gate (VBAR-01)

**UI hint**: yes

### Phase 21: Receipt row, Pill selector & sheet shape

**Goal**: A caller can build receipt-style lists with a stateless, accessible `ReceiptRow`, offer a Pill-style segmented choice with individually disabled options, and give any `SheetScaffold` a custom shape, while default selector and sheet calls look exactly as they do in v2.5.1.
**Depends on**: Phase 20 (`ContentTone`, `ToneColors`, `ToneDefaults.warn()`)
**Requirements**: VROW-01, VROW-02, VSEG-01, VSHEET-01
**Success Criteria** (what must be TRUE):

  1. At 360dp with a long German title, `ReceiptRow` shows the value in full and ellipsizes the title. The chevron renders only when `onClick != null && trailingAction == null` and rotates 0→180° over 200ms. `dimmed` applies 38% alpha plus strikethrough. `expandedContent` renders full width below the summary. With all lines and no cue, the collapsed row measures 92dp ±1dp (Robolectric bounds + semantics tests) (VROW-01).
  2. The `ReceiptRow` summary is one merged semantics node holding title, subtitle, value, caption and cue. With `onClick` it is a `Role.Button` carrying the caller's `onClickLabel`, and its `stateDescription` switches between the caller's expanded and collapsed labels. `trailingAction` and `expandedContent` stay separate nodes (semantics-tree test) (VROW-02).
  3. In `SegmentedOptionStyle.Pill`, the selected segment paints `primary`/`onPrimary` with no check icon, and labels are single-line at 40dp tall (a German 360dp test pins the documented truncation). An option in `disabledOptions` renders at 38% alpha and never fires `onSelect`, while its siblings still do (Robolectric tests) (VSEG-01).
  4. `SheetScaffold` applies a caller-supplied `shape`. For `SegmentedOptionSelector` (Material, empty set) and `SheetScaffold` (M3 `ExpandedShape`), the defaulted call renders exactly as v2.5.1 (same code path; Robolectric state assertions), and the G-15-1 long-label tests pass unedited (VSEG-01, VSHEET-01).
  5. `ReceiptRow` is registered in its family screen with a gallery state whose single-open accordion is held in demo state, not in the row. The registry integrity test and the CATALOG drift guard are green, and `apiCheck` is green on the regenerated `api.txt`.

**Notes**: The `receipt_row_*` tags may land with the row. VTAG-01 acceptance belongs to Phase 22.
**Plans**: 4 plans

Plans:
**Wave 1**

- [x] 21-01-PLAN.md — Tracer: `SheetScaffold(shape)` end to end (signature, forward, api.txt, source contract, recording-shape probe, shaped-sheet gallery demo) (VSHEET-01)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 21-02-PLAN.md — `SegmentedOptionSelector` Pill style + `disabledOptions` above the byte-identical Material block, parity contract, Pill/German 360dp/disabled tests, gallery demos (VSEG-01)

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 21-03-PLAN.md — `ReceiptRow` end to end (Y-2 signature, 92dp anatomy, merge-domain semantics, motion seams, Cards entry + vocabulary acknowledgment), line-height probe, geometry/semantics/source-contract tests (VROW-01, VROW-02)

**Wave 4** *(blocked on Wave 3 completion)*

- [x] 21-04-PLAN.md — Motion proof (200ms chevron/dim/expand), 360dp German overflow + extreme-value edge, single-open accordion demo + registry render, phase gate and Gate-1 handoff (VROW-01)

**UI hint**: yes

### Phase 22: OutcomeSheet Receipt style

**Goal**: A caller can opt `OutcomeSheet` into the Receipt style and get the Y-3 frame for confirm, success and failure. That includes the richer model fields, stable test tags, an action that stays usable while in flight, a refused-undo surface that TalkBack announces, and undo on failures. The 3-arg Classic call still renders exactly as it does in v2.5.1.
**Depends on**: Phase 20 (tones, `ToneCard`, `ActionButtonEmphasis`), Phase 21 (`SheetScaffold(shape)`, `ReceiptRow`)
**Requirements**: VRCPT-01, VRCPT-02, VRCPT-03, VRCPT-04, VRCPT-05, VTAG-01, VXR-01, VXR-02
**Success Criteria** (what must be TRUE):

  1. For the 3-arg `OutcomeSheet(outcome, onDismissRequest, modifier)` with each `VoiceOutcomeUiState` arm built from v2.5.1 args only, the defaulted call renders exactly as v2.5.1 (same code path; Robolectric state assertions). Classic ignores `badge`, `titleActions` and the start/end actions, `HandledByRow` is unchanged, and the existing `OutcomeSheet` tests pass unedited. Classic with `FailureTone.Warn` paints `colors.warnTone` (VRCPT-01, VRCPT-03, VRCPT-04, VRCPT-05).
  2. In Receipt, `NeedsConfirmation` and `Failure` pin the action bar under a 1dp divider. In a 384dp-wide sheet, the start action's start edge is at 16dp and the end action's end edge is at 368dp. The bar stays on screen with an over-long body (the v2.4.1 `weight(1f, fill = false)` body). With `confirmEnabled = false`, the confirm node is disabled and `onConfirm` never fires. The Failure card is a `ToneCard` holding `reason` + `body`, with the tip below it, `action` at the end (Filled, in its role hue) and `secondaryAction` at the start (Robolectric bounds + click tests) (VRCPT-01, VRCPT-02, VRCPT-04).
  3. Receipt `Success` renders in this order: title row → handled-by pill → `editableContent` → `batchResults`/`undo` → actions row (node-order assertion). With `inFlight = true`, every action is disabled except one marked `activeWhileInFlight`, which still fires (SB's in-flight Cancel). `OutcomeBadge.Reverted` uses `Icons.AutoMirrored.Filled.Undo`. The handled-by pill reads "Handled by Local grammar · offline", with the `warnTone.content` dot when `onDevice = true` (VRCPT-03, VRCPT-05).
  4. A tag test asserts each applicable Y-14 tag in each Receipt state (confirm single/batch, success single/batch/all-undone, failure error/warn). The Receipt confirm bar keeps `outcome_sheet_confirmation_cancel`/`_confirm`, the Classic tags are unchanged, and a source grep finds no `testTagsAsResourceId` in `src/main` (VTAG-01).
  5. The refused/partial undo surface reports `liveRegion = LiveRegionMode.Assertive` in the semantics tree, with no visual change. A `Failure` carrying `undo` renders the same undo block as `Success` (undo-all + per-item rows) in both Classic and Receipt (semantics + render tests) (VXR-01, VXR-02).

**Notes**: This phase widens six signatures (`OutcomeSheet`, `NeedsConfirmation`, `Success`, `Failure`, `FailureActionUiModel`, `HandledByUiModel`). Each commit keeps `apiCheck` green on the regenerated `api.txt`. The Receipt action-bar and pill labels get a long German ~360dp check before layout sign-off (the long-localized-label lesson). If a new public top-level composable appears, it is registered or allowlisted in this phase.
**Plans**: 5 plans

Plans:
**Wave 1**

- [x] 22-01-PLAN.md — API types and model widening, leaf types first: `OutcomeBadge`, `FailureTone`, `SheetActionUiModel`, `HandledByUiModel` / `FailureActionUiModel` / `Success` / `Failure` (redacting `toString`) / `NeedsConfirmation` fields, `OutcomeSheetStyle` / `OutcomeSheetColors` / `OutcomeSheetDefaults`, `Dimens.OutcomeReceipt`, three arity pins, v2.5.1 call-shape canary, `api.txt` per commit (VRCPT-01..05 fields, VXR-02 field)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 22-02-PLAN.md — Tracer: `OutcomeSheet(style, colors)` dispatch + Receipt frame + NeedsConfirmation end to end (24dp shape, title row, transcript, pinned Text/Filled bar, `confirmEnabled`), 384dp edges, tall body, long German 360dp, source contracts (VRCPT-01, VRCPT-02, VTAG-01)

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 22-03-PLAN.md — The four named Classic deltas (confirm enabled, Warn tone seam, `Failure.undo`, Assertive live region) + Classic parity inventories and v2.5.1-derived source contract; `api.txt` byte-identical (VRCPT-01..05 Classic, VXR-01, VXR-02)

**Wave 4** *(blocked on Wave 3 completion)*

- [x] 22-04-PLAN.md — Receipt Success: handled-by pill, `editableContent`, shared undo/batch blocks, actions row with the in-flight rule, badge seams, long German pill (VRCPT-03, VRCPT-05, VTAG-01, VXR-01)

**Wave 5** *(blocked on Wave 4 completion)*

- [x] 22-05-PLAN.md — Receipt Failure (ToneCard, tip, undo, pinned bar), full Y-14 tag matrix across every Receipt state, phase gate and Gate-1 handoff (VRCPT-04, VTAG-01, VXR-01, VXR-02)

**UI hint**: yes

### Phase 23: ProviderKeyCard lifecycle & Undo Center groups

**Goal**: A caller can let `ProviderKeyCard` show each provider's saved key as a fingerprint-only summary with Save/Update/Clear/Cancel actions, and can show grouped undo entries in `UndoCenterScreen` with one "Undo all", while default calls of both look exactly as they do in v2.5.1.
**Depends on**: Phase 20 (`DynamicActionButton` emphasis for the action row only)
**Requirements**: VKEY-01, VKEY-02, VUNDO-02
**Success Criteria** (what must be TRUE):

  1. For `ProviderKeyCard` with an empty `savedKeys` and `actions = null`, and for `UndoCenterScreen` without the group params (today's flat list), the defaulted call renders exactly as v2.5.1 (same code path; Robolectric state assertions), and the existing tests for both pass unedited (VKEY-01, VUNDO-02).
  2. With a saved key for the selected provider and `editingSavedKey = false`, the field is replaced by `provider_key_saved_summary`, which reads "Saved · sk-ant-…abcd" and offers Update | Clear. Otherwise the field shows with Save, plus Cancel when `onCancelEdit != null`. `inFlight = true` disables every action, and each action carries its `provider_key_action_{save,update,clear,cancel}` tag (Robolectric semantics + click tests) (VKEY-01, VKEY-02).
  3. Switching `selectedProviderId` across 3 providers shows each provider's own summary or field, and providers in `savedKeys` show `labels.savedMarker` in the dropdown. With a raw `keyValue` set while the summary shows, a semantics-tree scan finds that raw key nowhere (VKEY-02).
  4. `UndoCenterScreen` renders one group row for entries that share an `UndoHistoryStore` group, showing the group label, member count and status, and its "Undo all" invokes the caller's callback for that group. The new params sit before the trailing `onClear`, so existing trailing-lambda call sites still compile (Robolectric test + compile fixture) (VUNDO-02).

**Notes**: Logically independent of Phases 21-22, but it runs after 22 in numeric order because every phase rewrites the shared `api.txt`. No new public composable is expected. If one appears, it is registered or allowlisted in this phase. `apiCheck` is green on the regenerated `api.txt`. The card stays presentational: no persistence, validation or network (Out of Scope, INV-01).
**Plans**: TBD
**UI hint**: yes

### Phase 24: Catalog, docs, Explorer demos & cut v2.6.0

**Goal**: Prove the whole milestone is registry-clean, source-compatible, documented and invariant-clean; demo every new surface in the Explorer with TESTER screencaps for the owner; then cut the immutable `v2.6.0` tag (the owner reviews the screencaps after the cut).
**Depends on**: Phase 20, Phase 21, Phase 22, Phase 23
**Requirements**: CAT-03, API-03, DOC-03, INV-03, DEMO-01, SHIP-04
**Success Criteria** (what must be TRUE):

  1. The full suite is green with `ReceiptRow`, `ToneCard`, `StackedProgressBar`, `PillChip` and `RoundIconButton` registered, including the registry integrity test and the CATALOG drift guard. The one-way-dependency invariant holds: no engine or consumer import, and no consumer nouns or colors (domain-vocabulary guard). Detekt is green at zero baseline, and there is no `@HiltAndroidApp` (CAT-03, INV-03).
  2. On a freshly regenerated `api.txt`, `./gradlew apiCheck` and `bash tools/test/run-all.sh` pass, and a v2.5.1 call-site canary test source set (SB + CT call shapes and the Explorer) compiles unchanged. The diff shows no public symbol removed, no required param added and every existing enum constant intact. `tools/verify-binary-abi.sh v2.5.1` runs and its result is recorded as information only (API-03).
  3. `API.md` (including a stable-test-tags section listing every Y-14 and `provider_key_*` tag), `INTEGRATION.md` and the KDoc cover Y-1..Y-14, the ProviderKeyCard lifecycle and XR-178-01 a/b/c, checked item by item against the sketch (DOC-03).
  4. The Explorer shows every DEMO-01 demo, each reached by a registry render test: Receipt confirm (single open, batch collapsed, batch with an attention row); a count editor with a disabled Pill segment and a warn `ToneCard`; Success single/batch/all-undone; Failure error/warn; the ProviderKeyCard lifecycle (3 providers, one saved, edit and clear flows); and an Undo Center group row. Screencaps are captured on the TESTER (`R5CT10XNKQN`) only and kept for the owner (DEMO-01).
  5. The annotated `v2.6.0` tag is cut via §11 on the gated commit. Only after gates 1-2 and the full unit suite pass is it pushed, with targeted `git push origin main` + `git push origin v2.6.0` (never `--all`) before the push OK expires (midnight 2026-10-12→13). Then the JitPack build is confirmed, and tag sha, commit, coordinate, JitPack status and gate results are reported to the teamlead. The owner's screencap review happens AFTER the cut (an objection ships as `v2.6.1`). There is no stray marker tag (`git.create_tag` false), and no consumer is repinned from the hub (SHIP-04).

**Notes**: The tag cut is the last step. Push authority: the director relayed Yahir's explicit OK for 2026-10-12 via the teamlead (gates first, targeted push only, expires at midnight; if the cut slips past it, stop before pushing and ask the teamlead). The owner's screencap review moved to after the cut (teamlead, 2026-10-12). Before pushing the tag, check `git log origin/main..v2.6.0`: a tag push publishes every unpushed commit reachable from it.
**Plans**: TBD

## Completed Milestones

<details>
<summary>✅ v1.0 — Hub Stewardship (Phases 1-5) — SHIPPED 2026-09-02, library <code>v2.0.0</code></summary>

The hub went from a flat, additive-only catalog to a legible, audited, governed two-tier design
system — Tier Legibility → Coherence Audit → Governance Gates → Repin Bookkeeping → Gardening (unify
shipped; the coordinated consumer repin, GARD-02, was deferred human-gated and was **absorbed into
v2.0's Phase 9** onto `v2.2.0`). Full detail:
[`.planning/milestones/v1.0-ROADMAP.md`](milestones/v1.0-ROADMAP.md).

</details>

<details>
<summary>✅ v2.0 — Line Reunification (Phases 6-9) — SHIPPED 2026-09-27, library <code>v2.2.0</code></summary>

Collapsed the divergent v1.x (SecondBrain `v1.13.0`) and v2.x/`main` (CalTracker `v2.1.0`) release
lines into one forward line on `main`, cut as library `v2.2.0`, so every consumer converges on one
tag and the v1.x line can retire — while keeping every v2.x improvement. Also landed two additive
riders (per-tag chip color, MicButton hardening), completed v1.0's deferred **GARD-02** coordinated
repin (onto `v2.2.0`), and cleared tech-debt **W-1**.

- [x] Phase 6: Forward-port reunification (3/3 plans) — DateTimePicker, PlaceMap* cluster, PresetChip forward-ported net-additively; osmdroid admitted; both drift guards green — completed 2026-09-27
- [x] Phase 7: Chip-color slot (1/1 plan) — opt-in backward-compatible per-tag chip container-color override — completed 2026-09-27
- [x] Phase 8: MicButton hardening (1/1 plan) — consumer-agnostic microcopy, latest-callback safety, hub-vocabulary KDoc — completed 2026-09-27
- [x] Phase 9: Ship & coordinated repin (2/2 plans) — cut library `v2.2.0` (human-gated), reconciled ECOSYSTEM.md matrix, surfaced consumer repins, closed KI-2026-09-02-01 — completed 2026-09-27

**Gate-2:** Phases 6, 7, 8 human-signed-off (Yahir, 2026-09-27); Phase 9 had no deferred device
checkpoint (doc/config ship phase). Audit: 10/10 requirements satisfied, cross-phase integration
CLEAN. Full detail: [`.planning/milestones/v2.0-ROADMAP.md`](milestones/v2.0-ROADMAP.md).

**Deferred (human-gated, cross-repo convention):** consumer repin execution — SecondBrain
(`v1.13.0→v2.2.0` + FilterBar→ChipBar migration) and CalTracker (`v2.1.0→v2.2.0`) — runs in each
consumer's own channel; repin paths surfaced in `ECOSYSTEM.md`.

</details>

<details>
<summary>✅ v2.4 — AI-Voice Command UI (Phases 10-14) — SHIPPED 2026-10-01, library <code>v2.4.0</code></summary>

Shipped the shared AI-voice UI layer in the hub — generic, prop-driven presentational composables
(`ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`, `OutcomeSheet` with a "handled by:
tier/approach" indicator + loud failure states + a generic undo affordance + a domain-neutral
needs-confirmation state, and `ClarificationBar` tap-to-clarify choices) as the tenth "Voice Command"
`ComponentRegistry` family, all strictly additive versus `v2.3.0` and engine-free (no OkHttp, no
`voice-action-engine` dependency — one-way-dependency invariant, INV-01). Cut as library `v2.4.0`.
Wave 0, §6.3 of the frozen `vae-bilingual` cross-repo contract.

- [x] Phase 10: Voice command settings surfaces (2/2 plans) — ProviderKeyCard, ModelSelectCard, ApproachLadderCard — completed 2026-09-30
- [x] Phase 11: Voice outcome & failure sheet (2/2 plans) — OutcomeSheet (handled-by + loud failure + generic undo) + ClarificationBar — completed 2026-09-30
- [x] Phase 12: Generic needs-confirmation state (1/1 plan) — additive NeedsConfirmation sealed arm (single + batch) — completed 2026-09-30
- [x] Phase 13: Catalog integrity & docs (1/1 plan, cuts NO tag) — registry registration + CATALOG-03 drift guard + additive API + doc-drift fix — completed 2026-09-30
- [x] Phase 14: Cut v2.4.0 (1/1 plan) — cut + push + JitPack-confirm `v2.4.0`, §11 ledger row relayed — completed 2026-10-01

**Gate-2:** Phases 10-13 human-signed-off (Yahir, 2026-10-01, PASS via orchestrator); Phase 14 had no
deferred device checkpoint (release-tooling phase). Audit: 16/16 requirements satisfied, cross-phase
integration CLEAN, Nyquist compliant. Closed via override_closeout (phase verifications read
mtime-`stale` after the downstream tag/doc commits — the known mtime artifact; real state verified by
certify `all_pass` + the full suite green at the tagged commit). Full detail:
[`.planning/milestones/v2.4-ROADMAP.md`](milestones/v2.4-ROADMAP.md).

**Deferred (future polish, Gate-2-waived):** two non-blocking `ApproachLadderCard` UI-polish notes
(combined-subdued-label right-edge crowding; light-theme capped-rung dimming) — see
`KNOWN-ISSUES.md` KI-2026-10-01-01. Consumer repins (SecondBrain `v2.3.0`→, CalTracker `v2.1.0`→
`v2.4.0`) are Wave-1, in each consumer's own channel.

</details>

<details>
<summary>✅ v2.5 — Voice UI Localization & Accessibility (Phases 15-19) — SHIPPED 2026-10-11, library <code>v2.5.0</code></summary>

Made the AI-voice UI surface fully caller-localizable and more accessible, strictly additively.
This is Wave 0, §6.3 of the `vae-bilingual` effort, with scope locked to R-v1.1 `cb5f047`.

- [x] Phase 15: Voice-surface i18n label params (4/4 plans) — caller-overridable English-default labels on the settings cards and voice-surface composables (VI18N-01..04). Gate-2 reopened it with G-15-1 (long German labels crammed the rung rows); 15-04 plus the code-review fixes closed it with the private `RungLabelLayout` — completed 2026-10-11
- [x] Phase 16: A11y + Failure enrichment (3/3 plans) — minimum interactive size and selected-radio semantics on rung rows; `Failure` gets an action role, a body slot and a semantics prefix (VA11Y-01, VFAIL-01..03) — completed 2026-10-05
- [x] Phase 17: ApproachLadderCard Router ON/OFF toggle (1/1 plan) — `require()`-paired `router`/`onRouterChange` (VAPPR-04) — completed 2026-10-05
- [x] Phase 18: Catalog integrity + API dump + docs (2/2 plans) — registry and drift guard green, additive API, docs, invariants (CAT-02, API-02, DOC-02, INV-02) — completed 2026-10-05
- [x] Phase 19: Cut v2.5.0 (2/2 plans) — immutable `v2.5.0` tag via §11, ledger relayed, no stray marker tag (SHIP-03) — completed 2026-10-05

**Gate-2:** all five phases are human-signed-off; Phase 15 was re-verified 17/17 on the personal
device on 2026-10-11. Audit: 14/14 requirements, integration WIRED, status `tech_debt`. Full detail:
[`.planning/milestones/v2.5-ROADMAP.md`](milestones/v2.5-ROADMAP.md).

**Follow-up (human-gated):** the G-15-1 fix landed after the immutable `v2.5.0` tag. It ships as
`v2.5.1` (layout-only, `api.txt` unchanged), plus SecondBrain/CalTracker repins, routed through the
orchestrator.

</details>

## Backlog

### Phase 999.1: Formalize reusable Gate-2 visualization harness APK (BACKLOG)

**Goal:** Promote the throwaway same-package-Intent harness — which every Gate-1 agent currently re-derives from scratch — into a committed, launchable Gate-2 visualization app for this library-only repo.

**Requirements:** TBD

**Plans:** 0 plans

Context:

- `yahirandroidtaste` is a pure `com.android.library` (no `applicationId`), so it ships **no installable APK**. Human Gate-2 on-device review therefore has nothing to open. Every Gate-1 self-UAT run rebuilds the same throwaway harness (a 1-Activity app that depends on the mavenLocal AAR and `startActivity(Intent(this, ExplorerActivity::class.java))`) just to see the gallery — documented in `01-05-SELF-UAT.md`'s "Driver-mechanism note" and re-derived by later verify sessions too (incl. v2.4's Gate-2 prep).
- Deliverables to scope when promoted: (a) a committed harness — a dedicated app module or a gradle task that assembles an installable debug APK opening `ExplorerActivity`; (b) a project-local `AGENT-DEVICE-TESTING.md` documenting the same-package-harness driver pattern (the SELF-UAT logs explicitly recommend authoring one so future Gate-1 runs don't re-derive it).
- **Invariant guard:** harness → library only, never the reverse (one-way dependency). The harness is host/consumer-side tooling; it must name no library-internal concepts and must not become something the library depends on.

Plans:

- [ ] TBD (promote with /gsd-review-backlog when ready)

## Progress

| Milestone | Phases | Plans | Status | Shipped |
|-----------|--------|-------|--------|---------|
| v1.0 | 1-5 | — | Complete | 2026-09-02 |
| v2.0 | 6-9 | 7/7 | Complete | 2026-09-27 |
| v2.4 | 10-14 | — | Complete | 2026-10-01 |
| v2.5 | 15-19 | 12/12 | Complete | 2026-10-11 |
| v2.6 | 20-24 | 0/TBD | In progress | - |

**v2.6 execution order:** Phases execute in numeric order: 20 → 21 → 22 → 23 → 24. (Phase 23 depends
only on Phase 20, but it runs after 22 because every phase rewrites the shared `api.txt`.)

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 20. Tone foundation & atoms | v2.6 | 4/4 | Planned |  |
| 21. Receipt row, Pill selector & sheet shape | v2.6 | 4/4 | Not started |  |
| 22. OutcomeSheet Receipt style | v2.6 | 5/5 | Not started |  |
| 23. ProviderKeyCard lifecycle & Undo Center groups | v2.6 | 2/2 | Not started |  |
| 24. Catalog, docs, Explorer demos & cut v2.6.0 | v2.6 | 4/4 | Not started |  |
