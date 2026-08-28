# Preview-02 Fidelity — Implementation Plan

> **For agentic workers:** each `### Task N` below is self-contained. You will be given exactly one task and no
> conversation history. Read your task top to bottom, read the reference files it names, and do only what it says.
> Steps use checkbox (`- [ ]`) syntax for tracking. Do not "improve" files outside your task's **Files:** list —
> other tasks are editing those files in parallel and your edit will be lost or will cause a merge conflict.

**Source of truth:** `docs/superpowers/specs/2026-08-06-create-theme-customizer-design.md` (Status: Accepted).
This plan implements that spec and supersedes `docs/superpowers/plans/2026-08-06-create-theme-customizer.md`.

**Goal:** a pixel-faithful Scala.js / Laminar port of shadcn-svelte's `/create/preview-02`: create shell with
customizer + iframe preview + floating switcher + Initialize Project dialog, the exact 33-card preview-02 mosaic
(34 site source files: 1 grid + 33 cards), full preset encode/decode, per-field locks, undo/redo history,
randomize/reset, dark-mode shortcut, and 5 icon libraries.

**Repository:** `/Users/elam/Personal/shadcn-scalajs`, branch `fix/alert-new-york-v4-fidelity`.
(The repo was moved here from `/Users/elam/Projects/shadcn-scalajs`. That old path is dead — never use it.)

**Reference checkouts (read-only; port from them, never invent layout or copy):**

- `/Users/elam/Personal/shadcn-svelte` — the reference implementation.
- `/Users/elam/Personal/shadcn-scalajs/vendor/shadcn-source` — vendored shadcn/ui snapshot.

**Tech stack:** Scala 3 / Scala.js / Laminar (`modules/core`, `modules/ui`, `modules/blocks`, `modules/site`),
Tailwind CSS v4 (`modules/site/src/styles/globals.css`), Vite, sbt. No Scala test framework is configured —
verification is compile + format + a headless-Chrome evidence check (see Global Constraints).

---

## Orchestration

Three waves. **Wave 1S is strictly sequential.** **Wave 1P runs fully in parallel with Wave 1S and with itself.**
**Wave 2 is fully parallel but must not start until every Wave 1 task has landed.** **Wave 3 is sequential.**

| Wave | Tasks | Parallel-safe? | File-disjointness guarantee |
|---|---|---|---|
| **1S — sequential foundations** | 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 | **No.** Run strictly in numeric order; each consumes the previous task's interfaces. | n/a |
| **1P — parallel foundations** | 10, 11, 12, 13, 14, 15, 16, 17 | **Yes**, with each other *and* with all of Wave 1S. | Tasks 10–16 each own a disjoint set of files under `modules/ui/`; Wave 1S touches only `modules/site/` and (in Task 5) `modules/ui/Icons*`. Task 17 owns `modules/site/package.json` + one new facade file. No file appears in two tasks' **Files:** lists. |
| **2 — parallel fan-out** | 18, 19, 20, 21 (icon libraries) and 22, 23, 24, 25, 26, 27, 28, 29 (card batches) | **Yes**, all twelve simultaneously. | Tasks 18–21 each own exactly one file under `modules/ui/icons/`. Tasks 22–29 each own a disjoint subset of the 33 files under `modules/site/.../create/preview02/`. **No Wave 2 task may edit `Preview02.scala`** — Task 9 already created it with all 33 call sites and Task 30 does the fidelity audit. |
| **3 — integration** | 30 → 31 → 32 | **No.** | n/a |

**Blocking edges to respect:**

- Tasks 22–29 (cards) require **all** of Wave 1P (primitives, charts, QR) and Task 5 (Icons catalog) and Task 9
  (stub files exist to overwrite).
- Tasks 18–21 (icon library data) require Task 5 (defines the concept-name list and the data-object shape).
- Task 30 requires all of Wave 2.

**Total: 32 tasks.**

---

## Global Constraints

Every worker must obey all of these.

### Build and verification

- **No Scala test framework exists in this repo** (verified: no `src/test` dirs, no munit/utest/scalatest in
  `build.sbt`). Verification is compile + format + a **headless-Chrome evidence check**. A green compile is *not*
  sufficient evidence of a finished task.

#### Who runs the build: the orchestrator, not you

Tasks run concurrently against a **single shared working directory**, so build tooling cannot be run per-task:
two `sbt` processes in one project contend on the same build lock, and one worker's
`lsof -ti:4300 | xargs -r kill` teardown would kill the dev server another worker is mid-verification against.

Therefore, **as a worker you must NOT**:

- run `sbt` (any task: `compile`, `scalafmtAll`, `fastLinkJS`, …)
- start, restart, or kill a dev server, or kill anything on port 4300
- run `npm run dev`, `npm install`, `./scripts/test`, or `git commit`

The orchestrator owns a long-running dev server on `http://localhost:4300` and runs one
`sbt <modules>/compile + scalafmtAll` **gate** after each wave (and after each Wave 1S task), then the
browser-evidence checks, then dispatches targeted fix tasks for anything that fails.

**What you do instead:** write the code, then *self-review it as if you were the compiler* before reporting done —
re-read every file you wrote against the code conventions below (especially the `Tag`-suffixed Laminar tags and
`Button.of`), confirm every symbol you referenced actually exists by grepping for its definition, and confirm your
imports match. Then report exactly which files you wrote and any place you were unsure. **Report honestly** — an
"I could not find the right API for X" is far more useful than a plausible guess that fails the gate.

Your task's **observable check** still matters: state, in your final response, precisely what the orchestrator
should look for on screen to confirm your work landed. That check will be run for you.

- If you need to inspect the running site read-only, `curl -s http://localhost:4300/...` is fine. Do not modify it.
- Node/browser scripts are the orchestrator's job; do not write verification scripts into `modules/site/`.

### Code conventions (these have all caused real bugs in this codebase)

0. **NEVER splat `mods*` into a Laminar element alongside other modifiers.** This single mistake caused roughly
   two thirds of Wave 1's compile failures, across six different files. Laminar element constructors take a
   repeated `Modifier[El]*`, and Scala 3 forbids mixing individual arguments with a splat for the *same* repeated
   parameter. Laminar already treats a `Seq[Modifier]` as one `Modifier`, so **drop the star**:

   ```scala
   // WRONG — "Sequence argument type annotation `*` cannot be used here"
   div(cls := "x", mods*)
   Separator(Separator.Orientation.Horizontal, cls := "y", mods*)
   div(cls := "x", mods.map(_(ItemApi))*)

   // RIGHT
   div(cls := "x", mods)
   Separator(Separator.Orientation.Horizontal, cls := "y", mods)
   div(cls := "x", mods.map(_(ItemApi)))
   ```

   `mods*` is legal **only** when it is the sole argument. A trailing `*` on a mapped collection
   (`content.map { … }*`) is wrong for the same reason. Related: a spread can never be followed by another
   argument — `title(mods*, text)` does not compile; append instead: `title((mods :+ (text: Modifier[HtmlElement]))*)`.
   Also note `Separator.apply`'s **first** parameter is an `Orientation`, so `Separator(cls := "…")` is a type
   error — pass `Separator(Separator.Orientation.Horizontal, cls := "…")`.

1. **Laminar tag-name collisions.** These tags are only exposed with a `Tag` suffix: `sectionTag`, `detailsTag`,
   `summaryTag`, `dialogTag`, `menuTag`, `commandTag`, `headerTag`, `footerTag`, `navTag`, `articleTag`,
   `asideTag`, `mainTag`, `timeTag`, `progressTag`. Bare `div`, `span`, `button`, `ul`, `li`, `ol`, `hr`, `figure`,
   `label`, `select`, `option`, `table`/`thead`/`tbody`/`tr`/`td`/`th` work fine. `HtmlTag` and `DetachedRoot` need
   explicit imports (`com.raquo.laminar.tags.HtmlTag`, `com.raquo.laminar.nodes.DetachedRoot`).
2. **`Button.apply` has NO default variant/size** (unlike upstream cva `defaultVariants`). A bare `Button("Save")`
   renders 20px tall and transparent. **Always** `Button.of(_.variant(...), _.size(...), ...)`.
3. **New SVGs must set an explicit `size-4`-style class directly on the `<svg>`** — do not rely on an ancestor's
   `[&_svg:not([class*='size-'])]:size-4` rule existing.
4. **Icon helpers must return an `SvgElement`, never a wrapper `<div>`/`<span>`.** A wrapper breaks
   `.accordion > details > summary > svg:last-child` and every other direct-child selector in `globals.css`.
   (Regression guard: this was fixed in commit `970c7b9` — do not reintroduce it.)
5. **Use the `cn-*` class prefix** for any new root-level class name that could collide with the legacy basecoat
   CSS still imported by `globals.css` (`@import "./basecoat.generated.css"`).
6. **Never name a parameter or `Var` `value`, `children`, `content`, `label`, `title`, `disabled`, or
   `onClick`** inside a scope that also needs the Laminar key of that name — it shadows the key and produces
   baffling errors like `value := is not a member of Boolean`. Use `codeVar`, `titleText`, `isDisabled`,
   `clickObserver`, etc.
