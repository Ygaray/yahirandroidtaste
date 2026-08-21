# Testing Patterns

**Analysis Date:** 2026-08-21

## Test Framework

**Runner:**
- **JUnit 4** (via `junit:junit:4.13.2`)
- **Robolectric 4.13.1** for lightweight Android environment without device/emulator
- **Jetpack Compose UI Test Framework** for composable testing
- Config: `testOptions { unitTests { isIncludeAndroidResources = true } }` in `build.gradle.kts`

**Assertion Library:**
- **JUnit Assert** (`org.junit.Assert.*`): `assertEquals()`, `assertTrue()`, `assertFalse()`, `assertNotNull()`, `fail()`
- **Compose Test Assertions** (`androidx.compose.ui.test.*`): `assertExists()`, `performClick()`, `onNodeWithText()`, `onNodeWithContentDescription()`

**Run Commands:**
```bash
./gradlew testDebugUnitTest              # Run all unit tests (Robolectric + Compose)
./gradlew detekt                         # Static analysis (zero-baseline policy)
./gradlew build                          # Build + run all tests (full CI check)
```

**Test Count:** 27 test files total (`src/test/java/io/github/ygaray/yahirandroidtaste/`)

## Test File Organization

**Location:**
- **Compose UI tests:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/*.kt` (22 files)
- **Logic tests:** `src/test/java/io/github/ygaray/yahirandroidtaste/feedback/*.kt` (2 files: UndoHistoryStoreTest, UndoCenterScreenTest)
- **Drift guard & registry:** `src/test/java/io/github/ygaray/yahirandroidtaste/explorer/*.kt` (2 files: ComponentRegistryDriftGuardTest, ComponentRegistrySearchTest)
- **Model tests:** None yet; logic tests are favored over data class tests

**Naming:**
- Composable test files mirror source component names: `AlbumCard.kt` → `AlbumCardTest.kt` (when tested)
- Not all components have tests yet (27 tests for 40+ registered components)
- Test method names use backtick strings describing behavior: `` `tapping an enabled button invokes onClick exactly once` ``

**Structure:**
```
src/test/java/io/github/ygaray/yahirandroidtaste/
├── component/
│   ├── DynamicActionButtonTest.kt       # Compose UI tests (button label, tap, disabled)
│   ├── CycleSubTypeButtonTest.kt        # Mixed: pure function (nextSubType) + Compose UI
│   ├── TagListItemTest.kt               # Compose UI: tag name & count rendering
│   ├── RelatednessEncodingTest.kt       # Pure JUnit (no Compose): logic functions
│   ├── CountBadgeTest.kt                # Compose UI: count formatting
│   ├── AppChipTest.kt                   # Compose UI: double-tap vs. single click vs. long-click
│   ├── AlbumTitleConfirmSheetTest.kt    # Compose UI: sheet content & interaction
│   ├── TagPickerSheetContentTest.kt     # Compose UI: picker list & selection
│   └── ... (16 more Compose UI tests)
├── feedback/
│   ├── UndoHistoryStoreTest.kt          # Pure JUnit: 50-cap eviction, CAS idempotency, coroutines
│   └── UndoCenterScreenTest.kt          # Compose UI: undo snackbar rendering
└── explorer/
    ├── ComponentRegistryDriftGuardTest.kt  # Source-scan, no reflection (CATALOG-03/05)
    └── ComponentRegistrySearchTest.kt      # Registry search functionality
```

## Test Structure

**Suite Organization (Compose UI Test):**

Example from `DynamicActionButtonTest.kt`:

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DynamicActionButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Label rendering across all 3 roles ---

    @Test
    fun `renders the label for the Save role`() {
        composeTestRule.setContent {
            DynamicActionButton(
                label = "Save",
                role = ActionButtonDefaults.ActionButtonRole.Save,
                onClick = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save").assertExists()
    }

    // --- Tap behavior ---

    @Test
    fun `tapping an enabled button invokes onClick exactly once`() {
        var callCount = 0

        composeTestRule.setContent {
            DynamicActionButton(
                label = "Save",
                role = ActionButtonDefaults.ActionButtonRole.Save,
                onClick = { callCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, callCount)
    }
}
```

**Suite Organization (Pure JUnit Logic Test):**

Example from `RelatednessEncodingTest.kt`:

```kotlin
class RelatednessEncodingTest {

    private val scheme = LightColorScheme

    // ---- Tier boundary sweep + clamping ----

    @Test
    fun `tier boundary sweep matches pinned cut points`() {
        assertEquals(RelatednessTier.MINIMAL, relatednessTier(0.0f))
        assertEquals(RelatednessTier.WEAK, relatednessTier(0.12f))
        assertEquals(RelatednessTier.RELATED, relatednessTier(0.35f))
        assertEquals(RelatednessTier.STRONG, relatednessTier(0.65f))
    }

    @Test
    fun `out-of-range input is clamped, not thrown`() {
        assertEquals(RelatednessTier.MINIMAL, relatednessTier(-0.5f))
        assertEquals(RelatednessTier.STRONG, relatednessTier(2.0f))
    }
}
```

**Patterns:**
- **Setup:** Create composable via `composeTestRule.setContent { /* compose hierarchy */ }`
- **Arrange:** Store callback lambdas as mutable variables to capture call count or parameters
- **Act:** Interact via `performClick()`, `performLongClick()`, etc.
- **Assert:** Verify via `onNodeWithText()`, `onNodeWithContentDescription()`, `assertEquals()`, etc.
- **Idle:** Call `composeTestRule.waitForIdle()` before and after interactions to wait for recomposition

## Mocking

**Framework:** Lambdas only; no mock libraries used (Mockito, etc.)

**Patterns:**

```kotlin
// Capture callback invocation and parameters
var onDeleteCallCount = 0
var capturedCardId = ""

