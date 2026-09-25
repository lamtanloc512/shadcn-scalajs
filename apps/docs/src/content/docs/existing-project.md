---
title: Existing project
description: Point an existing Scala.js project at the registry without scaffolding a new app.
---

`init --no-scaffold` writes only `shadcn-scalajs.json`. It does not create `build.sbt`, packages, or a Vite app.

```bash
npx shadcn-scalajs@latest init --no-scaffold \
  --source-dir src/main/scala/shadcnscalajs
```

`--source-dir` is the directory that already matches the `shadcnscalajs` package. `add` writes `ui/Button.scala` under that directory and does not rewrite package declarations. The folder name has to stay `shadcnscalajs`.

Pass `--registry` when the components should come from somewhere other than `https://shadcn-scalajs.vercel.app/registry`. A local checkout can use the generated files directly:

```bash
npx shadcn-scalajs@latest init --no-scaffold \
  --registry /path/to/shadcn-scalajs/modules/site/public/registry \
  --source-dir src/main/scala/shadcnscalajs
```

The project still needs Tailwind CSS v4, the theme files `add` installs on first use (`tokens.css` and one `pack-*.css`), and the small `shadcnscalajs.core` sources. A scaffold from `init` includes that core package so copied components compile without a published Maven artifact. An existing build can vendor the same sources or depend on a locally published `core`.

`init` will not replace an existing `shadcn-scalajs.json` unless you pass `--force`.
