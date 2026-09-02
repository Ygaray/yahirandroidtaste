# Human UAT Pending

## Entries

### Phase 1 — tier-legibility (v1.0)

- **Status:** `pending`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v1.0 (Hub Stewardship — Tier Legibility → Coherence Audit → Governance → Repin
  Bookkeeping → Gardening)

- **Gate 1 self-UAT log:** [`.planning/phases/01-tier-legibility/01-05-SELF-UAT.md`](phases/01-tier-legibility/01-05-SELF-UAT.md) — Verdict: **ALL 1 outstanding criteria PASS** (device Samsung SM-S908U / yahirs-s22-ultra-2 (R5CT10XNKQN), library AAR md5 `18e493e666f8bcaeed23b2c22953fadb` @ `87561ff`, 2026-09-02). All 4 ROADMAP success criteria are now backed by live evidence: 3 (LEG-01 code/wiring + LEG-02 doc) were already `VERIFIED` by `01-VERIFICATION.md`'s static/compile/test evidence; the 1 remaining `human_verification` item (on-device visual confirmation of the Primitive/Pattern `TierBadge` on both gallery surfaces) is closed by this Gate-1 self-UAT run on real hardware.
- **Items covered (1 outstanding ROADMAP success criterion / human_verification item):**
  - **SC2 — `ExplorerActivity` gallery displays each component's tier on its detail/list view (the on-device visual sub-claim).** Confirmed on real hardware, both `ComponentRow` (family lists + index search results) and `ComponentDetailScreen`'s `TopAppBar`, both light and dark theme, for the longest registered names (`RecordingBottomSheetContent` 28 ch, `SegmentedOptionSelector` 23 ch) and a short name (`AppChip`). The badge never clips/pushes off-row on the list surface; on the detail `TopAppBar` the **component name truncates with an ellipsis, never the badge**, when the two don't fit; Primitive (`secondaryContainer`) vs. Pattern (`tertiaryContainer`) badge colors are visually distinguishable in both themes. Note: the `uiautomator` accessibility-tree dump reported the full un-truncated name string even where the rendered pixels showed a real ellipsis-truncation — only the screenshot (rung 5) caught this; a structure-tree-only check would have been a false pass on this exact criterion.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `01-05-SELF-UAT.md` above for the full per-surface/per-theme evidence trail (screenshot descriptions + `uiautomator` bounds cross-checks).
  2. Repro on-device: since `yahirandroidtaste` is a pure library with no installable APK of its own, either (a) sideload SecondBrain/CalTracker once each has repinned to a tag containing this phase's tier wiring and open their in-app gallery entry point, or (b) build the same throwaway same-package-Intent harness described in the SELF-UAT log's "Driver-mechanism note" (`publishReleasePublicationToMavenLocal` + a 5-line `MainActivity` that Intents into `ExplorerActivity`). Browse to the Sheets family (has the 28-char longest name) and toggle dark theme via the detail screen's theme-toggle action.
- **Note:** No schema/DB involved (phase is UI-only). This phase's Gate-1 run also surfaced a
  reusable gap: no project-local `AGENT-DEVICE-TESTING.md` exists yet for this library-only repo
  (global template assumes an installable app). Recommend authoring one in a later phase so future
  Gate-1 runs don't re-derive the harness pattern from scratch — not a blocker for this sign-off.

### Phase 2 — coherence-audit (v1.0)

- **Status:** `pending`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v1.0 (Hub Stewardship — Tier Legibility → Coherence Audit → Governance → Repin
  Bookkeeping → Gardening)

- **Gate 1 self-UAT log:** [`.planning/phases/02-coherence-audit/02-02-SELF-UAT.md`](phases/02-coherence-audit/02-02-SELF-UAT.md) — Verdict: **ALL 4 criteria PASS** (documentation-content verification; no device/app surface in scope — this phase shipped zero `.kt` changes, confirmed via `git diff` across the full phase commit range, only `docs/COHERENCE-AUDIT.md`, 2026-09-02).
- **Items covered (4 ROADMAP success criteria, AUD-01):**
  - **SC1 — All 9 registered families enumerated.** Confirmed: `docs/COHERENCE-AUDIT.md` has one section per family (Cards, Chips, Sheets, Buttons/FAB, Pickers, Feedback, Empty State, Progress/Metrics, Tactile Foundation), matching `ComponentRegistry.kt`.
  - **SC2 — Overlaps/near-duplicates/altitude mismatches flagged.** Confirmed: 9 named findings (C-1, C-2, CH-1, CH-2, CH-3, S-1, S-2, PK-1, T-1) plus explicit no-finding statements for families with none.
  - **SC3 — Every flagged finding carries a documented disposition (unify/keep-with-rationale/prune).** Confirmed: 13 disposition statements, each with cited rationale; no finding left undispositioned.
  - **SC4 — "Unify" dispositions form a concrete, actionable list.** Confirmed: self-contained "Unify Work-Order" section with WO-1 (`FilterBar`→`ChipBar`) and WO-2 (shared sheet-chrome extraction), each with components/rationale/blast-radius, ready for Phase 5 to execute against.
- **Owner how-to-verify (run at milestone completion):**
  1. Read `.planning/phases/02-coherence-audit/02-02-SELF-UAT.md` above for the full evidence trail (grep commands + line ranges cited per criterion).
  2. Read `docs/COHERENCE-AUDIT.md` directly and independently judge the *editorial* calls behind each disposition — is `ChipBar`/`FilterBar` genuinely worth unifying, is `CardQuickView` genuinely a distinct purpose from the `CardBase` group, etc. This is the one part of this phase that is inherently a human architectural-taste call, not a mechanical check; Gate-1 confirmed the audit's structural completeness and internal consistency, not the correctness of its architectural opinions.
- **Note:** No device/app behavior shipped this phase — pure documentation deliverable
  (`docs/COHERENCE-AUDIT.md`), zero `.kt` files touched. A separate static/code-level verification
  (`.planning/phases/02-coherence-audit/02-VERIFICATION.md`, `gsd-verifier`) independently
  re-derived all 53 registry entries' name+tier from source and cross-checked all 4 blast-radius
  grep counts against both consumer repos — this Gate-1 pass corroborated that with its own
  independent spot-check rather than trusting it outright. `.planning/REQUIREMENTS.md`'s AUD-01
  checkbox/traceability bookkeeping was still unchecked as of this pass (info-level, non-blocking,
  flagged for whoever closes this phase).
