# Phase 13: Catalog integrity & v2.4.0 ship - Pattern Map

**Mapped:** 2026-09-30
**Files analyzed:** 5 (4 doc files edited in place + 1 evidence-capture artifact)
**Analogs found:** 5 / 5 (all edits are in-place corrections to existing files; "analog" = the file's own surrounding prose/section, since no new file is authored)

This phase authors **no new composables or source files** (CAT-01/INV-01 already satisfied per RESEARCH.md). The only "files to create/modify" are: 4 existing doc files (text correction) and one new evidence-capture artifact (verification log, not source code). There is no controller/service/component pattern to map — the relevant patterns are **doc house-style** (prose conventions, verified-tag conventions) and **the existing shell script's output-format convention** for evidence capture.

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `CLAUDE.md` (line 35, "seven" → "ten") | config/doc | transform (text edit) | itself — surrounding bullet list prose, same file | exact |
| `README.md` (lines 21, 59-60, 73, 79) | config/doc | transform (text edit) | itself — "surface at a glance" section + usage prose | exact |
| `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` (KDoc lines 88, 92) | config/doc (KDoc) | transform (text edit) | itself — KDoc block above `val entries` | exact |
| `API.md` (lines 1, 7-11, 27) | config/doc | transform (text edit) | itself — header + "Surface at a glance" table | exact |
| New evidence artifact (e.g. `tools/` output log or plan-embedded verification transcript) | utility/test-evidence | batch (one-shot verification capture) | `tools/verify-api-additive.sh` (output format: `API-ADDITIVE PASS:`/`FAIL (lane N):`/`SKIP:` prefixed echo lines to stderr/stdout, `exit N` codes) | role-match |

## Pattern Assignments

### `CLAUDE.md` — doc-drift correction (role: config/doc, flow: transform)

**Analog:** itself, `CLAUDE.md` lines ~87-93 (`ComponentRegistry is the single source of truth` bullet)

**Current text to replace** (verified live, lines 87-93):
```markdown
- **`ComponentRegistry` is the single source of truth + a drift guard.** Every public top-level
  `@Composable` in the visual packages (`component/`, `feedback/`, `modifier/`, `theme/`) must be
  **registered** in one of the seven family lists XOR **allowlisted** in
  `INTENTIONALLY_UNREGISTERED` — never neither, never both. The registry's integrity test (and the
  CATALOG drift guard) fails the build otherwise. When you add a public component, register it in
  its family screen's entries list; when you add a private sub-part, no action needed.
```
**Edit:** change `one of the seven family lists` → `one of the ten family lists`. House style: bold lead term per bullet (`**registered**`, `**allowlisted**`), terse declarative sentences, backtick-quoted identifiers. Preserve exactly — only the numeral/word changes.

---

### `README.md` — doc-drift correction, 4 locations (role: config/doc, flow: transform)

**Analog:** itself — "Docs for agents & integrators" list, numbered usage steps, code comment, "surface at a glance" bullet list.

**Location 1** (line 21):
```markdown
- **[`API.md`](API.md)** — the full public surface, organized as the seven-family composable catalog.
```
→ `seven-family` → `ten-family`.

**Location 2** (lines 59-60):
```markdown
2. **Call components directly.** They are plain public `@Composable` functions grouped into seven
   families — see **[`API.md`](API.md)** for the catalog and the key parameters each one takes.
```
→ `seven` → `ten`.

**Location 3** (line 73, inside a fenced kotlin code comment):
```kotlin
    // …the full seven-family catalog is enumerated in API.md
```
→ `seven-family` → `ten-family`.

**Location 4** (line 79, "surface at a glance" — the roster bullet, NOT a simple word-swap per RESEARCH.md Pitfall 3):
```markdown
- **Seven component families:** cards, chips, sheets, buttons/FAB, pickers, feedback, empty-state.
- **41 registered public composables** in `ComponentRegistry` (the single source of truth that
  drives the gallery and the CATALOG drift guard), plus **4 intentionally-unregistered** structural
  sub-parts (`CardBase`, `WaveformCanvas`, `SwipeableActionRow`, `YahirAndroidTasteTheme`) — 45
  public composables total. The exact per-family enumeration lives in **[`API.md`](API.md)**.
```
**Edit:** `Seven component families:` → `Ten component families:`, and the roster list must be extended to include the 3 missing families (Progress/Metrics, Tactile Foundation, Voice Command) — per D-03/Pitfall 3, the family NAME wording is in scope even though the composable-COUNT numbers (`41`, `4`, `45`) are explicitly OUT of scope (Pitfall 4) and must be left untouched unless the planner deliberately widens scope as a flagged discretionary addition.

House style for this doc: bold lead term, en-dash separated clauses, cross-reference links in backtick+markdown-link form (`**[\`API.md\`](API.md)**`).

---

### `ComponentRegistry.kt` KDoc — doc-drift correction (role: config/doc (KDoc), flow: transform)

**Analog:** itself, KDoc block directly above `val entries` (verified live, lines 87-93 above `val entries: List<Entry> = cardsFamilyEntries + ... + voiceCommandFamilyEntries`):
```kotlin
    /**
     * One entry per showcaseable public `@Composable`, authored in a fixed, deterministic order
     * (family order matches [ExplorerFamilies.ORDERED_KEYS]; within a family, declaration order
     * matches the family screen's rendered section order) so registry iteration is reproducible
     * across runs (EDGE ordering).
     *
     * D-05 (Phase 62 Plan 02): the seven per-family lists below are each declared in their own
     * family screen file (`cardsFamilyEntries` in `CardsFamilyScreen.kt`, etc.) rather than
     * inline here, so family-content plans (03-05) can author `states`/`content` on disjoint
     * files without touching this shared registry file. This concatenation — reproducing the
     * exact prior declaration order — is the only place the seven lists are combined.
     */
```
**Edit:** both occurrences of `seven` (line 88: "the seven per-family lists", line 92: "the seven lists are combined") → `ten`. Note the `entries` concatenation beneath this KDoc is **already correct** (already ends `+ voiceCommandFamilyEntries`, ten terms) — only the KDoc prose lags. Preserve the `D-05 (Phase 62 Plan 02)` historical citation verbatim; do not touch it beyond the numeral/word.

---

### `API.md` — doc-drift correction, header + table (role: config/doc, flow: transform)

**Analog:** itself, header line + "Surface at a glance" table (verified live, lines 1-27).

**Location 1** (line 1, title):
```markdown
# API.md — `yahirandroidtaste` public surface (the nine-family composable catalog)
```
→ `nine-family` → `ten-family`.

**Location 2** (lines 6-11, prose + concatenation listing — must gain the missing `voiceCommandFamilyEntries` term, matching Pitfall 3's finding that this listing is short one term even before the family-count fix):
```markdown
This is a **UI component library**, so its public surface is a **catalog of composables**, not a
service seam. The composables are organized into the library's **nine families** — the same
taxonomy the library ships in `explorer/ComponentRegistry.kt` (`cardsFamilyEntries +
chipsFamilyEntries + sheetsFamilyEntries + buttonsFabFamilyEntries + pickersFamilyEntries +
feedbackFamilyEntries + emptyStateFamilyEntries + progressFamilyEntries +
tactileFoundationFamilyEntries`), which is the single source of truth and the CATALOG drift guard.
```
**Edit:** `nine families` → `ten families`; append `+ voiceCommandFamilyEntries` to the parenthetical concatenation listing so it matches the live `ComponentRegistry.kt` code exactly (copy the real concatenation from `ComponentRegistry.kt` lines 94-103, shown in RESEARCH.md Code Examples, as the source of truth for this listing's exact term order).

**Location 3** (lines 13-23, "Surface at a glance" table — add a tenth row):
```markdown
| Family | Registered composables | What it is |
|--------|-----------------------|------------|
| 1. Cards | 9 | ... |
...
| 9. Tactile Foundation | 4 | Elevation ladder, Space Grotesk display ramp, gradient/tint accent surfaces, and the Heat relatedness ramp |
```
**Edit:** add `| 10. Voice Command | 5 | ... |` row (5 composables: `ProviderKeyCard`, `ModelSelectCard`, `ApproachLadderCard`, `OutcomeSheet`, `ClarificationBar` per RESEARCH.md's verified list) using the same table-row prose style as rows 1-9 (short noun-phrase description, no trailing period).

**Location 4** (line 27, totals sentence — composable COUNT numbers are OUT OF SCOPE per Pitfall 4; only `nine families` → `ten families` wording, if present in this sentence, is in scope):
```markdown
**51 registered public composables** across the nine families, plus **5 intentionally-unregistered**
structural sub-parts (see the end of this doc) = **56 public composables total**.
```
**Edit:** `nine families` → `ten families` only. Leave `51`/`5`/`56` untouched per D-03's literal scope (composable counts are a separate staleness the phase does not fix) — unless the planner explicitly flags widening scope.

---

### Evidence-capture artifact — API-01 additive proof (role: utility/test-evidence, flow: batch)

**Analog:** `tools/verify-api-additive.sh` (read in full) — this is the project's existing convention for how verification results get reported, even though the authoritative check for THIS phase is the Metalava swap-baseline technique (RESEARCH.md Code Examples), not this script directly.

**Output-format pattern to mirror** (verified, `tools/verify-api-additive.sh` lines 1-32):
```bash
#!/usr/bin/env bash
# verify-api-additive.sh — the .api append-only lane signal.
# Exit 0: current .api is an append-only superset of the baseline .api (lane 1/2 for API).
# Exit 3: a public-API line was removed/renamed/re-signatured (lane-3 API break).
...
echo "API-ADDITIVE PASS: +$added new public symbol line(s), 0 removed (append-only)"
exit 0
```
Key conventions to reuse when the plan persists its own evidence (whether as a plan-embedded transcript or a new small script): (1) a leading comment banner describing exit-code semantics, (2) a single-line `PREFIX: verdict` echo (`API-ADDITIVE PASS/FAIL/SKIP:`) rather than verbose prose, (3) explicit `set -euo pipefail` + `git rev-parse --show-toplevel` cd-to-root guard for any new script, (4) distinguishing a real failure from a "degraded/skipped" check via a named condition rather than silent exit-0.

**Swap-baseline procedure the evidence must record** (RESEARCH.md Code Examples section, already verified this session — reuse verbatim as the plan's procedure, not a new invention):
```bash
./gradlew apiCheck                      # Step 1: local sync check
cp api.txt /tmp/api.txt.current.bak
git show v2.3.0:api.txt > api.txt
./gradlew apiCheck                      # Step 2: TRUE v2.3.0-compat verdict (authoritative)
cp /tmp/api.txt.current.bak api.txt
git status --short api.txt              # MUST be empty — confirms restore
```

## Shared Patterns

### Doc house style (applies to all 4 doc-drift files)
**Source:** the files themselves (`CLAUDE.md`, `README.md`, `API.md`, `ComponentRegistry.kt` KDoc)
**Apply to:** every text edit in this phase
- Bold the lead term/number in a bullet (`**registered**`, `**Ten component families:**`, `**ten families**`).
- Numerals spelled as words in prose for small family counts ("seven"/"nine"/"ten"), consistent with existing usage — do not switch to digit form ("10 families") when correcting.
- Cross-doc references use `**[\`File.md\`](File.md)**` markdown-link form with backticks inside brackets.
- KDoc blocks cite the originating phase/plan when explaining a structural decision (e.g. `D-05 (Phase 62 Plan 02)`) — preserve these citations verbatim when editing adjacent prose.

### Verification-evidence capture (API-01, CAT-01, INV-01)
**Source:** `tools/verify-api-additive.sh`, RESEARCH.md Validation Architecture section
**Apply to:** the phase's evidence-capture task
- Use the existing Gradle tasks (`testDebugUnitTest`, `apiCheck`, `detekt`) exactly as already wired — do not write new Gradle tasks.
- For API-01 specifically, the shell script's raw exit code is a KNOWN false positive for this version range (ClearableTextField's additive optional params) — do not treat `tools/verify-api-additive.sh`/`classify-hub-change.sh`'s LANE 3 as ground truth; use the swap-baseline Metalava technique above as the authoritative evidence, and may cite the script's known false positive as corroborating context (not as the proof itself).

## No Analog Found

None — every file in scope is an in-place edit to an existing, already-read file; there is no new source file requiring an external analog search.

## Metadata

**Analog search scope:** repo root docs (`CLAUDE.md`, `README.md`, `API.md`), `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt`, `tools/verify-api-additive.sh`
**Files scanned:** 5 (all read directly, no Glob/Grep search needed since CONTEXT.md/RESEARCH.md already named exact file+line locations)
**Pattern extraction date:** 2026-09-30
