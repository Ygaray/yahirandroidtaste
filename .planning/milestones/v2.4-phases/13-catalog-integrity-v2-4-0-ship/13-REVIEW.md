---
phase: 13-catalog-integrity-v2-4-0-ship
reviewed: 2026-09-30T00:00:00Z
depth: standard
files_reviewed: 4
files_reviewed_list:
  - CLAUDE.md
  - README.md
  - API.md
  - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt
findings:
  critical: 3
  warning: 2
  info: 0
  total: 5
status: resolved
---

# Phase 13: Code Review Report

**Reviewed:** 2026-09-30T00:00:00Z
**Depth:** standard
**Files Reviewed:** 4
**Status:** issues_found

## Summary

This phase's stated purpose is a verification-and-doc-correction gate: re-verify CAT-01/API-01/INV-01
and correct stale "seven"/"nine" family-count wording to "ten" across `CLAUDE.md`, `README.md`,
`API.md`, and `ComponentRegistry.kt`. The top-line family count ("ten families", "ten family lists")
was indeed corrected consistently across all four files — that part checks out.

However, I counted the actual registered `Entry(...)` declarations in every
`*FamilyScreen.kt` file (the files that back `ComponentRegistry.entries`, concatenated in the exact
order `API.md` itself cites) and cross-checked them against what `API.md` and `README.md` claim.
**The deeper, more load-bearing numbers these docs also advertise — the per-family composable counts
and the total-composable counts — are themselves still wrong, in a way the "seven→ten" fix did not
touch.** `API.md` is this library's sole public-surface contract for consumers (per `CLAUDE.md`'s own
"`ComponentRegistry` is the single source of truth" invariant and this phase's API-01 criterion), so
a catalog that silently omits real public composables is a direct violation of that invariant, not a
cosmetic wording nit.

Actual live counts (verified directly against `*FamilyScreen.kt`, not inferred from doc prose):

| Family | Actual registered | `API.md` table says |
|---|---|---|
| Cards | 11 (`CardBase`, `CardTypeChip` missing from docs) | 9 |
| Chips | 5 (`PresetChip` missing) | 4 |
| Sheets | 18 | 18 (correct) |
| Buttons/FAB | 4 (`MicButton` missing) | 3 |
| Pickers | 6 (`DateTimePicker`, `PlaceMapPicker` missing) | 4 |
| Feedback | 3 | 3 |
| Empty-state | 1 | 1 |
| Progress/Metrics | 4 | 4 |
| Tactile Foundation | 4 | 4 |
| Voice Command | 5 | 5 |
| **Total registered** | **61** | **51** (text) / **55** (table row-sum) |
| **Intentionally unregistered** | **5** (live map in `ComponentRegistry.kt`) | **5** (intro text) vs **6** (section header) |
| **Grand total** | **66** | **56** |

`README.md`'s own totals ("41 registered … 45 public composables total") disagree with both the live
source and with `API.md`'s (also-wrong) numbers — three different documents, three different totals,
none matching the code.

## Critical Issues

### CR-01: `API.md`'s per-family composable tables omit 6 real public composables and its summary counts are internally inconsistent

**File:** `API.md:16-28` (family-count table), `API.md:29-31` (summary text), `API.md:40-169` (per-family detail tables)
**Issue:** The "Surface at a glance" table and the detailed per-family tables for Cards, Chips,
Buttons/FAB, and Pickers undercount the actual registered composables, confirmed directly against
`CardsFamilyScreen.kt`, `ChipsFamilyScreen.kt`, `ButtonsFabFamilyScreen.kt`, and
`PickersFamilyScreen.kt`:
- Cards table lists 9 composables; the live `cardsFamilyEntries` registers 11 — `CardBase` and
  `CardTypeChip` are both real, registered, undocumented public composables.
- Chips table lists 4; live `chipsFamilyEntries` registers 5 — `PresetChip` is undocumented.
- Buttons/FAB table lists 3; live `buttonsFabFamilyEntries` registers 4 — `MicButton` is
  undocumented.
- Pickers table lists 4; live `pickersFamilyEntries` registers 6 — `DateTimePicker` and
  `PlaceMapPicker` are both undocumented (and `DateTimePicker`/`PlaceMapPicker` aren't mentioned
  anywhere else in `API.md` either — a consumer reading this doc has no way to discover these two
  public composables exist).

