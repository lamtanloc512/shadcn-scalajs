# Vendored source

This repo no longer vendors pre-compiled, patched CSS files (an earlier version of this
notice described a `basecoat-vega.cdn.css` / `basecoat-lyra.cdn.min.css` patch workflow —
that's gone, superseded by the Tailwind CSS v4 migration; the git history around the
`Tailwind v4 migration` commits is the record of it). What's here
now is reference *source*, consumed by build-time generator scripts rather than shipped
directly:

- **`vendor/basecoat/`** — a full clone of the upstream [basecoat](https://github.com/hunvreus/basecoat)
  git repo (MIT License, copyright Ronan Berder), kept as a raw mirror. Not read by any
  build script directly; it's the thing `basecoat-source/` was extracted from, and where
  you'd `git pull` to refresh that extraction.
- **`vendor/basecoat-source/`** — curated extraction from `basecoat/`: `components/*.css`
  (structural CSS per component, no color), `styles/*.css` (the 8 style packs — lyra,
  vega, nova, maia, mira, luma, sera, rhea), `js/*.js`, `docs/`. Consumed by
  `modules/site/scripts/build-basecoat-styles.mjs`, which extracts each file's
  `@layer components { ... }` block and writes
  `modules/site/src/styles/basecoat.generated.css` (gitignored, rebuilt on every
  `npm run dev`/`build` via the `predev`/`prebuild` script). Regenerate the extraction
  after pulling a newer basecoat with `npm run build:basecoat-styles` in `modules/site`.
- **`vendor/shadcn-source/styles/`** — the real shadcn/ui v4 theme presets
  (`style-<pack>.css`, one per style pack). Consumed by
  `modules/site/scripts/build-shadcn-presets.mjs`, which rewrites each file's `.style-X`
  selectors to `[data-style-pack="X"]` and writes
  `modules/site/src/styles/shadcn-presets.generated.css` (also gitignored/regenerated).

Design tokens and per-component Tailwind utility classes live directly in
`modules/site/src/styles/globals.css` (hand-written `@theme inline` token map +
`:root`/`.dark` blocks copied from shadcn/ui's actual output) and in each component's own
`.scala` file in `modules/ui` (Tailwind utility strings matching the canonical
`button.tsx`/`badge.tsx`/etc. source) — not generated from the vendor snapshots above.

## Shadow DOM tokens: `globals.css`'s `:root` won't reach a Shadow DOM

`globals.css:66` defines the real design-token values under a bare `:root { ... }` rule.
`:root` only ever matches the top-level document's `<html>` element — even from inside a
stylesheet loaded in a Shadow Root, only `:host` matches the shadow host. This bit the
project once already in the basecoat-CSS era (see the earlier git history), and it is the
reason `modules/site/scripts/build-webcomponents.mjs` runs its `sc-shadow-scope` PostCSS
step over `sc-components.css`: every `:root` rule is duplicated onto `:host`, and the
baked-pack selector gets a shadow-theme-host fallback, so the tokens resolve inside each
`Sc*` component's shadow root. Without that rewrite every Web Component renders
structurally correct but completely uncolored — no error, just
`getComputedStyle(...).backgroundColor` silently returning transparent. Any new build step
that emits CSS for shadow roots must apply the same rewrite.
