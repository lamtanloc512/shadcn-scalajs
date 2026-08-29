import { Command } from "commander";
import { mkdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";
import { readConfig, writeConfig } from "../utils/config.js";
import { resolveInstallPath } from "../utils/paths.js";
import { resolveItems } from "../utils/registry.js";
import { assertStylePack, detectStylePack, styleRegistryName, uiRootFromConfig } from "../utils/stylePack.js";

async function ensureGlobalsImport(cwd: string, sourceDir: string, stylePack: string): Promise<void> {
  const uiRoot = uiRootFromConfig(cwd, { registry: "", sourceDir });
  const globalsPath = path.join(uiRoot, "src", "styles", "globals.css");
  let css: string;
  try {
    css = await readFile(globalsPath, "utf8");
  } catch {
    return;
  }

  const packImport = `@import "./pack-${stylePack}.css";`;
  if (css.includes(packImport)) return;

  const next = css.replace(/@import\s+["']\.\/pack-[a-z]+\.css["']\s*;?/, packImport);
  if (next !== css) {
    await writeFile(globalsPath, next);
    console.log(`~ ${path.relative(cwd, globalsPath)} (style pack -> ${stylePack})`);
    return;
  }

  if (css.includes('@import "./tokens.css"')) {
    await writeFile(
      globalsPath,
      css.replace('@import "./tokens.css";', `@import "./tokens.css";\n${packImport}`)
    );
    console.log(`~ ${path.relative(cwd, globalsPath)} (added ${stylePack} pack import)`);
  }
}

async function ensureHtmlStylePack(cwd: string, sourceDir: string, stylePack: string): Promise<void> {
  const uiRoot = uiRootFromConfig(cwd, { registry: "", sourceDir });
  const htmlPath = path.join(uiRoot, "index.html");
  let html: string;
  try {
    html = await readFile(htmlPath, "utf8");
  } catch {
    return;
  }

  let next = html;
  if (/data-style-pack\s*=/.test(html)) {
    next = html.replace(/data-style-pack\s*=\s*["'][a-z]+["']/i, `data-style-pack="${stylePack}"`);
  } else {
    next = html.replace(/<html\b([^>]*)>/i, `<html$1 data-style-pack="${stylePack}">`);
  }
  if (next !== html) {
    await writeFile(htmlPath, next);
    console.log(`~ ${path.relative(cwd, htmlPath)} (data-style-pack="${stylePack}")`);
  }
}

export const addCommand = new Command("add")
  .description("Add one or more components or blocks, including transitive registry dependencies")
  .argument("<components...>", "component or block names, e.g. button dashboard-01")
  .option("--style <pack>", "style pack to install (nova, vega, maia, lyra, mira, luma, sera, rhea)")
  .action(async (components: string[], opts: { style?: string }) => {
    const cwd = process.cwd();
    const config = await readConfig(cwd);
    const stylePack = opts.style ? assertStylePack(opts.style) : await detectStylePack(cwd, config);
    const styleItem = styleRegistryName(stylePack);

    // Tokens + the project's active style pack + requested components.
    const requested = Array.from(
      new Set([
        "theme",
        styleItem,
        ...components.filter(name => name !== "theme" && !name.startsWith("style-"))
      ])
    );

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

    await ensureGlobalsImport(cwd, config.sourceDir, stylePack);
    await ensureHtmlStylePack(cwd, config.sourceDir, stylePack);

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
