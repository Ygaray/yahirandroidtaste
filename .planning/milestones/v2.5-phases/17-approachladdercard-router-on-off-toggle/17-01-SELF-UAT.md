---
status: complete
result: all_pass
gate: 1
phase: 17-approachladdercard-router-on-off-toggle
source: [17-ROADMAP success criteria SC1-SC5]
device: Samsung SM-S908U Galaxy S22 Ultra, tester rig (USB serial R5CT10XNKQN, Android 15 / SDK 35, 1080x2316 @ 450dpi)
apk: harness-debug.apk md5 2ec074296d3f4627346e6c5ee26f0681 over library AAR yahirandroidtaste-1.10.0.aar md5 ac172bfed07386fabb11a92f67f6f7de @ 5b0de39
run: 2026-10-05T11:40:00-06:00
---

<!-- Gate-1 agentic self-UAT, Phase 17 (plan 17-01). Platform: Android library, driver playbook
     /home/yahir/Projects/Reusable/android/yahirandroidtaste/AGENT-DEVICE-TESTING.md (project root). -->

# Self-UAT Log: Phase 17 Plan 01 (ApproachLadderCard Router ON/OFF toggle, VAPPR-04)

**Device:** tester rig R5CT10XNKQN (USB; wireless `:1496` also up)   **Run:** 2026-10-05
**Build identity:** git HEAD `5b0de39` (`git status --short src api.txt` empty before and after). Deleted
`~/.m2/.../yahirandroidtaste/1.10.0` + `maven-metadata-local.xml`, then
`./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q` -> AAR md5
`ac172bfed07386fabb11a92f67f6f7de`. Throwaway harness (session scratchpad, never committed; copied from the
Phase 16 harness minus its Phase 16 activities, plus a new `Phase17Activity`, source preserved as
`17-01-evidence/harness-Phase17Activity.kt.txt`), AGP 9.2.1, `compileSdk release(36){minorApiLevel=1}`,
`minSdk 35`, APK md5 `2ec074296d3f4627346e6c5ee26f0681`. The built APK's dex contains the
`approach_ladder_card_router_toggle` tag string (confirms the fresh AAR was resolved).
**Pre-flight (D1/D8, live probe):** `get-state=device`, `ro.build.version.sdk=35`, was Dozing (woken,
`wm dismiss-keyguard`, `svc power stayon true`), keyguard not showing, `~/.gsd/leases/` empty, no other tester
process. Reachable and free, so the real rig drove everything: no emulator fallback, nothing DEFERRED.
Personal phone never touched; no airplane mode. No recalled-memory contradiction (no `stale_target_memory`).
**Unit suite (rung 1, fresh on HEAD):** `./gradlew testDebugUnitTest --tests *ApproachLadderCardTest
--tests *ApproachLadderRouterCompatTest --tests *ApproachLadderCardGalleryDemoTest --tests *ComponentRegistry*
apiCheck --no-daemon -q` -> exit 0; result XML summed: 49 tests (ApproachLadderCardTest 33, RouterCompat 2,
GalleryDemo 3, ComponentRegistry* 11), 0 failures, 0 errors, 0 skipped. `apiCheck` (Metalava) exit 0.
**Seed/fixture integrity (D5):** no data layer. Two surfaces were driven. (1) The real Explorer gallery
(`ExplorerActivity` -> Voice Command -> ApproachLadderCard), whose fixtures hoist live state
(`ApproachLadderCardFixture` wrapper -> `ApproachLadderCardRouterFixture`, router starts OFF in every registry cell,
plus an appended "router on" Variants cell). (2) `Phase17Activity`, a direct-call seed (D5 fallback: the gallery
swallows `onRouterChange` into private state, so no UI path exposes the emitted values or a half-pair) with
readouts of every emitted boolean/cap/offline value, a null-pair card, a localized-label card, and `mode=1/2` half-pair
launches.

## Method

