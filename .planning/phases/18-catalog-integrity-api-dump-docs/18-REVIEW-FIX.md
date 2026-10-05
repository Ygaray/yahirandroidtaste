---
phase: 18-catalog-integrity-api-dump-docs
fixed_at: 2026-10-05T00:00:00Z
review_path: .planning/phases/18-catalog-integrity-api-dump-docs/18-REVIEW.md
iteration: 1
findings_in_scope: 5
fixed: 4
skipped: 1
status: resolved
---

# Phase 18: Code Review Fix Report

**Fixed at:** 2026-10-05
**Source review:** .planning/phases/18-catalog-integrity-api-dump-docs/18-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 5
- Fixed: 4
- Skipped: 1 (documented acceptable-skip)

Verification ran in the isolated review-fix worktree (documentation-only edits, so Tier 1 re-read plus
grep checks; no build gates apply). The worktree was fast-forwarded into `main` and removed.
Source claims were re-checked against `ClearableTextField.kt`, `ProviderKeyCard.kt` and
`OutcomeSheet.kt` before editing.

## Fixed Issues

### WR-01: Section 10 claims every user-visible literal is overridable, but ProviderKeyCard has non-overridable English strings

**Files modified:** `API.md`, `INTEGRATION.md`
**Commit:** 6b6641c
**Applied fix:** Weakened "every user-visible literal" in section 10 and listed the `ProviderKeyCard`
reveal / hide toggle and clear control ("Show key", "Hide key", "Clear text") as a second documented
English-only residual alongside the segmented toggles' state words, in both API.md and the
INTEGRATION.md localization subsection. Documentation-only; no defaulted label parameters added
(that would be a hub-gated source change).

### WR-02: ProviderKeyCard row omits behavior that affects callers (trim on every edit, masked by default)

**Files modified:** `API.md`
**Commit:** cfaa0ad
**Applied fix:** Appended to the `ProviderKeyCard` row that the field is masked by default with a
reveal toggle and every edit is trimmed before `onKeyChange` (pure formatting, no validation). Added
"the prefix is trimmed before joining" to the `semanticsPrefix` cell.

### IN-01: Table omits the default for `emptyProvidersReason`

**Files modified:** `API.md`
**Commit:** 57dd18e
**Applied fix:** Listed `emptyProvidersReason = "No providers configured yet"` in the `ProviderKeyCard`
row.

### IN-03: Binary-compat bullet asserts a proof the same section later disclaims

**Files modified:** `API.md`
**Commit:** 75695ea
**Applied fix:** Harmonized the two blocks without asserting a fresh proof. The label-fields bullet
now says the `javap` diff showed zero missing descriptors when the binary-compat fix landed and is
re-proven at the v2.5.0 cut (not asserted as freshly verified for later additions). The Failure/router
block now references that same proof and status. The Phase 19 sentence, the 61/5/66 counts, and the
once-only headings were left unchanged.

## Skipped Issues

### IN-02: Consumer names in the new compatibility paragraph

**File:** `API.md:295-296`
**Reason:** Documented acceptable-skip per orchestrator instruction. It follows the existing v2.3.0
precedent (API.md lines 256-257) and the plan's planner-discipline allowances, and the named-consumer
sweep note is intentional evidence for the trailing-lambda caveat that the plan requires kept intact.
**Original issue:** The paragraph names SecondBrain and CalTracker as swept call sites, which is
consumer-name leakage in a domain-agnostic public doc.

---

_Fixed: 2026-10-05_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
