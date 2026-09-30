# Stack Research

**Domain:** Reusable Jetpack Compose design-system library — presentational AI-voice command UI (settings + outcome/confirm surfaces)
**Researched:** 2026-09-29
**Confidence:** HIGH

## Headline: NO new dependencies

**Milestone v2.4 (Phases 10–13) needs zero stack additions and zero version changes.** Every
composable in scope is generic, prop-driven, and presentational — Material3 plus the library's
already-declared Compose surface covers 100% of it. The only two Material3/Compose APIs the design
calls for that are *not yet used in the tree* — `ExposedDropdownMenuBox` and
`PasswordVisualTransformation` — already ship inside the pinned Compose BOM `2026.04.01`, so they
add **no** new coordinate. This finding is grounded in the actual `build.gradle.kts`,
`gradle/libs.versions.toml`, and in-tree API usage (verified below), not in generic knowledge.

Adding anything network- or engine-shaped (OkHttp, Retrofit, kotlinx-serialization, Moshi, a
`voice-action-engine` module) would **break the one-way-dependency invariant (INV-01)** and fail the
Phase 13 ship gate (Success Criterion 3: "declares no OkHttp or `voice-action-engine` dependency").
See **What NOT to Use**.

## Recommended Stack

### Core Technologies (all already declared — no change)

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Compose BOM | `2026.04.01` (pinned) | Version-aligns every Compose artifact | Already the module's single source of Compose versions; every API below resolves from it — no per-artifact bump needed |
| `androidx.compose.material3:material3` | via BOM | Cards, `ModalBottomSheet`, `Switch`, `Slider`, `SegmentedButton`, `ExposedDropdownMenuBox`, `OutlinedTextField` | The design system's primitive layer; every voice-settings/outcome control is a stock M3 component. BOM `2026.04.01` is long past M3 stabilization of all these APIs |
| `androidx.compose.ui:ui` (+ `ui-graphics`, `ui-tooling-preview`) | via BOM | `PasswordVisualTransformation` (in `androidx.compose.ui.text.input`), layout, previews | Masking the API-key field needs nothing beyond `androidx.compose.ui` — already declared |
| `androidx.compose.material:material-icons-extended` | via BOM | Tier/approach + failure iconography (e.g. warning, offline, check) | Already declared; gives the "loud failure" and "handled by tier" indicators their glyphs with no new dep |
| Hilt (bindings-only) | `2.60.1` | If any voice composable needs a `@Singleton` state holder | Already the library's bindings-only pattern (`UndoHistoryStore`). Almost certainly **not** needed — these are stateless prop-driven composables — but available without change. Never add `@HiltAndroidApp`/`@AndroidEntryPoint` |

### Supporting Libraries

None required. The library's other declared deps (`coil-compose`, `navigation-compose`,
`reorderable`, `osmdroid-android`, `lifecycle-runtime-compose`) are irrelevant to this milestone and
need no touching.

### Development Tools (unchanged, already wired)

| Tool | Purpose | Notes |
|------|---------|-------|
| Metalava `apiCheck` (`me.tylerbwong.gradle.metalava:0.5.0`) | Enforces strictly-additive public API vs `v2.3.0` | Phase 13 SC-2 gate; new composables are additions only |
| detekt `1.23.8` (zero-baseline) | Static analysis | Keep green at zero baseline — do not regenerate a baseline to bury a finding |
| ComponentRegistry drift guard + Robolectric Compose-UI tests | Registers every new public composable + locks its 4-cell states matrix | Phase 13 SC-1; register in the right family screen (Sheets, and a Settings/Pickers surface for the cards) |

## Material3 / Compose APIs available in Compose BOM `2026.04.01`

Mapped to each phase. "In-tree" = the exact API is already imported and used in this repo (verified by
source grep), so there is precedent and a house wrapper to reuse.

