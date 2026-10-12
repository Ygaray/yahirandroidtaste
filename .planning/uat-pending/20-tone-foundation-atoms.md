### Phase 20 — tone-foundation-atoms (v2.6)

- **Status:** `pending`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v2.6 (Receipt voice surfaces + ProviderKeyCard lifecycle)
- **Gate 1 self-UAT log:** [`.planning/phases/20-tone-foundation-atoms/20-04-SELF-UAT.md`](phases/20-tone-foundation-atoms/20-04-SELF-UAT.md) — Verdict: **ALL 5 criteria PASS** (device SM-S908U tester `R5CT10XNKQN`, harness against library AAR md5 `aab6d21f0faf978c2300ce5df6c66d88` @ `3015de6`, uat_run `uatr-20261012T112516Z-132c1d6b`, 2026-10-12). Fresh Robolectric/detekt/apiCheck run 869 tests / 22 skipped / 0 failures; on-device pixel/geometry checks of ToneCard, the DynamicActionButton emphasis matrix, RoundIconButton, PillChip and StackedProgressBar; legacy `DynamicActionButton` rendering pixel-identical to a real v2.5.1 build.
- **Items covered (5 ROADMAP success criteria):**
  - **SC1 — ToneCard / tone tokens.** Error tone reads the ACTIVE scheme (M3-baseline `#F9DEDC`/`#B3261E`, contrast 5.14:1 light, 5.34:1 dark); 12dp corners, 16dp x 12dp padding, 4dp spacing measured on device.
  - **SC2 — DynamicActionButton emphasis axis.** Filled/Tonal/Text/TextSmall x role hue sampled from pixels, leading icon shown, every emphasis row has a 48dp touch region, and the defaulted legacy rows are byte-identical to v2.5.1.
  - **SC3 — RoundIconButton and PillChip.** 40/36/56dp circles, 32dp pill, single-line ellipsized label, 48dp touch regions, content descriptions, held-press ripple on enabled and none on disabled (`enabled=false` in the tree).
  - **SC4 — StackedProgressBar.** Segment widths 0.40/0.18, clamp 0.7+0.6 to 0.7+0.3 of the track, negative draws 0, rounded track, 6dp and 12dp heights.
  - **SC5 — Registration + gates.** All four atoms listed in their family screens on device; registry/drift tests, apiCheck and detekt green.
- **Owner how-to-verify (run at milestone completion):**
  1. Read the Gate-1 log above for per-criterion evidence (signed captures in `phases/20-tone-foundation-atoms/20-04-uat-evidence/uatr-20261012T112516Z-132c1d6b/`).
  2. Judge the warn tone: the Explorer runs dynamic color, so on a Material You device `ToneDefaults.warn()` renders mauve, not the hub amber (`#8A6A89`/white light, `#A886A6`/black dark). Decide whether the Explorer should force the static hub schemes for tone previews, and confirm the hub amber look in the Phase 24 screencap review (the hub-scheme hex/contrast claim is Robolectric-only by design).
  3. Eyeball the `TextSmall` emphasis look: its 8dp x 4dp padding tokens are inert (code-review WR-02), so only the 48dp touch region is guaranteed, not a tight drawn size.
  4. Before the v2.6.0 cut, decide code-review IN-04 (`BarSegment` as a public `data class`).
- **Note:** Library-only phase, so the on-device surface is the Explorer gallery via a throwaway same-package harness (never committed). Nyquist: `20-VALIDATION.md` still `status: draft` / `nyquist_compliant: false`; run `gsd-validate-phase` section 6 to flip it. A capture in the run (`11-sc3-pillchip-disabled-pressed-screen.png`) is a mis-aimed press on the system nav bar and is not relied on.
