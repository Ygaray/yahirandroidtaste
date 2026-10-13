# INTEGRATION.md — adopt `yahirandroidtaste` in a new Android app

Operational checklist for consuming the design-system library in a **consumer app**. The public
surface it references is in **`API.md`**; the deeper reuse doctrine is in **`CLAUDE.md`**;
`SecondBrain` is the first (pending) reference consumer, repinned in Phase 103.

**Prerequisites (the host must already have) — read these two first, they are the ones that bite:**

- **A Hilt-enabled `Application`.** The library provides `@Singleton` bindings (e.g. `UndoHistoryStore`,
  declared `@Singleton class UndoHistoryStore @Inject constructor()`) but declares **NO
  `@HiltAndroidApp` and NO `@AndroidEntryPoint`** — it is *bindings-only*. Your app must be a Hilt app
  (`@HiltAndroidApp class MyApp : Application()`) so its `SingletonComponent` aggregates the library's
  `@Singleton` bindings. **Without a Hilt application host, injection of the library's singletons fails
  at the consumer** (no component to install the bindings into). See §3.
- **A Compose BOM aligned with the library's.** The library builds against **Compose BOM 2026.04.01**.
  Your consumer must align its own Compose BOM so the Compose runtime/UI/material3 versions match and
  there is no duplicate/mismatched Compose on the classpath (a mismatch surfaces as
  `NoSuchMethodError` / composition crashes at runtime, not at compile time). See §4.
- Android `minSdk 35`, `compileSdk 36` (minor API 36.1), JDK 17.

---

## 1. Add the JitPack repository

In **`settings.gradle.kts`** → `dependencyResolutionManagement { repositories { … } }`:

```kotlin
maven { url = uri("https://jitpack.io") }
```

(Project-level repos are forbidden under `FAIL_ON_PROJECT_REPOS`.)

## 2. Depend on the library (pin an immutable tag)

> **The tag is cut in Phase 102 (human-gated) — none exists yet.** Use the coordinate below once it
> lands.

Prefer a version-catalog entry in **`gradle/libs.versions.toml`**:

```toml
[versions]
yahirandroidtaste = "vX.Y.Z"   # immutable tag — never main-SNAPSHOT

[libraries]
yahirandroidtaste = { group = "com.github.Ygaray", name = "yahirandroidtaste", version.ref = "yahirandroidtaste" }
```

Then in your app module's **`build.gradle.kts`**:

```kotlin
implementation(libs.yahirandroidtaste)
// equivalently: implementation("com.github.Ygaray:yahirandroidtaste:vX.Y.Z")
```

Confirm it resolved (Gradle/JitPack caching can hand back stale bytes):

```bash
./gradlew --refresh-dependencies :app:dependencies | grep yahirandroidtaste   # must print the pinned tag
```

> The library exposes `sh.calvin.reorderable` via `api` (the `EditorItemRow` receiver type,
> `ReorderableCollectionItemScope`), so that type lands transitively on your compile classpath — you
> do not add it yourself, but be aware it is on the graph.

## 3. Prerequisite — host the Hilt `SingletonComponent` (bindings-only library)

The library ships **no application host**. You provide one; its `SingletonComponent` is where the
library's `@Singleton` bindings live.

