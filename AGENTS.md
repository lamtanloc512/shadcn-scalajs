# shadcn-scalajs

<!-- franky:base @include .franky/base/instructions.md -->

## Agent skill

Cursor loads `.cursor/skills/franky/SKILL.md` automatically — it contains the full `franky help` instructions. Run `franky help` to refresh from CLI.

## Project rules

Add project-specific agent instructions below. Franky base rules above always apply.

### What this is

A port of shadcn/ui's philosophy to Scala.js + Laminar: components you copy into your own project (CLI + registry, like real shadcn/ui — not just a published library), styled with Tailwind CSS v4 utilities matching shadcn/ui's canonical `new-york-v4` source exactly (not basecoat CSS — see "History" below), and every component also compiles to a standalone Web Component so non-Scala frontends can use it too. Read `.franky/memory/PROGRESS.md` for what's currently in progress and what's next.

### History

v1 (5 components: Button, Badge, Dialog, Accordion, DropdownMenu) shipped styled with vendored, patched basecoat CSS loaded into each Web Component's Shadow Root. The project was then migrated wholesale to Tailwind CSS v4: components now carry shadcn/ui's own Tailwind utility classes directly (see any `modules/ui/*.scala` file — e.g. `Button.scala`'s `variantClasses`/`sizeClasses` maps are copied straight from `button.tsx`), and `modules/site` runs a real Tailwind v4 + PostCSS pipeline instead of linking a static CSS file. `vendor/basecoat-*.cdn.css` no longer exist — see `vendor/NOTICE.md` for exactly what's vendored now and why.

### Status

Component implementation status and the tier breakdown (pure Tailwind / native-element / hand-rolled state machine) now live in `modules/ui/CLAUDE.md` — read it before touching `modules/ui` or `modules/webcomponents`.

### Layout

```
modules/core/           CommonAttrs (openAttr), Tags (slot) — DataAttrs was deleted in the Tailwind migration, styling is Tailwind classes now, not data-variant/data-size attrs
modules/ui/             Laminar component source of truth — what the CLI copies into consumer projects; one .scala + one .registry.json per component
modules/blocks/         Multi-file page/section compositions built from modules/ui (login-01, signup-01, otp-01, calendar-01) — one package-legal dir per block (`login01/`) plus a `<name>.registry.json` sidecar whose `name` keeps the hyphen (`login-01`). Laminar has no file-based routing, so a block's "page" file is a mountable `def apply(): HtmlElement`, not a route.
modules/webcomponents/  ScElementBase + Sc*/ScPrimitives custom-element wrappers around modules/ui, for non-Scala consumers (sc-components.css bundle not yet wired — see PROGRESS.md)
modules/site/           Vite + Tailwind v4 + PostCSS dev app: Main.scala (landing page + /components/:name docs route) + plain-html-demo.html + scripts/{build-basecoat-styles,build-shadcn-presets,build-registry}.mjs
packages/cli/           Node/TS + Commander: `init` writes shadcn-scalajs.json, `add <names...>` resolves registryDependencies and writes files
vendor/                 reference source (basecoat + shadcn/ui snapshots) consumed by modules/site's generator scripts — see vendor/NOTICE.md, it is NOT pre-compiled CSS anymore
```

### Build/dev commands

```bash
# add coursier-installed sbt to PATH if `sbt` isn't found:
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"

sbt core/compile ui/compile webcomponents/compile site/compile   # compile everything
sbt ui/fastLinkJS webcomponents/fastLinkJS site/fastLinkJS       # Scala.js link (dev / per module)
sbt siteOpt                                                      # size-optimized site/fullLinkJS (or `sbt opt` for ui+wc+site)
sbt scalafmtAll                                                  # format — CI (franky verify / scripts/lint) checks this, run it before committing
sbt core/publishLocal                                            # publish core to ~/.ivy2/local (needed for consumer fixtures / real CLI testing)

cd modules/site && npm install && npm run dev   # predev runs build-basecoat-styles + build-shadcn-presets + build-registry, then Vite
cd modules/site && npm run build                # production: Vite plugin runs site/fullLinkJS, then esbuild minify → dist/
# → http://localhost:4300/                    native Laminar landing page
# → http://localhost:4300/components          components index (componentsGalleryPage)
# → http://localhost:4300/components/<name>   per-component docs + live preview
# → http://localhost:4300/plain-html-demo.html  Web Component demo (zero Scala.js on the page)
cd modules/site && node scripts/build-registry.mjs   # regenerate public/registry/*.json from modules/ui (also runs as part of predev/prebuild)

cd packages/cli && npm install && npm run build  # -> dist/index.js
node packages/cli/dist/index.js init --registry <path-or-url> --source-dir <path>
node packages/cli/dist/index.js add <component...>

./scripts/test   # franky's ground-truth check: build + registry rebuild + CLI init/add smoke test against a temp dir
```

### Things that will bite you if you don't know them

1. **Laminar tag-name collisions**: several HTML tags are exposed with a `Tag` suffix, not their bare name — `sectionTag`, `detailsTag`, `summaryTag`, `dialogTag`, `menuTag`, `commandTag`, `headerTag`, `footerTag`, `navTag`, `articleTag`, `asideTag`, `mainTag`, `timeTag`, `progressTag`. Bare `div`, `span`, `button`, `ul`, `li`, `ol`, `hr`, `figure`, `label`, `select`, `option`, `table`/`thead`/`tbody`/`tr`/`td`/`th` all work fine. `HtmlTag`, `DetachedRoot` also need explicit imports (`com.raquo.laminar.tags.HtmlTag`, `com.raquo.laminar.nodes.DetachedRoot`) — not re-exported by the `L.*` wildcard import. When in doubt, `grep` the actual name out of the `laminar` sources jar (`cs fetch --intransitive com.raquo:laminar_sjs1_3:17.2.1 --classifier sources`) rather than guessing — this list has been wrong before.
2. **`children`/other DOM-property names collide inside `ScElementBase` subclasses**: since `Sc*` classes extend `dom.HTMLElement`, which itself has a native `children: HTMLCollection` member, writing `children <-- signal` directly inside such a class resolves to the wrong thing. Build the Laminar tree in a companion-object function instead (see `ScAccordion`/`ScDropdownMenu` for the pattern) and pass in whatever `Var`s it needs.
3. **Shadow DOM retargets `ev.target`**: any document-level "click outside to close" check must use `ev.composedPath()`, not `ev.target` — see `DropdownMenu.scala`'s `compPath` helper (also duplicated in `ContextMenu.scala`) and its doc comment for the exact failure mode this avoids (item selection silently eating clicks). `composedPath` isn't typed in the pinned scalajs-dom facade — cast through `js.Dynamic`.
4. **`globals.css`'s `:root` token block won't reach a Shadow DOM** — same class of bug as the old basecoat-CSS era, currently latent since `sc-components.css` isn't wired up yet. See `vendor/NOTICE.md`'s "Known latent issue" section before wiring that up.
5. **`@scala-js/vite-plugin-scalajs`'s `cwd` option** is relative to the Vite project's own directory, not the repo root — `modules/site/vite.config.js` needs `cwd: "../.."` (two levels up), not `".."`.
5b. **Scala.js sourcemaps + Vite**: linker maps use absolute `file:` / `https:` URIs; Vite wrongly resolves them under `*-fastopt/` and prints "Sourcemap ... points to missing source files". Linker source maps are disabled in `build.sbt` (`withSourceMap(false)`). Do not re-enable without a Vite-compatible map strategy.
6. **Fetching `js.Promise` chains**: `.`then`[String](_.text())` needs the explicit type parameter on the first `.then` — Scala's type inference doesn't always widen `js.Promise[String]` to the expected `B | Thenable[B]` on its own (see `webcomponents/Main.scala`).
7. **`js.Date` getters return `Double`, the constructor wants `Int`**: `new js.Date(d.getFullYear(), d.getMonth(), day)` fails to compile — `.toInt` both getter calls first (see `Calendar.scala`). No java.time dependency is in this build; date logic is hand-rolled on `js.Date`.
8. **A method/value named the same as one of Laminar's own keys shadows it inside that scope** — e.g. never name a `Var[String]` parameter `value`, since `value` is also Laminar's `<input>` value prop; `InputOTP.scala` uses `codeVar` for exactly this reason. Same caution applies to `children`, `content`, `label`, etc. if you're inside a scope that also needs the Laminar key of the same name.
9. **Right-click (`contextmenu`) interactions are hard to verify via claude-in-chrome's browser automation** — its right-click simulation appears to trigger Chrome's native context menu directly rather than dispatching a page-level `contextmenu` DOM event, so `ContextMenu.scala` could not be end-to-end browser-verified the way every other component was; it was reviewed against the already-proven `DropdownMenu` pattern instead. Worth a manual check if you touch it.

### Verification checklist for new work

- `sbt <module>/compile` for anything touched, then `sbt scalafmtAll` before committing.
- For `ui`/`webcomponents`/`site` changes: actually load a page in a browser (claude-in-chrome or manual) and click through the interaction, not just eyeball it — several real bugs in this codebase were invisible from source review or compilation alone (the composedPath bug, the double-fire dialog-close bug, the CSS-not-applying-in-shadow-DOM bug — all found via live testing, see git history / `.franky/memory/decisions.log`).
- New component checklist: `.scala` in `modules/ui` (Tailwind classes matching the real shadcn/ui source) → `.registry.json` sidecar → add the display name to `componentNavList` in `modules/site/Main.scala` → add a `liveExample()` case and a matching `usageSource` case (keep these two matches in exact 1:1 correspondence — the Usage code block shown is only accurate if it matches what actually rendered) → `node scripts/build-registry.mjs` (or just let `predev`/`prebuild` do it).
- New block checklist: directory + `.scala` files + `<name>.registry.json` sidecar under `modules/blocks` (sidecar needs `type: "scala:block"`, `description`, `categories`, and per-file `type` of `scala:page`/`scala:component`) → add a `Blocks.Meta` entry to `Blocks.all` **and** a case to `Blocks.render` in `modules/site/Blocks.scala` (both, or the block is unreachable) → `node scripts/build-registry.mjs` → browser-check all three routes (`/blocks`, `/blocks/<name>`, `/blocks/<name>/preview`).
- **`Button(...)` with no variant/size is unstyled** — unlike upstream's cva, `Button.apply` has no `defaultVariants`, so a bare `Button("Save")` renders at 20px tall with no background. Always `Button.of(_.variant(...), _.size(...), ...)`. This bit the block ports; see PROGRESS.md "Next".
- For `cli` changes: run `init`+`add` against a scratch directory and, ideally, `sbt compile` the result against a `core/publishLocal`'d build (`./scripts/test` does the smoke-test part of this automatically; the full sbt-compile check is still manual — see `scripts/test`'s own comment).

## Slash commands

Use deterministic commands from `.franky/commands.toml` — run `franky commands` to list them.

**Start every session with `franky help` and follow its instructions.**

| Command | Action |
|---|---|
| help | `franky help` — read first |
| verify | `franky verify` — run before completing work |
| test | `scripts/test` |
| doctor | `franky doctor` |

Full reference: franky `docs/slash-commands.md`.

## Scripts

```bash
franky verify          # setup → build → lint → test
scripts/test           # ground truth test step only
```

## Specs

Feature specs live in `specs/features/*.spec.md`. Read the active spec before implementing.

## Memory

Update `.franky/memory/PROGRESS.md` (done / next / blockers) as you work.