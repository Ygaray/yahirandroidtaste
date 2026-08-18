# ECOSYSTEM.md — the yahirandroidtaste hub & its consumers (read this before working across repos)

> **Audience: coding agents (and humans) working in `yahirandroidtaste` — this hub — or in any
> Android app that consumes it.** This is the constitution for how the repos relate, who has
> authority over what, and where new components go. When it conflicts with a single repo's stale
> note, this document wins — flag the drift. The authoritative pin is always each consumer's
> manifest + Gradle resolution; this doc is a best-effort cache.
>
> This hub uses **Mechanism B (Android / Gradle / JitPack)**. The ecosystem-wide repin procedure
> lives in the machine-config layer at `~/.claude/context/workflows/repin.md` (§ Mechanism B) and
> the cross-dependency map at `~/.claude/context/deps/_index.md`.

---

## 1. The shape — hub + spokes

This is **not one project.** It is a shared Android **design-system library** module plus N
independent apps that consume it:

- **The hub — `yahirandroidtaste`** (this repo; JitPack `com.github.Ygaray:yahirandroidtaste`;
  import root `io.github.ygaray.yahirandroidtaste`; public at
  `github.com/Ygaray/yahirandroidtaste`). A curated Compose UI component catalog: seven component
  families (cards, chips, sheets, buttons/FAB, pickers, feedback, empty-state), the theme tokens
  they read, the interaction conventions they enforce (reveal-confirm swipe, standardized snackbar
  feedback, conditional-render-no-dead-space), and a self-launching `ExplorerActivity` gallery.
  **Domain-agnostic UI mechanism** — it names no note, no card *content*, no module; a consumer
  passes its own data and callbacks into each composable.
- **The consumers (spokes)** — separate repos/GSD projects, each pinning the hub at an immutable
  JitPack tag:

  | Consumer | Repo | Dev checkout | Pins hub at | Pin file |
  |----------|------|--------------|-------------|----------|
  | SecondBrain | — | `~/Projects/SecondBrain` | *(pending — repin in Phase 103)* | `gradle/libs.versions.toml` |

  _(Best-effort cache — keep it current: a new consumer adds a row; a repin updates "Pins hub at".
  The authoritative pin is each consumer's manifest + `./gradlew :app:dependencies` resolution.
  No "Deploy host" column — Android apps are installed on devices, not daemon-deployed.)_

**Current published tag:** *none yet.* The first immutable tag is cut in **Phase 102** (LIB-, the
tag cut is human-gated), and SecondBrain's repin onto it is **Phase 103** (REPIN-). Until then the
hub exists as source + a green local build only; **do not fabricate a tag or a repin that has not
happened.**

**The load-bearing invariant (one-way dependency):** consumers import the hub; **no hub file ever
imports a consumer.** Everything app-specific — the data a card renders, the callbacks a sheet
fires, the module a chip filters — is *passed in* at the call site. A hub change is **inert** until
each consumer cuts a repin (new tag → bump the pin → re-resolve → rebuild → reinstall/re-verify).
This is the #1 thing that surprises people (and agents).

---

## 2. How a consumer actually consumes the hub (and why it matters for bug-fixing)

A consumer does **not** run the hub *source*. It runs the **pinned, resolved artifact**: the
`implementation("com.github.Ygaray:yahirandroidtaste:vX.Y.Z")` coordinate names an immutable JitPack
tag, which JitPack lazily builds from that tagged commit on first request. So "the hub" a running
consumer sees is an AAR built from a frozen commit — not your local `../Reusable/yahirandroidtaste`
working tree.

**This is the single most important thing to understand before you edit the hub from a consumer.**
If you fix a bug in `../Reusable/yahirandroidtaste` source and re-run the consumer's build, it
**still uses the old pinned AAR** — not your edit.

Two mechanisms bridge that gap:

- **Dev-time — the `mavenLocal()` overlay (Mechanism B's editable equivalent).** Publish the hub
  locally and let the consumer resolve it first:
  ```bash
  # from this hub repo (root IS the module — no module prefix, D-01):
  ./gradlew publishReleasePublicationToMavenLocal      # publishes local coordinate 1.0.0
  # in the consumer's settings.gradle.kts dependencyResolutionManagement:
  #   repositories { mavenLocal(); google(); mavenCentral(); maven { url = uri("https://jitpack.io") } }
  ```
  Use a **distinct local version string** (the local publish writes `1.0.0`, no `v` prefix) so it
  can't collide with an immutable JitPack tag (`vX.Y.Z`). This is **uncommitted, dev-only**. Revert
  by removing `mavenLocal()` and repinning the JitPack tag.
- **Confirm the repin actually landed.** Editing the version string proves nothing — Gradle/JitPack
  caching can silently resolve stale bytes:
  ```bash
  ./gradlew --refresh-dependencies :app:dependencies | grep yahirandroidtaste   # must print the NEW tag
  ```
- **Ship-time — the repin ritual.** To make a hub change *real* in a consumer, see §7. That step is
  **human-gated** (§3).

---

## 3. Cross-repo jurisdiction — you may fix the hub, but shipping it is gated

**You have standing authority to fix hub bugs from a consumer.** If a UI bug you hit in a consumer
actually lives in this shared library, the correct fix is *upstream in the hub*, not a consumer-side
work-around. Do not paper over a hub bug.

But respect the blast radius: **a hub change ripples to every consumer, present and future.**

- ✅ **Autonomous:** read/edit the hub source, add/adjust its tests, run the hub's own JVM /
  Robolectric / Compose-UI-test suite and detekt, and verify the fix live in a consumer via the
  `mavenLocal()` overlay (§2). Fix it, prove it, keep the suite green and detekt at its zero
  baseline.
- 🛑 **Surface and confirm before doing:** the step that changes *what a shipped app runs* — cutting
  a new immutable JitPack tag, repinning a consumer's coordinate, and reinstalling / device-
  re-verifying. Do everything up to that point, then stop and present the tag-cut + repin for human
  confirmation. This is the standing invariant in `~/.claude/context/workflows/repin.md` (human
  gates the tag cut).

---

## 4. Where new code goes — the tiers

| Tier | What it is | Where it lives |
|------|------------|----------------|
| **Generic component archetypes / theme tokens / interaction conventions** | A reusable visual component or design token any app could render (`AppChip`, `ConfirmationDialog`, `EmptyState`, `YahirAndroidTasteTheme`, the reveal-confirm swipe convention) | **The hub.** |
| **New concrete component built from existing primitives** | A composable that composes the hub's own building blocks and names no domain concept | **The hub** (register it in `ComponentRegistry` — the drift guard requires every public composable be registered XOR allowlisted). |
| **Consumer-specific screens / navigation / domain data** | Which module a screen shows, the Room data a card renders, the app's nav graph and ViewModels | **The consumer, forever.** Never promotes. |

## 5. The litmus — the one test that decides tier

> **"Could a *different* app render this with zero domain assumptions?"**

- **Yes** → it belongs in the **hub**. It must name no domain noun (no `note`, `module`, `voice
  card` *content* semantics) — it takes data + callbacks as parameters and renders.
- **No** → it stays in the **consumer**.

`yahirandroidtaste` is generic by construction — its components take content and callbacks as
parameters, hold no Room dependency, and name zero domain concepts. **The CATALOG drift guard**
(`ComponentRegistry` + its integrity test) enforces that every public composable is either
registered in the seven-family catalog or explicitly allowlisted — so a new hub component cannot be
added silently.

---

## 6. Extraction history — this hub was carved OUT of a consumer (consumer-first origin)

Unlike a hub-first library, `yahirandroidtaste` was **extracted from SecondBrain**: SecondBrain's
in-app `:yahirandroidtaste` design-system package (built up across SecondBrain Phases ~55–95: the
component families, `ComponentRegistry`, the explorer gallery, the reveal-confirm / snackbar /
conditional-render conventions) was lifted into this standalone repo (milestone v1.20, LIB-01/02 —
Phase 100). SecondBrain then repins to consume it as the published JitPack artifact (Phase 103),
keeping only its own screens, navigation, and Room domain in-app.

*(There is no `_promotable/` quarantine convention in these Android repos — the reusable code
originated by extraction, not by consumer-side incubation. This section records the actual history
rather than importing an unused pattern.)*

---

## 7. Versioning & the repin ritual (shipping a hub change)

- The hub uses **semver immutable JitPack tags** (`v1.0.0`, …). **Never `-SNAPSHOT`** (mutable +
  Gradle's ~24 h changing-module cache = stale-bytes false pass). A re-cut is a new version number.
- Removing/renaming a public composable, or changing a component's required parameters, is a
  **breaking change** → major-ish bump, human-gated. (The package rename in Phase 101 is itself such
  a break — every consumer import path changes.)
- Each consumer pins **one tag** and repins on its own cadence; one hub release does not force every
  consumer to move at once.
- The full, ordered ritual is **`~/.claude/context/workflows/repin.md` (§ Mechanism B)** — edit the
  coordinate → `--refresh-dependencies` + resolve-confirm → rebuild → run the suite → reinstall +
  device re-verify → update the four registries. It ends at a human-gated tag cut.

---

## 8. Onboarding a new consumer — the manual path (no scaffolder yet)

There is no `new_consumer` scaffolder for these Android hubs. To wire a new consumer by hand:

1. Add the JitPack repo in `settings.gradle.kts` `dependencyResolutionManagement.repositories`:
   `maven { url = uri("https://jitpack.io") }` (project-level repos are forbidden under
   `FAIL_ON_PROJECT_REPOS`).
2. Pin the immutable tag: `implementation("com.github.Ygaray:yahirandroidtaste:vX.Y.Z")` (or a
   `libs.versions.toml` catalog entry). Never `-SNAPSHOT`.
3. `./gradlew --refresh-dependencies :app:dependencies | grep yahirandroidtaste` — confirm it resolved.
4. Ensure the two consumer prerequisites hold: a Hilt-enabled `Application` (`@HiltAndroidApp`) so
   the library's `@Singleton` bindings aggregate into your `SingletonComponent`, and a Compose BOM
   aligned with the library's (2026.02.01). See this repo's `README.md`, `API.md`, and
   `INTEGRATION.md`.
5. Register the new consumer in §1 above **and** in `~/.claude/context/deps/_index.md` +
   `deps/yahirandroidtaste.md` (the drift rule — four registries must agree, manifest wins).

---

*Canonical hub constitution. Lives in the hub repo root (matching the backup-engine /
YahirReusableBot ECOSYSTEM convention). The §1 consumer table is a best-effort cache; the
authoritative pin is each consumer's manifest + Gradle resolution. Created SecondBrain milestone
v1.20, Phase 100 (LIB-05).*