```kotlin
@HiltAndroidApp
class MyApp : Application()          // registered as android:name in AndroidManifest.xml
```

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Inject library singletons anywhere in your graph — e.g. into a ViewModel:
    //   class MyViewModel @Inject constructor(private val undoHistory: UndoHistoryStore) : ViewModel()
    // They resolve because MyApp's SingletonComponent aggregated the library's @Singleton bindings.
}
```

**If you skip this** (no `@HiltAndroidApp` app), Hilt has no `SingletonComponent` to install the
library's bindings into and injection of `UndoHistoryStore` (and any other library singleton) fails —
this is the #1 integration mistake for a bindings-only library.

## 4. Prerequisite — align your Compose BOM

Match the library's Compose BOM so a single, consistent Compose is on the classpath:

```kotlin
// build.gradle.kts (consumer)
implementation(platform("androidx.compose:compose-bom:2026.04.01"))   // align with the library
```

Then wrap your UI in the library theme — every component assumes it renders inside it:

```kotlin
setContent {
    YahirAndroidTasteTheme {
        // …call the ten-family components (see API.md)…
    }
}
```

## 5. Call components from your UI

The components are plain public `@Composable` functions — call them directly, passing your domain
data + callbacks (the library holds no domain state). See **`API.md`** for the full catalog and each
component's key parameters:

```kotlin
YahirAndroidTasteTheme {
    EmptyState(icon = Icons.Default.Inbox, title = "Nothing here yet")
    AppChip(label = "Work", isSelected = true, onClick = { /* … */ })
    ConfirmationDialog(title = "Delete?", body = "This can't be undone", onDismissRequest = { /* … */ })
}
```

### Localizing the voice surface (optional)

The hub ships English defaults and localizes nothing, so pass your own already-localized strings.
The composable labels are optional parameters: `providerLabel` on `ProviderKeyCard`, `modelLabel` on
`ModelSelectCard`, `dismissLabel` on `ClarificationBar`, and `unavailableLabel`, `cappedLabel`,
`needsNetworkLabel`, `onlineLabel`, `offlineOnlyLabel`, `routerOnLabel`, `routerOffLabel` on
`ApproachLadderCard`. The outcome-sheet texts are defaulted fields on the models you already build:
`escalationsLabel` on `HandledByUiModel`, `undoneLabel` on `UndoRowUiModel`, `refusedPrefix` and
`changedSinceSuffix` on `UndoRefusedUiModel`, `removeContentDescription` on `ProposedItemUiModel`,
and `semanticsPrefix` on `VoiceOutcomeUiState.Failure`. Omitting any of them keeps the English
default. Two residuals stay English and are not yet overridable: the segmented toggles' accessibility state
words ("selected" / "not selected"), and the `ProviderKeyCard` key field's reveal / hide toggle and
clear control ("Show key", "Hide key", "Clear text").
When moving to v2.5.0, pass `trailingContent` on `ProposedItemUiModel` as a named argument.

```kotlin
ClarificationBar(
    question = question,
    options = options,
    onSelect = { id -> /* … */ },
    onDismiss = { /* … */ },
    dismissLabel = stringResource(R.string.dismiss)   // your own string resource
)
```

The full parameter list and the compatibility scope live in `API.md` sections 10 and 11, its
"Stable test tags (documented API)" section and its v2.5.0 and v2.6.0 compatibility notes.

### 5b. Wiring the v2.6.0 Receipt, key-lifecycle and Undo Center additions (optional)

Everything in this subsection is opt-in. A call site that compiled against v2.5.1 recompiles and
renders as before, with three positional exceptions: a four-argument positional `UndoCenterScreen`
call (see (iii)), a `NeedsConfirmation` constructed or destructured positionally (or its `copy`
called positionally), and a `SheetScaffold` that passes `content` as the sixth positional argument
instead of as the trailing lambda. Named and trailing-lambda calls are unaffected; the one-line fix
for each is in `API.md` "Adding / changing components". The full signatures are in `API.md`
sections 10 and 11; the test tags a UI test may rely on are in its "Stable test tags (documented
API)" section.

**(i) The Receipt `OutcomeSheet`.** Pass `style = OutcomeSheetStyle.Receipt` and, if your theme uses
dynamic color, your own tone tokens, because the default warn tone is `tertiaryContainer` over
`onTertiaryContainer` and is not amber under Material You:

```kotlin
val amber = ToneColors(container = MyColors.AmberContainer, content = MyColors.OnAmberContainer)

