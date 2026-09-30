# Human UAT Pending

## Entries

### Phase 1 — tier-legibility (v1.0)

- **Status:** `signed-off — Yahir, 2026-09-02`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v1.0 (Hub Stewardship — Tier Legibility → Coherence Audit → Governance → Repin
  Bookkeeping → Gardening)

- **Gate 1 self-UAT log:** [`.planning/phases/01-tier-legibility/01-05-SELF-UAT.md`](phases/01-tier-legibility/01-05-SELF-UAT.md) — Verdict: **ALL 1 outstanding criteria PASS** (device Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN), library AAR md5 `18e493e666f8bcaeed23b2c22953fadb` @ `87561ff`, 2026-09-02). All 4 ROADMAP success criteria are now backed by live evidence: 3 (LEG-01 code/wiring + LEG-02 doc) were already `VERIFIED` by `01-VERIFICATION.md`'s static/compile/test evidence; the 1 remaining `human_verification` item (on-device visual confirmation of the Primitive/Pattern `TierBadge` on both gallery surfaces) is closed by this Gate-1 self-UAT run on real hardware.
- **Items covered (1 outstanding ROADMAP success criterion / human_verification item):**
  - **SC2 — `ExplorerActivity` gallery displays each component's tier on its detail/list view (the on-device visual sub-claim).** Confirmed on real hardware, both `ComponentRow` (family lists + index search results) and `ComponentDetailScreen`'s `TopAppBar`, both light and dark theme, for the longest registered names (`RecordingBottomSheetContent` 28 ch, `SegmentedOptionSelector` 23 ch) and a short name (`AppChip`). The badge never clips/pushes off-row on the list surface; on the detail `TopAppBar` the **component name truncates with an ellipsis, never the badge**, when the two don't fit; Primitive (`secondaryContainer`) vs. Pattern (`tertiaryContainer`) badge colors are visually distinguishable in both themes. Note: the `uiautomator` accessibility-tree dump reported the full un-truncated name string even where the rendered pixels showed a real ellipsis-truncation — only the screenshot (rung 5) caught this; a structure-tree-only check would have been a false pass on this exact criterion.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `01-05-SELF-UAT.md` above for the full per-surface/per-theme evidence trail (screenshot descriptions + `uiautomator` bounds cross-checks).
  2. Repro on-device: since `yahirandroidtaste` is a pure library with no installable APK of its own, either (a) sideload SecondBrain/CalTracker once each has repinned to a tag containing this phase's tier wiring and open their in-app gallery entry point, or (b) build the same throwaway same-package-Intent harness described in the SELF-UAT log's "Driver-mechanism note" (`publishReleasePublicationToMavenLocal` + a 5-line `MainActivity` that Intents into `ExplorerActivity`). Browse to the Sheets family (has the 28-char longest name) and toggle dark theme via the detail screen's theme-toggle action.
- **Note:** No schema/DB involved (phase is UI-only). This phase's Gate-1 run also surfaced a
  reusable gap: no project-local `AGENT-DEVICE-TESTING.md` exists yet for this library-only repo
  (global template assumes an installable app). Recommend authoring one in a later phase so future
  Gate-1 runs don't re-derive the harness pattern from scratch — not a blocker for this sign-off.

### Phase 2 — coherence-audit (v1.0)

- **Status:** `signed-off — Yahir, 2026-09-02`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v1.0 (Hub Stewardship — Tier Legibility → Coherence Audit → Governance → Repin
  Bookkeeping → Gardening)

