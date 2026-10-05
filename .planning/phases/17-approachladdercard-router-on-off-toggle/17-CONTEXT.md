# Phase 17: ApproachLadderCard Router ON/OFF toggle - Context

**Gathered:** 2026-10-05
**Status:** Ready for planning

<domain>
## Phase Boundary

Add a Router ON/OFF toggle to `ApproachLadderCard` as an additive `router: Boolean? = null` + `onRouterChange: ((Boolean) -> Unit)? = null` pair, `require()`-paired and `null`-hides, mirroring the existing `offlineOnly`/`onOfflineOnlyChange` shape. NOT per-rung navigation. Covers VAPPR-04.

</domain>

<decisions>
## Implementation Decisions

### router-copy
- **D-01 [router-copy]:** Mirror whatever Phase 15 settles for the offline toggle — if Phase 15 makes the offline-toggle labels caller-overridable (`onlineLabel`/`offlineOnlyLabel`), add matching `routerOnLabel`/`routerOffLabel` params so the card has no lone non-localizable toggle; otherwise hardcode. Default copy: "Router off" / "Router on". Resolution (human). _(provisional — refresh at execution; depends on Phase 15)_ — **Reversibility:** costly — adding the label params later is another api.txt append across the published signature.

### toggle-placement
- **D-02 [toggle-placement]:** Render the router `SegmentedOptionSelector` below the offline toggle (card bottom), and extend `VoiceCommandFamilyScreen`'s fixture to wire a `router` demo state. _(source: ai-auto)_

### binary-compat (LOCKED — orchestrator yahir-gsd-control-plane-3b ruling, 2026-10-05, F2 of the binary-compat fix plan)
- **D-03 [binary-compat]:** Never change a **tagged** public composable's signature in place. The binding rules for this phase:
  - **Append order:** `router: Boolean? = null`, `onRouterChange: ((Boolean) -> Unit)? = null`, `routerOnLabel: String = "Router on"` and `routerOffLabel: String = "Router off"` are appended AFTER the current last parameter (`offlineOnlyLabel`). This makes the change append-only at source level.
  - **The F1 shim stays:** `ApproachLadderCard`'s `@Deprecated(level = HIDDEN)` v2.4.1 overload (quick 261005-dmc, `07f66cf`) MUST keep its exact v2.4.1 parameter list. It MUST keep delegating to the current overload by **named** args, so the new router params take their defaults. Its `VoiceBinaryCompatShimTest` case MUST stay green, including the `$default`-mask render-through. Update the test only if the current signature it calls with all params changes.
  - **No shim for the Phase-15 shape:** the Phase-15 v2.5 shape is untagged (v2.5.0 is not cut). Only tagged signatures get a hidden delegate, so Phase 17 adds **no** new shim for it. Each published signature keeps exactly one hidden shim.
  - **Data classes (variant K, orchestrator ruling 2026-10-05):** `@JvmOverloads` alone only covers Java callers. A Kotlin caller compiled against a tagged shape links to the synthetic `(…, int, DefaultConstructorMarker)` ctor and to `copy$default`. To keep both:
    - Keep `@JvmOverloads` and the visible legacy `copy`.
    - Add a `@Deprecated(HIDDEN)` ctor declared with the tagged shape's exact synthetic params (`…, mask: Int, marker: DefaultConstructorMarker?`).
    - Add a private companion `@JvmStatic @JvmName("copy\$default")` shim.
    - Give each a per-class reflection test plus a behavioural test with a non-zero mask.
    
    K members are needed **only for TAGGED shapes**: add them at the release cut, not mid-milestone. A property this phase appends to a data class needs no K member for the untagged v2.5 shape. The v2.4.1 K members (quick 261005-e2e / F1c) must stay intact. The recipe is in the quick 261005-e2e SUMMARY.
  - **Gates:** Metalava `apiCheck` is the **source-level** gate only. The **binary** gate is the javap descriptor diff of the release AAR against the v2.4.1 JitPack AAR, with zero missing public descriptors (method in quick 261005-dmc/261005-e2e SUMMARY; it becomes `tools/verify-binary-abi.sh` at the v2.5.0 cut, F3). Run it as part of the phase's closing gate. _(source: orchestrator ruling — not revisitable by the planner)_

### Claude's Discretion
- Exact testTag name for the router toggle; the `index==1 → ON` mapping must match the offline-toggle convention.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Decision source
- `.planning/v2.5-DECISION-MAP.md` § Phase 17
- `.planning/cross-repo/RECONVENE-BRIEF-R-v1.1.md` (cb5f047) §0/§2 (e) — Router ON/OFF toggle, `offlineOnly` pattern, no per-rung nav

### Requirements / roadmap
- `.planning/REQUIREMENTS.md` — VAPPR-04
- `.planning/ROADMAP.md` § Phase 17

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `ApproachLadderCard.kt:64-67` (`offlineOnly`/`onOfflineOnlyChange` pair), `:75-83` (`require()` pairing guard), `:108-118` (offline `SegmentedOptionSelector` render) — the exact template to clone for the router pair.
- `SegmentedOptionSelector` (same package) — the two-option toggle widget (hard-requires exactly 2 options).

### Established Patterns
- `ApproachLadderCardTest.kt:167-188` — the pairing-symmetry tests that assert the offline pair throws on a half-pairing; a matching router pair of tests is expected.
- `ApproachLadderCard` is already catalog-registered (no new `ComponentRegistry` entry — a param addition, not a new composable).

### Integration Points
- Phases 15 and 16 both edit `ApproachLadderCard.kt` first (serialized) — clone the offline toggle in whatever final shape Phase 15 leaves it (this is the crux of D-01's provisional dependency).

</code_context>

<specifics>
## Specific Ideas

Default segment copy "Router off" / "Router on" unless the owner specifies otherwise.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 17-approachladdercard-router-on-off-toggle*
*Context gathered: 2026-10-05*

## Runtime Decisions

- **router-copy** (provisional, Depends-on Phase 15 — resolved at execute-milestone time, 2026-10-05, source: human/Yahir in-session): The router toggle's two segment labels are **caller-localizable overridable params** — add `routerOnLabel: String = "Router on"` and `routerOffLabel: String = "Router off"` to `ApproachLadderCard`, mirroring Phase 15's `offlineOnlyLabel`/`onlineLabel` pattern (those DID become overridable). Strictly additive; defaults are English.