OutcomeSheet(
    outcome = VoiceOutcomeUiState.NeedsConfirmation(
        reason = "You said: “…”",
        items = emptyList(),                       // the rows come from topLevelContent
        title = "Review these",
        confirmLabel = "Confirm all",
        topLevelContent = {
            ReceiptRow(
                title = "Item A", subtitle = "2 units", value = "12", valueCaption = "total",
                attentionCue = "Check this one",   // the Warn cue
                expanded = openIndex == 0,         // the row is stateless: YOU hold the accordion
                onClick = { openIndex = if (openIndex == 0) null else 0 },
                expandedContent = { /* your own editor */ },
            )
        },
        confirmEnabled = nothingNeedsAttention,    // false disables Confirm; onConfirm never fires
        onConfirm = { /* … */ },
        onCancel = { /* … */ },
    ),
    onDismissRequest = { /* the same decline logic as onCancel */ },
    style = OutcomeSheetStyle.Receipt,
    colors = OutcomeSheetDefaults.colors(warnTone = amber),
)
```

- `Success` takes `badge`, `titleActions` and the `startAction` / `endAction` pair
  (`SheetActionUiModel`); mark a Cancel with `activeWhileInFlight = true` so it stays usable while
  `inFlight` locks the others. The badge is decorative, so put the status in the `summary` text.
- `Failure` takes `title`, `transcript`, `tone = FailureTone.Warn`, a `tip` slot, a
  `secondaryAction` and `undo`. `Failure.undo` renders the same grouped undo block as `Success`, so a
  consumer that kept a local failure-undo body can retire it.
- `HandledByUiModel(prefixLabel = …, onDevice = true)` localizes the Receipt pill prefix and tints its dot.
- The library never sets `testTagsAsResourceId`. To let UI Automator see the stable tags, set
  `Modifier.semantics { testTagsAsResourceId = true }` on your own root composable.

**(ii) The `ProviderKeyCard` key lifecycle.** The card stays stateless: you own persistence and the
typed text, and you pass fingerprints only, never a raw key. The fingerprint is a short display string
(the placeholder form is `key-…c3d4`: the text key, a dash, an ellipsis and four characters).

```kotlin
ProviderKeyCard(
    providers = providers,
    selectedProviderId = selected,
    onProviderSelected = { id -> selected = id; typed = ""; editing = false },  // reset on a provider switch
    keyValue = typed,
    onKeyChange = { typed = it },
    keyState = keyState,
    keyLabel = "API key",
    savedKeys = mapOf("provider-b" to SavedKeyUiModel("key-…c3d4")),           // providerId -> fingerprint
    editingSavedKey = editing,
    actions = ProviderKeyActions(
        onSave = { save(selected, typed) },
        saveEnabled = typed.isNotBlank(),
        onUpdate = { editing = true },
        onClear = { clear(selected) },
        onCancelEdit = if (editing) { { editing = false; typed = "" } } else null,  // only while editing a saved key
        inFlight = saving,                                                      // disables every action
    ),
)
```

- A provider with an entry in `savedKeys` shows the summary `"Saved · <fingerprint>"` with Update and
  Clear instead of the field; `editingSavedKey = true` brings the field back for the Update flow. It is
  one flag for the whole card, so reset it (and the typed text) in `onProviderSelected`.
- A `null` callback in `ProviderKeyActions` hides that button, and `null` `actions` renders no button row.
- Pass `onCancelEdit` only while `editingSavedKey` is true. The card draws Cancel whenever the callback
  is non-null, and a provider with no saved key has nothing to cancel back to.
- **What a consumer can delete.** CalTracker can drop its dead provider dropdown
  (`onProviderSelected = {}`) and its own Save, Cancel and "key configured" rows, pass the real provider
  list and let the card draw them. SecondBrain is unchanged: with `savedKeys` empty and `actions` null the
  card renders exactly as before, and it keeps its own Save row.

**(iii) Undo Center groups.** Register a group once per batch, append its members with the group id,
and project the groups into rows from the store on each recomposition:

```kotlin
store.openGroup(groupId = batchId, label = "Batch edit") { revertWholeBatch(batchId) }  // ONE atomic undo
store.append("Renamed item A", groupId = batchId) { revertOne(itemA) }                  // a member

val groups = entries
    .mapNotNull { store.groupIdOf(it.id) }.distinct()
    .mapNotNull { id ->
        val label = store.groupLabel(id) ?: return@mapNotNull null
        UndoGroupRowUiModel(
            groupId = id,
            label = label,
            memberIds = store.group(id).map { it.id },
            status = store.groupStatus(id),
        )
    }