- **Gate 1 self-UAT log:** [`.planning/phases/02-coherence-audit/02-02-SELF-UAT.md`](phases/02-coherence-audit/02-02-SELF-UAT.md) — Verdict: **ALL 4 criteria PASS** (documentation-content verification; no device/app surface in scope — this phase shipped zero `.kt` changes, confirmed via `git diff` across the full phase commit range, only `docs/COHERENCE-AUDIT.md`, 2026-09-02).
- **Items covered (4 ROADMAP success criteria, AUD-01):**
  - **SC1 — All 9 registered families enumerated.** Confirmed: `docs/COHERENCE-AUDIT.md` has one section per family (Cards, Chips, Sheets, Buttons/FAB, Pickers, Feedback, Empty State, Progress/Metrics, Tactile Foundation), matching `ComponentRegistry.kt`.
  - **SC2 — Overlaps/near-duplicates/altitude mismatches flagged.** Confirmed: 9 named findings (C-1, C-2, CH-1, CH-2, CH-3, S-1, S-2, PK-1, T-1) plus explicit no-finding statements for families with none.
  - **SC3 — Every flagged finding carries a documented disposition (unify/keep-with-rationale/prune).** Confirmed: 13 disposition statements, each with cited rationale; no finding left undispositioned.
  - **SC4 — "Unify" dispositions form a concrete, actionable list.** Confirmed: self-contained "Unify Work-Order" section with WO-1 (`FilterBar`→`ChipBar`) and WO-2 (shared sheet-chrome extraction), each with components/rationale/blast-radius, ready for Phase 5 to execute against.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `.planning/phases/02-coherence-audit/02-02-SELF-UAT.md` above for the full evidence trail (grep commands + line ranges cited per criterion).
  2. Read `docs/COHERENCE-AUDIT.md` directly and independently judge the *editorial* calls behind each disposition — is `ChipBar`/`FilterBar` genuinely worth unifying, is `CardQuickView` genuinely a distinct purpose from the `CardBase` group, etc. This is the one part of this phase that is inherently a human architectural-taste call, not a mechanical check; Gate-1 confirmed the audit's structural completeness and internal consistency, not the correctness of its architectural opinions.
- **Note:** No device/app behavior shipped this phase — pure documentation deliverable
  (`docs/COHERENCE-AUDIT.md`), zero `.kt` files touched. A separate static/code-level verification
  (`.planning/phases/02-coherence-audit/02-VERIFICATION.md`, `gsd-verifier`) independently
  re-derived all 53 registry entries' name+tier from source and cross-checked all 4 blast-radius
  grep counts against both consumer repos — this Gate-1 pass corroborated that with its own
  independent spot-check rather than trusting it outright. `.planning/REQUIREMENTS.md`'s AUD-01
  checkbox/traceability bookkeeping was still unchecked as of this pass (info-level, non-blocking,
  flagged for whoever closes this phase).

### Phase 3 — governance-gates (v1.0)

- **Status:** `signed-off — Yahir, 2026-09-02`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v1.0 (Hub Stewardship — Tier Legibility → Coherence Audit → Governance → Repin
  Bookkeeping → Gardening)

- **Gate 1 self-UAT log:** [`.planning/phases/03-governance-gates/03-02-SELF-UAT.md`](phases/03-governance-gates/03-02-SELF-UAT.md) — Verdict: **ALL 4 criteria PASS** (tooling-behavior verification against the real, production-wired pre-commit hook and the real JVM test runner; no mobile-device/UI surface in scope — this phase shipped zero `src/main`/Activity changes, confirmed via `git log --stat` across both phase plans, only `tools/`, a `src/test/` JUnit file, and `docs/DESIGN-INTENT.md`, 2026-09-01).
- **Items covered (4 ROADMAP success criteria, GOV-01/GOV-02/GOV-03):**
  - **SC1 — Tier-aware contribution litmus documented.** Confirmed: `docs/DESIGN-INTENT.md`'s new `## The Tier-Aware Contribution Litmus` section states the asymmetric strict-primitives/loose-patterns gate, cross-referencing D-04.
  - **SC2 — Litmus enforced where feasible (not just prose).** Confirmed: `## Enforcement` accurately names `DomainVocabularyDriftGuardTest` as the mechanically-enforced strict half and explicitly scopes the patterns-loose half as prose-only (no CI/PR-template surface exists) — verified against the guard's actual live behavior, not just the doc's claim.
  - **SC3 — Domain-vocabulary drift guard flags (not forbids) new domain nouns.** Live-falsified: added a real throwaway `ProjectMilestoneWidget` composable to `src/main`, confirmed `DomainVocabularyDriftGuardTest` genuinely fails the build naming exactly that offender with remediation instructions, then removed it and confirmed green again.
  - **SC4 — Pre-commit hook no longer false-flags `.planning/`/docs paths as lane-2.** Live-falsified against the REAL production-symlinked hook (not just the bash fixture sandbox): staged a real `.planning/config.json` change, ran the hook with no `HUB_LANE_OVERRIDE`, got lane-1/exit-0. Counter-probe: staged a genuine line-rewrite in a real `src/main` file, confirmed the hook still correctly blocks it as lane-2 without an override — proving the fix didn't neuter legitimate detection.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `.planning/phases/03-governance-gates/03-02-SELF-UAT.md` above for the full live-probe evidence trail (exact hook invocations, diffs, exit codes, and the JUnit red/green transcript).
  2. Optionally re-run `bash tools/test/run-all.sh` and `./gradlew testDebugUnitTest detekt` to reconfirm all four suites are green (`PASS=4/7/5/5 FAIL=0`, `BUILD SUCCESSFUL` with zero detekt findings).
  3. Read `docs/DESIGN-INTENT.md`'s `## The Tier-Aware Contribution Litmus` / `## Enforcement` sections and judge whether the strict/loose split reads as the intended governance policy — this is the one part of this phase that carries an editorial/policy-scoping judgment, not a mechanical check.
