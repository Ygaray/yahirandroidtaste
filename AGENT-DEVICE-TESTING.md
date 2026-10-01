# Agent Device-Testing Playbook — yahirandroidtaste

Project-specific UAT driver playbook for `verify-work-agentic` (Gate-1 self-UAT). This repo is a
**pure `com.android.library`** (no `applicationId`) whose only runnable surface is the
self-launching `ExplorerActivity` gallery (`exported=false`, same-package only). The generic global
`AGENT-DEVICE-TESTING.md` template assumes an installable app APK and does not cover this; this file
exists specifically to document the harness mechanic so every future Gate-1 run stops re-deriving it
from scratch (backlog item `999.1-formalize-reusable-gate-2-visualization-harness-apk`).

Status note: this playbook was authored during Phase 11's Gate-1 run (2026-09-30), consolidating the
identical ad-hoc mechanism independently re-derived in five prior Gate-1 logs
(`01-05`, `06-03`, `07-01`, `08-01`, `10-02-SELF-UAT.md`). Detection is empirically proven, not a
best-effort draft — no `needs-confirmation` stamp required.

## D1 — Target + preflight

Target: the Gate-1 Android test rig (`tester` / `yahirs-s22-ultra-2`, see
`~/.claude/context/devices/test-android.md`). Resolve/lease/wake per the global workflow's D8 +
pre-flight steps (`dumpsys power`/`dumpsys trust`, `KEYCODE_WAKEUP`, `svc power stayon true`). A
not-ready target after one retry is INFRA, not a FAIL.

## D2 — Build / install / launch (the harness mechanism)

**The library itself produces an AAR, not an installable APK.** `ExplorerActivity` can only be
launched by a same-package (or same-signature) `Intent` because it is `exported=false`. The
established pattern is a **throwaway, never-committed `com.android.application` harness module**
built outside the repo (in the session scratchpad) that depends on the library fresh-published to
`mavenLocal()`:

1. **Publish the library under test to mavenLocal, from a clean/known HEAD:**
   ```bash
   # Rule out a stale-cache false pass -- delete any prior mavenLocal artifact first.
   rm -rf ~/.m2/repository/com/github/Ygaray/yahirandroidtaste/1.10.0
   rm -f  ~/.m2/repository/com/github/Ygaray/yahirandroidtaste/maven-metadata-local.xml
   cd /home/yahir/Projects/Reusable/android/yahirandroidtaste
   ./gradlew clean publishReleasePublicationToMavenLocal --no-daemon -q
   md5sum ~/.m2/repository/com/github/Ygaray/yahirandroidtaste/1.10.0/yahirandroidtaste-1.10.0.aar
   ```
   The coordinate/version is hardcoded in the root `build.gradle.kts`'s `publishing{}` block
   (currently `com.github.Ygaray:yahirandroidtaste:1.10.0`) — it does NOT bump per phase, so always
   delete-then-republish to avoid trusting a stale artifact from an earlier HEAD.

2. **Scaffold (or reuse, same session) a throwaway harness app** — `com.android.application`,
   matching AGP/compileSdk/minSdk to the library (AGP 9.2.1, `compileSdk { version = release(36) {
   minorApiLevel = 1 } }` — a plain integer `compileSdk = 36` fails `checkDebugAarMetadata` demanding
   "36.1 or later"; `minSdk = 35`), `mavenLocal()` first in `dependencyResolutionManagement`, one
   `MainActivity` that Intents straight into the library's `ExplorerActivity` and finishes:
   ```kotlin
   // app/build.gradle.kts
   android {
       namespace = "io.github.ygaray.yahirandroidtasteharness"
       compileSdk { version = release(36) { minorApiLevel = 1 } }
       defaultConfig { applicationId = "io.github.ygaray.yahirandroidtasteharness"; minSdk = 35; targetSdk = 36 }
   }
   dependencies { implementation("com.github.Ygaray:yahirandroidtaste:1.10.0") }
   ```
   ```kotlin
   // MainActivity.kt
   class MainActivity : Activity() {
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           startActivity(Intent(this, ExplorerActivity::class.java)); finish()
       }
   }
   ```
   Build in the **session scratchpad** (`$SCRATCHPAD/harness/`), never inside the repo tree — this is
   diagnose-only test scaffolding, never committed. A harness built in an earlier turn of the SAME
   session may already exist at that path; reuse it (just rebuild `assembleDebug` against the
   freshly-republished AAR) rather than re-scaffolding from scratch.

3. **Build + install + launch:**
   ```bash
   cd "$SCRATCHPAD/harness" && ./gradlew clean assembleDebug --no-daemon -q
   adb -s "$D" install -r build/outputs/apk/debug/yahirandroidtasteharness-debug.apk
   adb -s "$D" logcat -c
   adb -s "$D" shell am start -n io.github.ygaray.yahirandroidtasteharness/.MainActivity
   ```
   **Build identity for the SELF-UAT log:** record BOTH the library AAR md5 @ git short-sha (what's
   actually under test) and the harness APK md5 (the throwaway scaffold) — the AAR md5 is the one
   that matters for "is this a stale build" purposes.

