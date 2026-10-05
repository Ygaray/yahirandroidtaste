### Phase 17 — approachladdercard-router-on-off-toggle (v2.5)

- **Status:** `pending`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v2.5 (Voice UI Localization & Accessibility)
- **Gate 1 self-UAT log:** [`.planning/phases/17-approachladdercard-router-on-off-toggle/17-01-SELF-UAT.md`](phases/17-approachladdercard-router-on-off-toggle/17-01-SELF-UAT.md) — Verdict: **ALL 5 criteria PASS** (device Samsung SM-S908U tester rig R5CT10XNKQN, library AAR md5 `ac172bfed07386fabb11a92f67f6f7de` @ `5b0de39`, harness APK md5 `2ec074296d3f4627346e6c5ee26f0681`, 2026-10-05). Router toggle driven in the real Explorer gallery (light + dark) and a direct-call harness: state reflected, emitted values read back, null hides it, both half-pair directions throw on-device, rung tap selects only the cap.
- **Items covered (5 ROADMAP success criteria + decisions):**
  - **SC1 — VAPPR-04 router toggle renders and reflects state.** With `router` + `onRouterChange` set, a Router ON/OFF segmented toggle shows the current state (OFF = first segment, ON = second).
  - **SC2 — tap emits the new boolean.** Tapping the non-selected segment invokes `onRouterChange` with the target value.
  - **SC3 — null hides it.** With both params null (default) no router node exists and no space is reserved; the 21 pre-existing card tests are unchanged.
  - **SC4 — half pair throws.** Exactly one of the pair non-null throws `IllegalArgumentException`, in both directions.
  - **SC5 — not per-rung navigation.** A rung tap still only selects the cap; toggling router never changes rung order, Capped / Needs network affordances or cap selection.
  - **D-02 — placement.** The router toggle sits directly below the offline toggle at the card bottom; the Explorer fixture shows a live router toggle in every fixture cell plus an appended "router on" cell.
  - **D-01 — labels.** `routerOnLabel` / `routerOffLabel` are caller-localizable with English defaults ("Router on" / "Router off").
- **Owner how-to-verify (run at milestone completion):**
  1. Open the Explorer gallery, Voice family, `ApproachLadderCard`.
  2. Toggle Router on and off, in light and dark theme.
  3. Judge the two stacked segmented toggles at the card bottom: spacing between them, card height growth, selected-segment contrast.
  4. Tap a rung and confirm it still only picks the cap (no router or navigation side effect).
  5. With TalkBack on, focus each router segment: expect "Router on, selected" / "Router off, not selected" (present as content-desc on the device; the spoken form is not audible to the Gate-1 tester).
- **Note:** No schema or persistence. Additive at source level; v2.4.1-compiled consumers keep linking through the untouched hidden v2.4.1 shim, proven by the javap binary gate. The accessibility state words "selected" / "not selected" stay English (announced by `SegmentedOptionSelector`; declared residual from Phase 15).
