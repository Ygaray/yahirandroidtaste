# Codebase Concerns

**Analysis Date:** 2026-08-21

## Overview

This reusable Compose library is well-maintained with strong architectural discipline and comprehensive drift guards. Analysis focuses on reusability invariants (one-way dependency, bindings-only Hilt, ComponentRegistry), human-gated shipping, and immutable tag integrity. Identified concerns are primarily documentation-level and architectural (guarding against future regression) rather than active bugs — the fix-voice-dirty-gate branch work has resolved a critical edit-state regression with regression guard test in place.

---

## Architectural Concerns

### One-way Dependency (Critical Invariant — Currently Clean)

**Status:** ✅ Maintained

**Files:** `src/main/java/io/github/ygaray/yahirandroidtaste/` (entire main source tree)

**What's guarded:**
- Library imports Android SDK, AndroidX/Compose, Hilt, Coil, navigation-compose, reorderable ONLY
- No imports of consumer code (no `secondbrain.*`, no domain nouns like `voice`, `note`, `album`, `list` as packages)
- Every component takes content + callbacks as parameters; holds no domain state

**Why this matters:**
A single import of consumer code would break the reusability contract — this library exists precisely to be dropped into SecondBrain and future apps unchanged. Any violating import is a supply-chain integrity failure.

**Risk if violated:**
- Consumers cannot adopt the library without adopting the consumer's domain model
- New consumers forced to fork or work around the constraint
- Breaking change requiring major version bump + human coordination across all apps

**Verification:** Grep confirms zero imports matching `secondbrain|voice|note|album|list` across src/main/java (note: these words appear in *parameter names* and *function documentation*, which is correct — only package-level imports are forbidden).

---

### Bindings-only Hilt Pattern (Critical Invariant — Currently Correct)

**Status:** ✅ Maintained

**Files:** 
- `src/main/java/io/github/ygaray/yahirandroidtaste/feedback/UndoHistoryStore.kt` (Hilt singleton)
- `build.gradle.kts:77-80` (Hilt configuration)

**What's guarded:**
- Library declares **zero** `@HiltAndroidApp` and zero `@AndroidEntryPoint` decorators
- Provides only `@Singleton class UndoHistoryStore @Inject constructor()` — bindings, not hosting
- Consuming app (`SecondBrain`, or future) owns the Hilt `Application` and aggregates library bindings

**Why this matters:**
A library that declares `@HiltAndroidApp` or `@AndroidEntryPoint` claims ownership of application initialization — correct only in an app, never in a library. A library's job is to *contribute* `@Singleton`/`@Provides` bindings; the app's job is to host and aggregate them.

**Risk if violated:**
- Multiple `@HiltAndroidApp` decorators on the same classpath cause a Hilt setup failure (multiple application hosts)
- Consumers' own Hilt initialization breaks
- Injection of library singletons fails with obscure "component not installed" errors

**Verification:** Grep confirms zero `@HiltAndroidApp` and zero `@AndroidEntryPoint` in src/main/java (the ExplorerActivity is a simple `ComponentActivity()`, not Hilt-enabled, which is correct — the gallery is standalone).

---

### ComponentRegistry Drift Guard (Critical Invariant — Currently Enforced)

**Status:** ✅ Maintained

**Files:**
- `src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` (registry + INTENTIONALLY_UNREGISTERED allowlist)
- `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistryDriftGuardTest.kt` (enforcement + denylist source-scan)

**What's guarded:**
- Every public top-level `@Composable` in `component/`, `feedback/`, `modifier/`, `theme/` packages MUST be:
  - Registered in `ComponentRegistry.entries` (one of the seven family lists), OR
  - Allowlisted in `INTENTIONALLY_UNREGISTERED` (documented structural sub-parts)
  - Never both, never neither
- Test performs automated source-scan from `yahirandroidtaste` root, excluding `explorer/` package (denylist-based CATALOG-05 widening)
- Test asserts registry is complete before checking coverage — vacuous-pass guard

**Why this matters:**
The registry is the single source of truth for the gallery's UI, the API.md catalog, and the drift barrier. An unregistered public composable silently escapes the documentation, testing, and gallery — creating a hidden surface and inviting bugs.

