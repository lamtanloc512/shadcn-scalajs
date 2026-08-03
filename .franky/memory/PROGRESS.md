# Progress — source of truth for task state

## Done

- Repo bootstrap: sbt build (`core`/`ui`/`webcomponents`/`site`, no cross-project — no JVM server in this product).
- `modules/core`: `DataAttrs` (data-variant/data-size + kebab-case), `CommonAttrs` (open/data-popover), `Tags` (slot).
- `modules/ui`: 5 v1 components — `Button`, `Badge` (pure CSS), `Dialog`, `Accordion` (native elements), `DropdownMenu` (hand-rolled Var/EventBus behavior) — each with a `.registry.json` sidecar.
- `modules/webcomponents`: `ScElementBase` (Shadow DOM + CSS injection + connectedCallback/disconnectedCallback lifecycle via Laminar's `renderDetached`) + `Sc*` wrapper for each component. Verified live in Chrome (claude-in-chrome): all 5 render, style, and behave correctly, including interactive flows (dialog open/close, accordion single-open, dropdown select).
- `modules/site`: Vite dev app — native-Laminar demo (`Main.scala`) + `plain-html-demo.html` (zero Scala.js, pure Web Component usage) + `scripts/build-registry.mjs` (walks `ui/*.registry.json`, emits `public/registry/{index,<name>}.json`).
- `packages/cli`: Node/TS + Commander, `init`/`add` commands. End-to-end verified: published `core` locally (`sbt core/publishLocal`), scaffolded an independent throwaway sbt project, ran `add` against it, ran `sbt compile` there — succeeded.
- `vendor/basecoat-vega.cdn.css`: compiled from basecoat's own Tailwind build, patched (`:root` → `:root, :host`) so tokens resolve inside Shadow DOM — see `vendor/NOTICE.md`.
- Three real bugs found via live testing and fixed: dialog `close` event double-firing (attribute↔Var feedback loop), dropdown-menu click-outside-closes silently eating item selection inside Shadow DOM (`ev.target` retargeting — fixed via `composedPath()`), and the CSS-not-applying-at-all bug above.
- `AGENTS.md`/`specs/PROJECT.md` updated with concrete architecture, commands, and gotchas for future agents.

## Next

- Port more of basecoat's ~38 components following the established per-component pattern (`.scala` + `.registry.json` + `Sc*` wrapper).
- Accordion has no chevron/expand icon yet (basecoat's markup expects an inline `<svg>` in `<summary>`; not added).
- Only the "Vega" style pack is vendored; basecoat ships 7 others. Dark mode (`.dark` class) is unpatched/untested for Shadow DOM.
- CLI has no package-rewriting (`ui`/`core` package names are fixed as `shadcnscalajs.*`) — a real "own every line" experience needs an import-rewriting transform like shadcn-svelte's, or publishing `core` for real instead of `publishLocal`.
- `franky`'s `scripts/lint`/`scripts/test` are still no-ops — not wired to sbt/npm yet.

## Blockers

- (none)
