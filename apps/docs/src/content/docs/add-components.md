---
title: Add components
description: Copy components and blocks into the project with the CLI.
---

Run `add` from the project root, the directory that contains `shadcn-scalajs.json`.

```bash
npx shadcn-scalajs@latest add button card dialog
```

Names match the registry: `button`, `dropdown-menu`, `login-01`, `dashboard-01`. The command walks `registryDependencies` and writes those files too. Adding `login-01` also installs the components that form uses.

Theme CSS is installed from files bundled in the CLI, into `packages/ui/src/styles/`. Pass `--style` to switch packs at the same time:

```bash
npx shadcn-scalajs@latest add button --style lyra
```

Browse names in the [component gallery](https://shadcn-scalajs.vercel.app/components) and the [block gallery](https://shadcn-scalajs.vercel.app/blocks).

## After the files land

Open the Scala file and use it from your own Laminar view. A button with no variant and no size is primary, at the default height. Pass both when you want another look:

```scala
Button.of(_.variant(Button.Variant.Primary), _.size(Button.Size.Default), _ => "Save")
```

Blocks are mountable elements, not routes. Laminar has no file-based router, so a login block is `Login01()` wherever your own router wants that screen.

The files are yours to edit. `add` writes the registry copy each time it installs an item, including dependencies, so run it before you customize those files.