Cheapest layer first: unit tests (rung 1), then uiautomator trees on the real device (rung 4: `checkable`/`checked`
flags, content-desc "Router on, selected" etc., bounds for order), logcat for the throw (rung 3), and two downscaled
captures (rung 5) for the genuinely visual claim (toggle shown at rest, stacked under the offline toggle, light + dark).
Each override arm sits beside a baseline arm so "hidden when null" is falsifiable.

## Criteria

### 1. SC1 - Router toggle renders when both params are passed and reflects the boolean state (VAPPR-04)
result: passed
- **Rung:** 5 (rung 4 settled state/placement; two downscaled captures for the visual claim)
- **Target:** device
- **Expected:** both `router` (non-null) and `onRouterChange` set -> a Router ON/OFF segmented toggle is shown, selected segment mirrors the boolean (false = OFF selected, true = ON selected), below the offline toggle.
- **Arranged (seeded):** gallery fixtures (router=false in the Default / Pressed / Disabled cells (only Default driven), router=true in the appended "router on" cell); `Phase17Activity` mode 0 (router=false, hoisted state).
- **Did (drove):** navigated gallery -> Voice Command -> ApproachLadderCard; dumped the tree; scrolled to the end of the Variants; toggled light -> dark theme and captured.
- **Observed:** Default cell (`g1-default-initial.xml`): after the offline toggle `Online, selected | Offline only, not selected` comes `Router off, selected` [90,1271][541,1406] (checkable+CHECKED) and `Router on, not selected` [538,1271][990,1406]; ordering rungs -> offline toggle (y 1125) -> router toggle (y 1271), i.e. directly below the offline toggle. Appended "router on" cell (`g8-router-on-cell.xml`, `g8-router-on-cell-small.jpg` read): `Router on, selected` CHECKED, `Router off, not selected`; the capture shows two stacked segmented toggles (Online/Offline only over Router off/Router on) with a check mark on the selected segment, light theme; dark capture (`g9-dark-router-on-small.jpg` read) legible, selected segment filled with check mark. The Default cell shows router OFF selected, matching the router=false seed (falsifies an "always ON" or "state ignored" bug); the Pressed / Disabled cells were below the fold of that dump and are covered by the registry-reached unit test `ApproachLadderCardGalleryDemoTest`, not driven. The harness localized card shows `Enrutador NO` / `Enrutador SI, selected` for router=true (`h7-card-c.xml`), so the boolean maps to the second segment with caller-supplied labels too.
- **Evidence:** `17-01-evidence/g1-default-initial.xml`, `g8-router-on-cell.xml`, `g8-router-on-cell-small.jpg`, `g9-dark-router-on-small.jpg`, `h1-initial.xml`, `h7-card-c.xml`.

### 2. SC2 - Toggling the control invokes onRouterChange with the new boolean value (VAPPR-04)
result: passed
- **Rung:** 4 (harness readout of emitted values via uiautomator; gallery state change as second witness)
- **Target:** device
- **Expected:** tapping a segment emits the tapped segment's value; the card reflects the caller's new state.
- **Arranged (seeded):** `Phase17Activity` mode 0 with a readout `ROUTER-EMITS [...]` appending `T`/`F` on every call.
- **Did (drove):** tapped Router on, Router on again (already selected), Router off, Router on (real taps via `adb shell input tap`). Gallery: tapped Router on then Router off in the Default cell.
- **Observed:** harness `ROUTER-EMITS` went `[T,]` -> `[T,T,]` (re-tap of selected ON emits `true`, a target value, not a negation) -> `[T,T,F,]` -> `[T,T,F,T,]`, with `STATE router=true/true/false/true` tracking it (`h2`..`h5`). Gallery Default cell: after tapping Router on the tree shows `Router on, selected` CHECKED and `Router off, not selected` (`g2-after-tap-router-on.xml`); after Router off the reverse (`g4`); a re-tap of Router off leaves `Router off, selected` (`g5`). Mechanism: `ApproachLadderCardTest` "toggling router emits onRouterChange with the new value" + "tapping the already-selected router segment re-emits its own value..." re-run green.
- **Evidence:** `17-01-evidence/h2-tap-on.xml`, `h3-retap-on.xml`, `h4-tap-off.xml`, `h5-rung-local.xml`, `g2-after-tap-router-on.xml`, `g4-after-tap-router-off.xml`, `g5-retap-router-off.xml`.