- **Note:** No device/app UI behavior shipped this phase — pure tooling deliverable (a bash
  pre-commit-guard diff-basis fix, a JUnit drift guard, a doc update). A separate static/code-level
  verification (`.planning/phases/03-governance-gates/03-VERIFICATION.md`, `gsd-verifier`,
  re-verified 7/7 after one gap-closure cycle) already re-derived and independently re-ran all four
  shell fixture suites plus the STATE.md residual-risk correction for the sibling
  `verify-api-additive.sh` script's dormant bug (tracked for Phase 5, not fixed this phase — see
  STATE.md's `Blockers/Concerns` GOV-03 entry). This Gate-1 pass corroborated that with its own
  independent live falsification probes against the real production hook and the real JUnit runner,
  rather than trusting either prior artifact outright.

### Phase 6 — forward-port-reunification (v2.0)

- **Status:** `signed-off — Yahir, 2026-09-27`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v2.0 (Line Reunification — Forward-Port Reunification → Chip-Color Slot → MicButton
  Hardening → Ship & Coordinated Repin)
- **Gate 1 self-UAT log:** [`.planning/phases/06-forward-port-reunification/06-03-SELF-UAT.md`](phases/06-forward-port-reunification/06-03-SELF-UAT.md) — Verdict: **ALL 5 ROADMAP success criteria PASS** (device Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN), library AAR md5 `52917361d23adb55c3b43e5919df6cd9` @ `6bf7017`, 2026-09-27). Confirms on real hardware the 3 device-only `human_verification` items `06-VERIFICATION.md` deferred (`DateTimePicker`/`PlaceMapPicker`/`PresetChip` gallery rendering) plus independently re-derives (not merely trusts) the 5 code-level criteria already scored by `06-VERIFICATION.md`.
- **Items covered (5 ROADMAP success criteria):**
  - **SC1 — `DateTimePicker`, `PlaceMapPicker`, `PresetChip` each render in the gallery.** Confirmed on real hardware: `DateTimePicker`'s 3 demo variants (date-only/time-only/date-and-time) render correctly with no crash; `PlaceMapPicker`'s live osmdroid `MapView` genuinely paints real OSM tiles (continent/country labels, `© OpenStreetMap contributors` attribution) across 2 states-matrix cells, and the saved-places `PresetChip`/`ChipBar` embedding is present when `savedPlaces` is non-empty and **entirely absent** (falsifying check, not merely empty) on the "errors and resolving" variant; `PresetChip`'s both overloads (7-chip label-only, 2-chip with-supporting-labels) render correctly inside `ChipBar`. Full-session logcat: 0 FATAL/crash lines.
  - **SC2 — the 5 ported tests pass.** Re-run directly (not trusted from inherited verdict): `DateTimePickerTest` 20/20, `PlaceMapPickerTest` 82/82, `PlaceMapOsmdroidConfigTest` 10/10, `PlaceMapPickerModelTest` 16/16, `PresetChipTest` 9/9 — zero failures/errors.
  - **SC3 — each composable registered exactly once; drift guards pass.** `grep -c` confirms count=1 for all 3 registry entries; `ComponentRegistryDriftGuardTest` 1/1 pass; on-device family lists show no duplicate rows.
  - **SC4 — `osmdroid` admitted + allowlisted.** `.planning/APPROVED-DEPS.md`/`CLAUDE.md` both confirmed; `PRIMITIVE_NOUN_ALLOWLIST`/`DOMAIN_VOCABULARY` entries confirmed; `DomainVocabularyDriftGuardTest` 2/2 pass.
  - **SC5 — `detekt` zero-baseline.** Literal criterion is false today (`TextCard.kt` `CyclomaticComplexMethod` finding) but independently reconfirmed pre-existing/unrelated to Phase 6 (`git log 60381d2..HEAD` for that file is empty — zero Phase-6 touches) and formally tracked (`KI-2026-09-27-01`) + overridden in `06-VERIFICATION.md`. Marked PASS under that recorded override, matching this repo's `KI-2026-09-02-01` precedent.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `06-03-SELF-UAT.md` above for the full per-criterion evidence trail (screenshot descriptions, `uiautomator` bounds, logcat/test-XML counts, `git log` re-derivation for SC5).
  2. Repro on-device: build the same throwaway same-package-Intent harness the log's "Driver-mechanism note" describes (`publishReleasePublicationToMavenLocal` + a `MainActivity` that Intents into `ExplorerActivity`), or wait for a consumer (SecondBrain/CalTracker) to repin onto a tag containing this phase. Browse Pickers family → `DateTimePicker` and `PlaceMapPicker` rows; Chips family → `PresetChip` row.
  3. Confirm `KI-2026-09-27-01` in `.planning/KNOWN-ISSUES.md` and the `overrides:` block in `06-VERIFICATION.md` still read as intended before signing off SC5.
