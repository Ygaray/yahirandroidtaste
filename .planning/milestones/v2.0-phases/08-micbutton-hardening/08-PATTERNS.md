# Phase 8: MicButton hardening - Pattern Map

**Mapped:** 2026-09-27
**Files analyzed:** 2 (1 modified source, 1 modified test)
**Analogs found:** 2 / 2 — both are the file's own existing internal patterns (self-analog: this is a
hardening phase on one existing composable, not a net-new file)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `src/main/java/io/github/ygaray/yahirandroidtaste/component/MicButton.kt` | component | event-driven (gesture callback) | itself — `enabled`'s existing `rememberUpdatedState` (line 64) + `pointerInput(Unit)` gesture block (lines 89-104) | exact (self) |
| `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt` | test | event-driven (Compose UI test / gesture injection) | itself — `pressAndHold_onDisabledMic_neverFiresOnTap` (lines 103-127), the down/waitForIdle/up/assert skeleton | exact (self) |

No separate "closest existing analog elsewhere in the codebase" is needed — CONTEXT.md and the
code itself point directly at the pattern to replicate within the same file. Sibling components
(`DateTimePicker.kt`, `PlaceMapPicker.kt`) were checked and confirm `rememberUpdatedState` is the
hub's standard latest-callback idiom, not something unique to `MicButton`.

## Pattern Assignments

### `src/main/java/io/github/ygaray/yahirandroidtaste/component/MicButton.kt` (component, event-driven)

**Analog:** the file's own `enabled` handling (this IS the pattern to copy, applied to `onTap`/`onDisabledTap`)

**Existing latest-value pattern to replicate** (lines 56-64):
```kotlin
val interactionSource = remember { MutableInteractionSource() }
// Read via rememberUpdatedState instead of keying pointerInput on `enabled` directly. Keying
// on `enabled` would restart (cancel) the gesture coroutine the instant it flips mid-press —
// and a cancellation arriving between `interactionSource.emit(press)` and the paired
// Release/Cancel emit below would leave an unterminated Press interaction, sticking the
// ripple/pressed visual indefinitely. Reading the latest value through this state instead
// keeps the SAME gesture coroutine alive for the whole press, so the `try`/`finally`-shaped
// emit pair below always completes.
val latestEnabled by rememberUpdatedState(enabled)
```

**Call site to convert** (line 100, inside `pointerInput(Unit)` / `detectTapGestures` / `onPress`):
```kotlin
if (latestEnabled) onTap() else onDisabledTap()
```
D-03 requires this becomes `if (latestEnabled) latestOnTap() else latestOnDisabledTap()`, where
`latestOnTap by rememberUpdatedState(onTap)` and `latestOnDisabledTap by rememberUpdatedState(onDisabledTap)`
are declared alongside `latestEnabled` (same block, same reasoning — the gesture coroutine is
keyed on `Unit`, so any callback swap mid-press must be read fresh via `rememberUpdatedState`
rather than force a coroutine restart).

**Signature to change** (lines 48-55) — apply D-01's param order and add the 3 description params
with hub-vocabulary generic defaults, replacing the hardcoded strings at lines 109-111:
```kotlin
@Composable
fun MicButton(
    isListening: Boolean,
    enabled: Boolean,
    onTap: () -> Unit,
    onDisabledTap: () -> Unit,
    modifier: Modifier = Modifier,
)
```
becomes (per D-01, exact order):
```kotlin
@Composable
fun MicButton(
    isListening: Boolean,
    enabled: Boolean = true,
    onTap: () -> Unit,
    onDisabledTap: () -> Unit = {},
    modifier: Modifier = Modifier,
    disabledDescription: String = "Microphone unavailable",
    tapToTalkDescription: String = "Tap to talk",
    listeningDescription: String = "Listening…",
)
```

**Hardcoded microcopy call site to parameterize** (lines 108-112, inside `Crossfade` content
lambda — do NOT touch `targetState = Pair(enabled, isListening)` at line 107 per CONTEXT.md):
```kotlin
!isEnabled -> Icon(Icons.Default.MicOff, contentDescription = "Voice not set up — open Settings")
listening -> Icon(Icons.Default.Stop, contentDescription = "Listening…")
else -> Icon(Icons.Default.Mic, contentDescription = "Tap to talk")
```
becomes: swap the three literal strings for `disabledDescription` / `listeningDescription` /
`tapToTalkDescription` respectively — only the content-lambda `Icon(contentDescription = …)`
usages change; the `targetState` pair stays untouched.

**KDoc to update to hub vocabulary** (lines 28-47): currently references CalTracker-specific
framing ("CalTracker's `MicFab` wrapper", "RECORD_AUDIO permission gating"). Per CONTEXT.md
Integration Points, this stays consumer-agnostic in prose but the "config flip" note at ~line 86
and the KDoc block at lines 36-46/57-63 (voice-mic-press-no-capture + stuck-ripple discipline)
must be preserved byte-for-byte in structure — only add doc for the new description params and
the callback latest-value note, do not restructure the existing gesture-discipline KDoc.

---