composeTestRule.setContent {
    AlbumCard(
        id = "album-1",
        title = "My Album",
        ...
        onDelete = { onDeleteCallCount++ },
        onRename = { capturedCardId = id }
    )
}
composeTestRule.waitForIdle()

// Interact and verify
composeTestRule.onNodeWithText("Delete").performClick()
assertEquals(1, onDeleteCallCount)
assertEquals("album-1", capturedCardId)
```

**What to Mock:**
- Callback lambdas: capture invocation count, parameters, or side effects (above pattern)
- Optional state holders: pass null or a default `mutableStateOf(null)` for swipe state
- No external dependencies mocked (no Hilt, no Coil, no Firebase)

**What NOT to Mock:**
- Android framework (Robolectric provides real Context, Resources, etc.)
- Compose runtime (use real `remember`, `mutableStateOf`, etc.)
- Color schemes (use real `LightColorScheme` for logic tests)
- `ComponentRegistry` (source-scan based, not mockable)

## Fixtures and Factories

**Test Data:**
- **Inline construction:** Test data created locally within each `@Test` method
  ```kotlin
  composeTestRule.setContent {
      TagListItem(
          tag = TagManagementUiModel(
              id = "tag-plain",
              name = "Groceries",
              cardCount = 3,
              isHome = false,
              iconName = null,
              color = null
          ),
          onClick = {}
      )
  }
  ```
- No factory/builder pattern observed; data classes constructed directly
- No test data fixtures file (e.g., `TestFixtures.kt` or `FakeData.kt`) yet

**Location:**
- Test data lives entirely within test method bodies (no separate fixture module)
- Small amounts of shared setup use class-level properties: `private val scheme = LightColorScheme`

## Coverage

**Requirements:** No coverage targets enforced (no JaCoCo or codecov integration detected)

**View Coverage:**
- Coverage reports not generated in routine test run
- Could generate via `./gradlew testDebugUnitTestCoverage` if JaCoCo configured

**Observed Coverage Gaps:**
- 27 test files × ~10-15 tests each ≈ ~270-400 test cases
- ~40+ public composables registered; ~22 have Compose UI tests (55% of components)
- Feedback module (undo snackbar): 2 tests covering UndoHistoryStore logic and UI rendering
- No negative-path tests observed (behavior when callbacks are null, data is missing, etc.)
- No accessibility (a11y) tests observed

## Test Types

**Unit Tests (Pure JUnit):**
- **Scope:** Logic functions with no Compose runtime needed
- **Approach:** Direct function call, assert return value
- **Location:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/RelatednessEncodingTest.kt`
- **Example:** `relatednessTier()` function testing boundary conditions, NaN/Infinity handling
  ```kotlin
  @Test
  fun `tier boundary sweep matches pinned cut points`() {
      assertEquals(RelatednessTier.MINIMAL, relatednessTier(0.0f))
      assertEquals(RelatednessTier.WEAK, relatednessTier(0.12f))
      // ... pinned cut points verified
  }
  ```