- **Note:** No schema/DB involved (phase is UI-only, static compiled-in registry). Same reusable gap as Phase 1: no project-local `AGENT-DEVICE-TESTING.md` exists yet for this library-only repo (ROADMAP's own Phase 999.1 backlog item tracks promoting a committed harness). New gotcha banked this run: `PlaceMapPicker`'s live `MapView` swallows small/slow scroll swipes (pans the map instead of scrolling the outer list) — use large fling-distance swipes to advance past a map-bearing item.

### Phase 7 — chip-color-slot (v2.0)

- **Status:** `signed-off — Yahir, 2026-09-27`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v2.0 (Line Reunification — Forward-Port Reunification → Chip-Color Slot → MicButton
  Hardening → Ship & Coordinated Repin)
- **Gate 1 self-UAT log:** [`.planning/phases/07-chip-color-slot/07-01-SELF-UAT.md`](phases/07-chip-color-slot/07-01-SELF-UAT.md) — Verdict: **ALL 5 ROADMAP success criteria PASS** (device Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN), library AAR md5 `633c2c599315919f12670f0c6e6b2b75` @ `418dd3b`, 2026-09-27). Confirms on real hardware that `AppChip`/`TagChipWithContextMenu`/`CardTagRow` — all touched by this phase, backing multiple gallery entries — build/install/launch/render/interact with zero regression, and independently re-derives (not merely trusts) `api.txt`'s additivity and the `isSelected`-wins-over-override precedence live in the running app.
- **Items covered (5 ROADMAP success criteria):**
  - **SC1 — `TagChipUiModel.color` / `AppChip`+`TagChipWithContextMenu`'s `containerColorOverride` all additive.** `api.txt` independently re-diffed against the TRUE pre-Phase-7 baseline (`8d692e5~1`, not the phase's own already-mutated committed copy) shows zero removed/changed lines — including `copy()`/`componentN()`, confirming the WR-01 ABI-break code review finding is genuinely closed. All 3 gallery surfaces these files back (`AppChip`, `TagChipWithContextMenu`, `CardTagRow` demos) build/install/launch/navigate with zero `FATAL`/`Exception` lines.
  - **SC2 — `CardTagRow` auto-thread test (both branches, overflow excluded).** `CardTagRowTest` 3/3 pass, re-run fresh. On-device: `CardTagRow` gallery entry renders exactly 2 visible chips + a "+2" overflow chip, matching the documented contract; no crash.
  - **SC3 — `AppChip` render test (non-null override + null regression floor).** `AppChipTest` 12/12 pass, re-run fresh. On-device screenshot confirms the default-`null` resting chip renders the pre-existing plain outlined look — the regression floor holds in the real running app, not just the unit harness.
  - **SC4 — override loses to `isSelected`/`relatednessStrength`.** Falsified live on-device (not just via the structural test): toggled the gallery Playground's "Selected" switch and captured a before/after screenshot pair showing the chip's container fill switching from the resting outlined look to the filled `secondaryContainer` look — `isSelected` genuinely wins in the real running composable.
  - **SC5 — no new public composable; `api.txt`/`apiCheck`/drift guards/`detekt` all green.** Re-run fresh: `apiCheck`, `detekt` (0 findings, no override needed — unlike the analogous Phase 6 SC5 case), both drift guards, `publishReleasePublicationToMavenLocal` all `BUILD SUCCESSFUL`.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `07-01-SELF-UAT.md` above for the full per-criterion evidence trail (screenshot descriptions, `uiautomator` bounds, before/after toggle pair, `git diff` re-derivation for SC1/SC5).
  2. Repro on-device: build the same throwaway same-package-Intent harness the log's "Driver-mechanism note" describes (`publishReleasePublicationToMavenLocal` + a `MainActivity` that Intents into `ExplorerActivity`), or wait for a consumer (SecondBrain) to repin onto a tag containing this phase. Browse Chips family → `AppChip` and `TagChipWithContextMenu` rows; Cards family → `CardTagRow` row.
  3. Note the deliberate held-out scope (`07-UI-SPEC.md`/`07-01-SUMMARY.md` D5): rendered *legibility* of a non-null `containerColorOverride` is NOT verifiable in this hub's own gallery (`ExplorerFakeData.kt`'s tag fakes are all `color = null`) — that verification belongs to SecondBrain once it wires a real color policy.
