# Phase 28: TalkBack sweep & Explorer demos (UI) - Context

**Gathered:** 2026-10-17
**Status:** Ready for planning
**Mode:** ai (every gray area took research's recommendation, Source: ai-auto; recommendations were set by the ui-designer seat that owns YAT)

<domain>
## Phase Boundary

TalkBack sweep & Explorer demos (UI) — milestone v2.8 (release v2.8.0, additive only). A11Y-02, DEMO-02, DEMO-03. Device work on the TESTER only (adb -s R5CT10XNKQN, device:tester lease); restore TalkBack off, wm density 450 (never reset), font scale. The hoisted sheetState hide() demo already exists (verify, don't rebuild).

</domain>

<decisions>
## Implementation Decisions

Locked milestone decisions D-01..D-17 live in `.planning/research/v2.8-BRIEF.md` and apply here unchanged. The phase decisions below are numbered P28-D-xx in prose but use the parser shape.

### scheme-pin
- **D-01 [scheme-pin]:** per-screen helper + source-scan guard + one TESTER screencap

### demo-stage
- **D-02 [demo-stage]:** fixed-height stage box; demo copy in ReleaseDemos.kt (guard-scanned)

### talkback-method
- **D-03 [talkback-method]:** two layers; park spoken-only items in HUMAN-UAT-PENDING (never waive); restore TalkBack off, wm density 450, font scale

### Claude's Discretion
- Internal naming, file split and test layout, within the hub conventions (CLAUDE.md, docs/STYLE.md).

</decisions>

<canonical_refs>
## Canonical References

- .planning/ROADMAP.md (§ Phase 28)
- .planning/REQUIREMENTS.md
- .planning/v2.8-DECISION-MAP.md (§ Phase 28) — source of the decisions below
- .planning/research/v2.8-BRIEF.md — LOCKED decisions D-01..D-17 (Yahir 2026-10-17 + designer D-13..D-17); they bind every phase
- .planning/research/SUMMARY.md, .planning/research/ARCHITECTURE.md, .planning/research/PITFALLS.md, .planning/research/STACK.md, .planning/research/FEATURES.md
- `docs/design/snackbar-go-to-item/index.html — the approved mock (direction A)`
- `docs/STYLE.md, docs/STYLE.REVERTED.md — the hub's accepted look and rejected ideas`
- `API.md, CLAUDE.md (source-compat rules, ComponentRegistry, detekt zero-baseline)`

</canonical_refs>

<code_context>
## Existing Code Insights

Cited per decision in `.planning/v2.8-DECISION-MAP.md` and the research files (feedback/*.kt, component/OutcomeSheet*.kt, feedback/UndoCenterScreen.kt, modifier/SwipeableActionRow.kt, explorer/*). Concrete file:line maps are surfaced at plan time.

</code_context>

<specifics>
## Specific Ideas

Match the approved mock (direction A) and docs/STYLE.md; Yahir's picks: keep the › chevron on openable cards, pause timers while touched or fanned.

</specifics>

<deferred>
## Deferred Ideas

999.4 voice-surface polish → v2.8.1; combined "Undo & open"; share/MCP deep links; SB sheet-swap blink (app-side).

</deferred>

## Runtime Decisions

- **POL-03 added (ui-designer, 2026-10-17, from teamlead@vae-bilingual / SB 999.111):** OSM user-agent foot-gun.
  - Probe from the host (curl 3/2/3.png):
    - Blocked (200 + `x-blocked`, 6987 B placeholder): `""`, and every `com.example.*` id in any form, for example `com.example.secondbrain/1.4.0 (Android 15)`.
    - Real tile: `com.ygaray.secondbrain`, bare or `/1.4.0 (Android 15)`.
  - So the fallback helps only real ids, and it must be loud otherwise.
  - Fix scope: internal only in `component/PlaceMapOsmdroidConfig.kt` (resolver + builder + one `Log.w`) and the `PlaceMapPicker` KDoc. No API change.
  - `x-blocked` response detection only if cheap (osmdroid's downloader is internal); otherwise it goes in the backlog.
  - Verification must re-run the curl probe with the exact resolved strings.
- **POL-03 scope change (2026-10-17, teamlead@vae-bilingual from SB 181.1 Gate-1 FAIL, verified by ui-designer in the osmdroid 6.1.20 bytecode):**
  - Why the current code fails:
    - `TileDownloader` checks `TileSourcePolicy.normalizesUserAgent()`. MAPNIK normalizes, so the `User-Agent` header is `Configuration.getNormalizedUserAgent()` (`mNormalizedUserAgent`, `<packageName>/<versionCode>`, for example `com.example.secondbrain/1`, which is blocked).
    - `userAgentValue` is ignored for MAPNIK, so today's `configureOsmdroid` setting of `userAgentValue` has NO effect on the wire.
    - `TileDownloader` then applies `getAdditionalHttpRequestProperties()` with `setRequestProperty` AFTER the UA header, so a `"User-Agent"` entry there wins.
  - Fix in `configureOsmdroid` (internal, no API change):
    - After `load()`, put the resolved caller UA into `Configuration.getInstance().additionalHttpRequestProperties["User-Agent"]`. Keep setting `userAgentValue` too, for non-normalizing sources.
    - SB compatibility: SB already sets the same map key app-side. Use the same header and key. If a non-blank `User-Agent` entry already exists, decide the precedence explicitly and pin it with a test. Proposed: the caller's `userAgent` param wins (it is the hub's documented input; SB passes the same value). Never add a second header.
    - Check whether `load()` clears `additionalHttpRequestProperties`, and set the entry after `load()` either way.
  - Keep the blank / `com.example` `Log.w`. Note that the fallback built from a `com.example` id is still blocked.
  - Stale cache: a blocked fetch re-stamps a 7-day expiry on the cached 6987-byte placeholder. SHOULD: when the resolved UA differs from the last one stored in the hub's osmdroid prefs, clear the tile cache once, so fixed apps don't show blocked tiles for a week. A cache purge is not a bulk download, so it is policy-safe.
  - Consumers: only SecondBrain uses PlaceMapPicker today (CalTracker checked: no use).
  - Verification MUST check the wire, not just a host curl:
    - On the TESTER, render the picker in the Explorer with a real-id UA, then pull the cached tile (or capture the request) and assert its md5/size is not the 6987-byte placeholder and that no `x-blocked` placeholder is cached.
    - Plus a unit/Robolectric test that `additionalHttpRequestProperties["User-Agent"]` equals the resolved UA after `configureOsmdroid`.
    - The host curl matrix stays as a supporting check.
- **POL-03 cache purge amended (teamlead, 2026-10-17):** do NOT key the purge on "resolved UA changed". A UA carries versionName, so every app update would wipe the cache and cause needless re-downloads, which the tile policy discourages. Supersedes the "clear the tile cache once" line above.
  - Purge only placeholder rows: cached tiles whose bytes match the blocked PNG (size 6987 B plus its md5, pinned as a constant from the probe).
  - When: once per install (flag in the hub's osmdroid prefs), and again whenever a placeholder is detected.
  - Same approach as SB's contingency, so the two don't conflict.
  - Placeholder fingerprint (probe 2026-10-17, blank UA, 3/2/3.png): 6987 bytes, md5 `c069a15b2cc2d6b6f527ad09eb93c61a`. Size AND md5 must match before a row is purged. If OSM changes the image, the purge just misses (fail-safe), and the Log.w still fires.
- **POL-03 load() detail (teamlead, from SB's javap check, 2026-10-17):** `DefaultConfigurationProvider.load()` clears `additionalHttpRequestProperties` on every non-first load, then refills it from the prefs keys `osmdroid.additionalHttpRequestProperty.*`. `configureOsmdroid` runs `load()` on every MapView creation, so:
  - Set `additionalHttpRequestProperties["User-Agent"]` in memory AFTER EVERY `load()`, inside `configureOsmdroid` on each call, never once per process.
  - The hub does NOT write the `osmdroid.additionalHttpRequestProperty.User-Agent` prefs key. SB persists that key in the hub's prefs file; leaving it alone keeps one owner per storage slot.
  - Precedence (pin with a Robolectric test): `load()` restores SB's persisted value, then the hub overwrites it in memory with the resolved caller `userAgent`. The caller param wins; SB passes the same string, so the wire value is identical either way.
  - Test cases: header present after a first and a second `configureOsmdroid` call; a pre-seeded prefs value is overwritten in memory by the caller UA; exactly one User-Agent entry.
- **POL-03 load() race (teamlead, SB 181.1 review WR-02, 2026-10-17):**
  - The problem: `load()` on every map creation clears and refills `additionalHttpRequestProperties`, a plain HashMap, while TileDownloader threads iterate it. Two consequences:
    - A tile fetched in that window goes out with the blocked normalized UA and caches a 7-day placeholder.
    - The map can throw `ConcurrentModificationException`.
  - Fix: `configureOsmdroid` calls `load()` at most ONCE per process (an `AtomicBoolean` / synchronized guard in the hub). The UA header is set right after that load, and is re-applied, without a reload, on later calls only if missing or different. No clear or refill ever happens while tiles are in flight.
  - The "after every load()" rule above still holds (there is now just one load).
  - Test: a second `configureOsmdroid` (second map creation) neither calls `load()` again nor clears the header, and the header is still the resolved UA.
  - SB's persisted prefs value is read by that first load, so precedence is unchanged.
  - Lands as a follow-up task in a later P28 wave or gap plan if 28-01 is already merged.
- **2026-10-17, ui-designer: C-3 / C-4 decided from Gate-1 evidence (28-07-SELF-UAT "C-3/C-4 evidence", evidence 116):**
  - **C-3 (oldest card's Undo order):** keep. The device shows each card's Undo child directly after its own card, in both the collapsed and the fanned deck. That keeps an action next to the item it acts on (D-11 honored). No `traversalBefore`/`traversalAfter` is set, and none is added. The cross-card order TalkBack speaks comes from `traversalIndex`, which the node dump can't show, so the spoken swipe order across cards is parked for Yahir's Gate-2 (never waived).
  - **C-4 (double Dismiss):** keep both forms (D-07). The system `ACTION_DISMISS` (1048576) serves TalkBack's dismiss gesture and generic services. The custom Dismiss is the localized entry in the actions menu ("Verwerfen" in German). The duplicate in the menu is cosmetic, and Gate-2 notes it for Yahir.
  - **New, fix in this phase: the collapsed deck's middle peek strip is a focusable node with an empty label, and TalkBack stops on it in silence (German evidence 97).** A silent stop is a defect. It must stop taking focus, with no label added, because every card is already a full merged node with Open/Undo/Dismiss (the P26 FA-1 ruling), so no TalkBack fan action is needed. The fix stays internal with no API change. It may touch FeedbackCard.kt/FeedbackStackHost.kt for this one node only. The locked card forms (D-11 Undo child plus custom action, D-07 both Dismiss forms) stay as they are. Lane 2 is allowed if the hook says so.
- **2026-10-18, ui-designer: rulings for gap cycle 2 (28-11) after the Gate-1 re-run (SELF-UAT addendum 73b5451):**
  - **C-5 (unnamed stops in the collapsed deck): label them, never hide them.** The collapsed back cards stay in the focus order: the P26 FA-1 ruling makes every card a full merged node with Open/Undo/Dismiss, and TalkBack has no fan action, so hiding them would cut TalkBack users off from those cards. Name the nodes from the data, not from what is drawn:
    - The card's merged node carries `item.message` (the same text the expanded card speaks).
    - The Undo child carries the caller's `undoLabel`.
    - A clipped or zero-height text layout must never leave a node unnamed.
    - Only the middle strip's empty hit area stays hidden (28-10).
  - **Unit test:** no focusable node in the collapsed or fanned deck has an empty name, in English or German.
  - **The fix is internal:** no API change, and the D-11/D-07 forms stay.
  - **C-1 route: (b), with (a) only as the fallback inside the same cycle.** When the sheet carrying the refusal settles, accessibility focus (not just input focus) moves to the refusal message node, so TalkBack reads it once. That is the standard pattern for a dialog that opens with an important message, and it needs no deprecated `announceForAccessibility`.
    - **If (b) is infeasible:** if research shows Compose cannot reliably move accessibility focus there, switch to (a): one deferred announcement after the sheet settles (re-trigger the live region with non-empty text). Record the choice.
    - **No double read:** never use both, so the message is never read twice.
    - **Event text:** the live-region node must still carry non-empty text (fix `text=[]`).
  - **Device acceptance (TalkBack alone, the real setup):**
    - The message is spoken on every showing, at least 5 of 5.
    - The observer shows the focus or announce event carrying the message text.
    - The message is never read twice.
  - **Cap:** this is cycle 2 of 3. If it fails, cycle 3 tries the other route, and a third failure escalates to Yahir.