**Compose UI Tests (Robolectric + Compose Test):**
- **Scope:** Composable rendering, interaction, callback invocation
- **Approach:** Render composable via `createComposeRule()`, interact via `performClick()`, assert via node finders
- **Framework:** `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()` rule
- **Location:** `src/test/java/io/github/ygaray/yahirandroidtaste/component/*Test.kt`
- **Example:** `DynamicActionButtonTest.kt`
  ```kotlin
  @Test
  fun `tapping an enabled button invokes onClick exactly once`() {
      var callCount = 0
      composeTestRule.setContent {
          DynamicActionButton(
              label = "Save",
              role = ActionButtonDefaults.ActionButtonRole.Save,
              onClick = { callCount++ }
          )
      }
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Save").performClick()
      composeTestRule.waitForIdle()
      assertEquals(1, callCount)
  }
  ```

**Integration Tests:** None detected (no `*ScreenTest.kt` combining multiple components end-to-end)

**E2E Tests:** None detected; library is component-only, not runnable as standalone app (gallery is explorer-only)

## Common Patterns

**Async Testing:**

Example from `UndoHistoryStoreTest.kt`:

```kotlin
@Test
fun append_createsExactlyOneAvailableEntry_andReturnsItsId() = runTest {
    val store = UndoHistoryStore()
    
    val returnedId = store.append("Card deleted") { /* no-op undo */ }
    
    val entries = store.entries.value
    assertEquals(1, entries.size)
    val entry = entries.single()
    assertEquals(returnedId, entry.id)
}
```

- Uses `runTest` from `kotlinx.coroutines.test` for coroutine-scoped tests
- No explicit `launch` or `runBlocking`; `runTest` handles suspension
- Flow/StateFlow values accessed via `.value`

**Error Testing:**

Example from `RelatednessEncodingTest.kt`:

```kotlin
@Test
fun `NaN and Infinity inputs are clamped, not thrown`() {
    // Float.NaN.coerceIn(...) alone does NOT clamp — every IEEE-754 comparison against NaN is
    // false, so the un-guarded coerceIn would silently fall through to STRONG. Explicitly
    // pinning NaN to MINIMAL ("no signal") locks the contract this shared function's own KDoc
    // promises to Phase 94's mindmap consumer (WR-01).
    assertEquals(RelatednessTier.MINIMAL, relatednessTier(Float.NaN))
    assertEquals(RelatednessTier.STRONG, relatednessTier(Float.POSITIVE_INFINITY))
    assertEquals(RelatednessTier.MINIMAL, relatednessTier(Float.NEGATIVE_INFINITY))
}
```

- No `assertThrows` used; instead, functions are expected to handle edge cases gracefully
- Comments document WHY a certain behavior is required (IEEE-754 semantics)

**Disabled/Optional Path Testing:**

Example from `DynamicActionButtonTest.kt`:

```kotlin
@Test
fun `a disabled button does not invoke onClick on tap`() {
    var callCount = 0

    composeTestRule.setContent {
        DynamicActionButton(
            label = "Save",
            role = ActionButtonDefaults.ActionButtonRole.Save,
            enabled = false,
            onClick = { callCount++ }
        )
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Save").performClick()
    composeTestRule.waitForIdle()

    assertEquals(0, callCount)
}
```

