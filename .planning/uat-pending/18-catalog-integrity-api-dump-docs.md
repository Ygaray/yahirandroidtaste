### Phase 18 — catalog-integrity-api-dump-docs (v2.5)

- **Status:** `pending`            <!-- pending | signed-off | signed-off-with-gap; owner adds date+name on sign-off -->
- **Milestone:** v2.5 (Voice UI Localization & Accessibility)
- **Gate 1 self-UAT log:** [`.planning/phases/18-catalog-integrity-api-dump-docs/18-02-SELF-UAT.md`](phases/18-catalog-integrity-api-dump-docs/18-02-SELF-UAT.md) — Verdict: **ALL 5 criteria PASS** (headless, non-device docs + evidence phase; no device driven; HEAD `a10c79e`, api.txt sha256 `218bd9c2acb1c01c3a988dfb462449dc3fd43f3ba387c88cb3ae01160d23740e`, 2026-10-05). Fresh uncached full suite 751 tests / 22 skipped / 0 failures / 0 errors, apiCheck + detekt green, v2.4.1 swap-baseline Metalava green, no v2.5 tag.
- **Items covered (5 ROADMAP success criteria):**
  - **SC1 / CAT-02 — registry drift guard green.** Four drift-guard classes ran fresh and passed in the full suite; 61 registered entries; the only added public composable names are the four registered hidden shims.
  - **SC2 / API-02 — api.txt current + additive vs v2.4.x.** `apiDump` is idempotent, `apiCheck` and the swap-baseline Metalava check vs v2.4.1 are green. The raw-line `tools/verify-api-additive.sh` exits 3 (10 lines, all independently confirmed as appended-defaulted-parameter rewrites), passed under the D-01 reading.
  - **SC3 / DOC-02 — docs updated.** API.md section 10 and INTEGRATION.md localization note cover all new label params, the Failure enrichment, and the router toggle; defaults match source.
  - **SC4 / INV-02 — one-way dependency + detekt zero baseline.** 10 added imports, none foreign; build/dependency/config files unchanged vs v2.4.1; detekt green, empty baseline.
  - **SC5 — no tag cut.** No `v2.5*` tag exists or points at HEAD.
- **Owner how-to-verify (run at milestone completion):**
  1. Read the Gate-1 log above for per-criterion evidence.
  2. Confirm the SC2 reading: accept "Metalava `apiCheck` green + `api.txt` current" (CONTEXT D-01) as satisfying "verify-api-additive passes", or amend the roadmap wording (the raw script exits 3 on a known false positive).
  3. Skim API.md section 10 and the INTEGRATION.md "Localizing the voice surface" note; confirm the `ProposedItemUiModel` trailing-lambda caveat (named `trailingContent = { ... }`) is acceptable as document-only.
  4. Before Phase 19: decide on the incorrect line 60 of `15-VERIFICATION.md` (claims trailing-lambda v2.4.0 shapes still bind for `ProposedItemUiModel`).
- **Note:** No on-device behavior; the library surface itself was device-verified in Phases 15-17 (see their fragments). Phase 19 owns the binary gate (`verify-binary-abi.sh`), the tag, JitPack, and the absolute-path `API_FILE` silent-SKIP guard defect.
