#!/usr/bin/env node
// Registry build script — mirrors shadcn-svelte's docs/scripts/build-registry.ts
// (authoring format -> distributable format split): walks modules/ui's
// `*.registry.json` sidecars (author-facing, files described by `path`),
// inlines each paired `.scala` file's content, and emits `index.json` +
// one `<name>.json` per item into modules/site/public/registry — the same
// "docs app doubles as registry HTTP host" pattern shadcn-svelte uses.
//
// Plain Node (no TS build step) for v1, per the project's general bias
// toward minimal tooling; can be ported to TS without changing the schema
// if/when packages/cli needs shared types.

import { readdir, readFile, writeFile, mkdir } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const uiDir = path.resolve(__dirname, "../../ui/src/main/scala/shadcnscalajs/ui");
const outDir = path.resolve(__dirname, "../public/registry");

async function main() {
  const entries = (await readdir(uiDir)).filter((f) => f.endsWith(".registry.json"));

  const indexItems = [];
  await mkdir(outDir, { recursive: true });

  for (const entry of entries) {
    const sidecar = JSON.parse(await readFile(path.join(uiDir, entry), "utf-8"));

    const files = await Promise.all(
      sidecar.files.map(async (file) => {
        const content = await readFile(path.join(uiDir, file.path), "utf-8");
        // `target` is relative to the consumer's configured Scala `ui`
        // source directory (resolved by the future CLI against its
        // `shadcn-scalajs.json` config), mirroring shadcn-svelte's
        // alias-resolved `target` field.
        return { content, type: file.type, target: `ui/${file.path}` };
      })
    );

    const registryItem = {
      $schema: "https://shadcn-scalajs.dev/schema/registry-item.json",
      name: sidecar.name,
      title: sidecar.title,
      type: "scala:ui",
      registryDependencies: sidecar.registryDependencies ?? [],
      scalaDependencies: sidecar.scalaDependencies ?? [],
      files
    };

    await writeFile(path.join(outDir, `${sidecar.name}.json`), JSON.stringify(registryItem, null, 2));

    indexItems.push({
      name: sidecar.name,
      title: sidecar.title,
      type: "scala:ui",
      registryDependencies: sidecar.registryDependencies ?? []
    });
  }

  await writeFile(path.join(outDir, "index.json"), JSON.stringify(indexItems, null, 2));

  console.log(`Built registry: ${indexItems.length} items -> ${path.relative(process.cwd(), outDir)}`);
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
