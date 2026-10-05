---
quick_id: 261005-eyu
mode: quick
autonomous: true
---
# Quick 261005-eyu: F4: write down the binary-compat rule

Docs-only. Run inline by the orchestrating session; no planner or executor was spawned.
Source: the control-plane fix plan §F4 and the yahir-gsd-control-plane-3b rulings (variant K; K members only for tagged shapes).

1. API.md: replace the v2.5.0 "NOT binary-compatible / must recompile" bullets with the restored-compat statement, and add § "The binary-compatibility rule".
2. .planning/codebase/CONVENTIONS.md: add § "Public API Evolution (binary compatibility)".
3. 15-CONTEXT D-01: append the correction (Metalava = source-level gate; javap AAR diff = binary gate).
4. 17-CONTEXT D-03: reword the data-class bullet to variant K, tagged shapes only (the F2 follow-up).
Excluded: root CLAUDE.md. Editing it needs Yahir's direct OK, which is pending.