**Risk if violated:**
- New public composable not rendered in gallery — consumer might discover bugs only when using it
- API.md becomes incomplete, confusing consumers about what's available
- Breaking changes shipped without understanding the impact surface

**Current state:**
- 41 registered composables across seven families (last count at Phase 87 Plan 01, 2026-08-08)
- 4 intentionally unregistered (CardBase, WaveformCanvas, SwipeableActionRow, YahirAndroidTasteTheme)
- Zero drift: test green, every scanned composable is registered or allowlisted

---

## Shipping & Versioning Concerns

### Human-gated Repin Ritual (Critical Process — Currently Documented)

**Status:** ✅ Documented, not yet tested with second repin

**Files:**
- `CLAUDE.md:41-53` (overview)
- `ECOSYSTEM.md:7,89-106` (full ritual + jurisdiction)
- `~/.claude/context/workflows/repin.md` (external: machine-config layer)

**What's guarded:**
- Hub changes are **not live in any consumer** until: new tag → JitPack builds it → consumer repins coordinate
- Tags are immutable (v1.0.0, v1.1.0, v1.1.1, v1.1.2, v1.1.3 live; zero `-SNAPSHOT`)
- Tag-cut is human-gated: developer fixes hub + tests autonomously, then **surfaces tag + consumer-bump for confirmation**
- Consumers pin immutable tag XOR commit-SHA; never a branch ref, never `-SNAPSHOT`

**Risk if violated:**
- Pushing a tag without consumer verification → broken app on device
- Using `-SNAPSHOT` on a mutable ref → Gradle's ~24h caching hands consumers stale bytes silently
- Bumping consumer coordinate without re-verifying → shipping untested combo to end users

**Current state:**
- v1.0.0 cut human-gated Phase 102, JitPack build successful, SecondBrain repinned Phase 103 + device-verified
- Recent fixes (EDIT-02 voice voice dirty-gate, WR-02 accessibility merge) are on main, not yet tagged/repinned
- No evidence of tag mutation or snapshot use

**Concern:** The ritual is documented but not yet stress-tested by a second full repin cycle. First repin (Phase 103) went well; if a future repin is rushed or shortcuts the workflow, a build/runtime failure could reach a consumer. Mitigation: enforce the ECOSYSTEM.md §7 checklist (edit coordinate → `--refresh-dependencies` → rebuild → suite green → device re-verify → human tag-cut confirm).

---

### Tag Immutability (Critical Invariant — Currently Enforced)

**Status:** ✅ Maintained

**Evidence:**
- All five live tags are semver format: v1.0.0, v1.1.0, v1.1.1, v1.1.2, v1.1.3
- Zero `-SNAPSHOT` tags in git history
- JitPack coordinate is immutable by design: `com.github.Ygaray:yahirandroidtaste:<tag>` resolves a fixed GitHub commit at that tag

**Risk if violated:**
- `-SNAPSHOT` on a mutable branch (e.g., main-SNAPSHOT) — Gradle caches ~24h, hand-back stale bytes even after a push
- Consumer gets broken bytes from stale cache, sees no Gradle error, debugging becomes a nightmare

**Verification:** `git tag -l` lists only semver tags. build.gradle.kts / INTEGRATION.md / ECOSYSTEM.md all forbid `-SNAPSHOT`.

---

## Test Coverage Gaps

### VoiceCard Coverage (Regression Risk — Now Guarded)

**Status:** ⚠️ Regression risk mitigated by gate test (Phase 113)

