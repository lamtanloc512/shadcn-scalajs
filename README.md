# shadcn-scalajs

A port of [shadcn/ui](https://ui.shadcn.com)'s philosophy to [Scala.js](https://www.scala-js.org/) + [Laminar](https://laminar.dev): components you copy into your own project and own, styled with [basecoat](https://github.com/hunvreus/basecoat)'s compiled CSS (so existing shadcn/TweakCN themes work unmodified), and every component also compiles to a standalone Web Component so any frontend stack — not just Scala.js — can use it.

## Layout

```
modules/
  core/           design tokens, data-attribute helpers shared by every component
  ui/             the Laminar component source of truth — what the CLI copies into consumer projects
  webcomponents/  Sc* custom-element wrappers around modules/ui, for non-Scala consumers
  site/           demo app (native Laminar usage) + plain-html-demo.html (Web Component usage) + registry build
packages/
  cli/            `shadcn-scalajs init` / `add <component>` — Node/TS, mirrors shadcn-svelte's CLI
vendor/           vendored, Tailwind-compiled basecoat CSS (see vendor/NOTICE.md)
```

## Using a component from Scala.js/Laminar

```
cd packages/cli && npm install && npm run build
cd /path/to/your/project
node /path/to/shadcn-scalajs/packages/cli/dist/index.js init --registry /path/to/shadcn-scalajs/modules/site/public/registry
node /path/to/shadcn-scalajs/packages/cli/dist/index.js add button dialog
```

Add `"dev.shadcn-scalajs" %%% "core" % "0.1.0"` (`sbt core/publishLocal` in this repo first) and `"com.raquo" %%% "laminar" % "17.2.1"` to your `build.sbt`.

## Using a component from any other frontend stack

```html
<script type="module" src="sc-components.js"></script>
<sc-button variant="outline">Click me</sc-button>
```

Build `sc-components.js`/`.css` from `modules/webcomponents` (`sbt webcomponents/fastLinkJS`, output at `modules/webcomponents/target/scala-3.5.2/webcomponents-fastopt/main.js`) plus the CSS bundle in `vendor/basecoat-vega.cdn.css`, co-located as `sc-components.js`/`sc-components.css`. See `modules/site/public/plain-html-demo.html` for a working example.

## Dev loop for the demo site

```
cd modules/site && npm install && npm run dev
```

Then visit `http://localhost:4300/` (native Laminar demo) or `http://localhost:4300/plain-html-demo.html` (Web Component demo).

## v1 component scope

Five components, chosen to cover the three architectural tiers basecoat itself uses: `Button`/`Badge` (pure CSS), `Dialog`/`Accordion` (native `<dialog>`/`<details>`, no custom JS), `DropdownMenu` (hand-rolled Airstream `Var`/`EventBus` behavior, since there's no Radix-equivalent primitives library for Laminar). Porting the remaining basecoat components follows the same pattern established by these five.

See `/Users/locgorilla/.claude/plans/let-create-for-effervescent-penguin.md` for the full design rationale, deviations found while implementing, and known follow-ups.
