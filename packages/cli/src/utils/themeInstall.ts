import { access, copyFile, mkdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import type { Config } from "./config.js";
import type { StylePack } from "./preset.js";
import { uiRootFromConfig } from "./stylePack.js";

function assetsDir(): string {
  return path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../assets");
}

async function exists(filePath: string): Promise<boolean> {
  return access(filePath).then(() => true).catch(() => false);
}

async function writeIfChanged(filePath: string, content: string): Promise<boolean> {
  const previous = await readFile(filePath, "utf8").catch(() => null);
  if (previous === content) return false;
  await mkdir(path.dirname(filePath), { recursive: true });
  await writeFile(filePath, content);
  return true;
}

/** Install tokens + the active style pack from CLI assets (works even if registry has no theme items yet). */
export async function installThemeFiles(
  cwd: string,
  config: Config,
  stylePack: StylePack
): Promise<string[]> {
  const uiRoot = uiRootFromConfig(cwd, config);
  const stylesDir = path.join(uiRoot, "src", "styles");
  const written: string[] = [];

  const copies: Array<{ from: string; to: string }> = [
    {
      from: path.join(assetsDir(), "styles", "tokens.css"),
      to: path.join(stylesDir, "tokens.css")
    },
    {
      from: path.join(assetsDir(), "styles", `pack-${stylePack}.css`),
      to: path.join(stylesDir, `pack-${stylePack}.css`)
    }
  ];

  for (const copy of copies) {
    if (!(await exists(copy.from))) {
      throw new Error(`Missing bundled theme asset: ${path.relative(assetsDir(), copy.from)}`);
    }
    await mkdir(path.dirname(copy.to), { recursive: true });
    await copyFile(copy.from, copy.to);
    written.push(path.relative(cwd, copy.to));
  }

  const globalsPath = path.join(stylesDir, "globals.css");
  // Always write the canonical entry CSS. Older scaffolds inlined incomplete tokens
  // (no sidebar/chart), which override `tokens.css` and break blocks like dashboard-01.
  const globalsCss = `@import "tailwindcss";
@import "tw-animate-css";
@import "./tokens.css";
@import "./pack-${stylePack}.css";
@source "../main/scala/**/*.scala";

@custom-variant dark (&:is(.dark *));

@layer base {
  * { @apply border-border outline-ring/50; }
  body { @apply m-0 bg-background text-foreground antialiased; }
}
`;
  if (await writeIfChanged(globalsPath, globalsCss)) {
    written.push(path.relative(cwd, globalsPath));
  }

  const htmlPath = path.join(uiRoot, "index.html");
  if (await exists(htmlPath)) {
    let html = await readFile(htmlPath, "utf8");
    let next = html;
    if (/data-style-pack\s*=/.test(html)) {
      next = html.replace(/data-style-pack\s*=\s*["'][a-z]+["']/i, `data-style-pack="${stylePack}"`);
    } else {
      next = html.replace(/<html\b([^>]*)>/i, `<html$1 data-style-pack="${stylePack}">`);
    }
    if (await writeIfChanged(htmlPath, next)) {
      written.push(path.relative(cwd, htmlPath));
    }
  }

  return written;
}
