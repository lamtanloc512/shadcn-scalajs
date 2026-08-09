# Featured Blocks Wave — Design Spec

Date: 2026-08-09

## Context

The blocks pipeline from
[`2026-08-05-blocks-pipeline-design.md`](./2026-08-05-blocks-pipeline-design.md)
is live: `modules/blocks`, registry root for `scala:block`, `/blocks` index,
and per-block Preview/Code + iframe preview. We already ship `login-01`,
`signup-01`, `otp-01`, `calendar-01`, and a **non-upstream** `dashboard-01`
(original Acme Analytics layout written for an earlier `/create` preview).

The [shadcn-svelte Blocks landing page](https://www.shadcn-svelte.com/blocks)
features five demos that visitors see first:

| Featured block | Upstream shape | Our status |
| --- | --- | --- |
| `dashboard-01` | ~20 files: inset sidebar, section cards, area chart, interactive data table | Simplified original page; not a port |
| `sidebar-07` | Collapses to icons; team switcher + nav pieces | Missing |
| `sidebar-03` | Nested submenus | Missing |
| `login-03` | Centered brand + form on muted background | Missing (`login-01` only) |
| `login-04` | Split form + image | Missing |

`Sidebar.scala` is now a full ~450-line surface (provider, inset, trigger,
rail, menuSub, collapsible cookie state). `Chart` and `DataTable` exist as
pure Laminar primitives. Local upstream source lives at
`/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/blocks/`. The
current `dashboard-01` is only referenced from `Blocks.scala` — `/create`
uses preview-02 — so replacing it is safe.

## Goals

1. Port the five featured blocks into `modules/blocks` using our UI components.
2. Replace the existing original `dashboard-01` with a full structural port of
   upstream's featured dashboard (same name, multi-file layout).
3. Register all five on `/blocks` with working Preview/Code and install
   commands (`add login-03`, etc.).
4. Browser-verify each against the live shadcn-svelte demo for layout,
   collapse/stack behaviour, and primary interactions.

## Non-goals

- Other login/signup/sidebar/calendar variants (`-01`…`-05` / `-16` / `-32`).
- A shared cross-block nav kit (`NavMain` shared between blocks) — upstream
  keeps those files per-block so `add <block>` stays self-contained.
- Rewriting `Chart` / `DataTable` beyond what these five need.
- Web Component (`Sc*`) wrappers for blocks.
- Changing the blocks pipeline, CLI, or docs IA.

## Decisions already taken

| Decision | Choice |
| --- | --- |
| Scope | Featured five only |
| `dashboard-01` fidelity | Full structural port (same file breakdown and features as upstream; omit only interactions we cannot express yet) |
| Execution order | Easy → hard: logins → sidebars → dashboard rewrite |
| Source of truth | Local shadcn-svelte registry blocks (not React) |
| Authoring layout | Same as `login-01`: package dir without hyphen (`login03/`), registry name with hyphen (`login-03`) |

## Design

### 1. Authoring layout (unchanged conventions)

```
modules/blocks/src/main/scala/shadcnscalajs/blocks/
  login03/
    Login03.scala              # page: def apply(): HtmlElement
    LoginForm.scala
    login-03.registry.json
  login04/
    Login04.scala
    LoginForm.scala
    login-04.registry.json
  sidebar03/
    Sidebar03.scala
    AppSidebar.scala
    sidebar-03.registry.json
  sidebar07/
    Sidebar07.scala
    AppSidebar.scala
    NavMain.scala
    NavProjects.scala
    NavUser.scala
    TeamSwitcher.scala
    sidebar-07.registry.json
  dashboard01/                 # REPLACE contents
    Dashboard01.scala
    AppSidebar.scala
    SiteHeader.scala
    SectionCards.scala
    ChartAreaInteractive.scala
    DataTable.scala            # block-local composition, not ui.DataTable
    …column helpers as upstream splits them…
    data.scala                 # typed sample rows (was data.ts)
    dashboard-01.registry.json
```

Each sidecar keeps `"type": "scala:block"`, lists `registryDependencies` as
component names, and lists every Scala file under `files` with
`scala:page` / `scala:component`. Packages stay per-block so multiple
`AppSidebar` / `LoginForm` objects do not collide.

### 2. Port mapping per block

#### `login-03` / `login-04`

Straight layout ports from upstream `+page.svelte` + `login-form.svelte`.
Reuse the Field / Card / Input / Button / Label patterns from our `login-01`.
Icons go through `Icons.*` (add any missing concepts to all five icon
libraries if needed — same rule as star/heart/bookmark). No new UI
components.

#### `sidebar-03` / `sidebar-07`

Compose `Sidebar.provider` → `Sidebar.root` / `AppSidebar` → `Sidebar.inset`
with trigger, breadcrumb header, and muted placeholder content matching
upstream. `sidebar-07` uses `Sidebar.Collapsible.Icon` plus team switcher
and project nav. `sidebar-03` uses `menuSub` / `menuSubButton` for nested
items.

Trigger and rail need the open `Var` from the provider context — follow the
existing `Sidebar` API (`trigger(openVar)`, `rail(openVar)`, cookie-backed
`provider`). Do not invent a parallel context.

#### `dashboard-01` (rewrite)

Delete the current single-file Acme Analytics composition. Rebuild as a
multi-file port matching upstream structure:

- Page wires `Sidebar.provider` with inset CSS vars, `AppSidebar(variant =
  Inset)`, `SiteHeader`, `SectionCards`, `ChartAreaInteractive`, block
  `DataTable`.
- Chart uses `shadcnscalajs.ui.Chart` (area + interactive range controls as
  upstream's chart-area-interactive).
- Table uses `shadcnscalajs.ui.DataTable` state (sort, filter, column
  visibility, pagination, row selection). Column cell widgets that upstream
  splits into separate files become separate Scala objects in the same
  package.
- Sample data ports from `data.ts` into a Scala object (case classes + vals).

**Explicit omissions** (document in the block description and this section if
they remain after the port attempt):

- Column **drag-and-drop reordering** if our DataTable has no DnD surface —
  leave columns in fixed order rather than pulling in a DnD library.
- Anything else discovered during port that requires a new UI primitive
  beyond a small, block-local helper — list it; do not silently fake it.

Prefer porting sheet-based cell viewers and row action menus when Sheet /
DropdownMenu already support them.

### 3. Site catalog

Update `Blocks.scala`:

- Add Meta entries for `login-03`, `login-04`, `sidebar-03`, `sidebar-07`.
- Replace the `dashboard-01` description with upstream's ("A dashboard with
  sidebar, charts and data table").
- Extend `render` match arms.
- Categories: login → authentication/login; sidebars → sidebar; dashboard →
  dashboard (and analytics if useful).

No changes to `BlockDocsPage` / preview iframe wiring — they already key off
`Blocks.find` / `Blocks.render`.

### 4. Registry and CLI

After each block lands, run `node modules/site/scripts/build-registry.mjs`.
`add <name>` must pull the block files under `blocks/<pkg>/` plus transitive
UI dependencies. No CLI code changes.

### 5. Verification

For each block, against `https://www.shadcn-svelte.com/blocks` (or the
per-block deep link) and `http://localhost:4300/blocks/<name>`:

1. Preview iframe loads; Refresh and Open in New Tab work.
2. Screenshot / layout compare for first viewport.
3. Sidebars: collapse to icons (`sidebar-07`), expand submenus (`sidebar-03`),
   mobile sheet if our Sidebar already provides it.
4. Dashboard: cards render, chart paints with theme chart tokens, table sorts
   / filters / toggles columns; note any documented omissions.
5. `scalafmtAll` + `blocks/compile` + `site/fastLinkJS` green.

### 6. Work order

1. `login-03`, `login-04` (small; prove registry + catalog path).
2. `sidebar-03`, then `sidebar-07` (exercise Sidebar; patterns feed dashboard).
3. Replace `dashboard-01` last.
4. Final pass: index page featured order if we surface a "Featured" strip
   later — out of scope unless trivial; the index already groups by category.

## Risks

| Risk | Mitigation |
| --- | --- |
| Dashboard table interactions exceed DataTable | Port what we can; document omissions; do not block the whole wave |
| Icon concepts missing (gallery-vertical-end, etc.) | Add to all five icon libraries in the same change as the block that needs them |
| Sidebar trigger needs open Var plumbing awkward in blocks | Follow existing Sidebar API; if awkward, fix Sidebar once with a small helper before copying the pattern into three blocks |
| Large dashboard commit hard to review | Keep logins and sidebars as separate commits; one commit for the dashboard rewrite |

## Success criteria

- All five names appear on `/blocks` and open working previews.
- `login-03`, `login-04`, `sidebar-03`, `sidebar-07` are recognizable ports of
  the shadcn-svelte demos.
- `dashboard-01` is no longer the Acme Analytics original; it matches
  upstream's sidebar + cards + chart + table structure.
- Registry JSON regenerated; no unrelated component registry churn beyond
  files these blocks touch.
- Documented omissions (if any) are explicit in this spec and the block
  description — not silent visual fakes.
