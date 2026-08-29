import { readFile, writeFile } from "node:fs/promises";
import path from "node:path";

/**
 * Project-local config, analogous to shadcn-svelte's `components.json`.
 *
 * v1 scope: written files keep their fixed `shadcnscalajs.ui`/`shadcnscalajs.core`
 * package declarations verbatim (no import-path rewriting, unlike shadcn-svelte's
 * transformImports) — so `sourceDir` must point at a directory literally named
 * `.../shadcnscalajs`, matching Scala's directory-matches-package convention;
 * `add` then writes e.g. `<sourceDir>/ui/Button.scala`. A generated scaffold includes
 * the small `core` package locally, so copied components compile without an unpublished
 * Maven artifact. Existing projects may provide that package from their own source or dependency.
 */
export interface Config {
  registry: string;
  sourceDir: string;
  preset?: string;
}

export const CONFIG_FILE_NAME = "shadcn-scalajs.json";

/** Local Vite registry (modules/site `npm run dev` → http://localhost:4300/registry).
 * Override with `init --registry` when using a deployed host. */
export const DEFAULT_REGISTRY = "https://shadcn-scalajs.vercel.app/registry";

export const defaultConfig: Config = {
  registry: DEFAULT_REGISTRY,
  sourceDir: "src/main/scala/shadcnscalajs"
};

export async function readConfig(cwd: string): Promise<Config> {
  const configPath = path.join(cwd, CONFIG_FILE_NAME);
  const raw = await readFile(configPath, "utf-8").catch(() => {
    throw new Error(`No ${CONFIG_FILE_NAME} found in ${cwd}. Run \`shadcn-scalajs init\` first.`);
  });
  return { ...defaultConfig, ...JSON.parse(raw) };
}

export async function writeConfig(cwd: string, config: Config): Promise<void> {
  const configPath = path.join(cwd, CONFIG_FILE_NAME);
  await writeFile(configPath, JSON.stringify(config, null, 2) + "\n");
}
