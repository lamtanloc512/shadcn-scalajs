# shadcn-scalajs

<!-- franky:base @include .franky/base/instructions.md -->

## Agent skill

Cursor loads `.cursor/skills/franky/SKILL.md` automatically — it contains the full `franky help` instructions. Run `franky help` to refresh from CLI.

## Project rules

Add project-specific agent instructions below. Franky base rules above always apply.

### What this is

A port of shadcn/ui's philosophy to Scala.js + Laminar: components you copy into your own project (CLI + registry, like real shadcn/ui — not just a published library), styled with vendored [basecoat](https://github.com/hunvreus/basecoat) CSS so shadcn/TweakCN themes work unmodified, and every component also compiles to a standalone Web Component so non-Scala frontends can use it too. Full design rationale, decisions, and a log of every bug found while building v1 lives at `/Users/locgorilla/.claude/plans/let-create-for-effervescent-penguin.md` (Claude Code plan file, not in this repo) — **read it before making architectural changes**.

### Status

v1 is complete and verified (real browser via claude-in-chrome, real independent `sbt compile` of CLI-written files) for 5 components: `Button`, `Badge` (pure CSS), `Dialog`, `Accordion` (native elements, no custom JS), `DropdownMenu` (hand-rolled Airstream `Var`/`EventBus` behavior — no Radix-equivalent exists for Laminar). Porting the remaining ~33 basecoat components follows the same per-component pattern (`.scala` in `modules/ui` + `.registry.json` sidecar + `Sc*` wrapper in `modules/webcomponents`).

### Layout

```
modules/core/           design tokens, data-variant/data-size attribute helpers (shadcnscalajs.core.DataAttrs, CommonAttrs, Tags)
modules/ui/             Laminar component source of truth — what the CLI copies into consumer projects; one .scala + one .registry.json per component
modules/webcomponents/  ScElementBase + Sc* custom-element wrappers around modules/ui, for non-Scala consumers
modules/site/           Vite dev app: native-Laminar demo (index.html/Main.scala) + plain-html-demo.html (Web Component demo, zero Scala.js) + scripts/build-registry.mjs
packages/cli/           Node/TS + Commander: `init` writes shadcn-scalajs.json, `add <names...>` resolves registryDependencies and writes files
vendor/                 vendored, Tailwind-compiled basecoat CSS — see vendor/NOTICE.md for provenance AND a required patch (see below)
```

### Build/dev commands

```bash
# add coursier-installed sbt to PATH if `sbt` isn't found:
export PATH="$PATH:/Users/locgorilla/Library/Application Support/Coursier/bin"

sbt core/compile ui/compile webcomponents/compile site/compile   # compile everything
sbt ui/fastLinkJS webcomponents/fastLinkJS site/fastLinkJS       # Scala.js link (per module)
sbt core/publishLocal                                            # publish core to ~/.ivy2/local (needed for consumer fixtures / real CLI testing)

cd modules/site && npm install && npm run dev   # http://localhost:4300/ (native) and /plain-html-demo.html (Web Component)
cd modules/site && node scripts/build-registry.mjs   # regenerate public/registry/*.json from modules/ui

cd packages/cli && npm install && npm run build  # -> dist/index.js
node packages/cli/dist/index.js init --registry <path-or-url> --source-dir <path>
node packages/cli/dist/index.js add <component...>
```

### Things that will bite you if you don't know them

1. **Laminar tag-name collisions**: several HTML tags are exposed with a `Tag` suffix, not their bare name — `sectionTag`, `detailsTag`, `summaryTag`, `dialogTag`, `menuTag`, `commandTag` (bare `section`/`details`/etc. don't exist in `com.raquo.laminar.api.L.*`). `HtmlTag`, `DetachedRoot` also need explicit imports (`com.raquo.laminar.tags.HtmlTag`, `com.raquo.laminar.nodes.DetachedRoot`) — they're not re-exported by the `L.*` wildcard import.
2. **`children`/other DOM-property names collide inside `ScElementBase` subclasses**: since `Sc*` classes extend `dom.HTMLElement`, which itself has a native `children: HTMLCollection` member, writing `children <-- signal` directly inside such a class resolves to the wrong thing. Build the Laminar tree in a companion-object function instead (see `ScAccordion`/`ScDropdownMenu` for the pattern) and pass in whatever `Var`s it needs.
3. **basecoat's compiled CSS needs a one-line patch for Shadow DOM to work at all**: the raw token values (`--background: oklch(...)`, etc.) live under bare `:root { ... }`, which never matches inside a Shadow Root (only `:host` does). `vendor/basecoat-vega.cdn.css` already has this patched (`:root, :host { ... }`) — if you regenerate it from a newer basecoat version, you MUST reapply `sed -i '' 's/^:root {$/:root, :host {/' vendor/basecoat-vega.cdn.css` (see `vendor/NOTICE.md`). Skipping this makes every Web Component render structurally correct but completely uncolored — no error, no warning, just `getComputedStyle(...).backgroundColor` silently returning transparent.
4. **Shadow DOM retargets `ev.target`**: any document-level "click outside to close" check must use `ev.composedPath()`, not `ev.target` — see `DropdownMenu.scala`'s `composedPath` helper and its doc comment for the exact failure mode this avoids (item selection silently eating clicks).
5. **`@scala-js/vite-plugin-scalajs`'s `cwd` option** is relative to the Vite project's own directory, not the repo root — `modules/site/vite.config.js` needs `cwd: "../.."` (two levels up), not `".."`.
6. Fetching `js.Promise` chains: `.`then`[String](_.text())` needs the explicit type parameter on the first `.then` — Scala's type inference doesn't always widen `js.Promise[String]` to the expected `B | Thenable[B]` on its own (see `webcomponents/Main.scala`).

### Verification checklist for new work

- `sbt <module>/compile` for anything touched.
- For `ui`/`webcomponents` changes: actually load a page in a browser (claude-in-chrome or manual) and check computed styles / click through the interaction — several real bugs here were invisible from source review or compilation alone (see the plan file's "Progress notes" section for the exact bugs and how they were found).
- For `cli` changes: run `init`+`add` against a scratch directory and, ideally, `sbt compile` the result against a `core/publishLocal`'d build.

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