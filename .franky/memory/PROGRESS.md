# Progress — source of truth for task state

## Done

- Repo bootstrap: sbt build (`core`/`ui`/`webcomponents`/`site`, no cross-project — no JVM server in this product).
- `modules/core`: `CommonAttrs` (openAttr), `Tags` (slot). DataAttrs removed during Tailwind migration.
- `modules/ui`: 5 v1 components — `Button`, `Badge`, `Dialog`, `Accordion`, `DropdownMenu` — each with a `.registry.json` sidecar. Originally built with basecoat CSS classes; fully migrated to Tailwind CSS v4 utilities matching the canonical shadcn/ui `new-york-v4` source.
- `modules/webcomponents`: `ScElementBase` (Shadow DOM + CSS injection + connectedCallback/disconnectedCallback lifecycle via Laminar's `renderDetached`) + `Sc*` wrapper for each component. Verified live in Chrome: all 5 render, style, and behave correctly. Updated to use reactive Tailwind classes instead of data-variant/data-size attributes.
- `modules/site`: Vite dev app with Tailwind CSS v4 — landing page (`Main.scala`) cloning basecoat's integration page + `plain-html-demo.html` + `scripts/build-registry.mjs`. PostCSS + `@tailwindcss/postcss` processes `src/styles/globals.css` with full `@theme inline` token mapping and `:root`/`.dark` blocks from the real shadcn/ui source.
- `packages/cli`: Node/TS + Commander, `init`/`add` commands. End-to-end verified with independent sbt project.
- Three real bugs found via live testing and fixed: dialog `close` event double-firing, dropdown-menu click-outside silently eating selections inside Shadow DOM (`composedPath()` fix), and basecoat CSS tokens not applying inside shadow roots (now moot after Tailwind migration).
- `AGENTS.md` updated with concrete architecture, commands, and gotchas.
- franky re-bootstrapped with real build/lint/test/verify scripts.
- Switched site typography to Inter variable font.
- **Basecoat → Tailwind CSS v4 migration (complete):**
  - Dropped all vendored basecoat CSS (`vendor/basecoat-*.css`, `public/*.css`)
  - Installed Tailwind CSS v4 (`tailwindcss`, `@tailwindcss/postcss`, `postcss`, `tw-animate-css`)
  - Created `modules/site/postcss.config.mjs` and `src/styles/globals.css` with full shadcn/ui design tokens
  - Deleted `modules/core/DataAttrs.scala` — data-variant/data-size no longer needed
  - Trimmed `modules/core/CommonAttrs.scala` to just `openAttr`
  - Rewrote all 5 UI components with direct Tailwind utilities matching shadcn/ui's `button.tsx`/`badge.tsx`/etc.
  - Rewrote all 5 `Sc*` web-component wrappers to use reactive Tailwind classes
  - Rewrote `Main.scala` (578 lines) with real Tailwind utilities — no more `tw-*` prefixed hand-written classes
  - Updated `index.html` to reference Tailwind-processed CSS via `index.js` import
  - Removed `vendor/basecoat/` from `.gitignore`
  - Full project compiles clean, Vite dev server serves Tailwind CSS at `http://localhost:4300/`
  - **Component Scala API unchanged** — `Button.of(...)`, `Dialog(isOpenVar)(...)`, etc. identical
  - Cloned full basecoat repo + extracted reference source into `vendor/basecoat-source/` (CSS, JS, MDX docs) for future component porting
- Bentogrid landing page: sticky nav, hero, 5-column responsive dashboard showcasing ~25 components (buttons, badges, inputs, textareas, radios, checkboxes, switch, button-group, dialog, sidebar nav, progress bars, chart bars, area chart, select, items, breadcrumbs, empty state, notification toggles, QR code SVG, dark mode toggle).
- Fixed the rewritten site `Main.scala` parser/API errors: restored dashboard grid nesting, corrected `Badge.of` modifier usage, and verified `site/compile`, scalafmt, and the Vite production build.
- Fixed the landing page layout regression caused by `main` being nested inside the header; made closed dialogs truly hidden, removed the dashboard fade clipping, and wired the style-pack selector to live radius tokens with Lyra as the default.
- Expanded style-pack switching beyond a radius-only toggle: each pack now has scoped card/control radius, shadow, border, and control-height tokens, with Lyra matching the square/no-shadow treatment from the cloned shadcn/ui source.
- Added the next Laminar primitive, `Card`, with `Card.header`, `title`, `description`, `action`, `content`, and `footer` helpers plus its registry sidecar.
- Added the foundational form primitives `Input`, `Textarea`, and `Label`, each with shadcn/ui Tailwind classes and a registry sidecar for CLI distribution.
- Ported the remaining Basecoat component surface into Laminar primitives: alert, avatar, breadcrumb, button group, checkbox, collapsible, combobox, command, chart, empty, field, form, input group, item, kbd, native select, popover, progress, radio, range, scrollbar, select, sidebar, skeleton, switch, table, tabs, toast, and tooltip.
- Added registry sidecars for the expanded component set and registered matching `sc-*` custom-element exports through `ScPrimitives`.
- Added a dedicated documentation-style `/components/:name` Laminar route. It has a persistent component index covering every registry item, per-component title/about/install/usage/examples sections, live previews rendered from the selected primitive, Scala.js code blocks, style-pack switching, dark mode, and a right-side table of contents.
- Updated README with full build commands, project layout, gotchas, and Tailwind setup.

## Next

- Port more of basecoat's ~33 remaining components following the established per-component pattern (`.scala` + `.registry.json` + `Sc*` wrapper), now using Tailwind utilities from shadcn/ui source. Reference at `vendor/basecoat-source/`.
- Accordion has no chevron/expand icon yet.
- Web Component CSS bundle (`sc-components.css`) still needs Tailwind build output wired — currently `ScElementBase` fetches the CSS from the same file.
- CLI has no package-rewriting — a real "own every line" experience needs import-rewriting or publishing `core` for real.
- `scripts/test` only checks that CLI-written files exist, not that they compile.

## Blockers

- (none)
