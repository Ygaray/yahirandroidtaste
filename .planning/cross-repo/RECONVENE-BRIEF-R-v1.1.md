# Reconvene brief — yahirandroidtaste (YAT) — R-v1.1
**Milestone:** v2.5 (scoping — not yet created)  ·  **Contract rev read:** A1–A19 / E1–E6 (via HANDOFF; XR items from orchestrator dispatch)  ·  **Date:** 2026-10-05
**Prep only** — no `/gsd-new-milestone`, no plan, no tag before the R-v1.1 verdict. All five items verified against the ACTUAL `src/main` code (research duty), not contract wording.
**Rev A (2026-10-05):** XR-175-02(e) re-sketched per SB feedback — a Router ON/OFF toggle in the tier-policy card (`router`/`onRouterChange`, mirrors `offlineOnly`), not per-rung navigation; `onRungActivate` dropped (gesture collision gone). SB signed off on the model-field approach for the 175 literals.

## 0. Per-item verdict (what the orchestrator asked for)

| Item | Verdict | One-line additive API sketch | Additive? | Serves |
|---|---|---|---|---|
| **XR-172-01** key/model card gaps | **ACCEPT** | `ProviderKeyCard(..., providerLabel: String = "Provider")` + `ModelSelectCard(..., modelLabel: String = "Model")` — parameterize the two hardcoded dropdown labels (`ProviderKeyCard.kt:147`, `ModelSelectCard.kt:112`); `keyLabel`/`emptyReason`/`emptyProvidersReason` already exist | ✅ new defaulted params | SB 172 |
| **XR-175-02(a,b)** optional label params for English literals | **ACCEPT** | `ClarificationBar(..., dismissLabel: String = "Dismiss")`; `ApproachLadderCard(..., unavailableLabel/cappedLabel/needsNetworkLabel/onlineLabel/offlineOnlyLabel = <today>)`; OutcomeSheet literals via **model** fields: `HandledByUiModel.escalationsLabel`, `UndoRowState.Undone.label`, refused-prefix on undo model, `ProposedItemUiModel.removeContentDescription` | ✅ defaulted params + defaulted model fields | SB 175 |
| **XR-175-02(c)** min interactive size + selected semantics on approach rows | **ACCEPT** | internal: `RungRow`/`CapControl` gains `Modifier.minimumInteractiveComponentSize()` + `semantics { selected = isCapped; role = Role.RadioButton }` — **no public API change** | ✅ internal + semantics only | SB 175 |
| **XR-175-02(d)** Failure.action role, body slot, semantics prefix | **ACCEPT** | `FailureActionUiModel(..., role: ActionButtonDefaults.ActionButtonRole = Neutral)` (wire to `FailureBody:302`, today hardcoded `Neutral`); `Failure(..., body: (@Composable () -> Unit)? = null, semanticsPrefix: String? = null)` | ✅ new defaulted field + 2 nullable fields | SB 175 |
| **XR-175-02(e)** ApproachLadderCard Router ON/OFF toggle | **ACCEPT** | `ApproachLadderCard(..., router: Boolean? = null, onRouterChange: ((Boolean) -> Unit)? = null)` — a Router ON/OFF toggle in the tier-policy card, `null` hides it; **exactly** the `offlineOnly`/`onOfflineOnlyChange` `require()`-paired pattern (verified `ApproachLadderCard.kt:79`). No per-rung nav → no gesture collision. | ✅ 2 new nullable params | SB 177 SC3 |

**All five stay additive under the hub guards** (`api.txt` Metalava dump + `tools/verify-api-additive.sh` + pre-commit): every change is a new parameter/field carrying a default that preserves today's exact behavior, or internal-only — nothing removed or reshaped. This matches the established `emptyProvidersReason` and `NeedsConfirmation` precedent (defaulted additions already accepted as additive). I'll re-confirm with `verify-api-additive.sh` against a regenerated `api.txt` at plan time.

