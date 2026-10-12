---
quick_id: 261005-eyu
status: complete
---
# Quick 261005-eyu Summary: F4 binary-compat rule docs

- API.md now says v2.5.0 is binary-compatible with v2.4.x, through hidden composable overloads and variant-K data-class synthetics proven by a javap diff with missing=0. The new § "The binary-compatibility rule" covers: never change a tagged signature in place; Metalava = source gate; javap AAR diff = binary gate (becomes tools/verify-binary-abi.sh at the cut); the composable shim recipe; the variant-K data-class recipe (JvmOverloads covers Java callers only); and shims for tagged shapes only, added at the cut.
- CONVENTIONS.md has a condensed copy of the rule.
- 15-CONTEXT D-01 is corrected.
- 17-CONTEXT D-03's data-class bullet is reworded to K.
- **Not done:** root CLAUDE.md. Editing it needs Yahir's direct OK (a peer request alone isn't enough), and that OK is pending.
