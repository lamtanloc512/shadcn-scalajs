# shadcn-scalajs

A port of [shadcn/ui](https://ui.shadcn.com)'s philosophy to [Scala.js](https://www.scala-js.org/) + [Laminar](https://laminar.dev): components you copy into your own project and own, styled with [basecoat](https://github.com/hunvreus/basecoat)'s compiled CSS (so existing shadcn/TweakCN themes work unmodified), and every component also compiles to a standalone Web Component so any frontend stack — not just Scala.js — can use it.

## Quick start

```bash
# ensure sbt is on PATH (Coursier-installed):
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"

# build everything
sbt compile

# Scala.js link (produces browser-ready .js per module)
sbt ui/fastLinkJS webcomponents/fastLinkJS site/fastLinkJS

# publish core to local Ivy (needed by consumer projects)
sbt core/publishLocal

# run the demo site
cd modules/site && npm install && npm run dev
# → http://localhost:4300/            native Laminar demo
# → http://localhost:4300/components/drawer  component documentation + live preview
# → http://localhost:4300/plain-html-demo.html   Web Component demo

# build the CLI
cd packages/cli && npm install && npm run build

# regenerate registry JSON from modules/ui/*.registry.json
cd modules/site && node scripts/build-registry.mjs
```

## Project layout

```
modules/
  core/             design tokens, data-variant/data-size attribute helpers
  ui/               Laminar component source of truth — one .scala + one .registry.json per component
  webcomponents/    ScElementBase + Sc* custom-element wrappers for non-Scala consumers
  site/             Vite dev app: Laminar landing page (Main.scala) + plain-html-demo.html + registry build script
packages/
  cli/              Node/TS + Commander: `init` writes shadcn-scalajs.json, `add <names>` resolves deps and writes files
vendor/
  basecoat/         cloned basecoat repo (gitignored) — used to generate/regenerate the vendored CSS
  basecoat-source/  reference source extracted from basecoat: CSS components, JS behavior, MDX docs, style packs
  basecoat-vega.cdn.css     vendored, patched Vega style pack
  basecoat-lyra.cdn.min.css vendored, patched Lyra style pack (minified)
```

## Consuming a component

### From Scala.js / Laminar

```bash
# publish core locally first
sbt core/publishLocal

# scaffold a consumer project
cd packages/cli && npm install && npm run build
cd /path/to/your/project
node /path/to/shadcn-scalajs/packages/cli/dist/index.js init \
  --registry /path/to/shadcn-scalajs/modules/site/public/registry
node /path/to/shadcn-scalajs/packages/cli/dist/index.js add button dialog
```

Add to your `build.sbt`:
```scala
libraryDependencies ++= Seq(
  "dev.shadcn-scalajs" %%% "core" % "0.1.0",
  "com.raquo" %%% "laminar" % "17.2.1"
)
```

### From any other frontend stack

```html
<script type="module" src="sc-components.js"></script>
<sc-button variant="outline">Click me</sc-button>
```

Build `sc-components.js`/`.css` from `modules/webcomponents` (`sbt webcomponents/fastLinkJS`, output at `modules/webcomponents/target/scala-3.5.2/webcomponents-fastopt/main.js`) plus the CSS bundle in `vendor/basecoat-vega.cdn.css`, co-located as `sc-components.js`/`sc-components.css`.

## Component scope

The Laminar library now covers the full Basecoat component surface with direct
Scala.js primitives and registry entries. The original five components cover
the three architectural tiers Basecoat uses:

| Tier | Components | Mechanism |
|---|---|---|
| Pure CSS | `Button`, `Badge` and the stateless primitives | Tailwind utility classes and native HTML semantics |
| Native elements | `Dialog`, `Accordion` | `<dialog>` with `showModal()`/`close()`, `<details>`/`<summary>` |
| Reactive behavior | `DropdownMenu`, `Switch` | Laminar `Var` signals and event observers |

Every component follows the same distribution pattern: `.scala` in
`modules/ui`, a `.registry.json` sidecar, and (where a native custom element
is useful) an `Sc*` wrapper in `modules/webcomponents`.

## Things that will bite you

1. **Laminar tag-name collisions**: several HTML tags are exposed with a suffix — `sectionTag`, `detailsTag`, `summaryTag`, `dialogTag`, `menuTag`, `commandTag`. Bare `button`, `select`, `label`, `nav`, `a` work. `HtmlTag`, `DetachedRoot` need explicit imports.
2. **`children`/other DOM-property names collide inside `ScElementBase` subclasses**: `Sc*` classes extend `dom.HTMLElement` which has a native `children` member — build the Laminar tree in a companion-object function instead.
3. **basecoat's compiled CSS needs a one-line patch for Shadow DOM**: raw `:root { ... }` token block must become `:root, :host { ... }` or no colors render inside shadow roots. Already applied to vendored files — reapply if regenerating (see `vendor/NOTICE.md`).
4. **Shadow DOM retargets `ev.target`**: document-level "click outside to close" checks must use `ev.composedPath()`, not `ev.target`.
5. **`@scala-js/vite-plugin-scalajs`'s `cwd` option** is relative to the Vite project directory, not the repo root — `modules/site/vite.config.js` needs `cwd: "../.."`.
6. **Fetching `js.Promise` chains**: `.then[String](_.text())` needs the explicit type parameter — Scala's type inference doesn't always widen on its own.

## Development

```bash
sbt ~ui/fastLinkJS          # watch & rebuild ui
sbt ~webcomponents/fastLinkJS # watch & rebuild web components
sbt ~site/fastLinkJS         # watch & rebuild site
```

### Updating vendored basecoat CSS

```bash
cd vendor/basecoat
npm install && npx tailwindcss -i src/css/basecoat-vega.cdn.css -o ../basecoat-vega.cdn.css
# reapply Shadow DOM patch (see vendor/NOTICE.md):
sed -i '' 's/^:root {$/:root, :host {/' ../basecoat-vega.cdn.css
```

Full provenance and patch notes: `vendor/NOTICE.md`.