### `src/test/java/io/github/ygaray/yahirandroidtaste/component/MicButtonGestureTest.kt` (test, event-driven)

**Analog:** its own `pressAndHold_onDisabledMic_neverFiresOnTap` test (lines 103-127) — closest
existing shape for a down/waitForIdle/up/assert sequence that already exercises mid-press timing.

**Skeleton to extend for the new WR-02 regression test** (D-03):
```kotlin
@Test
fun pressAndHold_onDisabledMic_neverFiresOnTap() {
    var tapped = 0
    var disabledTap = 0
    composeRule.setContent {
        MaterialTheme {
            MicButton(
                isListening = false,
                enabled = false,
                onTap = { tapped++ },
                onDisabledTap = { disabledTap++ },
            )
        }
    }
    val node = composeRule.onNodeWithContentDescription(notSetUp)

    node.performTouchInput { down(center) }
    composeRule.waitForIdle()
    assertEquals("holding a disabled mic must never fire onTap", 0, tapped)

    node.performTouchInput { up() }
    composeRule.waitForIdle()
    assertEquals("onDisabledTap fires exactly once on release", 1, disabledTap)
    assertEquals(0, tapped)
}
```

**New test must diverge from this skeleton per D-03's exact spec:**
- Hoist the callback itself in `mutableStateOf` (not just a counter var) so it can be swapped
  mid-press: `var onTapCallback by remember/mutableStateOf<() -> Unit>({ tapA++ })` pattern —
  no existing analog for this in the file; this is new machinery layered on top of the existing
  down/waitForIdle/up skeleton.
- Between `down(center)` and `up()`, reassign the hoisted callback to a **distinct** counter
  (`tapA` → `tapB`), then force recomposition via `composeRule.waitForIdle()` before calling `up()`.
- Assert only `tapB` incremented and `tapA` did not — proving the closure read at release-time is
  the latest one (rememberUpdatedState working), not the one captured at press-time.
- Guard against vacuous pass: the two lambdas must be observably distinct (different counters),
  not a same-behavior swap that would pass regardless of `rememberUpdatedState` being present.
- Must go RED against current `MicButton.kt` (callbacks called directly, not via
  `rememberUpdatedState`) and GREEN after the `MicButton.kt` fix above.

**Existing content-description constants to reuse/extend** (lines 44-45):
```kotlin
private val tapToTalk = "Tap to talk"
private val notSetUp = "Voice not set up — open Settings"
```
Since the source defaults are staying as the CURRENT strings become the new defaults
(`"Tap to talk"`, `"Microphone unavailable"` — note: D-01's default disabled string is
`"Microphone unavailable"`, DIFFERENT from the current hardcoded `"Voice not set up — open
Settings"`), the test's `notSetUp` constant must be updated to `"Microphone unavailable"` to match
the new generic default, since MicButtonGestureTest calls `MicButton` without passing
`disabledDescription` explicitly.

---

## Shared Patterns

### Latest-callback safety via `rememberUpdatedState`
**Source:** `MicButton.kt` lines 56-64 (existing `enabled` → `latestEnabled` pattern)
**Apply to:** `onTap` and `onDisabledTap` in the same file, same `pointerInput(Unit)` block — do
not key `pointerInput` on the callbacks either; keep `pointerInput(Unit)` as the single gesture
owner per the byte-for-byte preservation requirement in CONTEXT.md.

### Modifier-first / default-before-nondefault param ordering
**Source:** CONTEXT.md D-01, cross-checked against hub convention (AttentionCue/SortControl use
`modifier: Modifier = Modifier` positioned after required params, before trailing optional
content lambdas — same shape MicButton already follows at line 54).
**Apply to:** `MicButton.kt` signature — append `disabledDescription`, `tapToTalkDescription`,
`listeningDescription` after `modifier`, grouped in disabled/tapToTalk/listening order to mirror
the `when` branch order at lines 108-112.

### Robolectric + Compose gesture-injection test harness
**Source:** `MicButtonGestureTest.kt` lines 37-42 (`@RunWith(RobolectricTestRunner::class)`,
`@Config(sdk = [35])`, `createComposeRule()`), consistent with `CycleSubTypeButtonTest` /
`AppChipTest` per its own KDoc (lines 26-27).
**Apply to:** the new WR-02 regression test added to the same test class — no new test
infrastructure needed, same `@Rule composeRule`.

## No Analog Found

None — this phase only touches one existing component and its one existing test file; every
pattern needed already exists in-file or in a directly-named sibling analog per CONTEXT.md.

## Metadata

**Analog search scope:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/`,
`src/test/java/io/github/ygaray/yahirandroidtaste/component/`
**Files scanned:** `MicButton.kt`, `MicButtonGestureTest.kt`, plus a grep for
`rememberUpdatedState` usage across `component/` (confirms `DateTimePicker.kt` and
`PlaceMapPicker.kt` as the hub's other users of this idiom — no further excerpting needed since
`MicButton.kt`'s own usage is the direct, CONTEXT.md-cited analog)
**Pattern extraction date:** 2026-09-27