- Explicitly test that disabled state prevents callback invocation
- Use same assertion style (callCount == 0) to show no-op outcome

## Special Tests

**ComponentRegistry Drift Guard:**

`src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistryDriftGuardTest.kt`

- **Purpose:** Ensure every public top-level `@Composable` is registered in `ComponentRegistry.entries` XOR allowlisted in `INTENTIONALLY_UNREGISTERED`
- **Technique:** Source-text scanning (no reflection), not Robolectric/Compose-based
- **Scope:** Scans all `.kt` files in non-`explorer/` packages
- **Detection:** Finds zero-indentation `@Composable` annotations followed by `fun` declarations (excludes nested private helpers)
- **Failure behavior:** Lists unregistered component names and fails build
- **Safeguards:**
  1. Vacuous-pass guard: asserts scan found non-zero `.kt` files before trusting coverage
  2. Working-directory robustness: walks upward from process CWD to find `yahirandroidtaste` module root
  3. Regex extraction: tolerates generic type params (`<T>`), extension receivers (`ReorderableCollectionItemScope.EditorItemRow`)
- **Example failure message:**
  ```
  Found 1 public top-level @Composable function(s) outside the excluded packages (explorer)
  that are neither registered in ComponentRegistry.entries nor allowlisted in
  ComponentRegistry.INTENTIONALLY_UNREGISTERED: [NewComponent] — register each as a new
  explorer entry, or add it to INTENTIONALLY_UNREGISTERED with a one-line reason (D-04).
  ```

**ComponentRegistry Integrity (Init Block):**

`src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ComponentRegistry.kt` (lines 106-128)

- Runs at class initialization (not a separate test)
- Asserts no duplicate names in `entries`
- Asserts no overlap between `entries` names and `INTENTIONALLY_UNREGISTERED` keys
- Asserts all `INTENTIONALLY_UNREGISTERED` reasons are non-blank
- Fails build with `require()` assertion if any invariant violated

## Test Naming & Conventions

**Method Names:**
- Backtick-enclosed behavior descriptions (Kotlin idiomatic for test names)
  - `` `tapping an enabled button invokes onClick exactly once` ``
  - `` `renders a home tag with a custom color` ``
  - `` `tier boundary sweep matches pinned cut points` ``
- Lowercase start inside backticks (reads naturally as a sentence)
- No `test` prefix (redundant with `@Test` annotation)

**Test Organization:**
- Group related tests with comment headers (separator lines with `// --- Group Name ---`)
- Example from `DynamicActionButtonTest.kt`:
  ```kotlin
  // --- Label rendering across all 3 roles ---
  
  @Test
  fun `renders the label for the Save role`() { ... }
  
  // --- Tap behavior ---
  
  @Test
  fun `tapping an enabled button invokes onClick exactly once`() { ... }
  ```

**Compose Test Imports:**
- Always import from `androidx.compose.ui.test.*` and `androidx.compose.ui.test.junit4.*`
- Typical imports:
  ```kotlin
  import androidx.compose.ui.test.junit4.createComposeRule
  import androidx.compose.ui.test.onNodeWithText
  import androidx.compose.ui.test.performClick
  import org.junit.Rule
  import org.junit.Test
  import org.junit.runner.RunWith
  import org.robolectric.RobolectricTestRunner
  import org.robolectric.annotation.Config
  ```

## Test Maintenance

**Detekt Zero-Baseline Policy:** Applies to test code as well
- Test sources excluded from naming rules (`FunctionNaming`, `VariableNaming`)
- Snake_case test method names acceptable: `fun someMethod_state_returnsX()`
- Test-local variable names follow Arrange-Act-Assert conventions without penalty

**Build Integration:**
- Tests run as part of `./gradlew build` (full CI check)
- Gradle test task working directory: module project directory (handled robustly by drift guard)
- No test parallelization observed; sequential execution is default

---

*Testing analysis: 2026-08-21*
