# Featured Blocks Wave Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the five featured shadcn-svelte blocks (`login-03`, `login-04`, `sidebar-03`, `sidebar-07`, and a full structural rewrite of `dashboard-01`) into `modules/blocks` and register them on `/blocks`.

**Architecture:** Each block is a per-package Laminar composition under `modules/blocks` (hyphenated registry name, package-legal directory), with a `*.registry.json` sidecar. Source of truth is `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/blocks/<name>/`. Tailwind class strings are copied verbatim. Shared UI comes from `modules/ui` (`Sidebar`, `Field`, `Chart`, `DataTable`, `Breadcrumb`, etc.). Site catalog is hand-updated in `Blocks.scala`. Registry regenerated via `node modules/site/scripts/build-registry.mjs`.

**Tech Stack:** Scala 3.5.2, Scala.js, Laminar 17.2.1, Airstream `Var`, Tailwind CSS v4, Vite, existing blocks pipeline.

## Global Constraints

- Spec: `docs/superpowers/specs/2026-08-09-featured-blocks-design.md`. Read it before starting.
- Upstream source root: `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/blocks/`.
- `sbt` needs `export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"` first.
- Tailwind class strings are copied **verbatim** from the named upstream file. Do not reformat, reorder, or "improve" them.
- Laminar tag collisions: use `headerTag`, `mainTag`, `navTag`, `asideTag`, `sectionTag`, `form` is fine as `form(...)`.
- Never name a `Var` or parameter `value`, `children`, `content`, or `label` — they shadow Laminar keys.
- `Button.apply` has **no** default variant/size. Always `Button.of(_.variant(...), _.size(...), ...)`. Bare `Button(...)` renders unstyled (~20px tall).
- Sidebar open state: create **one** `Var[Boolean]` and pass it to `Sidebar.provider(openVar)`, `Sidebar.root(..., openVar = openVar)`, `Sidebar.trigger(openVar)`, and `Sidebar.rail(openVar)`. Separate Vars break collapse.
- Icon concepts added to `Icons.Concepts` must get path data in **all five** of Lucide / Tabler / Phosphor / Remix / Hugeicons.
- Brand logos (Apple / Google / Meta) stay as **inline SVG paths** in the block form — do not add them to `Icons.Concepts`.
- Run `sbt scalafmtAll` before finishing any task that touches `.scala`.
- Do **not** `git commit` or `git push` unless the user explicitly asks. "Commit" steps below mean `git add` staging only (or stop and ask).
- There is no Scala test framework. "Test" means: compiles, `scalafmtCheckAll` passes, and the stated browser observation holds on `http://localhost:4300`.

## File map

| Path | Responsibility |
| --- | --- |
| `modules/blocks/.../login03/*` | Featured login with brand header + OAuth buttons |
| `modules/blocks/.../login04/*` | Split login form + image panel |
| `modules/blocks/.../sidebar03/*` | Sidebar with nested `menuSub` items |
| `modules/blocks/.../sidebar07/*` | Icon-collapsible sidebar + team/nav pieces |
| `modules/blocks/.../dashboard01/*` | **Replace** current Acme Analytics page with upstream multi-file dashboard |
| `modules/site/.../Blocks.scala` | Catalog Meta + `render` arms |
| `modules/ui/.../Icons.scala` (+ 5 icon data files) | Add `gallery-vertical-end` (and any other Lucide concepts these blocks need) |
| `modules/site/public/registry/*.json` | Regenerated — do not hand-edit |

---

### Task 1: `gallery-vertical-end` icon concept

Both `login-03` and `sidebar-03` use Lucide `GalleryVerticalEnd`. Add it to the shared icon registry before those blocks.

**Files:**
- Modify: `modules/ui/src/main/scala/shadcnscalajs/ui/Icons.scala`
- Modify: `modules/ui/src/main/scala/shadcnscalajs/ui/icons/{Lucide,Tabler,Phosphor,Remix,Hugeicons}IconData.scala`

**Interfaces:**
- Consumes: existing `Icons.icon` / `IconDataSource` pattern.
- Produces: `Icons.galleryVerticalEnd(mods*): SvgElement` and concept `"gallery-vertical-end"` in all five `paths` maps.

- [ ] **Step 1: Add the concept and accessor**

In `Icons.Concepts`, insert alphabetically after `"gauge"` (before `"globe"`):

```scala
"gallery-vertical-end",
```

