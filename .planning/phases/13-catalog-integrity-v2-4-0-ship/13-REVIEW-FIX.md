---
phase: 13-catalog-integrity-v2-4-0-ship
fixed_at: 2026-10-01T05:40:00Z
review_path: .planning/phases/13-catalog-integrity-v2-4-0-ship/13-REVIEW.md
iteration: 1
findings_in_scope: 5
fixed: 5
skipped: 0
status: all_fixed
---

# Phase 13: Code Review Fix Report

**Fixed at:** 2026-10-01T05:40:00Z
**Source review:** .planning/phases/13-catalog-integrity-v2-4-0-ship/13-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 5 (3 critical, 2 warning, 0 info)
- Fixed: 5
- Skipped: 0

Before applying any fix, every reviewer-cited number was independently re-verified against live
HEAD source (not copied from REVIEW.md): counted `Entry(...)` declarations in each of the ten
`*FamilyScreen.kt` files, read `ComponentRegistry.kt`'s `INTENTIONALLY_UNREGISTERED` map directly,
and confirmed `gradle/libs.versions.toml`'s `composeBom` value. All reviewer-reported counts
(61 registered / 5 unregistered / 66 total) matched the live source exactly — no drift between
review time and fix time.

## Fixed Issues

### CR-01: `API.md`'s per-family composable tables omit 6 real public composables and its summary counts are internally inconsistent

**Files modified:** `API.md`
**Commit:** `8b20e76`
**Applied fix:** Corrected the "Surface at a glance" table's per-family counts (Cards 9→11, Chips
4→5, Buttons/FAB 3→4, Pickers 4→6) and the summary line (51/5/56 → 61/5/66). Added missing rows to
the detail tables, in the exact live registration order, for `CardBase` and `CardTypeChip` (Cards),
`PresetChip` (Chips), `MicButton` (Buttons/FAB), and `DateTimePicker`/`PlaceMapPicker` (Pickers) —
each row's signature was read directly from the composable's own source file, not guessed from the
reviewer's prose.

### CR-02: `API.md`'s "Intentionally-unregistered" section miscounts itself and lists `CardBase` as unregistered when it is actually registered

**Files modified:** `API.md`
**Commit:** `5ba9463`
**Applied fix:** Changed the section header from "(6)" to "(5)" and removed the stale `CardBase`
row from the unregistered table (confirmed live: `CardBase` is registered in
`cardsFamilyEntries`, not in `INTENTIONALLY_UNREGISTERED`). The intro paragraph's "5" was already
correct and needed no change.

### CR-03: `README.md`'s composable totals are wrong and disagree with `API.md`'s (also wrong) numbers

**Files modified:** `README.md`
**Commit:** `522196d`
**Applied fix:** Updated "41 registered ... 4 intentionally-unregistered ... 45 total" to
"61 registered ... 5 intentionally-unregistered ... 66 total", and corrected the named
unregistered list to all 5 live entries (`WaveformCanvas`, `SwipeableActionRow`,
`RevealActionRow`, `YahirAndroidTasteTheme`, `SheetHeaderMenu`), dropping the stale `CardBase`
entry.

## Warnings — Fixed

### WR-01: `README.md` states a stale Compose BOM version that contradicts `CLAUDE.md` and the actual build config

**Files modified:** `README.md`
**Commit:** `351f7ef`
**Applied fix:** Changed "Compose BOM 2026.02.01" to "Compose BOM 2026.04.01", matching both
`CLAUDE.md:66` and `gradle/libs.versions.toml`'s live `composeBom = "2026.04.01"`.

### WR-02: `ComponentRegistry.kt`'s own KDoc states counts that no longer match its live `entries`/`INTENTIONALLY_UNREGISTERED`, and nothing enforces it

**Files modified:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt`
**Commit:** `247225c`
**Applied fix:** Updated the class-level KDoc's stale "53 registered, 4 intentionally unregistered
= 57 total" to the live "61 registered, 5 intentionally unregistered = 66 total", re-dated to this
phase, and removed the misleading claim that `ComponentRegistryDriftGuardTest` "cross-checks" these
specific numbers (it only enforces the membership/XOR invariant, not literal counts — per the
reviewer's own finding). The revised comment now explicitly tells a future reader to prefer reading
`entries.size`/`INTENTIONALLY_UNREGISTERED.size` directly over trusting this prose.

**Note on commit process:** this repo's pre-commit hook (`tools/classify-hub-change.sh`) classifies
any rewrite of a pre-existing line inside `src/main/**` as a "lane 2" (non-additive) change
requiring the documented `HUB_LANE_OVERRIDE=2` escape hatch — it cannot distinguish a KDoc comment
reword from a behavioral code change. This commit used that override, since the change is verified
comment-only: `ComponentRegistryDriftGuardTest` and `DomainVocabularyDriftGuardTest` were run and
confirmed green both before and after the edit (and again after all 5 fixes landed, as a final
sanity pass).

## Skipped Issues

None — all 5 in-scope findings were fixed.

---

_Fixed: 2026-10-01T05:40:00Z_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
