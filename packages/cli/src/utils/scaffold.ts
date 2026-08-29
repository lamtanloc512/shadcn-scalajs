import { access, copyFile, mkdir, readdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

export interface ScaffoldOptions {
  projectName: string;
  artifactGroup: string;
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
  const { projectName, artifactGroup, scalaPackage, preset } = options;
  const packagePath = scalaPackage.split(".").join("/");
  const presetAttribute = [
    ` data-style-pack="nova"`,
    preset ? ` data-preset="${preset}"` : ""
  ].join("");

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

The dev command runs Vite and \`sbt ~ui/fastLinkJS\` together. Scala edits are relinked automatically and Vite reloads the Laminar UI at http://localhost:5173.

## Add UI components

\`\`\`bash
npx shadcn-scalajs@latest add button card dialog
\`\`\`

Components are copied into \`packages/ui/src/main/scala/shadcnscalajs/ui\` so your project owns their source.

## Packages

- \`packages/shared\` — domain models and contracts compiled for both Scala.js and the JVM. Keep this code platform-neutral.
- \`packages/ui\` — the Laminar frontend, Vite entry point, Tailwind CSS, and copied shadcn-scalajs components.
- \`packages/services\` — backend-neutral JVM services. Add your preferred HTTP framework here and depend on shared contracts.

The sbt artifact group is \`${artifactGroup}\` and the generated Scala package prefix is \`${scalaPackage}\`.

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
    "dev": "concurrently --kill-others --names scala,vite \\\"sbt ~ui/fastLinkJS\\\" \\\"npm --workspace packages/ui run dev:vite\\\"",
    "build": "npm --workspace packages/ui run build",
    "compile": "sbt ui/compile services/compile"
  },
  "devDependencies": {
    "concurrently": "^9.2.1"
  }
}
`,
    "build.sbt": `import org.scalajs.linker.interface.{ESVersion, ModuleKind, ModuleSplitStyle}
import sbtcrossproject.CrossPlugin.autoImport.*
import scalajscrossproject.ScalaJSCrossPlugin.autoImport.*

ThisBuild / scalaVersion := "3.5.2"
ThisBuild / organization := "${artifactGroup}"
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
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("${scalaPackage}.ui", "shadcnscalajs.ui")))
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
    "dev:vite": "vite",
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
@import "./pack-nova.css";
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
  --color-destructive-foreground: var(--destructive-foreground);
  --color-border: var(--border);
  --color-input: var(--input);
  --color-ring: var(--ring);
  --color-chart-1: var(--chart-1);
  --color-chart-2: var(--chart-2);
  --color-chart-3: var(--chart-3);
  --color-chart-4: var(--chart-4);
  --color-chart-5: var(--chart-5);
  --color-sidebar: var(--sidebar);
  --color-sidebar-foreground: var(--sidebar-foreground);
  --color-sidebar-primary: var(--sidebar-primary);
  --color-sidebar-primary-foreground: var(--sidebar-primary-foreground);
  --color-sidebar-accent: var(--sidebar-accent);
  --color-sidebar-accent-foreground: var(--sidebar-accent-foreground);
  --color-sidebar-border: var(--sidebar-border);
  --color-sidebar-ring: var(--sidebar-ring);
  --radius-sm: calc(var(--radius) * 0.6);
  --radius-md: calc(var(--radius) * 0.8);
  --radius-lg: var(--radius);
  --radius-xl: calc(var(--radius) * 1.4);
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
  --destructive-foreground: oklch(0.97 0.01 17);
  --border: oklch(0.922 0 0);
  --input: oklch(0.922 0 0);
  --ring: oklch(0.708 0 0);
  --chart-1: #93c5fd;
  --chart-2: #3b82f6;
  --chart-3: #2563eb;
  --chart-4: #1d4ed8;
  --chart-5: #1e40af;
  --sidebar: oklch(0.985 0 0);
  --sidebar-foreground: oklch(0.145 0 0);
  --sidebar-primary: oklch(0.205 0 0);
  --sidebar-primary-foreground: oklch(0.985 0 0);
  --sidebar-accent: oklch(0.97 0 0);
  --sidebar-accent-foreground: oklch(0.205 0 0);
  --sidebar-border: oklch(0.922 0 0);
  --sidebar-ring: oklch(0.708 0 0);
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
  --destructive-foreground: oklch(0.985 0 0);
  --border: oklch(1 0 0 / 10%);
  --input: oklch(1 0 0 / 15%);
  --ring: oklch(0.556 0 0);
  --chart-1: #93c5fd;
  --chart-2: #3b82f6;
  --chart-3: #2563eb;
  --chart-4: #1d4ed8;
  --chart-5: #1e40af;
  --sidebar: oklch(0.205 0 0);
  --sidebar-foreground: oklch(0.985 0 0);
  --sidebar-primary: oklch(0.488 0.243 264.376);
  --sidebar-primary-foreground: oklch(0.985 0 0);
  --sidebar-accent: oklch(0.269 0 0);
  --sidebar-accent-foreground: oklch(0.985 0 0);
  --sidebar-border: oklch(1 0 0 / 10%);
  --sidebar-ring: oklch(0.439 0 0);
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

function assetsDir(): string {
  // dist/utils/scaffold.js -> ../../assets
  return path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../assets");
}

export async function scaffold(cwd: string, options: ScaffoldOptions, force: boolean): Promise<string[]> {
  const generated = files(options);
  const assetCopies: Array<{ from: string; to: string }> = [
    {
      from: path.join(assetsDir(), "styles", "pack-nova.css"),
      to: "packages/ui/src/styles/pack-nova.css"
    }
  ];

  if (!force) {
    for (const relative of [...Object.keys(generated), ...assetCopies.map(copy => copy.to)]) {
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
  for (const copy of assetCopies) {
    const target = path.join(cwd, copy.to);
    await mkdir(path.dirname(target), { recursive: true });
    await copyFile(copy.from, target);
    written.push(copy.to);
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
