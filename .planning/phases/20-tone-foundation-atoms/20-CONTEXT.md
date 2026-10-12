# Phase 20: Tone foundation & atoms - Context

**Gathered:** 2026-10-12
**Status:** Ready for planning

<domain>
## Phase Boundary

This phase adds the tone tokens (`ToneColors`, `ToneDefaults`, `ContentTone`) and four new atoms (`RoundIconButton`, `StackedProgressBar`, `ToneCard`, `PillChip`). It also adds an `emphasis`/`icon` axis to `DynamicActionButton`. Every existing `DynamicActionButton` call renders exactly as in v2.5.1. Requirements: VTONE-01, VBTN-01, VBTN-02, VBAR-01, VCARD-01, VCHIP-01.

</domain>

<decisions>
## Implementation Decisions

### Signatures (locked by the decision record)
- **D-01 [signatures]:** Public signatures are exactly those in `.planning/seams/v2.6.0-SKETCH.md` §2 Y-1, Y-8, Y-9, Y-10 and Y-12. CT and SB signed them off with no deltas. Do not rename or reorder params. — **Reversibility:** one-way — consumers code against the signed-off signatures.
- **D-02 [compat]:** Source compatibility only (P1). Do not add `@Deprecated(HIDDEN)` overloads, hand-written old-arity `copy`, hidden ctors or `copy$default` shims. New params go after `modifier` with defaults. New behavior uses new enums; no existing enum gains constants.
- **D-03 [api-dump]:** Regenerate `api.txt` (`./gradlew apiDump`) in the same commit as each public API change so `apiCheck` stays green. Expect lane-2 pre-commit classification when an existing source line is rewritten; declare it with `HUB_LANE_OVERRIDE=2`.

### tone-pkg
- **D-04 [tone-pkg]:** `ToneColors`, `ToneDefaults` and `ContentTone` live in `theme/`, since they are tokens like `Dimens`. _(ai-auto)_
- **D-05 [tone-defaults]:** `ToneDefaults.error()` = `errorContainer`/`error`, and `warn()` = `tertiaryContainer`/`onTertiaryContainer`. Both are `@Composable` and read `MaterialTheme.colorScheme`. `ToneColors` is an `@Immutable class` with value `equals`/`hashCode`. The KDoc says consumers pass their own tokens when dynamic color makes `tertiary` non-amber.

### atom-family
- **D-06 [atom-family]:** Register `RoundIconButton` → ButtonsFab, `StackedProgressBar` → Progress, `ToneCard` → Cards and `PillChip` → Chips, in this phase, so the drift guard stays green. _(ai-auto)_

### emphasis-impl
- **D-07 [emphasis-impl]:** Build the emphases from M3 `Button`/`TextButton` with color and `contentPadding` overrides:
  - **Filled:** pill, `primary`/`onPrimary`, 20dp × 8dp padding, 40dp tall.
  - **Tonal:** a `Button` with `containerColor = primary.copy(alpha = 0.14f)` and `primary` content, same padding.
  - **Text:** a `TextButton`, 12dp × 8dp padding.
  - **TextSmall:** a `TextButton` with a `bodySmall` label, 8dp × 4dp padding, and a ≥ 48dp touch target.

  Filled, Tonal and Text labels are `labelLarge` SemiBold. An optional leading icon is 18dp with an 8dp gap. `role` picks the hue (Destructive → `error`, otherwise `primary`). With `emphasis = null`, the existing branch is untouched. _(ai-auto)_

### Atom details
- **D-08 [round-icon]:** `RoundIconButton` is a 40dp (or `size`) circle with a 1dp `outlineVariant` border, a transparent container and a 20dp `onSurface` icon, inside a 48dp touch target. It uses `outlineVariant`, not M3 `OutlinedIconButton`'s `outline`.
- **D-09 [bar]:** For `StackedProgressBar`, a negative fraction clamps to 0. Fractions accumulate left to right and the total clamps to ≤ 1. The bar clips to a fully rounded track. `trackColor` is required. Tag: `stacked_progress_bar`.
- **D-10 [tags]:** The atoms carry Y-14 tags from the start: `round_icon_button`, `stacked_progress_bar`, `tone_card`, `pill_chip`. A consumer `modifier.testTag` can override them. Never set `testTagsAsResourceId`.

### Claude's Discretion
- Internal file split, private helpers, and test file names.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision record & requirements
- `.planning/seams/v2.6.0-SKETCH.md` — signed-off signatures (§0 compat mechanics, §2 Y-1/Y-8/Y-9/Y-10/Y-12, §3 pushbacks P1-P3)
- `.planning/REQUIREMENTS.md` — VTONE-01, VBTN-01, VBTN-02, VBAR-01, VCARD-01, VCHIP-01
- `.planning/ROADMAP.md` § Phase 20 — success criteria
- `.planning/v2.6-DECISION-MAP.md` § Phase 20

### Consumer spec (pixel specs)
- `/home/yahir/Projects/AndroidApps/Personal/CalTracker_Android/.planning/phases/76-gate-2-gap-closure-voice-sheets-piece-weight-grammar-header/76-UI-SPEC.md` § "YAT Evolution Asks" Y-1, Y-8, Y-9, Y-10, Y-12, Y-14; § Spacing / Typography / Color. This is the UI contract for this phase.

### Hub conventions
- `CLAUDE.md` (repo root) — invariants, toolchain, zero-baseline detekt
- `API.md` § Compatibility rule; `CONVENTIONS.md`; `tools/README-api-guard.md`

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `component/DynamicActionButton.kt` + `ActionButtonDefaults` (roles Save/Neutral/Destructive) — extend; keep the default branch.
- `theme/Dimens.kt` (`TouchTarget` 48dp, `CornerRadius.Medium` 12dp, `ContentSpacing` 4dp, `HorizontalPadding` 16dp) — use for every dimension.
- `material-icons-extended` is already a dependency.

### Established Patterns
- Registry: every public top-level `@Composable` in `component/`/`feedback/`/`modifier/`/`theme/` is registered XOR allowlisted (`explorer/*FamilyScreen.kt`, `ComponentRegistry`).
- Robolectric + Compose UI tests under `src/test`. NATIVE graphics are used where pixel colors are asserted (`ApproachLadderCardGalleryDemoTest`).

### Integration Points
- The Phase 21/22 consumers of these atoms are `ReceiptRow`, the Receipt `OutcomeSheet` and the `ProviderKeyCard` actions.

</code_context>

<specifics>
## Specific Ideas

- The long-localized-label lesson: check any caller-overridable label with a long German string at ~360dp.

</specifics>

<deferred>
## Deferred Ideas

None. This phase stays within its scope.

</deferred>
