---
phase: "21"
slug: "receipt-row-pill-selector-sheet-shape"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-10-12"
---

# Phase 21 — Validation Strategy

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

Host build lock: only ONE gradle invocation at a time, never parallel gradle. No pixel capture (`captureToImage` times out in this harness): structural, semantics and bounds proofs only.

---

## Sampling Rate

- **After every task commit:** Run that task's `--tests` filters plus `io.github.ygaray.yahirandroidtaste.explorer.*` in ONE gradle invocation (`apiDump` first only when public API changed), then `bash tools/classify-hub-change.sh --baseline v2.5.1` on the staged diff.
- **After every plan wave:** `./gradlew apiDump testDebugUnitTest detekt apiCheck`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** ~240 seconds

---

## Per-Task Verification Map

Task IDs are finalized by the plans; the requirement-level map is:

| Requirement | Behavior | Test Type | Automated Command | File Exists | Status |
|-------------|----------|-----------|-------------------|-------------|--------|
| VROW-01 | Line-height probe 24/20/16dp; collapsed 92±1dp; cue 24dp + 4dp gap; German 360dp value wins, title ellipsizes; chevron matrix; expanded content full width below; dimmed 38% + strikethrough; 200ms chevron/dim motion | Robolectric NATIVE + source contract | `--tests "...component.ReceiptRowLineHeightTest" --tests "...component.ReceiptRowTest" --tests "...component.ReceiptRowMotionTest" --tests "...component.ReceiptRowSourceContractTest"` | ✅ | ✅ green |
| VROW-02 | One merged summary node; Role.Button + onClickLabel; stateDescription toggles; trailingAction and expandedContent are separate nodes | Robolectric | `--tests "...component.ReceiptRowTest"` | ✅ | ✅ green |
| VSEG-01 | Pill: primary/onPrimary, no check icon, 40dp, single-line German truncation; `disabledOptions` 38% alpha, never fires, both styles; Material path unchanged | Robolectric NATIVE + source contract | `--tests "...component.SegmentedOptionSelectorPillTest" --tests "...component.SegmentedOptionSelectorTest" --tests "...component.ApproachLadderCardLongLabelTest" --tests "...component.ApproachLadderCardTest" --tests "...component.SegmentedOptionMaterialParitySourceContractTest"` | ✅ | ✅ green |
| VSHEET-01 | `shape` before `content`, default `BottomSheetDefaults.ExpandedShape`, forwarded; existing sheet callers pass | source contract + Robolectric | `--tests "...component.SheetScaffoldShapeTest" --tests "...component.OutcomeSheetTest" --tests "...component.AlbumTitleConfirmSheetComposeTest"` | ✅ | ✅ green |
| Registry (D-09) | `ReceiptRow` in Cards family, single-open demo accordion in demo state, guards green | JVM + Robolectric | `--tests "io.github.ygaray.yahirandroidtaste.explorer.*"` | ✅ | ✅ green |
| API | `api.txt` regenerated and `apiCheck` green | Gradle | `./gradlew apiDump apiCheck` | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] `src/test/.../component/ReceiptRowLineHeightTest.kt` — 24/20/16 probe
- [x] `src/test/.../component/ReceiptRowTest.kt`
- [x] `src/test/.../component/ReceiptRowMotionTest.kt`
- [x] `src/test/.../component/ReceiptRowSourceContractTest.kt`
- [x] `src/test/.../component/SegmentedOptionSelectorPillTest.kt`
- [x] `src/test/.../component/SegmentedOptionMaterialParitySourceContractTest.kt`
- [x] `src/test/.../component/SheetScaffoldShapeTest.kt`
- [x] `src/test/.../explorer/ReceiptRowRegistryRenderTest.kt`
- [x] `DomainVocabularyDriftGuardTest` acknowledgment for `ReceiptRow`; `DimensTest` appended methods

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Visual look of ReceiptRow, Pill selector and shaped sheet on the TESTER | VROW-01, VSEG-01, VSHEET-01 | Pixel capture unavailable in the harness; owner screencap review is Phase 24 (Y-15) | Phase 24 gallery screencaps on `adb -s R5CT10XNKQN` |

---

## Validation Sign-Off

> **Plan-time state is a DRAFT.** Frontmatter `status: draft` and `nyquist_compliant: false` are finalized only post-execution by the Nyquist finalizer.

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 240s
- [x] _(finalizer-only, post-execution)_ `nyquist_compliant: true` (zero automatable gaps)

**Approval:** validated 2026-10-12 (finalizer)

---

## Validation Audit 2026-10-12

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |

Every requirement (VROW-01, VROW-02, VSEG-01, VSHEET-01, registry, API) maps to a named test file that exists; the fresh uncached run `cleanTestDebugUnitTest testDebugUnitTest detekt apiCheck --rerun-tasks` recorded in 21-04-SELF-UAT.md is green (968 tests, 0 failures). The only manual-only item (visual look on the tester) was additionally driven on the tester during Gate-1 (all 5 criteria pass); the Phase 24 owner screencap review remains a Gate-2 item.
