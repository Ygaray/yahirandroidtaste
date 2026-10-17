### Phase 25 — feedback-target-model-stack-state (v2.8)

- **Status:** `pending`
- **Milestone:** v2.8 (Snackbar go-to-item + stacked feedback)
- **Gate 1 self-UAT log:** [`.planning/phases/25-feedback-target-model-stack-state/25-05-SELF-UAT.md`](phases/25-feedback-target-model-stack-state/25-05-SELF-UAT.md) — Verdict: **ALL 5 criteria PASS** (device SM-S908U tester `R5CT10XNKQN`, library AAR md5 `cd6bf08da91cc12e15b9a7c679269211` @ `7c19bab`, harness APK md5 `329b9f0c0d16ef3317e64a240752ef71`, uat_run `uatr-20261017T093534Z-c9306f1b`, 15 signed captures, 2026-10-17). Fresh uncached run 1452 tests / 22 skipped / 0 failures, detekt and apiCheck executed, additive tool SOURCE-ADDITIVE OK vs v2.7.1; the stack host and both tracked-undo paths were driven on the real tester through the Explorer and a throwaway consumer harness.
- **Items covered (5 ROADMAP success criteria):**
  - **SC1 — FeedbackTarget on Notify/WithUndo, v2.7.1 call shapes.** Value type with two Strings; canary 8/8; additive API tool 0 removed members, 0 required-param changes; the consumer harness compiled against the new AAR.
  - **SC2 — Target reaches the Undo history on both tracked paths.** Live store read on the tester: Available with openable target, then Undone with `openable=null` (stack and Material3 paths).
  - **SC3 — Exactly-one result.** Opened, Undone, Dismissed and TimedOut each seen once on the tester, one caller resume each; virtual-time tests 24 + 21 + 6.
  - **SC4 — Tracked undo through the store guard.** Entry in the store while the card is up; card Undo ran the lambda once; Undo Center first made the card spent and the lambda still ran once; Dismissed/Opened/TimedOut never undo.
  - **SC5 — FeedbackStackHost.** Body tap on a targeted card opens once (`Opened`, key shown), plain card inert, Undo resolves `Undone`, German "Rückgängig machen" rendered as passed (screenshot read), registered in the Feedback family, registry and CATALOG guards green.
- **Owner how-to-verify (run at milestone completion):**
  1. Read the Gate-1 log above for per-criterion evidence (screenshots `02` and `12` in the run dir).
  2. Open the Explorer, Feedback, FeedbackStackHost, push the three demo cards and tap the body of a targeted one (it should report `Opened` plus the key), tap Undo on another (`Undone`), tap the plain note (nothing). The deck look, swipe, fan-out and pause are Phase 26, not this phase.
- **Note:** Library-only phase; on-device surface is the Explorer plus a throwaway same-package/consumer harness (never committed). No `src/androidTest`, so no destructive check. The tracer host has no TalkBack confirmation yet (Phase 28 sweep). A first signed run was abandoned (APK reinstalled mid-run) and everything re-run on the final APK.
