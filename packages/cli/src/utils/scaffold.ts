import { access, mkdir, readdir, writeFile } from "node:fs/promises";
import path from "node:path";

export interface ScaffoldOptions {
  projectName: string;
  scalaPackage: string;
  preset?: string;
}

const coreFiles: Record<string, string> = {
  "CommonAttrs.scala": `package shadcnscalajs.core

import com.raquo.laminar.api.L.*
import com.raquo.laminar.codecs.BooleanAsAttrPresenceCodec

/** Attributes shared by components that wrap native elements. */
object CommonAttrs:
  val openAttr: HtmlAttr[Boolean] = htmlAttr("open", BooleanAsAttrPresenceCodec)
`,
  "Tags.scala": `package shadcnscalajs.core

import com.raquo.laminar.api.L.*
import com.raquo.laminar.tags.HtmlTag
import org.scalajs.dom

object Tags:
  val slotTag: HtmlTag[dom.HTMLElement] = htmlTag("slot")
`
};

function files(options: ScaffoldOptions): Record<string, string> {
  const { projectName, scalaPackage, preset } = options;
  const packagePath = scalaPackage.split(".").join("/");
  const presetAttribute = preset ? ` data-preset="${preset}"` : "";

  return {
    "README.md": `# ${projectName}

A Scala.js + Laminar application scaffolded with [shadcn-scalajs](https://shadcn-scalajs.vercel.app).

## Prerequisites

- JDK 21
- sbt 1.10+
- Node.js 20+

## Start the UI

\`\`\`bash
npm install
npm run dev
\`\`\`

Vite starts the Laminar UI at http://localhost:5173 and invokes Scala.js automatically.

## Add UI components

\`\`\`bash
npx shadcn-scalajs@latest add button card dialog
\`\`\`

Components are copied into \`packages/ui/src/main/scala/shadcnscalajs/ui\` so your project owns their source.

## Packages

- \`packages/shared\` — domain models and contracts compiled for both Scala.js and the JVM. Keep this code platform-neutral.
- \`packages/ui\` — the Laminar frontend, Vite entry point, Tailwind CSS, and copied shadcn-scalajs components.
- \`packages/services\` — backend-neutral JVM services. Add your preferred HTTP framework here and depend on shared contracts.

The generated Scala package prefix is \`${scalaPackage}\`.

## Build and verify

\`\`\`bash
npm run compile  # compile the UI and services with sbt
npm run build    # optimized Scala.js + Vite production build
\`\`\`

The production UI is written to \`packages/ui/dist\`.

## Connect a backend

Add your backend library to the \`services\` project in \`build.sbt\`. Put request/response models in
\`packages/shared\`, implement server behavior in \`packages/services\`, and call its HTTP API from
\`packages/ui\`. The scaffold deliberately does not force a backend framework.
`,
    "package.json": `{
  "name": "${projectName}",
  "private": true,
  "workspaces": ["packages/ui"],
  "scripts": {
    "dev": "npm --workspace packages/ui run dev",
    "build": "npm --workspace packages/ui run build",
    "compile": "sbt ui/compile services/compile"
  }
}
`,
    "build.sbt": `import org.scalajs.linker.interface.{ESVersion, ModuleKind}
import sbtcrossproject.CrossPlugin.autoImport.*
import scalajscrossproject.ScalaJSCrossPlugin.autoImport.*

ThisBuild / scalaVersion := "3.5.2"
ThisBuild / organization := "${scalaPackage}"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("packages/shared"))
  .settings(name := "${projectName}-shared")

lazy val ui = project
  .in(file("packages/ui"))
  .enablePlugins(org.scalajs.sbtplugin.ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withESFeatures(_.withESVersion(ESVersion.ES2020))
        .withSourceMap(false)
    },
    libraryDependencies += "com.raquo" %%% "laminar" % "17.2.1"
  )
  .dependsOn(shared.js)

/** Backend-framework-neutral JVM module. Add http4s, Pekko HTTP, ZIO HTTP,
  * Play, or another backend at this boundary without coupling it to the UI. */
lazy val services = project
  .in(file("packages/services"))
  .settings(name := "${projectName}-services")
  .dependsOn(shared.jvm)

lazy val root = project
  .in(file("."))
  .aggregate(shared.js, shared.jvm, ui, services)
  .settings(name := "${projectName}", publish / skip := true)
`,
    "project/plugins.sbt": `addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.1")
addSbtPlugin("org.portable-scala" % "sbt-crossproject" % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
`,
    "project/build.properties": `sbt.version=1.10.5
`,
    [`packages/shared/src/main/scala/${packagePath}/shared/Shared.scala`]: `package ${scalaPackage}.shared

/** Domain contracts and data that compile for both Scala.js and the JVM. */
object Shared:
  val applicationName: String = "${projectName}"
`,
    [`packages/services/src/main/scala/${packagePath}/services/Services.scala`]: `package ${scalaPackage}.services

import ${scalaPackage}.shared.Shared

/** Backend-framework-neutral service boundary. */
object Services:
  def health: String = s"\${Shared.applicationName}: ok"
`,
    [`packages/ui/src/main/scala/${packagePath}/ui/Main.scala`]: `package ${scalaPackage}.ui

import com.raquo.laminar.api.L.*
import org.scalajs.dom
import ${scalaPackage}.shared.Shared

object Main:
  def main(args: Array[String]): Unit =
    renderOnDomContentLoaded(dom.document.getElementById("app"), app())

  private def app(): HtmlElement =
    mainTag(
      cls := "mx-auto flex min-h-dvh max-w-3xl flex-col justify-center gap-4 px-6",
      p(cls := "text-sm font-medium text-muted-foreground", "Scala.js + Laminar"),
      h1(cls := "text-4xl font-semibold tracking-tight", s"Welcome to \${Shared.applicationName}"),
      p(cls := "text-muted-foreground", "Your shared, services, and UI packages are ready.")
    )
`,
    "packages/ui/src/main/scala/shadcnscalajs/core/CommonAttrs.scala": coreFiles["CommonAttrs.scala"],
    "packages/ui/src/main/scala/shadcnscalajs/core/Tags.scala": coreFiles["Tags.scala"],
    "packages/ui/package.json": `{
  "name": "${projectName}-ui",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "devDependencies": {
    "@scala-js/vite-plugin-scalajs": "^1.0.0",
    "@tailwindcss/postcss": "^4.1.0",
    "postcss": "^8.5.0",
    "tailwindcss": "^4.1.0",
    "tw-animate-css": "^1.3.0",
    "vite": "^7.0.0"
  }
}
`,
    "packages/ui/vite.config.js": `import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig({
  plugins: [scalaJSPlugin({ cwd: "../..", projectID: "ui" })],
  server: { port: 5173 },
  build: { target: "es2020", sourcemap: false }
});
`,
    "packages/ui/postcss.config.mjs": `export default { plugins: { "@tailwindcss/postcss": {} } };
`,
    "packages/ui/src/styles/globals.css": `@import "tailwindcss";
@import "tw-animate-css";
@source "../main/scala/**/*.scala";

@custom-variant dark (&:is(.dark *));

@theme inline {
  --color-background: var(--background);
  --color-foreground: var(--foreground);
  --color-card: var(--card);
  --color-card-foreground: var(--card-foreground);
  --color-popover: var(--popover);
  --color-popover-foreground: var(--popover-foreground);
  --color-primary: var(--primary);
  --color-primary-foreground: var(--primary-foreground);
  --color-secondary: var(--secondary);
  --color-secondary-foreground: var(--secondary-foreground);
  --color-muted: var(--muted);
  --color-muted-foreground: var(--muted-foreground);
  --color-accent: var(--accent);
  --color-accent-foreground: var(--accent-foreground);
  --color-destructive: var(--destructive);
  --color-border: var(--border);
  --color-input: var(--input);
  --color-ring: var(--ring);
  --radius-sm: calc(var(--radius) - 4px);
  --radius-md: calc(var(--radius) - 2px);
  --radius-lg: var(--radius);
  --radius-xl: calc(var(--radius) + 4px);
}

:root {
  --radius: 0.625rem;
  --background: oklch(1 0 0);
  --foreground: oklch(0.145 0 0);
  --card: oklch(1 0 0);
  --card-foreground: oklch(0.145 0 0);
  --popover: oklch(1 0 0);
  --popover-foreground: oklch(0.145 0 0);
  --primary: oklch(0.205 0 0);
  --primary-foreground: oklch(0.985 0 0);
  --secondary: oklch(0.97 0 0);
  --secondary-foreground: oklch(0.205 0 0);
  --muted: oklch(0.97 0 0);
  --muted-foreground: oklch(0.556 0 0);
  --accent: oklch(0.97 0 0);
  --accent-foreground: oklch(0.205 0 0);
  --destructive: oklch(0.577 0.245 27.325);
  --border: oklch(0.922 0 0);
  --input: oklch(0.922 0 0);
  --ring: oklch(0.708 0 0);
}

.dark {
  --background: oklch(0.145 0 0);
  --foreground: oklch(0.985 0 0);
  --card: oklch(0.205 0 0);
  --card-foreground: oklch(0.985 0 0);
  --popover: oklch(0.205 0 0);
  --popover-foreground: oklch(0.985 0 0);
  --primary: oklch(0.922 0 0);
  --primary-foreground: oklch(0.205 0 0);
  --secondary: oklch(0.269 0 0);
  --secondary-foreground: oklch(0.985 0 0);
  --muted: oklch(0.269 0 0);
  --muted-foreground: oklch(0.708 0 0);
  --accent: oklch(0.269 0 0);
  --accent-foreground: oklch(0.985 0 0);
  --destructive: oklch(0.704 0.191 22.216);
  --border: oklch(1 0 0 / 10%);
  --input: oklch(1 0 0 / 15%);
  --ring: oklch(0.556 0 0);
}

@layer base {
  * { @apply border-border outline-ring/50; }
  body { @apply m-0 bg-background text-foreground antialiased; }
}
`,
    "packages/ui/index.html": `<!doctype html>
<html lang="en"${presetAttribute}>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${projectName}</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/index.js"></script>
  </body>
</html>
`,
    "packages/ui/index.js": `import "./src/styles/globals.css";
import "scalajs:main.js";
`,
    ".gitignore": `target/
project/target/
.bsp/
.metals/
.idea/
node_modules/
dist/
.DS_Store
`
  };
}

export async function scaffold(cwd: string, options: ScaffoldOptions, force: boolean): Promise<string[]> {
  const generated = files(options);

  if (!force) {
    for (const relative of Object.keys(generated)) {
      const target = path.join(cwd, relative);
      const exists = await access(target).then(() => true).catch(() => false);
      if (exists) throw new Error(`Refusing to overwrite generated file: ${relative}`);
    }
  }

  const written: string[] = [];
  for (const [relative, content] of Object.entries(generated)) {
    const target = path.join(cwd, relative);
    await mkdir(path.dirname(target), { recursive: true });
    await writeFile(target, content, "utf8");
    written.push(relative);
  }
  return written;
}

export async function isEmptyDirectory(cwd: string): Promise<boolean> {
  const ignored = new Set([".git", ".DS_Store"]);
  try {
    return (await readdir(cwd)).every(entry => ignored.has(entry));
  } catch (error) {
    if ((error as NodeJS.ErrnoException).code === "ENOENT") return true;
    throw error;
  }
}