| Phase | Need | Material3 / Compose API | Status in BOM `2026.04.01` | In-tree today |
|-------|------|--------------------------|-----------------------------|---------------|
| P10 provider/key card | Provider selection dropdown | `ExposedDropdownMenuBox` + `ExposedDropdownMenu` (M3) | Stable, available | Not yet used — first use; no new dep |
| P10 provider/key card | API-key entry (secure) | `OutlinedTextField` + `PasswordVisualTransformation` (`androidx.compose.ui.text.input`) + a reveal `IconButton` | Stable, available | `OutlinedTextField` in 3 files; `PasswordVisualTransformation` first use |
| P10 model card | Model pick (list/dropdown) | `ExposedDropdownMenuBox` **or** `SegmentedButton` row for few models | Stable, available | `SegmentedOptionSelector` already wraps `SingleChoiceSegmentedButtonRow` |
| P10 command-approach card | Tier-ladder display (ordered approaches) | `Column`/`Row` of M3 `ListItem`/`AssistChip` + icons — pure layout | Stable, available | Cards/chips families already exist |
| P10 command-approach card | Offline-only toggle | `Switch` (M3) | Stable, available | `Switch(` used in 1 file — precedent |
| P10 command-approach card | Max-tier cap control | `SingleChoiceSegmentedButtonRow` (discrete tiers) — preferred over `Slider` for a small discrete ladder; `Slider`/`Slider` steps available if a continuous feel is wanted | Stable, available | `SegmentedButton` in 6 files; `Slider` in 3 files |
| P11 outcome/failure sheet | Domain-neutral outcome sheet | `ModalBottomSheet` via the house `SheetScaffold` wrapper | Stable, available | `ModalBottomSheet` in 13 files; `SheetScaffold` wraps it |
| P11 | "handled by: tier/approach" indicator | `AssistChip`/`Badge`/`Text` + `material-icons-extended` glyph | Stable, available | Chips + icons families exist |
| P11 | Loud, visible failure state | `error`/`errorContainer` color roles from the theme + icon; not silent | Stable, available | Theme tokens already defined |
| P12 needs-confirmation state | reason string + single-or-batch proposed items + confirm/cancel | Same `ModalBottomSheet` content + `LazyColumn`/`Column` of items + two `Button`s | Stable, available | Sheet + list patterns exist |

Every row resolves from the already-declared `androidx.compose.material3:material3` and
`androidx.compose.ui:ui` artifacts under the pinned BOM. There is nothing to install.

## Installation

```bash
# Nothing to install. No change to build.gradle.kts or gradle/libs.versions.toml.
# New composables live under src/main/.../component/ (or a new voice/ package) and compile
# against the already-declared Material3 + Compose UI surface.
```

## Integration Points (reuse, don't reinvent)

- **`SheetScaffold`** (`component/SheetScaffold.kt`) — the single house wrapper over
  `ModalBottomSheet` (drag handle, window insets). The P11 outcome/failure sheet and the P12
  confirmation state should render **inside** `SheetScaffold`, not call `ModalBottomSheet` directly
  — that is the library-wide convention.
- **`SegmentedOptionSelector`** (`component/SegmentedOptionSelector.kt`) — existing generic wrapper
  over `SingleChoiceSegmentedButtonRow`. Reuse for the max-tier cap and/or few-model selection;
  extend only if a >2-option variant is needed (keep additive, register the new entry).
- **`ComponentRegistry`** (`explorer/ComponentRegistry.kt`) — every new public composable must be
  registered in its family screen (Sheets for the outcome/confirm sheet; a settings surface —
  likely Pickers/Cards or a new family) XOR allowlisted in `INTENTIONALLY_UNREGISTERED`. Drift guard
  fails the build otherwise (Phase 13 SC-1).
- **Snackbar/undo + reveal-confirm conventions** — preserve; the confirmation state is an explicit
  in-sheet confirm/cancel (props + callbacks), not a swipe.
- **Theme tokens** (`theme/`) — use the existing `error`/`errorContainer` color roles for the "loud
  failure" states rather than hardcoded colors.

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| `SegmentedButton` for max-tier cap (discrete ladder) | `Slider` with `steps` | If the tier ladder is long enough (5+) that segments crowd; `Slider` is already in-tree so either is dependency-free |
| `ExposedDropdownMenuBox` for provider/model | `SingleChoiceSegmentedButtonRow` | Few (≤3) providers/models → segmented reads better; many → dropdown. Both are stock M3, no dep either way |
| `PasswordVisualTransformation` + reveal toggle | Plain `OutlinedTextField` | Never for an API key — masking is table-stakes; both are dependency-free |

