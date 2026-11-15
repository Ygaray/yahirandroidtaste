# The Undo button on the see-through deck: pick a, b or d

> **Decided 2026-11-14: a** (T-3163). Yahir called the 4.21:1 a non-issue that needed no fix. v2.8.1 ships today's teal.

With your picks (A inverse card, F 92% see-through, J 24dp margins), the dark-mode Undo button falls just
under the 4.5:1 contrast line that small text should meet. It happens when a bright colour shows through
the card. Body text stays well above the line (8.1:1 worst case). Below are three ways to colour Undo,
rendered on CalTracker's Daily Log in dark mode. Pick one letter.

**My pick: d.** It keeps the teal Undo you see today and clears 4.5:1 everywhere, and only an
eye looking for it will notice it is darker. b is the safest number but makes Undo look like plain text,
so it no longer reads as the card's one action.

| Option | Undo colour (dark mode) | Over black | Over cyan (worst) | Clears 4.5:1? |
|---|---|---|---|---|
| a | today's teal `#006875` | 4.77:1 | **4.21:1** | no |
| b | the card's text colour `#2B3235` | 9.00:1 | 8.18:1 | yes |
| d | darker teal `#00626F` | 5.15:1 | **4.54:1** | yes |

Light mode is not affected: the light-mode Undo stays above 6:1 with every option. The change ships in
YAT v2.8.1 with the rest of the deck.

## On CalTracker's Daily Log (dark)

The A F J M deck drawn over the real Daily Log capture, collapsed and fanned.

_Render: Undo options a, b, d on CalTracker's Daily Log, collapsed and fanned (kept in the private design record, not published)._

## Over the two worst backdrops

The card is 92% opaque, so 8% of whatever sits behind it shows through the card and the Undo label alike.
Black and saturated cyan are the backdrops that pull each option lowest.

_Render: Undo options a, b, d over black and over cyan, each labelled with its contrast (kept in the private design record, not published)._

## The label up close (over cyan, 3x)

_Render: The Undo label of a, b and d over cyan, zoomed three times (kept in the private design record, not published)._

Reply with one letter: a, b or d. a keeps today's colour and stays under 4.5:1 over bright content.

## Appendix: how these were made

- **Background:** CalTracker's Daily Log, captured in dark mode on the test phone on 2026-11-13, the same
  capture as the options page. The phone was not used again for this page.
- **Deck:** the real YAT v2.8.0 card and deck rendered by Compose in a Robolectric screenshot test, with
  your picked look applied: inverse colours, the whole deck at 92% as one layer, 24dp side margins. Only
  the Undo colour changes between a, b and d. The renderer draws no card shadow; on the phone each card
  also has its soft shadow.
- **How d was chosen:** the same hue and saturation as `#006875` (hue 187°), lowering only the lightness one
  step at a time, and taking the lightest teal that clears 4.5:1 over both black and cyan, measured inside
  the 92% layer as shipped. Its worst case over a 15-step grid of every RGB backdrop is also 4.54:1.
- **Contrast formula:** WCAG 2 relative luminance, the same mapping as the v2.8.1 test
  `FeedbackDeckContrastTest`. The Undo label and the card are both composited over the backdrop at 92%,
  then compared.
- **Ids:** D-1683 is your CalTracker Gate-2 note, T-3109 your A F J M picks, and T-3173 your ask to see this
  before choosing.
