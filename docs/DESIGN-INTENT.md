# Design Intent — yahirandroidtaste

This document states what the hub *means to be*, per tier — distinct from `ComponentRegistry.kt`
(the registry of what exists) and `API.md` (the public surface). Where those enumerate, this
document sets intent: the contract each tier promises, and the litmus that decides which tier a
component belongs to.

Scope: this litmus applies to every component registered in `ComponentRegistry.entries`. Sub-parts
listed in `ComponentRegistry.INTENTIONALLY_UNREGISTERED` are infrastructure exercised indirectly
through their callers, not independently showcased — they sit outside the tier taxonomy entirely
and are not tiered.

## The Primitives Contract

A **primitive** is domain-agnostic presentation with zero opinions. It:
- Introduces no domain vocabulary in its name or its parameters.
- Renders only what the caller passes in — content, callbacks, and generic type parameters —
  never content it invents or assumes.
- Holds no interaction convention, no composition opinion, no business rule of its own.

A primitive is safe to drop into any consumer, in any domain, because it makes no assumptions
about what it is showing.

## The Patterns Contract

A **pattern** is an opinionated composition or interaction convention built from primitives (and
possibly other patterns). It:
- May introduce domain vocabulary in its name or parameters (e.g. a "card", a "tag").
- Bakes in a specific interaction convention (e.g. reveal-confirm destructive swipe) or a
  specific composition of primitives that the hub has decided is the *right* shape for that
  convention.
- Is still domain-agnostic in the sense that it names no *consumer's* concepts — it may say
  "card" or "tag" (the hub's own vocabulary), but never a specific consumer app's business
  objects.

A pattern is still reusable across consumers, but it carries an opinion the caller does not have
to re-derive.

## The Litmus

The test is decidable, not adjective-based — "simpler" vs. "more opinionated" is exactly the
kind of test that lets the same component be tiered two ways, which is why this litmus asks two
yes/no questions instead:

1. **Does the name or any parameter introduce a domain noun** (e.g. "Card", "Tag", "Voice",
   "Album")?
2. **Does the component render only caller-passed content**, with no baked-in interaction
   convention (swipe-to-reveal, modal-chrome pattern, etc.) or composition opinion of its own?

**PRIMITIVE** = "no" to (1) AND "yes" to (2).
**PATTERN** = "yes" to (1) OR "no" to (2) — either condition alone is enough to make it a
pattern.

## Worked Examples (the three borderline cases)

- **`CardBase` -> PATTERN.** Its name carries no domain noun on its own, but it bakes in the
  hub's reveal-confirm destructive-swipe interaction convention (left = delete, right = edit)
  via the shared `SwipeableActionRow` infrastructure — an opinionated interaction convention, not
  caller-supplied content. Fails condition (2).
- **`ChipBar` -> PRIMITIVE.** Fully generic (`fun <T> ChipBar(items: List<T>, key: (T) ->
  Any, itemContent: @Composable (T) -> Unit, ...)`), zero domain nouns in its name or
  parameters, and its own KDoc states it "holds no chip-rendering opinions and imports no
  `:app` type — it is pure presentation." Passes both conditions.
- **`HeatSwatch` -> PATTERN.** Takes no caller-supplied content — it hardcodes its own sample
  data and renders a specific visual convention (the Heat relatedness ramp) targeting mindmap
  nodes/edges. Fails condition (2): it does not render only caller-passed content.

## Applying the Litmus

When authoring a new component or tiering an existing one, ask both questions above in order. If
either yes/no lands you in PATTERN, stop — you don't need to weigh the two conditions against
each other, either one is sufficient. Read the component's actual public signature (not just how
a caller happens to use it) before deciding — usage patterns can be a proxy but the true test is
the signature and body, not the call site.