Add accessor next to the other `g*` helpers:

```scala
def galleryVerticalEnd(mods: Modifier[SvgElement]*): SvgElement = icon("gallery-vertical-end")(mods*)
```

- [ ] **Step 2: Add Lucide path data**

In `LucideIconData.scala`, after `"gauge"` (or nearest alpha neighbor):

```scala
"gallery-vertical-end" -> Seq(
  "M7 2h10",
  "M5 6h14",
  "M6 10h12a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V11a1 1 0 0 1 1-1z"
),
```

(Use the current Lucide path list for `gallery-vertical-end` if the published paths differ — prefer the paths from `@lucide/svelte` / Lucide source over inventing them.)

- [ ] **Step 3: Add the same concept to Tabler, Phosphor, Remix, Hugeicons**

Look up each library's equivalent glyph (Tabler `layout-navbar` / `stack-2`, Phosphor `rows`, Remix `gallery-view-2`, Hugeicons gallery/stack). Insert alphabetically. Every library must have a non-empty `Seq` for `"gallery-vertical-end"` so coverage stays equal.

- [ ] **Step 4: Compile**

```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt -batch "ui/compile" scalafmtAll
```

Expected: success.

- [ ] **Step 5: Stage**

```bash
git add modules/ui/src/main/scala/shadcnscalajs/ui/Icons.scala \
  modules/ui/src/main/scala/shadcnscalajs/ui/icons/*.scala
```

---

### Task 2: `login-03`

**Files:**
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/login03/Login03.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/login03/LoginForm.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/login03/login-03.registry.json`
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/Blocks.scala`
- Reference (read-only):
  - `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/blocks/login-03/+page.svelte`
  - `/Users/elam/Personal/shadcn-svelte/docs/src/lib/registry/blocks/login-03/components/login-form.svelte`
  - Our pattern: `modules/blocks/.../login01/LoginForm.scala`

**Interfaces:**
- Consumes: `Field.group`, `Field.separator`, `Field.label`, `Field.description`, `Card.*`, `Input`, `Button.of`, `Icons.galleryVerticalEnd`.
- Produces: `Login03.apply(): HtmlElement`, `LoginForm.apply(mods*): HtmlElement`.

- [ ] **Step 1: Write `Login03.scala`**

```scala
package shadcnscalajs.blocks.login03

import com.raquo.laminar.api.L.*
import shadcnscalajs.ui.*

/** Port of shadcn-svelte `blocks/login-03/+page.svelte`. */
object Login03:

  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh flex-col items-center justify-center gap-6 bg-muted p-6 md:p-10",
      div(
        cls := "flex w-full max-w-sm flex-col gap-6",
        a(
          href := "#",
          cls := "flex items-center gap-2 self-center font-medium",
          div(
            cls := "flex size-6 items-center justify-center rounded-md bg-primary text-primary-foreground",
            Icons.galleryVerticalEnd()
          ),
          "Acme Inc."
        ),
        LoginForm()
      )
    )
```

- [ ] **Step 2: Write `LoginForm.scala`**

Port `login-form.svelte` using our Field/Card/Button APIs. Keep upstream class strings on the card header (`text-center`), separator (`*:data-[slot=field-separator-content]:bg-card`), and description. Inline Apple/Google SVG paths from upstream (fill `currentColor`). Primary submit and outline OAuth buttons must use `Button.of` with explicit variant/size. Pattern for separator:

```scala
Field.separator(cls := "*:data-[slot=field-separator-content]:bg-card")(span("Or continue with"))
```

Use `forId := "email"` / `idAttr := "email"` (and password) like `login-01` — no Svelte `$props.id()`.

- [ ] **Step 3: Write `login-03.registry.json`**

```json
{
  "name": "login-03",
  "title": "Login 03",
  "type": "scala:block",
  "description": "A login page with a muted background and brand header.",
  "categories": ["authentication", "login"],
  "registryDependencies": ["button", "card", "input", "field", "separator", "icons"],
  "scalaDependencies": [],
  "files": [
    { "path": "login03/Login03.scala", "type": "scala:page" },
    { "path": "login03/LoginForm.scala", "type": "scala:component" }
  ]
}
```

- [ ] **Step 4: Wire `Blocks.scala`**

Add import `shadcnscalajs.blocks.login03.Login03`. Insert Meta after `login-01`:

```scala
Meta("login-03", "Login 03", "A login page with a muted background and brand header.", List("authentication", "login")),
```

