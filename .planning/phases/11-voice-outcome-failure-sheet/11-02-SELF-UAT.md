---
status: complete
result: all_pass
gate: 1
phase: 11-voice-outcome-failure-sheet
source: [11-ROADMAP.md success criteria]
device: Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN via USB adb, Android 15)
apk: yahirandroidtasteharness-debug.apk (md5 40dfd1835777223edeb137704174c181) — throwaway same-package-Intent harness depending on com.github.Ygaray:yahirandroidtaste:1.10.0 published fresh to mavenLocal @ 3a6dfa4
run: 2026-09-30T19:03:00Z
---

# Self-UAT Log — Phase 11 (Voice Outcome & Failure Sheet — Plans 01+02)

**Device:** Samsung SM-S908U / yahirs-s22-ultra-2 (`R5CT10XNKQN` via USB adb, Android 15) — the Gate-1 tester rig. Reachability live-probed (`adb -s R5CT10XNKQN get-state` → `device`) and leased (`gsd-lease.sh acquire device:R5CT10XNKQN`, owner `phase-11-<pid>`) before driving; released at run end. **No project-local `AGENT-DEVICE-TESTING.md` existed before this run** (same gap flagged in 01-05/06-03/07-01/08-01/10-02-SELF-UAT.md) — authored one at the repo root during this run, consolidating the identical harness mechanism independently re-derived in all five prior logs, so future Gate-1 runs stop re-deriving it from scratch.
**Library build:** Fresh HEAD `3a6dfa4` (clean tree at `src/`, `build.gradle.kts`, `api.txt`; only `.planning/`-internal tooling files + the new `AGENT-DEVICE-TESTING.md` dirty, neither phase-code-relevant) → deleted the stale mavenLocal `1.10.0` artifact (left over from Phase 10's `78477f7` build, to rule out a stale-cache false pass) → `./gradlew clean publishReleasePublicationToMavenLocal` → `BUILD SUCCESSFUL`. AAR md5 `c6fd4cf3401e1d6c1382f17014ecc4f9`.
**Run:** 2026-09-30T19:03:00Z
**Schema:** N/A — no DB/data layer; `ExplorerActivity` reads only the static, compiled-in fixtures in `VoiceCommandFamilyScreen.kt` (`fixtureOutcome*`, `fixtureClarificationOptions*`).
**Pre-flight:** device awake (`mWakefulness=Awake`); user-0 unlocked (`deviceLocked=0`; the co-present `deviceLocked=1` belongs to the unrelated Secure Folder profile, a known device quirk per `test-android.md`).
**Unit suite:** re-run directly at HEAD `3a6dfa4`, not trusted from `11-VERIFICATION.md`'s inherited verdict — `./gradlew testDebugUnitTest --tests "*OutcomeSheetTest*" --tests "*ClarificationBarTest*" --tests "*UndoHistoryStoreTest*" --rerun-tasks` → `BUILD SUCCESSFUL`. JUnit XML: `OutcomeSheetTest` tests="15" failures="0" errors="0"; `ClarificationBarTest` tests="9" failures="0" errors="0"; `UndoHistoryStoreTest` tests="25" failures="0" errors="0".
**Coverage/Nyquist:** not re-run standalone (out of this log's scope) — `11-VERIFICATION.md` already re-derived 12/12 truths at the code level and flagged exactly 2 on-device visual/interaction items as `human_verification`. This log drives those 2 items live (plus a sanity pass over the rest of VOUT-01/02/03/VUNDO-01/VCLAR-01) rather than trusting the inherited code-level verdict for the device-facing claims.
**Seed/fixture integrity:** N/A — all fixtures are static compiled-in Kotlin values (`explorer/` package, drift-guard denylisted, never registered). Arrange work was entirely environmental: delete stale mavenLocal artifact, `publishReleasePublicationToMavenLocal` from clean HEAD `3a6dfa4`, reuse this session's already-scaffolded throwaway harness app (`io.github.ygaray.yahirandroidtasteharness`) rebuilt against the fresh AAR, install + launch on `R5CT10XNKQN` only.

## Driver-mechanism note

Authored `AGENT-DEVICE-TESTING.md` at the repo root this run (see file for full detail) — the
established pattern across six Gate-1 logs now: a throwaway `com.android.application` harness
(`io.github.ygaray.yahirandroidtasteharness`, AGP 9.2.1, `compileSdk { version = release(36) {
minorApiLevel = 1 } }`, `minSdk 35`) whose sole `MainActivity` does `startActivity(Intent(this,
ExplorerActivity::class.java)); finish()`, depending on `com.github.Ygaray:yahirandroidtaste:1.10.0`
resolved from `mavenLocal()`. Built in this session's scratchpad, never committed. Zero
`FATAL`/`AndroidRuntime.*Exception` lines in `adb logcat -d` across the entire session
(`grep -ciE "FATAL|AndroidRuntime.*Exception"` → `0`). Harness uninstalled at the end of the run
(confirmed absent via `pm list packages --user 0`); the device lease was released.

**Adversarial notes:**
- Both gallery fixtures under test (`OutcomeSheet`'s undo rows, `ClarificationBar`'s options) wire
  their interactive callbacks (`onUndo`, `onSelect`, `onDismiss`) as no-ops (`{}`) in the gallery demo
  — tapping them produces NO visible state change (unlike e.g. Phase 10's `ProviderKeyCard`/
  `ApproachLadderCard` demos, which do hoist local state). A naive device pass could misread "nothing
  visibly changes after tap" as a broken callback. Resolved this by (a) trusting the unit-test rung
  (already 9/9 + 15/15 passing, decisive for callback-identity/argument-correctness) for the
  "fires with the correct id/doesn't re-speak" sub-claims, and (b) using a **held-press ripple
  capture** (`input swipe <x> <y> <x> <y> 400` + a `screencap` fired mid-press) to prove genuine
  per-element tap-responsiveness on the real running app even with a no-op callback — this is a
  stronger, more falsifiable signal than a static dump because it requires nothing downstream of the
  tap to work, only the tap itself to register on that exact element.
- The ripple capture also directly falsified the alternate, more worrying hypothesis for the
  duplicate-label `ClarificationBar` chips — that two same-label chips might secretly be ONE merged
  semantics node (which would make "each independently tappable" false even though the unit test
  proves `onSelect` receives the right id when invoked programmatically, since Robolectric's
  `performClick()` doesn't necessarily catch a touch-target overlap/merge the way a live two-finger
  reality would). The structural tree (two separate `clickable=true` ancestors, non-overlapping
  bounds) and the ripple capture (press-state highlight confined to only the tapped chip, zero bleed
  onto its label-twin) together close that gap.
- `OutcomeSheet`'s "handled by" check was driven in dark theme (left over from the prior
  ClarificationBar dark-theme pass, since the theme toggle is per-family-screen state, not global/
  persisted — noted in the new playbook's D3 section) rather than light; this is still valid evidence
  for the "renders from props" claim (ROADMAP SC2 has no light/dark requirement), just noted for the
  reviewer's awareness since it wasn't a deliberate re-confirmation in both themes for that specific
  sub-check.

## Criteria

### 1. An outcome/failure sheet renders a command outcome from props with no app-specific nouns (ROADMAP SC1, VOUT-01)

result: passed

- **Rung:** 4 (UI structure tree) — decisive for "renders from props"; this truth was already
  shipped pre-phase (commit `6a946d3`) and code-confirmed in `11-VERIFICATION.md` Truth 1; this run
  independently re-confirms it live on the real running app rather than trusting the inherited verdict.
- **Target:** device (yahirs-s22-ultra-2, real hardware, via the throwaway harness).
- **Expected:** `OutcomeSheet` renders whatever `VoiceOutcomeUiState` (Success/Failure) it's given, using only domain-neutral vocabulary (no SecondBrain/CalTracker-specific nouns).
- **Arranged (seeded):** none — static compiled-in fixtures (`fixtureOutcomeSuccessSummaryOnly`, `fixtureOutcomeFailureNoAction`, etc.).
- **Did (drove):** Launched the harness → Voice Command family → `OutcomeSheet` entry → tapped "Show sheet" across all 6 Variants fixtures over the course of this run (summary-only, handled-by, failure×2, undo×3 — see Criteria 2–5 below for the specific ones with dedicated must-haves).
- **Observed:** Every fixture rendered its own distinct `VoiceOutcomeUiState` content with zero app-specific nouns anywhere in the tree/screenshots (generic vocabulary throughout: "Logged N items", "Card deleted", "Tag removed", "Couldn't undo", "Could not reach the provider", "API key rejected" — all plausibly domain-neutral placeholder copy, not tied to any one consumer app).
- **Evidence:** all screenshots/dumps under Criteria 2–5 below double as evidence for this criterion. `OutcomeSheetTest` 15/15 fresh at HEAD `3a6dfa4`.

### 2. The sheet surfaces a "handled by: tier/approach" indicator identifying which tier/approach handled the command, from props (ROADMAP SC2, VOUT-02)

result: passed

- **Rung:** 4 (UI structure tree) — decisive; a text-presence claim, fully settled without a screenshot (screenshot captured anyway for completeness).
- **Target:** device (yahirs-s22-ultra-2, real hardware). Driven in dark theme (see adversarial notes).
- **Expected:** per ROADMAP SC2 + `HandledByRow`: tier renders as the primary label; approach/provider/model/escalation count render as an optional secondary caption line, all from props.
- **Arranged (seeded):** none — static fixture `fixtureOutcomeSuccessHandledBy` (tier "Cloud", approach "Direct", provider "OpenAI", model "gpt-4", escalationCount 1).
- **Did (drove):** From the OutcomeSheet detail page, tapped "Show sheet" for "OutcomeSheet — Success, handled-by populated".
- **Observed:** `uiautomator` dump shows `"Cloud"` (primary label) and `"Direct · OpenAI · gpt-4 · Escalations: 1"` (secondary caption, `·`-joined exactly as `HandledByRow`'s `joinToString(" · ")` implementation specifies) — both rendered live from the fixture's props, confirmed in the accompanying screenshot too.
- **Evidence:** `11-02-SELF-UAT-evidence/ui19-handledby.xml`, `ui20-handledby-sheet.xml`; `shot10-handledby-crop-small.png`.

### 3. Failure states render prominently and visibly — loud, not silent or subtle — with an OPTIONAL prop-driven action slot; absent prop → no action rendered (ROADMAP SC3, VOUT-03)

result: passed

- **Rung:** 5 (visual capture) — decisive for "loud, not silent or subtle"; the structure tree already proves text presence/absence of the action button, but "loud" is a color/contrast claim a tree can't settle.
- **Target:** device (yahirs-s22-ultra-2, real hardware). Both fixtures driven in dark theme.
- **Expected:** both Failure fixtures render on the error-container color roles (not `AttentionCue`/snackbar); the no-action fixture renders zero action controls; the action-populated fixture renders exactly the supplied action's label.
- **Arranged (seeded):** none — static fixtures `fixtureOutcomeFailureNoAction` (reason "Could not reach the provider", no action) and `fixtureOutcomeFailureWithAction` (reason "API key rejected", action "Open Settings").
- **Did (drove):** Tapped "Show sheet" for each fixture in turn; dumped + screenshotted both.
- **Observed:** No-action fixture: tree shows ONLY `"Could not reach the provider"` — zero button/action nodes anywhere in the sheet. Action-populated fixture: tree additionally shows `"Open Settings"` as a tappable action. Both screenshots show the identical loud, solid red `errorContainer`/`onErrorContainer` surface filling the sheet — a clearly prominent, non-subtle treatment in both cases, with the only difference being the presence/absence of the action row underneath the reason text, exactly matching "absent prop → no action rendered."
- **Evidence:** `11-02-SELF-UAT-evidence/ui23-failure-noaction.xml`, `ui24-failure-withaction.xml`; `shot11-failure-noaction-dark-crop.png`, `shot12-failure-withaction-dark-crop.png`.

### 4. The sheet renders a prop-driven "Undo all (N)" action alongside per-item Undo, and can represent a per-item-undo-unavailable state — an item entangled with another that cannot be undone alone (ROADMAP SC4, VUNDO-01) — HUMAN_VERIFICATION item 1

result: passed

- **Rung:** 5 (visual capture) — decisive. The structural clickable/non-clickable distinction (rung 4) already proves the mechanism, but the ROADMAP/CONTEXT.md explicitly require a Gate-1 on-device visual check of "dimmed/no-ripple vs. responds-to-tap," which only a screenshot (specifically, a held-press ripple capture) can settle.
- **Target:** device (yahirs-s22-ultra-2, real hardware). Driven in BOTH light and dark theme, per the human_verification spec.
- **Expected:** per `11-VERIFICATION.md`'s human_verification item 1: the `Unavailable` row's reason renders as static, visibly non-interactive text (no ripple/press feedback), while the sibling `Available` row visibly responds to tap, in both themes.
- **Arranged (seeded):** none — static fixture `fixtureOutcomeSuccessUndoUnavailable` ("Card deleted" = `Available`, "Tag removed" = `Unavailable(reason="Entangled with another edit")`, `allLabel="Undo all (1)"`).
- **Did (drove):** Light theme: tapped "Show sheet" for the entangled-Unavailable fixture; dumped the tree (parent-clickable walk); held-press-tapped the "Card deleted" row center and fired a `screencap` mid-press. Repeated identically after toggling to dark theme (tap "Switch to dark theme" → re-navigate → re-open the same fixture → re-dump → re-ripple-capture).
- **Observed:** **Structural (both themes):** `Card deleted`'s immediate row container has `clickable="true"`; `Tag removed`'s immediate row container has `clickable="false"` (no clickable modifier present at all) — directly reconfirms `OutcomeSheet.kt`'s `UndoRowItem` conditional-modifier logic live on the real running app, in both themes. **Visual (light):** the held-press capture (`shot2-ripple-zoom.png`) shows a grey press-state rectangle confined EXACTLY to the "Card deleted" row's bounds, with ZERO press feedback on "Tag removed" directly below it in the same frame — the single most decisive, falsifiable confirmation possible that one row responds to touch and its sibling genuinely does not. The static dump (`shot1-rows-zoom.png`) additionally confirms the Unavailable row's reason text ("Entangled with another edit") renders in a visibly smaller, muted-grey (`onSurfaceVariant`/`labelSmall`) style, distinct from the full-weight black row labels. **Visual (dark):** `shot4-unavailable-dark-crop-small.png` confirms the identical pattern renders correctly on the dark surface (muted-grey reason text against the dark card background, same row structure).
- **Evidence:** `11-02-SELF-UAT-evidence/ui6-unavailable-sheet.xml` (light, structural), `ui11-dark-unavailable-sheet.xml` (dark, structural), `shot1-rows-zoom.png` (light, static zoom), `shot2-ripple-zoom.png` (light, **decisive** ripple capture), `shot4-unavailable-dark-crop-small.png` (dark). `OutcomeSheetTest` 15/15 fresh at HEAD `3a6dfa4` (includes the Unavailable-rendering regression tests).

### 5. An undo-refused / partial-undo state renders loudly with a reason, domain-neutral (ROADMAP SC5, VUNDO-01) — HUMAN_VERIFICATION item 1 (continued)

result: passed

- **Rung:** 5 (visual capture) — decisive for "loud... not a subtle/snackbar treatment," a color/surface-prominence claim a structure tree alone cannot settle.
- **Target:** device (yahirs-s22-ultra-2, real hardware). Driven in BOTH light and dark theme, per the human_verification spec.
- **Expected:** per `11-VERIFICATION.md`'s human_verification item 1: the refused substate renders on the error-container color roles (not subtle/snackbar), with the reason text, in both themes.
- **Arranged (seeded):** none — static fixture `fixtureOutcomeSuccessUndoRefused` (reason "Item changed since", changedItem "Card deleted").
- **Did (drove):** Tapped "Show sheet" for the undo-refused fixture in dark theme first; dumped + screenshotted. Toggled to light theme, re-navigated, re-opened the same fixture, re-dumped + re-screenshotted.
- **Observed:** Both themes: tree shows `"Couldn't undo: Item changed since, Card deleted changed since"` (reason + `", <item> changed since"` suffix, exactly matching `UndoAffordanceBody`'s string template). Both screenshots show a large, solid, full-width error-container surface (deep red on dark theme / light pink-red on light theme) wrapping the message in `labelLarge` white/dark-red text — unambiguously a loud, prominent card treatment, nothing resembling a transient/subtle snackbar in either theme.
- **Evidence:** `11-02-SELF-UAT-evidence/ui12-dark-refused.xml`, `ui14-light-refused.xml`; `shot5-refused-dark-crop-small.png`, `shot6-refused-light-crop-small.png`.

### 6. A prop-driven clarification-choices surface renders a question + pressable options (label + opaque id) with onSelect + dismiss — visually informative (not an error); tapping an option resolves without re-speaking (ROADMAP SC6, VCLAR-01) — HUMAN_VERIFICATION item 2

result: passed

- **Rung:** 5 (visual capture) — decisive for "visually informative (not an error)" and for falsifying the "two same-label chips might be one merged touch target" hypothesis; rung 1 (unit test) is decisive and sufficient on its own for the "resolves without re-speaking"/correct-opaque-id sub-claims (the library contains no speech code at all — domain-neutral by construction — and `ClarificationBarTest` already directly asserts `onSelect` fires with the correct, distinct id per duplicate-label chip).
- **Target:** headless (unit test, decisive for id-correctness) + device (live visual/structural confirmation, decisive for the deferred visual-design judgment). Driven in BOTH light and dark theme.
- **Expected:** per `11-VERIFICATION.md`'s human_verification item 2: the bar reads as a compact, visually-informative (non-error) surface, distinct from the Failure/undo-refused error treatment; tapping resolves without a re-speak prompt; the two duplicate-label chips are each independently distinguishable/tappable.
- **Arranged (seeded):** none — static fixtures `fixtureClarificationOptionsTwo` (Groceries/Work) and `fixtureClarificationOptionsDuplicateLabel` (two options both labeled "Shopping List", ids `list-a`/`list-b`).
- **Did (drove):** Light theme: Voice Command family → `ClarificationBar` entry (both fixtures render unconditionally in the States matrix, no "Show" trigger needed). Dumped the tree (parent-clickable walk on both "Shopping List" nodes); held-press-tapped the SECOND "Shopping List" chip and fired a `screencap` mid-press. Repeated the full visual pass again after toggling to dark theme.
- **Observed:** **Structural (light):** the two "Shopping List" text nodes resolve to two SEPARATE immediate ancestors, each independently `clickable="true"`, with non-overlapping bounds (`[90,1251][403,1386]` vs `[426,1251][739,1386]`) — directly refutes any "merged into one semantics node" concern; each chip is a genuinely distinct tap target. **Visual (light), decisive:** the held-press capture (`shot8-chip-ripple-zoom.png`) shows a press-state highlight confined EXACTLY to the second "Shopping List" chip's rounded-rect bounds, with its same-label twin to the left showing zero bleed-over feedback — definitive proof the two identically-labeled chips are independently, exclusively tappable. **Visual (both themes):** `shot7-clarbar-light-crop-small.png`/`shot9-clarbar-dark-crop-small.png` show the bar as a plain neutral-surface card ("Which list?" + two outlined chip buttons + a "Dismiss" text action) with NO red/error styling in either theme — directly contrasting with Criteria 3/5's error-container-red Failure/refused surfaces, confirming the "visually informative, not an error" design call. "Resolves without re-speaking": confirmed by source inspection (`ClarificationBar.kt` contains no speech/TTS-related code or re-speak affordance of any kind — the library is domain-neutral by construction, it cannot "re-speak" anything) plus `ClarificationBarTest`'s 9/9 passing suite, which directly asserts `onSelect(id)` fires exactly once per tap with the correct, non-deduplicated, non-reordered id even for same-label options.
- **Evidence:** `11-02-SELF-UAT-evidence/ui16-clarbar-detail.xml` (structural, both chips' clickable ancestors + bounds); `shot7-clarbar-light-crop-small.png`, `shot8-chip-ripple-zoom.png` (**decisive**), `shot9-clarbar-dark-crop-small.png`. `ClarificationBarTest` 9/9 fresh at HEAD `3a6dfa4`.

## Summary

total: 6
passed: 6
partial: 0
failed: 0
infra: 0

## Notes / anomalies (for the Gate-2 reviewer)

- **Both of `11-VERIFICATION.md`'s human_verification items are now resolved by this Gate-1 run, not deferred further.** Item 1 (OutcomeSheet entangled-Unavailable row + undo-refused substate, both themes) and item 2 (ClarificationBar tap-to-resolve + duplicate-label edge) were both driven live on the real device with decisive ripple-capture evidence, closing the gap `11-CONTEXT.md`'s "Specific Ideas" note and D-07 explicitly assigned to Gate-1 rather than the executor. Nothing from this phase remains deferred to milestone-close Gate-2 as a mechanism/behavior question — the owner's milestone-close pass is a final sign-off read of this evidence, not new verification work.
- **Authored the repo's first `AGENT-DEVICE-TESTING.md`** this run (see file). This closes a gap flagged in five prior Gate-1 logs (01-05, 06-03, 07-01, 08-01, 10-02) where the identical harness mechanism was independently re-derived from scratch each time. Future Gate-1 runs for this repo should resolve this playbook directly rather than re-deriving the mechanism.
- **The gallery's `OutcomeSheet`/`ClarificationBar` demo fixtures wire their callbacks as no-ops** (`onUndo = {}`, `onSelect = {}`, `onDismiss = {}`) — unlike Phase 10's live-state demos. This run's held-press ripple-capture technique (documented in the new playbook) is the generalizable answer for proving genuine tap-responsiveness on a no-op-callback fixture without needing a visible side effect; it's now part of the project's standing D4 observation toolkit for future phases with similar no-op demo fixtures.
- The harness app and its build artifacts are **not committed** — throwaway UAT infra only, per the diagnose-only tester contract. Uninstalled from the device at run end (`pm list packages --user 0` confirms absence); the device lease was released; the personal phone (`100.126.94.47`) was never targeted.

## Findings routed to gap-closure (if any)

None — all 6 ROADMAP success criteria are genuine PASSes, each independently re-derived from the ROADMAP text (not from `11-VERIFICATION.md`'s or any plan SUMMARY's prior claims) and confirmed by actually driving the real running app: live structural clickable/non-clickable distinction plus decisive held-press ripple captures for both deferred human-judgment items, live loud error-container rendering for both Failure and undo-refused states in both themes, live "handled by" prop rendering, and live confirmation that duplicate-label ClarificationBar chips are genuinely independent tap targets.

## Verdict

All 6 criteria PASS → Gate-1 complete. Registered in `.planning/uat-pending/11-voice-outcome-failure-sheet.md` → `HUMAN-UAT-PENDING.md`.