7. **`js.Date` getters return `Double`; the constructor wants `Int`** — `.toInt` the getters.
8. **Click-outside checks must use `ev.composedPath()`, not `ev.target`.** `composedPath` isn't typed in the pinned
   scalajs-dom facade — cast through `js.Dynamic` (see `DropdownMenu.scala`'s `compPath`).
9. **`children` collides inside `ScElementBase` subclasses.** Not relevant to this plan (no
   `modules/webcomponents` work), but do not add any.
10. **There is no `dom.html.HTMLElement`.** The `org.scalajs.dom.html` object holds *short* aliases only —
    `html.Element = HTMLElement`, `html.Input = HTMLInputElement`, `html.Div = HTMLDivElement`. Write
    `dom.html.Element` (or the fully-qualified `dom.HTMLElement`), never `dom.html.HTMLElement`. This slipped through
    twice in Wave 1S (`ThemeConfig.scala` ×3, `CreateState.scala` ×1) and was caught only by inspection, because
    `modules/site` cannot compile while `modules/ui` is red.
11. **`Signal.now` is `protected[airstream]`** — you cannot call `state.presetCode.now()`. Read from the backing
    `Var` instead (`state.config.now()`), or subscribe. Also: `presetCode` is already a `Signal`, so write
    `state.presetCode.map(...)`, never `state.presetCode.signal.map(...)`.
12. **Icon helpers take `Modifier[SvgElement]*`** — pass `svg.cls := "size-4"`, never bare `cls :=` (that is an
    `HtmlAttr` setter). Most icons already bake in `size-4`, so `Icons.maximize()` with no mods is usually enough.
13. **`Field.group`, `Card.footer`, `Card.header`, `Card.content` take a single `mods*` list** — put children in that
    list (`Field.group(cls := "…", childA, childB)`), never `Field.group(cls := "…")(childA, childB)`. The second
    param-list form treats the returned `HtmlElement` as a function and produces baffling tuple/`Unit` mismatches.
14. **`Button.of` only accepts `(ButtonApi => Modifier)*`** — wrap every non-builder arg as `_ => …`
    (`Button.of(_.variant(…), _.size(…), _ => cls := "…", _ => "Label")`). Passing bare `cls :=` / children
    positionally fails with a tuple-type error.
15. **Picker / dropdown children are `Modifier[HtmlElement]*`, not `HtmlElement*`** — so strings and `SvgElement`s
    are legal. For sync reads of a `Signal` (keyboard nav), mirror into a `Var` via `onMountBind`; do not call
    `Signal.now`. `aria.checked` wants a `String` (`"true"`/`"false"`), not a `Boolean`.

### CSS / Tailwind constraints

- Tailwind v4 auto source detection is scoped to `modules/site` (its cwd). `globals.css` lines 15–16 already add
  `@source` for `modules/ui` and `modules/blocks`, so those are covered. **Any new source tree outside those three
  globs will not be scanned** — do not put Scala that carries Tailwind classes anywhere else.
- Arbitrary variants like `style-lyra:md:w-[2600px]` require a `@custom-variant` declaration. Task 3 adds them; do
  not invent your own variant syntax.
- **Specificity discipline for dark mode.** `.dark` and `[data-base-color="X"]` are both specificity (0,1,0) and
  both land on `<html>`, so whichever is later wins and one control silently goes inert. The fix already in the
  file (commit `c0490aa`) is to write compound `.dark[data-base-color="X"]` / `.dark[data-theme-color="X"]` rules
  at specificity (0,2,0). **Every new dark token block must follow that compound form.** Do not add a bare `.dark`
  block after the attribute blocks.
- Do not use `a[class*="bg-accent"]`-style substring matching on class lists — it also matches inactive links'
  `hover:bg-accent`. Target a stable `data-*` hook instead (regression guard, commit `d59392e`).

### Hard non-goals — no task may do these

1. **Do not port the switcher "02" mosaic** (`shadcn-svelte/docs/src/lib/registry/examples/create/preview/`, a
   separate 33-card set). Keep the floating 01/02 switcher UI with 01 (= `preview-02`) active; 02 shows a
   "coming soon" affordance and does not navigate.
2. **Do not package preview-02 for the CLI.** It is site-only under `modules/site`, never a `modules/blocks` block,
   never a registry item, never written by `packages/cli add`.
3. **Do not touch `modules/blocks/src/main/scala/shadcnscalajs/blocks/dashboard01/` or its registry entry** — not
   one character. `/create` simply stops using it. Blocks routes keep working unchanged.
4. **Do not unify the four duplicated site headers.** Only touch the `Create` nav links the shell needs.
5. **Do not chase welcome-dialog marketing copy parity** beyond structure/behavior.
6. Do not add `modules/webcomponents` wrappers for any new primitive.

### Reconciliation with the prior, now-superseded implementation

A previous `/create` implementation is already committed on this branch, built from the old draft spec. This plan
**evolves** some of it and **deletes** the rest. Concretely:

| Existing artifact | Disposition |
|---|---|
| `modules/site/.../site/ThemeConfig.scala` | **Evolved in place** (Task 2): new value vocabularies, new defaults (nova/neutral), `fontHeading` gains `inherit`, `menuAccent` `solid`→`bold`, `menuColor` gains translucent variants, preset-string round-trip. |
| `modules/site/.../site/CreatePage.scala` (115 lines, 10 bare `<select>`s, inline `Dashboard01`) | **Deleted** (Task 6). Replaced by `shadcnscalajs.site.create.*`. |
| `Main.scala` `/create` route branch (line 43) | **Rewritten** (Task 6) into three branches: `/create`, `/create/preview-02`, `/preview/preview-02`. |
| `Create` nav link in 4 header blocks (`Main.scala` ×3, `BlocksLayout.scala` ×1) | **Kept as-is.** No edits needed; `/create` still exists. |
| `modules/ui/.../Icons.scala` + `Icons.registry.json` (2 libraries, 4 concepts) | **Restructured** (Task 5) into a concept catalog + 5 per-library data objects; the 4 existing concepts are migrated, not rewritten. |
| `globals.css` `[data-*]` attribute blocks | **Extended** (Task 3): base colors `gray`/`slate` removed, `mauve`/`olive`/`mist`/`taupe` added; theme/chart palettes go from 17 to 24; fonts from 3 to 26. |
| `globals.css` `.cn-dashboard-01[data-menu-*]` rules (bottom of file) | **Left untouched.** They are scoped to `.cn-dashboard-01` and become inert once `/create` stops rendering `Dashboard01`. Removing them would count as touching dashboard-01 behavior. |

**Defects listed in prior reviews that are already fixed on this branch** — treat these as *regression guards*, not
as work items. Verify you have not reintroduced them; do not "fix" them again:

- Dark mode disabling Base/Theme Color (`c0490aa`) — compound `.dark[data-…]` selectors are in place.
- Icon wrapper `<div>` breaking `> svg` selectors (`970c7b9`) — `Icons` already returns `SvgElement`.
- `a[class*="bg-accent"]` matching inactive links (`d59392e`) — replaced with `a[data-active="true"]`.
- Tailwind not scanning `modules/ui` / `modules/blocks` (`@source` lines 15–16 of `globals.css`).
- Theme FOUC on navigation (`af09085`) — blocking pre-paint script exists in `index.html`.
- Missing Icons registry sidecar (`772e57c`).

---

# Wave 1S — Sequential foundations

Run tasks 1 → 9 in order. Each depends on the one before it.

---

### Task 1: Preset codec (`Preset.scala`)

**Context:** The create page's entire state is a single short base62 code (e.g. `bA3f`) that must round-trip with
shadcn-svelte's own codes. This task ports the codec only — no UI, no state, no DOM.

**Reference file to port from:** `/Users/elam/Personal/shadcn-svelte/packages/cli/src/preset/preset.ts` (284 lines).
Read it completely. The value arrays, bit widths and field order there are load-bearing: reorder anything and
shared codes stop round-tripping.

**Files:**
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/Preset.scala`

**Interfaces:**
- Produces:
  - `Preset.Styles: List[String]` (8 entries, order: nova, vega, maia, lyra, mira, luma, sera, rhea)
  - `Preset.BaseColors: List[(String, String)]` name→hex (7: neutral #737373, stone #79716B, zinc #71717B, mauve #79697B, olive #7C7C67, mist #67787C, taupe #7C6D67)
  - `Preset.Themes: List[(String, String)]` name→hex (24, exact order from `PRESET_THEMES`: neutral, stone, zinc, amber, blue, cyan, emerald, fuchsia, green, indigo, lime, orange, pink, purple, red, rose, sky, teal, violet, yellow, mauve, olive, mist, taupe)
  - `Preset.ChartColors: List[String]` (= theme keys)
  - `Preset.IconLibraries: List[(String, String)]` key→title (lucide→"Lucide", tabler→"Tabler", hugeicons→"HugeIcons", phosphor→"Phosphor", remixicon→"Remix Icon")
  - `Preset.Fonts: List[String]` (26, exact order from `PRESET_FONTS`)
  - `Preset.FontHeadings: List[String]` (= `"inherit" :: Fonts`)
  - `Preset.Radii: List[(String, String, String)]` name/label/value (default 0.5rem, none 0rem, small 0.45rem, medium 0.625rem, large 0.875rem)
  - `Preset.MenuAccents: List[String]` (subtle, bold)
  - `Preset.MenuColors: List[String]` (default, inverted, default-translucent, inverted-translucent)
  - `final case class PresetConfig(style, baseColor, theme, chartColor, iconLibrary, font, fontHeading, radius, menuAccent, menuColor)` — all `String`
  - `Preset.default: PresetConfig` (first value of every v2 field array: nova / neutral / neutral / neutral / lucide / inter / inherit / default / subtle / default)
  - `Preset.encode(cfg: PresetConfig): String`
  - `Preset.decode(code: String): Option[PresetConfig]`
  - `Preset.isPresetCode(s: String): Boolean`
  - `Preset.randomConfig(): PresetConfig`

- [ ] **Step 1: Port the value arrays and the field table**

Two field tables, exactly as in the reference:

```
V1 ("a", 40 bits): menuColor 3, menuAccent 3, radius 4, font 6, iconLibrary 6, theme 6, baseColor 6, style 6
V2 ("b", 51 bits): V1 ++ (chartColor 6, fontHeading 5)
```

- [ ] **Step 2: Port base62 + encode/decode**

Alphabet: `0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz`. Current version prefix `"b"`; valid
versions `"a"`, `"b"`.

**Critical: use `Double` arithmetic, not Scala `Int`/bitwise ops.** The packed value is 51 bits and Scala's `Int`
is 32 bits. The reference deliberately uses `bits += idx * 2 ** offset` and `Math.floor(bits / 2**offset) % 2**bits`
for the same reason. In Scala.js use `Double` and `math.floor`, and index base62 digits with
`(n % 62).toInt` after a `math.floor` division. A `Long` also works and is safer — but `Long` in Scala.js is
emulated and slower; `Double` is exact to 2^53 and 51 bits fits, so prefer `Double`.

Decoding an `"a"` code must set `fontHeading = "inherit"` and `chartColor` to the v2 default (`"neutral"`).
Out-of-range indices fall back to `values.head` (do not throw). Unknown version prefix, empty string, length < 2, or
any non-base62 character ⇒ `None`.

`isPresetCode`: length 2..10, valid version prefix, all remaining chars in the alphabet.

- [ ] **Step 3: `randomConfig()`** — uniform pick from each v2 field array. No biases here (Task 4 owns biases).

- [ ] **Step 4: Compile and format**

```bash
cd /Users/elam/Personal/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" \
  && sbt -batch site/compile scalafmtAll
```

- [ ] **Step 5: Observable check — round-trip against the reference implementation**

The codec is pure, so verify it against real reference output rather than by eyeballing. From the repo root:

```bash
cd /Users/elam/Personal/shadcn-svelte/packages/cli && node -e '
const p = require("./src/preset/preset.ts");' 2>/dev/null || true
```

If the reference cannot be executed directly (it is TypeScript), instead hand-compute and assert these three
fixtures inside a throwaway Scala.js check driven through the browser (Task 6 has no page yet, so use
`sbt site/fastLinkJS` + a temp Node script under `modules/site/` that imports the linked output and calls the
exported functions is *not* available either — `Preset` is not exported to JS).

**Therefore the accepted evidence for this task is a temporary `main` shim:** temporarily add to
`Main.main()`'s very first line `dom.console.log(shadcnscalajs.site.create.Preset.selfCheck())`, where
`selfCheck()` is a temporary method you add to `Preset` that returns a string. It must assert:

1. `encode(default) == "b0"` (all indices are 0 ⇒ packed value 0 ⇒ base62 `"0"`).
2. `decode(encode(c)) == Some(c)` for 200 `randomConfig()` values.
3. `decode("a0") == Some(default.copy(fontHeading = "inherit"))`.
4. `decode("!!") == None`, `decode("b") == None`, `decode("") == None`.
5. A known non-trivial round-trip: `encode(default.copy(style = "rhea", radius = "large", menuColor = "inverted-translucent"))` decodes back to exactly that config.

Start the dev server, load `http://localhost:4300/`, capture `console` output with puppeteer-core, and confirm it
prints `OK 5/5`. **Then remove the `selfCheck()` method and the `Main.main()` shim line**, recompile, and confirm
`git diff --stat` shows only `Preset.scala` as new and `Main.scala` unchanged.

- [ ] **Step 6: Teardown + commit**

Delete the temp script, `lsof -ti:4300 | xargs -r kill`.

```bash
cd /Users/elam/Personal/shadcn-scalajs
git add modules/site/src/main/scala/shadcnscalajs/site/create/Preset.scala
git commit -m "Add preset codec matching shadcn-svelte's base62 v2 format"
```

---

### Task 2: Evolve `ThemeConfig` onto the preset vocabulary

**Context:** `ThemeConfig` is the site-wide persisted theme state. It currently uses the *old draft's* vocabulary
(defaults `lyra`/orange, base colors including `gray`/`slate`, fonts `"default"`, menu accent `"solid"`). The
Accepted spec replaces all of that with the reference vocabulary from Task 1. Because this site has **no
client-side router** — every path is a real page load and `Main.main()` re-runs with fresh `Var`s — persistence
must go through `localStorage` plus the URL `?preset=` param.

**Files:**
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/ThemeConfig.scala`
- Modify: `modules/site/index.html` (the blocking pre-paint script only)

**Interfaces:**
- Consumes: `shadcnscalajs.site.create.Preset` (Task 1).
- Produces:
  - `final case class ThemeConfig(stylePack, darkMode: Boolean, baseColor, themeColor, chartColor, headingFont, bodyFont, iconLibrary, radius, menuColor, menuAccent)` — field *names* unchanged, *values* now from `Preset`.
  - `ThemeConfig.default` = `Preset.default` mapped over (nova / neutral / neutral / neutral / inherit heading / inter body / lucide / default / default / subtle), `darkMode = false`.
  - `ThemeConfig.fromPreset(c: PresetConfig, darkMode: Boolean): ThemeConfig` and `ThemeConfig.toPreset(cfg): PresetConfig`.
  - `ThemeConfig.load(): ThemeConfig`, `ThemeConfig.store(cfg): Unit`, `ThemeConfig.applyToDocument(cfg): Unit`,
    `ThemeConfig.applyToDocument(cfg, doc: dom.Document): Unit` (iframe variant).

- [ ] **Step 1: Replace the defaults and add coercion**

Change the case-class defaults to the Accepted values. `load()` currently trusts whatever string is in
`localStorage`. It must now **coerce**: any persisted value not present in the corresponding `Preset` list falls
back to that field's default. This matters because real users on this branch already have `"lyra"`, `"orange"`,
`"gray"`, `"slate"`, `"default"` (font) and `"solid"` (menu accent) in `localStorage`, and those values no longer
have CSS behind them. Keep the existing `Try { ... }.getOrElse(default)` outer guard.

- [ ] **Step 2: Add preset bridging**

`toPreset` / `fromPreset` map the 10 preset fields; `darkMode` is **orthogonal to the preset code** (it is not one
of the packed fields) and is persisted separately.

- [ ] **Step 3: Extend `applyToDocument`**

Keep the existing `<html>`-level `data-style-pack` / `data-base-color` / `data-theme-color` / `data-chart-color` /
`data-heading-font` / `data-body-font` / `data-icon-library` / `data-radius` attributes and the `dark` class add/
remove. **Add**:

- `data-menu-color` and `data-menu-accent` on `<html>` (today they are only ever set on the dashboard block).
- For `inverted` / `inverted-translucent`: add the class `dark` to every `.cn-menu-target` element.
  For `default` / `default-translucent`: remove it.
- For `*-translucent`: add class `cn-menu-translucent` to every `.cn-menu-target`; otherwise remove it.
- Because menus may mount **after** `applyToDocument` runs, install a `dom.MutationObserver` on
  `document.body` (subtree, childList) that re-applies the menu classes to any newly added `.cn-menu-target`.
  Mirror the reference's MutationObserver pattern in
  `/Users/elam/Personal/shadcn-svelte/docs/src/lib/features/design-system/components/design-system-provider.svelte`.
  Install it exactly once (guard with a private `var observerInstalled: Boolean`).

Add the second overload `applyToDocument(cfg, doc: dom.Document)` that does the same against an arbitrary
document, so the create page can push theme into the preview `<iframe>`'s document directly.

- [ ] **Step 4: Update the pre-paint script in `index.html`**

`modules/site/index.html` has a blocking `<script>` that mirrors `applyToDocument` by reading the same
`localStorage` key before first paint. Add `data-menu-color` and `data-menu-accent` to it. Leave everything else in
that script alone. **Do not** touch the `<link>` font tags or the `<script type="module">` line — Task 3 owns those.

- [ ] **Step 5: Compile and format** — `sbt -batch site/compile scalafmtAll`.

- [ ] **Step 6: Observable check**

Start the dev server. With puppeteer-core, on `http://localhost:4300/`:

```js
await page.evaluate(() => localStorage.setItem("shadcn-scalajs:theme",
  JSON.stringify({ stylePack: "lyra", baseColor: "gray", themeColor: "orange", bodyFont: "default", menuAccent: "solid" })));
await page.reload({ waitUntil: "networkidle0" });
const attrs = await page.evaluate(() => Object.fromEntries(
  [...document.documentElement.attributes].filter(a => a.name.startsWith("data-")).map(a => [a.name, a.value])));
console.log(JSON.stringify(attrs));
```

Expected: `data-base-color` is `"neutral"` (not `"gray"` — coerced), `data-body-font` is `"inter"` (not
`"default"`), `data-menu-accent` is `"subtle"` (not `"solid"`), `data-style-pack` is `"lyra"` (still valid, so
*not* coerced), `data-menu-color` is present and `"default"`. Print the JSON into the task log as evidence.

- [ ] **Step 7: Teardown + commit**

```bash
git add modules/site/src/main/scala/shadcnscalajs/site/ThemeConfig.scala modules/site/index.html
git commit -m "Move ThemeConfig onto the reference preset vocabulary with value coercion"
```

---

### Task 3: CSS tokens, custom variants, fonts, and the two-segment-path script fix

**Context:** `globals.css` (973 lines) drives everything through `<html>` attribute selectors. It currently covers
5 base colors, 17 themes, 17 chart colors and 3 fonts. The Accepted spec needs 7 base colors, 24 themes, 24 chart
colors and 26 fonts, plus new layout utilities for the create shell and the mosaic.

**Files:**
- Modify: `modules/site/src/styles/globals.css`
- Modify: `modules/site/index.html` (font `<link>` and the module `<script src>` only)

**Interfaces:**
- Consumes: the attribute contract from Task 2's `applyToDocument`.
- Produces: CSS custom properties for every selectable combination; `@custom-variant style-lyra` / `style-mira`;
  `.cn-menu-target` / `.cn-menu-translucent` hooks.

Hex values for base colors and themes are in
`/Users/elam/Personal/shadcn-svelte/packages/cli/src/preset/preset.ts` (`PRESET_BASE_COLORS`, `PRESET_THEMES`).
Full token ramps are in `/Users/elam/Personal/shadcn-scalajs/vendor/shadcn-source` and in the already-generated
`modules/site/src/styles/shadcn-presets.generated.css`.

- [ ] **Step 1: Base colors — replace the set**

Remove the `[data-base-color="gray"]` and `[data-base-color="slate"]` blocks and their `.dark[data-base-color=…]`
counterparts. Keep `neutral`, `stone`, `zinc`. Add `mauve`, `olive`, `mist`, `taupe`, deriving their ramps at the
same lightness steps the existing blocks use, tinted toward the hue of their `PRESET_BASE_COLORS` hex.

**Each new base color needs both a light block `[data-base-color="X"]` and a compound dark block
`.dark[data-base-color="X"]`.** Never a bare `.dark` block after the attribute blocks — see the specificity rule in
Global Constraints.

- [ ] **Step 2: Themes and chart colors — go from 17 to 24**

Existing: red, orange, amber, yellow, lime, green, emerald, teal, cyan, sky, blue, indigo, violet, purple, fuchsia,
pink, rose. **Add:** neutral, stone, zinc, mauve, olive, mist, taupe. Same for `[data-chart-color="X"]` (the chart
blocks set `--chart-1`..`--chart-5`). Each needs its `.dark[data-theme-color="X"]` / `.dark[data-chart-color="X"]`
compound counterpart.

- [ ] **Step 3: Fonts — go from 3 to 26**

Add `[data-body-font="X"]` and `[data-heading-font="X"]` blocks for every entry in `Preset.Fonts` (26 values, listed
in `packages/cli/src/preset/preset.ts` `PRESET_FONTS`), plus `[data-heading-font="inherit"]` which sets
`--font-heading-token: var(--font-body)`. Delete the now-dead `"default"` blocks.

- [ ] **Step 4: Load every font in `index.html`**

Extend the Google Fonts `<link href="https://fonts.googleapis.com/css2?...">` to cover all 26 families. A font
option with no `@font-face` behind it is a dead option and counts as a defect. Keep `preconnect` links.

- [ ] **Step 5: Fix the two-segment-path script URL — REQUIRED, do not skip**

`index.html` currently has `<script type="module" src="./index.js">`. That is **relative**. At `/create` it
resolves to `/index.js` and works; at `/create/preview-02` and `/preview/preview-02` (this feature's new routes) it
resolves to `/create/index.js` and 404s, so the whole page renders blank with no Scala.js. Change it to the
absolute `src="/index.js"`. This is not in the spec — it is a latent bug this feature is the first to hit.

- [ ] **Step 6: Custom variants and menu hooks**

```css
@custom-variant style-lyra (&:is([data-style-pack="lyra"] *));
@custom-variant style-mira (&:is([data-style-pack="mira"] *));
```

Place these next to the existing `@custom-variant dark` on line 18. Then add the menu hooks:

```css
.cn-menu-translucent { /* translucent surface treatment, see reference menu-color-picker.svelte */ }
```

`.cn-menu-target` itself needs no rule — it is a marker class the Scala side toggles `dark` /
`cn-menu-translucent` on (Task 2 Step 3).

- [ ] **Step 7: Shell + mosaic layout utilities**

The shell uses `--customizer-width`, `--gap`, `--preview-height`, `--header-height`; the mosaic uses
`w-[2400px]`/`md:w-[3000px]`/`style-lyra:md:w-[2600px]`, `[contain-intrinsic-size:380px_1200px]`,
`[content-visibility:auto]`, `contain-[paint]`, `*:[div]:gap-(--gap)`, and `data-slot="capture-target"`. All of
these are Tailwind arbitrary values that get emitted from the class strings in the Scala sources under
`modules/site` (which Tailwind does scan). **Confirm `--header-height` is defined**; if `globals.css` does not
already define it, add `--header-height: 3.5rem;` to `:root`. Add nothing else here — do not hand-write utilities
Tailwind will generate.

- [ ] **Step 8: Compile check**

There is no Scala change here. Run `cd modules/site && npm run build` and confirm it exits 0 with no PostCSS/
Tailwind error.

- [ ] **Step 9: Observable check**

Start the dev server. With puppeteer-core:

```js
const probe = async (attrs) => page.evaluate((a) => {
  Object.entries(a).forEach(([k, v]) => document.documentElement.setAttribute(k, v));
  const s = getComputedStyle(document.documentElement);
  return { primary: s.getPropertyValue("--primary").trim(), bg: s.getPropertyValue("--background").trim(),
           chart1: s.getPropertyValue("--chart-1").trim(), font: s.getPropertyValue("--font-body").trim() };
}, attrs);
```

Assert all of the following and print them:
1. `data-base-color=taupe` (light) yields a `--background` different from `neutral`'s.
2. Add class `dark` to `<html>`, then `data-base-color=taupe` **still** yields a different `--background` from
   `data-base-color=neutral` in dark mode. *(This is the exact regression from commit `c0490aa`. If the two are
   equal, your new dark blocks are not compound-specificity and the task is not done.)*
3. Same two-step check for `data-theme-color=mist` vs `neutral` on `--primary`, in light **and** dark.
4. `data-chart-color=olive` changes `--chart-1`.
5. All 26 `data-body-font` values produce 26 distinct `--font-body` strings.
6. `document.querySelector('script[type=module]').getAttribute("src") === "/index.js"`.

- [ ] **Step 10: Teardown + commit**

```bash
git add modules/site/src/styles/globals.css modules/site/index.html
git commit -m "Expand theme tokens to the reference vocabulary and fix module script path"
```

---

### Task 4: Create state — locks, history, randomize, reset, URL sync

**Context:** The customizer's behavior lives here: which fields are locked, undo/redo over the preset string,
biased randomization, and keeping `?preset=` in the URL. The reference packs all of this into one class.

**Reference files to port from:**
- `/Users/elam/Personal/shadcn-svelte/docs/src/lib/features/design-system/components/design-system-provider-state.svelte.ts` (364 lines) — the whole state machine.
- `/Users/elam/Personal/shadcn-svelte/docs/src/routes/(app)/(layout)/(create)/lib/randomize-biases.ts` (100 lines) — the bias tables.

**Files:**
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/CreateState.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/RandomizeBiases.scala`

**Interfaces:**
- Consumes: `Preset` (Task 1), `ThemeConfig` (Task 2).
- Produces `final class CreateState`:
  - `val config: Var[ThemeConfig]`, `val presetCode: Signal[String]`
  - `def update(f: PresetConfig => PresetConfig): Unit` (pushes history, writes localStorage, syncs URL)
  - `val locks: Var[Map[String, Boolean]]`, `def toggleLock(key: String): Unit`, `def isLocked(key: String): Signal[Boolean]`
  - `def undo(): Unit`, `def redo(): Unit`, `val canUndo: Signal[Boolean]`, `val canRedo: Signal[Boolean]`
  - `def randomize(): Unit`, `def reset(): Unit`, `def toggleDark(): Unit`
  - `def shareUrl: String`, `def applyPresetCode(code: String): Boolean`
  - `def installShortcuts(): Unit`

- [ ] **Step 1: Construction and persistence**

Seed order, matching the reference: URL `?preset=` if present and valid → else `localStorage` key
`design-system-preset` → else `Preset.encode(Preset.default)`. `darkMode` seeds from the existing
`shadcn-scalajs:theme` localStorage entry (Task 2's key), not from the preset code.

Locks persist under localStorage key `locks` as a JSON object with **twelve** keys, matching the reference
`Lockable` type exactly (so codes/locks stay structurally compatible): `style, baseColor, theme, chartColor,
iconLibrary, font, fontHeading, item, menuAccent, menuColor, radius, template`. `item` and `template` are unused by
this port but are kept for structural parity. All default `false`.

- [ ] **Step 2: URL sync**

On every `update`, write `?preset=<code>` using `dom.window.history.replaceState` — **`replaceState`, not
`pushState`**, and no scroll jump (do not touch `scrollTop`). Preserve every other existing search param and the
hash. `shareUrl` = `origin + "/create?preset=" + code` (the spec prefers `/create?preset=` because `/create`
redirects while preserving `search`).

- [ ] **Step 3: History**

An undo/redo stack over the encoded preset **string** (not the config object), mirroring the reference's
`StateHistory`. `undo`/`redo` move a cursor; a fresh `update` truncates the redo tail. `canUndo`/`canRedo` are
`Signal[Boolean]` so menu items can disable.

- [ ] **Step 4: `RandomizeBiases.scala`**

Port `randomize-biases.ts` literally:
- `CHART_COLOR_PAIRINGS`: the 17-entry `Map[String, List[String]]` (red→teal,sky; orange→teal,blue; amber→cyan,indigo; yellow→sky,violet; lime→indigo,pink; green→purple,rose; emerald→purple,red; teal→fuchsia,red; cyan→rose,amber; sky→red,yellow; blue→orange,yellow; indigo→amber,yellow; violet→yellow,lime; purple→green,lime; fuchsia→lime,teal; pink→green,cyan; rose→emerald,sky).
- `fonts` bias: style `lyra` ⇒ only `jetbrains-mono`.
- `radius` bias: style `lyra` ⇒ only `none`; style `rhea` ⇒ everything except `large`.
- `chartColors` bias: if the chosen theme has a pairing, restrict to it (unless that leaves an empty list).

- [ ] **Step 5: `randomize()`**

Follow the reference order exactly, and **skip every locked field** (a locked field keeps its current value):
baseColor → style → (build context) → themes available for the base color → biased fonts → biased radii → theme →
biased chart colors → chartColor → font → **fontHeading: 70% `inherit`, else a font from a different category than
the body font** → radius → iconLibrary → menuAccent → menuColor. Then one single `update` with the whole config.

- [ ] **Step 6: Style-driven radius coercion**

After any style change (from the picker *or* from randomize), coerce radius: `lyra` and `sera` ⇒ `none`; `rhea`
must never end on `large` (fall back to `medium`). This mirrors `design-system-provider.svelte`. Apply it inside
`update`, so it cannot be bypassed.

- [ ] **Step 7: `reset()` and `toggleDark()`** — `reset` sets `Preset.default` (does not clear locks, matching the
reference); `toggleDark` flips `darkMode` and persists it.

- [ ] **Step 8: `installShortcuts()`**

A `document` keydown listener: `R` randomize, `⇧R` reset, `D` toggle dark, `⌘/Ctrl+Z` undo, `⇧⌘/Ctrl+Shift+Z` redo.
**Every one of these must no-op when the event target is an `<input>`, `<textarea>`, `<select>`, or a
`contentEditable` element** (match the reference guard). Detect the target via `ev.composedPath()` head, cast
through `js.Dynamic` — not `ev.target` (Global Constraints #8).

- [ ] **Step 9: Compile, format, observable check**

`sbt -batch site/compile scalafmtAll`. `CreateState` has no page yet, so verification is deferred to Task 7 Step 5,
which exercises locks/undo/redo/randomize through real UI. **State this deferral explicitly in your commit
message** so the next worker knows it is intentional.

- [ ] **Step 10: Commit**

```bash
git add modules/site/src/main/scala/shadcnscalajs/site/create/CreateState.scala \
        modules/site/src/main/scala/shadcnscalajs/site/create/RandomizeBiases.scala
git commit -m "Add create-page state: locks, history, biased randomize, URL preset sync

Browser verification deferred to the customizer task (no UI mounts this yet)."
```

---

### Task 5: Icons — 5-library catalog and per-library data objects

**Context:** `modules/ui/Icons.scala` today resolves 4 concepts (chevronDown, chevronsUpDown, check, x) across 2
libraries by reading `<html data-icon-library>`. preview-02 and the create shell need ~55 concepts across 5
libraries. Putting 275 path strings in one file would make a single unreviewable task, so this task builds the
**structure** and **Wave 2 fills the four non-Lucide libraries in parallel**.

**Reference:** the concept list comes from the `lucide="…"` props on `IconPlaceholder` across
`/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/*.svelte` (46 distinct)
plus the create shell's own icons in
`/Users/elam/Personal/shadcn-svelte/docs/src/routes/(app)/(layout)/(create)/components/`.
Library metadata: `/Users/elam/Personal/shadcn-svelte/packages/cli/src/icons/libraries.ts`.

**Files:**
- Modify: `modules/ui/src/main/scala/shadcnscalajs/ui/Icons.scala`
- Modify: `modules/ui/src/main/scala/shadcnscalajs/ui/Icons.registry.json`
- Create: `modules/ui/src/main/scala/shadcnscalajs/ui/icons/LucideIconData.scala`
- Create: `modules/ui/src/main/scala/shadcnscalajs/ui/icons/TablerIconData.scala`
- Create: `modules/ui/src/main/scala/shadcnscalajs/ui/icons/HugeiconsIconData.scala`
- Create: `modules/ui/src/main/scala/shadcnscalajs/ui/icons/PhosphorIconData.scala`
- Create: `modules/ui/src/main/scala/shadcnscalajs/ui/icons/RemixIconData.scala`

**Interfaces:**
- Produces:
  - `Icons.Concepts: List[String]` — the canonical, alphabetically sorted concept-name list (kebab-case, e.g. `"arrow-left-right"`).
  - `trait IconDataSource { def viewBox: String; def strokeBased: Boolean; def paths: Map[String, Seq[String]] }`
  - Five objects implementing it, one per file above.
  - `Icons.activeLibrary: Signal[String]` (unchanged behavior).
  - `def Icons.icon(concept: String)(mods: Modifier[SvgElement]*): SvgElement` — the generic resolver.
  - One named helper per concept, e.g. `def Icons.arrowLeftRight(mods: Modifier[SvgElement]*): SvgElement`.
  - The 4 existing helpers (`chevronDown`, `chevronsUpDown`, `check`, `x`) keep their exact current signatures — **existing call sites must not break.**

> **LANDED — authoritative result.** Task 5 shipped **58 concepts**, not the ~55 estimated below (the audit of the
> cards plus the create shell yielded a superset). `Icons.Concepts` in `Icons.scala` is the single source of truth —
> **Tasks 18–21 must key their `paths` maps off `Icons.Concepts` verbatim**, not off the estimate in Step 1.
> `LucideIconData` is fully populated for all 58 and is the **fallback** for any concept a library omits, so a
> partially-filled library still renders. Two concepts have no native Lucide glyph and use stand-ins there —
> `square-lock` and `square-unlock` borrow `lock-keyhole` / `lock-keyhole-open`; **Task 19 (Hugeicons) should supply
> the real SquareLock/SquareUnlock glyphs.** Lucide name aliases already resolved: `alert-circle`→`circle-alert`,
> `more-horizontal`→`ellipsis`, `pie-chart`→`chart-pie`, `dice-faces`→`dice-5`, `undo`→`undo-2`,
> `file-bar-chart`→`file-chart-column`. Icons Lucide draws with `<circle>`/`<rect>`/`<line>` were converted to
> equivalent path `d` strings so `paths: Map[String, Seq[String]]` stays uniform — do the same for your library.

- [ ] **Step 1: Define the concept list (55 names)**

From cards (46): activity, alert-circle, arrow-left-right, arrow-right, audio-lines, bell, book-open, building-2,
calendar, camera, car, chevron-down, chevron-right, circle-help, circle-plus, cloud, coffee, credit-card,
file-bar-chart, file-text, gauge, globe, image, layout-dashboard, lock, lock-keyhole, message-square,
more-horizontal, paintbrush, pie-chart, plus, refresh-cw, repeat, search, shield, shopping-cart, sun, target,
thermometer, timer, trending-up, tv, user, volume-2, wallet, x.

From the create shell (9 more): check, chevrons-up-down, menu, maximize, minimize, copy, square-lock, square-unlock,
dice-faces, undo, square-terminal, moon. *(Some overlap; de-duplicate and sort. If your final list is not exactly
55, that is fine — but it must be a superset of the two source lists above, and it must be sorted.)*

- [ ] **Step 2: Write `IconDataSource` and the five data files**

Each file is one object with a `Map[String, Seq[String]]` from concept name to SVG `d` path strings. `viewBox` and
stroke-vs-fill differ per library — Lucide/Tabler are `0 0 24 24` stroke-based; Hugeicons is `0 0 24 24`
stroke-based; Phosphor is `0 0 256 256` fill-based; Remix Icon is `0 0 24 24` fill-based. Model that with the
`viewBox` / `strokeBased` members so the resolver can set `fill`/`stroke` correctly.

**Fill `LucideIconData` completely in this task** (all 55 concepts, migrating the 4 existing Lucide paths verbatim).
Create the other four files with the same object shape and **an empty `paths` map plus a doc comment naming the
Wave 2 task that fills them** (Tasks 18–21). They must compile as-is.

- [ ] **Step 3: Rewrite the resolver**

```scala
def icon(concept: String)(mods: Modifier[SvgElement]*): SvgElement
```

- Reads `activeLibrary`; picks the matching `IconDataSource`; **falls back to `LucideIconData` for any concept the
  active library does not define** (this is what lets Wave 2 land incrementally without a blank screen).
- Returns an `SvgElement` **directly** — never wrapped. See Global Constraints #4.
- Sets `svg.cls := "size-4"` by default (Global Constraints #3), overridable by a caller-supplied `svg.cls`.
- Sets `aria.hidden := true`.
- For stroke-based libraries: `fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
  stroke-linejoin="round"`. For fill-based: `fill="currentColor" stroke="none"`.
- Keep the existing `MutationObserver` on `data-icon-library`.

Generate the 55 named helpers as thin wrappers over `icon("<name>")`.

- [ ] **Step 4: Update `Icons.registry.json`** — add the five new `icons/*.scala` files to the `files` array with
`"type": "scala:ui"`. Keep `"name": "icons"`.

- [ ] **Step 5: Compile, format** — `sbt -batch ui/compile site/compile blocks/compile scalafmtAll`. All three must
compile: `Icons` is consumed by Accordion/Combobox/dashboard-01 today.

- [ ] **Step 6: Observable check — the wrapper-div regression guard**

Start the dev server, load `http://localhost:4300/components/accordion`, and assert with puppeteer-core:

```js
const ok = await page.evaluate(() => {
  const s = document.querySelector(".accordion > details > summary");
  const last = s && s.lastElementChild;
  return { tag: last && last.tagName, isSvg: last instanceof SVGElement,
           rotates: last && getComputedStyle(last).transition.includes("transform"),
           box: last && last.getBoundingClientRect().width };
});
```

Expected: `tag === "svg"`, `isSvg === true`, and `box` is ~16 (the `size-4` class landed on the `<svg>` itself, not
~150px). Then click the summary and confirm the chevron rotates (the `[open] > summary > svg:last-child` rule
fires). Also load `/components/combobox` and confirm its check icon is ~16px. Print all measurements.

- [ ] **Step 7: Teardown + commit**

```bash
git add modules/ui/src/main/scala/shadcnscalajs/ui/Icons.scala \
        modules/ui/src/main/scala/shadcnscalajs/ui/Icons.registry.json \
        modules/ui/src/main/scala/shadcnscalajs/ui/icons/
git commit -m "Restructure Icons into a 55-concept catalog with per-library data sources"
```

---

### Task 6: Create shell, preview frame, switcher, and the three routes

**Context:** This task deletes the old monolithic `CreatePage.scala` and replaces it with the reference's shell
structure. The site has **no client-side router** — every path is a real page load handled by the `pathname`
dispatch chain in `Main.main()`.

**Reference files to port from:**
- `/Users/elam/Personal/shadcn-svelte/docs/src/routes/(app)/(layout)/(create)/create/+layout.svelte` (48 lines) — the shell.
- `.../(create)/components/preview.svelte` (41 lines) — the preview frame + maximize button.
- `.../(create)/components/preview-switcher.svelte` (36 lines) — the floating 01/02 switcher.
- `.../(app)/preview/[item]/+page.svelte` — the chrome-less preview route + minimize button.
- `.../(create)/create/+page.server.ts` — the `/create` → `/create/preview-02` redirect preserving `url.search`.

**Files:**
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/CreateShell.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/PreviewFrame.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/PreviewSwitcher.scala`
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/Main.scala` (the dispatch chain in `main()` **only** — do not touch the four header nav blocks or anything else in this 2215-line file)
- Delete: `modules/site/src/main/scala/shadcnscalajs/site/CreatePage.scala`

**Interfaces:**
- Consumes: `CreateState` (Task 4), `ThemeConfig` (Task 2), `Preset` (Task 1).
- Produces: `CreateShell.apply(state: CreateState, customizer: HtmlElement): HtmlElement`,
  `PreviewFrame.apply(state, item: String): HtmlElement`, `PreviewSwitcher.apply(activeItem: String): HtmlElement`.

- [ ] **Step 1: Routing in `Main.main()`**

Replace the single line `else if pathname == "/create" || pathname == "/create/" then CreatePage()` with:

```scala
else if pathname == "/create" || pathname == "/create/" then
  dom.window.location.replace("/create/preview-02" + dom.window.location.search)
  div()  // never rendered; the replace() navigates away
else if pathname == "/create/preview-02" then CreatePageEntry()
else if pathname == "/preview/preview-02" then PreviewOnlyPage()
```

`location.replace` (not `assign`) so `/create` does not land in the back-history — this is the closest equivalent
to the reference's server-side 303. `search` must be preserved verbatim.

- [ ] **Step 2: `CreateShell`**

Port the exact class strings from `+layout.svelte`. Root element:

```
group/layout relative z-10 flex h-svh flex-col overflow-hidden section-soft
[--customizer-width:--spacing(56)] [--gap:--spacing(4)] md:[--gap:--spacing(6)]
[--preview-height:calc(100svh-var(--header-height)-2rem-150px)]
md:[--preview-height:calc(100svh-var(--header-height)-2rem)]
```
with `dataAttr("slot") := "layout"`. Inside: the site header (reuse the existing header markup pattern from
`Main.scala`'s primary header — **copy it into `CreateShell`, do not refactor `Main.scala`'s headers**; header
unification is an explicit non-goal), then:

```
main[data-slot="designer"]:
container-wrapper flex min-h-0 flex-1 flex-col gap-(--gap) p-(--gap)
pt-[calc(var(--gap)*0.25)] md:flex-row-reverse
```

Children of `main`, in this order: the preview frame (`flex-1`), then the customizer column. **`md:flex-row-reverse`
is what puts the customizer on the right on desktop — do not build a left-rail `<aside>`.** Remember `mainTag`, not
`main` (Global Constraints #1).

For this task, pass in a placeholder customizer element (`div(cls := "md:w-(--customizer-width)")`); Task 7 replaces
it. Wire `state.config.signal --> ThemeConfig.applyToDocument` and call `state.installShortcuts()` on mount.

- [ ] **Step 3: `PreviewFrame`**

Port `preview.svelte`:

```
div[data-slot="preview"]: relative -mx-1 flex flex-1 flex-col justify-center overflow-hidden rounded-2xl border border-border sm:mx-0
  inner: z-0 mx-auto flex max-h-(--preview-height) w-full flex-1 flex-col overflow-y-auto
    maximize Button (absolute top-2 right-2 isolate z-10, variant ghost, size icon-sm)
      → href "/preview/preview-02?preset=<code>&fromPreview=true"
    iframe(src := "/preview/preview-02?preset=<code>", cls := "h-(--preview-height)", title := "preview-02")
    PreviewSwitcher
```

The maximize button must be `Button.of(_.variant("ghost"), _.size("icon-sm"), ...)` (Global Constraints #2). The
iframe `title` must be the item name (accessibility requirement in the spec).

**Iframe theme sync:** the iframe is same-origin, so on each `state.config` change call
`ThemeConfig.applyToDocument(cfg, iframeEl.contentDocument)` (Task 2's overload), guarding for `null` before the
iframe has loaded and re-applying on the iframe's `load` event. Do **not** reload the iframe on every theme change.

- [ ] **Step 4: `PreviewSwitcher`**

Port `preview-switcher.svelte`. Two buttons, labels `"01"` and `"02"`; note the reference maps label `"01"` →
item `preview-02` and label `"02"` → item `preview`. Container:

```
dark absolute right-3 bottom-3 z-20 flex items-center gap-1 rounded-xl bg-card/90 p-1 shadow-xl backdrop-blur-xl
```
Buttons:
```
h-7 min-w-8 cursor-pointer rounded-lg px-2.5 text-xs font-medium text-muted-foreground transition-colors
hover:text-foreground data-[active=true]:bg-accent data-[active=true]:text-accent-foreground
```

`01` carries `data-active="true"` and does nothing when clicked (it is already active). **`02` must not navigate**
— it is an explicit non-goal. Give it `aria-disabled="true"`, `title="Coming soon"`, and visible non-color state
(e.g. the text stays muted and it never gains `data-active`). Do not rely on color alone to distinguish active
(spec accessibility requirement).

- [ ] **Step 5: `PreviewOnlyPage` and `CreatePageEntry`**

Put both small entry functions in `CreateShell.scala`.

- `CreatePageEntry()` — builds a `CreateState`, returns `CreateShell(state, placeholder)`.
- `PreviewOnlyPage()` — chrome-less: applies the theme from `?preset=` + localStorage, renders
  `preview02.Preview02()` (Task 9 creates it — for **this** task render a temporary
  `div("preview-02 placeholder")` and leave a `// TODO(Task 9)` comment), and when
  `?fromPreview=true` is present shows a minimize `Button.of(_.variant("ghost"), _.size("icon-sm"))` at
  `absolute top-2 right-2 isolate z-10` linking back to `/create/preview-02` with `search` preserved (minus
  `fromPreview`).

- [ ] **Step 6: Delete `CreatePage.scala`** — `git rm`. Confirm nothing else references it (`rg CreatePage`).

- [ ] **Step 7: Compile and format** — `sbt -batch site/compile site/fastLinkJS scalafmtAll`.

- [ ] **Step 8: Observable check**

Start the dev server. With puppeteer-core assert and print:

1. `GET /create?preset=b0` ends up at `/create/preview-02?preset=b0` (check `page.url()`), and the search param
   survived.
2. On `/create/preview-02`: `document.querySelector('[data-slot="designer"]')` exists; its computed
   `flex-direction` is `row-reverse` at viewport width 1280 and `column` at width 500.
3. `getComputedStyle(document.querySelector('[data-slot="layout"]')).getPropertyValue("--customizer-width")` is
   non-empty, and `--preview-height` resolves to a positive pixel value.
4. An `<iframe>` exists with `title === "preview-02"` and a `src` starting `/preview/preview-02`.
5. `/preview/preview-02` loads with **no** site header and **no** `[data-slot="layout"]`.
6. `/preview/preview-02?fromPreview=true` shows the minimize link and its `href` starts `/create/preview-02`.
7. **Scala.js actually booted on the two-segment path**: on `/preview/preview-02`,
   `document.getElementById("root").children.length > 0`. *(If this is 0, Task 3 Step 5's `/index.js` fix is
   missing or regressed.)*

- [ ] **Step 9: Teardown + commit**

```bash
git add -A modules/site/src/main/scala/shadcnscalajs/site/
git commit -m "Replace CreatePage with the reference create shell, preview frame, and routes"
```

---

### Task 7: Customizer — pickers, lock buttons, main menu

**Context:** The customizer is the dark, floating card on the right of the create shell. The old implementation
used ten bare `<select>` elements; the reference uses rich picker menus with per-field lock buttons.

**Reference files to port from** (all under
`/Users/elam/Personal/shadcn-svelte/docs/src/routes/(app)/(layout)/(create)/components/`):
`customizer.svelte` (89), `customizer-controls.svelte` (17), `picker/*` (12 files), `style-picker.svelte` (70),
`base-color-picker.svelte` (94), `theme-picker.svelte` (99), `chart-color-picker.svelte` (97),
`font-picker.svelte` (111), `icon-library-picker.svelte` (266), `radius-picker.svelte` (89),
`menu-color-picker.svelte` (140), `menu-accent-picker.svelte` (86), `menu-icon-default.svelte`,
`menu-icon-inverted.svelte`, `lock-button.svelte` (39), `main-menu.svelte` (77), `random-button.svelte` (39),
`reset-button.svelte` (46).

**Files:**
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/Picker.scala` (shared picker chrome: root/trigger/content/group/label/item/radio-item/checkbox-item/separator/shortcut)
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/LockButton.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/Customizer.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/Pickers.scala` (all nine field pickers)
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/MainMenu.scala`
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/create/CreateShell.scala` (swap the Task 6 placeholder for the real `Customizer`)

**Interfaces:**
- Consumes: `CreateState` (Task 4), `Preset` (Task 1), `Icons` (Task 5), existing `Card`, `Field`, `Separator`, `Button`, `Tooltip`, `DropdownMenu`, `Kbd` from `modules/ui`.
- Produces: `Customizer.apply(state: CreateState): HtmlElement`.

- [ ] **Step 1: `Picker.scala`**

The reference picker is a Bits-UI dropdown. This repo has no floating-portal primitive (a separate spec owns
that), so build the picker on the existing `DropdownMenu` interaction pattern — an absolutely-positioned panel with
a `documentEvents(_.onMouseDown)` outside-click close that uses **`ev.composedPath()`, not `ev.target`**
(Global Constraints #8; copy `DropdownMenu.scala`'s `compPath` helper). Escape closes. Arrow keys move the active
item. Each picker trigger needs a visible focus ring (spec accessibility requirement) and the
`group/picker` class, because `LockButton` reveals itself on `group-hover/picker` / `group-focus-within/picker`.

- [ ] **Step 2: `LockButton.scala`**

Port `lock-button.svelte`:

```
flex size-4 cursor-pointer items-center justify-center rounded opacity-0 transition-opacity
group-focus-within/picker:opacity-100 group-hover/picker:opacity-100 focus-visible:opacity-100
data-[locked=true]:opacity-100 pointer-coarse:hidden
```
`data-locked` reflects `state.isLocked(key)`. **Accessible name is mandatory**: `aria-label` = `"Lock <Field>"` /
`"Unlock <Field>"` (e.g. `"Lock style"`), not just `"Lock"`. Use `Icons.squareLock` / `Icons.squareUnlock`
(Task 5). This repo's `Tooltip` is a `title`-attribute span, not a floating tooltip — use it as-is; do not build a
floating tooltip.

- [ ] **Step 3: `Pickers.scala` — nine pickers**

Style, BaseColor, Theme, ChartColor, Font (heading), Font (body), IconLibrary, Radius, MenuColor, MenuAccent. Each:
a labelled trigger showing the current value, a menu of options, and a `LockButton`. Port the option chrome from
each reference picker — color swatches for base/theme/chart, font-name-rendered-in-that-font rows for the font
pickers, radius previews for radius, the `menu-icon-default` / `menu-icon-inverted` SVG previews for menu color.

**IconLibraryPicker** additionally shows, per library, the library name plus a preview row of glyphs rendered in
that library (reference `icon-library-picker.svelte`). Five entries: Lucide, Tabler, HugeIcons, Phosphor,
Remix Icon.

Values come from `Preset.*`; writes go through `state.update`.

- [ ] **Step 4: `Customizer.scala` and `MainMenu.scala`**

Port `customizer.svelte`'s structure exactly:

```
outer: top-24 right-12 isolate z-10 flex min-h-0 w-full flex-col gap-2 self-start md:w-(--customizer-width)
Card (size sm): dark max-h-(--preview-height) min-h-0 w-full gap-0 rounded-2xl bg-card/90 shadow-xl backdrop-blur-xl
  Card.header: hidden items-center justify-between gap-2 border-b px-3! md:flex   → MainMenu
  Card.content: no-scrollbar min-h-0 flex-1 overflow-x-auto overflow-y-hidden p-0 md:overflow-y-auto
    Field.group(flex-row gap-2.5 p-3 md:flex-col md:gap-3.25) → StylePicker
    Separator
    Field.group → BaseColorPicker, ThemePicker, ChartColorPicker
    Separator
    Field.group → FontPicker(Heading), FontPicker(Font)
    Separator
    Field.group → IconLibraryPicker, RadiusPicker
    Separator
    Field.group → MenuColorPicker, MenuAccentPicker
  Card.footer: flex min-w-0 gap-2 px-3! md:flex-col md:**:[button,a]:w-full  → CopyPreset, OpenPreset, RandomButton
  Card.footer: ... pt-2 → "Initialize Project" Button
```

For **this** task, the two footers render the Shuffle/Reset buttons only; CopyPreset / OpenPreset / Initialize
Project are Task 8 (leave clearly-labelled `// TODO(Task 8)` placeholders that still compile).

`MainMenu` is a `Picker` whose trigger says "Menu" with a menu icon, containing: Navigate… (⌘P / Ctrl+P — for this
port it may open a simple item list or be disabled; the full command palette is out of scope), Shuffle (R),
Light/Dark (D), separator, Undo (⌘Z, disabled when `!canUndo`), Redo (⇧⌘Z, disabled when `!canRedo`), separator,
Reset (⇧R). Show the Mac vs non-Mac shortcut label based on `navigator.platform`.

Also add the desktop-only sponsor slots below the card: `div(cls := "hidden w-full flex-1 flex-col gap-12 md:flex")`
and `div(cls := "hidden flex-col gap-12 md:flex")`. Content may be a simple CTA card; no marketing copy parity
required.

- [ ] **Step 5: Compile, format, observable check**

`sbt -batch site/compile site/fastLinkJS scalafmtAll`, then on `/create/preview-02` assert and print:

1. **Every field drives the document.** For each of style, baseColor, theme, chartColor, radius, menuColor,
   menuAccent, iconLibrary, font, fontHeading: open the picker, click a non-default option, and confirm the
   matching `<html>` `data-*` attribute changed **and** `?preset=` in the URL changed. Ten before/after pairs.
2. **The theme reaches the iframe too**: after changing theme color, the iframe's
   `contentDocument.documentElement.getAttribute("data-theme-color")` matches the parent's.
3. **Locks block randomize.** Lock `style` via its LockButton (`[data-locked="true"]` appears), record
   `data-style-pack`, press `R` 10 times, confirm `data-style-pack` never changed while at least one other
   attribute did.
4. **Undo/redo.** Change theme twice, ⌘Z twice → back to the original code; ⇧⌘Z once → forward one step.
   Confirm Undo is disabled in MainMenu at the start of a fresh session.
5. **Editable-target guard.** Focus the customizer's Open Preset input if present, else any `<input>` on the page,
   type the literal characters `r`, `R`, `d`; confirm neither `data-style-pack` nor the `dark` class changed.
6. **Style-driven radius coercion.** Pick style `lyra`, confirm `data-radius === "none"`. Pick style `rhea` then
   radius `large` and confirm it does not stick on `large`.
7. **`Button` sizing regression guard.** Every `<button>` inside `[data-slot="designer"]` has
   `getBoundingClientRect().height >= 24`. A 20px-tall button means a bare `Button(...)` with no variant/size.
8. **Locks persist** across a reload.

- [ ] **Step 6: Teardown + commit**

```bash
git add modules/site/src/main/scala/shadcnscalajs/site/create/
git commit -m "Add the customizer: rich pickers, per-field locks, and the main menu"
```

---

### Task 8: Preset actions and dialogs

**Context:** The three remaining customizer footer actions plus the first-visit welcome dialog.

**Reference files to port from** (under `.../(create)/components/`): `copy-preset.svelte` (32),
`open-preset.svelte` (89), `initialize-dialog.svelte` (72), `welcome-dialog.svelte` (40).

**Files:**
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/CopyPreset.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/OpenPreset.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/InitializeDialog.scala`
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/WelcomeDialog.scala`
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/create/Customizer.scala` (replace the Task 7 placeholders)
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/create/CreateShell.scala` (mount `WelcomeDialog` inside `main`)

**Interfaces:**
- Consumes: `CreateState` (Task 4), `Preset` (Task 1), `Dialog`/`Button`/`Input`/`Field` from `modules/ui`.

- [ ] **Step 1: `CopyPreset`** — copies the literal string `` `--preset <code>` `` via
`dom.window.navigator.clipboard.writeText`. Swap the icon to a check for ~2s after a successful copy.

- [ ] **Step 2: `OpenPreset`** — a dialog with a text input accepting either a raw code (`b3F`) or a full
`--preset b3F`. Strip a leading `--preset ` prefix, then validate with `Preset.isPresetCode` **and**
`Preset.decode(...).isDefined`. On valid, `state.applyPresetCode` and close. On invalid, set `aria-invalid="true"`
**only when the input is non-empty** (spec requirement) and keep the dialog open. The label must be present and
`sr-only`. Do not name the input's `Var` `value` (Global Constraints #6) — use `codeVar`.

- [ ] **Step 3: `InitializeDialog`** — shows the init command including the current preset, e.g.
`npx shadcn-scalajs@latest init --preset <code>` (adapt the command to this project's own CLI:
`node packages/cli/dist/index.js init` is the local form; use the published-style command in the dialog text).
A "Copy Command" button copies the full command. Opened from the Customizer's second footer button
(`Button.of(_.variant("default"), ...)` with a terminal icon).

- [ ] **Step 4: `WelcomeDialog`** — first-visit dismissible dialog, persisted under localStorage key
`shadcn-create-welcome-dialog` (boolean "dismissed"). Structure from `welcome-dialog.svelte`: a logo panel with
`aspect-[2/1.2] sm:aspect-2/1`, title, two description paragraphs, a full-width "Get Started" close button. Adapt
copy to shadcn-scalajs; **copy parity beyond structure is an explicit non-goal**.

- [ ] **Step 5: Compile, format, observable check**

On `/create/preview-02`, with clipboard permission granted
(`context.overridePermissions(origin, ["clipboard-read", "clipboard-write"])`), assert and print:

1. Welcome dialog is open on a fresh profile; after clicking "Get Started" and reloading, it does **not** reopen.
2. Copy Preset writes exactly `--preset <current code from the URL>` to the clipboard.
3. Open Preset with `--preset b0` applies the defaults (`data-style-pack === "nova"`, `data-base-color === "neutral"`).
4. Open Preset with `!!!!` sets `aria-invalid="true"` on the input, keeps the dialog open, and leaves `<html>`
   attributes unchanged. Clearing the input removes `aria-invalid`.
5. Initialize Project dialog text contains the current preset code, and the copy button writes that exact string.
6. **Escape closes each of the three dialogs**, and focus returns to the element that opened it.

- [ ] **Step 6: Teardown + commit**

```bash
git add modules/site/src/main/scala/shadcnscalajs/site/create/
git commit -m "Add copy/open preset, initialize project, and welcome dialogs"
```

---

### Task 9: Preview-02 grid shell + 33 card stubs + wire the preview route

**Context:** This task creates the exact mosaic grid and **33 placeholder card files** so that Wave 2 can port the
real cards in parallel without any two workers touching the same file, and so every Wave 2 worker has a live,
browser-verifiable page from the very first minute.

**Reference file to port from:**
`/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/preview-02.svelte` (110 lines).
Port its markup exactly.

**Files:**
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/preview02/Preview02.scala`
- Create: 33 files under `modules/site/src/main/scala/shadcnscalajs/site/create/preview02/`, one per card, PascalCase:
  `AccountAccess, CardOverview, ClaimableBalance, ContributionHistory, CoverArt, DividendIncome, EmptyConnectBank,
  EmptyDistributeTrack, EmptyExploreCatalog, Faq, FrontDoor, IndexInvesting, KitchenIsland, LoadingCard,
  NewMilestone, NotificationSettings, Payments, PayoutThreshold, PowerUsage, Preferences, QrConnect,
  ReceivingMethod, RecentTransactions, ReleaseCatalog, RollerShades, SavingsProgress, SavingsTargets, SidebarNav,
  SocialLinks, StockPerformance, SyncingState, TransferFunds, UpcomingPayments` — each `.scala`.
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/create/CreateShell.scala` (replace Task 6's
  `// TODO(Task 9)` placeholder in `PreviewOnlyPage` with `Preview02()`)

- [ ] **Step 1: The 33 stubs**

Each stub is exactly this shape (substituting the name):

```scala
package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** preview-02 mosaic card. STUB — port from
  * /Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/<kebab-name>.svelte
  */
object <Name>:
  def apply(): HtmlElement =
    Card(dataAttr("card") := "<kebab-name>", Card.header(Card.title("<Name>")))
```

The `data-card="<kebab-name>"` attribute is **required** — Wave 2 and Task 30 use it to locate cards in the DOM.
Every real card port must preserve it on its root element.

- [ ] **Step 2: `Preview02.scala` — the grid**

Port the markup from `preview-02.svelte` exactly. Outer:

```
overflow-x-auto overflow-y-hidden bg-muted contain-[paint] [--gap:--spacing(4)]
3xl:[--gap:--spacing(12)] md:[--gap:--spacing(10)] dark:bg-background
style-lyra:md:[--gap:--spacing(6)] style-mira:md:[--gap:--spacing(6)]
```
then `div(cls := "flex w-full min-w-max justify-center")`, then the capture grid:

```
grid w-[2400px] grid-cols-7 items-start gap-(--gap) bg-muted p-(--gap) md:w-[3000px]
dark:bg-background style-lyra:md:w-[2600px] style-mira:md:w-[2600px] *:[div]:gap-(--gap)
```
with `dataAttr("slot") := "capture-target"`.

Six direct children (columns 3–4 are one `col-span-2` child), in this exact order:

| # | Column class | Cards, top → bottom |
|---|---|---|
| 1 | `flex flex-col p-1 [contain-intrinsic-size:380px_1200px] [content-visibility:auto]` | ContributionHistory, EmptyDistributeTrack, QrConnect, DividendIncome, IndexInvesting, SyncingState |
| 2 | same | PayoutThreshold, ClaimableBalance, Preferences, SavingsProgress, KitchenIsland |
| 3 | `col-span-2 flex flex-col p-1 [contain-intrinsic-size:760px_1200px] [content-visibility:auto]` | SavingsTargets, RecentTransactions, then a nested `div(cls := "grid grid-cols-2 items-start gap-(--gap)")` containing `div(cls := "flex flex-col gap-(--gap)")` (SidebarNav, Faq) and `div(cls := "flex flex-col gap-(--gap)")` (Payments, FrontDoor), then ReleaseCatalog |
| 4 | `flex flex-col p-1 [contain-intrinsic-size:380px_1200px] [content-visibility:auto]` | AccountAccess, CardOverview, TransferFunds, CoverArt, LoadingCard |
| 5 | same | ReceivingMethod, PowerUsage, EmptyConnectBank, UpcomingPayments, RollerShades |
| 6 | same | StockPerformance, EmptyExploreCatalog, NewMilestone, SocialLinks, NotificationSettings |

- [ ] **Step 3: Compile, format** — `sbt -batch site/compile site/fastLinkJS scalafmtAll`.

- [ ] **Step 4: Observable check**

On `/preview/preview-02` at viewport 1600×1000:

1. `document.querySelectorAll('[data-card]').length === 33`, and the 33 `data-card` values match the kebab names of
   the 33 reference `.svelte` files exactly (diff the two sorted lists and print any mismatch).
2. `document.querySelector('[data-slot="capture-target"]')` exists;
   `getComputedStyle(it).gridTemplateColumns.split(" ").length === 7`.
3. Its `getBoundingClientRect().width` is 3000 at viewport ≥ 768px, and 2400 at viewport 500px.
4. Set `<html data-style-pack="lyra">`; the width becomes 2600 at ≥768px. *(If it stays 3000, the
   `@custom-variant style-lyra` from Task 3 Step 6 is missing.)*
5. Reading the cards in **DOM order**, the sequence of `data-card` values equals the order in the table above.
   Print the actual sequence.

- [ ] **Step 5: Teardown + commit**

```bash
git add modules/site/src/main/scala/shadcnscalajs/site/create/
git commit -m "Add the preview-02 grid with 33 card stubs and wire the chrome-less preview route"
```

---

# Wave 1P — Parallel foundations

Tasks 10–17 may run **simultaneously with each other and with all of Wave 1S**. Each owns a disjoint file set.
They are all prerequisites for Wave 2's card batches.

**Shared context for tasks 10–16** (restated because each worker sees only its own task): you are extending
existing Laminar components in `/Users/elam/Personal/shadcn-scalajs/modules/ui/src/main/scala/shadcnscalajs/ui/`.
Every component here mirrors shadcn/ui's canonical `new-york-v4` source: the Tailwind class strings are copied
verbatim from the corresponding `.tsx`/`.svelte`. Read the reference implementation in
`/Users/elam/Personal/shadcn-scalajs/vendor/shadcn-source` (and
`/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/ui/<name>/`) before writing anything. Each component has
a `<Name>.registry.json` sidecar next to it — **update the sidecar whenever the public API or dependency list
changes**. Follow Global Constraints (especially #1 tag names, #2 Button variants, #3 SVG sizing, #5 `cn-` prefix).

Common per-task verification for 10–16: `sbt -batch ui/compile site/compile blocks/compile scalafmtAll`, then load
`http://localhost:4300/components/<name>` (each component already has a docs page) and assert the new parts render
with the expected computed styles. If a new part has no docs-page example, add a `liveExample()` case **and** the
matching `usageSource` case in `Main.scala`… **no — do not edit `Main.scala`**, it is owned by Wave 1S Task 6.
Instead verify the new parts by injecting them from the browser is not possible either; therefore: verify via the
existing docs page for the parts that are already demoed, and for genuinely new parts state clearly in your commit
message that browser verification is deferred to the Wave 2 card task that first consumes them (named in your task).

---

### Task 10: `Sidebar` — full composition

**Files:** Modify `modules/ui/.../Sidebar.scala`, `Sidebar.registry.json`.

**Consumer:** `SidebarNav` card (Task 28). Reference:
`/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/sidebar-nav.svelte`
(270 lines) shows exactly which parts are used; the primitive itself is
`shadcn-svelte/docs/src/lib/registry/ui/sidebar/`.

Current `Sidebar.scala` is a 5-function stub (`apply`, `header`, `content`, `footer`, `menu`, `menuItem`).

- [ ] **Step 1:** Add the parts `sidebar-nav.svelte` actually references: Provider, Root (with collapsible
  variants), Inset, Content, Group, GroupLabel, GroupContent, Menu, MenuItem, MenuButton (with `isActive` /
  `data-active`), MenuBadge, MenuSub, MenuSubItem, MenuSubButton, Separator, Rail, Trigger. Copy class strings
  verbatim from the reference.
- [ ] **Step 2:** Add the class `cn-menu-target` to the sidebar root. The create page's Menu Color control toggles
  `dark` / `cn-menu-translucent` on `.cn-menu-target` elements (see `ThemeConfig.applyToDocument`), and this is the
  hook it looks for. Use `data-active="true"` for the active menu item — **not** a `bg-accent` class that a
  `[class*="bg-accent"]` selector could false-match (Global Constraints).
- [ ] **Step 3:** Update `Sidebar.registry.json`. Compile, format, verify on `/components/sidebar`, commit.

---

### Task 11: `Item` — variants and remaining parts

**Files:** Modify `modules/ui/.../Item.scala`, `Item.registry.json`.

**Consumers:** AccountAccess, ClaimableBalance, ContributionHistory, CoverArt, DividendIncome, KitchenIsland,
Payments, ReleaseCatalog, SavingsTargets, TransferFunds, UpcomingPayments.

Current file has `apply/media/content/title/description/actions`. Reference:
`shadcn-svelte/docs/src/lib/registry/ui/item/`.

- [ ] **Step 1:** Add `variant` (`default` / `outline` / `muted`) and `size` (`default` / `sm` / `xs`) as
  `Item.of(_.variant(...), _.size(...))`-style options **or** as explicit parameters — match whichever convention
  the neighbouring components in this repo already use (`Button.scala` is the reference for the `.of` builder
  pattern; grep it first).
- [ ] **Step 2:** Add `group`, `header`, `footer`, `separator`, and an `ItemMedia` `variant="icon"` form.
- [ ] **Step 3:** Update sidecar. Compile, format, verify on `/components/item`, commit.

---

### Task 12: `Field` — FieldSet, FieldLegend, FieldSeparator, FieldContent, FieldTitle, orientation

**Files:** Modify `modules/ui/.../Field.scala`, `Field.registry.json`.

**Consumers:** AccountAccess, NewMilestone, NotificationSettings, PayoutThreshold, Preferences, ReceivingMethod,
SavingsTargets, SocialLinks, StockPerformance, TransferFunds — and the create-page Customizer (Wave 1S Task 7),
which uses `Field.group` with `flex-row` on mobile and `md:flex-col` on desktop.

`Field.scala`'s own doc comment already says FieldSet/FieldLegend/FieldSeparator/FieldContent/FieldTitle are not
ported. Port them from `field.tsx` in `vendor/shadcn-source`, plus `orientation="horizontal"` /
`"responsive"` variants on `Field`.

- [ ] Update the sidecar and the file's doc comment (it must stop claiming the parts are missing). Compile, format,
  verify on `/components/field`, commit.

---

### Task 13: `InputGroup` and `Empty`

**Files:** Modify `modules/ui/.../InputGroup.scala`, `InputGroup.registry.json`, `Empty.scala`,
`Empty.registry.json`.

**Consumers:** InputGroup → ReleaseCatalog, SavingsTargets, SocialLinks, TransferFunds. Empty → EmptyConnectBank,
EmptyDistributeTrack, EmptyExploreCatalog, SyncingState.

- [ ] **Step 1:** `InputGroup` currently has only `apply` + `addon`. Add `input`, `text`, `button`, and addon
  alignment (`inline-start` / `inline-end` / `block-start` / `block-end`) from the reference `input-group.tsx`.
- [ ] **Step 2:** `Empty` currently has `apply/header/title/description`. Add `media` (with a `variant="icon"`
  form) and `content`. Any SVG passed to `Empty.media` must carry its own size class (Global Constraints #3).
- [ ] **Step 3:** Update both sidecars. Compile, format, verify on `/components/input-group` and
  `/components/empty`, commit.

---

### Task 14: `Slider` and `ToggleGroup`

**Files:** Modify `modules/ui/.../Slider.scala`, `Slider.registry.json`, `ToggleGroup.scala`,
`ToggleGroup.registry.json`.

**Consumers:** Slider → KitchenIsland, PayoutThreshold, RollerShades. ToggleGroup → KitchenIsland, ReleaseCatalog,
RollerShades.

- [ ] **Step 1:** `Slider.scala` is currently a one-line alias to `Range` with no state binding. Add controlled
  forms: `Slider.single(valueVar: Var[Double], min, max, step, mods)` and
  `Slider.multiple(valuesVar: Var[List[Double]], ...)` (RollerShades and KitchenIsland need two-way binding, and
  the customizer-driven cards need the value to be readable). **Do not name the parameter `value`** — Laminar's
  `value` prop is in scope (Global Constraints #6); use `valueVar`. Copy the track/range/thumb class strings from
  the reference `slider.tsx`.
- [ ] **Step 2:** `ToggleGroup` already has `single`/`multiple`. Add `variant` (`default` / `outline`), `size`
  (`default` / `sm` / `lg`), and per-item `disabled`. Keep the existing `data-[state=on]` hook.
- [ ] **Step 3:** Update both sidecars. Compile, format, verify on `/components/slider` and
  `/components/toggle-group` (drag the slider thumb with `page.mouse` and assert the bound value changed), commit.

---

### Task 15: `Checkbox` indeterminate, `Tabs` stateful, `Select` stateful

**Files:** Modify `modules/ui/.../Checkbox.scala`, `Checkbox.registry.json`, `Tabs.scala`, `Tabs.registry.json`,
`Select.scala`, `Select.registry.json`.

**Consumers:** Checkbox → NotificationSettings (select-all with indeterminate). Tabs → Faq. Select → Preferences,
TransferFunds, SavingsTargets, PayoutThreshold.

- [ ] **Step 1:** `Checkbox` is a bare `<input type=checkbox>`. Add an indeterminate form — the DOM
  `indeterminate` property is **not** an attribute, so it must be set imperatively via `onMountCallback` /
  a `Signal[Boolean] --> ` binding on `ctx.thisNode.ref.asInstanceOf[dom.html.Input].indeterminate`.
- [ ] **Step 2:** `Tabs` has no state. Add `Tabs.stateful(selected: Var[String])(tabs: (String, String, HtmlElement)*)`
  (value, label, panel) wiring `data-state`, `aria-selected`, and panel visibility.
- [ ] **Step 3:** `Select` is a bare styled `<select>`. Add `Select.stateful(selected: Var[String], options: List[(String, String)], mods)`.
  Do not name any parameter `value`.
- [ ] **Step 4:** Update the three sidecars. Compile, format, verify on `/components/checkbox`, `/components/tabs`,
  `/components/select`, commit.

---

### Task 16: `Chart` — Laminar-owned SVG bar / area / donut helpers

**Files:** Modify `modules/ui/.../Chart.scala`, `Chart.registry.json`.

**Context:** The reference cards use LayerChart (`BarChart`, `AreaChart`, `PieChart`) inside shadcn `Chart`
containers. **This port does not add a charting dependency** — it implements Laminar-owned SVG geometry. Current
`Chart.scala` is a 10-line placeholder `div`.

**Consumers:** ContributionHistory, DividendIncome, CardOverview, PowerUsage, SavingsProgress, StockPerformance,
SidebarNav (mini charts) — i.e. Wave 2 Tasks 23 and 24. Reference cards to read for the shapes actually needed:
`.../preview-02/cards/{contribution-history,dividend-income,card-overview,power-usage,savings-progress,stock-performance}.svelte`.

- [ ] **Step 1:** Keep `Chart.apply` as the container (it is the shadcn `ChartContainer` equivalent) but drop the
  hard-coded `aspect-video` if the reference cards override it; make aspect a parameter.
- [ ] **Step 2:** Add:
  - `Chart.bar(series: List[(String, Double)], mods)` — vertical bars, baseline, optional x labels.
  - `Chart.area(series: List[(String, Double)], mods)` — smoothed area + stroke, with a gradient fill.
  - `Chart.donut(slices: List[(String, Double)], mods)` — donut/pie arcs.
  - `Chart.tooltip(...)` — a hover tooltip driven by an `onMouseMove` → nearest-point `Var`, matching the
    reference's tooltip content shape (label + value + colored swatch).
- [ ] **Step 3:** All colors come from the existing `--chart-1`..`--chart-5` CSS tokens (which the Chart Color
  picker drives) — reference them as `var(--chart-N)`, never a hard-coded hex. Build SVG with
  `com.raquo.laminar.api.L.svg.*` (the same import style `Icons.scala` uses).
- [ ] **Step 4:** Update the sidecar. Compile, format. **Observable check:** on `/components/chart`, assert an
  `<svg>` renders with `> rect`/`> path` children, that changing `<html data-chart-color>` changes the computed
  `fill` of the first bar, and that hovering a bar shows the tooltip. Commit.

---

### Task 17: QR facade and the `qrcode` dependency

**Files:**
- Modify: `modules/site/package.json` (add the `qrcode` dependency)
- Create: `modules/site/src/main/scala/shadcnscalajs/site/create/preview02/QrCode.scala`

**Consumer:** the `QrConnect` card (Task 29). Reference:
`/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/qr-connect.svelte`.

- [ ] **Step 1:** `npm install qrcode --save` from `modules/site` (this adds it to `dependencies`, not
  `devDependencies` — it ships in the bundle). Commit the `package-lock.json` change.
- [ ] **Step 2:** Write a thin Scala.js facade:

```scala
@js.native @JSImport("qrcode", JSImport.Default)
private object QRCodeJS extends js.Object:
  def toDataURL(text: String, opts: js.Object): js.Promise[String] = js.native
```

exposing `QrCode.toDataUrl(url: String, width: Int = 160, margin: Int = 1): js.Promise[String]`. Note Global
Constraints and `AGENTS.md` item 6: `.then[String](...)` needs the **explicit type parameter** on the first
`.then` or Scala's inference fails to widen `js.Promise[String]`.

- [ ] **Step 3:** Compile, format. **Observable check:** temporarily render the facade's output into
  `/preview/preview-02`'s stub `QrConnect` card is not allowed (Task 29 owns that file). Instead verify at the JS
  level: `cd modules/site && node -e 'import("qrcode").then(m => m.default.toDataURL("https://x", {width:160,margin:1}).then(d => console.log(d.slice(0,30), d.length)))'`
  and confirm it prints a `data:image/png;base64,` prefix. State in your commit message that Scala-side browser
  verification is deferred to Task 29. Commit.

---

# Wave 2 — Parallel fan-out

**Do not start any Wave 2 task until every Wave 1 task has landed.** All twelve Wave 2 tasks may then run
simultaneously; no two of them share a file.

---

## Known API gaps carried over from Wave 1 — read before writing any card

Wave 1 workers reported these deliberately-unported behaviours. They are recorded here so that twelve card workers
do not each improvise a different workaround. **If your card needs one of these, use the stated workaround; do not
edit the `modules/ui` file to add the missing feature** — another card task may be reading that same file, and
`modules/ui` is what the CLI copies into consumer projects.

| Gap | Where it bites | Workaround |
|---|---|---|
| `Slider.apply` is still a legacy alias for the native `Range` input. | Any card with a real slider. | Use `Slider.single(valueVar, …)` or `Slider.multiple(valuesVar, …)`. Never `Slider(...)`. |
| `Slider` has no `disabled` parameter. | KitchenIsland, RollerShades. | Pass it through `mods`, e.g. a reactive `dataAttr("disabled")`, and rely on the `data-disabled:opacity-50` rule. |
| `ToggleGroup` has no `spacing` parameter (upstream maps it to a `--gap` var). | ReleaseCatalog, RollerShades. | Pass `cls := "gap-1"` on the group. |
| `Tabs.stateful` does not expose per-trigger or per-list modifiers, so you cannot set the reference's `w-full` list / `flex-1` triggers through it. | Faq (Task 28). | Compose `Tabs.list` / `Tabs.trigger` / `Tabs.content` manually with your own `Var`, exactly as the pre-existing manual call sites do. |
| `Checkbox` sets the `indeterminate` DOM property correctly, but no `:indeterminate` CSS exists in this repo, so a partially-checked box may render like a checked one. | NotificationSettings (Task 27). | Wire the property anyway via `Checkbox(checkedVar, indeterminateSignal, …)`. Report the visual gap in your final response; Task 30 decides whether it needs CSS. |
| `Field.label` was not upgraded to upstream's nested-field / checked-state classes. | ReceivingMethod, Preferences. | Use it as-is. If a class is visibly missing, add it via `cls :=` at the call site and report it. |
| `InputGroup.textarea` was not ported. | Any card wanting a grouped textarea. | Use `InputGroup.input`; report if a card genuinely needs a textarea. |
| `Sidebar` has no mobile `Sheet` path and no context — `openVar` must be passed explicitly to `provider`/`root`/`trigger`/`rail`, and `Sidebar.menuButton(isActive = true)` emits `data-active="true"` (not an accent class). | SidebarNav (Task 28). | Pass `openVar` explicitly and use `Collapsible.None`, matching `sidebar-nav.svelte`, which is desktop-only inside the mosaic anyway. |
| `Item.title` carries a `cn-font-heading` hook that this repo's generated CSS does not define. | Any Item-based card. | Harmless; leave it. Do not strip it — it is upstream style-pack parity. |
| **Defaulted parameters sit *before* the varargs** on several Wave 1 components — `ToggleGroup.single/multiple(selected, variant, size, items*)`, `InputGroup.addon(align, mods*)`, `Chart.apply(aspectRatio, mods*)`. Scala cannot skip a defaulted parameter positionally, so `ToggleGroup.single(v, Item(...))` fails with a baffling `Found: ToggleGroup.Item / Required: Toggle.Variant`. | Every card using these. | Pass the variant/size/align explicitly (`ToggleGroup.single(v, Toggle.Variant.Default, Toggle.Size.Default, items*)`), or use named arguments. `Chart` also has a plain `Chart(mods*)` overload for the no-aspect case. |

Two conventions that follow from the above: prefer `data-*` hooks over class-substring matching when you need a
state selector, and if a primitive genuinely cannot express what your card needs, **report it in your final
response rather than editing the primitive**.

---

## Tasks 18–21: Icon library data (parallel, one file each)

**Shared context** (each task restates it): Wave 1S Task 5 created
`modules/ui/src/main/scala/shadcnscalajs/ui/icons/<Library>IconData.scala` as an object implementing
`IconDataSource` with an **empty** `paths: Map[String, Seq[String]]`. Your job is to fill it with real path data for
every concept in `Icons.Concepts` (~55 names, defined in `modules/ui/.../Icons.scala` — read that list first; it is
the authoritative list, not this plan).

Rules for all four tasks:

- Path data must be **real** for that library, not Lucide paths copied over. Getting the glyph shapes right *is*
  the task; a library whose icons are visually identical to Lucide's is a failed port.
- Get the data from the library's own published SVGs. The mapping from this repo's kebab concept name to each
  library's icon name follows `/Users/elam/Personal/shadcn-svelte/packages/cli/src/icons/libraries.ts` and the
  `IconPlaceholder` props across
  `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/*.svelte`
  (each `<IconPlaceholder lucide="…" tabler="…" hugeicons="…" phosphor="…" remixicon="…" />` gives you the
  five names for one concept — this is your primary source of truth for the name mapping).
- Respect the library's coordinate system and paint model: **Tabler** `0 0 24 24` stroke; **HugeIcons** `0 0 24 24`
  stroke; **Phosphor** `0 0 256 256` fill; **Remix Icon** `0 0 24 24` fill. Set `viewBox` and `strokeBased`
  accordingly on your object.
- If you genuinely cannot source a glyph, **omit the key** — `Icons.icon` falls back to Lucide. Do not invent path
  data. List every omitted concept in your commit message.
- Do not modify `Icons.scala` or any other file.

Per-task verification (same for all four): `sbt -batch ui/compile site/compile scalafmtAll`, then on
`http://localhost:4300/preview/preview-02` set `document.documentElement.setAttribute("data-icon-library", "<your library>")`
and assert with puppeteer-core that (a) every `svg` under `[data-slot="capture-target"]` still has a
`getBoundingClientRect().width` between 8 and 40 (no un-sized icon blowups), (b) the rendered `d` attribute of at
least 40 distinct icons differs from what the same page renders with `data-icon-library="lucide"` (i.e. your data
is actually being used and is actually different), and (c) `document.querySelectorAll("svg > div").length === 0`
(no wrapper elements). Print the count of concepts you filled and the list you omitted. Then teardown and commit.

- [ ] **Task 18** — `modules/ui/src/main/scala/shadcnscalajs/ui/icons/TablerIconData.scala`
- [ ] **Task 19** — `modules/ui/src/main/scala/shadcnscalajs/ui/icons/HugeiconsIconData.scala`
- [ ] **Task 20** — `modules/ui/src/main/scala/shadcnscalajs/ui/icons/PhosphorIconData.scala`
- [ ] **Task 21** — `modules/ui/src/main/scala/shadcnscalajs/ui/icons/RemixIconData.scala`

---

## Tasks 22–29: Preview-02 card batches (parallel, disjoint files)

**Shared context** (each task must restate it; a worker reads only its own task):

You are porting cards of the preview-02 mosaic from Svelte to Scala.js / Laminar, in
`/Users/elam/Personal/shadcn-scalajs`, branch `fix/alert-new-york-v4-fidelity`.

- Each card already exists as a **stub file** at
  `modules/site/src/main/scala/shadcnscalajs/site/create/preview02/<Name>.scala` rendering a placeholder `Card`.
  **Overwrite the stub with the real port.** Do not create new files, do not rename, do not touch `Preview02.scala`.
- Port from `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/examples/create/preview-02/cards/<kebab>.svelte`.
  **Copy, datasets, class strings, and per-card responsive visibility must match the reference exactly.** Do not
  invent layout, do not paraphrase copy, do not simplify the data.
- **Keep `dataAttr("card") := "<kebab-name>"` on the root element.** Task 30 and the Wave 2 verifiers locate cards
  by it.
- Use `modules/ui` primitives (`Card`, `Button`, `Badge`, `Item`, `Field`, `Empty`, `InputGroup`, `Slider`,
  `ToggleGroup`, `Checkbox`, `Tabs`, `Select`, `Separator`, `Progress`, `Skeleton`, `Spinner`, `Table`,
  `Breadcrumb`, `RadioGroup`, `Textarea`, `Calendar`, `Accordion`, `DropdownMenu`, `Popover`, `Command`, `Chart`).
  If a primitive part you need is missing, **do not add it to `modules/ui`** — another task owns those files. Build
  it inline in your card file and note the gap in your commit message.
- Icons come from `shadcnscalajs.ui.Icons`. Each `<IconPlaceholder lucide="XIcon" …/>` in the reference maps to
  `Icons.x(...)` (kebab-cased concept). If a concept is missing from `Icons.Concepts`, use the closest existing one
  and note it in your commit message — do not edit `Icons.scala`.
- Global Constraints apply, in particular: **`Button.of(_.variant(...), _.size(...))` always** (a bare `Button(...)`
  renders 20px tall and transparent); Laminar `Tag`-suffixed tag names; explicit `size-4`-class on every SVG;
  `.toInt` on `js.Date` getters; never name a `Var` `value`.

**Per-batch verification recipe** (identical for tasks 22–29 — substitute your own card list):

1. `cd /Users/elam/Personal/shadcn-scalajs && export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch site/compile site/fastLinkJS scalafmtAll`
2. Start the dev server, load `http://localhost:4300/preview/preview-02` at viewport 1600×1000.
3. For each of your cards, with puppeteer-core:
   - `const el = document.querySelector('[data-card="<kebab>"]')` — must be non-null.
   - `el.getBoundingClientRect().height > 80` — the stub was ~60px; a real card is taller. Print the height.
   - `el.innerText` contains the card's distinctive reference copy (name three exact strings from the `.svelte`
     file in your task and assert all three). Print the full `innerText`.
   - No `<button>` inside `el` has `getBoundingClientRect().height < 24` (the bare-`Button` regression guard).
   - Every `svg` inside `el` has width between 8 and 40 (the un-sized-SVG regression guard).
   - `el.querySelectorAll("svg > div").length === 0`.
4. Run the **interaction check** named in your task (below), and print its before/after values.
5. **Screenshot each of your cards** (`el.screenshot({ path: "/tmp/<kebab>.png" })`) and view them with the Read
   tool. Compare against the reference `.svelte` source structurally: same sections, same order, same copy.
6. Teardown: delete the temp script, `lsof -ti:4300 | xargs -r kill`.
7. Commit only your own files:
   `git add modules/site/src/main/scala/shadcnscalajs/site/create/preview02/{<YourCards>}.scala && git commit -m "Port preview-02 cards: <list>"`

---

### Task 22: Cards — empty states and loading (5 cards)

**Reference files:** `cards/empty-connect-bank.svelte` (31), `cards/empty-distribute-track.svelte` (31),
`cards/empty-explore-catalog.svelte` (31), `cards/syncing-state.svelte` (25), `cards/loading-card.svelte` (23).

**Files:** `EmptyConnectBank.scala`, `EmptyDistributeTrack.scala`, `EmptyExploreCatalog.scala`,
`SyncingState.scala`, `LoadingCard.scala`.

**Primitives used:** `Card`, `Empty` (incl. `Empty.media` icon variant — Task 13), `Button`, `Spinner`, `Skeleton`.

**Interaction check:** none of these are interactive. Instead assert `LoadingCard` renders ≥ 3
`.animate-pulse`-classed skeleton elements and `SyncingState` renders a `Spinner` with a non-`none` computed
`animation-name`.

---

### Task 23: Cards — bar and donut charts (4 cards)

**Reference files:** `cards/contribution-history.svelte` (88), `cards/dividend-income.svelte` (119),
`cards/card-overview.svelte` (82), `cards/power-usage.svelte` (74).

**Files:** `ContributionHistory.scala`, `DividendIncome.scala`, `CardOverview.scala`, `PowerUsage.scala`.

**Primitives used:** `Card`, `Chart` (Task 16 — bar/donut + tooltip), `Item`, `Badge`, `Button`, `Progress`,
`Separator`.

The reference uses LayerChart + `d3-scale`. **This port has no charting dependency** — use `Chart.bar` /
`Chart.donut` from `modules/ui/Chart.scala` and the `--chart-1`..`--chart-5` tokens. Keep the reference's exact
datasets (copy the numbers verbatim), axis labels, and legends.

**DividendIncome specifically:** the reference hides its chart and label below `md` (`hidden md:block`). Reproduce
that responsive visibility exactly.

**Interaction check:** for each card, assert an `<svg>` exists inside with ≥ 4 `rect` or `path` children; then set
`document.documentElement.setAttribute("data-chart-color", "violet")` and confirm the computed `fill` of the first
bar/arc changed. Hover a bar and confirm a tooltip element appears.

---

### Task 24: Cards — area chart, progress chart, plain stat (3 cards)

**Reference files:** `cards/stock-performance.svelte` (149), `cards/savings-progress.svelte` (60),
`cards/index-investing.svelte` (18).

**Files:** `StockPerformance.scala`, `SavingsProgress.scala`, `IndexInvesting.scala`.

**Primitives used:** `Card`, `Chart` (Task 16 — `area` + `tooltip`), `Command`, `Popover`, `Field`, `Button`,
`Separator`.

`StockPerformance` is the most interactive card in the mosaic: a Command/combobox ticker search that swaps the
plotted series, plus an area chart with a hover tooltip. Port the ticker list and series data verbatim.

**Interaction check:** open the ticker combobox, type to filter, select a different ticker, and assert the chart's
first `path`'s `d` attribute changed. Then hover the chart and assert a tooltip appears containing a value string.

---

### Task 25: Cards — Item/Badge compositions (5 cards)

**Reference files:** `cards/claimable-balance.svelte` (42), `cards/account-access.svelte` (77),
`cards/front-door.svelte` (32), `cards/new-milestone.svelte` (37), `cards/cover-art.svelte` (53).

**Files:** `ClaimableBalance.scala`, `AccountAccess.scala`, `FrontDoor.scala`, `NewMilestone.scala`,
`CoverArt.scala`.

**Primitives used:** `Card`, `Item` (Task 11), `Badge`, `Separator`, `Field` (Task 12), `Button`, `Input`, `Label`.

`CoverArt` includes a file-picker / upload affordance — port the reference's behavior (a hidden `<input
type="file">` triggered by a labelled button), not just its appearance.

**Interaction check:** click `CoverArt`'s upload affordance and assert an `input[type=file]` exists and is the
click target (`document.querySelector('[data-card="cover-art"] input[type=file]')` non-null). For the other four,
assert each renders the exact number of `[data-slot="item"]`-equivalent rows the reference does.

---

### Task 26: Cards — Field / Select / Slider forms (4 cards)

**Reference files:** `cards/payout-threshold.svelte` (89), `cards/preferences.svelte` (76),
`cards/receiving-method.svelte` (66), `cards/transfer-funds.svelte` (105).

**Files:** `PayoutThreshold.scala`, `Preferences.scala`, `ReceivingMethod.scala`, `TransferFunds.scala`.

**Primitives used:** `Card`, `Field` (Task 12), `Select` (Task 15 stateful), `Slider` (Task 14 controlled),
`Switch`, `Textarea`, `RadioGroup`, `InputGroup` (Task 13), `Item` (Task 11), `Separator`, `Button`, `Input`.

**Interaction check:** in `PayoutThreshold`, drag the slider with `page.mouse` and assert the displayed threshold
number changed; change its `<select>` and assert the displayed unit changed. In `Preferences`, `TransferFunds`,
change one select each and assert the displayed value updated. In `ReceivingMethod`, click a radio and assert
`checked` moved.

---

### Task 27: Cards — stateful interactive controls (4 cards)

**Reference files:** `cards/kitchen-island.svelte` (141), `cards/roller-shades.svelte` (58),
`cards/release-catalog.svelte` (97), `cards/notification-settings.svelte` (86).

**Files:** `KitchenIsland.scala`, `RollerShades.scala`, `ReleaseCatalog.scala`, `NotificationSettings.scala`.

**Primitives used:** `Card`, `Item` (Task 11), `ToggleGroup` + `Slider` (Task 14), `Switch`, `Checkbox` (Task 15
indeterminate), `InputGroup` (Task 13), `Badge`, `Field` (Task 12).

`KitchenIsland`'s scene ToggleGroup needs an `sr-only` label ("Scenes" or equivalent) — an explicit spec
accessibility requirement.

**Interaction check** (all four are load-bearing spec requirements):
- `KitchenIsland`: toggle the Switch → controls enable/disable; click each scene in the ToggleGroup → the slider
  values change; drag a slider → its readout changes.
- `RollerShades`: drag the slider → the rendered shade height style changes; click open/half/closed → the slider
  value snaps to each preset.
- `ReleaseCatalog`: click each ToggleGroup filter → the number of visible catalog rows changes.
- `NotificationSettings`: check one row → the master checkbox's `indeterminate` property becomes `true`; check all
  rows → `indeterminate` false and `checked` true; click the master → all rows toggle together.

---

### Task 28: Cards — tables, menus, navigation, tabs (4 cards)

**Reference files:** `cards/recent-transactions.svelte` (263), `cards/payments.svelte` (162),
`cards/sidebar-nav.svelte` (270), `cards/faq.svelte` (97).

**Files:** `RecentTransactions.scala`, `Payments.scala`, `SidebarNav.scala`, `Faq.scala`.

**Primitives used:** `Card`, `Table`, `DropdownMenu`, `Breadcrumb`, `Item` (Task 11), `Sidebar` (Task 10),
`Tabs` (Task 15 stateful), `Accordion`, `Button`.

These are the four largest cards. Port the full datasets — `recent-transactions.svelte` has a long transaction
table; do not truncate it.

`SidebarNav` is the card the Menu Color / Menu Accent customizer controls act on: its sidebar root must carry the
`cn-menu-target` class that Task 10 added to `Sidebar`, and its active item must use `data-active="true"` (not a
`bg-accent` class), per Global Constraints.

**Interaction check:**
- `Faq`: click each of the three tabs (General / Billing / Goals) → panel content changes; click an accordion
  summary → `details[open]` toggles and the chevron rotates.
- `RecentTransactions`: open a row's dropdown menu → the menu appears; click outside → it closes (this exercises the
  `composedPath` path).
- `Payments`: open its dropdowns and click a calendar date; assert the selection changed.
- `SidebarNav`: set `<html data-menu-color="inverted">` and assert the sidebar root gained the `dark` class; set
  `inverted-translucent` and assert it also gained `cn-menu-translucent`; set `default` and assert both are gone.

---

### Task 29: Cards — QR, targets, calendar, links (4 cards)

**Reference files:** `cards/qr-connect.svelte` (37), `cards/savings-targets.svelte` (103),
`cards/upcoming-payments.svelte` (48), `cards/social-links.svelte` (81).

**Files:** `QrConnect.scala`, `SavingsTargets.scala`, `UpcomingPayments.scala`, `SocialLinks.scala`.

**Primitives used:** `Card`, `Button`, `Field` (Task 12), `InputGroup` (Task 13), `Item` (Task 11),
`NativeSelect`, `Progress`, `Badge`, `Calendar`, and `QrCode` (Task 17's facade).

- `QrConnect`: render a 160×160 QR image from `QrCode.toDataUrl(connectUrl, 160, 1)`, with a pulse placeholder
  until the promise resolves. Same connect URL and copy as the reference. **Alt text**: the reference uses an empty
  alt because adjacent text describes the purpose — if your port does not include that adjacent text verbatim, use
  meaningful alt text describing "scan to connect" (spec accessibility requirement).
- `UpcomingPayments`: the reference uses `@internationalized/date`. **This repo has no java.time and no date
  library** — bind through the existing `js.Date`-based `Calendar` API in `modules/ui/Calendar.scala`. Remember
  `js.Date` getters return `Double` and the constructor wants `Int`: `.toInt` both (Global Constraints #7).

**Interaction check:**
- `QrConnect`: assert the `<img>`'s `src` starts with `data:image/png;base64,` and its rendered size is 160×160.
- `UpcomingPayments`: click a calendar day and assert the selected-day element changed.
- `SavingsTargets`: change its native select and assert the displayed target updated; assert each `Progress` bar's
  computed width matches its reference percentage.
- `SocialLinks`: assert each `InputGroup` renders its addon prefix text from the reference.

---

# Wave 3 — Integration

Sequential: 30 → 31 → 32.

---

### Task 30: Mosaic fidelity audit

**Context:** Wave 2 ported 33 cards independently. This task is the only place the mosaic is judged **as a whole**.

**Files:**
- Modify (only if the audit finds a real ordering/dimension defect): `modules/site/.../create/preview02/Preview02.scala`
- Modify (only to fix defects the audit finds): any card file under `create/preview02/`

- [ ] **Step 1: Order and count.** On `/preview/preview-02`, read `[data-card]` values in DOM order and diff
  against the column table in Task 9 Step 2. Print both lists. Count must be 33.
- [ ] **Step 2: Dimensions.** Assert `[data-slot="capture-target"]` is 3000px wide at ≥768px, 2400px at 500px, and
  2600px with `data-style-pack="lyra"` and with `data-style-pack="mira"`. Assert the resolved `--gap` is
  `--spacing(4)` at 500px, `--spacing(10)` at 1024px, `--spacing(6)` under lyra/mira at ≥768px, and
  `--spacing(12)` at the `3xl` breakpoint.
- [ ] **Step 3: Full-mosaic screenshots.** Screenshot `[data-slot="capture-target"]` at 3000px for:
  default Nova/Neutral light; the same in dark; `style-pack=lyra`; `style-pack=mira`; a non-default chart color; a
  non-default font; `radius=none` and `radius=large`; each of the four menu colors; and each of the five icon
  libraries. View each with the Read tool.
- [ ] **Step 4: Compare against the live reference.** Where a shadcn-svelte dev server can be started
  (`cd /Users/elam/Personal/shadcn-svelte && pnpm --filter docs dev`), capture the same
  `[data-slot="capture-target"]` screenshots and compare side by side. If the reference cannot be built, compare
  structurally against the `.svelte` sources instead and **say so explicitly** in the audit notes.
- [ ] **Step 5: Write down residual deltas.** Record a short "Known residual deltas vs the reference" note.
  The charts are the expected source of these (hand-rolled SVG vs LayerChart);
  every delta must be listed and justified as intentional, per the spec's verification criteria.
- [ ] **Step 6:** Fix only genuine ordering/dimension/copy defects found above. Compile, format, commit.

---

### Task 31: End-to-end create-page verification

**Files:** none expected. Fix only defects this task surfaces, in whichever file owns them.

Run the full functional checklist from the spec's "Verification criteria → Functional" section and print evidence
for each:

- [ ] Every customizer field updates **both** the parent document and the iframe: style, base, theme, chart,
  heading font, body font, radius, menu color, menu accent, icon library. Ten before/after pairs, each showing the
  iframe's `contentDocument.documentElement` attribute matching the parent's.
- [ ] `?preset=` updates on every change, via `replaceState` (assert `history.length` did not grow across 10
  changes) and with no scroll jump (assert `scrollY` unchanged).
- [ ] Copy Preset → Open Preset round-trip restores an identical `<html>` attribute set.
- [ ] Invalid preset codes are rejected with `aria-invalid` and change nothing.
- [ ] Locks persist across reload and block randomize for every one of the ten lockable fields (loop all ten).
- [ ] Undo / redo / Reset restore expected presets; menu items disable correctly at the ends of the stack.
- [ ] R / ⇧R / D / ⌘Z / ⇧⌘Z all no-op while focus is in an `input`, `textarea`, `select`, and a
  `contentEditable` element (four separate checks each).
- [ ] Initialize Project dialog shows the current `--preset` and copies the exact command.
- [ ] `/create?preset=X` → `/create/preview-02?preset=X`; `/preview/preview-02` is chrome-less; maximize and
  minimize both preserve `search`.
- [ ] Switcher shows 01 active and 02 non-navigating.
- [ ] All twelve card interactions from the spec's "Card interactions (must work)" table still pass after
  integration (re-run the Wave 2 interaction checks in one script).
- [ ] Dark mode: toggle with D and confirm the `dark` class is **removed** cleanly when going back to light (the
  spec calls out a stale-`dark`-removal path explicitly) and that base/theme controls still work in dark mode.
- [ ] Mobile: at 390×844, the customizer becomes a horizontally scrolling control strip
  (`Card.content` has `overflow-x: auto`) and `designer` is `flex-direction: column`.
- [ ] Commit any fixes.

---

### Task 32: Repo-level verification and handoff

- [ ] **Step 1:** `export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin" && sbt -batch core/compile ui/compile webcomponents/compile blocks/compile site/compile site/fastLinkJS scalafmtCheckAll` — all green.
- [ ] **Step 2:** `cd modules/site && node scripts/build-registry.mjs`. Confirm it succeeds and that the `icons`
  registry item now lists the five `icons/*.scala` files. Confirm the updated sidecars from Tasks 10–16 are
  reflected.
- [ ] **Step 3: dashboard-01 non-regression.** `git diff --stat main -- modules/blocks/src/main/scala/shadcnscalajs/blocks/dashboard01/`
  must be **empty**, and `modules/site/public/registry/dashboard-01.json` must be unchanged except for anything
  produced by Step 2's regeneration of unrelated fields. Load `/blocks/dashboard-01` and
  `/blocks/dashboard-01/preview` and confirm both still render.
- [ ] **Step 4:** `./scripts/test` exits 0.
- [ ] **Step 5:** Commit any remaining changes (registry regeneration).

---

## Appendix: card → primitive dependency map

Extracted from the `import` statements of the 33 reference `.svelte` files. Use it to sanity-check that a Wave 1P
primitive really is needed before a card batch starts.

| Primitive | Cards that use it |
|---|---|
| Card | all 33 |
| Item | account-access, claimable-balance, contribution-history, cover-art, dividend-income, kitchen-island, payments, release-catalog, savings-targets, transfer-funds, upcoming-payments |
| Field | account-access, new-milestone, notification-settings, payout-threshold, preferences, receiving-method, savings-targets, social-links, stock-performance, transfer-funds |
| Button | 20 cards |
| Chart (+ LayerChart in the reference) | card-overview, contribution-history, dividend-income, power-usage, savings-progress, stock-performance |
| Empty | empty-connect-bank, empty-distribute-track, empty-explore-catalog, syncing-state |
| InputGroup | release-catalog, savings-targets, social-links, transfer-funds |
| Slider | kitchen-island, payout-threshold, roller-shades |
| ToggleGroup | kitchen-island, release-catalog, roller-shades |
| Select | payout-threshold, preferences, transfer-funds |
| NativeSelect | savings-targets |
| Checkbox | notification-settings |
| Tabs / Accordion | faq |
| Sidebar | sidebar-nav |
| DropdownMenu | payments, recent-transactions |
| Table | recent-transactions |
| Breadcrumb | payments |
| Calendar | upcoming-payments |
| Command / Popover | stock-performance |
| Switch | kitchen-island, preferences |
| Progress | power-usage, savings-targets |
| Separator | claimable-balance, power-usage, savings-progress, stock-performance, transfer-funds |
| Skeleton / Spinner | loading-card / syncing-state |
| Badge | card-overview, claimable-balance, contribution-history, front-door, release-catalog, upcoming-payments |
| RadioGroup | receiving-method |
| Textarea | payout-threshold |
| Input / Label | account-access, cover-art, new-milestone, receiving-method |
| qrcode | qr-connect |