Add `case "login-03" => Some(Login03())` in `render`.

- [ ] **Step 5: Build registry + compile + browser check**

```bash
node modules/site/scripts/build-registry.mjs
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt -batch scalafmtAll blocks/compile site/fastLinkJS
```

Open `http://localhost:4300/blocks/login-03` (dev server on 4300). Expected: Preview shows centered Acme header + card with Apple/Google + email/password; Code tab lists both files; Refresh works.

- [ ] **Step 6: Stage**

```bash
git add modules/blocks/src/main/scala/shadcnscalajs/blocks/login03 \
  modules/site/src/main/scala/shadcnscalajs/site/Blocks.scala \
  modules/site/public/registry/login-03.json modules/site/public/registry/index.json
```

---

### Task 3: `login-04`

**Files:**
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/login04/Login04.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/login04/LoginForm.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/login04/login-04.registry.json`
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/Blocks.scala`
- Reference:
  - `.../login-04/+page.svelte`
  - `.../login-04/components/login-form.svelte`

**Interfaces:**
- Consumes: same Field/Card/Button stack as Task 2.
- Produces: `Login04.apply(): HtmlElement`, `LoginForm.apply(mods*): HtmlElement`.

- [ ] **Step 1: Write page + form**

Page:

```scala
object Login04:
  def apply(): HtmlElement =
    div(
      cls := "flex min-h-svh flex-col items-center justify-center bg-muted p-6 md:p-10",
      div(cls := "w-full max-w-sm md:max-w-3xl", LoginForm())
    )
```

Form: port the split `md:grid-cols-2` card. Image panel:

```scala
div(
  cls := "relative hidden bg-muted md:block",
  img(
    src := "/placeholder.svg",
    alt := "placeholder",
    cls := "absolute inset-0 h-full w-full object-cover dark:brightness-[0.2] dark:grayscale"
  )
)
```

If `/placeholder.svg` is missing from `modules/site/public`, add a minimal SVG placeholder there (or reuse any existing public asset) so the preview is not a broken image.

OAuth row: `Field(cls := "grid grid-cols-3 gap-4", ...)` with three outline icon-only buttons (Apple / Google / Meta) and `span(cls := "sr-only", ...)`.

- [ ] **Step 2: Registry + Blocks.scala**

Same sidecar shape as login-03 with name `login-04`, description matching upstream ("A login page with form and image."). Wire Meta + `render`.

- [ ] **Step 3: Build, browser-check `/blocks/login-04`, stage**

Expected: split card on desktop; image panel on the right; three OAuth icon buttons.

---

### Task 4: `sidebar-03`

**Files:**
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/sidebar03/Sidebar03.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/sidebar03/AppSidebar.scala`
- Create: `modules/blocks/src/main/scala/shadcnscalajs/blocks/sidebar03/sidebar-03.registry.json`
- Modify: `modules/site/src/main/scala/shadcnscalajs/site/Blocks.scala`
- Reference:
  - `.../sidebar-03/+page.svelte`
  - `.../sidebar-03/components/app-sidebar.svelte`
- Read API: `modules/ui/.../Sidebar.scala` (`provider`, `root`, `inset`, `trigger`, `rail`, `menuSub`, `menuSubButton`)

**Interfaces:**
- Consumes: `Sidebar.*`, `Breadcrumb.*`, `Separator`, `Icons.galleryVerticalEnd`.
- Produces: `Sidebar03.apply(): HtmlElement`, `AppSidebar.apply(openVar: Var[Boolean])(mods*): HtmlElement`.

- [ ] **Step 1: Write `AppSidebar.scala`**

Port the nested nav data as Scala case classes / vals. Structure:

```scala
object AppSidebar:
  final case class SubItem(title: String, url: String, isActive: Boolean = false)
  final case class NavItem(title: String, url: String, items: List[SubItem] = Nil)

  private val navMain: List[NavItem] = List(/* copy titles from upstream module script */)

  def apply(openVar: Var[Boolean])(mods: Modifier[HtmlElement]*): HtmlElement =
    Sidebar.root(collapsible = Sidebar.Collapsible.Offcanvas, openVar = openVar)(
      mods,
      Sidebar.header(/* Documentation brand menuButton size lg + galleryVerticalEnd */),
      Sidebar.content(
        Sidebar.group(
          Sidebar.menu(
            navMain.map { item =>
              Sidebar.menuItem(
                Sidebar.menuButton(false)(cls := "font-medium", a(href := item.url, item.title)),
                Option.when(item.items.nonEmpty)(
                  Sidebar.menuSub(
                    item.items.map { sub =>
                      Sidebar.menuSubItem(
                        Sidebar.menuSubButton(isActive = sub.isActive)(a(href := sub.url, sub.title))
                      )
                    }
                  )
                )
              )
            }
          )
        )
      ),
      Sidebar.rail(openVar)
    )
