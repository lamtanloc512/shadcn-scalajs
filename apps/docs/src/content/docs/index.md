---
title: Introduction
description: Copy-and-own shadcn/ui components for Scala.js and Laminar.
---

shadcn-scalajs is the shadcn/ui distribution model for Scala.js. The CLI copies component source into your project. You edit those files. Nothing is imported from a published component package at runtime.

The components are Laminar views styled with Tailwind CSS v4 utilities, following shadcn/ui's `new-york-v4` source. A generated app also has a JVM module for services and a shared module for contracts that compile to both platforms.

Live previews stay on the component site, because they are a Scala.js application:

- [Components](https://shadcn-scalajs.vercel.app/components)
- [Blocks](https://shadcn-scalajs.vercel.app/blocks)
- [Customizer](https://shadcn-scalajs.vercel.app/create)

## What you own

After `add`, a button is a Scala file under `packages/ui/src/main/scala/shadcnscalajs/`. Change the markup, the variants, or the classes there. Running `add` again for that same item, or for anything that depends on it, writes the registry copy back over the file.

## What you need

JDK 21, sbt 1.10 or newer, and Node.js 20 or newer. The first `npm run dev` compiles Scala.js before Vite starts, so that run takes longer than a typical JavaScript dev server. Later saves reload the browser without restarting sbt.

Continue with [Installation](/installation/).
