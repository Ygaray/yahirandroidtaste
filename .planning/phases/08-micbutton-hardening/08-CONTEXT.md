# Phase 8: MicButton hardening - Context

**Gathered:** 2026-09-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Make `MicButton` consumer-agnostic and callback-correct (CalTracker Phase-64 findings), all
backward-compatibly, ready for SecondBrain to adopt: parameterized microcopy with generic defaults,
latest-callback safety, hub-vocabulary KDoc, sensible param defaults. The single `pointerInput(Unit)`
gesture-ownership discipline is preserved byte-for-byte.

</domain>

<decisions>
## Implementation Decisions

### param-order
- **D-01 [param-order]:** Append the three content-description params after `modifier`, grouped disabled/tapToTalk/listening to mirror the `when` branches (ai-auto). Matches the hub's modifier-first convention (AttentionCue/SortControl); `enabled = true` before `onTap` is a legal default-before-nondefault ordering. Final: `MicButton(isListening, enabled = true, onTap, onDisabledTap = {}, modifier = Modifier, disabledDescription = "Microphone unavailable", tapToTalkDescription = "Tap to talk", listeningDescription = "Listening…")`.

### api-compat
- **D-02 [api-compat]:** Expect the added defaults + appended optionals to be an additive/source-compatible metalava change; regenerate + commit `api.txt` via `./gradlew apiDump`, and **confirm `apiCheck` green at execution** rather than assume — this AGP-9 / built-in-Kotlin stack has broken two other ABI validators, so the exact metalava verdict is confirmed empirically (ai-auto).

### test-design
- **D-03 [test-design]:** The WR-02 mid-press regression test hoists the callback in `mutableStateOf`, swaps to a **distinct** counter (tapA→tapB) between `down(center)` and `up()`, forces recomposition (`waitForIdle`), and asserts only the latest closure fired; it must go RED on current code and green after routing both callbacks through `rememberUpdatedState`. Guard against a vacuous pass (a same-behavior lambda swap would be unobservable — wrong) (ai-auto).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Milestone artifacts
- `.planning/ROADMAP.md` — Phase 8 goal + success criteria
- `.planning/REQUIREMENTS.md` — MICBTN-01, MICBTN-02, MICBTN-03
- `.planning/v2.0-DECISION-MAP.md` § Phase 8 — the resolved gray areas

### Design + invariants
- `docs/superpowers/specs/2026-09-26-hub-line-reunification-design.md` §5.4 — the four findings (WR-01/02, IN-01/02) with the RESOLVED generic-defaults decision
- `CLAUDE.md` (repo root) — one-way dependency, apiCheck, detekt zero-baseline

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `MicButtonGestureTest.kt` establishes the down / `waitForIdle` / up assertion pattern — extend it for the callback-identity test.

### Established Patterns
- `MicButton.kt`: `enabled` is already protected via `rememberUpdatedState` (line ~64) — apply the same to `onTap`/`onDisabledTap` (called directly at line ~100). Microcopy is hardcoded at lines 109-111; KDoc "config flip" at line ~86.

### Integration Points
- Preserve the single `pointerInput(Unit)` owner (KDoc lines 36-46, 57-63 — the voice-mic-press-no-capture + stuck-ripple discipline). Do NOT fold description strings into the `Crossfade` `targetState` (stays `Pair(enabled, isListening)`, line 107) — they live only in the content-lambda `Icon(contentDescription = …)`.
- CalTracker passes its own `disabledDescription = "Voice not set up — open Settings"` on repin; tapToTalk/listening keep generic defaults.

</code_context>

<specifics>
## Specific Ideas

All four fixes are additive/backward-compatible — existing call sites (CalTracker) compile unchanged.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 8-micbutton-hardening*
*Context gathered: 2026-09-26*
