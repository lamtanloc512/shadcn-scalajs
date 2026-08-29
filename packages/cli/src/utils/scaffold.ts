import { access, copyFile, mkdir, readdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

export interface ScaffoldOptions {
  projectName: string;
  artifactGroup: string;
  scalaPackage: string;
  preset?: string;
  stylePack?: string;
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
  const stylePack = options.stylePack ?? "nova";
  const packagePath = scalaPackage.split(".").join("/");
  const presetAttribute = [
    ` data-style-pack="${stylePack}"`,
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

The dev command starts \`sbt ~ui/fastLinkJS\` first, waits for the Scala.js bundle, then starts Vite. That avoids sbt ServerAlreadyBootingException when both processes boot at once.

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
    "dev": "concurrently --kill-others --names scala,vite \\\"sbt ~ui/fastLinkJS\\\" \\\"node scripts/wait-for-scalajs.mjs && npm --workspace packages/ui run dev:vite\\\"",
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
@import "./tokens.css";
@import "./pack-${stylePack}.css";
@source "../main/scala/**/*.scala";

@custom-variant dark (&:is(.dark *));

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
    "scripts/wait-for-scalajs.mjs": `import { access } from "node:fs/promises";
import path from "node:path";

// Vite's Scala.js plugin also starts sbt. If it races the watcher process during
// boot, sbt throws ServerAlreadyBootingException ("Address already in use").
// Wait until the first fastopt bundle exists so the server is already up.
const marker = path.resolve("packages/ui/target/scala-3.5.2/ui-fastopt/main.js");
const deadline = Date.now() + 180_000;

while (Date.now() < deadline) {
  try {
    await access(marker);
    console.log("[wait-for-scalajs] Scala.js bundle ready");
    process.exit(0);
  } catch {
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }
}

console.error("[wait-for-scalajs] Timed out waiting for " + marker);
process.exit(1);
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
  const stylePack = options.stylePack ?? "nova";
  const generated = files(options);
  const assetCopies: Array<{ from: string; to: string }> = [
    {
      from: path.join(assetsDir(), "styles", "tokens.css"),
      to: "packages/ui/src/styles/tokens.css"
    },
    {
      from: path.join(assetsDir(), "styles", `pack-${stylePack}.css`),
      to: `packages/ui/src/styles/pack-${stylePack}.css`
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
