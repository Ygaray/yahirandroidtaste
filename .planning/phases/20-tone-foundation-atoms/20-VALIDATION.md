---
phase: "20"
slug: "tone-foundation-atoms"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-12"
---

# Phase 20 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 + Robolectric 4.16.1 + Compose ui-test (`createComposeRule`) |
| **Config file** | `build.gradle.kts` (`testOptions { unitTests { isIncludeAndroidResources = true } }`) |
| **Quick run command** | `./gradlew testDebugUnitTest --tests "io.github.ygaray.yahirandroidtaste.<pkg>.<Class>"` (batch all filters for a task into ONE invocation) |
| **Full suite command** | `./gradlew apiDump testDebugUnitTest detekt apiCheck` then `bash tools/test/run-all.sh` |
| **Estimated runtime** | quick ~120-240 s (single gradle invocation); full several minutes |

Host build lock: only ONE gradle invocation at a time, never parallel gradle.

---

## Sampling Rate

- **After every task commit:** one gradle invocation with that task's `--tests` filters plus `io.github.ygaray.yahirandroidtaste.explorer.*`, then `bash tools/classify-hub-change.sh --baseline v2.5.1` on the staged diff
- **After every plan wave:** `./gradlew apiDump testDebugUnitTest detekt apiCheck` and `bash tools/test/run-all.sh`
- **Before `/gsd-verify-work`:** full suite green
- **Max feedback latency:** ~300 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 20-XX | (see PLAN files) | — | VTONE-01 | — | N/A | Robolectric unit | `--tests "...theme.ToneTokensTest"` | ❌ W0 | ⬜ pending |
| 20-XX | (see PLAN files) | — | VCARD-01 | — | N/A | Robolectric | `--tests "...component.ToneCardTest"` | ❌ W0 | ⬜ pending |
| 20-XX | (see PLAN files) | — | VBTN-01 | — | N/A | Robolectric + source-contract | `--tests "...component.DynamicActionButtonEmphasisTest" --tests "...DynamicActionButtonLegacyPathSourceContractTest" --tests "...DynamicActionButtonTest"` | ❌ W0 / ✅ existing | ⬜ pending |
| 20-XX | (see PLAN files) | — | VBTN-02 | — | N/A | Robolectric | `--tests "...component.RoundIconButtonTest"` | ❌ W0 | ⬜ pending |
| 20-XX | (see PLAN files) | — | VBAR-01 | — | N/A | JVM + Robolectric | `--tests "...component.StackedProgressBarTest"` | ❌ W0 | ⬜ pending |
| 20-XX | (see PLAN files) | — | VCHIP-01 | — | N/A | Robolectric | `--tests "...component.PillChipTest" --tests "...AppChipTest" --tests "...PresetChipTest"` | ❌ W0 / ✅ existing | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `src/test/.../theme/ToneTokensTest.kt` — VTONE-01 (+ contrast helper)
- [ ] `src/test/.../component/ToneCardTest.kt`, `RoundIconButtonTest.kt`, `StackedProgressBarTest.kt`, `PillChipTest.kt`
- [ ] `src/test/.../component/DynamicActionButtonEmphasisTest.kt` + `DynamicActionButtonLegacyPathSourceContractTest.kt`
- [ ] `src/test/.../component/ToneAtomsLongLabelTest.kt` (NATIVE, `w360dp-h800dp`, long German strings)
- [ ] `src/test/.../explorer/DomainVocabularyDriftGuardTest.kt` — append `Round`, `Stacked`, `Tone`, `Pill` to `PRIMITIVE_NOUN_ALLOWLIST`
- [ ] Framework install: none (existing infrastructure)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Visual fidelity of emphases / atoms in the gallery | VBTN-01, VBTN-02, VBAR-01, VCARD-01, VCHIP-01 | Pixel look is judged by the owner | Open `ExplorerActivity` on the TESTER (`adb -s R5CT10XNKQN`) and review the new entries (Gate-1/Gate-2 per milestone flow) |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Leave frontmatter `status: draft` and `nyquist_compliant: false`.
> These are finalized ONLY post-execution by the Nyquist finalizer.

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 300s
- [ ] _(finalizer-only, post-execution)_ `nyquist_compliant` — leave `false` at plan time

**Approval:** pending
