# Project Research Summary

**Project:** yahirandroidtaste
**Milestone:** v2.4
**Domain:** Reusable Jetpack Compose design-system library — additive, prop-driven AI-voice command UI cohort (settings surfaces + outcome/confirm surfaces)
**Researched:** 2026-09-29
**Confidence:** HIGH

## Executive Summary

Milestone v2.4 adds a cohort of **generic, presentational Compose composables** to the reusable
`yahirandroidtaste` hub: two settings cards (provider/API-key, model), a command-approach card
(tier ladder + offline-only + max-tier cap), and a single outcome/confirm sheet that renders
success, loud-failure, and needs-confirmation states. The defining constraint — repeated across all
four research streams — is that this is a **display-and-emit** slice: the library renders whatever
the consumer passes and emits decisions via callbacks; it never validates a key, fetches a model
list, calls a provider, parses an engine result, or enforces the tier ladder. That is INV-01 (the
one-way-dependency invariant) and it is a hard ship gate (Phase 13 SC-3).

The recommended approach is **zero new dependencies, all-additive public API.** Every control maps
to a stock Material 3 / Compose API already resolvable from the pinned Compose BOM `2026.04.01`
(including the two first-in-tree APIs, `ExposedDropdownMenuBox` and `PasswordVisualTransformation`),
and every new symbol is net-new (no growth of shipped types). The architecture is settled: new
composables land **flat in `component/`**, new immutable all-`val` models in `model/`, and the whole
cohort registers as **one new tenth ComponentRegistry family** ("Voice Command" label), with the
outcome sheet built as a **single sealed-state composable** so Phase 12's confirmation state is a
purely additive subtype on the Phase 11 shell.

The risks are almost entirely about silent erosion of reusability and API stability, not technical
difficulty. The top four: (1) a domain noun leaking into a public name/param/string (the repo's
`DomainVocabularyDriftGuardTest` mechanically rejects a `Voice`-headed composable name); (2) the
needs-confirmation prop shape fitting only one consumer — mitigated by modeling single as a
`List`-of-one batch and paper-validating both SB and CT call-sites before coding; (3) a non-additive
`copy()`/data-class ABI break (the documented `TagChipUiModel` lesson) — mitigated by all-new,
constructor-frozen, all-`val` models; and (4) missing `ComponentRegistry` registration, which only
fails in the **full** test suite, not scoped executor runs. All are prevented at authoring time with
Phase 13 as the backstop gate.

## Key Findings

### Recommended Stack

**No stack additions and no version changes.** All in-scope UI is generic, prop-driven, and
presentational — Material 3 plus the library's already-declared Compose surface covers 100%. The two
APIs the design needs that aren't yet used in-tree ship inside the pinned Compose BOM `2026.04.01`,
so they add no coordinate. Adding anything network/serialization/engine-shaped (OkHttp, Retrofit,
kotlinx-serialization, a `voice-action-engine` dependency) would break INV-01 and fail the Phase 13
ship gate — these are forbidden, not merely discouraged.

**Core technologies (all already declared):**
- Compose BOM `2026.04.01` — single source of Compose versions; every needed API resolves from it, no per-artifact bump
- `material3` (via BOM) — Cards, `ModalBottomSheet`, `Switch`, `SegmentedButton`, `ExposedDropdownMenuBox`, `OutlinedTextField`; the primitive layer for every control
- `compose.ui` + `material-icons-extended` (via BOM) — `PasswordVisualTransformation` for key masking, plus failure/tier glyphs
- Hilt `2.60.1` (bindings-only) — available if a `@Singleton` holder is ever needed; almost certainly not (these are stateless). Never add `@HiltAndroidApp`/`@AndroidEntryPoint`

Reuse, don't reinvent: `SheetScaffold` (house `ModalBottomSheet` wrapper), `SegmentedOptionSelector`,
`ClearableTextField`, `AppChip`/`ChipBar`, `DynamicActionButton`, and the theme's `error`/
`errorContainer` roles for the loud-failure state.

### Expected Features

This is a presentational slice, so "features" = component-behavior expectations and, critically, the
**prop-shape + 4-cell states-matrix** per composable.

