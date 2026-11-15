# Snackbar deck look and Approach card title: pick one per row

You asked at CalTracker's Gate-2 (D-1683) for snackbar cards that stand out from the app, are somewhat
see-through, and don't run edge to edge. Below are real renders of each choice on CalTracker's Daily Log,
light and dark, with the deck collapsed (peek strips showing) and fanned. Pick one letter per row.

**My pick: A + E + I + L.** A is the only colour that clearly separates the card from CalTracker's cards
and background. E keeps the see-through feel without the macro bars showing through the text. I lines the
deck up with CalTracker's own 16dp card edges. L names the card where people scan for it.

| Row | Options | My pick |
|---|---|---|
| Card colour | A inverse (dark card in light mode, light in dark) · B tonal teal · C today's grey | **A** |
| See-through | D 100% (solid) · E 96% · F 92% · G 85% | **E** |
| Side margin | H 8dp · I 16dp · J 24dp | **I** |
| Approach card title | K none (today) · L title inside the card · M header above the card · N title + one-line subtitle | **L** |

Reply with four letters, e.g. "A E I L". The change ships as YAT v2.8.1, the default for every app (CalTracker
and SecondBrain pick it up when they repin).

## Card colour (A, B, C)

Shown at 92% opacity and 16dp margins.

_Render: Card colour options A, B, C (kept in the private design record, not published)._

| Option | Card vs app background (light / dark) | Text vs card, worst case |
|---|---|---|
| A inverse | 12.4:1 / 13.3:1 | 8.1:1 |
| B tonal | 1.2:1 / 1.8:1 | 5.9:1 |
| C today | 1.2:1 / 1.4:1 | 7.7:1 |

B and C stay close to CalTracker's own grey cards, which is what you flagged. A reads as a notification,
not as another list card.

## See-through (D, E, F, G)

Shown with colour A and 16dp margins. The whole deck fades as one layer, so the cards behind never show
through the front card; only the app behind does.

_Render: See-through options D, E, F, G (kept in the private design record, not published)._

At F (92%) the "Calories" label and the macro bars already show through the card text; at G (85%) they
compete with it. E (96%) keeps a hint of the screen behind while the text stays clean.

## Side margin (H, I, J)

Shown with colour A at 92%.

_Render: Side margin options H, I, J (kept in the private design record, not published)._

I (16dp) matches the left and right edges of CalTracker's own cards. J (24dp) narrows the card enough that
the long food name wraps one word earlier.

## Approach card title (K, L, M, N)

The card that holds Local grammar, On-device AI and Single-shot has no heading today, so it was easy to
miss in Settings. The title is an optional parameter: apps that pass none look exactly as today.

_Render: Approach card title options K, L, M, N (kept in the private design record, not published)._

L puts the title inside the card, like other titled cards in the hub. M is a small teal section header
above it (a Settings-group look). N adds a one-line explanation under the title, which costs a line of
height.

## Appendix: how these were made

- Backgrounds: CalTracker's Daily Log captured on the test phone in light and dark (2026-11-13), not
  drawn.
- Deck and card: the real YAT v2.8.0 components rendered by Compose (a Robolectric screenshot test) on
  top of those captures, with only the colour, opacity and margin changed. The renderer draws no card
  shadow, so on the phone each card also has its usual soft shadow.
- Contrast numbers use the hub colour scheme CalTracker runs on; "worst case" assumes pure white or black
  showing through at 8%.
- Decision ids: D-1683 is your CalTracker Gate-2 note, Q-430 its question, D-1684 your ask for these
  options.
