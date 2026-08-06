# Blocks Pipeline + Four Featured Blocks — Design Spec

Date: 2026-08-05

## Context

`shadcn-scalajs` has 60 components but no *blocks* — the full-page/section
compositions that shadcn/ui and shadcn-svelte both ship (`login-01`,
`sidebar-01`, `dashboard-01`, …). The target experience is
[shadcn-svelte's Blocks page](https://www.shadcn-svelte.com/blocks): a nav link,
an index grouped by category, and per-block Preview/Code tabs with refresh,
"Open in New Tab", a file tree, and an install command. shadcn-svelte is the
right reference because this repo's registry generator and CLI already say they
mirror it (`build-registry.mjs` header comment; `resolveItems`' doc comment).

### Confirmed current state (read from source, 2026-08-05)

- `modules/site/scripts/build-registry.mjs` scans exactly one root
  (`modules/ui/src/main/scala/shadcnscalajs/ui`), non-recursively, for
  `*.registry.json` sidecars; it hardcodes `type: "scala:ui"` and
  `target: "ui/" + file.path` for every emitted file.
- `packages/cli/src/utils/registry.ts`'s `resolveItems` already walks
  `registryDependencies` recursively and dedupes by name.
- `packages/cli/src/commands/add.ts` already writes each file to
  `path.join(cwd, config.sourceDir, file.target)` with `mkdir -p`, so nested
  targets work today.
- `modules/site/vite.config.js` sets no `appType`, so Vite's default SPA
  fallback serves `index.html` for unknown paths — which is why
  `/components/:name` works and why an iframe preview route needs no extra
  HTML entry.
- `build.sbt` has four modules (`core`, `ui`, `webcomponents`, `site`);
  `.franky/scripts/build` names its sbt targets explicitly.
- `Main.scala` is ~2,100 lines and already carries all three existing pages.
  A wholesale split is specified separately in
  `docs/superpowers/specs/2026-08-05-docs-site-ia-redesign-design.md`.

### Upstream availability (checked against `/Users/elam/Projects/ui`)

| Category | Status |
| --- | --- |
| Login | `login-01`…`05` — source present, 2 files / 81 lines for `-01` |
| Signup | `signup-01`…`05` — source present, 2 files / 87 lines for `-01` |
| Sidebar | `sidebar-01`…`16` — source present, 4 files / 325 lines for `-01` |
| Calendar | `calendar-01`…`32` — single-file blocks; full content in the built registry JSON under `apps/v4/public/r/styles/new-york/` |
| OTP | **absent** — featured on shadcn-svelte's page, no React counterpart in this snapshot |

## Goals

1. A blocks authoring location that supports multi-file, nested-directory items.
2. A registry generator that emits block items alongside component items, with
   no change to existing component output.
3. `/blocks`, `/blocks/<name>`, and `/blocks/<name>/preview` routes plus a
   "Blocks" nav link.
4. Four blocks: `login-01`, `signup-01`, `calendar-01` ported from upstream, and
   `otp-01` authored here. (`sidebar-01` was cut during planning — see below.)
5. `shadcn-scalajs add <block>` works end-to-end, pulling in the block's
   transitive component dependencies.

## Non-goals

- **`sidebar-01`** — cut during planning, 2026-08-05. It uses 14 distinct
  Sidebar parts (`SidebarProvider`, `SidebarInset`, `SidebarTrigger`,
  `SidebarRail`, `SidebarInput`, `SidebarGroup`/`GroupLabel`/`GroupContent`,
  `SidebarMenuButton`, …) while `modules/ui/Sidebar.scala` is 19 lines with six
  unrelated stubs (`apply/header/content/footer/menu/menuItem`) and no provider,
  collapse state, inset, trigger or rail. Upstream `sidebar.tsx` is 726 lines
  across 24 exports with a context, a keyboard shortcut, cookie-persisted
  collapse state and a mobile sheet mode. Rebuilding Sidebar is its own
  sub-project; `sidebar-01` lands on top of it afterwards.
- `dashboard-01` (11 files, charts + data table) — deliberately deferred.
- Variants `-02`…`-05` of any category.
- The wholesale `Main.scala` split (separate spec).
- Copy-to-clipboard, "Copy as Markdown", `llms.txt`.
- `Sc*` Web Component wrappers for blocks.
- CLI package/import rewriting (see "Why no CLI changes" below).

## Design

### 1. `modules/blocks` sbt module

```scala
lazy val blocks = project
  .in(file("modules/blocks"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsSettings)
  .settings(noPublish)
  .dependsOn(ui)
```

Plus: add `blocks` to the root `.aggregate(...)`, change `site` to
`.dependsOn(ui, webcomponents, blocks)`, and add `"blocks/compile"` to
`.franky/scripts/build`'s sbt invocation. No `fastLinkJS` target — blocks are
not an entry point; `site` links them in.

Chosen over a `blocks/` subpackage inside `modules/ui` (which would force the
generator's flat scan to recurse and would make "`modules/ui` is what the CLI
copies as components" false) and over hosting blocks in `modules/site` (which
is `noPublish` demo code and would let blocks depend on site-only helpers).

### 2. Authoring layout

```
modules/blocks/src/main/scala/shadcnscalajs/blocks/
  login01/Login01.scala             page-level composition
  login01/LoginForm.scala           component part
  login01/login-01.registry.json    sidecar
  otp01/…  signup01/…  calendar01/…
```

Scala packages cannot contain hyphens, so directories and packages are
`login01` while the **registry item name stays `login-01`**, declared in the
sidecar. Package per block (`shadcnscalajs.blocks.login01`) keeps parts from
colliding across blocks — several blocks legitimately want a `LoginForm` or
`AppSidebar`.

**Deviation from origin:** upstream blocks rely on file-based routing —
`login-01` ships `page.tsx` with `target: "app/login/page.tsx"`, and
shadcn-svelte's ships `routes/dashboard-01/+page.svelte`. Laminar has no
file-based routing, so the page file exposes `def apply(): HtmlElement` for the
consumer to mount in their own router. This is the only place blocks here
cannot be literally identical to origin.

### 3. Sidecar schema

Additive to the existing component sidecar shape:

```json
{
  "name": "login-01",
  "title": "Login 01",
  "type": "scala:block",
  "description": "A simple login form.",
  "categories": ["authentication", "login"],
  "registryDependencies": ["button", "card", "input", "label", "field"],
  "scalaDependencies": [],
  "files": [
    { "path": "login01/Login01.scala",  "type": "scala:page" },
    { "path": "login01/LoginForm.scala", "type": "scala:component" }
  ]
}
```

`description` and `categories` are new and block-only; component sidecars are
untouched. `registryDependencies` are component names, resolved by the existing
CLI walk.

### 4. Generator: two explicit roots

Replace `build-registry.mjs`'s single hardcoded root with a `roots` array:

```js
const roots = [
  { dir: "<repo>/modules/ui/src/main/scala/shadcnscalajs/ui",
    type: "scala:ui",    targetPrefix: "ui/",     recursive: false },
  { dir: "<repo>/modules/blocks/src/main/scala/shadcnscalajs/blocks",
    type: "scala:block", targetPrefix: "blocks/", recursive: true  },
];
```

Per-file `type` comes from the sidecar when present, else the root's `type`.
`index.json` entries gain `description` and `categories` (absent for
components). Component output must stay byte-identical — verify by regenerating
and diffing before/after on an unmodified tree.

The blocks root must tolerate not existing yet, so the generator degrades to
components-only rather than failing.

### 5. Why no CLI changes

`resolveItems` already walks `registryDependencies`; `add.ts` already writes
nested `file.target` paths. So `add login-01` writes
`<sourceDir>/blocks/login01/*.scala` plus every transitive component into
`<sourceDir>/ui/`.

Copied component files keep `package shadcnscalajs.ui`, so a copied block
declaring `package shadcnscalajs.blocks.login01` and importing
`shadcnscalajs.ui.*` compiles in the consumer project with no rewriting. The
known missing package-rewriting (PROGRESS.md "Next") therefore does not block
blocks; it only means consumers keep our namespace.

### 6. Site routes and pages

Router additions in `Main.scala`:

| Path | Page |
| --- | --- |
| `/blocks` | category-grouped index of all blocks, each linking to its page |
| `/blocks/<name>` | title, description, Preview/Code tabs, file tree, install command |
| `/blocks/<name>/preview` | chrome-less: mounts only the block |

A "Blocks" nav link goes in the three existing page headers (`app()`,
`componentsGalleryPage()`, `componentDocsPage()`). Those headers are duplicated
today; unifying them belongs to the IA-redesign spec, not here.

New files rather than growing `Main.scala`:

- `Blocks.scala` — the block list (name, title, description, categories) and
  the `liveBlock(name)` dispatch to each block's `apply()`.
- `BlocksIndexPage.scala` — `/blocks`.
- `BlockDocsPage.scala` — `/blocks/<name>`.
- `BlockPreviewPage.scala` — `/blocks/<name>/preview`.

The **Code tab fetches `/registry/<name>.json` at runtime and renders
`files[].content`**, rather than the hand-maintained `usageSource`-style string
match used for components. A 325-line, 4-file block cannot be kept in a string
literal without drifting, and the registry JSON already holds exact source.
Note the pinned-facade gotcha: the first `.then` needs an explicit type
parameter (`.then[String](_.text())`).

Preview tab holds an `iframe` pointed at `/blocks/<name>/preview`, a refresh
button that re-assigns `src`, and an "Open in New Tab" link. Viewport width
toggles are out of scope.

`Blocks.scala`'s list is hand-maintained, matching the existing
`componentNavList` house style; it can drift from the sidecars. Accepted for
now, with the follow-up noted below.

### 7. The four blocks

`login-01`, `signup-01`, `calendar-01` are ports: read the upstream `.tsx`,
reproduce structure and Tailwind classes exactly, translate React state to
Airstream `Var`s.

`login-01` and `signup-01` both use `FieldGroup`, which `modules/ui/Field.scala`
lacks (it has only `apply`/`label`/`description`/`error`, 12 lines vs upstream
field.tsx's 248). Adding `Field.group` plus a fidelity pass on the existing
three against upstream's class strings is in scope here — it is one bounded
file, unlike the Sidebar gap.

`otp-01` is **authored, not ported** — no OTP block exists upstream in this
snapshot. It follows `login-01`'s structure (centred card, `Field` group) with
our `InputOTP` component as the input, and a "Didn't get a code? Resend" line.

Known Laminar hazards from `AGENTS.md` that these ports will hit: tag-name
collisions (`sectionTag`, `headerTag`, `navTag`, `mainTag`, `labelTag` family),
never naming a `Var` `value`/`children`/`label`, and `js.Date` getters returning
`Double`.

### 8. Fidelity risk

Blocks are a fidelity stress test for the components they compose. The Alert
audit (2026-08-05) found Alert was a v3-era/basecoat hybrid whose layout came
entirely from `basecoat.generated.css`; the same is expected of many of the
other 59. The Sidebar gap above was found this way, and `Card`, `Input`, `Label`
and `Field` all still carry basecoat hook classes (`card cn-card`, `input
cn-input`, `label cn-label`, `field`), so the four remaining ports may surface
more of the same.

Policy for this spec: fix small component gaps in place (as with Alert), and
file anything large as follow-up work rather than shipping a block that only
looks correct while a style pack is active. A block that renders correctly only
because of `basecoat.generated.css` is a defect, because consumers get neither
that file nor the presets.

## Verification

- `sbt core/compile ui/compile blocks/compile webcomponents/compile site/compile`,
  then `sbt scalafmtAll`.
- `franky verify` exits 0.
- Browser (real Chrome, per the approach recorded in `decisions.log`
  2026-08-05): `/blocks` lists all four with working links; each
  `/blocks/<name>` renders Preview (iframe loads, refresh works, Open in New
  Tab opens the bare route) and Code (file tree plus real source from the
  registry JSON); each `/blocks/<name>/preview` renders chrome-less; light and
  dark both checked.
- Computed-style assertions for at least `login-01` with `data-style-pack`
  removed, proving the block does not depend on pack or basecoat CSS.
- `./scripts/test` extended to cover a **block** `add` — it currently only
  covers components. Assert the block's nested files *and* its transitive
  component files land in the temp project.
- Registry regeneration produces byte-identical output for all 60 existing
  component items.

## Follow-ups (not in this slice)

- Generate `Blocks.scala`'s list from the sidecars to remove hand-maintained
  drift.
- `dashboard-01`, and variants `-02`…`-05` per category.
- Viewport width toggles on the Preview tab.
- Whatever component fidelity gaps the ports surface.
