# Phase 6 — Deferred Items

Out-of-scope discoveries logged during execution, per the executor's scope-boundary rule
(only auto-fix issues directly caused by the current task's changes).

## 06-01

- **`./gradlew detekt` pre-existing finding in `TextCard.kt`** (`CyclomaticComplexMethod`, 25/25,
  `TextCard.kt:132`). `TextCard.kt` is not in this plan's `files_modified` list and was last
  touched 2026-09-07 (well before this phase) — unrelated to the `DateTimePicker` forward-port.
  Confirmed via the detekt Complexity Report: "1 number of total code smells" total, all
  attributable to this one pre-existing finding; the 3 files this plan touches
  (`component/DateTimePicker.kt`, `explorer/PickersFamilyScreen.kt`,
  `explorer/DomainVocabularyDriftGuardTest.kt`) introduce zero new detekt findings. Not fixed —
  out of this plan's scope. Left for a future dedicated detekt-cleanup task if the owner wants
  zero-baseline restored at the whole-repo level.

- **`./gradlew build`'s `metalavaCheckCompatibilityDebug` fails** with "Removed class
  `io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryStore_Factory`". This is the
  already-documented, already-tracked **`KI-2026-09-02-01`** known issue (see
  `.planning/KNOWN-ISSUES.md` and `.planning/STATE.md` Blockers/Concerns) — a Dagger-generated
  factory class leaked into the committed `api.txt` baseline, unrelated to osmdroid/lifecycle
  dependency admission. Confirmed unrelated: `./gradlew build -x detekt
  -x metalavaCheckCompatibilityDebug -x metalavaCheckCompatibilityRelease` (i.e. excluding both
  this task's pre-existing findings) is BUILD SUCCESSFUL, including full `assemble` + `test` +
  `lint` + `check` — proving the two new Gradle dependencies (`osmdroid-android`,
  `androidx-lifecycle-runtime-compose`) resolve and compile without error. Not fixed here — it is
  already tracked as a pre-Phase-9 (`SHIP-01` tag-cut gate) blocker, out of this plan's scope.
  status: acknowledged

## 06-03

- **`./gradlew detekt` re-confirmed pre-existing finding in `TextCard.kt`** (same
  `CyclomaticComplexMethod` finding logged in 06-01/06-02, unchanged). `./gradlew detekt`'s own
  Complexity Report again shows "1 number of total code smells" total after restoring/registering
  the `PlaceMapPicker` cluster — zero new findings from this plan's touched files
  (`component/PlaceMapPicker.kt`, `component/PlaceMapOsmdroidConfig.kt`,
  `component/PlaceMapPickerModel.kt`, `model/SavedPlaceUiModel.kt`,
  `test/component/SourceContractTestSupport.kt`, `explorer/PickersFamilyScreen.kt`,
  `explorer/DomainVocabularyDriftGuardTest.kt`). Not fixed — out of this plan's scope, same
  disposition as 06-01/06-02.
- **`metalavaCheckCompatibilityRelease` (the variant `apiCheck` actually runs) is green** —
  `./gradlew apiDump && ./gradlew apiCheck` both succeeded, confirming `api.txt` is net-additive
  (only `DateTimePickerKt`/`PlaceMapPickerKt`/`PresetChipKt` added, zero removals). The
  already-tracked `KI-2026-09-02-01` (`metalavaCheckCompatibilityDebug`, the `build`-task variant)
  was not re-triggered by this plan's targeted `apiDump`/`apiCheck` run — remains a Phase 9
  (`SHIP-01`) blocker, unrelated to and unaffected by this plan.
  status: acknowledged