```

Use `menuButtonOf` if size `Lg` is required for the brand row (`MenuButtonSize`).

- [ ] **Step 2: Write `Sidebar03.scala` page**

```scala
object Sidebar03:
  def apply(): HtmlElement =
    val openVar = Var(true)
    Sidebar.provider(openVar)(
      AppSidebar(openVar)(),
      Sidebar.inset(
        headerTag(
          cls := "flex h-16 shrink-0 items-center gap-2 border-b",
          div(
            cls := "flex items-center gap-2 px-3",
            Sidebar.trigger(openVar)(),
            Separator(Separator.Orientation.Vertical, cls := "me-2 h-4"),
            Breadcrumb(
              Breadcrumb.list(
                Breadcrumb.item(cls := "hidden md:block", Breadcrumb.link("#", "Build Your Application")),
                Breadcrumb.separator(cls := "hidden md:block"),
                Breadcrumb.item(Breadcrumb.page("Data Fetching"))
              )
            )
          )
        ),
        div(
          cls := "flex flex-1 flex-col gap-4 p-4",
          div(
            cls := "grid auto-rows-min gap-4 md:grid-cols-3",
            div(cls := "aspect-video rounded-xl bg-muted/50"),
            div(cls := "aspect-video rounded-xl bg-muted/50"),
            div(cls := "aspect-video rounded-xl bg-muted/50")
          ),
          div(cls := "min-h-screen flex-1 rounded-xl bg-muted/50 md:min-h-min")
        )
      )
    )