### 3. SC3 - Null default renders no toggle and the card is unchanged (VAPPR-04)
result: passed
- **Rung:** 4 (tree absence + geometry), corroborated by unit tests; not a pixel diff against a pre-phase build
- **Target:** device
- **Expected:** `router`/`onRouterChange` both null -> no router toggle and no reserved space; existing callers unchanged.
- **Arranged (seeded):** gallery "every control hidden (null offlineOnly/cap props)" cell; combined-subdued cell (offline + cap but router null); harness CARD-B (offline + cap, router omitted).
- **Did (drove):** scrolled the gallery to those cells and dumped (`g7-hidden-cell.xml`, `g6-scrolled-a.xml`, `h1-initial.xml`).
- **Observed:** the hidden cell has only the three rung text nodes (`Cloud/Hybrid/Local` at 96 px pitch) and zero checkable nodes, no `Router ...` node. The combined-subdued cell and harness CARD-B end at the `Online`/`Offline only` toggle with NO `Router off`/`Router on` node and the card ends right after the offline toggle (CARD-B: last node bottom y=1924, next label y=1969 vs the router card whose toggle ends y=1137 + same padding), so no gap is reserved. Mechanism: unit tests "router toggle is not rendered when the pair is null", "router toggle appears and disappears as the pair flips", `ApproachLadderRouterCompatTest` (hidden v2.4.1 overload renders with router=null, no toggle) re-run green; `apiCheck` exit 0 vs the released v2.4.1 `api.txt`. Honest scope note: "byte-identical" is evidenced structurally (no node, same geometry) plus unit tests; no pre-phase APK was built for a pixel diff.
- **Evidence:** `17-01-evidence/g7-hidden-cell.xml`, `g6-scrolled-a.xml`, `h1-initial.xml`.

### 4. SC4 - Exactly one of the pair fails the require()-paired contract (VAPPR-04)
result: passed
- **Rung:** 3 (device logcat of the real throw) + 1 (unit tests, both directions)
- **Target:** device (harness direct-call) + headless unit tests. The gallery cannot construct a half pair (fixtures always pass both), so the device drive is via the harness `mode` extra.
- **Expected:** `router` non-null with `onRouterChange` null, and the reverse, each throw `IllegalArgumentException`; both-null and both-non-null do not.
- **Arranged (seeded):** `Phase17Activity` mode 1 (`router=true, onRouterChange=null`) and mode 2 (`router=null, onRouterChange={}`).
- **Did (drove):** `am start ... --ei mode 1`, `--ei mode 2` after `logcat -c`; read `logcat -d -b crash,main -s AndroidRuntime:E`; checked `pidof`.
- **Observed:** mode 1: `FATAL EXCEPTION: main ... java.lang.IllegalArgumentException: ApproachLadderCard: router and onRouterChange must both be null or both be non-null (got router=true, onRouterChange=null)`, process died. Mode 2: same exception with `(got router=null, onRouterChange=non-null)`, process died. Both-null (CARD-B, gallery hidden cell) and both-non-null (CARD-A) launched without a crash. Mechanism: unit tests "a non-null router with a null onRouterChange throws" and "a non-null onRouterChange with a null router throws" re-run green.
- **Evidence:** `17-01-evidence/i1-crash.txt`, `i2-crash.txt`.

