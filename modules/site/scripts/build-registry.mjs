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
const repoRoot = path.resolve(__dirname, "../../..");
const outDir = path.resolve(__dirname, "../public/registry");

const roots = [
  {
    dir: path.join(repoRoot, "modules/ui/src/main/scala/shadcnscalajs/ui"),
    type: "scala:ui",
    targetPrefix: "ui/",
    recursive: false
  },
  {
    dir: path.join(repoRoot, "modules/blocks/src/main/scala/shadcnscalajs/blocks"),
    type: "scala:block",
    targetPrefix: "blocks/",
    recursive: true
  }
];

/** Sidecar paths relative to `dir`. Non-recursive roots keep the original flat readdir behaviour. */
async function findSidecars(dir, recursive) {
  let entries;
  try {
    entries = await readdir(dir, { withFileTypes: true });
  } catch (err) {
    if (err.code === "ENOENT") return []; // root not created yet
    throw err;
  }
  const found = [];
  for (const entry of entries) {
    if (entry.isDirectory()) {
      if (!recursive) continue;
      const nested = await findSidecars(path.join(dir, entry.name), recursive);
      found.push(...nested.map((p) => path.join(entry.name, p)));
    } else if (entry.name.endsWith(".registry.json")) {
      found.push(entry.name);
    }
  }
  return found;
}

async function main() {
  const indexItems = [];
  await mkdir(outDir, { recursive: true });

  for (const root of roots) {
    const sidecars = await findSidecars(root.dir, root.recursive);

    for (const rel of sidecars) {
      const sidecar = JSON.parse(await readFile(path.join(root.dir, rel), "utf-8"));

      const files = await Promise.all(
        sidecar.files.map(async (file) => {
          const content = await readFile(path.join(root.dir, file.path), "utf-8");
          return { content, type: file.type ?? root.type, target: `${root.targetPrefix}${file.path}` };
        })
      );

      const registryItem = {
        $schema: "https://shadcn-scalajs.dev/schema/registry-item.json",
        name: sidecar.name,
        title: sidecar.title,
        type: sidecar.type ?? root.type,
        registryDependencies: sidecar.registryDependencies ?? [],
        scalaDependencies: sidecar.scalaDependencies ?? [],
        files
      };
      if (sidecar.description) registryItem.description = sidecar.description;
      if (sidecar.categories) registryItem.categories = sidecar.categories;

      await writeFile(path.join(outDir, `${sidecar.name}.json`), JSON.stringify(registryItem, null, 2));

      const indexEntry = {
        name: sidecar.name,
        title: sidecar.title,
        type: sidecar.type ?? root.type,
        registryDependencies: sidecar.registryDependencies ?? []
      };
      if (sidecar.description) indexEntry.description = sidecar.description;
      if (sidecar.categories) indexEntry.categories = sidecar.categories;
      indexItems.push(indexEntry);
    }
  }

  await writeFile(path.join(outDir, "index.json"), JSON.stringify(indexItems, null, 2));

  console.log(`Built registry: ${indexItems.length} items -> ${path.relative(process.cwd(), outDir)}`);
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
