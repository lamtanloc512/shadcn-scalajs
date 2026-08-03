import { Command } from "commander";
import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { readConfig } from "../utils/config.js";
import { resolveItems } from "../utils/registry.js";

export const addCommand = new Command("add")
  .description("Add one or more components' Scala source into this project")
  .argument("<components...>", "component names, e.g. button dialog accordion")
  .action(async (components: string[]) => {
    const cwd = process.cwd();
    const config = await readConfig(cwd);

    const items = await resolveItems(config.registry, components);

    const scalaDependencies = new Set<string>();
    let fileCount = 0;

    for (const item of items) {
      for (const file of item.files) {
        const target = path.join(cwd, config.sourceDir, file.target);
        await mkdir(path.dirname(target), { recursive: true });
        await writeFile(target, file.content);
        fileCount++;
        console.log(`+ ${path.relative(cwd, target)}`);
      }
      for (const dep of item.scalaDependencies) scalaDependencies.add(dep);
    }

    console.log(`\nAdded ${items.length} component(s), ${fileCount} file(s).`);

    if (scalaDependencies.size > 0) {
      console.log("\nAdd these to your build.sbt if not already present:");
      for (const dep of scalaDependencies) console.log(`  "${dep}"`);
    }
  });