- **Note:** No schema/DB involved (phase is UI-only, static compiled-in registry/fakes). Same reusable gap as Phases 1/6: no project-local `AGENT-DEVICE-TESTING.md` exists yet for this library-only repo. Landing this code here does NOT make it live for any consumer — a tag cut + consumer coordinate bump remains a separate, human-gated step (Phase 9).

### Phase 8 — micbutton-hardening (v2.0)

- **Status:** `signed-off — Yahir, 2026-09-27`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v2.0 (Line Reunification — Forward-Port Reunification → Chip-Color Slot → MicButton
  Hardening → Ship & Coordinated Repin)
- **Gate 1 self-UAT log:** [`.planning/phases/08-micbutton-hardening/08-01-SELF-UAT.md`](phases/08-micbutton-hardening/08-01-SELF-UAT.md) — Verdict: **ALL 4 ROADMAP success criteria PASS** (device Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN), library AAR md5 `10457c8251ec04bbc6011a53c03077c1` @ `e9c7eda`, 2026-09-27). Confirms on real hardware that `MicButton`'s parameterized microcopy renders correctly, the accessibility semantics click action (CR-01) reaches the LIVE accessibility tree (not just Robolectric's), and both taps and the fresh governance battery are crash-free/green.
- **Items covered (4 ROADMAP success criteria):**
  - **SC1 — Parameterized descriptions, generic defaults, no CalTracker microcopy.** Live `uiautomator` dump of the gallery's States matrix shows `content-desc="Tap to talk"` (Default), `"Listening…"` (Pressed/Selected), `"Microphone unavailable"` (Disabled) — the literal generic defaults, on the real running app. `grep` for CalTracker-specific wording (e.g. the old `"Voice not set up"`) is zero matches at HEAD.
  - **SC2 — `onTap`/`onDisabledTap` latest-callback safety via `rememberUpdatedState`, mid-press regression test.** Re-run fresh (`--rerun-tasks`, no cache): `MicButtonGestureTest` 7/7 pass, including the mid-press callback-identity-swap test and both of the gap-closure's direct-`performSemanticsAction(OnClick)` tests. This mechanism is only provable at the unit-test rung (a real `adb input tap` cannot synthesize a callback-identity swap between `ACTION_DOWN`/`ACTION_UP`) — the device pass supplements with a crash-free/no-stuck-ripple confirmation across enabled, disabled, and listening taps.
  - **SC3 — Hub-vocabulary KDoc, `enabled`/`onDisabledTap` defaults.** Confirmed by direct source read at HEAD: KDoc uses `enabled`/`onTap`/`onDisabledTap`/`isListening` throughout, zero "config"/CalTracker wording; `enabled: Boolean = true`, `onDisabledTap: () -> Unit = {}` both present.
  - **SC4 — Backward-compatible; tests/drift guards/zero-baseline detekt green.** Re-run fresh: `testDebugUnitTest`, `ComponentRegistryDriftGuardTest`, `DomainVocabularyDriftGuardTest`, `apiCheck`, `detekt` (0 findings), `publishReleasePublicationToMavenLocal` all `BUILD SUCCESSFUL`; `api.txt` has zero uncommitted diff.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `08-01-SELF-UAT.md` above for the full per-criterion evidence trail (uiautomator bounds/content-desc dumps, before/after tap screenshots, the leaf-vs-merged-semantics-node adversarial check).
  2. Repro on-device: build the same throwaway same-package-Intent harness the log's "Driver-mechanism note" describes (`publishReleasePublicationToMavenLocal` + a `MainActivity` that Intents into `ExplorerActivity`), or wait for a consumer (SecondBrain/CalTracker) to repin onto a tag containing this phase. Browse Buttons/FAB family → `MicButton` row; States matrix shows Default/Pressed-Selected/Disabled/Focused, Variants shows the 3-across row.
  3. Note the held-out nice-to-have: a live TalkBack-enabled activation pass was not separately driven this run (the exact `SemanticsActions.OnClick` API a TalkBack `performAction(ACTION_CLICK)` invokes is already directly exercised by two passing unit tests, and this run independently confirmed the merged semantics node — `clickable=true focusable=true` — exists in the real, non-Robolectric accessibility tree). A hands-on TalkBack pass is optional polish, not required to certify the phase goal.