## 1. Phases (proposed — for discuss-milestone to ratify)
| Phase | Goal | Contract steps / seams touched | Needs from other repos |
|---|---|---|---|
| 1 | **i18n label params** — settings cards (XR-172-01) + voice-surface literals (XR-175-02 a,b) | §6.3; L7 (props only) | SB 172/175 confirm the exact literal set they need overridable |
| 2 | **a11y + Failure enrichment** — approach-row min-size/selected semantics (c) + Failure role/body/semanticsPrefix (d) | §6.3; A2 (needs-confirm sheet family) | SB 175 confirms Failure `role`/body usage |
| 3 | **ApproachLadderCard Router ON/OFF toggle** (XR-175-02 e) — `router`/`onRouterChange` pair | §6.3; L7 | SB 177 SC3 timing |
| 4 | **Catalog integrity + API dump + docs** — ComponentRegistry/CATALOG/API.md; regenerate `api.txt`. **Cuts NO tag.** | CATALOG-03 drift guard (full suite) | none |
| 5 | **Cut `v2.5.0`** — isolated so the cut follows a green Phase 4 (mirrors v2.4's Phase 13→14 split) | §11 tag protocol (A12→§11→A14) | SB/CT repin in Wave-1 after |

## 2. Public surface this milestone adds or changes (strictly additive)
- `ProviderKeyCard`: `providerLabel: String = "Provider"`.
- `ModelSelectCard`: `modelLabel: String = "Model"`.
- `ClarificationBar`: `dismissLabel: String = "Dismiss"`.
- `ApproachLadderCard`: `unavailableLabel`, `cappedLabel`, `needsNetworkLabel`, `onlineLabel`, `offlineOnlyLabel` (all `String` = today's text); `router: Boolean? = null` + `onRouterChange: ((Boolean) -> Unit)? = null` (Router ON/OFF toggle, `require()`-paired like `offlineOnly`, `null` hides it).
- `FailureActionUiModel`: `role: ActionButtonDefaults.ActionButtonRole = Neutral`.
- `VoiceOutcomeUiState.Failure`: `body: (@Composable () -> Unit)? = null`, `semanticsPrefix: String? = null`.
- `HandledByUiModel`: `escalationsLabel: String = "Escalations:"` (parameterizes the one literal in the private `HandledByRow`).
- Undo models (`UndoRowState.Undone`, refused model), `ProposedItemUiModel.removeContentDescription`: optional defaulted label fields.
- **Internal / no-API:** `RungRow`/`CapControl` min-interactive-size + selected semantics.

## 3. Assumptions about other repos (each needs peer confirm at R-v1.1)
- SB maps engine outcomes → these props and will pass **localized strings** itself (library ships English defaults only; it localizes nothing — INV-01).
- SB 177 SC3's "router" is a **Router ON/OFF toggle** in the tier-policy card (confirmed by SB at R-v1.1), a `Boolean?`/`(Boolean)->Unit` pair — **not** navigation; no route type crosses the seam. SB also signed off on the §2 model-field approach for the 175 literals.
- CT (§6.5) consumes `MicButton`/voice surface; assumed to need **none** of these label params forced (all defaulted). CT to confirm it isn't blocked.

## 4. Contract drift found (code vs contract)
- **`FailureActionUiModel` has no `role`** — `OutcomeSheet.FailureBody` hardcodes `ActionButtonRole.Neutral` (`OutcomeSheet.kt:302`). If the contract implies a destructive/primary Failure action is already expressible, that's drift — (d) closes it.
- **Dropdown labels `"Provider"`/`"Model"` are hardcoded** (not parameterized) — XR-172-01 is a real gap, not already-shipped.
- No other drift: `keyLabel`, `emptyReason`, `emptyProvidersReason`, and the full `NeedsConfirmation` label set (`confirmLabel`/`cancelLabel`/etc.) are already caller-supplied.

## 5. Proposed amendments
- None required — all five fit the existing §6.3 slice and additive-only directive. (If the orchestrator wants the i18n pattern — per-literal params vs a single labels-holder — pinned contract-side, that could be a minor erratum; see §6 Q2.)

## 6. Risks and open questions for Yahir
1. **(e) gesture collision — RESOLVED (SB, R-v1.1).** (e) is a Router ON/OFF toggle in the policy card, not per-rung nav; re-sketched as the `router`/`onRouterChange` pair (mirrors `offlineOnly`). No row-click ambiguity remains — no Yahir action needed here.
2. **i18n shape:** per-literal optional params (what's requested) grows param lists but is the most additive and matches precedent. Alternative: one `labels:` holder object. **Recommend per-param.**
3. **Batch vs split (e):** if SB 177 lands later than SB 172/175, do we still ship (e) in v2.5.0, or hold it for a v2.5.1 patch? Sequencing call.
4. **(c) visual nudge:** `minimumInteractiveComponentSize()` may grow short rung rows slightly — a Gate-2 visual check item (not an API change).

## 7. Tag / repin intent
- **Tag:** cut **`v2.5.0`** only after a green Phase 4→5, human-gated per root `CLAUDE.md` + A12 (tag-cut waiver still pending Yahir's direct confirm for this effort's YAT tag). Relay the full §11 row to the orchestrator; never cut before the R-v1.1 GO.
- **Repins (Wave-1, consumer channels):** SB → `v2.5.0` for phases 172/175/177; CT optional (all additive, defaults off). No consumer touched from this hub.