**Files:**
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/VoiceCard.kt` (417 lines)
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/VoiceRenameTagsSheet.kt` (67 lines)
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceRenameTagsSheetGateTest.kt` (38 lines — newly added on fix-voice-dirty-gate)
- `src/test/java/io/github/ygaray/yahirandroidtaste/component/VoiceAlbumEditMenuTest.kt`

**Issue:** EDIT-02 regression (SecondBrain Phase 113 Gate-1 finding 1, fixed hub v1.1.3)

**What happened:**
- VoiceRenameTagsSheet passes `NameAndTagsEditor` component an `enabled` parameter
- NameAndTagsEditor defaults `enabled = true` (always-on Save button)
- Before fix: VoiceRenameTagsSheet **omitted the `enabled` argument**, so Save was always enabled at rest (unlike Album/Text/List cards)
- Caller couldn't supply tags-dirty state, so Save button didn't gate on tag changes

**Root cause:** Copy-paste from an earlier create-mode sheet where always-on was correct; edit-mode requires name-OR-tags dirty.

**Fix (commit 394c089):**
- VoiceRenameTagsSheet now exposes `tagsDirty: Boolean = false` parameter
- Passes `enabled = (title.trim() != defaultTitle) || tagsDirty` to NameAndTagsEditor
- Caller (SecondBrain) supplies live `tagsDirty` from its own tag-editor state

**Regression guard (new VoiceRenameTagsSheetGateTest):**
- Source-level structural assertion: VoiceRenameTagsSheet.kt **must contain** the string `tagsDirty: Boolean = false`
- Source-level structural assertion: VoiceRenameTagsSheet.kt **must contain** the string `enabled = (title.trim() != defaultTitle) || tagsDirty`
- If either is removed, the gate fails the build before the code reaches a consumer
- **Rendered proof** is discharged on-device at Phase 113's Gate-1 (not automated here)

**Impact:** Low on the hub (test is in place); HIGH on consumers if they repin v1.1.2 without the fix. SecondBrain already reknew v1.1.3 (fixed). Future consumers will get the fix in their first adoption.

**Risk:** A future voice-card refactor could introduce a similar regression if the gate test or `enabled` expression is changed carelessly. Test is brittle (source text matching, not semantic). As long as the test runs + passes before release, regression is caught; if someone skips tests or regenerates the baseline carelessly, the risk re-opens.

---

### Overall Test Coverage (Adequate but Gaps Exist)

**Status:** Reasonable but incomplete

**Metrics:**
- Main source: 17,883 lines
- Test source: 4,427 lines
- Ratio: ~25% (test-to-code ratio; healthy is 25–50%)
- Test files: 27

**Coverage by family:**
- Cards: TextCard (tests), ListCard (tests), AlbumCard (tests), VoiceCard (2 test files: VoiceAlbumEditMenuTest, VoiceRenameTagsSheetGateTest)
- Chips: AppChip, TagChipWithContextMenu (tests)
- Sheets: NameAndTagsEditor, TagPickerSheet, AlbumTitleConfirmSheet (tests), others
- Buttons/FAB: CycleSubTypeButton, DynamicActionButton (tests)
- Pickers: CropOverlay (tests)
- Feedback: UndoHistoryStore, UndoCenterScreen (tests)
- Empty-state: No dedicated test found

**Known gaps:**
- EditorItemRow (reorderable list-editor row — complex gesture mechanic, no dedicated test visible)
- RecordingBottomSheetContent (voice recording waveform + timer — complex state, not easily testable in Robolectric)
- Some gallery components (explorers' own screens are fixtures, not tested in the module suite)
- Waveform rendering (WaveformCanvas, VoiceWaveformCanvas — pixel-level rendering, requires snapshot testing or visual inspection)

**Risk:** Performance bottlenecks or subtle rendering bugs in complex sheets could ship unnoticed. Mitigation: on-device gallery (ExplorerActivity) serves as manual smoke-test + consumer integration tests.

---

## Documentation Drift

### IN-01 — README Links to GitHub Profile, Not Repo (OPEN)

**Status:** Open (cosmetic, safe to batch)

**Files:** `README.md:7`, `README.md:25`

**Issue:**
Both lines link the text `SecondBrain` to `https://github.com/Ygaray` (the user profile) instead of the SecondBrain repository URL. `ECOSYSTEM.md:22,33` use the correct repo-qualified form, revealing an internal inconsistency.

**Impact:** Cosmetic / documentation only. No code, build, or publish impact. Users clicking the link land on the wrong page (profile vs. repo).

**Fix:** Point both to the SecondBrain repo (e.g., `https://github.com/Ygaray/SecondBrain`), or if the repo is still private/unpublished, de-link the text or point it at whatever canonical location exists.

**Risk if skipped:** None on the library. Low on users (confusing navigation, not a supply-chain issue).

---

## Detekt Baseline (Zero-baseline Policy — Maintained)

**Status:** ✅ Clean

