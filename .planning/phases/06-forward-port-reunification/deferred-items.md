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
