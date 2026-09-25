# shadcn-scalajs

Copy-and-own [shadcn/ui](https://ui.shadcn.com/) for [Scala.js](https://www.scala-js.org/) and [Laminar](https://laminar.dev/). The CLI writes the component source into your project. You edit it there. It is not a runtime component library.

[![npm](https://img.shields.io/npm/v/shadcn-scalajs)](https://www.npmjs.com/package/shadcn-scalajs)
[![license](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Components use Tailwind CSS v4 utilities aligned with shadcn/ui's `new-york-v4` source. A project is a small Scala.js and JVM layout: shared contracts, a backend-neutral services module, and a Laminar UI.

| | |
| --- | --- |
| Live gallery | [shadcn-scalajs.vercel.app/components](https://shadcn-scalajs.vercel.app/components) |
| Blocks | [shadcn-scalajs.vercel.app/blocks](https://shadcn-scalajs.vercel.app/blocks) |
| Customizer | [shadcn-scalajs.vercel.app/create](https://shadcn-scalajs.vercel.app/create) |
| Written docs | [`apps/docs`](apps/docs) (Astro Starlight) |

## Quick start

JDK 21, sbt 1.10 or newer, and Node.js 20 or newer.

```bash
mkdir my-app
cd my-app
npx shadcn-scalajs@latest init \
  --project-name my-app \
  --group com.example.app
npm install
npm run dev
```

`init` asks for a project name and an sbt group when those flags are omitted. A preset code from the [customizer](https://shadcn-scalajs.vercel.app/create) selects the style pack and theme tokens. Without `--preset`, the project starts on the Nova pack.

```bash
npx shadcn-scalajs@latest add button card dialog
```

Copied files land in `packages/ui/src/main/scala/shadcnscalajs/`. Change them in place.

`npm run dev` starts the sbt watcher, waits until that run is ready, then starts Vite. The first compile is the slow one. Later Scala edits reload the browser without restarting sbt.

If `sbt` is not on your `PATH` after a Coursier install:

```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
```

## What a generated project looks like

```text
my-app/
├── packages/
│   ├── shared/       # contracts compiled for Scala.js and the JVM
│   ├── services/     # JVM services, with no HTTP framework chosen for you
│   └── ui/           # Laminar, Vite, Tailwind CSS v4, copied components
├── build.sbt
├── package.json
└── shadcn-scalajs.json
```

Put domain models in `shared`, the backend in `services`, and browser code in `ui`. Add http4s, ZIO HTTP, Pekko HTTP, Play, or another server inside `services` without coupling it to Laminar.

An existing Scala.js project only needs the registry config:

```bash
npx shadcn-scalajs@latest init --no-scaffold \
  --source-dir src/main/scala/shadcnscalajs
```

`init` will not overwrite a non-empty directory or an existing `shadcn-scalajs.json` unless you pass `--force`.

## Documentation

The component gallery, block previews, and theme customizer run as the Scala.js site. The written guide — installation, existing projects, the CLI, and the agent server — lives in Starlight:

```bash
cd apps/docs
npm install
npm run dev
```

That serves the docs at `http://localhost:4321`.

## Agents

The CLI speaks the Model Context Protocol over stdio. Agents can list the registry, read a component's source, and get the `add` command.

```json
{
  "mcpServers": {
    "shadcn-scalajs": {
      "command": "npx",
      "args": ["-y", "shadcn-scalajs@latest", "mcp"]
    }
  }
}
```

From this repository, point the server at the local registry:

```bash
node packages/cli/dist/index.js mcp \
  --registry modules/site/public/registry
```

Build the CLI first with `npm run build` inside `packages/cli`. The protocol details are in [apps/docs/src/content/docs/mcp.md](apps/docs/src/content/docs/mcp.md).

## Common commands

Inside a generated project:

```bash
npm run dev      # Vite, plus a watched Scala.js fastLinkJS
npm run compile  # compile UI and services
npm run build    # optimized Scala.js and a Vite production build
```

The production frontend is written to `packages/ui/dist`.

## Develop this repository

```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt core/compile ui/compile blocks/compile site/compile
cd modules/site && npm install && npm run dev
```

Useful routes once the site is up:

```text
http://localhost:4300/components
http://localhost:4300/components/button
http://localhost:4300/blocks
http://localhost:4300/create
```

```bash
./scripts/test
```

The test script compiles the repo, rebuilds the registry, scaffolds a temporary consumer, installs components and a block, then compiles and builds that project.

```text
modules/core            Laminar helpers copied with the components
modules/ui              component source and registry sidecars
modules/blocks          page and section compositions
modules/site            gallery, previews, registry generation
packages/cli            npm scaffolder, installer, and MCP server
apps/docs               Starlight documentation
```

Web Component wrappers exist in `modules/webcomponents` as experiments. They are not part of the install flow or the compatibility promise.

## License

[MIT](LICENSE)