**Must have (table stakes):**
- Provider/key card — provider selector (segmented ≤4 / dropdown if more), masked key entry default-hidden with reveal toggle, `keyState` render (library never validates), set/not-set via last-4 prop
- Model card — `models` list + selection + empty/disabled (no provider) / loading / error states
- Command-approach card — ordered display-only tier ladder + offline-only toggle + max-tier cap rendered *on* the ladder (capped rungs greyed-but-present); derived per-rung availability computed in the composable from primitives
- Outcome sheet — success/failure from props, nullable "handled by" provenance chip, and a **loud** (error-color, icon, headline, reason) failure state
- Needs-confirmation — **one composable, one prop shape**: `reason` + `items: List` (single = list-of-one) + `AllOrNothing`/`PerItem` selection mode + symmetric confirm/cancel
- Every composable registered in `ComponentRegistry` with a full 4-cell states matrix

**Should have (competitive / additive slots):**
- `onValidateKey` "test key" action slot (state + callback only; consumer does the probe)
- Per-item include/exclude for `PerItem` batch (needed for CT weak-match "confirm each")
- Destructive `severity` emphasis on confirm; per-rung tag colors (reuse v2.3.0 TAGCOLOR); provenance `trace` expander

**Defer (v2+ / consumer wave):**
- Cost/usage surfacing in the model card (needs an undefined usage contract)
- Multi-key / key-rotation UI (no consumer needs it; tempts library-side storage)

### Architecture Approach

The cohort integrates additively into the existing library: composables **flat in `component/`**
(the 60 existing components are flat; families are a registry taxonomy, not a package layout);
immutable all-`val` models in `model/`; and one new tenth registry family, `voiceCommandFamilyEntries`,
mirroring how Progress/Metrics and Tactile Foundation each landed as a cohesive new-family drop.
Data flow is strictly prop-in / callback-out; the library holds nothing and calls nothing.

**Major components:**
1. New `component/*` composables — render voice settings/outcome UI from props+callbacks; presentation-only, hoisted state
2. New `model/*` immutable models — a sealed outcome/confirm state + supporting UI models the consumer maps its engine output into (all-`val`, Compose-`STABLE`-inferable)
3. One sealed-state outcome sheet (host + content split) — Success/Failure in Phase 11; `NeedsConfirmation` added as an additive subtype + one `when` branch in Phase 12
4. New registry family + gallery wiring — one `Entry` per composable (`tier = PATTERN`, 4-cell states), plus the family in `ExplorerFamilies` consts + `ORDERED_KEYS`

> **Doc-drift flag:** `CLAUDE.md`/the brief say "seven" registry families; the live code
> concatenates nine, and this cohort makes ten. Phase 13 should correct that wording.

### Critical Pitfalls

