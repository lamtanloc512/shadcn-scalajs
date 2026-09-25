---
title: Installation
description: Create a Scala.js and Laminar project that is ready for shadcn-scalajs components.
---

Use an empty directory. `init` refuses to write into a directory that already has files unless you pass `--force`. For a project you already have, use [Existing project](/existing-project/) instead.

## Prerequisites

```bash
java --version    # 21
sbt --version     # 1.10 or newer
node --version    # 20 or newer
```

sbt installed through Coursier is often missing from a new shell:

```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
```

## Create a project

Interactive:

```bash
mkdir my-app
cd my-app
npx shadcn-scalajs@latest init
```

The prompts are the project name and the sbt artifact group, for example `com.example.app`.

Non-interactive:

```bash
npx shadcn-scalajs@latest init \
  --project-name my-app \
  --group com.example.app
```

### Style pack

A preset code from the [customizer](https://shadcn-scalajs.vercel.app/create) picks the style pack and theme tokens:

```bash
npx shadcn-scalajs@latest init \
  --project-name my-app \
  --group com.example.app \
  --preset buFywLo
```

Omit `--preset` and the project starts on the Nova pack. You can also pass `--style nova` (or `vega`, `maia`, `lyra`, `mira`, `luma`, `sera`, `rhea`) without a preset code.

When the current directory is named `examples` or `.examples`, `init` creates `examples/<project-name>` and prints the path to enter next.

## Run it

```bash
npm install
npm run dev
```

`npm run dev` starts the sbt watcher, waits until that run is ready, then starts Vite. The browser stays blank until the first Scala.js link finishes. That wait is the compile, not a hung dev server.

Saving a Scala file rebuilds and reloads the page. It does not restart sbt.

## Production

```bash
npm run compile
npm run build
```

The optimized frontend is written to `packages/ui/dist`.

Next: [add components](/add-components/), or read how the [project is laid out](/project-layout/).