```

Match `Breadcrumb` / `Separator` call shapes to the actual Scala APIs if the snippets above need trivial adjustment — do not invent new wrappers.

- [ ] **Step 3: Registry**

`registryDependencies`: `sidebar`, `breadcrumb`, `separator`, `icons`. Categories: `["sidebar"]`. Description: `"A sidebar with submenus."`

- [ ] **Step 4: Blocks.scala + build + browser**

Expected at `/blocks/sidebar-03`: nested submenu under "Build Your Application" with "Data Fetching" active; trigger toggles offcanvas; Cmd/Ctrl+B toggles.

- [ ] **Step 5: Stage**

---

### Task 5: `sidebar-07`

**Files:**
- Create: `modules/blocks/.../sidebar07/Sidebar07.scala`
- Create: `modules/blocks/.../sidebar07/AppSidebar.scala`
- Create: `modules/blocks/.../sidebar07/NavMain.scala`
- Create: `modules/blocks/.../sidebar07/NavProjects.scala`
- Create: `modules/blocks/.../sidebar07/NavUser.scala`
- Create: `modules/blocks/.../sidebar07/TeamSwitcher.scala`
- Create: `modules/blocks/.../sidebar07/sidebar-07.registry.json`
- Modify: `Blocks.scala`
- Reference: all files under `.../sidebar-07/`

**Interfaces:**
- Consumes: `Sidebar.Collapsible.Icon`, DropdownMenu (for team/user menus if upstream uses them), `Icons.*`.
- Produces: `Sidebar07.apply(): HtmlElement` and the five component objects.

- [ ] **Step 1: Port component files one-for-one from upstream**

Read each svelte file and produce a Scala object with the same responsibility. Pass `openVar` into `AppSidebar` → `Sidebar.root(collapsible = Sidebar.Collapsible.Icon, openVar = openVar)`. Header height class on the page must keep upstream's `group-has-data-[collapsible=icon]/sidebar-wrapper:h-12`.

Add any missing Lucide concepts these files need (e.g. `chevrons-up-down` already exists; check `audio-lines`, `book-open`, etc. against `Icons.Concepts` before inventing). Prefer existing concepts; only extend the icon registry when a glyph is absent.

- [ ] **Step 2: Page wiring**

Same `openVar` shared across provider / AppSidebar / trigger. Header classes verbatim from `sidebar-07/+page.svelte`.

- [ ] **Step 3: Registry lists every Scala file under `files`**

- [ ] **Step 4: Build, browser-check collapse-to-icons, stage**

Expected: collapsing shrinks to icon rail; team switcher and user menu open; projects list renders.

---

### Task 6: Rewrite `dashboard-01` shell (page, sidebar, header, cards)

Delete the current original Acme Analytics single-file dashboard and start the upstream structure. Chart and data table land in Tasks 7–8.

**Files:**
- Delete / replace: `modules/blocks/.../dashboard01/Dashboard01.scala`
- Create: `AppSidebar.scala`, `NavMain.scala`, `NavSecondary.scala`, `NavDocuments.scala`, `NavUser.scala`, `SiteHeader.scala`, `SectionCards.scala`
- Modify: `dashboard-01.registry.json` (expand `files` + dependencies as files land)
- Modify: `Blocks.scala` Meta description → `"A dashboard with sidebar, charts and data table."`
- Reference: matching files under `.../dashboard-01/`

**Interfaces:**
- Consumes: `Sidebar` with `Variant.Inset`, `Collapsible.Icon` (match upstream app-sidebar), Card, Badge as used by section cards.
- Produces: `Dashboard01.apply(): HtmlElement` mounting the shell with placeholder regions for chart + table.

- [ ] **Step 1: Remove the old dashboard body**

Replace `Dashboard01.scala` so it no longer contains Acme Analytics / weekly activity bars. Keep the object name `Dashboard01`.

- [ ] **Step 2: Port nav + sidebar + site header + section cards**

Upstream page CSS vars:

```text
--sidebar-width: calc(var(--spacing) * 72); --header-height: calc(var(--spacing) * 12);
```

`Sidebar.provider` currently hardcodes `--sidebar-width: 16rem`. For this block, either:

1. Pass an extra `styleAttr := "--sidebar-width: calc(var(--spacing) * 72); --header-height: calc(var(--spacing) * 12);"` on the provider root via a wrapper/`cls`/`style` mod if the API allows appending styles, **or**
2. Add an optional `styleMods` / override parameter to `Sidebar.provider` in `modules/ui` (small, justified by this block).

Prefer (2) only if (1) cannot override the inline style. Document which you chose in the PR/commit message later.

`AppSidebar` uses `Sidebar.root(variant = Sidebar.Variant.Inset, collapsible = Sidebar.Collapsible.Icon, openVar = openVar)`.

- [ ] **Step 3: Temporary placeholders**

In the main column, after `SectionCards`, leave:

```scala
div(cls := "px-4 lg:px-6", div(cls := "flex h-[250px] items-center justify-center rounded-xl border border-dashed text-sm text-muted-foreground", "ChartAreaInteractive — Task 7"))
div(cls := "px-4 lg:px-6", div(cls := "flex h-[200px] items-center justify-center rounded-xl border border-dashed text-sm text-muted-foreground", "DataTable — Task 8"))
```

So `/blocks/dashboard-01` already shows the real chrome.

- [ ] **Step 4: Update registry dependencies** (`sidebar`, `breadcrumb`, `separator`, `card`, `badge`, `button`, `avatar`, `dropdown-menu`, `icons` as needed) and `files` list for shell files only.

- [ ] **Step 5: Build, open `/blocks/dashboard-01`, confirm inset sidebar + four section cards, stage**

---

### Task 7: `ChartAreaInteractive`

**Files:**
- Create: `modules/blocks/.../dashboard01/ChartAreaInteractive.scala`
- Modify: `Dashboard01.scala` (swap chart placeholder)
- Modify: `dashboard-01.registry.json`
- Reference: `.../dashboard-01/components/chart-area-interactive.svelte`
- Read: `modules/ui/.../Chart.scala` (`Chart.area`, `Chart.HoverVar`, tooltip helpers)

**Interfaces:**
- Consumes: `Chart.*`, `Card.*`, `ToggleGroup` or `Select` for the range control (match upstream).
- Produces: `ChartAreaInteractive.apply(): HtmlElement`.

- [ ] **Step 1: Port chart data + interactive range**

Translate the svelte series / date-range logic into Scala vals + a `Var` for the selected range. Prefer `Chart.area` with `--chart-*` tokens. Do not add a JS charting library.

- [ ] **Step 2: Wire into `Dashboard01`**, rebuild, browser-check that the chart paints and range toggles update the series.

- [ ] **Step 3: Stage**

---

### Task 8: Dashboard `DataTable` + sample data

**Files:**
- Create: `modules/blocks/.../dashboard01/data.scala` (from `data.ts`)
- Create: `modules/blocks/.../dashboard01/DataTable.scala` (block-local; do not put in `ui`)
- Create column helpers as separate objects **only when** the file is non-trivial (status, reviewer, limit, target, type, actions, checkbox, cell-viewer). Skip empty 1-line svelte stubs by inlining.
- Modify: `Dashboard01.scala`, `dashboard-01.registry.json`
- Reference: `data.ts`, `data-table.svelte`, and the helper components
- Read: `modules/ui/.../DataTable.scala`

**Interfaces:**
- Consumes: `shadcnscalajs.ui.DataTable` state helpers, `Table`, `Checkbox`, `DropdownMenu`, `Sheet` (cell viewer), `Select`/`Badge` as upstream uses.
- Produces: typed row model + `dashboard01.DataTable.apply(rows): HtmlElement`.

- [ ] **Step 1: Port `data.ts` → `data.scala`**

Case classes for each row shape; `val data: List[Row] = List(...)`. Keep the same sample content.

- [ ] **Step 2: Port the table**

Wire sort, global filter, column visibility, pagination, row selection through `ui.DataTable`. Port cell viewer with `Sheet` if feasible.

**Omission rule (from spec):** if column drag-and-drop cannot be done without a new DnD dependency, omit `data-table-drag-handle` and note in `dashboard-01.registry.json` description:

```text
A dashboard with sidebar, charts and data table. (Column drag-reorder omitted — no DnD in ui.DataTable.)
```

Do not fake a drag handle that does nothing.

- [ ] **Step 3: Replace table placeholder in `Dashboard01`**, expand registry `files` + `registryDependencies` (`table`, `checkbox`, `dropdown-menu`, `sheet`, `select`, `label`, `tabs`, … as actually imported).

- [ ] **Step 4: Build + browser-check**

Expected: table sorts on header click; filter input narrows rows; column visibility menu works; row selection toggles; sheet viewer opens when that control exists.

- [ ] **Step 5: Stage entire `dashboard01/` package + Blocks Meta + registry**

---

### Task 9: Final verification pass

- [ ] **Step 1: Regenerated registry is complete**

```bash
node modules/site/scripts/build-registry.mjs
ls modules/site/public/registry/{login-03,login-04,sidebar-03,sidebar-07,dashboard-01}.json
```

- [ ] **Step 2: Compile everything**

```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt -batch scalafmtCheckAll blocks/compile site/fastLinkJS
```

- [ ] **Step 3: Browser matrix**

| URL | Check |
| --- | --- |
| `/blocks` | All five names listed with correct titles |
| `/blocks/login-03` | Matches upstream featured login |
| `/blocks/login-04` | Split form + image |
| `/blocks/sidebar-03` | Nested subs + trigger |
| `/blocks/sidebar-07` | Collapse to icons |
| `/blocks/dashboard-01` | Inset sidebar + cards + chart + table; no Acme Analytics leftovers |

Compare each to `https://www.shadcn-svelte.com/blocks` (open the matching preview). Capture screenshots if useful — do not commit them unless asked.

- [ ] **Step 4: Ask the user to commit**

Summarize files changed and proposed commit message(s). Prefer separate commits: icons → login-03/04 → sidebars → dashboard, matching the task boundaries — only when the user says to commit.

---

## Spec coverage checklist

| Spec requirement | Task |
| --- | --- |
| Port `login-03` | 2 |
| Port `login-04` | 3 |
| Port `sidebar-03` | 4 |
| Port `sidebar-07` | 5 |
| Rewrite `dashboard-01` multi-file | 6–8 |
| Easy → hard order | Task order 2→8 |
| Per-block packages, no shared nav kit | Tasks 4–8 keep local `Nav*` |
| Register on `/blocks` | Each task updates `Blocks.scala` |
| Registry sidecars + regenerate | Each task + Task 9 |
| Document DataTable DnD omission | Task 8 |
| Browser verify vs upstream | Tasks 2–5, 9 |
| `gallery-vertical-end` icon | Task 1 |

## Self-review notes

- No TBD/TODO placeholders remain.
- `Sidebar` openVar sharing called out in Global Constraints and Tasks 4–6.
- `Button.of` required (house footgun from the first blocks wave).
- Dashboard split into shell / chart / table so each task is reviewable alone.
