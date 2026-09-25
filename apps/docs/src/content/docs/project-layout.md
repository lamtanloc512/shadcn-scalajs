---
title: Project layout
description: What init generates, and where each kind of code belongs.
---

```text
my-app/
├── packages/
│   ├── shared/       # contracts compiled for Scala.js and the JVM
│   ├── services/     # JVM services, with no HTTP framework chosen
│   └── ui/           # Laminar, Vite, Tailwind CSS v4, components
├── project/
├── build.sbt
├── package.json
└── shadcn-scalajs.json
```

## shared

Platform-neutral models and contracts. Code here is compiled for Scala.js and for the JVM, so the browser and the server can share a type without sharing a framework.

## services

The JVM boundary. Add http4s, ZIO HTTP, Pekko HTTP, Play, or another server in this module. Keep Laminar types out of it.

## ui

The browser. Copied components live in:

```text
packages/ui/src/main/scala/shadcnscalajs/
```

Theme tokens and the active style pack live in `packages/ui/src/styles/`. `globals.css` imports Tailwind, the tokens, and one pack. Only one pack is linked, so unused style packs are not in the CSS bundle.

`shadcn-scalajs.json` records the registry URL, the source directory, and the active style pack. `add` reads it. You should not need to edit it unless the registry host changes.
