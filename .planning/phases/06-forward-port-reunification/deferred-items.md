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