## What NOT to Use

The one-way-dependency invariant (INV-01) and Phase 13 SC-3 make these **forbidden**, not merely
discouraged. The library renders voice-command *state*; it never *performs* voice commands.

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| **OkHttp / Retrofit / Ktor** (any HTTP client) | Networking belongs to the consumer's engine. The library holds no keys and makes no calls (P10 SC-1). Adding it breaks INV-01 and fails Phase 13 SC-3 | Consumer's app owns the client; the library takes provider/model/key **as props + callbacks** |
| **kotlinx-serialization / Moshi / Gson** (any (de)serializer) | Implies the library parses engine/wire payloads — a domain assumption. Outcomes/confirm data arrive as already-typed Kotlin props | Consumer maps its engine outcome → the composable's plain data-class/enum params |
| **A `voice-action-engine` module dependency** (or any consumer module) | Directly inverts the one-way dependency; the hub would import a consumer/engine. Explicitly barred by Phase 13 SC-3 | The engine lives entirely consumer-side; the hub exposes a domain-neutral shell the consumer feeds |
| **Any speech/STT/ASR SDK, audio-recording lib, or on-device ML runtime** | Capturing/recognizing speech is engine work, not presentation. `MicButton` is already a prop-driven trigger, not a recorder | Consumer wires the mic/STT; the library only renders the button + resulting state |
| **A key-store / EncryptedSharedPreferences / DataStore persistence dep** | The library "holds no secrets" and does no key persistence (P10 SC-1). Persistence is host-owned | Key is a prop in, edits emit via callback; consumer persists it |
| **New `@HiltAndroidApp` / `@AndroidEntryPoint`** | Bindings-only Hilt invariant; the consumer owns the Hilt `Application` | If a `@Singleton` holder is ever needed, `@Inject constructor()` bindings-only — but these stateless composables likely need none |
| **Domain nouns in any public symbol** | Domain-vocabulary drift guard flags app-specific names; these are generic surfaces | Neutral names ("outcome", "approach", "tier", "proposed item"); content arrives via props |

## Version Compatibility

| Package | Compatible With | Notes |
|---------|-----------------|-------|
| Compose BOM `2026.04.01` | AGP `9.2.1`, Kotlin `2.3.20`, `kotlin.plugin.compose` | Already the working, shipped combination (through `v2.3.0`). No change means no compatibility risk |
| `ExposedDropdownMenuBox`, `PasswordVisualTransformation` | Compose BOM `2026.04.01` | Both long-stable APIs resolved by the existing `material3` / `ui` artifacts — first use in-tree, but no new coordinate and no version bump |
| Metalava `apiCheck` | New additive composables | Additions pass; run `./gradlew apiDump` to refresh `api.txt` after adding public symbols, then `apiCheck` stays green vs `v2.3.0` |

## Sources

- `build.gradle.kts` + `gradle/libs.versions.toml` (this repo, read 2026-09-29) — confirmed declared
  deps and pinned Compose BOM `2026.04.01`; no HTTP/serialization/engine dep present — HIGH
- In-tree source grep (this repo) — confirmed `ModalBottomSheet` (13 files, via `SheetScaffold`),
  `SegmentedButton`/`SingleChoiceSegmentedButtonRow` (6 files, `SegmentedOptionSelector`), `Slider`
  (3), `OutlinedTextField` (3), `Switch` (1); `ExposedDropdownMenu` + `PasswordVisualTransformation`
  not yet used (first use, no new dep) — HIGH
- `.planning/ROADMAP.md` Phases 10–13 success criteria + `.planning/PROJECT.md` §6.3 scope +
  root `CLAUDE.md` invariants — confirmed the "no OkHttp / no engine / prop-driven" gate — HIGH

---
*Stack research for: reusable Compose design-system — presentational AI-voice command UI*
*Researched: 2026-09-29*
