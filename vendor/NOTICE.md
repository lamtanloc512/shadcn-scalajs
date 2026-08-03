# Vendored CSS

`basecoat-vega.cdn.css` is the Tailwind-compiled, dependency-free "Vega" style
bundle from [basecoat](https://github.com/hunvreus/basecoat) (MIT License,
copyright Ronan Berder), built from that project's
`src/css/basecoat-vega.cdn.css` source via its own `npx tailwindcss` build
step (see `basecoat/scripts/build.js`). It supplies shadcn-scalajs's CSS
custom-property tokens (`--background`, `--primary`, `--ring`, etc.) and all
component visual styling (`.btn`, `.badge`, `.dialog`, `.accordion`,
`.dropdown-menu`, ...) — shadcn-scalajs's Scala/Laminar components emit
markup and `data-variant`/`data-size` attributes that this stylesheet
targets; it does not define its own CSS.

Regenerate after upgrading the basecoat submodule/version with:

```
cd basecoat && npm install && npx tailwindcss -i src/css/basecoat-vega.cdn.css -o ../shadcn-scalajs/vendor/basecoat-vega.cdn.css
```

**Required patch — `:root` → `:root, :host`:** basecoat's compiled output defines
its real token *values* (`--background: oklch(1 0 0)`, etc.) under a bare
`:root { ... }` rule. `:root` only ever matches the top-level document's
`<html>` element, even when the stylesheet is loaded inside a Shadow Root —
unlike `:host`, which matches the shadow host from within its own tree. Since
every `Sc*` web-component wrapper injects this CSS into its own shadow root
(see `ScElementBase`), the bare `:root` rule never applies there, so every
component renders structurally correct but with no colors/tokens at all
(discovered via live browser testing, not visible from reading the CSS or
compiling Scala — computed styles showed `background-color: rgba(0,0,0,0)`
instead of the expected token value). The `@theme` bridge block basecoat
generates (`--color-background: var(--background)`, etc.) already correctly
uses `:root, :host { ... }` — only the one raw-token block needs the same
treatment. Regenerating from a newer basecoat version must reapply this
one-line patch:

```
sed -i '' 's/^:root {$/:root, :host {/' vendor/basecoat-vega.cdn.css
```

(Native Laminar/light-DOM usage in `modules/site` is unaffected either way,
since `:root` already matches there.)
