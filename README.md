# shadcn-scalajs

A port of [shadcn/ui](https://ui.shadcn.com)'s philosophy to [Scala.js](https://www.scala-js.org/) + [Laminar](https://laminar.dev): components you copy into your own project and own, styled with Tailwind CSS v4 utilities matching shadcn/ui's canonical `new-york-v4` source exactly, and every component also compiles to a standalone Web Component so any frontend stack — not just Scala.js — can use it. Covers the full shadcn/ui catalog (~60 components; the newest AI-chat-specific additions and non-component doc pages excluded), documented in a basecoat-style docs site with live previews rendered by the real Laminar components, not static screenshots.

## Quick start

```bash
# ensure sbt is on PATH (Coursier-installed):
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"

# build everything
sbt compile

# Scala.js link — fast (dev) vs optimized (production size)
sbt ui/fastLinkJS webcomponents/fastLinkJS site/fastLinkJS
sbt siteOpt   # or: sbt opt — fullLinkJS minify + FewestModules

# publish core to local Ivy (needed by consumer projects)
sbt core/publishLocal

# run the demo/docs site — predev generates Tailwind CSS + the registry first
cd modules/site && npm install && npm run dev
# → http://localhost:4300/                     landing page
# → http://localhost:4300/components           components index
# → http://localhost:4300/components/<name>    per-component docs + live preview
# → http://localhost:4300/web-components       Web Component docs + live preview
# → http://localhost:4300/plain-html-demo.html  Standalone Web Component mosaic (zero Scala.js on the page)

# production bundle (runs site/fullLinkJS via the Vite plugin, then minifies)
cd modules/site && npm run build
# → modules/site/dist/

# build the CLI
cd packages/cli && npm install && npm run build

# regenerate registry JSON from modules/ui/*.registry.json (also runs as part of predev/prebuild)
cd modules/site && node scripts/build-registry.mjs
```

## Project layout

```
modules/
  core/             CommonAttrs (openAttr), Tags (slot) — styling is Tailwind classes on the components themselves
  ui/               Laminar component source of truth — one .scala + one .registry.json per component
  webcomponents/    ScElementBase + Sc*/ScPrimitives custom-element wrappers for non-Scala consumers
  site/             Vite + Tailwind v4 + PostCSS: Main.scala (landing + /components/:name docs) + plain-html-demo.html + generator scripts
packages/
  cli/              Node/TS + Commander: `init` writes shadcn-scalajs.json, `add <names>` resolves deps and writes files
vendor/
  basecoat/         cloned basecoat repo (gitignored) — source of the basecoat-source/ extraction
  basecoat-source/  reference CSS/JS/docs extracted from basecoat, consumed by scripts/build-basecoat-styles.mjs
  shadcn-source/    real shadcn/ui v4 theme presets, consumed by scripts/build-shadcn-presets.mjs
```

No CSS is vendored pre-compiled anymore — `modules/site`'s `predev`/`prebuild` scripts generate `basecoat.generated.css`/`shadcn-presets.generated.css` from the `vendor/` snapshots on every run. See `vendor/NOTICE.md` for the full picture, including a latent Shadow-DOM bug flagged for whoever wires up the Web Component CSS bundle next.

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

`modules/webcomponents` wraps most (not yet all) of the components in `Sc*`/`ScPrimitives` custom-element classes — `modules/ui/CLAUDE.md` keeps the current wrapper gap list. The bundle itself is built by `modules/site/scripts/build-webcomponents.mjs` (runs in `predev`/`prebuild`): it links `webcomponents/fullLinkJS`, esbuilds it into `public/sc-components.js`, and emits `public/sc-components.css` (Tailwind output rewritten so `:root` tokens also apply to `:host`, plus one baked style pack).

## Component scope

All ~60 components cover three architectural tiers (matching the split basecoat itself uses):

| Tier | Examples | Mechanism |
|---|---|---|
| Pure CSS | Button, Badge, Card, Separator, AspectRatio | Tailwind utility classes and native HTML semantics |
| Native elements | Dialog, Sheet, Accordion, Collapsible, Popover, Combobox | `<dialog>` with `showModal()`/`close()`, `<details>`/`<summary>` |
| Reactive behavior | DropdownMenu, ContextMenu, Menubar, Calendar, InputOTP, Resizable | Laminar `Var`/`EventBus` — no Radix-equivalent primitives library exists for Laminar |

Every component follows the same distribution pattern: `.scala` in `modules/ui`, a `.registry.json` sidecar, a doc page under `/components/:name` in `modules/site`, and (where built) an `Sc*`/`ScPrimitives` wrapper in `modules/webcomponents`.

## Things that will bite you

See `AGENTS.md`'s "Things that will bite you if you don't know them" section for the full, current list (Laminar tag-name suffixes, `ScElementBase` `children` shadowing, `composedPath()` for Shadow-DOM click-outside checks, `js.Date` `Int`/`Double` mismatches, and more) — kept there rather than duplicated here since it's the file most likely to be read first by an agent picking up this repo.

## Development

```bash
sbt ~ui/fastLinkJS            # watch & rebuild ui
sbt ~webcomponents/fastLinkJS # watch & rebuild web components
sbt ~site/fastLinkJS          # watch & rebuild site
sbt siteOpt                   # size-optimized site link (fullLinkJS)
sbt scalafmtAll                # format before committing — scripts/lint checks this
cd modules/site && npm run build   # fullLinkJS + Vite/esbuild minify → dist/
```

Scala.js linker notes: `fastLinkJS` uses `SmallModulesFor` for Vite HMR; `fullLinkJS` uses `FewestModules` + Scala.js minify + `avoidClasses=false` for smaller output (Vite finishes with esbuild). Source maps from the linker are off — Vite cannot resolve Scala.js absolute `file:`/`https:` map URIs and would warn about missing sources.

Full architecture notes, gotchas, and the new-component checklist: `AGENTS.md`.
