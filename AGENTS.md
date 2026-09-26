# shadcn-scalajs

## Project rules

### What this is

A port of shadcn/ui's philosophy to Scala.js + Laminar: components you copy into your own project (CLI + registry, like real shadcn/ui — not just a published library), styled with Tailwind CSS v4 utilities matching shadcn/ui's canonical `new-york-v4` source exactly (not basecoat CSS — see "History" below), and every component also compiles to a standalone Web Component so non-Scala frontends can use it too.

### History

v1 (5 components: Button, Badge, Dialog, Accordion, DropdownMenu) shipped styled with vendored, patched basecoat CSS loaded into each Web Component's Shadow Root. The project was then migrated wholesale to Tailwind CSS v4: components now carry shadcn/ui's own Tailwind utility classes directly (see any `packages/ui/*.scala` file — e.g. `Button.scala`'s `variantClasses`/`sizeClasses` maps are copied straight from `button.tsx`), and `apps/web` runs a real Tailwind v4 + PostCSS pipeline instead of linking a static CSS file. `vendor/basecoat-*.cdn.css` no longer exist — see `vendor/NOTICE.md` for exactly what's vendored now and why.

### Status

Component implementation status and the tier breakdown (pure Tailwind / native-element / hand-rolled state machine) now live in `packages/ui/CLAUDE.md` — read it before touching `packages/ui` or `packages/webcomponents`.

### Layout

```
apps/web/                 Vite site and registry host. sbt project id is still `site` (apps/web -> repo root is two levels, so vite `cwd` stays `"../.."`).
apps/docs/                Written guide (Astro Starlight). Not the component gallery.
packages/cli/             Published npm package: `init`, `add`, `mcp`.
packages/core/            CommonAttrs (openAttr), Tags (slot). Copied with components.
packages/ui/              Laminar component source of truth — what the CLI copies; one .scala + one .registry.json per component.
packages/blocks/          Page/section compositions built from packages/ui. Package-legal dir (`login01/`) plus a hyphenated `<name>.registry.json` sidecar.
packages/theme/           Style-pack CSS and tokens installed by `add`.
packages/webcomponents/   Experimental custom-element wrappers. Bundle built by apps/web/scripts/build-webcomponents.mjs.
vendor/                   shadcn/ui style-pack snapshots consumed by build-style-packs.mjs — see vendor/NOTICE.md.
docs/                     Contributor design notes. Not the published guide.
```

### Build/dev commands

```bash
# add coursier-installed sbt to PATH if `sbt` isn't found:
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"

sbt core/compile ui/compile webcomponents/compile site/compile   # compile everything
sbt ui/fastLinkJS webcomponents/fastLinkJS site/fastLinkJS       # Scala.js link (dev / per module)
sbt siteOpt                                                      # size-optimized site/fullLinkJS (or `sbt opt` for ui+wc+site)
 sbt scalafmtAll                                                  # format before committing
sbt core/publishLocal                                            # publish core to ~/.ivy2/local (needed for consumer fixtures / real CLI testing)

cd apps/web && npm install && npm run dev   # predev runs build-style-packs + build-registry, then Vite
cd apps/web && npm run build                # production: Vite plugin runs site/fullLinkJS, then esbuild minify → dist/
# → http://localhost:4300/                    native Laminar landing page
# → http://localhost:4300/components          components index (componentsGalleryPage)
# → http://localhost:4300/components/<name>   per-component docs + live preview
# → http://localhost:4300/plain-html-demo.html  Web Component demo (zero Scala.js on the page)
cd apps/web && node scripts/build-registry.mjs   # regenerate public/registry/*.json from packages/ui (also runs as part of predev/prebuild)

cd packages/cli && npm install && npm run build  # -> dist/index.js
node packages/cli/dist/index.js init --registry <path-or-url> --source-dir <path>
node packages/cli/dist/index.js add <component...>

./scripts/test   # build + registry rebuild + CLI init/add smoke test against a temp dir
```

### Things that will bite you if you don't know them

1. **Laminar tag-name collisions**: several HTML tags are exposed with a `Tag` suffix, not their bare name — `sectionTag`, `detailsTag`, `summaryTag`, `dialogTag`, `menuTag`, `commandTag`, `headerTag`, `footerTag`, `navTag`, `articleTag`, `asideTag`, `mainTag`, `timeTag`, `progressTag`. Bare `div`, `span`, `button`, `ul`, `li`, `ol`, `hr`, `figure`, `label`, `select`, `option`, `table`/`thead`/`tbody`/`tr`/`td`/`th` all work fine. `HtmlTag`, `DetachedRoot` also need explicit imports (`com.raquo.laminar.tags.HtmlTag`, `com.raquo.laminar.nodes.DetachedRoot`) — not re-exported by the `L.*` wildcard import. When in doubt, `grep` the actual name out of the `laminar` sources jar (`cs fetch --intransitive com.raquo:laminar_sjs1_3:17.2.1 --classifier sources`) rather than guessing — this list has been wrong before.
2. **`children`/other DOM-property names collide inside `ScElementBase` subclasses**: since `Sc*` classes extend `dom.HTMLElement`, which itself has a native `children: HTMLCollection` member, writing `children <-- signal` directly inside such a class resolves to the wrong thing. Build the Laminar tree in a companion-object function instead (see `ScAccordion`/`ScDropdownMenu` for the pattern) and pass in whatever `Var`s it needs.
3. **Shadow DOM retargets `ev.target`**: any document-level "click outside to close" check must use `ev.composedPath()`, not `ev.target` — see `Floating.scala`'s `compPath` helper and its doc comment for the exact failure mode this avoids (item selection silently eating clicks). `composedPath` isn't typed in the pinned scalajs-dom facade — cast through `js.Dynamic`. Every floating component (popover, tooltip, hover card, all three menus) routes its dismissal through `Floating.content`, so this check lives in one place now; a portaled panel is not a DOM descendant of its trigger, and nested submenu panels are siblings of their parent panel, which is why `Floating.Anchor` tracks nested anchors explicitly.
4. **`globals.css`'s `:root` token block won't reach a Shadow DOM** — `:root` only matches the document's `<html>`; inside a shadow root only `:host` matches. `sc-components.css` is wired up now and survives this only because `apps/web/scripts/build-webcomponents.mjs`'s `sc-shadow-scope` PostCSS step duplicates `:root` rules onto `:host` (and mirrors the baked-pack selector onto the shadow theme host). Any new CSS build step aimed at shadow roots must do the same rewrite, or components render structurally correct but completely uncolored — no error, just transparent backgrounds. See `vendor/NOTICE.md`'s "Shadow DOM tokens" section.
5. **`@scala-js/vite-plugin-scalajs`'s `cwd` option** is relative to the Vite project's own directory, not the repo root — `apps/web/vite.config.js` needs `cwd: "../.."` (two levels up), not `".."`.
5b. **Scala.js sourcemaps + Vite**: linker maps use absolute `file:` / `https:` URIs; Vite wrongly resolves them under `*-fastopt/` and prints "Sourcemap ... points to missing source files". Linker source maps are disabled in `build.sbt` (`withSourceMap(false)`). Do not re-enable without a Vite-compatible map strategy.
6. **Fetching `js.Promise` chains**: `.`then`[String](_.text())` needs the explicit type parameter on the first `.then` — Scala's type inference doesn't always widen `js.Promise[String]` to the expected `B | Thenable[B]` on its own (see `webcomponents/Main.scala`).
7. **`js.Date` getters return `Double`, the constructor wants `Int`**: `new js.Date(d.getFullYear(), d.getMonth(), day)` fails to compile — `.toInt` both getter calls first (see `Calendar.scala`). No java.time dependency is in this build; date logic is hand-rolled on `js.Date`.
8. **A method/value named the same as one of Laminar's own keys shadows it inside that scope** — e.g. never name a `Var[String]` parameter `value`, since `value` is also Laminar's `<input>` value prop; `InputOTP.scala` uses `codeVar` for exactly this reason. Same caution applies to `children`, `content`, `label`, etc. if you're inside a scope that also needs the Laminar key of the same name.
9. **Right-click (`contextmenu`) interactions are hard to verify via claude-in-chrome's browser automation** — its right-click simulation appears to trigger Chrome's native context menu directly rather than dispatching a page-level `contextmenu` DOM event. Playwright does dispatch the DOM event (`locator.click({ button: "right" })`), and `playwright` is already an `apps/web` devDependency, so drive context-menu checks from a script against the dev server.
10. **A docs page renders every component's demo, so `document.querySelector` finds the wrong instance** — there are a dozen closed `[data-slot=dropdown-menu-content]` panels on `/components/dropdown-menu` alone. Scope browser assertions to the open one (`:visible`, or `[data-open]`) or you will "prove" a bug that isn't there.

### Verification checklist for new work

- `sbt <module>/compile` for anything touched, then `sbt scalafmtAll` before committing.
- For `ui`/`webcomponents`/`site` changes: actually load a page in a browser (Playwright or manual) and click through the interaction, not just eyeball it — several real bugs in this codebase were invisible from source review or compilation alone.
- New component checklist: `.scala` in `packages/ui` (Tailwind classes matching the real shadcn/ui source) → `.registry.json` sidecar → add the display name to `componentNavList` in `apps/web/src/main/scala/shadcnscalajs/site/Main.scala` → add a `liveExample()` case and a matching `usageSource` case (keep these two matches in exact 1:1 correspondence — the Usage code block shown is only accurate if it matches what actually rendered) → `node scripts/build-registry.mjs` (or just let `predev`/`prebuild` do it).
- New block checklist: directory + `.scala` files + `<name>.registry.json` sidecar under `packages/blocks` (sidecar needs `type: "scala:block"`, `description`, `categories`, and per-file `type` of `scala:page`/`scala:component`) → add a `Blocks.Meta` entry to `Blocks.all` **and** a case to `Blocks.render` in `apps/web/.../Blocks.scala` (both, or the block is unreachable) → `node scripts/build-registry.mjs` → browser-check all three routes (`/blocks`, `/blocks/<name>`, `/blocks/<name>/preview`).
- **`Button(...)` with no variant/size uses the shadcn defaults** — `globals.css` paints `[data-slot=button]` without `data-variant` / `data-size` as primary and `h-9`. `Button.of` / `Button.appearance` set those attributes and opt out of the defaults. Do not also emit a second background utility on the same element; Tailwind resolves the clash by stylesheet order.
- For `cli` changes: run `init`+`add` against a scratch directory and, ideally, `sbt compile` the result against a `core/publishLocal`'d build (`./scripts/test` does the smoke-test part of this automatically; the full sbt-compile check is still manual — see `scripts/test`'s own comment).

## Verification

- Compile touched Scala modules with `sbt <module>/compile`.
- Run `sbt scalafmtAll` before committing.
- Run `./scripts/test` for the build, registry, and CLI smoke checks.
- Load affected site and Web Component pages in a browser and exercise their interactions.
