### Phase 15 — voice-surface-i18n-label-params (v2.5)

- **Status:** `pending`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v2.5 (Voice UI Localization & Accessibility)
- **Gate 1 self-UAT log:** [`.planning/phases/15-voice-surface-i18n-label-params/15-03-SELF-UAT.md`](phases/15-voice-surface-i18n-label-params/15-03-SELF-UAT.md) — Verdict: **ALL 5 criteria PASS** (device Samsung SM-S908U tester rig `R5CT10XNKQN`, library AAR md5 `a5e6946417d793080cc7fed523a76677` @ `83169fe`, 2026-10-05). Defaults rendered on the real gallery match the v2.4.1 literals; overrides rendered on the real device via a throwaway harness activity; 105 targeted unit tests + apiCheck green.
- **Items covered (5 ROADMAP success criteria):**
  - **SC1 — VI18N-01 providerLabel / modelLabel.** Gallery shows `Provider` / `Model`; harness override shows `Proveedor-OVR` / `Modelo-OVR`, no English leak.
  - **SC2 — VI18N-02 ClarificationBar dismissLabel.** Gallery `Dismiss`; override `Descartar-OVR`.
  - **SC3 — VI18N-03 ApproachLadderCard five labels.** Gallery shows Unavailable / Capped / Needs network / Online / Offline only; all five overridden in the harness.
  - **SC4 — VI18N-04 OutcomeSheet model-field labels.** Gallery `Escalations: 1`, `Couldn't undo: ... changed since`, `Remove`; harness overrides for escalations, Undone, refusal prefix/suffix, and Remove content description. (Default `Undone` is not on any gallery fixture; covered by unit tests.)
  - **SC5 — Existing callers byte-identical, strictly additive.** Source-compat fixture + label-defaults tests + `apiCheck` green; device defaults match v2.4.1.
- **Owner how-to-verify (run at milestone completion):**
  1. Read the Gate-1 log above for per-criterion evidence and the preserved uiautomator dumps in `15-03-evidence/`.
  2. In a consumer (or the harness source `15-03-evidence/harness-OverrideActivity.kt.txt`), pass a real localized string (a long one, e.g. German) to each new param and eyeball truncation: the ProviderKeyCard/ModelSelectCard dropdown labels, the ClarificationBar dismiss button, the ApproachLadderCard segmented toggle + rung subdued labels, and the OutcomeSheet handled-by / undo / refusal rows.
- **Note:** No schema or persistence involved. "Strictly additive" holds at the source/Metalava level only; Kotlin default-arg call sites are not binary compatible, so consumers must recompile on repin (documented in `API.md`). `tools/verify-api-additive.sh` exits 3 against v2.4.1 (known false positive) and needs the lane-3 override at Phase 18. Residual i18n limits WR-02/WR-03/WR-04 are in `15-REVIEW-FIX.md`, out of VI18N-01..04 scope.
