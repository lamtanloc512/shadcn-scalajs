---
title: Build Web Components
description: Compile the Scala components copied into your project into standalone JavaScript custom elements.
---

There are two routes. The default bundle in `packages/wc-bundle` runs without Scala tooling on the consuming page. The `build-wc` command instead compiles the UI Scala source copied into your project, including local changes. That command currently needs a project created with `init` scaffolding. An `init --no-scaffold` project needs its own sbt `webcomponents` project and build script.

## Default bundle and variables

From this repository, run `cd packages/wc-bundle && npm run build`. Copy its complete `dist/` folder to your server, for example `/vendor/sc-components/`. Import its `index.js` in an HTML page:

```html
<html data-style-pack="nova">
  <head>
    <style>:root { --primary: #b91c1c; --primary-foreground: white; }</style>
  </head>
  <body>
    <sc-button>Save</sc-button>
    <script type="module" src="/vendor/sc-components/index.js"></script>
  </body>
</html>
```

The entry locates its CSS and pack file relative to itself. CSS variables on `:root` control the page theme; inline variables on an individual `sc-*` tag override them for that element. `ShadcnScalaJS.setTokens({ primary: "#..." })` updates existing elements at runtime.

This is a **preview bundle**. All 63 catalog examples render in the browser QA, but that does not establish complete behavioral parity or compatibility with every host stylesheet. Its loader installs a document sheet scoped to Web Component light-DOM trees (including slotted content); full Tailwind CSS remains inside shadow roots. The scoped sheet uses CSS `@scope`, so check target browser support and host styles. Only Nova is packaged; selecting another pack requires a source build. Compound controls use JSON attributes or named slots rather than exposing every Laminar composition API. Try their editable examples in the app's `/web-components` playground: modal triggers use `slot="trigger"`, Carousel slides use `slide-0`, `slide-1`, and so on, and Sonner exposes `toast()`/`success()` on a single mounted `<sc-sonner>`. Drawer does not provide swipe gestures or snap points; Carousel does not include Embla plugins. Package details are in [`packages/wc-bundle/README.md`](https://github.com/lamtanloc512/shadcn-scalajs/blob/main/packages/wc-bundle/README.md).

## Build from your own Scala source

```bash
npx shadcn-scalajs@latest add button badge
npm install
npx shadcn-scalajs@latest build-wc button badge
```

`build-wc` copies the available wrappers into `packages/webcomponents/src/main/scala/shadcnscalajs/webcomponents`, then builds the selected components. Omit the names to include all installed components that have independent wrappers. Unsupported components are rejected by name; the current wrappers do not cover the whole UI catalog. Your copies of the wrapper source are not overwritten when you run the command again.

The output is:

- `packages/ui/public/sc-components-loader.js` (entry point)
- `packages/ui/public/sc-components.js` (compiled Scala.js)
- `packages/ui/public/sc-components.css` (shadow roots)
- `packages/ui/public/sc-document.css` (scoped component light DOM)
- `packages/ui/public/styles/pack-<style>.css`

Include the script on a plain HTML page. Set the asset locations *before* loading the module:

```html
<html data-style-pack="nova" data-sc-assets-base="/" data-sc-pack-base="/styles">
  <body>
    <sc-button variant="primary">Save</sc-button>
    <script type="module" src="/sc-components-loader.js"></script>
  </body>
</html>
```

The loader waits for the scoped `sc-document.css` before registering elements. The compiled module fetches the full `sc-components.css` for shadow roots; it does not inject that sheet into the document in isolated mode. Serve all generated assets together. The loader infers asset URLs from its own URL; `data-sc-assets-base` and `data-sc-pack-base` can override them. A direct import of `sc-components.js` without the loader opts out of isolation and installs the full sheet in the document.

After changing a copied Scala UI component, wrapper, or style pack, rebuild the assets:

```bash
npm run build:webcomponents
npm run build
```

`npm run build` copies `packages/ui/public` to the Vite output, but does not recompile Web Components by itself. The Web Component build compiles the copied Scala UI sources again. Both routes run compiled Scala.js in the browser; a plain HTML consumer does not need a Scala build tool.