UndoCenterScreen(
    entries = entries,
    onNavigateBack = { navController.navigateUp() },
    onUndo = viewModel::onUndo,
    groups = groups,
    onUndoGroup = { id -> viewModel.onUndoGroup(id) },   // calls store.attemptUndoGroup(id)
    onClear = viewModel::onClear,
)
```

- `onUndoGroup` should call `UndoHistoryStore.attemptUndoGroup(groupId)`. It runs your registered
  `undoAll` exactly once; throw `UndoGroupRefusedException` from it to refuse without writing anything
  (the members stay Available and a retry can succeed).
- **Positional-call note.** `UndoCenterScreen(entries, onNavigateBack, onUndo, onClear)` with four
  positional arguments no longer compiles, because `groups` and `onUndoGroup` now sit before the
  trailing `onClear`. Name it (`onClear = …`) or pass it as the trailing lambda; every named and
  trailing-lambda form compiles.
- The member count and status words of a group row ("3 items", "Undoable", "Resolved") are
  English-only library text; only `undoAllLabel` is overridable.

**(iv) Seeing it work.** The Explorer gallery has demos for all of the above: the Receipt `OutcomeSheet`
states under `OutcomeSheet`, the count editor under `SegmentedOptionSelector`, the key lifecycle under
`ProviderKeyCard` and the group rows under `UndoCenterScreen`.

**(v) After v2.6.0: the confirm-sheet handler pill and a hoistable sheet state.** Two more opt-in
additions ship with the next tag after v2.6.0:

- `NeedsConfirmation(handledBy = HandledByUiModel(…))` draws the same handled-by pill as `Success`
  directly under the Receipt confirm title row. `null` (the default) draws nothing and Classic ignores
  it. The field sits after `titleActions` and before `onConfirm` / `onCancel`, so, like v2.6.0, only a
  positional construction, a positional `copy` or destructuring breaks (`component12()` is now
  `handledBy`; the lambdas are `component13()` and `component14()`). Named calls, which is what both
  consumers write, compile unchanged.
- `OutcomeSheet(…, sheetState = state)` is a new overload with a required, named `sheetState` that
  both styles pass to `SheetScaffold`. Hoist one `rememberModalBottomSheetState()` next to a SINGLE
  `OutcomeSheet` call (swap the `outcome`, don't swap call sites), then slide a finished sheet down on
  Close / Done / Cancel and clear the outcome only afterwards; `hide()` does not call
  `onDismissRequest`:

```kotlin
val sheetState = rememberModalBottomSheetState()
val scope = rememberCoroutineScope()
outcome?.let {
    OutcomeSheet(outcome = it, onDismissRequest = { outcome = null }, style = OutcomeSheetStyle.Receipt, sheetState = sheetState)
}
fun close() = scope.launch { sheetState.hide() }.invokeOnCompletion { outcome = null }
```

  `SheetState` is `@ExperimentalMaterial3Api`, so a call that passes it needs the opt-in. The v2.6.0
  signature without `sheetState` is untouched and needs no opt-in, which is why this is an overload and
  not a defaulted parameter (an experimental type in a signature demands the opt-in on every call).

## 6. (Optional) The component gallery

The library ships a self-launching `ExplorerActivity` (declared in its own `AndroidManifest.xml` as
`.explorer.ExplorerActivity`, `exported=false`, `singleTop`) that browses the whole catalog with
per-component States / Variants / Playground pages. Manifest-merge pulls it in automatically; launch
it explicitly (`Intent` to `…explorer.ExplorerActivity`) if you want the gallery in-app.

---

## Notes & gotchas

- **Bindings-only, not an app.** The library never calls `@HiltAndroidApp`/`@AndroidEntryPoint` — that
  is *your* job (§3). Adding one to the library would be wrong (a library owns no application).
- **Compose version skew is a runtime failure, not a compile failure.** A mismatched BOM compiles fine
  and crashes at composition — align the BOM (§4) and, if in doubt, check
  `./gradlew :app:dependencies | grep androidx.compose` for a single resolved Compose version.
- **The package is `io.github.ygaray.yahirandroidtaste`.** Import composables from that root.
- **Bumping to a new library version is human-gated** (see `CLAUDE.md` / `ECOSYSTEM.md` §7): change the
  coordinate, `--refresh-dependencies` + resolve-confirm, rebuild, re-verify on-device before shipping.