Additionally, the summary line ("**51 registered public composables** … plus **5
intentionally-unregistered** … = **56 public composables total**") does not even match the table's
own row-sum (9+4+18+3+4+3+1+4+4+5 = **55**, not 51) — the doc contradicts itself before it contradicts
the source. The real live total (counted directly from the ten `*FamilyScreen.kt` files) is **61
registered**, not 51 or 55.

Since `API.md` is the library's sole public-API contract (per `CLAUDE.md`'s "`ComponentRegistry` is
the single source of truth" invariant) and this phase's explicit job was to re-verify API-01, shipping
with 6 undocumented public composables and self-contradicting totals is a correctness failure of
exactly the property this phase claims to certify.
**Fix:** Regenerate the per-family tables and summary counts directly from
`ComponentRegistry.entries` (e.g. a small generator/test asserting `API.md`'s row counts equal each
family's live `entries` slice size, so this can't silently drift again) rather than hand-maintaining
the numbers. At minimum, add rows for `CardBase`, `CardTypeChip`, `PresetChip`, `MicButton`,
`DateTimePicker`, and `PlaceMapPicker`, and correct the summary line to 61 registered / 5
unregistered / 66 total.

### CR-02: `API.md`'s "Intentionally-unregistered" section miscounts itself and lists `CardBase` as unregistered when it is actually registered

**File:** `API.md:172` (section header), `API.md:29` (intro text), `API.md:177-184` (table)
**Issue:** The section header reads "## Intentionally-unregistered sub-parts (**6**)" while the
earlier summary text says "**5** intentionally-unregistered structural sub-parts" — these two numbers
in the same document disagree. The live source of truth, `ComponentRegistry.INTENTIONALLY_UNREGISTERED`
in `ComponentRegistry.kt:110-133`, has exactly **5** keys (`WaveformCanvas`, `SwipeableActionRow`,
`RevealActionRow`, `YahirAndroidTasteTheme`, `SheetHeaderMenu`) — so "5" is right and "(6)" is wrong.
Worse, the table under that header lists `CardBase` as one of the 6 unregistered rows, but `CardBase`
is **not** in `INTENTIONALLY_UNREGISTERED` — it is registered in `cardsFamilyEntries`
(`CardsFamilyScreen.kt:85`, `name = "CardBase"`), which also matches `ComponentRegistry.kt`'s own
KDoc history (lines 27-31: "Phase 129 … moves `CardBase` from `INTENTIONALLY_UNREGISTERED` into
`entries`"). Documenting `CardBase` as unregistered directly contradicts the actual registry's XOR
invariant that `CLAUDE.md` calls load-bearing ("registered XOR allowlisted — never neither, never
both").
**Fix:** Drop the stale `CardBase` row from the unregistered table, fix the header to "(5)", and
reconcile it with the "5" already stated in the intro paragraph.

### CR-03: `README.md`'s composable totals are wrong and disagree with `API.md`'s (also wrong) numbers

**File:** `README.md:81-84`
**Issue:** `README.md` states "**41 registered public composables** … plus **4
intentionally-unregistered** structural sub-parts (`CardBase`, `WaveformCanvas`,
`SwipeableActionRow`, `YahirAndroidTasteTheme`) — **45 public composables total**." This is wrong on
every count against the live source checked for this review: actual registered = 61, actual
unregistered = 5 (not 4 — `SheetHeaderMenu` is missing from this list entirely, and `CardBase` is
listed here as unregistered while it is actually registered, same error as CR-02), actual total = 66.
It also disagrees with `API.md`'s own (separately wrong) "51 / 5 / 56" — three documents in this
phase's own scope state three different, and all incorrect, totals for the same underlying fact.
Since correcting stale catalog-count wording is this phase's explicit deliverable, this file was not
actually brought in line with the live registry.
**Fix:** Update to the live counts (61 registered / 5 unregistered / 66 total) and list all 5
unregistered sub-parts (`WaveformCanvas`, `SwipeableActionRow`, `RevealActionRow`,
`YahirAndroidTasteTheme`, `SheetHeaderMenu`), dropping `CardBase` from that list.

## Warnings

### WR-01: `README.md` states a stale Compose BOM version that contradicts `CLAUDE.md` and the actual build config

**File:** `README.md:90`
**Issue:** `README.md`'s Requirements section reads "Compose BOM **2026.02.01**", but `CLAUDE.md:66`
states "Compose BOM **2026.04.01**", and `gradle/libs.versions.toml:12` (`composeBom = "2026.04.01"`)
confirms `2026.04.01` is the actual value in use. `README.md` is the doc a new consumer reads first,
and its own next line (line 91-92) tells the consumer "a Compose BOM aligned with the library's" is a
hard integration prerequisite — shipping the wrong version here can cause a consumer to align to the
wrong BOM and hit real Compose compiler/runtime mismatches.
**Fix:** Update `README.md:90` to `Compose BOM 2026.04.01` to match `CLAUDE.md` and
`gradle/libs.versions.toml`.

### WR-02: `ComponentRegistry.kt`'s own KDoc states counts that no longer match its live `entries`/`INTENTIONALLY_UNREGISTERED`, and nothing enforces it

**File:** `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt:18-31`
**Issue:** The class-level KDoc asserts "**53 registered, 4 intentionally unregistered = 57 total**
public composables" as a figure "[cross-checked against] `ComponentRegistryDriftGuardTest`, which
stays green." That cross-check is misleading: the drift guard test (and the `init` block at
`ComponentRegistry.kt:135-157`) only asserts the *membership* invariant (no duplicates, no XOR
overlap, non-blank reasons) — it does not assert the specific numbers `53`/`4`/`57` written into the
comment. Those numbers are now stale against the live file content (actual: 61 registered per the
ten `*FamilyScreen.kt` files, 5 unregistered per `INTENTIONALLY_UNREGISTERED`, 66 total) — this
comment has silently drifted since Phase 129 (2026-08-28) and nothing would catch it drifting further.
**Fix:** Either update the comment to the current 61/5/66, or — better, matching the fix proposed for
CR-01 — replace the hand-maintained count assertion with a computed one
(`entries.size`/`INTENTIONALLY_UNREGISTERED.size`) referenced from the KDoc rather than a literal
number that can only be correct on the day it's written.

---

_Reviewed: 2026-09-30T00:00:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
