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

The dev command starts the sbt watcher, waits for that run to become ready, and then starts Vite. Saving a Scala file recompiles the UI and reloads the browser without the sbt restart race.

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

## Build Web Components

After adding UI components and running \`npm install\`, install wrappers and build the standalone assets:

\`\`\`bash
npx shadcn-scalajs@latest build-wc button badge
\`\`\`

Only installed, supported UI components are registered. Omit the names to include all supported components currently installed. Wrappers are copied to \`packages/webcomponents/src/main/scala\` and can be edited there; rerunning \`build-wc\` does not overwrite them. When UI or wrapper source changes, run \`npm run build:webcomponents\` again before \`npm run build\`. The latter copies the generated assets into \`packages/ui/dist\`, but does not rebuild them.

The optional build writes \`packages/ui/public/sc-components-loader.js\`, \`sc-components.js\`, \`sc-components.css\`, \`sc-document.css\`, and \`styles/pack-*.css\`. In an HTML consumer served at the site root:

\`\`\`html
<html data-style-pack="${stylePack}">
  <body>
    <sc-button variant="primary">Save</sc-button>
    <script type="module" src="/sc-components-loader.js"></script>
  </body>
</html>
\`\`\`

Host the loader, bundle, and both stylesheets together, with \`styles/pack-${stylePack}.css\` beneath them. The loader resolves asset and pack URLs relative to its own URL, loads the document stylesheet before defining tags, and isolates component styles from the consumer page. It sets the built style pack if the HTML pack is missing or differs. Nova and custom packs use their generated pack stylesheet inside the component themes; load your own page theme separately if the surrounding page needs matching colors. The normal UI build does not require wrappers.

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
    "dev": "concurrently --kill-others --names scala,vite \\\"node scripts/run-scalajs-watch.mjs\\\" \\\"node scripts/wait-for-scalajs.mjs && npm --workspace packages/ui run dev:vite\\\"",
    "build": "npm --workspace packages/ui run build",
    "compile": "sbt ui/compile services/compile",
    "build:webcomponents": "node scripts/build-webcomponents.mjs"
  },
  "devDependencies": {
    "@tailwindcss/postcss": "^4.1.0",
    "concurrently": "^9.2.1",
    "esbuild": "^0.25.0",
    "postcss": "^8.5.0",
    "tailwindcss": "^4.1.0",
    "tw-animate-css": "^1.3.0"
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

lazy val webcomponents = project
  .in(file("packages/webcomponents"))
  .enablePlugins(org.scalajs.sbtplugin.ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withESFeatures(_.withESVersion(ESVersion.ES2020))
        .withSourceMap(false)
    }
  )
  .dependsOn(ui)

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
    "scripts/build-webcomponents.mjs": `import { execFileSync } from "node:child_process";