**Files:**
- `config/detekt/detekt.yml` (configuration)
- `config/detekt-baseline.xml` (baseline — empty)
- `build.gradle.kts:10-19` (detekt configuration)

**What's guarded:**
- `maxIssues: 0` in config — build fails if any issues exist
- Baseline is empty (no pre-existing suppressions)
- ~10 targeted rules enabled (complexity, naming, style)
- Compose-idiomatic size thresholds tuned (LongMethod threshold 60, FunctionNaming ignores @Composable PascalCase)

**Risk if violated:**
- Regenerating the baseline to bury new findings would weaken the gate
- Disabling rules just to pass would accumulate debt

**Current state:** Build reports show `detekt.txt` / `detekt.html` are generated; a full detekt run is in CI. No visible issues or baseline regressions.

---

## Fragile Areas

### Large Files (Size Complexity)

**Files:**
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/IconPickerGrid.kt` (4,143 lines)

**Issue:** File is large due to 1,988 `Icons.Filled` imports at the top, then a LazyVerticalGrid rendering the icon library.

**Why it exists:** The icon picker is a complete, self-contained UI surface; the Material Icons Filled family is large by design. Splitting it would require centralizing the icon list elsewhere or parameterizing it as a data table — both add complexity.

**Risk:** The file is not *broken*, but it's hard to navigate. If icon-picker rendering changes, the edit window is wide. If the icon list needs versioning or dynamic loading (e.g., loading from a remote catalog), refactoring would be necessary.

**Mitigation:** No action needed today. If future concerns arise (e.g., "we need a custom icon set" or "icons should load dynamically"), consider extracting the icon list to a separate object or data file.

---

### Waveform Rendering (Complex, Visual, Not Easily Testable)

**Files:**
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/WaveformCanvas.kt` (WaveformCanvas)
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/VoiceCard.kt` (VoiceWaveformCanvas inner component; private)
- `src/main/java/io/github/ygaray/yahirandroidtaste/component/RecordingBottomSheetContent.kt` (recording sheet with live waveform)

**Issue:** Waveform rendering involves Canvas drawing, downsampling, and real-time update — requires visual inspection to verify correctness. Robolectric unit tests cannot easily capture pixel-perfect rendering.

**Why fragile:** A small change to bar width, gap, color, or downsampling logic could introduce visual artifacts (skewed waveforms, flickering, performance slowdown). The code is private/sub-part (not in ComponentRegistry entries), so gallery inspection is the only eye check.

**Mitigation:**
- Code review on changes to `downsample()`, bar-rendering math, or color logic
- Manual gallery testing (ExplorerActivity) on-device to spot visual regressions
- Consumer integration tests (SecondBrain's own voice recording flow) as the real smoke test

---

## Cross-Repo Work Convention (Sequential in Hub)

**Status:** ✅ Documented

**Files:** `CLAUDE.md:55-60`

**What's guarded:**
- When a SecondBrain phase lands code in the hub: run **sequential in the hub** (no consumer worktrees)
- Commit here on `main`; SecondBrain's orchestrator owns its own STATE/ROADMAP
- Do not modify SecondBrain files from a hub-scoped task

**Why this matters:**
The hub and consumers have different orchestrators. If a hub task writes to SecondBrain files, it orphans the consumer's phase tracking and creates confusion about responsibility.

**Current state:** Recent fixes (EDIT-02, WR-02) are hub-only. No cross-repo file modifications observed in git log.

**Risk if violated:**
- Orchestrator confusion: which repo owns the phase?
- Merge conflicts or orphaned changes if the consumer is working on the same branch
- Unclear audit trail (is this a hub phase or a consumer phase?)

---

## Security Considerations

### No Secrets in Repository (Verified)

**Status:** ✅ Maintained

**What's checked:**
- `.env` files: present in `.gitignore`, never committed
- `.keystore`, `*.pem`, `*.key`: not found in repository
- Service account credentials: not found
- API keys in code: not found (Hilt bindings for `UndoHistoryStore` are domain-agnostic, no API keys)

**Risk:** Low. The library is domain-agnostic and holds no app-specific secrets.

---

### Dependency Supply Chain (Stable, Low Risk)

**Status:** ✅ Stable

**Dependencies:**
- Android SDK, AndroidX (latest Compose BOM 2026.02.01), Hilt 2.60.1, Kotlin 2.3.20, Coil, navigation-compose, reorderable (sh.calvin)
- All are from reputable sources (Google, Jetbrains, community)
- No pinning to insecure/deprecated libraries

**Risk:** Low. Material icon set is curated by Google; Compose runtime is well-maintained. No obsolete or abandoned dependencies observed.

---

## Performance Concerns

### IconPickerGrid Rendering (Potential Jank with 1,988+ Icons)

**Status:** ⚠️ Requires on-device verification

**Files:** `src/main/java/io/github/ygaray/yahirandroidtaste/component/IconPickerGrid.kt`

**Issue:** Grid renders all 1,988 Material Icons in a LazyVerticalGrid. Scrolling a 1,988-item list could cause jank on low-end devices (minSdk 35 supports older Pixels, some low-RAM phones).

**Why concern:** LazyVerticalGrid should handle this gracefully (compose will virtualize), but rendering 1,988 icon selection targets + text labels is not trivial.

**Mitigation:** ExplorerActivity gallery can be used to spot-check scrolling performance. If jank is observed, consider:
- Pagination (load icons in chunks)
- Search/filter to reduce visible items
- Lazy composition of icon names (render only visible cells' text)

**Current state:** No performance complaints in recent commits or issues. Assumed to be adequate unless a consumer reports jank.

---

## Tags at Risk / Version Constraints

**Status:** ✅ Stable, no version conflicts

**Current published tag:** v1.0.0 (Phase 102, device-verified on SecondBrain v1.20 Phase 103)

**Recent tags:** v1.1.0, v1.1.1, v1.1.2, v1.1.3 (latest, bug fixes for EDIT-02, WR-02, IMG-02)

**Risk if Compose BOM version drifts:**
- Library is pinned to Compose BOM 2026.02.01
- Consumers must match (INTEGRATION.md:15)
- If a consumer forgets to align the BOM, Compose version skew causes `NoSuchMethodError` at composition time
- Mitigation: INTEGRATION.md is explicit; consumer's own test suite (if any) should verify Compose versions via `./gradlew :app:dependencies | grep androidx.compose`

---

## Summary of Risks by Priority

| Area | Risk | Priority | Mitigation |
|------|------|----------|-----------|
| **IN-01 documentation drift** | README links to profile, not repo — cosmetic | Low | Batch into next doc pass; point to correct URL |
| **Voice dirty-gate regression (FIXED)** | Regression guard now in place; v1.1.3 shipped | Low | Gate test in CI; future refactors will catch regression |
| **Cross-repo repin ritual** | First repin succeeded; second repin not yet tested | Medium | Follow ECOSYSTEM.md §7 checklist strictly |
| **Waveform rendering fragility** | Visual correctness requires on-device inspection | Low | Gallery + consumer integration tests catch bugs |
| **IconPickerGrid potential jank** | 1,988 icons might stutter on low-end devices | Low | Monitor on-device; optimize if reported |
| **Test coverage gaps** | Some complex sheets lack full coverage | Low | On-device gallery serves as smoke test |
| **Detekt baseline discipline** | Zero-baseline policy could be broken by re-baseline | Medium | Enforce "fix, don't bury" in code review |
| **One-way dependency** | A single consumer import would break reusability | Critical | Automated grep in CI (not in place; recommend adding) |
| **Hilt bindings-only pattern** | `@HiltAndroidApp` in lib would break consumers | Critical | Source scan in CI (not in place; recommend adding) |

---

## Recommendations (Non-blocking)

1. **Add CI gate for one-way dependency:** `grep -r "import.*secondbrain" src/main/` to fail the build if a consumer package is ever imported.
2. **Add CI gate for Hilt pattern:** `grep -r "@HiltAndroidApp\|@AndroidEntryPoint" src/main/` to fail if either is declared.
3. **Resolve IN-01:** Update README.md links to point to SecondBrain repo (or canonical location) instead of GitHub profile.
4. **Detekt baseline discipline:** Add pre-commit hook or CI step to reject baseline regeneration (enforce "fix, don't bury").
5. **On-device gallery testing:** Document the ExplorerActivity as part of release QA (manual visual inspection of all components on the target device before tagging).

---

*Concerns audit: 2026-08-21*
