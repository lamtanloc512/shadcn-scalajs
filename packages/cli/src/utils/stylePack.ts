import { access, readFile, readdir } from "node:fs/promises";
import path from "node:path";
import type { Config } from "./config.js";
import { STYLE_PACKS, isStylePack, stylePackFromPreset, type StylePack } from "./preset.js";

const PACK_FILE = /^pack-([a-z]+)\.css$/;

async function readText(filePath: string): Promise<string | undefined> {
  try {
    return await readFile(filePath, "utf8");
  } catch {
    return undefined;
  }
}

function uiRootFromConfig(cwd: string, config: Config): string {
  return path.resolve(cwd, config.sourceDir, "..", "..", "..", "..");
}

function packFromHtml(html: string): StylePack | undefined {
  const match = html.match(/data-style-pack\s*=\s*["']([a-z]+)["']/i);
  if (match && isStylePack(match[1])) return match[1];
  return undefined;
}

function packFromGlobals(css: string): StylePack | undefined {
  const match = css.match(/@import\s+["']\.\/pack-([a-z]+)\.css["']/);
  if (match && isStylePack(match[1])) return match[1];
  return undefined;
}

async function packFromStylesDir(stylesDir: string): Promise<StylePack | undefined> {
  try {
    const names = await readdir(stylesDir);
    const packs = names.flatMap(name => {
      const match = name.match(PACK_FILE)?.[1];
      return match && isStylePack(match) ? [match] : [];
    });
    if (packs.length === 1) return packs[0];
    // Prefer the pack imported by globals when multiple exist.
    return undefined;
  } catch {
    return undefined;
  }
}

/** Resolve the active style pack for this project. */
export async function detectStylePack(cwd: string, config: Config): Promise<StylePack> {
  if (config.stylePack && isStylePack(config.stylePack)) return config.stylePack;

  const uiRoot = uiRootFromConfig(cwd, config);
  const stylesDir = path.join(uiRoot, "src", "styles");
  const htmlCandidates = [
    path.join(uiRoot, "index.html"),
    path.join(cwd, "index.html"),
    path.join(cwd, "packages/ui/index.html")
  ];

  for (const htmlPath of htmlCandidates) {
    const html = await readText(htmlPath);
    if (!html) continue;
    const fromHtml = packFromHtml(html);
    if (fromHtml) return fromHtml;
  }

  const globals = await readText(path.join(stylesDir, "globals.css"));
  if (globals) {
    const fromGlobals = packFromGlobals(globals);
    if (fromGlobals) return fromGlobals;
  }

  const fromDir = await packFromStylesDir(stylesDir);
  if (fromDir) return fromDir;

  const fromPreset = stylePackFromPreset(config.preset);
  if (fromPreset) return fromPreset;

  return "nova";
}

export function styleRegistryName(pack: StylePack): string {
  return `style-${pack}`;
}

export function assertStylePack(pack: string): StylePack {
  if (!isStylePack(pack)) {
    throw new Error(`Unknown style pack: ${pack}. Expected one of ${STYLE_PACKS.join(", ")}`);
  }
  return pack;
}

export async function pathExists(filePath: string): Promise<boolean> {
  return access(filePath).then(() => true).catch(() => false);
}

export { uiRootFromConfig };