- **Note:** No schema/DB involved (phase is UI-only, static compiled-in registry). Same reusable gap as Phases 1/6/7: no project-local `AGENT-DEVICE-TESTING.md` exists yet for this library-only repo. Landing this code here does NOT make it live for any consumer — a tag cut + consumer coordinate bump remains a separate, human-gated step (Phase 9).

### Phase 10 — voice-command-settings-surfaces (v2.4)

- **Status:** `pending`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v2.4 (first phase of the milestone)
- **Gate 1 self-UAT log:** [`.planning/phases/10-voice-command-settings-surfaces/10-02-SELF-UAT.md`](phases/10-voice-command-settings-surfaces/10-02-SELF-UAT.md) — Verdict: **ALL 5 ROADMAP success criteria PASS** (device Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN), library AAR md5 `3fcdeb908f1675863a838907569ec4eb` @ `78477f7`, 2026-09-30). Confirms on real hardware that `ProviderKeyCard`'s masking/reveal mechanism genuinely swaps masked-dots-to-raw-text on screen (not just a semantics flag), `ModelSelectCard`'s empty state renders a visible reason rather than a blank control, and `ApproachLadderCard`'s offline-only toggle and tap-a-rung cap both fire live callbacks that visibly update the ladder (greyed-but-present capped rungs; a dedicated null-props variant renders zero controls, not disabled-looking ones).
- **Items covered (5 ROADMAP success criteria):**
  - **SC1 — Provider/API-key card renders from props + callbacks, no key held, no network (VSET-01).** Live device drive: key masked by default (`"••••••••••••••••••••"` in the accessibility tree, no raw value anywhere); tapping the reveal eye flips both the semantics text AND the on-screen pixels to the raw fixture key (`sk-fixture-key-value`), confirmed in a screenshot. Provider dropdown selection (`OpenAI` → `Anthropic`) updates live on tap. `adb logcat` across the whole session: 0 FATAL/Exception lines, 0 occurrences of the raw key value anywhere in the log (INV-01 live-confirmed, not just source-grepped). `ProviderKeyCardTest` 5/5 fresh at HEAD.
  - **SC2 — Model card renders model(s) from props, emits selection via callback (VSET-02).** Default/Pressed-Selected cells render `GPT-4`/`Local Llama (offline)` from fixture props; the empty-models Disabled cell renders a legible caption `"Set a provider and key first"` — confirmed by screenshot to be genuinely readable placeholder text, not a blank or greyed-out dropdown shell. `ModelSelectCardTest` 3/3 fresh at HEAD.
  - **SC3 — Command-approach card displays the ordered tier ladder from props (VAPPR-01).** `ApproachLadderCardTest`'s own deliberately-unsorted fixture decisively proves list-order (not re-sorted) at the unit-test rung; live device render across all 5 gallery cells shows the same `Cloud, Hybrid, Local` order consistently. `ApproachLadderCardTest` 7/7 fresh at HEAD.
  - **SC4 — Offline-only toggle reflects/emits via props + callback (VAPPR-02).** Live tap on the "Offline only" segment immediately added a "Needs network" tag to the non-offline-capable rung in the SAME dumped cell (before/after comparison, not a fixture difference) — genuine `onOfflineOnlyChange` emit confirmed on the real running app.
  - **SC5 — Max-tier cap reflects/emits via props + callback (VAPPR-03).** Live tap-a-rung immediately added a "Capped" tag to the rung above the tapped one; screenshot confirms the capped rung renders in a visibly lighter/greyed tone versus bold siblings — genuinely greyed-but-present, not merely tagged. A dedicated gallery Variants cell ("ApproachLadderCard — every control hidden (null offlineOnly/cap props)") screenshots to zero toggle row and zero cap tags at uniform full opacity — confirms null-prop controls are absent, not disabled-looking.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `10-02-SELF-UAT.md` above for the full per-criterion evidence trail (uiautomator dumps, before/after tap comparisons, screenshots in `10-02-SELF-UAT-evidence/`).
  2. Repro on-device: build the same throwaway same-package-Intent harness the log's "Driver-mechanism note" describes (`publishReleasePublicationToMavenLocal` + a `MainActivity` that Intents into `ExplorerActivity`), or wait for a consumer to repin onto a tag containing this phase. Browse Voice Command family (10th row, below the fold — scroll down) → `ProviderKeyCard` / `ModelSelectCard` / `ApproachLadderCard`.
  3. **The 2 deferred visual/interaction-feel items (unchanged from `10-UAT.md`/`10-VERIFICATION.md`, NOT re-litigated by this Gate-1 pass) still need your judgment here:**
     - Masked-key reveal affordance **contrast/readability quality**, light AND dark theme (mechanism already confirmed working, above — this is the "does it look/feel good" call).
     - Tap-a-rung cap **interaction feel**/discoverability + hidden-vs-shown-disabled **layout quality**, light AND dark theme (mechanism already confirmed working, above).
- **Note:** No schema/DB involved (phase is UI-only, static compiled-in registry). Same reusable gap as Phases 1/6/7/8: no project-local `AGENT-DEVICE-TESTING.md` exists yet for this library-only repo (backlog item `999.1-formalize-reusable-gate-2-visualization-harness-apk` remains an empty placeholder). Landing this code here does NOT make it live for any consumer — a tag cut + consumer coordinate bump is a separate, human-gated step later in the milestone.
