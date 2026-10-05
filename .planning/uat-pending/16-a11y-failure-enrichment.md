### Phase 16 — a11y-failure-enrichment (v2.5)

- **Status:** `pending`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v2.5 (Voice UI Localization & Accessibility)
- **Gate 1 self-UAT log:** [`.planning/phases/16-a11y-failure-enrichment/16-03-SELF-UAT.md`](phases/16-a11y-failure-enrichment/16-03-SELF-UAT.md) — Verdict: **SC1-SC4 PASS, SC5 PARTIAL (one owner API-contract call)** (device Samsung SM-S908U tester rig `R5CT10XNKQN`, library AAR md5 `2a7ce56547374776d5f5583031b80597` @ `118af62`, 2026-10-05). Rung semantics, role color, body-in-surface and merged-node prefix observed on the real device via a throwaway harness activity; 114 targeted unit tests + apiCheck green.
- **Items covered (5 ROADMAP success criteria):**
  - **SC1 — VA11Y-01 ApproachLadderCard rung size + selected semantics.** Selectable-cap rows are 48 dp, checkable, the cap rung `checked`, a stale id checks none; cap-less rows stay compact and non-checkable; tapping moves the selection.
  - **SC2 — VFAIL-01 FailureActionUiModel.role.** Default renders the plain Neutral button; Destructive renders red text; Save renders a filled primary button; callback still fires.
  - **SC3 — VFAIL-02 Failure.body.** Slot renders inside the error surface between handled-by and the action; null body leaves the v2.4 layout.
  - **SC4 — VFAIL-03 Failure.semanticsPrefix.** Surface merged node `content-desc` = `"<prefix> <reason>"`, action stays its own node; null prefix adds no description.
  - **SC5 — strictly additive.** Tests + apiCheck green, named-arg consumer call shapes compile. PARTIAL: trailing-lambda `FailureActionUiModel("x") { ... }` no longer compiles (lambda binds to the new last `role` param).
- **Owner how-to-verify (run at milestone completion):**
  1. Read the Gate-1 log above for per-criterion evidence and `16-03-evidence/` (dumps, downscaled captures, harness source `harness-Phase16Activity.kt.txt`).
  2. TalkBack: open the sheet-2 layout (`am start -n <harness>/.Phase16Activity --ei sheet 2`) and listen. Confirm the announcement starts with the prefix, and decide whether the handled-by tier ("Cloud") and the body text are still read or are dropped by the merged `contentDescription`.
  3. Visual taste: the selectable rung pitch (58 dp vs 34 dp cap-less) and the Destructive (red text) / Save (filled) role looks.
  4. Decide the `FailureActionUiModel` trailing-lambda source break: accept it (document in Phase 18 DOC-02 / API.md) or route to gap-closure.
- **Note:** No schema or persistence. "Strictly additive" holds at the Metalava/named-argument level; binary compatibility is not asserted (consumers recompile on repin). Real consumers on this host (CalTracker, SecondBrain) use named `label =, onClick =` arguments and are unaffected.