### 5. SC5 - Row click still selects the tier cap; no per-rung navigation or gesture collision (VAPPR-04)
result: passed
- **Rung:** 4
- **Target:** device
- **Expected:** tapping a rung only selects the cap; it neither toggles the router nor navigates; toggling the router does not reorder rungs or change cap/Capped/Needs-network affordances.
- **Arranged (seeded):** gallery Default cell with router OFF/ON states reached by tapping; harness CARD-A with separate emit counters for cap, offline and router.
- **Did (drove):** gallery: with Router ON, tapped rung `Local` (500,1036); then Router off/on. Harness: with Router ON, tapped rung `Local`; then tapped `Offline only`.
- **Observed:** gallery after the Local tap (`g3-after-rung-local-tap.xml`): `Local` CHECKED (cap moved), Cloud and Hybrid now show `Capped`, router stays `Router on, selected`, offline unchanged, the screen is still the same detail page (no navigation), rung row bounds unchanged ([90,643],[90,806],[90,969]). Harness: `capEmits 0 -> 1` and `ROUTER-EMITS` unchanged (`[T,T,F,T,]`) on the rung tap (`h5`); `offlineEmits 0 -> 1` with router and cap emits unchanged on the offline tap, and with offline ON `Cloud` shows `Capped` + `Needs network`, `Hybrid` `Capped` while router remained ON and rung order Cloud/Hybrid/Local unchanged (`h6-offline.xml`). Router taps emitted only `onRouterChange` (cap emits stayed 0 through h2-h4). No tap collision: rows and toggle are separate nodes (rung nodes `[90,..][990,..]` end well above the toggle nodes at y>=1002 in the harness). Mechanism: unit tests "toggling router leaves rung order capped and needs-network affordances and cap selection unchanged" and "tapping a router segment emits only onRouterChange while rung and offline taps emit only their own callbacks" re-run green. Source check: the router branch (`ApproachLadderCard.kt:176-186`) adds only a `SegmentedOptionSelector`; no gesture/navigation construct.
- **Evidence:** `17-01-evidence/g3-after-rung-local-tap.xml`, `h5-rung-local.xml`, `h6-offline.xml`.

## Summary

total: 5
passed: 5
partial: 0
failed: 0
infra: 0

## Logs

`adb logcat -d -b crash` after the gallery + toggle drive on a fresh launch: 0 lines; `logcat -d *:E` filtered for
`yahirandroidtaste`/`AndroidRuntime`: 0 matches. The only exceptions in the run are the two deliberate SC4 half-pair
launches. Gallery arms and harness mode 0 ran with no crash.

## Notes / anomalies (for the Gate-2 reviewer)

- Gate-2 (human, taste/a11y) items that this tester cannot judge, registered in the fragment: (a) the visual call on two near-identical stacked segmented toggles (Online/Offline only over Router off/Router on): spacing, card height growth, selected-segment contrast, light and dark (captures read; both toggles share one style and are told apart only by label); (b) TalkBack listen: segments announce "Router on, selected" / "Router off, not selected" (confirmed present as content-desc on the merged node; the spoken form and the English state words are not audible to this tester).
- Selected segments report `clickable=false` in uiautomator (platform bridge of a checked radio), yet re-tapping the selected segment still emitted (`ROUTER-EMITS [T,T]`), so it is tappable.
- The gallery fixture hoists state internally, so the emitted value is witnessed only through the resulting selection; the exact emitted argument was proven in the harness readout and unit tests.
- Restore: harness force-stopped and uninstalled (`DELETE_FAILED_INTERNAL_ERROR` on the initial pre-install uninstall is the known harmless playbook D7 case), HOME pressed, `svc power stayon false` (`stay_on_while_plugged_in` read 0 at start and end). The device was Dozing at start and was left awake; no residual app state.

## Audit of prior verdicts

No prior Gate-1 log existed for Phase 17 (the pending fragment said "no self-UAT evidence exists yet").
`17-VERIFICATION.md` and `17-01-SUMMARY.md` were treated as claims; each SC was re-derived from the ROADMAP and
re-observed on the device. No contradiction found.

## Findings routed to gap-closure

None.

## Verdict

All 5 criteria PASS on the real device -> Gate-1 complete. Human Gate-2 (visual taste + TalkBack) deferred to milestone
completion, registered as `.planning/uat-pending/17-approachladdercard-router-on-off-toggle.md`.