1. **Domain noun/assumption leak into a "generic" composable** — a name, param, string, or enum encodes one consumer's domain. The repo's `DomainVocabularyDriftGuardTest` mechanically rejects a public composable whose head token isn't allowlisted. Settle a structural naming vocabulary at the top of Phase 10; keep every user-facing string a prop with no domain default; never allowlist a domain head token to silence the guard.
2. **Needs-confirmation shape too narrow for both consumers** — fits SB's risk-confirm OR CT's weak-match, not both. Model the structural union: `reason: String` + `items: List` (single = size 1) + slot APIs (`trailingContent`) for per-consumer badges. Paper-validate both SB `MutationGate` and CT weak-match call-sites against one signature *before* coding (Phase 12 SC-4).
3. **Non-additive API / `copy()`-ABI break** — growing a `data class` primary constructor is an unfixable ABI removal (`@JvmOverloads` can't paper over synthetic members). Prefer all-new symbols; freeze model constructors on first authoring; add later fields as defaulted body properties or use non-`data`/interface+slots; keep models all-`val` (a `var` also kills `STABLE`).
4. **Missing `ComponentRegistry` registration — full-suite-only guard** — CATALOG-03's drift guard fails only in the full suite, not scoped executor tests, so a phase looks green while the gallery silently lacks the component. Register-as-you-author (name + 4-cell states + tier) and run `./gradlew testDebugUnitTest` (full suite) at least once per phase.
5. **Forbidden dependency/coupling** — OkHttp/serialization/engine sneaking in to "make it work." Hold the line: the library renders passed-in state and never fetches/validates/parses/calls. No new `build.gradle.kts` dependency this milestone.
6. **Silent/subtle failure state** — happy-path-first authoring leaves failure a muted subtitle or an empty `when` branch. Treat failure as first-class (error color/icon/headline/reason), make the `when` exhaustive with a loud fallback, and drive the failure/cancel branches explicitly in Gate-1 self-UAT.

### Cross-cutting tension to resolve: composable naming vs. family/model naming

The research streams **split** on voice naming and this must be an explicit decision, not a silent
pick:

- **ARCHITECTURE** proposes `Voice*`-prefixed composable names/models (`VoiceProviderSettingsCard`,
  `VoiceOutcomeSheet`, `VoiceOutcomeUiState`, …) and a "Voice Command" family.
- **PITFALLS** shows the live `DomainVocabularyDriftGuardTest` **rejects** any public composable
  whose head token is `Voice` — only `VoiceCard`/`MicButton`/`VoiceRenameTagsSheet` are grandfathered
  in `DOMAIN_VOCABULARY`, and `Voice` is not in `PRIMITIVE_NOUN_ALLOWLIST`. A new `Voice…`
  composable goes RED at build time.

**Reconciliation (recommended, flag for discuss/planning):** the two are separable concerns.
- **Composable names must be structural primitives** — e.g. `CommandOutcomeSheet` / `ConfirmationSheet`,
  `ProviderKeyCard`, `ApproachLadderCard`, `ModelSelectorCard` — using already-allowlisted head tokens
  (`Card`, `Sheet`, `Bar`, `Ladder`, `Badge`, `Field`, `Selector`, `State`, `Confirmation`). Note
  even these head tokens (`Command`, `Approach`, `Provider`) must be checked against the live
  allowlist; some may themselves need resolution.
- **The family label and UI-model class names are a separate question** — the family display string
  ("Voice Command") and model class names are not scanned by the composable head-token guard, so they
  have more latitude, but they should still read domain-neutral where practical.

Do **not** silently adopt the `Voice*` names from ARCHITECTURE, and do **not** reflexively allowlist
`Voice`/`Command` into `PRIMITIVE_NOUN_ALLOWLIST` to make the guard pass. This is a naming decision
to settle **at the top of Phase 10** (discuss/planning) and reuse across all phases.

## Implications for Roadmap

The roadmap is already fixed at four phases (10–13); research confirms the ordering and loads each
with its prop-shapes, reuse targets, and pitfalls.

### Phase 10: Settings surfaces (provider/key, model, command-approach cards)
**Rationale:** Independent of the outcome sheet; the largest authoring surface; the phase where the
**naming vocabulary must be settled** and reused everywhere after.
**Delivers:** Three prop-driven settings cards (provider/key with masked-reveal entry + `keyState`;
model with list/selection/empty-loading-error; command-approach with ordered ladder + offline-only +
max-tier cap rendered on the ladder).
**Uses:** `ExposedDropdownMenuBox`/`SegmentedOptionSelector`, `PasswordVisualTransformation`,
`ClearableTextField`, `Switch`, theme roles — all from the pinned BOM, zero new deps.
**Avoids:** Domain-noun leak (Pitfall 1 — settle names here), forbidden dep (Pitfall 5 — cards hold
no key, call nothing), non-additive API (Pitfall 3 — all-new symbols).

### Phase 11: Outcome/failure sheet (sealed-state shell)
**Rationale:** Independent of Phase 10, runs in parallel; must ship the **sealed** `Outcome` state
(Success/Failure) and a sheet param typed as the sealed interface so Phase 12 is purely additive.
**Delivers:** One outcome sheet inside `SheetScaffold` with success, a **loud** failure state, and a
nullable "handled by" provenance chip; host+content split.
**Implements:** The sealed-state outcome-sheet architecture (Pattern 1).
**Avoids:** Silent/subtle failure (Pitfall 6 — this phase owns the loud-failure gate; do not use
`AttentionCue`, whose KDoc forbids failure use), non-additive API (freeze the sealed type here).

### Phase 12: Needs-confirmation state (additive subtype)
**Rationale:** Gates on Phase 11's sheet — `NeedsConfirmation` is a new subtype + one `when` branch,
no signature change. This phase exists to satisfy **both** consumers with one shape.
**Delivers:** `reason` + `items: List` (single = list-of-one) + `AllOrNothing`/`PerItem` +
symmetric confirm/cancel + optional per-item slot and destructive severity.
**Avoids:** The narrow-confirm-shape trap (Pitfall 2 — paper-validate SB `MutationGate` and CT
weak-match call-sites against one signature before coding), silent cancel/reject state (Pitfall 6).

### Phase 13: Catalog & ship (integrity gate)
**Rationale:** Gates on all prior phases; the hard verification gate, not new feature surface.
**Delivers:** Every composable registered in `voiceCommandFamilyEntries` (4-cell states + tier),
the tenth family wired into `ExplorerFamilies`, full-suite green, Metalava strictly-additive vs
`v2.3.0`, detekt zero-baseline, the "seven families" doc-drift corrected, and `v2.4.0` cut.
**Avoids:** Missing registration (Pitfall 4 — full suite, not scoped tests), non-additive API
(Pitfall 3 — `apiCheck` + `verify-api-additive.sh`), forbidden dep (Pitfall 5 — grep sources +
`build.gradle.kts`).

### Phase Ordering Rationale
- P10 ∥ P11 are genuinely independent (settings vs outcome); running them in parallel is safe.
- P12 depends on P11 because it extends the same sealed sheet; shipping the sealed type early keeps
  P12 additive (decisive for Metalava).
- P13 is last because it is the aggregate integrity gate (registry + additive API + no-dep + ship).
- Every pitfall is a per-composable authoring discipline (P10–12) with P13 as the mechanical backstop
  — the *thinking* must not be deferred to P13, since a break authored in P10 and caught in P13 costs
  a P10 rework.

### Research Flags

Phases likely needing deeper attention during planning:
- **Phase 10:** The command-approach card is the one **non-standard** surface (no off-the-shelf
  Material pattern for a tier ladder + on-ladder cap marker + derived offline availability). Its
  interaction model and states matrix warrant deliberate design in discuss/plan. Also the phase that
  must resolve the **naming-vocabulary tension** above.
- **Phase 12:** The dual-consumer confirm prop shape is the sharpest design risk; plan must include
  both worked call-sites (SB + CT) as acceptance evidence before authoring.

Phases with standard patterns (lighter research):
- **Phase 11:** Well-understood M3 bottom-sheet + loud-error patterns; house `SheetScaffold` exists.
- **Phase 13:** Mechanical integrity gate against existing, documented guards and tooling.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Grounded in the live `build.gradle.kts` / `libs.versions.toml` and in-tree API greps; "no new deps" is verified, not inferred |
| Features | HIGH | Established M3/Compose UX patterns; prop-shapes derived from the frozen §6.3 contract + SB `MutationGate`/`VoiceConfirmGate` and CT weak-match |
| Architecture | HIGH | Grounded in live source (`ComponentRegistry.kt`, drift-guard tests, `ExplorerIndexScreen.kt`, `model/`, `API.md`) |
| Pitfalls | HIGH | Grounded in the repo's own live guards, the documented v2.0 `copy()`-ABI break, and the cross-repo HANDOFF |

**Overall confidence:** HIGH

### Gaps to Address
- **Naming vocabulary (composable head tokens):** the exact structural names must be checked against
  the live `PRIMITIVE_NOUN_ALLOWLIST`/`DOMAIN_VOCABULARY` — `Command`/`Approach`/`Provider` head
  tokens may themselves need a decision. Resolve in Phase 10 discuss/planning; do not assume.
- **Command-approach card interaction design:** no prior-art Material pattern; the ladder + on-ladder
  cap + derived offline-availability presentation needs a concrete states-matrix decision in Phase 10.
- **Confirm shape per-item vs top-level callbacks:** whether confirm/cancel live on the sealed arm or
  as nullable sheet callbacks, and the exact `PerItem` selection API, should be nailed down with both
  consumer call-sites in Phase 12 planning.
- **"Handled by" nullability on failure:** confirm the provenance indicator renders on failure
  ("attempted by X, failed") not only on success — a Gate-1 verification item.

## Sources

### Primary (HIGH confidence)
- `.planning/research/STACK.md` — no-new-deps finding grounded in `build.gradle.kts`, `libs.versions.toml`, in-tree API greps, pinned Compose BOM `2026.04.01`
- `.planning/research/FEATURES.md` — per-phase prop-shapes, states matrices, anti-features; frozen §6.3 contract + SB/CT confirm concepts
- `.planning/research/ARCHITECTURE.md` — flat `component/`, one new registry family, sealed-state sheet; grounded in `ComponentRegistry.kt`, drift-guard tests, `ExplorerIndexScreen.kt`, `model/`, `API.md`
- `.planning/research/PITFALLS.md` — domain drift guard, `copy()`-ABI break (`TagChipUiModel`), CATALOG-03 full-suite-only guard, INV-01, loud-failure rule; grounded in live guards + cross-repo HANDOFF
- `.planning/ROADMAP.md`, `.planning/PROJECT.md`, root `CLAUDE.md` — phase success criteria, INV-01, additive + human-gated ship ritual

### Secondary (MEDIUM confidence)
- Material 3 segmented-button / dropdown count guidance; password-field accessibility patterns (cited in FEATURES.md) — established UX consensus

### Tertiary (LOW confidence)
- None load-bearing; all findings resolve to repo source or the frozen contract

---
*Research completed: 2026-09-29*
*Ready for roadmap: yes*
