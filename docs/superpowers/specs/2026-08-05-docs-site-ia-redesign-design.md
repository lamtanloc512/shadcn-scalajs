# Docs Site IA/Layout Redesign — Design Spec

Date: 2026-08-05

## Context

Part of the same larger initiative as
`docs/superpowers/specs/2026-08-05-floating-portal-primitive-design.md` (porting
shadcn/ui to shadcn-scalajs more faithfully, matching the real shadcn/ui site). That
spec's sub-project (floating/portal primitive + Popover retrofit) is **paused** —
this spec jumps ahead to fix the site's navigation/layout gap first, since it's the
more visible, more urgent problem: the live site (`/components`, and `/`) still looks
like basecoat's own demo site, not shadcn-scalajs.

### Confirmed current state (read directly from `modules/site/src/main/scala/shadcnscalajs/site/Main.scala`)

- **`app()`** (`/`, the landing page): header literally reads `"Basecoat"`
  (`aria.label := "Basecoat home"`, visible text `"Basecoat"`), with GitHub/Discord
  links pointing at `github.com/hunvreus/basecoat` / `basecoatui.com/chat` — basecoat's
  own real links, not this project's.
- **`componentsGalleryPage()`** (`/components`): same `"Basecoat"` branding bug. Its
  body is a hand-curated grid of 9 grouped preview cards (Button, Badge, Form controls,
  Feedback, Navigation, Disclosure, Overlay primitives, Data display, Layout) that
  demo a handful of components together — it does **not** link to any of the 60
  individual component doc pages. There is no way to reach `/components/select` or
  `/components/tooltip` from this page at all today.
- **`componentDocsPage()`** (`/components/:name`): already correctly branded
  ("shadcn-scalajs"), and already has a real sidebar built from `componentNavList`
  (a flat, already-alphabetical `List[String]` of all 60 component names at
  `Main.scala:153`, already used correctly here via `componentNavList.map(navLink)`).
  Has About/Installation/Usage sections and even an inert `button(... "Copy page")`
  stub (decorative, no handler — future sub-project's territory, not this one). Gap:
  the live example renders directly, with source code only appearing later in a
  separate "Usage" section — real shadcn/ui shows Preview and Code as two tabs of the
  *same* top block.
- Per-component descriptions only exist today for ~5 components, inline in a `match`
  inside `componentDocsPage` (line ~1276) — everything else falls through to a
  generic `"The ${componentTitle.toLowerCase} primitive for shadcn-scalajs."` string.

## Goals

1. Fix the "Basecoat" branding and wrong GitHub/Discord links, on `app()` and
   `componentsGalleryPage()` (`componentDocsPage()` already correct — verify its
   GitHub link too, fix if also wrong).
2. Replace `componentsGalleryPage()`'s curated demo grid with a real directory:
   every one of the 60 entries in `componentNavList`, each linking to
   `/components/<slug>`, each with a one-line description.
3. Hoist per-component descriptions out of `componentDocsPage`'s local `match` into
   one shared function/map so the new index page and the doc page can't drift apart.
4. Add a real Preview/Code tabbed block to `componentDocsPage()`, replacing the
   direct live-example render, reusing this project's own `Tabs` component.
5. Split `Main.scala` (2,175 lines covering all three pages plus ~40 icon defs) into
   focused files, since this work touches all three page functions anyway.

## Non-goals (explicitly deferred)

- The landing page's dashboard-mockup **content/structure** stays as-is — only its
  branding/links change. Not being replaced with a shadcn/ui-style homepage.
- Copy-to-clipboard functionality (the inert "Copy page" button, and per-code-block
  copy buttons) — separate, later sub-project.
- Markdown/`llms.txt` AI-agent export — separate, later sub-project.
- The style-pack `<select>` and its 8 named packs (lyra/vega/...) in the header —
  untouched here; that's the basecoat-CSS-removal sub-project's territory.
- Per-variant "Examples" sub-sections (e.g. Button's Default/Secondary/Destructive/
  Icon/Loading variants each with their own Preview/Code) — real shadcn/ui has these,
  but adding them is new content work per component, out of scope for this IA pass.
- The floating/portal primitive + Popover retrofit (previous spec) — resume later.

## Design

### 1. Branding fix

In `app()` and `componentsGalleryPage()`:
- `aria.label := "Basecoat home"` → `aria.label := "shadcn-scalajs home"`.
- Visible header text `"Basecoat"` → `"shadcn-scalajs"`.
- `href := "https://github.com/hunvreus/basecoat"` → this project's actual GitHub
  URL (confirm via `git remote get-url origin` at implementation time).
- `href := "https://basecoatui.com/chat"` (Discord link) → remove the link entirely
  (no Discord exists for this project yet); if a nav item needs to stay for layout-grid
  balance, prefer removing the item over pointing it at someone else's community.
- Verify `componentDocsPage()`'s GitHub link (a second `github.com/hunvreus/basecoat`
  reference was seen elsewhere in `Main.scala` during the earlier audit) and fix if
  it's also wrong.

