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
- franky re-bootstrapped after a franky update: real `scripts/{setup,build,lint,test,serve}` wired to sbt/npm (previously no-ops), `.scalafmt.conf` added and all Scala sources formatted once, `.franky/scripts.toml` updated to match, hooks installed (`franky hooks install --all`). `franky verify` passes all 4 steps for real (not just executable checks). Note: `franky init --refresh-adapters` overwrote the hand-written scripts with self-calling stubs (`./scripts/setup` invoking itself) despite docs saying root-level scripts "win" — had to restore them by hand afterward; re-check this if refresh-adapters runs again.
- Added `vendor/basecoat-lyra.cdn.min.css` (compiled + minified + `:root`→`:host`-patched) after `modules/site/index.html` was switched to reference it — same patch as Vega but on a one-line minified selector (`:root{--radius:` → `:root,:host{--radius:`), documented in `vendor/NOTICE.md`.

## Next

- Port more of basecoat's ~38 components following the established per-component pattern (`.scala` + `.registry.json` + `Sc*` wrapper).
- Accordion has no chevron/expand icon yet (basecoat's markup expects an inline `<svg>` in `<summary>`; not added).
- Only Vega (full) and Lyra (minified) style packs are vendored; basecoat ships 6 more. Dark mode (`.dark` class) is unpatched/untested for Shadow DOM.
- CLI has no package-rewriting (`ui`/`core` package names are fixed as `shadcnscalajs.*`) — a real "own every line" experience needs an import-rewriting transform like shadcn-svelte's, or publishing `core` for real instead of `publishLocal`.
- `scripts/test` only checks that CLI-written files exist, not that they compile (the full publishLocal+fixture-sbt-compile check is documented in AGENTS.md as a slower manual step, not run every `franky verify`).

## Blockers

- (none)
