# Phase 6: Forward-port reunification - Context

**Gathered:** 2026-09-26
**Status:** Ready for planning

<domain>
## Phase Boundary

`main` gains the six v1.x-only components (`DateTimePicker`, the `PlaceMap*` cluster, `PresetChip`),
forward-ported net-additively from tag `v1.13.0` with their tests, registered in `ComponentRegistry`,
shown in the ExplorerActivity gallery, with `osmdroid` admitted and both drift guards + zero-baseline
detekt green. No behavior change to existing components; the ChipBar consolidation is preserved (no
standalone FilterBar).

</domain>

<decisions>
## Implementation Decisions

### tier
- **D-01 [tier]:** `DateTimePicker`, `PlaceMapPicker`, and `PresetChip` all register with `tier = PATTERN` (human). They bake in a composition/interaction convention, matching peers (AccentColorPicker/IconPickerGrid). NOTE: there is **no drift guard on `tier`** — a wrong value ships silently as a mislabeled gallery altitude badge, so this was confirmed deliberately, not defaulted.

### osmdroid
- **D-02 [osmdroid-pin]:** Pin `org.osmdroid:osmdroid-android` at `6.1.20` (implementation scope), verbatim from v1.13.0; **before the tag is cut, verify 6.1.20 is current / carries no open advisories** (it becomes a transitive runtime dep for every consumer); add an INTEGRATION.md/ECOSYSTEM.md note that a consumer rendering `PlaceMapPicker` must declare INTERNET itself (tiles cache to `cacheDir`, no storage permission) (human). Pre-approved in `.planning/APPROVED-DEPS.md`; also add to `CLAUDE.md` allowed-deps.

### map-render
- **D-03 [map-render]:** Rely on the hub's `configureOsmdroid` (cacheDir tiles, `userAgent = packageName`) for the live `MapView` gallery preview; confirm it actually renders **on-device at Gate-1** — it is device-only-verifiable, not resolvable from source (ai-auto).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone artifacts
- `.planning/ROADMAP.md` — Phase 6 goal + success criteria
- `.planning/REQUIREMENTS.md` — REUNI-01, REUNI-02, REUNI-03, REUNI-04
- `.planning/v2.0-DECISION-MAP.md` § Phase 6 — the resolved gray areas (source of these decisions)
- `.planning/APPROVED-DEPS.md` — osmdroid human-approval record + pre-tag currency action

### Design + invariants
- `docs/superpowers/specs/2026-09-26-hub-line-reunification-design.md` §2, §3.1, §4, §5.1–5.2 — the forward-port mechanics, dependency inventory, osmdroid wording, head-token dispositions
- `CLAUDE.md` (repo root) — reusability invariants, ComponentRegistry/drift-guard rules, toolchain, detekt zero-baseline

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Restore targets exist verbatim at `v1.13.0`: `src/main/.../component/{DateTimePicker,PlaceMapPicker,PlaceMapOsmdroidConfig,PlaceMapPickerModel,PresetChip}.kt`, `src/main/.../model/SavedPlaceUiModel.kt`, and 5 tests in `src/test/.../component/`.
- `theme/Dimens.kt` is **byte-identical** between v1.13.0 and main — every token the ported files use resolves.

### Established Patterns
- Head-token dispositions (spec §5.2): `Date`/`Preset` → `PRIMITIVE_NOUN_ALLOWLIST`, `Place` → `DOMAIN_VOCABULARY` (with rationale). `DomainVocabularyDriftGuardTest` is fail-loud — a missed disposition blocks the build, not a silent leak.

### Integration Points
- **`PlaceMapPicker` calls `ChipBar` (same package)** — the spec §3.1 under-stated this; the call site passes only `items/key/itemContent/testTag`, all still present on main's ChipBar (which merely appended `expandable`/`rawContent`), so it compiles clean. Plan should explicitly compile-verify this call site.
- **`PresetChip` is a public OVERLOAD PAIR** — both overloads must be ported or the gallery `content` fixtures won't compile; the drift guard dedupes them to one name.
- All 3 public composables register in exactly one family list; non-composable `PlaceMapPickerModel`/`PlaceMapOsmdroidConfig`/`SavedPlaceUiModel` helpers are `internal`/`private` → not registry- or vocab-tracked.
- `api.txt` is metalava-generated — regenerate additively via `./gradlew apiDump`, gated by `apiCheck`.

</code_context>

<specifics>
## Specific Ideas

Forward-port is a restore-from-`v1.13.0`, not a rewrite — keep the components byte-faithful to their v1.x form except where registration (tier) requires the new `Entry` ctor field.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 6-forward-port-reunification*
*Context gathered: 2026-09-26*