## D3 — Act

Standard `adb shell input tap/swipe`. No special gotchas beyond the global template's, EXCEPT:
- `ExplorerActivity`'s own `KEYCODE_BACK` handling pops ONE level (dismiss an open
  `ModalBottomSheet` → detail page → family list → gallery index → harness `finish()`s the app). It
  does NOT skip levels — budget one `BACK` per nav level when driving back out.
- The theme toggle (`content-desc="Switch to dark/light theme"`) lives in each family screen's own
  `TopAppBar` — it is NOT global/persisted across navigation; toggling it on the Voice Command family
  screen only affects that screen's `ThemeMode` state for the current composition. Re-toggle after
  navigating back to the family list if you left mid-detail-page.

## D4 — Observe

Standard four layers (uiautomator dump, `screencap`, logcat, no files-on-disk layer — this library
has no persistence). One **library-specific structural trick** worth knowing: Compose's `clickable`
modifier merges into the semantics tree as `clickable="true"` on exactly the node/ancestor that
carries it — this means a `uiautomator dump` can DECISIVELY prove "this row is interactive, this
sibling row is not" (rung 4, no screenshot needed) by walking to the nearest `clickable` ancestor of
each node, e.g. distinguishing `OutcomeSheet`'s `Available` vs `Unavailable` undo rows. Reserve
`screencap` for the genuinely visual judgment (ripple/press-state capture, color-role contrast) on
top of that structural proof.

**Ripple/press-state capture recipe** (decisive rung-5 evidence that a control responds to tap vs.
not, stronger than a static before/after since it needs no visible side effect to work — useful when
a gallery fixture's callback is a no-op `{}`):
```bash
adb -s "$D" shell input swipe <x> <y> <x> <y> 400 &   # long "swipe" in place = held press
sleep 0.15
adb -s "$D" exec-out screencap -p > /tmp/ripple.png     # fires WHILE the press is held
wait
```

## D5 — Fixture/seed integrity

No DB/data layer — `ComponentRegistry` and every family screen's demo fixtures are static,
compiled-in Kotlin values (e.g. `VoiceCommandFamilyScreen.kt`'s `fixtureOutcome*`/
`fixtureClarificationOptions*`). "Seeding" is purely environmental: publish the correct HEAD to
mavenLocal and confirm the harness resolves it (no rows/relations to seed or verify-landed).
`explorer/` is drift-guard-denylisted so these fixtures never need registering in `ComponentRegistry`
itself.

## D6 — Ladder commands

Same as the global template (rungs 0–5: `./gradlew testDebugUnitTest`, `uiautomator dump`,
`screencap`, `logcat`). Rung 2 (Nyquist) is `gsd-validate-phase`. No rung-3 data layer (D5, above).

## D7 — Gotchas

- **compileSdk mismatch:** a plain `compileSdk = 36` integer in the harness fails
  `checkDebugAarMetadata` ("requires compileSdk 36.1 or later") — use the structured
  `release(36) { minorApiLevel = 1 }` form to match the library's AAR metadata.
- **Stale mavenLocal artifact:** the publish coordinate/version is a hardcoded constant in
  `build.gradle.kts`, not bumped per phase/commit — ALWAYS delete the existing
  `~/.m2/repository/com/github/Ygaray/yahirandroidtaste/<version>/` directory before republishing,
  or a prior phase's library code can silently pass as "current."
- **"Voice Command" (and other recently-added families) sit below the fold** on the gallery index —
  a `uiautomator dump` against the unscrolled index alone can falsely suggest a family isn't
  registered; scroll (`input swipe 540 1800 540 600 300`) before concluding absence.
- **A detail page's "Show sheet" button Y-coordinates shift once you scroll** — the States-matrix
  block above the Variants list changes effective offsets; re-dump after any scroll instead of
  reusing bounds from an unscrolled dump.
- **Gallery fixture callbacks are frequently no-ops (`onSelect = {}`, `onUndo = {}`)** — the
  interactive demo intentionally does NOT always hoist state (unlike e.g. `ProviderKeyCard`'s
  live-state demo). Don't mistake "no visible change after tap" for a broken callback; the unit
  tests are the decisive rung for callback-identity/argument-correctness claims, while the device
  pass should target the genuinely visual/structural claims a screenshot/tree dump can settle (see
  the ripple-capture recipe above for proving tap-responsiveness even when the callback is a no-op).
- **`DELETE_FAILED_INTERNAL_ERROR` on `adb uninstall` before a fresh harness install** is harmless
  when the package was never installed (or already removed) — `adb install -r` after it still
  succeeds; don't treat it as a device fault.

## When to involve the human

Same as the global template: secure keyguard, physical action, a genuine product/UX decision, or a
flow too brittle to automate. Nothing library-specific adds to this list.
