# shadcn-scalajs Web Components

An experimental browser ESM bundle of the currently registered `sc-*` elements. It includes Tailwind styles and the Nova style pack. Install the site build dependencies with `cd apps/web && npm install`, then run `cd packages/wc-bundle && npm run build` from the repository root. `prepublishOnly` runs the same build. This directory is packaging for a future release; it has not been published to npm.

```html
<html data-style-pack="nova">
  <head>
    <style>
      :root { --primary: #b91c1c; --primary-foreground: white; --radius: 0.75rem; }
    </style>
  </head>
  <body>
    <sc-button>Save</sc-button>
    <sc-button style="--primary: #1d4ed8">Other color</sc-button>
    <script type="module" src="/vendor/sc-components/index.js"></script>
  </body>
</html>
```

Copy the complete `dist/` directory to `/vendor/sc-components/` for this example. The entry module sets `data-sc-assets-base` and `data-sc-pack-base` on `<html>` from its own URL before loading the components. It leaves existing attributes unchanged. The base CSS sits beside `index.js`; pack CSS lives at `styles/pack-nova.css`.

Theme tokens such as `--background`, `--foreground`, `--primary`, and `--radius` are CSS custom properties. Declare them on `:root` in CSS, put inline overrides on an individual `sc-*` host, or choose a bundled palette using `data-base-color`, `data-theme-color`, and the `dark` class. The entry ignores the project's persisted site theme. Nova is baked in and works without an explicit `data-style-pack`. Isolated mode ships only the Nova pack. Other pack names require a matching `styles/pack-<name>.css` at the pack base URL and are not supported by this package's document stylesheet. You can override either base URL with `data-sc-assets-base` or `data-sc-pack-base` before importing the entry module. For runtime changes use `ShadcnScalaJS.setTokens({ primary: "#..." })`, which refreshes existing shadow roots.

The entry marks the document `data-sc-isolated`, preloads `sc-document.css`, and waits for that stylesheet before defining elements. The document sheet keeps public theme token defaults in `@layer defaults` and confines light-DOM styling to custom-element trees (including slotted markup) using `@scope`. It does not apply a document-wide reset or Tailwind utilities. The full `sc-components.css` remains available for shadow roots and explicit opt-out: omit `data-sc-isolated` when loading `sc-components.js` directly if you need the original document styling. Existing asset and pack base attributes remain unchanged. For a bundle compiled from edited Scala components, use the CLI `build-wc` path instead.