### 2. `/components` index page rebuild

Replace the current 9-card curated grid with a directory built from
`componentNavList`: for each name, a link to `/components/<slugify(name)>` plus its
one-line description (see section 3). Reasonable layout: a responsive multi-column
list of compact link+description rows (2-3 columns depending on viewport width),
sorted in `componentNavList`'s existing order (already alphabetical). Keep the header
(title "Components", intro paragraph) as-is structurally, just correct its wording if
it references the old curated-grid framing (currently: "Every preview below is
rendered from shadcn-scalajs UI primitives, with the active style pack applied
live." — no longer accurate once this is a link directory, needs a new one-liner).

### 3. Shared per-component descriptions

Move the `componentDescription` match (currently local to `componentDocsPage`,
~5 real entries + generic fallback) to a shared `private def componentDescription(name: String): String`
at the same level as `componentNavList`, so both the new index page and
`componentDocsPage` call the same function. Expand real descriptions for the
components that currently fall through to the generic fallback where a short,
accurate one-liner is easy to write (matching real shadcn/ui's own one-line
descriptions per component where they're a natural fit); leave the generic fallback
in place for any that would need deeper research to describe accurately — this is a
copy-writing improvement, not a blocker for this sub-project's structural goal.

### 4. Preview/Code tabs on the component doc page

Replace:
```scala
div(cls := "mt-8 overflow-hidden rounded-md border bg-card", liveExample())
```
with a `Tabs` block combining `liveExample()` (already exists, per-component `match`)
and a `codeBlock("scala", usageSource)` (the existing `usageSource` match, already
kept in 1:1 correspondence with `liveExample()` per `AGENTS.md`'s existing
convention) as its two tab panels — `Tabs.list(Tabs.trigger("Preview"), Tabs.trigger("Code"))`
plus two `Tabs.content(...)` panels. This dogfoods the project's own `Tabs`
component rather than introducing a new one-off tab implementation. The separate
"Installation"/"Usage" sections lower on the page are unaffected by this change.

### 5. `Main.scala` split

Since this work touches `app()`, `componentsGalleryPage()`, and `componentDocsPage()`
(and the new shared `componentDescription`), split into focused files under
`modules/site/src/main/scala/shadcnscalajs/site/`:
- `Icons.scala` — the ~40 `iconXxx` defs and `rawIcon`/`iconSvg` helpers.
- `LandingPage.scala` — `app()` and its private helpers.
- `ComponentsIndexPage.scala` — the rebuilt `componentsGalleryPage()`.
- `ComponentDocsPage.scala` — `componentDocsPage()` plus `liveExample()`/`usageSource`/
  `codeBlock`/`tableOfContents`/`navLink`.
- `Main.scala` stays as the router/entry point plus genuinely shared bits
  (`componentNavList`, `componentDescription`, `slugify`, `btnPrimary`/`btnOutline`/
  `btnGhost`/`btnIcon`, `stylePackA`).

Exact file boundaries can shift slightly during implementation if a cleaner split
emerges; the point is no single file staying anywhere near 2,000+ lines.

## Verification

No component test framework exists for this project (consistent with prior work);
verify live in a browser:
- `/` and `/components` show correct branding, correct GitHub link (Discord link
  gone), no visible "Basecoat" text anywhere.
- `/components` lists all 60 components with working links; spot-check a handful
  (e.g. `select`, `tooltip`, `context-menu`) actually navigate to working doc pages.
- `/components/:name` for a few different components: Preview/Code tabs both render
  correctly and match (code shown is what's actually rendered), sidebar highlight/
  prev-next still correct, page still works after the file split.
- `sbt site/compile`, `sbt site/fastLinkJS`, `sbt scalafmtAll`, `franky verify`.
