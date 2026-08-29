import { Command } from "commander";
import { mkdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { readConfig, writeConfig } from "../utils/config.js";
import { resolveInstallPath } from "../utils/paths.js";
import { resolveItems } from "../utils/registry.js";
import { assertStylePack, detectStylePack } from "../utils/stylePack.js";
import { installThemeFiles } from "../utils/themeInstall.js";

export const addCommand = new Command("add")
  .description("Add one or more components or blocks, including transitive registry dependencies")
  .argument("<components...>", "component or block names, e.g. button dashboard-01 sidebar-07")
  .option("--style <pack>", "style pack to install (nova, vega, maia, lyra, mira, luma, sera, rhea)")
  .action(async (components: string[], opts: { style?: string }) => {
    const cwd = process.cwd();
    const config = await readConfig(cwd);
    const stylePack = opts.style ? assertStylePack(opts.style) : await detectStylePack(cwd, config);

    // Resolve only component/block graph from the registry. Theme CSS is installed from
    // bundled assets so style application works even when the hosted registry is older.
    const requested = components.filter(name => name !== "theme" && !name.startsWith("style-"));
    const items = await resolveItems(config.registry, requested);

    const scalaDependencies = new Set<string>();
    let fileCount = 0;
    const names: string[] = [];

    for (const item of items) {
      names.push(item.name);
      for (const file of item.files) {
        // Skip registry CSS theme payloads; assets are the source of truth for packs.
        if (file.type.startsWith("css:")) continue;
        const target = resolveInstallPath(cwd, config, file);
        await mkdir(path.dirname(target), { recursive: true });
        await writeFile(target, file.content);
        fileCount++;
        console.log(`+ ${path.relative(cwd, target)}`);
      }
      for (const dep of item.scalaDependencies) scalaDependencies.add(dep);
    }

    const themeFiles = await installThemeFiles(cwd, config, stylePack);
    for (const file of themeFiles) {
      console.log(`+ ${file}`);
      fileCount++;
    }

    if (config.stylePack !== stylePack) {
      await writeConfig(cwd, { ...config, stylePack });
      console.log(`~ shadcn-scalajs.json (stylePack=${stylePack})`);
    }

    console.log(`\nAdded ${names.length} registry item(s), ${fileCount} file(s).`);
    console.log(`Style pack: ${stylePack}`);
    console.log(`Resolved: ${names.join(", ")}`);

    if (scalaDependencies.size > 0) {
      console.log("\nAdd these to your build.sbt if not already present:");
      for (const dep of scalaDependencies) console.log(`  "${dep}"`);
    }
  });
