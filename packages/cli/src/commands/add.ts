import { Command } from "commander";
import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { readConfig } from "../utils/config.js";
import { resolveInstallPath } from "../utils/paths.js";
import { resolveItems } from "../utils/registry.js";

export const addCommand = new Command("add")
  .description("Add one or more components or blocks, including transitive registry dependencies")
  .argument("<components...>", "component or block names, e.g. button dashboard-01")
  .action(async (components: string[]) => {
    const cwd = process.cwd();
    const config = await readConfig(cwd);

    // Theme tokens + Nova pack are required for components/blocks to render correctly.
    // Always resolve them so `add dashboard-01` installs the CSS baseline when missing.
    const requested = components.includes("theme") ? components : ["theme", ...components];
    const items = await resolveItems(config.registry, requested);

    const scalaDependencies = new Set<string>();
    let fileCount = 0;
    const names: string[] = [];

    for (const item of items) {
      names.push(item.name);
      for (const file of item.files) {
        const target = resolveInstallPath(cwd, config, file);
        await mkdir(path.dirname(target), { recursive: true });
        await writeFile(target, file.content);
        fileCount++;
        console.log(`+ ${path.relative(cwd, target)}`);
      }
      for (const dep of item.scalaDependencies) scalaDependencies.add(dep);
    }

    console.log(`\nAdded ${names.length} registry item(s), ${fileCount} file(s).`);
    console.log(`Resolved: ${names.join(", ")}`);

    if (scalaDependencies.size > 0) {
      console.log("\nAdd these to your build.sbt if not already present:");
      for (const dep of scalaDependencies) console.log(`  "${dep}"`);
    }
  });
