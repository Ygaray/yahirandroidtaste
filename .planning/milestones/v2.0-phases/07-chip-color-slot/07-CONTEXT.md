# Phase 7: Chip-color slot - Context

**Gathered:** 2026-09-26
**Status:** Ready for planning

<domain>
## Phase Boundary

A caller can opt into a per-tag chip container color, backward-compatibly: `color: Color? = null` on
`TagChipUiModel`, `containerColorOverride: Color? = null` on `AppChip` and `TagChipWithContextMenu`,
auto-threaded from `tag.color` at `CardTagRow`. The hub renders the supplied color as-is (consumer owns
the muted/theme-aware policy); the override loses to `isSelected`/`relatednessStrength`; the default-`null`
path is byte-identical to today.

</domain>

<decisions>
## Implementation Decisions

### content-color
- **D-01 [content-color]:** Leave `AppChip.contentColor` untouched — add no new `contentColor` branch (ai-auto). The resting content color in code is `MaterialTheme.colorScheme.onSurfaceVariant`, **not** the `onSurface` the spec prose loosely names; do NOT "correct" it and do NOT wire `ColorUtils.contrastingForeground`, or the default-`null` path stops being byte-identical and the "hub computes no contrast / domain-free" invariant breaks. (Container-override precedence — new branch slotted below `isSelected`/`relatedness`, above `else` — is settled/Confident, so selection/relatedness theme roles win.)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone artifacts
- `.planning/ROADMAP.md` — Phase 7 goal + success criteria
- `.planning/REQUIREMENTS.md` — TAGCOLOR-01
- `.planning/v2.0-DECISION-MAP.md` § Phase 7 — the resolved gray area

### Design + invariants
- `docs/superpowers/specs/2026-09-26-hub-line-reunification-design.md` §5.3 — the full confirmed, SB-verified API (param name, precedence, CardTagRow auto-thread, threading design)
- `CLAUDE.md` (repo root) — domain-free / theme-frozen invariants, drift guards, detekt zero-baseline

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `AppChipTest.kt` establishes the parity-test idiom (Robolectric + Compose, `@Config(sdk=[35])`) and explicitly disclaims pixel-golden testing — reuse it to assert the null-override path behaves identically.
- `Color` is already an established public-API type across the hub (AccentColorPicker, ColorUtils, CardBase, card `accent` params).

### Established Patterns
- `AppChip` computes `containerColor`/`contentColor` from theme roles + `relatednessStrength` + `isSelected`; `relatedness` is only computed when `!isSelected && relatednessStrength != null` — so slotting the override arm below those in the `when` makes precedence fall out of ordering alone (no extra guards).

### Integration Points
- `CardTagRow(tags: List<TagChipUiModel>)` is the model→chip mapper and the auto-thread point — thread `tag.color → containerColorOverride` in **both** visible-chip branches (`TagChipWithContextMenu` and plain `AppChip`); the "+N" overflow chip has no backing model → leave it theme-default.
- `TagChipWithContextMenu` takes `label: String`, not the model — it gets the explicit pass-through param; it is deliberately NOT coupled to `TagChipUiModel` (keeps it domain-free/generic).
- api.txt: append trailing `optional Color?` params (additive metalava shape); no new public composable → no drift-guard impact.

</code_context>

<specifics>
## Specific Ideas

SB owns the muted, theme-aware color policy (computed in a `@Composable` reading `isSystemInDarkTheme()`); the hub only accepts and renders the supplied color. Raw non-tag AppChip uses (Clear filter, language pill) pass `null` and stay theme-colored.

</specifics>

<deferred>
## Deferred Ideas

Which of SB's ~dozen tag-chip sites route through `CardTagRow` (auto-thread) vs. call the chips directly (explicit param) is a consumer-side mapping SB resolves at its own plan time — out of this phase's boundary.

</deferred>

---

*Phase: 7-chip-color-slot*
*Context gathered: 2026-09-26*

## Runtime Decisions

### 2026-09-27 — ABI-break resolution (operator decision, milestone execute stage)
**Decision point (from 07-VERIFICATION.md gap / 07-REVIEW.md WR-01):** `@JvmOverloads` on the
`TagChipUiModel` data class removed the old 5-arg `copy()` overload from `api.txt` — a public-API
ABI break on a JitPack-consumed reusable library (SecondBrain).

**Operator ruling: CODE FIX — restore an additive API.** Do NOT accept the break. Remove
`@JvmOverloads` from the `TagChipUiModel` data class, and instead add a `@JvmStatic
Companion.of(...)` factory annotated `@JvmOverloads` (the alternative 07-REVIEW.md itself proposed).
`api.txt` must end up genuinely additive — no removed symbol, the pre-existing 5-arg `copy()`
overload preserved — so no consumer can hit `NoSuchMethodError`. Then re-run metalava/API check and
resume the tail gates (security → Gate-1 self-UAT → nyquist → verify).
