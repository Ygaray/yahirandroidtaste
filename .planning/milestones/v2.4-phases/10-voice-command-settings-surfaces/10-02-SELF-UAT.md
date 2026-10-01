---
status: complete
result: all_pass
gate: 1
phase: 10-voice-command-settings-surfaces
source: [10-ROADMAP.md success criteria]
device: Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN via USB adb, Android 15)
apk: yahirandroidtasteharness-debug.apk (md5 d187091da1a780fc242070a2b9b33c70) — throwaway same-package-Intent harness depending on com.github.Ygaray:yahirandroidtaste:1.10.0 published fresh to mavenLocal @ 78477f7
run: 2026-09-30T21:35:00Z
---

# Self-UAT Log — Phase 10 (Voice Command Settings Surfaces — Plans 01+02)

**Device:** Samsung SM-S908U / yahirs-s22-ultra-2 (`R5CT10XNKQN` via USB adb, Android 15) — the Gate-1 tester rig. Reachability live-probed (`adb -s R5CT10XNKQN get-state` → `device`) and leased (`gsd-lease.sh acquire device:R5CT10XNKQN`, owner `phase-10-3144468`) before driving; released at run end. No project-local `AGENT-DEVICE-TESTING.md` exists for this pure-library repo (same gap flagged in 01-05/06-03/07-01/08-01 SELF-UATs) — fell back to the global `AGENT-DEVICE-TESTING.md` template per `resolve_driver`.
**Library build:** Fresh HEAD `78477f7` (clean tree at `src/`, `build.gradle.kts`, `api.txt`; only `.planning/`-internal tooling files dirty, none phase-relevant) → `./gradlew clean publishReleasePublicationToMavenLocal` → `BUILD SUCCESSFUL`; old mavenLocal `1.10.0` artifact deleted first to rule out a stale-cache false pass. AAR md5 `3fcdeb908f1675863a838907569ec4eb`.
**Run:** 2026-09-30T21:35:00Z
**Schema:** N/A — no DB/data layer; `ExplorerActivity` reads only the static, compiled-in `ComponentRegistry` (`voiceCommandFamilyEntries`).
**Pre-flight:** device awake (`mWakefulness=Awake`); user-0 unlocked (`deviceLocked=0` on the "Yahir Garay" user-0 trust record; the co-present `deviceLocked=1` belongs to the unrelated Secure Folder profile, id=150, a known device quirk per `test-android.md`; `isKeyguardShowing=false` independently confirms).
**Unit suite:** re-run directly at HEAD `78477f7`, not trusted from `10-VERIFICATION.md`'s inherited verdict — `./gradlew testDebugUnitTest --tests "*ProviderKeyCard*" --tests "*ModelSelectCard*" --tests "*ApproachLadderCard*" --tests "*ComponentRegistryDriftGuardTest*" --tests "*DomainVocabularyDriftGuardTest*" --rerun-tasks` → `BUILD SUCCESSFUL`. JUnit XML: `ProviderKeyCardTest` tests="5" failures="0" errors="0"; `ModelSelectCardTest` tests="3" failures="0" errors="0"; `ApproachLadderCardTest` tests="7" failures="0" errors="0"; `ComponentRegistryDriftGuardTest` tests="1" failures="0" errors="0"; `DomainVocabularyDriftGuardTest` tests="2" failures="0" errors="0".
**Coverage/Nyquist:** not re-run standalone (out of this log's scope) — `10-VERIFICATION.md` already re-derived 13/13 truths; this log independently re-confirms the device-facing subset live rather than trusting that inherited verdict, and goes materially further than the prior audit by actually driving the interactive mechanisms (reveal toggle, provider dropdown, offline-only toggle, tap-a-rung cap) on the real running app rather than stopping at the unit-test rung.
**Seed/fixture integrity:** N/A — `ComponentRegistry`/`voiceCommandFamilyEntries` are static compiled-in catalogs (fixtures authored directly in `VoiceCommandFamilyScreen.kt`, drift-guard-denylisted `explorer/` package), no rows/fixtures to seed. Arrange work was entirely environmental: delete stale mavenLocal artifact, `publishReleasePublicationToMavenLocal` from clean HEAD `78477f7`, scaffold + build a throwaway harness app (`io.github.ygaray.yahirandroidtasteharness`, a same-package-Intent host whose sole `MainActivity` does `startActivity(Intent(this, ExplorerActivity::class.java)); finish()`) in this session's scratchpad (no prior scaffolded copy found from an earlier session this time), install + launch on `R5CT10XNKQN` only.

## Driver-mechanism note (carried forward from 01-05/06-03/07-01/08-01-SELF-UAT.md)

`yahirandroidtaste` is a pure `com.android.library` (no `applicationId`, `ExplorerActivity` declared `exported=false`) — still no project-local `AGENT-DEVICE-TESTING.md`. This run built a fresh throwaway harness from scratch (the prior session's scratchpad copy was not available in this session's scratchpad), following the same shape documented in `08-01-SELF-UAT.md`: `com.android.application` v9.2.1, `compileSdk { version = release(36) { minorApiLevel = 1 } }` (matching the library's AAR metadata requirement — a plain `compileSdk = 36` integer failed `checkDebugAarMetadata` with "requires compileSdk 36.1 or later," caught and fixed before any device work), `implementation("com.github.Ygaray:yahirandroidtaste:1.10.0")` resolved from `mavenLocal()`. Zero `FATAL`/`AndroidRuntime.*Exception` lines in `adb logcat -d` across the entire session (`grep -ciE "FATAL|AndroidRuntime.*Exception"` → `0`). The literal fixture key value (`sk-fixture-key-value`) never appears in logcat (`grep -c` → `0`), live-confirming INV-01's no-logging claim on the real running app, not just by source grep. Harness uninstalled at the end of the run (confirmed absent via `pm list packages --user 0`) and the device lease released — not committed, throwaway testing infra only, per the diagnose-only tester contract.

**Adversarial notes:**
- The gallery's "States" tab renders a 4-cell matrix under generic labels ("Default"/"Pressed / Selected"/"Disabled"/"Focused") that do NOT literally match each plan's own 4-cell naming (e.g. "Empty/Filled-hidden/Filled-revealed/Error") — this is the registry-wide generic label convention (also seen in `08-01-SELF-UAT.md`'s MicButton entry), not a defect; the per-cell *content* (fixture data) is what varies, not the tab labels.
- My first reveal-toggle tap landed on the "Default" cell, whose fixture key value is *empty* — tapping its reveal icon flipped `revealed` (content-desc `"Show key"` → `"Hide key"`) but produced no visible text change, which could misleadingly read as "reveal is broken." Re-targeted the "Pressed / Selected" cell (non-empty fixture, `sk-fixture-key-value`) and confirmed the masked-dots → raw-text swap is real and visual, not just a semantics-tree flag flip — this is exactly the kind of adversarial re-check the workflow calls for rather than accepting the first (ambiguous) result.
- "Voice Command" is the 10th family row and sits below the fold on the gallery index — required one scroll (`input swipe`) to become visible/tappable; a structure dump against the unscrolled screen alone would have falsely suggested the family wasn't registered. Confirmed it IS present after scrolling (`ComponentRegistryDriftGuardTest`/`DomainVocabularyDriftGuardTest` already proved this at the code level; this is the live on-screen confirmation).

## Criteria

### 1. A provider/API-key settings card renders provider selection and API-key entry from props + callbacks — holding no key and making no network call in the library (ROADMAP SC1, VSET-01)

result: passed

- **Rung:** 5 (visual capture) — decisive for the masking/reveal claim; a `uiautomator` text-node check alone was ambiguous on its own (see adversarial note above) and had to be settled by actually reading the screenshot pixels, consistent with the ladder's "a node's text/state ≠ the rendered claim" discipline for genuinely visual mechanisms. Supplemented by rung 1 (unit tests, callback-identity/logic) and rung 3 (logcat, no key leakage) for the non-visual sub-claims.
- **Target:** device (yahirs-s22-ultra-2, real hardware, via the throwaway harness).
- **Expected:** per ROADMAP SC1 + the plan's own deferred-to-Gate-1 verification note: the API-key field renders masked by default; tapping the reveal eye visibly un-masks it; provider selection and key edits emit via callbacks; the library holds/logs/transmits no key value.
- **Arranged (seeded):** none — static compiled-in registry fixtures (`ProviderKeyCard` "Pressed / Selected" states-cell fixture: provider "OpenAI", key `sk-fixture-key-value`). Arrange was environmental only (harness build/install, see driver-mechanism note).
- **Did (drove):** Launched the harness → scrolled to and tapped "Voice Command" family → tapped `ProviderKeyCard` entry. Dumped the hierarchy (masked dots visible, `content-desc="Show key"`). Tapped the reveal eye on the "Pressed / Selected" cell (bounds center `788,1508`). Re-dumped + screenshotted. Tapped the Provider dropdown on the "Default" cell (center `540,720`), confirmed the menu opened with `OpenAI`/`Anthropic`/`Local (offline)`, tapped `Anthropic`.
- **Observed:** Before the reveal tap, the key field's accessibility text was literally `"••••••••••••••••••••"` (20 masked glyphs) with `content-desc="Show key"` — the raw value was NOT present anywhere in the tree. After the tap, `content-desc` flipped to `"Hide key"` and the accessibility text became the literal raw fixture value `"sk-fixture-key-value"` — confirmed identically in the screenshot pixels (`shot2-crop-small.png`: plain readable "sk-fixture-key-value" text where the dots were). The Default cell's Provider control, independently, went from showing `"OpenAI"` to showing `"Anthropic"` immediately after tapping the Anthropic menu item — live `onProviderSelected` callback confirmed on the real running app (the other, untouched cells stayed `"OpenAI"`, confirming per-instance state isolation, not a global side effect). `adb logcat -d` across the whole session: 0 FATAL/Exception lines; 0 occurrences of the literal key value `sk-fixture-key-value` anywhere in the log (INV-01 no-logging, live-confirmed). `ProviderKeyCardTest` 5/5 fresh at HEAD `78477f7` (callback-identity/logic rung).
- **Evidence:** `ui4-providerkeycard.xml` (pre-reveal, masked dots), `shot1-crop-small.png` (pre-reveal screenshot — Default cell empty, Pressed/Selected cell shows dots), `ui6-revealed2.xml` (post-reveal, raw text in tree), `shot2-crop-small.png` (post-reveal screenshot — raw `sk-fixture-key-value` visible), `ui19-provider-selected.xml` (Default cell now `Anthropic`); `build/test-results/testDebugUnitTest/TEST-...ProviderKeyCardTest.xml` (this run, 5/5); logcat capture (0 FATAL, 0 key-value leaks, this run). Deferred (Gate-2, per existing standing policy, unchanged by this run): rendered-pixel contrast/readability quality judgment in both light AND dark theme — `10-UAT.md`/`10-VERIFICATION.md` item 1.

### 2. A model settings card renders the available/selected model(s) from props and emits the selection via callback (ROADMAP SC2, VSET-02)

result: passed

- **Rung:** 4 (UI structure tree) — decisive; supplemented with rung 5 (screenshot) to falsify the specific "never a blank control" must-have, which a text-presence check alone cannot distinguish from a genuinely blank/disabled-looking control.
- **Target:** device (yahirs-s22-ultra-2, real hardware).
- **Expected:** per ROADMAP SC2: available/selected models render from props; selecting a model emits via callback; an empty `models` list renders a disabled state carrying a reason string, never a blank control.
- **Arranged (seeded):** none — static compiled-in `ModelSelectCard` states fixtures (Default: "GPT-4"; Pressed/Selected: "Local Llama (offline)"; Disabled: empty models + reason "Set a provider and key first").
- **Did (drove):** From the Voice Command family screen, tapped `ModelSelectCard`. Dumped the hierarchy and screenshotted the full States matrix.
- **Observed:** Default cell shows `"GPT-4"` in the model dropdown; Pressed/Selected cell shows `"Local Llama (offline)"` — both rendered directly from the fixture's model list/selection, not hardcoded. The Disabled cell (empty `models`) renders a distinctly-styled caption `"Set a provider and key first"` in place of the dropdown — confirmed visually in `shot3-crop-small.png`: it is a plain, readable, caption-toned line of text inside the card surface, NOT a blank/greyed dropdown shell and not error-red styling, matching the must-have exactly.
- **Evidence:** `ui8-modelselect.xml`; `shot3-crop-small.png` (full States matrix: Default/Pressed-Selected/Disabled/Focused cells); `ModelSelectCardTest` 3/3 fresh at HEAD `78477f7` (callback logic rung).

### 3. A command-approach settings card displays the configured tier ladder (ordered approaches) from props (ROADMAP SC3, VAPPR-01)

result: passed

- **Rung:** 1 (unit test) — decisive for the "list order, not re-sorted" claim specifically: `ApproachLadderCardTest`'s own fixture is deliberately unsorted by rank to make this claim falsifiable, while the gallery's own demo fixture (Cloud/Hybrid/Local) happens to coincide with alphabetical order too, so a device-only check could not by itself distinguish "preserved list order" from "coincidentally-alphabetical resort" — the cheaper rung already decisively settles this per the ladder rule. Supplemented with rung 4 (device) to confirm the ladder renders live, in the same order, across every States/Variants cell.
- **Target:** headless (unit test, decisive) + device (live-render confirmation).
- **Expected:** per ROADMAP SC3: the ladder renders in the caller-supplied list order (e.g. Grammar → SingleShot → Plan → Agentic in a real consumer's props), not re-sorted by any internal field.
- **Arranged (seeded):** none — static compiled-in `ApproachLadderCard` fixtures (`Cloud`/`Hybrid`/`Local` rungs across 3 states-cells + 2 variant cells).
- **Did (drove):** `ApproachLadderCardTest` re-run fresh at HEAD `78477f7` (rung 1, decisive). On-device: tapped `ApproachLadderCard` from the family screen, dumped every States/Variants cell's rung order by y-coordinate.
- **Observed:** `ApproachLadderCardTest` "renders each ladder rung label in list order" — 7/7 pass, 0 failures/errors, fresh run (uses a deliberately-unsorted fixture, per `10-VERIFICATION.md` Truth 8). On-device, all 5 rendered cells (3 States + 2 Variants) consistently show `Cloud, Hybrid, Local` top-to-bottom, matching the list order supplied to each cell's fixture.
- **Evidence:** `build/test-results/testDebugUnitTest/TEST-...ApproachLadderCardTest.xml` (this run, 7/7); `ui10-approachladder.xml`, `ui11-scrolled.xml`, `ui12-scrolled2.xml`, `ui15-cap-tapped.xml` (rung order consistent across all cells, this run).

### 4. The command-approach card's offline-only toggle reflects and emits offline-only state via props + callback (ROADMAP SC4, VAPPR-02)

result: passed

- **Rung:** 4 (UI structure tree) — decisive; the live state change (a rung acquiring a "Needs network" tag immediately after a real tap) is a presence/text claim, fully settled without a screenshot; supplemented with rung 5 to also confirm the toggle's own checked-state visual (filled/checkmarked "Offline only" segment) for completeness.
- **Target:** device (yahirs-s22-ultra-2, real hardware).
- **Expected:** per ROADMAP SC4 + VAPPR-02: toggling offline-only emits `onOfflineOnlyChange`; a non-offline-capable rung then shows "Needs network" while staying visible; the control is absent (not shown-disabled) when its prop pair is null.
- **Arranged (seeded):** none — static compiled-in fixtures; the "Default" states-cell starts `offlineOnly = false`/Online selected, no "Needs network" tag present.
- **Did (drove):** On the Default cell, tapped the "Offline only" segment of the Online/Offline-only toggle (bounds center `764,991`). Re-dumped the hierarchy.
- **Observed:** Before the tap: `Cloud`/`Hybrid`/`Local` render with no "Needs network" tag, toggle shows "Online" selected. Immediately after the tap: `Cloud` (the non-offline-capable rung in this fixture) gained a `"Needs network"` tag in the SAME row, while remaining fully present in the list (not removed) — this is a genuine live state change driven by a real tap through the real UI, not a pre-baked fixture difference (the "before" and "after" dumps are the identical cell, before/after the tap). The null-prop "every control hidden" Variant cell (see Criterion 5 below) independently confirms the toggle is entirely absent — no row, no placeholder — when `offlineOnly`/`onOfflineOnlyChange` are null.
- **Evidence:** `ui13-top.xml` (pre-tap: Default cell, no "Needs network" tag), `ui14-offline-toggled.xml` (post-tap: same cell, "Needs network" now present on Cloud); `shot6-crop-small.png` (screenshot, post-tap, showing the checkmarked "Offline only" segment); `ApproachLadderCardTest` 7/7 (includes the offline-only emit + hidden-when-null sub-tests, fresh this run).

### 5. The command-approach card's max-tier cap control reflects and emits the cap via props + callback (ROADMAP SC5, VAPPR-03)

result: passed

- **Rung:** 5 (visual capture) — decisive for the "greyed-but-present, not disabled-looking" and "absent, not disabled-looking when null" must-haves, both genuinely visual judgments the structure tree's text/tag presence alone cannot settle (a tag node existing doesn't prove the sibling rung is visually de-emphasized rather than, say, styled identically). Supplemented by rung 4 for the live tap-a-rung emit confirmation.
- **Target:** device (yahirs-s22-ultra-2, real hardware).
- **Expected:** per ROADMAP SC5 + VAPPR-03: tapping a rung emits `onMaxTierChange`; rungs above the cap render greyed-but-present (not removed); the cap control is entirely absent (not shown-disabled) when its prop pair is null.
- **Arranged (seeded):** none — static compiled-in fixtures (Default cell starts uncapped; Disabled cell starts pre-capped at "Local" — Cloud+Hybrid shown "Capped"; a dedicated Variants cell titled "ApproachLadderCard — every control hidden (null offlineOnly/cap props)" ships with both props null).
- **Did (drove):** On the Default cell (already offline-only-toggled from Criterion 4, same live session), tapped the "Hybrid" rung row (bounds center `540,769`) to set the cap there. Re-dumped + screenshotted. Independently, scrolled to and screenshotted the "every control hidden" Variants cell.
- **Observed:** Immediately after tapping "Hybrid": `Cloud` (the rung above Hybrid) gained a `"Capped"` tag alongside its pre-existing `"Needs network"` tag, while remaining fully present in the list — a genuine live `onMaxTierChange` emit confirmed by the tap producing an immediate UI change. The screenshot (`shot6-crop-small.png`) confirms this is a real *visual* de-emphasis, not just a tag: `Cloud`'s label renders in a visibly lighter/greyed tone versus the bold, full-opacity `Hybrid`/`Local` labels below it — satisfying "greyed-but-present," not merely "tagged." Separately, the pre-capped "Disabled" states-cell shows BOTH `Cloud` and `Hybrid` tagged `"Capped"` (only `Local`, at/below the cap, untagged) — confirming the cap boundary semantics (rungs strictly above the cap render capped; the capped-to rung and below do not) on a second, independently-seeded fixture. The "every control hidden" Variants cell (screenshot `shot5-crop-small.png`) shows `Cloud`/`Hybrid`/`Local` at full, uniform opacity with **zero** toggle row and **zero** "Capped"/"Needs network" tags anywhere in the card — directly falsifying the concern that a null-prop card might render a disabled-looking (greyed) control instead of no control at all; it is unambiguously absent, not disabled-looking.
- **Evidence:** `ui13-top.xml` (pre-tap Default cell, no Capped tag), `ui15-cap-tapped.xml` (post-tap, Cloud shows Capped+Needs network), `shot6-crop-small.png` (visual: Cloud visibly lighter-toned vs. bold Hybrid/Local); `ui10-approachladder.xml` / `shot4-crop-small.png` (Disabled cell, pre-capped, Cloud+Hybrid tagged, Local not); `ui11-scrolled.xml`/`ui12-scrolled2.xml` + `shot5-crop-small.png` (null-props Variants cell: no toggle, no cap tags, uniform opacity); `ApproachLadderCardTest` 7/7 (cap-emit + hidden-when-null sub-tests, fresh this run).

## Summary

total: 5
passed: 5
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **Two visual/interaction-*feel* items remain correctly deferred to milestone v2.4 Gate-2**, per the existing, unchanged disposition in `10-UAT.md`/`10-VERIFICATION.md` (standing policy "Gallery Gate-1 visual review deferred to milestone-close (Yahir)"; orchestrator `yahir-gsd-control-plane-f2` ruling, 2026-09-30): (1) masked-key reveal affordance **contrast/readability quality** in both light AND dark theme, and (2) tap-a-rung cap **interaction feel**/discoverability plus the hidden-vs-shown-disabled **layout quality** judgment, both themes. This run does NOT re-litigate that disposition — it is a legitimate subjective-quality judgment, not a mechanism question. What this run adds beyond the prior audit: it independently, adversarially confirmed on the real running app (not Robolectric, not source-reading) that the underlying *mechanisms* those two deferred items sit on top of are genuinely functional — masking hides the real key, the reveal toggle genuinely swaps to the raw characters (both in the accessibility tree AND the rendered screenshot pixels), the tap-a-rung cap genuinely fires a live `onMaxTierChange` that visibly greys the affected rung while keeping it present, and the null-prop variant genuinely renders zero controls rather than a disabled-looking one. Only the "is the contrast/feel *good*" judgment remains for the human — not "does it work at all," which was the more fundamental risk an inherited-VERIFICATION rubber-stamp could have missed.
- **Adversarial catch, resolved without a defect:** the first reveal-toggle tap landed on a cell with an *empty* key fixture, producing a content-desc flip with no visible text change — which in isolation could misleadingly read as "reveal doesn't work." Re-targeting the non-empty "Pressed / Selected" cell and re-observing (both tree text AND screenshot pixels) resolved this decisively in the mechanism's favor; documented above and in the driver-mechanism note so a future re-run doesn't repeat the same false lead.
- **The gallery's States/Variants demo cells for these three new cards ARE live composables with real local state** (unlike `08-01-SELF-UAT.md`'s static no-op `MicButton` cells) — every interaction driven in this run (reveal toggle, provider dropdown, offline-only toggle, tap-a-rung cap) produced a genuine, observable, real-time UI change on the actual running app, which is stronger evidence than the callback-identity-only confirmation some earlier phases' galleries could offer.
- No project-local `AGENT-DEVICE-TESTING.md` exists yet for this pure-library repo (same gap flagged in four prior SELF-UAT logs). The harness had to be rebuilt from scratch this session (no scaffolded copy carried over); the backlog item `999.1-formalize-reusable-gate-2-visualization-harness-apk` (currently an empty placeholder phase) remains a good investment to stop re-paying this cost every phase.
- The harness app and its build artifacts are **not committed** — throwaway UAT infra only, per the diagnose-only tester contract. Uninstalled from the device at run end (`pm list packages --user 0` confirms absence); the device lease was released; the personal phone (`100.126.94.47`) was never targeted.

## Findings routed to gap-closure (if any)

None — all 5 ROADMAP success criteria are genuine PASSes, each independently re-derived from the ROADMAP text (not from `10-UAT.md`/`10-VERIFICATION.md`'s prior claims) and confirmed by actually driving the real running app: live provider selection, live masked-reveal (tree + pixels), live empty-state-not-blank rendering, live offline-only toggle emit, and live tap-a-rung cap emit with genuine greyed-but-present / absent-not-disabled-looking visual confirmation.

## Verdict

All 5 criteria PASS → Gate-1 complete; the 2 visual/interaction-*feel* items remain correctly deferred to milestone v2.4 Gate-2 per the existing, unchanged standing-policy disposition (not re-litigated by this run). Registered in `.planning/uat-pending/10-voice-command-settings-surfaces.md` → `HUMAN-UAT-PENDING.md`.

---

## Addendum — 2026-10-01 re-verify re-drive (source materially changed since the 2026-09-30 run above)

**Why this addendum exists:** source changed materially since the run above (`78477f7`): Phase 11 landed `maxTierId`/`onMaxTierChange` pairing enforcement as a `require()` throw, and this phase's own code-review re-drive added (WR-01) a symmetric `require()` guard for `offlineOnly`/`onOfflineOnlyChange`, (WR-02) a new "Unavailable" affordance for `enabled = false` rungs, and (WR-03) a defaulted `emptyProvidersReason` param on `ProviderKeyCard`. Per this workflow's own instruction, the prior `all_pass` verdict above is a claim to audit, not a fact — this addendum re-derives each affected criterion from the ROADMAP text and re-observes on the real running app at the new HEAD rather than trusting the inherited log.

**Build identity (this run):** HEAD `b3fe6e7` (`git status --short -- src/ build.gradle.kts api.txt` clean at the time of this run). Old mavenLocal `1.10.0` artifact deleted; `./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q` → BUILD SUCCESSFUL. AAR md5 `ec4ab0a2221d7489700ac97714fc35f1`.

**Device:** same rig, `R5CT10XNKQN` (Samsung SM-S908U, Android 15). Live-probed (`adb -s R5CT10XNKQN get-state` → `device`, both USB serial and tailnet `100.118.21.106:1496` listed in `adb devices`), leased (`gsd-lease.sh acquire device:R5CT10XNKQN`, owner `phase-10-1079787`), confirmed awake (`mWakefulness=Awake`) and unlocked (`deviceLocked=0` on the real user-0 trust record, `isKeyguardShowing=false`; the co-present `deviceLocked=1` is the unrelated Secure Folder profile, a known device quirk). Released at run end (`gsd-lease.sh release`, confirmed `RELEASED`).

**Harness:** reused this session's existing scratchpad harness scaffold (`com.android.application`, AGP 9.2.1, `compileSdk { release(36) { minorApiLevel = 1 } }`, `minSdk 35`), rebuilt against the fresh `1.10.0` AAR. **Extended this run** with a second Activity, `FixtureActivity` (Compose added to the harness solely to host it), which directly instantiates `ApproachLadderCard` with a hand-crafted ladder — this is a D5 **programmatic-seed fallback**, not a UI-driven Arrange: no gallery fixture (`VoiceCommandFamilyScreen.kt`'s `fixtureLadder`) constructs a rung with `enabled = false`, so there is no UI path from the static compiled-in registry fixtures to the combined-affordance state the re-verify needed to exercise. `pm uninstall` confirmed the harness absent at run end (`pm list packages --user 0`, no match).

**Unit suite (fresh at HEAD `b3fe6e7`, `--rerun-tasks`):** `ApproachLadderCardTest` 12/12 (grew from 7 → 12: the two new symmetric `require()`-pairing tests for `offlineOnly`/`onOfflineOnlyChange`, plus the new "a disabled rung renders greyed-but-present with an Unavailable affordance" test), `ProviderKeyCardTest` 6/6 (grew from 5 → 6, the new `emptyProvidersReason` test), `ModelSelectCardTest` 3/3, `ComponentRegistryDriftGuardTest` 1/1, `DomainVocabularyDriftGuardTest` 2/2 — all green, 0 failures/errors.

### Re-verified: Criterion 4 (VAPPR-02, offline-only toggle) and Criterion 5 (VAPPR-03, max-tier cap) at new HEAD

result: passed (re-confirmed)

- **Rung:** 4 (UI structure tree), decisive — re-ran the same live interaction from the original run (tap "Offline only" → tap the "Hybrid" rung) against the gallery's default `ApproachLadderCard` cell.
- **Did (drove):** Voice Command family → `ApproachLadderCard` → Default cell: tapped "Offline only" segment (`765,991`); re-dumped (Cloud gained "Needs network", correct — `offlineCapable = false`). Tapped "Hybrid" rung (`540,753`) to set the cap there; re-dumped (Cloud now shows BOTH "Capped" AND "Needs network" simultaneously, Hybrid/Local unaffected).
- **Observed:** Both live emits (`onOfflineOnlyChange`, `onMaxTierChange`) still fire correctly and visibly on the real gallery card at the new HEAD — the `require()` additions did not regress the normal (paired-prop) path, as expected since the gallery's own fixture (`ApproachLadderCardFixture`) always supplies both props of each pair together. No regression.
- **Evidence:** `redrive-ladder-default.xml`, `redrive-ladder-offline-toggled.xml`, `redrive-ladder-cap-tapped.xml` (`10-02-SELF-UAT-evidence-2026-10-01/`).

### New: the WR-02 "Unavailable" affordance renders live, and co-exists structurally with "Capped"/"Needs network" on the same rung (ROADMAP SC5 / VAPPR-03, expanded scope)

result: passed

- **Rung:** 4 (UI structure tree) — decisive for the FUNCTIONAL claim this addendum targets ("does it render, structurally, when three affordances co-occur on one rung"). The *aesthetic* legibility/contrast judgment of three co-occurring subdued labels remains correctly deferred to milestone v2.4 Gate-2 per `10-VERIFICATION.md`/`10-UAT.md`'s explicit, already-recorded scope expansion — this addendum does not re-litigate that deferral, only confirms the underlying mechanism is genuinely functional (every label text node is actually present, none is dropped/overwritten when stacked).
- **Target:** device (yahirs-s22-ultra-2, real hardware), via the `FixtureActivity` D5 programmatic-seed path (no gallery fixture constructs a disabled rung — see Harness note above).
- **Expected:** per `10-VERIFICATION.md`'s explicit re-scope: "confirm a rung with `enabled = false` shows the new 'Unavailable' label legibly alongside the other two affordances" — i.e., when a rung is simultaneously `enabled = false`, above the max-tier cap, AND offline-incapable while `offlineOnly = true`, all three subdued labels ("Unavailable", "Capped", "Needs network") must render — none silently suppressed/overwritten by the others.
- **Arranged (seeded):** a hand-crafted ladder passed directly to `ApproachLadderCard` via `FixtureActivity` (D5 programmatic seed, no UI path exists for this combination): `Cloud` (`id="cloud"`, `rank=3`, `enabled=false`, `offlineCapable=false`), `Hybrid` (`rank=2`, `enabled=true`, `offlineCapable=true`), `Local` (`rank=1`, `enabled=true`, `offlineCapable=true`); card instantiated with `offlineOnly=true`, `maxTierId="local"` — so `Cloud` is simultaneously disabled, above the cap, and needs-network.
- **Did (drove):** Launched `FixtureActivity` directly (`am start -n io.github.ygaray.yahirandroidtasteharness/.FixtureActivity`); dumped the hierarchy.
- **Observed:** The `Cloud` row's text nodes, in order, are exactly `"Cloud"`, `"Unavailable"`, `"Capped"`, `"Needs network"` — all three affordance labels present simultaneously on the one ineffective rung, each a distinct, intact text node (none merged/overwritten/dropped). `Hybrid` (enabled, offline-capable, above the cap) shows only `"Capped"` — correctly NOT "Unavailable" (it IS enabled) and NOT "Needs network" (it IS offline-capable), confirming the three conditions are independently gated, not conflated. `Local` (at the cap, enabled, offline-capable) shows no affordance at all — the fully-effective rung. This decisively proves the WR-02 mechanism: `ApproachLadderCard.kt:155-176`'s three `if` blocks (`!rung.enabled` → "Unavailable", `isCapped` → "Capped", `needsNetwork` → "Needs network") are independent and additive, not mutually exclusive, and none silently wins over the others when stacked on one rung. A supplementary screenshot was captured but a harness-only cosmetic issue (this throwaway `FixtureActivity` renders no system-bar insets padding, so its first rung is partly obscured by the status bar/title bar) made it non-decisive for a pixel read — not a library defect, since the decisive claim here is structural presence, already settled at rung 4, and the genuinely visual contrast/legibility judgment is the explicitly-deferred Gate-2 item, not this addendum's job.
- **Evidence:** `redrive-fixture-combined-affordances.xml` (`10-02-SELF-UAT-evidence-2026-10-01/`) — `Cloud` row's text nodes `Cloud`/`Unavailable`/`Capped`/`Needs network` all present; `redrive-fixture-combined-affordances-shot.png` (supplementary, non-decisive per above); `ApproachLadderCardTest`'s new "a disabled rung renders greyed-but-present with an Unavailable affordance" test, 1/1 fresh at HEAD `b3fe6e7` (headless rung, confirms the single-affordance case; this device pass extends it to the triple-stacked case the unit suite does not cover).

### Re-verified: Criterion 1 (VSET-01, ProviderKeyCard) at new HEAD, including the new `emptyProvidersReason` default

result: passed (re-confirmed)

- **Rung:** 5 (visual capture) — decisive for the masking/reveal claim, same as the original run; re-captured independently rather than trusting the prior screenshot.
- **Did (drove):** Fresh navigation (relaunch → index → Voice Command → ProviderKeyCard, to rule out any stale-scroll-position false lead from an earlier fumbled attempt this session — see note below). Screenshotted the Pressed/Selected cell's key field BEFORE any tap (masked dots + "Show key" + "Clear text" visible). Tapped the reveal eye (`788,1508`). Screenshotted again.
- **Observed:** Pre-tap: `••••••••••••••••••••` masked dots, eye icon = "Show key". Post-tap: `content-desc` flips to "Hide key" (confirmed via tree dump) AND the screenshot shows the literal raw text `sk-fixture-key-value` in the field — re-confirms the original run's finding at the new HEAD, byte-for-byte the same mechanism, unaffected by this phase's `require()`/`Unavailable`/`emptyProvidersReason` changes (none of which touch `ProviderKeyCard`'s reveal path). Provider dropdown on the Default cell: tapped to open (menu showed `OpenAI`/`Anthropic`/`Local (offline)`), tapped `Anthropic` — Default cell's dropdown updated to `"Anthropic"` live, while the untouched Pressed/Selected and Focused cells stayed `"OpenAI"` (per-instance isolation, re-confirmed). `emptyProvidersReason`'s new defaulted param produces no visible behavior change for any gallery fixture (all pass non-empty `providers` lists), consistent with its additive, backward-compatible contract — confirmed by `ProviderKeyCardTest` 6/6 (including the new empty-providers test, headless) rather than needing a device-level empty-providers demo (none exists in the gallery, and none is required — `ModelSelectCard`'s identical convention was already device-confirmed in the original run above).
- **Adversarial note (process, not a defect):** mid-session, an earlier attempt at this same re-check landed on a transient blank-field read after a missed-then-retried tap sequence left the page in an inconsistent scroll/focus state (two stacked taps at stale coordinates from an earlier dump). A clean relaunch + fresh navigation + a single precise tap reproduced the correct masked→raw swap cleanly, matching the original run's finding exactly — recorded here so a future re-run doesn't mistake a self-inflicted stale-coordinate artifact for a reveal-mechanism regression.
- **Evidence:** `redrive-pkc-pre-reveal.xml`/`redrive-pkc-pre-reveal-shot.png`, `redrive-pkc-post-reveal.xml`/`redrive-pkc-post-reveal-shot.png`, `redrive-provider-dropdown-open.xml`, `redrive-provider-selected-anthropic.xml` (`10-02-SELF-UAT-evidence-2026-10-01/`); `ProviderKeyCardTest` 6/6 fresh at HEAD `b3fe6e7`; `redrive-logcat.txt` (0 FATAL/Exception, 0 occurrences of `sk-fixture-key-value`, this run).

### Re-verified: Criterion 2 (VSET-02, ModelSelectCard) at new HEAD

result: passed (re-confirmed)

- **Rung:** 4 (UI structure tree), decisive — no code in this diff touches `ModelSelectCard`; re-confirmed as a regression check only.
- **Did (drove):** Voice Command family → `ModelSelectCard`; dumped the hierarchy.
- **Observed:** Default cell shows `"GPT-4"`; Pressed/Selected shows `"Local Llama (offline)"`; Disabled cell (empty `models`) shows the caption `"Set a provider and key first"` in place of a dropdown. Identical to the original run's finding — no regression.
- **Evidence:** `redrive-modelselectcard.xml` (`10-02-SELF-UAT-evidence-2026-10-01/`); `ModelSelectCardTest` 3/3 fresh at HEAD `b3fe6e7`.

### Re-verified: Criterion 3 (VAPPR-01, ladder list order) at new HEAD

result: passed (re-confirmed)

- **Rung:** 1 (unit test), decisive, same reasoning as the original run (the gallery's own fixture is coincidentally alphabetical; only the unit test's deliberately-unsorted fixture can distinguish "preserved order" from "coincidental resort"). `ApproachLadderCardTest`'s list-order test, 1/1 fresh at HEAD `b3fe6e7` (part of the 12/12 suite run above). Device dumps across this addendum (`redrive-ladder-default.xml`, `redrive-fixture-combined-affordances.xml`) additionally show `Cloud, Hybrid, Local` top-to-bottom in every cell touched, consistent with list order, not re-sorted.
- **Evidence:** `build/test-results/testDebugUnitTest/TEST-...ApproachLadderCardTest.xml` (this run, 12/12); device dumps above.

## Summary (Addendum)

total re-verified/newly-verified: 5 (Criteria 1-5, all re-confirmed; the new "Unavailable" + combined-affordance structural claim folded into Criterion 5)
passed: 5
partial: 0
failed: 0
infra: 0

## Notes / anomalies (Addendum)

- **The combined-legibility (aesthetic) judgment remains correctly deferred to Gate-2**, per `10-VERIFICATION.md`'s own explicit scope expansion ("Gate-2 should now also judge the combined legibility of up to three co-occurring subdued labels on one ineffective rung"). This addendum settles the functional/mechanism layer underneath that judgment — all three labels DO render, independently gated, none dropped or overwritten — which is a materially stronger finding than "the code looks right," since it was previously unexercised live by any prior self-UAT (flagged explicitly in `10-VERIFICATION.md`'s own why_human note).
- **Harness note:** `FixtureActivity` (the direct-call seed harness added this run) is throwaway, uncommitted UAT scaffolding — same diagnose-only contract as the rest of the harness. Its lack of system-bar-insets handling is a harness cosmetic limitation, not a library finding; it did not block the decisive rung-4 structural proof.
- **An incidental accessibility observation, NOT filed as a defect:** while chasing the stale-coordinate false lead (see Criterion 1's adversarial note), the `ProviderKeyCard` key field's `uiautomator`-reported `password="true"` attribute was observed to persist even after the reveal toggle flips to "Hide key" (visually unmasked). This is plausibly intentional/standard Android behavior (TalkBack/accessibility services conventionally still withhold password-field text from spoken announcement regardless of the visual masking state, to prevent audio shoulder-surfing) and is unrelated to this phase's diff (`ClearableTextField`'s `visualTransformation` forwarding is unchanged code, not touched by WR-01/02/03). It is not one of the 5 ROADMAP success criteria and is called out here only for completeness, not as a routed finding — a future phase involving accessibility-specific requirements should examine it with that lens if relevant, but it does not block this phase's gate.
- Device lease (`phase-10-1079787`) and harness app both cleaned up at run end, confirmed by direct check (`RELEASED`, `pm list packages` absent).

## Findings routed to gap-closure (Addendum)

None. All criteria remain genuine PASSes at HEAD `b3fe6e7`; the newly-added `require()` guards, "Unavailable" affordance, and `emptyProvidersReason` default are all additive, correctly functioning, and introduce no regression to the previously-verified mechanisms.

## Verdict (Addendum)

All 5 ROADMAP success criteria remain PASS at HEAD `b3fe6e7`, independently re-derived and re-observed on the real running app (not trusted from the 2026-09-30 log above). The WR-01/02/03 changes are confirmed additive and non-regressing. The combined-legibility (three-subdued-labels) structural mechanism is now, for the first time, live-confirmed functional — closing the specific gap `10-VERIFICATION.md` flagged as unexercised by the prior self-UAT — while the remaining aesthetic/contrast judgment correctly stays deferred to milestone v2.4 Gate-2, unchanged. Gate-1 remains satisfied; no gap-closure routing required.