import { copyFileSync, mkdirSync, readFileSync, statSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import * as esbuild from "esbuild";
import postcss from "postcss";
import tailwind from "@tailwindcss/postcss";
import { isolateDocumentCss } from "./isolated-css.mjs";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const styles = join(root, "packages/ui/src/styles");
const output = join(root, "packages/ui/public");
const pack = JSON.parse(readFileSync(join(root, "shadcn-scalajs.json"), "utf8")).stylePack ?? "nova";
const linkDir = join(root, "packages/webcomponents/target/scala-3.5.2/webcomponents-opt");

function shadowScope() {
  const selector = '[data-style-pack="' + pack + '"]';
  const fallback = '[data-slot="sc-theme-host"]:not([data-style-pack])';
  return {
    postcssPlugin: "sc-shadow-scope",
    OnceExit(css) {
      css.walkRules(rule => {
        const next = [...rule.selectors];
        if (next.includes(":root") && !next.includes(":host")) next.push(":host");
        for (const item of rule.selectors) {
          if (item.includes(selector)) next.push(item.replaceAll(selector, fallback));
        }
        rule.selectors = [...new Set(next)];
      });
    }
  };
}

mkdirSync(output, { recursive: true });
mkdirSync(join(output, "styles"), { recursive: true });
execFileSync(process.platform === "win32" ? "sbt.bat" : "sbt", ["-batch", "webcomponents/fullLinkJS"], { cwd: root, stdio: "inherit" });
const js = join(output, "sc-components.js");
await esbuild.build({
  entryPoints: [join(linkDir, "main.js")],
  outfile: js,
  bundle: true,
  format: "esm",
  target: "es2020",
  legalComments: "none"
});
const css = join(output, "sc-components.css");
const entry = [
  '@import "./globals.css";',
  '@source "../../../webcomponents/src/main/scala/**/*.scala";',
  '@layer base { :root { --sc-pack: "' + pack + '"; } }'
].join("\\n");
const result = await postcss([tailwind(), shadowScope()]).process(entry, {
  from: join(styles, "sc-components.entry.css"),
  to: css
});
writeFileSync(css, result.css);
const stylesheet = readFileSync(css, "utf8");
const documentCss = join(output, "sc-document.css");
writeFileSync(documentCss, isolateDocumentCss(stylesheet, postcss));
const loader = join(output, "sc-components-loader.js");
writeFileSync(loader, [
  'const html = document.documentElement;',
  'const assets = new URL(".", import.meta.url);',
  'if (!html.hasAttribute("data-sc-assets-base")) html.dataset.scAssetsBase = assets.href;',
  'if (!html.hasAttribute("data-sc-pack-base")) html.dataset.scPackBase = new URL("styles/", assets).href;',
  'const pack = ' + JSON.stringify(pack) + ';',
  'if (html.dataset.stylePack && html.dataset.stylePack !== pack) throw new Error("This bundle only includes the " + pack + " style pack");',
  'if (!html.dataset.stylePack) html.dataset.stylePack = pack;',
  'html.dataset.scIsolated = "";',
  'html.dataset.scNoStoredTheme = "";',
  'const href = new URL("sc-document.css", assets).href;',
  'let sheet = [...document.querySelectorAll("link[rel=stylesheet]")].find(link => link.href === href);',
  'if (!sheet) { sheet = document.createElement("link"); sheet.rel = "stylesheet"; sheet.href = href; }',
  'if (!sheet.sheet) {',
  '  const loaded = new Promise((resolve, reject) => { sheet.addEventListener("load", resolve, { once: true }); sheet.addEventListener("error", () => reject(new Error("Failed to load sc-document.css")), { once: true }); });',
  '  if (!sheet.isConnected) document.head.append(sheet);',
  '  await loaded;',
  '}',
  'await import("./sc-components.js");'
].join("\\n") + "\\n");
if (!stylesheet.includes(":host")) throw new Error("Missing shadow root tokens in sc-components.css");
if (!stylesheet.includes('data-style-pack="' + pack + '"')) throw new Error("Missing style pack rules in sc-components.css");
copyFileSync(join(styles, "pack-" + pack + ".css"), join(output, "styles", "pack-" + pack + ".css"));
for (const file of [js, css, documentCss, loader, join(output, "styles", "pack-" + pack + ".css")]) {
  if (!statSync(file).size) throw new Error("Empty Web Component output: " + file);
  console.log(file);
}
`,
    "scripts/run-scalajs-watch.mjs": `import { spawn } from "node:child_process";
import { rm, writeFile } from "node:fs/promises";
import path from "node:path";

const readyFile = path.resolve(".scalajs-ready");
await rm(readyFile, { force: true });

const command = process.platform === "win32" ? "sbt.bat" : "sbt";
const sbt = spawn(command, ["~ui/fastLinkJS"], { stdio: ["inherit", "pipe", "pipe"] });
let output = "";
let ready = false;

const forward = (chunk, stream) => {
  const text = chunk.toString();
  stream.write(text);
  output = (output + text).slice(-4096);
  if (!ready && /Monitoring source files for .*fastLinkJS/.test(output)) {
    ready = true;
    writeFile(readyFile, "ready\\n").catch(() => {});
  }
};

sbt.stdout.on("data", (chunk) => forward(chunk, process.stdout));
sbt.stderr.on("data", (chunk) => forward(chunk, process.stderr));

const stop = () => {
  rm(readyFile, { force: true }).catch(() => {});
  sbt.kill("SIGTERM");
};
process.once("SIGINT", stop);
process.once("SIGTERM", stop);

sbt.once("close", async (code) => {
  await rm(readyFile, { force: true });
  process.exit(code ?? 1);
});
`,
    "scripts/wait-for-scalajs.mjs": `import { access } from "node:fs/promises";
import path from "node:path";

// The bundle can survive Ctrl+C, so do not use its existence as a readiness check.
// Wait for run-scalajs-watch.mjs to signal that this run's sbt watcher is monitoring.
const marker = path.resolve(".scalajs-ready");
const deadline = Date.now() + 180_000;

while (Date.now() < deadline) {
  try {
    await access(marker);
    console.log("[wait-for-scalajs] sbt watcher ready");
    process.exit(0);
  } catch {
    await new Promise((resolve) => setTimeout(resolve, 250));
  }
}

console.error("[wait-for-scalajs] Timed out waiting for the sbt watcher");
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
      from: path.join(assetsDir(), "webcomponents", "isolated-css.mjs"),
      to: "scripts/isolated-css.mjs"
    },
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
