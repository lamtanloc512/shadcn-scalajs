# shadcn-scalajs

Copy-and-own [shadcn/ui](https://ui.shadcn.com/) components for [Scala.js](https://www.scala-js.org/) and [Laminar](https://laminar.dev/). Components use Tailwind CSS v4 utilities that follow shadcn/ui's canonical `new-york-v4` source and are installed into your application as editable Scala files.

> **Alpha:** shadcn-scalajs is ready for early projects and feedback, but APIs and generated project structure may change before 1.0.

- Documentation: https://shadcn-scalajs.vercel.app/docs/installation
- Components: https://shadcn-scalajs.vercel.app/components
- Customizer: https://shadcn-scalajs.vercel.app/create
- npm CLI: https://www.npmjs.com/package/shadcn-scalajs

## Installation

### Create a new project

Use an empty directory:

```bash
mkdir my-app
cd my-app
npx shadcn-scalajs@latest init --preset buFywLo
```

The initializer asks for:

```text
Project name: my-app
Artifact group (for example org.ethan.app): org.ethan.app
```

Then install dependencies and start development:

```bash
npm install
npm run dev
```

`npm run dev` runs Vite and `sbt ~ui/fastLinkJS` together. Saving a Scala file recompiles the UI and reloads the browser.

When `init` is run from a directory named `examples` or `.examples`, it creates a child directory using the project name:

```text
examples/
└── my-app/
```

Enter that directory before running the remaining commands.

### Non-interactive setup

```bash
npx shadcn-scalajs@latest init \
  --project-name my-app \
  --group org.ethan.app \
  --preset buFywLo
```

Use `--package` when the Scala package prefix should differ from the sbt artifact group.

## Generated architecture

```text
my-app/
├── packages/
│   ├── shared/       # domain contracts compiled for Scala.js and the JVM
│   ├── services/     # backend-framework-neutral JVM services
│   └── ui/           # Laminar + Scala.js + Vite + Tailwind CSS v4
├── project/
├── build.sbt
├── package.json
├── shadcn-scalajs.json
└── README.md
```

- **shared** contains platform-neutral models and contracts used by both the browser and backend.
- **services** is the JVM boundary. Add http4s, ZIO HTTP, Pekko HTTP, Play, or another backend without coupling it to the UI.
- **ui** contains the Laminar application and all copied shadcn-scalajs components.

## Add components

Run commands from the generated project root:

```bash
npx shadcn-scalajs@latest add button card dialog
```

The CLI resolves transitive registry dependencies and writes source under:

```text
packages/ui/src/main/scala/shadcnscalajs/
```

The files belong to your project. Edit them directly instead of treating shadcn-scalajs as a runtime component library.

Browse available components at https://shadcn-scalajs.vercel.app/components.

## Common commands

```bash
npm run dev      # Vite plus watched Scala.js fastLinkJS
npm run compile  # compile UI and services
npm run build    # optimized Scala.js and Vite production build
```

The production frontend is written to `packages/ui/dist`.

## Initialize an existing project

For an existing Scala.js project, write only the registry configuration:

```bash
npx shadcn-scalajs@latest init --no-scaffold \
  --source-dir src/main/scala/shadcnscalajs
```

`init` refuses to overwrite existing generated files unless `--force` is supplied.

## Repository development

Prerequisites:

- JDK 21
- sbt 1.10+
- Node.js 20+

Compile and link the repository:

```bash
export PATH="$PATH:$HOME/Library/Application Support/Coursier/bin"
sbt core/compile ui/compile blocks/compile site/compile
sbt ui/fastLinkJS site/fastLinkJS
```

Run the documentation site:

```bash
cd modules/site
npm install
npm run dev
```

Useful routes:

```text
http://localhost:4300/docs/installation
http://localhost:4300/components
http://localhost:4300/components/button
http://localhost:4300/blocks
http://localhost:4300/create
```

Build and test everything:

```bash
./scripts/test
```

The test script compiles the repository, rebuilds the registry, scaffolds a temporary consumer project, installs components, compiles the generated shared/UI/services projects, and produces a Vite production build.

## Repository layout

```text
modules/
  core/             small Laminar helpers used by copied components
  ui/               Laminar component source and registry sidecars
  blocks/           multi-file compositions built from UI components
  webcomponents/    future Web Component work; not part of the current release
  site/             documentation, previews, registry generation, and Vercel build
packages/
  cli/              npm scaffolder and component installer
vendor/              pinned upstream reference sources
```

## Future feature: Web Components

The current product and supported release target are Scala.js + Laminar. The repository contains early Web Component experiments, but they are not part of the public release, default site build, installation flow, or compatibility promise. Custom-element packaging, framework-neutral distribution, documentation, and support will be completed and released as a separate future feature.

## Publishing the CLI

npm publishing uses GitHub Trusted Publishing. To release:

1. Bump `packages/cli/package.json`, its lockfile, and the CLI version in `packages/cli/src/index.ts`.
2. Push the changes.
3. Create a GitHub release whose tag exactly matches `v<package-version>`.
4. `.github/workflows/publish-cli.yml` builds and publishes with npm provenance through OIDC.

No `NPM_TOKEN` repository secret is required.

## License

MIT
